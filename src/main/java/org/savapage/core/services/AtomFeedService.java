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
package org.savapage.core.services;

import java.io.OutputStream;

import org.savapage.lib.feed.AtomFeedWriter;
import org.savapage.lib.feed.FeedException;

/**
 *
 * @author Rijk Ravestein
 *
 */
public interface AtomFeedService extends StatefulService {

    /**
     *
     * @throws FeedException
     *             When errors.
     */
    void refreshAdminFeed() throws FeedException;

    /**
     * Gets the Atom Feed writer for admin news.
     *
     * @param ostr
     *            The output stream to write atom to.
     * @return The {@link AtomFeedWriter}.
     * @throws FeedException
     *             When errors.
     */
    AtomFeedWriter getAdminFeedWriter(OutputStream ostr) throws FeedException;

}