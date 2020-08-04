package de.uni_passau.fim.se2.catnip.pq_gram;

import de.uni_passau.fim.se2.litterbox.ast.model.Script;

public class ScriptWithProfile extends ASTNodeWithProfile {
    public ScriptWithProfile(Script script) {
        super(script);
    }

    public ScriptWithProfile(Script script, PQGramProfile profile) {
        super(script,profile);
    }
}
