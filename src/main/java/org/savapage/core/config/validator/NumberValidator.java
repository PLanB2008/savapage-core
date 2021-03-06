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
package org.savapage.core.config.validator;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Rijk Ravestein
 *
 */
public class NumberValidator implements ConfigPropValidator {

    private final Long minValue;
    private final Long maxValue;

    /**
     * {@code true} if value is optional.
     */
    private final boolean isOptional;

    public NumberValidator(final boolean optional) {
        this.minValue = null;
        this.maxValue = null;
        this.isOptional = optional;
    }

    public NumberValidator(final Long minValue, final Long maxValue,
            final boolean optional) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.isOptional = optional;
    }

    @Override
    public final ValidationResult validate(final String value) {

        final ValidationResult res = new ValidationResult(value);

        if (StringUtils.isBlank(value)) {

            if (!this.isOptional) {
                res.setStatus(ValidationStatusEnum.ERROR_EMPTY);
                res.setMessage("Value must be a number.");
            }

        } else {

            if (StringUtils.isNumeric(value)) {

                final Long longValue = Long.valueOf(value);

                final boolean isValid =
                        (this.minValue == null || longValue >= this.minValue)
                                && (this.maxValue == null
                                        || longValue <= this.maxValue);

                if (!isValid) {
                    res.setStatus(ValidationStatusEnum.ERROR_RANGE);
                    res.setMessage("Value is not in range.");
                }

            } else {

                res.setStatus(ValidationStatusEnum.ERROR_NOT_NUMERIC);
                res.setMessage("Value must contain digits only.");

            }
        }
        return res;
    }

}
