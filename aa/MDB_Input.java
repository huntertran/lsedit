package aa;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.StringTokenizer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
* This class creates a tree with the info passed by the input file.
*
* FILE FORMAT:
*
* First token in each line of the input file represents an object. 
* Following tokens represent features of that object.
*/
public class MDB_Input extends InputHandler
{
	public DefaultTreeModel readInput(String filename)
	{
		String str = "";
		allFeatures = new HashSet();
		Node nRoot = new Node("ROOT");
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(nRoot);
		nRoot.setTreeNode(root);
		DefaultTreeModel treeModel = new DefaultTreeModel(root);

		try
		{
			BufferedReader in = new BufferedReader(new FileReader(filename));

			str = in.readLine();

			String firstToken, nextToken;

			//traverse each line in the input file 
			while (str != null)
			{
				str = str.trim();
				StringTokenizer strTok = new StringTokenizer(str);
				firstToken = strTok.nextToken();
				//create a node named by first token on this line 
				Node nRootChild = new Node(firstToken);
				DefaultMutableTreeNode rootChild = new DefaultMutableTreeNode(nRootChild);
				nRootChild.setTreeNode(rootChild);
				//add current node (object) under root
				root.add(rootChild);
				//add following tokens(features of current object) on this line to current node
				while (strTok.hasMoreElements())
				{
					nextToken = strTok.nextToken();
					nRootChild.addFeature(nextToken);
					allFeatures.add(nextToken);
				}

				str = in.readLine();
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

		return treeModel;

	} // end readInput method

} //end class 
