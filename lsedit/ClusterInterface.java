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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;

public class ClusterInterface extends LandscapeLayouter implements ToolBarEventHandler {

	protected final static	int	COMMAND   = 0;
	protected final static  int	EXPORT    = 1;
	protected final static  int	IMPORT    = 2;


	protected final static String[] m_textfield_tags = 
							{
								"clusterinterface:command[",
								"clusterinterface:export[",
								"clusterinterface:import["
							};

	protected final static String[] m_textfield_titles = 
							{
								"Command to be executed:",
								"Exported file used by command:",
								"Imported file created by command:"
							};

	protected final static String[]	m_textfield_resets = 
							{ 
								Util.javaCommand() + "lsedit.LsClusterer",
								"",
								""
							};

	protected static String[] m_textfield_defaults = 
							{ 
								Util.javaCommand() + "lsedit.LsClusterer",
								"",
								""
							};

	protected static String[] m_textfield_currents = 
							{ 
								Util.javaCommand() + "lsedit.LsClusterer",
								"",
								""
							};

	protected final static	int	DELETEEXPORT  = 0;
	protected final static  int	DELETEIMPORT  = 1;
	protected final static  int	LEAVES        = 2;
	protected final static  int	FEEDBACK      = 3;

	protected final static String[] m_checkbox_tags = 
							{
								"clusterinterface:deleteExport[",
								"clusterinterface:deleteImport[",
								"clusterinterface:leaves[",
								"clusterinterface:feedback["
							};

	protected final static String[] m_checkbox_titles = 
							{
								"Delete export file",
								"Delete import file",
								"Cluster leaves",
								"Feedback"
							};

	protected final static boolean[] m_checkbox_resets = 
							{ 
								false,
								false,
								true,
								true
							};

	protected static boolean[]	m_checkbox_defaults = 
							{ 
								false,
								false,
								true,
								true
							};

	protected static boolean[]	m_checkbox_currents = 
							{ 
								false,
								false,
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

	// Working registers
	protected Diagram		m_dg;
	private	String			m_ret;


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
		return "clusterinterface:";
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

	class ClusterInterfaceDialog extends JDialog implements ActionListener {

		protected JTextField[]	m_textfields;
		protected JCheckBox[]	m_checkboxes;
		protected JButton[]		m_buttons;
		protected JLabel		m_message;
		protected boolean		m_isok;
		
		public ClusterInterfaceDialog()
		{
			super(m_ls.getFrame(), "Invoke external clustering tool", true);

			LandscapeEditorCore	ls = m_ls;
			Container			contentPane;
			Font				font, bold;
			JLabel				label;
			JTextField			textfield;
			JCheckBox			checkbox;
			JButton				button;
			String				string, tip;
			int					i;

			m_isok = false;
			font   = FontCache.getDialogFont();
			bold   = font.deriveFont(Font.BOLD);

			setForeground(ColorCache.get(0,0,0));
			setBackground(ColorCache.get(192,192,192));
			setFont(font);

			JPanel topPanel    = new JPanel();
			JPanel labelPanel  = new JPanel();
			JPanel valuePanel  = new JPanel();
		

			GridLayout gridLayout;

			topPanel.setLayout(new BorderLayout());
			gridLayout = new GridLayout(3, 1, 0, 10);
			labelPanel.setLayout(gridLayout);
			gridLayout = new GridLayout(3, 1, 0, 10);
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

			JPanel optionsPanel = new JPanel();
			optionsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));

			m_checkboxes = new JCheckBox[m_checkbox_tags.length];
			for (i = 0; i < m_checkbox_tags.length; ++i) {
				m_checkboxes[i] = checkbox = new JCheckBox(m_checkbox_titles[i], m_checkbox_currents[i]);
				checkbox.setFont(font);
				optionsPanel.add(checkbox);
			}

			topPanel.add(BorderLayout.WEST, labelPanel);
			topPanel.add(BorderLayout.EAST, valuePanel);
			topPanel.add(BorderLayout.SOUTH, optionsPanel);

			contentPane = getContentPane();
			contentPane.add( BorderLayout.NORTH, topPanel );

			m_message = new JLabel(" ", JLabel.CENTER);

			if (ls.getDiagram().undoEnabled()) {
				string = "You might wish to disable undo/redo operations";
			} else {
				string = "You might wish to enable undo/redo operations";
			}

			m_message.setText(string);
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
				  "To merely export a file for later clustering provide an export filename.\n" +
				  "To run an external clusterer provide the command that invokes that external tool.\n" +
				  "If no export file specifying what is to be clustered is provided, this\n" +
				  "information will be piped directly to the specified commands standard input.\n" +
				  "The provided command is expected to write instructions as to how clustering is to\n" +
				  "be performed to the specified import file.  If no such file is specified it will\n" +
				  "be presumed that this input is to be read directly from the provided commands\n" +
				  "standard output stream.  Temporary files that are created may optionally be deleted\n" +
				  "when the clustering interface has no further use for them.\n" +
				  "\n" +
				  "The export file contains TA specifying the nodes to be clustered and the edges\n" +
				  "between them considered relevant when performing that clustering.  The import file\n" +
				  "should contain suitably structured TA showing how these exported nodes are to be\n" +
				  "clustered.  New containment hierarchies in the imported data will be constructed\n" +
				  "and the relevant nodes contained within such imported hierarchies moved into these\n" +
				  "hierarchies.  If sizing and positioning information is provided in the imported data\n" +
				  "this information will be mirrored in the resulting internal TA." 
					  , "Help", JOptionPane.OK_OPTION);
				return;
			case BUTTON_OK:
				for (i = 0; i < m_textfield_tags.length; ++i) {
					m_textfield_currents[i] = m_textfields[i].getText();
				}
				for (i = 0; i < m_checkbox_tags.length; ++i) {
					m_checkbox_currents[i]= m_checkboxes[i].isSelected();
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

	protected String message(String string) 
	{
		log(string);
		JOptionPane.showMessageDialog(m_ls.getFrame(), 	string, "Error", JOptionPane.OK_OPTION);
		return string;
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
				log("Syserr input error: " + error.getMessage());
	}	}	}

	class WriteOutput implements Runnable {

		Process			m_process = null;
		InputStream		m_inputStream = null;
		boolean			m_waiting = true;

		Vector			m_selectedBoxes;

		WriteOutput(Vector selectedBoxes)
		{
			m_selectedBoxes = selectedBoxes;
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

		public InputStream getInputStream()
		{
			return m_inputStream;
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

				m_inputStream  = process.getInputStream();	// Stdout of process
			}
			m_process = process;
			m_waiting = false;
			return process;
		}

		public boolean write()
		{
			Vector			selectedBoxes = m_selectedBoxes;
			String			exportname    = parameterString(EXPORT);
			String			command       = parameterString(COMMAND);
			Diagram			dg            = m_ls.getDiagram();


			Process			process    = null;
			File			exportfile = null;
			PrintWriter		ps;

			try {
				OutputStream	os;

				if (exportname.length() == 0) {
					log("Piping output to subprocess");
					process = startCommand(command);
					if (process == null) {
						return false;
					}
					os = process.getOutputStream();
				} else {
					log("Exporting " + exportname);
		
					exportfile  = new File(exportname);
					os          = new FileOutputStream(exportfile);
				}
				ps  = new PrintWriter(os);
			} catch (Exception error) {
				message("Exception creating output stream " + exportname + ": " + error.getMessage());
				return(false);
			}

			Enumeration			en, en1;
			EntityInstance		e, e1;
			RelationInstance	ri;
			RelationClass		containsClass;
			boolean				leaves = parameterBoolean(LEAVES);

			try {
				ps.println("// Clusting TA file written by LSEdit " + Version.Number());
				ps.println();

				dg.writeSchemeTuples(ps);
				dg.writeSchemeAttributes(ps);
				ps.println();
				ps.println();
				ps.println("FACT TUPLE :");
				ps.println();
				
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

				ps.println();
				ps.println();
				ps.println("FACT ATTRIBUTE :");
				ps.println(); 

				containsClass = dg.getPrimaryContainsClass();

				for (en = selectedBoxes.elements(); en.hasMoreElements(); ) {
					e = (EntityInstance) en.nextElement();
					e.writeAttribute(ps, containsClass);
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
								ri.writeAttributes(ps); 

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
								ri.writeAttributes(ps); 
				}	}	}	}

				ps.flush();
				ps.close();

				log("Export written");
			}
			catch(Exception error) {
				message("Exception writing output: " + error.getMessage());
				return(false);
			}

			if (exportname.length() != 0 && command.length() != 0) {
				process = startCommand(command);
				if (process == null) {
					return false;
			}	}
			return true;
		}

		public void run()
		{
			write();
	}	}

	protected void transferNodes(EntityInstance parent, EntityInstance e /* other */)
	{
		Enumeration		en;
		EntityInstance	e1    = null;
		EntityInstance	child = null;

		// This part builds the new containers
		// It is presumed that the input has no edges to/from containers

		for (en = e.getChildren(); en.hasMoreElements(); ) {
			if (child == null) {
				e1 = m_dg.updateClusterEntity(parent, e);
			}
			child = (EntityInstance) en.nextElement();
			transferNodes(e1, child);
		}
		if (child == null) {
			// This is a leaf node in the import
			String			id    = e.getId();
			EntityInstance	match = m_dg.getCache(id);
			e1 = m_dg.updateImportEntity(parent, e, match);
			e1.nandMark(EntityInstance.SPRING_MARK);
		}
		// Signal that parent being used
		parent.nandMark(EntityInstance.SPRING_MARK);
	}

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

	ClusterInterface(LandscapeEditorCore ls, LandscapeLayouter fallback)
	{
		super(ls, fallback);
	}

	public String getName()
	{
		return "Cluster Interface";
	}

	public String getMenuLabel()
	{
		return "Cluster using program";
	}


	public boolean doLayout1(Vector masterBoxes, EntityInstance parent)
	{
		String	exportname = parameterString(EXPORT);
		String  command    = parameterString(COMMAND);
		String	importname = parameterString(IMPORT);

		if (exportname.length() == 0 && command.length() == 0) {
			m_ret = message("Please specify an export file to write to or command to pipe to");
			return true;
		}
		
		Vector				selectedBoxes;
		Enumeration			en;
		EntityInstance		e;
		boolean				leaves = parameterBoolean(LEAVES);

		if (!leaves) {
			selectedBoxes = masterBoxes;
		} else {
			selectedBoxes = new Vector();
			for (en = masterBoxes.elements(); en.hasMoreElements(); ) {
				e = (EntityInstance) en.nextElement();
				e.gatherLeaves(selectedBoxes);
		}	}		

		WriteOutput	writeOutput = new WriteOutput(selectedBoxes);

		if (command.length() == 0) {
			writeOutput.write();
			m_ret = message("Export written to '" + exportname + "'. No command specified");
			return true;
		}

		Ta		ta     = new Ta(m_ls);
		
		InputStream	is;
		Process		process;
		String		result;

		// Import the result

		if (importname.length() == 0) {
			new Thread(writeOutput).start();
			process = writeOutput.getProcess();
			if (process == null) {
				return false;
			}
			log("Reading subprocess output as our input");
			is = writeOutput.getInputStream();

			result = ta.loadTA("", is);
			if (result != null) {
				m_ret = message(result);
				return false;
			}
			log("Import loaded");
			if (!waitFor(process)) {
				return false;
			}

		} else {
			if (!writeOutput.write()) {
				return false;
			}
			process = writeOutput.getProcess();
			if (process == null) {
				return false;
			}
			if (!waitFor(process)) {
				return false;
			}
		
			log("Importing '" + importname);
			result = ta.loadTA(importname, null);
			if (result != null) {
				m_ret = message(result);
				return false;
			}
			log("Import loaded");

			if (parameterBoolean(DELETEIMPORT)) {
				try {
					File importfile = new File(importname);

					if (!importfile.delete()) {
						message("Unable to delete '" + importfile + "'");
					} else {
						message("Deleted " + importfile);
					}
				} 
				catch (Exception error) {
					message("Exception deleting '" + importname + "' " + error.getMessage());
		}	}	}

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

		log("Reclustering");

		if (leaves) {
			for (en = masterBoxes.elements(); en.hasMoreElements(); ) {
				e = (EntityInstance) en.nextElement();
				e.orMark(EntityInstance.SPRING_MARK);
		}	}

		EntityInstance root = ta.getRootInstance();
		EntityInstance child;

		for (en = root.getChildren(); en.hasMoreElements(); ) {
			child = (EntityInstance) en.nextElement();
			transferNodes(parent /* My TA */, child /* Other TA */);
		}

		// Make sure that we leave things as entered
		for (en = selectedBoxes.elements(); en.hasMoreElements(); ) {
			e = (EntityInstance) en.nextElement();
			e.nandMark(EntityInstance.SPRING_MARK);
		}

		if (leaves) {
			log("Removing redundant containers");

			/* These items were marked and are not things in selectedBoxes
			 * and have had nothing added back into/under them if SPRING_MARK
			 * still set
			 */

			for (en = masterBoxes.elements(); en.hasMoreElements(); ) {
				e = (EntityInstance) en.nextElement();
				if (e.isMarked(EntityInstance.SPRING_MARK)) {
					m_dg.updateCutEntity(e);
		}	}	}

		m_ret = "Clustering operation complete";
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
			return "Selected items do not have the same parent entity";
		}

		m_ret = "Clustering attempt failed";
		ls.doLayout1(this, masterBoxes, parent, false);
		return m_ret;

	} // doLayout


	public boolean configure(LandscapeEditorCore ls)
	{
		boolean ok;

		ClusterInterfaceDialog configure = new ClusterInterfaceDialog();
		ok = configure.ok();
		configure.dispose();
		return ok;
	}

	public boolean isConfigurable()
	{
		return true;
	}

	public boolean isLayouter()
	{
		return false;
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











