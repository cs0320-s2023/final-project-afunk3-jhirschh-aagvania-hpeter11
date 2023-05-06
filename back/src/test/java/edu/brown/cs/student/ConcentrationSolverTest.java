package edu.brown.cs.student;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.ortools.Loader;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.data.Assignment;
import edu.brown.cs.student.data.Course;
import edu.brown.cs.student.data.IntermediateGroup;
import edu.brown.cs.student.data.Pathway;
import edu.brown.cs.student.data.Season;
import edu.brown.cs.student.data.Semester;
import edu.brown.cs.student.solver.ConcentrationSolver;
import edu.brown.cs.student.solver.SolverParams;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import org.junit.BeforeClass;
import org.junit.Test;


public class ConcentrationSolverTest {

  private static List<Course> courses;
  private static List<Pathway> pathways;
  private static List<IntermediateGroup> intermediateGroups;
  private static List<List<String>> equivalenceGroups;

  @BeforeClass
  public static void setup() throws IOException {
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
  }

  @Test
  public void solverFuzzTest() {
    List<Assignment> partialAssignment = new ArrayList<>(
        List.of(new Assignment(new Semester(2023, Season.Fall), List.of(), false),
            new Assignment(new Semester(2024, Season.Spring), List.of(), false),
            new Assignment(new Semester(2024, Season.Fall), List.of(), false),
            new Assignment(new Semester(2025, Season.Spring), List.of(), false),
            new Assignment(new Semester(2025, Season.Fall), List.of(), false),
            new Assignment(new Semester(2026, Season.Spring), List.of(), false),
            new Assignment(new Semester(2026, Season.Fall), List.of(), false),
            new Assignment(new Semester(2027, Season.Spring), List.of(), false)));

    List<String> preferredCourses = List.of("CSCI0190");
    List<String> undesirableCourses = List.of("APMA1650");
    List<String> preferredPathways = List.of("Systems");

    // Course load parameters.
    final int minCoursesPerSemester = 2;
    final int maxCoursesPerSemester = 4;
    final int minTotalCourses = 16;
    final int minPathwaysCompleted = 2;
    final int minIntermediateCourses = 2;

    for (int i = 0; i < 1000; i++) {
      SolverParams params = new SolverParams(courses, partialAssignment, preferredCourses,
          undesirableCourses, pathways, preferredPathways, intermediateGroups, equivalenceGroups,
          minCoursesPerSemester, maxCoursesPerSemester, minTotalCourses, minPathwaysCompleted,
          minIntermediateCourses);

      ConcentrationSolver solver = new ConcentrationSolver(params);
      solver.buildConstraints();

      // Check that the schedule exists.
      assertTrue(solver.solve());

      List<Assignment> schedule = solver.getSchedule();
      Map<String, List<String>> pathwayCourseAssignment = solver.getPathwayCourseAssignment();

      // Check course min/max boundaries.
      int totalCourses = 0;
      for (Assignment assignment : schedule) {
        assertTrue(assignment.courses().size() >= minCoursesPerSemester);
        assertTrue(assignment.courses().size() <= maxCoursesPerSemester);

        for (String course : assignment.courses()) {
          assertFalse(undesirableCourses.contains(course));
        }
        totalCourses += assignment.courses().size();
      }
      assertTrue(totalCourses >= minTotalCourses);
      assertTrue(pathwayCourseAssignment.size() >= minPathwaysCompleted);

      // Check undesirable courses.
      for (Assignment assignment : schedule) {
        for (String course : assignment.courses()) {
          assertFalse(undesirableCourses.contains(course));
        }
      }

      // Check preferred courses.
      for (String preferredCourse : preferredCourses) {
        boolean courseFound = false;
        for (Assignment assignment : schedule) {
          if (assignment.courses().contains(preferredCourse)) {
            courseFound = true;
            break;
          }
        }
        assertTrue(courseFound);
      }

      // Check preferred pathways.
      for (String preferredPathway : preferredPathways) {
        assertTrue(pathwayCourseAssignment.containsKey(preferredPathway));
      }

      // Check equivalence groups.
      for (List<String> equivalenceGroup : equivalenceGroups) {
        int equivalenceGroupCount = 0;
        for (Assignment assignment : schedule) {
          for (String course : assignment.courses()) {
            if (equivalenceGroup.contains(course)) {
              equivalenceGroupCount++;
            }
          }
        }
        assertTrue(equivalenceGroupCount <= 1);
      }

      // Check prerequisites.
      for (int assignmentIdx = 0; assignmentIdx < schedule.size(); assignmentIdx++) {
        for (String course : schedule.get(assignmentIdx).courses()) {
          Optional<Course> maybeCourse = courses.stream()
              .filter(possibleCourse -> Objects.equals(possibleCourse.courseCode(), course))
              .findFirst();
          assertTrue(maybeCourse.isPresent());
          List<List<String>> prerequisites = maybeCourse.get().prerequisites();
          List<String> previouslyTakenCourses = new ArrayList<>();
          for (int previousAssignmentIdx = 0; previousAssignmentIdx < assignmentIdx;
              previousAssignmentIdx++) {
            previouslyTakenCourses.addAll(schedule.get(previousAssignmentIdx).courses());
          }
          boolean satisfiesPrerequisites = true;
          for (List<String> clause : prerequisites) {
            boolean satisfiesClause = false;
            for (String literal : clause) {
              if (previouslyTakenCourses.contains(literal)) {
                satisfiesClause = true;
                break;
              }
            }
            satisfiesPrerequisites &= satisfiesClause;
          }
          assertTrue(satisfiesPrerequisites);
        }
      }

      // Check pathways.
      List<String> allCourses = new ArrayList<>();
      for (Assignment assignment : schedule) {
        allCourses.addAll(assignment.courses());
      }

      List<String> allPathwayCourses = new ArrayList<>();

      for (Entry<String, List<String>> assignment : pathwayCourseAssignment.entrySet()) {
        Optional<Pathway> maybePathway = pathways.stream()
            .filter(possiblePathway -> Objects.equals(possiblePathway.name(), assignment.getKey()))
            .findFirst();
        assertTrue(maybePathway.isPresent());
        Pathway pathway = maybePathway.get();

        // Check core and related courses.
        int coreCoursesTaken = 0;
        int relatedCoursesTaken = 0;

        for (String course : assignment.getValue()) {
          assertTrue(allCourses.contains(course));
          assertFalse(allPathwayCourses.contains(course));
          allPathwayCourses.add(course);
          if (pathway.coreCourses().contains(course)) {
            coreCoursesTaken++;
          } else if (pathway.relatedCourses().contains(course)) {
            relatedCoursesTaken++;
          }
        }
        assertTrue(coreCoursesTaken >= 1);
        assertTrue(coreCoursesTaken + relatedCoursesTaken >= 2);

        // Check intermediate courses.
        boolean satisfiesIntermediateCourses = true;
        for (List<String> clause : pathway.intermediateCourses()) {
          boolean satisfiesClause = false;
          for (String literal : clause) {
            if (allCourses.contains(literal)) {
              satisfiesClause = true;
              break;
            }
          }
          satisfiesIntermediateCourses &= satisfiesClause;
        }
        assertTrue(satisfiesIntermediateCourses);
      }

      // Check intermediate groups.
      int intermediateCourseCount = 0;
      for (IntermediateGroup intermediateGroup : intermediateGroups) {
        boolean satisfiesPrerequisites = true;
        for (List<String> clause : intermediateGroup.courses()) {
          boolean satisfiesClause = false;
          for (String literal : clause) {
            if (allCourses.contains(literal)) {
              satisfiesClause = true;
              intermediateCourseCount++;
              break;
            }
          }
          satisfiesPrerequisites &= satisfiesClause;
        }
        assertTrue(satisfiesPrerequisites);
      }

      assertTrue(intermediateCourseCount >= minIntermediateCourses);
    }
  }
}
