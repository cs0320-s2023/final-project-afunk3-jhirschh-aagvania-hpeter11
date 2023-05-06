import { useEffect } from "react";

// interface containing the React inputs to the class
interface ClassBoxProps {
  classes: Array<Array<String>>;
  setClasses: (data: Array<Array<String>>) => void;
  grad_year: number;
  posX: number;
  posY: number;
  currClass: String;
  setCurrClass: (data: String) => void;
  setTextbox: (data: string) => void;
  optionTable: Array<Array<String>>;
}

// helper function to help determine the position on screen of the class box
function position(position: number) {
  let tippy: number = 1 + position * 5.5;
  return {
    left: "0.375vw",
    top: tippy.toString() + "vh",
  };
}

export default function ClassBox(props: ClassBoxProps) {
  // variable representing the class in the overall schedule
  let currentElt = props.classes[props.posX][props.posY];

  // helper function that whcks to see if the input class is offered in the same
  // semester as this slot is
  function checkSem(): boolean {
    let sem: string = props.currClass.split(", ")[1];
    return (
      sem === "Both" ||
      (sem === "Fall" &&
        ((props.grad_year - (7 - props.posX) * 0.5) * 2) % 2 !== 0) ||
      (sem === "Spring" &&
        ((props.grad_year - (7 - props.posX) * 0.5) * 2) % 2 === 0)
    );
  }

  // function that runs when the class is clicked
  function handleClick() {
    // if there is nothing currently in the box
    if (
      (currentElt === "" || currentElt === undefined) &&
      !props.classes.find(([cat]) => cat === props.currClass.split(",")[0]) &&
      !props.optionTable.find(
        ([cat]) => cat === props.currClass.split(",")[0]
      ) &&
      checkSem()
    ) {
      // make a new array (defensive copy), and add the current class to that array
      let newArray: Array<Array<String>> = props.classes;
      newArray[props.posX][props.posY] = props.currClass.split(",")[0];
      // setClasses with that array, and reset current class and textbox
      props.setClasses([...newArray]);
      props.setCurrClass("");
      props.setTextbox("");
    }
    // else, if there is no current class
    else if (props.currClass === "") {
      // make a new array (defensive copy), and delete the class existing in this
      // location from that array
      let newArray: Array<Array<String>> = props.classes;
      delete newArray[props.posX][props.posY];
      // setClasses with that array, and reset current class and textbox
      props.setClasses([...newArray]);
      props.setCurrClass("");
      props.setTextbox("");
    }
    // else, if there is a current class to replace an existing class
    else {
      // make a new array (defensive copy), and replace the existing class
      // with the current class
      let newArray: Array<Array<String>> = props.classes;
      newArray[props.posX][props.posY] = props.currClass.split(",")[0];
      // setClasses with that array, and reset current class and textbox
      props.setClasses([...newArray]);
      props.setCurrClass("");
      props.setTextbox("");
    }
  }

  return (
    <button
      className="ClassBox"
      style={position(props.posY + 1)}
      onClick={handleClick}
    >
      {props.classes[props.posX][props.posY] === undefined
        ? ""
        : props.classes[props.posX][props.posY % 5]}
    </button>
  );
}
