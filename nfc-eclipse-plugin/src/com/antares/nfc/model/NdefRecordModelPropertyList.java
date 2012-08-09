/***************************************************************************
 *
 * This file is part of the NFC Eclipse Plugin project at
 * http://code.google.com/p/nfc-eclipse-plugin/
 *
 * Copyright (C) 2012 by Thomas R�rvik Skj�lberg / Antares Gruppen AS.
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

public class NdefRecordModelPropertyList extends NdefRecordModelParent {
	
	private String name;
	private String itemName;
	
	public NdefRecordModelPropertyList(String name, String itemName, List<NdefRecordModelNode> children, NdefRecordModelParent parent) {
		super(children, parent);
		this.name = name;
		this.itemName = itemName;
	}

	public NdefRecordModelPropertyList(String name, String itemName, NdefRecordModelParent parent) {
		this(name, itemName, new ArrayList<NdefRecordModelNode>(), parent);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	@Override
	public boolean add(NdefRecordModelNode e) {
		if(e instanceof NdefRecordModelPropertyListItem) {
			return super.add(e);
		}
		throw new IllegalArgumentException();
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public NdefRecordModelNode clone() {
		List<NdefRecordModelNode> children = new ArrayList<NdefRecordModelNode>();
		for(NdefRecordModelNode child : this.children) {
			children.add(child.clone());
		}
		
		return new NdefRecordModelPropertyList(name, itemName, children, parent);
	}
	
}
