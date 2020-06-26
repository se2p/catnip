package de.uni_passau.fim.se2.catnip.recommendation;

import de.uni_passau.fim.se2.catnip.pqGram.Edits;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;

public class ActorScriptEdit extends ActorBlockEdit {
    private Script script;

    public ActorScriptEdit(ActorDefinition actor, Script script, Edits edit) {
        super(actor, edit);
        this.script = script;

    }

    public Script getScript() {
        return script;
    }


    @Override
    public String toString() {
        return "ActorScriptEdit{" +
                "actor=" + getActor().getIdent().getName() +
                ", script=" + script.getStmtList().getStmts().toString() +
                ", edit=" + getEdit() +
                '}';
    }
}
