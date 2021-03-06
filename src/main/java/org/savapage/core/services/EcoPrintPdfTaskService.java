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

import java.util.UUID;

import org.savapage.core.imaging.EcoPrintPdfTaskInfo;

/**
 * A service for asynchronous (background) conversion of PDF files to their Eco
 * Print variant.
 *
 * @author Rijk Ravestein
 *
 */
public interface EcoPrintPdfTaskService extends StatefulService {

    /**
     * Submits a task.
     *
     * @param info
     *            The {@link EcoPrintPdfTaskInfo}.
     */
    void submitTask(final EcoPrintPdfTaskInfo info);

    /**
     * Pauses the service.
     */
    void pause();

    /**
     * Resumes the service.
     */
    void resume();

    /**
     * Cancels a task, either by removing it from the queue or by aborting it
     * when running.
     *
     * @param uuid
     *            The {@link UUID} of the task, see
     *            {@link EcoPrintPdfTaskInfo#getUuid()} .
     * @return {@code true} when task was found and cancelled, {@code false}
     *         when task was not found.
     */
    boolean cancelTask(UUID uuid);

    /**
     * Checks if a task is in queue or running.
     *
     * @param uuid
     *            The {@link UUID} of the task, see
     *            {@link EcoPrintPdfTaskInfo#getUuid()} .
     * @return {@code true} when task is in queue or running.
     */
    boolean hasTask(UUID uuid);

    /**
     * Shuts the service down and blocks till is has terminated. Current running
     * tasks are aborted.
     */
    @Override
    void shutdown();
}
