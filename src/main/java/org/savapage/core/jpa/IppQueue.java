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
package org.savapage.core.jpa;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

/**
 *
 * @author Rijk Ravestein
 *
 */
@Entity
@Table(name = IppQueue.TABLE_NAME)
public class IppQueue extends org.savapage.core.jpa.Entity {

    /**
     *
     */
    public static final String TABLE_NAME = "tbl_ipp_queue";

    @Id
    @Column(name = "queue_id")
    @TableGenerator(name = "ippQueuePropGen", table = Sequence.TABLE_NAME,
            pkColumnName = Sequence.COL_SEQUENCE_NAME,
            valueColumnName = Sequence.COL_SEQUENCE_NEXT_VALUE,
            pkColumnValue = TABLE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE,
            generator = "ippQueuePropGen")
    private Long id;

    @Column(name = "url_path", length = 255, nullable = false,
            insertable = true, updatable = true)
    private String urlPath;

    @Column(name = "ip_allowed", length = 255, nullable = true,
            insertable = true, updatable = true)
    private String ipAllowed;

    @Column(name = "trusted", nullable = false, insertable = true,
            updatable = true)
    private Boolean trusted = Boolean.TRUE;

    @Column(name = "lpd_enabled", nullable = false)
    private Boolean lpdEnabled = Boolean.FALSE;

    //
    @Column(name = "deleted", nullable = false, insertable = true,
            updatable = true)
    private Boolean deleted = Boolean.FALSE;

    @Column(name = "deleted_date", nullable = true, insertable = true,
            updatable = true)
    private Date deletedDate;

    @Column(name = "disabled", nullable = false, insertable = true,
            updatable = true)
    private Boolean disabled = Boolean.FALSE;

    @Column(name = "disabled_date", nullable = true, insertable = true,
            updatable = true)
    private Date disabledDate;

    @Column(name = "created_date", nullable = false, insertable = true,
            updatable = true)
    private Date createdDate;

    @Column(name = "created_by", length = 50, nullable = false,
            insertable = true, updatable = true)
    private String createdBy;

    @Column(name = "modified_date", nullable = true, insertable = true,
            updatable = true)
    private Date modifiedDate;

    @Column(name = "modified_by", length = 50, nullable = true,
            insertable = true, updatable = true)
    private String modifiedBy;

    @Column(name = "reset_by", length = 50, nullable = true, insertable = true,
            updatable = true)
    private String resetBy;

    @Column(name = "reset_date", nullable = true, insertable = true,
            updatable = true)
    private Date resetDate;

    @Column(name = "total_jobs", nullable = false, insertable = true,
            updatable = true)
    private Integer totalJobs = 0;

    @Column(name = "total_pages", nullable = false, insertable = true,
            updatable = true)
    private Integer totalPages = 0;

    @Column(name = "total_bytes", nullable = false, insertable = true,
            updatable = true)
    private Long totalBytes = 0L;

    @Column(name = "last_usage_date", nullable = true, insertable = true,
            updatable = true)
    private Date lastUsageDate;

    /**
     * The LAZY PrintIn list.
     */
    @OneToMany(targetEntity = PrintIn.class, mappedBy = "queue",
            cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PrintIn> printsIn;

    /**
     * The LAZY IppQueueAttr list.
     */
    @OneToMany(targetEntity = IppQueueAttr.class, mappedBy = "queue",
            cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IppQueueAttr> attributes;

    /**
     *
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getUrlPath() {
        return urlPath;
    }

    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    public String getIpAllowed() {
        return ipAllowed;
    }

    public void setIpAllowed(String ipAllowed) {
        this.ipAllowed = ipAllowed;
    }

    public Boolean getTrusted() {
        return trusted;
    }

    public void setTrusted(Boolean trusted) {
        this.trusted = trusted;
    }

    public Boolean getLpdEnabled() {
        return lpdEnabled;
    }

    public void setLpdEnabled(Boolean lpdEnabled) {
        this.lpdEnabled = lpdEnabled;
    }

    // ---------------
    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Date getDeletedDate() {
        return deletedDate;
    }

    public void setDeletedDate(Date deletedDate) {
        this.deletedDate = deletedDate;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public Date getDisabledDate() {
        return disabledDate;
    }

    public void setDisabledDate(Date disabledDate) {
        this.disabledDate = disabledDate;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public String getResetBy() {
        return resetBy;
    }

    public void setResetBy(String resetBy) {
        this.resetBy = resetBy;
    }

    public Date getResetDate() {
        return resetDate;
    }

    public void setResetDate(Date resetDate) {
        this.resetDate = resetDate;
    }

    public Integer getTotalJobs() {
        return totalJobs;
    }

    public void setTotalJobs(Integer totalJobs) {
        this.totalJobs = totalJobs;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(Long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public Date getLastUsageDate() {
        return lastUsageDate;
    }

    public void setLastUsageDate(Date lastUsageDate) {
        this.lastUsageDate = lastUsageDate;
    }

    public List<PrintIn> getPrintsIn() {
        return printsIn;
    }

    public void setPrintsIn(List<PrintIn> printsIn) {
        this.printsIn = printsIn;
    }

    public List<IppQueueAttr> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<IppQueueAttr> attributes) {
        this.attributes = attributes;
    }

}
