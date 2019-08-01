package com.colabriq.endpoint.crypto;

import java.security.PublicKey;

import com.colabriq.endpoint.crypto.key.EncodeableKeyException;
import com.colabriq.endpoint.crypto.key.EncodeablePrivateKey;
import com.colabriq.endpoint.crypto.key.EncodeablePublicKey;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/** Sign with your identity public/private key pair */
public class Identity {
	private final String did;
	private final EncodeablePrivateKey privateKey;
	private final EncodeablePublicKey publicKey;

	@Inject
	public Identity(
		@Named("identity.did") String did,
		@Named("identity.privateKey") String privateKey,
		@Named("identity.publicKey") String publicKey) throws EncodeableKeyException {
		
		this.did = did;
		this.privateKey = new EncodeablePrivateKey(privateKey);
		this.publicKey = new EncodeablePublicKey(publicKey);
	}
	
	public String getDID() {
		return did;
	}
	
	public EncodeablePrivateKey getPrivate() {
		return privateKey;
	}
	
	public EncodeablePublicKey getPublic() {
		return publicKey;
	}
	
	public String sign(byte [] input) throws EncryptionException {
		return AsymmetricEncryption.sign(input, privateKey);
	}
	
	public String sign(String input) throws EncryptionException {
		return AsymmetricEncryption.sign(input, privateKey);
	}
	
	public static boolean verify(byte [] input, String signature, PublicKey publicKey) throws EncryptionException {
		return AsymmetricEncryption.verify(input, signature, publicKey);
	}
	
	public boolean verify(String input, String signature) throws EncryptionException {
		return AsymmetricEncryption.verify(input, signature, publicKey);
	}
}
