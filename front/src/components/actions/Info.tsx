import { useState } from "react";

interface InfoProps {}

export default function Info(props: InfoProps) {
  // useState to determine when the dropdown is open or closed
  const [open, setOpen] = useState(false);

  // handles opening the dropdown
  const handleOpen = () => {
    setOpen(!open);
  };

  return (
    <div>
      {open ? (
        <div>
          <div
            className="InfoBox"
            style={{
              width: "16vw",
              height: "13vh",
              left: "11vw",
              top: "7vh",
            }}
          >
            This is where you input your expected graduation year. If you'll be
            graduating in the Fall, put .5 at the end of your year!
          </div>
          <div
            className="InfoBox"
            style={{ width: "20vw", height: "10vh", left: "11vw", top: "30vh" }}
          >
            This is where you search for classes. Select the class you want from
            the dropdown menu, then select another button to place it!
          </div>
          <div
            className="InfoBox"
            style={{ width: "25vw", height: "18vh", left: "11vw", top: "63vh" }}
          >
            This is where you can specify that you want to avoid or definitely
            take certain classes, or if you have credit for a requirement from
            before Brown. After selecting a button in the search bar, select the
            title of the column you want to add that class to! You can clear any
            of the categories by clicking their respective 'clear' buttons.
          </div>
          <div
            className="InfoBox"
            style={{
              width: "25vw",
              height: "20vh",
              left: "60.925vw",
              top: "35vh",
            }}
          >
            This is the schedule that will be filled in once you click
            'generate'! Each of the 5 slots beneath a given semester can be
            selected. If you have a class selected from the search bar that
            you've already taken or want to take during a specific semester,
            click a course slot to fill it! To remove that course, click it
            again. You can also reset the whole schedule at once.
          </div>
          <div
            className="InfoBox"
            style={{
              width: "24vw",
              height: "12vh",
              left: "67.375vw",
              top: "2vh",
            }}
          >
            Click here to export the currect schedule and selected or
            recommended pathways as a CSV, which can be opened and viewed in
            another program of your choice (such as Excel or Google Sheets).
          </div>
        </div>
      ) : null}
      <button className="InfoButton" onClick={(e) => handleOpen()}>
        Info
      </button>
    </div>
  );
}
