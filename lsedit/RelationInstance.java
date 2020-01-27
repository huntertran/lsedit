package lsedit;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Event;
import java.awt.Rectangle;

import java.awt.event.MouseEvent;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JComponent;

/* This provides the dumb concept of a relational class */

public final class RelationInstance extends LandscapeObject 
{
	// Constants

	protected final static int	  NEAR_PIXEL_SIZE		 = 6;

	public final static String	  LABEL_ID               = "label";
	public final static String	  RLABEL_ID              = "rlabel";
	public final static String    DESC_ID                = "description";

	// Bits set in m_marks

	public    final static int STYLE_BITS           = 0x00007;
	protected final static int LOOP_MARK	    	= 0x00010;	// Repaint this edge
	protected final static int NORMAL_MARK			= 0x00020;
	protected final static int ELISION_MARK         = 0x00040;
	protected final static int OUT_ELIDED_MARK		= 0x00100;
	protected final static int IN_ELIDED_MARK		= 0x00200;
	protected final static int IN_DIAGRAM_MARK		= 0x00400;
	protected final static int TRACED_MARK          = 0x000800; // This node participates in a backward/forward etc. trace

	public final static int DIFFERENT_REAL_SRC_MARK = 0x01000;
	public final static int DIFFERENT_REAL_DST_MARK = 0x02000;
	public final static int VALIDATED_MARK			= 0x04000;	// We have performed schema validation
	public final static int	LIFTED_MARK			    = 0x08000;

	public final static int GROUP_FLAG_MARK         = EntityInstance.GROUP_MARK	/* 0x10000 */;
	public final static int VALID_MARK              = 0x20000;	// We have performed the gross decision as to the type of this relation component
	public final static int HIGHLIGHT_FLAG_MARK     = 0x40000;
	public final static int FILLED_MARK             = 0x80000;		// No need to refill this edge
	public final static int	SPANNING_MARK			= 0x100000;
	public final static int	REVERSED_MARK			= 0x200000;		// The spanning edge is reversed
	
	public final static int IN_SET_MARK             = EntityInstance.IN_SET_MARK /* 0x1000000 */;


	public final static int PERMANENT_MARKS         = STYLE_BITS | SPANNING_MARK | REVERSED_MARK;
	public final static int IN_OUT_ELIDED           = IN_ELIDED_MARK  | OUT_ELIDED_MARK;
	public final static int VARIOUS_TYPE_MARKS      = LOOP_MARK | NORMAL_MARK | IN_OUT_ELIDED;
	public final static int PRESENTATION_MARKS      = GROUP_FLAG_MARK | HIGHLIGHT_FLAG_MARK | TRACED_MARK;

	public final static int CLASS_ATTR              = 0;
	public final static int COLOR_ATTR              = 1;
	public final static int STYLE_ATTR              = 2;

	// An edge is defined by a relation class and two entity instances

	protected EntityInstance	m_src;						// The real source      of this edge
	protected EntityInstance	m_dst;						// The real destination of this edge
	protected RelationComponent	m_relationComponent = null;	// The drawable component associated with this RelationInstance

	// Variables

	protected int			  m_mark = Util.LINE_STYLE_UNDEFINED;		// A collection of bits

	// --------------
	// Object methods
	// --------------

	public String getId()
	{
		RelationClass		parentClass = getRelationClass();
		String				src         = m_src.getId();
		String				dst         = m_dst.getId();
		
		if (isMarked(REVERSED_MARK)) {
			String temp = src;
			
			src = dst;
			dst = temp;
		}

		return "(" + parentClass.getId() + " " + src + " " + dst + ")";
	}
	
	public String toString() 
	{
		return drawSrc() + (drawSrc() == m_src ? "" : "{" + m_src + "}") + "->" + getRelationClass() + "->" + drawDst() + (drawDst() == m_dst ? "" : "{" + m_dst + "}");
	}

/*
	protected void finalize() throws Throwable
	{
		--m_totalRelations;
	}
*/
	// -------------------------
	// LandscapeObject3D methods
	// -------------------------

	public JComponent getSwingObject()
	{
		return m_relationComponent;
	}

	// ---------------
	// Wrapper methods
	// ---------------

	public EntityInstance drawSrc()
	{
		return m_src.getDrawEntity();
	}

	public EntityInstance drawDst()
	{
		return m_dst.getDrawEntity();
	}

	public RelationComponent neededComponent()
	{
		RelationComponent relationComponent = m_relationComponent;
		if (relationComponent == null) {
			m_relationComponent = relationComponent = new RelationComponent(this);
		}
		return(relationComponent);
	} 

	public void orEmbellished(int value)
	{
		RelationComponent relationComponent = m_relationComponent;

		if (relationComponent != null) {
			relationComponent.orEmbellished(value);
	}	}

	public void nandEmbellished(int value)
	{
		RelationComponent relationComponent = m_relationComponent;

		if (relationComponent != null) {
			relationComponent.nandEmbellished(value);
	}	}


	public void repaint()
	{
		RelationComponent relationComponent = m_relationComponent;

		if (relationComponent != null) {
			relationComponent.repaint();
	}	}
		
	public void validate()
	{
		RelationComponent relationComponent = m_relationComponent;

		if (relationComponent != null) {
			relationComponent.validate();
	}	}

	public int getFrequency()
	{
		RelationComponent component = m_relationComponent;

		if (component != null) {
			return component.getFrequency();
		}
		return -1;
	}

	// First order attributes

	public boolean processFirstOrder(String id, String value) 
	{
		if (id.equals(STYLE_ID)) {
			if (value != null) {
				setStyle(Attribute.parseIntValue(value));
			}
			return true;
		}

		return super.processFirstOrder(id, value);
	}

	public static void reportFirstOrderAttributes(ResultBox resultBox)
	{
		resultBox.addText(COLOR_ID);
		resultBox.addText(STYLE_ID);
	}

	// --------------
	// Public methods
	// --------------



	public RelationInstance(RelationClass parentClass, EntityInstance src, EntityInstance dst)
	{
		setParentClass(parentClass);
		this.m_src       = src;
		this.m_dst       = dst;
//		++m_totalRelations;
	}

	/* A reversed relation behaves exactly as if it pointed the other way */
	
	public void reverseRelation()
	{
		EntityInstance src = m_src;
		EntityInstance dst = m_dst;
		
		if (src != dst) {
			src.removeSrcRelation(this);
			dst.removeDstRelation(this);
			src.addDstRelation(this);
			dst.addSrcRelation(this);
			m_src = dst;
			m_dst = src;
	}	}
	
	public RelationComponent getRelationComponent()
	{
		return m_relationComponent;
	}

	public void setRelationComponent(RelationComponent relationComponent)
	{
		m_relationComponent = relationComponent;
	}

/*
 	public static int totalRelations()
	{
		return m_totalRelations;
	}
*/

	public boolean isRelationShown()
	{
		if (isMarked(SPANNING_MARK)) {
			return false;
		}
		return getRelationClass().isShown();
	}

	public int getStyle()
	{	
		int	ret = m_mark & STYLE_BITS;
		if (ret == Util.LINE_STYLE_UNDEFINED) {
			ret = -1;
		}
		return ret;
	}

	public void setStyle(int value)
	{
		m_mark &= ~STYLE_BITS;
		m_mark |= (value & STYLE_BITS);
	}

	public String getRelationLabel()
	{
		RelationClass	rc     = getRelationClass();
		Attribute		attribute;
		String			ret;
		
		if (rc.getShown() != LandscapeClassObject.DIRECTION_REVERSED) {
			attribute = getLsAttribute(LABEL_ID);
			if (attribute != null) {
				ret = attribute.parseString();
				if (ret != null) {
					return ret;
			}	}
		} else {
			attribute = getLsAttribute(RLABEL_ID);
			if (attribute != null) {
				ret = attribute.parseString();
				if (ret != null) {
					return ret;
			}	}
			attribute = getLsAttribute(LABEL_ID);
			if (attribute != null) {
				ret = attribute.parseString();
				if (ret != null) {
					return ret + " (r)";
		}	}	}
		return rc.getRelationLabel();
	}

	public String getRelationTooltip()
	{
		Option	option = Options.getDiagramOptions();
		String	id;

		if (option.isShowEdgeLabels()) {
			id = DESC_ID;
		} else {
			id = LABEL_ID;
		}
		Attribute attribute = getLsAttribute(id);
		
		if (attribute != null) {
			return attribute.parseString();
		}
		return null;
	}

	public String getClassLabel()
	{
		return getRelationClass().getLabel();
	}

	public String getStyleName(int style) 
	{
		return Util.getLineStyleName(style);
	}

	public void clearRelationMark()
	{
		andMark(PERMANENT_MARKS | PRESENTATION_MARKS);
		m_relationComponent = null;		// IJD: Make this smarter

//		System.out.println("RelationInstance.clearRelationMark " + this);
	}

	public void initFrequency()
	{
		RelationComponent component = neededComponent();

		component.initFrequency();
	}

	public void incrementFrequency(RelationInstance ri)
	{
		RelationComponent component = m_relationComponent;

		if (component != null) {
			component.incrementFrequency();
		}
		if (ri.m_src != m_src) {
			orMark(DIFFERENT_REAL_SRC_MARK);
		}
		if (ri.m_dst != m_dst) {
			orMark(DIFFERENT_REAL_DST_MARK);
	}	}

	public void orMark(int value)
	{
		m_mark |= value;
	}

	public void nandMark(int value)
	{
		m_mark &= ~value;
	}

	public void andMark(int value)
	{
		m_mark &= value;
	}

	public boolean isMarked(int value)
	{
		return((m_mark & value) != 0);
	}

	public boolean isSrcVisible()
	{
		return (m_src == drawSrc());
	}

	public boolean isDstVisible()
	{
		return (m_dst == drawDst());
	}

	/* Mark the edge as invalid so we paint it only once and then mark valid
	   -- not twice once from the source and once from the destination
	*/

	public void invalidateEdge()
	{
		nandMark(VALID_MARK);
	}

	public boolean getHighlightFlag() 
	{
		return isMarked(HIGHLIGHT_FLAG_MARK);
	}

	public boolean getGroupFlag() 
	{
		return isMarked(GROUP_FLAG_MARK);
	}

	// Compute the rules for how an edge is to be drawn between two entities, handling elision settings.
	// Essentially just flag the relation appropriately to assist the verify process know what it is dealing with

	public void drawRelation(Diagram diagram, boolean allowElision) 
	{
		if (isMarked(VALID_MARK)) {
			return;
		}

		int				marks = 0;

	/*	System.out.println("RelationInstance:draw(" + allowElision + ") " + this);
		java.lang.Thread.dumpStack();
		System.out.println("-----");
	*/
		if (!allowElision || isRelationShown()) {
			EntityInstance	src = drawSrc();
			EntityInstance	dst = drawDst();
			
			if (src.isMarked(EntityInstance.HIDDEN_MARK) || dst.isMarked(EntityInstance.HIDDEN_MARK)) {
				marks = 0;
			} else {
				marks   = NORMAL_MARK;
				if (allowElision) {

					EntityInstance	ancestor    = src.commonAncestor(dst);
					RelationClass	parentClass = getRelationClass();
					int				direction   = parentClass.getShown();
					int				nid         = parentClass.getNid();

					if (ancestor != null && ancestor.isMarked(EntityInstance.DIAGRAM_MARK) && ancestor.getElision(EntityInstance.INTERNAL_ELISION, nid)) {
						// Don't show internal relations contained under ancestor if this nearest common ancestor elides internal edges
						marks = 0;
					} else if (src.getElision(EntityInstance.SRC_ELISION, nid)) {
						// Show only stub of this relation from source entity (we are eliding src edges of this type)
						marks |= OUT_ELIDED_MARK;
					} else {
						if (src != m_src && src != dst && src.getElision(EntityInstance.EXITING_ELISION, nid)) {
							// The edge comes from within me and goes to outside of me
							marks |= OUT_ELIDED_MARK;
						} else {
							for (ancestor = src.getContainedBy(); ancestor != null && ancestor.isMarked(EntityInstance.IN_DIAGRAM) && !ancestor.hasDescendantOrSelf(dst); ancestor = ancestor.getContainedBy()) {
								if (ancestor.getElision(EntityInstance.EXITING_ELISION, nid)) {
									// The edge comes from within ancestor and goes outside ancestor (ie it exits from ancestor)
									marks |= OUT_ELIDED_MARK;
									break;
					}	}	}	}

					if (dst.getElision(EntityInstance.DST_ELISION, nid)) {
						// Show only stub of this relation to dst entity (we are eliding dst edges of this type)
						marks |= IN_ELIDED_MARK;
					} else {
						if (dst != m_dst && src != dst && dst.getElision(EntityInstance.ENTERING_ELISION, nid)) {
							// The edge comes from outside me and goes to inside me
							marks |= IN_ELIDED_MARK;
						} else {
							for (ancestor = dst.getContainedBy(); ancestor != null && ancestor.isMarked(EntityInstance.IN_DIAGRAM) && !ancestor.hasDescendantOrSelf(src); ancestor = ancestor.getContainedBy()) {
								if (ancestor.getElision(EntityInstance.ENTERING_ELISION, nid)) {
									// The edge comes from outside this ancestor and goes inside (ie it enters this ancestor)
									marks |= IN_ELIDED_MARK;
									break;
				}	}	}	}	}

				if (src == dst) {
					if ((marks & NORMAL_MARK) != 0) {
						// Recursive loop
						marks |= LOOP_MARK;
					}
				} 
		}	}	
			
		RelationComponent relationComponent;

		if (marks == 0) {
			// Don't know how to draw this thing
			if (isMarked(IN_DIAGRAM_MARK)) {
				relationComponent = m_relationComponent;
				if (relationComponent != null) {
					diagram.remove(relationComponent);
					m_relationComponent = null;
				}
				nandMark(IN_DIAGRAM_MARK);
			}
		} else {
			relationComponent = neededComponent();

			marks |= VALID_MARK;
			if (allowElision) {
				marks |= ELISION_MARK;
			}

			if (!isMarked(IN_DIAGRAM_MARK)) {
				marks |= IN_DIAGRAM_MARK;
				orMark(marks);
				diagram.add(relationComponent);	// Add this relation into the diagram
				relationComponent.setVisible(true);
			} else {
				orMark(marks);
			}
			relationComponent.validate();
	}	} 

	public void writeRelation(PrintWriter ps)
	{
		String src = m_src.getId();
		String dst = m_dst.getId();
		
		if (isMarked(REVERSED_MARK)) {
			String temp = src;
			
			src = dst;
			dst = temp;
		}
		ps.println(getParentClass().getId() + " " + src + " " + dst);
	}

	public void writeAttributes(PrintWriter ps) 
	{
		RelationClass	parentClass = getRelationClass();
		String			src         = m_src.getId();
		String			dst         = m_dst.getId();
		
		if (isMarked(REVERSED_MARK)) {
			String temp = src;
			
			src = dst;
			dst = temp;
		}

		String			nodeId      = "(" + parentClass.getId() + " " + src + " " + dst + ")";
		int				style       = getStyle();
		
		if (style >= 0) {
			if (parentClass == null || parentClass.getInheritedStyle() != style) {
				nodeId = writeAttribute(ps, nodeId, STYLE_ID, style);
		}	}

		nodeId = super.writeAttributes(ps, nodeId, parentClass, false);
		if (nodeId == null) {
			ps.println("}");
		}
	}

	public EntityInstance getSrc() 
	{
		return m_src;
	}

	public void setSrc(EntityInstance src) 
	{
		m_src = src;
	}

	public EntityInstance getDst() 
	{
		return m_dst;
	}

	public void setDst(EntityInstance dst) 
	{
		m_dst = dst;
	}

	public EntityInstance getDrawSrc() 
	{
		return drawSrc();
	}

	public EntityInstance getCurrentSrc()
	{
		if (drawSrc() != null) {
			return(drawSrc());
		}
		return(m_src);
	}

	public EntityInstance getDrawDst() 
	{
		return drawDst();
	}
	
	public EntityInstance getCurrentDst()
	{
		if (drawDst() != null) {
			return(drawDst());
		}
		return(m_dst);
	}

	public RelationClass getRelationClass() 
	{
		return (RelationClass) getParentClass();
	}

	public boolean matches(RelationInstance other)
	{
		return(drawSrc() == other.drawSrc() && drawDst() == other.drawDst());
	}

	// Only called within an update

	public void removeEdge()
	{
		getSrc().removeSrcRelation(this);
		getDst().removeDstRelation(this);
	}

	// The routines that follow hide the complexity of getting/setting attribute values
	// from EditAttributes

	public int getPrimaryAttributeCount()
	{
		return(3);
	}

	public String getLsAttributeNameAt(int index)
	{
		String	name;

		switch (index) {
		case CLASS_ATTR:
			name  = "class";
			break;
		case COLOR_ATTR:
			name  = COLOR_ID;
			break;
		case STYLE_ATTR:
			name  = STYLE_ID;
			break;	
		default:
			name  = super.getLsAttributeNameAt(index);
		}
		return(name);
	}

	public Object getLsAttributeValueAt(int index)
	{
		Object	value;

		switch (index) {
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
		case COLOR_ATTR:
			value = getObjectColor();
			break;
		case STYLE_ATTR:
			value = new Integer(getStyle());
			break;
		default:
			value = super.getLsAttributeValueAt(index);
		}
		return(value);
	}

	// Need to know the type in cases where value might be null
	// For example with some colors

	public int getLsAttributeTypeAt(int index)
	{
		int		ret;
		
		switch (index) {
		case CLASS_ATTR:
			ret = Attribute.RELATION_CLASS_TYPE;
			break;
		case COLOR_ATTR:
			ret = Attribute.COLOR_OR_NULL_TYPE;
			break;
		case STYLE_ATTR:
			ret = Attribute.REL_STYLE_TYPE;
			break;
		default:
			ret = super.getLsAttributeTypeAt(index);
		}
		return(ret);
	}

	public LandscapeEditorCore getLs()
	{
		return getDiagram().getLs();
	}

	public void mouseEntered()
	{
		getLs().setCursor(Cursor.HAND_CURSOR);
	}

	public void mouseExited()
	{
		getLs().setCursor(Cursor.DEFAULT_CURSOR); 
	}

	public void mousePressed(MouseEvent ev, int diagramX, int diagramY)
	{
		getDiagram().relationPressed(ev, this, diagramX, diagramY);
	}

	public void mouseReleased(MouseEvent ev, int diagramX, int diagramY)
	{
		getDiagram().relationReleased(ev, this, diagramX, diagramY);
	}

	public void mouseDragged(MouseEvent ev, int diagramX, int diagramY)
	{
		getDiagram().relationDragged(ev, this, diagramX, diagramY);
	}

	public void mouseMoved(MouseEvent ev, int diagramX, int diagramY)
	{
		Diagram diagram = getDiagram();

		if (this != g_infoShown) {
			LandscapeEditorCore ls   = diagram.getLs();
			int					freq = getFrequency();
			String				message;
			int					modifiers;



			modifiers = ev.getModifiers();
			if ((modifiers & Event.SHIFT_MASK) == 0) {
				ls.infoShown(this);
			}
			repaint();
			if (m_src == drawSrc()) {
				message = "";
			} else {
				message  = Util.quoted(m_src.getEntityLabel());
				if (isMarked(DIFFERENT_REAL_SRC_MARK)) {
					message += " et. al.";
				}
				message += "->";
			}
			message += Util.quoted(drawSrc().getEntityLabel()) +  " " + getClassLabel() + " " + Util.quoted(drawDst().getEntityLabel());
			if (m_dst != drawDst()) {
				message += "->" + Util.quoted(m_dst.getEntityLabel());
				if (isMarked(DIFFERENT_REAL_DST_MARK)) {
					message += " et. al.";
				}
			}

			if (freq != 1) {
				message += " (" + freq + " edges)";
			}
//			message += " " + (getX() + ev.getX()) + "x" + (getY() + ev.getY());
			
			getLs().showInfo(message);
		}
		diagram.movedOverThing(ev, this, diagramX, diagramY);
	}
}
