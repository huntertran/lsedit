package lsedit;

import java.util.Vector;

import java.awt.Graphics;

// This object has the responsibility for laying out clients and supplier lists

public class ClientSet extends ClientSupplierSet
{
	// --------------
	// Public methods
	// --------------

	public ClientSet(Diagram diagram) 
	{
		super(diagram);

		setToolTipText("Clients are things outside the diagram that have edges into the diagram");
	}

	public int setFound()
	{
		setToolTipText(m_fullSize + " clients");
		return(m_fullSize);
	}

	/* Two problems are addressed here, which are inter-related.
	 * (1) We can't physically show an entity as both client and supplier.. since
	 *     and EntityInstance has only one EntityComponent.  For safety we don't
	 *     even want to display X and Y where X contains Y since the problem of
	 *     duplication still arises if X is opened.  The solution here is to
	 *     throw away conflicts.
	 * (2) We don't want to display too many clients/suppliers if compacting.
	 *     The solution here is to aggregate
	 *
	 * At this point we know that clients and suppliers do not overlap
	 * because of the way they were initially found.  Specifically we don't look
	 * for clients under clients or suppliers under suppliers and things that are
	 * both clients and suppliers have been placed in supplierSet 
	 * collide with the other
	 */

	public boolean compact(int width, SupplierSet supplierSet, EntityInstance drawRoot)
	{
		Vector			clients     = getFullSet();
		int				size        = clients.size();
		Graphics		g           = m_ls.getGraphics();
		int				clientWidth = calcWidth(g) + (GAP * (size + 1));	// Allow a gap both sides
		boolean			mayCompact  = true;
		boolean			addedSuppliers = false;
		Vector			suppliers   = null;

		EntityInstance	e, above;
		int				i, j;
		boolean			climbing, lifted;

		if (width <= 0 || size == 0) {
			return false;
		}

		if (supplierSet != null) {
			suppliers = supplierSet.getFullSet();
		}

		while (clientWidth > width && mayCompact) {

			// Initialize IN_GRAPH_MARK to be known clients

			for (i = size; --i >= 0; ) {
				e = (EntityInstance) clients.elementAt(i);
				e.setDrawEntity(e);
				e.orMark(EntityInstance.IN_GRAPH_MARK);
			}

			mayCompact = false;

			do {
				climbing   = false;

				// Lift all drawEntity pointers one entity if resulting node still valid client

				for (i = size; --i >= 0; ) {
					e          = (EntityInstance) clients.elementAt(i);
					above      = e.getDrawEntity();
					above      = above.getContainedBy();
					if (above.hasDescendantOrSelf(drawRoot)) {
						// Above is not allowed to be a client
						continue;
					}
					e.setDrawEntity(above);

					if (!above.isMarked(EntityInstance.IN_GRAPH_MARK)) {
						// First path up  to this node
						above.orMark(EntityInstance.IN_GRAPH_MARK);
						climbing = true;
					} else {
						// Two paths to this node
						above.orMark(EntityInstance.COMPACT_MARK);
						mayCompact = true;
				}	}
			} while (!mayCompact && climbing);

			if (mayCompact) {

				// Seen one or more potential client nodes that have two or more paths to current client nodes
				// Do compaction
		

				/*	We have to do the lifting carefully to allow for cases such as shown below.
					After performing first edge lift both B and C will be marked to be compacted

                           B
                         / | \
				        A  |  F
						   C
						  / \
                         D   E

				 */

				for (i = size; --i >= 0; ) {
					e          = (EntityInstance) clients.elementAt(i);
					above      = e.getDrawEntity();

					if (!above.isMarked(EntityInstance.COMPACT_MARK)) {
						continue;
					}
					// Clear mark so don't see it more than once
					above.nandMark(EntityInstance.COMPACT_MARK);

					lifted = false;

					for (j = size; --j >= 0; ) {
						e = (EntityInstance) clients.elementAt(j);
						if (above.hasDescendant(e)) {

							// Cleanup this subpath beneath above

							e.setDrawEntity(null);
							for (; e != above; e = e.getContainedBy()) {
								e.nandMark(EntityInstance.IN_GRAPH_MARK | EntityInstance.COMPACT_MARK);
							}
							clients.removeElementAt(j);
							lifted = true;
					}	}

					if (!lifted) {
						// For safety
						continue;
					}

					above.orMark(EntityInstance.CLIENT_MARK);

					if (suppliers != null) {
						for (j = suppliers.size(); --j >= 0; ) {
							e = (EntityInstance) suppliers.elementAt(j);
							if (above.hasDescendant(e)) {
								suppliers.removeElementAt(j);
								above.orMark(EntityInstance.SUPPLIER_MARK);
					}	}	}

					if (!above.isMarked(EntityInstance.SUPPLIER_MARK)) {
						above.setDrawEntity(above);
						clients.add(above);
					} else {
						// Move above to the suppliers side since both client and supplier

						// Clean up this path since don't want to attempt to compact anything
						// that unbeknown to us has suppliers because we have moved above to suppliers

						above.setDrawEntity(null);
						for (e = above; e.isMarked(EntityInstance.IN_GRAPH_MARK); e = e.getContainedBy()) {
							e.nandMark(EntityInstance.IN_GRAPH_MARK | EntityInstance.COMPACT_MARK);
						}
						suppliers.add(above);
						addedSuppliers = true;
					} 

					size = clients.size();
					if (size == 0) {
						clientWidth = 0;
					} else {
						clientWidth   = calcWidth(g) + (GAP * (size + 1));	// Allow a gap both sides
					}
					if (clientWidth <= width) {
						// No need to lift any more nodes
						break;
					}
					// Rescan from size

					i      = size;
			}	}

			// Clean up marked entities

			for (i = size; --i >= 0; ) {
				e = (EntityInstance) clients.elementAt(i);
				e.setDrawEntity(null);
				for (; e.isMarked(EntityInstance.IN_GRAPH_MARK); e = e.getContainedBy()) {
					e.nandMark(EntityInstance.COMPACT_MARK | EntityInstance.IN_GRAPH_MARK);
			}	}
		} 

		return(addedSuppliers);
	}
}
