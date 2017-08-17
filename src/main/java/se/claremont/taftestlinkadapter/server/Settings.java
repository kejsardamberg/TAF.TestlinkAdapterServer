package se.claremont.taftestlinkadapter.server;

/**
 * Static test run settings
 *
 * Created by jordam on 2017-03-19.
 */
public class Settings {
    public static String currentApiVersion = "v1";
    public static String testlinkServerAddress = "http://127.0.0.1/testlink/lib/api/xmlrpc/v1/xmlrpc.php";
    public static String testlinkDevKey = null;
    public static String testlinkUserName = null;
    public static String defaultTestProjectNameForNewTestCases = "Test automation project";
    public static int port = 80;
    public static String defaultTestSuiteNameForNewTestCases = "Automated regression tests";
    public static String defaultTestPlanName = "Automated regression tests";
    public static String testlinkServerConnectionTimeoutInSeconds = "10";
}
