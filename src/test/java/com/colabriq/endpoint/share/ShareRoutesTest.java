//package com.goodforgoodbusiness.endpoint.share;
//
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//import java.time.Duration;
//import java.util.Optional;
//
//import com.goodforgoodbusiness.engine.Governer;
//import com.goodforgoodbusiness.engine.ShareManager;
//import com.goodforgoodbusiness.engine.dht.warp.ShareKeyCreator;
//import com.goodforgoodbusiness.engine.store.keys.impl.MemKeyStore;
//import com.goodforgoodbusiness.kpabe.local.KPABELocalInstance;
//import com.goodforgoodbusiness.model.TriTuple;
//
//import spark.Request;
//import spark.Response;
//
//public class ShareRoutesTest {
//	public static void main(String[] args) throws Exception {
//		var kpabe = KPABELocalInstance.newKeys();
//		var keyManager = new ShareManager(kpabe.getPublicKey(), kpabe.getSecretKey());
//		
//		var scc = new ShareKeyCreator(keyManager);
//		var keyStore = new MemKeyStore();
//		
//		var requestRoute = new ShareRequestRoute(scc);
//		var acceptRoute = new ShareAcceptRoute(keyStore, new Governer(false, Duration.ZERO));
//		
//		var req1 = mock(Request.class);
//		when(req1.queryParams("sub")).thenReturn("s");
//		when(req1.queryParams("pre")).thenReturn("p");
//		when(req1.queryParams("obj")).thenReturn("o");
//		
//		var res1 = mock(Response.class);
//		
//		var output1 = requestRoute.handle(req1, res1);
//		System.out.println(output1);
//		
//		var req2 = mock(Request.class);
//		when(req2.body()).thenReturn(output1.toString());
//		
//		var res2 = mock(Response.class);
//		
//		acceptRoute.handle(req2, res2);
//		var output2 = acceptRoute.handle(req2, res2);
//		System.out.println(output2);
//		
//		var tt = new TriTuple(Optional.of("s"), Optional.of("p"), Optional.of("o"));
//		
//		// verify in the store
//		var searchKeys = keyStore.knownContainerCreators(tt);
//		searchKeys.forEach(searchKey -> {
//			System.out.println(searchKey);
//		});
//		
//		var decryptKeys = keyStore.keysForDecrypt(kpabe.getPublicKey(), tt);
//		decryptKeys.forEach(decryptKey -> {
//			System.out.println(decryptKey);
//		});
//	}
//}
