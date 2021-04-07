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

import java.util.Objects;

public class Label {
    private final String label;
    private final ASTNode node;

    public Label(String label, ASTNode node) {
        this.label = label;
        this.node = node;
    }

    public String getLabel() {
        return label;
    }

    public ASTNode getNode() {
        return node;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Label label1 = (Label) o;
        return getLabel().equals(label1.getLabel());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLabel());
    }

    @Override
    public String toString() {
        return label;
    }
}
