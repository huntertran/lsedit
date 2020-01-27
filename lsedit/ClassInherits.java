package lsedit;

import java.util.Enumeration;
import java.util.Vector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class ClassInherits extends JDialog implements ActionListener { 

	private	LandscapeEditorCore		m_ls;
	private	LandscapeClassObject	m_o;

	private	JCheckBox[]				m_checkboxes;
	private	LandscapeClassObject[]	m_classes;

	private	JButton					m_ok;
	JButton							m_cancel;

	Vector							m_result;


	protected Enumeration inOrder()
	{
		if (m_o instanceof EntityClass) {
			return	m_ls.enumEntityClassesInOrder();
		} 
		return m_ls.enumRelationClassesInOrder();
	}

	protected LandscapeClassObject baseClass()
	{
		Diagram	diagram = m_ls.getDiagram();

		if (m_o instanceof EntityClass) {
			return diagram.m_entityBaseClass;
		}
		return diagram.m_relationBaseClass;
	}

	protected Vector getInheritance() 
	{
		LandscapeClassObject	base = baseClass();

		if (m_o == base) {
			return null;
		}

		Vector					v = new Vector();
		LandscapeClassObject[]	classes;
		JCheckBox[]				checkboxes;
		LandscapeClassObject	o;
		JCheckBox				checkbox;
		int						i, j, size;

		classes    = m_classes;
		checkboxes = m_checkboxes;

		if (checkboxes != null) {
			size = checkboxes.length;

			for (i = 0; i < size; ++i) {
				checkbox = checkboxes[i];
				if (checkbox.isSelected()) {
					o = classes[i];
					for (j = 0; j < size; ++j) {
						if (j != i && checkboxes[j].isSelected()) {
							if (classes[j].subclassOf(o)) {
								o = null;
								break;
					}	}	}
					if (o != null) {
						v.add(o);
		}	}	}	}

		if (v.isEmpty()) {
			v.add(base);
		}
		return v;
	}

	public ClassInherits(LandscapeEditorCore ls, LandscapeClassObject o) //Constructor
	{
		super(ls.getFrame(), o.getLabel(),true); //false if non-modal

		Container				contentPane;
		Font					font, bold;
		JCheckBox[]				checkboxes;
		Enumeration				en;
		LandscapeClassObject[]	classes;
		LandscapeClassObject	o1;
		JCheckBox				checkbox;
		int						i, size;
		GridLayout				gridLayout;
		JScrollPane				scrollPane;
		JLabel					label;
		boolean					flag;

		m_ls     = ls;
		m_o      = o;
		m_result = null;

		font = FontCache.getDialogFont();
		bold = font.deriveFont(Font.BOLD);

		contentPane = getContentPane();
        contentPane.setLayout( new BorderLayout() );

		setForeground(ColorCache.get(0,0,0));
		setBackground(ColorCache.get(192,192,192));

		setFont(font);

		JPanel centerPanel = new JPanel();


		gridLayout = new GridLayout(0,1);
		gridLayout.setVgap(10);
		centerPanel.setLayout(gridLayout);

		m_checkboxes  = null;
		m_classes     = null;

		size = 0;
		for (en = inOrder(); en.hasMoreElements(); ) {
			o1 = (LandscapeClassObject) en.nextElement();
			if (o == o1 || o1.subclassOf(o)) {
				continue;
			}
			++size;
		}

		if (size > 0) {
			label = new JLabel(" Inherits from: ");
			label.setFont(bold);
			centerPanel.add(label);

			m_checkboxes    = checkboxes    = new JCheckBox[size];
			m_classes       = classes       = new LandscapeClassObject[size];


			i = 0;
			for (en = inOrder(); en.hasMoreElements(); ) {
				o1      = (LandscapeClassObject) en.nextElement();
				if (o == o1 || o1.subclassOf(o)) {
					continue;
				}
				classes[i]    = o1;
				checkboxes[i] = checkbox = new JCheckBox(o1.getLabel());
				++i;

				checkbox.setSelected(o.directlyInheritsFrom(o1));
				centerPanel.add(checkbox);
	//			checkbox.addItemListener(this);
		}	}

		flag = false;
		for (en = inOrder(); en.hasMoreElements(); ) {
			o1 = (LandscapeClassObject) en.nextElement();
			if (o == o1 || !o1.directlyInheritsFrom(o)) {
				continue;
			}
			if (!flag) {
				label = new JLabel(" Inherited by: ");
				label.setFont(bold);
				centerPanel.add(label);
				flag = true;
			}
			label = new JLabel(" " + o1.getLabel());
			label.setForeground(Color.BLUE);
			centerPanel.add(label);
		}

		flag = false;
		for (en = inOrder(); en.hasMoreElements(); ) {
			o1 = (LandscapeClassObject) en.nextElement();
			if (o == o1 || o1.directlyInheritsFrom(o) || !o1.subclassOf(o)) {
				continue;
			}
			if (!flag) {
				label = new JLabel(" Indirectly by: ");
				label.setFont(bold);
				centerPanel.add(label);
				flag = true;
			}
			label = new JLabel(" " + o1.getLabel());
			label.setForeground(Color.RED);
			centerPanel.add(label);
		}

		scrollPane = new JScrollPane(centerPanel);
		scrollPane.setVisible(true);
		contentPane.add(BorderLayout.CENTER, scrollPane);
	
		// --------------
		// Use a FlowLayout to center the button and give it margins.

		JPanel bottomPanel = new JPanel();

		bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));

		m_ok = new JButton("Ok");
		m_ok.setFont(bold);
		bottomPanel.add(m_ok);
		m_ok.addActionListener(this);

		m_cancel = new JButton("Cancel");
		m_cancel.setFont(bold);
		bottomPanel.add(m_cancel);
		m_cancel.addActionListener(this);


		contentPane.add( BorderLayout.SOUTH, bottomPanel);

		// Resize the window to the preferred size of its components
		pack();
	}

	public Vector getResult()
	{
		return m_result;
	}

	// ActionListener interface

	public void actionPerformed(ActionEvent ev)
	{
		Object source = ev.getSource();

		// Pop down the window when the ok button is clicked.
		// System.out.println("event: " + ev);

		if (source == m_ok) {
			m_result = getInheritance();
		} else if (source != m_cancel) {
			return;
		}
		setVisible(false);
		return;
	}
} 



