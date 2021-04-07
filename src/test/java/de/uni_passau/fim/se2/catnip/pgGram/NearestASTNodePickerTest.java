/*
 * Copyright (C) 2019 Catnip contributors
 *
 * This file is part of Catnip.
 *
 * Catnip is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Catnip is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Catnip. If not, see <http://www.gnu.org/licenses/>.
 */
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
