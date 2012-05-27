package com.antares.nfc.plugin.operation;

import org.nfctools.ndef.Record;

import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.model.NdefRecordModelParent;
import com.antares.nfc.plugin.NdefRecordFactory;

public class NdefModelRecordMoveOperation implements NdefModelOperation {

	private NdefRecordModelNode node;
	
	private NdefRecordModelParent nextParent;
	private int nextIndex;
	
	private NdefRecordModelParent previousParent;
	private int previousIndex;
	
	public NdefModelRecordMoveOperation(NdefRecordModelNode node, NdefRecordModelParent nextParent, int nextIndex) {
		this.node = node;
		this.nextParent = nextParent;
		this.nextIndex = nextIndex;
		
		initialize();
	}

	private void initialize() {
		this.previousIndex = node.getParentIndex();
		this.previousParent = node.getParent();
		
		if(previousParent == nextParent) { // check if remove affects insert index
			int currentIndex = node.getParentIndex();

			if(currentIndex < nextIndex) {
				nextIndex--;
			}
		}
	}

	@Override
	public void execute() {
		
		Record previousRecord = previousParent.getRecord();

		if(previousRecord != null) {
			NdefRecordFactory.disconnect(previousRecord, node.getRecord());
		}

		previousParent.remove(node);

		nextParent.insert(node, nextIndex);

		Record nextRecord = nextParent.getRecord();
		if(nextRecord != null) {
			NdefRecordFactory.connect(nextRecord, node.getRecord());
		}
	}

	@Override
	public void revoke() {
		Record nextRecord = nextParent.getRecord();

		if(nextRecord != null) {
			NdefRecordFactory.disconnect(nextRecord, node.getRecord());
		}

		nextParent.remove(node);

		previousParent.insert(node, previousIndex);

		Record previousRecord = previousParent.getRecord();
		if(previousRecord != null) {
			NdefRecordFactory.connect(previousRecord, node.getRecord());
		}
	}

}
