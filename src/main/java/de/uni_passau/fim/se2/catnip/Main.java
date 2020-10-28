package de.uni_passau.fim.se2.catnip;

import com.opencsv.exceptions.CsvException;
import de.uni_passau.fim.se2.catnip.recommendation.HintGenerationTool;
import org.apache.commons.cli.*;

import java.io.IOException;

public class Main {
    private static final String SOURCE_PATH = "path";
    private static final String SOURCE_PATH_SHORT = "p";
    private static final String TARGET_PATH = "target";
    private static final String TARGET_PATH_SHORT = "t";
    private static final String CSV_PATH = "csv";
    private static final String CSV_PATH_SHORT = "c";
    private static final String HELP = "help";
    private static final String HELP_SHORT = "h";
    private static final String MINIMUM_PERCENTAGE = "minpercentage";
    private static final String MINIMUM_PERCENTAGE_SHORT = "m";

    private Main() {
    }

    public static void main(String[] args) {
        Options options = getCommandLineOptions();

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption(SOURCE_PATH) && cmd.hasOption(TARGET_PATH) && cmd.hasOption(CSV_PATH)) {
                HintGenerationTool hintGenerationTool;
                if (cmd.hasOption(MINIMUM_PERCENTAGE)) {
                    double percentage = Double.parseDouble(cmd.getOptionValue(MINIMUM_PERCENTAGE));
                    hintGenerationTool = new HintGenerationTool(cmd.getOptionValue(SOURCE_PATH), cmd.getOptionValue(TARGET_PATH), cmd.getOptionValue(CSV_PATH), percentage);
                } else {
                    hintGenerationTool = new HintGenerationTool(cmd.getOptionValue(SOURCE_PATH), cmd.getOptionValue(TARGET_PATH), cmd.getOptionValue(CSV_PATH));
                }

                hintGenerationTool.generateHints();
                return;
            }
            printHelp();
        } catch (ParseException parseException) {
            System.err.println("Invalid option: " + parseException.getMessage());
            printHelp();
        }
    }

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("Catnip", getCommandLineOptions(), true);
        System.out.println("Example: " + "java -jar Catnip.jar -path "
                + "C:\\scratchprojects\\files\\mySolution.sb3 -target C:\\scratchprojects\\sampleSolutions -csv -folder C:\\scratchprojects\\sampleSolutions\\results.csv -m 80");
    }

    private static Options getCommandLineOptions() {
        Options options = new Options();

        options.addOption(SOURCE_PATH_SHORT, SOURCE_PATH, true, "file path (required)");
        options.addOption(TARGET_PATH_SHORT, TARGET_PATH, true, "folder path to sample solutions (required)");
        options.addOption(CSV_PATH_SHORT, CSV_PATH, true, "path to sample csv file (required)");
        options.addOption(MINIMUM_PERCENTAGE_SHORT, MINIMUM_PERCENTAGE, true, "minimum percentage of successful tests a target needs to be considered (default 90)");
        options.addOption(HELP_SHORT, HELP, false, "print this message");

        return options;
    }
}
