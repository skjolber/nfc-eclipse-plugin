package com.antares.nfc.plugin;

import org.eclipse.ui.IStartup;

import com.antares.nfc.terminal.NdefTerminalDetector;

/**
 * This is a startup hook for detecting NFC terminals. There should be some option to disable this TODO.
 * 
 * @author thomas
 *
 */

public class Startup implements IStartup {

	public void earlyStartup() {
		if(NdefTerminalDetector.initialize()) {
			NdefTerminalDetector detector = NdefTerminalDetector.getInstance();
			detector.startDetecting();
		}
	}

}
