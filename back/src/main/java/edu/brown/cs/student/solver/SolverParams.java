package edu.brown.cs.student.solver;

import edu.brown.cs.student.data.Assignment;
import edu.brown.cs.student.data.Course;
import edu.brown.cs.student.data.IntermediateGroup;
import edu.brown.cs.student.data.Pathway;
import java.util.Comparator;
import java.util.List;

/**
 * Parameters to the concentration solver.
 */
public record SolverParams(List<Course> courses, List<Assignment> partialAssignment,
                           List<String> preferredCourses, List<String> undesirableCourses,

                           List<Pathway> pathways, List<String> preferredPathways,
                           List<IntermediateGroup> intermediateGroups,
                           List<List<String>> equivalenceGroups, int minCoursesPerSemester,
                           int maxCoursesPerSemester, int minTotalCourses, int minPathwaysCompleted,
                           int minIntermediateCourses) {

  /**
   * Sorts the courses from a partial assignment.
   */
  public SolverParams {
    partialAssignment.sort(Comparator.comparing(Assignment::semester));
  }
}
