package lsedit;

import javax.swing.Icon;

/* This interface allows anything that call EntityComponent.paintShape() to provide input into the
 * configurable issues relating to the painting of shapes.  
 */

public interface PaintShapeHelper
{
	abstract public String		getEntityLabel();
	abstract public Icon		getUnscaledIcon();
	abstract public Icon		getScaledIcon();
	abstract public void		setScaledIcon(Icon icon);
}
