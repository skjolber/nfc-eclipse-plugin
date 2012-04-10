/***************************************************************************
 *
 * This file is part of the NFC Eclipse Plugin project at
 * http://code.google.com/p/nfc-eclipse-plugin/
 *
 * Copyright (C) 2012 by Thomas Rørvik Skjølberg / Antares Gruppen AS.
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

package com.antares.nfc.model;

import java.util.ArrayList;
import java.util.List;

public class NdefRecordModelParent extends NdefRecordModelNode {
	
	protected List<NdefRecordModelNode> children;
	
	public NdefRecordModelParent(List<NdefRecordModelNode> children, NdefRecordModelParent parent) {
		super(parent);
		this.children = children;
	}

	public NdefRecordModelParent(NdefRecordModelParent parent) {
		this(new ArrayList<NdefRecordModelNode>(), parent);
	}

	public NdefRecordModelParent() {
		this.children = new ArrayList<NdefRecordModelNode>();
	}

	public List<NdefRecordModelNode> getChildren() {
		return children;
	}

	public boolean hasChildren() {
		return children != null && children.size() > 0;
	}

	public boolean add(NdefRecordModelNode e) {
		return children.add(e);
	}

	public int indexOf(NdefRecordModelNode node) {
		for(int i = 0; i < children.size(); i++) { // do not use indexOf list, we do not want to check using equals, but using reference
			if(node == children.get(i)) {
				return i;
			}
		}
		return -1;
	}

	public void insert(NdefRecordModelNode node, int index) {
		children.add(index, node);
	}

	public NdefRecordModelNode getChild(int i) {
		return children.get(i);
	}

	public int getSize() {
		return children.size();
	}

	public void remove(int index) {
		children.remove(index);
	}

	public void remove(NdefRecordModelNode node) {
		for(int i = 0; i < children.size(); i++) { // do not use indexOf list, we do not want to check using equals, but using reference
			if(node == children.get(i)) {
				children.remove(i);
				
				break;
			}
		}
	}

	public boolean hasRecordChildren() {
		for(NdefRecordModelNode child : children) {
			if(child instanceof NdefRecordModelRecord) {
				return true;
			}
		}
		return false;
	}

}
