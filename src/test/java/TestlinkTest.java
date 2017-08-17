import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import se.claremont.taftestlinkadapter.server.Settings;
import se.claremont.taftestlinkadapter.testlink.TestlinkClient;
import se.claremont.taftestlinkadapter.testlink.TestlinkReporter;

/**
 * Sandbox tests for Testlink interaction
 *
 * Created by jordam on 2017-03-16.
 */
@SuppressWarnings("ConstantConditions")
public class TestlinkTest {
    @BeforeClass
    public static void classSetup(){
        Settings.testlinkDevKey = "0e78ec457224441d53f2fb27e4f78025";
        Settings.testlinkServerAddress = "http://172.16.201.90:81/testlink/lib/api/xmlrpc/v1/xmlrpc.php";
        Settings.testlinkUserName = "yoda";
        Settings.port = 2221;
    }

    @Test
    @Ignore
    public void testlinkConnectionEstablished(){
        TestlinkClient testlinkClient = new TestlinkClient();
        Assert.assertTrue(testlinkClient != null);
    }

    @Test
    @Ignore
    public void testlinkReporterSetupAndTestSuiteId(){
        TestlinkReporter testlinkReporter = new TestlinkReporter();
        Assert.assertTrue(testlinkReporter != null);
        Integer i = testlinkReporter.getTestSuiteId("MyPages", "DummyTestSuite");
        Assert.assertTrue("Expected testSuiteId to be 418, but it was " + i.toString(), i == 3);
    }

    @Test
    @Ignore
    public void projectId(){
        TestlinkReporter testlinkReporter = new TestlinkReporter();
        Assert.assertTrue(testlinkReporter.getProjectID("MyPages").toString(), testlinkReporter.getProjectID("MyPages" ) == 1);
    }

    @Test
    @Ignore
    public void testPlanId(){
        TestlinkReporter testlinkReporter = new TestlinkReporter();
        Assert.assertTrue(testlinkReporter.getTestPlanId("MyPages", "AutoTest").toString(), testlinkReporter.getTestPlanId("MyPages", "AutoTest") == 2);
    }

    @Test
    @Ignore
    public void testSuiteId(){
        TestlinkReporter testlinkReporter = new TestlinkReporter();
        Assert.assertTrue(testlinkReporter.getTestSuiteId("MyPages", "DummyTestSuite").toString(), testlinkReporter.getTestSuiteId("MyPages", "DummyTestSuite") == 3);
    }

    @Test
    @Ignore
    public void testTestProjectListing(){
        TestlinkReporter testlinkReporter = new TestlinkReporter();
        Assert.assertTrue("'" + String.join("', '", testlinkReporter.testlinkProjects()) + "'", testlinkReporter.testlinkProjects().toString().contains("MyPages"));

    }

    @Test
    @Ignore
    public void reportTestResult(){
        TestlinkReporter testlinkReporter = new TestlinkReporter();
        Assert.assertTrue(testlinkReporter != null);
        //Boolean success = testlinkReporter.evaluateTestCase("MyPages", "AutoTest", "DummyTestSuite", "Default build", "Dummy Test Case", "These are the notes", "pass", true);
        System.out.println(testlinkReporter.logMessage);
        //Assert.assertTrue(success);
    }

}
