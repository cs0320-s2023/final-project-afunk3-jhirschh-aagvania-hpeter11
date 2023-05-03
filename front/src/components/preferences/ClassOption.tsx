import { useEffect, useState } from "react";
import SemYearHeader from "../schedule/SemYearHeader";
import ClassBox from "../schedule/ClassBox";
import { each } from "jquery";

interface ClassOptionProps {
  classes: Array<Array<String>>;
  setClasses: (data: Array<Array<String>>) => void;
  currClass: String;
  setCurrClass: (data: String) => void;
  setTextbox: (data: string) => void;
  schedule: Array<Array<String>>;
}

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

function formTable(tableData: String[][]): React.ReactElement[] {
  let table: React.ReactElement[] = [];

  for (var i = 0; i < tableData.length; i++) {
    table.push(<tr className="ClassOptionRow">{formRow(tableData[i])}</tr>);
  }
  return table;
}

function formRow(rowData: String[]): React.ReactElement[] {
  let row: React.ReactElement[] = [];
  for (var i = 0; i < rowData.length; i++) {
    row.push(<td className="ClassOptionRow">{rowData[i]}</td>);
  }
  return row;
}

export default function ClassOption(props: ClassOptionProps) {
  const [avoidTable, setAvoidTable] = useState<Array<String>>([]);
  const [completedTable, setCompletedTable] = useState<Array<String>>([]);
  const [takeTable, setTakeTable] = useState<Array<String>>([]);

  function clearSubmit(
    setFunc: React.Dispatch<React.SetStateAction<String[]>>,
    num: number
  ) {
    setFunc([]);
    let newArr: Array<Array<String>> = props.classes;
    newArr[num] = [];
    props.setClasses(newArr);
  }

  function twoDFind<T>(arr: Array<Array<T>>, check: T): boolean {
    for (var i = 0; i < arr.length; i++) {
      if (!!arr[i].find((car) => car === check)) {
        return true;
      }
    }
    return false;
  }

  function handleSubmit(type: String) {
    console.log(props.schedule);
    if (
      props.currClass === "" ||
      avoidTable.find((cat) => cat === props.currClass) ||
      completedTable.find((cat) => cat === props.currClass) ||
      takeTable.find((cat) => cat === props.currClass) ||
      twoDFind<String>(props.schedule, props.currClass)
    ) {
      props.setCurrClass("");
      props.setTextbox("");
      return;
    }
    if (type === "Avoid") {
      setAvoidTable([...avoidTable, props.currClass]);
      let avoidCopy: Array<String> = avoidTable;
      avoidCopy.push(props.currClass);
      let newArr: Array<Array<String>> = props.classes;
      newArr[0] = avoidCopy;
      props.setClasses(newArr);
      console.log(props.currClass);
    } else if (type === "Completed") {
      setCompletedTable([...completedTable, props.currClass]);
      let completedCopy: Array<String> = completedTable;
      completedCopy.push(props.currClass);
      let newArr: Array<Array<String>> = props.classes;
      newArr[0] = completedCopy;
      props.setClasses(newArr);
    } else if (type === "Take") {
      setTakeTable([...takeTable, props.currClass]);
      let takeCopy: Array<String> = takeTable;
      takeCopy.push(props.currClass);
      let newArr: Array<Array<String>> = props.classes;
      newArr[0] = takeCopy;
      props.setClasses(newArr);
    } else {
      throw new Error("Should never reach here.");
    }
    props.setCurrClass("");
    props.setTextbox("");
  }
  // TODO: finish handleSubmit and figure out how to present the classes properly
  return (
    <div>
      <button
        className="OptionButton"
        style={{ left: "0vw", top: "0vh" }}
        onClick={(e) => handleSubmit("Avoid")}
      >
        Avoid
      </button>
      <div className="OptionList" style={{ left: "0vw", top: "5vh" }}>
        {makeTable(avoidTable)}
      </div>
      <button
        className="OptionButton"
        style={{ left: "0vw", top: "34.5vh" }}
        onClick={(e) => clearSubmit(setAvoidTable, 0)}
      >
        Clear
      </button>

      <button
        className="OptionButton"
        style={{ left: "9.9vw", top: "0vh" }}
        onClick={(e) => handleSubmit("Completed")}
      >
        Completed
      </button>
      <div className="OptionList" style={{ left: "9.9vw", top: "5vh" }}>
        {makeTable(completedTable)}
      </div>
      <button
        className="OptionButton"
        style={{ left: "9.9vw", top: "34.5vh" }}
        onClick={(e) => clearSubmit(setCompletedTable, 1)}
      >
        Clear
      </button>

      <button
        className="OptionButton"
        style={{ left: "19.8vw", top: "0vh" }}
        onClick={(e) => handleSubmit("Take")}
      >
        Take
      </button>
      <div className="OptionList" style={{ left: "19.8vw", top: "5vh" }}>
        {makeTable(takeTable)}
      </div>
      <button
        className="OptionButton"
        style={{ left: "19.8vw", top: "34.5vh" }}
        onClick={(e) => clearSubmit(setTakeTable, 2)}
      >
        Clear
      </button>
    </div>
  );
}
