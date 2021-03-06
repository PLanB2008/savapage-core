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
package org.savapage.core.doc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.savapage.core.pdf.ITextPdfCreator;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;

/**
 * Converts a PDF file to 2-up duplex booklet page ordering.
 *
 * @author Rijk Ravestein
 *
 */
public final class PdfToBooklet extends AbstractPdfConverter
        implements IPdfConverter {

    /**
     * A unique suffix to type the kind of PDF convert.
     */
    private static final String OUTPUT_FILE_SFX = "booklet";

    /**
     * Number of virtual pages on 2-up duplex sheet.
     */
    private static final int PAGES_ON_SHEET = 4;

    /**
     *
     */
    public PdfToBooklet() {
        super();
    }

    /**
     *
     * @param createDir
     *            The directory location of the created file.
     */
    public PdfToBooklet(final File createDir) {
        super(createDir);
    }

    /**
     * Gets booklet page order.
     *
     * @param nSheets
     *            Number of booklet sheets.
     * @return Array with 1-based page ordinals.
     */
    private static int[] getPageOrder(final int nSheets) {

        final int[] pageOrder = new int[nSheets * PAGES_ON_SHEET];

        int nWlkEnd = nSheets * PAGES_ON_SHEET;
        int nWlkStart = 1;
        int j = 0;

        for (int i = 0; i < nSheets; i++) {
            pageOrder[j++] = nWlkEnd--;
            pageOrder[j++] = nWlkStart++;
            pageOrder[j++] = nWlkStart++;
            pageOrder[j++] = nWlkEnd--;
        }
        return pageOrder;
    }

    /**
     * Calculates the number of blank pages to be appended.
     *
     * @param nPages
     *            Number of pages in input PDF document.
     * @return Number of blank pages.
     */
    private static int calcBlankPages(final int nPages) {
        int nPagesBlank = 0;

        while (true) {
            if ((nPages + nPagesBlank) % PAGES_ON_SHEET == 0) {
                break;
            }
            nPagesBlank++;
        }
        return nPagesBlank;
    }

    @Override
    public File convert(final File pdfFile) throws IOException {

        final File pdfOut = getOutputFile(pdfFile);
        final OutputStream ostr = new FileOutputStream(pdfOut);

        final Document doc = new Document();
        final PdfCopy pdfCopy;

        try {
            pdfCopy = new PdfCopy(doc, ostr);
            doc.open();
        } catch (DocumentException e) {
            throw new IOException(e.getMessage());
        }

        //
        final PdfReader reader = new PdfReader(new FileInputStream(pdfFile));
        final int nPagesMax = reader.getNumberOfPages();
        final int nPagesBlank = calcBlankPages(nPagesMax);

        //
        boolean exception = true;
        PdfReader singleBlankPagePdfReader = null;

        pdfCopy.setLinearPageMode();

        try {

            for (int i = 1; i <= nPagesMax; i++) {
                pdfCopy.addPage(pdfCopy.getImportedPage(reader, i));
            }

            if (nPagesBlank > 0) {
                singleBlankPagePdfReader = ITextPdfCreator
                        .createBlankPageReader(reader.getPageSize(1));
                for (int i = 1; i <= nPagesBlank; i++) {
                    pdfCopy.addPage(pdfCopy
                            .getImportedPage(singleBlankPagePdfReader, 1));
                }
            }
            // Reorder pages
            pdfCopy.reorderPages(
                    getPageOrder((nPagesMax + nPagesBlank) / PAGES_ON_SHEET));

            exception = false;

        } catch (DocumentException e) {
            throw new IOException(e.getMessage());
        } finally {
            if (singleBlankPagePdfReader != null) {
                singleBlankPagePdfReader.close();
            }
            doc.close();
            if (exception) {
                pdfOut.delete();
            }
        }
        return pdfOut;
    }

    @Override
    protected String getOutputFileSfx() {
        return OUTPUT_FILE_SFX;
    }

}
