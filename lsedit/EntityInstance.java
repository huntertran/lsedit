package lsedit;

import java.util.BitSet;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.PrintWriter;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

// Entities in a landscape 

/* Enumerates over those entities having edges from us of the correct relation class */

class EntityBelow implements Enumeration
{
	Vector				m_srcRelList;
	RelationClass		m_rc;
	int					m_next;
	RelationInstance	m_ri = null;

	private void advance(int i)
	{
		Vector				srcRelList    = m_srcRelList;
		RelationClass		rc            = m_rc;
		int					size          = srcRelList.size();
		RelationInstance	ri;
		RelationClass		rc1;

		for (;;) {
			if (++i >= size) {
				m_ri = null;
				break;
			}
			ri = (RelationInstance) srcRelList.elementAt(i);
			rc1 = ri.getRelationClass();
			if (rc1 == rc) {
				m_ri = ri;
				break;
		}	}
		m_next = i;
	}
	
	public EntityBelow(Vector srcRelList, RelationClass rc)
	{
		if (srcRelList != null) {
			m_srcRelList    = srcRelList;
			m_rc            = rc;
			advance(-1);
	}	}

	public boolean hasMoreElements()
	{
		return (m_ri != null);
	}

	public Object nextElement()
	{
		RelationInstance ri = m_ri;
		advance(m_next);
		return ri.getDst();
}	}

class EntityChildren implements Enumeration
{
	Vector				m_srcRelList;
	int					m_next;
	RelationInstance	m_ri = null;

	protected void advance(int i)
	{
		Vector				srcRelList    = m_srcRelList;
		int					size          = srcRelList.size();
		RelationInstance	ri;

		for (;;) {
			if (++i >= size) {
				m_ri = null;
				break;
			}
			ri = (RelationInstance) srcRelList.elementAt(i);
			if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
				m_ri = ri;
				break;
		}	}
		m_next = i;
	}
			

	public EntityChildren(Vector srcRelList)
	{
		if (srcRelList != null) {
			m_srcRelList    = srcRelList;
			advance(-1);
	}	}

	public boolean hasMoreElements()
	{
		return (m_ri != null);
	}

	public Object nextElement()
	{
		RelationInstance ri = m_ri;
		advance(m_next);
		return ri.getDst();
}	}

class EntityChildrenShown extends EntityChildren implements Enumeration
{
	protected void advance(int i)
	{
		RelationInstance ri;
		EntityInstance	 dst;
		
		for (;;) {
			super.advance(i);
			ri = m_ri;
			if (ri == null) {
				break;
			}
			dst = ri.getDst();
			if (dst.isShown() && !dst.isMarked(EntityInstance.HIDDEN_MARK)) {
				break;
			}
			i = m_next;
	}	}

	public EntityChildrenShown(Vector srcRelList)
	{
		super(srcRelList);
}	}

public final class EntityInstance extends LandscapeObject3D /* extends LandscapeObject */ implements Icon, MouseListener, MouseMotionListener {

	// Final values
	
	public final static int SMALL_FONT = 0;
	public final static int REG_FONT   = 1;

	public final static String LABEL_ID =			 "label";

	public final static String XRELPOSITION_ID =	 "xrel";
	public final static String YRELPOSITION_ID =	 "yrel";
	public final static String WIDTHREL_ID =		 "widthrel";
	public final static String HEIGHTREL_ID =		 "heightrel";

	public final static String IN_ELISION_ID =		 "elision";
	public final static String OUT_ELISION_ID =		 "outelision";
	public final static String CLIENT_ELISION_ID =	 "clientelision";
	public final static String SUPPLIER_ELISION_ID = "supplierelision";
	public final static String INTERNAL_ELISION_ID = "internalelision";
	public final static String CLOSED_ELISION_ID   = "open";

	public final static String NAVLINK_ID =			 "navlink";
	public final static String INPOINT_ID =			 "inpoints";
	public final static String OUTPOINT_ID =		 "outpoints";
	public final static String LEFTPOINT_ID =		 "leftpoints";
	public final static String RIGHTPOINT_ID =		 "rightpoints";
	public final static String DESC_ID =			 "description";
	public final static String TITLE_ID =			 "title";
	public final static String FONTDELTA_ID =		 "fontdelta";


	public static final int	ID_ATTR				= 0;
	public static final int	CLASS_ATTR			= 1;
	public static final int	LABEL_ATTR			= 2;
	public static final int	DESC_ATTR			= 3;
	public static final int	COLOR_ATTR			= 4;
	public static final int	LABEL_COLOR_ATTR	= 5;
	public static final int	OPEN_COLOR_ATTR		= 6;
	public static final int XRELPOSITION_ATTR   = 7;
	public static final int YRELPOSITION_ATTR   = 8;
	public static final int WIDTHREL_ATTR		= 9;
	public static final int HEIGHTREL_ATTR		= 10;
	public static final int FONTDELTA_ATTR      = 11;
	public static final int ATTRS               = 12;

	public static final String[] attributeName =
	{
		"id",
		"class",
		LABEL_ID,
		DESC_ID,
		COLOR_ID,
		LABEL_COLOR_ID,
		OPEN_COLOR_ID,
		XRELPOSITION_ID,
		YRELPOSITION_ID,
		WIDTHREL_ID,
		HEIGHTREL_ID,
		FONTDELTA_ID
	};

	public static final int[] attributeType =
	{
		Attribute.STRING_TYPE,
		Attribute.ENTITY_CLASS_TYPE,
		Attribute.STRING_TYPE,
		Attribute.TEXT_TYPE,
		Attribute.COLOR_OR_NULL_TYPE,
		Attribute.COLOR_OR_NULL_TYPE,
		Attribute.COLOR_OR_NULL_TYPE,
		Attribute.DOUBLE_TYPE,
		Attribute.DOUBLE_TYPE,
		Attribute.DOUBLE_TYPE,
		Attribute.DOUBLE_TYPE,
		Attribute.INT_TYPE
	};

	public	final static String NAVNEXT_ATTR          = "LSEDIT_NAVNEXT";
	public	final static String NAVPREV_ATTR          = "LSEDIT_NAVPREV";
	public	final static String NAVNEXT_DRAWROOT_ATTR = "LSEDIT_NAVNEXT_DRAWROOT";
	public	final static String NAVPREV_DRAWROOT_ATTR = "LSEDIT_NAVPREV_DRAWROOT";

	public  final static int RSZ_NONE = -1;
	private final static int RSZ_NW	= 0;
	private final static int RSZ_N	= 1;
	private final static int RSZ_NE	= 2; 
	private final static int RSZ_E	= 3;
	private final static int RSZ_SE	= 4;
	private final static int RSZ_S	= 5;
	private final static int RSZ_SW	= 6;
	private final static int RSZ_W	= 7;

	// Annotation tab constants

	private final static double TAB_HEIGHT = 16.0; 

	private final static int MOUSE_NEAR_EDGE_THRESHOLD = 4;
	private final static int SEP_THRESHOLD = 8;

	// Static font info 

	private final static int MARGIN = 5; 
	private final static int MIN_HEIGHT = 10;
	private final static int MIN_WIDTH = 30;

	private final static int CONTENTS_FLAG_DIM = 8;

	// Bits set in m_mark
	
	public final static int	DIAGRAM_MARK		= 0x00000001;	// This entity is visible in the main diagram  (at present)
	public final static int	CLIENT_MARK			= 0x00000002;	// This entity is in the set of visible clients   (at present)
	public final static int SUPPLIER_MARK		= 0x00000004;	// This entity is in the set of visible suppliers (at present)
	public final static int HIDDEN_MARK         = 0x00000008;	// This entity is to be treated as absent in the diagram

	public final static int	DRAWROOT_MARK       = 0x00000010;
	public final static int UNDER_DRAWROOT_MARK = 0x00000040;
	public final static int LEGEND_MARK         = 0x00000080;

	public final static int	OMNIPRESENT_LIBRARY = 0x00000100;
	public final static int	OMNIPRESENT_CLIENT  = 0x00000200;
	public final static int OMNIPRESENT_SUPPLIER= 0x00000400;
	public final static int TRACED_MARK         = 0x00000800; // This node participates in a backward/forward etc. trace

	public final static int REDBOX_MARK         = 0x00008000;	// Mark this box in deep red for (forward/back) edge tracing     
	public final static int	GROUP_MARK			= 0x00010000;	// This entity is part of a group == GROUP_FLAG_MARK
	public final static int GROUPKEY_MARK		= 0x00020000;	// This entity is the key entity in the group
	public final static int	OPEN_MARK			= 0x00040000;	// This entity is open
	public final static int CLOSED_MARK			= 0x00080000;	// This entity is closed
	public final static int	NOT_IN_FOREST_MARK  = 0x00100000;
	public final static int IN_GRAPH_MARK       = 0x00200000;	// Used by layouters
	public final static int HOVER_SCALE_MARK    = 0x00400000;
	public final static int COMPACT_MARK		= 0x00800000;
	public final static int HAS_LABEL_MARK      = 0x01000000;
	public final static int DELETED_MARK        = 0x02000000;
	public final static int SPRING_MARK         = 0x04000000;
	public final static int IN_SET_MARK         = 0x08000000;
	public final static int	SHADING_KNOWN_MARK  = 0x10000000;	// We have to recompute what our children shade
	public final static int	SHADES_MARK			= 0x20000000;	// We shade something larger than us
	public final static int	EXPANDED_TOC_MARK   = 0x40000000;
	
	public final static int CLIENT_SUPPLIER     = CLIENT_MARK | SUPPLIER_MARK;
	public final static int IN_DIAGRAM          = CLIENT_SUPPLIER | DIAGRAM_MARK;
	public final static int PRESENTATION_MARKS  = REDBOX_MARK | GROUP_MARK | GROUPKEY_MARK | TRACED_MARK;
	public final static int OMNIPRESENT_CS      = OMNIPRESENT_CLIENT | OMNIPRESENT_SUPPLIER;
	public final static int OMNIPRESENT         = OMNIPRESENT_LIBRARY | OMNIPRESENT_CS;
	public final static int PERMANENT_MARKS     = HAS_LABEL_MARK | DELETED_MARK | DRAWROOT_MARK | OMNIPRESENT;


	/* Conceptually an EntityInstance should derive from a BendPoint but it is hard to change
	 * everything to have relations point at bend points that may or may not be EntityInstances
	 * For now take the hit of wasting memory by having a bend point internally represented by
	 * a zero size EentityInstance.
	 */

	
	public final static double WIDTHRELLOCAL_DEFAULT  = 15.0/16.0;
	public final static double HEIGHTRELLOCAL_DEFAULT = 15.0/16.0;

	private static Font		m_closedFont        = null;
	private static Font		m_openFont          = null;
	private static Font		m_smallFont         = null;
	
//	private static int			  m_totalEntities     = 0;
	private static EntityInstance m_currentDescEntity = null;	// The current entry being described in the feedback box
	
	private int				m_mark = 0;

	// First order attributes

	public final static int DST_ELISION       = 0;		// Hide all edges (except the contains edge) which have me as their destination
	public final static int SRC_ELISION       = 1;		// Hide this edge if SRC_ELISION is set in the source of edge
	public final static int ENTERING_ELISION  = 2;
	public final static int EXITING_ELISION   = 3;
	public final static int INTERNAL_ELISION  = 4;
	public final static int CLOSED_ELISION    = 5;
	public final static int ELISIONS          = 6;
	public final static int	BOX_ELISION       = 7;		// Draw a box around the elision icon

	// Elisions are put into a BitSet to save space
	// Ordering DST_ELISION nid<0>, SRC_ELISION nid<0> ... CLOSED_ELISION nid<n>

	private BitSet	m_elisions;

	// Put in one array to save space
	// Ordering TOP nid<0>, BOTTOM_nid<0>, LEFT_nid<0>, RIGHT_nid<0>, TOP_nid<1> .... RIGHT_nid<n>
	private EdgePoint[] m_edgePoints;

	// These are the master coordinates with respect to the parent node
	// Shorts are used rather than bytes so that if the diagram is really blown up we can still get the
	// granularity needed to move things only a small bit.

	protected short	m_xRelLocal	     = Short.MIN_VALUE;
	protected short	m_yRelLocal      = Short.MIN_VALUE;
	protected short	m_widthRelLocal  = Short.MIN_VALUE;
	protected short	m_heightRelLocal = Short.MIN_VALUE;

	protected EntityPosition[] m_positions = null;

	// These are the top left coordinates of the object on the screen

	private int			m_fontDelta     = 0;

	private int			m_preorder;
	private int			m_postorder;

	// The entity actually in the drawn diagram which is an ancestor or same as this or null if item not visible

	private	EntityInstance	m_drawEntity;
	
	/* keep tight control over the group flags so can draw changes at a low level */

	private Vector m_srcRelList    = null;	// List of relations for which this entity is the source 
	private Vector m_dstRelList    = null;	// List of relations for which this entity is the destination 

	private EntityComponent	m_entityComponent = null;	// Our drawing component
	
	private RelationInstance	m_containedByRelation  = null;	// Spanning edge to us

	// --------------
	// Object methods
	// --------------

	public String toString() 
	{
		return getEntityLabel();
	}

	// -------------------------
	// Component wrapper methods
	// -------------------------

	public int getWidth()
	{
		if (m_entityComponent == null) {
			return 0;
		}
		return m_entityComponent.getWidth();
	}

	public int getHeight()
	{
		if (m_entityComponent == null) {
			return 0;
		}
		return m_entityComponent.getHeight();
	}

	// 
	// --------------------------
	// JComponent wrapper methods
	// --------------------------

	public void setVisible(boolean value)
	{
		JComponent entityComponent;
		
		if (value) {
			entityComponent = neededComponent();
			entityComponent.setVisible(true);
		} else {
			entityComponent = m_entityComponent;
			if (entityComponent != null) {
				entityComponent.setVisible(false);
	}	}	}
	
	protected String getExtendedDescription()
	{
		String	ret = getDescription();
		
		if (m_containedByRelation != null) {
			RelationClass	rc = m_containedByRelation.getRelationClass();

			if (rc != null && rc.getContainsClassOffset() > 0) {
				String temp;
				
				temp = rc.getLabel();
				if (temp != null) {
					temp  = "(" + temp + ")";
					if (ret != null) {
						temp += " " + ret;
					}
					ret = temp;
		}	}	}
		return ret;
	}
	
	public void setToolTipText(EntityComponent entityComponent)
	{
		String text;

		if (isDrawRoot()) {
			text = null;
		} else {
			text = getExtendedDescription();
			if (text == null) {
				text = getFullEntityLabel();
		}	}
		entityComponent.setToolTipText(text);
	}

	public void setToolTipText()
	{
		EntityComponent	component = m_entityComponent;
		
		if (component != null) {
			setToolTipText(component);
	}	}

	public void repaint()
	{
		EntityComponent entityComponent = m_entityComponent;

		if (entityComponent != null) {
			entityComponent.repaint();
	}	}
		
	public void revalidate()
	{
		EntityComponent entityComponent = m_entityComponent;

		if (entityComponent != null) {
			entityComponent.revalidate();
	}	}

	// -------------------------
	// LandscapeObject3D methods
	// -------------------------

	public JComponent getSwingObject()
	{
		return m_entityComponent;
	}

	// -------------------------------
	// EntityComponent Wrapper methods
	// -------------------------------

	// Diagram Coordinates
	
	public int getDiagramX()
	{
		return m_entityComponent.getDiagramX();
	}

	public int getDiagramY()
	{
		return m_entityComponent.getDiagramY();
	}

	public Rectangle getDiagramBounds() 
	{
		return m_entityComponent.getDiagramBounds();
	}

	public double getEdgePointX(EdgePoint edgePoint)
	{
		EntityComponent entityComponent = m_entityComponent;
		double			wf;

		if (isDrawRoot()) {
			// The draw root is always drawn square regardless of its actual shape
			wf = edgePoint.getWidthFactor();
		} else {
			wf = edgePoint.getAdjustedWidthFactor();
		}
		
//		System.out.println("This " + this + " edgePoint=" + edgePoint + " wf=" + wf);
		return ((double) entityComponent.getDiagramX()) + ((double) entityComponent.getWidth()) * wf;
	}

	public double getEdgePointY(EdgePoint edgePoint)
	{
		EntityComponent entityComponent = m_entityComponent;
		double			hf;

		if (isDrawRoot()) {
			// The draw root is always drawn square regardless of its actual shape
			hf = edgePoint.getHeightFactor();
		} else {
			hf = edgePoint.getAdjustedHeightFactor();
		}

		return ((double) entityComponent.getDiagramY()) + ((double) entityComponent.getHeight()) * hf;
	}

	public void refillEdges()
	{
		m_entityComponent.refillEdges();
	}

	public void computeShading()
	{
		m_entityComponent.computeShading();
	}

	public void paintMap(Graphics g, int x, int y, int width, int height, EntityInstance onPath, int depth)
	{
		EntityComponent entityComponent = neededComponent();	// TODO is this necessary

		entityComponent.paintMap(g, x, y, width, height, onPath, depth);
	}

	// Put e under us 

	public void addContainment(EntityInstance e) 
	{
		if (isMarked(DRAWROOT_MARK | UNDER_DRAWROOT_MARK)) {
			e.orMark(UNDER_DRAWROOT_MARK);
		}

		if (inDiagram()) {
			EntityComponent entityComponent = m_entityComponent;
			EntityComponent childComponent  = e.neededComponent();

			// We need to create childComponent even for hidden entities because it is the child component which
			// holds lifted edges.  If we do a forward trace and then a back trace we need the lifted edges in
			// the nodes not reached via a forward trace to easily perform the back trace.
			
			openStatusUnknown();	// Haven't yet decided if open or closed (cause we now have an additional child)

			// Add e back into new containment
			entityComponent.add(childComponent); 

			if (isOpen()) {
				e.orMark(IN_DIAGRAM);
		}	}
	}

	public void removeContainment(EntityInstance e) 
	{
		EntityComponent entityComponent = m_entityComponent;
			
		if (entityComponent != null) {
			JComponent childComponent = e.getEntityComponent();
			if (childComponent != null) {
				entityComponent.remove(childComponent);
		}	}
		e.m_mark       &= PERMANENT_MARKS;
	}

	public void resizeDstCardinals(int numRelations)
	{
		m_entityComponent.resizeDstCardinals(numRelations);
	}

	public void resizeSrcCardinals(int numRelations)
	{
		m_entityComponent.resizeSrcCardinals(numRelations);
	}

	// Reset all cardinals associated with this entity and things it contains

	public void resetDstCardinals() 
	{
		m_entityComponent.resetDstCardinals();
	}

	public void resetSrcCardinals() 
	{
		m_entityComponent.resetSrcCardinals();
	}

	// Calculate the number of edges into me

	public void calcDstEdgeCardinals()
	{
		m_entityComponent.calcDstEdgeCardinals();
	}

	// Calculate the number of edges out of me

	public void calcSrcEdgeCardinals()
	{
		m_entityComponent.calcSrcEdgeCardinals();
	}

	public void showDstCardinals()
	{
		m_entityComponent.showDstCardinals(getDiagram());
	}

	public void showSrcCardinals()
	{
		m_entityComponent.showSrcCardinals(getDiagram());
	}

	// ---------------------------
	// EntityInstance construction
	// ---------------------------

 	public EntityInstance(EntityClass parentClass, String id) 
	{
		if (m_openFont   == null) {
			m_openFont   = Options.getTargetFont(Option.FONT_OPEN);
		}
		if (m_closedFont == null) {
			m_closedFont = Options.getTargetFont(Option.FONT_CLOSED);
		}
		if (m_smallFont  == null) {
			m_smallFont  = Options.getTargetFont(Option.FONT_SMALL);
		}
			
		setParentClass(parentClass);
		super.setLabel(id);	         // The default
		setId(id);
//		++m_totalEntities;
	}
	
	public static void setOpenFont(Font font)
	{
		m_openFont = font;
	}
	
	public static void setClosedFont(Font font)
	{
		m_closedFont = font;
	}
	
	public static Font getSmallFont()
	{
		return m_smallFont;
	}
	
	public static void setSmallFont(Font font)
	{
		m_smallFont = font;
	}

/*
	public static int totalEntities()
	{
		return m_totalEntities;
	}
 */

	public EntityClass getEntityClass() 
	{
		return (EntityClass) getParentClass();
	}

	public String getStyleName(int style)
	{
		return EntityClass.getEntityStyleName(style);
	}

	public String getNavNext()
	{
		Attribute attribute = getLsAttribute(NAVNEXT_ATTR);

		if (attribute != null) {
			return attribute.parseString();
		}
		return null;
	}

	public void putNavNext(String value)
	{
		putAttribute(NAVNEXT_ATTR, qt(value));
	}

	public String getNavNextDrawRoot()
	{
		Attribute attribute = getLsAttribute(NAVNEXT_DRAWROOT_ATTR);

		if (attribute != null) {
			return attribute.parseString();
		}
		return null;
	}

	public void putNavNextDrawRoot(String value)
	{
		putAttribute(NAVNEXT_DRAWROOT_ATTR, qt(value));
	}

	public String getNavPrev()
	{
		Attribute 		attribute = getLsAttribute(NAVPREV_ATTR);
		Diagram			diagram;
		EntityInstance 	root;
	
		if (attribute != null) {
			return attribute.parseString();
		}
		diagram = getDiagram();
		root    = diagram.getRootInstance();
		if (root != this) {
			return root.getNavPrev();
		}
		return null;
	}

	public void putNavPrev(String value)
	{
		putAttribute(NAVPREV_ATTR, qt(value));
	}

	public String getNavPrevDrawRoot()
	{
		Attribute 		attribute = getLsAttribute(NAVPREV_DRAWROOT_ATTR);
		Diagram			diagram;
		EntityInstance 	root;
	
		if (attribute != null) {
			return attribute.parseString();
		}
		diagram = getDiagram();
		root    = diagram.getRootInstance();
		if (root != this) {
			return root.getNavPrevDrawRoot();
		}
		return null;
	}

	public void putNavPrevDrawRoot(String value)
	{
		putAttribute(NAVPREV_DRAWROOT_ATTR, qt(value));
	}

	// --------------------------
	// Low level state management
	// --------------------------

	public void orMark(int val) 
	{
		m_mark |= val;
	}

	public void andMark(int val)
	{
		m_mark &= val;
	}

	public void nandMark(int val) 
	{
		m_mark &= ~val;
	}
	
	public boolean isMarked(int val)
	{
		return((m_mark & val) != 0);
	}

	public boolean isAllMarked(int val)
	{
		return ((m_mark & val) == val);
	}

	public int getMark()
	{
		return m_mark;
	}

	public int getOmnipresent()
	{
		return (m_mark & OMNIPRESENT);
	}

	public boolean isClient()
	{
		return(isMarked(CLIENT_MARK));
	}

	public boolean isSupplier()
	{
		return(isMarked(SUPPLIER_MARK));
	}

	public boolean isClientOrSupplier()
	{
		return(isMarked(CLIENT_MARK|SUPPLIER_MARK));
	}
	
	public boolean isShown()
	{
		if (isMarked(DRAWROOT_MARK)) {
			return true;
		}
		return (getEntityClass().isShown());
	}

	public void markDeleted()
	{
		// Can't use orMark() -- permanent mark
		m_mark |= DELETED_MARK;
	}

	public void clearDeleted()
	{
		m_mark &= ~DELETED_MARK;
	}

	// ----------------
	// Color management
	// ----------------

	public Color getBackgroundWhenOpen() 
	{
		Color c;

		// (255 255 255) is white
		// Container is handled specially
		// Find color from depth

		c = getInheritedColorWhenOpen();
		if (c == null) {
			int            v    = Diagram.BG;
			EntityInstance b    = this;
			EntityInstance root = getDrawRoot();


			if (!root.hasDescendant(this)) {
				root = root.commonAncestor(this);
			}

			for (; b != root; b = b.getContainedBy()) {
				v -= 13;		// Darken
				if (v <= 0) {
					return Color.lightGray;
			}	}

			c = ColorCache.get(v, v, v);
		}
		return c;
	}
			
	public Color getCurrentObjectColor() 
	{
		Color c;

		if (isOpen()) {
			c = getBackgroundWhenOpen();
		} else {
			c = getInheritedObjectColor();
		}
		return c;
	}

	public Color getCurrentLabelColor() 
	{
		Color c;

		Option	option = Options.getDiagramOptions();
			
		if (option.isLabelInvertForeground()) {
			c = getCurrentObjectColor();
			if (!option.isLabelInvertBackground()) {
				c = ColorCache.getInverse(c.getRGB());
			}
		} else {
			c = getInheritedLabelColor();
		}
		if (option.isLabelBlackWhite()) {
			c = ColorCache.getBW(c);
		}
		return c;
	}

	// ----------------
	// Forest management
	// -----------------
	
	public void notInForest(boolean switching)
	{
		Vector dstRelList = m_dstRelList;
		
		// Mark that this node is not in the forest
		orMark(EntityInstance.NOT_IN_FOREST_MARK);
		if (dstRelList != null) {
			RelationInstance	ri;
			RelationClass		rc;
			int					cindex;
			Enumeration			en;

			for (en = dstRelList.elements(); en.hasMoreElements(); ) {
				ri  = (RelationInstance) en.nextElement();
				if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
					if (switching) {
						savePositioning(ri.getRelationClass());
					}
					ri.nandMark(RelationInstance.SPANNING_MARK);
	}	}	}	}
	
	public void tryToAddToForest(RelationClass containsClass, TaFeedback taFeedback)
	{
		Vector				dstRelList = m_dstRelList;	// Edges that come to me
		RelationInstance	ri;
		EntityInstance		parent;
		int					i, seen;
		
		seen        = 0;
		if (dstRelList != null) {
			for (i = dstRelList.size(); --i >= 0; ) {
				ri    = (RelationInstance) dstRelList.elementAt(i);
				if (ri.getParentClass() == containsClass) {
					++seen;
					if (isMarked(NOT_IN_FOREST_MARK)) {
						// Only the head of the spanning list can be us since all other nodes are !NOT_IN_FOREST_MARK 
						for (parent = ri.getSrc(); !parent.isMarked(EntityInstance.NOT_IN_FOREST_MARK); parent = parent.getContainedBy());
						if (parent == this) {
							// We have detected a cycle using this edge if we arrive here
							taFeedback.showCycle(ri);
						} else {
							// This edge does not form a cycle and is a valid spanning edge
							// Make it one.
							nandMark(EntityInstance.NOT_IN_FOREST_MARK);
							setContainedByRelation(ri);
//							System.out.println("Span " + ri);
		}	}	}	}	}
		if (seen > 0) {
			if (seen > 1) {
				taFeedback.hasMultipleParents(containsClass, this);
			}
	}	}	
	
	// ---------------
	// Font Management
	// ---------------

	public Font getAdjustedClosedFont(Font font)
	{
		int	 size1;
		int  size = size1 = font.getSize();

		size1 += m_fontDelta;
		if ((m_mark & HOVER_SCALE_MARK) != 0) {
			size1 = (int) (((double) size1) * Options.getHoverScale());
		}
		if (size1 != size) {
			String fontname = font.getFamily();
			int    style    = font.getStyle();

			if (size1 < 1) {
				size1 = 1;
			}
			font = FontCache.get(fontname, style, size1);
		}
		return font;
	}

	protected void setFont(Graphics g, int type) 
	{
		Font	font;
		
		if (type == SMALL_FONT) {
			font = m_smallFont;
		} else {
			font = getAdjustedClosedFont(m_closedFont);
		}
		g.setFont(font);
	}

	public int getFontDelta()
	{
		return(m_fontDelta);
	}

	public void setFontDelta(int value) 
	{
		m_fontDelta = value;
	}

	// ----------------
	// Label management
	// ----------------

	public String getTitle() 
	{
		Attribute attribute = getLsAttribute(TITLE_ID);
		if (attribute != null) {
			return attribute.parseString();
		}	
		return null;
	}

 	/* We must know if we assign our label mungeId(id) as the default because we should write the TA we
	 * get in, and we don't want to loose explicit label statements al-la PR-82. Cludgy but don't want
	 * to incur storage cost of extra string field per Entity.
	 */

	public String getLabel()
	{
		if (isMarked(HAS_LABEL_MARK)) {
			return super.getLabel();
		}
		return null;
	}
		
	public void setLabel(String value)
	{
		if (value == null || Util.isBlank(value)) {
			if (isMarked(HAS_LABEL_MARK)) {
				super.setLabel(getId());
				m_mark &= ~HAS_LABEL_MARK;
			}
		} else {
			super.setLabel(value);
			orMark(HAS_LABEL_MARK);
	}	}

	public String getEntityLabel()
	{
		return super.getLabel();
	}

	public String getFullEntityLabel()
	{
		if (isMarked(HAS_LABEL_MARK)) {
			return super.getLabel();
		}
		return super.getId();
	}
	
	public Font getEntityLabelFont()
	{
		Font	font;
		
		if (isOpen()) {
			if (getInheritedStyle() == EntityClass.ENTITY_STYLE_CLASS) {
				font = m_openFont;
			} else {
				font = m_smallFont;
			}
		} else {
			if (!isMarked(EntityInstance.CLIENT_SUPPLIER)) {
				font = getAdjustedClosedFont(m_closedFont);
			} else {
				font = ClientSupplierSet.getClientSupplierFont();
		}	}
		return font;
	}

	public String getClassLabel()
	{
		return getEntityClass().getLabel();
	}

	public Dimension getLabelDim(Graphics g, int type, boolean wParent) 
	{
		String	str = getEntityLabel();

		setFont(g, type);
		if (wParent) {
			EntityInstance pe = getContainedBy();

			if (pe != null) {
				str = pe.getEntityLabel() + " |\n" + str;
		}	} 
		return(Util.stringWrappedDim(g, str));
	}

	public Dimension getLabelDim(Graphics g, int type) 
	{
		return getLabelDim(g, type, false);
	}

	public int getMinFitWidth(Graphics graphics)
	{
		Dimension	ld       = getLabelDim(graphics, EntityInstance.REG_FONT);
		int			minWidth = ld.width + EntityComponent.MARGIN*2;

		if (hasChildren()) {
			minWidth += EntityComponent.CONTENTS_FLAG_X_RESERVE;
		}
		return minWidth;
	}

	public Dimension getFitDim(Graphics g, int font_type, boolean wParent) 
	{
		Dimension dim = getLabelDim(g, font_type, wParent);

		// Can't use isOpen because this is used to determine the size of a client/supplier
		if (hasChildren() && !isStateOpen()) {
			dim.width += CONTENTS_FLAG_DIM;
		}
		dim.width  += MARGIN*3;
		dim.height += (MARGIN*3)/2;

		return dim;
	}

	public Dimension getFitDim(Graphics g, int font_type) 
	{
		return getFitDim(g, font_type, false);
	}

	// ------------------
	// Position managment
	// ------------------

	/* Relative local coordinate system
     *
     * 0.0 same offset as top/left hand edge of parent     Linearly mapped to Short.MIN_VALUE + 1;
     * 1.0 same offset as bottom/right hand edge of parent Linearly mapped to Short.MAX_VALUE;
     */
     
	// Have to be careful that trueWidths >= 0
	// Returns -1 if undefined()
	
	private int trueWidth()
	{
		return m_widthRelLocal + Short.MAX_VALUE;
	}

	// Have to be careful that trueHeights >= 0
	// Returns -1 if undefined()
	
	private int trueHeight()
	{
		return m_heightRelLocal + Short.MAX_VALUE;
	}
	
	public double xRelLocal()  
	{
		double ret = Util.shortToRelative(m_xRelLocal);
		
		if ((m_mark & HOVER_SCALE_MARK) != 0) {
			ret += ((Util.shortToRelative(m_widthRelLocal) * (1.0 - Options.getHoverScale())) / 2.0);

			if (ret < 0.0) {
				ret = 0.0;
		}	}
		return(ret);
	}

	public void setXRelLocal(double xRelLocal) 
	{
		m_xRelLocal = Util.relativeToShort(xRelLocal);
	}

	public void setXRelLocalBounded(short xRelLocal)  
	{
		m_xRelLocal = xRelLocal;
	}

	public double yRelLocal() 
	{
		double ret = Util.shortToRelative(m_yRelLocal);
		
		if ((m_mark & HOVER_SCALE_MARK) != 0) {
			ret += ((Util.shortToRelative(m_heightRelLocal) * (1.0 - Options.getHoverScale())) / 2.0);
			if (ret < 0.0) {
				ret = 0.0;
		}	}
		return (ret);
	}

	public void setYRelLocal(double yRelLocal) 
	{
		m_yRelLocal = Util.relativeToShort(yRelLocal);
	}

	public void setYRelLocalBounded(short yRelLocal) 
	{
		m_yRelLocal = yRelLocal;
	}	

	public double widthRelLocal() 
	{
		double ret = Util.shortToRelative(m_widthRelLocal);
		
		if ((m_mark & HOVER_SCALE_MARK) != 0) {
			ret *= Options.getHoverScale();
			if (ret > 1.0) {
				ret = 1.0;
		}	}
		return (ret);
	}

	public void setWidthRelLocal(double widthRelLocal) 
	{
		m_widthRelLocal = Util.relativeToShort(widthRelLocal);
	}

	public void setWidthRelLocalBounded(short widthRelLocal) 
	{
		int	truewidth  = widthRelLocal + Short.MAX_VALUE;
		int rightPoint = m_xRelLocal   + truewidth;
		
		if (rightPoint > Short.MAX_VALUE) {
			m_xRelLocal = (short) (Short.MAX_VALUE - truewidth);
		}
		m_widthRelLocal = widthRelLocal;
	}

	public double heightRelLocal() 
	{
		double ret = Util.shortToRelative(m_heightRelLocal);
		
		if ((m_mark & HOVER_SCALE_MARK) != 0) {
			ret *= Options.getHoverScale();
			if (ret > 1.0) {
				ret = 1.0;
		}	}
		return (ret);
	}

	public void setHeightRelLocal(double heightRelLocal) 
	{
		m_heightRelLocal = Util.relativeToShort(heightRelLocal);
	}

	public void setHeightRelLocalBounded(short heightRelLocal) 
	{
		int trueheight = heightRelLocal + Short.MAX_VALUE;
		int	bottomEnd  = m_yRelLocal    + trueheight;
		
		if (bottomEnd > Short.MAX_VALUE) {
			m_yRelLocal = (short) (Short.MAX_VALUE - trueheight);
		}
		m_heightRelLocal = heightRelLocal;
	}
	
	public void setRelLocal(short x, short y, short width, short height)
	{
		m_xRelLocal      = x;
		m_yRelLocal      = y;
		m_widthRelLocal  = width;
		m_heightRelLocal = height;
	}

	public void setRelLocal(double x, double y, double width, double height)
	{
		setXRelLocal(x);
		setYRelLocal(y);
		setWidthRelLocal(width);
		setHeightRelLocal(height);
	}

	public void setRelLocal(EntityInstance e)
	{
		m_xRelLocal      = e.m_xRelLocal;
		m_yRelLocal      = e.m_yRelLocal;
		m_widthRelLocal  = e.m_widthRelLocal;
		m_heightRelLocal = e.m_heightRelLocal;
	}

	protected boolean overlaps(EntityInstance container)
	{
		Enumeration		en;
		EntityInstance	e;
		int				xrel1, yrel1, xendrel1, yendrel1;
		int				xrel2, yrel2, xendrel2, yendrel2;

		xrel1    = m_xRelLocal;
		yrel1    = m_yRelLocal;
		xendrel1 = xrel1 + trueWidth();
		yendrel1 = yrel1 + trueHeight();

		for (en = container.getChildren(); en.hasMoreElements(); ) {
			e        = (EntityInstance) en.nextElement();
			xrel2    = e.m_xRelLocal;
			yrel2    = e.m_yRelLocal;
			xendrel2 = xrel2 + e.trueWidth();
			yendrel2 = yrel2 + e.trueHeight();

			if (xrel2 > xendrel1) {
				// e is to the right of our right end point so no overlap
				continue;
			}
			if (yrel2 > yendrel1) {
				// e is below our bottom so no overlap
				continue;
			}
			if (xendrel2 < xrel1) {
				// e's right end if before our right start so no overlap
				continue;
			}
			if (yendrel2 < yrel1) {
				// e's bottom is above our top so no overlap
				continue;
			}
			return true;
		}	
		return false;
	}

	// Logic to find empty space in container in which to insert new entity

	protected void getEdgePoints(short[] xpoints, short[] ypoints)
	{
		xpoints[0] = xpoints[3] = m_xRelLocal;
		ypoints[0] = ypoints[1] = m_yRelLocal;
		xpoints[1] = xpoints[2] = (short) (m_xRelLocal + trueWidth());	// Compensate for widthRelLocal  < 0
		ypoints[2] = ypoints[3] = (short) (m_yRelLocal + trueHeight());	// Compensate for heightRelLocal < 0
	}

	protected static void getFramePoints(short[] xpoints, short[] ypoints)
	{
		xpoints[0] = xpoints[3] = ypoints[0] = ypoints[1] = Short.MIN_VALUE + 1;	// ie 0.0
		xpoints[1] = xpoints[2] = ypoints[2] = ypoints[3] = Short.MAX_VALUE;		// ie 1.0
	}
	
	protected void	setInitialLocation(EntityInstance container)
	{
		EntityInstance[]	children = new EntityInstance[container.numChildren()];
		short[]				xpoints1 = new short[4];
		short[]				ypoints1 = new short[4];
		short[]				xpoints2 = new short[4];
		short[]				ypoints2 = new short[4];
		short				xpoint1, ypoint1, xpoint2, ypoint2;
		short				x, y, xend, yend;
		short				x1, y1, x1end, y1end;
		short				bestx, besty;
		int					width, height, bestwidth, bestheight, area, bestarea;

		Enumeration			en;
		EntityInstance		child;
		int					i, j, i1, j1, k, size;

		size = 0;
		for (en = container.getChildren(); en.hasMoreElements(); ) {
			child = (EntityInstance) en.nextElement();
			if (child != this && Util.defined(child.m_xRelLocal) && Util.defined(child.m_yRelLocal) && Util.defined(child.m_widthRelLocal) && Util.defined(child.m_heightRelLocal)) {
				children[size++] = child;
		}	}

		if (size == 0) {
			bestx      = Short.MIN_VALUE + 1;	// ie 0.0
			besty      = Short.MIN_VALUE + 1;	// ie 0.0
			bestwidth  = Short.MAX_VALUE * 2;	// ie 1.0
			bestheight = Short.MAX_VALUE * 2;	// ie 1.0
		} else {
			// Find largest free area within the container

			bestx      = Short.MIN_VALUE + 1;
			besty      = Short.MIN_VALUE + 1;
			bestwidth  = 0;
			bestheight = 0;
			bestarea   = 0;

			for (i = 0; i <= size; ++i) {		// For each child and the frame

				if (i < size) {
					child      = children[i];
					child.getEdgePoints(xpoints1, ypoints1);
				} else {
					getFramePoints(xpoints1, ypoints1);
				}
				
				for (j = i; j <= size; ++j) {	// For this and subsequent children and frame
					if (j < size) {
						child      = children[j];
						child.getEdgePoints(xpoints2, ypoints2);
					} else {
						getFramePoints(xpoints2, ypoints2);
					}

					for (i1 = 0; i1 < 4; ++i1) {	// For each edge point of i
						xpoint1 = xpoints1[i1];
						ypoint1 = ypoints1[i1];
						j1 = ((i == j) ? i1+1 : 0);
						for (; j1 < 4; ++j1) {	// For each edge point of j
							xpoint2 = xpoints2[j1];
							width   = xpoint2 - xpoint1;
							if (width == 0) {
								continue;
							}
							ypoint2 = ypoints2[j1];
							height  = ypoint2 - ypoint1;
							if (height == 0) {
								continue;
							}

							if (width < 0) {
								x     = xpoint2;
								width = -width;
							} else {
								x     = xpoint1;
							}
							if (height < 0) {
								y      = ypoint2;
								height = -height;
							} else {
								y      = ypoint1;
							}
							xend = (short) (x + width);
							yend = (short) (y + height);

							// Ignore choice if rectangle framed by two edge points selected overlaps with any child

							for (k = size; k > 0; ) {
								child = children[--k];
								x1    = child.m_xRelLocal;
								if (x1 >= xend) {
									continue;
								}
								x1end = (short) (x1 + child.trueWidth());
								if (x1end <= x) {
									continue;
								}
								y1    = child.m_yRelLocal;
								if (y1 >= yend) {
									continue;
								}
								y1end = (short) (y1 + child.trueHeight());
								if (y1end <= y) {
									continue;
								}
								++k;
								break;
							}
							if (k != 0) {
								// Overlap so ignore
								continue;
							}

							area = (int) (width * height);
							if (area > bestarea) {
								bestx      = x;
								besty      = y;
								bestarea   = area;
								bestwidth  = width;
								bestheight = height;
							}
						}
					}
				}
			}
		}
		bestwidth  /= 2;
		bestheight /= 2;
		x           = (short) (bestx + (bestwidth/2));
		y           = (short) (besty + (bestheight/2));
		bestwidth  -= Short.MAX_VALUE;					// Switch to internal short range
		bestheight -= Short.MAX_VALUE;					// Switch to internal short range
		setRelLocal(x, y, (short) bestwidth, (short) bestheight);
	}
	
	// --------------------------
	// EntityComponent management
	// --------------------------

	public EntityComponent getEntityComponent()
	{
		return m_entityComponent;
	}

	public void setEntityComponent(EntityComponent entityComponent)
	{
		m_entityComponent = entityComponent;
	}

	public EntityComponent neededPlainComponent()
	{
		EntityComponent entityComponent = m_entityComponent;
		if (entityComponent == null) {
			entityComponent = new EntityComponent(this);
		}
		return(entityComponent);
	} 

	public EntityComponent neededComponent()
	{
		EntityComponent entityComponent = m_entityComponent;
		if (entityComponent == null) {
			entityComponent = new EntityComponent(this);
			// Do this here so that the legend use of entities isn't compromised
			setToolTipText(entityComponent);
			entityComponent.addMouseListener(this);
			entityComponent.addMouseMotionListener(this);

		}
		return(entityComponent);
	} 

	// ------------------------------------
	// Relation Connection Point management
	// ------------------------------------

	/* This is the list of all relations currently having us as their source (ie out relations) */

	public Vector getSrcRelList()
	{
		return m_srcRelList;
	}

	public void addSrcRelation(RelationInstance ri) 
	{
		Vector	srcRelList = m_srcRelList;

		if (srcRelList == null) {
			m_srcRelList = srcRelList = new Vector();
		}
		srcRelList.addElement(ri); 
	}
	
	public void addSrcRelationIfAbsent(RelationInstance ri) 
	{
		Vector srcRelList = m_srcRelList;

		if (srcRelList == null) {
			m_srcRelList = srcRelList = new Vector();
		} else if (srcRelList.contains(ri)) {
			return;
		}
		m_srcRelList.addElement(ri); 
	}

	public boolean removeSrcRelation(RelationInstance ri) 
	{
		Vector	srcRelList = m_srcRelList;
		boolean	ret        = false;  

		if (srcRelList != null) {
			ret = srcRelList.removeElement(ri);
			if (srcRelList.isEmpty()) {
				m_srcRelList = null; 
		}	}
		return ret;
	}

	public Enumeration srcRelationElements() 
	{
		Vector srcRelList = m_srcRelList;

		if (srcRelList != null) {
			return srcRelList.elements(); 
		}
		return null;
	}

	/* This is the list of all relations currently having us as their destination (ie in relations) */

	public Vector getDstRelList()
	{
		return m_dstRelList;
	}

	public void addDstRelation(RelationInstance ri) 
	{
		Vector dstRelList = m_dstRelList;

		if (dstRelList == null) {
			m_dstRelList = dstRelList = new Vector();
		}
		dstRelList.addElement(ri); 
	}

	public void addDstRelationIfAbsent(RelationInstance ri) 
	{
		Vector dstRelList = m_dstRelList;

		if (dstRelList == null) {
			m_dstRelList = dstRelList = new Vector();
		} else if (dstRelList.contains(ri)) {
			return;
		}
		dstRelList.addElement(ri); 
	}	

	public boolean removeDstRelation(RelationInstance ri) 
	{
		Vector	dstRelList = m_dstRelList;
		boolean	ret        = false;

		if (dstRelList != null) {
			ret = dstRelList.removeElement(ri);
			if (dstRelList.isEmpty()) {
				dstRelList = null;
		}	}
		return ret;
	}

	public Enumeration dstRelationElements() 
	{
		Vector dstRelList = m_dstRelList;

		if (dstRelList != null) {
			return dstRelList.elements(); 
		}
		return null;
	}

	// This is the list of all source relations logically lifted to us 
	 
	public Vector getSrcLiftedList()
	{
		EntityComponent entityComponent = m_entityComponent;

		if (entityComponent != null) {
			return entityComponent.getSrcLiftedList();
		}
		return null;
	}

	public Vector getDstLiftedList()
	{
		EntityComponent entityComponent = m_entityComponent;

		if (entityComponent != null) {
			return m_entityComponent.getDstLiftedList();
		}
		return null;
	}

	public Enumeration srcLiftedRelationElements() 
	{
		Vector srcLiftedList = getSrcLiftedList();

		if (srcLiftedList != null) {
			return srcLiftedList.elements();
		}
		return null; 
	}

	// This is the list of all destination relations logically lifted to us


	public Enumeration dstLiftedRelationElements() 
	{
		Vector dstLiftedList = getDstLiftedList();

		if (dstLiftedList != null) {
			return dstLiftedList.elements();
		}
		return null; 
	}

	public RelationInstance getRelationTo(RelationClass rc, EntityInstance dst) 
	{
		// Return relation instance if present

		Vector	srcRelList = m_srcRelList;

		if (srcRelList != null) {
			Enumeration		en;
			RelationInstance ri;

			for (en = srcRelList.elements(); en.hasMoreElements(); ) {
				ri = (RelationInstance) en.nextElement();
				if (ri.getRelationClass() == rc && ri.getDst() == dst) {
					return ri;
		}	}	}
		return null;
	}

	// Eliminate useless space at the end of the vectors

	public void compact()
	{
		Vector	v;

		v = m_srcRelList;
		if (v != null) {
			v.trimToSize();
		}
		v = m_dstRelList;
		if (v != null) {
			v.trimToSize();
		}
	}

	// -----------------------
	// Child management
	// -----------------------

	public boolean hasChildren() 
	{
		Vector	srcRelList = m_srcRelList;

		if (srcRelList != null) {
			RelationInstance	ri;

			for (int i = srcRelList.size(); --i >= 0;) {
				ri = (RelationInstance) srcRelList.elementAt(i);
				if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
					return true;
		}	}	}
		return false;
	}
			
	public int numChildren() 
	{
		Vector	srcRelList = m_srcRelList;
		int		ret        = 0;

		if (srcRelList != null) {
			RelationInstance	ri;

			for (int i = srcRelList.size(); --i >= 0; ) {
				ri = (RelationInstance) srcRelList.elementAt(i);
				if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
					++ret;
		}	}	}
		return ret;	
	}

	public EntityInstance getFirstChild() 
	{
		Vector	srcRelList = m_srcRelList;

		if (srcRelList != null) {
			int					size = srcRelList.size();
			RelationInstance	ri;

			for (int i = 0; i < size; ++i) {
				ri = (RelationInstance) srcRelList.elementAt(i);
				if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
					return ri.getDst();
		}	}	}
		return null;	
	}

	public EntityInstance getChild(int index) 
	{
		Vector	srcRelList = m_srcRelList;

		if (srcRelList != null) {
			int					size = srcRelList.size();
			RelationInstance	ri;

			for (int i = 0; i < size; ++i) {
				ri = (RelationInstance) srcRelList.elementAt(i);
				if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
					if (index == 0) {
						return ri.getDst();
					}
					--index;
		}	}	}
		return null;	
	}

	public int getIndexOfChild(Object child) 
	{
		Vector	srcRelList = m_srcRelList;

		if (srcRelList != null) {
			int					size          = srcRelList.size();
			RelationInstance	ri;
			int					index         = 0;

			for (int i = 0; i < size; ++i) {
				ri = (RelationInstance) srcRelList.elementAt(i);
				if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
					if (ri.getDst() == child) {
						return index;
					}
					++index;
		}	}	}
		return -1;	
	}

	public Enumeration getChildren() 
	{
		return new EntityChildren(m_srcRelList);
	}

	public Enumeration getChildrenShown() 
	{
		return new EntityChildrenShown(m_srcRelList);
	}
	
	public void	addChildren(Vector v)
	{
		Vector	srcRelList = m_srcRelList;

		if (srcRelList != null) {
			int					size          = srcRelList.size();
			RelationInstance	ri;

			for (int i = 0; i < size; ++i) {
				ri = (RelationInstance) srcRelList.elementAt(i);
				if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
					v.add(ri.getDst());
	}	}	}	}

	public Enumeration getBelow(RelationClass rc)
	{
		return new EntityBelow(m_srcRelList, rc);
	}

	// -----------------
	// Parent management
	// -----------------

	public RelationInstance getContainedByRelation() 
	{ 
		return m_containedByRelation;
	}

	public void setContainedByRelation(RelationInstance spanning)
	{
		spanning.orMark(RelationInstance.SPANNING_MARK);

		m_containedByRelation = spanning;
		restorePositioning(spanning.getRelationClass());
	}	

	public EntityInstance getContainedBy() 
	{ 
		RelationInstance	ri = getContainedByRelation();
		
		if (ri != null) {
			return ri.getSrc();
		}
		return null;
	}
	
	// Some stuff for example finding full path names must use the contains edge to navigate up.
	
	public EntityInstance getOriginalContainedBy() 
	{ 
		Diagram				diagram  = getDiagram();
		RelationClass		contains = diagram.getDefaultContainsClass();
		Vector 				v        = m_dstRelList;	// Edges that come to me
		int					i        = 2;
		RelationInstance	ri;
		int					j;
		
		for (;;) {
			if (v != null) {
				for (j = v.size(); --j >= 0; ) {
					ri    = (RelationInstance) v.elementAt(j);
					if (ri.getParentClass() == contains) {
						if (i == 2) {
							if (!ri.isMarked(RelationInstance.REVERSED_MARK)) {
								return ri.getSrc();
							}
						} else {
							if (ri.isMarked(RelationInstance.REVERSED_MARK)) {
								return ri.getDst();
			}	}	}	}	}
			if (--i <= 0) {
				break;
			}
			v = m_srcRelList;
		}
		return null;
	}
	
	// -----------------
	// Containment tests
	// -----------------

	public boolean isRoot() 
	{
		return (getContainedBy() == null);
	}

	public boolean inDiagram()
	{
		return(isMarked(IN_DIAGRAM));
	}

	// Postorder - Preorder == number of nodes below me

	public int prepostorder(int value)
	{
		Enumeration		en;
		EntityInstance	e;

		m_preorder = value;

		for (en = getChildren(); en.hasMoreElements(); ) {
			e = (EntityInstance) en.nextElement();
			value = e.prepostorder(value+1);
		}
		m_postorder = value;
		return(value);
	}

	public int	getPreorder()
	{
		return m_preorder;
	}

	public int	nodesInSubtree()
	{
		return m_postorder - m_preorder + 1;
	}

	public boolean hasAncestor(EntityInstance e) 
	{
		return(e.m_preorder < m_preorder && m_postorder <= e.m_postorder);
	}

	// Returns true if e is a descendant of me

	public boolean hasDescendant(EntityInstance e) 
	{
		return(m_preorder < e.m_preorder && e.m_postorder <= m_postorder);
	}

	public boolean hasDescendantOrSelf(EntityInstance e) 
	{
		return(m_preorder <= e.m_preorder && e.m_postorder <= m_postorder);
	}

	public boolean hasDescendantsOrSelf(Vector v)
	{
		int				cnt = v.size();
		int				i;
		EntityInstance	e;

		for (i = 0; i < cnt; ++i) {
			e = (EntityInstance) v.elementAt(i);
			if (!hasDescendantOrSelf(e)) {
				return false;
		}	}
		return true;
	}

	// Return the common ancestor entity

	public EntityInstance commonAncestor(EntityInstance e) 
	{
		EntityInstance e1;

		for (e1 = getContainedBy(); e1 != null; e1 = e1.getContainedBy()) {
			if (e1.hasDescendant(e)) {
				break;
		}	}
		return(e1);
	}

	// Return the common ancestor entity

	public EntityInstance commonAncestorOrSelf(EntityInstance e) 
	{
		EntityInstance e1;

		for (e1 = this; e1 != null; e1 = e1.getContainedBy()) {
			if (e1.hasDescendantOrSelf(e)) {
				break;
		}	}
		return(e1);
	}

	// Used to remove the forest of nodes under the root

	public void removeAllEdges()
	{
		Vector			 srcRelList = m_srcRelList;
		Vector			 dstRelList = m_dstRelList;
		RelationInstance ri;
		int				 i;

		if (srcRelList != null) {
			for (i = srcRelList.size(); --i >= 0;) {
				ri = (RelationInstance) srcRelList.elementAt(i);
				ri.removeEdge();
		}	}
		if (dstRelList != null) {
			for (i = dstRelList.size(); --i >= 0;) {
				ri = (RelationInstance) dstRelList.elementAt(i);
				ri.removeEdge();
	}	}	}

	public void setDefaultOpenStatus()
	{
		Enumeration		en;
		EntityInstance	child;

		// Since the root is a forest always open the roots children
		// These are the real highest level nodes

		if (numChildren() == 1) {
			for (en = getChildren(); en.hasMoreElements(); ) {
				child = (EntityInstance) en.nextElement();
				child.setStateOpen();
		}	}
	}

	// -------------------
	// DrawRoot management
	// -------------------

	protected EntityInstance getDrawRoot()
	{
		return(getDiagram().getDrawRoot());
	}

	public boolean isDrawRoot()
	{
		return (isMarked(DRAWROOT_MARK));
	}

	// ----------------------
	// Resize tabs management
	// ----------------------

	public int overResizeTab(int x1, int y1) 
	{
		int xLeft, xMid, xRight;
		int	yTop,  yMid, yBottom;
		int	pos;

		xLeft  = getDiagramX();

		if (x1 < xLeft) {
			// Outside object
			return RSZ_NONE;
		}
		if (x1 <= xLeft + 6) {
			pos = 0;
		} else {
			xRight = xLeft + getWidth();
			if (x1 > xRight) {
				// Outside object
				return RSZ_NONE;
			}
			if (x1 >= xRight - 6) {
				pos = 2;
			} else {
				xMid   = (xLeft + xRight)  / 2;
				if (x1 < xMid - 3 || x1 > xMid + 3) {
					return RSZ_NONE;
				}
				pos = 1;
		}	}

		yTop = getDiagramY();
		if (y1 < yTop) {
			// Outside object
			return RSZ_NONE;
		}
		if (y1 > yTop + 6) {
			yBottom = (int) (yTop + getHeight());
			if (y1 > yBottom) {
				// Outside object
				return RSZ_NONE;
			}
			if (y1 >= yBottom - 6) {
				pos += 6;
			} else {
				yMid = (yTop  + yBottom) / 2;
				if (y1 < yMid - 3 || y1 > yMid + 3) {
					return RSZ_NONE;
				}
				pos += 3;
		}	}

		switch (pos) {
		case 0:
			return RSZ_NW;
		case 1:
			return RSZ_N;
		case 2:
			return RSZ_NE;
		case 3:
			return RSZ_W;
		case 5:
			return RSZ_E;
		case 6:
			return RSZ_SW;
		case 7:
			return RSZ_S;
		case 8:
			return RSZ_SE;
		}
		return RSZ_NONE;
	}

	// ------------------------
	// RelationClass management
	// ------------------------

	protected int numRelationClasses()
	{
		return(getTa().numRelationClasses());
	}

	protected RelationClass numToRelationClass(int i)
	{
		return(getTa().numToRelationClass(i));
	}

	// ------------------
	// IOPoint management
	// ------------------

	protected EdgePoint[] needEdgePoints()
	{
		EdgePoint[]	edgePoints = m_edgePoints;
		int			needed     = numRelationClasses() * EdgePoint.SIDES;

		if (edgePoints == null) {
			m_edgePoints  = edgePoints = new EdgePoint[needed];
		} else {
			int	have = m_edgePoints.length;

			if (have < needed) {
				EdgePoint[] old = edgePoints;

				m_edgePoints = edgePoints = new EdgePoint[needed];
				for (int i = 0; i < have; ++i) {
					edgePoints[i] = old[i];
				}
				old = null;
		}	}
		return edgePoints;
	}

	public void refreshIOpoints(RelationClass rc, EdgePoint oldPoint, EdgePoint newPoint)
	{
		Vector			liftedList = getSrcLiftedList();
		int				i, j, size;
		RelationInstance ri;
		JComponent		relationComponent;

		for (j = 0; j < 2; ++j) {

			if (liftedList != null) {
				size = liftedList.size();
				for (i = 0; i < size; ++i) {
					ri = (RelationInstance) liftedList.elementAt(i);
					if (rc == null || ri.getRelationClass() == rc) {
						relationComponent = ri.getRelationComponent();
						if (relationComponent != null) {
							((RelationComponent) relationComponent).switchEdgePoint(oldPoint, newPoint);
			}	}	}	}
			liftedList = getDstLiftedList();
		}
	}

	public EdgePoint setFactors(RelationInstance relationInstance, EdgePoint edgePoint, double factorX, double factorY)
	{
		RelationClass	rc	    	= relationInstance.getRelationClass();
		EntityClass		ec          = getEntityClass();
		int				startindex	= rc.getNid() * EdgePoint.SIDES;
		int				endindex    = startindex + EdgePoint.SIDES;
		int				index;

		if (m_edgePoints != null) {
			int size = m_edgePoints.length;
			if (endindex > size) {
				endindex = size;
			}
			for (index = startindex; index < endindex; ++index) {
				if (edgePoint == m_edgePoints[index]) {
					edgePoint.setFactors(ec, factorX, factorY);
					return edgePoint;
		}	}	}

		index = ec.getEdgePointIndex(startindex, endindex, edgePoint);
		if (index < 0) {
			System.out.println("EntityInstance: Missing edgePoint");
			return edgePoint;
		}

		EdgePoint[]	edgePoints	= needEdgePoints();
		EdgePoint	oldPoint    = ec.getPoint(index);
		EdgePoint	newPoint    = new EdgePoint();

		RelationInstance ri;
		JComponent	relationComponent;

		newPoint.setFactors(ec, factorX, factorY);
		edgePoints[index]       = newPoint;

		refreshIOpoints(rc, oldPoint, newPoint);
		return newPoint;
	}

	public void resetIOpoints()
	{
		Enumeration		en;
		EntityInstance	e;


		if (m_edgePoints != null) {
			EntityClass		ec          = getEntityClass();

			if (ec.isActive()) {
				int				size        = m_edgePoints.length;
				int				index;
				EdgePoint		oldPoint;
				EdgePoint		newPoint;
				RelationClass	rc1;
				int				side, nic;
				boolean			change = false;
				Diagram			diagram = null;

				for (index = 0; index < size; ++index) {
					oldPoint = m_edgePoints[index];
					if (oldPoint != null) {
						nic     = index / EdgePoint.SIDES;
						if (diagram == null) {
							diagram = getDiagram();
						}
						rc1 = diagram.numToRelationClass(nic);
						if (rc1.isActive()) {
							side = index % EdgePoint.SIDES;

							newPoint = ec.getPoint(rc1, side);
							m_edgePoints[index] = null;
							refreshIOpoints(null, oldPoint, newPoint);
							change = true;
				}	}	}
				if (change) {
					// Need to do this because otherwise relations clip when redrawn as a consequence of drawOutline causing diagram repaint having diagram as bounds
					revalidateAllMyEdgesForClass(null);
		}	}	}

		for (en = getChildren(); en.hasMoreElements(); ) {
			e = (EntityInstance) en.nextElement();
			e.resetIOpoints();
	}	}

	protected void adjustEdgePoints() 
	{
		EdgePoint[]	edgePoints = m_edgePoints;
		EdgePoint	edgePoint;

		if (edgePoints != null) {
			int i      = 0;
			int	length = edgePoints.length;

			for (; i < length; ++i) {
				edgePoint = edgePoints[i];
				if (edgePoint != null) {
					edgePoint.adjustEdgePoint(getEntityClass());
	}	}	}	}

	public boolean isPointOverIO(EdgePoint pt, int x, int y) 
	{
		int	x1, y1;

		x1 = (int) (getEdgePointX(pt) + 0.5);	// round
		y1 = (int) (getEdgePointY(pt) + 0.5);	// round	

		return ((x1 - RelationInstance.NEAR_PIXEL_SIZE/2) < x && x < (x1 + RelationInstance.NEAR_PIXEL_SIZE/2) && (y1 - RelationInstance.NEAR_PIXEL_SIZE/2) < y && y < (y1 + RelationInstance.NEAR_PIXEL_SIZE/2));
	}

	public EdgePoint getPoint(RelationClass rc, int side) 
	{
		EdgePoint[] edgePoints = m_edgePoints;

		
		if (edgePoints != null) {
			int			index     = rc.getNid() * EdgePoint.SIDES + side;
			EdgePoint	edgePoint = edgePoints[index];
			if (edgePoint != null) {
				return(edgePoint);
		}	}
		return getEntityClass().getPoint(rc, side);
	}

	// At what point does the ri relation hit this, given that it goes from the box specified by srcLyt to dstLyt
	// N.B. Most of the time anyway it appears that srcLyt is the srcLyt of this.

	public EdgePoint getOutPoint(RelationClass rc, int edge_mode, Rectangle srcLyt, Rectangle dstLyt) 
	{
		if (edge_mode >= 0) {
			// Only allow edges to be from top or bottom
			return getPoint(rc, edge_mode);
		}
		if ((dstLyt.y - (srcLyt.y+srcLyt.height)) > SEP_THRESHOLD) {

			// dstLyt strictly below srcLyt
			return getPoint(rc, EdgePoint.BOTTOM);
		}

		if ((srcLyt.y - (dstLyt.y+dstLyt.height)) > SEP_THRESHOLD) {
			// srcLyt strictly below dstLyt
			return getPoint(rc, EdgePoint.TOP);
		}

		if ((dstLyt.x - (srcLyt.x+srcLyt.width)) > SEP_THRESHOLD) {
			return getPoint(rc, EdgePoint.RIGHT);
		}

		if ((srcLyt.x - (dstLyt.x+dstLyt.width)) > SEP_THRESHOLD) {
			return getPoint(rc, EdgePoint.LEFT);
		}

		if (dstLyt.y > (srcLyt.y+srcLyt.height)) {
			return getPoint(rc, EdgePoint.BOTTOM);
		}
		return getPoint(rc, EdgePoint.TOP);
	}

	public EdgePoint getLeftOutPoint(RelationInstance ri) 
	{
		RelationClass rc = ri.getRelationClass();

		return getPoint(rc, EdgePoint.LEFT);
	}

	public EdgePoint getRightOutPoint(RelationInstance ri) 
	{
		RelationClass rc = ri.getRelationClass();

		return getPoint(rc, EdgePoint.RIGHT);
	}

	public EdgePoint getTopOutPoint(RelationInstance ri) 
	{
		RelationClass rc = ri.getRelationClass();

		return getPoint(rc, EdgePoint.TOP);
	}

	public EdgePoint getBottomOutPoint(RelationInstance ri) 
	{
		RelationClass rc = ri.getRelationClass();

		return getPoint(rc, EdgePoint.BOTTOM);
	}

	protected EdgePoint getMouseOverIO(int x, int y) 
	{
		EdgePoint[] edgePoints = m_edgePoints;

		if (edgePoints != null) {
			int i      = 0;
			int	length = edgePoints.length;

			for (; i < length; ++i) {
				EdgePoint pt = edgePoints[i];

				if (pt != null) {
					if (isPointOverIO(pt, x, y)) {
						return pt;
		}	}	}	}
		return null;
	}

	// ------------------
	// Elision Management
	// ------------------

	public void elisionsChanged()
	{
		EntityComponent	entityComponent = m_entityComponent;
		
		if (entityComponent != null) {
			entityComponent.elisionsChanged();
	}	}
	
	public int getOpenBit()
	{
		return ((m_containedByRelation == null) ? 0 : (m_containedByRelation.getRelationClass().getNid() * ELISIONS) + CLOSED_ELISION);
	}

	private BitSet needElisions()
	{
		BitSet bitset = m_elisions;
		if (bitset == null) {
			m_elisions = bitset = new BitSet(ELISIONS * numRelationClasses());
		}
		return bitset;
	}

	public boolean getElision(int bit)
	{
		BitSet elisions = m_elisions;

		if (elisions != null) {
			return elisions.get(bit);
		}
		return false;
	}
	
	public void clearElision(int bit)
	{
		BitSet elisions = m_elisions;

		if (elisions != null && elisions.get(bit)) {
			elisions.clear(bit);
			if (elisions.isEmpty()) {
				m_elisions = null;
		}	}
//		System.out.println("Entity " + this + " elision " + (bit/ELISIONS) + ":" + (bit % ELISIONS) + " cleared");
	}

	public void setElision(int bit)
	{
		BitSet elisions = needElisions();
		elisions.set(bit);
//		System.out.println("Entity " + this + " elision " + (bit/ELISIONS) + ":" + (bit % ELISIONS) + " set");
	}

	public boolean getElision(int type, int nid)
	{
		return getElision((nid * ELISIONS) + type);
	}
	
	public int	getElisions(RelationClass rc)
	{
		int nid  = rc.getNid();
		int mask = 1;
		int	ret  = 0;
		int i;
		
		for (i = 0; i < ELISIONS; ++i) {
			if (getElision(i, nid)) {
				ret |= mask;
			}
			mask <<= 1;
		}
		return (ret);
	}	
	
	public void clearElision(int type, int nid)
	{
		clearElision((nid * ELISIONS) + type);
	}

	public void setElision(int type, int nid)
	{
		setElision((nid * ELISIONS) + type);
	}

	private void toggleElision(int bit)
	{
		if (getElision(bit)) {
			clearElision(bit);
			return;
		} 
		setElision(bit);
	}

	private void toggleElision(int type, int nid)
	{
		toggleElision((nid * ELISIONS) + type);
	}
	
	public void toggleElision(int type, Enumeration relationClasses) 
	{
		Enumeration en;
		RelationClass rc;

		for (; relationClasses.hasMoreElements(); ) {
			rc = (RelationClass) relationClasses.nextElement(); 
			if (rc.isShown()) {
				toggleElision(type, rc.getNid()); 
			}
		}
		elisionsChanged();
	}

	// Used to clear everything but the open/closed state of an entity

	public int	clearElisions()
	{
		BitSet			elisions = m_elisions;
		int				ret      = 0;
		Enumeration		en;
		EntityInstance	e;

		if (elisions != null) {
			int				index = -1;
			boolean			seen = false;

			while ((index = elisions.nextSetBit(++index)) >= 0) {
				if ((index % ELISIONS) == CLOSED_ELISION) {
					seen = true;
					continue;
				}
				elisions.clear(index);
				ret = 1;
			}
			if (!seen) {
				m_elisions = null;
		}	}
		elisionsChanged();

		for (en = getChildren(); en.hasMoreElements(); ) {
			e = (EntityInstance) en.nextElement();
			ret += e.clearElisions();
		}
		return ret;
	}

	// ---------------------
	// Open/Close managenent
	// ---------------------

	public void setOpen() 
	{
		if (!isMarked(OPEN_MARK)) {
			nandMark(CLOSED_MARK);
			orMark(OPEN_MARK);
	}	}

	public void setClosed()
	{
		if (!isMarked(CLOSED_MARK)) {
			nandMark(OPEN_MARK);
			orMark(CLOSED_MARK);
	}	}

	public void openStatusUnknown()
	{
		nandMark(OPEN_MARK|CLOSED_MARK);
	}

	// Returns true if the entity is marked as open (under the current containment heirarchy)
	// Default is for entity to be closed under all containment heirarchies

	public boolean isStateOpen()
	{
		return getElision(getOpenBit());
	}

	public boolean setStateClosed() 
	{
		int openBit = getOpenBit();

		if (getElision(openBit)) {
			clearElision(openBit);
			nandMark(OPEN_MARK);
			orMark(CLOSED_MARK);
			return true;
		}
		return false;
	}

	public void setStateOpen() 
	{
		int openBit = getOpenBit();

		if (!getElision(openBit)) {
			setElision(openBit);
			openStatusUnknown();
	}	}

	public boolean isOpen() 
	{
		if (isMarked(DRAWROOT_MARK)) {
			return true;
		}

		// If haven't yet decided if open or closed
		// We will treat zero sized objects as being closed so that we don't have to repaint edges to zero
		// sized objects which is very expensive if have huge tree and everything open

		if (!isMarked(OPEN_MARK|CLOSED_MARK)) {
			if (!hasChildren() || getWidth() <= 0 || getHeight() <= 0) {
				setClosed();
			} else {
				if (isStateOpen()) {	// True if logically to be considered open
					setOpen();
				} else {
					setClosed();
		}	}	}

		return(isMarked(OPEN_MARK));
	}

	public boolean closedWithChildren()
	{
		return(!isOpen() && hasChildren());
	}

/*
	public boolean closedWithChildrenUnderDrawroot()
	{
		return(!isOpen() && hasChildren() && !isMarked(EntityInstance.CLIENT_SUPPLIER));
	}
*/

	public boolean red_closed()
	{
		return(isMarked(REDBOX_MARK) && !isOpen());
	}

	public boolean red_open()
	{
		return(isMarked(REDBOX_MARK | HOVER_SCALE_MARK) && isOpen());
	}

	// Used to close everything under me

	public int closeDescendants()
	{
		Enumeration		en  = getChildren();
		EntityInstance	e;
		int				ret = 0;

		if (en.hasMoreElements()) {
			BitSet elisions = needElisions();
		
			if (isStateOpen()) {
				setStateClosed();
				ret = 1;
			}
			for (; en.hasMoreElements(); ) {
				e = (EntityInstance) en.nextElement();
				ret += e.closeDescendants();
		}	}
		return ret;
	}

	// Open everything but only while it is effectively visible
	// We flag everything as open but the open logic actually decides

	public int openDescendants()
	{
		Enumeration		en  = getChildren();
		int				ret = 0;
		EntityInstance	e;

		if (en.hasMoreElements()) {
			BitSet	elisions = m_elisions;

			if (!isStateOpen()) {
				// Was closed
				setStateOpen();
				ret = 1;
			}

			for (; en.hasMoreElements(); ) {
				e = (EntityInstance) en.nextElement();
				ret += e.openDescendants();
		}	}
		return ret;
	}

	// ----------------
	// Group management
	// ----------------

	// Return reference to entity the cursor is over. 
	// Return null if none.

	public EntityInstance getMouseOver(int x, int y) 
	{
		EntityComponent entityComponent = m_entityComponent;

		if (entityComponent != null && entityComponent.contains(x,y)) {
			if (isOpen()) {
				Enumeration en;
				EntityInstance e, over;

				for (en = getChildrenShown(); en.hasMoreElements(); ) {
					e = (EntityInstance) en.nextElement();
					over = e.getMouseOver(x, y);
					if (over != null)
						return over; 
			}	}
			// It's not over any of our children, so it's over us.
			return this; 
		}
		return null;
	}

 	public boolean containsRectangle(int x, int y, int width, int height) 
	{
		int x1 = getDiagramX();
		int y1 = getDiagramY();

		if (x1 > x) {
			return false;
		}
		if (y1 > y) {
			return false;
		}
		if (x + width > x1 + getWidth()) {
			return false;
		}
		if (y + height > y1 + getHeight()) {
			return false;
		}
		return true; 
	}

	// Find the smallest entity that contains the indicated bounds

	public EntityInstance containing(int x, int y, int width, int height) 
	{
		if (containsRectangle(x, y, width, height)) {
			if (hasChildren() && isOpen()) {
				// Check children
				Enumeration en;

				for (en = getChildrenShown(); en.hasMoreElements(); ) {
					EntityInstance e = (EntityInstance) en.nextElement();
					EntityInstance oe = e.containing(x, y, width, height);
					if (oe != null) {
						return oe;
				}	}
			}
			return this;
		}
		return null;
	}

	public boolean getGroupFlag() 
	{
		return isMarked(GROUP_MARK);
	}

	public boolean getGroupKeyFlag() 
	{
		return isMarked(GROUPKEY_MARK);
	}

	// ----------------
	// Cache management
	// ----------------

	public void removeTreeFromCache(EntityCache entityCache)
	{
		Enumeration			children;
		EntityInstance		child;


		entityCache.remove(this);
		for (children = getChildren(); children.hasMoreElements(); ) {
			child = (EntityInstance) children.nextElement();
			child.removeTreeFromCache(entityCache);
	}	}

	public void addTreeToCache(EntityCache entityCache)
	{
		Enumeration			children;
		EntityInstance		child;


		entityCache.put(this);
		for (children = getChildren(); children.hasMoreElements(); ) {
			child = (EntityInstance) children.nextElement();
			child.addTreeToCache(entityCache);
	}	}

	// ---------------------------
	// Size and location managment
	// ---------------------------

	/* Invoked when some of the children have no assigned relwidth/relheight 
	 * These operation never update only set because the relative bounds
	 * have never yet been seen so these updates are transparent to high
	 * level logic
	 */

	protected void assignDimensions()
	{
		EntityInstance	e;
		Enumeration		en;
		int				total, rows;
		double			relWidth, relHeight;

		// Total must be > 0 since we know of a child which needs sizing 

		total     = numChildren();
		rows      = (int) Math.sqrt(total);
		relWidth  = (WIDTHRELLOCAL_DEFAULT  * rows) / (2 * total);
		relHeight = (HEIGHTRELLOCAL_DEFAULT * rows) / (2 * total);

		for (en = getChildren(); en.hasMoreElements(); ) {
			e = (EntityInstance) en.nextElement();
			if (!Util.defined(e.m_widthRelLocal)) {
				e.setWidthRelLocal(relWidth);	// Direct assignment (no signalling)
			}
			if (!Util.defined(e.m_heightRelLocal)) {		// Direct assignment (no signalling)
				e.setHeightRelLocal(relHeight);
			}
//			System.out.println("EntityInstance.assignDimensions " + e + " relWidth=" + e.m_widthRelLocal + " relHeight=" + e.m_heightRelLocal);
	}	}

	// May only call this once our parents size has been set
	// Recompute our size based on that parent, add us to the
	// diagram if not yet in it, and flag our open state

	private void resizeEntity(EntityComponent parentComponent)
	{
		double			widthRel   = widthRelLocal();
		double			heightRel  = heightRelLocal();
		EntityComponent	component;
		int				width, height;

		if (widthRel < 0 || heightRel < 0) {
			EntityInstance	parent = parentComponent.getEntityInstance();
			// Assign default sizes to use and all our siblings without such dimensions
			parent.assignDimensions();
			widthRel  = widthRelLocal();
			heightRel = heightRelLocal();
		}
		component = neededComponent();
		if (component.getParent() != parentComponent) {
			parentComponent.add(component);
		}
		width  = (int) (widthRel  * (double) parentComponent.getWidth());
		height = (int) (heightRel * (double) parentComponent.getHeight());

		if (width <= 0 || height <= 0) {
			width = height = 0;
		}

		if (width == 0) {
			setClosed();
		} else {
			openStatusUnknown();
		}
		component.setSize(width, height);
		resizeChildren(component);
	}

	// May only call this once our size has been set

	public void resizeChildren(EntityComponent entityComponent)
	{
		// Now our size is known so can test if we are to be considered open

		if (isOpen()) {
			Vector srcRelList = m_srcRelList;

			if (srcRelList != null) {
				RelationInstance	ri;
				EntityInstance		child;
				EntityComponent		childComponent;
				int					i;
				
				for (i = srcRelList.size(); --i >= 0; ) {
					ri  = (RelationInstance) srcRelList.elementAt(i);
					if (!ri.isMarked(RelationInstance.SPANNING_MARK)) {
						continue;
					}
					child = ri.getDst();
					if (!child.isShown()) {
						continue;
					}
//					System.out.println("resizeChildren " + child);
					child.resizeEntity(entityComponent);
	}	}	}	}
		
	/* This method establishes the size of the root entityComponent in a visual tree
	 * The root is unique in not using its relative sizes in order to position itself
	 */

	public void setEntitySize(int width, int height, Container above) 
	{
		EntityComponent entityComponent = neededComponent();

		if (entityComponent.getParent() != above) {
			above.add(entityComponent);
		}

		if (width <= 0 || height <= 0) {
			width = height = 0;
		}

		entityComponent.setBounds(0, 0, width, height);

		if (!isMarked(DRAWROOT_MARK)) {
			// The current drawroot is never marked closed
			if (width == 0) {
				setClosed();
			} else {
				openStatusUnknown();
		}	}

		resizeChildren(entityComponent);
	}

	// Must invoke this after lifted all edges
	// Don't do when resizing because resizing must be done before
	// lifting of edges -- otherwise can't decide what is open and
	// what is closed. This operation must cleverly disable all
	// appearance that values are being updated with undo/redo
	// signalling etc.  To the high level logic it is as if this
	// function is never called.

	protected void assignLocations()
	{
		Enumeration			en;
		EntityInstance		e;
		Vector				v = null;

//		System.out.println("EntityInstance.assignLocations " + this);

		for (en = getChildren(); en.hasMoreElements(); ) {
			e = (EntityInstance) en.nextElement();
			if (!Util.defined(e.m_xRelLocal) || !Util.defined(e.m_yRelLocal)) {
				if (v == null) {
					v = new Vector();
				}
				v.add(e);
		}	}

		if (v != null) {
			/* These vertices have no position */
			Diagram				diagram         = getDiagram();
			LandscapeEditorCore	ls              = diagram.getLs();
			EntityComponent		entityComponent = getEntityComponent();
			boolean				undo            = diagram.undoEnabled();
			int					new_width, new_height;


			if (undo) {
				diagram.setUndoEnabled(false);
			}
			diagram.disableTaListeners();

			ls.doLayout1(ls.getLayouter(), v, this, true);

			// Handle the fact that layout algorithm may have changed sizes of children

			resizeChildren(entityComponent);

/*
			{
				int i, cnt;

				cnt = v.size();
				for (i = 0; i < cnt; ++i) {
					e = (EntityInstance) v.elementAt(i);
					System.out.println("EntityInstance.assignLocations " + e + " relX=" + e.m_xRelLocal + " relY=" + e.m_yRelLocal);
			}	}
*/
			diagram.enableTaListeners();

			if (undo) {
				diagram.setUndoEnabled(true);
			}
			v = null;

		}
//		System.out.println("EntityInstance.assignLocations " + this + " done");
	}

	// May only call this once our location and size has been set
	// Also don't do before lifted edges since we may use these edges
	// to compute default locations for children
	// Need to call this even if our location hasn't changed but our size has

	public void relocateEntity(EntityComponent parentComponent)
	{
		double			xRel, yRel;
		EntityComponent	component;
		int				x, y;

		xRel = xRelLocal();
		yRel = yRelLocal();

		if (xRel < 0 || yRel < 0) {
			EntityInstance parent = parentComponent.getEntityInstance();

			// Somehow assign locations to us and all our siblings
			parent.assignLocations();
			xRel = xRelLocal();
			yRel = yRelLocal();
		}

		component = getEntityComponent();
		x         = (int) (xRel * (double) parentComponent.getWidth());
		y         = (int) (yRel * (double) parentComponent.getHeight());
					
		component.setLocation(x, y);
		relocateChildren(component);	
	}

	public void relocateChildren(EntityComponent entityComponent)
	{
		// Our size is known so can test if we are to be considered open

		if (isOpen()) {
			Vector srcRelList = m_srcRelList;

			if (srcRelList != null) {
				RelationInstance	ri;
				EntityInstance		child;
				int					i;
				
				for (i = srcRelList.size(); --i >= 0; ) {
					ri  = (RelationInstance) srcRelList.elementAt(i);
					if (!ri.isMarked(RelationInstance.SPANNING_MARK)) {
						continue;
					}
					child = ri.getDst();
					if (!child.isShown()) {
						continue;
					}
					child.relocateEntity(entityComponent);
	}	}	}	}
	
	public void setDiagramLocation(int diagramX, int diagramY)
	{
		m_entityComponent.setDiagramLocation(diagramX, diagramY);
	}

	// Our top left hand corner is within our parent at (x, y)
		
	public void setEntityLocation(int x, int y) 
	{
		EntityComponent entityComponent = m_entityComponent;

		entityComponent.setLocation(x, y);
		relocateChildren(entityComponent);
	}

	public void setEntityBounds()
	{
		if (m_entityComponent != null) {
			// Can become null if an undo is done later
			m_entityComponent.setEntityBounds();
	}	}

	private	void setPositionArraySize(int lth)
	{
		EntityPosition[]	positions = m_positions;

		if (positions == null) {
			m_positions = positions = new EntityPosition[lth];
		} else if (positions.length < lth) {
			EntityPosition[]	oldpositions = positions;
			int					i;
			
			m_positions = positions = new EntityPosition[lth];

			for (i = oldpositions.length; --i >= 0; ) {
				positions[i] = oldpositions[i];
	}	}	}

	public void savePositioning(RelationClass rc)
	{
		// Copy current values to correct index position

		if (Util.defined(m_xRelLocal) || Util.defined(m_yRelLocal) || Util.defined(m_widthRelLocal) || Util.defined(m_heightRelLocal)) {
		
			int				index = rc.computeCIndex();

			setPositionArraySize(index + 1);
			EntityPosition[]	positions = m_positions;
			EntityPosition		position  = positions[index];
			if (position == null) {
				positions[index] = position = new EntityPosition();
			}
			position.m_xRelLocal      = m_xRelLocal;
			position.m_yRelLocal      = m_yRelLocal;
			position.m_widthRelLocal  = m_widthRelLocal;
			position.m_heightRelLocal = m_heightRelLocal;
	}	}

	public void restorePositioning(RelationClass rc)
	{
		int					index     = rc.computeCIndex();
		EntityPosition[]	positions = m_positions;
		EntityPosition		position;
		
		if (positions == null && index == 0) {
			return;
		}
		if (positions == null || positions.length <= index || (position = positions[index]) == null) {
			m_xRelLocal      = Util.undefined();
			m_yRelLocal      = Util.undefined();
			m_widthRelLocal  = Util.undefined();
			m_heightRelLocal = Util.undefined();
		} else {
			m_xRelLocal      = position.m_xRelLocal;
			m_yRelLocal      = position.m_yRelLocal;
			m_widthRelLocal  = position.m_widthRelLocal;
			m_heightRelLocal = position.m_heightRelLocal;
		}
	}

	// -------------------------------
	// Change to appearance management
	// -------------------------------

	public void shapeChanges(EntityClass ec)
	{
		Vector				srcRelList = getSrcRelList();
		EntityInstance		e;
		RelationInstance	ri;
		int					i;

		if (ec == getParentClass()) {
			adjustEdgePoints();
			revalidateAllMyEdges();
			repaint();
		}
		if (srcRelList != null) {
			for (i = srcRelList.size(); --i >= 0; ) {
				ri = (RelationInstance) srcRelList.elementAt(i);
				if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
					e = ri.getDst();
					e.shapeChanges(ec);
		}	}	}
	}

	public void entityAppearanceChanges(EntityClass ec)
	{
		Vector				srcRelList = getSrcRelList();
		RelationInstance	ri;
		EntityInstance		e;
		int					i;

		if (ec == getParentClass()) {
			repaint();
		}

		if (isMarked(IN_DIAGRAM) && !isOpen()) {
			return;
		}

		if (srcRelList != null) {
			for (i = srcRelList.size(); --i >= 0; ) {
				ri  = (RelationInstance) srcRelList.elementAt(i);
				if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
					e = ri.getDst();
					e.entityAppearanceChanges(ec);
	}	}	}	}

	public void entityIconChanges(EntityClass ec)
	{
		Vector srcRelList = getSrcRelList();
		RelationInstance ri;
		EntityInstance e;
		int i;

		if (ec == null || ec == getParentClass())	{
			EntityComponent entityComponent = m_entityComponent;

			if (entityComponent != null) {
				entityComponent.clearScaledIcon();
			}
			repaint();
		}

		if (isMarked(IN_DIAGRAM) && !isOpen()) {
			return;
		}

		if (srcRelList != null)	{
			for (i = srcRelList.size(); --i >= 0; )	{
				ri = (RelationInstance)srcRelList.elementAt(i);
				if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
					e = ri.getDst();
					e.entityIconChanges(ec);
				}
			}
		}
	}

	public void edgeAppearanceChanges(RelationClass rc)
	{
		Vector				list;
		RelationInstance	ri;
		EntityInstance		e;
		int					i;


		list = getSrcLiftedList();
		if (list != null) {
			for (i = list.size(); --i >= 0; ) {
				ri  = (RelationInstance) list.elementAt(i);
				if (ri.getRelationClass() == rc) {
					ri.repaint();
		}	}	}

		if (isMarked(IN_DIAGRAM) && !isOpen()) {
			return;
		}

		list = getSrcRelList();
		if (list != null) {
			for (i = list.size(); --i >= 0; ) {
				ri  = (RelationInstance) list.elementAt(i);
				if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
					e = ri.getDst();
					e.edgeAppearanceChanges(rc);
	}	}	}	}

	// -----------------------------
	// Change to relations managment
	// -----------------------------

	public void clearValidatedMark()
	{
		Vector			 relList = m_dstRelList;
		RelationInstance ri;
		Enumeration		 en;
		EntityInstance	 e;

		if (relList != null) {
			for (en = relList.elements(); en.hasMoreElements(); ) {
				ri  = (RelationInstance) en.nextElement();
				ri.nandMark(RelationInstance.VALIDATED_MARK);
		}	}

		relList = m_srcRelList;
		if (relList != null) {
			for (en = relList.elements(); en.hasMoreElements(); ) {
				ri  = (RelationInstance) en.nextElement();
				ri.nandMark(RelationInstance.VALIDATED_MARK);
			}

			for (en = getChildren(); en.hasMoreElements(); ) {
				e = (EntityInstance) en.nextElement();
				e.clearValidatedMark();
	}	}	}

	// Used when edge points move

	public void revalidateAllMyEdgesForClass(RelationClass rc) 
	{
		Vector				liftedList      = getSrcLiftedList();
		RelationInstance	ri;
		int					i;

		if (liftedList != null) {
			for (i = liftedList.size(); --i >= 0; ) {
				ri = (RelationInstance) liftedList.elementAt(i);
				if (rc != null && ri.getRelationClass() != rc) {
					continue;
				}
				if (ri.drawSrc() != this) {
					System.out.println("revalidateAllMyEdges() " + this + " drawsrc " + ri.drawSrc());
					continue;
				}
				ri.validate();
		}	}

		liftedList = getDstLiftedList();
		if (liftedList != null) {
			for (i = liftedList.size(); --i >= 0; ) {
				ri = (RelationInstance) liftedList.elementAt(i);
				if (rc != null && ri.getRelationClass() != rc) {
					continue;
				}
				if (ri.drawDst() != this) {
					System.out.println("revalidateAllMyEdges() " + this + " drawdst " + ri.drawDst());
					continue;
				}
				ri.validate();
	}	}	}

/*
		v6.0.3
		Stupid to rescale -- might as well recalculate 
 */

	public void revalidateAllMyEdges() 
	{

		Vector				liftedList      = getSrcLiftedList();
		RelationInstance	ri;
		int					i;

		if (liftedList != null) {
			for (i = liftedList.size(); --i >= 0; ) {
				ri = (RelationInstance) liftedList.elementAt(i);
				ri.validate();
		}	}

		liftedList = getDstLiftedList();
		if (liftedList != null) {
			for (i = liftedList.size(); --i >= 0; ) {
				ri = (RelationInstance) liftedList.elementAt(i);
				ri.validate();
	}	}	}

	// Edges

	public void invalidateAllEdges() 
	{ 
		Enumeration		 en = srcLiftedRelationElements();
		RelationInstance ri;

//		System.out.println("EntityInstance.invalidateAllEdges " + this);
		
		if (en != null) {
			while (en.hasMoreElements()) {
				ri = (RelationInstance) en.nextElement();
				ri.invalidateEdge();
		}	}	

		en = dstLiftedRelationElements();
		if (en != null) {
			while (en.hasMoreElements()) {
				ri = (RelationInstance) en.nextElement();
				ri.invalidateEdge();
	}	}	}	

	// --------------------------
	// Diagram validation support
	// --------------------------

	// This is the entity that relations pointing at this entity should be lifted to

	public EntityInstance getDrawEntity()
	{
		return m_drawEntity;
	}

	public void setDrawEntity(EntityInstance drawEntity)
	{
		m_drawEntity = drawEntity;
	}

	/* Resets all the flags
	 * Opens the draw root
	 * Clears any sizing/location information in EntityComponents
	 * Sets all reachable nodes under the drawroot to UNDER_DRAWROOT

	 * Flag values:
	 *		Not yet under drawRoot or beneath a hard closed entity under drawroot -> 0
	 *		Else -> UNDER_DRAWROOT_MARK 
	 */

	public void clearLiftedEdges(int flags, boolean liftEdges, boolean hide) 
	{
		Vector			srcRelList      = m_srcRelList;
		EntityComponent entityComponent = m_entityComponent;

		m_drawEntity    = null;		// Signal that this node is of no interest in lifting relations

		if (entityComponent != null) {
			entityComponent.clearLiftedEdges();
		}

		andMark(PERMANENT_MARKS | PRESENTATION_MARKS);
		
		if (hide && !isMarked(TRACED_MARK)) {
			orMark(HIDDEN_MARK);
		}

		if (flags != 0) {
//			System.out.println(this + " under draw root");
			orMark(flags);

			if (!liftEdges && !isStateOpen()) {
				/* Under this can't be logically reachable by client/supplier 
				 * but keep going down so sure all flags cleared
				 * N.B. Don't use the test isOpen() because the entity does
				 *      not yet have sizing information
				 */
				flags = 0;
			}
		} else {
			if (isMarked(DRAWROOT_MARK)) {
				setOpen();
				flags = UNDER_DRAWROOT_MARK;
		}	}
		
		if (entityComponent != null) {
			entityComponent.removeAll();
		}
				
		if (srcRelList != null) {
			RelationInstance	ri;
			EntityInstance		e;
			int					i;

			// Doing all the source points clears all edges since all edges have a source

			for (i = srcRelList.size(); --i >= 0; ) {
				ri = (RelationInstance) srcRelList.elementAt(i);
				ri.clearRelationMark();
				if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
					ri.getDst().clearLiftedEdges(flags, liftEdges, hide);
	}	}	}	}

	// N.B. This finds Client and Suppliers without regard to open/closed entity states
	//      Indeed were it to consider these states it would have to be very careful
	//      since the pseudo closed state is dependent on entity sizes which are not
	//      yet computed.

	public void findClientsSuppliers(ClientSet clientSet, SupplierSet supplierSet, EntityInstance drawRoot, EntityInstance found, boolean visibleEntities, boolean visibleEdges, boolean liftEdges)
	{
		if (this == drawRoot) {
			// Things at under the drawRoot are never client/suppliers
			return;
		}

		Vector				srcRelList = getSrcRelList();
		EntityInstance		found1     = found;
		RelationInstance	ri;
		RelationClass		rc;
		EntityInstance		e; 
		int					i;

//		System.out.println("Finding Clients/Suppliers for " + this);

		if (!hasDescendant(drawRoot)) {
			// Things on the path to the drawroot are never client suppliers
			// But it can still have children that might be since they may lie off the root->draw root path

			if (visibleEntities) {
				if (!isShown()) {
					// Ignore invisible entities as possible clients/suppliers
					return;
			}	}

			if (supplierSet != null) {
				// Interested in knowing about suppliers
				if (found1 == null || !found1.isMarked(SUPPLIER_MARK)) {
					// Haven't yet found a supplier under this subtree

					Vector dstRelList = m_dstRelList;

					// Examine all visible incoming edges to see if any comes from the diagram

					if (dstRelList != null) {
						for (i = dstRelList.size(); --i >= 0; ) {
							ri = (RelationInstance) dstRelList.elementAt(i);
							if (visibleEdges) {
								// Ignore invisible relations if this is set
								rc = ri.getRelationClass();
								if (!rc.isShown()) {
									continue;
							}	}
							e  = ri.getSrc();
							if (e.isMarked(UNDER_DRAWROOT_MARK | DRAWROOT_MARK)) {
								// This node is a supplier
								if (found1 == null) {
									found1 = this;
								} 
//								System.out.println("Supplier for " + e);
								found1.orMark(SUPPLIER_MARK);
								supplierSet.seenMember();
								break;
			}	}	}	}	}

			if (srcRelList != null) {
				if (clientSet != null) {
					// Interested in knowing about clients
					if (found1 == null || !found1.isMarked(CLIENT_MARK)) {
						// Haven't yet found a client under this subtree

						// Examine all visible outgoing edges to see if any goes to the diagram

						for (i = srcRelList.size(); --i >= 0; ) {
							ri = (RelationInstance) srcRelList.elementAt(i);
							if (visibleEdges) {
								rc = ri.getRelationClass();
								if (!rc.isShown()) {
									continue;
							}	}
							e  = ri.getDst();
							if (e.isMarked(UNDER_DRAWROOT_MARK | DRAWROOT_MARK)) {
//								System.out.println("Client for " + e);
								if (found1 == null) {
									found1 = this;
								} 
								found1.orMark(CLIENT_MARK);
								clientSet.seenMember();
								break;
		}	}	}	}	}	}

		if (srcRelList != null) {

			// If we are not lifting edges stop descent if see a closed entity
			// Also stop the descent if we have now found both clients and suppliers under the subtree

			if ((liftEdges || isStateOpen() || hasDescendant(drawRoot)) && (found1 == null || !found1.isAllMarked(CLIENT_MARK | SUPPLIER_MARK))) {
					
				for (i = srcRelList.size(); --i >= 0; ) {
					ri = (RelationInstance) srcRelList.elementAt(i);
					if (!ri.isMarked(RelationInstance.SPANNING_MARK)) {
						continue;
					}
					if (visibleEdges) {
						rc = ri.getRelationClass();
						if (!rc.isShown()) {
							continue;
					}	}
					e = ri.getDst();
					e.findClientsSuppliers(clientSet, supplierSet, drawRoot, found1, visibleEntities, visibleEdges, liftEdges);
		}	}	}

		if (found != found1) {
			// We first discovered this client/supplier node
			if (isMarked(SUPPLIER_MARK)) {
//				System.out.println(this + " is supplier");
				supplierSet.addMember(this);
			} else {
//				System.out.println(this + " is supplier");
				clientSet.addMember(this);
	}	}	}

	/* For each entity under at/under the chosen root set m_drawEntity to the lowest open visible entity at above this entity */

	public void computeDrawEntity(EntityInstance closedEntity, int mark, boolean visibleEntities, boolean liftEdges)
	{
		Vector			 srcRelList = m_srcRelList;
		RelationInstance ri;
		int				 i;

		if (visibleEntities) {
			// We can find entities under client suppliers that are not shown if visibleEntities == false (so we must continue to lift them)
			if (!isShown()) {
				return;
		}	}

		if (closedEntity == null) {
			orMark(mark);
			m_drawEntity = this;
//			System.out.println("EntityInstance.computeDrawEntity " + this + " width=" + getWidth() + " height=" + getHeight());
			if (!isOpen()) {
				if (!liftEdges) {
					return;
				}
				closedEntity = this;
				mark         = 0;
			}
		} else {
			m_drawEntity	  = closedEntity;
			m_entityComponent = null;	// Release this EntityComponent -- its not visible
		}

		if (srcRelList != null) {
			for (i = srcRelList.size(); --i >= 0; ) {
				ri = (RelationInstance) srcRelList.elementAt(i);
				if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
					ri.getDst().computeDrawEntity(closedEntity, mark, visibleEntities, liftEdges);
	}	}	}	}

	protected void liftDst(RelationInstance ri)
	{
		Vector dstLiftedList = m_entityComponent.getNeededDstLiftedList();
		dstLiftedList.add(ri);	// Add this relation to the vector of relations that have draw destination to this entity
	}

	// Only lifts the edge if another identical lifted edge has not yet been lifted

	protected void liftSrc(RelationInstance ri, EntityInstance dst)
	{
		Vector					srcLiftedList = m_entityComponent.getNeededSrcLiftedList();
		RelationInstance		other;
		int						i;

		if (!ri.isMarked(RelationInstance.PRESENTATION_MARKS)) {		// Always lift things that are highlighted
			for (i = srcLiftedList.size(); --i >= 0; ) {
				other = (RelationInstance) srcLiftedList.elementAt(i);
				if (dst == other.drawDst()) {					// Must have the same lifted source
					if (ri.getParentClass() == other.getParentClass()) {
						other.incrementFrequency(ri);
//						System.out.println(ri + " same as " + other + " freq now=" + other.getFrequency());
						return;
		}	}	}	}

		ri.initFrequency();
		srcLiftedList.add(ri);	// Add this relation to the vector of relations that have draw source in this entity
		dst.liftDst(ri);
	}

	/* This is the second part of computeDrawEntity and must match its entity visit behaviour
	 * Visit all the edges in/under the diagram that must be lifted and lift them
	 * It only descends into nodes under the drawroot and the rule for an edge being lifted
	 * is that it has both a src and a dst which themselves know what the lifted src/dst is
	 */

	public void liftAllDiagramEdges(EntityInstance drawRoot, boolean liftEdges)
	{
		Vector				srcRelList = m_srcRelList;
		RelationInstance	ri;
		EntityInstance		dst;
		int					i;

//		System.out.println("EntityInstance.liftAllDiagramEdges " + this);

		{
			Vector				dstRelList = m_dstRelList;
			EntityInstance		drawSrc, drawDst, src;

			// Even if this entityInstance is not shown we still have to lift all entities if we are to be able to
			// do forward and back traces
			
			if (dstRelList != null) {
				for (i = dstRelList.size(); --i >= 0; ) {		// Each thing for which I am the target
					ri  = (RelationInstance) dstRelList.elementAt(i);
					if (!ri.isMarked(RelationInstance.LIFTED_MARK)) {
						ri.orMark(RelationInstance.LIFTED_MARK);
						if (!ri.isMarked(RelationInstance.SPANNING_MARK)) {
							drawDst = ri.drawDst();
							if (drawDst == null) {
								// Going to something we are not interested in
								continue;
							}
							if (!isMarked(DRAWROOT_MARK) /* && drawRoot != null */) {	// Could use drawRoot null to disable
								src = ri.getSrc();
								if (drawRoot.hasAncestor(src)) {
									src.m_drawEntity = drawRoot;
									drawRoot.liftSrc(ri, drawDst);
									continue;
							}	}
											
							drawSrc = ri.drawSrc();					// The draw entity of the node addressed by ri.src()
							if (drawSrc == null) {
								// Coming from something we are not interested in
								continue;
							}

							if (!drawSrc.isMarked(DIAGRAM_MARK | CLIENT_MARK)) {
								continue;
							}
							if (drawSrc == drawDst && (drawSrc != ri.getSrc() || drawDst != ri.getDst()) ) {
								// Ignore lifting to common entity
								continue;
							}
							drawSrc.liftSrc(ri, drawDst);
			}	}	}	}
		
			if (srcRelList != null) {
				for (i = srcRelList.size(); --i >= 0; ) {
					ri  = (RelationInstance) srcRelList.elementAt(i);
					if (!ri.isMarked(RelationInstance.LIFTED_MARK)) {
						ri.orMark(RelationInstance.LIFTED_MARK);
						if (!ri.isMarked(RelationInstance.SPANNING_MARK)) {
							drawSrc = ri.drawSrc();
							if (drawSrc == null) {
								continue;
							}
							drawDst = ri.drawDst();
							if (drawDst == null) {
								continue;
							}
							if (!drawDst.isMarked(DIAGRAM_MARK | SUPPLIER_MARK)) {
								continue;
							}
							if (drawSrc == drawDst && (drawSrc != ri.getSrc() || drawDst != ri.getDst()) ) {
								continue;
							}
							drawSrc.liftSrc(ri, drawDst);
			}	}	}	}
		
			if (!liftEdges && !isOpen()) {
				return;
		}	}

		if (srcRelList != null) {
			for (i = srcRelList.size(); --i >= 0; ) {
				ri = (RelationInstance) srcRelList.elementAt(i);
				if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
					dst = ri.getDst();
					dst.liftAllDiagramEdges(drawRoot, liftEdges);
	}	}	}	}

	
	/* Compute the average position on the x axis of all drawn edges in/out of me */
	/* Used to order clients and suppliers */

	public void computeAvgX() 
	{
		Option			diagramOptions = Options.getDiagramOptions();
		EntityComponent ec             = neededComponent();

		Enumeration			en;
		RelationInstance	ri;
		EntityInstance		src, dst;
		int					edgeMode;
		double				f, avg;
		double				x = 0.0;
		int					n = 0;

		edgeMode = diagramOptions.getEdgeMode();
		if (edgeMode == Option.DIRECT_EDGE_STATE) {
			// For every edge coming out of us consider the destination
			en = srcLiftedRelationElements();
			if (en != null) {
				while (en.hasMoreElements()) {
					ri  = (RelationInstance) en.nextElement();
					dst = ri.drawDst();
					x  += (dst.getDiagramX() + dst.getWidth()/2);
					++n;
			}	}
			// For every edge coming into us consider the source
			en = dstLiftedRelationElements();
			if (en != null) {
				while ( en.hasMoreElements()) {
					ri  = (RelationInstance) en.nextElement();
					src = ri.drawSrc();
					x  += (src.getDiagramX() + src.getWidth()/2);
					++n;
			}	}
		} else {
			en = srcLiftedRelationElements();
			if (en != null) {
				while (en.hasMoreElements()) {
					ri  = (RelationInstance) en.nextElement();
					dst = ri.drawDst();
					f   = ri.getRelationClass().getRelativeIOfactor();
					x  += (dst.getDiagramX() + dst.getWidth()*f);
					++n;
			}	}
			en = dstLiftedRelationElements();
			if (en != null) {
				while ( en.hasMoreElements() ) {
					ri  = (RelationInstance) en.nextElement();
					src = ri.drawSrc();
					f   = ri.getRelationClass().getRelativeIOfactor();
					x  += (src.getDiagramX() + src.getWidth()*f);
					++n;
		}	}	}
		if (n > 0) {
			avg = x/((double) n);
		} else {
			avg = -1.0;
		}
		ec.setAvgX(avg);
	}

	public double getAvgX()
	{
		EntityComponent ec = neededComponent();

		return ec.getAvgX();
	}

	public void drawAllEdges(Diagram diagram, boolean normal)	// If not normal showing lighlight edges only regardless of elision 
	{ 
		Vector			 list;
		RelationInstance ri;
		EntityInstance	 child;
		int				 i;

//		System.out.println("EntityInstance.drawAllEdges " + this);
		
		if (!isMarked(HIDDEN_MARK) || isMarked(DRAWROOT_MARK)) {
			list = getSrcLiftedList();
			if (list != null) {
				for (i = list.size(); --i >= 0; ) {
					ri = (RelationInstance) list.elementAt(i);
					if (normal || (ri.getHighlightFlag() && ri.getRelationClass().isActive())) {
						ri.drawRelation(diagram, normal);	// if normal allow elision from source
			}	}	}

			list = getDstLiftedList();
			if (list != null) {
				for (i = list.size(); --i >= 0; ) {
					ri = (RelationInstance) list.elementAt(i);
					if (normal || (ri.getHighlightFlag() && ri.getRelationClass().isActive())) {
						ri.drawRelation(diagram, normal);	// if normal allow elision from source
			}	}	}

			if (isOpen()) {
				list = m_srcRelList;
				if (list != null) {
					for (i = list.size(); --i >= 0; ) {
						ri = (RelationInstance) list.elementAt(i);
						if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
							child = ri.getDst();
							if (child.isShown()) {
								child.drawAllEdges(diagram, normal);
		}	}	}	}	}	}

//		System.out.println("EntityInstance.drawAllEdges " + this + " done");
	}

	// --------------------
	// Attribute Management
	// --------------------

	// The routines that follow hide the complexity of getting/setting attribute values
	// from EditAttributes
 
	public int getPrimaryAttributeCount()
	{
		return(ATTRS);
	}

	public String getLsAttributeNameAt(int index)
	{
		String	name;

		if (index < ATTRS) {
			name = attributeName[index];
		} else {
			name  = super.getLsAttributeNameAt(index);
		}
		return(name);
	}

	// Need to know the type in cases where value might be null
	// For example with some colors

	public int getLsAttributeTypeAt(int index)
	{
		int		ret;
		
		if (index < ATTRS) {
			ret = attributeType[index];
		} else {
			ret = super.getLsAttributeTypeAt(index);
		}
		return(ret);
	}

	public Object getLsAttributeValueAt(int index)
	{
		Object				 value;

		switch (index) {
		case ID_ATTR:
			value = getId();
			break;
		case CLASS_ATTR:
		{
			LandscapeClassObject parentClass = getParentClass();

			if (parentClass == null) {
				value = null;
			} else {
				value = parentClass.getLabelId();
			}
			break;
		}
		case LABEL_ATTR:
			value = getLabel();
			break;
		case DESC_ATTR:
			value = getDescription();
			break;
		case COLOR_ATTR:
			value = getObjectColor();
			break;
		case LABEL_COLOR_ATTR:
			value = getLabelColor();
			break;
		case OPEN_COLOR_ATTR:
			value = getColorWhenOpen();
			break;
		case XRELPOSITION_ATTR:
			value = new Double(xRelLocal());
			break;
		case YRELPOSITION_ATTR:
			value = new Double(yRelLocal());
			break;
		case WIDTHREL_ATTR:
			value = new Double(widthRelLocal());
			break;
		case HEIGHTREL_ATTR:
			value = new Double(heightRelLocal());
			break;
		case FONTDELTA_ATTR:
			value = new Integer(m_fontDelta);
			break;
		default:
			value = super.getLsAttributeValueAt(index);
		}
		return(value);
	}

	// ------------------
	// TA Reading support
	// ------------------

	//
	// Parse the values of any first order attributes into their storage.
	// 

	// First thing called by EntityInstance::AddAttributes
	// If the attribute is a known member of storage -- don't save attribute -- just store value

	public boolean processFirstOrder(String id, String value)
	{
		int	values;
		int	numRelClasses;

		if (super.processFirstOrder(id, value)) {
			return true;
		}

		if (id.equals(XRELPOSITION_ID)) {
			if (value != null) {
				if (value.charAt(0) != '(') {
					setXRelLocalBounded(Util.parseRelativeValue(value));
				} else {
					values = Attribute.countValues(value);
					if (values > 0) {
						setPositionArraySize(values);
						AttributeValue.parseXEntityPositions(value, m_positions);
						setXRelLocalBounded(m_positions[0].m_xRelLocal);
			}	}	}
			return true; 
		}

		if (id.equals(YRELPOSITION_ID)) {
			if (value != null) {
				if (value.charAt(0) != '(') {
					setYRelLocalBounded(Util.parseRelativeValue(value));
				} else {
					values = Attribute.countValues(value);
					if (values > 0) {
						setPositionArraySize(values);
						AttributeValue.parseYEntityPositions(value, m_positions);
						setYRelLocalBounded(m_positions[0].m_yRelLocal);
			}	}	}
			return true; 
		}

		if (id.equals(WIDTHREL_ID)) {
			if (value != null) {
				if (value.charAt(0) != '(') {
					setWidthRelLocalBounded(Util.parseRelativeValue(value));
				} else {
					values = Attribute.countValues(value);
					if (values > 0) {
						setPositionArraySize(values);
						AttributeValue.parseWidthEntityPositions(value, m_positions);
						setWidthRelLocalBounded(m_positions[0].m_widthRelLocal);
			}	}	}
			return true; 
		}

		if (id.equals(HEIGHTREL_ID)) {
			if (value != null) {
				if (value.charAt(0) != '(') {
					setHeightRelLocalBounded(Util.parseRelativeValue(value));
				} else {
					values = Attribute.countValues(value);
					if (values > 0) {
						setPositionArraySize(values);
						AttributeValue.parseHeightEntityPositions(value, m_positions);
						setHeightRelLocalBounded(m_positions[0].m_heightRelLocal);
			}	}	}
			return true; 
		}

		if (id.equals(LABEL_ID)) {
			if (value != null) {
				setLabel(Attribute.parseStringValue(value));
			}
			return true; 
		}

		if (id.equals(DESC_ID)) {
			if (value != null) { 
				setDescription(Attribute.parseStringValue(value));
			}
			return true; 
		} 

		if (id.equals(FONTDELTA_ID)) {
			if (value != null) {
				setFontDelta(Attribute.parseIntValue(value));
			}
			return true; 
		}

		if (id.equals(IN_ELISION_ID)) {
			if (value != null) { 
				Attribute.parseElisionsValue(value, getTa(), DST_ELISION, needElisions()); 
			}
			return true; 
		}

		if (id.equals(OUT_ELISION_ID)) {
			if (value != null) {
				Attribute.parseElisionsValue(value, getTa(), SRC_ELISION, needElisions());
			}
			return true;
		}

		if (id.equals(CLIENT_ELISION_ID)) {
			if (value != null) {
				Attribute.parseElisionsValue(value, getTa(), ENTERING_ELISION, needElisions());
			}
			return true;
		}

		if (id.equals(SUPPLIER_ELISION_ID)) {
			if (value != null) {
				Attribute.parseElisionsValue(value, getTa(), EXITING_ELISION, needElisions());
			}
			return true;
		}

		if (id.equals(INTERNAL_ELISION_ID)) {
			if (value != null) {
				Attribute.parseElisionsValue(value, getTa(), INTERNAL_ELISION, needElisions());
			}
			return true;
		}

		if (id.equals(CLOSED_ELISION_ID)) {
			if (value != null) {
				Attribute.parseElisionsValue(value, getTa(), CLOSED_ELISION, needElisions());
			}
			return true;
		}

		if (id.equals(INPOINT_ID)) {
			if (value != null) {
				EdgePoint[] edgePoints = needEdgePoints();
				AttributeValue.parsePoints(value, this, EdgePoint.TOP, edgePoints);
			}
			return true; 
		}

		if (id.equals(OUTPOINT_ID)) {
			if (value != null) {
				EdgePoint[] edgePoints = needEdgePoints();
				AttributeValue.parsePoints(value, this, EdgePoint.BOTTOM, edgePoints); 
			}
			return true; 
		}

		if (id.equals(LEFTPOINT_ID)) {
			if (value != null) {
				EdgePoint[] edgePoints = needEdgePoints();
				AttributeValue.parsePoints(value, this, EdgePoint.LEFT, edgePoints); 
			}
			return true; 
		}

		if (id.equals(RIGHTPOINT_ID)) {
			if (value != null) {
				EdgePoint[] edgePoints = needEdgePoints();
				AttributeValue.parsePoints(value, this, EdgePoint.RIGHT, edgePoints); 
			}
			return true; 
		}
		
		if (id.startsWith("view_") && getId().equals(Ta.ROOT_ID)) {
			Diagram				diagram = getDiagram();
			diagram.addRootView(id, value);
			return true;
		}

		return false; 
	}

	// ------------------
	// TA writing methods 
	// ------------------

	public void writeInstance(PrintWriter ps)
	{
		// Write an instance line for ourself

		ps.println(Ta.INSTANCE_ID + " " + qt(getId()) + " " + qt(getParentClass().getId()));
	}

	public void writeInstances(PrintWriter ps)
	{
		Enumeration en;

		// Write an instance line for ourself, and then all our children

		writeInstance(ps);
		for (en = getChildren(); en.hasMoreElements(); ) {
			EntityInstance child = (EntityInstance) en.nextElement();
			child.writeInstances(ps);
		}
	}

	public void writeRelations(PrintWriter ps)
	{
		// Write out the relations on us, and then all our children
		// By writing out relations to us, we also include edges from m_rootInstance

		Vector			 srcRelList = m_srcRelList;

		if (srcRelList != null) {
			Enumeration		 en; 
			RelationInstance ri;

			for (en = srcRelList.elements(); en.hasMoreElements(); ) {
				ri = (RelationInstance) en.nextElement();
				ri.writeRelation(ps); 
			}				

			for (en = getChildren(); en.hasMoreElements(); ) {
				EntityInstance child = (EntityInstance) en.nextElement();
				child.writeRelations(ps);
		}	}
	}

	protected String writeElision(PrintWriter ps, String nodeId, Vector rcs, BitSet elisions, int type, String id) 
	{
		int				index = -1;
		int				nid;
		RelationClass	rc;
		String			value = null;

		while ((index = elisions.nextSetBit(++index)) >= 0) {
			if ((index % ELISIONS) != type) {
				continue;
			}
			nid = index / ELISIONS;
			rc  = (RelationClass) rcs.elementAt(nid);
			if (rc == null) {
				continue;
			}
			if (value == null) {
				value  = "";
			} else {
				value += " ";
			}
			value += rc.getId();
		}
		if (value != null) {
			nodeId = writeAttribute(ps, nodeId, id, "(" + value + ")");
		} 
		return nodeId;
	}

	protected String writeElisions(PrintWriter ps, String id)
	{
		BitSet	elisions = m_elisions;

		if (elisions != null) {
			Ta				ta		   = getTa();
			Vector			rcs        = ta.getRelationClasses();

			id = writeElision(ps, id, rcs, elisions, DST_ELISION,		IN_ELISION_ID);
			id = writeElision(ps, id, rcs, elisions, SRC_ELISION,		OUT_ELISION_ID);
			id = writeElision(ps, id, rcs, elisions, ENTERING_ELISION,	CLIENT_ELISION_ID);
			id = writeElision(ps, id, rcs, elisions, EXITING_ELISION,	SUPPLIER_ELISION_ID);
			id = writeElision(ps, id, rcs, elisions, INTERNAL_ELISION,	INTERNAL_ELISION_ID);
			id = writeElision(ps, id, rcs, elisions, CLOSED_ELISION,	CLOSED_ELISION_ID);
		}
		return id;
	}

	protected String writePoints(PrintWriter ps, String nodeId, EdgePoint[] edgePoints, Vector rcs, int side, String name) 
	{

		int	start,	i, length;
		EdgePoint	edgePoint;
		int			rcIndex = 1;	/* Ignore the zero rc nid */
		String		value = "";

		start  = rcIndex * EdgePoint.SIDES+side;
		length = edgePoints.length;

		for (i = start; i < length; i += EdgePoint.SIDES) {
			edgePoint = edgePoints[i];
			if (edgePoint != null) {
				break;
		}	}

		if (i < length) {
			for (i = start; i < length; i += EdgePoint.SIDES) {
				edgePoint = edgePoints[i];
				if (edgePoint != null) {
					value += edgePoint.getString((RelationClass) rcs.elementAt(rcIndex));
				}
				++rcIndex;
			}
			if (value.length() > 0) {
				nodeId = writeAttribute(ps, nodeId, name, "(" + value + ")");
		}	}
		return nodeId;
	}

	protected String writePoints(PrintWriter ps, String nodeId)
	{
		EdgePoint[] edgePoints = m_edgePoints;

		if (edgePoints != null) {
			Ta				ta		   = getTa();
			Vector			rcs        = ta.getRelationClasses();
			int				rcsSize    = rcs.size();
			int				rcIndex    = -1;
			int				side       = EdgePoint.SIDES;
			RelationClass	rc         = null;
			int				cnt        = 0;
			EdgePoint		edgePoint;
			int				i;
			
			for (i = 0; i < edgePoints.length; ++i) {
				edgePoint = edgePoints[i];
				if (side == EdgePoint.SIDES) {
					++rcIndex;
					if (rcIndex < rcsSize) {
						rc = (RelationClass) rcs.elementAt(rcIndex);
					} else {
						rc = null;
					} 
					side = 0;
				}
				if (edgePoint != null) {
					if (rc == null || edgePoint.isDefault(rc)) {
						edgePoints[i] = null;
					} else {
						++cnt;
				}	}
				++side;
			}

			if (cnt == 0) {
				m_edgePoints = null;
			} else {
				nodeId = writePoints(ps, nodeId, edgePoints, rcs, EdgePoint.TOP,    INPOINT_ID);
				nodeId = writePoints(ps, nodeId, edgePoints,	rcs, EdgePoint.BOTTOM, OUTPOINT_ID);
				nodeId = writePoints(ps, nodeId, edgePoints, rcs, EdgePoint.LEFT,   LEFTPOINT_ID);
				nodeId = writePoints(ps, nodeId, edgePoints, rcs, EdgePoint.RIGHT,  RIGHTPOINT_ID);
		}	}
		return nodeId;
	}

	public void	writeOptionsAttributes(PrintWriter ps)
	{  
		LandscapeClassObject parentClass    = getParentClass();
		Diagram				 diagram        = getDiagram();
		EntityInstance		 drawRoot       = diagram.getDrawRoot();
		Option				 diagramOptions = Options.getDiagramOptions();
		Vector				 views          = diagram.getViews();
		String				 nodeId         = qt(getId());

		// Write out attribute record for us

		nodeId = writeAttribute(ps, nodeId, "version", Version.InternalNumber());

		if (drawRoot != null) {
		
			nodeId = writeColorAttributes(ps, nodeId, parentClass); 

			nodeId = writeAttribute(ps, nodeId, "diagram:drawroot", drawRoot.getId());
			
			if (diagramOptions.getZoomX() != 1.0 || diagramOptions.getZoomY() != 1.0) {
				JScrollPane	scrollPane = diagram.getLs().m_scrollDiagram;
				JViewport	viewport   = scrollPane.getViewport();
				Point		point	   = viewport.getViewPosition();

				nodeId = writeAttribute(ps, nodeId, "diagram:viewx", point.x);
				nodeId = writeAttribute(ps, nodeId, "diagram:viewy", point.y);
		}	}
		 
		diagramOptions.saveOptions(ps, true);
		
		if (views != null) {
			int		size = views.size();
			int		i;
			View	view;
			
			for (i = 0; i < size; ++i) {
				view = (View) views.elementAt(i);
				ps.print("view_" + i + "=\"");
				view.write(ps);
				ps.println("\"");
		}	}

		String label = getLabel();
		if (label == null) {
			label = getEntityLabel();
		}

		if (label != null) {
			ps.println(LABEL_ID + " = " + qt(label));
		}

		String description = getDescription();

		if (description != null) {
			ps.println(DESC_ID + " = " + qt(description));
		}

		// End the record 

		ps.println("}"); 
	}

	public void	writeAttribute(PrintWriter ps, RelationClass containsClass)
	{  
		LandscapeClassObject parentClass = getParentClass();
		String				 nodeId      = getId();
		short				 value;
		EntityPosition[]	 positions;
		EntityPosition		 position;
		int					 i, last;

		// Write out attribute record for us

		if (containsClass != null) {
			positions = m_positions;
			if (positions != null || containsClass.getCIndex() != 0) {
				savePositioning(containsClass);
				positions = m_positions;
			}
			// Write out Rectangle attributes
			if (positions == null) {
				last = -1;
			} else {
				for (last = positions.length; --last >= 0;) {
					position = positions[last];
					if (position != null) {
						if (Util.defined(position.m_xRelLocal) || Util.defined(position.m_yRelLocal) || Util.defined(position.m_widthRelLocal) || Util.defined(position.m_heightRelLocal)) {
							break;
			}	}	}	}

			if (last < 0) {
				value = m_xRelLocal;		// Use the actual values
				if (Util.defined(value)) {
					nodeId = writeAttribute(ps, nodeId, "xrel     ", value);
				}
				value = m_yRelLocal;		// Use the actual values
				if (Util.defined(value)) {
					nodeId = writeAttribute(ps, nodeId, "yrel     ", value);
				}
				value = m_widthRelLocal;
				if (Util.defined(value)) {
					nodeId = writeAttribute(ps, nodeId, "widthrel ", value); 
				}
				value = m_heightRelLocal;
				if (Util.defined(value)) {
					nodeId = writeAttribute(ps, nodeId, "heightrel", value);
				}

			} else {
				String temp = "(";
				
				for (i = 0; i <= last; ++i) {
					if (i != 0) {
						temp += " ";
					}
					position = positions[i];
					if (position == null) {
						value = Util.undefined();
					} else {
						value = position.m_xRelLocal;
					}
					temp += value;
				}
				temp += ")";
				nodeId = writeAttribute(ps, nodeId, "xrel     ", temp);
				
				temp = "(";
				for (i = 0; i <= last; ++i) {
					if (i != 0) {
						temp += " ";
					}
					position = positions[i];
					if (position == null) {
						value = Util.undefined();
					} else {
						value = position.m_yRelLocal;
					}
					temp += value;
				}
				temp += ")";
				nodeId = writeAttribute(ps, nodeId, "yrel     ", temp);
				
				temp = "(";
				for (i = 0; i <= last; ++i) {
					if (i != 0) {
						temp += " ";
					}
					position = positions[i];
					if (position == null) {
						value = Util.undefined();
					} else {
						value = position.m_widthRelLocal;
					}
					temp += value;
				}
				temp += ")";
				nodeId = writeAttribute(ps, nodeId, "widthrel ", temp); 
				
				temp = "(";
				for (i = 0; i <= last; ++i) {
					if (i != 0) {
						temp += " ";
					}
					position = positions[i];
					if (position == null) {
						value = Util.undefined();
					} else {
						value = position.m_heightRelLocal;
					}
					temp += value;
				}
				temp += ")";
				nodeId = writeAttribute(ps, nodeId, "heightrel", temp);
		}	}

		String label = getLabel();

		if (label != null) {
			nodeId = writeAttribute(ps, nodeId, LABEL_ID, qt(label));
		}

		int style = getStyle();

		if (style >= 0) {
			if (parentClass == null || parentClass.getInheritedStyle() != style) {
				nodeId = writeAttribute(ps, nodeId, STYLE_ID, style);
		}	}

		String description = getDescription();

		if (description != null) {
			if (isRoot() || !parentClass.defaultValue(DESC_ID, description)) {
				nodeId = writeAttribute(ps, nodeId, DESC_ID, qt(description));
		}	}

		// First order attributes not associated with $ROOT 
		nodeId = writeElisions(ps, nodeId);

		nodeId = writePoints(ps, nodeId);

		if (m_fontDelta != 0) {
			nodeId = writeAttribute(ps, nodeId, FONTDELTA_ID, m_fontDelta);  
		}

		// Finally output the second-class attributes 

		nodeId = super.writeAttributes(ps, nodeId, parentClass, false); 

		// End the record 

		if (nodeId == null) {
			ps.println("}"); 
	}	}

	public void	writeAttributes(PrintWriter ps, RelationClass containsClass)
	{  
		Vector srcRelList = m_srcRelList;

		// Write out attribute record for us, and then our children
		
		writeAttribute(ps, containsClass);

		if (srcRelList != null) {
			RelationInstance	ri;
			EntityInstance		child;
			int					i, size;

			// Write any attributes of src relations

			size = srcRelList.size();
			for (i = 0; i < size; ++i) {
				ri = (RelationInstance) srcRelList.elementAt(i);
				ri.writeAttributes(ps);
			}

			// Recurse for contained children

			for (i = 0; i < size; ++i) {
				ri = (RelationInstance) srcRelList.elementAt(i);
				if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
					child = ri.getDst();
					child.writeAttributes(ps, ri.getRelationClass());
	}	}	}	}

	// ------------------
	// Legend box support
	// ------------------

	public void recomputeCounts()
	{
		Vector srcRelList = m_srcRelList;

		incrementClassMembers();

		if (srcRelList != null) {
			RelationInstance	ri;
			RelationClass		rc;
			Enumeration			en;
			EntityInstance		child;

			for (en = srcRelList.elements(); en.hasMoreElements(); ) {
				ri  = (RelationInstance) en.nextElement();
				rc  = ri.getRelationClass();
				rc.incrementMembers();
				if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
					child = ri.getDst();
					child.recomputeCounts();
	}	}	}	}

	// ------------------------
	// FrameModeHandler support
	// ------------------------

	public boolean isFramedBy(Rectangle frame)
	{
		int					myX = getDiagramX();
		int					myY = getDiagramY();

		if (frame.x > myX || frame.y > myY) {
			return false;
		}
		myX += getWidth();
		if (frame.x + frame.width < myX) {
			return false;
		}
		myY += getHeight();
		if (frame.y + frame.height < myY) {
			return false;
		}
		if (!isShown()) {
			return false;
		}
		return true;
	}
	 

	// -----------------------------
	// Show valid attributes support
	// -----------------------------

	public static void reportFirstOrderAttributes(ResultBox resultBox)
	{
		resultBox.addText(XRELPOSITION_ID);
		resultBox.addText(YRELPOSITION_ID);
		resultBox.addText(WIDTHREL_ID);
		resultBox.addText(HEIGHTREL_ID);
		resultBox.addText(LABEL_ID);
		resultBox.addText(DESC_ID);
		resultBox.addText(TITLE_ID);
		resultBox.addText(FONTDELTA_ID);
		resultBox.addText(COLOR_ID);
		resultBox.addText(OPEN_COLOR_ID);
		resultBox.addText(LABEL_COLOR_ID);
		resultBox.addText(IN_ELISION_ID);
		resultBox.addText(OUT_ELISION_ID);
		resultBox.addText(CLIENT_ELISION_ID);
		resultBox.addText(SUPPLIER_ELISION_ID);
		resultBox.addText(INTERNAL_ELISION_ID);
		resultBox.addText(INPOINT_ID);
		resultBox.addText(OUTPOINT_ID);
		resultBox.addText(LEFTPOINT_ID);
		resultBox.addText(RIGHTPOINT_ID);
	}

	public int validateEntityAttributes(Vector v, Vector a[], ResultBox resultBox)
	{
		EntityClass		ec1  = getEntityClass();
		int				ret  = 0;
		Vector			valid_attributes;
		Attribute[]		attributes;
		Enumeration		en;
		EntityInstance	child;
		int				i, j, k, size, attributes_length;
		Attribute		attribute, attribute1;
		boolean			seen;
		String			id;

		attributes = m_attributes;
		if (attributes != null) {
			attributes_length = attributes.length;
			size              = v.size();

			for (i = 0; i < size; ++i) {
				if (ec1 == v.elementAt(i)) {
					valid_attributes = a[i];
					if (valid_attributes == null) {
						a[i] = valid_attributes = ec1.getValidAttributes();
					}
					seen = false;
					for (j = 0; j < attributes_length; ++j) {
						attribute = attributes[j];
						if (attribute == null) {
							continue;
						}
						id        = attribute.m_id;
						for (k = valid_attributes.size(); ; ) {
							if (--k < 0) {
								if (!seen) {
									resultBox.addResultEntity(this, resultBox.getLeftShowAncestors());
									seen = true;
									++ret;
								}
								resultBox.addResultAttribute(attribute);
								break;
							}
							attribute1 = (Attribute) valid_attributes.elementAt(k);
							if (id.equals(attribute1.m_id)) {
								break;
		}	}	}	}	}	}

		for (en = getChildren(); en.hasMoreElements(); ) {
			child = (EntityInstance) en.nextElement();
			ret += child.validateEntityAttributes(v, a, resultBox);
		}
		return(ret);
	}

	public int validateRelationAttributes(Vector v, Vector a[], ResultBox resultBox)
	{
		Vector	srcRelList = m_srcRelList;
		int		ret        = 0;

		if (srcRelList != null) {
			RelationClass	rc;
			RelationInstance ri;
			Vector			valid_attributes;
			Enumeration		en1, en;
			EntityInstance	child;
			int				i, j, k, size, attributes_length;
			Attribute[]		attributes;
			Attribute		attribute, attribute1;
			boolean			seen;
			String			id;

			size = v.size();
			for (en1 = srcRelList.elements(); en1.hasMoreElements(); ) {
				ri  = (RelationInstance) en1.nextElement();
				rc  = ri.getRelationClass();
				for (i = 0; i < size; ++i) {
					if (rc == v.elementAt(i)) {
						valid_attributes = a[i];
						if (valid_attributes == null) {
							a[i] = valid_attributes = rc.getValidAttributes();
						}
						seen = false;
						attributes = ri.m_attributes;
						if (attributes == null) {
							continue;
						}
						attributes_length = attributes.length;
						for (j = 0; j < attributes_length; ++j) {
							attribute = attributes[j];
							if (attribute == null) {
								continue;
							}
							id = attribute.m_id;
							for (k = valid_attributes.size(); ; ) {
								if (--k < 0) {
									if (!seen) {
										resultBox.addRelation(ri);
										seen = true;
										++ret;
									}
									resultBox.addResultAttribute(attribute);
									break;
								}
								attribute1 = (Attribute) valid_attributes.elementAt(k);
								if (id.equals(attribute1.m_id)) {
									break;
						}	}	}
						break;
			}	}	}

			for (en = getChildren(); en.hasMoreElements(); ) {
				child = (EntityInstance) en.nextElement();
				ret += child.validateRelationAttributes(v, a, resultBox);
		}	}
		return(ret);
	}

	public int validateRelations(Vector v, boolean a[][][], ResultBox resultBox, EntityInstance root)
	{
		Vector			relList = m_srcRelList;
		RelationClass	rc;
		RelationInstance ri;
		int				ret  = 0;
		boolean			relationArray[][];
		Enumeration		en1, en;
		EntityInstance	child;
		int				phase, i, j, size;
		Attribute		attribute, attribute1;
		boolean			seen;
		EntityInstance	s, d;
		EntityClass		src, dst;

		size = v.size();
		for (phase = 0; phase < 2; ++phase) {
			if (relList != null) {
				for (en1 = relList.elements(); en1.hasMoreElements(); ) {
					ri  = (RelationInstance) en1.nextElement();
					if (ri.isMarked(RelationInstance.VALIDATED_MARK)) {
						continue;
					}
					ri.orMark(RelationInstance.VALIDATED_MARK);
					rc  = ri.getRelationClass();
					for (i = 0; i < size; ++i) {
						if (rc == v.elementAt(i)) {
							relationArray = a[i];
							if (relationArray == null) {
								a[i] = relationArray = rc.getInheritedRelationArray();
							}
							s    = ri.getSrc();
							d    = ri.getDst();
							if (s != root && d != root) {
								src  = s.getEntityClass();
								dst  = d.getEntityClass();
								if (!relationArray[src.getOrderedId()][dst.getOrderedId()]) {
									resultBox.addRelation("", ri);
									++ret;
							}	}
							break;
			}	}	}	}
			relList = m_dstRelList;
		}

		for (en = getChildren(); en.hasMoreElements(); ) {
			child = (EntityInstance) en.nextElement();
			ret += child.validateRelations(v, a, resultBox, root);
		}
		return(ret);
	}

	// -------------------------
	// Group Unconnected support
	// -------------------------

	/* Returns true if this subtree has no edges except possibly containment edges */

	public boolean hasNoEdges()
	{
		Vector				relList = m_dstRelList;
		Enumeration			en;
		RelationInstance	ri;
		EntityInstance		child;

		if (relList != null) {
			for (en = relList.elements(); en.hasMoreElements(); ) {
				ri  = (RelationInstance) en.nextElement();
				if (!ri.isMarked(RelationInstance.SPANNING_MARK)) {
					return false;
		}	}	}

		relList = m_srcRelList;
		if (relList != null) {
			for (en = relList.elements(); en.hasMoreElements(); ) {
				ri  = (RelationInstance) en.nextElement();
				if (!ri.isMarked(RelationInstance.SPANNING_MARK)) {
					return false;
				}
				child = ri.getDst();
				if (!child.hasNoEdges()) {
					return(false);
		}	}	}

		return true;
	}

	private boolean hasActiveEdges(Vector list, RelationClass edges, int min, int max)
	{
		if (list == null) {
			if (min > 0) {
				return false;
			}
		} else {
			int					cnt = 0;
			RelationInstance	ri;
			RelationClass		rc;
			int					i;

			for (i = list.size(); --i >= 0; ) {
				ri = (RelationInstance) list.elementAt(i);
				rc = ri.getRelationClass();
				if (rc != edges) {
					if (edges != null) {
						continue;
					}
					if (ri.isMarked(RelationInstance.SPANNING_MARK) || !rc.isActive()) {
						continue;
				}	}
				if (++cnt >= min) {
					if (max == -1) {
						break;
					}
					if (cnt > max) {
						return false;
			}	}	}
			if (cnt < min) {
				return false;
		}	}
		return true;
	}

	public boolean hasActiveInEdges(RelationClass inEdges, int min, int max)
	{
		return hasActiveEdges(m_dstRelList, inEdges, min, max);
	}

	public boolean hasActiveOutEdges(RelationClass outEdges, int min, int max)
	{
		return hasActiveEdges(m_srcRelList, outEdges, min, max);
	}


	// ---------------
	// Cluster support
	// ---------------

	public void clusterMetrics(ClusterMetrics dialog, int depth)
	{
		Vector srcRelList = m_srcRelList;

		dialog.seenEntity(this, depth);

		if (srcRelList != null) {
			RelationInstance	ri;
			Enumeration			en;
			EntityInstance		child;

			for (en = srcRelList.elements(); en.hasMoreElements(); ) {
				ri  = (RelationInstance) en.nextElement();
				dialog.seenRelation(ri);
				if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
					child = ri.getDst();
					child.clusterMetrics(dialog, depth+1);
		}	}	}
	}
	public void gatherLeaves(Vector v)
	{
		Enumeration		en;
		EntityInstance	child;

		child = null;
		for (en = getChildrenShown(); en.hasMoreElements(); ) {
			child = (EntityInstance) en.nextElement();
			child.gatherLeaves(v);
		}
		if (child == null) {
			v.add(this);
	}	}

	// Used by ClusterInterface

	public void noEdges()
	{
		// N.B. We can continue to enumerate over any previously cached vector for srcRelList
		m_srcRelList    = null;
		m_dstRelList    = null;
		if (m_entityComponent != null) {
			m_entityComponent.clearLiftedEdges();
		}
	}

	public void checkRefcnts(ResultBox resultBox)
	{
		EntityClass			ec;
		Enumeration			en;
		EntityInstance		e;

		if (isShown()) {
			Attribute		attr;
		
			attr = getLsAttribute("refcnt");
			if (attr != null) {
				Vector				dstRelList = m_dstRelList;
				int					refcnt, cnt;
				RelationInstance	ri;
				String				title;

				refcnt = attr.parseInt();
				cnt    = 0;

				if (dstRelList != null) {
					for (en = dstRelList.elements(); en.hasMoreElements(); ) {
						ri  = (RelationInstance) en.nextElement();
						if (!ri.isMarked(RelationInstance.SPANNING_MARK)) {
							++cnt;
				}	}	}
				if (cnt != refcnt) {
					resultBox.addResultEntity(this, resultBox.getLeftShowAncestors());
					for (en = dstRelList.elements(); en.hasMoreElements(); ) {
						ri  = (RelationInstance) en.nextElement();
						if (!ri.isMarked(RelationInstance.SPANNING_MARK)) {
							resultBox.addRelation("    ", ri);
					}	}
					title = getTitle();
					if (title == null) {
						title = "";
					}
					resultBox.addText("    " + title + " Refcnt = " + refcnt + " but actual cnt = " + cnt);
		}	}	}
		
		for (en = getChildren(); en.hasMoreElements(); ) {
			e = (EntityInstance) en.nextElement();
			e.checkRefcnts(resultBox);
	}	}

	public void dump(int indent)
	{
		Enumeration		en;
		EntityInstance	e;
		int				i;

		for (i = 0; i < indent; ++i) {
			System.out.print(" ");
		}
		System.out.println(this);

		// Handle children

		for (en = getChildren(); en.hasMoreElements(); ) {
			e = (EntityInstance) en.nextElement();
			e.dump(indent+2);
		}
	}
	
	public void startHover()
	{
		m_mark |= HOVER_SCALE_MARK;
		if (!isDrawRoot() && m_entityComponent != null)	{
			Diagram diagram = getDiagram();

			diagram.beginUpdates();
			diagram.signalEntityChanged(this, TaListener.BOUNDS_SIGNAL);
			diagram.endUpdates();
	}	}

	public boolean endHover()
	{
		boolean ret = ((m_mark & HOVER_SCALE_MARK) != 0);
		
		if (ret) {
			m_mark &= ~HOVER_SCALE_MARK;
			if (!isDrawRoot() && m_entityComponent != null) {
				Diagram diagram = getDiagram();

				diagram.beginUpdates();
				diagram.signalEntityChanged(this, TaListener.BOUNDS_SIGNAL);
				diagram.endUpdates();
		}	}
		return ret;
	}

	// Icon interface (used to paint legends)

	public int getIconWidth()
	{
		return(getWidth());
	}

	public int getIconHeight()
	{
		return(getHeight());
	}

	public void paintIcon(Component c, Graphics g, int x, int y)
	{
		EntityComponent entityComponent = neededComponent();

		entityComponent.paintIcon(c, g, x, y);	
	}

	// MouseListener interface

	public void mouseClicked(MouseEvent ev)
	{
	}

	public void mouseEntered(MouseEvent ev)
	{
	/*
	 	if (m_label.equals("code")) {
			System.out.println("EntityInstance.mouseEntered code " + getDiagramBounds());
			repaint();
		}
	*/
	}

	public void mouseExited(MouseEvent ev)
	{
	}

	public void mousePressed(MouseEvent ev)
	{
//		System.out.println("EntityInstance.mousePressed " + this);

		if (ev.isAltDown())	{
			startHover();
		} else {
			Diagram         diagram         = getDiagram();
			int             x               = ev.getX();
			int             y               = ev.getY();
			EntityComponent entityComponent = m_entityComponent;
			int				width           = entityComponent.getWidth();
			int				height          = entityComponent.getHeight();
			LandscapeEditorCore ls;

			if (x <= EntityComponent.CONTENTS_FLAG_X_RESERVE) {
				if (y <= EntityComponent.CONTENTS_FLAG_Y_RESERVE &&
					x >= EntityComponent.CONTENTS_FLAG_X &&
					y >= EntityComponent.CONTENTS_FLAG_Y &&
					width > EntityComponent.CONTENTS_FLAG_X_RESERVE &&
					height > EntityComponent.CONTENTS_FLAG_Y_RESERVE &&
					closedWithChildren()) {
					ls = diagram.getLs();
					// Pressed the [+] button on the entity
					ls.processKey(Do.SHOW_CONTENTS, 0, this);
					return;
				}
			} else if (x >= width - EntityComponent.NAV_WIDTH) {
				if (y <= EntityComponent.NAV_WIDTH || y >= height - EntityComponent.NAV_WIDTH) {
					if (!isMarked(LEGEND_MARK)) {
						if ((EntityComponent.NAV_WIDTH << 1) <= width && (EntityComponent.NAV_WIDTH << 1) <= height) {
							// Otherwise couldn't grab entity when small
							EntityClass	ec    = getEntityClass();
							int			image = ec.getImage();
	
							if (y <= EntityComponent.NAV_WIDTH) {
								if ((image & EntityClass.ENTITY_IMAGE_NAV_NEXT) != 0) {
									ls = diagram.getLs();
									ls.processKey(Do.NAV_NEXT, Event.CTRL_MASK, this);
									return;
								}
							} else {
								if ((image & EntityClass.ENTITY_IMAGE_NAV_PREV) != 0) {
									ls = diagram.getLs();
									ls.processKey(Do.NAV_PREV, Event.CTRL_MASK, this);
									return;
			}	}	}	}	}	}

			// The X, Y values here are the logical position in the diagram
			diagram.entityPressed(ev, this, ev.getX() + getDiagramX(), ev.getY() + getDiagramY());
	}	}
	
	public void mouseReleased(MouseEvent ev)
	{
		if (!endHover()) {
			getDiagram().entityReleased(ev, this, ev.getX() + getDiagramX(), ev.getY() + getDiagramY());
	}	}

	// MouseMotionListener interface

	public void mouseDragged(MouseEvent ev)
	{
		getDiagram().entityDragged(ev, this, ev.getX() + getDiagramX(), ev.getY() + getDiagramY());
	}

	public void mouseMoved(MouseEvent ev)
	{
		Diagram				diagram = getDiagram();

		if (this != g_infoShown) {
			int	modifiers;

			LandscapeEditorCore	ls      = diagram.getLs();
			if (g_infoShown instanceof RelationInstance) {
				((RelationInstance) g_infoShown).repaint();
			}
		
			modifiers = ev.getModifiers();
			if ((modifiers & Event.SHIFT_MASK) == 0) {
				ls.infoShown(this);
			}

			String str = getEntityLabel();
			EntityInstance pe = getContainedBy();

			if (pe != null) {
				str = pe.getEntityLabel() + " . " + str /* + " " + getDiagramX() + "x" + getDiagramY() + " " + getX() + "X" + getY() */;
			} 
//			str += " " + getGroupFlag() + " " + getGroupKeyFlag();
			ls.showInfo(str);

			if (m_currentDescEntity != this && !isOpen() && getEntityClass() != null) {
				String label = getEntityLabel();
				String title = getTitle(); 
				String desc  = getDescription();

				if (desc == null) {
					desc = "The " + label + " " + getClassLabel() + ".";
				}

				String topline = " (" + getClassLabel() + (hasChildren() ? " - " + numChildren() + " items)" : ")" );

				if (title != null) {
					topline = title + topline;
				} else {
					topline = label + topline;
				}
				ls.setRightTextBox(topline, desc);
				m_currentDescEntity = this;
		}	}
		diagram.movedOverThing(ev, this, ev.getX() + getDiagramX(), ev.getY() + getDiagramY());
	}
}
