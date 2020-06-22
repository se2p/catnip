package de.uni_passau.fim.se2.catnip.recommendation;


import de.uni_passau.fim.se2.catnip.pqGram.Edits;
import de.uni_passau.fim.se2.catnip.pqGram.NearestASTNodePicker;
import de.uni_passau.fim.se2.catnip.pqGram.PQGramProfileCreator;
import de.uni_passau.fim.se2.catnip.pqGram.PQGramUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;

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
        List<ActorDefinition> sourceActorDefinitions = sourceProgram.getActorDefinitionList().getDefintions();
        for (ActorDefinition currentSourceActor : sourceActorDefinitions) {
            ActorWithProfile currentTargetActor = NearestASTNodePicker.pickNearestActor(currentSourceActor,
                    target.getActorDefinitionList().getDefintions());
            System.out.println(currentTargetActor.getActorDefinition().getIdent().getName());
            List<Script> sourceScripts = currentSourceActor.getScripts().getScriptList();
            List<Script> targetScripts = currentTargetActor.getActorDefinition().getScripts().getScriptList();
            if (sourceScripts.size() == 0 && targetScripts.size() > 0) {
                //todo add edits for new script
            }
            for (Script sourceScript : sourceScripts) {

                ScriptWithProfile targetScript = NearestASTNodePicker.pickNearestScript(sourceScript,
                        targetScripts);
                Edits edit = PQGramUtil.identifyEdits(PQGramProfileCreator.createPQProfile(sourceScript),
                        targetScript.getProfile());
                if (edit.getAdditions().size() > 0 || edit.getDeletions().size() > 0) {
                    edits.add(new ActorScriptEdit(currentSourceActor, sourceScript, edit));
                }
            }
        }
        return edits;
    }
}
