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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.nfctools.ndef.Record;
import org.nfctools.ndef.auri.AbsoluteUriRecord;
import org.nfctools.ndef.empty.EmptyRecord;
import org.nfctools.ndef.ext.AndroidApplicationRecord;
import org.nfctools.ndef.ext.UnsupportedExternalTypeRecord;
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

public class NdefRecordModelMenuListener implements IMenuListener, ISelectionChangedListener {

	@SuppressWarnings({ "unchecked" })
	private Class<? extends Record>[] rootRecordTypes = new Class[]{
			AbsoluteUriRecord.class,
			ActionRecord.class,
			AndroidApplicationRecord.class,
			UnsupportedExternalTypeRecord.class,
			EmptyRecord.class,
			GenericControlRecord.class,
			
			HandoverSelectRecord.class,
			HandoverCarrierRecord.class,
			HandoverRequestRecord.class,

			MimeRecord.class,
			SmartPosterRecord.class,
			TextRecord.class,
			UnknownRecord.class,
			UriRecord.class
			
	};
	
	@SuppressWarnings({ "unchecked" })
	private Class<? extends Record>[] genericControlRecordTargetRecordTypes = new Class[]{
			TextRecord.class,
			UriRecord.class,
	};

	@SuppressWarnings("rawtypes")
	private Class[] genericControlRecordDataChildRecordTypes = rootRecordTypes;

	@SuppressWarnings({ "unused" })
	private Class<? extends Record>[] genericControlRecordActionRecordTypes = rootRecordTypes;

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
        
        for(Class<? extends Record> recordType: rootRecordTypes) {
        	insertRootSiblingRecordBefore.add(new InsertSiblingAction(recordType.getSimpleName(), recordType, 0));
        }
		
		// insert after
        insertRootSiblingRecordAfter = new MenuManager("Insert record after", null);
        
        for(Class<? extends Record> recordType: rootRecordTypes) {
        	insertRootSiblingRecordAfter.add(new InsertSiblingAction(recordType.getSimpleName(), recordType, 1));
        }

		// just insert
        addRootChildRecord = new MenuManager("Add record", null);
        
        for(Class<? extends Record> recordType: rootRecordTypes) {
        	addRootChildRecord.add(new AddChildAction(recordType.getSimpleName(), recordType));
        }
		
		// generic control
		// insert before
		insertGenericControlDataSiblingRecordBefore = new MenuManager("Insert record before", null);
        
        for(Class<? extends Record> recordType: genericControlRecordDataChildRecordTypes) {
        	insertGenericControlDataSiblingRecordBefore.add(new InsertSiblingAction(recordType.getSimpleName(), recordType, 0));
        }
		
		// insert after
        insertGenericControlDataSiblingRecordAfter = new MenuManager("Insert record after", null);
        
        for(Class<? extends Record> recordType: genericControlRecordDataChildRecordTypes) {
        	insertGenericControlDataSiblingRecordAfter.add(new InsertSiblingAction(recordType.getSimpleName(), recordType, 1));
        }

		// just insert
        addGenericControlDataChildRecord = new MenuManager("Add record", null);
        
        for(Class<? extends Record> recordType: genericControlRecordDataChildRecordTypes) {
        	addGenericControlDataChildRecord.add(new AddChildAction(recordType.getSimpleName(), recordType));
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
        for(Class<? extends Record> recordType: genericControlRecordTargetRecordTypes) {
        	setGenericControlTargetRecord.add(new SetChildAction(recordType.getSimpleName(), recordType));
        }
        
        setGenericControlActionRecord = new MenuManager("Set action record", null);
        for(Class<? extends Record> recordType: rootRecordTypes) {
        	setGenericControlActionRecord.add(new SetChildAction(recordType.getSimpleName(), recordType));
        }
                
        // HandoverRequestRecord
    	insertAlternativeCarrierRecordSiblingRecordBefore = new InsertSiblingAction("Insert " + AlternativeCarrierRecord.class.getSimpleName() + " before", AlternativeCarrierRecord.class, 0);
    	insertAlternativeCarrierRecordSiblingRecordAfter = new InsertSiblingAction("Insert " + AlternativeCarrierRecord.class.getSimpleName() + " after", AlternativeCarrierRecord.class, 1);
    	addAlternativeCarrierRecordChildRecord = new AddChildAction("Add " + AlternativeCarrierRecord.class.getSimpleName(), AlternativeCarrierRecord.class);

    	// HandoverCarrierRecord
    	// well known type
        setHandoverCarrierWellKnownType = new MenuManager("Set carrier type", null);
        for(Class<? extends Record> recordType: NdefRecordModelEditingSupport.wellKnownRecordTypes) {
        	setHandoverCarrierWellKnownType.add(new SetChildAction(recordType.getSimpleName(), recordType));
        }
        
        // external type
        setHandoverCarrierExternalType = new MenuManager("Set carrier type", null);
        for(Class<? extends Record> recordType: NdefRecordModelEditingSupport.externalRecordTypes) {
        	setHandoverCarrierExternalType.add(new SetChildAction(recordType.getSimpleName(), recordType));
        }
    	
        // list
        addListItem = new AddListItemAction("Add item");
        insertListItemSiblingBefore = new InsertListItemAction("Insert item before", 0);
        insertListItemSiblingAfter = new InsertListItemAction("Insert item after", 1);
        removeListItem = new RemoveListItemAction("Remove item");
        
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
