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
package org.savapage.core.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

/**
 * Relation between {@link UserGroup} and {@link Account}.
 *
 * @author Rijk Ravestein
 *
 */
@Entity
@Table(name = UserGroupAccount.TABLE_NAME)
public class UserGroupAccount extends org.savapage.core.jpa.Entity {

    /**
     *
     */
    public static final String TABLE_NAME = "tbl_user_group_account";

    @Id
    @Column(name = "user_group_account_id")
    @TableGenerator(name = "userGroupAccountPropGen", table = Sequence.TABLE_NAME,
            //
            pkColumnName = "SEQUENCE_NAME",
            valueColumnName = "SEQUENCE_NEXT_VALUE",
            //
            pkColumnValue = TABLE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE,
            generator = "userGroupAccountPropGen")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_group_id", nullable = false)
    private UserGroup userGroup;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "created_date", nullable = false)
    private Date createdDate;

    @Column(name = "created_by", length = 50, nullable = false)
    private String createdBy;

    @Column(name = "modified_date")
    private Date modifiedDate;

    @Column(name = "modified_by", length = 50)
    private String modifiedBy;

    /**
     *
     * @return
     */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserGroup getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(UserGroup userGroup) {
        this.userGroup = userGroup;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
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

}