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
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;


public class AAClusterLayout extends LandscapeLayouter implements ToolBarEventHandler {

	public final static int TYPE_COLUMN           = 0;
	public final static int	SOURCE_COLUMN         = 1;
	public final static int RELATION_COLUMN       = 2;
	public final static int	TARGET_COLUMN         = 3;
	public final static int	ATTRIBUTE_COLUMN      = 4;
	public final static int	USE_COLUMN            = 5;
	public final static int RULE_COMPONENTS       = 6;

	public final static String[] g_columnnames = new String[]
								{
									"Type",
									"Source",
									"Relation",
									"Target",
									"Attribute",
									"Construct"
								};

	public final static int TYPE_DELETE           = 0;
	public final static int TYPE_ANY              = 1;
	public final static int TYPE_CLIENT           = 2;
	public final static int TYPE_SUPPLIER         = 3;
	public final static int	TYPE_CLASS            = 4;
	public final static int TYPE_CLIENT_CLASS     = 5;
	public final static int TYPE_SUPPLIER_CLASS   = 6;

	public final static String[] g_typenames = new String[]
								{
									"Delete",
									"Any",
									"Client",
									"Supplier",
									"Class",
									"Client class",
									"Supplier class"
								};

	public final static String g_all_entities  = "ALL ENTITIES";
	public final static String g_all_relations = "ALL RELATIONS";
	public final static String g_null          = "";
	public final static String g_all           = "ALL";

	public final static String[] g_usenames = new String[]
								{
									"%a=%v",
									"%a",
									"%v",
									"%c:%a=%v",
									"%c:%a",
									"%c"
								};

	public final static String[] g_metrics = new String[]
								{
									"Jaccard",
									"Simple Matching",
									"Sorensen-Dice"
								};

	public final static String[] g_algorithms = new String[]
								{
									"Single Linkage",
									"Complete Linkage",
									"Weighted Average",
									"Unweighted Average"
								};

	public final static String[] g_debugs = new String[]
								{
									"Run Silently",
									"Minimal output",
									"Verbose debugging"
								};

	public final static String[] g_presets = new String[]
								{
									"Custom",
									"Connected nodes",
									"Cluster node types",
									"Parent node"
								};

	protected Vector		m_rules;
	protected MyJTable		m_table;

	protected final static	int	COMMAND  = 0;
	protected final static  int	EXPORT   = 1;
	protected final static  int	IMPORT   = 2;
	protected final static  int	CUTPOINT = 3;

	protected final static String[] m_textfield_tags = 
							{
								"aa:command",
								"aa:export",
								"aa:import",
								"aa:cutpoint"
							};

	protected final static String[] m_textfield_titles = 
							{
								"Command to execute AA:",
								"Exported file used by command:",
								"Imported file created by command:",
								"AA Cutpoint:"
							};

	protected final static String[]	m_textfield_resets = 
							{ 
								Util.javaCommand() + "aa.AA",
								"junk.mbd",
								"junk.rsf",
								"0.0"
							};

	protected static String[] m_textfield_defaults = 
							{ 
								Util.javaCommand() + "aa.AA",
								"junk.mbd",
								"junk.rsf",
								"0.0"
							};

	protected	static String[]	m_textfield_currents =
							{ 
								Util.javaCommand() + "aa.AA",
								"junk.mbd",
								"junk.rsf",
								"0.0"
							};

	protected final static	int	DELETEEXPORT  = 0;
	protected final static  int	DELETEIMPORT  = 1;
	protected final static  int	LEAVES        = 2;
	protected final static  int	FEEDBACK      = 3;

	protected final static String[] m_checkbox_tags = 
							{
								"aa:deleteExport",
								"aa:deleteImport",
								"aa:leaves",
								"aa:feedback"
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

	protected static boolean[] m_checkbox_defaults = 
							{ 
								false,
								false,
								true,
								true
							};

	protected static boolean[] m_checkbox_currents = 
							{ 
								false,
								false,
								true,
								true
							};

	static protected final int BUTTON_OK      = 0;
	static protected final int BUTTON_CANCEL  = 1;
	static protected final int BUTTON_ADD     = 2;
	static protected final int BUTTON_HELP    = 3;
	static protected final int BUTTON_UNDO    = 4;
	static protected final int BUTTON_DEFAULT = 5;
	static protected final int BUTTON_SET     = 6;
	static protected final int BUTTON_RESET   = 7;

	protected final static String[] m_button_titles =
							{
								"Ok",
								"Cancel",
								"Add rule",
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
								"Add a new rule",
								null,
								"Enable/disable undo",
								"Use remembered default",
								"Set default to current",
								"Set default to initial"
							};

	protected JComboBox		m_metric    = new JComboBox(g_metrics);
	protected JComboBox		m_algorithm = new JComboBox(g_algorithms);
	protected JComboBox		m_debug     = new JComboBox(g_debugs);
	protected JComboBox		m_presets   = new JComboBox(g_presets);

	// Working registers

	protected String		m_ret;


	protected static String	parameterString(int i)
	{
		return m_textfield_currents[i];
	}

	protected static boolean	parameterBoolean(int i)
	{
		return m_checkbox_currents[i];
	}

	public String getTag()
	{
		return "aa:";
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
				switch(mode) {
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
				switch(mode) {
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

	class Rule implements ActionListener {

		private JComboBox[]	m_components;
		
		// Logic to set up the appropriate attribute names

		private void addAttributeNames(LandscapeObject object, Vector v)
		{
			int		index;
			String	name;

			for (index = 0; (name = object.getLsAttributeNameAt(index)) != null; ++index) {
				if (name.length() > 0) {
					v.addElement(name);
		}	}	}

		private void addAllEntityAttributeNames(Vector v)
		{
			Diagram diagram = m_ls.getDiagram();

			if (diagram != null) {
				Enumeration en;
				EntityClass	ec;

				for (en = diagram.enumEntityClassesInOrder(); en.hasMoreElements(); ) {
					ec = (EntityClass) en.nextElement();
					addAttributeNames(ec, v);
		}	}	}

		private void addAllRelationAttributeNames(Vector v)
		{
			Diagram diagram = m_ls.getDiagram();

			if (diagram != null) {
				Enumeration		en;
				RelationClass	rc;

				for (en = diagram.enumRelationClassesInOrder(); en.hasMoreElements(); ) {
					rc = (RelationClass) en.nextElement();
					addAttributeNames(rc, v);
		}	}	}

		private void setAttributeColumn()
		{
			JComboBox		comboBox  = getValueAt(ATTRIBUTE_COLUMN);
			JComboBox		typeBox   = getValueAt(TYPE_COLUMN);
			JComboBox		targetBox = getValueAt(TARGET_COLUMN);
			Object			item      = targetBox.getSelectedItem();

			comboBox.removeAllItems();
			comboBox.addItem(g_all);

			switch (typeBox.getSelectedIndex()) {
			case TYPE_CLASS:
			case TYPE_CLIENT_CLASS:
			case TYPE_SUPPLIER_CLASS:

				// Get the legitimate attribute names for a class

				if (item == null || item == g_null) {
					JComboBox	relationBox = getValueAt(RELATION_COLUMN);
					item = relationBox.getSelectedItem();
					if (item == null || item == g_null) {
						JComboBox sourceBox = getValueAt(SOURCE_COLUMN);
						item = sourceBox.getSelectedItem();
				}	}
				
				if (item != null) {
					Vector		v        = new Vector();
					Enumeration	en;
					String		name, last;

					if (item == g_all_entities) {
						addAllEntityAttributeNames(v);
					} else if (item == g_all_relations) {
						addAllRelationAttributeNames(v);
					} else {
						addAttributeNames((LandscapeObject) item, v);
					}
					SortVector.byString(v);

					last = null;
					for (en = v.elements(); en.hasMoreElements(); ) {
						name = (String) en.nextElement();
						if (name.equals(last)) {
							continue;
						}
						comboBox.addItem(name);
						last = name;
				}	}
				comboBox.setEditable(false);
				break;
			default:
				// Entities and relations can have arbitrary attribute names
				// So make the comboBox editable
				comboBox.addItem("");
				comboBox.setEditable(true);
			}
			comboBox.setSelectedIndex(0);
		}

		private void addEntityClasses(JComboBox comboBox)
		{
			Diagram		diagram = m_ls.getDiagram();
			Vector		v       = new Vector();
			Enumeration	en;
			Object		item;

			comboBox.addItem(g_all_entities);
			for (en = diagram.enumEntityClasses(); en.hasMoreElements(); ) {
				v.addElement(en.nextElement());
			}

			SortVector.byString(v);

			for (en = v.elements(); en.hasMoreElements(); ) {
				item = en.nextElement();
				comboBox.addItem(item);
			}
			comboBox.setSelectedIndex(0);
		}

		private void addRelationClasses(JComboBox comboBox)
		{
			Diagram		diagram = m_ls.getDiagram();
			Vector		v       = new Vector();
			Enumeration	en;
			Object		item;

			comboBox.addItem(g_all_relations);
			for (en = diagram.enumRelationClasses(); en.hasMoreElements(); ) {
				v.addElement(en.nextElement());
			}

			SortVector.byString(v);

			for (en = v.elements(); en.hasMoreElements(); ) {
				item = en.nextElement();
				comboBox.addItem(item);
			} 
			comboBox.setSelectedIndex(0);
		}

		private void setTargetColumn()
		{
			JComboBox		comboBox    = getValueAt(TARGET_COLUMN);
			JComboBox		relationBox = getValueAt(RELATION_COLUMN);
			int				index       = relationBox.getSelectedIndex();

			comboBox.removeAllItems();
			comboBox.addItem(g_null);
			if (index > 0) {
				addEntityClasses(comboBox);
			}
			comboBox.setSelectedIndex(0);
		}

		private void setRelationColumn()
		{
			JComboBox		comboBox = getValueAt(RELATION_COLUMN);

			comboBox.removeAllItems();
			comboBox.addItem(g_null);
			addRelationClasses(comboBox);
			comboBox.setSelectedIndex(0);
		}

		private void setSourceColumn()
		{
			JComboBox		comboBox = getValueAt(SOURCE_COLUMN);
			Diagram			diagram  = m_ls.getDiagram();

			comboBox.removeAllItems();

			if (diagram == null) {
				comboBox.addItem(g_null);
			} else {
				addEntityClasses(comboBox);
			}
		}

		public JComboBox getValueAt(int column)
		{
			return m_components[column];
		}

		public Rule()
		{
			JComboBox	comboBox;

			m_components = new JComboBox[RULE_COMPONENTS];
			m_components[TYPE_COLUMN]      = new JComboBox(g_typenames);
			m_components[SOURCE_COLUMN]    = new JComboBox();
			m_components[RELATION_COLUMN]  = new JComboBox();
			m_components[TARGET_COLUMN]    = new JComboBox();
			m_components[ATTRIBUTE_COLUMN] = new JComboBox();
			m_components[USE_COLUMN]       = new JComboBox(g_usenames);

			comboBox = getValueAt(TYPE_COLUMN);
			comboBox.setSelectedIndex(TYPE_ANY);
			comboBox.setMaximumRowCount(g_typenames.length);
			comboBox.addActionListener(this);

			comboBox = getValueAt(SOURCE_COLUMN);
			comboBox.setMaximumRowCount(12);
			comboBox.addActionListener(this);
			setSourceColumn();

			comboBox = getValueAt(RELATION_COLUMN);
			comboBox.setMaximumRowCount(12);
			comboBox.addActionListener(this);
			setRelationColumn();

			comboBox = getValueAt(TARGET_COLUMN);
			comboBox.setMaximumRowCount(12);
			comboBox.addActionListener(this);
			setTargetColumn();

			comboBox = getValueAt(ATTRIBUTE_COLUMN);
			comboBox.setMaximumRowCount(12);
			comboBox.addActionListener(this);
			setAttributeColumn();

			comboBox = getValueAt(USE_COLUMN);
			comboBox.setSelectedIndex(0);
			comboBox.setMaximumRowCount(g_usenames.length);
			comboBox.setEditable(true);
			comboBox.addActionListener(this);
		}

		public void connectedNodeId()
		{
			m_components[TYPE_COLUMN].setSelectedIndex(3);
			m_components[SOURCE_COLUMN].setSelectedIndex(0);
			m_components[RELATION_COLUMN].setSelectedIndex(1);
			m_components[TARGET_COLUMN].setSelectedIndex(1);
			m_components[ATTRIBUTE_COLUMN].setSelectedItem("id");
			m_components[USE_COLUMN].setSelectedIndex(2);
		}

		public void nodeId()
		{
			m_components[TYPE_COLUMN].setSelectedIndex(1);
			m_components[SOURCE_COLUMN].setSelectedIndex(0);
			m_components[RELATION_COLUMN].setSelectedIndex(0);
			m_components[TARGET_COLUMN].setSelectedIndex(0);
			m_components[ATTRIBUTE_COLUMN].setSelectedItem("id");
			m_components[USE_COLUMN].setSelectedIndex(2);
		}

		public void classId()
		{
			m_components[TYPE_COLUMN].setSelectedIndex(4);
			m_components[SOURCE_COLUMN].setSelectedIndex(0);
			m_components[RELATION_COLUMN].setSelectedIndex(0);
			m_components[TARGET_COLUMN].setSelectedIndex(0);
			m_components[ATTRIBUTE_COLUMN].setSelectedItem("id");
			m_components[USE_COLUMN].setSelectedIndex(2);
		}

		public void parentId()
		{
			JComboBox		relations;
			RelationClass	rc;
			int				i;

			m_components[TYPE_COLUMN].setSelectedIndex(2);
			m_components[SOURCE_COLUMN].setSelectedIndex(0);

			relations = m_components[RELATION_COLUMN];
			relations.setSelectedIndex(0);

			for (i = 2; i < relations.getItemCount(); ++i) {
				rc = (RelationClass) relations.getItemAt(i);
				if (rc.getContainsClassOffset() == 0) {
					// Select the dominant contains class
					relations.setSelectedIndex(i);
					break;
			}	}
			m_components[TARGET_COLUMN].setSelectedIndex(1);
			m_components[ATTRIBUTE_COLUMN].setSelectedItem("id");
			m_components[USE_COLUMN].setSelectedIndex(2);
		}

		// ActionListener interface

		public void actionPerformed(ActionEvent ev)
		{
			Object		source = ev.getSource();
			int			i, index;
			JComboBox	comboBox;

			for (i = 0; ; ++i) {
				if (i >= RULE_COMPONENTS) {
					return;
				}
				if (source == m_components[i]) {
					break;
			}	}
			comboBox = (JComboBox) source;
			index    = comboBox.getSelectedIndex();
			if (index < 0) {
				return;
			}

			switch (i) {
			case TYPE_COLUMN:
				switch (index) {
				case TYPE_DELETE:
					m_rules.remove(this);				// Delete
					break;
				default:
					setSourceColumn();
				}
			case SOURCE_COLUMN:
				setRelationColumn();
			case RELATION_COLUMN:
				setTargetColumn();
			case TARGET_COLUMN:
				setAttributeColumn();
			}
			m_table.revalidate();
			m_table.repaint();
		}
	}

	protected class CellRenderer extends JTextField implements TableCellRenderer {

		public CellRenderer() 
		{
			super();
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
		{
			Object	selectedItem;
			String	description;

			selectedItem = ((JComboBox) value).getSelectedItem();
			if (selectedItem == null) {
				description = "";
			} else if (selectedItem instanceof LandscapeClassObject) {
				description = ((LandscapeClassObject) selectedItem).getId();
			} else {
				description = (String) selectedItem;
			}
			setText(description);
			return this;
		}
	}

	class CellEditor extends DefaultCellEditor {

		JComboBox		m_comboBox;

		public CellEditor(int row, int column) 
		{
			super(((Rule) m_rules.elementAt(row)).getValueAt(column)); 

			m_comboBox =  (JComboBox) editorComponent; 

			setClickCountToStart(1); //This is usually 1 or 2.

			//Must do this so that editing stops when appropriate.
			m_comboBox.addActionListener(new ActionListener() 
										 {
											public void actionPerformed(ActionEvent e) {
												fireEditingStopped();
											}
										 });
		}

		public Object getCellEditorValue() 
		{
			return m_comboBox;
		}

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) 
		{
			return editorComponent;
		}
	}

	protected class MyTableModel extends AbstractTableModel {
		
		JTable			m_table;

		public MyTableModel()
		{
		}

		public void setJTable(JTable table)
		{
			m_table = table;
		}

		public int getRowCount()
		{
			return(m_rules.size());
		}

		public int getColumnCount()
		{
			return(RULE_COMPONENTS);
		}

		public String getColumnName(int column)
		{
			return g_columnnames[column];
		}

		public boolean isCellEditable(int row, int col)
		{
			Rule		rule     = (Rule) m_rules.elementAt(row);
			JComboBox	comboBox = rule.getValueAt(col);

			return (comboBox.getItemCount() > 1);
		}

		public Object getValueAt(int row, int col)
		{
			Rule		rule     = (Rule) m_rules.elementAt(row);
			JComboBox	comboBox = rule.getValueAt(col);
			return(comboBox);
		}

		public void setValueAt(Object value, int row, int col)
		{
		}
	}

	// This is the class which decides how the table is drawn and edited

	protected class MyJTable extends JTable {

		public MyJTable(AbstractTableModel tableModel)
		{
			super(tableModel);
		}

		public TableCellRenderer getCellRenderer(int row, int column)
		{
			return new CellRenderer();
		}

		public TableCellEditor getCellEditor(int row, int column)
		{
			return new CellEditor(row, column);
		}
	}

	class AAClusterConfigure extends JDialog implements ActionListener {

		protected JTextField[]		m_textfields;
		protected JCheckBox[]		m_checkboxes;
		protected JButton[]			m_buttons;
		protected JLabel			m_message;
		protected boolean			m_isok;
		
		public AAClusterConfigure(AAClusterLayout layout, String message)
		{
			super(layout.getLs().getFrame(), layout.getName() + " Configuration", true);

			Container			contentPane;
			JScrollPane			scrollPane;
			JPanel				topPanel, labelPanel, valuePanel, bottomPanel, buttonPanel;
			GridLayout			gridLayout;
			JTextField			textfield;
			Font				font, bold;
			JLabel				label;
			JCheckBox			checkbox;
			JButton				button;
			String				string, tip;
			int					i;

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

			label = new JLabel("Similarity metric:", JLabel.RIGHT);
			label.setFont(bold);
			labelPanel.add(label);
			m_metric.setFont(bold);
			valuePanel.add(m_metric);

			label = new JLabel("Cluster algorithm:", JLabel.RIGHT);
			label.setFont(bold);
			labelPanel.add(label);
			m_algorithm.setFont(bold);
			valuePanel.add(m_algorithm);

			label = new JLabel("Tracing within AA:", JLabel.RIGHT);
			label.setFont(bold);
			labelPanel.add(label);
			m_debug.setFont(bold);
			valuePanel.add(m_debug);

			label = new JLabel("Preset rules:", JLabel.RIGHT);
			label.setFont(bold);
			labelPanel.add(label);
			m_presets.addActionListener(this);
			m_presets.setFont(bold);
			valuePanel.add(m_presets);

			buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));

			m_checkboxes = new JCheckBox[m_checkbox_tags.length];
			for (i = 0; i < m_checkbox_tags.length; ++i) {
				m_checkboxes[i] = checkbox = new JCheckBox(m_checkbox_titles[i], m_checkbox_currents[i]);
				checkbox.setFont(font);
				buttonPanel.add(checkbox);
			}
	
			topPanel.add(BorderLayout.WEST, labelPanel);
			topPanel.add(BorderLayout.EAST, valuePanel);
			topPanel.add(BorderLayout.SOUTH, buttonPanel);


			MyJTable		table = m_table;

			table.setFont(font);

			JTableHeader tableHeader = table.getTableHeader();
			tableHeader.setFont(bold);

			FontMetrics fm = getFontMetrics(font);

			table.setRowHeight(fm.getHeight() + 4);

			table.setVisible(true);
			scrollPane = new JScrollPane(table);

			scrollPane.setVisible(true);


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
			contentPane.add(BorderLayout.CENTER, scrollPane);
			contentPane.add(BorderLayout.SOUTH,  bottomPanel);

			// Resize the window to the preferred size of its components
			pack();
			setVisible(true);
		}

		public boolean ok()
		{
			return m_isok;
		}

		protected void setPreset(int choice)
		{
			Rule	rule;

			m_rules.removeAllElements();

			switch (choice) {
			case 0:
				break;
			case 1:
				rule = new Rule();
				rule.connectedNodeId();
				m_rules.add(rule);
				rule = new Rule();
				rule.nodeId();
				m_rules.add(rule);
				break;
			case 2:
				rule = new Rule();
				rule.classId();
				m_rules.add(rule);
				break;
			case 3:
				rule = new Rule();
				rule.parentId();
				m_rules.add(rule);
				break;
			}
			m_table.revalidate();
			m_table.repaint();
		}

		// ActionListener interface

		public void actionPerformed(ActionEvent ev)
		{
			Object		source;
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
				m_metric.setSelectedIndex(0);
				m_algorithm.setSelectedIndex(0);
				m_debug.setSelectedIndex(0);
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
			case BUTTON_ADD:
				m_rules.addElement(new Rule());
				m_table.revalidate();
				m_table.repaint();
				return;
			case BUTTON_HELP:
				JOptionPane.showMessageDialog(m_ls.getFrame(), 	
				  "The AA clustering algorithm attempts to cluster nodes on the basis of\n" +
				  "attributes that have the same or somewhat similar values in distinct\n" +
				  "entities. Specify the rules here which will produce the input attribute\n" +
				  "values on which clustering is to be performed.  Each such rule contains\n" +
				  "six components:\n\n" +
				  "1) The rule to use when navigating from the entity to be clustered.  If\n" +
				  "   client/supplier is specified restrict navigation to in/out edges. If\n" +
				  "	  class is specified recover attributes from the entity/relation class.\n" + 
				  "2) The entity class for which this rule is to be considered applicable.\n" + 
				  "3) The relation class if any to use when navigating to the object from\n" +
				  "   which attributes are to be recovered.\n" +
				  "4) The class of entity reached from such relations if any from which the\n" +
				  "   attributes are to be recovered.\n" +
				  "5) The attribute to be recovered from the resulting entity or relation.\n" +
				  "   Select one of the choices or enter the name of the desired attribute.\n" + 
				  "6) The format of the value to be emitted as input to AA. This is a format\n" +
				  "   string in which %c expands to class name, %a to attribute name, %v to\n" +
				  "   attribute value and %% to %.\n\n" +
				  "Output values derived from any of the specified rules will be emitted.\n" +
				  "Use the [add] button to create new rules, and selecte [Delete] option in\n" +
				  "the rules first component to delete existing rules.\n\n" +
				  "If the cutpoint is an integer then AA will attempt to create this number\n" +
				  "of clusters.  If it is a double precision value between 0.0 and 1.0, this\n" +
				  "value will be used as the desired cutpoint."
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
				if (source == m_presets) {
					setPreset(m_presets.getSelectedIndex());
				}
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


	public AAClusterLayout(LandscapeEditorCore ls, LandscapeLayouter fallback) 
	{
		super(ls, fallback);

		MyTableModel	tableModel;
		MyJTable		table;

		m_rules    = new Vector();
		// Can't add a rule here cause not yet a diagram
		// m_rules.addElement(new Rule());
		tableModel = new MyTableModel();
		table      = new MyJTable(tableModel);
		tableModel.setJTable(table);
		m_table    = table;
		m_debug.setSelectedIndex(1);
	}


	public String getName()
	{
		return "AA Cluster";
	}

	public String getMenuLabel() 
	{
		return "AA Cluster";
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

		AAClusterConfigure configure = new AAClusterConfigure(this, message);
		ok = configure.ok();
		configure.dispose();
		return ok;
	}

	public boolean configure(LandscapeEditorCore ls)
	{
		return configure(ls, null);
	}

/***************************************************************************/

	protected void addKeyword(Vector v, int typeIndex, LandscapeClassObject parent, LandscapeObject object, String attributeItem, String useItem)
	{

		int		index, i, length, cnt;
		String	name;
		String	result;
		char	c;

		switch (typeIndex) {
		case TYPE_CLASS:
		case TYPE_CLIENT_CLASS:
		case TYPE_SUPPLIER_CLASS:
			// Examine the attributes of the class
			object = parent;
		}

		length = useItem.length();
		cnt    = 0;
		for (index = 0; (name = object.getLsAttributeNameAt(index)) != null; ++index) {
			if (name.length() <= 0) {
				continue;
			}
			if (attributeItem != null && !name.equalsIgnoreCase(attributeItem)) {
				continue;
			}
			++cnt;
			result = "";
			for (i = 0; i < length; ++i) {
				c = useItem.charAt(i);
				if (c != '%' || ++i == length) {
					result += c;
					continue;
				}
				c = useItem.charAt(i);
				switch (c) {
				case 'c':
					result += parent.getId();
					continue;
				case 'a':
					result += name;
					continue;
				case 'v':
					result += object.getLsAttributeValueAt(index);
					continue;
				case '%':
					result += '%';
					continue;
				default:
					result += '%' + c;
			}	}

			// AA has a pathetic string tokenizer so remove all whitespace inside a token
			v.addElement(result.replaceAll("[ \t\n\r\f]","")); 
		}

		// A constant value gets emitted even if the object has no attributes -- relations may not
		if (cnt == 0 && attributeItem == null  && useItem.indexOf('%') < 0) {
			v.addElement(useItem.replaceAll("[ \t\n\r\f]","")); 
		}
	}

	protected void navigate(Vector v, int typeIndex, RelationInstance ri, EntityInstance e, Object relationItem, Object targetItem, String attributeItem, String useItem)
	{
		RelationClass rc = ri.getRelationClass();

		if (relationItem == null) {
			if (!ri.isRelationShown()) {
				// Consider only visible edges when clustering
				return;
			}
		} else {
			if (relationItem != rc) {
				return;
		}	}
		if (targetItem == null) {
			addKeyword(v, typeIndex, rc, ri, attributeItem, useItem);
			return;
		}
		EntityClass ec = (EntityClass) e.getParentClass();
		if (targetItem != ec && targetItem instanceof EntityClass) {
			return;
		}
		addKeyword(v, typeIndex, ec, e, attributeItem, useItem);
	}

	protected boolean write(Vector selectedBoxes, String exportname)
	{
		FileOutputStream os;
		PrintWriter		ps = null;

		try {
			log("Exporting " + exportname);

			os = new FileOutputStream(exportname);
			ps = new PrintWriter(os);

		} catch (Exception error) {
			message("Exception creating output stream " + exportname + ": " + error.getMessage());
			return(false);
		}

		Enumeration			en, en1, en2;
		EntityInstance		e;
		EntityClass			ec;
		RelationInstance	ri;
		boolean				leaves = parameterBoolean(LEAVES);
		Vector				v      = new Vector();
		Rule				rule;
		JComboBox			type, source, relation, target, attribute, use;
		int					typeIndex;
		Object				sourceItem, relationItem, targetItem;
		String				attributeItem;
		String				useItem;


		try {
		
			for (en = selectedBoxes.elements(); en.hasMoreElements(); ) {
				e  = (EntityInstance) en.nextElement();
				ec = (EntityClass) e.getParentClass();
				v.removeAllElements();

				for (en1 = m_rules.elements(); en1.hasMoreElements(); ) {
					rule           = (Rule) en1.nextElement();
					type           = rule.getValueAt(TYPE_COLUMN);
					typeIndex      = type.getSelectedIndex();

					source         = rule.getValueAt(SOURCE_COLUMN);
					sourceItem     = source.getSelectedItem();
					if (sourceItem != null && sourceItem != ec && sourceItem instanceof EntityClass) {
						// This rule does not apply to this source
						continue;
					}

					relation	   = rule.getValueAt(RELATION_COLUMN);
					target		   = rule.getValueAt(TARGET_COLUMN);
					attribute	   = rule.getValueAt(ATTRIBUTE_COLUMN);
					attributeItem  = (String) attribute.getSelectedItem();
					if (attributeItem == g_all) {
						attributeItem = null;
					}
					use            = rule.getValueAt(USE_COLUMN);
					useItem        = (String) use.getSelectedItem();

					relationItem = relation.getSelectedItem();
					if (relationItem == g_null) {
						relationItem = null;
					}

					if (relationItem == null) {
						addKeyword(v, typeIndex, ec, e, attributeItem, useItem);
						continue;
					}

					if (!(relationItem instanceof RelationClass)) {
						relationItem = null;
					}

					targetItem   = target.getSelectedItem();
					if (targetItem == g_null) {
						targetItem = null;
					}

					switch (typeIndex) {
					case TYPE_ANY:
					case TYPE_CLASS:
					case TYPE_SUPPLIER:
					case TYPE_SUPPLIER_CLASS:

						if (leaves) {
							// Use real edges to determine relationships

							en2 = e.srcRelationElements();
							if (en2 != null) {
								while (en2.hasMoreElements()) {
									ri = (RelationInstance) en2.nextElement();
									navigate(v, typeIndex, ri, ri.getDst(), relationItem, targetItem, attributeItem, useItem);
							}	}
						} else {
							// Use lifted edges to determine relationships
							en2 = e.srcLiftedRelationElements();
							if (en2 != null) {
								while (en2.hasMoreElements()) {
									ri = (RelationInstance) en2.nextElement();
									navigate(v, typeIndex, ri, ri.getDrawDst(), relationItem, targetItem, attributeItem, useItem);
							}	}
						}
						if (typeIndex == TYPE_SUPPLIER) {
							break;
						}
					default:
						if (leaves) {
							// Use real edges to determine relationships

							en2 = e.dstRelationElements();
							if (en2 != null) {
								while (en2.hasMoreElements()) {
									ri = (RelationInstance) en2.nextElement();
									navigate(v, typeIndex, ri, ri.getSrc(), relationItem, targetItem, attributeItem, useItem);
							}	}
						} else {
							// Use lifted edges to determine relationships
							en2 = e.dstLiftedRelationElements();
							if (en2 != null) {
								while (en2.hasMoreElements()) {
									ri = (RelationInstance) en2.nextElement();
									navigate(v, typeIndex, ri, ri.getDrawSrc(), relationItem, targetItem, attributeItem, useItem);
							}	}
					}	}
				} 

				ps.print(e.getId());
	

				String		result, last;

				SortVector.byString(v);

				last = null;
				for (en1 = v.elements(); en1.hasMoreElements(); ) {
					result = (String) en1.nextElement();
					if (result.equals(last)) {
						continue;
					}
					ps.print(" " + result);
					last = result;
				}

				ps.print("\n");
			}

				

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
		String				exportname, command, importname, cutpoint;
		String				string;
		int					icutpoint;
		double				dcutpoint;

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
			cutpoint   = parameterString(CUTPOINT);
			icutpoint  = 0;
			dcutpoint  = -1.0;

			string = null;
			if (exportname.length() == 0) {
				string = "Please specify an export file to write to";
			} else if (importname.length() != 0 && command.length() == 0) {
				string = "Please specify the command to execute AA";
			} else if (command.length() != 0 && importname.length() == 0) {
				string = "Please specify an import file to read from";
			} else if (m_rules.isEmpty()) {
				string = "Please specify rules for generating attribute values";
			} else {
				try {
					icutpoint = Integer.parseInt(cutpoint);
					if (icutpoint < 2) {
						string = "Cutpoint specifies less than two clusters are to be formed";
						icutpoint = 0;
					}
				} catch (Throwable exception) {
					icutpoint = 0;
					dcutpoint = -2;
				}
				if (dcutpoint == -2) {
					try {
						dcutpoint = Double.parseDouble(cutpoint);
						if (dcutpoint < 0 || dcutpoint > 1.0) {
							string = "Double precision cutpoint must be between 0.0 and 1.0";
						}
					} catch (Throwable exception) {
						string = "Cutpoint '" + cutpoint + "' must be an integer or double precision value";
			}	}	}

			if (string == null) {
				break;
			}
			if (!configure(m_ls, string)) {
				return true;
		}	}

		if (!exportname.endsWith(".mbd")) {
			exportname += ".mbd";
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

		log("Using AA to cluster " + size + " items");

		if (!write(selectedBoxes, exportname)) {
			return false;
		}

		if (command.length() == 0) {
			m_ret = "AA output written to file";
			return true;
		}
		
		String	s       = command + " " + exportname + " " + importname;
		String	s1;
		Process process = null;
		int		index;
			
		if (icutpoint != 0) {
			s1 = "" + icutpoint;
		} else {
			s1 = "" + dcutpoint;
		}
		s += " -c" + s1;

		index = m_debug.getSelectedIndex();
		if (index > 0) {
			s += " -d" + index;
		}
		index = m_metric.getSelectedIndex();
		if (index > 0) {
			s += " -s" + index;
		}
		index = m_algorithm.getSelectedIndex();
		if (index > 0) {
			s += " -a" + index;
		}

		log("Executing [" + s + "]");

		try {
			Runtime runtime = Runtime.getRuntime();
			if (runtime == null) {
				message("No runtime available");
				return false;
			} else {
				EchoOutput	output;

				process = runtime.exec(s);
				output  = new EchoOutput("AA Stdout", process.getInputStream());
				new Thread(output).start();

				output  = new EchoOutput("AA Stderr", process.getErrorStream());
				new Thread(output).start();
			}
		} catch (Exception error) {
			log("Exception executing [" + s + "] " + error.getMessage());
			return false;
		}	

		if (!waitFor(process)) {
			return false;
		}
		
		importname = importname.substring(0, importname.length()-4) + s1 + ".rsf";
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

		m_ret = "Graph redrawn using AA Cluster Layout";
		return true;
	} 

	public String doLayout(Diagram dg) 
	{
		LandscapeEditorCore	ls = m_ls;
		EntityInstance		parent;

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

		m_ret = "AA Cluster layout aborted";
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





