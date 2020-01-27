package lsedit;

import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.Vector;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class FindBox extends JDialog implements ActionListener
{
	protected LandscapeEditorCore	m_ls;

	protected JCheckBox				m_caseSensitive;
	protected JTextField			m_inputField;
	protected static String			m_input = "";

	protected Pattern				m_pattern;
	protected JComboBox				m_classes;
	protected EntityClass			m_entityClass;
	protected JCheckBox				m_superclasses;
	protected JCheckBox				m_subclasses;

	protected JComboBox				m_in_edges;
	protected JSpinner              m_in_edges_min;
	protected JSpinner              m_in_edges_max;
	protected JComboBox				m_out_edges;
	protected JSpinner              m_out_edges_min;
	protected JSpinner              m_out_edges_max;
	protected JSpinner              m_children_min;
	protected JSpinner              m_children_max;

	protected JButton				m_findButton, m_helpButton, m_canButton;

	public FindBox(JFrame f, LandscapeEditorCore ls, Diagram diagram)
	{
		super(f, "Find Landscape Entities", true);

		JPanel		p;
		JLabel		label;
		EntityClass	entityClass;
		RelationClass relationClass;
		RelationClass containsClass = null;
		Container	contentPane = getContentPane();
		Font		font, bold;
		Enumeration	en;

		m_ls          = ls;
		m_pattern     = null;
		m_entityClass = null;

		font          = FontCache.getDialogFont();
		bold          = font.deriveFont(Font.BOLD);

		// Create a dialog 

		contentPane.setLayout(new BorderLayout());

		GridBagLayout		gridBagLayout = new GridBagLayout();
		GridBagConstraints	c             = new GridBagConstraints();

		p = new JPanel();
		p.setLayout(gridBagLayout);

		setFont(font);

		label = new JLabel("Pattern.");
		label.setFont(font);
		c.gridx     = 0;
		c.anchor    = GridBagConstraints.WEST;
		c.fill      = GridBagConstraints.NONE;
		c.gridwidth = 1;
		c.weightx   = 0.0;
		gridBagLayout.setConstraints(label, c);
		p.add(label);


		m_inputField = new JTextField(m_input, 30);
		m_inputField.setFont(font);
		m_inputField.addActionListener(this);
		c.gridx     = 1;
		c.anchor    = GridBagConstraints.CENTER;
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;
		c.weightx   = 1.0;
		gridBagLayout.setConstraints(m_inputField, c);
		p.add(m_inputField);

		m_caseSensitive = new JCheckBox("Case sensitive");
		m_caseSensitive.setFont(bold);
		m_caseSensitive.setSelected(true);
		c.gridx     = 2;
		c.anchor    = GridBagConstraints.WEST;
		c.fill      = GridBagConstraints.NONE;
		c.gridwidth = 1;
		c.weightx   = 0.0;
		gridBagLayout.setConstraints(m_caseSensitive, c);
		p.add(m_caseSensitive);


		label = new JLabel("Any java.util.regex.");
		label.setFont(font);
		c.gridx     = 3;
		c.anchor    = GridBagConstraints.WEST;
		c.fill      = GridBagConstraints.NONE;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx   = 0.0;
		gridBagLayout.setConstraints(label, c);
		p.add(label);

		label       = new JLabel("Entity Class");
		label.setFont(font);
		c.anchor    = GridBagConstraints.WEST;
		c.gridx     = 0;
		c.gridwidth = 1;
		c.fill      = GridBagConstraints.NONE;
		c.weightx   = 0.0;
		gridBagLayout.setConstraints(label, c);
		p.add(label);


		m_classes = new JComboBox();
		m_classes.addItem("");
		for (en = diagram.enumEntityClassesInOrder(); en.hasMoreElements(); ) { 
			entityClass = (EntityClass) en.nextElement();
			m_classes.addItem(entityClass);
		}
		c.gridx     = 1;
		c.anchor    = GridBagConstraints.CENTER;
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;
		c.weightx   = 1.0;
		gridBagLayout.setConstraints(m_classes, c);
		p.add(m_classes);


		m_subclasses = new JCheckBox("plus subclasses");
		m_subclasses.setFont(bold);
		m_subclasses.setSelected(false);
		c.gridx     = 2;
		c.anchor    = GridBagConstraints.CENTER;
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;
		c.weightx   = 0.0;
		gridBagLayout.setConstraints(m_subclasses, c);
		p.add(m_subclasses);

		label = new JLabel("");
		label.setFont(font);
		c.gridx     = 3;
		c.anchor    = GridBagConstraints.CENTER;
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;
		c.weightx   = 0.0;
		gridBagLayout.setConstraints(label, c);
		p.add(label);

		m_superclasses = new JCheckBox("plus superclasses");
		m_superclasses.setFont(bold);
		m_superclasses.setSelected(false);
		c.gridx     = 4;
		c.anchor    = GridBagConstraints.CENTER;
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx   = 0.0;
		gridBagLayout.setConstraints(m_superclasses, c);
		p.add(m_superclasses);

		m_in_edges  = new JComboBox();
		m_out_edges = new JComboBox();
		m_in_edges.addItem("");
		m_out_edges.addItem("");

		for (en = diagram.enumRelationClassesInOrder(); en.hasMoreElements(); ) { 
			relationClass = (RelationClass) en.nextElement();
			if (relationClass.getContainsClassOffset() == 0) {
				containsClass = relationClass;
			} else {
				m_in_edges.addItem(relationClass);
				m_out_edges.addItem(relationClass);
		}	}

		label       = new JLabel("Destination Edges");
		label.setFont(font);

		c.anchor    = GridBagConstraints.CENTER;
		c.gridx     = 0;
		c.gridwidth = 1;
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.weightx   = 0.0;
		gridBagLayout.setConstraints(label, c);
		p.add(label);

		c.anchor    = GridBagConstraints.WEST;
		c.gridx     = 1;
		c.gridwidth = 1;
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.weightx   = 1.0;
		gridBagLayout.setConstraints(m_in_edges, c);
		p.add(m_in_edges);

		m_in_edges_min = new JSpinner(new SpinnerNumberModel( 0, 0,Integer.MAX_VALUE, 1));
		c.anchor    = GridBagConstraints.EAST;
		c.gridx     = 2;
		c.gridwidth = 1;
		c.weightx   = 0.5;
		gridBagLayout.setConstraints(m_in_edges_min, c);
		p.add(m_in_edges_min);

		label = new JLabel(" \u2264 active edges \u2264 ");
		label.setFont(font);
		c.gridx     = 3;
		c.anchor    = GridBagConstraints.CENTER;
		c.fill      = GridBagConstraints.NONE;
		c.weightx   = 0.0;
		gridBagLayout.setConstraints(label, c);
		p.add(label);


		m_in_edges_max = new JSpinner(new SpinnerNumberModel(-1,-1,Integer.MAX_VALUE, 1));
		c.gridx     = 4;
		c.anchor    = GridBagConstraints.WEST;
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx   = 0.5;
		gridBagLayout.setConstraints(m_in_edges_max, c);
		p.add(m_in_edges_max);


		label       = new JLabel("Source Edges");
		label.setFont(font);

		c.anchor    = GridBagConstraints.CENTER;
		c.gridx     = 0;
		c.gridwidth = 1;
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.weightx   = 0.0;
		gridBagLayout.setConstraints(label, c);
		p.add(label);

		c.anchor    = GridBagConstraints.WEST;
		c.gridx     = 1;
		c.gridwidth = 1;
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.weightx   = 1.0;
		gridBagLayout.setConstraints(m_out_edges, c);
		p.add(m_out_edges);

		m_out_edges_min = new JSpinner(new SpinnerNumberModel( 0, 0, Integer.MAX_VALUE, 1));
		c.anchor    = GridBagConstraints.EAST;
		c.gridx     = 2;
		c.gridwidth = 1;
		gridBagLayout.setConstraints(m_out_edges_min, c);
		p.add(m_out_edges_min);

		label = new JLabel(" \u2264 active edges \u2264 ");
		label.setFont(font);
		c.gridx     = 3;
		c.gridwidth = 1;
		c.fill      = GridBagConstraints.NONE;
		c.anchor    = GridBagConstraints.CENTER;
		c.weightx   = 0.0;
		gridBagLayout.setConstraints(label, c);
		p.add(label);

		m_out_edges_max = new JSpinner(new SpinnerNumberModel(-1,-1, Integer.MAX_VALUE, 1));
		c.gridx     = 4;
		c.anchor    = GridBagConstraints.WEST;
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx   = 0.5;
		gridBagLayout.setConstraints(m_out_edges_max, c);
		p.add(m_out_edges_max);

		label       = new JLabel("Child edges");
		label.setFont(font);
		c.anchor    = GridBagConstraints.WEST;
		c.gridx     = 0;
		c.gridwidth = 1;
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.weightx   = 0.0;
		gridBagLayout.setConstraints(label, c);
		p.add(label);

		label       = new JLabel((containsClass != null) ? containsClass.toString() : "");
		c.anchor    = GridBagConstraints.WEST;
		c.gridx     = 1;
		c.gridwidth = 1;
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.weightx   = 1.0;
		gridBagLayout.setConstraints(label, c);
		p.add(label);

		m_children_min = new JSpinner(new SpinnerNumberModel( 0, 0, Integer.MAX_VALUE, 1));
		c.anchor    = GridBagConstraints.EAST;
		c.gridx     = 2;
		c.gridwidth = 1;
		gridBagLayout.setConstraints(m_children_min, c);
		p.add(m_children_min);

		label = new JLabel(" \u2264 children \u2264 ");
		label.setFont(font);
		c.gridx     = 3;
		c.gridwidth = 1;
		c.fill      = GridBagConstraints.NONE;
		c.anchor    = GridBagConstraints.CENTER;
		c.weightx   = 0.0;
		gridBagLayout.setConstraints(label, c);
		p.add(label);

		m_children_max = new JSpinner(new SpinnerNumberModel(-1,-1, Integer.MAX_VALUE, 1));
		c.gridx     = 4;
		c.anchor    = GridBagConstraints.WEST;
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx   = 0.5;
		gridBagLayout.setConstraints(m_children_max, c);
		p.add(m_children_max);

		contentPane.add(p, BorderLayout.CENTER);

		p = new JPanel();
		p.setLayout(new FlowLayout());

		m_findButton = new JButton("Find");
		m_findButton.setFont(bold);
		p.add(m_findButton);
		m_findButton.addActionListener(this);

		m_helpButton = new JButton("Help");
		m_helpButton.setFont(bold);
		p.add(m_helpButton);
		m_helpButton.addActionListener(this);

		m_canButton = new JButton("Cancel");
		m_canButton.setFont(bold);
		p.add(m_canButton);
		m_canButton.addActionListener(this);

		contentPane.add(p, BorderLayout.SOUTH);

		// Resize the window to the preferred size of its components

		this.pack();

		if (f != null) {	// ie. not an applet
			setLocation(f.getX()+200, f.getY()+300);
		}
		setVisible(true);
	}

	private boolean parsePattern() 
	{
		String	input = m_input;
		boolean	ret   = true;
		Pattern pattern;
		int		flags;

		pattern = null;
		if (input != null && !input.equals("") && !input.equals("*")) {
			try {
				if (m_caseSensitive.isSelected()) {
					flags = 0;
				} else {
					flags = Pattern.CASE_INSENSITIVE;
				}
				flags |= Pattern.DOTALL;
				
				pattern = Pattern.compile(input, flags);
			} catch (Exception e) {

				JOptionPane.showMessageDialog(this, "Syntax error is regular expression '" + input + "'", "Error", JOptionPane.OK_OPTION);
				pattern = null;
				ret     = false;
		}	}
		m_pattern = pattern;
		return ret;
	}

	private int	spinnerValue(JSpinner spinner)
	{
		Number	   value  = (Number) spinner.getValue();

		return value.intValue();
	}

	public FindRules getFindRules()
	{
		Pattern		pattern       = m_pattern;
		Vector		entityClasses = null;
		EntityClass	entityClass   = m_entityClass;
		int			inMin         = spinnerValue(m_in_edges_min);
		int			inMax         = spinnerValue(m_in_edges_max);
		int			outMin        = spinnerValue(m_out_edges_min);
		int			outMax        = spinnerValue(m_out_edges_max);
		int			childrenMin   = spinnerValue(m_children_min);
		int			childrenMax   = spinnerValue(m_children_max);
		Object		object_in     = m_in_edges.getSelectedItem();
		Object		object_out    = m_out_edges.getSelectedItem();
		RelationClass	in_edges  = null;
		RelationClass   out_edges = null;

		Diagram		diagram;

		if (entityClass != null) {
			int	mode;

			if (m_subclasses.isSelected()) {
				mode = 1;
			} else {
				mode = 0;
			}
			if (m_superclasses.isSelected()) {
				mode += 2;
			}
			switch (mode) {
				case 0:
				{
					entityClasses = new Vector();
					entityClasses.addElement(entityClass);
					break;
				}
				case 1:
				{
					diagram       = m_ls.getDiagram();
					entityClasses = diagram.getClassAndSubclasses(entityClass);
					break;
				}
				case 2:
				{
					entityClasses = entityClass.getClassAndSuperclasses();
					break;
				}
				default:
				{
					Vector	v     = entityClass.getClassAndSuperclasses();
					Object	o;
					int		i;

					diagram       = m_ls.getDiagram();
					entityClasses = diagram.getClassAndSubclasses(entityClass);
					for (i = v.size(); --i >= 0; ) {
						o = v.elementAt(i);
						if (!entityClasses.contains(o)) {
							entityClasses.addElement(o);
		}	}	}	}	}

		if (object_in instanceof RelationClass) {
			in_edges = (RelationClass) object_in;
		}
		if (object_out instanceof RelationClass) {
			out_edges = (RelationClass) object_out;
		}
		if ((inMax == -1 || inMin <= inMax) && (outMax == -1 || outMin <= outMax) && (childrenMax == -1 || childrenMin <= childrenMax)) {
			if (pattern != null || entityClass != null || inMin > 0 || outMin > 0 || childrenMin > 0 || inMax >= 0 || outMax >= 0 || childrenMax >= 0) {
				return new FindRules(pattern, entityClasses, in_edges, inMin, inMax, out_edges, outMin, outMax, childrenMin, childrenMax);
		}	}
		return null;
	}		 


	// ActionListener interface

	public void actionPerformed(ActionEvent ev)
	{
		Object	source;

		// Pop down the window when the button is clicked.
		// System.out.println("event: " + ev);

		source = ev.getSource();

		if (source == m_helpButton) {
			int ret = JOptionPane.showConfirmDialog(m_ls.getFrame(),
			"Entities may be located by a number of cumulative criteria:\n" +
			"1.  By their name identified using an appropriate regular expression\n" +
			"2.  By the class that they belong to\n" +
			"3.  By membership in sub or superclasses of this class\n" +
			"4.  By the cardinality of edges to them\n" +
			"5.  Using a specific incoming edge class if specified\n" +
			"6.  By the cardinality of edges leaving them\n" +
			"7.  Using a specific outgoing edge class if specified\n" +
			"8.  By the number of children such entities have\n" +
			"\n" +
			"For instructions on the syntax of regular expressions select OK."
			, "Find Box Help", JOptionPane.OK_CANCEL_OPTION);

			if (ret == JOptionPane.OK_OPTION) {
				m_ls.showURL("http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html", LsLink.TARGET_HELP);
			}
			return;
		}

		if (source == m_inputField || source == m_findButton || source == m_canButton) {
			m_input = m_inputField.getText();
			if (source != m_canButton) {
				int	selected;

				if (!parsePattern()) {
					return;
				}
				selected  = m_classes.getSelectedIndex();
				if (selected > 0) {
					m_entityClass = (EntityClass) m_classes.getSelectedItem();
			}	}
			this.setVisible(false);
		}
		return;
	}
}

