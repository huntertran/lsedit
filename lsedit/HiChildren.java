package lsedit;
import java.util.Vector;
import java.util.Enumeration;

/**
 * Developed by: Ian Davis in Summer 1997 for Grant Weddell
 * Provides a static method to associate an ordering with child nodes suitable for subsequent
 * display.  At the core of this logic an attempt is made to solve the following problem:
 *
 * Given a set of child nodes of some parent node, and edges between these child nodes,
 * as well as edges off to the right and edges off to the left of all children, find an ordering
 * of children which minimises edge crossings within this set of known edges.  Note that for
 * any possible edge zero or more instances of that edge may occur, with each being treated
 * as a separate edge.
 *
 * Assumes:
 * m_depth is valid
 *
 * Strategy:
 *
 * Perform a breadth first traversal of the inclusion tree reordering all of the children under each
 * node as visited using the knowledge about the gross ordering so far established.  Specifically:
 *
 * Count the number of edges which connect separate subtrees at every level of branching within
 * the inclusion tree and store these counts in the matrix.  Note that the matrix is triangular
 * since we are not concerned about the direction of these edges, but logically it should be
 * considered to be symmetric.
 *
 * For each level of branching within the inclusion tree try to place children which have lots of
 * edges which connect them close together.  Do this by attempting to minimise the total length
 * of these edges in the horizontal plane, based on the ordering of the children.
 *
 * The total length is the total of the edges between children * their distance apart. ie:
 *   sum (i = 0..size-2)(j = i+1..size-1) matrix(j.id,i.id)*(j-i)
 *
 * The reduction in total length when we interchange any two children x, and y originally at position
 * i and j respectively is:
 *
 * sum (m=0..size-1,m!=i,j) matrix(m.id,x.id)[|i-m| - |j-m|] + matrix(m.id,y.id)[|j-m| - |i-m|] =
 * sum (m=0..size-1,m!=i,j) matrix(m.id,x.id)[|i-m] - |j-m|] - matrix(m.id,y.id)[|i-m| - |j-m|] =
 * sum (m=0..size-1,m!=i,j) (matrix(m.id,x.id)-matrix(m.id,y.id))[|i-m] - |j-m|]
 *
 * This is the value we wish to maximise at each iteration.  Termination occurs when the maximum value
 * of this formula for all i,j is not positive (ie. we can not reduce the cost).  We won't flip if the
 * reduction is zero because we would rather not alter the input graph if it is for example a tree, and
 * it becomes a little harder to decide when to terminate (need some cycle checking on 0 reductions).
 *
 * This approach is refined by keeping track also of the edges coming into the tree being sorted from
 * the left and the right, and to which child tree they connect with.  We consider all left edges to
 * be of just outside to the left of the children and likewise for right edges.  Since we are only
 * interested in the difference we can treat each left input edge as -1 and each right input edges as +1.
 *
 * We only have to recompute which direction things outside of us are coming at us from once per level
 * if we perform a breadth first traversal.  This is a huge saving over computing it for each node
 * separately.  Note also that our computation is a function of the nodes in the graph.. not the
 * edges in the graph, since the matrix can be very effectively used to compute outside edges being

 * Rewritten from scratch: November 2000
 *
 * Code very slow needed improving.
 *
 * Changes:
 *
 * (1) Rather than compute a huge sparse matrix for how every node has direct or internal indirect arcs to every other node,
 *     this has been greatly simplified by having a matrix for each level of nodes in the inclusion tree.
 *     We are only interested in the values in this inclusion tree for nodes at the same depth.
 *
 * (2) Greatly simplified the code.  Attempted to do a better job of computing what the arc counts were, and to do a much
 *     simpler reordering step.
 *
 * Historically the reordering step considered reversing the order of all pairs of children.  Now instead it simply considers
 * reversing adjacent children.  After e

 * associated with nodes being sorted.
 */

class HiChildren {
	
	static final boolean debug = false;
	
	/* Places dummy nodes on every arc having some slack (ie difference in rank between in and out
	 * node > 1).  These dummy nodes are included within the closest parent including both ends of
	 * the original arc.  The original arc is preserved as the final arc in the chain.. ie. The one
	 * arc which goes from a dummy node to a real node.  This must be done before we reorder children
	 * since dummy nodes must be treated as legitimate children.
	 */

	private static void addDummyNodes(HiGraph node) throws HiGraphException {
		HiArc		arc;
		HiGraph		child;
		Enumeration	e;
	
		node.m_visited = 0;		// For safety 
		
		for (e = node.m_children.elements(); e.hasMoreElements(); ) {
			arc   = (HiArc) e.nextElement();
			child = arc.to();
			addDummyNodes(child);
		}

		HiGraph		to;
		int			i, slack, minslack;
		HiGraph		parent;
		int			nodes;
				
		if (node.m_parent == null) {
			parent = null;
		} else {
			parent = node.m_parent.from();
		}

		nodes = node.m_out.size();
		for (i = 0; i < nodes; ++i) {
			arc   = (HiArc) node.m_out.elementAt(i);
			to    = arc.to();
			if (to.m_parent == null || to.m_parent.from() != parent) {
				continue;
			}
			slack = to.m_rank - node.m_rank - 1;
		
			if (slack > 0) {
				
				/* If node is below bottom of box and need not be onside we can dispense with all the
				 * dummy arcs from the top of the box.. instead create dummy arcs from the bottom of
				 * the box.
				 */
				minslack = node.m_sink_rank - node.m_rank;
				if ((slack < (minslack-1)) || arc.onSide() ) {
					minslack = 0;
				} 
//				System.out.println(arc + " has slack=" + slack + " minslack=" + minslack);
				
				/* N.B. By ensuring that the final arc remains attached to its original to node we
				 * avoid having to correct the in arc in this to node
				 */
				boolean	reversed     = arc.reversed();
				HiGraph	dummy        = null;

				for (; slack > minslack; --slack) {
					dummy = parent.newChild();	/* New dummy node */
					dummy.m_rank = node.m_rank + slack;
					dummy.sinkrank(dummy.m_rank);
					dummy.newOutputArc(arc);			/* Move the arc to originate in dummy			*/
					arc = new HiArc(node,dummy);		/* Create a new arc to span the remaining space */
					arc.reverse(reversed);
					dummy.newInputArc(arc);				/* Make sure it addresses the dummy node		*/
				}
				node.m_out.setElementAt(arc, i);		/* Replace the old arc with the new fragment	*/
		}	}
		return;
	}

	private static void postorderlevel(HiGraph node) {
		HiArc	arc;
		HiGraph	child;
		int		min = node.m_minbeneath;
		int		beneath;

		for (Enumeration e = node.m_children.elements(); e.hasMoreElements(); ) {
			arc     = (HiArc) e.nextElement();
			child   = arc.to();
			beneath = child.m_postorder - child.m_minbeneath;
			child.m_minbeneath = min;
			child.m_postorder  = min + beneath;
			min               += beneath + 1;
	}	}

	private static int	postorder(HiGraph node, int min /* Preorder number of this node */) {
		HiArc		arc;
		HiGraph		child;
		int			max  = min;
		
		node.m_minbeneath = min;		
		for (Enumeration e = node.m_children.elements(); e.hasMoreElements(); ) {
			arc   = (HiArc) e.nextElement();
			child = arc.to();
			max   = postorder(child, max);
		}
		node.m_postorder = max;
		return(max+1);
	}
	
	/* Count the edges between distinct nodes at or under the same depth nodes.  Ie. an edge
	 * between X and Y is counted as an edge between X and Y  X parent and Y parent (if different)
	 * etc. 
	 */
	
	private static void fillMatrix(HiGraph queue[], int matrices[][][]) {
		Enumeration e;
		HiArc		arc;
		HiGraph		node1, node2;
		int			matrix[][];
		int			id1, id2, temp, depth;

		// For every node in the graph..
		// count edges between things at or under a common level in distinct subtrees

		for (int i = queue.length - 1; i >= 0; --i) {
			node1 = queue[i];
			for (e = node1.m_in.elements(); e.hasMoreElements(); ) {
				node1   = queue[i];
				arc     = (HiArc) e.nextElement();
				node2   = arc.from();

				for (;;) {
					depth = node1.m_depth;
					if (depth == node2.m_depth) {
						// Nodes are now at same depth

						id1 = node1.m_position;
						id2 = node2.m_position;

						if (id1 == id2) {
							break;
						}
						/* Count as an edge between the subtree rooted at id2 and the subtree rooted at id1 */
						/* Exchange if necessary since matrix triangular */

						matrix = matrices[depth];
						if (id2 < id1) {
							// Experimental .. make crossings between large structures much more expensive
							matrix[id1][id2] += 1; 
						} else {
							matrix[id2][id1] += 1;
						}
						node1 = node1.m_parent.from();
						node2 = node2.m_parent.from();
						continue;
					}
					// We are only interested in counts at the same level so no point in incrementing them for
					// relationships between nodes at different levels
					if (node2.m_depth > depth) {
						node2 = node2.m_parent.from();
					} else {
						node1 = node1.m_parent.from();
	}	}	}	}	}


	private static void dumpMatrix(HiGraph queue[], int matrices[][][]) 
	{
		HiGraph		node;
		int			row[];
		int			i, j, k, last, last_depth;
		int			matrix[][];

		System.out.println("\nAdjacency matrix\n");
		for (i = 0; i < matrices.length; ++i) {
			System.out.println("At depth " + i);
			matrix = matrices[i];
			for (j = 0; j < matrix.length; ++j) {
				row = matrix[j];
				for (k = 0; k < row.length; ++k) {
					System.out.print("\t" + row[k]);
				}
				System.out.print("\n");
	}	}	}	

	/* Create the structure used to hold the total collection of counts of edge destinations
	 * Top level is indexed by depth.
	 * Next two levels index by position within depth as a lower triangular matrix (ie longer and longer rows)
	 */

	private static int[][][] buildMatrices(HiGraph queue[])  
	{
		HiGraph	node, next;
		
		int	matrix[][];
		int	i, j, last_depth, start, last, width, nodes;

		nodes = queue.length;
		last  = nodes - 1;;
		node  = queue[last];

		int	matrices[][][] = new int[node.m_depth+1][][];

		start      = 0;
		node       = queue[0];
		last_depth = node.m_depth;
		for (i = 1; i < nodes; ++i) {
			node = queue[i];
			if (node.m_depth != last_depth) {
				width = i - start;
				matrices[last_depth] = matrix = new int[width][];

				for (j = 0; j < width; ++j) {
					matrix[j] = new int[j+1];
				}
				start      = i;
				last_depth = node.m_depth;
		}	}
		width = i - start;
		matrices[last_depth] = matrix = new int[width][];
		for (j = 0; j < width; ++j) {
			matrix[j] = new int[j+1];
		}
		return(matrices);
	}

	/* For each of the children under queue[head] examine all of the nodes at the child depth
	 * to determine the number of arcs to the right and to the left of the individual children
	 * occurring under queue[head].  An arc is counted as +ve if to the right else -ve.  Arcs
	 * between siblings are not counted as being outside.
	 */

	private static void computeOutside(HiGraph queue[], int head, int matrices[][][]) {
		
		HiGraph	node, node1, child, child1, parent, parent1;
		Vector	children;
		HiArc	arc;
		int		matrix[][];
		int		start_level, end_level;
		int		position, position1, outside, lth;
		int		i, j, tail, edges, depth;
		Enumeration	e;

		node = queue[head];
		children = node.m_children;
		if (children.size() == 0) {
			return;
		}
		node  = queue[head];
		depth = node.m_depth;
		lth   = queue.length;

		for (start_level = head+1; start_level < lth; ++start_level) {
			node1 = queue[start_level];
			if (node1.m_depth != depth) {	// Ie first node at level one greater than node
				depth = node1.m_depth;
				break;
		}	}
		for (end_level = start_level; end_level < lth; ++end_level) {
			node1 = queue[end_level];
			if (node1.m_depth != depth) {	// Ie first node two levels below level of node
				break;
		}	}
		// All nodes at same level of children are referenced by
		// queue[start_level] through queue[end_level-1]

		matrix = matrices[depth];
		
		for (e = children.elements(); e.hasMoreElements(); ) {
			arc      = (HiArc) e.nextElement();
			child    = arc.to();
			position = child.m_position;
			outside  = 0;

			for (i = start_level; i < end_level; ++i) {
				child1  = queue[i];		// For each child at the same level as child
				parent1 = child1.m_parent.from();
				if (parent1 == node) {
					// This node is a sibling..
					continue;
				}
				position1 = child1.m_position;
				if (position1 > position) {
					edges = matrix[position1][position];
				} else {
					edges = matrix[position][position1];
				}
				if (parent1.m_postorder > node.m_postorder) {
					// This child is to our right
					outside += edges;
				} else {
					// This child is to our left
					outside -= edges;
			}	}
			child.m_outside = outside;
	}	}

	/* Reorder the children of the node to try and minimise the distance of edges 
	 * The general problem of minimising distances between edges is a special case
	 * of the Quadratic Assignment Problem known to be NP-Hard.
	 *
	 * Our strategy is a very quick and dirty one.
	 * (1) Place that node on the left which has the most negative value for
	 *     m_outside + sum all edges not to it.
	 * (2) Count all edges to this placed node as -ve 
	 *     (ie to the left of nodes yet to be places)
	 * (3) Repeat while not all nodes placed
	 *
	 * ie.  Bias towards things having edges to the outside left when at left, most edges in the
	 *      middle, and edges to the right forcing things to the right
	 */

	private static boolean reorder(HiGraph node, int matrices[][][], int outside_bias) 
	{
		Vector	children = node.m_children;
		int		size     = children.size();
		int		matrix[][];
		int		row[];
		HiArc	arc1, arc2;
		HiGraph child1, child2;
		int		i, j;
		int		position1, position2, val;
		int		best, min1, min2;
		boolean	ret;

		/* Don't worry about this phase if all the children are leaf nodes in the inclusion tree..
		 * The more sophisticated row ordering algorithm will resolve the ordering.  We only need
		 * to select an order for boxes ahead of time.
		 */

		ret = false;
		for (i = 0; i < size; ++i) {
			arc1   = (HiArc) children.elementAt(i);
			child1 = arc1.to();
			if (child1.m_children.size() != 0) {
				ret = true;
				break;
		}	}

		if (!ret) {
			return(ret);
		}
	
		ret = false;
		if (node.dontReorder()) {
			return(ret);
		}

		// Obtain the matrix for the appropriate depth
		matrix = matrices[node.m_depth+1];

		// Compute initial weights for all nodes in the row
		// This is sum of edges to other nodes - edges to left

		for (i = 0; i < size; ++i) {
			arc1           = (HiArc) children.elementAt(i);
			child1         = arc1.to();
			position1      = child1.m_position;
			row            = matrix[position1];
			val            = child1.m_outside * outside_bias;
			for (j = 0; j < position1; ++j) {
				val += row[j];	
			}
			row[position1] = val;
		}

		for (i = 0; i < size; ++i) {
			arc1      = (HiArc) children.elementAt(i);
			child1    = arc1.to();
			position1 = child1.m_position;
			best      = i;
			min1      = matrix[position1][position1];
			for (j = i+1; j < size; ++j) {
				arc2      = (HiArc) children.elementAt(j);
				child2    = arc2.to();
				position2 = child2.m_position;
				min2      = matrix[position2][position2];
				if (min2 < min1) {
					best = j;
					min1 = min2;
			}	}
			arc2      = (HiArc) children.elementAt(best);
			child2    = arc2.to();
			if (best != i) {
				// switch position1 with best
				// Reverse child1 and child2
				children.setElementAt(arc2, i);
				children.setElementAt(arc1, best);
				ret = true;
			}
			position1 = child2.m_position;

			// position1 is now going to be to the left of all nodes considered
			// change its edge cost to -ve by subtracting twice its cost
			// from all subsequent nodes
			for (j = i+1; j < size; ++j) {
				arc2            = (HiArc) children.elementAt(j);
				child2          = arc1.to();
				position2       = child2.m_position;
				row             = matrix[position2];
				if (position1 < position2) {
					val = row[position1];
				} else {
					val = matrix[position1][position2];
				}
				row[position2] -= (val << 1);
		}	}

		return(ret);
	}

	static void order(HiGraph root, int outside_bias) throws HiGraphException 
	{
		addDummyNodes(root);

		int		nodes   = postorder(root, 1) - 1;	// Compute the number of nodes within each inclusion
		HiGraph queue[] = new HiGraph[nodes];
		int		head    = 0;
		int		tail    = 0;
		Vector	children;
		HiArc	arc;
		HiGraph	node;
		int		i, size, last_depth, position;
	
		/* Create a breadth first traversal ordering in an queue 
		 * Assign each node a unique consecutive index for referencing its row/column in matrix
		 */

		queue[tail] = root;
		position        = 0;
		last_depth      = root.m_depth;
		root.m_position = 0;
		for (++tail; head < tail; ++head) {
			node            = queue[head];
			if (node.m_depth != last_depth) {
				last_depth = node.m_depth;
				position   = 0;
			}
			node.m_position = position++;		// Position within nodes at this depth
			children        = node.m_children;
			size            = children.size();
			for (i = 0; i < size; ++i) {
				arc = (HiArc) children.elementAt(i);
				queue[tail++] = arc.to();
		}	}

		/* Create an nodes*nodes triangular matrix */

		int matrices[][][]  = buildMatrices(queue);

		fillMatrix(queue, matrices);

		/*
		if (debug) {
			dumpMatrix(queue, matrices);
		}
		*/

		for (head = 0; head < tail; ++head) {
			// System.out.println("Compute outside");
			computeOutside(queue, head, matrices);
			node = queue[head];
			if (reorder(node, matrices, outside_bias)) {
				/* Adjust postorder information for sorted children */
				postorderlevel(node);
		}	}
		// System.out.println("Done children");
	}
}