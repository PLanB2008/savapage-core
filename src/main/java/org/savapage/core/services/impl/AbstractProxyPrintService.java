/*
 * This file is part of the SavaPage project <https://www.savapage.org>.
 * Copyright (c) 2011-2018 Datraverse B.V.
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
package org.savapage.core.services.impl;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.print.attribute.standard.MediaSizeName;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.savapage.core.LetterheadNotFoundException;
import org.savapage.core.PerformanceLogger;
import org.savapage.core.PostScriptDrmException;
import org.savapage.core.SpException;
import org.savapage.core.SpInfo;
import org.savapage.core.circuitbreaker.CircuitStateEnum;
import org.savapage.core.cometd.AdminPublisher;
import org.savapage.core.cometd.PubLevelEnum;
import org.savapage.core.cometd.PubTopicEnum;
import org.savapage.core.config.CircuitBreakerEnum;
import org.savapage.core.config.ConfigManager;
import org.savapage.core.config.IConfigProp;
import org.savapage.core.config.IConfigProp.Key;
import org.savapage.core.dao.DaoContext;
import org.savapage.core.dao.PrinterDao;
import org.savapage.core.dao.enums.ACLRoleEnum;
import org.savapage.core.dao.enums.AccountTrxTypeEnum;
import org.savapage.core.dao.enums.DeviceTypeEnum;
import org.savapage.core.dao.enums.DocLogProtocolEnum;
import org.savapage.core.dao.enums.ExternalSupplierEnum;
import org.savapage.core.dao.enums.ExternalSupplierStatusEnum;
import org.savapage.core.dao.enums.PrintModeEnum;
import org.savapage.core.dao.enums.PrinterAttrEnum;
import org.savapage.core.dao.helpers.DaoBatchCommitter;
import org.savapage.core.dao.helpers.ProxyPrinterName;
import org.savapage.core.dto.IppMediaSourceCostDto;
import org.savapage.core.dto.IppMediaSourceMappingDto;
import org.savapage.core.dto.PrinterSnmpDto;
import org.savapage.core.imaging.EcoPrintPdfTask;
import org.savapage.core.imaging.EcoPrintPdfTaskPendingException;
import org.savapage.core.inbox.InboxInfoDto;
import org.savapage.core.inbox.InboxInfoDto.InboxJob;
import org.savapage.core.inbox.InboxInfoDto.InboxJobRange;
import org.savapage.core.inbox.OutputProducer;
import org.savapage.core.ipp.IppJobStateEnum;
import org.savapage.core.ipp.IppMediaSizeEnum;
import org.savapage.core.ipp.IppSyntaxException;
import org.savapage.core.ipp.attribute.IppDictJobTemplateAttr;
import org.savapage.core.ipp.attribute.syntax.IppKeyword;
import org.savapage.core.ipp.client.IppConnectException;
import org.savapage.core.ipp.client.IppNotificationRecipient;
import org.savapage.core.ipp.rules.IppRuleConstraint;
import org.savapage.core.job.SpJobScheduler;
import org.savapage.core.job.SpJobType;
import org.savapage.core.jpa.Account;
import org.savapage.core.jpa.Account.AccountTypeEnum;
import org.savapage.core.jpa.CostChange;
import org.savapage.core.jpa.Device;
import org.savapage.core.jpa.DocLog;
import org.savapage.core.jpa.DocOut;
import org.savapage.core.jpa.Entity;
import org.savapage.core.jpa.PrintOut;
import org.savapage.core.jpa.Printer;
import org.savapage.core.jpa.PrinterAttr;
import org.savapage.core.jpa.User;
import org.savapage.core.jpa.UserCard;
import org.savapage.core.json.JsonPrinter;
import org.savapage.core.json.JsonPrinterDetail;
import org.savapage.core.json.JsonPrinterList;
import org.savapage.core.json.rpc.AbstractJsonRpcMessage;
import org.savapage.core.json.rpc.JsonRpcError.Code;
import org.savapage.core.json.rpc.JsonRpcMethodError;
import org.savapage.core.json.rpc.JsonRpcMethodResult;
import org.savapage.core.json.rpc.impl.ParamsPrinterSnmp;
import org.savapage.core.json.rpc.impl.ResultAttribute;
import org.savapage.core.json.rpc.impl.ResultPrinterSnmp;
import org.savapage.core.outbox.OutboxInfoDto.OutboxJobDto;
import org.savapage.core.pdf.PdfCreateInfo;
import org.savapage.core.pdf.PdfCreateRequest;
import org.savapage.core.pdf.PdfPrintCollector;
import org.savapage.core.print.proxy.AbstractProxyPrintReq;
import org.savapage.core.print.proxy.JsonProxyPrintJob;
import org.savapage.core.print.proxy.JsonProxyPrinter;
import org.savapage.core.print.proxy.JsonProxyPrinterOpt;
import org.savapage.core.print.proxy.JsonProxyPrinterOptChoice;
import org.savapage.core.print.proxy.JsonProxyPrinterOptGroup;
import org.savapage.core.print.proxy.ProxyPrintDocReq;
import org.savapage.core.print.proxy.ProxyPrintException;
import org.savapage.core.print.proxy.ProxyPrintInboxReq;
import org.savapage.core.print.proxy.ProxyPrintJobChunk;
import org.savapage.core.print.proxy.ProxyPrinterOptGroupEnum;
import org.savapage.core.print.proxy.TicketJobSheetDto;
import org.savapage.core.services.ProxyPrintService;
import org.savapage.core.services.ServiceContext;
import org.savapage.core.services.helpers.AccountTrxInfoSet;
import org.savapage.core.services.helpers.ExternalSupplierInfo;
import org.savapage.core.services.helpers.InboxSelectScopeEnum;
import org.savapage.core.services.helpers.JobTicketSupplierData;
import org.savapage.core.services.helpers.PageScalingEnum;
import org.savapage.core.services.helpers.PpdExtFileReader;
import org.savapage.core.services.helpers.PrinterAttrLookup;
import org.savapage.core.services.helpers.PrinterSnmpReader;
import org.savapage.core.services.helpers.ProxyPrintCostDto;
import org.savapage.core.services.helpers.ProxyPrintCostParms;
import org.savapage.core.services.helpers.ProxyPrintInboxReqChunker;
import org.savapage.core.services.helpers.ProxyPrintOutboxResult;
import org.savapage.core.services.helpers.SyncPrintJobsResult;
import org.savapage.core.services.helpers.ThirdPartyEnum;
import org.savapage.core.snmp.SnmpClientSession;
import org.savapage.core.snmp.SnmpConnectException;
import org.savapage.core.util.DateUtil;
import org.savapage.core.util.JsonHelper;
import org.savapage.core.util.MediaUtils;
import org.savapage.core.util.Messages;
import org.savapage.ext.papercut.PaperCutAccountAdjustPrint;
import org.savapage.ext.papercut.PaperCutAccountAdjustPrintRefund;
import org.savapage.ext.papercut.PaperCutAccountResolver;
import org.savapage.ext.papercut.PaperCutException;
import org.savapage.ext.papercut.PaperCutServerProxy;
import org.savapage.ext.papercut.job.PaperCutPrintMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Rijk Ravestein
 *
 */
public abstract class AbstractProxyPrintService extends AbstractService
        implements ProxyPrintService {

    /**
     *
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(AbstractProxyPrintService.class);

    /**
     *
     * @author Rijk Ravestein
     *
     */
    private static final class StandardRuleConstraintList {

        /** */
        public static final StandardRuleConstraintList INSTANCE =
                new StandardRuleConstraintList();

        /** */
        private static final String ALIAS_PFX = "sp";

        /** */
        private static final String ALIAS_BOOKLET_PFX = "booklet";

        /** */
        private final List<IppRuleConstraint> rulesBooklet = new ArrayList<>();

        /**
         * Constructor.
         */
        private StandardRuleConstraintList() {
            addBookletConstraints();
        }

        /** */
        private void addBookletConstraints() {

            final ImmutablePair<String, String> pairBooklet =
                    new ImmutablePair<String, String>(
                            IppDictJobTemplateAttr.ORG_SAVAPAGE_ATTR_FINISHINGS_BOOKLET,
                            IppKeyword.ORG_SAVAPAGE_ATTR_FINISHINGS_BOOKLET_NONE);

            final Set<String> setBookletNegate = new HashSet<>();
            setBookletNegate.add(
                    IppDictJobTemplateAttr.ORG_SAVAPAGE_ATTR_FINISHINGS_BOOKLET);

            for (final String nUp : new String[] { IppKeyword.NUMBER_UP_1,
                    IppKeyword.NUMBER_UP_4, IppKeyword.NUMBER_UP_6,
                    IppKeyword.NUMBER_UP_9 }) {

                final IppRuleConstraint rule =
                        new IppRuleConstraint(String.format("%s-%s-%s-%s",
                                ALIAS_PFX, ALIAS_BOOKLET_PFX,
                                IppDictJobTemplateAttr.ATTR_NUMBER_UP, nUp));
                final List<Pair<String, String>> pairs = new ArrayList<>();

                pairs.add(pairBooklet);
                pairs.add(new ImmutablePair<String, String>(
                        IppDictJobTemplateAttr.ATTR_NUMBER_UP, nUp));

                rule.setIppContraints(pairs);
                rule.setIppNegateSet(setBookletNegate);

                this.rulesBooklet.add(rule);
            }

            //
            final IppRuleConstraint rule =
                    new IppRuleConstraint(String.format("%s-%s-%s", ALIAS_PFX,
                            ALIAS_BOOKLET_PFX, IppKeyword.SIDES_ONE_SIDED));

            final List<Pair<String, String>> pairs = new ArrayList<>();

            pairs.add(pairBooklet);
            pairs.add(new ImmutablePair<String, String>(
                    IppDictJobTemplateAttr.ATTR_SIDES,
                    IppKeyword.SIDES_ONE_SIDED));

            rule.setIppContraints(pairs);
            rule.setIppNegateSet(setBookletNegate);
            this.rulesBooklet.add(rule);
        }

        /**
         *
         * @return The pore-defined Booklet constraint rules.
         */
        public List<IppRuleConstraint> getRulesBooklet() {
            return rulesBooklet;
        }

    }

    /**
     *
     */
    private static final PaperCutAccountResolver PAPERCUT_ACCOUNT_RESOLVER =
            new PaperCutAccountResolver() {

                @Override
                public String getUserAccountName() {
                    return PaperCutPrintMonitor.getAccountNameUser();
                }

                @Override
                public String getSharedParentAccountName() {
                    return PaperCutPrintMonitor.getSharedAccountNameParent();
                }

                @Override
                public String getSharedJobsAccountName() {
                    return PaperCutPrintMonitor.getSharedAccountNameJobs();
                }

                @Override
                public String getKlasFromAccountName(final String accountName) {
                    return PaperCutPrintMonitor
                            .extractKlasFromAccountName(accountName);
                }

                @Override
                public String composeSharedSubAccountName(
                        final AccountTypeEnum accountType,
                        final String accountName,
                        final String accountNameParent) {
                    return PaperCutPrintMonitor.createSharedSubAccountName(
                            accountType, accountName, accountNameParent);
                }
            };

    /**
     *
     */
    private final IppNotificationRecipient notificationRecipient;

    /**
     * True or False option.
     */
    // protected static final Integer UI_BOOLEAN = 0;

    /**
     * Dictionary on printer name. NOTE: the key is in UPPER CASE.
     */
    private final ConcurrentMap<String, JsonProxyPrinter> cupsPrinterCache =
            new ConcurrentHashMap<>();

    /**
     * Is this the first time CUPS is contacted? This switch is used for lazy
     * starting the CUPS subscription.
     */
    private final AtomicBoolean isFirstTimeCupsContact =
            new AtomicBoolean(true);

    /**
     *
     */
    private ArrayList<JsonProxyPrinterOptGroup> commonPrinterOptGroups = null;

    /**
     * The {@link URL} of the default (local) CUPS server.
     */
    private URL urlDefaultServer = null;

    /**
     * .
     */
    protected AbstractProxyPrintService() {

        notificationRecipient = new IppNotificationRecipient(this);

        commonPrinterOptGroups = createCommonCupsOptions();

        try {
            urlDefaultServer = new URL(getDefaultCupsUrl());
        } catch (MalformedURLException e) {
            throw new SpException(e);
        }

    }

    protected final URL getUrlDefaultServer() {
        return this.urlDefaultServer;
    }

    /**
     * URL of the CUPS host, like http://localhost:631
     *
     * @return
     */
    private String getDefaultCupsUrl() {
        return "http://" + ConfigManager.getDefaultCupsHost() + ":"
                + ConfigManager.getCupsPort();
    }

    @Override
    public final boolean isConnectedToCups() {
        return ConfigManager
                .getCircuitBreaker(CircuitBreakerEnum.CUPS_LOCAL_IPP_CONNECTION)
                .isCircuitClosed();
    }

    protected boolean hasCommonPrinterOptGroups() {
        return commonPrinterOptGroups != null;
    }

    protected ArrayList<JsonProxyPrinterOptGroup> getCommonPrinterOptGroups() {
        return commonPrinterOptGroups;
    }

    /**
     * Creates common option groups to be added to ALL printers.
     *
     * @return {@code null} when NO common groups are defined.
     */
    protected abstract ArrayList<JsonProxyPrinterOptGroup>
            createCommonCupsOptions();

    @Override
    public void init() {

        /*
         * We have never contacted CUPS at this point.
         */
        this.isFirstTimeCupsContact.set(true);

        /*
         * Make sure the circuit is closed, so a first attempt to use it is
         * honored.
         */
        ConfigManager
                .getCircuitBreaker(CircuitBreakerEnum.CUPS_LOCAL_IPP_CONNECTION)
                .setCircuitState(CircuitStateEnum.CLOSED);

    }

    @Override
    public void exit() throws IppConnectException, IppSyntaxException {
        /*
         * Closes the CUPS services.
         *
         * The subscription to CUPS events is stopped. However, when this method
         * is called as a reaction to a <i>Linux OS shutdown</i>, CUPS probably
         * is stopped before SavaPage. In that case we encounter an exception
         * because the CUPS API fails in {@link #CUPS_BIN}. The exception is
         * catched and logged at INFO level.
         */
        try {
            stopSubscription(null);
        } catch (SpException e) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(e.getMessage());
            }
        }
    }

    @Override
    public final JsonPrinterDetail
            getPrinterDetailCopy(final String printerName) {
        return this.getPrinterDetailCopy(printerName, null, false, true);
    }

    @Override
    public final JsonPrinterDetail getPrinterDetailUserCopy(final Locale locale,
            final String printerName, final boolean isExtended) {
        return this.getPrinterDetailCopy(printerName, locale, true, isExtended);
    }

    @Override
    public final JsonProxyPrinterOpt getPrinterOptUserCopy(
            final String printerName, final String ippKeyword,
            final Locale locale) {

        final JsonProxyPrinter proxyPrinter = getCachedPrinter(printerName);

        if (proxyPrinter != null) {
            for (JsonProxyPrinterOptGroup group : proxyPrinter.getGroups()) {
                for (JsonProxyPrinterOpt option : group.getOptions()) {
                    if (option.getKeyword().equals(ippKeyword)) {
                        final JsonProxyPrinterOpt optionCopy = option.copy();
                        localizePrinterOpt(locale, optionCopy);
                        return optionCopy;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gets a copy of the {@link JsonProxyPrinter} from the printer cache.
     *
     * @param printerName
     *            The printer name.
     * @return {@code null} when the printer is no longer part of the cache.
     */
    private JsonProxyPrinter getJsonProxyPrinterCopy(final String printerName) {

        final JsonProxyPrinter printerCopy;

        final JsonProxyPrinter cupsPrinter = getCachedCupsPrinter(printerName);

        if (cupsPrinter != null) {
            printerCopy = cupsPrinter.copy();
        } else {
            printerCopy = null;
        }

        return printerCopy;
    }

    /**
     * Gets a copy of the JsonPrinter from the printer cache. If this is a User
     * copy, the printer options are filtered according to user settings and
     * permissions.
     * <p>
     * <b>Note</b>: a copy is returned so the caller can do his private
     * {@link #localize(Locale, JsonPrinterDetail)}.
     * </p>
     *
     * @param printerName
     *            The printer name.
     * @param locale
     *            The user {@link Locale}.
     * @param isUserCopy
     *            {@code true} if this is a copy for a user.
     * @param isExtended
     *            {@code true} if this is an extended copy.
     * @return {@code null} when the printer is no longer part of the cache.
     */
    private JsonPrinterDetail getPrinterDetailCopy(final String printerName,
            final Locale locale, final boolean isUserCopy,
            final boolean isExtended) {

        final JsonPrinterDetail printerCopy;

        final JsonProxyPrinter cupsPrinter = getCachedCupsPrinter(printerName);

        if (cupsPrinter != null) {

            final JsonPrinterDetail printer = new JsonPrinterDetail();

            printer.setDbKey(cupsPrinter.getDbPrinter().getId());
            printer.setName(cupsPrinter.getName());
            printer.setLocation(cupsPrinter.getDbPrinter().getLocation());
            printer.setAlias(cupsPrinter.getDbPrinter().getDisplayName());
            printer.setGroups(cupsPrinter.getGroups());
            printer.setPrinterUri(cupsPrinter.getPrinterUri());
            printer.setJobTicket(cupsPrinter.getJobTicket());
            printer.setPrintScalingExt(cupsPrinter.isPrintScalingExt());

            /*
             * Create copy, localize and prune.
             */
            printerCopy = printer.copy();

            if (locale != null) {
                this.localize(locale, printerCopy);
            }

            if (isUserCopy) {

                this.setPrinterMediaSourcesForUser(locale,
                        cupsPrinter.getDbPrinter(), printerCopy);

                removeOptGroup(printerCopy,
                        ProxyPrinterOptGroupEnum.REFERENCE_ONLY);

                pruneUserPrinterIppOptions(cupsPrinter, printerCopy);
            }

            if (!isExtended) {
                restrictUserPrinterIppOptions(printerCopy);
            }

        } else {
            printerCopy = null;
        }

        return printerCopy;
    }

    /**
     * Restricts printer IPP options by pruning extended choices.
     *
     * @param userPrinter
     *            The printer to restrict.
     */
    private static void
            restrictUserPrinterIppOptions(final JsonPrinterDetail userPrinter) {

        for (final JsonProxyPrinterOptGroup optGroup : userPrinter
                .getGroups()) {
            for (final JsonProxyPrinterOpt opt : optGroup.getOptions()) {
                final Iterator<JsonProxyPrinterOptChoice> iter =
                        opt.getChoices().iterator();
                while (iter.hasNext()) {
                    if (iter.next().isExtended()) {
                        iter.remove();
                    }
                }
            }
        }
    }

    /**
     * Prunes printer IPP options that user is not allowed to set.
     *
     * @param cupsPrinter
     *            The cupsPrinter.
     * @param userPrinter
     *            The printer to prune.
     */
    private static void pruneUserPrinterIppOptions(
            final JsonProxyPrinter cupsPrinter,
            final JsonPrinterDetail userPrinter) {

        final Map<String, JsonProxyPrinterOpt> cachedOptionLookup =
                cupsPrinter.getOptionsLookup();

        for (final String kw : IppDictJobTemplateAttr.ATTR_SET_UI_PPDE_ONLY) {

            final JsonProxyPrinterOpt opt = cachedOptionLookup.get(kw);

            if (opt == null || opt.isPpdExt()) {
                continue;
            }

            for (final JsonProxyPrinterOptGroup optGroup : userPrinter
                    .getGroups()) {

                final Iterator<JsonProxyPrinterOpt> iter =
                        optGroup.getOptions().iterator();

                while (iter.hasNext()) {
                    if (iter.next().getKeyword().equals(kw)) {
                        iter.remove();
                        break;
                    }
                }
            }
        }
    }

    /**
     * Removes an {@link ProxyPrinterOptGroupEnum} from a
     * {@link JsonPrinterDetail} definition.
     *
     * @param printerDetail
     *            The printer definition
     * @param optGroup
     *            The option group.
     */
    private static void removeOptGroup(final JsonPrinterDetail printerDetail,
            final ProxyPrinterOptGroupEnum optGroup) {

        final Iterator<JsonProxyPrinterOptGroup> iter =
                printerDetail.getGroups().iterator();

        while (iter.hasNext()) {
            final JsonProxyPrinterOptGroup group = iter.next();
            if (group.getGroupId() == optGroup) {
                iter.remove();
                break;
            }
        }
    }

    /**
     * Prunes printer media-source options according to user settings and
     * permissions and sets the
     * {@link JsonPrinterDetail#setMediaSources(ArrayList)} .
     *
     * @param locale
     *            The {@link Locale}.
     * @param printer
     *            The {@link Printer} from the cache.
     * @param printerDetail
     *            The {@link JsonPrinterDetail} to prune.
     */
    private void setPrinterMediaSourcesForUser(final Locale locale,
            final Printer printer, final JsonPrinterDetail printerDetail) {

        final ArrayList<IppMediaSourceMappingDto> mediaSources =
                new ArrayList<>();

        printerDetail.setMediaSources(mediaSources);

        /*
         * Find the media-source option and choices.
         */
        JsonProxyPrinterOpt mediaSourceOption = null;

        List<JsonProxyPrinterOptChoice> mediaSourceChoices = null;

        for (final JsonProxyPrinterOptGroup optGroup : printerDetail
                .getGroups()) {

            for (final JsonProxyPrinterOpt option : optGroup.getOptions()) {

                if (option.getKeyword()
                        .equals(IppDictJobTemplateAttr.ATTR_MEDIA_SOURCE)) {

                    mediaSourceOption = option;
                    mediaSourceChoices = option.getChoices();

                    break;
                }
            }

            if (mediaSourceChoices != null) {
                break;
            }
        }

        if (mediaSourceChoices == null) {
            return;
        }

        /*
         * We need a JPA "attached" printer instance to create the lookup.
         */

        final Printer dbPrinter = printerDAO().findById(printer.getId());

        final PrinterAttrLookup lookup = new PrinterAttrLookup(dbPrinter);

        final Iterator<JsonProxyPrinterOptChoice> iterMediaSourceChoice =
                mediaSourceChoices.iterator();

        while (iterMediaSourceChoice.hasNext()) {

            final JsonProxyPrinterOptChoice optChoice =
                    iterMediaSourceChoice.next();

            final PrinterDao.MediaSourceAttr mediaSourceAttr =
                    new PrinterDao.MediaSourceAttr(optChoice.getChoice());

            final String json = lookup.get(mediaSourceAttr.getKey());

            boolean removeMediaSourceChoice = true;

            if (json != null) {

                try {

                    final IppMediaSourceCostDto dto =
                            IppMediaSourceCostDto.create(json);

                    if (dto.getActive()) {

                        optChoice.setUiText(dto.getDisplay());

                        if (dto.getMedia() != null) {

                            final IppMediaSourceMappingDto mediaSource =
                                    new IppMediaSourceMappingDto();

                            mediaSource.setSource(dto.getSource());
                            mediaSource.setMedia(dto.getMedia().getMedia());

                            mediaSources.add(mediaSource);
                        }

                        removeMediaSourceChoice = false;
                    }

                } catch (IOException e) {
                    // be forgiving
                    LOGGER.error(e.getMessage());
                }
            }

            if (removeMediaSourceChoice) {
                iterMediaSourceChoice.remove();
            }
        }

        final JsonProxyPrinterOptChoice choiceAuto =
                new JsonProxyPrinterOptChoice();

        choiceAuto.setChoice(IppKeyword.MEDIA_SOURCE_AUTO);
        this.localizePrinterOptChoice(locale,
                IppDictJobTemplateAttr.ATTR_MEDIA_SOURCE, choiceAuto);
        mediaSourceChoices.add(0, choiceAuto);

        mediaSourceOption.setDefchoice(IppKeyword.MEDIA_SOURCE_AUTO);
    }

    @Override
    public final boolean isJobTicketPrinterPresent() {
        for (final JsonProxyPrinter printer : this.cupsPrinterCache.values()) {
            if (BooleanUtils.isTrue(printer.getJobTicket())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public final boolean areJobTicketPrintersOnly(final Device terminal,
            final String userName)
            throws IppConnectException, IppSyntaxException {

        boolean isTicketPrinterFound = false;

        for (final JsonPrinter printer : this
                .getUserPrinterList(terminal, userName).getList()) {

            if (BooleanUtils.isFalse(printer.getJobTicket())) {
                return false;
            }

            isTicketPrinterFound = true;
        }
        return isTicketPrinterFound;
    }

    @Override
    public final JsonPrinterList getUserPrinterList(final Device terminal,
            final String userName)
            throws IppConnectException, IppSyntaxException {

        lazyInitPrinterCache();

        final User user = userDAO().findActiveUserByUserId(userName);

        final boolean hasAccessToJobTicket = accessControlService()
                .hasAccess(user, ACLRoleEnum.JOB_TICKET_CREATOR);

        final boolean hasAccessToProxyPrinter = accessControlService()
                .hasAccess(user, ACLRoleEnum.PRINT_CREATOR);

        /*
         * The collected valid printers.
         */
        final ArrayList<JsonPrinter> collectedPrinters = new ArrayList<>();

        /*
         * Walker variables.
         */
        final MutableBoolean terminalSecuredWlk = new MutableBoolean();
        final MutableBoolean readerSecuredWlk = new MutableBoolean();

        final Map<String, Device> terminalDevicesWlk = new HashMap<>();
        final Map<String, Device> readerDevicesWlk = new HashMap<>();

        /*
         * Traverse the printer cache.
         */
        for (final JsonProxyPrinter printer : this.cupsPrinterCache.values()) {

            final boolean isJobTicketPrinter =
                    BooleanUtils.isTrue(printer.getJobTicket());

            if (isJobTicketPrinter && !hasAccessToJobTicket) {
                continue;
            }

            if (!isJobTicketPrinter && !hasAccessToProxyPrinter) {
                continue;
            }

            /*
             * Bring printer back into JPA session so @OneTo* relations can be
             * resolved.
             */
            final Printer dbPrinterWlk =
                    printerDAO().findById(printer.getDbPrinter().getId());

            final PrinterAttrLookup attrLookup =
                    new PrinterAttrLookup(dbPrinterWlk);

            /*
             * Skip internal printer.
             */
            if (printerAttrDAO().isInternalPrinter(attrLookup)) {
                continue;
            }
            /*
             * Skip printer that is not configured.
             */
            if (!this.isPrinterConfigured(printer, attrLookup)) {
                continue;
            }

            if (isPrinterGrantedOnTerminal(terminal, userName, dbPrinterWlk,
                    terminalSecuredWlk, readerSecuredWlk, terminalDevicesWlk,
                    readerDevicesWlk)) {

                if (readerSecuredWlk.getValue()) {
                    /*
                     * Card Reader secured: one Printer entry for each reader.
                     */
                    for (final Entry<String, Device> entry : readerDevicesWlk
                            .entrySet()) {

                        final JsonPrinter basicPrinter = new JsonPrinter();

                        collectedPrinters.add(basicPrinter);

                        basicPrinter.setDbKey(dbPrinterWlk.getId());
                        basicPrinter.setName(printer.getName());
                        basicPrinter.setLocation(dbPrinterWlk.getLocation());
                        basicPrinter.setAlias(dbPrinterWlk.getDisplayName());
                        basicPrinter.setTerminalSecured(
                                terminalSecuredWlk.getValue());
                        basicPrinter
                                .setReaderSecured(readerSecuredWlk.getValue());

                        basicPrinter.setAuthMode(
                                deviceService().getProxyPrintAuthMode(
                                        entry.getValue().getId()));

                        basicPrinter.setReaderName(entry.getKey());
                    }

                } else {
                    /*
                     * Just Terminal secured: one Printer entry.
                     */
                    final JsonPrinter basicPrinter = new JsonPrinter();

                    collectedPrinters.add(basicPrinter);

                    basicPrinter.setDbKey(dbPrinterWlk.getId());
                    basicPrinter.setName(printer.getName());
                    basicPrinter.setAlias(dbPrinterWlk.getDisplayName());
                    basicPrinter.setLocation(dbPrinterWlk.getLocation());
                    basicPrinter
                            .setTerminalSecured(terminalSecuredWlk.getValue());
                    basicPrinter.setReaderSecured(readerSecuredWlk.getValue());

                    basicPrinter
                            .setJobTicket(Boolean.valueOf(isJobTicketPrinter));
                }

            }
        }

        Collections.sort(collectedPrinters, new Comparator<JsonPrinter>() {

            @Override
            public int compare(final JsonPrinter o1, final JsonPrinter o2) {

                return o1.getAlias().compareToIgnoreCase(o2.getAlias());
            }
        });

        final JsonPrinterList printerList = new JsonPrinterList();
        printerList.setList(collectedPrinters);
        return printerList;
    }

    @Override
    public final IppNotificationRecipient notificationRecipient() {
        return notificationRecipient;
    }

    @Override
    public final boolean isPrinterConfigured(final JsonProxyPrinter cupsPrinter,
            final PrinterAttrLookup lookup) {

        final ArrayList<JsonProxyPrinterOptGroup> cupsPrinterGroups =
                cupsPrinter.getGroups();

        if (cupsPrinterGroups == null) {
            return false;
        }

        /*
         * Any media sources defined in CUPS printer?
         */
        List<JsonProxyPrinterOptChoice> mediaSourceChoices = null;

        for (final JsonProxyPrinterOptGroup optGroup : cupsPrinterGroups) {

            for (final JsonProxyPrinterOpt option : optGroup.getOptions()) {

                if (option.getKeyword()
                        .equals(IppDictJobTemplateAttr.ATTR_MEDIA_SOURCE)) {
                    mediaSourceChoices = option.getChoices();
                    break;
                }
            }

            if (mediaSourceChoices != null) {
                break;
            }
        }

        /*
         * There MUST be media source(s) defines in CUPS printer.
         */
        if (mediaSourceChoices == null) {
            return false;
        }

        /*
         * Count the number of configured media sources.
         */
        int nMediaSources = 0;

        for (final JsonProxyPrinterOptChoice optChoice : mediaSourceChoices) {

            final PrinterDao.MediaSourceAttr mediaSourceAttr =
                    new PrinterDao.MediaSourceAttr(optChoice.getChoice());

            final String json = lookup.get(mediaSourceAttr.getKey());

            if (json != null) {

                try {

                    final IppMediaSourceCostDto dto =
                            IppMediaSourceCostDto.create(json);

                    if (dto.getActive() && dto.getMedia() != null) {
                        nMediaSources++;
                    }

                } catch (IOException e) {
                    // Be forgiving when old JSON format.
                    LOGGER.debug(e.getMessage());
                }
            }
        }

        return nMediaSources > 0;
    }

    @Override
    public final boolean isColorPrinter(final String printerName) {
        return getCachedCupsPrinter(printerName).getColorDevice()
                .booleanValue();
    }

    @Override
    public final boolean isDuplexPrinter(final String printerName) {
        return getCachedCupsPrinter(printerName).getDuplexDevice()
                .booleanValue();
    }

    @Override
    public final boolean isCupsPrinterDetails(final String printerName) {
        return getCachedCupsPrinter(printerName) != null;
    }

    @Override
    public final boolean hasMediaSourceManual(final String printerName) {
        final Boolean manual =
                getCachedCupsPrinter(printerName).getManualMediaSource();
        if (manual == null) {
            return false;
        }
        return manual.booleanValue();
    }

    @Override
    public final boolean hasMediaSourceAuto(final String printerName) {
        final Boolean auto =
                getCachedCupsPrinter(printerName).getAutoMediaSource();
        if (auto == null) {
            return false;
        }
        return auto.booleanValue();
    }

    /**
     * Convenience method to make sure the printer name is converted to format
     * used in database, i.e. UPPER CASE.
     *
     * @param printerName
     *            The unique printer name.
     * @return The {@link JsonProxyPrinter}.
     */
    private JsonProxyPrinter getCachedCupsPrinter(final String printerName) {
        return this.cupsPrinterCache
                .get(ProxyPrinterName.getDaoName(printerName));
    }

    @Override
    public final JsonProxyPrinter getCachedPrinter(final String printerName) {
        return getCachedCupsPrinter(printerName);
    }

    @Override
    public final void updateCachedPrinter(final Printer dbPrinter) {

        final JsonProxyPrinter proxyPrinter =
                this.cupsPrinterCache.get(dbPrinter.getPrinterName());

        if (proxyPrinter != null) {
            this.assignDbPrinter(proxyPrinter, dbPrinter);
        }
    }

    /**
     * Assigns the database {@link Printer} to the {@link JsonProxyPrinter}, and
     * overrules IPP option defaults specified as {@link PrinterAttr}.
     *
     * @param proxyPrinter
     *            The {@link JsonProxyPrinter}.
     * @param dbPrinter
     *            The database {@link Printer}.
     */
    private void assignDbPrinter(final JsonProxyPrinter proxyPrinter,
            final Printer dbPrinter) {

        proxyPrinter.setDbPrinter(dbPrinter);

        proxyPrinter
                .setJobTicket(printerService().isJobTicketPrinter(dbPrinter));

        final String ppdfExtFile = printerService().getAttributeValue(dbPrinter,
                PrinterAttrEnum.CUSTOM_PPD_EXT_FILE);

        if (StringUtils.isNotBlank(ppdfExtFile)) {

            final File filePpdExt = Paths.get(
                    ConfigManager.getServerCustomCupsHome().getAbsolutePath(),
                    ppdfExtFile).toFile();

            if (filePpdExt.exists()) {
                try {
                    PpdExtFileReader.injectPpdExt(proxyPrinter, filePpdExt);
                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
                }

            } else {
                LOGGER.error(String.format("Printer %s: %s does not exist.",
                        dbPrinter.getPrinterName(),
                        filePpdExt.getAbsolutePath()));
            }
        }

        final String colorModeDefault =
                printerService().getPrintColorModeDefault(dbPrinter);

        if (colorModeDefault != null) {

            final Map<String, JsonProxyPrinterOpt> optionLookup =
                    proxyPrinter.getOptionsLookup();

            final JsonProxyPrinterOpt colorModeOpt = optionLookup
                    .get(IppDictJobTemplateAttr.ATTR_PRINT_COLOR_MODE);

            if (colorModeOpt != null) {
                colorModeOpt.setDefchoice(colorModeDefault);
            }
        }
    }

    /**
     * Checks if a Printer is granted for a User on a Terminal and if its access
     * is Terminal Secured or Reader Secured (Print Authentication required via
     * Network Card Reader).
     *
     * @param terminal
     *            The Terminal Device (can be {@code null}).
     * @param userName
     *            The unique name of the requesting user.
     * @param printer
     *            The Printer.
     * @param terminalSecured
     *            Return value which holds {@code true} if Printer is secured
     *            via {@link Device.DeviceTypeEnum#TERMINAL}.
     * @param readerSecured
     *            Return value which holds {@code true} if Printer is secured
     *            via {@link Device.DeviceTypeEnum#CARD_READER}.
     * @param terminalDevices
     *            The Terminal Devices responsible for printer being secured.
     *            The map is cleared before collecting the members.
     * @param readerDevices
     *            The Reader Devices responsible for printer being secured. The
     *            map is cleared before collecting the members.
     * @return {@code true} is Printer is secured (either via Reader or
     *         Terminal).
     */
    private boolean isPrinterGrantedOnTerminal(final Device terminal,
            final String userName, final Printer printer,
            final MutableBoolean terminalSecured,
            final MutableBoolean readerSecured,
            final Map<String, Device> terminalDevices,
            final Map<String, Device> readerDevices) {

        /*
         * INVARIANT: we MUST be dealing with a terminal device.
         */
        if (terminal != null && !deviceDAO().isTerminal(terminal)) {
            throw new SpException("Device [" + terminal.getDisplayName()
                    + "] is not of type [" + DeviceTypeEnum.TERMINAL + "]");
        }

        /*
         * Reset return values.
         */
        terminalSecured.setValue(false);
        readerSecured.setValue(false);
        terminalDevices.clear();
        readerDevices.clear();

        /*
         * Evaluate availability.
         *
         * (1) disabled or deleted?
         */
        if (printer.getDisabled() || printer.getDeleted()) {
            return false;
        }

        /*
         * (2) Check dedicated printer(s) for device.
         */
        boolean isGlobalNonSecure = false;

        if (terminal != null && BooleanUtils.isNotTrue(terminal.getDisabled())
                && deviceDAO().hasPrinterRestriction(terminal)) {

            terminalSecured.setValue(printerService().checkDeviceSecurity(
                    printer, DeviceTypeEnum.TERMINAL, terminal));

        } else {

            if (!printerService().checkPrinterSecurity(printer, terminalSecured,
                    readerSecured, terminalDevices, readerDevices)) {

                isGlobalNonSecure = ConfigManager.instance()
                        .isNonSecureProxyPrinter(printer);
            }
        }

        boolean isAvailable = isGlobalNonSecure || terminalSecured.getValue()
                || readerSecured.getValue();

        /*
         * (3) user group access control?
         */
        if (isAvailable) {

            final User user = userDAO().findActiveUserByUserId(userName);

            isAvailable = user != null
                    && printerService().isPrinterAccessGranted(printer, user);
        }
        return isAvailable;
    }

    /**
     * Retrieves printer details from CUPS.
     *
     * @return A list of {@link JsonProxyPrinter} objects.
     * @throws IppConnectException
     * @throws URISyntaxException
     * @throws MalformedURLException
     */
    protected abstract List<JsonProxyPrinter> retrieveCupsPrinters()
            throws IppConnectException, URISyntaxException,
            MalformedURLException;

    /**
     * Retrieves the printer details. Note that the details are a subset of all
     * the IPP printer options.
     *
     * @param printerName
     *            The CUPS printer name.
     * @param printerUri
     *            The URI of the printer.
     * @return The {@link JsonProxyPrinter} or {@code null} when not found.
     * @throws IppConnectException
     *             When IPP connection failed.
     */
    abstract JsonProxyPrinter retrieveCupsPrinterDetails(String printerName,
            URI printerUri) throws IppConnectException;

    /**
     * Retrieves data for a list of print jobs ids for a printer.
     *
     * @param printerName
     *            The identifying name of the printer.
     * @param jobIds
     *            The job ids.
     * @return A list of print job objects.
     * @throws IppConnectException
     *             When a connection error occurs.
     */
    protected abstract List<JsonProxyPrintJob>
            retrievePrintJobs(String printerName, List<Integer> jobIds)
                    throws IppConnectException;

    @Override
    public final JsonProxyPrintJob retrievePrintJob(final String printerName,
            final Integer jobId) throws IppConnectException {

        JsonProxyPrintJob printJob = null;

        final List<Integer> jobIds = new ArrayList<>();
        jobIds.add(jobId);

        final List<JsonProxyPrintJob> printJobList =
                retrievePrintJobs(printerName, jobIds);

        if (!printJobList.isEmpty()) {
            printJob = printJobList.get(0);
        }
        return printJob;
    }

    @Override
    public final SyncPrintJobsResult syncPrintJobs(
            final DaoBatchCommitter batchCommitter) throws IppConnectException {

        SpInfo.instance().log(String.format("| Syncing CUPS jobs ..."));

        /*
         * Constants
         */
        final int nChunkMax = 50;

        /*
         * Init batch.
         */
        final long startTime = System.currentTimeMillis();

        final List<PrintOut> printOutList = printOutDAO().findActiveCupsJobs();

        SpInfo.instance()
                .log(String.format("|   %s : %d Active PrintOut jobs.",
                        DateUtil.formatDuration(
                                System.currentTimeMillis() - startTime),
                        printOutList.size()));
        //
        final Map<Integer, PrintOut> lookupPrintOut = new HashMap<>();

        for (final PrintOut printOut : printOutList) {
            lookupPrintOut.put(printOut.getCupsJobId(), printOut);
        }

        // The number of active PrintOut jobs.
        final int jobsActive = printOutList.size();

        // The number of PrintOut jobs that were updated with a new CUPS state.
        int jobsUpdated = 0;

        // The number of jobs that were not found in CUPS.
        int jobsNotFound = 0;

        //
        final Set<Integer> cupsJobsFound = new HashSet<>();

        //
        int i = 0;
        int iChunk = 0;
        PrintOut printOut = null;
        String printer = null;
        String printerPrv = null;
        List<Integer> jobIds = null;

        /*
         * Initial read.
         */
        if (i < printOutList.size()) {
            printOut = printOutList.get(i);
            printer = printOut.getPrinter().getPrinterName();
        }

        /*
         * Processing loop.
         */
        while (printOut != null) {

            printerPrv = printer;

            if (iChunk == 0) {
                jobIds = new ArrayList<>();
            }

            jobIds.add(printOut.getCupsJobId());

            /*
             * Read next.
             */
            printOut = null;
            i++;
            iChunk++;

            if (i < printOutList.size()) {
                printOut = printOutList.get(i);
                printer = printOut.getPrinter().getPrinterName();
            }

            /*
             * EOF, new printer or chunk filled to the max.
             */
            if (printOut == null || !printer.equals(printerPrv)
                    || iChunk == nChunkMax) {

                final List<JsonProxyPrintJob> cupsJobs =
                        retrievePrintJobs(printerPrv, jobIds);

                jobsNotFound += (iChunk - cupsJobs.size());

                for (final JsonProxyPrintJob cupsJob : cupsJobs) {

                    final Integer cupsJobId = cupsJob.getJobId();

                    cupsJobsFound.add(cupsJobId);

                    /*
                     * Since the list of retrieved jobs does NOT contain jobs
                     * that were NOT found, we use the lookup map.
                     */
                    final PrintOut printOutWlk = lookupPrintOut.get(cupsJobId);

                    /*
                     * It turns out that when using IPP (HTTP) there might be a
                     * difference, so we do NOT check on time differences.
                     */
                    boolean checkCreationTime = false;

                    if (checkCreationTime && !printOutWlk.getCupsCreationTime()
                            .equals(cupsJob.getCreationTime())) {

                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("MISMATCH printer [" + printerPrv
                                    + "] job [" + cupsJobId + "] state ["
                                    + cupsJob.getJobState()
                                    + "] created in CUPS ["
                                    + cupsJob.getCreationTime() + "] in log ["
                                    + printOutWlk.getCupsCreationTime() + "]");
                        }

                    } else if (!printOutWlk.getCupsJobState()
                            .equals(cupsJob.getJobState())) {
                        /*
                         * State change.
                         */
                        printOutWlk.setCupsJobState(
                                cupsJob.getIppJobState().asInteger());
                        printOutWlk.setCupsCompletedTime(
                                cupsJob.getCompletedTime());

                        printOutDAO().update(printOutWlk);
                        jobsUpdated++;
                        batchCommitter.increment();

                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("printer [" + printerPrv + "] job ["
                                    + cupsJobId + "] state ["
                                    + cupsJob.getJobState() + "] completed ["
                                    + cupsJob.getCompletedTime() + "]");
                        }
                    }

                }

                iChunk = 0;
            }
        }

        /*
         * Handle the jobs not found.
         */
        if (jobsNotFound > 0) {

            final Iterator<PrintOut> iter = printOutList.iterator();

            while (iter.hasNext()) {

                final PrintOut printOutWlk = iter.next();

                if (cupsJobsFound.contains(printOutWlk.getCupsJobId())) {
                    continue;
                }
                // Set completed time to null, so we know we interpreted the
                // status as completed.
                printOutWlk.setCupsCompletedTime(null);
                printOutWlk.setCupsJobState(
                        IppJobStateEnum.IPP_JOB_COMPLETED.asInteger());

                printOutDAO().update(printOutWlk);
                batchCommitter.increment();
            }
        }

        //
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Syncing [" + jobsActive + "] active PrintOut jobs "
                    + "with CUPS : updated [" + jobsUpdated + "], not found ["
                    + jobsNotFound + "]");
        }

        if (jobsActive > 0) {
            SpInfo.instance().log(String.format("|      : %d PrintOut updated.",
                    jobsUpdated));
            SpInfo.instance().log(String.format(
                    "|      : %d PrintOut not found in CUPS.", jobsNotFound));
        }

        return new SyncPrintJobsResult(jobsActive, jobsUpdated, jobsNotFound);
    }

    /**
     * Calculates the number of Environmental Sheets Units (ESU) from number of
     * printed sheets and media size.
     *
     * <ul>
     * <li>1 ESU == 1/100 of an A4 sheet.</li>
     * <li>1 Sheet Unit (SU) == 1 A4 sheet.</li>
     * </ul>
     *
     * <p>
     * NOTE: As environmental impact is concerned, {@link MediaSizeName#ISO_A4}
     * and {@link MediaSizeName#NA_LETTER} are equivalent, and are therefore
     * counted as 100 ESUs.
     * </p>
     *
     * @param numberOfSheets
     *            The number of physical sheets.
     * @param mediaWidth
     *            Media width in mm.
     * @param mediaHeight
     *            Media height in mm.
     * @return The number of ESU.
     */
    protected final long calcNumberOfEsu(final int numberOfSheets,
            final int mediaWidth, final int mediaHeight) {

        final int[] sizeA4 =
                MediaUtils.getMediaWidthHeight(MediaSizeName.ISO_A4);
        final int[] sizeLetter =
                MediaUtils.getMediaWidthHeight(MediaSizeName.NA_LETTER);

        for (int[] size : new int[][] { sizeA4, sizeLetter }) {
            if (size[0] == mediaWidth && size[1] == mediaHeight) {
                return numberOfSheets * 100;
            }
        }
        /*
         * The full double.
         */
        final double nSheets =
                (double) (numberOfSheets * mediaWidth * mediaHeight)
                        / (sizeA4[0] * sizeA4[1]);
        /*
         * Round on 2 decimals by multiplying by 100.
         */
        return Math.round(nSheets * 100);
    }

    /**
     * Gets the CUPS {@code notify-recipient-uri}.
     * <p>
     * Example: {@code savapage:localhost:8631}
     * </p>
     *
     * @return
     */
    private String getSubscrNotifyRecipientUri() {
        return ConfigManager.getCupsNotifier() + ":localhost:"
                + ConfigManager.getServerPort();
    }

    /**
     *
     * @return
     */
    private String getSubscrNotifyLeaseSeconds() {
        return ConfigManager.instance()
                .getConfigValue(Key.CUPS_IPP_SUBSCR_NOTIFY_LEASE_DURATION);
    }

    /**
     *
     * @param requestingUser
     * @return
     */
    private String getSubscrRequestingUser(final String requestingUser) {
        if (requestingUser == null) {
            return ConfigManager.getProcessUserName();
        }
        return requestingUser;
    }

    @Override
    public final void startSubscription(final String requestingUser)
            throws IppConnectException, IppSyntaxException {
        startSubscription(getSubscrRequestingUser(requestingUser),
                getSubscrNotifyLeaseSeconds(), getSubscrNotifyRecipientUri());
    }

    @Override
    public final void stopSubscription(final String requestingUser)
            throws IppConnectException, IppSyntaxException {
        stopSubscription(getSubscrRequestingUser(requestingUser),
                getSubscrNotifyRecipientUri());
    }

    /**
     *
     * @param requestingUser
     * @param recipientUri
     * @throws IppConnectException
     * @throws IppSyntaxException
     */
    abstract protected void stopSubscription(final String requestingUser,
            String recipientUri) throws IppConnectException, IppSyntaxException;

    /**
     *
     * @param requestingUser
     * @param leaseSeconds
     * @param recipientUri
     * @throws IppConnectException
     * @throws IppSyntaxException
     */
    abstract protected void startSubscription(final String requestingUser,
            String leaseSeconds, String recipientUri)
            throws IppConnectException, IppSyntaxException;

    @Override
    public final void lazyInitPrinterCache()
            throws IppConnectException, IppSyntaxException {

        if (this.isFirstTimeCupsContact.get()) {
            initPrinterCache(true);
        }
    }

    @Override
    public final void initPrinterCache()
            throws IppConnectException, IppSyntaxException {
        initPrinterCache(false);
    }

    /**
     * Initializes the CUPS printer cache (clearing any existing one).
     * <p>
     * <b>Important</b>: This method performs a commit, and re-opens any
     * transaction this was pending at the start of this method.
     * </p>
     *
     * @param isLazyInit
     *            {@code true} if this is a lazy init update.
     * @throws IppConnectException
     *             When a connection error occurs.
     * @throws IppSyntaxException
     *             When a syntax error.
     */
    private void initPrinterCache(final boolean isLazyInit)
            throws IppConnectException, IppSyntaxException {

        final DaoContext ctx = ServiceContext.getDaoContext();
        final boolean currentTrxActive = ctx.isTransactionActive();

        if (!currentTrxActive) {
            ctx.beginTransaction();
        }

        try {
            updatePrinterCache(isLazyInit);
            ctx.commit();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new IppConnectException(e);
        } finally {
            if (ctx.isTransactionActive()) {
                ctx.rollback();
            }
            if (currentTrxActive) {
                ctx.beginTransaction();
            }
        }
    }

    @Override
    public final boolean isPrinterCacheAvailable() {
        return !this.isFirstTimeCupsContact.get();
    }

    /**
     * Updates (or initializes) the printer cache with retrieved printer
     * information from CUPS.
     * <p>
     * <i>When this is a first-time connection to CUPS, CUPS event subscription
     * and a one-shot CUPS job sync is started.</i>
     * </p>
     * <ul>
     * <li>New printers (printer name as key) are added to the cache AND to the
     * database.</li>
     * <li>Removed CUPS printers are deleted from the cache.</li>
     * <li>Printers with same name but changed signature (PPD name and version)
     * are update in the cache.</li>
     * <li>When a CUPS printer is identical to a logical deleted ProxyPrinter,
     * the logical delete mark will be removed so the ProxyPrinter will be
     * re-activated.</li>
     * </ul>
     *
     * @param isLazyInit
     *            {@code true} if this is a lazy init update.
     * @throws URISyntaxException
     *             When URI syntax error.
     * @throws MalformedURLException
     *             When URL malformed.
     * @throws IppConnectException
     *             When a connection error occurs.
     * @throws IppSyntaxException
     *             When a syntax error.
     */
    private synchronized void updatePrinterCache(final boolean isLazyInit)
            throws MalformedURLException, IppConnectException,
            URISyntaxException, IppSyntaxException {

        final boolean firstTimeCupsContact =
                this.isFirstTimeCupsContact.getAndSet(false);

        // Concurrent lazy init try.
        if (isLazyInit && !firstTimeCupsContact) {
            return;
        }

        final boolean connectedToCupsPrv =
                !firstTimeCupsContact && isConnectedToCups();

        /*
         * If method below succeeds, the CUPS circuit breaker will be closed and
         * isConnectedToCups() will return true.
         */
        final List<JsonProxyPrinter> cupsPrinters = this.retrieveCupsPrinters();

        /*
         * We have a first-time connection to CUPS, so start the event
         * subscription and sync with CUPS jobs.
         */
        if (!connectedToCupsPrv && isConnectedToCups()) {

            startSubscription(null);

            LOGGER.trace("CUPS job synchronization started");

            SpJobScheduler.instance()
                    .scheduleOneShotJob(SpJobType.CUPS_SYNC_PRINT_JOBS, 1L);
        }

        /*
         * Mark all currently cached printers as 'not present'.
         */
        final Map<String, Boolean> printersPresent = new HashMap<>();

        for (final String key : this.cupsPrinterCache.keySet()) {
            printersPresent.put(key, Boolean.FALSE);
        }

        /*
         * Traverse the CUPS printers.
         */
        final Date now = new Date();

        final boolean remoteCupsEnabled = ConfigManager.instance()
                .isConfigValue(Key.CUPS_IPP_REMOTE_ENABLED);

        for (final JsonProxyPrinter cupsPrinter : cupsPrinters) {

            /*
             * Access remote CUPS for remote printer?
             */
            if (!remoteCupsEnabled
                    && !isLocalPrinter(cupsPrinter.getPrinterUri())) {
                continue;
            }

            final String cupsPrinterKey = cupsPrinter.getName();

            /*
             * Mark as present.
             */
            printersPresent.put(cupsPrinterKey, Boolean.TRUE);

            /*
             * Get the cached replicate.
             */
            JsonProxyPrinter cachedCupsPrinter =
                    this.cupsPrinterCache.get(cupsPrinterKey);

            /*
             * Is this a new printer?
             */
            if (cachedCupsPrinter == null) {

                /*
                 * New cached object.
                 */
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("CUPS printer [" + cupsPrinter.getName()
                            + "] detected");
                }
                /*
                 * Add the extra groups.
                 */
                if (this.hasCommonPrinterOptGroups()) {
                    cupsPrinter.getGroups().addAll(0,
                            getCommonPrinterOptGroups());
                }
            }

            /*
             * Assign the replicated database printer + lazy create printer in
             * database.
             */
            this.assignDbPrinter(cupsPrinter,
                    printerDAO().findByNameInsert(cupsPrinter.getName()));

            /*
             * Undo the logical delete (if present).
             */
            final Printer dbPrinter = cupsPrinter.getDbPrinter();

            if (dbPrinter.getDeleted()) {

                printerService().undoLogicalDeleted(dbPrinter);

                dbPrinter.setModifiedBy(Entity.ACTOR_SYSTEM);
                dbPrinter.setModifiedDate(now);

                printerDAO().update(dbPrinter);
            }

            /*
             * Update the cache.
             */
            this.cupsPrinterCache.put(cupsPrinter.getName(), cupsPrinter);
            cachedCupsPrinter = cupsPrinter;
        }

        /*
         * Remove printers from cache which are no longer present in CUPS.
         */
        for (Map.Entry<String, Boolean> entry : printersPresent.entrySet()) {

            if (!entry.getValue().booleanValue()) {

                final JsonProxyPrinter removed =
                        this.cupsPrinterCache.remove(entry.getKey());

                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("removed CUPS printer [" + removed.getName()
                            + "] detected");
                }
            }
        }

        if (isLazyInit) {
            SpInfo.instance().log(String.format("| %s CUPS printers retrieved.",
                    cupsPrinters.size()));
        }
    }

    @Override
    public final Map<String, String> getDefaultPrinterCostOptions(
            final String printerName) throws ProxyPrintException {

        try {
            lazyInitPrinterCache();
        } catch (Exception e) {
            throw new SpException(e.getMessage(), e);
        }

        final Map<String, String> printerOptionValues =
                new HashMap<String, String>();

        collectDefaultPrinterCostOptions(printerName, printerOptionValues);

        return printerOptionValues;
    }

    /**
     * Collects the "print-color-mode" and "sides" printer default options,
     * needed for cost calculation.
     *
     * @param printerName
     *            The printer name.
     * @param printerOptionValues
     *            The map to collect the default values on.
     * @throws ProxyPrintException
     *             When no printer details found.
     */
    private void collectDefaultPrinterCostOptions(final String printerName,
            final Map<String, String> printerOptionValues)
            throws ProxyPrintException {

        final JsonPrinterDetail printerDetail =
                getPrinterDetailCopy(printerName);

        if (printerDetail != null) {

            for (final JsonProxyPrinterOptGroup optGroup : printerDetail
                    .getGroups()) {

                for (final JsonProxyPrinterOpt option : optGroup.getOptions()) {

                    final String keyword = option.getKeyword();

                    if (keyword
                            .equals(IppDictJobTemplateAttr.ATTR_PRINT_COLOR_MODE)
                            || keyword.equals(
                                    IppDictJobTemplateAttr.ATTR_SIDES)) {
                        printerOptionValues.put(keyword, option.getDefchoice());
                    }
                }
            }

        } else {
            /*
             * INVARIANT: Printer details MUST be present.
             */
            if (printerDetail == null) {
                throw new ProxyPrintException(
                        "No details found for printer [" + printerName + "].");
            }

        }
    }

    /**
     * Collects data of the print event in the {@link DocLog} object.
     *
     * @param request
     *            The {@link AbstractProxyPrintReq}.
     * @param docLog
     *            The documentation object to log the event.
     * @param printer
     *            The printer object.
     * @param printJob
     *            The job object.
     * @param createInfo
     *            The {@link PdfCreateInfo} of the printed PDF file.
     */
    protected final void collectPrintOutData(
            final AbstractProxyPrintReq request, final DocLog docLog,
            final JsonProxyPrinter printer, final JsonProxyPrintJob printJob,
            final PdfCreateInfo createInfo) {

        // ------------------
        final boolean duplex =
                ProxyPrintInboxReq.isDuplex(request.getOptionValues());

        final boolean grayscale =
                ProxyPrintInboxReq.isGrayscale(request.getOptionValues());

        final int nUp = ProxyPrintInboxReq.getNup(request.getOptionValues());

        final String cupsPageSet = IppKeyword.CUPS_ATTR_PAGE_SET_ALL;
        final String cupsJobSheets = "";

        final MediaSizeName mediaSizeName =
                IppMediaSizeEnum.findMediaSizeName(request.getOptionValues()
                        .get(IppDictJobTemplateAttr.ATTR_MEDIA));

        // ------------------
        int numberOfSheets = PdfPrintCollector.calcNumberOfPrintedSheets(
                request, createInfo.getBlankFillerPages());

        // ------------------
        final DocOut docOut = docLog.getDocOut();

        docLog.setDeliveryProtocol(DocLogProtocolEnum.IPP.getDbName());

        docOut.setDestination(printer.getName());
        docOut.setEcoPrint(Boolean.valueOf(request.isEcoPrintShadow()));
        docOut.setRemoveGraphics(Boolean.valueOf(request.isRemoveGraphics()));

        docLog.setTitle(request.getJobName());

        final PrintOut printOut = new PrintOut();
        printOut.setDocOut(docOut);

        printOut.setPrintMode(request.getPrintMode().toString());
        printOut.setCupsJobId(printJob.getJobId());
        printOut.setCupsJobState(printJob.getJobState());
        printOut.setCupsCreationTime(printJob.getCreationTime());

        printOut.setDuplex(duplex);
        printOut.setReversePages(false);

        printOut.setGrayscale(grayscale);

        printOut.setCupsJobSheets(cupsJobSheets);
        printOut.setCupsNumberUp(String.valueOf(nUp));
        printOut.setCupsPageSet(cupsPageSet);

        printOut.setNumberOfCopies(request.getNumberOfCopies());
        printOut.setNumberOfSheets(numberOfSheets);

        if (request.getNumberOfCopies() > 1) {
            printOut.setCollateCopies(Boolean.valueOf(request.isCollate()));
        }

        printOut.setPaperSize(mediaSizeName.toString());

        int[] size = MediaUtils.getMediaWidthHeight(mediaSizeName);
        printOut.setPaperWidth(size[0]);
        printOut.setPaperHeight(size[1]);

        printOut.setNumberOfEsu(calcNumberOfEsu(numberOfSheets,
                printOut.getPaperWidth(), printOut.getPaperHeight()));

        printOut.setPrinter(printer.getDbPrinter());

        printOut.setIppOptions(
                JsonHelper.stringifyStringMap(request.getOptionValues()));

        docOut.setPrintOut(printOut);
    }

    /**
     * Gets the {@link User} attached to the card number.
     *
     * @param cardNumber
     *            The card number.
     * @return The {@link user}.
     * @throws ProxyPrintException
     *             When card is not associated with a user.
     */
    private User getValidateUserOfCard(final String cardNumber)
            throws ProxyPrintException {

        final UserCard userCard = userCardDAO().findByCardNumber(cardNumber);

        /*
         * INVARIANT: Card number MUST be associated with a User.
         */
        if (userCard == null) {
            throw new ProxyPrintException("Card number [" + cardNumber
                    + "] not associated with a user.");
        }
        return userCard.getUser();
    }

    /**
     * Gets the single {@link Printer} object from the reader {@link Device}
     * while validating {@link User} access.
     *
     * @param reader
     *            The reader {@link Device}.
     * @param user
     *            The {@link User}.
     * @return The {@link Printer}.
     * @throws ProxyPrintException
     *             When access is denied or no single proxy printer defined for
     *             card reader.
     */
    private Printer getValidateSingleProxyPrinterAccess(final Device reader,
            final User user) throws ProxyPrintException {

        final Printer printer = reader.getPrinter();

        /*
         * INVARIANT: printer MUST be available.
         */
        if (printer == null) {
            throw new ProxyPrintException(
                    "No proxy printer defined for card reader ["
                            + reader.getDeviceName() + "]");
        }

        this.getValidateProxyPrinterAccess(user, printer.getPrinterName(),
                ServiceContext.getTransactionDate());

        return printer;
    }

    @Override
    public final ProxyPrintDocReq createProxyPrintDocReq(final User user,
            final OutboxJobDto job, final PrintModeEnum printMode) {

        final ProxyPrintDocReq printReq = new ProxyPrintDocReq(printMode);

        printReq.setDocumentUuid(FilenameUtils.getBaseName(job.getFile()));
        printReq.setJobName(job.getJobName());
        printReq.setComment(job.getComment());
        printReq.setNumberOfPages(job.getPages());
        printReq.setNumberOfCopies(job.getCopies());

        if (job.isJobTicket()) {
            printReq.setPrinterName(job.getPrinterRedirect());
        } else {
            printReq.setPrinterName(job.getPrinter());
        }

        printReq.setRemoveGraphics(job.isRemoveGraphics());
        printReq.setEcoPrintShadow(job.isEcoPrint());
        printReq.setCollate(job.isCollate());
        printReq.setLocale(ServiceContext.getLocale());
        printReq.setIdUser(user.getId());
        printReq.putOptionValues(job.getOptionValues());
        printReq.setFitToPage(job.getFitToPage());
        printReq.setLandscape(job.getLandscape());
        printReq.setPdfOrientation(job.getPdfOrientation());
        printReq.setCostResult(job.getCostResult());

        printReq.setAccountTrxInfoSet(
                outboxService().createAccountTrxInfoSet(job));

        return printReq;
    }

    /**
     * Prints or Settles an {@link OutboxJobDto}.
     *
     * @param operator
     *            The {@link User#getUserId()} with
     *            {@link ACLRoleEnum#JOB_TICKET_OPERATOR}. {@code null} when
     *            <i>not</i> a Job Ticket.
     * @param lockedUser
     *            The locked {@link User}.
     * @param job
     *            The {@link OutboxJobDto} to print.
     * @param printMode
     *            The {@link PrintModeEnum}.
     * @param pdfFileToPrint
     *            The file (not) to print.
     * @param monitorPaperCutPrintStatus
     *            {@code true} when print status of the {@link OutboxJobDto}
     *            must be monitored in PaperCut.
     * @return The committed {@link DocLog} instance related to the
     *         {@link PrintOut}.
     * @throws IOException
     *             When IO error.
     * @throws IppConnectException
     *             When connection to CUPS fails.
     */
    private DocLog execOutboxJob(final String operator, final User lockedUser,
            final OutboxJobDto job, final PrintModeEnum printMode,
            final File pdfFileToPrint, final boolean monitorPaperCutPrintStatus)
            throws IOException, IppConnectException {

        //
        final boolean isSettlement =
                EnumSet.of(PrintModeEnum.TICKET_C, PrintModeEnum.TICKET_E)
                        .contains(printMode);

        final boolean isProxyPrint = !isSettlement;

        final boolean isJobTicket =
                isSettlement || printMode == PrintModeEnum.TICKET;

        final ProxyPrintDocReq printReq =
                this.createProxyPrintDocReq(lockedUser, job, printMode);

        final ExternalSupplierInfo supplierInfo = job.getExternalSupplierInfo();

        if (isProxyPrint && monitorPaperCutPrintStatus) {

            final PrintModeEnum printModeWrk;

            if (printMode == PrintModeEnum.TICKET) {
                printModeWrk = printMode;
            } else {
                printModeWrk = null;
            }
            paperCutService().prepareForExtPaperCut(printReq, supplierInfo,
                    printModeWrk);
        }

        /*
         * Create the DocLog container.
         */
        final DocLog docLog = this.createProxyPrintDocLog(printReq);

        final DocOut docOut = new DocOut();
        docLog.setDocOut(docOut);
        docOut.setDocLog(docLog);

        docLog.setTitle(printReq.getJobName());

        //
        if (docLog.getExternalSupplier() == null
                && job.getExternalSupplierInfo() != null) {

            docLog.setExternalSupplier(
                    job.getExternalSupplierInfo().getSupplier().toString());
        }

        if (isJobTicket) {

            docLog.setExternalId(job.getTicketNumber());

            final JobTicketSupplierData supplierData =
                    new JobTicketSupplierData();

            supplierData.setCostMedia(job.getCostResult().getCostMedia());
            supplierData.setCostCopy(job.getCostResult().getCostCopy());
            supplierData.setCostSet(job.getCostResult().getCostSet());
            supplierData.setOperator(operator);

            docLog.setExternalData(supplierData.dataAsString());
        }

        /*
         * Collect the DocOut data and proxy print.
         */
        final PdfCreateInfo createInfo = new PdfCreateInfo(pdfFileToPrint);
        createInfo.setBlankFillerPages(job.getFillerPages());

        if (printMode == PrintModeEnum.TICKET_C) {
            docLogService().collectData4DocOutCopyJob(lockedUser, docLog,
                    printReq.getNumberOfPages());
        } else {
            docLogService().collectData4DocOut(lockedUser, docLog, createInfo,
                    job.getUuidPageCount());
        }

        /*
         * Print.
         */
        if (isProxyPrint) {

            final TicketJobSheetDto jobSheetDto;
            final File pdfTicketJobSheet;

            if (isJobTicket) {

                jobSheetDto = jobTicketService()
                        .getTicketJobSheet(printReq.createIppOptionMap());

                if (jobSheetDto.getSheet() == TicketJobSheetDto.Sheet.NONE) {
                    pdfTicketJobSheet = null;
                } else {
                    jobSheetDto
                            .setMediaSourceOption(job.getMediaSourceJobSheet());

                    pdfTicketJobSheet = jobTicketService().createTicketJobSheet(
                            lockedUser.getUserId(), job, jobSheetDto);
                }
            } else {
                jobSheetDto = null;
                pdfTicketJobSheet = null;
            }

            if (pdfTicketJobSheet != null && jobSheetDto
                    .getSheet() == TicketJobSheetDto.Sheet.START) {
                proxyPrintJobSheet(printReq, job, lockedUser.getUserId(),
                        jobSheetDto, pdfTicketJobSheet);
            }

            proxyPrint(lockedUser, printReq, docLog, createInfo);

            if (jobSheetDto != null
                    && jobSheetDto.getSheet() == TicketJobSheetDto.Sheet.END) {
                proxyPrintJobSheet(printReq, job, lockedUser.getUserId(),
                        jobSheetDto, pdfTicketJobSheet);
            }

        } else {
            settleProxyPrint(lockedUser, printReq, docLog, createInfo);
        }

        if (isSettlement && monitorPaperCutPrintStatus) {
            try {
                settleProxyPrintPaperCut(docLog, job.getCopies(),
                        job.getCostResult());
            } catch (PaperCutException e) {
                throw new IOException(e.getMessage(), e);
            }
        }

        // Preserve PDF file when Job Ticket.
        if (!isJobTicket) {
            pdfFileToPrint.delete();
        }

        /*
         * If this is NOT a job ticket and we do not monitor PaperCut print
         * status, we know (assume) the job is completed. We notify for a HOLD
         * job.
         *
         * IMPORTANT: The Job Ticket client handles its own notification.
         */
        if (!isJobTicket && !monitorPaperCutPrintStatus) {
            outboxService().onOutboxJobCompleted(job);
        }

        return docLog;
    }

    /**
     * Settles a proxy print in PaperCut.
     *
     * @param docLog
     *            The {@link DocLog} container.
     * @param copies
     *            The number of printed copies.
     * @param cost
     *            The print cost.
     * @throws PaperCutException
     *             When logical PaperCut error.
     */
    private void settleProxyPrintPaperCut(final DocLog docLog, final int copies,
            final ProxyPrintCostDto cost) throws PaperCutException {

        final PaperCutServerProxy serverProxy =
                PaperCutServerProxy.create(ConfigManager.instance(), true);

        final PaperCutAccountAdjustPrint adjustPattern =
                new PaperCutAccountAdjustPrint(serverProxy,
                        PAPERCUT_ACCOUNT_RESOLVER, LOGGER);

        adjustPattern.process(docLog, docLog, false, cost.getCostTotal(),
                copies);
    }

    @Override
    public void refundProxyPrintPaperCut(final CostChange costChange)
            throws PaperCutException {

        final PaperCutServerProxy serverProxy =
                PaperCutServerProxy.create(ConfigManager.instance(), true);

        final PaperCutAccountAdjustPrintRefund adjustPattern =
                new PaperCutAccountAdjustPrintRefund(serverProxy,
                        PAPERCUT_ACCOUNT_RESOLVER, LOGGER);

        adjustPattern.process(costChange);
    }

    /**
     * Publishes a proxy print event and sets the user message with
     * {@link AbstractProxyPrintReq#setUserMsgKey(String)} and
     * {@link AbstractProxyPrintReq#setUserMsg(String)}.
     *
     * @param lockedUser
     *            The requesting {@link User}, which should be locked.
     * @param request
     *            The {@link AbstractProxyPrintReq}.
     * @param docLog
     *            The {@link DocLog} persisted in the database.
     */
    private void publishProxyPrintEvent(final User lockedUser,
            final AbstractProxyPrintReq request, final DocLog docLog) {

        final String userMsgKey;
        final String userMsg;

        if (request.getClearedObjects() == 0) {

            userMsgKey = "msg-printed";
            userMsg = localize(request.getLocale(), userMsgKey);

        } else if (request.getClearedObjects() == 1) {

            if (request.getClearScope() == InboxSelectScopeEnum.JOBS) {
                userMsgKey = "msg-printed-deleted-job-one";
            } else {
                userMsgKey = "msg-printed-deleted-one";
            }
            userMsg = localize(request.getLocale(), userMsgKey);
        } else {

            if (request.getClearScope() == InboxSelectScopeEnum.JOBS) {
                userMsgKey = "msg-printed-deleted-job-multiple";
            } else {
                userMsgKey = "msg-printed-deleted-multiple";
            }
            userMsg = localize(request.getLocale(), userMsgKey,
                    String.valueOf(request.getClearedObjects()));
        }

        //
        final String pagesMsgKey;

        if (docLog.getNumberOfPages().intValue() == 1) {
            pagesMsgKey = "msg-printed-for-admin-single-page";
        } else {
            pagesMsgKey = "msg-printed-for-admin-multiple-pages";
        }

        //
        final PrintOut printOut = docLog.getDocOut().getPrintOut();

        final String copiesMsgKey;

        if (printOut.getNumberOfCopies().intValue() == 1) {
            copiesMsgKey = "msg-printed-for-admin-single-copy";
        } else {
            copiesMsgKey = "msg-printed-for-admin-multiple-copies";
        }

        //
        final String sheetsMsgKey;

        if (printOut.getNumberOfSheets().intValue() == 1) {
            sheetsMsgKey = "msg-printed-for-admin-single-sheet";
        } else {
            sheetsMsgKey = "msg-printed-for-admin-multiple-sheets";
        }

        AdminPublisher.instance().publish(PubTopicEnum.PROXY_PRINT,
                PubLevelEnum.INFO,
                localize(request.getLocale(), "msg-printed-for-admin",
                        request.getPrintMode().uiText(request.getLocale()),
                        printOut.getPrinter().getDisplayName(),
                        lockedUser.getUserId(),
                        //
                        localize(request.getLocale(), pagesMsgKey,
                                docLog.getNumberOfPages().toString()),
                        //
                        localize(request.getLocale(), copiesMsgKey,
                                printOut.getNumberOfCopies().toString()),
                        //
                        localize(request.getLocale(), sheetsMsgKey,
                                printOut.getNumberOfSheets().toString()))
        //
        );
        request.setUserMsgKey(userMsgKey);
        request.setUserMsg(userMsg);
    }

    /**
     * Settles a proxy print without doing the actual printing. Updates
     * {@link User}, {@link Printer} and global {@link IConfigProp} statistics.
     * <p>
     * Note: Invariants are NOT checked. The {@link InboxInfoDto} is NOT
     * updated.
     * </p>
     *
     * @param lockedUser
     *            The requesting {@link User}, which should be locked.
     * @param request
     *            The {@link AbstractProxyPrintReq}.
     * @param docLog
     *            The {@link DocLog} to persist in the database.
     * @param createInfo
     *            The {@link PdfCreateInfo} with the PDF file to send to the
     *            printer.
     */
    private void settleProxyPrint(final User lockedUser,
            final AbstractProxyPrintReq request, final DocLog docLog,
            final PdfCreateInfo createInfo) {

        /*
         * Create fake CUPS print job.
         */
        final int cupsTimeNow =
                (int) (ServiceContext.getTransactionDate().getTime()
                        / DateUtil.DURATION_MSEC_SECOND);

        final JsonProxyPrintJob printJob = new JsonProxyPrintJob();

        printJob.setUser(lockedUser.getUserId());
        printJob.setJobId(Integer.valueOf(0));
        printJob.setJobState(IppJobStateEnum.IPP_JOB_COMPLETED.asInteger());
        printJob.setCreationTime(cupsTimeNow);
        printJob.setCompletedTime(cupsTimeNow);
        printJob.setDest("");
        printJob.setTitle(request.getJobName());

        /*
         * Collect the PrintOut data.
         */
        final JsonProxyPrinter jsonPrinter =
                this.getJsonProxyPrinterCopy(request.getPrinterName());

        collectPrintOutData(request, docLog, jsonPrinter, printJob, createInfo);

        final PrintOut printOut = docLog.getDocOut().getPrintOut();

        printOut.setCupsJobSheets("");
        printOut.setCupsNumberUp(String.valueOf(request.getNup()));
        printOut.setCupsPageSet(IppKeyword.CUPS_ATTR_PAGE_SET_ALL);

        /*
         * Persists the DocLog.
         */
        docLogService().settlePrintOut(lockedUser,
                docLog.getDocOut().getPrintOut(),
                request.getAccountTrxInfoSet());

        request.setStatus(ProxyPrintInboxReq.Status.PRINTED); // TODO

        /*
         * Publish.
         */
        publishProxyPrintEvent(lockedUser, request, docLog);
    }

    /**
     * Checks if {@link OutboxJobDto} has External Supplier other than
     * {@link ExternalSupplierEnum#SAVAPAGE} and external print manager is
     * {@link ThirdPartyEnum#PAPERCUT}.
     * <p>
     * If so, the following corrections are performed:
     * </p>
     * <ol>
     * <li>Change the DocLog of the PrintIn.
     * <ul>
     * <li>Change ExternalStatus from PENDING to PENDING_EXT</li>
     * <li>Change ExternalSupplier from SAVAPAGE to the original external
     * supplier.</li>
     * </ul>
     * </li>
     * <li>If present, ad-hoc create the AccountTrx's at the DocLog of the
     * PrintIn document.</li>
     * </ol>
     * <p>
     * <i>The corrections will restore things to a situation as if the ticket
     * from the External Supplier was printed directly to a PaperCut Managed
     * Printer.</i>
     * </p>
     * <p>
     * For example, when a {@link ExternalSupplierEnum#SMARTSCHOOL} ticket is
     * printed to a SavaPage Job Ticket Printer, and released to a
     * {@link ThirdPartyEnum#PAPERCUT} managed printer from there, it will be as
     * if the external ticket was directly printed to the PaperCut managed
     * printer: the SavaPage Ticket was just a detour.
     * </p>
     *
     * @param job
     *            The {@link OutboxJobDto} Job Ticket. *
     * @param extPrinterManager
     *            The {@link ThirdPartyEnum} external print manager:
     *            {@code null} when native SavaPage.
     *
     * @return {@code true} when correction was performed, {@code false} when
     *         correction was not needed.
     */
    private boolean validateJobTicketPaperCutSupplier(final OutboxJobDto job,
            final ThirdPartyEnum extPrinterManager) {

        /*
         * INVARIANT: Print manager MUST be PaperCut.
         */
        if (extPrinterManager != ThirdPartyEnum.PAPERCUT) {
            return false;
        }

        final ExternalSupplierInfo supplierInfo = job.getExternalSupplierInfo();

        /*
         * INVARIANT: Current supplier of the job MUST NOT be SavaPage.
         */
        if (supplierInfo == null || supplierInfo
                .getSupplier() == ExternalSupplierEnum.SAVAPAGE) {
            return false;
        }

        /*
         * Find the DocLog of the DocIn that lead to this ticket.
         */
        final DocLog docLogIn = docLogService().getSuppliedDocLog(
                supplierInfo.getSupplier(), supplierInfo.getAccount(),
                supplierInfo.getId(), ExternalSupplierStatusEnum.PENDING);

        /*
         * INVARIANT: DocLog MUST be present.
         */
        if (docLogIn == null) {
            return false;
        }

        /*
         * Correct status and ad-hoc create trx.
         */
        final DaoContext daoContext = ServiceContext.getDaoContext();

        final boolean wasAdhocTransaction = daoContext.isTransactionActive();

        if (!wasAdhocTransaction) {
            daoContext.beginTransaction();
        }

        try {
            docLogIn.setExternalStatus(
                    ExternalSupplierStatusEnum.PENDING_EXT.toString());

            docLogIn.setExternalSupplier(supplierInfo.getSupplier().toString());

            final AccountTrxInfoSet accountTrxInfoSet =
                    outboxService().createAccountTrxInfoSet(job);

            if (accountTrxInfoSet != null) {
                accountingService().createAccountTrxs(accountTrxInfoSet,
                        docLogIn, AccountTrxTypeEnum.PRINT_IN);
            }

            docLogDAO().update(docLogIn);

            daoContext.commit();
        } finally {
            daoContext.rollback();
        }

        if (wasAdhocTransaction) {
            daoContext.beginTransaction();
        }

        /*
         * IMPORTANT: Remove AccountTrx's at the Job Ticket, so they are NOT
         * created (again) at the DocLog of the DocOut object, when ticket is
         * proxy printed.
         */
        job.setAccountTransactions(null);

        return true;
    }

    @Override
    public final DocLog proxyPrintJobTicket(final String operator,
            final User lockedUser, final OutboxJobDto job,
            final File pdfFileToPrint, final ThirdPartyEnum extPrinterManager)
            throws IOException, IppConnectException {

        this.validateJobTicketPaperCutSupplier(job, extPrinterManager);

        return this.execOutboxJob(operator, lockedUser, job,
                PrintModeEnum.TICKET, pdfFileToPrint,
                extPrinterManager == ThirdPartyEnum.PAPERCUT);
    }

    @Override
    public final int settleJobTicket(final String operator,
            final User lockedUser, final OutboxJobDto job,
            final File pdfFileToPrint, final ThirdPartyEnum extPrinterManager)
            throws IOException {

        this.validateJobTicketPaperCutSupplier(job, extPrinterManager);

        try {

            final PrintModeEnum printMode;

            if (job.isCopyJobTicket()) {
                printMode = PrintModeEnum.TICKET_C;
            } else {
                printMode = PrintModeEnum.TICKET_E;
            }

            this.execOutboxJob(operator, lockedUser, job, printMode,
                    pdfFileToPrint,
                    extPrinterManager == ThirdPartyEnum.PAPERCUT);

        } catch (IppConnectException e) {
            throw new SpException(e.getMessage());
        }
        return job.getPages() * job.getCopies();
    }

    @Override
    public final ProxyPrintOutboxResult proxyPrintOutbox(final Device reader,
            final String cardNumber) throws ProxyPrintException {

        final Date perfStartTime = PerformanceLogger.startTime();

        /*
         * Make sure the CUPS printer is cached.
         */
        try {
            this.lazyInitPrinterCache();
        } catch (Exception e) {
            throw new ProxyPrintException(e);
        }

        final User cardUser = getValidateUserOfCard(cardNumber);

        if (!outboxService().isOutboxPresent(cardUser.getUserId())) {
            return new ProxyPrintOutboxResult();
        }

        final Set<String> printerNames =
                deviceService().collectPrinterNames(reader);

        /*
         * Lock the user.
         */
        final User lockedUser = userDAO().lock(cardUser.getId());

        /*
         * Get the outbox job candidates.
         */
        final List<OutboxJobDto> jobs =
                outboxService().getOutboxJobs(lockedUser.getUserId(),
                        printerNames, ServiceContext.getTransactionDate());

        /*
         * Check printer access and total costs first (all-or-none).
         */
        BigDecimal totCost = BigDecimal.ZERO;

        for (final OutboxJobDto job : jobs) {

            this.getValidateProxyPrinterAccess(cardUser, job.getPrinter(),
                    ServiceContext.getTransactionDate());

            totCost = totCost.add(job.getCostTotal());
        }

        accountingService().validateProxyPrintUserCost(lockedUser, totCost,
                ServiceContext.getLocale(),
                ServiceContext.getAppCurrencySymbol());

        int totSheets = 0;
        int totPages = 0;

        for (final OutboxJobDto job : jobs) {

            final boolean monitorPaperCutPrintStatus =
                    outboxService().isMonitorPaperCutPrintStatus(job);

            final File pdfFileToPrint = outboxService()
                    .getOutboxFile(cardUser.getUserId(), job.getFile());

            try {
                this.execOutboxJob(null, lockedUser, job, PrintModeEnum.HOLD,
                        pdfFileToPrint, monitorPaperCutPrintStatus);

                pdfFileToPrint.delete();

            } catch (IppConnectException | IOException e) {
                throw new SpException(e.getMessage());
            }

            totSheets += job.getSheets() * job.getCopies();
            totPages += job.getPages() * job.getCopies();
        }

        PerformanceLogger.log(this.getClass(), "proxyPrintOutbox",
                perfStartTime, cardUser.getUserId());

        return new ProxyPrintOutboxResult(jobs.size(), totSheets, totPages);
    }

    @Override
    public final int proxyPrintInboxFast(final Device reader,
            final String cardNumber) throws ProxyPrintException {

        final Date perfStartTime = PerformanceLogger.startTime();

        final User cardUser = getValidateUserOfCard(cardNumber);

        final Printer printer =
                getValidateSingleProxyPrinterAccess(reader, cardUser);

        /*
         * Printer must be properly configured.
         */
        if (!this.isPrinterConfigured(
                this.getCachedCupsPrinter(printer.getPrinterName()),
                new PrinterAttrLookup(printer))) {

            throw new ProxyPrintException(
                    String.format("Print for user \"%s\" denied: %s \"%s\" %s",
                            cardUser.getUserId(), "printer",
                            printer.getPrinterName(), "is not configured."));
        }

        //
        this.getValidateProxyPrinterAccess(cardUser, printer.getPrinterName(),
                ServiceContext.getTransactionDate());

        /*
         * Get Printer default options.
         */
        final Map<String, String> printerOptionValues =
                getDefaultPrinterCostOptions(printer.getPrinterName());

        final boolean isConvertToGrayscale =
                AbstractProxyPrintReq.isGrayscale(printerOptionValues)
                        && isColorPrinter(printer.getPrinterName())
                        && printerService().isClientSideMonochrome(printer);
        /*
         * Lock the user.
         */
        final User user = userDAO().lock(cardUser.getId());

        /*
         * Get the inbox.
         */
        final InboxInfoDto jobs;
        final int nPagesTot;

        if (inboxService().doesHomeDirExist(user.getUserId())) {

            inboxService().pruneOrphanJobs(
                    ConfigManager.getUserHomeDir(user.getUserId()), user);

            jobs = inboxService().pruneForFastProxyPrint(user.getUserId(),
                    ServiceContext.getTransactionDate(),
                    ConfigManager.instance()
                            .getConfigInt(Key.PROXY_PRINT_FAST_EXPIRY_MINS));

            nPagesTot = inboxService().calcNumberOfPagesInJobs(jobs);

        } else {

            jobs = null;
            nPagesTot = 0;
        }

        /*
         * INVARIANT: There MUST be at least one (1) inbox job.
         */
        if (nPagesTot == 0) {
            return 0;
        }

        /*
         * Create the request for each job, so we can check the credit limit
         * invariant.
         */
        final List<ProxyPrintInboxReq> printReqList = new ArrayList<>();

        final int nJobs = jobs.getJobs().size();

        if (nJobs > 1 && inboxService().isInboxVanilla(jobs)) {
            /*
             * Print each job separately.
             */
            int nJobPageBegin = 1;

            for (int iJob = 0; iJob < nJobs; iJob++) {

                final ProxyPrintInboxReq printReq =
                        new ProxyPrintInboxReq(null);

                printReqList.add(printReq);

                final InboxJob job = jobs.getJobs().get(iJob);

                final int totJobPages = job.getPages().intValue();
                final int nJobPageEnd = nJobPageBegin + totJobPages - 1;
                final String pageRanges = nJobPageBegin + "-" + nJobPageEnd;

                /*
                 * Fixed values.
                 */
                printReq.setPrintMode(PrintModeEnum.FAST);
                printReq.setPrinterName(printer.getPrinterName());
                printReq.setNumberOfCopies(Integer.valueOf(1));
                printReq.setRemoveGraphics(false);
                printReq.setConvertToGrayscale(isConvertToGrayscale);
                printReq.setLocale(ServiceContext.getLocale());
                printReq.setIdUser(user.getId());
                printReq.putOptionValues(printerOptionValues);
                printReq.setMediaSourceOption(IppKeyword.MEDIA_SOURCE_AUTO);

                /*
                 * Variable values.
                 */
                printReq.setJobName(job.getTitle());
                printReq.setPageRanges(pageRanges);
                printReq.setNumberOfPages(totJobPages);

                /*
                 * If this is the last job, then clear all pages.
                 */
                final InboxSelectScopeEnum clearScope;

                if (iJob + 1 == nJobs) {
                    clearScope = InboxSelectScopeEnum.ALL;
                } else {
                    clearScope = InboxSelectScopeEnum.NONE;
                }

                printReq.setClearScope(clearScope);

                //
                nJobPageBegin += totJobPages;
            }

        } else {
            /*
             * Print as ONE job.
             */
            final ProxyPrintInboxReq printReq = new ProxyPrintInboxReq(null);
            printReqList.add(printReq);

            /*
             * Fixed values.
             */
            printReq.setPrintMode(PrintModeEnum.FAST);
            printReq.setPrinterName(printer.getPrinterName());
            printReq.setNumberOfCopies(Integer.valueOf(1));
            printReq.setRemoveGraphics(false);
            printReq.setConvertToGrayscale(isConvertToGrayscale);
            printReq.setLocale(ServiceContext.getLocale());
            printReq.setIdUser(user.getId());
            printReq.putOptionValues(printerOptionValues);
            printReq.setMediaSourceOption(IppKeyword.MEDIA_SOURCE_AUTO);

            /*
             * Variable values.
             */
            printReq.setJobName(jobs.getJobs().get(0).getTitle());
            printReq.setPageRanges(ProxyPrintInboxReq.PAGE_RANGES_ALL);
            printReq.setNumberOfPages(nPagesTot);
            printReq.setClearScope(InboxSelectScopeEnum.ALL);
        }

        /*
         * INVARIANT: User MUST have enough balance.
         */
        final String currencySymbol = "";

        BigDecimal totalCost = BigDecimal.ZERO;

        for (final ProxyPrintInboxReq printReq : printReqList) {

            /*
             * Chunk!
             */
            this.chunkProxyPrintRequest(user, printReq, PageScalingEnum.FIT,
                    false, null);

            final ProxyPrintCostParms costParms = new ProxyPrintCostParms(null);

            /*
             * Set the common parameters for all print job chunks, and calculate
             * the cost.
             */
            costParms.setDuplex(printReq.isDuplex());
            costParms.setGrayscale(printReq.isGrayscale());
            costParms.setEcoPrint(printReq.isEcoPrintShadow());
            costParms.setNumberOfCopies(printReq.getNumberOfCopies());
            costParms.setPagesPerSide(printReq.getNup());

            printReq.setCostResult(accountingService().calcProxyPrintCost(
                    ServiceContext.getLocale(), currencySymbol, user, printer,
                    costParms, printReq.getJobChunkInfo()));

            totalCost = totalCost.add(printReq.getCostResult().getCostTotal());
        }

        /*
         * Check the total, since each individual job may be within credit
         * limit, but the total may not.
         */
        final Account account = accountingService()
                .lazyGetUserAccount(user, AccountTypeEnum.USER).getAccount();

        if (!accountingService().isBalanceSufficient(account, totalCost)) {
            throw new ProxyPrintException("User [" + user.getUserId()
                    + "] has insufficient balance for proxy printing.");
        }

        /*
         * Direct Proxy Print.
         */
        for (final ProxyPrintInboxReq printReq : printReqList) {

            try {

                proxyPrintInbox(user, printReq);

            } catch (Exception e) {

                throw new SpException("Printing error for user ["
                        + user.getUserId() + "] on printer ["
                        + printer.getPrinterName() + "].", e);
            }

            if (printReq.getStatus() != ProxyPrintInboxReq.Status.PRINTED) {

                throw new ProxyPrintException(
                        "Proxy print error [" + printReq.getStatus() + "] on ["
                                + printer.getPrinterName() + "] for user ["
                                + user.getUserId() + "].");
            }
        }

        PerformanceLogger.log(this.getClass(), "proxyPrintInboxFast",
                perfStartTime, user.getUserId());

        return nPagesTot;
    }

    /**
     * Creates a standard {@link DocLog} instance for proxy printing. Just the
     * financial data and external supplier data are used from The
     * {@link AbstractProxyPrintReq}: no related objects are created.
     *
     * @param request
     *            The {@link AbstractProxyPrintReq}.
     * @return The {@link DocLog} instance.
     */
    private DocLog createProxyPrintDocLog(final AbstractProxyPrintReq request) {

        final DocLog docLog = new DocLog();

        /*
         * Financial data.
         */
        docLog.setCost(request.getCostResult().getCostTotal());
        docLog.setCostOriginal(request.getCostResult().getCostTotal());
        docLog.setRefunded(false);
        docLog.setInvoiced(true);

        docLog.setLogComment(request.getComment());

        /*
         * External supplier.
         */
        final ExternalSupplierInfo supplierInfo = request.getSupplierInfo();

        if (supplierInfo != null) {

            docLog.setExternalId(supplierInfo.getId());
            docLog.setExternalStatus(supplierInfo.getStatus());
            docLog.setExternalSupplier(supplierInfo.getSupplier().toString());

            if (supplierInfo.getData() != null) {
                docLog.setExternalData(supplierInfo.getData().dataAsString());
            }
        }

        return docLog;
    }

    @Override
    public final int clearInbox(final User lockedUser,
            final ProxyPrintInboxReq request) {

        final String userid = lockedUser.getUserId();
        final int clearedObjects;

        switch (request.getClearScope()) {

        case ALL:
            clearedObjects = inboxService().deleteAllPages(userid);
            break;

        case JOBS:
            clearedObjects = inboxService().deleteJobs(userid,
                    request.getJobChunkInfo().getChunks());
            break;

        case PAGES:
            if (request.getPageRangesJobIndex() == null) {
                clearedObjects = inboxService().deletePages(userid,
                        request.getPageRanges());
            } else {
                clearedObjects = inboxService().deleteJobPages(userid,
                        request.getPageRangesJobIndex().intValue(),
                        request.getPageRanges());
            }
            break;

        case NONE:
            clearedObjects = 0;
            break;

        default:
            throw new SpException(String.format("Unhandled enum value [%s]",
                    request.getClearScope().toString()));
        }

        return clearedObjects;
    }

    /**
     * Print a Job Sheet and deletes the PDF job sheet afterwards.
     *
     * @param reqMain
     *            The print request of the main job.
     * @param job
     *            The {@link OutboxJobDto}.
     * @param user
     *            The unique user id.
     * @param jobSheetDto
     *            Job Sheet info.
     * @param pdfJobSheet
     *            The Job sheet PDF file.
     * @throws IppConnectException
     *             When printing fails.
     */
    private void proxyPrintJobSheet(final AbstractProxyPrintReq reqMain,
            final OutboxJobDto job, final String user,
            final TicketJobSheetDto jobSheetDto, final File pdfJobSheet)
            throws IppConnectException {

        final JsonProxyPrinter printer =
                this.getJsonProxyPrinterCopy(reqMain.getPrinterName());

        if (printer == null) {
            throw new IllegalStateException(String.format(
                    "Printer [%s] not found.", reqMain.getPrinterName()));
        } else if (printer.getDbPrinter().getDeleted()) {
            throw new IllegalStateException(String.format(
                    "Printer [%s] is deleted.", reqMain.getPrinterName()));
        } else if (printer.getDbPrinter().getDisabled()) {
            throw new IllegalStateException(String.format(
                    "Printer [%s] is disabled.", reqMain.getPrinterName()));
        }

        try {
            final PdfCreateInfo createInfo = new PdfCreateInfo(pdfJobSheet);

            final ProxyPrintDocReq reqBanner =
                    new ProxyPrintDocReq(PrintModeEnum.TICKET);

            reqBanner.setNumberOfCopies(1);
            reqBanner.setFitToPage(Boolean.TRUE);

            reqBanner.setJobName(
                    String.format("Ticket-Banner-%s", job.getTicketNumber()));

            final Map<String, String> options = new HashMap<>();

            options.put(IppDictJobTemplateAttr.ATTR_MEDIA,
                    jobSheetDto.getMediaOption());
            options.put(IppDictJobTemplateAttr.ATTR_MEDIA_SOURCE,
                    jobSheetDto.getMediaSourceOption());
            options.put(IppDictJobTemplateAttr.ATTR_OUTPUT_BIN,
                    reqMain.getOptionValues()
                            .get(IppDictJobTemplateAttr.ATTR_OUTPUT_BIN));

            // Overrule printer defaults.
            options.put(IppDictJobTemplateAttr.ATTR_SIDES,
                    IppKeyword.SIDES_ONE_SIDED);
            options.put(IppDictJobTemplateAttr.ATTR_PRINT_COLOR_MODE,
                    IppKeyword.PRINT_COLOR_MODE_MONOCHROME);
            options.put(IppDictJobTemplateAttr.ATTR_JOB_SHEETS,
                    IppKeyword.ATTR_JOB_SHEETS_NONE);

            //
            reqBanner.setOptionValues(options);

            // final JsonProxyPrintJob printJob =
            this.sendPdfToPrinter(reqBanner, printer, user, createInfo);

        } finally {
            if (pdfJobSheet != null && pdfJobSheet.exists()) {
                pdfJobSheet.delete();
            }
        }
    }

    /**
     * Sends PDF file to the CUPS Printer, and updates {@link User},
     * {@link Printer} and global {@link IConfigProp} statistics.
     * <p>
     * Note: This is a straight proxy print. Invariants are NOT checked. The
     * {@link InboxInfoDto} is updated when this is an
     * {@link ProxyPrintInboxReq} and pages or jobs need to be cleared. See
     * {@link ProxyPrintInboxReq#getClearScope()}.
     * </p>
     *
     * @param lockedUser
     *            The requesting {@link User}, which should be locked.
     * @param request
     *            The {@link AbstractProxyPrintReq}.
     * @param docLog
     *            The {@link DocLog} to persist in the database.
     * @param createInfo
     *            The {@link PdfCreateInfo} with the PDF file to send to the
     *            printer.
     * @throws IppConnectException
     *             When CUPS connection is broken.
     */
    private void proxyPrint(final User lockedUser,
            final AbstractProxyPrintReq request, final DocLog docLog,
            final PdfCreateInfo createInfo) throws IppConnectException {

        final String userid = lockedUser.getUserId();

        /*
         * Print the PDF file.
         */
        if (this.print(request, userid, createInfo, docLog)) {

            if (request instanceof ProxyPrintInboxReq) {
                request.setClearedObjects(this.clearInbox(lockedUser,
                        (ProxyPrintInboxReq) request));
            } else {
                request.setClearedObjects(0);
            }

            docLogService().logDocOut(lockedUser, docLog.getDocOut(),
                    request.getAccountTrxInfoSet());

            request.setStatus(ProxyPrintInboxReq.Status.PRINTED);

            publishProxyPrintEvent(lockedUser, request, docLog);

        } else {
            final String userMsgKey = "msg-printer-not-found";
            final String userMsg = localize(request.getLocale(), userMsgKey,
                    request.getPrinterName());

            request.setStatus(
                    ProxyPrintInboxReq.Status.ERROR_PRINTER_NOT_FOUND);
            request.setUserMsgKey(userMsgKey);
            request.setUserMsg(userMsg);

            LOGGER.error(userMsg);
        }
    }

    @Override
    public final void proxyPrintPdf(final User lockedUser,
            final ProxyPrintDocReq request, final PdfCreateInfo createInfo)
            throws IppConnectException, ProxyPrintException {

        /*
         * Get access to the printer.
         */
        final String printerName = request.getPrinterName();

        final Printer printer = this.getValidateProxyPrinterAccess(lockedUser,
                printerName, ServiceContext.getTransactionDate());
        /*
         * Calculate and validate cost.
         */
        final ProxyPrintCostParms costParms = new ProxyPrintCostParms(null);

        /*
         * Set the common parameters.
         */
        costParms.setDuplex(request.isDuplex());
        costParms.setGrayscale(request.isGrayscale());
        costParms.setEcoPrint(request.isEcoPrintShadow());
        costParms.setNumberOfCopies(request.getNumberOfCopies());
        costParms.setPagesPerSide(request.getNup());

        /*
         * Set the parameters for this single PDF file.
         */
        costParms.setNumberOfSheets(PdfPrintCollector.calcNumberOfPrintedSheets(
                request, createInfo.getBlankFillerPages()));
        costParms.setNumberOfPages(request.getNumberOfPages());
        costParms.setLogicalNumberOfPages(createInfo.getLogicalJobPages());
        costParms.setIppMediaOption(request.getMediaOption());

        final ProxyPrintCostDto costResult = accountingService()
                .calcProxyPrintCost(ServiceContext.getLocale(),
                        ServiceContext.getAppCurrencySymbol(), lockedUser,
                        printer, costParms, request.getJobChunkInfo());

        request.setCostResult(costResult);

        /*
         * Create the DocLog container.
         */
        final DocLog docLog = this.createProxyPrintDocLog(request);

        final DocOut docOut = new DocOut();
        docLog.setDocOut(docOut);
        docOut.setDocLog(docLog);

        docLog.setTitle(request.getJobName());

        /*
         * Collect the DocOut data for just a single DocIn document.
         */
        final LinkedHashMap<String, Integer> uuidPageCount =
                new LinkedHashMap<>();

        uuidPageCount.put(request.getDocumentUuid(),
                Integer.valueOf(request.getNumberOfPages()));

        try {
            docLogService().collectData4DocOut(lockedUser, docLog, createInfo,
                    uuidPageCount);
        } catch (IOException e) {
            throw new SpException(e.getMessage());
        }

        /*
         * Finally, proxy print.
         */
        proxyPrint(lockedUser, request, docLog, createInfo);
    }

    @Override
    public final void chunkProxyPrintRequest(final User lockedUser,
            final ProxyPrintInboxReq request, final PageScalingEnum pageScaling,
            final boolean chunkVanillaJobs, final Integer iVanillaJob)
            throws ProxyPrintException {
        new ProxyPrintInboxReqChunker(lockedUser, request, pageScaling)
                .chunk(chunkVanillaJobs, iVanillaJob, request.getPageRanges());
    }

    @Override
    public final void proxyPrintInbox(final User lockedUser,
            final ProxyPrintInboxReq request)
            throws IppConnectException, EcoPrintPdfTaskPendingException {

        /*
         * When printing the chunks, the container request parameters are
         * replaced by chunk values. So, we save the original request parameters
         * here, and restore them afterwards.
         */
        final String orgJobName = request.getJobName();
        final InboxSelectScopeEnum orgClearScope = request.getClearScope();
        final Boolean orgFitToPage = request.getFitToPage();
        final String orgMediaOption = request.getMediaOption();
        final String orgMediaSourceOption = request.getMediaSourceOption();
        final ProxyPrintCostDto orgCostResult = request.getCostResult();

        try {

            if (request.getJobChunkInfo() == null) {

                final InboxInfoDto inboxInfo =
                        inboxService().readInboxInfo(lockedUser.getUserId());

                final InboxInfoDto filteredInboxInfo =
                        inboxService().filterInboxInfoPages(inboxInfo,
                                request.getPageRanges());

                proxyPrintInboxChunk(lockedUser, request, filteredInboxInfo, 0);

            } else {

                final InboxInfoDto inboxInfo =
                        request.getJobChunkInfo().getFilteredInboxInfo();

                final int nChunkMax =
                        request.getJobChunkInfo().getChunks().size();

                int nChunk = 0;

                for (final ProxyPrintJobChunk chunk : request.getJobChunkInfo()
                        .getChunks()) {

                    nChunk++;

                    /*
                     * Replace the request parameters with the chunk parameters.
                     */
                    if (nChunk == nChunkMax) {
                        request.setClearScope(orgClearScope);
                    } else {
                        request.setClearScope(InboxSelectScopeEnum.NONE);
                    }

                    request.setFitToPage(chunk.getFitToPage());

                    request.setMediaOption(
                            chunk.getAssignedMedia().getIppKeyword());

                    /*
                     * Take the media-source from the print request, unless it
                     * is assigned in the chunk.
                     */
                    if (chunk.getAssignedMediaSource() == null) {
                        request.setMediaSourceOption(orgMediaSourceOption);
                    } else {
                        request.setMediaSourceOption(
                                chunk.getAssignedMediaSource().getSource());
                    }

                    request.setCostResult(chunk.getCostResult());

                    if (StringUtils.isBlank(orgJobName)) {
                        request.setJobName(chunk.getJobName());
                    }

                    /*
                     * Save the original pages.
                     */
                    final ArrayList<InboxJobRange> orgPages =
                            inboxService().replaceInboxInfoPages(inboxInfo,
                                    chunk.getRanges());

                    final int orgNumberOfPages = request.getNumberOfPages();

                    /*
                     * Proxy print the chunk.
                     */

                    // Mantis #723
                    request.setNumberOfPages(chunk.getNumberOfPages());

                    proxyPrintInboxChunk(lockedUser, request, inboxInfo,
                            nChunk);

                    /*
                     * Restore the original pages.
                     */
                    request.setNumberOfPages(orgNumberOfPages);
                    inboxInfo.setPages(orgPages);
                }
            }

        } finally {
            /*
             * Restore the original request parameters.
             */
            request.setJobName(orgJobName);
            request.setClearScope(orgClearScope);
            request.setFitToPage(orgFitToPage);
            request.setMediaOption(orgMediaOption);
            request.setMediaSourceOption(orgMediaSourceOption);
            request.setCostResult(orgCostResult);
        }
    }

    /**
     * Proxy prints a single inbox chunk.
     *
     * @param lockedUser
     *            The requesting {@link User}, which should be locked.
     * @param request
     *            The {@link ProxyPrintInboxReq}.
     * @param inboxInfo
     *            The {@link InboxInfoDto}.
     * @param nChunk
     *            The chunk ordinal (used to compose a unique PDF filename).
     * @throws IppConnectException
     *             When CUPS connection is broken.
     * @throws EcoPrintPdfTaskPendingException
     *             When {@link EcoPrintPdfTask} objects needed for this PDF are
     *             pending.
     */
    private void proxyPrintInboxChunk(final User lockedUser,
            final ProxyPrintInboxReq request, final InboxInfoDto inboxInfo,
            final int nChunk)
            throws IppConnectException, EcoPrintPdfTaskPendingException {

        final DocLog docLog = this.createProxyPrintDocLog(request);

        /*
         * Generate the temporary PDF file.
         */
        File pdfFileToPrint = null;

        try {

            final String pdfFileName = OutputProducer.createUniqueTempPdfName(
                    lockedUser, String.format("printjob-%d-", nChunk));

            final LinkedHashMap<String, Integer> uuidPageCount =
                    new LinkedHashMap<>();

            final PdfCreateRequest pdfRequest = new PdfCreateRequest();

            pdfRequest.setUserObj(lockedUser);
            pdfRequest.setPdfFile(pdfFileName);
            pdfRequest.setInboxInfo(inboxInfo);
            pdfRequest.setRemoveGraphics(request.isRemoveGraphics());
            pdfRequest.setEcoPdfShadow(request.isEcoPrintShadow());
            pdfRequest.setGrayscale(request.isConvertToGrayscale());

            pdfRequest.setApplyPdfProps(false);
            pdfRequest.setApplyLetterhead(true);
            pdfRequest.setForPrinting(true);

            pdfRequest.setPrintDuplex(request.isDuplex());
            pdfRequest.setPrintNup(request.getNup());

            pdfRequest.setForPrintingFillerPages(
                    request.isDuplex() || request.getNup() > 0);

            final PdfCreateInfo createInfo = outputProducer()
                    .generatePdf(pdfRequest, uuidPageCount, docLog);

            pdfFileToPrint = createInfo.getPdfFile();

            docLogService().collectData4DocOut(lockedUser, docLog, createInfo,
                    uuidPageCount);

            // Print
            proxyPrint(lockedUser, request, docLog, createInfo);

        } catch (LetterheadNotFoundException | PostScriptDrmException
                | IOException e) {

            throw new SpException(e.getMessage());

        } finally {

            if (pdfFileToPrint != null && pdfFileToPrint.exists()) {

                if (pdfFileToPrint.delete()) {

                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace(
                                "deleted temp file [" + pdfFileToPrint + "]");
                    }

                } else {
                    LOGGER.error("delete of temp file [" + pdfFileToPrint
                            + "] FAILED");
                }
            }
        }
    }

    /**
     * Gets the printerName from the printer cache, and prints the offered job
     * with parameters and options.
     * <p>
     * NOTE: page ranges are not relevant, since they already filtered into the
     * PDF document.
     * </p>
     *
     * @param request
     *            The {@link AbstractProxyPrintReq}.
     * @param user
     *            The user (owner of the print job).
     * @param createInfo
     *            The {@link PdfCreateInfo} with the PDF file to print.
     * @param docLog
     *            The object to collect print data on.
     * @return {@code true} when printer was found, {@code false} when printer
     *         is no longer valid (because not found in cache, or when it is
     *         logically deleted or disabled).
     * @throws IppConnectException
     *             When IPP connect error.
     */
    private boolean print(final AbstractProxyPrintReq request,
            final String user, final PdfCreateInfo createInfo,
            final DocLog docLog) throws IppConnectException {

        final JsonProxyPrinter printer =
                this.getJsonProxyPrinterCopy(request.getPrinterName());

        if (printer == null) {
            return false;
        }

        if (printer.getDbPrinter().getDisabled()
                || printer.getDbPrinter().getDeleted()) {
            return false;
        }

        this.printPdf(request, printer, user, createInfo, docLog);

        return true;
    }

    /**
     * Prints a file and logs the event.
     *
     * @param request
     *            The {@link AbstractProxyPrintReq}.
     * @param printer
     *            The printer object.
     * @param user
     *            The requesting user.
     * @param createInfo
     *            The {@link PdfCreateInfo} with the file to print.
     * @param docLog
     *            The documentation object to log the event.
     * @throws IppConnectException
     *             When IPP connection error.
     */
    protected abstract void printPdf(final AbstractProxyPrintReq request,
            final JsonProxyPrinter printer, final String user,
            final PdfCreateInfo createInfo, final DocLog docLog)
            throws IppConnectException;

    /**
     * Return a localized string.
     *
     * @param key
     *            The key of the string.
     * @return The localized string.
     */
    protected final String localize(final String key) {
        return Messages.getMessage(getClass(), key, null);
    }

    /**
     * Return a localized string.
     *
     * @param locale
     *            The Locale
     * @param key
     *            The key of the string.
     * @return The localized string.
     */
    protected final String localize(final Locale locale, final String key) {
        return Messages.getMessage(getClass(), locale, key);
    }

    /**
     *
     * @param locale
     *            The {@link Locale}.
     * @param key
     *            The key of the string.
     * @param dfault
     *            The default value.
     * @return The localized string.
     */
    protected final String localizeWithDefault(final Locale locale,
            final String key, final String dfault) {
        if (Messages.containsKey(getClass(), key, locale)) {
            return Messages.getMessage(getClass(), locale, key);
        }
        return dfault;
    }

    @Override
    public final Printer getValidateProxyPrinterAccess(final User user,
            final String printerName, final Date refDate)
            throws ProxyPrintException {

        final Printer printer = printerDAO().findByName(printerName);

        /*
         * INVARIANT: printer MUST exist.
         */
        if (printer == null) {
            throw new SpException("Printer [" + printerName + "] not found");
        }

        /*
         * INVARIANT: printer MUST be enabled.
         */
        if (printer.getDisabled()) {
            throw new ProxyPrintException("Proxy printer ["
                    + printer.getPrinterName() + "] is disabled");
        }

        /*
         * INVARIANT: User MUST be enabled to print.
         */
        if (userService().isUserPrintOutDisabled(user, refDate)) {
            throw new ProxyPrintException(
                    localize("msg-user-print-out-disabled"));
        }

        /*
         * INVARIANT: User MUST be have access to printer.
         */
        if (!printerService().isPrinterAccessGranted(printer, user)) {
            throw new ProxyPrintException(
                    localize("msg-user-print-out-access-denied"));
        }

        return printer;
    }

    @Override
    public final boolean isLocalPrinter(final URI uriPrinter) {
        return uriPrinter.getHost().equals(urlDefaultServer.getHost());
    }

    @Override
    public final Boolean isLocalPrinter(final String cupsPrinterName) {

        final JsonProxyPrinter proxyPrinter =
                this.getCachedPrinter(cupsPrinterName);

        final Boolean isLocal;

        if (proxyPrinter == null) {
            isLocal = null;
        } else {
            isLocal = isLocalPrinter(proxyPrinter.getPrinterUri());
        }

        return isLocal;
    }

    @Override
    public final List<JsonProxyPrinterOptChoice>
            getMediaChoices(final String printerName) {

        final JsonProxyPrinter proxyPrinter = getCachedPrinter(printerName);

        if (proxyPrinter != null) {

            for (final JsonProxyPrinterOptGroup group : proxyPrinter
                    .getGroups()) {

                for (final JsonProxyPrinterOpt option : group.getOptions()) {
                    if (option.getKeyword()
                            .equals(IppDictJobTemplateAttr.ATTR_MEDIA)) {
                        return option.getChoices();
                    }
                }
            }
        }

        return new ArrayList<JsonProxyPrinterOptChoice>();
    }

    @Override
    public final List<JsonProxyPrinterOptChoice>
            getMediaChoices(final String printerName, final Locale locale) {

        final List<JsonProxyPrinterOptChoice> list =
                this.getMediaChoices(printerName);
        this.localizePrinterOptChoices(locale,
                IppDictJobTemplateAttr.ATTR_MEDIA, list);
        return list;
    }

    @Override
    public final Map<String, JsonProxyPrinterOpt>
            getOptionsLookup(final String printerName) {

        final Map<String, JsonProxyPrinterOpt> lookup;

        final JsonProxyPrinter proxyPrinter = getCachedPrinter(printerName);

        if (proxyPrinter == null) {
            lookup = new HashMap<>();
        } else {
            lookup = proxyPrinter.getOptionsLookup();
        }

        return lookup;
    }

    @Override
    public final AbstractJsonRpcMessage readSnmp(final ParamsPrinterSnmp params)
            throws SnmpConnectException {

        final String printerName = params.getPrinterName();

        String host = null;

        if (printerName != null) {

            final JsonProxyPrinter printer = this.getCachedPrinter(printerName);

            /*
             * INVARIANT: printer MUST present in cache.
             */
            if (printer == null) {
                return JsonRpcMethodError.createBasicError(Code.INVALID_REQUEST,
                        "Printer [" + printerName + "] is unknown.", null);
            }

            final URI printerUri = printer.getPrinterUri();

            if (printerUri != null) {

                final String scheme = printerUri.getScheme();

                if (scheme != null && (scheme.endsWith("socket")
                        || scheme.equalsIgnoreCase("ipp"))) {
                    host = printerUri.getHost();
                }
            }

        } else {
            host = params.getHost();
        }

        /*
         * INVARIANT: host MUST be present.
         */
        if (host == null) {
            return JsonRpcMethodError.createBasicError(Code.INVALID_REQUEST,
                    "No host name.", null);
        }

        //
        final int port;

        if (params.getPort() == null) {
            port = SnmpClientSession.DEFAULT_PORT_READ;
        } else {
            port = Integer.valueOf(params.getPort()).intValue();
        }

        //
        final String community;

        if (params.getCommunity() == null) {
            community = SnmpClientSession.DEFAULT_COMMUNITY;
        } else {
            community = params.getCommunity();
        }

        //
        final PrinterSnmpDto dto = PrinterSnmpReader.read(host, port, community,
                params.getVersion());

        final ResultPrinterSnmp data = new ResultPrinterSnmp();

        // data.setAttributes(dto.asAttributes());
        data.setAttributes(new ArrayList<ResultAttribute>());

        try {
            data.getAttributes().add(0,
                    new ResultAttribute("json", dto.stringifyPrettyPrinted()));
        } catch (IOException e) {
            throw new SpException(e.getMessage());
        }

        if (params.getVersion() != null) {
            data.getAttributes().add(0, new ResultAttribute("SNMP Version",
                    params.getVersion().getCmdLineOption()));
        }
        data.getAttributes().add(0,
                new ResultAttribute("Community", community));
        data.getAttributes().add(0,
                new ResultAttribute("Port", String.valueOf(port)));
        data.getAttributes().add(0, new ResultAttribute("Host", host));

        for (final ResultAttribute attr : data.getAttributes()) {
            if (attr.getValue() == null) {
                attr.setValue("?");
            }
        }

        return JsonRpcMethodResult.createResult(data);
    }

    @Override
    public final Set<String> validateContraints(
            final JsonProxyPrinter proxyPrinter,
            final Map<String, String> ippOptions) {

        final Set<String> keywords = new HashSet<>();

        if (ConfigManager.instance()
                .isConfigValue(Key.IPP_EXT_CONSTRAINT_BOOKLET_ENABLE)) {

            validateContraints(ippOptions,
                    StandardRuleConstraintList.INSTANCE.getRulesBooklet(),
                    keywords);
        }

        if (proxyPrinter.hasCustomRulesConstraint()) {
            validateContraints(ippOptions,
                    proxyPrinter.getCustomRulesConstraint(), keywords);
        }
        return keywords;
    }

    /**
     * Validates IPP choices according to constraints.
     *
     * @param ippOptions
     *            The IPP attribute key/choice pairs.
     * @param rules
     *            The constraint rules.
     * @param keywords
     *            The {@link Set} to append conflicting IPP option keywords on.
     * @return The {@link Set} with conflicting IPP option keywords.
     */
    public final Set<String> validateContraints(
            final Map<String, String> ippOptions,
            final List<IppRuleConstraint> rules, final Set<String> keywords) {

        for (final IppRuleConstraint rule : rules) {
            if (rule.doesRuleApply(ippOptions)) {
                for (final Pair<String, String> pair : rule
                        .getIppContraints()) {
                    keywords.add(pair.getKey());
                }
            }
        }
        return keywords;
    }

    @Override
    public final String validateContraintsMsg(
            final JsonProxyPrinter proxyPrinter,
            final Map<String, String> ippOptions, final Locale locale) {

        final Set<String> conflictingIppKeywords =
                this.validateContraints(proxyPrinter, ippOptions);

        if (conflictingIppKeywords.isEmpty()) {
            return null;
        }

        final StringBuilder builder = new StringBuilder();

        for (final String attrKeyword : conflictingIppKeywords) {
            builder.append("\"")
                    .append(this.localizePrinterOpt(locale, attrKeyword))
                    .append("\"").append(", ");
        }

        return localize(locale, "msg-user-print-out-incompatible-options",
                StringUtils.removeEnd(builder.toString(), ", "));
    }

    @Override
    public final String validateCustomCostRules(
            final JsonProxyPrinter proxyPrinter,
            final Map<String, String> ippOptions, final Locale locale) {

        /*
         * Media: if cost rules are present, a rule MUST be found.
         */
        if (proxyPrinter.hasCustomCostRulesMedia()) {

            final BigDecimal cost =
                    proxyPrinter.calcCustomCostMedia(ippOptions);

            if (cost == null || cost.compareTo(BigDecimal.ZERO) == 0) {

                final String ippKeyword =
                        IppDictJobTemplateAttr.ATTR_MEDIA_TYPE;

                return localize(locale,
                        "msg-user-print-out-validation-media-warning",
                        String.format("\"%s : %s\"",
                                this.localizePrinterOpt(locale, ippKeyword),
                                this.localizePrinterOptValue(locale, ippKeyword,
                                        ippOptions.get(ippKeyword))));
            }
        }

        final StringBuilder msg = new StringBuilder();

        /*
         * Copy Sheet Rules are NOT used to validate IPP options.
         */

        /*
         * If Copy Cost Rules are present, AND a Job Ticket Copy option or
         * Custom finishing option is chosen, a cost rule MUST be present.
         *
         * IMPORTANT: this validation is deprecated and is replaced by
         * SPConstraint.
         */
        if (proxyPrinter.hasCustomCostRulesCopy()) {

            /*
             * Collect all Job Ticket Copy options and Custom finishing options
             * with their NONE choice.
             */
            final List<String[]> copyOptionsNone = new ArrayList<>();

            for (final String[] attrArray : IppDictJobTemplateAttr.JOBTICKET_ATTR_COPY_V_NONE) {
                copyOptionsNone.add(attrArray);
            }

            for (final Entry<String, String> entry : ippOptions.entrySet()) {
                if (IppDictJobTemplateAttr.isCustomExtAttr(entry.getKey())) {
                    copyOptionsNone.add(new String[] { entry.getKey(),
                            IppKeyword.ORG_SAVAPAGE_EXT_ATTR_NONE });
                }
            }

            // Validate.
            for (final String[] attrArray : copyOptionsNone) {

                final String ippKey = attrArray[0];
                final String ippChoice = ippOptions.get(ippKey);

                // Skip when option not found, or NONE choice?
                if (ippChoice == null || ippChoice.equals(attrArray[1])) {
                    continue;
                }

                final Pair<String, String> option =
                        new ImmutablePair<>(ippKey, ippChoice);

                final Boolean isValid = proxyPrinter
                        .isCustomCopyCostOptionValid(option, ippOptions);

                if (isValid == null || isValid.booleanValue()) {
                    continue;
                }

                if (msg.length() > 0) {
                    msg.append(", ");
                }

                msg.append("\"").append(this.localizePrinterOpt(locale, ippKey))
                        .append(" : ")
                        .append(this.localizePrinterOptValue(locale, ippKey,
                                ippChoice))
                        .append("\"");
            }
        }

        if (msg.length() > 0) {
            return localize(locale,
                    "msg-user-print-out-validation-finishing-warning",
                    msg.toString());
        }

        return null;
    }

}
