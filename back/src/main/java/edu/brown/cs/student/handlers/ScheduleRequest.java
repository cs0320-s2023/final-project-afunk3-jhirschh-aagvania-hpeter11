package edu.brown.cs.student.handlers;

import edu.brown.cs.student.data.Assignment;
import java.util.List;

public record ScheduleRequest(List<String> preferred, List<String> undesirable,
                              List<String> preferredPathways,
                              List<Assignment> partialAssignment) {

}
