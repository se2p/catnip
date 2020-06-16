package de.uni_passau.fim.se2.catnip.pqGram;

import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LabelTuple {
    private final List<String> labels;

    public LabelTuple(List<String> anc, List<String> sib) {
        Preconditions.checkArgument(anc.size() == PQGramProfileCreator.getP(), "Too many ancestors for the specified " +
                "p.");
        Preconditions.checkArgument(sib.size() == PQGramProfileCreator.getQ(), "Too many siblings for the specified q" +
                ".");
        labels = new ArrayList<String>();
        labels.addAll(anc);
        labels.addAll(sib);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LabelTuple that = (LabelTuple) o;
        return Objects.equals(getLabels(), that.getLabels());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLabels());
    }

    public List<String> getLabels() {
        return labels;
    }
}
