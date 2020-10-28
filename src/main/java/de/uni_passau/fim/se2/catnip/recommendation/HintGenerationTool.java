package de.uni_passau.fim.se2.catnip.recommendation;

import com.opencsv.exceptions.CsvException;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HintGenerationTool {
    private final Scratch3Parser parser;
    private final String sourcePath;
    private final String targetPath;
    private final String csvPath;
    private TargetSelector targetSelector;

    public HintGenerationTool(String sourcePath, String targetPath, String csvPath, double minPercentage) {
        this.sourcePath = sourcePath;
        this.targetPath = targetPath;
        this.csvPath = csvPath;
        targetSelector = new TargetSelector(minPercentage);
        parser = new Scratch3Parser();
    }

    public HintGenerationTool(String sourcePath, String targetPath, String csvPath) {
        this.sourcePath = sourcePath;
        this.targetPath = targetPath;
        this.csvPath = csvPath;
        targetSelector = new TargetSelector();
        parser = new Scratch3Parser();
    }

    public void generateHints() {
        try {
            Program sourceProgram = parser.parseFile(sourcePath);
            List<Program> targetPrograms = parseTargets();
            RecommendationGenerator recommendationGenerator = new RecommendationGenerator();
            List<Recommendation> recommendations = recommendationGenerator.generateHints(sourceProgram, targetPrograms);
            //Todo
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        } catch (ParsingException e) {
            System.err.println("Something went wrong while parsing.");
        } catch (ImpossibleEditException e) {
            System.err.println("Something went wrong while generating Edits.");
        }
    }

    private List<Program> parseTargets() throws IOException, CsvException, ParsingException {
        List<String> suitableTargets = targetSelector.getViableTargetNames(csvPath);
        File file = new File(targetPath);
        List<Program> targets = new ArrayList<>();
        for (String targetName : suitableTargets) {
            targets.add(parser.parseFile(file.getPath() + targetName));
        }
        return targets;
    }
}
