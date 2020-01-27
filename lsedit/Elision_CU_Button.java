package lsedit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Graphics;

import javax.swing.Icon;

public class Elision_CU_Button extends ToolBarButton implements Icon 
{
	protected static final String description = "Hide/show edges going from outside of selected entities to inside";

	protected String getHelpString()
	{
		return Do.g_entering_edges_text;
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
		gc.fillRect(MARGIN*2, MARGIN+(h/6), w, h-(h/6));
		gc.setColor(Color.black);
		gc.drawRect(MARGIN*2, MARGIN+(h/6), w, h-(h/6));

		int x = MARGIN*2 + w/2;

		drawEdge(gc, x, MARGIN, x, dim.height/2);
	}

	public Elision_CU_Button(ToolBarEventHandler teh) 
	{
		super(teh);
		setKeystroke(Event.SHIFT_MASK, Do.ENTERING_EDGES);
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


