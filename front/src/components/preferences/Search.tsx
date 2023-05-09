import { useEffect, useState } from "react";
import courses from "../../../../back/data/courses.json";

// interface containing the React inputs to the class
interface ClassOfProps {
  class: String;
  setClass: (data: String) => void;
  textbox: string;
  setTextbox: (data: string) => void;
}

// interface containing the classes that are taken in the dropdown
export interface Class {
  courseCode: string;
  prerequisites: string[][];
  season: string;
}

export default function Search(props: ClassOfProps) {
  // useState to determine when the dropdown is open or closed
  const [open, setOpen] = useState(false);

  // turning the courses json into an Array of Classes
  let catalog: Array<Class> = courses;

  // useState for the courses presented in the dropdown
  const [courseList, setCourseList] = useState<Array<string>>([]);

  // function to find the courses presented in the dropdown
  function bestMatches(text: string) {
    let courses: string[] = [];
    for (var i = 0; i < catalog.length; i++) {
      // set the course list with the best match courses if there are 4
      if (courses.length === 4) {
        console.log(courses);
        setCourseList(courses);
        return;
      }
      // add a course to the course list
      if (catalog[i].courseCode.includes(text.toUpperCase())) {
        courses.push(catalog[i].courseCode + ", " + catalog[i].season);
      }
    }
    // set the course list with the best match courses
    setCourseList(courses);
  }

  // handles opening the dropdown
  const handleOpen = () => {
    setOpen(!open);
  };

  // handles a change in the textbox
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    props.setTextbox(e.target.value);
    // if the dropdown is not open or the textbox gets emptied
    if (open === false || e.target.value === "") {
      // change the state of open
      handleOpen();
    }
    // run the bestMatches helper function
    bestMatches(e.target.value);
  };

  // function that runs when an item is clicked in the dropdown
  function handleClick(num: number) {
    // set the textbox and classes
    props.setTextbox(courseList[num]);
    handleOpen();
    props.setClass(courseList[num]);
  }

  return (
    <div
      aria-label="Search Box Background"
      aria-description="Search for classes in this input box"
      style={{ left: "15vw", top: "25vh" }}
    >
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
        <ul
          aria-label="Search Dropdown"
          aria-description="List of classes that are close to what has been inputted"
          className="SearchDropdown"
        >
          {courseList.length >= 1 ? (
            <li>
              <button
                aria-label="Search option 1"
                aria-description="Top option for what the user is searching for"
                className="SearchOption"
                style={{ left: "11vw", top: "45vh" }}
                onClick={(e) => handleClick(0)}
              >
                {courseList[0]}
              </button>
            </li>
          ) : null}
          {courseList.length >= 2 ? (
            <li>
              <button
                aria-label="Search option 2"
                aria-description="Second option for what the user is searching for"
                className="SearchOption"
                style={{ left: "11vw", top: "49.9vh" }}
                onClick={(e) => handleClick(1)}
              >
                {courseList[1]}
              </button>
            </li>
          ) : null}
          {courseList.length >= 3 ? (
            <li>
              <button
                aria-label="Search option 3"
                aria-description="Third option for what the user is searching for"
                className="SearchOption"
                style={{ left: "11vw", top: "54.9vh" }}
                onClick={(e) => handleClick(2)}
              >
                {courseList[2]}
              </button>
            </li>
          ) : null}
          {courseList.length >= 4 ? (
            <li>
              <button
                aria-label="Search option 4"
                aria-description="Fourth option for what the user is searching for"
                className="SearchOption"
                style={{ left: "11vw", top: "59.9vh" }}
                onClick={(e) => handleClick(3)}
              >
                {courseList[3]}
              </button>
            </li>
          ) : null}
        </ul>
      ) : null}
    </div>
  );
}
