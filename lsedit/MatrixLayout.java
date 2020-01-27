package lsedit;

import java.util.Enumeration;
import java.util.Vector;

import java.io.PrintWriter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;

public class MatrixLayout extends LandscapeLayouter  implements ToolBarEventHandler {

	static protected final int BORDER           = 0;	
	static protected final int XGAP             = 1;
	static protected final int YGAP             = 2;
	static protected final int FITFRACTION      = 3;

	protected final static String[] m_textfield_tags = 
							{
								"matrix:border",
								"matrix:xgap",
								"matrix:ygap",
								"matrix:fit"
							};

	protected final static String[] m_textfield_titles = 
							{
								"Border fraction of total:",
								"Horizontal whitespace fraction of grid:",
								"Vertical whitespace fraction of grid:",
								"Fraction of entities that fit width"
							};					

	protected final static String[] m_textfield_resets = 
							{
								"0.0333",
								"0.2",
								"0.2",
								"1.0"
							};

	protected static String[] m_textfield_defaults = 
							{
								"0.0333",
								"0.2",
								"0.2",
								"1.0"
							};

	protected static String[] m_textfield_currents = 
							{
								"0.0333",
								"0.2",
								"0.2",
								"1.0"
							};

	protected double	m_border = 0.0333;
	protected double	m_xgap   = 0.2;
	protected double	m_ygap   = 0.2;
	protected double	m_fit    = 1.0;

	static protected final int DIRECTION = 0;

	protected final static String[] m_checkbox_tags = 
							{
								"matrix:direction"
							};

	protected final static String[] m_checkbox_titles = 
							{
								 "Order vertically"
							};

	protected final static boolean[] m_checkbox_resets = 
							{
								true
							};

	protected static boolean[] m_checkbox_defaults = 
							{
								true
							};

	protected static boolean[] m_checkbox_currents = 
							{
								true
							};

	static protected final int BUTTON_OK      = 0;
	static protected final int BUTTON_CANCEL  = 1;
	static protected final int BUTTON_HELP    = 2;
	static protected final int BUTTON_DEFAULT = 3;
	static protected final int BUTTON_SET     = 4;
	static protected final int BUTTON_RESET   = 5;

	protected final static String[] m_button_titles =
							{
								"Ok",
								"Cancel",
								"Help",
								"Default",
								"Set",
								"Reset"
							};

	protected final static String[] m_button_tips =
							{
								null,
								null,
								null,
								"Use remembered default",
								"Set default to current",
								"Set default to initial"
							};

	protected static boolean parameterBoolean(int i)
	{
		return m_checkbox_currents[i];
	}

	protected void setParameter(int i, String string)
	{
		string = string.trim();

		try {
			double dval = Double.parseDouble(string);
			switch (i) {
			case BORDER:
				m_border = dval;
				break;
			case XGAP:
				m_xgap = dval;
				break;
			case YGAP:
				m_ygap = dval;
				break;
			case FITFRACTION:
				m_fit  = dval;
				break;
			}
			m_textfield_currents[i] = string;
		} catch (Throwable exception) {
		}
	}

	public String getTag()
	{
		return "matrix:";
	}

	public void reset()
	{
		String[]	textfield_resets   = m_textfield_resets;
		String[]	textfield_defaults = m_textfield_defaults;
		String[]	textfield_currents = m_textfield_currents;
		boolean[]	checkbox_resets    = m_checkbox_resets;
		boolean[]	checkbox_defaults  = m_checkbox_defaults;
		boolean[]	checkbox_currents  = m_checkbox_currents;
		String		string;
		boolean		bool;
		int			i;

		for (i = 0; i < textfield_resets.length; ++i) {
			string                = textfield_resets[i];
			textfield_defaults[i] = string;
			textfield_currents[i] = string;
		}
		for (i = 0; i < checkbox_resets.length; ++i) {
			bool                  = checkbox_resets[i];
			checkbox_defaults[i]  = bool;
			checkbox_currents[i]  = bool;
	}	}
	
	public void loadLayoutOption(int mode, String attribute, String value)
	{
		String[]	textfield_tags, checkbox_tags;
		int			i;

		textfield_tags = m_textfield_tags;
		for (i = 0; i < textfield_tags.length; ++i) {
			if (attribute.equals(textfield_tags[i])) {
				switch (mode) {
				case 0:
					m_textfield_defaults[i] = value;
				case 1:
					setParameter(i, value);
				}
				return;
		}	}
		checkbox_tags = m_checkbox_tags;
		for (i = 0; i < checkbox_tags.length; ++i) {
			if (attribute.equals(checkbox_tags[i])) {
				boolean bool   = ((value.charAt(0) == 't') ? true : false);
				switch (mode) {
				case 0:
					m_checkbox_defaults[i] = bool;
				case 1:
					m_checkbox_currents[i] = bool;
				}
				return;
		}	}
	}

	public void saveLayoutOptions(int mode, PrintWriter ps)
	{
		String	string;
		int		i;
		String	prior_strings[];
		String	emit_strings[];
		boolean prior_booleans[];
		boolean emit_booleans[];
		boolean	bool;

		switch (mode) {
		case 0:
			prior_strings  = m_textfield_resets;
			prior_booleans = m_checkbox_resets;
			emit_strings   = m_textfield_defaults;
			emit_booleans  = m_checkbox_defaults;
			break;
		case 1:
			prior_strings  = m_textfield_defaults;
			prior_booleans = m_checkbox_defaults;
			emit_strings   = m_textfield_currents;
			emit_booleans  = m_checkbox_currents;
			break;
		default:
			return;
		}

		for (i = 0; i < m_textfield_tags.length; ++i) {
			string = emit_strings[i];
			if (string.equals(prior_strings[i])) {
				continue;
			}
			ps.println(m_textfield_tags[i] + "=" + string);
		}

		for (i = 0; i < m_checkbox_tags.length; ++i) {
			bool = emit_booleans[i];
			if (bool == prior_booleans[i]) {
				continue;
			}
			ps.println(m_checkbox_tags[i] + "=" + (bool ? "true" : "false"));
	}	}

	class MatrixConfigure extends JDialog implements ActionListener {

		class LayoutImage extends JComponent implements MouseListener
		{
			Rectangle[] m_boxs;
			int			m_rows = 2;
			boolean		m_validated;


			public LayoutImage()
			{
				super();

				Dimension	dimension = new Dimension(400,240);
				int			i;

				setLayout(null);
				setPreferredSize(dimension);
				setMinimumSize(dimension);
				setMaximumSize(dimension);
				setSize(dimension);

				m_boxs      = new Rectangle[25];
				m_validated = false;
				addMouseListener(this);
				setVisible(true);
			}
			
			public void validate()
			{
				Rectangle[]	boxs       = m_boxs;
				int			cnt        = boxs.length;
				int			dimension  = m_rows;;				
				int			width      = getWidth();
				int			height     = getHeight();
				double		new_border = m_border;
				double		new_xgap   = m_xgap;
				double		new_ygap   = m_ygap;

				int			x, y, w, h, gapw, gaph, w1, h1, row, col, i, gaps;
				double		dgaps;
				Rectangle	rectangle;

				x      = (int) (((double) width)  * new_border / 2.0);
				y      = (int) (((double) height) * new_border / 2.0);
				w      = width  - 2 * x;
				h      = height - 2 * y;

				m_validated = false;

				if (w < 1 || h < 1) {
					return;
				}

				gaps      = dimension - 1;
				dgaps     = (double) gaps;
				gapw      = (int) ((new_xgap * ((double) w))/dgaps); // Size of the gaps
				gaph      = (int) ((new_ygap * ((double) h))/dgaps);
				w1        = (w - (gapw * gaps)) / dimension;
				h1        = (h - (gaph * gaps)) / dimension;
					
				if (w1 < 1 || h1 < 1) {
					return;
				}

				row = col = 0;
				cnt = dimension * dimension;
				for (i = 0; i < cnt; ++i) {
					rectangle = (Rectangle) boxs[i];
					if (rectangle == null) {
						boxs[i] = rectangle = new Rectangle();
					}
					rectangle.x = x;
					if (col != 0) {
						rectangle.x += col*(w1 + gapw);
					}
					rectangle.y = y;
					if (row != 0) {
						rectangle.y += row*(h1 + gaph);
					}
					rectangle.width  = w1;
					rectangle.height = h1;
					if (++col == dimension) {
						++row;
						col = 0;
				}	}

				m_validated = true;
			}

			
			public void paintComponent(Graphics g)
			{
				int	dimension, w, h, row, col, cnt;

				w = getWidth();
				h = getHeight();

				g.setColor(Color.black);
				g.drawRect(0, 0, w-1, h-1);

				if (m_validated) {
					Rectangle[]	boxs = m_boxs;
					Rectangle	rectangle;
					int			i;

					g.setColor(Color.blue);
					dimension = m_rows;
					row = col = 0;
					cnt = dimension * dimension;
					for (i = 0; i < cnt; ++i) {
						rectangle = boxs[i];
						g.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height); 
						if (++col == dimension) {
							++row;
							col = 0;
					}	}
					if (dimension == 2) {
						String		s     = "Click to change dimensions";
						FontMetrics fm    = g.getFontMetrics();
						int			width = fm.stringWidth(s);

						g.setColor(Color.black);
						g.drawString(s, (w - width)/2, h/2);
				}	}
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
				if (ev.isMetaDown()) {
					--m_rows;
					if (m_rows < 2) {
						m_rows = 5;
					}
				} else {
					++m_rows;
					if (m_rows > 5) {
						m_rows = 2;
				}	}
				validate();
				repaint();
			}

			public void mouseReleased(MouseEvent ev)
			{
			}
		}

		protected JTextField[]	m_textfields;
		protected JCheckBox[]	m_checkboxes;
		protected LayoutImage	m_layoutImage;
		protected JLabel		m_message;
		protected JButton[]		m_buttons;
		protected boolean		m_ret;

		public MatrixConfigure()
		{
			super(getLs().getFrame(), "Matrix Whitespace", true);

			Container			contentPane;
			Font				font, bold;
			JLabel				label;
			JTextField			textfield;
			JCheckBox			checkbox;
			JButton				button;
			String				tip;
			int					i;

			m_ret    = false;

			font   = FontCache.getDialogFont();
			bold   = font.deriveFont(Font.BOLD);

			setForeground(ColorCache.get(0,0,0));
			setBackground(ColorCache.get(192,192,192));
			setFont(font);

			JPanel topPanel    = new JPanel();
			JPanel labelPanel  = new JPanel();
			JPanel valuePanel  = new JPanel();

			GridLayout gridLayout;

			topPanel.setLayout( new BorderLayout() );
			gridLayout = new GridLayout(5, 1, 0, 10);
			labelPanel.setLayout(gridLayout);

			gridLayout = new GridLayout(5, 1, 0, 10);
			valuePanel.setLayout(gridLayout);


			m_textfields = new JTextField[m_textfield_tags.length];
			for (i = 0; i < m_textfield_tags.length; ++i) {

				label = new JLabel(m_textfield_titles[i], JLabel.RIGHT);
				label.setFont(bold);
				labelPanel.add(label);
				m_textfields[i] = textfield = new JTextField(m_textfield_currents[i],  20);
				textfield.addActionListener(this);
				textfield.setFont(font);
				valuePanel.add(textfield);
			}

			m_checkboxes = new JCheckBox[m_checkbox_tags.length];
			for (i = 0; i < m_checkbox_tags.length; ++i) {
				label = new JLabel(m_checkbox_titles[i], JLabel.RIGHT);
				label.setFont(bold);
				labelPanel.add(label);
				m_checkboxes[i] = checkbox = new JCheckBox("", m_checkbox_currents[i]);
				checkbox.setFont(font);
				valuePanel.add(checkbox);
			}

			topPanel.add( BorderLayout.WEST,   labelPanel);
			topPanel.add( BorderLayout.EAST,   valuePanel);

			contentPane = getContentPane();
			
			contentPane.add( BorderLayout.NORTH, topPanel );

			JPanel centerPanel = new JPanel();
			centerPanel.setLayout( new BorderLayout() );


			m_layoutImage = new LayoutImage();
			centerPanel.add( BorderLayout.NORTH, m_layoutImage);
			m_layoutImage.validate();

			m_message = new JLabel(" ", JLabel.CENTER);
			m_message.setFont(font);
			m_message.setForeground(Color.RED);
			m_message.setSize(400,50);
			m_message.setPreferredSize(new Dimension(400,50));
			centerPanel.add( BorderLayout.SOUTH, m_message);

			contentPane.add( BorderLayout.CENTER, centerPanel);

			// --------------
			// Use a FlowLayout to center the button and give it margins.

			JPanel bottomPanel = new JPanel();

			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));

			m_buttons = new JButton[m_button_titles.length];
			for (i = 0; i < m_button_titles.length; ++i) {
				m_buttons[i] = button = new JButton(m_button_titles[i]);
				button.setFont(bold);
				tip = m_button_tips[i];
				if (tip != null) {
					button.setToolTipText(tip);
				}
				button.addActionListener(this);
				bottomPanel.add(button);
			}

			contentPane.add( BorderLayout.SOUTH, bottomPanel);

			// Resize the window to the preferred size of its components
			pack();
			setVisible(true);
		}

		public boolean ok()
		{
			return m_ret;
		}

		// ActionListener interface

		public void actionPerformed(ActionEvent ev)
		{
			Object		source;
			JTextField	textfield;
			String		string, name;
			int			state, i;

			source = ev.getSource();

			state = -1;
			for (i = 0; i < m_button_titles.length; ++i) {
				if (source == m_buttons[i]) {
					state = i;
					break;
			}	}

			switch (state) {
			case BUTTON_RESET:
				for (i = 0; i < m_textfield_tags.length; ++i) {
					m_textfield_defaults[i] = m_textfield_resets[i];
				}
			case BUTTON_DEFAULT:

				for (i = 0; i < m_textfield_tags.length; ++i) {
					m_textfields[i].setText(m_textfield_defaults[i]);
				}
				break;
			case BUTTON_SET:
				for (i = 0; i < m_textfield_tags.length; ++i) {
					m_textfield_defaults[i] = m_textfields[i].getText();
				}
				break;
			case BUTTON_HELP:
				JOptionPane.showMessageDialog(m_ls.getFrame(), 	
				  "Border\n" +
				  "  Fraction of diagram to reserve for border\n" +
				  "  Must be a value between 0 and 1\n" +
				  "Gap\n" +
				  "  Fraction of diagram to reserve for whitespace"
				    , "Help", JOptionPane.OK_OPTION);
				return;
			default:

				for (i = 0; i < m_textfield_tags.length; ++i) {
					textfield = m_textfields[i];
					if (source == textfield || state == BUTTON_OK) {
						string = textfield.getText();
						string = string.trim();
						name   = m_textfield_titles[i];
						switch (i) {
						case FITFRACTION:
						case BORDER:
						case XGAP:
						case YGAP:
							try {
								double dval;

								dval = Double.parseDouble(string);
								if (dval < 0) {
									m_message.setText(name + " may not be negative");
									return;
								}
								if (dval > 1.0) {
									m_message.setText(name + " must not be greater than 1.0");
									return;
								} 
								if (dval == 1.0 && i != FITFRACTION) {
									m_message.setText(name + " must be less than 1.0");
									return;
								}
							} catch (Throwable exception) {
								m_message.setText(name + " not a double precision value");
								return;
			}	}	}	}	}

			switch (state) {
			case BUTTON_OK:
				for (i = 0; i < m_textfield_tags.length; ++i) {
					setParameter(i, m_textfields[i].getText());
				}
				for (i = 0; i < m_checkbox_tags.length; ++i) {
					m_checkbox_currents[i] = m_checkboxes[i].isSelected();
				}
				m_ret = true;
			case BUTTON_CANCEL:
				break;
			default:
				m_layoutImage.validate();
				m_layoutImage.repaint();
				return;
			}

			setVisible(false);
			return;
		}
	}


	public MatrixLayout(LandscapeEditorCore ls, LandscapeLayouter fallback) 
	{
		super(ls, fallback);
	}

	public String getName()
	{
		return "Matrix";
	}

	public String getMenuLabel() 
	{
		return "Layout in a matrix";
	} 

	public boolean isConfigurable()
	{
		return true;
	}

	public boolean configure(LandscapeEditorCore ls)
	{
		MatrixConfigure configure = new MatrixConfigure();
		boolean         ok        = configure.ok();
		configure.dispose();
		return ok;
	}

	// The doLayout method executes the Matrix algorithm
	// Assumption: All boxes selected are in the same container.

	public boolean doLayout1(Vector selectedBoxes, EntityInstance container) 
	{
		Diagram			diagram  = m_ls.getDiagram();
		double			border   = m_border;
		double			xgap     = m_xgap;
		double			ygap     = m_ygap;
		boolean			vertical = parameterBoolean(DIRECTION);

		int				size     = selectedBoxes.size();
		int				badwidth = (int) (((double) size) * (1 - m_fit));
		int				rows, columns, row, col, i, gaps, actualWidth, minWidth, cnt;
		EntityInstance	e, parent, parent1;
		double			xmarginrel, ymarginrel, xrel, yrel, widthrel, heightrel, dval, gapw, gaph, parentWidth;
		Graphics		graphics;

		if (border < 0 || xgap < 0 || ygap < 0) {
			m_ls.error("MatrixLayout: some parameters -ve");
			return false;
		}

		if (xgap >= 1.0 || ygap >= 1.0 || border >= 1.0) {
			m_ls.error("MatrixLayout: no parameter may have a value 1.0");
			return false;
		}

		xmarginrel = border / 2.0;
		ymarginrel = border / 2.0;

		switch (size) {
		case 0:
			return true;
		case 1:
			rows      = 1;
			break;
		default:
			rows      = (int) (Math.ceil(Math.sqrt(size)));
		}

		columns = rows;

		parent = null;
		for (i = 0; i < size; ++i) {
			e = (EntityInstance) selectedBoxes.elementAt(i);
			parent1 = e.getContainedBy();
			if (parent1 == null) {
				System.out.println("MatrixLayout: Entity " + e + " has no parent");
			} else if (parent == null) {
				parent = parent1;
			} else if (parent != parent1) {
				System.out.println("MatrixLayout: Entity " + e + " has different parent from other entities being laid out");
		}	}
		if (parent == null) {
			return false;
		}

		parentWidth = (double) parent.getWidth();
		graphics    = m_ls.getGraphics();
				 
		for (; ; ) {

			if (rows == 1) {
				gaph      = 0;
				heightrel = 1.0 - border;
			} else {
				heightrel = (1.0 - border) * (1.0 - ygap) / (double) rows;
				gaph      = ((1.0 - border) - (heightrel * (double) rows))/(double) (rows-1);
			}

			if (columns == 1) {
				gapw      = 0;
				widthrel  = 1.0 - border;
				break;
			} 
			widthrel = (1.0 - border) * (1.0 - xgap) / (double) columns;
			gapw     = ((1.0 - border) - (widthrel * (double) columns))/(double) (columns-1);

			if (parentWidth <= 0) {
				// Simply layout square
				break;
			}

			cnt = 0;
			for (i = 0; i < size; ++i) {
				e = (EntityInstance) selectedBoxes.elementAt(i);
				minWidth    = e.getMinFitWidth(graphics);
				actualWidth = (int) (widthrel * parentWidth);
				if (actualWidth < minWidth) {
					if (++cnt > badwidth) {
						break;
			}	}	}
			if (i == size) {
				break;
			}
			--columns;
			while (rows * columns < size) {
				++rows;
		}	}

		SortVector.byString(selectedBoxes, true /* ascending */);
			
		row = col = 0;
		for (i = 0; i < size; ++i) {
			e = (EntityInstance) selectedBoxes.elementAt(i);
			xrel = xmarginrel;
			if (col != 0) {
				xrel += ((double) col) * (widthrel + gapw);
			}
			yrel = ymarginrel;
			if (row != 0) {
				yrel += ((double) row)*(heightrel + gaph);
			}
			diagram.updateRelLocal(e, xrel, yrel, widthrel, heightrel);
			if (vertical) {
				if (++row == rows) {
					++col;
					row = 0;
				}
			} else {
				if (++col == columns) {
					++row;
					col = 0;
		}	}	}

//		System.out.println("Matrix done");
		return true;

	} // doLayout


  // The doLayout method executes the Coffman-Graham Layer Assignment
  // algorithm and the Sugiyama algorithm on the boxes selected.
  // Assumption: All boxes selected are in the same container.

	public String doLayout(Diagram dg) 
	{
		LandscapeEditorCore	ls = m_ls;
		EntityInstance		parent;

		// get user's selection of boxes to be laid out

		ls.setLayouter(this);

		Vector selectedBoxes = dg.getClusterGroup();
		if (selectedBoxes == null) {
			  Util.beep();
			  return "No group selected";
		}

		String msg = allInDiagram(selectedBoxes);
		if (msg != null) {
			return msg;
		}

		parent = parentOfSet(selectedBoxes);
		if (parent == null) {
			return	"Matrix layouter requires that all things laid out share same parent";
		}
		ls.doLayout1(this, selectedBoxes, parent, false);
		return "Graph redrawn using Matrix Layout";
	} // doLayout

	public void processKeyEvent(int key, int modifiers, Object object) 
	{
		Diagram	dg;
		String	rmsg;

/*
		if (!configure(m_ls)) {
			return;
		}
*/
		dg = m_ls.getDiagram();
		if (dg != null) {
			rmsg = doLayout(dg);
			m_ls.doFeedback(rmsg);
	}	}
} 





