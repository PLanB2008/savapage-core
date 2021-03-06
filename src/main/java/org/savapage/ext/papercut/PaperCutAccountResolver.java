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
package org.savapage.ext.papercut;

import org.savapage.core.jpa.Account.AccountTypeEnum;

/**
 * Gives information about PaperCut shared accounts.
 *
 * @author Rijk Ravestein
 *
 */
public interface PaperCutAccountResolver {

    /**
     * @return The shared top-level shared account that must be present in
     *         PaperCut. Several sub-accounts will be lazy created by SavaPage.
     */
    String getSharedParentAccountName();

    /**
     * Uses SavaPage {@link Account} data to compose a shared (sub) account name
     * for PaperCut.
     *
     * @param accountType
     *            The SavaPage {@link AccountTypeEnum}.
     * @param accountName
     *            The SavaPage account name.
     * @param accountNameParent
     *            The name of the SavaPage parent account. Is {@code null} when
     *            account is not a child account, but a parent account itself.
     * @return The composed sub account name to be used in PaperCut.
     */
    String composeSharedSubAccountName(AccountTypeEnum accountType,
            String accountName, String accountNameParent);

    /**
     * @return The sub-account of {@link #getSharedParentAccountName()} holding
     *         Print Job transactions.
     */
    String getSharedJobsAccountName();

    /**
     * @return PaperCut is configured with Multiple Personal Accounts, and this
     *         is the account name to use for personal transactions.
     */
    String getUserAccountName();

    /**
     * Gets the klas (group or shared account) name from the composed account
     * name.
     * <p>
     * Note: The klas (group) name is needed to compose the comment of a newly
     * created PaperCut account transaction.
     * </p>
     *
     * @param accountName
     *            The (composed) SavaPage account name.
     * @return The klas (group) name.
     */
    String getKlasFromAccountName(String accountName);

}
