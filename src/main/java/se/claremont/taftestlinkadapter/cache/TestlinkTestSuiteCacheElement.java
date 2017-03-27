package se.claremont.taftestlinkadapter.cache;

import br.eti.kinoshita.testlinkjavaapi.model.TestSuite;

/**
 * Testlink TestSuite cacheable element
 *
 * Created by jordam on 2017-03-26.
 */
public class TestlinkTestSuiteCacheElement {
    Integer id;
    Integer testPlanId;
    TestSuite testSuite;

    public TestlinkTestSuiteCacheElement(TestSuite testSuite, Integer testPlanId) {
        this.testSuite = testSuite;
        this.testPlanId = testPlanId;
        this.id = testSuite.getId();
    }

    public Integer getId() {
        return id;
    }

    public Integer getTestPlanId(){
        return testPlanId;
    }

    public TestSuite getTestSuite(){
        return testSuite;
    }
}
