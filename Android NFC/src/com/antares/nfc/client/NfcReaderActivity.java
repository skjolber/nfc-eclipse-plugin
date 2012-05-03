package com.antares.nfc.client;

import java.util.List;

import org.nfctools.ndef.NdefContext;
import org.nfctools.ndef.NdefMessageDecoder;
import org.nfctools.ndef.Record;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
	
	protected NdefMessage[] messages;

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

			// show in log
			if(messages != null) {
				// iterate through all records in all messages (usually only one message)
				
				Log.d(TAG, "Found " + messages.length + " NDEF messages");

				NdefMessageDecoder ndefMessageDecoder = NdefContext.getNdefMessageDecoder();
				for(int i = 0; i < messages.length; i++) {

					byte[] messagePayload = messages[0].toByteArray();
					
					// parse to records - byte to POJO
					List<Record> records = ndefMessageDecoder.decodeToRecords(messages[i].toByteArray());

					Log.d(TAG, "Message " + i + " is of size " + messagePayload.length + " and contains " + records.size() + " records"); // note: after combined chunks, if any.

					for(int k = 0; k < records.size(); k++) {
						Log.d(TAG, " Record " + k + " type " + records.get(k).getClass().getSimpleName());
					}
				}
			}
			
			// show in gui
			showList();
		} else {
			// do nothing(?)
			
			clearList();
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
		this.messages = messages;
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

	private void showList() {
		if(messages != null && messages.length > 0) {
			
			// display the first message
			byte[] messagePayload = messages[0].toByteArray();
			
			// parse to records
			NdefMessageDecoder ndefMessageDecoder = NdefContext.getNdefMessageDecoder();
			List<Record> records = ndefMessageDecoder.decodeToRecords(messagePayload);
			
			// show in gui
			ArrayAdapter<? extends Object> adapter = new NdefRecordAdapter(this, records);
			ListView listView = (ListView) findViewById(R.id.recordListView);
			listView.setAdapter(adapter);
		} else {
			clearList();
		}
	}
	
	private void clearList() {
		ListView listView = (ListView) findViewById(R.id.recordListView);
		listView.setAdapter(null);
	}


}
