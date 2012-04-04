package com.antares.nfc.model;

import org.nfctools.ndef.Record;
import org.nfctools.ndef.auri.AbsoluteUriRecord;
import org.nfctools.ndef.empty.EmptyRecord;
import org.nfctools.ndef.ext.AndroidApplicationRecord;
import org.nfctools.ndef.ext.ExternalTypeRecord;
import org.nfctools.ndef.mime.BinaryMimeRecord;
import org.nfctools.ndef.mime.MimeRecord;
import org.nfctools.ndef.unknown.UnknownRecord;
import org.nfctools.ndef.wkt.records.ActionRecord;
import org.nfctools.ndef.wkt.records.AlternativeCarrierRecord;
import org.nfctools.ndef.wkt.records.GcActionRecord;
import org.nfctools.ndef.wkt.records.GcDataRecord;
import org.nfctools.ndef.wkt.records.GcTargetRecord;
import org.nfctools.ndef.wkt.records.GenericControlRecord;
import org.nfctools.ndef.wkt.records.HandoverCarrierRecord;
import org.nfctools.ndef.wkt.records.HandoverRequestRecord;
import org.nfctools.ndef.wkt.records.HandoverSelectRecord;
import org.nfctools.ndef.wkt.records.SmartPosterRecord;
import org.nfctools.ndef.wkt.records.TextRecord;
import org.nfctools.ndef.wkt.records.UriRecord;

public class NdefRecordModelFactory {

	public NdefRecordModelParent represent(Record[] records) {
				
		NdefRecordModelParent ndefRecordModelParent = new NdefRecordModelParent(null);

		for(Record record : records) {
			ndefRecordModelParent.add(getNode(record, ndefRecordModelParent));
		}
		
		return ndefRecordModelParent;
		
	}
	
	public NdefRecordModelNode getNode(Record record, NdefRecordModelParent ndefRecordModelParent) {
		if(record instanceof AndroidApplicationRecord) {
			AndroidApplicationRecord androidApplicationRecord = (AndroidApplicationRecord)record;
			
			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(record, ndefRecordModelParent);

			NdefRecordModelProperty ndefRecordModelProperty = new NdefRecordModelProperty("Package name", androidApplicationRecord.getPackageName(), ndefRecordModelRecord);
			
			ndefRecordModelRecord.add(ndefRecordModelProperty);
			
			return ndefRecordModelRecord;
		} else if(record instanceof ExternalTypeRecord) {
			ExternalTypeRecord externalTypeRecord = (ExternalTypeRecord)record;
			
			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(record, ndefRecordModelParent);

			ndefRecordModelRecord.add(new NdefRecordModelProperty("Namespace", externalTypeRecord.getNamespace(), ndefRecordModelRecord));
			ndefRecordModelRecord.add(new NdefRecordModelProperty("Content", externalTypeRecord.getContent(), ndefRecordModelRecord));
			
			return ndefRecordModelRecord;
		} else if(record instanceof AbsoluteUriRecord) {
			AbsoluteUriRecord uriRecord = (AbsoluteUriRecord)record;
				
			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(record, ndefRecordModelParent);

			NdefRecordModelProperty ndefRecordModelProperty = new NdefRecordModelProperty("URI", uriRecord.getUri(), ndefRecordModelRecord);
			
			ndefRecordModelRecord.add(ndefRecordModelProperty);
			
			return ndefRecordModelRecord;
		} else if(record instanceof SmartPosterRecord) {
			SmartPosterRecord smartPosterRecord = (SmartPosterRecord)record;
			
			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(record, ndefRecordModelParent);

			ndefRecordModelRecord.add(getNode(smartPosterRecord.getTitle(), ndefRecordModelRecord));
			ndefRecordModelRecord.add(getNode( smartPosterRecord.getUri(), ndefRecordModelRecord));
			ndefRecordModelRecord.add(getNode(smartPosterRecord.getAction(), ndefRecordModelRecord));
			
			return ndefRecordModelRecord;
		} else if(record instanceof TextRecord) {
			TextRecord textRecord = (TextRecord)record;

			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(record, ndefRecordModelParent);

			ndefRecordModelRecord.add(new NdefRecordModelProperty("Message", textRecord.getText(), ndefRecordModelRecord));
			ndefRecordModelRecord.add(new NdefRecordModelProperty("Locale", textRecord.getLocale().toString(), ndefRecordModelRecord));
			ndefRecordModelRecord.add(new NdefRecordModelProperty("Encoding", textRecord.getEncoding().displayName(), ndefRecordModelRecord));
			
			return ndefRecordModelRecord;
		} else if(record instanceof ActionRecord) {
			ActionRecord actionRecord = (ActionRecord)record;

			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(record, ndefRecordModelParent);

			NdefRecordModelProperty ndefRecordModelProperty = new NdefRecordModelProperty("Action", actionRecord.getAction().toString(), ndefRecordModelRecord);
			
			ndefRecordModelRecord.add(ndefRecordModelProperty);
			
			return ndefRecordModelRecord;
		} else if(record instanceof MimeRecord) {
			MimeRecord mimeMediaRecord = (MimeRecord)record;

			// go with binary from here
			BinaryMimeRecord binaryMimeRecord;
			if(mimeMediaRecord instanceof BinaryMimeRecord) {
				binaryMimeRecord = (BinaryMimeRecord)mimeMediaRecord;
			} else {
				binaryMimeRecord = new BinaryMimeRecord(mimeMediaRecord.getContentType(), mimeMediaRecord.getContentAsBytes());
			}
			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(binaryMimeRecord, ndefRecordModelParent);

			ndefRecordModelRecord.add(new NdefRecordModelProperty("Mimetype", binaryMimeRecord.getContentType(), ndefRecordModelRecord));
			ndefRecordModelRecord.add(new NdefRecordModelProperty("Content", Integer.toString(binaryMimeRecord.getContentAsBytes().length) + " bytes binary payload", ndefRecordModelRecord));
			
			return ndefRecordModelRecord;
		} else if(record instanceof UnknownRecord) {
			UnknownRecord unknownRecord = (UnknownRecord)record;

			return new NdefRecordModelRecord(unknownRecord, ndefRecordModelParent);
		} else if(record instanceof AlternativeCarrierRecord) {
			AlternativeCarrierRecord alternativeCarrierRecord = (AlternativeCarrierRecord)record;

			return new NdefRecordModelRecord(alternativeCarrierRecord, ndefRecordModelParent);
		} else if(record instanceof HandoverCarrierRecord) {
			HandoverCarrierRecord handoverCarrierRecord = (HandoverCarrierRecord)record;
			
			return new NdefRecordModelRecord(handoverCarrierRecord, ndefRecordModelParent);
		} else if(record instanceof HandoverRequestRecord) {
			HandoverRequestRecord handoverRequestRecord = (HandoverRequestRecord)record;
			
			return new NdefRecordModelRecord(handoverRequestRecord, ndefRecordModelParent);
		} else if(record instanceof HandoverSelectRecord) {
			HandoverSelectRecord handoverSelectRecord = (HandoverSelectRecord)record;
			
			return new NdefRecordModelRecord(handoverSelectRecord, ndefRecordModelParent);
		} else if(record instanceof EmptyRecord) {
			EmptyRecord emptyRecord = (EmptyRecord)record;
				
			return new NdefRecordModelRecord(emptyRecord, ndefRecordModelParent);
		} else if(record instanceof UriRecord) {
			UriRecord uriRecord = (UriRecord)record;
			
			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(uriRecord, ndefRecordModelParent);

			NdefRecordModelProperty ndefRecordModelProperty = new NdefRecordModelProperty("URI", uriRecord.getUri(), ndefRecordModelRecord);
			
			ndefRecordModelRecord.add(ndefRecordModelProperty);
			
			return ndefRecordModelRecord;		
		} else if(record instanceof GenericControlRecord) {
			GenericControlRecord genericControlRecord = (GenericControlRecord)record;
			
			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(genericControlRecord, ndefRecordModelParent);
			NdefRecordModelProperty ndefRecordModelProperty = new NdefRecordModelProperty("Configuration", Byte.toString(genericControlRecord.getConfigurationByte()), ndefRecordModelRecord);
			ndefRecordModelRecord.add(ndefRecordModelProperty);
			
			/**
			 * The Generic Control RTD does not assume
			 * any particular order for these sub-records inside the Generic Control payload. However, it is
			 * RECOMMENDED that a Target record is specified first, an Action record is specified next, and
			 * a Data record is specified at the end for ease of readability and efficiency of processing.
			 * 
			 */
			
			ndefRecordModelRecord.add(getNode(genericControlRecord.getTarget(), ndefRecordModelRecord));
			ndefRecordModelRecord.add(getNode(genericControlRecord.getAction(), ndefRecordModelRecord));
			ndefRecordModelRecord.add(getNode(genericControlRecord.getData(), ndefRecordModelRecord));

			return ndefRecordModelRecord;
		} else if(record instanceof GcTargetRecord) {
			GcTargetRecord gcTargetRecord = (GcTargetRecord)record;
			
			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(gcTargetRecord, ndefRecordModelParent);
			
			// text or uri type
			if(gcTargetRecord.hasTargetIdentifier()) {
				ndefRecordModelRecord.add(getNode(gcTargetRecord.getTargetIdentifier(), ndefRecordModelRecord));
			}
			
			return ndefRecordModelRecord;
		} else if(record instanceof GcActionRecord) {
			GcActionRecord gcActionRecord = (GcActionRecord)record;

			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(gcActionRecord, ndefRecordModelParent);
			
			NdefRecordModelProperty ndefRecordModelProperty = new NdefRecordModelProperty("Action", gcActionRecord.getAction().toString(), ndefRecordModelRecord);
			ndefRecordModelRecord.add(ndefRecordModelProperty);

			if(gcActionRecord.hasActionRecord()) {
				ndefRecordModelRecord.add(getNode(gcActionRecord.getActionRecord(), ndefRecordModelRecord));
			}

			return ndefRecordModelRecord;
		} else if(record instanceof GcDataRecord) {

			GcDataRecord gcDataRecord = (GcDataRecord)record;

			NdefRecordModelRecord ndefRecordModelRecord = new NdefRecordModelRecord(gcDataRecord, ndefRecordModelParent);

			for(Record dataRecord : gcDataRecord.getRecords()) {
				ndefRecordModelRecord.add(getNode(dataRecord, ndefRecordModelRecord));
			}

			return ndefRecordModelRecord;
		} else {
			return new NdefRecordModelRecord(record, ndefRecordModelParent);
		}

		
	}
}
