package de.uni_passau.fim.se2.catnip;

import de.uni_passau.fim.se2.catnip.recommendation.Hint;
import de.uni_passau.fim.se2.catnip.recommendation.HintGenerationTool;
import de.uni_passau.fim.se2.catnip.util.CSVWriter;
import org.apache.commons.cli.*;

public class Main {
    private static final String SOURCE_PATH = "path";
    private static final String SOURCE_PATH_SHORT = "p";
    private static final String TARGET_PATH = "target";
    private static final String TARGET_PATH_SHORT = "t";
    private static final String CSV_PATH = "csv";
    private static final String CSV_PATH_SHORT = "c";
    private static final String OUTPUT_PATH = "output";
    private static final String OUTPUT_PATH_SHORT = "o";
    private static final String HELP = "help";
    private static final String HELP_SHORT = "h";
    private static final String MINIMUM_PERCENTAGE = "minpercentage";
    private static final String MINIMUM_PERCENTAGE_SHORT = "m";
    private static final String INDIVIDUAL_BETTER = "individual";
    private static final String INDIVIDUAL_BETTER_SHORT = "i";

    private Main() {
    }

    public static void main(String[] args) {
        Options options = getCommandLineOptions();

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption(SOURCE_PATH) && cmd.hasOption(TARGET_PATH) && cmd.hasOption(CSV_PATH) && cmd.hasOption(OUTPUT_PATH)) {
                HintGenerationTool hintGenerationTool;
                boolean individual = false;
                if (cmd.hasOption(INDIVIDUAL_BETTER)) {
                    individual = true;
                }
                if (cmd.hasOption(MINIMUM_PERCENTAGE)) {
                    double percentage = Double.parseDouble(cmd.getOptionValue(MINIMUM_PERCENTAGE));
                    hintGenerationTool = new HintGenerationTool(cmd.getOptionValue(SOURCE_PATH), cmd.getOptionValue(TARGET_PATH), cmd.getOptionValue(CSV_PATH), individual, percentage);
                } else {
                    hintGenerationTool = new HintGenerationTool(cmd.getOptionValue(SOURCE_PATH), cmd.getOptionValue(TARGET_PATH), cmd.getOptionValue(CSV_PATH), individual);
                }

                Hint hint = hintGenerationTool.generateHints();
                CSVWriter.printHints(cmd.getOptionValue(OUTPUT_PATH), hint);
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
                + "C:\\scratchprojects\\files\\mySolution.sb3 -target C:\\scratchprojects\\sampleSolutions -csv C:\\scratchprojects\\sampleSolutions\\results.csv -output C:\\scratchprojects\\files\\mySolution_hints.csv  -m 80");
    }

    private static Options getCommandLineOptions() {
        Options options = new Options();

        options.addOption(SOURCE_PATH_SHORT, SOURCE_PATH, true, "file path (required)");
        options.addOption(TARGET_PATH_SHORT, TARGET_PATH, true, "folder path to sample solutions (required)");
        options.addOption(CSV_PATH_SHORT, CSV_PATH, true, "path to csv file containing WHISKER results (required)");
        options.addOption(OUTPUT_PATH_SHORT, OUTPUT_PATH, true, "output path to csv file with hints created (required)");
        options.addOption(MINIMUM_PERCENTAGE_SHORT, MINIMUM_PERCENTAGE, true, "minimum percentage of successful tests a target needs to be considered (default 90)");
        options.addOption(INDIVIDUAL_BETTER_SHORT, INDIVIDUAL_BETTER, false, "individually better projects are chosen as possible targets");
        options.addOption(HELP_SHORT, HELP, false, "print this message");

        return options;
    }
}
