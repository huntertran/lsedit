package lsedit;
import java.util.Vector;
import java.util.Enumeration;

class HiGroup {
	
	static final boolean debug = false;
	
	/* Places dummy nodes on every arc having some slack (ie difference in rank between in and out
	 * node > 1).  These dummy nodes are included within the closest parent including both ends of
	 * the original arc.  The original arc is preserved as the final arc in the chain.. ie. The one
	 * arc which goes from a dummy node to a real node.  This must be done before we reorder children
	 * since dummy nodes must be treated as legitimate children.
	 */

	private static void addDummyBoxes(HiGraph node, int direction) throws HiGraphException 
	{
		Vector		children;
		HiArc		arc;
		HiGraph		child;
		int			nodes, i, flag;

		flag     = 0;
		children = node.m_children;
		nodes    = children.size();

		for (i = 0; i < nodes; ++i) {
			arc   = (HiArc) children.elementAt(i);
			child = arc.to();
			addDummyBoxes(child, direction);
			if (child.m_children.size() > 0) {
				flag |= 1;
			} else {
				flag |= 2;
		}	}

		if (flag == 3) {

			HiGraph box       = node.newChild();
			Vector	children1 = box.m_children;

			for (i = nodes; --i >= 0; ) {
				arc   = (HiArc) children.elementAt(i);
				child = arc.to();
				if (child.m_children.size() != 0) {
					continue;
				}
				if (child == box) {
					continue;
				}
				children.removeElementAt(i);
				arc.from(box);
				children1.addElement(arc);
				child.m_depth++;
			}

			nodes = children.size();
			for (i = 0; i < nodes; ++i) {
				arc   = (HiArc) children.elementAt(i);
				child = arc.to();
				if (child == box) {
					continue;
				}
				if (direction > 0) {
					arc = box.newInputArc(child);
				} else {
					arc = box.newOutputArc(child);
				}
	}	}	}


	static void group(HiGraph root, int direction) throws HiGraphException 
	{

//		System.out.println("Grouping " + direction);
		if (direction != 0) {
			addDummyBoxes(root, direction);
	}	}

}