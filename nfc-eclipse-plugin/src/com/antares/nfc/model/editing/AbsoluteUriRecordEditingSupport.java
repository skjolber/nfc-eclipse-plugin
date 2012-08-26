package com.antares.nfc.model.editing;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.nfctools.ndef.auri.AbsoluteUriRecord;

import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.model.NdefRecordModelProperty;
import com.antares.nfc.plugin.operation.DefaultNdefModelPropertyOperation;
import com.antares.nfc.plugin.operation.NdefModelOperation;

class AbsoluteUriRecordEditingSupport extends DefaultRecordEditingSupport {

	public AbsoluteUriRecordEditingSupport(
			TreeViewer treeViewer) {
		super(treeViewer);
	}

	@Override
	public NdefModelOperation setValue(NdefRecordModelNode node, Object value) {
		AbsoluteUriRecord absoluteUriRecord = (AbsoluteUriRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {
			String stringValue = (String)value;
			
			if(!stringValue.equals(absoluteUriRecord.getUri())) {
				return new DefaultNdefModelPropertyOperation<String, AbsoluteUriRecord>(absoluteUriRecord, (NdefRecordModelProperty)node,absoluteUriRecord.getUri(), stringValue) {
					
					@Override
					public void execute() {
						super.execute();
						
						record.setUri(next);
					}
					
					@Override
					public void revoke() {
						super.revoke();
						
						record.setUri(previous);
					}
				};					
				
				
			}
		} else {
			return super.setValue(node, value);
		}
		return null;
	}

	@Override
	public Object getValue(NdefRecordModelNode node) {
		AbsoluteUriRecord absoluteUriRecord = (AbsoluteUriRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {
			if(absoluteUriRecord.hasUri()) {
				return absoluteUriRecord.getUri();
			} else {
				return EMPTY_STRING;
			}
		} else {
			return super.getValue(node);
		}
	}

	@Override
	public CellEditor getCellEditor(NdefRecordModelNode node) {
		if(node instanceof NdefRecordModelProperty) {
			return new TextCellEditor(treeViewer.getTree());
		} else {
			return super.getCellEditor(node);
		}
	}
}