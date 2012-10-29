/***************************************************************************
 *
 * This file is part of the NFC Eclipse Plugin project at
 * http://code.google.com/p/nfc-eclipse-plugin/
 *
 * Copyright (C) 2012 by Thomas Rorvik Skjolberg / Antares Gruppen AS.
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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.nfctools.ndef.NdefContext;
import org.nfctools.ndef.NdefMessageDecoder;


/**
 * 
 * Tool for checking messages  manually created in editor.
 * 
 * @author thomas
 *
 */

public class RecordValidator {

	public static void main(String[] args) throws IOException {
		File directory = new File("./resources/ndef/graphical/");
		
		for(File file : directory.listFiles()) {
			
			NdefMessageDecoder ndefMessageDecoder = NdefContext.getNdefMessageDecoder();

			FileInputStream fin = new FileInputStream(file);
			
			try {
				DataInputStream din = new DataInputStream(fin);
				byte[] ndef = new byte[(int) file.length()];
				
				din.readFully(ndef);
				
				ndefMessageDecoder.decode(ndef);
			} finally {
				fin.close();
			}
		}
		
	}
}
