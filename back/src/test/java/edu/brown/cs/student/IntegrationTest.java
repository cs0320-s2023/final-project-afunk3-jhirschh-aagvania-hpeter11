package edu.brown.cs.student;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.ortools.Loader;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.data.Course;
import edu.brown.cs.student.data.IntermediateGroup;
import edu.brown.cs.student.data.Pathway;
import edu.brown.cs.student.handlers.ConcentrationHandler;
import edu.brown.cs.student.handlers.ScheduleFailureResponse;
import org.junit.BeforeClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.testng.AssertJUnit.assertEquals;

public class IntegrationTest {

    @BeforeAll
    public static void setup_before_everything() throws IOException {

        Spark.port(0);
        Logger.getLogger("").setLevel(Level.WARNING); // empty name = root logger
    }


    @BeforeEach
    public void setup() {
        // Re-initialize state, etc. for _every_ test method run
        Spark.init();
        Spark.awaitInitialization();

        Spark.init();
        Spark.awaitInitialization(); // don't continue until the server is listening
    }

    @AfterEach
    public void teardown() {
        // Gracefully stop Spark listening on both endpoints
        Spark.unmap("/schedule");

        Spark.awaitStop(); // don't proceed until the server is stopped
    }

    static private HttpURLConnection tryRequest(String apiCall) throws IOException {
        // Configure the connection (but don't actually send the request yet)
        URL requestURL = new URL("http://localhost:" + Spark.port() + "/schedule");
        HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();

        //Specify request type as POST
        clientConnection.setRequestMethod("POST");

        //Specify request body as Json
        clientConnection.setRequestProperty("Content-Type", "application/json");

        //enable output for POST request
        clientConnection.setDoOutput(true);


        OutputStreamWriter writer = new OutputStreamWriter(clientConnection.getOutputStream());

        //changes input to bytes
        writer.write(apiCall);
        writer.flush();

        clientConnection.connect();
        return clientConnection;
    }

    static private <T> T getResponse(HttpURLConnection clientConnection, Class<T> customClass) throws IOException {
        Moshi moshi = new Moshi.Builder().build();
        return moshi.adapter(customClass).fromJson(new okio.Buffer().readFrom(clientConnection.getInputStream()));
    }

    public String ReadJsonAsString(String filename) throws IOException {
        String jsonString = new String(Files.readAllBytes(Paths.get(filename)));

        //Using Jackson library to implement stringify
        ObjectMapper objectMapper = new ObjectMapper();

        //Bundles json into generic container of elements
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        //returns string version
        String json = objectMapper.writeValueAsString(jsonNode);

        System.out.println(json);
        return json;
        }

    @Test
    public void testReturnsResponse() throws IOException {
        //HttpURLConnection clientConnection = tryRequest(ReadJsonAsString("src/test/java/edu/brown/cs/student/mockJSON/MockFailure.json"));
        HttpURLConnection clientConnection = tryRequest(ReadJsonAsString("/Users/scottpetersen/Desktop/cs32/final-project-afunk3-jhirschh-aagvania-hpeter11/back/src/test/java/edu/brown/cs/student/mockJSON/MockFailure.json"));
        // Get an OK response (the *connection* worked, the *API* provides an error response)
        assertEquals(200, clientConnection.getResponseCode());

        Moshi moshi = new Moshi.Builder().build();
        ScheduleFailureResponse response = moshi.adapter(ScheduleFailureResponse.class).fromJson(new okio.Buffer().readFrom(clientConnection.getInputStream()));
        assertEquals("error", response.result());
        assertEquals("JSON parsing error.", response.message());

        clientConnection.disconnect();
    }


}
