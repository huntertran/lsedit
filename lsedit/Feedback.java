package lsedit;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JTextField;

public class Feedback extends JTextField {

	// --------------
	// Public Methods
	// --------------

	public Feedback(String helpString) 
	{
		setToolTipText(helpString);

		setBackground(Diagram.boxColor);
		setForeground(Color.black);
		setFont(Options.getTargetFont(Option.FONT_FEEDBACK));
		setEditable(false);
		setVisible(true);
	}

	public void set(String str) 
	{
		setText(str);
		repaint();
	}

	public String get() 
	{
		return(getText());
	}
}

