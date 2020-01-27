package lsedit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.util.Enumeration;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ClipboardBox extends TabBox /* extends JComponent */ implements ChangeListener, ToolBarEventHandler, ClipboardListener, MouseListener
{
	protected class ExpandedChkBox extends JCheckBox implements ItemListener
	{
		ExpandedChkBox(String label)
		{
			super(label);

			setSelected(false);
			addItemListener(this);
		}

		public void itemStateChanged(ItemEvent ev)
		{
			clipboardChanged(); 
	}	}

	protected static final int MARGIN    = 5;
	protected static final int INDENT    = 10;

	protected JCheckBox				m_addChk;
	protected ExpandedChkBox		m_expandChk;

	protected static Dimension      m_preferredSize    = new Dimension(0,0);

	public    static final String m_helpStr	 = "This box shows the contents of the clipboard within the current diagram.";

	protected static     Font		 m_textFont	 = null;
	protected static	 FontMetrics m_fm        = null;
	
	protected static	 int		 m_fontheight;
	protected static	 int		 m_baseline;
	protected static	 int		 m_height;
	protected static	 int		 m_top;

	// ------------------
	// JComponent methods
	// ------------------

	public Dimension getMaximumSize()
	{
		return(getPreferredSize());
	}

	public void paintComponent(Graphics g)
	{
		m_height = m_top;

		Diagram diagram = m_ls.getDiagram();
		if (diagram != null) {
			Clipboard clipboard = diagram.getClipboard();

			if (clipboard != null  && !clipboard.isEmpty()) {
				ClipboardEnumerator	en;
				EntityInstance		e;

				if (g != null) {
					g.setFont(m_textFont);
					g.setColor(Color.BLACK);
				}

				for (en = clipboard.clipboardElements(); en.hasMoreElements(); ) {
					e = (EntityInstance) en.nextElement();
					paintTree(g, e, 0);
					m_height += m_fontheight;
	}	}	}	}

	// --------------
	// Public methods 
	// --------------

	public void setTop()
	{

		FontMetrics	fm = m_fm;
		Dimension	d;
		int			y;

		if (fm == null) {
			m_fm = fm = getFontMetrics(m_textFont);
		}
		m_fontheight = fm.getHeight();
		m_baseline   = m_fontheight - fm.getDescent();
		
		y  = MARGIN;
		d  = m_addChk.getPreferredSize();
		m_addChk.setBounds(MARGIN, y, d.width, d.height);
		y += d.height;
	
		d = m_expandChk.getPreferredSize();
		m_expandChk.setBounds(MARGIN, y, d.width, d.height);
		y += d.height;

		m_top = y + m_fontheight + m_baseline;
	}

	public static void setTextFont(Font font)
	{
		if (m_textFont != font) {
			m_textFont = font;
			m_fm	   = null;
		}
	}
	
	public void setComponentsFont(Font font)
	{
		m_addChk.setFont(font);
		m_expandChk.setFont(font);
		setTop();
	}

	public void textFontChanged(Font font)
	{
		setTextFont(font);
		setComponentsFont(font);
		clipboardChanged();
	}
		
	public ClipboardBox(LandscapeEditorCore ls, JTabbedPane tabbedPane) 
	{
		super(ls, tabbedPane, "Clipboard", m_helpStr);

		if (m_textFont == null) {
			setTextFont(Options.getTargetFont(Option.FONT_CLIPBOARD));
		}
		m_addChk = new JCheckBox("Add to clipboard");
		m_addChk.setForeground(Color.black);

		m_expandChk = new ExpandedChkBox("Expand trees");
		m_expandChk.setForeground(Color.black);
		
		setComponentsFont(m_textFont);

		add(m_addChk);
		add(m_expandChk);

		tabbedPane.addChangeListener(this);
		addMouseListener(this);
		setNewPreferredSize();
	}

	private void paintTree(Graphics g, EntityInstance e, int indent)
	{
		Enumeration		children;
		EntityInstance	child, parent1;
		String			label;

		label = e.getEntityLabel();
		if (g == null) {
			FontMetrics fm = m_fm;
			// Computing sizes
			if (fm == null) {
				m_fm = fm = getFontMetrics(m_textFont);
			}
			int w = fm.stringWidth(label) + MARGIN * 2 + indent;
			if (w > m_preferredSize.width) {
				m_preferredSize.width = w;
			}
		} else {
			g.drawString(label, MARGIN+indent, m_height);
		}
		m_height += m_fontheight;

		if (m_expandChk.isSelected()) {
			for (children = e.getChildren(); children.hasMoreElements(); ) {
				child   = (EntityInstance) children.nextElement();
				parent1 = child.getContainedBy();
				if (parent1 == e) {
					paintTree(g, child, indent+INDENT);
	}	}	}	}

	protected void setNewPreferredSize()
	{
		int			oldwidth, oldheight;

		oldwidth  = ClipboardBox.m_preferredSize.width;
		oldheight = ClipboardBox.m_preferredSize.height;

		Dimension	d;

		d = m_addChk.getPreferredSize();
		m_preferredSize.width  = MARGIN + d.width;

		paintComponent(null);
		m_preferredSize.height =  m_height + m_fontheight;

		if (ClipboardBox.m_preferredSize.width != oldwidth || ClipboardBox.m_preferredSize.height != oldheight) {
			setPreferredSize(m_preferredSize);
			setSize(m_preferredSize);
			m_scrollPane.revalidate();
	}	}

	public boolean isAddToClipboard()
	{
		return m_addChk.isSelected();
	}

	public void clear()
	{
		Diagram diagram = m_ls.getDiagram();
		if (diagram != null) {
			diagram.setClipboard(null);
		}
		m_ls.doFeedback("Clipboard cleared");
	}

	// ClipboardListener interface

	public void clipboardChanged()
	{
		setNewPreferredSize();
		repaint();
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
		Diagram diagram = m_ls.getDiagram();
		if (diagram != null) {
			Clipboard clipboard = diagram.getClipboard();

			if (clipboard != null && !clipboard.isEmpty()) {
				if (ev.isMetaDown()) {
					JPopupMenu	m =  new JPopupMenu("Clipboard options");
					new MyMenuItem(m, "Dispose clipboard", this, -1, Do.DELETE, "Discard this clipboard");
					FontCache.setMenuTreeFont(m); 
					m.show(this, ev.getX(), ev.getY());
					return;
	}	}	}	}

	public void mouseReleased(MouseEvent ev)
	{
	}

	// ChangeListener interface

	public void stateChanged(ChangeEvent e) 
	{
//		System.out.println("ClipboardBox stateChanged " + isActive());
		if (isActive()) {
			repaint();
		}
	}

	// ToolBarEventHandler

	public LandscapeEditorCore getLs()
	{
		return m_ls;
	}

	public boolean processMetaKeyEvent(String name)
	{
		return m_ls.processMetaKeyEvent(name);
	}

	public void processKeyEvent(int key, int modifiers, Object object)
	{
		switch (key) {
			case Do.DELETE:
			{
				int ret = JOptionPane.showConfirmDialog(m_ls.getFrame(), "Confirm deletion?", "Delete Clipboard", JOptionPane.OK_CANCEL_OPTION);
				if (ret == JOptionPane.OK_OPTION) {
					clear();
				}
				break;
	 }	}	}

	 public void showInfo(String msg)
	 {
	 }		
}



