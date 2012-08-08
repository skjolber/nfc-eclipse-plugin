package com.antares.nfc.terminal;

/**
 * 
 * 
 * 
 * @author thomas
 *
 */

public interface NdefTerminalListener {

	public enum Type {
		READ, WRITE, NONE;
	}
	
	byte[] getNdefContent();
	
	void setNdefContent(byte[] content);
	
	Type getType();
}
