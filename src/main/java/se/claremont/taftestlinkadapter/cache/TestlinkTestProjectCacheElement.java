package se.claremont.taftestlinkadapter.cache;

import br.eti.kinoshita.testlinkjavaapi.model.TestProject;

/**
 * Testlink TestProject cacheable element
 *
 * Created by jordam on 2017-03-26.
 */
public class TestlinkTestProjectCacheElement {
    Integer id;
    TestProject testProject;

    public TestlinkTestProjectCacheElement(TestProject testProject){
        this.testProject = testProject;
        this.id = testProject.getId();
    }

    public Integer getId(){
        return id;
    }

    public TestProject getTestProject(){
        return testProject;
    }
}
