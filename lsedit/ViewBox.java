package lsedit;

import java.util.Vector;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class HelpViews implements ActionListener
{
	private LandscapeEditorCore	m_ls;

	public HelpViews(LandscapeEditorCore ls)
	{
		m_ls = ls;
	}

	public void actionPerformed(ActionEvent ev)
	{
		LandscapeEditorCore	ls    = m_ls;
		
		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (ls.processMetaKeyEvent("HelpViews")) {
				return;
		}	}
		
		ls.processMetaKeyEvent("views");
	}
}

class RemoveAllViews implements ActionListener
{
	private LandscapeEditorCore	m_ls;

	public RemoveAllViews(LandscapeEditorCore ls)
	{
		m_ls = ls;
	}

	public void actionPerformed(ActionEvent ev)
	{
		LandscapeEditorCore	ls    = m_ls;
		
		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (ls.processMetaKeyEvent("RemoveAllViews")) {
				return;
		}	}

		Diagram				diagram = ls.getDiagram();

		diagram.removeAllViews();
	}
}

class Add extends JButton implements ActionListener, MouseListener
{
	LandscapeEditorCore	m_ls;
	
	public Add(LandscapeEditorCore	ls)
	{
		super("[Add]");
		m_ls = ls;
//		setForeground(Color.RED);
//		setBackground(Color.BLUE);
		addActionListener(this);
		addMouseListener(this);
	}

	public void actionPerformed(ActionEvent ev)
	{
		LandscapeEditorCore ls      = m_ls;
		Diagram				diagram = ls.getDiagram();
		
		if (diagram != null) {
			ViewBox	viewBox		 = ls.getViewBox();
			View	view		 = new View();
			view.setDiagram(diagram);
			view.rename();
			view.getSnapshot(diagram);
				
			diagram.addView(view);
			viewBox.fill();
		}
	}
	
	protected void doRightPopup(MouseEvent ev)
	{
		LandscapeEditorCore ls      = m_ls;
		int					x       = ev.getX();
		int					y       = ev.getY();
		JPopupMenu			popupMenu;
		JMenuItem			mi;

		popupMenu = new JPopupMenu("Options");

		mi = new JMenuItem("Remove All");
		mi.addActionListener(new RemoveAllViews(ls));
		popupMenu.add(mi);

		mi = new JMenuItem("Help");
		mi.addActionListener(new HelpViews(ls));
		popupMenu.add(mi);
		
		FontCache.setMenuTreeFont(popupMenu);
		add(popupMenu);
		popupMenu.show(this, x, y);
		//		Do.dump_menu(popupMenu);
		remove(popupMenu);
	}
		
	// MouseListener interface

	public void mouseClicked(MouseEvent ev)
	{
	}

	public void mouseEntered(MouseEvent ev)
	{
	}

	public void mouseExited(MouseEvent ev)
	{
	}

	public void mousePressed(MouseEvent ev)
	{
		if(ev.isMetaDown()) {
			doRightPopup(ev);
	}	}
	
	public void mouseReleased(MouseEvent ev)
	{
	}
}

public class ViewBox extends TabBox /* extends JComponent */ implements ChangeListener, TaListener
{
	protected static final int MARGIN = 5;
	protected static final int GAP    = 5; 

	protected static       Font m_textFont = null;

	public    static final String m_helpStr = "This box shows available views for the diagram";
	
	public	Add			m_add = null;

	// ------------------
	// JComponent methods
	// ------------------

	public Dimension getPreferredSize()
	{
		return(getSize());
	}

	public Dimension getMaximumSize()
	{
		return(getSize());
	}
	
	// --------------
	// Public methods 
	// --------------

	public ViewBox(LandscapeEditorCore ls, JTabbedPane tabbedPane) 
	{
		super(ls, tabbedPane, "Views", m_helpStr);

		setBounds(0, 0, tabbedPane.getWidth(), tabbedPane.getHeight());
		
		m_add   = new Add(ls);

		tabbedPane.addChangeListener(this);
	}
	
	public void validate()
	{
		int			cnt     = getComponentCount();
		int			width1  = getWidth() - MARGIN;
		int			depth   = 0;
		int			i, height1;
		Component	c;
		Dimension	d;
		
		for (i = 0; i < cnt; ++i) {
			c = getComponent(i);
			d = c.getPreferredSize();
			height1 = d.height;
			c.setBounds(MARGIN, depth, width1, height1);
			depth += height1 + GAP;
	}	}
	
	public void fill()
	{
		Insets	insets	= m_scrollPane.getInsets();
		int		width   = m_scrollPane.getWidth() - insets.left - insets.right;
		int		height  = m_scrollPane.getHeight()- insets.top  - insets.bottom;
		
		removeAll();

		if (isActive()) {
			LandscapeEditorCore	ls      = getLs();
			Diagram				diagram = ls.getDiagram();
			
			if (diagram != null) {
				int			width1, width2, height1;
				Component	c;
				Dimension	d;
		
				Font		textFont = m_textFont;
	
				if (textFont == null) {
					m_textFont = textFont = Options.getTargetFont(Option.FONT_HISTORY);
				}
				
				m_add.setFont(textFont);
				d        = m_add.getPreferredSize();
				width2   = d.width;
				height1  = d.height + GAP;
				add(m_add);

				Vector		views    = diagram.getViews();

				if (views != null) {

					int		i, size;
					View	view;
					
					size = views.size();
					for (i = 0; i < size; ++i) {
						view = (View) views.elementAt(i);
						view.setFont(textFont);
						d    = view.getPreferredSize();
						width1 = d.width;
						if (width1 < width2) {
							width2 = width1;
						}
						height1 += d.height + GAP;
						view.setFont(textFont);
						add(view);
				}	}
				width2 += MARGIN;
				if (width < width2) {
					width = width2;
		}	}	}

		if (getWidth() != width || getHeight() != height) {
			setBounds(0, 0, width, height);
		} 							
		validate();
		repaint();
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
		fill();
	}
	
	// TaListener interface
	
	public void diagramChanging(Diagram diagram)
	{
	}

	public void diagramChanged(Diagram diagram, int signal)
	{
		if (signal == TaListener.DIAGRAM_CHANGED) {
			fill();
	}	}

	public void updateBegins()
	{
	}

	public void updateEnds()
	{
	}

	public void entityClassChanged(EntityClass ec, int signal)
	{
	}

	public void relationClassChanged(RelationClass rc, int signal)
	{
	}
}
