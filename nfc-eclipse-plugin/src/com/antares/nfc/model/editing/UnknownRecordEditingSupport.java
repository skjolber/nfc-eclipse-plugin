package com.antares.nfc.model.editing;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.nfctools.ndef.unknown.UnknownRecord;

import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.model.NdefRecordModelProperty;
import com.antares.nfc.plugin.operation.DefaultNdefModelPropertyOperation;
import com.antares.nfc.plugin.operation.NdefModelOperation;

class UnknownRecordEditingSupport extends DefaultRecordEditingSupport {

	public UnknownRecordEditingSupport(
			TreeViewer treeViewer) {
		super(treeViewer);
	}

	@Override
	public NdefModelOperation setValue(NdefRecordModelNode node, Object value) {
		UnknownRecord unknownRecord = (UnknownRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {

			if(value != null) {
			
				String path = (String)value;
				
				File file = new File(path);

				int length = (int)file.length();
				
				byte[] payload = new byte[length];
				
				InputStream in = null;
				try {
					in = new FileInputStream(file);
					DataInputStream din = new DataInputStream(in);
					
					din.readFully(payload);
				} catch(IOException e) {
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					MessageDialog.openError(shell, "Error", "Could not read file '" + file + "', reverting to previous value.");
					
					return null;
				} finally {
					if(in != null) {
						try {
							in.close();
						} catch(IOException e) {
							// ignore
						}
					}
				}
				
				return new DefaultNdefModelPropertyOperation<byte[], UnknownRecord>(unknownRecord, (NdefRecordModelProperty)node, unknownRecord.getPayload(), payload) {
					
					@Override
					public void execute() {
						super.execute();
						
						record.setPayload(next);
						
						if(next == null) {
							ndefRecordModelProperty.setValue("Zero byte data");
						} else {
							ndefRecordModelProperty.setValue(Integer.toString(next.length) + " bytes binary payload");
						}	

					}
					
					@Override
					public void revoke() {
						super.revoke();
						
						record.setPayload(previous);
						
						if(previous == null) {
							ndefRecordModelProperty.setValue("Zero byte data");
						} else {
							ndefRecordModelProperty.setValue(Integer.toString(previous.length) + " bytes binary payload");
						}	
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
		if(node instanceof NdefRecordModelProperty) {
			return EMPTY_STRING;
		} else {
			return super.getValue(node);
		}
	}

	@Override
	public CellEditor getCellEditor(NdefRecordModelNode node) {
		if(node instanceof NdefRecordModelProperty) {
			return new FileDialogCellEditor(treeViewer.getTree());
		} else {
			return super.getCellEditor(node);
		}
	}
}