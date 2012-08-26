package com.antares.nfc.model.editing;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.nfctools.ndef.ext.UnsupportedExternalTypeRecord;

import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.model.NdefRecordModelProperty;
import com.antares.nfc.plugin.operation.DefaultNdefModelPropertyOperation;
import com.antares.nfc.plugin.operation.NdefModelOperation;

public class ExternalTypeRecordEditingSupport extends DefaultRecordEditingSupport {

	public ExternalTypeRecordEditingSupport(
			TreeViewer treeViewer) {
		super(treeViewer);
	}

	@Override
	public NdefModelOperation setValue(NdefRecordModelNode node, Object value) {
		UnsupportedExternalTypeRecord unsupportedExternalTypeRecord = (UnsupportedExternalTypeRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {
			String stringValue = (String)value;
			
			int parentIndex = node.getParentIndex();
			if(parentIndex == 0) {
				if(!stringValue.equals(unsupportedExternalTypeRecord.getNamespace())) {
					return new DefaultNdefModelPropertyOperation<String, UnsupportedExternalTypeRecord>(unsupportedExternalTypeRecord, (NdefRecordModelProperty)node, unsupportedExternalTypeRecord.getNamespace(), stringValue) {
						
						@Override
						public void execute() {
							super.execute();
							
							record.setNamespace(next);
						}
						
						@Override
						public void revoke() {
							super.revoke();
							
							record.setNamespace(previous);
						}
					};
					
				}
			} else if(parentIndex == 1) {
				if(!stringValue.equals(unsupportedExternalTypeRecord.getContent())) {
					return new DefaultNdefModelPropertyOperation<String, UnsupportedExternalTypeRecord>(unsupportedExternalTypeRecord, (NdefRecordModelProperty)node, unsupportedExternalTypeRecord.getContent(), stringValue) {
						
						@Override
						public void execute() {
							super.execute();
							
							record.setContent(next);
						}
						
						@Override
						public void revoke() {
							super.revoke();
							
							record.setContent(previous);
						}
					};
				}
			}
			
			return null;
		} else {
			return super.setValue(node, value);
		}
	}

	@Override
	public Object getValue(NdefRecordModelNode node) {
		UnsupportedExternalTypeRecord unsupportedExternalTypeRecord = (UnsupportedExternalTypeRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {
			
			int parentIndex = node.getParentIndex();
			if(parentIndex == 0) {
				if(unsupportedExternalTypeRecord.hasNamespace()) {
					return unsupportedExternalTypeRecord.getNamespace();
				} else {
					return EMPTY_STRING;
				}
			} else if(parentIndex == 1) {
				if(unsupportedExternalTypeRecord.hasContent()) {
					return unsupportedExternalTypeRecord.getContent();
				} else {
					return EMPTY_STRING;
				}
			} else {
				throw new RuntimeException();
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