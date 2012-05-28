/***************************************************************************
 *
 * This file is part of the NFC Eclipse Plugin project at
 * http://code.google.com/p/nfc-eclipse-plugin/
 *
 * Copyright (C) 2012 by Thomas Rørvik Skjølberg / Antares Gruppen AS.
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

package com.antares.nfc.model;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.nfctools.ndef.Record;
import org.nfctools.ndef.auri.AbsoluteUriRecord;
import org.nfctools.ndef.empty.EmptyRecord;
import org.nfctools.ndef.ext.AndroidApplicationRecord;
import org.nfctools.ndef.ext.UnsupportedExternalTypeRecord;
import org.nfctools.ndef.mime.BinaryMimeRecord;
import org.nfctools.ndef.mime.MimeRecord;
import org.nfctools.ndef.unknown.UnknownRecord;
import org.nfctools.ndef.wkt.handover.records.AlternativeCarrierRecord;
import org.nfctools.ndef.wkt.handover.records.CollisionResolutionRecord;
import org.nfctools.ndef.wkt.handover.records.ErrorRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverCarrierRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverCarrierRecord.CarrierTypeFormat;
import org.nfctools.ndef.wkt.handover.records.HandoverRequestRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverSelectRecord;
import org.nfctools.ndef.wkt.records.Action;
import org.nfctools.ndef.wkt.records.ActionRecord;
import org.nfctools.ndef.wkt.records.GcActionRecord;
import org.nfctools.ndef.wkt.records.GcDataRecord;
import org.nfctools.ndef.wkt.records.GcTargetRecord;
import org.nfctools.ndef.wkt.records.GenericControlRecord;
import org.nfctools.ndef.wkt.records.SmartPosterRecord;
import org.nfctools.ndef.wkt.records.TextRecord;
import org.nfctools.ndef.wkt.records.UriRecord;

import com.antares.nfc.plugin.Activator;
import com.antares.nfc.plugin.NdefRecordFactory;
import com.antares.nfc.plugin.operation.DefaultNdefModelListItemOperation;
import com.antares.nfc.plugin.operation.DefaultNdefModelPropertyOperation;
import com.antares.nfc.plugin.operation.DefaultNdefRecordModelParentPropertyOperation;
import com.antares.nfc.plugin.operation.NdefModelOperation;

/**
 * Main editing (as in changing property values) class.
 * 
 * @author trs
 *
 */

public class NdefRecordModelEditingSupport extends EditingSupport {

	private static final Object EMPTY_STRING = "";

	public static final String[] PRESENT_OR_NOT = new String[]{"Present", "Not present"};

	@SuppressWarnings({ "unchecked" })
	private static Class<? extends Record>[] recordTypes = new Class[]{
			AbsoluteUriRecord.class,
			ActionRecord.class,
			AndroidApplicationRecord.class,
			UnsupportedExternalTypeRecord.class,
			EmptyRecord.class,
			MimeRecord.class,
			SmartPosterRecord.class,
			TextRecord.class,
			UnknownRecord.class,
			UriRecord.class,
			HandoverSelectRecord.class,
			HandoverCarrierRecord.class,
			HandoverRequestRecord.class,
			
			GenericControlRecord.class
	};
	
	@SuppressWarnings({ "unchecked" })
	public static Class<? extends Record>[] wellKnownRecordTypes = new Class[]{
			ActionRecord.class,
			SmartPosterRecord.class,
			TextRecord.class,
			UriRecord.class,
			
			AlternativeCarrierRecord.class,
			HandoverSelectRecord.class,
			HandoverCarrierRecord.class,
			HandoverRequestRecord.class,
			
			GenericControlRecord.class
	};
	
	@SuppressWarnings({ "unchecked" })
	public static Class<? extends Record>[] externalRecordTypes = new Class[]{
			AndroidApplicationRecord.class,
			UnsupportedExternalTypeRecord.class,
	};
	
	@SuppressWarnings({"unchecked" })
	private Class<? extends Record>[] genericControlRecordTargetRecordTypes = new Class[]{
			TextRecord.class,
			UriRecord.class,
	};

	
	private NdefRecordModelChangeListener listener;
	
	private TextCellEditor textCellEditor;
	private TreeViewer treeViewer;
	
	private NdefRecordFactory ndefRecordFactory;
	
	private Map<Class<? extends Record>, RecordEditingSupport> editing = new HashMap<Class<? extends Record>, RecordEditingSupport>();
	
	private static interface RecordEditingSupport {
		
		boolean canEdit(NdefRecordModelNode node);
		
		NdefModelOperation setValue(NdefRecordModelNode node, Object value);

		Object getValue(NdefRecordModelNode node);
		
		CellEditor getCellEditor(NdefRecordModelNode node);
	}
	
	private class DefaultRecordEditingSupport implements RecordEditingSupport {

		@Override
		public boolean canEdit(NdefRecordModelNode node) {
			return true;
		}
		
		@Override
		public NdefModelOperation setValue(NdefRecordModelNode node, Object value) {
			if(node instanceof NdefRecordModelRecord) {
				String stringValue = (String)value;

				Record record = node.getRecord();
				
				if(!stringValue.equals(record.getKey())) {
					return new NdefModelOperation() {
						
						private Record record;
						private String previous;
						private String next;
						
						@Override
						public void revoke() {
							record.setKey(previous);
						}

						@Override
						public void execute() {
							record.setKey(next);
						}
						
						public NdefModelOperation init(Record record, String previous, String next) {
							this.record = record;
							
							this.previous = previous;
							this.next = next;
							
							return this;
						}
						
					}.init(record, record.getKey(), stringValue);
				}
			} else {
				throw new RuntimeException(node.getClass().getSimpleName());
			}
			return null;
		}
		
		@Override
		public Object getValue(NdefRecordModelNode node) {
			if(node instanceof NdefRecordModelRecord) {
				Record record = node.getRecord();
				
				return record.getKey();
			} else {
				throw new RuntimeException();
			}
		}

		@Override
		public CellEditor getCellEditor(NdefRecordModelNode node) {
			if(node instanceof NdefRecordModelRecord) {
				return textCellEditor;
			} else {
				throw new RuntimeException();
			}
		}		
	}
	
	private class ActionRecordEditingSupport extends DefaultRecordEditingSupport {

		@Override
		public NdefModelOperation setValue(NdefRecordModelNode node, Object value) {
			ActionRecord record = (ActionRecord) node.getRecord();
			if(node instanceof NdefRecordModelProperty) {
				Integer index = (Integer)value;
				
				Action action;
				if(index.intValue() != -1) {
					Action[] values = Action.values();
				
					action = values[index.intValue()];
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
					return record.getAction().ordinal();
				}
				return -1;
			} else {
				return super.getValue(node);
			}
		}

		@Override
		public CellEditor getCellEditor(NdefRecordModelNode node) {
			if(node instanceof NdefRecordModelProperty) {
				return getComboBoxCellEditor(Action.values(), false);
			} else {
				return super.getCellEditor(node);
			}

		}
	}
	
	private class HandoverCarrierRecordEditingSupport extends DefaultRecordEditingSupport {

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
										ndefRecordModelParentProperty.remove(0);
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
					
						Class<? extends Record>[] types;
						switch(carrierTypeFormat) {
							case WellKnown : {
								// NFC Forum well-known type [NFC RTD]
								types = wellKnownRecordTypes;
								break;
							}
							
							case External : {
								// NFC Forum external type [NFC RTD]
								types = externalRecordTypes;
								
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
								if(c ==  types[i]) {
									previousIndex = i;
									
									break;
								}
							}

						}
						
						if(index.intValue() != previousIndex) {
							return new DefaultNdefRecordModelParentPropertyOperation<Record, HandoverCarrierRecord>(record, (NdefRecordModelParentProperty)node, (Record)record.getCarrierType(),  ndefRecordFactory.createRecord(types[index]));
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

								return getIndex(wellKnownRecordTypes, carrierType.getClass());
							}
						
							case External : {
								// NFC Forum external type [NFC RTD]
								
								return getIndex(externalRecordTypes, carrierType.getClass());
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
					return textCellEditor;
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
							return getComboBoxCellEditor(wellKnownRecordTypes, false);
						}
					
						case External : {
							// NFC Forum external type [NFC RTD]
							return getComboBoxCellEditor(externalRecordTypes, false);
						}
					}
				}
				throw new RuntimeException();
			} else {
				return super.getCellEditor(node);
			}

		}
	}	
	
	private class GcActionRecordEditingSupport extends DefaultRecordEditingSupport  {

		@Override
		public NdefModelOperation setValue(NdefRecordModelNode node, Object value) {
			GcActionRecord gcActionRecord = (GcActionRecord) node.getRecord();
			if(node instanceof NdefRecordModelProperty) {
				Integer index = (Integer)value;
				
				Action action;
				if(index.intValue() != -1) {
					Action[] values = Action.values();
				
					action = values[index.intValue()];
				} else {
					action = null;
				}
				if(action != gcActionRecord.getAction()) {
					return new DefaultNdefModelPropertyOperation<Action, GcActionRecord>(gcActionRecord, (NdefRecordModelProperty)node, gcActionRecord.getAction(), action) {
						
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
				
			} else if(node instanceof NdefRecordModelParentProperty) {
				Integer index = (Integer)value;

				if(index.intValue() != -1) {
					int previousIndex = -1;
					if(gcActionRecord.hasActionRecord()) {
						for(int i = 0; i < recordTypes.length; i++) {
							if(gcActionRecord.getActionRecord().getClass() == recordTypes[i]) {
								previousIndex = i;
							}
						}
					}
					
					previousIndex++;

					if(previousIndex != index.intValue()) {
						
						NdefRecordModelParentProperty ndefRecordModelParentProperty = (NdefRecordModelParentProperty)node;
						if(index.intValue() == 0)  {
							listener.removeRecord((NdefRecordModelRecord) ndefRecordModelParentProperty.getChild(0));
						} else {
							return new DefaultNdefRecordModelParentPropertyOperation<Record, GcActionRecord>(gcActionRecord, (NdefRecordModelParentProperty)node, gcActionRecord.getActionRecord(), ndefRecordFactory.createRecord(recordTypes[index - 1]));
						}
						
						return null;
					}
					
				}			
			} else {
				return super.setValue(node, value);
			}
			return null;
		}

		@Override
		public Object getValue(NdefRecordModelNode node) {
			GcActionRecord gcActionRecord = (GcActionRecord) node.getRecord();
			if(node instanceof NdefRecordModelProperty) {
				if(gcActionRecord.hasAction()) {
					return gcActionRecord.getAction().ordinal();
				}
				return -1;
			} else if(node instanceof NdefRecordModelParentProperty) {
				if(gcActionRecord.hasActionRecord()) {
					return 1 + getIndex(recordTypes, gcActionRecord.getActionRecord().getClass());
				}
				return 0;
			} else {
				return super.getValue(node);
			}
		}

		@Override
		public CellEditor getCellEditor(NdefRecordModelNode node) {
			if(node instanceof NdefRecordModelProperty) {
				return getComboBoxCellEditor(Action.values(), false);
			} else if(node instanceof NdefRecordModelParentProperty) {
				return getComboBoxCellEditor(recordTypes, true);
			} else {
				return super.getCellEditor(node);
			}

		}
		
	}
	
	private class HandoverSelectRecordEditingSupport extends DefaultRecordEditingSupport {

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
				return textCellEditor;
			} else if(node instanceof NdefRecordModelParentProperty) {
				return new ComboBoxCellEditor(treeViewer.getTree(), PRESENT_OR_NOT);
			} else {
				return super.getCellEditor(node);
			}

		}
	}	
	
	private class HandoverRequestRecordEditingSupport extends DefaultRecordEditingSupport {

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
				return textCellEditor;
			} else if(node instanceof NdefRecordModelParentProperty) {
				return new ComboBoxCellEditor(treeViewer.getTree(), PRESENT_OR_NOT);
			} else {
				return super.getCellEditor(node);
			}

		}
	}		
	
	private class AlternativeCarrierRecordSelectEditingSupport extends DefaultRecordEditingSupport {
		
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
					return textCellEditor;
				} else if(parentIndex == 2) {
					return null;
				} else {
					throw new RuntimeException();
				}
			} else if(node instanceof NdefRecordModelParentProperty) {
				return new ComboBoxCellEditor(treeViewer.getTree(), PRESENT_OR_NOT);
			} else if(node instanceof NdefRecordModelPropertyListItem) {
				return textCellEditor;
			} else {
				return super.getCellEditor(node);
			}

		}
	}	
	
	
	private class AbsoluteUriRecordEditingSupport extends DefaultRecordEditingSupport {

		@Override
		public NdefModelOperation setValue(NdefRecordModelNode node, Object value) {
			AbsoluteUriRecord absoluteUriRecord = (AbsoluteUriRecord) node.getRecord();
			if(node instanceof NdefRecordModelProperty) {
				String stringValue = (String)value;
				
				if(!stringValue.equals(absoluteUriRecord.getUri())) {
					return new DefaultNdefModelPropertyOperation<String, AbsoluteUriRecord>(absoluteUriRecord, (NdefRecordModelProperty)node,absoluteUriRecord.getUri(), stringValue) {
						
						@Override
						public void execute() {
							super.execute();
							
							record.setUri(next);
						}
						
						@Override
						public void revoke() {
							super.revoke();
							
							record.setUri(previous);
						}
					};					
					
					
				}
			} else {
				return super.setValue(node, value);
			}
			return null;
		}

		@Override
		public Object getValue(NdefRecordModelNode node) {
			AbsoluteUriRecord absoluteUriRecord = (AbsoluteUriRecord) node.getRecord();
			if(node instanceof NdefRecordModelProperty) {
				if(absoluteUriRecord.hasUri()) {
					return absoluteUriRecord.getUri();
				} else {
					return EMPTY_STRING;
				}
			} else {
				return super.getValue(node);
			}
		}

		@Override
		public CellEditor getCellEditor(NdefRecordModelNode node) {
			if(node instanceof NdefRecordModelProperty) {
				return textCellEditor;
			} else {
				return super.getCellEditor(node);
			}
		}
	}
	
	private class UriRecordEditingSuppport extends DefaultRecordEditingSupport {

		@Override
		public NdefModelOperation setValue(NdefRecordModelNode node, Object value) {
			UriRecord uriRecord = (UriRecord) node.getRecord();
			if(node instanceof NdefRecordModelProperty) {
				String stringValue = (String)value;
				
				if(!stringValue.equals(uriRecord.getUri())) {
					return new DefaultNdefModelPropertyOperation<String, UriRecord>(uriRecord, (NdefRecordModelProperty)node, uriRecord.getUri(), stringValue) {
						
						@Override
						public void execute() {
							super.execute();
							
							record.setUri(next);
						}
						
						@Override
						public void revoke() {
							super.revoke();
							
							record.setUri(previous);
						}
					};	
				}
			} else {
				return super.setValue(node, value);
			}
			return null;
		}

		@Override
		public Object getValue(NdefRecordModelNode node) {
			UriRecord uriRecord = (UriRecord) node.getRecord();
			if(node instanceof NdefRecordModelProperty) {
				if(uriRecord.hasUri()) {
					return uriRecord.getUri();
				} else {
					return EMPTY_STRING;
				}

			} else {
				return super.getValue(node);
			}
		}

		@Override
		public CellEditor getCellEditor(NdefRecordModelNode node) {
			if(node instanceof NdefRecordModelProperty) {
				return textCellEditor;
			} else {
				return super.getCellEditor(node);
			}
		}
	}
	
	private class CollisionResolutionRecordEditingSupport extends DefaultRecordEditingSupport {

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
				return textCellEditor;
			} else {
				return super.getCellEditor(node);
			}
		}
	}
	
	
	private class ErrorRecordEditingSupport extends DefaultRecordEditingSupport {

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
					return textCellEditor;
				}
			} else {
				return super.getCellEditor(node);
			}
		}
	}

	
	
	private class TextRecordEditingSuppport extends DefaultRecordEditingSupport {

		@Override
		public NdefModelOperation setValue(NdefRecordModelNode node, Object value) {
			TextRecord textRecord = (TextRecord) node.getRecord();
			if(node instanceof NdefRecordModelProperty) {
				int propertyIndex  = node.getParentIndex();
				if(propertyIndex == 0) {
					String stringValue = (String)value;
					
					if(!stringValue.equals(textRecord.getText())) {
					
						return new DefaultNdefModelPropertyOperation<String, TextRecord>(textRecord, (NdefRecordModelProperty)node, textRecord.getText(), stringValue) {
							
							@Override
							public void execute() {
								super.execute();
								
								record.setText(next);
							}
							
							@Override
							public void revoke() {
								super.revoke();
								
								record.setText(previous);
							}
						};	
						
						
					}
				} else if(propertyIndex == 1) {
					if(value instanceof Integer) {
						Integer index = (Integer)value;
	
						if(index.intValue() != -1) {
							String[] values = Locale.getISOLanguages();
		
							Locale locale = new Locale(values[index.intValue()]);
							if(!locale.equals(textRecord.getLocale())) {
								return new DefaultNdefModelPropertyOperation<Locale, TextRecord>(textRecord, (NdefRecordModelProperty)node, textRecord.getLocale(), locale) {
									
									@Override
									public void execute() {
										ndefRecordModelProperty.setValue(next.getLanguage());
										
										record.setLocale(next);
									}
									
									@Override
									public void revoke() {
										ndefRecordModelProperty.setValue(previous.getLanguage());
										
										record.setLocale(previous);
									}
								};	
								
								
							}
						}
					} else {
	
						// manually entered value
						final String stringValue = (String)value;
						
				    	Display.getCurrent().asyncExec(
				                new Runnable()
				                {
				                    public void run()
				                    {
										// http://www.vogella.de/articles/EclipseDialogs/article.html#dialogs_jfacemessage
										Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
										MessageDialog.openError(shell, "Error", "Illegal language '" + stringValue + "', must be ISO language code.");
				                    }
				                }
				            );
						
						
					}
				} else if(propertyIndex == 2) {
					if(value instanceof Integer) {
						Integer index = (Integer)value;
						
						Charset charset;
						if(index == 0) {
							charset = TextRecord.UTF8;
						} else if(index == 1) {
							charset = TextRecord.UTF16;
						} else {
							charset = null;
						}
							
						if(charset != null) {
							if(!charset.equals(textRecord.getEncoding())) {
								return new DefaultNdefModelPropertyOperation<Charset, TextRecord>(textRecord, (NdefRecordModelProperty)node, textRecord.getEncoding(), charset) {
									
									@Override
									public void execute() {
										ndefRecordModelProperty.setValue(next.displayName());
										
										record.setEncoding(next);
									}
									
									@Override
									public void revoke() {
										ndefRecordModelProperty.setValue(previous.displayName());
										
										record.setEncoding(previous);
									}
								};	

							}
						}
					} else {
						// manually entered value
						final String stringValue = (String)value;
						
				    	Display.getCurrent().asyncExec(
				                new Runnable()
				                {
				                    public void run()
				                    {
										// http://www.vogella.de/articles/EclipseDialogs/article.html#dialogs_jfacemessage
										Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
										MessageDialog.openError(shell, "Error", "Illegal encoding '" + stringValue + "', only UTF-8 and UTF-16 supported.");
				                    }
				                }
				            );
						}
				}			
			} else {
				return super.setValue(node, value);
			}
			return null;
		}

		@Override
		public Object getValue(NdefRecordModelNode node) {
			TextRecord textRecord = (TextRecord) node.getRecord();
			if(node instanceof NdefRecordModelProperty) {
				int propertyIndex  = node.getParentIndex();
				if(propertyIndex == 0) {
					if(textRecord.hasText()) {
						return textRecord.getText();
					} else {
						return EMPTY_STRING;
					}
					
				} else if(propertyIndex == 1) {
					// handle language
					int index;
					if(textRecord.hasLocale()) {
						index = getIndex(Locale.getISOLanguages(), textRecord.getLocale().getLanguage());
					} else {
						index = -1;
					}
					return index;
				} else if(propertyIndex == 2) {
					// handle encodings, utf-8 or utf-16

					Charset encoding = textRecord.getEncoding();
					if(encoding == TextRecord.UTF8 || TextRecord.UTF8.aliases().contains(encoding.name())) {
						return 0;
					} else if(encoding == TextRecord.UTF16 || TextRecord.UTF16.aliases().contains(encoding.name())) {
						return 1;
					}
					
					throw new IllegalArgumentException("Illegal encoding " + encoding);

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
				int index = node.getParentIndex();
				if(index == 1) {
					// handle language codes
					
					final String[] isoLanguages = Locale.getISOLanguages();
					
					return new ComboBoxCellEditor(treeViewer.getTree(), isoLanguages) {
						// subclass to allow typing of language value, if it is the list of iso languages
						protected Object doGetValue() {
							Integer integer =  (Integer) super.doGetValue();
							
							if(integer.intValue() == -1) {
								String text = ((CCombo)this.getControl()).getText();

								for(int i = 0; i < isoLanguages.length; i++) {
									if(isoLanguages[i].equalsIgnoreCase(text)) {
										return new Integer(i);
									}
								}
								
								// return entered text for error message
								return text;
							}
							return integer;
						}
					};
				} else if(index == 2) {
					// handle encodings, utf-8 or utf-16
					
					final String[] encodings = new String[]{TextRecord.UTF8.name(), TextRecord.UTF16.name()};
					
					return new ComboBoxCellEditor(treeViewer.getTree(), encodings) {
						// subclass to allow typing of language value, if it is the list of iso languages
						protected Object doGetValue() {
							Integer integer =  (Integer) super.doGetValue();
							
							if(integer.intValue() == -1) {
								final String text = ((CCombo)this.getControl()).getText();

								String uppercaseText = text.toUpperCase();
								
								if(TextRecord.UTF8.aliases().contains(uppercaseText)) {
									return new Integer(0);
								}
								
								if(TextRecord.UTF16.aliases().contains(uppercaseText)) {
									return new Integer(1);
								}
								
								// be extra helpful for special utf-16 encodings?
								if(uppercaseText.equals("UTF-16") || uppercaseText.equals("UTF16")) {
									return new Integer(1);
								}
								
								// return entered text for error message
								return text;
							}
							return integer;
						}
					};

				}
				return textCellEditor;
				
			} else {
				return super.getCellEditor(node);
			}
		}
	}
	
	private class AndroidApplicationRecordEditingSupport extends DefaultRecordEditingSupport {

		@Override
		public NdefModelOperation setValue(NdefRecordModelNode node, Object value) {
			AndroidApplicationRecord androidApplicationRecord = (AndroidApplicationRecord) node.getRecord();
			if(node instanceof NdefRecordModelProperty) {
				String stringValue = (String)value;
				
				if(!stringValue.equals(androidApplicationRecord.getPackageName())) {
					return new DefaultNdefModelPropertyOperation<String, AndroidApplicationRecord>(androidApplicationRecord, (NdefRecordModelProperty)node, androidApplicationRecord.getPackageName(), stringValue) {
						
						@Override
						public void execute() {
							super.execute();
							
							record.setPackageName(next);
						}
						
						@Override
						public void revoke() {
							super.revoke();
							
							record.setPackageName(previous);
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
			AndroidApplicationRecord androidApplicationRecord = (AndroidApplicationRecord) node.getRecord();
			if(node instanceof NdefRecordModelProperty) {
				if(androidApplicationRecord.hasPackageName()) {
					return androidApplicationRecord.getPackageName();
				} else {
					return EMPTY_STRING;
				}
			} else {
				return super.getValue(node);
			}
		}

		@Override
		public CellEditor getCellEditor(NdefRecordModelNode node) {
			if(node instanceof NdefRecordModelProperty) {
				return textCellEditor;
			} else {
				return super.getCellEditor(node);
			}
		}
	}
	
	private class ExternalTypeRecordEditingSupport extends DefaultRecordEditingSupport {

		@Override
		public NdefModelOperation setValue(NdefRecordModelNode node, Object value) {
			UnsupportedExternalTypeRecord unsupportedExternalTypeRecord = (UnsupportedExternalTypeRecord) node.getRecord();
			if(node instanceof NdefRecordModelProperty) {
				String stringValue = (String)value;
				
				int parentIndex = node.getParentIndex();
				if(parentIndex == 0) {
					if(!stringValue.equals(unsupportedExternalTypeRecord.getNamespace())) {
						return new DefaultNdefModelPropertyOperation<String, UnsupportedExternalTypeRecord>(unsupportedExternalTypeRecord, (NdefRecordModelProperty)node, unsupportedExternalTypeRecord.getNamespace(), stringValue) {
							
							@Override
							public void execute() {
								super.execute();
								
								record.setNamespace(next);
							}
							
							@Override
							public void revoke() {
								super.revoke();
								
								record.setNamespace(previous);
							}
						};
						
					}
				} else if(parentIndex == 1) {
					if(!stringValue.equals(unsupportedExternalTypeRecord.getContent())) {
						return new DefaultNdefModelPropertyOperation<String, UnsupportedExternalTypeRecord>(unsupportedExternalTypeRecord, (NdefRecordModelProperty)node, unsupportedExternalTypeRecord.getContent(), stringValue) {
							
							@Override
							public void execute() {
								super.execute();
								
								record.setContent(next);
							}
							
							@Override
							public void revoke() {
								super.revoke();
								
								record.setContent(previous);
							}
						};
					}
				}
				
				return null;
			} else {
				return super.setValue(node, value);
			}
		}

		@Override
		public Object getValue(NdefRecordModelNode node) {
			UnsupportedExternalTypeRecord unsupportedExternalTypeRecord = (UnsupportedExternalTypeRecord) node.getRecord();
			if(node instanceof NdefRecordModelProperty) {
				
				int parentIndex = node.getParentIndex();
				if(parentIndex == 0) {
					if(unsupportedExternalTypeRecord.hasNamespace()) {
						return unsupportedExternalTypeRecord.getNamespace();
					} else {
						return EMPTY_STRING;
					}
				} else if(parentIndex == 1) {
					if(unsupportedExternalTypeRecord.hasContent()) {
						return unsupportedExternalTypeRecord.getContent();
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
				return textCellEditor;
			} else {
				return super.getCellEditor(node);
			}
		}
	}

	private class MimeRecordEditingSupport extends DefaultRecordEditingSupport {

		@Override
		public NdefModelOperation setValue(NdefRecordModelNode node, Object value) {
			MimeRecord mimeRecord = (MimeRecord) node.getRecord();
			if(node instanceof NdefRecordModelProperty) {
				String stringValue = (String)value;
				
				int parentIndex = node.getParentIndex();
				if(parentIndex == 0) {
					if(!stringValue.equals(mimeRecord.getContentType())) {
						return new DefaultNdefModelPropertyOperation<String, MimeRecord>(mimeRecord, (NdefRecordModelProperty)node, mimeRecord.getContentType(), stringValue) {
							
							@Override
							public void execute() {
								super.execute();
								
								record.setContentType(next);
							}
							
							@Override
							public void revoke() {
								super.revoke();
								
								record.setContentType(previous);
							}
						};
					}
				} else if(parentIndex == 1) {
					if(mimeRecord instanceof BinaryMimeRecord) {
						BinaryMimeRecord binaryMimeRecord = (BinaryMimeRecord)mimeRecord;
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
							
							return new DefaultNdefModelPropertyOperation<byte[], BinaryMimeRecord>(binaryMimeRecord, (NdefRecordModelProperty)node, binaryMimeRecord.getContent(), payload) {
								
								@Override
								public void execute() {
									super.execute();
									
									record.setContent(next);
									
									if(next == null) {
										ndefRecordModelProperty.setValue("Zero byte data");
									} else {
										ndefRecordModelProperty.setValue(Integer.toString(next.length) + " bytes binary payload");
									}	

								}
								
								@Override
								public void revoke() {
									super.revoke();
									
									record.setContent(previous);
									
									if(previous == null) {
										ndefRecordModelProperty.setValue("Zero byte data");
									} else {
										ndefRecordModelProperty.setValue(Integer.toString(previous.length) + " bytes binary payload");
									}	
								}
							};
							
						}
					} else {
						throw new RuntimeException();
					}
				}
				
				return null;
			} else {
				return super.setValue(node, value);
			}
		}

		@Override
		public Object getValue(NdefRecordModelNode node) {
			MimeRecord mimeRecord = (MimeRecord) node.getRecord();
			if(node instanceof NdefRecordModelProperty) {
				int parentIndex = node.getParentIndex();
				if(parentIndex == 0) {
					return mimeRecord.getContentType();
				} else if(parentIndex == 1) {
					return EMPTY_STRING;
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
					return textCellEditor;
				} else if(parentIndex == 1) {
					return new FileDialogCellEditor(treeViewer.getTree());
				} else {
					throw new RuntimeException();
				}
			} else {
				return super.getCellEditor(node);
			}
		}
	}
	
	private class UnknownRecordEditingSupport extends DefaultRecordEditingSupport {

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
	

	private class GcTargetEditingSupport extends DefaultRecordEditingSupport {

		@Override
		public NdefModelOperation setValue(NdefRecordModelNode node, Object value) {
			GcTargetRecord gcTargetRecord = (GcTargetRecord) node.getRecord();

			if(node instanceof NdefRecordModelParentProperty) {
				Integer index = (Integer)value;

				if(index.intValue() != -1) {
					int previousIndex = -1;
					if(gcTargetRecord.hasTargetIdentifier()) {
						for(int i = 0; i < genericControlRecordTargetRecordTypes.length; i++) {
							if(gcTargetRecord.getTargetIdentifier().getClass() == genericControlRecordTargetRecordTypes[i]) {
								previousIndex = i;
							}
						}
					}

					if(previousIndex != index.intValue()) {
						return new DefaultNdefRecordModelParentPropertyOperation<Record, GcTargetRecord>(gcTargetRecord, (NdefRecordModelParentProperty)node, gcTargetRecord.getTargetIdentifier(), ndefRecordFactory.createRecord(genericControlRecordTargetRecordTypes[index]));
					}
				}
			
			} else {
				return super.setValue(node, value);
			}
			return null;
		}

		@Override
		public Object getValue(NdefRecordModelNode node) {
			GcTargetRecord gcTargetRecord = (GcTargetRecord) node.getRecord();
			if(node instanceof NdefRecordModelParentProperty) {
				if(gcTargetRecord.hasTargetIdentifier()) {
					return getIndex(genericControlRecordTargetRecordTypes, gcTargetRecord.getTargetIdentifier().getClass());
				}
				return -1;
			} else {
				return super.getValue(node);
			}
		}

		@Override
		public CellEditor getCellEditor(NdefRecordModelNode node) {
			if(node instanceof NdefRecordModelParentProperty) {
				return getComboBoxCellEditor(genericControlRecordTargetRecordTypes, false);
			} else {
				return super.getCellEditor(node);
			}
		}
	}
	
	private class GenericControlRecordEditingSupport extends DefaultRecordEditingSupport {

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
				return textCellEditor;
			} else {
				return super.getCellEditor(node);
			}
		}
	}
		
	public NdefRecordModelEditingSupport(TreeViewer viewer, NdefRecordModelChangeListener listener, NdefRecordFactory ndefRecordFactory) {
		super(viewer);
		this.listener = listener;
		this.treeViewer = viewer;
		this.ndefRecordFactory = ndefRecordFactory;
		
		this.textCellEditor = new TextCellEditor(viewer.getTree());
		
		editing.put(ActionRecord.class, new ActionRecordEditingSupport());
		editing.put(GcActionRecord.class, new GcActionRecordEditingSupport());
		editing.put(GcTargetRecord.class, new GcTargetEditingSupport());
		editing.put(GcDataRecord.class, new DefaultRecordEditingSupport());
		editing.put(GenericControlRecord.class, new GenericControlRecordEditingSupport());
		
		editing.put(TextRecord.class, new TextRecordEditingSuppport());
		editing.put(AndroidApplicationRecord.class, new AndroidApplicationRecordEditingSupport());
		editing.put(AbsoluteUriRecord.class, new AbsoluteUriRecordEditingSupport());
		editing.put(UriRecord.class, new UriRecordEditingSuppport());
		editing.put(ErrorRecord.class, new ErrorRecordEditingSupport());
		editing.put(UnsupportedExternalTypeRecord.class, new ExternalTypeRecordEditingSupport());
		
		editing.put(SmartPosterRecord.class, new DefaultRecordEditingSupport());
		editing.put(EmptyRecord.class, new DefaultRecordEditingSupport());

		editing.put(BinaryMimeRecord.class, new MimeRecordEditingSupport());

		editing.put(HandoverSelectRecord.class, new HandoverSelectRecordEditingSupport());
		editing.put(AlternativeCarrierRecord.class, new AlternativeCarrierRecordSelectEditingSupport());

		editing.put(CollisionResolutionRecord.class, new CollisionResolutionRecordEditingSupport());
		
		editing.put(HandoverCarrierRecord.class, new HandoverCarrierRecordEditingSupport());
		editing.put(HandoverRequestRecord.class, new HandoverRequestRecordEditingSupport());

		editing.put(UnknownRecord.class, new UnknownRecordEditingSupport());
		
	}

	@Override
	protected boolean canEdit(Object element) {
		
		NdefRecordModelNode ndefRecordModelNode = (NdefRecordModelNode)element;
		Record record = ndefRecordModelNode.getRecord();
		if(record != null) {
			return editing.get(record.getClass()).canEdit(ndefRecordModelNode);
		}
		throw new RuntimeException();
	}
	
	protected ComboBoxCellEditor getComboBoxCellEditor(Object[] values, boolean nullable) {

		String[] strings;
		if(nullable) {
			strings = new String[values.length + 1];
			strings[0] = "-";
			
			for(int i = 0; i < values.length; i++) {
				strings[1 + i] = values[i].toString();
			}
		} else {
			strings = new String[values.length];
			
			for(int i = 0; i < values.length; i++) {
				strings[i] = values[i].toString();
			}
		}
		
		return new ComboBoxCellEditor(treeViewer.getTree(), strings);
	}

	protected ComboBoxCellEditor getComboBoxCellEditor(Class<? extends Record>[] values, boolean nullable) {
		
		String[] strings = new String[values.length];
		for(int i = 0; i < values.length; i++) {
			strings[i] = values[i].getSimpleName();
		}
		
		return getComboBoxCellEditor(strings, nullable);
	}
	
	@Override
	protected CellEditor getCellEditor(Object element) {
		NdefRecordModelNode ndefRecordModelNode = (NdefRecordModelNode)element;
		Record record = ndefRecordModelNode.getRecord();
		if(record != null) {
			return editing.get(record.getClass()).getCellEditor(ndefRecordModelNode);
		}
		throw new RuntimeException();
	}

	@Override
	protected Object getValue(Object element) {
		Activator.info("Get element " + element + " value");

		NdefRecordModelNode ndefRecordModelNode = (NdefRecordModelNode)element;
		Record record = ndefRecordModelNode.getRecord();
		if(record != null) {
			return editing.get(record.getClass()).getValue(ndefRecordModelNode);
		}
		throw new RuntimeException();
	}

	private int getIndex(Object[] values, Object value) {
		if(value != null) {
			for(int i = 0; i < values.length; i++) {
				if(values[i] == value || values[i].equals(value)) {
					return i;
				}
			}
		}
		return -1;
	}

	@Override
	protected void setValue(Object element, Object value) {
		Activator.info("Set element " + element + " value " + value + ", currently have " + getValue(element));

		NdefRecordModelNode ndefRecordModelNode = (NdefRecordModelNode)element;
		Record record = ndefRecordModelNode.getRecord();
		if(record != null) {
			NdefModelOperation setValue = editing.get(record.getClass()).setValue(ndefRecordModelNode, value);

			if(setValue != null) {
				Activator.info("Model operation " + setValue.getClass().getSimpleName());
				if(listener != null) {
					listener.update(ndefRecordModelNode, setValue);
				}			

				// update all but the root node
				NdefRecordModelNode node = (NdefRecordModelNode) element;

				do {
					treeViewer.update(node, null);
					
					node = node.getParent();
				} while(node != null && node.hasParent());

			}
		}
		
		
	}
}