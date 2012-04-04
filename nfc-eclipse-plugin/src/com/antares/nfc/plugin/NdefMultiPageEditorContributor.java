package com.antares.nfc.plugin;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;

public class NdefMultiPageEditorContributor extends MultiPageEditorActionBarContributor {
	
	private IEditorPart activeEditorPart;
	
	public void setActivePage(IEditorPart part) {
		if (activeEditorPart != part) {
			activeEditorPart = part;
			
			if(activeEditorPart instanceof NdefEditorPart) {
			
				IActionBars actionBars = getActionBars();
				if (actionBars != null) {
		
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
					
					actionBars.updateActionBars();
					*/
	
				}
			}
		}
	}
	
}
