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
 * An NFC Card associated with a user.
 * <p>
 * NOTE: card numbers need to be delivered (and are thereby stored) LOWER CASE.
 * </p>
 *
 * @author Rijk Ravestein
 *
 */
@Entity
@Table(name = UserCard.TABLE_NAME)
public class UserCard extends org.savapage.core.jpa.Entity {

    /**
     *
     */
    public static final String TABLE_NAME = "tbl_user_card";

    @Id
    @Column(name = "user_card_id")
    @TableGenerator(name = "userCardPropGen", table = Sequence.TABLE_NAME,
            //
            pkColumnName = Sequence.COL_SEQUENCE_NAME,
            valueColumnName = Sequence.COL_SEQUENCE_NEXT_VALUE,
            //
            pkColumnValue = TABLE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE,
            generator = "userCardPropGen")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "index_number", nullable = false)
    private Integer indexNumber;

    @Column(name = "card_number", length = 100, nullable = false)
    private String number;

    @Column(name = "card_name", length = 255, nullable = true)
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getIndexNumber() {
        return indexNumber;
    }

    public void setIndexNumber(Integer indexNumber) {
        this.indexNumber = indexNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    /**
     * IMPORTANT: Number MUST be set as LOWER CASE.
     *
     * @param number
     */
    public void setNumber(String number) {
        this.number = number;
    }

}
