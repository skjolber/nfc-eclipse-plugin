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


import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

public class NdefMultiPageEditor extends MultiPageEditorPart implements IResourceChangeListener {

	private NdefEditorPart ndefEditor;
	private NdefQREditorPart ndefQREditor;
	private Label binaryQRLabel;
	
	private NdefModelOperator modelOperator;
	
	public NdefMultiPageEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	public void createBinaryQRPage() {
		Composite composite = new Composite(getContainer(), SWT.NONE);
		composite.setBackground(new Color(Display.getDefault(), 0xFF, 0xFF, 0xFF));
		

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		
		composite.setLayout(gridLayout);

		binaryQRLabel = new Label(composite, SWT.NONE);
		
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 2;
		binaryQRLabel.setLayoutData(gridData);

		
		binaryQRLabel.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
               	refreshBinaryQR();
			}
		});

		int index = addPage(composite);
		setPageText(index, "QR");
	}

	public void createNdefQREditorPage() {
		try {
			ndefQREditor = new NdefQREditorPart(modelOperator);
			int index = addPage(ndefQREditor, getEditorInput());
			setPageText(index, ndefQREditor.getTitle());
		} catch (PartInitException e) {
			ErrorDialog.openError(
				getSite().getShell(),
				"Error creating NDEF QR editor",
				null,
				e.getStatus());
		}
	}

	public void createNdefEditorPage() {
		try {
			ndefEditor = new NdefEditorPart(modelOperator);
			int index = addPage(ndefEditor, getEditorInput());
			setPageText(index, ndefEditor.getTitle());
		} catch (PartInitException e) {
			ErrorDialog.openError(
				getSite().getShell(),
				"Error creating NDEF editor",
				null,
				e.getStatus());
		}
	}
	
	/**
	 * Creates the pages of the multi-page editor.
	 */
	protected void createPages() {
		createNdefQREditorPage();
		createNdefEditorPage();
		createBinaryQRPage();
	}
	
	/**
	 * The <code>MultiPageEditorPart</code> implementation of this 
	 * <code>IWorkbenchPart</code> method disposes all nested editors.
	 * Subclasses may extend.
	 */
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}
	/**
	 * Saves the multi-page editor's document.
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		
		IEditorInput input = getEditorInput();
		
		IFile iFile = null;
		File file = null;
		
		if (input instanceof IFileEditorInput) {
			//
			// Input file found in Eclipse Workspace - good
			//
			iFile = ((IFileEditorInput) input).getFile();
			file = iFile.getRawLocation().toFile();
		} else if (input instanceof IPathEditorInput) {
			//
			// Input file is outside the Eclipse Workspace
			//
			IPathEditorInput pathEditorInput = (IPathEditorInput) input;
			IPath path = pathEditorInput.getPath();
			file = path.toFile();
		} else if (input instanceof IURIEditorInput) {
			//
			// Input file is outside the Eclipse Workspace
			//
			IURIEditorInput uriEditorInput = (IURIEditorInput) input;
			URI uri = uriEditorInput.getURI();
			file = new File(uri);
		} else {
			// unknown file type
		} 

		if(file != null) {

			try {
				modelOperator.save(file);

				ndefEditor.setDirty(false);
				ndefQREditor.setDirty(false);
				
				monitor.done();
			} catch(IOException e) {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				MessageDialog.openError(shell, "Error", "Error writing file '" + file + "', " + e.toString());
			}

			if (iFile != null) {
				try {
					iFile.refreshLocal(IResource.DEPTH_ONE, monitor);
				} catch (Exception e) {
					// ignore
				}
			}
		}		
		
	}

	public void doSaveAs() {
		throw new RuntimeException();
	}
	
	/* (non-Javadoc)
	 * Method declared on IEditorPart
	 */
	public void gotoMarker(IMarker marker) {
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
	}
	
	public void init(IEditorSite site, IEditorInput editorInput)
		throws PartInitException {
		
		super.init(site, editorInput);
		
		Activator.info("Initialize");
		
	}
	
	public void setInput(IEditorInput input) {
		
		super.setInput(input);
		
		File file = null;

		if (input instanceof IFileEditorInput) {
			IFile iFile = ((IFileEditorInput) input).getFile();
			
			file = iFile.getRawLocation().toFile();
		} else if (input instanceof IPathEditorInput) {
			IPathEditorInput pathEditorInput = (IPathEditorInput) input;
			IPath path = pathEditorInput.getPath();

			file = path.toFile();

		} else if (input instanceof IURIEditorInput) {
			//
			// Input file is outside the Eclipse Workspace
			//
			IURIEditorInput uriEditorInput = (IURIEditorInput) input;
			URI uri = uriEditorInput.getURI();
			file = new File(uri);
		} else {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			MessageDialog.openError(shell, "Error", "Could not read input " + input);
			
			return;
		}
		
		// get the project path file
		File projectPath = null;
		
		search:
		if (input instanceof IPathEditorInput) {
			IPathEditorInput pathEditorInput = (IPathEditorInput) input;
			IPath path = pathEditorInput.getPath();
			for(int i = 0; i < path.segmentCount(); i++) {
				if(path.segment(i).equals("runtime-EclipseApplication")) {
					projectPath = path.uptoSegment(path.segmentCount() - i + 1).toFile();
					
					break search;
				}
			}

			IWorkspace workspace = ResourcesPlugin.getWorkspace();  
			  
			//get location of workspace (java.io.File)  
			File workspaceDirectory = workspace.getRoot().getLocation().toFile();

			File parent = path.toFile();
			do {
				
				File nextParent = parent.getParentFile();
				
				if(workspaceDirectory.equals(nextParent)) {
					projectPath = parent;
					
					break search;
				}
				
				parent = nextParent;
			} while(parent != null);

			// no path found
		} else {
			// no path found
		}
		
		if(projectPath != null) {
			modelOperator = new NdefModelOperator(projectPath);
		} else {
			modelOperator = new NdefModelOperator(null);
		}
		if(file.exists()) {
			try {
				if(!modelOperator.load(file)) {
					modelOperator.newModel();
				}
			} catch(IOException e) {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				MessageDialog.openError(shell, "Error", "Could not read file '" + file + "'");
				
				return;
			}
		} else {
			modelOperator.newModel();
		}
		
		setPartName(file.getName());
	}
	
	
	/* (non-Javadoc)
	 * Method declared on IEditorPart.
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	
	/**
	 * Calculates the contents of page 2 when the it is activated.
	 */
	protected void pageChange(int newPageIndex) {
		Activator.info("Change to page " + newPageIndex);
		super.pageChange(newPageIndex);
		if(newPageIndex == 0) {
			ndefQREditor.refresh();
		} else if(newPageIndex == 1) {
			ndefEditor.refresh();
		} else if (newPageIndex == 2) {
			refreshBinaryQR();
		}
	}
	
	public void refreshBinaryQR() {

		Point size = binaryQRLabel.getSize();

		try {
			binaryQRLabel.setImage(modelOperator.toBinaryQRImage(size.x, size.y, 0, 0));
		} catch (Exception e) {
			// TODO error message
			binaryQRLabel.setImage(null);
		}
	}
	
	/**
	 * Closes all project files on project close.
	 */
	
	public void resourceChanged(final IResourceChangeEvent event){
		if(event.getType() == IResourceChangeEvent.PRE_CLOSE){
			Display.getDefault().asyncExec(new Runnable(){
				public void run(){
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
					for (int i = 0; i<pages.length; i++){
						if(((FileEditorInput)ndefEditor.getEditorInput()).getFile().getProject().equals(event.getResource())){
							IEditorPart editorPart = pages[i].findEditor(ndefEditor.getEditorInput());
							pages[i].closeEditor(editorPart,true);
						}
					}
				}            
			});
		}
	}

	@Override
	public boolean isDirty() {
		return ndefEditor.isDirty() || ndefQREditor.isDirty();
	}

	
}
