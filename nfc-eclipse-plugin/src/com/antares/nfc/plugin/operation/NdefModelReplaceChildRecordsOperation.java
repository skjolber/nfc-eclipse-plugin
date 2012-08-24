package com.antares.nfc.plugin.operation;

import java.util.List;

import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.model.NdefRecordModelParent;

/**
 * 
 * Operation to replace parent records 
 * 
 * @author thomas
 *
 */

public class NdefModelReplaceChildRecordsOperation  implements NdefModelOperation {

	private NdefRecordModelParent parent;
	private List<NdefRecordModelNode> previous;
	private List<NdefRecordModelNode> next;
	
	public NdefModelReplaceChildRecordsOperation(NdefRecordModelParent parent, List<NdefRecordModelNode> previous, List<NdefRecordModelNode> next) {
		this.parent = parent;
		this.previous = previous;
		this.next = next;
	}

	@Override
	public void execute() {
		parent.setChildren(next);
	}

	@Override
	public void revoke() {
		parent.setChildren(previous);
	}

}
