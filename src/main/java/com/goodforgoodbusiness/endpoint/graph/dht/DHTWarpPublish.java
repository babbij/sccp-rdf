package com.goodforgoodbusiness.endpoint.graph.dht;

import com.goodforgoodbusiness.model.Pointer;

/**
 * A result of publishing to the warp.
 */
public class DHTWarpPublish {
	private final Pointer pointer;
	
	private final String publishedLocation;
	private final String publishedData;
	
	public DHTWarpPublish(Pointer pointer, String publishedLocation, String publishedData) {
		this.pointer = pointer;
		
		this.publishedLocation = publishedLocation;
		this.publishedData = publishedData;
	}
	
	public Pointer getPointer() {
		return pointer;
	}
	
	/**
	 * This will be the encrypted pointer
	 */
	public String getPublishedData() {
		return publishedData;
	}
	
	public String getPublishedLocation() {
		return publishedLocation;
	}
}
