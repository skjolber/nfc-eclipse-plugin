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

import com.antares.nfc.model.NdefRecordModelFactory;
import com.antares.nfc.model.NdefRecordModelParentProperty;
import com.antares.nfc.model.NdefRecordModelRecord;
import com.antares.nfc.plugin.NdefRecordFactory;

public class DefaultNdefRecordModelParentPropertyOperation<V extends Record, R extends Record> implements NdefModelOperation {

	protected NdefRecordModelParentProperty ndefRecordModelParentProperty;

	protected V previous;
	protected NdefRecordModelRecord previousNode;
	
	protected V next;
	protected NdefRecordModelRecord nextNode;
	
	protected R record;

	public DefaultNdefRecordModelParentPropertyOperation(R record, NdefRecordModelParentProperty ndefRecordModelParentProperty, V previous, V next) {
		this.record = record;
		this.ndefRecordModelParentProperty = ndefRecordModelParentProperty;
		this.previous = previous;
		this.next = next;
		
		initialize();
	}
	
	public void initialize() {
		if(ndefRecordModelParentProperty.hasChildren()) {
			previousNode = (NdefRecordModelRecord) ndefRecordModelParentProperty.getChild(0);
		}
		
		nextNode = NdefRecordModelFactory.getNode(next, ndefRecordModelParentProperty);
	}							
	
	@Override
	public void execute() {
		if(previous != null) {
			NdefRecordFactory.disconnect(record, previous);
		}
		
		ndefRecordModelParentProperty.removeAllChildren();
		
		NdefRecordFactory.connect(record, next);
		
		ndefRecordModelParentProperty.add(nextNode);

	}
	
	@Override
	public void revoke() {
		NdefRecordFactory.disconnect(record, next);
		ndefRecordModelParentProperty.removeAllChildren();
		
		if(previous != null) {
			NdefRecordFactory.connect(record, previous);
		}
		
		if(previousNode != null) {
			ndefRecordModelParentProperty.add(previousNode);
		}
		
	}
	
}
