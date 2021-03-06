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
package org.savapage.core.ipp.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.savapage.core.ipp.attribute.AbstractIppDict;
import org.savapage.core.ipp.attribute.IppAttrGroup;
import org.savapage.core.ipp.attribute.IppDictOperationAttr;

/**
 *
 * @author Rijk Ravestein
 *
 */
public final class IppReqCupsGetPpd extends IppReqCommon {

    /** */
    private final URI printerURI;

    /**
     * Creates Operation Attributes.
     *
     * @return The group.
     */
    private IppAttrGroup createOperationAttributes() {

        final IppAttrGroup group = createOperationGroup();

        final AbstractIppDict dict = IppDictOperationAttr.instance();

        group.add(dict.getAttr(IppDictOperationAttr.ATTR_PRINTER_URI),
                this.printerURI.toString());

        return group;
    }

    /**
     *
     * @param printerURI
     *            The URI of the printer.
     */
    public IppReqCupsGetPpd(final URI printerURI) {
        this.printerURI = printerURI;
    }

    @Override
    public List<IppAttrGroup> build() {

        final List<IppAttrGroup> attrGroups = new ArrayList<>();

        /*
         * Group 1: Operation Attributes
         */
        attrGroups.add(this.createOperationAttributes());

        return attrGroups;
    }

}
