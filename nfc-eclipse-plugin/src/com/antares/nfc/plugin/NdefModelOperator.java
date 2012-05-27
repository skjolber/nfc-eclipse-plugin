/***************************************************************************
 *
 * This file is part of the NFC Eclipse Plugin project at
 * http://code.google.com/p/nfc-eclipse-plugin/
 *
 * Copyright (C) 2012 by Thomas R�rvik Skj�lberg / Antares Gruppen AS.
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
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

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
import org.nfctools.ndef.wkt.handover.records.ErrorRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverCarrierRecord;
import org.nfctools.ndef.wkt.handover.records.HandoverSelectRecord;
import org.nfctools.ndef.wkt.records.GcActionRecord;
import org.nfctools.ndef.wkt.records.GcTargetRecord;

import com.antares.nfc.model.NdefRecordModelChangeListener;
import com.antares.nfc.model.NdefRecordModelFactory;
import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.model.NdefRecordModelParent;
import com.antares.nfc.model.NdefRecordModelParentProperty;
import com.antares.nfc.model.NdefRecordModelPropertyList;
import com.antares.nfc.model.NdefRecordModelPropertyListItem;
import com.antares.nfc.model.NdefRecordModelRecord;
import com.antares.nfc.plugin.operation.NdefModelAddListItemOperation;
import com.antares.nfc.plugin.operation.NdefModelAddRecordOperation;
import com.antares.nfc.plugin.operation.NdefModelOperation;
import com.antares.nfc.plugin.operation.NdefModelRecordMoveOperation;
import com.antares.nfc.plugin.operation.NdefModelRemoveListItemOperation;
import com.antares.nfc.plugin.operation.NdefModelRemoveRecordOperation;
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
	
	private NdefRecordFactory ndefRecordFactory;
	
	/**
	 * We use two stacks to store undo & redo information 
	 */
	private Stack<NdefModelOperation> undolist = new Stack<NdefModelOperation>();
	private Stack<NdefModelOperation> redolist = new Stack<NdefModelOperation>();

	private int maxUndoSteps = 100;
	
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
		
		addOperationStep(recordNode, operation);
		
		operation.execute();
	}
	
	@Override
	public void add(NdefRecordModelParent parent, int index, Class type) {
		Activator.info("Insert " + type.getSimpleName() + " at " + index);
		
		addImpl(parent, index, type);
	}

	private void addImpl(NdefRecordModelParent parent, int index, Class type) {
		if(Record.class.isAssignableFrom(type)) {
			NdefModelAddRecordOperation ndefModelAddRecordOperation = new NdefModelAddRecordOperation(parent, ndefRecordFactory.createRecord(type), index);
			
			addOperationStep(parent, ndefModelAddRecordOperation);
			
			ndefModelAddRecordOperation.execute();
			
		} else if(type == String.class) {

			NdefModelAddListItemOperation ndefModelAddListItemOperation = new NdefModelAddListItemOperation((NdefRecordModelPropertyList)parent, index, "");
			
			addOperationStep(parent, ndefModelAddListItemOperation);
			
			ndefModelAddListItemOperation.execute();
		}
	}

	@Override
	public void remove(NdefRecordModelNode node) {
		Activator.info("Remove record at " + node.getParentIndex());
		
		removeImpl(node);
	}

	private void removeImpl(NdefRecordModelNode node) {
		if(node instanceof NdefRecordModelPropertyListItem) {
			NdefModelRemoveListItemOperation ndefModelRemoveListItemOperation = new NdefModelRemoveListItemOperation((NdefRecordModelPropertyList)node.getParent(), (NdefRecordModelPropertyListItem) node);
			
			addOperationStep(node, ndefModelRemoveListItemOperation);
			
			ndefModelRemoveListItemOperation.execute();
		} else if(node instanceof NdefRecordModelRecord) {
			
			NdefModelRemoveRecordOperation ndefModelRemoveRecordOperation = new NdefModelRemoveRecordOperation(node.getParent(), (NdefRecordModelRecord) node);
			
			addOperationStep(node, ndefModelRemoveRecordOperation);
			
			ndefModelRemoveRecordOperation.execute();
		} else {
			throw new RuntimeException();
		}
	}
	
	public void move(NdefRecordModelNode node, NdefRecordModelParent nextParent, int nextIndex) {
		Activator.info("Move record at " + node.getParent().indexOf(node));
		
		NdefModelRecordMoveOperation ndefModelRecordMoveOperation = new NdefModelRecordMoveOperation(node, nextParent, nextIndex);
		
		addOperationStep(node, ndefModelRecordMoveOperation);
		
		ndefModelRecordMoveOperation.execute();
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
	 * Undo a command stored in undolist, and move this command 
	 * in redolist
	 */	
	public void undo(){
		if(undolist.empty())
			return;
		
		NdefModelOperation operation = (NdefModelOperation)undolist.pop();
		if (operation == null)
			return;

		// edit
		operation.revoke();

		
		redolist.push(operation);
	}
	
	/** 
	 * Redo a command stored in redolist, and move this command 
	 * in undolist
	 */
	public void redo(){
		if(redolist.empty())
			return;
		
		NdefModelOperation operation = (NdefModelOperation)redolist.pop();
		if (operation == null)
			return;
		
		operation.execute();
		
		undolist.push(operation);
	}
	
	public boolean canUndo() {
		return !undolist.isEmpty();
	}

	public boolean canRedo() {
		return !redolist.isEmpty();
	}

	/**
	 * Executes the command and adds a command to undolist, then redolist is cleared.
	 * undolist.size() will always be less than PROPERTY_MAX_UNDO_STEPS 
	 * @param encoded 
	 * @param recordNode 
	 * @param comm
	 */
	
	private void addOperationStep(NdefRecordModelNode node, NdefModelOperation operation) {
		addStep(operation);
	}

	private void addStep(NdefModelOperation step) {
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

}
