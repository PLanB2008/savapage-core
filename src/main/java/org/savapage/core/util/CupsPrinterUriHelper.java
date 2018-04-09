/*
 * This file is part of the SavaPage project <https://www.savapage.org>.
 * Copyright (c) 2011-2018 Datraverse B.V.
 * Authors: Rijk Ravestein.
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
package org.savapage.core.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Rijk Ravestein
 *
 */
public final class CupsPrinterUriHelper {

    /** */
    private static final String SCHEME_HP = "hp";

    /** */
    private static final String HP_QUERY_PARM_IP = "ip";

    /** */
    private static final String SCHEME_IPP = "ipp";

    /** */
    private static final String SCHEME_IPPS = "ipps";

    /** */
    private static final String SCHEME_SOCKET = "socket";

    /**
     *
     * @param uri
     *            The URI.
     * @return The query key/values.
     * @throws UnsupportedEncodingException
     *             When URL decode error.
     */
    private static Map<String, String> splitQuery(final URI uri)
            throws UnsupportedEncodingException {

        final Map<String, String> queryPairs = new HashMap<>();

        final String query = uri.getQuery();
        final String[] pairs = query.split("&");

        for (final String pair : pairs) {
            final int idx = pair.indexOf("=");
            queryPairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
                    URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return queryPairs;
    }

    /**
     *
     * @param uri
     *            The device URI.
     * @return The host part of the URI.
     */
    public static String resolveHost(final URI uri) {

        final String scheme = uri.getScheme();

        if (scheme == null) {
            return null;
        }

        final String schemeLower = scheme.toLowerCase();

        if (schemeLower.equals(SCHEME_HP)) {

            try {
                return splitQuery(uri).get(HP_QUERY_PARM_IP);
            } catch (UnsupportedEncodingException e) {
                return null;
            }

        } else if (schemeLower.equals(SCHEME_SOCKET)
                || schemeLower.equals(SCHEME_IPP)
                || schemeLower.equals(SCHEME_IPPS)) {

            return uri.getHost();
        }

        if (uri.getRawSchemeSpecificPart() != null) {
            try {
                // Recurse.
                return resolveHost(new URI(uri.getRawSchemeSpecificPart()));
            } catch (URISyntaxException e) {
                return null;
            }
        }

        return null;
    }

}
