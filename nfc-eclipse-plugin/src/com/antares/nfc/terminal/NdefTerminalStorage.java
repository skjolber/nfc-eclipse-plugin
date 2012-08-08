package com.antares.nfc.terminal;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class NdefTerminalStorage implements IStorage {
	private byte[] contents;
	private String name;
	
	NdefTerminalStorage(byte[] contents, String name) {
		this.contents = contents;
		this.name = name;
	}

	public InputStream getContents() throws CoreException {
		return new ByteArrayInputStream(contents);
	}

	public IPath getFullPath() {
		return null;
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

	public String getName() {
		return name;
	}

	public boolean isReadOnly() {
		return false;
	}
}
