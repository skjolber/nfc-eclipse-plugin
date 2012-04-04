package com.antares.nfc.model;

import java.util.ArrayList;
import java.util.List;

public class NdefRecordModelParent extends NdefRecordModelNode {
	
	protected List<NdefRecordModelNode> children;
	
	public NdefRecordModelParent(List<NdefRecordModelNode> children, NdefRecordModelParent parent) {
		super(parent);
		this.children = children;
	}

	public NdefRecordModelParent(NdefRecordModelParent parent) {
		super(parent);
		this.children = new ArrayList<NdefRecordModelNode>();
	}

	public NdefRecordModelParent() {
		this.children = new ArrayList<NdefRecordModelNode>();
	}

	public List<NdefRecordModelNode> getChildren() {
		return children;
	}

	public boolean hasChildren() {
		return children != null && children.size() > 0;
	}

	public boolean add(NdefRecordModelNode e) {
		return children.add(e);
	}

	public int indexOf(NdefRecordModelNode node) {
		return children.indexOf(node);
	}

	public void insert(NdefRecordModelNode node, int index) {
		children.add(index, node);
	}

	public int getSize() {
		return children.size();
	}

	public void remove(int index) {
		children.remove(index);
	}

	public void remove(NdefRecordModelNode node) {
		children.remove(node);
	}

	public boolean hasRecordChildren() {
		for(NdefRecordModelNode child : children) {
			if(child instanceof NdefRecordModelRecord) {
				return true;
			}
		}
		return false;
	}

}
