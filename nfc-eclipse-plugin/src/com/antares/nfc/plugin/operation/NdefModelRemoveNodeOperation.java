package com.antares.nfc.plugin.operation;

import org.nfctools.ndef.Record;

import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.model.NdefRecordModelParent;
import com.antares.nfc.model.NdefRecordModelRecord;
import com.antares.nfc.plugin.NdefRecordFactory;

public class NdefModelRemoveNodeOperation implements NdefModelOperation {

	private NdefRecordModelParent parent;
	private NdefRecordModelNode child;
	
	private int index;

	public NdefModelRemoveNodeOperation(NdefRecordModelParent parent, NdefRecordModelNode child) {
		this.parent = parent;
		this.child = child;
		
		this.index = child.getParentIndex();
		
		initialize();
	}
	
	public void initialize() {
	}

	@Override
	public void execute() {
		Record record = parent.getRecord();
		if(record != null) {
			if(child instanceof NdefRecordModelRecord) {
				NdefRecordFactory.disconnect(record, child.getRecord());
			}
		}

		parent.remove(index);
	}

	@Override
	public void revoke() {
		Record record = parent.getRecord();
		if(record != null) {
			if(child instanceof NdefRecordModelRecord) {
				NdefRecordFactory.connect(record, child.getRecord());
			}
		}
		
		parent.insert(child, index);
	}

}
