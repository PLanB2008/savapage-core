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
package org.savapage.core.imaging;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Information for a {@link EcoPrintPdfTask}.
 *
 * @author Rijk Ravestein
 *
 */
public final class EcoPrintPdfTaskInfo {

    private final UUID uuid;

    private Path pathTmpDir;

    private File pdfIn;
    private File pdfOut;

    private Integer resolution;

    private String rotation;

    public EcoPrintPdfTaskInfo(final UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Path getPathTmpDir() {
        return pathTmpDir;
    }

    public void setPathTmpDir(Path pathTmpDir) {
        this.pathTmpDir = pathTmpDir;
    }

    public File getPdfIn() {
        return pdfIn;
    }

    public void setPdfIn(File pdfIn) {
        this.pdfIn = pdfIn;
    }

    public File getPdfOut() {
        return pdfOut;
    }

    public void setPdfOut(File pdfOut) {
        this.pdfOut = pdfOut;
    }

    public Integer getResolution() {
        return resolution;
    }

    public void setResolution(Integer resolution) {
        this.resolution = resolution;
    }

    public String getRotation() {
        return rotation;
    }

    public void setRotation(String rotation) {
        this.rotation = rotation;
    }

}