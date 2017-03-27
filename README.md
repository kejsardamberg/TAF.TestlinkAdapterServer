# TAF Testlink Adapter Server
**The TAF Testlink Adapter Server** is a proxy server for pushing test case results from a TAF test execution to *Testlink*. It require some parameters to start this TAF Testlink Adapter Server, and one minor addition to your TAF tests.

For more information about TAF, visit the [TAF GitHub repo](https://github.com/claremontqualitymanagement/TestAutomationFramework "TAF on GitHub").

**Testlink** is an open source test and requirement management tool. More information about Testlink, and a demo version, can be found on the [Testlink site](http://testlink.org/ "Testlink web site").

## What this adapter does
The adapter takes the test results from a test run in TAF and pushes it as test results in Testlink.
![TestlinkView](http://46.101.193.212/TAF/images/TafTestlinkAdapterServer/TafTestlinkAdapterServerTestlinkView.PNG "Testlink execution view")
*Testlink execution view*

Testlink test cases are identified and test results from TAF execution is reported to that test case, or a test case is created in Testlink if no matching test case can be identified.

![LogAsNotes](http://46.101.193.212/TAF/images/TafTestlinkAdapterServer/TafTestlinkAdapterServerTafLogAsNotes.PNG "TAF test case log as Testlink test case notes")
*Testlink view of TAF test case log being populated as Testlink test case testing notes*

The test case execution log can be seen in Testlink, in the test case notes field.

![AdapterAboutPage](http://46.101.193.212/TAF/images/TafTestlinkAdapterServer/TafTestlinkAdapterServerAboutPageExample.PNG "TAF Testlink Adapter About page")
*The about page of TAF Testlink Adapter Server*

The web interface of the adapter is very limited, but views run status and general information. The REST interface is also quite simple, and only allows for posting of test run results from TAF.


## Schematic overview of TAF Testlink Adapter Server
![Schematic overview of TAF Testlink Adapter Server workings](http://46.101.193.212/TAF/images/TafTestlinkAdapterServer/TafTestlinkAdapterServerOverviewV2.png "Overview of how this adapter server works")

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
Five things are required to get this adapter working.
1. **Pre-requisites** Make sure you have a test automation in a TAF with a version of 2.5.24 or later. Make sure you have the host name or IP address of your Testlink installation.
2. Get hold of a **TafTestlinkAdapterServer.jar** file.
3. Make sure you have at least java version 1.8 installed.
4. Start your TafTestlinkAdapterServer.jar with appropriate parameters.
5. Modify your TAF test setup to include Testlink reporting.

All of those steps will be walked through below.

## 1. Check your TAF version
In your test automation project, open the `pom.xml` file and look for a section like
```pom
        <dependency>
            <groupId>com.github.claremontqualitymanagement</groupId>
            <artifactId>TestAutomationFramework</artifactId>
            <version>2.5.24</version>
        </dependency>
```

Make sure the version is at least 2.5.24.

If your are running tests from the command line interface the TAF version is also visible in HTML summary reports for test runs.


## 2. Achieve a jar
There are two ways of getting started with this adapter server.
1. Either clone this repository and build it to a jar file. Maven will do this for you if you have maven installed.
2. [Download](http://46.101.193.212/TAF/bin/TafTestlinkAdapterServer.jar "TAF Testlink Adapter Server jar file download") a readily built jar file and place it in a folder of your choice on your machine. The adapter server will produce a log file in the same directory.


## 3. Check your java version
Open a command prompt and write
```
java -version
```
When you press **enter** you'll be presented with the java version installed in your operating system. If you don't, either your java installation is not in your operating system path variable (this variable tells your operating system where to look for files when they are not in the current folder) or maybe you don't have java installed at all. Java is free of charge, and can be downloaded.

Make sure your java is at least version 1.8.

## 4. Getting the TAF Testlink Adapter Server started
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

## 5. Modifications on the TAF tests
You need to set the setting called `URL_TO_TESTLINK_ADAPTER` on your test execution. One way of doing this is from the command line when starting your TAF test run.

### 5a). Via command line interface at test run execution

     java -jar TafFull.jar MyTestClasses URL_TO_TESTLINK_ADAPTER=http://localhost:2222/taftestlinkadapter

### 5b). Programatically in your TAF tests
Another way is programatically, by adding the Testlink adapter, like in the example below.
```java
    @BeforeClass
    public static void setup(){
        TestRun.setSettingsValue(Settings.SettingParameters.URL_TO_TESTLINK_ADAPTER, 
               "http://127.0.0.1:2221/taftestlinkadapter");
   }
```

### 5c). Editing the `runSettings.properties` file
Test run settings can be set in a TAF run file. Setting the value of `URL_TO_TESTLINK_ADAPTER` in this file will engage the TafBackendTestRunReporter. 

Either way of updating the `URL_TO_TESTLINK_ADAPTER` settings value will engage the reporting to this adapter.

## More information
For more information, clone this repo, or [download the Javadocs](http://46.101.193.212/TAF/bin/TafTestlinkAdapterServer-javadoc.jar "Javadocs for TAF Testlink Adapter Server").

gl:hf



![ClaremontLogo](http://46.101.193.212/TAF/images/claremontlogo.gif "Claremont logo")
