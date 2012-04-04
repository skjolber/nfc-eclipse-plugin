package com.antares.nfc.plugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
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
	public void createPartControl(Composite composite) {

		super.createPartControl(composite);

		binaryQRLabel = new Label(composite, SWT.NONE);

		composite.getDisplay().asyncExec(
				new Runnable()
				{
					public void run()
					{
						refreshBinaryQR();
					}
				}
				);


		composite.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				refreshBinaryQR();
			}
		});
	}

	public void refreshBinaryQR() {

		Point size = binaryQRLabel.getParent().getSize();

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
