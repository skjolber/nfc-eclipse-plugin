package com.antares.nfc.model.editing;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.nfctools.ndef.wkt.handover.records.AlternativeCarrierRecord;
import org.nfctools.ndef.wkt.handover.records.AlternativeCarrierRecord.CarrierPowerState;

import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.model.NdefRecordModelParentProperty;
import com.antares.nfc.model.NdefRecordModelProperty;
import com.antares.nfc.model.NdefRecordModelPropertyListItem;
import com.antares.nfc.plugin.operation.DefaultNdefModelListItemOperation;
import com.antares.nfc.plugin.operation.DefaultNdefModelPropertyOperation;
import com.antares.nfc.plugin.operation.NdefModelOperation;

class AlternativeCarrierRecordSelectEditingSupport extends DefaultRecordEditingSupport {
	
	public AlternativeCarrierRecordSelectEditingSupport(
			TreeViewer treeViewer) {
		super(treeViewer);
	}

	@Override
	public NdefModelOperation setValue(NdefRecordModelNode node, Object value) {
		AlternativeCarrierRecord record = (AlternativeCarrierRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {
			
			int parentIndex = node.getParentIndex();
			if(parentIndex == 0) {
						
				Integer index = (Integer)value;

				AlternativeCarrierRecord.CarrierPowerState carrierPowerState;
				if(index.intValue() != -1) {
					AlternativeCarrierRecord.CarrierPowerState[] values = AlternativeCarrierRecord.CarrierPowerState.values();
					carrierPowerState = values[index.intValue()];
				} else {
					carrierPowerState = null;
				}
				
				if(carrierPowerState !=  record.getCarrierPowerState()) {
					return new DefaultNdefModelPropertyOperation<AlternativeCarrierRecord.CarrierPowerState, AlternativeCarrierRecord>(record, (NdefRecordModelProperty)node, record.getCarrierPowerState(), carrierPowerState) {
						
						@Override
						public void execute() {
							super.execute();
							
							record.setCarrierPowerState(next);
						}
						
						@Override
						public void revoke() {
							super.revoke();
							
							record.setCarrierPowerState(previous);
						}
					};
				}
			
			} else if(parentIndex == 1) {
				String stringValue = (String)value;
				
				String carrierDataReference = record.getCarrierDataReference();
				
				if(!stringValue.equals(carrierDataReference)) {

					return new DefaultNdefModelPropertyOperation<String, AlternativeCarrierRecord>(record, (NdefRecordModelProperty)node, record.getCarrierDataReference(), stringValue) {
						
						@Override
						public void execute() {
							super.execute();
							
							record.setCarrierDataReference(next);
						}
						
						@Override
						public void revoke() {
							super.revoke();
							
							record.setCarrierDataReference(previous);
						}
					};
					
				}
			}
			return null;
			
		} else if(node instanceof NdefRecordModelPropertyListItem) {
			NdefRecordModelPropertyListItem ndefRecordModelPropertyListItem = (NdefRecordModelPropertyListItem)node;
			
			AlternativeCarrierRecord alternativeCarrierRecord = (AlternativeCarrierRecord)record;
			
			String stringValue = (String)value;
			int index = ndefRecordModelPropertyListItem.getParentIndex();

			String auxiliaryDataReference = alternativeCarrierRecord.getAuxiliaryDataReferenceAt(index);
			
			if(!stringValue.equals(auxiliaryDataReference)) {

				return new DefaultNdefModelListItemOperation<String, AlternativeCarrierRecord>(record, ndefRecordModelPropertyListItem, auxiliaryDataReference, stringValue) {
					
					@Override
					public void execute() {
						super.execute();
						
						int index = ndefRecordModelPropertyListItem.getParentIndex();

						record.setAuxiliaryDataReference(index, next);
					}
					
					@Override
					public void revoke() {
						super.revoke();
						
						int index = ndefRecordModelPropertyListItem.getParentIndex();

						record.setAuxiliaryDataReference(index, previous);
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
		AlternativeCarrierRecord record = (AlternativeCarrierRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {
			int parentIndex = node.getParentIndex();
			if(parentIndex == 0) {
				
				if(record.hasCarrierPowerState()) {
					return record.getCarrierPowerState().ordinal();
				}

				return -1;
			} else if(parentIndex == 1) {
				if(record.hasCarrierDataReference()) {
					return record.getCarrierDataReference();
				}
				return EMPTY_STRING;
			} else {
				throw new RuntimeException();
			}
		} else if(node instanceof NdefRecordModelPropertyListItem) {
			return record.getAuxiliaryDataReferenceAt(node.getParentIndex());
		} else {
			return super.getValue(node);
		}
	}

	@Override
	public CellEditor getCellEditor(NdefRecordModelNode node) {
		if(node instanceof NdefRecordModelProperty) {
			int parentIndex = node.getParentIndex();
			if(parentIndex == 0) {
				return getComboBoxCellEditor(AlternativeCarrierRecord.CarrierPowerState.values(), false);
			} else if(parentIndex == 1) {
				return new TextCellEditor(treeViewer.getTree());
			} else if(parentIndex == 2) {
				return null;
			} else {
				throw new RuntimeException();
			}
		} else if(node instanceof NdefRecordModelParentProperty) {
			return new ComboBoxCellEditor(treeViewer.getTree(), NdefRecordModelEditingSupport.PRESENT_OR_NOT, ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION);
		} else if(node instanceof NdefRecordModelPropertyListItem) {
			return new TextCellEditor(treeViewer.getTree());
		} else {
			return super.getCellEditor(node);
		}

	}
}