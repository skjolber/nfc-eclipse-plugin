package com.antares.nfc.client;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.antares.nfc.util.NdefReader;
import com.antares.nfc.util.NdefReader.NdefReaderListener;

/**
 * 
 * Activity for reading NFC tags.
 * 
 * @author Thomas Rorvik Skjolberg
 *
 */

public class NfcReaderActivity extends NfcDetectorActivity implements NdefReaderListener {

	private static final String TAG = NfcReaderActivity.class.getSimpleName();

	protected NdefReader reader;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.reader);
	}
	
	@Override
	protected void onNfcFeatureFound() {
		reader = new NdefReader();
		reader.setListener(this);
		
        toast(getString(R.string.nfcMessage));
	}

	@Override
	protected void onNfcFeatureNotFound() {
        toast(getString(R.string.noNfcMessage));
	}
	
	@Override
	public void nfcIntentDetected(Intent intent, String action) {
		Log.d(TAG, "nfcIntentDetected: " + action);
		
		if(reader.read(intent)) {
			// do something
		} else {
			// do nothing(?)
		}
	}

	@Override
	public void readNdefMessages(NdefMessage[] messages) {
		if(messages.length > 1) {
	        toast(getString(R.string.readMultipleNDEFMessage));
		} else {
	        toast(getString(R.string.readSingleNDEFMessage));
		}		
		
		// save message
	}

	@Override
	public void readNdefEmptyMessage() {
        toast(getString(R.string.readEmptyMessage));
	}

	@Override
	public void readNonNdefMessage() {
	    toast(getString(R.string.readNonNDEFMessage));
	}

	public void toast(String message) {
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
		toast.show();
	}



}
