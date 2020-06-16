package de.uni_passau.fim.se2.catnip;

import java.util.LinkedHashSet;
import java.util.Set;

public class PQGramProfile {
    private Set<LabelTuple> tuples;

    public PQGramProfile() {
        tuples = new LinkedHashSet<>();
    }

    public void addLabelTuple(LabelTuple tuple){
        tuples.add(tuple);
    }

    public Set<LabelTuple> getTuples() {
        return tuples;
    }
}
