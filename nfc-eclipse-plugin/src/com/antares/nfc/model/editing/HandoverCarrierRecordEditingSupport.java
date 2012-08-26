package com.antares.nfc.model.editing;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.nfctools.ndef.Record;
import org.nfctools.ndef.wkt.handover.records.HandoverCarrierRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverCarrierRecord.CarrierTypeFormat;

import com.antares.nfc.model.NdefRecordModelFactory;
import com.antares.nfc.model.NdefRecordModelMenuListener;
import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.model.NdefRecordModelParentProperty;
import com.antares.nfc.model.NdefRecordModelProperty;
import com.antares.nfc.model.NdefRecordType;
import com.antares.nfc.plugin.NdefRecordFactory;
import com.antares.nfc.plugin.operation.DefaultNdefModelPropertyOperation;
import com.antares.nfc.plugin.operation.DefaultNdefRecordModelParentPropertyOperation;
import com.antares.nfc.plugin.operation.NdefModelOperation;

class HandoverCarrierRecordEditingSupport extends DefaultRecordEditingSupport {

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
					
					String path = (String)value;
					
					byte[] payload;
					if(path != null) {
						File file = new File(path);
	
						int length = (int)file.length();
						
						payload = new byte[length];
						
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
					} else {
						payload = null;
					}
					
					return new DefaultNdefModelPropertyOperation<byte[], HandoverCarrierRecord>(record, (NdefRecordModelProperty)node, record.getCarrierData(), payload) {
						
						@Override
						public void execute() {
							super.execute();
							
							record.setCarrierData(next);
							
							if(next == null) {
								ndefRecordModelProperty.setValue("Zero byte data");
							} else {
								ndefRecordModelProperty.setValue(Integer.toString(next.length) + " bytes data");
							}	

						}
						
						@Override
						public void revoke() {
							super.revoke();
							
							record.setCarrierData(previous);
							
							if(previous == null) {
								ndefRecordModelProperty.setValue("Zero byte data");
							} else {
								ndefRecordModelProperty.setValue(Integer.toString(previous.length) + " bytes data");
							}	
						}
					};
					
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