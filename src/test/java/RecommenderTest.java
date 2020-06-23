import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.catnip.pqGram.Edits;
import de.uni_passau.fim.se2.catnip.recommendation.ActorScriptEdit;
import de.uni_passau.fim.se2.catnip.recommendation.Recommender;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ClearGraphicEffects;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.javatuples.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RecommenderTest {
    private static Program oneBlockDifferenceSource;
    private static Program oneBlockDifferenceTarget;
    private static Program sameBlocksSource;
    private static Program sameBlocksTarget;
    private static Program empty;
    private static Program oneScript;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/oneBlockDifferenceSource.json");
        oneBlockDifferenceSource = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/oneBlockDifferenceTarget.json");
        oneBlockDifferenceTarget = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/sameBlocksSource.json");
        sameBlocksSource = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/sameBlocksTarget.json");
        sameBlocksTarget = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/oneScript.json");
        oneScript = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testOneBlockDifference() {
        List<Program> targets = new ArrayList<>();
        targets.add(oneBlockDifferenceTarget);
        Recommender recommender = new Recommender(oneBlockDifferenceSource, targets);
        List<ActorScriptEdit> actorEdits = recommender.getEdits();
        Assertions.assertEquals(1, actorEdits.size());
        ActorScriptEdit actorEdit = actorEdits.get(0);
        Assertions.assertEquals("Bananas", actorEdit.getActor().getIdent().getName());
        Edits edit = actorEdit.getEdit();
        Assertions.assertEquals(0, edit.getDeletions().size());
        Assertions.assertEquals(1, edit.getAdditions().size());
        Pair<String, String> addition = new Pair<>("StmtList", "IfOnEdgeBounce");
        Set<Pair<String, String>> additions = new LinkedHashSet<>();
        additions.add(addition);
        Assertions.assertEquals(additions, edit.getAdditions());
    }

    @Test
    public void testSameBlocksDifference() {
        List<Program> targets = new ArrayList<>();
        targets.add(sameBlocksTarget);
        Recommender recommender = new Recommender(sameBlocksSource, targets);
        List<ActorScriptEdit> actorEdits = recommender.getEdits();
        Assertions.assertEquals(1, actorEdits.size());
        ActorScriptEdit actorEdit = actorEdits.get(0);
        Assertions.assertEquals("Bananas", actorEdit.getActor().getIdent().getName());
        Edits edit = actorEdit.getEdit();
        Assertions.assertEquals(0, edit.getDeletions().size());
        Assertions.assertEquals(1, edit.getAdditions().size());
        Pair<String, String> addition = new Pair<>("StmtList", "ClearGraphicEffects");
        Set<Pair<String, String>> additions = new LinkedHashSet<>();
        additions.add(addition);
        Assertions.assertEquals(additions, edit.getAdditions());
    }

    @Test
    public void testOneScriptDifference() {
        List<Program> targets = new ArrayList<>();
        targets.add(oneScript);
        Recommender recommender = new Recommender(empty, targets);
        List<ActorScriptEdit> actorEdits = recommender.getEdits();
        Assertions.assertEquals(1, actorEdits.size());
        ActorScriptEdit actorEdit = actorEdits.get(0);
        Assertions.assertEquals("Figur1", actorEdit.getActor().getIdent().getName());
        Edits edit = actorEdit.getEdit();
        Assertions.assertEquals(0, edit.getDeletions().size());
        Assertions.assertEquals(1, edit.getAdditions().size());
        Pair<String, String> addition = new Pair<>("Script", "GreenFlag");
        Set<Pair<String, String>> additions = new LinkedHashSet<>();
        additions.add(addition);
        Assertions.assertEquals(additions, edit.getAdditions());
    }

}
