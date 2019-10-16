package recommendation;

import scratch.structure.Project;

import java.util.List;

public class Recommender {
    private Project currentProject;
    private List<Project> referenceProjects;
    private double neededCorrectness;

    public Recommender() {
    }

    public Recommender(Project currentProject, List<Project> referenceProjects, double neededCorrectness) {
        this.currentProject = currentProject;
        this.referenceProjects = referenceProjects;
        this.neededCorrectness = neededCorrectness;
    }
}
