package pqGram;


import java.util.LinkedHashSet;
import java.util.Set;

public abstract class PQGramDistance {

    public static double calculateDistance(PQGramProfile profile1, PQGramProfile profile2) {
        Set<LabelTuple> intersection = new LinkedHashSet<>(profile1.getTuples());
        intersection.retainAll(profile2.getTuples());
        Set<LabelTuple> union = new LinkedHashSet<>(profile1.getTuples());
        union.addAll(profile2.getTuples());
        double division = (double) intersection.size() / union.size();
        return 1 - (2 * division);
    }
}
