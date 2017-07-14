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

import org.savapage.core.SpException;
import org.savapage.core.dao.PrinterDao;
import org.savapage.core.dao.helpers.ProxyPrinterName;
import org.savapage.core.dto.IppMediaCostDto;
import org.savapage.core.dto.MediaCostDto;
import org.savapage.core.jpa.Entity;
import org.savapage.core.jpa.Printer;
import org.savapage.core.jpa.Printer.ChargeType;
import org.savapage.core.jpa.PrinterAttr;
import org.savapage.core.json.JsonAbstractBase;

/**
 *
 * @author Rijk Ravestein
 *
 */
public final class PrinterDaoImpl extends GenericDaoImpl<Printer>
        implements PrinterDao {

    @Override
    public CostMediaAttr getCostMediaAttr() {
        return new CostMediaAttr();
    }

    @Override
    public CostMediaAttr getCostMediaAttr(final String ippMediaName) {
        return new CostMediaAttr(ippMediaName);
    }

    @Override
    public MediaSourceAttr getMediaSourceAttr(final String ippMediaSourceName) {
        return new MediaSourceAttr(ippMediaSourceName);
    }

    @Override
    public ChargeType getChargeType(final String chargeType) {
        return ChargeType.valueOf(chargeType);
    }

    @Override
    public IppMediaCostDto getMediaCost(final Printer printer,
            final String ippMediaName) {

        IppMediaCostDto dto = null;

        MediaCostDto dtoPageCost = null;

        final String defaultkey = getCostMediaAttr().getKey();
        final String mediaKey = getCostMediaAttr(ippMediaName).getKey();

        for (PrinterAttr attr : printer.getAttributes()) {

            final boolean isDefault =
                    attr.getName().equalsIgnoreCase(defaultkey);

            if (attr.getName().equalsIgnoreCase(mediaKey)
                    || (dtoPageCost == null && isDefault)) {

                try {
                    dtoPageCost = JsonAbstractBase.create(MediaCostDto.class,
                            attr.getValue());
                } catch (SpException e) {
                    // Be forgiving :)
                }

                if (!isDefault) {
                    break;
                }

            }
        }

        if (dtoPageCost != null) {
            dto = new IppMediaCostDto();
            dto.setMedia(ippMediaName);
            dto.setActive(Boolean.TRUE);
            dto.setPageCost(dtoPageCost);
        }

        return dto;
    }

    @Override
    public void resetTotals(final Date resetDate, final String resetBy) {

        final String jpql = "UPDATE Printer P SET "
                + "P.totalBytes = 0, P.totalEsu = 0, P.totalJobs = 0, "
                + "P.totalPages = 0, P.totalSheets = 0, "
                + "P.resetDate = :resetDate, P.resetBy = :resetBy";

        Query query = getEntityManager().createQuery(jpql);

        query.setParameter("resetDate", resetDate);
        query.setParameter("resetBy", resetBy);

        query.executeUpdate();
    }

    @Override
    public int prunePrinters() {
        /*
         * NOTE: We do NOT use bulk delete with JPQL since we want the option to
         * roll back the deletions as part of a transaction, and we want to use
         * cascade deletion. Therefore we use the remove() method in
         * EntityManager to delete individual records instead (so cascaded
         * deleted are triggered).
         */
        int nCount = 0;

        final String jpql = "SELECT P FROM Printer P WHERE P.deleted = true "
                + "AND P.printsOut IS EMPTY";

        final Query query = getEntityManager().createQuery(jpql);

        @SuppressWarnings("unchecked")
        final List<Printer> list = query.getResultList();

        for (final Printer printer : list) {
            this.delete(printer);
            nCount++;
        }

        return nCount;
    }

    @Override
    public long countPrintOuts(final Long id) {

        final String jpql = "SELECT COUNT(O.id) FROM Printer P "
                + "JOIN P.printsOut O WHERE P.id = :id";
        final Query query = getEntityManager().createQuery(jpql);

        query.setParameter("id", id);

        final Number countResult = (Number) query.getSingleResult();

        return countResult.longValue();
    }

    @Override
    public Printer findByName(final String printerName) {

        final String key = ProxyPrinterName.getDaoName(printerName);

        final String jpql =
                "SELECT P FROM Printer P WHERE P.printerName = :printerName";

        final Query query = getEntityManager().createQuery(jpql);
        query.setParameter("printerName", key);

        Printer obj;

        try {
            obj = (Printer) query.getSingleResult();
        } catch (NoResultException e) {
            obj = null;
        }

        return obj;
    }

    @Override
    public long getListCount(final ListFilter filter) {

        final StringBuilder jpql =
                new StringBuilder(JPSQL_STRINGBUILDER_CAPACITY);

        jpql.append("SELECT COUNT(P.id) FROM Printer P");

        applyListFilter(jpql, filter);

        final Query query = createListQuery(jpql.toString(), filter);
        final Number countResult = (Number) query.getSingleResult();

        return countResult.longValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Printer> getListChunk(final ListFilter filter,
            final Integer startPosition, final Integer maxResults,
            final Field orderBy, final boolean sortAscending) {

        final StringBuilder jpql =
                new StringBuilder(JPSQL_STRINGBUILDER_CAPACITY);

        /**
         * #190: Do not use JOIN FETCH construct.
         */
        jpql.append("SELECT P FROM Printer P");

        applyListFilter(jpql, filter);

        //
        jpql.append(" ORDER BY ");

        if (orderBy == Field.DISPLAY_NAME) {
            jpql.append("P.displayName");
        } else {
            jpql.append("P.displayName");
        }

        if (!sortAscending) {
            jpql.append(" DESC");
        }

        //
        final Query query = createListQuery(jpql.toString(), filter);

        //
        if (startPosition != null) {
            query.setFirstResult(startPosition);
        }
        if (maxResults != null) {
            query.setMaxResults(maxResults);
        }

        return query.getResultList();
    }

    /**
     * Creates the List Query and sets the filter parameters.
     *
     * @param jpql
     *            The JPA query string.
     * @param filter
     *            The filter.
     * @return The query.
     */
    private Query createListQuery(final String jpql, final ListFilter filter) {

        final Query query = getEntityManager().createQuery(jpql);

        if (filter.getContainingText() != null) {
            query.setParameter("containingText", String.format("%%%s%%",
                    filter.getContainingText().toLowerCase()));
        }

        if (filter.getDisabled() != null) {
            query.setParameter("selDisabled", filter.getDisabled());
        }

        if (filter.getDeleted() != null) {
            query.setParameter("selDeleted", filter.getDeleted());
        }

        return query;
    }

    /**
     * Applies the list filter to the JPQL string.
     *
     * @param jpql
     *            The {@link StringBuilder} to append to.
     * @param filter
     *            The filter.
     */
    private void applyListFilter(final StringBuilder jpql,
            final ListFilter filter) {

        final StringBuilder where = new StringBuilder();

        int nWhere = 0;

        if (filter.getContainingText() != null) {
            if (nWhere > 0) {
                where.append(" AND");
            }
            nWhere++;
            where.append(" lower(P.displayName) like :containingText");
        }

        if (filter.getDisabled() != null) {
            if (nWhere > 0) {
                where.append(" AND");
            }
            nWhere++;
            where.append(" P.disabled = :selDisabled");
        }

        if (filter.getDeleted() != null) {
            if (nWhere > 0) {
                where.append(" AND");
            }
            nWhere++;
            where.append(" P.deleted = :selDeleted");
        }
        //
        if (nWhere > 0) {
            jpql.append(" WHERE ").append(where.toString());
        }

    }

    @Override
    public Printer findByNameInsert(final String printerName) {

        Printer printer = findByName(printerName);

        if (printer == null) {

            printer = new Printer();

            printer.setPrinterName(ProxyPrinterName.getDaoName(printerName));
            printer.setDisplayName(printerName);
            printer.setCreatedDate(new Date());
            printer.setCreatedBy(Entity.ACTOR_SYSTEM);

            this.create(printer);
        }

        return printer;

    }

}
