package de.uni_passau.fim.se2.catnip.util;

import de.uni_passau.fim.se2.catnip.pq_gram.Label;
import de.uni_passau.fim.se2.catnip.recommendation.Hint;
import de.uni_passau.fim.se2.catnip.recommendation.Recommendation;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public abstract class CSVWriter {

    public static void printHints(String csvPath, Hint hint) {
        try {
            CSVPrinter printer = getNewPrinter(csvPath);
            for (Recommendation recommendation : hint.getRecommendations()) {
                List<String> output = generateOutputFromHint(recommendation, hint.getProgram());
                printer.printRecord(output);
                printer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> generateOutputFromHint(Recommendation hint, Program program) {
        List<String> hintAsList = new ArrayList<>();
        hintAsList.add(hint.getActor().getIdent().getName());
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor();
        visitor.setProgram(program);
        visitor.setCurrentActor(hint.getActor());
        visitor.begin();
        if (hint.getProcedure() != null) {
            hint.getProcedure().accept(visitor);
        } else {
            hint.getScript().accept(visitor);
        }
        visitor.end();
        hintAsList.add(visitor.getScratchBlocks().stripTrailing());
        hintAsList.add(hint.getAffectedNode().getLabel());
        hintAsList.add(String.valueOf(hint.isAddition()));
        hintAsList.add(createStringForMultipleNodes(hint.getPreviousNodes()));
        hintAsList.add(createStringForMultipleNodes(hint.getFollowingNodes()));
        hintAsList.add(hint.getParentNode().getLabel());
        return hintAsList;
    }

    private static String createStringForMultipleNodes(List<Label> nodeLabels) {
        StringBuilder builder = new StringBuilder();
        int i = 1;
        for (Label label : nodeLabels) {
            builder.append(label.getLabel());
            if (i < nodeLabels.size()) {
                builder.append(", ");
            }
            i++;
        }
        return builder.toString();
    }

    protected static CSVPrinter getNewPrinter(String name) throws IOException {
        List<String> headers = new ArrayList<>();
        headers.add("actor_name");
        headers.add("script/procedure");
        headers.add("affected_block");
        headers.add("is_addition");
        headers.add("previous blocks");
        headers.add("following blocks");
        headers.add("parent");
        Path filePath = Paths.get(name);
        if (filePath.toFile().length() > 0) {
            BufferedWriter writer = Files.newBufferedWriter(
                    filePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return new CSVPrinter(writer, CSVFormat.DEFAULT.withSkipHeaderRecord());
        } else {
            BufferedWriter writer = Files.newBufferedWriter(
                    filePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers.toArray(new String[0])));
        }
    }
}