/***************************************************************************
 *
 * This file is part of the NFC Eclipse Plugin project at
 * http://code.google.com/p/nfc-eclipse-plugin/
 *
 * Copyright (C) 2012 by Thomas Rorvik Skjolberg.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ****************************************************************************/

package org.nfc.eclipse.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Random;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.nfctools.ndef.NdefConstants;
import org.nfctools.ndef.Record;
import org.nfctools.ndef.auri.AbsoluteUriRecord;
import org.nfctools.ndef.empty.EmptyRecord;
import org.nfctools.ndef.ext.AndroidApplicationRecord;
import org.nfctools.ndef.ext.UnsupportedExternalTypeRecord;
import org.nfctools.ndef.mime.BinaryMimeRecord;
import org.nfctools.ndef.unknown.UnknownRecord;
import org.nfctools.ndef.wkt.handover.records.AlternativeCarrierRecord;
import org.nfctools.ndef.wkt.handover.records.CollisionResolutionRecord;
import org.nfctools.ndef.wkt.handover.records.ErrorRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverCarrierRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverRequestRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverSelectRecord;
import org.nfctools.ndef.wkt.records.Action;
import org.nfctools.ndef.wkt.records.ActionRecord;
import org.nfctools.ndef.wkt.records.GcActionRecord;
import org.nfctools.ndef.wkt.records.GcDataRecord;
import org.nfctools.ndef.wkt.records.GcTargetRecord;
import org.nfctools.ndef.wkt.records.GenericControlRecord;
import org.nfctools.ndef.wkt.records.SignatureRecord;
import org.nfctools.ndef.wkt.records.SmartPosterRecord;
import org.nfctools.ndef.wkt.records.TextRecord;
import org.nfctools.ndef.wkt.records.UriRecord;

public class NdefRecordFactory {

	private File projectPath;
	
	public NdefRecordFactory(File projectPath) {
		this.projectPath = projectPath;
	}
	
	public NdefRecordFactory() {
	}

	public File getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(File projectPath) {
		this.projectPath = projectPath;
	}

	@SuppressWarnings("unchecked")
	public <T extends Record> T createRecord(Class<T> recordType) {
		Record child = null;
		if(recordType == AbsoluteUriRecord.class) {
			AbsoluteUriRecord record = new AbsoluteUriRecord();
			record.setUri("");
			
			child = record;
		} else if(recordType == ActionRecord.class) {
			ActionRecord actionRecord = new ActionRecord();
			//actionRecord.setAction(Action.DEFAULT_ACTION);
			
			child = actionRecord;
		} else if(recordType == AndroidApplicationRecord.class) {
			AndroidApplicationRecord androidApplicationRecord = new AndroidApplicationRecord();
			
			String packageName = getAndroidProjectPackageName();
			if(packageName != null) {
				androidApplicationRecord.setPackageName(packageName);
			}
			
			child = androidApplicationRecord;
		} else if(recordType == UnsupportedExternalTypeRecord.class) {
			UnsupportedExternalTypeRecord externalTypeRecord = new UnsupportedExternalTypeRecord();
			
			child = externalTypeRecord;
		} else if(recordType == EmptyRecord.class) {
			EmptyRecord emptyRecord = new EmptyRecord();
			
			child = emptyRecord;
		} else if(recordType == BinaryMimeRecord.class) {
			BinaryMimeRecord mimeMediaRecord = new BinaryMimeRecord();
			
			mimeMediaRecord.setContent(new byte[]{});
			mimeMediaRecord.setContentType("");
			
			child = mimeMediaRecord;
		} else if(recordType == SmartPosterRecord.class) {
			SmartPosterRecord smartPosterRecord = new SmartPosterRecord();

			ActionRecord actionRecord = new ActionRecord();
			smartPosterRecord.setAction(actionRecord);
			
			TextRecord textRecord = new TextRecord();
			textRecord.setEncoding(Charset.forName("UTF-8"));
			textRecord.setLocale(new Locale(Locale.getDefault().getCountry()));
			textRecord.setText("");
			smartPosterRecord.setTitle(textRecord);

			UriRecord uriRecord = new UriRecord("");
			smartPosterRecord.setUri(uriRecord);

			child = smartPosterRecord;
		} else if(recordType == TextRecord.class) {
			
			TextRecord textRecord = new TextRecord();
			textRecord.setEncoding(Charset.forName("UTF-8"));
			textRecord.setLocale(new Locale(Locale.getDefault().getCountry()));
			textRecord.setText("");

			child = textRecord;
		} else if(recordType == UnknownRecord.class) {
			UnknownRecord unknownRecord = new UnknownRecord();

			unknownRecord.setPayload(NdefConstants.EMPTY_BYTE_ARRAY);
			
			child = unknownRecord;
		} else if(recordType == UriRecord.class) {
			UriRecord uriRecord = new UriRecord("");

			child = uriRecord;
		} else if(recordType == AlternativeCarrierRecord.class) {
			AlternativeCarrierRecord alternativeCarrierRecord = new AlternativeCarrierRecord();

			child = alternativeCarrierRecord;
		} else if(recordType == HandoverSelectRecord.class) {
			HandoverSelectRecord handoverSelectRecord = new HandoverSelectRecord();
			
			child = handoverSelectRecord;
		} else if(recordType == HandoverCarrierRecord.class) {
			HandoverCarrierRecord handoverCarrierRecord = new HandoverCarrierRecord();
			
			child = handoverCarrierRecord;
		} else if(recordType == HandoverRequestRecord.class) {
			HandoverRequestRecord handoverRequestRecord = new HandoverRequestRecord();
			
			// add collision
			CollisionResolutionRecord collisionResolutionRecord = new CollisionResolutionRecord();
			Random random = new Random();
			int nextInt = random.nextInt(65536);
			collisionResolutionRecord.setRandomNumber(nextInt);
			handoverRequestRecord.setCollisionResolution(collisionResolutionRecord);
			
			// add alternative carrier record (one is required)
			AlternativeCarrierRecord alternativeCarrierRecord = new AlternativeCarrierRecord();
			handoverRequestRecord.add(alternativeCarrierRecord);
			
			child = handoverRequestRecord;
		} else if(recordType == ErrorRecord.class) {
			ErrorRecord errorRecord = new ErrorRecord();
			
			errorRecord.setErrorReason(null);
			errorRecord.setErrorData(null);
			
			child = errorRecord;
		} else if(recordType == CollisionResolutionRecord.class) {
			CollisionResolutionRecord collisionResolutionRecord = new CollisionResolutionRecord();
			
			Random random = new Random();
			
			int nextInt = random.nextInt(65536);

			collisionResolutionRecord.setRandomNumber(nextInt);
			
			child = collisionResolutionRecord;
		} else if(recordType == GenericControlRecord.class) {
			GenericControlRecord genericControlRecord = new GenericControlRecord();

			/**
			 * A Generic Control record MAY contain one Action record. Generic Control records MUST NOT
			 * contain more than one Action record. When the Action record is omitted, the default action of the
			 * function may be applied. The default action is up to each function.
			 */
			
			GcActionRecord actionRecord = new GcActionRecord();
			actionRecord.setAction(Action.DEFAULT_ACTION);
			genericControlRecord.setAction(actionRecord);
			
			/**
			 * A Data record MAY contain any type of data. The data of records contained in the Data record
			 * SHOULD simply be passed to the target function.
			 * Interpretations of content in a data record are up to the target function.
			 */
			
			GcDataRecord gcDataRecord = new GcDataRecord();
			genericControlRecord.setData(gcDataRecord);

			/** 
			 * A Generic Control record MUST contain one and only one Target record.
			 */
			
			GcTargetRecord gcTargetRecord = new GcTargetRecord();
			genericControlRecord.setTarget(gcTargetRecord);
		
			child = genericControlRecord;
		} else if(recordType == GcActionRecord.class) {
			GcActionRecord gcActionRecord = new GcActionRecord();
			gcActionRecord.setAction(Action.DEFAULT_ACTION);
			
			child = gcActionRecord;
		} else if(recordType == GcDataRecord.class) {
			GcDataRecord gcDataRecord = new GcDataRecord();
			
			child = gcDataRecord;
		} else if(recordType == SignatureRecord.class) {
			SignatureRecord signatureRecord = new SignatureRecord();
			
			child = signatureRecord;
		}
		return (T)child;
	}
	
	private String getAndroidProjectPackageName() {
		if(projectPath != null) {
			File android = new File(projectPath, "AndroidManifest.xml");
			
			if(android.exists()) {
				
				XMLInputFactory factory = XMLInputFactory.newInstance();
				
				InputStream in = null;
				
				try {
					in = new FileInputStream(android);
					XMLStreamReader reader = factory.createXMLStreamReader(in);
					
					int event = reader.nextTag();
					if(event == XMLStreamConstants.START_ELEMENT) {
						String localName = reader.getLocalName();
						if(localName.equals("manifest")) {
							return reader.getAttributeValue(null, "package");
						}
					}
				} catch(IOException e) {
					// ignore
				} catch (XMLStreamException e) {
					// ignore
				} finally {
					if(in != null) {
						try {
							in.close();
						} catch (IOException e) {
							// ignore
						}
					}
				}
			}
		}
		
		return null;
	}

	/**
	 * 
	 * Add parent/child relationships between records
	 * 
	 * @param parent
	 * @param child
	 * @return index of index of child is determined in parent, -1 otherwise
	 */
	
	public static void connect(Record parent, Record child) {
		Activator.info("Connect " + parent.getClass().getSimpleName() + " to " + child.getClass().getSimpleName());
		if(parent instanceof GcDataRecord) {
			GcDataRecord gcDataRecord = (GcDataRecord)parent;
			
			gcDataRecord.add(child);
		} else if(parent instanceof GcTargetRecord) {
			GcTargetRecord gcTargetRecord = (GcTargetRecord)parent;
			
			gcTargetRecord.setTargetIdentifier(child);
		} else if(parent instanceof GcActionRecord) {
			GcActionRecord gcActionRecord = (GcActionRecord)parent;
			
			gcActionRecord.setActionRecord(child);
		} else if(parent instanceof GenericControlRecord) {
			if(child instanceof GcActionRecord) {
				GenericControlRecord genericControlRecord = (GenericControlRecord)parent;
				genericControlRecord.setAction((GcActionRecord) child);
			} else if(child instanceof GcDataRecord) {
				GenericControlRecord genericControlRecord = (GenericControlRecord)parent;
				genericControlRecord.setData((GcDataRecord) child);
				
			}
		} else if(parent instanceof HandoverCarrierRecord) {
			HandoverCarrierRecord handoverCarrierRecord = (HandoverCarrierRecord)parent;
			
			handoverCarrierRecord.setCarrierType(child);
		} else if(parent instanceof HandoverSelectRecord) {
			HandoverSelectRecord handoverSelectRecord = (HandoverSelectRecord)parent;
			
			if(child instanceof ErrorRecord) {
				handoverSelectRecord.setError((ErrorRecord)child);
			}
		}
		
	}


	/**
	 * 
	 * Remove parent/child relationships between records
	 * 
	 * @param parent
	 * @param child
	 */

	
	public static void disconnect(Record parent, Record child) {
		Activator.info("Disconnect child " + child.getClass().getSimpleName() + " from " + parent.getClass().getSimpleName());
		
		if(parent instanceof GcDataRecord) {
			GcDataRecord gcDataRecord = (GcDataRecord)parent;
			
			gcDataRecord.remove(child);
		} else if(parent instanceof GcTargetRecord) {
			GcTargetRecord gcTargetRecord = (GcTargetRecord)parent;
			
			gcTargetRecord.setTargetIdentifier(null);
		} else if(parent instanceof GcActionRecord) {
			GcActionRecord gcActionRecord = (GcActionRecord)parent;
			
			gcActionRecord.setActionRecord(null);
		} else if(parent instanceof GenericControlRecord) {
			if(child instanceof GcActionRecord) {
				GenericControlRecord genericControlRecord = (GenericControlRecord)parent;
				genericControlRecord.setAction(null);
			} else if(child instanceof GcDataRecord) {
				GenericControlRecord genericControlRecord = (GenericControlRecord)parent;
				genericControlRecord.setData(null);
			}
		} else if(parent instanceof HandoverCarrierRecord) {
			HandoverCarrierRecord handoverCarrierRecord = (HandoverCarrierRecord)parent;
			
			handoverCarrierRecord.setCarrierType(null);
		} else if(parent instanceof HandoverSelectRecord) {
			HandoverSelectRecord handoverSelectRecord = (HandoverSelectRecord)parent;
			
			if(child instanceof ErrorRecord) {
				handoverSelectRecord.setError(null);
			}
		}
	}
	
}
