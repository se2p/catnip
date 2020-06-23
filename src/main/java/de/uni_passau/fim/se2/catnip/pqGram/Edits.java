package de.uni_passau.fim.se2.catnip.pqGram;

import org.javatuples.Pair;

import java.util.LinkedHashSet;
import java.util.Set;

public class Edits {
    private Set<Pair<String, String>> additions;
    private Set<Pair<String, String>> deletions;

    public Edits() {
        additions = new LinkedHashSet<>();
        deletions = new LinkedHashSet<>();
    }

    public Edits(Set<Pair<String, String>> additions, Set<Pair<String, String>> deletions) {
        this.additions = new LinkedHashSet<>(additions);
        this.deletions = new LinkedHashSet(deletions);
    }

    public void addAddition(Pair<String, String> tuple) {
        additions.add(tuple);
    }

    public void addDeletion(Pair<String, String> tuple) {
        deletions.add(tuple);
    }

    public Set<Pair<String, String>> getAdditions() {
        return additions;
    }

    public Set<Pair<String, String>> getDeletions() {
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
