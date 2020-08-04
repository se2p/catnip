package de.uni_passau.fim.se2.catnip.recommendation;

import de.uni_passau.fim.se2.catnip.pq_gram.Label;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

import java.util.List;

public class Recommendation {
    private final List<Label> previousNodes;
    private final List<Label> followingNodes;
    private final Label affectedNode;
    private final boolean deletion;
    private final boolean addition;
    private final Script script;
    private final ProcedureDefinition procedure;
    private final ActorDefinition actor;

    public Recommendation(List<Label> previousNodes, List<Label> followingNodes, Label affectedNode, boolean deletion,
                          boolean addition, Script script, ProcedureDefinition procedure, ActorDefinition actor) {
        assert (deletion && !addition) || (!deletion && addition);
        assert (procedure == null && script != null) || (procedure != null && script == null);
        this.previousNodes = previousNodes;
        this.followingNodes = followingNodes;
        this.affectedNode = affectedNode;
        this.deletion = deletion;
        this.addition = addition;
        this.script = script;
        this.procedure = procedure;
        this.actor = actor;
    }

    public List<Label> getPreviousNodes() {
        return previousNodes;
    }

    public List<Label> getFollowingNodes() {
        return followingNodes;
    }

    public Label getAffectedNode() {
        return affectedNode;
    }

    public boolean isDeletion() {
        return deletion;
    }

    public boolean isAddition() {
        return addition;
    }

    public Script getScript() {
        return script;
    }

    public ProcedureDefinition getProcedure() {
        return procedure;
    }

    public ActorDefinition getActor() {
        return actor;
    }
}
