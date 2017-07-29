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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.savapage.lib.pgp.PGPBaseException;
import org.savapage.lib.pgp.PGPHelper;

/**
 * PGP/MIME helper methods.
 *
 * @author Rijk Ravestein
 *
 */
public final class PGPMimeHelper {

    /** */
    private static final class SingletonHolder {
        /** */
        static final PGPMimeHelper SINGLETON = new PGPMimeHelper();
    }

    /**
     * Singleton instantiation.
     */
    private PGPMimeHelper() {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * @return The singleton instance.
     */
    public static PGPMimeHelper instance() {
        return SingletonHolder.SINGLETON;
    }

    /**
     * Creates a PGP signed and encrypted {@link Multipart} from body content
     * and file attachments.
     *
     * @param bodyContent
     *            The body content.
     * @param secretKeyRing
     *            The {@link InputStream} of the private PGP key, used for
     *            signing.
     * @param secretKeyPassword
     *            The password of the private PGP key.
     * @param signPublicKeyList
     *            The list of public PGP key files, used for encryption.
     * @param attachments
     *            The mail attachments.
     * @return The signed and encrypted {@link Multipart}.
     * @throws MessagingException
     *             When mail message error.
     * @throws IOException
     *             When mail attachment {@link File} error.
     * @throws PGPMimeException
     *             When error occurs.
     */
    public Multipart createSignedEncrypted(final String bodyContent,
            final InputStream secretKeyRing, final String secretKeyPassword,
            final List<File> signPublicKeyList, final List<File> attachments)
                    throws PGPMimeException {

        final PGPHelper helper = PGPHelper.instance();

        try {
            final PGPBodyPartEncrypter encrypter = new PGPBodyPartEncrypter(
                    helper.getSecretKey(secretKeyRing), secretKeyPassword,
                    helper.getPublicKeyList(signPublicKeyList));

            final MimeBodyPart mbp = new MimeBodyPart();

            mbp.setText(bodyContent);

            final PGPMimeMultipart mme;

            if (attachments.isEmpty()) {
                mme = PGPMimeMultipart.create(mbp, encrypter);
            } else {
                final MimeMultipart mmultip = new MimeMultipart();

                mmultip.addBodyPart(mbp);

                for (final File attachment : attachments) {
                    final MimeBodyPart mbp2 = new MimeBodyPart();
                    mbp2.attachFile(attachment);
                    mbp2.setFileName(attachment.getName());
                    mmultip.addBodyPart(mbp2);
                }

                mme = PGPMimeMultipart.create(mmultip, encrypter);
            }

            return mme;

        } catch (MessagingException | IOException | PGPBaseException e) {
            throw new PGPMimeException(e.getMessage(), e);
        }
    }

}