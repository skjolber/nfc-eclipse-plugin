package com.antares.nfc.plugin;

import org.eclipse.ui.IStartup;

/**
 * This is a startup hook for detecting NFC terminals. There should be some option to disable this TODO.
 * 
 * @author thomas
 *
 */

public class Startup implements IStartup {

	public void earlyStartup() {
		
		// make sure no weird classloading effects and such
		try {
			com.antares.nfc.terminal.NdefTerminalDetector.initialize();
			com.antares.nfc.terminal.NdefTerminalDetector detector = com.antares.nfc.terminal.NdefTerminalDetector.getInstance();
			detector.startDetecting();
		} catch(Throwable e) {
			// assume some classloading issue
		}
	}

}
