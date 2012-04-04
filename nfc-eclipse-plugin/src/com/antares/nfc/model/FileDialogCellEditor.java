package com.antares.nfc.model;

/*******************************************************************************
 * Copyright (c) 2006, 2009 Eric Rizzo and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eric Rizzo - initial implementation
 *******************************************************************************/

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;

/**
 * A CellEditor that is a blending of DialogCellEditor and TextCellEditor. The user can either type
 * directly into the Text or use the button to open a Dialog for editing the cell's value.
 * 
 */
public class FileDialogCellEditor extends CellEditor {

	private Label defaultLabel;
	private String value;
	
	public FileDialogCellEditor(Composite parent) {
		super(parent);
	}

	protected String openDialogBox(Control cellEditorWindow) {
		
		// File standard dialog
		FileDialog fileDialog = new FileDialog(cellEditorWindow.getShell());
		// Set the text
		fileDialog.setText("Select File");
		// Set filter
		//fileDialog.setFilterExtensions(new String[] { "*.*" });
		// Put in a readable name for the filter
		//fileDialog.setFilterNames(new String[] { "All files" });
		// Open Dialog and save result of selection
		return fileDialog.open();

	}

	@Override
	protected Control createControl(Composite cell) {
		defaultLabel = new Label(cell, SWT.LEFT);
		defaultLabel.setFont(cell.getFont());
		defaultLabel.setBackground(cell.getBackground());
		
		return defaultLabel;
	}

	@Override
	protected Object doGetValue() {
		return value;
	}

	@Override
	protected void doSetFocus() {
		defaultLabel.setFocus();

		// schedule opening dialog box after the containing call is complete
    	defaultLabel.getDisplay().asyncExec(
            new Runnable()
            {
                public void run()
                {
            		value = openDialogBox(defaultLabel);
            		
            		if(value != null) {
            			fireApplyEditorValue();
            		} else {
            			fireCancelEditor();
            		}
                }
            }
        );
	}
	
	@Override
	protected void doSetValue(Object object) {
		defaultLabel.setText(object.toString());
	}
	
}
