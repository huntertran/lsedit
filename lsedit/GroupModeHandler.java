package lsedit;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Vector;


import javax.swing.JComponent;

/* This handler is responsible for moving things about on the screen
   when dragged.
 */

public class GroupModeHandler extends LandscapeModeHandler
{

	// A small JComponent so that the whole handler doesn't need to be one
	// This object just draws the outline of the thing(s) being moved

	protected class DrawOutline extends JComponent
	{
		public DrawOutline()
		{
			super();
			setForeground(Color.BLACK);
		}

		public void paintComponent(Graphics g)
		{
			if (m_seen_motion) {
				Vector			groupList = m_groupList;
				EntityInstance	e;
				EntityComponent	entityComponent;
				int				i;

				for (i = groupList.size(); --i >= 0; ) {
					e               = (EntityInstance) groupList.elementAt(i); 
					entityComponent = e.getEntityComponent();
					g.drawRect(entityComponent.getDiagramX()-m_grpLayoutX, entityComponent.getDiagramY()-m_grpLayoutY, entityComponent.getWidth(), entityComponent.getHeight());
	}	}	}	}

	protected static final int NONE				= 0;
	protected static final int SINGLE_MOVE		= 1;

	protected EditModeHandler	m_parent;
	protected DrawOutline		m_drawOutline;				// The outline drawn
	protected Vector			m_groupList = null;			// List of entities being moved

	protected int				m_grpLayoutX;				// Bounding box for set of entities moved
	protected int				m_grpLayoutY;
	protected int				m_grpLayoutWidth;
	protected int				m_grpLayoutHeight;

	protected int				m_shiftX;					// Top left hand corner of bounding box after the move
	protected int				m_shiftY;

	protected int				m_groupMode = NONE;			// State of move process

	protected int				m_dx, m_dy;					// Initial mouse point at start of move (future mouse points w.r.t. these)
	private	  boolean			m_seen_motion;		

	// Returns true if any of the entities in group overlap any of the entities in container

	protected boolean moveOverlaps(Vector group, EntityInstance container)
	{
		EntityInstance	e;
		int				i;

		for (i = group.size(); --i >= 0; ) {
			e = (EntityInstance) group.elementAt(i);
			if (e.overlaps(container)) {
				return true;
		}	}
		return false;
	}

	// This can only move things contained within an entity in the drawing

	protected void moveGroupInto(EntityInstance container, int dx, int dy, Diagram diagram) 
	{
		Vector			groupList      = m_groupList;
		int				grid           = diagram.getHardGrid();
		EntityInstance	e              = (EntityInstance) m_groupList.firstElement();
		EntityInstance	parent         = e.getContainedBy();
		int				i;

		if (parent == container) {
			EntityComponent parentComponent = parent.getEntityComponent();
			double			xShift          = ((double) dx) / parentComponent.getWidth();
			double			yShift			= ((double) dy) / parentComponent.getHeight();
			double			xRelLocal, yRelLocal;

			for (i = groupList.size(); --i >= 0; ) { 
				e               = (EntityInstance) groupList.elementAt(i);
				xRelLocal       = e.xRelLocal();
				if (xShift != 0) {
					xRelLocal +=  xShift;
					if (xRelLocal < 0.0) {
						xRelLocal = 0.0;
					} else {
						double	widthRelLocal   = e.widthRelLocal();

						if (xRelLocal + widthRelLocal > 1.0) {
							xRelLocal = 1.0 - widthRelLocal;
				}	}	}

				yRelLocal       = e.yRelLocal();
				if (yShift != 0) {
					yRelLocal += yShift;
					if (yRelLocal < 0.0) {
						yRelLocal = 0.0;
					} else {
						double heightRelLocal  = e.heightRelLocal();

						if (yRelLocal + heightRelLocal > 1.0) {
							yRelLocal = 1.0 - heightRelLocal;
				}	}	}
				diagram.updateLocationRelLocal(e, xRelLocal, yRelLocal);
			}
		} else {

			if (!moveOverlaps(groupList, container)) {
				for (i = groupList.size(); --i >= 0; ) {
					e = (EntityInstance) groupList.elementAt(i);
					diagram.updateMoveEntityContainment(container, e);
				}
			} else {
				for (i = groupList.size(); --i >= 0; ) {
					e          = (EntityInstance) groupList.elementAt(i);
					diagram.updateMovePlaceEntityContainment(container, e);
			}	} 
			diagram.prepostorder();
		}
	}

	protected void selectEntity(EntityInstance e) 
	{
		Diagram		  diagram = m_ls.getDiagram();

		if (e.getGroupFlag()) {
			// Already a member of a group

			if (e != diagram.getKeyEntity()) {
				diagram.setKeyEntity(e);
			}
		} else {
			EntityInstance old_ke = diagram.getKeyEntity();

			if (old_ke != null) {
				boolean cleared = diagram.clearFlags(false);
				diagram.setKeyEntity(e);
				if (cleared) {
					diagram.revalidate();
				}
			} else {
				diagram.setKeyEntity(e);
			}
		}
		m_ls.show_groupList();
	}

	// Toggle from unselected -> GROUPKEY -> GROUP

	protected void toggleMembership(EntityInstance e) 
	{
		Diagram		  diagram = m_ls.getDiagram();

		if (e.getGroupFlag()) {
			if (e == diagram.getKeyEntity()) {
				int size = diagram.getGroupedEntitiesCount();

				if (size == 1) {
					// Only one so just clear
					diagram.clearGroupFlags();
				} else {
					// Clear me making something else the key entry
					diagram.clearKeyEntity();
				}
			} else {
				diagram.clearEntityGroupFlag(e);
			}
			e.repaint();
		} else {
			int size = diagram.getGroupedEntitiesCount();

			// Nothing -> keyselected

			if (size > 0) {
				// See if this is in same container as others

				if (diagram.getGroupedEntitiesContainer() != e.getContainedBy()) {
					selectEntity(e);
					return;
				}
			}
			diagram.setKeyEntity(e);
		}

		m_ls.show_groupList();
	}

	protected void reportShift()
	{
		String	type = ((m_groupList.size() == 1) ? "entity" : "group");

		m_ls.doFeedback("Moving " + type + "(" + m_shiftX + ", " + m_shiftY + ")");
	}

	protected boolean moveGroupStart(MouseEvent ev, int x, int y) 
	{
		Diagram			diagram   = m_ls.getDiagram();
		Vector			groupList = diagram.getGroupedEntities();
		EntityInstance	e;
		EntityComponent	entityComponent;
		int				minX, maxX, minY, maxY;
		int				diagramX, diagramY;	
		int				i;

		m_groupList = groupList;
		m_seen_motion = false;

		if (groupList == null) {
			return false;
		}

		EntityInstance pe = ((EntityInstance) m_groupList.firstElement()).getContainedBy();

		if (pe == null) {
			return false;
		}

		// Determine the initial bounding box of whole group


		minX = minY = Integer.MAX_VALUE;
		maxX = maxY = Integer.MIN_VALUE;

		for (i = groupList.size(); --i >= 0; ) {
			e               = (EntityInstance) groupList.elementAt(i);
			entityComponent = e.getEntityComponent();
			diagramX        = entityComponent.getDiagramX();
			diagramY        = entityComponent.getDiagramY();

			if (diagramX < minX) {
				minX = diagramX;
			}
			if (diagramY < minY) {
				minY = diagramY;
			} 
			diagramX += entityComponent.getWidth();
			diagramY += entityComponent.getHeight();
			if (diagramX > maxX) {
				maxX = diagramX;
			}
			if (diagramY > maxY) {
				maxY = diagramY;
		}	}

		// grpLayout is bounding box in diagram coordinates

		m_grpLayoutX      = m_shiftX      = minX;
		m_grpLayoutY      = m_shiftY      = minY;
		m_grpLayoutWidth  = maxX - minX;
		m_grpLayoutHeight = maxY - minY;

		// The outline is the same size as the bounding box to minimise repainting
		
		m_drawOutline.setBounds(m_shiftX, m_shiftY, m_grpLayoutWidth + 1, m_grpLayoutHeight + 1);
		diagram.add(m_drawOutline /* JLayeredPane.PALETTE_LAYER */ , 0);

		m_dx	  = x - m_grpLayoutX; // Offset of mouse from top left
		m_dy	  = y - m_grpLayoutY;

		reportShift();
		return true;
	}

	protected void moveGroupMotion(MouseEvent ev, int x, int y) 
	{
		int				nx				= x - m_dx;					// Position of top left hand corner of new bounding box	
		int				ny				= y - m_dy;
		Diagram			diagram         = m_ls.getDiagram();
		int				grid			= diagram.getHardGrid();
		EntityInstance  drawRoot		= diagram.getDrawRoot();
		int				drawRootX		= drawRoot.getDiagramX();
		int				drawRootY		= drawRoot.getDiagramY();
		int				drawRootWidth	= drawRoot.getWidth();
		int				drawRootHeight	= drawRoot.getHeight();
		EntityInstance	e               = (EntityInstance) m_groupList.firstElement();
		EntityComponent	entityComponent = e.getEntityComponent();
		EntityComponent parentComponent = (EntityComponent) entityComponent.getParent();
		int				px				= parentComponent.getDiagramX();
		int				py				= parentComponent.getDiagramY();
		int				bound;

		if (!m_seen_motion) {
			m_seen_motion = true;
		}
		
		// Convert to coordinates of e relative to parent because this is how our grid is laid out

		if (grid != 1) {
			nx -= px;		
			ny -= py;

			// Compute position on the grid

			nx  = (nx/grid)*grid;
			ny  = (ny/grid)*grid;

			// Convert back to diagram coordinates 

			nx += px;
			ny += py;
		}

		// Make sure we don't moved out of the root

		bound = drawRootX /* +LandscapeEditorCore.GAP */;
		if (nx < bound) {
			nx = bound;
		}

		bound = drawRootY /* +LandscapeEditorCore.GAP */;
		if (ny < bound) {
			ny = bound;
		}

		bound = drawRootX+drawRootWidth /* - 2 * LandscapeEditorCore.GAP */ - m_grpLayoutWidth;
		if (nx > bound) {
			nx = bound;
		}
		bound = drawRootY+drawRootHeight /* -2 * LandscapeEditorCore.GAP */ - m_grpLayoutHeight;
		if (ny> bound) {
			ny = bound;
		}

		m_shiftX = nx;
		m_shiftY = ny;

		reportShift();
		m_drawOutline.setLocation(nx, ny);

//		System.out.println("nx = " + nx + " ny = " + ny);
		m_drawOutline.repaint();
	}

	protected void moveGroupEnd(MouseEvent ev) 
	{
		m_ls.clearFeedback();

		if (m_seen_motion) {

			int				dx        = m_shiftX - m_grpLayoutX;
			int				dy        = m_shiftY - m_grpLayoutY;
			Diagram			diagram   = m_ls.getDiagram();
			EntityInstance  drawRoot  = diagram.getDrawRoot();
			EntityInstance	ke        = diagram.getKeyEntity();
			EntityComponent	component = ke.getEntityComponent();
			int				keX       = component.getDiagramX() + dx;
			int				keY       = component.getDiagramY() + dy;
			int				keWidth   = component.getWidth();
			int				keHeight  = component.getHeight();
			EntityInstance	container = drawRoot.containing(keX, keY, keWidth, keHeight);
			Vector			groupList = m_groupList;

			while (container != null && groupList.contains(container)) {
				container = container.getContainedBy();
			}

			if (container == null) {
				m_ls.error("Failed to find container");
				cleanup();
				return;
			}

			boolean changeParent = (container != ke.getContainedBy());
			

			diagram.beginUndoRedo("Group Move");

//			System.out.println("GroupModeHandler.moveGroupEnd dx=" + dx + " dy=" + dy);
			moveGroupInto(container, dx, dy, diagram);
			if (changeParent) {
				m_ls.doFeedback("Group move into: " + container.getEntityLabel());
			}
			diagram.endUndoRedo();
		}
		cleanup();
	}

	//
	// Public methods
	//

	public GroupModeHandler(EditModeHandler parent) 
	{
		super(parent.m_ls);
		m_parent      = parent;
		m_drawOutline = new DrawOutline();
	}

	public void cleanup()
	{
		Diagram	diagram = m_ls.getDiagram();

		diagram.remove(m_drawOutline);
		m_ls.setCursor(Cursor.DEFAULT_CURSOR);
		
		m_groupMode = NONE;
	}

	public void moveGroup(int key) 
	{
		Diagram diagram = m_ls.getDiagram();
		int		grid    = diagram.getHardGrid();

		m_groupList = diagram.getGroupedEntities();

		if (m_groupList == null) {
			return;
		}

		EntityInstance	e               = diagram.getKeyEntity();
		EntityComponent	entityComponent = e.getEntityComponent();
		EntityComponent parentComponent = (EntityComponent) entityComponent.getParent();
		EntityInstance	pe              = parentComponent.getEntityInstance();

		int dx = 0;
		int dy = 0;

		switch(key) {
		case Do.MOVE_GROUP_UP:
			dy = -grid;
			break;

		case Do.MOVE_GROUP_DOWN:
			dy = grid;
			break;

		case Do.MOVE_GROUP_RIGHT:
			dx = grid;
			break;

		case Do.MOVE_GROUP_LEFT:
			dx = -grid;
			break;
		}

		if (grid != 1) {
			if (dx != 0) {
				int	px	= entityComponent.getX();

				dx += px;
				dx  = (dx/grid)*grid;
				dx -= px;
			}
			if (dy != 0) {
				int	py	= entityComponent.getY();
	
				dy += py;
				dy  = (dy/grid)*grid;
				dy -= py;
		}	}

		moveGroupInto(pe, dx, dy, diagram);
	}

	public void entityPressed(MouseEvent ev, EntityInstance e, int x, int y) 
	{
		Diagram diagram = m_ls.getDiagram();

//		System.out.println("GroupModeHandler mousedown e=" + e + " x=" + x + " y=" + y);

		if (ev.isShiftDown()) {
			if (!e.isDrawRoot()) {
				toggleMembership(e);
			}
		} else {
			selectEntity(e);
 
			if (!e.isDrawRoot() && !e.isClientOrSupplier()) {
				m_ls.setCursor(Cursor.MOVE_CURSOR);
				m_groupMode = SINGLE_MOVE;

				if (moveGroupStart(ev, x, y)) {
					m_parent.setSubHandler(this);
				}
				return;
			}
			// Otherwise it is a simple selection event
		}
		m_groupMode = NONE;
	}

	public void entityDragged(MouseEvent ev, EntityInstance e, int x, int y) 
	{
//		System.out.println("GroupModeHandler.entityDragged()");
		if (m_groupMode == SINGLE_MOVE)	{
			moveGroupMotion(ev, x, y);
		}
	}

	public void entityReleased(MouseEvent ev, EntityInstance e, int x, int y) 
	{
		if (m_groupMode == SINGLE_MOVE) {
			moveGroupEnd(ev);
			m_parent.cleanup();
	}	}

	public void relationPressed(MouseEvent ev, RelationInstance ri, int x, int y)
	{
		m_ls.getDiagram().setGroupAndHighlightFlag(ri);
		m_parent.cleanup();
	}
}
