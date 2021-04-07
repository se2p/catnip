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

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;

public class ActorScriptEdit extends ActorBlockEdit {
    private final Script script;

    public ActorScriptEdit(ActorDefinition actor, Script script, EditSet edit) {
        super(actor, edit);
        this.script = script;
    }

    public Script getScript() {
        return script;
    }

    @Override
    public String toString() {
        return "ActorScriptEdit{"
                + "actor=" + getActor().getIdent().getName()
                + ", script=" + script.getStmtList().getStmts().toString()
                + ", edit=" + getEdit() + '}';
    }
}
