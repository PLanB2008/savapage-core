/*
 * This file is part of the SavaPage project <https://www.savapage.org>.
 * Copyright (c) 2011-2020 Datraverse B.V.
 * Author: Rijk Ravestein.
 *
 * SPDX-FileCopyrightText: 2011-2020 Datraverse B.V. <info@datraverse.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * For more information, please contact Datraverse B.V. at this
 * address: info@datraverse.com
 */
package org.savapage.core.circuitbreaker;

/**
 * A {@link Exception} used by {@link CircuitBreaker} to communicate exceptions
 * due to {@link CircuitStateEnum} state during
 * {@link CircuitBreaker#execute(CircuitBreakerOperation)}.
 *
 * @author Rijk Ravestein
 *
 */
public class CircuitBreakerException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@link CircuitBreakerException} with the specified
     * detail message and cause.
     *
     * @param message
     *            The detail message.
     * @param cause
     *            The cause.
     */
    public CircuitBreakerException(final String message,
            final Exception cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@link CircuitBreakerException} with the specified
     * detail message.
     *
     * @param message
     *            The detail message.
     */
    public CircuitBreakerException(final String message) {
        super(message);
    }

}
// end-of-file
