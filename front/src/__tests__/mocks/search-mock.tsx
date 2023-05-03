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

export const search_mock: REPLFunction = async function (
  args: Array<string>
): Promise<ReturnContents> {
  return new Promise((resolve, reject) => {
    if (!(args.length === 2 || args.length === 4)) {
      resolve({
        contents: [["Invalid number of arguments!"]],
        table: false,
        ariaLabel: "search",
        ariaDescr: "Error: invalid number of arguments",
      });
    }

    let fileContents: string[][] | undefined = commandMap.get(fileLoaded);
    if (typeof fileContents === "undefined") {
      resolve({
        contents: [["Must load a file to search"]],
        table: false,
        ariaLabel: "search",
        ariaDescr: "Error: Must load a file to search",
      });
    } else {
      if (
        (args[0] === "0") && 
        (args[1] === "true") && 
        (args[2] === "hello") && 
        (args[3] === "0")) {
          resolve({
            contents: [
              [
                "for coltype, input COLUMN_INDEX to search by column index, COLUMN_NAME to search by column name",
              ],
            ],
            table: false,
            ariaLabel: "search",
            ariaDescr:
              "Error: for coltype, input COLUMN_INDEX to search by column index, COLUMN_NAME to search by column name",
          });
        };
      if (
        args[0] === "Student" &&
        args[1] === "true" &&
        args[2] === "COLUMN_HEADER" &&
        args[3] === "Department"
      ) {
        resolve({
          contents: [["query is not in the CSV"]],
          table: false,
          ariaLabel: "search",
          ariaDescr: "query is not in the CSV",
        });
      }
        let searchResults = fileContents.filter((r) =>
          r.some((e) => e === args[0])
        );
      resolve({
        contents: searchResults,
        table: true,
        ariaLabel: "search",
        ariaDescr: "Search performed",
      });
    }
  });
};
