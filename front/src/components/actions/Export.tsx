import { SetStateAction, useState } from "react";

// interface containing the React inputs to the class
interface ExportProps {
  schedule: String[][];
  pathway1: String;
  pathway2: String;
  gradYear: number;
}

export default function Export(props: ExportProps) {
  // helper function that actually downloads the file to the user's computer
  function downloadFile(file: File) {
    // Create a link and set the URL using `createObjectURL`
    const link = document.createElement("a");
    link.style.display = "none";
    link.href = URL.createObjectURL(file);
    link.download = file.name;

    // It needs to be added to the DOM so it can be clicked
    document.body.appendChild(link);
    link.click();

    // To make this work on Firefox we need to wait
    // a little while before removing it.
    setTimeout(() => {
      URL.revokeObjectURL(link.href);
      if (link.parentNode !== null) {
        link.parentNode.removeChild(link);
      }
    }, 0);
  }

  // function that runs on submission
  function handleClick() {
    let exportSched: String[][] = props.schedule;
    // add year header to each semester
    for (
      var year = props.gradYear - 3.5;
      year < props.gradYear + 0.5;
      year += 0.5
    ) {
      let semHeader: String = "";
      // if the semester is fall
      if ((year * 2) % 2 !== 0) {
        semHeader = Math.floor(year).toString() + " Fall";
      }
      // if the semester is spring
      else {
        semHeader = Math.floor(year).toString() + " Spring";
      }
      // combine the semester header with classes for that semester
      exportSched[7 - (props.gradYear - year) * 2].unshift(semHeader);
    }
    // convert to csv
    let csv: string = "";
    for (var r = 0; r < 6; r++) {
      for (var sem = 0; sem < 8; sem++) {
        // if a box is blank
        if (typeof exportSched[sem][r] === "undefined") {
          csv += ", ";
        }
        // else, add that box to the csv
        else {
          csv += exportSched[sem][r] + ", ";
        }
      }
      // at the end of a line, enter down to the next line
      csv = csv.substring(0, csv.length - 1) + "\n";
    }
    // add the pathways to the csv
    csv += "Pathways:, " + props.pathway1 + ", " + props.pathway2;
    // make and download the csv file
    const csvFile = new File([csv as BlobPart], "schedule.csv");
    downloadFile(csvFile);
  }

  return (
    <button
      aria-label="Export Button"
      aria-description="Click this button to export the class schedule as a CSV"
      className="ActionButton"
      onClick={(e) => handleClick()}
      style={{ left: "73.375vw" }}
    >
      Export
    </button>
  );
}
