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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.nfc.eclipse.plugin.NdefRecordFactory;
import org.nfc.eclipse.plugin.model.NdefRecordModelNode;
import org.nfc.eclipse.plugin.model.NdefRecordModelParentProperty;
import org.nfc.eclipse.plugin.model.NdefRecordModelProperty;
import org.nfc.eclipse.plugin.operation.DefaultNdefModelPropertyOperation;
import org.nfc.eclipse.plugin.operation.DefaultNdefRecordModelParentPropertyOperation;
import org.nfc.eclipse.plugin.operation.NdefModelOperation;
import org.nfctools.ndef.wkt.handover.records.ErrorRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverSelectRecord;


public class HandoverSelectRecordEditingSupport extends DefaultRecordEditingSupport {

	private NdefRecordFactory ndefRecordFactory;

	public HandoverSelectRecordEditingSupport(TreeViewer treeViewer, NdefRecordFactory ndefRecordFactory) {
		super(treeViewer);
		
		this.ndefRecordFactory = ndefRecordFactory;
	}
	
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
			return new TextCellEditor(treeViewer.getTree());
		} else if(node instanceof NdefRecordModelParentProperty) {
			return new ComboBoxCellEditor(treeViewer.getTree(), NdefRecordModelEditingSupport.PRESENT_OR_NOT, ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION);
		} else {
			return super.getCellEditor(node);
		}

	}
}