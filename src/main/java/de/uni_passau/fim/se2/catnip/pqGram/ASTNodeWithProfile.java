package de.uni_passau.fim.se2.catnip.pqGram;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;

public class ASTNodeWithProfile {
    private ASTNode astNode;
    private PQGramProfile profile;

    public ASTNodeWithProfile(ASTNode astNode) {
        this.astNode = astNode;
        profile = PQGramProfileCreator.createPQProfile(astNode);
    }

    public ASTNodeWithProfile(ASTNode astNode, PQGramProfile profile) {
        this.astNode = astNode;
        this.profile = profile;
    }

    public ASTNode getASTNode() {
        return astNode;
    }

    public PQGramProfile getProfile() {
        return profile;
    }
}
