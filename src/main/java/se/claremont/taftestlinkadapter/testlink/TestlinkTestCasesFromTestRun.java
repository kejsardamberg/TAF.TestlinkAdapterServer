package se.claremont.taftestlinkadapter.testlink;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

/**
 * A list of all test cases from a test run
 *
 * Created by jordam on 2017-03-26.
 */
class TestlinkTestCasesFromTestRun {
    @JsonProperty ArrayList<TestlinkTestCaseMapper> testCases = new ArrayList<>();

    public TestlinkTestCasesFromTestRun(){}

}
