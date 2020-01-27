package lsedit;

import java.util.Enumeration;
import java.util.Vector;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.PrintWriter;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;

class ClusterNode extends SpringNode {
	int						m_cluster;
	ClusterNode				m_next;
};

public class ClusterLayout extends LandscapeLayouter implements ToolBarEventHandler {

	static protected final int STIFFNESS        = 0;
	static protected final int REPULSION        = 1;
	static protected final int ATTRACTION       = 2;
	static protected final int GAP              = 3;
	static protected final int BORDER           = 4;
	static protected final int ITERATIONS       = 5;
	static protected final int TIMEOUT          = 6;
	static protected final int FORM_CLUSTERS    = 7;
	static protected final int SEPARATION_FACTOR= 8;


	protected final static String[] m_textfield_tags = 
							{
								"clusterlayout:stiffness[",
								"clusterlayout:repulsion[",
								"clusterlayout:attraction[",
								"clusterlayout:gap[",
								"clusterlayout:border[",
								"clusterlayout:iterations[",
								"clusterlayout:timeout[",
								"clusterlayout:clusters[",
								"clusterlayout:separation["
							};

	protected final static String[] m_textfield_titles = 
							{
								 "Edge stiffness",
								 "Collision repulsion",
								 "General Gravity",
								 "Ideal gap",
								 "Border",
								 "Iterations",
								 "Timeout",
								 "Number of clusters",
								 "Separation factor"
							};

	protected final static String[] m_textfield_resets = 
							{
								"0.05",
								"0.025",
								"0.005",
								"0.01",
								"0.01",
								"1000",
								"300",
								"0",
								"2.5"
							};


	protected static String[] m_textfield_defaults = 
							{
								"0.05",
								"0.025",
								"0.005",
								"0.01",
								"0.01",
								"1000",
								"300",
								"0",
								"2.5"
							};

	protected static String[] m_textfield_currents = 
							{
								"0.05",
								"0.025",
								"0.005",
								"0.01",
								"0.01",
								"1000",
								"300",
								"0",
								"2.5"
							};

	protected double	m_stiffness         = 0.05;
	protected double	m_repulsion         = 0.025;
	protected double	m_attraction        = 0.005;
	protected double	m_gap               = 0.01;
	protected double	m_border            = 0.01;
	protected int		m_iterations        = 1000;
	protected int		m_timeout           = 300;		// 5 minutes
	protected int		m_form_clusters	    = 0; 
	protected double	m_separation_factor = 2.5;

	static protected final int LEAVES           = 0;
	static protected final int MUSTBE_RELATED   = 1;
	static protected final int COMBINE_CLUSTERS = 2;
	static protected final int CLUSTER_SOURCES  = 3;
	static protected final int CLUSTER_SINKS    = 4;
	static protected final int FEEDBACK         = 5;

	protected final static String[] m_checkbox_tags = 
							{
								"clusterlayout:leaves[",
								"clusterlayout:related[",
								"clusterlayout:combine[",
								"clusterlayout:sources[",
								"clusterlayout:sinks[",
								"clusterlayout:feedback["
							};

	protected final static String[] m_checkbox_titles = 
							{
								 "Cluster leaves",
								 "Must be related",
								 "Combine clusters",
								 "Cluster sources",
								 "Cluster sinks",
								 "Provide feedback"
							};

	protected final static boolean[] m_checkbox_resets = 
							{
								true,
								true,
								true,
								true,
								true,
								true
							};

	protected static boolean[] m_checkbox_defaults = 
							{
								true,
								true,
								true,
								true,
								true,
								true
							};

	protected static boolean[] m_checkbox_currents = 
							{
								true,
								true,
								true,
								true,
								true,
								true
							};

	static protected final int BUTTON_OK      = 0;
	static protected final int BUTTON_CANCEL  = 1;
	static protected final int BUTTON_HELP    = 2;
	static protected final int BUTTON_UNDO    = 3;
	static protected final int BUTTON_DEFAULT = 4;
	static protected final int BUTTON_SET     = 5;
	static protected final int BUTTON_RESET   = 6;

	protected final static String[] m_button_titles =
							{
								"Ok",
								"Cancel",
								"Help",
								null,
								"Default",
								"Set",
								"Reset"
							};

	protected final static String[] m_button_tips =
							{
								null,
								null,
								null,
								"Enable/disable undo",
								"Use remembered default",
								"Set default to current",
								"Set default to initial"
							};


	protected static boolean parameterBoolean(int i)
	{
		return m_checkbox_currents[i];
	}

	public String getTag()
	{
		return "clusterlayout:";
	}

	protected void setParameter(int i, String string)
	{
		string = string.trim();

		try {
			switch (i) {
				case ITERATIONS:
				case TIMEOUT:
				case FORM_CLUSTERS:
				{
					int ival = Integer.parseInt(string);
					switch (i) {
					case ITERATIONS:
						m_iterations    = ival;
						break;
					case TIMEOUT:
						m_timeout       = ival;
						break;
					case FORM_CLUSTERS:
						m_form_clusters = ival;
						break;
					}
					break;
				}
				default:
				{
					double dval = Double.parseDouble(string);
					switch (i) {
					case STIFFNESS:
						m_stiffness = dval;
						break;
					case REPULSION:
						m_repulsion = dval;
						break;
					case ATTRACTION:
						m_attraction = dval;
						break;
					case GAP:
						m_gap       = dval;
						break;
					case BORDER:
						m_border    = dval;
						break;
					case SEPARATION_FACTOR:
						m_separation_factor = dval;
						break;
					}
					break;
			}	}
			m_textfield_currents[i] = string;
		} catch (Throwable exception) {
		}
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
		int		i;

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
					break;
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

	class ClusterConfigure extends JDialog implements ActionListener {

		protected JTextField[]	m_textfields;
		protected JCheckBox[]	m_checkboxes;
		protected JButton[]		m_buttons;
		protected JLabel		m_message;
		protected boolean		m_ret;
		
		public ClusterConfigure()
		{
			super(getLs().getFrame(), "Cluster Configuration", true);

			Container			contentPane;
			Font				font, bold;
			JLabel				label;
			int					i;
			String				string;
			JTextField			textfield;
			JCheckBox			checkbox;
			JButton				button;
			String				tip;

			m_ret        = false;
			font         = FontCache.getDialogFont();
			bold         = font.deriveFont(Font.BOLD);

			setForeground(ColorCache.get(0,0,0));
			setBackground(ColorCache.get(192,192,192));
			setFont(font);

			JPanel topPanel    = new JPanel();
			JPanel labelPanel1 = new JPanel();
			JPanel valuePanel1 = new JPanel();
			JPanel labelPanel2 = new JPanel();
			JPanel valuePanel2 = new JPanel();
			JPanel labelPanel, valuePanel;

			GridLayout gridLayout;

			topPanel.setLayout(new BorderLayout());
			gridLayout = new GridLayout(9, 1, 0, 10);
			labelPanel1.setLayout(gridLayout);
			gridLayout = new GridLayout(9, 1, 0, 10);
			valuePanel1.setLayout(gridLayout);
			gridLayout = new GridLayout(9, 1, 0, 10);
			labelPanel2.setLayout(gridLayout);
			gridLayout = new GridLayout(9, 1, 0, 10);
			valuePanel2.setLayout(gridLayout);

			labelPanel = labelPanel1;
			valuePanel = valuePanel1;

			m_textfields = new JTextField[m_textfield_tags.length];
			for (i = 0; i < m_textfield_tags.length; ++i) {

				if (i == FORM_CLUSTERS) {
					labelPanel = labelPanel2;
					valuePanel = valuePanel2;
				}
				m_textfields[i] = textfield = new JTextField(m_textfield_currents[i],  20);
				label = new JLabel(m_textfield_titles[i], JLabel.RIGHT);
				label.setFont(bold);
				labelPanel.add(label);
				textfield.setFont(font);
				textfield.addActionListener(this);
				valuePanel.add(textfield);
			}

			m_checkboxes = new JCheckBox[m_checkbox_tags.length];
			for (i = 0; i < m_checkbox_tags.length; ++i) {
				m_checkboxes[i] = checkbox = new JCheckBox("", m_checkbox_currents[i]);
				label = new JLabel(m_checkbox_titles[i], JLabel.RIGHT);
				label.setFont(bold);
				labelPanel.add(label);
				checkbox.setFont(font);
				valuePanel.add(checkbox);
			}

			JPanel leftPanel   = new JPanel();
			leftPanel.setLayout(new BorderLayout());
			leftPanel.add(BorderLayout.WEST, labelPanel1);
			leftPanel.add(BorderLayout.EAST, valuePanel1);

			JPanel rightPanel = new JPanel();
			rightPanel.setLayout(new BorderLayout());
			rightPanel.add(BorderLayout.WEST, labelPanel2);
			rightPanel.add(BorderLayout.EAST, valuePanel2);
	
			topPanel.add(BorderLayout.WEST, leftPanel);
			topPanel.add(BorderLayout.EAST, rightPanel);

			contentPane = getContentPane();
			contentPane.add( BorderLayout.NORTH, topPanel );

			if (m_ls.getDiagram().undoEnabled()) {
				string = "You might wish to disable undo/redo operations";
			} else {
				string = "You might wish to enable undo/redo operations";
			}
			m_message = new JLabel(string, JLabel.CENTER);
			m_message.setFont(font);
			m_message.setForeground(Color.RED);
			m_message.setSize(400,50);
			m_message.setPreferredSize(new Dimension(400,50));
			contentPane.add( BorderLayout.CENTER, m_message);

			// --------------
			// Use a FlowLayout to center the button and give it margins.

			JPanel bottomPanel = new JPanel();

			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));

			m_buttons = new JButton[m_button_titles.length];
			for (i = 0; i < m_button_titles.length; ++i) {
				string = m_button_titles[i];
				if (string == null) {
					string = undoLabel();
				}
				m_buttons[i] = button = new JButton(string);
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
			case BUTTON_DEFAULT:

				for (i = 0; i < m_textfield_tags.length; ++i) {
					m_textfields[i].setText(m_textfield_defaults[i]);
				}
				for (i = 0; i < m_checkbox_tags.length; ++i) {
					m_checkboxes[i].setSelected(m_checkbox_defaults[i]);
				}
				return;
			case BUTTON_SET:
				for (i = 0; i < m_textfield_tags.length; ++i) {
					m_textfield_defaults[i] = m_textfields[i].getText();
				}
				for (i = 0; i < m_checkbox_tags.length; ++i) {
					m_checkbox_defaults[i] = m_checkboxes[i].isSelected();
				}
				return;
			case BUTTON_UNDO:
				LandscapeEditorCore	ls = m_ls;
				ls.invertUndo();
				m_buttons[state].setText(undoLabel());
				m_message.setText("");
				return;
			case BUTTON_HELP:
				JOptionPane.showMessageDialog(m_ls.getFrame(), 	
				  "<Stiffness> of edges connecting related nodes\n" +
				  "  A larger value increases the force between related nodes\n" +
				  "<Repulsive force> between overlapping nodes\n" +
				  "  A larger value thrusts overlapping nodes further apart\n" +
				  "  0     => disable collision detection\n" +
				  "<Attraction> between nodes\n" +
				  "  General attractive force reducing size of graph which\n" +
				  "  permits larger nodes\n" +
				  "<Ideal gap> as fraction for space between entities\n" +
				  "<Border> as fraction to leave for border of diagram\n" +
				  "<Iterations> of spring layout algorithm\n" +
				  "<Timeout> in seconds to spend in layout algorithm\n" +
				  "\n" +
				  "<Number of clusters> to form. Use 0 to have the layouter use the\n" +
				  " separation factor to decide when to terminate clustering.\n"  +
				  " Set to 1 to view the graph used to cluster\n" +
				  "<Separation factor> causes clustering to stop when the\n" +
				  " minimum distance between nodes left to be clustered, is\n" +
				  " more than the prior minimum distance * factor.  Only\n" +
				  " relevant when no set number of clusters are required and\n" +
				  " clusters may themselves be combined.\n" +
				  "<Cluster leaves> of the tree discarding all existing containers\n" +
				  "<Mustbe related> restricts items in cluster to connected items\n" +
				  " Otherwise clustering is purely based on the spacial layout\n" +
				  "<Combine clusters> permits clusters containing multiple nodes to be\n" +
				  " themselves further clustered.\n" +
				  "<Custer sources> having no incoming edges in a special cluster.\n" +
				  "<Cluster sinks> having no output edges in a special cluster."
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
					case ITERATIONS:
					case TIMEOUT:
						try {
							int ivalue;

							ivalue = Integer.parseInt(string);
							if (ivalue <= 0) {
								m_message.setText(name + " must be positive");
								return;
							}
						} catch (Throwable exception) {
							m_message.setText(name + " not an integer value");
							return;
						}
						break;

					case FORM_CLUSTERS:
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
					default:
						try {
							double dval;

							dval = Double.parseDouble(string);
							switch (i) {
							case BORDER:
							case GAP:
								if (dval < 0.0 || dval >= 1.0) {
									m_message.setText(name + " must be in the range 0 to 1.0");
									return;
							}	}

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

	protected void log(String message)
	{
		if (parameterBoolean(FEEDBACK)) {
			System.out.println(Util.toLocaleString() + ": " + message);
	}	}

	public ClusterLayout(LandscapeEditorCore ls, LandscapeLayouter fallback) 
	{
		super(ls, fallback);
	}

/****************************************************************************/

	public String getName()
	{
		return "Cluster";
	}

	public String getMenuLabel() 
	{
		return "Cluster";
	} 

/***************************************************************************/

	public boolean isConfigurable()
	{
		return true;
	}

	public boolean isLayouter()
	{
		return false;
	}

	public boolean configure(LandscapeEditorCore ls)
	{
		boolean ok;

		ClusterConfigure configure = new ClusterConfigure();
		ok = configure.ok();
		configure.dispose();
		return ok;
	}

	// N.B. can't use as layout tool during initial load

	public boolean doLayout1(Vector masterBoxes, EntityInstance container) 
	{
		Vector				selectedBoxes;
		Enumeration			en;
		int					size;
		EntityInstance		e;
		boolean				leaves         = parameterBoolean(LEAVES);
		boolean				mustbe_related = parameterBoolean(MUSTBE_RELATED);

		// Do we want to cluster the selected items or the leaves of the selected items

		if (!leaves) {
			selectedBoxes = masterBoxes;
		} else {
			selectedBoxes = new Vector();
			for (en = masterBoxes.elements(); en.hasMoreElements(); ) {
				e = (EntityInstance) en.nextElement();
				e.gatherLeaves(selectedBoxes);
		}	}

		size = selectedBoxes.size();
		if (size < 2) {
			// Not worth attempting to cluster
			return true;
		}

		log("Clustering " + size + " items");

		// Use a variant of the spring layout algorithm to rapidly pull related things together
		// and push unrelated things apart. This differs from the spring layout algorithm in
		// being unconcerned about client/supplier relationships.

		ClusterNode[]		clusterNodes = new ClusterNode[size];
		boolean[][]			related      = new boolean[size][];;
		ClusterNode			clusterNode, clusterNode1;
		int					i, j;
		EntityInstance		parent, e1;
		RelationInstance	ri;
		double				x, y, xdiff, ydiff, length, ideal_length;


		for (i = 0; i < size; ++i) {
			clusterNodes[i]         = clusterNode = new ClusterNode();
			related[i]              = new boolean[size-i];
			clusterNode.m_e         = e = (EntityInstance) selectedBoxes.elementAt(i);
			clusterNode.m_clients   = 0;
			clusterNode.m_suppliers = 0;
			clusterNode.m_cluster   = i;	// Each node is originally the head of its own cluster
			clusterNode.m_next      = null;
			e.orMark(EntityInstance.SPRING_MARK);
		}

		if (leaves) {
			// Use real edges to determine relationships

			for (i = 0; i < size; ++i) {
				clusterNode = clusterNodes[i];
				e           = clusterNode.m_e;

				en = e.srcRelationElements();
				if (en != null) {
					while (en.hasMoreElements()) {
						ri = (RelationInstance) en.nextElement();
						// Consider only visible edges when drawing layout
						if (ri.isRelationShown()) {
							e1       = ri.getDst();
							if (e1.isMarked(EntityInstance.SPRING_MARK)) {
								for (j = i+1; j < size; ++j) {
									if (e1 == clusterNodes[j].m_e) {
										related[i][j-i] = true;
										break;
				}	}	}	}	}	}
				en = e.dstRelationElements();
				if (en != null) {
					while (en.hasMoreElements()) {
						ri = (RelationInstance) en.nextElement();
						// Consider only visible edges when drawing layout
						if (ri.isRelationShown()) {
							e1       = ri.getSrc();
							if (e1.isMarked(EntityInstance.SPRING_MARK)) {
								for (j = i+1; j < size; ++j) {
									if (e1 == clusterNodes[j].m_e) {
										related[i][j-i] = true;
										break;
			}	}	}	}	}	}	}
		} else {
			// Use lifted edges to determine relationships
			for (i = 0; i < size; ++i) {
				clusterNode = clusterNodes[i];
				e           = clusterNode.m_e;
				en          = e.srcLiftedRelationElements();
				if (en != null) { 
					while (en.hasMoreElements()) {
						ri = (RelationInstance) en.nextElement();
						// Consider only visible edges when drawing layout
						if (ri.isRelationShown()) {
							e1       = ri.getDrawDst();
							if (e1.isMarked(EntityInstance.SPRING_MARK)) {
								for (j = i+1; j < size; ++j) {
									if (e1 == clusterNodes[j].m_e) {
										related[i][j-i] = true;
										break;
				}	}	}	}	}	}
				en = e.dstLiftedRelationElements();
				if (en != null) {
					while (en.hasMoreElements()) {
						ri = (RelationInstance) en.nextElement();
						// Consider only visible edges when drawing layout
						if (ri.isRelationShown()) {
							e1       = ri.getDrawSrc();
							if (e1.isMarked(EntityInstance.SPRING_MARK)) {
								for (j = i+1; j < size; ++j) {
									if (e1 == clusterNodes[j].m_e) {
										related[i][j-i] = true;
										break;
			}	}	}	}	}	}	}
		}

		log("Laying out these " + size + " items");

		double	stiffness         = m_stiffness;
		double	repulsion         = m_repulsion;
		double  attraction        = m_attraction;
		double	gap               = m_gap;
		double	border            = m_border;
		double	separation_factor = m_separation_factor;
		int		iterations        = m_iterations;
		int		timeout           = m_timeout;
		int		form_clusters	  = m_form_clusters; 

		SpringLayout2.place(clusterNodes, related, iterations, gap, border, stiffness, repulsion, attraction, 0, 0, timeout);

		log("Build graph for " + size + " items");

		int			flag;
		ClusterNode	tail, target;
		ClusterNode sources   = null;
		ClusterNode sinks     = null;
		ClusterNode	utilities = null;
		ClusterNode catchall  = null;
		int			clusters  = size;
		Diagram		diagram   = m_ls.getDiagram();
		int			size1     = size - 1;
		boolean		cluster_sources = parameterBoolean(CLUSTER_SOURCES);
		boolean		cluster_sinks   = parameterBoolean(CLUSTER_SINKS);

		// Put things without connecting edges immediately into the utilities cluster

		for (i = 0; i < size; ++i) {
			clusterNode = clusterNodes[i];
			e           = clusterNode.m_e;

			flag = 0;
			if (leaves) {
				en = e.srcRelationElements();
				if (en != null) {
					while (en.hasMoreElements()) {
						ri = (RelationInstance) en.nextElement();
						if (ri.isRelationShown()) {
							e1       = ri.getDst();
							if (e1.isMarked(EntityInstance.SPRING_MARK)) {
								flag |= 1;
								break;
				}	}	}	}
				en = e.dstRelationElements();
				if (en != null) {
					while (en.hasMoreElements()) {
						ri = (RelationInstance) en.nextElement();
						if (ri.isRelationShown()) {
							e1       = ri.getSrc();
							if (e1.isMarked(EntityInstance.SPRING_MARK)) {
								flag |= 2;
								break;
				}	}	}	}
			} else {
				en = e.srcLiftedRelationElements();
				if (en != null) {
					while (en.hasMoreElements()) {
						ri = (RelationInstance) en.nextElement();
						// Consider only visible edges when drawing layout
						if (ri.isRelationShown()) {
							e1       = ri.getDrawDst();
							if (e1.isMarked(EntityInstance.SPRING_MARK)) {
								flag |= 1;
								break;
				}	}	}	}
				en = e.dstLiftedRelationElements();
				if (en != null) {
					while (en.hasMoreElements()) {
						ri = (RelationInstance) en.nextElement();
						// Consider only visible edges when drawing layout
						if (ri.isRelationShown()) {
							e1       = ri.getDrawSrc();
							if (e1.isMarked(EntityInstance.SPRING_MARK)) {
								flag |= 2;
								break;
			}	}	}	}	}

			switch (flag) {
			case 0:
				if (utilities == null) {
					utilities             = clusterNode;
					continue;
				}
				target = utilities;
				break;
			case 1:
				if (!cluster_sources) {
					continue;
				}
				if (sources == null) {
					sources = clusterNode;
					continue;
				}
				target = sources;
				break;
			case 2:
				if (!cluster_sinks) {
					continue;
				}
				if (sinks == null) {
					sinks = clusterNode;
					continue;
				}
				target = sinks;
				break;
			default:
				continue;
			}
			clusterNode.m_cluster = target.m_cluster;
			clusterNode.m_next    = target.m_next;
			target.m_next         = clusterNode;
			--clusters;
		} 

		if (form_clusters == 1) {
			log("Placing everything in a single cluster");
			
			target = null;
			size1  = 0;
			for (i = 0; i < size; ++i) {
				clusterNode = clusterNodes[i];
				if ((utilities == null || clusterNode.m_cluster != utilities.m_cluster) &&
				    (sources   == null || clusterNode.m_cluster != sources.m_cluster)   &&
					(sinks     == null || clusterNode.m_cluster != sinks.m_cluster)) {

					if (target != null) {
						clusterNode.m_cluster = target.m_cluster;
						clusterNode.m_next    = target.m_next;
						target.m_next         = clusterNode;
						--clusters;
						continue;
					}
					target = clusterNode;
				}
				size1 = i;
			}
		} else {
			log("After initial restructuring " + clusters + " clusters remain");

			if (form_clusters < clusters) {
				// Now compute the distances between 

				Vector		distances = new Vector();
				Distance	distance;
				int			pairs;
				
				for (i = 0; i < size; ++i) {
					clusterNode = clusterNodes[i];
					if (utilities != null && clusterNode.m_cluster == utilities.m_cluster) {
						continue;
					}
					x          = clusterNode.m_x;
					y          = clusterNode.m_y;
					for (j = i+1; j < size; ++j) {
						if (mustbe_related && !related[i][j-i]) {
							continue;
						}
						clusterNode1 = clusterNodes[j];
						if (utilities != null && clusterNode1.m_cluster == utilities.m_cluster) {
							continue;
						}
						xdiff  = clusterNode1.m_x - x;
						ydiff  = clusterNode1.m_y - y;

						length = Math.sqrt(xdiff*xdiff + ydiff*ydiff);
						distances.add(new Distance(length, i, j));
				}	}

/*
				for (j = 0; j < size; ++j) {
					clusterNode = clusterNodes[j];
					if (clusterNode.m_cluster == j) {
						System.out.print(j + ") ");
						for (tail = clusterNode; tail != null; tail = tail.m_next) {
							System.out.print(" " + tail.m_e + "(" + tail.m_x + "x" + tail.m_y + ")");
						}
						System.out.println("");
				}	}
 */

				boolean combine = parameterBoolean(COMBINE_CLUSTERS);

				pairs = distances.size();

				log("Sorting " + pairs + " of distances");

				SortVector.byDistance(distances);
				ideal_length = -1;
				for (i = 0; i < pairs; ++i) {
					distance = (Distance) distances.elementAt(i);
					length   = distance.m_length;
					if (form_clusters == 0 && combine && ideal_length >= 0) {
						if (length > ideal_length * separation_factor) {
							log("Distance #" + i + " of " + length + " exceeds prior distance " + ideal_length + "*" + separation_factor);
							break;
					}	}
					ideal_length = length;
					clusterNode  = clusterNodes[distance.m_i];
					clusterNode1 = clusterNodes[distance.m_j];

					if (clusterNode.m_cluster == clusterNode1.m_cluster) {
						// Both already in same cluster
						continue;
					}

					j            = clusterNode.m_cluster;					// Cluster to add to
					clusterNode  = clusterNodes[j];							// Head of this cluster
					clusterNode1 = clusterNodes[clusterNode1.m_cluster];	// Head of this cluster

					if (clusterNode == sources || clusterNode == sinks || clusterNode1 == sources || clusterNode1 == sinks) {
						continue;
					}
					if (!combine) {
						if (clusterNode.m_next != null && clusterNode1.m_next != null) {
							continue;
					}	}

	//				System.out.println("Joining cluster " + clusterNode1.m_cluster + " to " + j); 

					// Put everything in clusterNode1 into clusterNode

					for (tail = clusterNode1; tail.m_next != null; tail = tail.m_next) {
						tail.m_cluster = j;
					}
					tail.m_cluster     = j;
					tail.m_next        = clusterNode.m_next;
					clusterNode.m_next = clusterNode1;

	/*
					for (tail = clusterNode; tail != null; tail = tail.m_next) {
						System.out.print(" " + tail.m_e + "(" + tail.m_cluster + ")");
					}
					System.out.println("");
	 */
					--clusters;
	//				System.out.println("Clusters=" + clusters + " form_clusters=" + form_clusters);
					if (clusters <= form_clusters) {
						log("Reduced to " + clusters + " clusters");
						break;
					}
					if (clusters < 3) {
						break;
				}	}
			
				// Putting loose things into a new container greatly simplifies layout problems
				// Putting them one at a time into the draw root and placing sensibly is very expensive

				if (clusters > form_clusters) {
					log("Grouping remaining clusters containing one item");
					size1     = -1;
					for (i = 0; i < size; ++i) {
						clusterNode = clusterNodes[i];

						if (clusterNode.m_cluster != i) {	// Not head of a cluster
							continue;
						}
						if (clusterNode.m_next == null) {
							if (catchall != null) {
								// Add cluster node to catchall
								clusterNode.m_cluster = catchall.m_cluster;
								clusterNode.m_next    = catchall.m_next;
								catchall.m_next       = clusterNode;
								--clusters;
								continue;
							}
							catchall = clusterNode;
						}
						size1 = i;
			}	}	}	

			log("Reorganised " + size + " items into " + clusters + " selected clusters");
		}

		double width, height;
		double xmin, xmax, ymin, ymax, x1, y1;
		int	   cnt;
		double mx, cx, my, cy;
		String text;

		clusters = 0;
		e1       = null;
		for (i = 0; i <= size1; ++i) {
			clusterNode = clusterNodes[i];

			if (clusterNode.m_cluster != i) {	// Not head of a cluster
				continue;
			}
			++clusters;
//			System.out.println("Seen cluster " + clusters + " at " + i);

			e1 = diagram.updateNewEntity(null, container);
			text = "Cluster" + clusters;
			if (clusterNode == utilities) {
				text += " (Unconnected)";
			} else if (clusterNode == catchall) {
				text += " (Stray)";
			} else if (clusterNode == sources) {
				text += " (Sources)";
			} else if (clusterNode == sinks) {
				text += " (Sinks)";
			}

			e1.setLabel(text);
			
			cnt    = 1;
			e      = clusterNode.m_e;
			x      = clusterNode.m_x;
			y      = clusterNode.m_y;
			width  = e.widthRelLocal() / 2.0;
			height = e.heightRelLocal()/ 2.0;

			xmin   = x - width;
			xmax   = x + width;
			ymin   = y - height;
			ymax   = y + height;

			for (tail = clusterNode.m_next; tail != null; tail = tail.m_next) {

				e      = tail.m_e;
				x      = tail.m_x;
				y      = tail.m_y;
				width  = e.widthRelLocal() / 2.0;
				height = e.heightRelLocal()/ 2.0;

				x1 = x - width;
				if (x1 < xmin) {
					xmin = x1;
				}
				x1 = x + width;
				if (x1 > xmax) {
					xmax = x1;
				}
				y1 = y - height;
				if (y1 < ymin) {
					ymin = y1;
				}
				y1 = y + height;
				if (y1 > ymax) {
					ymax = y1;
				}
				++cnt;
			}
			e1.setDescription("Cluster of " + cnt + " items");

			mx = (1.0 - (2.0*border)) / (xmax - xmin);
			cx = border - (mx * xmin);
			my = (1.0 - (2.0*border)) / (ymax - ymin);
			cy = border - (my * ymin);

			for (tail = clusterNode; tail != null; tail = tail.m_next) {
				e        = tail.m_e;
				x        = tail.m_x * mx + cx;
				y        = tail.m_y * my + cy;
				width    = e.widthRelLocal();
				height   = e.heightRelLocal();
//					System.out.println(e + "=" + tail.m_x + "x" + tail.m_y + " -> " + x + "x" + y);

				diagram.updateRelLocal(e, x-(width/2.0), y-(height/2.0), width, height);
				parent = e.getContainedBy();
				diagram.updateMoveEntityContainment(e1, e);
				if (leaves) {
					for (;;) {
						e      = parent;
						parent = e.getContainedBy(); 
						if (e.getFirstChild() != null) {
							break;
						}
						diagram.updateCutEntity(e);
				}	}
			}
		}
			
		for (i = 0; i < size; ++i) {
			clusterNode        = clusterNodes[i];
			e                 = clusterNode.m_e;
			e.nandMark(EntityInstance.SPRING_MARK);
		}

		boolean cleared = diagram.clearFlags(false);
		if (clusters == 1 && e1 != null) {
			diagram.navigateTo(e1, true);
		} else if (cleared) {
			diagram.revalidate();
		} 

		log("Finished forming " + clusters + " clusters");
		return true;
	}


	public String doLayout(Diagram dg) 
	{
		LandscapeEditorCore	ls = m_ls;
		EntityInstance		parent;

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
			return	"Cluster layout requires that all things laid out share same parent";
		}
		ls.doLayout1(this, selectedBoxes, parent, false);
		return "Graph redrawn using Cluster Layout";
	} 

	public void processKeyEvent(int key, int modifiers, Object object) 
	{
		Diagram	dg;
		String	rmsg;

		if (!configure(m_ls)) {
			return;
		}

		dg = m_ls.getDiagram();
		if (dg != null) {
			rmsg = doLayout(dg);
			m_ls.doFeedback(rmsg);
	}	}
} 





