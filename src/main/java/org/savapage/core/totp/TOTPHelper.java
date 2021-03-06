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
package org.savapage.core.totp;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.savapage.core.config.ConfigManager;
import org.savapage.core.config.IConfigProp.Key;
import org.savapage.core.crypto.CryptoUser;
import org.savapage.core.dao.enums.UserAttrEnum;
import org.savapage.core.dto.AbstractDto;
import org.savapage.core.jpa.User;
import org.savapage.core.services.ServiceContext;
import org.savapage.core.services.UserService;
import org.savapage.core.util.JsonHelper;
import org.savapage.ext.telegram.TelegramHelper;
import org.savapage.lib.totp.TOTPAuthenticator;
import org.savapage.lib.totp.TOTPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Rijk Ravestein
 *
 */
public final class TOTPHelper {

    /** */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(TOTPHelper.class);

    /** */
    private static final UserService USER_SERVICE =
            ServiceContext.getServiceFactory().getUserService();

    /** */
    private static final int RECOVERY_CODE_SIZE = 12;
    /** */
    private static final int MAX_RECOVERY_TRIALS = 3;
    /** */
    private static final long MAX_RECOVERY_AGE_MSEC = DateUtils.MILLIS_PER_HOUR;

    /** Utility class. */
    private TOTPHelper() {
    }

    /**
     * Generates a random recovery code.
     *
     * @return Backup code.
     */
    public static TOTPRecoveryDto generateRecoveryCode() {
        final TOTPRecoveryDto dto = new TOTPRecoveryDto();
        dto.setIssued(new Date());
        dto.setTrial(0);
        dto.setCode(RandomStringUtils.randomAlphanumeric(RECOVERY_CODE_SIZE));
        return dto;
    }

    /**
     * @param userDb
     *            User.
     * @return TOTP authentication builder.
     */
    private static TOTPAuthenticator.Builder
            getAuthenticatorBuilder(final User userDb) {
        final String secretKey =
                USER_SERVICE.getUserAttrValue(userDb, UserAttrEnum.TOTP_SECRET);
        if (secretKey == null) {
            throw new IllegalStateException(
                    "Application error: user TOTP secret missing.");
        }
        return new TOTPAuthenticator.Builder(secretKey);
    }

    /**
     *
     * @param userDb
     *            User.
     * @return {@code null} when not found or sending TOTP code to Telegram is
     *         disabled.
     */
    private static String getEnabledTelegramID(final User userDb) {

        if (!TelegramHelper.isTOTPEnabled()) {
            return null;
        }

        if (!USER_SERVICE.isUserAttrValue(userDb,
                UserAttrEnum.EXT_TELEGRAM_TOTP_ENABLE)) {
            return null;
        }

        return USER_SERVICE.getUserAttrValue(userDb,
                UserAttrEnum.EXT_TELEGRAM_ID);
    }

    /**
     *
     * @param userDb
     *            User.
     * @return {@code true} if a generated TOTP code was send to User Telegram
     *         ID. {@code false} when code was <i>not</i> send because Telegram
     *         TOTP bot is not enabled/configured, or User Telegram ID is not
     *         specified, or User TOTP code by Telegram is disabled.
     * @throws IOException
     *             If send failed.
     */
    public static boolean sendCodeToTelegram(final User userDb)
            throws IOException {

        if (!TelegramHelper.isTOTPEnabled()) {
            return false;
        }

        if (!USER_SERVICE.isUserAttrValue(userDb,
                UserAttrEnum.EXT_TELEGRAM_TOTP_ENABLE)) {
            return false;
        }

        final String telegramID = getEnabledTelegramID(userDb);

        if (StringUtils.isBlank(telegramID)) {
            return false;
        }

        final TOTPAuthenticator.Builder authBuilder =
                getAuthenticatorBuilder(userDb);
        final TOTPAuthenticator auth = authBuilder.build();

        try {
            final TOTPSentDto dtoSent = getTelegramSentCode(userDb);
            /*
             * If sent code is not consumed yet, verify is code is valid for at
             * least one (1) step. If so, do not resent the code.
             */
            if (dtoSent != null && auth.verifyTOTP(
                    DateUtils.addSeconds(new Date(),
                            -1 * authBuilder.getStepSeconds()),
                    Long.parseLong(dtoSent.getCode()))) {
                return true;
            }
        } catch (NumberFormatException | TOTPException e) {
            // no code intended
        }

        final String totpCode = auth.generateTOTP();
        final boolean isSent = TelegramHelper.sendMessage(telegramID, totpCode);

        if (isSent) {
            final TOTPSentDto dto = new TOTPSentDto();
            dto.setSent(new Date());
            dto.setCode(totpCode);
            TOTPHelper.setTelegramSentCodeDB(userDb, dto);
        }
        return isSent;
    }

    /**
     * @return {@code true} if User TOTP is enabled.
     */
    public static boolean isTOTPEnabled() {
        return ConfigManager.instance().isConfigValue(Key.USER_TOTP_ENABLE);
    }

    /**
     *
     * @param userDb
     *            User.
     * @return {@code true} if TOTP is activated for User.
     */
    public static boolean isTOTPActivated(final User userDb) {
        return isTOTPEnabled()
                && USER_SERVICE.isUserAttrValue(userDb,
                        UserAttrEnum.TOTP_ENABLE)
                && StringUtils.isNotBlank(USER_SERVICE.getUserAttrValue(userDb,
                        UserAttrEnum.TOTP_SECRET));
    }

    /**
     * Verifies if code is a valid TOTP code, and updates/deletes user TOTP code
     * history in database (within commit scope of caller).
     *
     * @param userDb
     *            User.
     * @param code
     *            TOTP code to verify.
     * @return {@code true} if code is a valid recovery code.
     * @throws IOException
     *             If JSON error.
     */
    public static boolean verifyCode(final User userDb, final String code)
            throws IOException {

        final String secretKey =
                USER_SERVICE.getUserAttrValue(userDb, UserAttrEnum.TOTP_SECRET);

        if (secretKey == null) {
            throw new IllegalStateException(
                    "Application error: user TOTP secret missing.");
        }

        final TOTPAuthenticator.Builder authBuilder =
                getAuthenticatorBuilder(userDb);
        final TOTPAuthenticator auth = authBuilder.build();

        boolean isAuthenticated = false;
        final Date now = new Date();
        try {
            isAuthenticated = auth.verifyTOTP(Long.parseLong(code));
        } catch (NumberFormatException e) {
            isAuthenticated = false;
        } catch (TOTPException e) {
            throw new IllegalStateException(e);
        }

        TOTPSentDto telegramSentCode = null;

        if (isAuthenticated) {
            // Code is valid, but does it match the code sent to Telegram?
            telegramSentCode = getTelegramSentCode(userDb);
            if (telegramSentCode != null
                    && !code.equals(telegramSentCode.getCode())
                    && StringUtils.isNotBlank(getEnabledTelegramID(userDb))) {
                isAuthenticated = false;
            }
        }

        if (!isAuthenticated) {
            return false;
        }

        final String json = USER_SERVICE.getUserAttrValue(userDb,
                UserAttrEnum.TOTP_HISTORY);

        TOTPHistoryDto history = null;
        if (json != null) {
            history = JsonHelper.createOrNull(TOTPHistoryDto.class, json);
        }
        if (history == null) {
            history = new TOTPHistoryDto();
        }

        if (history.getCodes() == null) {
            history.setCodes(new HashMap<String, Long>());
        }
        final Map<String, Long> codes = history.getCodes();

        // Validate.
        if (codes.containsKey(code)) {
            LOGGER.warn("User [{}]: TOTP code used a second time.",
                    userDb.getUserId());
            return false;
        }

        // Prune history
        final Iterator<Entry<String, Long>> iterCodes =
                codes.entrySet().iterator();

        while (iterCodes.hasNext()) {
            final Entry<String, Long> entry = iterCodes.next();
            if (entry.getValue()
                    .longValue() < now.getTime() - (authBuilder.getStepSeconds()
                            * authBuilder.getSyncSteps()
                            * DateUtils.MILLIS_PER_SECOND)) {
                iterCodes.remove();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(
                            "User [{}]: expired TOTP code"
                                    + " removed from history.",
                            userDb.getUserId());
                }
            }
        }

        // Add to history.
        codes.put(code, Long.valueOf(now.getTime()));

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("User [{}]: {} TOTP code(s)" + " in history.",
                    userDb.getUserId(), codes.size());
        }

        setEncryptUserAttrInDB(userDb, UserAttrEnum.TOTP_HISTORY, history);

        // Prune code sent to Telegram.
        if (telegramSentCode != null) {
            USER_SERVICE.removeUserAttr(userDb,
                    UserAttrEnum.EXT_TELEGRAM_TOTP_SENT);
        }

        return true;
    }

    /**
     * Verifies if code is a valid recovery code, and updates/deletes user
     * recovery code in database (within commit scope of caller).
     *
     * @param userDb
     *            User.
     * @param code
     *            Code to verify.
     * @return {@code true} if code is a valid recovery code.
     * @throws IOException
     *             If JSON error.
     */
    public static boolean verifyRecoveryCode(final User userDb,
            final String code) throws IOException {

        if (code == null) {
            return false;
        }

        final String json = USER_SERVICE.getUserAttrValue(userDb,
                UserAttrEnum.TOTP_RECOVERY);

        if (json == null) {
            return false;
        }

        final TOTPRecoveryDto dto =
                JsonHelper.createOrNull(TOTPRecoveryDto.class, json);

        boolean remove = true;
        boolean valid = false;

        if (dto != null) {

            final long age = new Date().getTime() - dto.getIssued().getTime();

            if (dto.getTrial() < MAX_RECOVERY_TRIALS
                    && age < MAX_RECOVERY_AGE_MSEC) {
                final int currentTrial = dto.getTrial() + 1;
                valid = code.equals(dto.getCode());
                remove = valid || currentTrial == MAX_RECOVERY_TRIALS;
                if (!remove) {
                    dto.setTrial(currentTrial);
                    setRecoveryCodeDB(userDb, dto);
                }
            }
        }

        if (remove) {
            USER_SERVICE.removeUserAttr(userDb, UserAttrEnum.TOTP_RECOVERY);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("User [{}]: TOTP recovery [{}]", userDb.getUserId(),
                    Boolean.valueOf(valid));
        }

        return valid;
    }

    /**
     * Get TOTP code sent to user's Telegram ID.
     *
     * @param userDb
     *            User.
     * @return {@code null} if no code was sent or code is removed because
     *         expired or consumed.
     */
    private static TOTPSentDto getTelegramSentCode(final User userDb) {

        final String json = USER_SERVICE.getUserAttrValue(userDb,
                UserAttrEnum.EXT_TELEGRAM_TOTP_SENT);

        if (json == null) {
            return null;
        }

        final TOTPSentDto dto =
                JsonHelper.createOrNull(TOTPSentDto.class, json);

        if (dto == null) {
            return null;
        }
        return dto;
    }

    /**
     * Updates/creates user recovery code in database (within commit scope of
     * caller).
     *
     * @param userDb
     *            User.
     * @param attrEnum
     *            {@link UserAttrEnum}.
     * @param dto
     *            DTO object.
     * @throws IOException
     *             If JSON error.
     */
    private static void setEncryptUserAttrInDB(final User userDb,
            final UserAttrEnum attrEnum, final AbstractDto dto)
            throws IOException {

        USER_SERVICE.setUserAttrValue(userDb, attrEnum,
                CryptoUser.encryptUserAttr(userDb.getId(),
                        JsonHelper.stringifyObject(dto)));
    }

    /**
     * Updates/creates user recovery code in database (within commit scope of
     * caller).
     *
     * @param userDb
     *            User.
     * @param dto
     *            Recovery code.
     * @throws IOException
     *             If JSON error.
     */
    public static void setRecoveryCodeDB(final User userDb,
            final TOTPRecoveryDto dto) throws IOException {
        setEncryptUserAttrInDB(userDb, UserAttrEnum.TOTP_RECOVERY, dto);
    }

    /**
     * Updates/creates user TOTP code snt to Telegram in database (within commit
     * scope of caller).
     *
     * @param userDb
     *            User.
     * @param dto
     *            Sent code.
     * @throws IOException
     *             If JSON error.
     */
    public static void setTelegramSentCodeDB(final User userDb,
            final TOTPSentDto dto) throws IOException {
        setEncryptUserAttrInDB(userDb, UserAttrEnum.EXT_TELEGRAM_TOTP_SENT,
                dto);
    }

}
