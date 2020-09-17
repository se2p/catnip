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
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecommendationGeneratorTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testOneAdditionRecommendation() throws ImpossibleEditException, IOException, ParsingException {
        File f = new File("./src/test/fixtures/oneBlockDifferenceSource.json");
        Program oneBlockDifferenceSource = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/oneBlockDifferenceTarget.json");
        Program oneBlockDifferenceTarget = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
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
        Assertions.assertEquals(new Label("StmtList", null), recommendations.get(0).getParentNode());
    }

    @Test
    public void testOneScriptRecommendation() throws ImpossibleEditException, IOException, ParsingException {
        File f = new File("./src/test/fixtures/emptyProject.json");
        Program empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/oneScript.json");
        Program oneScript = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
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
        Assertions.assertEquals(new Label("Script", null), recommendations.get(0).getParentNode());
    }

    @Test
    public void testOneScriptDeleteRecommendation() throws ImpossibleEditException, IOException, ParsingException {
        File f = new File("./src/test/fixtures/tooMuchScriptTarget.json");
        Program tooMuchScriptTarget = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/tooMuchScriptSource.json");
        Program tooMuchScriptSource = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
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
        Assertions.assertEquals(new Label("Script", null), recommendations.get(0).getParentNode());
    }

    @Test
    public void testProcedureTwoRecommendations() throws ImpossibleEditException, IOException, ParsingException {
        File f = new File("./src/test/fixtures/oneProcedureSource.json");
        Program oneProcedureSource = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/oneProcedureTarget.json");
        Program oneProcedureTarget = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        List<Program> targets = new ArrayList<>();
        targets.add(oneProcedureTarget);
        RecommendationGenerator recommendationGenerator = new RecommendationGenerator();
        List<Recommendation> recommendations = recommendationGenerator.generateHints(oneProcedureSource, targets);
        Assertions.assertEquals(2, recommendations.size());
        Assertions.assertTrue(recommendations.get(0).isAddition());
        Assertions.assertFalse(recommendations.get(0).isDeletion());
        Assertions.assertNull(recommendations.get(0).getScript());
        Assertions.assertTrue(recommendations.get(1).isAddition());
        Assertions.assertFalse(recommendations.get(1).isDeletion());
        Assertions.assertNull(recommendations.get(1).getScript());
        Assertions.assertEquals(new Label("NumberLiteral", null), recommendations.get(0).getAffectedNode());
        Assertions.assertEquals(new Label("TurnRight", null), recommendations.get(1).getAffectedNode());
    }

    @Test
    public void testOneScriptMultipleSameBlockRecommendation() throws ImpossibleEditException, IOException, ParsingException {
        File f = new File("./src/test/fixtures/sameBlockScriptTarget.json");
        Program sameBlockScriptTarget = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/sameBlockScriptSource.json");
        Program sameBlockScriptSource = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        List<Program> targets = new ArrayList<>();
        targets.add(sameBlockScriptTarget);
        RecommendationGenerator recommendationGenerator = new RecommendationGenerator();
        List<Recommendation> recommendations = recommendationGenerator.generateHints(sameBlockScriptSource, targets);
        Assertions.assertEquals(2, recommendations.size());
        Assertions.assertTrue(recommendations.get(0).isAddition());
        Assertions.assertFalse(recommendations.get(0).isDeletion());
        Assertions.assertNull(recommendations.get(0).getProcedure());
        Assertions.assertTrue(recommendations.get(1).isAddition());
        Assertions.assertFalse(recommendations.get(1).isDeletion());
        Assertions.assertNull(recommendations.get(1).getProcedure());
        Assertions.assertEquals(new Label("IfOnEdgeBounce", null), recommendations.get(0).getAffectedNode());
        List<Label> after = new ArrayList<>();
        after.add(new Label(PQGramProfileCreator.NULL_NODE, null));
        after.add(new Label(PQGramProfileCreator.NULL_NODE, null));
        Assertions.assertEquals(after, recommendations.get(0).getFollowingNodes());
        List<Label> middle = new ArrayList<>();
        middle.add(new Label("StopAllSounds", null));
        middle.add(new Label("ClearSoundEffects", null));
        Assertions.assertEquals(middle, recommendations.get(0).getPreviousNodes());
        Assertions.assertEquals(new Label("IfOnEdgeBounce", null), recommendations.get(1).getAffectedNode());
        Assertions.assertEquals(middle, recommendations.get(1).getFollowingNodes());
        List<Label> prev = new ArrayList<>();
        prev.add(new Label("SayForSecs", null));
        prev.add(new Label("Think", null));
        Assertions.assertEquals(prev, recommendations.get(1).getPreviousNodes());
    }
}
