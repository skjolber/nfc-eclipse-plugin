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

package com.antares.nfc.plugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TreeDropTargetEffect;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.nfctools.ndef.Record;

import com.antares.nfc.model.NdefRecordModelChangeListener;
import com.antares.nfc.model.NdefRecordModelContentProvider;
import com.antares.nfc.model.NdefRecordModelEditingSupport;
import com.antares.nfc.model.NdefRecordModelMenuListener;
import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.model.NdefRecordModelParent;
import com.antares.nfc.model.NdefRecordModelParentProperty;
import com.antares.nfc.model.NdefRecordModelRecord;
import com.antares.nfc.model.NdefRecordModelSizeColumnLabelProvider;
import com.antares.nfc.model.NdefRecordModelValueColumnLabelProvider;

public class NdefEditorPart extends EditorPart implements NdefRecordModelChangeListener {

	protected boolean dirty = false;
	protected TreeViewer treeViewer; 
	protected NdefModelOperator operator;
	
	public NdefEditorPart(NdefModelOperator operator) {
		this.operator = operator;
	}
	
	@Override
	public void update(NdefRecordModelParent model) {
		operator.update(model);
		
		setDirty(true);
	}
	
	@Override
	public void insert(NdefRecordModelParent parent, int index, Class<? extends Record> recordType) {
		operator.insert(parent, index, recordType);
		
		modified();
	}
	
	@Override
	public void set(NdefRecordModelParentProperty ndefRecordModelParentProperty, Class type) {
		operator.set(ndefRecordModelParentProperty, type);
		
		modified();
	}
	
	@Override
	public void remove(NdefRecordModelNode node) {
		operator.remove(node);
		
		modified();
	}

	@Override
	public void add(NdefRecordModelParent node, Class<? extends Record> recordType) {
		operator.add(node, recordType);

		modified();
	}

	protected void modified() {
		treeViewer.refresh(operator.getModel());

		setDirty(true);
	}
	
	@Override
	public void doSaveAs() {
		throw new RuntimeException();
	}
	
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}
	
	/**
	 * Sets the "dirty" flag.
	 * @param isDirty true if the file has been modified otherwise false
	 */
	public void setDirty(boolean isDirty) {
		//
		// Set internal "dirty" flag
		//
		this.dirty = isDirty;
		//
		// Fire the "property change" event to change file's status within Eclipse IDE
		//
		firePropertyChange(IEditorPart.PROP_DIRTY);
	}
	
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite composite) {
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		
		composite.setLayout(gridLayout);
		composite.setBackground(new Color(composite.getDisplay(), 0xFF, 0xFF, 0xFF));

		treeViewer = new TreeViewer(composite, SWT.BORDER
				| SWT.FULL_SELECTION);
		treeViewer.getTree().setLinesVisible(true);
		treeViewer.getTree().setHeaderVisible(true);
				
		GridData gridData = new GridData();
		
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 2;

		treeViewer.getTree().setLayoutData(gridData);

		TreeViewerFocusCellManager focusCellManager = new TreeViewerFocusCellManager(treeViewer,new FocusCellOwnerDrawHighlighter(treeViewer));
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(treeViewer) {
			@Override
			protected boolean isEditorActivationEvent(
					ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR)
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};
		
		TreeViewerEditor.create(treeViewer, focusCellManager, actSupport, ColumnViewerEditor.TABBING_HORIZONTAL
				| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
				| ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);
		
		TreeViewerColumn column = new TreeViewerColumn(treeViewer, 0, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setMoveable(true);
		column.getColumn().setText("Record");
		column.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				return element.toString();
			}

		});
		
		column = new TreeViewerColumn(treeViewer, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setMoveable(true);
		column.getColumn().setText("Value");
		column.setLabelProvider(new NdefRecordModelValueColumnLabelProvider());
		column.setEditingSupport(new NdefRecordModelEditingSupport(treeViewer, this));		
		
		column = new TreeViewerColumn(treeViewer, SWT.NONE);
		column.getColumn().setWidth(50);
		column.getColumn().setMoveable(true);
		column.getColumn().setText("Size");
		column.getColumn().setAlignment(SWT.CENTER);
		column.setLabelProvider(new NdefRecordModelSizeColumnLabelProvider());
		
		treeViewer.setContentProvider(new NdefRecordModelContentProvider());

		treeViewer.setInput(operator.getModel());
		
		new NdefRecordModelMenuListener(treeViewer, this, operator.getModel());
		
		treeViewer.expandAll();

	    Transfer[] types = new Transfer[] {LocalSelectionTransfer.getTransfer()};
	    int operations = DND.DROP_MOVE;

		treeViewer.addDragSupport(operations, types, new DragSourceListener() {

			@Override
			public void dragFinished(DragSourceEvent event) {
				// end drag
			}

			@Override
			public void dragSetData(DragSourceEvent event) {
				// do nothing for LocalSelectionTransfer
			}

			@Override
			public void dragStart(DragSourceEvent event) {
				
				int column = NdefEditorPart.this.getColumn(event.x);

				if(column == 0) {
					TreeSelection selection = (TreeSelection) treeViewer.getSelection();
					
					Object node = selection.getFirstElement();
					if(node instanceof NdefRecordModelRecord) {
						event.doit = true;
						
						NdefRecordModelRecord ndefRecordModelRecord = (NdefRecordModelRecord)node;
						
						if(ndefRecordModelRecord.getLevel() == 1) {
							Activator.info("Start drag for " + ndefRecordModelRecord.getRecord().getClass().getSimpleName() + " at level " + ndefRecordModelRecord.getLevel());
							
							event.data = node;
							
							event.doit = true;
						} else {
							Activator.info("Do not start drag for level " + ndefRecordModelRecord.getLevel());
							
							event.doit = false;
						}
					} else {
						Activator.info("Do not start drag");
						
						event.doit = false;
					}
				} else {
					Activator.info("Do not start drag for column " + column);
					
					event.doit = false;
				}
			}
		});
		
		treeViewer.addDropSupport(operations, types, new TreeDropTargetEffect(treeViewer.getTree()) {
			
			/**
			 * 
			 * Check out what kind of insert feedback to give user in GUI while dragging: insert before or after.
			 * 
			 */
			
			@Override
			public void dragOver(DropTargetEvent event) {
				Widget item = event.item;
				if(item instanceof TreeItem) {
					TreeItem treeItem = (TreeItem)item;
					
					NdefRecordModelNode node = (NdefRecordModelNode) treeItem.getData();
					
					if(node instanceof NdefRecordModelRecord) {
						if(node.getLevel() == 1) {
							
							event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
							
							Point pt = event.display.map(null, treeViewer.getControl(), event.x, event.y);
							Rectangle bounds = treeItem.getBounds();
							if (pt.y < bounds.y + bounds.height / 3) {
								event.feedback |= DND.FEEDBACK_INSERT_BEFORE;
							} else if (pt.y > bounds.y + 2 * bounds.height / 3) {
								event.feedback |= DND.FEEDBACK_INSERT_AFTER;
							} else {
								// event.feedback |= DND.FEEDBACK_SELECT;
							}
							
						} else {
							//System.out.println("Ignore node " + node.getClass().getSimpleName() + " at level " + node.getLevel());
						}
					} else {
						//System.out.println("Ignore node " + node.getClass().getSimpleName());
					}
				} else if(item != null) {
					// System.out.println("Ignore item " + item.getClass().getSimpleName() + " " + item.getData());
				} else if(item == null) {
					// ignore null item
				}
				
				super.dragOver(event);
			}
			
			
			public void drop(DropTargetEvent event) {
				
				Activator.info("Drop " + event.getSource() + " " + event.item);
				
				Widget item = event.item;
				if(item instanceof TreeItem) {
					TreeItem treeItem = (TreeItem)item;
					
					NdefRecordModelNode node = (NdefRecordModelNode) treeItem.getData();
					
					if(node instanceof NdefRecordModelRecord) {
						if(node.getLevel() == 1) {
							
							TreeSelection selection = (TreeSelection) treeViewer.getSelection();
							
							NdefRecordModelRecord source = (NdefRecordModelRecord) selection.getFirstElement();
							
							if(source.getLevel() != 1) {
								
								return;
							}
							
							Point pt = event.display.map(null, treeViewer.getControl(), event.x, event.y);
							Rectangle bounds = treeItem.getBounds();
							if (pt.y < bounds.y + bounds.height / 3) {
								Activator.info("Drop " + source + " before " + node);
								
								operator.move(source, node.getParent(), node.getParentIndex());
								
								modified();
							} else if (pt.y > bounds.y + 2 * bounds.height / 3) {
								Activator.info("Drop " + source + " after " + node);
								
								operator.move(source, node.getParent(), node.getParentIndex() + 1);
								
								modified();
							} else {
								// event.feedback |= DND.FEEDBACK_SELECT;
							}
							
						} else {
							Activator.info("Ignore drop node " + node.getClass().getSimpleName() + " at level " + node.getLevel());
						}
					} else {
						Activator.info("Ignore drop node " + node.getClass().getSimpleName());
					}
				} else if(item != null) {
					Activator.info("Ignore drop item " + item.getClass().getSimpleName() + " " + item.getData());
				} else if(item == null) {
					// ignore null item
				}
		      }
			
		});
	}
	
	private int getColumn(int x) {
		int a = 0;
		for (int i = 0; i < treeViewer.getTree().getColumnCount(); i++) {
			a += treeViewer.getTree().getColumn(i).getWidth();
			if (x <= a) {
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public void setFocus() {
		treeViewer.refresh();
	}
	
	@Override
	public String getTitle() {
		return "NDEF";
	}

	public void undo() {
		
	}

	public void redo() {
		
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		throw new RuntimeException("Not implemented");
	}

	public void refresh() {
		treeViewer.refresh();
	}

	

}
