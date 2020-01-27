package lsedit;


import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.Icon;

public class Elision_c_Button extends ToolBarButton implements Icon
{
	protected static final String description = "Open/close container";

	protected String getHelpString()
	{
		return Do.g_show_contents_text;
	}

	protected String getDesc() 
	{
		return description;
	}

	public void paintIcon(Graphics gc) 
	{
		Dimension dim = getSize();
		int w = dim.width/2;
		int h = dim.height - MARGIN*2 - 1;
		int[] xs = new int[5];
		int[] ys = new int[5];
		xs[0] = MARGIN*2;
		ys[0] = MARGIN;
		xs[1] = xs[0] + w;
		ys[1] = ys[0];
		xs[2] = xs[1];
		ys[2] = ys[0] + h/2;
		xs[3] = xs[0] + w/2;
		ys[3] = ys[0] + h;
		xs[4] = xs[0];
		ys[4] = ys[3];

		gc.setColor(Color.gray);
		gc.fillPolygon(xs, ys, 5);
		xs[0] = xs[2];
		ys[0] = ys[2];
		xs[1] = xs[3];
		ys[1] = ys[3];
		xs[2] = xs[1];
		ys[2] = ys[0];
		xs[3] = xs[0];
		ys[3] = ys[0];

		Color c = ColorCache.get(0.70f, 0.70f, 0.70f);
		if (!Util.isBlack(c)) {
			gc.setColor(c);
			gc.fillPolygon(xs, ys, 3);
		}
		gc.setColor(Color.black);
		gc.drawRect(MARGIN*2, MARGIN, w, h);
		gc.drawPolygon(xs, ys, 4);
	}

	public Elision_c_Button(ToolBarEventHandler teh) 
	{
		super(teh);
		setKeystroke(0, Do.SHOW_CONTENTS);
	}

	// Icon interface

    public void paintIcon(Component c, Graphics g, int x, int y)
	{
		g.translate(x, y);
		paintIcon(g);
	}
    
    public int getIconWidth()
	{
		return getWidth();
	}

    public int getIconHeight()
	{
		return getHeight();
	}
}


