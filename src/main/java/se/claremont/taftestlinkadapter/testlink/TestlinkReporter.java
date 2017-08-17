package se.claremont.taftestlinkadapter.testlink;

import br.eti.kinoshita.testlinkjavaapi.constants.ExecutionStatus;
import br.eti.kinoshita.testlinkjavaapi.constants.TestCaseDetails;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import br.eti.kinoshita.testlinkjavaapi.model.TestSuite;
import br.eti.kinoshita.testlinkjavaapi.util.TestLinkAPIException;
import se.claremont.taftestlinkadapter.server.Settings;

import java.util.ArrayList;

/**
 * Interacts with the Testlink server. Possible partial over-lap with TestRunRegistration class.
 *
 * Created by jordam on 2016-10-29.
 */
@SuppressWarnings("SameParameterValue")
public class TestlinkReporter {
    boolean success = true;
    boolean hasReportedConfigText = false;
    public TestlinkClient api = null;
    public String logMessage = System.lineSeparator();

    /**
     * Setup connection
     */
    @SuppressWarnings("ConstantConditions")
    public TestlinkReporter(){
        try {
            log("TestlinkReporter is using address '" + Settings.testlinkServerAddress + "'.");
            api = new TestlinkClient();
            if(api == null || api.api == null) {
                success = false;
                log("Testlink reporter became null. This should not happen.");
            }
            //about = api.about().toString();
        } catch (Exception e) {
            log("Could not connect to Testlink at url '" + Settings.testlinkServerAddress + "' with the supplied devKey '" + Settings.testlinkDevKey + "'. " + e.getMessage());
            success = false;
        }
        if(success){
            log("Created TestlinkReporter successfully.");
        } else {
            log("Could not create Testlink reporter.");
        }
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


    private void log(String message){
        System.out.println(message);
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