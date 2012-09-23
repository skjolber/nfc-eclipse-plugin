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
import org.nfc.eclipse.plugin.model.NdefRecordModelNode;
import org.nfc.eclipse.plugin.model.NdefRecordModelProperty;
import org.nfc.eclipse.plugin.operation.DefaultNdefModelPropertyOperation;
import org.nfc.eclipse.plugin.operation.NdefModelOperation;
import org.nfctools.ndef.auri.AbsoluteUriRecord;


public class AbsoluteUriRecordEditingSupport extends DefaultRecordEditingSupport {

	public AbsoluteUriRecordEditingSupport(
			TreeViewer treeViewer) {
		super(treeViewer);
	}

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
			return new TextCellEditor(treeViewer.getTree());
		} else {
			return super.getCellEditor(node);
		}
	}
}