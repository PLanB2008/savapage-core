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

import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.savapage.core.dao.PrintOutDao;
import org.savapage.core.dao.helpers.ProxyPrinterName;
import org.savapage.core.ipp.IppJobStateEnum;
import org.savapage.core.jpa.PrintOut;
import org.savapage.core.jpa.Printer;
import org.savapage.core.print.proxy.JsonProxyPrintJob;
import org.savapage.core.util.JsonHelper;

/**
 *
 * @author Rijk Ravestein
 *
 */
public final class PrintOutDaoImpl extends GenericDaoImpl<PrintOut>
        implements PrintOutDao {

    @Override
    public PrintOut findCupsJob(final String jobPrinterName,
            final Integer jobId) {

        final String jpql = "SELECT O FROM PrintOut O JOIN O.printer P "
                + "WHERE O.cupsJobId = :jobId "
                + "AND P.printerName = :printerName";

        final Query query = getEntityManager().createQuery(jpql);

        query.setParameter("jobId", jobId);
        query.setParameter("printerName",
                ProxyPrinterName.getDaoName(jobPrinterName));

        PrintOut printOut;

        try {
            printOut = (PrintOut) query.getSingleResult();
        } catch (NoResultException e) {
            printOut = null;
        }

        return printOut;
    }

    @Override
    public List<PrintOut> findActiveCupsJobs() {

        final String jpql = "SELECT O FROM PrintOut O JOIN O.printer P "
                + "WHERE O.cupsJobId > 0 "
                + "AND O.cupsJobState < :cupsJobState "
                + "ORDER BY P.printerName, O.cupsJobId";

        final Query query = getEntityManager().createQuery(jpql);
        query.setParameter("cupsJobState",
                IppJobStateEnum.getFirstAbsentOnQueueOrdinal().asInteger());

        @SuppressWarnings("unchecked")
        final List<PrintOut> jobs = query.getResultList();

        return jobs;
    }

    @Override
    public boolean updateCupsJob(final Long printOutId,
            final IppJobStateEnum ippState, final Integer cupsCompletedTime) {

        final String jpql = "UPDATE PrintOut SET cupsJobState = :cupsJobState"
                + ", cupsCompletedTime = :cupsCompletedTime  WHERE id = :id";

        final Query query = getEntityManager().createQuery(jpql);

        query.setParameter("cupsJobState", ippState.asInteger())
                .setParameter("cupsCompletedTime", cupsCompletedTime)
                .setParameter("id", printOutId);

        return executeSingleRowUpdate(query);
    }

    @Override
    public boolean updateCupsJobPrinter(final Long printOutId,
            final Printer printer, final JsonProxyPrintJob printJob,
            final Map<String, String> optionValues) {

        final String jpql = "UPDATE PrintOut" + " SET cupsJobId = :cupsJobId"
                + ", ippOptions = :ippOptions"
                + ", cupsJobState = :cupsJobState"
                + ", cupsCreationTime = :cupsCreationTime"
                + ", cupsCompletedTime = :cupsCompletedTime"
                + ", printer = :printer" + " WHERE id = :id ";

        final Query query = getEntityManager().createQuery(jpql);

        query.setParameter("cupsJobId", printJob.getJobId())
                .setParameter("cupsJobState", printJob.getJobState())
                .setParameter("ippOptions",
                        JsonHelper.stringifyStringMap(optionValues))
                .setParameter("cupsCreationTime", printJob.getCreationTime())
                .setParameter("cupsCompletedTime", printJob.getCompletedTime())
                .setParameter("printer", printer)
                .setParameter("id", printOutId);

        return executeSingleRowUpdate(query);
    }

    @Override
    public IppJobStateEnum getIppJobState(final PrintOut printOut) {

        final Integer jobState = printOut.getCupsJobState();

        if (jobState == null) {
            return null;
        }
        return IppJobStateEnum.asEnum(jobState);
    }

}
