// interface containing the React inputs to the class
interface SemYearHeaderProps {
  year: number;
}

// function to determine the semester presented by the header
function process_year(year: number) {
  if (year < 1764) {
    return "Semester";
  }

  if ((year * 2) % 2 !== 0) {
    return "Fall " + (year - 0.5).toString();
  }
  return "Spring " + year.toString();
}

export default function SemYearHeader(props: SemYearHeaderProps) {
  const ariaLabel = "Semester Header " + props.year.toString();
  return (
    <div
      aria-label={ariaLabel}
      aria-description="Semester Header"
      className="SemYearHeader"
      style={{ left: "0.375vw", top: "0.5vh" }}
    >
      {process_year(props.year)}
    </div>
  );
}
