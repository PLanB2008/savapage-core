/*
 * This file is part of the SavaPage project <httsp://www.savapage.org>.
 * Copyright (c) 2011-2019 Datraverse B.V.
 * Author: Rijk Ravestein.
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
package org.savapage.core.print.gcp;

/**
 * @deprecated See Mantis #1094.
 *
 * @author Rijk Ravestein
 *
 */
@Deprecated
public class GcpAuthException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     *
     * @param ex
     *            The exception.
     */
    public GcpAuthException(final Exception ex) {
        super(ex);
    }

    /**
     *
     * @param message
     */
    public GcpAuthException(final String message) {
        super(message);
    }

    /**
     *
     * @param message
     * @param cause
     */
    public GcpAuthException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
