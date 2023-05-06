import { useEffect, useState } from "react";
import SemYearHeader from "../schedule/SemYearHeader";
import ClassBox from "../schedule/ClassBox";
import { each } from "jquery";

// interface containing the React inputs to the class
interface ClassOptionProps {
  classes: Array<Array<String>>;
  setClasses: (data: Array<Array<String>>) => void;
  currClass: String;
  setCurrClass: (data: String) => void;
  setTextbox: (data: string) => void;
  schedule: Array<Array<String>>;
}

// function that makes a table given an array of strings, where each string is a row
function makeTable(tableData: String[]): React.ReactElement {
  let newArr: Array<Array<String>> = [];
  for (var r = 0; r < tableData.length; r++) {
    newArr.push([tableData[r]]);
  }
  return (
    <table className="ClassOptionTable">
      <tbody className="ClassOptionRow">{formTable(newArr)}</tbody>
    </table>
  );
}

// helper function that forms the body of the table
function formTable(tableData: String[][]): React.ReactElement[] {
  let table: React.ReactElement[] = [];

  for (var i = 0; i < tableData.length; i++) {
    table.push(<tr className="ClassOptionRow">{formRow(tableData[i])}</tr>);
  }
  return table;
}

// helper function that forms the rows of the table
function formRow(rowData: String[]): React.ReactElement[] {
  let row: React.ReactElement[] = [];
  for (var i = 0; i < rowData.length; i++) {
    row.push(<td className="ClassOptionRow">{rowData[i]}</td>);
  }
  return row;
}

export default function ClassOption(props: ClassOptionProps) {
  // useStates for the three class option divs
  const [avoidTable, setAvoidTable] = useState<Array<String>>([]);
  const [completedTable, setCompletedTable] = useState<Array<String>>([]);
  const [takeTable, setTakeTable] = useState<Array<String>>([]);

  // function that clears the div corresponding to an inputted function
  function clearSubmit(
    setFunc: React.Dispatch<React.SetStateAction<String[]>>,
    num: number
  ) {
    setFunc([]);
    let newArr: Array<Array<String>> = props.classes;
    newArr[num] = [];
    props.setClasses(newArr);
  }

  // helper function that finds if an item is in a 2d-array
  function twoDFind<T>(arr: Array<Array<T>>, check: T): boolean {
    for (var i = 0; i < arr.length; i++) {
      if (!!arr[i].find((car) => car === check)) {
        return true;
      }
    }
    return false;
  }

  // function that runs on submission
  function handleSubmit(type: String) {
    // get the class from the input string, removing the semester
    let currClass: string = props.currClass.split(",")[0];
    // if the class has already been logged, or if there is no class to log
    if (
      props.currClass === "" ||
      avoidTable.find((cat) => cat === currClass) ||
      completedTable.find((cat) => cat === currClass) ||
      takeTable.find((cat) => cat === currClass) ||
      twoDFind<String>(props.schedule, currClass)
    ) {
      // set the current class and textbox to blank
      props.setCurrClass("");
      props.setTextbox("");
      return;
    }
    // if the avoid button was clicked
    if (type === "Avoid") {
      // set the avoid output div
      setAvoidTable([...avoidTable, currClass]);
      // create a copy of the avoid table to work around concurrency issues
      let avoidCopy: Array<String> = avoidTable;
      // add the new class to the table
      avoidCopy.push(currClass);
      let newArr: Array<Array<String>> = props.classes;
      newArr[0] = avoidCopy;
      // update the global variable corresponding to this table with the new class
      props.setClasses(newArr);
    }
    // else if the completed button was clicked
    else if (type === "Completed") {
      // set the completed output div
      setCompletedTable([...completedTable, currClass]);
      // create a copy of the completed table to work around concurrency issues
      let completedCopy: Array<String> = completedTable;
      // add the new class to the table
      completedCopy.push(currClass);
      let newArr: Array<Array<String>> = props.classes;
      newArr[1] = completedCopy;
      // update the global variable corresponding to this table with the new class
      props.setClasses(newArr);
    }
    // else if the take button was clicked
    else if (type === "Take") {
      // set the take output div
      setTakeTable([...takeTable, currClass]);
      // create a copy of the take table to work around concurrency issues
      let takeCopy: Array<String> = takeTable;
      // add the new class to the table
      takeCopy.push(currClass);
      let newArr: Array<Array<String>> = props.classes;
      newArr[2] = takeCopy;
      // update the global variable corresponding to this table with the new class
      props.setClasses(newArr);
    }
    // else (should never happen)
    else {
      throw new Error("Should never reach here.");
    }
    props.setCurrClass("");
    props.setTextbox("");
  }
  // TODO: finish handleSubmit and figure out how to present the classes properly
  return (
    <div>
      <button
        aria-label="Avoid Button"
        aria-description="Click this button after searching to signify that you want to avoid a class"
        className="OptionButton"
        style={{ left: "0vw", top: "0vh" }}
        onClick={(e) => handleSubmit("Avoid")}
      >
        Avoid
      </button>
      <div
        aria-label="Avoid Table"
        aria-description="A table that displays the classes you wish to avoid"
        className="OptionList"
        style={{ left: "0vw", top: "5vh" }}
      >
        {makeTable(avoidTable)}
      </div>
      <button
        aria-label="Clear Button"
        aria-description="Click this button to clear the avoid table"
        className="OptionButton"
        style={{ left: "0vw", top: "34.5vh" }}
        onClick={(e) => clearSubmit(setAvoidTable, 0)}
      >
        Clear
      </button>

      <button
        aria-label="Prerequisites Button"
        aria-description="Click this button after searching to signify that you have a credit corresponding to a class"
        className="OptionButton"
        style={{ left: "9.9vw", top: "0vh" }}
        onClick={(e) => handleSubmit("Completed")}
      >
        Prerequisites
      </button>
      <div
        aria-label="Prerequisites Table"
        aria-description="A table that displays the classes you have credit for as prerequisites, such as from APs"
        className="OptionList"
        style={{ left: "9.9vw", top: "5vh" }}
      >
        {makeTable(completedTable)}
      </div>
      <button
        aria-label="Clear Button"
        aria-description="Click this button to clear the prerequisites table"
        className="OptionButton"
        style={{ left: "9.9vw", top: "34.5vh" }}
        onClick={(e) => clearSubmit(setCompletedTable, 1)}
      >
        Clear
      </button>

      <button
        aria-label="Take Button"
        aria-description="Click this button after searching to signify that you want to take a class"
        className="OptionButton"
        style={{ left: "19.8vw", top: "0vh" }}
        onClick={(e) => handleSubmit("Take")}
      >
        Take
      </button>
      <div
        aria-label="Take Table"
        aria-description="A table that displays the classes you wish to take"
        className="OptionList"
        style={{ left: "19.8vw", top: "5vh" }}
      >
        {makeTable(takeTable)}
      </div>
      <button
        aria-label="Clear Button"
        aria-description="Click this button to clear the take table"
        className="OptionButton"
        style={{ left: "19.8vw", top: "34.5vh" }}
        onClick={(e) => clearSubmit(setTakeTable, 2)}
      >
        Clear
      </button>
    </div>
  );
}
