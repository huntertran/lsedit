package lsedit;

// Layout method using Coffman-Graham Layer Assignment algorithm and the
// Sugiyama algorithm.

// John S. Y. Lee January 2000

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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;

public class SugiyamaLayout extends LandscapeLayouter implements ToolBarEventHandler {

	static protected final int BORDER           = 0;	
	static protected final int XGAP             = 1;
	static protected final int YGAP             = 2;

	protected final static String[] m_textfield_tags = 
							{
								"sugiyama:border",
								"sugiyama:xgap",
								"sugiyama:ygap"
							};

	protected final static String[] m_textfield_titles = 
							{
								"Border fraction of total:",
								"Horizontal whitespace fraction of grid:",
								"Vertical whitespace fraction of grid:"
							};	

	protected final static String[] m_textfield_resets = 
							{
								"0.0333",
								"0.2",
								"0.2"
							};

	protected static String[] m_textfield_defaults = 
							{
								"0.0333",
								"0.2",
								"0.2"
							};

	protected static String[] m_textfield_currents = 
							{
								"0.0333",
								"0.2",
								"0.2"
							};

	protected double	m_border = 0.0333;
	protected double	m_xgap   = 0.2;
	protected double	m_ygap   = 0.2;

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
			}
			m_textfield_currents[i] = string;
		} catch (Throwable exception) {
		}
	}

	public String getTag()
	{
		return "sugiyama:";
	}

	public void reset()
	{
		String[]	textfield_resets   = m_textfield_resets;
		String[]	textfield_defaults = m_textfield_defaults;
		String[]	textfield_currents = m_textfield_currents;
		String		string;
		int			i;

		for (i = 0; i < textfield_resets.length; ++i) {
			string                = textfield_resets[i];
			textfield_defaults[i] = string;
			textfield_currents[i] = string;
	}	}
	
	public void loadLayoutOption(int mode, String attribute, String value)
	{
		String[]	textfield_tags = m_textfield_tags;
		int		i;

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
	}

	public void saveLayoutOptions(int mode, PrintWriter ps)
	{
		String	string;
		int		i;
		String	prior_strings[];
		String	emit_strings[];

		switch (mode) {
		case 0:
			prior_strings  = m_textfield_resets;
			emit_strings   = m_textfield_defaults;
			break;
		case 1:
			prior_strings  = m_textfield_defaults;
			emit_strings   = m_textfield_currents;
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
	}	}

	class SugiyamaConfigure extends JDialog implements ActionListener {

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
						if (row >= col) {
							rectangle = boxs[i];
							g.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height); 
						}
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
		protected LayoutImage	m_layoutImage;
		protected JLabel		m_message;
		protected JButton[]		m_buttons;
		protected boolean		m_ret;

		public SugiyamaConfigure()
		{
			super(getLs().getFrame(), "Sugiyama Whitespace", true);

			Container			contentPane;
			Font				font, bold;
			JLabel				label;
			JTextField			textfield;
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
			gridLayout = new GridLayout(3, 1, 0, 10);
			labelPanel.setLayout(gridLayout);

			gridLayout = new GridLayout(3,1, 0, 10);
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
								if (dval >= 1.0) {
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


	public SugiyamaLayout(LandscapeEditorCore ls, LandscapeLayouter fallback) 
	{
		super(ls, fallback);
	}

	public String getName()
	{
		return "Sugiyama";
	}

	public String getMenuLabel() 
	{
		return "Layout using Sugiyama algorithm";
	} // getMenuLabel

	public boolean isConfigurable()
	{
		return true;
	}

	public boolean configure(LandscapeEditorCore ls)
	{
		SugiyamaConfigure	configure = new SugiyamaConfigure();
		boolean				ok        = configure.ok();
		configure.dispose();
		return ok;
	}

  // The doLayout method executes the Coffman-Graham Layer Assignment
  // algorithm and the Sugiyama algorithm on the boxes selected.
  // Assumption: All boxes selected are in the same container.

  // This is called directly to layout incoming TA

	public boolean doLayout1(Vector selectedBoxes, EntityInstance container) 
	{
		Diagram			diagram = m_ls.getDiagram();
		double			border  = m_border;
		double			xgap    = m_xgap;
		double			ygap    = m_ygap;

		Enumeration		en, edge, f;
		EntityInstance	e, child;
		int				vertexID = 0;
		int				groupSize = selectedBoxes.size();
		RelationInstance relation;
		int				index;
		int				width         = diagram.getWidth();
		int				height        = diagram.getHeight();

		// create graph to store info on selected boxes and their relationships
		Graph graph = new Graph(groupSize);

		/* Graph is an array of vertex's */

		for (en = selectedBoxes.elements(); en.hasMoreElements(); ) {
			e = (EntityInstance) en.nextElement();
			graph.addVertex(e.widthRelLocal());
			e.orMark(EntityInstance.IN_GRAPH_MARK);
		}

		// get (directed) relationships between the boxes

		for (en = selectedBoxes.elements(); en.hasMoreElements(); ++vertexID) {
			e = (EntityInstance) en.nextElement();

			// Need only look at src since will be src for one or other node in diagram

			edge = e.srcRelationElements();
			if (edge != null) {
				while (edge.hasMoreElements()) {
					relation = (RelationInstance) edge.nextElement();
					if (relation.isRelationShown()) {
						child = relation.getDst();
						if (child != e && child.isMarked(EntityInstance.IN_GRAPH_MARK)) {
							// store relationship only if children is in the same container
							for (index = 0; index < selectedBoxes.size(); index++) {
								if (child == selectedBoxes.elementAt(index)) {
									break;
								}
							} // for
							graph.addGraphEdge(vertexID, index);
						}
					}	
				} // while
			}
		} // for

		for (en = selectedBoxes.elements(); en.hasMoreElements(); ) {
			e = (EntityInstance) en.nextElement();
			e.nandMark(EntityInstance.IN_GRAPH_MARK);
		}

/*
		int index = 0;
	  
		System.out.println("Graph read:");
		for (en = selectedBoxes.elements(); en.hasMoreElements(); index++) {
			System.out.println(index + " " + en.nextElement());
		}
		graph.print();
*/

		Vector layers = graph.doCoffmanGrahamSugiyama();

/*
		System.out.println("RESULTS");
		int level = 0;
		for (en = layers.elements(); en.hasMoreElements(); ) {
			System.out.println("level " + level);
			for (Enumeration f = ((Vector)en.nextElement()).elements(); f.hasMoreElements(); ) {
				System.out.print(f.nextElement() + " ");
			}
			System.out.println();
		}
 */

		// Assign coordinates to each box

		Vector	row;
		int		row_across, max_across;

		max_across       = 0;
		for (en = layers.elements(); en.hasMoreElements(); ) {
			row        = (Vector) en.nextElement();
			row_across = row.size();
			if (row_across > max_across) {
				max_across = row_across;
		}	}

		int		available_width, available_height;
		int		row_gap, column_gap;
		double  widthrel, heightrel;

		// Compute maximum coordinates when show as draw root

		available_width  = (int) ((1.0 - border) * (double) width);
		available_height = (int) ((1.0 - border) * (double) height);

		if (max_across < 2) {
			row_gap = 0;
		} else {
			row_gap = (int) ((xgap * (double) available_width)/(max_across - 1)); 
		}

		int		numLayers   = layers.size();

		if (numLayers < 2) {
			column_gap = 0;
		} else {
			column_gap = (int) ((ygap * (double) available_height)/(numLayers - 1)); 
		}


		int		graph_width, graph_height, row_width, row_height, entity_height;

		graph_width      = 0;
		graph_height     = 0;

		for (en = layers.elements(); en.hasMoreElements(); ) {
			row = (Vector) en.nextElement();
			row_width  = 0;
			row_height = 0;
			for (f = row.elements(); f.hasMoreElements(); ) {
				int curVertex = ((Integer)f.nextElement()).intValue();

				// Process non-dummy boxes only
				if (curVertex < groupSize) {
					e          = (EntityInstance) selectedBoxes.elementAt(curVertex);
					widthrel   = e.widthRelLocal();
					heightrel  = e.heightRelLocal();

					row_width += row_gap + (int) (widthrel * (double) width);
					entity_height = (int) (heightrel * (double) height);
					if (entity_height > row_height) {
						row_height = entity_height;
					}

				} // if
			} // for
			row_width -= row_gap;	// No gap before first element

			if (row_width > graph_width) {
				graph_width = row_width;
			}
			graph_height += column_gap + row_height;
		}
		graph_height -= column_gap;	// No gap before first column

		double	scaleX, scaleY, curX, curY;

		scaleX = ((double) available_width) / ((double) graph_width);
		if (scaleX > 1.0) {
			scaleX  = 1.0;
		} 

		scaleY = ((double) available_height) / ((double) graph_height);
		if (scaleY > 1.0) {
			scaleY  = 1.0;
			//  Place the curY at the point such that graph is centred within height
			//  Divide by height to express as a ratio
			curY    = (((double) height) - ((double) graph_height))/(2.0 * ((double) height));
		} else {
			curY    = border / 2.0;
		}

		double	maxheightrel;
		
		xgap    = scaleX * ((double) row_gap)/((double) width);
		ygap    = scaleY * ((double) column_gap)/((double) height);

		for (index = layers.size(); index > 0; ) {
			row            = (Vector) layers.elementAt(--index);
			curX           = 0;
			maxheightrel   = 0;
			for (f = row.elements(); f.hasMoreElements(); ) {
				int curVertex = ((Integer)f.nextElement()).intValue();

				// Process non-dummy boxes only
				if (curVertex < groupSize) {
					e     = (EntityInstance) selectedBoxes.elementAt(curVertex);
					curX += (e.widthRelLocal()  * scaleX) + xgap;
				} // if
			} // for

			// Centre row in the horizontal plane

			curX -= xgap;
			curX  = (1.0 - curX) / 2.0;
			if (curX < 0) {
				curX = 0;
			}

			for (f = row.elements(); f.hasMoreElements(); ) {
				int curVertex = ((Integer)f.nextElement()).intValue();

				// Process non-dummy boxes only
				if (curVertex < groupSize) {
					e         = (EntityInstance) selectedBoxes.elementAt(curVertex);

					widthrel  = e.widthRelLocal()  * scaleX;
					heightrel = e.heightRelLocal() * scaleY;
					diagram.updateRelLocal(e, curX, curY, widthrel, heightrel);
					curX += widthrel + xgap;
					if (heightrel > maxheightrel) {
						maxheightrel = heightrel;
					}

				} // if
			} // for
			curY += maxheightrel + ygap;
		} // for
		return true;
	} 


  // The doLayout method executes the Coffman-Graham Layer Assignment
  // algorithm and the Sugiyama algorithm on the boxes selected.
  // Assumption: All boxes selected are in the same container.

	public String doLayout(Diagram dg) 
	{
		LandscapeEditorCore	ls = m_ls;
		EntityInstance		parent;

		ls.setLayouter(this);
		// get user's selection of boxes to be laid out

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
			return	"Sugiyama requires that all things laid out share same parent";
		}
		ls.doLayout1(this, selectedBoxes, parent, false);
		return "Graph redrawn with Coffman-Graham-Sugiyama method";

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

} // SugiyamaLayout






