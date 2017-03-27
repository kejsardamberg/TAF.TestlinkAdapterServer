package se.claremont.taftestlinkadapter.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * The server itself. This is the simple HTTP server that hosts the REST services
 *
 * Created by jordam on 2017-03-18.
 */
public class HttpServer {

    ResourceConfig config = new ResourceConfig();
    Server server;

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
        if(isStarted()){
            System.out.println(System.lineSeparator() + "Server started." + System.lineSeparator());
            System.out.println("Connect at = http://" + getIPAddressesOfLocalMachine() + ":" + Settings.port + "/taftestlinkadapter");
            System.out.println("Testlink API address to attempt connect = " + Settings.testlinkServerAddress);
            System.out.println("Testlink user name = " + Settings.testlinkUserName);
        } else {
            System.out.println(System.lineSeparator() + "Could not start server." + System.lineSeparator());
            return;
        }
        System.out.println(System.lineSeparator() + "Server ready to serve." + System.lineSeparator());
    }


    public boolean isStarted(){
        return (server != null && !server.isFailed());
    }

    public void stop(){
        try{
            server.stop();
            server.destroy();
            System.out.println("Server stopped." + System.lineSeparator());
        }catch (Exception e){
            System.out.println("Error stopping HTTP server: " + e.toString());
        }
    }

    public static String getIPAddressesOfLocalMachine(){
        String ip = "Could not identify local IP address.";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return ip;
    }
}
