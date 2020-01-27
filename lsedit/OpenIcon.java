package lsedit;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Color;

import javax.swing.Icon;

class OpenIcon implements Icon, PaintShapeHelper {

	public static final int	g_height = 18;
	public static final int g_width  = 18;

	private EntityClass	m_ec;
	private Icon		m_scaledIcon = null;

	public OpenIcon(EntityClass ec)
	{
		m_ec = ec;
	}
	
	public void clear()
	{
		m_scaledIcon = null;
	}

    public void paintIcon(Component c, Graphics g, int x, int y)
	{
		EntityClass ec = m_ec;

		EntityComponent.paintShape(g, this, ec, x, y, g_width, g_height, false /* Not draw root */, true /* open */, Color.black, m_ec.getInheritedObjectColor());
	}
    
    public int getIconWidth()
	{
		return g_width;
	}

    public int getIconHeight()
	{
		return g_height;
	}

	// PaintShapeHelper interface

	public String getEntityLabel()
	{
		return null;
	}

	public Icon getUnscaledIcon()
	{
		return m_ec.getUnscaledIcon();
	}

	public Icon getScaledIcon()
	{
		return m_scaledIcon;
	}

	public void setScaledIcon(Icon icon)
	{
		m_scaledIcon = icon;
	}
}



