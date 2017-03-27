import org.junit.*;
import se.claremont.autotest.common.testset.TestSet;
import se.claremont.autotest.restsupport.RestSupport;
import se.claremont.taftestlinkadapter.server.HttpServer;
import se.claremont.taftestlinkadapter.server.Settings;

/**
 * Tests to check Page Views
 *
 * Created by jordam on 2017-03-24.
 */
public class PageViewTest extends TestSet {
    static HttpServer httpServer = null;
    RestSupport restSupport = null;

     @BeforeClass
     public static void classSetup(){
         Settings.testlinkDevKey = "2a861343a3dca60b876ca5b6567568de";
         Settings.testlinkServerAddress = "http://127.0.0.1:81/testlink/lib/api/xmlrpc/v1/xmlrpc.php";
         Settings.testlinkUserName = "joda";
         Settings.port = 2221;
         httpServer = new HttpServer();
         httpServer.start();
     }

     @AfterClass
     public static void classTeardown(){
         if(httpServer != null){
             httpServer.stop();
         }
     }

    @Before
    public void testSetup(){
         restSupport = new RestSupport(currentTestCase);
    }

    @Test
    public void serverStart(){
        Assert.assertTrue(httpServer.isStarted());
    }

}
