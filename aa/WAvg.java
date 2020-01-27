package aa;

public class WAvg extends Algorithm
{
	public double computeSimilarity(int k, int a, int b)
	{
		return (findValueInArray(k, a) + findValueInArray(k, b)) / 2.0;
	}

//	public double computeSimilarity(DefaultMutableTreeNode c1, DefaultMutableTreeNode c2, 
//						SimilarityMetric s)
//	{
//	    double result;
//		Node n1 = (Node) c1.getUserObject();
//		Node n2 = (Node) c2.getUserObject();
//	    
//	    /*******************************************
//	    	Case1: leaf, leaf
//	    *******************************************/
//	    if(c1.isLeaf() && c2.isLeaf())
//	    {
//	    	 result = s.compute(n1,n2);
//			IO.put("Leaf & Leaf *** simetry =: " + result,1);
//		 return result ;
//	    }
//	    
//	    /*******************************************
//	    	Case2: cluster, leaf
//	    *******************************************/
//	    else if(!c1.isLeaf() && c2.isLeaf())
//	    {
//				 
//		result = (computeSimilarity((DefaultMutableTreeNode)c1.getFirstChild(),c2,s)
//					+ 
//			  computeSimilarity((DefaultMutableTreeNode)c1.getLastChild(),c2,s))
//					/2;
//		IO.put("Cluster & Leaf *** simetry:= " + result,1);
//		
//		return result;		
//	    	
//	    }
//	    
//	    /*********************************************
//	    	Case3: leaf, cluster OR cluster, cluster
//	    **********************************************/
//	    else 
//	    {
//	    	result = (computeSimilarity(c1,(DefaultMutableTreeNode)c2.getFirstChild(),s) 
//					+ 
//			  computeSimilarity(c1,(DefaultMutableTreeNode)c2.getLastChild(),s))
//					/2;
//		IO.put("Leaf & Cluster *** Cluster & Cluster *** simetry := " + result,1);
//		
//		return result;
//	    }
//	   
//	}// end method execute
	
}
