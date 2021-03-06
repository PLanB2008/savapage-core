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
package org.savapage.core.dao.impl;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.savapage.core.dao.IAttrDao;
import org.savapage.core.dao.UserAttrDao;
import org.savapage.core.dao.enums.UserAttrEnum;
import org.savapage.core.jpa.UserAttr;

/**
 *
 * @author Rijk Ravestein
 *
 */
public final class UserAttrDaoImpl extends GenericDaoImpl<UserAttr>
        implements UserAttrDao, IAttrDao {

    @Override
    protected String getCountQuery() {
        return "SELECT COUNT(T.id) FROM UserAttr T";
    }

    /**
     * This SQL LIKE value is used to select all rolling statistics.
     * <p>
     * INVARIANT: all rolling statistics MUST hold the fragment
     * {@code " + UserAttrDao.STATS_ROLLING + "} in their name.
     * </p>
     */
    private static final String SQL_LIKE_STATS_ROLLING =
            "%" + STATS_ROLLING + "%";

    @Override
    public UserAttr findByName(final Long userDbKey, final UserAttrEnum name) {
        return this.findByName(userDbKey, name.getName());
    }

    @Override
    public UserAttr findByName(final Long userDbKey, final String name) {

        final String jpql = "SELECT A FROM UserAttr A JOIN A.user U "
                + "WHERE U.id = :userId AND A.name = :name";

        final Query query = getEntityManager().createQuery(jpql);

        query.setParameter("userId", userDbKey);
        query.setParameter("name", name);

        UserAttr result = null;

        try {
            result = (UserAttr) query.getSingleResult();
        } catch (NoResultException e) {
            result = null;
        }

        return result;
    }

    @Override
    public UserAttr findByNameValue(final UserAttrEnum name,
            final String value) {

        final String jpql = "SELECT A FROM UserAttr A "
                + "WHERE A.name = :name AND A.value = :value";

        final Query query = getEntityManager().createQuery(jpql);

        query.setParameter("name", name.getName());
        query.setParameter("value", value);

        UserAttr result = null;

        try {
            result = (UserAttr) query.getSingleResult();
        } catch (NoResultException e) {
            result = null;
        }

        return result;
    }

    @Override
    public void deleteRollingStats() {
        final String jpql = "DELETE UserAttr A WHERE A.name LIKE :name";
        final Query query = getEntityManager().createQuery(jpql);
        query.setParameter("name", SQL_LIKE_STATS_ROLLING);
        query.executeUpdate();
    }

    @Override
    public boolean getBooleanValue(final UserAttr attr) {
        return attr != null && StringUtils.defaultString(attr.getValue(), V_NO)
                .equalsIgnoreCase(V_YES);
    }

    @Override
    public String getDbBooleanValue(final boolean value) {
        if (value) {
            return V_YES;
        }
        return V_NO;
    }

}
