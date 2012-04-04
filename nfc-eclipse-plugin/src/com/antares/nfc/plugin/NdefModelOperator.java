/***************************************************************************
 *
 * This file is part of the NFC Eclipse Plugin project at
 * http://code.google.com/p/nfc-eclipse-plugin/
 *
 * Copyright (C) 2012 by Thomas Rørvik Skjølberg / Antares Gruppen AS.
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

package com.antares.nfc.plugin;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.nfctools.ndef.NdefContext;
import org.nfctools.ndef.NdefMessage;
import org.nfctools.ndef.NdefMessageDecoder;
import org.nfctools.ndef.NdefMessageEncoder;
import org.nfctools.ndef.Record;
import org.nfctools.ndef.auri.AbsoluteUriRecord;
import org.nfctools.ndef.empty.EmptyRecord;
import org.nfctools.ndef.ext.AndroidApplicationRecord;
import org.nfctools.ndef.ext.ExternalTypeRecord;
import org.nfctools.ndef.mime.BinaryMimeRecord;
import org.nfctools.ndef.mime.MimeRecord;
import org.nfctools.ndef.unknown.UnknownRecord;
import org.nfctools.ndef.wkt.records.Action;
import org.nfctools.ndef.wkt.records.ActionRecord;
import org.nfctools.ndef.wkt.records.AlternativeCarrierRecord;
import org.nfctools.ndef.wkt.records.GcActionRecord;
import org.nfctools.ndef.wkt.records.GcDataRecord;
import org.nfctools.ndef.wkt.records.GcTargetRecord;
import org.nfctools.ndef.wkt.records.GenericControlRecord;
import org.nfctools.ndef.wkt.records.HandoverCarrierRecord;
import org.nfctools.ndef.wkt.records.HandoverRequestRecord;
import org.nfctools.ndef.wkt.records.HandoverSelectRecord;
import org.nfctools.ndef.wkt.records.SmartPosterRecord;
import org.nfctools.ndef.wkt.records.TextRecord;
import org.nfctools.ndef.wkt.records.UriRecord;

import com.antares.nfc.model.NdefRecordModelChangeListener;
import com.antares.nfc.model.NdefRecordModelFactory;
import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.model.NdefRecordModelParent;
import com.antares.nfc.model.NdefRecordModelRecord;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.binary.BinaryQRCodeWriter;

public class NdefModelOperator implements NdefRecordModelChangeListener {

	// IEditorInput input = getEditorInput();
	
	public static File getProjectPath(IEditorInput input) {
		// find project path
		
		IPath projectPath = null;
		if (input instanceof IPathEditorInput) {
			IPathEditorInput pathEditorInput = (IPathEditorInput) input;
			IPath path = pathEditorInput.getPath();

			for(int i = 0; i < path.segmentCount(); i++) {
				if(path.segment(i).equals("runtime-EclipseApplication")) {
					projectPath = path.uptoSegment(path.segmentCount() - i + 1);
					
					break;
				}
			}
		}
		if(projectPath != null) {
			return projectPath.toFile();
		}
		return null;
	}
	
	private BinaryQRCodeWriter writer = new BinaryQRCodeWriter();

	private NdefRecordModelFactory ndefRecordModelFactory = new NdefRecordModelFactory();

	private NdefRecordModelParent model;
	
	private File projectPath;

	public NdefModelOperator(File projectPath) {
		this.projectPath = projectPath;
	}
	
	public void setModel(NdefRecordModelParent model) {
		this.model = model;
	}

	public void newModel() {
		this.model = new NdefRecordModelParent();
	}
	
	public boolean load(File file) throws IOException {
		NdefMessageDecoder ndefMessageDecoder = NdefContext.getNdefMessageDecoder();
		
		int length = (int)file.length();
		
		Activator.info("Read " + length + " bytes from " + file);

		Record[] records = null;
		if(length > 0) {
			byte[] payload = new byte[length];
			
			InputStream in = null;
			try {
				in = new FileInputStream(file);
				DataInputStream din = new DataInputStream(in);
				
				din.readFully(payload);
				
				NdefMessage decode = ndefMessageDecoder.decode(payload);
				
				List<Record> list = ndefMessageDecoder.decodeToRecords(decode);
				
				records = list.toArray(new Record[list.size()]);
				
				this.model = ndefRecordModelFactory.represent(records);
				
				return true;
			} finally {
				if(in != null) {
					try {
						in.close();
					} catch(IOException e) {
						// ignore
					}
				}
			}
		}
		return false;
	}
	
	

	public boolean save(File file) throws IOException {
		byte[] encode = toNdefMessage();
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(file);
			fout.write(encode);
			
			fout.close();

			return true;
		} finally {
			if(fout != null) {
				try {
					fout.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	public byte[] toNdefMessage() {
		NdefMessageEncoder ndefMessageEncoder = NdefContext.getNdefMessageEncoder();

		List<Record> records = new ArrayList<Record>();
		
		List<NdefRecordModelNode> children = model.getChildren();
		for(NdefRecordModelNode child : children) {
			NdefRecordModelRecord record = (NdefRecordModelRecord)child;
			
			records.add(record.getRecord());
		}
		byte[] encode = ndefMessageEncoder.encode(records);
		return encode;
	}

	@Override
	public void update(NdefRecordModelParent model) {
		Activator.info("Update model");
	}
	
	@Override
	public void insert(NdefRecordModelParent parent, int index, Class<? extends Record> recordType) {
		Activator.info("Insert " + recordType.getSimpleName() + " at " + index);
		
		Record child = null;
		if(recordType == AbsoluteUriRecord.class) {
			AbsoluteUriRecord record = new AbsoluteUriRecord();
			record.setUri("");
			
			child = record;
		} else if(recordType == ActionRecord.class) {
			ActionRecord actionRecord = new ActionRecord();
			actionRecord.setAction(Action.DEFAULT_ACTION);
			
			child = actionRecord;
		} else if(recordType == AndroidApplicationRecord.class) {
			AndroidApplicationRecord androidApplicationRecord = new AndroidApplicationRecord();
			
			String packageName = getAndroidProjectPackageName();
			if(packageName != null) {
				androidApplicationRecord.setPackageName(packageName);
			} else {
				androidApplicationRecord.setPackageName("");
			}
			
			child = androidApplicationRecord;
		} else if(recordType == ExternalTypeRecord.class) {
			ExternalTypeRecord externalTypeRecord = new ExternalTypeRecord();
			externalTypeRecord.setNamespace("");
			externalTypeRecord.setContent("");
			
			child = externalTypeRecord;
		} else if(recordType == EmptyRecord.class) {
			EmptyRecord emptyRecord = new EmptyRecord();
			
			child = emptyRecord;
		} else if(recordType == MimeRecord.class) {
			BinaryMimeRecord mimeMediaRecord = new BinaryMimeRecord();
			
			mimeMediaRecord.setContent(new byte[]{});
			mimeMediaRecord.setContentType("");
			
			child = mimeMediaRecord;
		} else if(recordType == SmartPosterRecord.class) {
			SmartPosterRecord smartPosterRecord = new SmartPosterRecord();

			ActionRecord actionRecord = new ActionRecord();
			actionRecord.setAction(Action.DEFAULT_ACTION);
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
			
			child = handoverRequestRecord;
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
		}
		
		if(child != null) {
			int parentIndex = -1;
			if(parent instanceof NdefRecordModelRecord) {
				NdefRecordModelRecord ndefRecordModelRecordParent = (NdefRecordModelRecord)parent;
				
				parentIndex = connect(ndefRecordModelRecordParent.getRecord(), child);
			}
			
			if(parentIndex != -1) {
				parent.insert(ndefRecordModelFactory.getNode(child, parent), parentIndex);
			} else {
				parent.insert(ndefRecordModelFactory.getNode(child, parent), index);
			}
		}
	}
	
	/**
	 * 
	 * Add parent/child relationships between records
	 * 
	 * @param parent
	 * @param child
	 * @return index of index of child is determined in parent, -1 otherwise
	 */
	
	private int connect(Record parent, Record child) {
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

				return 1 + 1;
			} else if(child instanceof GcDataRecord) {
				GenericControlRecord genericControlRecord = (GenericControlRecord)parent;
				genericControlRecord.setData((GcDataRecord) child);
				
				if(genericControlRecord.hasAction()) {
					return 2 + 1;
				}
				return 1 + 1;
			}
		}
		return -1;
	}

	/**
	 * 
	 * Remove parent/child relationships between records
	 * 
	 * @param parent
	 * @param child
	 */

	
	private void disconnect(Record parent, Record child) {
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
		}
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

	@Override
	public void remove(NdefRecordModelNode node) {
		Activator.info("Remove record at " + node.getParent().indexOf(node));
		
		NdefRecordModelParent parent = node.getParent();
		parent.remove(node);

		if(parent instanceof NdefRecordModelRecord) {
			NdefRecordModelRecord parentRecordNode = (NdefRecordModelRecord)parent;
			NdefRecordModelRecord childRecordNode = (NdefRecordModelRecord)node;

			disconnect(parentRecordNode.getRecord(), childRecordNode.getRecord());
		}
	}
	
	public void move(NdefRecordModelNode node, NdefRecordModelParent nextParent, int nextIndex) {
		Activator.info("Move record at " + node.getParent().indexOf(node));
		
		NdefRecordModelParent currentParent = node.getParent();
		
		if(currentParent == nextParent) { // check if remove affects insert index
			int currentIndex = node.getParentIndex();

			if(currentIndex < nextIndex) {
				nextIndex--;
			}
		}
		
		currentParent.remove(node);

		if(currentParent instanceof NdefRecordModelRecord) {
			NdefRecordModelRecord parentRecordNode = (NdefRecordModelRecord)currentParent;
			NdefRecordModelRecord childRecordNode = (NdefRecordModelRecord)node;

			disconnect(parentRecordNode.getRecord(), childRecordNode.getRecord());
		}
		
		nextParent.insert(node, nextIndex);

		if(nextParent instanceof NdefRecordModelRecord) {
			NdefRecordModelRecord parentRecordNode = (NdefRecordModelRecord)nextParent;
			NdefRecordModelRecord childRecordNode = (NdefRecordModelRecord)node;

			connect(parentRecordNode.getRecord(), childRecordNode.getRecord());
		}
		
		
	}


	@Override
	public void add(NdefRecordModelParent node, Class<? extends Record> recordType) {
		Activator.info("Add " + recordType.getSimpleName());
		
		insert(node, node.getSize(), recordType);
	}

	public NdefRecordModelParent getModel() {
		return model;
	}

	public Image toBinaryQRImage(int parentWidth, int parentHeight, int horizontal, int vertical) throws WriterException {

		byte[] ndef = toNdefMessage();

		if(ndef.length > 0) {
			int parent = Math.min(parentWidth, parentHeight);
			
			writer.setAligment(horizontal, vertical);

			//get a byte matrix for the data
			BitMatrix matrix = writer.encode(ndef, com.google.zxing.BarcodeFormat.QR_CODE, parent, parent);
				
			//generate an image from the byte matrix
			int width = matrix.getWidth(); 
			int height = matrix.getHeight(); 

			//create buffered image to draw to
			ImageData imageData = new ImageData(width, height, 1, new PaletteData(new RGB[]{new RGB(0xFF, 0xFF, 0xFF), new RGB(0x00, 0x00, 0x00)}));
			//iterate through the matrix and draw the pixels to the image
			for (int y = 0; y < height; y++) { 
				for (int x = 0; x < width; x++) { 
					int grayValue = matrix.get(x, y) ? 0 : 0xff; 
					imageData.setPixel(x, y, (grayValue != 0 ? 0 : 0xFFFFFF));
				}
			}

			return new Image(getDisplay(), imageData);
		}
		return null;
	}

	public static Display getDisplay() {
	      Display display = Display.getCurrent();
	      //may be null if outside the UI thread
	      if (display == null)
	         display = Display.getDefault();
	      return display;		
	   }
	
}
