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
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testSameProgram() {
        PQGramProfile profile1 = PQGramProfileCreator.createPQProfile(empty);
        Assertions.assertEquals(0, PQGramDistance.calculateDistance(profile1, profile1));
    }
}
