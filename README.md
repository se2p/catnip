# Catnip

Catnip is a next step recommendation generation tool for [Scratch][scratch]
projects.

Catnip is developed at the [Chair of Software Engineering II][se2p]
and the [Didactics of Informatics][ddi] of the [University of Passau][uni-passau].

> This branch contains the version of Catnip as presented at ITiCSE 2022.
> For our ITiCSE 2021 paper (c.f. [Publications](#publications)), see the
> [branch `master`](https://github.com/se2p/catnip/tree/master).

> Checkout with `git clone --recursive` to clone the `scratch-gui` and `whisker`
submodules.


## Running The Hint Generator

### Using `docker-compose`

- In `build_containers.sh` change the path to a **Java 11** JDK.
- Run `build_containers.sh` to
  - apply the necessary patches to Whisker and the Scratch GUI
  - Build the Docker containers for the GUI and the backend modules.
- Add some solution `sb3`-programs to the `run_conf/tasks/.../solutions/` folder.
- Adapt the settings in `run_conf/application.conf`.
- `docker-compose up` to start the containers.
- The GUI can be reached on `http://localhost:8080/`.
- `Ctrl + C` stops the containers, they can then be removed with
  `docker-compose down`.

#### With Whisker

- In `run_conf/application.conf` enable the option `runTestSuite`.
- In `docker_compose.yml` uncomment/adapt the paths to the Whisker tests file
  and the Whisker folder.


### Without Docker

#### Server
- Run `./sbtx -java-home $JDK11 serve/assembly` where `$JDK11` is the path to
  some **Java 11** JDK.
- The runnable JAR file is placed in `serve/target/scala-2.13/hintgen-serve.jar`.
  Copy it into `run_conf`.
- Adapt the settings in `run_conf/application.conf`.
  - Especially the settings `receivedProgramsDir`, `tasksBasePath`, and `tasks`
    have to be adapted.
  - You can ignore the `whisker…` settings if `runTestSuite` is `false`.
- Start the server with
  ```shell
  cd run_conf
  $JDK11/bin/java -Dconfig.file=application.conf -Dplay.http.secret.key="…" -jar hintgen-serve.jar
  ```

#### GUI
- Adapt the URL to the server component in `run_conf/server-info.txt`.
- Run the same commands as in the GUI-section of `build_containers.sh`.
  - Instead of running the `docker` command, place the content of the
    `scratch_gui/build` directory into a folder that is served by some HTTP
    server like nginx or Apache2.


## Project Structure

- `algorithm`: the actual implementation of the hint generator.
- `serve`: the backend server connecting the GUI to the algorithm.
- `run_conf`: configuration files for the server components.
- `example_programs`: sb3 files used for unit-testing the various components.
- `scratch-gui`, `whisker`: git submodules containing the respective projects.
- `program_fixer.py`: The comments added by the hint generator to the programs
  sometimes cannot be removed properly when copying blocks during application of
  the hints. This results in an invalid program that neither LitterBox nor the
  scratch-vm itself can parse.

  This program removes all comments from a program to make it usable again.
  Takes the filename of the program as first argument.


# Publications

To learn more about Catnip and its hint generation technique, see the following papers:

- F. Obermüller, U. Heuer, and G. Fraser, “Guiding Next-Step Hint Generation Using Automated Tests”, in
  26th ACM Conference on Innovation and Technology in Computer Science Education V. 1 (ITiCSE 2021), ACM, 2021.
  https://doi.org/10.1145/3430665.3456344
  - code available on [branch `master`](https://github.com/se2p/catnip/tree/master)
- B. Fein, F. Obermüller, and G. Fraser, “Catnip: An Automated Hint Generation Tool for Scratch”, in
  27th ACM Conference on Innovation and Technology in Computer Science Education V. 1 (ITiCSE 2022), ACM, 2022.
  https://doi.org/10.1145/3502718.3524820
  - code available on [branch `iticse2022`](https://github.com/se2p/catnip/tree/iticse2022)


# Contributors
Benedikt Fein\
Gordon Fraser\
Florian Obermüller


# Licence Information
- Packaged with the [APTED][apted] algorithm (`algorithm/lib/apted.jar`),
  published under MIT licence.
  No source code has been changed.

## Modules `algorithm`, `common`
Licenced under the EUPL, Version 1.2 or – as soon as they will be approved by
the European Commission - subsequent versions of the EUPL (the ‘Licence’).
You may not use this work except in compliance with the Licence.
You may obtain a copy of the Licence at:

https://joinup.ec.europa.eu/software/page/eupl

Unless required by applicable law or agreed to in writing, software distributed
under the Licence is distributed on an ‘AS IS’ basis, WITHOUT WARRANTIES OR
CONDITIONS OF ANY KIND, either expressed or implied.
See the Licence for the specific language governing permissions and limitations
under the Licence.

## Module `serve`
This module contains the class `CustomScratchBlocksVisitor` that is directly
based on the source of the [LitterBox][litterbox] class `ScratchBlocksVisitor`
as it can be found in LitterBox version 1.5.

Therefore, this module is licensed GPL-3.0 identically to LitterBox.
You may use, distribute and copy it under those licence terms.


[apted]: https://github.com/DatabaseGroup/apted
[ddi]: https://ddi.fim.uni-passau.de/
[litterbox]: https://github.com/se2p/LitterBox/
[uni-passau]: https://www.uni-passau.de
[scratch]: https://scratch.mit.edu/
[se2p]: https://www.fim.uni-passau.de/lehrstuhl-fuer-software-engineering-ii/
