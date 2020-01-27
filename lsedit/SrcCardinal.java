package lsedit;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JComponent;

class SrcCardinal extends Cardinal {

	public SrcCardinal()
	{
		super();
	}

	// Center at top the cardinal

	public void setCenterTop(int x, int y, int width, int height, double f)
	{
//		System.out.println("setCenterTop " + x + "x" + y);

		setFont(m_font);

		if (m_fm == null) {
			m_fm = getFontMetrics(getFont());
			m_h  = m_fm.getAscent();
		}
		m_w  = m_fm.stringWidth("" + m_cnt);
		x   += ((double) width) * f;
		y   -= (m_h + MARGIN);

		this.setBounds(x - (m_w/2) - 2, y, m_w + 4, m_h + 4);
	}

/*
	public void paintComponent(Graphics g)
	{
		int width, height;

//		System.out.println("SrcCardinal.paint cardinal Component()");
		
		width  = getWidth();
		height = getHeight();


		int[] xp = new int[5];
		int[] yp = new int[5];

		xp[0] = yp[4] = 0;
		yp[0] = yp[2] = yp[4] = m_h/2;
		xp[1] = xp[3] = m_w/2;
		yp[1] = 0;
		xp[2] = m_w - 1;
		yp[3] = m_h - 1;

		g.setFont(m_font);
		g.setColor(getBackground());
		g.fillPolygon(xp, yp, 5);
		g.setColor(Color.black);
		g.drawPolygon(xp, yp, 5);
		g.setColor(getForeground());
		g.drawString(""+m_cnt, 1, m_h - 2);

//		System.out.println("SrcCardinal.paintComponent() done");
	}
*/

	public void paintComponent(Graphics g)
	{
		int width, height;

//		System.out.println("SrcCardinal.paint cardinal Component()");
		
		width  = getWidth();
		height = getHeight();

		g.setFont(m_font);
		g.setColor(getBackground());
		g.fillRect(1, 1, width-2, height-2);
		g.setColor(Color.black);
		g.drawRect(0, 0, width-1, height-1);
		g.setColor(getForeground());
		g.drawString(""+m_cnt, 1, m_h - 2);

//		System.out.println("SrcCardinal.paintComponent() done");
	}

	public String toString()
	{
		return("SrcCardinal " + m_cnt);
	}
}
