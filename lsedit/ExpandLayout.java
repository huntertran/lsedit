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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class ExpandLayout extends LandscapeLayouter implements ToolBarEventHandler {

	static protected final int	XBORDER = 0;
	static protected final int	YBORDER = 1;

	protected final static String[] m_textfield_tags = 
							{
								"expand:xborder",
								"expand:yborder",
							};

	protected final static String[] m_textfield_titles = 
							{
								"X Border fraction of total:",
								"Y Border fraction of total:"
							};

	protected final static String[] m_textfield_resets = 
							{
								"0.05",
								"0.05",
							};


	protected static String[] m_textfield_defaults = 
							{
								"0.05",
								"0.05",
							};

	protected static String[] m_textfield_currents = 
							{
								"0.05",
								"0.05",
							};

	protected static double	m_xborder = 0.05;
	protected static double	m_yborder = 0.05;

	static protected final int HORIZONTALLY     = 0;
	static protected final int VERTICALLY       = 1;

	protected final static String[] m_checkbox_tags = 
							{
								"expand:horizontally",
								"expand:vertically"
							};

	protected final static String[] m_checkbox_titles = 
							{
								 "Expand horizontally:",
								 "Expand vertically:"
							};

	protected final static boolean[] m_checkbox_resets = 
							{
								true,
								true
							};

	protected static boolean[] m_checkbox_defaults = 
							{
								true,
								true
							};
	protected static boolean[] m_checkbox_currents = 
							{
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

	protected void setParameter(int i, String string)
	{
		string = string.trim();

		try {
			double dval = Double.parseDouble(string);
			switch (i) {
			case XBORDER:
				m_xborder = dval;
				break;
			case YBORDER:
				m_yborder = dval;
				break;
			}
			m_textfield_currents[i] = string;
		} catch (Throwable exception) {
		}
	}

	protected static boolean parameterBoolean(int i)
	{
		return m_checkbox_currents[i];
	}

	public String getTag()
	{
		return "expand:";
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
			if (attribute.startsWith(textfield_tags[i])) {
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

	class ExpandConfigure extends JDialog implements ActionListener, ItemListener {

		class ExpandImage extends JComponent
		{
			public ExpandImage()
			{
				super();

				Dimension	dimension = new Dimension(400,240);
				int			i;

				setLayout(null);
				setPreferredSize(dimension);
				setMinimumSize(dimension);
				setMaximumSize(dimension);
				setSize(dimension);
				setVisible(true);
			}
			
			public void paintComponent(Graphics g)
			{
				double	xborder    = m_xborder;
				double	yborder    = m_yborder;
				boolean	horizontal = parameterBoolean(HORIZONTALLY);
				boolean	vertical   = parameterBoolean(VERTICALLY);

				int	dimension, w, h;
				int	indentx, indenty, indentw, indenth;

				w = getWidth();
				h = getHeight();

				indentx = (int) (((double) w) * (xborder/2.0));
				indenty = (int) (((double) h) * (yborder/2.0));
				indentw = (int) (((double) w) * (1.0 - xborder));
				indenth = (int) (((double) h) * (1.0 - yborder));

				g.setColor(Color.black);
				g.drawRect(0,0,w-1, h-1);
				g.setColor(Color.blue);
				if (horizontal) {
					g.drawLine(indentx,           indenty, indentx,           indenty + indenth);
					g.drawLine(indentx + indentw, indenty, indentx + indentw, indenty + indenth);
				}
				if (vertical) {
					g.drawLine(indentx,          indenty,           indentx + indentw, indenty);
					g.drawLine(indentx,          indenty + indenth, indentx + indentw, indenty + indenth);
		}	}	}

		protected JTextField[]	m_textfields;
		protected JCheckBox[]	m_checkboxes;
		protected ExpandImage	m_expandImage;
		protected JButton[]		m_buttons;
		protected JLabel		m_message;
		protected boolean		m_ret;

		public ExpandConfigure()
		{
			super(getLs().getFrame(), "Expand Configuration", true);

			Container			contentPane;
			Font				font, bold;
			JLabel				label;
			int					i;
			String				string;
			JTextField			textfield;
			JCheckBox			checkbox;
			JButton				button;
			String				tip;

			m_ret  = false;
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
			gridLayout = new GridLayout(4, 1, 0, 10);
			labelPanel.setLayout(gridLayout);

			gridLayout = new GridLayout(4,1, 0, 10);
			valuePanel.setLayout(gridLayout);

			m_textfields = new JTextField[m_textfield_tags.length];

			for (i = 0; i < m_textfield_tags.length; ++i) {

				label = new JLabel(m_textfield_titles[i], JLabel.RIGHT);
				label.setFont(bold);
				labelPanel.add(label);
				m_textfields[i] = textfield = new JTextField(m_textfield_currents[i],  20);
				textfield.setFont(font);
				textfield.addActionListener(this);
				valuePanel.add(textfield);
			}

			m_checkboxes = new JCheckBox[m_checkbox_tags.length];

			for (i = 0; i < m_checkbox_tags.length; ++i) {
				label = new JLabel(m_checkbox_titles[i], JLabel.RIGHT);
				label.setFont(bold);
				labelPanel.add(label);
				m_checkboxes[i] = checkbox = new JCheckBox("", m_checkbox_currents[i]);
				checkbox.setFont(font);
				checkbox.addActionListener(this);
				valuePanel.add(checkbox);
			}

			topPanel.add( BorderLayout.WEST,   labelPanel);
			topPanel.add( BorderLayout.EAST,   valuePanel);

			contentPane = getContentPane();
			contentPane.add( BorderLayout.NORTH, topPanel );

			JPanel centerPanel = new JPanel();
			centerPanel.setLayout( new BorderLayout() );
			m_expandImage = new ExpandImage();
			centerPanel.add( BorderLayout.NORTH, m_expandImage);
			m_expandImage.revalidate();

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
				break;
			case BUTTON_SET:
				for (i = 0; i < m_textfield_tags.length; ++i) {
					m_textfield_defaults[i] = m_textfields[i].getText();
				}
				for (i = 0; i < m_checkbox_tags.length; ++i) {
					m_checkbox_defaults[i] = m_checkboxes[i].isSelected();
				}
				break;
			case BUTTON_UNDO:
				LandscapeEditorCore	ls = m_ls;
				ls.invertUndo();
				m_buttons[state].setText(undoLabel());
				m_message.setText("");
				return;
			case BUTTON_HELP:
				JOptionPane.showMessageDialog(m_ls.getFrame(), 	
				  "Expands/Contracts the visible area of the diagram\n" +
				  "to produce the specified horizontal and vertical\n" +
				  "borders",
				 	  "Help", JOptionPane.OK_OPTION);
				return;
			default:
				for (i = 0; i < m_textfield_tags.length; ++i) {
					textfield = m_textfields[i];
					if (source == textfield || state == BUTTON_OK) {
						string = textfield.getText();
						string = string.trim();
						name   = m_textfield_titles[i];

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
				m_ret = true;
			case BUTTON_CANCEL:
				break;
			default:
				m_expandImage.repaint();
				return;
			}

			setVisible(false);
			return;
		}

		public void itemStateChanged(ItemEvent ev)
		{
			m_expandImage.repaint();
		}
	}

	public ExpandLayout(LandscapeEditorCore ls, LandscapeLayouter fallback) 
	{
		super(ls, fallback);
	}

	public String getName()
	{
		return "Expand";
	}

	public String getMenuLabel() 
	{
		return "Expand layout to fill area";
	}

	public boolean isConfigurable()
	{
		return true;
	}

	public boolean isLayouter()
	{
		return true;
	}

	public boolean configure(LandscapeEditorCore ls)
	{
		boolean	ok;

		ExpandConfigure configure = new ExpandConfigure();
		ok = configure.ok();
		configure.dispose();
		return ok;
	}

	public boolean doLayout1(Vector selectedBoxes, EntityInstance container) 
	{
		double	xborder   = m_xborder;
		double	yborder   = m_yborder;
		boolean xexpand   = parameterBoolean(HORIZONTALLY);
		boolean	yexpand   = parameterBoolean(VERTICALLY);

		EntityInstance	ce;
		double			xRel, yRel, widthRel, heightRel;
		double			min, max;
		double			diff, xshift, xscale, yshift, yscale;
		int				i, size;

		if (!xexpand && !yexpand) {
			return true;
		}

		size = selectedBoxes.size();
		if (size == 0) {
			return true;
		}

		Diagram	diagram = m_ls.getDiagram();

		xshift = 0.0;
		xscale = 1.0;
		yshift = 0.0;
		yscale = 1.0;


		if (xexpand) {

			min    = Double.MAX_VALUE;
			max    = Double.MIN_VALUE;

			for (i = size; --i >= 0; ) {
				ce   = (EntityInstance) selectedBoxes.elementAt(i);
				xRel = ce.xRelLocal();
				if (xRel < min) {
					min = xRel;
				}
				xRel += ce.widthRelLocal();
				if (xRel > max) {
					max = xRel;
			}	} 
			/* What linear transformation transforms bounds (min, max) to (xborder/2, 1 - xborder/2)

				x' = x * m + c

				xborder/2     = min * m + c
				1-(xborder/2) = max * m + c

				1 - xborder/2 - xborder/2 = (max - min) * m

				m = (1 - xborder)/(max-min)

				1 = (max+min) * m + 2c

				c = (1 - (max+min) * m)/2

				c = 1/2 - ((max+min) * (1 - xborder)/((max-min)*2)

				c = 1/2 - (max-min) * (1 - xborder)/((max-min)*2) - 2 * min * (1 - xborder)/ (max-min) * 2

				c = 1/2 - (1 - xborder)/2 - min * (1-xborder)/(max-min)

				c = xborder/2 - (1-xborder)*min/(max-min)

				Test:

				min * (1 - xborder)/(max-min) + xborder/2 - (1-xborder) * min / (max - min) =

				min/(max-min) - (min * xborder)/(max-min) + xborder/2 - min/(max-min) + (min*xborder)/(max-min) =

				xborder/2

				(max * (1 - xborder)/(max-min)) + xborder/2 - ((1-xborder) * min / (max - min)) =

				(2 * max * (1 - xborder) + xborder * (max-min) - 2 * (1-xborder) * min)/(2*(max-min)) = 

				(2 * max - 2 * max * xborder + max * xborder - min * xborder - 2 * min + 2 * min * xborder)/(2*(max-min)) = 

				(max * (2 - xborder) - min * (2 - xborder))/(2* (max - min)) =

				(max-min)*(2-xborder)/2*(max-min) =

				1 - xborder/2

			 */

			diff = max - min;
			if (diff != 0.0) {
				xscale = (1.0 - xborder) / diff;
				xshift = (xborder * 0.5) - ((1.0 - xborder) * min / diff);
		}	}

		if (yexpand) {

			min    = Double.MAX_VALUE;
			max    = Double.MIN_VALUE;

			for (i = size; --i >= 0; ) {
				ce   = (EntityInstance) selectedBoxes.elementAt(i);
				yRel = ce.yRelLocal();
				if (yRel < min) {
					min = yRel;
				}
				yRel += ce.heightRelLocal();
				if (yRel > max) {
					max = yRel;
			}	}

			diff = max - min;
			if (diff != 0.0) {
				yscale = (1.0 - yborder) / diff;
				yshift = (yborder * 0.5) - ((1.0 - yborder) * min / diff);
		}	}


		for (i = size; --i >= 0; ) {
			ce   = (EntityInstance) selectedBoxes.elementAt(i);
			xRel      = ce.xRelLocal()      * xscale + xshift;
			widthRel  = ce.widthRelLocal()  * xscale;
			yRel      = ce.yRelLocal()      * yscale + yshift;
			heightRel = ce.heightRelLocal() * yscale;

//			System.out.println(ce + " " + xRel + "," + yRel + " " + (xRel + widthRel) + "," + (yRel + heightRel));

			diagram.updateRelLocal(ce, xRel, yRel, widthRel, heightRel);
		}
		return true;
	}

  // The doLayout method executes the Coffman-Graham Layer Assignment
  // algorithm and the Sugiyama algorithm on the boxes selected.
  // Assumption: All boxes selected are in the same container.

	public String doLayout(Diagram diagram) 
	{
		LandscapeEditorCore ls = m_ls;
		EntityInstance		parent;

		// get user's selection of boxes to be laid out

		ls.setLayouter(this);

		Vector selectedBoxes = diagram.getClusterGroup();
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
			return	"Expand algorithm requires that all things laid out share same parent";
		}
		ls.doLayout1(this, selectedBoxes, parent, false);
		return "Graph redrawn using Expand algorithm";
	}

	public void processKeyEvent(int key, int modifiers, Object object) 
	{
		Diagram				diagram;
		String				rmsg;

/*
		if (!configure(m_ls)) {
			return;
		}
*/

		diagram = m_ls.getDiagram();
		if (diagram != null) {
			rmsg = doLayout(diagram);
			m_ls.doFeedback(rmsg);
	}	}
}

