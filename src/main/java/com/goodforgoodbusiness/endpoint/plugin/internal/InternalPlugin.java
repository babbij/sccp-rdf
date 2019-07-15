package com.goodforgoodbusiness.endpoint.plugin.internal;

import com.goodforgoodbusiness.endpoint.plugin.StorableGraphContainer;

/**
 * Harness for reasoner plugins
 */
public interface InternalPlugin {
	/**
	 * Initialize this plugin.
	 */
	public void init() throws InternalPluginException;
	
	/**
	 * Perform reasoning on some new triples added to the main graph
	 */
	public void exec(StorableGraphContainer container) throws InternalPluginException;
}
