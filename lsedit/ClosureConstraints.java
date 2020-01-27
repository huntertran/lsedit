package lsedit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ClosureConstraints extends JDialog implements ActionListener { 

	private JButton	m_ok      = null;
	
	protected ClosureConstraints(JFrame frame, Diagram diagram, RelationClass rc)
	{
		super(frame, "Closure of constraints on " + rc.getLabel(), true); //false if non-modal

		Container	contentPane;
		JCheckBox	checkBox;
		JScrollPane	scrollPane;
		JPanel		panel;
		JLabel		label;
		Font		font, bold;

		Enumeration	en;
		EntityClass	ec;
		int			i, j, size;
		EntityClass	ecs[];
		boolean[][] array = rc.getInheritedRelationArray();

		font    = FontCache.getDialogFont();
		bold    = font.deriveFont(Font.BOLD);

		size    = diagram.numEntityClasses();
		ecs     = new EntityClass[size];

		i = 0;
		for (en = diagram.enumEntityClassesInOrder(); en.hasMoreElements(); ++i) {
			ec = (EntityClass) en.nextElement();
			ecs[i] = ec;
		}

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
			label = new JLabel(ecs[i].getLabel());
			label.setFont(bold);
			panel.add(label);
		}

		for (i = 0; i < size; ++i) {
			label = new JLabel(ecs[i].getLabel() + " ");
			label.setFont(bold);
			panel.add(label);

			for (j = 0; j < size; ++j) {
				checkBox = new JCheckBox();
				checkBox.setFont(font);
				checkBox.setToolTipText(ecs[i].getLabel() + "->" + ecs[j].getLabel());
				checkBox.setEnabled(false);
//				checkBox.setSelected(array[i][j]);
				if (array[i][j]) {
					checkBox.setSelected(true);
					checkBox.setForeground(Color.RED);
				}
				panel.add(checkBox);
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

		contentPane.add(panel, BorderLayout.SOUTH);

		pack();
		setVisible(true);
	}

	public static void create(Diagram diagram, RelationClass rc) 
	{
		LandscapeEditorCore ls = diagram.getLs();

		ClosureConstraints closureConstraints = new ClosureConstraints(ls.getFrame(), diagram, rc);
		closureConstraints.dispose();
	}

	// ActionListener interface

	public void actionPerformed(ActionEvent ev)
	{
		Object	source;

		// Pop down the window when the button is clicked.
		// System.out.println("event: " + ev);

		source = ev.getSource();

	

		if (source != m_ok) {
			return;
		}
		this.setVisible(false);
	}
}



