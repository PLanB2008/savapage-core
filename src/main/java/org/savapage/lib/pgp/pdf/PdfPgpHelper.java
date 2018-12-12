/*
 * This file is part of the SavaPage project <https://www.savapage.org>.
 * Copyright (c) 2011-2018 Datraverse B.V.
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
package org.savapage.lib.pgp.pdf;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.security.Security;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.savapage.lib.pgp.PGPBaseException;
import org.savapage.lib.pgp.PGPHelper;
import org.savapage.lib.pgp.PGPPublicKeyInfo;
import org.savapage.lib.pgp.PGPSecretKeyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfFileSpecification;
import com.itextpdf.text.pdf.PdfFormField;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PushbuttonField;

/**
 *
 * @author Rijk Ravestein
 *
 */
public final class PdfPgpHelper {

    /** */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(PdfPgpHelper.class);

    /** */
    private static final Font NORMAL_FONT_COURIER = new Font(
            Font.FontFamily.COURIER, 12, Font.NORMAL, BaseColor.DARK_GRAY);

    /**
     * The file name of the ASCII armored OnePass PGP signature stored in the
     * PDF.
     */
    private static final String PGP_PAYLOAD_FILE_NAME = "verification.asc";

    /**
     * Max size of PDF owner password.
     */
    private static final int PDF_OWNER_PASSWORD_SIZE = 32;

    /** */
    private static final boolean ASCII_ARMOR = true;

    /**
     * Rectangle with zero space.
     */
    private static final Rectangle RECT_ZERO = new Rectangle(0, 0);

    /**
     * Name of PDF attachment of ASCII armored public key of PDF Creator
     * (Signer).
     */
    private static final String PGP_PUBKEY_FILENAME_CREATOR = "creator.asc";

    /**
     * Name of PDF attachment of ASCII armored public key of PDF Author.
     */
    private static final String PGP_PUBKEY_FILENAME_AUTHOR = "author.asc";

    /**
     * The PGP mime-type for Armored Encrypted File.
     */
    private static final String PGP_MIMETYPE_ASCII_ARMOR =
            "application/pgp-encrypted";

    /** */
    private static final class SingletonHolder {
        /** */
        static final PdfPgpHelper SINGLETON = new PdfPgpHelper();
    }

    /**
     * Singleton instantiation.
     */
    private PdfPgpHelper() {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * @return The singleton instance.
     */
    public static PdfPgpHelper instance() {
        return SingletonHolder.SINGLETON;
    }

    /**
     * Closes a Stamper ignoring exceptions.
     *
     * @param stamper
     *            The stamper.
     */
    private static void closeQuietly(final PdfStamper stamper) {
        if (stamper != null) {
            try {
                stamper.close();
            } catch (DocumentException | IOException e) {
                // no code intended.
            }
        }
    }

    /**
     *
     * @param stamper
     *            The PDF stamper.
     * @param pubKey
     *            PGP Public key.
     * @param fileDisplay
     *            The file information that is presented to the user.
     * @param fileDescript
     *            File description.
     * @throws IOException
     *             If IO error.
     */
    private static void attachFile(final PdfStamper stamper,
            final PGPPublicKey pubKey, final String fileDisplay,
            final String fileDescript) throws IOException {
        /*
         * Attach Public Key of PDF Creator.
         */
        final int compressionLevel = 0;
        final PdfWriter writer = stamper.getWriter();

        final PdfFileSpecification fsPubKey = PdfFileSpecification.fileEmbedded(
                writer, null, fileDisplay, PGPHelper.encodeArmored(pubKey),
                PGP_MIMETYPE_ASCII_ARMOR, null, compressionLevel);
        fsPubKey.addDescription(fileDescript, false);

        final PdfAnnotation annotPdfSig;
        writer.addFileAttachment(fsPubKey);
        annotPdfSig = new PdfAnnotation(writer, RECT_ZERO);
        stamper.addAnnotation(annotPdfSig, 1);
    }

    /**
     * Gets Public Key attachment of Author from PDF file.
     *
     * @param pdfFile
     *            The signed PDF file.
     * @return {@code null} when not found.
     * @throws IOException
     *             If IO error.
     * @throws PGPBaseException
     *             If PGP error.
     */
    private static PGPPublicKeyInfo getPubKeyAuthor(final File pdfFile)
            throws IOException, PGPBaseException {

        final PdfReader reader = new PdfReader(pdfFile.getAbsolutePath());

        try {
            final PdfDictionary root = reader.getCatalog();
            final PdfDictionary documentnames = root.getAsDict(PdfName.NAMES);
            final PdfDictionary embeddedfiles =
                    documentnames.getAsDict(PdfName.EMBEDDEDFILES);

            final PdfArray filespecs = embeddedfiles.getAsArray(PdfName.NAMES);

            for (int i = 0; i < filespecs.size();) {

                filespecs.getAsString(i++);

                final PdfDictionary filespec = filespecs.getAsDict(i++);
                final PdfDictionary refs = filespec.getAsDict(PdfName.EF);

                for (final PdfName key : refs.getKeys()) {

                    final String attachmentName =
                            filespec.getAsString(key).toString();

                    if (!attachmentName.equals(PGP_PUBKEY_FILENAME_AUTHOR)) {
                        continue;
                    }

                    final PRStream stream = (PRStream) PdfReader
                            .getPdfObject(refs.getAsIndirectObject(key));

                    return PGPHelper.instance()
                            .readPublicKey(new ByteArrayInputStream(
                                    PdfReader.getStreamBytes(stream)));
                }
            }
        } finally {
            reader.close();
        }

        return null;
    }

    /**
     * Appends PGP signature to a PDF as % comment, and adds Verify button with
     * one-pass signed/encrypted Verification Payload URL. Note: the payload is
     * the PDF owner password.
     *
     * @param fileIn
     *            The PDF to sign.
     * @param fileOut
     *            The signed PDF.
     * @param secKeyInfo
     *            The secret key to sign with.
     * @param pubKeyAuthor
     *            Public key of the author ({@code null} when not available.
     * @param pubKeyInfoList
     *            The public keys to encrypt with.
     * @param urlBuilder
     *            The verification URL builder.
     * @param embeddedSignature
     *            If {@code true}, signature if embedded just before %%EOF. If
     *            {@code false} signature if appended just after %%EOF.
     *
     * @throws PGPBaseException
     *             When error.
     */
    public void sign(final File fileIn, final File fileOut,
            final PGPSecretKeyInfo secKeyInfo,
            final PGPPublicKeyInfo pubKeyAuthor,
            final List<PGPPublicKeyInfo> pubKeyInfoList,
            final PdfPgpVerifyUrl urlBuilder, final boolean embeddedSignature)
            throws PGPBaseException {

        boolean verificationParms = false; // TODO

        PdfReader reader = null;
        PdfStamper stamper = null;

        final String ownerPw;

        if (verificationParms) {
            ownerPw = RandomStringUtils.random(PDF_OWNER_PASSWORD_SIZE, true,
                    true);
        } else {
            ownerPw = null;
        }

        try (InputStream pdfIn = new FileInputStream(fileIn);
                OutputStream pdfSigned = new FileOutputStream(fileOut);) {

            reader = new PdfReader(pdfIn);
            stamper = new PdfStamper(reader, pdfSigned);

            if (verificationParms) {
                stamper.setEncryption(null, ownerPw.getBytes(),
                        PdfWriter.ALLOW_PRINTING, PdfWriter.ENCRYPTION_AES_256
                                | PdfWriter.DO_NOT_ENCRYPT_METADATA);
            }

            final PdfWriter writer = stamper.getWriter();

            /*
             * Create the encrypted onepass signature.
             */
            final byte[] payload;

            if (verificationParms) {

                final InputStream istrPayload =
                        new ByteArrayInputStream(ownerPw.getBytes());
                final ByteArrayOutputStream bostrSignedEncrypted =
                        new ByteArrayOutputStream();

                PGPHelper.instance().encryptOnePassSignature(istrPayload,
                        bostrSignedEncrypted, secKeyInfo, pubKeyInfoList,
                        PGP_PAYLOAD_FILE_NAME, new Date(), ASCII_ARMOR);
                payload = bostrSignedEncrypted.toByteArray();

            } else {
                payload = null;
            }

            //
            final int iFirstPage = 1;
            final Rectangle rectPage = reader.getPageSize(iFirstPage);

            /*
             * Add button to open browser.
             */
            final Rectangle rect = new Rectangle(10,
                    rectPage.getTop() - 30 - 10, 100, rectPage.getTop() - 10);

            final PushbuttonField push =
                    new PushbuttonField(writer, rect, "openVerifyURL");

            push.setText("Verify . . .");

            push.setBackgroundColor(BaseColor.LIGHT_GRAY);
            push.setBorderColor(BaseColor.GRAY);
            push.setTextColor(BaseColor.DARK_GRAY);
            push.setFontSize(NORMAL_FONT_COURIER.getSize());
            push.setFont(NORMAL_FONT_COURIER.getBaseFont());
            push.setVisibility(PushbuttonField.VISIBLE_BUT_DOES_NOT_PRINT);

            final String urlVerify =
                    urlBuilder.build(secKeyInfo, payload).toExternalForm();

            final PdfFormField pushButton = push.getField();
            pushButton.setAction(new PdfAction(urlVerify));

            stamper.addAnnotation(pushButton, iFirstPage);

            /*
             *
             */
            final float fontSize = 8f;
            final Font font = new Font(FontFamily.COURIER, fontSize);

            final Phrase header =
                    new Phrase(secKeyInfo.formattedFingerPrint(), font);

            final float x = rect.getRight() + 20;
            final float y = rect.getBottom()
                    + (rect.getTop() - rect.getBottom()) / 2 - fontSize / 2;

            ColumnText.showTextAligned(stamper.getOverContent(iFirstPage),
                    Element.ALIGN_LEFT, header, x, y, 0);

            /*
             * Attach Public Key of PDF Creator and (optionally) Author.
             */
            attachFile(stamper, secKeyInfo.getPublicKey(),
                    PGP_PUBKEY_FILENAME_CREATOR, "PGP Public key of Creator.");

            if (pubKeyAuthor != null) {
                attachFile(stamper, pubKeyAuthor.getMasterKey(),
                        PGP_PUBKEY_FILENAME_AUTHOR,
                        "PGP Public key of Author.");
            }
            //
            stamper.close();
            reader.close();
            reader = null;

            /*
             * Embed or append PGP signature of PDF as PDF comment.
             */
            final ByteArrayOutputStream ostrPdfSig =
                    new ByteArrayOutputStream();

            PGPHelper.instance().createSignature(new FileInputStream(fileOut),
                    ostrPdfSig, secKeyInfo, PGPHelper.CONTENT_SIGN_ALGORITHM,
                    ASCII_ARMOR);

            if (embeddedSignature) {

                final File fileOutSigned = new File(String.format("%s.%s",
                        fileOut.getPath(), UUID.randomUUID().toString()));

                final PdfPgpReader readerForSig = new PdfPgpReaderEmbedSig(
                        new FileOutputStream(fileOutSigned),
                        ostrPdfSig.toByteArray());

                readerForSig.read(new FileInputStream(fileOut));

                fileOut.delete();
                FileUtils.moveFile(fileOutSigned, fileOut);

            } else {

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Sign  : File PDF md5 [{}] [{}] bytes",
                            DigestUtils.md5Hex(new FileInputStream(fileOut)),
                            FileUtils.sizeOf(fileOut));
                }

                final Writer output =
                        new BufferedWriter(new FileWriter(fileOut, true));

                output.append(PdfPgpReader.PDF_COMMENT_PFX).append(
                        new String(ostrPdfSig.toByteArray()).replace("\n",
                                "\n" + PdfPgpReader.PDF_COMMENT_PFX));
                output.close();
            }

        } catch (IOException | DocumentException e) {
            throw new PGPBaseException(e.getMessage(), e);
        } finally {
            closeQuietly(stamper);
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * Verifies a PGP signed (appended or embedded) PDF file.
     *
     * @param pdfFileSigned
     *            Signed PDF as input.
     * @param signPublicKey
     *            The {@link PGPPublicKey} of the private key the PGP signature
     *            content was signed with.
     * @return The {@link PdfPgpSignatureInfo}}.
     * @throws PGPBaseException
     *             When errors.
     * @throws IOException
     *             When File IO errors.
     */
    public PdfPgpSignatureInfo verify(final File pdfFileSigned,
            final PGPPublicKey signPublicKey)
            throws PGPBaseException, IOException {

        final PdfPgpSignatureInfo sigInfo;

        try (InputStream istrPdf = new FileInputStream(pdfFileSigned);) {
            sigInfo = this.verify(istrPdf, signPublicKey);
        }

        if (sigInfo.isValid()) {
            sigInfo.setPubKeyAuthor(getPubKeyAuthor(pdfFileSigned));
        }

        return sigInfo;
    }

    /**
     * Verifies a PGP signed (appended or embedded) PDF file.
     *
     * @param istrPdfSigned
     *            Signed PDF document as input stream.
     * @param trustedPublicKey
     *            The trusted {@link PGPPublicKey}.
     * @return The {@link PdfPgpSignatureInfo}}.
     * @throws PGPBaseException
     *             When errors.
     */
    private PdfPgpSignatureInfo verify(final InputStream istrPdfSigned,
            final PGPPublicKey trustedPublicKey) throws PGPBaseException {

        try (ByteArrayOutputStream ostrPdf = new ByteArrayOutputStream()) {

            final PdfPgpReaderVerify reader = new PdfPgpReaderVerify(ostrPdf);

            reader.read(istrPdfSigned);

            final byte[] pgpBytes = reader.getPgpSignature();

            if (pgpBytes == null) {
                throw new IllegalArgumentException("No signature found");
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("\n{}", new String(pgpBytes));
            }

            final PGPHelper helper = PGPHelper.instance();

            final PGPSignature sig =
                    helper.getSignature(new ByteArrayInputStream(pgpBytes));

            final boolean isValid = PGPHelper.instance().verifySignature(
                    new ByteArrayInputStream(ostrPdf.toByteArray()), sig,
                    trustedPublicKey);

            return new PdfPgpSignatureInfo(sig, isValid);

        } catch (IOException e) {
            throw new PGPBaseException(e.getMessage(), e);
        }
    }

}