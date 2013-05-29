package edu.upenn.cis455.storage;

import java.util.Date;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class Webpage {
	
	public Webpage(){}
	
	@PrimaryKey
	private String absURL;

	private Date lastCrawled;
	private double size;
	private String type;
	private String content;
	
	public Webpage(String url, Date lastCrawled, double size, String type, String doc){
		this.absURL = url;
		this.lastCrawled = lastCrawled;
		this.size = size;
		this.type = type;
		this.content = doc;
	}
	
	public String getURL(){return absURL;}
	public Date getLastCrawled(){return lastCrawled;}
	public double getSize(){return size;}
	public String getType(){return type;}
	public String getContent(){return content;}
}
