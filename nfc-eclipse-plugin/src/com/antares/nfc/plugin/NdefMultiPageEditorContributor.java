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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;

public class NdefMultiPageEditorContributor extends MultiPageEditorActionBarContributor {
	
	private IEditorPart activeEditorPart;
	
	private StatusLineContributionItem statusLineContributionItem;
	
	public NdefMultiPageEditorContributor() {
		super();
	}
	
	@Override
	public void init(IActionBars bars) {
		super.init(bars);
	}

	@Override
	public void contributeToStatusLine(IStatusLineManager statusLineManager) {
		super.contributeToStatusLine(statusLineManager);

		Activator.info("Contribute to status line");

		if(statusLineContributionItem == null) {
			StatusLineContributionItem statusLineContributionItem = new StatusLineContributionItem(getClass().getName()+".size");

			statusLineManager.add(statusLineContributionItem);
		}
	}
	
	public void setActivePage(IEditorPart part) {
		if (activeEditorPart != part) {
			activeEditorPart = part;
			
			if(activeEditorPart instanceof NdefEditorPart) {
				final NdefEditorPart ndefEditorPart = (NdefEditorPart)activeEditorPart;
				
				IActionBars actionBars = getActionBars();
				if (actionBars != null) {
		
					
					Action undoAction = new Action() {
					    public void run() {
					    	if (ndefEditorPart != null)
					    		ndefEditorPart.undo();
					    }
					};
					actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(), undoAction);
			
					Action redoAction = new Action() {
					    public void run() {
					    	if (ndefEditorPart != null)
					    		ndefEditorPart.redo();
					    }
					};
					actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(), redoAction);
					
					actionBars.updateActionBars();
					
	
				}
			}
		}
	}
	
}
