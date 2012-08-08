package com.antares.nfc.terminal;

import java.util.Collection;

import org.eclipse.core.resources.IStorage;
import org.eclipse.swt.widgets.Display;
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
import com.antares.nfc.terminal.NdefTerminalListener.Type;

public class NdefTerminalDetector implements Runnable, NdefListener, NdefOperationsListener, TerminalStatusListener {

	private Logger log = LoggerFactory.getLogger(getClass());
	
	private static NdefTerminalDetector detector;

	public static void initialize() {
		detector = new NdefTerminalDetector();
	}
	
	public static NdefTerminalDetector getInstance() {
		return detector;
	}
	
	private Terminal currentTerminal;
	
	private TerminalHandler terminalHandler;
	
	private NfcAdapter nfcAdapter;
	
	private boolean close = false;
	
	/** did we ever see a terminal */
	private boolean foundTerminal = false;
	
	private NdefTerminalListener ndefTerminalListener;
	
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
				if(!foundTerminal) {
					foundTerminal = true;
				}
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
		
		final byte[] encode = NdefContext.getNdefMessageEncoder().encode(records);

		synchronized(this) {
			if(ndefTerminalListener != null) {
				Type type = ndefTerminalListener.getType();
				switch(type) {
				case NONE: {
					openNewEditor(encode);
					
					break;
				}
				case READ: {
					log("Read NDEF into open editor " + ndefTerminalListener.getClass().getSimpleName());
					
					ndefTerminalListener.setNdefContent(encode);
					
					break;
				}
				case WRITE: {
					log("Write NDEF from editor " + ndefTerminalListener.getClass().getSimpleName());

					byte[] ndefContent = ndefTerminalListener.getNdefContent();
					// TODO
					
					break;
				}
				}
			} else {
				openNewEditor(encode);
			}
		}
	}

	private void openNewEditor(final byte[] encode) {
		log("Open NDEF content in new editor");

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				IStorage storage = new NdefTerminalStorage(encode, currentTerminal.getTerminalName()); // TODO add file name counter
				IStorageEditorInput input = new NdefTerminalInput(storage, currentTerminal.getTerminalName());

				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

				if (page != null) {
					try {
						page.openEditor(input, NdefMultiPageEditor.class.getName());
					} catch (PartInitException e) {
						log(e.toString());

						// do nothing more
					}
				} else {
					log("No active page for opening editor");
				}
			}
		});
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

	public boolean hasFoundTerminal() {
		return foundTerminal;
	}

	public NdefTerminalListener getNdefTerminalListener() {
		return ndefTerminalListener;
	}

	public void setNdefTerminalListener(NdefTerminalListener ndefTerminalListener) {
		synchronized(this) {
			this.ndefTerminalListener = ndefTerminalListener;
		}
	}

	
}
