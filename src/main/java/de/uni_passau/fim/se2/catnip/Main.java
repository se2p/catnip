package de.uni_passau.fim.se2.catnip;

import org.apache.commons.cli.*;

public class Main {
    private static final String SOURCE_PATH = "path";
    private static final String SOURCE_PATH_SHORT = "p";
    private static final String TARGET_PATH = "target";
    private static final String TARGET_PATH_SHORT = "t";
    private static final String CSV_PATH = "csv";
    private static final String CSV_PATH_SHORT = "c";
    private static final String HELP = "help";
    private static final String HELP_SHORT = "h";

    private Main() {
    }

    public static void main(String[] args) {
        Options options = getCommandLineOptions();

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption(SOURCE_PATH) && cmd.hasOption(TARGET_PATH) && cmd.hasOption(CSV_PATH)) {

                //TODO

                return;
            }
            printHelp();
        } catch (ParseException parseException) {
            System.err.println("Invalid option: " + parseException.getMessage());
            printHelp();
        }
    }

    static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("Catnip", getCommandLineOptions(), true);
        System.out.println("Example: " + "java -jar Catnip.jar -path "
                + "C:\\scratchprojects\\files\\mySolution.sb3 -target C:\\scratchprojects\\sampleSolutions -csv -folder C:\\scratchprojects\\sampleSolutions\\results.csv");
    }

    static Options getCommandLineOptions() {
        Options options = new Options();

        options.addOption(SOURCE_PATH_SHORT, SOURCE_PATH, true, "file path (required)");
        options.addOption(TARGET_PATH_SHORT, TARGET_PATH, true, "folder path to sample solutions (required)");
        options.addOption(CSV_PATH_SHORT, CSV_PATH, true, "path to sample csv file (required)");
        options.addOption(HELP_SHORT, HELP, false, "print this message");

        return options;
    }
}
