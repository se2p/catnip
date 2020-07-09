package de.uni_passau.fim.se2.catnip.pqGram;


import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.HashBag;
import org.javatuples.Pair;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class PQGramUtil {

    public static double calculateDistance(PQGramProfile profile1, PQGramProfile profile2) {
        Bag<LabelTuple> intersection = new HashBag<>(profile1.getTuples());
        intersection.retainAll(profile2.getTuples());
        double division = (double) intersection.size() / (profile1.getTuples().size() + profile2.getTuples().size());
        return 1 - (2 * division);
    }

    /*
    public static Set<String> commonSubtrees(PQGramProfile profile1, PQGramProfile profile2) {
        Set<LabelTuple> intersection = new LinkedHashSet<>(profile1.getTuples());
        intersection.retainAll(profile2.getTuples());
        Set<String> commonRoots = new LinkedHashSet<>();
        for (LabelTuple labelTuple : intersection) {
            List<String> currentTuple = labelTuple.getLabels();
            commonRoots.add(currentTuple.get(0));
            for (int i = 1; i < currentTuple.size(); i++) {
                commonRoots.remove(currentTuple.get(i));
            }
        }
        return commonRoots;
    }

     */

    public static Edits identifyEdits(PQGramProfile source, PQGramProfile target) {
        Edits edits = new Edits();
        Set<LabelTuple> intersection = new LinkedHashSet<>(source.getTuples());
        intersection.retainAll(target.getTuples());
        Set<LabelTuple> extraTuples = new LinkedHashSet<>(source.getTuples());
        extraTuples.removeAll(intersection);
        Set<LabelTuple> missingTuples = new LinkedHashSet<>(target.getTuples());
        missingTuples.removeAll(intersection);

        for (LabelTuple tuple : missingTuples) {
            for (int i = 1; i < PQGramProfileCreator.getP(); i++) {
                if (intersectionNotContainsLabel(intersection, tuple.getLabels().get(i).getLabel()) && !tuple.getLabels().get(i).getLabel().contains("Metadata")) {
                    edits.addAddition(new Pair<>(tuple.getLabels().get(i - 1), tuple.getLabels().get(i)));
                }
            }
            for (int i = PQGramProfileCreator.getP(); i < tuple.getLabels().size(); i++) {
                if (intersectionNotContainsLabel(intersection, tuple.getLabels().get(i).getLabel()) && !tuple.getLabels().get(i).getLabel().contains("Metadata")) {
                    edits.addAddition(new Pair<>(tuple.getLabels().get(PQGramProfileCreator.getP() - 1),
                            tuple.getLabels().get(i)));
                }
            }
        }

        for (LabelTuple tuple : extraTuples) {
            for (int i = 1; i < PQGramProfileCreator.getP(); i++) {
                if (intersectionNotContainsLabel(intersection, tuple.getLabels().get(i).getLabel()) && !tuple.getLabels().get(i).getLabel().contains("Metadata")) {
                    edits.addDeletion(new Pair<>(tuple.getLabels().get(i - 1), tuple.getLabels().get(i)));
                }
            }
            for (int i = PQGramProfileCreator.getP(); i < tuple.getLabels().size(); i++) {
                if (intersectionNotContainsLabel(intersection, tuple.getLabels().get(i).getLabel()) && !tuple.getLabels().get(i).getLabel().contains("Metadata")) {
                    edits.addDeletion(new Pair<>(tuple.getLabels().get(PQGramProfileCreator.getP()-1),
                            tuple.getLabels().get(i)));
                }
            }
        }

        return edits;
    }

    private static boolean intersectionNotContainsLabel(Set<LabelTuple> intersection, String label) {
        for (LabelTuple tuple : intersection) {
            if (tuple.containsLabel(label)) {
                return false;
            }
        }
        return true;
    }
}
