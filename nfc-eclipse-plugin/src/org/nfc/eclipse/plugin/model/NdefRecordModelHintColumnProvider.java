/***************************************************************************
 *
 * This file is part of the NFC Eclipse Plugin project at
 * http://code.google.com/p/nfc-eclipse-plugin/
 *
 * Copyright (C) 2012 by Thomas Rorvik Skjolberg.
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

package org.nfc.eclipse.plugin.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.bouncycastle.crypto.RuntimeCryptoException;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.nfc.eclipse.ndef.signature.SignatureVerifier;
import org.nfctools.ndef.NdefContext;
import org.nfctools.ndef.NdefEncoder;
import org.nfctools.ndef.NdefEncoderException;
import org.nfctools.ndef.NdefRecord;
import org.nfctools.ndef.NdefRecordEncoder;
import org.nfctools.ndef.Record;
import org.nfctools.ndef.ext.AndroidApplicationRecord;
import org.nfctools.ndef.mime.MimeRecord;
import org.nfctools.ndef.wkt.records.SignatureRecord;
import org.nfctools.ndef.wkt.records.SignatureRecord.CertificateFormat;
import org.nfctools.ndef.wkt.records.SignatureRecord.SignatureType;
import org.nfctools.ndef.wkt.records.UriRecord;

import android.nfc16.NdefMessage;

public class NdefRecordModelHintColumnProvider extends ColumnLabelProvider {

		private NdefEncoder encoder = NdefContext.getNdefEncoder();

		@Override
		public String getText(Object element) {
						
			if(element instanceof NdefRecordModelNode) {
				NdefRecordModelNode ndefRecordModelNode = (NdefRecordModelNode)element;

				Record record = ndefRecordModelNode.getRecord();

				// first check problems with encoding
				if(element instanceof NdefRecordModelRecord) {
					try {
						byte[] bytes = encoder.encode(ndefRecordModelNode.getRecord());
						
						try {
							new NdefMessage(bytes);
						} catch(Exception e) {
							return "Android incompatible";
						}
					} catch(NdefEncoderException e) {
						
						if(e.getLocation() == ndefRecordModelNode.getRecord()) {
							return e.getMessage();
						} else {
							// do nothing
						}
					} catch(Exception e) {
						// do nothing
					}
				}
				
				if(element instanceof NdefRecordModelRecord) {
					// do nothing
				} else {
					if(record instanceof AndroidApplicationRecord) {						
						// http://developer.android.com/guide/topics/nfc/nfc.html#aar
						AndroidApplicationRecord androidApplicationRecord = (AndroidApplicationRecord)record;
						
						if(androidApplicationRecord.hasPackageName()) {
			
							if(!androidApplicationRecord.matchesNamingConvension()) {
								return "Package convension violated";
							}
						} else {
							return "Enter package name of application";
						}
					
					} else if(record instanceof MimeRecord) {
						MimeRecord mimeRecord = (MimeRecord)record;
						
						if(ndefRecordModelNode.getParentIndex() == 0) {
							if(mimeRecord.hasContentType()) {
								String contentType = mimeRecord.getContentType();
								
								if(contentType.length() > 0) {
									int index = contentType.indexOf('/');
									if(index == -1) {
										return "MIME type convension violated";
									}
								} else {
									return "Enter mime type";
								}
							}
						}
					} else if(record instanceof UriRecord) {
						UriRecord uriRecord = (UriRecord)record;
						
						if(uriRecord.hasUri()) {
							int index = getAbbreviateIndex(uriRecord.getUri());
							
							if(index == 0) {
								return "Uri prefix not in preset list";
							}
						}			
						
						
					} else if(record instanceof SignatureRecord) {
						SignatureRecord signatureRecord = (SignatureRecord)record;
						if(element instanceof NdefRecordModelPropertyListItem) {
							
							int index = ndefRecordModelNode.getParentIndex();
							
							if(signatureRecord.getCertificateFormat() == CertificateFormat.X_509) {
								byte[] certificate = signatureRecord.getCertificate(index);
								
						        try {
									java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
									
									X509Certificate x509Certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificate));

									return x509Certificate.getSubjectX500Principal().getName();
									
								} catch (Exception e) {
									
								}

								
							}
						} else if(element instanceof NdefRecordModelParentProperty) {
							
							int index = ndefRecordModelNode.getParentIndex();

							if(index == 2) {
								if(signatureRecord.hasSignature()) {
									byte[] signature = signatureRecord.getSignature();
									if(signature == null || signature.length == 0) {
										return null;
									}

									if(signatureRecord.getCertificates().isEmpty()) {
										return "No certificate to verify signature";
									}
									
									int level = ndefRecordModelNode.getLevel();
									if(level == 2) {
										int treeRootIndex = ndefRecordModelNode.getTreeRootIndex();
										
										NdefRecordModelParent parent = ndefRecordModelNode.getRecordNode().getParent();
										
										ByteArrayOutputStream bout = new ByteArrayOutputStream();
										for(int i = 0; i < treeRootIndex; i++) {
											NdefRecordModelParent recordParent = (NdefRecordModelParent) parent.getChild(i);
											
											Record covered = recordParent.getRecord();
											
											NdefRecordEncoder ndefRecordEncoder = NdefContext.getNdefRecordEncoder();
											NdefEncoder ndefEncoder = NdefContext.getNdefEncoder();
											
											NdefRecord encode = ndefRecordEncoder.encode(covered, ndefEncoder);

											try {
												byte[] type = encode.getType();
												if(type != null) {
													bout.write(type);
												}
												byte[] id = encode.getId();
												if(id != null) {
													bout.write(id);
												}
												byte[] payload  = encode.getPayload();
												if(payload != null) {
													bout.write(payload);
												}
											} catch (IOException e) {
												throw new RuntimeException();
											}
										}
									

										SignatureVerifier signatureVerifier = new SignatureVerifier();
										
										Boolean verify;
										try {
											verify = signatureVerifier.verify(signatureRecord.getCertificateFormat(), signatureRecord.getCertificates().get(0), signatureRecord.getSignatureType(), signatureRecord.getSignature(), bout.toByteArray());
											
											if(verify != null) {
												if(!verify) {
													return "Signature does not verify";
												} 
											} else {
												return "Verification unsupported";
											}
										} catch (Exception e) {
											return "Problem verifying signature";
										}
									}										
								}
							} else {
								return index + "";
							}
						}
					}
				}
			}
			
			return null;
		}
		
		private int getAbbreviateIndex(String uri) {
			int maxLength = 0;
			int abbreviateIndex = 0;
			for (int x = 1; x < UriRecord.abbreviableUris.length; x++) {

				String abbreviablePrefix = UriRecord.abbreviableUris[x];

				if (uri.startsWith(abbreviablePrefix) && abbreviablePrefix.length() > maxLength) {
					abbreviateIndex = x;
					maxLength = abbreviablePrefix.length();
				}
			}
			return abbreviateIndex;
		}
		
		@Override
		public Color getForeground(Object element) {
			
			if(element instanceof NdefRecordModelNode) {
				NdefRecordModelNode ndefRecordModelNode = (NdefRecordModelNode)element;

				Record record = ndefRecordModelNode.getRecord();

				// first check problems with encoding
				if(element instanceof NdefRecordModelRecord) {
					try {
						byte[] bytes = encoder.encode(ndefRecordModelNode.getRecord());
						
						try {
							new NdefMessage(bytes);
						} catch(Exception e) {
							return new Color(Display.getCurrent(), 0xFF, 0x00, 0x00); 
						}
					} catch(NdefEncoderException e) {
						if(e.getLocation() == ndefRecordModelNode.getRecord()) {
							return new Color(Display.getCurrent(), 0xFF, 0x00, 0x00); 
						} else {
							// do nothing
						}
					} catch(Exception e) {
						// do nothing
					}
				}
				if(element instanceof NdefRecordModelRecord) {
					
				} else {								
					if(record instanceof AndroidApplicationRecord) {
							 
						AndroidApplicationRecord androidApplicationRecord = (AndroidApplicationRecord)record;
						
						if(androidApplicationRecord.hasPackageName()) {
			
							if(!androidApplicationRecord.matchesNamingConvension()) {
								return new Color(Display.getCurrent(), 0xFF, 0x00, 0x00); 
							}
						}
					} else if(record instanceof MimeRecord) {
						MimeRecord mimeRecord = (MimeRecord)record;
						
						if(ndefRecordModelNode.getParentIndex() == 0) {
							if(mimeRecord.hasContentType()) {
								String contentType = mimeRecord.getContentType();
								
								if(contentType.length() > 0) {
									int index = contentType.indexOf('/');
									if(index == -1) {
										return new Color(Display.getCurrent(), 0xFF, 0x00, 0x00); 
									}
								} else {
									return new Color(Display.getCurrent(), 0x00, 0x00, 0x00); 
								}
							}
						}
					} else if(record instanceof UriRecord) {
						UriRecord uriRecord = (UriRecord)record;
						
						if(uriRecord.hasUri()) {
							int index = getAbbreviateIndex(uriRecord.getUri());
							
							if(index == 0) {
								return new Color(Display.getCurrent(), 0x00, 0x00, 0x00); 
							}
						}			
										
					}
				}				
			}
			return super.getForeground(element);
		}
}
