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

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.nfctools.ndef.Record;

public class NdefRecordModelValueColumnLabelProvider extends ColumnLabelProvider {

		@Override
		public String getText(Object element) {
			if(element instanceof NdefRecordModelProperty) {
				NdefRecordModelProperty ndefRecordModelProperty = (NdefRecordModelProperty)element;
				
				// System.out.println("Get element " + element + " label " + ndefRecordModelProperty.getValue());

				return ndefRecordModelProperty.getValue();
			} else if(element instanceof NdefRecordModelRecord) {
				NdefRecordModelRecord ndefRecordModelRecord = (NdefRecordModelRecord)element;
				
				Record record = ndefRecordModelRecord.getRecord();
				
				byte[] id = record.getId();
				if(id == null || id.length == 0) {
					return "Record URI";
				}
				return record.getKey();
			}
			return null;
		}
		
		@Override
		public Color getForeground(Object element) {
			
			if(element instanceof NdefRecordModelRecord) {
				NdefRecordModelRecord ndefRecordModelRecord = (NdefRecordModelRecord)element;
			
				Record record = ndefRecordModelRecord.getRecord();
			
				byte[] id = record.getId();
				if(id == null || id.length == 0) {
					return new Color(Display.getCurrent(), 0xBB, 0xBB, 0xBB); 
				}
			}
			return super.getForeground(element);
		}
}
