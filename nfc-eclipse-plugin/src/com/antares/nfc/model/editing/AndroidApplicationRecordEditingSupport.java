package com.antares.nfc.model.editing;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.nfctools.ndef.ext.AndroidApplicationRecord;

import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.model.NdefRecordModelProperty;
import com.antares.nfc.plugin.operation.DefaultNdefModelPropertyOperation;
import com.antares.nfc.plugin.operation.NdefModelOperation;

public class AndroidApplicationRecordEditingSupport extends DefaultRecordEditingSupport {

	public AndroidApplicationRecordEditingSupport(
			TreeViewer treeViewer) {
		super(treeViewer);
	}

	@Override
	public NdefModelOperation setValue(NdefRecordModelNode node, Object value) {
		AndroidApplicationRecord androidApplicationRecord = (AndroidApplicationRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {
			String stringValue = (String)value;
			
			if(!stringValue.equals(androidApplicationRecord.getPackageName())) {
				return new DefaultNdefModelPropertyOperation<String, AndroidApplicationRecord>(androidApplicationRecord, (NdefRecordModelProperty)node, androidApplicationRecord.getPackageName(), stringValue) {
					
					@Override
					public void execute() {
						super.execute();
						
						record.setPackageName(next);
					}
					
					@Override
					public void revoke() {
						super.revoke();
						
						record.setPackageName(previous);
					}
				};	

			}
			return null;
		} else {
			return super.setValue(node, value);
		}
	}

	@Override
	public Object getValue(NdefRecordModelNode node) {
		AndroidApplicationRecord androidApplicationRecord = (AndroidApplicationRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {
			if(androidApplicationRecord.hasPackageName()) {
				return androidApplicationRecord.getPackageName();
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