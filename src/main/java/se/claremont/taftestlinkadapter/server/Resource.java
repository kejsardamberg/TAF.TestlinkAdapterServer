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

    /**
     * Returns version of this server.
     *
     * @return Returns version of this server.
     */
    @GET
    @Path("version")
    @Produces(MediaType.TEXT_HTML)
    public String versionHtml() {
        System.out.println("Got a request for version.");
        return InfoPage.toHtml("<p>TAF Testlink Adapter Server code version 1.1.<br><br>Try a GET request to <i>'/apiversion'</i> for supported API version.</p>");
    }

    /**
     * Returns version of this server.
     *
     * @return Returns version of this server.
     */
    @SuppressWarnings("SameReturnValue")
    @GET
    @Path("version")
    @Produces(MediaType.TEXT_PLAIN)
    public String versionPlainText() {
        System.out.println("Got a request for version.");
        return "TAF Testlink Adapter Server code version 0.1.";
    }

    /**
     * Returns API version suppurted by this server - for the API between TAF and this server (alas not the Testlink API version).
     *
     * @return Returns API version of this server.
     */
    @GET
    @Path("apiversion")
    @Produces(MediaType.TEXT_HTML)
    public String apiVersion() {
        return InfoPage.toHtml("<p>TAF Testlink Adapter Server REST API version: <i>'" + Settings.currentApiVersion + "'</i>.</p>");
    }

    /**
     * Landing page for general identification of this server.
     *
     * @return Return general identification information
     */
    @GET
    @Path("about")
    @Produces(MediaType.TEXT_HTML)
    public String about() {
        return AboutPage.toHtml();
    }

    /**
     * Returns API version suppurted by this server - for the API between TAF and this server (alas not the Testlink API version).
     *
     * @return Returns API version of this server.
     */
    @GET
    @Path("apiversion")
    @Produces(MediaType.TEXT_PLAIN)
    public String apiVersionText() {
        return "TAF Testlink Adapter Server REST API version: '" + Settings.currentApiVersion + "'.";
    }

    /**
     * Landing page is simple redirect to About Page.
     *
     * @return Returns the about page.
     */
    @GET
    @Path("")
    @Produces(MediaType.TEXT_HTML)
    public String landingPage() {
        return AboutPage.toHtml();
    }

    /**
     * API endpoint for posting test run results from TAF to be read into Testlink. The response is status information.
     *
     * @param testRun The JSON from the TAF test run execution TestRunResults object.
     * @return Returns a report from the information transfer. If errors are encountered more information is displayed.
     */
    @POST
    @Path("v1/testrun")
    public String postTestRun(String testRun) {
        System.out.println("Received POST request to /taftestlinkadapter/v1/testrun/ with content: '" + testRun + "'." + System.lineSeparator());
        TestRunRegistration testRunRegistration = new TestRunRegistration();
        try{
            testRunRegistration.reportTestRun(testRun);
            return testRunRegistration.log.toString();
        } catch (Exception e){
            StringBuilder error = new StringBuilder();
            error.append(System.lineSeparator());
            error.append("Problems encountered in 'TAF Testlink Adapter Server' while trying to register TAF test run to Testlink.");
            error.append(System.lineSeparator());
            error.append("Error: ");
            error.append(e.getMessage());
            error.append(System.lineSeparator());
            error.append("Cause: ");
            error.append(e.getCause());
            error.append(System.lineSeparator());
            try{
                for(StackTraceElement stackTraceElement : e.getStackTrace()){
                    error.append(stackTraceElement.toString()).append(System.lineSeparator());
                }
                error.append(System.lineSeparator());
            }catch (Exception ignored){}
            error.append("Debug log entries for test run registration from TAF Testlink Adapter Server:");
            error.append(System.lineSeparator());
            error.append(testRunRegistration.debugLog.toString());
            System.out.println(error.toString());
            return error.toString();
        }
    }
}