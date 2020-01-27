package lsedit;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

// A small JComponent so that the whole handler doesn't need to be one
// This object just draws the outline of the thing(s) being moved

class DrawOutline extends JComponent
{
	public DrawOutline()
	{
		super();
		setForeground(Color.BLACK);
	}

	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		g.drawRect(0, 0, getWidth()-1, getHeight()-1);
	}
}

public class ResizeModeHandler extends LandscapeModeHandler
{


	protected static final int[] resizeCursor =
		{ 
			Cursor.NW_RESIZE_CURSOR, Cursor.N_RESIZE_CURSOR, 
			Cursor.NE_RESIZE_CURSOR, Cursor.E_RESIZE_CURSOR, 
			Cursor.SE_RESIZE_CURSOR, Cursor.S_RESIZE_CURSOR,
			Cursor.SW_RESIZE_CURSOR, Cursor.W_RESIZE_CURSOR, 
		};


	// Variables used by modes

	protected static final int RM_NONE	 = 0;
	protected static final int RM_LEFT	 = 1;
	protected static final int RM_RIGHT	 = 2;
	protected static final int RM_TOP	 = 4;
	protected static final int RM_BOTTOM = 8;

	protected static final int LR_MASK = RM_LEFT | RM_RIGHT;
	protected static final int TB_MASK = RM_TOP  | RM_BOTTOM;

	protected static final int RZONE_SIZE = 5;
	protected static final double MIN_DIM	 = 10.0;

	protected EditModeHandler	m_parent;
	protected int				m_zn;
	protected DrawOutline		m_drawOutline;
	protected Rectangle			m_curLayout;
	private	  Rectangle			m_plyt = null;
	protected int				m_resizeMode = RM_NONE;
	protected int				m_cursor = Cursor.DEFAULT_CURSOR;


	protected int[] startMode = 

		{ RM_TOP    | RM_LEFT, 
		  RM_TOP, 
		  RM_TOP    | RM_RIGHT, 
		  RM_RIGHT,
		  RM_BOTTOM | RM_RIGHT,
		  RM_BOTTOM, 
		  RM_BOTTOM | RM_LEFT, 
		  RM_LEFT
		};

	protected int overResizeTab(Object thing, int x, int y) 
	{
		int	ret = EntityInstance.RSZ_NONE;

		if (thing instanceof EntityInstance) {
			Diagram			diagram = m_ls.getDiagram();
			EntityInstance	e       = (EntityInstance) thing;

			if (!e.isDrawRoot()) {
				if (e.getGroupKeyFlag()) {
					ret = e.overResizeTab(x, y);
		}	}	}
		return(ret);
	}

	protected void setCurLayout(int x, int y, int width, int height)
	{
		DrawOutline	drawOutline = m_drawOutline;
		Rectangle curLayout		= m_curLayout;

		if (curLayout == null) {
			Diagram diagram = m_ls.getDiagram();
			m_curLayout     = curLayout = new Rectangle(x, y, width, height);
			drawOutline.setBounds(x, y, width+1, height+1);
			drawOutline.setVisible(true);
			diagram.add(m_drawOutline /* , JLayeredPane.PALETTE_LAYER */, 0);
		} else {
			curLayout.x      = x;
			curLayout.y      = y;
			curLayout.width  = width;
			curLayout.height = height;
			drawOutline.setBounds(x, y, width+1, height+1);
			m_drawOutline.repaint();
		}
	}

	protected void addMode(int newMode) 
	{
		int omode = 0;

		switch(newMode) {
		case RM_TOP:
			omode = RM_BOTTOM;
			break;
		case RM_BOTTOM:
			omode = RM_TOP;
			break;
		case RM_LEFT:
			omode = RM_RIGHT;
			break;
		case RM_RIGHT:
			omode = RM_LEFT;
			break;
		} 

		if ((m_resizeMode & omode) == 0) {
			m_resizeMode |= newMode;
		}
	}

	private String resizeDescription() 
	{
		String ret;

		if (m_resizeMode == RM_NONE) {
			ret = "NONE";
		} else {
			ret = "";
			if ((m_resizeMode & RM_LEFT) != 0) {
				ret += "LEFT ";
			}
			if ((m_resizeMode & RM_RIGHT) != 0) {
				ret += "RIGHT ";
			}
			if ((m_resizeMode & RM_TOP) != 0) {
				ret += "TOP ";
			}
			if ((m_resizeMode & RM_BOTTOM) != 0) {
				ret += "BOTTOM";
			}
		}
		return(ret);
	}

	protected void doResizeAdjust(int mode, int xpos, int ypos) 
	{
		int gap = 20;
		int x1, y1, width1, height1;

		x1		= (int) m_curLayout.x;
		y1		= (int) m_curLayout.y;
		width1	= (int) m_curLayout.width;
		height1 = (int) m_curLayout.height;

		if (xpos-x1 < 4) {
			addMode(RM_LEFT);
		}
		if (x1 + width1 - xpos < 4) {
			addMode(RM_RIGHT);
		}
		if (ypos-y1 < 4) {
			addMode(RM_TOP);
		}
		if (y1 + height1 - ypos < 4) {
			addMode(RM_BOTTOM);
		}

		if ((mode & RM_TOP) != 0) {

			if (ypos >= m_plyt.y + gap) {
				height1 += y1 - ypos;
				y1		 = ypos;
			}
		} else if ((mode & RM_BOTTOM) != 0) {
			if (ypos <= m_plyt.y + m_plyt.height - gap) {
				height1 = ypos - y1;
		}	 }

		if ((mode & RM_LEFT) != 0) {
			if (xpos >= m_plyt.x + gap) {
				width1 += x1 - xpos;
				x1		= xpos;
			}
		} else if ((mode & RM_RIGHT) != 0) {
			if (xpos <= m_plyt.x + m_plyt.width - gap) {
				width1 = xpos - x1;
		}	}	

//		System.out.println("1: x=" + xpos + " y=" + ypos + " m_curLayout=" + m_curLayout + " plyt=" + m_plyt + " new={" + x1 +"," + y1 + "," + width1 + "," + height1);

		if (width1 < MIN_DIM) {
			x1      = m_curLayout.x;
			width1  = m_curLayout.width;
		}
		if (height1 < MIN_DIM) {
			y1      = m_curLayout.y;
			height1 = m_curLayout.height;
		}

		setCurLayout(x1,y1, width1, height1);
	}


	//
	// Public methods
	//

	public ResizeModeHandler(EditModeHandler	parent) 
	{
		super(parent.m_ls);
		m_parent      = parent;
		m_drawOutline = new DrawOutline();
	}

	public Rectangle getCurLayout()
	{
		return(m_curLayout);
	}

	//
	//	 ++-----------++
	//	 +			   +   
	//	 |			   |  Corner or edge grab
	//	 |			   | 
	//	 +			   +
	//	 ++-----------++
	//

	public void cleanup()
	{
		if (m_curLayout != null) {
			Diagram diagram = m_ls.getDiagram();
			diagram.remove(m_drawOutline);
			m_curLayout = null;
		}
		if (m_cursor != Cursor.DEFAULT_CURSOR) {
			m_cursor = Cursor.DEFAULT_CURSOR;
			m_ls.setCursor(m_cursor);
		}
		m_resizeMode = RM_NONE;
	}

	public void movedOverThing(MouseEvent ev, Object thing, int x, int y) 
	{
		m_zn = overResizeTab(thing, x, y);

		if (m_parent.getSubHandler() != this) {
			if (m_zn != EntityInstance.RSZ_NONE) {
				m_cursor = resizeCursor[m_zn];
				m_ls.setCursor(m_cursor);
				m_parent.setSubHandler(this);
			}
		} else {
			if (m_zn == EntityInstance.RSZ_NONE) {
				m_parent.cleanup();
	}	}	}

	public void entityPressed(MouseEvent ev, EntityInstance e, int x, int y) 
	{
		movedOverThing(ev, e, x, y);
		if (m_zn == EntityInstance.RSZ_NONE) {
			return;
		}

		Diagram diagram = m_ls.getDiagram();
		
		if (diagram == null) {
			return;
		}

		m_resizeMode = startMode[m_zn];
		setCurLayout(e.getDiagramX(), e.getDiagramY(), e.getWidth(), e.getHeight());

		if (m_plyt == null) {
			m_plyt = new Rectangle();
		} 

		EntityInstance parent;

		parent = e.getContainedBy();
		if (parent != null && parent.getContainedBy() != null) {
			Rectangle plyt;

			plyt          = parent.getDiagramBounds();
			m_plyt.x	  = plyt.x;
			m_plyt.y	  = plyt.y;
			m_plyt.width  = plyt.width;
			m_plyt.height = plyt.height;
		} else {
			m_plyt	      = diagram.getBounds();
			m_plyt.x      = 0;
			m_plyt.y      = 0;
		}
		m_ls.doFeedback("Size: " + m_curLayout.width + " x " + m_curLayout.height);
	}

	public void entityDragged(MouseEvent ev, EntityInstance e, int x, int y) 
	{
		double xpos = x;
		double ypos = y;

		if (m_resizeMode == RM_NONE && m_curLayout != null) {

			// See if we have reached an edge

			boolean l = (xpos <= m_curLayout.x);
			boolean r = (xpos >= m_curLayout.x + m_curLayout.width);
			boolean t = (ypos <= m_curLayout.y);
			boolean b = (ypos >= m_curLayout.y + m_curLayout.height);
			if (e.isDrawRoot()) {
				l = false;		// Can't change left
				t = false;		// Can't change top
			}

			if (l) {
				if (t || ypos-m_curLayout.y < 4) {
					m_resizeMode = RM_LEFT | RM_TOP;
				} else if (b || (m_curLayout.y + m_curLayout.height - ypos < 4)) {
					m_resizeMode = RM_LEFT | RM_BOTTOM;
				} else {
					m_resizeMode = RM_LEFT;
				}
			} else if (r) {
				if (t || ypos-m_curLayout.y < 4) {
					m_resizeMode = RM_RIGHT | RM_TOP;
				} else if (b || (m_curLayout.y + m_curLayout.height - ypos < 4)) {
					m_resizeMode = RM_RIGHT | RM_BOTTOM;
				} else {
					m_resizeMode = RM_RIGHT;
				}
			} else if (t) {
				if (xpos-m_curLayout.x < 4) {
					m_resizeMode = RM_TOP | RM_LEFT;
				} else if (m_curLayout.x + m_curLayout.width - xpos < 4) {
					m_resizeMode = RM_TOP | RM_RIGHT;
				} else {
					m_resizeMode = RM_TOP;
				}
			} else if (b) {
				if (xpos-m_curLayout.x < 4) {
					m_resizeMode = RM_BOTTOM | RM_LEFT;
				} else if (m_curLayout.x + m_curLayout.width - xpos < 4) {
					m_resizeMode = RM_BOTTOM | RM_RIGHT;
				} else {
					m_resizeMode = RM_BOTTOM;
			}	}
		}

		if (m_resizeMode != RM_NONE) {
			doResizeAdjust(m_resizeMode, x, y);
			m_ls.doFeedback("Size: " + m_curLayout.width + " x " + m_curLayout.height);
		}
	}

	public void entityReleased(MouseEvent ev, EntityInstance e, int x, int y) 
	{
		if (e != null) {
			Diagram diagram = m_ls.getDiagram();

			diagram.resizeEntity(e, m_curLayout);
		}
		m_ls.clearFeedback();
		m_parent.cleanup();
	}


}

