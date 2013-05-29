package edu.upenn.cis455.storage;

import java.io.File;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;




public class DatabaseUtil {

	private static Environment environment = null;
	private static EntityStore store = null;

	private static PrimaryIndex<String, User> userIndex;
	private static PrimaryIndex<String, Channel> channelIndex;
	private static PrimaryIndex<String, Webpage> webpageIndex;

	private static String dbRoot = null;
	private static File dbDir;

	/*------------------------------- Setup ----------------------------------*/
	//
	//	public static void setRoot(String root){
	//		if(root.equals(dbRoot))return;
	//		dbRoot = root;
	//	}
	public static void setupContext(String root){

		// if same set up, return
		if(environment != null && environment.isValid() && root.equals(dbRoot)) {
			return;
		}

		// find dir to store databse
		dbRoot = root;
		if(dbRoot == null)
			dbRoot = System.getProperty("user.dir") + "/database";

		// create dir
		File dir = new File(dbRoot);
		if(dir.exists()){
			dbDir = dir;
		}else{
			dir.mkdir();
			dbDir = dir;
			System.out.println("Created directory for database!\t");
		}
		
		System.out.println("[status] Database started at " + dbRoot);

		// setting up enviroment
		EnvironmentConfig envConf = new EnvironmentConfig();
		StoreConfig storeConf = new StoreConfig();

		envConf.setAllowCreate(true);
		envConf.setTransactional(true);
		storeConf.setAllowCreate(true);
		//		storeConf.setTransactional(true);

		storeConf.setDeferredWrite(true);
		//		envConf.setLocking(false);

		environment = new Environment(dbDir, envConf);
		store = new EntityStore(environment, "EntityStore", storeConf);

		userIndex = store.getPrimaryIndex(String.class, User.class);
		channelIndex = store.getPrimaryIndex(String.class, Channel.class);
		webpageIndex = store.getPrimaryIndex(String.class, Webpage.class);
	}

	public static void close(){
		try{
			if(store != null)
				store.close();
			if(environment != null)
				environment.close();
		}catch(DatabaseException e){
			System.out.println("Cannot close database");
		}
	}
	/*------------------------------- Webpage --------------------------------*/

	public static void putWebpage(Webpage webpage){
		webpageIndex.put(webpage);
		store.sync();
	}
	public static Webpage getWebpage(String url){
		return webpageIndex.get(url);
	}

	public static void deleteWebpage(String url){
		webpageIndex.delete(url);
	}


	/*------------------------------- User -----------------------------------*/
	public static void putUser(User user){
		userIndex.put(user);
		store.sync();
	}
	public static User getUser(String name){
		return userIndex.get(name);
	}

	public static boolean containsUser(String name){
		return userIndex.contains(name);
	}
	public static boolean passAuthentication(String usr, String pwd){
		if(userIndex.contains(usr))
			if(userIndex.get(usr).getPassword().equals(pwd))
				return true;
		return false;
	}
	/*------------------------------- Channel -----------------------------------*/

	public static void putChannel(Channel ch){
		if(ch == null){
			System.out.println("Channel is null!");
		}
		else 
			channelIndex.put(ch);
		store.sync();
	}

	public static Channel getChannel(String name){
		return channelIndex.get(name);
	}

	public static boolean containsChannel(String cname){
		return channelIndex.contains(cname);
	}
	public static boolean userHasChannel(String username, String cname){
		ArrayList<String> channels = userIndex.get(username).getChannelNames();
		return channels.contains(cname);
	}

	public static ArrayList<Channel> getChannels(){
		ArrayList<Channel> channels = new ArrayList<Channel>();


		EntityCursor<Channel> cursor = channelIndex.entities();
		try{
			Iterator<Channel> iter = cursor.iterator();
			while(iter.hasNext())  channels.add(iter.next());
		}finally{
			cursor.close();
		}
		return channels;
	}
	
	public static void deleteChannel(String username, String cname){
		channelIndex.delete(cname);
		User owner = userIndex.get(username);
		owner.getChannelNames().remove(cname);
		putUser(owner);
	}

	/*------------------------------- Util -----------------------------------*/
	public static Date getDateFromString(String str){
		Date date = null;
		String[] format = {"EEEEE, dd-MMM-yy HH:mm:ss zzz",
				"EEE MMM dd HH:mm:ss yyyy", 
				"EEE, dd MMM yyyy HH:mm:ss zzz",
		"EEE, dd MMM yyyy HH"};
		SimpleDateFormat parser = new SimpleDateFormat(format[0]);
		try {
			date = parser.parse(str);
		} catch (ParseException e) {
			parser = new SimpleDateFormat(format[1]);
			try {
				date = parser.parse(str);
			} catch (ParseException e1) {
				parser = new SimpleDateFormat(format[2]);
				try {
					date = parser.parse(str);
				} catch (ParseException e2) {
					parser = new SimpleDateFormat(format[3]);
					try {
						date = parser.parse(str);
					} catch (ParseException e3) {
						System.out.println("Bad date format.");
						return null;
					}
				}
			}
		}
		return date;
	}

}

