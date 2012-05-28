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
import org.nfctools.ndef.NdefContext;
import org.nfctools.ndef.NdefEncoderException;
import org.nfctools.ndef.NdefMessageEncoder;
import org.nfctools.ndef.Record;
import org.nfctools.ndef.ext.AndroidApplicationRecord;
import org.nfctools.ndef.mime.MimeRecord;
import org.nfctools.ndef.wkt.records.UriRecord;

public class NdefRecordModelHintColumnProvider extends ColumnLabelProvider {

		private NdefMessageEncoder encoder = NdefContext.getNdefMessageEncoder();

		@Override
		public String getText(Object element) {
						
			if(element instanceof NdefRecordModelNode) {
				NdefRecordModelNode ndefRecordModelNode = (NdefRecordModelNode)element;

				Record record = ndefRecordModelNode.getRecord();

				// first check problems with encoding
				if(element instanceof NdefRecordModelRecord) {
					try {
						encoder.encodeSingle(ndefRecordModelNode.getRecord());
					} catch(NdefEncoderException e) {
						
						if(e.getLocation() == ndefRecordModelNode.getRecord()) {
							return e.getMessage();
						} else {
							// do nothing
						}
					} catch(Exception e) {
						// do nothing
					}
				}
				
				if(element instanceof NdefRecordModelRecord) {
					// do nothing
				} else {
					if(record instanceof AndroidApplicationRecord) {						
						// http://developer.android.com/guide/topics/nfc/nfc.html#aar
						AndroidApplicationRecord androidApplicationRecord = (AndroidApplicationRecord)record;
						
						if(androidApplicationRecord.hasPackageName()) {
			
							if(!androidApplicationRecord.matchesNamingConvension()) {
								return "Package convension violated";
							}
						} else {
							return "Enter package name of application";
						}
					
					} else if(record instanceof MimeRecord) {
						MimeRecord mimeRecord = (MimeRecord)record;
						
						if(ndefRecordModelNode.getParentIndex() == 0) {
							if(mimeRecord.hasContentType()) {
								String contentType = mimeRecord.getContentType();
								
								if(contentType.length() > 0) {
									int index = contentType.indexOf('/');
									if(index == -1) {
										return "MIME type convension violated";
									}
								} else {
									return "Enter mime type";
								}
							}
						}
					} else if(record instanceof UriRecord) {
						UriRecord uriRecord = (UriRecord)record;
						
						if(uriRecord.hasUri()) {
							int index = getAbbreviateIndex(uriRecord.getUri());
							
							if(index == 0) {
								return "Uri prefix not in preset list";
							}
						}			
					}
				}
			}
			
			return null;
		}
		
		private int getAbbreviateIndex(String uri) {
			int maxLength = 0;
			int abbreviateIndex = 0;
			for (int x = 1; x < UriRecord.abbreviableUris.length; x++) {

				String abbreviablePrefix = UriRecord.abbreviableUris[x];

				if (uri.startsWith(abbreviablePrefix) && abbreviablePrefix.length() > maxLength) {
					abbreviateIndex = x;
					maxLength = abbreviablePrefix.length();
				}
			}
			return abbreviateIndex;
		}
		
		@Override
		public Color getForeground(Object element) {
			
			if(element instanceof NdefRecordModelNode) {
				NdefRecordModelNode ndefRecordModelNode = (NdefRecordModelNode)element;

				Record record = ndefRecordModelNode.getRecord();

				// first check problems with encoding
				if(element instanceof NdefRecordModelRecord) {
					try {
						encoder.encodeSingle(ndefRecordModelNode.getRecord());
					} catch(NdefEncoderException e) {
						if(e.getLocation() == ndefRecordModelNode.getRecord()) {
							return new Color(Display.getCurrent(), 0xFF, 0x00, 0x00); 
						} else {
							// do nothing
						}
					} catch(Exception e) {
						// do nothing
					}
				}
				if(element instanceof NdefRecordModelRecord) {
					
				} else {								
					if(record instanceof AndroidApplicationRecord) {
							 
						AndroidApplicationRecord androidApplicationRecord = (AndroidApplicationRecord)record;
						
						if(androidApplicationRecord.hasPackageName()) {
			
							if(!androidApplicationRecord.matchesNamingConvension()) {
								return new Color(Display.getCurrent(), 0xFF, 0x00, 0x00); 
							}
						}
					} else if(record instanceof MimeRecord) {
						MimeRecord mimeRecord = (MimeRecord)record;
						
						if(ndefRecordModelNode.getParentIndex() == 0) {
							if(mimeRecord.hasContentType()) {
								String contentType = mimeRecord.getContentType();
								
								if(contentType.length() > 0) {
									int index = contentType.indexOf('/');
									if(index == -1) {
										return new Color(Display.getCurrent(), 0xFF, 0x00, 0x00); 
									}
								} else {
									return new Color(Display.getCurrent(), 0x00, 0x00, 0x00); 
								}
							}
						}
					} else if(record instanceof UriRecord) {
						UriRecord uriRecord = (UriRecord)record;
						
						if(uriRecord.hasUri()) {
							int index = getAbbreviateIndex(uriRecord.getUri());
							
							if(index == 0) {
								return new Color(Display.getCurrent(), 0x00, 0x00, 0x00); 
							}
						}			
										
					}
				}				
			}
			return super.getForeground(element);
		}
}
