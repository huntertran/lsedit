package lsedit;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

class ElisionChooser extends JDialog implements ItemListener, ActionListener
{
	private JCheckBox	m_checkboxes[];

	private JButton		m_ok;
	private JButton		m_and  = null;
	private JButton		m_or   = null;
	private JButton		m_nand = null;
	private JButton		m_cancel;
	private int			m_result = -1;
	
	public ElisionChooser(JFrame frame, int elisions)
	{
		super(frame, "Select desired elisions", true);

		Container			contentPane;
		JCheckBox			checkBox;
		ToolBarButton		icon;
		String				desc, state;
		Font				font, bold;
		int					i, mask;
		boolean				flag, flag1;

		mask = elisions;

		font = FontCache.getDialogFont();
		bold = font.deriveFont(Font.BOLD);


		JPanel topPanel    = new JPanel();	

		GridBagLayout		gridBagLayout = new GridBagLayout();
		GridBagConstraints	c             = new GridBagConstraints();

		c.weightx = 1.0;

		topPanel.setLayout(gridBagLayout);

		setForeground(ColorCache.get(0,0,0));
		setBackground(ColorCache.get(192,192,192));
		setFont(font);

		m_checkboxes    = new JCheckBox[EntityInstance.ELISIONS + 1];

		for (i = 0; i <= EntityInstance.ELISIONS; ++i) {
			flag  = flag1 = ((mask & 1) != 0);
			switch (i) {
			case EntityInstance.ELISIONS:
				state = "";
				break;
			case EntityInstance.CLOSED_ELISION:
				flag1 = !flag;
			default:
				state = (flag1 ? "Hide" : "Show");
			}
			
			switch (i) {
			case EntityInstance.DST_ELISION:
				icon = new Elision_u_Button(null);
				desc = " destination edges";
				break;
			case EntityInstance.SRC_ELISION:
				icon = new Elision_s_Button(null);
				desc = " source edges";
				break;
			case EntityInstance.ENTERING_ELISION:
				icon = new Elision_CU_Button(null);
				desc = " entering edges";
				break;
			case EntityInstance.EXITING_ELISION:
				icon = new Elision_CS_Button(null);
				desc = " exiting edges";
				break;
			case EntityInstance.INTERNAL_ELISION:
				icon = new Elision_I_Button(null);
				desc = " internal edges";
				break;
			case EntityInstance.CLOSED_ELISION:
				icon = new Elision_c_Button(null);
				desc = " children";
				break;
			default:	// EntityInstance.ELISIONS
				icon = null;
				if (flag) {
					desc = "Set above elisions";
				} else {
					desc = "Preserve elisions unchanged";
				}
			}
			if (i != EntityInstance.ELISIONS) {
				desc = state + desc;
			}
			
			c.anchor    = GridBagConstraints.EAST;

			if (icon != null) {
				c.gridx = 0;
				gridBagLayout.setConstraints(icon, c);
				topPanel.add(icon);
				c.gridx     = 1;
			} else {
				c.gridx     = 0;
				c.gridwidth = 2;
			}

			m_checkboxes[i] = checkBox = new JCheckBox(desc, flag);
			c.anchor    = GridBagConstraints.WEST;
			c.gridwidth = GridBagConstraints.REMAINDER;

			gridBagLayout.setConstraints(checkBox, c);
			topPanel.add(checkBox);
			checkBox.addItemListener(this);

			c.weightx   = 0.0;
			c.gridwidth = GridBagConstraints.RELATIVE;
			mask >>= 1;
		}

		contentPane = getContentPane();
		contentPane.add( BorderLayout.NORTH, topPanel );

		// --------------
		// Use a FlowLayout to center the button and give it margins.

		JPanel bottomPanel = new JPanel();

		bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));

		m_ok = new JButton("Set");
		m_ok.setFont(bold);
		bottomPanel.add(m_ok);
		m_ok.addActionListener(this);

		if ((elisions & SelectedElisions.SUMMARY) != 0) {
			m_and = new JButton("And");
			m_and.setFont(bold);
			bottomPanel.add(m_and);
			m_and.addActionListener(this);

			m_or = new JButton("Or");
			m_or.setFont(bold);
			bottomPanel.add(m_or);
			m_or.addActionListener(this);

			m_nand = new JButton("Nand");
			m_nand.setFont(bold);
			bottomPanel.add(m_nand);
			m_nand.addActionListener(this);
		}

		m_cancel = new JButton("Cancel");
		m_cancel.setFont(bold);
		bottomPanel.add(m_cancel);
		m_cancel.addActionListener(this);

		contentPane.add( BorderLayout.SOUTH, bottomPanel);

		// Resize the window to the preferred size of its components
		pack();
		setVisible(true);
	}

	private int	getNewElisionValue()
	{
		JCheckBox[]	checkboxes = m_checkboxes;
		int			elisions   = checkboxes.length;
		int			ret	       = 0;
		int			mask       = 1;
		JCheckBox	checkbox;
		int			i;

		for (i = 0; i < elisions; ++i) {
			checkbox = checkboxes[i];
			if (checkbox != null && checkbox.isSelected()) {
				ret |= mask;
			}
			mask <<= 1;
		}
		return ret;
	}

	public int getElisions()
	{
		return m_result;
	}

	// ItemListener interface

	public void itemStateChanged(ItemEvent ev)
	{
		JCheckBox[]	checkboxes   = m_checkboxes;
		JCheckBox	checkBox     = (JCheckBox) ev.getItem();
		JCheckBox	unchangedBox = checkboxes[EntityInstance.ELISIONS];
		boolean		state        = (ev.getStateChange() == ItemEvent.SELECTED);
		String		desc;

		if (checkBox != unchangedBox) {
			String		text       = checkBox.getText();
			String		text1      = text.substring(4);
			boolean		state1     = state;

			if (checkBox == checkboxes[EntityInstance.CLOSED_ELISION]) {
				state1 = !state1;
			}
			checkBox.setText((state1 ? "Hide" : "Show") + text1);

			checkBox = unchangedBox;
			state    = true;
			checkBox.setSelected(state);
		} 
		if (state) {
			desc = "Set above elisions";
		} else {
			desc = "Preserve elisions unchanged";
		}
		checkBox.setText(desc);
 	}

	// ActionListener interface

	public void actionPerformed(ActionEvent ev)
	{
		Object	source = ev.getSource();
		int		action = -2;

		if (source == m_ok) {
			action = 0;
		} else if (source == m_cancel) {
			action = -1;
		} else if (source == m_and) {
			action = SelectedElisions.AND_ELISIONS;
		} else if (source == m_or) {
			action = SelectedElisions.OR_ELISIONS;
		} else if (source == m_nand) {
			action = SelectedElisions.NAND_ELISIONS;
		} else {
			return;
		}
		if (action >= 0) {
			m_result  = getNewElisionValue();
			m_result |= action;
		}
		setVisible(false);
		return;
	}
}

