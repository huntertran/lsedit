package lsedit;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JComponent;


/* This handler is responsible for selecting things by dragging a box over them
 */

class HorizontalLine extends JComponent
{
	public HorizontalLine()
	{
		super();
		setForeground(Color.GREEN);
	}

	public void paintComponent(Graphics g)
	{
		g.drawLine(0, 0, getWidth(), 0);
}	}

class VerticalLine extends JComponent
{
	public VerticalLine()
	{
		super();
		setForeground(Color.GREEN);
	}

	public void paintComponent(Graphics g)
	{
		g.drawLine(0, 0, 0, getHeight());
}	}

public class FrameModeHandler extends LandscapeModeHandler
{
	protected Diagram			m_diagram = null;

	// Using lines avoids massive amounts of repainting for large landscapes

	protected HorizontalLine	m_top     = new HorizontalLine();
	protected HorizontalLine	m_bottom  = new HorizontalLine();
	protected VerticalLine		m_left    = new VerticalLine();
	protected VerticalLine		m_right   = new VerticalLine();
	
	protected Rectangle			m_frame = null;

	protected boolean			m_shift_down;
	protected boolean			m_control_down;
	protected int				m_start_x, m_start_y;
	protected EditModeHandler	m_parent;

	//
	// Public methods
	//

	public FrameModeHandler(EditModeHandler parent) 
	{
		super(parent.m_ls);
		m_parent      = parent;
	}

	private void frameRelation(RelationInstance ri, Rectangle frame, boolean clear_flag)
	{
		if (ri.isRelationShown()) {
			RelationComponent relationComponent = ri.getRelationComponent();

			if (relationComponent != null && ((RelationComponent) relationComponent).isFramedBy(frame)) {
				if (clear_flag) {
					m_diagram.clearRelationGroupFlag(ri);
				} else {
					m_diagram.setGroupAndHighlightFlag(ri);
				}
			}
		}
	}

	private void frameRelations(EntityInstance e, Rectangle frame, boolean clear_flag)
	{
		int					i;
		Vector				v;
		Enumeration			en;
		RelationInstance	ri;
		EntityInstance		child;

		for (i = 0; i < 2; ++i) {
			switch(i) {
			case 0:
				v = e.getSrcLiftedList();
				break;
			default:
				v = e.getDstLiftedList();
				break;
			}

			if (v != null) {
				for (en = v.elements(); en.hasMoreElements(); ) {
					ri  = (RelationInstance) en.nextElement();
					frameRelation(ri, frame, clear_flag);
		}	}	}

		if (e.isOpen()) {
			for (en = e.getChildrenShown(); en.hasMoreElements(); ) {
				child = (EntityInstance) en.nextElement();
				frameRelations(child, frame, clear_flag);
		}	}
	}

	public void cleanup()
	{
		if (m_diagram != null) {
			Diagram		diagram     = m_diagram;

			diagram.remove(m_top);
			diagram.remove(m_bottom);
			diagram.remove(m_left);
			diagram.remove(m_right);

			m_diagram = null;
			m_ls.setCursor(Cursor.DEFAULT_CURSOR);
	}	}

	public void entityPressed(MouseEvent ev, EntityInstance drawRoot, int x, int y) 
	{
		LandscapeEditorCore	ls          = m_ls;
		Diagram				diagram     = ls.getDiagram();

		m_diagram      = diagram;
		m_shift_down   = ev.isShiftDown();
		m_control_down = ev.isControlDown();

//		System.out.println("FrameModeHandler mousedown drawRoot=" + drawRoot + " x=" + x + " y=" + y);

		m_start_x  = x;
		m_start_y  = y;

		m_top.setBounds(x, y, 0, 0);
		m_bottom.setBounds(x, y, 0, 0);
		m_left.setBounds(x, y, 0, 0);
		m_right.setBounds(x, y, 0, 0);

		diagram.add(m_top		/* JLayeredPane.PALETTE_LAYER */ , 0);
		diagram.add(m_bottom	/* JLayeredPane.PALETTE_LAYER */ , 0);
		diagram.add(m_left		/* JLayeredPane.PALETTE_LAYER */ , 0);
		diagram.add(m_right		/* JLayeredPane.PALETTE_LAYER */ , 0);

		m_parent.setSubHandler(this);
	}

	public void entityDragged(MouseEvent ev, EntityInstance drawRoot, int x, int y) 
	{
//		System.out.println("FrameModeHandler.entityDragged() drawRoot=" + drawRoot + " x=" + x + " y=" + y);

		int	width  = x - m_start_x;
		int	height = y - m_start_y;
		 
		if (width > 0) {
			x     = m_start_x;
		} else {
			width = -width;
		}
		if (height > 0) {
			y      = m_start_y;
		} else {
			height = -height;
		}

		m_top.setBounds(   x, y,             width, 1);
		m_bottom.setBounds(x, y + height -1, width, 1);
		m_left.setBounds(  x,             y, 1,     height);
		m_right.setBounds( x + width - 1, y, 1,     height);
	}

	public void entityReleased(MouseEvent ev, EntityInstance drawRoot, int x, int y) 
	{
		Diagram diagram = m_diagram;


		if (diagram != null) {
//			boolean				cleared = diagram.clearHighlighting(false);

			int					frameX, frameY, frameWidth, frameHeight;
			Enumeration			en;
			EntityInstance		e;
			boolean				change;

			frameX      = m_start_x;
			frameWidth  = x - frameX;
			frameY      = m_start_y;
			frameHeight = y - frameY;

			m_top.setBounds(   frameX, frameY, 0, 0);
			m_bottom.setBounds(frameX, frameY, 0, 0);
			m_left.setBounds(  frameX, frameY, 0, 0);;
			m_right.setBounds( frameX, frameY, 0, 0);

			if (frameWidth < 0) {
				frameX      = x;
				frameWidth  = -frameWidth;
			}
			if (frameHeight < 0) {
				frameY      = y;
				frameHeight = -frameHeight;
			}

			if (frameWidth == 0 || frameHeight == 0) {
				if (!m_shift_down) {
					// Added 7.1.10 -- rather dubious in my opinion
					diagram.clearGroupFlags();
				}
			} else {
				Rectangle frame   = m_frame;

				if (frame == null) {
					frame = m_frame = new Rectangle(frameX, frameY, frameWidth, frameHeight);
				} else {
					frame.setBounds(frameX, frameY, frameWidth, frameHeight);
				}

				change       = false;

				if (m_control_down) {
					// Clear everything grouped within the frame
					for (en = drawRoot.getChildrenShown(); en.hasMoreElements(); ) {
						e = (EntityInstance) en.nextElement();
						if (e.isFramedBy(frame)) {
							if (diagram.clearEntityGroupFlag(e)) {
								e.repaint();
								change = true;
					}	}	}
				} else {
					EntityInstance keyEntity    = m_diagram.getKeyEntity();
					EntityInstance newKeyEntity;

					if (m_shift_down) {
						// Adding to prior group so keep keyEntity unchanged
						newKeyEntity = keyEntity;
					} else {
						newKeyEntity = null;
						if (keyEntity != null) {
							if (keyEntity.getContainedBy() == drawRoot) {
								if (keyEntity.isFramedBy(frame)) {
									newKeyEntity = keyEntity;
						}	}	}
						diagram.clearGroupFlags();
					}

					for (en = drawRoot.getChildrenShown(); en.hasMoreElements(); ) {
						e = (EntityInstance) en.nextElement();
						if (e.isFramedBy(frame)) {
							if (!change) {
								if (newKeyEntity == null) {
									newKeyEntity = e;
								}
								change = true;
							}
							if (e == newKeyEntity) {
								if (!e.getGroupKeyFlag()) {
									diagram.setKeyEntity(e);
								}
							} else {
								if (!e.getGroupFlag()) {
									diagram.setEntityGroupFlag(e);
				}	}	}	}	}
				if (change) {
					m_ls.show_groupList();
				} else {
					frameRelations(drawRoot, frame, m_control_down);
				}
			}
/*
			if (cleared) {
				diagram.revalidate();
			}
 */
		}
		m_parent.cleanup();
	}

	public void relationPressed(MouseEvent ev, RelationInstance ri, int x, int y)
	{
		m_ls.getDiagram().setGroupAndHighlightFlag(ri);
		m_parent.cleanup();
	}
}
