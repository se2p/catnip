package de.uni_passau.fim.se2.catnip.pq_gram;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;

import java.util.ArrayList;
import java.util.List;

public abstract class PQGramProfileCreator {
    private static int p = 2;
    private static int q = 3;
    public static final String NULL_NODE = "*";

    public static PQGramProfile createPQProfile(ASTNode node) {
        PQGramProfile profile = new PQGramProfile();
        List<Label> anc = new ArrayList<>();
        for (int i = 0; i < p; i++) {
            anc.add(new Label(NULL_NODE, null));
        }
        profile = profileStep(profile, node, anc);
        return profile;
    }

    private static PQGramProfile profileStep(PQGramProfile profile, ASTNode root, List<Label> anc) {
        List<Label> ancHere = new ArrayList<>(anc);
        shift(ancHere, new Label(root.getClass().getSimpleName(), root));
        List<Label> sib = new ArrayList<>();
        for (int i = 0; i < q; i++) {
            sib.add(new Label(NULL_NODE, null));
        }

        List<ASTNode> children = (List<ASTNode>) root.getChildren();
        if (children.size() == 0) {
            profile.addLabelTuple(new LabelTuple(ancHere, sib));
        } else {

            for (ASTNode child : children) {
                shift(sib, new Label(child.getClass().getSimpleName(), child));
                profile.addLabelTuple(new LabelTuple(ancHere, sib));
                profile = profileStep(profile, child, ancHere);
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
