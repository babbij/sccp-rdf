package com.goodforgoodbusiness.endpoint.graph.dht;

import static com.goodforgoodbusiness.endpoint.graph.dht.container.StorableGraphContainer.toStorableGraphContainers;
import static com.goodforgoodbusiness.shared.TimingRecorder.timer;
import static com.goodforgoodbusiness.shared.TimingRecorder.TimingCategory.RDF_FETCHING;
import static com.goodforgoodbusiness.shared.TripleUtil.isNone;
import static com.goodforgoodbusiness.shared.treesort.TreeSort.sort;
import static java.util.Collections.emptyList;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;

import org.apache.jena.graph.Triple;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.graph.dht.container.StorableGraphContainer;
import com.goodforgoodbusiness.endpoint.graph.dht.container.SubmittedGraphContainer;
import com.goodforgoodbusiness.model.SubmitResult;
import com.goodforgoodbusiness.model.SubmittableContainer;
import com.goodforgoodbusiness.model.TriTuple;
import com.goodforgoodbusiness.shared.URIModifier;
import com.goodforgoodbusiness.shared.encode.JSON;
import com.goodforgoodbusiness.webapp.ContentType;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class DHTEngineClient {
	private static final Logger log = Logger.getLogger(DHTEngineClient.class);
	
	private static final HttpClient HTTP_CLIENT = 
		HttpClient
			.newBuilder()
			.version(Version.HTTP_1_1)
			.build()
		;
	
	private static final String MATCHES_PATH = "/matches";
	private static final String CONTAINERS_PATH = "/containers";
	
	private final URI dhtURI;
	private final DHTAccessGovernor governor;
	private final boolean allowTestQueries;
	
	@Inject
	public DHTEngineClient(@Named("dht.uri") String dhtURI, DHTAccessGovernor governor, @Named("allow.test.queries") boolean allowTestQueries) 
		throws URISyntaxException {
		
		this.dhtURI = new URI(dhtURI);
		this.governor = governor;
		this.allowTestQueries = allowTestQueries;
	}
	
	public List<StorableGraphContainer> matches(Triple trup) throws URISyntaxException, IOException, InterruptedException {
		if (!allowTestQueries) {
			if (isNone(trup.getSubject()) && isNone(trup.getObject())) {
				log.error("Triple with s/p/o " + trup.toString() + " cannot be fetched from DHT");
				return emptyList();
			}
		}
		
		if (governor.allow(trup)) {
			log.debug("Finding matches for: " + trup);
			
			try (var timer = timer(RDF_FETCHING)) {
				var uri = URIModifier
					.from(dhtURI)
					.appendPath(MATCHES_PATH)
					.addParam("pattern", JSON.encode(TriTuple.from(trup)).toString())
					.build();
				
				var request = HttpRequest
					.newBuilder(uri)
					.GET()
					.build();
				
				var response = HTTP_CLIENT.send(request, BodyHandlers.ofString());
				
				if (response.statusCode() == 200) {
					var containers = toStorableGraphContainers(response.body());
					log.info("Results=" + containers.size());
					
					return sort(containers, true);
				}
				else {
					throw new IOException("DHT response was " + response.statusCode());
				}
			}
		}
		else {
			return emptyList();
		}
	}
	
	public SubmittedGraphContainer submit(SubmittableContainer container) throws URISyntaxException, IOException, InterruptedException {
		log.info("Submitting container: " + container);
		
		// invalidate any cached results for triples in container
		container.getTriples().forEach(governor::invalidate);
		
		var uri = URIModifier
			.from(dhtURI)
			.appendPath(CONTAINERS_PATH)
			.build();

		var request = HttpRequest
			.newBuilder(uri)
			.header("Content-Type", ContentType.JSON.getContentTypeString())
			.POST(BodyPublishers.ofString(JSON.encode(container).toString()))
			.build();
		
		var response = HTTP_CLIENT.send(request, BodyHandlers.ofString());
		
		if (response.statusCode() == 200) {
			return new SubmittedGraphContainer(
				container,
				JSON.decode(response.body(), SubmitResult.class)
			);
		}
		else {
			throw new IOException("DHT response was " + response.statusCode());
		}
	}
}