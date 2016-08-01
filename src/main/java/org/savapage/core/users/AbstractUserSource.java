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
package org.savapage.core.users;

import java.util.Comparator;

/**
 *
 * @author Datraverse B.V.
 *
 */
public class AbstractUserSource {

    /**
     *
     */
    public static class CommonUserComparator implements
            Comparator<CommonUser> {

        @Override
        public final int compare(final CommonUser o1, final CommonUser o2) {
            return o1.getUserName().compareTo(o2.getUserName());
        }

    };

    /**
     *
     */
    protected static class IgnoreCaseComparator implements Comparator<String> {

        @Override
        public final int compare(final String o1, final String o2) {
            return o1.compareToIgnoreCase(o2);
        }

    }

    /**
     * Converts an externally offered user id to a format used in the database.
     *
     * @param userId
     *            The user id offered by an external source.
     * @param isLdapSync
     *            {@code true} is database users are synchronized with an LDAP
     *            user source.
     * @return The converted user id.
     */
    public static String asDbUserId(final String userId,
            final boolean isLdapSync) {

        if (isLdapSync) {
            return userId.toLowerCase();
        }
        return userId;
    }

}
