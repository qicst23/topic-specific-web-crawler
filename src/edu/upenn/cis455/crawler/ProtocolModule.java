package edu.upenn.cis455.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.upenn.cis455.storage.DatabaseUtil;
import edu.upenn.cis455.storage.Webpage;


/**
 * @author Alantyy
 * This class dertermines protocol, ensures politeness and downloads pages
 */
public class ProtocolModule {

	String protocol = "HTTP";
	String host = null;
	HashMap<String, String> headersFromServer;

	String contentType = null;
	double contentSize = 0;
	double maxSize = 100;
	int maxNum = 100;

	// Client to get /robot.txt and content of resources
	CrawlerClient client;

	// To avoid downloading again; LRU based; max = 2^18
	HashMap<String, String> hostExclusionMap = new HashMap<String, String>();
	HashMap<String, Long> hostIntervalMap = new HashMap<String, Long>();
	HashMap<String, Long> hostNextCrawlMap = new HashMap<String, Long>();

	// To avild dowloading the same content, different URL
	HashSet<String> allUniqueContents = new HashSet<String>();

	public ProtocolModule(int maxSize, int maxNum){
		//The protocol modules are specified in a user-supplied configuration file HTTP/FTP
		// before crawling
		this.maxSize =  maxSize;
		this.maxNum  = maxNum;
	}

	/**
	 * @return downloaded webpage if we can crawl it 
	 */
//	public Webpage fetch(String serverURL, DatabaseUtil dbManager) throws IOException{
	public Webpage fetch(String serverURL) throws IOException{


		// Set up client
		client = new CrawlerClient(serverURL);
		host = client.getHost();

		/** 1. First check politeness */
		if (!isPolite(serverURL)) return null;

		/** 2. Then send header to server asking content type and size */
		if(!isValidFile(serverURL, this.maxSize)) return null;

		/** 3. If polite and well-formed: Download and store file */
//		Webpage wpage = downloadAndStore(dbManager);
		Webpage wpage = downloadAndStore();

		/** 4. Also added back to the URL frontier just downloaded URL */
		/** ### Continuous crawling : randomized priority-based scheme. */
		//		frontier.addURL(serverURL); ????
		return wpage;
	}


	/**
	 *  @return true if obey rules defined in /robots.txt 
	 */
	public boolean isPolite(String url) throws IOException{
		log("\n---------------------------------------------------------------\n");
		log(url + "\n1. Politeness:\t\t");

		// see if in the cache; if not, download robots and parse it
		if(!hostExclusionMap.containsKey(host)){
			//download
			String rulesText = client.downloadRobotRules();

			if(rulesText == null){
				log(" -> No robots found for this site.");
				return true;
			}
			else {
				parseExclusionRules(rulesText);
				// first time crawling, remember when is next available crawl
				long delay = hostIntervalMap.get(host);
				hostNextCrawlMap.put(host, delay * 1000 + System.currentTimeMillis());   
			}
		}

		// 1. check if crawled too frequently
		long timeleft = hostNextCrawlMap.get(host) - System.currentTimeMillis();
		if(timeleft > 0){
			log("-> Wait " +timeleft/1000  + " s...");
			try {
				Thread.sleep(timeleft);
//				Thread.sleep(200);  // #### change this!!!
			} catch (InterruptedException e) {
				log(e.getMessage());
			}
		} 
		// Set next crawl time
		long delay = hostIntervalMap.get(host);
		hostNextCrawlMap.put(host, delay * 1000 + System.currentTimeMillis());   

		// 2. check if the url is banned 
		String[] bans = hostExclusionMap.get(host).split(";");
		for(String ban : bans){
//			log(ban);
			if(ban.equals("/")){
				log("-> Not polite. Banned ALL robots for: " + url);
				return false;
			}
			String matchURL = url;
			if(ban.endsWith("/") && !url.endsWith("/"))
				matchURL = url + "/";
			if(matchURL.contains(ban)){
				log("-> Not polite. Banned Robot for URL: " + url);
				return false;
			}
		}

		log(" -> Polite Now.");
		return true;
	}


	/**
	 *  @return true if file type and size is valid 
	 */  
	public boolean isValidFile(String serverURL, double maxSize) throws IOException{
		
		log("\n3. Get file:\t\t");
		// send HEAD request 
		headersFromServer = client.getHeadFromURL(serverURL);
		if(headersFromServer == null){
			log("Error. No headers response from server.");
			return false;
		}

		if(!headersFromServer.containsKey("content-type")){
			System.out.println("Error. No content type.");
			return false;
		}

		// check type
		contentType = headersFromServer.get("content-type");
		if(!contentType.equals("text/xml") && !contentType.equals("text/html") && 
				!contentType.endsWith("+xml") && !contentType.equals("application/xml")){
			log(" -> Not polite. Unsupport file type.");
			return false;	
		}

		// check size
		contentSize = Double.parseDouble(headersFromServer.get("content-length"));
		if(contentSize > maxSize * 1024 * 1024){
			log(" -> Not polite. Size too big.");
			return false;
		}
		return true;
	}

	public Webpage downloadAndStore() throws IOException{
//	public Webpage downloadAndStore(DatabaseUtil dbManager) throws IOException{
		String doc = null;
		String absURL = client.getAbsoluteURL();
		Date lastModified = null;
		if(headersFromServer.containsKey("last-modified")){
			String lastStr = headersFromServer.get("last-modified");
			lastModified = DatabaseUtil.getDateFromString(lastStr);
		}

//		Webpage webpage = dbManager.getWebpage(absURL);
		Webpage webpage = DatabaseUtil.getWebpage(absURL);

		// if not in database, download and store
		if(webpage == null){
			log(" -> Downloading...");
			doc = client.getContentFromURL(absURL);
//			System.out.println("[to store in db]\t" + absURL);   //uncomment to see store page
			webpage = new Webpage(absURL, 
					new Date(), contentSize, contentType, doc);
//			dbManager.putWebpage(webpage);
			DatabaseUtil.putWebpage(webpage);
		}

		// if in database, retrieve file last crawled if file not changed 
		else{
			Date lastCrawled = webpage.getLastCrawled();
			if(lastCrawled.after(lastModified)){
				log("->Got cached file.");
				doc = webpage.getContent();
			}else{
				doc = client.getContentFromURL(absURL);
				DatabaseUtil.deleteWebpage(absURL);
//				dbManager.deleteWebpage(absURL);
				Webpage newpage = new Webpage(absURL, 
						new Date(), contentSize, contentType, doc);
//				dbManager.putWebpage(newpage);
				DatabaseUtil.putWebpage(newpage);

			}
		}
		//debug
//		Webpage p = DatabaseUtil.getWebpage(absURL);   // uncomment to see cached file
//		System.out.println("[db]\t" + p.getURL());
//		System.out.println("[db]\t" + p.getLastCrawled().toString());
//		System.out.println("[db]\t" + p.getSize());
//		System.out.println("[db]\t" + p.getType());
//		System.out.println("[db]\t" + p.getContent());

		return webpage;
	}


	/**
	 * store rules information from robots.txt 
	 */
	public void parseExclusionRules(String rulesText){

		String robot = "";
		String bannedURLs = "";
		long delay = 0;

		Pattern pattern = Pattern.compile("(\\S+)(:[ ]?)(\\S+)([ ]?)");
		Matcher matcher = pattern.matcher(rulesText);
		while(matcher.find()){

			String key = matcher.group(1);
			String value = matcher.group(3);

			if(key.equalsIgnoreCase("User-agent")) {
				robot = value;
				// * is not effective if I fould myself
				if(robot.equals("cis455crawler")) bannedURLs = ""; 
			}

			else if(key.equalsIgnoreCase("Disallow")){ 
				if(robot.equals("cis455crawler") || robot.equals("*"))
					bannedURLs += value + ";";
			}

			else if(key.equalsIgnoreCase("Crawl-delay")){
				if(robot.equals("cis455crawler") || robot.equals("*"))
					delay =  Integer.parseInt(value);
			}
		}
		hostExclusionMap.put(host, bannedURLs);
		hostIntervalMap.put(host, delay);


	}



	/** @return true is the document contains the same text */
	public boolean contentSeenTest(String content){
		if(allUniqueContents.contains(content))
			return true;	
		else return false;
	}


	/**
	 *  System.out.println
	 */
	private static void log(Object value){
		System.out.print(String.valueOf(value));
	}

}
