export const generate = async function (
  gradYear: number,
  semClasses: String[][],
  optionTable: String[][],
  pathway1: String,
  pathway2: String
): Promise<String[][]> {
  return new Promise<String[][]>((resolve, reject) => {
    // set up pathways
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
    // set up schedule
    let courses: Course[] = [];
    for (var i = 0; i < semClasses.length; i++) {
      let year: number = gradYear - (semClasses.length - i);
      let coursei: Course = {
        semester: {
          year: Math.floor(year),
          season: (year * 2) % 2 === 0 ? "Spring" : "Fall",
        },
        courses: semClasses[i],
      };
    }

    const inputJson = {
      preferred: optionTable[2],
      undesirable: optionTable[0],
      preferredPathways: prefPaths,
      partialAssignment: courses,
    };
    const request = new Request("http://localhost:3232/schedule", {
      method: "GET",
      body: JSON.stringify(inputJson),
    });
    fetch(request)
      .then((response) => response.json())
      .then((data) => {
        console.log(data);
      });
  });
};

interface Course {
  semester: Semester;
  courses: String[];
}

interface Semester {
  year: number;
  season: String;
}
