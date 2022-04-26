# Catnip

Catnip is a next step recommendation generation tool for
[Scratch](https://scratch.mit.edu/) projects.

Catnip is developed at the
[Chair of Software Engineering II](https://www.fim.uni-passau.de/lehrstuhl-fuer-software-engineering-ii/)
and the [Didactics of Informatics](https://ddi.fim.uni-passau.de/) of the [University of Passau](https://www.uni-passau.de).

> This branch contains the version of Catnip as presented at ITiCSE 2021.
> For our ITiCSE 2022 paper (c.f. [Publications](#publications)), see the
> [branch `iticse2022`](https://github.com/se2p/catnip/tree/iticse2022).


## Building Catnip

Catnip is built using [Maven](https://maven.apache.org/). To
produce an executable jar-file, run the following command:

```
mvn package
```

This will produce `target/Catnip-1.0.jar`


## Using Catnip

To see an overview of the command line options available in Catnip type:

```
java -jar Catnip-1.0.jar --help
```

### Basic usage

Catnip computes hints for a Scratch project using the project in question, a pool of solutions and [Whisker](https://github.com/se2p/whisker-main) results for this pool in form of a csv file. Catnip is invoked as follows:

```
java -jar Catnip-1.0.jar --path <path/to/project.sb3> --target <path/to/pool/folder> --csv <path/to/Whisker/result.csv> --output <path/to/hint.csv> 
```

As a result, Catnip will report any hints in the csv file specified with the output parameter.

### Correctness Specification

The correctness that solutions in the pool have to fulfill to be selected in the hint generation process can be specified as well, otherwise 90% correctness is used:

```
java -jar Catnip-1.0.jar --path <path/to/project.sb3> --target <path/to/pool/folder> --csv <path/to/Whisker/result.csv> --output <path/to/hint.csv> --minpercentage 80
```

### Individual Results Method

Catnip has an alternative method of selecting suitable solutions out of the pool. Here the individual results of the candidate have to pass the same tests as the current student program and at least one more test.

```
java -jar Catnip-1.0.jar --path <path/to/project.sb3> --target <path/to/pool/folder> --csv <path/to/Whisker/result.csv> --output <path/to/hint.csv> --individual
```

## Publications

To learn more about Catnip and its hint generation technique, see the following papers:

- F. Obermüller, U. Heuer, and G. Fraser, “Guiding Next-Step Hint Generation Using Automated Tests”, in
  26th ACM Conference on Innovation and Technology in Computer Science Education V. 1 (ITiCSE 2021), ACM, 2021.
  https://doi.org/10.1145/3430665.3456344
  - code available on [branch `master`](https://github.com/se2p/catnip/tree/master)
- B. Fein, F. Obermüller, and G. Fraser, “Catnip: An Automated Hint Generation Tool for Scratch”, in
  27th ACM Conference on Innovation and Technology in Computer Science Education V. 1 (ITiCSE 2022), ACM, 2022.
  https://doi.org/10.1145/3502718.3524820
  - code available on [branch `iticse2022`](https://github.com/se2p/catnip/tree/iticse2022)

## Contributors

Gordon Fraser\
Florian Obermüller
