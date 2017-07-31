/*
 * This file is part of the SavaPage project <https://www.savapage.org>.
 * Copyright (c) 2011-2017 Datraverse B.V.
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * For more information, please contact Datraverse B.V. at this
 * address: info@datraverse.com
 */
package org.savapage.lib.pgp.mime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.ContentDisposition;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import javax.mail.internet.MimeUtility;

import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPSecretKey;

import com.sun.mail.util.CRLFOutputStream;

/**
 *
 * @author Rijk Ravestein
 *
 */
public abstract class PGPBodyPartProcessor {

    /**
     * Secret key (container of private key).
     */
    private final PGPSecretKey secretKey;

    /**
     * Private key for signing.
     */
    private final PGPPrivateKey privateKey;

    /**
     * The content part to sign.
     */
    private BodyPart contentPart;

    /**
     * The processed content part.
     */
    private BodyPart processedPart;

    /**
     *
     * @param keySec
     *            Secret key (container of private key).
     * @param keyPrv
     *            Private key for signing.
     */
    public PGPBodyPartProcessor(final PGPSecretKey keySec,
            final PGPPrivateKey keyPrv) {
        this.secretKey = keySec;
        this.privateKey = keyPrv;
    }

    /**
     * Gets body part as string.
     *
     * @param bodyPart
     *            The body part
     * @return {@link BodyPart} as string.
     * @throws IOException
     *             When output stream error.
     * @throws MessagingException
     *             When mail message error.
     */
    protected static String bodyPartAsString(final BodyPart bodyPart)
            throws IOException, MessagingException {

        updateHeaders(bodyPart);

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final CRLFOutputStream nlos = new CRLFOutputStream(bos);

        try {
            bodyPart.writeTo(nlos);
        } finally {
            nlos.close();
        }
        return bos.toString();
    }

    /**
     * Recursively updates the "Content-Type" and "Content-Transfer-Encoding"
     * headers of a {@link MimeMultipart} or {@link MimePart}. <i>Update for
     * each part is done only when headers are not already set.</i>
     * <p>
     * Note: This method is based on code from private method
     * {@code updateHeaders()} from the MimeBodyPart and the MimeMultipart
     * implementation from SUN.
     * </p>
     *
     * @param part
     *            Either a {@link MimeMultipart} or {@link MimePart}.
     * @throws MessagingException
     *             When mail message error.
     */
    protected static void updateHeaders(final Object part)
            throws MessagingException {

        if (part instanceof MimeMultipart) {
            MimeMultipart mmp = (MimeMultipart) part;
            for (int i = 0; i < mmp.getCount(); i++) {
                // recurse
                updateHeaders(mmp.getBodyPart(i));
            }
        } else if (part instanceof MimePart) {
            MimePart mp = (MimePart) part;
            DataHandler dh = mp.getDataHandler();
            if (dh == null) { // Huh ?
                return;
            }

            try {
                String type = dh.getContentType();
                boolean composite = false;

                ContentType cType = new ContentType(type);
                if (cType.match("multipart/*")) {
                    // If multipart, recurse
                    composite = true;
                    Object o = dh.getContent();
                    updateHeaders(o);
                } else if (cType.match("message/rfc822")) {
                    composite = true;
                }

                // Now, let's update our own headers ...
                // Content-type, but only if we don't already have one
                if (mp.getHeader("Content-Type") == null) {
                    /*
                     * Pull out "filename" from Content-Disposition, and use
                     * that to set the "name" parameter. This is to satisfy
                     * older MUAs (DtMail, Roam and probably a bunch of others).
                     */
                    String s = mp.getHeader("Content-Disposition", null);
                    if (s != null) {
                        // Parse the header ..
                        ContentDisposition cd = new ContentDisposition(s);
                        String filename = cd.getParameter("filename");
                        if (filename != null) {
                            cType.setParameter("name", filename);
                            type = cType.toString();
                        }
                    }
                    mp.setHeader("Content-Type", type);
                }

                // Content-Transfer-Encoding, but only if we don't
                // already have one
                if (!composite // not allowed on composite parts
                        && (mp.getHeader(
                                "Content-Transfer-Encoding") == null)) {
                    mp.setHeader("Content-Transfer-Encoding",
                            MimeUtility.getEncoding(dh));
                }
            } catch (IOException e) {
                throw new MessagingException(e.getMessage(), e);
            }
        }
    }

    /**
     * @return the secretKeyForSigning
     */
    protected PGPSecretKey getSecretKey() {
        return secretKey;
    }

    /**
     * @return Private key for signing.
     */
    protected PGPPrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * @param part
     *            The content part to process.
     */
    public void setContentPart(final BodyPart part) {
        this.contentPart = part;
    }

    /**
     * @return The content part to process.
     */
    public BodyPart getContentPart() {
        return this.contentPart;
    }

    /**
     * @param part
     *            The processed content part.
     */
    protected void setProcessedPart(final BodyPart part) {
        this.processedPart = part;
    }

    /**
     * @return The processed content part.
     */
    public BodyPart getProcessedPart() {
        return this.processedPart;
    }

}
