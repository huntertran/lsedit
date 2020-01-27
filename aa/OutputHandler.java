package aa;
import java.io.*;
import java.util.*;
import javax.swing.tree.*;

public class OutputHandler
{
	public void writeOutput(String outputName, Vector vNodes)
	{
		PrintWriter out = null;
		try
		{
			out = new PrintWriter(new BufferedWriter(new FileWriter(outputName)));
		}
		catch (IOException e)
		{
			System.err.println(e.getMessage());
		}

		IO.put("Creating output with " + vNodes.size() + " clusters.",1);
		Iterator ivn = vNodes.iterator();
		while (ivn.hasNext())
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) ivn.next();
			Node nchild = (Node) node.getUserObject();

			if (!node.isLeaf())
			{
				// Traverse subtree, output for leaf nodes
				Enumeration subtree = node.breadthFirstEnumeration();
				while (subtree.hasMoreElements())
				{
					DefaultMutableTreeNode subnode = (DefaultMutableTreeNode) subtree.nextElement();
					if (subnode.isLeaf())
					{
						Node nsubnode = (Node) subnode.getUserObject();
						out.println("contain " + nchild.getName() + " " + nsubnode.getName());
					}
				}
			}
			else
				out.println("contain " + nchild.getName() + " " + nchild.getName());
		}
		out.close();
	}
}
