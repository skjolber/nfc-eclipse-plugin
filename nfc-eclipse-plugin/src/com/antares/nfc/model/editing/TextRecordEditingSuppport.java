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

package com.antares.nfc.model.editing;

import java.nio.charset.Charset;
import java.util.Locale;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.nfctools.ndef.wkt.records.TextRecord;

import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.model.NdefRecordModelProperty;
import com.antares.nfc.model.NdefTextRecordLocale;
import com.antares.nfc.plugin.operation.DefaultNdefModelPropertyOperation;
import com.antares.nfc.plugin.operation.NdefModelOperation;

public class TextRecordEditingSuppport extends DefaultRecordEditingSupport {

	public TextRecordEditingSuppport(
			TreeViewer treeViewer) {
		super(treeViewer);
	}

	private NdefTextRecordLocale localeSupport = new NdefTextRecordLocale();
	
	@Override
	public NdefModelOperation setValue(NdefRecordModelNode node, final Object value) {
		TextRecord textRecord = (TextRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {
			int propertyIndex  = node.getParentIndex();
			if(propertyIndex == 0) {
				String stringValue = (String)value;
				
				if(!stringValue.equals(textRecord.getText())) {
				
					return new DefaultNdefModelPropertyOperation<String, TextRecord>(textRecord, (NdefRecordModelProperty)node, textRecord.getText(), stringValue) {
						
						@Override
						public void execute() {
							super.execute();
							
							record.setText(next);
						}
						
						@Override
						public void revoke() {
							super.revoke();
							
							record.setText(previous);
						}
					};	
					
					
				}
			} else if(propertyIndex == 1) {
				Locale locale = null;
				
				if(value instanceof Integer) {
					locale = localeSupport.get((Integer)value);
				} else {
					// manually entered value, try to detect locale

					locale = localeSupport.getLocaleFromString((String)value);
				}
				
				if(locale != null) {
					// do not replace the text record if the string representation is equivalent
					if(!locale.equals(textRecord.getLocale()) && !NdefTextRecordLocale.getLocaleString(locale).equals(NdefTextRecordLocale.getLocaleString(textRecord.getLocale()))) {
						return new DefaultNdefModelPropertyOperation<Locale, TextRecord>(textRecord, (NdefRecordModelProperty)node, textRecord.getLocale(), locale) {

							@Override
							public void execute() {
								ndefRecordModelProperty.setValue(NdefTextRecordLocale.getLocaleString(next));

								record.setLocale(next);
							}

							@Override
							public void revoke() {
								ndefRecordModelProperty.setValue(NdefTextRecordLocale.getLocaleString(previous));

								record.setLocale(previous);
							}
						};	


					}
				} else {
					Display.getCurrent().asyncExec(
							new Runnable()
							{
								public void run()
								{
									// http://www.vogella.de/articles/EclipseDialogs/article.html#dialogs_jfacemessage
									Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
									MessageDialog.openError(shell, "Error", "Illegal locale '" + value + "', must be previously known RFC 3066 locale.");
								}
							}
							);


				}
			} else if(propertyIndex == 2) {
				if(value instanceof Integer) {
					Integer index = (Integer)value;
					
					Charset charset;
					if(index == 0) {
						charset = TextRecord.UTF8;
					} else if(index == 1) {
						charset = TextRecord.UTF16;
					} else {
						charset = null;
					}
						
					if(charset != null) {
						if(!charset.equals(textRecord.getEncoding())) {
							return new DefaultNdefModelPropertyOperation<Charset, TextRecord>(textRecord, (NdefRecordModelProperty)node, textRecord.getEncoding(), charset) {
								
								@Override
								public void execute() {
									ndefRecordModelProperty.setValue(next.displayName());
									
									record.setEncoding(next);
								}
								
								@Override
								public void revoke() {
									ndefRecordModelProperty.setValue(previous.displayName());
									
									record.setEncoding(previous);
								}
							};	

						}
					}
				} else {
					// manually entered value
					final String stringValue = (String)value;
					
			    	Display.getCurrent().asyncExec(
			                new Runnable()
			                {
			                    public void run()
			                    {
									// http://www.vogella.de/articles/EclipseDialogs/article.html#dialogs_jfacemessage
									Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
									MessageDialog.openError(shell, "Error", "Illegal encoding '" + stringValue + "', only UTF-8 and UTF-16 supported.");
			                    }
			                }
			            );
					}
			}			
		} else {
			return super.setValue(node, value);
		}
		return null;
	}

	@Override
	public Object getValue(NdefRecordModelNode node) {
		TextRecord textRecord = (TextRecord) node.getRecord();
		if(node instanceof NdefRecordModelProperty) {
			int propertyIndex  = node.getParentIndex();
			if(propertyIndex == 0) {
				if(textRecord.hasText()) {
					return textRecord.getText();
				} else {
					return EMPTY_STRING;
				}
				
			} else if(propertyIndex == 1) {
				// handle language
				int index;
				if(textRecord.hasLocale()) {
					
					index = localeSupport.spawnIndex(textRecord.getLocale());
				} else {
					index = -1;
				}
				return index;
			} else if(propertyIndex == 2) {
				// handle encodings, utf-8 or utf-16

				Charset encoding = textRecord.getEncoding();
				if(encoding == TextRecord.UTF8 || TextRecord.UTF8.aliases().contains(encoding.name())) {
					return 0;
				} else if(encoding == TextRecord.UTF16 || TextRecord.UTF16.aliases().contains(encoding.name())) {
					return 1;
				}
				
				throw new IllegalArgumentException("Illegal encoding " + encoding);

			} else {
				throw new RuntimeException();
			}
		
		} else {
			return super.getValue(node);
		}
	}

	@Override
	public CellEditor getCellEditor(NdefRecordModelNode node) {
		if(node instanceof NdefRecordModelProperty) {
			int index = node.getParentIndex();
			if(index == 1) {
				// handle language codes
				
				return new ComboBoxCellEditor(treeViewer.getTree(), localeSupport.getStringLocales(), ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION) {
					// subclass to allow typing of language value, if it is the list of locales
					protected Object doGetValue() {
						Integer integer =  (Integer) super.doGetValue();
						
						if(integer.intValue() == -1) {
							String text = ((CCombo)this.getControl()).getText();

							Locale locale = localeSupport.getLocaleFromString(text);
							if(locale != null) {
								return new Integer(localeSupport.getIndex(locale));
							}
							
							// return entered text for error message
							return text;
						}
						return integer;
					}
				};
			} else if(index == 2) {
				// handle encodings, utf-8 or utf-16
				
				final String[] encodings = new String[]{TextRecord.UTF8.name(), TextRecord.UTF16.name()};
				
				return new ComboBoxCellEditor(treeViewer.getTree(), encodings, ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION) {
					// subclass to allow typing of language value, if it is the list of iso languages
					protected Object doGetValue() {
						Integer integer =  (Integer) super.doGetValue();
						
						if(integer.intValue() == -1) {
							final String text = ((CCombo)this.getControl()).getText();

							String uppercaseText = text.toUpperCase();
							
							if(TextRecord.UTF8.aliases().contains(uppercaseText)) {
								return new Integer(0);
							}
							
							if(TextRecord.UTF16.aliases().contains(uppercaseText)) {
								return new Integer(1);
							}
							
							// be extra helpful for special utf-16 encodings?
							if(uppercaseText.equals("UTF-16") || uppercaseText.equals("UTF16")) {
								return new Integer(1);
							}
							
							// return entered text for error message
							return text;
						}
						return integer;
					}
				};

			}
			return new TextCellEditor(treeViewer.getTree());
			
		} else {
			return super.getCellEditor(node);
		}
	}
}