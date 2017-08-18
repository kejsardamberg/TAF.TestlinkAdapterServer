package se.claremont.taftestlinkadapter.server;

/**
 * Static test run settings with default values for important runtime parameters.
 *
 * Created by jordam on 2017-03-19.
 */
public class Settings {

    /**
     * The API version need to be updated when relevant structural changes to TAF occurs.
     */
    public static String currentApiVersion = "v1";

    /**
     * The IP port where this server sets up it endpoint at.
     */
    public static int port = 2221;

    /**
     * The address to the Testlink API endpoint
     */
    public static String testlinkServerAddress = "http://127.0.0.1/testlink/lib/api/xmlrpc/v1/xmlrpc.php";

    /**
     * The Testlink user account name used for communication with Testlink
     */
    public static String testlinkUserName = null;

    /**
     * The Testlink developer key generated for a user in the Testlink GUI (User Account Page).
     */
    public static String testlinkDevKey = null;

    /**
     * Any connection to Testlink has a timeout for achieving a successful connection.
     * The parameter below sets the timeout, stated in seconds.
     */
    public static String testlinkServerConnectionTimeoutInSeconds = "10";

    /**
     * If a suitable test case to report results to cannot be identified in Testlink a new
     * test case is created (if only one test project exist, that test project is used. If several test projects
     * exist, or none exist, a test project named as below is created.).
     * The following parameter states in what TestProject in Testlink unidentified test cases are created.
     */
    public static String defaultTestProjectNameForNewTestCases = "Test automation project";

    /**
     * If a suitable test case to report results to cannot be identified in Testlink a new
     * test case is created-
     * The following parameter states in what TestSuite in Testlink unidentified test cases are created.
     */
    public static String defaultTestSuiteNameForNewTestCases = "Automated regression tests";

    /**
     * If a suitable test case to report results to cannot be identified in Testlink a new
     * test case is created (if only one test plan exist, that test plan is used. If several test plans
     * exist, or none exist, a test plan named as below is created.).
     * The following parameter states in what TestPlan in Testlink unidentified test cases are created.
     */
    public static String defaultTestPlanName = "Automated regression tests";


    /**
     * If a suitable test case to report results to cannot be identified in Testlink a new
     * test case is created. If no suitable Testlink build object is found for this test case,
     * the value for this parameter will be used as name for the new build.
     */
    public static String defaultBuildNameForNewTestCases = "Default automation build";
}
