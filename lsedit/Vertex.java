package lsedit;

// The class Vertex is used in SugiyamaLayout.java.	 It stores information
// of a vertex (box) in the graph.

import java.util.Enumeration;
import java.util.Vector;

class Vertex {

  private int layer;		  // layer to which the vertex is assigned
  private int inDegree;		  // number of parents
  private int outDegree;	  // number of children
  private double width;		  // width of the box
  private boolean visited;
  private boolean isDummy;
  private Vector parents;	  // list of parents of the vertex
  private Vector children;	  // list of children of the vertex

/***************************************************************************/

  public Vertex(double boxWidth) {
	parents = new Vector();
	children = new Vector();
	layer = -1;
	inDegree = 0;
	outDegree = 0;
	isDummy = false;
	visited = false;
	width = boxWidth;
  }

/****************************************************************************/

  public void addChild(Integer child) {
	if (children.indexOf(child) == -1) {
	  children.addElement(child);
	  outDegree++;
	}
  }

  public boolean removeChild(Integer child) {
	return children.removeElement(child);
  }

  public void replaceChild(Integer oldChild, Integer newChild) {
	try {
	  children.setElementAt(newChild, children.indexOf(oldChild));
	} catch (ArrayIndexOutOfBoundsException e) {
	  System.exit(-1);
	} // try
  }
	  
  public void addParent(Integer parent) {
	if (parents.indexOf(parent) == -1) {
	  parents.addElement(parent);
	  inDegree++;
	}
  }

  public boolean removeParent(Integer parent) {
	return parents.removeElement(parent);
  }

  public void replaceParent(Integer oldParent, Integer newParent) {
	try {
	  parents.setElementAt(newParent, parents.indexOf(oldParent));
	} catch (ArrayIndexOutOfBoundsException e) {
	  System.out.println("array problem");
	  System.exit(-1);
	} // try
  }

  public void setLayer(int newLayer) {
	layer = newLayer;
  }

  public int getLayer() {
	return layer;
  }

  public void setDummy() {
	isDummy = true;
  }

  public double getWidth() {
	return width;
  }

  public int getInDegree() {
	return inDegree;
  }

  public int getOutDegree() {
	return outDegree;
  }

  public void decInDegree() {
	inDegree--;
  }

  public void decOutDegree() {
	outDegree--;
  }

  public void setVisited() {
	visited = true;
  }

  public boolean isVisited() {
	return visited;
  }

  public Vector getParents() {
	return parents;
  }

  public Vector getChildren() {
	return children;
  }

  public void print() {
	System.out.print("Parents: ");
	for (Enumeration e = parents.elements(); e.hasMoreElements(); ) {
	  System.out.print(e.nextElement() + " ");
	}
	System.out.println();
	System.out.print("Children: ");
	for (Enumeration e = children.elements(); e.hasMoreElements(); ) {
	  System.out.print(e.nextElement() + " ");
	}
	System.out.println();
	System.out.println("Layer " + layer);
  }

} // Vertex

