package lsedit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Graphics;

import javax.swing.Icon;

public class Elision_I_Button extends ToolBarButton implements Icon
{
	protected static final String description = "Hide/show internal edges between entities whose nearest common ancestor are selected entities";

	protected String getHelpString()
	{
		return Do.g_internal_edges_text;
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
		Color c = ColorCache.get(0.70f, 0.70f, 0.70f);
		if (Util.isBlack(c))
			c = Color.lightGray;
		gc.setColor(c);
		gc.fillRect(MARGIN*2, MARGIN, w, h);
		gc.setColor(Color.black);
		gc.drawRect(MARGIN*2, MARGIN, w, h);

		int x1 = MARGIN*4;
		int y1 = MARGIN*4;
		int x2 = x1 + w/2;
		int y2 = y1 + 2;
		int x3 = x1 + 2;
		int y3 = y1 + h/2;

		drawEdge(gc, x1, y1, x2, y2);
		drawEdge(gc, x1, y1, x3, y3);
	}

	public Elision_I_Button(ToolBarEventHandler teh) 
	{
		super(teh);
		setKeystroke(Event.SHIFT_MASK, Do.INTERNAL_EDGES);
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


