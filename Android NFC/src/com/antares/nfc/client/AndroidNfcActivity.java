package com.antares.nfc.client;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * 
 * Boilerplate activity selector
 * 
 * @author Thomas Rorvik Skjolberg
 *
 */

public class AndroidNfcActivity extends Activity {
	
    private static final String TAG = AndroidNfcActivity.class.getSimpleName();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		setContentView(R.layout.main);
    }
    
    public void writer(View view) {
    	Log.d(TAG, "Show writer");
    	
    	Intent intent = new Intent(this, NfcWriterActivity.class);
    	startActivity(intent);
    }

    public void reader(View view) {
    	Log.d(TAG, "Show reader");
    	
    	Intent intent = new Intent(this, NfcReaderActivity.class);
    	startActivity(intent);
    }
    

}
