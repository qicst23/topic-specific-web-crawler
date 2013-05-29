package edu.upenn.cis455.storage;

import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class Channel {
	
	@PrimaryKey
	private String channelName;
	
	
	private ArrayList<String> xpaths;
	private String xslURL;
	
	// Also store matched URLs
	private ArrayList<String> matchedURLs = new ArrayList<String>(); 
	
	public Channel(){
	}
	
	public Channel(String cname, ArrayList<String>paths, String url) {
		this.channelName = cname;
		this.xpaths = paths;
		this.xslURL = url;
	}
	
	public String getName(){
		return channelName;
	}
	public ArrayList<String> getXPaths(){
		return xpaths;
	}
	
	public void putMatchedURL(String url){
		matchedURLs.add(url);
	}
	
	public ArrayList<String> getMatchedURLs(){
		return matchedURLs;
	}
	public String getXSLURL(){
		return xslURL;
	}

}
