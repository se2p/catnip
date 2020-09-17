package de.uni_passau.fim.se2.catnip.pgGram;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.catnip.pq_gram.NearestASTNodePicker;
import de.uni_passau.fim.se2.catnip.pq_gram.PQGramProfileCreator;
import de.uni_passau.fim.se2.catnip.pq_gram.ProgramWithProfile;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NearestASTNodePickerTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testProgramPick() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/farTargetProgram.json");
        Program farTargetProgram = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/nearTargetProgram.json");
        Program nearTargetProgram = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/middleTargetProgram.json");
        Program middleTargetProgram = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        List<ProgramWithProfile> targets = new ArrayList<>();
        targets.add(new ProgramWithProfile(farTargetProgram, PQGramProfileCreator.createPQProfile(farTargetProgram)));
        targets.add(new ProgramWithProfile(nearTargetProgram, PQGramProfileCreator.createPQProfile(nearTargetProgram)));
        f = new File("./src/test/fixtures/sourceProgram.json");
        Program sourceProgram = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        Program nearestProgram = NearestASTNodePicker.pickNearestProgram(sourceProgram, targets);
        Assertions.assertSame(nearestProgram, nearTargetProgram);

        targets = new ArrayList<>();
        targets.add(new ProgramWithProfile(farTargetProgram, PQGramProfileCreator.createPQProfile(farTargetProgram)));
        targets.add(new ProgramWithProfile(middleTargetProgram, PQGramProfileCreator.createPQProfile(middleTargetProgram)));
        nearestProgram = NearestASTNodePicker.pickNearestProgram(sourceProgram, targets);
        Assertions.assertSame(nearestProgram, middleTargetProgram);

        targets = new ArrayList<>();
        targets.add(new ProgramWithProfile(nearTargetProgram, PQGramProfileCreator.createPQProfile(nearTargetProgram)));
        targets.add(new ProgramWithProfile(middleTargetProgram, PQGramProfileCreator.createPQProfile(middleTargetProgram)));
        nearestProgram = NearestASTNodePicker.pickNearestProgram(sourceProgram, targets);
        Assertions.assertSame(nearestProgram, nearTargetProgram);
    }
}
