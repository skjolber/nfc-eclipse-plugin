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
import org.nfc.eclipse.plugin.model.NdefRecordModelNode;
import org.nfc.eclipse.plugin.model.NdefRecordModelParent;
import org.nfctools.ndef.Record;


public class NdefModelMoveRecordOperation implements NdefModelOperation {

	private NdefRecordModelNode node;
	
	private NdefRecordModelParent nextParent;
	private int nextIndex;
	
	private NdefRecordModelParent previousParent;
	private int previousIndex;
	
	public NdefModelMoveRecordOperation(NdefRecordModelNode node, NdefRecordModelParent nextParent, int nextIndex) {
		this.node = node;
		this.nextParent = nextParent;
		this.nextIndex = nextIndex;
		
		initialize();
	}

	private void initialize() {
		this.previousIndex = node.getParentIndex();
		this.previousParent = node.getParent();
		
		if(previousParent == nextParent) { // check if remove affects insert index
			int currentIndex = node.getParentIndex();

			if(currentIndex < nextIndex) {
				nextIndex--;
			}
		}
	}

	@Override
	public void execute() {
		
		Record previousRecord = previousParent.getRecord();

		if(previousRecord != null) {
			NdefRecordFactory.disconnect(previousRecord, node.getRecord());
		}

		previousParent.remove(node);

		nextParent.insert(node, nextIndex);

		Record nextRecord = nextParent.getRecord();
		if(nextRecord != null) {
			NdefRecordFactory.connect(nextRecord, node.getRecord());
		}
	}

	@Override
	public void revoke() {
		Record nextRecord = nextParent.getRecord();

		if(nextRecord != null) {
			NdefRecordFactory.disconnect(nextRecord, node.getRecord());
		}

		nextParent.remove(node);

		previousParent.insert(node, previousIndex);

		Record previousRecord = previousParent.getRecord();
		if(previousRecord != null) {
			NdefRecordFactory.connect(previousRecord, node.getRecord());
		}
	}

}
