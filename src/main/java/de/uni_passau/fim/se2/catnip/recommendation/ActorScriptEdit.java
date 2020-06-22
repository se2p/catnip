package de.uni_passau.fim.se2.catnip.recommendation;

import de.uni_passau.fim.se2.catnip.pqGram.Edits;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;

public class ActorScriptEdit {
    private ActorDefinition actor;
    private Script script;
    private Edits edit;

    public ActorScriptEdit(ActorDefinition actor, Script script, Edits edit) {
        this.actor = actor;
        this.script = script;
        this.edit = edit;
    }

    public ActorDefinition getActor() {
        return actor;
    }

    public Script getScript() {
        return script;
    }

    public Edits getEdit() {
        return edit;
    }

    @Override
    public String toString() {
        return "ActorScriptEdit{" +
                "actor=" + actor.getIdent().getName() +
                ", script=" + script.getStmtList().getStmts().toString() +
                ", edit=" + edit +
                '}';
    }
}
