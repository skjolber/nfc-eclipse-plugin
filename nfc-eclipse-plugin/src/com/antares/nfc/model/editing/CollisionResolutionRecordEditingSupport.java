package com.antares.nfc.model.editing;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.nfctools.ndef.wkt.handover.records.CollisionResolutionRecord;

import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.model.NdefRecordModelProperty;
import com.antares.nfc.plugin.operation.DefaultNdefModelPropertyOperation;
import com.antares.nfc.plugin.operation.NdefModelOperation;

class CollisionResolutionRecordEditingSupport extends DefaultRecordEditingSupport {

	public CollisionResolutionRecordEditingSupport(
			TreeViewer treeViewer) {
		super(treeViewer);
	}

	@Override
	public NdefModelOperation setValue(NdefRecordModelNode node, Object value) {
		CollisionResolutionRecord collisionResolutionRecord = (CollisionResolutionRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {
			String stringValue = (String)value;
			
			try {
				int intValue = Integer.parseInt(stringValue) & 0xFFF;
			
				if(collisionResolutionRecord.getRandomNumber() != intValue) {
					collisionResolutionRecord.setRandomNumber(intValue);
					
					NdefRecordModelProperty ndefRecordModelProperty = (NdefRecordModelProperty)node;
					ndefRecordModelProperty.setValue(Integer.toString(collisionResolutionRecord.getRandomNumber()));
					
					return new DefaultNdefModelPropertyOperation<Integer, CollisionResolutionRecord>(collisionResolutionRecord, (NdefRecordModelProperty)node, collisionResolutionRecord.getRandomNumber(), intValue) {
						
						@Override
						public void execute() {
							super.execute();
							
							record.setRandomNumber(next);
						}
						
						@Override
						public void revoke() {
							super.revoke();
							
							record.setRandomNumber(previous);
						}
					};	
				}
			} catch(NumberFormatException e) {
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
		CollisionResolutionRecord collisionResolutionRecord = (CollisionResolutionRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {
			return Integer.toString(collisionResolutionRecord.getRandomNumber());
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