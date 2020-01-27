package aa;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

abstract class Algorithm
{
	double[][] similarities;
	Vector vRootChildren;

	int best_i, best_j;

	/** 
	 *  Computes similarity between a pair of given nodes, one or both of 
	 *  which can be cluster-nodes.
	 */
	abstract public double computeSimilarity(int k, int a, int b);

	/** 
	 *  Clusters a given one-level tree, based on similarities between the nodes in the tree, 
	 *  excluding the root. After executing this method the tree is binary.
	 */
	public void cluster(DefaultTreeModel treeModel, SimilarityMetric sim)
	{
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();

		// Pair of tree nodes that display max similarity 
		DefaultMutableTreeNode mostSimilarTreeNode1;
		DefaultMutableTreeNode mostSimilarTreeNode2;

		// Put the children of the root in a vector
		vRootChildren = new Vector();
		Enumeration rootChildren = root.children();
		while (rootChildren.hasMoreElements())
		{
			vRootChildren.add((DefaultMutableTreeNode) rootChildren.nextElement());
		}
		int noRootKids = vRootChildren.size();
		
		similarities = new double[noRootKids][noRootKids];

		computeAllPairs(sim, noRootKids, vRootChildren);

		// Cluster bottom-up, until there are only two children under the root
		while (noRootKids > 2)
		{
			IO.put("noRootKids = " + noRootKids,2);

			findBestIJ(noRootKids, vRootChildren);
			
			// Get the two tree nodes
			mostSimilarTreeNode1 = (DefaultMutableTreeNode) vRootChildren.elementAt(best_i);
			mostSimilarTreeNode2 = (DefaultMutableTreeNode) vRootChildren.elementAt(best_j);

//			Node mostSimilarNode1 = (Node) mostSimilarTreeNode1.getUserObject();
//			Node mostSimilarNode2 = (Node) mostSimilarTreeNode2.getUserObject();

//			IO.put("Best pair : (" + mostSimilarNode1.getName() + ", "
//					               + mostSimilarNode2.getName() + ") Similarity = "
//					               + distances[best_i][best_j], 2);

			// Create new cluster node and put the pair under it 

			Node newCluster = new Node("ss" + noRootKids);

			newCluster.setValue(similarities[best_i][best_j]);

			DefaultMutableTreeNode newClusterTN = new DefaultMutableTreeNode(newCluster);

			newCluster.setTreeNode(newClusterTN);

			// Make new cluster a child of the root
			root.add(newClusterTN);
			newClusterTN.add(mostSimilarTreeNode1);
			newClusterTN.add(mostSimilarTreeNode2);

			// Compute the similarities to the new cluster
			double [] newSimilarities = new double[noRootKids-2];
			int newIndex = 0;
			for (int k = 0; k < noRootKids; k++)
			{
				if ((k != best_i) && (k != best_j))
				{
					newSimilarities[newIndex] = computeSimilarity(k, best_i, best_j);				
					newIndex++;
				}
			}

			// Update vRootChildren. Important to remove best_j first
			vRootChildren.removeElementAt(best_j);
			vRootChildren.removeElementAt(best_i);			
			vRootChildren.add(newClusterTN);
			
			shiftDistanceArray(best_i, best_j, noRootKids);

			// Enter the new similarities in the array
			for (int i = 0; i < noRootKids - 2; i++)
			{
				similarities[i][noRootKids - 2] = newSimilarities[i];
			}

			noRootKids = vRootChildren.size();
			
		} //end while
	}

	private String newName(Node mostSimilarNode1, Node mostSimilarNode2)
	{
		int lastIndexOf_inMostSimilarTreeNode1 = mostSimilarNode1.getName().lastIndexOf("@");
		int lastIndexOf_inMostSimilarTreeNode2 = mostSimilarNode2.getName().lastIndexOf("@");
		
		String firstHalfName, secondHalfName;
		if (lastIndexOf_inMostSimilarTreeNode1 == -1)
			firstHalfName = mostSimilarNode1.getName();
		else
			firstHalfName = mostSimilarNode1.getName().substring(0, lastIndexOf_inMostSimilarTreeNode1);
		
		if (lastIndexOf_inMostSimilarTreeNode2 == -1)
			secondHalfName = mostSimilarNode2.getName();
		else
			secondHalfName = mostSimilarNode2.getName().substring(lastIndexOf_inMostSimilarTreeNode2 + 1);
		
		String newName = firstHalfName + "@" + secondHalfName;
		return newName;
	}

	private void computeAllPairs(SimilarityMetric sim, int size, Vector vRootChildren)
	{
		// Iterate through the children of the root and compute all pair-wise similarities
		for (int i = 0; i < size; i++)
		{
			DefaultMutableTreeNode curr_i = (DefaultMutableTreeNode) vRootChildren.elementAt(i);
			for (int j = i + 1; j < size; j++)
			{
				DefaultMutableTreeNode curr_j = (DefaultMutableTreeNode) vRootChildren.elementAt(j);
				double curr_sim = sim.compute(curr_i, curr_j);
				similarities[i][j] = curr_sim;
				if (curr_sim > 0) IO.put("similarity["+i+"]["+j+"] = "+curr_sim,2);
			}
		}
	}

	private void findBestIJ(int size, Vector vRootChildren)
	{
		double max = -1;
		for (int i = 0; i < size; i++)
		{
			for (int j = i + 1; j < size; j++)
			{
//				DefaultMutableTreeNode curr_i = (DefaultMutableTreeNode) vRootChildren.elementAt(i);
//				DefaultMutableTreeNode curr_j = (DefaultMutableTreeNode) vRootChildren.elementAt(j);				
//				Node ni = (Node) curr_i.getUserObject();
//				Node nj = (Node) curr_j.getUserObject();
//				IO.put("distance( "+ ni.getName() + "," + nj.getName() + "): " + similarities[i][j],1);

				double curr = similarities[i][j];
				if (curr > max)
				{
					max = curr;
					best_i = i;
					best_j = j;
				}
			}
		}
//		DefaultMutableTreeNode curr_i = (DefaultMutableTreeNode) vRootChildren.elementAt(best_i);
//		DefaultMutableTreeNode curr_j = (DefaultMutableTreeNode) vRootChildren.elementAt(best_j);				
//		Node ni = (Node) curr_i.getUserObject();
//		Node nj = (Node) curr_j.getUserObject();
//		IO.put("Picked ("+ ni.getName() + "," + nj.getName() + "): " + similarities[best_i][best_j],1);
	}

	private void shiftDistanceArray(int a, int b, int size)
	{
		if (a >= b)
		{
			IO.put("Precondition violation in shiftDistanceArray", 0);
			System.exit(0);
		}
		for (int i = a; i < b; i++)
		{
			for (int j = 0; j < size; j++)
				similarities[i][j] = similarities[i + 1][j];
			for (int j = 0; j < size; j++)
				similarities[j][i] = similarities[j][i + 1];
		}
		for (int i = b-1; i < size-2; i++)
		{
			for (int j = 0; j < size; j++)
				similarities[i][j] = similarities[i + 2][j];
			for (int j = 0; j < size; j++)
				similarities[j][i] = similarities[j][i + 2];
		}
	}
	
	protected double findValueInArray(int k, int a)
	{
		if (k<a)
		{
			return similarities [k][a];
		}
		else
		{
			return similarities [a][k];
		}
	}
}
