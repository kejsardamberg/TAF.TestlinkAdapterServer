package se.claremont.taftestlinkadapter.testlink;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Testlink TestCase mapper, mapping the TAF TestCase to its essentials to
 * not have to reference this code base to TAF code base.
 *
 * Created by jordam on 2017-03-26.
 */
public class TestlinkTestCaseMapper {
    @JsonProperty String testName;
    @JsonProperty String testSetName;
    @JsonProperty String notes;
    @JsonProperty String executionStatus;

    public TestlinkTestCaseMapper(){}

    public TestlinkTestCaseMapper(String testName, String testSetName, String notes, String executionStatus){
        this.testName = testName;
        this.testSetName = testSetName;
        this.notes = notes;
        this.executionStatus = executionStatus;
    }
}
