package com.antares.nfc.model;

public class NdefRecordModelNode {
	
	protected NdefRecordModelParent parent;

	public NdefRecordModelNode() {
	}
	
	public NdefRecordModelNode(NdefRecordModelParent parent) {
		this.parent = parent;
	}

	public NdefRecordModelParent getParent() {
		return parent;
	}

	public void setParent(NdefRecordModelParent parent) {
		this.parent = parent;
	}

	public boolean hasParent() {
		return parent != null;
	}

	public int getLevel() {
		return getLevel(0);
	}
	
	protected int getLevel(int current) {
		if(parent != null) {
			return parent.getLevel(current + 1);
		}
		return current;
	}
	
	protected int getTreeRootIndex() {
		NdefRecordModelNode p = this;
		
		while(p != null && p.hasParent()) {
			NdefRecordModelParent next = p.getParent();
			
			if(!next.hasParent()) { // next is root
				return next.indexOf(p);
			}
			
			p = next;
		}
		
		return -1;
	}

	public int getParentIndex() {
		return parent.indexOf(this);
	}
}
