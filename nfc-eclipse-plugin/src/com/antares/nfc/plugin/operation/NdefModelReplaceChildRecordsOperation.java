package com.antares.nfc.plugin.operation;

import java.util.ArrayList;
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

	public NdefModelReplaceChildRecordsOperation(NdefRecordModelParent parent, NdefRecordModelNode previous, NdefRecordModelNode next) {
		this.parent = parent;
		
		this.previous = new ArrayList<NdefRecordModelNode>();
		this.previous.add(previous);
		this.next = new ArrayList<NdefRecordModelNode>();
		this.next.add(next);
		
		initialize();
	}

	public NdefModelReplaceChildRecordsOperation(NdefRecordModelParent parent, List<NdefRecordModelNode> previous, List<NdefRecordModelNode> next) {
		this.parent = parent;
		this.previous = previous;
		this.next = next;
		
		initialize();
	}
	
	public void initialize() {
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
