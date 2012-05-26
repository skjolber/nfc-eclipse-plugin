package com.antares.nfc.plugin.operation;

import org.nfctools.ndef.Record;
import org.nfctools.ndef.wkt.handover.records.AlternativeCarrierRecord;

import com.antares.nfc.model.NdefRecordModelPropertyList;
import com.antares.nfc.model.NdefRecordModelPropertyListItem;

public class NdefModelRemoveListItemOperation implements NdefModelOperation {

	private NdefRecordModelPropertyList parent;
	private NdefRecordModelPropertyListItem childNode;
	
	private int index;
	private String value;

	public NdefModelRemoveListItemOperation(NdefRecordModelPropertyList parent, NdefRecordModelPropertyListItem childNode) {
		this.parent = parent;
		this.childNode = childNode;
		
		initialize();
	}
	
	private void initialize() {
		this.index = childNode.getParentIndex();
		
		Record record = parent.getRecord();

		if(record instanceof AlternativeCarrierRecord) {
			AlternativeCarrierRecord alternativeCarrierRecord = (AlternativeCarrierRecord)record;
			
			value = alternativeCarrierRecord.getAuxiliaryDataReferenceAt(index);
		}

	}

	@Override
	public void execute() {
		Record record = parent.getRecord();
		
		if(record instanceof AlternativeCarrierRecord) {
			AlternativeCarrierRecord alternativeCarrierRecord = (AlternativeCarrierRecord)record;
			
			value = alternativeCarrierRecord.removeAuxiliaryDataReference(index);
		}	

		parent.remove(index);
	}

	@Override
	public void revoke() {
		Record record = parent.getRecord();
		
		if(record instanceof AlternativeCarrierRecord) {
			AlternativeCarrierRecord alternativeCarrierRecord = (AlternativeCarrierRecord)record;
			
			alternativeCarrierRecord.insertAuxiliaryDataReference(value, index);
		}	
		
		parent.insert(childNode, index);
	}
	

}
