/*
 * Copyright (C) 2019 Catnip contributors
 *
 * This file is part of Catnip.
 *
 * Catnip is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Catnip is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Catnip. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.catnip.pq_gram;

import de.uni_passau.fim.se2.catnip.recommendation.Edit;
import de.uni_passau.fim.se2.catnip.recommendation.EditSet;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.HashBag;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class PQGramUtil {

    public static double calculateDistance(PQGramProfile profile1, PQGramProfile profile2) {
        if (profile1.getTuples().isEmpty() && profile2.getTuples().isEmpty()) {
            return 0;
        }
        Bag<LabelTuple> intersection = new HashBag<>(profile1.getTuples());
        intersection.retainAll(profile2.getTuples());
        double division = (double) intersection.size() / (profile1.getTuples().size() + profile2.getTuples().size());
        return 1 - (2 * division);
    }

    public static EditSet identifyEdits(PQGramProfile source, PQGramProfile target) {
        EditSet editSet = new EditSet();
        Set<LabelTuple> intersection = new LinkedHashSet<>(target.getTuples());
        intersection.retainAll(source.getTuples());
        Set<LabelTuple> extraTuples = new LinkedHashSet<>(source.getTuples());
        extraTuples.removeAll(intersection);
        Set<LabelTuple> missingTuples = new LinkedHashSet<>(target.getTuples());
        missingTuples.removeAll(intersection);

        for (LabelTuple tuple : missingTuples) {
            for (int i = 1; i < PQGramProfileCreator.getP(); i++) {
                if (!tuple.getLabels().get(i).getLabel().contains("Metadata") && !tuple.getLabels().get(i).getLabel().contains("Literal") && !tuple.getLabels().get(i).getLabel().contains("StrId")
                        && intersectionNotContainsLabel(intersection, tuple.getLabels().get(i))) {
                    editSet.addAddition(new Edit(tuple.getLabels().get(i - 1), tuple.getLabels().get(i)));
                }
            }
            List<Label> leftSiblings = new ArrayList<>();
            List<Label> rightSiblings = new ArrayList<>(tuple.getLabels().subList(PQGramProfileCreator.getP(),
                    tuple.getLabels().size()));
            Label current = null;
            for (int i = PQGramProfileCreator.getP(); i < tuple.getLabels().size(); i++) {
                if (!rightSiblings.isEmpty()) {
                    current = rightSiblings.get(0);
                    rightSiblings.remove(0);
                }
                if (!tuple.getLabels().get(i).getLabel().contains("Metadata") && !tuple.getLabels().get(i).getLabel().contains("Literal") && !tuple.getLabels().get(i).getLabel().contains("StrId")
                        && intersectionNotContainsLabel(intersection, tuple.getLabels().get(i))) {
                    editSet.addAddition(new Edit(tuple.getLabels().get(PQGramProfileCreator.getP() - 1),
                            tuple.getLabels().get(i), new ArrayList<>(leftSiblings), new ArrayList<>(rightSiblings)));
                }
                if (current != null) {
                    leftSiblings.add(current);
                }
            }
        }

        for (LabelTuple tuple : extraTuples) {
            for (int i = 1; i < PQGramProfileCreator.getP(); i++) {
                if (!tuple.getLabels().get(i).getLabel().contains("Metadata") && !tuple.getLabels().get(i).getLabel().contains("Literal") && !tuple.getLabels().get(i).getLabel().contains("StrId")
                        && intersectionNotContainsLabel(intersection, tuple.getLabels().get(i))) {
                    editSet.addDeletion(new Edit(tuple.getLabels().get(i - 1), tuple.getLabels().get(i)));
                }
            }
            List<Label> leftSiblings = new ArrayList<>();
            List<Label> rightSiblings = new ArrayList<>(tuple.getLabels().subList(PQGramProfileCreator.getP(),
                    tuple.getLabels().size()));
            Label current = null;
            for (int i = PQGramProfileCreator.getP(); i < tuple.getLabels().size(); i++) {
                if (!rightSiblings.isEmpty()) {
                    current = rightSiblings.get(0);
                    rightSiblings.remove(0);
                }
                if (!tuple.getLabels().get(i).getLabel().contains("Metadata") && !tuple.getLabels().get(i).getLabel().contains("Literal") && !tuple.getLabels().get(i).getLabel().contains("StrId")
                        && intersectionNotContainsLabel(intersection, tuple.getLabels().get(i))) {
                    editSet.addDeletion(new Edit(tuple.getLabels().get(PQGramProfileCreator.getP() - 1),
                            tuple.getLabels().get(i), new ArrayList<>(leftSiblings), new ArrayList<>(rightSiblings)));
                }
                if (current != null) {
                    leftSiblings.add(current);
                }
            }
        }

        return editSet;
    }

    private static boolean intersectionNotContainsLabel(Set<LabelTuple> intersection, Label label) {
        for (LabelTuple tuple : intersection) {
            if (tuple.containsLabel(label)) {
                return false;
            }
        }
        return true;
    }
}
