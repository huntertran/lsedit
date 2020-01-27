package aa;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

/**
* This class encapsulates information about a node.
*/
public class Node 
{
    private String name;
    
    private DefaultMutableTreeNode treeNode;
    
    public Vector features; // Contains all the features of this node as Strings
    
    private double value;
    
    public boolean equals(Object o)
    {
    	Node e = (Node) o;
    	return (this.getName()).equals(e.getName());
    }

    /**
     * Creates a node initialized with the specified parameter as its name
     * and a value of 1.0.
     *
     * @param name	 the name of this node
     */
    public Node(String name) 
    {
	this.name = name;
	this.features = new Vector(); 
	this.value = 1.0;
    }
 
 
    /**
     * Adds a new feature to this object's vector.
     *
     * @param name	 new feature of this object
     */
    public void addFeature(String i)
    {
    	features.add(i); 
    }
    
    
//    /**
//     * Returns the features of this object's in a vector.
//     *
//     * @param name	 new feature of this object
//     */
//    public Vector getFeatures()
//    {
//    	return features; 
//    }
    
    /**
     * Sets the value of <code>this</code> node
     * @param name the name of this node
     */
    public void setValue(double d) { this.value = d; }
    
    /**
     * Sets the name of <code>this</code> node
     * @param name the name of this node
     */
    public void setName(String name) { this.name = name; }
    
     
    public void setTreeNode(DefaultMutableTreeNode treeNode)
      { this.treeNode = treeNode; }
   
  
    /**
     * Returns <code>this</code> node's name
     * @return the name of this node
     */
    public String getName() { return name; }
    
    /**
     * Sets the name of <code>this</code> node
     * @param name the name of this node
     */
    public double getValue() { return value; }
    
    /**
     * Returns the tree node to which this node is referring 
     * @return the tree node 
     */
    public DefaultMutableTreeNode getTreeNode() { return treeNode; }
   
    
    /**
     * Returns a string representation of <code>this</code> node
     * @return name followed by the type of this node
     */
    public String toString() { return name; }

}
