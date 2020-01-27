package lsedit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

public class FontSmallerButton extends ToolBarButton
{
	protected static final String description = "Decrease font size";

	protected String getHelpString()
	{
		return Do.g_decrease_font_size_text;
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
		xs[0] = MARGIN*2 + w/2;
		ys[0] = dim.height/4 + MARGIN;
		xs[1] = xs[0] + w/2 + 1;
		ys[1] = ys[0];
		xs[2] = xs[0] + w/4;
		ys[2] = ys[0] + dim.height/4;

		gc.setColor(Color.gray);
		gc.fillPolygon(xs, ys, 3);
		gc.setColor(Color.black);
		gc.drawPolygon(xs, ys, 3);
	}

	public FontSmallerButton(ToolBarEventHandler teh) 
	{
		super(teh);

		setKeystroke(0, Do.DECREASE_LABEL_FONT);
		setSize(WIDTH - WIDTH/4, HEIGHT);
	}
}


