package edu.upenn.cis455.storage;
import java.util.ArrayList;
public class AddWarPeace {
	
	/** find war and peace and add to channel */
	public static void main(String[] args) {
		if (args.length!=3) {
			System.err.println("[How to use]java AddWarPeace <dbRoot><xslRoot><channelName>" );
			return;
		}
		String dbRoot = args[0];
		String xslRoot = args[1];
		String cname = args[2];
		DatabaseUtil.setupContext(dbRoot);
		
		String xwar = "/rss[@version = \"2.0\"]/channel/item/title[contains(text(), \"war\")]";
		String xpeace = "/rss[@version = \"2.0\"]/channel/item/description[contains(text(), \"peace\")]";
		
		ArrayList<String> xpaths = new ArrayList<String>();
		xpaths.add(xwar);
		xpaths.add(xpeace);
		
		Channel newChannel = new Channel(cname, xpaths, xslRoot);
		DatabaseUtil.putChannel(newChannel);
	}

}