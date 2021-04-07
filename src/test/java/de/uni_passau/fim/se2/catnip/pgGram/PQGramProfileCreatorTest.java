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
    private static List<Label> topAnc;
    private static List<Label> topSib;

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {
        ObjectMapper mapper = new ObjectMapper();
        topAnc = new ArrayList<>();
        topAnc.add(new Label(NULL_NODE, null));
        topAnc.add(new Label(Program.class.getSimpleName(), null));
        topSib = new ArrayList<>();
        topSib.add(new Label(NULL_NODE, null));
        topSib.add(new Label(NULL_NODE, null));
        topSib.add(new Label(StrId.class.getSimpleName(), null));
        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testEmpty() {
        PQGramProfile profile1 = PQGramProfileCreator.createPQProfile(empty);
        LabelTuple tuple = new LabelTuple(topAnc, topSib);
        Assertions.assertTrue(profile1.getTuples().contains(tuple));
    }
}
