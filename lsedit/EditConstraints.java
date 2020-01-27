package lsedit;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class EditConstraints extends JDialog implements ActionListener { 

	JFrame			m_frame;
	RelationClass	m_rc;
	private JButton	m_ok      = null;
	private JButton m_restart = null;
	private JButton m_clear   = null;
	private JButton m_help    = null;
	private JButton	m_cancel  = null;
	EntityClass		m_ecs[];
	JCheckBox		m_array[][];
	int				m_size;
	
	protected void clearArray()
	{
		int			size      = m_size;
		JCheckBox	array[][] = m_array;
		int			i, j;

		for (i = 0; i < size; ++i) {
			for (j = 0; j < size; ++j) {
				array[i][j].setSelected(false);
		}	}
	}

	protected void initArray()
	{
		JCheckBox	array[][] = m_array;
		Enumeration	en;
		EntityClassPair ep;

		clearArray();

		Vector relationList = m_rc.getRelationList();
		for (en = relationList.elements(); en.hasMoreElements();) {
			ep = (EntityClassPair) en.nextElement();
			array[ep.m_entityClass1.getOrderedId()][ep.m_entityClass2.getOrderedId()].setSelected(true);
		}
	}
		
	protected void processArray()
	{

		Vector			relationList = m_rc.getRelationList();
		int				i, j, size;
		EntityClassPair	ep;

		for (i = relationList.size(); i > 0; ) {
			--i;
			ep = (EntityClassPair) relationList.elementAt(i);
			if (!m_array[ep.m_entityClass1.getOrderedId()][ep.m_entityClass2.getOrderedId()].isSelected()) {
				relationList.removeElementAt(i);
		}	}

		size = m_size;
		for (i = 0; i < size; ++i) {
			for (j = 0; j < size; j++) {
				if (m_array[i][j].isSelected()) {
					m_rc.addRelationConstraint(m_ecs[i], m_ecs[j]);
	}	}	}	} 
	
	protected EditConstraints(JFrame frame, Diagram diagram, RelationClass rc)
	{
		super(frame, "Edit Constraints on " + rc.getLabel(), true); //false if non-modal

		Container	contentPane;
		JCheckBox	checkBox;
		JScrollPane	scrollPane;
		JPanel		panel;
		JLabel		label;
		Font		font, bold;

		Enumeration		en;
		EntityClass		ec;
		int				i, j, size;

		m_frame = frame;
		font    = FontCache.getDialogFont();
		bold    = font.deriveFont(Font.BOLD);

		m_rc    = rc;
		m_size  = size  = diagram.numEntityClasses();
		m_ecs   = new EntityClass[size];

		i = 0;
		for (en = diagram.enumEntityClassesInOrder(); en.hasMoreElements(); ++i) {
			ec = (EntityClass) en.nextElement();
			ec.setOrderedId(i);
			m_ecs[i] = ec;
		}

		m_array = new JCheckBox[size][];
		for (i = 0; i < size; ++i) {
			m_array[i] = new JCheckBox[size];
			for (j = 0; j < size; ++j) {
				checkBox      = new JCheckBox();
				checkBox.setFont(font);
				checkBox.setToolTipText(m_ecs[i].getLabel() + "->" + m_ecs[j].getLabel());
				m_array[i][j] = checkBox;
		}	}

		initArray();


//		setSize(438,369);

		contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		setForeground(ColorCache.get(0,0,0));
		setBackground(ColorCache.get(192,192,192));
		setFont(font);

		panel = new JPanel();
		panel.setLayout(new GridLayout(0 /* rows */, size+1 /* columns */));

		label = new JLabel("");
		panel.add(label);
		for (i = 0; i < size; ++i) {
			label = new JLabel(m_ecs[i].getLabel());
			label.setFont(bold);
			panel.add(label);
		}


		for (i = 0; i < size; ++i) {	// For each column
			label = new JLabel(m_ecs[i].getLabel() + " ");
			label.setFont(bold);
			panel.add(label);
			for (j = 0; j < size; ++j) {
				panel.add(m_array[i][j]);
		}	}

		scrollPane = new JScrollPane(panel);

		scrollPane.setVisible(true);
		contentPane.add(scrollPane, BorderLayout.CENTER);

		panel = new JPanel();
		panel.setLayout(new FlowLayout());

		m_ok     = new JButton("Ok");
		m_ok.setFont(bold);
		m_ok.addActionListener(this);
		panel.add(m_ok);

		m_restart = new JButton("Restart");
		m_restart.setFont(bold);
		m_restart.addActionListener(this);
		panel.add(m_restart);

		m_clear = new JButton("Clear");
		m_clear.setFont(bold);
		m_clear.addActionListener(this);
		panel.add(m_clear);

		m_help  = new JButton("Help");
		m_help.setFont(bold);
		m_help.addActionListener(this);
		panel.add(m_help);

		m_cancel  = new JButton("Cancel");
		m_cancel.setFont(bold);
		m_cancel.addActionListener(this);
		panel.add(m_cancel);

		contentPane.add(panel, BorderLayout.SOUTH);

		pack();
		setVisible(true);
	}

	public static void create(Diagram diagram, RelationClass rc) 
	{
		LandscapeEditorCore ls = diagram.getLs();

		EditConstraints editConstraints = new EditConstraints(ls.getFrame(), diagram, rc);
		editConstraints.dispose();
	}

	// ActionListener interface

	public void actionPerformed(ActionEvent ev)
	{
		Object	source;

		// Pop down the window when the button is clicked.
		// System.out.println("event: " + ev);

		source = ev.getSource();

		if (source == m_restart) {
			initArray();
			return;
		}

		if (source == m_clear) {
			clearArray();
			return;
		}
		
		if (source == m_help) {
			JOptionPane.showMessageDialog(m_frame, 	
			  "This matrix shows for all combination of source entities (shown vertically)\n" +
			  "and destination entities (shown horizontally) those that may (according to\n" +
			  "the current schema) be connected by edges of the relation class:\n\n" +
			  m_rc.getLabel() + "\n\n" +
			  "Update these constraints by checking/unchecking the desired pairings of\n" +
			  "source and destination entities\n"
			   , "Help", JOptionPane.OK_OPTION | JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		if (source == m_ok) {
			processArray();
		} else if (source != m_cancel) {
			return;
		}
		this.setVisible(false);
	}
}



