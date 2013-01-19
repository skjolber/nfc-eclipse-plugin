/***************************************************************************
 *
 * This file is part of the NFC Eclipse Plugin project at
 * http://code.google.com/p/nfc-eclipse-plugin/
 *
 * Copyright (C) 2012 by Thomas Rorvik Skjolberg.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ****************************************************************************/

package org.nfc.eclipse.plugin.model.editing;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.nfc.eclipse.plugin.NdefRecordFactory;
import org.nfc.eclipse.plugin.model.NdefRecordModelBinaryProperty;
import org.nfc.eclipse.plugin.model.NdefRecordModelFactory;
import org.nfc.eclipse.plugin.model.NdefRecordModelMenuListener;
import org.nfc.eclipse.plugin.model.NdefRecordModelNode;
import org.nfc.eclipse.plugin.model.NdefRecordModelParentProperty;
import org.nfc.eclipse.plugin.model.NdefRecordModelProperty;
import org.nfc.eclipse.plugin.model.NdefRecordType;
import org.nfc.eclipse.plugin.operation.DefaultNdefModelPropertyOperation;
import org.nfc.eclipse.plugin.operation.DefaultNdefRecordModelParentPropertyOperation;
import org.nfc.eclipse.plugin.operation.NdefModelOperation;
import org.nfctools.ndef.Record;
import org.nfctools.ndef.wkt.handover.records.HandoverCarrierRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverCarrierRecord.CarrierTypeFormat;


public class HandoverCarrierRecordEditingSupport extends DefaultRecordEditingSupport {

	public static NdefModelOperation newSetCarrierDataOperation(HandoverCarrierRecord record, NdefRecordModelProperty node, byte[] next) {
		return new SetCarrierDataOperation(record, (NdefRecordModelProperty)node, record.getCarrierData(), next);
	}

	private static class SetCarrierDataOperation extends DefaultNdefModelPropertyOperation<byte[], HandoverCarrierRecord> {

		public SetCarrierDataOperation(HandoverCarrierRecord record, NdefRecordModelProperty ndefRecordModelProperty, byte[] previous, byte[] next) {
			super(record, ndefRecordModelProperty, previous, next);
		}

		@Override
		public void execute() {
			super.execute();
			
			record.setCarrierData(next);
			
			if(next == null) {
				ndefRecordModelProperty.setValue(NdefRecordModelFactory.getNoBytesString());
			} else {
				ndefRecordModelProperty.setValue(NdefRecordModelFactory.getBytesString(next.length));
			}	

		}
		
		@Override
		public void revoke() {
			super.revoke();
			
			record.setCarrierData(previous);
			
			if(previous == null) {
				ndefRecordModelProperty.setValue(NdefRecordModelFactory.getNoBytesString());
			} else {
				ndefRecordModelProperty.setValue(NdefRecordModelFactory.getBytesString(previous.length));
			}	
		}
	}

	
	private NdefRecordFactory ndefRecordFactory;

	public HandoverCarrierRecordEditingSupport(TreeViewer treeViewer, NdefRecordFactory ndefRecordFactory) {
		super(treeViewer);
		
		this.ndefRecordFactory = ndefRecordFactory;
	}

	@Override
	public boolean canEdit(NdefRecordModelNode node) {
		if(node instanceof NdefRecordModelParentProperty) {
			NdefRecordModelParentProperty ndefRecordModelParentProperty = (NdefRecordModelParentProperty)node;
			
			int recordIndex = ndefRecordModelParentProperty.getRecordBranchIndex();
			if(recordIndex == 1) {
				HandoverCarrierRecord record = (HandoverCarrierRecord) node.getRecord();
				if(record.hasCarrierTypeFormat()) {
					
					CarrierTypeFormat carrierTypeFormat = record.getCarrierTypeFormat();
					if(carrierTypeFormat == CarrierTypeFormat.External || carrierTypeFormat == CarrierTypeFormat.WellKnown) {
						return true;
					}
				}
				
				return false;
			}
		}
		
		return super.canEdit(node);
	}
	
	@Override
	public NdefModelOperation setValue(NdefRecordModelNode node, Object value) {
		HandoverCarrierRecord record = (HandoverCarrierRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {
			
			HandoverCarrierRecord handoverCarrierRecord = (HandoverCarrierRecord)record;
			
			int recordLevel = node.getRecordLevel();
			if(recordLevel == 1) {
				int parentIndex = node.getRecordBranchIndex();
				if(parentIndex == 0) {
					
					Integer index = (Integer)value;
	
					HandoverCarrierRecord.CarrierTypeFormat carrierTypeFormat;
					if(index.intValue() != -1) {
						HandoverCarrierRecord.CarrierTypeFormat[] values = HandoverCarrierRecord.CarrierTypeFormat.values();
						carrierTypeFormat = values[index.intValue()];
					} else {
						carrierTypeFormat = null;
					}
					
					if(carrierTypeFormat != handoverCarrierRecord.getCarrierTypeFormat()) {
						
						return new DefaultNdefModelPropertyOperation<HandoverCarrierRecord.CarrierTypeFormat, HandoverCarrierRecord>(record, (NdefRecordModelProperty)node, handoverCarrierRecord.getCarrierTypeFormat(), carrierTypeFormat) {
							
							private Object carrierType;
							private NdefRecordModelNode carrierTypeNode;
							private NdefRecordModelParentProperty ndefRecordModelParentProperty;
							
							@Override
							public void initialize() {
								ndefRecordModelParentProperty = (NdefRecordModelParentProperty)ndefRecordModelProperty.getParent().getChild(1);

								if(record.hasCarrierType()) {
									carrierType = record.getCarrierType();
									if(ndefRecordModelParentProperty.hasChildren()) {
										carrierTypeNode = ndefRecordModelParentProperty.getChild(0);
									}
								}
							}
							
							@Override
							public void execute() {
								super.execute();
								
								// clean up
								if(previous != null) {
									if(carrierType instanceof Record) {
										NdefRecordFactory.disconnect(record, (Record)carrierType);
									}
									if(ndefRecordModelParentProperty.hasChildren()) {
										ndefRecordModelParentProperty.remove(0);
									}
								}
								
								// set next value
								record.setCarrierTypeFormat(next);
								if(next == CarrierTypeFormat.AbsoluteURI || next == CarrierTypeFormat.Media) {
									// Absolute URI as defined in RFC 3986 [RFC 3986]
									// Media-type as defined in RFC 2046 [RFC 2046]
									
									record.setCarrierType("");
									ndefRecordModelParentProperty.add(NdefRecordModelFactory.getNode(next.name(), "", ndefRecordModelParentProperty));
								} else {
									// NFC Forum well-known type [NFC RTD]
									// NFC Forum external type [NFC RTD]
									record.setCarrierType(null);
								}
							}
							
							@Override
							public void revoke() {
								super.revoke();
								
								record.setCarrierTypeFormat(previous);
								if(next == CarrierTypeFormat.AbsoluteURI || next == CarrierTypeFormat.Media) {
									ndefRecordModelParentProperty.remove(0);
								}
								record.setCarrierType(carrierType);
								if(carrierType instanceof Record) {
									NdefRecordFactory.connect(record, (Record)carrierType);
								}
								if(carrierTypeNode != null) {
									ndefRecordModelParentProperty.add(carrierTypeNode);
								}
							}
						};
					}
				} else if(parentIndex == 2) {
					
					byte[] payload = load((String)value);
					
					if(payload != null) {
						NdefRecordModelBinaryProperty ndefRecordModelBinaryProperty = (NdefRecordModelBinaryProperty)node;
						ndefRecordModelBinaryProperty.setFile((String)value);
						
						return newSetCarrierDataOperation(record, ndefRecordModelBinaryProperty, payload);
					}
					
				} else {
					throw new RuntimeException();
				}
				
			} else if(recordLevel == 2) {
				String stringValue = (String)value;
				
				if(!stringValue.equals(handoverCarrierRecord.getCarrierType())) {
					handoverCarrierRecord.setCarrierType(stringValue);
						
					NdefRecordModelProperty ndefRecordModelProperty = (NdefRecordModelProperty)node;
					ndefRecordModelProperty.setValue(handoverCarrierRecord.getCarrierType().toString());
					
					return new DefaultNdefModelPropertyOperation<Object, HandoverCarrierRecord>(record, (NdefRecordModelProperty)node, record.getCarrierType(), stringValue) {
						
						@Override
						public void execute() {
							super.execute();
							
							record.setCarrierType(next);
						}
						
						@Override
						public void revoke() {
							super.revoke();
							
							record.setCarrierType(previous);
						}
					};

					
				}
			} else {
				throw new RuntimeException();
			}
			return null;
		} else if(node instanceof NdefRecordModelParentProperty) {
			Integer index = (Integer)value;

			if(index.intValue() != -1) {

				if(record.hasCarrierTypeFormat()) {
					HandoverCarrierRecord.CarrierTypeFormat carrierTypeFormat = record.getCarrierTypeFormat();
				
					NdefRecordType[] types;
					switch(carrierTypeFormat) {
						case WellKnown : {
							// NFC Forum well-known type [NFC RTD]
							types = NdefRecordModelMenuListener.wellKnownRecordTypes;
							break;
						}
						
						case External : {
							// NFC Forum external type [NFC RTD]
							types = NdefRecordModelMenuListener.externalRecordTypes;
							
							break;
						}
						
						default : {
							throw new RuntimeException();
						}
					}
					
					int previousIndex = -1;
					if(record.hasCarrierType()) {
						Class<?> c = record.getCarrierType().getClass();
						
						for(int i = 0; i < types.length; i++) {
							if(c ==  types[i].getRecordClass()) {
								previousIndex = i;
								
								break;
							}
						}

					}
					
					if(index.intValue() != previousIndex) {
						return new DefaultNdefRecordModelParentPropertyOperation<Record, HandoverCarrierRecord>(record, (NdefRecordModelParentProperty)node, (Record)record.getCarrierType(),  ndefRecordFactory.createRecord(types[index].getRecordClass()));
					}
				}
			}
			return null;
		} else {
			return super.setValue(node, value);
		}
	}

	@Override
	public Object getValue(NdefRecordModelNode node) {
		HandoverCarrierRecord record = (HandoverCarrierRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {
			
			int recordLevel = node.getRecordLevel();
			if(recordLevel == 1) {
				int parentIndex = node.getParentIndex();
				if(parentIndex == 0) {
					if(record.hasCarrierTypeFormat()) {
						return record.getCarrierTypeFormat().ordinal();
					}
					return -1;
				} else if(parentIndex == 2) {
					return EMPTY_STRING;
				}
			} else if(recordLevel == 2) {
				CarrierTypeFormat carrierTypeFormat = record.getCarrierTypeFormat();
				if(carrierTypeFormat != null) {
					
					Object carrierType = record.getCarrierType();
					if(carrierType != null) {
						switch(carrierTypeFormat) {
							case AbsoluteURI : {
								return record.getCarrierType().toString();
							}
						
							case Media : {
								return record.getCarrierType().toString();
							}
						default:
							break;
						}
					}
				}
			}					
			throw new RuntimeException();
			
		} else if(node instanceof NdefRecordModelParentProperty) {
			CarrierTypeFormat carrierTypeFormat = record.getCarrierTypeFormat();
			if(carrierTypeFormat != null) {
				
				Object carrierType = record.getCarrierType();
				if(carrierType != null) {
					switch(carrierTypeFormat) {
						case WellKnown : {
							// NFC Forum well-known type [NFC RTD]

							return getIndex(NdefRecordModelMenuListener.wellKnownRecordTypes, carrierType.getClass());
						}
					
						case External : {
							// NFC Forum external type [NFC RTD]
							
							return getIndex(NdefRecordModelMenuListener.externalRecordTypes, carrierType.getClass());
						}
					default:
						break;
					}
				}
					
			}
			
			return -1;
		} else {
			return super.getValue(node);
		}
	}

	@Override
	public CellEditor getCellEditor(NdefRecordModelNode node) {
		if(node instanceof NdefRecordModelProperty) {
			int recordLevel = node.getRecordLevel();
			if(recordLevel == 1) {
				int parentIndex = node.getParentIndex();
				if(parentIndex == 0) {
					return getComboBoxCellEditor(HandoverCarrierRecord.CarrierTypeFormat.values(), false);
				} else if(parentIndex == 2) {
					return new FileDialogCellEditor(treeViewer.getTree());
				} else {
					throw new RuntimeException();
				}
			} else if(recordLevel == 2) {
				return new TextCellEditor(treeViewer.getTree());
			} else {
				throw new RuntimeException();
			}
		} else if(node instanceof NdefRecordModelParentProperty) {
			HandoverCarrierRecord record = (HandoverCarrierRecord) node.getRecord();
			
			CarrierTypeFormat carrierTypeFormat = record.getCarrierTypeFormat();
			if(carrierTypeFormat != null) {
				switch(carrierTypeFormat) {
					case WellKnown : {
						// NFC Forum well-known type [NFC RTD]
						return getComboBoxCellEditor(NdefRecordModelMenuListener.wellKnownRecordTypes, false);
					}
				
					case External : {
						// NFC Forum external type [NFC RTD]
						return getComboBoxCellEditor(NdefRecordModelMenuListener.externalRecordTypes, false);
					}
				}
			}
			throw new RuntimeException();
		} else {
			return super.getCellEditor(node);
		}

	}
}