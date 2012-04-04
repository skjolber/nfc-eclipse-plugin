package com.antares.nfc.model;

public class NdefRecordModelValue extends NdefRecordModelNode {

	private String value;
	
	public NdefRecordModelValue(String value, NdefRecordModelParent parent) {
		super(parent);
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}
}
