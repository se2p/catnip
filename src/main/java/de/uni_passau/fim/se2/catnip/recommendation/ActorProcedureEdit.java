package de.uni_passau.fim.se2.catnip.recommendation;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

public class ActorProcedureEdit extends ActorBlockEdit {
    private final ProcedureDefinition procedure;

    public ActorProcedureEdit(ActorDefinition actor, ProcedureDefinition procedure, EditSet edit) {
        super(actor, edit);
        this.procedure = procedure;
    }

    public ProcedureDefinition getProcedure() {
        return procedure;
    }

    @Override
    public String toString() {
        return "ActorProcedureEdit{" + "actor=" + getActor().getIdent().getName()
                + "procedure=" + procedure.getStmtList().getStmts().toString()
                + ", edit=" + getEdit() + '}';
    }
}
