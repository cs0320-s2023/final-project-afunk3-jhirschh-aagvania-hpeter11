import { useEffect, useState } from "react";
import courses from "../../../../back/data/courses.json";

interface ClassOfProps {
  class: String;
  setClass: (data: String) => void;
  textbox: string;
  setTextbox: (data: string) => void;
}

interface Class {
  courseCode: string;
  prerequisites: string[][];
  season: string;
}

function parseCatalog() {
  let catalog: Array<Class> = courses;
  // for (var i = 0; i < courses.length; i++) {
  //   catalog.push(JSON.parse(courses[i]));
  // }
  // courses.forEach((elt) => {
  //   catalog.push(JSON.parse(elt.toString()));
  // });
  //let catalog: Catalog = JSON.parse(courses.toString());
  console.log(catalog);
}

export default function Search(props: ClassOfProps) {
  const [open, setOpen] = useState(false);

  let catalog: Array<Class> = courses;

  const [courseList, setCourseList] = useState<Array<string>>([]);
  //let courseList: string[] = [];

  function bestMatches(text: string) {
    let courses: string[] = [];
    for (var i = 0; i < catalog.length; i++) {
      if (courses.length === 4) {
        console.log(courses);
        setCourseList(courses);
        return;
      }
      if (catalog[i].courseCode.includes(text)) {
        courses.push(catalog[i].courseCode + ", " + catalog[i].season);
      }
    }
    setCourseList(courses);
  }

  const handleOpen = () => {
    setOpen(!open);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    props.setTextbox(e.target.value);
    if (open === false || e.target.value === "") {
      handleOpen();
    }
    bestMatches(e.target.value);
  };

  function handleClick(num: number) {
    props.setTextbox(courseList[num]);
    handleOpen();
    props.setClass(courseList[num]);
  }

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
