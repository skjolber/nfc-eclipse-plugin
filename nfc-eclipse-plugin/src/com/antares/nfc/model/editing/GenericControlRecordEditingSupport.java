package com.antares.nfc.model.editing;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.nfctools.ndef.wkt.records.GenericControlRecord;

import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.model.NdefRecordModelProperty;
import com.antares.nfc.plugin.operation.DefaultNdefModelPropertyOperation;
import com.antares.nfc.plugin.operation.NdefModelOperation;

class GenericControlRecordEditingSupport extends DefaultRecordEditingSupport {

	public GenericControlRecordEditingSupport(
			TreeViewer treeViewer) {
		super(treeViewer);
	}

	@Override
	public NdefModelOperation setValue(NdefRecordModelNode node, Object value) {
		GenericControlRecord genericControlRecord = (GenericControlRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {
			String stringValue = (String)value;

			try {
				byte b;
				if(stringValue.startsWith("0x")) {
					b = Byte.parseByte(stringValue.substring(2), 16);
				} else {
					b = Byte.parseByte(stringValue);
				}
				
				if(b != genericControlRecord.getConfigurationByte()) {
					return new DefaultNdefModelPropertyOperation<Byte, GenericControlRecord>(genericControlRecord, (NdefRecordModelProperty)node, genericControlRecord.getConfigurationByte(), b) {
						
						@Override
						public void execute() {
							super.execute();
							
							record.setConfigurationByte(next);
						}
						
						@Override
						public void revoke() {
							super.revoke();
							
							record.setConfigurationByte(previous);
						}
					};
				}
			} catch(Exception e) {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				MessageDialog.openError(shell, "Error", "Could not set value '" + stringValue + "', reverting to previous value.");
			}
		} else {
			return super.setValue(node, value);
		}
		return null;
	}

	@Override
	public Object getValue(NdefRecordModelNode node) {
		GenericControlRecord genericControlRecord = (GenericControlRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {
			return Byte.toString(genericControlRecord.getConfigurationByte());
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