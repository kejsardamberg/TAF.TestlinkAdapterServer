package se.claremont.taftestlinkadapter.testlink;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.util.TestLinkAPIException;
import se.claremont.taftestlinkadapter.server.Settings;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * A connection to the Testlink server, via the API provided.
 *
 * Created by jordam on 2017-03-24.
 */
public class TestlinkClient {

    public br.eti.kinoshita.testlinkjavaapi.TestLinkAPI api;

    public TestlinkClient(){
        try {
            api = new TestLinkAPI(new URL(Settings.testlinkServerAddress), Settings.testlinkDevKey);
        } catch (MalformedURLException e) {
            System.out.println(e.toString());
        }
    }

    public TestlinkClient(String url, String devKey){
        try {
            api = new TestLinkAPI(new URL(url), devKey);
        } catch (MalformedURLException | TestLinkAPIException e) {
            System.out.println(e.toString());
        }
    }
}
