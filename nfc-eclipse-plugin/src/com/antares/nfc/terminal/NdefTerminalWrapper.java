package com.antares.nfc.terminal;

import org.nfctools.ndef.NdefOperations;

/**
 * 
 * Wrapper for terminal logic - card terminal is part of the javax packages and cannot be expected to always be present.
 * 
 * @author thomas
 *
 */

public class NdefTerminalWrapper {

	public static boolean isAvailable() {
		try {
			return com.antares.nfc.terminal.NdefTerminalDetector.getInstance() != null;
		} catch(Exception e) {
			return false;
		}
	}
	

	public static NdefTerminalListener getNdefTerminalListener() {
		try {
			com.antares.nfc.terminal.NdefTerminalDetector detector = com.antares.nfc.terminal.NdefTerminalDetector.getInstance();
	
			if(detector != null) {
				return detector.getNdefTerminalListener();
			}
		} catch(Exception e) {
			// ignore
		} 
		return null;
	}

	public static void setNdefTerminalListener(NdefTerminalListener ndefTerminalListener) {
		try {
			com.antares.nfc.terminal.NdefTerminalDetector detector = com.antares.nfc.terminal.NdefTerminalDetector.getInstance();
	
			if(detector != null) {
				detector.setNdefTerminalListener(ndefTerminalListener);
			}
		} catch(Exception e) {
			// ignore
		} 
	}

	public static String getTerminalName() {
		try {
			com.antares.nfc.terminal.NdefTerminalDetector detector = com.antares.nfc.terminal.NdefTerminalDetector.getInstance();
	
			if(detector != null) {
				return detector.getTerminalName();
			}
		} catch(Exception e) {
			// ignore
		} 
		return null;
	}


	public static NdefOperations getNdefOperations() {
		try {
			com.antares.nfc.terminal.NdefTerminalDetector detector = com.antares.nfc.terminal.NdefTerminalDetector.getInstance();
	
			if(detector != null) {
				return detector.getNdefOperations();
			}
		} catch(Exception e) {
			// ignore
		} 
		return null;
	}


	public static boolean hasFoundTerminal() {
		try {
			com.antares.nfc.terminal.NdefTerminalDetector detector = com.antares.nfc.terminal.NdefTerminalDetector.getInstance();
	
			if(detector != null) {
				return detector.hasFoundTerminal();
			}
		} catch(Exception e) {
			// ignore
		} 
		return false;
	}

}
