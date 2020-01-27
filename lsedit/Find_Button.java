package lsedit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.Graphics;



public class Find_Button extends ToolBarButton
{

	protected static final Font cfont = FontCache.get("Helvetica", Font.BOLD, 16);
	protected static final String description = "Find entities (search)";

	protected String getHelpString()
	{
		return Do.g_find_query_text;
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

		if (Util.isBlack(c)) {
			c = Color.lightGray;
		}

		gc.setColor(c);
		gc.fillRect(MARGIN*2, MARGIN, w, h);
		gc.setColor(Color.black);
		gc.drawRect(MARGIN*2, MARGIN, w, h);

		int x, y;

		x = MARGIN*4;
		y = h/2-2;
		int sw = w/4;
		int sh = sw/2;

		gc.setColor(Color.red);
		gc.fillRect(x, y, sw, sh);
		gc.fillRect(x+2, y+sh+2, sw, sh);
		gc.fillRect(x-2, y+sh*2+4, sw, sh);

		x = MARGIN*2 + w/4;
		gc.setColor(Color.black);
		gc.setFont(cfont);
		gc.drawString("?", x + w/4, dim.height/2 + dim.height/4);
	}



	public Find_Button(ToolBarEventHandler teh) 
	{
		super(teh);

		setKeystroke(Event.CTRL_MASK, Do.FIND_QUERY);
	}

}

