package lsedit;
import java.util.Vector;
import java.util.Enumeration;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.FontMetrics;
import java.awt.TextArea;

/**
 * Developed by: Ian Davis in Summer 1997 for Grant Weddell
 * A HiGraph is a Vertex with arcs to and from it, contained in a
 * hierarchy of HiGraphs.  This class provides the methods needed
 * to create, extend, display and dispose of a HiGraph.
 *
 * In, out, and self arcs are grouped in separate vectors for efficiency.
 * This allows efficient processing of arcs, and easy counting of total
 * input and output arcs separately.  It also allows logic to ignore presence
 * of self arcs.
 *
 * To ease in attaching properties to arcs and in treating all arcs in
 * a consistent manner when desired, all arcs are supported in a common fashion
 * as a separate class.
 */

public class HiGraph {
	
	// Information which controls presentation

	int		m_width;							// Desired width of a box
	int		m_height;							// Desired height of a box

	// Information which controls layout

	int		m_xGap            = -1;		// N.B. -1 = use forms x_gap
	boolean	m_horizontalArray = false;	// Make all children adjacent horizontally
	boolean m_dontReorder     = false;	// Child order must be preserved when presenting
	boolean m_uniformDepth	  = false;	// All adjacent children must have the same vertical offset..
	boolean m_uniformWidth    = false;	// All children must have the same width
	boolean m_uniqueRank	  = false;	// No other node may appear at the same rank as this

	// The structures which define arcs within the high graph

	Vector  m_children = new Vector();	// Inclusion arcs to children
	HiArc   m_parent   = null;			// The arc in my parents children set that addresses me
	Vector  m_out      = new Vector();	// Non-inclusion output arcs to another node
	Vector  m_in       = new Vector();	// Non-inclusion input arcs from another node
	Vector  m_self     = new Vector();	// Non-inclusion arcs from this to this
	Vector	m_hidden   = null;			// Hidden children
	int		m_depth    = 0;				// Depth of this node within the inclusion tree

	HiGraph m_duplicate;				// Used when duplicating the graph .. otherwise null

	/* Used to build feasible ranking solution */

	HiGraph	m_sink     = null;			// The sink child node
	int		m_sink_rank;
	int		m_visited = 0;				// Used for cycle checking
	int		m_inputs;					// Count of unscanned input edges (used in feasibleRank)
	int		m_rank = 0;					// Vertical ranking of node
	int		m_weight;					// The weight of this node (also used by simplex).

	/* Used when building undirected spanning tree over graph */

	HiArc	m_back		 = null;		// Undirected spanning arc to parent in spanning tree
	Vector	m_span		 = null;		// Undirected arcs to children of spanning node
	int		m_minbeneath;				// Minimum post order traversal number beneath me
	int		m_postorder;				// Post order traversal number within spanning tree
	
	/* Used when setting up the nodes in a row */

	int		m_dummy_node   = 0;			// Set to values below if unusual else 0
	final static int	edgepointV = 1;
	final static int	sinkV      = 2;

	int		m_position		  = 0;		// The order within the row (all nodes with same rank)
	double	m_rowweight       = 0;
	int		m_outside	      = 0;		// Right outside edges to/from node - left outside edges to from node.
	
	/* Used to establish the layout coordinates */

	int			m_x;					// Centre of rectangle on x axis
	int			m_y;					// Centre of rectangle on y axis
	
	/* Thing being laid out */

	EntityInstance	m_object;
	String			m_label;			// This is an aid when debugging

	/**
	 * Constructor.
	 */

	public HiGraph(EntityInstance object, String label, int width, int height) 
	{
		super();
		m_object = object;
		m_width  = width;
		m_height = height;
		m_label  = label;
	}

	public String toString()
	{
		return m_label;
	}

	public String label()
	{
		return m_label;
	}

	public int dummyNode()
	{
		return m_dummy_node;
	}

	public boolean edgePoint()
	{
		return(m_dummy_node == edgepointV);
	}

	public EntityInstance getReferencedObject()
	{
		return(m_object);
	}

	public int x()
	{
		return(m_x);
	}

	public void x(int value)
	{
		m_x = value;
	}

	public int y()
	{
		return(m_y);
	}

	public void y(int value)
	{
		m_y = value;
	}

	public int width() 
	{
		return(m_width);
	}
	
	public void width(int width)
	{
		m_width = width;
	}

	public int height() 
	{
		return(m_height);
	}

	public void height(int value) 
	{
		m_height = value;
	}

	/* The depth of a node in the inclusion tree */

	public int depth() {
		return(m_depth);
	}
	
	public int rank() {
		return(m_rank);
	}

	public Vector children() {
		return(m_children);
	}

	public Vector in() {
		return(m_in);
	}

	public Vector out() {
		return(m_out);
	}

	// Used by controlflowgraph to change value of rank

	public void rank(int value) {
		m_rank = value;
	}

	void sinkrank(int value) {
		if (m_sink != null) {
			m_sink.m_rank = value;
		}
		m_sink_rank = value;
	}

	/* Create a new child node contained in this higraph and return
     * it to the caller as a new object
	 */

		
	public HiGraph newChild(EntityInstance object, String label, int width, int height) 
	{
		HiGraph	child = new HiGraph(object, label, width, height);
		HiArc   arc   = new HiArc(this, child, true);

		child.m_parent = arc;
		child.m_depth  = this.m_depth + 1;
		child.m_rank   = child.m_depth;
		m_children.addElement(arc);

		return(child);
	}

	public HiGraph newChild(int type)
	{
		String label;

		switch (type) {
		case edgepointV:
			label = "edgepoint";
			break;
		case sinkV:
			label = "sinkpoint";
			break;
		default:
			label = "";
		}

		HiGraph	child = newChild(null, label, 0, 0);
		child.m_dummy_node = type;
		return(child);
	}

	public HiGraph newChild()
	{
		HiGraph	child = newChild(edgepointV);
		return(child);
	}

	public void xGap(int value) 
	{
		m_xGap = value;
	}

	public int xGap() {
		return(m_xGap);
	}

	public void horizontalArray(boolean value) {
		m_horizontalArray = value;
	}

	public boolean horizontalArray() {
		return(m_horizontalArray);
	}

	public void dontReorder(boolean value) {
		m_dontReorder = value;
	}

	public boolean dontReorder() {
		return(m_dontReorder);
	}

	public void uniqueRank(boolean value) {
		m_uniqueRank = value;
	}

	public boolean uniqueRank() {
		return(m_uniqueRank);
	}

	public void uniformDepth(boolean value) {
		m_uniformDepth = value;
	}

	public boolean uniformWidth() {
		return(m_uniformWidth);
	}

	public void uniformWidth(boolean value) {
		m_uniformWidth = value;
	}

	public boolean uniformDepth() {
		return(m_uniformDepth);
	}

	/* Add an arc from the specified hiGraph node to this higraph node */

	public HiArc newInputArc(HiGraph from) {
		HiArc arc = new HiArc(from, this);
	
		if (from == this) {
			this.m_self.addElement(arc);
		} else {
			from.m_out.addElement(arc);
			this.m_in.addElement(arc);
		}
		return(arc);
	}

	void newInputArc(HiArc arc) {		// Used to add dummy edges
		m_in.addElement(arc);
	}

	/* Add an arc from this higraph node to the specified higraph node */

	void newOutputArc(HiArc arc) {		// Used to add dummy edges
		arc.from(this);
		m_out.addElement(arc);
	}

	public HiArc newOutputArc(HiGraph to) {
		return(to.newInputArc(this));
	}

	static void removeArc(Vector arcs, HiArc arc) throws HiGraphException {

		if (arcs == null || arc == null || !arcs.removeElement(arc)) {
			String  s    = "Can't remove " + arc;
			if (arcs == null) {
				s += " from null vector";
			}
			if (arc != null) {
				HiGraph node;
				for (int i = 0; i < 2; ++i) {
					node = (i == 0 ? arc.from() : arc.to());
					if (arcs == node.m_children) {
						s += " from " + node + ".m_children";
					}
					if (arcs == node.m_in) {
						s += " from " + node + ".m_in";
					}
					if (arcs == node.m_out) {
						s += " from " + node + ".m_out";
					}
					if (arcs == node.m_self) {
						s += " from " + node + ".m_self";
					}
					if (arcs == node.m_span) {
						s += " from " + node + ".m_span";
			}	}	}
			throw new HiGraphException(s);
	}	}

	public HiArc locateInputArc(HiGraph from, boolean chain) {
		HiGraph	previous;
		HiArc	arc;
		Vector	v = ((from == this) ? m_self : m_in);
		
		for (Enumeration e = v.elements(); e.hasMoreElements(); ) {
			arc      = (HiArc) e.nextElement();
			previous = arc.from();
			if (previous == from) {
				return(arc);
			}
			if (chain && previous.m_dummy_node != 0 && previous.locateInputArc(from, chain) != null) {
				return(arc);
		}	}
		return(null);
	}

	/* Eliminate this higraph node and anything contained within it as well
     * as any arcs to/from it.  Check the HiGraph structures for internal
	 * consistency while we are at it.  Note that we divide this into two
	 * routines because we can't remove children from parent while iterating
	 * over these same children.. The enumeration gets confused.
	 */

	private void dispose_internal() throws HiGraphException 
	{
		HiArc		arc;
		HiGraph		other;
		Enumeration e;
		
		/* Remove all input arcs into this node */

		for (e = m_in.elements(); e.hasMoreElements(); ) {
			arc = (HiArc) e.nextElement();
			other = arc.from();
			if (other == this || arc.to() != this) {
				throw new HiGraphException(this + " has an illegal input arc " + arc);
			}
			removeArc(other.m_out, arc);
		}

		/* Remove all output arcs from this node */

		for (e = m_out.elements(); e.hasMoreElements(); ) {
			arc   = (HiArc) e.nextElement();
			other = arc.to();
			if (arc.from() != this || other == this) {
				throw new HiGraphException(this + " has an illegal output arc " + arc);
			}
			removeArc(other.m_in, arc);
		}

		m_in.removeAllElements();
		m_out.removeAllElements();
		m_self.removeAllElements();
						
		/* dispose all descendants contained under this node */

		for (e = m_children.elements(); e.hasMoreElements(); ) {
			arc   = (HiArc) e.nextElement();
			other = arc.to();
			other.dispose_internal();
		}
		m_children.removeAllElements();
		
		if (m_hidden != null) {
			m_hidden.removeAllElements();
			m_hidden = null;
		}

		m_in       = null;
		m_out      = null;
		m_self     = null;
		m_parent   = null;
		m_children = null;
	}

	public void dispose() throws HiGraphException 
	{
		HiGraph parent;

		if (m_parent != null) {
			parent = m_parent.from();
			if (!parent.m_children.removeElement(m_parent) &&
				!parent.m_hidden.removeElement(m_parent) ) {
				throw new HiGraphException("Can't disconnect " + this + " from " + parent);
		}	}
				
		dispose_internal();
	}

	void removeDummy() throws HiGraphException {
		int		size  = m_children.size();
		HiArc	arc, arc1, arc2;
		HiGraph	child, from, to;
		int		i;

		for (i = 0; i < size; ) {
			arc   = (HiArc) m_children.elementAt(i);
			child = arc.to();

			if (child.m_dummy_node == HiGraph.edgepointV) {
				arc1       = (HiArc) child.m_in.elementAt(0);
				from       = arc1.from();
				arc2       = (HiArc) child.m_out.elementAt(0);
				arc2.from(from);
				from.m_out.removeElement(arc1);
				from.m_out.addElement(arc2);
				m_children.removeElementAt(i);
				--size;
				continue;
			}
			child.removeDummy();
			++i;
	}	}

	void orderByRank() {
		int	children = m_children.size();
		HiArc	arc1, arc2;
		HiGraph child1, child2;
		int	i, j, best;

		for (i = 0; i < children; ++i) {
			arc1   = (HiArc) m_children.elementAt(i);
			child1 = arc1.to();
			best   = i;
			for (j = i+1; j < children; ++j) {
				arc2   = (HiArc) m_children.elementAt(j);
				child2 = arc2.to();
				if (child2.m_rank < child1.m_rank) {
					best   = j;
					child1 = child2;
			}	}
			if (best != i) {
				arc1 = (HiArc) m_children.elementAt(i);
				arc2 = (HiArc) m_children.elementAt(best);
				m_children.setElementAt(arc1, best);
				m_children.setElementAt(arc2, i);
	}	}	}
	
	public void describe() {

		HiArc		arc;
		Enumeration e;
				
		System.out.println("\nDescription of " + label());
		System.out.println("Depth=" + m_depth + " rank=" + m_rank + "-" + m_sink_rank + " position=" + m_position);
		System.out.println("x="+m_x+" y="+m_y+" width="+m_width+" height="+m_height);
		System.out.println(" Weight=" + m_weight + " outside=" + m_outside + " type=" + m_dummy_node );

		if (m_parent != null) {
			System.out.println("   Parent: " + m_parent );
		}

		for (e = m_children.elements(); e.hasMoreElements(); ) {
			arc   = (HiArc) e.nextElement();
			System.out.println("    Child: " + arc );
		}
		
		if (m_hidden != null) {
			for (e = m_hidden.elements(); e.hasMoreElements(); ) {
				arc   = (HiArc) e.nextElement();
				System.out.println("   Hidden: " + arc );
		}	}

		for (e = m_out.elements(); e.hasMoreElements(); ) {
			arc = (HiArc) e.nextElement();
			System.out.println("   Arc to: " + arc);
		}

		for (e = m_in.elements(); e.hasMoreElements(); ) {
			arc = (HiArc) e.nextElement();
			System.out.println(" Arc from: " + arc);
		}

		for (e = m_self.elements(); e.hasMoreElements(); ) {
			arc = (HiArc) e.nextElement();
			System.out.println(" Self arc: " + arc);
		}
	}

	/* Create an output dump of the higraphs contained within
	 * this higraph node.
	 */
	
	public void dump() {

		String		out = "";
		HiArc		arc;
		Enumeration e;
				
		for (int indent = m_rank; indent > 0; --indent) {
		 	out += " ";
		}
		
		out += label() + "[rank=" + m_rank;
		
		if (m_rank != m_sink_rank) {
			out += "-" + m_sink_rank;
		}
		out += "/" + m_position + "/" + m_rank + "{"+m_x+","+m_y+":"+m_width+","+m_height+ "}]";
		
		if (m_back != null) {
			out += " back=" + m_back;
		}
		for (e = m_children.elements(); e.hasMoreElements(); ) {
			arc   = (HiArc) e.nextElement();
			out  += " " + arc;
			if (out.length() > 70) {
				System.out.println(out);
				out = " ";
		}	}

		out += " | ";
		for (e = m_out.elements(); e.hasMoreElements(); ) {
			arc = (HiArc) e.nextElement();
			out += " " + arc;
		}

		out += " | ";
		for (e = m_in.elements(); e.hasMoreElements(); ) {
			arc = (HiArc) e.nextElement();
			out += " " + arc;
		}

		if (m_self.size() != 0) {
			out += " | ";
			for (e = m_self.elements(); e.hasMoreElements(); ) {
				arc = (HiArc) e.nextElement();
				out += " " + arc;
		}	}

		System.out.println(out);
		
		HiGraph child;

		for (e = m_children.elements(); e.hasMoreElements(); ) {
			arc   = (HiArc) e.nextElement();
			child = arc.to();
			child.dump();
	}	}

	private void duplicateNodes() {
		HiArc		arc;
		HiGraph		child;
		
		m_duplicate         = new HiGraph(m_object, m_label, m_width, m_height);
		m_duplicate.m_depth = m_depth;
		for (Enumeration e = m_children.elements(); e.hasMoreElements(); ) {
			arc   = (HiArc) e.nextElement();
			child = arc.to();
			child.duplicateNodes();
	}	}
		
	private void duplicateEdges() {
		HiGraph		from, to;
		HiGraph		other;
		HiArc		arc, edge;
		Enumeration	e;

		from = m_duplicate;

		for (e = m_children.elements(); e.hasMoreElements(); ) {
			edge  = (HiArc) e.nextElement();
			other = edge.to();
			to    = other.m_duplicate;

			arc   = new HiArc(from, to, true);
			from.m_children.addElement(arc);
			to.m_parent = arc;
			other.duplicateEdges();
		}

		for (e = m_out.elements(); e.hasMoreElements(); ) {
			edge  = (HiArc) e.nextElement();
			other = edge.to();
			to    = other.m_duplicate;
			
			arc   = new HiArc(from, to);
			from.m_out.addElement(arc);
			to.m_in.addElement(arc);
		}

		for (e = m_self.elements(); e.hasMoreElements(); ) {
			edge  = (HiArc) e.nextElement();
			arc   = new HiArc(from, from);
			from.m_self.addElement(arc);
	}	}

	private void clearReferences() {
		HiArc		arc;
		HiGraph		child;
		
		m_duplicate = null;
		for (Enumeration e = m_children.elements(); e.hasMoreElements(); ) {
			arc   = (HiArc) e.nextElement();
			child = arc.to();
			child.clearReferences();
	}	}

	HiGraph duplicate() {
		HiGraph ret;
		duplicateNodes();
		duplicateEdges();
		ret = m_duplicate;
		clearReferences();
		return(ret);
	}

 	/* Shift this node and all children by xdelta and ydelta */

	void shift(int xdelta, int ydelta) {

		m_x += xdelta;
		m_y += ydelta;

		HiArc		arc;
		HiGraph		child;
		
		for (Enumeration e = m_children.elements(); e.hasMoreElements(); ) {
			arc   = (HiArc) e.nextElement();
			child = arc.to();
			child.shift(xdelta, ydelta);
	}	}
}
