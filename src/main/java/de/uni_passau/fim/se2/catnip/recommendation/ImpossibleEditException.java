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

public class ImpossibleEditException extends Exception {

    public ImpossibleEditException() {
    }

    public ImpossibleEditException(String message) {
        super(message);
    }

    public ImpossibleEditException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImpossibleEditException(Throwable cause) {
        super(cause);
    }

    public ImpossibleEditException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

