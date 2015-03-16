# Overview NFC / NDEF #
The most used NFC message format on Android is the NDEF format.

  * NFC is the radio transmission technology, much like your wifi network card
  * NDEF is the payload format, much like a video you're downloading off the Internet

Other formats to NDEF are specified with different domains of functionality, for example secure access cards. See [this](http://www.radio-electronics.com/info/wireless/nfc/near-field-communications-tutorial.php) for a longer introduction to NFC.

# NDEF messages #
The NDEF message format is a binary format in which an NDEF message contains a list of NDEF records, much like a video file contains both audio and picture tracks.

# NDEF Records #
You can make your own custom record types, but there is already [standardized](http://www.nfc-forum.org) a very useful set of records:

  * absolute URI (links)
  * external type (namespaced/typed data)
    * Android Application Record
  * mime media (xml, image, etc)
  * well-known
    * URI
    * text (text with language)
    * smart poster (online action)
    * connection handover records (wifi, bluetooth, etc)

and more.

## Android Application record ##
The [Android Application Record](http://developer.android.com/guide/topics/connectivity/nfc/nfc.html#aar) is especially important since it lets the NDEF message author specify precisely for which application the message is intended. So if that app is installed and such an NFC tag is scanned, the app launches. If not, Google Play launches to the to-be launched app page, and user can chose to install it (or not).

# Getting started #
As the below resources will get you started on sending and receiving messages, you might want to start by getting to know the NDEF format.

A good, hands-on way to learn is to create some records in the editor hosted on this site, [install](Installation.md) and try the [tutorial](Tutorial.md), then do some experimentation in the editor on your own.

The editor supports [most](https://code.google.com/p/nfc-eclipse-plugin/wiki/Specifications) of the known NDEF Record types, and probably all the ones you will need.

# NFC on Android #
The Android NFC API is specified [here](http://developer.android.com/guide/topics/nfc/nfc.html). Note that NDEF classes are supported but byte-array based.

## Backwards compatibility ##
From version 2.3.3, NFC functionality has been included in Android. Some changes were introduced in 4.0, notably Android Application Records.

More changes were introduced in 4.1, mostly concerning push (beam) messaging.

# Hands-on development #
The emulator does not currently support NFC in/out, so you will need an actual NFC device. Getting some [tags](http://rapidnfc.com/r/1372) or even an NFC terminal for experimentation is also recommended. You can also use two NFC-phones.

To [some extent](http://stackoverflow.com/questions/10346894/possibility-for-fake-nfcnear-field-communication-launch), NFC messaging can be emulated using broadcast intents like in the `FakeTagsActivity` of the NFCDemo project in the Android samples. This is supported by our [NFC Developer](http://play.google.com/store/apps/details?id=com.antares.nfc) app and documented [here](AndroidBroadcastIntents.md).
## Hands-on workshop ##
For a soft learning curve, check out [this](https://github.com/skjolber/Fagmote/tree/master/Android/Near%20Field%20Communications) entry-level workshop.

## Example project ##
There is a boilerplate Eclipse project for Android included within the download section of this project.

## NDEF Tools for Android ##
For <b>dynamic</b> reading and writing of NDEF messages at runtime, check out [NDEF Tools for Android](http://code.google.com/p/ndef-tools-for-android/). This library has higher-level functionality than the current Android NDEF byte-array-based implementation.

## NFC Tools for Java ##
If  you are into interacting Android devices with NFC readers (terminals) and such, you might also check out [NFC Tools for Java](https://github.com/grundid/nfctools).

# Links #
  * [NFC developer forum](https://groups.google.com/forum/?fromgroups#!forum/nfc-developers)
  * [NDEF Tools for Java](http://code.google.com/p/ndef-tools-for-android/)
  * [NFC Tools for Java](https://github.com/grundid/nfctools)
  * [Android NFC demo](http://developer.android.com/resources/samples/NFCDemo/index.html)
  * [Android Beam demo](http://developer.android.com/resources/samples/AndroidBeamDemo/index.html)
