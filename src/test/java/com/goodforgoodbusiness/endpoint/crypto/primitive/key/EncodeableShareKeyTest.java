package com.goodforgoodbusiness.endpoint.crypto.primitive.key;

import com.goodforgoodbusiness.endpoint.crypto.key.EncodeableShareKey;
import com.goodforgoodbusiness.kpabe.KPABEEncryption;
import com.goodforgoodbusiness.kpabe.KPABEKeyManager;
import com.goodforgoodbusiness.shared.encode.JSON;

public class EncodeableShareKeyTest {
	public static void main(String[] args) throws Exception {
		var keys = KPABEKeyManager.newKeys();
		var abe = KPABEEncryption.getInstance(keys);
		
		var keyPair = abe.shareKey("foo");
		var encodeablePair = new EncodeableShareKey(keyPair);
		var json = JSON.encodeToString(encodeablePair);
		
		System.out.println(json);
		
		EncodeableShareKey keyPair2 = JSON.decode(json, EncodeableShareKey.class);
		
		System.out.println(keyPair2.getPublic().toString());
		System.out.println(keyPair2.getShare().toString());
	}
}
