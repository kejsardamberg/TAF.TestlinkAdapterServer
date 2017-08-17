package se.claremont.taftestlinkadapter.eventstore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Event store that logs all access to this server, to be able to read it back in case of failure.
 *
 * Created by jordam on 2017-03-24.
 */
public class EventStoreManager {

    public static void registerTestRun(String json){
        String logPost = "Test run registered " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()) + ":" + System.lineSeparator() + json + System.lineSeparator() + System.lineSeparator();
        try {
            if(!Files.exists(Paths.get("TafTestlinkAdapterServer.log"))){
                Files.createFile(Paths.get("TafTestlinkAdapterServer.log"));
            }
            Files.write(Paths.get("TafTestlinkAdapterServer.log"), logPost.getBytes(), StandardOpenOption.APPEND);
        }catch (IOException e) {
            System.out.println("Tried appending to file based event store log file, but something went wrong. Error: " + e.toString());
        }
    }

}
