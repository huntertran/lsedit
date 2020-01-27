package lsedit;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Graphics;


public class HistoryPrev_Button extends ToolBarButton
{
	protected static final String description = "Go to prev entity in history";
	protected static final int    MARGIN      = 2;

	protected String getHelpString()
	{
		return Do.g_prev_text;
	}

	protected String getDesc() 
	{
		return description;
	}

	protected void paintIcon(Graphics gc) 
	{
		int	w = getWidth();
		int h = getHeight();

		int[] x = new int[8];
		int[] y = new int[8];

		x[0]  = w/2;
		y[0]  = h - MARGIN;
		x[1]  = MARGIN;
		y[1]  = h/2;
		x[2]  = w/3;
		y[2]  = y[1];
		x[3]  = x[2];
		y[3]  = MARGIN;
		x[4]  = (2*w)/3;
		y[4]  = y[3];
		x[5]  = x[4];
		y[5]  = y[1];
		x[6]  = w - MARGIN;
		y[6]  = y[5];
		x[7]  = x[0];
		y[7]  = y[0];

		if (isEnabled()) {
			gc.setColor(Color.cyan);
		} else {
			gc.setColor(Color.lightGray);
		}
		gc.fillPolygon(x, y, 8);
		gc.setColor(Color.black);
		gc.drawPolygon(x, y, 8);
	}

	public HistoryPrev_Button(ToolBarEventHandler teh) 
	{
		super(teh);
		setKeystroke(0, Do.PREV_HISTORY);
	}
}


