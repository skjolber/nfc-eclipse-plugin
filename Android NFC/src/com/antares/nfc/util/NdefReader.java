package com.antares.nfc.util;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Parcelable;

/**
 * 
 * NFC tag reader of NDEF format.
 * 
 * @author Thomas Rorvik Skjolberg
 *
 */


public class NdefReader {

	public static interface NdefReaderListener {

		void readNdefMessages(NdefMessage[] messages);
		
		void readNdefEmptyMessage();

		void readNonNdefMessage();

	}
	
	private NdefReaderListener listener;
	
	public NdefReaderListener getListener() {
		return listener;
	}

	public void setListener(NdefReaderListener listener) {
		this.listener = listener;
	}

	public boolean read(Intent intent) {
		Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		if (messages != null) {
			NdefMessage[] ndefMessages = new NdefMessage[messages.length];
		    for (int i = 0; i < messages.length; i++) {
		        ndefMessages[i] = (NdefMessage) messages[i];
		    }
		    
		    if(ndefMessages.length > 0) {
				listener.readNdefMessages(ndefMessages);
				
				return true;
		    } else {
		    	listener.readNdefEmptyMessage();
		    }
		} else  {
			listener.readNonNdefMessage();
		}
		
		return false;
	}

}
