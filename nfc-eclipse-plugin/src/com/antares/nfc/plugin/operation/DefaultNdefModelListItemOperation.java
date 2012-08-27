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

package com.antares.nfc.plugin.operation;

import org.nfctools.ndef.Record;

import com.antares.nfc.model.NdefRecordModelPropertyListItem;

public class DefaultNdefModelListItemOperation<V, R extends Record> implements NdefModelOperation {

	protected NdefRecordModelPropertyListItem ndefRecordModelPropertyListItem;

	protected V previous;
	
	protected V next;
	
	protected R record;

	public DefaultNdefModelListItemOperation(R record, NdefRecordModelPropertyListItem ndefRecordModelPropertyListItem, V previous, V next) {
		this.record = record;
		this.ndefRecordModelPropertyListItem = ndefRecordModelPropertyListItem;
		this.previous = previous;
		this.next = next;
		
		initialize();
	}

	public void initialize() {
	}
	
	@Override
	public void execute() {
		if(next != null) {
			ndefRecordModelPropertyListItem.setValue(toString(next));
		} else {
			ndefRecordModelPropertyListItem.setValue("");
		}
	}

	@Override
	public void revoke() {
		if(previous != null) {
			ndefRecordModelPropertyListItem.setValue(toString(previous));
		} else {
			ndefRecordModelPropertyListItem.setValue("");
		}
	}
	
	public String toString(V object) {
		return object.toString();
	}
	
}
