package se.claremont.taftestlinkadapter.testlink;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import se.claremont.taftestlinkadapter.server.Settings;

import java.net.URL;
import java.util.concurrent.*;

/**
 * A connection to the Testlink server, via the API provided.
 *
 * Created by jordam on 2017-03-24.
 */
public class TestlinkClient {

    public TestLinkAPI api;

    public TestlinkClient(){
        api = getTestlinkApiConnection();
        if(api == null){
            System.out.println("Could not connect to the Testlink server at '" + Settings.testlinkServerAddress + "' within the stated timeout of " + Settings.testlinkServerConnectionTimeoutInSeconds + " seconds.");
        } else {
            System.out.println("Connection to Testlink server successfully established.");
        }
    }

    private static TestLinkAPI getTestlinkApiConnection() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        TestLinkAPI client = null;
        Future<TestLinkAPI> future = executor.submit(new TestlinkConnection());
        try {
            Integer timeoutInSeconds = Integer.parseInt(Settings.testlinkServerConnectionTimeoutInSeconds);
            if(timeoutInSeconds == null) timeoutInSeconds = 10;
            client = future.get(timeoutInSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
        } catch (InterruptedException e) {
            System.out.println(System.lineSeparator() + "OUPS! Failed to connect to Testlink server. Error:");
            System.out.println(e.getMessage() + System.lineSeparator());
        } catch (ExecutionException e) {
            System.out.println(System.lineSeparator() + "OUPS! Failed to connect to Testlink server. Error:");
            System.out.println(e.getMessage() + System.lineSeparator());
        }
        executor.shutdownNow();
        return client;
    }

    private static class TestlinkConnection implements Callable<TestLinkAPI> {
        @Override
        public TestLinkAPI call() throws Exception{
            return new TestLinkAPI(new URL(Settings.testlinkServerAddress), Settings.testlinkDevKey);
        }
    }
}
