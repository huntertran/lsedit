package lsedit;


public class RealPoint extends Object {

	protected double m_x, m_y;

	public RealPoint() 
	{
	}

	public RealPoint(double x, double y) 
	{
		m_x = x;
		m_y = y;
	}

	public RealPoint(int x, int y) 
	{
		m_x = (double) x;
		m_y = (double) y;
	}

	public Object clone() 
	{
		return new RealPoint(m_x, m_y);
	}

	public double getX()
	{
		return(m_x);
	}

	public double getY()
	{
		return(m_y);
	}

	public void setX(double x)
	{	
		m_x = x;
	}

	public void setY(double y)
	{	
		m_y = y;
	}

	public void setLocation(double x, double y) 
	{
		m_x = x;
		m_y = y;
	}

	public String toString() 
	{
		return "RealPoint(" + m_x + ", " + m_y + ")";
	}
}

