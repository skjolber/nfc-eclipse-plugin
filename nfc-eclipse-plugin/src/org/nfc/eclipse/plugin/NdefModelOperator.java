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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.nfc.eclipse.plugin.model.NdefRecordModelChangeListener;
import org.nfc.eclipse.plugin.model.NdefRecordModelFactory;
import org.nfc.eclipse.plugin.model.NdefRecordModelNode;
import org.nfc.eclipse.plugin.model.NdefRecordModelParent;
import org.nfc.eclipse.plugin.model.NdefRecordModelParentProperty;
import org.nfc.eclipse.plugin.model.NdefRecordModelPropertyList;
import org.nfc.eclipse.plugin.model.NdefRecordModelPropertyListItem;
import org.nfc.eclipse.plugin.model.NdefRecordModelRecord;
import org.nfc.eclipse.plugin.operation.DefaultNdefRecordModelParentPropertyOperation;
import org.nfc.eclipse.plugin.operation.NdefModelAddListItemOperation;
import org.nfc.eclipse.plugin.operation.NdefModelAddNodeOperation;
import org.nfc.eclipse.plugin.operation.NdefModelMoveRecordOperation;
import org.nfc.eclipse.plugin.operation.NdefModelOperation;
import org.nfc.eclipse.plugin.operation.NdefModelRemoveListItemOperation;
import org.nfc.eclipse.plugin.operation.NdefModelRemoveNodeOperation;
import org.nfc.eclipse.plugin.operation.NdefModelReplaceChildRecordsOperation;
import org.nfctools.ndef.NdefContext;
import org.nfctools.ndef.NdefEncoderException;
import org.nfctools.ndef.NdefException;
import org.nfctools.ndef.NdefMessageDecoder;
import org.nfctools.ndef.NdefMessageEncoder;
import org.nfctools.ndef.Record;
import org.nfctools.ndef.wkt.handover.records.HandoverCarrierRecord;
import org.nfctools.ndef.wkt.records.GcActionRecord;
import org.nfctools.ndef.wkt.records.GcTargetRecord;

import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.binary.BinaryQRCodeWriter;

public class NdefModelOperator implements NdefRecordModelChangeListener {
	
	public static final int MAX_BINARY_QR_PAYLOAD = 2953;

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
	/** Mark where we are on the current list of operations (undo list) so that we can determine if dirty even when doing undo and redo */
	private int undoListSizeSaveMark = 0;
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

	public void newModel() {
		this.model = new NdefRecordModelParent();
	}
	
	/**
	 * 
	 * Load from stream. 
	 * 
	 * @param in
	 * @return true if the read input will not be represented the same way when reading (spec interpretation mismatch or bugs)
	 * @throws IOException
	 */
	
	public boolean load(InputStream in) throws IOException {
		// load all the input first
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		
		byte[] buffer = new byte[4 * 1024];
		
		int read;
		do {
			read = in.read(buffer, 0, buffer.length);
			if(read == -1) {
				break;
			}
			
			bout.write(buffer, 0, read);
		} while(true);
		
		// compare input with output; would we write the same contents differently?
		// if so, the save button should be enabled
		byte[] inBytes = bout.toByteArray();
		
		this.model = loadModel(bout.toByteArray());
		
		try {
			byte[] outBytes = toNdefMessage();
			
			return !Arrays.equals(inBytes, outBytes);
		} catch(NdefException e) {
			return true;
		}
	}

	private NdefRecordModelParent loadModel(byte[] ndef) throws IOException {
		if(ndef.length > 0) {
			NdefMessageDecoder ndefMessageDecoder = NdefContext.getNdefMessageDecoder();

			List<Record> list = ndefMessageDecoder.decodeToRecords(ndef);
			
			
			
			Record[] records = list.toArray(new Record[list.size()]);
			
			return NdefRecordModelFactory.represent(records);
		} else {
			return new NdefRecordModelParent();
		}
	}

	public boolean save(File file) throws IOException {
		byte[] encode = toNdefMessage();
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(file);
			fout.write(encode);
			
			fout.close();

			markSave();
			
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

		return ndefMessageEncoder.encode(getRecords());
	}

	@Override
	public void update(NdefRecordModelNode ndefRecordModelNode, NdefModelOperation operation) {
		Activator.info("Update model");

		NdefRecordModelRecord recordNode = ndefRecordModelNode.getRecordNode();
		
		addOperationStep(recordNode, operation);
		
		operation.execute();
	}
	
	@Override
	public void addRecord(NdefRecordModelParent parent, int index, Class<? extends Record> type) {
		
		if(index == -1) {
			index = parent.getSize();
		}
		
		NdefModelAddNodeOperation ndefModelAddRecordOperation = new NdefModelAddNodeOperation(parent, ndefRecordFactory.createRecord(type), index);
		
		addOperationStep(parent, ndefModelAddRecordOperation);
		
		ndefModelAddRecordOperation.execute();
		
	}
	
	@Override
	public void addListItem(NdefRecordModelParent node, int index) {
		
		if(index == -1) {
			index = node.getSize();
		}
		
		NdefModelAddListItemOperation ndefModelAddListItemOperation = new NdefModelAddListItemOperation((NdefRecordModelPropertyList)node, index, "");
		
		addOperationStep(node, ndefModelAddListItemOperation);
		
		ndefModelAddListItemOperation.execute();
	}


	@Override
	public void removeRecord(NdefRecordModelRecord node) {
		Activator.info("Remove record at " + node.getParentIndex());
		
		NdefModelRemoveNodeOperation operation = new NdefModelRemoveNodeOperation(node.getParent(), node);
		
		addOperationStep(node, operation);
		
		operation.execute();
	}

	public void removeListItem(NdefRecordModelPropertyListItem node) {
		NdefModelRemoveListItemOperation ndefModelRemoveListItemOperation = new NdefModelRemoveListItemOperation((NdefRecordModelPropertyList)node.getParent(), (NdefRecordModelPropertyListItem) node);
		
		addOperationStep(node, ndefModelRemoveListItemOperation);
		
		ndefModelRemoveListItemOperation.execute();
	}
	
	public void move(NdefRecordModelNode node, NdefRecordModelParent nextParent, int nextIndex) {
		Activator.info("Move record at " + node.getParent().indexOf(node));
		
		NdefModelMoveRecordOperation ndefModelRecordMoveOperation = new NdefModelMoveRecordOperation(node, nextParent, nextIndex);
		
		addOperationStep(node, ndefModelRecordMoveOperation);
		
		ndefModelRecordMoveOperation.execute();
	}


	public NdefRecordModelParent getModel() {
		return model;
	}
	public boolean refreshBinaryQR(Label label) {
		
		// implementation note: Label seems to display an image OR an label, whatever is set last.
		try {

			byte[] ndef = toNdefMessage();

			if(ndef.length > 0) {
				// do not encode if too large. the encoding takes a lot of time to fail
				if(ndef.length > MAX_BINARY_QR_PAYLOAD) {
					label.setText("NDEF payload size of " + ndef.length + " exceeeds QR code capacity of " + NdefModelOperator.MAX_BINARY_QR_PAYLOAD + " by " + (ndef.length - NdefModelOperator.MAX_BINARY_QR_PAYLOAD) + " bytes.\nIf you wish to use larger payloads:\n - use a NFC reader terminal, or\n - transfer as file to phone memory and use 'Load file' option.");
					
					Activator.info("NDEF size is too large, " + ndef.length);

				} else {
					Point size = label.getSize();

					int parent = Math.min(size.x, size.y);

					writer.setAligment(0, 0);

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

					label.setImage(new Image(getDisplay(), imageData));
					Activator.info("NDEF size is " + ndef.length + ", set image size " + parent + "x" + parent);
				}
				
				return true;
			} else {
				Activator.info("NDEF size is zero");
				
				label.setImage(null);
			}
		} catch(NdefEncoderException e) {
			Activator.error("Cannot create: " + e.getMessage());
			label.setImage(null);
		} catch (Exception e) {
			Activator.error("Cannot create ", e);
			label.setImage(null);
		}
		return false;
	}

	public static Display getDisplay() {
	      Display display = Display.getCurrent();
	      //may be null if outside the UI thread
	      if (display == null)
	         display = Display.getDefault();
	      return display;		
	   }

	public void setRecord(NdefRecordModelParentProperty ndefRecordModelParentProperty, Class<? extends Record> type) {
		
		NdefRecordModelParent parent = ndefRecordModelParentProperty.getParent();
		
		if(parent instanceof NdefRecordModelRecord) {
			NdefRecordModelRecord ndefRecordModelRecord = (NdefRecordModelRecord)parent;
			
			Record record = ndefRecordModelRecord.getRecord();
		
			if(record instanceof GcTargetRecord) {
				
				GcTargetRecord gcTargetRecord = (GcTargetRecord)record;
				
				NdefModelOperation step = new DefaultNdefRecordModelParentPropertyOperation<Record, GcTargetRecord>(gcTargetRecord, ndefRecordModelParentProperty, gcTargetRecord.getTargetIdentifier(), ndefRecordFactory.createRecord(type));
				
				addStep(step);
				
				step.execute();
			} else if(record instanceof GcActionRecord) {

				GcActionRecord gcActionRecord = (GcActionRecord)record;
								
				NdefModelOperation step = new DefaultNdefRecordModelParentPropertyOperation<Record, GcActionRecord>(gcActionRecord, ndefRecordModelParentProperty, gcActionRecord.getActionRecord(), ndefRecordFactory.createRecord(type));
				
				addStep(step);
				
				step.execute();

			} else if(record instanceof HandoverCarrierRecord) {
				HandoverCarrierRecord handoverCarrierRecord = (HandoverCarrierRecord)record;
				
				NdefModelOperation step = new DefaultNdefRecordModelParentPropertyOperation<Record, HandoverCarrierRecord>(handoverCarrierRecord, ndefRecordModelParentProperty, (Record)handoverCarrierRecord.getCarrierType(), ndefRecordFactory.createRecord(type));
				
				addStep(step);
				
				step.execute();
				
			} else {
				throw new RuntimeException();
			}
			
		}
		

	}
	
	
	/** 
	 * Undo a command stored in undolist, and move this command 
	 * in redolist
	 */	
	public void undo(){
		if(!undolist.empty()) {
			NdefModelOperation operation = (NdefModelOperation)undolist.pop();
	
			// undo
			operation.revoke();
			
			redolist.push(operation);
		}
	}
	/**
	 * 
	 * Check if model is modified, i.e. that the current operation list size is not equal to the marked size.
	 * 
	 * @return is model modified 
	 */
	
	public boolean isDirty() {
		return undolist.size() != undoListSizeSaveMark;
	}
	
	/** 
	 * Redo a command stored in redolist, and move this command 
	 * in undolist
	 */
	public void redo(){
		if(!redolist.empty()) {
			NdefModelOperation operation = (NdefModelOperation)redolist.pop();
			
			operation.execute();
			
			undolist.push(operation);
		}
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
			
			undoListSizeSaveMark--;
		}
	}
	
	public void setRecords(byte[] content) {
		try {
			// set the children of the root parent so that all initialized references still point to the correct node
			NdefRecordModelParent nextModel = loadModel(content);
			
			NdefModelReplaceChildRecordsOperation step = new NdefModelReplaceChildRecordsOperation(model, model.getChildren(), nextModel.getChildren());
			
			addStep(step);
			
			step.execute();
		} catch(NdefException e) {
			e.printStackTrace();
			// do nothing
		} catch (IOException e) {
			// internal error?
			e.printStackTrace();
		}
		
	}

	public List<Record> getRecords() {
		List<Record> records = new ArrayList<Record>();
		
		List<NdefRecordModelNode> children = model.getChildren();
		for(NdefRecordModelNode child : children) {
			NdefRecordModelRecord record = (NdefRecordModelRecord)child;
			
			records.add(record.getRecord());
			
		}
		
		return records;
	}

	public void setRecords(List<Record> content) {

		Record[] records = content.toArray(new Record[content.size()]);
		
		NdefModelReplaceChildRecordsOperation step = new NdefModelReplaceChildRecordsOperation(model, model.getChildren(), NdefRecordModelFactory.represent(records).getChildren());
		
		addStep(step);
		
		step.execute();

	}
	
	/**
	 * 
	 * Mark where we are on the current list of operations (undo list) so that we can determine if dirty even when doing undo and redo
	 * 
	 */
	
	public void markSave() {
		undoListSizeSaveMark = undolist.size();
	}
	
	/*
	public boolean canPaste() {
		return getClipboard().getContents(BinaryTransfer.instance) instanceof byte[] ||
				getClipboard().getContents(TextTransfer.getInstance()) instanceof String;
	}
	*/

}
