package se.claremont.taftestlinkadapter.testlink;

import br.eti.kinoshita.testlinkjavaapi.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Since communication with Testlink get slow when Testlink holds a lot of data this cache of Testlink elements is implemented.
 */
public class TestlinkElementCache {
    List<TestProject> testProjects = new ArrayList<>();
    List<TestPlan> testPlans = new ArrayList<>();
    List<TestSuite> testSuites = new ArrayList<>();
    List<TestCase> testCases = new ArrayList<>();
    private TestlinkReporter testlinkReporter;
    List<Platform> platforms = new ArrayList<>();

    TestlinkElementCache(){

    }

    /**
     * Refreshes the cache with current state of the Testlink DB content
     * .
     * @param testlinkReporter A reporter object for communication with the Testlink API
     * @return Returns cache status information as text
     */
    String updateContent(TestlinkReporter testlinkReporter){
        this.testlinkReporter = testlinkReporter;
        String returnString = "Updating cache.";
        clearCache();
        updateTestProjects();
        updateTestPlans();
        updateTestSuites();
        updateTestCases();
        return returnString + System.lineSeparator() + "Cache status: " + System.lineSeparator() + " * " + testProjects.size() + " test projects" + System.lineSeparator() + " * " + testSuites.size() + " test suites" + System.lineSeparator() + " * " + testPlans.size() + " test plans" + System.lineSeparator() + " * " + testCases.size() + " test cases";
    }

    /**
     * Updates the test cases in this cache
     */
    private void updateTestCases() {
        for(TestPlan plan : testPlans){
            TestCase[] cases = testlinkReporter.api.api.getTestCasesForTestPlan(plan.getId(), null, null, null, null, null, null, null, null, null, null);
            Collections.addAll(testCases, cases);
        }
        collectTestCasesNotInTestPlans();
    }

    /**
     * Not all test cases in Testlink is part of a test plan. This methods identifies test cases not already identified from test plan scanning.
     */
    private void collectTestCasesNotInTestPlans() {
        for(TestSuite suite : testSuites){
            TestCase[] cases = testlinkReporter.api.api.getTestCasesForTestSuite(suite.getId(), true, null);
            for(TestCase testCase : cases){
                boolean alreadyRegistered = false;
                for(TestCase alreadyRegisteredTestCase : testCases){
                    if(alreadyRegisteredTestCase.getId().equals(testCase.getId())) {
                        alreadyRegistered = true;
                        break;
                    }
                }
                if(!alreadyRegistered){
                    testCases.add(testCase);
                }
            }
        }
    }

    /**
     * Updates test suites in cache
     */
    private void updateTestSuites() {
        for(TestPlan plan : testPlans){
            TestSuite[] suites = testlinkReporter.api.api.getTestSuitesForTestPlan(plan.getId());
            Collections.addAll(testSuites, suites);
        }
    }

    /**
     * Updates test projects in cache
     */
    private void updateTestProjects() {
        TestProject[] projects = testlinkReporter.api.api.getProjects();
        Collections.addAll(testProjects, projects);
    }

    /**
     * Updates test plans in cache
     */
    private void updateTestPlans(){
        for(TestProject project : testProjects){
            TestPlan[] plans = testlinkReporter.api.api.getProjectTestPlans(project.getId());
            Collections.addAll(testPlans, plans);
        }
    }

    /**
     * Clears the cache, making it empty for refreshing.
     */
    private void clearCache(){
        testProjects.clear();
        testPlans.clear();
        testSuites.clear();
        testCases.clear();
    }


}
