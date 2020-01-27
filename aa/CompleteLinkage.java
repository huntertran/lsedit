package aa;

public class CompleteLinkage extends Algorithm
{
	public double computeSimilarity(int k, int a, int b)
	{
		return min(findValueInArray(k, a), findValueInArray(k, b));
	}

	private double min(double i, double j)
	{
		if (i < j)
			return i;
		else
			return j;
	}
	
	//	public double computeSimilarity(int k, int a, int b)
	//	{
	//		double result;
	//		Node n1 = (Node) c1.getUserObject();
	//		Node n2 = (Node) c2.getUserObject();
	//		
	//		/***************************   
	//		  CASE1: leaf & leaf  
	//		***************************/
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
	//			result = max(computeSimilarity((DefaultMutableTreeNode) c1.getFirstChild(), c2, s),
	//					computeSimilarity((DefaultMutableTreeNode) c1.getLastChild(), c2, s));
	//			IO.put("Cluster & Leaf similarity: " + result, 2);
	//			return result;
	//		}
	//
	//		/*******************************************************   
	//		  CASE3: leaf & cluster   OR   cluster & cluster 
	//		********************************************************/
	//		else
	//		{
	//			result = max(computeSimilarity(c1, (DefaultMutableTreeNode) c2.getFirstChild(), s),
	//					computeSimilarity(c1, (DefaultMutableTreeNode) c2.getLastChild(), s));
	//			IO.put("Leaf/Cluster & Cluster similarity: " + result, 2);
	//			return result;
	//		}
	//
	//	} // end method computeSimilarity

}
