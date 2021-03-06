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
package org.savapage.core.dao.enums;

import java.util.HashMap;
import java.util.Map;

import org.savapage.core.SpException;
import org.savapage.core.dao.UserAttrDao;
import org.savapage.core.jpa.UserAttr;
import org.savapage.core.json.JobTicketProperties;
import org.savapage.core.totp.TOTPHistoryDto;
import org.savapage.core.totp.TOTPRecoveryDto;
import org.savapage.core.totp.TOTPSentDto;
import org.savapage.lib.pgp.PGPKeyID;

/**
 * {@link UserAttr} names. See {@link UserAttr#setName(String)}.
 *
 * @author Rijk Ravestein
 *
 */
public enum UserAttrEnum {

    /**
     * A JSON value of {@link Map} with key {@link ACLRoleEnum} and value
     * {@link Boolean}. When a {@link ACLRoleEnum} key is not present the value
     * is indeterminate.
     */
    ACL_ROLES("acl.roles"),

    /**
     * OIDS for Role "User": A JSON value of a {@link Map} with key
     * {@link ACLOidEnum}. Value {@link Integer} is a bitwise OR of
     * {@link ACLPermissionEnum#getPermission()} values that hold the granted
     * access. When a {@link ACLOidEnum} key is not present in the map the
     * access is indeterminate.
     */
    ACL_OIDS_USER("acl.oids.user"),

    /**
     * OIDS for Role "Admin": A JSON value of a {@link Map} with key
     * {@link ACLOidEnum}. Value {@link Integer} is a bitwise OR of
     * {@link ACLPermissionEnum#getPermission()} values that hold the granted
     * access. When a {@link ACLOidEnum} key is not present in the map the
     * access is indeterminate.
     */
    ACL_OIDS_ADMIN("acl.oids.admin"),

    /**
     * The pending bitcoin address created for a {@link User} payment
     * transaction. This address is deleted when consumed, i.e. upon first
     * confirmation.
     */
    BITCOIN_PAYMENT_ADDRESS("bitcoin.payment.address"),

    /**
     * Password of an Internal user.
     */
    INTERNAL_PASSWORD("internal-password"),

    /**
     * JSON string of {@link JobTicketProperties}.
     */
    JOBTICKET_PROPS_LATEST("jobticket.properties.latest"),

    /**
     * Encrypted PIN optionally to be used in combination with ID number and
     * Card(s).
     */
    PIN("pin"),

    /**
     * User PGP Public Key ID in upper-case hex notation <i>without</i> "0x"
     * prefix.
     */
    PGP_PUBKEY_ID("pgp.pubkey.id"),

    /**
     * Prefix for PGP public key ID in Key Ring, in upper-case hex notation
     * <i>without</i> "0x" prefix. This key is not used as such: note the <u>dot
     * character</u> at the end.
     * <p>
     * Usage example: {@code pgp.pubring.key.id.AD234BCC27362AF1}
     * </p>
     */
    PFX_PGP_PUBRING_KEY_ID("pgp.pubring.key.id."),

    /**
     *
     */
    PDF_PROPS("pdf-properties"),

    /**
     * Encrypted Telegram ID.
     */
    EXT_TELEGRAM_ID("ext.telegram.id"),

    /**
     * Enable sending One-time Password (TOTP) to
     * {@link UserAttrEnum#EXT_TELEGRAM_ID}.
     */
    EXT_TELEGRAM_TOTP_ENABLE("ext.telegram.totp.enable"),

    /**
     * Last TOTP code sent to Telegram. See {@link TOTPSentDto}.
     */
    EXT_TELEGRAM_TOTP_SENT("ext.telegram.totp.sent"),

    /**
     * Enable RFC 6238 Time-based One-time Password (TOTP).
     */
    TOTP_ENABLE("totp.enable"),

    /**
     * Encrypted secret key for RFC 6238 Time-based One-time Password (TOTP).
     */
    TOTP_SECRET("totp.secret"),

    /**
     * <p>
     * <a href="https://tools.ietf.org/html/rfc6238">RFC6238</a>, page 7:
     * <i>"Note that a prover may send the same OTP inside a given time-step
     * window multiple times to a verifier. The verifier MUST NOT accept the
     * second attempt of the OTP after the successful validation has been issued
     * for the first OTP, which ensures one-time only use of an OTP."</i>
     * </p>
     *
     * See {@link TOTPHistoryDto}.
     */
    TOTP_HISTORY("totp.history"),

    /**
     * See {@link TOTPRecoveryDto}.
     */
    TOTP_RECOVERY("totp.recovery"),

    /**
     * The Encrypted PIN {@link UUID} used for identification when printing from
     * the internet. See {@link ReservedIppQueueEnum#IPP_PRINT_INTERNET} and
     * {@link ReservedIppQueueEnum#WEBSERVICE}.
     */
    UUID("uuid"),

    /**
     * Statistic time series. Example:
     * <p>
     * {@code 1342562400000,7,16,2,9,16}
     * </p>
     */
    PRINT_IN_ROLLING_WEEK_PAGES("print.in." + UserAttrDao.STATS_ROLLING //
            + "-week.pages"),

    /**
     * Statistic time series. Example:
     * <p>
     * {@code 1342562400000,70033,163344,22335511,9999332,16345}
     * </p>
     */
    PRINT_IN_ROLLING_WEEK_BYTES("print.in." + UserAttrDao.STATS_ROLLING //
            + "-week.bytes"),

    /**
     * Statistic time series. Example:
     * <p>
     * {@code 1342562400000,7,16,2,9,16}
     * </p>
     */
    PRINT_IN_ROLLING_MONTH_PAGES("print.in." + UserAttrDao.STATS_ROLLING //
            + "-month.pages"),

    /**
     * Statistic time series. Example:
     * <p>
     * {@code 1342562400000,70033,163344,22335511,9999332,16345}
     * </p>
     */
    PRINT_IN_ROLLING_MONTH_BYTES("print.in." + UserAttrDao.STATS_ROLLING //
            + "-month.bytes"),
    /**
     * Statistic time series. Example:
     * <p>
     * {@code 1342562400000,7,16,2,9,16}
     * </p>
     */
    PRINT_OUT_ROLLING_WEEK_PAGES("print.out." + UserAttrDao.STATS_ROLLING //
            + "-week.pages"),

    /**
     * Statistic time series. Example:
     * <p>
     * {@code 1342562400000,7,16,2,9,16}
     * </p>
     */
    PRINT_OUT_ROLLING_WEEK_SHEETS("print.out." + UserAttrDao.STATS_ROLLING //
            + "-week.sheets"),
    /**
     * Statistic time series. Example:
     * <p>
     * {@code 1342562400000,7,16,2,9,16}
     * </p>
     */
    PRINT_OUT_ROLLING_WEEK_ESU("print.out." + UserAttrDao.STATS_ROLLING //
            + "-week.esu"),
    /**
     * Statistic time series. Example:
     * <p>
     * {@code 1342562400000,70033,163344,22335511,9999332,16345}
     * </p>
     */
    PRINT_OUT_ROLLING_WEEK_BYTES("print.out." + UserAttrDao.STATS_ROLLING //
            + "-week.bytes"),
    /**
     * Statistic time series. Example:
     * <p>
     * {@code 1342562400000,7,16,2,9,16}
     * </p>
     */
    PRINT_OUT_ROLLING_MONTH_PAGES("print.out." + UserAttrDao.STATS_ROLLING //
            + "-month.pages"),

    /**
     * Statistic time series. Example:
     * <p>
     * {@code 1342562400000,7,16,2,9,16}
     * </p>
     */
    PRINT_OUT_ROLLING_MONTH_SHEETS("print.out." + UserAttrDao.STATS_ROLLING //
            + "-month.sheets"),
    /**
     * Statistic time series. Example:
     * <p>
     * {@code 1342562400000,7,16,2,9,16}
     * </p>
     */
    PRINT_OUT_ROLLING_MONTH_ESU("print.out." + UserAttrDao.STATS_ROLLING //
            + "-month.esu"),

    /**
     * Statistic time series. Example:
     * <p>
     * {@code 1342562400000,70033,163344,22335511,9999332,16345}
     * </p>
     */
    PRINT_OUT_ROLLING_MONTH_BYTES("print.out." + UserAttrDao.STATS_ROLLING //
            + "-month.bytes"),

    /**
     * Statistic time series. Example:
     * <p>
     * {@code 1342562400000,7,16,2,9,16}
     * </p>
     */
    PDF_OUT_ROLLING_WEEK_PAGES("pdf.out." + UserAttrDao.STATS_ROLLING //
            + "-week.pages"),

    /**
     * Statistic time series. Example:
     * <p>
     * {@code 1342562400000,70033,163344,22335511,9999332,16345}
     * </p>
     */
    PDF_OUT_ROLLING_WEEK_BYTES("pdf.out." + UserAttrDao.STATS_ROLLING //
            + "-week.bytes"),

    /**
     * Statistic time series. Example:
     * <p>
     * {@code 1342562400000,7,16,2,9,16}
     * </p>
     */
    PDF_OUT_ROLLING_MONTH_PAGES("pdf.out." + UserAttrDao.STATS_ROLLING //
            + "-month.pages"),

    /**
     * Statistic time series. Example:
     * <p>
     * {@code 1342562400000,70033,163344,22335511,9999332,16345}
     * </p>
     */
    PDF_OUT_ROLLING_MONTH_BYTES("pdf.out." + UserAttrDao.STATS_ROLLING //
            + "-month.bytes"),

    /**
     * JSON set of preferred Shared Account IDs for delegated proxy print.
     */
    PROXY_PRINT_DELEGATE_ACCOUNTS_PREFERRED(//
            "proxy-print.delegate.accounts.preferred"),

    /**
     * Boolean: "true" when user selected preferred search scope.
     */
    PROXY_PRINT_DELEGATE_ACCOUNTS_PREFERRED_SELECT(//
            "proxy-print.delegate.accounts.preferred.select"),

    /**
     * JSON set of preferred UserGroup IDs for delegated proxy print.
     */
    PROXY_PRINT_DELEGATE_GROUPS_PREFERRED(//
            "proxy-print.delegate.groups.preferred"),

    /**
     * Boolean: "true" when user selected preferred search scope.
     */
    PROXY_PRINT_DELEGATE_GROUPS_PREFERRED_SELECT(//
            "proxy-print.delegate.groups.preferred.select");

    /**
     * Lookup {@link UserAttrEnum} by database name.
     */
    private static class Lookup {

        /**
         *
         */
        private final Map<String, UserAttrEnum> enumLookup =
                new HashMap<String, UserAttrEnum>();

        /**
         *
         */
        Lookup() {
            for (UserAttrEnum value : UserAttrEnum.values()) {
                enumLookup.put(value.name, value);
            }
        }

        /**
         *
         * @param key
         *            The key (name).
         * @return The enum.
         */
        public UserAttrEnum get(final String key) {
            return enumLookup.get(key);
        }
    }

    /**
     * The name used in the database.
     */
    private final String name;

    /**
     * Ensure one-time initialization on class loading.
     */
    private static class LookupHolder {
        /** */
        public static final Lookup INSTANCE = new Lookup();
    }

    /**
     *
     * @param name
     *            The database name.
     * @return The {@link UserAttrEnum}.
     */
    public static UserAttrEnum asEnum(final String name) {
        return LookupHolder.INSTANCE.get(name);
    }

    /**
     *
     * @param name
     *            The database name.
     */
    UserAttrEnum(final String name) {
        this.name = name;
    }

    /**
     *
     * @return The database name.
     */
    public final String getName() {
        return this.name;
    }

    /**
     *
     * @return {@code true} if encrypted.
     */
    public final boolean isEncrypted() {
        return this == PIN || this == UUID || this == EXT_TELEGRAM_ID
                || this == TOTP_SECRET || this == TOTP_HISTORY
                || this == TOTP_RECOVERY || this == EXT_TELEGRAM_TOTP_SENT;
    }

    /**
     * Gets the enum value based on the {@link ACLOidEnum} role.
     *
     * @param oid
     *            The {@link ACLOidEnum}
     * @return The enum value.
     */
    public static UserAttrEnum valueOf(final ACLOidEnum oid) {
        if (oid.isUserRole()) {
            return ACL_OIDS_USER;
        } else if (oid.isAdminRole()) {
            return ACL_OIDS_ADMIN;
        }
        throw new SpException(
                String.format("No role found for %s.", oid.toString()));
    }

    /**
     * Gets database key for PGP public key ring entry.
     *
     * @param keyId
     *            The PGP Key ID.
     * @return The database key.
     */
    public static String getPgpPubRingDbKey(final PGPKeyID keyId) {
        return PFX_PGP_PUBRING_KEY_ID.getName().concat(keyId.toHex());
    }

}
