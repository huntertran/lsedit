package lsedit;

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;

/* This class augments JColorChooser to allow optionally the
 * inclusion of a null color and an alpha associated with a color
 */

public class ColorChooser extends JDialog implements ActionListener {

	/* m_tcc is static so preserves chosen colors */

	private static	JColorChooser m_tcc = null;
	private JSlider m_slider;
	private JButton m_okButton, m_canButton, m_nullButton, m_clearButton;
	private Color	m_color;
	int				alpha;

	public ColorChooser(JFrame frame, String title, Color oldColor, boolean include_alpha, boolean allow_null) 
	{
		super(frame, title, true /* modal */);

		Container	contentPane;
		Font		font, bold;
		JLabel		label;

		font         = FontCache.getDialogFont();
		bold         = font.deriveFont(Font.BOLD);

		m_slider = null;
		m_color  = oldColor;
		contentPane = getContentPane();

		//Set up color chooser for setting text color
		if (m_tcc == null) {
			m_tcc	 = new JColorChooser();
//			m_tcc.setDragEnabled(true);
		}
		m_tcc.setFont(font);
		m_tcc.setColor(((oldColor == null) ? Color.gray : oldColor));

		contentPane.add("North", m_tcc);

		// Create an Okay button in a Panel; add the Panel to the window

		// Use a FlowLayout to center the button and give it margins.

		JPanel p = new JPanel();

		p.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));

		if (include_alpha) {
			JPanel p1 = new JPanel();

			m_slider = new JSlider(0, 255, 255);
			m_slider.setFont(font);
			m_slider.setMajorTickSpacing(50);
			m_slider.setPaintLabels(true);
			m_slider.setPaintTicks(true);

			if (oldColor == null) {
				alpha = 255;
			} else {
				alpha = oldColor.getAlpha();
			}
			m_slider.setValue(alpha);
			p1.add("Top", m_slider);
			label = new JLabel("Alpha", JLabel.CENTER);
			label.setFont(bold);
			p1.add("Bottom", label);

			contentPane.add("Center", p1);
		}
		m_okButton = new JButton("OK");
		m_okButton.setFont(bold);
		m_okButton.addActionListener(this);
		p.add(m_okButton);

		if (!allow_null) {
			m_nullButton = null;
		} else {
			m_nullButton = new JButton("Null");
			m_nullButton.setFont(bold);
			m_nullButton.addActionListener(this);
			p.add(m_nullButton);
		}

		m_clearButton = new JButton("Clear Cache");
		m_clearButton.setFont(bold);
		m_clearButton.addActionListener(this);
		p.add(m_clearButton);

		m_canButton = new JButton("Cancel");
		m_canButton.setFont(bold);
		m_canButton.addActionListener(this);

		p.add(m_canButton);

		contentPane.add("South", p);

		setLocation(20, 20);
		
		// Resize the window to the preferred size of its components
		pack();
		setVisible(true);
	}

	// Pop down the window when the button is clicked.

	public void actionPerformed(ActionEvent e)
	{
		Object source = e.getSource();

		if (source != m_canButton) {
			if (source == m_okButton) {
				m_color = m_tcc.getColor();
				if (m_slider != null) {
					int alpha;

					alpha = m_slider.getValue();
					if (alpha != 255) {
						m_color = ColorCache.get(m_color.getRed(), m_color.getGreen(), m_color.getBlue(), alpha);
				}	}
			} else if (source != null && source == m_nullButton) {
				m_color = null;
			} else if (source == m_clearButton) {
				int	cnt = ColorCache.size();
				ColorCache.clear();
				JOptionPane.showMessageDialog(this, cnt + " colors have been removed from the cache");
				return;
 			} else {
				return;
		}	}
		setVisible(false);
	}

	public Color getColor()
	{
		return(m_color);
	}
}
