package de.uni_passau.fim.se2.catnip.pgGram;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.catnip.pqGram.LabelTuple;
import de.uni_passau.fim.se2.catnip.pqGram.PQGramProfile;
import de.uni_passau.fim.se2.catnip.pqGram.PQGramProfileCreator;
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

import static de.uni_passau.fim.se2.catnip.pqGram.PQGramProfileCreator.NULL_NODE;

public class PQGramProfileCreatorTest {
    private static Program empty;
    private static Program emptyOtherVariable;
    private static Program oneBlock;
    private static ObjectMapper mapper = new ObjectMapper();
    private static List<String> topAnc;
    private static List<String> topSib;

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {
        topAnc = new ArrayList<>();
        topAnc.add(NULL_NODE);
        topAnc.add(Program.class.getSimpleName());
        topSib = new ArrayList<>();
        topSib.add(NULL_NODE);
        topSib.add(NULL_NODE);
        topSib.add(StrId.class.getSimpleName());
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
        LabelTuple tuple = new LabelTuple(topAnc,topSib);
        System.out.println(profile1);
        Assertions.assertTrue(profile1.getTuples().contains(tuple));
    }
}
