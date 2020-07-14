package de.uni_passau.fim.se2.catnip.recommendation;

import de.uni_passau.fim.se2.catnip.pqGram.*;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

import java.util.ArrayList;
import java.util.List;

public class Recommender {
    private Program sourceProgram;
    private List<ProgramWithProfile> possibleTargetPrograms;

    public Recommender(Program sourceProgram, List<Program> possibleTargetPrograms) {
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
                new ArrayList<>(sourceProgram.getActorDefinitionList().getDefintions());
        List<ActorDefinition> targetActorDefinitions = new ArrayList<>(target.getActorDefinitionList().getDefintions());

        for (ActorDefinition currentSourceActor : sourceActorDefinitions) {
            ActorWithProfile currentTargetActor = NearestASTNodePicker.pickNearestActor(currentSourceActor,
                    targetActorDefinitions);

            List<Script> sourceScripts = new ArrayList<>(currentSourceActor.getScripts().getScriptList());
            List<Script> targetScripts =
                    new ArrayList<>(currentTargetActor.getActorDefinition().getScripts().getScriptList());

            List<ScriptWithProfile> sourceScriptsWithProfile = new ArrayList<>();
            for (Script script : sourceScripts) {
                sourceScriptsWithProfile.add(new ScriptWithProfile(script));
            }

            List<ScriptWithProfile> targetScriptsWithProfile = new ArrayList<>();
            for (Script script : targetScripts) {
                targetScriptsWithProfile.add(new ScriptWithProfile(script));
            }
            //todo if source has more scripts than target
            /*
            if (targetScripts.size() < sourceScripts.size()) {
                List<Script> sourceScripts =
                for ()
            }

             */
            for (ScriptWithProfile sourceScript : sourceScriptsWithProfile) {

                ScriptWithProfile targetScript = NearestASTNodePicker.pickNearestScript(sourceScript,
                        targetScriptsWithProfile);
                EditSet edit = PQGramUtil.identifyEdits(sourceScript.getProfile(),
                        targetScript.getProfile());
                if (edit.getAdditions().size() > 0 || edit.getDeletions().size() > 0) {
                    edits.add(new ActorScriptEdit(currentSourceActor, (Script) sourceScript.getASTNode(), edit));
                }
                targetScriptsWithProfile.remove(targetScript);
            }

            if (sourceScriptsWithProfile.size() < targetScriptsWithProfile.size()) {
                EditSet edit = new EditSet();
                for (ScriptWithProfile targetScript : targetScriptsWithProfile) {
                    edit.addAddition(new Edit(new Label("Script", null), new Label(
                            ((Script) targetScript.getASTNode()).getEvent().getClass().getSimpleName(), ((Script) targetScript.getASTNode()).getEvent())));
                }
                edits.add(new ActorBlockEdit(currentSourceActor, edit));
            }

            List<ProcedureDefinition> sourceProcedures =
                    new ArrayList<>(currentSourceActor.getProcedureDefinitionList().getList());
            List<ProcedureDefinition> targetProcedures =
                    new ArrayList<>(currentTargetActor.getActorDefinition().getProcedureDefinitionList().getList());
            for (ProcedureDefinition sourceProcedure : sourceProcedures) {
                ProcedureWithProfile targetProcedure =
                        NearestASTNodePicker.pickNearestProcedureDefinition(sourceProcedure,
                                targetProcedures);
                EditSet edit = PQGramUtil.identifyEdits(PQGramProfileCreator.createPQProfile(sourceProcedure),
                        targetProcedure.getProfile());
                if (edit.getAdditions().size() > 0 || edit.getDeletions().size() > 0) {
                    edits.add(new ActorProcedureEdit(currentSourceActor, sourceProcedure, edit));
                }
                targetProcedures.remove(targetProcedure.getProcedureDefinition());
            }

            targetActorDefinitions.remove(currentTargetActor.getActorDefinition());
        }
        return edits;
    }
}
