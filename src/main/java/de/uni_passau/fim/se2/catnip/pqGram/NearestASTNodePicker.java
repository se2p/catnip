package de.uni_passau.fim.se2.catnip.pqGram;

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

    public static Program pickNearestProgram(Program source, List<Program> targets) {
        return (Program) pickNearestASTNode(source, targets);
    }

    private static ASTNode pickNearestASTNode(ASTNode source, List<? extends ASTNode> targets) {
        double minDistance = 1.0;
        PQGramProfile sourceProfile = PQGramProfileCreator.createPQProfile(source);
        List<ASTNode> minTargets = new ArrayList<>();
        for (ASTNode target : targets) {
            double currentDistance = PQGramUtil.calculateDistance(sourceProfile,
                    PQGramProfileCreator.createPQProfile(target));
            if (minDistance > currentDistance) {
                minDistance = currentDistance;
                minTargets = new ArrayList<>();
                minTargets.add(target);
            } else if (minDistance == currentDistance) {
                minTargets.add(target);
            }
        }

        return minTargets.get(rand.nextInt(minTargets.size()));
    }

    public static ActorDefinition pickNearestProgram(ActorDefinition source, List<ActorDefinition> targets) {
        return (ActorDefinition) pickNearestASTNode(source, targets);
    }

    public static Script pickNearestScript(Script source, List<Script> targets) {
        return (Script) pickNearestASTNode(source, targets);
    }

    public static ProcedureDefinition pickNearestProcedureDefinition(ProcedureDefinition source,
                                                                     List<ProcedureDefinition> targets) {
        return (ProcedureDefinition) pickNearestASTNode(source, targets);
    }
}
