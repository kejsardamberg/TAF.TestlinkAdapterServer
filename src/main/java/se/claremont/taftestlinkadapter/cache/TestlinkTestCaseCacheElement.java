package se.claremont.taftestlinkadapter.cache;

import br.eti.kinoshita.testlinkjavaapi.model.TestCase;

/**
 * Testlink TestCase cacheable element
 *
 * Created by jordam on 2017-03-26.
 */
public class TestlinkTestCaseCacheElement {
    Integer id;
    Integer testSuiteId;
    TestCase testCase;

    public TestlinkTestCaseCacheElement(TestCase testlinkTestCase, Integer testSuiteId) {
        this.testCase = testlinkTestCase;
        this.id = testlinkTestCase.getId();
        this.testSuiteId = testSuiteId;
    }

    public Integer getId(){
        return id;
    }

    public Integer getTestSuiteId(){
        return testSuiteId;
    }

    public TestCase getTestCase(){
        return testCase;
    }
}
