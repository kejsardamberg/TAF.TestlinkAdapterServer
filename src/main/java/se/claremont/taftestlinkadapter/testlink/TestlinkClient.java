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

    /**
     * The connection to the Testlink APi
     */
    public TestLinkAPI api;

    /**
     * Creating a connection to the Testlink API.
     */
    public TestlinkClient(){
        api = getTestlinkApiConnection();
        if(api == null){
            System.out.println(System.lineSeparator() + "Could not connect to the Testlink server at '" + Settings.testlinkServerAddress + "' within the stated timeout of " + Settings.testlinkServerConnectionTimeoutInSeconds + " seconds.");
        } else {
            System.out.println(System.lineSeparator() + "Connection to Testlink server successfully established.");
        }
    }

    /**
     * Attempts setting up a connection to the Testlink API given by the runtime parameters.
     *
     * @return Return a session to the Testlink API
     */
    private static TestLinkAPI getTestlinkApiConnection() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        TestLinkAPI client = null;
        Future<TestLinkAPI> future = executor.submit(new TestlinkConnection());
        ExecutorService es = Executors.newSingleThreadExecutor();
        Future f = es.submit(new DotPrinter());
        try {
            Integer timeoutInSeconds = Integer.parseInt(Settings.testlinkServerConnectionTimeoutInSeconds);
            client = future.get(timeoutInSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            f.cancel(true);
            es.shutdownNow();
            System.out.print(System.lineSeparator() + "Connection attempt timed out.");
        } catch (ExecutionException | InterruptedException e) {
            System.out.print(System.lineSeparator());
            System.out.println(System.lineSeparator() + "OUPS! Failed to connect to Testlink server. Error:");
            System.out.println(e.getMessage() + System.lineSeparator());
        } finally {
            f.cancel(true);
            es.shutdownNow();
        }
        executor.shutdownNow();
        System.out.print(System.lineSeparator());
        return client;
    }

    /**
     * A connection to a Testlink server API. Stated in a special class for threading purposes.
     */
    private static class TestlinkConnection implements Callable<TestLinkAPI> {
        @Override
        public TestLinkAPI call() throws Exception{
            return new TestLinkAPI(new URL(Settings.testlinkServerAddress), Settings.testlinkDevKey);
        }
    }

    /**
     * Cosmetic class for printing out dots at the output while waiting for a successful connection to the Testlink API.
     */
    private static class DotPrinter implements Runnable {
        @Override
        public void run(){
            while(!Thread.currentThread().isInterrupted()){
                try {
                    Thread.sleep(1000); //exclude try/catch for brevity
                } catch (InterruptedException ignored) {
                }
                System.out.print(".");
            }
        }
    }
}
