# CS Concentration Manager (Backend)

## Design Choices

The codebase is split into three sub-packages:

- `data` – all records and enums used to formalize the course requirements.
- `handlers` – API handlers, request and response objects.
- `solver` – wrapper around a CP-solver, solver parameters.

For more information, please refer to the JavaDoc comments for each of the classes.

Lastly, the solver is using the database which is stored in multiple JSON files in `data` directory
in the root of the backend.

## Testing

We feature two test types: API tests, which ensure that correct responses are returned on partially
malformed data, and solver fuzz tests, which check whether the suggested schedule actually
corresponds to the specification.

## Deployment

By default, the single `/schedule` endpoint is exposed after the server is run on port `3232`. Here
are typical server requests and responses.

### Server Request

Notice: all semesters for which the schedule should be generated, even if contain no pre-selected
courses, should be submitted in the request. It's not necessary to submit all 8 semesters, the
solver would try to fit the requirements into however few semesters required.

```json
{
  "preferred": [
    "CSCI0170"
  ],
  "undesirable": [
    "CSCI0020"
  ],
  "preferredPathways": [
    "Systems"
  ],
  "partialAssignment": [
    {
      "semester": {
        "year": 2024,
        "season": "Spring"
      },
      "courses": [
        "CSCI0220"
      ],
      "frozen": false
    },
    {
      "semester": {
        "year": 2024,
        "season": "Fall"
      },
      "courses": [],
      "frozen": false
    },
    {
      "semester": {
        "year": 2025,
        "season": "Spring"
      },
      "courses": [],
      "frozen": false
    },
    {
      "semester": {
        "year": 2025,
        "season": "Fall"
      },
      "courses": [],
      "frozen": false
    },
    {
      "semester": {
        "year": 2026,
        "season": "Spring"
      },
      "courses": [],
      "frozen": false
    },
    {
      "semester": {
        "year": 2026,
        "season": "Fall"
      },
      "courses": [],
      "frozen": false
    },
    {
      "semester": {
        "year": 2027,
        "season": "Spring"
      },
      "courses": [],
      "frozen": false
    }
  ]
}
```

### Server Response

```json
{
  "result": "success",
  "schedule": [
    {
      "semester": {
        "year": 2024,
        "season": "Spring"
      },
      "courses": [
        "CSCI0220",
        "CSCI1040",
        "MATH0540",
        "APMA1655"
      ],
      "frozen": false
    },
    {
      "semester": {
        "year": 2024,
        "season": "Fall"
      },
      "courses": [
        "CSCI0170",
        "CSCI1460",
        "MATH0520",
        "APMA0360"
      ],
      "frozen": false
    },
    {
      "semester": {
        "year": 2025,
        "season": "Spring"
      },
      "courses": [
        "CSCI0111",
        "CSCI0200",
        "CSCI1440",
        "CSCI1951-T"
      ],
      "frozen": false
    },
    {
      "semester": {
        "year": 2025,
        "season": "Fall"
      },
      "courses": [
        "CSCI0320",
        "CSCI1230",
        "CSCI1270",
        "CSCI1710"
      ],
      "frozen": false
    },
    {
      "semester": {
        "year": 2026,
        "season": "Spring"
      },
      "courses": [
        "CSCI0300",
        "CSCI1430",
        "CSCI1950-U",
        "APMA0350"
      ],
      "frozen": false
    },
    {
      "semester": {
        "year": 2026,
        "season": "Fall"
      },
      "courses": [
        "CSCI1010",
        "CSCI1410",
        "CSCI1810",
        "MATH0350"
      ],
      "frozen": false
    },
    {
      "semester": {
        "year": 2027,
        "season": "Spring"
      },
      "courses": [
        "CSCI1380",
        "CSCI1420",
        "CSCI1570",
        "CSCI1951-I"
      ],
      "frozen": false
    }
  ],
  "pathways": {
    "Software Principles": [
      "CSCI1710",
      "CSCI1951-T"
    ],
    "Systems": [
      "CSCI1270",
      "CSCI1380"
    ]
  }
}
```