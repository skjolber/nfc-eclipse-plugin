package com.antares.nfc.plugin.operation;

import org.nfctools.ndef.Record;
import org.nfctools.ndef.wkt.handover.records.AlternativeCarrierRecord;

import com.antares.nfc.model.NdefRecordModelPropertyList;
import com.antares.nfc.model.NdefRecordModelPropertyListItem;

public class NdefModelAddListItemOperation implements NdefModelOperation {

	private NdefRecordModelPropertyList parent;
	private NdefRecordModelPropertyListItem childNode;
	
	private int index;
	private String value;

	public NdefModelAddListItemOperation(NdefRecordModelPropertyList parent, int index, String value) {
		this.parent = parent;
		this.index = index;
		this.value = value;
		
		initialize();
	}
	
	private void initialize() {
		childNode = new NdefRecordModelPropertyListItem(value, parent);
	}
	
	@Override
	public void execute() {
		Record record = parent.getRecord();
		
		if(record instanceof AlternativeCarrierRecord) {
			AlternativeCarrierRecord alternativeCarrierRecord = (AlternativeCarrierRecord)record;
			
			alternativeCarrierRecord.insertAuxiliaryDataReference(value, index);
		}	
		
		parent.insert(childNode, index);
	}

	@Override
	public void revoke() {
		Record record = parent.getRecord();
		
		if(record instanceof AlternativeCarrierRecord) {
			AlternativeCarrierRecord alternativeCarrierRecord = (AlternativeCarrierRecord)record;
			
			value = alternativeCarrierRecord.removeAuxiliaryDataReference(index);
		}	

		parent.remove(index);
	}


}
