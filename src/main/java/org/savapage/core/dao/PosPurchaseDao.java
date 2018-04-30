/*
 * This file is part of the SavaPage project <https://www.savapage.org>.
 * Copyright (c) 2011-2017 Datraverse B.V.
 * Authors: Rijk Ravestein.
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
package org.savapage.core.dao;

import org.savapage.core.jpa.PosPurchase;

/**
 *
 * @author Rijk Ravestein
 *
 */
public interface PosPurchaseDao extends UserErasableDao<PosPurchase> {

    /**
     *
     * @author Datraverse B.V.
     *
     */
    enum ReceiptNumberPrefixEnum {

        /**
         *
         */
        PURCHASE("P"),
        /**
         *
         */
        DEPOSIT("D"),
        /**
         *
         */
        GATEWAY("G");

        final private String prefix;

        private ReceiptNumberPrefixEnum(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public String toString() {
            return this.prefix;
        }
    }

    /**
     * The minimal width of the number part of a receipt number. This is used to
     * generate a receipt number with a minimum length using pre-pended zeroes.
     */
    int RECEIPT_NUMBER_MIN_WIDTH = 6;

    /**
     * Gets the highest receipt number with prefix.
     *
     * @param prefix
     *            The prefix.
     * @return The receipt number.
     */
    int getHighestReceiptNumber(ReceiptNumberPrefixEnum prefix);

    /**
     * Gets the next receipt number with prefix.
     *
     * @param prefix
     *            The prefix.
     * @return The receipt number.
     */
    String getNextReceiptNumber(ReceiptNumberPrefixEnum prefix);

}
