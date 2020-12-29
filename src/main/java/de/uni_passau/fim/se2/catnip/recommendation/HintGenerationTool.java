package de.uni_passau.fim.se2.catnip.recommendation;

import com.opencsv.exceptions.CsvException;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;
import org.apache.commons.io.FilenameUtils;

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
        if (targetPath.endsWith(File.separator)) {
            this.targetPath = targetPath;
        } else {
            this.targetPath = targetPath + File.separator;
        }
        this.csvPath = csvPath;
        targetSelector = new TargetSelector(minPercentage);
        parser = new Scratch3Parser();
    }

    public HintGenerationTool(String sourcePath, String targetPath, String csvPath) {
        this.sourcePath = sourcePath;
        if (targetPath.endsWith(File.separator)) {
            this.targetPath = targetPath;
        } else {
            this.targetPath = targetPath + File.separator;
        }
        this.csvPath = csvPath;
        targetSelector = new TargetSelector();
        parser = new Scratch3Parser();
    }

    public Hint generateHints() {
        try {
            Program sourceProgram = parser.parseFile(sourcePath);
            List<Program> targetPrograms = getTargets();
            RecommendationGenerator recommendationGenerator = new RecommendationGenerator();
            List<Recommendation> recommendations = recommendationGenerator.generateHints(sourceProgram, targetPrograms);
            return new Hint(sourceProgram, recommendations);
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        } catch (ParsingException e) {
            System.err.println("Something went wrong while parsing.");
        } catch (ImpossibleEditException e) {
            System.err.println("Something went wrong while generating Edits.");
        }
        return null;
    }

    private List<Program> getTargets() throws IOException, CsvException, ParsingException {
        File sourceProject = new File(sourcePath);
        String sourceName = FilenameUtils.removeExtension(sourceProject.getName());
        List<String> suitableTargets = targetSelector.getViableTargetNames(csvPath, sourceName);
        List<Program> targets = new ArrayList<>();
        for (String targetName : suitableTargets) {
            File project;
            if (targetName.endsWith(".sb3") || targetName.endsWith(".json")) {
                targets.add(parser.parseFile(targetPath + targetName));
            } else {
                project = new File(targetPath + targetName + ".sb3");
                if (project.exists()) {
                    targets.add(parser.parseFile(project));
                } else {
                    project = new File(targetPath + targetName + ".json");
                    if (project.exists()) {
                        targets.add(parser.parseFile(project));
                    } else {
                        System.err.println("No suitable file with name " + targetName + " exists in folder " + targetPath);
                    }
                }
            }
        }
        return targets;
    }
}
