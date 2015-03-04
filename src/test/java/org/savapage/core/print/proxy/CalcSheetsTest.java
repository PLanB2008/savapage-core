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
package org.savapage.core.print.proxy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.savapage.core.services.impl.AbstractProxyPrintService;

/**
 * Test cases for
 * {@link AbstractProxyPrintService#calcNumberOfPrintedSheets(int, int, boolean, int, boolean, boolean, boolean)}
 * .
 * <p>
 * References:
 * <ul>
 * <li><a
 * href="http://www.vogella.com/articles/JUnit/article.html">www.vogella.com</a>
 * <li>
 * </ul>
 * </p>
 *
 * @author Datraverse B.V.
 *
 */
public class CalcSheetsTest {

    @Test
    public void test1() {

        boolean duplex = false;
        boolean oddOrEvenSheets = false;
        boolean coverPageBefore = false;
        boolean coverPageAfter = false;

        int numberOfPages = 1;
        int nUp = 1;
        int copies = 1;

        // ----------------
        assertEquals("1 page, 1-up", 1,
                AbstractProxyPrintService.calcNumberOfPrintedSheets(
                        numberOfPages, copies, duplex, nUp, oddOrEvenSheets,
                        coverPageBefore, coverPageAfter));
    }

    @Test
    public void test2() {

        boolean duplex = false;
        boolean oddOrEvenSheets = false;
        boolean coverPageBefore = false;
        boolean coverPageAfter = false;

        int numberOfPages = 2;
        int nUp = 4;
        int copies = 1;

        assertEquals("2 pages, 4-up", 1,
                AbstractProxyPrintService.calcNumberOfPrintedSheets(
                        numberOfPages, copies, duplex, nUp, oddOrEvenSheets,
                        coverPageBefore, coverPageAfter));
    }

    @Test
    public void test3() {

        boolean duplex = false;
        boolean oddOrEvenSheets = false;
        boolean coverPageBefore = false;
        boolean coverPageAfter = false;

        int numberOfPages = 2;
        int nUp = 2;
        int copies = 1;

        assertEquals("2 pages, 2-up", 1,
                AbstractProxyPrintService.calcNumberOfPrintedSheets(
                        numberOfPages, copies, duplex, nUp, oddOrEvenSheets,
                        coverPageBefore, coverPageAfter));
    }

    @Test
    public void test4() {

        boolean duplex = false;
        boolean oddOrEvenSheets = false;
        boolean coverPageBefore = false;
        boolean coverPageAfter = false;

        int numberOfPages = 3;
        int nUp = 2;
        int copies = 1;

        assertEquals("3 pages, 2-up", 2,
                AbstractProxyPrintService.calcNumberOfPrintedSheets(
                        numberOfPages, copies, duplex, nUp, oddOrEvenSheets,
                        coverPageBefore, coverPageAfter));
    }

    @Test
    public void test5() {

        boolean duplex = false;
        boolean oddOrEvenSheets = false;
        boolean coverPageBefore = false;
        boolean coverPageAfter = false;

        int numberOfPages = 3;
        int nUp = 4;
        int copies = 2;

        assertEquals("3 pages, 4-up, 2 copies", 2,
                AbstractProxyPrintService.calcNumberOfPrintedSheets(
                        numberOfPages, copies, duplex, nUp, oddOrEvenSheets,
                        coverPageBefore, coverPageAfter));
    }

}
