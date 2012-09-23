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

package org.nfc.eclipse.plugin.operation;

import org.nfc.eclipse.plugin.NdefRecordFactory;
import org.nfc.eclipse.plugin.model.NdefRecordModelFactory;
import org.nfc.eclipse.plugin.model.NdefRecordModelNode;
import org.nfc.eclipse.plugin.model.NdefRecordModelParent;
import org.nfc.eclipse.plugin.model.NdefRecordModelRecord;
import org.nfctools.ndef.Record;


public class NdefModelAddNodeOperation implements NdefModelOperation {

	private NdefRecordModelParent parent;
	private NdefRecordModelNode child;
	
	private int index;

	public NdefModelAddNodeOperation(NdefRecordModelParent parent, Record child) {
		this(parent, child, -1);
	}
	
	public NdefModelAddNodeOperation(NdefRecordModelParent parent, Record child, int index) {
		this(parent, NdefRecordModelFactory.getNode(child, parent), index);
	}
	
	public NdefModelAddNodeOperation(NdefRecordModelParent parent, NdefRecordModelNode child, int index) {
		this.parent = parent;
		this.child = child;
		this.index = index;
	}

	public NdefModelAddNodeOperation(NdefRecordModelParent parent, NdefRecordModelNode child) {
		this(parent, child, -1);
	}

	@Override
	public void execute() {
		if(parent instanceof NdefRecordModelRecord && child instanceof NdefRecordModelRecord) {
			NdefRecordModelRecord ndefRecordModelRecordParent = (NdefRecordModelRecord)parent;
			
			NdefRecordFactory.connect(ndefRecordModelRecordParent.getRecord(), child.getRecord());
		}
		
		if(index != -1) {
			parent.insert(child, index);
		} else {
			parent.add(child);
		}
	}

	@Override
	public void revoke() {
		if(parent instanceof NdefRecordModelRecord && child instanceof NdefRecordModelRecord) {
			NdefRecordModelRecord ndefRecordModelRecordParent = (NdefRecordModelRecord)parent;
			
			NdefRecordFactory.disconnect(ndefRecordModelRecordParent.getRecord(), child.getRecord());
		}

		parent.remove(child);
		
	}

}
