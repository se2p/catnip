package de.uni_passau.fim.se2.catnip.recommendation;

import de.uni_passau.fim.se2.catnip.pqGram.Edits;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;

public class ActorBlockEdit {
    private ActorDefinition actor;
    private Edits edit;

    public ActorBlockEdit(ActorDefinition actor, Edits edit) {
        this.actor = actor;
        this.edit = edit;
    }

    public ActorDefinition getActor() {
        return actor;
    }

    public Edits getEdit() {
        return edit;
    }

    @Override
    public String toString() {
        return "ActorBlockEdit{" +
                "actor=" + actor.getIdent().getName() +
                ", edit=" + edit +
                '}';
    }
}
