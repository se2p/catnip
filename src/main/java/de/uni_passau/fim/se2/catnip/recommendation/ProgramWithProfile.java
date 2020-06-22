package de.uni_passau.fim.se2.catnip.recommendation;

import de.uni_passau.fim.se2.catnip.pqGram.PQGramProfile;
import de.uni_passau.fim.se2.catnip.pqGram.PQGramProfileCreator;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

public class ProgramWithProfile {
    private Program program;
    private PQGramProfile profile;

    public ProgramWithProfile(Program program) {
        this.program = program;
        profile = PQGramProfileCreator.createPQProfile(program);
    }

    public Program getProgram() {
        return program;
    }

    public PQGramProfile getProfile() {
        return profile;
    }
}
