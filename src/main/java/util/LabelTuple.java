package util;

import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

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

    public List<String> getLabels() {
        return labels;
    }
}
