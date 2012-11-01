/***************************************************************************
 *
 * This file is part of the NFC Eclipse Plugin project at
 * http://code.google.com/p/nfc-eclipse-plugin/
 *
 * Copyright (C) 2012 by Thomas Rorvik Skjolberg.
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

package org.nfc.eclipse.plugin.model;

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

		map.put(AbsoluteUriRecord.class, new NdefRecordType(AbsoluteUriRecord.class, "Absolute URI Record"));
		map.put(EmptyRecord.class, new NdefRecordType(EmptyRecord.class, "Empty Record"));
		// external  type records
		// map.put(ExternalTypeRecord.class, new NdefRecordType(ExternalTypeRecord.class)); 
		map.put(AndroidApplicationRecord.class, new NdefRecordType(AndroidApplicationRecord.class, "Android Application Record"));
		map.put(GeoRecord.class, new NdefRecordType(GeoRecord.class, "Geo Record"));
		map.put(UnsupportedExternalTypeRecord.class, new NdefRecordType(UnsupportedExternalTypeRecord.class, "External Type Record"));
		
		// mime
		//map.put(MimeRecord.class, new NdefRecordType(MimeRecord.class, "MimeRecord"));
		map.put(BinaryMimeRecord.class, new NdefRecordType(BinaryMimeRecord.class, "Mime Record"));
		
		map.put(UnknownRecord.class, new NdefRecordType(UnknownRecord.class, "Unknown Record"));
		map.put(UnsupportedRecord.class, new NdefRecordType(UnsupportedRecord.class, "Unsupported Record"));
		
		// well-known types
		map.put(ActionRecord.class, new NdefRecordType(ActionRecord.class, "Action Record"));
		map.put(AlternativeCarrierRecord.class, new NdefRecordType(AlternativeCarrierRecord.class, "Alternative Carrier Record"));
		map.put(CollisionResolutionRecord.class, new NdefRecordType(CollisionResolutionRecord.class, "Collision Resolution Record"));
		map.put(ErrorRecord.class, new NdefRecordType(ErrorRecord.class, "Error Record"));
		map.put(GcActionRecord.class, new NdefRecordType(GcActionRecord.class, "Generic Control Action Record"));
		map.put(GcDataRecord.class, new NdefRecordType(GcDataRecord.class, "Generic Control Data Record"));
		map.put(GcTargetRecord.class, new NdefRecordType(GcTargetRecord.class, "Generic Control Target Record"));
		map.put(GenericControlRecord.class, new NdefRecordType(GenericControlRecord.class, "Generic Control Record"));
		map.put(HandoverCarrierRecord.class, new NdefRecordType(HandoverCarrierRecord.class, "Handover Carrier Record"));
		map.put(HandoverRequestRecord.class, new NdefRecordType(HandoverRequestRecord.class, "Handover Request Record"));
		map.put(HandoverSelectRecord.class, new NdefRecordType(HandoverSelectRecord.class, "Handover Select Record"));
		map.put(SignatureRecord.class, new NdefRecordType(SignatureRecord.class, "Signature Record"));
		map.put(SmartPosterRecord.class, new NdefRecordType(SmartPosterRecord.class, "Smart Poster Record"));
		map.put(TextRecord.class, new NdefRecordType(TextRecord.class, "Text Record"));
		map.put(UriRecord.class, new NdefRecordType(UriRecord.class, "URI Record"));
		
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