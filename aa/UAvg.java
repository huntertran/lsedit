package aa;
import javax.swing.tree.DefaultMutableTreeNode;

public class UAvg extends Algorithm
{
	public double computeSimilarity(int k, int a, int b)
	{
		return uavg(findValueInArray(k, a), findValueInArray(k, b), a, b);
	}

	public double uavg(double sim1, double sim2, int a, int b)
	{
		double result;
		DefaultMutableTreeNode t1, t2;
		t1 = (DefaultMutableTreeNode) vRootChildren.elementAt(a);
		t2 = (DefaultMutableTreeNode) vRootChildren.elementAt(b);
		int c1 = t1.getLeafCount();
		int c2 = t2.getLeafCount();
		result = (c1 * sim1 + c2 * sim2) / (c1 + c2);
		return result;
	}
	
//		public double computeSimilarity(DefaultMutableTreeNode c1, DefaultMutableTreeNode c2, SimilarityMetric s)
//	{
//		double result;
//		Vector v, v1, v2; //contain leaves 
//		Enumeration e, e1, e2; //iterates v,v1,v2 respectively
//		DefaultMutableTreeNode curr;
//		int leaf_countL = 0; //counts the number of leaves on left subtree
//		int leaf_countR = 0; //counts the number of leaves on right subtree
//		Node n1 = (Node) c1.getUserObject();
//		Node n2 = (Node) c2.getUserObject();
//
//		/***************************   
//		   CASE1: leaf & leaf  
//		 ***************************/
//		if (c1.isLeaf() && c2.isLeaf())
//		{
//			result = s.compute(n1, n2);
//			IO.put("Leaf & Leaf similarity: " + result, 2);
//			return result;
//		}
//
//		/***************************   
//		  CASE2: cluster & leaf 
//		***************************/
//		else if (!c1.isLeaf() && c2.isLeaf())
//		{
//			//calculate size of left subtree of c1
//			e1 = ((DefaultMutableTreeNode) c1.getFirstChild()).depthFirstEnumeration();
//			v1 = new Vector();
//
//			while (e1.hasMoreElements())
//			{
//				curr = (DefaultMutableTreeNode) e1.nextElement();
//				if (curr.isLeaf())
//				{
//					v1.add(curr);
//				}
//			}
//			//don't multiply by zero! 
//			if (v1.size() != 0)
//			{
//				leaf_countL = v1.size();
//			}
//			else
//				leaf_countL = 1;
//
//			//calculate size of right subtree of c1
//			e2 = ((DefaultMutableTreeNode) c1.getLastChild()).depthFirstEnumeration();
//			v2 = new Vector();
//			while (e2.hasMoreElements())
//			{
//				curr = (DefaultMutableTreeNode) e2.nextElement();
//				if (curr.isLeaf())
//					v2.add(curr);
//			}
//
//			//don't multiply by zero! 
//			if (v2.size() != 0)
//			{
//				leaf_countR = v2.size();
//			}
//			else
//				leaf_countR = 1;
//
//			IO.put("Left: " + leaf_countL + " Right: " + leaf_countR, 2);	
//			result =
//				((leaf_countL * computeSimilarity((DefaultMutableTreeNode) c1.getFirstChild(), c2, s))
//					+ (leaf_countR * computeSimilarity((DefaultMutableTreeNode) c1.getLastChild(), c2, s)))
//					/ (leaf_countL + leaf_countR);
//
//			IO.put("Cluster & Leaf similarity: " + result, 2);
//			return result;
//		}
//
//		/*******************************************************   
//		     CASE3: leaf & cluster   OR   cluster & cluster 
//		********************************************************/
//		else
//		{
//			//calculate size of left subtree of c2
//			e1 = ((DefaultMutableTreeNode) c2.getFirstChild()).depthFirstEnumeration();
//			v1 = new Vector();
//			while (e1.hasMoreElements())
//			{
//				curr = (DefaultMutableTreeNode) e1.nextElement();
//				if (curr.isLeaf())
//					v1.add(curr);
//			}
//
//			//don't multiply by zero! 
//			if (v1.size() != 0)
//			{
//				leaf_countL = v1.size();
//			}
//			else
//				leaf_countL = 1;
//
//			//calculate size of right subtree of c1
//			e2 = ((DefaultMutableTreeNode) c2.getLastChild()).depthFirstEnumeration();
//			v2 = new Vector();
//			while (e2.hasMoreElements())
//			{
//				curr = (DefaultMutableTreeNode) e2.nextElement();
//				if (curr.isLeaf())
//					v2.add(curr);
//			}
//
//			//don't multiply by zero! 
//			if (v2.size() != 0)
//			{
//				leaf_countR = v2.size();
//			}
//			else
//				leaf_countR = 1;
//
//			IO.put("Left: " + leaf_countL + " Right : " + leaf_countR, 2);
//			result =
//				((leaf_countL * computeSimilarity(c1, (DefaultMutableTreeNode) c2.getFirstChild(), s))
//					+ (leaf_countR * computeSimilarity(c1, (DefaultMutableTreeNode) c2.getLastChild(), s)))
//					/ (leaf_countL + leaf_countR);
//
//			IO.put("Leaf/Cluster & Cluster similarity: " + result, 2);
//			return result;
//		}
//	} // end method execute
}
