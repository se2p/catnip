package recommendation;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/oneBlockDifferenceSource.json");
        oneBlockDifferenceSource = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/oneBlockDifferenceTarget.json");
        oneBlockDifferenceTarget = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testOneRecommendation() throws ImpossibleEditException {
        List<Program> targets = new ArrayList<>();
        targets.add(oneBlockDifferenceTarget);
        RecommendationGenerator recommendationGenerator = new RecommendationGenerator();
        List<Recommendation> recommendations = recommendationGenerator.generateHints(oneBlockDifferenceSource, targets);
        Assertions.assertEquals(1,recommendations.size());
    }
}
