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
package org.savapage.core.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.savapage.core.SpException;
import org.savapage.core.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic XML message loader and parser. This class looks for an
 * {@code message_<locale>.xml} file in the same directory as the requester
 * class, which is passed as parameter to all public methods.
 *
 * @author Rijk Ravestein
 *
 */
public final class Messages extends MessagesBundleMixin {

    /** */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(Messages.class);

    /** */
    private static final String DEFAULT_XML_RESOURCE = "messages";

    /** */
    private static class XMLResourceBundleControlHolder {
        /** */
        public static final XMLResourceBundleControl INSTANCE =
                new XMLResourceBundleControl();
    }

    /**
     * {@code true} if custom i18n is enabled.
     */
    private static boolean useCustomI18n = false;

    /**
     * Utility class.
     */
    private Messages() {
    }

    /**
     * Initializes the i18n messystem.
     *
     * @param enableCustomI18n
     *            {@code true} if custom i18n is enabled.
     */
    public static void init(final boolean enableCustomI18n) {
        useCustomI18n = enableCustomI18n;
    }

    /**
     * @return {@code true} if custom i18n is enabled.
     */
    public static boolean isCustomI18nEnabled() {
        return useCustomI18n;
    }

    /**
     * Loads a {@link ResourceBundle} from a jar file.
     *
     * @param reqClass
     *            The requester {@link Class}.
     * @param resourceName
     *            The name of the resource bundle without the locale suffix and
     *            file extension.
     * @param locale
     *            The {@link Locale}. Use {@code null} for default locale.
     * @return The {@link ResourceBundle}.
     */
    public static ResourceBundle loadXmlResource(
            final Class<? extends Object> reqClass, final String resourceName,
            final Locale locale) {
        return getResourceBundle(reqClass, resourceName, locale,
                XMLResourceBundleControlHolder.INSTANCE);
    }

    /**
     * Loads a custom {@link ResourceBundle} from the file system.
     *
     * @param clazz
     *            The class.
     * @param locale
     *            The locale.
     * @return {@code null} when resource is not found.
     */
    public static ResourceBundle loadXmlResourceCustom(
            final Class<? extends Object> clazz, final Locale locale) {
        return loadXmlResourceCustom(clazz, clazz.getSimpleName(), locale);
    }

    /**
     * Loads a custom {@link ResourceBundle} from the file system.
     *
     * @param clazz
     *            The class.
     * @param locale
     *            The locale.
     * @param resourceName
     *            The name of the resource bundle <i>without</i> the locale
     *            suffix and file extension.
     * @return {@code null} when resource is not found.
     */
    private static ResourceBundle loadXmlResourceCustom(
            final Class<? extends Object> clazz, final String resourceName,
            final Locale locale) {
        try {
            return loadXmlResource(ConfigManager.getServerCustomI18nHome(clazz),
                    resourceName, locale);

        } catch (MissingResourceException e) {
            // no code intended;
        }
        return null;
    }

    /**
     * Loads a {@link ResourceBundle} from the file system.
     *
     * @param directory
     *            The directory location of the XML resource.
     * @param resourceName
     *            The name of the resource bundle <i>without</i> the locale
     *            suffix and file extension.
     * @param candidate
     *            The {@link Locale}. Use {@code null} for default locale.
     * @return The {@link ResourceBundle}.
     */
    public static ResourceBundle loadXmlResource(final File directory,
            final String resourceName, final Locale candidate) {

        final URL[] urls;

        try {
            urls = new URL[] { directory.toURI().toURL() };
        } catch (MalformedURLException e) {
            throw new SpException(e.getMessage());
        }

        return getResourceBundle(new URLClassLoader(urls), resourceName,
                resourceName, candidate,
                XMLResourceBundleControlHolder.INSTANCE);
    }

    /**
     * Loads a {@link ResourceBundle} from a jar file.
     *
     * @param reqClass
     *            The requester {@link Class} (used to compose the bunble name).
     * @param resourceName
     *            The name of the resource bundle without the locale suffix and
     *            file extension.
     * @param candidate
     *            The {@link Locale} candidate. Use {@code null} for default
     *            locale.
     * @param control
     *            The {@link XMLResourceBundleControl}.
     * @return The {@link ResourceBundle}.
     */
    private static ResourceBundle getResourceBundle(
            final Class<? extends Object> reqClass, final String resourceName,
            final Locale candidate, final XMLResourceBundleControl control) {

        final String bundleName =
                getResourceBundleBaseName(reqClass.getPackage(), resourceName);

        return getResourceBundle(reqClass.getClassLoader(), bundleName,
                resourceName, candidate, control);
    }

    /**
     * Loads a {@link ResourceBundle} using the class loader.
     * <p>
     * NOTE: When a {@code message_<locale>.properties} files is already loaded
     * in the cache, the content of this file is used instead of the XML
     * variant. See
     * {@link ResourceBundle#getBundle(String, Locale, ClassLoader, java.util.ResourceBundle.Control)}
     * .
     * </p>
     *
     * @param classLoader
     *            The class loader.
     * @param bundleName
     *            The bundle name.
     * @param resourceName
     *            The name of the resource bundle without the locale suffix and
     *            file extension.
     * @param candidate
     *            The {@link Locale} candidate. Use {@code null} for default
     *            locale.
     * @param control
     *            The {@link XMLResourceBundleControl}.
     * @return The {@link ResourceBundle}.
     */
    private static ResourceBundle getResourceBundle(
            final ClassLoader classLoader, final String bundleName,
            final String resourceName, final Locale candidate,
            final XMLResourceBundleControl control) {

        final Locale locale = determineLocale(candidate);

        final ResourceBundle bundle = ResourceBundle.getBundle(bundleName,
                locale, classLoader, control);

        final Locale localeAlt = checkAlternative(locale, bundle);

        if (localeAlt == null) {
            return bundle;
        }
        return ResourceBundle.getBundle(bundleName, localeAlt, classLoader,
                control);
    }

    /**
     * Gets the localized message from {@code message*.xml} residing in the same
     * package as the requester class.
     *
     * @param reqClass
     *            The requester {@link Class}.
     * @param locale
     *            The {@link Locale}.
     * @param key
     *            The message key.
     * @return The message.
     */
    private static String loadMessagePattern(
            final Class<? extends Object> reqClass, final Locale locale,
            final String key) {

        if (isCustomI18nEnabled()) {
            final ResourceBundle bundle = loadXmlResourceCustom(reqClass,
                    DEFAULT_XML_RESOURCE, locale);
            if (bundle != null && bundle.containsKey(key)) {
                return bundle.getString(key);
            }
        }
        return loadXmlResource(reqClass, DEFAULT_XML_RESOURCE, locale)
                .getString(key);
    }

    /**
     * Gets the default locale (system) message from {@code message*.xml}
     * residing in the same package as the requester class.
     *
     * @param reqClass
     *            The requester {@link Class}.
     * @param key
     *            The message key.
     * @param args
     *            The message arguments.
     * @return The message.
     */
    public static String getMessage(final Class<? extends Object> reqClass,
            final String key, final String[] args) {
        return getMessage(reqClass, null, key, args);
    }

    /**
     * Gets the default locale (system) message from {@code message*.xml}
     * residing in the same package as the requester class.
     *
     * @param reqClass
     *            The requester {@link Class}.
     * @param key
     *            The message key.
     * @param args
     *            The message arguments.
     * @return The message.
     */
    public static String getSystemMessage(
            final Class<? extends Object> reqClass, final String key,
            final String... args) {
        return getMessage(reqClass, null, key, args);
    }

    /**
     * Gets the locale message for technical logging from {@code message.xml}
     * residing in the same package as the requester class.
     *
     * @param reqClass
     *            The requester {@link Class}.
     * @param key
     *            The message key.
     * @param args
     *            The message arguments.
     * @return The message.
     */
    public static String getLogFileMessage(
            final Class<? extends Object> reqClass, final String key,
            final String... args) {
        return getMessage(reqClass, LOCALE_NO_LANGUAGE, key, args);
    }

    /**
     * Creates a MessageFormat with the given pattern and uses it to format the
     * given arguments.
     *
     * @param pattern
     *            The pattern.
     * @param args
     *            The arguments
     * @return The formatted string.
     */
    public static String formatMessage(final String pattern,
            final String... args) {
        try {
            /*
             * Add an extra apostrophe ' to the MessageFormat pattern String to
             * ensure the ' character is displayed.
             */
            return MessageFormat.format(pattern.replace("\'", "\'\'"),
                    (Object[]) args);

        } catch (IllegalArgumentException e) {
            LOGGER.error("Error parsing message pattern [" + pattern + "]"
                    + e.getMessage());
            return pattern;
        }

    }

    /**
     * Gets the localized message from {@code message*.xml} residing in the same
     * package as the requester class.
     *
     * @param reqClass
     *            The requester {@link Class}.
     * @param locale
     *            The {@link Locale}.
     * @param key
     *            The message key.
     * @param args
     *            The message arguments.
     * @return The message.
     */
    public static String getMessage(final Class<? extends Object> reqClass,
            final Locale locale, final String key, final String... args) {

        final String pattern = loadMessagePattern(reqClass, locale, key);

        if ((args == null) || args.length == 0) {
            return pattern;
        }
        return formatMessage(pattern, args);
    }

    /**
     * Checks if default (system) message key exists.
     *
     * @param reqClass
     *            The requester {@link Class}.
     * @param key
     *            The message key.
     * @return {@code true} if default (system) message key exists.
     */
    public static boolean containsKey(final Class<? extends Object> reqClass,
            final String key) {
        return containsKey(reqClass, key, null);
    }

    /**
     * Checks if locale message key exists.
     *
     * @param reqClass
     *            The requester {@link Class}.
     * @param key
     *            The message key.
     * @param locale
     *            The {@link Locale}. Use {@code null} for default locale.
     * @return {@code true} if locale message key exists.
     */
    public static boolean containsKey(final Class<? extends Object> reqClass,
            final String key, final Locale locale) {

        if (isCustomI18nEnabled()) {
            final ResourceBundle bundle = loadXmlResourceCustom(reqClass,
                    DEFAULT_XML_RESOURCE, locale);
            if (bundle != null && bundle.containsKey(key)) {
                return true;
            }
        }
        return loadXmlResource(reqClass, DEFAULT_XML_RESOURCE, locale)
                .containsKey(key);
    }

}
