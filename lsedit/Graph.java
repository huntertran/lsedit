package lsedit;
import java.util.Enumeration;
import java.util.Vector;

// The class graph holds information of a graph (set of selected boxes) for 
// manipulation.

class Graph {

  private Vector vertices;			// adjacency list
  private int[] labelled;			// label of each vertex
  private int curLabel;
  private int size;					// number of vertices
  private int numLayers;			// number of layers
  private boolean debugMode = false;

/***************************************************************************/

  // constructor

  public Graph(int numVertices) {
	size = 0;
	curLabel = 1;
	vertices       = new Vector();
	labelled       = new int[numVertices];
	for (int i = 0; i < numVertices; i++) {
	  labelled[i] = 0;
	}
  } // Graph

/***************************************************************************/

  public void addVertex(double boxWidth) 
  {
	vertices.addElement(new Vertex(boxWidth));
	size++;
  } // addVertex

/***************************************************************************/

  public Vertex getVertex(int vertexID) 
  {
	return (Vertex)vertices.elementAt(vertexID);
  } // getVertex

/***************************************************************************/

  public void addGraphEdge(int sourceID, int destID) 
  {
	getVertex(sourceID).addChild(new Integer(destID));
	getVertex(destID).addParent(new Integer(sourceID));
  } // addGraphEdge

/***************************************************************************/

  // for testing purpose

  public void print() 
  {
	int x = 0;
	for (Enumeration e = vertices.elements(); e.hasMoreElements(); ) {
	  System.out.print(x + "  ");
	  x++;
	  ((Vertex)e.nextElement()).print();
	}
  }

/****************************************************************************
 METHODS FOR CYCLE REMOVAL
 ***************************************************************************/

  // Updates the number of parents after removing a vertex from the graph
  // in the cycle removal process.

  private void DeleteInEdges(int vertexID) 
  {
	// remove edges going to the vertex
	Vector parentVertices = getVertex(vertexID).getParents();
	for (Enumeration e = parentVertices.elements(); e.hasMoreElements(); ) {
	  getVertex(((Integer)e.nextElement()).intValue()).decOutDegree();
	} // for

  } // DeleteInEdges

/****************************************************************************/

  // Updates the number of children after removing a vertex from the graph
  // in the cycle removal process.

  private void DeleteOutEdges(int vertexID, Vector precedingVertices) 
  {
	// get list of children
	Vector childrenVertices = getVertex(vertexID).getChildren();
	Integer child = null;

	Vector childrenToBeRemoved = new Vector();

	for (Enumeration e = childrenVertices.elements(); e.hasMoreElements(); ) {
	  child = (Integer)e.nextElement();
	  getVertex(child.intValue()).decInDegree();
	  int index = precedingVertices.indexOf(child);

	  // child is in front in topological order, reverse edges
	  if (index != -1) {
		if (debugMode) 
		  System.out.println("Reverse " + vertexID + "->" + child.intValue());
		getVertex(child.intValue()).removeParent(new Integer(vertexID));
		getVertex(child.intValue()).addChild(new Integer(vertexID));
		childrenToBeRemoved.addElement(child);
		getVertex(vertexID).addParent(child);

	  } // if
   } // for

	for (Enumeration f = childrenToBeRemoved.elements(); f.hasMoreElements();){
		getVertex(vertexID).removeChild((Integer)f.nextElement());
	}

  } // DeleteOutEdges

/****************************************************************************/

  private int FindSink(int curSize, Vector precedingVertices) 
  {
	int i;

	//System.out.println("curSize = " + curSize);
	do {

	  for (i = 0; i < size; i++ ) {

		// if it is a sink, remove from graph

		if (getVertex(i).getOutDegree() == 0 && getVertex(i).isVisited() == false) {

		  getVertex(i).setVisited();

		  // Remove vertex i from graph
		  curSize--;
		  DeleteOutEdges(i, precedingVertices);
		  DeleteInEdges(i);
		  break;
		}
	  } // for

	} while (i < size && curSize > 0);

	return curSize;

  } // FindSink

/***************************************************************************/

  private int FindSource(int curSize, Vector precedingVertices) {

	int i;
	// System.out.println("curSize = " + curSize);

	do {

	  for (i = 0; i < size; i++) {

		// if it is a source, remove from graph

		if (getVertex(i).getInDegree() == 0 &&
			getVertex(i).isVisited() == false) {

		  getVertex(i).setVisited();

		  // Remove vertex i from graph
		  curSize--;
		  DeleteOutEdges(i, precedingVertices);
		  DeleteInEdges(i);
		  break;
		} // if

	  } // for

	} while (i < size && curSize > 0);

	return curSize;

  } // FindSource

/***************************************************************************/

  private int FindNext(int curSize, Vector precedingVertices) {

	int diff = 0;
	int maxDiff = -1000; // a less arbitrary "small" number?
	int maxVertex = size;

	// find vertex with maximum (outdegree - indegree)
	for (int i = 0; i < size; i++) {
	  if (getVertex(i).isVisited() == false) {
		diff = getVertex(i).getOutDegree() - getVertex(i).getInDegree();
		if (diff > maxDiff) {
		  maxDiff = diff;
		  maxVertex = i;
		}
	  } // if
	} // for

	// remove vertex from graph
	curSize--;
	getVertex(maxVertex).setVisited();
	DeleteOutEdges(maxVertex, precedingVertices);
	DeleteInEdges(maxVertex);
	precedingVertices.addElement(new Integer(maxVertex));
	return curSize;

  } // FindNext

/***************************************************************************/

  private void RemoveCycles() {

	// Non-source vertices preceding current vertex; may have to reverse edge
	Vector precedingVertices = new Vector();
	int curSize = size;

	do {

	  curSize = FindSink(curSize, precedingVertices);
	  if (curSize == 0) break;
	  curSize = FindSource(curSize, precedingVertices);
	  if (curSize == 0) break;
	  curSize = FindNext(curSize, precedingVertices);

	} while (curSize > 0);

  } // RemoveCycles

/***************************************************************************
 METHODS FOR LAYER ASSIGNMENT
 **************************************************************************/

  // Returns the labels of vertices in the vector parentIDs

  private Vector getLabels(Vector parentIDs) {
	Vector labelVector = new Vector();

	for (Enumeration e = parentIDs.elements(); e.hasMoreElements(); ) {
	  int curLabel = labelled[((Integer)e.nextElement()).intValue()];
	  if (curLabel == 0) return null;
	  labelVector.addElement(new Integer(curLabel));
	}
	return labelVector;

  } // getLabels

/**************************************************************************/

  // intVector must be non-empty;  return max integer from vector

  private Integer maxValue(Vector intVector) 
  {
	Integer maxInt = (Integer)intVector.firstElement();
	for (int i = 1; i < intVector.size(); i++) {
	  if (((Integer)intVector.elementAt(i)).intValue() > maxInt.intValue())
		maxInt = (Integer)intVector.elementAt(i);
	}
	return maxInt;
  } // maxValue

/***************************************************************************/

  // < comparison for lexicographic order defined on page 275 of "Graph
  // "Drawing";	 used to determine the order in which vertices are labelled

  private boolean lessThan(Vector vector1, Vector vector2) 
  {
	if (vector1.isEmpty() && !vector2.isEmpty()) return true;

	else if (!vector1.isEmpty() && !vector2.isEmpty()) {

	  Integer maxValue1 = maxValue(vector1);
	  Integer maxValue2 = maxValue(vector2);

	  if (maxValue1.intValue() < maxValue2.intValue()) return true;
	  else if (maxValue1 == maxValue2) {
		Vector newVector1 = (Vector)vector1.clone();
		newVector1.removeElement(maxValue1); 
		Vector newVector2 = (Vector)vector2.clone();
		newVector2.removeElement(maxValue2);
		return lessThan(newVector1, newVector2);
	  }
	}

	return false;

  } // lessThan

/***************************************************************************/

  // label each vertex in lexicographic order; see page 275 of "Graph Drawing"

  private void AssignLabel() 
  {
	for (int j = 0; j < size; j++) {

	  int minVertex = 0;
	  Vector minParentsLabels = new Vector();
	  minParentsLabels.addElement(new Integer(size));

	  for (int i = 0; i < size; i++) {

		if (labelled[i] == 0) {
		  Vector curParentsLabels = getLabels(getVertex(i).getParents());
		  if (curParentsLabels != null && 
			  lessThan(curParentsLabels, minParentsLabels)) {
			minParentsLabels = curParentsLabels;
			minVertex = i;
		  }
		}
	  }

	  labelled[minVertex] = curLabel++;
	}
  } // AssignLabel

/****************************************************************************/

  // return true if all children have been placed in lower rows

  private boolean childrenPlaced(int vertexID) 
  {
	Vector childrenVertices = getVertex(vertexID).getChildren();
	boolean result = true;
	for (Enumeration e = childrenVertices.elements(); e.hasMoreElements(); ) {
	  if (labelled[((Integer)e.nextElement()).intValue()] != -1) {
		result = false;
		break;
	  }
	}
	return result;
  } // childrenPlaced

/****************************************************************************/

  // Dummy vertices are added with an edge spans more than one layer.

  private void AddDummies(int vertexID, int curLayer, Vector layers) 
  {
	Vector childrenVertices = getVertex(vertexID).getChildren();
	for (Enumeration e = childrenVertices.elements(); e.hasMoreElements(); ) {
	  Integer child = (Integer)e.nextElement(); 
	  int childLayer = getVertex(child.intValue()).getLayer();

	  // add dummy vertices if edge spans more than one layer

	  if (curLayer - childLayer > 1) {

		int curIndex = vertices.size();
		int childIndex = child.intValue();
		getVertex(childIndex).replaceParent(new Integer(vertexID), new Integer(curIndex));

		// put in one dummy vertex per layer per edge

		for (int i = childLayer + 1; i < curLayer - 1; i++) {
			vertices.addElement(new Vertex(0));
			((Vector)layers.elementAt(i)).addElement(new Integer(size));
			size++;
			getVertex(curIndex).setDummy();
			getVertex(curIndex).setLayer(i);
			getVertex(curIndex).addChild(new Integer(childIndex));
			getVertex(curIndex).addParent(new Integer(curIndex + 1));
			childIndex = curIndex;
			curIndex++;
		}

		vertices.addElement(new Vertex(0));
		((Vector)layers.elementAt(curLayer - 1)).addElement(new Integer(size));
		size++;
		getVertex(curIndex).setDummy();
		getVertex(curIndex).setLayer(curLayer - 1);
		getVertex(curIndex).addChild(new Integer(childIndex));
		getVertex(curIndex).addParent(new Integer(vertexID));

		// update parent of vertex in lower level

		getVertex(vertexID).replaceChild(child, new Integer(curIndex));
	  } // if
  
	} // for

  } // AddDummies
		
/****************************************************************************/

  // The doCoffmanGrahamSugiyama method does the following:
  // 1. Remove cycles in the graph using the greedy algorithm (p.297)
  // 2. Assign layer to each vertex using the Coffman-Graham algorithm (p.275)
  // 3. Reorder position of vertices on each layer using the Barycentre
  //	method in [Sugiyama's paper .. details?]

  public Vector doCoffmanGrahamSugiyama() 
  {
	RemoveCycles();
	if (debugMode) System.out.println("finish removeCycle");
	AssignLabel();
	if (debugMode) System.out.println("finish assignLabel");

	// Assign layer

	int layerNo = 0;
	Vector thisLayer = new Vector();
	double curWidth = 0;

	// 2D-array representing layers of vertices
	Vector layers = new Vector();
	layers.addElement(new Vector());

	// need this variable since dummies may be added
	int currentSize = size;

	double newWidth = 0;

	for (int numPlacedVertices = 0; numPlacedVertices < currentSize; ) {
	  int maxLabel = 0;
	  int maxVertex = currentSize;

	  // find vertex with maximum label whose children have all be placed
	  for (int i = 0; i < currentSize; i++) {
		if (labelled[i] > maxLabel && childrenPlaced(i)) {
		  maxLabel = labelled[i];
		  maxVertex = i;
		}
	  }

	  // label the vertex

	  if (maxVertex < currentSize) {
		newWidth = curWidth + getVertex(maxVertex).getWidth();
	  }

	  if (maxVertex < currentSize && newWidth < 1.0) {
		getVertex(maxVertex).setLayer(layerNo);
		thisLayer.addElement(new Integer(maxVertex));
		((Vector)layers.lastElement()).addElement(new Integer(maxVertex));
		AddDummies(maxVertex, layerNo, layers);
		labelled[maxVertex] = 0;
		numPlacedVertices++;
		curWidth = newWidth;
	  }

	  // check if a new row needed
	  if (maxVertex >= currentSize || newWidth >= 1.0) {
		layerNo++;
		// indicate the present row is full
		for (Enumeration e = thisLayer.elements(); e.hasMoreElements(); ) {
		  labelled[((Integer)e.nextElement()).intValue()] = -1;
		}
		layers.addElement(new Vector());
//		System.out.println("Graph.doCoffmanGrahamSugiyama layers size = " + layers.size() + "/" + layers.capacity());
		curWidth = 0;
	  }

	} // for

	numLayers = layerNo;
	Sugiyama(layers);
	if (debugMode) System.out.println("Finish Sugiyama");
	return layers;

  } // doCoffmanGrahamSugiyama

/****************************************************************************
 METHODS FOR SUGIYAMA METHOD
 ***************************************************************************/

  // The Sort method takes an array of barycentre values of the vertices on
  // the bottom row, and the vertex numbers of the vertices on the bottom row.
  // It sorts the vertices by increasing value of their barycentre.

  // Think about which sort algorithm would be best

  private void Sort(float[] barycentres, Vector bottomRow) {

	int pos = 0;
	float tempBary;
	Integer tempVertex = null;

	for (int i = 1; i < bottomRow.size(); i++) {
	  pos = 0;
	  tempBary = barycentres[i];
	  tempVertex = (Integer) bottomRow.elementAt(i);

	  while (pos < i) {
		if (barycentres[i] < barycentres[pos]) break;
		pos++;
	  }
	  for (int k = i; k > pos; k--) {
		barycentres[k] = barycentres[k-1];
		//order[k] = order[k-1];
	  }

	  bottomRow.removeElementAt(i);
	  bottomRow.insertElementAt(tempVertex, pos);
	  barycentres[pos] = tempBary;
	}
  } // Sort

/****************************************************************************/

  public void BarycentreSort(Vector topRow, Vector bottomRow, Vector relations) 
  {

	float[] barycentres = new float[bottomRow.size()];
	int nominator = 0;
	int denominator = 0;

	for (int i = 0; i < bottomRow.size(); i++) {
		nominator = 0;
		denominator = 0;
		for (Enumeration e = ((Vector)(relations.elementAt(i))).elements();  e.hasMoreElements(); ) {
			Integer x = (Integer) e.nextElement();
			int index = topRow.indexOf(x);
			if (index != -1) {
				nominator += 1+index;
				denominator++;
			}
		}
		if (denominator == 0) {
			barycentres[i] = 0;
		} else {
			barycentres[i] = ((float)nominator)/((float)denominator);
		}
	}
	Sort(barycentres, bottomRow);

  } // BarycentreSort

/****************************************************************************/

  // Sort order of vertices in each row by barycentre value

  private void Sugiyama(Vector layers) 
  {
	for (int i = layers.size() - 2; i >= 0; i--) {
	  Vector relations = new Vector();
	  for (Enumeration e = ((Vector)(layers.elementAt(i))).elements(); e.hasMoreElements(); ) {
		Integer x = (Integer)e.nextElement();
		relations.addElement(getVertex(x.intValue()).getParents());
	  } // for

	  BarycentreSort((Vector)layers.elementAt(i+1), (Vector)layers.elementAt(i), relations);
	} // for

  } // Sugiyama

} // Graph





