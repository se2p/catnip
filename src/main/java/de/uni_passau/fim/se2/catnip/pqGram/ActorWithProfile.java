package de.uni_passau.fim.se2.catnip.pqGram;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;

public class ActorWithProfile extends ASTNodeWithProfile {

    public ActorWithProfile(ActorDefinition actor) {
        super(actor);
    }

    public ActorWithProfile(ActorDefinition actor, PQGramProfile profile) {
        super(actor, profile);
    }
}
