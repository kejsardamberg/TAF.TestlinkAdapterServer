import org.junit.*;
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
        Settings.testlinkDevKey = "2a861343a3dca60b876ca5b6567568de";
        Settings.testlinkServerAddress = "http://127.0.0.1:81/testlink/lib/api/xmlrpc/v1/xmlrpc.php";
        Settings.testlinkUserName = "joda";
        Settings.port = 2221;
    }

    @Test
    public void testlinkConnectionEstablished(){
        TestlinkClient testlinkClient = new TestlinkClient();
        Assert.assertTrue(testlinkClient != null);
    }

    @Test
    public void testlinkReporterSetupAndTestSuiteId(){
        TestlinkReporter testlinkReporter = new TestlinkReporter();
        Assert.assertTrue(testlinkReporter != null);
        Integer i = testlinkReporter.getTestSuiteId("Mina sidor", "Inloggningstester");
        Assert.assertTrue("Expected testSuiteId to be 418, but it was " + i.toString(), i == 418);
    }

    @Test
    public void projectId(){
        TestlinkReporter testlinkReporter = new TestlinkReporter();
        Assert.assertTrue(testlinkReporter.getProjectID("Mina sidor").toString(), testlinkReporter.getProjectID("Mina sidor" ) == 1);
    }

    @Test
    public void testPlanId(){
        TestlinkReporter testlinkReporter = new TestlinkReporter();
        Assert.assertTrue(testlinkReporter.getTestPlanId("Mina sidor", "Mina sidor systemtest").toString(), testlinkReporter.getTestPlanId("Mina sidor", "Mina sidor systemtest") == 757);
    }

    @Test
    public void testSuiteId(){
        TestlinkReporter testlinkReporter = new TestlinkReporter();
        Assert.assertTrue(testlinkReporter.getTestSuiteId("Mina sidor", "Inloggningstester").toString(), testlinkReporter.getTestSuiteId("Mina sidor", "Inloggningstester") == 418);
    }

    @Test
    public void testTestProjectListing(){
        TestlinkReporter testlinkReporter = new TestlinkReporter();
        Assert.assertTrue("'" + String.join("', '", testlinkReporter.testlinkProjects()) + "'", testlinkReporter.testlinkProjects().toString().contains("Mina sidor"));

    }

    @Test
    public void reportTestResult(){
        TestlinkReporter testlinkReporter = new TestlinkReporter();
        Assert.assertTrue(testlinkReporter != null);
        Boolean success = testlinkReporter.evaluateTestCase("Mina sidor", "Mina sidor systemtest", "Inloggningstester", "30:e november", "Inloggning med BankID", "These are the notes", "pass", false);
        System.out.println(testlinkReporter.logMessage);
        Assert.assertTrue(success);
    }

}
