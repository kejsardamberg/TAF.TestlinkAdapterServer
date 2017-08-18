package se.claremont.taftestlinkadapter.server;

import se.claremont.taftestlinkadapter.testlink.TestRunRegistration;
import se.claremont.taftestlinkadapter.webpages.AboutPage;
import se.claremont.taftestlinkadapter.webpages.InfoPage;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * The different end-points of the HTTP server
 *
 * Created by jordam on 2017-03-18.
 */
@Path("taftestlinkadapter")
public class Resource {

    @GET
    @Path("version")
    @Produces(MediaType.TEXT_HTML)
    public String versionHtml() {
        System.out.println("Got a request for version.");
        return InfoPage.toHtml("<p>TAF Testlink Adapter Server code version 0.1.<br><br>Try a GET request to <i>'/apiversion'</i> for supported API version.</p>");
    }

    //GET /taf/version
    @SuppressWarnings("SameReturnValue")
    @GET
    @Path("version")
    @Produces(MediaType.TEXT_PLAIN)
    public String versionPlainText() {
        System.out.println("Got a request for version.");
        return "TAF Testlink Adapter Server code version 0.1.";
    }

    //GET /taf/version
    @GET
    @Path("apiversion")
    @Produces(MediaType.TEXT_HTML)
    public String apiVersion() {
        return InfoPage.toHtml("<p>TAF Testlink Adapter Server REST API version: <i>'" + Settings.currentApiVersion + "'</i>.</p>");
    }

    //GET /taf/version
    @GET
    @Path("about")
    @Produces(MediaType.TEXT_HTML)
    public String about() {
        return AboutPage.toHtml();
    }

    //GET /taf/version
    @GET
    @Path("apiversion")
    @Produces(MediaType.TEXT_PLAIN)
    public String apiVersionText() {
        return "TAF Testlink Adapter Server REST API version: '" + Settings.currentApiVersion + "'.";
    }

    @GET
    @Path("")
    @Produces(MediaType.TEXT_HTML)
    public String landingPage() {
        return AboutPage.toHtml();
    }

    //Should be re-name to v1/testruns for compliance? Then it needs to be updated in TAF as well - and possibly the API version updated.
    @POST
    @Path("v1/testrun")
    public String postTestRun(String testRun) {
        System.out.println("Received POST request to /taftestlinkadapter/v1/testrun/ with content: '" + testRun + "'." + System.lineSeparator());
        TestRunRegistration testRunRegistration = new TestRunRegistration();
        try{
            testRunRegistration.reportTestRun(testRun);
            return testRunRegistration.log.toString();
        } catch (Exception e){
            System.out.println("Could not register test run. Error: " + e.getMessage() + ". Cause: " + e.getCause());
            StringBuilder error = new StringBuilder();
            for(StackTraceElement stackTraceElement : e.getStackTrace()){
                error.append(stackTraceElement.toString()).append(System.lineSeparator());
            }
            System.out.println(error);
            return "Problem. Could not register test run to Testlink. Error: " + e.getMessage() + System.lineSeparator() + System.lineSeparator() + "Debug log entries for test run registration from TAF Testlink Adapter Server:" + System.lineSeparator() + testRunRegistration.debugLog.toString() + System.lineSeparator() + System.lineSeparator() + error.toString();
        }
    }
}