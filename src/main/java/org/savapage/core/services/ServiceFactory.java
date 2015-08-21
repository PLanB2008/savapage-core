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
package org.savapage.core.services;

/**
 *
 * @author Datraverse B.V.
 *
 */
public interface ServiceFactory {

    /**
     * Gets the {@link AccountingService} singleton.
     *
     * @return The singleton.
     */
    AccountingService getAccountingService();

    /**
     * Gets the {@link AppLogService} singleton.
     *
     * @return The singleton.
     */
    AppLogService getAppLogService();

    /**
     * Gets the {@link AccountVoucherService} singleton.
     *
     * @return The singleton.
     */
    AccountVoucherService getAccountVoucherService();

    /**
     * Gets the {@link DeviceService} singleton.
     *
     * @return The singleton.
     */
    DeviceService getDeviceService();

    /**
     * Gets the {@link AppLogService} singleton.
     *
     * @return The singleton.
     */
    DocLogService getDocLogService();

    /**
     * Gets the {@link EmailService} singleton.
     *
     * @return The singleton.
     */
    EmailService getEmailService();

    /**
     * Gets the {@link InboxService} singleton.
     *
     * @return The singleton.
     */
    InboxService getInboxService();

    /**
     * Gets the {@link OutboxService} singleton.
     *
     * @return The singleton.
     */
    OutboxService getOutboxService();

    /**
     * Gets the {@link PaperCutService} singleton.
     *
     * @return The singleton.
     */
    PaperCutService getPaperCutService();

    /**
     * Gets the {@link PrinterGroupService} singleton.
     *
     * @return The singleton.
     */
    PrinterGroupService getPrinterGroupService();

    /**
     * Gets the {@link PrinterService} singleton.
     *
     * @return The singleton.
     */
    PrinterService getPrinterService();

    /**
     * Gets the {@link ProxyPrintService} singleton.
     *
     * @return The singleton.
     */
    ProxyPrintService getProxyPrintService();

    /**
     * Gets the {@link QueueService} singleton.
     *
     * @return The singleton.
     */
    QueueService getQueueService();

    /**
     * Gets the {@link RfIdReaderService} singleton.
     *
     * @return The singleton.
     */
    RfIdReaderService getRfIdReaderService();

    /**
     * Gets the {@link SmartSchoolService} singleton.
     *
     * @return The singleton.
     */
    SmartSchoolService getSmartSchoolService();

    /**
     * Gets the {@link UserGroupService} singleton.
     *
     * @return The singleton.
     */
    UserGroupService getUserGroupService();

    /**
     * Gets the {@link UserService} singleton.
     *
     * @return The singleton.
     */
    UserService getUserService();

}
