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
import org.nfctools.ndef.ext.ExternalTypeRecord;
import org.nfctools.ndef.mime.MimeRecord;
import org.nfctools.ndef.reserved.ReservedRecord;
import org.nfctools.ndef.unchanged.UnchangedRecord;
import org.nfctools.ndef.unknown.UnknownRecord;
import org.nfctools.ndef.wkt.records.ActionRecord;
import org.nfctools.ndef.wkt.records.AlternativeCarrierRecord;
import org.nfctools.ndef.wkt.records.GcActionRecord;
import org.nfctools.ndef.wkt.records.GcDataRecord;
import org.nfctools.ndef.wkt.records.GcTargetRecord;
import org.nfctools.ndef.wkt.records.GenericControlRecord;
import org.nfctools.ndef.wkt.records.HandoverCarrierRecord;
import org.nfctools.ndef.wkt.records.HandoverRequestRecord;
import org.nfctools.ndef.wkt.records.HandoverSelectRecord;
import org.nfctools.ndef.wkt.records.SmartPosterRecord;
import org.nfctools.ndef.wkt.records.TextRecord;
import org.nfctools.ndef.wkt.records.UriRecord;

import com.antares.nfc.plugin.Activator;

public class NdefRecordModelMenuListener implements IMenuListener, ISelectionChangedListener {

	@SuppressWarnings("rawtypes")
	private Class[] rootRecordTypes = new Class[]{
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
			
			ReservedRecord.class,
			UnchangedRecord.class,
			
			GenericControlRecord.class
	};
	
	@SuppressWarnings("rawtypes")
	private Class[] genericControlRecordTargetRecordTypes = new Class[]{
			TextRecord.class,
			UriRecord.class,
	};

	@SuppressWarnings("rawtypes")
	private Class[] genericControlRecordDataChildRecordTypes = rootRecordTypes;

	@SuppressWarnings("rawtypes")
	private Class[] genericControlRecordActionRecordTypes = rootRecordTypes;

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
	
	// GenericControlTarget
	private MenuManager setGenericControlTargetRecord;

	// GenericControlAction
	private MenuManager setGenericControlActionRecord;

	// GenericControl
	private MenuManager addGenericControlActionRecord;
	private MenuManager addGenericControlDataRecord;
	private MenuManager addGenericControlDataOrActionRecord;

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
				listener.insert(selectedNode.getParent(), selectedNode.getParent().indexOf(selectedNode) + offset, recordType);
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
				listener.add((NdefRecordModelParent)selectedNode, recordType);
			}
		}
	}
	
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

		removeRecord = new Action("Remove record") {
			@Override
			public void run() {
				if(listener != null) {
					if(selectedNode != null) {
						listener.remove(selectedNode);
						
						selectedNode = null;
					}
				}
			}
		};

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

		removeRecord = new Action("Remove record") {
			@Override
			public void run() {
				if(listener != null) {
					if(selectedNode != null) {
						listener.remove(selectedNode);
						
						selectedNode = null;
					}
				}
			}
		};

		setGenericControlActionRecord = new MenuManager("Add action", null);
        for(Class<? extends Record> recordType: genericControlRecordActionRecordTypes) {
        	setGenericControlActionRecord.add(new AddChildAction(recordType.getSimpleName(), recordType));
        }
		
		setGenericControlTargetRecord = new MenuManager("Add target identifier", null);
        for(Class<? extends Record> recordType: genericControlRecordTargetRecordTypes) {
        	setGenericControlTargetRecord.add(new AddChildAction(recordType.getSimpleName(), recordType));
        }
		
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(this);
		
		treeViewer.getControl().setMenu(manager.createContextMenu(treeViewer.getControl()));
		
		triggerColumnSelectedColumn(treeViewer);
		
		treeViewer.addSelectionChangedListener(this);
	}
	
	@Override
	public void menuAboutToShow(IMenuManager menuManager) {
		
		if(selectedNode != null) {
			
			if(selectedNode instanceof NdefRecordModelRecord) {
				
				// parent operation (sibling) options
				NdefRecordModelParent selectedNodeParent = selectedNode.getParent();
				if(!selectedNodeParent.hasParent()) { 
					// add and remove sibling nodes
					menuManager.add(insertRootSiblingRecordBefore);
					menuManager.add(insertRootSiblingRecordAfter);
					menuManager.add(removeRecord);
				} else if(selectedNodeParent instanceof NdefRecordModelRecord) {
					NdefRecordModelRecord selectedNodeParentRecord = (NdefRecordModelRecord)selectedNodeParent;
					
					Class<? extends Record> parentType = selectedNodeParentRecord.getRecord().getClass();
					if(parentType == GcDataRecord.class) {
						// add and remove sibling nodes
						menuManager.add(insertGenericControlDataSiblingRecordBefore);
						menuManager.add(insertGenericControlDataSiblingRecordAfter);
						menuManager.add(removeRecord);
					} else if(parentType == GcTargetRecord.class) {
						menuManager.add(removeRecord);
					} else if(parentType == GcActionRecord.class) {
						menuManager.add(removeRecord);
					}
				}
				
				// child operation options
				NdefRecordModelRecord selectedNodeRecord = (NdefRecordModelRecord)selectedNode;
				Class<? extends Record> childType = selectedNodeRecord.getRecord().getClass();
				if(childType == GcDataRecord.class) {
					menuManager.add(addGenericControlDataChildRecord);
					menuManager.add(removeRecord);
				} else if(childType == GcTargetRecord.class) {
					GcTargetRecord gcTargetRecord = (GcTargetRecord) selectedNodeRecord.getRecord();
					if(!gcTargetRecord.hasTargetIdentifier()) {
						menuManager.add(setGenericControlTargetRecord);
					}
				} else if(childType == GcActionRecord.class) {
					menuManager.add(setGenericControlActionRecord);
					menuManager.add(removeRecord);
				} else if(childType == GenericControlRecord.class) {
					GenericControlRecord genericControlRecord = (GenericControlRecord)selectedNodeRecord.getRecord();
					
					if(!genericControlRecord.hasAction() && !genericControlRecord.hasData()) {
						menuManager.add(addGenericControlDataOrActionRecord);
					} else if(!genericControlRecord.hasAction()) {
						menuManager.add(addGenericControlActionRecord);
					} else if(!genericControlRecord.hasData()) {
						menuManager.add(addGenericControlDataRecord);
					}
				}
			} else {
				Activator.info("Ignore " + selectedNode.getClass().getSimpleName());
			}
			

		} else {
			selectedNode = root;
			
			menuManager.add(addRootChildRecord);
		}
	}
	
	public boolean hasParent(NdefRecordModelNode node, Class<? extends Record> type) {
		NdefRecordModelParent parent = node.getParent();
		while(parent != null) {
			
			if(parent instanceof NdefRecordModelRecord) {
				NdefRecordModelRecord ndefRecordModelRecord = (NdefRecordModelRecord)parent;

				if(ndefRecordModelRecord.getRecord().getClass() == type) {
					return true;
				}
			}
			
			parent = parent.getParent();
		}
		return false;
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
