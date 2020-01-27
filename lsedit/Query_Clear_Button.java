package lsedit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;



public class Query_Clear_Button extends ToolBarButton
{
	protected static final Font cfont = FontCache.get("Helvetica", Font.BOLD, 16);
	protected static final Font font  = FontCache.get("Helvetica", Font.PLAIN, 8);
	protected static final String description = "Query/selection clear";

	protected String getHelpString()
	{
		return Do.g_clear_text;
	}

	protected String getDesc() 
	{
		return description;
	}

	protected void paintIcon(Graphics gc) 
	{
		Dimension dim = getSize();
		int h = dim.height - MARGIN*2;

		gc.setColor(Color.black);
		gc.setFont(cfont);
		gc.drawString("?", MARGIN*5-1, dim.height - MARGIN*2 - 1);

		int x = MARGIN*2;
		int y = MARGIN;

		gc.setColor(Color.red);
		gc.drawOval(x, y, h, h);
		gc.drawOval(x+1, y+1, h-2, h-2);
		gc.drawLine(x+2, y+h-3, x+h-MARGIN, y+MARGIN+2);
		gc.drawLine(x+3, y+h-3, x+h-MARGIN+1, y+MARGIN+2);

		// Draw ESC

		gc.setColor(Color.black);
		gc.setFont(font);
		FontMetrics fm = gc.getFontMetrics();
		String str = String.valueOf(((char) 'E'));
		int fw = fm.stringWidth(str);
		int fh = h/3;
		x = dim.width - fw*2 - MARGIN*2;
		y = fh + 2;
		gc.drawString(str, x, y);
		x += fw/2;
		y += fh+1; 
		str = String.valueOf(((char ) 'S'));
		gc.drawString(str, x, y);
		x += fw/2;
		y += fh+1;
		str = String.valueOf(((char ) 'C'));
		gc.drawString(str, x, y);
	}

	public Query_Clear_Button(ToolBarEventHandler teh) 
	{
		super(teh);
		setKeystroke(0, Do.ESCAPE);	/* Esc */
	}
}



