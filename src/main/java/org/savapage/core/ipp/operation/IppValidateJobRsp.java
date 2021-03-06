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
package org.savapage.core.ipp.operation;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.savapage.core.ipp.attribute.IppAttr;
import org.savapage.core.ipp.attribute.IppAttrGroup;
import org.savapage.core.ipp.attribute.IppAttrValue;
import org.savapage.core.ipp.attribute.syntax.IppText;

/**
 *
 * @author Rijk Ravestein
 *
 */
public class IppValidateJobRsp extends AbstractIppResponse {

    /**
     *
     * @param operation
     * @param ostr
     * @throws IOException
     */
    public final void process(final IppValidateJobOperation operation,
            final IppValidateJobReq request, final OutputStream ostr)
            throws IOException {

        List<IppAttrGroup> attrGroups = new ArrayList<>();

        IppAttrGroup group = null;
        IppAttrValue value = null;
        IppAttr attr = null;
        IppStatusCode requestStatus = null;

        /*
         * First test on an exception since this overrules everything.
         */
        if (request.hasDeferredException()) {

            requestStatus = IppStatusCode.OK;

        } else if (operation.isAuthorized()) {

            requestStatus = IppStatusCode.OK;

        } else {
            /*
             * IMPORTANT: the request status should be OK, since there is no
             * problem with the ValidationJob request.
             */
            requestStatus = IppStatusCode.OK;
        }

        /**
         * Group 1: Operation Attributes
         */
        group = this.createOperationGroup();
        attrGroups.add(group);

        /*
         * (detailed) messages
         */
        if (!operation.isAuthorized()) {

            attr = new IppAttr("status-message", new IppText());
            value = new IppAttrValue(attr);
            value.addValue(
                    "before printing login to the SavaPage WebApp first");
            group.addAttribute(value);

            attr = new IppAttr("detailed-status-message", new IppText());
            value = new IppAttrValue(attr);
            value.addValue("You are printing to an untrusted SavaPage Queue. "
                    + "Make sure you are logged into the SavaPage WebApp, "
                    + "and inspect your local printer queue for held jobs.");
            group.addAttribute(value);

        } else if (request.hasDeferredException()) {

            attr = new IppAttr("status-message", new IppText());
            value = new IppAttrValue(attr);
            value.addValue("Internal Savapage Error");
            group.addAttribute(value);

            String msg = request.getDeferredException().getMessage();
            if (StringUtils.isNotBlank(msg)) {
                attr = new IppAttr("detailed-status-message", new IppText());
                value = new IppAttrValue(attr);
                value.addValue(msg);
                group.addAttribute(value);
            }
        }

        this.write(operation, requestStatus, attrGroups, ostr,
                request.getAttributesCharset());
    }

}
