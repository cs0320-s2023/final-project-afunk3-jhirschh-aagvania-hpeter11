package edu.brown.cs.student;


import static spark.Spark.after;

import com.google.ortools.Loader;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.data.Course;
import edu.brown.cs.student.data.IntermediateGroup;
import edu.brown.cs.student.data.Pathway;
import edu.brown.cs.student.handlers.ConcentrationHandler;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import spark.Spark;

public class Main {

  public static void main(String[] args) throws IOException {
    // This can be performed only once per server instantiation.
    Loader.loadNativeLibraries();

    // All schedule available.
    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<List<Course>> courseSerializer = moshi.adapter(
        Types.newParameterizedType(List.class, Course.class));
    List<Course> courses = courseSerializer.fromJson(
        Files.readString(Paths.get("data/courses.json")));

    // All pathways available.
    JsonAdapter<List<Pathway>> pathwaySerializer = moshi.adapter(
        Types.newParameterizedType(List.class, Pathway.class));
    List<Pathway> pathways = pathwaySerializer.fromJson(
        Files.readString(Paths.get("data/pathways.json")));

    // All intermediate groups available.
    JsonAdapter<List<IntermediateGroup>> intermediateGroupSerializer = moshi.adapter(
        Types.newParameterizedType(List.class, IntermediateGroup.class));
    List<IntermediateGroup> intermediateGroups = intermediateGroupSerializer.fromJson(
        Files.readString(Paths.get("data/intermediateGroups.json")));

    // Courses that cannot be taken together.
    JsonAdapter<List<List<String>>> equivalenceGroupSerializer = moshi.adapter(
        Types.newParameterizedType(List.class,
            Types.newParameterizedType(List.class, String.class)));
    List<List<String>> equivalenceGroups = equivalenceGroupSerializer.fromJson(
        Files.readString(Paths.get("data/equivalenceGroups.json")));

    Spark.port(3232);
    /*
       Setting CORS headers to allow cross-origin requests from the client; this is necessary for the client to
       be able to make requests to the server.
       By setting the Access-Control-Allow-Origin header to "*", we allow requests from any origin.
       This is not a good idea in real-world applications, since it opens up your server to cross-origin requests
       from any website. Instead, you should set this header to the origin of your client, or a list of origins
       that you trust.
       By setting the Access-Control-Allow-Methods header to "*", we allow requests with any HTTP method.
       Again, it's generally better to be more specific here and only allow the methods you need, but for
       this demo we'll allow all methods.
       We recommend you learn more about CORS with these resources:
           - https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS
           - https://portswigger.net/web-security/cors
    */
    after((request, response) -> {
      response.header("Access-Control-Allow-Origin", "*");
      response.header("Access-Control-Allow-Methods", "*");
      response.header("Content-Type", "application/json");
    });

    Spark.get("schedule",
        new ConcentrationHandler(courses, pathways, intermediateGroups, equivalenceGroups));
    Spark.init();
    Spark.awaitInitialization();
    System.out.println("Server started.");
  }
}
