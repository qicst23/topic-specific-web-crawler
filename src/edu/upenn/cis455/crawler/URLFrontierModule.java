package edu.upenn.cis455.crawler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Controlling download scheme
 * Storing the name, download history of documents
 * @author Alantyy
 *
 */
public class URLFrontierModule {

	String removedURL = null;
	
	public URLFrontierModule(String startURL){
		addURL(startURL);
	}


	/* Step:
	 * (1) Remove a URL from the URL list
	 * (2) Determine the IP address of its host name
	 * (3) Download the corresponding document 
	 * (4) extract any links contained in it
	 * (5) add it to the list of URLs to download (not encountered before)
	 */
	/* Weak Politeness Guarantee: 
	   only one tread is allowed to access a particular web server

	   (1) Front-end: prioritizing URLs
	   (2) Back-end: ensuring strong politeness
	 */ 

	/* Implement them if I have time
	Queue frontEndQueue;
	Queue backEndQueue;
	// One host corresponds to many urls
	HashMap <String, Queue> hostQueueMap = new HashMap<String, Queue>();
	// Determine when the web server corresponding to the queue may be contacted again.
	HashMap <String, Queue> timeHeapMap = new HashMap<String, Queue>();
	 */

	/*
	 * Extracted links are submitted to the DUE, 
	 * which passes new ones to the frontier while ignoring those 
	 * that have been submitted to it before.
	 */

	/** Storing the per-site urls */
	static Queue<String> siteURLsQueue = new LinkedList<String>();




	public void addURL(String url){
		// If a link referred to the site of the page it was contained in, 
		// it was added to the appropriate site queue; 
		// otherwise it was logged to disk.
		siteURLsQueue.add(url);
		

	}

	public boolean isEmpty(){
		return siteURLsQueue.isEmpty();
	}
	public String getURL(String id){
		return null;
	} 
	
	public boolean contains(String url){
		return siteURLsQueue.contains(url);
	}

	public String pollURL(){
		
		removedURL = siteURLsQueue.poll();
		return removedURL;
	}
	
	public String getRemovedURL(){
		return removedURL;
	}
	


}
