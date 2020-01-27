package aa;
import javax.swing.tree.DefaultMutableTreeNode;

abstract class SimilarityMetric
{
	abstract protected double compute(Node n1, Node n2);
	
	public double compute(DefaultMutableTreeNode t1, DefaultMutableTreeNode t2)
	{
		Node n1 = (Node) t1.getUserObject();
		Node n2 = (Node) t2.getUserObject();
		return compute(n1,n2);
	}
}
