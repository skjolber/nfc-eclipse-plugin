package com.antares.nfc.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.util.Log;

import com.antares.nfc.client.AndroidNfcActivity;

/**
 * 
 * NFC detector for backing of activities.
 * 
 * @author Thomas Rorvik Skjolberg
 *
 */

public class NfcDetector {

    private static final String TAG = AndroidNfcActivity.class.getSimpleName();
    
    public static interface NfcIntentListener {
    	
		void nfcIntentDetected(Intent intent, String action);
    }
    
	protected NfcAdapter nfcAdapter;
	protected IntentFilter[] writeTagFilters;
	protected PendingIntent nfcPendingIntent;
	
	protected boolean write = false;
	protected boolean foreground = false;
	protected NdefMessage message;

	protected Activity context;
	protected NfcIntentListener listener;
	
	public NfcDetector(Activity context) {
		this.context = context;
		
        nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        nfcPendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, context.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
	}
	
	public void enableForeground() {
        if(!foreground) {
        	Log.d(TAG, "Enable nfc forground mode");
        	
	        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
	        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
	        writeTagFilters = new IntentFilter[] {tagDetected, ndefDetected};
	        nfcAdapter.enableForegroundDispatch(context, nfcPendingIntent, writeTagFilters, null);
	        
	    	foreground = true;
        }
    }
    
    public void disableForeground() {
    	if(foreground) {
        	Log.d(TAG, "Disable nfc forground mode");
        	
    		nfcAdapter.disableForegroundDispatch(context);
    	
    		foreground = false;
    	}
    }

	public void processIntent() {
		Intent intent = context.getIntent();
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
        	Log.d(TAG, "Process NDEF discovered action");

        	listener.nfcIntentDetected(intent, NfcAdapter.ACTION_NDEF_DISCOVERED);
        } else if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
        	Log.d(TAG, "Process TAG discovered action");

        	listener.nfcIntentDetected(intent, NfcAdapter.ACTION_TAG_DISCOVERED);
        } else {
        	Log.d(TAG, "Ignore action " + intent.getAction());
        }
	}

	public NfcIntentListener getListener() {
		return listener;
	}

	public void setListener(NfcIntentListener listener) {
		this.listener = listener;
	}

    
}

