/*
 * This file is part of the SavaPage project <http://savapage.org>.
 * Copyright (c) 2011-2015 Datraverse B.V.
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please contact Datraverse B.V. at this
 * address: info@datraverse.com
 */
package org.savapage.core.ipp;

import org.savapage.core.SpException;

/**
 * Enumeration of job states.
 *
 * @author Datraverse B.V.
 */
public enum IppJobStateEnum {

    /** Job is waiting to be printed. */
    IPP_JOB_PENDING(0x03, "PENDING"),

    /** Job is held for printing. */
    IPP_JOB_HELD(0x04, "HELD"),

    /** Job is currently printing. */
    IPP_JOB_PROCESSING(0x05, "PROCESSING"),

    /** Job has been stopped. */
    IPP_JOB_STOPPED(0x06, "STOPPED"),

    /** Job has been canceled. */
    IPP_JOB_CANCELED(0x07, "CANCELED"),

    /** Job has aborted due to error. */
    IPP_JOB_ABORTED(0x08, "ABORTED"),

    /** Job has completed successfully. */
    IPP_JOB_COMPLETED(0x09, "COMPLETED");

    /**
     *
     */
    private final int bitPattern;

    /**
     * Text to be used in user interface.
     */
    private final String uiText;

    /**
     * Creates an enum value from an integer.
     *
     * @param value
     *            The integer.
     * @param text
     *            Text to be used in user interface.
     */
    IppJobStateEnum(final int value, final String text) {
        this.bitPattern = value;
        this.uiText = text;
    }

    /**
     * Gets the integer representing this enum value.
     *
     * @return The integer.
     */
    public int asInt() {
        return this.bitPattern;
    }

    /**
     *
     * @return Text string to be used in user interface.
     */
    public String asUiText() {
        return this.uiText;
    }

    /**
     * Checks if status means a job is present on queue.
     *
     * @return {@code true} when state is PENDING, HELD, PROCESSING or STOPPED.
     */
    public boolean isPresentOnQueue() {
        return bitPattern < getFirstAbsentOnQueueOrdinal().asInt();
    }

    /**
     *
     * @return The {@link IppJobStateEnum} that is the first ordinal indicating
     *         a status that job is absent on queue.
     */
    public static IppJobStateEnum getFirstAbsentOnQueueOrdinal() {
        return IppJobStateEnum.IPP_JOB_CANCELED;
    }

    /**
     *
     * @param value
     * @return
     */
    public static IppJobStateEnum asEnum(final int value) {
        if (value == IppJobStateEnum.IPP_JOB_ABORTED.asInt()) {
            return IPP_JOB_ABORTED;
        } else if (value == IppJobStateEnum.IPP_JOB_CANCELED.asInt()) {
            return IPP_JOB_CANCELED;
        } else if (value == IppJobStateEnum.IPP_JOB_COMPLETED.asInt()) {
            return IPP_JOB_COMPLETED;
        } else if (value == IppJobStateEnum.IPP_JOB_HELD.asInt()) {
            return IPP_JOB_HELD;
        } else if (value == IppJobStateEnum.IPP_JOB_PENDING.asInt()) {
            return IPP_JOB_PENDING;
        } else if (value == IppJobStateEnum.IPP_JOB_PROCESSING.asInt()) {
            return IPP_JOB_PROCESSING;
        } else if (value == IppJobStateEnum.IPP_JOB_STOPPED.asInt()) {
            return IPP_JOB_STOPPED;
        }
        throw new SpException("value [" + value
                + "] can not be converted to enum");
    }

}
