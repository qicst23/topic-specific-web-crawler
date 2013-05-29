package edu.upenn.cis455.xpathengine;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class XPathEngineImpl implements XPathEngine {

	private String[] xpaths;

	private int pathId;
	private HashMap<String, ArrayList<String>> nodeTestsMap;
	
	static boolean openLog = false;

	public XPathEngineImpl() {
		// Do NOT add arguments to the constructor!!
	}

	public void setXPaths(String[] s) {
		/* TODO: Store the XPath expressions that are given to this method */
		xpaths = s;
	}


	/*------------------------------------------------------------------------*
	 * 			First Part:  Check if XPath Obeys Vaid Format
	 *------------------------------------------------------------------------*/
	public boolean isValid(int i) {
		/* TODO: Check which of the XPath expressions are valid */

		/** I define "level" to be an xapth without another xpath
		 * i.e. from "/" to next "/" with not open bracket inside
		 * 
		 * xpath = axis step
		 *       = axis + node[(test)]*(axis step)?
		 *       = axis + level + xpath?
		 *       
		 */

		log("\n--- Validating XPath", xpaths[i] + "  ---\n"); 

		pathId = i;
		String path = xpaths[pathId];
		if(!path.startsWith("/") || path.equals("/"))return false;

		// Store for global reuse of evaluation
		nodeTestsMap = new HashMap<String,ArrayList<String>>();

		// Check validity recursively, step -> level -> test 
		return isValidStep(path.substring(1));    
	}


	/**
	 *  @return true if all levels of this step are valid
	 *  */
	public boolean isValidStep(String step){
		log("step", step);

		ArrayList<String> levels = retrieveLevels(step);
		for(String level : levels){
			if (!isValidLevel(level))
				return false;
		}
		return true;
	}


	/**
	 *  @return true is level -> nodename[(test)]* are valid
	 *  */
	public boolean isValidLevel(String level){
		log("level", level);

		// level -> nodename[(test)]*    
		String levelRegex = "(\\s)*([A-Z_a-z][A-Z_a-z-.0-9]*)(\\s)*(\\[.+\\])*";
		Pattern levelPattern = Pattern.compile(levelRegex);
		Matcher levelMatcher = levelPattern.matcher(level);

		// e.g. _&-hi-[bbb/ccc/[d/e][f/g]]
		if(levelMatcher.matches()){  

			String nodeName = null;
			StringBuffer thisTest = new StringBuffer(); 

			Stack<Character> bracketStack = new Stack<Character>();
			boolean gotNodeName = false;
			boolean inQuote = false;
			boolean inTest = false;

			for(int lp = 0; lp < level.length(); lp ++){
				char ch = level.charAt(lp);

				switch(ch){
				case '"':
					inQuote = !inQuote;
					break;
				case '[':
					if(!inQuote) {
						bracketStack.push(ch);
						inTest = true;
						if(!gotNodeName){
							nodeName = level.substring(0, lp);
							gotNodeName = true; 
						}
					}
					break;
				case  ']' :
					if(!inQuote){
						bracketStack.pop();
						if(bracketStack.isEmpty()){  // if got a test segament
							inTest = false;

							if(!isValidTest(thisTest.toString())) return false;
							// if it's a valid test, cache it
							if(nodeTestsMap.containsKey(nodeName))
								nodeTestsMap.get(nodeName).add(thisTest.toString());
							else{
								ArrayList<String> tests = new ArrayList<String>();
								tests.add(thisTest.toString());
								nodeTestsMap.put(nodeName, tests);  
							}
							thisTest.setLength(0);
						}
					}
					break;
				}
				if(inTest) thisTest.append(ch);
			}
			return true;
		}
		// not match at all
		log("Level not valid at all",level);
		return false;
	}

	/**
	 * 
	 * @return true if either of the 4 format is valid
	 */
	public boolean isValidTest(String testWithBrackets){
		String test = testWithBrackets.substring(1, (testWithBrackets.length()));
		log("\t test" , test);
		// ([test])*
		String regex = "(((\\s)*text(\\s)*\\((\\s)*\\)(\\s)*\\=(\\s)*\\\"[^\\\"]+\\\"(\\s)*)|"  +    // test patterns 2:  text() = "..."
				"((\\s)*contains(\\s)*\\((\\s)*text(\\s)*\\((\\s)*\\)(\\s)*,(\\s)*\\\"[^\\\"]+\\\"(\\s)*\\))|" + // test patterns 3:  contains(text(), "...")
				"((\\s*)\\@(\\s*)([A-Z_a-z][A-Z_a-z0-9-.]*)(\\s*)\\=\\\"[^\\\"]+\\\"(\\s*)))";              // test patterns 4:  @attname = "..."
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(test);

		// (1)  either of 3 simple format valid
		if(matcher.matches()){
			log("\t test valid (simple)");
			return true;
		}
		// (2) nested test = another step
		else {log("\t further validating (nested)");
		return isValidStep(test);
		}
	}


	/*------------------------------------------------------------------------*
	 * 			Second Part:  Check if XPath Exists in XML Dom
	 *------------------------------------------------------------------------*/

	public boolean[] evaluate(Document d) { 
		/* TODO: Check whether the document matches the XPath expressions */

		// no dom at all
		boolean[]  matches = new boolean[xpaths.length];
		if(d == null){
			return matches;
		}

		// vadidate xpath one by one
		for(int i = 0; i < xpaths.length; i ++){
			pathId = i;

			boolean valid = isValid(i);
			System.out.println("[xpath]" + xpaths[i]);
			System.out.print("-> Valid? " + valid);

			if(valid){
				log("\n--- Matching Document", xpaths[i] + "  ---"); 
				String firstStep = xpaths[i].substring(1);
				
				ArrayList<Node> rootNodeArray = new ArrayList<Node>();
				rootNodeArray.add(d.getDocumentElement());
				
				matches[i] = stepMatched(firstStep, rootNodeArray);
				System.out.print(" -> Match? " +  matches[i] + ".");
			}
		}
		System.out.println();
		return matches;
	}


	/**
	 * Evaluate Xpath level by level. Traverse Dom level by level.
     * xpath = / level / level / level
     *       = / nodename(tests)* / nodename(test)* / nodename(test)*
     *       
     *
     * @return true if the all level matches
     */
	public boolean stepMatched(String step, ArrayList<Node> thisLevelNodes){
		log("\nstep", step);
		
		ArrayList<String> levels = retrieveLevels(step);
		ArrayList<Node> nextLevelCandidates = thisLevelNodes;
		int numMatched = 0;

		for(String level : levels){
			
			nextLevelCandidates = levelMatched(level, nextLevelCandidates);
			if (nextLevelCandidates == null) {
				log("\tnum of candidate children: ", 0);
				return false;
			
			}
			numMatched = nextLevelCandidates.size();
			log("\tnum of candidate children: ", numMatched);
		}
		return numMatched > 0;
	}

	
	/**
     * @return the nodes of the next matched level; return null if no nodes matched
     */
	public ArrayList<Node> levelMatched(String thisLevelString, ArrayList<Node> nodesToMatch){
		log("\nlevel", thisLevelString);
		
		ArrayList<Node> nextLevelCandidates = new ArrayList<Node>();
		String nodeNameParsed = null;
		
		if(!thisLevelString.contains("[")) nodeNameParsed = thisLevelString.trim();
		else	nodeNameParsed = thisLevelString.split("[\\[]")[0];
		
		for(Node node: nodesToMatch){

			// if this level doesn't have tests
			if(!nodeTestsMap.containsKey(nodeNameParsed)){
			
				if(nodeNameParsed.equals(node.getNodeName())) {
					
					NodeList children = node.getChildNodes();
					for(int c = 0; c < children.getLength(); c ++){
						nextLevelCandidates.add(children.item(c));
					}
				}
			}
			else{
				ArrayList<String> tests = nodeTestsMap.get(nodeNameParsed);
				for(String test: tests){
					if(!testMatched(test, node)) continue;
					NodeList children = node.getChildNodes();
					for(int c = 0; c < children.getLength(); c ++){
						nextLevelCandidates.add(children.item(c));
					}
				}
			}
			// got a correct node. we retrive its children for further validation
			
			

		}
		return nextLevelCandidates;
	}


	/**
	 *  @return true if the test matches
	 */
	public boolean testMatched(String testString, Node thisLevelNode){
		String test = testString.substring(1, (testString.length())).trim();
		log("\tteststring",test);
		log("\tnodename", thisLevelNode.getNodeName());
		// ([test])*
		String textReg = "(\\s)*text(\\s)*\\((\\s)*\\)(\\s)*\\=(\\s)*\\\"[^\\\"]+\\\"(\\s)*";      // test patterns 2:  text() = "..."
		String containsReg= "(\\s)*contains(\\s)*\\((\\s)*text(\\s)*\\((\\s)*\\)(\\s)*,(\\s)*\\\"[^\\\"]+\\\"(\\s)*\\)"; // test patterns 3:  contains(text(), "...")
		String attrReg ="(\\s*)\\@(\\s*)([A-Z_a-z][A-Z_a-z0-9-.]*)(\\s*)\\=\\\"[^\\\"]+\\\"(\\s*)";           // test patterns 4:  @attname = "..."

		if(test.matches(textReg)){
			String strText = test.split("\"")[1];  
			Node nodeText = thisLevelNode.getFirstChild();
			if(nodeText != null && nodeText.getNodeType() == Node.TEXT_NODE &&
					nodeText.getNodeValue().equals(strText)) {
				log("\text() match!");
				return true;
			}

		}else if(test.matches(containsReg)){  // 
			String strContains = test.split("\"")[1];  
			Node nodeContains = thisLevelNode.getFirstChild();
			if(nodeContains != null && nodeContains.getNodeType() == Node.TEXT_NODE &&
					nodeContains.getNodeValue().contains(strContains)){
				log("\tcontains match!");
				return true;
			}

		}else if (test.matches(attrReg)){
			String attKey = test.split("\"")[0].split("@")[1].split("=")[0].replace("\\s*","");
			String attValue = test.split("\"")[1].trim(); 
			NamedNodeMap map = thisLevelNode.getAttributes();
			if(map != null){
				Node nodeAttr = map.getNamedItem(attKey); 
				if(nodeAttr != null && attValue.equals(nodeAttr.getNodeValue())){
					log("\tattr match!");
					return true;
				}
			}
		}

		// (2) nested match: test the children
		else {
			log("\nNested Test", test);
			ArrayList<Node> childNodes = new ArrayList<Node>();
			NodeList children = thisLevelNode.getChildNodes();
			for(int ch = 0; ch < children.getLength(); ch ++){
				childNodes.add(children.item(ch));
			}
			return stepMatched(test, childNodes);
		}
		return false;

	}

	
	/**
	 * I define level to be the segment between two slash(without another step)
	 * @return levels parsed from step
	 * 
	 */
	public ArrayList<String> retrieveLevels(String path){	

		ArrayList<String> levels = new ArrayList<String>();

		//directly return if only a nodename
		if(!path.contains("/")){   
			levels.add(path);
			return levels;
		}

		StringBuffer alevel = new StringBuffer();   
		Stack<Character> bracketStack = new Stack<Character>();  

		boolean inQuote = false;
		for(int pid = 0; pid < path.length(); pid ++){
			char ch = path.charAt(pid);
			switch(ch){
			case '"':
				inQuote = !inQuote;
				break;
			case '[':
				if(!inQuote) bracketStack.push(ch);
				break;
			case  ']' :
				if(!inQuote){
					if(bracketStack.isEmpty()){break;}
					bracketStack.pop();
				}
				break;
			case '/':
				if(!inQuote){
					if(!bracketStack.isEmpty()) break; 
					levels.add(alevel.toString());
					alevel.setLength(0);
					continue;
				}
			}
			alevel.append(ch);
		}
		levels.add(alevel.toString());
		return levels;
	}


	/**
	 * @return key-value pairs for debugging
	 */
	private static void log(Object key, Object value){
		if(openLog)
			System.out.println("" + key + " :\t" + String.valueOf(value));
	}
	private static void log(Object value){
		System.out.println(String.valueOf(value));
	}

}
