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

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.nfctools.ndef.Record;
import org.nfctools.ndef.wkt.records.GcTargetRecord;

import com.antares.nfc.model.NdefRecordModelMenuListener;
import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.model.NdefRecordModelParentProperty;
import com.antares.nfc.plugin.NdefRecordFactory;
import com.antares.nfc.plugin.operation.DefaultNdefRecordModelParentPropertyOperation;
import com.antares.nfc.plugin.operation.NdefModelOperation;

public class GcTargetEditingSupport extends DefaultRecordEditingSupport {

	private NdefRecordFactory ndefRecordFactory;

	public GcTargetEditingSupport(TreeViewer treeViewer, NdefRecordFactory ndefRecordFactory) {
		super(treeViewer);
		
		this.ndefRecordFactory = ndefRecordFactory;
	}
	
	@Override
	public NdefModelOperation setValue(NdefRecordModelNode node, Object value) {
		GcTargetRecord gcTargetRecord = (GcTargetRecord) node.getRecord();

		if(node instanceof NdefRecordModelParentProperty) {
			Integer index = (Integer)value;

			if(index.intValue() != -1) {
				int previousIndex = -1;
				if(gcTargetRecord.hasTargetIdentifier()) {
					for(int i = 0; i < NdefRecordModelMenuListener.genericControlRecordTargetRecordTypes.length; i++) {
						if(gcTargetRecord.getTargetIdentifier().getClass() == NdefRecordModelMenuListener.genericControlRecordTargetRecordTypes[i].getRecordClass()) {
							previousIndex = i;
						}
					}
				}

				if(previousIndex != index.intValue()) {
					return new DefaultNdefRecordModelParentPropertyOperation<Record, GcTargetRecord>(gcTargetRecord, (NdefRecordModelParentProperty)node, gcTargetRecord.getTargetIdentifier(), ndefRecordFactory.createRecord(NdefRecordModelMenuListener.genericControlRecordTargetRecordTypes[index].getRecordClass()));
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
				return getIndex(NdefRecordModelMenuListener.genericControlRecordTargetRecordTypes, gcTargetRecord.getTargetIdentifier().getClass());
			}
			return -1;
		} else {
			return super.getValue(node);
		}
	}

	@Override
	public CellEditor getCellEditor(NdefRecordModelNode node) {
		if(node instanceof NdefRecordModelParentProperty) {
			return getComboBoxCellEditor(NdefRecordModelMenuListener.genericControlRecordTargetRecordTypes, false);
		} else {
			return super.getCellEditor(node);
		}
	}
}