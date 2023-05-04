package edu.brown.cs.student.data;

import java.util.List;

/**
 * Group of intermediate courses.
 *
 * @param name    unique group identifier.
 * @param courses courses necessary to satisfy this group, specified as a CNF.
 */
public record IntermediateGroup(String name, List<List<String>> courses) {

}
