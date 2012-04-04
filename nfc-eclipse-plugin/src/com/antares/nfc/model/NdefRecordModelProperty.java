package com.antares.nfc.model;

public class NdefRecordModelProperty extends NdefRecordModelNode {

	private String name;
	private String value;
	
	public NdefRecordModelProperty(String name, String value, NdefRecordModelParent parent) {
		super(parent);
		this.name = name;
		this.value = value;
	}

	@Override
	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	
}
