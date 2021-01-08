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

public abstract class PQGramProfileCreator {
    private static int p = 2;
    private static int q = 3;
    public static final String NULL_NODE = "*";
    private static Map<String, Integer> countPerLabel;

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
