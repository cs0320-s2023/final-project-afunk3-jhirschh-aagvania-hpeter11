import { useState } from "react";

// interface containing the React inputs to the class
interface ClassOfProps {
  setNum: (data: number) => void;
}

export default function ClassOf(props: ClassOfProps) {
  // use state for the textbox
  const [textbox, setTextbox] = useState("");
  // use state for the placeholder text
  const [placeholder, setPlaceholder] = useState("Graduation Year");

  // function that runs on submission
  function handleSubmit() {
    // convert the input into a number
    let inputYear: number | undefined = Number(textbox);
    let roundness = (inputYear * 2) % 2;
    // if the input is not a valid number
    if (isNaN(inputYear) || (roundness != 0 && roundness != 1)) {
      // TODO: Change how we tell users about errors
      setTextbox("");
      setPlaceholder("Invalid Year");
      setTimeout(() => setPlaceholder("Graduation Year"), 2000);
    }
    // else if the input is a number
    else if (typeof inputYear === "number") {
      if (inputYear < 1764) {
        setTextbox("");
        setPlaceholder("Invalid Year");
        setTimeout(() => setPlaceholder("Graduation Year"), 2000);
      } else {
        props.setNum(inputYear);
        setTextbox("");
      }
    }
    // else (should not reach this)
    else {
      throw new Error("Should not get here.");
    }
  }
  return (
    <div
      aria-label="Graduation Year Input Box Background"
      aria-description="Type graduation year in this input box"
      style={{ left: "15vw", top: "25vh" }}
    >
      <input
        type="text"
        aria-label="Graduation Year Input Box"
        aria-description="Type graduation year in this input box"
        className="ClassOfBox"
        placeholder={placeholder}
        onChange={(e) => setTextbox(e.target.value)}
        value={textbox}
        onKeyUp={(e) => {
          if (e.key === "Enter") {
            handleSubmit();
          }
        }}
        style={{ left: "11vw", top: "20vh" }}
      />
      <button
        aria-label=" Graduation Year Input Button"
        aria-description="Click this button to input the graduation year"
        className="ClassOfButton"
        onClick={handleSubmit}
        style={{ left: "31vw", top: "20vh" }}
      >
        Submit
      </button>
    </div>
  );
}
