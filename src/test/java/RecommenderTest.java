import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.catnip.recommendation.ActorScriptEdit;
import de.uni_passau.fim.se2.catnip.recommendation.Recommender;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecommenderTest {
    private static Program sameBlocksSource;
    private static Program oneBlocksTarget;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/oneBlockDifferenceSource.json");
        sameBlocksSource = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/sameBlocksTarget.json");
        oneBlocksTarget = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testOneBlockDifference() {
        List<Program> targets = new ArrayList<>();
        targets.add(oneBlocksTarget);
        Recommender recommender = new Recommender(sameBlocksSource, targets);
        List<ActorScriptEdit> actorEdits = recommender.getEdits();

    }
}
