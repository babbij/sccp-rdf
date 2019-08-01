package com.colabriq.endpoint.crypto;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;

import com.colabriq.endpoint.crypto.key.EncodeableKeyPair;
import com.colabriq.endpoint.crypto.key.EncodeablePrivateKey;
import com.colabriq.endpoint.crypto.key.EncodeablePublicKey;
import com.colabriq.shared.encode.Hex;

public class AsymmetricEncryption {
	public static final String KEY_ALGORITHM = "EC";
	public static final int KEY_LENGTH = 256;
	public static final String SIGNING_ALGORITHM = "SHA256withECDSA";

	private static final SecureRandom RANDOM;
	
	static {
		try {
			RANDOM = SecureRandom.getInstanceStrong();
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static EncodeableKeyPair createKeyPair() {
		try {
			var gen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
			gen.initialize(KEY_LENGTH, RANDOM);
			
			var kp = gen.generateKeyPair();
			
			return new EncodeableKeyPair(
				new EncodeablePublicKey(kp.getPublic()),
				new EncodeablePrivateKey(kp.getPrivate())
			);
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Could not create strong KeyPairGenerator instance");
		}
	}
	
	public static String sign(byte [] input, PrivateKey privateKey) throws EncryptionException {
		try {
			Signature sign = Signature.getInstance(SIGNING_ALGORITHM);
			sign.initSign(privateKey);
			sign.update(input);
			
			return Hex.encode(sign.sign());
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Unexpected signing Exception", e);
		}
		catch (InvalidKeyException | SignatureException e) {
			throw new EncryptionException("Unable to sign", e);
		}
	}
	
	public static String sign(String input, PrivateKey privateKey) throws EncryptionException {
		try {
			return sign(input.getBytes("UTF-8"), privateKey);
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unexpected signing Exception", e);
		}
	}
	
	public static boolean verify(byte [] input, String signature, PublicKey publicKey) throws EncryptionException {
		try {
			var sign = Signature.getInstance(SIGNING_ALGORITHM);
			
			sign.initVerify(publicKey);
			sign.update(input);
			
			return sign.verify(Hex.decode(signature));
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Unexpected signing Exception", e);
		}
		catch (InvalidKeyException | SignatureException e) {
			throw new EncryptionException("Unable to sign", e);
		}
	}
	
	public static boolean verify(String input, String signature, PublicKey publicKey) throws EncryptionException {
		try {
			return verify(input.getBytes("UTF-8"), signature, publicKey);
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unexpected signing Exception", e);
		}
	}
}
