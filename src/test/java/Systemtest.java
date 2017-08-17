import org.junit.BeforeClass;
import org.junit.Test;
import se.claremont.autotest.common.logging.LogLevel;
import se.claremont.autotest.common.reporting.testrunreports.TestlinkAdapterTestRunReporter;
import se.claremont.autotest.common.testrun.TestRun;
import se.claremont.autotest.common.testset.TestSet;
import se.claremont.taftestlinkadapter.server.Settings;
import se.claremont.taftestlinkadapter.testlink.TestRunRegistration;

/**
 * Sandbox tests
 *
 * Created by jordam on 2017-03-24.
 */
public class Systemtest extends TestSet {
    @BeforeClass
    public static void classSetup(){
        Settings.testlinkDevKey = "0e78ec457224441d53f2fb27e4f78025";
        Settings.testlinkServerAddress = "http://172.16.201.90:81/testlink/lib/api/xmlrpc/v1/xmlrpc.php";
        Settings.testlinkUserName = "yoda";
        Settings.port = 2221;
    }

    @Test
    public void ansökan(){
        currentTestCase.setName("Dummy Test Case");
        currentTestCase.log(LogLevel.INFO, "Doing ok.");
        currentTestCase.log(LogLevel.DEBUG, "Debug row.");
        currentTestCase.log(LogLevel.EXECUTED, "Executing ok.");
        currentTestCase.log(LogLevel.VERIFICATION_PASSED, "Passing ok");
        currentTestCase.testCaseResult.evaluateResultStatus();
        TestlinkAdapterTestRunReporter reporter = new TestlinkAdapterTestRunReporter();
        reporter.evaluateTestCase(currentTestCase);
        TestRunRegistration testRunRegistration = new TestRunRegistration(reporter.toJson());
        System.out.println(testRunRegistration.log.toString());
    }

    @Test
    public void newTest5(){
        TestRun.setSettingsValue(se.claremont.autotest.common.testrun.Settings.SettingParameters.URL_TO_TESTLINK_ADAPTER, "http://newaddress");
        currentTestCase.log(LogLevel.VERIFICATION_PASSED, "Passing ok");
        currentTestCase.testCaseResult.evaluateResultStatus();
        TestlinkAdapterTestRunReporter reporter = new TestlinkAdapterTestRunReporter();
        reporter.evaluateTestCase(currentTestCase);
        TestRunRegistration testRunRegistration = new TestRunRegistration(reporter.toJson());
        System.out.println(testRunRegistration.log.toString());
    }
}
