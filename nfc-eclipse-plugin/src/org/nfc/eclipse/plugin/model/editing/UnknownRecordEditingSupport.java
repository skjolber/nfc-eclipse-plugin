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
import org.eclipse.jface.viewers.TreeViewer;
import org.nfc.eclipse.plugin.model.NdefRecordModelBinaryProperty;
import org.nfc.eclipse.plugin.model.NdefRecordModelFactory;
import org.nfc.eclipse.plugin.model.NdefRecordModelNode;
import org.nfc.eclipse.plugin.model.NdefRecordModelProperty;
import org.nfc.eclipse.plugin.operation.DefaultNdefModelPropertyOperation;
import org.nfc.eclipse.plugin.operation.NdefModelOperation;
import org.nfctools.ndef.unknown.UnknownRecord;


public class UnknownRecordEditingSupport extends DefaultRecordEditingSupport {

	public static NdefModelOperation newSetContentOperation(UnknownRecord record, NdefRecordModelProperty node, byte[] next) {
		return new SetContentOperation(record, (NdefRecordModelProperty)node, record.getPayload(), next);
	}

	private static class SetContentOperation extends DefaultNdefModelPropertyOperation<byte[], UnknownRecord> {

		public SetContentOperation(UnknownRecord record, NdefRecordModelProperty ndefRecordModelProperty, byte[] previous, byte[] next) {
			super(record, ndefRecordModelProperty, previous, next);
		}

		@Override
		public void execute() {
			super.execute();
			
			record.setPayload(next);
			
			if(next == null) {
				ndefRecordModelProperty.setValue(NdefRecordModelFactory.getNoBytesString());
			} else {
				ndefRecordModelProperty.setValue(NdefRecordModelFactory.getBytesString(next.length) );
			}	

		}
		
		@Override
		public void revoke() {
			super.revoke();
			
			record.setPayload(previous);
			
			if(previous == null) {
				ndefRecordModelProperty.setValue(NdefRecordModelFactory.getNoBytesString());
			} else {
				ndefRecordModelProperty.setValue(NdefRecordModelFactory.getBytesString(previous.length));
			}	
		}
	}

	
	public UnknownRecordEditingSupport(
			TreeViewer treeViewer) {
		super(treeViewer);
	}

	@Override
	public NdefModelOperation setValue(NdefRecordModelNode node, Object value) {
		UnknownRecord unknownRecord = (UnknownRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {

			if(value != null) {
			
				byte[] payload = load((String)value);
				if(payload != null) {
					NdefRecordModelBinaryProperty ndefRecordModelBinaryProperty = (NdefRecordModelBinaryProperty)node;
					ndefRecordModelBinaryProperty.setFile((String)value);

					return newSetContentOperation(unknownRecord, (NdefRecordModelProperty)node, payload);
				}
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