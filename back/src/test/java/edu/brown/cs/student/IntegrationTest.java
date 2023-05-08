package edu.brown.cs.student;

import static org.testng.AssertJUnit.assertEquals;
import static spark.Spark.after;

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
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import spark.Spark;

public class IntegrationTest {

  @BeforeClass
  public static void serverSetup() throws IOException {
    // This can be performed only once per server instantiation.
    Loader.loadNativeLibraries();

    // Load all available courses into memory.
    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<List<Course>> courseSerializer = moshi.adapter(
        Types.newParameterizedType(List.class, Course.class));
    List<Course> courses = courseSerializer.fromJson(
        Files.readString(Paths.get("data/courses.json")));

    // Load all available pathways into memory.
    JsonAdapter<List<Pathway>> pathwaySerializer = moshi.adapter(
        Types.newParameterizedType(List.class, Pathway.class));
    List<Pathway> pathways = pathwaySerializer.fromJson(
        Files.readString(Paths.get("data/pathways.json")));

    // Load all available intermediate groups into memory.
    JsonAdapter<List<IntermediateGroup>> intermediateGroupSerializer = moshi.adapter(
        Types.newParameterizedType(List.class, IntermediateGroup.class));
    List<IntermediateGroup> intermediateGroups = intermediateGroupSerializer.fromJson(
        Files.readString(Paths.get("data/intermediateGroups.json")));

    // Load all courses that cannot be taken together into memory.
    JsonAdapter<List<List<String>>> equivalenceGroupSerializer = moshi.adapter(
        Types.newParameterizedType(List.class,
            Types.newParameterizedType(List.class, String.class)));
    List<List<String>> equivalenceGroups = equivalenceGroupSerializer.fromJson(
        Files.readString(Paths.get("data/equivalenceGroups.json")));

    Spark.port(3232);

    after((request, response) -> {
      response.header("Access-Control-Allow-Origin", "*");
      response.header("Access-Control-Allow-Methods", "*");
      response.header("Content-Type", "application/json");
    });

    // Main solver endpoint.
    Spark.post("schedule",
        new ConcentrationHandler(courses, pathways, intermediateGroups, equivalenceGroups));
    Spark.init();

    Spark.awaitInitialization();
    System.out.println("Server started.");
  }

  static private HttpURLConnection tryRequest(String json) throws IOException {
    // Configure the connection (but don't actually send the request yet)
    URL requestURL = new URL("http://localhost:3232/schedule");
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();

    //Specify request type as POST
    clientConnection.setRequestMethod("POST");

    //Specify request body as Json
    clientConnection.setRequestProperty("Content-Type", "application/json");

    //enable output for POST request
    clientConnection.setDoOutput(true);

    OutputStreamWriter writer = new OutputStreamWriter(clientConnection.getOutputStream());

    //changes input to bytes
    writer.write(json);
    writer.flush();

    clientConnection.connect();
    return clientConnection;
  }

  static private <T> T getResponse(HttpURLConnection clientConnection, Class<T> customClass)
      throws IOException {
    Moshi moshi = new Moshi.Builder().build();
    return moshi.adapter(customClass)
        .fromJson(new okio.Buffer().readFrom(clientConnection.getInputStream()));
  }

  @Before
  public void setup() {
    // Re-initialize state, etc. for _every_ test method run
    Spark.init();
    Spark.awaitInitialization();

    Spark.init();
    Spark.awaitInitialization(); // don't continue until the server is listening
  }

//  @After
//  public void teardown() {
//    // Gracefully stop Spark listening on both endpoints
//    Spark.unmap("/schedule");
//
//    Spark.awaitStop(); // don't proceed until the server is stopped
//  }

  public String ReadJsonAsString(String filename) throws IOException {
    String jsonString = new String(Files.readAllBytes(Paths.get(filename)));

    //Using Jackson library to implement stringify
    ObjectMapper objectMapper = new ObjectMapper();

    //Bundles json into generic container of elements
    JsonNode jsonNode = objectMapper.readTree(jsonString);

    //returns string version
    String json = objectMapper.writeValueAsString(jsonNode);

    return json;
  }

  @Test
  public void testReturnsResponse() throws IOException {
    HttpURLConnection clientConnection = tryRequest("not a json");
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
    ScheduleFailureResponse response = getResponse(clientConnection, ScheduleFailureResponse.class);

    assertEquals("error", response.result());
    assertEquals("JSON parsing error.", response.message());

    clientConnection.disconnect();
  }

  @Test
  public void testDoesParse() throws IOException {
    HttpURLConnection clientConnection = tryRequest(ReadJsonAsString("src/test/java/edu/brown/cs/student/mockJSON/MockSuccess.json"));
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
    ScheduleFailureResponse response = getResponse(clientConnection, ScheduleFailureResponse.class);

    assertEquals("success", response.result());

    clientConnection.disconnect();
  }

  @Test
  public void testDoesNotParse() throws IOException {
    HttpURLConnection clientConnection = tryRequest(ReadJsonAsString("src/test/java/edu/brown/cs/student/mockJSON/MockPartialFailure.json"));
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
    ScheduleFailureResponse response = getResponse(clientConnection, ScheduleFailureResponse.class);

    assertEquals("error", response.result());
    assertEquals("Partial assignment is empty.", response.message());

    clientConnection.disconnect();
  }

  @Test
  public void testCourseEmpty() throws IOException {
    HttpURLConnection clientConnection = tryRequest(ReadJsonAsString("src/test/java/edu/brown/cs/student/mockJSON/MockCourses.json"));
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
    ScheduleFailureResponse response = getResponse(clientConnection, ScheduleFailureResponse.class);

    assertEquals("error", response.result());
    assertEquals("Missing courses field of some assignment.", response.message());

    clientConnection.disconnect();
  }

  @Test
  public void testFrozenEmpty() throws IOException {
    HttpURLConnection clientConnection = tryRequest(ReadJsonAsString("src/test/java/edu/brown/cs/student/mockJSON/MockFrozen.json"));
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
    ScheduleFailureResponse response = getResponse(clientConnection, ScheduleFailureResponse.class);

    assertEquals("error", response.result());
    assertEquals("Missing frozen field of some assignment.", response.message());

    clientConnection.disconnect();
  }

  @Test
  public void testSeasonEmpty() throws IOException {
    HttpURLConnection clientConnection = tryRequest(ReadJsonAsString("src/test/java/edu/brown/cs/student/mockJSON/MockSeason.json"));
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
    ScheduleFailureResponse response = getResponse(clientConnection, ScheduleFailureResponse.class);

    assertEquals("error", response.result());
    assertEquals("Missing season field of some assignment.", response.message());

    clientConnection.disconnect();
  }

  @Test
  public void testSemesterEmpty() throws IOException {
    HttpURLConnection clientConnection = tryRequest(ReadJsonAsString("src/test/java/edu/brown/cs/student/mockJSON/MockSemester.json"));
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
    ScheduleFailureResponse response = getResponse(clientConnection, ScheduleFailureResponse.class);

    assertEquals("error", response.result());
    assertEquals("Missing semester field of some assignment.", response.message());

    clientConnection.disconnect();
  }

  @Test
  public void testYearEmpty() throws IOException {
    HttpURLConnection clientConnection = tryRequest(ReadJsonAsString("src/test/java/edu/brown/cs/student/mockJSON/MockYear.json"));
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
    ScheduleFailureResponse response = getResponse(clientConnection, ScheduleFailureResponse.class);

    assertEquals("error", response.result());
    assertEquals("Missing year field of some assignment.", response.message());

    clientConnection.disconnect();
  }

  @Test
  public void testFakeCourse() throws IOException {
    HttpURLConnection clientConnection = tryRequest(ReadJsonAsString("src/test/java/edu/brown/cs/student/mockJSON/MockFakeCourse.json"));
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
    ScheduleFailureResponse response = getResponse(clientConnection, ScheduleFailureResponse.class);

    assertEquals("error", response.result());
    assertEquals("Unknown course provided to the solver.", response.message());

    clientConnection.disconnect();
  }
}
