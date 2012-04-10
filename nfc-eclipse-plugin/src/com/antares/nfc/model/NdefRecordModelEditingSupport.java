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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Locale;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.nfctools.ndef.NdefConstants;
import org.nfctools.ndef.Record;
import org.nfctools.ndef.auri.AbsoluteUriRecord;
import org.nfctools.ndef.empty.EmptyRecord;
import org.nfctools.ndef.ext.AndroidApplicationRecord;
import org.nfctools.ndef.ext.ExternalTypeRecord;
import org.nfctools.ndef.mime.BinaryMimeRecord;
import org.nfctools.ndef.mime.MimeRecord;
import org.nfctools.ndef.unknown.UnknownRecord;
import org.nfctools.ndef.wkt.records.Action;
import org.nfctools.ndef.wkt.records.ActionRecord;
import org.nfctools.ndef.wkt.records.GcActionRecord;
import org.nfctools.ndef.wkt.records.GcTargetRecord;
import org.nfctools.ndef.wkt.records.GenericControlRecord;
import org.nfctools.ndef.wkt.records.SmartPosterRecord;
import org.nfctools.ndef.wkt.records.TextRecord;
import org.nfctools.ndef.wkt.records.UriRecord;
import org.nfctools.ndef.wkt.records.WellKnownRecord;
import org.nfctools.ndef.wkt.records.handover.AlternativeCarrierRecord;
import org.nfctools.ndef.wkt.records.handover.ErrorRecord;
import org.nfctools.ndef.wkt.records.handover.HandoverCarrierRecord;
import org.nfctools.ndef.wkt.records.handover.HandoverCarrierRecord.CarrierTypeFormat;
import org.nfctools.ndef.wkt.records.handover.HandoverRequestRecord;
import org.nfctools.ndef.wkt.records.handover.HandoverSelectRecord;

import com.antares.nfc.plugin.Activator;
import com.antares.nfc.plugin.NdefModelOperator;

public class NdefRecordModelEditingSupport extends EditingSupport {

	@SuppressWarnings("rawtypes")
	private static Class[] recordTypes = new Class[]{
			AbsoluteUriRecord.class,
			ActionRecord.class,
			AndroidApplicationRecord.class,
			ExternalTypeRecord.class,
			EmptyRecord.class,
			MimeRecord.class,
			SmartPosterRecord.class,
			TextRecord.class,
			UnknownRecord.class,
			UriRecord.class,
			AlternativeCarrierRecord.class,
			HandoverSelectRecord.class,
			HandoverCarrierRecord.class,
			HandoverRequestRecord.class,
			
			GenericControlRecord.class
	};
	
	@SuppressWarnings("rawtypes")
	private static Class[] wellKnownRecordTypes = new Class[]{
			ActionRecord.class,
			SmartPosterRecord.class,
			TextRecord.class,
			UriRecord.class,
			
			AlternativeCarrierRecord.class,
			HandoverSelectRecord.class,
			HandoverCarrierRecord.class,
			HandoverRequestRecord.class,
			
			GenericControlRecord.class
	};
	
	@SuppressWarnings("rawtypes")
	private static Class[] externalRecordTypes = new Class[]{
			AndroidApplicationRecord.class,
			ExternalTypeRecord.class,
	};
	
	@SuppressWarnings("rawtypes")
	private Class[] genericControlRecordTargetRecordTypes = new Class[]{
			TextRecord.class,
			UriRecord.class,
	};

	
	private NdefRecordModelChangeListener listener;
	
	private TextCellEditor textCellEditor;
	private TreeViewer treeViewer;
	
	public NdefRecordModelEditingSupport(TreeViewer viewer, NdefRecordModelChangeListener listener) {
		super(viewer);
		this.listener = listener;
		this.treeViewer = viewer;
		
		this.textCellEditor = new TextCellEditor(viewer.getTree());
	}

	@Override
	protected boolean canEdit(Object element) {
		return element instanceof NdefRecordModelProperty || element instanceof NdefRecordModelRecord || element instanceof NdefRecordModelPropertyListItem || element instanceof NdefRecordModelParentProperty;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		if(element instanceof NdefRecordModelProperty) {
			NdefRecordModelProperty ndefRecordModelProperty = (NdefRecordModelProperty)element;

			NdefRecordModelParent parent = ndefRecordModelProperty.getParent();

			if(parent instanceof NdefRecordModelRecord) {
				NdefRecordModelRecord recordParent = (NdefRecordModelRecord) parent;
				
				Record record = recordParent.getRecord();
				
				if(record instanceof ActionRecord) {
					ActionRecord actionRecord = (ActionRecord)record;
					
					Action[] values = Action.values();
					String[] strings = new String[values.length];
					for(int i = 0; i < values.length; i++) {
						strings[i] = values[i].toString();
					}
					
					return new ComboBoxCellEditor(treeViewer.getTree(), strings);
	
				} else if(record instanceof MimeRecord) {
					if(recordParent.indexOf(ndefRecordModelProperty) == 1) {
						// handle mime media
						
						return new FileDialogCellEditor(treeViewer.getTree());
					}
				} else if(record instanceof TextRecord) {
					if(recordParent.indexOf(ndefRecordModelProperty) == 1) {
						// handle language codes
						
						final String[] isoLanguages = Locale.getISOLanguages();
						
						return new ComboBoxCellEditor(treeViewer.getTree(), isoLanguages) {
							// subclass to allow typing of language value, if it is the list of iso languages
							protected Object doGetValue() {
								Integer integer =  (Integer) super.doGetValue();
								
								if(integer.intValue() == -1) {
									String text = ((CCombo)this.getControl()).getText();
	
									for(int i = 0; i < isoLanguages.length; i++) {
										if(isoLanguages[i].equalsIgnoreCase(text)) {
											return new Integer(i);
										}
									}
									
									// return entered text for error message
									return text;
								}
								return integer;
							}
						};
					} else if(recordParent.indexOf(ndefRecordModelProperty) == 2) {
						// handle encodings, utf-8 or utf-16
						
						final String[] encodings = new String[]{TextRecord.UTF8.name(), TextRecord.UTF16.name()};
						
						return new ComboBoxCellEditor(treeViewer.getTree(), encodings) {
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
				} else if(record instanceof GcActionRecord) {
					GcActionRecord gcActionRecord = (GcActionRecord)record;
					
					Action[] values = Action.values();
					String[] strings = new String[values.length];
					for(int i = 0; i < values.length; i++) {
						strings[i] = values[i].toString();
					}
					
					return new ComboBoxCellEditor(treeViewer.getTree(), strings);
				} else if(record instanceof HandoverCarrierRecord) {
					
					if(recordParent.indexOf(ndefRecordModelProperty) == 0) {
						HandoverCarrierRecord handoverCarrierRecord = (HandoverCarrierRecord)record;
						
						HandoverCarrierRecord.CarrierTypeFormat[] values = HandoverCarrierRecord.CarrierTypeFormat.values();
						String[] strings = new String[values.length];
						for(int i = 0; i < values.length; i++) {
							strings[i] = values[i].toString();
						}
						
						return new ComboBoxCellEditor(treeViewer.getTree(), strings);
					}
				} else if(record instanceof ErrorRecord) {
					if(recordParent.indexOf(ndefRecordModelProperty) == 0) {
		
						ErrorRecord errorRecord = (ErrorRecord)record;
						
						ErrorRecord.ErrorReason[] values = ErrorRecord.ErrorReason.values();
						String[] strings = new String[values.length];
						for(int i = 0; i < values.length; i++) {
							strings[i] = values[i].toString();
						}
						
						return new ComboBoxCellEditor(treeViewer.getTree(), strings);
					}
				} else if(record instanceof AlternativeCarrierRecord) {
					if(recordParent.indexOf(ndefRecordModelProperty) == 0) {
		
						AlternativeCarrierRecord alternativeCarrierRecord = (AlternativeCarrierRecord)record;
						
						AlternativeCarrierRecord.CarrierPowerState[] values = AlternativeCarrierRecord.CarrierPowerState.values();
						String[] strings = new String[values.length];
						for(int i = 0; i < values.length; i++) {
							strings[i] = values[i].toString();
						}
						
						return new ComboBoxCellEditor(treeViewer.getTree(), strings);
					}
				} else if(record instanceof UnknownRecord) {
					return new FileDialogCellEditor(treeViewer.getTree());
				}
			}
		} else if(element instanceof NdefRecordModelParentProperty) {
			NdefRecordModelParentProperty ndefRecordModelParentProperty = (NdefRecordModelParentProperty)element;
			
			NdefRecordModelRecord parent = (NdefRecordModelRecord) ndefRecordModelParentProperty.getParent();
			
			Record record = parent.getRecord();
			
			if(record instanceof GcTargetRecord) {
				GcTargetRecord gcTargetRecord = (GcTargetRecord)record;
				if(parent.indexOf(ndefRecordModelParentProperty) == 0) {
					
					String[] strings = new String[genericControlRecordTargetRecordTypes.length];
					for(int i = 0; i < genericControlRecordTargetRecordTypes.length; i++) {
						strings[i] = genericControlRecordTargetRecordTypes[i].getSimpleName();
					}
					return new ComboBoxCellEditor(treeViewer.getTree(), strings);

				}
			} else if(record instanceof GcActionRecord) {
				GcActionRecord gcActionRecord = (GcActionRecord)record;
				
				if(parent.indexOf(ndefRecordModelParentProperty) == 1) {
					
					String[] strings = new String[recordTypes.length];
					for(int i = 0; i < recordTypes.length; i++) {
						strings[i] = recordTypes[i].getSimpleName();
					}
					return new ComboBoxCellEditor(treeViewer.getTree(), strings);

				}
			} else if(record instanceof HandoverCarrierRecord) {
				HandoverCarrierRecord handoverCarrierRecord = (HandoverCarrierRecord)record;

				if(parent.indexOf(ndefRecordModelParentProperty) == 1) {
					
					CarrierTypeFormat carrierTypeFormat = handoverCarrierRecord.getCarrierTypeFormat();
					if(carrierTypeFormat != null) {
						switch(carrierTypeFormat) {
							case WellKnown : {
								// NFC Forum well-known type [NFC RTD]

								String[] strings = new String[wellKnownRecordTypes.length];
								for(int i = 0; i < wellKnownRecordTypes.length; i++) {
									strings[i] = wellKnownRecordTypes[i].getSimpleName();
								}
								return new ComboBoxCellEditor(treeViewer.getTree(), strings);
							}
						
							case External : {
								// NFC Forum external type [NFC RTD]
								String[] strings = new String[externalRecordTypes.length];
								for(int i = 0; i < externalRecordTypes.length; i++) {
									strings[i] = externalRecordTypes[i].getSimpleName();
								}
								return new ComboBoxCellEditor(treeViewer.getTree(), strings);
							}
						}
						
					}
					return new ComboBoxCellEditor(treeViewer.getTree(), new String[]{});
				}
			}
			
			
		}

		return textCellEditor;
	}

	@Override
	protected Object getValue(Object element) {
		Activator.info("Get element " + element + " value");

		if(element instanceof NdefRecordModelProperty) {
			NdefRecordModelProperty ndefRecordModelProperty = (NdefRecordModelProperty)element;
			
			NdefRecordModelParent parent = ndefRecordModelProperty.getParent();
			
			if(parent instanceof NdefRecordModelRecord) {
				NdefRecordModelRecord recordParent = (NdefRecordModelRecord) parent;
				
				Record record = recordParent.getRecord();
				
				if(record instanceof ActionRecord) {
					ActionRecord actionRecord = (ActionRecord)record;
					
					if(actionRecord.hasAction()) {
						Action[] values = Action.values();
						for(int i = 0; i < values.length; i++) {
							if(values[i] == actionRecord.getAction()) {
								return new Integer(i);
							}
						}
					}
					return new Integer(-1);
				} else if(record instanceof MimeRecord) {
					if(recordParent.indexOf(ndefRecordModelProperty) == 1) {
						// handle mime media
	
					}
				} else if(record instanceof TextRecord) {
					if(recordParent.indexOf(ndefRecordModelProperty) == 1) {
						// handle language
						TextRecord textRecord = (TextRecord)record;
	
						String language = textRecord.getLocale().getLanguage();
						String[] values = Locale.getISOLanguages();
						for(int i = 0; i < values.length; i++) {
							if(values[i].equals(language)) {
								return new Integer(i);
							}
						}
						throw new IllegalArgumentException("Unknown language " + textRecord.getLocale().getLanguage());
					} else if(recordParent.indexOf(ndefRecordModelProperty) == 2) {
						// handle encodings, utf-8 or utf-16
						
						TextRecord textRecord = (TextRecord)record;
	
						Charset encoding = textRecord.getEncoding();
						if(encoding == TextRecord.UTF8 || TextRecord.UTF8.aliases().contains(encoding.name())) {
							return 0;
						} else if(encoding == TextRecord.UTF16 || TextRecord.UTF16.aliases().contains(encoding.name())) {
							return 1;
						}
						
						throw new IllegalArgumentException("Illegal encoding " + encoding);
	
					}
				} else if(record instanceof GcActionRecord) {
					GcActionRecord gcActionRecord = (GcActionRecord)record;
					
					if(gcActionRecord.hasAction()) {
						Action[] values = Action.values();
						for(int i = 0; i < values.length; i++) {
							if(values[i] == gcActionRecord.getAction()) {
								return new Integer(i);
							}
						}
					}
					return new Integer(-1);
				} else if(record instanceof HandoverCarrierRecord) {
					
					if(recordParent.indexOf(ndefRecordModelProperty) == 0) {
						HandoverCarrierRecord handoverCarrierRecord = (HandoverCarrierRecord)record;
						
						if(handoverCarrierRecord.hasCarrierTypeFormat()) {
							HandoverCarrierRecord.CarrierTypeFormat[] values = HandoverCarrierRecord.CarrierTypeFormat.values();
							for(int i = 0; i < values.length; i++) {
								if(values[i] == handoverCarrierRecord.getCarrierTypeFormat()) {
									return new Integer(i);
								}
							}
						}
						return new Integer(-1);
					}
				} else if(record instanceof ErrorRecord) {
					if(recordParent.indexOf(ndefRecordModelProperty) == 0) {
		
						ErrorRecord errorRecord = (ErrorRecord)record;
						
						if(errorRecord.hasErrorReason()) {
							ErrorRecord.ErrorReason[] values = ErrorRecord.ErrorReason.values();
							for(int i = 0; i < values.length; i++) {
								if(values[i] == errorRecord.getErrorReason()) {
									return new Integer(i);
								}
							}
						}
						return new Integer(-1);
					}
				} else if(record instanceof AlternativeCarrierRecord) {
					if(recordParent.indexOf(ndefRecordModelProperty) == 0) {
		
						AlternativeCarrierRecord alternativeCarrierRecord = (AlternativeCarrierRecord)record;
						
						if(alternativeCarrierRecord.hasCarrierPowerState()) {
							AlternativeCarrierRecord.CarrierPowerState[] values = AlternativeCarrierRecord.CarrierPowerState.values();
							for(int i = 0; i < values.length; i++) {
								if(values[i] == alternativeCarrierRecord.getCarrierPowerState()) {
									return new Integer(i);
								}
							}
						}
						
						return new Integer(-1);
					}
				}
			}
			// default to empty value if no value
			return ndefRecordModelProperty.getValue();
		} else if(element instanceof NdefRecordModelRecord) {
			NdefRecordModelRecord ndefRecordModelRecord = (NdefRecordModelRecord)element;
			
			Record record = ndefRecordModelRecord.getRecord();
			
			return record.getKey();
		} else if(element instanceof NdefRecordModelParentProperty) {
			NdefRecordModelParentProperty ndefRecordModelParentProperty = (NdefRecordModelParentProperty)element;
			
			NdefRecordModelRecord parent = (NdefRecordModelRecord) ndefRecordModelParentProperty.getParent();
			
			Record record = parent.getRecord();
			
			if(record instanceof GcTargetRecord) {
				GcTargetRecord gcTargetRecord = (GcTargetRecord)record;
				if(parent.indexOf(ndefRecordModelParentProperty) == 0) {
					
					if(gcTargetRecord.hasTargetIdentifier()) {
						for(int i = 0; i < genericControlRecordTargetRecordTypes.length; i++) {
							if(gcTargetRecord.getTargetIdentifier().getClass() ==  genericControlRecordTargetRecordTypes[i]) {
								return i;
							}
						}
					}
					return -1;
				}
			} else if(record instanceof GcActionRecord) {
				GcActionRecord gcActionRecord = (GcActionRecord)record;
				
				if(parent.indexOf(ndefRecordModelParentProperty) == 1) {
					if(gcActionRecord.hasActionRecord()) {
						for(int i = 0; i < recordTypes.length; i++) {
							if(gcActionRecord.getActionRecord().getClass() ==  recordTypes[i]) {
								return i;
							}
						}
					}
					return -1;
				}
				
			} else if(record instanceof HandoverCarrierRecord) {
				HandoverCarrierRecord handoverCarrierRecord = (HandoverCarrierRecord)record;

				if(parent.indexOf(ndefRecordModelParentProperty) == 1) {
					
					CarrierTypeFormat carrierTypeFormat = handoverCarrierRecord.getCarrierTypeFormat();
					if(carrierTypeFormat != null) {
						
						Object carrierType = handoverCarrierRecord.getCarrierType();
						if(carrierType != null) {
							switch(carrierTypeFormat) {
								case WellKnown : {
									// NFC Forum well-known type [NFC RTD]
	
									for(int i = 0; i < wellKnownRecordTypes.length; i++) {
										if(carrierType.getClass() ==  wellKnownRecordTypes[i]) {
											return i;
										}
									}
								}
							
								case External : {
									// NFC Forum external type [NFC RTD]
									for(int i = 0; i < externalRecordTypes.length; i++) {
										if(carrierType.getClass() ==  externalRecordTypes[i]) {
											return i;
										}
									}
								}
							}
						}
							
					}
					return -1;
				}
			}
			
		}
		
		return element.toString();
	}

	@Override
	protected void setValue(Object element, Object value) {
		Activator.info("Set element " + element + " value " + value + ", currently have " + getValue(element));


		if(element instanceof NdefRecordModelNode) {
			boolean change = false;

			NdefRecordModelNode ndefRecordModelNode = (NdefRecordModelNode)element;
			
			if(element instanceof NdefRecordModelProperty) {
				NdefRecordModelProperty ndefRecordModelProperty = (NdefRecordModelProperty)element;

				NdefRecordModelParent parent = ndefRecordModelNode.getParent();
				
				if(parent instanceof NdefRecordModelRecord) {
					NdefRecordModelRecord recordParent = (NdefRecordModelRecord)parent;
	
					Record record = recordParent.getRecord();
					
					if(record instanceof ActionRecord) {
						ActionRecord actionRecord = (ActionRecord)record;
						
						Integer index = (Integer)value;
						
						Action action;
						if(index.intValue() != -1) {
							Action[] values = Action.values();
						
							action = values[index.intValue()];
						} else {
							action = null;
						}
						if(action != actionRecord.getAction()) {
							actionRecord.setAction(action);
		
							// update property as well
							if(actionRecord.hasAction()) {
								ndefRecordModelProperty.setValue(actionRecord.getAction().name());
							} else {
								ndefRecordModelProperty.setValue(null);
							}
							change = true;
						}
					} else if(record instanceof TextRecord) {
						// handle language
						TextRecord textRecord = (TextRecord)record;
						
						int propertyIndex  = recordParent.indexOf(ndefRecordModelProperty);
						if(propertyIndex == 0) {
							String stringValue = (String)value;
							
							if(!stringValue.equals(textRecord.getText())) {
								textRecord.setText(stringValue);
								
								ndefRecordModelProperty.setValue(textRecord.getText());
								
								change = true;
							}
						} else if(propertyIndex == 1) {
							if(value instanceof Integer) {
								Integer index = (Integer)value;
			
								if(index.intValue() != -1) {
									String[] values = Locale.getISOLanguages();
				
									Locale locale = new Locale(values[index.intValue()]);
									if(!locale.equals(textRecord.getLocale())) {
										textRecord.setLocale(locale);
					
										ndefRecordModelProperty.setValue(textRecord.getLocale().getLanguage());
										
										change = true;
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
												MessageDialog.openError(shell, "Error", "Illegal language '" + stringValue + "', must be ISO language code.");
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
										textRecord.setEncoding(charset);
										
										ndefRecordModelProperty.setValue(textRecord.getEncoding().displayName());
									
										change = true;
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
					} else if(record instanceof AndroidApplicationRecord) {
						AndroidApplicationRecord androidApplicationRecord = (AndroidApplicationRecord)record;
						
						String stringValue = (String)value;
						if(!stringValue.equals(androidApplicationRecord.getPackageName())) {
							androidApplicationRecord.setPackageName(stringValue);
							
							ndefRecordModelProperty.setValue(androidApplicationRecord.getPackageName());
						
							change = true;
						}
					} else if(record instanceof ExternalTypeRecord) {
						ExternalTypeRecord externalTypeRecord = (ExternalTypeRecord)record;
						
						int propertyIndex  = recordParent.indexOf(ndefRecordModelProperty);
						if(propertyIndex == 0) {
							String stringValue = (String)value;
							if(!stringValue.equals(externalTypeRecord.getNamespace())) {
								externalTypeRecord.setNamespace(stringValue);
								
								ndefRecordModelProperty.setValue(externalTypeRecord.getNamespace());
							
								change = true;
							}
						} else if(propertyIndex == 1) {
							String stringValue = (String)value;
							if(!stringValue.equals(externalTypeRecord.getContent())) {
								externalTypeRecord.setContent(stringValue);
								
								ndefRecordModelProperty.setValue(externalTypeRecord.getContent());
							
								change = true;
							}
						}
					} else if(record instanceof MimeRecord) {
						
						MimeRecord mimeMediaRecord = (MimeRecord)record;
						
						int propertyIndex  = recordParent.indexOf(ndefRecordModelProperty);
						if(propertyIndex == 0) {
							String stringValue = (String)value;
							
							if(!stringValue.equals(mimeMediaRecord.getContentType())) {
								mimeMediaRecord.setContentType(stringValue);
								
								ndefRecordModelProperty.setValue(mimeMediaRecord.getContentType());
							
								change = true;
							}
						} else if(propertyIndex == 1) {
							
							if(mimeMediaRecord instanceof BinaryMimeRecord) {
								BinaryMimeRecord binaryMimeRecord = (BinaryMimeRecord)mimeMediaRecord;
								if(value != null) {
								
									String path = (String)value;
									
									File file = new File(path);
				
									int length = (int)file.length();
									
									byte[] payload = new byte[length];
									
									InputStream in = null;
									try {
										in = new FileInputStream(file);
										DataInputStream din = new DataInputStream(in);
										
										din.readFully(payload);
										
										binaryMimeRecord.setContent(payload);
										
										ndefRecordModelProperty.setValue(Integer.toString(length) + " bytes binary payload");
				
										change = true;
									} catch(IOException e) {
										Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
										MessageDialog.openError(shell, "Error", "Could not read file '" + file + "', reverting to previous value.");
									} finally {
										if(in != null) {
											try {
												in.close();
											} catch(IOException e) {
												// ignore
											}
										}
									}
								}
							} else {
								throw new RuntimeException();
							}
						}
					} else if(record instanceof UriRecord) {
						UriRecord uriRecord = (UriRecord)record;
						
						String stringValue = (String)value;
						
						if(!stringValue.equals(uriRecord.getUri())) {
							uriRecord.setUri(stringValue);
								
							ndefRecordModelProperty.setValue(uriRecord.getUri());
							
							change = true;
						}
					} else if(record instanceof AbsoluteUriRecord) {
						AbsoluteUriRecord uriRecord = (AbsoluteUriRecord)record;
						
						String stringValue = (String)value;
						if(!stringValue.equals(uriRecord.getUri())) {
							uriRecord.setUri(stringValue);
								
							ndefRecordModelProperty.setValue(uriRecord.getUri());
						
							change = true;
						}
					} else if(record instanceof GcActionRecord) {
						GcActionRecord gcActionRecord = (GcActionRecord)record;
							
						Integer index = (Integer)value;
	
						Action action;
						if(index.intValue() != -1) {
							Action[] values = Action.values();
							action = values[index.intValue()];
						} else {
							action = null;
						}
	
						if(action != gcActionRecord.getAction()) {
							gcActionRecord.setAction(action);
		
							// update property as well
							if(gcActionRecord.hasAction()) {
								ndefRecordModelProperty.setValue(gcActionRecord.getAction().name());
							} else {
								ndefRecordModelProperty.setValue(null);
							}
							change = true;
						}
					} else if(record instanceof GenericControlRecord) {
						GenericControlRecord genericControlRecord = (GenericControlRecord)record;
		
						String stringValue = (String)value;
		
						try {
							byte b = Byte.parseByte(stringValue);
							
							if(b != genericControlRecord.getConfigurationByte()) {
								genericControlRecord.setConfigurationByte(b);
								
								// update property as well
								ndefRecordModelProperty.setValue(Byte.toString(genericControlRecord.getConfigurationByte()));
		
								change = true;
							}
						} catch(Exception e) {
							Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
							MessageDialog.openError(shell, "Error", "Could not set value '" + stringValue + "', reverting to previous value.");
						}
					} else if(record instanceof HandoverCarrierRecord) {
						HandoverCarrierRecord handoverCarrierRecord = (HandoverCarrierRecord)record;
						
						if(recordParent.indexOf(ndefRecordModelProperty) == 0) {
							
							Integer index = (Integer)value;
			
							HandoverCarrierRecord.CarrierTypeFormat carrierTypeFormat;
							if(index.intValue() != -1) {
								HandoverCarrierRecord.CarrierTypeFormat[] values = HandoverCarrierRecord.CarrierTypeFormat.values();
								carrierTypeFormat = values[index.intValue()];
							} else {
								carrierTypeFormat = null;
							}
							
							if(carrierTypeFormat != handoverCarrierRecord.getCarrierTypeFormat()) {
								handoverCarrierRecord.setCarrierTypeFormat(carrierTypeFormat);
			
								// update property as well
								if(handoverCarrierRecord.hasCarrierTypeFormat()) {
									ndefRecordModelProperty.setValue(handoverCarrierRecord.getCarrierTypeFormat().name());
								} else {
									ndefRecordModelProperty.setValue(null);
								}
	
								if(carrierTypeFormat != null) {
									switch(carrierTypeFormat) {
									case WellKnown : {
										// NFC Forum well-known type [NFC RTD]
		
										// clear type, select sub type later
										listener.set((NdefRecordModelParentProperty)recordParent.getChild(1), null);
		
										break;
									}
									case Media : {
										
										// Media-type as defined in RFC 2046 [RFC 2046]
										listener.set((NdefRecordModelParentProperty)recordParent.getChild(1), String.class);
		
										break;
									}
									case AbsoluteURI : {
										// Absolute URI as defined in RFC 3986 [RFC 3986]
										listener.set((NdefRecordModelParentProperty)recordParent.getChild(1), String.class);
		
										break;
									}
									case External : {
										// NFC Forum external type [NFC RTD]
										listener.set((NdefRecordModelParentProperty)recordParent.getChild(1), null);
		
										break;
									}
									default: {
										throw new RuntimeException();
									}
		
									}		
								} else {
									listener.set((NdefRecordModelParentProperty)recordParent.getChild(1), null);
								}
								
								change = true;
							}
						}
					} else if(record instanceof ErrorRecord) {
						if(recordParent.indexOf(ndefRecordModelProperty) == 0) {
			
							ErrorRecord errorRecord = (ErrorRecord)record;
							
							
							Integer index = (Integer)value;
			
							ErrorRecord.ErrorReason errorReason;
							if(index.intValue() != -1) {
								ErrorRecord.ErrorReason[] values = ErrorRecord.ErrorReason.values();
								errorReason = values[index.intValue()];
							} else {
								errorReason = null;
							}
							if(errorReason !=  errorRecord.getErrorReason()) {
								errorRecord.setErrorReason(errorReason);
			
								// update property as well
								if(errorRecord.hasErrorReason()) {
									ndefRecordModelProperty.setValue(errorRecord.getErrorReason().name());
								} else {
									ndefRecordModelProperty.setValue(null);
								}
								change = true;
							}
						}
					} else if(record instanceof AlternativeCarrierRecord) {
						AlternativeCarrierRecord alternativeCarrierRecord = (AlternativeCarrierRecord)record;
						if(recordParent.indexOf(ndefRecordModelProperty) == 0) {
									
							Integer index = (Integer)value;
			
							AlternativeCarrierRecord.CarrierPowerState carrierPowerState;
							if(index.intValue() != -1) {
								AlternativeCarrierRecord.CarrierPowerState[] values = AlternativeCarrierRecord.CarrierPowerState.values();
								carrierPowerState = values[index.intValue()];
							} else {
								carrierPowerState = null;
							}
							
							if(carrierPowerState !=  alternativeCarrierRecord.getCarrierPowerState()) {
								alternativeCarrierRecord.setCarrierPowerState(carrierPowerState);
			
								// update property as well
								if(alternativeCarrierRecord.hasCarrierPowerState()) {
									ndefRecordModelProperty.setValue(alternativeCarrierRecord.getCarrierPowerState().name());
								} else {
									ndefRecordModelProperty.setValue(null);
								}
								change = true;
							}
						
						} else if(recordParent.indexOf(ndefRecordModelProperty) == 1) {
							String stringValue = (String)value;
							
							String carrierDataReference = alternativeCarrierRecord.getCarrierDataReference();
							
							if(!stringValue.equals(carrierDataReference)) {
								alternativeCarrierRecord.setCarrierDataReference(stringValue);
									
								// update property as well
								if(alternativeCarrierRecord.hasCarrierDataReference()) {
									ndefRecordModelProperty.setValue(alternativeCarrierRecord.getCarrierDataReference());
								} else {
									ndefRecordModelProperty.setValue("");
								}
								change = true;
							}
						}
					} else if(record instanceof HandoverRequestRecord) {
						HandoverRequestRecord handoverRequestRecord = (HandoverRequestRecord)record;
						if(recordParent.indexOf(ndefRecordModelProperty) == 0) {
							String stringValue = (String)value;
	
							try {
								byte byteValue = Byte.parseByte(stringValue);
								if(byteValue < 0) {
									throw new NumberFormatException();
								}
								if(byteValue != handoverRequestRecord.getMajorVersion()) {
									handoverRequestRecord.setMajorVersion(byteValue);
									
									// update property as well
									ndefRecordModelProperty.setValue(Byte.toString(byteValue));
									change = true;
								}
							} catch(Exception e) {
								Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
								MessageDialog.openError(shell, "Error", "Could not set value '" + stringValue + "', reverting to previous value.");
							}
	
						} else if(recordParent.indexOf(ndefRecordModelProperty) == 1) {
							
							String stringValue = (String)value;
	
							try {
								byte byteValue = Byte.parseByte(stringValue);
								if(byteValue < 0) {
									throw new NumberFormatException();
								}
								if(byteValue != handoverRequestRecord.getMinorVersion()) {
									handoverRequestRecord.setMinorVersion(byteValue);
									
									// update property as well
									ndefRecordModelProperty.setValue(Byte.toString(byteValue));
									change = true;
								}
							} catch(Exception e) {
								Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
								MessageDialog.openError(shell, "Error", "Could not set value '" + stringValue + "', reverting to previous value.");
							}
						}
	
					} else if(record instanceof HandoverSelectRecord) {
						HandoverSelectRecord handoverSelectRecord = (HandoverSelectRecord)record;
						if(recordParent.indexOf(ndefRecordModelProperty) == 0) {
							String stringValue = (String)value;
	
							try {
								byte byteValue = Byte.parseByte(stringValue);
								if(byteValue < 0) {
									throw new NumberFormatException();
								}
								if(byteValue != handoverSelectRecord.getMajorVersion()) {
									handoverSelectRecord.setMajorVersion(byteValue);
									
									// update property as well
									ndefRecordModelProperty.setValue(Byte.toString(byteValue));
									change = true;
								}
							} catch(Exception e) {
								Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
								MessageDialog.openError(shell, "Error", "Could not set value '" + stringValue + "', reverting to previous value.");
							}
	
						} else if(recordParent.indexOf(ndefRecordModelProperty) == 1) {
							
							String stringValue = (String)value;
	
							try {
								byte byteValue = Byte.parseByte(stringValue);
								if(byteValue < 0) {
									throw new NumberFormatException();
								}
								if(byteValue != handoverSelectRecord.getMinorVersion()) {
									handoverSelectRecord.setMinorVersion(byteValue);
									
									// update property as well
									ndefRecordModelProperty.setValue(Byte.toString(byteValue));
									change = true;
								}
							} catch(Exception e) {
								Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
								MessageDialog.openError(shell, "Error", "Could not set value '" + stringValue + "', reverting to previous value.");
							}
						}
						
					} else if(record instanceof UnknownRecord) {
						UnknownRecord unknownRecord = (UnknownRecord)record;
						
						if(value != null) {
						
							String path = (String)value;
							
							File file = new File(path);
	
							int length = (int)file.length();
							
							byte[] payload = new byte[length];
							
							InputStream in = null;
							try {
								in = new FileInputStream(file);
								DataInputStream din = new DataInputStream(in);
								
								din.readFully(payload);
								
								unknownRecord.setPayload(payload);
								
								ndefRecordModelProperty.setValue(Integer.toString(length) + " bytes payload");
	
								change = true;
							} catch(IOException e) {
								Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
								MessageDialog.openError(shell, "Error", "Could not read file '" + file + "', reverting to previous value.");
							} finally {
								if(in != null) {
									try {
										in.close();
									} catch(IOException e) {
										// ignore
									}
								}
							}
						}
					}
				} else if(parent instanceof NdefRecordModelParentProperty) {
					NdefRecordModelRecord recordParent = (NdefRecordModelRecord) parent.getParent();
					
					Record record = recordParent.getRecord();
					
					if(record instanceof HandoverCarrierRecord) {
						HandoverCarrierRecord handoverCarrierRecord = (HandoverCarrierRecord)record;

						String stringValue = (String)value;
						
						if(!stringValue.equals(handoverCarrierRecord.getCarrierType())) {
							handoverCarrierRecord.setCarrierType(stringValue);
								
							ndefRecordModelProperty.setValue(handoverCarrierRecord.getCarrierType().toString());

							change = true;
						}
					}
				}
			} else if(element instanceof NdefRecordModelRecord) {
				NdefRecordModelRecord ndefRecordModelRecord = (NdefRecordModelRecord)element;
				
				Record record = ndefRecordModelRecord.getRecord();
				
				String stringValue = (String)value;
				
				if(!stringValue.equals(record.getKey())) {
					record.setKey(stringValue);
						
					change = true;
				}

			} else if(element instanceof NdefRecordModelPropertyListItem) {
				NdefRecordModelPropertyListItem ndefRecordModelPropertyListItem = (NdefRecordModelPropertyListItem)element;
				
				NdefRecordModelPropertyList parent = (NdefRecordModelPropertyList) ndefRecordModelPropertyListItem.getParent();

				NdefRecordModelRecord ndefRecordModelRecord = (NdefRecordModelRecord)parent.getParent();

				Record record = ndefRecordModelRecord.getRecord();
				
				if(record instanceof AlternativeCarrierRecord) {
					AlternativeCarrierRecord alternativeCarrierRecord = (AlternativeCarrierRecord)record;
					
					String stringValue = (String)value;
					int index = ndefRecordModelPropertyListItem.getParentIndex();

					String auxiliaryDataReference = alternativeCarrierRecord.getAuxiliaryDataReferenceAt(index);
					
					if(!stringValue.equals(auxiliaryDataReference)) {
						alternativeCarrierRecord.setAuxiliaryDataReferenceAt(index, stringValue);
							
						// update list value
						
						ndefRecordModelPropertyListItem.setValue(stringValue);
						
						change = true;
					}

				}
			} else if(element instanceof NdefRecordModelParentProperty) {
				NdefRecordModelParentProperty ndefRecordModelParentProperty = (NdefRecordModelParentProperty)element;
				
				NdefRecordModelRecord parent = (NdefRecordModelRecord) ndefRecordModelParentProperty.getParent();
				
				Record record = parent.getRecord();
				
				if(record instanceof GcTargetRecord) {
					GcTargetRecord gcTargetRecord = (GcTargetRecord)record;
					if(parent.indexOf(ndefRecordModelParentProperty) == 0) {
						
						Integer index = (Integer)value;

						if(index.intValue() != -1) {
							int previousIndex = -1;
							if(gcTargetRecord.hasTargetIdentifier()) {
								for(int i = 0; i < genericControlRecordTargetRecordTypes.length; i++) {
									if(gcTargetRecord.getTargetIdentifier().getClass() == genericControlRecordTargetRecordTypes[i]) {
										previousIndex = i;
									}
								}
							}

							if(previousIndex != index.intValue()) {
								listener.set(ndefRecordModelParentProperty, genericControlRecordTargetRecordTypes[index]);
								
								treeViewer.update(ndefRecordModelParentProperty, null);

								change = true;
							}
							
						}						
					}
				} else if(record instanceof GcActionRecord) {
					GcActionRecord gcActionRecord = (GcActionRecord)record;

					Integer index = (Integer)value;

					if(index.intValue() != -1) {
						int previousIndex = -1;
						if(gcActionRecord.hasActionRecord()) {
							for(int i = 0; i < recordTypes.length; i++) {
								if(gcActionRecord.getActionRecord().getClass() == recordTypes[i]) {
									previousIndex = i;
								}
							}
						}

						if(previousIndex != index.intValue()) {
							listener.set(ndefRecordModelParentProperty, recordTypes[index]);
							
							treeViewer.update(ndefRecordModelParentProperty, null);

							change = true;
						}
						
					}						
					
				} else if(record instanceof HandoverCarrierRecord) {
					HandoverCarrierRecord handoverCarrierRecord = (HandoverCarrierRecord)record;
					if(ndefRecordModelParentProperty.getParentIndex() == 1) {
						Integer index = (Integer)value;
		
						if(index.intValue() != -1) {
	
							if(handoverCarrierRecord.hasCarrierTypeFormat()) {
								HandoverCarrierRecord.CarrierTypeFormat carrierTypeFormat = handoverCarrierRecord.getCarrierTypeFormat();
							
								Class[] types;
								switch(carrierTypeFormat) {
									case WellKnown : {
										// NFC Forum well-known type [NFC RTD]
										types = wellKnownRecordTypes;
										break;
									}
									
									case External : {
										// NFC Forum external type [NFC RTD]
										types = externalRecordTypes;
										
										break;
									}
									
									default : {
										throw new RuntimeException();
									}
								}
								
								int previousIndex = -1;
								if(handoverCarrierRecord.hasCarrierType()) {
									Class c = handoverCarrierRecord.getCarrierType().getClass();
									
									for(int i = 0; i < types.length; i++) {
										if(c ==  types[i]) {
											previousIndex = i;
											
											break;
										}
									}

								}
								
								if(index.intValue() != previousIndex) {
									listener.set(ndefRecordModelParentProperty, types[index]);

									change = true;
								}

								
							}
						}
					}
					
					
					
				}
				
				
			}
			
			if(change) {
				Activator.info("Model change");
				if(listener != null) {
					// find root
					NdefRecordModelParent p = ndefRecordModelNode.getParent();
					while(p.hasParent()) {
						p = p.getParent();
					}
					// notify listener
					listener.update(p);
				}			
	
				// update all but the root node
				NdefRecordModelNode node = (NdefRecordModelNode) element;
	
				do {
					treeViewer.update(node, null);
					
					node = node.getParent();
				} while(node != null && node.hasParent());
			}	
		}
		
	}
}