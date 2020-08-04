package de.uni_passau.fim.se2.catnip.pq_gram;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;

import java.util.Objects;

public class Label {
    private String label;
    private ASTNode node;

    public Label(String label, ASTNode node) {
        this.label = label;
        this.node = node;
    }

    public String getLabel() {
        return label;
    }

    public ASTNode getNode() {
        return node;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Label label1 = (Label) o;
        return getLabel().equals(label1.getLabel());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLabel());
    }

    @Override
    public String toString() {
        return label;
    }
}
