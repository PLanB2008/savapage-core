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
package org.savapage.core.services.helpers;

import java.io.IOException;
import java.math.BigDecimal;

import org.savapage.core.SpException;
import org.savapage.core.dao.enums.ACLRoleEnum;
import org.savapage.core.dao.enums.ExternalSupplierEnum;
import org.savapage.core.jpa.User;
import org.savapage.core.json.JsonAbstractBase;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Print data from {@link ExternalSupplierEnum} in communication with
 * {@link ThirdPartyEnum} client.
 *
 * @author Rijk Ravestein
 *
 */
@JsonInclude(Include.NON_NULL)
public final class PrintSupplierData extends JsonAbstractBase
        implements ExternalSupplierData {

    /**
     * {@link ThirdPartyEnum} this data is supplied to.
     */
    private ThirdPartyEnum client;

    /**
     * If {@link Boolean#TRUE}, printing cost of client is leading.
     */
    private Boolean clientCost;

    /**
     * If {@link Boolean#TRUE}, accounting transactions for printing cost are
     * send to client.
     */
    private Boolean clientCostTrx;

    /**
     * The transaction weight total.
     */
    private Integer weightTotal;

    /**
     * Media cost total of all printed media.
     */
    private BigDecimal costMedia;

    /**
     * Copy cost total of all printed copies.
     */
    private BigDecimal costCopy;

    /**
     * Cost total for set of copies.
     */
    private BigDecimal costSet;

    /**
     * Optional {@link User#getUserId()} of
     * {@link ACLRoleEnum#JOB_TICKET_OPERATOR}.
     */
    private String operator;

    /**
     * @return {@link ThirdPartyEnum} this data is supplied to.
     */
    public ThirdPartyEnum getClient() {
        return client;
    }

    /**
     * @param client
     *            {@link ThirdPartyEnum} this data is supplied to.
     */
    public void setClient(ThirdPartyEnum client) {
        this.client = client;
    }

    /**
     * @return {@link Boolean#TRUE}, if printing cost of client is leading.
     */
    public Boolean getClientCost() {
        return clientCost;
    }

    /**
     * @param clientCost
     *            If {@link Boolean#TRUE}, printing cost of client is leading.
     */
    public void setClientCost(Boolean clientCost) {
        this.clientCost = clientCost;
    }

    /**
     * @return {@link Boolean#TRUE}, if accounting transactions for printing
     *         cost are send to client.
     */
    public Boolean getClientCostTrx() {
        return clientCostTrx;
    }

    /**
     * @param clientCostTrx
     *            If {@link Boolean#TRUE}, accounting transactions for printing
     *            cost are send to client.
     */
    public void setClientCostTrx(Boolean clientCostTrx) {
        this.clientCostTrx = clientCostTrx;
    }

    /**
     * @return The transaction weight total.
     */
    public Integer getWeightTotal() {
        return weightTotal;
    }

    /**
     * @param weightTotal
     *            The transaction weight total.
     */
    public void setWeightTotal(final Integer weightTotal) {
        this.weightTotal = weightTotal;
    }

    /**
     *
     * @return Cost total of all printed media.
     */
    public BigDecimal getCostMedia() {
        return costMedia;
    }

    /**
     *
     * @param cost
     *            Cost total of all printed media.
     */
    public void setCostMedia(final BigDecimal cost) {
        this.costMedia = cost;
    }

    /**
     *
     * @return Copy cost total of all printed copies.
     */
    public BigDecimal getCostCopy() {
        return costCopy;
    }

    /**
     *
     * @param cost
     *            Copy cost total of all printed copies.
     */
    public void setCostCopy(final BigDecimal cost) {
        this.costCopy = cost;
    }

    /**
     * @return Cost total for set of copies.
     */
    public BigDecimal getCostSet() {
        return costSet;
    }

    /**
     * @param cost
     *            Cost total for set of copies.
     */
    public void setCostSet(final BigDecimal cost) {
        this.costSet = cost;
    }

    /**
     * @return Optional {@link User#getUserId()} of
     *         {@link ACLRoleEnum#JOB_TICKET_OPERATOR}.
     */
    public String getOperator() {
        return operator;
    }

    /**
     * @param operator
     *            {@link User#getUserId()} of
     *            {@link ACLRoleEnum#JOB_TICKET_OPERATOR}.
     */
    public void setOperator(String operator) {
        this.operator = operator;
    }

    /**
     * @return {@code true} when cost is specified.
     */
    @JsonIgnore
    public boolean hasCost() {
        return costCopy != null || costMedia != null || costSet != null;
    }

    /**
     * @return Gets the sums of all costs.
     */
    @JsonIgnore
    public BigDecimal getCostTotal() {

        BigDecimal total = BigDecimal.ZERO;

        if (costSet != null) {
            total = total.add(costSet);
        }
        if (costCopy != null) {
            total = total.add(costCopy);
        }
        if (costMedia != null) {
            total = total.add(costMedia);
        }
        return total;
    }

    @Override
    public final String dataAsString() {
        try {
            return this.stringify();
        } catch (IOException e) {
            throw new SpException(e.getMessage());
        }
    }

    /**
     * Creates an object from data string.
     *
     * @param data
     *            The serialized data.
     * @return The {@link PrintSupplierData} object.
     */
    public static PrintSupplierData createFromData(final String data) {
        return PrintSupplierData.create(PrintSupplierData.class, data);
    }
}
