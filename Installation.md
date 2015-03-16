# Installation #
This plugin [works](Requirements.md) with any Eclipse distribution above version 3.7.2 (Indigo). Below is the recommended clean install from scratch.<br />

  1. Get JDK 1.6 or later
  1. Download [Eclipse Classic](http://www.eclipse.org/downloads/packages/eclipse-classic-42/junor) and unpack it
  1. Start Eclipse and go to menu <b>Help->Install new software</b>. Add update site
```
  http://nfc-eclipse-plugin.googlecode.com/git/nfc-eclipse-plugin-feature/update-site/
```
> > You should see something like
> > ![![](http://wiki.nfc-eclipse-plugin.googlecode.com/git/images/update_site.png)](http://wiki.nfc-eclipse-plugin.googlecode.com/git/images/update_site.png)
  1. Check <b>Near Field Communications</b>, finish the wizard, and restart Eclipse.

Now you can either use an NFC reader or your Android device's NFC chip via the free [NFC Developer](http://play.google.com/store/apps/details?id=com.antares.nfc) app. Or just use the editor for editing files.

Continue to the [tutorial](Tutorial.md).
# Drivers #
By default, try the the drivers for your reader.

## ACR 122 ##
Official [downloads](http://www.acs.com.hk/index.php?pid=product&id=ACR122U).
### Windows ###
Default ACR drivers works.
### Linux ###
Install the [acsccid](http://acsccid.sourceforge.net/) drivers. Might also be available from repos. In addition, get

Fedora:
```
# yum install pcsc-lite-devel
```
Ubuntu:
```
$ sudo apt-get install libpcsclite-dev
```


