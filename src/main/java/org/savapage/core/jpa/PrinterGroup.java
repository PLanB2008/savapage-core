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
@Table(name = PrinterGroup.TABLE_NAME)
public class PrinterGroup extends org.savapage.core.jpa.Entity {

    /** */
    public static final String TABLE_NAME = "tbl_printer_group";

    /** */
    @Id
    @Column(name = "printer_group_id")
    @TableGenerator(name = "printerGroupPropGen", table = Sequence.TABLE_NAME,
            //
            pkColumnName = Sequence.COL_SEQUENCE_NAME,
            valueColumnName = Sequence.COL_SEQUENCE_NEXT_VALUE,
            //
            pkColumnValue = TABLE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE,
            generator = "printerGroupPropGen")
    private Long id;

    /**
     * The unique lower-case group name.
     */
    @Column(name = "group_name", length = 255, nullable = false)
    private String groupName;

    /**
     * The unique case-sensitive group name used for display.
     */
    @Column(name = "display_name", length = 255, nullable = false)
    private String displayName;

    /**
     * The LAZY Device list.
     */
    @OneToMany(targetEntity = Device.class, mappedBy = "printerGroup",
            fetch = FetchType.LAZY)
    private List<Device> devices;

    /**
     * The LAZY member list.
     */
    @OneToMany(targetEntity = PrinterGroupMember.class, mappedBy = "group",
            fetch = FetchType.LAZY)
    private List<PrinterGroupMember> members;

    /**
     *
     */
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

    /**
     * @return The lower-case group name.
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Sets the group name (converted to lower-case value).
     *
     * @param name
     *            The unique <i>case-insensitive</i> group name.
     */
    public void setGroupName(final String name) {
        this.groupName = name.toLowerCase();
    }

    /**
     * @return The unique case-sensitive group name used for display.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param name
     *            The unique case-sensitive group name used for display.
     */
    public void setDisplayName(final String name) {
        this.displayName = name;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    public List<PrinterGroupMember> getMembers() {
        return members;
    }

    public void setMembers(List<PrinterGroupMember> members) {
        this.members = members;
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
