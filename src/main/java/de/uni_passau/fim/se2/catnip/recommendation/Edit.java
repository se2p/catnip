package de.uni_passau.fim.se2.catnip.recommendation;

import de.uni_passau.fim.se2.catnip.pq_gram.Label;

import java.util.List;
import java.util.Objects;

public class Edit {
    private Label parent;
    private Label changeNode;
    private List<Label> leftSiblings;
    private List<Label> rightSiblings;

    public Edit(Label parent, Label changeNode) {
        this.parent = parent;
        this.changeNode = changeNode;
    }

    public Edit(Label parent, Label changeNode, List<Label> leftSiblings, List<Label> rightSiblings) {
        this.parent = parent;
        this.changeNode = changeNode;
        this.leftSiblings = leftSiblings;
        this.rightSiblings = rightSiblings;
    }

    public Label getParent() {
        return parent;
    }

    public Label getChangeNode() {
        return changeNode;
    }

    public List<Label> getLeftSiblings() {
        return leftSiblings;
    }

    public List<Label> getRightSiblings() {
        return rightSiblings;
    }

    public boolean hasLeftSiblings() {
        return leftSiblings.size() != 0;
    }

    public boolean hasRightSiblings() {
        return rightSiblings.size() != 0;
    }

    public boolean hasSiblings() {
        return rightSiblings != null && leftSiblings != null;
    }

    @Override
    public String toString() {
        return "Edit{"
                + "parent=" + parent
                + ", newNode=" + changeNode
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
                && changeNode.equals(edit.changeNode)
                && Objects.equals(leftSiblings, edit.leftSiblings)
                && Objects.equals(rightSiblings, edit.rightSiblings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, changeNode, leftSiblings, rightSiblings);
    }
}
