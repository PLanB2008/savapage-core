/*
 * This file is part of the SavaPage project <http://savapage.org>.
 * Copyright (c) 2011-2014 Datraverse B.V.
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
package org.savapage.core.services;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.savapage.core.config.IConfigProp;
import org.savapage.core.dao.helpers.IppQueueAttrEnum;
import org.savapage.core.dao.helpers.ReservedIppQueueEnum;
import org.savapage.core.jpa.IppQueue;
import org.savapage.core.jpa.IppQueueAttr;
import org.savapage.core.jpa.User;
import org.savapage.core.print.server.DocContentPrintException;
import org.savapage.core.print.server.DocContentPrintReq;
import org.savapage.core.print.server.DocContentPrintRsp;

/**
 *
 * @author Datraverse B.V.
 *
 */
public interface QueueService {

    /**
     *
     */
    int MAX_TIME_SERIES_INTERVALS_DAYS = 40;

    /**
     * Encapsulation of IppQueue attribute map.
     */
    class IppQueueAttrLookup {

        /**
         *
         */
        private final Map<String, String> lookup;

        /**
         *
         * @param queue
         *            The IppQueue.
         */
        public IppQueueAttrLookup(final IppQueue queue) {
            this.lookup = new HashMap<>();
            if (queue.getAttributes() != null) {
                for (final IppQueueAttr attr : queue.getAttributes()) {
                    lookup.put(attr.getName(), attr.getValue());
                }
            }
        }

        /**
         * Gets the attribute value.
         *
         * @param key
         *            The attribute key.
         * @return The attribute value, or {@code null} when not found.
         */
        public String get(final IppQueueAttrEnum key) {
            return lookup.get(key.getDbName());
        }

        /**
         * Gets the attribute value.
         *
         * @param key
         *            The attribute key.
         * @param dfault
         *            The default value.
         * @return The attribute value, or the default when not found.
         */
        public String get(final IppQueueAttrEnum key, final String dfault) {
            String value = this.get(key);
            if (value == null) {
                value = dfault;
            }
            return value;
        }

        /**
         * Checks if the the value of the attribute key represents "true" value.
         *
         * @param key
         *            The attribute key.
         * @param dfault
         *            The default value.
         * @return {@code true} when key value represents true.
         */
        public boolean isTrue(final IppQueueAttrEnum key, final boolean dfault) {

            final boolean bValue;

            final String value = this.get(key);

            if (value == null) {
                bValue = dfault;
            } else {
                bValue = value.equals(IConfigProp.V_YES);
            }

            return bValue;
        }

    }

    /**
     * Creates the reserved queues when they do not exist.
     */
    void lazyCreateReservedQueues();

    /**
     * Does {@link IppQueue} represent a raw print queue.
     *
     * @param queue
     *            The {@link IppQueue} .
     * @return {@code true} when queue represents a raw print queue.
     */
    boolean isRawPrintQueue(IppQueue queue);

    /**
     * Gets the reserved {@link IppQueue}. When it does not exist it is created.
     *
     * @param reservedQueue
     *            The {@link ReservedIppQueueEnum}..
     * @return The {@link IppQueue}.
     */
    IppQueue getOrCreateReservedQueue(ReservedIppQueueEnum reservedQueue);

    /**
     * Checks if urlPath represents a reserved queue (the check is
     * case-insensitive).
     *
     * @param urlPath
     *            The URL path.
     * @return {@link ReservedIppQueueEnum} or {@code null} when URL path is a
     *         user-defined queue.
     */
    ReservedIppQueueEnum getReservedQueue(final String urlPath);

    /**
     * Checks if urlPath represents a reserved queue (the check is
     * case-insensitive).
     *
     * @param urlPath
     *            The URL path.
     * @return {@code true} when URL path represents a reserved queue.
     */
    boolean isReservedQueue(String urlPath);

    /**
     * Does {@link IppQueue} represent the GCP print queue.
     *
     * @param queue
     *            The queue.
     * @return {@code true} when queue represents the GCP print queue.
     */
    boolean isGcpPrintQueue(IppQueue queue);

    /**
     * Does {@link IppQueue} represent the Mail Print queue.
     *
     * @param queue
     *            The {@link IppQueue} .
     * @return {@code true} when queue represents the Mail Print queue.
     */
    boolean isMailPrintQueue(IppQueue queue);

    /**
     * Does {@link IppQueue} represent the SmartSchool queue.
     *
     * @param queue
     *            The {@link IppQueue} .
     * @return {@code true} when queue represents the SmartSchool queue.
     */
    boolean isSmartSchoolQueue(IppQueue queue);

    /**
     * Does {@link IppQueue} represent the Web Print queue.
     *
     * @param queue
     *            The queue.
     * @return {@code true} when queue represents the Web Print queue.
     */
    boolean isWebPrintQueue(IppQueue queue);

    /**
     * Traverses the attributes of a {@link IppQueue} to get the value of an
     * attribute.
     *
     * @param queue
     *            The {@link IppQueue} .
     * @param name
     *            The name of the attribute.
     *
     * @return {@code null} when not found.
     */
    String getAttributeValue(IppQueue queue, String name);

    /**
     * Sets queue instance as logically deleted (database is NOT updated).
     *
     * @param queue
     *            The {@link IppQueue} .
     * @param deletedDate
     *            The date.
     * @param deletedBy
     *            The actor.
     */
    void setLogicalDeleted(IppQueue queue, Date deletedDate, String deletedBy);

    /**
     * Reverses a logical delete (database is NOT updated).
     *
     * @param queue
     *            The queue.
     */
    void undoLogicalDeleted(final IppQueue queue);

    /**
     * Adds totals of a job to an {@link IppQueue} (database is NOT updated).
     *
     * @param queue
     *            The {@link IppQueue} .
     * @param jobDate
     *            The date.
     * @param jobPages
     *            The number of pages.
     * @param jobBytes
     *            The number of bytes.
     */
    void
            addJobTotals(IppQueue queue, Date jobDate, int jobPages,
                    long jobBytes);

    /**
     * Logs a PrintIn job, by adding a data point to the time series (database
     * IS updated).
     *
     * @param queue
     *            The {@link IppQueue}
     * @param observationTime
     *            The observation time (time of the PrintIn job).
     * @param jobPages
     *            The number of pages.
     */
    void logPrintIn(IppQueue queue, Date observationTime, Integer jobPages);

    /**
     * Prints a document {@link InputStream} originating from a {@link User} to
     * a Queue.
     *
     * @param queueName
     *            The name of the {@link IppQueue}.
     * @param user
     *            The {@link User} requesting the print.
     * @param printReq
     *            The {@link DocContentPrintReq}.
     * @param istrContent
     *            The {@link InputStream} with the content.
     * @return The {@link DocContentPrintRsp}.
     * @throws DocContentPrintException
     *             When something goes wrong during printing.
     */
    DocContentPrintRsp printDocContent(String queueName, User user,
            DocContentPrintReq printReq, InputStream istrContent)
            throws DocContentPrintException;

    /**
     * Checks if client IPv4 address has access to a queue.
     * <p>
     * Access is allowed if one of these conditions is met:
     * <ol>
     * <li>The queue is not deleted and not disabled.</li>
     * <li>clientIpAddr is an allowed IP address for the queue.</li>
     * <li>The queue is trusted, and the clientIpAddr is allowed to print to it.
     * </li>
     * <li>remoteAddr (client) has access to queue.</li>
     * </ol>
     * </p>
     *
     * @param queue
     *            The queue.
     * @param uri
     *            The print request URI (used for logging).
     * @param clientIpAddr
     *            The IP address of the requesting user.
     * @return {@code true} when access to queue is allowed, {@code false} when
     *         not.
     */
    boolean hasClientIpAccessToQueue(IppQueue queue, String uri,
            String clientIpAddr);
}
