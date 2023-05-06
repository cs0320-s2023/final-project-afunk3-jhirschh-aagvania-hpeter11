export const generate = async function (
  gradYear: number,
  semClasses: String[][],
  optionTable: String[][],
  pathway1: String,
  pathway2: String
): Promise<Output> {
  // return a new promise with the proper output
  return new Promise<Output>((resolve, reject) => {
    // set up pathways to be inputted to the API
    let prefPaths: String[] = [];
    if (pathway1.length !== 0 && pathway1 !== "Select Pathway...") {
      prefPaths.push(pathway1);
    }
    if (
      pathway2.length !== 0 &&
      pathway2 !== "Select Pathway..." &&
      pathway1 !== pathway2
    ) {
      prefPaths.push(pathway2);
    }
    // set up schedule to be inputted to the API
    let courses: Course[] = [];
    for (var i = 0; i < semClasses.length; i++) {
      let year: number = gradYear - (semClasses.length - i + 1) * 0.5;
      let coursei: Course = {
        semester: {
          year: Math.floor(year) + 1,
          season: (year * 2) % 2 === 0 ? "Spring" : "Fall",
        },
        courses: semClasses[i],
      };
      courses.push(coursei);
    }

    // the JSON to be inputted into the API call
    const inputJson = {
      preferred: optionTable[2],
      undesirable: optionTable[0],
      preferredPathways: prefPaths,
      partialAssignment: courses,
    };

    // the request to be sent to the API server
    const request = new Request("http://localhost:3232/schedule", {
      method: "POST",
      headers: {
        Accept: "application/json",
      },
      body: JSON.stringify(inputJson),
    });
    // fetch the request and get the output JSON
    fetch(request)
      .then((response) => response.json())
      .then((data) => {
        // check to see if the output is properly formatted
        if (isResponse(data)) {
          // retrieve the pathways from the output JSON
          let outputPathway1: String = pathway1;
          let outputPathway2: String = pathway2;
          const dataPaths: Map<String, Array<String>> = new Map(
            Object.entries(data.pathways)
          );
          if (!dataPaths.has(outputPathway1)) {
            for (let [path] of dataPaths) {
              if (path !== outputPathway2) {
                outputPathway1 = path;
                break;
              }
            }
          }
          if (!dataPaths.has(outputPathway2)) {
            for (let [path] of dataPaths) {
              if (path !== outputPathway1) {
                outputPathway2 = path;
                break;
              }
            }
          }

          // retrieve the course schedule from the output JSON
          let outputSched: String[][] = [];
          let schedLen: number = data.schedule.length;
          for (var i = schedLen - 8; i < schedLen; i++) {
            outputSched.push(data.schedule[i].courses);
          }
          // resolve the promise with the pathways and schedule
          resolve({
            result: "Success",
            schedule: outputSched,
            pathway1: outputPathway1,
            pathway2: outputPathway2,
          });
        }
        // if the output from the API is improper, return an error message
        else {
          resolve({
            result: "Error",
            schedule: [],
            pathway1: "",
            pathway2: "",
          });
        }
      });
  });
};

// interface for the output schedule
export interface Output {
  result: String;
  schedule: String[][];
  pathway1: String;
  pathway2: String;
}

// interfaces for the input
interface Course {
  semester: SemesterInput;
  courses: String[];
}

interface SemesterInput {
  year: number;
  season: String;
}

// interfaces for the API return
interface SemData {
  year: number;
  season: String;
}

interface SemYear {
  semester: SemData;
  courses: Array<String>;
}

interface Response {
  result: String;
  pathways: Map<String, Array<String>>;
  schedule: Array<SemYear>;
}

// Type predicate for the overall API response
function isResponse(data: any): data is Response {
  if (!("result" in data)) {
    return false;
  }
  if (!("pathways" in data)) {
    return false;
  } else if (!(data.pathways instanceof Object)) {
    console.log(data.pathways);
    console.log("pathways isnt the proper form");
    return false;
  }
  if (!("schedule" in data)) {
    return false;
  } else {
    return data.schedule.every((i: any) => typeof isSemYear(i));
  }
}

// helper type predicate for each class in the schedule
function isSemYear(data: any): data is SemYear {
  if (!("courses" in data)) {
    return false;
  } else if (!data.courses.every((i: any) => i instanceof String)) {
    return false;
  }
  if (!("semester" in data)) {
    return false;
  } else if (!isSemData(data.semester)) {
    return false;
  }
  return true;
}

// helper type predicate for the semester information for each class
function isSemData(data: any): data is SemData {
  if (!("year" in data)) {
    return false;
  } else if (!(typeof data.year === "number")) {
    return false;
  }
  if (!("season" in data)) {
    return false;
  } else if (!(data.season instanceof String)) {
    return false;
  }
  return true;
}
