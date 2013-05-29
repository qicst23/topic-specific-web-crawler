package test.edu.upenn.cis455;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.upenn.cis455.xpathengine.XPathEngineImpl;
import junit.framework.TestCase;

public class XPathTestCases extends TestCase {
    private String servletUrl = "http://localhost:8080/HW2/xpath";
	 private String xmlUrl = "http://cis555.co.nf/xml/courses.xml";

     public void testText() throws UnknownHostException, IOException, InterruptedException
     {
             String xpath = "/courses/course/term[text() = \"spring\"]";
             XPathEngineImpl engine = new XPathEngineImpl();
             String[] xpaths = new String[1];
       		 xpaths[0] = xpath;
       		 engine.setXPaths(xpaths);
       		 Document dom = getDomFromUrl(xmlUrl);
       		 boolean[] existed = engine.evaluate(dom);
             assertEquals(true, existed[0]);
     }
     
	public void testContains() throws UnknownHostException, IOException, InterruptedException
     {
             String xpath =  "/courses/course/term[contains(text(), \"in g\"]";
             XPathEngineImpl engine = new XPathEngineImpl();
             String[] xpaths = new String[1];
       		 xpaths[0] = xpath;
       		 engine.setXPaths(xpaths);
       		 Document dom = getDomFromUrl(xmlUrl);
       		 boolean[] existed = engine.evaluate(dom);
             assertEquals(false, existed[0]);
     }
     
     public void testAtt() throws UnknownHostException, IOException, InterruptedException
     {
             String xpath =  "/courses/course/term[text() = \"spring\"]";;
             XPathEngineImpl engine = new XPathEngineImpl();
             String[] xpaths = new String[1];
       		 xpaths[0] = xpath;
       		 engine.setXPaths(xpaths);
       		 Document dom = getDomFromUrl(xmlUrl);
       		 boolean[] existed = engine.evaluate(dom);
             assertEquals(true, existed[0]);
     }
     
     
     /* download page, and convert html/xml -> dom tree */
 	public Document getDomFromUrl(String url) throws UnknownHostException, IOException, InterruptedException{

 		URL urlObject;
 		try {
 			if (!url.contains("http://"))
 				url = "http://" + url;
 			urlObject = new URL(url);
 		} catch (MalformedURLException e) {
 			System.out.println(e.getMessage());
 			return null;
 		}
 		

 		try {
 			Document dom = DocumentBuilderFactory.newInstance().
 					newDocumentBuilder().parse(urlObject.openStream());  //#### Change to my own client if have time
// 			printDomTree(dom);
 			return dom;
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		}

 		/* Client Implementation  */
// 		Socket serverSocket = new Socket(urlObject.getHost(), 80);
// 		BufferedReader in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
// 		PrintWriter out = new PrintWriter(serverSocket.getOutputStream(), true);
 //
// 		// send initial status & headers to server
// 		StringBuffer toServer = new StringBuffer();
// 		toServer.append("GET " + urlObject.getPath() + " HTTP/1.1\r\n");
// 		toServer.append("User-Agent: cis455crawler\r\n");
// 		toServer.append("Host: " + urlObject.getHost() + ":80\r\n");
// 		toServer.append("Connection: close\r\n\r\n");
// 		System.out.println("Request: " + toServer.toString());
// 		
// 		// parse respond from server
// 		StringBuffer toClientBody = new StringBuffer(); 
// 		toClientBody = new StringBuffer();
// 		
// 		String lenHeader = "content-length: 1024";
// 		String typeHeader = "text/html";
// 		
// 		String thisLine = in.readLine();
// 		while((thisLine != null && !thisLine.equals(""))){
// 			if(thisLine.toLowerCase().startsWith("connection: close")){
// 				throw new InterruptedException();
// 			}
// 			//			System.out.println(thisLine);
// 			if(thisLine.toLowerCase().startsWith("content-type:")){
// 				lenHeader =  thisLine;
// 			}
// 			if(thisLine.toLowerCase().startsWith("content-length:")){
// 				typeHeader = thisLine;
// 			}
// 		}
// 		
// 		// read respond body of with xml/html files
// 		int contentlen = Integer.parseInt(lenHeader.split(":")[1].trim());
// 		int readChar = 0;
// 		while (true) {
// 			toClientBody.append((char)in.read());
// 			readChar ++;
// 			if(readChar >= contentlen)break;
// 		}
// 		
// 		System.out.println("xml read: " + toClientBody.toString());
// 		out.flush();
// 		out.close();
// 		in.close();
// 		serverSocket.close();
 		
 		return null;
 	}


}

