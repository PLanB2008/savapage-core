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
package org.savapage.core.jpa.schema;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

/**
 * Relation between {@link PrinterV01} and {@link PrinterGroupV01}.
 *
 * @author Rijk Ravestein
 *
 */
@Entity
@Table(name = PrinterGroupMemberV01.TABLE_NAME, indexes = { //
        @Index(name = "ix_printer_group_member_1", columnList = "printer_id"),
        @Index(name = "ix_printer_group_member_2",
                columnList = "printer_group_id") })
public class PrinterGroupMemberV01 implements SchemaEntityVersion {

    public static final String TABLE_NAME = "tbl_printer_group_member";

    @Id
    @Column(name = "printer_group_member_id")
    @TableGenerator(name = "printerGroupMemberPropGen",
            table = SequenceV01.TABLE_NAME,
            //
            pkColumnName = SequenceV01.COL_SEQUENCE_NAME,
            valueColumnName = SequenceV01.COL_SEQUENCE_NEXT_VALUE,
            //
            pkColumnValue = TABLE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE,
            generator = "printerGroupMemberPropGen")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "printer_id", nullable = false, foreignKey = @ForeignKey(
            name = "FK_PRINTER_GROUP_MEMBER_TO_PRINTER"))
    private PrinterV01 printer;

    @ManyToOne
    @JoinColumn(name = "printer_group_id", nullable = false,
            foreignKey = @ForeignKey(
                    name = "FK_PRINTER_GROUP_MEMBER_TO_PRINTER_GROUP"))
    private PrinterGroupV01 group;

    @Column(name = "created_date", nullable = false)
    private Date createdDate;

    @Column(name = "created_by", length = 50, nullable = false)
    private String createdBy;

    @Column(name = "modified_date")
    private Date modifiedDate;

    @Column(name = "modified_by", length = 50)
    private String modifiedBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PrinterV01 getPrinter() {
        return printer;
    }

    public void setPrinter(PrinterV01 printer) {
        this.printer = printer;
    }

    public PrinterGroupV01 getGroup() {
        return group;
    }

    public void setGroup(PrinterGroupV01 group) {
        this.group = group;
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
