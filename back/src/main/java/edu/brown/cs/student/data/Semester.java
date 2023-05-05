package edu.brown.cs.student.data;

/**
 * Semester object.
 *
 * @param year   current or future year.
 * @param season of the current semester.
 */
public record Semester(Integer year, Season season) implements Comparable<Semester> {

  /**
   * Compares two semesters chronologically.
   *
   * @param semester the object to be compared.
   * @return 1 if current object is greater, -1 if less and 0 if equal.
   */
  @Override
  public int compareTo(Semester semester) {
    if (this.year < semester.year) {
      return -1;
    } else if (this.year > semester.year) {
      return 1;
    } else {
      if (this.season == Season.Spring && semester.season == Season.Fall) {
        return -1;
      } else if (this.season == Season.Fall && semester.season == Season.Spring) {
        return 1;
      } else {
        return 0;
      }
    }
  }
}
