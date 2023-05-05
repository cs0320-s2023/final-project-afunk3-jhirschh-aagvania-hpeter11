package edu.brown.cs.student.data;

import java.util.List;

/**
 * Course object.
 *
 * @param courseCode    unique course identifier.
 * @param prerequisites course codes of prerequisites, specified as a CNF.
 * @param season        when the course is offered.
 */
public record Course(String courseCode, List<List<String>> prerequisites, Season season) {

}
