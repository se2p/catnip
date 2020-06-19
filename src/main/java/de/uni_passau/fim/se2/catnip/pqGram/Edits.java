package de.uni_passau.fim.se2.catnip.pqGram;

import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;

public class Edits {
    private List<Pair<String,String>> additions;
    private List<Pair<String,String>> deletions;

    public Edits() {
        additions = new ArrayList<>();
        deletions = new ArrayList<>();
    }

    public Edits(List<Pair<String,String>> additions, List<Pair<String,String>> deletions) {
        this.additions = new ArrayList<Pair<String,String>>(additions);
        this.deletions = new ArrayList<Pair<String,String>>(deletions);
    }

    public void addAddition(Pair<String,String> tuple) {
        additions.add(tuple);
    }

    public void addDeletion(Pair<String,String> tuple) {
        deletions.add(tuple);
    }

    public List<Pair<String,String>> getAdditions() {
        return additions;
    }

    public List<Pair<String,String>> getDeletions() {
        return deletions;
    }
}
