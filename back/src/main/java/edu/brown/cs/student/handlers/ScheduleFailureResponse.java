package edu.brown.cs.student.handlers;

public record ScheduleFailureResponse(String result, String message) {

  public ScheduleFailureResponse(String message) {
    this("error", message);
  }

}
