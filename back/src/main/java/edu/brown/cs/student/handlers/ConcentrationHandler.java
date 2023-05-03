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

public final class ConcentrationHandler implements Route {

  public final List<Course> courses;
  public final List<Pathway> pathways;
  public final List<IntermediateGroup> intermediateGroups;
  public final List<List<String>> equivalenceGroups;
  public final Moshi moshi;

  public ConcentrationHandler(List<Course> courses, List<Pathway> pathways,
      List<IntermediateGroup> intermediateGroups, List<List<String>> equivalenceGroups) {
    this.courses = courses;
    this.pathways = pathways;
    this.intermediateGroups = intermediateGroups;
    this.equivalenceGroups = equivalenceGroups;
    this.moshi = new Moshi.Builder().build();
  }

  @Override
  public Object handle(Request request, Response response) {
    ScheduleRequest body;
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
    List<String> preferredCourses = body.preferred() == null ? List.of() : body.preferred();

    // Parse undesirable courses.
    List<String> undesirableCourses = body.undesirable() == null ? List.of() : body.undesirable();

    // Parse preferred pathways.
    List<String> preferredPathways =
        body.preferredPathways() == null ? List.of() : body.preferredPathways();

    // Parse partial assignment.
    List<Assignment> partialAssignment;
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
    final int minCoursesPerSemester = 2;
    final int maxCoursesPerSemester = 4;
    final int minTotalCourses = 16;
    final int minPathwaysCompleted = 2;
    final int minIntermediateCourses = 2;

    SolverParams params = new SolverParams(this.courses, partialAssignment, preferredCourses,
        undesirableCourses, this.pathways, preferredPathways, this.intermediateGroups,
        this.equivalenceGroups, minCoursesPerSemester, maxCoursesPerSemester, minTotalCourses,
        minPathwaysCompleted, minIntermediateCourses);

    ConcentrationSolver solver = new ConcentrationSolver(params);
    solver.buildConstraints();

    boolean hasSolution = solver.solve();

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
