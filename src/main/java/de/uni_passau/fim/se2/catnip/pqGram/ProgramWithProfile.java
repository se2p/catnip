package de.uni_passau.fim.se2.catnip.pqGram;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;

public class ProgramWithProfile extends ASTNodeWithProfile {

    public ProgramWithProfile(Program program) {
        super(program);
    }

    public ProgramWithProfile(Program program, PQGramProfile profile) {
        super(program, profile);
    }
}
