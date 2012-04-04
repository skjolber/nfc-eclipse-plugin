
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
