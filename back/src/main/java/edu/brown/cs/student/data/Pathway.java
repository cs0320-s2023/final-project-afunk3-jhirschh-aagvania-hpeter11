package edu.brown.cs.student.data;

import java.util.List;

public record Pathway(String name, List<String> coreCourses, List<String> relatedCourses,
                      List<List<String>> intermediateCourses) {

}
