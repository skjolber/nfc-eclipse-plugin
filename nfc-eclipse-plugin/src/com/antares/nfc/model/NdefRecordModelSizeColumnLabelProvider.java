package com.antares.nfc.model;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.nfctools.ndef.NdefContext;
import org.nfctools.ndef.NdefMessageEncoder;

public class NdefRecordModelSizeColumnLabelProvider extends ColumnLabelProvider {

	private NdefMessageEncoder encoder = NdefContext.getNdefMessageEncoder();
	
	@Override
	public String getText(Object element) {
		if(element instanceof NdefRecordModelRecord) {
			NdefRecordModelRecord ndefRecordModelRecord = (NdefRecordModelRecord)element;
			
			try {
				byte[] encodeSingle = encoder.encodeSingle(ndefRecordModelRecord.getRecord());
				
				return Integer.toString(encodeSingle.length);
			} catch(Exception e) {
				return "-";
			}
		}
		return null;
	}
}
