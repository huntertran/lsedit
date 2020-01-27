package lsedit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

public class Query_b_Button extends ToolBarButton 
{
	protected static final Font cfont = FontCache.get("Helvetica", Font.BOLD, 16);
	protected static final String description = "Backtrace edge query";

	protected String getHelpString()
	{
		return Do.g_backward_query_text;
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
		gc.fillRect(MARGIN*2, dim.height/2, w, h);
		gc.setColor(Color.black);
		gc.drawRect(MARGIN*2, dim.height/2, w, h);
		int x = MARGIN*2 + w/4;
		drawEdge(gc, x, MARGIN*2, x, dim.height/2);
		gc.setColor(Color.black);
		gc.setFont(cfont);
		gc.drawString("?", x + w/4, dim.height/2 + dim.height/4);
	}

	public Query_b_Button(ToolBarEventHandler teh) 
	{
		super(teh);
		setKeystroke(0, Do.BACKWARD_QUERY);
	}
}


