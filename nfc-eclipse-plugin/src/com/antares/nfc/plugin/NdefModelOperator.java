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
import java.util.Random;
import java.util.Stack;

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
import org.eclipse.ui.actions.ActionFactory;
import org.nfctools.ndef.NdefConstants;
import org.nfctools.ndef.NdefContext;
import org.nfctools.ndef.NdefMessage;
import org.nfctools.ndef.NdefMessageDecoder;
import org.nfctools.ndef.NdefMessageEncoder;
import org.nfctools.ndef.Record;
import org.nfctools.ndef.auri.AbsoluteUriRecord;
import org.nfctools.ndef.empty.EmptyRecord;
import org.nfctools.ndef.ext.AndroidApplicationRecord;
import org.nfctools.ndef.ext.UnsupportedExternalTypeRecord;
import org.nfctools.ndef.mime.BinaryMimeRecord;
import org.nfctools.ndef.mime.MimeRecord;
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
import org.nfctools.ndef.wkt.records.SmartPosterRecord;
import org.nfctools.ndef.wkt.records.TextRecord;
import org.nfctools.ndef.wkt.records.UriRecord;

import com.antares.nfc.model.NdefRecordModelChangeListener;
import com.antares.nfc.model.NdefRecordModelFactory;
import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.model.NdefRecordModelParent;
import com.antares.nfc.model.NdefRecordModelParentProperty;
import com.antares.nfc.model.NdefRecordModelPropertyList;
import com.antares.nfc.model.NdefRecordModelPropertyListItem;
import com.antares.nfc.model.NdefRecordModelRecord;
import com.antares.nfc.plugin.operation.NdefModelOperation;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.binary.BinaryQRCodeWriter;

public class NdefModelOperator implements NdefRecordModelChangeListener {

	private interface EditorCommand {
		
	}

	private class EditorInsertCommand implements EditorCommand {
		private NdefRecordModelParent parent;
		private int index;
		private Class type;
		
		public EditorInsertCommand(NdefRecordModelParent parent, int index, Class type) {
			this.parent = parent;
			this.index = index;
			this.type = type;
		}		
		
	}
	
	private class EditorUpdateCommand implements EditorCommand {
		
		private NdefRecordModelRecord recordNode;
		private NdefModelOperation operation;
		
		public EditorUpdateCommand(NdefRecordModelRecord recordNode, NdefModelOperation operation) {
			this.recordNode = recordNode;
			this.operation = operation;
		}
	}
	
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
	
	private NdefRecordFactory ndefRecordFactory;
	
	public NdefRecordModelFactory getNdefRecordModelFactory() {
		return ndefRecordModelFactory;
	}

	public NdefRecordFactory getNdefRecordFactory() {
		return ndefRecordFactory;
	}

	public NdefModelOperator( NdefRecordFactory ndefRecordFactory) {
		this.ndefRecordFactory = ndefRecordFactory;
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
	public void update(NdefRecordModelNode ndefRecordModelNode, NdefModelOperation operation) {
		Activator.info("Update model");

		NdefRecordModelRecord recordNode = ndefRecordModelNode.getRecordNode();
		
		addRecordUpdateStep(recordNode, operation);
		
		operation.execute();
	}
	
	@Override
	public void add(NdefRecordModelParent parent, int index, Class type) {
		Activator.info("Insert " + type.getSimpleName() + " at " + index);
		
		addRecordInsertStep(parent, index, type);
		
		addImpl(parent, index, type);
	}

	private void addImpl(NdefRecordModelParent parent, int index, Class type) {
		if(Record.class.isAssignableFrom(type)) {
		
			Record child = ndefRecordFactory.createRecord(type);
			
			if(child != null) {
				if(parent instanceof NdefRecordModelRecord) {
					NdefRecordModelRecord ndefRecordModelRecordParent = (NdefRecordModelRecord)parent;
					
					ndefRecordFactory.connect(ndefRecordModelRecordParent.getRecord(), child);
				}
				
				parent.insert(ndefRecordModelFactory.getNode(child, parent), index);
			}
		} else if(type == String.class) {
			
			if(parent instanceof NdefRecordModelPropertyList) {
				NdefRecordModelPropertyList ndefRecordModelPropertyList = (NdefRecordModelPropertyList)parent;
				
				NdefRecordModelRecord recordParent = (NdefRecordModelRecord) ndefRecordModelPropertyList.getParent();
				
				Record record = recordParent.getRecord();
				if(record instanceof AlternativeCarrierRecord) {
					AlternativeCarrierRecord alternativeCarrierRecord = (AlternativeCarrierRecord)record;
					
					alternativeCarrierRecord.insertAuxiliaryDataReference("", index);
					
					ndefRecordModelPropertyList.insert(new NdefRecordModelPropertyListItem("", ndefRecordModelPropertyList), index);
				}
			}
			
		}
	}


	


	@Override
	public void remove(NdefRecordModelNode node) {
		Activator.info("Remove record at " + node.getParentIndex());
		
		removeImpl(node);
	}

	private void removeImpl(NdefRecordModelNode node) {
		int index = node.getParentIndex();
		
		NdefRecordModelParent parent = node.getParent();
		parent.remove(index);

		if(parent instanceof NdefRecordModelRecord) {
			NdefRecordModelRecord parentRecordNode = (NdefRecordModelRecord)parent;
			NdefRecordModelRecord childRecordNode = (NdefRecordModelRecord)node;

			ndefRecordFactory.disconnect(parentRecordNode.getRecord(), childRecordNode.getRecord());
		} else if(parent instanceof NdefRecordModelPropertyList) {
			NdefRecordModelPropertyList ndefRecordModelPropertyList = (NdefRecordModelPropertyList)parent;
			
			NdefRecordModelRecord recordParent = (NdefRecordModelRecord) ndefRecordModelPropertyList.getParent();
			
			Record record = recordParent.getRecord();
			
			if(record instanceof AlternativeCarrierRecord) {
				AlternativeCarrierRecord alternativeCarrierRecord = (AlternativeCarrierRecord)record;
				
				alternativeCarrierRecord.removeAuxiliaryDataReference(index);
			}				
		} else if(parent instanceof NdefRecordModelParentProperty) {
			Record record = parent.getRecord();
			if(record instanceof GcTargetRecord) {
				GcTargetRecord gcTargetRecord = (GcTargetRecord)record;

				if(gcTargetRecord.hasTargetIdentifier()) {
					ndefRecordFactory.disconnect(gcTargetRecord, gcTargetRecord.getTargetIdentifier());
				}
			} else if(record instanceof GcActionRecord) {
				GcActionRecord gcActionRecord = (GcActionRecord)record;
				
				if(gcActionRecord.hasActionRecord()) {
					ndefRecordFactory.disconnect(gcActionRecord, gcActionRecord.getActionRecord());
				}
			}
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

			ndefRecordFactory.disconnect(parentRecordNode.getRecord(), childRecordNode.getRecord());
		}
		
		nextParent.insert(node, nextIndex);

		if(nextParent instanceof NdefRecordModelRecord) {
			NdefRecordModelRecord parentRecordNode = (NdefRecordModelRecord)nextParent;
			NdefRecordModelRecord childRecordNode = (NdefRecordModelRecord)node;

			ndefRecordFactory.connect(parentRecordNode.getRecord(), childRecordNode.getRecord());
		}
	}

	@Override
	public void add(NdefRecordModelParent node, Class type) {
		Activator.info("Add " + type.getSimpleName());
		
		add(node, node.getSize(), type);
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

	public void set(NdefRecordModelParentProperty ndefRecordModelParentProperty, Class type) {
		
		NdefRecordModelParent parent = ndefRecordModelParentProperty.getParent();
		
		if(parent instanceof NdefRecordModelRecord) {
			NdefRecordModelRecord ndefRecordModelRecord = (NdefRecordModelRecord)parent;
			
			Record record = ndefRecordModelRecord.getRecord();
		
			if(record instanceof GcTargetRecord) {
				
				GcTargetRecord gcTargetRecord = (GcTargetRecord)record;
				
				if(gcTargetRecord.hasTargetIdentifier()) {
					ndefRecordFactory.disconnect(gcTargetRecord, gcTargetRecord.getTargetIdentifier());
					
					ndefRecordModelParentProperty.remove(0);
				}
				
				Record child = ndefRecordFactory.createRecord(type);
				
				ndefRecordFactory.connect(record, child); // ignore index, only one child
				
				ndefRecordModelParentProperty.add(ndefRecordModelFactory.getNode(child, ndefRecordModelParentProperty));

			} else if(record instanceof GcActionRecord) {

				GcActionRecord gcActionRecord = (GcActionRecord)record;
				
				if(gcActionRecord.hasActionRecord()) {
					ndefRecordFactory.disconnect(gcActionRecord, gcActionRecord.getActionRecord());
					
					ndefRecordModelParentProperty.remove(0);
				}
				
				Record child = ndefRecordFactory.createRecord(type);

				ndefRecordFactory.connect(record, child);
				
				ndefRecordModelParentProperty.add(ndefRecordModelFactory.getNode(child, ndefRecordModelParentProperty));

			} else if(record instanceof HandoverCarrierRecord) {

				HandoverCarrierRecord handoverCarrierRecord = (HandoverCarrierRecord)record;

				if(handoverCarrierRecord.hasCarrierType()) {
					Object carrierType = handoverCarrierRecord.getCarrierType();
					if(carrierType instanceof Record) {
						ndefRecordFactory.disconnect(handoverCarrierRecord, (Record)carrierType);
					} else {
						handoverCarrierRecord.setCarrierType(null);
					}
					ndefRecordModelParentProperty.remove(0);
				}
				
				if(type != null) {
					if(type == String.class) {
						handoverCarrierRecord.setCarrierType("");
						
						ndefRecordModelParentProperty.add(ndefRecordModelFactory.getNode(handoverCarrierRecord.getCarrierTypeFormat().name(), handoverCarrierRecord.getCarrierType().toString(), ndefRecordModelParentProperty));
	
					} else {
						
						Record child = ndefRecordFactory.createRecord(type);
	
						ndefRecordFactory.connect(record, child);
						
						ndefRecordModelParentProperty.add(ndefRecordModelFactory.getNode(child, ndefRecordModelParentProperty));
					}
				}
			} else if(record instanceof HandoverSelectRecord) {
				HandoverSelectRecord handoverSelectRecord = (HandoverSelectRecord)record;
				
				if(ndefRecordModelParentProperty.getParentIndex() == 3) {
					
					if(handoverSelectRecord.hasError()) {
						ndefRecordFactory.disconnect(handoverSelectRecord, handoverSelectRecord.getError());
						
						ndefRecordModelParentProperty.remove(0);
					}

					if(type == null) {						
						handoverSelectRecord.setError(null);
					} else if(type == ErrorRecord.class) {

						Record child = ndefRecordFactory.createRecord(type);
						
						ndefRecordFactory.connect(record, child);
						
						ndefRecordModelParentProperty.add(ndefRecordModelFactory.getNode(child, ndefRecordModelParentProperty));

					}
				}
			}
			
		}
		

	}
	
	/**
	 * We use two stacks to store undo & redo information 
	 */
	private Stack<EditorCommand> undolist = new Stack<EditorCommand>();
	private Stack<EditorCommand> redolist = new Stack<EditorCommand>();

	private int maxUndoSteps = 100;
	
	/** 
	 * Undo a command stored in undolist, and move this command 
	 * in redolist
	 */	
	public void undo(){
		if(undolist.empty())
			return;
		
		EditorCommand comm = (EditorCommand)undolist.pop();
		if (comm == null)
			return;

		// edit
		if(comm instanceof EditorUpdateCommand) {
			EditorUpdateCommand editorUpdateCommand = (EditorUpdateCommand)comm;
			
		} else if(comm instanceof EditorInsertCommand) {
			EditorInsertCommand editorInsertCommand = (EditorInsertCommand)comm;
			
			removeImpl(editorInsertCommand.parent.getChild(editorInsertCommand.index));
		}

		if(comm instanceof EditorUpdateCommand) {
			EditorUpdateCommand editorUpdateCommand = (EditorUpdateCommand)comm;
			
			editorUpdateCommand.operation.revoke();
		}

		
		redolist.push(comm);
	}
	
	/** 
	 * Redo a command stored in redolist, and move this command 
	 * in undolist
	 */
	public void redo(){
		if(redolist.empty())
			return;
		
		EditorCommand comm = (EditorCommand)redolist.pop();
		if (comm == null)
			return;
		
		if(comm instanceof EditorUpdateCommand) {
			EditorUpdateCommand editorUpdateCommand = (EditorUpdateCommand)comm;
			
			editorUpdateCommand.operation.execute();
		}
		
		undolist.push(comm);
	}
	
	public boolean canUndo() {
		return !undolist.isEmpty();
	}

	public boolean canRedo() {
		return !redolist.isEmpty();
	}

	private void addRecordInsertStep(NdefRecordModelParent parent, int index, Class type) {
		addStep(new EditorInsertCommand(parent, index, type));
	}

	
	/**
	 * Executes the command and adds a command to undolist, then redolist is cleared.
	 * undolist.size() will always be less than PROPERTY_MAX_UNDO_STEPS 
	 * @param encoded 
	 * @param recordNode 
	 * @param comm
	 */
	
	private void addRecordUpdateStep(NdefRecordModelRecord recordNode, NdefModelOperation operation) {
		addStep(new EditorUpdateCommand(recordNode, operation));
	}

	private void addStep(EditorCommand step) {
		undolist.push(step);
		redolist.clear();
		
		if(maxUndoSteps > 0 && undolist.size() > maxUndoSteps){
			undolist.remove(0);
		}
	}

	/*
	public boolean canPaste() {
		return getClipboard().getContents(BinaryTransfer.instance) instanceof byte[] ||
				getClipboard().getContents(TextTransfer.getInstance()) instanceof String;
	}
	*/
	
	public void addOperation(NdefModelOperation operation) {
		
	}
}
