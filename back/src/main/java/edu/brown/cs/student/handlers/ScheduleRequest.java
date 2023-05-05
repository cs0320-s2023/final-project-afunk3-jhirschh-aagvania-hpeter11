package edu.brown.cs.student.handlers;

import edu.brown.cs.student.data.Assignment;
import java.util.List;

/**
 * Request to the concentration handler.
 *
 * @param preferred         list of preferred courses.
 * @param undesirable       list of undesirable courses.
 * @param preferredPathways list of preferred pathways.
 * @param partialAssignment partial assignment to the schedule.
 */
public record ScheduleRequest(List<String> preferred, List<String> undesirable,
                              List<String> preferredPathways,
                              List<Assignment> partialAssignment) {

}
