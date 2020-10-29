package de.uni_passau.fim.se2.catnip.pgGram;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.catnip.pq_gram.PQGramProfile;
import de.uni_passau.fim.se2.catnip.pq_gram.PQGramProfileCreator;
import de.uni_passau.fim.se2.catnip.pq_gram.PQGramUtil;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class PQGramUtilTest {
    private static Program empty;
    private static final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testSameProgramDistance() {
        PQGramProfile profile1 = PQGramProfileCreator.createPQProfile(empty);
        Assertions.assertEquals(0, PQGramUtil.calculateDistance(profile1, profile1));
    }

    @Test
    public void testSameProgramEdits() {
        PQGramProfile profile1 = PQGramProfileCreator.createPQProfile(empty);
        Assertions.assertEquals(0, PQGramUtil.identifyEdits(profile1, profile1).getAdditions().size());
        Assertions.assertEquals(0, PQGramUtil.identifyEdits(profile1, profile1).getDeletions().size());
    }

    @Test
    public void testOtherVariableProgram() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/emptyOtherVariableProject.json");
        Program emptyOtherVariable = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        PQGramProfile profile1 = PQGramProfileCreator.createPQProfile(empty);
        PQGramProfile profile2 = PQGramProfileCreator.createPQProfile(emptyOtherVariable);
        Assertions.assertEquals(0, PQGramUtil.calculateDistance(profile1, profile2));
    }

    @Test
    public void testOneBlockProgram() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/oneBlockProject.json");
        Program oneBlock = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        PQGramProfile profile1 = PQGramProfileCreator.createPQProfile(empty);
        PQGramProfile profile2 = PQGramProfileCreator.createPQProfile(oneBlock);
        Assertions.assertTrue(0 < PQGramUtil.calculateDistance(profile1, profile2) && 1 > PQGramUtil.calculateDistance(profile1, profile2));
    }
}
