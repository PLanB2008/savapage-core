/*
 * This file is part of the SavaPage project <https://www.savapage.org>.
 * Copyright (c) 2011-2017 Datraverse B.V.
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * For more information, please contact Datraverse B.V. at this
 * address: info@datraverse.com
 */
package org.savapage.core.dao.impl;

import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.savapage.core.dao.AccountTrxDao;
import org.savapage.core.jpa.AccountTrx;

/**
 *
 * @author Rijk Ravestein
 *
 */
public final class AccountTrxDaoImpl extends GenericDaoImpl<AccountTrx>
        implements AccountTrxDao {

    @Override
    protected String getCountQuery() {
        return "SELECT COUNT(T.id) FROM AccountTrx T";
    }

    @Override
    public long getListCount(final ListFilter filter) {

        final StringBuilder jpql =
                new StringBuilder(JPSQL_STRINGBUILDER_CAPACITY);

        jpql.append("SELECT COUNT(TRX.id) FROM AccountTrx TRX");

        applyListFilter(jpql, filter);

        final Query query = createListQuery(jpql, filter);
        final Number countResult = (Number) query.getSingleResult();

        return countResult.longValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<AccountTrx> getListChunk(final ListFilter filter,
            final Integer startPosition, final Integer maxResults,
            final Field orderBy, final boolean sortAscending) {

        final StringBuilder jpql =
                new StringBuilder(JPSQL_STRINGBUILDER_CAPACITY);

        jpql.append("SELECT TRX FROM AccountTrx TRX");

        applyListFilter(jpql, filter);

        //
        jpql.append(" ORDER BY ");

        if (orderBy == Field.TRX_TYPE) {
            jpql.append("TRX.trxType");
        } else {
            jpql.append("TRX.transactionDate");
        }

        if (!sortAscending) {
            jpql.append(" DESC");
        }

        jpql.append(", TRX.id DESC");

        //
        final Query query = createListQuery(jpql, filter);

        if (startPosition != null) {
            query.setFirstResult(startPosition);
        }
        if (maxResults != null) {
            query.setMaxResults(maxResults);
        }

        return query.getResultList();
    }

    /**
     * Applies the list filter to the JPQL string.
     *
     * @param jpql
     *            The JPA query string.
     * @param filter
     *            The {@link ListFilter}.
     */
    private void applyListFilter(final StringBuilder jpql,
            final ListFilter filter) {

        final boolean filterAccountType = filter.getAccountType() != null;

        final StringBuilder joinClause = new StringBuilder();

        if (filter.getUserId() != null) {

            joinClause.append(" JOIN TRX.account AA WHERE AA.id ="
                    + " (SELECT A.id FROM UserAccount UA" + " JOIN UA.user U"
                    + " JOIN UA.account A" + " WHERE U.id = :userId");

            if (filterAccountType) {
                joinClause.append(" AND A.accountType = :accountType");
            }

            joinClause.append(")");

        } else if (filter.getAccountId() != null) {

            joinClause.append(" JOIN TRX.account AA WHERE AA.id = ")
                    .append(filter.getAccountId());

        } else if (filter.getDocLogId() != null) {

            joinClause.append(" WHERE TRX.docLog.id = :docLogId");

        } else {

            joinClause.append(" JOIN TRX.account AA");

            if (filterAccountType) {
                joinClause.append(" WHERE AA.accountType = :accountType");
            }

        }

        int nWhere = 0;
        final StringBuilder where = new StringBuilder();

        if (filter.getTrxType() != null) {
            if (nWhere > 0) {
                where.append(" AND");
            }
            nWhere++;
            where.append(" TRX.trxType = :trxType");
        }

        if (filter.getDateFrom() != null) {
            if (nWhere > 0) {
                where.append(" AND");
            }
            nWhere++;
            where.append(" TRX.transactionDate >= :dateFrom");
        }

        if (filter.getDateTo() != null) {
            if (nWhere > 0) {
                where.append(" AND");
            }
            nWhere++;
            where.append(" TRX.transactionDate <= :dateTo");
        }

        if (filter.getContainingCommentText() != null) {
            if (nWhere > 0) {
                where.append(" AND");
            }
            nWhere++;
            where.append(" lower(TRX.comment) like :containingText");
        }

        jpql.append(joinClause);

        if (nWhere > 0) {
            jpql.append(" AND").append(where);
        }

    }

    /**
     * Creates the List Query and sets the filter parameters.
     *
     * @param jpql
     *            The JPA query string.
     * @param filter
     *            The {@link ListFilter}.
     * @return The {@link Query}.
     */
    private Query createListQuery(final StringBuilder jpql,
            final ListFilter filter) {

        final Query query = getEntityManager().createQuery(jpql.toString());

        if (filter.getDocLogId() != null) {
            query.setParameter("docLogId", filter.getDocLogId());
        }

        if (filter.getAccountType() != null) {
            query.setParameter("accountType",
                    filter.getAccountType().toString());
        }
        if (filter.getUserId() != null) {
            query.setParameter("userId", filter.getUserId());
        }
        if (filter.getTrxType() != null) {
            query.setParameter("trxType", filter.getTrxType().toString());
        }
        if (filter.getDateFrom() != null) {
            query.setParameter("dateFrom", filter.getDateFrom());
        }
        if (filter.getDateTo() != null) {
            query.setParameter("dateTo", filter.getDateTo());
        }
        if (filter.getContainingCommentText() != null) {
            query.setParameter("containingText", String.format("%%%s%%",
                    filter.getContainingCommentText().toLowerCase()));
        }
        return query;
    }

    @Override
    public int cleanHistory(final Date dateBackInTime) {

        final String jpql = "SELECT T FROM AccountTrx T WHERE"
                + " transactionDate <= :transactionDate";

        final Query query = getEntityManager().createQuery(jpql);

        query.setParameter("transactionDate", dateBackInTime);

        @SuppressWarnings("unchecked")
        final List<AccountTrx> list = query.getResultList();

        int nDeleted = 0;

        for (final AccountTrx accountTrx : list) {
            // cascaded delete
            this.delete(accountTrx);
            nDeleted++;
        }
        return nDeleted;

    }

    @Override
    public AccountTrx findByExtId(final String extId) {

        final String jpql = "SELECT T FROM AccountTrx T WHERE extId = :extId";

        final Query query = getEntityManager().createQuery(jpql);

        query.setParameter("extId", extId);

        AccountTrx result = null;

        try {
            result = (AccountTrx) query.getSingleResult();
        } catch (NoResultException e) {
            result = null;
        }

        return result;
    }

    @Override
    public List<AccountTrx> findByExtMethodAddress(final String address) {

        final String jpql = "SELECT T FROM AccountTrx T "
                + "WHERE extMethodAddress = :extMethodAddress";

        final Query query = getEntityManager().createQuery(jpql);

        query.setParameter("extMethodAddress", address);

        @SuppressWarnings("unchecked")
        final List<AccountTrx> list = query.getResultList();

        return list;
    }

}
