package com.antares.nfc.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.nfctools.ndef.Record;
import org.nfctools.ndef.auri.AbsoluteUriRecord;
import org.nfctools.ndef.empty.EmptyRecord;
import org.nfctools.ndef.ext.AndroidApplicationRecord;
import org.nfctools.ndef.ext.GeoRecord;
import org.nfctools.ndef.ext.UnsupportedExternalTypeRecord;
import org.nfctools.ndef.mime.BinaryMimeRecord;
import org.nfctools.ndef.mime.TextMimeRecord;
import org.nfctools.ndef.unknown.UnknownRecord;
import org.nfctools.ndef.unknown.unsupported.UnsupportedRecord;
import org.nfctools.ndef.wkt.handover.records.AlternativeCarrierRecord;
import org.nfctools.ndef.wkt.handover.records.CollisionResolutionRecord;
import org.nfctools.ndef.wkt.handover.records.ErrorRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverCarrierRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverRequestRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverSelectRecord;
import org.nfctools.ndef.wkt.records.ActionRecord;
import org.nfctools.ndef.wkt.records.GcActionRecord;
import org.nfctools.ndef.wkt.records.GcDataRecord;
import org.nfctools.ndef.wkt.records.GcTargetRecord;
import org.nfctools.ndef.wkt.records.GenericControlRecord;
import org.nfctools.ndef.wkt.records.SignatureRecord;
import org.nfctools.ndef.wkt.records.SmartPosterRecord;
import org.nfctools.ndef.wkt.records.TextRecord;
import org.nfctools.ndef.wkt.records.UriRecord;

/**
 * 
 * Class for uniform naming of different record types
 * 
 * @author thomas
 *
 */

public class NdefRecordType {
	
	public static final Comparator<NdefRecordType> comparator = new Comparator<NdefRecordType>() {

		@Override
		public int compare(NdefRecordType o1, NdefRecordType o2) {
			return o1.getRecordLabel().compareTo(o2.getRecordLabel());
		}
		
	};
	
	private static final Map<Class<? extends Record>, NdefRecordType> records;
	static {
		Map<Class<? extends Record>, NdefRecordType> map = new ConcurrentHashMap<Class<? extends Record>, NdefRecordType>();

		map.put(AbsoluteUriRecord.class, new NdefRecordType(AbsoluteUriRecord.class));
		map.put(EmptyRecord.class, new NdefRecordType(EmptyRecord.class));
		// external  type records
		// map.put(ExternalTypeRecord.class, new NdefRecordType(ExternalTypeRecord.class)); 
		map.put(AndroidApplicationRecord.class, new NdefRecordType(AndroidApplicationRecord.class));
		map.put(GeoRecord.class, new NdefRecordType(GeoRecord.class));
		map.put(UnsupportedExternalTypeRecord.class, new NdefRecordType(UnsupportedExternalTypeRecord.class, "ExternalType"));
		
		// mime
		//map.put(MimeRecord.class, new NdefRecordType(MimeRecord.class, "MimeRecord"));
		map.put(BinaryMimeRecord.class, new NdefRecordType(BinaryMimeRecord.class, "MimeRecord"));
		map.put(TextMimeRecord.class, new NdefRecordType(TextMimeRecord.class)); // strictly not necessary
		
		map.put(UnknownRecord.class, new NdefRecordType(UnknownRecord.class));
		map.put(UnsupportedRecord.class, new NdefRecordType(UnsupportedRecord.class));
		
		// well-known types
		map.put(ActionRecord.class, new NdefRecordType(ActionRecord.class));
		map.put(AlternativeCarrierRecord.class, new NdefRecordType(AlternativeCarrierRecord.class));
		map.put(CollisionResolutionRecord.class, new NdefRecordType(CollisionResolutionRecord.class));
		map.put(ErrorRecord.class, new NdefRecordType(ErrorRecord.class));
		map.put(GcActionRecord.class, new NdefRecordType(GcActionRecord.class));
		map.put(GcDataRecord.class, new NdefRecordType(GcDataRecord.class));
		map.put(GcTargetRecord.class, new NdefRecordType(GcTargetRecord.class));
		map.put(GenericControlRecord.class, new NdefRecordType(GenericControlRecord.class));
		map.put(HandoverCarrierRecord.class, new NdefRecordType(HandoverCarrierRecord.class));
		map.put(HandoverRequestRecord.class, new NdefRecordType(HandoverRequestRecord.class));
		map.put(HandoverSelectRecord.class, new NdefRecordType(HandoverSelectRecord.class));
		map.put(SignatureRecord.class, new NdefRecordType(SignatureRecord.class));
		map.put(SmartPosterRecord.class, new NdefRecordType(SmartPosterRecord.class));
		map.put(TextRecord.class, new NdefRecordType(TextRecord.class));
		map.put(UriRecord.class, new NdefRecordType(UriRecord.class));
		
		records = map;
	}

	public static NdefRecordType[] sort(NdefRecordType[] types) {
		ArrayList<NdefRecordType> list = new ArrayList<NdefRecordType>(types.length);
		for(NdefRecordType type : types) {
			list.add(type);
		}
		Collections.sort(list, comparator);
		
		return list.toArray(new NdefRecordType[list.size()]);
	}

	public static void sort(List<NdefRecordType> types) {
		Collections.sort(types, comparator);
	}
	
	public static NdefRecordType getType(Class<? extends Record> c) {
		
		NdefRecordType ndefRecordType = records.get(c);
		
		if(ndefRecordType == null) {
			throw new IllegalArgumentException("Unknown record type " + c.getName());
		}
		
		return ndefRecordType;
	}
	
	private Class<? extends Record> c;
	private String label;
	
	private NdefRecordType(Class<? extends Record> c) {
		this.c = c;
		this.label = c.getSimpleName();
	}

	private NdefRecordType(Class<? extends Record> c, String label) {
		this.c = c;
		this.label = label;
	}

	public String getRecordLabel() {
		return label;
	}
	
	public Class<? extends Record> getRecordClass() {
		return c;
	}
}