package edu.upenn.cis455.crawler;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

import edu.upenn.cis455.storage.*;

public class XPathCrawler {
	/** Three Module described in Macator paper  */
	
	/** 1. URLFrontierModule: storing and retriveing URLs */
	URLFrontierModule frontier;
	
	/** 2. ProtocolModule: check protocol, ensure politeness, download */
	ProtocolModule protocoler;
	
	/** 3. ProcessingModule: extract links, filter, eliminate dups, add to frontier */ 
	ProcessingModule urlProcessor;
	
	/** Storing users, channels and crawled urls */
//	DatabaseUtil dbManager;
		
	/** Four inputing params */
	String startURL = "http://crawltest.cis.upenn.edu/international/";
	String dbRoot = System.getProperty("user.dir") + "/database";
	int maxSize = 100;  // in Megabytes
	int maxNum = 10;   // max # of files before it stop

	
	public XPathCrawler(String url, String root, int max, int num){
		this.startURL = url;
		this.dbRoot = root;
		this.maxSize = max;
		this.maxNum = num;
		DatabaseUtil.setupContext(root);
		frontier = new URLFrontierModule(startURL); 
		protocoler = new ProtocolModule(maxSize, maxNum);
		urlProcessor = new ProcessingModule();
		
		System.out.println("\nstartURL = " + startURL);
		System.out.println("doRoot = " + dbRoot);
		System.out.println("maxSize = " + maxSize);
		System.out.println("maxNum = " + maxNum);

	}
	
	
	public void startCrawling() throws IOException{
		/**
		 * every Mercator crawl also has a single background thread that performs 
		 * a variety of tasks. The background thread wakes up periodically 
		 * (by default, ev- ery 10 seconds), logs summary statistics about the 
		 * crawl's progress, checks if the crawl should be terminated (either 
		 * because the frontier is empty or because a user- specified time limit
		 *  has been exceeded), and checks to see if it is time to checkpoint
	     *	the crawl's state to stable storage.
		 */
		System.out.println("Start Crawling.\n");
		int numCrawled = 0;
		
		while(!frontier.isEmpty()){
			
			/** Step 1: Remove URL. Polled from shared URL frontier. */
			String absURL = frontier.pollURL();

			/** Step 2: Check politeness, download and store webpages. */
//			Webpage wpage = protocoler.fetch(absURL, dbManager);
			Webpage wpage = protocoler.fetch(absURL);
			
			/** Step 3: Process URL. Extract links and add new URLs to frontier. */
//			urlProcessor.process(wpage, dbManager, frontier);
			urlProcessor.process(wpage, frontier);
			
			// Check if achieved max numr
			numCrawled ++;
			if(numCrawled >= maxNum) break;
			
		}
		
	}
	
	public void shutDown(){
		System.out.println("\nShutdown.");
	}
	
	
	public static void main(String args[])
	{
		/* TODO: Implement crawler */
		if (args.length < 3){
			System.out.println("Usage java XPathCrawler <startUrl> <dbRoot> <maxFileSize>");
		}
		
		String startUrl = args[0];
		String dbRoot = args[1];
		int maxSize = Integer.parseInt(args[2]);
		int numFiles = 100;
		
		if (args.length == 4){
			numFiles = Integer.parseInt(args[3]);
		}
		
		XPathCrawler crawler = new XPathCrawler(startUrl, dbRoot, maxSize, numFiles);
		try {
			crawler.startCrawling();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		crawler.shutDown();
	}

}
