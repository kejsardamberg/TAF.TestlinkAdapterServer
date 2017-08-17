package se.claremont.taftestlinkadapter.testlink;

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
    public TestlinkClient api = null;
    public String logMessage = System.lineSeparator();

    /**
     * Setup connection
     */
    @SuppressWarnings("ConstantConditions")
    public TestlinkReporter(){
        boolean success = true;
        try {
            System.out.print("TestlinkReporter is using address '" + Settings.testlinkServerAddress + "'. Connecting.");
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

    private void log(String message){
        System.out.println(message);
        this.logMessage += message + System.lineSeparator();
    }
}