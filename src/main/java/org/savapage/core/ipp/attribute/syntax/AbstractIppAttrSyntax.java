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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * For more information, please contact Datraverse B.V. at this
 * address: info@datraverse.com
 */
package org.savapage.core.ipp.attribute.syntax;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.savapage.core.ipp.encoding.IppEncoder;
import org.savapage.core.ipp.encoding.IppValueTag;

/**
 *
 * @author Rijk Ravestein
 *
 */
public abstract class AbstractIppAttrSyntax {

    private static final Charset US_ASCII = Charset.forName("US-ASCII");

    /**
     * Gets the {@link IppValueTag} marking the start of the syntax.
     *
     * @return The {@link IppValueTag}.
     */
    public abstract IppValueTag getValueTag();

    /**
     *
     * @param ostr
     * @param value
     * @param charset
     * @throws IOException
     */
    public abstract void write(final OutputStream ostr, final String value,
            final Charset charset) throws IOException;

    /**
     *
     * @param ostr
     * @param bytes
     * @throws IOException
     */
    protected void write(final OutputStream ostr, final byte[] bytes)
            throws IOException {
        IppEncoder.writeInt16(ostr, bytes.length);
        ostr.write(bytes);
    }

    /**
     *
     * @param ostr
     * @param bytes
     * @throws IOException
     */
    protected void writeUsAscii(final OutputStream ostr, final String value)
            throws IOException {
        write(ostr, value.getBytes(US_ASCII));
    }

    /**
     *
     * @param bytes
     * @return
     */
    protected static String readUsAscii(final byte[] bytes) {
        return new String(bytes, US_ASCII);
    }

}
