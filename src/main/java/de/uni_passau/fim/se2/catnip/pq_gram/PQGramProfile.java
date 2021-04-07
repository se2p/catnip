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

import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.HashBag;

public class PQGramProfile {
    private Bag<LabelTuple> tuples;

    public PQGramProfile() {
        tuples = new HashBag<>();
    }

    public void addLabelTuple(LabelTuple tuple) {
        tuples.add(tuple);
    }

    public Bag<LabelTuple> getTuples() {
        return tuples;
    }

    @Override
    public String toString() {
        return "PQGramProfile{"
                + "tuples=" + tuples + '}';
    }
}
