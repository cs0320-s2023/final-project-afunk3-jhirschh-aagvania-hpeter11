package edu.brown.cs.student.handlers;

/**
 * Failure response by the concentration handler.
 */
public record ScheduleFailureResponse(String result, String message) {

  public ScheduleFailureResponse(String message) {
    this("error", message);
  }

}
