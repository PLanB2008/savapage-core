/*
 * This file is part of the SavaPage project <http://savapage.org>.
 * Copyright (c) 2011-2015 Datraverse B.V.
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please contact Datraverse B.V. at this
 * address: info@datraverse.com
 */
package org.savapage.core.reports.impl;

import java.util.Locale;
import java.util.Map;

import net.sf.jasperreports.engine.JRDataSource;

import org.savapage.core.dao.helpers.UserPagerReq;

/**
 * User List Report creator.
 *
 * @author Datraverse B.V.
 * @since 0.9.9
 */
public final class UserListReport extends ReportCreator {

    /**
     * The unique ID of this report.
     */
    public static final String REPORT_ID = "UserList";

    /**
     * Constructor.
     *
     * @param requestingUser
     *            The requesting user.
     * @param requestingUserAdmin
     *            {@code true} if requesting user is an administrator.
     * @param inputData
     *            The input data for the report.
     * @param locale
     *            {@link Locale} of the report.
     */
    public UserListReport(final String requestingUser,
            final boolean requestingUserAdmin, final String inputData,
            final Locale locale) {
        super(requestingUser, requestingUserAdmin, inputData, locale);
    }

    @Override
    protected JRDataSource onCreateDataSource(final String inputData,
            final Locale locale, final Map<String, Object> reportParameters) {

        final UserPagerReq request = UserPagerReq.read(inputData);

        this.onUserAuthentication(null);

        final UserDataSource dataSource = new UserDataSource(request, locale);

        reportParameters
                .put("SP_DATA_SELECTION", dataSource.getSelectionInfo());

        reportParameters.put("SP_COL_HEADER_BALANCE",
                dataSource.getBalanceHeaderText());

        return dataSource;
    }

    @Override
    protected String getReportId() {
        return REPORT_ID;
    }

}
