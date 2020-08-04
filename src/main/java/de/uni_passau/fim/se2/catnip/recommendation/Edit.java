package de.uni_passau.fim.se2.catnip.recommendation;

import de.uni_passau.fim.se2.catnip.pq_gram.Label;

import java.util.List;
import java.util.Objects;

public class Edit {
    private Label parent;
    private Label newNode;
    private List<Label> leftSiblings;
    private List<Label> rightSiblings;

    public Edit(Label parent, Label newNode) {
        this.parent = parent;
        this.newNode = newNode;
    }

    public Edit(Label parent, Label newNode, List<Label> leftSiblings, List<Label> rightSiblings) {
        this.parent = parent;
        this.newNode = newNode;
        this.leftSiblings = leftSiblings;
        this.rightSiblings = rightSiblings;
    }

    public Label getParent() {
        return parent;
    }

    public Label getNewNode() {
        return newNode;
    }

    public List<Label> getLeftSiblings() {
        return leftSiblings;
    }

    public List<Label> getRightSiblings() {
        return rightSiblings;
    }

    public boolean hasLeftSiblings() {
        return leftSiblings != null;
    }

    public boolean hasRightSiblings() {
        return rightSiblings != null;
    }

    public boolean hasSiblings() {
        return rightSiblings != null && leftSiblings != null;
    }

    @Override
    public String toString() {
        return "Edit{"
                + "parent=" + parent
                + ", newNode=" + newNode
                + ", leftSiblings=" + leftSiblings
                + ", rightSiblings=" + rightSiblings + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Edit edit = (Edit) o;
        return parent.equals(edit.parent)
                && newNode.equals(edit.newNode)
                && Objects.equals(leftSiblings, edit.leftSiblings)
                && Objects.equals(rightSiblings, edit.rightSiblings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, newNode, leftSiblings, rightSiblings);
    }
}
