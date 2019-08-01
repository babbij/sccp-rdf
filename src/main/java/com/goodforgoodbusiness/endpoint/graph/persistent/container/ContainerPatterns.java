package com.goodforgoodbusiness.endpoint.graph.persistent.container;

import java.util.stream.Stream;

import com.goodforgoodbusiness.endpoint.ShareManager;
import com.goodforgoodbusiness.kpabe.key.KPABEPublicKey;
import com.goodforgoodbusiness.model.TriTuple;
import com.goodforgoodbusiness.shared.Rounds;
import com.goodforgoodbusiness.shared.encode.CBOR;
import com.goodforgoodbusiness.shared.encode.Hash;
import com.goodforgoodbusiness.shared.encode.Hex;

/**
 * Build index patterns for publishing in to the warp.
 */
public final class ContainerPatterns {
	private static String hash(KPABEPublicKey key, TriTuple tt) {
		var cbor = CBOR.forObject(new Object [] { 
			key.toString(),
			
			tt.getSubject().orElse(null),
			tt.getPredicate().orElse(null),
			tt.getObject().orElse(null) 
		});
		
		// do two rounds of hashing
		return Hex.encode(Rounds.apply(Hash::sha512, cbor, 2)); 
	}
	
	/**
	 * Generate all possible pattern hashes.
	 * These also include the public key of the publishing party.
	 */
	public static Stream<String> forPublish(ShareManager keyManager, TriTuple tuple) {
		return tuple
			.matchingCombinations()
			// for DHT publish, tuple pattern must have either defined subject or defined object
			.filter(tt -> tt.getSubject().isPresent() || tt.getObject().isPresent())
			.map(tt -> hash(keyManager.getCreatorKey(), tt))
			.parallel()
		;
	}
	
	/** 
	 * Generate the pattern hash for this specific TriTuple only.
	 */
	public static String forSearch(KPABEPublicKey key, TriTuple tuple) {
		return hash(key, tuple);
	}
	
	private ContainerPatterns() {
	}
}
