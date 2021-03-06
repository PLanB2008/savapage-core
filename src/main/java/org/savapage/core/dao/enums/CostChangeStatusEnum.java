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
package org.savapage.core.dao.enums;

import java.util.Locale;

import org.savapage.core.i18n.AdjectiveEnum;
import org.savapage.core.jpa.CostChange;

/**
 * Status of Cost Change.
 *
 * <p>
 * <b>Important</b>: the length of {@link CostChangeStatusEnum#toString()} MUST
 * NOT exceed the column width of {@link CostChange#getChgStatus()}.
 *
 * @author Rijk Ravestein
 *
 */
public enum CostChangeStatusEnum {

    /**
     *
     */
    PENDING, APPROVED, REJECTED;

    /**
     * @param locale
     *            The {@link Locale}.
     * @return The localized text.
     */
    public String uiText(final Locale locale) {
        switch (this) {
        case PENDING:
            return AdjectiveEnum.PENDING.uiText(locale);
        case APPROVED:
            return AdjectiveEnum.APPROVED.uiText(locale);
        case REJECTED:
            return AdjectiveEnum.REJECTED.uiText(locale);
        default:
            throw new UnsupportedOperationException();
        }
    }

}
