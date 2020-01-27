package lsedit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Graphics;

import javax.swing.Icon;

public class Elision_CS_Button extends ToolBarButton implements Icon
{
	protected static final String description = "Hide/show edges going from inside selected entities to outside";

	protected String getHelpString()
	{
		return Do.g_exiting_edges_text;
	}

	protected String getDesc() 
	{
		return description;
	}

	protected void paintIcon(Graphics gc) 
	{
		Dimension dim = getSize();

		int w = dim.width/2;
		int h = dim.height - MARGIN*2 - 1;
		Color c = ColorCache.get(0.70f, 0.70f, 0.70f);

		if (Util.isBlack(c))
			c = Color.lightGray;

		gc.setColor(c);
		gc.fillRect(MARGIN*2, MARGIN, w, h-(h/4));
		gc.setColor(Color.black);
		gc.drawRect(MARGIN*2, MARGIN, w, h-(h/4));

		int x = MARGIN*2 + w/2;

		drawEdge(gc, x, dim.height/2, x, dim.height-MARGIN-1);
	 }

	public Elision_CS_Button(ToolBarEventHandler teh) 
	{
		super(teh);
		setKeystroke(Event.SHIFT_MASK, Do.EXITING_EDGES);
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


