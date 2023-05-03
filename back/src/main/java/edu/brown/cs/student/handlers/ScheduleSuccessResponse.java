package edu.brown.cs.student.handlers;

import edu.brown.cs.student.data.Assignment;
import java.util.List;
import java.util.Map;

public record ScheduleSuccessResponse(String result, List<Assignment> schedule,
                                      Map<String, List<String>> pathways) {

  public ScheduleSuccessResponse(List<Assignment> schedule,
      Map<String, List<String>> pathways) {
    this("success", schedule, pathways);
  }
}
