package com.antares.nfc.model;

import org.nfctools.ndef.Record;

public interface NdefRecordModelChangeListener {

	void update(NdefRecordModelParent node);
	
	void insert(NdefRecordModelParent node, int index, Class<? extends Record> recordType);

	void add(NdefRecordModelParent node, Class<? extends Record> recordType);

	void remove(NdefRecordModelNode node);
}
