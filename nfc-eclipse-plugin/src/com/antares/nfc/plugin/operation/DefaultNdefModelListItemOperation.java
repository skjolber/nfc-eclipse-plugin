package com.antares.nfc.plugin.operation;

import org.nfctools.ndef.Record;

import com.antares.nfc.model.NdefRecordModelPropertyListItem;

public class DefaultNdefModelListItemOperation<V, R extends Record> implements NdefModelOperation {

	protected NdefRecordModelPropertyListItem ndefRecordModelPropertyListItem;

	protected V previous;
	
	protected V next;
	
	protected R record;

	public DefaultNdefModelListItemOperation(R record, NdefRecordModelPropertyListItem ndefRecordModelPropertyListItem, V previous, V next) {
		this.record = record;
		this.ndefRecordModelPropertyListItem = ndefRecordModelPropertyListItem;
		this.previous = previous;
		this.next = next;
		
		initialize();
	}

	public void initialize() {
	}
	
	@Override
	public void execute() {
		if(next != null) {
			ndefRecordModelPropertyListItem.setValue(toString(next));
		} else {
			ndefRecordModelPropertyListItem.setValue("");
		}
	}

	@Override
	public void revoke() {
		if(previous != null) {
			ndefRecordModelPropertyListItem.setValue(toString(previous));
		} else {
			ndefRecordModelPropertyListItem.setValue("");
		}
	}
	
	public String toString(V object) {
		return object.toString();
	}
	
}
