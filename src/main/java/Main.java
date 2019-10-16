import org.apache.commons.cli.*;

public class Main {

    public static void main(String[] args) throws ParseException {
        Options options = new Options();

        options.addOption("path", true, "file path (required)");
        options.addOption("folder", true, "folder path to sample solutions and csv file" +
                " (required)");

        CommandLineParser parser = new DefaultParser();

        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("path") && cmd.hasOption("folder")) {

            //TODO

            return;
        }
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("Catnip", options);
        System.out.println("Example: " + "java -cp C:\\Catnip1.0.jar Main -path " +
                "C:\\scratchprojects\\files\\mySolution.sb3 -folder C:\\scratchprojects\\sampleSolutions");
    }
}
