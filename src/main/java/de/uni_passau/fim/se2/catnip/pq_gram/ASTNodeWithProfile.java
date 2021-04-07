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

public class ASTNodeWithProfile {
    private final ASTNode astNode;
    private final PQGramProfile profile;

    public ASTNodeWithProfile(ASTNode astNode) {
        this.astNode = astNode;
        profile = PQGramProfileCreator.createPQProfile(astNode);
    }

    public ASTNodeWithProfile(ASTNode astNode, PQGramProfile profile) {
        this.astNode = astNode;
        this.profile = profile;
    }

    public ASTNode getASTNode() {
        return astNode;
    }

    public PQGramProfile getProfile() {
        return profile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ASTNodeWithProfile that = (ASTNodeWithProfile) o;
        return astNode.equals(that.astNode)
                && getProfile().equals(that.getProfile());
    }

    @Override
    public int hashCode() {
        return Objects.hash(astNode, getProfile());
    }
}
