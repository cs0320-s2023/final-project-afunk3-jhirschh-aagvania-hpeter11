package edu.brown.cs.student.handlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import edu.brown.cs.student.data.Assignment;
import edu.brown.cs.student.data.Course;
import edu.brown.cs.student.data.IntermediateGroup;
import edu.brown.cs.student.data.Pathway;
import edu.brown.cs.student.solver.ConcentrationSolver;
import edu.brown.cs.student.solver.SolverParams;
import java.util.List;
import java.util.Objects;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * API handler for the concentration solver.
 */
public final class ConcentrationHandler implements Route {

  /**
   * List of available courses.
   */
  public final List<Course> courses;
  /**
   * List of available pathways.
   */
  public final List<Pathway> pathways;
  /**
   * List of available intermediate groups.
   */
  public final List<IntermediateGroup> intermediateGroups;
  /**
   * List of available equivalence groups.
   */
  public final List<List<String>> equivalenceGroups;
  /**
   * Serializer object.
   */
  public final Moshi moshi;

  /**
   * Constructs a handler object.
   */
  public ConcentrationHandler(List<Course> courses, List<Pathway> pathways,
      List<IntermediateGroup> intermediateGroups, List<List<String>> equivalenceGroups) {
    this.courses = courses;
    this.pathways = pathways;
    this.intermediateGroups = intermediateGroups;
    this.equivalenceGroups = equivalenceGroups;
    this.moshi = new Moshi.Builder().build();
  }

  /**
   * Handles the request.
   */
  @Override
  public Object handle(Request request, Response response) {
    ScheduleRequest body;
    // Try parsing JSON request.
    try {
      JsonAdapter<ScheduleRequest> serializer = this.moshi.adapter(ScheduleRequest.class);
      body = serializer.fromJson(request.body());
    } catch (Exception e) {
      ScheduleFailureResponse failureResponse = new ScheduleFailureResponse("JSON parsing error.");
      JsonAdapter<ScheduleFailureResponse> serializer = this.moshi.adapter(
          ScheduleFailureResponse.class);
      return serializer.toJson(failureResponse);
    }

    assert body != null;

    // Parse preferred courses.
    final List<String> preferredCourses = body.preferred() == null ? List.of() : body.preferred();

    // Parse undesirable courses.
    final List<String> undesirableCourses =
        body.undesirable() == null ? List.of() : body.undesirable();

    // Parse preferred pathways.
    final List<String> preferredPathways =
        body.preferredPathways() == null ? List.of() : body.preferredPathways();

    // Parse partial assignment.
    final List<Assignment> partialAssignment;
    try {
      if (body.partialAssignment() == null || body.partialAssignment().size() == 0) {
        throw new IllegalArgumentException("Partial assignment is empty.");
      } else {
        for (Assignment assignment : body.partialAssignment()) {
          if (assignment.semester() == null) {
            throw new IllegalArgumentException("Missing semester field of some assignment.");
          } else if (assignment.courses() == null) {
            throw new IllegalArgumentException("Missing courses field of some assignment.");
          } else if (assignment.semester().year() == null) {
            throw new IllegalArgumentException("Missing year field of some assignment.");
          } else if (assignment.semester().season() == null) {
            throw new IllegalArgumentException("Missing season field of some assignment.");
          } else {
            // Check that no extraneous courses are present.
            for (String assignedCourse : assignment.courses()) {
              if (this.courses.stream()
                  .noneMatch((course) -> Objects.equals(course.courseCode(), assignedCourse))) {
                throw new IllegalArgumentException("Unknown course provided to the solver.");
              }
            }
          }
        }
      }
    } catch (IllegalArgumentException e) {
      ScheduleFailureResponse failureResponse = new ScheduleFailureResponse(e.getMessage());
      JsonAdapter<ScheduleFailureResponse> serializer = this.moshi.adapter(
          ScheduleFailureResponse.class);
      return serializer.toJson(failureResponse);
    }
    partialAssignment = body.partialAssignment();

    // Check that no extraneous courses are present.
    for (String preferredCourse : preferredCourses) {
      if (this.courses.stream()
          .noneMatch((course) -> Objects.equals(course.courseCode(), preferredCourse))) {
        ScheduleFailureResponse failureResponse = new ScheduleFailureResponse(
            "Preferred courses are incorrectly formatted.");
        JsonAdapter<ScheduleFailureResponse> serializer = this.moshi.adapter(
            ScheduleFailureResponse.class);
        return serializer.toJson(failureResponse);
      }
    }

    // Check that no extraneous courses are present.
    for (String undesirableCourse : undesirableCourses) {
      if (this.courses.stream()
          .noneMatch((course) -> Objects.equals(course.courseCode(), undesirableCourse))) {
        ScheduleFailureResponse failureResponse = new ScheduleFailureResponse(
            "Undesirable courses are incorrectly formatted.");
        JsonAdapter<ScheduleFailureResponse> serializer = this.moshi.adapter(
            ScheduleFailureResponse.class);
        return serializer.toJson(failureResponse);
      }
    }

    // Course load parameters.
    final int minCoursesPerSemester = 0;
    final int maxCoursesPerSemester = 5;
    final int minTotalCourses = 16;
    final int minPathwaysCompleted = 2;
    final int minIntermediateCourses = 2;

    // Instantiate solver parameters.
    SolverParams params = new SolverParams(this.courses, partialAssignment, preferredCourses,
        undesirableCourses, this.pathways, preferredPathways, this.intermediateGroups,
        this.equivalenceGroups, minCoursesPerSemester, maxCoursesPerSemester, minTotalCourses,
        minPathwaysCompleted, minIntermediateCourses);

    // Instantiate solver.
    ConcentrationSolver solver = new ConcentrationSolver(params);
    solver.buildConstraints();

    // And solve.
    boolean hasSolution = solver.solve();

    // Serialize solution or failure response.
    if (!hasSolution) {
      ScheduleFailureResponse failureResponse = new ScheduleFailureResponse("No solution.");
      JsonAdapter<ScheduleFailureResponse> serializer = this.moshi.adapter(
          ScheduleFailureResponse.class);
      return serializer.toJson(failureResponse);
    } else {
      ScheduleSuccessResponse successResponse = new ScheduleSuccessResponse(solver.getSchedule(),
          solver.getPathwayCourseAssignment());
      JsonAdapter<ScheduleSuccessResponse> serializer = this.moshi.adapter(
          ScheduleSuccessResponse.class);
      return serializer.toJson(successResponse);
    }
  }

}
