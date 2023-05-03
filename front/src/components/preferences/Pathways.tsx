import { SetStateAction, useState } from "react";

interface PathwayProps {
  num: number;
  setPathway: (data: string) => void;
}

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
  const options = [
    { label: "Select Pathway...", value: "none" },

    { label: "Systems", value: "systems" },

    { label: "Software Principles", value: "software_principles" },

    { label: "Data", value: "data" },

    { label: "AI/ML", value: "ai/ml" },

    { label: "Theory", value: "theory" },

    { label: "Security", value: "security" },

    { label: "Visual Computing", value: "visual_computing" },

    { label: "Computer Architecture", value: "comp_arch" },

    { label: "Computational Biology", value: "comp_bio" },

    { label: "Design", value: "design" },
  ];

  const [value, setValue] = useState("");

  const handleChange = (event: {
    target: { value: SetStateAction<string> };
  }) => {
    setValue(event.target.value);
    props.setPathway(event.target.value.toString());
  };

  return (
    <div>
      <label>
        <select
          className="PathwayDropdown"
          value={value}
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
