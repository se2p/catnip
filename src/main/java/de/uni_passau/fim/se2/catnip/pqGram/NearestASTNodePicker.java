package de.uni_passau.fim.se2.catnip.pqGram;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

import java.util.List;

public abstract class NearestASTNodePicker {
    public static Program pickNearestProgram(Program source, List<Program> targets) {
        return (Program) pickNearestASTNode(source, targets);
    }

    private static ASTNode pickNearestASTNode(ASTNode source, List<? extends ASTNode> targets) {
        return null;
    }

    public static ActorDefinition pickNearestProgram(ActorDefinition source, List<ActorDefinition> targets) {
        return (ActorDefinition) pickNearestASTNode(source, targets);
    }

    public static Script pickNearestScript(Script source, List<Script> targets) {
        return (Script) pickNearestASTNode(source, targets);
    }

    public static ProcedureDefinition pickNearestProcedureDefinition(ProcedureDefinition source, List<ProcedureDefinition> targets) {
        return (ProcedureDefinition) pickNearestASTNode(source, targets);
    }
}
