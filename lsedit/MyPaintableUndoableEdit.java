package lsedit;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.undo.UndoableEdit;

// Something with the smarts to paint itself specially

public class MyPaintableUndoableEdit extends MyUndoableEdit implements UndoableEdit {

	// Helper methods 

	protected void paintComponentColor(Graphics g, int x, int y, Color color)
	{
		String		s = getPresentationName();
		FontMetrics	fm;
		int			x1;
		Color		save;

		g.drawString(s, x, y+UndoBox.m_baseline);
		fm    = g.getFontMetrics(UndoBox.m_textFont);
		x1    = x + fm.stringWidth(s);
		if (color == null) {
			g.drawRect(x1, y+3, UndoBox.m_fontheight-6, UndoBox.m_fontheight-3);
		} else {
			save = g.getColor();
			g.setColor(color);
			g.fillRect(x1, y+3, UndoBox.m_fontheight-6, UndoBox.m_fontheight-3);
			g.setColor(save);
	}	}

	public int getPreferredWidthColor(LandscapeObject o)
	{
		FontMetrics	fm;

		fm   = o.getDiagram().getFontMetrics(UndoBox.m_textFont);
		return(fm.stringWidth(getPresentationName()) + UndoBox.m_fontheight);
	}

	public void paintComponent(Graphics g, int x, int y)
	{
	}

	public int getPreferredWidth()
	{
		return(0);
}	}

