package de.uni_passau.fim.se2.catnip.recommendation;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.util.List;

public class Hint {
    private Program program;
    private List<Recommendation> recommendations;

    public Hint(Program program, List<Recommendation> recommendations) {
        this.program = program;
        this.recommendations = recommendations;
    }

    public Program getProgram() {
        return program;
    }

    public List<Recommendation> getRecommendations() {
        return recommendations;
    }
}
