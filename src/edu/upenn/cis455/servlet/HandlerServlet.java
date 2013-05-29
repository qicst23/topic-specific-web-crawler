package edu.upenn.cis455.servlet;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.w3c.dom.Node;
import org.w3c.tidy.Tidy;

import edu.upenn.cis455.crawler.CrawlerClient;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;

/**
 * When form submitted, trigger a parse and a scan for XPath matches
 * The servlet return success of failure
 */
@WebServlet(name = "handler", urlPatterns = { "/handler" })
public class HandlerServlet extends HttpServlet {

	LinkedList<String> xpathQueue = new LinkedList<String>();

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{

		String xpath = request.getParameter("xpath");
		String url = request.getParameter("url");

		if(xpath == null || url == null ||
				xpath.trim().equals("") || url.trim().equals("")){
			response.sendRedirect(request.getContextPath() + "/xpath");
			return;
		}

		xpath = URLDecoder.decode(xpath, "UTF-8");
		url = URLDecoder.decode(url, "UTF-8");

//		System.out.println("xpath = " + xpath);

		// convert xmlURL -> dom
		Document dom = null;
		try {
			dom = getDomFromUrl(url);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// from xpath -> retrive elem in the dom
		XPathEngineImpl xEngine = new XPathEngineImpl();
		String[] xpaths = new String[1];
		xpaths[0] = xpath;
		xEngine.setXPaths(xpaths);


		// tell users if sucess or not
		PrintWriter out = response.getWriter();
		out.println("<html><body>");
		setStyle(out);
		out.println("<div id='wrapper' class='xform'><h1>Matching Result</h1>");
		out.println("<p class='green'>[URL] "+ url +"</p>");
		if(dom == null) out.println("<br>File does not exist!");
		else{
			out.println("<table><tr><th>xpath</th><th>matched?</th></tr>");
			boolean[] existed = xEngine.evaluate(dom);
			for(int i = 0; i < existed.length; i ++){
				if(existed[i])
					out.println("<tr><td>" + xpaths[i] + "</td><td class='green'>Yes</td><tr>");
				else
					out.println("<tr><td>" + xpaths[i] + "</td><td class='red'>No</td><tr>");
			}
			out.println("</table></div></body></html>");
		}
	}


	/* download page, and convert html/xml -> dom tree */
	public Document getDomFromUrl(String url) throws UnknownHostException, IOException, InterruptedException{
		if (!url.contains("http://"))
			url = "http://" + url;

		CrawlerClient client = new CrawlerClient(url);

		return client.getDomFromURL(url);
	}



	public void setStyle(PrintWriter out){
		out.println("<head><style>");

		// Default font
		out.println("body {font-family: 'Droid Sans', 'Trebuchet Ms', verdana; font-size: 14px;}");
		out.println("p,h1,form,button {border: 0; margin: 0; padding: 0;}");

		// Form layout
		out.println(".xform {margin: 0 auto; width: auto; height: 218px; display: inline-block; padding: 30px;}");

		// Form background & font
		out.println("#wrapper {border: solid 2px #b7ddf2; background: #ebf4fb;}");
		out.println("#wrapper h1 {font-size: 18px; font-weight: bold; margin-bottom: 8x;}");
		out.println("#wrapper p {font-size: 13px; color: #666666; margin-bottom: 20px; " +
				"padding:5 3 5 3px; border-bottom: solid 1px #b7ddf2;}");
		out.println("#wrapper .green{color:green;}");
		out.println("#wrapper .red{color:red}");

		//table
		out.println("table{border-collapse: collapse; margin-left: auto; margin-right: auto; width=100%;} ");
		out.println("th{padding: 3 15 3 15px; border-bottom: 2px solid #b7ddf3; color: #666666}");
		out.println("td{padding: 3 15 3 15px; border-bottom: 2px solid #b7ddf3; }");

		// input box & submit button
		out.println("#wrapper input {float: left; padding: 4px 2px; border: font-size: 12px; " + "solid 1px #aacfe4; " +
				"width: 300px; height: 30px; margin: 2px 0 20px 10px; overflow: auto;}");
		out.println("#wrapper input.submit {position:relative; margin-left: 328px; width: 120px; height: 30px; " +
				"background: #666666; text-align: center; color: #FFFFFF; font-size: 14px; font-weight: bold;");

		out.println("</style></head>");
	}

}
