package lsedit;

import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
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
import java.io.PrintWriter;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ACDCClusterLayout extends LandscapeLayouter implements ToolBarEventHandler {

	public final static String[] g_pattern = new String[]
								{
									"Body/Subgraph/Orphan",
									"Body/Orphan/Subgraph",
									"Body/Subgraph",
									"Body/Orphan",
									"Body",
									"Subgraph/Body/Orphan",
									"Subgraph/Orphan/Body",
									"Subgraph/Body",
									"Subgraph/Orphan",
									"Subgraph",
									"Orphan/Body/Subgraph",
									"Orphan/Subgraph/Body",
									"Orphan/Subgraph",
									"Orphan/Body",
									"Orphan"
								};

	public final static String[] g_patterncode = new String[]
								{
									"bso",
									"bos",
									"bs",
									"bo",
									"b",
									"sbo",
									"sob",
									"sb",
									"so",
									"s",
									"obs",
									"osb",
									"os",
									"ob",
									"o"
								};


	public final static String[] g_filter = new String[]
								{
									"All",					/* -a      */
									"Files within Modules",	/* default */
									"Files"					/* -u      */
								};


	public final static String[] g_debugs = new String[]
								{
									"Run Silently",
									"Minimal output",		/* -d1 */
									"Verbose debugging"		/* -d2 */
								};

	protected final static	int	COMMAND   = 0;
	protected final static  int	EXPORT    = 1;
	protected final static  int	IMPORT    = 2;
	protected final static  int	MAXSIZE   = 3;
	protected final static  int	CONTAINER = 4;


	protected final static String[] m_textfield_tags = 
							{
								"acdc:command",
								"acdc:export",
								"acdc:import",
								"acdc:maxsize",
								"acdc:container"
							};

	protected final static String[] m_textfield_titles = 
							{
								"Command to execute ACDC:",
								"Exported file used by command:",
								"Imported file created by command:",
								"Optional maximum cluster size:",
								"Optional container:"
							};

	protected final static String[]	m_textfield_resets = 
							{ 
								Util.javaCommand() + "acdc.ACDC",
								"junk.ta",
								"junk.rsf",
								"",
								""
							};

	protected static String[] m_textfield_defaults = 
							{ 
								Util.javaCommand() + "acdc.ACDC",
								"junk.ta",
								"junk.rsf",
								"",
								""
							};

	protected static String[] m_textfield_currents = 
							{ 
								Util.javaCommand() + "acdc.ACDC",
								"junk.ta",
								"junk.rsf",
								"",
								""
							};

	protected final static	int	DELETEEXPORT  = 0;
	protected final static  int	DELETEIMPORT  = 1;
	protected final static  int	LEAVES        = 2;
	protected final static  int	FEEDBACK      = 3;
	protected final static  int GUI           = 4;

	protected final static String[] m_checkbox_tags = 
							{
								"acdc:deleteExport",
								"acdc:deleteImport",
								"acdc:leaves",
								"acdc:feedback",
								"acdc:gui"
							};

	protected final static String[] m_checkbox_titles = 
							{
								"Delete export file",
								"Delete import file",
								"Cluster leaves",
								"Feedback",
								"Popup gui"
							};

	protected final static boolean[] m_checkbox_resets = 
							{ 
								false,
								false,
								true,
								true,
								false
							};
	protected static boolean[]m_checkbox_defaults = 
							{ 
								false,
								false,
								true,
								true,
								false
							};

	protected static boolean[]m_checkbox_currents = 
							{ 
								false,
								false,
								true,
								true,
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

	protected JComboBox		m_pattern   = new JComboBox(g_pattern);
	protected JComboBox		m_filter    = new JComboBox(g_filter);
	protected JComboBox		m_debug     = new JComboBox(g_debugs);

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
		return "acdc:";
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
					m_textfield_currents[i] = value;
				}
				return;
		}	}
		checkbox_tags = m_checkbox_tags;
		for (i = 0; i < checkbox_tags.length; ++i) {
			if (attribute.equals(checkbox_tags)) {
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
		}
	}	

	class ACDCClusterConfigure extends JDialog implements ActionListener {

		protected JTextField[]	m_textfields;
		protected JCheckBox[]	m_checkboxes;
		protected JButton[]		m_buttons;
		protected JLabel		m_message;
		protected boolean		m_isok;
		
		public ACDCClusterConfigure(ACDCClusterLayout layout, String message)
		{
			super(layout.getLs().getFrame(), layout.getName() + " Configuration", true);

			Container			contentPane;
			JPanel				topPanel, labelPanel, valuePanel, bottomPanel, buttonPanel;
			GridLayout			gridLayout;
			JTextField			textfield;
			Font				font, bold;
			JLabel				label;
			int					i;
			String				string, tip;
			JCheckBox			checkbox;
			JButton				button;

			m_isok       = false;
			font         = FontCache.getDialogFont();
			bold         = font.deriveFont(Font.BOLD);

			contentPane = getContentPane();
			contentPane.setLayout(new BorderLayout());

			setForeground(ColorCache.get(0,0,0));
			setBackground(ColorCache.get(192,192,192));
			setFont(font);

			topPanel    = new JPanel();
			topPanel.setLayout(new BorderLayout());

			labelPanel  = new JPanel();
			gridLayout  = new GridLayout(8, 1, 0, 10);
			labelPanel.setLayout(gridLayout);

			valuePanel  = new JPanel();
			gridLayout  = new GridLayout(8, 1, 0, 10);
			valuePanel.setLayout(gridLayout);
	
			m_textfields = new JTextField[m_textfield_tags.length];
			for (i = 0; i < m_textfield_tags.length; ++i) {
				m_textfields[i] = textfield = new JTextField(m_textfield_currents[i],  60);
				label = new JLabel(m_textfield_titles[i], JLabel.RIGHT);
				label.setFont(bold);
				labelPanel.add(label);
				textfield.setFont(font);
				valuePanel.add(textfield);
			}

			label = new JLabel("Apply patterns:", JLabel.RIGHT);
			label.setFont(bold);
			labelPanel.add(label);
			m_pattern.setFont(bold);
			valuePanel.add(m_pattern);

			label = new JLabel("Emit as output: ", JLabel.RIGHT);
			label.setFont(bold);
			labelPanel.add(label);
			m_filter.setFont(bold);
			valuePanel.add(m_filter);

			label = new JLabel("Tracing within ACDC:", JLabel.RIGHT);
			label.setFont(bold);
			labelPanel.add(label);
			m_debug.setFont(bold);
			valuePanel.add(m_debug);

			topPanel.add(BorderLayout.WEST, labelPanel);
			topPanel.add(BorderLayout.EAST, valuePanel);

			buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));

			m_checkboxes = new JCheckBox[m_checkbox_tags.length];
			for (i = 0; i < m_checkbox_tags.length; ++i) {
				m_checkboxes[i] = checkbox = new JCheckBox(m_checkbox_titles[i], m_checkbox_currents[i]);
				checkbox.setFont(font);
				buttonPanel.add(checkbox);
			}

			topPanel.add(BorderLayout.SOUTH, buttonPanel);

			bottomPanel = new JPanel();
			bottomPanel.setLayout(new BorderLayout());

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

			bottomPanel.add( BorderLayout.NORTH, m_message);
			bottomPanel.add( BorderLayout.SOUTH, buttonPanel);

			contentPane.add(BorderLayout.NORTH,  topPanel);
			contentPane.add(BorderLayout.SOUTH,  bottomPanel);

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
				m_pattern.setSelectedIndex(0);
				m_filter.setSelectedIndex(0);
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
				  "The ACDC clustering algorithm attempts to cluster nodes on the basis of software\n" +
				  "engineering patterns observed in how entities are connected by edges.  Patterns\n" +
				  "may be executed in an arbitrary order.  The body pattern places those nodes\n" +
				  "whose names end in '.c' or '.h' and which have the same prefix in a common cluster.\n" +
				  "The subgraph pattern clusters nodes based on their dependencies.  The orphan pattern\n" +
				  "places orphans not in any cluster in the cluster containing nodes that most frequently\n" +
				  "reference this orphan.\n\n" +
				  "It is recommended that 'All' output generated by ACDC be emitted.  When this option is\n" +
				  "selected all clusters generated by ACDC may optionally be placed under the named container.\n\n" +
				  "If 'files' or 'files within modules' is specified ACDC will produce reduced output in which\n" +
				  "much of the clustering information computed by ACDC may be discarded before it generates\n" +
				  "any output. Nodes of type 'File'/'cFile' will be output when either option is specified\n" +
				  "with these nodes only occurring under nodes of type 'cModule'/'Subsystem'/'cSubSystem'\n" +
				  "when modules are also requestd.  These two options will result in ACDC producing an empty\n" +
				  "output file when none of the nodes to be clustered have such a type.  In such cases\n" +
				  "no clustering will be performed by LSEdit, since it will be provided no instructions on\n" +
				  "how clustering is to be performed.  Even in cases where ACDC does produce output it is\n" +
				  "stressed that while the output from ACDC might suggest that most of the input used to drive\n" +
				  "ACDC should be discarded, LSEdit will merely move documented nodes and all their descendants\n" +
				  "into the specified containment hierachy documented in this output file.\n\n" +
				  "The gui checkbox instructs ACDC to show its progress graphically as clustering is performed\n" +
				  "and exists primarily as a debugging aid." 
			  				   
				 	  , "Help", JOptionPane.OK_OPTION);
				return;
			case BUTTON_OK:
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


	public ACDCClusterLayout(LandscapeEditorCore ls, LandscapeLayouter fallback) 
	{
		super(ls, fallback);

		m_debug.setSelectedIndex(1);
	}


	public String getName()
	{
		return "ACDC Cluster";
	}

	public String getMenuLabel() 
	{
		return "ACDC Cluster";
	} 

	public boolean isConfigurable()
	{
		return true;
	}

	public boolean isLayouter()
	{
		return false;
	}

	public boolean configure(LandscapeEditorCore ls, String message)
	{
		boolean ok;

		ACDCClusterConfigure configure = new ACDCClusterConfigure(this, message);
		ok = configure.ok();
		configure.dispose();
		return ok;
	}

	public boolean configure(LandscapeEditorCore ls)
	{
		return configure(ls, null);
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

		try {
//			ps.print("// ACDC TA file written by LSEdit " + Version.Number() + "\n\n");
		
			ps.print("FACT TUPLE :\n");

			for (en = selectedBoxes.elements(); en.hasMoreElements(); ) {
				e = (EntityInstance) en.nextElement();
				e.writeInstance(ps);
				e.orMark(EntityInstance.SPRING_MARK);
			}


			for (en = selectedBoxes.elements(); en.hasMoreElements(); ) {
				e = (EntityInstance) en.nextElement();
				if (leaves) {
					// Use real edges to determine relationships
					en1 = e.srcRelationElements();
					if (en1 != null) {
						while (en1.hasMoreElements()) {
							ri = (RelationInstance) en1.nextElement();
							// Consider only visible edges when drawing layout
							if (!ri.isRelationShown()) {
								continue;
							}
							e1 = ri.getDst();
							if (!e1.isMarked(EntityInstance.SPRING_MARK)) {
								continue;
							}
							ri.writeRelation(ps); 
					}	}
				} else {
					// Use lifted edges to determine relationships
					en1 = e.srcLiftedRelationElements();
					if (en1 != null) {
						while (en1.hasMoreElements()) {
							ri = (RelationInstance) en1.nextElement();
							// Consider only visible edges when drawing layout
							if (!ri.isRelationShown()) {
								continue;
							}
							e1 = ri.getDrawDst();
							if (!e1.isMarked(EntityInstance.SPRING_MARK)) {
								continue;
							}
							ri.writeRelation(ps); 
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

	protected boolean read(String importname, EntityInstance container, boolean collapse)
	{
		Diagram			diagram  = m_ls.getDiagram();
		FileReader		fileReader;
		BufferedReader	in;

		String			str, keyword, firstToken, secondToken;
		int				line, index;
		boolean			ok       = false;
		Hashtable		clusters = new Hashtable(20);
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
				keyword = str.substring(0, 8);
				if (!keyword.equals("contain ")) {
					message("Expected to see 'contain ' but saw '" + keyword + "' in " + importname + " at line " + line);
					break;
				}
				index = str.indexOf(' ', 8);
				if (index < 1) {
					message("First token missing in " + importname + " at line " + line);
					break;
				}

				firstToken  = str.substring(8, index);
				secondToken = str.substring(index+1);
				if (secondToken.length() < 1) {
					message("First token missing in " + importname + " at line " + line);
					break;
				}
//				System.out.println("'" + firstToken + "' '" + secondToken + "'");
				
				if (firstToken.equals(secondToken)) {
					continue;
				}
				cluster = (EntityInstance) clusters.get(firstToken);
				if (cluster == null) {
					cluster = diagram.updateNewEntity(null, container);
					cluster.setLabel(firstToken);
					clusters.put(firstToken, cluster);
				}

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
				}	}	}
			}

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

	public boolean doLayout1(Vector masterBoxes, EntityInstance parent)
	{
		Vector				selectedBoxes;
		Enumeration			en;
		EntityInstance		e;
		boolean				leaves = parameterBoolean(LEAVES);
		String				exportname, command, importname, maxsize;
		String				string;
		int					imaxsize;

		if (!leaves) {
			selectedBoxes = masterBoxes;
		} else {
			selectedBoxes = new Vector();
			for (en = masterBoxes.elements(); en.hasMoreElements(); ) {
				e = (EntityInstance) en.nextElement();
				e.gatherLeaves(selectedBoxes);
		}	}


		for (;;) {
			exportname = parameterString(EXPORT);
			command    = parameterString(COMMAND);
			importname = parameterString(IMPORT);
			maxsize    = parameterString(MAXSIZE);
			maxsize    = maxsize.trim();
			imaxsize   = 0;

			string = null;
			if (exportname.length() == 0) {
				string = "Please specify an export file to write to";
			} else if (importname.length() != 0 && command.length() == 0) {
				string = "Please specify the command to execute ACDC";
			} else if (command.length() != 0 && importname.length() == 0) {
				string = "Please specify an import file to read from";
			} else if (maxsize.length() > 0) {
				try {
					imaxsize = Integer.parseInt(maxsize);
					if (imaxsize < 1) {
						string = "Maxsize size of a cluster must be positive";
					}
				} catch (Throwable exception) {
					string = "Max cluster size '" + maxsize + "' must be an integer";
			}	}
			
			if (string == null) {
				break;
			}
			if (!configure(m_ls, string)) {
				return true;
		}	}

		if (!exportname.endsWith(".ta")) {
			exportname += ".ta";
		}
		if (!importname.endsWith(".rsf")) {
			importname += ".rsf";
		}

		int	size = selectedBoxes.size();
		
		if (size < 3) {
			// Not worth attempting to cluster
			m_ret = "Too few entities to reasonably cluster";
			return true;
		}

		log("Using ACDC to cluster " + size + " items");

		if (!write(selectedBoxes, exportname)) {
			return false;
		}

		if (command.length() == 0) {
			m_ret = "ACDC output written to file";
			return true;
		}
		
		String	s       = command + " " + exportname + " " + importname;
		int		index;
		String	s1;

		index = m_pattern.getSelectedIndex();
		if (index < 0) {
			index = 0;
		}
		s += " +" + g_patterncode[index];

		index = m_filter.getSelectedIndex();
		switch (index) {
		case 0:
			s += " -a";
			s1 = parameterString(CONTAINER);
			s1 = s1.replaceAll("[ \t\n\r\f]",""); 
			s += s1;
			break;
		case 2:
			s += " -u";
			break;
		}

		if (imaxsize > 0) {
			s += " -l" + imaxsize;
		}

		if (parameterBoolean(GUI)) {
			s += " -t";
		}

		index = m_debug.getSelectedIndex();
		if (index > 0) {
			s += " -d" + index;
		}

		Process process = null;
			
		log("Executing [" + s + "]");

		try {
			Runtime runtime = Runtime.getRuntime();
			if (runtime == null) {
				message("No runtime available");
				return false;
			} else {
				EchoOutput	output;

				process = runtime.exec(s);
				output  = new EchoOutput("ACDC Stdout", process.getInputStream());
				new Thread(output).start();

				output  = new EchoOutput("ACDC Stderr", process.getErrorStream());
				new Thread(output).start();
			}
		} catch (Exception error) {
			log("Exception executing [" + s + "] " + error.getMessage());
			process = null;
			return false;
		}	

		if (!waitFor(process)) {
			return false;
		}
		
		if (!read(importname, parent, leaves)) {
			return false;
		}

		log("Import loaded");

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

		m_ret = "Graph redrawn using ACDC Cluster Layout";
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

		m_ret = "ACDC Cluster layout aborted";

		ls.doLayout1(this, masterBoxes, parent, false);
		return m_ret;
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





