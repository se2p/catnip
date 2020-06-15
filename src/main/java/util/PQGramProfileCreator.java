package util;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.util.ArrayList;
import java.util.List;

public abstract class PQGramProfileCreator {
    private static int p = 2;
    private static int q = 3;
    private static final String NULL_NODE = "*";

    public static PQGramProfile createPQProfile(Program program) {
        PQGramProfile profile = new PQGramProfile();
        List<String> anc = new ArrayList<>();
        for (int i = 0; i < p; i++) {
            anc.add(NULL_NODE);
        }
        profile = profileStep(profile, program, anc);
        return profile;
    }

    private static PQGramProfile profileStep(PQGramProfile profile, ASTNode root, List<String> anc) {
        shift(anc,root.getClass().getSimpleName());
        List<String> sib = new ArrayList<>();
        for (int i = 0; i < q; i++) {
            sib.add(NULL_NODE);
        }

        List<ASTNode> children = (List<ASTNode>) root.getChildren();
        if (children.size()==0){
            profile.addLabelTuple(new LabelTuple(anc,sib));
        }else{

            for (ASTNode child : children) {
                shift(sib, child.getClass().getSimpleName());
                profile.addLabelTuple(new LabelTuple(anc, sib));
                profile = profileStep(profile, child, anc);
            }
            for (int k = 0; k < q-1; k++) {
                shift(sib,NULL_NODE);
                profile.addLabelTuple(new LabelTuple(anc, sib));
            }
        }
        return profile;
    }

    private static void shift(List<String> register, String label){
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
