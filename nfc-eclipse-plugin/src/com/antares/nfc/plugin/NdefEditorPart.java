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

package com.antares.nfc.plugin;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TreeDropTargetEffect;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorPart;
import org.nfctools.ndef.NdefContext;
import org.nfctools.ndef.NdefException;
import org.nfctools.ndef.NdefMessageEncoder;
import org.nfctools.ndef.NdefOperations;
import org.nfctools.ndef.Record;

import com.antares.nfc.model.NdefRecordModelChangeListener;
import com.antares.nfc.model.NdefRecordModelContentProvider;
import com.antares.nfc.model.NdefRecordModelHintColumnProvider;
import com.antares.nfc.model.NdefRecordModelMenuListener;
import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.model.NdefRecordModelParent;
import com.antares.nfc.model.NdefRecordModelParentProperty;
import com.antares.nfc.model.NdefRecordModelPropertyListItem;
import com.antares.nfc.model.NdefRecordModelRecord;
import com.antares.nfc.model.NdefRecordModelSizeColumnLabelProvider;
import com.antares.nfc.model.NdefRecordModelValueColumnLabelProvider;
import com.antares.nfc.model.editing.NdefRecordModelEditingSupport;
import com.antares.nfc.plugin.operation.NdefModelOperation;
import com.antares.nfc.terminal.NdefTerminalListener;
import com.antares.nfc.terminal.NdefTerminalListener.Type;
import com.antares.nfc.terminal.NdefTerminalWrapper;

public class NdefEditorPart extends EditorPart implements NdefRecordModelChangeListener {

	protected boolean dirty = false;
	protected TreeViewer treeViewer; 
	protected NdefModelOperator operator;
	protected SashForm form;
	protected NdefMultiPageEditor ndefMultiPageEditor;
	
	public NdefEditorPart(NdefModelOperator operator, NdefMultiPageEditor ndefMultiPageEditor) {
		this.operator = operator;
		this.ndefMultiPageEditor = ndefMultiPageEditor;
	}
	
	@Override
	public void update(NdefRecordModelNode ndefRecordModelNode, NdefModelOperation operation) {
		operator.update(ndefRecordModelNode, operation);
		
		modified(true);
		
		treeViewer.expandToLevel(ndefRecordModelNode, TreeViewer.ALL_LEVELS);
	}
	
	@Override
	public void addRecord(NdefRecordModelParent parent, int index, Class<? extends Record> type) {
		operator.addRecord(parent, index, type);
		
		modified(true);
		
		if(index == -1) {
			treeViewer.expandToLevel(parent.getChild(parent.getSize() - 1), TreeViewer.ALL_LEVELS);
		} else {
			treeViewer.expandToLevel(parent.getChild(index), TreeViewer.ALL_LEVELS);
		}
	}
	
	@Override
	public void addListItem(NdefRecordModelParent parent, int index) {
		operator.addListItem(parent, index);
		
		modified(true);
		
		if(index == -1) {
			treeViewer.expandToLevel(parent.getChild(parent.getSize() - 1), TreeViewer.ALL_LEVELS);
		} else {
			treeViewer.expandToLevel(parent.getChild(index), TreeViewer.ALL_LEVELS);
		}
	}
	
	@Override
	public void setRecord(NdefRecordModelParentProperty ndefRecordModelParentProperty, Class<? extends Record> type) {
		operator.setRecord(ndefRecordModelParentProperty, type);
		
		modified(true);
		
		treeViewer.expandToLevel(ndefRecordModelParentProperty, TreeViewer.ALL_LEVELS);
	}
	
	@Override
	public void removeRecord(NdefRecordModelRecord node) {
		operator.removeRecord(node);
		
		modified(true);
		
		clearStatus();
	}

	private void clearStatus() {
		setStatus("");
	}

	@Override
	public void removeListItem(NdefRecordModelPropertyListItem node) {
		operator.removeListItem(node);
		
		modified(true);
	}
		
	protected void modified(boolean terminal) {
		treeViewer.refresh();

		form.update();
		
		setDirty(true);
		
		updateActions();
		
		clearStatus();

		refreshStatusLine();
		
		// also fill the last column (i.e. pack or fill) if any hint has been modified
		packAndFillLastColumn();
		
		if(terminal) {
			handleTerminal();
		}
	}

	private void handleTerminal() {
		if(NdefTerminalWrapper.isAvailable()) {
			
			NdefTerminalListener ndefTerminalListener = NdefTerminalWrapper.getNdefTerminalListener();
			
			if(ndefTerminalListener != null) {
				if(ndefTerminalListener == ndefMultiPageEditor) {
					
					Type type = ndefMultiPageEditor.getType();
					
					if(type == Type.WRITE || type == Type.READ_WRITE) {
						NdefOperations ndefOperations = NdefTerminalWrapper.getNdefOperations();
						
						if(ndefOperations != null) {
							List<Record> records = operator.getRecords();
							
			        		// add write option IF message can in fact be written
			        		NdefMessageEncoder ndefMessageEncoder = NdefContext.getNdefMessageEncoder();
			        		
			        		try {
			        			ndefMessageEncoder.encode(records);

								if(ndefOperations.isFormatted()) {
									ndefOperations.writeNdefMessage(records.toArray(new Record[records.size()]));
								} else {
									ndefOperations.format(records.toArray(new Record[records.size()]));
								}
			        			setStatus("Auto-write successful.");
			        		} catch(Exception e) {
			        			setStatus("Auto-write not possible.");
			        		}
						}
					}
				}
			}			
		}
	}
	
	private void updateActions() {
		getEditorSite().getActionBars().getGlobalActionHandler(ActionFactory.UNDO.getId()).setEnabled(operator.canUndo());
		getEditorSite().getActionBars().getGlobalActionHandler(ActionFactory.REDO.getId()).setEnabled(operator.canRedo());
		//hexEditor.getEditorSite().getActionBars().getGlobalActionHandler(ActionFactory.PASTE.getId()).setEnabled(canPaste());
	}
	
	public void refreshStatusLine() {
		final Display display = Display.getDefault();

		new Thread() {

			public void run() {

				display.syncExec(new Runnable() {
					public void run() {

						IActionBars actionBars = getEditorSite().getActionBars(); 

						if( actionBars == null ) {
							return ;
						}

						IStatusLineManager statusLineManager = actionBars.getStatusLineManager();

						if( statusLineManager == null ) {
							return ;
						}
						
						IContributionItem[] items = statusLineManager.getItems();
						
						for(IContributionItem item : items) {
							if(item.getId().equals(NdefMultiPageEditorContributor.class.getName()+".size")) {
								
								StatusLineContributionItem size = (StatusLineContributionItem)item;
								
								try {
									size.setText(operator.toNdefMessage().length + " bytes ");
								} catch(NdefException e) {
									size.setText("-");
								}
							} else if(item.getId().equals(NdefMultiPageEditorContributor.class.getName()+".terminal")) {
								StatusLineContributionItem size = (StatusLineContributionItem)item;
								
								if(NdefTerminalWrapper.isAvailable()) {
									if(NdefTerminalWrapper.hasFoundTerminal()) {
	
										String terminalName = NdefTerminalWrapper.getTerminalName();
										if(terminalName != null) {
											size.setText(terminalName);
										} else {
											size.setText("Disconnected");
										}
									} else {
										size.setText("No card terminal");
									}
								} else {
									size.setText("No card terminal");
								}
							}

						}
						
						// set global message using
						// statusLineManager.setMessage( ..);
					}
				});
			}
		}.start();	
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

		composite.setLayout (new FillLayout());
		composite.setBackground(new Color(composite.getDisplay(), 0xFF, 0xFF, 0xFF));

		form = new SashForm(composite, SWT.HORIZONTAL);
		form.setLayout(new FillLayout());

		Composite wrapper = new Composite(form,SWT.NONE);
		wrapper.setLayout(new FillLayout());

		treeViewer = new TreeViewer(wrapper, SWT.BORDER
				| SWT.FULL_SELECTION);
		treeViewer.getTree().setLinesVisible(true);
		treeViewer.getTree().setHeaderVisible(true);
				
		ColumnViewerToolTipSupport.enableFor(treeViewer);		
		
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
			
			// add tooltip here
		});
		
		column = new TreeViewerColumn(treeViewer, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setMoveable(true);
		column.getColumn().setText("Value");
		
		column.setLabelProvider(new NdefRecordModelValueColumnLabelProvider());
		column.setEditingSupport(new NdefRecordModelEditingSupport(treeViewer, this, operator.getNdefRecordFactory()));		
		
		column = new TreeViewerColumn(treeViewer, SWT.NONE);
		column.getColumn().setWidth(50);
		column.getColumn().setMoveable(true);
		column.getColumn().setText("Size");
		column.getColumn().setAlignment(SWT.CENTER);
		column.setLabelProvider(new NdefRecordModelSizeColumnLabelProvider());
		
		// http://blog.eclipse-tips.com/2008/05/single-column-tableviewer-and.html
		
		column = new TreeViewerColumn(treeViewer, SWT.NONE);
		column.getColumn().setMoveable(true);
		column.getColumn().setResizable(false);
		column.getColumn().setText("Hint");
		column.getColumn().setAlignment(SWT.LEFT);
		
		column.setLabelProvider(new NdefRecordModelHintColumnProvider());
		
		column.getColumn().pack();
				
		treeViewer.setContentProvider(new NdefRecordModelContentProvider());

		treeViewer.setInput(operator.getModel());
		
		new NdefRecordModelMenuListener(treeViewer, this, ndefMultiPageEditor, operator.getModel());
		
		treeViewer.expandAll();

		// we want the last column to 'fill' with the layout
		// trigger at key points:
		// first show
		composite.getDisplay().asyncExec(
				new Runnable()
				{
					public void run()
					{
						packAndFillLastColumn();
					}
				}
				);

		// resize
		treeViewer.getTree().addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
               	packAndFillLastColumn();
			}
		});

		// manual resizing of columns
		Tree tree = treeViewer.getTree();
		for (int i = 0; i < tree.getColumnCount() - 1; i++) {
			tree.getColumn(i).addControlListener(new ControlAdapter() {
				public void controlResized(ControlEvent e) {
	               	packAndFillLastColumn();
				}
			});
		}
		
		// drag and drop
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
			 * Check out what kind of (visual) insert feedback to give user in GUI while dragging: insert before or after.
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
							// ignore node
						}
					} else {
						// ignore node
					}
				} else if(item != null) {
					// ignore item
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
								
								modified(true);
							} else if (pt.y > bounds.y + 2 * bounds.height / 3) {
								Activator.info("Drop " + source + " after " + node);
								
								operator.move(source, node.getParent(), node.getParentIndex() + 1);
								
								modified(true);
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
	
	/**
	 * 
	 * Resize last column in tree viewer so that it fills the client area completely if extra space.
	 * 
	 */
	
	protected void packAndFillLastColumn() {
		Tree tree = treeViewer.getTree();
		int columnsWidth = 0;
		for (int i = 0; i < tree.getColumnCount() - 1; i++) {
			columnsWidth += tree.getColumn(i).getWidth();
		}
		TreeColumn lastColumn = tree.getColumn(tree.getColumnCount() - 1);
		lastColumn.pack();
		
		Rectangle area = tree.getClientArea();
		
		Point preferredSize = tree.computeSize(SWT.DEFAULT, SWT.DEFAULT);
	    int width = area.width - 2*tree.getBorderWidth();
		
	    if (preferredSize.y > area.height + tree.getHeaderHeight()) {
	        // Subtract the scrollbar width from the total column width
	        // if a vertical scrollbar will be required
	        Point vBarSize = tree.getVerticalBar().getSize();
	        width -= vBarSize.x;
	    }

	    // last column is packed, so that is the minimum. If more space is available, add it.
	    if(lastColumn.getWidth() < width - columnsWidth) {
	    	lastColumn.setWidth(width - columnsWidth);
	    }
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
		
		refreshStatusLine();
	}
	
	@Override
	public String getTitle() {
		return "NDEF";
	}

	public void undo() {
		operator.undo();

		modified(true);
		
		treeViewer.expandAll();
	}

	public void redo() {
		operator.redo();
		
		modified(true);
		
		treeViewer.expandAll();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		throw new RuntimeException("Not implemented");
	}

	public void refresh() {
		treeViewer.refresh();
	}

	public void setStatus(String string) {
		IActionBars actionBars = getEditorSite().getActionBars(); 

		if( actionBars == null ) {
			return ;
		}

		IStatusLineManager statusLineManager = actionBars.getStatusLineManager();

		if( statusLineManager == null ) {
			return ;
		}

		statusLineManager.setMessage(string);
	}

	
	@Override
	public void dispose() {
		super.dispose();
		
		if(NdefTerminalWrapper.isAvailable()) {
			
			NdefTerminalListener ndefTerminalListener = NdefTerminalWrapper.getNdefTerminalListener();
			
			if(ndefTerminalListener != null) {
				if(ndefTerminalListener == ndefMultiPageEditor) {
					NdefTerminalWrapper.setNdefTerminalListener(null);
				}
			}
		}

	}
	

}
