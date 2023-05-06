import { useEffect, useState } from "react";
import "../styles/App.css";
import Header from "./components/Header";
import SemYear from "./components/schedule/SemYear";
import ClassOption from "./components/preferences/ClassOption";
import ClassOf from "./components/preferences/ClassOf";
import Pathways from "./components/preferences/Pathways";
import Search, { Class } from "./components/preferences/Search";
import { generate, Output } from "./components/actions/Generate";
import courses from "../../back/data/courses.json";
import Export from "./components/actions/Export";
import Info from "./components/actions/Info";

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

  function handleGenerate() {
    // add prereqs as earlier semesters to semClasses
    let prereqs: String[][] = [];
    let curSem: String[] = [];
    for (var i = 0; i < optionTable[1].length; i++) {
      curSem.push(optionTable[1][i]);
      if (curSem.length % 5 === 0) {
        prereqs.push(curSem);
        curSem = [];
      }
    }
    // if there is an uncompleted "semester" of prereqs, also add that
    if (curSem.length !== 0) {
      prereqs.push(curSem);
      curSem = [];
    }
    // add the prereqs and classes into one array
    let genClasses: String[][] = prereqs.concat(semClasses);
    // make sure the array has no empty slots
    for (var i = 0; i < genClasses.length; i++) {
      genClasses[i] = genClasses[i].flat();
    }
    // generate the optimized schedule
    generate(gradYear, genClasses, optionTable, pathway1, pathway2).then(
      (response) => {
        // if the input is incorrect or a schedule cannot be made
        if (response.result === "Error") {
          // TODO: fill this out with what happens for an improper result
          console.log("uh oh");
        } else {
          // else, update the schedule and pathways
          setSemClasses(response.schedule);
          setPathway1(response.pathway1);
          setPathway2(response.pathway2);
        }
      }
    );
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
      <Pathways num={0} setPathway={setPathway1} pathway={pathway1} />
      <Pathways num={1} setPathway={setPathway2} pathway={pathway2} />
      <Search
        class={currClass}
        setClass={setCurrClass}
        textbox={textbox}
        setTextbox={setTextbox}
      />

      <button className="ResetSchedButton" onClick={(e) => handleResetSched()}>
        Reset Schedule
      </button>
      <button className="GenerateButton" onClick={(e) => handleGenerate()}>
        Generate
      </button>
      <Export
        schedule={semClasses}
        pathway1={pathway1}
        pathway2={pathway2}
        gradYear={gradYear}
      />
      <Info />
    </div>
  );
}

export default App;
