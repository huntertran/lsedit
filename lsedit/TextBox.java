package lsedit;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class TextBox extends JTextArea
{
	protected static final Color titleColor = Color.red.darker();

	protected JScrollPane		  m_scroller;	// null if not under any JScrollPane
	protected String			  m_helpString;

	// --------------
	// Public methods 
	// --------------

	public TextBox(JScrollPane scroller, String helpString) 
	{
		m_scroller    = scroller;

		scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
		scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 

		m_helpString  = helpString;
		setToolTipText(helpString);

		setBackground(Diagram.boxColor);
		setFont(Options.getTargetFont(Option.FONT_TEXTBOX_TEXT));
		setEditable(false);
//		setSize(5, 5);
		setLineWrap(true);


		if (scroller != null) {
			scroller.setViewportView(this);
	}	}

	public void set(String text) 
	{
		setText(text); 
		repaint();
	}
}





