/*
 * Copyright (C) 2019 Catnip contributors
 *
 * This file is part of Catnip.
 *
 * Catnip is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Catnip is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Catnip. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.catnip.pq_gram;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class NearestASTNodePicker {
    private static final Random rand = new Random();

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
                minTargets.add((Program) target.getASTNode());
            } else if (minDistance == currentDistance) {
                minTargets.add((Program) target.getASTNode());
            }
        }

        return minTargets.get(rand.nextInt(minTargets.size()));
    }

    private static ASTNodeWithProfile pickNearestASTNode(ASTNode source, List<? extends ASTNode> targets) {
        List<ASTNodeWithProfile> targetsWithProfile = new ArrayList<>();
        for (ASTNode node : targets) {
            targetsWithProfile.add(new ASTNodeWithProfile(node, PQGramProfileCreator.createPQProfile(node)));
        }
        ASTNodeWithProfile sourceWithProfile = new ASTNodeWithProfile(source,
                PQGramProfileCreator.createPQProfile(source));
        return pickNearestASTNode(sourceWithProfile, targetsWithProfile);
    }

    private static ASTNodeWithProfile pickNearestASTNode(ASTNodeWithProfile source,
                                                         List<? extends ASTNodeWithProfile> targets) {
        double minDistance = 1.0;
        PQGramProfile sourceProfile = source.getProfile();
        List<ASTNodeWithProfile> minTargets = new ArrayList<>();
        for (ASTNodeWithProfile target : targets) {
            PQGramProfile currentProfile = target.getProfile();
            double currentDistance = PQGramUtil.calculateDistance(sourceProfile, currentProfile);
            if (minDistance > currentDistance) {
                minDistance = currentDistance;
                minTargets = new ArrayList<>();
                minTargets.add(target);
            } else if (minDistance == currentDistance) {
                minTargets.add(target);
            }
        }
        int index = rand.nextInt(minTargets.size());
        return minTargets.get(index);
    }

    public static ActorWithProfile pickNearestActor(ActorDefinition source, List<ActorDefinition> targets) {
        for (ActorDefinition target : targets) {
            if (target.getIdent().getName().equals(source.getIdent().getName())) {
                return new ActorWithProfile(target);
            }
        }
        ASTNodeWithProfile astNode = pickNearestASTNode(source, targets);
        return new ActorWithProfile((ActorDefinition) astNode.getASTNode(), astNode.getProfile());
    }

    public static ActorWithProfile pickNearestActor(ActorWithProfile source, List<ActorWithProfile> targets) {
        for (ActorWithProfile target : targets) {
            if (((ActorDefinition) target.getASTNode()).getIdent().getName().equals(((ActorDefinition) source.getASTNode()).getIdent().getName())) {
                return new ActorWithProfile((ActorDefinition) target.getASTNode(), target.getProfile());
            }
        }
        ASTNodeWithProfile astNode = pickNearestASTNode(source, targets);
        return new ActorWithProfile((ActorDefinition) astNode.getASTNode(), astNode.getProfile());
    }

    public static ScriptWithProfile pickNearestScript(Script source, List<Script> targets) {
        ASTNodeWithProfile astNode = pickNearestASTNode(source, targets);
        return new ScriptWithProfile((Script) astNode.getASTNode(), astNode.getProfile());
    }

    public static ScriptWithProfile pickNearestScript(ScriptWithProfile source, List<ScriptWithProfile> targets) {
        ASTNodeWithProfile astNode = pickNearestASTNode(source, targets);
        return new ScriptWithProfile((Script) astNode.getASTNode(), astNode.getProfile());
    }

    public static ProcedureWithProfile pickNearestProcedureDefinition(ProcedureDefinition source,
                                                                      List<ProcedureDefinition> targets) {
        ASTNodeWithProfile astNode = pickNearestASTNode(source, targets);
        return new ProcedureWithProfile((ProcedureDefinition) astNode.getASTNode(), astNode.getProfile());
    }

    public static ProcedureWithProfile pickNearestProcedureDefinition(ProcedureWithProfile source,
                                                                      List<ProcedureWithProfile> targets) {
        ASTNodeWithProfile astNode = pickNearestASTNode(source, targets);
        return new ProcedureWithProfile((ProcedureDefinition) astNode.getASTNode(), astNode.getProfile());
    }
}
