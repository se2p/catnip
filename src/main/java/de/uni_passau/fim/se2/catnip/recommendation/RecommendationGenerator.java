package de.uni_passau.fim.se2.catnip.recommendation;

import de.uni_passau.fim.se2.catnip.pq_gram.Label;
import de.uni_passau.fim.se2.catnip.pq_gram.PQGramProfileCreator;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

import java.util.*;

public class RecommendationGenerator {
    private List<Label> fullEmpty;

    public RecommendationGenerator() {
        fullEmpty = new ArrayList<>();
        for (int i = 0; i < PQGramProfileCreator.getQ() - 1; i++) {
            fullEmpty.add(new Label(PQGramProfileCreator.NULL_NODE, null));
        }
    }

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
        if (edit instanceof ActorProcedureEdit) {
            return createProcedureRecommend((ActorProcedureEdit) edit);
        } else if (edit instanceof ActorScriptEdit) {
            return createScriptRecommend((ActorScriptEdit) edit);
        }
        return null;
    }

    private List<Recommendation> createProcedureRecommend(ActorProcedureEdit edit) {

        return null;
    }

    private List<Recommendation> createScriptRecommend(ActorScriptEdit edit) throws ImpossibleEditException {
        List<Recommendation> recommendations = new ArrayList<>();
        EditSet allEdits = edit.getEdit();
        Set<Edit> additions = allEdits.getAdditions();
        Map<Label, Set<Edit>> editsPerLabel = getEditsPerLabel(additions);
        for (Label label : editsPerLabel.keySet()) {
            Set<Edit> currentEdits = new LinkedHashSet<>(editsPerLabel.get(label));
            if (currentEdits.size() == PQGramProfileCreator.getQ() + 1) {
                recommendations.add(generateRecommendForSingleBlock(currentEdits, label, edit.getScript(), edit.getActor(), true));
            } else if (currentEdits.size() == 1) {
                Set<Label> parents = getParents(currentEdits);
                assert (parents.size() == 1);
                recommendations.add(createScriptAdditionRecommendation(fullEmpty,
                        fullEmpty, label, (Label) parents.toArray()[0], edit.getScript(), edit.getActor()));
            } else {
                Set<Label> parents = getParents(currentEdits);
                for (Label parent : parents) {
                    Set<Edit> editsWithSameParent = getEditsFromParent(parent, currentEdits);
                    if (editsWithSameParent.size() == PQGramProfileCreator.getQ() + 1) {
                        recommendations.add(generateRecommendForSingleBlock(editsWithSameParent, label, edit.getScript(), edit.getActor(), true));
                    } else {
                        recommendations.addAll(generateMultipleRecommendationsForSingleBlock(editsWithSameParent, label, edit.getScript(), edit.getActor(), true));
                    }
                }
            }
        }
        Set<Edit> deletion = allEdits.getDeletions();
        editsPerLabel = getEditsPerLabel(deletion);
        for (Label label : editsPerLabel.keySet()) {
            Set<Edit> currentEdits = editsPerLabel.get(label);
            if (currentEdits.size() == PQGramProfileCreator.getQ() + 1) {
                recommendations.add(generateRecommendForSingleBlock(currentEdits, label, edit.getScript(), edit.getActor(), false));
            } else if (currentEdits.size() == 1) {
                Set<Label> parents = getParents(currentEdits);
                assert (parents.size() == 1);
                recommendations.add(createScriptDeletionRecommendation(fullEmpty,
                        fullEmpty, label, (Label) parents.toArray()[0], edit.getScript(), edit.getActor()));
            } else {
                Set<Label> parents = getParents(currentEdits);
                for (Label parent : parents) {
                    Set<Edit> editsWithSameParent = getEditsFromParent(parent, currentEdits);
                    if (editsWithSameParent.size() == PQGramProfileCreator.getQ() + 1) {
                        recommendations.add(generateRecommendForSingleBlock(editsWithSameParent, label, edit.getScript(), edit.getActor(), false));
                    } else {
                        recommendations.addAll(generateMultipleRecommendationsForSingleBlock(editsWithSameParent, label, edit.getScript(), edit.getActor(), false));
                    }
                }
            }
        }
        return recommendations;
    }

    private List<Recommendation> generateMultipleRecommendationsForSingleBlock(Set<Edit> editsWithSameParent, Label label, Script script, ActorDefinition actor, boolean isAddition) {
        List<Recommendation> recommendations = new ArrayList<>();
        // TODO: 03.09.2020
        return recommendations;
    }

    private Set<Edit> getEditsFromParent(Label parent, Set<Edit> currentEdits) {
        Set<Edit> edits = new LinkedHashSet<>();
        for (Edit current : currentEdits) {
            if (current.getParent().equals(parent)) {
                edits.add(current);
                currentEdits.remove(current);
            }
        }
        return edits;
    }

    private Recommendation generateRecommendForSingleBlock(Set<Edit> currentEdits, Label label, Script script, ActorDefinition actor, boolean isAddition) throws ImpossibleEditException {
        Edit maxLeft = getLeft(currentEdits, PQGramProfileCreator.getQ() - 1);
        Edit maxRight = getRight(currentEdits, PQGramProfileCreator.getQ() - 1);
        Set<Label> parents = getParents(currentEdits);
        assert (parents.size() == 1);
        if (isAddition) {
            return createScriptAdditionRecommendation(maxLeft.getLeftSiblings(),
                    maxRight.getRightSiblings(), label, (Label) parents.toArray()[0], script, actor);
        } else {
            return createScriptDeletionRecommendation(maxLeft.getLeftSiblings(),
                    maxRight.getRightSiblings(), label, (Label) parents.toArray()[0], script, actor);
        }
    }

    private Map<Label, Set<Edit>> getEditsPerLabel(Set<Edit> additions) {
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
        return editsPerLabel;
    }

    private Set<Label> getParents(Set<Edit> currentEdits) {
        Set<Label> parents = new LinkedHashSet<>();
        for (Edit edit : currentEdits) {
            parents.add(edit.getParent());
        }
        return parents;
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
                                                              Label affectedNode, Label parentNode, Script script,
                                                              ActorDefinition actor) {
        return new Recommendation(leftSiblings, rightSiblings, affectedNode, parentNode, false, true, script, null, actor);
    }

    private Recommendation createScriptDeletionRecommendation(List<Label> leftSiblings, List<Label> rightSiblings,
                                                              Label affectedNode, Label parentNode, Script script,
                                                              ActorDefinition actor) {
        return new Recommendation(leftSiblings, rightSiblings, affectedNode, parentNode, true, false, script, null, actor);
    }

    private Recommendation createProcedureDeletionRecommendation(List<Label> leftSiblings, List<Label> rightSiblings,
                                                                 Label affectedNode, Label parentNode, ProcedureDefinition procedure,
                                                                 ActorDefinition actor) {
        return new Recommendation(leftSiblings, rightSiblings, affectedNode, parentNode, true, false, null, procedure, actor);
    }

    private Recommendation createProcedureAdditionRecommendation(List<Label> leftSiblings, List<Label> rightSiblings,
                                                                 Label affectedNode, Label parentNode, ProcedureDefinition procedure,
                                                                 ActorDefinition actor) {
        return new Recommendation(leftSiblings, rightSiblings, affectedNode, parentNode, false, true, null, procedure, actor);
    }
}
