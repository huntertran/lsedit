package lsedit;

import java.util.Enumeration;
import java.util.Vector;

public class LsClusterer implements TaFeedback {

	static protected final double default_attractive_force = 0.05;
	static protected final double default_sparse_factor    = 1.0;
	static protected final double default_repulsive_force  = 0.01;
	static protected final double default_repulsive_diameter = 0.75;
	static protected final int    default_iterations       = 1000;
	static protected final int	  default_timeout           = 60;


	static protected final double default_separation_factor = 2.5;
	static protected final double  default_margin           = 0.05;
	static protected final double  default_gap              = 0.4;
	static protected final int     default_form_clusters    = 0; 
	static protected final boolean default_remove_contains  = true;
	static protected final boolean default_mustbe_related   = true;
	static protected final boolean default_feedback         = true;

	protected double	m_attractive_force = default_attractive_force;
	protected double	m_sparse_factor    = default_sparse_factor;
	protected double	m_repulsive_force  = default_repulsive_force;
	protected double	m_repulsive_diameter = default_repulsive_diameter;
	protected int		m_iterations       = default_iterations;
	protected int		m_timeout          = default_timeout;

	protected boolean	m_remove_contains  = default_remove_contains;
	protected boolean	m_mustbe_related   = default_mustbe_related;
	protected boolean	m_feedback         = default_feedback;

	protected double	m_separation_factor= default_separation_factor;
	protected double	m_margin           = default_margin;
	protected double	m_gap              = default_gap;
	protected int  	    m_form_clusters    = default_form_clusters;

	class ClusterNode {
		public EntityInstance	m_e;
		double					m_x;
		double					m_y;
		int						m_cluster;
		ClusterNode				m_next;
	};

	protected static void die()
	{
		System.exit(1);
	}

	protected void log(String message)
	{
		if (m_feedback) {
			System.err.println(Util.toLocaleString() + ": LSClusterer : " + message);
	}	}

	protected void layout(EditableTa ta) 
	{
		EntityInstance		rootInstance = ta.getRootInstance();
		Vector				selectedBoxes;
		int					size, i, j, iteration;
		EntityInstance		e, e1;
		Enumeration			en;
		RelationInstance	ri;
		double				width, height, width1, height1;
		double				x, y, x1, y1, xmax, ymax, x1max, y1max, xdiff, radius, radius1, ydiff, f, length, newlength;
		double				ideal_length;
		long				timeout;

		selectedBoxes = new Vector();
		rootInstance.gatherLeaves(selectedBoxes);

		size = selectedBoxes.size();
		if (size < 3) {
			return;
		}

		log("Clustering " + size + " items");

		// Use a variant of the spring layout algorithm to rapidly pull related things together
		// and push unrelated things apart. This differs from the spring layout algorithm in
		// being unconcerned about client/supplier relationships.

		ClusterNode[]	clusterNodes = new ClusterNode[size];
		ClusterNode		clusterNode, clusterNode1;
		boolean[][]		related;

		related = new boolean[size][];

		for (i = 0; i < size; ++i) {
			clusterNodes[i]       = clusterNode = new ClusterNode();
			related[i]            = new boolean[size-i];
			clusterNode.m_e       = e = (EntityInstance) selectedBoxes.elementAt(i);
			clusterNode.m_x       = e.xRelLocal();
			clusterNode.m_y       = e.yRelLocal();
			clusterNode.m_cluster = i;
			clusterNode.m_next    = null;
			e.orMark(EntityInstance.SPRING_MARK);
		}

		// Compute related and ideal length (max of two times any two widths or two heights -- converts 1.4 on diagonal)

		ideal_length   = 0;


		// Use real edges to determine relationships

		for (i = 0; i < size; ++i) {
			clusterNode = clusterNodes[i];
			e           = clusterNode.m_e;
			en          = e.srcRelationElements();
			if (en != null) {
				while (en.hasMoreElements()) {
					ri = (RelationInstance) en.nextElement();
					// Consider only visible edges when drawing layout
					if (ri.isRelationShown()) {
						e1       = ri.getDst();
						if (e1.isMarked(EntityInstance.SPRING_MARK)) {
							for (j = i+1; j < size; ++j) {
								if (e1 == clusterNodes[j].m_e) {
									related[i][j-i] = true;
									break;
			}	}	}	}	}	}
			en = e.dstRelationElements();
			if (en != null) {
				while (en.hasMoreElements()) {
					ri = (RelationInstance) en.nextElement();
					// Consider only visible edges when drawing layout
					if (ri.isRelationShown()) {
						e1       = ri.getSrc();
						if (e1.isMarked(EntityInstance.SPRING_MARK)) {
							for (j = i+1; j < size; ++j) {
								if (e1 == clusterNodes[j].m_e) {
									related[i][j-i] = true;
									break;
		}	}	}	}	}	}	}

		// Compute ideal distance as the maximum length of two related nodes
		
		for (i = 0; i < size; ++i) {
			clusterNode = clusterNodes[i];
			e           = clusterNode.m_e;
			width       = e.widthRelLocal();
			height      = e.heightRelLocal();
			for (j = i+1; j < size; ++j) {
				if (!related[i][j-i]) {
					continue;
				}
				clusterNode1 = clusterNodes[j];
				e1           = clusterNode1.m_e;
				width1       = e1.widthRelLocal();
				height1      = e1.heightRelLocal();
				xdiff        = width + width1;
				if (xdiff > ideal_length) {
					ideal_length = xdiff;
				}
				ydiff        = height + height1;
				if (ydiff > ideal_length) {
					ideal_length = ydiff;
		}	}	}

		ideal_length  *= m_sparse_factor;

		log("Iterating over these " + size + " items");

		timeout = System.currentTimeMillis() + (m_timeout * 1000);
		for (iteration = m_iterations; iteration > 0; --iteration) {

			for (i = 0; i < size; ++i) {
				clusterNode = clusterNodes[i];
				x           = clusterNode.m_x;
				y           = clusterNode.m_y;

				for (j = i+1; j < size; ++j) {
					clusterNode1 = clusterNodes[j];
					x1           = clusterNode1.m_x;
					y1           = clusterNode1.m_y;
					xdiff        = x1 - x;
					ydiff        = y1 - y;

					if (xdiff == 0 && ydiff == 0) {
						// Choose some direction and make length none zero.
						xdiff = (i%3)-1;
						if (xdiff == 0) {
							ydiff = (j%2)*2-1;
						} else {
							ydiff = (j%3)-1;
					}	} 
					length		 = Math.sqrt(xdiff*xdiff + ydiff*ydiff);

					if (related[i][j-i]) {
						f = (length - ideal_length)         * m_attractive_force;
					} else {
						f = (length - m_repulsive_diameter) * m_repulsive_force;

					}
					x  += (f * xdiff/length);		// (xdiff/length) -> cos angle
					x1 -= (f * xdiff/length);
					y  += (f * ydiff/length);		// (ydiff/length) -> sin angle
					y1 -= (f * ydiff/length);
	
					clusterNode1.m_x = x1;
					clusterNode1.m_y = y1;
				}
				clusterNode.m_x  = x;
				clusterNode.m_y  = y;
			}
			if (System.currentTimeMillis() > timeout) {
				log("Timeout after " + (m_iterations - iteration) + " iterations");
				break;
		}	}

		log("Build graph for " + size + " items");

		ClusterNode	tail;
		int			clusters;

		clusters = size;
		if (m_form_clusters != 1  && m_form_clusters < clusters) {
			// Now compute the distances between 

			Vector		distances = new Vector();
			Distance	distance;
			int			pairs;
			
			for (i = 0; i < size; ++i) {
				clusterNode = clusterNodes[i];
				x          = clusterNode.m_x;
				y          = clusterNode.m_y;
				for (j = i+1; j < size; ++j) {
					if (m_mustbe_related && !related[i][j-i]) {
						continue;
					}
					clusterNode1 = clusterNodes[j];
					xdiff  = clusterNode1.m_x - x;
					ydiff  = clusterNode1.m_y - y;

					length = Math.sqrt(xdiff*xdiff + ydiff*ydiff);
					distances.add(new Distance(length, i, j));
			}	}

/*
			for (j = 0; j < size; ++j) {
				clusterNode = clusterNodes[j];
				if (clusterNode.m_cluster == j) {
					System.err.print(j + ") ");
					for (tail = clusterNode; tail != null; tail = tail.m_next) {
						System.err.print(" " + tail.m_e + "(" + tail.m_x + "x" + tail.m_y + ")");
					}
					System.err.println("");
			}	}
 */

			pairs = distances.size();

			log("Sorting " + pairs + " of distances");

			SortVector.byDistance(distances);
			ideal_length = -1;
			for (i = 0; i < pairs; ++i) {
				distance = (Distance) distances.elementAt(i);
				length   = distance.m_length;
				if (length > ideal_length * m_separation_factor) {
					if (m_form_clusters == 0 && ideal_length >= 0) {
						break;
				}	}
				ideal_length = length;
				clusterNode  = clusterNodes[distance.m_i];
				clusterNode1 = clusterNodes[distance.m_j];

				if (clusterNode.m_cluster == clusterNode1.m_cluster) {
					// Both already in same cluster
					continue;
				}
				j            = clusterNode.m_cluster;					// Cluster to add to
				clusterNode  = clusterNodes[j];							// Head of this cluster
				clusterNode1 = clusterNodes[clusterNode1.m_cluster];	// Head of this cluster

				// Put everything in clusterNode1 into clusterNode

				for (tail = clusterNode1; tail.m_next != null; tail = tail.m_next) {
					tail.m_cluster = j;
				}
				tail.m_cluster     = j;
				tail.m_next        = clusterNode.m_next;
				clusterNode.m_next = clusterNode1;

/*
				for (j = 0; j < size; ++j) {
					clusterNode = clusterNodes[j];
					if (clusterNode.m_cluster == j) {
						System.err.print(j + ") ");
						for (tail = clusterNode; tail != null; tail = tail.m_next) {
							System.err.print(" " + tail.m_e + "(" + tail.m_cluster + ")");
						}
						System.err.println("");
				}	}
 */
				--clusters;
				if (clusters <= m_form_clusters) {
					break;
				}
				if (clusters < 3) {
					break;
		}	}	}

		double		xm, ym, xc, yc, fill;
		int			cnt, size1;
		ClusterNode	utilities;
		String		text;

		// Putting loose things into a new container greatly simplifies layout problems
		// Putting them one at a time into the draw root and placing sensibly is very expensive

		size1     = size - 1;
		utilities = null;

		if (clusters > m_form_clusters) {
			log("Identifying utilities");

			size1     = -1;
			clusters  = 0;
			for (i = 0; i < size; ++i) {
				clusterNode = clusterNodes[i];

				if (clusterNode.m_cluster != i) {	// Not head of a cluster
					continue;
				}
				if (clusterNode.m_next == null) {
					if (utilities != null) {
						// Add cluster node to utilities
						clusterNode.m_cluster = utilities.m_cluster;
						clusterNode.m_next    = utilities.m_next;
						utilities.m_next      = clusterNode;
						continue;
					}
					utilities = clusterNode;
				}
				++clusters;
				size1 = i;
		}	}	

		log("Reorganising " + size + " items into " + clusters + " selected clusters");

		clusters = 0;
		e1       = null;
		for (i = 0; i <= size1; ++i) {
			clusterNode = clusterNodes[i];

			if (clusterNode.m_cluster == i) {	// Head of a cluster
				++clusters;

				e1 = ta.getNewEntity(null, rootInstance);
				text = "Cluster" + clusters;
				if (clusterNode == utilities) {
					if (m_form_clusters == 1) {
						text += " (Layout)";
					} else {
						text += " (Utilities)";
				}	}
				e1.setLabel(text);
				
				cnt = 0;
				x   = xmax = clusterNode.m_x;
				y   = ymax = clusterNode.m_y;
				for (tail = clusterNode; tail != null; tail = tail.m_next) {
					if (tail.m_x < x) {
						x = tail.m_x;
					}
					if (tail.m_y < y) {
						y = tail.m_y;
					}
					if (tail.m_x > xmax) {
						xmax = tail.m_x;
					}
					if (tail.m_y > ymax) {
						ymax = tail.m_y;
					}
					++cnt;
				}
				e1.setDescription("Cluster of " + cnt + " items");

				cnt   = (int) (Math.ceil(Math.sqrt(cnt)));
				// 
				width = (1.0 - m_margin) * (1.0 - m_gap) / cnt;
				xdiff = xmax - x;
				if (xdiff == 0) {
					xm = 0;
				} else {

					/* Solve:
					 * (m_margin/2)             = m*x    + c
					 * 1 - (m_margin/2) - width = m*xmax + c
					 */
					xm = (1.0 - m_margin - width)/xdiff;
				}
				xc  = (m_margin*0.5)-xm*x;

				ydiff = ymax - y;
				if (ydiff == 0) {
					ym = 0;
				} else {
					ym = (1.0 - m_margin - width)/ydiff;
				}
				yc = (m_margin*0.5)-ym*y;

//				System.err.println("xm=" + xm + " ym=" + ym + " xc=" + xc + " yc=" + yc);
				for (; clusterNode != null; clusterNode = clusterNode.m_next) {
					e  = clusterNode.m_e;
					x = clusterNode.m_x * xm + xc;
					y = clusterNode.m_y * ym + yc;
//					System.err.println(e + "=" + clusterNode.m_x + "x" + clusterNode.m_y + " -> " + x + "x" + y);

					e.setRelLocal(x, y, width, width);
					ta.moveEntityContainment(e1, e);

					e.orMark(EntityInstance.SPRING_MARK);	// Incase cleared
			}	}
		}
			
		log("Finished forming " + clusters + " clusters");
	} 

	public LsClusterer(String input, String output) 
	{
		EditableTa ta = new EditableTa(this);

		String ret = ta.loadTA(input, System.in);
		if (ret != null) {
			error(ret);
		}

		layout(ta);

		ret = ta.saveByFile(output);
		if (ret != null) {
			error(ret);
		}
		System.exit(0);
	}

	// TaFeedback interface

	public void showProgress(String message)
	{
		if (m_feedback) {
			System.err.println("Progress: " + message + "\n");
	}	}

	public void doFeedback(String message)
	{
		if (m_feedback) {
			System.err.println("Feedback: " + message + "\n");
	}	}

	public void showInfo(String message)
	{
		if (m_feedback) {
			System.err.println("    Info: " + message + "\n");
	}	}

	public void error(String message)
	{
		System.err.println("   Error: " + message + "\n");
		die();
	}

	public void showCycle(RelationInstance ri)
	{
		System.err.println("Cycle detected in contains heirarchy within input TA\n");
		die();
	}

	 public void noContainRelation(String taPath)
	 {
		System.err.println("No contains relation defined in the input TA\n");
		die();
	}

	public void hasMultipleParents(RelationClass rc, EntityInstance e)
	{
		System.err.println("Multiple parents detected in contains heirarchy within input TA\n");
		die();
	}

	// Stand-alone version of this layouter.

	public static void main(String args[]) 
	{
		String input  = "";
		String output = "";

		switch (args.length) {
		case 2:
			output = args[1];
		case 1:
			input  = args[0];
		case 0:
			break;
		default:
			System.err.println("usage: java lsedit.LsClusterer [<input> [<output>]]\n");
			die();
		}
		new LsClusterer(input, output);
	}
} 





