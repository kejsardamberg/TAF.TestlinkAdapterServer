package se.claremont.taftestlinkadapter.webpages;

import se.claremont.taftestlinkadapter.server.HttpServer;
import se.claremont.taftestlinkadapter.server.Settings;

/**
 * The about page displaying information about this server
 *
 * Created by jordam on 2017-03-23.
 */
public class AboutPage {

    public static String toHtml(){
        return InfoPage.toHtml("<h1>TAF Testlink Adapter Server</h1>" +
                "<p>This server helps propagate results from test runs performed in the test automation solution TAF to open source test and require management tool Testlink.</p>" +
                "<h2>Run status</h2>" +
                "<table>" +
                "<tr><td>Address to Testlink API:</td><td>" + Settings.testlinkServerAddress + "</td></tr>" +
                "<tr><td>Listening for incoming TAF test run data on port:</td><td>" + Settings.port + "</td></tr>" +
                "<tr><td>Current IP address of this TAF Testlink Adapter Server:</td><td>" + HttpServer.getIPAddressesOfLocalMachine() + "</td></tr>" +
                "</table>" +
                "<p><a href=\"taftestlinkadapter/version\">Software version</a>" +
                "</p><p><a href=\"taftestlinkadapter/apiversion\">Implemented API version</a></p>" +
                "<h2>About TAF</h2><p>" +
                "TAF is an open source test automation framework to quickly get going with robust " +
                "and maintainable scripts for test automation. It is developed by <a href=\"http://www.claremont.se\">Claremont</a>, a " +
                "Swedish company.<br>For more information about TAF, please visit the " +
                "<a href=\"https://github.com/claremontqualitymanagement/TestAutomationFramework\" target=\"_blank\">" +
                "TAF home on GitHub</a>, and the " +
                "<a href=\"https://github.com/claremontqualitymanagement/TestAutomationFramework/wiki\" target=\"_blank\">" +
                "TAF wiki on that site</a>.<br>" +
                "<img src=\"http://46.101.193.212/TAF/images/Taf2.png\" alt=\"TAF logo\"><br>" +
                "<h2>About TAF Testlink Adapter Server</h2>" +
                "</p><p>" +
                "For more information about <b>TAF Testlink Adapter Server</b> and how it works, please find the " +
                "<a href=\"https://github.com/kejsardamberg/TafTestlinkAdapterServer\">TAF Testlink Adapter Server page on GitHub</a><br>" +
                "<img width=\"50%\" src=\"http://46.101.193.212/TAF/images/TafTestlinkAdapterServerOverviewV2.png\" alt=\"Schematic overview of TAF Testlink Adapter Server\">" +
                "</p>");
    }
}
