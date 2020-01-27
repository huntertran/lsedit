package lsedit;

import java.util.Enumeration;
import java.util.Vector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
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

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JLabel;

class ImageCheckBox extends JCheckBox implements Icon
{
	int	m_image;

	ImageCheckBox(String name, boolean isSelected, int image)
	{
		super(name, isSelected);
		m_image = image;
		setIcon(this);
	}

	// Icon interface (used to paint image as icon)

	public int getIconWidth()
	{
		return(50);
	}

	public int getIconHeight()
	{
		return(30);
	}

	public void paintIcon(Component c, Graphics g, int x, int y)
	{
		Color	color;

		g.translate(x, y);

		color = (isSelected() ? Color.blue : Color.black);
		g.setColor(color);
		EntityComponent.paintImage(g, m_image, 0, 0, 50, 30, true);
}	}


class ImageChooser extends JDialog implements ActionListener, ItemListener {

	class LayoutImage extends JComponent
	{
		public LayoutImage()
		{
			super();

			Dimension	dimension = new Dimension(240,240);

			setLayout(null);
			setPreferredSize(dimension);
			setMinimumSize(dimension);
			setMaximumSize(dimension);
			setSize(dimension);

			setVisible(true);
		}
	
		public void paintComponent(Graphics g)
		{
			int	image = getNewImageValue();

			g.setColor(Color.black);
			EntityComponent.paintImage(g, image, 20, 20, 200, 200, true);
		}
	}

	protected ImageCheckBox[]	m_checkboxes;
	protected LayoutImage		m_layoutImage;
	protected JButton			m_ok;
	protected JButton			m_cancel;
	protected Integer			m_result;
	
	private int	getNewImageValue()
	{
		ImageCheckBox[]	checkboxes = m_checkboxes;
		int				images     = checkboxes.length;
		int				image      = 0;
		int				i;
		ImageCheckBox	checkbox;

		for (i = 0; i < images; ++i) {
			checkbox = checkboxes[i];
			if (checkbox.isSelected()) {
				image |= (1 << i);
		}	}
		return image;
	}

	public ImageChooser(JFrame frame, Integer integer)
	{
		super(frame, "Select image properties", true);

		Container			contentPane;
		Font				font, bold;
		JLabel				label;
		ImageCheckBox		checkBox;
		int					image, i, mask;
		int					images       = EntityClass.imageName.length;

		m_result = integer;
		image    = integer.intValue();
		font     = FontCache.getDialogFont();
		bold     = font.deriveFont(Font.BOLD);

		setForeground(ColorCache.get(0,0,0));
		setBackground(ColorCache.get(192,192,192));
		setFont(font);

		JPanel topPanel    = new JPanel();	// Contains options

		GridLayout gridLayout = new GridLayout(images, 1, 0, 0);

		topPanel.setLayout(gridLayout);

		m_checkboxes = new ImageCheckBox[images];
		mask  = 1;
		for (i = 0; i < images; ++i) {
			m_checkboxes[i] = checkBox = new ImageCheckBox(EntityClass.imageName[i], (image & mask) != 0, mask);
			checkBox.setFont(bold);
			topPanel.add(checkBox);
			checkBox.addItemListener(this);
			mask <<= 1;
		}

		contentPane = getContentPane();
		contentPane.add( BorderLayout.NORTH, topPanel );

		m_layoutImage = new LayoutImage();
		m_layoutImage.validate();
		contentPane.add( BorderLayout.CENTER, m_layoutImage);

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
		setVisible(true);
	}

	public Integer getImage()
	{
		return m_result;
	}

/*
 * ItemListener interface
 */
    public void itemStateChanged(ItemEvent e) 
	{
		if (e.getSource() instanceof ImageCheckBox) {
			m_layoutImage.repaint();
	}	}

	// ActionListener interface

	public void actionPerformed(ActionEvent ev)
	{
		Object			source;

		source = ev.getSource();

		if (source == m_ok) {
			int	newvalue = getNewImageValue();

			if (newvalue != m_result.intValue()) {
				m_result = new Integer(newvalue);
			}
		} else if (source != m_cancel) {
			return;
		}
		setVisible(false);
		return;
	}
}

