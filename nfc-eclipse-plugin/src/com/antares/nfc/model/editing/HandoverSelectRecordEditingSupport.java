package com.antares.nfc.model.editing;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.nfctools.ndef.wkt.handover.records.ErrorRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverSelectRecord;

import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.model.NdefRecordModelParentProperty;
import com.antares.nfc.model.NdefRecordModelProperty;
import com.antares.nfc.plugin.NdefRecordFactory;
import com.antares.nfc.plugin.operation.DefaultNdefModelPropertyOperation;
import com.antares.nfc.plugin.operation.DefaultNdefRecordModelParentPropertyOperation;
import com.antares.nfc.plugin.operation.NdefModelOperation;

class HandoverSelectRecordEditingSupport extends DefaultRecordEditingSupport {

	private NdefRecordFactory ndefRecordFactory;

	public HandoverSelectRecordEditingSupport(TreeViewer treeViewer, NdefRecordFactory ndefRecordFactory) {
		super(treeViewer);
		
		this.ndefRecordFactory = ndefRecordFactory;
	}
	
	@Override
	public boolean canEdit(NdefRecordModelNode node) {
		if(node instanceof NdefRecordModelProperty) { 
			return true;
		} else if(node instanceof NdefRecordModelParentProperty) {
			NdefRecordModelParentProperty ndefRecordModelParentProperty = (NdefRecordModelParentProperty)node;
			
			int recordIndex = ndefRecordModelParentProperty.getRecordBranchIndex();
			
			if(recordIndex == 3) {
				return true;
			}
			return false;
		}
				
		return super.canEdit(node);
	}
	
	@Override
	public NdefModelOperation setValue(NdefRecordModelNode node, Object value) {
		HandoverSelectRecord record = (HandoverSelectRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {
			
			int parentIndex = node.getParentIndex();
			if(parentIndex == 0) {
				String stringValue = (String)value;

				try {
					byte byteValue = Byte.parseByte(stringValue);
					if(byteValue < 0) {
						throw new NumberFormatException();
					}
					if(byteValue != record.getMajorVersion()) {
						
						return new DefaultNdefModelPropertyOperation<Byte, HandoverSelectRecord>(record, (NdefRecordModelProperty)node, record.getMajorVersion(), byteValue) {
							
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
					if(byteValue != record.getMinorVersion()) {
						return new DefaultNdefModelPropertyOperation<Byte, HandoverSelectRecord>(record, (NdefRecordModelProperty)node, record.getMinorVersion(), byteValue) {
							
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
		} else if(node instanceof NdefRecordModelParentProperty) {
			Integer index = (Integer)value;
			
			if(index.intValue() != -1) {
				HandoverSelectRecord handoverSelectRecord = (HandoverSelectRecord)record;
				
				int previousIndex;
				if(handoverSelectRecord.hasError()) {
					previousIndex = 0;
				} else {
					previousIndex = 1;
				}
				
				if(index.intValue() != previousIndex) {
					ErrorRecord errorRecord;
					if(index.intValue() == 1) {
						errorRecord = null;
					} else {
						errorRecord = ndefRecordFactory.createRecord(ErrorRecord.class);
					}
					
					return new DefaultNdefRecordModelParentPropertyOperation<ErrorRecord, HandoverSelectRecord>(record, (NdefRecordModelParentProperty)node, record.getError(), errorRecord);
				}
			}			
		} else {
			return super.setValue(node, value);
		}
		return null;

	}

	@Override
	public Object getValue(NdefRecordModelNode node) {
		HandoverSelectRecord record = (HandoverSelectRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {
			int parentIndex = node.getParentIndex();
			if(parentIndex == 0) {
				return Byte.toString(record.getMajorVersion());
			} else if(parentIndex == 1) {
				return Byte.toString(record.getMinorVersion());
			} else {
				throw new RuntimeException();
			}
		} else if(node instanceof NdefRecordModelParentProperty) {
			if(record.hasError()) {
				return new Integer(0);
			}
			return new Integer(1);
		} else {
			return super.getValue(node);
		}
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