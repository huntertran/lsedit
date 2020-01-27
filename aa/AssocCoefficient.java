package aa;
import java.util.Vector;

/**
* Provides methods to count the number of features that two objects, implemented
* as tree nodes, have in common, or the number of features found in one object
* but not in the other, or found in neither two objects.
*/
public class AssocCoefficient
{
	static Vector vAllFeatures; // Contains all features of all objects

	/** Returns #features that these two objects have in common. **/
	static double a(Node n1, Node n2)
	{
		int count = 0;

//		Node nObject1 = (Node) o1.getUserObject();
//		Vector v1 = nObject1.getFeatures();
//
//		Node nObject2 = (Node) o2.getUserObject();
//		Vector v2 = nObject2.getFeatures();

		for (int i = 0; i < n1.features.size(); i++)
			if (n2.features.contains((String) n1.features.elementAt(i)))
			{
				count++;
			}
		return count;

	} //end a

	/** Returns #features found in first object but not in the second object. **/
	static double b(Node n1, Node n2)
	{
		int count = 0;
		//int count_common_features = 0;

//		Node nObject1 = (Node) o1.getUserObject();
//		Vector v1 = nObject1.getFeatures();
//
//		Node nObject2 = (Node) o2.getUserObject();
//		Vector v2 = nObject2.getFeatures();

		//iterate through each feature of o1 and check whether o2 has it
		//count only those features which were not found in o2  
		for (int i = 0; i < n1.features.size(); i++)
			if (!n2.features.contains((String) n1.features.elementAt(i)))
			{
				count++;
			}
		return count;

	} //end b

	/** Returns #features found in the second object but not in the first object. **/
	static double c(Node n1, Node n2)
	{
		int count = 0;
		//int count_common_features = 0;

//		Node nObject1 = (Node) o1.getUserObject();
//		Vector v1 = nObject1.getFeatures();
//
//		Node nObject2 = (Node) o2.getUserObject();
//		Vector v2 = nObject2.getFeatures();

		//iterate through each feature of o2 and check whether o1 has it
		//count only those features which were not found in o1  
		for (int i = 0; i < n2.features.size(); i++)
			if (!n1.features.contains((String) n2.features.elementAt(i)))
			{
				count++;
			}
		return count;

	} //end c

	/** Returns #features not found neither in the first nor in the second object. **/
	static double d(Node n1, Node n2)
	{
//		if (find_all_objects_once)
//		{
//			vAllFeatures = new Vector();
//			DefaultMutableTreeNode root = (DefaultMutableTreeNode) my_treeModel.getRoot();
//			Enumeration e = root.breadthFirstEnumeration();
//			while (e.hasMoreElements())
//			{
//				DefaultMutableTreeNode curr = (DefaultMutableTreeNode) e.nextElement();
//				if (curr.isLeaf())
//				{
//					Node ncurr = (Node) curr.getUserObject();
//					Vector vFeatures = ncurr.getFeatures();
//
//					for (int i = 0; i < vFeatures.size(); i++)
//						if (!vAllFeatures.contains((String) vFeatures.elementAt(i)))
//							vAllFeatures.add(vFeatures.elementAt(i));
//				}
//			}
//			find_all_objects_once = false;
//		}

		int count = 0;
		//int count_common_features = 0;

//		Node nObject1 = (Node) o1.getUserObject();
//		Vector v1 = nObject1.getFeatures();
//
//		Node nObject2 = (Node) o2.getUserObject();
//		Vector v2 = nObject2.getFeatures();

		//iterate through all features and check whether o1 has it
		//iterate through all features and check whether o2 has it
		//count only those features which were not found in o1 nor in o2  
		for (int i = 0; i < vAllFeatures.size(); i++)
			if (!n1.features.contains((String) vAllFeatures.elementAt(i)) && !n2.features.contains((String) vAllFeatures.elementAt(i)))
			{
				count++;
			}
		//can you have duplicate features in one object?? 
		//count = vAllFeatures.size() - (v1.size() + v2.size()- count_common_features);
		return count;

	} //end d

} //end class
