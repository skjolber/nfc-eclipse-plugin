package com.antares.nfc.plugin.operation;

import org.nfctools.ndef.Record;

import com.antares.nfc.model.NdefRecordModelProperty;

public class DefaultNdefModelPropertyOperation<V, R extends Record> implements NdefModelOperation {

	protected NdefRecordModelProperty ndefRecordModelProperty;

	protected V previous;
	
	protected V next;
	
	protected R record;

	public DefaultNdefModelPropertyOperation(R record, NdefRecordModelProperty ndefRecordModelProperty, V previous, V next) {
		this.record = record;
		this.ndefRecordModelProperty = ndefRecordModelProperty;
		this.previous = previous;
		this.next = next;
		
		initialize();
	}

	public void initialize() {
	}
	
	@Override
	public void execute() {
		if(next != null) {
			ndefRecordModelProperty.setValue(next.toString());
		} else {
			ndefRecordModelProperty.setValue("");
		}
	}

	@Override
	public void revoke() {
		if(previous != null) {
			ndefRecordModelProperty.setValue(previous.toString());
		} else {
			ndefRecordModelProperty.setValue("");
		}
	}
	
	
	
}
