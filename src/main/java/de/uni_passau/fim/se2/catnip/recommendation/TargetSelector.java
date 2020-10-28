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
        currentPercentage = minPercentage;
    }

    public TargetSelector() {
        currentPercentage = DEFAULT_PERCENTAGE;
    }

    public List<String> getViableTargetNames(String path) throws IOException, CsvException {
        CSVReader reader = new CSVReader(new FileReader(path));
        List<String[]> myEntries = reader.readAll();

        List<String> suitableProjects = new ArrayList<>();
        for (String[] currentEntry : myEntries) {
            //format of csv after converting taps with taps to csv converter
            if (!currentEntry[0].equals("projektname")) {
                String coverage = currentEntry[currentEntry.length - 1];
                String error = currentEntry[currentEntry.length - 2];
                String failed = currentEntry[currentEntry.length - 3];
                String passed = currentEntry[currentEntry.length - 4];

                double coverageNumber = Double.parseDouble(coverage);
                int errorNumber = Integer.parseInt(error);
                int failedNumber = Integer.parseInt(failed);
                int passedNumber = Integer.parseInt(passed);
                int numberOfTests = errorNumber + failedNumber + passedNumber;

                if ((double) passedNumber / (double) numberOfTests >= currentPercentage) {
                    suitableProjects.add(currentEntry[0]);
                }
            }
        }
        return suitableProjects;
    }
}
