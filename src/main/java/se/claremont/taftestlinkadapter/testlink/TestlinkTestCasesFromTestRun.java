package se.claremont.taftestlinkadapter.testlink;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;

/**
 * A list of all test cases from a test run
 *
 * Created by jordam on 2017-03-26.
 */
public class TestlinkTestCasesFromTestRun {
    @JsonProperty public ArrayList<TestlinkTestCaseMapper> testCases = new ArrayList<>();

    public TestlinkTestCasesFromTestRun(){}

    public String toJson(){
        ObjectMapper mapper = new ObjectMapper();
        String json = null;
        try {
            json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            System.out.println(e.toString());
        }
        return json;
    }
}
