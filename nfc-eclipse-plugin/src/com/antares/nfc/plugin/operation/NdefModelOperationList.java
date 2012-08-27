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

package com.antares.nfc.plugin.operation;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Wrapper for doing multiple operations
 * 
 * @author thomas
 *
 */

public class NdefModelOperationList implements NdefModelOperation {

	private List<NdefModelOperation> operations;

	public NdefModelOperationList() {
		this(new ArrayList<NdefModelOperation>());
	}
	
	public NdefModelOperationList(List<NdefModelOperation> operations) {
		this.operations = operations;
	}

	public boolean add(NdefModelOperation e) {
		return operations.add(e);
	}

	public void clear() {
		operations.clear();
	}

	@Override
	public void execute() {
		for(NdefModelOperation opertion :operations) {
			opertion.execute();
		}
	}

	@Override
	public void revoke() {
		for(NdefModelOperation opertion :operations) {
			opertion.revoke();
		}
	}

}
