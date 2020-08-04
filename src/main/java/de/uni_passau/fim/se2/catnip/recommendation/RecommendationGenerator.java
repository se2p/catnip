package de.uni_passau.fim.se2.catnip.recommendation;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.util.ArrayList;
import java.util.List;

public class RecommendationGenerator {

    public List<Recommendation> generateHints(Program sourceProgram, List<Program> possibleTargetPrograms) {
        EditsGenerator gen = new EditsGenerator(sourceProgram, possibleTargetPrograms);
        List<ActorBlockEdit> edits = gen.getEdits();
        List<Recommendation> hints = new ArrayList<>();
        for (ActorBlockEdit edit : edits) {
            hints.addAll(createHint(edit));
        }
        return hints;
    }

    private List<Recommendation> createHint(ActorBlockEdit edit) {
        if (edit instanceof ActorScriptEdit) {
            return createScriptRecommend((ActorScriptEdit) edit);
        } else {
            return createProcedureRecommend((ActorProcedureEdit) edit);
        }
    }

    private List<Recommendation> createProcedureRecommend(ActorProcedureEdit edit) {
        return null;
    }

    private List<Recommendation> createScriptRecommend(ActorScriptEdit edit) {
        return null;
    }
}
