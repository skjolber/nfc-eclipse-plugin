package com.antares.nfc.model.editing;

import org.eclipse.jface.viewers.CellEditor;

import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.plugin.operation.NdefModelOperation;

interface RecordEditingSupport {
	
	boolean canEdit(NdefRecordModelNode node);
	
	NdefModelOperation setValue(NdefRecordModelNode node, Object value);

	Object getValue(NdefRecordModelNode node);
	
	CellEditor getCellEditor(NdefRecordModelNode node);
}