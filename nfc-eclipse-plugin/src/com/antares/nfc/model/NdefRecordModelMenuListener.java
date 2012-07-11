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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.nfctools.ndef.Record;
import org.nfctools.ndef.auri.AbsoluteUriRecord;
import org.nfctools.ndef.empty.EmptyRecord;
import org.nfctools.ndef.ext.AndroidApplicationRecord;
import org.nfctools.ndef.ext.UnsupportedExternalTypeRecord;
import org.nfctools.ndef.mime.BinaryMimeRecord;
import org.nfctools.ndef.mime.MimeRecord;
import org.nfctools.ndef.unknown.UnknownRecord;
import org.nfctools.ndef.wkt.handover.records.AlternativeCarrierRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverCarrierRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverRequestRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverSelectRecord;
import org.nfctools.ndef.wkt.records.ActionRecord;
import org.nfctools.ndef.wkt.records.GcActionRecord;
import org.nfctools.ndef.wkt.records.GcDataRecord;
import org.nfctools.ndef.wkt.records.GcTargetRecord;
import org.nfctools.ndef.wkt.records.GenericControlRecord;
import org.nfctools.ndef.wkt.records.SmartPosterRecord;
import org.nfctools.ndef.wkt.records.TextRecord;
import org.nfctools.ndef.wkt.records.UriRecord;

import com.antares.nfc.plugin.Activator;

public class NdefRecordModelMenuListener implements IMenuListener, ISelectionChangedListener {
	
	private NdefRecordType[] rootRecordTypes = new NdefRecordType[]{
			NdefRecordType.getType(AbsoluteUriRecord.class),
			NdefRecordType.getType(ActionRecord.class),
			NdefRecordType.getType(AndroidApplicationRecord.class),
			NdefRecordType.getType(UnsupportedExternalTypeRecord.class),
			NdefRecordType.getType(EmptyRecord.class),
			NdefRecordType.getType(GenericControlRecord.class),
		
			NdefRecordType.getType(HandoverSelectRecord.class),
			NdefRecordType.getType(HandoverCarrierRecord.class),
			NdefRecordType.getType(HandoverRequestRecord.class),

			NdefRecordType.getType(BinaryMimeRecord.class),
			NdefRecordType.getType(SmartPosterRecord.class),
			NdefRecordType.getType(TextRecord.class),
			NdefRecordType.getType(UnknownRecord.class),
			NdefRecordType.getType(UriRecord.class)
	};
	
	private NdefRecordType[] genericControlRecordTargetRecordTypes = new NdefRecordType[]{
			NdefRecordType.getType(TextRecord.class),
			NdefRecordType.getType(UriRecord.class)
	};

	private NdefRecordType[] genericControlRecordDataChildRecordTypes = rootRecordTypes;

	@SuppressWarnings("unused")
	private NdefRecordType[] genericControlRecordActionRecordTypes = rootRecordTypes;

	private TreeViewer treeViewer;
	private MenuManager manager = new MenuManager();
	private NdefRecordModelChangeListener listener;
	
	private int activeColumn = -1;
	
	private NdefRecordModelParent root;
		
	private NdefRecordModelNode selectedNode;
	
	private void triggerColumnSelectedColumn(final TreeViewer v) {
		v.getTree().addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDown(MouseEvent e) {
				
				activeColumn = -1;
						
				int x = 0;
				for (int i = 0; i < v.getTree().getColumnCount(); i++) {
					x += v.getTree().getColumn(i).getWidth();
					if (e.x <= x) {
						activeColumn = i;
						break;
					}
				}

			
			}
		});
		
		
	}
	
	private MenuManager insertRootSiblingRecordBefore;
	private MenuManager insertRootSiblingRecordAfter;
	private MenuManager addRootChildRecord;
	private Action removeRecord;

	// GenericControlData
	private MenuManager insertGenericControlDataSiblingRecordBefore;
	private MenuManager insertGenericControlDataSiblingRecordAfter;
	private MenuManager addGenericControlDataChildRecord;
	
	// GenericControl Target Record
	private MenuManager setGenericControlTargetRecord;

	// GenericControl Action Record
	private MenuManager setGenericControlActionRecord;

	// GenericControl
	private MenuManager addGenericControlActionRecord;
	private MenuManager addGenericControlDataRecord;
	private MenuManager addGenericControlDataOrActionRecord;

	// Lists
	private Action addListItem;
	private Action insertListItemSiblingBefore;
	private Action insertListItemSiblingAfter;
	private Action removeListItem;

	// HandoverRequestRecord
	private Action insertAlternativeCarrierRecordSiblingRecordBefore;
	private Action insertAlternativeCarrierRecordSiblingRecordAfter;
	private Action addAlternativeCarrierRecordChildRecord;

	// HandoverCarrierRecord Action Record
	private MenuManager setHandoverCarrierExternalType;
	private MenuManager setHandoverCarrierWellKnownType;

	// mime content
	private Action viewContent;
	private Action saveContent;

	private class InsertSiblingAction extends Action {

		private Class<? extends Record> recordType;
		private int offset;
		
		public InsertSiblingAction(String name, Class<? extends Record> recordType, int offset) {
			super(name);
			
			this.recordType = recordType;
			this.offset = offset;
		}
		
		@Override
		public void run() {
			if(listener != null) {
				listener.addRecord(selectedNode.getParent(), selectedNode.getParentIndex() + offset, recordType);
			}
		}
	}
	
	private class AddChildAction extends Action {

		private Class<? extends Record> recordType;
		
		public AddChildAction(String name, Class<? extends Record> recordType) {
			super(name);
			
			this.recordType = recordType;
		}
		
		@Override
		public void run() {
			if(listener != null) {
				if(selectedNode != null) {
					listener.addRecord((NdefRecordModelParent)selectedNode, -1, recordType);
				} else {
					listener.addRecord((NdefRecordModelParent)root, -1, recordType);
				}
			}
		}
	}
	
	private class ViewContentAction extends Action {

		public ViewContentAction(String name) {
			super(name);
		}
		
		@Override
		public void run() {
			if(listener != null) {
				
				byte[] payload;
				String mimeType;
				Record record = selectedNode.getRecord();
				if(record instanceof MimeRecord) {
					MimeRecord mimeRecord = (MimeRecord) record;

					mimeType = mimeRecord.getContentType();
					payload = mimeRecord.getContentAsBytes();
				} else if(record instanceof UnknownRecord) {
					UnknownRecord unknownRecord = (UnknownRecord) record;
					
					mimeType = null;
					payload = unknownRecord.getPayload();
				} else {
					throw new RuntimeException();
				}

				Activator.info("View " + payload.length + " bytes");
				
				
				//listener.viewBinaryContent((NdefRecordModelParentProperty)selectedNode);
			}
		}
	}
	
	private class SaveContentAction extends Action {

		public SaveContentAction(String name) {
			super(name);
		}
		
		@Override
		public void run() {
			if(listener != null) {
				
				// File standard dialog
				FileDialog fileDialog = new FileDialog(treeViewer.getTree().getShell());
				// Set the text
				fileDialog.setText("Save mime media");
				// Set filter
				
				final String fileString = fileDialog.open();
				
				if(fileString != null) {
					Activator.info("Save to " + fileString);

					File file = new File(fileString);
					
					OutputStream outputStream = null;
					try {
						outputStream = new FileOutputStream(file);

						byte[] payload;
						Record record = selectedNode.getRecord();
						if(record instanceof MimeRecord) {
							MimeRecord mimeRecord = (MimeRecord) record;
							
							payload = mimeRecord.getContentAsBytes();
						} else if(record instanceof UnknownRecord) {
							UnknownRecord unknownRecord = (UnknownRecord) record;
							
							payload = unknownRecord.getPayload();
						} else {
							throw new RuntimeException();
						}

						Activator.info("Save " + payload.length + " bytes");


						outputStream.write(payload);
					} catch(IOException e) {
						
						Activator.warn("Unable to save to file " + fileString, e);
						
				    	Display.getCurrent().asyncExec(
				                new Runnable()
				                {
				                    public void run()
				                    {
										// http://www.vogella.de/articles/EclipseDialogs/article.html#dialogs_jfacemessage
										Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
										MessageDialog.openError(shell, "Error", "Unable to save to file " + fileString);
				                    }
				                }
				            );

					} finally {
						try {
							if(outputStream != null) {
								outputStream.close();
							}
						} catch(Exception e) {
							// ignore
						}
					}
				} else {
					Activator.info("No save");
				}
			}
		}
	}
	
	
	private class SetChildAction extends Action {

		private Class<? extends Record> recordType;
		
		public SetChildAction(String name, Class<? extends Record> recordType) {
			super(name);
			
			this.recordType = recordType;
		}
		
		@Override
		public void run() {
			if(listener != null) {
				listener.setRecord((NdefRecordModelParentProperty)selectedNode, recordType);
			}
		}
	}

	
	private class InsertListItemAction extends Action {

		private int offset;
		
		public InsertListItemAction(String name, int offset) {
			super(name);
			
			this.offset = offset;
		}
		
		@Override
		public void run() {
			if(listener != null) {
				listener.addListItem(selectedNode.getParent(), selectedNode.getParent().indexOf(selectedNode) + offset);
			}
		}
	}
	
	private class AddListItemAction extends Action {

		public AddListItemAction(String name) {
			super(name);
		}
		
		@Override
		public void run() {
			if(listener != null) {
				listener.addListItem((NdefRecordModelParent)selectedNode, -1);
			}
		}
	}

	private class RemoveAction extends Action {

		public RemoveAction(String name) {
			super(name);
		}
		
		@Override
		public void run() {
			if(listener != null) {
				if(selectedNode != null) {
					listener.removeRecord((NdefRecordModelRecord)selectedNode);
					
					selectedNode = null;
				}
			}
		}

	}
	
	private class RemoveListItemAction extends Action {

		public RemoveListItemAction(String name) {
			super(name);
		}
		
		@Override
		public void run() {
			if(listener != null) {
				if(selectedNode != null) {
					listener.removeListItem((NdefRecordModelPropertyListItem)selectedNode);
					
					selectedNode = null;
				}
			}
		}

	}

	@SuppressWarnings("unchecked")
	public NdefRecordModelMenuListener(final TreeViewer treeViewer, final NdefRecordModelChangeListener listener, NdefRecordModelParent root) {
		this.treeViewer = treeViewer;
		this.listener = listener;
		this.root = root;
		
		// root
		// insert before
		insertRootSiblingRecordBefore = new MenuManager("Insert record before", null);
        
        for(NdefRecordType recordType: rootRecordTypes) {
        	insertRootSiblingRecordBefore.add(new InsertSiblingAction(recordType.getRecordLabel(), recordType.getRecordClass(), 0));
        }
		
		// insert after
        insertRootSiblingRecordAfter = new MenuManager("Insert record after", null);
        
        for(NdefRecordType recordType: rootRecordTypes) {
        	insertRootSiblingRecordAfter.add(new InsertSiblingAction(recordType.getRecordLabel(), recordType.getRecordClass(), 1));
        }

		// just insert
        addRootChildRecord = new MenuManager("Add record", null);
        
        for(NdefRecordType recordType: rootRecordTypes) {
        	addRootChildRecord.add(new AddChildAction(recordType.getRecordLabel(), recordType.getRecordClass()));
        }
		
		// generic control
		// insert before
		insertGenericControlDataSiblingRecordBefore = new MenuManager("Insert record before", null);
        
        for(NdefRecordType recordType: genericControlRecordDataChildRecordTypes) {
        	insertGenericControlDataSiblingRecordBefore.add(new InsertSiblingAction(recordType.getRecordLabel(), recordType.getRecordClass(), 0));
        }
		
		// insert after
        insertGenericControlDataSiblingRecordAfter = new MenuManager("Insert record after", null);
        
        for(NdefRecordType recordType: genericControlRecordDataChildRecordTypes) {
        	insertGenericControlDataSiblingRecordAfter.add(new InsertSiblingAction(recordType.getRecordLabel(), recordType.getRecordClass(), 1));
        }

		// just insert
        addGenericControlDataChildRecord = new MenuManager("Add record", null);
        
        for(NdefRecordType recordType: genericControlRecordDataChildRecordTypes) {
        	addGenericControlDataChildRecord.add(new AddChildAction(recordType.getRecordLabel(), recordType.getRecordClass()));
        }

        addGenericControlActionRecord = new MenuManager("Add record", null);
       	addGenericControlActionRecord.add(new AddChildAction(GcActionRecord.class.getSimpleName(), GcActionRecord.class));

        addGenericControlDataRecord = new MenuManager("Add record", null);
       	addGenericControlDataRecord.add(new AddChildAction(GcDataRecord.class.getSimpleName(), GcDataRecord.class));

        addGenericControlDataOrActionRecord = new MenuManager("Add record", null);
        addGenericControlDataOrActionRecord.add(new AddChildAction(GcActionRecord.class.getSimpleName(), GcActionRecord.class));
        addGenericControlDataOrActionRecord.add(new AddChildAction(GcDataRecord.class.getSimpleName(), GcDataRecord.class));

		removeRecord = new RemoveAction("Remove record");

		setGenericControlTargetRecord = new MenuManager("Set target identifier", null);
        for(NdefRecordType recordType: genericControlRecordTargetRecordTypes) {
        	setGenericControlTargetRecord.add(new SetChildAction(recordType.getRecordLabel(), recordType.getRecordClass()));
        }
        
        setGenericControlActionRecord = new MenuManager("Set action record", null);
        for(NdefRecordType recordType: rootRecordTypes) {
        	setGenericControlActionRecord.add(new SetChildAction(recordType.getRecordLabel(), recordType.getRecordClass()));
        }
                
        // HandoverRequestRecord
    	insertAlternativeCarrierRecordSiblingRecordBefore = new InsertSiblingAction("Insert " + AlternativeCarrierRecord.class.getSimpleName() + " before", AlternativeCarrierRecord.class, 0);
    	insertAlternativeCarrierRecordSiblingRecordAfter = new InsertSiblingAction("Insert " + AlternativeCarrierRecord.class.getSimpleName() + " after", AlternativeCarrierRecord.class, 1);
    	addAlternativeCarrierRecordChildRecord = new AddChildAction("Add " + AlternativeCarrierRecord.class.getSimpleName(), AlternativeCarrierRecord.class);

    	// HandoverCarrierRecord
    	// well known type
        setHandoverCarrierWellKnownType = new MenuManager("Set carrier type", null);
        for(NdefRecordType recordType: NdefRecordModelEditingSupport.wellKnownRecordTypes) {
        	setHandoverCarrierWellKnownType.add(new SetChildAction(recordType.getRecordLabel(), recordType.getRecordClass()));
        }
        
        // external type
        setHandoverCarrierExternalType = new MenuManager("Set carrier type", null);
        for(NdefRecordType recordType: NdefRecordModelEditingSupport.externalRecordTypes) {
        	setHandoverCarrierExternalType.add(new SetChildAction(recordType.getRecordLabel(), recordType.getRecordClass()));
        }
    	
        // list
        addListItem = new AddListItemAction("Add item");
        insertListItemSiblingBefore = new InsertListItemAction("Insert item before", 0);
        insertListItemSiblingAfter = new InsertListItemAction("Insert item after", 1);
        removeListItem = new RemoveListItemAction("Remove item");
        
        // mime interaction
        viewContent = new ViewContentAction("View content");
        saveContent = new SaveContentAction("Save to file");
        
		manager.setRemoveAllWhenShown(true);
		
		manager.addMenuListener(this);
		
		treeViewer.getControl().setMenu(manager.createContextMenu(treeViewer.getControl()));
		
		triggerColumnSelectedColumn(treeViewer);
		
		treeViewer.addSelectionChangedListener(this);
	}
	
	@Override
	public void menuAboutToShow(IMenuManager menuManager) {
		
		if(selectedNode != null) {

			// filter out list types
			if(selectedNode instanceof NdefRecordModelPropertyListItem) {
				menuManager.add(insertListItemSiblingBefore);
				menuManager.add(insertListItemSiblingAfter);
				menuManager.add(removeListItem);
			} else if(selectedNode instanceof NdefRecordModelPropertyList) {
				menuManager.add(addListItem);
			} else {
			
				// parent operation (sibling) options
				Record parentRecord = selectedNode.getParentRecord();
				if(parentRecord == null) {
					// root
					// add and remove sibling nodes
					menuManager.add(insertRootSiblingRecordBefore);
					menuManager.add(insertRootSiblingRecordAfter);
					menuManager.add(removeRecord);
				} else {
					
					if(selectedNode instanceof NdefRecordModelRecord) {
						// parent operation options
						if(parentRecord instanceof GcDataRecord) {
							// add and remove sibling nodes
							menuManager.add(insertGenericControlDataSiblingRecordBefore);
							menuManager.add(insertGenericControlDataSiblingRecordAfter);
							menuManager.add(removeRecord);
						} else if(parentRecord instanceof GcTargetRecord) {
							menuManager.add(removeRecord);
						} else if(parentRecord instanceof GcActionRecord) {
							menuManager.add(removeRecord);
						} else if(parentRecord instanceof HandoverRequestRecord) {
							if(selectedNode.getRecordBranchIndex() == 3) {
								menuManager.add(insertAlternativeCarrierRecordSiblingRecordBefore);
								menuManager.add(insertAlternativeCarrierRecordSiblingRecordAfter);
								menuManager.add(removeRecord);
							}
						} else if(parentRecord instanceof HandoverSelectRecord) {
							if(selectedNode.getRecordBranchIndex() == 2) {
								menuManager.add(insertAlternativeCarrierRecordSiblingRecordBefore);
								menuManager.add(insertAlternativeCarrierRecordSiblingRecordAfter);
								menuManager.add(removeRecord);
							}
						}
					}
					
					// child operation options
					Record record = selectedNode.getRecord();
					
					if(record instanceof GcDataRecord) {
						menuManager.add(addGenericControlDataChildRecord);
					} else if(record instanceof GcTargetRecord) {
						if(selectedNode instanceof NdefRecordModelParentProperty) {
							menuManager.add(setGenericControlTargetRecord);
						}
					} else if(record instanceof GcActionRecord) {
						if(selectedNode instanceof NdefRecordModelParentProperty) {
							menuManager.add(setGenericControlActionRecord);
						}
					} else if(record instanceof GenericControlRecord) {
						GenericControlRecord genericControlRecord = (GenericControlRecord)record;
						
						if(!genericControlRecord.hasAction() && !genericControlRecord.hasData()) {
							menuManager.add(addGenericControlDataOrActionRecord);
						} else if(!genericControlRecord.hasAction()) {
							menuManager.add(addGenericControlActionRecord);
						} else if(!genericControlRecord.hasData()) {
							menuManager.add(addGenericControlDataRecord);
						}
					} else if(record instanceof HandoverRequestRecord) {
						if(selectedNode instanceof NdefRecordModelParentProperty) {
							menuManager.add(addAlternativeCarrierRecordChildRecord);
						}
					} else if(record instanceof HandoverSelectRecord) {
						
						if(selectedNode instanceof NdefRecordModelParentProperty) {
							if(selectedNode.getRecordBranchIndex() == 2) {
								menuManager.add(addAlternativeCarrierRecordChildRecord);
							}
						}
					} else if(record instanceof HandoverCarrierRecord) {

						if(selectedNode instanceof NdefRecordModelParentProperty) {
							if(selectedNode.getRecordBranchIndex() == 1) {
								HandoverCarrierRecord handoverCarrierRecord = (HandoverCarrierRecord)record;
							
								if(handoverCarrierRecord.hasCarrierTypeFormat()) {
									HandoverCarrierRecord.CarrierTypeFormat carrierTypeFormat = handoverCarrierRecord.getCarrierTypeFormat();
								
									switch(carrierTypeFormat) {
										case WellKnown : {
											// NFC Forum well-known type [NFC RTD]
											menuManager.add(setHandoverCarrierWellKnownType);
											break;
										}
										
										case External : {
											// NFC Forum external type [NFC RTD]
											menuManager.add(setHandoverCarrierWellKnownType);
											break;
										}
									}
								}
							}
						}
					} else if(record instanceof MimeRecord) {

						if(selectedNode.getRecordBranchIndex() == 1) {
							MimeRecord mimeRecord = (MimeRecord)record;
							if(mimeRecord.hasContentType()) {
								//menuManager.add(viewContent);
								menuManager.add(saveContent);
							}
						}
					} else if(record instanceof UnknownRecord) {

						UnknownRecord unknownRecord = (UnknownRecord)record;
						if(unknownRecord.hasPayload()) {
							//menuManager.add(viewContent);
							menuManager.add(saveContent);
						}
					}
				}
			}
		} else {
			// force select of root node
			menuManager.add(addRootChildRecord);
		}
	}
	
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection iStructuredSelection = (IStructuredSelection)event.getSelection();
        
        if(iStructuredSelection != null) {
        	this.selectedNode = (NdefRecordModelNode) iStructuredSelection.getFirstElement();
        } else {
        	this.selectedNode = null;
        }
	}

}
