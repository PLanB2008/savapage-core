/*
 * This file is part of the SavaPage project <http://savapage.org>.
 * Copyright (c) 2011-2014 Datraverse B.V.
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
package org.savapage.core.dao.helpers;

/**
 * The trigger (reason why) for a credit assigment or change.
 *
 * @author Datraverse B.V.
 *
 */
public enum AccountTrxTypeEnum {

    /**
     * "Initial allocation".
     */
    INITIAL,

    /**
     * "Manual adjustment".
     */
    ADJUST,

    /**
     * "Manual transaction" (deposit funds at point-of-sales).
     */
    DEPOSIT,

    /**
     * "Voucher use".
     */
    VOUCHER,

    /**
     *
     */
    PRINT_IN,

    /**
     * A proxy print.
     */
    PRINT_OUT,

    /**
     * PDF download.
     */
    PDF_OUT
}
