import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class HealthProfileReader {  
	
	Document doc;
    XPath xpath;
   

    public void loadXML() throws ParserConfigurationException, SAXException, IOException 
    {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        doc = builder.parse("people.xml");

        //creating XPATH object
        getXPathObj();
    }

    public XPath getXPathObj() 
    {
        XPathFactory factory = XPathFactory.newInstance();
        xpath = factory.newXPath();
        return xpath;
    }
    
    // Use XPATH to implement methods like getWeight and getHeight giving the id person
    public Double getLastUpdate(String id) throws XPathExpressionException 
    {
        XPathExpression expr = xpath.compile("/people/person[@id = '" + id + "']/healthprofile/lastupdate");
        Double val = (Double) expr.evaluate(doc, XPathConstants.NUMBER);
        return val;
    }
    
    public Double getWeight(String id) throws XPathExpressionException 
    {
        XPathExpression expr = xpath.compile("/people/person[@id = '" + id + "']/healthprofile/weight");
        Double val = (Double) expr.evaluate(doc, XPathConstants.NUMBER);
        return val;
    }

    public Double getHeight(String id) throws XPathExpressionException 
    {
        XPathExpression expr = xpath.compile("/people/person[@id = '" + id + "']/healthprofile/height");
        Double val = (Double) expr.evaluate(doc, XPathConstants.NUMBER);
        return val;
    }

    public Double getBmi(String id) throws XPathExpressionException 
    {
        XPathExpression expr = xpath.compile("/people/person[@id = '" + id + "']/healthprofile/bmi");
        Double val = (Double) expr.evaluate(doc, XPathConstants.NUMBER);
        return val;
    }
    
    // Read the XML file and return a list with all the information
    public List<String> readallinfo()  throws XPathExpressionException
    {
    	List<String> peopleInformation = new ArrayList<String>();
    	
        XPathExpression expr = xpath.compile("//person/@id | //person/*/text() | //person/*/*/text()");
        NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        
        // insert into a list the information
        for (int i=0; i<nodes.getLength(); i++)
		{
        	// check if node text is empty
        	if(nodes.item(i).getTextContent().replace("\n", "").replace("\t", "").isEmpty())
        		continue;
        	
        	peopleInformation.add(nodes.item(i).getTextContent());
        }  
        
        return peopleInformation;     	
    }
    // Make a function that prints all people in the list with detail (if >20, paginated)
    public void getAllInfo() throws XPathExpressionException
    {    	
    	List<String> peopleInformation = this.readallinfo();    	
    	
    	// node names are fixed, from the XML file structure
    	String[] nodeNames = {"id", "FIRSTNAME", "LASTNAME", "BIRTHDATE", "LAST UPDATE", "WEIGHT", "HEIGHT", "BMI"};
    	int index = 0; 

        // count the number of people already written
    	int peopleCount = 0;
    	boolean page = false;
    	
        // print the information
    	// paginated
        for (int i=0; i<peopleInformation.size(); i++)
        {
			// check if there are no more information about a person
        	if(index%8 == 0) 
        	{
				// carriage return before each person for better formatting of the output
        		System.out.println("\n");
				// node names index come back to the start
        		index=0;
				// increment number of written people 
        		peopleCount++;
        		// not yet paginated
        		page = false;
        	}
        	
        	// pagination (20 people per page)
        	if(peopleCount%20 == 0 && !page) 
        	{
        		System.out.println("Press enter to continue...");
        		// wait for the user to input enter
        		new Scanner(System.in).nextLine();
        		// flag to indicate we already paginated
        		page = true;
        	}
        	
        	System.out.println(nodeNames[index++] + ": " + peopleInformation.get(i));
        }
    }
    
    // A function that accepts id as parameter and prints the HealthProfile of the person with that id
    public void printHealthProfile(String id) throws XPathExpressionException 
    {    	
        XPathExpression expr = xpath.compile( "/people/person[@id='" + id +"']/healthprofile/*");		// actual XPATH expression to get the HealthProfile given an id	

        NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        
        // check if the XPATH expression returned empty results
        // which means that the given id is not present
        if(nodes.getLength() == 0)
        	System.out.println("id not found");

        // print the Health Profile
    	System.out.println("Health Profile of " + nodes.item(0).getTextContent().toUpperCase() + " " + nodes.item(1).getTextContent().toUpperCase());
		// i starts from 2 because the first 2 values (name, surname) was already written above
        for(int i=2;i<nodes.getLength();i++) 
        {
        	System.out.println("   " + nodes.item(i).getNodeName() + ": " + nodes.item(i).getTextContent());
        }
    }
    
    //  A function which accepts a weight and an operator (=, >, <) as parameters and prints people that fulfill that condition (i.e., >80Kg, =75Kg, etc.).
    public NodeList getPeopleByWeight(char condition, Double weight) throws XPathExpressionException 
    {    	
    	XPathExpression expr = xpath.compile("/people/person[healthprofile[weight " + condition + " " + weight + "]]/*");
        NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        return nodes;
    }

	public static void main(String[] args) throws ParserConfigurationException, SAXException,
    IOException, XPathExpressionException
	{	
		HealthProfileReader test = new HealthProfileReader();
        test.loadXML();
        
		// get the number of command line argument
        int argCount = args.length;
		if (argCount == 0) 
		{
			System.out.println("Insert at least the name of the method!");
			return;
		} 
		
		// argument id
		String id;
		// value returned by the methods
		Double val;
		
		// get the name of the method, given as 1st argument
		String method = args[0];
		switch(method) 
		{
			// Use xpath to implement methods like getWeight and getHeight
			case "getWeight": 
				if (argCount != 2) 
				{
					System.out.println("Usage: getWeight [id]");
					break;
				} 
				id = args[1];
				val = test.getWeight(id);
				if(val.isNaN())
				{
			        System.out.println("id not found");
			        break;
				}
				System.out.println("Weight = " + val);
				break;
			case "getHeight": 
				if (argCount != 2) 
				{
					System.out.println("Usage: getHeight [id]");
					break;
				} 
				id = args[1];
				val = test.getHeight(id);
				if(val.isNaN())
				{
			        System.out.println("id not found");
			        break;
				}
				System.out.println("Height = " + val);
				break;
			case "getBmi": 
				if (argCount != 2) 
				{
					System.out.println("Usage: getBmi [id]");
					break;
				} 
				id = args[1];
				val = test.getBmi(id);
				if(val.isNaN())
				{
			        System.out.println("id not found");
			        break;
				}
				System.out.println("BMI = " + val);
				break;

			// Make a function that prints all people in the list with detail (if >20, paginated)
			case "getAllPeople":
				if (argCount != 1) 
				{
					System.out.println("Usage: getAllPeople");
					break;
				}  
				
				test.getAllInfo();
				break;
			
			// A function that accepts id as parameter and prints the HealthProfile of the person with that id
			case "getHealthProfile": 
				if (argCount != 2) 
				{
					System.out.println("Usage: getHealthProfile [id]");
					break;
				} 
				id = args[1];
				test.printHealthProfile(id);
				break;
			
			// A function which accepts a weight and an operator (=, > , <) as parameters and prints people that fulfill that condition (i.e., >80Kg, =75Kg, etc.) 
			case "getPeopleByWeight": 
				if (argCount != 3) 
				{
					System.out.println("Usage: getPeopleByWeight [operator] [weight]");
					break;
				}
				// check if the given weight can be interpreted as a number
		    	try 
		    	{ 
		    		val = Double.parseDouble(args[2]); 
		        } 
		    	catch(NumberFormatException e) 
		    	{  	
		        	System.out.println("Weight not valid");
		        	break;
		        }
		    	
		    	char op = args[1].charAt(0);
		    	// check if given condition is correct
		    	if(op!='=' && op!='<' && op!='>')
		    	{
		    		System.out.println("Condition not valid");
		    		break;
		    	}
		    	
		    	NodeList nodes = test.getPeopleByWeight(op, val);
		    	
		    	if(nodes == null) 
		        {
		        	System.out.println("Error retrieving information");	
					break;     	
		        }
		        if(nodes.getLength() == 0) 	
		        {
		        	System.out.println("No entry found");
					break;
		        }       
		        
		        // print results
		        for(int i=0;i<nodes.getLength();i++) 
		        {
		        	if(nodes.item(i).getNodeName().equals("healthprofile"))
		        		System.out.print("HealthProfile [Last update, Weight, Height, BMI]");
		        	else
		        		System.out.print(nodes.item(i).getNodeName() + ": ");
		        	System.out.println(nodes.item(i).getTextContent());
		        }
				break;
				
			default: 
				System.out.println("The system did not find the method '"+method+"'");
				break;
		}
    }
}