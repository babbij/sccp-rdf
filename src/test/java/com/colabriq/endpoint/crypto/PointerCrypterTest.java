package com.colabriq.endpoint.crypto;
//package com.goodforgoodbusiness.engine.crypto;
//
//import static org.apache.jena.graph.NodeFactory.createURI;
//import static org.apache.jena.sparql.util.NodeFactoryExtra.createLiteralNode;
//
//import java.util.Optional;
//import java.util.stream.Stream;
//
//import org.apache.jena.graph.Triple;
//
//import com.goodforgoodbusiness.engine.Attributes;
//import com.goodforgoodbusiness.engine.ShareManager;
//import com.goodforgoodbusiness.engine.crypto.key.EncodeableShareKey;
//import com.goodforgoodbusiness.engine.dht.warp.LocalPointerCrypter;
//import com.goodforgoodbusiness.engine.dht.weft.ContainerEncryption;
//import com.goodforgoodbusiness.engine.store.keys.impl.MemKeyStore;
//import com.goodforgoodbusiness.kpabe.local.KPABELocalInstance;
//import com.goodforgoodbusiness.model.Pointer;
//import com.goodforgoodbusiness.model.TriTuple;
//
//public class PointerCrypterTest {
//	public static void main(String[] args) throws Exception {
//		// imagine A is sharing a container with the following triples
//		var tt1 = TriTuple.from(
//			new Triple(
//				createURI("https://twitter.com/ijmad8x"),
//				createURI("http://xmlns.com/foaf/0.1/name"),
//				createLiteralNode("Ian Maddison", null, "http://www.w3.org/2001/XMLSchema/string")
//			)
//		);
//				
//		var tt2 = TriTuple.from(
//			new Triple(
//				createURI("https://twitter.com/ijmad8x"),
//				createURI("http://xmlns.com/foaf/0.1/age"),
//				createLiteralNode("35", null, "http://www.w3.org/2001/XMLSchema/integer")
//			)
//		);
//		
//		// generate kpabe keys for A
//		var kpabeA = KPABELocalInstance.newKeys();
//		var keyManagerA = new ShareManager(kpabeA.getPublicKey(), kpabeA.getSecretKey());
//		
//		// pointer crypter for encrypt
//		var crypterA = new LocalPointerCrypter(keyManagerA, new MemKeyStore());
//		
//		var containerKey = new ContainerEncryption().getSecretKey();
//		
//		// generate fake pointer
//		var pointer = new Pointer("abc123", containerKey.toEncodedString(), 1234l);
//		var ptr = crypterA.encrypt(pointer, Attributes.forPublish(kpabeA.getPublicKey(), Stream.of(tt1, tt2)));
//		
//		System.out.println(ptr);
//		
//		// create fake share key
//		var shareKey = new EncodeableShareKey(kpabeA.shareKey(
//			Attributes.forShare(kpabeA.getPublicKey(), tt1, Optional.empty(), Optional.empty())
//		));
//		
//		// put it in a store created for B
//		var storeB = new MemKeyStore();
//		storeB.saveKey(tt1, shareKey);
//		
//		// create a pointer crypter around B's store
//		var kpabeB = KPABELocalInstance.newKeys();
//		var keyManagerB = new ShareManager(kpabeB.getPublicKey(), kpabeB.getSecretKey());
//		var crypterB = new LocalPointerCrypter(keyManagerB, storeB);
//		
//		// see if B can decrypt, using A's identity (public key)
//		var recoveredPointer = crypterB.decrypt(keyManagerA.getPublicKey(), tt1, ptr.getData());
//		System.out.println(recoveredPointer.get());
//	}
//}
