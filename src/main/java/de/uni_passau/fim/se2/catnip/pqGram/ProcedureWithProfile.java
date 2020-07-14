package de.uni_passau.fim.se2.catnip.pqGram;


import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

public class ProcedureWithProfile extends ASTNodeWithProfile{

    public ProcedureWithProfile(ProcedureDefinition procedureDefinition) {
       super(procedureDefinition);
    }

    public ProcedureWithProfile(ProcedureDefinition procedureDefinition, PQGramProfile profile) {
        super(procedureDefinition,profile);
    }
}
