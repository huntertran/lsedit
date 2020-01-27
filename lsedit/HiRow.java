package lsedit;
import java.util.Vector;
import java.util.Enumeration;

/**
 * Developed by: Ian Davis in Summer 1997 for Grant Weddell
 * Provides a static method to associate an ordering of nodes at the same rank.. In chess parlance
 * on the same row.. thus the name.  This code is conscious of the fact that any attempt to reorder
 * nodes on a rank must be careful not to violate the orderings enforced on a global higraph order
 * imposed by HiChildren (which is used to determine whether nodes not in our subtree are to our
 * left or right.
 *
 * N.B.  The problem of optimising the ordering of nodes on two rows so that edge crossings are
 * minimized is NP-complete.  Therefore this is a rattle and shake algorithm, which attempts to
 * reduce edge crossings by placing nodes at the median position within the nodes which then 
 * address/ are addressed by.
 */

 
/* N.B. We assume below that all edges are directed strictly downwards according to rank one level
 * at a time.  
 */

class HiRow {
	
	/* Create the structure used to hold the total collection of HiGraph nodes on the board
	 * We assume that the minimal rank or all children is node.m_rank that no child node has
	 * a rank > node.m_sink_rank
	 */

	static HiGraph[][] buildBoard(HiGraph node)  {

		int			count[]  = new int[node.m_sink_rank-node.m_rank + 1];
		int			ranks[]  = new int[node.m_sink_rank-node.m_rank + 1];
		Vector		children = node.m_children;
		int			size     = children.size();
		int			i, j;
		HiArc		arc;
		HiGraph		child;
		boolean		planar = true;
		
		/* Count the children at each possible rank */

		for (i = 0; i < size; ++i) {
			arc   = (HiArc) children.elementAt(i);
			child = arc.to();
			// Ranks are with respect to parent rank
			++count[child.m_rank-node.m_rank];
			child.m_position = 0;
		}

		/* Compute how many things that are children of node point to me on my rank 
		 * We can't assume that the first thing seen at the lowest rank has nothing
		 * earlier than it (ie pointing at it from the same rank)
		 */

		Vector	outArcs;
		HiGraph	dst, other;


		for (i = 0; i < size; ++i) {
			arc     = (HiArc) children.elementAt(i);
			child   = arc.to();
			if (child.m_object == null) {
				continue;
			}
			outArcs = child.m_out;
			for (j = outArcs.size(); j > 0; ) {
				arc = (HiArc) outArcs.elementAt(--j);
				dst = arc.to();
				if (dst.m_object == null) {
					continue;
				}
				if (dst.m_rank != child.m_rank) {
					continue;
				}
				arc = dst.m_parent;
				if (arc == null || arc.from() != node) {
					continue;
				}
				++dst.m_position;
		}	}


		/* Reorder nodes as an arbitrary breath first traversal
		 * so that if the graph is planar no edges cross, when
		 * the reordered nodes are subsequently inserted into the board
		 */

		HiArc	bestArc, firstArc;
		HiGraph	best;
		int		at, tail, arcs;

		for (i = 0; i < size; ) {

			// Find the node having the smallest remaining rank

			bestArc = firstArc = (HiArc) children.elementAt(i);
			best    = bestArc.to();
			at      = i;

			for (j = i+1; j < size; ++j) {
				arc   = (HiArc) children.elementAt(j);
				dst   = arc.to();

				if (best.m_object != null) {
					// Give low priority to HiGraphs not citing an object
					if (dst.m_object == null) {
						continue;
					}
					if (dst.m_rank > best.m_rank) {
						continue;
					}
					if (dst.m_rank == best.m_rank) {
						if (dst.m_position >= best.m_position) {
							continue;
				}	}	}
				bestArc = arc;
				best    = dst;
				at      = j;
			}
			
			if (at != i) {
				children.set(at, firstArc);
				children.set(i,  bestArc);
			}

			if (best.m_object == null) {
				// HiGraphs not citing an object are simply for control
				++i;
				continue;
			}

			// Find everything under this node and move it immediately after this node

			for (tail = i; i <= tail; ++i) {
				arc     = (HiArc) children.elementAt(i);
				child   = arc.to();
				outArcs = child.m_out;	
				arcs    = outArcs.size();		
				for (j = 0; j < arcs; ++j) {
					arc     = (HiArc) outArcs.elementAt(j);
					dst     = arc.to();
					arc     = dst.m_parent;
					if (arc == null || arc.from() != node || dst.m_object == null) {
						continue;
					}
					// Dst is in this board
					for (at = tail+1; at < size; ++at ) {
						bestArc  = (HiArc) children.elementAt(at);
						other    = bestArc.to();
						if (other == dst) {
							break;
					}	}
					if (at == size) {
						// Might already have been sorted
						planar = false;
						continue;
					}
					
					++tail;
					if (tail != at) {
						arc   = (HiArc) children.elementAt(tail);
						children.set(at,   arc);
						children.set(tail, bestArc);
			}	}	}
		}			


		/* Compute the real board ranks ignoring empty ranks */
	
		for (i = j = 0; i < count.length; ++i) {
			ranks[i] = j;
			if (count[i] != 0) ++j;
		}

		/* Build the board of the correct shape */

		HiGraph	board[][] = new HiGraph[j][];

		for (i = j = 0; i < count.length; ++i) {
			if (count[i] != 0) {
				board[j++] = new HiGraph[count[i]];
				count[i] = 0;
		}	}

		/* Fill the board */

		int	rank, position;

		for (i = 0; i < size; ++i) {
			arc                   = (HiArc) children.elementAt(i);
			child                 = arc.to();
			rank                  = ranks[child.m_rank-node.m_rank]; // rank on board to use for this rank.
			position              = count[rank];
			child.m_position      = position;
			board[rank][position] = child;
			++count[rank];
		}
		/* A cludge */
		if (planar) {
			node.m_rowweight = 0.0;
		} else {
			node.m_rowweight = 1.0;
		}
		return(board);
	}

	static void dumpRow(int number, HiGraph row[]) 
	{
		int j;

		System.out.print(number + ":");
		for (j = 0; j < row.length; ++j) {
			System.out.print(row[j].m_position + ":" + row[j] + " ");
		}
		System.out.println("");
	}

	static void dumpBoard(HiGraph board[][]) 
	{
		int		i;
	
		for (i = 0; i < board.length; ++i) {
			dumpRow(i, board[i]);
	}	}

	private static void rowWeight(HiGraph higraph)
	{
		Vector		arcs;
		int			weight, i, in_size, out_size;
		HiArc		arc;
		HiGraph		other;

		weight       = 0;

		arcs       = higraph.m_out;									// Our output arcs
		out_size   = arcs.size(); 	
		for (i = out_size; i > 0; ) {								// For each of our output arcs
			arc       = (HiArc) arcs.elementAt(--i);				// Get arc
			other     = arc.to();									// Get node addressed on next row
			weight   += other.m_position;
		}

		arcs       = higraph.m_in;									// Our input arcs
		in_size    = arcs.size();
		for (i = in_size; i > 0; ) {								// For each of our input arcs
			arc       = (HiArc) arcs.elementAt(--i);				// Get arc
			other     = arc.from();									// Get node addressed on prior row
			weight   += other.m_position;
		}
		in_size += out_size;
		if (in_size == 0) {
			higraph.m_rowweight = -1.0;
		} else {
			weight += in_size;
			higraph.m_rowweight = ((double) weight)/((double) in_size);
		}
	}

	private static boolean shake(HiGraph row[])
	{
		HiGraph	higraph;
		int		i, position;
		double	max;
		boolean	ret;

		ret = false;
		for (i = row.length; i > 0; ) {
			higraph = row[--i];
			rowWeight(higraph);
		}

		for (position = row.length;;) {
			max = -2.0;
			for (i = row.length; i > 0; ) {
				higraph = row[--i];
				if (max < higraph.m_rowweight) {
					max = higraph.m_rowweight;
			}	}
			// Do the reverse way so flip equal weights at each cycle
			for (i = 0; i < row.length; ++i) {
				higraph = row[i];
				if (higraph.m_rowweight == max) {
					--position;
					if (higraph.m_position != position) {
						// Assign this row its new position
						higraph.m_position  = position;
						ret                 = true;
					}
					higraph.m_rowweight = -3.0;
					if (position == 0) {
						/*
						if (ret) {
							System.out.println("Baked row");
							dumpRow(0, row);
						}
						*/
						return(ret);
	}	}	}	}	}

	// Try to put the horizontal edges on the immediate right of the source addressing them

	private static void designated(HiGraph board[][], RelationClass rc)
	{
		HiGraph			row[], row1[];
		HiGraph			dst, src, other;
		Enumeration		en;
		int				i, j, i1, j1, rank, position;
		EntityInstance	e, e1;
		HiArc			arc;

		for (i = 0; i < board.length; ++i) {
			row = board[i];
			for (j = 0; j < row.length; ++j) {
				dst = row[j];
				dst.m_visited = 0;
		}	}

		for (i = 0; i < board.length; ++i) {
			row = board[i];
			for (j = 0; j < row.length; ++j) {
				dst = row[j];
				if (dst.m_visited != 0) {
					continue;
				}
				for (en = dst.m_in.elements(); en.hasMoreElements(); ) {
					arc  = (HiArc) en.nextElement();
					if (arc.getMinlength() != 0) {
						continue;
					}
					src  = arc.from();
					rank = src.m_rank;
					if (rank != dst.m_rank) {
						continue;
					}
					if (src == dst) {
						continue;
					}
					e   = dst.m_object;
					e1  = src.m_object;
					if (e1.getRelationTo(rc, e) == null) {
						continue;
					}
					position = src.m_position + 1;
					if (dst.m_position != position) {
						for (j1 = 0; j1 < row.length; ++j1) {
							other = row[j1];
							if (other.m_position >= position) {
								++other.m_position;
						}	}
						dst.m_position = position;
						dst.m_visited  = 1;
						j              = -1;
						break;
	}	}	}	}	}

	// Returns true if children have been considered for reordering

	private static boolean orderchildren(SimplexLayout options, HiGraph node) throws HiGraphException {

		HiArc		arc;
		HiGraph		child;
		Vector		children = node.m_children;
		int			size     = children.size();
		int			i, direction;
		boolean		ret      = false;
		
//		System.out.println("HiRow.orderChildren " + node);

		node.m_position = 0;
		if (size == 0) {
			return(ret);
		}

		// Order children from the bottom up
		for (i = 0; i < size; ++i) {
			arc = (HiArc) children.elementAt(i);
			child = arc.to();
			ret |= orderchildren(options, child);
		}
		
		// Only reorder at the lowest reorderable level
		if (ret) {
			return(ret);
		}
		

		if (node.dontReorder()) {
//			System.out.println("Don't reorder " + node);
			return(true);
		}

//		System.out.println("Building board");
		HiGraph board[][]      = buildBoard(node);		// [rank][offset within rank]
//		System.out.println("Built board");
	
		int		cycles         = options.crossing();
		int		to;
		boolean	improved, changed;
		RelationClass rc;

//		System.out.println("Initial board");
//		dumpBoard(board);
		
		if (node.m_rowweight == 0.0) {
//			System.out.println("Planar");
		} else {
//			System.out.println("Not planar");
			// Not a planar graph
			to        = board.length;
			direction = 1;
			improved  = false;
			for (i = 0;;) {
				/*
				System.out.println("Shake " + (direction > 0 ? "down" : "up"));
				dumpRow(i, board[i]);
				*/
				improved |= shake(board[i]);
				// Count each row as a cycle to balance load on graphs with many rows
				cycles -= to;
				if (cycles < 0) {
					break;
				}
				i        += direction;
				if (i < 0 || i >= to) {
					if (!improved) {
						break;
					}
					improved  = false;
					direction = -direction;
					i        += 2*direction;
			}	}
//			System.out.println("Non planar done");
		}

		rc = options.getDesignatedClass();
		if (rc != null) {
			designated(board, rc);
		}

		SortVector.byPosition(children);
		
//		System.out.println("After ordering lowest level of descendant");
//		dumpBoard(board);

//		System.out.println("Order children done");

		return(true);
	}
	
	static void order(SimplexLayout options, HiGraph root) throws HiGraphException 
	{
		orderchildren(options, root);

//		System.out.println("After ordering children");
//		root.dump();
	}
}
