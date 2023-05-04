package edu.brown.cs.student.data;

import java.util.List;

/**
 * Pathway object.
 *
 * @param name                unique pathway identifier.
 * @param coreCourses         list of core courses in this pathway.
 * @param relatedCourses      list of related courses in this pathway.
 * @param intermediateCourses list of intermediate courses in this pathway, specified as a CNF.
 */
public record Pathway(String name, List<String> coreCourses, List<String> relatedCourses,
                      List<List<String>> intermediateCourses) {

}
