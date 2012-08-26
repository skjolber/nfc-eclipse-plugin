package com.antares.nfc.model.editing;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.nfctools.ndef.wkt.records.UriRecord;

import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.model.NdefRecordModelProperty;
import com.antares.nfc.plugin.operation.DefaultNdefModelPropertyOperation;
import com.antares.nfc.plugin.operation.NdefModelOperation;

class UriRecordEditingSuppport extends DefaultRecordEditingSupport {

	public UriRecordEditingSuppport(
			TreeViewer treeViewer) {
		super(treeViewer);
	}

	@Override
	public NdefModelOperation setValue(NdefRecordModelNode node, Object value) {
		UriRecord uriRecord = (UriRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {
			String stringValue = (String)value;
			
			if(!stringValue.equals(uriRecord.getUri())) {
				return new DefaultNdefModelPropertyOperation<String, UriRecord>(uriRecord, (NdefRecordModelProperty)node, uriRecord.getUri(), stringValue) {
					
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
		UriRecord uriRecord = (UriRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {
			if(uriRecord.hasUri()) {
				return uriRecord.getUri();
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