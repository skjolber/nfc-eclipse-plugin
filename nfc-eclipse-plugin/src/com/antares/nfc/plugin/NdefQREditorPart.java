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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.antares.nfc.model.NdefRecordModelChangeListener;

public class NdefQREditorPart extends NdefEditorPart implements NdefRecordModelChangeListener {

	private Label binaryQRLabel;

	public NdefQREditorPart(NdefModelOperator operator) {
		super(operator);
	}

	public void setDirty(boolean dirty) {
		super.setDirty(dirty);
		refreshBinaryQR();
	}

	@Override
	public void createPartControl(final Composite composite) {

		super.createPartControl(composite);

		Composite wrapper = new Composite(form, SWT.NONE);
		wrapper.setLayout(new FillLayout());
		
		binaryQRLabel = new Label(wrapper, SWT.NONE);		
		binaryQRLabel.setBackground(new Color(composite.getDisplay(), 0xFF, 0xFF, 0xFF));
		
		form.setWeights(new int[] {10, 10});
		
		composite.getDisplay().asyncExec(
				new Runnable()
				{
					public void run()
					{
						refreshBinaryQR();
					}
				}
				);

		binaryQRLabel.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
               	refreshBinaryQR();
			}
		});
	
	}

	public void refreshBinaryQR() {

		//Point parentSize = binaryQRLabel.getParent().getSize();

		Point size = binaryQRLabel.getSize();

		try {
			binaryQRLabel.setImage(operator.toBinaryQRImage(size.x, size.y, -1, 0));
		} catch (Exception e) {
			// TODO error message
			binaryQRLabel.setImage(null);
		}
	}

	@Override
	protected void modified() {
		super.modified();
		
		refreshBinaryQR();
	}
	
	@Override
	public void setFocus() {
		super.setFocus();
		
		refreshBinaryQR();
	}

	@Override
	public String getTitle() {
		return "NDEF+QR";
	}

	public void refresh() {
		super.refresh();
		
		refreshBinaryQR();
	}

}
