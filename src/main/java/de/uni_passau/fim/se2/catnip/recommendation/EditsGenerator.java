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
package de.uni_passau.fim.se2.catnip.recommendation;

import de.uni_passau.fim.se2.catnip.pq_gram.*;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

import java.util.ArrayList;
import java.util.List;

public class EditsGenerator {
    private final Program sourceProgram;
    private final List<ProgramWithProfile> possibleTargetPrograms;

    public EditsGenerator(Program sourceProgram, List<Program> possibleTargetPrograms) {
        this.sourceProgram = sourceProgram;
        this.possibleTargetPrograms = new ArrayList<>();
        for (Program possibleTargetProgram : possibleTargetPrograms) {
            this.possibleTargetPrograms.add(new ProgramWithProfile(possibleTargetProgram));
        }
    }

    public List<ActorBlockEdit> getEdits() {
        List<ActorBlockEdit> edits = new ArrayList<>();
        Program target = NearestASTNodePicker.pickNearestProgram(sourceProgram, possibleTargetPrograms);
        List<ActorDefinition> sourceActorDefinitions =
                new ArrayList<>(sourceProgram.getActorDefinitionList().getDefinitions());
        List<ActorDefinition> targetActorDefinitions = new ArrayList<>(target.getActorDefinitionList().getDefinitions());

        List<ActorWithProfile> sourceActorDefinitionsWithProfile = new ArrayList<>();
        for (ActorDefinition actorDefinition : sourceActorDefinitions) {
            sourceActorDefinitionsWithProfile.add(new ActorWithProfile(actorDefinition));
        }
        List<ActorWithProfile> targetActorDefinitionsWithProfile = new ArrayList<>();
        for (ActorDefinition actorDefinition : targetActorDefinitions) {
            targetActorDefinitionsWithProfile.add(new ActorWithProfile(actorDefinition));
        }

        for (ActorWithProfile currentSourceActor : sourceActorDefinitionsWithProfile) {
            //introduce edit that addresses deletion of actors
            if (targetActorDefinitionsWithProfile.size() > 0) {
                ActorWithProfile currentTargetActor = NearestASTNodePicker.pickNearestActor(currentSourceActor,
                        targetActorDefinitionsWithProfile);

                List<Script> sourceScripts = new ArrayList<>((
                        (ActorDefinition) currentSourceActor.getASTNode()).getScripts().getScriptList());
                List<Script> targetScripts =
                        new ArrayList<>(((ActorDefinition) currentTargetActor.getASTNode()).getScripts().getScriptList());

                List<ScriptWithProfile> sourceScriptsWithProfile = new ArrayList<>();
                for (Script script : sourceScripts) {
                    sourceScriptsWithProfile.add(new ScriptWithProfile(script));
                }

                List<ScriptWithProfile> targetScriptsWithProfile = new ArrayList<>();
                for (Script script : targetScripts) {
                    targetScriptsWithProfile.add(new ScriptWithProfile(script));
                }

                if (targetScriptsWithProfile.size() < sourceScriptsWithProfile.size()) {
                    List<ScriptWithProfile> sourceScriptsNew = new ArrayList<>(sourceScriptsWithProfile);
                    for (ScriptWithProfile script : targetScriptsWithProfile) {
                        ScriptWithProfile sourceScript = NearestASTNodePicker.pickNearestScript(script,
                                sourceScriptsNew);
                        sourceScriptsNew.remove(sourceScript);
                    }
                    assert sourceScriptsNew.size() > 0;

                    for (ScriptWithProfile script : sourceScriptsNew) {
                        EditSet edit = new EditSet();
                        if (((Script) script.getASTNode()).getEvent() instanceof Never) {
                            edit.addDeletion(new Edit(new Label("Script", null), new Label(
                                    ((Script) script.getASTNode()).getStmtList().getStmts().get(0).getClass().getSimpleName() + 0,
                                    ((Script) script.getASTNode()).getStmtList().getStmts().get(0))));
                        } else {
                            edit.addDeletion(new Edit(new Label("Script", null), new Label(
                                    ((Script) script.getASTNode()).getEvent().getClass().getSimpleName(),
                                    ((Script) script.getASTNode()).getEvent())));
                        }
                        edits.add(new ActorScriptEdit((ActorDefinition) currentSourceActor.getASTNode(),
                                (Script) script.getASTNode(), edit));
                    }
                    sourceScriptsWithProfile.removeAll(sourceScriptsNew);
                }

                for (ScriptWithProfile sourceScript : sourceScriptsWithProfile) {

                    ScriptWithProfile targetScript = NearestASTNodePicker.pickNearestScript(sourceScript,
                            targetScriptsWithProfile);
                    EditSet edit = PQGramUtil.identifyEdits(sourceScript.getProfile(),
                            targetScript.getProfile());
                    if (edit.getAdditions().size() > 0 || edit.getDeletions().size() > 0) {
                        edits.add(new ActorScriptEdit((ActorDefinition) currentSourceActor.getASTNode(),
                                (Script) sourceScript.getASTNode(), edit));
                    }
                    targetScriptsWithProfile.remove(targetScript);
                }

                if (targetScriptsWithProfile.size() > 0) {
                    for (ScriptWithProfile targetScript : targetScriptsWithProfile) {
                        EditSet edit = new EditSet();
                        if (((Script) targetScript.getASTNode()).getEvent() instanceof Never) {
                            edit.addAddition(new Edit(new Label("Script", null), new Label(
                                    ((Script) targetScript.getASTNode()).getStmtList().getStmts().get(0).getClass().getSimpleName() + 0,
                                    ((Script) targetScript.getASTNode()).getStmtList().getStmts().get(0))));
                        } else {
                            edit.addAddition(new Edit(new Label("Script", null), new Label(
                                    ((Script) targetScript.getASTNode()).getEvent().getClass().getSimpleName(),
                                    ((Script) targetScript.getASTNode()).getEvent())));
                        }
                        edits.add(new ActorScriptEdit((ActorDefinition) currentSourceActor.getASTNode(), new EmptyScript(), edit));
                    }
                }

                List<ProcedureDefinition> sourceProcedures =
                        new ArrayList<>(((ActorDefinition)
                                currentSourceActor.getASTNode()).getProcedureDefinitionList().getList());
                List<ProcedureDefinition> targetProcedures =
                        new ArrayList<>(((ActorDefinition)
                                currentTargetActor.getASTNode()).getProcedureDefinitionList().getList());

                List<ProcedureWithProfile> sourceProceduresWithProfile =
                        new ArrayList<>();
                for (ProcedureDefinition procedure : sourceProcedures) {
                    sourceProceduresWithProfile.add(new ProcedureWithProfile(procedure));
                }
                List<ProcedureWithProfile> targetProceduresWithProfile =
                        new ArrayList<>();
                for (ProcedureDefinition procedure : targetProcedures) {
                    targetProceduresWithProfile.add(new ProcedureWithProfile(procedure));
                }

                for (ProcedureWithProfile sourceProcedure : sourceProceduresWithProfile) {
                    ProcedureWithProfile targetProcedure =
                            NearestASTNodePicker.pickNearestProcedureDefinition(sourceProcedure,
                                    targetProceduresWithProfile);
                    EditSet edit = PQGramUtil.identifyEdits(sourceProcedure.getProfile(),
                            targetProcedure.getProfile());
                    if (edit.getAdditions().size() > 0 || edit.getDeletions().size() > 0) {
                        edits.add(new ActorProcedureEdit((ActorDefinition) currentSourceActor.getASTNode(),
                                (ProcedureDefinition) sourceProcedure.getASTNode(), edit));
                    }
                    targetProceduresWithProfile.remove(targetProcedure);
                }

                targetActorDefinitionsWithProfile.remove(currentTargetActor);
            }
        }
        return edits;
    }
}
