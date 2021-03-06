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
package org.savapage.core.jpa.xml;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.savapage.core.jpa.schema.AccountV01;

/**
 *
 * @author Rijk Ravestein
 *
 */
@Entity
@Table(name = AccountV01.TABLE_NAME)
public class XAccountV01 extends XEntityVersion {

    @Id
    @Column(name = "account_id")
    private Long id;

    /**
     * Enum: USER | SHARED | USER-001 ... USER-002
     */
    @Column(name = "account_type", length = 10, nullable = false)
    private String accountType;

    /**
     *
     */
    @Column(name = "account_name", length = 255, nullable = false)
    private String name;

    @Column(name = "account_name_lower", length = 255, nullable = false)
    private String nameLower;

    /**
     *
     */
    @Column(name = "balance", nullable = false, precision = 16, scale = 6)
    private BigDecimal balance;

    /**
     *
     */
    @Column(name = "restricted", nullable = false)
    private Boolean restricted = Boolean.FALSE;

    /**
     *
     */
    @Column(name = "overdraft", nullable = false, precision = 16, scale = 6)
    private BigDecimal overdraft;

    /**
     *
     */
    @Column(name = "pin", length = 50, nullable = true)
    private String pin;

    /**
     *
     */
    @Column(name = "use_global_overdraft", nullable = false)
    private Boolean useGlobalOverdraft = Boolean.TRUE;

    /**
     *
     */
    @Column(name = "notes", length = 2000, nullable = true)
    private String notes;

    /**
     * Logical delete
     */
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = Boolean.FALSE;

    @Column(name = "deleted_date", nullable = true)
    private Date deletedDate;

    /**
     *
     */
    @Column(name = "created_date", nullable = false)
    private Date createdDate;

    @Column(name = "created_by", length = 50, nullable = false, unique = false,
            insertable = true, updatable = true)
    private String createdBy;

    /**
     *
     */
    @Column(name = "modified_date", nullable = true)
    private Date modifiedDate;

    @Column(name = "modified_by", length = 50, nullable = true)
    private String modifiedBy;

    /**
     * The parent account.
     */
    @Column(name = "parent_id")
    private Long parent;

    /**
     *
     */
    @Column(name = "sub_name", length = 255, nullable = true)
    private String subName;

    @Column(name = "sub_name_lower", length = 255, nullable = true)
    private String subNameLower;

    @Column(name = "sub_pin", length = 50, nullable = true)
    private String subPin;

    /**
     *
     */
    @Column(name = "disabled", nullable = false)
    private Boolean disabled = Boolean.FALSE;

    /**
     *
     */
    @Column(name = "disabled_until", nullable = true)
    private Date disabledUntil;

    /**
     * COMMENT_OPTIONAL | COMMENT_REQUIRED | COMMENT_NOT_ALLOWED
     */
    @Column(name = "comments", length = 20, nullable = false)
    private String comments;

    /**
     * <ul>
     * <li>User choice (defaults to invoice) : USER_CHOICE_ON</li>
     * <li>User choice (defaults to not invoice) : USER_CHOICE_OFF</li>
     * <li>Always show on invoice : ALWAYS_ON</li>
     * <li>Never show on invoice : ALWAYS_OFF</li>
     * </ul>
     */
    @Column(name = "invoicing", length = 20, nullable = false)
    private String invoicing;

    @Override
    public final String xmlName() {
        return "Account";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameLower() {
        return nameLower;
    }

    public void setNameLower(String nameLower) {
        this.nameLower = nameLower;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Boolean getRestricted() {
        return restricted;
    }

    public void setRestricted(Boolean restricted) {
        this.restricted = restricted;
    }

    public BigDecimal getOverdraft() {
        return overdraft;
    }

    public void setOverdraft(BigDecimal overdraft) {
        this.overdraft = overdraft;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public Boolean getUseGlobalOverdraft() {
        return useGlobalOverdraft;
    }

    public void setUseGlobalOverdraft(Boolean useGlobalOverdraft) {
        this.useGlobalOverdraft = useGlobalOverdraft;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

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

    public Long getParent() {
        return parent;
    }

    public void setParent(Long parent) {
        this.parent = parent;
    }

    public String getSubName() {
        return subName;
    }

    public void setSubName(String subName) {
        this.subName = subName;
    }

    public String getSubNameLower() {
        return subNameLower;
    }

    public void setSubNameLower(String subNameLower) {
        this.subNameLower = subNameLower;
    }

    public String getSubPin() {
        return subPin;
    }

    public void setSubPin(String subPin) {
        this.subPin = subPin;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public Date getDisabledUntil() {
        return disabledUntil;
    }

    public void setDisabledUntil(Date disabledUntil) {
        this.disabledUntil = disabledUntil;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getInvoicing() {
        return invoicing;
    }

    public void setInvoicing(String invoicing) {
        this.invoicing = invoicing;
    }

}
