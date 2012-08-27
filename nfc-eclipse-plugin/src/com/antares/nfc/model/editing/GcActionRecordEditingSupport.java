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
import org.nfctools.ndef.auri.AbsoluteUriRecord;
import org.nfctools.ndef.empty.EmptyRecord;
import org.nfctools.ndef.ext.AndroidApplicationRecord;
import org.nfctools.ndef.ext.UnsupportedExternalTypeRecord;
import org.nfctools.ndef.mime.BinaryMimeRecord;
import org.nfctools.ndef.unknown.UnknownRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverCarrierRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverRequestRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverSelectRecord;
import org.nfctools.ndef.wkt.records.Action;
import org.nfctools.ndef.wkt.records.ActionRecord;
import org.nfctools.ndef.wkt.records.GcActionRecord;
import org.nfctools.ndef.wkt.records.GenericControlRecord;
import org.nfctools.ndef.wkt.records.SignatureRecord;
import org.nfctools.ndef.wkt.records.SmartPosterRecord;
import org.nfctools.ndef.wkt.records.TextRecord;
import org.nfctools.ndef.wkt.records.UriRecord;

import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.model.NdefRecordModelParentProperty;
import com.antares.nfc.model.NdefRecordModelProperty;
import com.antares.nfc.model.NdefRecordType;
import com.antares.nfc.plugin.NdefRecordFactory;
import com.antares.nfc.plugin.operation.DefaultNdefModelPropertyOperation;
import com.antares.nfc.plugin.operation.DefaultNdefRecordModelParentPropertyOperation;
import com.antares.nfc.plugin.operation.NdefModelOperation;
import com.antares.nfc.plugin.operation.NdefModelRemoveNodeOperation;

public class GcActionRecordEditingSupport extends DefaultRecordEditingSupport  {

	private NdefRecordFactory ndefRecordFactory;

	public GcActionRecordEditingSupport(TreeViewer treeViewer, NdefRecordFactory ndefRecordFactory) {
		super(treeViewer);
		
		this.ndefRecordFactory = ndefRecordFactory;
	}

	private NdefRecordType[] recordTypes = new NdefRecordType[]{
		NdefRecordType.getType(AbsoluteUriRecord.class),
		NdefRecordType.getType(ActionRecord.class),
		NdefRecordType.getType(AndroidApplicationRecord.class),
		NdefRecordType.getType(EmptyRecord.class),
		NdefRecordType.getType(UnsupportedExternalTypeRecord.class),
		NdefRecordType.getType(BinaryMimeRecord.class),
		NdefRecordType.getType(SmartPosterRecord.class),
		NdefRecordType.getType(TextRecord.class),
		NdefRecordType.getType(UnknownRecord.class),
		NdefRecordType.getType(UriRecord.class),
		NdefRecordType.getType(HandoverSelectRecord.class),
		NdefRecordType.getType(HandoverCarrierRecord.class),
		NdefRecordType.getType(HandoverRequestRecord.class),
		
		NdefRecordType.getType(GenericControlRecord.class),
		NdefRecordType.getType(SignatureRecord.class),
};

	
	@Override
	public NdefModelOperation setValue(NdefRecordModelNode node, Object value) {
		GcActionRecord gcActionRecord = (GcActionRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {
			Integer index = (Integer)value;
			
			Action action;
			if(index.intValue() > 0) {
				Action[] values = Action.values();
			
				action = values[index.intValue() - 1];
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
						if(gcActionRecord.getActionRecord().getClass() == recordTypes[i].getRecordClass()) {
							previousIndex = i;
						}
					}
				}
				
				previousIndex++;

				if(previousIndex != index.intValue()) {
					
					NdefRecordModelParentProperty ndefRecordModelParentProperty = (NdefRecordModelParentProperty)node;
					if(index.intValue() == 0)  {
						return new NdefModelRemoveNodeOperation(ndefRecordModelParentProperty, ndefRecordModelParentProperty.getChild(0));
					} else {
						return new DefaultNdefRecordModelParentPropertyOperation<Record, GcActionRecord>(gcActionRecord, (NdefRecordModelParentProperty)node, gcActionRecord.getActionRecord(), ndefRecordFactory.createRecord(recordTypes[index - 1].getRecordClass()));
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
		GcActionRecord gcActionRecord = (GcActionRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {
			if(gcActionRecord.hasAction()) {
				return gcActionRecord.getAction().ordinal() + 1;
			}
			return 0;
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
			return getComboBoxCellEditor(Action.values(), true);
		} else if(node instanceof NdefRecordModelParentProperty) {
			return getComboBoxCellEditor(recordTypes, true);
		} else {
			return super.getCellEditor(node);
		}

	}
	
}