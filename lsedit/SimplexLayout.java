package lsedit;

import java.util.Enumeration;
import java.util.Vector;

import java.io.PrintWriter;

import java.awt.Cursor;
import java.awt.FontMetrics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JLabel;

/** 
 *   Developed by: Ian Davis in Summer 1997 for Grant Weddell
 *   Class which hides all of the various display options which may be passed (as a structure)
 *   to Layouter.   
 */

public class SimplexLayout extends LandscapeLayouter  implements ToolBarEventHandler {

	static protected final int XGAP             = 0;
	static protected final int YGAP             = 1;
	static protected final int SIMPLEX          = 2;
	static protected final int CROSSINGS        = 3;
	static protected final int BORDER           = 4;

	protected final static String[] m_textfield_tags = 
							{
								"simplex:xgap",
								"simplex:ygap",
								"simplex:simplex",
								"simplex:crossings",
								"simplex:border"
							};

	protected final static String[] m_textfield_titles = 
							{
								"Minimum horizontal gap between nodes:",
								"Minimum vertical gap between nodes:",
								"Maximum allowed simplex pivots:",
								"Maximum reorderings of a row:",
								"Percentage for border"
							};

	protected final static String[] m_textfield_resets = 
							{
								"20",
								"20",
								"10000",
								"125",
								"0.08"
							};

	protected static String[] m_textfield_defaults = 
							{
								"20",
								"20",
								"10000",
								"125",
								"0.08"
							};

	protected static String[] m_textfield_currents = 
							{
								"20",
								"20",
								"10000",
								"125",
								"0.08"
							};

	private static int			m_xgap      = 20;					// specify horizontal gap between nodes
	private static int			m_ygap      = 20;					// specify vertical gap between nodes
	private static int     		m_simplex   = 10000;				// specify the maximum number of simplex pivots
	private static int			m_crossings = 125;					// specify the maximum number of crossing cycles
	private static double		m_border    = 0.08;
	private static RelationClass m_designated = null;
	private static boolean		m_lisp      = false;

	static protected final int FIXEDRATIO       = 0;

	protected final static String[] m_checkbox_tags = 
							{
								"simplex:fixedratio"
							};

	protected final static String[] m_checkbox_titles = 
							{
								 "Scale using fixed ratio"
							};

	protected final static boolean[] m_checkbox_resets = 
							{
								false
							};

	protected static boolean[] m_checkbox_defaults = 
							{
								false
							};

	protected static boolean[] m_checkbox_currents = 
							{
								false
							};

	static protected final int FIXEDORDER       = 0;

	protected final static String[] m_combobox_tags = 
							{
								"simplex:fixedorder"
							};

	protected final static String[] m_combobox_titles = 
							{
								 "Horizontal order by"
							};

	protected final static String[] g_preserveOrdering =
							{
								"edge crossings",
								"hierarchical order",
								"src edge order"
							};

	protected final static String[][] m_combobox_entries =
							{
								g_preserveOrdering
							};

	protected final static int[] m_combobox_resets = 
							{
								0
							};

	protected static int[] m_combobox_defaults = 
							{
								0
							};

	protected static int[] m_combobox_currents = 
							{
								0
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

	public final static int		horizMargin    = 2;			// horizontal margin for the graph
	public final static int		vertMargin     = 2;			// vertical margin for graph

	/* Global display parameters */

	private static int			m_weight_1dummy = 0;		// Weight of edge with one dummy node in it (2 if showing such nodes)
	private static int			m_weight_2dummy = 0;		// Weight of edge with two dummy nodes in it (8 if showing such nodes)
	private static int			m_outside_bias  = 1;		// Bias for outside edges in layout
	private static int			m_group_children = 0;		// Group children above/below higraphs
	private static boolean		m_fixedRatio;;

	/* Other */

	private	static boolean		m_bends    = false;			// True if will be using the bends in arcs

	String	m_message;

	protected static boolean parameterBoolean(int i)
	{
		return m_checkbox_currents[i];
	}

	protected static int	 parameterCombo(int i)
	{
		return m_combobox_currents[i];
	}

	protected void setParameter(int i, String string)
	{
		string = string.trim();

		try {
			switch (i) {
				case BORDER:
				{
					double dval = Double.parseDouble(string);
					m_border = dval;
					break;
				}
				default:
				{
					int ival = Integer.parseInt(string);
					switch (i) {
					case XGAP:
						m_xgap      = ival;
						break;
					case YGAP:
						m_ygap      = ival;
						break;
					case SIMPLEX:
						m_simplex   = ival;
						break;
					case CROSSINGS:
						m_crossings = ival;
						break;
					}
					m_textfield_currents[i] = string;
					break;
			}	}
		} catch (Throwable exception) {
		}
	}

	public String getTag()
	{
		return "simplex:";
	}

	public void reset()
	{
		String[]	textfield_resets   = m_textfield_resets;
		String[]	textfield_defaults = m_textfield_defaults;
		String[]	textfield_currents = m_textfield_currents;
		boolean[]	checkbox_resets    = m_checkbox_resets;
		boolean[]	checkbox_defaults  = m_checkbox_defaults;
		boolean[]	checkbox_currents  = m_checkbox_currents;
		int[]		combo_resets       = m_combobox_resets;
		int[]		combo_defaults     = m_combobox_defaults;
		int[]		combo_currents     = m_combobox_currents;
		String		string;
		boolean		bool;
		int			i, combo;

		for (i = 0; i < textfield_resets.length; ++i) {
			string                = textfield_resets[i];
			textfield_defaults[i] = string;
			textfield_currents[i] = string;
		}
		
		for (i = 0; i < checkbox_resets.length; ++i) {
			bool                  = checkbox_resets[i];
			checkbox_defaults[i]  = bool;
			checkbox_currents[i]  = bool;
		}
	
		for (i = 0; i < combo_resets.length; ++i) {
			combo             = combo_resets[i];
			combo_defaults[i] = combo;
			combo_currents[i] = combo;
	}	}
		
	public void loadLayoutOption(int mode, String attribute, String value)
	{
		String[]	textfield_tags, checkbox_tags, combobox_tags;
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
				boolean bool = ((value.charAt(0) == 't') ? true : false);
				switch (mode) {
				case 0:
					m_checkbox_defaults[i] = bool;
				case 1:
					m_checkbox_currents[i] = bool;
				}
				return;
		}	}
		
		combobox_tags = m_combobox_tags;
		for (i = 0; i < combobox_tags.length; ++i) {
			if (attribute.equals(combobox_tags[i])) {
				int val = value.charAt(0)  - '0';
				if (val >= 0 && val <= 9) {
					switch (mode) {
					case 0:
						m_combobox_defaults[i] = val;
					case 1:
						m_combobox_currents[i] = val;
				}	}
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
		int		prior_combos[];
		int		emit_combos[];
		boolean	bool;
		int		val;

		switch (mode) {
		case 0:
			prior_strings  = m_textfield_resets;
			prior_booleans = m_checkbox_resets;
			prior_combos   = m_combobox_resets;
			emit_strings   = m_textfield_defaults;
			emit_booleans  = m_checkbox_defaults;
			emit_combos    = m_combobox_defaults;
			break;
		case 1:
			prior_strings  = m_textfield_defaults;
			prior_booleans = m_checkbox_defaults;
			prior_combos   = m_combobox_defaults;
			emit_strings   = m_textfield_currents;
			emit_booleans  = m_checkbox_currents;
			emit_combos    = m_combobox_currents;
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
		}
		for (i = 0; i < m_combobox_tags.length; ++i) {
			val = emit_combos[i];
			if (val == prior_combos[i]) {
				continue;
			}
			ps.println(m_combobox_tags[i] + "=" + val);
	}	}

	public static void lispSemantics()
	{
		m_lisp = true;
	}

	class SimplexConfigure extends JDialog implements ActionListener {

		protected JTextField[]	m_textfields;
		protected JCheckBox[]	m_checkboxes;
		protected JComboBox[]	m_comboboxes;
		protected JComboBox		m_designatedClass;
		protected JButton[]		m_buttons;
		protected JLabel		m_message;
		protected boolean		m_ret;
		
		public SimplexConfigure(LandscapeEditorCore ls)
		{
			super(ls.getFrame(), "Set Simplex Algorithm parameters", true);

			Container			contentPane;
			Font				font, bold;
			JLabel				label;
			JTextField			textfield;
			JCheckBox			checkbox;
			JComboBox			combobox;
			JButton				button;
			String				tip;
			Diagram				diagram;
			int					i;

			m_ret  = false;
			font   = FontCache.getDialogFont();
			bold   = font.deriveFont(Font.BOLD);

			setForeground(ColorCache.get(0,0,0));
			setBackground(ColorCache.get(192,192,192));
			setFont(font);

			JPanel topPanel    = new JPanel();
			JPanel leftPanel   = new JPanel();
			JPanel rightPanel  = new JPanel();

			GridLayout gridLayout;

			topPanel.setLayout( new BorderLayout() );
			gridLayout = new GridLayout(8,1);
			gridLayout.setVgap(10);
			leftPanel.setLayout(gridLayout);

			gridLayout = new GridLayout(8,1);
			gridLayout.setVgap(10);
			rightPanel.setLayout( gridLayout);

			m_textfields = new JTextField[m_textfield_tags.length];

			for (i = 0; i < m_textfield_tags.length; ++i) {

				label = new JLabel(m_textfield_titles[i], JLabel.RIGHT);
				label.setFont(bold);
				leftPanel.add(label);
				m_textfields[i] = textfield = new JTextField(m_textfield_currents[i],  20);
				textfield.addActionListener(this);
				textfield.setFont(font);
				rightPanel.add(textfield);
			}

			m_checkboxes = new JCheckBox[m_checkbox_tags.length];
			for (i = 0; i < m_checkbox_tags.length; ++i) {
				label = new JLabel(m_checkbox_titles[i], JLabel.RIGHT);
				label.setFont(bold);
				leftPanel.add(label);
				m_checkboxes[i] = checkbox = new JCheckBox("", m_checkbox_currents[i]);
				checkbox.setFont(font);
				rightPanel.add(checkbox);
			}

			m_comboboxes = new JComboBox[m_combobox_tags.length];
			for (i = 0; i < m_combobox_tags.length; ++i) {
				label = new JLabel(m_combobox_titles[i], JLabel.RIGHT);
				label.setFont(bold);
				leftPanel.add(label);
				m_comboboxes[i] = combobox = new JComboBox(m_combobox_entries[i]);
				combobox.setSelectedIndex(m_combobox_currents[i]);
				combobox.setFont(font);
				rightPanel.add(combobox);
			}

			diagram = ls.getDiagram();
			if (diagram == null) {
				m_designatedClass = null;
			} else {
				Enumeration		en;
				RelationClass	rc;
				String			id;
				int				selected;

				m_designatedClass = new JComboBox();
				label             = new JLabel("Horizontal class", JLabel.RIGHT);
				label.setFont(bold);
				leftPanel.add(label);
				m_designatedClass.setFont(font);
				selected = 0;

				m_designatedClass.addItem("");
				i = 0;
				for (en = diagram.enumRelationClassesInOrder(); en.hasMoreElements(); ) {
					rc = (RelationClass) en.nextElement();
					id = rc.getId();
					m_designatedClass.addItem(rc);
					++i;
					if (m_lisp && id.equalsIgnoreCase("cdr")) {
						m_designated = rc;
					}
					if (rc == m_designated) {
						selected = i;
				}	}
				m_lisp = false;
				m_designatedClass.setSelectedIndex(selected);
				rightPanel.add(m_designatedClass);
			}
		
			topPanel.add( BorderLayout.WEST, leftPanel);
			topPanel.add( BorderLayout.EAST, rightPanel);

			contentPane = getContentPane();
			
			contentPane.add( BorderLayout.NORTH, topPanel );

			m_message = new JLabel(" ", JLabel.CENTER);
			m_message.setFont(font);
			m_message.setForeground(Color.RED);

			m_message.setSize(400,50);
			contentPane.add( BorderLayout.CENTER, m_message);

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
			Object			source;
			JTextField		textfield;
			String			string;
			String			name;
			int				state, i;

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
				for (i = 0; i < m_checkbox_tags.length; ++i) {
					m_checkbox_defaults[i]  = m_checkbox_resets[i];
				}
				for (i = 0; i < m_combobox_tags.length; ++i) {
					m_combobox_defaults[i]  = m_combobox_resets[i];
				}
			case BUTTON_DEFAULT:

				for (i = 0; i < m_textfield_tags.length; ++i) {
					m_textfields[i].setText(m_textfield_defaults[i]);
				}
				for (i = 0; i < m_checkbox_tags.length; ++i) {
					m_checkboxes[i].setSelected(m_checkbox_defaults[i]);
				}
				for (i = 0; i < m_combobox_tags.length; ++i) {
					m_comboboxes[i].setSelectedIndex(m_combobox_defaults[i]);
				}
				return;
			case BUTTON_SET:
				for (i = 0; i < m_textfield_tags.length; ++i) {
					m_textfield_defaults[i] = m_textfields[i].getText();
				}
				for (i = 0; i < m_checkbox_tags.length; ++i) {
					m_checkbox_defaults[i] = m_checkboxes[i].isSelected();
				}
				for (i = 0; i < m_combobox_tags.length; ++i) {
					m_combobox_defaults[i] = m_comboboxes[i].getSelectedIndex();
				}
				return;
			case BUTTON_HELP:
				JOptionPane.showMessageDialog(m_ls.getFrame(), 	
				  "Gap\n" +
				  "  Minimum number of pixels between entities\n" +
				  "Border\n" +
				  "  Fraction of diagram to reserve for border\n" +
				  "  Must be a value between 0 and 1\n" +
				  "Pivots\n" +
				  "  Maximum number of simplex pivots to perform\n" +
				  "Reorderings\n" +
				  "  Maximumn number of horizontal reorderings" 
				 	  , "Help", JOptionPane.OK_OPTION);
				return;
			}

			for (i = 0; i < m_textfield_tags.length; ++i) {
				textfield = m_textfields[i];
				if (source == textfield || state == BUTTON_OK) {
					string = textfield.getText();
					string = string.trim();
					name   = m_textfield_titles[i];
					switch (i) {
					case XGAP:
					case YGAP:
					case SIMPLEX:
					case CROSSINGS:
						try {
							int ivalue;

							ivalue = Integer.parseInt(string);
							if (ivalue < 0) {
								m_message.setText(name + " may not be negative");
								return;
							}
						} catch (Throwable exception) {
							m_message.setText(name + " not an integer value");
							return;
						}
						break;
					case BORDER:
						try {
							double dval;

							dval = Double.parseDouble(string);
							if (dval < 0.0 || dval >= 1.0) {
								m_message.setText(name + " must be in the range 0 to 1.0");
								return;
							}

						} catch (Throwable exception) {
							m_message.setText(name + " not a double precision value");
							return;
			}	}	}	}

			switch (state) {
			case BUTTON_OK:
				for (i = 0; i < m_textfield_tags.length; ++i) {
					setParameter(i, m_textfields[i].getText());
				}
				for (i = 0; i < m_checkbox_tags.length; ++i) {
					m_checkbox_currents[i] = m_checkboxes[i].isSelected();
				}
				for (i = 0; i < m_combobox_tags.length; ++i) {
					m_combobox_currents[i] = m_comboboxes[i].getSelectedIndex();
				}

				if (m_designatedClass == null || (m_designatedClass.getSelectedIndex() <= 0)) {
					m_designated = null;
				} else {
					m_designated = (RelationClass) m_designatedClass.getSelectedItem();
				}
				m_ret = true;
			case BUTTON_CANCEL:
				break;
			default:
				return;
			}

			setVisible(false);
			return;
		}
	}

	public static void setBends(boolean value)
	{
		if (value) {
			m_weight_1dummy = 2;
			m_weight_2dummy = 8;
		} else {
			m_weight_1dummy = 0;
			m_weight_2dummy = 0;
		}
		m_bends = value;
	}
	
	public static boolean hasBends()
	{
		return m_bends;
	}

	public static int	xgap() 
	{
		return (m_xgap);
	}

	public static boolean xgap(int value) 
	{
		if (value >= 1) {
			m_xgap = value;
			return(true);
		}
		return(false);
	}

	public static int ygap() 
	{
		return (m_ygap);
	}

	public static boolean ygap(int value) 
	{
		if (value >= 1) {
			m_ygap = value;
			return(true);
		}
		return(false);
	}

	public static int	simplex() 
	{
		return(m_simplex);
	}

	public static boolean simplex(int value) 
	{
		if (value >= 0) {
			m_simplex = value;
			return(true);
		}
		return(false);
	}

	public static int crossing() 
	{
		return(m_crossings);
	}

	public static boolean crossing(int value) 
	{
		if (value >= 0) {
			m_crossings = value;
			return(true);
		}
		return(false);
	}

	public static double border()
	{
		return m_border;
	}

	public static void border(double value)
	{
		m_border = value;
	}

	public static boolean fixedRatio()
	{
		return m_fixedRatio;
	}

	public static void fixedRatio(boolean value)
	{
		m_fixedRatio = value;
	}

	public static int weight_1dummy() 
	{
		return(m_weight_1dummy);
	}

	public static boolean weight_1dummy(int value) 
	{
		if (value >= 1) {
			m_weight_1dummy = value;
			return(true);
		}
		return(false);
	}

	public static int weight_2dummy() 
	{
		return(m_weight_2dummy);
	}

	public static boolean weight_2dummy(int value) 
	{
		if (value >= 1) {
			m_weight_2dummy = value;
			return(true);
		}
		return(false);
	}

	public static int outside_bias() 
	{
		return(m_outside_bias);
	}

	public static boolean outside_bias(int value) 
	{
		if (value >= 0) {
			m_outside_bias = value;
			return(true);
		}
		return(false);
	}

	public static int group_children() 
	{
		return(m_group_children);
	}

	public static boolean group_children(int value) 
	{
		if (value >= -1 && value <= 1) {
			m_group_children = value;
			return(true);
		}
		return(false);
	}

	public static int xmargin() {
		return (horizMargin);
	}

	public static int ymargin() {
		return (vertMargin);
	}

	public static RelationClass getDesignatedClass()
	{
		return(m_designated);
	}
	

	// --- Main processing logic

	// Once the highgraph has been laid out this may be called separately if the fonts change

	public boolean coordinates(HiGraph root) 
	{
		try {
//			System.out.println("Laying out");
			HiGraphCoordinates.coordinates(this, root);
//			System.out.println("Layout done");
			return(true);
		}
		catch (HiGraphException e) {
			System.out.println("HiGraph error in HiGraphCoordinates: " + e.getMessage());
			e.printStackTrace();
		}
		catch (Exception e) {
			System.out.println("Error during HiGraph layout in HiGraphCoordinates: " + e.getMessage());
			e.printStackTrace();
		}
		return(false);
	}

	protected void orderByEdges(HiGraph root)
	{
		boolean			seen = false;

		Vector			children, children1, out, srcRelList;
		HiArc			arc;
		HiGraph			node, to;
		EntityInstance	e, e1, e2;
		RelationInstance ri;
		int				i, position, childrenSize, outSize, listSize, j, k, phase;
		boolean			reorder;

		/* Begin by ordering the nodes in the graph in a separate vector by increasing order of fanout */

		children     = root.children();
		childrenSize = children.size();
		for (i = 0; i < childrenSize; ++i) {
			arc             = (HiArc) children.elementAt(i);
			node            = arc.to();	// A node in the graph
			out             = node.out();
			node.m_position = out.size();;
		}

		children1 = (Vector) children.clone();
		SortVector.byPosition(children1);

		/* Now assign all nodes in a graph a position order consistent with their current position order */

		position = 0;
		for (i = 0; i < childrenSize; ++i) {
			arc             = (HiArc) children.elementAt(i);
			node            = arc.to();	// A node in the graph
			node.m_position = position;
			position       += 1024;
		}

		/* For nodes having multiple nodes connected to them at a lower rank, for which an edge ordering
		   makes sense - make the position the earliest such edges position + 1 + edge position in vector
		   Assume that this value fits into 10 bits (mod 1024)
		 */

		for (i = childrenSize; --i >= 0; ) {
			// For each node in the graph visiting those with the largest fanouts first

			arc           = (HiArc) children1.elementAt(i);
			node          = arc.to();
			out           = node.out();
			outSize       = out.size();
			if (outSize < 2) {
				break;
			}			
			e             = node.getReferencedObject();
			if (e == null) {
				continue;
			}
			srcRelList    = e.getSrcRelList();
			if (srcRelList == null) {
				continue;
			}
			listSize      = srcRelList.size();
			if (listSize < 2) {
				continue;
			}

			position = -1;
			for (phase = 0; ; ++phase) {
				reorder = false;
				for (j = 0; j < outSize; ++j) {
					arc       = (HiArc) out.elementAt(j);
					to        = arc.to();	// Something that node addresses in the graph
					if (to.m_rank <= node.m_rank) {
						continue;
					}
					if ((to.m_position & 1023) != 0) {
						// Already moved this node
						continue;
					}
					e1 = to.getReferencedObject();
					if (e1 == null) {
						continue;
					}
					for (k = listSize; --k >= 0; ) {
						ri = (RelationInstance) srcRelList.elementAt(k);
						if (!ri.isMarked(RelationInstance.SPANNING_MARK)) {
							e2 = ri.getDst();
							if (e1.hasDescendantOrSelf(e2)) {
								break;
					}	}	}
					if (k < 0) {
						continue;
					}
					if (phase == 0) {
						if (position < 0) {
							position = to.m_position;
							continue;
						}
						// Have at least two that are below me that can be moved
						reorder = true;
						break;
					} else {
						to.m_position = position | ((k + 1) & 1023);
						seen = true;
				}	}
				if (!reorder) {
					break;
		}	}	}

		if (seen) {
/*
			System.out.println("Reordering by edges");
			for (i = childrenSize; --i >= 0; ) {
				// For each node in the graph visiting those with the largest fanouts first

				arc           = (HiArc) children1.elementAt(i);
				node          = arc.to();
				out           = node.out();
				outSize       = out.size();
				if (outSize < 2) {
					break;
				}			
				e             = node.getReferencedObject();
				if (e == null) {
					continue;
				}
				System.out.println("  " + e + " rank=" + node.m_rank);
				for (j = 0; j < outSize; ++j) {
					arc       = (HiArc) out.elementAt(j);
					to        = arc.to();	// Something that node addresses in the graph
					e1 = to.getReferencedObject();
					if (e1 == null) {
						continue;
					}
					position = to.m_position;
					System.out.println("    " + e1 + " rank=" + to.m_rank + " position=" + position + "[" +(position >> 10) + "/" + (position & 1023) + "]");
			}	}
 */
			SortVector.byPosition(children);
		}
	}

	public boolean layout(HiGraph root)
	{
		boolean ret;
		int fixedOrder = parameterCombo(FIXEDORDER);

		try {
			
//			System.out.println("SimplexLayout: beginning group");
			HiGroup.group(root, group_children());

			// Compute an optimal vertical ranking 
//			System.out.println("SimplexLayout: beginning ranking");
			HiRank.compute(root, simplex(), getDesignatedClass());

			switch (fixedOrder) {
			case 0:
				// Compute a good horizontal ordering for higraphs based on edges between higraphs
//				System.out.println("SimplexLayout: beginning child ordering");
				HiChildren.order(root, outside_bias());
//				System.out.println("SimplexLayout: beginning row ordering");
				HiRow.order(this, root);
				// System.out.println("After interchange\n");
				// root.dump();
				break;
			case 2:
				orderByEdges(root);
				break;
			}

//			System.out.println("SimplexLayout: beginning coordinates");
			ret = coordinates(root);
//			System.out.println("SimplexLayout: success");
			return(ret);
		}
		catch (HiGraphException e) {
			System.out.println("HiGraph error in " + root + ": " + e.getMessage());
			e.printStackTrace();
		}
		catch (Exception e) {
			System.out.println("Error during HiGraph layout in " + root + ": " + e.getMessage());
			e.printStackTrace();
		}
/*
		catch (OutOfMemoryError e) {
			System.out.println("Out of memory error during simplex layout in " + root + ": " + e.getMessage());
			e.printStackTrace();
		}
*/
		return(false);
	}

	public SimplexLayout(LandscapeEditorCore ls, LandscapeLayouter fallback) 
	{
		super(ls, fallback);
	}

/****************************************************************************/

	public String getName()
	{
		return "Simplex";
	}

	public String getMenuLabel() 
	{
		return "Layout using Simplex algorithm";
	} 

/***************************************************************************/

	/* Returns the number of edges from things at/below from to things at/below to
	 * While seemingly somewhat inefficient this may be more efficient than lifting
	 * edges twice. It has the advantage of being quick when edges are found
	 */

	protected static int edgesBetween(EntityInstance from, EntityInstance to)
	{
		Vector				srcList = from.getSrcLiftedList();
		Enumeration			en;
		RelationInstance	ri;
		EntityInstance		e;
		int					i, ret;

		ret = 0;
		if (srcList != null) {
			for (i = srcList.size(); --i >= 0; ) {
				ri = (RelationInstance) srcList.elementAt(i);
				// Consider only visible edges when drawing layout
				if (ri.isRelationShown()) {
					e = ri.getDrawDst();
					if (to.hasDescendantOrSelf(e)) {
						++ret;
				}	}
		}	}
		
		if (from.isOpen()) {
			for (en = from.getChildrenShown(); en.hasMoreElements(); ) {
				e = (EntityInstance) en.nextElement();
				ret += edgesBetween(e, to);
		}	}

//		System.out.println("SimplexLayout: Testing for edge from " + from + (from.isOpen() ? "[open]" : "[notopen]") + " to at/under " + to + "=" + ret);
		return(ret);
	}

	protected static int edgesFromClient(EntityInstance to, boolean visibleEdges)
	{
		Enumeration			en = to.dstLiftedRelationElements();
		RelationInstance	ri;
		RelationClass		rc;
		EntityInstance		e;
		int					ret;

		ret = 0;
		if (en != null) {
			while (en.hasMoreElements()) {
				ri = (RelationInstance) en.nextElement();
				if (visibleEdges) {
					rc = ri.getRelationClass();
					if (!rc.isShown()) {
						continue;
				}	}
				e        = ri.getDrawSrc();
				if (e.isMarked(EntityInstance.CLIENT_MARK)) {
					++ret;
		}	}	}

		if (to.isOpen()) {
			for (en = to.getChildrenShown(); en.hasMoreElements(); ) {
				e = (EntityInstance) en.nextElement();
				ret += edgesFromClient(e, visibleEdges);
		}	}
		return(ret);
	}

	protected static int edgesToSupplier(EntityInstance from, boolean visibleEdges)
	{
		Enumeration			en = from.srcLiftedRelationElements();
		RelationInstance	ri;
		RelationClass		rc;
		EntityInstance		e;
		int					ret;

		ret = 0;
		if (en != null) {
			while (en.hasMoreElements()) {
				ri = (RelationInstance) en.nextElement();
				if (visibleEdges) {
					rc = ri.getRelationClass();
					if (!rc.isShown()) {
						continue;
				}	}
				e        = ri.getDrawDst();
				if (e.isMarked(EntityInstance.SUPPLIER_MARK)) {
					++ret;
		}	}	}

		if (from.isOpen()) {
			for (en = from.getChildrenShown(); en.hasMoreElements(); ) {
				e = (EntityInstance) en.nextElement();
				ret += edgesToSupplier(e, visibleEdges);
		}	}
		return(ret);
	}

	public boolean isConfigurable()
	{
		return true;
	}

	public boolean configure(LandscapeEditorCore ls)
	{
		SimplexConfigure configure = new SimplexConfigure(ls);
		boolean			 ok        = configure.ok();
		configure.dispose();
		return ok;
	}

	// The doLayout method executes the Network Simplex algorithm
	// Assumption: All boxes selected are in the same container.

	public boolean doLayout1(Vector selectedBoxes, EntityInstance container) 
	{
		Option				option     = Options.getDiagramOptions();
		LandscapeEditorCore	ls         = m_ls;
		Diagram				diagram    = ls.getDiagram();
		int					fixedOrder = parameterCombo(FIXEDORDER);

		HiGraph				root, from, to, client, supplier;
		HiArc				arc1, arc2;
		Vector				children;
		Vector				unconnected;
		Enumeration			en, en1;
		EntityInstance		e, other;
		double				border;
		Graphics			graphics;
		int					i;
		boolean				visibleEdges    = option.isVisibleEdges();

		int		width, height, max_width, fixed, fit;
		int		weight;
		int		min_x,  max_x,  min_y,  max_y;
		int		min_x1, max_x1, min_y1, max_y1;
		double	xrel, yrel, widthrel, heightrel;
		double	scaleWidth, scaleHeight, shiftX, shiftY;
		double	graph_width, graph_height, graph_x, graph_y;
		double	avg_relheight, d;

		boolean	some_src_edges = false;
		boolean some_dst_edges = false;

//		System.out.println("Simplex started on " + container + "(" + selectedBoxes.size() + ")" + ": " + selectedBoxes);

		m_fixedRatio = parameterBoolean(FIXEDRATIO);

		border = border();

		switch (selectedBoxes.size()) {
		case 0:
			return true;
		case 1:
			e = (EntityInstance) selectedBoxes.firstElement();
			widthrel  = 1.0 - border;
			heightrel = 1.0 - border;
			xrel      = (1 - widthrel )/2;
			yrel      = (1 - heightrel)/2;

			diagram.updateRelLocal(e, xrel, yrel, widthrel, heightrel);
			return true;
		}
		
		if (m_lisp) {
			RelationClass	rc;
			String			id;
				
			for (en = diagram.enumRelationClassesInOrder(); en.hasMoreElements(); ) {
				rc = (RelationClass) en.nextElement();
				id = rc.getId();
				if (id.equalsIgnoreCase("cdr")) {
					m_designated = rc;
			}	}
			m_lisp = false;
		}
		
		graphics        = ls.getGraphics();
		width           = diagram.getWidth();
		height          = diagram.getHeight();

		// Simplex algorithm assumes all boxes same height

		avg_relheight = 0;
		max_width     = 0;
		for (en = selectedBoxes.elements(); en.hasMoreElements(); ) {
			e              = (EntityInstance) en.nextElement();
			avg_relheight += e.heightRelLocal();
			fit            = e.getMinFitWidth(graphics);
			if (fit > max_width) {
				max_width = fit;
		}	}
		avg_relheight /= selectedBoxes.size();

		// create graph to store info on selected boxes and their relationships
		root     = new HiGraph(null, "root", 0, height);

				 
		for (en = selectedBoxes.elements(); en.hasMoreElements(); ) {
			e = (EntityInstance) en.nextElement();
			// N.B. don't use getWidth() since may not yet be known
			root.newChild(e, e.getEntityLabel(), max_width, (int) (avg_relheight * height));
		} 

		children  = root.children();


		// get (directed) relationships between the boxes

		for (en = children.elements(); en.hasMoreElements(); ) {
			arc1        = (HiArc) en.nextElement();
			from        = arc1.to();
			e           = from.getReferencedObject();
			for (en1 = children.elements(); en1.hasMoreElements(); ) {
				arc2    = (HiArc) en1.nextElement();
				if (arc2 == arc1) {
					continue;
				}
				to      = arc2.to();
				other   = to.getReferencedObject();
				weight  = edgesBetween(e, other);
//				System.out.println("SimplexLayout: " + from + "->" + to + "[" + weight + "]");
				if (weight > 0) {
					some_src_edges = true;
					some_dst_edges = true;
//					System.out.println(e + "=>" + other);
					arc1 = to.newInputArc(from);
					arc1.setWeight(weight);
					arc1.setMinlength(1);
		}	}	}

//		System.out.println("Computed inner edges");

		client   = null;
		supplier = null;
		if (container != ls.getDiagram().getRootInstance()) {

			if (option.isShowClients()) {
				for (en = children.elements(); en.hasMoreElements(); ) {
					arc1        = (HiArc) en.nextElement();
					to          = arc1.to();
					e           = to.getReferencedObject();
					if (e == null) {
						continue;
					}
					weight = edgesFromClient(e, visibleEdges);
					if (weight > 0) {
						if (client == null) {
							client = root.newChild(null, "client", 0, 0);
						}
//						System.out.println("SimplexLayout: client=>" + to + "[" + weight + "]");
						if (option.isTopClients()) {
							arc1 = to.newInputArc(client);
						} else {
							arc1 = client.newInputArc(to);
						}
						some_src_edges = true;
						arc1.setWeight(weight);
						arc1.setMinlength(1);
			}	}	}

//			System.out.println("Computed client edges");

			if (option.isShowSuppliers()) {
				for (en = children.elements(); en.hasMoreElements(); ) {
					arc1        = (HiArc) en.nextElement();
					from        = arc1.to();
					e           = from.getReferencedObject();
					if (e == null) {
						continue;
					}
					weight = edgesToSupplier(e, visibleEdges);
					if (weight > 0) {
						if (supplier == null) {
							supplier = root.newChild(null, "supplier", 0, 0);
						}
//						System.out.println("SimplexLayout: " + from + "=>supplier[" + weight + "]");
						if (option.isTopClients()) {
							arc1 = supplier.newInputArc(from);
						} else {
							arc1 = from.newInputArc(supplier);
						}
						some_dst_edges = true;
						arc1.setWeight(weight);
						arc1.setMinlength(1);
			}	}	}
//			System.out.println("Computed supplier edges");
		}

		if (!some_src_edges || !some_dst_edges) {
			// All entities are unconnected
			return false;
		}

		unconnected = null;

		for (i = children.size(); --i >= 0; ) {
			arc1        = (HiArc) children.elementAt(i);
			from        = arc1.to();
			if (from.in().size() == 0 && from.out().size() == 0) {
				if (unconnected == null) {
					unconnected = new Vector();
				}
				unconnected.addElement(arc1);
				children.remove(i);
		}	}


//		System.out.println("Beginning layout");

//		root.dump();
		if (!layout(root)) {
			root = null;
			m_message = "Simplex layout algorithm failed";
			return false;
		} 
//		
//		System.out.println("Done layout");

		min_x = min_y = Integer.MAX_VALUE;
		max_x = max_y = Integer.MIN_VALUE;

		children = root.children();

		for (en = children.elements(); en.hasMoreElements(); ) {
			arc1       = (HiArc) en.nextElement();
			from       = arc1.to();
			e          = from.getReferencedObject();
			if (e == null) {
				continue;
			}
//			System.out.println(e + "{" + from.x() + "," + from.y() + "/" + e.getWidth() + "," + e.getHeight() + "}");

			min_x1  = from.x() - from.width()/2;
			max_x1  = min_x1   + from.width();
			min_y1  = from.y() - from.height()/2;
			max_y1  = min_y1   + from.height();

			if (min_x1 < min_x) {
				min_x = min_x1;
			}
			if (max_x1 > max_x) {
				max_x = max_x1;
			}
			if (min_y1 < min_y) {
				min_y = min_y1;
			}
			if (max_y1 > max_y) {
				max_y = max_y1;
		}	} 


		if (unconnected != null) {
			int	cols, col, rows, row, at_y, new_max_y;

			new_max_y = max_y;
			cols      = 1;
			rows      = 0;
			at_y      = min_y;
			for (i = 0; ;) {
				arc1       = (HiArc) unconnected.elementAt(i);
				from       = arc1.to();
				children.addElement(arc1);
				from.y(at_y + from.height()/2);
				at_y += from.height();
				if (at_y > new_max_y) {
					new_max_y = at_y;
				}
				if (cols == 1) {
					++rows;
				}
				if (++i >= unconnected.size()) {
					break;
				}
				at_y += m_ygap;
				if (at_y > max_y) {
					++cols;
					at_y  = min_y;
			}	}

			min_x -= cols * (max_width + m_xgap);
			max_y  = new_max_y;

			row = 0;
			col = 0;
			for (i = 0; i < unconnected.size(); ++i) {
				arc1       = (HiArc) unconnected.elementAt(i);
				from       = arc1.to();
				from.x(min_x + col * (max_width + m_xgap) + from.width()/2);
				if (++row >= rows) {
					++col;
					row = 0;
			}	}
		}

		graph_width  = (double) (max_x - min_x);
		graph_x      = ((double) min_x) - (graph_width*border/2.0);
		graph_width += graph_width*border;
		graph_height = (double) (max_y - min_y);
		graph_y      = ((double) min_y) - (graph_height*border/2.0);
		graph_height+= graph_height*border;

//		System.out.println(container + " " + graph_width + "x" + graph_height);

		if (graph_width <= 0 || graph_height <= 0) {
			return false;
		}

		shiftX      = 0.0;
		shiftY      = 0.0;
		scaleWidth  = 1.0;
		scaleHeight = 1.0;
		if (fixedRatio()) {

			/* If we are using a fixed ratio we want the graph_width/graph_height ratio
			   to be preserved in the diagram.  So we have to multiply the widths by some
			   x and the heights  by some y such that:
			      graph_width/graph_height = (x * diagram_width)/(y * diagram_height)
			   or equivalently so that
			      x/y = (graph_width * diagram_height) / (graph_height * diagram_width);
				  with either x or y being 1, and the other value <= 1.
			 */

			scaleWidth     = (graph_width * height) / (graph_height * width);
			if (scaleWidth <= 1.0) {
				shiftX      = (1.0 - scaleWidth) / 2.0;
			} else {
				scaleHeight = 1.0 / scaleWidth;
				scaleWidth  = 1.0;
				shiftY      = (1.0 - scaleHeight) / 2.0;
			}

			// Scaling can effectively be done by changing graph_width and graph_height

			graph_width  /= scaleWidth;
			graph_height /= scaleHeight;
		}

		for (en = children.elements(); en.hasMoreElements(); ) {
			arc1       = (HiArc) en.nextElement();
			from       = arc1.to();
			e          = from.getReferencedObject();
			if (e == null) {
				continue;
			}
			widthrel  = ((double) from.width())         / graph_width;
			heightrel = ((double) from.height())        / graph_height;
			xrel      = (((double) from.x()) - graph_x) / graph_width;
			yrel      = (((double) from.y()) - graph_y) / graph_height;
			xrel     += shiftX - (widthrel/2) ;
			yrel     += shiftY - (heightrel/2);

//			System.out.println("  " + e + " {" + xrel + "," + yrel + " " + widthrel + "x" + heightrel + "}");
			diagram.updateRelLocal(e, xrel, yrel, widthrel, heightrel);
		}

//		System.out.println("Simplex done");
		return true;

	} 


  // The doLayout method executes the Coffman-Graham Layer Assignment
  // algorithm and the Sugiyama algorithm on the boxes selected.
  // Assumption: All boxes selected are in the same container.

	public String doLayout(Diagram dg) 
	{
		LandscapeEditorCore	ls = m_ls;
		EntityInstance		parent;

		m_message = "Graph redrawn using Network Simplex algorithm";

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
			return	"Simplex algorithm requires that all things laid out share same parent";
		}
		ls.doLayout1(this, selectedBoxes, parent, false);
		return m_message;
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





