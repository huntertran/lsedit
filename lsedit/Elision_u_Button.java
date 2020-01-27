package lsedit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.Icon;

public class Elision_u_Button extends ToolBarButton implements Icon
{
	protected static final String description = "Hide/show edges whose target are selected entities";

	protected String getHelpString()
	{
		return Do.g_dst_edges_text;
	}

	protected String getDesc() {
		return description;
	}

	public void paintIcon(Graphics gc) {
		Dimension dim = getSize();

		int w = dim.width/2;
		int h = dim.height/2 - MARGIN*2;
		
		gc.setColor(Color.cyan);
		gc.fillRect(MARGIN*2, dim.height/2, w, h);

		gc.setColor(Color.black);
		gc.drawRect(MARGIN*2, dim.height/2, w, h);

		int x = MARGIN*2 + w/2;

		drawEdge(gc, x, MARGIN*2, x, dim.height/2);
	}

	public Elision_u_Button(ToolBarEventHandler teh) {
		super(teh);
		setKeystroke(0, Do.DST_EDGES);
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
