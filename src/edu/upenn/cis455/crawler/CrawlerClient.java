package edu.upenn.cis455.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

public class CrawlerClient {

	private String host;
	private int port;
	
	private URL urlObject;
	private String currentURL;
	private String currentPath;
	
	private String contentType = null;

	public CrawlerClient(){
		
	}
	public CrawlerClient(String url) throws MalformedURLException{
		setupParams(url);
	}

	/** Set protocol, host, port, path */
	public boolean setupParams(String url){
		try {
			this.urlObject = new URL(url);
		} catch (MalformedURLException e) {
			System.out.println(e.getMessage());
			return false;
		}
		this.host = urlObject.getHost();
		this.currentURL = url;
		this.currentPath = urlObject.getPath();
		if (currentPath == "") currentPath = "/";
		this.port = urlObject.getPort();
		if(this.port == -1) this.port = 80;
		return true;
	}

	
	/** 
	 * @return content from server to test file validity 
	 */
	public String getContentFromURL (String url) throws IOException{
		boolean success  = setupParams(url);
		if(!success) return null;
		
		System.out.print(" -> [Downloading " + currentPath + "]  ");
		Socket socket = new Socket(host, port);
		
		// Send GET request
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		out.write("GET " + currentPath + " HTTP/1.1\r\n");
		out.write("User-Agent: cis455crawler\r\n");
		out.write("Host: " + host + ":80\r\n");
		out.write("Connection: close\r\n\r\n");
		out.flush();
		
		// read GET response 
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		StringBuffer contentBuffer = new StringBuffer();
		String thisLine = in.readLine();
		
		// read initial -> redirect
		if(thisLine.contains("301") || thisLine.contains("302")){
			String newURL = getRedirectURL(in);
			getHeadFromURL(newURL);
		}
		if(!thisLine.endsWith("200 OK")) return null;

		// read head
		thisLine = in.readLine();
		while((thisLine != null && !thisLine.equals(""))){
			String key = thisLine.split(":")[0].trim();
			if(key.equalsIgnoreCase("content-type"))
				contentType = thisLine.split(":")[1].trim();
			thisLine = in.readLine();
		}

		//read body 
		thisLine = in.readLine();
		while(thisLine != null){
			contentBuffer.append(thisLine + "\r\n");
			thisLine = in.readLine();


		}

		out.close();
		in.close();
		socket.close();
		return contentBuffer.toString();
	}
	
	/** @return key-value pair headers from server to test file validity */
	HashMap<String, String> getHeadFromURL (String url) throws IOException{
		boolean success  = setupParams(url);
		if(!success) return null;
		
		Socket socket = new Socket(host, port);
		System.out.print(" -> Send HEAD ");
		// send HEAD request 
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		out.write("HEAD " + currentPath + " HTTP/1.1\r\n");
		out.write("User-Agent: cis455crawler\r\n");
		out.write("Host: " + host + ":80\r\n");
		out.write("Connection: close\r\n\r\n");
		out.flush();

		// read HEAD response
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		HashMap<String, String> respHeaders = new HashMap<String, String>();
		String thisLine = in.readLine(); 

		// read initial -> redirect
		if(thisLine.contains("301") || thisLine.contains("302")){
			String newURL = getRedirectURL(in);
			System.out.print(" -> Redirect to " + new URL(newURL).getPath());
			return getHeadFromURL(newURL);
		}

		if(!thisLine.endsWith("200 OK")) return null;

		// read head
		thisLine = in.readLine();
		while((thisLine != null && !thisLine.equals(""))){
//			System.out.println("\t[head]\t" + thisLine);    // uncomment to see heads 
			String key = thisLine.split(":")[0].trim();
			String value = thisLine.split(":")[1].trim();
			respHeaders.put(key.toLowerCase(), value);
			thisLine = in.readLine();
		}
		out.close();
		in.close();
		socket.close();
		return respHeaders;
	}

	/**
	 * get redirect URL if status = 301 or 302
	 */
	public String getRedirectURL(BufferedReader in) throws IOException{
		String thisLine = in.readLine();
		while(thisLine != null && !thisLine.equals("")){
			if(thisLine.contains(":")) {
				String key = thisLine.split(":")[0];
				if(key.trim().equalsIgnoreCase("Location")){
					String newURL = thisLine.substring(key.length() + 1, thisLine.length());
					return newURL;
				}
			}
			thisLine = in.readLine();
		}


		System.out.println("Invalid redirect page on server.");
		return null;
	}

	
	/** 
	 * @return the content of website crawlered 
	 */
	public Document getDomFromURL(String url) throws UnknownHostException, IOException, InterruptedException{
		String content = getContentFromURL(url);
		if(content == null) return null;

		Document dom = null;

		if(contentType.equals("text/html")){
			Tidy tidy = new Tidy();
			tidy.setDocType("omit");
			tidy.setTidyMark(false);

			StringWriter writer = new StringWriter(content.length());
			dom = tidy.parseDOM(new StringReader(content), writer);
		}
		else if(contentType.equals("text/xml")){
			try {
				dom = DocumentBuilderFactory.newInstance().
						newDocumentBuilder().parse(urlObject.openStream());  //#### Change to my own client if have time
				return dom;
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
		}
		return dom;
	}


	/** 
	 *  @return the content of robots.txt
	 */
	public String downloadRobotRules() throws IOException{
		String robotURL = urlObject.getProtocol() + "://" + urlObject.getHost() + "/robots.txt";
		return getContentFromURL(robotURL);
	}
	
	/**
	 *  return the current url just processed 
	 */ 
	public String getAbsoluteURL(){
		return currentURL;
	}
	/**
	 * @return the current host just processed
	 */
	public String getHost(){
		return host;
	}
}
