import React, { useState } from "react";
import { ReturnContents, REPLFunction } from "../../functions/REPLFunction";

let commandSet = new Set<string>([
  "deniro.csv",
  "numbered.csv",
  "small.csv",
  "stars.csv",
]);

//export const [currentFile, setCurrentFile] = useState<string>("");

export const load_mock: REPLFunction = async function (
  args: Array<string>
): Promise<ReturnContents> {
  return new Promise((resolve, reject) => {
    if (!(args.length === 1)) {
      resolve({
        contents: [["Please input a filepath"]],
        table: false,
        ariaLabel: "load_file",
        ariaDescr: "Error: No filepath inputted",
      });
    }
    if (!commandSet.has(args[0])) {
      resolve({
        contents: [["file " + args[0] + " not found"]],
        table: false,
        ariaLabel: "load_file",
        ariaDescr: "Error: No filepath inputted",
      });
    }
    //setCurrentFile(args[0]);
    resolve({
      contents: [["File " + args[0] + " loaded"]],
      table: false,
      ariaLabel: "load_file",
      ariaDescr: "File " + args[0] + " loaded",
    });
  });
};
