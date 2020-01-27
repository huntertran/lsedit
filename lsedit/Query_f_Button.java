package lsedit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

public class Query_f_Button extends ToolBarButton 
{
	protected static final Font cfont = FontCache.get("Helvetica", Font.BOLD, 16);
	protected static final String description = "Forward edge query";

	protected String getHelpString()
	{
		return Do.g_forward_query_text;
	}

	protected String getDesc() 
	{
		return description;
	}

	protected void paintIcon(Graphics gc) 
	{
		Dimension dim = getSize();

		int w = dim.width/2;
		int h = dim.height/2 - MARGIN*2;

		gc.setColor(Color.red);
		gc.fillRect(MARGIN*2, MARGIN*2, w, h);
		gc.setColor(Color.black);
		gc.drawRect(MARGIN*2, MARGIN*2, w, h);

		int x = MARGIN*2 + w/4;
		drawEdge(gc, x, dim.height/2, x, dim.height-MARGIN);
		// gc.setColor(Color.cyan);
		gc.setFont(cfont);
		gc.drawString("?", x + w/4, dim.height - MARGIN*2);
	}

	public Query_f_Button(ToolBarEventHandler teh) 
	{
		super(teh);
		setKeystroke(0, Do.FORWARD_QUERY);
	}
}


