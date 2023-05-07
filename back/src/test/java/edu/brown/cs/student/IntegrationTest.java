package edu.brown.cs.student;


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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.testng.AssertJUnit.assertEquals;

public class IntegrationTest {

    private static List<Course> courses;
    private static List<Pathway> pathways;
    private static List<IntermediateGroup> intermediateGroups;
    private static List<List<String>> equivalenceGroups;

    @BeforeAll
    public static void setup_before_everything() throws IOException {

        // This can be performed only once per server instantiation.
        Loader.loadNativeLibraries();

        // All courses available.
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<List<Course>> courseSerializer = moshi.adapter(
                Types.newParameterizedType(List.class, Course.class));
        courses = courseSerializer.fromJson(Files.readString(Paths.get("data/courses.json")));

        // All pathways available.
        JsonAdapter<List<Pathway>> pathwaySerializer = moshi.adapter(
                Types.newParameterizedType(List.class, Pathway.class));
        pathways = pathwaySerializer.fromJson(Files.readString(Paths.get("data/pathways.json")));

        // All intermediate groups available.
        JsonAdapter<List<IntermediateGroup>> intermediateGroupSerializer = moshi.adapter(
                Types.newParameterizedType(List.class, IntermediateGroup.class));
        intermediateGroups = intermediateGroupSerializer.fromJson(
                Files.readString(Paths.get("data/intermediateGroups.json")));

        // Courses that cannot be taken together.
        JsonAdapter<List<List<String>>> equivalenceGroupSerializer = moshi.adapter(
                Types.newParameterizedType(List.class,
                        Types.newParameterizedType(List.class, String.class)));
        equivalenceGroups = equivalenceGroupSerializer.fromJson(
                Files.readString(Paths.get("data/equivalenceGroups.json")));

        Spark.port(3232);
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
        URL requestURL = new URL("http://localhost:"+ Spark.port()+"/schedule?" + apiCall);
        HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();

        // The default method is "GET", which is what we're using here.
        // If we were using "POST", we'd need to say so.
        //clientConnection.setRequestMethod("GET");

        clientConnection.connect();
        return clientConnection;
    }

    static private <T> T getResponse(HttpURLConnection clientConnection, Class<T> customClass) throws IOException {
        Moshi moshi = new Moshi.Builder().build();
        return moshi.adapter(customClass).fromJson(new okio.Buffer().readFrom(clientConnection.getInputStream()));
    }

    @Test
    public void testReturnsResponse() throws IOException {
        HttpURLConnection clientConnection = tryRequest("test");
        // Get an OK response (the *connection* worked, the *API* provides an error response)
        assertEquals(200, clientConnection.getResponseCode());

        Moshi moshi = new Moshi.Builder().build();
        ScheduleFailureResponse response = moshi.adapter(ScheduleFailureResponse.class).fromJson(new okio.Buffer().readFrom(clientConnection.getInputStream()));
        assertEquals("error", response.result());
        assertEquals("JSON parsing error.", response.message());

        clientConnection.disconnect();
    }


}
