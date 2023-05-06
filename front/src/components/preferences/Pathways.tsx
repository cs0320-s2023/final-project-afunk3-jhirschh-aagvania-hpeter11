import { SetStateAction, useState } from "react";

// interface containing the React inputs to the class
interface PathwayProps {
  num: number;
  setPathway: (data: string) => void;
  pathway: String;
}

// helper function to help determine the position on screen of the pathway box
function position(num: number) {
  if (num === 0) {
    return {
      left: "10.5vw",
      top: "30vh",
    };
  } else if (num === 1) {
    return {
      left: "25.5vw",
      top: "30vh",
    };
  } else {
    throw new Error("Invalid num.");
  }
}

export default function Pathways(props: PathwayProps) {
  // constant determining what items are shown in the dropdown
  const options = [
    { label: "Select Pathway...", value: "none" },

    { label: "Systems", value: "Systems" },

    { label: "Software Principles", value: "Software Principles" },

    { label: "Data", value: "Data" },

    { label: "AI/ML", value: "Artificial Intelligence/Machine Learning" },

    { label: "Theory", value: "Theory" },

    { label: "Security", value: "Security" },

    { label: "Visual Computing", value: "Visual Computing" },

    { label: "Computational Biology", value: "Computational Biology" },

    { label: "Design", value: "Design" },
  ];

  // useState determining what gets shown
  const [value, setValue] = useState<String>("");

  // function that runs when the pathway is changed
  const handleChange = (event: {
    target: { value: SetStateAction<String> };
  }) => {
    setValue(event.target.value);
    props.setPathway(event.target.value.toString());
  };

  return (
    <div
      aria-label="Pathway Dropdown Menu"
      aria-description="Dropdown Menu you can use to select which pathways to complete"
    >
      <label>
        <select
          aria-label="Pathway Dropdown Menu"
          aria-description="Dropdown Menu you can use to select which pathways to complete"
          className="PathwayDropdown"
          value={String(value)}
          onChange={handleChange}
          style={position(props.num)}
        >
          {options.map((option) => (
            <option className="PathwayOption" value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </label>
    </div>
  );
}
