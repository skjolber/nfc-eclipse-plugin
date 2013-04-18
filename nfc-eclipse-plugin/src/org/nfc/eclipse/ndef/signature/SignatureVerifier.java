package org.nfc.eclipse.ndef.signature;

import java.io.ByteArrayInputStream;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.signers.RSADigestSigner;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey;
import org.nfctools.ndef.wkt.records.SignatureRecord.CertificateFormat;
import org.nfctools.ndef.wkt.records.SignatureRecord.SignatureType;

public class SignatureVerifier {

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
			
			/*
		        try {
					java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509", "BC");
					
					X509Certificate x509Certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(signatureRecord.getCertificate(0)));

					SignatureVerifier signatureVerifier = new SignatureVerifier();
					if(!signatureVerifier.verifyRSASSA_PKCS1_v1_5_WITH_SHA_1(x509Certificate, signature, bout.toByteArray())) {
						return "Unable to verify signature";
					}
					
				} catch (Exception e) {
					return e.toString();
				}
			}
			*/
		}
		
		return null;

	}
	
	// første certificate er det som er brukt til signering
	// de etter må i tur og orden validere hverandre
	// det er ikke lov å ha root-certifikatet som siste certifikat - det må distribueres separat
	
	// 1. validere signatur
	// 2. vise certifikat-detaljer
	// 3. validere keychain mot en keystore
	
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
