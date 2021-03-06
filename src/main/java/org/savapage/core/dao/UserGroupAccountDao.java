/*
 * This file is part of the SavaPage project <https://www.savapage.org>.
 * Copyright (c) 2011-2020 Datraverse B.V.
 * Author: Rijk Ravestein.
 *
 * SPDX-FileCopyrightText: 2011-2020 Datraverse B.V. <info@datraverse.com>
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
package org.savapage.core.dao;

import java.util.List;
import java.util.Set;

import org.savapage.core.dto.SharedAccountDto;
import org.savapage.core.jpa.UserGroupAccount;

/**
 *
 * @author Rijk Ravestein
 *
 */
public interface UserGroupAccountDao extends GenericDao<UserGroupAccount> {

    /**
     * .
     */
    class ListFilter {

        private Long userId;
        private String containingNameText;
        private Boolean disabled;
        private Set<Long> accountIds;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getContainingNameText() {
            return containingNameText;
        }

        public void setContainingNameText(String containingNameText) {
            this.containingNameText = containingNameText;
        }

        public Boolean getDisabled() {
            return disabled;
        }

        public void setDisabled(Boolean disabled) {
            this.disabled = disabled;
        }

        public Set<Long> getAccountIds() {
            return accountIds;
        }

        public void setAccountIds(Set<Long> accountIds) {
            this.accountIds = accountIds;
        }

    }

    /**
     *
     * @param filter
     *            The {@link ListFilter}.
     * @return The number of filtered instances.
     */
    long getListCount(ListFilter filter);

    /**
     * Gets the sorted shared accounts.
     *
     * @param filter
     * @param startPosition
     * @param maxResults
     * @param orderBy
     * @param sortAscending
     * @return The list.
     */
    List<SharedAccountDto> getListChunk(ListFilter filter,
            Integer startPosition, Integer maxResults);

}
