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
import java.util.Map;

/**
 * TAF test run registration. Tries to push the result of every test case
 * from the test run to Testlink. If the test case cannot be found in
 * Testlink it will be created.
 * <p>
 * Created by jordam on 2017-03-24.
 */
public class TestRunRegistration {
    public StringBuilder log = new StringBuilder();
    public StringBuilder debugLog = new StringBuilder();
    private TestlinkReporter testlinkReporter = null;
    private Boolean defaultTestSuiteFound = null;
    private Integer defaultTestSuiteId = null;
    private TestlinkElementCache cache = new TestlinkElementCache();

    /**
     * Empty constructor used for logging upon fails
     */
    public TestRunRegistration() {
    }

    /**
     * Takes the information in the testRunJsonString parameter, interprets it and sends it to Testlink.
     *
     * @param testRunJsonString The JSON from the TAF test run TestRunResult object.
     * @throws Exception If anything goes wrong during test results importing an exception could be thrown,
     *                   altering the logging behavior.
     */
    public void reportTestRun(String testRunJsonString) throws Exception {
        log("Reporting TAF test run results to Testlink endpoint '" + Settings.testlinkServerAddress + "'.");
        testlinkReporter = new TestlinkReporter();
        if (testRunJsonString == null || testRunJsonString.length() == 0)
            throw new TestResultsProblemException("Empty JSON to extract TAF TestRun from. Nothing to register to Testlink.");
        EventStoreManager.registerTestRun(testRunJsonString);
        TestlinkTestCasesFromTestRun testlinkTestCasesFromTestRun = getTestlinkReporterObject(testRunJsonString);
        if (testlinkTestCasesFromTestRun == null)
            throw new TestResultsProblemException("Could not find any test cases in TAF TestRun JSON.");
        logToDebugLog(cache.updateContent(testlinkReporter));
        reportTestCases(testlinkTestCasesFromTestRun);
        log("Reported all test cases.");
    }

    //Todo: Test cases being created does not get a test result on first run, but subsequent ones work fine.

    /**
     * Add a log message to the unified logging
     *
     * @param message Log message string to add to the normal (non-debug) log.
     */
    private void log(String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        Date now = new Date();
        log.append(sdf.format(now)).append("   ").append(message).append(System.lineSeparator());
        logToDebugLog(message);
    }

    /**
     * Log to a specific debug log holding more information than the regular log.
     * The debugging information is only meant to be relevant upon failure,
     * and upon failures the debug log is returned rather than the normal log.
     *
     * @param message Log message string to add to the debug log.
     */
    private void logToDebugLog(String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        Date now = new Date();
        debugLog.append(sdf.format(now)).append("   ").append(message).append(System.lineSeparator());
        //logToDebugLog(sdf.format(now) + "   " + message);
    }

    /**
     * Takes the test cases from the test run, one by one, and attempts to push the
     * test case run results to Testlink.
     *
     * @param testlinkTestCasesFromTestRun The test cases information from the TAf test run.
     */
    private void reportTestCases(TestlinkTestCasesFromTestRun testlinkTestCasesFromTestRun) {
        for (TestlinkTestCaseMapper testCase : testlinkTestCasesFromTestRun.testCases) {
            if (testCase == null) continue;
            reportTestCaseToTestlink(testCase);
        }
        System.out.println(log.toString());
    }

    /**
     * Converts the JSON with the run results from the TAF test run into proper Java objects.
     *
     * @param json Input JSON
     * @return Returns an object with a set of TAF TestCases equivalents for further use.
     */
    private TestlinkTestCasesFromTestRun getTestlinkReporterObject(String json) {
        TestlinkTestCasesFromTestRun testlinkTestCasesFromTestRun = null;
        try {
            testlinkTestCasesFromTestRun = App.mapper.readValue(json, TestlinkTestCasesFromTestRun.class);
        } catch (IOException e) {
            log("Oups! Could not map json to TestlinkTestCasesFromTestRun object. Error: " + e.toString());
        }
        if (testlinkTestCasesFromTestRun == null) {
            log("Oups! Could not interprete json '" + json + "' to a TestlinkTestCasesFromTestRun object.");
        }
        return testlinkTestCasesFromTestRun;
    }

    /**
     * Attempts to report the results of a test case to a suitable test case in Testlink.
     *
     * @param testCase The incoming test case info to try to send to Testlink.
     */
    private void reportTestCaseToTestlink(TestlinkTestCaseMapper testCase) {
        //noinspection ConstantConditions
        logToDebugLog("Starting registration of test case '" + testCase.testSetName + "/" + testCase.testName + "'.");
        TestCaseMatch testCaseToWorkWith = identifyTestCaseToReportResultsTo(testCase);
        if (testCaseToWorkWith == null) {
            log("Could neither identify nor create test case in Testlink for test case '" + testCase.testSetName + "/" + testCase.testName + "'.");
            return;
        }
        markTestCaseAsAutomatedIfItIsNotAlreadyMarkedAsAutomated(testCaseToWorkWith);
        log(postTestCaseResultToTestlink(testCaseToWorkWith, testCase));
    }

    /**
     * When a suitable test case for test results reporting is identified in Testlink
     * this method makes sure it is marked to be of the execution type AUTOMATED in Testlink.
     *
     * @param testlinkTestCase The test case instance in Testlink
     */
    private void markTestCaseAsAutomatedIfItIsNotAlreadyMarkedAsAutomated(TestCaseMatch testlinkTestCase) {
        if (
                testlinkTestCase != null &&
                testlinkTestCase.testCase != null &&
                testlinkTestCase.testCase.getExecutionType() != null &&
                testlinkTestCase.testCase.getExecutionType().equals(ExecutionType.AUTOMATED)) return;
        try {
            testlinkReporter.api.api.setTestCaseExecutionType(
                    testlinkTestCase.projectId,
                    testlinkTestCase.testCase.getId(),
                    Integer.parseInt(testlinkTestCase.testCase.getFullExternalId()),
                    testlinkTestCase.testCase.getVersion(),
                    ExecutionType.AUTOMATED);
            logToDebugLog("The identified Testlink test case is not marked as automated in Testlink. Changing Testlink ExecutionType to AUTOMATED for test case '" + testlinkTestCase.testCase.getName() + "'.");
        } catch (Exception e) {
            log("Something went wrong when trying to mark test case '" + testlinkTestCase.testCase.getName() + " as ExecutionType AUTOMATED in Testlink. Error message: " + e.getMessage());
        }
    }

    /**
     * Creates a new test case run instance in Testlink for the identified test case, with an appropriate test result status.
     *
     * @param testCaseMatch The identified Testlink test case to report results to.
     * @param testCase      The test case information from a test case from the TAF test run
     * @return Returns an operation status text.
     */
    private String postTestCaseResultToTestlink(TestCaseMatch testCaseMatch, TestlinkTestCaseMapper testCase) {
        ReportTCResultResponse results;
        try {
            Integer.parseInt(testCaseMatch.testCase.getFullExternalId());
        } catch (Exception ignored) {
        }
        try {
            //TestCase testCaseToUse = testlinkReporter.api.api.getTestCase(testCaseMatch.testCase.getId(), null, null);
            results = testlinkReporter.api.api.reportTCResult(
                    testCaseMatch.testCase.getId(),
                    null,
                    testCaseMatch.testPlanId,
                    executionStatus(testCase),
                    testCaseMatch.buildId,
                    xmlSafeString(testCaseMatch.buildName),
                    xmlSafeString(testCase.notes),
                    false,
                    null,
                    testCaseMatch.testCase.getPlatform().getId(),
                    xmlSafeString(testCaseMatch.testCase.getPlatform().getName()),
                    null,
                    false);
        } catch (Exception e) {
            return "Could not report test case '" + testCase.testSetName + "/" + testCase.testName + "' to '" + testCaseMatch.testCase.toString() + "'. Results: " + e.getMessage() + System.lineSeparator();
        }
        return "Reported test case '" + testCase.testSetName + "/" + testCase.testName + "' to '" + testCaseMatch.testCase.toString() + "'. Results: " + results.getMessage() + System.lineSeparator();
    }

    private static String xmlSafeString(String instring){
        if(instring == null) return  null;
        return instring
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;")
                .replace("&", "&amp;")
                .replace("\u001B[34m", "")
                .replace("\u001B[0m", "")
                .replace("\u001B[30m", "")
                .replace("\u001B[31m", "")
                .replace("\u001B[32m", "")
                .replace("\u001B[33m", "")
                .replace("\u001B[34m", "")
                .replace("\u001B[35m", "")
                .replace("\u001B[36m", "")
                .replace("\u001B[37m", "")
                .replace("\u001B[1m", "");
    }

    /**
     * Returns the most suitable Testlink test case identified for results reporting.
     * If no corresponding match can be found a new test case is created in Testlink.
     *
     * @param testCase The test case from the TAF test run to find a test case in Testlink for.
     * @return Returns a Testlink test case.
     */
    private TestCaseMatch identifyTestCaseToReportResultsTo(TestlinkTestCaseMapper testCase) {
        List<TestCase> potentialMatches = identifyPotentialMatches(testCase);
        if (potentialMatches.size() == 0) {
            return createNewTestCase(testCase);
        } else if (potentialMatches.size() == 1) {
            return getTestCaseMatch(testCase, potentialMatches);
        } else {
            TestCaseMatch singleAutomatedTestCase = getSingleTestCaseMarkedAsAutomated(potentialMatches, testCase);
            if (singleAutomatedTestCase != null) return singleAutomatedTestCase;
            TestCaseMatch exactNameMatchingTestCase = identifyTestCaseWithExactNameMatch(potentialMatches, testCase);
            if (exactNameMatchingTestCase != null) return exactNameMatchingTestCase;

            List<TestCase> containsTestSuiteNameMatchesToTestSetName = identifyTestCasesWithTestSuiteNameCorrespondingToTafTestSetName(testCase, potentialMatches);
            if (containsTestSuiteNameMatchesToTestSetName.size() == 1)
                return getSingleTestCaseWithBothTestSetAndTestNameContainsMatch(testCase, containsTestSuiteNameMatchesToTestSetName);

            TestCaseMatch exactTestSetNameMatchAndTestNameMatch = getTestCaseWithExactTestSetNameMatchAndTestNameMatch(containsTestSuiteNameMatchesToTestSetName, testCase);
            if (exactTestSetNameMatchAndTestNameMatch != null) return exactTestSetNameMatchAndTestNameMatch;
        }
        return getFirstTestCaseMatch(testCase, potentialMatches);
    }

    /**
     * If only one of the test cases in the list of potential test case matches
     * is marked with Execution Type AUTOMATED in Testlink this is returned by
     * this method, else null is returned.
     *
     * @param potentialMatches List of potential test cases from Testlink
     * @param testCase         The test case from the TAF test run.
     * @return Returns a Testlink test case if only on of the test cases in the list of potential ones are automated.
     */
    private TestCaseMatch getSingleTestCaseMarkedAsAutomated(List<TestCase> potentialMatches, TestlinkTestCaseMapper testCase) {
        List<TestCase> potentialTestCaseMatchesMarkedAsAutomated = new ArrayList<>();
        for (TestCase potentialMatch : potentialMatches) {
            try {
                if (
                        potentialMatch != null &&
                        potentialMatch.getExecutionType() != null &&
                        potentialMatch.getExecutionType().equals(ExecutionType.AUTOMATED)) {
                    potentialTestCaseMatchesMarkedAsAutomated.add(potentialMatch);
                }
            } catch (Exception e) {
                System.out.println("Assessing test case for execution type but encountered error: " + e.getMessage());
            }
        }
        if (potentialTestCaseMatchesMarkedAsAutomated.size() == 1) {
            TestProject testProject = null;
            for (TestProject testProject1 : cache.testProjects) {
                if (testProject1.getId().equals(potentialTestCaseMatchesMarkedAsAutomated.get(0).getTestProjectId())) {
                    testProject = testProject1;
                    break;
                }
            }
            if (testProject == null) testProject = identifyTestProject(testCase);
            TestPlan testPlan = identifyTestPlan(testProject, testCase);
            Build build = identifyBuild(testPlan);
            return new TestCaseMatch(potentialTestCaseMatchesMarkedAsAutomated.get(0), testPlan.getId(), potentialTestCaseMatchesMarkedAsAutomated.get(0).getTestSuiteId(), potentialTestCaseMatchesMarkedAsAutomated.get(0).getTestProjectId(), build.getId(), build.getName());
        }
        return null;
    }

    /**
     * This method is used as a last resort. If several suitable test cases are found in Testlink. This method returns the first one.
     *
     * @param testCase         The test case from the TAF test run, to find suitable corresponding test case for in Testlink.
     * @param potentialMatches A list of Testlink test cases that could be suitable for matching the 'testCase' parameter.
     * @return Returns the first TestCase from the list of test cases.
     */
    private TestCaseMatch getFirstTestCaseMatch(TestlinkTestCaseMapper testCase, List<TestCase> potentialMatches) {
        StringBuilder logMessage = new StringBuilder("Found multiple potential test cases for result reporting in Testlink. Using the first potential Testlink test case since there are several matching the test case." + System.lineSeparator());
        for (TestCase potentialMatch : potentialMatches) {
            logMessage.append(" * Test case name: '").append(potentialMatch.getName()).append("' (suite id: ").append(potentialMatch.getId()).append(").").append(System.lineSeparator());
        }
        logMessage.append("Using the first test case: ").append(potentialMatches.get(0).toString());
        logToDebugLog(logMessage.toString());
        TestCase theTestCaseInstanceInTestlink = potentialMatches.get(0);
        TestProject testProject = null;
        for (TestProject testProject1 : cache.testProjects) {
            if (testProject1.getId().equals(theTestCaseInstanceInTestlink.getTestProjectId())) {
                testProject = testProject1;
                break;
            }
        }
        TestPlan testPlan = identifyTestPlan(testProject, testCase);
        Build build = identifyBuild(testPlan);
        return new TestCaseMatch(theTestCaseInstanceInTestlink, testPlan.getId(), theTestCaseInstanceInTestlink.getTestSuiteId(), theTestCaseInstanceInTestlink.getTestProjectId(), build.getId(), build.getName());
    }

    /**
     * If several Testlink test cases are identified to have exactly the same name as
     * the sought after test case this method checks if any of these has a immediate
     * parent TestSuite with a name exactly as the sought after test case TestSet name.
     * If such test case is found this is returned, else null.
     *
     * @param containsTestSuiteNameMatchesToTestSetName List of potential Testlink test case candidates
     * @param testCase                                  The test case from the TAF test run to find Testlink match for
     * @return Returns a match if such is found, else null.
     */
    private TestCaseMatch getTestCaseWithExactTestSetNameMatchAndTestNameMatch(List<TestCase> containsTestSuiteNameMatchesToTestSetName, TestlinkTestCaseMapper testCase) {
        for (TestCase potentialExactTestSuiteNameMatchToTestSetName : containsTestSuiteNameMatchesToTestSetName) {
            List<Integer> suiteIds = new ArrayList<>();
            suiteIds.add(potentialExactTestSuiteNameMatchToTestSetName.getTestSuiteId());
            TestSuite[] testSuites = testlinkReporter.api.api.getTestSuiteByID(suiteIds);
            for (TestSuite suite : testSuites) {
                if (suite.getName().equals(testCase.testSetName)) {
                    logToDebugLog("Found exactly one Testlink test case with both a name ('" + potentialExactTestSuiteNameMatchToTestSetName.getName() + "') corresponding to the TAF test case name '" + testCase.testName + "', and a test suite '" + suite.getName() + "' exactly matching the TAF TestSet name '" + testCase.testSetName + "'.");
                    TestProject testProject = null;
                    for (TestProject testProject1 : cache.testProjects) {
                        if (testProject1.getId().equals(potentialExactTestSuiteNameMatchToTestSetName.getTestProjectId())) {
                            testProject = testProject1;
                            break;
                        }
                    }
                    TestPlan testPlan = identifyTestPlan(testProject, testCase);
                    Build build = identifyBuild(testPlan);
                    return new TestCaseMatch(potentialExactTestSuiteNameMatchToTestSetName, testPlan.getId(), potentialExactTestSuiteNameMatchToTestSetName.getTestSuiteId(), potentialExactTestSuiteNameMatchToTestSetName.getTestProjectId(), build.getId(), build.getName());
                }
            }
        }
        return null;
    }

    /**
     * When only one Testlink test case is left in the list of candidate test cases this test case is prepared and converted to a TestCaseMatch object.
     *
     * @param testCase                                  The test case from the TAF test run to look for match for..
     * @param containsTestSuiteNameMatchesToTestSetName The list of candidate Testlink test cases. Now only holding one relevant element left.
     * @return Returns a converted object.
     */
    private TestCaseMatch getSingleTestCaseWithBothTestSetAndTestNameContainsMatch(TestlinkTestCaseMapper testCase, List<TestCase> containsTestSuiteNameMatchesToTestSetName) {
        logToDebugLog("Found exactly one Testlink test case with both a name corresponding to the TAF test case '" + testCase.testName + "', and a test suite '" + containsTestSuiteNameMatchesToTestSetName.get(0).getName() + "' containing the TAF TestSet name '" + testCase.testSetName + "'.");
        TestProject testProject = null;
        for (TestProject testProject1 : cache.testProjects) {
            if (testProject1.getId().equals(containsTestSuiteNameMatchesToTestSetName.get(0).getTestProjectId())) {
                testProject = testProject1;
                break;
            }
        }
        if (testProject == null) identifyTestProject(testCase);
        TestPlan testPlan = identifyTestPlan(testProject, testCase);
        Build build = identifyBuild(testPlan);
        return new TestCaseMatch(containsTestSuiteNameMatchesToTestSetName.get(0), testPlan.getId(), containsTestSuiteNameMatchesToTestSetName.get(0).getTestSuiteId(), containsTestSuiteNameMatchesToTestSetName.get(0).getTestProjectId(), build.getId(), build.getName());
    }

    /**
     * Returns a list of Testlink test cases where the Testlink test cases has a immediate parent
     * test suite with a name containing the TAF test case TestSet name.
     *
     * @param testCase         The TAF test run test case to look for a Testlink match for.
     * @param potentialMatches List of candidate Testlink test cases for matching.
     * @return Returns a subset of incoming potentialMatches.
     */
    private List<TestCase> identifyTestCasesWithTestSuiteNameCorrespondingToTafTestSetName(TestlinkTestCaseMapper testCase, List<TestCase> potentialMatches) {
        List<TestCase> containsTestSuiteNameMatchesToTestSetName = new ArrayList<>();
        for (TestCase potentialMatch : potentialMatches) {
            List<Integer> suiteIds = new ArrayList<>();
            suiteIds.add(potentialMatch.getTestSuiteId());
            TestSuite[] testSuites = testlinkReporter.api.api.getTestSuiteByID(suiteIds);
            for (TestSuite suite : testSuites) {
                if (suite.getName().contains(testCase.testSetName)) {
                    containsTestSuiteNameMatchesToTestSetName.add(potentialMatch);
                    //return new TestCaseMatch(potentialMatch, null, suite.getId(), potentialMatch.getTestProjectId(), null );
                }
            }
        }
        return containsTestSuiteNameMatchesToTestSetName;
    }

    /**
     * Scans the Testlink test cases for test cases with a test case name containing
     * the name of the TAF test case from the test run.
     *
     * @param testCase The test case from the TAF test run to look for suitable Testlink match for.
     * @return Returns a list of Testlink test cases.
     */
    private List<TestCase> identifyPotentialMatches(TestlinkTestCaseMapper testCase) {
        List<TestCase> potentialMatches = new ArrayList<>();
        for (TestCase registeredTestCase : cache.testCases) {
            if (registeredTestCase.getName().contains(testCase.testName)) {
                potentialMatches.add(registeredTestCase);
            }
        }
        return potentialMatches;
    }

    /**
     * Checks if a Testlink test case with exactly the same test name as the test case from the TAF test run has exist.
     * If so, this is returned. Else null is returned.
     *
     * @param potentialMatches List of candidate Testlink test cases
     * @param testCase         The TAF test case to look for match for
     * @return Returns the unique match if encountered, else null.
     */
    private TestCaseMatch identifyTestCaseWithExactNameMatch(List<TestCase> potentialMatches, TestlinkTestCaseMapper testCase) {
        List<TestCase> exactNameMatches = new ArrayList<>();
        for (TestCase potentialMatch : potentialMatches) {
            if (potentialMatch.getName().equals(testCase.testName)) {
                exactNameMatches.add(potentialMatch);
            }
        }
        if (exactNameMatches.size() == 1) {
            logToDebugLog("Found exactly one Testlink test case with a name exactly as the TAF test case name. Using test case '" + exactNameMatches.get(0).getName() + "' for reporting.");
            TestProject testProject = null;
            for (TestProject testProject1 : cache.testProjects) {
                if (testProject1.getId().equals(exactNameMatches.get(0).getTestProjectId())) {
                    testProject = testProject1;
                    break;
                }
            }
            TestPlan testPlan = identifyTestPlan(testProject, testCase);
            Build build = identifyBuild(testPlan);
            return new TestCaseMatch(exactNameMatches.get(0), testPlan.getId(), exactNameMatches.get(0).getTestSuiteId(), exactNameMatches.get(0).getTestProjectId(), build.getId(), build.getName());
        }
        return null;
    }

    /**
     * Checks if only one Testlink test case is found that has a test name containing the sought after test name.
     * If so, this is returned - else null is returned.
     *
     * @param testCase         The test case from the TAF test run to find Testlink match for.
     * @param potentialMatches A list of candidate Testlink test cases.
     * @return Returns the unique test case, if exist. Else null.
     */
    private TestCaseMatch getTestCaseMatch(TestlinkTestCaseMapper testCase, List<TestCase> potentialMatches) {
        logToDebugLog("Found exactly one Testlink test case with a name containing the TAF test case name. Using test case '" + potentialMatches.get(0).getName() + "' for reporting.");
        TestCase theTestCaseInstanceInTestlink = potentialMatches.get(0);
        TestProject testProject = null;
        for (TestProject testProject1 : cache.testProjects) {
            if (testProject1.getId().equals(theTestCaseInstanceInTestlink.getTestProjectId())) {
                testProject = testProject1;
                break;
            }
        }
        if (testProject == null)
            testProject = identifyTestProject(testCase);

        TestPlan testPlan = identifyTestPlan(testProject, testCase);
        TestSuite testSuite = identifyTestSuite(testProject, testCase);
        Build build = identifyBuild(testPlan);
        //Platform platform = identifyPlatform(testProject, testPlan);

        TestCaseMatch tcm = new TestCaseMatch(
                theTestCaseInstanceInTestlink,
                testPlan.getId(),
                testSuite.getId(),
                testProject.getId(),
                build.getId(),
                build.getName());
        return tcm;
    }

    /**
     * Creates a new test case in Testlink, for the TAF test case provided.
     *
     * @param testCase The TAF test case to create a Testlink test case for.
     * @return Returns the created Testlink test case.
     */
    private TestCaseMatch createNewTestCase(TestlinkTestCaseMapper testCase) {
        logToDebugLog("Zero potential matches for test case found in Testlink.");
        TestCaseMatch testCaseMatch = createTestCaseInTestlink(testCase);
        cache.testCases.add(testCaseMatch.testCase);
        return testCaseMatch;
    }

    /*
    private Platform identifyPlatform(TestProject testProject, TestPlan testPlan) {
        Platform[] platforms = testlinkReporter.api.api.getTestPlanPlatforms(testPlan.getId());
        if(platforms == null || platforms.length == 0){
            System.out.println("No platform description found for this test plan. Need a platform to report test results to. You should create one and assign all relevant test cases to it.");
        }
        if(platforms.length > 0) return platforms[0];
        return null;
    }
    */

    /**
     * Converts the TAF test case result status to a corresponding Testlink test result status.
     *
     * @param testCase The TAF test case.
     * @return Returns corresponding Testlink execution status.
     */
    private ExecutionStatus executionStatus(TestlinkTestCaseMapper testCase) {
        switch (testCase.executionStatus) {
            case "PASSED":
                return ExecutionStatus.PASSED;
            case "UNEVALUATED":
                return ExecutionStatus.BLOCKED;
            default:
                return ExecutionStatus.FAILED;
        }
    }

    /**
     * Creates a test case in Testlink, in the most suitable place.
     *
     * @param testCase The test case from the TAF test run to create a Testlink test case for.
     * @return Returns the Testlink test case created.
     */
    private TestCaseMatch createTestCaseInTestlink(TestlinkTestCaseMapper testCase) {

        TestProject testProject = identifyTestProject(testCase);

        TestSuite testSuite = identifyTestSuite(testProject, testCase);

        TestPlan testPlan = identifyTestPlan(testProject, testCase);

        Platform platform = identifyPlatform(testProject, testPlan);

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
                false, //Full external id
                ActionOnDuplicate.CREATE_NEW_VERSION    //Action on duplicate
        );

        log("Created '" + testlinkTestCase.toString() + "'. Adding it to test plan '" + testPlan.getName() + "'.");

        //platform = testlinkTestCase.getPlatform();

        Integer version = testlinkTestCase.getVersion();

        testlinkReporter.api.api.addTestCaseToTestPlan(
                testProject.getId(),
                testPlan.getId(),
                testlinkTestCase.getId(),
                version,
                platform.getId(),
                null,
                null);

        return new TestCaseMatch(
                testlinkTestCase, //Feature id
                testPlan.getId(),
                testSuite.getId(),
                testProject.getId(),
                build.getId(),
                build.getName());
    }

    private Platform identifyPlatform(TestProject testProject, TestPlan testPlan) {
        Platform[] testPlanPlatforms = testlinkReporter.api.api.getTestPlanPlatforms(testPlan.getId());
        if(testPlanPlatforms == null || testPlanPlatforms.length == 0){
            log("Creating platform '" + Settings.defaultPlatformNameForNewTestCases + "' and adding it to test plan.");
            Map<String, Object> response = testlinkReporter.api.api.addPlatformToTestPlan(testProject.getId(), testPlan.getId(), Settings.defaultPlatformNameForNewTestCases);
        }
        return testlinkReporter.api.api.getTestPlanPlatforms(testPlan.getId())[0];
    }

    /**
     * Identifies a suitable Testlink build to report the test case results from thee TAF test run to.
     * If no suitable build can be created a new build is created.
     *
     * @param testPlan The Testlink test plan to look for build in
     * @return Returns a suitable Testlink build element
     */
    private Build identifyBuild(TestPlan testPlan) {
        Build[] builds = testlinkReporter.api.api.getBuildsForTestPlan(testPlan.getId());
        if (builds == null || builds.length == 0) {
            System.out.println("No build found for this test plan. Need a build to report test results to. Creating a build called '" + Settings.defaultBuildNameForNewTestCases + "' for Testlink test plan '" + testPlan.getName() + "' (test plan id=" + testPlan.getId() + ").");
            Build build = testlinkReporter.api.api.createBuild(
                    testPlan.getId(),
                    Settings.defaultBuildNameForNewTestCases,
                    "Automatically created from TAF Testlink adapter server.");
            return build;
        }
        if (builds.length == 1) return builds[0];
        return testlinkReporter.api.api.getLatestBuildForTestPlan(testPlan.getId());
    }

    /**
     * Identifies the most suitable Testlink test plan to report the test case results from the TAF test run to.
     * If no suitable test plan can be identified, one is created.
     *
     * @param testProject The Testlink test project where to find a suitable test plan.
     * @param testCase    The test case from the TAF test run to find suitable Testlink test plan for.
     * @return Returns a suitable Teslink test plan.
     */
    private TestPlan identifyTestPlan(TestProject testProject, TestlinkTestCaseMapper testCase) {
        if (testProject == null) {
            System.out.println("Null project when trying to identify test plan.");
            return null;
        }
        TestPlan[] testPlansForThisProject = null;
        try {
            testPlansForThisProject = testlinkReporter.api.api.getProjectTestPlans(testProject.getId());
        } catch (Exception e) {
            System.out.println("Could not get any test plans for project '" + testProject.toString());
        }

        if (testPlansForThisProject == null || testPlansForThisProject.length == 0) {
            System.out.println("Could not find any test plans for project '" + testProject.toString());
            TestPlan testPlan = testlinkReporter.api.api.createTestPlan(
                    Settings.defaultTestPlanName,
                    testProject.getName(),
                    "Automatically created from TAF Testlink Adapter",
                    true,
                    true);
            cache.testPlans.add(testPlan);
            return testPlan;
        }

        //If this test project only contains one test plan this should be returned
        if (testPlansForThisProject.length == 1) return testPlansForThisProject[0];

        //If a TestPlan contains a TestCase with correct name, return it
        for (TestPlan testPlan : testPlansForThisProject) {
            TestCase[] testCasesForTestPlan = testlinkReporter.api.api.getTestCasesForTestPlan(
                    testPlan.getId(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);
            for (TestCase foundTestCase : testCasesForTestPlan) {
                if (foundTestCase.getName().equals(testCase.testName)) {
                    return testPlan;
                }
            }
        }

        //Else create a new test plan
        TestPlan newTestPlan = testlinkReporter.api.api.createTestPlan(
                Settings.defaultTestPlanName,
                testProject.getName(),
                null,
                true,
                true);
        cache.testPlans.add(newTestPlan);
        return newTestPlan;
    }

    /**
     * Identifies a suitable Testlink test suite to report TAF test case results to.
     * If no suitable test suite can be found one is created and returned.
     *
     * @param testProject The Testlink test project where to find a suitable test suite.
     * @param testCase    The TAF test run test case to find suitable match for
     * @return Returns the best possible Testlink test suite
     */
    private TestSuite identifyTestSuite(TestProject testProject, TestlinkTestCaseMapper testCase) {
        //If a Testlink test suite exist with a name corresponding to the testCase TestSet, and with a Testlink TestCase with the same name as the TAF test case name, return this.
        for (TestSuite testSuite : cache.testSuites) {
            if (testSuite.getName().equals(testCase.testSetName)) {
                for (TestCase test : cache.testCases) {
                    if (test.getTestSuiteId() == null) continue;
                    if (test.getTestSuiteId().equals(testSuite.getId()) && testCase.testName.equals(test.getName())) {
                        for (TestProject project : cache.testProjects) {
                            if (project.getId().equals(testSuite.getTestProjectId())) {
                                return testSuite;
                            }
                        }
                    }
                }
            }
        }

        updateDefaultTestSuiteStatus(testProject.getId());


        //Create default test test suite for orphan test cases if needed
        if (!defaultTestSuiteFound) {
            TestSuite newlyCreatedDefaultTestSuite = testlinkReporter.api.api.createTestSuite(
                    testProject.getId(),
                    Settings.defaultTestSuiteNameForNewTestCases,
                    null,
                    null,
                    null,
                    null,
                    null);
            cache.testSuites.add(newlyCreatedDefaultTestSuite);
            defaultTestSuiteId = newlyCreatedDefaultTestSuite.getId();
        }

        //If a test suite with the same name as the test set exist, return it
        TestSuite[] automationTestSuites = testlinkReporter.api.api.getTestSuitesForTestSuite(defaultTestSuiteId);
        for (TestSuite testSuite : automationTestSuites) {
            if (testSuite.getName().equals(testCase.testSetName)) {
                return testSuite;
            }
        }

        //Else create it and return it
        TestSuite newlyCreatedTestSuite = testlinkReporter.api.api.createTestSuite(
                testProject.getId(),
                testCase.testSetName,
                null,
                defaultTestSuiteId,
                null,
                null,
                null);
        cache.testSuites.add(newlyCreatedTestSuite);
        return newlyCreatedTestSuite;
    }

    /**
     * Identifies a suitable Testlink test project to report test results to. If none is found one is created and returned.
     *
     * @param testCase The test case from the TAF test run to identify suitable Testlink test project for
     * @return Returns a suitable Testlink test project.
     */
    private TestProject identifyTestProject(TestlinkTestCaseMapper testCase) {
        Integer projectId = null;
        if (cache.testProjects.size() == 1) {
            //If only one test project exist this should be used
            return cache.testProjects.get(0);
        } else {
            //Try finding a Testlink project with a TestSuite with the same name as the TAF testCase TestSet.
            for (TestSuite testSuite : cache.testSuites) {
                if (testSuite.getName().equals(testCase.testSetName)) {
                    for (TestCase test : cache.testCases) {
                        if (test.getTestSuiteId().equals(testSuite.getId()) && testCase.testName.equals(test.getName())) {
                            for (TestProject project : cache.testProjects) {
                                if (project.getId().equals(testSuite.getTestProjectId())) {
                                    return project;
                                }
                            }
                        }
                    }
                }
            }
            //Return default TestProject in Testlink if exist
            for (TestProject testProject : cache.testProjects) {
                if (testProject.getName().equals(Settings.defaultTestProjectNameForNewTestCases)) {
                    return testProject;
                }
            }
            //Othervice create a new default TestProject in Testlink
            TestProject newlyCreatedTestProject = testlinkReporter.api.api.createTestProject(
                    Settings.defaultTestProjectNameForNewTestCases,
                    "ZX",
                    "Automatically created for test automation test cases that cannot be identified already",
                    true,
                    true,
                    true,
                    true,
                    true,
                    true);
            cache.testProjects.add(newlyCreatedTestProject);
            return newlyCreatedTestProject;
        }

    }

    /**
     * Used to check if the default Testlink test suite for test case creation exist.
     *
     * @param projectId The Testlink project id where to check.
     */
    private void updateDefaultTestSuiteStatus(Integer projectId) {
        if (defaultTestSuiteFound == null) {
            TestSuite[] candidateTestSuites = testlinkReporter.api.api.getFirstLevelTestSuitesForTestProject(projectId);
            defaultTestSuiteFound = false;
            for (TestSuite candidateTestSuite : candidateTestSuites) {
                if (candidateTestSuite.getName().equals(Settings.defaultTestSuiteNameForNewTestCases)) {
                    defaultTestSuiteFound = true;
                    defaultTestSuiteId = candidateTestSuite.getId();
                    break;
                }
            }
        }
    }

    /**
     * Exception used to trigger more return information to TAF.
     */
    public class TestResultsProblemException extends Exception {
        public TestResultsProblemException(String message) {
            super(message);
        }
    }

    /**
     * Holder for test case instance. Also used to fill in missing information from Testlink.
     */
    class TestCaseMatch {
        TestCase testCase;
        Integer testPlanId;
        Integer testSuiteId;
        Integer projectId;
        Integer buildId;
        String buildName;

        TestCaseMatch(br.eti.kinoshita.testlinkjavaapi.model.TestCase testCase, Integer testPlanId, Integer testSuiteId, Integer projectId, Integer buildId, String buildName) {
            this.testPlanId = testPlanId;
            this.testSuiteId = testSuiteId;
            this.projectId = projectId;
            this.testCase = testCase;
            this.buildId = buildId;
            this.buildName = buildName;
        }

    }
}
