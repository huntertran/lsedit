package lsedit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Graphics;

public class FontBiggerButton extends ToolBarButton
{
	protected static final String description = "Increase font size";

	protected String getHelpString()
	{
		return Do.g_increase_font_size_text;
	}

	protected String getDesc() 
	{
		return description;
	}

	protected void paintIcon(Graphics gc) 
	{
		Dimension dim = getSize();

		int w = dim.width/2;
		int[] xs = new int[3];
		int[] ys = new int[3];
		xs[0] = MARGIN*2 + w/4;
		ys[0] = dim.height/2 + MARGIN;
		xs[1] = xs[0] + w/2 + 1;
		ys[1] = ys[0];
		xs[2] = xs[0] + w/4;
		ys[2] = ys[0] - dim.height/4;

		gc.setColor(Color.gray);
		gc.fillPolygon(xs, ys, 3);
		gc.setColor(Color.black);
		gc.drawPolygon(xs, ys, 3);
	}

	public FontBiggerButton(ToolBarEventHandler teh) 
	{
		super(teh);

		setKeystroke(Event.SHIFT_MASK, Do.INCREASE_LABEL_FONT);
		setSize(WIDTH - WIDTH/4, HEIGHT);
	}
}


