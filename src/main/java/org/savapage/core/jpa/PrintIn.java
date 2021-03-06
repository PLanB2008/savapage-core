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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.print.attribute.standard.MediaSizeName;

/**
 * IPP Print Input Document.
 *
 * @author Rijk Ravestein
 *
 */
@Entity
@Table(name = PrintIn.TABLE_NAME)
public class PrintIn extends org.savapage.core.jpa.Entity {

    /**
     *
     */
    public static final String TABLE_NAME = "tbl_print_in";

    @Id
    @Column(name = "print_in_id")
    @TableGenerator(name = "printinPropGen", table = Sequence.TABLE_NAME,
            pkColumnName = Sequence.COL_SEQUENCE_NAME,
            valueColumnName = Sequence.COL_SEQUENCE_NEXT_VALUE,
            pkColumnValue = TABLE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE,
            generator = "printinPropGen")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "queue_id", nullable = false)
    private IppQueue queue;

    /**
     * The RFC2911 IPP media name as in {@link MediaSizeName#getName()}.
     */
    @Column(name = "paper_size", length = 20, nullable = true,
            insertable = true, updatable = true)
    private String paperSize;

    @Column(name = "paper_height_mm", nullable = true, insertable = true,
            updatable = true)
    private Integer paperHeight;

    @Column(name = "paper_width_mm", nullable = true, insertable = true,
            updatable = true)
    private Integer paperWidth;

    @Column(name = "printed", nullable = false, insertable = true,
            updatable = true)
    private Boolean printed;

    @Column(name = "denied_reason", length = 30, nullable = true,
            insertable = true, updatable = true)
    private String deniedReason;

    @OneToOne(mappedBy = "printIn", cascade = { CascadeType.ALL },
            fetch = FetchType.EAGER, optional = false)
    private DocIn docIn;

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

    public IppQueue getQueue() {
        return queue;
    }

    public void setQueue(IppQueue queue) {
        this.queue = queue;
    }

    /**
     *
     * @return the IPP RFC2911 "media" name.
     */
    public String getPaperSize() {
        return paperSize;
    }

    /**
     *
     * @param paperSize
     *            the IPP RFC2911 "media" name.
     */
    public void setPaperSize(String paperSize) {
        this.paperSize = paperSize;
    }

    public Integer getPaperHeight() {
        return paperHeight;
    }

    public void setPaperHeight(Integer paperHeight) {
        this.paperHeight = paperHeight;
    }

    public Integer getPaperWidth() {
        return paperWidth;
    }

    public void setPaperWidth(Integer paperWidth) {
        this.paperWidth = paperWidth;
    }

    public Boolean getPrinted() {
        return printed;
    }

    public void setPrinted(Boolean printed) {
        this.printed = printed;
    }

    public String getDeniedReason() {
        return deniedReason;
    }

    public void setDeniedReason(String deniedReason) {
        this.deniedReason = deniedReason;
    }

    public DocIn getDocIn() {
        return docIn;
    }

    public void setDocIn(DocIn docIn) {
        this.docIn = docIn;
    }

}
