package edu.brown.cs.student.data;

import java.util.List;


public record Course(String courseCode, List<List<String>> prerequisites, Season season) {

}
