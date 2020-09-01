package de.uni_passau.fim.se2.catnip.recommendation;

import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;

import java.util.ArrayList;

public class EmptyScript extends Script {

    public EmptyScript() {
        super(new Never(), new StmtList(new ArrayList<>()));
    }
}
