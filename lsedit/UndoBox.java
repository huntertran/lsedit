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
import java.awt.Insets;
import java.awt.Rectangle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.util.Enumeration;
import java.util.Vector;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

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
import javax.swing.JViewport;
import javax.swing.undo.UndoableEdit;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public final class UndoBox extends TabBox implements ChangeListener, TaListener, ToolBarEventHandler, MouseListener, MouseMotionListener
{
	public class ShowCompoundEdit extends JDialog { 

		public class ShowCompoundEditPanel extends JComponent {
			
			Vector m_v;

			ShowCompoundEditPanel(Vector v)
			{
				m_v = v;
			}

			public void paintComponent(Graphics g)
			{
				int height = m_fontheight;
				Vector v   = m_v;

				setBackground(Color.lightGray);

				if (v != null) {
					Enumeration		en;
					UndoableEdit	undoableEdit;

					g.setFont(m_textFont);
					g.setColor(Color.BLACK);

					for (en = v.elements(); en.hasMoreElements(); ) {
						undoableEdit = (UndoableEdit) en.nextElement();
						if (undoableEdit instanceof MyPaintableUndoableEdit) {
							((MyPaintableUndoableEdit) undoableEdit).paintComponent(g, MARGIN, height);
						} else {
							g.drawString(undoableEdit.getPresentationName(), MARGIN, height+m_baseline);
						}
						height      += m_fontheight;
			}	}	}
		}

		MyCompoundEdit		m_compoundEdit;

		public ShowCompoundEdit(LandscapeEditorCore ls, UndoBox undoBox, MyCompoundEdit compoundEdit)
		{
			super(ls.getFrame(), compoundEdit.getPresentationName(), true);

			int				x, y, width, height, w;
			Enumeration		en;
			UndoableEdit	undoableEdit;
			Vector			v;
			Dimension		preferredSize = new Dimension();
			Container		contentPane;
			ShowCompoundEditPanel panel;

			x      = undoBox.m_tabbedPane.getX() + 20;
			y      = undoBox.m_tabbedPane.getY() + 20;
			setLocation(x,y);

			width  = undoBox.getWidth();
			height = undoBox.getHeight();
			m_compoundEdit = compoundEdit;
			v              = compoundEdit.getEdits();

			preferredSize.height = (v.size() * m_fontheight) + UndoBox.GAP + 20;
			preferredSize.width  = 0;
			for (en = v.elements(); en.hasMoreElements(); ) {
				undoableEdit = (UndoableEdit) en.nextElement();

				if (undoableEdit instanceof MyPaintableUndoableEdit) {
					w   = ((MyPaintableUndoableEdit) undoableEdit).getPreferredWidth();
				} else {
					FontMetrics	fm;

					fm   = getFontMetrics(UndoBox.m_textFont);
					w    = fm.stringWidth(undoableEdit.getPresentationName());
				}
				if (w > preferredSize.width) {
					preferredSize.width = w;
			}	}
			preferredSize.width  += UndoBox.GAP;
			if (preferredSize.width < width / 2) {
				preferredSize.width = width / 2;
			}
			contentPane = getContentPane();
			contentPane.setLayout(new BorderLayout());
			contentPane.setSize(preferredSize);
			panel       = new ShowCompoundEditPanel(v);
						panel.setPreferredSize(preferredSize);
			panel.setSize(preferredSize);
			contentPane.add(panel, BorderLayout.CENTER);

			pack();
			setVisible(true);
		}
	}

	public class ShowMaxUndo extends JDialog implements ActionListener {

		protected LandscapeEditorCore	m_ls;
		protected Diagram				m_diagram;
		protected UndoBox				m_undoBox;
		protected JTextField			m_max;
		protected int					m_limit;
		protected JButton				m_ok;
		protected JButton				m_cancel;
		
		public ShowMaxUndo(LandscapeEditorCore ls, UndoBox undoBox)
		{
			super(ls.getFrame(), "Maximum Undo", true);

			Container		contentPane;
			JPanel			grid;
			JPanel			panel;
			JLabel			label;
			int				x, y;
			
			m_ls          = ls;
			m_diagram     = ls.getDiagram();

			x      = undoBox.m_tabbedPane.getX() + 20;
			y      = undoBox.m_tabbedPane.getY() + 20;
			setLocation(x, y);
			setForeground(ColorCache.get(0,0,0));
			setBackground(ColorCache.get(192,192,192));
			setFont(FontCache.getDialogFont());

			contentPane = getContentPane();
			
			grid           = new JPanel();
			grid.setLayout(new GridLayout(0,1));

			panel = new JPanel();
			panel.setLayout(new FlowLayout());
			label      = new JLabel("Maximum: ");
			panel.add(label);
			m_limit = m_diagram.getLimit();
			m_max = new JTextField("" + m_limit, 10);
			panel.add(m_max);
			grid.add(panel);
			
			contentPane.add(grid, BorderLayout.CENTER);
			 
			panel = new JPanel();
			panel.setLayout(new FlowLayout());

			m_ok = new JButton("Ok");
			panel.add(m_ok);
			m_ok.addActionListener(this);
			m_cancel = new JButton("Cancel");
			panel.add(m_cancel);
			m_cancel.addActionListener(this);

			contentPane.add(panel, BorderLayout.SOUTH);

			// Resize the window to the preferred size of its components

			this.pack();
			setVisible(true);
		}

		// ActionListener interface

		public void actionPerformed(ActionEvent ev)
		{
			Object	source;

			// Pop down the window when the button is clicked.
			// System.out.println("event: " + ev);

			source = ev.getSource();

			if (source == m_ok) {
				String	max;

				max = m_max.getText();
				max = max.trim();
				if (max.length() > 0) {
					try {
						int		limit;

						limit = Integer.parseInt(max);
						if (limit >= 0) {
							m_diagram.setLimit(limit);
							m_undoBox.fill();
						}
					} catch (Throwable exception) {
				}	}
				this.setVisible(false);
				return;
			}

			if (source == m_cancel) {
				this.setVisible(false);
				return;
			}
		}
	}

	protected static final int MARGIN    = 5;
	protected static final int GAP       = 5; 

	protected static final int TY_CLEAR	 = 0;
	protected static final int TY_RAISED = 1;
	protected static final int TY_SUNK	 = 2;

	public static Dimension         m_preferredSize    = new Dimension(0,0);
	public static Dimension			m_size             = new Dimension(0,0);

	public    static final String m_helpStr	 = "This box shows the history of updates made in the current diagram.";

	protected int					m_style;
	protected int					m_over;

	protected static     Font		 m_textFont	  = null;
	protected static	 FontMetrics m_fm         = null;
	public    static     int         m_fontheight = 0;
	public    static     int         m_baseline   = 0;

	// ------------------
	// JComponent methods
	// ------------------

	public void paintComponent(Graphics g)
	{
		int	fontheight = m_fontheight;
		int	baseline   = m_baseline;
		int height     = fontheight;
		Vector v       = m_ls.getEdits();

		if (v != null) {
			Enumeration		en;
			UndoableEdit	undoableEdit;
			UndoableEdit	editToBeRedone = m_ls.getEditToBeRedone();
			String			name;
			int				at     = m_over;

			g.setFont(m_textFont);
			g.setColor(Color.BLACK);

			for (en = v.elements(); en.hasMoreElements(); --at) {
				undoableEdit = (UndoableEdit) en.nextElement();

				if (undoableEdit == editToBeRedone) {
					g.setColor(Color.RED);
				}
				if (at == 0) {
					if (m_style != MapBox.TY_CLEAR) {
						Color color;

						color = g.getColor();
						g.setColor(Color.BLACK);
						g.draw3DRect(0, height, m_preferredSize.width-1, fontheight-2, m_style == TY_RAISED);
						g.setColor(color);
				}	} 
				if (undoableEdit instanceof MyPaintableUndoableEdit) {
					((MyPaintableUndoableEdit) undoableEdit).paintComponent(g, MARGIN, height);
				} else if (undoableEdit instanceof MyCompoundEdit) {
					Color color;

					color = g.getColor();
					g.setColor(Color.WHITE);
					g.fill3DRect(0, height, m_preferredSize.width-1, fontheight-2, (at == 0 && m_style == TY_RAISED));
					g.setColor(color);
					g.drawString(undoableEdit.getPresentationName(), MARGIN, height+baseline);
				} else {
					g.drawString(undoableEdit.getPresentationName(), MARGIN, height+baseline);
				}
				height      += fontheight;
	}	}	}

	// --------------
	// Public methods 
	// --------------

	public UndoBox(LandscapeEditorCore ls, JTabbedPane tabbedPane) 
	{
		super(ls, tabbedPane, "Undo", m_helpStr);

		Font textFont = m_textFont;
		
		if (textFont == null) {
			textFont = Options.getTargetFont(Option.FONT_UNDO);
			textFontChanged(textFont);
		}
		tabbedPane.addChangeListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public static void setTextFont(Font font)
	{
		if (font != m_textFont) {
			m_textFont   = font;
			m_fm         = null;
	}	}

	public void textFontChanged(Font font)
	{
		FontMetrics fm;

		setTextFont(font);
		m_fm         = fm = getFontMetrics(font);
		m_fontheight = fm.getHeight();
		m_baseline   = m_fontheight - fm.getDescent();
		fill();
	}

	public boolean isPreferredSizeSet()
	{
		return (m_preferredSize.height > 0);
	}

	public Dimension getPreferredSize()
	{
		return m_preferredSize;
	}

	public void fill()
	{
		if (isActive()) {
			repaint();
	}	}

	public void setNewPreferredSize(Vector edits, UndoableEdit lastEdit)
	{
		int	fontheight = m_fontheight;
		int width      = GAP * 2;
		int height     = fontheight + GAP * 3;

		if (lastEdit instanceof MyPaintableUndoableEdit) {
			width   += ((MyPaintableUndoableEdit) lastEdit).getPreferredWidth();
		} else {
			FontMetrics	fm;

			fm      = getFontMetrics(m_textFont);
			width  += fm.stringWidth(lastEdit.getPresentationName());
		}
		if (width > m_preferredSize.width) {
			m_preferredSize.width = width;
		}

		height += fontheight * edits.size();
		
		if (height > m_preferredSize.height) {
			m_preferredSize.height = height;
		}

		m_scrollPane.revalidate();

		fill();
	}

	protected void mouseAt(int y)
	{
		int	over = (y / m_fontheight) - 1;
		int	max;
		int style;

		if (over < 0) {
			style = UndoBox.TY_CLEAR;
			over  = -1;
		} else if (over >= (max = m_ls.countEdits())) {
			style = UndoBox.TY_CLEAR;
			over  = max;
		} else {
			style = UndoBox.TY_RAISED;
		}
		if (style != m_style || over != m_over) {
			m_style = style;
			m_over  = over;
			repaint();
	}	}

	public void invertUndo()
	{
		Diagram diagram = m_ls.getDiagram();
		boolean	undo    = diagram.undoEnabled();
		if (undo && !m_ls.clearUndoCache()) {
			return;
		}
		diagram.setUndoEnabled(!undo);
	}
	
	public void dumpEdits() 
	{
		LandscapeEditorCore	ls = m_ls;
		String				filename;

		filename = ls.filePrompt("Save Edit History", "LSedits.txt", LandscapeEditorCore.FA_SAVE, null);
		if (filename == null || filename.length() == 0) {
			return;
		}

		try {
			File				file = new File(filename);
			FileOutputStream	os   = new FileOutputStream(file);
			PrintWriter			ps   = new PrintWriter(os);
			Vector				v    = m_ls.getEdits();
			
			ps.println("SCHEME TUPLE :");
			ps.println("SCHEME ATTRIBUTE :");
			ps.println("FACT TUPLE :");
			ps.println("FACT ATTRIBUTE :");

			if (v != null) {
				Enumeration		en;
				UndoableEdit	undoableEdit;
				UndoableEdit	editToBeRedone = m_ls.getEditToBeRedone();

				for (en = v.elements(); en.hasMoreElements(); ) {
					undoableEdit = (UndoableEdit) en.nextElement();

					if (undoableEdit == editToBeRedone) {
						break;
					}
					if (undoableEdit instanceof MyCompoundEdit) {
						Vector			v1 = ((MyCompoundEdit) undoableEdit).getEdits();
						UndoableEdit	undoableEdit1;
						Enumeration		en1;
						
						ps.println("//@Start " + undoableEdit.getPresentationName());
						for (en1 = v1.elements(); en1.hasMoreElements(); ) {
							undoableEdit1 = (UndoableEdit) en1.nextElement();
							((MyUndoableEdit) undoableEdit1).dumpEdit(ps);
						}
						ps.println("//@End " + undoableEdit.getPresentationName());
					} else {
						((MyUndoableEdit) undoableEdit).dumpEdit(ps);
					}
			}	}
			ps.close();
		} catch(IOException e) {
			System.out.println("Can't write edit history to " + filename + ": IOException " + e);
		}
	}	
	
	// ChangeListener interface

	public void stateChanged(ChangeEvent e) 
	{
//		System.out.println("UndoBox stateChanged " + isActive());
		fill();
	}

	// ToolBarEventHandler

	public boolean processMetaKeyEvent(String name)
	{
		return (m_ls.processMetaKeyEvent(name));
	}

	public void processKeyEvent(int key, int modifiers, Object object)
	{
		switch (key) {
			case Do.DESCEND:
			{
				ShowCompoundEdit dialog = new ShowCompoundEdit(m_ls, this, (MyCompoundEdit) object);
				dialog.dispose();
				break;
			}
			case Do.DELETE:
				m_ls.clearUndoCache();
				break;
			case 'C':
			case 'c':
			{
				Diagram diagram = m_ls.getDiagram();
				if (diagram != null) {
					boolean state = diagram.useCompoundEdit();
					diagram.setUseCompoundEdit(!state);
				}
				break;
			}
			case 'L':
			case 'l':
			{
				ShowMaxUndo dialog = new ShowMaxUndo(m_ls, this);
				dialog.dispose();
				break;
			}
			case 'X':
			case 'x':
			{
				invertUndo();
				break;
			}
			case 'D':
			case 'd':
			{
				dumpEdits();
				break;
	}	}	}

	public void showInfo(String msg)
	{
	}

	// TaListener interface

	public void diagramChanging(Diagram diagram)
	{
	}

	public void diagramChanged(Diagram diagram, int signal)
	{
		if (signal == DIAGRAM_CHANGED) {
			fill();
	}	}

/*
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
		int over;

		m_over  = over = (ev.getY() / m_fontheight) - 1;
		if (ev.isMetaDown()) {
			JPopupMenu	m =  new JPopupMenu("Undo options");
			Vector		v = m_ls.getEdits();
			Diagram		diagram;
			
			diagram = m_ls.getDiagram();
			if (diagram != null) {
				if (v != null) {
					int			max;

					max = v.size();
					if (max > 0) {

						if (over >= 0 && over < max) {
							UndoableEdit	undoableEdit = (UndoableEdit) v.elementAt(over);

							if (undoableEdit instanceof MyCompoundEdit) {
								MyMenuItem	m1;
						
								m1 = new MyMenuItem(m, "Show details", this, -1, Do.DESCEND, "Show the details of this edit");
								m1.setObject(undoableEdit);
							}
						} 
						new MyMenuItem(m, "Dispose undo", this, -1, Do.DELETE, "Discard undoable edits");
				}	}
				new MyMenuItem(m, "Limit", this, -1, 'l', "Set the limit on the number of undoable edits");
				if (diagram.useCompoundEdit()) {
					new MyMenuItem(m, "No Compound Edits", this, -1, (int) 'C', "Disallow compound edits");
				} else {
					new MyMenuItem(m, "Use Compound Edits", this, -1, (int) 'C', "Allow compound edits");
				}
				if (diagram.undoEnabled()) {
					new MyMenuItem(m, "Disable undo feature", this, -1, (int) 'X', "Don't incur memory cost of supporting undo/redo operations");
				} else {
					new MyMenuItem(m, "Enable undo feature", this, -1, (int) 'X', "Cache all updates so can be undone (potentially expensive)");
				}
				new MyMenuItem(m, "Dump Edit History", this, -1, (int) 'D', "Write all edits to a file for post processing");

				FontCache.setMenuTreeFont(m); 
//				Do.dump_menu(m);

				m.show(this, ev.getX(), ev.getY());
			}
			return;
		}

		m_style = MapBox.TY_SUNK;
		if (over >= 0) {
			Vector v   = m_ls.getEdits();

			if (v != null) {
				Enumeration		en;
				UndoableEdit	undoableEdit;
				UndoableEdit	editToBeRedone = m_ls.getEditToBeRedone();
				String			name;
				int				at     = m_over;
				boolean			redo   = false;

				for (en = v.elements(); en.hasMoreElements(); --at) {
					undoableEdit = (UndoableEdit) en.nextElement();
					if (undoableEdit == editToBeRedone) {
						redo = true;
					}
					if (at == 0) {
						m_ls.massChange(undoableEdit, redo);
						break;
		}	}	}	}
		repaint();
	}

	public void mouseReleased(MouseEvent ev)
	{

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
}



