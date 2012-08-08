package com.antares.nfc.terminal;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.nfctools.NfcAdapter;
import org.nfctools.mf.classic.MfClassicNfcTagListener;
import org.nfctools.mf.ul.Type2NfcTagListener;
import org.nfctools.ndef.NdefContext;
import org.nfctools.ndef.NdefListener;
import org.nfctools.ndef.NdefOperations;
import org.nfctools.ndef.NdefOperationsListener;
import org.nfctools.ndef.Record;
import org.nfctools.scio.Terminal;
import org.nfctools.scio.TerminalHandler;
import org.nfctools.scio.TerminalMode;
import org.nfctools.scio.TerminalStatus;
import org.nfctools.scio.TerminalStatusListener;
import org.nfctools.spi.acs.AcsTerminal;
import org.nfctools.spi.scm.SclTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.antares.nfc.plugin.Activator;
import com.antares.nfc.plugin.NdefMultiPageEditor;

public class NdefTerminalDetector implements Runnable, NdefListener, NdefOperationsListener, TerminalStatusListener {

	private Logger log = LoggerFactory.getLogger(getClass());
	
	private static NdefTerminalDetector detector;

	public static boolean initialize() {
		try {
			detector = new NdefTerminalDetector();
			
			return true;
		} catch(Throwable e) {
			e.printStackTrace();
			
			// ignore
		}
		return false;
	}
	
	public static NdefTerminalDetector getInstance() {
		return detector;
	}
	
	private Terminal currentTerminal;
	
	private TerminalHandler terminalHandler;
	
	private NfcAdapter nfcAdapter;
	
	private boolean close = false;
	
	public NdefTerminalDetector() {
		terminalHandler = new TerminalHandler();
		terminalHandler.addTerminal(new AcsTerminal());
		terminalHandler.addTerminal(new SclTerminal());
	}
	
	public boolean detectTerminal() {
		
		synchronized(this) {
			Terminal terminal = terminalHandler.getAvailableTerminal();
			
			if(currentTerminal == terminal) {
				return false;
			}
			
			if(currentTerminal != null) {
				stopReader();
			}
			
			if(terminal != null) {
				startReader(terminal);
			}
			return true;
		}

	}

	private void startReader(Terminal terminal) {
		synchronized(this) {
			log("Starting terminal " + terminal.getTerminalName());
			
			currentTerminal = terminal;
			currentTerminal.setStatusListener(this);
			nfcAdapter = new NfcAdapter(terminal, TerminalMode.INITIATOR);
			
			nfcAdapter.registerTagListener(new MfClassicNfcTagListener(this));
			nfcAdapter.registerTagListener(new Type2NfcTagListener(this));
			nfcAdapter.startListening();
		}
	}

	private void log(String message) {
		Activator activator = Activator.getDefault();		
		
		if(activator != null) {
			Activator.info(message);
		} else {
			log.info(message);
		}
			
	}

	private void stopReader() {
		synchronized(this) {
			log("Stopping terminal " + currentTerminal.getTerminalName());
			if(nfcAdapter != null) {
				nfcAdapter.stopListening();
				
				nfcAdapter = null;
			}
			currentTerminal = null;
		}
	}
	
	public void startDetecting() {
		log("Start detecting card terminals");
		
		Thread thread = new Thread(this);
		thread.start();
	}
	
	public void stopDetecting() {
		log("Stop detecting card terminals");
		
		close = true;
	}

	@Override
	public void run() {
		while(!close) {
			try {
				detectTerminal();
			} catch(IllegalArgumentException e) {
				// ignore
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				
			}
		}
	}
	
	@Override
	public void onNdefMessages(final Collection<Record> records) {
		System.out.println("Got records");

		Display.getDefault().asyncExec(new Runnable() {
			public void run()
			{
				byte[] encode = NdefContext.getNdefMessageEncoder().encode(records);

				System.out.println("Encoded " + encode.length + " records");

				IStorage storage = new NdefTerminalStorage(encode, currentTerminal.getTerminalName()); // TODO add file name counter
				IStorageEditorInput input = new NdefTerminalInput(storage, currentTerminal.getTerminalName());

				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

				if (page != null)
					try {
						page.openEditor(input, NdefMultiPageEditor.class.getName());

						System.out.println("Opened");
					} catch (PartInitException e) {
						e.printStackTrace();
					}


				/*
						try {

                    	File fileToOpen = File.createTempFile("eclipse", ".ndef");

                    	FileOutputStream fout = new FileOutputStream(fileToOpen);
                    	fout.write(encode);
                    	fout.close();

                    	System.out.println("Wrote to file " + fileToOpen);

                    	IWorkspace workspace= ResourcesPlugin.getWorkspace(); 
                    	 IPath location= Path.fromOSString(fileToOpen.getAbsolutePath()); 
                    	 IFile inputFile= workspace.getRoot().getFileForLocation(location);

                    	if (inputFile != null) {
                    	    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

                    	    IEditorPart openEditor = IDE.openEditor(page, inputFile);

                    	    System.out.println("Open editor " + openEditor.getClass().getName());
                    	} else {
                    		System.out.println("No input file");
                    	}

						} catch (Exception e) {
							e.printStackTrace();
						}


/*                    	
                    	if (fileToOpen.exists() && fileToOpen.isFile()) {
                    	    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

                    	    IEditorDescriptor desc = PlatformUI.getWorkbench().
                    	            getEditorRegistry().getDefaultEditor(fileToOpen.getName());
                    	    page.openEditor(new FileEditorInput(fileToOpen), desc.getId());
                    	} else {
                    	    //Do something if the file does not exist
                    	}
				 */


			}
		}
				);



	}

	// http://wiki.eclipse.org/FAQ_How_do_I_open_an_editor_programmatically%3F
	// http://wiki.eclipse.org/FAQ_How_do_I_open_an_editor_on_something_that_is_not_a_file%3F
	// http://eclipsesnippets.blogspot.no/2008/06/programmatically-opening-editor.html
	// http://stackoverflow.com/questions/171824/programmatically-showing-a-view-from-an-eclipse-plug-in
	@Override
	public void onNdefOperations(NdefOperations ndefOperations) {
		if (ndefOperations.isFormatted()) {
			if (ndefOperations.hasNdefMessage())
				onNdefMessages(ndefOperations.readNdefMessage());
			else
				log.info("Empty formatted tag. Size: " + ndefOperations.getMaxSize() + " bytes");
		}
		else
			log.info("Empty tag. NOT formatted. Size: " + ndefOperations.getMaxSize() + " bytes");
	}

	@Override
	public void onStatusChanged(TerminalStatus status) {
		if(status == TerminalStatus.CLOSED) {
			stopReader();
		}
	}

}
