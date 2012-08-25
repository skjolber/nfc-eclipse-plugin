package com.antares.nfc.plugin.operation;

import org.nfctools.ndef.Record;

import com.antares.nfc.model.NdefRecordModelFactory;
import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.model.NdefRecordModelParent;
import com.antares.nfc.model.NdefRecordModelRecord;
import com.antares.nfc.plugin.NdefRecordFactory;

public class NdefModelAddNodeOperation implements NdefModelOperation {

	private NdefRecordModelParent parent;
	private NdefRecordModelNode child;
	
	private int index;

	public NdefModelAddNodeOperation(NdefRecordModelParent parent, Record child) {
		this(parent, child, -1);
	}
	
	public NdefModelAddNodeOperation(NdefRecordModelParent parent, Record child, int index) {
		this(parent, NdefRecordModelFactory.getNode(child, parent), index);
	}
	
	public NdefModelAddNodeOperation(NdefRecordModelParent parent, NdefRecordModelNode child, int index) {
		this.parent = parent;
		this.child = child;
		this.index = index;
	}

	public NdefModelAddNodeOperation(NdefRecordModelParent parent, NdefRecordModelNode child) {
		this(parent, child, -1);
	}

	@Override
	public void execute() {
		if(parent instanceof NdefRecordModelRecord && child instanceof NdefRecordModelRecord) {
			NdefRecordModelRecord ndefRecordModelRecordParent = (NdefRecordModelRecord)parent;
			
			NdefRecordFactory.connect(ndefRecordModelRecordParent.getRecord(), child.getRecord());
		}
		
		if(index != -1) {
			parent.insert(child, index);
		} else {
			parent.add(child);
		}
	}

	@Override
	public void revoke() {
		if(parent instanceof NdefRecordModelRecord && child instanceof NdefRecordModelRecord) {
			NdefRecordModelRecord ndefRecordModelRecordParent = (NdefRecordModelRecord)parent;
			
			NdefRecordFactory.disconnect(ndefRecordModelRecordParent.getRecord(), child.getRecord());
		}

		parent.remove(child);
		
	}

}
