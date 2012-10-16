package org.nfc.eclipse.plugin;

import org.nfctools.NfcAdapter;
import org.nfctools.mf.classic.MfClassicNfcTagListener;
import org.nfctools.mf.ul.Type2NfcTagListener;
import org.nfctools.scio.Terminal;
import org.nfctools.scio.TerminalHandler;
import org.nfctools.scio.TerminalMode;
import org.nfctools.spi.acs.AcsTerminal;
import org.nfctools.spi.scm.SclTerminal;
import org.nfctools.utils.LoggingNdefListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestConnection extends Thread {

	private Logger log = LoggerFactory.getLogger(getClass());

	private Terminal terminal;

	public TestConnection() {
		TerminalHandler terminalHandler = new TerminalHandler();
		terminalHandler.addTerminal(new AcsTerminal());
		terminalHandler.addTerminal(new SclTerminal());
		
		terminal = terminalHandler.getAvailableTerminal();
		
		log.info("Connected to " + terminal.getTerminalName());

	}

	public String getTerminalName() {
		return terminal.getTerminalName();
	}

	public void run() {

		NfcAdapter nfcAdapter = new NfcAdapter(terminal, TerminalMode.INITIATOR);
		
		try {
			nfcAdapter.registerTagListener(new MfClassicNfcTagListener(new LoggingNdefListener()));
			nfcAdapter.registerTagListener(new Type2NfcTagListener(new LoggingNdefListener()));
			nfcAdapter.startListening();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		TestConnection service = new TestConnection();
		service.run();
		
		try {
			Thread.sleep(Long.MAX_VALUE); // the service terminates if this thread terminates
		} catch (InterruptedException e) {
		}
	}

	

}