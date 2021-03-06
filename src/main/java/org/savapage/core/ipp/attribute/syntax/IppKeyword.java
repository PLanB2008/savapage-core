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

import org.savapage.core.ipp.attribute.IppDictJobTemplateAttr;
import org.savapage.core.ipp.encoding.IppValueTag;

/**
 *
 * @author Rijk Ravestein
 *
 */
public final class IppKeyword extends AbstractIppAttrSyntax {

    /**
     * All pages.
     */
    public static final String CUPS_ATTR_PAGE_SET_ALL = "all";
    /**
     * Even pages.
     */
    public static final String CUPS_ATTR_PAGE_SET_EVEN = "even";
    /**
     * Odd pages.
     */
    public static final String CUPS_ATTR_PAGE_SET_ODD = "odd";

    /**
     * Do not produce a banner page.
     */
    public static final String ATTR_JOB_SHEETS_NONE = "none";
    /**
     * A banner page with a "classified" label at the top and bottom.
     */
    public static final String ATTR_JOB_SHEETS_CLASSIFIED = "classified";
    /**
     * A banner page with a "confidential" label at the top and bottom.
     */
    public static final String ATTR_JOB_SHEETS_CONFIDENTIAL = "confidential";
    /**
     * A banner page with a "secret" label at the top and bottom.
     */
    public static final String ATTR_JOB_SHEETS_SECRET = "secret";
    /**
     * A banner page with no label at the top and bottom.
     */
    public static final String ATTR_JOB_SHEETS_STANDARD = "standard";
    /**
     * A banner page with a "top secret" label at the top and bottom.
     */
    public static final String ATTR_JOB_SHEETS_TOPSECRET = "topsecret";
    /**
     * A banner page with an "unclassified" label at the top and bottom.
     */
    public static final String ATTR_JOB_SHEETS_UNCLASSIFIED = "unclassified";

    // ------------------------------------------------------------------------
    // compression: type3 keyword
    // ------------------------------------------------------------------------

    /**
     * No compression is used.
     */
    public static final String COMPRESSION_NONE = "none";

    /**
     * ZIP public domain inflate/deflate) compression technology in RFC1951.
     */
    public static final String COMPRESSION_DEFLATE = "deflate";

    /**
     * GNU zip compression technology described in RFC 1952.
     */
    public static final String COMPRESSION_GZIP = "gzip";

    /**
     * UNIX compression technology in RFC1977.
     */
    public static final String COMPRESSION_COMPRESS = "compress";

    // ------------------------------------------------------------------------
    // print-color-mode
    // ------------------------------------------------------------------------
    /**
     * Automatic based on document REQUIRED.
     */
    public static final String PRINT_COLOR_MODE_AUTO = "auto";

    /**
     * 1-colorant (typically black) threshold output OPTIONAL (note 1).
     */
    public static final String PRINT_COLOR_MODE_BI_LEVEL = "bi-level";

    /**
     * Full-color output CONDITIONALLY REQUIRED (note 2).
     */
    public static final String PRINT_COLOR_MODE_COLOR = "color";

    /**
     * 1-colorant + black output OPTIONAL.
     */
    public static final String PRINT_COLOR_MODE_HIGHLIGHT = "highlight";

    /**
     * 1-colorant (typically black) shaded/grayscale output REQUIRED.
     */
    public static final String PRINT_COLOR_MODE_MONOCHROME = "monochrome";

    /**
     * Process (2 or more colorants) threshold output OPTIONAL.
     */
    public static final String PRINT_COLOR_MODE_PROCESS_BI_LEVEL =
            "process-bi-level";

    /**
     * Process (2 or more colorants) shaded/grayscale output.
     */
    public static final String PRINT_COLOR_MODE_PROCESS_MONOCHROME =
            "process-monochrome";

    // Enum value.
    public static final String PRINT_QUALITY_HIGH = "5";

    //
    public static final String JOB_PASSWORD_ENCRYPTION_NONE = "none";
    public static final String JOB_PASSWORD_ENCRYPTION_MD5 = "md5";
    // and lots more encryption possible.

    //
    public static final String MULTIPLE_DOCUMENT_HANDLING_SINGLE =
            "single-document";

    // ------------------------------------------------------------------------
    // sheet-collate (RFC 3381)
    // ------------------------------------------------------------------------
    public static final String SHEET_COLLATE_UNCOLLATED = "uncollated";
    public static final String SHEET_COLLATE_COLLATED = "collated";

    // ------------------------------------------------------------------------
    // print-scaling (PWG5100.16)
    //
    // NOTE: values 'auto', 'auto-fit' and 'fill' are currently not supported
    // by SavaPage.
    // ------------------------------------------------------------------------

    /**
     * Scale the document to fit the printable area of the requested media size,
     * preserving the aspect ratio of the document data without cropping the
     * document.
     */
    public static final String PRINT_SCALING_FIT = "fit";

    /**
     * Do not scale the document to fit the requested media size. If the
     * document is larger than the requested media, center and clip the
     * resulting output. If the document is smaller than the requested media,
     * center the resulting output.
     */
    public static final String PRINT_SCALING_NONE = "none";

    /**
     * If the “ipp-attribute-fidelity” attribute is true or the document is
     * larger than the requested media, scale the document using the 'fit'
     * method if the margins are nonzero, otherwise scale using the 'fill'
     * method. If the “ipp-attribute-fidelity” attribute is false or unspecified
     * and the document is smaller than the requested media, scale using the
     * 'none' method.
     */
    public static final String PRINT_SCALING_AUTO = "auto";

    /**
     * If the “ipp-attribute-fidelity” attribute is true or the document is
     * larger than the requested media, scale the document using the ‘fit’
     * method. Otherwise, scale using the ‘none’ method.
     */
    public static final String PRINT_SCALING_AUTO_FIT = "auto-fit";

    /**
     * Scale the document to fill the requested media size, preserving the
     * aspect ratio of the document data but potentially cropping portions of
     * the document.
     */
    public static final String PRINT_SCALING_FILL = "fill";

    // ------------------------------------------------------------------------
    // output-bin
    // ------------------------------------------------------------------------
    public static final String OUTPUT_BIN_AUTO = "auto";

    // ------------------------------------------------------------------------
    // media-source
    // ------------------------------------------------------------------------
    public static final String MEDIA_SOURCE_AUTO = "auto";
    public static final String MEDIA_SOURCE_MANUAL = "manual";

    // ------------------------------------------------------------------------
    // sides
    // ------------------------------------------------------------------------
    public static final String SIDES_ONE_SIDED = "one-sided";
    public static final String SIDES_TWO_SIDED_LONG_EDGE =
            "two-sided-long-edge";
    public static final String SIDES_TWO_SIDED_SHORT_EDGE =
            "two-sided-short-edge";

    // ------------------------------------------------------------------------
    // media-type
    // ------------------------------------------------------------------------
    public static final String MEDIA_TYPE_PAPER = "paper";
    public static final String MEDIA_TYPE_TRANSPARENCY = "transparency";
    public static final String MEDIA_TYPE_LABELS = "labels";

    // ------------------------------------------------------------------------
    // number-up: https://www.cups.org/doc/options.html
    // ------------------------------------------------------------------------
    public static final String NUMBER_UP_1 = "1";
    public static final String NUMBER_UP_2 = "2";
    public static final String NUMBER_UP_4 = "4";
    public static final String NUMBER_UP_6 = "6";
    public static final String NUMBER_UP_9 = "9";
    public static final String NUMBER_UP_16 = "16";

    public static final String[] ARRAY_NUMBER_UP = { NUMBER_UP_1, NUMBER_UP_2,
            NUMBER_UP_4, NUMBER_UP_6, NUMBER_UP_9, NUMBER_UP_16 };

    // ------------------------------------------------------------------------
    // number-up-layout : https://www.cups.org/doc/options.html
    // ------------------------------------------------------------------------

    /**
     * number-up-layout: Bottom to top, left to right.
     */
    public static final String NUMBER_UP_LAYOUT_BTLR = "btlr";

    /**
     * number-up-layout: Bottom to top, right to left.
     */
    public static final String NUMBER_UP_LAYOUT_BTRL = "btrl";

    /**
     * number-up-layout: Left to right, bottom to top.
     */
    public static final String NUMBER_UP_LAYOUT_LRBT = "lrbt";

    /**
     * number-up-layout: Left to right, top to bottom (default).
     */
    public static final String NUMBER_UP_LAYOUT_LRTB = "lrtb";

    /**
     * number-up-layout: Right to left, bottom to top.
     */
    public static final String NUMBER_UP_LAYOUT_RLBT = "rlbt";

    /**
     * number-up-layout: Right to left, top to bottom.
     */
    public static final String NUMBER_UP_LAYOUT_RLTB = "rltb";

    /**
     * number-up-layout: Top to bottom, left to right.
     */
    public static final String NUMBER_UP_LAYOUT_TBLR = "tblr";

    /**
     * number-up-layout: Top to bottom, right to left.
     */
    public static final String NUMBER_UP_LAYOUT_TBRL = "tbrl";

    public static final String[] ARRAY_NUMBER_UP_LAYOUT = {
            NUMBER_UP_LAYOUT_BTLR, NUMBER_UP_LAYOUT_BTRL, NUMBER_UP_LAYOUT_LRBT,
            NUMBER_UP_LAYOUT_LRTB, NUMBER_UP_LAYOUT_RLBT, NUMBER_UP_LAYOUT_RLTB,
            NUMBER_UP_LAYOUT_TBLR, NUMBER_UP_LAYOUT_TBRL };

    // ------------------------------------------------------------------------
    // orientation-requested : https://www.cups.org/doc/options.html
    // ------------------------------------------------------------------------
    /**
     * portrait orientation (no rotation). See
     * {@link IppDictJobTemplateAttr#CUPS_ATTR_ORIENTATION_REQUESTED}.
     */
    public static final String ORIENTATION_REQUESTED_PORTRAIT = "3";

    /**
     * An alias for {@link #ORIENTATION_REQUESTED_PORTRAIT}.
     */
    public static final String ORIENTATION_REQUESTED_0_DEGREES =
            ORIENTATION_REQUESTED_PORTRAIT;

    /**
     * landscape orientation (90 degrees). See
     * {@link IppDictJobTemplateAttr#CUPS_ATTR_ORIENTATION_REQUESTED}.
     */
    public static final String ORIENTATION_REQUESTED_LANDSCAPE = "4";

    /**
     * An alias for {@link #ORIENTATION_REQUESTED_LANDSCAPE}.
     */
    public static final String ORIENTATION_REQUESTED_90_DEGREES =
            ORIENTATION_REQUESTED_LANDSCAPE;

    /**
     * reverse landscape or seascape orientation (270 degrees). See
     * {@link IppDictJobTemplateAttr#CUPS_ATTR_ORIENTATION_REQUESTED}.
     */
    public static final String ORIENTATION_REQUESTED_REVERSE_LANDSCAPE = "5";

    /**
     * An alias for {@link #ORIENTATION_REQUESTED_REVERSE_LANDSCAPE}.
     */
    public static final String ORIENTATION_REQUESTED_270_DEGREES =
            ORIENTATION_REQUESTED_REVERSE_LANDSCAPE;

    /**
     * reverse portrait or upside-down orientation (180 degrees). See
     * {@link IppDictJobTemplateAttr#CUPS_ATTR_ORIENTATION_REQUESTED}.
     */
    public static final String ORIENTATION_REQUESTED_REVERSE_PORTRAIT = "6";

    /**
     * An alias for {@link #ORIENTATION_REQUESTED_REVERSE_PORTRAIT}.
     */
    public static final String ORIENTATION_REQUESTED_180_DEGREES =
            ORIENTATION_REQUESTED_REVERSE_PORTRAIT;

    public static final String[] ARRAY_ORIENTATION_REQUESTED =
            { ORIENTATION_REQUESTED_LANDSCAPE, ORIENTATION_REQUESTED_PORTRAIT,
                    ORIENTATION_REQUESTED_REVERSE_LANDSCAPE,
                    ORIENTATION_REQUESTED_REVERSE_PORTRAIT };

    // ------------------------------------------------------------------------
    // internal
    // ------------------------------------------------------------------------
    public static final String ORG_SAVAPAGE_ATTR_INT_PAGE_ROTATE180_OFF =
            IppBoolean.FALSE;

    public static final String ORG_SAVAPAGE_ATTR_INT_PAGE_ROTATE180_ON =
            IppBoolean.TRUE;

    // ------------------------------------------------------------------------
    // finishings
    // ------------------------------------------------------------------------
    public static final String ORG_SAVAPAGE_ATTR_FINISHINGS_PUNCH_NONE = "3";
    public static final String ORG_SAVAPAGE_ATTR_FINISHINGS_STAPLE_NONE = "3";
    public static final String ORG_SAVAPAGE_ATTR_FINISHINGS_FOLD_NONE = "3";
    public static final String ORG_SAVAPAGE_ATTR_FINISHINGS_BOOKLET_NONE =
            "none";

    // ------------------------------------------------------------------------
    // finishings-external
    // ------------------------------------------------------------------------
    public static final String ORG_SAVAPAGE_ATTR_FINISHINGS_EXTERNAL_NONE =
            "none";
    public static final String ORG_SAVAPAGE_ATTR_FINISHINGS_EXTERNAL_LAMINATE =
            "laminate";
    public static final String ORG_SAVAPAGE_ATTR_FINISHINGS_EXTERNAL_BIND =
            "bind";
    public static final String ORG_SAVAPAGE_ATTR_FINISHINGS_EXTERNAL_GLUE =
            "glue";
    public static final String ORG_SAVAPAGE_ATTR_FINISHINGS_EXTERNAL_FOLDER =
            "folder";

    // ------------------------------------------------------------------------
    // org.savapage-job-sheets
    // ------------------------------------------------------------------------
    public static final String ORG_SAVAPAGE_ATTR_JOB_SHEETS_NONE = "none";
    public static final String ORG_SAVAPAGE_ATTR_JOB_SHEETS_START =
            "job-start-sheet";
    public static final String ORG_SAVAPAGE_ATTR_JOB_SHEETS_END =
            "job-end-sheet";

    /**
     * The generic 'non' value for all org.savapage.ext-* options.
     */
    public static final String ORG_SAVAPAGE_EXT_ATTR_NONE = "none";

    /**
     *
     */
    public static final String ORG_SAVAPAGE_ATTR_COVER_TYPE_NO_COVER =
            "no-cover";

    /**
     *
     */
    public static final String ORG_SAVAPAGE_ATTR_COVER_TYPE_PRINTFRONT_EXT_PFX =
            "printfront.ext.";

    /**
     *
     */
    public static final String ORG_SAVAPAGE_ATTR_COVER_TYPE_PRINTBOTH_EXT_PFX =
            "printboth.ext.";

    /** */
    private static class SingletonHolder {
        /** */
        public static final IppKeyword INSTANCE = new IppKeyword();
    }

    /**
     * @return The singleton instance.
     */
    public static IppKeyword instance() {
        return SingletonHolder.INSTANCE;
    }

    @Override
    public IppValueTag getValueTag() {
        return IppValueTag.KEYWORD;
    }

    @Override
    public void write(final OutputStream ostr, final String value,
            final Charset charset) throws IOException {
        /*
         * Ignore the offered charset, use US_ASCII instead.
         */
        writeUsAscii(ostr, value);
    }

    /**
     * Checks value of {@link IppDictJobTemplateAttr#ATTR_NUMBER_UP}.
     *
     * @param value
     *            The candidate value.
     * @return {@code true} when valid.
     */
    public static boolean checkNumberUp(final String value) {
        return checkKeywordValue(ARRAY_NUMBER_UP, value);
    }

    /**
     * Checks value of
     * {@link IppDictJobTemplateAttr#CUPS_ATTR_ORIENTATION_REQUESTED}.
     *
     * @param value
     *            The candidate value.
     * @return {@code true} when valid.
     */
    public static boolean checkOrientationRequested(final String value) {
        return checkKeywordValue(ARRAY_ORIENTATION_REQUESTED, value);
    }

    /**
     * Checks value of
     * {@link IppDictJobTemplateAttr#CUPS_ATTR_NUMBER_UP_LAYOUT}.
     *
     * @param value
     *            The candidate value.
     * @return {@code true} when valid.
     */
    public static boolean checkNumberUpLayout(final String value) {
        return checkKeywordValue(ARRAY_NUMBER_UP_LAYOUT, value);
    }

    /**
     * @param validValues
     *            Array of valid values.
     * @param value
     *            The candidate value.
     * @return {@code true} when valid.
     */
    private static boolean checkKeywordValue(final String[] validValues,
            final String value) {
        for (final String valueWlk : validValues) {
            if (valueWlk.equals(value)) {
                return true;
            }
        }
        return false;
    }
}
