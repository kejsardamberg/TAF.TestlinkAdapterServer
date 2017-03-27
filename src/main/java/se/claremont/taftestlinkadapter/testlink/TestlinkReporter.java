package se.claremont.taftestlinkadapter.testlink;

import br.eti.kinoshita.testlinkjavaapi.constants.*;
import br.eti.kinoshita.testlinkjavaapi.model.*;
import br.eti.kinoshita.testlinkjavaapi.util.TestLinkAPIException;
import se.claremont.taftestlinkadapter.cache.TestlinkCache;
import se.claremont.taftestlinkadapter.server.Settings;

import java.util.ArrayList;
import java.util.List;

/**
 * Interacts with the Testlink server. Possible partial over-lap with TestRunRegistration class.
 *
 * Created by jordam on 2016-10-29.
 */
@SuppressWarnings("SameParameterValue")
public class TestlinkReporter {
    ArrayList<String> projects = new ArrayList<>();
    boolean success = true;
    boolean hasReportedConfigText = false;
    public TestlinkClient api = null;
    public String logMessage = System.lineSeparator();

    /**
     * Setup connection
     */
    @SuppressWarnings("ConstantConditions")
    public TestlinkReporter(){
        String about = null;
        try {
            api = new TestlinkClient(Settings.testlinkServerAddress, Settings.testlinkDevKey);
            if(api == null || api.api == null) {
                success = false;
            }
            //about = api.about().toString();
        } catch (Exception e) {
            log("Could not connect to Testlink at url '" + Settings.testlinkServerAddress + "' with the supplied devKey '" + Settings.testlinkDevKey + "'. " + e.getMessage());
            success = false;
        }
        this.projects = testlinkProjects();
    }


    public String testlinkProjectsAndPlansListing(){
        StringBuilder sb = new StringBuilder(System.lineSeparator());
        ArrayList<String> projects = testlinkProjects();
        for(String project : projects){
            sb.append("Project: '").append(project).append("'").append(System.lineSeparator());
            ArrayList<String> plans = testlinkTestPlans(project);
            if(plans.size() == 0) continue;
            sb.append("   Plans in '").append(project).append("':").append(System.lineSeparator());
            for (String plan : plans){
                sb.append("   > '").append(plan).append("'").append(System.lineSeparator());
                ArrayList<String> suites = testlinkTestSuites(project, plan);
                if(suites.size() == 0) continue;
                sb.append("      Test suites in plan '").append(plan).append("':").append(System.lineSeparator());
                for (String suite : suites){
                    sb.append("        > '").append(suite).append("'").append(System.lineSeparator());
                }
            }
        }
        return sb.toString();
    }

    public void resetSuccessToTrue(){
        this.success = true;
    }

    /**
     * Reports the result of a test case to Testlink.
     *
     */
    public boolean evaluateTestCase(String testProjectName, String testPlanName, String testSuiteName, String buildName, String testName, String notes, String results, boolean createTestCaseInTestlinkIfItDoesNotExistThere){
        TestlinkTestResult testResult = new TestlinkTestResult(testProjectName, testPlanName, testSuiteName, buildName, testName, notes, results);
        if(apiIsNotReady()) {
            reportApiProblem(testName);
            //return;
        }
        if(createTestCaseInTestlinkIfItDoesNotExistThere)
            createTestCaseInTestlinkIfNotExistThere(testResult);
        reportToLogIfTestCaseDoesNotExistInTestlink(testResult);
        if(testCaseExistInTestlink(testResult))
            tryReportResults(testResult);
        boolean returnStatus = success;
        resetSuccessToTrue();
        return returnStatus;
    }

    private boolean testCaseExistInTestlink(TestlinkTestResult testlinkTestResult){
        try {
            api.api.getTestCaseIDByName(testlinkTestResult.testName, testlinkTestResult.testSuiteName, testlinkTestResult.testProjectName, "autotest");
            return true;
        }catch (Exception e){
            return false;
        }
    }

    private void reportToLogIfTestCaseDoesNotExistInTestlink(TestlinkTestResult testlinkTestResult){
        if(testCaseExistInTestlink(testlinkTestResult)){
            log("Checking that test case '" + testlinkTestResult.testName + "' exists in Testlink. It does. Proceeding.");
        }else{
            log("Could not report results for test automation test case '" + testlinkTestResult.testName + "'. Tried to report results to Testlink for the Testlink test case '" + testlinkTestResult.testName + "' in test suite '" + testlinkTestResult.testSuiteName + "' in test plan '" + testlinkTestResult.testPlanName + "' in test project '" + testlinkTestResult.testProjectName + "'.");
        }
    }

    public ArrayList<String> testlinkProjects(){
        ArrayList<String> projects = new ArrayList<>();
        TestProject[] queryResult;
        try {
            queryResult = api.api.getProjects();
            for(TestProject testProject : queryResult){
                projects.add(testProject.getName());
            }
        } catch (TestLinkAPIException e) {
            log(e.getMessage());
        }
        return projects;
    }

    public ArrayList<String> testlinkTestPlans(String projectName){
        ArrayList<String> testPlans = new ArrayList<>();
        String queryResult = "";
        try {
            Integer projectId = getProjectID(projectName);
            if(projectId == null) return testPlans;
            queryResult = api.api.getProjectTestPlans(projectId).toString();
        } catch (TestLinkAPIException e) {
            log(e.getMessage());
        }
        for(String entry : queryResult.split(",")){
            if(entry.contains("=")){
                if(entry.split("=")[0].trim().toLowerCase().equals("name")){
                    testPlans.add(entry.split("=")[1].trim());
                }
            }
        }
        return testPlans;
    }


    public Integer getProjectID(String projectName) {
        TestProject[] projects = api.api.getProjects();
        for(TestProject testProject : projects){
            if(testProject.getName().equals(projectName)){
                return testProject.getId();
            }
        }
        return null;
    }

    public Integer getTestPlanId(String projectName, String testPlanName){
        TestPlan[] testPlans = api.api.getProjectTestPlans(getProjectID(projectName));
        for(TestPlan testPlan : testPlans){
            if(testPlan.getName().equals(testPlanName)){
                return testPlan.getId();
            }
        }
        return null;
    }

    public ArrayList<String> testlinkTestSuites(String projectName, String testPlanName){
        ArrayList<String> suites = new ArrayList<>();
        String queryResult = null;
        try {
            queryResult = api.api.getTestSuitesForTestPlan(getTestPlanId(projectName, testPlanName)).toString();
        } catch (TestLinkAPIException e) {
            log(e.getMessage());
        }
        if(queryResult == null) return suites;
        for(String entry : queryResult.split(",")){
            if(entry.contains("=")){
                if(entry.split("=")[0].trim().toLowerCase().equals("name")){
                    suites.add(entry.split("=")[1].trim());
                }
            }
        }
        return suites;
    }

    /**
     * Returns brief configuration information.
     *
     * @return Returns a brief text on how to set up Testlink to enable automation integration.
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    public String configInformation(){
        StringBuilder sb = new StringBuilder();
        sb.append("Check this out: 'ww.softwaretestinghelp.com/testlink-tutorial-3/'").append(System.lineSeparator());
        sb.append("Basically:").append(System.lineSeparator());
        sb.append("1). Install the Testlink test management tool somewhere where you have administrative rights enough to alter the config files of the installation.").append(System.lineSeparator());
        sb.append("2). Enable Testlink API and automation integration on testlink server (edit config files and restart).").append(System.lineSeparator());
        sb.append("3). Generate API reference access key in Testlink GUI, as admin.").append(System.lineSeparator());
        sb.append("4). Change Testlink test cases to the 'Automated' status, and make sure your automated implementation of the test case reports the results to this test case using the reportResults() method.").append(System.lineSeparator());
        return sb.toString();
    }

    /**
     * Attempts to create test project in Testlink if it doesn't exist there already.
     *
     * @param testProjectName THe name of the test project in Testlink
     * @return Returns the ID of the test project (found or created)
     */
    public Integer createTestProjectInTestlinkIfNotExistThere(String testProjectName){
        Integer id = getProjectID(testProjectName);
        if(id != null)return id;
        try{
            log("Test project '" + testProjectName + "' is missing in Testlink. Trying to create it.");
            TestProject testProject = api.api.createTestProject(testProjectName, testProjectName.substring(0,1),"", false, false, true, false, true, true);
            TestlinkCache.addTestProjectToCache(testProject);
            return testProject.getId();
        }catch (Exception e){
            log("Could not create missing test project '" + testProjectName + "' in Testlink. " + e.getMessage());
            success = false;
        }
        return getProjectID(testProjectName);
    }

    public void resetLogMessage(){
        logMessage = System.lineSeparator();
    }

    public Integer createTestPlanInTestlinkIfNotExistThere(String testProjectName, String testPlanName){
        createTestProjectInTestlinkIfNotExistThere(testProjectName);
        log("Verifying that test plan '" + testPlanName + "' need to be created in Testlink test project '" + testProjectName + "'.");
        Integer id = getTestPlanId(testProjectName, testPlanName);
        if(id != null) return id;
        log("Test plan '" + testPlanName + "' does not exist in test project '" + testProjectName + "' and should be created.");
        TestPlan testPlan = api.api.createTestPlan(testPlanName, testProjectName, "Automatically created", true, true);
        TestlinkCache.addTestPlanToCache(testPlan, getProjectID(testProjectName));
        return getTestPlanId(testProjectName, testPlanName);
    }

    public Integer createTestSuiteInTestlinkIfNotExistThere(String testProjectName, String testSuiteName, String testPlanName){
        createTestPlanInTestlinkIfNotExistThere(testProjectName, testPlanName);
        Integer id = getTestSuiteId(testProjectName, testSuiteName);
        if(id != null) return id;
        TestSuite testSuite;
        try {
            log("Assessing if Testlink test suite '" + testSuiteName + "' need to be created in test plan '" + testPlanName + "' in test project '" + testProjectName + "'.");
            testSuite = api.api.createTestSuite(getProjectID(testProjectName), testSuiteName, "Automatically created from test automation TAF.", null, null, false, ActionOnDuplicate.CREATE_NEW_VERSION);
            TestlinkCache.addTestSuiteToCache(testSuite, getTestPlanId(testProjectName, testPlanName));
            id = testSuite.getId();
            log("Created test suite '" + testSuiteName + "' in test plan '" + testPlanName + "' in Testlink test project '" + testProjectName + "'.");
        } catch (TestLinkAPIException e) {
            log("Could not create test suite test suite '" + testSuiteName + "' in test plan '" + testPlanName + "' in Testlink test project '" + testProjectName + "'. " + e.getMessage());
            success = false;
        }
        return id;
    }

    public Integer getTestSuiteId(String testProjectName, String testSuiteName){
        TestPlan[] testPlans = api.api.getProjectTestPlans(getProjectID(testProjectName));
        for(TestPlan testPlan : testPlans){
            TestSuite[] testSuites = api.api.getTestSuitesForTestPlan(testPlan.getId());
            for(TestSuite testSuite : testSuites){
                if(testSuite.getName().equals(testSuiteName)) return testSuite.getId();
            }
        }
        return null;
    }

    public boolean testPlanExist(String testProject){
        return (getProjectID(testProject) != null);
    }

    public boolean testSuiteExist(String testProject, String testSuite){
        return (getTestSuiteId(testProject, testSuite) != null);
    }


    private void createTestCaseInTestlinkIfNotExistThere(TestlinkTestResult testlinkTestResult){
        createTestSuiteInTestlinkIfNotExistThere(testlinkTestResult.testProjectName, testlinkTestResult.testSuiteName, testlinkTestResult.testPlanName);
        try {
            log(api.api.getTestCaseIDByName(testlinkTestResult.testName, testlinkTestResult.testSuiteName, testlinkTestResult.testProjectName, "autotest").toString());
            log("Verified that test case '" + testlinkTestResult.testName + "' exist in test suite '" + testlinkTestResult.testSuiteName + "' in test plan '" + testlinkTestResult.testPlanName + "' in test project '" + testlinkTestResult.testProjectName + "', and it does.");
        }catch (Exception e){
            try {
                List<TestCaseStep> steps = new ArrayList<>();
                log(api.api.createTestCase(testlinkTestResult.testName,
                        getTestSuiteId(testlinkTestResult.testProjectName, testlinkTestResult.testSuiteName),
                        getProjectID(testlinkTestResult.testProjectName),
                        Settings.testlinkUserName,
                        "Test case automatically created by test automation execution.",
                        steps,
                        "ExpectedToPass",
                        TestCaseStatus.FINAL, TestImportance.MEDIUM, ExecutionType.AUTOMATED, null, null, false, ActionOnDuplicate.CREATE_NEW_VERSION).toString());
                log("Creating test case '" + testlinkTestResult.testName + "' in Testlink (in test suite '" + testlinkTestResult.testSuiteName + "' and project '" + testlinkTestResult.testProjectName + "') since it didn't exist.");
            } catch (TestLinkAPIException e1) {
                log("Tried to create test case in Testlink since the test case didn't exist. This did not work out as expected." + e1.getMessage());
                success = false;
            }
        }
    }

    private void tryReportResults(TestlinkTestResult testlinkTestResult){
        try{
            doReportResult(testlinkTestResult.testProjectName, testlinkTestResult.testPlanName, testlinkTestResult.testName, testlinkTestResult.buildName, testlinkTestResult.notes, testlinkTestResult.resultStatus);
            log("Reported results to Testlink successfully.");
        }catch (Exception e){
            log("Error from TestlinkReporter when trying to report results: " + e.getMessage());
            success = false;
            if(!hasReportedConfigText) {
                hasReportedConfigText = true;
                log(configInformation() + System.lineSeparator() + testlinkProjectsAndPlansListing());
            }
        }
    }

    private void doReportResult(String testProject,String testPlan,String testCaseName, String build, String notes, String result) throws TestLinkAPIException {
        TestSuite[] testSuites = api.api.getTestSuitesForTestPlan(getTestPlanId(testProject, testPlan));
        for(TestSuite testSuite : testSuites){
            TestCase[] testCases = api.api.getTestCasesForTestSuite(testSuite.getId(), true, TestCaseDetails.FULL);
            for(TestCase testCase : testCases){
                if(testCase.getName().equals(testCaseName)){
                    api.api.reportTCResult(testCase.getId(), testCase.getId(), getTestPlanId(testProject, testPlan), getExecutionStatus(result), null, null, notes, null, null, null, null, null, true);
                    log("Reported results for test case '" + testCaseName + "' as '" + result + "' to Testlink.");
                }
            }
        }
    }

    private ExecutionStatus getExecutionStatus(String status){
        switch (status.toLowerCase()){
            case "pass":
            case "passed":
                return ExecutionStatus.PASSED;
            case "failed":
            case "fail":
                return ExecutionStatus.FAILED;
            case "block":
            case "blocked":
                return ExecutionStatus.BLOCKED;
            case "no_run":
            case "norun":
            case "none":
            case "no-run":
            case "no run":
                return ExecutionStatus.NOT_RUN;
            default:
                return ExecutionStatus.NOT_RUN;
        }
    }

    private boolean apiIsNotReady(){
        return this.api == null;
    }

    private void reportApiProblem(String testName){
        log("No connection to Testlink API. Could not report test results for test case '" + testName + "' to Testlink.");
    }

    private void log(String message){
        this.logMessage += message + System.lineSeparator();
    }

    class TestlinkTestResult{
        String testProjectName;
        String testPlanName;
        String testSuiteName;
        String buildName;
        String testName;
        String notes;
        String resultStatus;

        public TestlinkTestResult(String testProjectName, String testPlanName, String testSuiteName, String buildName, String testName, String notes, String resultStatus){
            this.testProjectName = testProjectName;
            this.testPlanName = testPlanName;
            this.testSuiteName = testSuiteName;
            this.buildName = buildName;
            this.testName = testName;
            this.notes = notes;
            this.resultStatus = resultStatus;
        }
    }
}