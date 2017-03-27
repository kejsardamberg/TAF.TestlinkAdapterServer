package se.claremont.taftestlinkadapter.webpages;

/**
 * The about page displaying information about this server
 *
 * Created by jordam on 2017-03-23.
 */
public class AboutPage {

    public static String toHtml(){
        return InfoPage.toHtml("<h1>TAF Testlink Adapter Server</h1>" +
                "<p>" +
                "This server helps propagate results from test runs performed in the test automation solution TAF to open source test and require management tool Testlink." +
                "</p><p>" +
                "TAF is an open source test automation framework to quickly get going with robust " +
                "and maintanable scripts for test automation. It is developed by Claremont, a " +
                "Swedish company.<br>For more information about TAF, please visit the " +
                "<a href=\"https://github.com/claremontqualitymanagement/TestAutomationFramework\" target=\"_blank\">" +
                "TAF home on GitHub</a>, and the " +
                "<a href=\"https://github.com/claremontqualitymanagement/TestAutomationFramework/wiki\" target=\"_blank\">" +
                "TAF wiki on that site</a>." +
                "</p><p>" +
                "<a href=\"taftestlinkadapter/version\">Software version</a>" +
                "</p><p>" +
                "<a href=\"taftestlinkadapter/apiversion\">Implemented API version</a>" +
                "</p>");
    }
}
