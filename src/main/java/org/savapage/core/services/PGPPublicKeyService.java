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
package org.savapage.core.services;

import org.savapage.core.jpa.User;
import org.savapage.lib.pgp.PGPBaseException;
import org.savapage.lib.pgp.PGPKeyID;
import org.savapage.lib.pgp.PGPPublicKeyInfo;

/**
 *
 * @author Rijk Ravestein
 *
 */
public interface PGPPublicKeyService {

    /**
     * Gets the URL template of Web Page to preview the content of the PGP
     * Public Key.
     * <p>
     * Placeholder <tt>{0}</tt> is to be replaced by the Hexadecimal KeyID,
     * without "0x" prefix.
     * </p>
     *
     * @return The template string, or {@code null} when unknown.
     */
    String getPublicKeyPreviewUrlTpl();

    /**
     * Adds public key to user's key ring.
     *
     * @param user
     *            The user.
     * @param keyID
     *            The Key ID.
     * @return Public Key info.
     * @throws PGPBaseException
     *             When errors.
     */
    PGPPublicKeyInfo lazyAddRingEntry(User user, PGPKeyID keyID)
            throws PGPBaseException;

    /**
     * Read user's own public key from user's key ring.
     *
     * @param user
     *            The user.
     * @return Public Key info, or {@code null} when not available.
     * @throws PGPBaseException
     *             When errors.
     */
    PGPPublicKeyInfo readRingEntry(User user) throws PGPBaseException;

    /**
     * Read user's own public key from user's key ring.
     *
     * @param userid
     *            The User ID.
     * @return Public Key info, or {@code null} when not available.
     * @throws PGPBaseException
     *             When errors.
     */
    PGPPublicKeyInfo readRingEntry(String userid) throws PGPBaseException;

    /**
     * Read public key from user's key ring.
     *
     * @param user
     *            The user.
     * @param keyID
     *            The Key ID.
     * @return Public Key info.
     * @throws PGPBaseException
     *             When errors.
     */
    PGPPublicKeyInfo readRingEntry(User user, PGPKeyID keyID)
            throws PGPBaseException;

    /**
     * Deletes public key from user's key ring.
     *
     * @param user
     *            The user.
     * @param keyID
     *            The Key ID.
     * @return {@code true} if and only if the entry is successfully deleted;
     *         {@code false} otherwise.
     */
    boolean deleteRingEntry(User user, PGPKeyID keyID);

    /**
     * @param filename
     *            Key ring entry filename.
     * @return {@code true} if filename syntax of Key Ring entry is valid.
     */
    boolean isValidRingEntryFileName(String filename);

}
