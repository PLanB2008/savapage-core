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
package org.savapage.core.services.helpers;

import org.savapage.core.pdf.SpPdfPageProps;

/**
 *
 * @author Datraverse B.V.
 *
 */
public class DocContentPrintInInfo {

    /**
     * .
     */
    private final boolean drmViolationDetected = false;

    /**
     * .
     */
    private boolean drmRestricted = false;

    /**
     * .
     */
    private String mimetype;

    /**
     * .
     */
    private String jobName;

    /**
     * .
     */
    private String logComment;

    /**
     * .
     */
    private String originatorIp;

    /**
     * .
     */
    private String originatorEmail;

    /**
     * .
     */
    private SpPdfPageProps pageProps;

    /**
     * .
     */
    private long jobBytes;

    /**
     * .
     */
    private java.util.UUID uuidJob;

    /**
     * .
     */
    private ExternalSupplierInfo supplierInfo;

    /**
     * .
     */
    private AccountTrxInfoSet accountTrxInfoSet;

    /**
     *
     * @return
     */
    public boolean isDrmRestricted() {
        return drmRestricted;
    }

    public void setDrmRestricted(boolean drmRestricted) {
        this.drmRestricted = drmRestricted;
    }

    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getLogComment() {
        return logComment;
    }

    public void setLogComment(String logComment) {
        this.logComment = logComment;
    }

    public String getOriginatorIp() {
        return originatorIp;
    }

    public void setOriginatorIp(String originatorIp) {
        this.originatorIp = originatorIp;
    }

    public String getOriginatorEmail() {
        return originatorEmail;
    }

    public void setOriginatorEmail(String originatorEmail) {
        this.originatorEmail = originatorEmail;
    }

    public SpPdfPageProps getPageProps() {
        return pageProps;
    }

    public void setPageProps(SpPdfPageProps pageProps) {
        this.pageProps = pageProps;
    }

    public long getJobBytes() {
        return jobBytes;
    }

    public void setJobBytes(long jobBytes) {
        this.jobBytes = jobBytes;
    }

    public java.util.UUID getUuidJob() {
        return uuidJob;
    }

    public void setUuidJob(java.util.UUID uuidJob) {
        this.uuidJob = uuidJob;
    }

    public boolean isDrmViolationDetected() {
        return drmViolationDetected;
    }

    public ExternalSupplierInfo getSupplierInfo() {
        return supplierInfo;
    }

    public void setSupplierInfo(ExternalSupplierInfo supplierInfo) {
        this.supplierInfo = supplierInfo;
    }

    public AccountTrxInfoSet getAccountTrxInfoSet() {
        return accountTrxInfoSet;
    }

    public void setAccountTrxInfoSet(AccountTrxInfoSet accountTrxInfoSet) {
        this.accountTrxInfoSet = accountTrxInfoSet;
    }

}
