# CS Concentration Manager

Frontend work for CS32 final project.

## Design Choices and Organization

The front end of this project is split into multiple directories. The main
section contains `App.tsx`, which contains the outlying main functionality of
the web app. The `styles` directory contains the main CSS styling for the site.
The `components` directory contains the functionality for the different parts
of the site. Within the `components` directory is the `Header.tsx` class, which
just contains the header for the site. There are three more subdirectories here.
The first is `actions`, which contains files relating to generating and
exporting the schedule, as well as looking for webiste information. The second
is `preferences`, which contains functionality related to logging graduation
year and pathways, as well as searching for courses and designating courses
outside of the schedule. The last subcirectory is `schedule`, which contains
the schedule and all functionality relating to it.

We use a lot of global useStates for this site, stored in `App.tsx`. We did this
because most of the information used in this site had to be passed between many
different parts, meaning that the useStates relating to them could not be stored
locally in individual files. Storing the useStates in the outer file meant that
they could affect and be changed by different parts of the site, even though
we were still able to keep the parts separate.

One main data structure we implemented was a 2-d array of Strings, which we used
for the course schedule. This allowed us to store each semester as a sublist,
and keep the classes in different semester separated but still connected.

## Testing

The main testing for the front end come in the `app.test.tsx` file.

### How to run the tests

To run the tests, input "npm run test" into the terminal

## How to run the program

To start the site, open the server in the Java backend code, and type "npm run
dev" into the terminal after cding into the "front" directory.

Once the site is open, you can click on the "Info" button to see tips on how to
use the site.

- You can input your graduation year in the graduation year input box. This will
  be update in the course schedule
- You can input your pathways into the two pathway dropdown menus. These will
  be incorporated into the generation of the optimal schedule.
- You can search for courses in the course search bar.

Once you have searched for a course and selected in the dropdown menu, there are
two places you can put it:

1. The option table. There are three categories: Take, Avoid, and Prerequisite.
   If there is a course you want your schedule to avoid, click the Avoid button to
   add it to the list. Take similar actions for the Take category. If there is a
   course credit you have completed before coming to college, input that course
   into the Prerequisites box
2. The schedule. This will be filled out when you generate a schedule, but
   before you do that you can also add classes manually, either to mark that you
   have already completed a class in a previous semester, or to indicate that you
   wish to take that class in a specific future semester

Once you have filled out all the classes you wish to before submitting, click
on the "Generate" button, and the site will return a schedule that satisfies your
pathways, includes all the classes you wish to take while avoiding the ones you
wish to avoid, and recommends no more than three classes in any future semester.

If you want to export the schedule to CSV, click the "Export" button

### Outside Resources:

https://stackoverflow.com/questions/44263892/how-to-style-a-clicked-button-in-css
https://stackoverflow.com/questions/175739/how-can-i-check-if-a-string-is-a-valid-number
https://stackoverflow.com/questions/23437476/in-typescript-how-to-check-if-a-string-is-numeric
https://www.simplilearn.com/tutorials/reactjs-tutorial/how-to-create-functional-react-dropdown-menu
https://stackoverflow.com/questions/64573727/javascript-check-if-value-exists-in-2d-array-update-else-create
https://www.delftstack.com/howto/css/css-bring-to-front/
https://stackoverflow.com/questions/10659523/how-to-get-the-exact-local-time-of-client
