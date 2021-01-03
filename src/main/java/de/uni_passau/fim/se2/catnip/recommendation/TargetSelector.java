package de.uni_passau.fim.se2.catnip.recommendation;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TargetSelector {
    private static final double DEFAULT_PERCENTAGE = 0.9;
    private double currentPercentage;

    public TargetSelector(double minPercentage) {
        currentPercentage = minPercentage / 100;
    }

    public TargetSelector() {
        currentPercentage = DEFAULT_PERCENTAGE;
    }

    public List<String> getViableTargetNames(String path, String sourceName) throws IOException, CsvException {
        CSVReader reader = new CSVReader(new FileReader(path));
        List<String[]> myEntries = reader.readAll();

        List<String> suitableProjects = new ArrayList<>();
        boolean hasSkip = false;
        for (String[] currentEntry : myEntries) {
            //format of csv after converting taps with taps to csv converter
            if (currentEntry[0].equals("projektname") || currentEntry[0].equals("projectname")) {
                if (currentEntry[currentEntry.length - 2].equals("skip")) {
                    hasSkip = true;
                }
            }
            if (!currentEntry[0].equals("projektname") && !currentEntry[0].equals("projectname") && !currentEntry[0].equals(sourceName)) {
                String coverage;
                String error;
                String failed;
                String passed;
                String skip = "0";

                coverage = currentEntry[currentEntry.length - 1];
                if (hasSkip) {
                    skip = currentEntry[currentEntry.length - 2];
                    error = currentEntry[currentEntry.length - 3];
                    failed = currentEntry[currentEntry.length - 4];
                    passed = currentEntry[currentEntry.length - 5];
                } else {
                    error = currentEntry[currentEntry.length - 2];
                    failed = currentEntry[currentEntry.length - 3];
                    passed = currentEntry[currentEntry.length - 4];
                }

                double coverageNumber = Double.parseDouble(coverage);
                int errorNumber = Integer.parseInt(error);
                int failedNumber = Integer.parseInt(failed);
                int passedNumber = Integer.parseInt(passed);
                int skipNumber = Integer.parseInt(skip);
                int numberOfTests = errorNumber + failedNumber + passedNumber + skipNumber;

                double percentagePassed = (double) passedNumber / (double) numberOfTests;
                if (percentagePassed >= currentPercentage) {
                    suitableProjects.add(currentEntry[0]);
                }
            }
        }
        return suitableProjects;
    }
}
