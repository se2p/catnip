package de.uni_passau.fim.se2.catnip.pq_gram;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;

import java.util.Objects;

public class ASTNodeWithProfile {
    private final ASTNode astNode;
    private final PQGramProfile profile;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ASTNodeWithProfile that = (ASTNodeWithProfile) o;
        return astNode.equals(that.astNode)
                && getProfile().equals(that.getProfile());
    }

    @Override
    public int hashCode() {
        return Objects.hash(astNode, getProfile());
    }
}
