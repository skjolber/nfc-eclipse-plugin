package com.antares.nfc.plugin.operation;

import org.nfctools.ndef.Record;

import com.antares.nfc.model.NdefRecordModelFactory;
import com.antares.nfc.model.NdefRecordModelParent;
import com.antares.nfc.model.NdefRecordModelRecord;
import com.antares.nfc.plugin.NdefRecordFactory;

public class NdefModelAddRecordOperation implements NdefModelOperation {

	private NdefRecordModelParent parent;
	private Record child;
	private NdefRecordModelRecord childNode;
	
	private int index;

	public NdefModelAddRecordOperation(NdefRecordModelParent parent, Record child, int index) {
		this.parent = parent;
		this.child = child;
		this.index = index;
		
		initialize();
	}

	public void initialize() {
		childNode = NdefRecordModelFactory.getNode(child, parent);
	}
	
	@Override
	public void execute() {
		if(parent instanceof NdefRecordModelRecord) {
			NdefRecordModelRecord ndefRecordModelRecordParent = (NdefRecordModelRecord)parent;
			
			NdefRecordFactory.connect(ndefRecordModelRecordParent.getRecord(), child);
		}
		
		if(index != -1) {
			parent.insert(childNode, index);
		} else {
			parent.add(childNode);
		}
	}

	@Override
	public void revoke() {
		if(parent instanceof NdefRecordModelRecord) {
			NdefRecordModelRecord ndefRecordModelRecordParent = (NdefRecordModelRecord)parent;
			
			NdefRecordFactory.disconnect(ndefRecordModelRecordParent.getRecord(), child);
		}

		parent.remove(childNode);
		
	}

}
