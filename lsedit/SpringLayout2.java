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

public class SpringLayout2 extends LandscapeLayouter  implements ToolBarEventHandler {

	static protected final int STIFFNESS        = 0;
	static protected final int REPULSION        = 1;
	static protected final int ATTRACTION       = 2;
	static protected final int CLIENT_WEIGHT    = 3;
	static protected final int SUPPLIER_WEIGHT  = 4;
	static protected final int GAP              = 5;
	static protected final int BORDER           = 6;

	// Integer values from here on

	static protected final int ITERATIONS       = 7;
	static protected final int TIMEOUT          = 8;


	protected final static String[] m_textfield_tags = 
							{
								"spring2:stiffness",
								"spring2:repulsion",
								"spring2:attraction",
								"spring2:clientweight",
								"spring2:supplierfactor",
								"spring2:gap",
								"spring2:border",
								"spring2:iterations",
								"spring2:timeout"
							};

	protected final static String[] m_textfield_titles = 
							{
								 "Edge stiffness",
								 "Collision repulsion",
								 "General attraction",
								 "Client weight",
								 "Supplier weight",
								 "Ideal gap",
								 "Border",
								 "Iterations",
								 "Time out"
							};

	protected final static String[] m_textfield_resets = 
							{
								"0.05",
								"0.075",
								"0.005",
								"1.0",
								"1.0",
								"0.01",
								"0.01",
								"1000",
								"300"
							};

	protected static String[] m_textfield_defaults = 
							{
								"0.05",
								"0.075",
								"0.005",
								"1.0",
								"1.0",
								"0.01",
								"0.01",
								"1000",
								"300"
							};

	protected static String[] m_textfield_currents = 
							{
								"0.05",
								"0.075",
								"0.005",
								"1.0",
								"1.0",
								"0.01",
								"0.01",
								"1000",
								"300"
							};


	protected double	m_stiffness          = 0.05;
	protected double	m_repulsion   	     = 0.075;
	protected double	m_attraction         = 0.005;
	protected double	m_client_weight      = 1.0;
	protected double	m_supplier_weight    = 1.0;
	protected double	m_gap                = 0.01;
	protected double	m_border             = 0.01;
	protected int		m_iterations         = 1000;
	protected long		m_timeout            = 300;

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
		return "spring2:";
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
					case STIFFNESS:
						m_stiffness = dval;
						break;
					case REPULSION:
						m_repulsion = dval;
						break;
					case ATTRACTION:
						m_attraction = dval;
						break;
					case CLIENT_WEIGHT:
						m_client_weight = dval;
						break;
					case SUPPLIER_WEIGHT:
						m_supplier_weight = dval;
						break;
					case GAP:
						m_gap = dval;
						break;
					case BORDER:
						m_border = dval;
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
			super(getLs().getFrame(), "Spring Configuration", true);

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
			gridLayout = new GridLayout(6, 1, 0, 10);
			labelPanel1.setLayout(gridLayout);
			gridLayout = new GridLayout(6, 1, 0, 10);
			valuePanel1.setLayout(gridLayout);
			gridLayout = new GridLayout(6, 1, 0, 10);
			labelPanel2.setLayout(gridLayout);
			gridLayout = new GridLayout(6, 1, 0, 10);
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
				  "Stiffness\n" +
				  "  The stiffness of edges connecting related nodes\n" +
				  "  A larger value increases the force between related nodes\n" +
				  "Repulsive force\n" +
				  "  The repulsive force between overlapping nodes\n" +
				  "  A larger value thrusts overlapping nodes further apart\n" +
				  "  0     => disable collision detection\n" +
				  "Attractive force\n" +
				  "  The attractive force between unconnected entities\n" +
				  "  It's purpose is to create a smaller overall graph in which\n" +
				  "  nodes as a consequence may be larger\n" +
				  "Client and Supplier weight\n" +
				  "  Specifies the significance of edges to clients/suppliers\n" +
				  "  0     => no significance\n" +
				  "  |1.0| => same significance as other edges\n" +
				  "  -ve   => consider all edges to clients/suppliers one edge\n" +
				  "Ideal gap\n" +
				  "  Fractional space between entities\n" +
				  "Border\n" +
				  "  Fractional space to leave for border of diagram\n" +
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

	public SpringLayout2(LandscapeEditorCore ls, LandscapeLayouter fallback) 
	{
		super(ls, fallback);
	}

/****************************************************************************/

	public String getName()
	{
		return "Spring";
	}

	public String getMenuLabel() 
	{
		return "Layout using Springs";
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

	public static void fitBorder(SpringNode[] springNodes, double border)
	{
		int					size         = springNodes.length;
		int					i;
		SpringNode			springNode;
		EntityInstance		e;
		double				x, y, width, height, x1, y1;
		double				xmin, xmax, ymin, ymax;
		double				mx, cx, my, cy;

		xmin = Double.MAX_VALUE;
		ymin = Double.MAX_VALUE;
		xmax = Double.MIN_VALUE;
		ymax = Double.MIN_VALUE;

		for (i = 0; i < size; ++i) {
			springNode = springNodes[i];
			e      = springNode.m_e;
			x      = springNode.m_x;
			y      = springNode.m_y;
			width  = e.widthRelLocal() / 2.0;
			height = e.heightRelLocal() / 2.0;

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
		}	}
				

		// Linearly transform the xpositions from [xmin,xmax] to [border, 1-border]
		// border    = m * xmin + c;
		// 1- border = m * xmax + c;
		// 1 = m(xmin+xmax) + 2*c				-- Adding equations
		// 1 - 2*border = m * (xmax-xmin);      -- Subtracting equations
		// m = (1 - 2*border) / (xmax - xmin);

		mx = (1.0 - (2.0*border)) / (xmax - xmin);
		cx = border - (mx * xmin);
		my = (1.0 - (2.0*border)) / (ymax - ymin);
		cy = border - (my * ymin);

		for (i = 0; i < size; ++i) {
			springNode = springNodes[i];
			springNode.m_x = mx * springNode.m_x + cx;
			springNode.m_y = my * springNode.m_y + cy;
		}
	}
	
	public static void place(SpringNode[] springNodes, boolean[][] related, int iterations, double gap, double border, double stiffness, double repulsion, double attraction, double client_weight, double supplier_weight, long timeout)
	{
		int					size         = springNodes.length;
		double				default_size = -1.0;
		int					dir          = 0;
		int					cycle        = 100;
		boolean				change;
		int					i, j, iteration;
		SpringNode			springNode, springNode1;
		EntityInstance		e, e1;
		double				width, height, width1, height1;
		double				client_weight1, supplier_weight1;
		boolean				compact_clients, compact_suppliers;
		int					clients, suppliers, forces;
		double				x, y, x1, y1, xdiff, ydiff, f, fx, fy, length;
		double				minwidth, minheight, hgap, vgap;
		double				ideal_length;
		long				timeout1;

//		System.out.println("SpringLayout2: Placing " + size + " nodes using " + iterations + " interations");
		
		repulsion        *= -1.0;
		compact_clients   = false;
		compact_suppliers = false;
		if (client_weight < 0) {
			compact_clients = true;
			client_weight = -client_weight;
		}
		if (supplier_weight < 0) {
			compact_suppliers = true;
			supplier_weight = -supplier_weight;
		}

		// Initial setup
		
		for (i = 0; i < size; ++i) {
			springNode = springNodes[i];
			e          = springNode.m_e;
			x          = e.xRelLocal();
			y          = e.yRelLocal();
			width      = e.widthRelLocal();
			height     = e.heightRelLocal();

			if (x < 0) {
				x = 0.5;
			}
			if (y < 0) {
				y = 0.5;
			}
			if (width < 0 || height < 0) {
				if (default_size < 0) {
					int rows;

					rows              = (int) (Math.ceil(Math.sqrt(size)));
					default_size      = (((1.0 - border*2.0)  / ((double) rows) ) - gap) / 2.0;
					if (default_size < 0) {
						gap           = 0;
						default_size  = (((1.0 - border*2.0)  / ((double) rows) )) / 2.0;
				}	}
				if (width < 0) {
					width = default_size;
					if (x + width > 1.0) {
						x = 1.0 - width;
					}
				}
				if (height < 0) {
					height = default_size;
					if (y + height > 1.0) {
						y = 1.0 - height;
				}	}
				e.setWidthRelLocal(width);
				e.setHeightRelLocal(height);
			}

			springNode.m_x         = x + width/2;
			springNode.m_y         = y + height/2;
		}

		timeout1 = System.currentTimeMillis() + (timeout * 1000);
		cycle    = 100;

		for (iteration = iterations; iteration > 0; --iteration) {
			for (i = 0; i < size; ++i) {
				springNode = springNodes[i];
				springNode.m_xForce = 0;
				springNode.m_yForce = 0;
				springNode.m_forces = 0;
			}
			change = false;
			for (i = 0; i < size; ++i) {
				springNode = springNodes[i];
				e          = springNode.m_e;
				x          = springNode.m_x;
				y          = springNode.m_y;
				width      = e.widthRelLocal();
				height     = e.heightRelLocal();
				for (j = i+1; j < size; ++j) {
					springNode1 = springNodes[j];
					e1          = springNode1.m_e;
					x1          = springNode1.m_x;
					y1          = springNode1.m_y;
					width1      = e1.widthRelLocal();
					height1     = e1.heightRelLocal();
					xdiff       = x - x1;
					ydiff       = y - y1;

					// Compute repulsive force between any two nodes

					hgap   = xdiff;
					if (hgap < 0) {
						hgap = -hgap;
					}
					vgap   = ydiff;
					if (vgap < 0) {
						vgap = -vgap;
					}

					minwidth  = (((double) (width  + width1))  / 2.0) + gap;
					minheight = (((double) (height + height1)) / 2.0) + gap;

					if (hgap <= minwidth && vgap <= minheight) {
						// If some overlap then force always pushes centers apart
						// No attractive force present.  Since length is the square of length
						// we multiple by minwidth etc. to cancel this squaring.
						// Smaller real length is to min the more force we use.
						f  = repulsion;
//						System.out.println("Too close " + e + " & " + e1 + " " + hgap + "<=" + minwidth + " & " + vgap + "<=" + minheight);
					} else {
						if (related[i][j-i]) {
							f = stiffness;
//							System.out.println("Related " + e + " & " + e1);
						} else {
							f = attraction;
//							System.out.println("Gravity " + e + " & " + e1);
					}	}
				
					if (f != 0.0) {
						while (xdiff == 0 && ydiff == 0) {
							xdiff = 0.01 * (Math.random() - 0.5);
							ydiff = 0.01 * (Math.random() - 0.5);
						}
								
						length = Math.sqrt(xdiff*xdiff + ydiff*ydiff);
						fx     = f * xdiff / length;
						fy     = f * ydiff / length;

/*
						xdiff = (x-fx) - (x1+fx);
						if (xdiff < 0) {
							xdiff = -xdiff;
						}
						if (xdiff < hgap) {
							System.out.println("X attractive " + xdiff + "<" + hgap);
						} else if (xdiff > hgap) {
							System.out.println("X repulsive  " + xdiff + ">" + hgap);
						}
						ydiff = (y-fy) - (y1+fy);
						if (ydiff < 0) {
							ydiff = -ydiff;
						}
						if (ydiff < vgap) {
							System.out.println("Y attractive " + ydiff + "<" + vgap);
						} else if (ydiff > vgap) {
							System.out.println("Y repulsive  " + ydiff + ">" + vgap);
						}
 						System.out.println("fx=" + fx + " fy=" + fy + " (" + x + "," + y + ").v.(" + x1 + "," + y1 + ")->(" + (x-fx) + "," + (y-fy) + ").v.(" + (x1+fx) + "," + (y1+fy) + ")");
 */
						springNode.m_xForce  -= fx;
						springNode.m_yForce  -= fy;
						springNode1.m_xForce += fx;
						springNode1.m_yForce += fy;
						++springNode.m_forces;
						++springNode1.m_forces;
					}
				}

				forces = springNode.m_forces;
				if (forces != 0) {
					x     += (springNode.m_xForce / (double) forces);
					y     += (springNode.m_yForce / (double) forces);
					change = true;
				}

				clients   = springNode.m_clients;
				suppliers = springNode.m_suppliers;

				if (clients != 0 && compact_clients) {
					clients = 1;
				}
				if (suppliers != 0 && compact_suppliers) {
					suppliers = 1;
				}

				client_weight1   = client_weight   * clients;
				supplier_weight1 = supplier_weight * suppliers;
				if (client_weight1 > supplier_weight1) {
					y += stiffness * ( (border + height/2.0)     +  (1.0 - (client_weight1 / (client_weight1 + supplier_weight1))) - y);
				} else if (client_weight1 < supplier_weight1) {
					y += stiffness * ( (1 - border - height/2.0) -  (client_weight1 / (client_weight1 + supplier_weight1))       - y);
				}
			
				springNode.m_x = x;
				springNode.m_y = y;
			}
			if (!change) {
				break;
		}	}
		
		fitBorder(springNodes, border);
//		System.out.println("SpringLayout2: Placed");
	}

	// Assumption: All boxes selected are in the same container.

	public boolean doLayout1(Vector selectedBoxes, EntityInstance container) 
	{
		Option	option             = Options.getDiagramOptions();
		LandscapeEditorCore	 ls    = m_ls;
		Diagram	diagram            = ls.getDiagram();
		double	stiffness          = m_stiffness;
		double	repulsion   	   = m_repulsion;
		double  attraction         = m_attraction;
		double	client_weight      = m_client_weight;
		double	supplier_weight    = m_supplier_weight;
		double	gap                = m_gap;
		double	border             = m_border;
		int		iterations         = m_iterations;
		long	timeout            = m_timeout;
		boolean	some_edges         = false;

		int					size = selectedBoxes.size();
		EntityInstance		e;
	
		switch (size) {
		case 0:
			return true;
		case 1:
			e = (EntityInstance) selectedBoxes.firstElement();
			diagram.updateRelLocal(e, 0.25, 0.25, 0.5, 0.5);
			return true;
		}

		SpringNode[]		springNodes = new SpringNode[size];
		boolean[][]			related     = new boolean[size][];

		SpringNode			springNode;
		boolean				isTopClients = option.isTopClients();
		EntityInstance		e1;
		Enumeration			en;
		RelationInstance	ri;
		double				width, height;
		int					i, j, temp;

		for (i = 0; i < size; ++i) {
			springNodes[i]         = springNode = new SpringNode();
			related[i]             = new boolean[size-i];
			springNode.m_e = e     = (EntityInstance) selectedBoxes.elementAt(i);
			springNode.m_clients   = 0;
			springNode.m_suppliers = 0;
			e.orMark(EntityInstance.SPRING_MARK);
		}

		for (i = 0; i < size; ++i) {
			springNode        = springNodes[i];
			e                 = springNode.m_e;
			en                = e.srcLiftedRelationElements();
			if (en != null) {
				while ( en.hasMoreElements() ) {
					ri = (RelationInstance) en.nextElement();
					// Consider only visible edges when drawing layout
					if (ri.isRelationShown()) {
						e1       = ri.getDrawDst();

						if (e1.isMarked(EntityInstance.SUPPLIER_MARK)) {
							some_edges = true;
							springNode.m_suppliers++;
						} 
						if (e1.isMarked(EntityInstance.SPRING_MARK)) {
							some_edges = true;
							for (j = i+1; j < size; ++j) {
								if (e1 == springNodes[j].m_e) {
									some_edges      = true;
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
						if (e1.isMarked(EntityInstance.CLIENT_MARK) && !e1.isMarked(EntityInstance.SUPPLIER_MARK)) {
							some_edges = true;
							springNode.m_clients++;
						} 
						if (e1.isMarked(EntityInstance.SPRING_MARK)) {
							some_edges = true;
							for (j = i+1; j < size; ++j) {
								if (e1 == springNodes[j].m_e) {
									some_edges      = true;
									related[i][j-i] = true;
									break;
			}	}	}	}	}	}
			if (!isTopClients) {
				temp                   = springNode.m_clients;
				springNode.m_clients   = springNode.m_suppliers;
				springNode.m_suppliers = temp;
			}
			e.nandMark(EntityInstance.SPRING_MARK);
		}

		if (!some_edges) {
			// All entities are unconnected
			System.out.println("No connected edges - using fallback layout");
			return false;
		}

		place(springNodes, related, iterations, gap, border, stiffness, repulsion, attraction, client_weight, supplier_weight, timeout);

		for (i = 0; i < size; ++i) {
			springNode = springNodes[i];
			e          = springNode.m_e;
			width      = e.widthRelLocal();
			height     = e.heightRelLocal();
			diagram.updateRelLocal(e, springNode.m_x - (width/2.0), springNode.m_y - (height/2.0), width, height);
		}
		return true;
	}


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
			return	"Spring layout requires that all things laid out share same parent";
		}
		ls.doLayout1(this, selectedBoxes, parent, false);
		return "Graph redrawn using Spring Layout";
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





