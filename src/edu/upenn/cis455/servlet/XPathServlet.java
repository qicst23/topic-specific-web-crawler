package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class XPathServlet extends HttpServlet {

	public void init(ServletConfig config) throws ServletException {
		System.out.println("XPathServlet has started!");
	}


	// Interface to Users: input form of XPATH and URL 
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// xpaths and urls for testing
		final String[] testPaths = {"/html/body/h3",
				"/courses/course[@id=\"cis555\"]/term",
				"/dwml/data/parameters/temperature[@type=\"minimum\"]/name", 
				"/note/from[contains(text(), \"Ja\")]",
				"/note/from[text()=\"Ja ni\"]",
				"/category/cd/title]text()=\"Eros\"[", 
		"/foo/doo[aa/bb[cc[text() =\"crazy?-()+,./:=;!*#@$_[]~^>|<`~\"][@attr=\"someAttr\"]]][dd/ee]/ff/gg[contains(text( ),\"textContained\")][hh]"};
		final String[] testUrls = {"http://www.htmldog.com/examples/headings1.html", 
				"http://cis555.co.nf/xml/courses.xml",
				"http://localhost:8080/HW2/res.xml",
				"http://crawltest.cis.upenn.edu/misc/weather.xml",
				"http://www.w3schools.com/xml/note.xml",
		"http://www.htmldog.com/examples/headings1.html"};

		PrintWriter out = response.getWriter();
		out.println("<html><body>");
		setStyle(out);
		out.println("<div id='wrapper' class='xform'>");
		out.println("<form action='handler' method = 'post'>");
		out.println("<h1>XPath Engine</h1>");
		out.println("<p>cis555-hw2ms1 | Yayang Tian | yaytian@cis.upenn.edu</p>"); 

		// xpath
		out.println("<label>XPath <span class='hint'>path to xml elem</span></label>");
		out.println("<input type='text' name='xpath' id='xpath' value='" + testPaths[0] + "' />");
		// url
		out.println("<label>Url <span class='hint'>website of xml</span></label>");
		out.println("<input type='text' name='url' id='url' value='" + testUrls[0] + "'/>");

		out.println("<input type='submit' class='submit' value='Query'/>");
		out.println("</form></body></html>");
	}


	public void destroy() {
//		System.out.println("XPathServlet has destroyed!");
	}

	public void setStyle(PrintWriter out){
		out.println("<head><style>");

		// Default font
		out.println("body {font-family: 'Droid Sans', 'Trebuchet Ms', verdana; font-size: 16px;}");
		out.println("p,h1,form,button {border: 0; margin: 0; padding: 0;}");

		// Form layout
		out.println(".xform {margin: 0 auto; width: 480px; height: 218px; padding: 30px;}");

		// Form background & font
		out.println("#wrapper {border: solid 2px #b7ddf2; background: #ebf4fb;}");
		out.println("#wrapper h1 {font-size: 18px; font-weight: bold; margin-bottom: 8px;}");
		out.println("#wrapper p {font-size: 12px; color: #666666; margin-bottom: 20px; border-bottom: solid 1px #b7ddf2; " +
				"padding-bottom: 10px;}");

		// label text
		out.println("#wrapper label {display: block; font-weight: bold; text-align: right; width: 140px; float: left;}");
		out.println("#wrapper .hint {color: #666666; display: block; font-size: 12px; font-weight: normal; " +
				"text-align: right; width: 140px;}");

		// input box & submit button
		out.println("#wrapper input {float: left; padding: 4px 2px; border: font-size: 16px; " + "solid 1px #aacfe4; " +
				"width: 300px; height: 30px; margin: 2px 0 20px 10px; overflow: auto;}");
		out.println("#wrapper input.submit {position:relative; margin-left: 328px; width: 120px; height: 30px; " +
				"background: #666666; text-align: center; color: #FFFFFF; font-size: 14px; font-weight: bold;");

		out.println("</style></head>");
	}
}