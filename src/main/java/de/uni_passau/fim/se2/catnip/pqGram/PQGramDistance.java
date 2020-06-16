package de.uni_passau.fim.se2.catnip.pqGram;


import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.HashBag;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class PQGramDistance {

    public static double calculateDistance(PQGramProfile profile1, PQGramProfile profile2) {
        Bag<LabelTuple> intersection = new HashBag<>(profile1.getTuples());
        intersection.retainAll(profile2.getTuples());
        double division = (double) intersection.size() / (profile1.getTuples().size() + profile2.getTuples().size());
        return 1 - (2 * division);
    }
}
