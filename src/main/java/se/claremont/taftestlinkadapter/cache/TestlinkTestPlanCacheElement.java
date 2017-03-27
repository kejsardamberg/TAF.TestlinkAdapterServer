package se.claremont.taftestlinkadapter.cache;

import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;

/**
 * Testlink TestPlan cacheable element
 *
 * Created by jordam on 2017-03-26.
 */
public class TestlinkTestPlanCacheElement {
    TestPlan testPlan;
    Integer id;
    Integer projectId;

    public TestlinkTestPlanCacheElement(TestPlan testPlan, Integer testProjectId){
        this.testPlan = testPlan;
        this.id = testPlan.getId();
        this.projectId = testProjectId;
    }

    public Integer getId(){
        return id;
    }

    public Integer getTestProjectId(){
        return projectId;
    }

    public TestPlan getTestPlan(){
        return testPlan;
    }
}
