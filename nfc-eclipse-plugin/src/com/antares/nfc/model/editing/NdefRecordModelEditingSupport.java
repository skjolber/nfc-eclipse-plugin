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

package com.antares.nfc.model.editing;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TreeViewer;
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
import org.nfctools.ndef.wkt.records.ActionRecord;
import org.nfctools.ndef.wkt.records.GcActionRecord;
import org.nfctools.ndef.wkt.records.GcDataRecord;
import org.nfctools.ndef.wkt.records.GcTargetRecord;
import org.nfctools.ndef.wkt.records.GenericControlRecord;
import org.nfctools.ndef.wkt.records.SignatureRecord;
import org.nfctools.ndef.wkt.records.SmartPosterRecord;
import org.nfctools.ndef.wkt.records.TextRecord;
import org.nfctools.ndef.wkt.records.UriRecord;

import com.antares.nfc.model.NdefRecordModelChangeListener;
import com.antares.nfc.model.NdefRecordModelNode;
import com.antares.nfc.plugin.Activator;
import com.antares.nfc.plugin.NdefRecordFactory;
import com.antares.nfc.plugin.operation.NdefModelOperation;


/**
 * Main editing (as in changing property values) class.
 * 
 * @author trs
 *
 */

public class NdefRecordModelEditingSupport extends EditingSupport {

	public final static String[] PRESENT_OR_NOT = new String[]{"Present", "Not present"};

	private NdefRecordModelChangeListener listener;
	
	TreeViewer treeViewer;
	
	private Map<Class<? extends Record>, RecordEditingSupport> editing = new HashMap<Class<? extends Record>, RecordEditingSupport>();
	
	public NdefRecordModelEditingSupport(TreeViewer viewer, NdefRecordModelChangeListener listener, NdefRecordFactory ndefRecordFactory) {
		super(viewer);
		this.listener = listener;
		this.treeViewer = viewer;
				
		editing.put(ActionRecord.class, new ActionRecordEditingSupport(treeViewer));
		editing.put(GcActionRecord.class, new GcActionRecordEditingSupport(treeViewer, ndefRecordFactory));
		editing.put(GcTargetRecord.class, new GcTargetEditingSupport(treeViewer, ndefRecordFactory));
		editing.put(GcDataRecord.class, new DefaultRecordEditingSupport(treeViewer));
		editing.put(GenericControlRecord.class, new GenericControlRecordEditingSupport(treeViewer));
		
		editing.put(TextRecord.class, new TextRecordEditingSuppport(treeViewer));
		editing.put(AndroidApplicationRecord.class, new AndroidApplicationRecordEditingSupport(treeViewer));
		editing.put(AbsoluteUriRecord.class, new AbsoluteUriRecordEditingSupport(treeViewer));
		editing.put(UriRecord.class, new UriRecordEditingSuppport(treeViewer));
		editing.put(ErrorRecord.class, new ErrorRecordEditingSupport(treeViewer));
		editing.put(UnsupportedExternalTypeRecord.class, new ExternalTypeRecordEditingSupport(treeViewer));
		
		editing.put(SmartPosterRecord.class, new DefaultRecordEditingSupport(treeViewer));
		editing.put(EmptyRecord.class, new DefaultRecordEditingSupport(treeViewer));

		editing.put(BinaryMimeRecord.class, new MimeRecordEditingSupport(treeViewer));

		editing.put(HandoverSelectRecord.class, new HandoverSelectRecordEditingSupport(treeViewer, ndefRecordFactory));
		editing.put(AlternativeCarrierRecord.class, new AlternativeCarrierRecordSelectEditingSupport(treeViewer));

		editing.put(CollisionResolutionRecord.class, new CollisionResolutionRecordEditingSupport(treeViewer));
		
		editing.put(HandoverCarrierRecord.class, new HandoverCarrierRecordEditingSupport(treeViewer, ndefRecordFactory));
		editing.put(HandoverRequestRecord.class, new HandoverRequestRecordEditingSupport(treeViewer));

		editing.put(UnknownRecord.class, new UnknownRecordEditingSupport(treeViewer));
		
		editing.put(SignatureRecord.class, new SignatureRecordEditingSupport(treeViewer));
	}

	@Override
	protected boolean canEdit(Object element) {
		
		NdefRecordModelNode ndefRecordModelNode = (NdefRecordModelNode)element;
		Record record = ndefRecordModelNode.getRecord();
		if(record != null) {
			boolean edit = editing.get(record.getClass()).canEdit(ndefRecordModelNode);
			
			Activator.info("Cell can be edited");
			
			return edit;
		}
		throw new RuntimeException();
	}
		
	@Override
	protected CellEditor getCellEditor(Object element) {
		NdefRecordModelNode ndefRecordModelNode = (NdefRecordModelNode)element;
		Record record = ndefRecordModelNode.getRecord();
		if(record != null) {
			return editing.get(record.getClass()).getCellEditor(ndefRecordModelNode);
		}
		throw new RuntimeException();
	}

	@Override
	protected Object getValue(Object element) {
		Activator.info("Get element " + element + " value");

		NdefRecordModelNode ndefRecordModelNode = (NdefRecordModelNode)element;
		Record record = ndefRecordModelNode.getRecord();
		if(record != null) {
			return editing.get(record.getClass()).getValue(ndefRecordModelNode);
		}
		throw new RuntimeException();
	}

	@Override
	protected void setValue(Object element, Object value) {
		Activator.info("Set element " + element + " value " + value + ", currently have " + getValue(element));

		NdefRecordModelNode ndefRecordModelNode = (NdefRecordModelNode)element;
		Record record = ndefRecordModelNode.getRecord();
		if(record != null) {
			NdefModelOperation setValue = editing.get(record.getClass()).setValue(ndefRecordModelNode, value);

			if(setValue != null) {
				Activator.info("Model operation " + setValue.getClass().getSimpleName());
				if(listener != null) {
					listener.update(ndefRecordModelNode, setValue);
				}			

				// update all but the root node
				NdefRecordModelNode node = (NdefRecordModelNode) element;

				do {
					treeViewer.update(node, null);
					
					node = node.getParent();
				} while(node != null && node.hasParent());

			}
		}
		
		
	}
	
	
}