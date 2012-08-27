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
import org.nfctools.ndef.wkt.records.Action;
import org.nfctools.ndef.wkt.records.ActionRecord;

import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.model.NdefRecordModelProperty;
import com.antares.nfc.plugin.operation.DefaultNdefModelPropertyOperation;
import com.antares.nfc.plugin.operation.NdefModelOperation;

public class ActionRecordEditingSupport extends DefaultRecordEditingSupport {

	public ActionRecordEditingSupport(TreeViewer treeViewer) {
		super(treeViewer);
	}

	@Override
	public NdefModelOperation setValue(NdefRecordModelNode node, Object value) {
		ActionRecord record = (ActionRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {
			Integer index = (Integer)value;
			
			Action action;
			if(index.intValue() > 0) {
				Action[] values = Action.values();
			
				action = values[index.intValue() - 1];
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
				return record.getAction().ordinal() + 1;
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
		} else {
			return super.getCellEditor(node);
		}

	}
}