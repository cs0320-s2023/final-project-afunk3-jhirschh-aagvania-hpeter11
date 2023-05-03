import { useState } from "react";

interface ClassOfProps {
  class: String;
  setClass: (data: String) => void;
  textbox: string;
  setTextbox: (data: string) => void;
}

export default function Search(props: ClassOfProps) {
  const [open, setOpen] = useState(false);

  const handleOpen = () => {
    setOpen(!open);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    props.setTextbox(e.target.value);
    if (open === false || e.target.value === "") {
      handleOpen();
    }
  };

  function handleClick(num: number) {
    props.setTextbox(courseList[num]);
    handleOpen();
    props.setClass(courseList[num]);
  }

  const courseList = ["hello", "hi", "hey", "yo"];

  return (
    <div style={{ left: "15vw", top: "25vh" }}>
      <input
        type="text"
        aria-label="Search Box"
        aria-description="Search for classes in this input box"
        className="SearchBox"
        placeholder="Find classes..."
        onChange={(e) => handleChange(e)}
        value={props.textbox}
        style={{ left: "11vw", top: "40vh" }}
      />
      {open ? (
        <ul className="SearchDropdown">
          <li>
            <button
              className="SearchOption"
              style={{ left: "11vw", top: "45vh" }}
              onClick={(e) => handleClick(0)}
            >
              {courseList[0]}
            </button>
          </li>
          <li>
            <button
              className="SearchOption"
              style={{ left: "11vw", top: "49.9vh" }}
              onClick={(e) => handleClick(1)}
            >
              {courseList[1]}
            </button>
          </li>
          <li>
            <button
              className="SearchOption"
              style={{ left: "11vw", top: "54.9vh" }}
              onClick={(e) => handleClick(2)}
            >
              {courseList[2]}
            </button>
          </li>
          <li>
            <button
              className="SearchOption"
              style={{ left: "11vw", top: "59.9vh" }}
              onClick={(e) => handleClick(3)}
            >
              {courseList[3]}
            </button>
          </li>
        </ul>
      ) : null}
    </div>
  );
}
