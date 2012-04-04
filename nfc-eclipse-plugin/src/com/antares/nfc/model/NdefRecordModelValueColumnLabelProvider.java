package com.antares.nfc.model;

import org.eclipse.jface.viewers.ColumnLabelProvider;

public class NdefRecordModelValueColumnLabelProvider extends ColumnLabelProvider {

		@Override
		public String getText(Object element) {
			if(element instanceof NdefRecordModelProperty) {
				NdefRecordModelProperty ndefRecordModelProperty = (NdefRecordModelProperty)element;
				
				// System.out.println("Get element " + element + " label " + ndefRecordModelProperty.getValue());

				return ndefRecordModelProperty.getValue();
			}
			return null;
		}
		
		

}
