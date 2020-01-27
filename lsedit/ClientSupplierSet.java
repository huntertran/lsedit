package lsedit;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;

// This object has the responsibility for laying out clients and supplier lists

abstract public class ClientSupplierSet extends JPanel 
{
	public final static int CLIENT_SUPPLIER_HEIGHT = 50;
	public final static int GAP                    = 5;

	// Established by constructor

	protected LandscapeEditorCore		m_ls;
	protected Diagram					m_diagram;
	protected JLabel                    m_label = null;


	// Working variables

	protected static Font		m_clientFont = null;
	protected EntityInstance	m_drawRoot;					// The current entity for which clients/services are being computed
	protected Vector			m_set;						// Full set identified
	protected int				m_fullSize;					// Number of members in m_set
	protected int				m_displayedSize;

	// --------------------
	// Overloads JComponent
	// --------------------

	public void setBounds(int x, int y, int width, int height)
	{
		super.setBounds(x, y, width, height);
		setSizes(width, height);
	}

	/* 

	// For debugging

	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g.drawRect(0, 0, getWidth()-1, getHeight()-1);

	}
	*/

	// -----------------
	// Protected methods
	// -----------------

	// Calculate the width needed to display the clients/suppliers

	protected int calcWidth(Graphics g) 
	{
		Enumeration		en;
		Dimension		dim;
		EntityInstance	e;
		int				tw;

		tw = 0;
		for (en = m_set.elements(); en.hasMoreElements(); ) {
			e = (EntityInstance) en.nextElement();
			dim = e.getFitDim(g, EntityInstance.SMALL_FONT, true);
			tw += dim.width;
		}
		return tw;
	}

	/* Validation is a problem here.  The problem is that while components are painted from
	 * high order element down, they are validated from low order up.  This means that the
	 * edges get validated in the diagram before the client and server gets validated.  Thus
	 * the edges end up pointing at where entities were -- not where they are moved to by
	 * the validation process. So we set the bounds on the entities in the client/supplier
	 * set when we add into that set, and there after don't change these values.
	 */

	protected void setSizes(int w, int h) 
	{
		Vector	set        = m_set;
		int		components = set.size();
		
		if (components == 0) {

			JLabel	label = m_label;

			if (label == null) {
				m_label = label = new JLabel();
				label.setFont(getClientSupplierFont());
			}
			label.setText("All " + m_fullSize + " clients are also suppliers");
			label.setHorizontalAlignment(JLabel.CENTER);
			label.setBounds(0, 0, w, h);

		} else {
			Graphics		g      = m_ls.getGraphics();
			int				gaps   = ((components+1) * GAP);	// Allow for a gap on both sides;
			int				width  = w - gaps;				// Available width
			int				tw     = calcWidth(g);			// Total widths of all entities
			double			width1 = (double) width;
			double			scale  = width1 / ((double) tw);
			int				width2;
			EntityInstance	e;
			Dimension		dim;
			int				i;
			
			m_label = null;

			if (scale > 2) {
				scale = 2;
			}

			for (i = components; --i >= 0; ) {
				e = (EntityInstance) set.elementAt(i);

				dim     = e.getFitDim(g, EntityInstance.SMALL_FONT, true);
				width1  = scale * (double) dim.width;
				width2  = (int) width1;

				if (width2 <= 0) {
					width2 = 1;
				}

				e.setEntitySize(width2, h, this);
	}	}	}

	protected void sort() 
	{
		if (m_set.size() > 1) {
			SortVector.byAvgX(m_set, true);
	}	}

	/* Validation is a problem here.  The problem is that while components are painted from
	 * high order element down, they are validated from low order up.  This means that the
	 * edges get validated in the diagram before the client and server gets validated.  Thus
	 * the edges end up pointing at where entities were -- not where they are moved to by
	 * the validation process. So we set the bounds on the entities in the client/supplier
	 * set when we add into that set, and there after don't change these values.
	 */

	protected void addSet() 
	{
		if (m_label != null) {
			// This goes on top
			add(m_label);
		}

		Vector	set        = m_set;
		int		components = set.size();
		
		if (components != 0) {
			Graphics	g        = m_ls.getGraphics();
			int			diagramX = getX();
			int			diagramY = getY();
			int			gaps     = ((components+1) * GAP);	// Allow for a gap on both sides;
			int			w        = getWidth();
			int			width    = w - gaps;				// Available width
			int			tw       = calcWidth(g);			// Total widths of all entities
			double		width1   = (double) width;
			double		scale    = width1 / ((double) tw);
			double		xpos     = GAP;

			EntityInstance	e;
			Dimension		dim;
			int				i, x;

			if (scale > 2) {
				scale = 2;
				xpos += ((double) (width - tw*2))/2.0;
			}

			for (i = 0; i < components; ++i) {
				e = (EntityInstance) set.elementAt(i);

				dim     = e.getFitDim(g, EntityInstance.SMALL_FONT, true);
				width1  = scale * (double) dim.width;
				x       = (int) xpos;

				e.setEntityLocation(x, 0);
				e.setDiagramLocation(diagramX+x, diagramY);
				xpos  += width1 + GAP;
	}	}	}

	// --------------
	// Public methods
	// --------------

	public static void setTextFont(Font font)
	{
		m_clientFont = font;
	}
	
	public static Font getClientSupplierFont()
	{
		if (m_clientFont == null) {
			m_clientFont = Options.getTargetFont(Option.FONT_CLIENTS);
		}
		return m_clientFont;
	}
	
	public ClientSupplierSet(Diagram diagram) 
	{
		setLayout(null);
		m_diagram = diagram;
		m_ls      = diagram.getLs();
		m_set     = new Vector();
	}

	public void removeAll()
	{
		super.removeAll();
		m_set.removeAllElements();
		m_fullSize      = 0;
		m_displayedSize = 0;	
	}

	public void addMember(EntityInstance e)
	{
		m_set.add(e);
	}

	public void seenMember()
	{
		++m_fullSize;
	}

	// Return the full set of entities in our set

	public Vector getFullSet()
	{
		return(m_set);
	}

	public int getFullSetSize()
	{
		return(m_fullSize);
	}

	public void order()
	{
		m_displayedSize = m_set.size();
		sort();
		addSet();
	}
}
