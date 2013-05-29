package edu.upenn.cis455.storage;

import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
/**
 * This stores information of users in Berkely DB: username, password, and channel names
 */

@Entity
public class User {
	
	@PrimaryKey
	private String username;

	private String password;
	
	private ArrayList<String> channelNames = new ArrayList<String>();

	
	public User(){
		
	}
	
	public void putChannel(String cname){
		this.channelNames.add(cname);
	}
	
	public User(String usr, String pwd){
		this.username = usr;
		this.password = pwd;
	}

	public String getUsername(){
		return this.username;
	}

	public String getPassword(){
		return this.password;
	}

	public ArrayList<String> getChannelNames(){
		return channelNames;
	}
	public void setChannel(ArrayList<String> channels){
		this.channelNames = channels;
	}

	
	
	public boolean hasChannel(Channel ch){
		System.out.println("[db user]\t" + username);
		System.out.println("[db cname]\t" + ch.getName());
		return channelNames.contains(ch);
		
	}
}
