package com.antares.nfc.terminal;

import java.util.List;

import org.nfctools.ndef.Record;


/**
 * 
 * Interface for editor read/write and state information
 * 
 * @author thomas
 *
 */

public interface NdefTerminalListener {

	public enum Type {
		READ, WRITE, NONE, READ_WRITE;
	}
	
	List<Record> getNdefRecords();
	
	void setNdefContent(List<Record> content);
	
	Type getType();
	
	void setType(Type type);
}
