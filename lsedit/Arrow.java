package lsedit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.*;

public class Arrow extends JComponent { 

	private	int[]	  m_x;
	private int[]     m_y;
	private Color	  m_headColor;
	private int		  m_style;
	private int		  m_width;
	private int		  m_base;
	private	boolean	  m_invert;

	protected Arrow(int width, int height)
	{
		Dimension dimension = new Dimension(width, height);
		m_x         = new int[3];
		m_y         = new int[3];
		m_width     = width;
		m_base      = height/2;
		m_invert    = false;

		Util.getArrow(0, m_base, width, m_base, m_x, m_y, 1);

		setMinimumSize(dimension);
		setPreferredSize(dimension);
		setMaximumSize(dimension);
		setSize(dimension);
		m_style = Util.LINE_STYLE_NORMAL;
	}

	public void setHeadColor(Color color)
	{
		m_headColor = color;
	}

	public void setInvert(boolean value)
	{
		if (m_invert != value) {
			if (value) {
				Util.getArrow(m_width, m_base, 0, m_base, m_x, m_y, 1);
			} else {
				Util.getArrow(0, m_base, m_width, m_base, m_x, m_y, 1);
			}
			m_invert = value;
		}
		repaint();
	}

	public void setStyle(int style)
	{
		m_style = style;
	}

	public void paintComponent(Graphics g)
	{
		Color	color;
		int	height = getHeight() / 2;

		color = getForeground();
		if (color != null) {
			g.setColor(color);
		}
		Util.drawSegment(g, m_style, 0, height, getWidth(), height);
		color = m_headColor;
		if (color != null) {
			g.setColor(color);
		}
		g.fillPolygon(m_x, m_y, 3);
}	}
