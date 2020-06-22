package de.uni_passau.fim.se2.catnip.recommendation;

import de.uni_passau.fim.se2.catnip.pqGram.PQGramProfile;
import de.uni_passau.fim.se2.catnip.pqGram.PQGramProfileCreator;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

public class ProcedureWithProfile {
    private ProcedureDefinition procedureDefinition;
    private PQGramProfile profile;

    public ProcedureWithProfile(ProcedureDefinition procedureDefinition) {
        this.procedureDefinition = procedureDefinition;
        profile = PQGramProfileCreator.createPQProfile(procedureDefinition);
    }

    public ProcedureWithProfile(ProcedureDefinition procedureDefinition, PQGramProfile profile) {
        this.procedureDefinition = procedureDefinition;
        this.profile = profile;
    }

    public ProcedureDefinition getActorDefinition() {
        return procedureDefinition;
    }

    public PQGramProfile getProfile() {
        return profile;
    }
}
