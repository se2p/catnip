package de.uni_passau.fim.se2.catnip.pqGram;

import org.javatuples.Pair;

import java.util.LinkedHashSet;
import java.util.Set;

public class Edits {
    private Set<Pair<Label, Label>> additions;
    private Set<Pair<Label, Label>> deletions;

    public Edits() {
        additions = new LinkedHashSet<>();
        deletions = new LinkedHashSet<>();
    }

    public Edits(Set<Pair<Label, Label>> additions, Set<Pair<Label, Label>> deletions) {
        this.additions = new LinkedHashSet<>(additions);
        this.deletions = new LinkedHashSet(deletions);
    }

    public void addAddition(Pair<Label, Label> tuple) {
        additions.add(tuple);
    }

    public void addDeletion(Pair<Label, Label> tuple) {
        deletions.add(tuple);
    }

    public Set<Pair<Label, Label>> getAdditions() {
        return additions;
    }

    public Set<Pair<Label, Label>> getDeletions() {
        return deletions;
    }

    @Override
    public String toString() {
        return "Edits{" +
                "additions=" + additions +
                ", deletions=" + deletions +
                '}';
    }
}
