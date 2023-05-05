import { useEffect, useState } from "react";
import "../styles/App.css";
import Header from "./components/Header";
import SemYear from "./components/schedule/SemYear";
import ClassOption from "./components/preferences/ClassOption";
import ClassOf from "./components/preferences/ClassOf";
import Pathways from "./components/preferences/Pathways";
import Search, { Class } from "./components/preferences/Search";
import { generate } from "./components/actions/Generate";
import courses from "../../back/data/courses.json";

function App() {
  // Text box for graduation year controls this hook
  const [gradYear, setGradYear] = useState<number>(2025);
  // Index corresponds to semester number
  const [semClasses, setSemClasses] = useState<Array<Array<String>>>([
    [],
    [],
    [],
    [],
    [],
    [],
    [],
    [],
  ]);

  // hook for the Avoid, pre-req, take table
  // 0 index -> Avoid
  // 1 index -> Prerequisites
  // 2 index -> Take / Want to take
  const [optionTable, setOptionTable] = useState<Array<Array<String>>>([
    [],
    [],
    [],
  ]);
  // hook for pathways
  const [pathway1, setPathway1] = useState<String>("");
  const [pathway2, setPathway2] = useState<String>("");
  // hook for the current class, as inputted into the search box
  const [currClass, setCurrClass] = useState<String>("");

  // hook for the output of the search box
  const [textbox, setTextbox] = useState<string>("");

  // map dictating how the schedule is presented
  let nums: Array<number> = [...Array(8).keys()];
  let numsMap: Map<String, number> = new Map(
    nums.map((n) => [n.toString(), n])
  );
  let numsMapArr = Array.from(numsMap, ([key, value]) => ({ key, value }));

  // handeResetSched function, clears the schedule
  function handleResetSched() {
    setSemClasses([[], [], [], [], [], [], [], []]);
  }

  let catalog: Array<Class> = courses;

  function handleGenerate() {
    // add completed classes as earlier semesters to semClasses
    let prereqs: String[][] = [];
    let curSem: String[] = [];
    for (var i = 0; i < optionTable[1].length; i++) {
      curSem.push(optionTable[1][i]);
      if (curSem.length % 5 === 0) {
        prereqs.push(curSem);
        curSem = [];
      }
    }
    if (curSem.length !== 0) {
      prereqs.push(curSem);
      curSem = [];
    }
    let genClasses: String[][] = prereqs.concat(semClasses);

    generate(gradYear, genClasses, optionTable, pathway1, pathway2);
  }

  return (
    <div>
      <Header />
      <div className="ScheduleBackground">
        {numsMapArr.map(({ key, value }) => (
          <SemYear
            num={value}
            grad_year={gradYear}
            classes={semClasses}
            setClasses={setSemClasses}
            currClass={currClass}
            setCurrClass={setCurrClass}
            setTextbox={setTextbox}
            optionTable={optionTable}
          />
        ))}
      </div>
      <div className="ClassOptionBackground">
        <ClassOption
          classes={optionTable}
          setClasses={setOptionTable}
          currClass={currClass}
          setCurrClass={setCurrClass}
          setTextbox={setTextbox}
          schedule={semClasses}
        />
      </div>
      <ClassOf setNum={setGradYear} />
      <Pathways num={0} setPathway={setPathway1} />
      <Pathways num={1} setPathway={setPathway2} />
      <Search
        class={currClass}
        setClass={setCurrClass}
        textbox={textbox}
        setTextbox={setTextbox}
      />
      <button className="ResetSchedButton" onClick={(e) => handleResetSched()}>
        Reset Schedule
      </button>
      <button
        className="GenerateButton"
        onClick={(e) => handleGenerate()}
      ></button>
    </div>
  );
}

export default App;
