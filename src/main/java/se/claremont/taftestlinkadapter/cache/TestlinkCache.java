package se.claremont.taftestlinkadapter.cache;

import br.eti.kinoshita.testlinkjavaapi.constants.TestCaseDetails;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import br.eti.kinoshita.testlinkjavaapi.model.TestSuite;
import se.claremont.taftestlinkadapter.testlink.TestlinkReporter;

import java.util.ArrayList;
import java.util.List;

/**
 * A cache mechanism to speed up test case identification
 *
 * Created by jordam on 2017-03-26.
 */
public class TestlinkCache {
    private static List<TestlinkTestProjectCacheElement> testProjectCache = new ArrayList<>();
    private static List<TestlinkTestPlanCacheElement> testPlanCache = new ArrayList<>();
    private static List<TestlinkTestSuiteCacheElement> testSuiteCache = new ArrayList<>();
    private static List<TestlinkTestCaseCacheElement> testCaseCache = new ArrayList<>();
    private TestlinkReporter testlinkReporter;

    public TestlinkCache(TestlinkReporter testlinkReporter){
        this.testlinkReporter = testlinkReporter;
    }

    public List<TestlinkTestProjectCacheElement> getTestProjects(){
        if(testProjectCache.size() == 0){
            for(TestProject testProject : testlinkReporter.api.api.getProjects()){
                testProjectCache.add(new TestlinkTestProjectCacheElement(testProject));
            }
        }
        return testProjectCache;
    }

    public List<TestlinkTestPlanCacheElement> getTestPlans(Integer testProjectId){
        if(testPlanCache.size() == 0){
            for(TestPlan testPlan: testlinkReporter.api.api.getProjectTestPlans(testProjectId)){
                testPlanCache.add(new TestlinkTestPlanCacheElement(testPlan, testProjectId));
            }
        }
        return testPlanCache;
    }

    public List<TestlinkTestSuiteCacheElement> getTestSuites(Integer testPlanId){
        if(testSuiteCache.size() == 0){
            for(TestSuite testSuite: testlinkReporter.api.api.getTestSuitesForTestPlan(testPlanId)){
                testSuiteCache.add(new TestlinkTestSuiteCacheElement(testSuite, testPlanId));
            }
        }
        return testSuiteCache;
    }

    public List<TestlinkTestCaseCacheElement> getTestCases(Integer testSuiteId){
        if(testCaseCache.size() == 0){
            for(br.eti.kinoshita.testlinkjavaapi.model.TestCase testlinkTestCase : testlinkReporter.api.api.getTestCasesForTestSuite(testSuiteId, true, TestCaseDetails.FULL)){
                testCaseCache.add(new TestlinkTestCaseCacheElement(testlinkTestCase, testSuiteId));
            }
        }
        return testCaseCache;
    }

    public static void addTestProjectToCache(TestProject testProject){
        TestlinkCache.testProjectCache.add(new TestlinkTestProjectCacheElement(testProject));
    }

    public static void addTestPlanToCache(TestPlan testPlan, Integer testlinkTestProjectId){
        TestlinkCache.testPlanCache.add(new TestlinkTestPlanCacheElement(testPlan, testlinkTestProjectId));
    }

    public static void addTestSuiteToCache(TestSuite testSuite, Integer testlinkTestPlanId){
        TestlinkCache.testSuiteCache.add(new TestlinkTestSuiteCacheElement(testSuite, testlinkTestPlanId));
    }

    public void addTestCaseToCache(TestCase testCase, Integer testlinkTestSuiteId){
        TestlinkCache.testCaseCache.add(new TestlinkTestCaseCacheElement(testCase, testlinkTestSuiteId));
    }

}
