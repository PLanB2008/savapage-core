/*
 * This file is part of the SavaPage project <https://www.savapage.org>.
 * Copyright (c) 2020 Datraverse B.V.
 * Author: Rijk Ravestein.
 *
 * SPDX-FileCopyrightText: © 2020 Datraverse B.V. <info@datraverse.com>
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please contact Datraverse B.V. at this
 * address: info@datraverse.com
 */
package org.savapage.ext.papercut;

/**
 * When PaperCut server cannot be reached.
 *
 * @author Datraverse B.V.
 */
public class PaperCutConnectException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@link PaperCutConnectException}.
     *
     * @param cause
     *            The cause.
     */
    public PaperCutConnectException(final Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new {@link PaperCutConnectException}.
     *
     * @param message
     *            The detail message.
     */
    public PaperCutConnectException(final String message) {
        super(message);
    }

    /**
     * Constructs a new {@link PaperCutConnectException}.
     *
     * @param message
     *            The detail message.
     * @param cause
     *            The cause.
     */
    public PaperCutConnectException(final String message,
            final Throwable cause) {
        super(message, cause);
    }

}
