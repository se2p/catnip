# Catnip

Catnip is a next step recommendation generation tool for
[Scratch](https://scratch.mit.edu/) projects.

Catnip is developed at the
[Chair of Software Engineering II](https://www.fim.uni-passau.de/lehrstuhl-fuer-software-engineering-ii/)
and the [Didactics of Informatics](https://ddi.fim.uni-passau.de/) of the [University of Passau](https://www.uni-passau.de).

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

Catnip computes hints for a Scratch project using a pool of solutions, [Whisker](https://github.com/se2p/whisker-main) results for this pool in form of a csv file and a correctness threshold. Catnip is invoked as follows:

```
java -jar Catnip-1.0.jar --path <path/to/project.sb3> --target <path/to/pool/folder> --csv <path/to/Whisker/result.csv> --output <path/to/hint.csv>  --minpercentage 80"
```

As a result, Catnip will report any hints in the csv file specified with the output parameter.

