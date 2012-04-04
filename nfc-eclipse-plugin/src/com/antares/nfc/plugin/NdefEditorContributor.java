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

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.EditorActionBarContributor;

public class NdefEditorContributor extends EditorActionBarContributor {
	
	private NdefEditorPart fCurrentEditor;

	/**
	 * 
	 */
	public NdefEditorContributor() {
		super();
	}
	
	public void init(IActionBars bars) {
		super.init(bars);
		
		/* TODO
		Action undoAction = new Action() {
		    public void run() {
		    	if (fCurrentEditor != null)
		    		fCurrentEditor.undo();
		    }
		};
		bars.setGlobalActionHandler(ActionFactory.UNDO.getId(), undoAction);

		Action redoAction = new Action() {
		    public void run() {
		    	if (fCurrentEditor != null)
		    		fCurrentEditor.redo();
		    }
		};
		bars.setGlobalActionHandler(ActionFactory.REDO.getId(), redoAction);
		*/
	}

	public void setActiveEditor(IEditorPart targetEditor) {
		super.setActiveEditor(targetEditor);
		
		if (!(targetEditor instanceof NdefEditorPart))
			return;
		fCurrentEditor = (NdefEditorPart) targetEditor;

		getActionBars().updateActionBars();
	}
}
