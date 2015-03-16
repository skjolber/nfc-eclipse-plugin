# Overview #
The NFC Eclipse plugin editor creates **static** NDEF messages as files.
```
An NDEF message consists of a list of NDEF Records.
```
Later, you might want to handle NDEF messages **dynamically** at runtime using a library like [NDEF Tools for Android](https://github.com/skjolber/ndef-tools-for-android).

# Creating a new NDEF file #
Right-click on a folder and select
```
New -> Other -> Near Field Communications -> NDEF File
```
You should see something like

<img src='https://raw.githubusercontent.com/skjolber/nfc-eclipse-plugin/wiki/images/tutorial/newFileWizard1.png' width='75%' /><br />
Then enter a file name,<br />
<img src='https://raw.githubusercontent.com/skjolber/nfc-eclipse-plugin/wiki/images/tutorial/newFileWizard2.png' width='75%' /><br />
press **Finish** and a new file is created. <br />
_Alternatively create a generic file with **extension .ndef**._
# Editor overview #
![![](https://raw.githubusercontent.com/skjolber/nfc-eclipse-plugin/wiki/images/tutorial/test1.png)](https://raw.githubusercontent.com/skjolber/nfc-eclipse-plugin/wiki/images/tutorial/test1.png)<br />
The editor consists of three parts:
## Records ##

The records are listed in a table of four columns:

| **Record** | **Value** | **Size** | **Hint** |
|:-----------|:----------|:---------|:---------|
| Record tree (record + property names) | Record property value | Record size | Useful hints or error messages |

## Tabs ##
There is also three tabs. The editor can display a custom QR code for your message, and so there is a tab for side-by-side display or QR-only.
## Status line ##
Two status line items appear at the bottom of the screen:

| **NDEF Message size** | **NFC Reader status** |
|:----------------------|:----------------------|
| 0 bytes | No card reader |

# Adding a record #
Right-click on the editor to bring up a popup:

![![](https://raw.githubusercontent.com/skjolber/nfc-eclipse-plugin/wiki/images/tutorial/test2.png)](https://raw.githubusercontent.com/skjolber/nfc-eclipse-plugin/wiki/images/tutorial/test2.png)<br />
For this example we will use an [Android Application Record](http://developer.android.com/guide/topics/connectivity/nfc/nfc.html#aar).

![![](https://raw.githubusercontent.com/skjolber/nfc-eclipse-plugin/wiki/images/tutorial/test3.png)](https://raw.githubusercontent.com/skjolber/nfc-eclipse-plugin/wiki/images/tutorial/test3.png)<br />
The record is missing some required information. Input a package name,

![![](https://raw.githubusercontent.com/skjolber/nfc-eclipse-plugin/wiki/images/tutorial/test4.png)](https://raw.githubusercontent.com/skjolber/nfc-eclipse-plugin/wiki/images/tutorial/test4.png)<br />
and we have successfully created an NDEF message with a single record.

# Adding/inserting more records #
Right-click on a record to insert more records.

![![](https://raw.githubusercontent.com/skjolber/nfc-eclipse-plugin/wiki/images/tutorial/test5.png)](https://raw.githubusercontent.com/skjolber/nfc-eclipse-plugin/wiki/images/tutorial/test5.png)
# Rearranging records - drag and drop #
Top-level records can be rearranged by drag and drop.
# Binary payloads #
Binary payloads can be loaded from files by clicking the field value (in blue, hovered, below). Once loaded, the field can be reloaded from the same file by right-clicking on the field name and choosing 'Reload previous file'. This applies to Mime Media, External Type and Unknown record types.
# Tooltip #
For some record types, like Text record and Mime record, tooltip are supported.

![![](https://raw.githubusercontent.com/skjolber/nfc-eclipse-plugin/wiki/images/tutorial/test6.png)](https://raw.githubusercontent.com/skjolber/nfc-eclipse-plugin/wiki/images/tutorial/test6.png)<br />

# Interfacing NFC hardware #
So far we have only created files, but this is not of much help unless we can interface some NFC hardware. Two approaches are supported:
  * NFC-enabled Android device via the free [NFC Developer](https://play.google.com/store/apps/details?id=com.antares.nfc) app
  * NFC reader
    * ACR 122 and others
Having an NFC reader is obiously better, but many have NFC-enabled smartphones these days, so why not put them to work? :-)

## [NFC Developer](https://play.google.com/store/apps/details?id=com.antares.nfc) app ##
After installing from Google Play, there are two alternatives:

### QR code ###
Switch to the **NDEF+QR** or **QR** tab to transfer the NDEF file by scanning the [QR code](http://en.wikipedia.org/wiki/QR_code) on your screen.

![![](https://raw.githubusercontent.com/skjolber/nfc-eclipse-plugin/wiki/images/tutorial/test7.png)](https://raw.githubusercontent.com/skjolber/nfc-eclipse-plugin/wiki/images/tutorial/test7.png)<br />
<img src='https://raw.githubusercontent.com/skjolber/nfc-eclipse-plugin/wiki/images/tutorial/test8.jpg' width='50%' /><br />

Then just directly scan a tag to write. **Simple, yet effective.**

### Via device file-system ###
Transfer an NDEF file to your Android device's file-system, then select 'Load file' from the app menu. Save to file is also supported. <br />
This option is **best suited for big NDEF messages**, as the QR code approach only supports up to 2953 bytes while tag capacity is currently up to 8k.

<img src='https://raw.githubusercontent.com/skjolber/nfc-eclipse-plugin/wiki/images/tutorial/test9.png' width='50%' /><br />
Then just scan a tag to write.

### Previews ###
For your convenience, the app can **auto-detect and preview** some data types - like the [greenbird](http://www.greenbird.com/) at the bottom of the above screenshot:

<img src='https://raw.githubusercontent.com/skjolber/nfc-eclipse-plugin/wiki/images/tutorial/test10.png' width='50%' /><br />
Please note that using XML or JSON is not necessarily the best choice for small (64 byte) tags, but it is all good for prototyping.
## NFC Reader ##
If an NFC reader is present, for example connected via USB, it should show up in the status line. If so, new alternatives appear within the editor right-click popup:
  * Read, or write to tag
  * Auto-import tag into specific editor whenever a tag is placed on the reader
  * Auto-export to tag whenever the message is edited (and valid).
  * Mark tag as read-only, or format tag

**By default, any tag placed on the scanner automatically appears as a new NDEF file** :-)

![![](https://raw.githubusercontent.com/skjolber/nfc-eclipse-plugin/wiki/images/juno.png)](https://raw.githubusercontent.com/skjolber/nfc-eclipse-plugin/wiki/images/juno.png)

NFC Reader interaction can also be enabled or disabled completely.

# Now what? #
You might consider getting some tags to play with, order using [this link](http://rapidnfc.com/r/1372) to support this project at the same time.

