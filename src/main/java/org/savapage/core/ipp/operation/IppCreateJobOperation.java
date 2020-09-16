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
package org.savapage.core.ipp.operation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.savapage.core.concurrent.ReadWriteLockEnum;
import org.savapage.core.dao.enums.ExternalSupplierEnum;
import org.savapage.core.dao.enums.ExternalSupplierStatusEnum;
import org.savapage.core.ipp.IppProcessingException;
import org.savapage.core.ipp.attribute.syntax.IppJobState;
import org.savapage.core.ipp.helpers.IppPrintInData;
import org.savapage.core.jpa.IppQueue;
import org.savapage.core.services.DocLogService;
import org.savapage.core.services.ServiceContext;
import org.savapage.core.services.helpers.ExternalSupplierInfo;

/**
 *
 * @author Rijk Ravestein
 *
 */
public final class IppCreateJobOperation extends AbstractIppJobOperation {

    /** */
    private static final DocLogService DOC_LOG_SERVICE =
            ServiceContext.getServiceFactory().getDocLogService();

    /**
     *
     * @author Rijk Ravestein
     *
     */
    private static final class IppCreateJobReq extends AbstractIppPrintJobReq {

        /** */
        IppCreateJobReq() {
            super();
        }

        @Override
        protected String getPrintInJobName() {
            return this.getJobName();
        }

        @Override
        protected boolean isJobIdGenerated() {
            return true;
        }

        @Override
        void process(final AbstractIppOperation operation,
                final InputStream istr) throws IOException {

            final ExternalSupplierInfo supplierInfo =
                    new ExternalSupplierInfo();

            supplierInfo.setSupplier(ExternalSupplierEnum.IPP_CLIENT);
            supplierInfo.setId(String.valueOf(this.getJobId()));
            supplierInfo
                    .setStatus(ExternalSupplierStatusEnum.PENDING.toString());

            final IppPrintInData data = new IppPrintInData();
            data.setIppVersion(operation.getIppVersion().getVersionKeyword());
            data.setAttrCreateJob(this.selectIppPrintInData());
            supplierInfo.setData(data);

            DOC_LOG_SERVICE.logIppCreateJob(this.getUserDb(), supplierInfo,
                    this.getPrintInJobName());
        }

        @Override
        protected IppStatusCode getResponseStatusCode() {
            return IppStatusCode.OK;
        }
    }

    /**
     *
     * @author Rijk Ravestein
     *
     */
    private static final class IppCreateJobRsp extends AbstractIppPrintJobRsp {
    }

    /**
     * @param queue
     *            The print queue.
     * @param authUser
     *            The authenticated user id associated with the IPP client. If
     *            {@code null} there is NO authenticated user.
     * @param isAuthUserIppRequester
     *            If {@code true}, the authUser overrules the IPP requesting
     *            user.
     * @param ctx
     *            The operation context.
     */
    public IppCreateJobOperation(final IppQueue queue, final String authUser,
            final boolean isAuthUserIppRequester,
            final IppOperationContext ctx) {

        super(queue, authUser, isAuthUserIppRequester, ctx,
                new IppCreateJobReq(), new IppCreateJobRsp());
    }

    @Override
    protected void process(final InputStream istr, final OutputStream ostr)
            throws IOException, IppProcessingException {

        /*
         * IMPORTANT: we want to give a response in ALL cases. When an exception
         * occurs, the response will act in such a way that the client will not
         * try again (because we assume that the exception will re-occur when
         * re-tried, leading to an end-less chain of print trials).
         */
        boolean isDbReadLock = false;

        /*
         * Step 0.
         */
        try {
            ReadWriteLockEnum.DATABASE_READONLY.tryReadLock();
            isDbReadLock = true;
        } catch (Exception e) {
            throw new IppProcessingException(
                    IppProcessingException.StateEnum.UNAVAILABLE,
                    e.getMessage());
        }

        try {

            /*
             * Step 1.
             */
            try {
                getRequest().processAttributes(this, istr);
            } catch (IOException e) {
                getRequest().setDeferredException(new IppProcessingException(
                        IppProcessingException.StateEnum.INTERNAL_ERROR,
                        e.getMessage()));
            }

            /*
             * Step 2: register as pending.
             */
            try {
                if (this.isAuthorized()) {
                    this.getRequest().process(this, istr);
                }
            } catch (IOException e) {
                this.getRequest()
                        .setDeferredException(new IppProcessingException(
                                IppProcessingException.StateEnum.INTERNAL_ERROR,
                                e.getMessage()));
            }

            /*
             * Step 3.
             */
            try {
                this.getResponse().process(this, getRequest(), ostr,
                        IppJobState.STATE_PROCESSING);
            } catch (IOException e) {
                this.getRequest()
                        .setDeferredException(new IppProcessingException(
                                IppProcessingException.StateEnum.INTERNAL_ERROR,
                                e.getMessage()));
            }

        } catch (Exception e) {
            this.getRequest()
                    .setDeferredException(new IppProcessingException(
                            IppProcessingException.StateEnum.INTERNAL_ERROR,
                            e.getMessage()));
        } finally {
            if (isDbReadLock) {
                ReadWriteLockEnum.DATABASE_READONLY.setReadLock(false);
            }
        }

        /*
         * Step 4: deferred exception? or not allowed to print?
         */
        getRequest().evaluateErrorState(this);

    }

}
