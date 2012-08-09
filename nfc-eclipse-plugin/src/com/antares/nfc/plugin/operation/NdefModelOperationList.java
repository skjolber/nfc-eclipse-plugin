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
