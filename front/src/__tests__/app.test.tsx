import React, { useState } from "react";
// Lets us render a component and send queries
import { render, screen } from "@testing-library/react";
// Lets us send user events
import userEvent from "@testing-library/user-event";
// Lets us check whether an element is within another element
import { within } from "@testing-library/dom";
// Lets us use 'toBeInTheDocument()'
import "@testing-library/jest-dom";

import App from "../App";
import { addCommand, deleteCommand, commandInMap, fileLoaded } from "../components/InputBox";
import { load_mock } from "./mocks/load-mock";
import { view_mock } from "./mocks/view-mock";
import { search_mock } from "./mocks/search-mock";
import small from "../fixtures/small";
import numbered from "../fixtures/numbered";

beforeEach(() => {
  render(<App />);

  deleteCommand("load_file");
  addCommand("load_file", load_mock);
  deleteCommand("view");
  addCommand("view", view_mock);
  deleteCommand("search");
  addCommand("search", search_mock);
});


test("add command works", async () => {
  expect(commandInMap("load_file")).toBe(true);
  expect(commandInMap("view")).toBe(true);
  expect(commandInMap("search")).toBe(true);
  expect(commandInMap("mock_invalid")).toBe(false);
});

// testing the commands are removed from the map correctly
test("delete command wokrs", async () => {
  deleteCommand("view");
  expect(commandInMap("view")).toBe(false);
  deleteCommand("search")
  expect(commandInMap("search")).toBe(false);
});


test("renders input box, history box, and submit button", async () => {
  const InputBox = screen.getByRole("textbox", {
    name: "Input Box",
  });
  const Button = screen.getByRole("button", {
    name: "Input Button",
  });
  const HistoryBox = screen.getByLabelText("History Box");

  expect(InputBox).toBeInTheDocument();
  expect(Button).toBeInTheDocument();
  expect(HistoryBox).toBeInTheDocument();
});




test("mode change works", async () => {
  const InputBox = screen.getByRole("textbox", {
    name: "Input Box",
  });
  const Button = screen.getByRole("button", {
    name: "Input Button",
  });
  let user = userEvent.setup();
  await user.type(InputBox, "mode");
  await user.keyboard("[Enter]");

  const ModeCall = screen.getByLabelText("mode");
  expect(await ModeCall).toBeInTheDocument();
  expect(
    await screen.findByText("mode switched to verbose")
  ).toBeInTheDocument();
  const VerboseLine1 = screen.getByLabelText("verbose first line");
  expect(await VerboseLine1).toBeInTheDocument();
  const VerboseLine2 = screen.getByLabelText("verbose second line");
  expect(await VerboseLine2).toBeInTheDocument();

  await user.type(InputBox, "mode");
  await user.click(Button);

  const ModeCalls = screen.getAllByLabelText("mode");
  expect(ModeCalls.length).toBe(2);
  expect(await ModeCalls[1]).toBeInTheDocument();
  expect(await screen.findByText("mode switched to brief")).toBeInTheDocument();
});




test("load_file command: no file, valid file, invalid file", async () => {
  const InputBox = screen.getByRole("textbox", { name: "Input Box" });
  const Button = screen.getByRole("button", { name: "Input Button" });

  let user = userEvent.setup();
  await user.type(InputBox, "load_file");
  await user.click(Button);

  const LoadCall = screen.getByLabelText("load_file");
  expect(await LoadCall).toBeInTheDocument();
  expect(
    await screen.findByText("Please input a filepath")
  ).toBeInTheDocument();

  await user.type(InputBox, "load_file deniro.csv");
  await user.click(Button);
  await user.type(InputBox, "load_file nothing.csv");
  await user.keyboard("[Enter]");

  const LoadCalls = screen.getAllByLabelText("load_file");
  expect(LoadCalls.length).toBe(3);
  expect(await LoadCalls[1]).toBeInTheDocument();
  expect(await LoadCalls[2]).toBeInTheDocument();
  expect(await screen.findByText("File deniro.csv loaded")).toBeInTheDocument();
  expect(
    await screen.findByText("file nothing.csv not found")
  ).toBeInTheDocument();
});




test("view command: no file, valid file, improper parameters", async () => {
  const InputBox = screen.getByRole("textbox", { name: "Input Box" });
  const Button = screen.getByRole("button", { name: "Input Button" });

  let user = userEvent.setup();
  await user.type(InputBox, "view");
  await user.click(Button);

  const ViewCall = screen.getByLabelText("view");
  expect(await ViewCall).toBeInTheDocument();
  expect(
    await screen.findByText("Must load a file to view")
  ).toBeInTheDocument();

  await user.type(InputBox, "load_file small.csv");
  await user.click(Button);
  await user.type(InputBox, "view error here");
  await user.click(Button);
  await user.type(InputBox, "view");
  await user.click(Button);
  

  const ViewCalls = screen.getAllByLabelText("view");
  expect(ViewCalls.length).toBe(3);
  expect(await ViewCalls[1]).toBeInTheDocument();
  expect(await ViewCalls[2]).toBeInTheDocument();
  const ViewTable = screen.getByRole("table");
  expect(ViewTable).toBeInTheDocument;
  for (const r of small) {
    for (const e of r) {
      expect(await ViewTable).toContainElement((await screen.findAllByText(e))[0]);
    }
  }
  expect(
    await screen.findByText("Please do not include any parameters")
  ).toBeInTheDocument();
});




test("search command: no file, errored search, successful search, failed search", async () => {
  const InputBox = screen.getByRole("textbox", { name: "Input Box" });
  const Button = screen.getByRole("button", { name: "Input Button" });

  let user = userEvent.setup();
  await user.type(InputBox, "search");
  await user.click(Button);

  const SearchCall = screen.getByLabelText("search");
  expect(await SearchCall).toBeInTheDocument();
  expect(
    await screen.findByText("Must load a file to search")
  ).toBeInTheDocument();

  await user.type(InputBox, "load_file small.csv");
  await user.click(Button);
  await user.type(InputBox, "search error");
  await user.keyboard("[Enter]");
  expect(await fileLoaded).toBe("small.csv")
  await user.type(InputBox, "search 0 true hello 0");
  await user.click(Button);
  await user.type(InputBox, "search Student true COLUMN_INDEX 0");
  await user.click(Button);
  await user.type(InputBox, "search Student true COLUMN_HEADER Department");
  await user.click(Button);

  const ViewCalls = screen.getAllByLabelText("search");

  expect(ViewCalls.length).toBe(5);
  expect(await ViewCalls[1]).toBeInTheDocument();
  expect(await ViewCalls[2]).toBeInTheDocument();
  expect(await ViewCalls[3]).toBeInTheDocument();
  expect(await ViewCalls[4]).toBeInTheDocument();
  const ViewTable = screen.getByRole("table");
  expect(ViewTable).toBeInTheDocument;

  for (const r of small[3]) {
      expect(await ViewTable).toContainElement(
        (await screen.findAllByText(r))[0]
      );
  }
  expect(
    await screen.findByText(
      "for coltype, input COLUMN_INDEX to search by column index, COLUMN_NAME to search by column name"
    )
  ).toBeInTheDocument();
  expect(
    await screen.findByText("query is not in the CSV")
  ).toBeInTheDocument();
});




test("interactions: load a file, view, load another file, view", async () => {
  const InputBox = screen.getByRole("textbox", { name: "Input Box" });
  const Button = screen.getByRole("button", { name: "Input Button" });

  let user = userEvent.setup();
  await user.type(InputBox, "load_file small.csv");
  await user.click(Button);

    expect(await fileLoaded).toBe("small.csv");

  const LoadCall = screen.getByLabelText("load_file");
  expect(await LoadCall).toBeInTheDocument();
  expect(await screen.findByText("File small.csv loaded")).toBeInTheDocument();

  await user.type(InputBox, "view");
  await user.click(Button);

  const ViewCall = screen.getByLabelText("view");
  expect(ViewCall).toBeInTheDocument;
  for (const r of small) {
    for (const e of r) {
      expect(await ViewCall).toContainElement((await screen.findAllByText(e))[0]);
    }
  }

  await user.type(InputBox, "load_file numbered.csv");
  await user.click(Button);
  await user.type(InputBox, "view");
  await user.click(Button);

    expect(await fileLoaded).toBe("numbered.csv");

  const ViewTables = screen.getAllByLabelText("view");
  expect(ViewTables[1]).toBeInTheDocument;
  for (const r of numbered) {
    for (const e of r) {
      expect(await ViewTables[1]).toContainElement(
        (await screen.findAllByText(e))[0]
      );
    }
  }
})




test("interactions: load a file, search, load another file, view", async () => {
  const InputBox = screen.getByRole("textbox", { name: "Input Box" });
  const Button = screen.getByRole("button", { name: "Input Button" });

  let user = userEvent.setup();
  await user.type(InputBox, "load_file numbered.csv");
  await user.click(Button);
  await user.type(InputBox, "search blub false");
  await user.click(Button);
  
  expect(await fileLoaded).toBe("numbered.csv");

  const SearchCall = screen.getByLabelText("search");
  expect(SearchCall).toBeInTheDocument;
  for (const r of numbered[0]) {
      expect(await SearchCall).toContainElement(
        (await screen.findAllByText(r))[0]
      );
  }

  await user.type(InputBox, "load_file small.csv");
  await user.click(Button);
  await user.type(InputBox, "view");
  await user.click(Button);

  const ViewCall = screen.getByLabelText("view");
  expect(ViewCall).toBeInTheDocument;
  for (const r of small) {
    for (const e of r) {
      expect(await ViewCall).toContainElement(
        (await screen.findAllByText(e))[0]
      );
    }
  }
})




test("interactions: load a file, search, load another file, search", async () => {
  const InputBox = screen.getByRole("textbox", { name: "Input Box" });
  const Button = screen.getByRole("button", { name: "Input Button" });

  let user = userEvent.setup();
  await user.type(InputBox, "load_file numbered.csv");
  await user.click(Button);
  await user.type(InputBox, "search blub false");
  await user.click(Button);

  expect(await fileLoaded).toBe("numbered.csv");

  const SearchCall = screen.getByLabelText("search");
  expect(SearchCall).toBeInTheDocument;
  for (const r of numbered[0]) {
    expect(await SearchCall).toContainElement(
      (await screen.findAllByText(r))[0]
    );
  }

  await user.type(InputBox, "load_file small.csv");
  await user.click(Button);
  await user.type(InputBox, "search Professor true COLUMN_INDEX 0");
  await user.click(Button);

  expect(await fileLoaded).toBe("small.csv");

  const SearchCalls = screen.getAllByLabelText("search");
  expect(SearchCalls[1]).toBeInTheDocument;
  for (const r of small[2]) {
    expect(await SearchCalls[1]).toContainElement(
      (await screen.findAllByText(r))[0]
    );
  }

})