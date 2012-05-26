package com.antares.nfc.plugin.operation;

import org.nfctools.ndef.Record;

import com.antares.nfc.model.NdefRecordModelParent;
import com.antares.nfc.model.NdefRecordModelRecord;
import com.antares.nfc.plugin.NdefRecordFactory;

public class NdefModelRemoveRecordOperation implements NdefModelOperation {

	private NdefRecordModelParent parent;
	private NdefRecordModelRecord childNode;
	
	private int index;

	public NdefModelRemoveRecordOperation(NdefRecordModelParent parent, NdefRecordModelRecord childNode) {
		this.parent = parent;
		this.childNode = childNode;
		
		this.index = childNode.getParentIndex();
	}

	@Override
	public void execute() {
		Record record = parent.getRecord();
		if(record != null) {
			NdefRecordFactory.disconnect(record, childNode.getRecord());
		}

		parent.remove(index);
	}

	@Override
	public void revoke() {
		Record record = parent.getRecord();
		if(record != null) {
			NdefRecordFactory.connect(record, childNode.getRecord());
		}
		
		parent.insert(childNode, index);
	}

}
