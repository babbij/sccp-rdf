//package com.goodforgoodbusiness.endpoint.aaaatemp;
//
//import static com.goodforgoodbusiness.shared.TimingRecorder.timer;
//import static com.goodforgoodbusiness.shared.TimingRecorder.TimingCategory.DHT_PUBLISH_WARP;
//import static com.goodforgoodbusiness.shared.TimingRecorder.TimingCategory.KPABE_ENCRYPT;
//import static java.util.Collections.singleton;
//
//import java.security.InvalidKeyException;
//import java.security.NoSuchAlgorithmException;
//import java.security.SecureRandom;
//import java.util.Objects;
//import java.util.Optional;
//import java.util.stream.Stream;
//
//import org.apache.log4j.Logger;
//
//import com.goodforgoodbusiness.endpoint.aaaatemp.crypto.key.EncodeableSecretKey;
//import com.goodforgoodbusiness.endpoint.aaaatemp.crypto.key.EncodeableShareKey;
//import com.goodforgoodbusiness.engine.ShareManager;
//import com.goodforgoodbusiness.engine.backend.DHTBackend;
//import com.goodforgoodbusiness.engine.store.keys.ShareKeyStore;
//import com.goodforgoodbusiness.kpabe.KPABEDecryption;
//import com.goodforgoodbusiness.kpabe.KPABEEncryption;
//import com.goodforgoodbusiness.kpabe.KPABEException;
//import com.goodforgoodbusiness.kpabe.key.KPABEPublicKey;
//import com.goodforgoodbusiness.model.Pointer;
//import com.goodforgoodbusiness.model.TriTuple;
//import com.goodforgoodbusiness.shared.TimingRecorder.TimingCategory;
//import com.goodforgoodbusiness.shared.encode.JSON;
//import com.google.inject.Inject;
//import com.google.inject.Singleton;
//
///** 
// * An instance of the Warp (container indexing).
// */
//@Singleton
//public class Warp {
//	private static final Logger log = Logger.getLogger(Warp.class);
//	
//	private static final SecureRandom RANDOM;
//	
//	static {
//		try {
//			RANDOM = SecureRandom.getInstanceStrong();
//		}
//		catch (NoSuchAlgorithmException e) {
//			throw new RuntimeException(e);
//		}
//	}
//	
//	private final DHTBackend backend;
//	private final ShareManager shareManager;
//	private final ShareKeyStore keyStore;
//	
//	@Inject
//	public Warp(DHTBackend backend, ShareManager shareManager, ShareKeyStore keyStore) {
//		this.backend = backend;
//		this.shareManager = shareManager;
//		this.keyStore = keyStore;
//	}
//	
////	public Optional<WarpPublishResult> publish(String containerId, String pattern, String accessPolicy, EncodeableSecretKey secretKey) {
////		log.debug("Publishing warp pattern: " + pattern);
////			
////		try (var timer = timer(DHT_PUBLISH_WARP)) {
////			var pointer = new Pointer(
////				containerId,
////				secretKey.toEncodedString(),
////				RANDOM.nextLong()
////			);
////			
////			var data = encrypt(pointer, accessPolicy);
////			var location = backend.publish(singleton(pattern), data);
////			
////			if (location.isPresent()) {
////				return Optional.of(new WarpPublishResult(pointer, location.get(), data));
////			}
////			else {
////				log.error("Unable to publish");
////				return Optional.empty();
////			}
////		}
////		catch (KPABEException e) {
////			log.error("Encryption Error", e);
////			return Optional.empty();
////		}
////	}
//	
//	public Stream<Pointer> search(TriTuple tuple) {
//		log.debug("Searching warp for triple " + tuple);
//		
//		// look for anyone who's ever shared a key matching these triples with us
//		// (possibly more than one) and fetch containers for each of them from the DHT
//		
//		return keyStore
//			.knownContainerCreators(tuple)
//			.distinct()
//			.flatMap(containerCreator -> search(containerCreator, tuple))
//		;
//	}
//	
//	/**
//	 * Search for containers by a specific creator
//	 */
//	private Stream<Pointer> search(KPABEPublicKey creator, TriTuple tuple) {
//		var patternHash = Patterns.forSearch(creator, tuple);
//		log.debug("Searching warp for containers from " + creator.toString().substring(0, 10) + 
//			"... with patterns " + patternHash.substring(0,  10) + "...");
//		
//		// have to process this stream because state is changed
//		return backend.search(patternHash)
//			.distinct()
//			.map(location -> backend.fetch(location))
//				.filter(Optional::isPresent)
//				.map(Optional::get)
//			.map(data -> decrypt(creator, tuple, data))
//				.filter(Optional::isPresent)
//				.map(Optional::get)
//		;
//	}
//
//	/**
//	 * Encrypt a pointer
//	 */
//	private String encrypt(Pointer pointer, String accessPolicy) throws KPABEException {
//		try (var timer = timer(KPABE_ENCRYPT)) {
//			return KPABEEncryption.getInstance(shareManager.getCurrentKeys()).encrypt(JSON.encodeToString(pointer), accessPolicy);
//		}
//	}
//	
//	/**
//	 * Attempt to decrypt a pointer.
//	 * Specify publicKey so we know which keys to try against the data
//	 */
//	private Optional<Pointer> decrypt(KPABEPublicKey creator, TriTuple pattern, String data) {
//		log.info("Got data for " + pattern + " from " + creator.toString().substring(0, 10) + "...");
//		
//		try (var timer = timer(TimingCategory.KPABE_DECRYPT)) {
//			return
//				keyStore.keysForDecrypt(creator, pattern) // XXX think about expiry?
//					.parallel()
//					.map(EncodeableShareKey::toKeyPair)
//					.map(keyPair -> {
//						try {
//							var dec = KPABEDecryption.getInstance();
//							var result = dec.decrypt(data, keyPair);
//							if (result != null) {
//								log.debug("Decrypt success");
//							}
//							else {
//								log.debug("Decrypt failure (this can be normal)");
//							}
//							
//							return result;
//						}
//						catch (KPABEException | InvalidKeyException e) {
//							log.error("Error decrypting pointer", e);
//							return null;
//						}
//					})
//					.filter(Objects::nonNull)
//					.findFirst()
//					.map(json -> JSON.decode(json, Pointer.class))
//				;
//		}
//	}
//}
