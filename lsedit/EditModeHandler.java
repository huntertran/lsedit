package lsedit;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Event;
import java.awt.Point;

import java.awt.event.MouseEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class EditModeHandler extends LandscapeModeHandler /* extends Object */ {

	// Supported modes

	protected MoveModeHandler	  m_moveHandler;
	protected ResizeModeHandler	  m_resizeHandler;
	protected GroupModeHandler	  m_groupingHandler;
	protected NewEdgeModeHandler  m_newEdgeModeHandler;
	protected FrameModeHandler	  m_frameHandler;

	private  LandscapeModeHandler m_handler = null;
	private	 long			      m_lastUp;
	private  boolean		 	  m_mouseIsDown = false;

	// Supported popups

	protected JPopupMenu		  m_entityPopup;
	protected JPopupMenu		  m_relationPopup;

	protected JPopupMenu buildEntityPopup()
	{
		JPopupMenu m;
		JMenu	   m1;
		
		m = new JPopupMenu("Entity options");
		m_ls.entityPopupMenuItem(m, m_ls, m_ls.isApplet());
		return(m);
	}

	protected JPopupMenu buildRelationPopup()
	{
		JPopupMenu m;
		JMenu	   m1;
		
		m = new JPopupMenu("Edge options");
		Do.edgePopupMenuItem(m, m_ls, m_ls.isApplet());

		return(m);
	}

	// --------------
	// Public methods
	// --------------

	public EditModeHandler(LandscapeEditorCore ls) 
	{
		super(ls);

		m_moveHandler        = new MoveModeHandler(this);
		m_resizeHandler      = new ResizeModeHandler(this);
		m_groupingHandler    = new GroupModeHandler(this);
		m_frameHandler       = new FrameModeHandler(this);
	}

	public boolean mouseIsDown()
	{
		return m_mouseIsDown;
	}

	public 	LandscapeModeHandler getSubHandler()
	{
//		System.out.println("EditModeHandler.getSubHandler");
		return(m_handler);
	}

	public 	void setSubHandler(LandscapeModeHandler handler)
	{
//		System.out.println("EditModeHandler.setSubHandler from " + m_handler + " to " + handler);
		if (m_handler != null) {
			m_handler.cleanup();
		}
		m_handler = handler;
	}

	public void cleanup() 
	{
//		System.out.println("EditModeHandler.cleanup");
		setSubHandler(null);
		m_mouseIsDown = false;
	}

	protected static void usesObject(Object c, Object object)
	{
		if (c instanceof MyMenuItem) {
			((MyMenuItem) c).setObject(object);
			return;
		}
		if (c instanceof JMenu) {
			JMenu 		c1;
			JMenuItem	item;
			int		i;
			
			c1 = (JMenu) c;
			for (i = c1.getItemCount(); i > 0; ) {
				item = c1.getItem(--i);
				if (item != null) {
					usesObject(item, object);
			}	}
			return;
		}				
		if (c instanceof Container) {
			Container	c1;
			int			i;

			c1 = (Container) c;
			for (i = c1.getComponentCount(); i > 0; ) {
				usesObject(c1.getComponent(--i), object);
			}
			return;
		}
//		System.out.println("EditModeHandler.usesObject " + c);
	}

	public void moveGroup(int key)
	{
		m_groupingHandler.moveGroup(key);
	}
	
	public void newEdge(Object object)
	{
		if (m_newEdgeModeHandler == null) {
			m_newEdgeModeHandler = new NewEdgeModeHandler(this);
		}
		m_newEdgeModeHandler.activate(object);
	}
			
	public void rightClickEntity(MouseEvent ev, EntityInstance e, int x, int y)
	{
		LandscapeEditorCore	ls    = m_ls;
		Point				shift = ls.getDiagramViewPosition();
		JPopupMenu			m     = m_entityPopup;
	
		if (m == null) {
			m_entityPopup = m = buildEntityPopup();
		}
	
		usesObject(m, e);
		FontCache.setMenuTreeFont(m); 
		ls.add(m);
		m.show(ls.getContentPane(), ls.getDiagramX()+x-shift.x, ls.getDiagramY()+y-shift.y);
	}

	public void rightClickRelation(MouseEvent ev, RelationInstance ri, int x, int y)
	{
		LandscapeEditorCore	ls    = m_ls;
		Point				shift = ls.getDiagramViewPosition();
		JPopupMenu			m     = m_relationPopup;
		
		if (m == null) {
			m_relationPopup = m = buildRelationPopup();
		}
		usesObject(m, ri);
		FontCache.setMenuTreeFont(m); 
		ls.add(m);
		m.show(ls.getContentPane(), ls.getDiagramX()+x-shift.x, ls.getDiagramY()+y-shift.y);
	}

	public void entityPressed(MouseEvent ev, EntityInstance e, int x, int y) 
	{
//		System.out.println("EditModeHandler entity mousedown\n");

		if (ev.isMetaDown()) {
			rightClickEntity(ev, e, x, y);
			return;
		}

		m_mouseIsDown     = true;
//		System.out.println("EditModeHandler.entityPressed " + m_handler);
		if (m_handler != null) {
			m_handler.entityPressed(ev, e, x, y);
			return;
		}

		if (e.isDrawRoot()) {
			m_frameHandler.entityPressed(ev, e, x, y);
			return;
		}

		if (ev.isShiftDown()) {
			m_groupingHandler.entityPressed(ev, e, x, y);
			return;
		}
		// Determine if bend/IO move, resize, or group move/activate

		/* Test to see if we are resizing an entity */
		m_resizeHandler.entityPressed(ev, e, x, y);
		if (m_handler != null) {
			return;
		}
		/* Test to see if we are moving an I/O point */
		m_moveHandler.entityPressed(ev, e, x, y);
		if (m_handler != null) {
			return;
		}
		m_groupingHandler.entityPressed(ev, e, x, y);
	}

	public void entityReleased(MouseEvent ev, EntityInstance e, int x, int y)
	{
		if (m_mouseIsDown) {
			long newUp = ev.getWhen();

//			System.out.println("EditModeHandler.entityReleased " + m_handler);
			if (newUp - m_lastUp < 300) {
//				System.out.println("Double click");
				m_ls.setCursor(Cursor.DEFAULT_CURSOR);
				mouseDoubleClick(ev, e);
				m_lastUp = 0;
			} else if (m_handler != null) {
				m_handler.entityReleased(ev, e, x, y);
			}
			m_lastUp    = newUp;
			m_mouseIsDown = false;
	}	}

	public void relationPressed(MouseEvent ev, RelationInstance ri, int x, int y) 
	{
//		System.out.println("EditModeHandler edge mousedown\n");

		if (ev.isMetaDown()) {
			rightClickRelation(ev, ri, x, y);
			return;
		}
		m_mouseIsDown   = true;

		if (m_handler != null) {
//			System.out.println("EditModeHandler.relationPressed " + m_handler);
			m_handler.relationPressed(ev, ri, x, y);
			return;
		} 

		if (!ev.isControlDown()) {
			if (ev.isShiftDown()) {
				m_groupingHandler.relationPressed(ev, ri, x, y);
				return;
			}
			Diagram	diagram = m_ls.getDiagram();
			boolean cleared = diagram.clearFlags(false);

			// Determine if edge point move
			m_moveHandler.relationPressed(ev, ri, x, y);
			if (m_handler == null) {
				m_groupingHandler.relationPressed(ev, ri, x, y);
			}
			if (cleared) {
				diagram.revalidate();
		}	}
	}

	public void relationReleased(MouseEvent ev, RelationInstance ri, int x, int y)
	{
		if (m_mouseIsDown) {
			long newUp = ev.getWhen();

			if (newUp - m_lastUp < 300) {

//				System.out.println("Double click");
				m_ls.setCursor(Cursor.DEFAULT_CURSOR);
				mouseDoubleClick(ev, ri);
				m_lastUp = 0;
			} else {
				m_lastUp = newUp;
//				System.out.println("EditModeHandler.relationReleased " + m_handler);
				if (m_handler != null) {
					m_handler.relationReleased(ev, ri, x, y);
			}	}
		}
		m_mouseIsDown = false;
	}
	
	public void mouseDoubleClick(MouseEvent ev, Object object) 
	{
		m_ls.processKey(Do.DESCEND, (ev.getModifiers() & (Event.SHIFT_MASK|Event.ALT_MASK|Event.CTRL_MASK|Event.META_MASK)), object);
	}

	public void movedOverThing(MouseEvent ev, Object thing, int x, int y) 
	{
//		m_ls.showInfo("Coordinate " + x + "x" + y);
		if (m_handler != null) {
			m_handler.movedOverThing(ev, thing, x, y);
			return;
		}
		m_resizeHandler.movedOverThing(ev, thing, x, y);
		if (m_handler == null) {
			m_moveHandler.movedOverThing(ev, thing, x, y);
	}	}

	public void entityDragged(MouseEvent ev, EntityInstance e, int x, int y) 
	{
//		System.out.println("EditModeHandler.entityDragged " + m_handler);
		if (m_handler != null) {
			m_handler.entityDragged(ev, e, x, y);
		}
	}

	public void relationDragged(MouseEvent ev, RelationInstance ri, int x, int y) 
	{
//		System.out.println("EditModeHandler.relationDragged " + m_handler);
		if (m_handler != null) {
			m_handler.relationDragged(ev, ri, x, y);
		}
	}
}
