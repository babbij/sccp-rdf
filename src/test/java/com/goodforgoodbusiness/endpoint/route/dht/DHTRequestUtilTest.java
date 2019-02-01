package com.goodforgoodbusiness.endpoint.route.dht;

import static com.goodforgoodbusiness.endpoint.route.dht.DHTRequestUtil.processCustodyChainHeader;
import static java.util.stream.Collectors.toList;

public class DHTRequestUtilTest {
	public static void main(String[] args) {
		var header = 
			"ref=d41d8cd98f00b204e9800998ecf8427e&rel=claim:causedBy; " + 
			"ref=8427800d8cdd4198f04e99908ecfe0b2&rel=claim:causedBy; "
		;
		
		System.out.println(
			processCustodyChainHeader(header).collect(toList())
		);
	}
}