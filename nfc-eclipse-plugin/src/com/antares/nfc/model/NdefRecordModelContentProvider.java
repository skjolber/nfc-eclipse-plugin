package com.antares.nfc.model;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class NdefRecordModelContentProvider implements ITreeContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		if(inputElement instanceof NdefRecordModelParent) {
			NdefRecordModelParent ndefRecordModelParent = (NdefRecordModelParent)inputElement;
			
			return ndefRecordModelParent.getChildren().toArray();
		} else {
			return new Object[]{};
		}
		
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		return getElements(parentElement);
	}

	@Override
	public Object getParent(Object element) {
		if (element == null) {
			return null;
		}
		return ((NdefRecordModelParent) element).getParent();
	}

	@Override
	public boolean hasChildren(Object element) {
		if(element instanceof NdefRecordModelParent) {
			NdefRecordModelParent ndefRecordModelParent = (NdefRecordModelParent)element;

			return ndefRecordModelParent.hasChildren();
		} else {
			return false;
		}
	}

}