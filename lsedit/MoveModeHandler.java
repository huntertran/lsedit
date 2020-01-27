package lsedit;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

/* This handler is strictly concerned with moving the I/O points */

public class MoveModeHandler extends LandscapeModeHandler
{
	// A small JComponent so that the whole handler doesn't need to be one
	// This object just draws a circle where the end point will be relocated

	protected class DrawOutline extends JComponent
	{
		public DrawOutline()
		{
			super();
			setForeground(Color.BLACK);
		}

		public void paintComponent(Graphics g)
		{
			EdgePoint edgePoint = m_edgePoint;

			super.paintComponent(g);

			if (edgePoint != null) {
				EntityInstance	entityInstance = m_entityInstance;
				g.drawOval((int) entityInstance.getEdgePointX(edgePoint)-6, (int) entityInstance.getEdgePointY(edgePoint)-6, 12, 12);
	}	}	}

	protected DrawOutline		m_drawOutline;				// The outline drawn
	protected EditModeHandler	m_parent;
	protected EdgePoint			m_edgePoint;
	protected EntityInstance	m_entityInstance;
	protected RelationInstance	m_relationInstance;
	protected RelationClass		m_rc;
	protected int			    m_active = 0;
	protected int				m_cursor = Cursor.DEFAULT_CURSOR;

	public	  void overEdgePointCallBack(EntityInstance entityInstance, RelationInstance relationInstance, EdgePoint edgePoint) 
	{
		// Solution to how to set two values are return from a single call
		m_entityInstance   = entityInstance;
		m_relationInstance = relationInstance;
		m_edgePoint        = edgePoint;
	}

	protected boolean overEdgePoint(Object thing, int x, int y)
	{
		if (thing instanceof RelationInstance) {
			RelationInstance	ri   = (RelationInstance)  thing;
			RelationComponent	c    = ri.getRelationComponent();
			
			if (c.mouseOverEdgePoint(x, y, this)) {
				m_rc = ri.getRelationClass();
				return true;
			}
		}
		return false;
	}

	// IO points

	protected RealPoint getFactors(int x, int y) 
	{
		// Can only have factors along edges 
		// Find closest edge and calculate new width and height factors

		Rectangle lyt = m_entityInstance.getDiagramBounds();

		double xp = x;
		double yp = y;

		double dl = Math.abs(xp - lyt.x);
		double dr = Math.abs(xp - (lyt.x + lyt.width));
		double dt = Math.abs(yp - lyt.y);
		double db = Math.abs(yp - (lyt.y + lyt.height));

		double wf, hf;

		if (dl < dr) {
			if (dt < db) {
				if (dt < dl) {
					// Top
					wf = (xp - lyt.x)/lyt.width;
					hf = 0.0;
				} else {
					// Left
					wf = 0.0;
					hf = (yp - lyt.y)/lyt.height;
				}
			} else if (db < dl) {
				// Bottom
				wf = (xp - lyt.x)/lyt.width;
				hf = 1.0;
			} else {
				// Left
				wf = 0.0;
				hf = (yp - lyt.y)/lyt.height;
			}
		} else {
			if (dt < db) {
				if (dt < dr) {
					// Top
					wf = (xp - lyt.x)/lyt.width;
					hf = 0.0;
				} else {
					// Right
					wf = 1.0;
					hf = (yp - lyt.y)/lyt.height;
				}
			} else if (db < dr) {
				// Bottom
				wf = (xp - lyt.x)/lyt.width;
				hf = 1.0;
			} else {
				// Right
				wf = 1.0;
				hf = (yp - lyt.y)/lyt.height;
			}
		}

		wf = Math.max(0.0, Math.min(1.0, wf));	// Normalize
		hf = Math.max(0.0, Math.min(1.0, hf));
		return new RealPoint(wf, hf);
	}

	protected boolean moveIOStart(int x, int y)
	{
		Diagram	diagram = m_ls.getDiagram();

		m_active = 2;
		m_ls.setCursor(Cursor.CROSSHAIR_CURSOR);
		m_drawOutline.setBounds(0, 0,diagram.getWidth(), diagram.getHeight());
		m_drawOutline.setVisible(true);
		diagram.add(m_drawOutline /* JLayeredPane.PALETTE_LAYER */ , 0);

		RealPoint factors = getFactors(x, y);
		m_ls.doFeedback("I/O point at factors (" + factors.getX() + ", " + factors.getY() + ")");
		return true;
	}

	protected void moveIOMotion(int x, int y) 
	{
		RealPoint factors = getFactors(x, y);

		m_ls.setCursor(Cursor.CROSSHAIR_CURSOR);
		m_edgePoint = m_entityInstance.setFactors(m_relationInstance, m_edgePoint, factors.getX(), factors.getY());

		// Need to do this because otherwise relations clip when redrawn as a consequence of drawOutline causing diagram repaint having diagram as bounds
		m_entityInstance.revalidateAllMyEdgesForClass(m_rc);

		m_drawOutline.repaint();
		m_ls.doFeedback("I/O point at (" + factors.getX() + ", " + factors.getY() + ")");
	}

	// --------------
	// Public methods
	// --------------

	public MoveModeHandler(EditModeHandler parent) 
	{
		super(parent.m_ls);
		m_parent = parent;
		m_drawOutline = new DrawOutline();
	}

	public void cleanup()
	{
		m_edgePoint    = null;
		switch (m_active) {
		case 2:
			{
				Diagram diagram = m_ls.getDiagram();

				m_drawOutline.setVisible(false);
				diagram.remove(m_drawOutline);
			}
		case 1:
			m_active = 0;
			if (m_cursor != Cursor.DEFAULT_CURSOR) {
				m_cursor = Cursor.DEFAULT_CURSOR;
				m_ls.setCursor(m_cursor);
	}	}	}

	public void movedOverThing(MouseEvent ev, Object thing, int x, int y) 
	{
/*
		if (thing instanceof RelationInstance) {
			System.out.println("Over " + thing + " active=" + m_active);
		}
 */

		switch (m_active) {
		case 0:
			if (!overEdgePoint(thing, x, y)) {
				return;
			}
			m_cursor = Cursor.CROSSHAIR_CURSOR;
			m_parent.setSubHandler(this);
			m_active = 1;
			break;
		case 1:
			if (!overEdgePoint(thing, x, y)) {
				m_active = 0;
				m_parent.cleanup();
				return;
		}	}
		m_ls.setCursor(m_cursor);
	}

	public void entityPressed(MouseEvent ev, EntityInstance e, int x, int y) 
	{
		if (m_active == 1) {
			moveIOStart(x, y);
		} 
	}

	public void relationPressed(MouseEvent ev, RelationInstance ri, int x, int y)
	{
//		System.out.println("MoveModeHandler relationPressed\n");

		movedOverThing(ev, ri, x, y);
		if (m_active == 1) {
			moveIOStart(x, y);
		} 
	}

	public void relationDragged(MouseEvent ev, RelationInstance ri, int x, int y) 
	{
//		System.out.println("MoveModeHandler relationDragged\n");
		if (m_edgePoint != null) {
			moveIOMotion(x, y);
		} 
	}

	public void relationReleased(MouseEvent ev, RelationInstance ri, int x, int y) 
	{
//		System.out.println("MoveModeHandler relationReleased\n");
		m_ls.clearFeedback();
		m_parent.cleanup();
	}
}

