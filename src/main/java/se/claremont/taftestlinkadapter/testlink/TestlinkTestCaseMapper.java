package se.claremont.taftestlinkadapter.testlink;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Testlink TestCase mapper, mapping the TAF TestCase to its essentials to
 * not have to reference this code base to TAF code base.
 *
 * Created by jordam on 2017-03-26.
 */
class TestlinkTestCaseMapper {
    @JsonProperty String testName;
    @JsonProperty String testSetName;
    @JsonProperty String notes;
    @JsonProperty String executionStatus;

    /**
     * Constructor needed only for auto JSON parsing
     */
    public TestlinkTestCaseMapper(){}

}
