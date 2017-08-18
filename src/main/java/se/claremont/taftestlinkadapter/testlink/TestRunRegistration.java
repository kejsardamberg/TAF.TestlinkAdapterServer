package se.claremont.taftestlinkadapter.testlink;

import br.eti.kinoshita.testlinkjavaapi.constants.*;
import br.eti.kinoshita.testlinkjavaapi.model.*;
import se.claremont.taftestlinkadapter.eventstore.EventStoreManager;
import se.claremont.taftestlinkadapter.server.App;
import se.claremont.taftestlinkadapter.server.Settings;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * TAF test run registration. Tries to push the result of every test case
 * from the test run to Testlink. If the test case cannot be found in
 * Testlink it will be created.
 *
 * Created by jordam on 2017-03-24.
 */
public class TestRunRegistration {
    private TestlinkReporter testlinkReporter = null;
    private Boolean defaultTestSuiteFound = null;
    private Integer defaultTestSuiteId = null;
    private TestlinkElementCache cache = new TestlinkElementCache();
    public StringBuilder log = new StringBuilder();
    public StringBuilder debugLog = new StringBuilder();

    public TestRunRegistration() {
    }

    public class TestResultsProblemException extends Exception {
        public TestResultsProblemException(String message) {
            super(message);
        }
    }

    public void reportTestRun(String testRun) throws Exception{
        log("Reporting TAF test run results to Testlink endpoint '" + Settings.testlinkServerAddress + "'.");
        testlinkReporter = new TestlinkReporter();
        if(testRun == null || testRun.length() == 0) throw new TestResultsProblemException("Empty JSON to extract TAF TestRun from. Nothing to register to Testlink.");
        EventStoreManager.registerTestRun(testRun);
        TestlinkTestCasesFromTestRun testlinkTestCasesFromTestRun = getTestlinkReporterObject(testRun);
        if(testlinkTestCasesFromTestRun== null) throw new TestResultsProblemException("Could not find any test cases in TAF TestRun JSON.");
        logToDebugLog(cache.updateContent(testlinkReporter));
        reportTestCases(testlinkTestCasesFromTestRun);
        log("Reported all test cases.");
    }

    private void log(String message){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        Date now = new Date();
        log.append(sdf.format(now)).append("   ").append(message).append(System.lineSeparator());
        logToDebugLog(message);
    }

    private void logToDebugLog(String message){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        Date now = new Date();
        debugLog.append(sdf.format(now)).append("   ").append(message).append(System.lineSeparator());
        logToDebugLog(sdf.format(now) + "   " + message);
    }


    private void reportTestCases(TestlinkTestCasesFromTestRun testlinkTestCasesFromTestRun) throws Exception{
        for(TestlinkTestCaseMapper testCase : testlinkTestCasesFromTestRun.testCases){
            if(testCase == null) continue;
            reportTestCaseToTestlink(testCase);
        }
    }

    private TestlinkTestCasesFromTestRun getTestlinkReporterObject(String json){
        TestlinkTestCasesFromTestRun testlinkTestCasesFromTestRun = null;
        try {
            testlinkTestCasesFromTestRun = App.mapper.readValue(json, TestlinkTestCasesFromTestRun.class);
        } catch (IOException e) {
            log("Oups! Could not map json to TestlinkTestCasesFromTestRun object. Error: " + e.toString());
        }
        if (testlinkTestCasesFromTestRun == null){
            log("Oups! Could not interprete json '" + json + "' to a TestlinkTestCasesFromTestRun object.");
        }
        return testlinkTestCasesFromTestRun;
    }

    private void reportTestCaseToTestlink(TestlinkTestCaseMapper testCase) throws Exception{
        //noinspection ConstantConditions
        logToDebugLog("Starting registration of test case '" + testCase.testSetName + "/" + testCase.testName + "'.");
        TestCaseMatch testCaseToWorkWith = identifyTestCaseToReportTo2(testCase);
        if(testCaseToWorkWith == null) {
            log("Could neither identify nor create test case in Testlink for test case '" + testCase.testSetName + "/" + testCase.testName + "'.");
            return;
        }
        markTestCaseAsAutomatedIfItIsNotAlreadyMarkedAsAutomated(testCaseToWorkWith);
        log(postTestCaseResultToTestlink(testCaseToWorkWith, testCase));
    }

    private void markTestCaseAsAutomatedIfItIsNotAlreadyMarkedAsAutomated(TestCaseMatch testCaseToWorkWith) {
        if(testCaseToWorkWith.testCase.getExecutionType().equals(ExecutionType.AUTOMATED))return;
        try{
            testlinkReporter.api.api.setTestCaseExecutionType(
                    testCaseToWorkWith.projectId,
                    testCaseToWorkWith.testCase.getId(),
                    Integer.parseInt(testCaseToWorkWith.testCase.getFullExternalId()),
                    testCaseToWorkWith.testCase.getVersion(),
                    ExecutionType.AUTOMATED);
            logToDebugLog("The identified Testlink test case is not marked as automated in Testlink. Changing Testlink ExecutionType to AUTOMATED for test case '" + testCaseToWorkWith.testCase.getName() + "'.");
        }catch (Exception e){
            log("Something went wrong when trying to mark test case '" + testCaseToWorkWith.testCase.getName() + " as ExecutionType AUTOMATED in Testlink. Error message: " + e.getMessage());
        }
    }

    private String postTestCaseResultToTestlink(TestCaseMatch testCaseMatch, TestlinkTestCaseMapper testCase){
        ReportTCResultResponse results = testlinkReporter.api.api.reportTCResult(
                testCaseMatch.testCase.getId(),
                null,
                testCaseMatch.testPlanId,
                executionStatus(testCase),
                testCaseMatch.buildId,
                null,
                testCase.notes,
                null,
                null,
                null,
                null,
                null,
                null );
        return "Reported test case '" + testCase.testSetName + "/" + testCase.testName + "' to '" + testCaseMatch.testCase.toString() + "'. Results: " + results.getMessage();
    }

    private TestCaseMatch identifyTestCaseToReportTo2(TestlinkTestCaseMapper testCase) {
        List<TestCase> potentialMatches = identifyPotentialMatches(testCase);
        if(potentialMatches.size() == 0){
            return createNewTestCase(testCase);
        } else if (potentialMatches.size() == 1){
            return getTestCaseMatch(testCase, potentialMatches);
        } else {
            TestCaseMatch singleAutomatedTestCase = getSingleTestCaseMarkedAsAutomated(potentialMatches, testCase);
            if(singleAutomatedTestCase != null) return singleAutomatedTestCase;
            TestCaseMatch exactNameMatchingTestCase = identifyTestCaseWithExactNameMatch(potentialMatches, testCase);
            if(exactNameMatchingTestCase != null) return exactNameMatchingTestCase;

            List<TestCase> containsTestSuiteNameMatchesToTestSetName = identifyTestCasesWithTestSuiteNameCorrespondingToTafTestSetName(testCase, potentialMatches);
            if(containsTestSuiteNameMatchesToTestSetName.size() == 1) return getSingleTestCaseWithBothTestSetAndTestNameContainsMatch(testCase, containsTestSuiteNameMatchesToTestSetName);

            TestCaseMatch exactTestSetNameMatchAndTestNameMatch = getTestCaseWithExactTestSetNameMatchAndTestNameMatch(containsTestSuiteNameMatchesToTestSetName, testCase);
            if(exactTestSetNameMatchAndTestNameMatch != null) return exactTestSetNameMatchAndTestNameMatch;
        }
        return getFirstTestCaseMatch(testCase, potentialMatches);
    }

    private TestCaseMatch getSingleTestCaseMarkedAsAutomated(List<TestCase> potentialMatches, TestlinkTestCaseMapper testCase) {
        List<TestCase> potentialTestCaseMatchesMarkedAsAutomated = new ArrayList<>();
        for(TestCase potentialMatch : potentialMatches){
            if(potentialMatch.getExecutionType().equals(ExecutionType.AUTOMATED)){
                potentialTestCaseMatchesMarkedAsAutomated.add(potentialMatch);
            }
        }
        if(potentialTestCaseMatchesMarkedAsAutomated.size() == 1){
            TestProject testProject = null;
            for(TestProject testProject1 : cache.testProjects){
                if(testProject1.getId().equals(potentialTestCaseMatchesMarkedAsAutomated.get(0).getTestProjectId())){
                    testProject = testProject1;
                    break;
                }
            }
            TestPlan testPlan = identifyTestPlan(testProject, testCase);
            Build build = identifyBuild(testPlan);
            return new TestCaseMatch(potentialTestCaseMatchesMarkedAsAutomated.get(0), testPlan.getId(), potentialTestCaseMatchesMarkedAsAutomated.get(0).getTestSuiteId(), potentialTestCaseMatchesMarkedAsAutomated.get(0).getTestProjectId(), build.getId() );
        }
        return null;
    }

    private TestCaseMatch getFirstTestCaseMatch(TestlinkTestCaseMapper testCase, List<TestCase> potentialMatches) {
        StringBuilder logMessage = new StringBuilder("Found multiple potential test cases for result reporting in Testlink. Using the first potential Testlink test case since there are several matching the test case." + System.lineSeparator());
        for(TestCase potentialMatch : potentialMatches){
            logMessage.append(" * Test case name: '").append(potentialMatch.getName()).append("' (suite id: ").append(potentialMatch.getId()).append(").").append(System.lineSeparator());
        }
        logMessage.append("Using the first test case: ").append(potentialMatches.get(0).toString());
        logToDebugLog(logMessage.toString());
        TestCase theTestCaseInstanceInTestlink = potentialMatches.get(0);
        TestProject testProject = null;
        for(TestProject testProject1 : cache.testProjects){
            if(testProject1.getId().equals(theTestCaseInstanceInTestlink.getTestProjectId())){
                testProject = testProject1;
                break;
            }
        }
        TestPlan testPlan = identifyTestPlan(testProject, testCase);
        Build build = identifyBuild(testPlan);
        return new TestCaseMatch(theTestCaseInstanceInTestlink, testPlan.getId(), theTestCaseInstanceInTestlink.getTestSuiteId(), theTestCaseInstanceInTestlink.getTestProjectId(), build.getId() );
    }

    private TestCaseMatch getTestCaseWithExactTestSetNameMatchAndTestNameMatch(List<TestCase> containsTestSuiteNameMatchesToTestSetName, TestlinkTestCaseMapper testCase){
        for (TestCase potentialExactTestSuiteNameMatchToTestSetName : containsTestSuiteNameMatchesToTestSetName){
            List<Integer> suiteIds = new ArrayList<>();
            suiteIds.add(potentialExactTestSuiteNameMatchToTestSetName.getTestSuiteId());
            TestSuite[] testSuites = testlinkReporter.api.api.getTestSuiteByID(suiteIds);
            for(TestSuite suite : testSuites){
                if(suite.getName().equals(testCase.testSetName)){
                    logToDebugLog("Found exactly one Testlink test case with both a name ('" + potentialExactTestSuiteNameMatchToTestSetName.getName() + "') corresponding to the TAF test case name '" + testCase.testName + "', and a test suite '" + suite.getName() + "' exactly matching the TAF TestSet name '" + testCase.testSetName + "'.");
                    TestProject testProject = null;
                    for(TestProject testProject1 : cache.testProjects){
                        if(testProject1.getId().equals(potentialExactTestSuiteNameMatchToTestSetName.getTestProjectId())){
                            testProject = testProject1;
                            break;
                        }
                    }
                    TestPlan testPlan = identifyTestPlan(testProject, testCase);
                    Build build = identifyBuild(testPlan);
                    return new TestCaseMatch(potentialExactTestSuiteNameMatchToTestSetName, testPlan.getId(), potentialExactTestSuiteNameMatchToTestSetName.getTestSuiteId(), potentialExactTestSuiteNameMatchToTestSetName.getTestProjectId(), build.getId() );
                }
            }
        }
        return null;
    }

    private TestCaseMatch getSingleTestCaseWithBothTestSetAndTestNameContainsMatch(TestlinkTestCaseMapper testCase, List<TestCase> containsTestSuiteNameMatchesToTestSetName) {
        logToDebugLog("Found exactly one Testlink test case with both a name corresponding to the TAF test case '" + testCase.testName + "', and a test suite '" + containsTestSuiteNameMatchesToTestSetName.get(0).getName() + "' containing the TAF TestSet name '" + testCase.testSetName + "'.");
        TestProject testProject = null;
        for(TestProject testProject1 : cache.testProjects){
            if(testProject1.getId().equals(containsTestSuiteNameMatchesToTestSetName.get(0).getTestProjectId())){
                testProject = testProject1;
                break;
            }
        }
        TestPlan testPlan = identifyTestPlan(testProject, testCase);
        Build build = identifyBuild(testPlan);
        return new TestCaseMatch(containsTestSuiteNameMatchesToTestSetName.get(0), testPlan.getId(), containsTestSuiteNameMatchesToTestSetName.get(0).getTestSuiteId(), containsTestSuiteNameMatchesToTestSetName.get(0).getTestProjectId(), build.getId() );
    }

    private List<TestCase> identifyTestCasesWithTestSuiteNameCorrespondingToTafTestSetName(TestlinkTestCaseMapper testCase, List<TestCase> potentialMatches) {
        List<TestCase> containsTestSuiteNameMatchesToTestSetName = new ArrayList<>();
        for(TestCase potentialMatch : potentialMatches){
            List<Integer> suiteIds = new ArrayList<>();
            suiteIds.add(potentialMatch.getTestSuiteId());
            TestSuite[] testSuites = testlinkReporter.api.api.getTestSuiteByID(suiteIds);
            for(TestSuite suite : testSuites){
                if(suite.getName().contains(testCase.testSetName)){
                    containsTestSuiteNameMatchesToTestSetName.add(potentialMatch);
                    //return new TestCaseMatch(potentialMatch, null, suite.getId(), potentialMatch.getTestProjectId(), null );
                }
            }
        }
        return containsTestSuiteNameMatchesToTestSetName;
    }

    private List<TestCase> identifyPotentialMatches(TestlinkTestCaseMapper testCase) {
        List<TestCase> potentialMatches = new ArrayList<>();
        for(TestCase registeredTestCase : cache.testCases){
            if(registeredTestCase.getName().contains(testCase.testName)){
                potentialMatches.add(registeredTestCase);
            }
        }
        return potentialMatches;
    }

    private TestCaseMatch identifyTestCaseWithExactNameMatch(List<TestCase> potentialMatches, TestlinkTestCaseMapper testCase){
        List<TestCase> exactNameMatches = new ArrayList<>();
        for(TestCase potentialMatch : potentialMatches){
            if(potentialMatch.getName().equals(testCase.testName)){
                exactNameMatches.add(potentialMatch);
            }
        }
        if(exactNameMatches.size() == 1) {
            logToDebugLog("Found exactly one Testlink test case with a name exactly as the TAF test case name. Using test case '" + exactNameMatches.get(0).getName() + "' for reporting.");
            TestProject testProject = null;
            for(TestProject testProject1 : cache.testProjects){
                if(testProject1.getId().equals(exactNameMatches.get(0).getTestProjectId())){
                    testProject = testProject1;
                    break;
                }
            }
            TestPlan testPlan = identifyTestPlan(testProject, testCase);
            Build build = identifyBuild(testPlan);
            return new TestCaseMatch(exactNameMatches.get(0), testPlan.getId(), exactNameMatches.get(0).getTestSuiteId(), exactNameMatches.get(0).getTestProjectId(), build.getId() );
        }
        return null;
    }

    private TestCaseMatch getTestCaseMatch(TestlinkTestCaseMapper testCase, List<TestCase> potentialMatches) {
        logToDebugLog("Found exactly one Testlink test case with a name containing the TAF test case name. Using test case '" + potentialMatches.get(0).getName() + "' for reporting.");
        TestCase theTestCaseInstanceInTestlink = potentialMatches.get(0);
        TestProject testProject = null;
        for(TestProject testProject1 : cache.testProjects){
            if(testProject1.getId().equals(theTestCaseInstanceInTestlink.getTestProjectId())){
                testProject = testProject1;
                break;
            }
        }
        if(testProject == null) testProject = identifyTestProject(testCase);
        TestPlan testPlan = identifyTestPlan(testProject, testCase);
        Build build = identifyBuild(testPlan);
        return new TestCaseMatch(theTestCaseInstanceInTestlink, testPlan.getId(), theTestCaseInstanceInTestlink.getTestSuiteId(), theTestCaseInstanceInTestlink.getTestProjectId(), build.getId() );
    }

    private TestCaseMatch createNewTestCase(TestlinkTestCaseMapper testCase) {
        logToDebugLog("Zero potential matches for test case found in Testlink.");
        TestCaseMatch testCaseMatch = createTestCaseInTestlink(testCase);
        cache.testCases.add(testCaseMatch.testCase);
        return testCaseMatch;
    }


    private ExecutionStatus executionStatus(TestlinkTestCaseMapper testCase) {
        switch (testCase.executionStatus){
            case "PASSED":
                return ExecutionStatus.PASSED;
            case "UNEVALUATED":
                return ExecutionStatus.BLOCKED;
            default:
                return ExecutionStatus.FAILED;
        }
    }


    private TestCaseMatch createTestCaseInTestlink(TestlinkTestCaseMapper testCase) {
        TestProject testProject = identifyTestProject(testCase);
        TestSuite testSuite = identifyTestSuite(testProject, testCase);
        TestPlan testPlan = identifyTestPlan(testProject, testCase);
        Build build = identifyBuild(testPlan);
        log("Creating a new test case called '" + testCase.testName + "' in test project '" + testProject.getName() + "', and test suite '" + testSuite.getName() + "'.");
        TestCase testlinkTestCase = testlinkReporter.api.api.createTestCase(
                testCase.testName, //name
                testSuite.getId(), //testSuiteId
                testProject.getId(),  //projectId
                Settings.testlinkUserName, //author login
                testCase.testName, //Summary
                null,  //Steps
                null, //preconditions
                TestCaseStatus.FINAL, //Test case status
                TestImportance.MEDIUM, //Test importance
                ExecutionType.AUTOMATED, //execution type
                null, //Order
                null, //Internal id
                null, //Full external id
                ActionOnDuplicate.GENERATE_NEW    //Action on duplicate
            );
        log("Created '" + testlinkTestCase.toString() + "'. Adding it to test plan '" + testPlan.getName() + "'.");
        testlinkReporter.api.api.addTestCaseToTestPlan(testProject.getId(), testPlan.getId(), testlinkTestCase.getId(), 1, null, null, null);
        return new TestCaseMatch(
                testlinkTestCase, //Feature id
                testPlan.getId(),
                testSuite.getId(),
                testProject.getId(),
                build.getId());
    }

    private Build identifyBuild(TestPlan testPlan) {
        Build[] builds = testlinkReporter.api.api.getBuildsForTestPlan(testPlan.getId());
        if(builds.length == 1) return builds[0];
        if(builds.length == 0) return testlinkReporter.api.api.createBuild(testPlan.getId(), "Default automation build", "Automatically created from TAF Testlink adapter server.");
        return testlinkReporter.api.api.getLatestBuildForTestPlan(testPlan.getId());
    }

    private TestPlan identifyTestPlan(TestProject testProject, TestlinkTestCaseMapper testCase) {
        TestPlan[] testPlansForThisProject = testlinkReporter.api.api.getProjectTestPlans(testProject.getId());
        if(testProject == null) logToDebugLog("Null project when trying to identify test plan.");

        //If this test project only contains one test plan this should be returned
        if(testPlansForThisProject.length == 1) return testPlansForThisProject[0];

        //If a TestPlan contains a TestCase with correct name, return it
        for(TestPlan testPlan : testPlansForThisProject){
            TestCase[] testCasesForTestPlan = testlinkReporter.api.api.getTestCasesForTestPlan(testPlan.getId(), null, null, null, null, null, null, null, null, null, null);
            for(TestCase foundTestCase : testCasesForTestPlan){
                if(foundTestCase.getName().equals(testCase.testName)){
                    return testPlan;
                }
            }
        }

        //Else create a new test plan
        TestPlan newTestPlan = testlinkReporter.api.api.createTestPlan(Settings.defaultTestPlanName, testProject.getName(), null, true, true);
        cache.testPlans.add(newTestPlan);
        return newTestPlan;
    }

    private TestSuite identifyTestSuite(TestProject testProject, TestlinkTestCaseMapper testCase) {
        //If a Testlink test suite exist with a name corresponding to the testCase TestSet, and with a Testlink TestCase with the same name as the TAF test case name, return this.
        for(TestSuite testSuite : cache.testSuites){
            if(testSuite.getName().equals(testCase.testSetName)){
                for(TestCase test : cache.testCases){
                    if(test.getTestSuiteId() == null) continue;
                    if(test.getTestSuiteId().equals(testSuite.getId()) && testCase.testName.equals(test.getName())){
                        for(TestProject project : cache.testProjects){
                            if(project.getId().equals(testSuite.getTestProjectId())){
                                return testSuite;
                            }
                        }
                    }
                }
            }
        }
        updateDefaultTestSuiteStatus(testProject.getId());
        //Create default test test suite for orphan test cases if needed
        if(!defaultTestSuiteFound){
            TestSuite newlyCreatedDefaultTestSuite = testlinkReporter.api.api.createTestSuite(testProject.getId(), Settings.defaultTestSuiteNameForNewTestCases, null, null, null, null, null);
            cache.testSuites.add(newlyCreatedDefaultTestSuite);
            defaultTestSuiteId = newlyCreatedDefaultTestSuite.getId();
        }
        //If a test suite with the same name as the test set exist, return it
        TestSuite[] automationTestSuites = testlinkReporter.api.api.getTestSuitesForTestSuite(defaultTestSuiteId);
        for(TestSuite testSuite : automationTestSuites){
            if(testSuite.getName().equals(testCase.testSetName)){
                return testSuite;
            }
        }
        //Else create it and return it
        TestSuite newlyCreatedTestSuite = testlinkReporter.api.api.createTestSuite(testProject.getId(), testCase.testSetName, null, defaultTestSuiteId, null, null, null);
        cache.testSuites.add(newlyCreatedTestSuite);
        return newlyCreatedTestSuite;
    }

    private TestProject identifyTestProject(TestlinkTestCaseMapper testCase) {
        Integer projectId = null;
        if(cache.testProjects.size() == 1){
            //If only one test project exist this should be used
            return cache.testProjects.get(0);
        } else {
            //Try finding a Testlink project with a TestSuite with the same name as the TAF testCase TestSet.
            for(TestSuite testSuite : cache.testSuites){
                if(testSuite.getName().equals(testCase.testSetName)){
                    for(TestCase test : cache.testCases){
                        if(test.getTestSuiteId().equals(testSuite.getId()) && testCase.testName.equals(test.getName())){
                            for(TestProject project : cache.testProjects){
                                if(project.getId().equals(testSuite.getTestProjectId())){
                                    return project;
                                }
                            }
                        }
                    }
                }
            }
            //Return default TestProject in Testlink if exist
            for(TestProject testProject : cache.testProjects){
                if(testProject.getName().equals(Settings.defaultTestProjectNameForNewTestCases)){
                    return testProject;
                }
            }
            //Othervice create a new default TestProject in Testlink
            TestProject newlyCreatedTestProject = testlinkReporter.api.api.createTestProject(Settings.defaultTestProjectNameForNewTestCases, "ZX", "Automatically created for test automation test cases that cannot be identified already", true, true, true, true, true, true);
            cache.testProjects.add(newlyCreatedTestProject);
            return newlyCreatedTestProject;
        }

    }

    private void updateDefaultTestSuiteStatus(Integer projectId) {
        if(defaultTestSuiteFound == null){
            TestSuite[] candidateTestSuites = testlinkReporter.api.api.getFirstLevelTestSuitesForTestProject(projectId);
            defaultTestSuiteFound = false;
            for(TestSuite candidateTestSuite : candidateTestSuites){
                if(candidateTestSuite.getName().equals(Settings.defaultTestSuiteNameForNewTestCases)){
                    defaultTestSuiteFound = true;
                    defaultTestSuiteId = candidateTestSuite.getId();
                    break;
                }
            }
        }
    }


    class TestCaseMatch{
        br.eti.kinoshita.testlinkjavaapi.model.TestCase testCase;
        Integer testPlanId;
        Integer testSuiteId;
        Integer projectId;
        Integer buildId;

        TestCaseMatch(br.eti.kinoshita.testlinkjavaapi.model.TestCase testCase, Integer testPlanId, Integer testSuiteId, Integer projectId, Integer buildId){
            this.testPlanId = testPlanId;
            this.testSuiteId = testSuiteId;
            this.projectId = projectId;
            this.testCase = testCase;
            this.buildId = buildId;
        }

    }
}
