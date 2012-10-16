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

package org.nfc.eclipse.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;

import org.nfctools.ndef.NdefConstants;
import org.nfctools.ndef.NdefContext;
import org.nfctools.ndef.NdefEncoder;
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
import org.nfctools.ndef.wkt.records.GcActionRecord;
import org.nfctools.ndef.wkt.records.GcDataRecord;
import org.nfctools.ndef.wkt.records.GcTargetRecord;
import org.nfctools.ndef.wkt.records.GenericControlRecord;
import org.nfctools.ndef.wkt.records.SignatureRecord;
import org.nfctools.ndef.wkt.records.SignatureRecord.CertificateFormat;
import org.nfctools.ndef.wkt.records.SignatureRecord.SignatureType;
import org.nfctools.ndef.wkt.records.SmartPosterRecord;
import org.nfctools.ndef.wkt.records.TextRecord;
import org.nfctools.ndef.wkt.records.UriRecord;

/**
 * 
 * Tool for creating message to be manually checked in editor.
 * 
 * @author thomas
 *
 */


public class RecordValidatorTool {
	
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
	
	private static GcActionRecord gcActionRecordAction = new GcActionRecord(Action.SAVE_FOR_LATER);
	private static GcActionRecord gcActionRecordRecord = new GcActionRecord(new ActionRecord(Action.SAVE_FOR_LATER));
	private static GcDataRecord gcDataRecord = new GcDataRecord();
	private static GcTargetRecord gcTargetRecord = new GcTargetRecord(new UriRecord("http://ndef.com"));
	private static GenericControlRecord genericControlRecord = new GenericControlRecord(gcTargetRecord, (byte)0x0);

	public static Record[] records = new Record[] { absoluteUriRecord, actionRecord, androidApplicationRecord,
			emptyRecord, textMimeRecord, binaryMimeRecord, smartPosterRecord, textRecord, unknownRecord, uriRecord,
			collisionResolutionRecord, errorRecord,
			alternativeCarrierRecord, handoverSelectRecord, handoverCarrierRecord, handoverRequestRecord,
			
			signatureRecordMarker, signatureRecord,
			
			unsupportedRecord,
			addressInformationGeoRecord, coordinatesGeoRecord, coordinatesAltitudeGeoRecord,
			
			gcActionRecordAction, gcActionRecordRecord, gcDataRecord, gcTargetRecord, genericControlRecord
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
		
		// add some GenericControlRecord
		gcDataRecord.add(new ActionRecord(Action.SAVE_FOR_LATER));
		gcDataRecord.add(new ActionRecord(Action.OPEN_FOR_EDITING));

		genericControlRecord.setAction(gcActionRecordAction);
		genericControlRecord.setTarget(gcTargetRecord);
		genericControlRecord.setData(gcDataRecord);

	}
	
	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws IOException {
		File directory = new File("./resources/ndef/programmatical/");
		
		for(File file : directory.listFiles()) {
			if(file.isFile() && file.getName().endsWith(".ndef")) {
				file.delete();
			}
		}
		
		NdefEncoder ndefMessageEncoder = NdefContext.getNdefEncoder();

		// SINGLE
		for(int i = 0; i < records.length; i++) {
			Record record = records[i];
			File file = new File(directory, record.getClass().getSimpleName() + ".ndef");
			if(file.exists()) {
				int index = 0;
				do {
					index++;
					
					file = new File(directory, record.getClass().getSimpleName() + "#" + index + ".ndef");
				} while(file.exists());
			}
			FileOutputStream fout = new FileOutputStream(file);
			try {
				fout.write(ndefMessageEncoder.encode(record));
			} finally {
				fout.close();
			}
		}

		// ALL
		File file = new File(directory, "all.ndef");
		FileOutputStream fout = new FileOutputStream(file);
		try {
			fout.write(ndefMessageEncoder.encode(records));
		} finally {
			fout.close();
		}
	}
}
