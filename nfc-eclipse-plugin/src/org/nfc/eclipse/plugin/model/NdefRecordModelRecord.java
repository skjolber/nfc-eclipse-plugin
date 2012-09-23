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

package org.nfc.eclipse.plugin.model;

import java.util.List;

import org.nfctools.ndef.Record;

public class NdefRecordModelRecord extends NdefRecordModelParent {
	
	private Record record;
	private String name;

	public NdefRecordModelRecord(Record record, List<NdefRecordModelNode> children, NdefRecordModelParent parent) {
		super(children, parent);
		this.record = record;
	}

	public NdefRecordModelRecord(Record record, String name, List<NdefRecordModelNode> children, NdefRecordModelParent parent) {
		this(record, children, parent);
		this.name = name;
	}

	public NdefRecordModelRecord(Record record, NdefRecordModelParent parent) {
		super(parent);
		this.record = record;
	}

	public NdefRecordModelRecord(Record record, String name, NdefRecordModelParent parent) {
		this(record, parent);
		this.name = name;
	}
	
	public Record getRecord() {
		return record;
	}

	@Override
	public String toString() {
		return name != null ? name : record.getClass().getSimpleName();
	}

	
}
