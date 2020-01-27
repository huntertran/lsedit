package lsedit;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JComponent;

/* This handler is responsible for drawing an edge while creating it.
 */

public class NewEdgeModeHandler extends LandscapeModeHandler
{
	// A small JComponent so that the whole handler doesn't need to be one
	// This object just draws the arrow of the thing(s) being constructed

	protected class DrawOutline extends JComponent
	{
		public DrawOutline()
		{
			super();
			setForeground(Color.BLACK);
		}

		public void activate()
		{
			Diagram	diagram = m_ls.getDiagram();

			setBounds(0, 0, diagram.getWidth(), diagram.getHeight());
			setVisible(true);
			diagram.add(m_drawOutline, 0);
		}

		public void deactivate()
		{
			Diagram	diagram = m_ls.getDiagram();

			setVisible(false);
			diagram.remove(this);
		}

		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);

			EntityInstance	from   = m_from;
			int				from_x = from.getDiagramX();
			int				from_y = from.getDiagramY();
			int				from_w = from.getWidth();
			int				from_h = from.getHeight();
			int				x      = NewEdgeModeHandler.this.m_x;
			int				y      = NewEdgeModeHandler.this.m_y;
			int				src_x;
			int				src_y;

			if (x >= from_x) {
				if (x <= from_x + from_w) {
					from_x += (from_w/2);
				} else {
					from_x += from_w;
			}	}

			if (y >= from_y) {
				if (y <= from_y + from_h) {
					from_y += (from_h/2);
				} else {
					from_y += from_h;
			}	}

			// Draw an arrow head with the arrow at the end (since we are dragging it around)

			double	fraction = Util.drawArrowHead(g, from_x, from_y, x, y, 1);
			x -= (int) (((double) (x-from_x)) * fraction);
			y -= (int) (((double) (y-from_y)) * fraction);
			Util.drawSegment(g, Util.LINE_STYLE_NORMAL, from_x, from_y, x, y);
	}	}

	protected DrawOutline			m_drawOutline;				// The outline drawn
	protected EditModeHandler		m_parent;
	protected EntityInstance		m_from;
	protected EntityInstance		m_to;
	protected int					m_x;
	protected int					m_y;

	protected void stateChange()
	{
		String msg;

		if (m_from == null) {
			msg = "Click on new Edge's source entity";
		} else if (m_to == null) {
			m_drawOutline.activate();
			msg = "Click on target entity for edge having source entity " + m_from;
		} else {
			m_ls.updateNewRelation(m_from, m_to);
			msg = "Edge created from " + m_from + " to " + m_to;
			m_parent.cleanup();
		}
		m_ls.doFeedback(msg);
	}

	//
	// Public methods
	//

	public NewEdgeModeHandler(EditModeHandler parent) 
	{
		super(parent.m_ls);
		m_parent = parent;
		m_drawOutline = new DrawOutline();
	}

	public void cleanup()
	{
		if (m_from != null || m_to != null) {
			Diagram	diagram = m_ls.getDiagram();
			
			m_from = null;
			m_to   = null;
			m_drawOutline.deactivate();
		}
	}

	public void activate(Object object)
	{
		Vector					entities;
		LandscapeModeHandler	handler;
		Diagram					diagram;

		m_parent.cleanup();
		m_parent.setSubHandler(this);

		diagram  = m_ls.getDiagram();
		entities = diagram.targetEntities(object);
		m_from   = null;
		m_to     = null;
		if (entities != null) {
			switch (entities.size()) {
			case 0:
				break;
			case 1:
				m_from = (EntityInstance) entities.elementAt(0);
				break;
			case 2:
				m_from = (EntityInstance) entities.elementAt(0);
				m_to   = (EntityInstance) entities.elementAt(1);
				if (m_from.getGroupKeyFlag()) {
					EntityInstance temp;

					temp   = m_from;
					m_from = m_to;
					m_to   = temp;
				}
				break;
		}	}
		stateChange();
	}

	public void entityPressed(MouseEvent ev, EntityInstance e, int x, int y) 
	{
//		System.out.println("NewEdgeModehandler.entityPressed");

		m_x = x;
		m_y = y;

		if (m_from == null) {
			m_from = e;
		} else {
			m_to   = e;
		}
//		System.out.println(m_from + "->" + m_to);
		stateChange();
	}

	public void relationPressed(MouseEvent ev, RelationInstance ri, int x, int y)
	{
		m_ls.error("Edges must be between two entities -- don't click on relations");
	}

	public void movedOverThing(MouseEvent ev, Object thing, int x, int y) 
	{
		m_x = x;
		m_y = y;
		if (m_from != null) {
			m_drawOutline.repaint();
	}	}
}
	
