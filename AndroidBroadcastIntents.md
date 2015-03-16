# Broadcast #
An alternative for emulating NFC on an Android device is broadcasting intents.

Broadcast an intent using the code

```
NdefMessage ndefMessage = ...; // your message to broadcast
String action = ...; your action

Intent intent = new Intent(action);
intent.putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, new NdefMessage[]{message});
try {
	startActivity(intent);
} catch(ActivityNotFoundException e) {
	// handle exception, no broadcast recievers
}
```
Where the available actions are
```
String action = NfcAdapter.ACTION_NDEF_DISCOVERED;
```
or
```
String action = NfcAdapter.ACTION_TAG_DISCOVERED;
```
or
```
String action = NfcAdapter.ACTION_TECH_DISCOVERED;

```
for NFC.
# Listening for a broadcast #
Add filters to your activity in [AndroidManifest.xml](http://developer.android.com/guide/topics/manifest/manifest-intro.html). Add
```
<intent-filter>
	<action   android:name="android.nfc.action.TECH_DISCOVERED" />
	<category android:name="android.intent.category.DEFAULT"/>
</intent-filter>            
			 	
<intent-filter>
	<action android:name="android.nfc.action.TAG_DISCOVERED"/>
	<category android:name="android.intent.category.DEFAULT"/>
</intent-filter>
	
<intent-filter>
	<action android:name="android.nfc.action.NDEF_DISCOVERED"/>
	<category android:name="android.intent.category.DEFAULT"/>
</intent-filter>
```

It is possible to [programmatically](http://stackoverflow.com/questions/11370177/handle-nfc-intents-only-when-preference-is-set) enable or disable listening.

# NFC Developer app #
The NFC Developer app supports sending and receiving the above broadcasts. Sending:
  * NFC device: Select 'Broadcast' from the menu.
  * Non-NFC device: Tap message on screen.

<img src='http://wiki.nfc-eclipse-plugin.googlecode.com/git/images/broadcasts/broadcast_actions.png' width='50%' /><br />
Obiously, both alternatives require you first load up an NDEF message using load file, broadcast, scan a tag or a QR code from the Eclipse plugin editor.

