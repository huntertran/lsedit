package lsedit;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Graphics;


public class NavPrev_Button extends ToolBarButton
{
	protected static final String description = "Go to prev landscape";
	protected static final int    MARGIN      = 2;

	static int[] m_xp = new int[8];
	static int[] m_yp = new int[8];

	protected String getHelpString()
	{
		return Do.g_nav_prev_text;
	}

	protected String getDesc() 
	{
		return description;
	}

	public static void paintIcon(Graphics gc, int x, int y, int boxWidth, int boxHeight, int width, int height)
	{
		int	xshift = boxWidth  - width;
		int	yshift = boxHeight - height;
		int	i;

		if (xshift < 0) {
			return;
		}
		if (yshift < 0) {
			return;
		}

		m_xp[0]  = MARGIN;
		m_yp[0]  = height/2;
		m_xp[1]  = width/2;
		m_yp[1]  = MARGIN;
		m_xp[2]  = m_xp[1];
		m_yp[2]  = height/3;
		m_xp[3]  = width - MARGIN;
		m_yp[3]  = m_yp[2];
		m_xp[4]  = m_xp[3];
		m_yp[4]  = (2*height)/3;
		m_xp[5]  = m_xp[1];
		m_yp[5]  = m_yp[4];
		m_xp[6]  = m_xp[5];
		m_yp[6]  = height - MARGIN;
		m_xp[7]  = m_xp[0];
		m_yp[7]  = m_yp[0];

		xshift += x;
		if (xshift != 0) {
			for (i = 8; --i >= 0; ) {
				m_xp[i] += xshift;
		}	}
		yshift += y;
		if (yshift != 0) {
			for (i = 8; --i >= 0; ) {
				m_yp[i] += yshift;
		}	}
		gc.fillPolygon(m_xp, m_yp, 8);
		gc.setColor(Color.black);
		gc.drawPolygon(m_xp, m_yp, 8);
	}

	protected void paintIcon(Graphics gc) 
	{
		int	w = getWidth();
		int h = getHeight();

		gc.setColor(Color.cyan);
		paintIcon(gc, 0, 0, w, h, w, h);
	}

	public NavPrev_Button(ToolBarEventHandler teh) 
	{
		super(teh);
		setKeystroke(Event.CTRL_MASK, Do.NAV_PREV);
	}
}


