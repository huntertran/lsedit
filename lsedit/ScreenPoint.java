package lsedit;

public class ScreenPoint extends Object {

	public int x, y; 

	protected int round(double v) 
	{
		return (int) (v + 0.5);
	}

	public ScreenPoint() 
	{ 
	}

	public ScreenPoint(double x, double y) 
	{
		set(x, y);
	}



	public ScreenPoint(RealPoint pt) 
	{
		set(pt.getX(), pt.getY());
	}



	public void set(double x, double y) 
	{
		this.x = round(x);
		this.y = round(y);
	} 
}

