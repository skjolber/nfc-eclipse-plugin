/***************************************************************************
 *
 * This file is part of the NFC Eclipse Plugin project at
 * http://code.google.com/p/nfc-eclipse-plugin/
 *
 * Copyright (C) 2012 by Thomas Rorvik Skjolberg / Antares Gruppen AS.
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

public class CollisionResolutionRecordEditingSupport extends DefaultRecordEditingSupport {

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