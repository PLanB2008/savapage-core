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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.savapage.core.concurrent.ReadWriteLockEnum;
import org.savapage.core.config.ConfigManager;
import org.savapage.core.config.IConfigProp.Key;
import org.savapage.core.dao.AccountTrxDao;
import org.savapage.core.dao.AccountTrxDao.ListFilter;
import org.savapage.core.dao.DaoContext;
import org.savapage.core.dao.DocInOutDao;
import org.savapage.core.dao.DocLogDao;
import org.savapage.core.dao.PrintOutDao;
import org.savapage.core.dao.enums.DaoEnumHelper;
import org.savapage.core.dao.enums.DocLogProtocolEnum;
import org.savapage.core.dao.enums.ExternalSupplierEnum;
import org.savapage.core.dao.enums.ExternalSupplierStatusEnum;
import org.savapage.core.dao.enums.PrintModeEnum;
import org.savapage.core.ipp.IppJobStateEnum;
import org.savapage.core.jpa.AccountTrx;
import org.savapage.core.jpa.DocIn;
import org.savapage.core.jpa.DocLog;
import org.savapage.core.jpa.DocOut;
import org.savapage.core.jpa.PrintOut;
import org.savapage.core.services.ProxyPrintService;
import org.savapage.core.services.ServiceContext;
import org.savapage.core.services.helpers.PrintSupplierData;
import org.savapage.core.services.helpers.ThirdPartyEnum;
import org.savapage.core.util.DateUtil;
import org.savapage.ext.ExtSupplierConnectException;
import org.savapage.ext.ExtSupplierException;
import org.savapage.ext.papercut.services.PaperCutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract pattern for monitoring PaperCut print status of jobs issued from an
 * {@link ExternalSupplierEnum}.
 *
 * @author Rijk Ravestein
 *
 */
public abstract class PaperCutPrintMonitorPattern
        implements PaperCutAccountResolver {

    /**
     * The logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(PaperCutPrintMonitorPattern.class);

    private static final AccountTrxDao ACCOUNT_TRX_DAO =
            ServiceContext.getDaoContext().getAccountTrxDao();

    private static final DocLogDao DOC_LOG_DAO =
            ServiceContext.getDaoContext().getDocLogDao();

    private static final PrintOutDao PRINT_OUT_DAO =
            ServiceContext.getDaoContext().getPrintOutDao();

    /**
     * .
     */
    protected static final PaperCutService PAPERCUT_SERVICE =
            ServiceContext.getServiceFactory().getPaperCutService();

    /**
     * .
     */
    protected static final ProxyPrintService PROXY_PRINT_SERVICE =
            ServiceContext.getServiceFactory().getProxyPrintService();

    /**
     * .
     */
    private final PaperCutServerProxy papercutServerProxy;

    /**
     * .
     */
    private final PaperCutDbProxy papercutDbProxy;

    /**
     * .
     */
    private final DocLogDao.ListFilter listFilterPendingExt;

    /**
     * .
     */
    private final PaperCutPrintJobListener statusListener;

    /**
     * @return {@code true} when account transaction candidates are linked with
     *         the {@link DocLog} of the {@link DocIn}, {@code false} when
     *         linked with the {@link DocLog} of the {@link DocOut}.
     */
    protected abstract boolean isDocInAccountTrx();

    /**
     *
     * @param externalSupplier
     *            The {@link ExternalSupplierEnum} the print job is issued from.
     * @param serverProxy
     *            The {@link PaperCutServerProxy}.
     * @param dbProxy
     *            The {@link PaperCutDbProxy}.
     * @param listener
     *            The {@link PaperCutPrintJobListener}.
     */
    protected PaperCutPrintMonitorPattern(
            final ExternalSupplierEnum externalSupplier,
            final PaperCutServerProxy serverProxy,
            final PaperCutDbProxy dbProxy,
            final PaperCutPrintJobListener listener) {

        this.listFilterPendingExt = new DocLogDao.ListFilter();
        this.listFilterPendingExt.setExternalSupplier(externalSupplier);
        this.listFilterPendingExt
                .setExternalStatus(ExternalSupplierStatusEnum.PENDING_EXT);
        this.listFilterPendingExt.setProtocol(DocLogProtocolEnum.IPP);

        this.papercutServerProxy = serverProxy;
        this.papercutDbProxy = dbProxy;

        this.statusListener = listener;
    }

    /**
     * Logs content of a {@link PaperCutPrinterUsageLog} object.
     *
     * @param usageLog
     *            The object to log.
     */
    private static void debugLog(final PaperCutPrinterUsageLog usageLog) {

        if (LOGGER.isDebugEnabled()) {
            final StringBuilder msg = new StringBuilder();
            msg.append(usageLog.getDocumentName()).append(" | printed [")
                    .append(usageLog.isPrinted()).append("] cancelled [")
                    .append(usageLog.isCancelled()).append("] deniedReason [")
                    .append(usageLog.getDeniedReason()).append("] usageCost[")
                    .append(usageLog.getUsageCost()).append("]");
            LOGGER.debug(msg.toString());
        }
    }

    /**
     * Gets the {@link DocLog} from the input or output depending on what is
     * leading for account transactions.
     *
     * @param docLogOut
     *            The DocLog with linked {@link DocOut}.
     * @param docLogIn
     *            The DocLog with linked {@link DocIn}.
     * @return The leading DocLog.
     */
    private DocLog getDocLogForTrx(final DocLog docLogOut,
            final DocLog docLogIn) {
        if (this.isDocInAccountTrx()) {
            return docLogIn;
        } else {
            return docLogOut;
        }
    }

    /**
     * Processes pending jobs printed to a PaperCut managed printer.
     *
     * @throws PaperCutException
     * @throws ExtSupplierException
     * @throws ExtSupplierConnectException
     */
    public final void process() throws PaperCutException, ExtSupplierException,
            ExtSupplierConnectException {

        final Map<String, DocLog> uniquePaperCutDocNames = new HashMap<>();

        /*
         * Find pending SavaPage jobs.
         */
        for (final DocLog docLog : DOC_LOG_DAO
                .getListChunk(listFilterPendingExt)) {
            uniquePaperCutDocNames.put(docLog.getTitle(), docLog);
        }

        if (uniquePaperCutDocNames.isEmpty()) {
            return;
        }

        if (!this.statusListener.onPaperCutPrintJobProcessingStep()) {
            return;
        }

        /*
         * Find PaperCut jobs.
         */
        final List<PaperCutPrinterUsageLog> papercutLogList =
                PAPERCUT_SERVICE.getPrinterUsageLog(papercutDbProxy,
                        uniquePaperCutDocNames.keySet());

        final Set<String> paperCutDocNamesHandled = new HashSet<>();

        ServiceContext.resetTransactionDate();

        for (final PaperCutPrinterUsageLog papercutLog : papercutLogList) {

            if (!this.statusListener.onPaperCutPrintJobProcessingStep()) {
                return;
            }

            //
            paperCutDocNamesHandled.add(papercutLog.getDocumentName());

            debugLog(papercutLog);

            final DocLog docLog =
                    uniquePaperCutDocNames.get(papercutLog.getDocumentName());

            final ExternalSupplierStatusEnum printStatus;
            boolean isDocumentTooLarge = false;

            /*
             * Database transaction.
             */
            ReadWriteLockEnum.DATABASE_READONLY.setReadLock(true);

            final DaoContext daoContext = ServiceContext.getDaoContext();

            try {

                if (!daoContext.isTransactionActive()) {
                    daoContext.beginTransaction();
                }

                if (papercutLog.isPrinted()) {

                    printStatus = ExternalSupplierStatusEnum.COMPLETED;

                    this.processPrintJobCompleted(docLog, papercutLog);

                } else {

                    if (papercutLog.getDeniedReason().contains("TIMEOUT")) {

                        printStatus = ExternalSupplierStatusEnum.EXPIRED;

                    } else if (papercutLog.getDeniedReason()
                            .contains("DOCUMENT_TOO_LARGE")) {

                        printStatus = ExternalSupplierStatusEnum.CANCELLED;
                        isDocumentTooLarge = true;

                    } else {
                        printStatus = ExternalSupplierStatusEnum.CANCELLED;
                    }

                    this.processPrintJobCancelled(docLog, papercutLog,
                            printStatus);
                }

                daoContext.commit();

            } finally {

                daoContext.rollback();

                ReadWriteLockEnum.DATABASE_READONLY.setReadLock(false);
            }

            this.statusListener.onPaperCutPrintJobProcessed(docLog, papercutLog,
                    printStatus, isDocumentTooLarge);

        } // end-for

        this.processPrintJobsNotFound(uniquePaperCutDocNames,
                paperCutDocNamesHandled);
    }

    /**
     * Processes pending SavaPage print jobs that cannot be found in PaperCut:
     * status is set to {@link ExternalSupplierStatusEnum#ERROR} when SavaPage
     * print job is more then
     * {@link PaperCutService#getPrintLogMaxWaitMinutes()} old.
     *
     * @param papercutDocNamesToFind
     *            The PaperCut documents to find.
     * @param papercutDocNamesFound
     *            The PaperCut documents found.
     */
    private void processPrintJobsNotFound(
            final Map<String, DocLog> papercutDocNamesToFind,
            final Set<String> papercutDocNamesFound) {

        final long maxWaitMsec = ConfigManager.instance()
                .getConfigInt(Key.PROXY_PRINT_PAPERCUT_PRINTLOG_MAX_MINS)
                * DateUtil.DURATION_MSEC_MINUTE;

        for (final String docName : papercutDocNamesToFind.keySet()) {

            if (papercutDocNamesFound.contains(docName)) {
                continue;
            }

            final DocLog docLog = papercutDocNamesToFind.get(docName);

            final long docAge = ServiceContext.getTransactionDate().getTime()
                    - docLog.getCreatedDate().getTime();

            if (docAge < maxWaitMsec) {
                continue;
            }

            /*
             * Database transaction.
             */
            ReadWriteLockEnum.DATABASE_READONLY.setReadLock(true);

            final DaoContext daoContext = ServiceContext.getDaoContext();

            try {

                if (!daoContext.isTransactionActive()) {
                    daoContext.beginTransaction();
                }

                docLog.setExternalStatus(
                        ExternalSupplierStatusEnum.ERROR.toString());

                DOC_LOG_DAO.update(docLog);

                daoContext.commit();

            } finally {

                daoContext.rollback();

                ReadWriteLockEnum.DATABASE_READONLY.setReadLock(false);
            }

            this.statusListener.onPaperCutPrintJobNotFound(docName, docAge);
        }
    }

    /**
     * Gets the total number of copies printed.
     * <p>
     * IMPORTANT: the accumulated weight of the individual Account transactions
     * need NOT be the same as the number of copies (since parts of the printing
     * costs may be charged to multiple accounts).
     * </p>
     *
     * @param docLogOut
     *            The DocLog with linked {@link DocOut}.
     * @param docLogIn
     *            The DocLog with linked {@link DocIn}.
     * @return The weight.
     */
    protected abstract int getAccountTrxWeightTotal(DocLog docLogOut,
            DocLog docLogIn);

    /**
     * @return The {@link Logger}.
     */
    protected abstract Logger getLogger();

    /**
     * @param docLogOut
     *            The DocLog with linked {@link DocOut}.
     *
     * @return The single DocLog source, if present and relevant. {@code null}
     *         when not present or not relevant.
     */
    private DocLog getDocLogIn(final DocLog docLogOut) {

        if (this.isDocInAccountTrx()) {
            final DocInOutDao docInOutDao =
                    ServiceContext.getDaoContext().getDocInOutDao();

            return docInOutDao.findDocOutSource(docLogOut.getDocOut().getId());
        }
        return null;
    }

    /**
     * Processes a cancelled PaperCut print job.
     * <ul>
     * <li>The {@link DocLog} target and (conditionally the) source is updated
     * with the {@link ExternalSupplierStatusEnum}.</li>
     * <li>Publish Admin messages.</li>
     * </ul>
     *
     * @param docLogOut
     *            The SavaPage {@link DocLog} target holding the {@link DocOut}
     *            with the {@link PrintOut}.
     * @param papercutLog
     *            The {@link PaperCutPrinterUsageLog}.
     * @param printStatus
     *            The {@link ExternalSupplierStatusEnum}.
     */
    private void processPrintJobCancelled(final DocLog docLogOut,
            final PaperCutPrinterUsageLog papercutLog,
            final ExternalSupplierStatusEnum printStatus) {

        /*
         * Update print status in the DocLog (with linked DocOut).
         */
        docLogOut.setExternalStatus(printStatus.toString());
        DOC_LOG_DAO.update(docLogOut);

        /*
         * Update the single DocLog source (if present and relevant).
         */
        final DocLog docLogIn = this.getDocLogIn(docLogOut);

        if (docLogIn != null) {
            docLogIn.setExternalStatus(printStatus.toString());
            DOC_LOG_DAO.update(docLogIn);
        }
    }

    /**
     * Processes a completed PaperCut print job.
     * <ul>
     * <li>The Personal and Shared [Parent]\[UserGroup] accounts are lazy
     * adjusted in PaperCut and SavaPage.</li>
     * <li>Conditionally the {@link AccountTrx} objects are moved from the
     * {@link DocLog} source to the {@link DocLog} target.</li>
     * <li>The {@link DocLog} target is updated with the
     * {@link ExternalSupplierStatusEnum}.</li>
     * <li>Publish Admin messages.</li>
     * </ul>
     *
     * @param docLogOut
     *            The SavaPage {@link DocLog} target holding the {@link DocOut}
     *            with the {@link PrintOut}.
     * @param papercutLog
     *            The {@link PaperCutPrinterUsageLog}.
     * @throws PaperCutException
     *             When a PaperCut error occurs.
     */
    private void processPrintJobCompleted(final DocLog docLogOut,
            final PaperCutPrinterUsageLog papercutLog)
            throws PaperCutException {

        /*
         * Get the single DocLog source (if present and relevant).
         */
        final DocLog docLogIn = this.getDocLogIn(docLogOut);

        //
        final DocLog docLogTrx = this.getDocLogForTrx(docLogOut, docLogIn);

        //
        final PrintOut printOutLog = docLogOut.getDocOut().getPrintOut();
        final PrintModeEnum printMode = DaoEnumHelper.getPrintMode(printOutLog);

        /*
         * Set External Status to COMPLETED.
         */
        final String externalPrintJobStatus =
                ExternalSupplierStatusEnum.COMPLETED.toString();

        docLogOut.setExternalStatus(externalPrintJobStatus);

        /*
         * Check if CUPS PrintOut status is completed as well. If not, Correct
         * CUPS print status to completed according to PaperCut reporting.
         * Mantis #833
         */
        final IppJobStateEnum cupsJobState = IppJobStateEnum
                .asEnum(printOutLog.getCupsJobState().intValue());

        if (cupsJobState != IppJobStateEnum.IPP_JOB_COMPLETED) {

            LOGGER.warn(String.format(
                    "%s reported %s: CUPS Job %d %s is corrected to %s.",
                    ThirdPartyEnum.PAPERCUT.getUiText(), externalPrintJobStatus,
                    printOutLog.getCupsJobId().intValue(),
                    cupsJobState.asLogText(),
                    IppJobStateEnum.IPP_JOB_COMPLETED.asLogText()));

            printOutLog.setCupsJobState(
                    IppJobStateEnum.IPP_JOB_COMPLETED.asInteger());

            printOutLog.setCupsCompletedTime(
                    Integer.valueOf(PROXY_PRINT_SERVICE.getCupsSystemTime()));

            PRINT_OUT_DAO.update(printOutLog);
        }

        /*
         * Any transactions?
         */
        if (docLogTrx.getTransactions() == null) {
            /*
             * Somehow (?), when AccountTrx's were ad-hoc created when Job
             * Ticket was printed to a PaperCut managed printer, we need to
             * retrieve them this way. Why?
             */
            final ListFilter filter = new ListFilter();
            filter.setDocLogId(docLogTrx.getId());
            docLogTrx.setTransactions(ACCOUNT_TRX_DAO.getListChunk(filter, null,
                    null, null, true));
        }

        final List<AccountTrx> trxList = docLogTrx.getTransactions();

        if (trxList.isEmpty()) {

            if (LOGGER.isWarnEnabled()) {

                LOGGER.warn(String.format(
                        "No DocLog transactions found for [%s] : "
                                + "DocLog (Out) [%d], DocLog (Trx) [%d]",
                        docLogTrx.getTitle(), docLogOut.getId().longValue(),
                        docLogTrx.getId().longValue()));
            }
            /*
             * Just update the DocLog with status COMPLETED.
             */
            DOC_LOG_DAO.update(docLogOut);
            return;
        }

        final int printedCopies =
                docLogOut.getDocOut().getPrintOut().getNumberOfCopies();

        final PrintSupplierData printSupplierData;

        if (docLogOut.getExternalData() == null) {
            printSupplierData = null;
        } else {
            printSupplierData = PrintSupplierData
                    .createFromData(docLogOut.getExternalData());
        }

        /*
         * Determine printing cost to use, and whether to create PaperCut
         * transactions.
         */
        final int weightTotal;
        final BigDecimal weightTotalCost;
        final boolean createPaperCutTrx;
        final boolean costPaperCutLeading;

        if (printMode == PrintModeEnum.TICKET) {

            if (printSupplierData == null) {
                /*
                 * TODO: use case?
                 */
                createPaperCutTrx = true;
                costPaperCutLeading = true;

                weightTotal = printedCopies;
                weightTotalCost =
                        BigDecimal.valueOf(papercutLog.getUsageCost());

                LOGGER.warn("{} Print: no external data. Using PaperCut cost.",
                        printMode);
            } else {
                /*
                 * Ticket Print.
                 */
                createPaperCutTrx = true;
                costPaperCutLeading = false;

                weightTotalCost = printSupplierData.getCostTotal();

                if (printSupplierData.getWeightTotal() == null) {
                    /*
                     * TODO: use case?
                     */
                    weightTotal = printedCopies;
                    LOGGER.warn(
                            "{} Print: no weight total in external data. "
                                    + "Using printed copies as weight total.",
                            printMode);
                } else {
                    weightTotal = printSupplierData.getWeightTotal().intValue();
                }
            }
        } else {

            final PaperCutIntegrationEnum papercutInt =
                    PAPERCUT_SERVICE.getPrintIntegration();

            createPaperCutTrx =
                    papercutInt == PaperCutIntegrationEnum.DELEGATED_PRINT;

            final BigDecimal costPaperCut =
                    BigDecimal.valueOf(papercutLog.getUsageCost());

            if (printSupplierData == null) {

                costPaperCutLeading = true;
                weightTotal = printedCopies;
                weightTotalCost = costPaperCut;

            } else {

                if (printSupplierData.getWeightTotal() == null) {
                    /*
                     * Just in case...
                     */
                    weightTotal = printedCopies;
                    LOGGER.warn(
                            "{} Print: no weight total in external data. "
                                    + "Using printed copies as weight total.",
                            printMode);
                } else {
                    weightTotal = printSupplierData.getWeightTotal().intValue();
                }

                if (printSupplierData.hasCost()) {
                    costPaperCutLeading = false;
                    weightTotalCost = printSupplierData.getCostTotal();
                } else {
                    costPaperCutLeading = true;
                    weightTotalCost = costPaperCut;
                }
            }
        }

        final PaperCutAccountAdjustPrint accountAdjustPattern =
                new PaperCutAccountAdjustPrint(papercutServerProxy, this,
                        this.getLogger());

        accountAdjustPattern.process(docLogTrx, docLogOut,
                this.isDocInAccountTrx(), weightTotalCost, weightTotal,
                printedCopies, createPaperCutTrx);

        /*
         * DocLog updates.
         */
        if (this.isDocInAccountTrx()) {
            /*
             * Move the AccountTrx list from DocLog source to target.
             */
            docLogOut.setTransactions(trxList);

            docLogIn.setExternalStatus(externalPrintJobStatus);
            docLogIn.setTransactions(null);
            DOC_LOG_DAO.update(docLogIn);
        }

        /*
         * Overwrite the zero cost(original) values with the charged cost.
         */
        docLogOut.setCost(weightTotalCost);
        docLogOut.setCostOriginal(weightTotalCost);

        /*
         * Update external data.
         */
        final PrintSupplierData printSupplierDataUpd;

        if (printSupplierData == null) {
            printSupplierDataUpd = new PrintSupplierData();
        } else {
            printSupplierDataUpd = printSupplierData;
        }

        printSupplierDataUpd
                .setClientCost(Boolean.valueOf(costPaperCutLeading));
        printSupplierDataUpd
                .setClientCostTrx(Boolean.valueOf(createPaperCutTrx));

        docLogOut.setExternalData(printSupplierDataUpd.dataAsString());

        DOC_LOG_DAO.update(docLogOut);
    }

}
