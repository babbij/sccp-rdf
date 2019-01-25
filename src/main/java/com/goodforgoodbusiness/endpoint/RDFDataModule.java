package com.goodforgoodbusiness.endpoint;

import static com.goodforgoodbusiness.shared.ConfigLoader.loadConfig;
import static com.goodforgoodbusiness.webapp.Resource.get;
import static com.goodforgoodbusiness.webapp.Resource.post;
import static com.google.inject.Guice.createInjector;
import static com.google.inject.multibindings.MapBinder.newMapBinder;
import static org.apache.commons.configuration2.ConfigurationConverter.getProperties;

import java.util.Properties;

import org.apache.commons.configuration2.Configuration;
import org.apache.jena.graph.impl.TripleStore;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetGraphMaker;

import com.goodforgoodbusiness.endpoint.dht.ClaimCollector;
import com.goodforgoodbusiness.endpoint.dht.ClaimContextMap;
import com.goodforgoodbusiness.endpoint.dht.DHTClient;
import com.goodforgoodbusiness.endpoint.dht.DHTRDFRunner;
import com.goodforgoodbusiness.endpoint.dht.DHTTripleStore;
import com.goodforgoodbusiness.endpoint.rdf.RDFRunner;
import com.goodforgoodbusiness.endpoint.route.SparqlRoute;
import com.goodforgoodbusiness.endpoint.route.UploadRoute;
import com.goodforgoodbusiness.webapp.Resource;
import com.goodforgoodbusiness.webapp.Webapp;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import spark.Route;

public class RDFDataModule extends AbstractModule {
	private final Configuration config;
	
	public RDFDataModule(Configuration config) {
		this.config = config;
	}
	
	@Override
	protected void configure() {
		binder().requireExplicitBindings();
		
		Properties props = getProperties(config);
		Names.bindProperties(binder(), props);
		
		bind(DHTClient.class);
		
		bind(RDFRunner.class).to(DHTRDFRunner.class);
		
		bind(ClaimCollector.class);
		bind(ClaimContextMap.class);
		
		bind(Webapp.class);
		
		var routes = newMapBinder(binder(), Resource.class, Route.class);
		
		routes.addBinding(post("/sparql")).to(SparqlRoute.class);
		routes.addBinding(get("/sparql")).to(SparqlRoute.class);
		routes.addBinding(post("/upload")).to(UploadRoute.class);
	}
	
	@Provides @Singleton
	protected Dataset getDataset(DHTClient client, ClaimContextMap contextMap, ClaimCollector collector) {
		return DatasetFactory.create(
			new DatasetGraphMaker(
				new GraphMem() {
					@Override
					protected TripleStore createTripleStore() {
						return new DHTTripleStore(this, client, contextMap, collector);
					}
				}
			)
		);
	}
	
	@Provides @Singleton
	protected Model getModel(Dataset dataset) {
		return dataset.getDefaultModel();
	}
	
	public static void main(String[] args) throws Exception {
		createInjector(new RDFDataModule(loadConfig(RDFDataModule.class, "data.properties")))
			.getInstance(Webapp.class)
			.start()
		;
	}
}