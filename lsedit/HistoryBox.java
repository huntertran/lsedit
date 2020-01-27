package lsedit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public final class HistoryBox extends TabBox /* extends JComponent */ implements ChangeListener, TaListener, ToolBarEventHandler, MouseListener, MouseMotionListener
{
	protected static final int MARGIN    = 5;
	protected static final int GAP       = 5; 

	protected static final int TY_CLEAR	 = 0;
	protected static final int TY_RAISED = 1;
	protected static final int TY_SUNK	 = 2;

	public static Dimension         m_preferredSize    = new Dimension(0,0);
	public static int				m_disable_history  = 0;

	public    static final String m_helpStr	 = "This box shows the history of navigation within the current diagram.";

	protected static     Font		 m_textFont	 = null;
	protected static	 FontMetrics m_fm        = null;
	protected static	 int		 m_fontheight;
	protected static	 int		 m_baseline;

	protected Vector		m_history = new Vector(1000);
	protected int			m_style;
	protected int			m_over    = -1;		// What the mouse is over
	protected int			m_at      = -1;		// What we are logically at (-1 means no current position)
	protected EntityInstance m_hover  = null;

	protected boolean		m_refill  = false;

	// ------------------
	// JComponent methods
	// ------------------

/*
	public Dimension getPreferredSize()
	{
		Dimension d = super.getPreferredSize();

		System.out.println("HistoryBox.getPreferredSize " + d);
		return d;
	}

	public Dimension getSize()
	{
		Dimension d = super.getSize();

		System.out.println("HistoryBox.getSize " + d);
		return d;
	}
*/
	public Dimension getMaximumSize()
	{
		return(getPreferredSize());
	}

	public void paintComponent(Graphics g)
	{
		Vector v   = m_history;

		if (v != null) {
			int				fontheight = m_fontheight;
			int				baseline   = m_baseline;
			int				height     = fontheight;
			int				at	       = m_at;
			String			label;

			Enumeration		en;
			int				over = m_over;
			int				i;
			EntityInstance	e;

			g.setFont(m_textFont);

			for (i = m_history.size(); i > 0; ) {
				e = (EntityInstance) m_history.elementAt(--i);
				if (e.isMarked(EntityInstance.DELETED_MARK)) {
					g.setColor(Color.BLACK);
				} else {
					if (i == over) {
						if (m_style != MapBox.TY_CLEAR) {
							Color color;

							color = g.getColor();
							g.setColor(Color.BLACK);
							g.draw3DRect(0, height, m_preferredSize.width-1, fontheight-2, m_style == TY_RAISED);
							g.setColor(color);
					}	} 
					if (i == at) {
						g.setColor(Color.RED);
					} else {
						g.setColor(Color.BLUE);
				}	}
				label = e.getEntityLabel();
				g.drawString(label, MARGIN, height+baseline);
				height      += fontheight;
	}	}	}

	// ------------------
	// HistoryBox methods 
	// ------------------

	public HistoryBox(LandscapeEditorCore ls, JTabbedPane tabbedPane) 
	{
		super(ls, tabbedPane, "History", m_helpStr);

		if (m_textFont == null) {
			m_textFont = Options.getTargetFont(Option.FONT_HISTORY);
		}
		tabbedPane.addChangeListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void fill()
	{
		if (isActive()) {
			repaint();
	}	}

	protected void navigateTo(EntityInstance e)
	{
		++m_disable_history;
		m_ls.navigateTo(e, false);
		--m_disable_history;
		fill();
	}

	public void clear()
	{
		m_history.removeAllElements();
		m_at   = -1;
		m_over = -1;
		m_ls.setHistoryButtons(HistoryBox.this);
		fill();
	}

	public static void setTextFont(Font font)
	{
		if (font != m_textFont) {
			m_textFont = font;
			m_fm       = null;
	}	}

	public void textFontChanged(Font font)
	{
		setTextFont(font);
		fill();
	}

	public void setNewPreferredSize()
	{
//		System.out.println("HistoryBox.setNewPreferredSize " + m_preferredSize);
		setPreferredSize(m_preferredSize);
		setSize(m_preferredSize);
		m_scrollPane.revalidate();
	}

	public void addEntity(EntityInstance e)
	{
		int			w, h;
		boolean		ret;
		Vector		v;
		FontMetrics	fm;
		Vector		history = m_history;

		if (m_disable_history == 0) {
			m_at = history.size();
			history.addElement(e);

			if ((fm = m_fm) == null) {
				m_fm = fm    = getFontMetrics(m_textFont);
				m_fontheight = fm.getHeight();
				m_baseline   = m_fontheight - fm.getDescent();
			}
			w    = fm.stringWidth(e.getEntityLabel())       + HistoryBox.GAP * 2;
			h    = history.size() * HistoryBox.m_fontheight + HistoryBox.GAP * 3;
			
			if (w > HistoryBox.m_preferredSize.width || h > HistoryBox.m_preferredSize.height) {
				if (w > HistoryBox.m_preferredSize.width) {
					HistoryBox.m_preferredSize.width  = w;
				}
				if (h > HistoryBox.m_preferredSize.height) {
					HistoryBox.m_preferredSize.height = h;
				}
				setNewPreferredSize();
			}
			fill();
	}	}

	public boolean hasPrevious()
	{

		if (m_history.size() > 1) {
			int	at;

			at = m_at;
			if (at == -1) {
				at = m_history.size() - 1;
			}
			for (; --at >= 0; ) {
				if (m_history.elementAt(at) instanceof EntityInstance) {
					return true;
		}	}	}
		return false;
	}

	public void navigatePrevious()
	{
		if (m_history.size() > 1) {
			int		at;
			Object	object;

			at = m_at;
			if (at == -1) {
				at = m_history.size() - 1;
			}
			for (; --at >= 0; ) {
				object = m_history.elementAt(at);
				if (object instanceof EntityInstance) {
					m_over = -1;
					m_at   = at;
					navigateTo((EntityInstance) object);
					return;
	}	}	}	}

	public boolean hasNext()
	{
		int	at = m_at;

		if (at >= 0) {
			for (; ++at < m_history.size(); ) {
					if (m_history.elementAt(at) instanceof EntityInstance) {
					return true;
		}	}	}
		return false;
	}

	public void navigateNext()
	{
		int	at = m_at;

		if (at >= 0) {
			Object object;

			for (; ++at < m_history.size(); ) {
				object = m_history.elementAt(at);
				if (object instanceof EntityInstance) {
					m_over = -1;
					m_at   = at;
					navigateTo((EntityInstance) object);
					return;
	}	}	}	}

	protected void mouseAt(int y)
	{
		int	size = m_history.size();
		if (size > 0) {
			int	over = size - (y / m_fontheight);
			int style;

			if (over < 0) {
				style = HistoryBox.TY_CLEAR;
				over  = -1;
			} else if (over >= size) {
				style = HistoryBox.TY_CLEAR;
				over  = size;
			} else {
				style = HistoryBox.TY_RAISED;
			}
			if (style != m_style || over != m_over) {
				m_style = style;
				m_over  = over;
				repaint();
	}	}	}

	// TaListener interface

	public void diagramChanging(Diagram diagram)
	{
		clear();
	}

	public void diagramChanged(Diagram diagram, int signal)
	{
	}

	public void updateBegins()
	{
	}

	public void updateEnds()
	{
		if (m_refill) {
			m_refill = false;
			fill();
	}	}

	public void entityClassChanged(EntityClass ec, int signal)
	{
	}

	public void relationClassChanged(RelationClass rc, int signal)
	{
	}

	public void entityParentChanged(EntityInstance e, EntityInstance parent, int signal)
	{
		switch (signal) {
		case TaListener.ENTITY_CUT_SIGNAL:
		case TaListener.ENTITY_PASTED_SIGNAL:
		case TaListener.CONTAINER_CUT_SIGNAL:
		case TaListener.CONTAINER_PASTED_SIGNAL:
			// Needed because something deleted may be repasted
			repaint();		
			break;
		}
	}

/*
	public void relationParentChanged(RelationInstance ri, int signal)
	{
	}

	public void entityInstanceChanged(EntityInstance e, int signal)
	{
	}

	public void relationInstanceChanged(RelationInstance ri, int signal)
	{
	}
 */
	// ChangeListener interface

	public void stateChanged(ChangeEvent e) 
	{
//		System.out.println("HistoryBox stateChanged " + isActive());
		if (isActive()) {
			repaint();
		}
	}

	// MouseListener interface

	public void mouseClicked(MouseEvent ev)
	{
	}

	public void mouseEntered(MouseEvent ev)
	{
		mouseAt(ev.getY());
	}

	public void mouseExited(MouseEvent ev)
	{
		m_style = MapBox.TY_CLEAR;
		repaint();
	}

	public void mousePressed(MouseEvent ev)
	{
		int		size    = m_history.size();

		if (size > 0) {
			Vector	history = m_history;
			int		over;

			if (ev.isMetaDown()) {
				JPopupMenu	m =  new JPopupMenu("History options");
				new MyMenuItem(m, "Dispose History", this, -1, Do.DELETE, "Discard the history");
				FontCache.setMenuTreeFont(m); 
				m.show(this, ev.getX(), ev.getY());
				return;
			}

			m_over  = over = size - (ev.getY() / m_fontheight);
			m_style = MapBox.TY_SUNK;
			if (over >= 0 && over < size) {
				EntityInstance	e = (EntityInstance) history.elementAt(over);
				m_at = over;
				if (!e.isMarked(EntityInstance.DELETED_MARK)) {
					if (ev.isAltDown()) {
						m_hover = e;
						e.startHover();
					} else {
						navigateTo(e);
			}	}	}
			repaint();
	}	}

	public void mouseReleased(MouseEvent ev)
	{
		if (m_hover != null) {
			m_hover.endHover();
			m_hover = null;
		}
		m_style = MapBox.TY_CLEAR;
		repaint();
	}

	// MouseMotionListener interface

	public void mouseDragged(MouseEvent ev)
	{
		mouseAt(ev.getY());

	}

	public void mouseMoved(MouseEvent ev)
	{
		mouseAt(ev.getY());
	}

	// ToolBarEventHandler

	public boolean processMetaKeyEvent(String name)
	{
		return (m_ls.processMetaKeyEvent(name));
	}

	public void processKeyEvent(int key, int modifiers, Object object)
	{
		switch (key) {
			case Do.DELETE:
			{
				int ret = JOptionPane.showConfirmDialog(m_ls.getFrame(), "Confirm deletion?", "Delete History", JOptionPane.OK_CANCEL_OPTION);
				if (ret == JOptionPane.OK_OPTION) {
					clear();
				}
				break;
	}	}	}

	public void showInfo(String msg)
	{
	}		
}



