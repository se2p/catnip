package recommendation;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.catnip.pq_gram.Label;
import de.uni_passau.fim.se2.catnip.pq_gram.PQGramProfileCreator;
import de.uni_passau.fim.se2.catnip.recommendation.ImpossibleEditException;
import de.uni_passau.fim.se2.catnip.recommendation.Recommendation;
import de.uni_passau.fim.se2.catnip.recommendation.RecommendationGenerator;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecommendationGeneratorTest {
    private static Program oneBlockDifferenceSource;
    private static Program oneBlockDifferenceTarget;
    private static Program empty;
    private static Program oneScript;
    private static Program tooMuchScriptSource;
    private static Program tooMuchScriptTarget;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/oneBlockDifferenceSource.json");
        oneBlockDifferenceSource = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/oneBlockDifferenceTarget.json");
        oneBlockDifferenceTarget = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/oneScript.json");
        oneScript = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/tooMuchScriptTarget.json");
        tooMuchScriptTarget = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/tooMuchScriptSource.json");
        tooMuchScriptSource = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testOneAdditionRecommendation() throws ImpossibleEditException {
        List<Program> targets = new ArrayList<>();
        targets.add(oneBlockDifferenceTarget);
        RecommendationGenerator recommendationGenerator = new RecommendationGenerator();
        List<Recommendation> recommendations = recommendationGenerator.generateHints(oneBlockDifferenceSource, targets);
        Assertions.assertEquals(1, recommendations.size());
        Assertions.assertTrue(recommendations.get(0).isAddition());
        Assertions.assertFalse(recommendations.get(0).isDeletion());
        Assertions.assertNull(recommendations.get(0).getProcedure());
        Assertions.assertEquals("Bananas", recommendations.get(0).getActor().getIdent().getName());
        Assertions.assertEquals(new Label("IfOnEdgeBounce", null), recommendations.get(0).getAffectedNode());
        List<Label> prev = new ArrayList<>();
        prev.add(new Label(PQGramProfileCreator.NULL_NODE, null));
        prev.add(new Label(PQGramProfileCreator.NULL_NODE, null));
        Assertions.assertEquals(prev, recommendations.get(0).getPreviousNodes());
        List<Label> next = new ArrayList<>();
        next.add(new Label("GoToPos", null));
        next.add(new Label(PQGramProfileCreator.NULL_NODE, null));
        Assertions.assertEquals(next, recommendations.get(0).getFollowingNodes());
    }

    @Test
    public void testOneScriptRecommendation() throws ImpossibleEditException {
        List<Program> targets = new ArrayList<>();
        targets.add(oneScript);
        RecommendationGenerator recommendationGenerator = new RecommendationGenerator();
        List<Recommendation> recommendations = recommendationGenerator.generateHints(empty, targets);
        Assertions.assertEquals(1, recommendations.size());
        Assertions.assertTrue(recommendations.get(0).isAddition());
        Assertions.assertFalse(recommendations.get(0).isDeletion());
        Assertions.assertNull(recommendations.get(0).getProcedure());
        Assertions.assertEquals("Figur1", recommendations.get(0).getActor().getIdent().getName());
        Assertions.assertEquals(new Label("GreenFlag", null), recommendations.get(0).getAffectedNode());
        List<Label> prev = new ArrayList<>();
        prev.add(new Label(PQGramProfileCreator.NULL_NODE, null));
        prev.add(new Label(PQGramProfileCreator.NULL_NODE, null));
        Assertions.assertEquals(prev, recommendations.get(0).getPreviousNodes());
        Assertions.assertEquals(prev, recommendations.get(0).getFollowingNodes());
    }

    @Test
    public void testOneScriptDeleteRecommendation() throws ImpossibleEditException {
        List<Program> targets = new ArrayList<>();
        targets.add(tooMuchScriptTarget);
        RecommendationGenerator recommendationGenerator = new RecommendationGenerator();
        List<Recommendation> recommendations = recommendationGenerator.generateHints(tooMuchScriptSource, targets);
        Assertions.assertEquals(1, recommendations.size());
        Assertions.assertFalse(recommendations.get(0).isAddition());
        Assertions.assertTrue(recommendations.get(0).isDeletion());
        Assertions.assertNull(recommendations.get(0).getProcedure());
        Assertions.assertEquals("Figur1", recommendations.get(0).getActor().getIdent().getName());
        Assertions.assertEquals(new Label("GreenFlag", null), recommendations.get(0).getAffectedNode());
        List<Label> prev = new ArrayList<>();
        prev.add(new Label(PQGramProfileCreator.NULL_NODE, null));
        prev.add(new Label(PQGramProfileCreator.NULL_NODE, null));
        Assertions.assertEquals(prev, recommendations.get(0).getPreviousNodes());
        Assertions.assertEquals(prev, recommendations.get(0).getFollowingNodes());
    }
}
