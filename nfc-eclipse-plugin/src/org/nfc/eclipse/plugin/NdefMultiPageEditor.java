/***************************************************************************
 *
 * This file is part of the NFC Eclipse Plugin project at
 * http://code.google.com/p/nfc-eclipse-plugin/
 *
 * Copyright (C) 2012 by Thomas Rorvik Skjolberg.
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

package org.nfc.eclipse.plugin;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableEditor;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.nfc.eclipse.plugin.terminal.NdefTerminalInput;
import org.nfc.eclipse.plugin.terminal.NdefTerminalListener;
import org.nfc.eclipse.plugin.terminal.NdefTerminalStorage;
import org.nfctools.ndef.Record;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;


public class NdefMultiPageEditor extends MultiPageEditorPart implements IResourceChangeListener, NdefTerminalListener, IPersistableEditor {

	private NdefEditorPart ndefEditor;
	private NdefQREditorPart ndefQREditor;
	private Label binaryQRLabel;
	
	private NdefModelOperator modelOperator;
	
	private NdefRecordFactory ndefRecordFactory = new NdefRecordFactory();
	
	private NdefTerminalListener.Type type;
	
	protected boolean dirty = false;
	
	protected boolean saveEditorPartPreference = false;
	
	protected int previousEditorIndex = -1;
	
	public NdefMultiPageEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		
		this.previousEditorIndex = getEditorPartPreference();
	}
	
	public void createBinaryQRPage() {
		Composite composite = new Composite(getContainer(), SWT.NONE);
		composite.setBackground(new Color(Display.getDefault(), 0xFF, 0xFF, 0xFF));

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		
		composite.setLayout(gridLayout);

		binaryQRLabel = new Label(composite, SWT.NONE);
		binaryQRLabel.setBackground(new Color(Display.getDefault(), 0xFF, 0xFF, 0xFF));

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
			ndefQREditor = new NdefQREditorPart(modelOperator, this);
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
			ndefEditor = new NdefEditorPart(modelOperator, this);
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
		Activator.info("Create pages");
		createNdefEditorPage();
		createNdefQREditorPage();
		createBinaryQRPage();
		
		if(previousEditorIndex != -1) {
			Activator.info("Set active page " + previousEditorIndex);
			
			setActivePage(previousEditorIndex);
			
		} else {
			setActivePage(0);
		}
		saveEditorPartPreference = true;
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
		} else if(input instanceof IStorageEditorInput) {
			IStorageEditorInput iStorageEditorInput = (IStorageEditorInput)input;

			try {
				IPath fullPath = iStorageEditorInput.getStorage().getFullPath();
				if(fullPath == null) {
					Activator.info("Open file save as dialog");
					
					file = openSaveDialog();
					
					if(file == null) {
						return;
					} else {
						setPartName(file.getName());
						
						if(iStorageEditorInput instanceof NdefTerminalInput) {
							NdefTerminalInput ndefTerminalInput = (NdefTerminalInput)iStorageEditorInput;
							
							IStorage storage = ndefTerminalInput.getStorage();
							if(storage instanceof NdefTerminalStorage) {
								NdefTerminalStorage ndefTerminalStorage = (NdefTerminalStorage)storage;
								
								ndefTerminalStorage.setFullPath(new Path(file.getAbsolutePath()));
							}
						}
					}
				}  else {
					file = fullPath.toFile();
				}
			} catch (CoreException e) {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				MessageDialog.openError(shell, "Error", "Error writing file '" + file + "', " + e.toString());

			}
		} else {
			// unknown file type
		} 

		if(file != null) {

			try {
				modelOperator.save(file);

				setDirty(false);
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

	private File openSaveDialog() {
		// File standard dialog
		FileDialog fileDialog = new FileDialog(getContainer().getShell(), SWT.SAVE);
		// Set the text
		fileDialog.setText("Save file");
		// Set filter
		String [] filterNames = new String [] {"NDEF Files", "All Files"};
		String [] filterExtensions = new String [] {"*.ndef", "*"};
		
		String platform = SWT.getPlatform();
		if (platform.equals("win32") || platform.equals("wpf")) {
			filterExtensions[1] = "*.*";
		}
		
		fileDialog.setFilterNames (filterNames);
		fileDialog.setFilterExtensions (filterExtensions);
		
		// Open Dialog and save result of selection
		String file = fileDialog.open();

		if(file != null) {
			return new File(file);
		}
		return null;
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
		InputStream contents = null;
		
		try {
			if (input instanceof IFileEditorInput) {
				IFile iFile = ((IFileEditorInput) input).getFile();
				
				file = iFile.getRawLocation().toFile();
				
				if(file.exists()) {
					contents = iFile.getContents();
				}
			} else if (input instanceof IPathEditorInput) {
				IPathEditorInput pathEditorInput = (IPathEditorInput) input;
				IPath path = pathEditorInput.getPath();
	
				file = path.toFile();
				
				if(file.exists()) {
					contents = new FileInputStream(file);
				}
			} else if (input instanceof IURIEditorInput) {
				//
				// Input file is outside the Eclipse Workspace
				//
				IURIEditorInput uriEditorInput = (IURIEditorInput) input;
				URI uri = uriEditorInput.getURI();
				file = new File(uri);
				
				if(file.exists()) {
					contents = new FileInputStream(file);
				}
			} else if(input instanceof IStorageEditorInput) {
				IStorageEditorInput iStorageEditorInput = (IStorageEditorInput)input;
				
				if(iStorageEditorInput.exists()) {
					contents = iStorageEditorInput.getStorage().getContents();
				}
			} else {
				throw new IllegalArgumentException("Unsupported input type " + input.getClass().getName());
			}
		} catch (Exception e) {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			MessageDialog.openError(shell, "Error", "Could not read input " + input);
			
			return;
		}
		
		// get the project path file for auto-detect of AAR package name
		File projectPath = findProjectRoot(input);
		
		if(projectPath != null) {
			ndefRecordFactory.setProjectPath(projectPath);
		}
		modelOperator = new NdefModelOperator(ndefRecordFactory);
		
		if(contents != null) {
			try {
				if(modelOperator.load(contents)) {
					setDirty(true);
				}
				
				// if this came from the terminal, saving should be possible straight away
				if(input instanceof NdefTerminalInput) {
					setDirty(true);
				}
			} catch(IOException e) {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				MessageDialog.openError(shell, "Error", "Could not read file '" + file + "'");
				
				return;
			} finally {
				if(contents != null) {
					try {
						contents.close();
					} catch(IOException e) {
						// ignore
					}
				}
			}
		} else {
			modelOperator.newModel();
		}
		
		setPartName(input.getName());
	}

	private File findProjectRoot(IEditorInput input) {
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
		return projectPath;
	}
	
	
	/* (non-Javadoc)
	 * Method declared on IEditorPart.
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	
	/**
	 * Refresh page views according to the model when activated.
	 */
	protected void pageChange(int newPageIndex) {
		Activator.info("Change to page " + newPageIndex);
		super.pageChange(newPageIndex);
		if(newPageIndex == 1) {
			ndefQREditor.refresh();
		} else if(newPageIndex == 0) {
			ndefEditor.refresh();
		} else if (newPageIndex == 2) {
			refreshBinaryQR();
		}
		
		if(saveEditorPartPreference) {
			setEditorPartPreference(newPageIndex);	
		}
	}
	
	
	
	public void refreshBinaryQR() {
		modelOperator.refreshBinaryQR(binaryQRLabel);
	}
	
	/**
	 * Closes all project files on project close.
	 */
	
	public void resourceChanged(final IResourceChangeEvent event){
		Activator.info(event.getClass().getName() + " " + event.getType());
		
		final IEditorInput input=getEditorInput();
	    if (!( input instanceof IFileEditorInput )) {
	           return;
	    }
	    final IFile jmolfile=((IFileEditorInput)input).getFile();
	       
		if(event.getType() == IResourceChangeEvent.PRE_CLOSE){
			Activator.info("Pre close");
			Display.getDefault().asyncExec(new Runnable(){
				public void run(){
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
					for (int i = 0; i < pages.length; i++){
						
						if(jmolfile.getProject().equals(event.getResource())){
							IEditorPart editorPart = pages[i].findEditor(ndefEditor.getEditorInput());
							pages[i].closeEditor(editorPart,true);
						}
					}
				}            
			});
		} else if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
	        // Close editor if resource is deleted (or if any resource that is a parent is deleted)
			Activator.info("Post change");

			IResourceDelta rootDelta = event.getDelta();
			// get the delta, if any, for the documentation directory
			final List<IResource> deletedlist = new ArrayList<IResource>();

			IResourceDelta docDelta = rootDelta.findMember(jmolfile.getFullPath());
			if (docDelta != null) {
				IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
					public boolean visit(IResourceDelta delta) {
						// only interested in removal changes
						if(delta.getKind() == IResourceDelta.REMOVED) {
							deletedlist.add(delta.getResource());
						}
						
						Activator.info(delta.toString());
						return true;
					}
				};
				try {
					docDelta.accept(visitor);
				} catch (CoreException e) {
					Activator.error(Activator.PLUGIN_ID, e);
				}
			}

			if (deletedlist.size() > 0 && deletedlist.contains(jmolfile)) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						if (getSite() == null)
							return;
						if (getSite().getWorkbenchWindow() == null)
							return;

						IWorkbenchPage[] pages = getSite().getWorkbenchWindow()
								.getPages();
						for (int i = 0; i < pages.length; i++) {
							IEditorPart editorPart = pages[i].findEditor(input);
							pages[i].closeEditor(editorPart, true);
						}
					}
				});
			}
		}
	}

	@Override
	public boolean isDirty() {
		return dirty || ndefEditor.isDirty() || ndefQREditor.isDirty();
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
	public List<Record> getNdefRecords() {
		return modelOperator.getRecords();
	}

	@Override
	public void setNdefContent(final List<Record> content) {
		
    	Display.getDefault().asyncExec(
                new Runnable()
                {
                    public void run()
                    {
		
						modelOperator.setRecords(content);
						
						if(ndefEditor == getActiveEditor()) {
							ndefEditor.modified(type != Type.READ_WRITE);
						}
						
						if(ndefQREditor == getActiveEditor()) {
							ndefQREditor.modified(type != Type.READ_WRITE);
						}
						
						setDirty(true);
		
                    }
                }
            );


	}

	@Override
	public Type getType() {
		return type;
	}
	
	@Override
	public void setType(Type type) {
		this.type = type;
	}

	@Override
	public void restoreState(IMemento iMemento) {
		Integer version = iMemento.getInteger("version");
		if(version != null) {
			Activator.info("Restore state for version " + version);
			
			if(version.intValue() <= 1) {
				this.previousEditorIndex = iMemento.getInteger("activeEditorIndex");
			}
		}
	}

	@Override
	public void saveState(IMemento iMemento) {
		iMemento.putInteger("version", 1);

		iMemento.putInteger("activeEditorIndex", getActivePage());
	}
	
	public static int getEditorPartPreference() {
		Preferences preferences = ConfigurationScope.INSTANCE.getNode(Activator.class.getPackage().getName());
		Preferences reader = preferences.node("editor");

		return reader.getInt("activeEditorIndex", -1);
	}

	public static void setEditorPartPreference(int index) {
		Preferences preferences = ConfigurationScope.INSTANCE.getNode(Activator.class.getPackage().getName());
		Preferences reader = preferences.node("editor");

		if(reader.getInt("activeEditorIndex", -1) != index) {
			reader.putInt("activeEditorIndex", index);
			
			try {
				  // Forces the application to save the preferences
				  preferences.flush();
			} catch (BackingStoreException e) {
				e.printStackTrace();
			}
		}
	}

	
}
