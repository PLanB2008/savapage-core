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
package org.savapage.core.ipp.attribute;

import org.savapage.core.ipp.attribute.syntax.IppInteger;
import org.savapage.core.ipp.attribute.syntax.IppKeyword;
import org.savapage.core.ipp.attribute.syntax.IppName;
import org.savapage.core.ipp.attribute.syntax.IppOctetString;
import org.savapage.core.ipp.attribute.syntax.IppUri;
import org.savapage.core.ipp.encoding.IppValueTag;

/**
 * A dictionary of subscription attributes:
 * <a href="http://tools.ietf.org/html/rfc3995">RFC3995</a>.
 *
 * @author Rijk Ravestein
 *
 */
public final class IppDictSubscriptionAttr extends AbstractIppDict {

    public static final String ATTR_NOTIFY_EVENTS = "notify-events";

    public static final String ATTR_NOTIFY_LEASE_DURATION =
            "notify-lease-duration";

    public static final String ATTR_NOTIFY_RECIPIENT_URI =
            "notify-recipient-uri";

    public static final String ATTR_NOTIFY_SUBSCRIBER_USER_NAME =
            "notify-subscriber-user-name";

    public static final String ATTR_NOTIFY_PULL_METHOD = "notify-pull-method";

    public static final String ATTR_NOTIFY_USER_DATA = "notify-user-data";

    /**
     * The notify-time-interval attribute specifies the minimum number of
     * seconds between job-progress event notifications. This attribute allows a
     * client to reduce the frequency of event notifications so that fast
     * printers do not bog down the client.
     */
    public static final String ATTR_NOTIFY_TIME_INTERVAL =
            "notify-time-interval";

    public static final String ATTR_NOTIFY_SUBSCRIPTION_ID =
            "notify-subscription-id";

    /**
     * Group 1: Operation Attributes.
     */
    private final IppAttr[] attributes = {
            //
            new IppAttr(ATTR_NOTIFY_EVENTS, IppKeyword.instance()),
            new IppAttr(ATTR_NOTIFY_LEASE_DURATION, IppInteger.instance()),
            new IppAttr(ATTR_NOTIFY_RECIPIENT_URI, IppUri.instance()),
            new IppAttr(ATTR_NOTIFY_SUBSCRIBER_USER_NAME, IppName.instance()),
            new IppAttr(ATTR_NOTIFY_TIME_INTERVAL, IppInteger.instance()),
            new IppAttr(ATTR_NOTIFY_SUBSCRIPTION_ID, IppInteger.instance()),
            new IppAttr(ATTR_NOTIFY_PULL_METHOD, IppKeyword.instance()),
            new IppAttr(ATTR_NOTIFY_USER_DATA, new IppOctetString(63)),
            //
    };

    /** */
    private static class SingletonHolder {
        /** */
        public static final IppDictSubscriptionAttr INSTANCE =
                new IppDictSubscriptionAttr();
    }

    /**
     * @return The singleton instance.
     */
    public static IppDictSubscriptionAttr instance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     *
     */
    private IppDictSubscriptionAttr() {
        init(attributes);

    }

    @Override
    public IppAttr getAttr(final String keyword, final IppValueTag valueTag) {
        /*
         * Ignore the value tag.
         */
        return getAttr(keyword);
    }

}
