package se.claremont.taftestlinkadapter.testlink;

import br.eti.kinoshita.testlinkjavaapi.constants.*;
import br.eti.kinoshita.testlinkjavaapi.model.*;
import se.claremont.taftestlinkadapter.cache.*;
import se.claremont.taftestlinkadapter.eventstore.EventStoreManager;
import se.claremont.taftestlinkadapter.server.App;
import se.claremont.taftestlinkadapter.server.Settings;

import java.io.IOException;
import java.util.*;

/**
 * TAF test run registration. Tries to push the result of every test case
 * from the test run to Testlink. If the test case cannot be found in
 * Testlink it will be created.
 *
 * Created by jordam on 2017-03-24.
 */
public class TestRunRegistration {
    List<String> registrationResult = new ArrayList<>();
    TestlinkReporter testlinkReporter = null;
    static TestlinkCache testlinkCache;

    public TestRunRegistration(String testRun) {
        testlinkReporter = new TestlinkReporter();
        if(testRun == null)return;
        EventStoreManager.registerTestRun(testRun);
        TestlinkTestCasesFromTestRun testlinkTestCasesFromTestRun = getTestlinkReporterObject(testRun);
        //TestlinkAdapterTestRunReporter testRunReporterObject = getTestlinkReporterObject(testRun);
        if(testlinkTestCasesFromTestRun== null) return;
        testlinkCache = new TestlinkCache(testlinkReporter);
        reportTestCases(testlinkTestCasesFromTestRun);
        System.out.println("Reported all test cases.");
    }

    public String result() {
        return String.join(", ", registrationResult);
    }

    private void reportTestCases(TestlinkTestCasesFromTestRun testlinkTestCasesFromTestRun){
        for(TestlinkTestCaseMapper testCase : testlinkTestCasesFromTestRun.testCases){
            if(testCase == null) continue;
            registrationResult.add(reportTestCaseToTestlink(testCase));
            System.out.println("Reported test run for test case '" + testCase.testName + "' to Testlink.");
        }
    }

    private TestlinkTestCasesFromTestRun getTestlinkReporterObject(String json){
        TestlinkTestCasesFromTestRun testlinkTestCasesFromTestRun = null;
        try {
            testlinkTestCasesFromTestRun = App.mapper.readValue(json, TestlinkTestCasesFromTestRun.class);
        } catch (IOException e) {
            System.out.println("Oups! Could not map json to TestlinkTestCasesFromTestRun object. Error: " + e.toString());
        }
        if (testlinkTestCasesFromTestRun == null){
            System.out.println("Oups! Could not interprete json '" + json + "' to a TestlinkTestCasesFromTestRun object.");
        }
        return testlinkTestCasesFromTestRun;
    }

    private String reportTestCaseToTestlink(TestlinkTestCaseMapper testCase){
        StringBuilder logMessage = new StringBuilder();
        //noinspection ConstantConditions
        TestCaseMatch testCaseToWorkWith = identifyTestCaseToReportTo(testCase);
        if(testCaseToWorkWith == null) {
            logMessage.append("Oups! Something went wrong identifying or creating test case in Testlink.").append(System.lineSeparator());
            return logMessage.toString();
        }
        postTestCaseResultToTestlink(testCaseToWorkWith, testCase);
        return logMessage.toString();
    }

    private void postTestCaseResultToTestlink(TestCaseMatch testCaseMatch, TestlinkTestCaseMapper testCase){
        testlinkReporter.api.api.reportTCResult(
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
        System.out.println("Reported status to Testlink.");
    }

    private TestCaseMatch identifyTestCaseToReportTo(TestlinkTestCaseMapper testCase){
        StringBuilder logMessage = new StringBuilder();
        Map<Integer, String> testCaseNames = new HashMap<>();
        Set<TestCaseMatch> matchingTestlinkTestCases = new HashSet<>();
        for(TestlinkTestProjectCacheElement testProject : testlinkCache.getTestProjects()){
            Integer projectId = testProject.getId();
            for(TestlinkTestPlanCacheElement testPlan : testlinkCache.getTestPlans(projectId)){
                Integer testPlanId = testPlan.getId();
                for(TestlinkTestSuiteCacheElement testSuite : testlinkCache.getTestSuites(testPlanId)){
                    Integer testSuiteId = testSuite.getId();
                    for(TestlinkTestCaseCacheElement testlinkTestCase : testlinkCache.getTestCases(testSuiteId)){
                        testCaseNames.put(testlinkTestCase.getTestCase().getInternalId(), testProject.getTestProject().getName() + "." + testPlan.getTestPlan().getName() + "." + testSuite.getTestSuite().getName() + "." + testlinkTestCase.getTestCase().getName());
                        if(testlinkTestCase.getTestCase().getName().toLowerCase().equals(testCase.testName.toLowerCase())){
                            if(testPlan.getTestPlan().getName().toLowerCase().contains(testCase.testSetName.toLowerCase()) || testSuite.getTestSuite().getName().toLowerCase().contains(testCase.testSetName.toLowerCase())){
                                logMessage.append("Found matching test case and test set/test plan for test case '" + testCase.testName + "'.");
                                System.out.println(logMessage.toString());
                                return new TestCaseMatch(testlinkTestCase.getTestCase(), testPlanId, testSuiteId, projectId, createBuildIfNoneExist(testPlanId));
                                /*
                                createBuildIfNoneExist(testPlanId);
                                testlinkReporter.api.api.reportTCResult(
                                        testlinkTestCase.getId(),
                                        null,
                                        testPlan.getId(),
                                        executionStatus(testCase),
                                        null,
                                        testlinkReporter.api.api.getLatestBuildForTestPlan(testPlanId).getName(),
                                        testCase.notes,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null );
                                logMessage.append("Reported status to Testlink.").append(System.lineSeparator());
                                return logMessage.toString();
                                */
                            }
                            Integer buildId = createBuildIfNoneExist(testPlanId);
                            matchingTestlinkTestCases.add(new TestCaseMatch(testlinkTestCase.getTestCase(), testPlanId, testSuiteId, projectId, buildId));
                            logMessage.append("Found test case with matching test case name, but no matching test set name in testlink: ").append(testlinkTestCase.getTestCase().getName()).append(".").append(System.lineSeparator());
                        }
                    }
                }
            }
        }
        TestCaseMatch testCaseToWorkWith;
        if(matchingTestlinkTestCases.size() == 0){
            testCaseToWorkWith = createTestCaseInTestlink(testCase);
            logMessage.append("Created test case with id '").append(testCaseToWorkWith.testCase.getFullExternalId()).append("' in Testlink since it could not be found.").append(System.lineSeparator());
        } else if(matchingTestlinkTestCases.size() == 1) {
            testCaseToWorkWith = (TestCaseMatch) matchingTestlinkTestCases.toArray()[0];
            logMessage.append("Found matching test case with external id '").append(testCaseToWorkWith.testCase.getFullExternalId()).append("' in Testlink.").append(System.lineSeparator());
        } else {
            StringBuilder sb = new StringBuilder();
            for(Map.Entry<Integer, String> entry : testCaseNames.entrySet()) {
                sb.append("   ").append(entry.getValue()).append(System.lineSeparator());
            }
            logMessage.append("Found several matches for test case. None of them could be a match for test set name. Test case id's: ").append(matchingTestlinkTestCases.toString()).append(System.lineSeparator()).append("All test cases found in Testlink:").append(System.lineSeparator()).append(sb.toString());
            testCaseToWorkWith = (TestCaseMatch) matchingTestlinkTestCases.toArray()[0];
            logMessage.append("Countinuing to report to first encountered test case, with id '").append(testCaseToWorkWith.testCase.getFullExternalId()).append("'.").append(System.lineSeparator());
        }
        System.out.println(logMessage.toString());
        return testCaseToWorkWith;
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

    private Integer createBuildIfNoneExist(Integer testPlanId){
        Build build = null;
        try{
            build = testlinkReporter.api.api.getLatestBuildForTestPlan(testPlanId);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        if(build == null){
            build = testlinkReporter.api.api.createBuild(testPlanId, "Test automation build", "Automatically created from TAF Testlink Adapted.");
        }
        return build.getId();
    }


    private TestCaseMatch createTestCaseInTestlink(TestlinkTestCaseMapper testCase) {

        Integer projectId = testlinkReporter.createTestProjectInTestlinkIfNotExistThere("Test automation project");
        Integer testPlanId = testlinkReporter.createTestPlanInTestlinkIfNotExistThere("Test automation project", "Automated regression tests");
        Integer testSuiteId = testlinkReporter.createTestSuiteInTestlinkIfNotExistThere("Test automation project", testCase.testSetName, "Automated regression tests");
        Integer buildId = createBuildIfNoneExist(testPlanId);
        br.eti.kinoshita.testlinkjavaapi.model.TestCase testlinkTestCase = testlinkReporter.api.api.createTestCase(
                testCase.testName, //name
                testSuiteId, //testSuiteId
                projectId,  //projectId
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
        //Integer testCaseIdToTry = getNextTestCaseId();
        testlinkReporter.api.api.addTestCaseToTestPlan(projectId, testPlanId, testlinkTestCase.getId(), 1, null, null, null);
        return new TestCaseMatch(
                testlinkTestCase, //Feature id
                testPlanId,
                testSuiteId,
                projectId,
                buildId);
    }


    class TestCaseMatch{
        public br.eti.kinoshita.testlinkjavaapi.model.TestCase testCase;
        public Integer testPlanId;
        public Integer testSuiteId;
        public Integer projectId;
        public Integer buildId;

        public TestCaseMatch(br.eti.kinoshita.testlinkjavaapi.model.TestCase testCase, Integer testPlanId, Integer testSuiteId, Integer projectId, Integer buildId){
            this.testPlanId = testPlanId;
            this.testSuiteId = testSuiteId;
            this.projectId = projectId;
            this.testCase = testCase;
            this.buildId = buildId;
        }

    }
}
