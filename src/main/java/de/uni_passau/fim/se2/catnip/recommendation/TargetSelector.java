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

    public List<String> getViableTargetNamesByPercentage(String path, String sourceName) throws IOException, CsvException {
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

    public List<String> getViableTargetNamesIndividualBetter(String path, String sourceName) throws IOException, CsvException {
        CSVReader reader = new CSVReader(new FileReader(path));
        List<String[]> myEntries = reader.readAll();

        List<String> suitableProjects = new ArrayList<>();
        boolean hasSkip = false;
        String[] sourceEntry = null;
        for (String[] currentEntry : myEntries) {
            //format of csv after converting taps with taps to csv converter
            if (currentEntry[0].equals("projektname") || currentEntry[0].equals("projectname")) {
                if (currentEntry[currentEntry.length - 2].equals("skip")) {
                    hasSkip = true;
                }
            } else if (currentEntry[0].equals(sourceName)) {
                sourceEntry = currentEntry;
                break;
            }
        }
        if (sourceEntry == null) {
            return getViableTargetNamesByPercentage(path, sourceName);
        }
        for (String[] currentEntry : myEntries) {
            //format of csv after converting taps with taps to csv converter
            if (!currentEntry[0].equals("projektname") && !currentEntry[0].equals("projectname") && !currentEntry[0].equals(sourceName)) {
                if (isIndividuallyBetter(currentEntry, sourceEntry, hasSkip)) {
                    suitableProjects.add(currentEntry[0]);
                }
            }
        }
        if (suitableProjects.size() == 0) {
            return getViableTargetNamesByPercentage(path, sourceName);
        }
        return suitableProjects;
    }

    private boolean isIndividuallyBetter(String[] currentEntry, String[] sourceEntry, boolean hasSkip) {
        int passedPosition;
        if (hasSkip) {
            passedPosition = currentEntry.length - 5;
        } else {
            passedPosition = currentEntry.length - 4;
        }
        int currentPassed = Integer.parseInt(currentEntry[passedPosition]);
        int sourcePassed = Integer.parseInt(sourceEntry[passedPosition]);
        if (currentPassed <= sourcePassed) {
            return false;
        }
        for (int i = 1; i < passedPosition; i++) {
            if (sourceEntry[i].equals("pass") && !currentEntry[i].equals("pass")) {
                return false;
            }
        }
        return true;
    }
}
