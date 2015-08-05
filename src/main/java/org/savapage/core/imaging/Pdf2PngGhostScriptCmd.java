/*
 * This file is part of the SavaPage project <http://savapage.org>.
 * Copyright (c) 2011-2015 Datraverse B.V.
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please contact Datraverse B.V. at this
 * address: info@datraverse.com
 */
package org.savapage.core.imaging;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

/**
 * Command using GhostScript.
 *
 * @author Datraverse B.V.
 *
 */
public final class Pdf2PngGhostScriptCmd implements Pdf2ImgCommand {

    /**
     *
     */
    private static final int STRINGBUILDER_CAPACITY = 256;

    @Override
    public String createCommand(final File pdfFile, final File imgFile,
            final int pageOrdinal, final String rotate2Apply,
            final int resolution) {

        final int pageOneBased = pageOrdinal + 1;

        final StringBuilder cmdBuffer =
                new StringBuilder(STRINGBUILDER_CAPACITY);

        cmdBuffer.append("gs -dNumRenderingThreads=4 -sDEVICE=pngalpha")
                .append(" -dNOPAUSE -dFirstPage=").append(pageOneBased)
                .append(" -dLastPage=").append(pageOneBased)
                .append(" -sOutputFile=- -r").append(resolution)
                .append(" -q \"").append(pdfFile.getAbsolutePath())
                .append("\" -c quit");

        /*
         * Apply rotate?
         */
        if (StringUtils.defaultString(rotate2Apply, "0").equals("0")) {
            cmdBuffer.append(" > ");
        } else {
            cmdBuffer.append(" | convert -rotate ").append(rotate2Apply)
                    .append(" - ");
        }

        cmdBuffer.append("\"").append(imgFile.getAbsolutePath()).append("\"");

        final String command = cmdBuffer.toString();

        return command;
    }
}
