package de.uni_passau.fim.se2.catnip.recommendation;

import java.util.LinkedHashSet;
import java.util.Set;

public class EditSet {
    private Set<Edit> additions;
    private Set<Edit> deletions;

    public EditSet() {
        additions = new LinkedHashSet<>();
        deletions = new LinkedHashSet<>();
    }

    public EditSet(Set<Edit> additions, Set<Edit> deletions) {
        this.additions = new LinkedHashSet<>(additions);
        this.deletions = new LinkedHashSet<>(deletions);
    }

    public void addAddition(Edit edit) {
        additions.add(edit);
    }

    public void addDeletion(Edit deletion) {
        deletions.add(deletion);
    }

    public Set<Edit> getAdditions() {
        return additions;
    }

    public Set<Edit> getDeletions() {
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
