package de.uni_passau.fim.se2.catnip.recommendation;

import de.uni_passau.fim.se2.catnip.pqGram.PQGramProfile;
import de.uni_passau.fim.se2.catnip.pqGram.PQGramProfileCreator;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;

public class ActorWithProfile {
    private ActorDefinition actor;
    private PQGramProfile profile;

    public ActorWithProfile(ActorDefinition actor) {
        this.actor = actor;
        profile = PQGramProfileCreator.createPQProfile(actor);
    }

    public ActorWithProfile(ActorDefinition actor, PQGramProfile profile) {
        this.actor = actor;
        this.profile = profile;
    }

    public ActorDefinition getActorDefinition() {
        return actor;
    }

    public PQGramProfile getProfile() {
        return profile;
    }
}
