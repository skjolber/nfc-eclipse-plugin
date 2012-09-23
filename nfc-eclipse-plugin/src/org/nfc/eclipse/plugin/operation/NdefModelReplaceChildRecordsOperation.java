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

import java.util.ArrayList;
import java.util.List;

import org.nfc.eclipse.plugin.model.NdefRecordModelNode;
import org.nfc.eclipse.plugin.model.NdefRecordModelParent;


/**
 * 
 * Operation to replace parent records 
 * 
 * @author thomas
 *
 */

public class NdefModelReplaceChildRecordsOperation  implements NdefModelOperation {

	private NdefRecordModelParent parent;
	private List<NdefRecordModelNode> previous;
	private List<NdefRecordModelNode> next;

	public NdefModelReplaceChildRecordsOperation(NdefRecordModelParent parent, NdefRecordModelNode previous, NdefRecordModelNode next) {
		this.parent = parent;
		
		this.previous = new ArrayList<NdefRecordModelNode>();
		this.previous.add(previous);
		this.next = new ArrayList<NdefRecordModelNode>();
		this.next.add(next);
		
		initialize();
	}

	public NdefModelReplaceChildRecordsOperation(NdefRecordModelParent parent, List<NdefRecordModelNode> previous, List<NdefRecordModelNode> next) {
		this.parent = parent;
		this.previous = previous;
		this.next = next;
		
		initialize();
	}
	
	public void initialize() {
	}

	@Override
	public void execute() {
		parent.setChildren(next);
	}

	@Override
	public void revoke() {
		parent.setChildren(previous);
	}

}
