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
import javax.persistence.UniqueConstraint;

/**
 *
 * @author Rijk Ravestein
 *
 */
@Entity
@Table(name = PrinterAttrV01.TABLE_NAME, indexes = { //
        @Index(name = "ix_printer_attr_1", columnList = "printer_id") },
        uniqueConstraints = { //
                @UniqueConstraint(name = "uc_printer_attr_1",
                        columnNames = { "printer_id", "attrib_name" }) //
        })
public class PrinterAttrV01 implements SchemaEntityVersion {

    public static final String TABLE_NAME = "tbl_printer_attr";

    @Id
    @Column(name = "printer_attr_id")
    @TableGenerator(name = "printerAttrPropGen", table = SequenceV01.TABLE_NAME,
            pkColumnName = SequenceV01.COL_SEQUENCE_NAME,
            valueColumnName = SequenceV01.COL_SEQUENCE_NEXT_VALUE,
            pkColumnValue = TABLE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE,
            generator = "printerAttrPropGen")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "printer_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_PRINTER_ATTR_TO_PRINTER"))
    private PrinterV01 printer;

    @Column(name = "attrib_name", length = 255, nullable = false,
            insertable = true, updatable = false)
    private String name;

    @Column(name = "attrib_value", length = 2000, nullable = true,
            insertable = true, updatable = true)
    private String value;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public PrinterV01 getPrinter() {
        return printer;
    }

    public void setPrinter(PrinterV01 printer) {
        this.printer = printer;
    }

}
