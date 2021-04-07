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
    private final Label parentNode;
    private final boolean deletion;
    private final boolean addition;
    private final Script script;
    private final ProcedureDefinition procedure;
    private final ActorDefinition actor;

    public Recommendation(List<Label> previousNodes, List<Label> followingNodes, Label affectedNode, Label parentNode, boolean deletion,
                          boolean addition, Script script, ProcedureDefinition procedure, ActorDefinition actor) {
        assert (deletion && !addition) || (!deletion && addition);
        assert (procedure == null && script != null) || (procedure != null && script == null);
        this.previousNodes = previousNodes;
        this.followingNodes = followingNodes;
        this.parentNode = parentNode;
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

    public Label getParentNode() {
        return parentNode;
    }
}
