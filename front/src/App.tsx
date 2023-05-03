import { useEffect, useState } from "react";
import "../styles/App.css";
import Header from "./components/Header";
import SemYear from "./components/schedule/SemYear";
import ClassOption from "./components/preferences/ClassOption";
import ClassOf from "./components/preferences/ClassOf";
import Pathways from "./components/preferences/Pathways";
import Search from "./components/preferences/Search";

function App() {
  // TODO: Text box for graduation year controls this hook
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

  // 0 index -> Avoid
  // 1 index -> Completed
  // 2 index -> Take / Want to take
  const [optionTable, setOptionTable] = useState<Array<Array<String>>>([
    [],
    [],
    [],
  ]);
  const [pathway1, setPathway1] = useState<String>("");
  const [pathway2, setPathway2] = useState<String>("");
  const [currClass, setCurrClass] = useState<String>("");

  const [textbox, setTextbox] = useState<string>("");

  let nums: Array<number> = [...Array(8).keys()];
  let numsMap: Map<String, number> = new Map(
    nums.map((n) => [n.toString(), n])
  );
  let numsMapArr = Array.from(numsMap, ([key, value]) => ({ key, value }));

  function handleResetSched() {
    setSemClasses([[], [], [], [], [], [], [], []]);
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
    </div>
  );
}

export default App;
