package de.uni_passau.fim.se2.catnip.recommendation;

import de.uni_passau.fim.se2.catnip.pq_gram.Label;
import de.uni_passau.fim.se2.catnip.pq_gram.PQGramProfileCreator;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

import java.util.*;

public class RecommendationGenerator {

    public List<Recommendation> generateHints(Program sourceProgram, List<Program> possibleTargetPrograms)
            throws ImpossibleEditException {
        EditsGenerator gen = new EditsGenerator(sourceProgram, possibleTargetPrograms);
        List<ActorBlockEdit> edits = gen.getEdits();
        List<Recommendation> hints = new ArrayList<>();
        for (ActorBlockEdit edit : edits) {
            hints.addAll(createHint(edit));
        }
        return hints;
    }

    private List<Recommendation> createHint(ActorBlockEdit edit) throws ImpossibleEditException {
        if (edit instanceof ActorScriptEdit) {
            return createScriptRecommend((ActorScriptEdit) edit);
        } else {
            return createProcedureRecommend((ActorProcedureEdit) edit);
        }
    }

    private List<Recommendation> createProcedureRecommend(ActorProcedureEdit edit) {

        return null;
    }

    private List<Recommendation> createScriptRecommend(ActorScriptEdit edit) throws ImpossibleEditException {
        List<Recommendation> recommendations = new ArrayList<>();
        EditSet allEdits = edit.getEdit();
        Set<Edit> additions = allEdits.getAdditions();
        Map<Label, Set<Edit>> editsPerLabel = new LinkedHashMap<>();
        for (Edit addition : additions) {
            Label editNode = addition.getChangeNode();
            Set<Edit> currentEdits;
            if (!editsPerLabel.containsKey(editNode)) {
                currentEdits = new LinkedHashSet<>();
                currentEdits.add(addition);
                editsPerLabel.put(editNode, currentEdits);
            } else {
                currentEdits = editsPerLabel.get(editNode);
                currentEdits.add(addition);
            }
        }
        for (Label label : editsPerLabel.keySet()) {
            Set<Edit> currentEdits = editsPerLabel.get(label);
            Set<Edit> usedEdit = new LinkedHashSet<>();
            if (currentEdits.size() == PQGramProfileCreator.getQ() + 1) {
                Edit maxLeft = getLeft(currentEdits, PQGramProfileCreator.getQ() - 1);
                Edit maxRight = getRight(currentEdits, PQGramProfileCreator.getQ() - 1);
                recommendations.add(createScriptAdditionRecommendation(maxLeft.getLeftSiblings(),
                        maxRight.getRightSiblings(), label, edit.getScript(), edit.getActor()));
            } else {
                //Todo
            }
        }
        return recommendations;
    }

    private Edit getRight(Set<Edit> currentEdits, int maxCount) throws ImpossibleEditException {
        for (Edit edit : currentEdits) {
            if (edit.hasSiblings() && edit.getRightSiblings().size() == maxCount) {
                return edit;
            }
        }
        throw new ImpossibleEditException("Edits do not have required max number of right siblings.");
    }

    private Edit getLeft(Set<Edit> currentEdits, int maxCount) throws ImpossibleEditException {
        for (Edit edit : currentEdits) {
            if (edit.hasSiblings() && edit.getLeftSiblings().size() == maxCount) {
                return edit;
            }
        }
        throw new ImpossibleEditException("Edits do not have required max number of left siblings.");
    }

    private Recommendation createScriptAdditionRecommendation(List<Label> leftSiblings, List<Label> rightSiblings,
                                                              Label affectedNode, Script script,
                                                              ActorDefinition actor) {
        return new Recommendation(leftSiblings, rightSiblings, affectedNode, false, true, script, null, actor);
    }

    private Recommendation createScriptDeletionRecommendation(List<Label> leftSiblings, List<Label> rightSiblings,
                                                              Label affectedNode, Script script,
                                                              ActorDefinition actor) {
        return new Recommendation(leftSiblings, rightSiblings, affectedNode, true, false, script, null, actor);
    }

    private Recommendation createProcedureDeletionRecommendation(List<Label> leftSiblings, List<Label> rightSiblings,
                                                                 Label affectedNode, ProcedureDefinition procedure,
                                                                 ActorDefinition actor) {
        return new Recommendation(leftSiblings, rightSiblings, affectedNode, true, false, null, procedure, actor);
    }

    private Recommendation createProcedureAdditionRecommendation(List<Label> leftSiblings, List<Label> rightSiblings,
                                                                 Label affectedNode, ProcedureDefinition procedure,
                                                                 ActorDefinition actor) {
        return new Recommendation(leftSiblings, rightSiblings, affectedNode, false, true, null, procedure, actor);
    }
}
