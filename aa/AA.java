package aa;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

//import javax.swing.JFrame;
//import javax.swing.JScrollPane;
//import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * An implementation of four different hierarchical algorithms and three
 * different similarity metrics.
 *
 * This application provides a convenient way to cluster a set of objects 
 * having attributes, on the basis of a variety of similarity metrics and 
 * algorithms and to prune the obtained dendrogram.
 */
public class AA
{

	private static DefaultTreeModel treeModel;
	static int SIM_COUNT = 3;
	static int ALG_COUNT = 4;

	/**
	 * Exits the program with an error message, reminding the user how to
	 * run this application. 
	 * @param sCount - number of similarities implemented so far
	 * @param aCount - number of algorithms implemented so far
	 */
	static void err_AND_exit()
	{
		IO.put("Usage: java aa.AA <input file> <output file> -c<0.0 to 1.0> [options]", 0);
		IO.put("  OR", 0);
		IO.put("Usage: java aa.AA <input file> <output file> -C<fileName> [options]", 0);
		IO.put("The file in the -C option must contain only numbers (one per line)", 0);
		IO.put("If the number is real, it will be used as the cut-point", 0);
		IO.put("If the number is integer, the cut will produce that many clusters.", 0);
		IO.put("A different output file will be produced for each number.", 0);
		IO.put("", 0);
		IO.put("Possible options include: ", 0);
		IO.put(" -d1 or -d2     Generates debugging info (d1 is minimal, d2 is verbose)", 0);
		IO.put(" -h             Prints this message and exits", 0);
		IO.put(" -s0 <default>  Jaccard Similarity Metric", 0);
		IO.put(" -s1            Simple Matching Similarity Metric", 0);
		IO.put(" -s2            Sorensen-Dice Similarity Metric", 0);
		IO.put(" -a0 <default>  Single Linkage Algorithm", 0);
		IO.put(" -a1 	        Complete Linkage Algorithm", 0);
		IO.put(" -a2            Weighted Average Algorithm", 0);
		IO.put(" -a3            Unweighted Average Algorithm", 0);
		System.exit(0);
	}

	/**
	 * Returns true if the string s1 is made up only of characters found
	 * in the string s2; else returns false.
	 */
	static boolean matches(String s1, String s2)
	{
		boolean containsNoOther = true;
		Vector v = new Vector(s2.length());

		for (int i = 0; i < s2.length(); i++)
			v.add(Character.toString(s2.charAt(i)));

		for (int j = 0; j < s1.length(); j++)
		{
			if (v.contains(Character.toString(s1.charAt(j))));

			else
				containsNoOther = false;

		}
		return containsNoOther;
	}

	/**
	 * Attempts to extract the double value representing the CUT value from the input.
	 * e.g. java AA t.in t.out -c0.4
	 * If this value is not between 0 and 1 an error message is displayed and the application
	 * exits with an error message reminding the user how to run the program.
	 *
	 * @param s - the string to be parsed
	 * @return the double value represented by the string argument, if it's between 0 and 1
	 *
	 */
	static Double verifyDouble(String s)
	{

		Double c = null;
		try
		{
			c = Double.valueOf(s);
		}
		catch (NumberFormatException n)
		{
			IO.put(s + " : Not a valid double", 0);
			err_AND_exit();
		}
		if (c.doubleValue() < 0 || c.doubleValue() > 1)
		{
			IO.put(c.toString() + " : The cut-point must be between 0.0 and 1.0", 0);
			err_AND_exit();
		}
		return c;
	}

	/**
	* Attempts to extract the int value representing the SIMILARITY measure from the input.
	* e.g. java AA t.in t.out -s2 -c0.4
	* If this value is not between 0 and SIM_COUNT an error message is displayed and the application
	* exits with an error message reminding the user how to run the program.
	*
	* @param s - the string to be parsed
	* @return the int value represented by the string argument, if it's between 0 and SIM_COUNT-1
	*
	*/
	static int extractSimilarity(String s)
	{
		int sim = 0;

		try
		{
			sim = Integer.parseInt(s);

		}
		catch (NumberFormatException n)
		{
			IO.put(s + " : integer was expected after argument <-s>", 0);
			err_AND_exit();
		}

		if (sim < 0 || sim >= SIM_COUNT)
		{
			IO.put(sim + " : invalid selection of similarity coefficient.", 0);
			err_AND_exit();
		}
		return sim;
	}

	/**
	* Attempts to extract the int value representing the ALGORITHM measure from the input.
	* e.g. java AA t.in t.out -a2 -c0.4
	* If this value is not between 0 and ALG_COUNT-1 an error message is displayed and the application
	* exits with an error message reminding the user how to run the program.
	*
	* @param s - the string to be parsed
	* @return the int value represented by the string argument, if it's between 0 and ALG_COUNT-1
	*
	*/
	static int extractAlgorithm(String s)
	{
		int a = 0;
		try
		{
			a = Integer.parseInt(s);
		}
		catch (NumberFormatException n)
		{
			IO.put(s + " : integer was expected after argument <-a>", 0);
			err_AND_exit();
		}
		if (a < 0 || a >= ALG_COUNT)
		{
			IO.put(a + " : invalid selection of algorithm.", 0);
			err_AND_exit();
		}
		return a;
	}

	/**
	 * Given ROOT, traverse its subtree and add those nodes whose value 
	 * is greater than CUT in a Vector to be returned.
	 */
	static Vector cutTreeDouble(double cut, DefaultMutableTreeNode root)
	{
		Vector oldVector = new Vector();
		Vector newVector = new Vector();
		Enumeration er = root.children();
		while (er.hasMoreElements())
			newVector.add((DefaultMutableTreeNode) er.nextElement());
		while (newVector.size() > oldVector.size())
		{
			oldVector = (Vector) newVector.clone();
			newVector = new Vector();
			Iterator ivr = oldVector.iterator();
			while (ivr.hasNext())
			{
				DefaultMutableTreeNode curr = (DefaultMutableTreeNode) ivr.next();
				Node ncurr = (Node) curr.getUserObject();
				double value = (double) ncurr.getValue();
				if (value <= cut)
				{
					Enumeration ec = curr.children();
					while (ec.hasMoreElements())
						newVector.add((DefaultMutableTreeNode) ec.nextElement());
				}
				else
				{
					newVector.add(curr);
				}
			}
		}
		return oldVector;
	}

	/**
	 * Given ROOT, traverse its subtree and create a Vector that will contain NO nodes
	 * (or higher in case of ties).
	 */
	static Vector cutTreeInt(int no, DefaultMutableTreeNode root)
	{
		Enumeration er = root.children();
		Vector result = new Vector();
		while (er.hasMoreElements())
			result.add((DefaultMutableTreeNode) er.nextElement());
		while (result.size() < no)
		{
			IO.put("At " + result.size() + " clusters.", 2);
			Iterator ivr = result.iterator();
			double min = 1.0;
			while (ivr.hasNext())
			{
				DefaultMutableTreeNode curr = (DefaultMutableTreeNode) ivr.next();
				Node ncurr = (Node) curr.getUserObject();
				double value = (double) ncurr.getValue();
				IO.put("value: " + value, 2);
				if (value < min)
				{
					min = value;
					IO.put("min value: " + min, 2);
				}
			}
			if (min == 1.0)
				break;
			Vector newVector = new Vector();
			ivr = result.iterator();
			while (ivr.hasNext())
			{
				DefaultMutableTreeNode curr = (DefaultMutableTreeNode) ivr.next();
				Node ncurr = (Node) curr.getUserObject();
				double value = (double) ncurr.getValue();
				if (value == min)
				{
					Enumeration ec = curr.children();
					while (ec.hasMoreElements())
						newVector.add((DefaultMutableTreeNode) ec.nextElement());
				}
				else
				{
					newVector.add(curr);
				}
			}
			result = (Vector) newVector.clone();
		}
		return result;
	}

	/************** MAIN METHOD ***********************/
	public static void main(String[] args)
	{

		IO.debug_level = 0;
		//if one of the given args is "-h", print Help message, then exit
		for (int u = 0; u < args.length; u++)
		{
			if (args[u].equalsIgnoreCase("-h"))
				err_AND_exit();
		}

		String inputName, outputName;
		Vector v_integer = new Vector();
		Vector v_double = new Vector();

		//defaults
		String debug = "";
		int similarity = 0;
		int algorithm = 0;
		String gui = "";

		// parse input arguments
		if (args.length < 3)
		{
			IO.put("Too few arguments.", 0);
			err_AND_exit();
		}

		if (args.length > 8)
		{
			IO.put("Too many arguments.", 0);
			err_AND_exit();
		}

		inputName = args[0];

		outputName = args[1];

		if (args[2].startsWith("-c"))
		{
			if (args[2].length() <= 2)
			{
				IO.put(args[2] + " : argument -c should be followed by a double", 0);
				err_AND_exit();
			}
			v_double.add(verifyDouble(args[2].substring(2)));
		}
		else if (args[2].startsWith("-C"))
		{
			String fileName = args[2].substring(2);
			if (fileName.length() == 0)
			{
				IO.put("No filename specified with -C option", 0);
				err_AND_exit();
			}
			try
			{
				BufferedReader in = new BufferedReader(new FileReader(fileName));

				String line = in.readLine();
				while (line != null)
				{
					line = line.trim();
					try
					{
						if (line.indexOf('.') == -1)
							v_integer.add(Integer.valueOf(line));
						else
							v_double.add(Double.valueOf(line));
					}
					catch (NumberFormatException e1)
					{
						IO.put("Wrong format in file: " + fileName + " in the line:", 0);
						IO.put(line, 0);
						IO.put("Aborting...", 0);
						System.exit(0);
					}
					line = in.readLine();
				}
				in.close();
			} // end try
			catch (FileNotFoundException e)
			{
				System.err.println(e.getMessage());
			}
			catch (IOException e)
			{
				System.err.println(e.getMessage());
			}
		}
		else
		{
			IO.put(args[2] + " : -c or -C was expected", 0);
			err_AND_exit();
		}

		int loops = args.length - 3; //read each arg except the required ones (input,output,cut)!

		int i = 3; //index of current argument

		int k;

		while (loops != 0)
		{

			k = i;

			while (k > 0) //check all previous arguments to the current one
			{
				if (matches(args[i].substring(0, 2), args[k - 1].substring(0, 2)))
				{
					IO.put("Two arguments are the same. Aborting ...", 0);
					err_AND_exit();
				}
				else
					k--;
			}

			if (args[i].substring(0, 2).equalsIgnoreCase("-d"))
			{
				debug = args[i];
			}
			else if (args[i].startsWith("-s"))
			{
				if (args[i].length() <= 2)
				{
					IO.put(args[i] + " : <-s> should be followed by an integer", 0);
					err_AND_exit();
					//System.exit(1);
				}
				similarity = extractSimilarity(args[i].substring(2));
			}
			else if (args[i].startsWith("-a"))
			{
				if (args[i].length() <= 2)
				{
					IO.put(args[i] + " : <-a> should be followed by an integer", 0);
					err_AND_exit();
				}
				algorithm = extractAlgorithm(args[i].substring(2));
			}
			else if (args[i].equalsIgnoreCase("-t"))
			{
				gui = args[i];
			}
			else
			{
				IO.put(args[i] + " : invalid flag", 0);
				err_AND_exit();
			}

			i++;
			loops--;

		} //end while

		if (debug.equalsIgnoreCase("-d1"))
			IO.debug_level = 1;
		if (debug.equalsIgnoreCase("-d2"))
			IO.debug_level = 2;

		String similarity_name = "Something has gone wrong!";
		String algorithm_name = "Something has gone wrong!";
		switch (similarity)
		{
			case 0 :
				similarity_name = "Jaccard";
				break;
			case 1 :
				similarity_name = "Simple Matching";
				break;
			case 2 :
				similarity_name = "Sorensen - Dice";
				break;
		}
		switch (algorithm)
		{
			case 0 :
				algorithm_name = "Single Linkage";
				break;
			case 1 :
				algorithm_name = "Complete Linkage";
				break;
			case 2 :
				algorithm_name = "Weighted Average";
				break;
			case 3 :
				algorithm_name = "Unweighted Average";
				break;
		}
		IO.put("Input File: " + inputName, 1);
		IO.put("Output File: " + outputName, 1);
		IO.put("Similarity: " + similarity_name, 1);
		IO.put("Algorithm: " + algorithm_name, 1);
		IO.put("Debug level: " + IO.debug_level + " \n", 1);

		String inputExtension = null, outputExtension = null;
		int dot = args[0].indexOf(".");
		if (dot >= 0)
		{
			inputExtension = args[0].substring(dot + 1);
		}
		else
		{
			IO.put("Filenames need to have an extension indicating their format.", 0);
			IO.put("Aborting...", 0);
			System.exit(0);
		}
		while (inputExtension.indexOf(".") >= 0)
		{
			dot = inputExtension.indexOf(".");
			inputExtension = inputExtension.substring(dot + 1);
		}
		dot = args[1].indexOf(".");
		if (dot >= 0)
		{
			outputExtension = args[1].substring(dot + 1);
		}
		else
		{
			IO.put("Filenames need to have an extension indicating their format.", 0);
			IO.put("Aborting...", 0);
			System.exit(0);
		}
		while (outputExtension.indexOf(".") >= 0)
		{
			dot = outputExtension.indexOf(".");
			outputExtension = outputExtension.substring(dot + 1);
		}
		if ((inputExtension.equals("mbd")) && (outputExtension.equals("rsf")))
		{
			IO.put("Reading Input ...\n", 1);
			//create initial tree from the input-file given
			//tree with one level only: dummy root and all else as children of root 
			InputHandler in = new MDB_Input();
			DefaultTreeModel treeModel = in.readInput(inputName);
			
			Vector tempVector = new Vector();
			Iterator ihs = in.allFeatures.iterator();
			while (ihs.hasNext())
			{
				tempVector.add(ihs.next());
			}
			AssocCoefficient.vAllFeatures = tempVector;
			
			SimilarityMetric simMetric = new Jaccard();
			switch (similarity)
			{
				case 0 :
					simMetric = new Jaccard(); //default similarity metric
					break;
				case 1 :
					simMetric = new SimpleMatching();
					break;
				case 2 :
					simMetric = new SorensenDice();
					break;

					//add new similarity metric here
			}

			Algorithm alg = new WAvg();
			switch (algorithm)
			{
				case 0 :
					alg = new SingleLinkage();
					break;
				case 1 :
					alg = new CompleteLinkage();
					break;
				case 2 :
					alg = new WAvg();
					break;
				case 3 :
					alg = new UAvg();
					break;
					//add new algorithm here
			}

			IO.put("Clustering ...\n", 1);

			// Create the binary tree
			alg.cluster(treeModel, simMetric);

			//			Node nTempRoot = new Node("ROOT");
			//			DefaultMutableTreeNode tempRoot = new DefaultMutableTreeNode(nTempRoot);
			//			nTempRoot.setTreeNode(tempRoot);
			//			DefaultTreeModel tempTreeModel = new DefaultTreeModel(tempRoot);
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
			OutputHandler out = new OutputHandler();
			Vector vNodes;
			Iterator ivi = v_integer.iterator();
			while (ivi.hasNext())
			{
				Integer ii = (Integer) ivi.next();
				IO.put("Cutting for " + ii.intValue() + " clusters.", 1);
				int dott = outputName.lastIndexOf('.');
				String outName = outputName.substring(0, dott) + ii.toString() + outputName.substring(dott);
				vNodes = cutTreeInt(ii.intValue(), root);
				out.writeOutput(outName, vNodes);
			}

			Iterator ivd = v_double.iterator();
			while (ivd.hasNext())
			{
				Double d = (Double) ivd.next();
				IO.put("Cutting for cut-point " + d.doubleValue(), 1);
				int dott = outputName.lastIndexOf('.');
				String outName = outputName.substring(0, dott) + d.toString() + outputName.substring(dott);
				vNodes = cutTreeDouble(d.doubleValue(), root);
				out.writeOutput(outName, vNodes);
			}

			//    //create GUI 
			//    if (gui.equalsIgnoreCase("-t"))
			//    {
			//	    JTree nodeTree = new JTree (final_treeModel);
			//	    nodeTree.setShowsRootHandles (true);
			//	    nodeTree.putClientProperty ("JTree.lineStyle", "Horizontal");
			//	    nodeTree.putClientProperty ("JTree.lineStyle", "Angled");
			//	    JFrame frame = new JFrame ("File Node Partition");
			//	    frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
			//	    frame.getContentPane ().add (new JScrollPane (nodeTree), "Center");
			//	    frame.setSize (400, 600);
			//	    frame.setVisible (true);
			//    }
		}
		else
		{
			IO.put("Only input in .mbd format and output in .rsf format is supported at this point.", 0);
			IO.put("Aborting...", 0);
			System.exit(0);
		}
	}
}
