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

import org.savapage.core.jpa.tools.DbTools;

/**
 * Parent class for '@Entity' annotated classes.
 * <p>
 * NOTES FOR DEVELOPERS.
 * </p>
 * <ol>
 * <li>Each ancestor of this class should be added to {@link DbTools}
 * entityClasses4Schema</li>
 *
 * <li>You cannot JPA annotate a '@Column' to have a default value. Therefore
 * declare defaults to the instance variables.</li>
 * <li>Bean properties which are NOT part of the database must be annotated with
 * '@Transient'.</li>
 *
 * <li>NOTE: Since {@link javax.persistence.UniqueConstraint},
 * {@link javax.persistence.Index} and {@link javax.persistence.ForeignKey}
 * annotation are exclusively needed for schema (DDL) generation they are NOT
 * needed here.</li>
 *
 * </ol>
 *
 * @author Rijk Ravestein
 *
 */
public class Entity {

    public static final String ACTOR_ADMIN = "[admin]";
    public static final String ACTOR_SYSTEM = "[system]";
    public static final String ACTOR_INSTALL = "[install]";
    public static final String ACTOR_SYSTEM_API = ACTOR_SYSTEM + " (api)";

    public static final int DECIMAL_PRECISION_6 = 6;
    public static final int DECIMAL_PRECISION_8 = 8;
    public static final int DECIMAL_PRECISION_10 = 10;
    public static final int DECIMAL_PRECISION_16 = 16;

    public static final int DECIMAL_SCALE_2 = 2;
    public static final int DECIMAL_SCALE_6 = 6;
    public static final int DECIMAL_SCALE_8 = 8;

    /**
     * Entity attribute for (sequence) ID.
     */
    public static final String ATTR_ID = "id";

    protected Entity() {
    }
}
