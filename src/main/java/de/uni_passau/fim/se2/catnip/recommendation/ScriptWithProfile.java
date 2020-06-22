package de.uni_passau.fim.se2.catnip.recommendation;

import de.uni_passau.fim.se2.catnip.pqGram.PQGramProfile;
import de.uni_passau.fim.se2.catnip.pqGram.PQGramProfileCreator;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;

public class ScriptWithProfile {
    private Script script;
    private PQGramProfile profile;

    public ScriptWithProfile(Script script) {
        this.script = script;
        profile = PQGramProfileCreator.createPQProfile(script);
    }

    public ScriptWithProfile(Script script, PQGramProfile profile) {
        this.script = script;
        this.profile = profile;
    }

    public Script getScript() {
        return script;
    }

    public PQGramProfile getProfile() {
        return profile;
    }
}
