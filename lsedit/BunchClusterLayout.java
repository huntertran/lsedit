package lsedit;

import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListCellRenderer;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;

import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

class ListVector extends Vector {
	protected Vector	m_listeners = null;

	public ListVector(int capacity)
	{	
		super(capacity);
	}

	public void addListDataListener(ListDataListener l)
	{
		if (m_listeners == null) {
			m_listeners = new Vector();
		}
		m_listeners.add(l);
	}

	public void removeListDataListener(ListDataListener l)
	{
		if (m_listeners != null) {
			m_listeners.remove(l);
			if (m_listeners.size() == 0) {
				m_listeners = null;
	}	}	} 

	public void stateChanged()
	{
		if (m_listeners != null) {
			ListDataEvent event = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, size());
			int			  size  = m_listeners.size();
			for (int i = 0; i < size; ++i) {
				((ListDataListener) m_listeners.elementAt(i)).contentsChanged(event);
	}	}	}
}	

class MyListModel implements ListModel, ListDataListener {

	protected Vector		m_listeners = null;
	protected ListVector	m_vector;
	protected int			m_size;
	protected int			m_vector_at;
	protected int			m_at;
	protected int			m_match;

	public MyListModel(ListVector vector, int match)
	{
		m_vector = vector;
		m_match  = match;
		contentsChanged(null);
		m_vector.addListDataListener(this);
	}

	public void addListDataListener(ListDataListener l)
	{
		if (m_listeners == null) {
			m_listeners = new Vector();
		}
		m_listeners.add(l);
	}

	public void removeListDataListener(ListDataListener l)
	{
		if (m_listeners != null) {
			m_listeners.remove(l);
			if (m_listeners.size() == 0) {
				m_listeners = null;
	}	}	} 

	public int getSize() 
	{
		return m_size;
	}

	public Object getElementAt(int index) 
	{
		int	at        = m_at;
		int	vector_at = m_vector_at;

		if (index != at) {
			EntityInstance	e;
			int				match = m_match;

			if (index < at) {
				vector_at = -1;
				at        = -1;
			}

			for (;;) {
				e = (EntityInstance) m_vector.elementAt(++vector_at);
				if (e.getOmnipresent() != match) {
					continue;
				}
				if (++at == index) {
					break;
			}	}
			m_at        = at;
			m_vector_at = vector_at;
		}
		return (m_vector.elementAt(vector_at));
	}

	// ListDataListener interface

	public void contentsChanged(ListDataEvent event)
	{
		int				match = m_match;
		int				cnt   = 0;
		int				size  = m_vector.size();
		int				i;
		EntityInstance	e;
		
		m_vector_at = -1;
		m_at        = -1;

		for (i = 0; i < size; ++i) {
			e = (EntityInstance) m_vector.elementAt(i);
			if (e.getOmnipresent() == match) {
				++cnt;
		}	}
		m_size      = cnt;

		if (m_listeners != null) {
			ListDataEvent event1 = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, cnt);
			size  = m_listeners.size();
			for (i = 0; i < size; ++i) {
				((ListDataListener) m_listeners.elementAt(i)).contentsChanged(event1);
	}	}	}

	public void intervalAdded(ListDataEvent event) 
	{
		contentsChanged(event);
	}

	public void intervalRemoved(ListDataEvent event) 
	{
		contentsChanged(event);
	}
}

class MyListCellRenderer extends JLabel implements ListCellRenderer {

 	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
	{
		String s = value.toString();
		setText(s);
    	if (isSelected) {
			setBackground(list.getSelectionBackground());
 			setForeground(list.getSelectionForeground());
 		} else {
 			setBackground(list.getBackground());
 			setForeground(list.getForeground());
 		}
 	//	setEnabled(list.isEnabled());
 		setFont(list.getFont());
		setOpaque(true);
		return this;
	}
}

public class BunchClusterLayout extends LandscapeLayouter implements ToolBarEventHandler {

	public final static String[] g_clustering_approach = new String[]
								{
									"Agglomerative",
									"One level"
								};

	public final static String[] g_output_mode = new String[]
								{
									"Median",
									"Top",
									"Detailed"
								};

	public final static String[] g_cluster_algorithm = new String[]
								{
									"Hill climbing",
									"Genetic",
									"Exhaustive"
								};

	public final static String[] g_debug = new String[]
								{
									"Silent",
									"Minimal",
									"Verbose",
									"Diagnostic"	
								};

	public final static String[] g_techniques = new String[]
								{
									"bunch.SASimpleTechnique"
								};

	public final static String[] g_selection_method = new String[]
								{
									"Tournament",
									"Roulette wheel"
								};

	public final static String[] g_calculator_classes = new String[]
								{
									"bunch.TurboMQIncrW",
									"bunch.TurboMQIncr",
									"bunch.TurboMQW",
									"bunch.TurboMQ",
									"bunch.ITurboMQ",
									"bunch.basicMQ"
								};

	protected final static  int	EXPORT               = 0;
	protected final static  int COMMAND              = 1;
	protected final static  int	IMPORTDIR            = 2;
	protected final static  int IMPORTFILE           = 3;
	protected final static	int LIBRARYS             = 4;
	protected final static  int CLIENTS              = 5;
	protected final static  int SUPPLIERS            = 6;
	protected final static  int OMNIPRESENT          = 7;
	protected final static	int HC_POPULATION_SIZE   = 8;
	protected final static	int HC_SEARCH_SPACE      = 9;
	protected final static	int HC_RANDOMIZE         = 10;
	protected final static  int HC_INITIAL_TEMP      = 11;
	protected final static	int HC_ALPHA             = 12;
	protected final static	int GA_GENERATIONS       = 13;
	protected final static	int GA_POPULATION_SIZE   = 14;
	protected final static	int GA_CROSSOVER_PROB    = 15;
	protected final static	int GA_MUTATION_PROB     = 16;
	protected final static  int MAXRUNTIME           = 17;
	protected final static	int USERFILE             = 18;
	protected final static	int	CLIENTS_MULTIPLIER   = 19;
	protected final static  int	SUPPLIERS_MULTIPLIER = 20;
	protected final static  int BOTH_MULTIPLIER      = 21;

	protected final static String[] m_textfield_tags = 
							{
								"bunch:export",
								"bunch:command",
								"bunch:importdir",
								"bunch:import",
								"bunch:librarys",
								"bunch:clients",
								"bunch:suppliers",
								"bunch:omnipresent",
								"bunch:hcpopulation",
								"bunch:hcsearchspace",
								"bunch:hcrandomize",
								"bunch:hcinitial",
								"bunch:hcalpha",
								"bunch:gagenerations",
								"bunch:gapopulation",
								"bunch:gacrossover",
								"bunch:gamutation",
								"bunch:maxruntime",
								"bunch:userfile",
								"bunch:clientsmult",
								"bunch:suppliermult",
								"bunch:bothmult"
							};

	protected final static String[] m_textfield_titles = 
							{
								"Initial graph file:",
								"Bunch command line interface:",
								"Bunch output directory:",
								"Import file:",
								"Library list:",
								"Omnipresent clients:",
								"Omnipresent suppliers:",
								"Omnipresent nodes:",
								null,
								null,
								null,
								null,
								null,
								"Number of Generations:",
								"Population Size",
								"Crossover Probability:",
								"Mutation Probability",
								null,
								"Input cluster file:",
								null,
								null,
								null
							};

	protected final static String[]	m_textfield_resets = 
							{ 
								"junk.ta",
								Util.javaCommand() + "clue.Clue",
								"",
								"junk.ta.bunch",
								"",
								"",
								"",
								"",
								"1",
								"0",
								"100",
								"1.0",
								"0.85",
								"500",
								"50",
								"0.6",
								"0.015",
								"1000",
								"",
								"0.3",
								"0.3",
								"0.3"
							};

	protected static String[] m_textfield_defaults = 
							{ 
								"junk.ta",
								Util.javaCommand() + "clue.Clue",
								"",
								"junk.ta.bunch",
								"",
								"",
								"",
								"",
								"1",
								"0",
								"100",
								"1.0",
								"0.85",
								"500",
								"50",
								"0.6",
								"0.015",
								"1000",
								"",
								"0.3",
								"0.3",
								"0.3"
							};

	protected static String[] m_textfield_currents = 
							{ 
								"junk.ta",
								Util.javaCommand() + "clue.Clue",
								"",
								"junk.ta.bunch",
								"",
								"",
								"",
								"",
								"1",
								"0",
								"100",
								"1.0",
								"0.85",
								"500",
								"50",
								"0.6",
								"0.015",
								"1000",
								"",
								"0.3",
								"0.3",
								"0.3"
							};

	protected JComboBox		m_clustering_approach = new JComboBox(g_clustering_approach);
	protected JComboBox		m_output_mode         = new JComboBox(g_output_mode);
	protected JComboBox		m_cluster_algorithm   = new JComboBox(g_cluster_algorithm);
	protected JComboBox		m_selection_method    = new JComboBox(g_selection_method);
	protected JComboBox		m_calculator_classes  = new JComboBox(g_calculator_classes);
	protected JComboBox		m_debug               = new JComboBox(g_debug);

	protected final static	int	DELETEEXPORT  = 0;
	protected final static  int	DELETEIMPORT  = 1;
	protected final static  int	LEAVES        = 2;
	protected final static  int	FEEDBACK      = 3;
	protected final static  int VANILLA       = 4;
	protected final static	int ANNEALING     = 5;
	protected final static  int TIMEOUT       = 6;
	protected final static  int LOCK_USER     = 7;

	protected final static String[] m_checkbox_tags = 
							{
								"bunch:deleteExport",
								"bunch:deleteImport",
								"bunch:leaves",
								"bunch:feedback",
								"bunch:vanilla",
								"bunch:annealing",
								"bunch:timeout",
								"bunch:lockuser"
							};

	protected final static String[] m_checkbox_titles = 
							{
								"Delete export file",
								"Delete import file",
								"Cluster leaves",
								"Feedback",
								"Vanilla",
								"Enable Simulated Annealing",
								"Limit runtime to",
								"Lock clusters"
							};

	protected final static boolean[] m_checkbox_resets = 
							{ 
								false,
								false,
								true,
								true,
								false,
								false,
								false,
								false
							};
	protected static boolean[]m_checkbox_defaults = 
							{ 
								false,
								false,
								true,
								true,
								false,
								false,
								false,
								false
							};

	protected static boolean[]m_checkbox_currents = 
							{ 
								false,
								false,
								true,
								true, 
								false,
								false,
								false,
								false
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

	protected final static int[] g_omnipresents =
							{
								EntityInstance.OMNIPRESENT_LIBRARY,
								EntityInstance.OMNIPRESENT_CLIENT,
								EntityInstance.OMNIPRESENT_SUPPLIER,
								EntityInstance.OMNIPRESENT_CS
							};

	protected final static String[] g_omnipresent_names =
							{
								"LIBRARY_LIST",
								"OMNIPRESENT_CLIENTS",
								"OMNIPRESENT_SUPPLIERS",
								"OMNIPRESENT_BOTH"
							};



	// Working registers
	
	protected Diagram		m_dg;
	protected String		m_ret;

	protected static String	parameterString(int i)
	{
		return m_textfield_currents[i];
	}

	protected static boolean parameterBoolean(int i)
	{
		return m_checkbox_currents[i];
	}

	public String getTag()
	{
		return "bunch:";
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
					m_textfield_currents[i] = value;
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

	class BunchClusterConfigure extends JDialog implements ActionListener {

		private JTabbedPane		m_topPanel;
		private BasicTab		m_basicTab;
		private OptionsTab		m_optionsTab;
		private LibrariesTab	m_librariesTab;
		private ClientsTab      m_clientsTab;
		private	SuppliersTab	m_suppliersTab;
		private BothTab			m_bothTab;
		private UserTab			m_userTab;
		private WeightsTab		m_weightsTab;

		protected Vector		m_selected;
		protected ListVector	m_sorted;		// Sorted version of m_selected with a listener capability
		public JTextField[]	m_textfields;
		protected JCheckBox[]	m_checkboxes;
		protected JButton[]		m_buttons;
		protected JLabel		m_message;
		protected boolean		m_isok;

		class BasicTab extends JPanel implements ActionListener, DocumentListener {

			class HillClimbingConfiguration extends JDialog implements ActionListener, ChangeListener {

				JTextField				m_population_size;
				JSlider					m_slider;
				JLabel					m_slider_value;
				JTextField				m_randomize;
				JComboBox				m_techniques;
				JCheckBox				m_annealing;
				JTextField				m_initialTemp;
				JTextField				m_alpha;
				JLabel					m_message;
				JButton					m_ok;
				JButton					m_cancel;

				private void allowAnnealing()
				{
					boolean enabled = m_annealing.isSelected();

					m_techniques.setEnabled(enabled);
					m_initialTemp.setEnabled(enabled);
					m_alpha.setEnabled(enabled);
				}

				public HillClimbingConfiguration()
				{
					super(getLs().getFrame(), "HillClimbing Configuration", true);
					
					Box				top, bottom;
					JPanel			main, buttons, row;
					Font			font;
					Border			border;
					JLabel			label;
					Hashtable		labels;
					int				val;
					
					top    = new Box(BoxLayout.Y_AXIS);
					font   = FontCache.getDialogFont();
					border = BorderFactory.createLineBorder(Color.BLACK);
					border = BorderFactory.createTitledBorder(border, "Clustering options", TitledBorder.LEADING, TitledBorder.TOP, font, Color.BLUE);
					top.setBorder(border);

					row = new JPanel();
					row.setLayout(new FlowLayout());
					label = new JLabel("Population size: ");
					label.setFont(font);
					row.add(label);
					m_population_size = new JTextField(m_textfields[HC_POPULATION_SIZE].getText());
					m_population_size.setColumns(6);
					row.add(m_population_size);
					top.add(row);

					row = new JPanel();
					row.setLayout(new FlowLayout());
					label = new JLabel("Minimum % of Search Space to Consider:");
					row.add(label);
					top.add(row);

					row = new JPanel();
					row.setLayout(new FlowLayout());

					try {
						val = Integer.parseInt(m_textfields[HC_SEARCH_SPACE].getText());
					} catch (Exception exception) {
						val = 0;
					}
					m_slider = new JSlider(0, 100, val);
					labels = new Hashtable();
					labels.put(new Integer(0),   new JLabel("NAHC"));
					labels.put(new Integer(100), new JLabel("SAHC"));
					m_slider.setLabelTable(labels);
					m_slider.setPaintLabels(true);
					m_slider.addChangeListener(this);
					row.add(m_slider);
					m_slider_value = new JLabel("" + val + "%");
					m_slider_value.setForeground(Color.BLUE);
					row.add(m_slider_value);
					top.add(row);

					row = new JPanel();
					row.setLayout(new FlowLayout());
					label = new JLabel("Randomize %: ");
					row.add(label);
					m_randomize = new JTextField(m_textfields[HC_RANDOMIZE].getText());
					m_randomize.setColumns(6);
					row.add(m_randomize);
					top.add(row);
					
					bottom = new Box(BoxLayout.Y_AXIS);
					border = BorderFactory.createLineBorder(Color.BLACK);
					border = BorderFactory.createTitledBorder(border, "Simulated Annealing", TitledBorder.LEADING, TitledBorder.TOP, font, Color.BLUE);
					bottom.setBorder(border);

					row = new JPanel();
					row.setLayout(new FlowLayout());
					m_annealing = new JCheckBox(m_checkbox_titles[ANNEALING], m_checkboxes[ANNEALING].isSelected());
					m_annealing.addActionListener(this);
					row.add(m_annealing);
					bottom.add(row);

					row = new JPanel();
					row.setLayout(new FlowLayout());
					label = new JLabel("Technique: ");
					row.add(label);
					m_techniques = new JComboBox(g_techniques);
					row.add(m_techniques);
					bottom.add(row);

					row = new JPanel();
					row.setLayout(new FlowLayout());
					label = new JLabel("P(accept)=exp(deltaMQ/T); T(k+1)=alpha*T(k)");
					label.setForeground(Color.RED);
					row.add(label);
					bottom.add(row);

					row = new JPanel();
					row.setLayout(new FlowLayout());
					label = new JLabel("Initial Temp. T(0): ");
					row.add(label);
					m_initialTemp = new JTextField(m_textfields[HC_INITIAL_TEMP].getText());
					row.add(m_initialTemp);
					label = new JLabel("Alpha: ");
					row.add(label);
					m_alpha = new JTextField(m_textfields[HC_ALPHA].getText());
					row.add(m_alpha);
					bottom.add(row);

					allowAnnealing();

					main   = new JPanel();
					main.setLayout(new BorderLayout());
					main.add(BorderLayout.NORTH, top);
					main.add(BorderLayout.SOUTH, bottom);

					setLayout(new BorderLayout());
					add(BorderLayout.NORTH, main);

					m_message = new JLabel("", JLabel.CENTER);
					m_message.setFont(font);
					m_message.setForeground(Color.RED);
					m_message.setSize(300,50);
					m_message.setPreferredSize(new Dimension(300,50));
					add(BorderLayout.CENTER, m_message);

					m_ok = new JButton("OK");
					m_ok.addActionListener(this);
					m_cancel = new JButton("Cancel");
					m_cancel.addActionListener(this);

					buttons = new JPanel();
					buttons.setLayout(new FlowLayout());
					buttons.add(m_ok);
					buttons.add(m_cancel);
					add(BorderLayout.SOUTH, buttons);

					pack();
					setVisible(true);
				}

				public void actionPerformed(ActionEvent ev)
				{
					Object	source;

					source = ev.getSource();

					if (source == m_annealing) {
						allowAnnealing();
						return;
					}

					if (source == m_ok) {
						String	value;
						int		i, val;
						double	dval;
						String	message = null;

						for (i = HC_POPULATION_SIZE; i <= HC_ALPHA; ++i) {
							switch (i) {
							case HC_POPULATION_SIZE:
								value = m_population_size.getText();
								try {
									val = Integer.parseInt(value);
								} catch (Exception e) {
									message = "Population size not an integer";
								}
								break;
							case HC_RANDOMIZE:
								value = m_randomize.getText();
								try {
									dval = Double.parseDouble(value);
								} catch (Exception e) {
									message = "Randomize not a double";
								}
								break;
							case HC_INITIAL_TEMP:
								value = m_initialTemp.getText();
								try {
									dval = Double.parseDouble(value);
								} catch (Exception e) {
									message = "Initial temp not a double";
								}
								break;
							case HC_ALPHA:
								value = m_alpha.getText();
								try {
									dval = Double.parseDouble(value);
								} catch (Exception e) {
									message = "Alpha not a double";
								}
								break;
							}
							if (message != null) {
								m_message.setText(message);
								return;
							}
						}
						for (i = HC_POPULATION_SIZE; i <= HC_ALPHA; ++i) {
							switch (i) {
							case HC_POPULATION_SIZE:
								value = m_population_size.getText();
								break;
							case HC_SEARCH_SPACE:
								value = "" + m_slider.getValue();
								break;
							case HC_RANDOMIZE:
								value = m_randomize.getText();
								break;
							case HC_INITIAL_TEMP:
								value = m_initialTemp.getText();
								break;
							case HC_ALPHA:
								value = m_alpha.getText();
								break;
							default:
								value = "";
							}
							m_textfields[i].setText(value);
						}
						m_checkboxes[ANNEALING].setSelected(m_annealing.isSelected());
					} else if (source != m_cancel) {
						return;
					}
					setVisible(false);
					return;
				}

				public void stateChanged(ChangeEvent ev)
				{
					Object source = ev.getSource();
					
					if (source == m_slider) {
						int value = m_slider.getValue();

						m_slider_value.setText("" + value);
						m_randomize.setText("" + (100 - value));
						return;
					}
				}	  
			}

			class GeneticConfiguration extends JDialog implements ActionListener {

				JTextField[]			m_local_textfields;
				JLabel					m_message;
				JButton					m_ok;
				JButton					m_cancel;

				public GeneticConfiguration()
				{
					super(getLs().getFrame(), "Genetic Algorithm Configuration", true);
					
					Font			font, bold;
					JPanel			main, labelPanel, valuePanel, buttons;
					GridLayout		gridLayout;
					JLabel			label;
					JTextField		textfield;
					int				i;
					
					font         = FontCache.getDialogFont();
					bold         = font.deriveFont(Font.BOLD);

					setLayout(new BorderLayout());

					main    = new JPanel();
					main.setLayout(new BorderLayout());

					labelPanel  = new JPanel();
					gridLayout  = new GridLayout(5, 1, 0, 10);
					labelPanel.setLayout(gridLayout);

					valuePanel  = new JPanel();
					gridLayout  = new GridLayout(5, 1, 0, 10);
					valuePanel.setLayout(gridLayout);

					label = new JLabel("GA Selection Method:", JLabel.LEFT);
					label.setFont(bold);
					labelPanel.add(label);
					valuePanel.add(m_selection_method);
	
					m_local_textfields = new JTextField[GA_MUTATION_PROB + 1];
					for (i = GA_GENERATIONS; i <= GA_MUTATION_PROB; ++i) {
						m_local_textfields[i] = textfield = new JTextField(m_textfields[i].getText(),  12);
						label = new JLabel(m_textfield_titles[i], JLabel.LEFT);
						label.setFont(bold);
						labelPanel.add(label);
						textfield.setFont(font);
						valuePanel.add(textfield);
					}

					main.add(BorderLayout.WEST, labelPanel);
					main.add(BorderLayout.EAST, valuePanel);

					add(BorderLayout.NORTH, main);

					m_message = new JLabel("", JLabel.CENTER);
					m_message.setFont(font);
					m_message.setForeground(Color.RED);
					m_message.setSize(300,50);
					m_message.setPreferredSize(new Dimension(300,50));
					add(BorderLayout.CENTER, m_message);

					m_ok = new JButton("OK");
					m_ok.addActionListener(this);
					m_cancel = new JButton("Cancel");
					m_cancel.addActionListener(this);

					buttons = new JPanel();
					buttons.setLayout(new FlowLayout());
					buttons.add(m_ok);
					buttons.add(m_cancel);
					add(BorderLayout.SOUTH, buttons);

					pack();
					setVisible(true);
				}

				public void actionPerformed(ActionEvent ev)
				{
					Object	source;

					source = ev.getSource();

					if (source == m_ok) {
						String	value;
						int		i, val;
						double	dval;
						String	message = null;

						for (i = GA_GENERATIONS; i <= GA_MUTATION_PROB; ++i) {
							value = m_local_textfields[i].getText();
							switch (i) {
							case GA_GENERATIONS:
								try {
									val = Integer.parseInt(value);
								} catch (Exception e) {
									message = "Generations not an integer";
								}
								break;
							case GA_POPULATION_SIZE:
								try {
									val = Integer.parseInt(value);
								} catch (Exception e) {
									message = "Population size not an integer";
								}
								break;
							case GA_CROSSOVER_PROB:
								try {
									dval = Double.parseDouble(value);
									if (dval < 0.0 || dval > 1.0) {
										message = "Crossover not a probability";
									}
								} catch (Exception e) {
									message = "Crossover probability not a double";
								}
								break;
							case GA_MUTATION_PROB:
								try {
									dval = Double.parseDouble(value);
									if (dval < 0.0 || dval > 1.0) {
										message = "Mutation not a probability";
									}
								} catch (Exception e) {
									message = "Mutation probability not a double";
								}
								break;
							}
							if (message != null) {
								m_message.setText(message);
								return;
							}
						}
						for (i = GA_GENERATIONS; i <= GA_MUTATION_PROB; ++i) {
							m_textfields[i].setText(m_local_textfields[i].getText());
						}
					} else if (source != m_cancel) {
						return;
					}
					setVisible(false);
					return;
				}
			}

			private JButton	m_exportButton = new JButton("Select...");
			private JButton m_optionButton = new JButton("Options");
			private JButton m_dirButton    = new JButton("Select...");
			private JButton m_importButton = new JButton("Select...");

			protected void setOptionState()
			{
				boolean enabled = false;

				switch (m_cluster_algorithm.getSelectedIndex()) {
				case 0:
				case 1:
					enabled = true;
				}
				m_optionButton.setEnabled(enabled);
			}

			protected void setImportFile()
			{
				if (!m_checkboxes[VANILLA].isSelected()) {
					m_textfields[IMPORTFILE].setText(Util.formFileName(m_textfields[IMPORTDIR].getText(), m_textfields[EXPORT].getText() + ".bunch"));
			}	}

			protected void setVanillaState()
			{
				boolean state = !m_checkboxes[VANILLA].isSelected();

				m_cluster_algorithm.setEnabled(state);
				m_optionButton.setEnabled(state);
				m_textfields[IMPORTDIR].setEnabled(state);
				m_dirButton.setEnabled(state);
//				m_textfields[IMPORTFILE].setEnabled(!state);
//				m_importButton.setEnabled(!state);
				if (state) {
					setOptionState();
					setImportFile();
				}
				setTabStates();
			}

			public BasicTab()
			{
				setLayout(new BorderLayout());

				JPanel		labelPanel, valuePanel, checkboxPanel, buttonPanel;
				JCheckBox	vanilla;
				GridLayout	gridLayout;
				JLabel		label;
				Font		font, bold;
				int			i;
				
				font         = FontCache.getDialogFont();
				bold         = font.deriveFont(Font.BOLD);

				labelPanel = new JPanel();
				gridLayout = new GridLayout(6, 1, 0, 10);
				labelPanel.setLayout(gridLayout);

				valuePanel = new JPanel();
				gridLayout = new GridLayout(6, 1, 0, 10);
				valuePanel.setLayout(gridLayout);

				buttonPanel = new JPanel();
				gridLayout  = new GridLayout(6, 1, 0, 10);
				buttonPanel.setLayout(gridLayout);

				label = new JLabel(m_textfield_titles[COMMAND], JLabel.RIGHT);
				label.setFont(bold);
				labelPanel.add(label);
				valuePanel.add(m_textfields[COMMAND]);

				vanilla = m_checkboxes[VANILLA];
				vanilla.addActionListener(this);
				buttonPanel.add(vanilla);

				label = new JLabel(m_textfield_titles[EXPORT], JLabel.RIGHT);
				label.setFont(bold);
				labelPanel.add(label);
				valuePanel.add(m_textfields[EXPORT]);
				m_textfields[EXPORT].getDocument().addDocumentListener(this);
				m_exportButton.setFont(bold);
				m_exportButton.addActionListener(this);
				buttonPanel.add(m_exportButton);

				label = new JLabel("Clustering method:", JLabel.RIGHT);
				label.setFont(bold);
				labelPanel.add(label);
				m_cluster_algorithm.addActionListener(this);
				valuePanel.add(m_cluster_algorithm);
				m_optionButton.setFont(bold);
				m_optionButton.addActionListener(this);
				setOptionState();
				buttonPanel.add(m_optionButton);

				label = new JLabel(m_textfield_titles[IMPORTDIR], JLabel.RIGHT);
				label.setFont(bold);
				labelPanel.add(label);
				valuePanel.add(m_textfields[IMPORTDIR]);
				m_textfields[IMPORTDIR].getDocument().addDocumentListener(this);
				m_dirButton.setFont(bold);
				m_dirButton.addActionListener(this);
				buttonPanel.add(m_dirButton);

				label = new JLabel(m_textfield_titles[IMPORTFILE], JLabel.RIGHT);
				label.setFont(bold);
				labelPanel.add(label);
				valuePanel.add(m_textfields[IMPORTFILE]);
				m_importButton.setFont(bold);
				m_importButton.addActionListener(this);
				buttonPanel.add(m_importButton);

				label = new JLabel("Output diagnostics:", JLabel.RIGHT);
				label.setFont(bold);
				labelPanel.add(label);
				valuePanel.add(m_debug);
				buttonPanel.add(new JLabel(""));

				checkboxPanel = new JPanel();
				checkboxPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));
				for (i = 0; i <= FEEDBACK; ++i) {
					if (i == LEAVES && m_selected != null) {
						// Can't change our mind about leaves now that we have selected things
						continue;
					}
					checkboxPanel.add(m_checkboxes[i]);
				}

				add(BorderLayout.WEST,   labelPanel);
				add(BorderLayout.CENTER, valuePanel);
				add(BorderLayout.EAST,   buttonPanel);
				add(BorderLayout.SOUTH,  checkboxPanel);
				setVanillaState();
			}

			// ActionListener interface

			public void actionPerformed(ActionEvent ev)
			{
				Object	source;

				source = ev.getSource();

				if (source == m_exportButton) {
					String name = m_ls.filePrompt("File to write to", m_textfields[EXPORT].getText(), LandscapeEditorCore.FA_SAVE, null); 

					if (name != null) {
						m_textfields[EXPORT].setText(name);
					}
					return;
				}
				if (source == m_cluster_algorithm) {
					setOptionState();
					return;
				}

				if (source == m_optionButton) {
					switch (m_cluster_algorithm.getSelectedIndex()) {
					case 0:
						new HillClimbingConfiguration();
						break;
					case 1:
						new GeneticConfiguration();
						break;
					}
					return;
				}

				if (source == m_dirButton) {
					String name = m_ls.filePrompt("Directory to read from", m_textfields[IMPORTDIR].getText(), LandscapeEditorCore.FA_LOAD_DIR, null); 
					if (name != null) {
						m_textfields[IMPORTDIR].setText(name);
					}
					return;
				}

				if (source == m_importButton) {
					String name = m_ls.filePrompt("File to read back from", m_textfields[IMPORTFILE].getText(), LandscapeEditorCore.FA_SAVE, null); 
					if (name != null) {
						m_textfields[IMPORTFILE].setText(name);
					}
					return;
				}


				if (source == m_checkboxes[VANILLA]) {
					setVanillaState();
					return;
				}

				return;
			}

			// DocumentListener interface

			public void changedUpdate(DocumentEvent e) 
			{
				setImportFile();
			}

			public void insertUpdate(DocumentEvent e)
			{
				setImportFile();
			} 
 
			public void removeUpdate(DocumentEvent e)
			{
				setImportFile();
			} 
 		}

		class WeightsTab extends JPanel {

			public static final int WIDTH  = 45;
			public static final int HEIGHT = 20;

			private	Box	m_weights;

			class WeightedReln extends JPanel implements DocumentListener
			{
				protected	JTextField			m_textfield;
				protected	Arrow				m_arrow;
				protected	JLabel				m_inverted;
				protected	RelationClass		m_rc;

				protected void setInverted()
				{
					boolean invert = (m_textfield.getText().indexOf('-') >= 0);

					if (m_textfield.getText().indexOf('-') >= 0) {
						m_inverted.setText(" (Inverted)");
					} else {
						m_inverted.setText("");
					}
					m_arrow.setInvert(invert);
				}


				public WeightedReln(RelationClass rc) 
				{
					Option		option;
					FlowLayout	flowLayout;
					Color		color;
					JTextField	textfield;
					Arrow		arrow;
					JLabel		label;
					String		labelText;
					int			weight;
					Font		font, bold;

					option     = Options.getDiagramOptions();
					font       = FontCache.getDialogFont();
					bold       = font.deriveFont(Font.BOLD);


					flowLayout = new FlowLayout(FlowLayout.LEFT);
//					flowLayout.setHgap(GAP);
					setLayout(flowLayout);

					m_rc   = rc;
					weight = rc.getWeight();
					m_textfield = textfield = new JTextField("" + weight);
					textfield.setFont(font);
					textfield.setColumns(5);
					textfield.getDocument().addDocumentListener(this);

					add(textfield);

					color = rc.getInheritedObjectColor();
					m_arrow = arrow = new Arrow(WIDTH, HEIGHT);
					arrow.setForeground(color);	// If null defaults to foreground

					if (option.isVariableArrowColor()) {
						color  = rc.getInheritedArrowColor();
						arrow.setHeadColor(color);
					}

					arrow.setStyle(rc.getInheritedStyle());
					arrow.setToolTipText(rc.getDescription());
					add(arrow);

					color       = rc.getInheritedLabelColor();
					labelText   = rc.getLabel();
					label = new JLabel(labelText);
					label.setForeground(color);
					label.setFont(font);
					add(label);

					m_inverted = new JLabel("");
					m_inverted.setForeground(Color.RED);
					m_inverted.setFont(bold);
					setInverted();
					add(m_inverted);
				}

				public boolean isOk()
				{
					int	val;

					try {
						val = Integer.parseInt(m_textfield.getText());
					} catch (Exception e) {
						return false;
					}
					return true;
				}

				public void setWeight()
				{
					int	val;

					try {
						val = Integer.parseInt(m_textfield.getText());
					} catch (Exception e) {
						val = 0;
					}
					m_rc.setWeight(val);
				}

				// DocumentListener interface

				public void changedUpdate(DocumentEvent e) 
				{
					setInverted();
				}

				public void insertUpdate(DocumentEvent e)
				{
					setInverted();
				} 
 
				public void removeUpdate(DocumentEvent e)
				{
					setInverted();
				} 
			}

			public WeightsTab()
			{
				Box			weights;
				Diagram		diagram = m_ls.getDiagram();
				JScrollPane	scrollPane;
				Dimension	dim;
				Enumeration	en;

				setLayout(new BorderLayout());

				m_weights = weights = new Box(BoxLayout.Y_AXIS);
				if (diagram != null) {
					for (en = diagram.enumRelationClassesInOrder(); en.hasMoreElements(); ) {
						RelationClass rc = (RelationClass) en.nextElement();

						weights.add(new WeightedReln(rc));
				}	}

				dim = m_basicTab.getPreferredSize();
//				m_weights.setPreferredSize(dim);

				scrollPane   = new JScrollPane(weights);
				scrollPane.setPreferredSize(dim);
				add(BorderLayout.CENTER, scrollPane);
			}

			public boolean isOk()
			{
				Box				weights = m_weights;
				int				size    = weights.getComponentCount();
				WeightedReln	weightedReln;
				int				i;

				for (i = 0; i < size; ++i) {
					weightedReln = (WeightedReln) weights.getComponent(i);
					if (!weightedReln.isOk()) {
						return false;
				}	}
				return true;
			}

			public void setWeights()
			{
				Box				weights = m_weights;
				int				size    = weights.getComponentCount();
				WeightedReln	weightedReln;
				int				i;

				for (i = 0; i < size; ++i) {
					weightedReln = (WeightedReln) weights.getComponent(i);
					weightedReln.setWeight();
			}	}
		}

		class OptionsTab extends Box implements ActionListener {

			JCheckBox	m_timeout;
			JTextField	m_maxruntime;

			protected JPanel newRow()
			{
				JPanel row = new JPanel();
				row.setLayout(new FlowLayout());
				return row;
			}

			protected void setMaxRuntimeState()
			{
				m_maxruntime.setEnabled(m_timeout.isSelected());
			}

			public OptionsTab()
			{
				super(BoxLayout.Y_AXIS);

				Font			font, bold;
				JLabel			label, label1, label2, label3;
				JComboBox		combo1, combo2, combo3;
				Dimension		labelsize, combosize;
				JPanel			row;
				JTextField		maxruntime;
				
				font   = FontCache.getDialogFont();
				bold   = font.deriveFont(Font.BOLD);

				label = new JLabel("Use the following options to control Bunch's clustering engine: ");
				label.setFont(bold);
				row   = newRow();
				row.add(label);
				add(row);


				label1 = new JLabel("Clustering Approach:");
				label1.setFont(bold);
				row   = newRow();
				row.add(label1);
				combo1 = m_clustering_approach;
				row.add(combo1);
				add(row);

				label2     = new JLabel("Clustering Algorithm:");
				label2.setFont(bold);
				row   = newRow();
				row.add(label2);
				combo2 = m_calculator_classes;
				row.add(combo2);
				add(row);

				label3 = new JLabel("Output options:");
				label3.setFont(bold);
				row   = newRow();
				row.add(label3);
				combo3 = m_output_mode;
				row.add(m_output_mode);
				add(row);

				labelsize =	label2.getPreferredSize(); 
				label1.setPreferredSize(labelsize);
				label3.setPreferredSize(labelsize);

				combosize = combo2.getPreferredSize();
				combo1.setPreferredSize(combosize);
				combo3.setPreferredSize(combosize);

				row = newRow();
				m_timeout = m_checkboxes[TIMEOUT];
				m_timeout.addActionListener(this);
				row.add(m_timeout);
				m_maxruntime = maxruntime = m_textfields[MAXRUNTIME];
				maxruntime.setColumns(6);
				setMaxRuntimeState();
				row.add(maxruntime);
				label = new JLabel("(ms)");
				label.setFont(bold);
				row.add(label);
				add(row);

				pack();
				setVisible(true);
			}

			// ActionListener interface

			public void actionPerformed(ActionEvent ev)
			{
				Object	source;

				source = ev.getSource();

				if (source == m_timeout) {
					setMaxRuntimeState();
					return;
				}
				return;
			}
		}

		abstract class OmnipresentTab extends JPanel implements ActionListener {
		
			MyListModel	m_rest_model;
			JList		m_rest;
			MyListModel	m_omnipresent_model;
			JList		m_omnipresent;
			JButton		m_add;
			JButton		m_remove;
			JButton		m_find;
			JButton		m_clear;

			abstract protected int			match();
			abstract protected String		topLabel();
			abstract protected String		generalLabel();
			abstract protected String		findText();
			abstract protected boolean		find();
			abstract protected JTextField	multiple(BunchClusterConfigure configure);
			abstract protected String		multipleLabel();
			
			public OmnipresentTab(BunchClusterConfigure configure)
			{	
				Font				font, bold;
				JList				rest, omnipresent;
				JScrollPane			scrollPane;
				GridBagLayout		gridBagLayout;
				GridBagConstraints	c;
				JTextField			mult;
				JLabel				label;

				font   = FontCache.getDialogFont();
				bold   = font.deriveFont(Font.BOLD);
				
				m_rest_model            = new MyListModel(m_sorted, 0);
				m_omnipresent_model     = new MyListModel(m_sorted, match());
				m_rest = rest           = new JList(m_rest_model);
				m_omnipresent = omnipresent = new JList(m_omnipresent_model);
				
				rest.setPrototypeCellValue("12345678901234567890");
				rest.setVisibleRowCount(16);
				rest.setCellRenderer(new MyListCellRenderer());

				omnipresent.setPrototypeCellValue("12345678901234567890");
				omnipresent.setVisibleRowCount(16);
				omnipresent.setCellRenderer(new MyListCellRenderer());

				Box	buttons = new Box(BoxLayout.Y_AXIS);
				m_add             = new JButton("=>");
				m_add.addActionListener(this);
				m_remove          = new JButton("<=");
				m_remove.addActionListener(this);

				buttons.add(m_remove);
				buttons.add(m_add);

				gridBagLayout = new GridBagLayout();
				c             = new GridBagConstraints();

				setFont(bold);
				setLayout(gridBagLayout);

				label = new JLabel("Nodes:");
				label.setFont(bold);

				c.fill       = GridBagConstraints.NONE;
				c.gridx      = 0;
				c.gridy      = 0;
				c.gridwidth  = 1;
				c.gridheight = 1;
				c.weightx    = 1.0;
				c.weighty    = 0.0;
				c.anchor     = GridBagConstraints.WEST; 

				gridBagLayout.setConstraints(label, c);
				add(label);

				label = new JLabel(topLabel());
				label.setFont(bold);

				c.fill       = GridBagConstraints.NONE;
				c.gridx      = 2;
				c.gridy      = 0;
				c.gridwidth  = GridBagConstraints.REMAINDER;
				c.gridheight = 1;
				c.weightx    = 1.0;
				c.weighty    = 0.0;
				c.anchor     = GridBagConstraints.WEST; 

				gridBagLayout.setConstraints(label, c);
				add(label);

				c.fill       = GridBagConstraints.BOTH;
				c.gridx      = 0;
				c.gridy      = 1;
				c.gridwidth  = 1;
				c.gridheight = 1;
				c.weightx    = 1.0;
				c.weighty    = 1.0;
				c.anchor     = GridBagConstraints.CENTER; 
				 
				scrollPane   = new JScrollPane(rest);
				gridBagLayout.setConstraints(scrollPane, c);
				add(scrollPane);

				c.fill       = GridBagConstraints.NONE;
				c.gridx      = 1;
				c.gridy      = 1;
				c.gridwidth  = 1;
				c.gridheight = 1;
				c.weightx    = 0.0;
				c.weighty    = 0.0;

				gridBagLayout.setConstraints(buttons, c);
				add(buttons);

				c.fill       = GridBagConstraints.BOTH;
				c.gridx      = 2;
				c.gridy      = 1;
				c.gridwidth  = GridBagConstraints.REMAINDER;
				c.gridheight = 1;
				c.weightx    = 1.0;
				c.weighty    = 1.0;

				omnipresent.setPreferredSize(rest.getPreferredSize());
				scrollPane   = new JScrollPane(omnipresent);
				gridBagLayout.setConstraints(scrollPane, c);
				add(scrollPane);

				JPanel	row = new JPanel();
				row.setLayout(new FlowLayout());

				m_clear = new JButton("Clear");
				m_clear.setToolTipText("Clear existing " + generalLabel());
				m_clear.addActionListener(this);
				m_find = new JButton("Find");
				m_find.setToolTipText(findText());
				m_find.addActionListener(this);
				row.add(m_clear);
				row.add(m_find);

				mult = multiple(configure);
				if (mult != null) {
					mult.setColumns(6);
					row.add(mult);
					label = new JLabel(" times average " + multipleLabel());
					label.setFont(bold);
					row.add(label);
				}

				c.fill       = GridBagConstraints.NONE;
				c.gridx      = 0;
				c.gridy      = 2;
				c.gridwidth  = GridBagConstraints.REMAINDER;
				c.gridheight = 1;
				c.weightx    = 0.0;
				c.weighty    = 0.0;
				gridBagLayout.setConstraints(row, c);
				add(row);
			}

			protected boolean positiveWeights()
			{
				Diagram		diagram = m_ls.getDiagram();
				Enumeration	en;

				if (diagram != null) {
					for (en = diagram.enumRelationClassesInOrder(); en.hasMoreElements(); ) {
						RelationClass rc = (RelationClass) en.nextElement();
						if (rc.getWeight() > 0) {
							return true;
				}	}	}
				return false;
			}

			protected boolean negativeWeights()
			{
				Diagram		diagram = m_ls.getDiagram();
				Enumeration	en;

				if (diagram != null) {
					for (en = diagram.enumRelationClassesInOrder(); en.hasMoreElements(); ) {
						RelationClass rc = (RelationClass) en.nextElement();
						if (rc.getWeight() < 0) {
							return true;
				}	}	}
				return false;
			}

			public void actionPerformed(ActionEvent ev)
			{
				Object			source;
				boolean			change = false;
				EntityInstance	e;
				int				i;
				int				size;
				int				match1 = match();

				source = ev.getSource();

				if (source == m_add) {
					Object[] values = m_rest.getSelectedValues();

					if (values != null) {
						size = values.length;
						if (size > 0) {
							for (i = 0; i < size; ++i) {
								e = (EntityInstance) values[i];
								e.orMark(match1);
							}
							change = true;
					}	}
				} else if (source == m_remove) {
					Object[] values = m_omnipresent.getSelectedValues();

					if (values != null) {
						size = values.length;
						if (size > 0) {
							for (i = 0; i < size; ++i) {
								e = (EntityInstance) values[i];
								e.nandMark(match1);
							}
							change = true;
					}	}
				} else if (source == m_find || source == m_clear) {
					Vector	sorted = m_sorted;

					size = m_sorted.size();

					if (m_omnipresent_model.getSize() != 0) {
						int rc = JOptionPane.showConfirmDialog(m_ls.getFrame(),
																"This will clear the " + generalLabel() + "\n" +
																"you have already selected\n" +
																"and start again.\n" +
																"Are you sure?",
																"Cancel automatic calculation?",
																JOptionPane.OK_CANCEL_OPTION);
						if (rc != JOptionPane.OK_OPTION) {
							return;
						}

						for (i = 0; i < size; ++i) {
							e = (EntityInstance) sorted.elementAt(i);
							if (e.getOmnipresent() == match1) {
								e.nandMark(match1);
								change = true;
					}	}	}

					if (source == m_find) {
						for (i = 0; i < size; ++i) {
							e = (EntityInstance) sorted.elementAt(i);
							e.orMark(EntityInstance.SPRING_MARK);
						}
						
						change |= find();

						for (i = 0; i < size; ++i) {
							e = (EntityInstance) sorted.elementAt(i);
							e.nandMark(EntityInstance.SPRING_MARK);
				}	}	}

				if (change) {
					m_sorted.stateChanged();
				}
				return;
			}
		}

		class LibrariesTab extends OmnipresentTab implements ActionListener {
		
			protected int match() 
			{
				return EntityInstance.OMNIPRESENT_LIBRARY;
			}

			protected String topLabel()
			{
				return "Libraries:";
			}

			protected String generalLabel()
			{
				return "libraries";
			}

			protected String findText()
			{
				return "Find nodes having only inputs";
			}

			protected JTextField multiple(BunchClusterConfigure configure)
			{
				return null;
			}

			protected String multipleLabel()
			{
				return null;
			}

			protected boolean find()
			{
				Vector				sorted = m_sorted;
				int					size   = sorted.size();

				EntityInstance		e, e1;
				Enumeration			en;
				RelationInstance	ri;
				RelationClass		rc;
				boolean				change, flag, leaves;
				int					i;
				boolean				pos = positiveWeights();
				boolean				neg = negativeWeights();

				change = false;
				leaves = parameterBoolean(LEAVES);

				for (i = 0; i < size; ++i) {
					e = (EntityInstance) sorted.elementAt(i);
					flag = true;

					if (leaves) {
						if (pos) {
							en = e.srcRelationElements();
							if (en != null) {
								while (en.hasMoreElements()) {
									ri = (RelationInstance) en.nextElement();
									rc = ri.getRelationClass();
									// Consider only visible edges
									if (rc.getWeight() > 0) {
										e1       = ri.getDst();
										if (e1.isMarked(EntityInstance.SPRING_MARK)) {
											flag = false;
											break;
							}	}	}	}

							if (!flag) {
								continue;
						}	}

						if (neg) {
							en = e.dstRelationElements();
							if (en != null) {
								while (en.hasMoreElements()) {
									ri = (RelationInstance) en.nextElement();
									rc = ri.getRelationClass();
									// Consider only visible edges
									if (rc.getWeight() < 0) {
										e1       = ri.getSrc();
										if (e1.isMarked(EntityInstance.SPRING_MARK)) {
											flag = false;
											break;
							}	}	}	}

							if (!flag) {
								continue;
						}	}
					} else {
						if (pos) {
							en = e.srcLiftedRelationElements();
							if (en != null) {
								while (en.hasMoreElements()) {
									ri = (RelationInstance) en.nextElement();
									rc = ri.getRelationClass();
									// Consider only visible edges
									if (rc.getWeight() > 0) {
										e1       = ri.getDrawDst();
										if (e1.isMarked(EntityInstance.SPRING_MARK)) {
											flag = false;
											break;
							}	}	}	}

							if (!flag) {
								continue;
						}	}

						if (neg) {
							en = e.dstLiftedRelationElements();
							if (en != null) {
								while (en.hasMoreElements()) {
									ri = (RelationInstance) en.nextElement();
									rc = ri.getRelationClass();
									// Consider only visible edges
									if (rc.getWeight() < 0) {
										e1       = ri.getDrawSrc();
										if (e1.isMarked(EntityInstance.SPRING_MARK)) {
											flag = false;
											break;
							}	}	}	}

							if (!flag) {
								continue;
						}	}
					}
					e.orMark(EntityInstance.OMNIPRESENT_LIBRARY);
					change = true;
				}

				return change;
			}

			public LibrariesTab(BunchClusterConfigure configure)
			{	
				super(configure);
			}

			public void actionPerformed(ActionEvent ev)
			{
				super.actionPerformed(ev);
		}	}

		class ClientsTab extends OmnipresentTab implements ActionListener {
		
			JTextField	m_multiple;

			protected int match() 
			{
				return EntityInstance.OMNIPRESENT_CLIENT;
			}

			protected String topLabel()
			{
				return "Clients:";
			}

			protected String generalLabel()
			{
				return "clients";
			}

			protected String findText()
			{
				return "Find nodes having excessive outputs";
			}

			protected JTextField multiple(BunchClusterConfigure configure)
			{
				m_multiple = configure.m_textfields[CLIENTS_MULTIPLIER];
				return m_multiple;
			}

			protected String multipleLabel()
			{
				return "outputs";
			}


			protected boolean find()
			{
				Vector				sorted = m_sorted;
				int					size   = sorted.size();
				String				string;
				EntityInstance		e, e1;
				Enumeration			en;
				RelationInstance	ri;
				RelationClass		rc;
				boolean				leaves, change;
				int					i, cnt;
				double				multiple, average;
				boolean				pos = positiveWeights();
				boolean				neg = negativeWeights();
				
				change = false;
				leaves = parameterBoolean(LEAVES);

				string = m_multiple.getText();
				try {
					multiple = Double.parseDouble(string);
				} catch (Exception exception) {
					JOptionPane.showMessageDialog(m_ls.getFrame(), 	
						"Multiple value '" + string + "' is not a double",
				 	    "Can't find items", JOptionPane.CANCEL_OPTION);
					return false;
				}

				if (size == 0) {
					return false;
				}

				cnt = 0;
				for (i = 0; i < size; ++i) {
					e = (EntityInstance) sorted.elementAt(i);
					if (pos) {
						if (leaves) {
							en = e.srcRelationElements();
							if (en != null) {
								while (en.hasMoreElements()) {
									ri = (RelationInstance) en.nextElement();
									rc = ri.getRelationClass();
									// Consider only visible edges
									if (rc.getWeight() > 0) {
										e1       = ri.getDst();
										if (e1.isMarked(EntityInstance.SPRING_MARK)) {
											++cnt;
							}	}	}	}
						} else {
							en = e.srcLiftedRelationElements();
							if (en != null) {
								while (en.hasMoreElements()) {
									ri = (RelationInstance) en.nextElement();
									rc = ri.getRelationClass();
									// Consider only visible edges
									if (rc.getWeight() > 0) {
										e1       = ri.getDrawDst();
										if (e1.isMarked(EntityInstance.SPRING_MARK)) {
											++cnt;
					}	}	}	}	}	}

					if (neg) {
						if (leaves) {
							en = e.dstRelationElements();
							if (en != null) {
								while (en.hasMoreElements()) {
									ri = (RelationInstance) en.nextElement();
									rc = ri.getRelationClass();
									// Consider only visible edges
									if (rc.getWeight() < 0) {
										e1       = ri.getSrc();
										if (e1.isMarked(EntityInstance.SPRING_MARK)) {
											++cnt;
							}	}	}	}
						} else {
							en = e.dstLiftedRelationElements();
							if (en != null) {
								while (en.hasMoreElements()) {
									ri = (RelationInstance) en.nextElement();
									rc = ri.getRelationClass();
									// Consider only visible edges
									if (rc.getWeight() < 0) {
										e1       = ri.getDrawSrc();
										if (e1.isMarked(EntityInstance.SPRING_MARK)) {
											++cnt;
				}	}	}	}	}	}	}

				if (cnt == 0) {
					return false;
				}

				average = multiple * ((double) cnt) / ((double) size);

				for (i = 0; i < size; ++i) {
					e   = (EntityInstance) sorted.elementAt(i);
					cnt = 0;
					if (pos) {
						if (leaves) {
							en = e.srcRelationElements();
							if (en != null) {
								while (en.hasMoreElements()) {
									ri = (RelationInstance) en.nextElement();
									rc = ri.getRelationClass();
									// Consider only visible edges
									if (rc.getWeight() > 0) {
										e1       = ri.getDst();
										if (e1.isMarked(EntityInstance.SPRING_MARK)) {
											++cnt;
							}	}	}	}
						} else {
							en = e.srcLiftedRelationElements();
							if (en != null) {
								while (en.hasMoreElements()) {
									ri = (RelationInstance) en.nextElement();
									rc = ri.getRelationClass();
									// Consider only visible edges
									if (rc.getWeight() > 0) {
										e1       = ri.getDrawDst();
										if (e1.isMarked(EntityInstance.SPRING_MARK)) {
											++cnt;
					}	}	}	}	}	}

					if (neg) {
						if (leaves) {
							en = e.dstRelationElements();
							if (en != null) {
								while (en.hasMoreElements()) {
									ri = (RelationInstance) en.nextElement();
									rc = ri.getRelationClass();
									// Consider only visible edges
									if (rc.getWeight() < 0) {
										e1       = ri.getSrc();
										if (e1.isMarked(EntityInstance.SPRING_MARK)) {
											++cnt;
							}	}	}	}
						} else {
							en = e.dstLiftedRelationElements();
							if (en != null) {
								while (en.hasMoreElements()) {
									ri = (RelationInstance) en.nextElement();
									rc = ri.getRelationClass();
									// Consider only visible edges
									if (rc.getWeight() < 0) {
										e1       = ri.getDrawSrc();
										if (e1.isMarked(EntityInstance.SPRING_MARK)) {
											++cnt;
					}	}	}	}	}	}

					if (cnt > 0 && ((double) cnt) >= average) {
						e.orMark(EntityInstance.OMNIPRESENT_CLIENT);
						change = true;
				}	}
				return change;
			}

			public ClientsTab(BunchClusterConfigure configure)
			{	
				super(configure);
			}

			public void actionPerformed(ActionEvent ev)
			{
				super.actionPerformed(ev);
		}	}

		class SuppliersTab extends OmnipresentTab implements ActionListener {
		
			JTextField	m_multiple;

			protected int match() 
			{
				return EntityInstance.OMNIPRESENT_SUPPLIER;
			}

			protected String topLabel()
			{
				return "Suppliers:";
			}

			protected String generalLabel()
			{
				return "suppliers";
			}

			protected String findText()
			{
				return "Find nodes having excessive inputs";
			}

			protected JTextField multiple(BunchClusterConfigure configure)
			{
				m_multiple = configure.m_textfields[SUPPLIERS_MULTIPLIER];
				return m_multiple;
			}

			protected String multipleLabel()
			{
				return "inputs";
			}

			protected boolean find()
			{
				Vector				sorted = m_sorted;
				int					size   = sorted.size();
				String				string;
				EntityInstance		e, e1;
				Enumeration			en;
				RelationInstance	ri;
				RelationClass		rc;
				boolean				leaves, change;
				int					i, cnt;
				double				multiple, average;
				boolean				pos = positiveWeights();
				boolean				neg = negativeWeights();
				
				change = false;
				leaves = parameterBoolean(LEAVES);

				string = m_multiple.getText();
				try {
					multiple = Double.parseDouble(string);
				} catch (Exception exception) {
					JOptionPane.showMessageDialog(m_ls.getFrame(), 	
						"Multiple value '" + string + "' is not a double",
				 	    "Can't find items", JOptionPane.CANCEL_OPTION);
					return false;
				}

				if (size == 0) {
					return false;
				}

				cnt = 0;
				for (i = 0; i < size; ++i) {
					e = (EntityInstance) sorted.elementAt(i);
					if (pos) {
						if (leaves) {
							en = e.dstRelationElements();
							if (en != null) {
								while (en.hasMoreElements()) {
									ri = (RelationInstance) en.nextElement();
									rc = ri.getRelationClass();
									// Consider only visible edges
									if (rc.getWeight() > 0) {
										e1       = ri.getSrc();
										if (e1.isMarked(EntityInstance.SPRING_MARK)) {
											++cnt;
							}	}	}	}
						} else {
							en = e.dstLiftedRelationElements();
							if (en != null) {
								while (en.hasMoreElements()) {
									ri = (RelationInstance) en.nextElement();
									rc = ri.getRelationClass();
									// Consider only visible edges
									if (rc.getWeight() > 0) {
										e1       = ri.getDrawSrc();
										if (e1.isMarked(EntityInstance.SPRING_MARK)) {
											++cnt;
					}	}	}	}	}	}

					if (neg) {
						if (leaves) {
							en = e.srcRelationElements();
							if (en != null) {
								while (en.hasMoreElements()) {
									ri = (RelationInstance) en.nextElement();
									rc = ri.getRelationClass();
									// Consider only visible edges
									if (rc.getWeight() < 0) {
										e1       = ri.getDst();
										if (e1.isMarked(EntityInstance.SPRING_MARK)) {
											++cnt;
							}	}	}	}
						} else {
							en = e.srcLiftedRelationElements();
							if (en != null) {
								while (en.hasMoreElements()) {
									ri = (RelationInstance) en.nextElement();
									rc = ri.getRelationClass();
									// Consider only visible edges
									if (rc.getWeight() < 0) {
										e1       = ri.getDrawDst();
										if (e1.isMarked(EntityInstance.SPRING_MARK)) {
											++cnt;
				}	}	}	}	}	}	}

				if (cnt == 0) {
					return false;
				}

				average = multiple * ((double) cnt) / ((double) size);

				for (i = 0; i < size; ++i) {
					e   = (EntityInstance) sorted.elementAt(i);
					cnt = 0;
					if (pos) {
						if (leaves) {
							en = e.dstRelationElements();
							if (en != null) {
								while (en.hasMoreElements()) {
									ri = (RelationInstance) en.nextElement();
									rc = ri.getRelationClass();
									// Consider only visible edges
									if (rc.getWeight() > 0) {
										e1       = ri.getSrc();
										if (e1.isMarked(EntityInstance.SPRING_MARK)) {
											++cnt;
							}	}	}	}
						} else {
							en = e.dstLiftedRelationElements();
							if (en != null) {
								while ( en.hasMoreElements() ) {
									ri = (RelationInstance) en.nextElement();
									rc = ri.getRelationClass();
									// Consider only visible edges
									if (rc.getWeight() > 0) {
										e1       = ri.getDrawSrc();
										if (e1.isMarked(EntityInstance.SPRING_MARK)) {
											++cnt;
					}	}	}	}	}	}

					if (neg) {
						if (leaves) {
							en = e.srcRelationElements();
							if (en != null) {
								while (en.hasMoreElements()) {
									ri = (RelationInstance) en.nextElement();
									rc = ri.getRelationClass();
									// Consider only visible edges
									if (rc.getWeight() < 0) {
										e1       = ri.getDst();
										if (e1.isMarked(EntityInstance.SPRING_MARK)) {
											++cnt;
							}	}	}	}
						} else {
							en = e.srcLiftedRelationElements();
							if (en != null) {
								while (en.hasMoreElements()) {
									ri = (RelationInstance) en.nextElement();
									rc = ri.getRelationClass();
									// Consider only visible edges
									if (rc.getWeight() < 0) {
										e1       = ri.getDrawDst();
										if (e1.isMarked(EntityInstance.SPRING_MARK)) {
											++cnt;
					}	}	}	}	}	}

					if (cnt > 0 && ((double) cnt) >= average) {
						e.orMark(EntityInstance.OMNIPRESENT_SUPPLIER);
						change = true;
				}	}
				return change;
			}

			public SuppliersTab(BunchClusterConfigure configure)
			{	
				super(configure);
			}

			public void actionPerformed(ActionEvent ev)
			{
				super.actionPerformed(ev);
		}	}

		class BothTab extends OmnipresentTab implements ActionListener {
		
			JTextField	m_multiple;

			protected int match() 
			{
				return EntityInstance.OMNIPRESENT_CS;
			}

			protected String topLabel()
			{
				return "Clients and Suppliers:";
			}

			protected String generalLabel()
			{
				return "items";
			}

			protected String findText()
			{
				return "Find nodes having excessive edges";
			}

			protected JTextField multiple(BunchClusterConfigure configure)
			{
				m_multiple = configure.m_textfields[BOTH_MULTIPLIER];
				return m_multiple;
			}

			protected String multipleLabel()
			{
				return "edges";
			}

			protected boolean find()
			{
				Vector				sorted = m_sorted;
				int					size   = sorted.size();
				String				string;
				EntityInstance		e, e1;
				Enumeration			en;
				RelationInstance	ri;
				RelationClass		rc;
				boolean				leaves, change;
				int					i, cnt;
				double				multiple, average;
				
				change = false;
				leaves = parameterBoolean(LEAVES);

				string = m_multiple.getText();
				try {
					multiple = Double.parseDouble(string);
				} catch (Exception exception) {
					JOptionPane.showMessageDialog(m_ls.getFrame(), 	
						"Multiple value '" + string + "' is not a double",
				 	    "Can't find items", JOptionPane.CANCEL_OPTION);
					return false;
				}

				if (size == 0) {
					return false;
				}

				cnt = 0;
				for (i = 0; i < size; ++i) {
					e = (EntityInstance) sorted.elementAt(i);
					if (leaves) {
						en = e.srcRelationElements();
						if (en != null) {
							while (en.hasMoreElements()) {
								ri = (RelationInstance) en.nextElement();
								rc = ri.getRelationClass();
								// Consider only visible edges
								if (rc.getWeight() != 0) {
									e1       = ri.getDst();
									if (e1.isMarked(EntityInstance.SPRING_MARK)) {
										++cnt;
						}	}	}	}
						en = e.dstRelationElements();
						if (en != null) {
							while (en.hasMoreElements()) {
								ri = (RelationInstance) en.nextElement();
								rc = ri.getRelationClass();
								// Consider only visible edges
								if (rc.getWeight() != 0) {
									e1       = ri.getSrc();
									if (e1.isMarked(EntityInstance.SPRING_MARK)) {
										++cnt;
						}	}	}	}

					} else {
						en = e.srcLiftedRelationElements();
						if (en != null) {
							while (en.hasMoreElements()) {
								ri = (RelationInstance) en.nextElement();
								rc = ri.getRelationClass();
								// Consider only visible edges
								if (rc.getWeight() != 0) {
									e1       = ri.getDrawDst();
									if (e1.isMarked(EntityInstance.SPRING_MARK)) {
										++cnt;
						}	}	}	}

						en = e.dstLiftedRelationElements();
						if (en != null) {
							while (en.hasMoreElements()) {
								ri = (RelationInstance) en.nextElement();
								rc = ri.getRelationClass();
								// Consider only visible edges
								if (rc.getWeight() != 0) {
									e1       = ri.getDrawSrc();
									if (e1.isMarked(EntityInstance.SPRING_MARK)) {
										++cnt;
				}	}	}	}	}	}

				if (cnt == 0) {
					return false;
				}

				average = multiple * ((double) cnt) / ((double) size);

				for (i = 0; i < size; ++i) {
					e   = (EntityInstance) sorted.elementAt(i);
					cnt = 0;
					if (leaves) {
						en = e.srcRelationElements();
						if (en != null) {
							while (en.hasMoreElements()) {
								ri = (RelationInstance) en.nextElement();
								rc = ri.getRelationClass();
								// Consider only visible edges
								if (rc.getWeight() != 0) {
									e1       = ri.getDst();
									if (e1.isMarked(EntityInstance.SPRING_MARK)) {
										++cnt;
						}	}	}	}
						en = e.dstRelationElements();
						if (en != null) {
							while (en.hasMoreElements()) {
								ri = (RelationInstance) en.nextElement();
								rc = ri.getRelationClass();
								// Consider only visible edges
								if (rc.getWeight() != 0) {
									e1       = ri.getSrc();
									if (e1.isMarked(EntityInstance.SPRING_MARK)) {
										++cnt;
						}	}	}	}

					} else {
						en = e.srcLiftedRelationElements();
						if (en != null) {
							while (en.hasMoreElements()) {
								ri = (RelationInstance) en.nextElement();
								rc = ri.getRelationClass();
								// Consider only visible edges
								if (rc.getWeight() != 0) {
									e1       = ri.getDrawDst();
									if (e1.isMarked(EntityInstance.SPRING_MARK)) {
										++cnt;
						}	}	}	}

						en = e.dstLiftedRelationElements();
						if (en != null) {
							while (en.hasMoreElements()) {
								ri = (RelationInstance) en.nextElement();
								rc = ri.getRelationClass();
								// Consider only visible edges
								if (rc.getWeight() != 0) {
									e1       = ri.getDrawSrc();
									if (e1.isMarked(EntityInstance.SPRING_MARK)) {
										++cnt;
					}	}	}	}	}
					if (cnt > 0 && ((double) cnt) >= average) {
						e.orMark(EntityInstance.OMNIPRESENT_CS);
						change = true;
				}	}
				return change;
			}

			public BothTab(BunchClusterConfigure configure)
			{	
				super(configure);
			}

			public void actionPerformed(ActionEvent ev)
			{
				super.actionPerformed(ev);
		}	}

		class UserTab extends Box implements ActionListener {

			private JTextField	m_userfile;
			private	JCheckBox	m_lock;
			private JButton		m_fileButton   = new JButton("Select...");
			private JButton		m_clearButton  = new JButton("Clear");

			public UserTab()
			{
				super(BoxLayout.Y_AXIS);

				JPanel		row;
				JLabel		label;
				Font		font, bold;
				
				font         = FontCache.getDialogFont();
				bold         = font.deriveFont(Font.BOLD);

				row = new JPanel();
				row.add(new JLabel());
				add(row);

				row = new JPanel();
				row.setLayout(new FlowLayout());
				label = new JLabel(m_textfield_titles[USERFILE], JLabel.RIGHT);
				label.setFont(bold);
				row.add(label);
				m_userfile = m_textfields[USERFILE];
				m_userfile.setColumns(50);
				row.add(m_userfile);
				m_fileButton.addActionListener(this);
				row.add(m_fileButton);
				add(row);

				row = new JPanel();
				row.setLayout(new FlowLayout());
				m_lock = m_checkboxes[LOCK_USER];
				row.add(m_lock);
				m_clearButton.addActionListener(this);
				row.add(m_clearButton);
				add(row);
			}

			// ActionListener interface

			public void actionPerformed(ActionEvent ev)
			{
				Object	source;

				source = ev.getSource();

				if (source == m_fileButton) {
					String name = m_ls.filePrompt("File to read preconfigured clustering from", m_userfile.getText(), LandscapeEditorCore.FA_LOAD, null); 
					if (name != null) {
						m_userfile.setText(name);
					}
					return;
				}
				if (source == m_clearButton) {
					m_userfile.setText("");
					m_lock.setSelected(false);
					return;
				}
				return;
			}
 		}

		protected void setTabStates()
		{
			boolean state = !m_checkboxes[VANILLA].isSelected();

			m_topPanel.remove(m_optionsTab);
			if (m_librariesTab != null) {
				m_topPanel.remove(m_librariesTab);
			}
			if (m_clientsTab != null) {
				m_topPanel.remove(m_clientsTab);
			}
			if (m_suppliersTab != null) {
				m_topPanel.remove(m_suppliersTab);
			}
			if (m_bothTab != null) {
				m_topPanel.remove(m_bothTab);
			}
			if (m_userTab != null) {
				m_topPanel.remove(m_userTab);
			}
			if (state) {
				m_topPanel.addTab("Options",     null, m_optionsTab,     null);
				if (m_selected != null) {
					if (m_librariesTab == null) {
						m_librariesTab   = new LibrariesTab(this);
					}
					if (m_clientsTab == null) {
						m_clientsTab = new ClientsTab(this);
					}
					if (m_suppliersTab == null) {
						m_suppliersTab = new SuppliersTab(this);
					}
					if (m_bothTab == null) {
						m_bothTab = new BothTab(this);
					}
					if (m_userTab == null) {
						m_userTab = new UserTab();
					}

					m_topPanel.addTab("Libraries",       null, m_librariesTab,   null);
					m_topPanel.addTab("Clients",         null, m_clientsTab,     null);
					m_topPanel.addTab("Suppliers",       null, m_suppliersTab,   null);
					m_topPanel.addTab("Both",            null, m_bothTab,        null);
					m_topPanel.addTab("User directed clustering",  null, m_userTab,        null);
			}	}
		}


		public BunchClusterConfigure(BunchClusterLayout layout, String message, Vector selected)
		{
			super(layout.getLs().getFrame(), layout.getName() + " Configuration", true);

			Container			contentPane;
			JPanel				buttonPanel;
			JTextField			textfield;
			Font				font, bold;
			int					i;
			String				string, tip;
			JCheckBox			checkbox;
			JButton				button;

			m_isok       = false;
			m_selected   = selected;

			if (selected != null) {
				m_sorted = new ListVector(selected.size());
				m_sorted.addAll(selected);
				SortVector.byString(m_sorted);
			} else {
				m_sorted = null;
			}

			font         = FontCache.getDialogFont();
			bold         = font.deriveFont(Font.BOLD);

			contentPane = getContentPane();
			contentPane.setLayout(new BorderLayout());

			setForeground(ColorCache.get(0,0,0));
			setBackground(ColorCache.get(192,192,192));
			setFont(font);

			m_textfields = new JTextField[m_textfield_tags.length];

			for (i = 0; i < m_textfield_tags.length; ++i) {
				m_textfields[i] = textfield = new JTextField(m_textfield_currents[i],  60);
				textfield.setFont(font);
			}

			m_checkboxes = new JCheckBox[m_checkbox_tags.length];
			for (i = 0; i < m_checkbox_tags.length; ++i) {
				m_checkboxes[i] = checkbox = new JCheckBox(m_checkbox_titles[i], m_checkbox_currents[i]);
				checkbox.setFont(font);
			}

			m_topPanel       = new JTabbedPane();
			m_optionsTab     = new OptionsTab();
			m_basicTab       = new BasicTab();
			m_weightsTab     = new WeightsTab();

			m_topPanel.addTab("Basic",       null, m_basicTab,       null);
			m_topPanel.addTab("Weights",     null, m_weightsTab,     null);

			setTabStates();

			if (message == null) {
				if (m_ls.getDiagram().undoEnabled()) {
					message = "You might wish to disable undo/redo operations";
				} else {
					message = "You might wish to enable undo/redo operations";
			}	}

			m_message = new JLabel(message, JLabel.CENTER);
			m_message.setFont(font);
			m_message.setForeground(Color.RED);
			m_message.setSize(400,50);
			m_message.setPreferredSize(new Dimension(400,50));

			// --------------
			// Use a FlowLayout to center the button and give it margins.

			buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));

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
				buttonPanel.add(button);
			}

			contentPane.add(BorderLayout.NORTH,  m_topPanel);
			contentPane.add(BorderLayout.SOUTH,  buttonPanel);
			contentPane.add( BorderLayout.CENTER,m_message);

			// Resize the window to the preferred size of its components
			pack();
			setVisible(true);
		}

		public boolean ok()
		{
			return m_isok;
		}

		// ActionListener interface

		public void actionPerformed(ActionEvent ev)
		{
			Object	source;
			int		state, i;

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

				m_clustering_approach.setSelectedIndex(0);
				m_output_mode.setSelectedIndex(0);
				m_cluster_algorithm.setSelectedIndex(0);
				m_selection_method.setSelectedIndex(0);
				m_debug.setSelectedIndex(1);
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
				  "The Bunch clustering algorithm attempts to cluster nodes by using the external bunch command line interface\n" +
				  "This command line interface to bunch is named clue. For help and documentation on bunch please visit:\n" +
				  "http://serg.mcs.drexel.edu/bunch"			  				   
				 	  , "Help", JOptionPane.OK_OPTION);
				return;
			case BUTTON_OK:
				if (!m_weightsTab.isOk()) {
					m_message.setText("Weights must be integers");
					return;
				}
				m_weightsTab.setWeights();

				for (i = 0; i < m_textfield_tags.length; ++i) {
					m_textfield_currents[i] = m_textfields[i].getText();
				}
				for (i = 0; i < m_checkbox_tags.length; ++i) {
					m_checkbox_currents[i] = m_checkboxes[i].isSelected();
				}
				m_isok = true;
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
			synchronized(this) {
				System.err.println(Util.toLocaleString() + ": " + message);
	}	}	}

	protected void message(String string) 
	{
		log(string);
		JOptionPane.showMessageDialog(m_ls.getFrame(), 	string, "Error", JOptionPane.OK_OPTION);
	}

	public BunchClusterLayout(LandscapeEditorCore ls, LandscapeLayouter fallback) 
	{
		super(ls, fallback);

		m_debug.setSelectedIndex(1);
	}

	public String getName()
	{
		return "Bunch Cluster";
	}

	public String getMenuLabel() 
	{
		return "Bunch Cluster";
	} 

	public boolean isConfigurable()
	{
		return true;
	}

	public boolean isLayouter()
	{
		return false;
	}

	public boolean configure(LandscapeEditorCore ls, String message, Vector selected)
	{
		boolean ok;

		BunchClusterConfigure configure = new BunchClusterConfigure(this, message, selected);
		ok = configure.ok();
		configure.dispose();
		return ok;
	}

	public boolean configure(LandscapeEditorCore ls)
	{
		return configure(ls, null, null);
	}

/***************************************************************************/

	protected boolean write(Vector selectedBoxes, String exportname)
	{
		PrintWriter		ps = null;

		try {
			log("Exporting " + exportname);

		
			File				 exportfile  = new File(exportname);
			FileOutputStream	 os          = new FileOutputStream(exportfile);
			
			ps = new PrintWriter(os);
		} catch (Exception error) {
			message("Exception creating export stream " + exportname + ": " + error.getMessage());
			return(false);
		}

		boolean				leaves = parameterBoolean(LEAVES);

		Enumeration			en, en1;
		EntityInstance		e, e1;
		RelationInstance	ri;
		RelationClass		rc;
		int					weight;

		try {
			for (en = selectedBoxes.elements(); en.hasMoreElements(); ) {
				e = (EntityInstance) en.nextElement();
				e.orMark(EntityInstance.SPRING_MARK);
			}

			for (en = selectedBoxes.elements(); en.hasMoreElements(); ) {
				e = (EntityInstance) en.nextElement();
				if (leaves) {
					// Use real edges to determine relationships
					en1 = e.srcRelationElements();
					if (en1 != null)  {
						while (en1.hasMoreElements()) {
							ri     = (RelationInstance) en1.nextElement();
							rc     = ri.getRelationClass();
							weight = rc.getWeight();
							// Consider only visible edges when drawing layout
							if (weight == 0) {
								continue;
							}
							e1 = ri.getDst();
							if (!e1.isMarked(EntityInstance.SPRING_MARK)) {
								continue;
							}
							if (weight > 0) {
								if (weight == 1) {
									ps.println(e.getId() + " " + e1.getId());
								} else {
									ps.println(e.getId() + " " + e1.getId() + " " + weight);
								}
							} else {
								weight = -weight;
								if (weight == 1) {
									ps.println(e1.getId() + " " + e.getId());
								} else {
									ps.println(e1.getId() + " " + e.getId() + " " + weight);
							}	}
					}	}
				} else {
					// Use lifted edges to determine relationships
					en1 = e.srcLiftedRelationElements();
					if (en1 != null) {
						while (en1.hasMoreElements()) {
							ri     = (RelationInstance) en1.nextElement();
							rc     = ri.getRelationClass();
							weight = rc.getWeight(); 
							// Consider only visible edges when drawing layout
							if (weight == 0) {
								continue;
							}
							e1 = ri.getDrawDst();
							if (!e1.isMarked(EntityInstance.SPRING_MARK)) {
								continue;
							}
							if (weight > 0) {
								if (weight == 1) {
									ps.println(e.getId() + " " + e1.getId());
								} else {
									ps.println(e.getId() + " " + e1.getId() + " " + weight);
								}
							} else {
								weight = -weight;
								if (weight == 1) {
									ps.println(e1.getId() + " " + e.getId());
								} else {
									ps.println(e1.getId() + " " + e.getId() + " " + weight);
							}	}
			}	}	}	}

			ps.close();

			if (ps.checkError()) {
				message("An unknown error occurred writing output");
				return false;
			}

			log("Export written");
		}
		catch(Exception error) {
			message("Exception writing output: " + error.getMessage());
			return(false);
		}
		return true;
	}


	class EchoOutput implements Runnable {

		InputStream		m_inputStream = null;
		String			m_source;

		EchoOutput(String source, InputStream inputStream)
		{
			m_inputStream = inputStream;
			m_source      = source;
		}

		public void run()
		{

			BufferedReader		reader;
			InputStreamReader	isReader;
			String				source, s;

			source = m_source;
			try {
				isReader = new InputStreamReader(m_inputStream);
				reader   = new BufferedReader(isReader);

				while ((s = reader.readLine()) != null) {
					log(source + ": " + s);
				}
				reader.close();
			} catch (Exception error) {
				log(source + " input error: " + error.getMessage());
	}	}	}

	boolean waitFor(Process process) 
	{
		try {
			int	ret = process.waitFor();
			log("Process returned exit value of " + ret);
		} catch (Exception error) {
			message("WaitFor failed: " + error.getMessage());
			return false;
		}
		return true;
	}

	class ErrOutput implements Runnable {

		InputStream		m_inputStream = null;

		ErrOutput(InputStream inputStream)
		{
			m_inputStream = inputStream;
		}

		public void run()
		{

			BufferedReader		reader;
			InputStreamReader	isReader;
			String				s;

			try {
				isReader = new InputStreamReader(m_inputStream);
				reader   = new BufferedReader(isReader);

				while ((s = reader.readLine()) != null) {
					log(s);
				}
				reader.close();
			} catch (Exception error) {
				log("ErrOutput error: " + error.getMessage());
	}	}	}

	class RunBunch implements Runnable {

		Process			m_process      = null;
		boolean			m_waiting      = true;

		RunBunch()
		{
		}

		public synchronized Process getProcess()
		{
			while (m_waiting) {
				try {
					wait(1);
				} catch(Exception error) {
					message("wait error: " + error.getMessage());
				}	
			}
			return m_process;
		}

		public Process startCommand(String command)
		{
			Process process = null;

			// Execute the command
			log("Executing [" + command + "]");

			try {
				Runtime runtime = Runtime.getRuntime();
				if (runtime == null) {
					message("No runtime available");
				} else {
					process = runtime.exec(command);
				}
			} catch (Exception error) {
				log("Exception executing [" + command + "] " + error.getMessage());
				process = null;
			}	
			if (process != null) {
				ErrOutput	errOutput = new ErrOutput(process.getErrorStream());
				new Thread(errOutput).start();
				if (parameterBoolean(VANILLA)) {
					ErrOutput	stdOutput = new ErrOutput(process.getInputStream());
					new Thread(stdOutput).start();
				}
			}
			m_process = process;
			m_waiting = false;
			return process;
		}

		public void run()
		{
			String command = parameterString(COMMAND);
			startCommand(command);
	}	}

	protected boolean read(String importname, EntityInstance container, boolean collapse)
	{
		Diagram			diagram  = m_ls.getDiagram();
		FileReader		fileReader;
		BufferedReader	in;

		String			str, keyword, firstToken, secondToken;
		int				line, index, endindex;
		boolean			ok       = false;
		Hashtable		clusters = new Hashtable();
		EntityInstance	parent, cluster, e;

		log("Importing '" + importname + "'");

		try {
			fileReader = new FileReader(importname);
			in         = new BufferedReader(fileReader);  
		} catch (Exception error) {
			message("Exception opening " + importname + ": " + error.getMessage());
			return false;
		}

		line = 0;
		str  = "";
		try {
			while ((str = in.readLine()) != null) {
				++line;
				keyword = str.substring(0, 3);
				if (!keyword.equals("SS(")) {
					message("Expected to see 'contain ' but saw '" + keyword + "' in " + importname + " at line " + line);
					break;
				}
				index = str.indexOf(')', 3);
				if (index < 1) {
					message("First token missing in " + importname + " at line " + line);
					break;
				}

				firstToken  = str.substring(3, index);
				if (firstToken.equals("ROOT")) {
					if (str.equals("SS(ROOT) = ")) {
						message("Bunch found nothing worth clustering");
						continue;
				}	}

				cluster = (EntityInstance) clusters.get(firstToken);
				if (cluster == null) {
					cluster = diagram.updateNewEntity(null, container);
					cluster.setLabel(firstToken);
					clusters.put(firstToken, cluster);
				}
				for (index = str.indexOf('=', index); index > 0; index = endindex) {
					index += 2;
					endindex = str.indexOf(',', index);
					if (endindex < 0) {
						secondToken = str.substring(index);
					} else {
						secondToken = str.substring(index, endindex);
					}
				
					if (secondToken.length() < 1) {
						message("Second token missing in " + importname + " at line " + line);
						break;
					}
//					System.out.println("'" + firstToken + "' '" + secondToken + "'");
				
					e = (EntityInstance) clusters.get(secondToken);
					if (e == null) {
						e = diagram.getCache(secondToken);
					}
					if (e == null) {
						e = diagram.updateNewEntity(null, cluster);
						e.setLabel(secondToken);
						clusters.put(secondToken, e);
					} else {
						parent  = e.getContainedBy();
						diagram.updateMoveEntityContainment(cluster, e);
						if (collapse) {
							for (;;) {
								e      = parent;
								parent = e.getContainedBy(); 
								if (e.getFirstChild() != null) {
									break;
								}
								diagram.updateCutEntity(e);
			}	}	}	}	}
		} catch (Exception error) {
			message("Exception reading " + importname + ": " + error.getMessage());
		}

		if (str == null) {
			Enumeration en;

			for (en = clusters.elements(); en.hasMoreElements(); ) {
				e = (EntityInstance) en.nextElement();
				diagram.doRelayoutAll(e, false);
			}
			ok = true;
		}
		clusters.clear();

		try {
			in.close();
		} catch (Exception error) {
			message("Exception closing " + importname + ": " + error.getMessage());
			return false;
		}

		return ok;
	}

	protected void emitParameter(PrintWriter ps, String name, String value)
	{
		if (value != null) {
			value = value.trim();
			if (value.length() > 0) {
				ps.println(name + "=" + value);
	}	}	}

	protected void emitParameter(PrintWriter ps, String name, int arg)
	{
		String value = parameterString(arg);

		if (value != null) {
			value = value.trim();
			if (value.length() > 0) {
				ps.println(name + "=" + value);
	}	}	}

	public boolean doLayout1(Vector masterBoxes, EntityInstance parent) 
	{
		Vector				selectedBoxes;
		Enumeration			en;
		EntityInstance		e;
		boolean				leaves = parameterBoolean(LEAVES);
		
		String				exportname, command, importname;
		String				string, value, value1;
		int					i, j, match;
		boolean				calledConfigure = false;
		boolean				flag;

		if (!leaves) {
			selectedBoxes = masterBoxes;
		} else {
			selectedBoxes = new Vector();
			for (en = masterBoxes.elements(); en.hasMoreElements(); ) {
				e = (EntityInstance) en.nextElement();
				e.gatherLeaves(selectedBoxes);
		}	}
		
		int	size = selectedBoxes.size();
		
		if (size < 2) {
			// Not worth attempting to cluster
			m_ret = "Too few entities to reasonably cluster";
			return true;
		}

		for (;;) {
			exportname = parameterString(EXPORT);
			command    = parameterString(COMMAND);
			importname = parameterString(IMPORTFILE);

			string = null;
			if (exportname.length() == 0) {
				string = "Please specify an export file to write to";
			} 
			if (string == null) {
				break;
			}
			calledConfigure = true;
			if (!configure(m_ls, string, selectedBoxes)) {
				return true;
		}	}

		if (!calledConfigure) {
			// Have to be able to configure clients/libraries etc.
			if (!configure(m_ls, null, selectedBoxes)) {
				return true;
		}	}

		log("Using Bunch to cluster " + size + " items");

		if (!write(selectedBoxes, exportname)) {
			return false;
		}

		if (command.length() == 0) {
			m_ret = "Bunch output written to file";
			return true;
		}

		int		 debug    = m_debug.getSelectedIndex();
		boolean  vanilla  = parameterBoolean(VANILLA);
		RunBunch runBunch = new RunBunch();
		Process	 process;

		new Thread(runBunch).start();
		process = runBunch.getProcess();
		if (process == null) {
			return false;
		}

		if (!vanilla) {
			OutputStream		 os  = process.getOutputStream();	// Stdin of process
			PrintWriter			 ps  = new PrintWriter(os);

			// Write out the parameters

			log("Writing API instructions to bunch");


			// This should be the first thing conveyed across the interface
			if (debug > 0) {
				ps.println("DEBUG=" + g_debug[debug]);
			}

			emitParameter(ps, "MDG_INPUT_FILE_NAME", EXPORT);

			if (importname != null && importname.length() > 0) {
				if (importname.endsWith(".bunch")) {
					value       = importname.substring(0, importname.length()-6);
				} else {
					value       = importname;
					importname += ".bunch";
				}
				emitParameter(ps, "OUTPUT_FILE", value);
			} else {
				emitParameter(ps, "OUTPUT_DIRECTORY", IMPORTDIR);
			}

			ps.println("OUTPUT_FORMAT=Text");
			ps.println("OUTPUT_TREE=True");

			i = m_output_mode.getSelectedIndex();
			if (i >= 0) {
				ps.println("MDG_OUTPUT_MODE=" + g_output_mode[i]);
			}


			i = m_clustering_approach.getSelectedIndex();
			if (i >= 0) {
				ps.println("CLUSTERING_APPROACH=" + g_clustering_approach[i]);
			}


			i = m_cluster_algorithm.getSelectedIndex();
			if (i >= 0) {
				ps.println("CLUSTERING_ALG=" + g_cluster_algorithm[i]);

				switch (i) {
				case 0:	// Hill climbing

					emitParameter(ps, "ALG_HC_POPULATION_SZ", HC_POPULATION_SIZE);
					emitParameter(ps, "ALG_HC_HC_PCT",		  HC_SEARCH_SPACE);
					emitParameter(ps, "ALG_HC_RND_PCT",		  HC_RANDOMIZE);
					if (parameterBoolean(ANNEALING)) {
						ps.println("ALG_HC_SA_CLASS=" + g_techniques[0]);
						value  = parameterString(HC_INITIAL_TEMP);
						value1 = parameterString(HC_ALPHA);
						if (value == null) {
							value = "";
						}
						if (value1 == null) {
							value1 = "";
						}
						value  = value.trim();
						value1 = value1.trim();
						if (value.length() > 0 || value1.length() > 0) {
							ps.print("ALG_HC_SA_CONFIG=");
							flag = false;
							if (value.length() > 0) {
								ps.print("InitialTemp=" + value);
								flag = true;
							}
							if (value1.length() > 0) {
								if (flag) {
									ps.print(",");
								}
								ps.print("Alpha=" + value1);
						}	}
					}
					break;
				case 1:	// Genetic

					i = m_selection_method.getSelectedIndex();
					if (i >= 0) {
						ps.println("ALG_GA_SELECTION_METHOD=" + g_selection_method[i]); 
					}

					emitParameter(ps,"ALG_GA_NUM_GENERATIONS",	GA_GENERATIONS);
					emitParameter(ps,"ALG_GA_POPULATION_SZ",	GA_POPULATION_SIZE);
					emitParameter(ps,"ALG_GA_CROSSOVER_PROB",	GA_CROSSOVER_PROB);
					emitParameter(ps,"ALG_GA_MUTATION_PROB",	GA_MUTATION_PROB);
					break;
				}
			}

			i = m_calculator_classes.getSelectedIndex();
			if (i >= 0) {
				ps.println("MQ_CALCULATOR_CLASS=" + g_calculator_classes[i]);
			}

			emitParameter(ps, "USER_DIRECTED_CLUSTER_SIL", USERFILE);
			if (parameterBoolean(LOCK_USER)) {
				emitParameter(ps, "LOCK_USER_SET_CLUSTERS", "True");
			}

			if (parameterBoolean(TIMEOUT)) {
				emitParameter(ps, "TIMEOUT_TIME", MAXRUNTIME);
			}

			for (i = 0; i < g_omnipresents.length; ++i) {
				flag  = false;
				match = g_omnipresents[i];
				for (j = 0; j < selectedBoxes.size(); ++j) {
					e = (EntityInstance) selectedBoxes.elementAt(j);
					if (e.getOmnipresent() == match) {
						if (!flag) {
							ps.print(g_omnipresent_names[i]+"=");
							flag = true;
						} else {
							ps.print(",");
						}
						ps.print(e.getId());
				}	}
				if (flag) {
					ps.println("");
			}	}

			ps.close();

			InputStream is = process.getInputStream();	// Stdout of process
			int			c  = ' ';

			try {
				c = is.read();
			} catch (Exception error) {
			}
			if (c != 'O') {
				message("Bunch execution failed");
				return false;
			}
		}

		if (!waitFor(process)) {
			return false;
		}

		if (importname != null && importname.length() > 0) {
			if (!read(importname, parent, leaves)) {
				return false;
			}
			log("Import loaded");
			m_ret = "Graph redrawn using Bunch";

			if (parameterBoolean(DELETEIMPORT)) {
				try {
					File importfile = new File(importname);
					if (!importfile.delete()) {
						message("Unable to delete '" + importfile + "'");
					} else {
						log("Deleted " + importfile);
					}
				} 
				catch (Exception error) {
					message("Exception deleting '" + importname + "' " + error.getMessage());
			}	}
		} else {
			m_ret = "No import file name specified";
		}

		if (exportname.length() != 0 && parameterBoolean(DELETEEXPORT)) {
			try {
				File exportfile = new File(exportname);

				if (!exportfile.delete()) {
					message("Unable to delete '" + exportfile + "'");
				} else {
					log("Deleted " + exportfile);
			}	}
			catch (Exception error) {
				message("Exception deleting '" + exportname + "' " + error.getMessage());
		}	}
		return true;
	} 

	public String doLayout(Diagram dg) 
	{
		LandscapeEditorCore	ls = m_ls;
		EntityInstance		parent;

		m_dg = dg;

		// get user's selection of boxes to be laid out

		Vector masterBoxes = dg.getClusterGroup();
		if (masterBoxes == null) {
			  Util.beep();
			  return "No group selected";
		}

		parent = parentOfSet(masterBoxes);
		if (parent == null) {
			return	"Cluster layout requires that all things laid out share same parent";
		}

		m_ret = "Bunch Cluster layout aborted";
		try {
			ls.doLayout1(this, masterBoxes, parent, false);
		} catch (Exception e) {
			StackTraceElement[] stack = e.getStackTrace();
			int					i;

			System.out.println(e);
			System.out.println(e.getMessage());
			for (i = stack.length; i > 0; ) {
				System.out.println(stack[--i].toString());
			}
			m_ret = "Bunch Cluster layout threw exception";
		}
		return m_ret;
	} 

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





