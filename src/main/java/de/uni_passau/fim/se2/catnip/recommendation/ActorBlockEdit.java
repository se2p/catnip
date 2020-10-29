package de.uni_passau.fim.se2.catnip.recommendation;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;

public class ActorBlockEdit {
    private final ActorDefinition actor;
    private final EditSet edit;

    public ActorBlockEdit(ActorDefinition actor, EditSet edit) {
        this.actor = actor;
        this.edit = edit;
    }

    public ActorDefinition getActor() {
        return actor;
    }

    public EditSet getEdit() {
        return edit;
    }

    @Override
    public String toString() {
        return "ActorBlockEdit{"
                + "actor=" + actor.getIdent().getName()
                + ", edit=" + edit + '}';
    }
}
