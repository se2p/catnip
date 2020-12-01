package de.uni_passau.fim.se2.catnip.recommendation;

import de.uni_passau.fim.se2.catnip.pq_gram.Label;
import de.uni_passau.fim.se2.catnip.pq_gram.PQGramProfileCreator;
import de.uni_passau.fim.se2.catnip.recommendation.HintGenerationTool;
import de.uni_passau.fim.se2.catnip.recommendation.Recommendation;
import de.uni_passau.fim.se2.catnip.util.CSVWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class HintGenerationToolTest {

    @Test
    public void testHintGeneration() {
        String sourcePath = "./src/test/fixtures/oneBlockDifferenceSource.json";
        String targetPath = "./src/test/fixtures";
        String csvPath = "./src/test/fixtures/hintGenerationResults.csv";
        HintGenerationTool hintGenerationTool = new HintGenerationTool(sourcePath, targetPath, csvPath);
        List<Recommendation> recommendations = hintGenerationTool.generateHints().getRecommendations();
        Assertions.assertEquals(1, recommendations.size());
        Recommendation rec = recommendations.get(0);
        Assertions.assertTrue(rec.isAddition());
        Assertions.assertEquals("Bananas", rec.getActor().getIdent().getName());
        Assertions.assertEquals(new Label("IfOnEdgeBounce0", null), rec.getAffectedNode());
        List<Label> prev = new ArrayList<>();
        prev.add(new Label(PQGramProfileCreator.NULL_NODE, null));
        prev.add(new Label(PQGramProfileCreator.NULL_NODE, null));
        Assertions.assertEquals(prev, rec.getPreviousNodes());
        List<Label> next = new ArrayList<>();
        next.add(new Label("GoToPos0", null));
        next.add(new Label(PQGramProfileCreator.NULL_NODE, null));
        Assertions.assertEquals(next, rec.getFollowingNodes());
        Assertions.assertEquals(new Label("StmtList", null), rec.getParentNode());
    }
}
