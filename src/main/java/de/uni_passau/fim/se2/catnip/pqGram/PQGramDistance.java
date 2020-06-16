package de.uni_passau.fim.se2.catnip.pqGram;


import java.util.LinkedHashSet;
import java.util.Set;

public abstract class PQGramDistance {

    public static double calculateDistance(PQGramProfile profile1, PQGramProfile profile2) {
        Set<LabelTuple> intersection = new LinkedHashSet<>(profile1.getTuples());
        intersection.retainAll(profile2.getTuples());
        double division = (double) intersection.size() / (profile1.getTuples().size() + profile2.getTuples().size());
        return 1 - (2 * division);
    }
}
