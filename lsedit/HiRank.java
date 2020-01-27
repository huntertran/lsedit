package lsedit;
import java.util.Vector;
import java.util.Enumeration;

/**
 * Developed by: Ian Davis in Summer 1997 for Grant Weddell
 * Provides a static method to compute an optimal vertical ranking for use when displaying a HiGraph.
 * The ranking is optimal in having the minimal total weighted edge length subject to the minlength
 * associated with each edge.  Internally this computation is performed by using the Network Simplex
 * Algorithm.
 *
 * See: A Technique for Drawing Directed Graphs by E. Gansner, E. Koutsofios
 * S. North, and K. Vo of AT&T Labs (http://www.research.att.com/sw/tools/graphviz/refs.html).
 */

/*
 * Note that because this code occurs in a new file it must be in a new class.. we really want
 * it to be in class HiGraph.  Worse, because it is in a new class any method defined here cannot
 * be applied against a this of type HiGraph.  However we don't want to subclass because Rank is
 * something that can be done to any HiGraph, because if we subclass we have to define an ordering
 * for these subclasses and can use only one per superclass, and we have to overload the creation
 * of both arcs and nodes so that an equivalent HiRankArc and HiRankGraph exists, since none of
 * the code here will operate on a this of type HiGraph.. an exception condition is raised.  YUK!!
 */

public class HiRank {
		
	/* -----------------------------------------------------------------------------------
	 * Assign the appropriate weights and minimum lengths to compute the optimal ranking
	 *
	 * Afterwards:
	 
	 * The weight of inclusion arcs is 0 and the weight of regular arcs is 1.
	 * Every non-leaf node will have a sink node to which all of its children are linked.
	 */

	private static boolean	designated(HiArc arc, RelationClass designatedClass) {

		if (designatedClass != null) {
			HiGraph src, dst;
			EntityInstance	e, e1;

			src = arc.from();
			dst = arc.to();
			e   = src.m_object;
			e1  = dst.m_object;

			if (e != null && e1 != null && e.getRelationTo(designatedClass, e1) != null) {
				return(true);
		}	}
		return(false);
	}

	private static void assignWeights(HiGraph node, RelationClass designatedClass) throws HiGraphException {

		HiArc		arc;
		HiGraph		child, sink, parent1;
		Enumeration e;

		sink = node.m_sink;
		if (sink != null) {
			sink.dispose();
			node.m_sink = sink = null;
		}
		if (node.m_children.size() != 0) {
			node.m_sink = sink = node.newChild(HiGraph.sinkV);
		}
		
		for (e = node.m_children.elements(); e.hasMoreElements(); ) {
			arc    = (HiArc) e.nextElement();
			child  = arc.to();
			arc.setWeight(0);	// We will happily increase the lengths of these invisible arcs
			arc.setMinlength(1);// We required all children to be at least one rank lower than their parent
			if (child != sink) {
				assignWeights(child, designatedClass);
				if (child.m_sink != null) {
					child = child.m_sink;
				}
				arc = sink.newInputArc(child);
				arc.setWeight(0);
				arc.setMinlength(1);// All nodes must be above sink
		}	}

		for (e = node.m_out.elements(); e.hasMoreElements(); ) {
			arc   = (HiArc) e.nextElement();
			if (arc.getWeight() == 0) {
				arc.setWeight(1);	// We would like to minise the total weight of these arcs
			}
			if (designated(arc, designatedClass)) {
				arc.setMinlength(0);
			} else {
				if (arc.getMinlength() == 0) {
					arc.setMinlength(1);// All arcs except for the designated arc must point down at least one rank
	}	}	}	}

	static void removesinks(HiGraph node) throws HiGraphException {
		HiArc		arc;
		HiGraph		sink;
		Enumeration e;

		for (e = node.m_children.elements(); e.hasMoreElements(); ) {
			arc    = (HiArc) e.nextElement();
			removesinks(arc.to());
		}

		sink = node.m_sink;
		if (sink == null) {
			node.m_sink_rank = node.m_rank;
		} else {
			node.m_sink_rank = sink.m_rank;
			node.m_sink = null;
			sink.dispose();
	}	}
	
	/*
	 * Balance this node using the local frequency counts associated with its HiGraph 
	 */

	private static void balance(HiGraph node, int local[]) throws HiGraphException {
		HiArc		arc = node.m_parent;
		HiGraph		child;
						
		int			min             = (arc != null ? arc.from().m_rank + 1 : 0);
		int			max             = node.m_sink_rank - 1;
		int			below[]  = new int[max+2];	// Allow for sink node just in case

		Vector		arcs            = node.m_children;
		int			size            = arcs.size();
		int			i;

		/* Compute the local rank frequencies for this box */

		if (node.horizontalArray()) {
			for (i = size - 1; i >= 0; --i) {
				arc   = (HiArc) arcs.elementAt(i);
				child = arc.to();
				// Force the nodes back into a horizontal row..
				child.m_rank = node.m_rank + 1;
		}	}
		
		for (i = size - 1; i >= 0; --i) {
			arc   = (HiArc) arcs.elementAt(i);
			child = arc.to();
			++below[child.m_rank];
		}

		/* Balance all of our descendants and begin  to compute our maximum rank */

		int		child_sink_rank = Integer.MIN_VALUE;

		if (size > 0) {
			int		child_rank      = Integer.MAX_VALUE;
			
			for (i = size - 1; i >=0; --i) {
				arc   = (HiArc) arcs.elementAt(i);
				child = arc.to();
				balance(child, below);
				if (child.m_rank      < child_rank) {
					child_rank      = child.m_rank;
				}
				if (child.m_sink_rank > child_sink_rank) {
					child_sink_rank = child.m_sink_rank;
			}	}
		
			/* Cant move top of box onto or below top of any child box */
			if (max >= child_rank) {
				max = child_rank - 1;
			}
			/* Never want to move this box up now */
			node.m_sink_rank = child_sink_rank + 1;
			min              = node.m_rank;
		}
		
			
		below = null;

		/* Balance this node */

		if (max <= min || node.m_weight != 0 ) {
			return;
		}

		arcs = node.m_in;
		size = arcs.size();

		for (i = size - 1; i >= 0; --i) {
			arc   = (HiArc) arcs.elementAt(i);
			child = arc.from();
			if (child.m_rank >= min) {
				min = child.m_rank + 1;
				if (max <= min) {
					return;
		}	}	}

		arcs = node.m_out;
		size = arcs.size();

		for (i = size - 1; i >= 0; --i) {
			arc   = (HiArc) arcs.elementAt(i);
			child = arc.to();
			if (child.m_rank <= max) {
				max = child.m_rank - 1;
				if (max <= min) {
					return;
		}	}	}

		/* If a box make as small as allowed while remaining feasible */

		if (child_sink_rank > 0) {
			node.m_rank = max;
			return;
		}

		/* Greedy algorithm: Put node on rank having fewest local components, or if equal best choice
		 * select the rank nearest the middle choice equalizing minimum input and output lengths.
		 */

		int choice = node.m_rank;
		int	mid    = (max + min);
		int diff1, diff2;
				
		for (i = min; i <= max; ++i) {
			if (local[i] < local[choice]) {
				choice = i;
				continue;
			}
			if (local[i] != local[choice]) {
				continue;
			}
			diff1  = mid - choice*2;
			if (diff1 < 0) diff1 = -diff1;
			diff2 = mid - i*2;
			if (diff2 < 0) diff2 = -diff2;
			if (diff2 < diff1) {
				choice = i;
		}	}

		diff1 = choice - node.m_rank;
		if (diff1 != 0) {
			System.out.println("Moving " + node + " from rank=" + node.m_rank + " to " + choice);
			--local[node.m_rank];
			++local[choice];
			node.m_rank      += diff1;
	}	}

/*
	public static void dump(HiGraph root)
	{
		Enumeration e, e1;
		HiArc		arc;
		HiGraph		node;

		for (e = root.m_children.elements(); e.hasMoreElements(); ) {
			arc    = (HiArc) e.nextElement();
			node   = arc.to();
			System.out.println(node + " position=" + node.m_rank + " ->");
			for (e1 = node.out().elements(); e1.hasMoreElements(); ) {
				arc  = (HiArc) e1.nextElement();
				node = arc.to();
				System.out.println(" " + node + " weight=" + arc.getWeight() + " minlength=" + arc.getMinlength());
	}	}	}
*/

	/* Compute an optimal feasible ranking for the graph
	 * This method may only be applied to the root of the graph.
	 */

	public static void compute(HiGraph root, int pivots, RelationClass designatedClass) throws HiGraphException 
	{
//		System.out.println("HiRank.compute begins");
//		dump(root);
//		System.out.println("HiRank: assignWeights");
		assignWeights(root, designatedClass);
//		System.out.println("HiRank: simplex");
		HiSimplex.simplex(root, pivots);
		// System.out.println("After adding sinks");
		// root.dump();
		removesinks(root);
		
		/* Allow for presence of sink node for safety */
/*
		int	local[] = new int[root.m_sink_rank + 1];
		balance(root, local);
		local = null;
 */
		
//		System.out.println("HiRank.compute done");
//		dump(root);
	}
}
