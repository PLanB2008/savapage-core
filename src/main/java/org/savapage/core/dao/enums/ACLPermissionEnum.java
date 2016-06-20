/*
 * This file is part of the SavaPage project <http:savapage.org>.
 * Copyright (c) 2011-2016 Datraverse B.V.
 * Author: Rijk Ravestein.
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
 * along with this program.  If not, see <http:www.gnu.org/licenses/>.
 *
 * For more information, please contact Datraverse B.V. at this
 * address: info@datraverse.com
 */
package org.savapage.core.dao.enums;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

import org.savapage.core.util.LocaleHelper;

/**
 *
 * @author Rijk Ravestein
 *
 */
public enum ACLPermissionEnum {
    /**
     * Allowed to read the domain object.
     */
    READER(ACLPermissionEnum.BIT_READER),

    /**
     * Allowed to send domain object.
     */
    SEND(ACLPermissionEnum.BIT_READER_SEND),

    /**
     * Allowed to download domain object.
     */
    DOWNLOAD(ACLPermissionEnum.BIT_READER_DOWNLOAD),

    /**
     * Allowed read and to make changes to the domain object.
     */
    EDITOR(ACLPermissionEnum.BIT_EDITOR, ACLPermissionEnum.BIT_READER),

    /**
     * Allowed to create the domain object.
     */
    CREATE(ACLPermissionEnum.BIT_EDITOR_CREATE),

    /**
     * Allowed to delete the domain object.
     */
    DELETE(ACLPermissionEnum.BIT_EDITOR_DELETE),

    /**
     * Allowed to restore a previously deleted domain object.
     */
    UNDELETE(ACLPermissionEnum.BIT_EDITOR_UNDELETE),

    /**
     * Allowed to perform all of the above actions.
     */
    OPERATOR(ACLPermissionEnum.BIT_OPERATOR, ACLPermissionEnum.BITMASK_OPERATOR),

    /**
     * Allowed to perform all of the above actions, and in addition allowed to
     * grant any of the above permissions to others.
     */
    MASTER(ACLPermissionEnum.BIT_MASTER, ACLPermissionEnum.BITMASK_MASTER),

    /**
     * Owns the domain object. An owner can perform any of the above actions and
     * grant master and owner permissions.
     */
    OWNER(ACLPermissionEnum.BIT_OWNER, ACLPermissionEnum.BITMASK_OWNER);

    // --------------------------------------------
    // Reader
    // --------------------------------------------
    private static final int BIT_READER = 0x1;
    //
    private static final int BIT_READER_DOWNLOAD = 0x2;
    private static final int BIT_READER_SEND = 0x4;
    //
    private static final int BIT_RESERVED_4 = 0x8;
    private static final int BIT_RESERVED_5 = 0x10;
    private static final int BIT_RESERVED_6 = 0x20;
    private static final int BIT_RESERVED_7 = 0x40;
    private static final int BIT_RESERVED_8 = 0x80;
    private static final int BIT_RESERVED_9 = 0x100;
    private static final int BIT_RESERVED_10 = 0x200;
    private static final int BIT_RESERVED_11 = 0x400;
    private static final int BIT_RESERVED_12 = 0x800;

    // --------------------------------------------
    // Editor
    // --------------------------------------------
    private static final int BIT_EDITOR = 0x1000;
    //
    private static final int BIT_EDITOR_CREATE = 0x2000;
    private static final int BIT_EDITOR_DELETE = 0x4000;
    private static final int BIT_EDITOR_UNDELETE = 0x8000;
    //
    private static final int BIT_RESERVED_17 = 0x10000;
    private static final int BIT_RESERVED_18 = 0x20000;
    private static final int BIT_RESERVED_19 = 0x40000;
    private static final int BIT_RESERVED_20 = 0x80000;
    private static final int BIT_RESERVED_21 = 0x100000;
    private static final int BIT_RESERVED_22 = 0x200000;
    private static final int BIT_RESERVED_23 = 0x400000;
    private static final int BIT_RESERVED_24 = 0x800000;
    private static final int BIT_RESERVED_25 = 0x1000000;
    private static final int BIT_RESERVED_26 = 0x2000000;
    private static final int BIT_RESERVED_27 = 0x4000000;
    private static final int BIT_RESERVED_28 = 0x8000000;
    private static final int BIT_RESERVED_29 = 0x10000000;

    // --------------------------------------------
    // Operator, Master, Owner
    // --------------------------------------------
    private static final int BIT_OPERATOR = 0x20000000;
    private static final int BIT_MASTER = 0x40000000;
    private static final int BIT_OWNER = 0x80000000;

    // --------------------------------------------
    //
    // --------------------------------------------
    private static final int BITMASK_OPERATOR = 0x2FFFFFFF;
    private static final int BITMASK_MASTER = BITMASK_OPERATOR | BIT_MASTER;
    private static final int BITMASK_OWNER = BITMASK_MASTER | BIT_OWNER;

    /**
     * NOTE: since order is important do NOT use an EnumSet.
     */
    private static final ACLPermissionEnum[] ROLES_SUPER_HIGH_TO_LOW =
            new ACLPermissionEnum[] { ACLPermissionEnum.OWNER,
                    ACLPermissionEnum.MASTER, ACLPermissionEnum.OPERATOR };

    /**
     * NOTE: since order is important do NOT use an EnumSet.
     */
    private static final ACLPermissionEnum[] ROLES_ALL_HIGH_TO_LOW =
            new ACLPermissionEnum[] { ACLPermissionEnum.OWNER,
                    ACLPermissionEnum.MASTER, ACLPermissionEnum.OPERATOR,
                    ACLPermissionEnum.EDITOR, ACLPermissionEnum.READER };

    private static final EnumSet<ACLPermissionEnum> PERMS_READER =
            EnumSet.of(ACLPermissionEnum.DOWNLOAD, ACLPermissionEnum.SEND);

    private static final EnumSet<ACLPermissionEnum> PERMS_EDITOR =
            EnumSet.of(ACLPermissionEnum.CREATE, ACLPermissionEnum.DELETE,
                    ACLPermissionEnum.UNDELETE);

    /**
     * The identifying bitmask flag.
     */
    private int flag;

    /**
     * A set of permission flags as bitmask.
     */
    private int privileges;

    /**
     *
     * @param flag
     *            The identifying bitmask flag.
     */
    ACLPermissionEnum(final int flag) {
        this.flag = flag;
        this.privileges = flag;
    }

    /**
     *
     * @param flag
     *            The identifying bitmask flag.
     * @param bitmask
     *            A set of permission flags as bitmask.
     */
    ACLPermissionEnum(final int flag, final int bitmask) {
        this.flag = flag;
        this.privileges = flag | bitmask;
    }

    /**
     * @return The identifying flag.
     */
    public int getFlag() {
        return this.flag;
    }

    /**
     * @return The set of permission flags as bitmask.
     */
    public int getPermission() {
        return this.privileges;
    }

    /**
     * Checks if required access permission is granted.
     *
     * @param required
     *            The required permission.
     * @return {@code true} if granted.
     */
    public boolean isGranted(final ACLPermissionEnum required) {
        return required.isPresent(this.privileges);
    }

    /**
     * Checks if present in bitmask.
     *
     * @param bitmask
     *            The bitmask.
     * @return {@code true} if privileged.
     */
    public boolean isPresent(final int bitmask) {
        return (this.flag & bitmask) != 0;
    }

    /**
     * @param locale
     *            The {@link Locale}.
     * @return The localized text.
     */
    public String uiText(final Locale locale) {
        return LocaleHelper.uiText(this, locale);
    }

    /**
     * @param list
     *            The list of permissions.
     * @return The integer bitmap with permissions
     */
    public static int asPrivilege(final List<ACLPermissionEnum> list) {
        int privilege = 0;

        for (final ACLPermissionEnum perm : list) {
            privilege |= perm.getPermission();
        }
        return privilege;
    }

    /**
     * @param privileges
     *            integer bitmap with permissions.
     * @return The main role permission.
     */
    public static ACLPermissionEnum asRole(final int privileges) {

        if (privileges == 0) {
            return null;
        }

        for (final ACLPermissionEnum perm : ROLES_ALL_HIGH_TO_LOW) {
            if (perm.isPresent(privileges)) {
                return perm;
            }
        }

        return null;
    }

    /**
     * @param privileges
     *            integer bitmap with permissions.
     * @return The list of permissions.
     */
    public static List<ACLPermissionEnum> asList(final int privileges) {

        final List<ACLPermissionEnum> list = new ArrayList<>();

        if (privileges == 0) {
            return list;
        }

        /*
         * Check for a super role.
         */
        for (final ACLPermissionEnum perm : ROLES_SUPER_HIGH_TO_LOW) {
            if (perm.isPresent(privileges)) {
                list.add(perm);
                return list;
            }
        }

        if (ACLPermissionEnum.EDITOR.isPresent(privileges)) {
            list.add(ACLPermissionEnum.EDITOR);
            list.addAll(asPermsEditor(privileges));
        }

        if (ACLPermissionEnum.READER.isPresent(privileges)) {
            list.add(ACLPermissionEnum.READER);
            list.addAll(asPermsReader(privileges));
        }

        return list;
    }

    /**
     * @param privileges
     *            integer bitmap with permissions.
     * @return The list of permissions.
     */
    public static List<ACLPermissionEnum> asPermsReader(final int privileges) {
        return asRolePerms(privileges, ACLPermissionEnum.READER, PERMS_READER);
    }

    /**
     * @param privileges
     *            integer bitmap with permissions.
     * @return The list of permissions.
     */
    public static List<ACLPermissionEnum> asPermsEditor(final int privileges) {
        return asRolePerms(privileges, ACLPermissionEnum.EDITOR, PERMS_EDITOR);
    }

    /**
     *
     * @param privileges
     * @param mainRole
     * @param permsRole
     * @return
     */
    private static List<ACLPermissionEnum> asRolePerms(final int privileges,
            final ACLPermissionEnum mainRole,
            final EnumSet<ACLPermissionEnum> permsRole) {

        final List<ACLPermissionEnum> list = new ArrayList<>();

        if (privileges == 0) {
            return list;
        }

        if (!mainRole.isPresent(privileges)) {
            return list;
        }

        for (final ACLPermissionEnum perm : permsRole) {
            if (perm.isPresent(privileges)) {
                list.add(perm);
            }
        }

        return list;
    }

}
