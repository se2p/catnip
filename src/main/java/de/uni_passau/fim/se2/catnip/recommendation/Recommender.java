package de.uni_passau.fim.se2.catnip.recommendation;


import de.uni_passau.fim.se2.catnip.pqGram.*;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import org.javatuples.Pair;

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
            for (Script sourceScript : sourceScripts) {

                ScriptWithProfile targetScript = NearestASTNodePicker.pickNearestScript(sourceScript,
                        targetScripts);
                Edits edit = PQGramUtil.identifyEdits(PQGramProfileCreator.createPQProfile(sourceScript),
                        targetScript.getProfile());
                if (edit.getAdditions().size() > 0 || edit.getDeletions().size() > 0) {
                    edits.add(new ActorScriptEdit(currentSourceActor, sourceScript, edit));
                }
                targetScripts.remove(targetScript.getScript());
            }

            if (sourceScripts.size() < targetScripts.size()) {
                Edits edit = new Edits();
                for (Script targetScript : targetScripts) {
                    edit.addAddition(new Pair<>(new Label("Script", null), new Label(
                            targetScript.getEvent().getClass().getSimpleName(), targetScript.getEvent())));
                }
                edits.add(new ActorBlockEdit(currentSourceActor, edit));
            }
            //todo if source has more scripts than target

            //todo procedures
            List<ProcedureDefinition> sourceProcedures =
                    new ArrayList<>(currentSourceActor.getProcedureDefinitionList().getList());
            List<ProcedureDefinition> targetProcedures =
                    new ArrayList<>(currentTargetActor.getActorDefinition().getProcedureDefinitionList().getList());
            for (ProcedureDefinition sourceProcedure : sourceProcedures) {
                ProcedureWithProfile targetProcedure =
                        NearestASTNodePicker.pickNearestProcedureDefinition(sourceProcedure,
                        targetProcedures);
                Edits edit = PQGramUtil.identifyEdits(PQGramProfileCreator.createPQProfile(sourceProcedure),
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
