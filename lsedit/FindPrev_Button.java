package lsedit;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;


public class FindPrev_Button extends ToolBarButton
{
	protected static final String description = "Go to previous layer in find";
	protected static final int    MARGIN      = 2;

	protected String getHelpString()
	{
		return Do.g_find_prev_text;
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

		x[0]  = MARGIN;
		y[0]  = h/2;
		x[1]  = w/2;
		y[1]  = MARGIN;
		x[2]  = x[1];
		y[2]  = h/3;
		x[3]  = w - MARGIN;
		y[3]  = y[2];
		x[4]  = x[3];
		y[4]  = (2*h)/3;
		x[5]  = x[1];
		y[5]  = y[4];
		x[6]  = x[5];
		y[6]  = h - MARGIN;
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

	public FindPrev_Button(ToolBarEventHandler teh) 
	{
		super(teh);
		setKeystroke(0, Do.FIND_PREV);
	}
}


