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

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;


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
