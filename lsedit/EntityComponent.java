package lsedit;

import java.lang.Math;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FontMetrics;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

// Entities in a landscape 

public class EntityComponent extends JComponent implements PaintShapeHelper, Icon {

	// Final values
	
	public final static int MARGIN             = 5; 
	
	/* Used in computing rules for shape of a folder */

	public  final static int MIN_FLAP_HT       = 3;
	public  final static int MAX_FLAP_HT       = 8;
	public  final static int FLAP_MARGIN       = 4;
	public  final static int TINY_FLAP_WD      = 10;
	public  final static int MIN_FLAP_WD       = 75;
	public  final static int NAV_WIDTH         = 24;

	/* Position and size of the contents flag when entity closed */

	public final static int CONTENTS_FLAG_X   = 3;
	public final static int CONTENTS_FLAG_Y   = 9;
	public final static int CONTENTS_FLAG_DIM = 8;
	public final static int CONTENTS_FLAG_X_RESERVE = CONTENTS_FLAG_X + CONTENTS_FLAG_DIM;
	public final static int CONTENTS_FLAG_Y_RESERVE = CONTENTS_FLAG_Y + CONTENTS_FLAG_DIM;

	// These are the top left coordinates of the object on the screen

	private EntityInstance m_entityInstance; 

	// Sorting uses this value and it is really expensive to recompute for each key compare

	private double		   m_avgX;

	// Array for expressing points of a polygon
	// One one instance of each of these arrays created since static

	private	static int[]   m_xp = null;
	private static int[]   m_yp = null;

	// We need the position within the diagram to compute coordinates for edges quickly

	private int		m_diagramX         = 0;
	private int		m_diagramY         = 0;

	private Vector m_srcLiftedList = null;	// List of relations whose source is lifted to leave this entity
	private Vector m_dstLiftedList = null;	// List of relations whose destination is lifted to address this entity

 	private   Cardinal[]    m_dstCardinals;
	private   SrcCardinal[] m_srcCardinals;

	private	  Icon			m_scaledIcon = null;

	// --------------
	// Object methods
	// --------------

	public String toString() 
	{
		return "EntityComponent: " + m_entityInstance.toString();
	}

	// -----------------
	// Component methods
	// -----------------
	
	public void setBounds(int x, int y, int width, int height)
	{
		int oldX      = getX();
		int	oldY      = getY();
		int	oldWidth  = getWidth();
		int	oldHeight = getHeight();
/*
		if (m_entityInstance.toString().equals("clibrary")) {
			System.out.println(this + " setBounds from (" + getX() + "," + getY() + ":" + getWidth() + "x" + getHeight() + ") to (" + x + "," + y + ":" + width + "x" + height + ") old diagram = " + m_diagramX + "x" + m_diagramY);
		}
*/
		super.setBounds(x, y, width, height);

		if (x != oldX || y != oldY) {
			shiftDiagramLocations(x - oldX, y - oldY);
		} else if (width == oldWidth && height == oldHeight) {
			return;
		}
		refillEdges();
		moveCardinals(m_entityInstance.getDiagram());
	}

	// ------------------
	// JComponent methods
	// ------------------

	public Component add(Component child)
	{

		EntityComponent ec    = (EntityComponent) child;
		EntityInstance	e     = ec.getEntityInstance();
		
		if (!e.isMarked(EntityInstance.HIDDEN_MARK)) {
			double			area  = e.widthRelLocal() * e.heightRelLocal();
			int				size  = getComponentCount();
			EntityComponent	ec1;
			EntityInstance	e1;
			double			area1;
			int		        i;


//			System.out.println("Adding " + child + " under " + this + " hidden=" + e.isMarked(EntityInstance.HIDDEN_MARK));
//			java.lang.Thread.dumpStack();

			for (i = 0; i < size; ++i) {
				ec1   = (EntityComponent) getComponent(i);
				e1    = ec1.getEntityInstance();
				area1 = e1.widthRelLocal() * e1.heightRelLocal();
				if (area1 > area) {
					// Put smaller areas before larger ones so they get painted later
					// We might shade larger things
					return add(child, i);
			}	}
			return super.add(child);
		}
		return null; 
	}

	public void paintComponent(Graphics g) 
	{
		Option			option         = Options.getDiagramOptions();
		EntityInstance	entityInstance = m_entityInstance;
		EntityClass		entityClass    = entityInstance.getEntityClass();
		boolean			isDrawRoot     = entityInstance.isDrawRoot();
		boolean			isOpen         = entityInstance.isOpen();
		int		x                      = 0;
		int		y                      = 0;
		int		width                  = getWidth();
		int		height                 = getHeight();
		int		margin                 = MARGIN;
		int		offset				   = 0;
		String	label1                 = entityInstance.getEntityLabel();
		int		labelWidth, labelHeight, labelY;
		boolean center;
		Color	labelColor             = entityInstance.getCurrentLabelColor();
		Color	backgroundColor;
		Color	foregroundColor;
		Font	font;
		FontMetrics fm;
		int		icon_rule, image;


//		System.out.println("EntityInstance.paintComponent: " + entityInstance + " redBox=" + entityInstance.isMarked(EntityInstance.REDBOX_MARK));		// IJD
/*		{
			int	cnt, i;

			cnt = getComponentCount();
			for (i = 0; i < cnt; ++i) {
				System.out.println("EntityInstance.paintComponent: child " + getComponent(i));
		}	}
*/

/*		java.lang.Thread.dumpStack();
		System.out.println("-----");
*/
/*
		if (entityInstance.red_closed()) {
			backgroundColor = Color.red.darker();
//			System.out.print(backgroundColor);
		} else {
			backgroundColor = entityInstance.getCurrentObjectColor();
		}
 */
		backgroundColor = entityInstance.getCurrentObjectColor();
		foregroundColor = Color.black;

		if (!isDrawRoot) {
			if (entityInstance.isMarked(EntityInstance.SHADES_MARK)) {
				int	shadow_size = option.getShadowSize();
				int	max         = ((width > height) ? height : width) >> 1;
				if (shadow_size > max) {
					shadow_size = max;
				}
				if (shadow_size > 0) {
					Color shadow;

					if (backgroundColor == null) {
						shadow = Color.GRAY;
					} else {
						shadow = backgroundColor;
					}
					shadow  = shadow.darker();
					width  -= shadow_size;
					height -= shadow_size;
					paintShape(g, this, entityClass, shadow_size, shadow_size, width, height, false, isOpen, foregroundColor, shadow);
		}	}	}

		if (entityInstance.red_open()) {
			foregroundColor = Color.red.darker();
		}
		paintShape(g, this, entityClass, 0, 0, width, height, isDrawRoot, isOpen, foregroundColor, backgroundColor); 

		Diagram				diagram = entityInstance.getDiagram();
		LandscapeEditorCore ls      = diagram.getLs();

		if (option.isShowGrid()) {

			if (isDrawRoot) {
				int	grid = option.getGridSize();

				if (grid > 1) {
					int i;
					g.setColor(option.getGridColor());
					for (i = grid; i < height; i += grid) {
						g.drawLine(0,i,width-1,i);
					}
					for (i = grid; i < width; i += grid) {
						g.drawLine(i,0,i, height-1);
		}	}	}	}

		if (entityInstance.closedWithChildren()) {

			/*  Draw a small mark as shown in top left of object

				x---
				|
				|   --------
				|  |        |
				   |    |   |
				   |    |   |
	  			   |  ----  |
				   |    |   |
				   |    |   |
				   |        |
				   |________|
			*/

			if (width > CONTENTS_FLAG_X_RESERVE && height > CONTENTS_FLAG_Y_RESERVE) {
				int x1, y1;
				Color	color1;

				color1 = entityInstance.getInheritedObjectColor();
				if (color1 != null) {
					color1 = ColorCache.getInverse(color1.getRGB());
				} else {
					color1 = Color.BLACK;
				}
				g.setColor(color1);

				g.drawRect(CONTENTS_FLAG_X, CONTENTS_FLAG_Y, CONTENTS_FLAG_DIM /* 8 */, CONTENTS_FLAG_DIM);

				g.drawLine(CONTENTS_FLAG_X+1,                     CONTENTS_FLAG_Y+(CONTENTS_FLAG_DIM/2), CONTENTS_FLAG_X_RESERVE-1,             CONTENTS_FLAG_Y+(CONTENTS_FLAG_DIM/2));
				g.drawLine(CONTENTS_FLAG_X+(CONTENTS_FLAG_DIM/2), CONTENTS_FLAG_Y+1,                     CONTENTS_FLAG_X+(CONTENTS_FLAG_DIM/2), CONTENTS_FLAG_Y_RESERVE-1);
				margin += CONTENTS_FLAG_X_RESERVE;
		}	}

		// Draw our own label

		g.setColor(labelColor);

		labelWidth  = width  - MARGIN - margin;
		labelHeight = height - MARGIN*2;

		switch (entityInstance.getInheritedStyle()) {
		case EntityClass.ENTITY_STYLE_LABELLED_GROUP:
			Util.drawGroupBoxLabel(g, 0, 0, width, label1);
			break;
		default:
			font        = entityInstance.getEntityLabelFont();
			g.setFont(font);
			
			if (isOpen) {
				switch (entityInstance.getInheritedStyle()) {
				case EntityClass.ENTITY_STYLE_FOLDER:
					// Compute flap size
					int fw = (int) (((double) width) * .4);
					int fh = Math.max(MIN_FLAP_HT, Math.min(MAX_FLAP_HT, ((int) (height * .2))));

					if (fw < MIN_FLAP_WD) {
					   fw += fw/2;
					}
					if (fw < TINY_FLAP_WD) {
						fw = Math.min(fw + FLAP_MARGIN, width - width/3);
					}
					margin     = FLAP_MARGIN+fh/2+2;
//					Util.drawStringClipped(g, label1, FLAP_MARGIN+fh/2+2, 0, (double) fw, height-MARGIN*2, false, false);
					break;
				case EntityClass.ENTITY_STYLE_CLASS:
//					Util.drawStringClipped(g, label1, MARGIN*3, MARGIN, width-MARGIN*2, height-MARGIN*2, false, false);
					break;
				default:
//					g.setColor(Color.black); 
//					Util.drawStringClipped(g, label1, MARGIN, MARGIN, width-MARGIN*2, height-MARGIN*2, false, false);
				}
				Util.drawStringWrapped(g, label1, margin, MARGIN, labelWidth, labelHeight, false /* Not centered */, false, option.isLabelInvertBackground());
			} else {
				if (m_scaledIcon == null) {
					// Don't have an icon to show
					icon_rule = Option.ICON_RULE_NONE;
				} else {
					icon_rule = option.getIconRule();
				}
								
				if (icon_rule != Option.ICON_RULE_PLAIN) {
					if (entityInstance.isMarked(EntityInstance.CLIENT_SUPPLIER)) {
						EntityInstance pe = entityInstance.getContainedBy();
						if (pe != null)	{
							label1 = pe.getEntityLabel() + " .\n" + label1;
					}	}

					switch (icon_rule) {
					case Option.ICON_RULE_BOTTOM:
					case Option.ICON_RULE_TOP:
						fm = g.getFontMetrics();
						labelHeight = fm.getHeight();
						if (icon_rule == Option.ICON_RULE_TOP) {
							labelY  = 0;
						} else {
							labelY  = height - labelHeight;
						} 
						Util.drawStringClipped(g, label1, 0, labelY, width, labelHeight, true /* centered */, false /* Not underlined */, option.isLabelInvertBackground());
						break;
					default:
						Util.drawStringWrapped(g, label1, margin, MARGIN, labelWidth, labelHeight, true /* centered */, false /* Not underlined */, option.isLabelInvertBackground());
		}	}	}	}
		if (!entityInstance.isMarked(EntityInstance.LEGEND_MARK)) {
			image = entityClass.getImage();
			if (!isOpen) {
				switch (option.getShowClassType()) {
				case 2:
					if ((image & EntityClass.ENTITY_IMAGE_CLASS) == 0) {
						break;
					}
				case 1:
					label1 = entityClass.getLabel();
					Util.drawStringClipped(g, label1, margin, MARGIN, labelWidth, labelHeight, false /* Not centered */, false, option.isLabelInvertBackground());
				}
				switch (option.getShowSpanType()) {
				case 2:
					if ((image & EntityClass.ENTITY_IMAGE_SPAN) == 0) {
						break;
					}
				case 1:
					RelationInstance r = entityInstance.getContainedByRelation();
					if (r != null) {
						label1 = r.getRelationLabel();
						Util.drawStringBottomClipped(g, label1, margin, MARGIN, labelWidth, labelHeight, false /* Not centered */, false, option.isLabelInvertBackground());
			}	}	}
			if ((NAV_WIDTH << 1) <= width && (NAV_WIDTH << 1) <= height) {
				switch (option.getShowNavNext()) {
				case 2:
					if ((image & EntityClass.ENTITY_IMAGE_NAV_NEXT) == 0) {
						break;
					}
				case 1:
					label1 = entityInstance.getNavNext();
					if (label1 != null) {
						g.setColor(labelColor);
						NavNext_Button.paintIcon(g, 0, 0, width, height, NAV_WIDTH, NAV_WIDTH);
				}	}
				switch (option.getShowNavPrev()) {
				case 2:
					if ((image & EntityClass.ENTITY_IMAGE_NAV_PREV) == 0) {
						break;
					}
				case 1:
					label1 = entityInstance.getNavPrev();
					if (label1 != null) {
						g.setColor(labelColor);
						NavPrev_Button.paintIcon(g, 0, 0, width, height, NAV_WIDTH, NAV_WIDTH);
		}	}	}	}
	
		if (m_entityInstance.isMarked(EntityInstance.REDBOX_MARK)) {
			Util.drawOutlineRedBox(g, 0, 0, width, height);
		}	

		// Put flags around the edges of the box

		if (entityInstance.getGroupFlag()) {
			g.setColor(entityInstance.getInheritedLabelColor());

			int pdim = (height < 20) ? 4 : 6;

			// Draw resize points

			if (entityInstance.getGroupKeyFlag()) {
				g.fillRect(1,			   1,				pdim, pdim);
				g.fillRect(width/2-pdim/2, 1,				pdim, pdim);
				g.fillRect(width-pdim,	   1,				pdim, pdim);
				g.fillRect(1,			   height/2-pdim/2, pdim, pdim);
				g.fillRect(width-pdim,	   height/2-pdim/2, pdim, pdim);
				g.fillRect(1,			   height-pdim,		pdim, pdim);
				g.fillRect(width/2-pdim/2, height-pdim,		pdim, pdim);
				g.fillRect(width-pdim,	   height-pdim,		pdim, pdim);
			}  else {
				// Stupidity here.. An outline is one extra byte wide and high because the two
				// edges of a one pixel box occupy 2 pixels.

				g.drawRect(1,			   1,				pdim, pdim);
				g.drawRect(width/2-pdim/2, 1,				pdim, pdim);
				g.drawRect(width-pdim-1,   1,				pdim, pdim);
				g.drawRect(1,			   height/2-pdim/2, pdim, pdim);
				g.drawRect(width-pdim-1,   height/2-pdim/2, pdim, pdim);
				g.drawRect(1,			   height-pdim-1,	pdim, pdim);
				g.drawRect(width/2-pdim/2, height-pdim-1,	pdim, pdim);
				g.drawRect(width-pdim-1,   height-pdim-1,	pdim, pdim);
			}
		}

		/* For debugging 
		g.setColor(Color.black);
		g.drawLine(0,0,width,height);
		g.drawLine(0,height, width, 0);
		*/
	}

	// --------------
	// Public methods
	// --------------

	private static int[] getXp()
	{
		if (m_xp == null) {
			m_xp = new int[21];
		}
		return m_xp;
	}

	private static int[] getYp()
	{
		if (m_yp == null) {
			m_yp = new int[21];
		}
		return m_yp;
	}

 	public EntityComponent(EntityInstance entityInstance) 
	{
		setLayout(null);
		m_entityInstance = entityInstance;
		entityInstance.setEntityComponent(this);
		if (m_xp == null) {
			m_xp = new int[21];
			m_yp = new int[21];
		}
	}

	public EntityInstance getEntityInstance()
	{
		return(m_entityInstance);
	}

	public Vector getSrcLiftedList()
	{
		return m_srcLiftedList;
	}

	public Vector getDstLiftedList()
	{
		return m_dstLiftedList;
	}

	public Vector getNeededSrcLiftedList()
	{
		Vector	srcLiftedList = m_srcLiftedList;

		if (srcLiftedList == null) {
			m_srcLiftedList = srcLiftedList = new Vector();
		}
		return srcLiftedList;
	}

	public Vector getNeededDstLiftedList()
	{
		Vector	dstLiftedList = m_dstLiftedList;

		if (dstLiftedList == null) {
			m_dstLiftedList = dstLiftedList = new Vector();
		}
		return dstLiftedList;
	}

	public void clearLiftedEdges()
	{
		m_srcLiftedList = null;		// Have no lifted relation sources
		m_dstLiftedList = null;		// Have no lifted relation destinations
	}

	protected void shiftDiagramLocations(int shiftX, int shiftY)
	{
		Component	component;
		int			i;

		m_diagramX += shiftX;
		m_diagramY += shiftY;

/*
		if (m_entityInstance.toString().equals("clibrary")) {
			System.out.println(this + " shiftDiagramLocation(" + shiftX + "," + shiftY + ") diagram = " + m_diagramX + "," + m_diagramY);
		}
 */
		for (i = getComponentCount(); --i >= 0; ) {
			component = getComponent(i);
			if (component instanceof EntityComponent) {
				((EntityComponent) component).shiftDiagramLocations(shiftX, shiftY);
	}	}	}

	// Initialise the root of the entity component tree so that it is
	// sync'd with the same diagram location as its parent.  Then when
	// we relocate it everything works correctly.  N.B. The initial
	// location is set to (0,0) apriori when setting the entity size

	public void setDiagramLocation(int diagramX, int diagramY)
	{
		Component	component;
		int			i;

		m_diagramX = diagramX;;
		m_diagramY = diagramY;

/*
		if (m_entityInstance.toString().equals("clibrary")) {
			System.out.println(this + " setDiagramLocation " + this + " diagram = " + m_diagramX + "x" + m_diagramY);
		}
 */
		for (i = getComponentCount(); --i >= 0; ) {
			component = getComponent(i);
			if (component instanceof EntityComponent) {
				((EntityComponent) component).setDiagramLocation(diagramX+component.getX(), diagramY+component.getY());
	}	}	}

	public int getDiagramX()
	{
		return m_diagramX;
	}

	public int getDiagramY()
	{
		return m_diagramY;
	}

	public Rectangle getDiagramBounds() 
	{
		return new Rectangle(m_diagramX, m_diagramY, getWidth(), getHeight());
	}

	public boolean shades(EntityComponent base)
	{
		int	x     = getX();
		int	basex = base.getX();

		if (x >= basex) {
			int	y     = getY();
			int basey = base.getY();
			if (y >= basey) {
				x     += getWidth();
				basex += base.getWidth();
				if (x <= basex) {
					y     += getHeight();
					basey += base.getHeight();
					if (y <= basey) {
						return true;
		}	}	}	}
		return false; 
	}

	// Compute shading for each of our children if that shading not known

	public void computeShading()
	{
		EntityInstance	e    = m_entityInstance;
		EntityComponent ec1;
		int				size = getComponentCount();
		int				i;

		if (!e.isMarked(EntityInstance.SHADING_KNOWN_MARK)) {
			int				j;
			EntityComponent	ec2;
			EntityInstance	child;

			// From smallest to largest
			for (i = 0; i < size; ++i) {
				ec1   = (EntityComponent) getComponent(i);
				child = ec1.getEntityInstance();
				// From largest to smallest
				for (j = size; ;) {
					if (--j == i) {
						// The entityInstance e doesn't shade any larger areas
						child.nandMark(EntityInstance.SHADES_MARK);
						break;
					}
					ec2 = (EntityComponent) getComponent(j);
					if (ec1.shades(ec2)) {
						// The entity instance e shades ec2
//						System.out.println(ec1 + " shades " + ec2);
						child.orMark(EntityInstance.SHADES_MARK);
						break;
			}	}	}
			e.orMark(EntityInstance.SHADING_KNOWN_MARK);
		}

		for (i = 0; i < size; ++i) {
			ec1   = (EntityComponent) getComponent(i);
			ec1.computeShading();
	}	}

	// -----------------
	// Cardinals support
	// -----------------

	public void resizeDstCardinals(int numRelations)
	{
		Cardinal[]		cardinals = m_dstCardinals;
		int				i;
		EntityComponent	child;

		if (cardinals == null) {
			if (numRelations > 0) {
				m_dstCardinals = new Cardinal[numRelations];
			}
		} else if (numRelations == 0) {
			m_dstCardinals = null;
		} else if (cardinals.length != numRelations) {
			Cardinal[]	newCardinals;

			i = cardinals.length;
			if (i > numRelations) {
				i = numRelations;
			}
			m_dstCardinals = newCardinals = new Cardinal[numRelations];
			while (--i >= 0) {
				newCardinals[i] = cardinals[i];
		}	}

		for (i = getComponentCount(); --i >= 0; ) {
			child = (EntityComponent) getComponent(i);
			child.resizeDstCardinals(numRelations);
	}	}

	public void resizeSrcCardinals(int numRelations)
	{
		SrcCardinal[]	cardinals = m_srcCardinals;
		int				i;
		EntityComponent	child;

		if (cardinals == null) {
			if (numRelations > 0) {
				m_srcCardinals = new SrcCardinal[numRelations];
			}
		} else if (numRelations == 0) {
			m_srcCardinals = null;
		} else if (cardinals.length != numRelations) {
			SrcCardinal[]	newCardinals;

			i = cardinals.length;
			if (i > numRelations) {
				i = numRelations;
			}
			m_srcCardinals = newCardinals = new SrcCardinal[numRelations];
			while (--i >= 0) {
				newCardinals[i] = cardinals[i];
		}	}

		for (i = getComponentCount(); --i >= 0; ) {
			child = (EntityComponent) getComponent(i);
			child.resizeSrcCardinals(numRelations);
	}	}

	// Reset all cardinals associated with this entity and things it contains

	public void resetDstCardinals() 
	{
		Cardinal[]		cardinals = m_dstCardinals;
		int				i;
		EntityComponent	child;

		if (cardinals != null) {
			Cardinal cardinal;
			
			for (i = cardinals.length; --i >= 0; ) {
				cardinal = cardinals[i];
				if (cardinal != null) {
					cardinal.reset();
		}	}	}

		for (i = getComponentCount(); --i >= 0; ) {
			child = (EntityComponent) getComponent(i);
			child.resetDstCardinals();
	}	}

	public void resetSrcCardinals() 
	{
		Cardinal[]		cardinals = m_srcCardinals;
		int				i;
		EntityComponent	child;

		if (cardinals != null) {
			Cardinal cardinal;
			
			for (i = cardinals.length; --i >= 0; ) {
				cardinal = cardinals[i];
				if (cardinal != null) {
					cardinal.reset();
		}	}	}

		for (i = getComponentCount(); --i >= 0; ) {
			child = (EntityComponent) getComponent(i);
			child.resetSrcCardinals();
	}	}

	// Calculate the number of edges into me

	public void calcDstEdgeCardinals()
	{
		Vector			dstLiftedList = getDstLiftedList();
		int				i;
		EntityComponent	child;

//		System.out.println("EntityComponent.calcEdgeCardinals for " + this);

		if (dstLiftedList != null) {
			Cardinal[]			cardinals = m_dstCardinals;
			Cardinal			cardinal;
			RelationInstance	ri;
			RelationClass		rc;
			int					ind;
		
			// For every edge that comes to us (something in or beneath entities in the diagram)
			for (i = dstLiftedList.size(); --i >= 0; ) {
				ri       = (RelationInstance) dstLiftedList.elementAt(i);
				rc       = ri.getRelationClass();
				ind      = rc.getNid();
				cardinal = cardinals[ind];
				if (cardinal == null) {
					cardinals[ind] = cardinal = new Cardinal();
				}
				cardinal.sum(ri);
		}	}	

		for (i = getComponentCount(); --i >= 0; ) {
			child = (EntityComponent) getComponent(i);
			child.calcDstEdgeCardinals();
	}	}

	// Calculate the number of edges out of me

	public void calcSrcEdgeCardinals()
	{
		Vector			srcLiftedList = getSrcLiftedList();
		int				i;
		EntityComponent	child;
		
//		System.out.println("EntityComponent.calcEdgeCardinals for " + this);

		// For every edge that comes to us (something in or beneath entities in the diagram)
		if (srcLiftedList != null) {
			Cardinal[]			cardinals = m_srcCardinals;
			Cardinal			cardinal;
			RelationInstance	ri;
			RelationClass		rc;
			int					ind;

			for (i = srcLiftedList.size(); --i >= 0; ) {
				ri       = (RelationInstance) srcLiftedList.elementAt(i);
				rc       = ri.getRelationClass();
				ind      = rc.getNid();
				cardinal = cardinals[ind];
				if (cardinal == null) {
					cardinals[ind] = cardinal = new SrcCardinal();
				}
				cardinal.sum(ri);
		}	}	

		for (i = getComponentCount(); --i >= 0; ) {
			child = (EntityComponent) getComponent(i);
			child.calcSrcEdgeCardinals();
	}	}


	private void moveCardinals(Diagram diagram)
	{
		EntityInstance	e;
		int				width, height;

		// Move cardinals in sync with entity

		Cardinal[]		cardinals;

		width  = getWidth();
		height = getHeight();

		if (width > 0 && height > 0) {
			int				i, diagramX, diagramY;
			EntityComponent	child;

			diagramX = getDiagramX();
			diagramY = getDiagramY();

			cardinals = m_dstCardinals;
			if (cardinals != null) {	
				Cardinal		cardinal;
				RelationClass	rc;

				for (i = cardinals.length; i > 0; ) {
					cardinal = cardinals[--i];
					if (cardinal != null) {
						rc       = diagram.numToRelationClass(i);
						cardinal.setCenterTop(diagramX, diagramY, width, height, rc);
//						cardinal.repaint();
			}	}	}

			cardinals = m_srcCardinals;
			if (cardinals != null) {	
				Cardinal		cardinal;
				RelationClass	rc;

				for (i = cardinals.length; i > 0; ) {
					cardinal = cardinals[--i];
					if (cardinal != null) {
						rc       = diagram.numToRelationClass(i);
						cardinal.setCenterTop(diagramX, diagramY, width, height, rc);
//						cardinal.repaint();
			}	}	}
			
			// Any cardinals inside us will also need to be repositioned since they depend on the diagramX, diagramY

			for (i = getComponentCount(); --i >= 0; ) {
				child = (EntityComponent) getComponent(i);
				child.moveCardinals(diagram);
	}	}	}

	public void showDstCardinals(Diagram diagram)
	{
		int	width  = getWidth();
		int	height = getHeight();

//		System.out.println("EntityComponent.showCardinals " + this);

		if (width > 0 && height > 0) {
			
			Cardinal[]		cardinals = m_dstCardinals;
			int				i;
			EntityComponent	child;

			if (cardinals != null) {
				int			diagramX = getDiagramX();
				int			diagramY = getDiagramY();
				Cardinal	cardinal;
				int			cnt;
				RelationClass rc;

				for (i = cardinals.length; i > 0; ) {
					cardinal = cardinals[--i];
					if (cardinal != null) {
						cnt      = cardinal.getCnt();
						if (cnt > 0) {
							rc = diagram.numToRelationClass(i);

							if (rc.isShown()) {
								cardinal.setBackground(rc.getInheritedObjectColor());
								cardinal.setCenterTop(diagramX, diagramY, width, height, rc);
								diagram.add(cardinal);		// Add this cardinal to the diagram
			}	}	}	}	}

			for (i = getComponentCount(); --i >= 0; ) {
				child = (EntityComponent) getComponent(i);
				child.showDstCardinals(diagram);
	}	}	}

	public void showSrcCardinals(Diagram diagram)
	{
		int				width  = getWidth();
		int				height = getHeight();

//		System.out.println("EntityComponent.showSrcCardinals " + this);

		if (width > 0 && height > 0) {
			Cardinal[]		cardinals = m_srcCardinals;
			int				i;
			EntityComponent	child;

			if (cardinals != null) {
				int			diagramX = getDiagramX();
				int			diagramY = getDiagramY();
				Cardinal	cardinal;
				int			cnt;
				RelationClass rc;

				for (i = cardinals.length; i > 0; ) {
					cardinal = cardinals[--i];
					if (cardinal != null) {
						cnt      = cardinal.getCnt();
						if (cnt > 0) {
							rc = diagram.numToRelationClass(i);
							if (rc.isShown()) {
								cardinal.setBackground(rc.getInheritedObjectColor());
								cardinal.setCenterTop(diagramX, diagramY, width, height, rc);
								diagram.add(cardinal);		// Add this cardinal to the diagram
			}	}	}	}	}	

			for (i = getComponentCount(); --i >= 0; ) {
				child = (EntityComponent) getComponent(i);
				child.showSrcCardinals(diagram);
	}	}	}

	// Used for sorting clients/suppliers

	public void setAvgX(double avgX)
	{
		m_avgX = avgX;
	}

	public double getAvgX()
	{
		return m_avgX;
	}

	// Invalidate our edges because of some change in our shape or position etc.

	public void refillEdges()
	{
		EntityComponent	component;
		int	i;

		{
			Vector				liftedList = m_srcLiftedList;
			RelationInstance	ri;
			int					dir;

			for (dir = 0; dir < 2; ++dir) {
				if (liftedList != null) {
					for (i = liftedList.size(); --i >= 0; ) {
						ri = (RelationInstance) liftedList.elementAt(i);
						ri.nandMark(RelationInstance.FILLED_MARK);
				}	}
				liftedList = m_dstLiftedList;
		}	}

		// Any edges to things inside us will also need to be refilled

		for (i = getComponentCount(); --i >= 0; ) {
			component = (EntityComponent) getComponent(i);
			component.refillEdges();
	}	}

	// Keep the Z ordering satisfying the fact that smaller areas get painted after later ones
	// Small areas occur before later ones in the component array

	private void entitySizeChanged()
	{
		Container parent = getParent();

		if (!(parent instanceof EntityComponent)) {
			return;
		}

		int				size  = parent.getComponentCount();
		int				i, j, k;
		EntityInstance	e, e1;
		EntityComponent	ec;
		double			area, area1;

		for (i = 0;; ++i) {
			if (i == size) {
				return;
			}
			if (this == parent.getComponent(i)) {
				break;
		}	}

		// This component used to occur at i

		e     = getEntityInstance();
		
		if (e.isMarked(EntityInstance.HOVER_SCALE_MARK)) {
			// Put in front of everything else
			j = 0;
		} else {
			area  = e.widthRelLocal() * e.heightRelLocal();

			for (j = i-1; j >= 0; --j) {
				ec    = (EntityComponent) parent.getComponent(j);
				e1    = ec.getEntityInstance();
				area1 = e1.widthRelLocal() * e1.heightRelLocal();
				if (area1 <= area) {
					break;
			}	}
			if (++j == i) {
				// Everything before old position remains <= new area size (perhaps our size has grown)
				for (j = i+1; j < size; ++j) {
					ec    = (EntityComponent) parent.getComponent(j);
					e1    = ec.getEntityInstance();
					area1 = e1.widthRelLocal() * e1.heightRelLocal();
					if (area1 >= area) {
						break;
				}	}
				--j;
		}	}
		// New position for component currently at position i is at position j

		if (j != i) {
			parent.remove(i);
			parent.add(this, j);	// Add after the entity with smaller area and before entity with larger area
	}	}

	// This must be called whenever our relative bounds change when we are
	// visible to recompute our coordinates within the diagram

	public void setEntityBounds(EntityComponent parentComponent)
	{
		EntityInstance	e               = m_entityInstance;
		EntityInstance	parent          = parentComponent.getEntityInstance();
		double			parentWidth     = (double) parentComponent.getWidth();
		double			parentHeight    = (double) parentComponent.getHeight();
		int				oldX            = getX();
		int				oldY            = getY();
		int				oldWidth        = getWidth();
		int				oldHeight       = getHeight();
		int				newX, newY, newWidth, newHeight;
		boolean			sameSize;

		parent.nandMark(EntityInstance.SHADING_KNOWN_MARK);

		if (parentWidth <= 0 || parentHeight <= 0) {
			newX      = oldX;
			newY      = oldY;
			newWidth  = 0;
			newHeight = 0;
		} else {
			newX      = (int) (e.xRelLocal()      * parentWidth);
			newY      = (int) (e.yRelLocal()      * parentHeight);
			newWidth  = (int) (e.widthRelLocal()  * parentWidth);
			newHeight = (int) (e.heightRelLocal() * parentHeight);
		}
		sameSize = (oldWidth == newWidth) && (oldHeight == newHeight);
		if (sameSize && oldX == newX && oldY == newY) {
			return;
		}
		setBounds(newX, newY, newWidth, newHeight);

		if (!sameSize) {
			EntityComponent	child;
			int				i;

			entitySizeChanged();

			for (i = getComponentCount(); --i >= 0; ) {
				child = (EntityComponent) getComponent(i);
				child.setEntityBounds(this);
	}	}	}

	public void setEntityBounds()
	{
		Component parent = getParent();

		if (parent != null && parent instanceof EntityComponent) {
			setEntityBounds((EntityComponent) parent);
	}	}

	/* N.B. Must set graphics color prior to entry */

	public static void paintImage(Graphics g, int image, int x, int y, int width, int height, boolean force)
	{
		int	i;

		for (i = EntityClass.ENTITY_IMAGE_LAST; i != 0; i >>= 1) {
			if ((i & image) == 0) {
				continue;
			}
			switch (i) {
				case EntityClass.ENTITY_IMAGE_ACTOR:
				{
					int	min, centerx, centery;

					min = width;
					if (height < min) {
						min = height;
					}
					centerx = x+width/2;
					centery = y+height/2;

					g.drawOval(centerx-(min/10), centery-((7*min)/20), min/5, min/5);						// Head
					g.drawLine(centerx, centery-((3*min)/20), centerx, centery+((3*min)/20));				// Body
					g.drawLine(centerx-(min/4), centery-(min/20), centerx+(min/4), centery-(min/20));		// Arms
					g.drawLine(centerx, centery+((3*min)/20), centerx-(min/5), centery+((2*min)/5));		// Left leg
					g.drawLine(centerx, centery+((3*min)/20), centerx+(min/5), centery+((2*min)/5));		// Right leg
					break;
				}
				case EntityClass.ENTITY_IMAGE_OVAL:
				{
					g.drawOval(x, y, width, height);
					break;
				}
				case EntityClass.ENTITY_IMAGE_FRAME:
				{
					g.drawRect(x, y, width-1, height-1);
					break;
				}
				case EntityClass.ENTITY_IMAGE_ROUNDED_FRAME:
				{
					int arc_w = width/5;
					int arc_h = height/5;
					int arc   = Math.min(arc_w, arc_h);

					g.drawRoundRect(x, y, width-1, height-1, arc, arc);
					break;

				}
				case EntityClass.ENTITY_IMAGE_X:
				{
					int	min, centerx, centery;

					min = width;
					if (height < min) {
						min = height;
					}
					centerx = x+width/2;
					centery = y+height/2;

					g.drawLine(centerx-(min/4), centery-(min/4), centerx+(min/4), centery+(min/4));	
					g.drawLine(centerx+(min/4), centery-(min/4), centerx-(min/4), centery+(min/4));	
					break;
				}
				case EntityClass.ENTITY_IMAGE_CLASS:
				{
					if (force) {
						Util.drawStringClipped(g, "CLASS", x, y, width, height, false /* not centered */, false, false);
					}
					break;
				}
				case EntityClass.ENTITY_IMAGE_SPAN:
				{
					if (force) {
						Util.drawStringBottomClipped(g, "SPAN", x, y, width, height, false /* not centered */, false, false);
					}
					break;
				}
				case EntityClass.ENTITY_IMAGE_NAV_NEXT:
				{
					if (force) {
						NavNext_Button.paintIcon(g, x, y, width, height, 16, 16);
					}
					break;	
				}
				case EntityClass.ENTITY_IMAGE_NAV_PREV:
				{
					if (force) {
						NavPrev_Button.paintIcon(g, x, y, width, height, 16, 16);
					}
					break;
				}
			}
		}
	}

	private static void draw3DRectRaised(Graphics g, Color c, Color fill, int x, int y, int width, int height)
	{
		Option	option   = Options.getDiagramOptions();
		Color	brighter = Color.white;
		Color	darker   = Color.darkGray;
		int		pixels3D = option.getPixels3D();
		
		if (!option.isBlackWhite3D()) {
			brighter = c.brighter();
			darker   = c.darker();
		}

		if (fill != null) {
			g.setColor(fill);
			g.fillRect(x+pixels3D, y+pixels3D, width-2*pixels3D, height-2*pixels3D);
		}
		g.setColor(brighter);
		g.fillRect(x, y, pixels3D, height);																									// Band down left side
		g.fillRect(x + pixels3D, y, width - pixels3D, pixels3D);					// Band across top
		g.setColor(darker);
		g.fillRect(x + pixels3D, y + height-pixels3D, width-pixels3D, pixels3D);	// Band across bottom
		g.fillRect(x + width-pixels3D, y, pixels3D, height);																// Band down right side
		g.setColor(Color.black);
		g.drawLine(x,y, x+pixels3D-1, y+pixels3D-1);
    }    

	private static void draw3DRectSunken(Graphics g, Color c, Color fill, int x, int y, int width, int height)
	{
		Option	option   = Options.getDiagramOptions();
		Color	brighter = Color.white;
		Color	darker   = Color.darkGray;
		int		pixels3D = option.getPixels3D();

		if (!option.isBlackWhite3D()) {
			brighter = c.brighter();
			darker   = c.darker();
		}

		if (fill != null) {
			g.setColor(fill);
			g.fillRect(x+pixels3D, y+pixels3D, width-2*pixels3D, height-2*pixels3D);
		}
		g.setColor(darker);
		g.fillRect(x, y, pixels3D, height);																									// Band down left side
		g.fillRect(x + pixels3D, y, width - pixels3D, pixels3D);					// Band across top
		g.setColor(brighter);
		g.fillRect(x + pixels3D, y + height-pixels3D, width-pixels3D, pixels3D);	// Band across bottom
		g.fillRect(x + width-pixels3D, y, pixels3D, height);						// Band down right side
		g.setColor(Color.black);
		g.drawLine(x+width-pixels3D ,y+height-pixels3D, x+width-1, y+height-1);
    }

	public void clearScaledIcon()
	{
		m_scaledIcon = null;
	}
	
	public void elisionsChanged()
	{
		if (m_scaledIcon instanceof JLabel) {
			clearScaledIcon();
			repaint();
	}	}
	
	private static Icon computeScaledIcon(PaintShapeHelper helper, int width, int height)
	{
		// Get any cached scaled Icon
		Icon scaledIcon   = helper.getScaledIcon();
		int	 scaledWidth;
		int	 scaledHeight;
		
		if (scaledIcon == null) {
			scaledWidth  = scaledHeight = -1;
		} else {
			scaledWidth  = scaledIcon.getIconWidth();
			scaledHeight = scaledIcon.getIconHeight();
			if (scaledIcon instanceof JLabel) {
				if (width != scaledWidth || height != scaledHeight) {
					((JLabel) scaledIcon).setSize(width, height);
				}
				return scaledIcon;
		}	}

		if (scaledWidth != width || scaledHeight != height){

			// If we didn't have a cached value or the scale is wrong
			// get the unscaled image which has to be scaled

			Icon unscaledIcon = helper.getUnscaledIcon();
			
			if (unscaledIcon == null) {
				scaledIcon = null;
			} else {
				int unscaledWidth  = unscaledIcon.getIconWidth();
				int unscaledHeight = unscaledIcon.getIconHeight();
				 
				if (unscaledWidth == width && unscaledHeight == height) {
					scaledIcon = unscaledIcon;
				} else {
					int	desiredWidth;
					int	desiredHeight;
					
					Option	option = Options.getDiagramOptions();
					desiredWidth   = width;
					desiredHeight  = height;
					if (option.isIconFixedShape()) {
						desiredHeight = (int) (((double) unscaledHeight) * ((double) width) / ((double) unscaledWidth));
						if (desiredHeight > height) {
							desiredWidth  = (int) (((double) unscaledWidth) * ((double) height) / ((double) unscaledHeight));
							desiredHeight = height;
					}	}
					if (unscaledWidth == desiredWidth && unscaledHeight == desiredHeight) {
						scaledIcon = unscaledIcon;
					} else if (scaledWidth != desiredWidth || scaledHeight != desiredHeight) {
						BufferedImage resizedImg = new BufferedImage(desiredWidth, desiredHeight, BufferedImage.TYPE_INT_RGB);
						Graphics2D g2 = resizedImg.createGraphics();

						g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
						g2.drawImage(((ImageIcon) unscaledIcon).getImage(), 0, 0, desiredWidth, desiredHeight, null);
						g2.dispose();
						scaledIcon = new ImageIcon(resizedImg);
			}	}	}
			// Offer the helper the option of caching this scaled image
			helper.setScaledIcon(scaledIcon);
		}
		return (scaledIcon);
	}

	/* N.B. Any change to shapes requires potential correction to EdgePoint.adjustEdgePoint() */

	public static void paintShape(Graphics g, PaintShapeHelper helper, EntityClass entityClass, int x, int y, int width, int height, boolean isDrawRoot, boolean isOpen, Color foregroundColor, Color backgroundColor) 
	{
		if (width > 0 && height > 0) {
			int			   style, direction, image;
			int[]		   xp = m_xp;
			int[]		   yp = m_yp;
			int			   polygon_dimension = 0;
			int			   regular_dimension = 0;
			Option		   option            = Options.getDiagramOptions();
			int			   icon_rule         = option.getIconRule();

			image = EntityClass.ENTITY_IMAGE_NONE;
			if (isDrawRoot) {
				style     = EntityClass.ENTITY_STYLE_3DBOX;
				direction = 0;
			} else {
				style     = entityClass.getInheritedStyle();
				direction = entityClass.getDirection();
				if (!isOpen) {
					image = entityClass.getImage();

					if (icon_rule != Option.ICON_RULE_NONE) {
						int	iconY, iconHeight;
						
						iconY      = y;
						iconHeight = height;
						
						if (icon_rule != Option.ICON_RULE_PLAIN) {
							String label = helper.getEntityLabel();
							if (label == null || label.length() == 0) {
								icon_rule = Option.ICON_RULE_PLAIN;
						}	}
						switch (icon_rule) {
							case Option.ICON_RULE_TOP:
							case Option.ICON_RULE_BOTTOM:
							{
								FontMetrics	fm			= g.getFontMetrics();
								int			fontHeight  = fm.getHeight();
								
								if (fontHeight < height) {
									iconHeight = height - fontHeight;
									if (icon_rule == Option.ICON_RULE_TOP)	{
										iconY = y + fontHeight;
								}	}
								break;
						}	}

						Icon scaledIcon = computeScaledIcon(helper, width, iconHeight);
						if (scaledIcon != null)	{
							int scaledWidth  = scaledIcon.getIconWidth();
							int scaledHeight = scaledIcon.getIconHeight();

							scaledIcon.paintIcon(null /* Component c */, g, x + ((width - scaledWidth)>>>1), iconY + ((iconHeight - scaledHeight)>>>1));
							style = EntityClass.ENTITY_STYLE_NONE;
						}
						if (option.isIconFixedShape()) {
							g.setColor(backgroundColor);
							Util.drawOutlineBox(g, x, y, width, height);
			}	}	}	}

			switch (style) {
				case EntityClass.ENTITY_STYLE_3DBOX:
				{
					draw3DRectRaised(g, foregroundColor, backgroundColor, x, y, width-1, height-1);
					break;
				}
				case EntityClass.ENTITY_STYLE_SUNK_BOX:
				{
					draw3DRectSunken(g, foregroundColor, backgroundColor, x, y, width-1, height-1);
					break;
				}
				case EntityClass.ENTITY_STYLE_2DBOX:
				{
					if (backgroundColor != null) {
						g.setColor(backgroundColor);
						g.fillRect(x, y, width, height);
					}
					g.setColor(foregroundColor);
					g.drawRect(x, y, width-1, height-1);
					break;
				}
				case EntityClass.ENTITY_STYLE_FILE:
				{
					int fd = Math.min(Math.min(width, height)/2, 16 /* FLAP_DIM */);

					/*
					   0--(width-fd)----1 \        
					   |                     \    (fd)
					   |				        \ 
					   |                           2
					   | 						    |
					   |						 (height)
					   |							|
					   4 -------------------------- 3
					 */

					xp[0] = x;
					yp[0] = y;
					xp[1] = x + width - fd;
					yp[1] = y;
					xp[2] = x + width - 1;
					yp[2] = y + fd;
					xp[3] = xp[2];
					yp[3] = y + height - 1;
					xp[4] = x;
					yp[4] = yp[3];
					xp[5] = xp[0];
					yp[5] = yp[0];

					if (backgroundColor != null) {
						g.setColor(backgroundColor);
						g.fillPolygon(xp, yp, 6);
					}
					g.setColor(foregroundColor);
					g.drawPolygon(xp, yp, 6);

					xp[0] = xp[1];
					yp[0] = yp[2];
					xp[3] = xp[0];
					yp[3] = yp[0];

					g.drawPolygon(xp, yp, 4);
					break;
				}

				case EntityClass.ENTITY_STYLE_DISK:
				{
					int ad = Math.min(Math.min(width, height)/2, 8 /* ARC_DIM */);

					// Draw a cylinder

					if (backgroundColor != null) {
						g.setColor(backgroundColor);
						g.fillRect(x, y+ad/2, width, height-ad);
						g.fillOval(x, y, width, ad);
						g.fillOval(x, y+height-ad, width, ad);
					}
					g.setColor(foregroundColor);

					if (isOpen) {
						g.drawArc(x, y, width, ad, 180, -180);
					} else {
						g.drawOval(x, y, width, ad);
					}
					g.drawArc(x, y+height-ad-1, width, ad, 180, 180);
					g.drawLine(x, y+ad/2, x, y+height-ad/2);
					g.drawLine(x+width-1, y+ad/2, x+width-1, y+height-ad/2);
					break;
				}

				case EntityClass.ENTITY_STYLE_FOLDER:
				{
					// Compute flap size

					int fw = ((int) (((double) width) * .4));
					int fh = Math.max(MIN_FLAP_HT, Math.min(MAX_FLAP_HT, ((int) (height * .2))));
					int fm = FLAP_MARGIN;
					int x0, y0, x1, y1, x2, y2, x3, y3, x4, y4, x5, y5;

					if (fw < MIN_FLAP_WD) {
					   fw += fw/2;
					}
					if (fw < TINY_FLAP_WD) {
						fw = Math.min(fw + FLAP_MARGIN, width - width/3);
						fm = 0;
					}

				/*   
                     <--------------fw-------------------------->

                                     2--------------------------3
                   (fh)             /                            \
				     0<--- fm---->1 fh/2                      fh/2  4---------------------------5
				     |                                                                         |
					 |																		   |
					 |																		   |
					 7------------------------------------------------------------------------ 6
			     */

					if (backgroundColor != null) {
						g.setColor(backgroundColor);
						g.fillRect(x, y, width-1, height-1);
					}

					g.setColor(foregroundColor);
					g.drawRect(x,y,width-1, height-1);

					x0 = x;
					y0 = y+fh;
					x5 = x + width - 1;
					y5 = y0;

					if (isOpen) {
						g.drawLine(x0, y0, x5, y5);
					} else {
						x1 = x+fm;
						y1 = y+fh;

						g.drawLine(x0, y0, x1, y1);

						x2 = x1+fh/2;
						y2 = y;

						g.drawLine(x1, y1, x2, y2);

						x3 = x + fw;
						y3 = y2;

						g.drawLine(x2, y2, x3, y3);

						x4 = x3 + fh/2;
						y4 = y0;

						g.drawLine(x3, y3, x4, y4);
						g.drawLine(x4, y4, x5, y5);
					}
/*
					xp[0] = x;
					yp[0] = y+fh;
					xp[1] = x+fm;
					yp[1] = y+fh;
					xp[2] = xp[1]+fh/2;
					yp[2] = y;
					xp[3] = x + fw;
					yp[3] = yp[2];
					xp[4] = xp[3]+fh/2;
					yp[4] = yp[0];
					xp[5] = x + width-1;
					yp[5] = yp[0];
					xp[6] = xp[5];
					yp[6] = y + height-1;
					xp[7] = xp[0];
					yp[7] = yp[6];
					xp[8] = xp[0];
					yp[8] = yp[0];

					if (backgroundColor != null) {
						g.setColor(backgroundColor);
						g.fillPolygon(xp, yp, 9);
					}

					g.setColor(foregroundColor);
					g.drawPolygon(xp, yp, 9);

					if (!isOpen) {
						g.drawLine(xp[1], yp[1], xp[4], yp[4]);
					}
 */
					break;
				}
				case EntityClass.ENTITY_STYLE_SOURCEOBJ:
				{
					if (backgroundColor != null) {
						g.setColor(backgroundColor);
						g.fillOval(x, y, width, height);
					}
					g.setColor(foregroundColor);
					g.drawOval(x, y, width, height);
					break;
				}

				case EntityClass.ENTITY_STYLE_CLASS:
				{
					int arc_w = width/4;
					int arc_h = height/4;
					int arc   = Math.min(arc_w, arc_h);

					if (backgroundColor != null) {
						g.setColor(backgroundColor);
						g.fillRoundRect(x, y, width-1, height-1, arc, arc);
					}
					g.setColor(foregroundColor);
					g.drawRoundRect(x, y, width-1, height-1, arc, arc);
					break;
				}

				case EntityClass.ENTITY_STYLE_GROUP:
				{
					g.setColor(backgroundColor);
					Util.drawOutlineBox(g, x, y, width, height);
					break;
				}

				case EntityClass.ENTITY_STYLE_LABELLED_GROUP:
				{
					String label = helper.getEntityLabel();

					g.setColor(backgroundColor);
					Util.drawGroupBox(g, x, y, width, height, label /* Label is used only for sizing info */);
					break;
				}
				case EntityClass.ENTITY_STYLE_TRIANGLE:
				{
					switch (direction) {
					case 0:
						xp[0] = x;
						yp[0] = y+height-1;
						xp[1] = x+width/2;
						yp[1] = y;
						xp[2] = x+width-1;
						yp[2] = yp[0];
						break;
					case 1:
						xp[0] = x;
						yp[0] = y;
						xp[1] = x+width-1;
						yp[1] = y+height/2;
						xp[2] = xp[0];
						yp[2] = y+height-1;
						break;
					case 2:
						xp[0] = x;
						yp[0] = y;
						xp[1] = x + width - 1;
						yp[1] = yp[0];
						xp[2] = x + width/2;
						yp[2] = y + height - 1;
						break;
					case 3:
						xp[0] = x + width - 1;
						yp[0] = y;
						xp[1] = x;
						yp[1] = y + height/2;
						xp[2] = xp[0];
						yp[2] = y + height - 1;
						break;
					}
					polygon_dimension = 4;
					break;
				}
				case EntityClass.ENTITY_STYLE_ROMBUS:
				{
					switch (direction) {
					case 0:
						xp[0] = x;
						yp[0] = y+height-1;
						xp[1] = x+width/5;
						yp[1] = y;
						xp[2] = x+width-1;
						yp[2] = y;
						xp[3] = x+(width*4)/5;
						yp[3] = y+height-1;
						break;
					case 1:
						xp[0] = x;
						yp[0] = y;
						xp[1] = x+width-1;
						yp[1] = y+height/5;
						xp[2] = xp[1];
						yp[2] = y+height-1;
						xp[3] = xp[0];
						yp[3] = y+(4*height)/5;
						break;
					case 2:
						xp[0] = x+width/5;
						yp[0] = y+height-1;
						xp[1] = x;
						yp[1] = y;
						xp[2] = x+(4 *width)/5;
						yp[2] = y;
						xp[3] = x+width-1;
						yp[3] = y+height-1;
						break;
					case 3:
						xp[0] = x;
						yp[0] = y+height/5;
						xp[1] = x+width-1;
						yp[1] = y;
						xp[2] = xp[1];
						yp[2] = y+(4*height)/5;
						xp[3] = xp[0];
						yp[3] = y+height-1;
						break;
					}
					polygon_dimension = 5;
					break;
				}
				case EntityClass.ENTITY_STYLE_TRAPEZOID:
				{
					switch (direction) {
					case 0:
						xp[0] = x;
						yp[0] = y+height-1;
						xp[1] = x+width/5;
						yp[1] = y;
						xp[2] = x+(4*width)/5;
						yp[2] = yp[1];
						xp[3] = x+width-1;
						yp[3] = yp[0];
						break;
					case 1:
						xp[0] = x;
						yp[0] = y;
						xp[1] = x + width - 1;
						yp[1] = y + height/5;
						xp[2] = xp[1];
						yp[2] = y + (4*height)/5;
						xp[3] = xp[0];
						yp[3] = y + height - 1;
						break;
					case 2:
						xp[0] = x;
						yp[0] = y;
						xp[1] = x+width-1;
						yp[1] = yp[0];
						xp[2] = x+(4*width)/5;
						yp[2] = y+height-1;
						xp[3] = x+width/5;
						yp[3] = yp[2];
						break;
					case 3:
						xp[0] = x + width - 1;
						yp[0] = y;
						xp[1] = x;
						yp[1] = y + height/5;
						xp[2] = xp[1];
						yp[2] = y + (4*height)/5;
						xp[3] = xp[0];
						yp[3] = y + height - 1;
						break;
					}
					polygon_dimension = 5;
					break;
				}
				case EntityClass.ENTITY_STYLE_TRIANGLE2:
				{
					regular_dimension = 3;
					break;
				}
				case EntityClass.ENTITY_STYLE_RECTANGLE:
				{
					regular_dimension = 4;
					break;
				}

				case EntityClass.ENTITY_STYLE_PENTAGON:
				{
					regular_dimension = 5;
					break;
				}
				case EntityClass.ENTITY_STYLE_HEXAGON:
				{
					regular_dimension = 6;
					break;
				}
				case EntityClass.ENTITY_STYLE_OCTAGON:
				{
					regular_dimension = 8;
					break;
				}
				case EntityClass.ENTITY_STYLE_DECAHEDRON:
				{
					regular_dimension = 10;
					break;
				}
				case EntityClass.ENTITY_STYLE_12SIDED:
				{
					regular_dimension = 12;
					break;
				}
				case EntityClass.ENTITY_STYLE_14SIDED:
				{
					regular_dimension = 14;
					break;
				}
				case EntityClass.ENTITY_STYLE_16SIDED:
				{
					regular_dimension = 16;
					break;
				}
				case EntityClass.ENTITY_STYLE_18SIDED:
				{
					regular_dimension = 18;
					break;
				}
				case EntityClass.ENTITY_STYLE_20SIDED:
				{
					regular_dimension = 20;
					break;
				}
				case EntityClass.ENTITY_STYLE_PAPER:
				{
					int	baseline = y+(height*4)/5;

					xp[0] = x+(3*width)/8;			// x-
					yp[0] = baseline;				// | /
					xp[1] = xp[0];					// |/
					yp[1] = y+((193*height)/200);	// x
					xp[2] = x+(2*width)/3;			//    Diagonal up
					yp[2] = baseline;				//    Right top point			
					xp[3] = xp[0];
					yp[3] = yp[0];

					if (backgroundColor != null) {
						g.setColor(backgroundColor);
						g.fillRect(x,y, width, (height*4)/5);
						g.fillArc(x, y+height/2, width/2, height/2, -180, 180);
						g.fillPolygon(xp,yp, 4);								// This triangle connects the arc with the baseline

					}

					g.setColor(foregroundColor);
					g.drawLine(x, y, x, y+(height*3)/4);						// Left vertical edge
					g.drawLine(x, y, x+width-1, y);								// Top horizontal edge
					g.drawLine(x+width-1, y, x+width-1, baseline);				// Right vertical edge
					g.drawLine(x+(2*width)/3, baseline, x+width-1, baseline);

					g.drawArc(x, y+(height/2)-1, (width/2), (height/2)-1, -180, 120);
					g.drawLine( xp[1],yp[1], xp[2], yp[2]);

//					g.drawArc(x+(width/12),  baseline, x+((7*width)/6), y+((5*height)/2)-1, 120, -30);
					break;
				}
/*
				default:
				{
					// For debugging (This shows the root)
					g.setColor(Color.red);
					g.drawLine(x, y,        x+width-1, y+height-1);
					g.drawLine(x, y+height-1, x+width-1, y);
				}
*/
			}
			if (regular_dimension != 0) {
				polygon_dimension = regular_dimension + 1;

				double	angle = Math.toRadians(entityClass.getAngle() - 90.0);		// -90.0 to point up N.B. coordinate at (0,-.5)
				double	shift = 2.0 * Math.PI/((double) regular_dimension);
				double	dwidth, dheight;
				int		i;

				dwidth  = (double) width;
				dheight = (double) height;
				for (i = 0; i < regular_dimension; ++i) {
					xp[i] = x + (int) (dwidth  * (1.0 + Math.cos(angle)) * 0.5);
					yp[i] = y + (int) (dheight * (1.0 + Math.sin(angle)) * 0.5);
					angle -= shift;
				}
			}

			if (polygon_dimension != 0) {
				xp[polygon_dimension-1] = xp[0];
				yp[polygon_dimension-1] = yp[0];
				if (backgroundColor != null) {
					g.setColor(backgroundColor);
					g.fillPolygon(xp, yp, polygon_dimension);
				}

				g.setColor(foregroundColor);
				g.drawPolygon(xp, yp, polygon_dimension);
			}

			if (image != EntityClass.ENTITY_IMAGE_NONE) {
				Color	inverse;

				g.setColor(backgroundColor);
				inverse = ColorCache.getInverse(backgroundColor.getRGB());
				g.setColor(inverse);

				paintImage(g, image, x, y, width, height, false);
			}	
	}	}		

	public void paintMap(Graphics g, int x, int y, int width, int height, EntityInstance onPath, int depth)
	{
		if (width > 0 && height > 0) {
			EntityInstance	entityInstance = m_entityInstance;
			Color			backgroundColor;

			if (entityInstance.hasChildren() && depth != 1) {
				backgroundColor = null;
			} else {
				if (entityInstance == onPath) {
					backgroundColor = Color.red.darker();
				} else {
					backgroundColor = entityInstance.getInheritedObjectColor();
			}	}
			paintShape(g, this, entityInstance.getEntityClass(), x, y, width, height, false /* Not draw root */, entityInstance.isOpen(), Color.black, backgroundColor);

			if (depth < 1) {
				Vector				srcRelList = entityInstance.getSrcRelList();

				if (srcRelList != null) {
					RelationInstance	ri;
					EntityInstance		child;
					int					x1, y1,width1, height1, i;


					for (i = srcRelList.size(); --i >= 0; ) {
						ri = (RelationInstance) srcRelList.elementAt(i);
						if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
							child   = ri.getDst();
							if (child.isShown()) {
								x1      = x + (int) (width  * child.xRelLocal());
								y1      = y + (int) (height * child.yRelLocal()); 
								width1  = (int) (width  * child.widthRelLocal());
								height1 = (int) (height * child.heightRelLocal());
								child.paintMap(g, x1, y1, width1, height1, onPath, depth+1);
	}	}	}	}	}	}	}

	// PaintShapeHelper interface

	public String getEntityLabel()
	{
		return m_entityInstance.getEntityLabel();
	}

	public Icon getScaledIcon()
	{
		Option	option      = Options.getDiagramOptions();
		int		elisionIcon = option.getElisionIcon();
		if (elisionIcon >= 0) {
			EntityInstance	e = m_entityInstance;
			
			if (!e.isMarked(EntityInstance.LEGEND_MARK)) {
				EntityClass		ec       = e.getEntityClass();
				Diagram			diagram  = e.getDiagram();
				RelationClass	rc       = diagram.numToRelationClass(elisionIcon);
				
				return IconCache.getElisionIcon(e.getElisions(rc), rc.toString());
		}	}
		return m_scaledIcon;
	}

	public void setScaledIcon(Icon icon)
	{
		m_scaledIcon = icon;
	}
	
	public Icon getUnscaledIcon()
	{
		return m_entityInstance.getEntityClass().getUnscaledIcon();
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
//		System.out.println("EntityComponent.paintIcon");
		g.translate(x, y);
		paintComponent(g);
	}
}
