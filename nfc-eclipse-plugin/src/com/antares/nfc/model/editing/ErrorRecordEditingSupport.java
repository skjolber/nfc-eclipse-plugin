package com.antares.nfc.model.editing;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.nfctools.ndef.wkt.handover.records.ErrorRecord;

import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.model.NdefRecordModelProperty;
import com.antares.nfc.plugin.operation.DefaultNdefModelPropertyOperation;
import com.antares.nfc.plugin.operation.NdefModelOperation;

public class ErrorRecordEditingSupport extends DefaultRecordEditingSupport {

	public ErrorRecordEditingSupport(
			TreeViewer treeViewer) {
		super(treeViewer);
	}

	@Override
	public NdefModelOperation setValue(NdefRecordModelNode node, Object value) {
		ErrorRecord errorRecord = (ErrorRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {
			int parentIndex = node.getParentIndex();
			
			if(parentIndex == 0) {

				Integer index = (Integer)value;

				ErrorRecord.ErrorReason errorReason;
				if(index.intValue() != -1) {
					ErrorRecord.ErrorReason[] values = ErrorRecord.ErrorReason.values();
					errorReason = values[index.intValue()];
				} else {
					errorReason = null;
				}
				if(errorReason !=  errorRecord.getErrorReason()) {

					return new DefaultNdefModelPropertyOperation<ErrorRecord.ErrorReason, ErrorRecord>(errorRecord, (NdefRecordModelProperty)node, errorRecord.getErrorReason(), errorReason) {
						
						@Override
						public void execute() {
							super.execute();
							
							record.setErrorReason(next);
						}
						
						@Override
						public void revoke() {
							super.revoke();
							
							record.setErrorReason(previous);
						}
					};		
					
				}
			} else if(parentIndex == 1) {
				String stringValue = (String)value;
						
				Long longValue;
				if(stringValue != null && stringValue.length() > 0) {

					try {
						if(stringValue.startsWith("0x")) {
							longValue = Long.parseLong(stringValue.substring(2), 16);
						} else {
							longValue = Long.parseLong(stringValue);
						}
						
						if(errorRecord.hasErrorReason()) {
							switch(errorRecord.getErrorReason()) {
							
							case TemporaryMemoryConstraints : {
								if(longValue.longValue() > 255) {
									throw new IllegalArgumentException("Expected value <= 255 (1 byte)");
								}
								break;
							}
							
							case PermanenteMemoryConstraints : {
								if(longValue.longValue() > 4294967295L) {
									throw new IllegalArgumentException("Expected value <= 4294967295 (4 bytes)");
								}

								break;
							}

							case CarrierSpecificConstraints : {
								if(longValue.longValue() > 255) {
									throw new IllegalArgumentException("Expected 8-bit value <= 255 (1 byte)");
								}
								break;
							}
							
							}
						}
						
					} catch(NumberFormatException e) {
						Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
						MessageDialog.openError(shell, "Error", "Could not set value '" + stringValue + "', reverting to previous value.");
						
						return null;
					} catch(IllegalArgumentException e) {
						Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
						MessageDialog.openError(shell, "Error", e.getMessage() + "', reverting to previous value.");
						
						return null;
					}
				
					if(!longValue.equals(errorRecord.getErrorData())) {
						return new DefaultNdefModelPropertyOperation<Number, ErrorRecord>(errorRecord, (NdefRecordModelProperty)node, errorRecord.getErrorData(), longValue) {
							
							@Override
							public void execute() {
								super.execute();
								
								record.setErrorData(next);
							}
							
							@Override
							public void revoke() {
								super.revoke();
								
								record.setErrorData(previous);
							}
						};		
						
					}
				}
			}
		} else {
			return super.setValue(node, value);
		}
		return null;
	}

	@Override
	public Object getValue(NdefRecordModelNode node) {
		ErrorRecord errorRecord = (ErrorRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {
			
			int parentIndex = node.getParentIndex();
			if(parentIndex == 0) {
				if(errorRecord.hasErrorReason()) {
					return errorRecord.getErrorReason().ordinal();
				}
				return -1;
			} else if(parentIndex == 1) {
				if(errorRecord.hasErrorData()) {
					return "0x" + Long.toHexString(errorRecord.getErrorData().longValue());
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
			
			int parentIndex = node.getParentIndex();
			if(parentIndex == 0) {
				return getComboBoxCellEditor(ErrorRecord.ErrorReason.values(), false);
			} else {
				return new TextCellEditor(treeViewer.getTree());
			}
		} else {
			return super.getCellEditor(node);
		}
	}
}