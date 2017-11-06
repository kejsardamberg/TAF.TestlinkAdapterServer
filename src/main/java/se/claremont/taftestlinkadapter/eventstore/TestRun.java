package se.claremont.taftestlinkadapter.eventstore;

import se.claremont.taftestlinkadapter.server.App;
import se.claremont.taftestlinkadapter.testlink.TestRunRegistration;
import se.claremont.taftestlinkadapter.testlink.TestlinkTestCasesFromTestRun;

import java.io.IOException;
import java.util.Date;

public class TestRun {
    Date time;
    TestlinkTestCasesFromTestRun testlinkTestCasesFromTestRun;
    TestRunInfoTransferStatus testRunInfoTransferStatus;

    public TestRun(String testRunJsonString){
        time = new Date();
        testlinkTestCasesFromTestRun = getTestlinkReporterObject(testRunJsonString);
        testRunInfoTransferStatus = TestRunInfoTransferStatus.TRANSFER_NOT_ATTEMPTED_YET;
    }

    public void setStatus(TestRunInfoTransferStatus testRunInfoTransferStatus){
        this.testRunInfoTransferStatus = testRunInfoTransferStatus;
    }

    /**
     * Converts the JSON with the run results from the TAF test run into proper Java objects.
     *
     * @param json Input JSON
     * @return Returns an object with a set of TAF TestCases equivalents for further use.
     */
    private TestlinkTestCasesFromTestRun getTestlinkReporterObject(String json){
        TestlinkTestCasesFromTestRun testlinkTestCasesFromTestRun = null;
        try {
            testlinkTestCasesFromTestRun = App.mapper.readValue(json, TestlinkTestCasesFromTestRun.class);
        } catch (IOException ignored) {
        }
        return testlinkTestCasesFromTestRun;
    }



}
