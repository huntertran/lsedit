package lsedit;

public class LineWalker extends Object {

	protected double segLen, segGap;

	protected double x, y;
	protected double dx1, dy1, dx2, dy2;

	protected int num;
	protected int seg = 0;

	protected double sign(double val) 
	{
		return (val < 0) ? -1 : 1;
	}

	public void init(int x1, int y1, int x2, int y2, int len, int gap) 
	{
		segLen = len;
		segGap = gap;

		x      = x1;
		y      = y1;

		double dx = x2-x1;
		double dy = y2-y1;

		double h = Math.sqrt(dx*dx + dy*dy);

		num = (int) ((h - segLen)/(segLen + segGap)) + 1;

		segGap = (h - (num * segLen))/(num-1);

		double angle = Math.asin(dy/h);
		double cos   = Math.cos(angle);
		double sin   = Math.sin(angle);

		double mfx   = (sign(dx) == sign(cos)) ? 1 : -1;
		double mfy   = (sign(dy) == sign(sin)) ? 1 : -1;

		dx1 = segLen * cos * mfx;
		dy1 = segLen * sin * mfy;

		dx2 = (segLen + segGap) * cos * mfx;
		dy2 = (segLen + segGap) * sin * mfy;
	}

	public LineWalker(int x1, int y1, int x2, int y2, int len, int gap) 
	{
		init(x1, y1, x2, y2, len, gap);
	}

	public LineWalker(ScreenPoint p1, ScreenPoint p2, int len, int gap) 
	{
		init(p1.x, p1.y, p2.x, p2.y, len, gap);
	}

	public boolean morePoints() 
	{
		return (seg != num);
	}



	public void nextPoints(ScreenPoint p1, ScreenPoint p2) 
	{
		ScreenPoint p = new ScreenPoint(x, y);

		p1.x = p.x;
		p1.y = p.y;

		p = new ScreenPoint(x + dx1, y + dy1);

		p2.x = p.x;
		p2.y = p.y;

		x   += dx2;
		y   += dy2;

		seg++;
	}
}

