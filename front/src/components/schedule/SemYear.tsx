import { useState } from "react";
import SemYearHeader from "./SemYearHeader";
import ClassBox from "./ClassBox";

// interface containing the React inputs to the class
interface SemYearProps {
  num: number;
  grad_year: number;
  classes: Array<Array<String>>;
  setClasses: (data: Array<Array<String>>) => void;
  currClass: String;
  setCurrClass: (data: String) => void;
  setTextbox: (data: string) => void;
  optionTable: Array<Array<String>>;
}

// helper function to help determine the position on screen of the semester box
function position(back: number) {
  let tippy: string;
  let loosey: string;
  if (back >= 4) {
    tippy = "36.75vh";
  } else {
    tippy = "0.75vh";
  }
  let looseyNum: number = (back % 4) * 11.6 + 1;
  loosey = looseyNum.toString() + "vw";
  return {
    left: loosey,
    top: tippy,
  };
}

export default function SemYear(props: SemYearProps) {
  // map dictating how the classes are presented within the semester
  let nums: Array<number> = [...Array(5).keys()];
  let numsMap: Map<String, number> = new Map(
    nums.map((n) => [n.toString(), n])
  );
  let numsMapArr = Array.from(numsMap, ([key, value]) => ({ key, value }));

  const ariaLabel = "Semester Background " + props.num.toString();

  return (
    <div
      aria-label={ariaLabel}
      aria-description="Background for an individual semester of classes"
      className="SemBackground"
      style={position(props.num)}
    >
      <SemYearHeader year={props.grad_year + 0.5 * (props.num - 7)} />
      {numsMapArr.map(({ key, value }) => (
        <ClassBox
          classes={props.classes}
          setClasses={props.setClasses}
          grad_year={props.grad_year}
          posX={props.num}
          posY={value}
          currClass={props.currClass}
          setCurrClass={props.setCurrClass}
          setTextbox={props.setTextbox}
          optionTable={props.optionTable}
        />
      ))}
    </div>
  );
}
