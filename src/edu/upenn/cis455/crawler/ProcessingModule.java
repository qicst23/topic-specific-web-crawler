package edu.upenn.cis455.crawler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.DatabaseUtil;
import edu.upenn.cis455.storage.Webpage;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;

/**
 * This processing module is an abstraction for processing downloaded documents.
 * It includes extrating links, filtering URLs, and eliminating duplicates.
 * @author Yayang Tian
 */
public class ProcessingModule {

	int maxSize = 100;
	int maxNum = 50;
	String absURL = null;
	String type = null;
	
	/** the URL used for extracting links */
	private ArrayList<String> extractedURLs = new ArrayList<String>();

	/** DUE: Rabin's fingerprinting algorithms : 8 bytes checksum -> URLs.  */
	HashMap<String,String> URLFingerprintMapping = new HashMap<String, String>();
	
	/** DUE: Store all unique urls that encountered */
	HashSet<String> allUniqueURLs = new HashSet<String>();

	ProcessingModule(){
	}
	
	/**
	 * After the right protocol, this processes URLs 
	 * (1) Extract urls from html
	 * (2) Filter urls to match channels
	 * (3) Eliminate duplicates 
	 */
	void process(Webpage wpage, URLFrontierModule frontier){
//	void process(Webpage wpage, DatabaseUtil dbManager, URLFrontierModule frontier){
		if(wpage == null) return;
		
		
		// determine type 
		boolean isHTML = wpage.getType().equals("text/html");
		
		// ertractd Urls from all links
		if(isHTML) extractLinks(wpage);

		// filtered by user defined channels
//		else filterURLs(wpage, dbManager);
		else filterURLs(wpage);

		// DUE
		if(isHTML) eliminateDupURLs(frontier);
	}
	
	/** extract all links from html */
	void extractLinks(Webpage wpage){
		log("\n3. Process HTML:\t");
		
		log("-> Links Parsed -> to Frontier. ");
		absURL = wpage.getURL();
		// extract
		String lReg = "<a\\s+href\\s*=\\s*[\\'\\\"](.*?)[\\\"]";
		Pattern lPattern = Pattern.compile(lReg, Pattern.CASE_INSENSITIVE);
		Matcher lMatcher = lPattern.matcher(wpage.getContent());
		while(lMatcher.find()){
			String link = lMatcher.group(1).trim();
			// unrelated links
			if(link.equals("#") || link.toLowerCase().startsWith("mailto") || 
					(link.toLowerCase().startsWith("javascript"))) continue;
			// relative links
			if(link.contains("http://")) {
				extractedURLs.add(link);
				return;
			}
		
			if(link.charAt(0) == '/'){
				
				if(!absURL.endsWith("/"))  
					link = absURL + link;
				else 
					link = absURL.substring(0, link.length() - 1) + link;
				
			} else {
				if(absURL.endsWith("/"))  
					link = absURL+ link;
				else link = absURL + "/" + link;
			}
			
			extractedURLs.add(link);
//			log("[debug]\t link = " + link);  // see all the links
		}
	}

	
	/**  This uses hw2ms1 XPath Engine(Filter) to match user-defined channels */
//	void filterURLs(Webpage wpage, DatabaseUtil dbManager){
	void filterURLs(Webpage wpage){
		log("\n3. Process XML:\t\t");
		log("-> XML Filtered ");
		// get dom
		Document dom = getDomFromXML(wpage.getContent());
		ArrayList<Channel> channels = DatabaseUtil.getChannels();
//		ArrayList<Channel> channels = dbManager.getChannels();
		for(Channel channel : channels){
			System.out.println(" -> channel name: \t" + channel.getName());
			ArrayList<String> xarr = channel.getXPaths();
			String[] xpaths = xarr.toArray(new String[xarr.size()]);
			XPathEngineImpl filter = new XPathEngineImpl();
			filter.setXPaths(xpaths);
			boolean[] matched = filter.evaluate(dom);
			for(int pid = 0; pid < xpaths.length; pid ++){
				
				if (matched[pid]) {
					log(" -> [" + xpaths[pid] + " matches " + channel.getName() + "]");
					channel.putMatchedURL(absURL);
					DatabaseUtil.putChannel(channel);
				}
			}
		}
		
		// see if the channels are there
	}

	/** @return true if incoming URL is not duplicate */
	void eliminateDupURLs(URLFrontierModule frontier){

		// ### Do it if have time: 1.upon given to DUE, calculating fingerprint
		// String fp = getFingerprint(url);

		// see if url is in the frontier or DOWNLOADED
		for(String url : extractedURLs){

			// if seen, discard it
			if(frontier.contains(url)) continue;

			// if new, add to frontier
			else frontier.addURL(url);
		}
		/*Each link is converted into an absolute URL and tested against 
		 * a user-supplied URL filter to determine if it should be downloaded 6 .*/ 
	}

	
	/** @return dom from XML text */
	Document getDomFromXML(String doc){
		
//		log(doc);
		Document dom = null;
		String encode = "UTF-8";
		try {
			ByteArrayInputStream byteStream = new ByteArrayInputStream(doc.getBytes(encode));
			dom = DocumentBuilderFactory.newInstance().
					newDocumentBuilder().parse(byteStream);  //#### Change to my own client if have time
			log("-> Got DOM.\n");
			return dom;
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dom;
	}
	
	
	/** @return 8 bytes checksum using Rabin Fingerprinting algorithm */
	public String getFingerprint(String url){
		return "blabla";
	}
	
	/**  System.out.println */
	private static void log(Object value){
		System.out.print(String.valueOf(value));
	}
}
