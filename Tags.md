<img src='http://static.ddmcdn.com/gif/nfc-tag-1.jpg' />

# Overview #
An NFC '<b>tag</b>' is a simple paper-thin <b>passive</b> chip with between 64 and 8k bytes storage space. The shape and size of the tag can vary. _'Passive' as in has no power (battery) of its own._

Tags can be read and written by <b>active</b> NFC terminals, like a NFC reader or mobile phone with NFC support. _'Active' as in has power and can induce wireless power onto passive tags._

Bigger chips take <b>longer</b> to read and write.

# Tag types #
Tags exist with various types of technology and capacity. You are looking for <b>tags which can be NDEF-formatted</b>:

  * NFC Forum Type 1, Type 2, Type 3 or Type 4

Note that write-protection is an optional feature, <b>so tag can potentially be written to by anyone with standard equipment</b>.

## Supported tag types using this plugin ##
Note that:
  * Type 2 tags are supported using an NFC reader.
  * Types 1, 2, 3 and 4 are supported through Android.

# Tag capacities #
In the same way as on your hard drive, <b>a tag's capacity is not all for you to put data on</b>. Rather some of the capacity is used for administration (just like a file-system index on a hard drive). Also note that, like hard drives, sometimes tags break and become unusable.

I recommend you at least <b>buy some 192 or 1k bytes tags for development</b>.

You could move to smaller tags later as an optimization - when you ready for production, but on the other hand, [starter/developer kits](http://rapidnfc.com/r/1372) are pretty cheap these days, so getting yourself one is a good starting point.

# Where to order tags online #
My experiments have been using tags from multiple suppliers, however I've come to prefer [RapidNFC](http://rapidnfc.com/r/1372) because:
  * Their developer kit tags are clearly labeled
  * Healty suite of products available
  * Prices seem competitive
  * Orders delivered on time, and to many countries.
  * The website is informative

### Disclamer ###
This project was after some time (February 2013) able to get a cut on sales through this site, so if you feel like supporting us, if equal products is offered on equal terms, go with RapidNFC (using the above link). If that seems like a total sellout, the git history of this page will confirm we recommended them before we had any such motive.