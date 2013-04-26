/***************************************************************************
 *
 * This file is part of the NFC Eclipse Plugin project at
 * http://code.google.com/p/nfc-eclipse-plugin/
 *
 * Copyright (C) 2013 by Thomas Rorvik Skjolberg.
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

package org.nfc.eclipse.ndef.signature;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAParams;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.RSABlindedEngine;
import org.bouncycastle.crypto.params.DSAParameters;
import org.bouncycastle.crypto.params.DSAPublicKeyParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.signers.DSASigner;
import org.bouncycastle.crypto.signers.PSSSigner;
import org.bouncycastle.crypto.signers.RSADigestSigner;
import org.bouncycastle.jcajce.provider.asymmetric.dsa.BCDSAPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey;
import org.nfctools.ndef.wkt.records.SignatureRecord.CertificateFormat;
import org.nfctools.ndef.wkt.records.SignatureRecord.SignatureType;

/**
 * 
 * Class for verifying a signature.
 * 
 * Notes: 
 *  The first certificate (index 0) is used for signing.
 *  Each certificate must validate the previous. 
 *  The root certificate cannot be part of the signatures (naturally), it must be distributed seperately.
 */

public class SignatureVerifier {

    public static final DERObjectIdentifier id_dsa = new DERObjectIdentifier("1.2.840.10040.4.1");
    public static final DERObjectIdentifier id_dsa_with_sha1 = new DERObjectIdentifier("1.2.840.10040.4.3");

	public Boolean verify(CertificateFormat certificateFormat, byte[] certificateBytes, SignatureType signatureType, byte[] signatureBytes, byte[] coveredBytes) throws CertificateException, NoSuchProviderException {

		if (Security.getProvider("BC") == null) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }

		Certificate certificate = null;
		if(certificateFormat == CertificateFormat.X_509) {
			java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509", "BC");

			certificate = cf.generateCertificate(new ByteArrayInputStream(certificateBytes));
		}

		if(signatureType == SignatureType.RSASSA_PKCS1_v1_5_WITH_SHA_1) {

			BCRSAPublicKey key = (BCRSAPublicKey) certificate.getPublicKey();

	        RSAKeyParameters pubParameters = new RSAKeyParameters(false, key.getModulus(), key.getPublicExponent());

	        SHA1Digest digest = new SHA1Digest();
	        
			RSADigestSigner rsaDigestSigner = new RSADigestSigner(digest);
	        rsaDigestSigner.init(false, pubParameters);
	        rsaDigestSigner.update(coveredBytes, 0, coveredBytes.length);

	        return rsaDigestSigner.verifySignature(signatureBytes);
		} else if(signatureType == SignatureType.RSASSA_PSS_SHA_1) {
			BCRSAPublicKey key = (BCRSAPublicKey) certificate.getPublicKey();

	        RSAKeyParameters pubParameters = new RSAKeyParameters(false, key.getModulus(), key.getPublicExponent());

            AsymmetricBlockCipher rsaEngine = new RSABlindedEngine();
            rsaEngine.init(false, pubParameters);

            SHA1Digest digest = new SHA1Digest();

            PSSSigner signer = new PSSSigner(rsaEngine, digest, digest.getDigestSize());
            signer.init(true, pubParameters);
            signer.update(coveredBytes, 0, coveredBytes.length);

            return signer.verifySignature(signatureBytes);
		} else if(signatureType == SignatureType.ECDSA) {
			
			// http://en.wikipedia.org/wiki/Elliptic_Curve_DSA
			// http://stackoverflow.com/questions/11339788/tutorial-of-ecdsa-algorithm-to-sign-a-string
			// http://www.bouncycastle.org/wiki/display/JA1/Elliptic+Curve+Key+Pair+Generation+and+Key+Factories
			// http://java2s.com/Open-Source/Java/Security/Bouncy-Castle/org/bouncycastle/crypto/test/ECTest.java.htm
			
			/*
			BCRSAPublicKey key = (BCRSAPublicKey) certificate.getPublicKey();

	        RSAKeyParameters pubParameters = new RSAKeyParameters(false, key.getModulus(), key.getPublicExponent());

            org.bouncycastle.crypto.signers.ECDSASigner signer = new org.bouncycastle.crypto.signers.ECDSASigner();
            signer.init(false, pubParameters);

	        SHA1Digest digest = new SHA1Digest();
            digest.update(coveredBytes, 0, coveredBytes.length);

            return signer.verifySignature(signatureBytes);
            */
		} else if(signatureType == SignatureType.DSA) {
			
			ASN1InputStream aIn = new ASN1InputStream(signatureBytes);
			ASN1Primitive o;
			try {
				o = aIn.readObject();

				ASN1Sequence asn1Sequence = (ASN1Sequence) o;
	
				BigInteger r = DERInteger.getInstance(asn1Sequence.getObjectAt(0)).getValue();
				BigInteger s = DERInteger.getInstance(asn1Sequence.getObjectAt(1)).getValue();
			 
				BCDSAPublicKey key = (BCDSAPublicKey) certificate.getPublicKey();
				
	            // DSA Domain parameters
	            DSAParams params = key.getParams();
	            if(params == null) {
	                return Boolean.FALSE;
	            }
	            
	            DSAParameters parameters = new DSAParameters(params.getP(), params.getQ(), params.getG());
	            
	            DSASigner signer = new DSASigner();
	            signer.init(false, new DSAPublicKeyParameters(key.getY(), parameters));
	
	            SHA1Digest digest = new SHA1Digest();
	            digest.update(coveredBytes, 0, coveredBytes.length);
	            byte[] message = new byte[digest.getDigestSize()];
	            digest.doFinal(message, 0);
	            
	            return signer.verifySignature(message, r, s);
			} catch (IOException e) {
				return Boolean.FALSE;
			}
		}

		
		return null;

	}
	
	public boolean verifyRSASSA_PKCS1_v1_5_WITH_SHA_1(X509Certificate certificate, byte[] signature, byte[] covered) {
        BCRSAPublicKey key = (BCRSAPublicKey) certificate.getPublicKey();

        RSAKeyParameters pubParameters = new RSAKeyParameters(false, key.getModulus(), key.getPublicExponent());

        SHA1Digest digest = new SHA1Digest();
        
		RSADigestSigner rsaDigestSigner = new RSADigestSigner(digest);
        rsaDigestSigner.init(false, pubParameters);
        rsaDigestSigner.update(covered, 0, covered.length);

        return rsaDigestSigner.verifySignature(signature);
	}
    
}
