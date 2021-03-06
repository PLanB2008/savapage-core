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
package org.savapage.core.fonts;

import java.util.Locale;

import org.savapage.core.util.LocaleHelper;

import net.sf.jasperreports.engine.design.JRDesignStyle;
import net.sf.jasperreports.engine.design.JRDesignTextField;

/**
 * Internal Fonts packaged with SavaPage.
 *
 * @author Rijk Ravestein
 *
 */
public enum InternalFontFamilyEnum {

    /**
     * The default font.
     */
    DEFAULT("DejaVu Sans", "dejavu/DejaVuSans.ttf"),

    /**
     * Chinese, Japanese and Korean.
     */
    CJK("Droid Sans Fallback", "droid/DroidSansFallbackFull.ttf"),

    /**
     * GNU Unifont.
     */
    UNIFONT("Unifont", "misc/unifont.ttf");

    /**
     * The font name.
     */
    private final String fontName;

    /**
     * File name relative to the font directory.
     */
    private final String relativePath;

    /**
     *
     * @param name
     *            The font name.
     * @param fileName
     *            The file name relative to the font directory.
     */
    private InternalFontFamilyEnum(final String name, final String fileName) {
        this.fontName = name;
        this.relativePath = fileName;
    }

    /**
     * Gets the Font Family name as defined in {@code fonts.xml} and to be used
     * as font name in the JasperReport API.
     * <p>
     * See e.g. {@link JRDesignStyle#setFontName(String)} or {
     * {@link JRDesignTextField#setFontName(String)}.
     * </p>
     *
     * @return The JasperReport font name.
     */
    public String getJrName() {
        return fontName;
    }

    /**
     * Gets the user interface font name.
     *
     * @param locale
     *            The {@link Locale}.
     * @return The localized text.
     */
    public String uiText(final Locale locale) {
        return LocaleHelper.uiText(this, locale);
    }

    /**
     *
     * @return The path of the font file.
     */
    public String fontFilePath() {
        return FontLocation.getClassPath() + this.relativePath;
    }

    /**
     *
     * @return The relative path of the font file.
     */
    public String getRelativePath() {
        return this.relativePath;
    }

}
