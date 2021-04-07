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

import java.util.LinkedHashSet;
import java.util.Set;

public class EditSet {
    private final Set<Edit> additions;
    private final Set<Edit> deletions;

    public EditSet() {
        additions = new LinkedHashSet<>();
        deletions = new LinkedHashSet<>();
    }

    public EditSet(Set<Edit> additions, Set<Edit> deletions) {
        this.additions = new LinkedHashSet<>(additions);
        this.deletions = new LinkedHashSet<>(deletions);
    }

    public void addAddition(Edit edit) {
        additions.add(edit);
    }

    public void addDeletion(Edit deletion) {
        deletions.add(deletion);
    }

    public Set<Edit> getAdditions() {
        return additions;
    }

    public Set<Edit> getDeletions() {
        return deletions;
    }

    @Override
    public String toString() {
        return "Edits{"
                + "additions=" + additions
                + ", deletions=" + deletions + '}';
    }
}
