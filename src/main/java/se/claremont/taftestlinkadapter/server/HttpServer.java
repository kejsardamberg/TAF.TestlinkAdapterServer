package se.claremont.taftestlinkadapter.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import se.claremont.taftestlinkadapter.testlink.TestlinkClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This class holds the gateway server itself. This is the simple HTTP server
 * that hosts the REST services the TAF installation connects to as well as
 * some HTML pages.
 *
 * Created by jordam on 2017-03-18.
 */
public class HttpServer {

    private ResourceConfig config = new ResourceConfig();
    Server server;

    /**
     * Starting the server to enable communication.
     */
    public void start(){
        System.out.println(System.lineSeparator() + "Starting TAF Testlink Adapter Server." + System.lineSeparator());
        config.packages("se.claremont.taftestlinkadapter");
        ServletHolder servlet = new ServletHolder(new ServletContainer(config));
        server = new Server(Settings.port);
        ServletContextHandler context = new ServletContextHandler(server, "/*");
        context.addServlet(servlet, "/*");
        try {
            server.start();
        }catch (Exception e){
            System.out.println(System.lineSeparator() + e.toString());
        }
        System.out.println("Parameters used:");
        System.out.println(" * Testlink API endpoint used = " + Settings.testlinkServerAddress);
        System.out.println(" * TAF Testlink Adapter Server connection port = " + Settings.port);
        System.out.println(" * Testlink user name = " + Settings.testlinkUserName);
        System.out.println(" * DevKey used for Testlink API = " + Settings.testlinkDevKey);
        System.out.println(" * TAF Testlink Adapter API version = " + Settings.currentApiVersion);
        System.out.println(" * Testlink server connection timeout = " + Settings.testlinkServerConnectionTimeoutInSeconds + " seconds.");
        System.out.println(" * Default Testlink project to create unidentified test cases in = '" + Settings.defaultTestProjectNameForNewTestCases + "'");
        System.out.println(" * Default Testlink TestSuite to create unidentified test cases in = '" + Settings.defaultTestSuiteNameForNewTestCases + "'");
        System.out.println(" * Default Testlink TestPlan to assign unidentified test cases to = '" + Settings.defaultTestPlanName + "'");
        System.out.println(" * URL for TAF to connect = http://" + getIPAddressesOfLocalMachine() + ":" + Settings.port + "/taftestlinkadapter");
        if(isStarted()){
            System.out.println(System.lineSeparator() + "TAF Testlink Adapter interface server started.");
        } else {
            System.out.println(System.lineSeparator() + "Could not start server." + System.lineSeparator());
            return;
        }
        if(!checkTestlinkServerConnection()){
            System.out.println("Could not establish connection to Testlink server. Exiting.");
            stop();
            System.exit(0);
        } else {
            System.out.println("Connection to Testlink server established." + System.lineSeparator());
            System.out.println("URL for TAF to connect    = http://" + getIPAddressesOfLocalMachine() + ":" + Settings.port + "/taftestlinkadapter" + System.lineSeparator());
            System.out.println(System.lineSeparator() + "Server ready to serve." + System.lineSeparator());
        }

    }

    /**
     * Checks if the server is started.
     *
     * @return Returns true if server is running, else false.
     */
    public boolean isStarted(){
        return (server != null && !server.isFailed());
    }


    /**
     * Stop the server.
     */
    public void stop(){
        try{
            server.stop();
            server.destroy();
            System.out.println(System.lineSeparator() + "Server stopped." + System.lineSeparator());
        }catch (Exception e){
            System.out.println("Error stopping HTTP server: " + e.toString());
        }
    }

    /**
     * Identifies local IP-address of the machine this server is executed on. Used to display connect help information.
     *
     * @return Returns IP address of the machine the server is executed upon.
     */
    public static String getIPAddressesOfLocalMachine(){
        String ip = "Could not identify local IP address.";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return ip;
    }

    /**
     * Checks if a successful connection to the Testlink server can be established.
     *
     * @return Return true if a successful connection can be made with the Testlink server API, given the runtime parameters stated (username and DevKey).
     */
    private static boolean checkTestlinkServerConnection() {
        System.out.print("Checking connection to Testlink (timeout " + Settings.testlinkServerConnectionTimeoutInSeconds + " seconds).");
        TestlinkClient client = new TestlinkClient();
        System.out.print(System.lineSeparator());
        return client.api != null;
    }

}
