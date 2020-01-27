package lsedit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.Icon;

public class Elision_s_Button extends ToolBarButton implements Icon
{
	protected static final String description = "Hide/show edges whose source are selected entities";

	protected String getHelpString()
	{
		return Do.g_src_edges_text;
	}

	protected String getDesc()
	{
		return description;
	}

	public void paintIcon(Graphics gc) 
	{
		Dimension dim = getSize();
		int w = dim.width/2;
		int h = dim.height/2 - MARGIN*2;

		gc.setColor(Color.cyan);
		gc.fillRect(MARGIN*2, MARGIN*2, w, h);
		gc.setColor(Color.black);
		gc.drawRect(MARGIN*2, MARGIN*2, w, h);

		int x = MARGIN*2 + w/2;
		drawEdge(gc, x, dim.height/2, x, dim.height-MARGIN);
	}

	public Elision_s_Button(ToolBarEventHandler teh) 
	{
		super(teh);
		setKeystroke(0, Do.SRC_EDGES);
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


