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
package de.uni_passau.fim.se2.catnip.pq_gram;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PQGramProfileCreator {
    private static int p = 2;
    private static int q = 3;
    public static final String NULL_NODE = "*";
    private static Map<String, Integer> countPerLabel;

    private PQGramProfileCreator() {

    }

    public static PQGramProfile createPQProfile(ASTNode node) {
        PQGramProfile profile = new PQGramProfile();
        List<Label> anc = new ArrayList<>();
        for (int i = 0; i < p; i++) {
            anc.add(new Label(NULL_NODE, null));
        }
        countPerLabel = new LinkedHashMap<>();

        profile = profileStep(profile, node, getBlockName(node), anc);
        return profile;
    }

    private static String getBlockName(ASTNode node) {
        String blockName;
        if ((node instanceof Stmt || node instanceof Expression || node instanceof StmtList) && !node.getClass().getSimpleName().contains("Literal")
                && !(node instanceof Identifier)) {
            String currentBlock = node.getClass().getSimpleName();
            if (countPerLabel.containsKey(currentBlock)) {
                int count = countPerLabel.get(currentBlock) + 1;
                countPerLabel.replace(currentBlock, count);
                blockName = currentBlock + count;
            } else {
                countPerLabel.put(currentBlock, 0);
                blockName = currentBlock + 0;
            }
        } else {
            blockName = node.getClass().getSimpleName();
        }
        return blockName;
    }

    private static PQGramProfile profileStep(PQGramProfile profile, ASTNode root, String rootLabel, List<Label> anc) {
        List<Label> ancHere = new ArrayList<>(anc);
        shift(ancHere, new Label(rootLabel, root));
        List<Label> sib = new ArrayList<>();
        for (int i = 0; i < q; i++) {
            sib.add(new Label(NULL_NODE, null));
        }

        List<ASTNode> children = (List<ASTNode>) root.getChildren();
        if (children.size() == 0) {
            profile.addLabelTuple(new LabelTuple(ancHere, sib));
        } else {

            for (ASTNode child : children) {
                String blockName = getBlockName(child);
                shift(sib, new Label(blockName, child));
                profile.addLabelTuple(new LabelTuple(ancHere, sib));
                profile = profileStep(profile, child, blockName, ancHere);
            }
            for (int k = 0; k < q - 1; k++) {
                shift(sib, new Label(NULL_NODE, null));
                profile.addLabelTuple(new LabelTuple(ancHere, sib));
            }
        }
        return profile;
    }

    private static void shift(List<Label> register, Label label) {
        register.remove(0);
        register.add(label);
    }

    public static void setP(int p) {
        PQGramProfileCreator.p = p;
    }

    public static void setQ(int q) {
        PQGramProfileCreator.q = q;
    }

    public static int getP() {
        return p;
    }

    public static int getQ() {
        return q;
    }
}
