package edu.brown.cs.student.data;

import java.util.List;

/**
 * Course-semester assignment.
 *
 * @param semester semester object.
 * @param courses  list of course codes.
 * @param frozen if the solver should not amend the semester.
 */
public record Assignment(Semester semester, List<String> courses, Boolean frozen) {

}
