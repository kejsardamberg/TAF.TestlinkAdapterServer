import org.junit.BeforeClass;
import org.junit.Ignore;
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
        Settings.testlinkDevKey = "2a861343a3dca60b876ca5b6567568de";
        Settings.testlinkServerAddress = "http://127.0.0.1:81/testlink/lib/api/xmlrpc/v1/xmlrpc.php";
        Settings.testlinkUserName = "joda";
    }

    @Test
    @Ignore
    public void ansökan(){
        currentTestCase.log(LogLevel.INFO, "Doing ok.");
        currentTestCase.log(LogLevel.DEBUG, "Debug row.");
        currentTestCase.log(LogLevel.EXECUTED, "Executing ok.");
        currentTestCase.log(LogLevel.VERIFICATION_PASSED, "Passing ok");
        currentTestCase.evaluateResultStatus();
        TestlinkAdapterTestRunReporter reporter = new TestlinkAdapterTestRunReporter();
        reporter.evaluateTestCase(currentTestCase);
        TestRunRegistration testRunRegistration = new TestRunRegistration(reporter.toJson());
        System.out.println(testRunRegistration.result());
    }

    @Test
    @Ignore
    public void newTest5(){
        TestRun.setSettingsValue(se.claremont.autotest.common.testrun.Settings.SettingParameters.URL_TO_TESTLINK_ADAPTER, "http://newaddress");
        currentTestCase.log(LogLevel.VERIFICATION_PASSED, "Passing ok");
        currentTestCase.evaluateResultStatus();
        TestlinkAdapterTestRunReporter reporter = new TestlinkAdapterTestRunReporter();
        reporter.evaluateTestCase(currentTestCase);
        TestRunRegistration testRunRegistration = new TestRunRegistration(reporter.toJson());
        System.out.println(testRunRegistration.result());
    }
}
