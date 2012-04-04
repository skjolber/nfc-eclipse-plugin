package com.antares.nfc.model;

import java.util.List;

import org.nfctools.ndef.Record;

public class NdefRecordModelRecord extends NdefRecordModelParent {
	
	private Record record;
	private int size = -1;

	public NdefRecordModelRecord(Record record, List<NdefRecordModelNode> children, NdefRecordModelParent parent) {
		super(children, parent);
		this.record = record;
	}

	public NdefRecordModelRecord(Record record, NdefRecordModelParent parent) {
		super(parent);
		this.record = record;
	}

	public Record getRecord() {
		return record;
	}

	@Override
	public String toString() {
		return record.getClass().getSimpleName();
	}

	public int getRecordSize() {
		return size;
	}

	public void setRecordSize(int size) {
		this.size = size;
	}

	
	
}
