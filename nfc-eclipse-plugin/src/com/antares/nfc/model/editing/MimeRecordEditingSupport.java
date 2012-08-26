package com.antares.nfc.model.editing;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.nfctools.ndef.mime.BinaryMimeRecord;
import org.nfctools.ndef.mime.MimeRecord;

import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.model.NdefRecordModelProperty;
import com.antares.nfc.plugin.operation.DefaultNdefModelPropertyOperation;
import com.antares.nfc.plugin.operation.NdefModelOperation;
import com.antares.nfc.plugin.operation.NdefModelOperationList;
import com.antares.nfc.plugin.util.FileDialogUtil;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.detector.ExtensionMimeDetector;

public class MimeRecordEditingSupport extends DefaultRecordEditingSupport {

	public MimeRecordEditingSupport(
			TreeViewer treeViewer) {
		super(treeViewer);
	}

	@Override
	public NdefModelOperation setValue(NdefRecordModelNode node, Object value) {
		MimeRecord mimeRecord = (MimeRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {
			String stringValue = (String)value;
			
			int parentIndex = node.getParentIndex();
			if(parentIndex == 0) {
				FileDialogUtil.registerMimeType(stringValue);
				
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

							// add to used extensions
							FileDialogUtil.registerExtension(ExtensionMimeDetector.getExtension(file.getName()));
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
						
						NdefModelOperation contentOperation = new DefaultNdefModelPropertyOperation<byte[], BinaryMimeRecord>(binaryMimeRecord, (NdefRecordModelProperty)node, binaryMimeRecord.getContent(), payload) {
							
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
						
						// can we auto-detect the mime type?
						String contentType = binaryMimeRecord.getContentType();
						if(contentType == null || contentType.length() == 0) {
							
							ExtensionMimeDetector extensionMimeDetector = new ExtensionMimeDetector();								
							Collection<MimeType> mimeTypes = extensionMimeDetector.getMimeTypes(file);
							if(!mimeTypes.isEmpty()) {
								
								MimeType mimeType = (MimeType) mimeTypes.iterator().next();
								
								NdefModelOperation mimeTypeOperation = new DefaultNdefModelPropertyOperation<String, MimeRecord>(mimeRecord, (NdefRecordModelProperty)node.getParent().getChild(0), mimeRecord.getContentType(), mimeType.toString()) {
										
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
									
								NdefModelOperationList listOperation = new NdefModelOperationList();
								listOperation.add(mimeTypeOperation);
								listOperation.add(contentOperation);
								
								return listOperation;
							}
						}
						
						return contentOperation;
						
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
				return new TextCellEditor(treeViewer.getTree());
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