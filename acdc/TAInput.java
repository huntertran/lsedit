package acdc;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Hashtable;

import javax.swing.tree.DefaultMutableTreeNode;

/**
* This class creates a tree from the info passed 
* by the input file.
*/
public class TAInput extends TA_StreamTokenizer implements InputHandler
{
	Hashtable 	m_nodes;

	private void error(String message)
	{
		System.err.println("Syntax error in input file at line " + lineno());
		if (message != null) {
			System.err.println(message);
		}
		System.err.println("Aborting...");
		System.exit(1);	
	}

	private	Node createNode(String name, String type, DefaultMutableTreeNode parent)
	{
		Node					n;
		DefaultMutableTreeNode	tn;

		n  = new Node(name, type);
		tn = new DefaultMutableTreeNode(n);
		n.setTreeNode(tn);
		m_nodes.put(name, n);
		parent.add(tn);
		return n;
	}

	/* Discard everything seen before "FACT TUPLE :" */

	private void processSchemeInfo() throws IOException 
	{
		int		state = 0;

		for (;;) {
			switch (nextToken()) {
			case TT_EOF:
				error("'FACT TUPLE :' not found in input file");
			case TT_WORD: 
				if (m_sval.equals("FACT")) {
					state = 1;
					break;
				}
				if (state == 1 && m_sval.equals("TUPLE")) {
					state = 2;
					break;
				}
				state = 0;
				break;
			case ':':
				if (state == 2) {
					return;
				}
			default:
				state = 0;
	}	}	}


	private void processFactTriples(DefaultMutableTreeNode root) throws IOException 
	{
		String	firstTok, secondTok, thirdTok;
		int		ttype;
		Node	n1, n2;

		for (;;) {
			ttype = nextToken();
			switch (ttype) {
			case TT_EOF:
				// End of file 
				return;
			case TT_WORD: 
				firstTok = m_sval;

				ttype = nextToken();
				if (ttype == TT_WORD) {
					secondTok = m_sval;

					ttype = nextToken();

					if (ttype == TT_WORD) {
						thirdTok = m_sval;
						break;
					}

					if (ttype == ':' && firstTok.equals("FACT") && secondTok.equals("ATTRIBUTE")) {
						// Next section
						return;
				}	}
			default:
				error("Reading fact tuples");
				return;
			}

			/******************************************************************************
				  CASE 1: first token in the line is  "$INSTANCE"
			 ******************************************************************************/

			n1 = (Node) m_nodes.get(secondTok);

			if (firstTok.equals("$INSTANCE"))
			{ 
				//secondTok <-- name of a node in the tree
				//thirdTok  <-- type of a node in the tree

				// If secondTok has been instantiated before, abort with a message
				if (n1 != null) 
				{
					error("Two instances of " + secondTok);
				}
				IO.put("TAInput.java:\t$INSTANCE Will create node " + secondTok,2);
				n1  = createNode(secondTok, thirdTok, root);
				continue;
			}
			
			/******************************************************************************
			 CASE 2: first token in the line is  "contain"
			 *****************************************************************************/


			
			//secondTok <-- name of a node (maybe) in the tree
			//thirdTok  <-- name of a node (maybe) in the tree

			if (firstTok.equals("contain"))
			{
				DefaultMutableTreeNode tn1, tn2;

				//if secondTok corresponding node was not found as part of the tree
				//create a node and add it under root

				if (n1 == null) 
				{
					IO.put("Container of unknown type: " + secondTok + ". Assumed to be a cluster", 2);
					n1  = createNode(secondTok, "UnknownContainer", root);
				}
				tn1 = n1.getTreeNode();
				n2  = (Node) m_nodes.get(thirdTok);

				//if thirdTok corresponding node was not found as part of the tree
				//create a node
				if (n2 == null) 
				{
					//IO.put("Node of unknown type: "+ thirdTok);
					n2 = createNode(thirdTok, "unknown", tn1);
				} else {
					tn2 = n2.getTreeNode();
					//add thirdTok node under secondTok node
					tn1.add(tn2);
				}
				IO.put("TAInput.java:\tcontain " + secondTok + "\t" +thirdTok,2);
				continue;
			}

			/******************************************************************************
			 CASE 3: first token in the line is other than "$INSTANCE" or "contain"
			 *****************************************************************************/

			// Edge from to

			/******************************************************************************
			 CASE 3.1:         secondTok doesn't have a corresponding node in the tree 
			 *****************************************************************************/
		
			if (n1 == null)
			{
				//create new node and add it under root
		
				//IO.put("Node of unknown type: "+ secondTok);
				n1 = createNode(secondTok, "Unknown", root);
			}

			n2 = (Node) m_nodes.get(thirdTok);
		
			/******************************************************************************
			 CASE 3.2:         thirdTok doesn't have a corresponding node in the tree 
			 *****************************************************************************/
	
			if (n2 == null)
			{
				//create new node and add it under root
				//IO.put("Node of unknown type: "+ thirdTok);
				//IO.put("firstToken := " + firstTok +" , secondToken := " + secondTok + " , thirdToken := " +thirdTok);
				n2 = createNode(thirdTok, "Unknown", root);
			}
		
			/***********************************************************************************
		     CASE 3.3         secondTok and thirdTok have corresponding nodes in the tree 
			 ***********************************************************************************/

			//now create an edge from secondTok node to thirdTok node
		
			//NOTE: might be creating an edge from a node onto itself!!
		
			//edge originates in secondTok node and is directed towards thirdTok node
			Edge e = new Edge(n1,n2,firstTok);
			n1.addOutEdge(e);
			n2.addInEdge(e);
			IO.put("TAInput.java:\tEdge created from " + secondTok + " to " + thirdTok,2);
		}
	}

	// Throw away any attribute list

	private void parseList() throws IOException 
	{
		// Called when value field started with '('

		for (;;) {
			switch(nextToken())	{
			case ')':
				return;
			case '(':
			{
				// Nested list
				parseList();
				break;
			}
			case TT_WORD:
			{
				break; 
			}
			default:
				error("Reading fact attribute list");
	}	}	}

	// Change the name of things to their label if they have one

	private void processAttributes(Node n) throws IOException 
	{
		int		ttype;
		String	attribute, value;

		for (;;) {
			ttype = nextToken();

			switch (ttype) {
			case '}':
				return;
			case TT_WORD:
				break;
			default:
				error("Expecting attribute id for " + n.getName());
				return;
			}
			attribute = m_sval;

			if (nextToken() != '=') {
				// Attribute declaration with no value 
				pushBack();
				continue; 
			} 

			ttype = nextToken();
			switch(ttype) {
			case '(':
				parseList();
				continue;
			case TT_WORD:
				value = m_sval;
				break;
			default:
				error("Expecting value for " + n.getName() + " attribute " + attribute);
				return;
			}

			if (!attribute.equals("label")) {
				continue;
			}

			if (value.length() > 0) {
				//change name of node here
	      		IO.put(n.getName() + " assigned name "+ value, 2);
				n.setName(value);
	}	}	}

	private void processFactAttributes(DefaultMutableTreeNode root) throws IOException 
	{
		int					ttype;
		String				name;
		Node				n;

		// 
		//  * id "{" {attribute} "}" *
		// 


		for (;;) {

			ttype = nextToken();
			switch (ttype) {
			case TT_EOF:
				// End of section
				return; 
			case '(':

				for (;;) {
					switch (nextToken()) {
					case TT_EOF:
						return;
					case '}':
						break;
					default:
						continue;
					}
					break;
				}
				break;
			case TT_WORD:
				// Entity class or entity 
				name   = m_sval;

				n      = (Node) m_nodes.get(name);

				if (n == null) {
					n = createNode(name, "unknown", root);
				}

				ttype  = nextToken();
				if (ttype == '{') {
					processAttributes(n);
					break;
				}
			default:
				error("Reading fact attributes");
	}	}	}

	public void readInput(String inputStr, DefaultMutableTreeNode treeModel)
	{
		IO.put("Reading input...",1);

	 	m_nodes = new Hashtable(10000);

		try 
		{
			BufferedReader			in   = new BufferedReader(new FileReader(inputStr));
	    	DefaultMutableTreeNode	root = (DefaultMutableTreeNode) treeModel.getRoot();

			
			setInputStream(in);

			processSchemeInfo();
			processFactTriples(root);
			processFactAttributes(root);

			IO.put("Finished reading the input file.\n",2);
	      
			in.close();
	    } 
	    catch(FileNotFoundException e) 
	    {
	      System.err.println(e.getMessage());
	    }
	    catch(IOException e) 
	    {
	      System.err.println(e.getMessage());
	    }

		m_nodes = null;
	}
}
