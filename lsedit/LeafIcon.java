package lsedit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

class LeafIcon implements Icon, PaintShapeHelper {

	private EntityClass	m_ec;
	private Icon		m_scaledIcon = null;

	public LeafIcon(EntityClass ec)
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

		EntityComponent.paintShape(g, this, ec, x, y, 18, 18, false /* Not draw root */, false /* open */, Color.black, m_ec.getInheritedObjectColor());
	}
    
    public int getIconWidth()
	{
		return 18;
	}

    public int getIconHeight()
	{
		return 18;
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



