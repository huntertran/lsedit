package lsedit;
import java.util.Vector;
import java.util.Enumeration;

/**
 * Developed by: Ian Davis in Summer 1997 for Grant Weddell
 * Provides a static method to compute an optimal x layout when displaying a HiGraph.
 *
 * We assume that all children are in order of major rank and minor order within ranks.
 * That is:  we assume that the left/right order within the inclusion tree is the desired
 * ordering of nodes within the presentation.
 *
 * Essentially, we compute the layout for a given subgraph, and then shift this absolute
 * layout left/right relative to the previous children under a HiGraph so that it is as
 * close as possible to its left child subject to a minimum positive separation between
 * them.  This is performed recursively up through the levels of the higraph.
 *
 * The y coordinate of a node is easier to compute, given that the top of all nodes on a given
 * rank are to be aligned horizontally.  The problem of how one manages multi line (and thus
 * variable depth) foundational nodes (ie having no included graph) correctly has not yet been
 * solved.. 
 *
 * Once the position of a node is known, the coordinates of the arcs to/from it can be
 * trivially determined.
 */

class HiGraphCoordinates {
	
	/* Create an auxiliary graph for executing the simplex algorithm on.. The rules are as
	 * follows.. Every node to be ordered occurs in the auxgraph. If y immediately follows x
	 * within a rank in the original graph create an edge x->y in the aux graph with minlength
	 * set to minlength between x and y and weight 0..  If an edge existed between x->y create
	 * a new node z, create edges z->x, and z->y with minlength 0 and weight relative to the
	 * importance that the original edge be "near" vertical.
	 * 
	 * To support outside edges to/from a high graph when necessary create a left and right
	 * node and treat such outside edges as being directed at the left or right node as
	 * appropriate.
	 */

	static final boolean debug = false;

	private static void postorder(HiGraph node, int min) 
	{
		HiArc		arc;
		HiGraph		down;
		int			max  = min;
		
		node.m_minbeneath = min;
		
		for (Enumeration e = node.m_children.elements(); e.hasMoreElements(); ) {
			arc = (HiArc) e.nextElement();
			down = arc.to();
			postorder(down, max);
			max    = down.m_postorder + 1;
		}
		node.m_postorder = max;
	}

	private static HiGraph auxGraph(SimplexLayout options, HiGraph node) throws HiGraphException 
	{
		int			outside_bias  = options.outside_bias();
		int			weight_1dummy = options.weight_1dummy();
		int			weight_2dummy = options.weight_2dummy();
		boolean		hasBends      = options.hasBends();
		int			xgap, xweight;

		xgap    = node.m_xGap;
		if (xgap < 0) {
			xgap = options.xgap();
		}

		// Place great importance on the minimum xgap being enforced if horizontal array
		xweight = (node.m_horizontalArray ? 100 : 0);
					
		if (outside_bias < 0) outside_bias = 0;
		if (xgap < 0) xgap = 0;
		if (weight_1dummy < 0) weight_1dummy = 0;
		if (weight_2dummy < 0) weight_2dummy = 0;

		HiGraph		container = new HiGraph(null, node.label(), 0, node.height());
		HiGraph		duplicate, child, prev, fork, other;
		HiArc		arc;
		int			weight, i, j, k;
		Vector		children      = node.m_children;
		int			children_size = children.size();
		Vector		arcs;
		int			arcs_size;
		int			minlength;
		boolean		right_edges    = false;
		boolean		left_edges     = false;
		boolean		nodes_adjacent = false;

		//System.out.println("Building auxgraph");
		// node.dump();

		// Create the auxilary nodes that mimic nodes in the graph.  Double link node and mimic node
		// through the m_duplicate pointer

		for (i = 0; i < children_size; ++i) {
			arc    = (HiArc) children.elementAt(i);
			child  = arc.to();
			child.m_duplicate = duplicate = container.newChild(child.getReferencedObject(), child.label(), child.width(), child.height());
			duplicate.m_duplicate = child;
			if (child.m_outside > 0) {
				right_edges = true;
			} else if (child.m_outside < 0) {
				left_edges = true;
		}	}

		for (i = 0; i < children_size; ++i) {
			arc       = (HiArc) children.elementAt(i);
			child     = arc.to();
			arcs      = child.m_duplicate.m_out;
			arcs_size = 0;
			
scan:		for (j = i+1; j < children_size; ++j) {
				arc   = (HiArc) children.elementAt(j);
				other = arc.to();
				if (child.m_rank < other.m_rank) {
					if (child.m_sink_rank <= other.m_rank) {
						// Child strictly above other
						continue;
					}
				} else {
					if (child.m_rank > other.m_sink_rank) {
						// Child strictly below other
						continue;
					}
					if (child.m_rank == other.m_sink_rank && other.m_children.size() > 0) {
						// Child squeaks below others base since this is raised 1/2 a font..
						continue;
				}	}
				// These two nodes overlap in some horizontal row
				// Test to see if other must be after some node previously forced after node
				for (k = 0; k < arcs_size; ++k) {
					// Look at each prev node already known to have to appear after node
					arc  = (HiArc) arcs.elementAt(k);
					prev = arc.to().m_duplicate;

					if (prev.m_rank < other.m_rank) {
						if (prev.m_sink_rank <= other.m_rank) {
							// Prev strictly above other
							continue;
						}
					} else {
						if (prev.m_rank > other.m_sink_rank) {
							// Prev strictly below other
							continue;
						}
						if (prev.m_rank == other.m_sink_rank && other.m_children.size() > 0) {
							// Prev squeaks under other since other sink rank is raised 1/2 a font
							continue;
					}	}
					// Since node before prev and prev before other node automatically before other
					continue scan;
				}
		
				// The other node must be forced to be separated from node to stop overlap on some row.
				nodes_adjacent = true;
				arc = other.m_duplicate.newInputArc(child.m_duplicate);
				// Minimum length between centres of the two rectangles

				minlength = xgap + ((other.m_width + child.m_width)/2);
				if (!hasBends) {
					// If we don't show bends we don't need space between dummy nodes
					if (other.dummyNode() != 0 && child.dummyNode() != 0) {
						minlength = 0;
					} else if (other.dummyNode() != 0) {
						minlength = xgap + child.m_width/2;
					} else if (child.dummyNode() != 0) {
						minlength = other.m_width/2;
				}	}
				if (minlength < 0) {
					throw new HiGraphException("Negative distance between nodes " + child + " and " + other);
				}
				arc.setMinlength(minlength);
				arc.setWeight(xweight);
				++arcs_size;
		}	}

		HiGraph		left  = null;
		HiGraph		right = null;

		if (outside_bias != 0 && (left_edges || right_edges) && nodes_adjacent) {			

			/* Decide if we need a left and or right node to drag nodes towards:
			 * We will if we are laying out nodes partially adjacently horizontally
			 * and nodes in the graph have m_outside edges
			 */

			if (right_edges) {
				right = container.newChild(null, "right", 0, 0);
			}
			if (left_edges) {
				left = container.newChild(null, "left", 0, 0);
		}	}

		for (i = 0; i < children_size; ++i) {	
			arc       = (HiArc) children.elementAt(i);
			child     = arc.to();
			if (left != null && child.m_duplicate.m_in.size() == 0) {
				// No after prev constraints on this rank.. make sure it is not before left
			   	arc = child.m_duplicate.newInputArc(left);
				arc.setMinlength(0);
				arc.setWeight(0);
			}
			if (right != null && child.m_duplicate.m_out.size() == 0) {
				// No before constraints on this rank.. make sure it is not after right
				arc = right.newInputArc(child.m_duplicate);
				arc.setMinlength(0);
				arc.setWeight(0);
			}

			if (child.m_outside > 0) {
				other  = right;
				weight = child.m_outside;
			} else if (child.m_outside < 0) {
				other  = left;
				weight = (0 - child.m_outside);
			} else {
				other  = null;
				weight = 0;
			}

			if (other != null) {
				weight *= outside_bias;
				fork = container.newChild(null, "fork1", 0, 0);
				arc  = child.m_duplicate.newInputArc(fork);
				arc.setMinlength(0);
				arc.setWeight(weight);
				arc = other.newInputArc(fork);
				arc.setMinlength(0);
				arc.setWeight(weight);
			}

			arcs      = child.m_out;
			arcs_size = arcs.size();
		
			for (j = 0; j < arcs_size; ++j) {
				arc   = (HiArc) arcs.elementAt(j);
				other = arc.to();
				if (other.m_parent != null && other.m_parent.from() == node) {
					/* child and other share the same parent so in same higraph box 
					 * and are adjacent on some arc
					 */
					fork = container.newChild(null, "fork2", 0, 0);
					if (child.m_dummy_node != 0 && other.m_dummy_node != 0) {
						weight = weight_2dummy;
					} else if (child.m_dummy_node == 0 && other.m_dummy_node == 0) {
						weight = 1;
					} else {
						weight = weight_1dummy;
					}
					arc = child.m_duplicate.newInputArc(fork);
					arc.setMinlength(0);
					arc.setWeight(weight);
					arc = other.m_duplicate.newInputArc(fork);
					arc.setMinlength(0);
					arc.setWeight(weight);
		}	}	}

		// System.out.println("Built aux graph");
		// container.dump();
		return(container);
	}

	/*
	 * Balance all nodes which can slide to their central positions
	 */

	private static void balance(HiGraph real, HiGraph node) 
	{
		HiArc		arc;
		HiGraph		child;
		Vector		arcs;
		int			size;
		int			i;	
				
		int			min = Integer.MIN_VALUE;	/* Minimum feasible rank	*/
		int			max = Integer.MAX_VALUE;	/* Maximal feasible rank	*/
		int			temp;

		arcs = node.m_in;
		size = arcs.size();
		
		for (i = size - 1; i >= 0; --i) {
			arc   = (HiArc) arcs.elementAt(i);
			if (arc.getWeight() == 0) {
				/* Ie. a previous node at this rank */
				child = arc.to();
				if (child == node) {
					child = arc.from();
					temp  = child.m_rank + arc.getMinlength();
					if (temp > min) {
						min = temp;
		}	}	}	}

		arcs = node.m_out;
		size = arcs.size();
		for (i = size - 1; i >= 0; --i) {
			arc   = (HiArc) arcs.elementAt(i);
			if (arc.getWeight() == 0) {
				/* Ie a next node at this rank */
				child = arc.from();
				if (child == node) {
					child = arc.to();
					temp  = child.m_rank - arc.getMinlength();
					if (temp < max) {
						max = temp;
		}	}	}	}

		if (max <= min) {
			return;
		}

		arcs = real.m_in;
		size = arcs.size();

		int	best  = 0;
		int	count = 0;

		for (i = size - 1; i >= 0; --i) {
			arc   = (HiArc) arcs.elementAt(i);
			child = arc.from();
			if (child.m_duplicate != null) {
				best += child.m_duplicate.m_rank;
				++count;
		}	}

		arcs   = real.m_out;
		size   = arcs.size();
		
		for (i = size - 1; i >= 0; --i) {
			arc   = (HiArc) arcs.elementAt(i);
			child = arc.to();
			if (child.m_duplicate != null) {
				best += child.m_duplicate.m_rank;
				++count;
		}	}

		if (count != 0) {
			best /= count;
			if (best < min) {
				node.m_rank = min;
			} else if (best > max) {
				node.m_rank = max;
			} else {
				node.m_rank = best;
		}	}
	}

	/* Starting at the leaves perform the simplex algorithm on each contained set of nodes 
	 * compute the width of the parent before its width is required by the next iteration up
	 * of the simplex algorithm.
	 */

	private static void local_xlayout(SimplexLayout options, HiGraph node) throws HiGraphException 
	{
		HiGraph		aux;
		Vector		arcs;
		int			i, size;
		HiArc		arc;
		HiGraph		child, duplicate;
		int			xgap = options.xgap();
		int			width, temp;
		int			min = Integer.MAX_VALUE;
		int			max = Integer.MIN_VALUE;

		/* Do from the bottom up */

		arcs = node.m_children;
		size = arcs.size();

		for (i = 0; i < size; ++i) {
			arc    = (HiArc) arcs.elementAt(i);
			child  = arc.to();
			local_xlayout(options, child);
		}

		if (node.m_uniformWidth) {
			width = 0;

			for (i = 0; i < size; ++i) {
				arc    = (HiArc) arcs.elementAt(i);
				child  = arc.to();
				if (width < child.m_width) {
					width = child.m_width;
			}	}
			for (i = 0; i < size; ++i) {
				arc    = (HiArc) arcs.elementAt(i);
				child  = arc.to();
				child.m_width = width;
		}	}

		/* Initially the width of any HiGraph is the width of its label */

		if (node.m_dummy_node != 0) {
			width = 0;
		} else {
			width = node.width();
		}
		min   = max = 0;

		if (size != 0) {
			aux = auxGraph(options, node);
			
			if (debug) {
				System.out.println("Auxiliary constraints");
				HiSimplex.dump_constraints(aux);
				//System.out.println("AUX before");
				//aux.dump();
			}
			HiSimplex.simplex(aux, options.simplex());
			// System.out.println("AUX after");
			// aux.dump();
			


			/* Working from the largest rank backwards so we balance correctly */

			for (i = size - 1; i >= 0; --i) {
				arc   = (HiArc) arcs.elementAt(i);
				child = arc.to();
				duplicate = child.m_duplicate;
				if (child.m_in.size() == 0 || child.m_out.size() == 0) {
					balance(child, duplicate);
				}
				child.m_x = duplicate.m_rank;
				temp = child.m_x - child.m_width/2;
				if (temp < min) {
					min = temp;
				}
				temp += child.m_width;
				if (temp > max) {
					max = temp;
			}	}
					
			for (i = size-1; i >= 0; --i) {
				arc                 = (HiArc) arcs.elementAt(i);
				child               = arc.to();
				child.m_duplicate   = null;
			}

			if (debug) {
				System.out.println("Local x_offsets of children below " + node + ":");
				for (i = 0; i < size; ++i) {
					arc   = (HiArc) arcs.elementAt(i);
					child = arc.to();
					System.out.print(" " + child + "=" + (child.m_x - child.m_width/2) + "-" + (child.m_x + child.m_width/2) );
				}
				System.out.println("\nMinbelow=" + min + " max=" + max + " xgap=" + xgap);
			}

			min -= xgap;				// Leave gap to left of left most box below me..
			max += xgap;				// Leave gap to right of right most box below me..
			if (max - min > width) {
				width = max - min;
			}
		
			aux.dispose();
		}
	
		
		node.m_x     = 0;
		node.m_width = width;
	}

	/* 
	 * shift all the m_rectangle.x values appropriately by centering within their boxes
	 */

	private static void global_xlayout(HiGraph node, int x_shift) 
	{
		HiGraph		child;
		HiArc		arc;
		Enumeration e;
		int			min = Integer.MAX_VALUE;
		int			max = Integer.MIN_VALUE;
		int			i, temp, size;
		Vector		arcs;
		
		/* Establish the left and right boundaries of the box */

		node.m_x += x_shift;
		arcs      = node.m_children;
		size      = arcs.size();

		for (i = 0; i < size; ++i) {
			arc    = (HiArc) arcs.elementAt(i);
			child  = arc.to();
			temp   = child.m_x - (child.m_width/2);
			if (temp < min) {
				min = temp;
			}
			temp  += child.m_width;
			if (temp > max) {
				max = temp;
		}	}

		x_shift = node.m_x - (max + min) / 2;
		for (i = 0; i < size; ++i) {
			arc    = (HiArc) arcs.elementAt(i);
			child  = arc.to();
			global_xlayout(child, x_shift);
		}
	}
	
	private static void local_ylayout(SimplexLayout options, HiGraph node) 
	{
		HiGraph		child;
		HiGraph		parent;
		HiArc		arc;
		Enumeration e;
		int			height      = node.height();
		int			ranks;
		int			ygap        = options.ygap();
		int			separation  = height + ygap;
		int			textheight;
		
		for (e = node.m_children.elements(); e.hasMoreElements(); ) {
			arc   = (HiArc) e.nextElement();
			child = arc.to();
			local_ylayout(options, child);
		}

		if (node.m_dummy_node == HiGraph.edgepointV) {
			int	displace  = 0;

			node.m_height = 0;
			arc           = (HiArc) node.m_out.elementAt(0);
			child         = arc.to();
			arc           = (HiArc) node.m_in.elementAt(0);
			parent        = arc.from();
			if (child.m_dummy_node == parent.m_dummy_node) {
				if (child.m_dummy_node == HiGraph.edgepointV) {
					displace  = (height/2);
				} else {
					node.width(0);
					node.height(separation - ygap);
					displace = (node.height()/2);
				}
			} else if (child.m_dummy_node != HiGraph.edgepointV) {
				displace  = height;
			} 
			node.m_y = node.m_rank * separation + displace;
		} else {
			ranks = node.m_sink_rank - node.m_rank + 1;
			if (ranks < 0) {
				System.out.println("Caution: " + node + " has " + ranks + " ranks " + node.m_rank + "-" + node.m_sink_rank);
			}
			node.m_height = ranks * separation - ygap;
			if (ranks != 1) {
				node.m_height -= (height + ygap/2);		// Don't include the sink rank in the box
			}
			textheight    = node.height();
			if (textheight > node.m_height) {
				node.m_height = textheight;
			}
			node.m_y      = node.m_rank * separation /* + (node.m_height/2) */;
		}
	}

	private static void global_ylayout(HiGraph node, int y_shift) 
	{
		Vector		children = node.m_children;
		int			size     = children.size();
		HiGraph		child;
		HiArc		arc;
		int			i;
			
		for (i = 0; i < size; ++i) {
			arc    = (HiArc) children.elementAt(i);
			child  = arc.to();
			global_ylayout(child, y_shift);
		}
		node.m_y += y_shift;

		if (node.uniformDepth()) {
			HiGraph	child1, child2;
			int		top, bottom, top1, bottom1;
			int		j, k, y, height, diff;
			boolean	flag;

			for (i = 0; i < size; ++i) {
				arc    = (HiArc) children.elementAt(i);
				child  = arc.to();
				top    = child.m_y - child.m_height/2;
				bottom = child.m_y + child.m_height/2;
				flag   = false;
				for (j = i+1; j < size; ++j) {
					arc     = (HiArc) children.elementAt(j);
					child1  = arc.to();
					top1    = child1.m_y - child1.m_height/2;
					bottom1 = child1.m_y + child1.m_height/2;
					switch (top - top1) {
					case -1:
					case 0:
					case 1:
						if (bottom != bottom1) {
							flag = true;
							if (bottom < bottom1) {
								bottom = bottom1;
				}	}	}	}
				if (!flag) {
					continue;
				}
				for (j = i; j < size; ++j) {
					arc     = (HiArc) children.elementAt(j);
					child1  = arc.to();
					top1    = child1.m_y - child1.m_height/2;
					bottom1 = child1.m_y + child1.m_height/2;
					switch (top - top1) {
					case -1:
					case 0:
					case 1:
						height = bottom - top;
						y      = top + height/2;

						// check for overlap
						for (k = 0; k < size; ++k) {
							if (k == j) {
								continue;
							}
							arc    = (HiArc) children.elementAt(k);
							child2 = arc.to();
							diff = child2.m_y - y;
							if (diff < 0) {
								diff = 0 - diff;
							}
							if (diff >= (height + child2.m_height) / 2) {
								// Dont overlap on the y axis
								continue;
							}
							diff   = child2.m_x - child1.m_x;
							if (diff < 0) {
								diff = 0 - diff;
							}
							if (diff >= (child1.m_width + child2.m_width) / 2) {
								// Dont overlap on the x axis
								continue;
							}
							
							// Abort change because of overlap
							break;
						}
						if (k == size) {
							child1.m_height = height;
							child1.m_y      = y;
	}	}	}	}	}	}

	private static int clip(int position, int border) 
	{
		if (position > border) {
			return(border);
		}
		if (position < -border) {
			return(-border);
		}
		return(position);
	}

	static void arc_layout(HiArc arc) 
	{
		int		fromX, fromY, toX, toY;
		HiGraph	from = arc.from();
		HiGraph to   = arc.to();
		int		half_from_width  = from.m_width  / 2;
		int		half_from_height = from.m_height / 2;
		int		half_to_width    = to.m_width    / 2;
		int		half_to_height   = to.m_height   / 2;

		int		from_centre_x    = from.m_x;
		int		from_centre_y    = from.m_y;
		int		to_centre_x      = to.m_x;
		int		to_centre_y      = to.m_y;

		int		opposite         = to_centre_y - from_centre_y;
		int		adjacent         = to_centre_x - from_centre_x;
		
		if (adjacent < 2 && adjacent > -2) {
			/* Draw vertical */
			fromY  = from_centre_y;
			toY    = to_centre_y;
			fromX  = toX = to_centre_x;
			if (opposite > 0) {
				fromY += half_from_height;
				toY   -= half_to_height;
			} else {
				fromY -= half_from_height;
				toY   += half_to_height;
			}
		} else if (opposite < 2 && opposite > -2) {
			/* Draw horizontal */
			fromX  = from_centre_x;
			toX    = to_centre_x;
			fromY  = toY = to_centre_y;
			if (adjacent > 0) {
				fromX += half_from_width;
				toX   -= half_to_width;
			} else {
				fromX -= half_from_width;
				toX   += half_to_width;
			}
		} else {
			/* Treat the centres as the origins so that the value pairs X,Y may be +ve/-ve */

			double	slope  = ((double) opposite) / ((double) adjacent);
		
			fromX = (int) (((double) half_from_height) / slope );
			fromY = (int) (((double) half_from_width) * slope );
			fromX = clip(fromX, half_from_width);
			fromY = clip(fromY, half_from_height);
			
			toX   = (int) (((double) half_to_height) / slope );
			toY   = (int) (((double) half_to_width) * slope );
			toX   = clip(toX, half_to_width);
			toY   = clip(toY, half_to_height);

			if (slope < 0.0) {
				fromY = 0 - fromY;
				toY   = 0 - toY;
			}
			
			/* Select the shortest length of line possible */

			int	choice1, choice2, length1, length2;

			length1 = (to_centre_x + toX) - (from_centre_x + fromX);
			length2 = (to_centre_y + toY) - (from_centre_y + fromY);
			choice1 = (length1 * length1) + (length2 * length2);

			length1  = (to_centre_x - toX) - (from_centre_x + fromX);
			length2  = (to_centre_y - toY) - (from_centre_y + fromY);
			choice2  = (length1 * length1) + (length2 * length2);
		
			if (choice2 < choice1) {
				choice1 = choice2;
				toX     = -toX;
				toY     = -toY;
			}

			length1  = (to_centre_x + toX) - (from_centre_x - fromX);
			length2  = (to_centre_y + toY) - (from_centre_y - fromY);
			choice2  = (length1 * length1) + (length2 * length2);

			if (choice2 < choice1) {
				fromX = -fromX;
				fromY = -fromY;
			}

			fromX += from_centre_x;
			fromY += from_centre_y;
			toX   += to_centre_x;
			toY   += to_centre_y;
		}
	
		int	diff;

		if (from.m_children.size() > 0) {
			diff = fromX - from_centre_x;
			if (diff < 0) {
				diff = 0 - diff;
			}
			if (diff == half_from_width) {
				/* On left or right edge of box */
				if (to.m_rank < from.m_sink_rank) {
					// Place at bottom of box if descendant strictly below me
					fromY = from_centre_y - half_from_height + from.height();
				} else {
					// Place at top of box if descendant overlaps my root through sink nodes.
					fromY = from_centre_y + half_from_height - from.height();
			}	}
		} else {
			if (from.m_width == 0) {
				fromY = from.m_y + half_from_height;
		}	}

		if (to.m_children.size() > 0) {
			diff = toX - to_centre_x;
			if (diff < 0) {
				diff = 0 - diff;
			}
			if (diff == half_to_width) {
				// On left or right side of box
				// Place at bottom of box
				toY = to_centre_y - half_to_height + to.height();
			}
		} else {
			if (to.m_width == 0) {
				toY = to.m_y - half_to_height;
		}	}

		if (arc.reversed() ) {
			int temp;
			temp   = fromX;
			fromX  = toX;
			toX    = temp;
			temp   = fromY;
			fromY  = toY;
			toY    = temp;
		}
		arc.m_fromX = fromX;
		arc.m_toX   = toX;
		arc.m_fromY = fromY;
		arc.m_toY   = toY;
	}

	/* Layout for self arcs */

	private static void arc_layout(HiGraph node, HiArc arc, int repeat) {
		arc.m_fromX = node.m_x;
		arc.m_fromY = node.m_y;
		if ((repeat & 1) == 0) {
			arc.m_fromX += node.m_width/2;
		} else {
			arc.m_fromX -= node.m_width/2;
		}
		if (((repeat>>1) & 1) == 0) {
			arc.m_fromY   -= node.m_height/2;
		} else {
			arc.m_fromY   += node.m_height/2;
		}
		if (node.m_height < node.m_width) {
			arc.m_toX = node.m_height;
		} else {
			arc.m_toX = node.m_width;
		}
		
		arc.m_toX -= (repeat >> 2) * 4;
		if (arc.m_toX < 4) {
			arc.m_toX = 4;
		}

		int shift = (arc.m_toX>>1);

		switch (repeat & 3) {
		case 1:
			arc.m_fromX -= shift;
			arc.m_fromY -= shift;
			arc.m_toY = 0;
			break;
		case 2:
			arc.m_fromX -= shift;
			arc.m_fromY -= shift;
			arc.m_toY = 180;
			break;
		case 3:
			arc.m_fromX -= shift;
			arc.m_fromY -= shift;
			arc.m_toY = 90;
			break;
		default:
			arc.m_fromX -= shift;
			arc.m_fromY -= shift;
			arc.m_toY = 270;
		}
	}

	/* Layout all arcs into out of a given node */

	private static void layout(HiGraph node, boolean all, boolean consider_inputs) 
	{
		HiArc		arc;
		HiGraph		child;
		Enumeration e;

		if (all) {
			for (e = node.m_children.elements(); e.hasMoreElements(); ) {
				arc    = (HiArc) e.nextElement();
				child  = arc.to();
				layout(child, all, consider_inputs);
		}	}

		if (consider_inputs) {
			for (e = node.m_in.elements(); e.hasMoreElements(); ) {
				arc = (HiArc) e.nextElement();
				arc_layout(arc);
		}	}

		for (e = node.m_out.elements(); e.hasMoreElements(); ) {
			arc = (HiArc) e.nextElement();
			arc_layout(arc);
		}

		int i = 0;
		for (e = node.m_self.elements(); e.hasMoreElements(); ) {
			arc = (HiArc) e.nextElement();
			arc_layout(node, arc, i);
			++i;
	}	}

	/* Called to merely layout a subtree */

	static void layout(HiGraph node, boolean all) 
	{
		layout(node, all, true);
	}

	/* Relayout everything from scratch */

	public static void coordinates(SimplexLayout options, HiGraph root) throws HiGraphException 
	{
		// System.out.println("Computing coordinates for HiGraph");
		// postorder(root, 0);
		// System.out.println("Before layout");
		// root.dump();
		local_xlayout(options, root);
		local_ylayout(options, root);
				
		global_xlayout(root, options.xmargin() - (root.m_x - root.m_width / 2) );
		global_ylayout(root, options.ymargin() - (root.m_y - root.m_height/2) );

		layout(root, true, false);
		// System.out.println("After global coordinates computed");
		// root.dump();
	}
}
