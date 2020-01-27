package lsedit;
import java.util.Vector;
import java.util.Enumeration;

/**
 * Developed by: Ian Davis in Summer 1997 for Grant Weddell
 * Provides a static method to compute an optimal vertical ranking for use when displaying a HiGraph.
 * The ranking is optimal in having the minimal total weighted edge length subject to the minlength
 * associated with each edge.  Note that the algorithm is also used to compute an optimal horizontal
 * ranking by performing the computation on a carefully constructed auxilary graph.
 */

/*
 *
 * Note that because this code occurs in a new file it must be in a new class.. we really want
 * it to be in class HiGraph.  Worse, because it is in a new class any method defined here cannot
 * be applied against a this of type HiGraph.  However we don't want to subclass because Rank is
 * something that can be done to any HiGraph, because if we subclass we have to define an ordering
 * for these subclasses and can use only one per superclass, and we have to overload the creation
 * of both arcs and nodes so that an equivalent HiRankArc and HiRankGraph exists, since none of
 * the code here will operate on a this of type HiGraph.. an exception condition is raised.  YUK!!
 *
 * The solution all be it a sad one is to use static functions which operate on HiGraph structures.
 */

class HiSimplex {
		
	static final boolean debug = false;		// Set to validate simplex cycles

	// Dump the constraints for debugging purposes

	public static void dump_constraints(HiGraph node) {

		HiGraph		child;
		HiArc		arc;
		Enumeration e;
				
		System.out.print(node + ":");
		for (e = node.m_children.elements(); e.hasMoreElements(); ) {
			arc   = (HiArc) e.nextElement();
			child = arc.to();
			System.out.print(" =" + arc.getMinlength() + "(" + arc.getWeight() + ")=>" + child);
		}

		for (e = node.m_out.elements(); e.hasMoreElements(); ) {
			arc   = (HiArc) e.nextElement();
			child = arc.to();
			System.out.print(" -" + arc.getMinlength() + "(" + arc.getWeight() + ")->" + child);
		}
		System.out.print("\n");

		for (e = node.m_children.elements(); e.hasMoreElements(); ) {
			arc   = (HiArc) e.nextElement();
			child = arc.to();
			dump_constraints(child);
	}	}

	private static void destroy_span(HiGraph node) throws HiGraphException {
		HiArc		arc;
		HiGraph		child;
				
		for (Enumeration e = node.m_children.elements(); e.hasMoreElements(); ) {
			arc   = (HiArc) e.nextElement();
			child = arc.to();
			destroy_span(child);
		}
		node.m_span.removeAllElements();
		node.m_span = null;
		node.m_back = null;
	}

	private static void dumpspan(HiGraph node) {
		HiGraph		child;
		HiArc		arc;
		Enumeration e;

		for (int i = 0; i < node.m_rank; ++i) {
			System.out.print(" ");
		}
		
		System.out.print("[rank=" + node.m_rank +  " weight=" + node.m_weight + "]" + node.label());

		for (e = node.m_span.elements(); e.hasMoreElements(); ) {
			arc   = (HiArc) e.nextElement();
			System.out.print(" (cut=" + arc.cutValue() + ")" + arc);
		}
		System.out.print("\n");
		for (e = node.m_span.elements(); e.hasMoreElements(); ) {
			arc   = (HiArc) e.nextElement();
			child = arc.to();
			if (child == node) {
				child = arc.from();
			}
			dumpspan(child);
	}	}

	private static void dumpspantree(HiGraph node) {
		HiGraph root;
	
		System.out.println("Dumping graph because of " + node);

		for (root = node; root.m_parent != null; root = root.m_parent.from());
		root.dump();

		System.out.println("Dumping spanning tree");
		for (root = node; root.m_back != null;) {
			if (root == root.m_back.from()) {
				root = root.m_back.to();
			} else {
				root = root.m_back.from();
		}	}
		dumpspan(root);
	}
	
	/* -----------------------------------------------------------------------------------
	 * Do any initialisation for the graph 
	 *
	 * Afterwards:
	 *
	 * all nodes are marked not visited
	 * all nodes have a rank consistent with their inclusion tree order
	 *
	 * Returns number of nodes in the graph
	 */

	private static int prepareGraph(HiGraph node) {

		int			nodes = 1;
		HiArc		arc;
		HiGraph		child;
		Enumeration e;
		
		node.m_visited	= 0;
		node.m_rank     = 0;
		
		for (e = node.m_children.elements(); e.hasMoreElements(); ) {
			arc     = (HiArc) e.nextElement();
			child   = arc.to();
			nodes  += prepareGraph(child);
		}
		return(nodes);
	}

	/* For any node outside of a box which is not address by an arc flagged as onside create a
	 * dummy edge from the sink node of the box to this node outside the box.  This typically
	 * (ie when there are no cycles) forces the node outside physically under rather than to the
	 * side of the box.
	 */

	private static void placeBelow(HiGraph node) throws HiGraphException {
		Vector	arcs = node.m_children;
		int		size = arcs.size();
		HiArc	arc;
		HiGraph	other;
		int		i;

		for (i = 0; i < size; ++i) {
			arc   = (HiArc) arcs.elementAt(i);
			other = arc.to();
			placeBelow(other);
		}

		if (size == 0) {
			return;
		}

		HiArc	arc1;
		HiGraph	ancestor;

		arcs = node.m_out;
		size = arcs.size();

		for (i = 0; i < size; ++i) {
			arc   = (HiArc) arcs.elementAt(i);
			if (arc.onSide()) {
				continue;
			}
			other = arc.to();
			for (ancestor = other; ancestor != node; ancestor = arc1.from() ) {
				arc1  = ancestor.m_parent;
				if (arc1 == null) {
					break;
			}	}
				
			if (ancestor != node) {	/* Not inside the node box */
				other.newInputArc(node.m_sink);
	}	}	}

	/* Find all cycles and mark those edges that cause them as reversed == true
	 *
	 * m_visited must be 0 on input
	 *
	 * Afterwards:
	 * 
	 * m_visited becomes 2 for all nodes
	 */

	private static void fixCycles(HiGraph node) throws HiGraphException 
	{
		HiGraph		to;
		HiArc		arc;
		Vector		children, out;
		int			i;

		if (node.m_visited == 0) {

			/* No cycle yet */

			node.m_visited = 1;

			// We traverse the containment tree but don't expect to find any cycles in just this tree

			children = node.m_children;
			for (i = children.size(); i > 0;) {
				arc = (HiArc) children.elementAt(--i);
				fixCycles(arc.to());
			}
			
			out  = node.m_out;
			for (i = out.size(); i > 0; ) {
				arc = (HiArc) out.elementAt(--i);
				to  = arc.to();
				if (to.m_visited == 1) {
//					System.out.println("HiSimplex: reversing edge between " + node + " and " + to);
					HiGraph.removeArc(out, arc);	// Won't effect nodes before it in the vector (still to be visited)
					HiGraph.removeArc(to.m_in, arc);
					arc.reverse();
					to.m_out.addElement(arc);	// Adds to end	(doesn't matter that we may not visit it)
					node.m_in.addElement(arc);
				} else {
					fixCycles(to);
			}	}

			// Don't need to check for a cycle under this node.  If there was one
			// we would by now have removed it since we followed all out arcs already
			// This is done for efficiency since there may be multiple paths to this
			// node.

			node.m_visited = 2;
	}	}

/*
	private static void checkCycles0(HiGraph node)
	{
		HiArc		arc;
		Vector		children, out;
		HiGraph		to;
		int			i;


		System.out.println(node + " rank=" + node.m_rank);
		children = node.m_children;
		for (i = children.size(); i > 0;) {
			arc = (HiArc) children.elementAt(--i);
			to  = arc.to();
			System.out.println("=>" + to + " minlength=" + arc.getMinlength());
			checkCycles0(to);
		}
		out  = node.m_out;
		for (i = out.size(); i > 0; ) {
			arc = (HiArc) out.elementAt(--i);
			to  = arc.to();
			System.out.println("->" + to + " minlength=" + arc.getMinlength());
		}
	}

	private static void checkCycles1(HiGraph node, int value)
	{
		HiArc		arc;
		Vector		children;
		int			i;

		node.m_visited = value;

		children = node.m_children;
		for (i = children.size(); i > 0;) {
			arc = (HiArc) children.elementAt(--i);
			checkCycles1(arc.to(), value);
	}	}

	// This routine will loop for ever if there is any cycle

	private static void checkCycles2(HiGraph node)
	{
		HiArc		arc;
		Vector		children, out;
		HiGraph		to;
		int			i;

		if (node.m_visited == 2) {
			children = node.m_children;
			for (i = children.size(); i > 0;) {
				arc = (HiArc) children.elementAt(--i);
				checkCycles2(arc.to());
			}

			out  = node.m_out;
			for (i = out.size(); i > 0; ) {
				arc = (HiArc) out.elementAt(--i);
				to  = arc.to();
				checkCycles2(to);
			}
			node.m_visited = 0;
		}
	}

	private static void checkCycles(HiGraph node)
	{
		System.out.println("HiSimplex.checkCycles");
		checkCycles0(node);
		// Make sure every node is flagged wih m_visited = 2
		checkCycles1(node, 2);
		checkCycles2(node);
		checkCycles1(node, 2);
		System.out.println("HiSimplex.checkCycles done");

	}
*/

	/* -----------------------------------------------------------------------------------
	 * Count input arcs directed at each node so that we can cheaply detect when all input arcs
	 * have been visited by decrementing this count as each input is seen.  Must be done after
	 * any node reversals have been performed.
	 *
	 * Afterwards:
	 *
	 * all nodes have appropriate m_inputs value
	 */

	private static void countInputArcs(HiGraph node) {
		HiArc		arc;
		HiGraph		child;
		Enumeration e;

		if (node.m_span == null) {
			node.m_span = new Vector();
		}

		node.m_inputs = (node.m_parent != null ? 1 : 0) + node.m_in.size();
		for (e = node.m_children.elements(); e.hasMoreElements(); ) {
			arc    = (HiArc) e.nextElement();
			child = arc.to();
			countInputArcs(child);
	}	}

	/* Using the initial ranking imposed by containership compute a feasible ranking for this in
	 * which all (possibly reversed) edges go from a higher node to a lower node in the ranking
	 *
	 * Input:
	 *   node is a node on the spanning tree whose rank is thus fixed.. 
	 *   tail is the address of the tail of the queue
	 *
	 * Afterwards:
	 *
	 * Every node reached from such an out arc (unless its this) has its m_count decremented
	 *
	 * any nodes below this node in the ranking order (ie having no to be visited input arcs)
	 * are added via a tight spanning arc to them to the end of queue
	 *
	 * Returns the size of the queue
	 *
	 * Method:
	 *  When an arc is added into the path the rank of the source of the arc is known, and the
	 *  length of the arc is known and this is a tightest arc, meaning that the rank of the sink
	 *  is known.
	 *  Basically compute the maximum path depth from all things already in the queue to a node
	 *  and when all things with arcs to the thing are in the queue select an arc from a node in
	 *  the queue to this new node to be added into the queue so that that maximum path depth is
	 *  enforced.
	 */

	private static int feasibleRank(HiGraph node, int tail, HiArc queue[]) {
		HiArc		arc;
		HiGraph		child, other;
		Enumeration e, e1;
		int			i, rank;

		e = node.m_children.elements();
		for (i = 0; i < 2; ++i) {
			while (e.hasMoreElements() ) {
				arc   = (HiArc) e.nextElement();
				child = arc.to();
				rank  = node.m_rank + arc.getMinlength();
				if (debug && arc.getMinlength() < 0) {
					System.out.println("Min arc length " + arc.getMinlength() + "!!!");
				}
				if (rank > child.m_rank) {
					child.m_rank = rank;  // Make it the minimum of the maximum for all input arcs.
				}
				if (debug && child.m_inputs == 0) {
					System.out.println("Child input count -ve!!!");
				}
				if (--child.m_inputs == 0) {
					// Find a tight spanning arc
					// This will often be the arc we were looking at but not always..
					if (rank == child.m_rank) {
						other = node;
					} else {
						for (e1 = child.m_in.elements(); e1.hasMoreElements(); ) {
						// The previous arc was not a tight spanning arc
						// Therefore another arc is tighter, and further this node must already
						// be in the queue (ie on the spanning tree above us) since child.m_inputs = 0
						
							arc   = (HiArc) e1.nextElement();
							other = arc.from();
							rank  = other.m_rank + arc.getMinlength();
							if (rank == child.m_rank) {
								break;
						}	}
						if (rank != child.m_rank) {
//							System.out.println("Most strange");
							// Most strange but we will play it safe..
							arc = child.m_parent;
							if (debug) {
								if (arc == null) {
									System.out.println("Can't find parent while computing initial feasible rank");
							}	}
							other = arc.from();
							rank  = other.m_rank + arc.getMinlength();
							if (rank != child.m_rank) {
								System.out.println("Can't find tight arc for " + node + " " + child);
					}	}	}
					
					child.m_back = arc;
					other        = arc.from();
					other.m_span.addElement(arc);
					queue[tail++] = arc;
//					System.out.println(arc);
			}	}
			e = node.m_out.elements();
		}
		return(tail);
	}

	private static int isfeasible(HiGraph node) throws HiGraphException {	// For debugging only
		HiArc		arc;
		HiGraph		child;
		Enumeration e;
		int			i;
		int			ret = 0;
		int			slack;

		node.m_visited = 0;
					
		for (i = 0; i < 2; ++i) {
			e = (i == 0 ? node.m_children.elements() : node.m_out.elements());
	
			while (e.hasMoreElements() ) {
				arc  = (HiArc) e.nextElement();
				child = arc.to();
				slack = child.m_rank - node.m_rank - arc.getMinlength();
				if (slack < 0) {
					// dumpspantree(child);
					throw new HiGraphException("Graph no longer feasible on " + arc + " " + child.m_rank + "-" + node.m_rank + "<" + arc.getMinlength());
				} 
				ret += slack * arc.getWeight();
				if (i == 0) {
					ret += isfeasible(child);
		}	}	}
		return(ret);
	}
		
	private static void spantreevalid1(HiGraph node) throws HiGraphException {
		HiArc		arc;
		HiGraph		child, other;
		Enumeration e;

		if (node.m_visited != 0) {
			// dumpspantree(node);
			throw new HiGraphException("Spanning tree is not a tree");
		}
		node.m_visited = 1;
		for (e = node.m_span.elements(); e.hasMoreElements(); ) {
			arc = (HiArc) e.nextElement();
			child = arc.to();
			other = arc.from();
			if (child != node) {
				if (child.m_rank - other.m_rank != arc.getMinlength()) {
					// dumpspantree(child);
					throw new HiGraphException("Spanning arc " + arc + " not tight " + child.m_rank + "-" + other.m_rank + "!=" + arc.getMinlength() );
				}
			} else {
				other = child;
				child = arc.from();
				if (other.m_rank - child.m_rank != arc.getMinlength()) {
					// dumpspantree(child);
					throw new HiGraphException("Spanning arc " + arc + " not tight " + other.m_rank + "-" + child.m_rank + "!=" + arc.getMinlength() );
				}
			}
			if (child.m_postorder >= node.m_postorder || child.m_postorder < node.m_minbeneath) {
				throw new HiGraphException("Post order wrong");
			}
			if (other != node) {
				throw new HiGraphException("Spanning arc broken");
			}
			spantreevalid1(child);
	}	}

	private static void spantreevalid2(HiGraph node) throws HiGraphException {
		HiArc		arc;
		HiGraph		child;
		Enumeration e;

		if (node.m_visited == 0) {
			// dumpspantree(node);
			throw new HiGraphException("Spanning tree never connected " + node.label());
		}
		node.m_visited = 0;
		for (e = node.m_children.elements(); e.hasMoreElements(); ) {
			arc = (HiArc) e.nextElement();
			child = arc.to();
			spantreevalid2(child);
	}	}

	private static void isvalid(HiGraph root) throws HiGraphException {
		HiGraph spanroot;
		int		slack;

		slack = isfeasible(root);
		for (spanroot = root; spanroot.m_back != null; spanroot = spanroot.m_back.from());
		spantreevalid1(spanroot);
		spantreevalid2(root);
			
//		System.out.println("Total slack now " + slack);
	}

	/*
	 * Compute the total weight of all input arcs minus all output arcs.
	 * Must be done after any arc reversals have been performed.
	 */

	private static void computeWeights(HiGraph node) {
	
		HiArc		arc;
		HiGraph		child;
		Enumeration e;
		int			weight = 0;

		if (debug && node.m_inputs != 0) {
			System.out.println("Didn't see all inputs!!!");
		}
		arc = node.m_parent;
		if (arc != null) {
			weight = arc.getWeight();				// Input arc so +ve
		}

		for (e = node.m_children.elements(); e.hasMoreElements(); ) {
			arc     = (HiArc) e.nextElement();
			weight -= arc.getWeight();				// Output arc weights are -ve
			child   = arc.to();
			computeWeights(child);
		}

		for (e = node.m_in.elements(); e.hasMoreElements(); ) {
			arc     = (HiArc) e.nextElement();
			weight += arc.getWeight();				// Input arc weights are +ve
			if (!arc.from().m_out.contains(arc)) {
				System.out.println("Input arc " + arc + " broken when computing weights");
			}
		}

		for (e = node.m_out.elements(); e.hasMoreElements(); ) {
			arc     = (HiArc) e.nextElement();
			weight -= arc.getWeight();				// Output arc weights are -ve
			if (!arc.to().m_in.contains(arc)) {
				System.out.println("Output arc " + arc + " broken when computing weights");
			}
		}
		
		node.m_weight = weight;
	}

	/* 
	 * Afterwards:
	 * Assigns a minbeneath and post order traversal value to every node in the graph rooted at this
	 * based on its spanning tree. The input min value is the start value for the lowest postorder number
	 * under this node.
	 *
	 * Also update the ranking for everything below the root which is assumed to be both known and
	 * the basis on which to compute other subsequent rankings.
	 *
	 * Cut values as described in the paper are just plain too complex.  The goal of the cut value in
	 * essence is to give a measure of the weighted cost of raising / lowering the head of the node in
	 * the spanning tree by one rank.. If the spanning arc agrees with the direction of the graph arc
	 * then the cut value is simply the weighted total of all arc into the component being moved minus
	 * the weighted total of all output arcs from this component ( This is because we are considering
	 * the merit of moving the component down and will whenever output weights > input weights).  If
	 * the spanning arc does not agree with the graph then we can only move the component up, and
	 * therefore want to reverse the sign of this computation.
	 *
	 * Forget reversing the sign during the computation of the cut value.. Instead reverse the sign within
	 * the algorithm whenever the cut arc disagrees with the graph arc direction.
	 *
	 * Then cut value is simply the sum of all cut values below me in the spanning tree (leaves having cut
	 * value = total weight of inputs - outputs) + (total weight of my inputs - my outputs).
	 *
	 * Justification:  The sum of the weighted edges into/out of the spanning tree rooted at X having
	 * children Y1..Yn is the sum of edges into out of each of X, Y1,.. Yn ignoring all edges which
	 * have both ends addressing nodes in the set (X, nodes in Y1.. nodes in Yn).  But if an edge has
	 * both ends in this set then either it is internal to some Yi (in which case it is ignored in
	 * Yi's cut value) or it goes between two of the set of (X,Y1..Yn).  In this case it is an input
	 * to one and an output from the other and its weight within the sum cancels.  
	 *
	 * While the paper suggests that cut values must be computed from the leafs up, using the above
	 * strategy things are simpler.. If the above justification is valid, any set of nodes has a
	 * cut value equal to the sum of its output - input weights.
	 *
	 * Afterwards:
	 * Has set the cutvalue of all edges in the spanning tree below this node
	 */

	private static void postorder(HiGraph node, int min) {
		HiArc		arc;
		HiGraph		down = null;
		int			max  = min;
		int			minlength;
		int			cut  = 0;
	
		node.m_minbeneath = min;
		
		for (Enumeration e = node.m_span.elements(); e.hasMoreElements(); ) {
			arc = (HiArc) e.nextElement();
			down = arc.to();
			minlength = arc.getMinlength();
			if (down != node) {						// Spanning direction agrees with graph edge
				down.m_rank = node.m_rank + minlength;
			} else {
				down = arc.from();
				down.m_rank = node.m_rank - minlength;	
			}		
			postorder(down, max);
			cut   += arc.cutValue();				// Sum of the cut values below
			max    = down.m_postorder + 1;
		}
		node.m_postorder = max;
		cut += node.m_weight;
		arc  = node.m_back;
		if (arc != null) {
			arc.cutValue(cut);
	}	}

	/* Returns the replacement arc for use in the simplex algorithm or null if none
	 * Note that we need not look for such an arc in the path[0].from the root of the
	 * spanning tree since (a) this node has no input arcs and (b) this arc can never
	 * be replaced since it doesn't have a negative cut value, since it has no input arcs.
	 */

	private static HiArc alternativeEdge(HiArc old, int tail, HiArc path[], int replace) throws HiGraphException {
		
		
		HiGraph		node, parent;
		HiArc		arc;
		int			head, i, slack;
		Enumeration e;
		boolean		shift_down = true;
		boolean		node_under_root;
		HiArc		ret   = null;
		HiGraph		root  = old.to();
		HiGraph		child = old.from();
		int			maxslack = Integer.MAX_VALUE;	// Have to select suitable arc with minimal slack
				
		if (root.m_postorder > child.m_postorder) {
			root       = child;
			shift_down = false;
		}

		/* Root is the root of head component of the disconnected spanning tree when old is deleted */

		int minbeneath = root.m_minbeneath;
		int postorder  = root.m_postorder;
		int start      = replace;
		head           = start;

		do {
			arc  = path[head];					// Following spanning edges to their head node so that
			node = arc.to();					// all nodes but the root are visited exactly once
			if (node.m_back != arc) {
				node = arc.from();
			}
			node_under_root = (node.m_postorder >= minbeneath && node.m_postorder <= postorder);

			/* Only interested in nodes in the spanning subtree rooted at root when shifting up.
			 * Only interested in nodes not in spanning subtree rooted at root when shifting down
			 * In both cases the only arcs which we can pivot on must therefore (with respect to
			 * node) be input arcs.
			 */

			if (node_under_root ^ shift_down) {

				for (e = node.m_in.elements(); e.hasMoreElements(); ) {
					arc    = (HiArc) e.nextElement();
					if (arc != old) {
						parent = arc.from();
						if ((parent.m_postorder >= minbeneath && parent.m_postorder <= postorder) ^ node_under_root) {
							slack = node.m_rank - parent.m_rank - arc.getMinlength();
							if (slack < maxslack) {
								maxslack = slack;
								ret      = arc;
				}	}	}	}

				arc = (HiArc) node.m_parent;
				if (arc == null) {
					throw new HiGraphException("AlternativeNodeEdge examining root?");
				}

				if (arc != old) {
					parent = arc.from();
					if ((parent.m_postorder >= minbeneath && parent.m_postorder <= postorder) ^ node_under_root) {
						slack = node.m_rank - parent.m_rank - arc.getMinlength();
						// Must be >= 0 since already feasible solution
						if (slack < maxslack ) {
							maxslack = slack;
							ret      = arc;
				}	}	}
			}
			++head;
			if (head == tail) {
				head = 0;
			}
		} while (head != start);

		return(ret);
	}

	private static void updateSpanningTree(HiArc old, HiArc alternative) throws HiGraphException {
		HiGraph oldtail, oldhead, node;
		
		// System.out.println(old + " replaced in spanning tree by " + alternative);

		oldhead = old.from();
		oldtail = old.to();

		if (oldhead.m_postorder > oldtail.m_postorder) {
			node    = oldhead;;
			oldhead = oldtail;
			oldtail = node;
		}

		/* oldhead is the node addressed by the head (ie oldtail -old-> oldheadX).  Therefore oldhead
		 * has m_back pointer in it which must be removed.  We know this because it is beneath the
		 * other node according to the preorder traversal.
		 */
		
		HiGraph newtail, newhead;

		/* The new head should be in the component within the spanning tree formerly under oldhead,
		 * while the newtail should be in the component not under oldhead.
		 */

		newhead = alternative.from();
		newtail = alternative.to();

		if (newhead.m_postorder < oldhead.m_minbeneath || newhead.m_postorder > oldhead.m_postorder) {
			node    = newhead;
			newhead = newtail;
			newtail = node;
		}

		if (newhead.m_postorder > oldhead.m_postorder || newhead.m_postorder < oldhead.m_minbeneath) {
			// dumpspantree(oldhead);
			throw new HiGraphException("Alternative head " + newhead + " in " + alternative + " doesn't address disconnected component " + oldhead + " in " + old);
		}

		if (newtail.m_postorder <= oldhead.m_postorder && newtail.m_postorder >= oldhead.m_minbeneath) {
			// dumpspantree(oldhead);
			throw new HiGraphException("Alternative tail " + newtail + " in " + alternative + " doesn't address connected component " + oldhead + " in " + old);
		}

		/* newhead is the node to have the new m_back arc in it addressing newtail.   Every m_back from
		 * newhead back to that addressing oldhead must be reversed to preserve the spanning tree, as a
		 * directed spanning tree. 
		 */

		HiGraph next, previous;
		HiArc	back, newback;

		
		newback  = alternative;
		previous = newtail;
		for (node = newhead; node != oldtail; node = next) {
			back        = node.m_back;
			if (back == null) {
				System.out.println("Back null!!!");
			}
			node.m_back = newback;		// Point back up the spanning tree
			previous.m_span.addElement(newback);
			next = back.from();
			if (next == node) {
				next = back.to();
			}
			HiGraph.removeArc(next.m_span, back);
			previous = node;
			newback  = back;
		}
		
		int min, max, temp;

		min = newhead.m_postorder;
		max = newtail.m_postorder;
		if (min > max) {
			temp = min;
			min  = max;
			max  = temp;
		}

		for (node = oldtail; node.m_minbeneath > min || node.m_postorder < max; node = next) {
			back = node.m_back;
			next = back.to();
			if (next == node) {
				next = back.from();
		}	}

		/* node is now the root of the spanning tree subsumming all changes
		 * recompute the cutValues, post-order values and ranks beneath node
		 */

		postorder(node, node.m_minbeneath);
		//dumpspantree(node);
	}

	/* use the network simplex algorithm to minimise edge lengths between ranks subject to
	 * preset minlength and weights on arcs. If pushdown is true we will consider placing
	 * nodes under nodes whenever the arc is not onSide.
	 */

	static void simplex(HiGraph root, int maxcycles) throws HiGraphException {
		int			nodes;
		HiArc		arc;
		Enumeration e;

//		System.out.println("HiSimplex started");
		/* Count nodes in graph and initialise nodes/edges etc. */

		nodes = prepareGraph(root);

		HiArc	path[]  = new HiArc[nodes];

		/* Add arcs forcing edges beneath sink nodes when edges are not onside() */

		if (root.m_sink != null) {
			placeBelow(root);
		}

		/* Convert the graph to an acyclic directed graph */

//		System.out.println("HiSimplex: find cycles");
		fixCycles(root);

//		checkCycles(root);

		/* Count the number of non-trivial input arcs into each node using any reversals */

//		System.out.println("HiSimplex: counting input arcs");
		countInputArcs(root);

//		System.out.println("HiSimplex: computing feasible ranking");

		/* Compute a feasible ranking in which every edge (ignoring reversals) is downwards */

		int		head = 0;
		int		tail;
		HiGraph node;
		
		root.m_rank = 0;
		for (tail = feasibleRank(root, 0, path); head < tail; ++head) {
			node = path[head].to();
			tail = feasibleRank(node, tail, path);
		} 

		/* Path[x] 0<=x<tail is the set of arcs that form the feasible spanning tree. This path is
		 * empty (ie tail = 0) if the root has no arcs.  The root of the spanning tree is also the
		 * root of the inclusion tree and has rank 0.
		 *	 
		 * Compute the cutvalue of every arc in the initial feasible spanning tree, postorder numbers
		 * and rank
		 */

		computeWeights(root);
		postorder(root, 1);

		if (debug) {
			// System.out.println("Feasible solution");
			// for (head = 0; head < tail; ++head) {
			//	System.out.println(path[head] + ": " + path[head].to().m_rank + " - " + path[head].from().m_rank + " >= " + path[head].getMinlength());
			// }

			System.out.println("Validating feasible solution");
			isvalid(root);
		}

		/* Repeatedly apply the simplex network improvement */

		head = 0;

		int		count	  = maxcycles;
		HiArc	alternative;
		int		replace = 0;
		int		start;
		int		worst;
		int		cutvalue;
			
		/* Picking the minimum negative cut is supposed to improve performance */

		if (tail != 0) {
			for (count = maxcycles;count != 0;--count) {
//				System.out.println("HiSimplex: Iteration " + (maxcycles - count) + " of " + maxcycles);
				// dumpspan(root);
				start   = replace;
				head    = start;
				worst   = 0;
				do {
					++head;
					if (head == tail) {
						head = 0;
					}
					arc      = path[head];
					cutvalue = arc.cutValue();
					node     = arc.to();
					if (arc != node.m_back) {
						cutvalue = 0 - cutvalue;
					}
					if (cutvalue < worst) {
						worst       = cutvalue;
						replace     = head;
					}
				} while (head != start);
				if (worst == 0) {
					break;
				}
				
				alternative = alternativeEdge(path[replace], tail, path, replace);
				if (alternative == null) {
					// dumpspantree(root);
					throw new HiGraphException("Simplex operation not yet optimal but no improvement found");
				}
				updateSpanningTree(path[replace], alternative);
				path[replace] = alternative;
				if (debug) {
					System.out.println("Validating iteration " + (maxcycles - count));
					isvalid(root);
			}	}
		
			if (count == 0) {	// A safety check in the event of degenerate cycling occurring..
				System.out.println("Unable to optimize ranking using " + maxcycles + " simplex improvements on a graph of " + nodes + " nodes");
			} 
			// System.out.println("Optimal ranking computed after " + (maxcycles - count) + " iterations");
						
			for (head = 0; head < tail; ++head) {
				arc = path[head];
				path[head]  = null;
			}
		}
		destroy_span(root);
		path = null;
//		System.out.println("HiSimplex finished");
	}
}
