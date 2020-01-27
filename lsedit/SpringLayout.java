package lsedit;

import java.util.Enumeration;
import java.util.Vector;

import java.io.PrintWriter;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;

public class SpringLayout extends LandscapeLayouter  implements ToolBarEventHandler {


	static protected final int ATTRACTIVE_FORCE = 0;
	static protected final int SPARSE_FACTOR    = 1;
	static protected final int CLIENT_FORCE     = 2;
	static protected final int CLIENT_FACTOR    = 3;
	static protected final int SUPPLIER_FORCE   = 4;
	static protected final int SUPPLIER_FACTOR  = 5;
	static protected final int REPULSIVE_FORCE  = 6;
	static protected final int REPULSIVE_DIAMETER = 7;
	static protected final int ITERATIONS       = 8;
	static protected final int TIMEOUT          = 9;

	protected final static String[] m_textfield_tags = 
							{
								"spring:attraction",
								"spring:sparse",
								"spring:clientforce",
								"spring:clientfactor",
								"spring:supplierforce",
								"spring:supplierfactor",
								"spring:repulsion",
								"spring:diameter",
								"spring:iterations",
								"spring:timeout"
							};

	protected final static String[] m_textfield_titles = 
							{
								 "Related force",
								 "Sparseness factor",
								 "Client force",
								 "Client factor",
								 "Supplier force",
								 "Supplier factor",
								 "Unrelated force",
								 "Repulsive range",
								 "Iterations",
								 "Time out"							
							};

	protected final static String[] m_textfield_resets = 
							{
								"0.05",
								"1.0",
								"0.05",
								"0.5",
								"0.05",
								"0.5",
								"0.01",
								"0.75",
								"1000",
								"300"
							};

	protected static String[] m_textfield_defaults = 
							{
								"0.05",
								"1.0",
								"0.05",
								"0.5",
								"0.05",
								"0.5",
								"0.01",
								"0.75",
								"1000",
								"300"
							};

	protected static String[] m_textfield_currents = 
							{
								"0.05",
								"1.0",
								"0.05",
								"0.5",
								"0.05",
								"0.5",
								"0.01",
								"0.75",
								"1000",
								"300"
							};


	static	double	m_attractive_force   = 0.05;
	static	double	m_sparse_factor	     = 1.0;
	static	double	m_client_force       = 0.05;
	static	double	m_client_factor      = 0.5;
	static	double	m_supplier_force     = 0.05;
	static	double	m_supplier_factor    = 0.5;
	static	double	m_repulsive_force    = 0.01;
	static	double	m_repulsive_diameter = 0.75;
	static	int		m_iterations         = 1000;
	static	int		m_timeout            = 300;

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

	public String getTag()
	{
		return "spring:";
	}

	protected void setParameter(int i, String string)
	{
		string = string.trim();

		try {
			switch (i) {
				case ITERATIONS:
				case TIMEOUT:
				{
					int ival = Integer.parseInt(string);
					switch (i) {
					case ITERATIONS:
						m_iterations    = ival;
						break;
					case TIMEOUT:
						m_timeout       = ival;
						break;
					}
					break;
				}
				default:
				{
					double dval = Double.parseDouble(string);
					switch (i) {
					case ATTRACTIVE_FORCE:
						m_attractive_force = dval;
						break;
					case SPARSE_FACTOR:
						m_sparse_factor = dval;
						break;
					case CLIENT_FORCE:
						m_client_force = dval;
						break;
					case CLIENT_FACTOR:
						m_client_factor = dval;
						break;
					case SUPPLIER_FORCE:
						m_supplier_force = dval;
						break;
					case SUPPLIER_FACTOR:
						m_supplier_factor = dval;
						break;
					case REPULSIVE_FORCE:
						m_repulsive_force = dval;
						break;
					case REPULSIVE_DIAMETER:
						m_repulsive_diameter = dval;
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
		int			i;

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

	class SpringConfigure extends JDialog implements ActionListener {


		protected JTextField[]	m_textfields;
		protected JButton[]		m_buttons;
		protected JLabel		m_message;
		protected boolean		m_ret;
		
		public SpringConfigure()
		{
			super(getLs().getFrame(), "SpringLayout Configuration", true);

			Container			contentPane;
			Font				font, bold;
			JLabel				label;
			int					i;
			String				string, tip;
			JTextField			textfield;
			JButton				button;

			m_ret  = false;
			font   = FontCache.getDialogFont();
			bold   = font.deriveFont(Font.BOLD);

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
			gridLayout = new GridLayout(5, 1, 0, 10);
			labelPanel1.setLayout(gridLayout);
			gridLayout = new GridLayout(5, 1, 0, 10);
			valuePanel1.setLayout(gridLayout);
			gridLayout = new GridLayout(5, 1, 0, 10);
			labelPanel2.setLayout(gridLayout);
			gridLayout = new GridLayout(5, 1, 0, 10);
			valuePanel2.setLayout(gridLayout);

			m_textfields = new JTextField[m_textfield_tags.length];
			for (i = 0; i < m_textfield_tags.length; ++i) {
				if ((i % 2) == 0) {
					labelPanel = labelPanel1;
					valuePanel = valuePanel1;
				} else {
					labelPanel = labelPanel2;
					valuePanel = valuePanel2;
				}

				label = new JLabel(m_textfield_titles[i], JLabel.RIGHT);
				label.setFont(bold);
				labelPanel.add(label);
				m_textfields[i] = textfield = new JTextField(m_textfield_currents[i],  20);
				textfield.setFont(font);
				textfield.addActionListener(this);
				valuePanel.add(textfield);
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

			m_message = new JLabel(" ", JLabel.CENTER);
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
			Object		source;
			JTextField	textfield;
			String		string;
			String		name;
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
				return;
			case BUTTON_SET:
				for (i = 0; i < m_textfield_tags.length; ++i) {
					m_textfield_defaults[i] = m_textfields[i].getText();
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
				  "Attractive force\n" +
				  "  Scales size of jumps towards ideal distance for related nodes.\n" +
				  "  Actual jump is proportional to difference from ideal distance\n" +
				  "Repulsive force\n" +
				  "  Scales size of jumps towards repulsive diameter for unrelated nodes\n" +
				  "  Actual jump is proportional to difference from repulsive diameter\n" +
				  "Client force\n" +
				  "  Scales size of jumps towards ideal distance from clients\n" +
				  "Supplier force\n" +
				  "  Scales size of jumps towards ideal distance from suppliers\n\n" +
				  "Sparse factor\n" +
				  "  The maximum width/height of any two distinct nodes is multiplied\n" +
				  "  by the sparse factor to give the ideal distance\n" +
				  "Client factor\n" +
				  "  The maximum width/height of any two distinct nodes is multiplied\n" +
				  "  by the client factor to give the ideal distance from clients\n" +
				  "Supplier factor\n" +
				  "  The maximum width/height of any two distinct nodes is multiplied\n" +
				  "  by the supplier factor to give the ideal distance from suppliers\n" +
				  "Repulsive diameter\n" +
				  "  Ideal ratio of distance between unrelated nodes to screen size\n" +
				  "Iterations\n" +
				  "  Number of times to iterate over the algorithm\n" +
				  "Timeout\n" +
				  "  Maximum number of seconds to spend iterating"  
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
							int ivalue = Integer.parseInt(string);

							if (ivalue <= 0) {
								m_message.setText(name + " must be positive");
								return;
							}
						} catch (Throwable exception) {
							m_message.setText(name + " not an integer value");
							return;
						}
						break;
					default:
						try {
							Double.parseDouble(string);
						} catch (Throwable exception) {
							m_message.setText(name + " not a double precision value");
							return;
			}	}	}	}

			switch (state) {
			case BUTTON_OK:
				for (i = 0; i < m_textfield_tags.length; ++i) {
					setParameter(i, m_textfields[i].getText());
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

	class SpringNode {
		public EntityInstance	m_e;
		double					m_x;
		double					m_y;
		double					m_xmax;
		double					m_ymax;
		boolean					m_clients;
		boolean					m_suppliers;
	};

	public SpringLayout(LandscapeEditorCore ls, LandscapeLayouter fallback) 
	{
		super(ls, fallback);
	}

/****************************************************************************/

	public String getName()
	{
		return "Old Spring";
	}

	public String getMenuLabel() 
	{
		return "Layout using old spring algorithm";
	} 

/***************************************************************************/


	public boolean isConfigurable()
	{
		return true;
	}

	public boolean configure(LandscapeEditorCore ls)
	{
		SpringConfigure configure = new SpringConfigure();
		boolean			ok        = configure.ok();
		configure.dispose();
		return ok;
	}

	// The doLayout method executes the Network Simplex algorithm
	// Assumption: All boxes selected are in the same container.

	public boolean doLayout1(Vector selectedBoxes, EntityInstance container) 
	{
		Option	option             = Options.getDiagramOptions();
		Diagram	diagram			   = m_ls.getDiagram();
		double	attractive_force   = m_attractive_force;
		double	sparse_factor	   = m_sparse_factor;
		double	client_force       = m_client_force;
		double	client_factor      = m_client_factor;
		double	supplier_force     = m_supplier_force;
		double	supplier_factor    = m_supplier_factor;
		double	repulsive_force    = m_repulsive_force;
		double	repulsive_diameter = m_repulsive_diameter;
		int		iterations         = m_iterations;
		long	timeout            = m_timeout;

		int					size = selectedBoxes.size();
		boolean				some_edges = false;
		int					i, j, iteration;
		EntityInstance		e, e1;
		Enumeration			en;
		RelationInstance	ri;
		double				width, height, width1, height1;
		double				x, y, x1, y1, xmax, ymax, x1max, y1max, xdiff, radius, radius1, ydiff, f, length, newlength;
		double				ideal_length, ideal_client, ideal_supplier;

		switch (size) {
		case 0:
			return true;
		case 1:
			e = (EntityInstance) selectedBoxes.firstElement();
			width  = 0.5;
			height = 0.5;
			x      = (1 - width)/2;
			y      = (1 - height)/2;

			diagram.updateRelLocal(e, x, y, width, height);
			return true;
		}

		SpringNode[]	springNodes = new SpringNode[size];
		SpringNode		springNode, springNode1;
		boolean[][]		related;
		boolean			isTopClients = option.isTopClients();
		boolean			temp;

		related = new boolean[size][];

		for (i = 0; i < size; ++i) {
			springNodes[i]    = springNode = new SpringNode();
			related[i]        = new boolean[size-i];
			springNode.m_e    = e = (EntityInstance) selectedBoxes.elementAt(i);
			springNode.m_x    = e.xRelLocal();
			springNode.m_y    = e.yRelLocal();
			springNode.m_xmax = 1.0 - e.widthRelLocal();
			springNode.m_ymax = 1.0 - e.heightRelLocal();
			springNode.m_clients = false;
			springNode.m_suppliers = false;
			e.orMark(EntityInstance.SPRING_MARK);
		}

		// Compute related and ideal length (max of two times any two widths or two hights -- converts 1.4 on diagonal)

		ideal_length   = 0;
		ideal_client   = 0;
		ideal_supplier = 0;
		for (i = 0; i < size; ++i) {
			springNode        = springNodes[i];
			e                 = springNode.m_e;
			width             = e.widthRelLocal();
			height            = e.heightRelLocal();
			en                = e.srcLiftedRelationElements();
			if (en != null) {
				while (en.hasMoreElements()) {
					ri = (RelationInstance) en.nextElement();
					// Consider only visible edges when drawing layout
					if (ri.isRelationShown()) {
						e1       = ri.getDrawDst();

						if (e1.isMarked(EntityInstance.CLIENT_SUPPLIER)) {
							springNode.m_suppliers = true;
						} 
						if (e1.isMarked(EntityInstance.SPRING_MARK)) {
							some_edges = true;
							for (j = i+1; j < size; ++j) {
								if (e1 == springNodes[j].m_e) {
									related[i][j-i] = true;
									related[i][0]   = true;
									related[j][0]   = true;
									width1          = e1.widthRelLocal();
									height1         = e1.heightRelLocal();
									xdiff           = width + width1;
									if (xdiff > ideal_length) {
										ideal_length = xdiff;
									}
									ydiff           = height + height1;
									if (ydiff > ideal_length) {
										ideal_length = ydiff;
									}
									break;
			}	}	}	}	}	}	
			en = e.dstLiftedRelationElements();
			if (en != null) {
				while (en.hasMoreElements()) {
					ri = (RelationInstance) en.nextElement();
					// Consider only visible edges when drawing layout
					if (ri.isRelationShown()) {
						e1       = ri.getDrawSrc();
						if (e1.isMarked(EntityInstance.CLIENT_SUPPLIER)) {
							springNode.m_clients = true;
						} 
						if (e1.isMarked(EntityInstance.SPRING_MARK)) {
							some_edges = true;
							for (j = i+1; j < size; ++j) {
								if (e1 == springNodes[j].m_e) {
									related[i][j-i] = true;
									related[i][0]   = true;
									related[j][0]   = true;
									width1          = e1.widthRelLocal();
									height1         = e1.heightRelLocal();
									xdiff           = width + width1;
									if (xdiff > ideal_length) {
										ideal_length = xdiff;
									}
									ydiff           = height + height1;
									if (ydiff > ideal_length) {
										ideal_length = ydiff;
									}
									break;
			}	}	}	}	}	}

			if (!some_edges) {
				// All entities are unconnected
				return false;
			}

			if (springNode.m_clients || springNode.m_suppliers) {
				if (springNode.m_clients & springNode.m_suppliers) {
					springNode.m_clients   = false;
					springNode.m_suppliers = false;
				} else {
					if (!isTopClients) {
						temp                   = springNode.m_clients;
						springNode.m_clients   = springNode.m_suppliers;
						springNode.m_suppliers = temp;
			}	}	}
			e.nandMark(EntityInstance.SPRING_MARK);
		}

		ideal_client   = ideal_length * client_factor;
		ideal_supplier = ideal_length * supplier_factor;
		ideal_length  *= sparse_factor;

		timeout = System.currentTimeMillis() + (timeout * 1000);

		for (iteration = iterations; iteration > 0; --iteration) {

			for (i = 0; i < size; ++i) {
				springNode = springNodes[i];
				x          = springNode.m_x;
				y          = springNode.m_y;
				xmax       = springNode.m_xmax;
				ymax       = springNode.m_ymax;
				if (springNode.m_suppliers || springNode.m_clients) {
					if (springNode.m_suppliers) {
						y  += (ymax - y - ideal_supplier) * supplier_force;
					} else {
						y  += (ideal_client - y) * client_force;
					}
					if (y > ymax) {
						y = ymax;
					}
					if (y < 0) {
						y = 0;
				}	}

				for (j = i+1; j < size; ++j) {
					springNode1 = springNodes[j];
					x1          = springNode1.m_x;
					y1          = springNode1.m_y;
					x1max       = springNode1.m_xmax;
					y1max       = springNode1.m_ymax;
					xdiff       = x1 - x;
					ydiff       = y1 - y;
					if (xdiff == 0 && ydiff == 0) {
						xdiff = (i%3)-1;
						if (xdiff == 0) {
							ydiff = (j%2)*2-1;
						} else {
							ydiff = (j%3)-1;
					}	} 
					length = Math.sqrt(xdiff*xdiff + ydiff*ydiff);
					if (related[i][j-i]) {
						f = (length - ideal_length)         * attractive_force;
					} else {
						f = (length - repulsive_diameter) * repulsive_force;

					}
					x  += (f * xdiff/length);
					x1 -= (f * xdiff/length);
					y  += (f * ydiff/length);
					y1 -= (f * ydiff/length);
	
					if (x > xmax) {
						x1 += xmax - x;
						x   = xmax;
					}
					if (x1 > x1max) {
						x  += x1max - x1;
						x1  = x1max;
					}
					if (x < 0) {
						x1 -= x;
						x   = 0;
					}
					if (x1 < 0) {
						x  -= x1;
						x1  = 0;
					}
					if (x > xmax) {
						x = xmax;
					}
					if (x1 > x1max) {
						x1 = x1max;
					}
					if (y > ymax) {
						y1 += ymax-y;
						y   = ymax;
					}
					if (y1 > y1max) {
						y  += y1max - y1;
						y1  = y1max;
					}
					if (y < 0) {
						y1 -= y;
						y   = 0;
					}
					if (y1 < 0) {
						y  -= y1;
						y1  = 0;
					}
					if (y > ymax) {
						y = ymax;
					}
					if (y1 > y1max) {
						y1 = y1max;
					}

					springNode1.m_x = x1;
					springNode1.m_y = y1;
				}
				springNode.m_x  = x;
				springNode.m_y  = y;
			}
			if (System.currentTimeMillis() > timeout) {
				System.out.println(Util.toLocaleString() + ": SpringLayout: Timeout after " + (iterations - iteration) + " iterations");
				break;
		}	}

		for (i = 0; i < size; ++i) {
			springNode = springNodes[i];
			e          = springNode.m_e;
			diagram.updateRelLocal(e, springNode.m_x, springNode.m_y, e.widthRelLocal(), e.heightRelLocal());
		}
		return true;
	}

 	public String doLayout(Diagram dg) 
	{
		LandscapeEditorCore	ls = m_ls;
		EntityInstance		parent;
		String				msg;

		// get user's selection of boxes to be laid out

		ls.setLayouter(this);

		Vector selectedBoxes = dg.getClusterGroup();
		if (selectedBoxes == null) {
			  Util.beep();
			  return "No group selected";
		}

		msg = allInDiagram(selectedBoxes);
		if (msg != null) {
			return msg;
		}

		parent = parentOfSet(selectedBoxes);
		if (parent == null) {
			return	"Spring layout requires that all things laid out share same parent";
		}
		msg = "Graph redrawn using Old Spring Layout";
		ls.doLayout1(this, selectedBoxes, parent, false);
		return msg;
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





