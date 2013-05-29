package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import oracle.net.aso.r;

import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.DatabaseUtil;
import edu.upenn.cis455.storage.User;
import edu.upenn.cis455.storage.Webpage;

public class CrawlerServlet extends HttpServlet {

	//DAO types
	final int LOGIN = 1;
	final int REGISTER = 2; 
	final int CHANNEL = 3;
	final int XML = 4;
	final int LOGOUT = 5;
	final int DELETE = 6;
	final int SUBSCRIBE = 7;

	/*------------------------------doGet-----------------------------------------*/
	/**
	 *  ENTRANCE & GET RESOURCES & REDIRECTING 
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse resp)throws ServletException, IOException {
		String userpage = req.getParameter("page");
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();
		out.println("<html><body>");
		setStyle(out);
		if(userpage == null) showLoginPage(out);
		else{
			System.out.println("[page of get]" + userpage);
			int page = Integer.parseInt(userpage);

			switch(page){

			/** 1. Login */
			case LOGIN:
				showLoginPage(out);
				break;

				/** 2. Register */
			case REGISTER:
				showRegisterPage(out);
				break;

				/** 3. Show all channels */
			case CHANNEL: 
				showChannelsPage(out, req);
				break;

				/** 4. Show all XMLs in a channel */
			case XML:   
				showXMLPage(out, req);
				break;

				/** 5. Log out page */
			case LOGOUT:
				showLogoutPage(out, req);
				break;

				/** 6. Delete a channel */
			case DELETE:
				deleteChannel(req, resp);
				break;

				/** Default = Login  */
			default:  
				showLoginPage(out);
				break;
			}
		}
		out.println("</body></html>");
		//		DatabaseUtil.close();
	}


	/*------------------------------doPost---------------------------------------*/

	/**
	 * FORM POSTING 
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
		// get page
		//		String userpage = (String) req.getSession().getAttribute("page");
		String userpage = req.getParameter("page");
		String username;
		String password;
		System.out.println("[page of post]" + userpage);

		if(userpage == null) {
			System.out.println("No pages");
			doGet(req, resp);
			return;
		}

		// setup database
		setDBRoot();
		PrintWriter out = resp.getWriter();

		int page = Integer.parseInt(userpage);
		switch(page){

		/** 1. Register */
		case REGISTER:
			username = req.getParameter("username");
			password = req.getParameter("password");
			if(username.isEmpty() || password.isEmpty()){
				doGet(req, resp);
			}
			if(DatabaseUtil.containsUser(username)){
				System.out.println("Register. User already existed.");
				showResultPage("existed", out);
			}
			else{
				User newUser = new User(username, password);
				DatabaseUtil.putUser(newUser);
				showResultPage("registered", out);
			}
			System.out.println("[usr]\t" + username);
			System.out.println("[pwd]\t" + password);
			break;

			/** 2. Log in */
		case LOGIN:
			username = req.getParameter("username");
			password = req.getParameter("password");
			if(DatabaseUtil.passAuthentication(username, password)){
				req.getSession().setAttribute("loggedUser", username);
				resp.sendRedirect(req.getContextPath() + "/crawler?page=3");
			}
			else {
				showResultPage("incorrect", out);
			}
			break;

			/** add channels to channels and user account*/
		case CHANNEL:
			String cname = req.getParameter("cname");
			String xpaths = req.getParameter("xpaths");
			String xslURL = req.getParameter("url");

			// Add channels
			if(cname == null || xpaths == null || xslURL == null){
				showResultPage("badchannel", out);
			}
			if(DatabaseUtil.containsChannel(cname)){
				showResultPage("channelExisted", out);
			}
			ArrayList<String>xpathsArray = new ArrayList<String>();
			for(String piece : xpaths.split("\\s+")){
				xpathsArray.add(piece);
			}
			Channel newChannel = new Channel(cname, xpathsArray, xslURL);
			username = (String) req.getSession().getAttribute("loggedUser");
			DatabaseUtil.putChannel(newChannel);

			// update user
			User owner = DatabaseUtil.getUser(username);
			owner.putChannel(cname);
			DatabaseUtil.putUser(owner);

			// debug
			Channel c = DatabaseUtil.getChannel(cname);
			System.out.println("\n[stored?]\t" + c.getName());
			System.out.println("[stored?]\t" + c.getXSLURL() + "\n");
			doGet(req, resp);
			break;
		}

	}

	/*------------------------------Show Pages-----------------------------------*/

	/**######## ENTRANCE PAGE  ##################### */

	public void showLoginPage(PrintWriter out){

		/** Show Login */
		out.println("<form action='crawler' method = 'POST' >");
		out.println("<h1>XPath Crawler: Log In</h1>");
		out.println("<p>cis555-hw2ms2 | Yayang Tian | yaytian@cis.upenn.edu</p><br>"); 
		out.println("<label>Username <span class='hint'>log in channels</span></label>");
		out.println("<input type='text' id='username' name='username' value='" + "' />");
		out.println("<label>Password <span class='hint'>authentication</span></label>");
		out.println("<input type='text' id='password' name='password' value='" +  "'/><br>");
		out.println("<br><input type='submit' name='login' class='btn' value='login'/>");
		out.println("<input type='hidden' name='page' class='btn' value='1'/>");
		out.println("</form>");//form end

		/** Sign up  */
		out.println("<form action='crawler' method='GET'>");
		out.println("<input type='submit' name='action' class='btn' value='register'/>");
		out.println("<input type='hidden' name='page' class='btn' value='2'/></form>");
		/** Show channels */
		out.println("<form action='crawler' method='GET'>");
		out.println("<input type='submit' name='action' class='btn' value='show channels'/>");
		out.println("<input type='hidden' name='page' class='btn' value='3'/></form>");
		/**  log out */
		out.println("<form action='crawler' method='GET'>");
		out.println("<input type='submit' name='action' class='btn' value='logout'/>");
		out.println("<input type='hidden' name='page' class='btn' value='5'/></form>");
	}
	/**############ ENTRANCE PAGE END###################################*/

	public void showRegisterPage(PrintWriter out){
		out.println("<form action='crawler' method = 'POST' id='wrapper' class='xform'>");
		out.println("<h1>Create New Account</h1>");
		// username
		out.println("<label>Username <span class='hint'>to view channels</span></label>");
		out.println("<input type='text' name='username' value='" + "' />");
		// password
		out.println("<label>Password <span class='hint'>to have privacy</span></label>");
		out.println("<input type='text' name='password'  value='" +  "'/>");
		// OK button
		out.println("<input type='submit' class='btn' value='OK'/>");
		out.println("<input type='hidden' name='page' value='2'/>");
		out.println("</form>");
	}


	public void showLogoutPage(PrintWriter out, HttpServletRequest req){
		req.getSession().setAttribute("loggedUser", null);
		showResultPage("logout", out);
		out.println("You have been logged out<a href='crawler?page=1>Back</a>");
	}


	/** 
	 * Show Channels Pages
	 */
	public void showChannelsPage(PrintWriter out, HttpServletRequest req){
		String loggedUser = (String) req.getSession().getAttribute("loggedUser");
		setDBRoot();
		ArrayList<Channel> channels = DatabaseUtil.getChannels();
		if(channels == null || channels.size() == 0) 
			out.println("No channels available.");
		else{
			out.println("<br><br>All Channels on the Systems:<table>");

			// if not logged, only can see names
			if(loggedUser == null){
				out.println("<tr><th>Channel Name</th></tr>");
				for(Channel ch : channels){
					out.println("<tr><td>" + ch.getName() + "</td></tr>");
				}
				out.println("You are not logged in. To see channel contents, please <a href='?page=1'>Log In</a>");
			}
			// if logged users, they can add/delete
			else{  
				out.println("<tr><th>Channel Name</th><th>page</th><tr>");
				for(Channel ch : channels){
					String cname = ch.getName();

					// display add/delete pages if this channel belongs to user
					if(DatabaseUtil.userHasChannel(loggedUser, cname)){
						System.out.println("[get]\t" + "<a href='?page=4&cname=" + cname + "'>" + cname + "</a>");
						out.println("<tr><td><a href='?page=4&cname=" + cname + "'>" + cname + 
								"</a></td><td><a href='?page=6&cname=" + cname + "'>delete</a></td></tr>");
					}else{
						out.println("<tr> <td>" + cname + "</td> <td><a href='?page=7'>subscribe</a></td></tr>");
					}
				}
			}
			out.println("</table><br><br><br>");
		}

		// if logged, also show add channel form
		if(loggedUser != null)showAddChannelForm(out);
	}

	/**
	 * Add Channels Form
	 */
	public void showAddChannelForm(PrintWriter out){
		out.println("<form action='crawler' method = 'POST'>");
		out.println("<h1>Add Channel Here:</h1>");
		// channel name
		out.println("<label>Channel Name <span class='hint'>path of xml elem</span></label>");
		out.println("<input type='text' name='cname' id='cname' value='" + "' />");
		// xpaths
		out.println("<label>XPaths <span class='hint'>please separate by space</span></label>");
		out.println("<input type='text' name='xpaths' id='xpaths' value='" + "' />");
		// xsl url
		out.println("<label>XSL Stylesheet URL <span class='hint'>website of xml</span></label>");
		out.println("<input type='text' name='url' id='url' value='" +  "'/>");
		out.println("<input type='submit' class='btn' value='Add'/>");
		out.println("<input type='hidden' name='page' value='3'/>");
		out.println("</form>");
		out.println("<a href='crawler'>Back</a>");
	}

	/**
	 *  Initial welcome interface 
	 */
	public void showXMLPage(PrintWriter out, HttpServletRequest req){
		String loggedUser = (String) req.getSession().getAttribute("loggedUser");
		String cname = req.getParameter("cname");
		
		System.out.println("[debug!!!!]\t" + loggedUser);
		System.out.println("[debug !!!!!]\t" + cname);
		
		setDBRoot();
		Channel channel = DatabaseUtil.getChannel(cname);

		// if not logged, only can see names
		if(loggedUser == null)
			out.println("You are not logged in. To see channel contents, please <a href='?page=1'>Log In</a>");
		else if(channel == null) 
			out.println("No XMLs for this channel. It's a new channel.");

		// if logged users & valid channel, display matched XMLs
		else{
			out.println("Welcome to channel:  " + channel.getName() + "<br>");
			out.println("<br><br>All XMLs matched on the Channel:<br><br><br><br>");
			
			// begin !!!
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
			String xslPath = channel.getXSLURL();
			String head = "";
			
			out.println(head);
			out.println("<documentcollection>");
			

			for(String mURL : channel.getMatchedURLs()){
				
				Webpage webpage = DatabaseUtil.getWebpage(mURL);
				
				String xmlContent = webpage.getContent();
				StringBuffer cBuf = new StringBuffer();
				if(xmlContent.contains("?>")){
					head = xmlContent.substring(0, xmlContent.indexOf("?>") + 2) +
							"<xml-stylesheet type=\"text/xsl\" href=\"" + xslPath + "\"?>";
					int startId = xmlContent.indexOf("?>") + 2;
					cBuf = new StringBuffer(xmlContent.substring(startId));
				}
				String lastCrawled = dateFormat.format(webpage.getLastCrawled()) + "T" + 
						dateFormat.format(webpage.getLastCrawled());
			    
				// Print one document
				out.println("<document crawled=\"" + lastCrawled +
			    		"\" location=\"" + webpage.getURL() + "\">");
			    out.println(xmlContent);
				out.println("</document>");
				// End printing one document
			}
			
			out.println("</documentcollection>");
		}

		// if logged, also show add channel form
		if(loggedUser != null)showAddChannelForm(out);
	}

	/**
	 * Result Page
	 */
	public void showResultPage(String result, PrintWriter out){
		String info = "";
		out.println("<html><body>");
		if(result.equals("existed"))
			info = "User already existed. <a href='crawler'>Back</a>";
		if(result.equals("registered"))
			info = "You have successfully registered. Thanks! + <a href='crawler'>Back</a>";
		if(result.equals("incorrect"))
			info = "Incorrect username or password. <a href='crawler'>Back</a>";
		if(result.equals("badchannel"))
			info = "Channel/XPaths/XSL URL cannot be empty.<a href='crawler'>Back";
		if(result.equals("channelExisted"))
			info = "Channel already existed. <a href='crawler'>Back</a>";
		if(result.equals("deleted"))
			info = "Channel deleted. <a href='crawler?page=3'>Back</a>";
		if(result.equals("logout"))
			info = "You've been sucessfully logged out. <a href='crawler?page=1'>Back</a>";
		out.println(info + "</body></html>");
	}

	/*------------------------------Operations----------------------------------------*/
	public void deleteChannel(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
		String username = (String) req.getSession().getAttribute("loggedUser");
		if(username == null)doGet(req, resp);
		String cname = req.getParameter("cname");
		DatabaseUtil.deleteChannel(username, cname);
		showResultPage("deleted", resp.getWriter());
	}


	/*------------------------------Others----------------------------------------*/

	public void setDBRoot(){
		String dbRoot = getServletConfig().getServletContext().getInitParameter("BDBstore");
		DatabaseUtil.setupContext(dbRoot);
	}

	public void destroy() {
		System.out.println("XPathServlet has destroyed!");
	}

	public void setStyle(PrintWriter out){
		out.println("<head><style>");

		// Default font & container
		out.println("body {font-family: 'Droid Sans', 'Trebuchet Ms', verdana; font-size: 16px;}");
		out.println("p,h1,form,button {border: 0; margin: 0; padding: 0;}");

		// label text
		out.println("form {background-color: #E7F3FF; width: 800px; height: 70px; padding: 30px; margin-left:auto; margin-right:auto;text-align:left;}");
		out.println("form label {display: block; font-weight: bold; text-align: right; width: 120px; float: left;}");
		out.println("form .hint {color: #666666; display: block; font-size: 12px; font-weight: normal; " +
				"text-align: right; width: 120px;}");

		//table
		out.println("table{border-collapse: collapse; margin-left: auto; margin-right: auto; width=100%;} ");
		out.println("th{padding: 3 15 3 15px; border-bottom: 2px solid #b7ddf3; color: #666666}");
		out.println("td{padding: 3 15 3 15px; border-bottom: 2px solid #b7ddf3; }");

		// input box & submit button
		out.println("input {float: left; padding: 4px 2px; font-size: 16px; " +
				"width: 250px; height: 30px; margin: 2px 0 20px 10px; overflow: auto;}");
		out.println("input .xpath{height: 300px;}");
		out.println("input.btn {display: inline; margin-left: 20px;width: 160px; height: 30px; " + 
				"background: #666666; text-align: center; color: #FFFFFF; font-size: 16px;");

		out.println("</style></head>");
	}

	/**  System.out.println */
	private static void log(Object value){
		System.out.println(String.valueOf(value));
	}
	private static final long serialVersionUID = 1L;
	private String username = null;
}