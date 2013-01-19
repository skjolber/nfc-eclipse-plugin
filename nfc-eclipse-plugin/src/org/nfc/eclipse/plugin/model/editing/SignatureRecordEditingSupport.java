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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.nfc.eclipse.plugin.model.NdefRecordModelFactory;
import org.nfc.eclipse.plugin.model.NdefRecordModelNode;
import org.nfc.eclipse.plugin.model.NdefRecordModelParent;
import org.nfc.eclipse.plugin.model.NdefRecordModelProperty;
import org.nfc.eclipse.plugin.model.NdefRecordModelPropertyListItem;
import org.nfc.eclipse.plugin.model.NdefRecordModelRecord;
import org.nfc.eclipse.plugin.operation.DefaultNdefModelListItemOperation;
import org.nfc.eclipse.plugin.operation.DefaultNdefModelPropertyOperation;
import org.nfc.eclipse.plugin.operation.NdefModelAddNodeOperation;
import org.nfc.eclipse.plugin.operation.NdefModelOperation;
import org.nfc.eclipse.plugin.operation.NdefModelOperationList;
import org.nfc.eclipse.plugin.operation.NdefModelRemoveNodeOperation;
import org.nfc.eclipse.plugin.operation.NdefModelReplaceChildRecordsOperation;
import org.nfctools.ndef.wkt.records.SignatureRecord;
import org.nfctools.ndef.wkt.records.SignatureRecord.CertificateFormat;
import org.nfctools.ndef.wkt.records.SignatureRecord.SignatureType;


public class SignatureRecordEditingSupport extends DefaultRecordEditingSupport {

	public static NdefModelOperation newSetSignatureOperation(SignatureRecord record, NdefRecordModelProperty node, byte[] next) {
		return new SetSignatureContentOperation(record, (NdefRecordModelProperty)node, record.getSignature(), next);
	}

	private static class SetSignatureContentOperation extends DefaultNdefModelPropertyOperation<byte[], SignatureRecord> {

		public SetSignatureContentOperation(SignatureRecord record, NdefRecordModelProperty ndefRecordModelProperty, byte[] previous, byte[] next) {
			super(record, ndefRecordModelProperty, previous, next);
		}

		@Override
		public void execute() {
			super.execute();
			
			record.setSignature(next);
			
			if(next == null) {
				ndefRecordModelProperty.setValue(NdefRecordModelFactory.getNoBytesString());
			} else {
				ndefRecordModelProperty.setValue(NdefRecordModelFactory.getBytesString(next.length));
			}	

		}
		
		@Override
		public void revoke() {
			super.revoke();
			
			record.setSignature(previous);
			
			if(previous == null) {
				ndefRecordModelProperty.setValue(NdefRecordModelFactory.getNoBytesString());
			} else {
				ndefRecordModelProperty.setValue(NdefRecordModelFactory.getBytesString(previous.length));
			}	
		}		
	}

	public static NdefModelOperation newSetCertificateValueOperation(SignatureRecord record, NdefRecordModelPropertyListItem node, byte[] next) {
		return new SetContentOperation(record, (NdefRecordModelPropertyListItem)node, record.getCertificates().get(node.getParentIndex()), next);
	}

	private static class SetContentOperation extends DefaultNdefModelListItemOperation<byte[], SignatureRecord> {

		public SetContentOperation(SignatureRecord record, NdefRecordModelPropertyListItem ndefRecordModelProperty, byte[] previous, byte[] next) {
			super(record, ndefRecordModelProperty, previous, next);
		}

		@Override
		public void execute() {
			super.execute();
			
			int index = ndefRecordModelPropertyListItem.getParentIndex();

			record.getCertificates().set(index, next);
		}
		
		@Override
		public void revoke() {
			super.revoke();

			int index = ndefRecordModelPropertyListItem.getParentIndex();

			record.getCertificates().set(index, previous);
		}
		
		@Override
		public String toString(byte[] object) {
			if(object == null || object.length == 0) {
				return NdefRecordModelFactory.getNoBytesString();
			} else {
				return NdefRecordModelFactory.getBytesString(object.length);
			}	
		}
	}

	// convention: empty string or empty certificates, but no null values
	
	public SignatureRecordEditingSupport(
			TreeViewer treeViewer) {
		super(treeViewer);
	}

	@Override
	public NdefModelOperation setValue(NdefRecordModelNode node, Object value) {
		final SignatureRecord record = (SignatureRecord) node.getRecord();
		int recordIndex = node.getRecordBranchIndex();
		if(recordIndex == 0) {
			String stringValue = (String)value;

			try {
				byte byteValue = Byte.parseByte(stringValue);
				if(byteValue < 0) {
					throw new NumberFormatException();
				}
				if(byteValue != record.getVersion()) {
					
					return new DefaultNdefModelPropertyOperation<Byte, SignatureRecord>(record, (NdefRecordModelProperty)node, record.getVersion(), byteValue) {
						
						@Override
						public void execute() {
							super.execute();
							
							record.setVersion(next);
						}
						
						@Override
						public void revoke() {
							super.revoke();
							
							record.setVersion(previous);
						}
					};
				}
			} catch(Exception e) {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				MessageDialog.openError(shell, "Error", "Could not set value '" + stringValue + "', reverting to previous value.");
			}
		
			return null;
		} else if(recordIndex == 1) {
			Integer index = (Integer)value;
			
			SignatureType type;
			if(index.intValue() > 0) {
				SignatureType[] values = SignatureType.values();
			
				type = values[index.intValue() - 1];
			} else {
				type = null;
			}
			if(type != record.getSignatureType()) {

				NdefModelOperation first = new DefaultNdefModelPropertyOperation<SignatureType, SignatureRecord>(record, (NdefRecordModelProperty)node, record.getSignatureType(), type) {
					
					@Override
					public void execute() {
						super.execute();
						
						record.setSignatureType(next);
					}
					
					@Override
					public void revoke() {
						super.revoke();
						
						record.setSignatureType(previous);
					}
				};
				
				if(type == SignatureType.NOT_PRESENT || record.getSignatureType() == SignatureType.NOT_PRESENT) {
					NdefRecordModelRecord recordNode = node.getRecordNode();

					List<NdefRecordModelNode> current = recordNode.getChildren();
					List<NdefRecordModelNode> next = new ArrayList<NdefRecordModelNode>();
					if(type == SignatureType.NOT_PRESENT) {
						// remove records
						next.add(current.get(0));
						next.add(current.get(1));
					} else if(record.getSignatureType() == SignatureType.NOT_PRESENT) {
						// add records
						next.addAll(current);
						next.addAll(NdefRecordModelFactory.getNonStartMarkerNodes(record, recordNode));
					}

					NdefModelOperation second = new NdefModelReplaceChildRecordsOperation(recordNode, current, next);
					
					NdefModelOperationList ndefModelOperationList = new NdefModelOperationList();
					ndefModelOperationList.add(first);
					ndefModelOperationList.add(second);
					
					return ndefModelOperationList;
				} else {
					return first;
				}
			}
			return null;
		} else if(recordIndex == 2) {
			if(node instanceof NdefRecordModelParent) {
				NdefRecordModelParent ndefRecordModelParent = (NdefRecordModelParent)node;
				
				Integer index = (Integer)value;

				if(index == 0) {
					if(ndefRecordModelParent.hasChildren()) {
						return new NdefModelRemoveNodeOperation(ndefRecordModelParent, ndefRecordModelParent.getChild(0)) {

							private String signatureUri;
							private byte[] signature;
							
							@Override
							public void initialize() {
								signatureUri = record.getSignatureUri();
								signature = record.getSignature();
							}
							
							@Override
							public void execute() {
								super.execute();
								
								record.setSignature(null);
								record.setSignatureUri(null);
							}
							
							@Override
							public void revoke() {
								super.revoke();
								
								record.setSignature(signature);
								record.setSignatureUri(signatureUri);
							}								
						};
						
						
					} else {
						// do nothing
					}
				} else if(index == 1) { // linked
					NdefRecordModelNode child = NdefRecordModelFactory.getNode("URI", record.getSignatureUri(), ndefRecordModelParent);
					if(ndefRecordModelParent.hasChildren()) {
						return new NdefModelReplaceChildRecordsOperation(ndefRecordModelParent, ndefRecordModelParent.getChild(0), child) {
							
							private String signatureUri;
							private byte[] signature;
							
							@Override
							public void initialize() {
								signatureUri = record.getSignatureUri();
								signature = record.getSignature();
							}
							
							@Override
							public void execute() {
								super.execute();
								
								record.setSignature(null);
								record.setSignatureUri("");

							}
							@Override
							public void revoke() {
								super.revoke();
								
								record.setSignature(signature);
								record.setSignatureUri(signatureUri);
							}
						};
					} else {
						return new NdefModelAddNodeOperation(ndefRecordModelParent, child) {
							
							@Override
							public void execute() {
								super.execute();
								
								record.setSignature(null);
								record.setSignatureUri("");

							}
							@Override
							public void revoke() {
								super.revoke();
								
								record.setSignature(null);
								record.setSignatureUri(null);
							}											
						};
					}
				} else if(index == 2) { // embedded
					
					NdefRecordModelNode child;
					byte[] signature = record.getSignature();
					if(signature != null) {
						child = NdefRecordModelFactory.getNode("Embedded value", NdefRecordModelFactory.getBytesString(signature.length), ndefRecordModelParent);
					} else {
						child = NdefRecordModelFactory.getNode("Embedded value", NdefRecordModelFactory.getNoBytesString(), ndefRecordModelParent);
					}
					
					if(ndefRecordModelParent.hasChildren()) {
						return new NdefModelReplaceChildRecordsOperation(ndefRecordModelParent, ndefRecordModelParent.getChild(0), child) {
							
							private String signatureUri;
							private byte[] signature;
							
							@Override
							public void initialize() {
								signatureUri = record.getSignatureUri();
								signature = record.getSignature();
							}
							
							@Override
							public void execute() {
								super.execute();
								
								record.setSignature(new byte[0]);
								record.setSignatureUri(null);

							}
							@Override
							public void revoke() {
								super.revoke();
								
								record.setSignature(signature);
								record.setSignatureUri(signatureUri);
							}								
						};
					} else {
						return new NdefModelAddNodeOperation(ndefRecordModelParent, child) {
							
							@Override
							public void execute() {
								super.execute();
								
								record.setSignature(new byte[0]);
								record.setSignatureUri(null);

							}
							@Override
							public void revoke() {
								super.revoke();
								
								record.setSignature(null);
								record.setSignatureUri(null);
							}											
						};
					}
				}
				
			} else if(node instanceof NdefRecordModelProperty){
				// child value
				if(record.hasSignatureUri()) {
					String stringValue = (String)value;
					
					if(!stringValue.equals(record.getSignatureUri())) {
						return new DefaultNdefModelPropertyOperation<String, SignatureRecord>(record, (NdefRecordModelProperty)node, record.getSignatureUri(), stringValue) {
							
							@Override
							public void execute() {
								super.execute();
								
								record.setSignatureUri(next);
							}
							
							@Override
							public void revoke() {
								super.revoke();
								
								record.setSignatureUri(previous);
							}
						};	
					}
				} else if(record.hasSignature()) {
					// handle file dialog
					
					if(value != null) {
						
						byte[] payload = load((String)value);
						
						if(payload != null) {
							return newSetSignatureOperation(record, (NdefRecordModelProperty)node, payload);
						}
					}				
				}
			}
		} else if(recordIndex == 3) {
			Integer index = (Integer)value;
			
			CertificateFormat type;
			if(index.intValue() > 0) {
				CertificateFormat[] values = CertificateFormat.values();
			
				type = values[index.intValue() - 1];
			} else {
				type = null;
			}
			if(type != record.getCertificateFormat()) {
				
				return new DefaultNdefModelPropertyOperation<CertificateFormat, SignatureRecord>(record, (NdefRecordModelProperty)node, record.getCertificateFormat(), type) {
					
					@Override
					public void execute() {
						super.execute();
						
						record.setCertificateFormat(next);
					}
					
					@Override
					public void revoke() {
						super.revoke();
						
						record.setCertificateFormat(previous);
					}
				};
			}
			return null;
		} else if(recordIndex == 4) {
			if(node instanceof NdefRecordModelPropertyListItem) {
				// handle file dialog
				
				if(value != null) {
					
					byte[] payload = load((String)value);
					
					if(payload != null) {
						return newSetCertificateValueOperation(record, (NdefRecordModelPropertyListItem)node, payload);
					}
				}				
			}
		} else if(recordIndex == 5) {
			
			String stringValue = (String)value;
			
			if(!stringValue.equals(record.getCertificateUri())) {
				return new DefaultNdefModelPropertyOperation<String, SignatureRecord>(record, (NdefRecordModelProperty)node, record.getCertificateUri(), stringValue) {
					
					@Override
					public void execute() {
						super.execute();
						
						record.setCertificateUri(next);
					}
					
					@Override
					public void revoke() {
						super.revoke();
						
						record.setCertificateUri(previous);
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
		SignatureRecord signatureRecord = (SignatureRecord) node.getRecord();

		int index = node.getRecordBranchIndex();
		if(index == 0) {
			return Byte.toString(signatureRecord.getVersion());
		} else if(index == 1) {
			if(signatureRecord.hasSignatureType()) {
				return signatureRecord.getSignatureType().ordinal() + 1;
			}
			return 0;
		} else if(index == 2) {
			if(node instanceof NdefRecordModelParent) {
				// parent mode
				if(signatureRecord.hasSignatureUri()) {
					return 1;
				} else if(signatureRecord.hasSignature()) {
					return 2;
				}
				return 0;
			} else if(node instanceof NdefRecordModelProperty){
				// child value
				if(signatureRecord.hasSignatureUri()) {
					return signatureRecord.getSignatureUri();
				} else if(signatureRecord.hasSignature()) {
					// open file dialog
					return EMPTY_STRING;				
				}
			}
			throw new RuntimeException();
		} else if(index == 3) {
			if(signatureRecord.hasCertificateFormat()) {
				return signatureRecord.getCertificateFormat().ordinal() + 1;
			}
			return 0;
		} else if(index == 4) {
			if(node instanceof NdefRecordModelPropertyListItem) {
				// open file dialog
				return EMPTY_STRING;
			}
			throw new RuntimeException();
		} else if(index == 5) {
			if(signatureRecord.hasCertificateUri()) {
				return signatureRecord.getCertificateUri();
			}
			return EMPTY_STRING;
		} else {
			return super.getValue(node);
		}
	}

	@Override
	public CellEditor getCellEditor(NdefRecordModelNode node) {
		SignatureRecord signatureRecord = (SignatureRecord) node.getRecord();

		int index = node.getRecordBranchIndex();
		if(index == 0) {
			return new TextCellEditor(treeViewer.getTree());
		} else if(index == 1) {
			return getComboBoxCellEditor(SignatureRecord.SignatureType.values(), true);
		} else if(index == 2) {
			if(node instanceof NdefRecordModelParent) {
				// parent mode
				return getComboBoxCellEditor(new String[]{"Linked", "Embedded"}, true);
			} else if(node instanceof NdefRecordModelProperty){
				// child value
				if(signatureRecord.hasSignatureUri()) {
					return new TextCellEditor(treeViewer.getTree());
				} else if(signatureRecord.hasSignature()) {
					// open file dialog
					return new FileDialogCellEditor(treeViewer.getTree());
				}
			}
			throw new RuntimeException();
		} else if(index == 3) {
			return getComboBoxCellEditor(SignatureRecord.CertificateFormat.values(), true);
		} else if(index == 4) {
			if(node instanceof NdefRecordModelPropertyListItem) {
				// open file dialog
				return new FileDialogCellEditor(treeViewer.getTree());
			}
			return null; // no edit for list
		} else if(index == 5) {
			return new TextCellEditor(treeViewer.getTree());
		} else {
			return super.getCellEditor(node);
		}
	}
}