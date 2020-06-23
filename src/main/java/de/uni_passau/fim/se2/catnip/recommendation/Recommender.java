package de.uni_passau.fim.se2.catnip.recommendation;


import de.uni_passau.fim.se2.catnip.pqGram.Edits;
import de.uni_passau.fim.se2.catnip.pqGram.NearestASTNodePicker;
import de.uni_passau.fim.se2.catnip.pqGram.PQGramProfileCreator;
import de.uni_passau.fim.se2.catnip.pqGram.PQGramUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
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

    public List<ActorScriptEdit> getEdits() {
        List<ActorScriptEdit> edits = new ArrayList<>();
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
                    edit.addAddition(new Pair<>("Script", targetScript.getEvent().getClass().getSimpleName()));
                }
                edits.add(new ActorScriptEdit(currentSourceActor, null, edit));
            }
            //todo if source has more scripts than target
            targetActorDefinitions.remove(currentTargetActor.getActorDefinition());
        }
        return edits;
    }
}
