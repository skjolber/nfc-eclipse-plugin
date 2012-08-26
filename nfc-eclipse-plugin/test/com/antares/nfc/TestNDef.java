/***************************************************************************
 *
 * This file is part of the NFC Eclipse Plugin project at
 * http://code.google.com/p/nfc-eclipse-plugin/
 *
 * Copyright (C) 2012 by Thomas Rorvik Skjolberg / Antares Gruppen AS.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ****************************************************************************/

package com.antares.nfc;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.nfctools.ndef.NdefConstants;
import org.nfctools.ndef.NdefContext;
import org.nfctools.ndef.NdefMessage;
import org.nfctools.ndef.NdefMessageDecoder;
import org.nfctools.ndef.NdefMessageEncoder;
import org.nfctools.ndef.Record;
import org.nfctools.ndef.auri.AbsoluteUriRecord;
import org.nfctools.ndef.empty.EmptyRecord;
import org.nfctools.ndef.ext.AndroidApplicationRecord;
import org.nfctools.ndef.ext.GeoRecord;
import org.nfctools.ndef.mime.BinaryMimeRecord;
import org.nfctools.ndef.mime.TextMimeRecord;
import org.nfctools.ndef.unknown.UnknownRecord;
import org.nfctools.ndef.unknown.unsupported.UnsupportedRecord;
import org.nfctools.ndef.wkt.handover.records.AlternativeCarrierRecord;
import org.nfctools.ndef.wkt.handover.records.AlternativeCarrierRecord.CarrierPowerState;
import org.nfctools.ndef.wkt.handover.records.CollisionResolutionRecord;
import org.nfctools.ndef.wkt.handover.records.ErrorRecord;
import org.nfctools.ndef.wkt.handover.records.ErrorRecord.ErrorReason;
import org.nfctools.ndef.wkt.handover.records.HandoverCarrierRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverCarrierRecord.CarrierTypeFormat;
import org.nfctools.ndef.wkt.handover.records.HandoverRequestRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverSelectRecord;
import org.nfctools.ndef.wkt.records.Action;
import org.nfctools.ndef.wkt.records.ActionRecord;
import org.nfctools.ndef.wkt.records.SignatureRecord;
import org.nfctools.ndef.wkt.records.SignatureRecord.CertificateFormat;
import org.nfctools.ndef.wkt.records.SignatureRecord.SignatureType;
import org.nfctools.ndef.wkt.records.SmartPosterRecord;
import org.nfctools.ndef.wkt.records.TextRecord;
import org.nfctools.ndef.wkt.records.UriRecord;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.binary.BinaryQRCodeReader;
import com.google.zxing.qrcode.binary.BinaryQRCodeWriter;

public class TestNDef {

	private static AbsoluteUriRecord absoluteUriRecord = new AbsoluteUriRecord("http://absolute.url");
	private static ActionRecord actionRecord = new ActionRecord(Action.SAVE_FOR_LATER);
	private static AndroidApplicationRecord androidApplicationRecord = new AndroidApplicationRecord("com.skjolberg.nfc");
	private static EmptyRecord emptyRecord = new EmptyRecord();
	private static TextMimeRecord textMimeRecord = new TextMimeRecord("text/xml; charset=utf-8", "abcd...���");
	private static BinaryMimeRecord binaryMimeRecord = new BinaryMimeRecord("application/binary",
			"<?xml version=\"1.0\" encoding=\"utf-8\"?><manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" />"
					.getBytes());
	private static SmartPosterRecord smartPosterRecord = new SmartPosterRecord(new TextRecord("Title message",
			Charset.forName("UTF-8"), new Locale("no")), new UriRecord("http://smartposter.uri"), new ActionRecord(
			Action.OPEN_FOR_EDITING));
	private static TextRecord textRecord = new TextRecord("Text message", Charset.forName("UTF-8"), new Locale("no"));
	private static UnknownRecord unknownRecord = new UnknownRecord(new byte[]{0x00, 0x01, 0x02, 0x03});
	private static UriRecord uriRecord = new UriRecord("http://wellknown.url");
	
	private static CollisionResolutionRecord collisionResolutionRecord = new CollisionResolutionRecord((short)123);
	private static ErrorRecord errorRecord = new ErrorRecord(ErrorReason.PermanenteMemoryConstraints, new Long(321L));
	
	private static AlternativeCarrierRecord alternativeCarrierRecord = new AlternativeCarrierRecord(CarrierPowerState.Active, "http://blabla");
	private static HandoverSelectRecord handoverSelectRecord = new HandoverSelectRecord();
	private static HandoverCarrierRecord handoverCarrierRecord = new HandoverCarrierRecord(CarrierTypeFormat.AbsoluteURI, "http://absolute.url", new byte[]{0x00, 0x01, 0x02, 0x03});

	private static HandoverRequestRecord handoverRequestRecord = new HandoverRequestRecord(new CollisionResolutionRecord((short)321));

	private static SignatureRecord signatureRecord = new SignatureRecord(SignatureRecord.SignatureType.NOT_PRESENT, new byte[]{0x00, 0x01, 0x10, 0x11}, CertificateFormat.X_509, "http://certificate.uri");
	private static SignatureRecord signatureRecordMarker = new SignatureRecord(SignatureRecord.SignatureType.NOT_PRESENT);
	
	private static UnsupportedRecord unsupportedRecord = new UnsupportedRecord(NdefConstants.TNF_RESERVED, "abc".getBytes(), "id".getBytes(), "DEF".getBytes());
	private static GeoRecord addressInformationGeoRecord = new GeoRecord("Oslo");
	private static GeoRecord coordinatesGeoRecord = new GeoRecord(59.949444, 10.756389);
	private static GeoRecord coordinatesAltitudeGeoRecord = new GeoRecord(59.949444, 10.756389, 100.0);
	
	public static Record[] records = new Record[] { absoluteUriRecord, actionRecord, androidApplicationRecord,
			emptyRecord, textMimeRecord, binaryMimeRecord, smartPosterRecord, textRecord, unknownRecord, uriRecord,
			collisionResolutionRecord, errorRecord,
			alternativeCarrierRecord, handoverSelectRecord, handoverCarrierRecord, handoverRequestRecord,
			
			signatureRecordMarker, signatureRecord,
			
			unsupportedRecord,
			addressInformationGeoRecord, coordinatesGeoRecord, coordinatesAltitudeGeoRecord
			};


	static {
		// handover request record requires at least on alternative carrier record
		AlternativeCarrierRecord alternativeCarrierRecord = new AlternativeCarrierRecord(CarrierPowerState.Active, "z");
		alternativeCarrierRecord.addAuxiliaryDataReference("a");
		alternativeCarrierRecord.addAuxiliaryDataReference("b");
		handoverRequestRecord.add(alternativeCarrierRecord);
		
		alternativeCarrierRecord = new AlternativeCarrierRecord(CarrierPowerState.Active, "y");
		alternativeCarrierRecord.addAuxiliaryDataReference("c");
		alternativeCarrierRecord.addAuxiliaryDataReference("d");

		handoverRequestRecord.add(alternativeCarrierRecord);

		handoverSelectRecord.add(alternativeCarrierRecord);
		handoverSelectRecord.setError(new ErrorRecord(ErrorReason.PermanenteMemoryConstraints, new Long(1L)));
		
		// add some certificates to signature
		signatureRecord.addCertificate(new byte[]{0x00, 0x10, 0x11});
		signatureRecord.setSignatureType(SignatureType.RSASSA_PSS_SHA_1);
		signatureRecord.setSignature(new byte[]{0x01, 0x11, 0x12});
	}


	@Test
	public void testRoundSingle() throws Exception {

		int awidth = 500;
		int aheight = 500;

		NdefMessageEncoder ndefMessageEncoder = NdefContext.getNdefMessageEncoder();

		for(Record record : records) {
			List<Record> originalRecords = new ArrayList<Record>();
			originalRecords.add(record);
			byte[] ndef = ndefMessageEncoder.encode(originalRecords);

			NdefMessageDecoder ndefMessageDecoder = NdefContext.getNdefMessageDecoder();
			NdefMessage decode = ndefMessageDecoder.decode(ndef);

			List<Record> roundTripRecordes = ndefMessageDecoder.decodeToRecords(decode);
			
			for(int i = 0; i < roundTripRecordes.size(); i++) {
				System.out.println(originalRecords.get(i));
				assertEquals(Integer.toString(i),  originalRecords.get(i), roundTripRecordes.get(i));
			}

			//get a byte matrix for the data
			BinaryQRCodeWriter writer = new BinaryQRCodeWriter();
			BitMatrix matrix = writer.encode(ndef, com.google.zxing.BarcodeFormat.QR_CODE, awidth, aheight);


			//generate an image from the byte matrix
			int width = matrix.getWidth(); 
			int height = matrix.getHeight(); 

			//create buffered image to draw to
			BufferedImage image = new BufferedImage(width * 2, height * 4, BufferedImage.TYPE_INT_RGB);
			image.getGraphics().setColor(Color.white);
			image.getGraphics().fillRect(0, 0, image.getWidth(), image.getHeight());
			//iterate through the matrix and draw the pixels to the image
			for (int y = 0; y < height; y++) { 
				for (int x = 0; x < width; x++) { 
					int grayValue = matrix.get(x, y) ? 0 : 0xff; 
					image.setRGB((image.getWidth() - width) / 2 + x, (image.getHeight() - height) / 2 + y, (grayValue == 0 ? 0 : 0xFFFFFF));
				}
			}

			//write the image to the output stream
			File output = new File("./output/" + record.getClass().getSimpleName() + ".png");
			FileOutputStream fout = new FileOutputStream(output);
			ImageIO.write(image, "png", fout);
			fout.close();

			System.out.println("Wrote " + output);

			//convert the image to a binary bitmap source
			LuminanceSource source = new BufferedImageLuminanceSource(image);
			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

			//decode the barcode
			BinaryQRCodeReader reader = new BinaryQRCodeReader();

			try {			
					byte[] result = reader.decode(bitmap);
			
					System.out.println("Got result " + result.length);
			
					for(int i = 0; i < ndef.length; i++) {
						assertEquals(ndef[i], result[i]); 
					}
			} catch(Exception e) {
				System.out.println("Problem decoding " + record.getClass().getSimpleName());
				e.printStackTrace();
			}
			 
		}
	}

	@Test
	public void testRoundTripMultiple() throws Exception {

		int awidth = 400;
		int aheight = 400;

		NdefMessageEncoder ndefMessageEncoder = NdefContext.getNdefMessageEncoder();

		List<Record> list = new ArrayList<Record>();
		for(Record record : records) {
			list.add(record);
		}
		byte[] ndef = ndefMessageEncoder.encode(list);

		NdefMessageDecoder ndefMessageDecoder = NdefContext.getNdefMessageDecoder();
		NdefMessage decode = ndefMessageDecoder.decode(ndef);

		List<Record> lists = ndefMessageDecoder.decodeToRecords(decode);

		//get a byte matrix for the data
		BinaryQRCodeWriter writer = new BinaryQRCodeWriter();
		BitMatrix matrix = writer.encode(ndef, com.google.zxing.BarcodeFormat.QR_CODE, awidth, aheight);


		//generate an image from the byte matrix
		int width = matrix.getWidth(); 
		int height = matrix.getHeight(); 

		//create buffered image to draw to
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		//iterate through the matrix and draw the pixels to the image
		for (int y = 0; y < height; y++) { 
			for (int x = 0; x < width; x++) { 
				int grayValue = matrix.get(x, y) ? 0 : 0xff; 
				image.setRGB(x, y, (grayValue == 0 ? 0 : 0xFFFFFF));
			}
		}

		//write the image to the output stream
		File output = new File("./output/ndef.png");
		FileOutputStream fout = new FileOutputStream(output);
		ImageIO.write(image, "png", fout);
		fout.close();

		System.out.println("Wrote " + output);

		//convert the image to a binary bitmap source
		LuminanceSource source = new BufferedImageLuminanceSource(image);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

		//decode the barcode
		BinaryQRCodeReader reader = new BinaryQRCodeReader();

		byte[] result = reader.decode(bitmap);

		System.out.println("Got result " + result.length);

		for(int i = 0; i < ndef.length; i++) {
			assertEquals(ndef[i], result[i]); 
		}
	}

}
