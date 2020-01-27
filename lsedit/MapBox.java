package lsedit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.Scrollable;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class Map extends JComponent implements MouseListener
{
	protected MapBox				m_mapBox;
	protected int					m_style;
	protected JLabel				m_label;
	protected EntityInstance		m_entity;

	// --------------
	// Object methods
	// --------------

	public String toString()
	{
		return ("Map " + m_label.getText() + " " + getBounds());
	}

	// ------------------
	// JComponent methods
	// ------------------

	public void setBounds(int x, int y, int width, int height)
	{
		super.setBounds(x, y, width, height);
		m_label.setSize(width, 20);
	}
	
	public void paintComponent(Graphics g)
	{
		EntityInstance	parent;
		int	width, height, lheight;

//		System.out.println("Map paintComponent " + m_entity + " width=" + getWidth() + " height=" + getHeight());
		width   = getWidth();
		height  = getHeight();
		lheight = m_label.getHeight();

		if (m_style != MapBox.TY_CLEAR) {
			g.setColor(Diagram.boxColor);
			g.draw3DRect(0, 0, width-2, height-2, m_style == MapBox.TY_RAISED);
		} 

/*			// For debugging
		g.setColor(Color.red);
		g.drawLine(0, 0, width-1, height-1);
		g.drawLine(0, height-1, width-1, 0);
*/
		g.setColor(Color.black);
		g.drawLine(0, height-1, width-1, height-1);

		parent = m_entity.getContainedBy();
		if (parent != null) {
			parent.paintMap(g, (int) (width * 0.166), lheight+5, (int) (width * 0.66), height-lheight-10, m_entity, 0);
	}	}

/*
	public void invalidate()
	{
		System.out.println("Map invalidate");
		super.invalidate();
	}

	public void revalidate()
	{
		System.out.println("Map revalidate");
		super.revalidate();
	}

	public void repaint()
	{
		System.out.println("Map repaint");
		super.repaint();
	}
*/
	public void validate()
	{
		int			label_height;

//		System.out.println("Map validate " + m_label);
		label_height = m_label.getHeight();
		m_label.setBounds(0, 0, getWidth(), label_height);

		super.validate();
	}

	// --------------
	// Map methods
	// --------------

	public Map(MapBox mapBox, EntityInstance entity, Font textFont)
	{
		EntityInstance	parent;

		setLayout(null);
		
		m_mapBox = mapBox;
		m_style  = MapBox.TY_CLEAR;
		m_entity = entity;
		parent   = entity.getContainedBy();
		m_label  = new JLabel(((parent != null) ? parent.getEntityLabel() : ""), JLabel.CENTER);
		m_label.setForeground(Color.black);
		m_label.setFont(textFont);
		m_label.setLocation(0,0);
		add(m_label);
		
		setToolTipText(entity.getEntityLabel());
		addMouseListener(this);
	}

	// MouseListener interface

	public void mouseClicked(MouseEvent e)
	{
	}

	public void mouseEntered(MouseEvent e)
	{
		m_style = MapBox.TY_RAISED;
		repaint();
	}

	public void mouseExited(MouseEvent e)
	{
		m_style = MapBox.TY_CLEAR;
		repaint();
	}

	public void mousePressed(MouseEvent ev)
	{
		m_style = MapBox.TY_SUNK;
		repaint();
	}

	public void mouseReleased(MouseEvent ev)
	{
		m_style = MapBox.TY_CLEAR;
		m_mapBox.getLs().followLink(m_entity.getContainedBy(), false);
	}
}

// ================================
// ******* MAPBOX *****************
// ================================

public class MapBox extends TabBox /* extends JComponent */ implements ChangeListener, TaListener
{
	protected static final int GAP = 5; 

	protected static       Font m_textFont = null;
	 
	protected static final int TY_CLEAR	 = 0;
	protected static final int TY_RAISED = 1;
	protected static final int TY_SUNK	 = 2;

	protected EntityInstance		m_drawRoot = null;
	protected boolean				m_refill   = false;

	public    static final String m_helpStr = "This box shows stylized versions of landscapes representing the path from the top of the containment hierarchy to the current landscape, with green boxes highlighting steps in the path.";

	// ------------------
	// JComponent methods
	// ------------------

/*
	public void setBounds(int x, int y, int width, int height)
	{
		System.out.println("MapBox setBounds " + x + "," + y + "x" + width + "," + height);
		super.setBounds(x,y,width,height);
	}
*/
	private int getPreferredWidth()
	{
		Insets	insets	= m_scrollPane.getInsets();

		return m_scrollPane.getWidth() - insets.left - insets.right;
	}

	private int getPreferredHeight()
	{
		Insets	insets = m_scrollPane.getInsets();
		int		height = m_scrollPane.getHeight() - insets.top   - insets.bottom;
		int		cnt    = getComponentCount();

		if (cnt > 0) {
			int		width   = m_scrollPane.getWidth()  - insets.left  - insets.right;
			double	ratio   = getRatio();
			int		height1 = getPreferredMapHeight(width, ratio);

			height1 = (height1 + GAP) * cnt;
			if (height1 > height) {
				height = height1;
		}	}
		return height;
	}
		
	public Dimension getPreferredSize()
	{
		int		width   = getPreferredWidth();
		int		height  = getPreferredHeight();
	
		return new Dimension(width, height);
	}

	public Dimension getMaximumSize()
	{
		return(getSize());
	}

/*
    protected void paintChildren(Graphics g) 
	{
		Component[] components = getComponents();

		System.out.println("MapBox paintChildren");
		super.paintChildren(g);
	}

	public void paintComponent(Graphics g)
	{
		int	width, height;

		super.paintComponent(g);

		System.out.println("MapBox paintComponent");

		width   = getWidth();
		height  = getHeight();
		
		// For debugging
		g.setColor(Color.green);
		g.drawLine(0, 0, width, height);
		g.drawLine(0, height, width, 0);
	}

	public void invalidate()
	{
		System.out.println("MapBox invalidate");
		super.invalidate();
	}

	public void revalidate()
	{
		System.out.println("MapBox revalidate");
		super.revalidate();
	}

	public void repaint()
	{
		System.out.println("MapBox repaint");
		super.repaint();
	}
*/

	public void validate()
	{
		int			width   = getPreferredWidth();
		int			height  = getPreferredHeight();
		double		ratio   = getRatio();
		int			height1 = getPreferredMapHeight(width, ratio);

//		System.out.println("MapBox.validate " + m_drawRoot + " " + getBounds() + " height1=" + height1);

		/* This is rather nasty since setBounds will cause recursive calls to validate().
		 * Our problem is that validate is called when the size of our component changes
		 * but the very changing of the size of our component alters the size of the maps
		 * within it which logically justifies resizing of our components height
		 *
		 * But the logic must terminate since:
		 * (a) preferred width is a constant so we will avoid inequality here second time down
		 * (b) height1 is a constant when width is
		 */

		if (getWidth() != width || getHeight() != height) {
//			System.out.println("MapBox.resizing because of validate " + m_drawRoot + " from " + getBounds() + " to size " + width + "x" + height);

			// Make sure our component is large enough for its contents
			setBounds(0, 0, width, height);
		} 

		if (height1 > 0) {
			int			width1  = width - MapBox.GAP;
			int			y       = 0;
			int			cnt, i;
			Component	map;
			
			cnt = getComponentCount();
			for (i = 0; i < cnt; ++i) {
				map = getComponent(i);
				map.setBounds(MapBox.GAP, y, width1, height1);
				y  += height1 + GAP;
	}	}	}

	// --------------
	// Public methods 
	// --------------

	public MapBox(LandscapeEditorCore ls, JTabbedPane tabbedPane) 
	{
		super(ls, tabbedPane, "Map", m_helpStr);

		setBounds(0, 0, tabbedPane.getWidth(), tabbedPane.getHeight());

		tabbedPane.addChangeListener(this);
	}

	public double getRatio()
	{
		Diagram	diagram = m_ls.getDiagram();
		double	ratio;
	
		if (diagram == null) {
			ratio = 0;
		} else {
			int		diagram_width, diagram_height;

			diagram_width  = diagram.getWidth();
			if (diagram_width <= 0) {
				ratio = 0;
			} else {
				diagram_height = diagram.getHeight();
				ratio          = ((double) diagram_height) / ((double) diagram_width);
		}	}
		return(ratio);
	}	

	private int getPreferredMapHeight(int width, double ratio)
	{
		int		width1 = width - MapBox.GAP;

		if (width1 <= 0 || ratio <= 0) {
			return 0;
		}
		return (int) (ratio * ((double) width1));
	}

	private void addAll(EntityInstance e)
	{
		Font textFont = m_textFont;
		
		if (textFont == null) {
			m_textFont = textFont = Options.getTargetFont(Option.FONT_MAP);
		}

		if (e != null) {
			EntityInstance	parent = e.getContainedBy();
			if (parent != null) {
				addAll(parent);
				add(new Map(this, e, textFont));
	}	}	}

	private void fill()
	{
		removeAll();
		
		if (isActive()) {
//			System.out.println("MapBox fill " + m_drawRoot);

			addAll(m_drawRoot);
		}
		validate();
		repaint();
	}

	public void setDrawRoot(EntityInstance e)
	{
//		System.out.println("MapBox setDrawRoot " + e + " " + isActive());

		m_drawRoot = e;
		fill();
	}

	public static void setTextFont(Font font)
	{
		m_textFont = font;
	}

	public void textFontChanged(Font font)
	{
		setTextFont(font);
		fill();
	}

	// ChangeListener interface

	public void stateChanged(ChangeEvent e) 
	{
//		System.out.println("MapBox stateChanged " + isActive());
		fill();
	}

	// TaListener interface
	
	public void diagramChanging(Diagram diagram)
	{
	}

	public void diagramChanged(Diagram diagram, int signal)
	{
		setDrawRoot(diagram.getDrawRoot());
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
		m_refill = true;
	}

	public void relationClassChanged(RelationClass rc, int signal)
	{
		switch (signal) {
		case TaListener.CONTAINS_CHANGED_SIGNAL:
			m_refill = true;
			break;
		}
	}

/*
	public void entityParentChanged(EntityInstance e, EntityInstance parent, int signal)
	{
	}

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
}
