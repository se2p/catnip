package de.uni_passau.fim.se2.catnip.recommendation;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.catnip.pq_gram.Label;
import de.uni_passau.fim.se2.catnip.recommendation.*;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class EditsGeneratorTest {
    private static Program empty;
    private static Program deadNewScript;
    private static final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/deadNewScript.json");
        deadNewScript = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testOneBlockDifference() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/oneBlockDifferenceSource.json");
        Program oneBlockDifferenceSource = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/oneBlockDifferenceTarget.json");
        Program oneBlockDifferenceTarget = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        List<Program> targets = new ArrayList<>();
        targets.add(oneBlockDifferenceTarget);
        EditsGenerator editsGenerator = new EditsGenerator(oneBlockDifferenceSource, targets);
        List<ActorBlockEdit> actorEdits = editsGenerator.getEdits();
        Assertions.assertEquals(1, actorEdits.size());
        Assertions.assertTrue(actorEdits.get(0) instanceof ActorScriptEdit);
        ActorScriptEdit actorEdit = (ActorScriptEdit) actorEdits.get(0);
        Assertions.assertEquals("Bananas", actorEdit.getActor().getIdent().getName());
        EditSet edit = actorEdit.getEdit();
        Assertions.assertEquals(0, edit.getDeletions().size());
        Assertions.assertEquals(4, edit.getAdditions().size());
        Edit addition1 = new Edit(new Label("StmtList0", null), new Label("IfOnEdgeBounce0", null));
        Label goTo = new Label("GoToPos0", null);
        Label emptyLabel = new Label("*", null);
        List<Label> left = new ArrayList<>();
        List<Label> right = new ArrayList<>();
        right.add(goTo);
        right.add(emptyLabel);
        Edit addition2 = new Edit(new Label("StmtList0", null), new Label("IfOnEdgeBounce0", null), new ArrayList<>(left), new ArrayList<>(right));
        left = new ArrayList<>();
        right = new ArrayList<>();
        left.add(emptyLabel);
        left.add(emptyLabel);
        Edit addition3 = new Edit(new Label("StmtList0", null), new Label("IfOnEdgeBounce0", null), new ArrayList<>(left), new ArrayList<>(right));
        left = new ArrayList<>();
        right = new ArrayList<>();
        left.add(emptyLabel);
        right.add(goTo);
        Edit addition4 = new Edit(new Label("StmtList0", null), new Label("IfOnEdgeBounce0", null), new ArrayList<>(left), new ArrayList<>(right));
        Set<Edit> additions = new LinkedHashSet<>();
        additions.add(addition1);
        additions.add(addition2);
        additions.add(addition3);
        additions.add(addition4);
        Assertions.assertEquals(additions, edit.getAdditions());
    }

    @Test
    public void testSameBlocksDifference() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/sameBlocksSource.json");
        Program sameBlocksSource = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/sameBlocksTarget.json");
        Program sameBlocksTarget = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        List<Program> targets = new ArrayList<>();
        targets.add(sameBlocksTarget);
        EditsGenerator editsGenerator = new EditsGenerator(sameBlocksSource, targets);
        List<ActorBlockEdit> actorEdits = editsGenerator.getEdits();
        Assertions.assertEquals(1, actorEdits.size());
        Assertions.assertTrue(actorEdits.get(0) instanceof ActorScriptEdit);
        ActorScriptEdit actorEdit = (ActorScriptEdit) actorEdits.get(0);
        Assertions.assertEquals("Bananas", actorEdit.getActor().getIdent().getName());
        EditSet edit = actorEdit.getEdit();
        Assertions.assertEquals(0, edit.getDeletions().size());
        Assertions.assertEquals(4, edit.getAdditions().size());
        Label ifOnEdge = new Label("IfOnEdgeBounce0", null);
        Label emptyLabel = new Label("*", null);
        List<Label> left = new ArrayList<>();
        List<Label> right = new ArrayList<>();
        left.add(emptyLabel);
        left.add(ifOnEdge);
        Edit addition1 = new Edit(new Label("StmtList0", null), new Label("ClearGraphicEffects0", null));
        Edit addition2 = new Edit(new Label("StmtList0", null), new Label("ClearGraphicEffects0", null), new ArrayList<>(left), new ArrayList<>(right));
        left = new ArrayList<>();
        right = new ArrayList<>();
        right.add(emptyLabel);
        right.add(emptyLabel);
        Edit addition3 = new Edit(new Label("StmtList0", null), new Label("ClearGraphicEffects0", null), new ArrayList<>(left), new ArrayList<>(right));
        left = new ArrayList<>();
        right = new ArrayList<>();
        left.add(ifOnEdge);
        right.add(emptyLabel);
        Edit addition4 = new Edit(new Label("StmtList0", null), new Label("ClearGraphicEffects0", null), new ArrayList<>(left), new ArrayList<>(right));
        Set<Edit> additions = new LinkedHashSet<>();
        additions.add(addition1);
        additions.add(addition2);
        additions.add(addition3);
        additions.add(addition4);
        Assertions.assertEquals(additions, edit.getAdditions());
    }

    @Test
    public void testOneScriptDifference() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/oneScript.json");
        Program oneScript = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        List<Program> targets = new ArrayList<>();
        targets.add(oneScript);
        EditsGenerator editsGenerator = new EditsGenerator(empty, targets);
        List<ActorBlockEdit> actorEdits = editsGenerator.getEdits();
        Assertions.assertEquals(1, actorEdits.size());
        ActorBlockEdit actorEdit = actorEdits.get(0);
        Assertions.assertEquals("Figur1", actorEdit.getActor().getIdent().getName());
        EditSet edit = actorEdit.getEdit();
        Assertions.assertEquals(0, edit.getDeletions().size());
        Assertions.assertEquals(1, edit.getAdditions().size());
        Edit addition = new Edit(new Label("Script", null), new Label("GreenFlag", null));
        Set<Edit> additions = new LinkedHashSet<>();
        additions.add(addition);
        Assertions.assertEquals(additions, edit.getAdditions());
    }

    @Test
    public void testTooMuch() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/tooMuchTarget.json");
        Program tooMuchTarget = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/tooMuchSource.json");
        Program tooMuchSource = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        List<Program> targets = new ArrayList<>();
        targets.add(tooMuchTarget);
        EditsGenerator editsGenerator = new EditsGenerator(tooMuchSource, targets);
        List<ActorBlockEdit> actorEdits = editsGenerator.getEdits();
        Assertions.assertEquals(1, actorEdits.size());
        ActorBlockEdit actorEdit = actorEdits.get(0);
        Assertions.assertEquals("Figur1", actorEdit.getActor().getIdent().getName());
        EditSet edit = actorEdit.getEdit();
        Assertions.assertEquals(8, edit.getDeletions().size());
        Edit deletion1 = new Edit(new Label("StmtList0", null), new Label("IfOnEdgeBounce0", null));
        Edit deletion2 = new Edit(new Label("StmtList0", null), new Label("NextCostume0", null));
        Set<Edit> deletions = new LinkedHashSet<>();
        deletions.add(deletion1);
        deletions.add(deletion2);
        Assertions.assertTrue(edit.getDeletions().containsAll(deletions));
    }

    @Test
    public void testProcedureDifference() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/oneProcedureSource.json");
        Program oneProcedureSource = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/oneProcedureTarget.json");
        Program oneProcedureTarget = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        List<Program> targets = new ArrayList<>();
        targets.add(oneProcedureTarget);
        EditsGenerator editsGenerator = new EditsGenerator(oneProcedureSource, targets);
        List<ActorBlockEdit> actorEdits = editsGenerator.getEdits();
        Assertions.assertEquals(1, actorEdits.size());
        ActorBlockEdit actorEdit = actorEdits.get(0);
        Assertions.assertEquals("Figur1", actorEdit.getActor().getIdent().getName());
        EditSet edit = actorEdit.getEdit();
        Assertions.assertEquals(0, edit.getDeletions().size());
        Assertions.assertEquals(4, edit.getAdditions().size());
        Edit addition2 = new Edit(new Label("StmtList0", null), new Label("TurnRight0", null));
        Set<Edit> additions = new LinkedHashSet<>();
        additions.add(addition2);
        Assertions.assertTrue(edit.getAdditions().containsAll(additions));
    }

    @Test
    public void testTooSourceMuch() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/tooMuchScriptTarget.json");
        Program tooMuchScriptTarget = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/tooMuchScriptSource.json");
        Program tooMuchScriptSource = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        List<Program> targets = new ArrayList<>();
        targets.add(tooMuchScriptTarget);
        EditsGenerator editsGenerator = new EditsGenerator(tooMuchScriptSource, targets);
        List<ActorBlockEdit> actorEdits = editsGenerator.getEdits();
        Assertions.assertEquals(1, actorEdits.size());
        ActorBlockEdit actorEdit = actorEdits.get(0);
        Assertions.assertEquals("Figur1", actorEdit.getActor().getIdent().getName());
        EditSet edit = actorEdit.getEdit();
        Assertions.assertEquals(1, edit.getDeletions().size());
        Edit deletion1 = new Edit(new Label("Script", null), new Label("GreenFlag", null));
        Set<Edit> deletions = new LinkedHashSet<>();
        deletions.add(deletion1);
        Assertions.assertTrue(edit.getDeletions().containsAll(deletions));
    }

    @Test
    public void testOneScriptAdditionDifferenceDead() {
        List<Program> targets = new ArrayList<>();
        targets.add(deadNewScript);
        EditsGenerator editsGenerator = new EditsGenerator(empty, targets);
        List<ActorBlockEdit> actorEdits = editsGenerator.getEdits();
        Assertions.assertEquals(1, actorEdits.size());
        ActorBlockEdit actorEdit = actorEdits.get(0);
        Assertions.assertEquals("Figur1", actorEdit.getActor().getIdent().getName());
        EditSet edit = actorEdit.getEdit();
        Assertions.assertEquals(0, edit.getDeletions().size());
        Assertions.assertEquals(1, edit.getAdditions().size());
        Edit addition = new Edit(new Label("Script", null), new Label("TurnRight0", null));
        Set<Edit> additions = new LinkedHashSet<>();
        additions.add(addition);
        Assertions.assertEquals(additions, edit.getAdditions());
    }

    @Test
    public void testOneScriptDeleteDifferenceDead() {
        List<Program> targets = new ArrayList<>();
        targets.add(empty);
        EditsGenerator editsGenerator = new EditsGenerator(deadNewScript, targets);
        List<ActorBlockEdit> actorEdits = editsGenerator.getEdits();
        Assertions.assertEquals(1, actorEdits.size());
        ActorBlockEdit actorEdit = actorEdits.get(0);
        Assertions.assertEquals("Sprite1", actorEdit.getActor().getIdent().getName());
        EditSet edit = actorEdit.getEdit();
        Assertions.assertEquals(1, edit.getDeletions().size());
        Assertions.assertEquals(0, edit.getAdditions().size());
        Edit addition = new Edit(new Label("Script", null), new Label("TurnRight0", null));
        Set<Edit> deletions = new LinkedHashSet<>();
        deletions.add(addition);
        Assertions.assertEquals(deletions, edit.getDeletions());
    }

    @Test
    public void testExpressionAddition() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/deadNewScriptWithExp.json");
        Program deadNewScriptWithExp = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        List<Program> targets = new ArrayList<>();
        targets.add(deadNewScriptWithExp);
        EditsGenerator editsGenerator = new EditsGenerator(deadNewScript, targets);
        List<ActorBlockEdit> actorEdits = editsGenerator.getEdits();
        Assertions.assertEquals(1, actorEdits.size());
        ActorBlockEdit actorEdit = actorEdits.get(0);
        Assertions.assertEquals("Sprite1", actorEdit.getActor().getIdent().getName());
        EditSet edit = actorEdit.getEdit();
        Assertions.assertEquals(0, edit.getDeletions().size());
        Assertions.assertEquals(4, edit.getAdditions().size());
        Edit addition = new Edit(new Label("TurnRight0", null), new Label("Size0", null));
        Set<Edit> additions = new LinkedHashSet<>();
        additions.add(addition);
        Label emptyLabel = new Label("*", null);
        List<Label> left = new ArrayList<>();
        List<Label> right = new ArrayList<>();
        left.add(emptyLabel);
        left.add(emptyLabel);
        additions.add(new Edit(new Label("TurnRight0", null), new Label("Size0", null), left, right));
        left = new ArrayList<>();
        right = new ArrayList<>();
        left.add(emptyLabel);
        right.add(new Label("TopNonDataBlockMetadata", null));
        additions.add(new Edit(new Label("TurnRight0", null), new Label("Size0", null), left, right));
        left = new ArrayList<>();
        right = new ArrayList<>();
        right.add(new Label("TopNonDataBlockMetadata", null));
        right.add(emptyLabel);
        additions.add(new Edit(new Label("TurnRight0", null), new Label("Size0", null), left, right));
        Assert.assertEquals(additions, edit.getAdditions());
    }
}
