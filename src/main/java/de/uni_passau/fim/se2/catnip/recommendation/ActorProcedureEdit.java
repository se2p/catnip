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
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

public class ActorProcedureEdit extends ActorBlockEdit {
    private final ProcedureDefinition procedure;

    public ActorProcedureEdit(ActorDefinition actor, ProcedureDefinition procedure, EditSet edit) {
        super(actor, edit);
        this.procedure = procedure;
    }

    public ProcedureDefinition getProcedure() {
        return procedure;
    }

    @Override
    public String toString() {
        return "ActorProcedureEdit{" + "actor=" + getActor().getIdent().getName()
                + "procedure=" + procedure.getStmtList().getStmts().toString()
                + ", edit=" + getEdit() + '}';
    }
}
