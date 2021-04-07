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
package de.uni_passau.fim.se2.catnip.recommendation;

import de.uni_passau.fim.se2.catnip.pq_gram.Label;
import de.uni_passau.fim.se2.catnip.pq_gram.PQGramProfileCreator;
import de.uni_passau.fim.se2.catnip.recommendation.HintGenerationTool;
import de.uni_passau.fim.se2.catnip.recommendation.Recommendation;
import de.uni_passau.fim.se2.catnip.util.CSVWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class HintGenerationToolTest {

    @Test
    public void testHintGeneration() {
        String sourcePath = "./src/test/fixtures/oneBlockDifferenceSource.json";
        String targetPath = "./src/test/fixtures";
        String csvPath = "./src/test/fixtures/hintGenerationResults.csv";
        HintGenerationTool hintGenerationTool = new HintGenerationTool(sourcePath, targetPath, csvPath,false);
        List<Recommendation> recommendations = hintGenerationTool.generateHints().getRecommendations();
        Assertions.assertEquals(1, recommendations.size());
        Recommendation rec = recommendations.get(0);
        Assertions.assertTrue(rec.isAddition());
        Assertions.assertEquals("Bananas", rec.getActor().getIdent().getName());
        Assertions.assertEquals(new Label("IfOnEdgeBounce0", null), rec.getAffectedNode());
        List<Label> prev = new ArrayList<>();
        prev.add(new Label(PQGramProfileCreator.NULL_NODE, null));
        prev.add(new Label(PQGramProfileCreator.NULL_NODE, null));
        Assertions.assertEquals(prev, rec.getPreviousNodes());
        List<Label> next = new ArrayList<>();
        next.add(new Label("GoToPos0", null));
        next.add(new Label(PQGramProfileCreator.NULL_NODE, null));
        Assertions.assertEquals(next, rec.getFollowingNodes());
        Assertions.assertEquals(new Label("StmtList0", null), rec.getParentNode());
    }
}
