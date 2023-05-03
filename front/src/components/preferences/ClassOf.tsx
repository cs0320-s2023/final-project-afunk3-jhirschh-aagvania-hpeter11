import { useState } from "react";

interface ClassOfProps {
  setNum: (data: number) => void;
}

export default function ClassOf(props: ClassOfProps) {
  const [textbox, setTextbox] = useState("");

  function handleSubmit() {
    let inputYear: number | undefined = Number(textbox);
    let roundness = (inputYear * 2) % 2;
    if (isNaN(inputYear) || (roundness != 0 && roundness != 1)) {
      // TODO: Change how we tell users about errors
      setTextbox("Invalid Year");
    } else if (typeof inputYear === "number") {
      props.setNum(inputYear);
      setTextbox("");
    } else {
      throw new Error("Should not get here.");
    }
  }
  // className="ClassOfBackground"
  return (
    <div style={{ left: "15vw", top: "25vh" }}>
      <input
        type="text"
        aria-label="Class Of Input Box"
        aria-description="Type class year in this input box"
        className="ClassOfBox"
        placeholder="Graduation Year"
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
        className="ClassOfButton"
        onClick={handleSubmit}
        style={{ left: "31vw", top: "20vh" }}
      >
        Submit
      </button>
    </div>
  );
}
