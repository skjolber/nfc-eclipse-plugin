package com.antares.nfc.model.editing;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.nfctools.ndef.wkt.records.Action;
import org.nfctools.ndef.wkt.records.ActionRecord;

import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.model.NdefRecordModelProperty;
import com.antares.nfc.plugin.operation.DefaultNdefModelPropertyOperation;
import com.antares.nfc.plugin.operation.NdefModelOperation;

class ActionRecordEditingSupport extends DefaultRecordEditingSupport {

	public ActionRecordEditingSupport(TreeViewer treeViewer) {
		super(treeViewer);
	}

	@Override
	public NdefModelOperation setValue(NdefRecordModelNode node, Object value) {
		ActionRecord record = (ActionRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {
			Integer index = (Integer)value;
			
			Action action;
			if(index.intValue() > 0) {
				Action[] values = Action.values();
			
				action = values[index.intValue() - 1];
			} else {
				action = null;
			}
			if(action != record.getAction()) {
				
				return new DefaultNdefModelPropertyOperation<Action, ActionRecord>(record, (NdefRecordModelProperty)node, record.getAction(), action) {
					
					@Override
					public void execute() {
						super.execute();
						
						record.setAction(next);
					}
					
					@Override
					public void revoke() {
						super.revoke();
						
						record.setAction(previous);
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
		ActionRecord record = (ActionRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {
			if(record.hasAction()) {
				return record.getAction().ordinal() + 1;
			}
			return 0;
		} else {
			return super.getValue(node);
		}
	}

	@Override
	public CellEditor getCellEditor(NdefRecordModelNode node) {
		if(node instanceof NdefRecordModelProperty) {
			return getComboBoxCellEditor(Action.values(), true);
		} else {
			return super.getCellEditor(node);
		}

	}
}