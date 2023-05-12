# CS Concentration Manager

Frontend work for CS32 final project.

## Design Choices

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

### Resources:

https://stackoverflow.com/questions/44263892/how-to-style-a-clicked-button-in-css
https://stackoverflow.com/questions/175739/how-can-i-check-if-a-string-is-a-valid-number
https://stackoverflow.com/questions/23437476/in-typescript-how-to-check-if-a-string-is-numeric
https://www.simplilearn.com/tutorials/reactjs-tutorial/how-to-create-functional-react-dropdown-menu
https://stackoverflow.com/questions/64573727/javascript-check-if-value-exists-in-2d-array-update-else-create
https://www.delftstack.com/howto/css/css-bring-to-front/
https://stackoverflow.com/questions/10659523/how-to-get-the-exact-local-time-of-client
