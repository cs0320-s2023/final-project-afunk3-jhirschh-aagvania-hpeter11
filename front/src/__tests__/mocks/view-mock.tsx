import React, { useState } from "react";
import { ReturnContents, REPLFunction } from "../../functions/REPLFunction";
import deniro from "../../fixtures/deniro";
import numbered from "../../fixtures/numbered";
import small from "../../fixtures/small";
import stars from "../../fixtures/stars";
//import { currentFile } from "./load-mock";
import { fileLoaded } from "../../components/InputBox";

let commandMap = new Map<string, string[][]>([
  ["deniro.csv", deniro],
  ["numbered.csv", numbered],
  ["small.csv", small],
  ["stars.csv", stars],
]);

export const view_mock: REPLFunction = async function (
  args: Array<string>
): Promise<ReturnContents> {
  return new Promise((resolve, reject) => {
    if (!(args.length === 0)) {
      resolve({
        contents: [["Please do not include any parameters"]],
        table: false,
        ariaLabel: "view",
        ariaDescr: "Error: Parameters inputted where there should be none",
      });
    }

    let fileContents: string[][] | undefined = commandMap.get(fileLoaded);
    if (typeof fileContents === "undefined") {
      resolve({
        contents: [["Must load a file to view"]],
        table: false,
        ariaLabel: "view",
        ariaDescr: "Error: Must load a file to view",
      });
    } else {
      resolve({
        contents: fileContents,
        table: true,
        ariaLabel: "view",
        ariaDescr: "View CSV conducted",
      });
    }
  });
};
