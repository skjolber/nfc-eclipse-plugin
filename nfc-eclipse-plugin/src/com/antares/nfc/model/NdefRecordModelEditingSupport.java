package com.antares.nfc.model;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Locale;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.nfctools.ndef.Record;
import org.nfctools.ndef.auri.AbsoluteUriRecord;
import org.nfctools.ndef.ext.AndroidApplicationRecord;
import org.nfctools.ndef.ext.ExternalTypeRecord;
import org.nfctools.ndef.mime.BinaryMimeRecord;
import org.nfctools.ndef.mime.MimeRecord;
import org.nfctools.ndef.wkt.records.Action;
import org.nfctools.ndef.wkt.records.ActionRecord;
import org.nfctools.ndef.wkt.records.GcActionRecord;
import org.nfctools.ndef.wkt.records.GenericControlRecord;
import org.nfctools.ndef.wkt.records.TextRecord;
import org.nfctools.ndef.wkt.records.UriRecord;

import com.antares.nfc.plugin.Activator;

public class NdefRecordModelEditingSupport extends EditingSupport {

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
		return element instanceof NdefRecordModelProperty;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		if(element instanceof NdefRecordModelProperty) {
			NdefRecordModelProperty ndefRecordModelProperty = (NdefRecordModelProperty)element;
			
			NdefRecordModelRecord parent = (NdefRecordModelRecord) ndefRecordModelProperty.getParent();
			
			Record record = parent.getRecord();
			
			if(record instanceof ActionRecord) {
				ActionRecord actionRecord = (ActionRecord)record;
				
				Action[] values = Action.values();
				String[] strings = new String[values.length];
				for(int i = 0; i < values.length; i++) {
					strings[i] = values[i].toString();
				}
				
				return new ComboBoxCellEditor(treeViewer.getTree(), strings);

			} else if(record instanceof MimeRecord) {
				if(parent.indexOf(ndefRecordModelProperty) == 1) {
					// handle mime media
					
					return new FileDialogCellEditor(treeViewer.getTree());
				}
			} else if(record instanceof TextRecord) {
				if(parent.indexOf(ndefRecordModelProperty) == 1) {
					// handle language codes

					return new ComboBoxCellEditor(treeViewer.getTree(), Locale.getISOLanguages());
				}
			} else if(record instanceof GcActionRecord) {
				GcActionRecord gcActionRecord = (GcActionRecord)record;
				
				Action[] values = Action.values();
				String[] strings = new String[values.length];
				for(int i = 0; i < values.length; i++) {
					strings[i] = values[i].toString();
				}
				
				return new ComboBoxCellEditor(treeViewer.getTree(), strings);

			}
		}
		return textCellEditor;
	}

	@Override
	protected Object getValue(Object element) {
		Activator.info("Get element " + element + " value");

		if(element instanceof NdefRecordModelProperty) {
			NdefRecordModelProperty ndefRecordModelProperty = (NdefRecordModelProperty)element;
			
			NdefRecordModelRecord parent = (NdefRecordModelRecord) ndefRecordModelProperty.getParent();
			
			Record record = parent.getRecord();
			
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
				throw new IllegalArgumentException();
			} else if(record instanceof MimeRecord) {
				if(parent.indexOf(ndefRecordModelProperty) == 1) {
					// handle mime media

				}
			} else if(record instanceof TextRecord) {
				if(parent.indexOf(ndefRecordModelProperty) == 1) {
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
				throw new IllegalArgumentException();
			}
			// default to empty value if no value
			return ndefRecordModelProperty.getValue();
		}
		
		return element.toString();
	}

	@Override
	protected void setValue(Object element, Object value) {
		Activator.info("Set element " + element + " value " + value + ", currently have " + getValue(element));

		if(element instanceof NdefRecordModelProperty) {
			NdefRecordModelProperty ndefRecordModelProperty = (NdefRecordModelProperty)element;
			
			NdefRecordModelRecord parent = (NdefRecordModelRecord) ndefRecordModelProperty.getParent();
			
			Record record = parent.getRecord();
			
			boolean change = false;
			
			if(record instanceof ActionRecord) {
				ActionRecord actionRecord = (ActionRecord)record;
				
				Action[] values = Action.values();
				
				Integer index = (Integer)value;
				
				Action action = values[index.intValue()];
				
				if(action != actionRecord.getAction()) {
					actionRecord.setAction(action);

					// update property as well
					ndefRecordModelProperty.setValue(actionRecord.getAction().name());
					
					change = true;
				}
			} else if(record instanceof TextRecord) {
				// handle language
				TextRecord textRecord = (TextRecord)record;
				
				int propertyIndex  = parent.indexOf(ndefRecordModelProperty);
				if(propertyIndex == 0) {
					String stringValue = (String)value;
					
					if(!stringValue.equals(textRecord.getText())) {
						textRecord.setText(stringValue);
						
						ndefRecordModelProperty.setValue(textRecord.getText());
						
						change = true;
					}
				} else if(propertyIndex == 1) {
					Integer index = (Integer)value;

					String[] values = Locale.getISOLanguages();

					Locale locale = new Locale(values[index.intValue()]);
					if(!locale.equals(textRecord.getLocale())) {
						textRecord.setLocale(locale);
	
						ndefRecordModelProperty.setValue(textRecord.getLocale().getLanguage());
						
						change = true;
					}

				} else if(propertyIndex == 2) {
					String stringValue = (String)value;
					try {
						Charset charset = Charset.forName(stringValue);
						
						if(!charset.equals(textRecord.getEncoding())) {
							textRecord.setEncoding(charset);
							
							ndefRecordModelProperty.setValue(textRecord.getEncoding().displayName());
						
							change = true;
						}
					} catch(UnsupportedCharsetException e) {
						// http://www.vogella.de/articles/EclipseDialogs/article.html#dialogs_jfacemessage
						
						Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
						MessageDialog.openError(shell, "Error", "Unknown encoding '" + stringValue + "', reverting to previous value.");
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
				
				int propertyIndex  = parent.indexOf(ndefRecordModelProperty);
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
				
				int propertyIndex  = parent.indexOf(ndefRecordModelProperty);
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
					
				Action[] values = Action.values();

				Integer index = (Integer)value;

				Action action = values[index.intValue()];

				if(action != gcActionRecord.getAction()) {
					gcActionRecord.setAction(action);

					// update property as well
					ndefRecordModelProperty.setValue(gcActionRecord.getAction().name());

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
			}
			
			if(change) {
				if(listener != null) {
					// find root
					NdefRecordModelParent p = parent;
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