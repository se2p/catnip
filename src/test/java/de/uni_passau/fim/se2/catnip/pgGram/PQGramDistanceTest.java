package de.uni_passau.fim.se2.catnip.pgGram;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.catnip.pqGram.PQGramDistance;
import de.uni_passau.fim.se2.catnip.pqGram.PQGramProfile;
import de.uni_passau.fim.se2.catnip.pqGram.PQGramProfileCreator;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class PQGramDistanceTest {
    private static Program empty;
    private static Program emptyOtherVariable;
    private static Program oneBlock;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/emptyOtherVariableProject.json");
        emptyOtherVariable = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/oneBlockProject.json");
        oneBlock = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testSameProgram() {
        PQGramProfile profile1 = PQGramProfileCreator.createPQProfile(empty);
        Assertions.assertEquals(0, PQGramDistance.calculateDistance(profile1, profile1));
    }

    @Test
    public void testOtherVariableProgram() {
        PQGramProfile profile1 = PQGramProfileCreator.createPQProfile(empty);
        PQGramProfile profile2 = PQGramProfileCreator.createPQProfile(emptyOtherVariable);
        Assertions.assertEquals(0, PQGramDistance.calculateDistance(profile1, profile2));
    }

    @Test
    public void testOneBlockProgram() {
        PQGramProfile profile1 = PQGramProfileCreator.createPQProfile(empty);
        PQGramProfile profile2 = PQGramProfileCreator.createPQProfile(oneBlock);
        Assertions.assertTrue(0 < PQGramDistance.calculateDistance(profile1, profile2) && 1 > PQGramDistance.calculateDistance(profile1, profile2));
    }
}
