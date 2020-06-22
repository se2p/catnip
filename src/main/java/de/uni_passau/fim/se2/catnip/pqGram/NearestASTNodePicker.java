package de.uni_passau.fim.se2.catnip.pqGram;

import de.uni_passau.fim.se2.catnip.recommendation.ActorWithProfile;
import de.uni_passau.fim.se2.catnip.recommendation.ProcedureWithProfile;
import de.uni_passau.fim.se2.catnip.recommendation.ProgramWithProfile;
import de.uni_passau.fim.se2.catnip.recommendation.ScriptWithProfile;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class NearestASTNodePicker {
    private static Random rand = new Random();

    public static Program pickNearestProgram(Program source, List<ProgramWithProfile> targets) {
        PQGramProfile sourceProfile = PQGramProfileCreator.createPQProfile(source);
        double minDistance = 1.0;
        List<Program> minTargets = new ArrayList<>();

        for (ProgramWithProfile target : targets) {
            double currentDistance = PQGramUtil.calculateDistance(sourceProfile,
                    target.getProfile());
            if (minDistance > currentDistance) {
                minDistance = currentDistance;
                minTargets = new ArrayList<>();
                minTargets.add(target.getProgram());
            } else if (minDistance == currentDistance) {
                minTargets.add(target.getProgram());
            }
        }

        return minTargets.get(rand.nextInt(minTargets.size()));
    }

    private static ASTNodeWithProfile pickNearestASTNode(ASTNode source, List<? extends ASTNode> targets) {
        double minDistance = 1.0;
        PQGramProfile sourceProfile = PQGramProfileCreator.createPQProfile(source);
        List<PQGramProfile> targetProfileList = new ArrayList<>();
        List<ASTNode> minTargets = new ArrayList<>();
        for (ASTNode target : targets) {
            PQGramProfile currentProfile = PQGramProfileCreator.createPQProfile(target);
            double currentDistance = PQGramUtil.calculateDistance(sourceProfile, currentProfile);
            if (minDistance > currentDistance) {
                minDistance = currentDistance;
                minTargets = new ArrayList<>();
                targetProfileList = new ArrayList<>();
                minTargets.add(target);
                targetProfileList.add(currentProfile);
            } else if (minDistance == currentDistance) {
                minTargets.add(target);
                targetProfileList.add(currentProfile);
            }
        }
        int index = rand.nextInt(minTargets.size());
        return new ASTNodeWithProfile(minTargets.get(index), targetProfileList.get(index));
    }

    public static ActorWithProfile pickNearestActor(ActorDefinition source, List<ActorDefinition> targets) {
        ASTNodeWithProfile astNode = pickNearestASTNode(source, targets);
        return new ActorWithProfile((ActorDefinition) astNode.getASTNode(), astNode.getProfile());
    }

    public static ScriptWithProfile pickNearestScript(Script source, List<Script> targets) {
        ASTNodeWithProfile astNode = pickNearestASTNode(source, targets);
        return new ScriptWithProfile((Script) astNode.getASTNode(), astNode.getProfile());
    }

    public static ProcedureWithProfile pickNearestProcedureDefinition(ProcedureDefinition source,
                                                                      List<ProcedureDefinition> targets) {
        ASTNodeWithProfile astNode = pickNearestASTNode(source, targets);
        return new ProcedureWithProfile((ProcedureDefinition) astNode.getASTNode(), astNode.getProfile());
    }
}
