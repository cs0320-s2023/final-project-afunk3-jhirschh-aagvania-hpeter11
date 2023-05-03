package edu.brown.cs.student.data;


public record Semester(Integer year, Season season) implements Comparable<Semester> {

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
