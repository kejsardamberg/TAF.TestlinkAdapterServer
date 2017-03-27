# TAF Testlink Adapter Server
**The TAF Testlink Adapter Server** is a proxy server for pushing test case results from a TAF test execution to *Testlink*. It require some parameters to start this TAF Testlink Adapter Server, and one minor addition to your TAF tests.

For more information about TAF, visit the [TAF GitHub repo](https://github.com/claremontqualitymanagement/TestAutomationFramework "TAF on GitHub").

**Testlink** is an open source test and requirement management tool. More information about Testlink, and a demo version, can be found on the [Testlink site](http://testlink.org/ "Testlink web site").

## Schematic overview of TAF Testlink Adapter Server
![Schematic overview of TAF Testlink Adapter Server workings](http://46.101.193.212/TAF/images/TafTestlinkAdapterServerOverviewV2.png "Overview of how this adapter server works")

## How this adapter works
The TAF test automation has a build in test runner and test listener. When a test run is finished a check is performed if the test run settings parameter called:

    TestRun.setSettingsValue(Settings.SettingParameters.URL_TO_TESTLINK_ADAPTER, "http://127.0.0.1:2221/taftestlinkadapter");

If this settings value is changed from its original value (for the record: It is meant to substitute the address and port to your own ones in the line above). If it is actively set to something other than its default value an attempt to push the test result to the TAF Testlink Adapter Server will be performed. 

*The TAF Testlink Adapter Server* has a few REST interfaces, and can display a few web pages. 

The TAF test execution run results are formatted to JSON data format and POST:ed to the *TAF Testlink Adapter Server* REST API.

### Testlink test case identification sequence
*The TAF Testlink Adapter Server* then connects to the Testlink server and tries to identify the test cases to report results to. The identification sequence is as follows: 
1. If a test case with a name corresponding to the TAF test case in the test run is found, and either the Testlink test suite name or the Testlink test plan name for that test case has a name containing the TAF test case test set name, reporting will be performed to that test case.
2. If only one Testlink test case with a name corresponding to the TAF test case is found reporting will be performed to that Testlink test case.
3. If several test cases with corresponding names are found reporting will be performed to the first test case encountered.
4. If no corresponding test case is found at all in Testlink a new one will be created, but in a Testlink test project called 'Test automation project' and a test plan named 'Automated regression tests' and a Testlink test suite named after the TAF test case test set name.

Test case reporting is always performed againt the latest Testlink build for the Testlink test plan.

The *TAF Testlink Adapter Server* also has a built in cache for Testlink resources, since lookup of these are quite slow. Although this should not bring any problems, if the adapter seem to behave badly, try restarting the adapter. 

The server will continuously present output while performing its tasks.

# Get started
Four things are required to get this adapter working.
1. **Pre-requisites** Make sure you have a test automation in a TAF with a version of 2.5.24 or later. Make sure you have the host name or IP address of your Testlink installation.
2. Get hold of a **TafTestlinkAdapterServer.jar** file.
3. Start your TafTestlinkAdapterServer.jar with appropriate parameters.
4. Modify your TAF test setup to include Testlink reporting.

All of those steps will be walked through below.

## Achieve a jar
There are two ways of getting started with this adapter server.
1. Either clone this repository and build it to a jar file. Maven will do this for you if you have maven installed.
2. [Download](http://46.101.193.212/TAF/bin/TafTestlinkAdapterServer.jar "TAF Testlink Adapter Server jar file download") a readily built jar file and place it in a folder of your choice on your machine. The adapter server will produce a log file in the same directory.

## Getting the TAF Testlink Adapter Server started
A few command line parameters are needed at *TAF Testlink Adapter Server* startup:
  * `port=2222` (Port number is of your own choice. Make sure it is not the same as the Testlink server uses. Default port is 81.)
  * `username=taftestlinkuser` (Used to create test cases in Testlink, so make sure you use a valid user name for Testlink.
  * `testlinkaddress=http://mytestlinkserver:80/testlink/lib/api/xmlrpc/v1/xmlrpc.php` (Make sure the full adress to the Testlink API is there. Default is local host, at TCP port 80.)
  * `devkey=2a861343a3dca60b876ca5b6567568de` (you can find the Testlink API DevKey on the user page in Testlink, called 'API interface Personal API access key'. The key in this example is only an example, so you know what to look for.)

All of these run time parameters are case insensitive and the order of them are irrelevant.

### Usage example
*The TAF Testlink Adapter Server* is started from the command line with a few parameters.

     java -jar TafTestlinkAdapterServer.jar testlinkaddress=http://mytestlinkserver:80/testlink/lib/api/xmlrpc/v1/xmlrpc.php devkey=2a861343a3dca60b876ca5b6567568de username=taftestlinkuser

where port number is the *TAF Testlink Adapter Server* port number for TAF to connect to, also stated as the TCP port in the URL given as a run settings parameter in the TAF test run (e.g. http://localhost:8080/taftestlinkadapter).
Default TCP port is 80. The important part is that it should not be a port in use already, by Testlink or other web server.

## Modifications on the TAF tests
You need to set the setting called `URL_TO_TESTLINK_ADAPTER` on your test execution. One way of doing this is from the command line when starting your TAF test run.

### Via command line interface at test run execution

     java -jar TafFull.jar MyTestClasses URL_TO_TESTLINK_ADAPTER=http://localhost:2222/taftestlinkadapter

### Programatically in your TAF tests
Another way is programatically, by adding the Testlink adapter, like in the example below.
```java
    @BeforeClass
    public static void setup(){
        TestRun.setSettingsValue(Settings.SettingParameters.URL_TO_TESTLINK_ADAPTER, 
               "http://127.0.0.1:2221/taftestlinkadapter");
   }
```

Doing this will engage the reporting to this adapter.

## More information
For more information, clone this repo, or [download the Javadocs](http://46.101.193.212/TAF/bin/TafTestlinkAdapterServer-javadoc.jar "Javadocs for TAF Testlink Adapter Server").

gl:hf
