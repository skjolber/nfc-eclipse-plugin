package com.antares.nfc.model.editing;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.nfctools.ndef.wkt.records.SignatureRecord;
import org.nfctools.ndef.wkt.records.SignatureRecord.CertificateFormat;
import org.nfctools.ndef.wkt.records.SignatureRecord.SignatureType;

import com.antares.nfc.model.NdefRecordModelFactory;
import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.model.NdefRecordModelParent;
import com.antares.nfc.model.NdefRecordModelProperty;
import com.antares.nfc.model.NdefRecordModelPropertyListItem;
import com.antares.nfc.model.NdefRecordModelRecord;
import com.antares.nfc.plugin.operation.DefaultNdefModelListItemOperation;
import com.antares.nfc.plugin.operation.DefaultNdefModelPropertyOperation;
import com.antares.nfc.plugin.operation.NdefModelAddNodeOperation;
import com.antares.nfc.plugin.operation.NdefModelOperation;
import com.antares.nfc.plugin.operation.NdefModelOperationList;
import com.antares.nfc.plugin.operation.NdefModelRemoveNodeOperation;
import com.antares.nfc.plugin.operation.NdefModelReplaceChildRecordsOperation;

class SignatureRecordEditingSupport extends DefaultRecordEditingSupport {

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
						child = NdefRecordModelFactory.getNode("Embedded value", signature.length + " bytes", ndefRecordModelParent);
					} else {
						child = NdefRecordModelFactory.getNode("Embedded value", "-", ndefRecordModelParent);
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
						
						return new DefaultNdefModelPropertyOperation<byte[], SignatureRecord>(record, (NdefRecordModelProperty)node, record.getSignature(), payload) {
							
							@Override
							public void execute() {
								super.execute();
								
								record.setSignature(next);
								
								if(next == null) {
									ndefRecordModelProperty.setValue("Zero bytes");
								} else {
									ndefRecordModelProperty.setValue(Integer.toString(next.length) + " bytes");
								}	

							}
							
							@Override
							public void revoke() {
								super.revoke();
								
								record.setSignature(previous);
								
								if(previous == null) {
									ndefRecordModelProperty.setValue("Zero bytes");
								} else {
									ndefRecordModelProperty.setValue(Integer.toString(previous.length) + " bytes");
								}	
							}
						};
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
					
					int index = node.getParentIndex();
					
					return new DefaultNdefModelListItemOperation<byte[], SignatureRecord>(record, (NdefRecordModelPropertyListItem)node, record.getCertificates().get(index), payload) {
						
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
								return "Zero bytes";
							} else {
								return Integer.toString(object.length) + " bytes";
							}	
						}
					};
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