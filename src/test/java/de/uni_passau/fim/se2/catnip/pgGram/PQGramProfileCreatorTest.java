package de.uni_passau.fim.se2.catnip.pgGram;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.catnip.pq_gram.Label;
import de.uni_passau.fim.se2.catnip.pq_gram.LabelTuple;
import de.uni_passau.fim.se2.catnip.pq_gram.PQGramProfile;
import de.uni_passau.fim.se2.catnip.pq_gram.PQGramProfileCreator;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.catnip.pq_gram.PQGramProfileCreator.NULL_NODE;

public class PQGramProfileCreatorTest {
    private static Program empty;
    private static Program emptyOtherVariable;
    private static Program oneBlock;
    private static ObjectMapper mapper = new ObjectMapper();
    private static List<Label> topAnc;
    private static List<Label> topSib;

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {
        topAnc = new ArrayList<>();
        topAnc.add(new Label(NULL_NODE, null));
        topAnc.add(new Label(Program.class.getSimpleName(), null));
        topSib = new ArrayList<>();
        topSib.add(new Label(NULL_NODE, null));
        topSib.add(new Label(NULL_NODE, null));
        topSib.add(new Label(StrId.class.getSimpleName(), null));
        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/emptyOtherVariableProject.json");
        emptyOtherVariable = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/oneBlockProject.json");
        oneBlock = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testEmpty() {
        PQGramProfile profile1 = PQGramProfileCreator.createPQProfile(empty);
        LabelTuple tuple = new LabelTuple(topAnc, topSib);
        Assertions.assertTrue(profile1.getTuples().contains(tuple));
    }
}
