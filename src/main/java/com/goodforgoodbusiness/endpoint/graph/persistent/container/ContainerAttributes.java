package com.goodforgoodbusiness.endpoint.graph.persistent.container;

import static java.time.ZonedDateTime.now;
import static java.util.stream.Collectors.toList;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.kpabe.key.KPABEPublicKey;
import com.goodforgoodbusiness.model.TriTuple;
import com.goodforgoodbusiness.shared.Rounds;
import com.goodforgoodbusiness.shared.encode.CBOR;
import com.goodforgoodbusiness.shared.encode.Hash;
import com.goodforgoodbusiness.shared.encode.Hex;
import com.google.inject.Singleton;

/**
 * Build attribute patterns for KP-ABE encryption routines for warp decryption.
 */
@Singleton
public final class ContainerAttributes {
	private static final Logger log = Logger.getLogger(ContainerAttributes.class);
	
	private static final String PREFIX = "a";
	
	private static String hash(KPABEPublicKey key, TriTuple tt) {
		var cbor = CBOR.forObject(new Object [] { 
			tt.getSubject().orElse(null),
			tt.getPredicate().orElse(null),
			tt.getObject().orElse(null) 
		});
		
		return PREFIX + Hex.encode(Rounds.apply(Hash::sha512, cbor, 3)); // three rounds
	}
	
	/**
	 * Generate attribute hashes.
	 * These undergo an additional round compared to PatternMaker.
	 * We also add time epoch.
	 * This means keys can be issued with a pattern + a timestamp range to restrict access to a particular temporal window.
	 * 
	 * Put a prefix on each attribute to avoid issues with OpenABE (which doesn't like attributes beginning with numbers).
	 */
	public static String forPublish(KPABEPublicKey key, Stream<TriTuple> tuples) {
		var attributeList = tuples
			.parallel()
			.flatMap(TriTuple::matchingCombinations)
			// for DHT publish, tuple pattern must have either defined subject or defined object
			.filter(tt -> tt.getSubject().isPresent() || tt.getObject().isPresent())
			.map(tt -> hash(key, tt))
			.collect(toList())
		;
		
		log.debug(attributeList.size() + " attributes identified");
		
		var attributes = StringUtils.join(attributeList, "|");
		
		// add epoch as additional attribute & return
		attributes += "|time = " + toTimeRepresentation(now());
		
		// add 'all' so we can share everything (usually bounded by a small time window!)
		attributes += "|all";
		
		log.debug("Publish attributes = " + attributes);
		return attributes;
	}
	
	public static String forShare(KPABEPublicKey key, TriTuple tuple, Optional<ZonedDateTime> start, Optional<ZonedDateTime> end) {
		var pattern = "";
		
		if (tuple.getSubject().isPresent() || tuple.getPredicate().isPresent() || tuple.getObject().isPresent()) {
			pattern += hash(key, tuple);
		}
		else {
			pattern += "all";
		}
		
		if (start.isPresent()) {
			pattern += start.map(ContainerAttributes::toTimeRepresentation).map(epochsec -> " AND time >= " + epochsec).get();
		}
		
		if (end.isPresent()) {
			pattern += end.map(ContainerAttributes::toTimeRepresentation).map(epochsec -> " AND time < " + epochsec).get();
		}
		
		return pattern;
	}
	
	private static long toTimeRepresentation(ZonedDateTime datetime) {
		return datetime.toInstant().toEpochMilli() / 1000;
	}
	
	private ContainerAttributes() {
	}
}
