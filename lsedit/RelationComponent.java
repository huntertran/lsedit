package lsedit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.awt.geom.AffineTransform;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;

class DrawInfo extends Object {

	protected final static Color  CENTRE_MARK_COLOR      = Color.yellow;
	protected final static int	  CENTRE_MARK_DIM	     = 4;

	protected final static double NEAR_POINT_THRESHOLD = 1.0;
	protected final static double NEAR_LINE_THRESHOLD2 = 1.0;		// Square of threshold to avoid using sqrt

	protected RelationComponent	m_component;
	protected int				m_style = 0;
	protected int				m_embellished;

	// -----------------
	// Component methods
	// -----------------

	public void		getBounds(Rectangle r)
	{
		r.setBounds(0,0,0,0);
	}
	
	public boolean contains(int x, int y)	// (x,y) w.r.t diagram
	{
		return false;
	}

	public void paintComponent(Graphics g, int shiftX, int shiftY)
	{
	}

	// ----------------
	// DrawInfo methods
	// ----------------

	protected static void getLineBounds(int srcX, int srcY, int dstX, int dstY, Rectangle r)
	{
		int x1, y1, x2, y2, x, y, linewidth;
		

		if (srcX <= dstX) {
			x1 = srcX;
			x2 = dstX;
		} else {
			x1 = dstX;
			x2 = srcX;
		}
		if (srcY <= dstY) {
			y1 = srcY;
			y2 = dstY;
		} else {
			y1 = dstY;
			y2 = srcY;
		}
		linewidth = Options.getLineWidth();
		if (linewidth > 1) {
			int w, h, shift;

			w     = x2 - x1;
			h     = y2 - y1;
			shift = linewidth / 2;

				
			if (h > 2*w) {
				x1 -= shift;
				x2 += shift;
			}
			if (w >= 2*h) {
				y1 -= shift;
				y2 += shift;
			}
		}
		r.setBounds(x1, y1, x2-x1, y2-y1);
	}
	
	protected static void includeTailBounds(int srcX, int srcY, Rectangle r)
	{
		int x1, y1, x2, y2, x, y;

		x1 = r.x;
		y1 = r.y;
		x2 = x1 + r.width;
		y2 = y1 + r.height;

		x = srcX - RelationInstance.NEAR_PIXEL_SIZE/2;
		y = srcY - RelationInstance.NEAR_PIXEL_SIZE/2;

		if (x < x1) {
			x1 = x;
		}
		if (y < y1) {
			y1 = y;
		} 
		x += RelationInstance.NEAR_PIXEL_SIZE;
		y += RelationInstance.NEAR_PIXEL_SIZE;

		if (x > x2) {
			x2 = x;
		}
		if (y > y2) {
			y2 = y;
		}
		r.setBounds(x1, y1, x2-x1, y2-y1);
	}

	protected static void includeCentreBounds(int srcX, int srcY, int dstX, int dstY, Rectangle r)
	{
		int x1, y1, x2, y2, x, y;

		x1 = r.x;
		y1 = r.y;
		x2 = x1 + r.width;
		y2 = y1 + r.height;

		x = (srcX + dstX - CENTRE_MARK_DIM)/2;
		y = (srcY + dstY - CENTRE_MARK_DIM)/2;

		if (x < x1) {
			x1 = x;
		}
		if (y < y1) {
			y1 = y;
		}
		x += CENTRE_MARK_DIM;
		y += CENTRE_MARK_DIM;
		if (x > x2) {
			x2 = x;
		}
		if (y > y2) {
			y2 = y;
		}
		r.setBounds(x1, y1, x2-x1, y2-y1);
	}

	protected void includeArrowBounds(int srcX, int srcY, int dstX, int dstY, Rectangle r)
	{
		int					x1, y1, x2, y2, x, y;
		RelationInstance	ri   = m_component.getRelationInstance();

		x1 = r.x;
		y1 = r.y;
		x2 = x1 + r.width;
		y2 = y1 + r.height;

		int[] x3 = new int[3];
		int[] y3 = new int[3];

		if (Util.getArrow(srcX, srcY, dstX, dstY, x3, y3, ri.getFrequency()) != 0) {
			int i;

			for (i = 0; i < 3; ++i) {
				x = x3[i];
				y = y3[i];

				if (x < x1) {
					x1 = x;
				}
				if (y < y1) {
					y1 = y;
				}
				if (x > x2) {
					x2 = x;
				}
				if (y > y2) {
					y2 = y;
		}	}	}
		r.setBounds(x1, y1, x2-x1, y2-y1);
	}

	// Test to see if (x,y) is near line from (x1,y1) to (x2,y2)

	protected static boolean nearPoint(double srcX, double srcY, int x, int y) 
	{
		double diff;

		diff = ((double) x)-srcX;
		if (diff < 0) {
			diff = -diff;
		}
		if (diff > NEAR_POINT_THRESHOLD) {
			return(false);
		}
		diff = ((double) y)-srcY;
		if (diff < 0) {
			diff = -diff;
		}
		if (diff > NEAR_POINT_THRESHOLD) {
			return(false);
		}
		return(true);
	} 

	protected static boolean nearLine(double srcX, double srcY, double dstX, double dstY, int x, int y) 
	{
		if (srcX == dstX && srcY == dstY) {
			// Degenerate case line becomes a point
			return(nearPoint(srcX, srcY, x, y));
		}
		
		// Determine distance to line and point R(x, y)
		//
		// Using parametric form for equation of line
		// L = P0 + tV	where V is vector from P0 to P1
		//
		// Find t such that vector R -> P0 + tV is orthogonal to V.
		// We then restrict to checking distances to
		// segment P0, P1 (0 < t < 1). 

		double rx = x;
		double ry = y;
		double vx = dstX - srcX;
		double vy = dstY - srcY; 
		
		double t = ((rx - srcX) * vx + (ry - srcY) * vy) / (vx*vx + vy*vy);

		if (t >= 0 && t <= 1.0) {
			// Only check for segment between P0 and P1
			double px = srcX + t * vx;
			double py = srcY + t * vy; 
			double lx = (rx - px);
			double ly = (ry - py);

			double dist2 = lx*lx + ly*ly;

			if (dist2 <= NEAR_LINE_THRESHOLD2) {
				return(true);
			}
//			System.out.println("farLine: " + dist2 + ".v." + NEAR_LINE_THRESHOLD2);
		}	
		return(false);
	}	

	public void setStyle(int style)
	{
		m_style = style;
	} 

	public void setEmbellished(int value)
	{
		m_embellished = value;
	}

	public boolean orEmbellished(int value)
	{
		if ((m_embellished & value) != value) {
			m_embellished |= value;
			return true;
		}
		return false;
	}

	public boolean nandEmbellished(int value)
	{
		if ((m_embellished & value) != 0) {
			m_embellished &= ~value;
			return true;
		}
		return false;
	}

	public boolean mouseOverEdgePoint(int x, int y, MoveModeHandler handler)
	{
		return false;
	}

	public boolean isFramedBy(Rectangle frame)
	{
		return false;
	}

	public void switchEdgePoint(EdgePoint oldPoint, EdgePoint newPoint)
	{
	}
}

class ArcInfo extends DrawInfo {

	protected final static int  RECURSIVE_LOOP_DOTTED_ANGLE = 8;
	protected final static int  RECURSIVE_LOOP_DOTTED_SPACE = 10;

	int					m_x;		// x coordinate of center of arc w.r.t diagram
	int					m_y;		// y coordinate of center of arc w.r.t diagram
	int					m_x_radius;
	int					m_y_radius;
	int					m_angle;	// angle in units of 90 degrees
	boolean				m_isElided;

	// -----------------
	// Component methods
	// -----------------

	public void getBounds(Rectangle r)
	{
		r.setBounds(m_x-m_x_radius, m_y-m_y_radius, m_x_radius*2, m_y_radius*2);
	}

	public boolean contains(int x, int y)		// (x,y) w.r.t. diagram
	{
		int		x1, y1;
		int		y2;
		double	d1, d2;

		// Transform axis so that over centre of arc
	
		y1 = y - m_y;
		x1 = x - m_x;

		switch (m_angle) {
		case 0:				// Center top left
			if (y1 >= 0 && x1 >= 0) {
				return(false);
			}
			break;
		case 1:				// Center bottom left
			if (y1 <= 0 && x1 >= 0) {
				return(false);
			}
			break;
		case 2:				// Center bottom right
			if (x1 <= 0 && y1 <= 0) {
				return(false);
			}
			break;
		default:			// Center top right
			if (x1 <= 0 && y1 >= 0) {
				return(false);
			}
			break;
		}

		/* Formula for ellipse (treating center of box as (0,0) 
		 *
		 * (x/xradius)^2 + (y/yradius)^2 = 1
		 *
		 * y^2 = (1 - (x/xradius)^2)*yradius^2
		 */

		d1  = ((double) x1)/((double) m_x_radius);
		d2  = ((double) y1)/((double) m_y_radius);
		d1  = (d1*d1) + (d2*d2);

		if (d1 > .95 && d1 < 1.05) {
			return(true);
		}
		return(false);
	}

	// ------------------
	// JComponent methods
	// ------------------


	public void paintComponent(Graphics g, int shiftX, int shiftY)
	{
		int embellished = m_embellished;

		int	x1, y1, x2, y2;
		int sweep;

		if (m_isElided) {
			sweep = 45;
		} else {
			sweep = 270;
		}

		if (m_style != Util.LINE_STYLE_DOTTED) {
			g.drawArc(0, 0, m_x_radius*2, m_y_radius*2, m_angle*90, sweep);
		} else {
			int	i;

			for (i = m_angle*90; i < m_angle*90 + sweep; i += RECURSIVE_LOOP_DOTTED_ANGLE+RECURSIVE_LOOP_DOTTED_SPACE) {
				g.drawArc(0, 0, m_x_radius*2, m_y_radius*2, i /* Start angle */, RECURSIVE_LOOP_DOTTED_ANGLE);
		}	}

		if (m_component.getDirection() != LandscapeClassObject.DIRECTION_NONE) {
			switch (m_angle) {
			case 0:
				x1 = 2*m_x_radius-1;
				y1 = 0;
				x2 = x1;
				y2 = m_y_radius-1;
				break;
			case 1:
				x1 = 2*m_x_radius-1;
				y1 = 2*m_y_radius-1;
				x2 = x1;
				y2 = m_y_radius-1;
				break;
			case 2:
				x1 = 0;
				y1 = 2*m_y_radius-1;
				x2 = 0;
				y2 = m_y_radius-1;
				break;
			default:
				x1 = 0;
				y1 = 0;
				x2 = 0;
				y2 = m_y_radius-1;
				break;
			}
			Util.drawArrowHead(g, x1, y1, x2, y2, 1); // For arc info
		}

		if ((embellished & RelationComponent.DRAW_CENTRE_MARK) != 0) {
			Color tc = g.getColor();
		
			g.setColor(CENTRE_MARK_COLOR);
			if (m_angle < 2) {
				x1 = 0;
			} else {
				x1 = 2 * m_x_radius - CENTRE_MARK_DIM;
			}
			g.fillRect(x1, m_y_radius-(CENTRE_MARK_DIM/2), CENTRE_MARK_DIM, CENTRE_MARK_DIM);
		}
	}

	// ---------------
	// ArcInfo methods
	// ---------------

	public ArcInfo(RelationComponent component)
	{
		m_component = component;
	}

	public void setElided(boolean elided)
	{
		m_isElided = elided;
	}

	public void computePosition()
	{
		RelationInstance ri	 	 = m_component.getRelationInstance();
		RelationClass	 rc      = ri.getRelationClass();
		int				 nid	 = rc.getNid();
		EntityInstance	 drawSrc = ri.getDrawSrc();
		int				 w       = drawSrc.getWidth();
		int			 	 h       = drawSrc.getHeight();
		int				 x1, y1, x2, y2;

		switch (nid / 8) {
		case 0:				// Center top right
			x1      = w;
			y1      = 0;	
			m_angle = 3;	// 90 * 3 = 270
			break;
		case 1:				// Center bottom right
			x1      = w;
			y1      = h;		
			m_angle = 2;	// 90 * 2 = 180
			break;
		case 2:				// Center bottom left
			x1      = 0;
			y1      = h;		
			m_angle = 1;	// 90 * 1 = 90
			break;
		default:			// Center top left
			x1      = 0;
			y1      = 0;	
			m_angle = 0;
		}

		nid = (nid % 8) + 1;

		m_x = x1 + drawSrc.getDiagramX();
		m_y = y1 + drawSrc.getDiagramY();
		m_x_radius = ((nid * w) / 36);
		m_y_radius = ((nid * h) / 36);
	}
}	

class ElidedInfo extends DrawInfo {

	// -----------------
	// Component methods
	// -----------------

	public void getBounds(int srcX, int srcY, int dstX, int dstY, Rectangle r)
	{
		getLineBounds(srcX, srcY, dstX, dstY, r);
		includeArrowBounds(srcX, srcY, dstX, dstY, r);
	}

	// ------------------
	// JComponent methods
	// ------------------

	public void paintComponent(Graphics g, int srcX, int srcY, int dstX, int dstY)
	{
		// Draw an arrow head with the arrow at the end (since elided)

		double	fraction = Util.drawArrowHead(g, srcX, srcY, dstX, dstY, 1);
		dstX -= (int) (((double) (dstX-srcX)) * fraction);
		dstY -= (int) (((double) (dstY-srcY)) * fraction);
		Util.drawSegment(g, m_style, srcX, srcY, dstX, dstY);
	}
	
	// Overridden
	
	public void setElidedInfo(EntityInstance srcEntity, EdgePoint srcPt, EntityInstance dstEntity, EdgePoint dstPt)
	{
	}
}

class PeerElidedInfo extends ElidedInfo {

	protected	EntityInstance	m_srcEntity;
	protected	EdgePoint		m_src;
	protected   int				m_dstX;
	protected	int				m_dstY;

	// -----------------
	// Component methods
	// -----------------

	public void getBounds(Rectangle r)
	{
		EntityInstance	srcEntity = m_srcEntity;
		EdgePoint		src       = m_src;

		getBounds((int) srcEntity.getEdgePointX(src), (int) srcEntity.getEdgePointY(src), m_dstX, m_dstY, r);
	}

	// Test to see if (x,y) is near line from (x1,y1) to (x2,y2)

	public boolean contains(int x, int y) // (x,y) w.r.t diagram
	{
		EntityInstance	srcEntity = m_srcEntity;
		EdgePoint		src       = m_src;

		return(nearLine(srcEntity.getEdgePointX(src), srcEntity.getEdgePointY(src), (double) m_dstX, (double) m_dstY, x, y));
	}

	// ------------------
	// JComponent methods
	// ------------------

	public void paintComponent(Graphics g, int shiftX, int shiftY)
	{
		EntityInstance	srcEntity = m_srcEntity;
		EdgePoint		src       = m_src;
		int				srcX      = (int) (srcEntity.getEdgePointX(src) - shiftX);
		int				srcY      = (int) (srcEntity.getEdgePointY(src) - shiftY);
		int				dstX      = m_dstX  - shiftX;
		int				dstY      = m_dstY  - shiftY;

		Util.drawSegment(g, m_style, srcX, srcY, dstX, dstY);
	}

	// ---------------------
	// OutElidedInfo methods
	// ---------------------
	
	public PeerElidedInfo(RelationComponent component)
	{
		m_component = component;
	}

	public void setElidedInfo(EntityInstance srcEntity, EdgePoint srcPt, EntityInstance dstEntity, EdgePoint dstPt)
	{
		double srcX        = srcEntity.getEdgePointX(srcPt);
		double srcY        = srcEntity.getEdgePointY(srcPt);
		double dx          = srcX - dstEntity.getEdgePointX(dstPt);
		double dy          = srcY - dstEntity.getEdgePointY(dstPt);
		double df          = Math.sqrt(dx*dx + dy*dy);
		double arrowLength = Options.getArrowLength();

		// Make line length twice arrow length

		if (df == 0.0) {
			m_dstX = (int) (srcX - 2*arrowLength);
			m_dstY = (int) (srcY - 2*arrowLength);
		} else {
			m_dstX = (int) (srcX - dx*2*arrowLength/df);
			m_dstY = (int) (srcY - dy*2*arrowLength/df);
		}

		m_srcEntity = srcEntity;
		m_src       = srcPt;
	}

	public boolean mouseOverEdgePoint(int x, int y, MoveModeHandler handler)
	{
		EntityInstance	srcEntity = m_srcEntity;
		EdgePoint		src       = m_src;

		if (nearPoint(srcEntity.getEdgePointX(src), srcEntity.getEdgePointY(src), x, y)) {
			RelationInstance ri = m_component.getRelationInstance();

			handler.overEdgePointCallBack(srcEntity, ri, src);
			return true;
		}
		return false;
	}

	public void switchEdgePoint(EdgePoint oldPoint, EdgePoint newPoint)
	{
		if (m_src == oldPoint) {
			m_src = newPoint;
	}	}
	
	public String toString()
	{
		return "Peer elided " + m_srcEntity.getLabel();
	}
}

class OutElidedInfo extends PeerElidedInfo {

	public void paintComponent(Graphics g, int shiftX, int shiftY)
	{
		EntityInstance	srcEntity = m_srcEntity;
		EdgePoint		src       = m_src;
		int				srcX      = (int) (srcEntity.getEdgePointX(src) - shiftX);
		int				srcY      = (int) (srcEntity.getEdgePointY(src) - shiftY);
		int				dstX      = m_dstX  - shiftX;
		int				dstY      = m_dstY  - shiftY;

		// Draw an arrow head with the arrow at the end (since elided)

		double			fraction  = Util.drawArrowHead(g, srcX, srcY, dstX, dstY, 1);

		dstX -= (int) (((double) (dstX-srcX)) * fraction);
		dstY -= (int) (((double) (dstY-srcY)) * fraction);
		Util.drawSegment(g, m_style, srcX, srcY, dstX, dstY);
	}

	// ---------------------
	// OutElidedInfo methods
	// ---------------------
	
	public OutElidedInfo(RelationComponent component)
	{
		super(component);
	}
	
	public String toString()
	{
		return "Out elided " + m_srcEntity.getLabel();
	}
}

class InElidedInfo extends ElidedInfo {

	private EntityInstance	m_dstEntity;
	private EdgePoint		m_dst;
	int						m_srcX;
	int						m_srcY;

	// -----------------
	// Component methods
	// -----------------

	public void getBounds(Rectangle r)
	{
		EntityInstance	dstEntity = m_dstEntity;
		EdgePoint		dst       = m_dst;

		getBounds(m_srcX, m_srcY, (int) dstEntity.getEdgePointX(dst), (int) dstEntity.getEdgePointY(dst), r);
	}

	// Test to see if (x,y) is near line from (x1,y1) to (x2,y2)

	public boolean contains(int x, int y) // (x,y) w.r.t diagram
	{
		EntityInstance	dstEntity = m_dstEntity;
		EdgePoint		dst       = m_dst;

		return(nearLine((double) m_srcX, (double) m_srcY, dstEntity.getEdgePointX(dst), dstEntity.getEdgePointY(dst), x, y));
	}

	// ------------------
	// JComponent methods
	// ------------------

	public void paintComponent(Graphics g, int shiftX, int shiftY)
	{
		EntityInstance	dstEntity = m_dstEntity;
		EdgePoint		dst       = m_dst;
		int				srcX      = m_srcX - shiftX;
		int				srcY      = m_srcY - shiftY;
		int				dstX      = (int) (dstEntity.getEdgePointX(dst) - shiftX);
		int				dstY      = (int) (dstEntity.getEdgePointY(dst) - shiftY);

		// Draw an arrow head with the arrow at the end (since elided)

		double			fraction  = Util.drawArrowHead(g, srcX, srcY, dstX, dstY, 1);
		
		dstX -= (int) (((double) (dstX-srcX)) * fraction);
		dstY -= (int) (((double) (dstY-srcY)) * fraction);
		Util.drawSegment(g, m_style, srcX, srcY, dstX, dstY);
	}

	// --------------------
	// InElidedInfo methods
	// --------------------

	public InElidedInfo(RelationComponent component)
	{
		m_component = component;
	}

	public void setElidedInfo(EntityInstance srcEntity, EdgePoint srcPt, EntityInstance dstEntity, EdgePoint dstPt)
	{
		double dstX = dstEntity.getEdgePointX(dstPt);
		double dstY = dstEntity.getEdgePointY(dstPt);
		double dx   = srcEntity.getEdgePointX(srcPt) - dstX;
		double dy   = srcEntity.getEdgePointY(srcPt) - dstY;
		double df   = Math.sqrt(dx*dx + dy*dy);
		double arrowLength = Options.getArrowLength();
		   
		// Make line length twice arrow length

		if (df == 0.0) {
			m_srcX = (int) (dstX + (2*arrowLength));
			m_srcY = (int) (dstY + (2*arrowLength));
		} else {
			m_srcX = (int) (dstX + (dx*2*arrowLength/df));
			m_srcY = (int) (dstY + (dy*2*arrowLength/df));
		}
		m_dstEntity = dstEntity;
		m_dst       = dstPt;
	}

	public boolean mouseOverEdgePoint(int x, int y, MoveModeHandler handler)
	{
		EntityInstance	dstEntity = m_dstEntity;
		EdgePoint		dst       = m_dst;

		if (nearPoint(dstEntity.getEdgePointX(dst), dstEntity.getEdgePointY(dst), x, y)) {
			RelationInstance ri = m_component.getRelationInstance();

			handler.overEdgePointCallBack(dstEntity, ri, dst);
			return true;
		}
		return false;
	}

	public void switchEdgePoint(EdgePoint oldPoint, EdgePoint newPoint)
	{
		if (m_dst == oldPoint) {
			m_dst = newPoint;
	}	}
	
	public String toString()
	{
		return "In elided " + m_dstEntity.getLabel();
	}
}

class EdgeInfo extends DrawInfo {

	private	EntityInstance	m_srcEntity;
	private EdgePoint		m_src;
	private EntityInstance	m_dstEntity;
	private EdgePoint		m_dst;

	// --------------
	// Object methods
	// --------------

	public String toString()
	{
		return "EdgeInfo: " + m_src + "->" + m_dst + " style=" + m_style + " embellished=" + m_embellished;
	}

	// -----------------
	// Component methods
	// -----------------

	public void getBounds(Rectangle r)
	{
		int	x1 = getSrcX();
		int y1 = getSrcY();
		int x2 = getDstX();
		int y2 = getDstY();

		getLineBounds(x1, y1, x2, y2, r);

		if ((m_embellished & RelationComponent.DRAW_TAIL_MARK) != 0) {
			includeTailBounds(x1, y1, r);
		}
		if ((m_embellished & RelationComponent.DRAW_CENTRE_MARK) != 0) {
			includeCentreBounds(x1, y1, x2, y2, r);
		}
		if ((m_embellished & RelationComponent.DRAW_ARROW_MARK) != 0) {
			includeArrowBounds(x1, y1, x2, y2, r);
		}
		if ((m_embellished & RelationComponent.DRAW_LABEL) != 0) {
			RelationLabel label = m_component.getRelationLabel();

			label.place(x1, y1, x2, y2);
		}
	}

	// Test to see if (x,y) is near line from (x1,y1) to (x2,y2)

	public boolean contains(int x, int y) // (x,y) w.r.t diagram
	{
		EntityInstance srcEntity = m_srcEntity;
		EdgePoint	   src       = m_src;
		EntityInstance dstEntity = m_dstEntity;
		EdgePoint	   dst       = m_dst;

//		System.out.println(m_component.getRelationInstance().toString() + "(" + (int) srcEntity.getEdgePointX(src) + "," + (int) srcEntity.getEdgePointY(src) + "x" + (int) dstEntity.getEdgePointX(dst) + "," + (int) dstEntity.getEdgePointY(dst) + ") .v. (" + x + "," + y + ")");

		return(nearLine(srcEntity.getEdgePointX(src), srcEntity.getEdgePointY(src), dstEntity.getEdgePointX(dst), dstEntity.getEdgePointY(dst), x, y));
	}

	// ------------------
	// JComponent methods
	// ------------------

	public void paintComponent(Graphics g, int shiftX, int shiftY)
	{
		int	embellished = m_embellished;

		EntityInstance srcEntity = m_srcEntity;
		EdgePoint	   src       = m_src;
		EntityInstance dstEntity = m_dstEntity;
		EdgePoint	   dst       = m_dst;
		int			   direction = m_component.getDirection();

		int srcX   = (int) (srcEntity.getEdgePointX(src) - shiftX);
		int srcY   = (int) (srcEntity.getEdgePointY(src) - shiftY);
		int dstX   = (int) (dstEntity.getEdgePointX(dst) - shiftX);
		int dstY   = (int) (dstEntity.getEdgePointY(dst) - shiftY);
		int arrowX = dstX;	// Head of the arrow (if one)
		int arrowY = dstY;
		
/*
		if (srcEntity.getId().equals("client")) {
			System.out.println("EdgeInfo.paintComponent parent = " + m_component.getRelationInstance().getParentClass() + " style = " + m_style );
		}	
*/		
		if ((embellished & RelationComponent.DRAW_ARROW_MARK) != 0 && direction != LandscapeClassObject.DIRECTION_NONE) {
			Option	option   = Options.getDiagramOptions();
			int     weight   = arrowWeight();
			Color	oldColor = g.getColor();
			double  fraction;
			

			if (option.isCenterArrowhead()) {
				arrowX = (srcX + dstX) / 2;
				arrowY = (srcY + dstY) / 2;

				Util.drawSegment(g, m_style, arrowX, arrowY, dstX, dstY);
			} 

			if (option.isVariableArrowColor()) {
				RelationInstance ri       = m_component.getRelationInstance();
				Color			 newColor = ri.getInheritedArrowColor();
				if (newColor != null) {
					g.setColor(newColor);
			}	}

			fraction = Util.drawArrowHead(g, srcX, srcY, arrowX, arrowY, weight);
			g.setColor(oldColor);

			arrowX  -= (int) (((double) (arrowX-srcX)) * fraction);
			arrowY  -= (int) (((double) (arrowY-srcY)) * fraction);
		}
		Util.drawSegment(g, m_style, srcX, srcY, arrowX, arrowY);

		if ((embellished & RelationComponent.DRAW_TAIL_MARK) != 0) {
			g.fillOval(srcX-RelationInstance.NEAR_PIXEL_SIZE/2, srcY-RelationInstance.NEAR_PIXEL_SIZE/2, RelationInstance.NEAR_PIXEL_SIZE, RelationInstance.NEAR_PIXEL_SIZE);
		}
		if ((embellished & RelationComponent.DRAW_CENTRE_MARK) != 0) {
			Color tc = g.getColor();
		
			g.setColor(CENTRE_MARK_COLOR);
			g.fillRect((srcX + dstX - CENTRE_MARK_DIM)/2, (srcY + dstY - CENTRE_MARK_DIM)/2, CENTRE_MARK_DIM, CENTRE_MARK_DIM);
			g.setColor(tc);
	}	}

	// ----------------
	// EdgeInfo methods
	// ----------------

	public EdgeInfo(RelationComponent component)
	{
		m_component = component;
	}

	public void setEdgeInfo(EntityInstance srcEntity, EdgePoint src, EntityInstance dstEntity, EdgePoint dst)
	{
//			System.out.println("EdgeInfo: " + srcEntity.getId() + src + dstEntity.getId() + dst);
/*
		if (src == null) {
			System.out.println("Source edgeInfo is null");
		}
 		if (dst == null) {
			System.out.println("Destination edgeInfo is null");
		}
*/
		m_srcEntity = srcEntity;
		m_src       = src;
		m_dstEntity = dstEntity;
		m_dst       = dst;
	}

	public int getSrcX()
	{
		return (int) m_srcEntity.getEdgePointX(m_src);
	}

	public int getSrcY()
	{
		return (int) m_srcEntity.getEdgePointY(m_src);
	}

	public int getDstX()
	{
		return (int) m_dstEntity.getEdgePointX(m_dst);
	}

	public int getDstY()
	{
		return (int) m_dstEntity.getEdgePointY(m_dst);
	}

	public boolean mouseOverEdgePoint(int x, int y, MoveModeHandler handler)
	{
		EntityInstance entity    = m_srcEntity;
		EdgePoint	   edgePoint = m_src;

		if (nearPoint(entity.getEdgePointX(edgePoint), entity.getEdgePointY(edgePoint), x, y)) {
			RelationInstance ri = m_component.getRelationInstance();

			handler.overEdgePointCallBack(entity, ri, edgePoint);
			return true;
		}

		entity    = m_dstEntity;
		edgePoint = m_dst;

		if (nearPoint(entity.getEdgePointX(edgePoint), entity.getEdgePointY(edgePoint), x, y)) {
			RelationInstance ri = m_component.getRelationInstance();

			handler.overEdgePointCallBack(entity, ri, edgePoint);
			return true;
		}
		return false;
	}

	public boolean isFramedBy(Rectangle frame)
	{
		EntityInstance srcEntity = m_srcEntity;
		EdgePoint	   src       = m_src;
		EntityInstance dstEntity = m_dstEntity;
		EdgePoint	   dst       = m_dst;

		return frame.intersectsLine(srcEntity.getEdgePointX(src), srcEntity.getEdgePointY(src), dstEntity.getEdgePointX(dst), dstEntity.getEdgePointY(dst));
	}

	protected int arrowWeight()
	{
		RelationInstance ri = m_component.getRelationInstance();
		if (LandscapeObject.g_infoShown != ri) {
			Option option = Options.getDiagramOptions();
			
			if (!option.isPermanentlyWeight()) {
				return 1;
		}	}
		return ri.getFrequency();
	}

	public void switchEdgePoint(EdgePoint oldPoint, EdgePoint newPoint)
	{
		if (m_src == oldPoint) {
			m_src = newPoint;
		}
		if (m_dst == oldPoint) {
			m_dst = newPoint;
	}	}
}

public class RelationComponent extends JComponent implements MouseListener, MouseMotionListener   
{
	public final static int DRAW_TAIL_MARK	 = 0x01;
	public final static int DRAW_CENTRE_MARK = 0x02;
	public final static int DRAW_ARROW_MARK  = 0x04;
	public final static int DRAW_LABEL       = 0x08;

	// Properties

	private static int[]  m_arc_xp = {0, 0, 0};
	private static int[]  m_arc_yp = {0, 0, 0};

	private RelationInstance	m_relationInstance;
	private Object				m_drawInfo;
	private	RelationLabel		m_label;
	private int					m_freq = 1;


	// --------------
	// Object methods
	// --------------

	public String toString() 
	{
		return "RelationComponent: " + m_relationInstance;
	}

	// -----------------
	// Component methods
	// -----------------

	public boolean contains(int x, int y)	// (x,y) w.r.t. my bounds
	{
		if (super.contains(x, y)) {
			// Translate so w.r.t diagram
			x += getX();
			y += getY();
			if (m_drawInfo != null) {
				if (m_drawInfo instanceof DrawInfo) {
					return ((DrawInfo) m_drawInfo).contains(x, y);
				}

				for (Enumeration en = ((Vector) m_drawInfo).elements(); en.hasMoreElements(); ) {
					if ( ((DrawInfo) en.nextElement()).contains(x, y)) {
						return(true);
		}	}	}	}
		return(false);
	}

	// ------------------
	// JComponent methods
	// ------------------

	// Paint the edge

	public void paintComponent(Graphics g)
	{
		int					shiftX, shiftY, x1, y1, x2, y2;

		shiftX = getX();
		shiftY = getY();
			
		g.setColor(m_relationInstance.getInheritedObjectColor());


		/*		For debugging */
		/*
			System.out.println("RelationComponent.paintComponent " + this + " {" + getX() + "," + getY() + "x" + getWidth() + "," + getHeight() + "}");
			g.drawRect(0, 0, getWidth()-1, getHeight()-1);

		if (m_src.getEntityLabel().equals("code")) {
			System.out.println("RelationComponent.paintComponent " + getBounds());
		}
		*/

		if (m_drawInfo != null) {

			if (m_drawInfo instanceof DrawInfo) {
				((DrawInfo) m_drawInfo).paintComponent(g, shiftX, shiftY);
			} else {
				Enumeration en;
				DrawInfo	drawInfo;

				for (en = ((Vector) m_drawInfo).elements(); en.hasMoreElements(); ) {
					drawInfo = (DrawInfo) en.nextElement();
					drawInfo.paintComponent(g, shiftX, shiftY);
	}	}	}	}	

	// -------------------------
	// RelationComponent methods
	// -------------------------

	protected int getDirection()
	{
		RelationClass rc = m_relationInstance.getRelationClass();
		
		return(rc.getShown());
	}
		
	protected LandscapeEditorCore getLs()
	{
		return m_relationInstance.getLs();
	}

	protected Vector computePoints(EntityInstance drawSrc, EntityInstance drawDst, int style) 
	{
		RelationInstance	relationInstance = m_relationInstance;
		RelationClass		rc               = relationInstance.getRelationClass();
		Diagram				diagram			 = rc.getDiagram();
		int					nid              = rc.getNid();
		int					direction		 = rc.getShown();
		EntityInstance		root             = diagram.getDrawRoot();
		ElidedInfo			outElided        = null;
		ElidedInfo			inElided         = null;
		EdgeInfo			edgeInfo;

		// We will travel from the source to the destination
		// going first up then down using as our points of traversal the inflection points

		EntityInstance endUp, endDown;

		boolean client   = !root.hasDescendantOrSelf(drawSrc);
		boolean supplier = !root.hasDescendantOrSelf(drawDst);

		if (!client && !supplier) {
			endUp   = drawSrc.commonAncestor(drawDst); // Common container
			endDown = endUp;
		} else if (client) {
			endUp   = drawSrc.getContainedBy();
			endDown = root.getContainedBy();
		} else {
			endUp   = root.getContainedBy();
			endDown = drawDst.getContainedBy();
		}

		if (endUp == null) {
			System.out.println("C Error: " + relationInstance + " " + client + " " + supplier);
			return null;
		}

		if (endDown == null) {
			System.out.println("S Error: " + relationInstance + " " + client + " " + supplier);
			return null;
		}

		// Not in the same container

		Vector v1 = new Vector();
		Vector v2 = new Vector();

		EntityInstance	e, ept1, ept2, eppt, sp, dp, ep, next;
	    EdgePoint		pt1, pt2, ppt;
   		Option			diagramOptions   = Options.getDiagramOptions();
		int				edgeMode         = diagramOptions.getEdgeMode();
		int				edgeMode1, edgeMode2;
		int				embellished      = 0;


		for (sp = drawSrc; (next = sp.getContainedBy()) != endUp;   sp = next);
		for (dp = drawDst; (next = dp.getContainedBy()) != endDown; dp = next);

		Rectangle spLyt = sp.getDiagramBounds();
		Rectangle dpLyt = dp.getDiagramBounds();

		switch (direction) {
		case LandscapeClassObject.DIRECTION_NONE:
			break;
		case LandscapeClassObject.DIRECTION_NORMAL:
		case LandscapeClassObject.DIRECTION_REVERSED:
			embellished = DRAW_TAIL_MARK;
		default:
			if (relationInstance.getGroupFlag()) {
				embellished |= DRAW_CENTRE_MARK;
		}	}

		/* The following is a bit tricky.
		 *
		 * We build up two vectors of points which represent the
		 * the curve we want the edge to take. These sets are built
		 * one point at a time. 
		 *
		 * The first set builds a curve from the source entity 
		 * to the common container entity. The second set builds
		 * the curve from the destination to the common container.
		 * The second curve then has its points reversed an is 
		 * appended to the first curve. 
		 *
		 * Client/Supplier edge elision complicated things because
		 * it requires the ability to have discontinuous curves.
		 *
		 */

		edgeMode1 = edgeMode2 = -1;
		if (edgeMode == Option.TB_EDGE_STATE) {
			edgeMode1 = EdgePoint.BOTTOM;
			edgeMode2 = EdgePoint.TOP;
		}
		ppt  = pt1  = null;		// Previous point
		eppt = ept1 = null;
		for (e = drawSrc; e != endUp; e = ep) {
			if (e == null) {
				MsgOut.println("Error in points: " + "(" + rc.getId() + " " +  relationInstance + ")");
				return null;
			}

			ep   = e.getContainedBy();
			ept1 = e;
			pt1  = ept1.getOutPoint(rc, edgeMode1, spLyt, dpLyt);

			if (ppt != null) {
				if (e.getElision(EntityInstance.EXITING_ELISION, nid)) {
					outElided = new OutElidedInfo(this);
					outElided.setElidedInfo(ept1, pt1, eppt, ppt);
					outElided.setStyle(style);
					v1.addElement(outElided);
					break;
				}
				edgeInfo = new EdgeInfo(this);
				edgeInfo.setEdgeInfo(eppt, ppt, ept1, pt1);
				edgeInfo.setStyle(style);
				edgeInfo.setEmbellished(embellished);
				embellished &= ~DRAW_TAIL_MARK;
				v1.addElement(edgeInfo);
			}
			ppt  = pt1;
			eppt = ept1;
		}

		// We've found all points from source entity to 
		// the edge of its ancestor in the common container
	
		// Finally, find the points from the destination entity
		// to the edge of its ancestor in the common container.
		// We'll reverse the order later.

		embellished &= ~DRAW_TAIL_MARK;
		
		switch (direction) {
		case LandscapeClassObject.DIRECTION_NORMAL:
		case LandscapeClassObject.DIRECTION_REVERSED:
			embellished |= DRAW_ARROW_MARK;
		}
		ppt  = pt2 = null;
		ept2 = null;
		
		for (e = drawDst; e != endDown; e = ep) {
			ep   = e.getContainedBy();
			ept2 = e;
			pt2  = ept2.getOutPoint(rc, edgeMode2, dpLyt, spLyt);

			if (ppt != null) {
				if (e.getElision(EntityInstance.ENTERING_ELISION, nid)) {
					inElided = new InElidedInfo(this);
					inElided.setElidedInfo(eppt, ppt, ept2, pt2);
					inElided.setStyle(style);
					v2.addElement(inElided);
					break;
				}
				edgeInfo = new EdgeInfo(this);
				edgeInfo.setEdgeInfo(ept2, pt2, eppt, ppt);
				edgeInfo.setStyle(style);
				edgeInfo.setEmbellished(embellished);
				embellished &= ~DRAW_ARROW_MARK;
				v2.addElement(edgeInfo);
			}
			ppt  = pt2;
			eppt = ept2;
		} 

		if (outElided == null && inElided == null) {
			edgeInfo = new EdgeInfo(this);
			edgeInfo.setEdgeInfo(ept1, pt1, ept2, pt2);
			edgeInfo.setStyle(style);
			v1.addElement(edgeInfo);
		} else if (inElided == null) {
			v2.removeAllElements();
		} else if (outElided == null) {
			v1.removeAllElements();
		}

		for (int i = v2.size()-1; i >= 0; i--) {
			v1.addElement(v2.elementAt(i));
		}

/*
		System.out.println("RelationComponent:points " + relationInstance);
		for (int i = 0; i < v1.size(); ++i) {
			edgeInfo = (EdgeInfo) v1.elementAt(i);
			System.out.println(i + ": " + edgeInfo);
		}
 */
		return v1;
	}

	// --------------
	// Public methods
	// --------------

	public RelationComponent(RelationInstance relationInstance)
	{
		Option	option = Options.getDiagramOptions();
		
		setLayout(null);
		m_relationInstance = relationInstance;
		relationInstance.setRelationComponent(this);

		if (option.isShowEdgeLabels()) {
			String label = relationInstance.getRelationLabel();
			if (label != null) {
				Color  color = relationInstance.getInheritedLabelColor();
				m_label = new RelationLabel(label, color);
		}	}

		if (option.isShowEdgeTooltip()) {
			String tip = relationInstance.getRelationTooltip();

			if (tip != null) {
				setToolTipText(tip);
		}	}
			
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void initFrequency()
	{
		m_freq = 1;
	}

	public void incrementFrequency()
	{
		++m_freq;
	}

	public int getFrequency()
	{
		return m_freq;
	}

	public RelationInstance getRelationInstance()
	{
		return m_relationInstance;
	}

	public RelationLabel getRelationLabel()
	{
		return m_label;
	}

	public void orEmbellished(int value)
	{
		boolean change;

		if (m_drawInfo != null) {
			if (m_drawInfo instanceof DrawInfo) {
				change = ((DrawInfo) m_drawInfo).orEmbellished(value);
			} else {
				Enumeration en;
				DrawInfo drawInfo;

				change = false;
				for (en = ((Vector) m_drawInfo).elements(); en.hasMoreElements(); ) {
					drawInfo = (DrawInfo) en.nextElement();
					change |= drawInfo.orEmbellished(value);
			}	}
			if (change) {
				repaint();
	}	}	}

	public void nandEmbellished(int value)
	{
		boolean change;

		if (m_drawInfo != null) {
			if (m_drawInfo instanceof DrawInfo) {
				change = ((DrawInfo) m_drawInfo).nandEmbellished(value);
			} else {
				Enumeration en;
				DrawInfo drawInfo;

				change = false;
				for (en = ((Vector) m_drawInfo).elements(); en.hasMoreElements(); ) {
					drawInfo = (DrawInfo) en.nextElement();
					change |= drawInfo.nandEmbellished(value);
			}	}
			if (change) {
				repaint();
	}	}	}

	public void styleChanged(int value)
	{
		if (m_drawInfo != null) {
			if (m_drawInfo instanceof DrawInfo) {
				((DrawInfo) m_drawInfo).setStyle(value);
			} else {
				Enumeration en;
				DrawInfo drawInfo;

				for (en = ((Vector) m_drawInfo).elements(); en.hasMoreElements(); ) {
					drawInfo = (DrawInfo) en.nextElement();
					drawInfo.setStyle(value);
			}	}
	}	}

	public void switchEdgePoint(EdgePoint oldPoint, EdgePoint newPoint)
	{

		if (m_drawInfo != null) {
			if (m_drawInfo instanceof DrawInfo) {
				((DrawInfo) m_drawInfo).switchEdgePoint(oldPoint, newPoint);
			} else {
				Enumeration en;
				DrawInfo drawInfo;

				for (en = ((Vector) m_drawInfo).elements(); en.hasMoreElements(); ) {
					drawInfo = (DrawInfo) en.nextElement();
					drawInfo.switchEdgePoint(oldPoint, newPoint);
	}	}	}	}

	public void computeBounds(Rectangle r)
	{
		DrawInfo drawInfo;

		if (m_drawInfo == null) {
			r.setBounds(0,0,0,0);
			return;
		} 

		if (m_drawInfo instanceof DrawInfo) {
			((DrawInfo) m_drawInfo).getBounds(r);
		} else {
			Enumeration en;
			int x1,y1,x2,y2, x, y;

			x1 = Integer.MAX_VALUE;
			x2 = Integer.MIN_VALUE;
			y1 = Integer.MAX_VALUE;
			y2 = Integer.MIN_VALUE;

			for (en = ((Vector) m_drawInfo).elements(); en.hasMoreElements(); ) {
				drawInfo = (DrawInfo) en.nextElement();
				drawInfo.getBounds(r);

				x = r.x;
				if (x < x1) {
					x1 = x;
				}
				y = r.y;
				if (y < y1) {
					y1 = y;
				}
				x += r.width;
				y += r.height;
				if (x > x2) {
					x2 = x;
				}
				if (y > y2) {
					y2 = y;
			}	}
			r.setBounds(x1,y1, x2-x1, y2-y1);
		}
	}

	// Compute the coordinates for the edge

	public void fill()
	{
//		System.out.println("RelationComponent.validate " + this);
/*		java.lang.Thread.dumpStack();
*/
		RelationInstance	relationInstance = m_relationInstance;

		if (!relationInstance.isMarked(RelationInstance.VARIOUS_TYPE_MARKS)) {
			return;
		}

		EntityInstance		drawSrc, drawDst;
		int					style;

		drawSrc = relationInstance.getDrawSrc();

		if (drawSrc == null) {
			// We may validate a relation because we have changed its flags, not realising it is no longer in the diagram
			return;
		}

		drawDst = relationInstance.getDrawDst();
		if (drawDst == null) {
			// We may validate a relation because we have changed its flags, not realising it is no longer in the diagram
			return;
		}

		if (relationInstance.getSrc() != drawSrc || relationInstance.getDst() != drawDst) {
			style = Util.LINE_STYLE_DOTTED;
		} else {
			style = relationInstance.getInheritedStyle();
		}

		RelationClass	rc             = relationInstance.getRelationClass();
		int				direction	   = rc.getShown();
		Diagram			diagram		   = rc.getDiagram();
		Option			diagramOptions = Options.getDiagramOptions();
		int				edgeMode       = diagramOptions.getEdgeMode();
		int				embellished    = 0;
		Rectangle		r;
		Enumeration		en;

		switch (direction) {
			case LandscapeClassObject.DIRECTION_NONE:
			{
				break;
			}
			case LandscapeClassObject.DIRECTION_NORMAL:
			case LandscapeClassObject.DIRECTION_REVERSED:
			{
				embellished = DRAW_ARROW_MARK;
			}
			default:
			{	
				if (relationInstance.getGroupFlag()) {
					embellished |= DRAW_CENTRE_MARK;
		}	}	}
			
		if (relationInstance.isMarked(RelationInstance.LOOP_MARK)) {
			// This is a recursive loop from something to itself


			if (m_drawInfo == null || !(m_drawInfo instanceof ArcInfo)) {
				m_drawInfo = new ArcInfo(this);
			}
			((ArcInfo) m_drawInfo).setStyle(style);
			((ArcInfo) m_drawInfo).setEmbellished(embellished);
			((ArcInfo) m_drawInfo).setElided(relationInstance.isMarked(RelationInstance.IN_OUT_ELIDED));

//			System.out.println("RelationComponent:validate " + this + " recursive");
			((ArcInfo) m_drawInfo).computePosition();
		} else if (!relationInstance.isMarked(RelationInstance.IN_OUT_ELIDED) && edgeMode != Option.DIRECT_EDGE_STATE && edgeMode != Option.SIDE_EDGE_STATE && drawSrc.getContainedBy() != drawDst.getContainedBy() && relationInstance.isMarked(RelationInstance.ELISION_MARK)) {
			// This is an ugly path of edges
			m_drawInfo = computePoints(drawSrc, drawDst, style);
		} else {
		
			Rectangle srcLyt    = drawSrc.getDiagramBounds();
			Rectangle dstLyt    = drawDst.getDiagramBounds();
			EdgePoint pt1, pt2;

			pt1 = pt2 = null;
			if (drawSrc.hasDescendant(drawDst)) {
				if (edgeMode == Option.SIDE_EDGE_STATE) {
					pt1 = drawSrc.getLeftOutPoint(relationInstance);
					pt2 = drawDst.getLeftOutPoint(relationInstance);
				} else {
					pt1 = drawSrc.getTopOutPoint(relationInstance);
					pt2 = drawDst.getTopOutPoint(relationInstance);
				}
			} else if (drawDst.hasDescendant(drawSrc)) {
				if (edgeMode == Option.SIDE_EDGE_STATE) {
					pt1 = drawSrc.getRightOutPoint(relationInstance);
					pt2 = drawDst.getRightOutPoint(relationInstance);
				} else {
					pt1 = drawSrc.getBottomOutPoint(relationInstance);
					pt2 = drawDst.getBottomOutPoint(relationInstance);
				}
			} else {
				int	edgeMode1, edgeMode2;
				
				if (edgeMode != Option.TB_EDGE_STATE) {
					edgeMode1 = edgeMode2 = -1;
				} else {
					edgeMode1 = EdgePoint.BOTTOM;
					edgeMode2 = EdgePoint.TOP;
				}
				pt1 = drawSrc.getOutPoint(rc, edgeMode1, srcLyt /* drawSrc.diagramBounds */, dstLyt /* drawDst.diagramBounds */);
				pt2 = drawDst.getOutPoint(rc, edgeMode2, dstLyt /* drawDst.diagramBounds */, srcLyt /* drawSrc.diagramBounds */);
			}

			if (relationInstance.isMarked(RelationInstance.IN_OUT_ELIDED)) {
				OutElidedInfo out = null;
				InElidedInfo  in  = null;
				DrawInfo	  drawInfo;

				if (relationInstance.isMarked(RelationInstance.OUT_ELIDED_MARK)) {
					if (m_drawInfo != null) {
						if (m_drawInfo instanceof OutElidedInfo) {
							out = (OutElidedInfo) m_drawInfo;
						} else if (m_drawInfo instanceof Vector) {
							for (en = ((Vector) m_drawInfo).elements(); en.hasMoreElements(); ) {
								drawInfo = (DrawInfo) en.nextElement();
								if (drawInfo instanceof OutElidedInfo) {
									out = (OutElidedInfo) drawInfo;
									break;
					}	}	}	}
					if (out == null) {
						out = new OutElidedInfo(this);
					}
					// Show that the out edge is elided

					out.setElidedInfo(drawSrc, pt1, drawDst, pt2);
					out.setStyle(relationInstance.getInheritedStyle());
//					System.out.println("RelationComponent:validate " + this + " out elided");
				} 

				if (relationInstance.isMarked(RelationInstance.IN_ELIDED_MARK)) {
					if (m_drawInfo != null) {
						if (m_drawInfo instanceof InElidedInfo) {
							in = (InElidedInfo) m_drawInfo;
						} else if (m_drawInfo instanceof Vector) {
							for (en = ((Vector) m_drawInfo).elements(); en.hasMoreElements(); ) {
								drawInfo = (DrawInfo) en.nextElement();
								if (drawInfo instanceof InElidedInfo) {
									in = (InElidedInfo) drawInfo;
									break;
					}	}	}	}
					if (in == null) {
						in = new InElidedInfo(this);
					}
					// Show that the in edge is elided
	   
					in.setElidedInfo(drawSrc, pt1, drawDst, pt2);
					in.setStyle(relationInstance.getInheritedStyle());
//					System.out.println("RelationComponent:validate " + this + " in elided");
				}

				if (out == null) {
					m_drawInfo = in;
				} else if (in == null) {
					m_drawInfo = out;
				} else {
					if (m_drawInfo instanceof Vector) {
						((Vector) m_drawInfo).removeAllElements();
					} else {
						m_drawInfo = new Vector();
					}
					((Vector) m_drawInfo).addElement(out);
					((Vector) m_drawInfo).addElement(in);
				}
			} else {

				// We will draw a direct edge between drawSrc and drawDst

				if (m_drawInfo == null || !(m_drawInfo instanceof EdgeInfo)) {
					m_drawInfo = new EdgeInfo(this);
				}

				EdgeInfo edgeInfo = (EdgeInfo) m_drawInfo;

				edgeInfo.setEdgeInfo(drawSrc, pt1, drawDst, pt2);

/*
				if (drawSrc.getEntityLabel().equals("code")) {
					System.out.println("RelationInstance code source point " + pt1);
				}
 */
				edgeInfo.setStyle(style);

				if (m_label != null) {
					embellished  |= DRAW_LABEL;
				}

				edgeInfo.setEmbellished(embellished);
		}	} 

		if (m_drawInfo != null) {
			r = new Rectangle();
			computeBounds(r);
			// We don't draw the right and bottom edge
			r.width  += 1;
			r.height += 1;
			setBounds(r);

//			System.out.println("RelationComponent.Bounds " + this + "=" + getBounds());
		}
	}

	public boolean mouseOverEdgePoint(int x, int y, MoveModeHandler handler)
	{
		if (m_drawInfo != null) {
			if (m_drawInfo instanceof DrawInfo) {
				return ((DrawInfo) m_drawInfo).mouseOverEdgePoint(x, y, handler);
			}

			Enumeration en;
			EdgePoint	ep;

			for (en = ((Vector) m_drawInfo).elements(); en.hasMoreElements(); ) {
				if (((DrawInfo) en.nextElement()).mouseOverEdgePoint(x,y, handler)) {
					return true;
		}	}	}
		return(false);
	}

	public boolean isFramedBy(Rectangle frame)
	{
		if (m_drawInfo != null) {
			if (m_drawInfo instanceof DrawInfo) {
				return ((DrawInfo) m_drawInfo).isFramedBy(frame);
			}

			Enumeration en;

			for (en = ((Vector) m_drawInfo).elements(); en.hasMoreElements(); ) {
				if (((DrawInfo) en.nextElement()).isFramedBy(frame)) {
					return true;
		}	}	}
		return(false);
	}

	// MouseListener interface

	public void mouseClicked(MouseEvent e)
	{
	}

	public void mouseEntered(MouseEvent e)
	{
//		System.out.println("RelationComponent.MouseEntered " + this);
		m_relationInstance.mouseEntered();
	}

	public void mouseExited(MouseEvent e)
	{
//		System.out.println("RelationComponent.MouseExited");
		m_relationInstance.mouseExited();
	}

	public void mousePressed(MouseEvent ev)
	{
//		System.out.println("RelationComponent.mousePressed");
		m_relationInstance.mousePressed(ev, ev.getX() + getX(), ev.getY() + getY());
	}

	public void mouseReleased(MouseEvent ev)
	{
//		System.out.println("RelationInstance.mousePressed");
		m_relationInstance.mouseReleased(ev, ev.getX() + getX(), ev.getY() + getY());
	}

	// MouseMotionListener interface

	public void mouseDragged(MouseEvent ev)
	{
//		System.out.println("RelationComponent.mouseDragged");
		m_relationInstance.mouseDragged(ev, ev.getX() + getX(), ev.getY() + getY());
	}

	public void mouseMoved(MouseEvent ev)
	{
//		System.out.println("RelationComponent.MouseMoved " + this);
		m_relationInstance.mouseMoved(ev, ev.getX() + getX(), ev.getY() + getY());
	}
}
