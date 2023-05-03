package edu.brown.cs.student.solver;

import com.google.ortools.sat.BoolVar;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.LinearExpr;
import com.google.ortools.sat.LinearExprBuilder;
import edu.brown.cs.student.data.Assignment;
import edu.brown.cs.student.data.Course;
import edu.brown.cs.student.data.IntermediateGroup;
import edu.brown.cs.student.data.Pathway;
import edu.brown.cs.student.data.Season;
import edu.brown.cs.student.data.Semester;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ConcentrationSolver {

  /**
   * Constraint programming model.
   */
  private final CpModel model;
  /**
   * Maps course code to the internal index.
   */
  private final Map<String, Integer> courseIdxMap;
  /**
   * Indicates whether a course is assigned to be taken during a semester.
   */
  private final BoolVar[][] courseSemesterMatrix;
  SolverParams params;
  /**
   * Indicates whether a given course was used to satisfy a given pathway.
   */
  private BoolVar[][] coursePathwayMatrix;
  private List<Assignment> schedule;
  private Map<String, List<String>> pathwayCourseAssignment;

  public ConcentrationSolver(SolverParams params) {
    // Instantiate all fields.
    this.params = params;

    this.model = new CpModel();
    this.courseIdxMap = new HashMap<>();

    // Create and initialize a course-semester boolean matrix.
    this.courseSemesterMatrix = new BoolVar[params.courses().size()][params.partialAssignment()
        .size()];
    for (int courseIdx = 0; courseIdx < params.courses().size(); courseIdx++) {
      for (int semesterIdx = 0; semesterIdx < params.partialAssignment().size(); semesterIdx++) {
        this.courseSemesterMatrix[courseIdx][semesterIdx] = this.model.newBoolVar(
            params.courses().get(courseIdx).courseCode() + " is taken in semester " + semesterIdx);
      }
      this.courseIdxMap.put(params.courses().get(courseIdx).courseCode(), courseIdx);
    }
  }

  public List<Assignment> getSchedule() {
    return schedule;
  }

  public Map<String, List<String>> getPathwayCourseAssignment() {
    return pathwayCourseAssignment;
  }

  public void buildConstraints() {
    // Satisfy partial assignment.
    for (int semesterIdx = 0; semesterIdx < this.params.partialAssignment().size(); semesterIdx++) {
      for (int assignedCourseIdx = 0;
          assignedCourseIdx < this.params.partialAssignment().get(semesterIdx).courses().size();
          assignedCourseIdx++) {
        int courseIdx = this.courseIdxMap.get(
            this.params.partialAssignment().get(semesterIdx).courses().get(assignedCourseIdx));
        this.model.addEquality(this.courseSemesterMatrix[courseIdx][semesterIdx], 1);
      }
    }

    // Enforce no more than 4 courses per semester.
    for (int semesterIdx = 0; semesterIdx < this.params.partialAssignment().size(); semesterIdx++) {
      LinearExprBuilder numCoursesInSemester = LinearExpr.newBuilder();
      for (int courseIdx = 0; courseIdx < this.params.courses().size(); courseIdx++) {
        numCoursesInSemester.add(this.courseSemesterMatrix[courseIdx][semesterIdx]);
      }
      this.model.addLessOrEqual(numCoursesInSemester, this.params.maxCoursesPerSemester());
      this.model.addGreaterOrEqual(numCoursesInSemester, this.params.minCoursesPerSemester());
    }

    // Disallow taking the same course several times.
    for (int courseIdx = 0; courseIdx < this.params.courses().size(); courseIdx++) {
      LinearExpr numTakenCourse = LinearExpr.sum(this.courseSemesterMatrix[courseIdx]);
      this.model.addLessOrEqual(numTakenCourse, 1);
    }

    // Enforce the prerequisites.
    for (Course course : this.params.courses()) {
      int courseIdx = this.courseIdxMap.get(course.courseCode());
      // If there are no prerequisites, there is nothing to enforce.
      if (course.prerequisites().size() == 0) {
        continue;
      }
      // Intermediate variable, indicates whether prerequisites are satisfied for the course.
      BoolVar satisfiesPrerequisites = this.satisfiesPrerequisitesConstraint(course);
      // Create a no-course-taken constraint to enforce if the prerequisites are not satisfied.
      LinearExpr numTakenCourse = LinearExpr.sum(this.courseSemesterMatrix[courseIdx]);
      this.model.addEquality(numTakenCourse, 0).onlyEnforceIf(satisfiesPrerequisites.not());
    }

    // Satisfy the offering semester.
    for (Course course : this.params.courses()) {
      int courseIdx = this.courseIdxMap.get(course.courseCode());
      for (int semesterIdx = 0; semesterIdx < this.params.partialAssignment().size();
          semesterIdx++) {
        if (this.params.partialAssignment().get(semesterIdx).semester().season() != course.season()
            && course.season() != Season.Both) {
          this.model.addEquality(this.courseSemesterMatrix[courseIdx][semesterIdx], 0);
        }
      }
    }

    // Satisfy the student preferences.
    for (String course : this.params.preferredCourses()) {
      int courseIdx = this.courseIdxMap.get(course);
      LinearExpr numTakenCourse = LinearExpr.sum(this.courseSemesterMatrix[courseIdx]);
      this.model.addEquality(numTakenCourse, 1);
    }

    // Satisfy the student preferences.
    for (String course : this.params.undesirableCourses()) {
      int courseIdx = this.courseIdxMap.get(course);
      LinearExpr numTakenCourse = LinearExpr.sum(this.courseSemesterMatrix[courseIdx]);
      this.model.addEquality(numTakenCourse, 0);
    }

    // Satisfy the pathways.
    BoolVar pathwaysSatisfied = this.satisfiesPathwaysConstraint();
    this.model.addEquality(pathwaysSatisfied, 1);

    // Satisfy the total number of courses.
    LinearExprBuilder totalCourses = LinearExpr.newBuilder();
    for (int i = 0; i < this.params.courses().size(); i++) {
      totalCourses.add(LinearExpr.sum(this.courseSemesterMatrix[i]));
    }
    this.model.addGreaterOrEqual(totalCourses, this.params.minTotalCourses());

    // Satisfy intermediate courses.
    BoolVar intermediateCoursesSatisfied = this.satisfiesIntermediateCoursesConstraint();
    this.model.addEquality(intermediateCoursesSatisfied, 1);

    // Satisfy equivalence groups.
    for (List<String> equivalenceGroup : this.params.equivalenceGroups()) {
      LinearExprBuilder equivalenceGroupSum = LinearExpr.newBuilder();
      for (String equivalenceCourse : equivalenceGroup) {
        int equivalenceCourseIdx = this.courseIdxMap.get(equivalenceCourse);
        equivalenceGroupSum.add(LinearExpr.sum(this.courseSemesterMatrix[equivalenceCourseIdx]));
      }
      this.model.addLessOrEqual(equivalenceGroupSum, 1);
    }
  }

  private BoolVar satisfiesPrerequisitesConstraint(Course course) {
    int courseIdx = this.courseIdxMap.get(course.courseCode());
    BoolVar satisfiesPrerequisites = this.model.newBoolVar(
        course.courseCode() + " satisfies all prerequisites");
    // Create a course weighted sum to check for prerequisites.
    LinearExprBuilder courseWeightedSum = LinearExpr.newBuilder();
    for (int semesterIdx = 0; semesterIdx < this.params.partialAssignment().size(); semesterIdx++) {
      courseWeightedSum.addTerm(this.courseSemesterMatrix[courseIdx][semesterIdx], semesterIdx + 1);
    }
    // Intermediate variables, indicate whether each of the prerequisite groups is satisfied.
    BoolVar[] clauseVars = new BoolVar[course.prerequisites().size()];
    // Look through every clause in the prerequisites array.
    for (int clauseIdx = 0; clauseIdx < course.prerequisites().size(); clauseIdx++) {
      // Current clause aka OR-group of prerequisites
      List<String> clause = course.prerequisites().get(clauseIdx);
      // Create a variable for the prerequisite group.
      clauseVars[clauseIdx] = this.model.newBoolVar(
          course.courseCode() + " satisfies prerequisite group " + clauseIdx);
      // Intermediate variables, indicate whether each individual prerequisite is satisfied.
      BoolVar[] literalVars = new BoolVar[clause.size()];
      // Look through every literal in the current clause.
      for (int literalIdx = 0; literalIdx < clause.size(); literalIdx++) {
        // Create a variable for the prerequisite.
        literalVars[literalIdx] = this.model.newBoolVar(
            course.courseCode() + " satisfies prerequisite " + clause.get(literalIdx));
        int prerequisiteIdx = this.courseIdxMap.get(clause.get(literalIdx));
        // Create a prerequisite weighted sum to check for prerequisite order.
        LinearExprBuilder prerequisiteWeightedSum = LinearExpr.newBuilder();
        // Create a prerequisite sum to check for prerequisite completion.
        LinearExprBuilder prerequisiteSum = LinearExpr.newBuilder();
        for (int semesterIdx = 0; semesterIdx < this.params.partialAssignment().size();
            semesterIdx++) {
          prerequisiteWeightedSum.addTerm(this.courseSemesterMatrix[prerequisiteIdx][semesterIdx],
              semesterIdx + 1);
          prerequisiteSum.add(this.courseSemesterMatrix[prerequisiteIdx][semesterIdx]);
        }
        // Intermediate variable, indicates whether the prerequisite was taken.
        BoolVar prerequisiteTaken = this.model.newBoolVar(
            course.courseCode() + " satisfies prerequisite (taken) " + clause.get(literalIdx));
        // Intermediate variable, indicates whether the prerequisite was taken before.
        BoolVar prerequisiteOrder = this.model.newBoolVar(
            course.courseCode() + " satisfies prerequisite (ordered) " + clause.get(literalIdx));
        // Sum constraint on the prerequisite
        this.model.addEquality(prerequisiteSum, 1).onlyEnforceIf(prerequisiteTaken);
        // Weighted sum constraint on the prerequisite.
        this.model.addLessThan(prerequisiteWeightedSum, courseWeightedSum)
            .onlyEnforceIf(prerequisiteOrder);
        // Combine the characteristics of the two variables into one.
        this.model.addBoolAnd(List.of(prerequisiteTaken, prerequisiteOrder))
            .onlyEnforceIf(literalVars[literalIdx]);
      }
      // Only enforce the OR-group if the clause is satisfied.
      this.model.addBoolOr(literalVars).onlyEnforceIf(clauseVars[clauseIdx]);
    }
    // Only enforce the AND-group if the whole course prerequisites are satisfied.
    this.model.addBoolAnd(clauseVars).onlyEnforceIf(satisfiesPrerequisites);
    return satisfiesPrerequisites;
  }

  private BoolVar satisfiesPathwaysConstraint() {
    // Indicates, whether each pathway is satisfied.
    BoolVar[] pathwaySatisfied = new BoolVar[this.params.pathways().size()];
    // Indicates whether a given course was used to satisfy a given pathway.
    this.coursePathwayMatrix = new BoolVar[this.params.courses().size()][this.params.pathways()
        .size()];

    // Instantiate variables.
    for (int i = 0; i < this.params.pathways().size(); i++) {
      pathwaySatisfied[i] = this.model.newBoolVar(
          "Pathway " + this.params.pathways().get(i).name() + " is satisfied.");
    }
    for (int i = 0; i < this.params.courses().size(); i++) {
      for (int j = 0; j < this.params.pathways().size(); j++) {
        this.coursePathwayMatrix[i][j] = this.model.newBoolVar(
            "Course " + this.params.courses().get(i).courseCode() + " is used to satisfy pathway "
                + this.params.pathways().get(j).name());
      }
    }

    // Satisfy preferred pathways.
    for (String pathwayName : this.params.preferredPathways()) {
      for (int i = 0; i < this.params.pathways().size(); i++) {
        if (Objects.equals(this.params.pathways().get(i).name(), pathwayName)) {
          this.model.addEquality(pathwaySatisfied[i], 1);
        }
      }
    }

    // Enforce taking at least the minimum number of pathways.
    for (int pathwayIdx = 0; pathwayIdx < this.params.pathways().size(); pathwayIdx++) {
      Pathway pathway = this.params.pathways().get(pathwayIdx);

      BoolVar pathwayCoreSatisfied = this.model.newBoolVar(
          "Core courses satisfied for pathway " + pathway.name());
      BoolVar pathwayAllSatisfied = this.model.newBoolVar(
          "All courses satisfied for pathway " + pathway.name());
      BoolVar pathwayIntermediateSatisfied = this.model.newBoolVar(
          "All courses satisfied for pathway " + pathway.name());

      // Total number of core and core & related courses taken.
      LinearExprBuilder coreCoursesRegularSum = LinearExpr.newBuilder();
      LinearExprBuilder allCoursesRegularSum = LinearExpr.newBuilder();

      for (String course : pathway.coreCourses()) {
        int coreCourseIdx = this.courseIdxMap.get(course);
        coreCoursesRegularSum.add(this.coursePathwayMatrix[coreCourseIdx][pathwayIdx]);
        allCoursesRegularSum.add(this.coursePathwayMatrix[coreCourseIdx][pathwayIdx]);
        // Can't count the course for the pathway if didn't take it in the first place.
        this.model.addGreaterOrEqual(LinearExpr.sum(this.courseSemesterMatrix[coreCourseIdx]),
            this.coursePathwayMatrix[coreCourseIdx][pathwayIdx]);
        // Can't count the same course into more than one pathway.
        this.model.addLessOrEqual(LinearExpr.sum(this.coursePathwayMatrix[coreCourseIdx]), 1);
      }

      for (String course : pathway.relatedCourses()) {
        int relatedCourseIdx = this.courseIdxMap.get(course);
        allCoursesRegularSum.add(this.coursePathwayMatrix[relatedCourseIdx][pathwayIdx]);
        // Can't count the course for the pathway if didn't take it in the first place.
        this.model.addGreaterOrEqual(LinearExpr.sum(this.courseSemesterMatrix[relatedCourseIdx]),
            this.coursePathwayMatrix[relatedCourseIdx][pathwayIdx]);
        // Can't count the same course into more than one pathway.
        this.model.addLessOrEqual(LinearExpr.sum(this.coursePathwayMatrix[relatedCourseIdx]), 1);
      }

      // Take at least 1 core course and at least 2 core + related courses per pathway.
      this.model.addGreaterOrEqual(coreCoursesRegularSum, 1).onlyEnforceIf(pathwayCoreSatisfied);
      this.model.addGreaterOrEqual(allCoursesRegularSum, 2).onlyEnforceIf(pathwayAllSatisfied);

      // Satisfy intermediate course groups.

      // Intermediate variables, indicate whether each of the intermediate course groups is satisfied.
      BoolVar[] clauseVars = new BoolVar[pathway.intermediateCourses().size()];
      // Look through every clause in the intermediate course array.
      for (int clauseIdx = 0; clauseIdx < pathway.intermediateCourses().size(); clauseIdx++) {
        // Current clause aka OR-group of intermediate courses.
        List<String> clause = pathway.intermediateCourses().get(clauseIdx);
        // Create a variable for the intermediate course group.
        clauseVars[clauseIdx] = this.model.newBoolVar(
            "Pathway " + pathway.name() + " satisfies intermediate course group " + clauseIdx);
        // Intermediate variables, indicate whether each individual intermediate course is satisfied.
        BoolVar[] literalVars = new BoolVar[clause.size()];
        // Look through every literal in the current clause.
        for (int literalIdx = 0; literalIdx < clause.size(); literalIdx++) {
          // Create a variable for the intermediate course.
          literalVars[literalIdx] = this.model.newBoolVar(
              "Pathway " + pathway.name() + " satisfies prerequisite " + clause.get(literalIdx));
          int prerequisiteIdx = this.courseIdxMap.get(clause.get(literalIdx));
          // Create an intermediate course sum to check for intermediate course completion.
          LinearExpr prerequisiteSum = LinearExpr.sum(this.courseSemesterMatrix[prerequisiteIdx]);
          // Sum constraint on the intermediate course.
          this.model.addEquality(prerequisiteSum, 1).onlyEnforceIf(literalVars[literalIdx]);
        }
        // Only enforce the OR-group if the clause is satisfied.
        this.model.addBoolOr(literalVars).onlyEnforceIf(clauseVars[clauseIdx]);
      }
      // Only enforce the AND-group if the whole pathway's intermediate courses are satisfied.
      this.model.addBoolAnd(clauseVars).onlyEnforceIf(pathwayIntermediateSatisfied);

      // Pathway is satisfied if all three sub-conditions are met per pathway.
      this.model.addBoolAnd(
              List.of(pathwayCoreSatisfied, pathwayAllSatisfied, pathwayIntermediateSatisfied))
          .onlyEnforceIf(pathwaySatisfied[pathwayIdx]);
    }

    BoolVar pathwaysSatisfied = this.model.newBoolVar("Required number of pathways is satisfied");
    this.model.addGreaterOrEqual(LinearExpr.sum(pathwaySatisfied),
        this.params.minPathwaysCompleted()).onlyEnforceIf(pathwaysSatisfied);

    return pathwaysSatisfied;
  }

  private BoolVar satisfiesIntermediateCoursesConstraint() {
    BoolVar intermediateCoursesSatisfied = this.model.newBoolVar(
        "All intermediate courses are satisfied");
    // The total number of intermediate courses taken.
    LinearExprBuilder totalIntermediateCourses = LinearExpr.newBuilder();
    // Indicates, if an intermediate group is satisfied.
    BoolVar[] intermediateGroupsSatisfied = new BoolVar[this.params.intermediateGroups().size()];

    for (int intermediateGroupIdx = 0;
        intermediateGroupIdx < this.params.intermediateGroups().size(); intermediateGroupIdx++) {
      IntermediateGroup intermediateGroup = this.params.intermediateGroups()
          .get(intermediateGroupIdx);
      intermediateGroupsSatisfied[intermediateGroupIdx] = this.model.newBoolVar(
          "Intermediate group " + intermediateGroup.name() + " is satisfied");

      // Intermediate variables, indicate whether each of the intermediate course groups is satisfied.
      BoolVar[] clauseVars = new BoolVar[intermediateGroup.courses().size()];
      // Look through every clause in the intermediate course group array.
      for (int clauseIdx = 0; clauseIdx < intermediateGroup.courses().size(); clauseIdx++) {
        // Current clause aka OR-group of intermediate courses.
        List<String> clause = intermediateGroup.courses().get(clauseIdx);
        // Create a variable for the intermediate course group.
        clauseVars[clauseIdx] = this.model.newBoolVar(
            "Intermediate group " + intermediateGroup.name()
                + " satisfies intermediate course group " + clauseIdx);
        // Intermediate variables, indicate whether each intermediate course is satisfied.
        BoolVar[] literalVars = new BoolVar[clause.size()];
        // Look through every literal in the current clause.
        for (int literalIdx = 0; literalIdx < clause.size(); literalIdx++) {
          // Create a variable for the intermediate course.
          literalVars[literalIdx] = this.model.newBoolVar(
              "Intermediate group " + intermediateGroup.name() + " satisfies prerequisite "
                  + clause.get(literalIdx));
          int prerequisiteIdx = this.courseIdxMap.get(clause.get(literalIdx));
          // Create an intermediate course sum to check for intermediate course completion.
          LinearExpr prerequisiteSum = LinearExpr.sum(this.courseSemesterMatrix[prerequisiteIdx]);
          // Sum constraint on the intermediate course.
          this.model.addEquality(prerequisiteSum, 1).onlyEnforceIf(literalVars[literalIdx]);
        }
        // Only enforce the OR-group if the clause is satisfied.
        this.model.addBoolOr(literalVars).onlyEnforceIf(clauseVars[clauseIdx]);
        totalIntermediateCourses.add(clauseVars[clauseIdx]);
      }
      // Only enforce the AND-group if the whole pathway's intermediate courses are satisfied.
      this.model.addGreaterOrEqual(LinearExpr.sum(clauseVars), 1)
          .onlyEnforceIf(intermediateGroupsSatisfied[intermediateGroupIdx]);
    }

    BoolVar totalIntermediateCoursesSatisfied = this.model.newBoolVar(
        "Total number of intermediate courses has been satisfied");
    BoolVar allIntermediateGroupsSatisfied = this.model.newBoolVar(
        "All intermediate groups have been satisfied");

    // Enforce the number of intermediate courses and intermediate group completion.
    this.model.addGreaterOrEqual(totalIntermediateCourses, this.params.minIntermediateCourses())
        .onlyEnforceIf(totalIntermediateCoursesSatisfied);
    this.model.addBoolAnd(intermediateGroupsSatisfied)
        .onlyEnforceIf(allIntermediateGroupsSatisfied);

    // Merge two constraints into one.
    this.model.addBoolAnd(
            List.of(totalIntermediateCoursesSatisfied, allIntermediateGroupsSatisfied))
        .onlyEnforceIf(intermediateCoursesSatisfied);
    return intermediateCoursesSatisfied;
  }

  public boolean solve() {
    // Create a solver.
    CpSolver solver = new CpSolver();
    // Randomize search.
    solver.getParameters().setRandomizeSearch(true);
    // And solve.
    CpSolverStatus status = solver.solve(this.model);

    if (status != CpSolverStatus.OPTIMAL && status != CpSolverStatus.FEASIBLE) {
      return false;
    }

    // Retrieve the results.
    this.schedule = new ArrayList<>();
    for (int semesterIdx = 0; semesterIdx < this.params.partialAssignment().size(); semesterIdx++) {
      List<String> semesterCourses = new ArrayList<>();
      for (int courseIdx = 0; courseIdx < this.params.courses().size(); courseIdx++) {
        if (solver.value(this.courseSemesterMatrix[courseIdx][semesterIdx]) == 1) {
          semesterCourses.add(this.params.courses().get(courseIdx).courseCode());
        }
      }
      Semester currentSemester = this.params.partialAssignment().get(semesterIdx).semester();
      schedule.add(new Assignment(currentSemester, semesterCourses));
    }

    this.pathwayCourseAssignment = new HashMap<>();
    for (int pathwayIdx = 0; pathwayIdx < this.params.pathways().size(); pathwayIdx++) {
      List<String> pathwayCourses = new ArrayList<>();
      for (int courseIdx = 0; courseIdx < this.params.courses().size(); courseIdx++) {
        if (solver.value(this.coursePathwayMatrix[courseIdx][pathwayIdx]) == 1) {
          pathwayCourses.add(this.params.courses().get(courseIdx).courseCode());
        }
      }
      if (pathwayCourses.size() > 0) {
        this.pathwayCourseAssignment.put(this.params.pathways().get(pathwayIdx).name(),
            pathwayCourses);
      }
    }
    return true;
  }
}
