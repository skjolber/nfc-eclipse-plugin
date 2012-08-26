package com.antares.nfc.model.editing;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.nfctools.ndef.wkt.handover.records.HandoverRequestRecord;

import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.model.NdefRecordModelParentProperty;
import com.antares.nfc.model.NdefRecordModelProperty;
import com.antares.nfc.plugin.operation.DefaultNdefModelPropertyOperation;
import com.antares.nfc.plugin.operation.NdefModelOperation;

class HandoverRequestRecordEditingSupport extends DefaultRecordEditingSupport {

	public HandoverRequestRecordEditingSupport(
			TreeViewer treeViewer) {
		super(treeViewer);
	}

	@Override
	public boolean canEdit(NdefRecordModelNode node) {
		if(node instanceof NdefRecordModelParentProperty) {
			return false;
		}
		return super.canEdit(node);
	}
	
	@Override
	public NdefModelOperation setValue(NdefRecordModelNode node, Object value) {
		HandoverRequestRecord handoverRequestRecord = (HandoverRequestRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {
			
			int parentIndex = node.getParentIndex();
			if(parentIndex == 0) {
				String stringValue = (String)value;

				try {
					byte byteValue = Byte.parseByte(stringValue);
					if(byteValue < 0) {
						throw new NumberFormatException();
					}
					if(byteValue != handoverRequestRecord.getMajorVersion()) {
						return new DefaultNdefModelPropertyOperation<Byte, HandoverRequestRecord>(handoverRequestRecord, (NdefRecordModelProperty)node, handoverRequestRecord.getMajorVersion(), byteValue) {
							
							@Override
							public void execute() {
								super.execute();
								
								record.setMajorVersion(next);
							}
							
							@Override
							public void revoke() {
								super.revoke();
								
								record.setMajorVersion(previous);
							}
						};
					}
				} catch(Exception e) {
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					MessageDialog.openError(shell, "Error", "Could not set value '" + stringValue + "', reverting to previous value.");
				}

			} else if(parentIndex == 1) {
				
				String stringValue = (String)value;

				try {
					byte byteValue = Byte.parseByte(stringValue);
					if(byteValue < 0) {
						throw new NumberFormatException();
					}
					if(byteValue != handoverRequestRecord.getMinorVersion()) {
						return new DefaultNdefModelPropertyOperation<Byte, HandoverRequestRecord>(handoverRequestRecord, (NdefRecordModelProperty)node, handoverRequestRecord.getMinorVersion(), byteValue) {
							
							@Override
							public void execute() {
								super.execute();
								
								record.setMinorVersion(next);
							}
							
							@Override
							public void revoke() {
								super.revoke();
								
								record.setMinorVersion(previous);
							}
						};

					}
				} catch(Exception e) {
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					MessageDialog.openError(shell, "Error", "Could not set value '" + stringValue + "', reverting to previous value.");
				}
			}

			return null;
			
		} else {
			return super.setValue(node, value);
		}
	}

	@Override
	public Object getValue(NdefRecordModelNode node) {
		HandoverRequestRecord record = (HandoverRequestRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {
			int parentIndex = node.getParentIndex();
			if(parentIndex == 0) {
				return Byte.toString(record.getMajorVersion());
			} else if(parentIndex == 1) {
				return Byte.toString(record.getMinorVersion());
			} else {
				throw new RuntimeException();
			}
		}
		
		return super.getValue(node);
	}

	@Override
	public CellEditor getCellEditor(NdefRecordModelNode node) {
		if(node instanceof NdefRecordModelProperty) {
			return new TextCellEditor(treeViewer.getTree());
		} else if(node instanceof NdefRecordModelParentProperty) {
			return new ComboBoxCellEditor(treeViewer.getTree(), NdefRecordModelEditingSupport.PRESENT_OR_NOT, ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION);
		} else {
			return super.getCellEditor(node);
		}

	}
}