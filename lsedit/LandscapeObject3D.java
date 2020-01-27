package lsedit;

import java.awt.Color;
import java.awt.Graphics;
import java.util.*;
import java.io.*; 
import javax.swing.*;

// 3 dimension objects have foreground colors, labels etc.
// This is everything but line objects such as edges

public abstract class LandscapeObject3D extends LandscapeObject {

	private String	m_id;			// The id of this entity or class (the name that identifies it uniquely in TA)
	private String	m_description;	// The description of this entity or class
	private String	m_label;		// The label for this entity or class

	/* Logical color's need to be kept separate from the JComponent colors
	 * otherwise when we paint a component we set its actual color to its
	 * logical color loosing the fact that it may not have had a logical color
	 * to begin with (ie. null->red if the class was red)
	 */

	private Color	m_labelColor  = null;
	private	Color	m_colorWhenOpen = null;

	// --------------
	// Object methods
	// --------------

	public String toString() 
	{
		String label = getLabel();

		if (label == null) {
			label = m_id;
		}
		return label;
	}

	// --------------
	// Public methods 
	// --------------

	public LandscapeObject3D() 
	{
		super();
	}

	public JComponent getSwingObject()
	{
		return null;
	}


	public String getId() 
	{
		return m_id;
	}
	
	public void setId(String id) 
	{
		m_id = id;
	}

	public boolean hasId(String id) 
	{
		return m_id.equals(id);
	}

	public String getDescription() 
	{
		return m_description;
	}

	public void setDescription(String description) 
	{
		if (description != null && description.length() == 0) {
			description = null;
		}
		m_description = description;
	}

	protected String getLabel()
	{
		return(m_label);
	}

	protected void setLabel(String label)
	{
		JComponent swingObject = getSwingObject();

		m_label = label;
		if (swingObject != null) {
			swingObject.setToolTipText(label);
	}	}

	public Color getLabelColor()
	{
		return m_labelColor;
	}


	public Color getInheritedLabelColor() 
	{
		Color	ret;

		ret = getLabelColor();
		if (ret == null) {
			LandscapeClassObject	superclass;

			for (int i = 0; (superclass = derivedFrom(i)) != null; ++i) {
				ret = superclass.getInheritedLabelColor();
				if (ret != null) {
					break;
		}	}	}
		return ret;
	}

	public boolean setLabelColor(Color color) 
	{
		m_labelColor = color;
		return true;
	}

	public Color getSuperColorWhenOpen() 
	{
		Color					ret = null;
		LandscapeClassObject	superclass;

		for (int i = 0; (superclass = derivedFrom(i)) != null; ++i) {
			ret = superclass.getInheritedColorWhenOpen();
			if (ret != null) {
				break;
		}	}
		return ret;
	}

	public Color getColorWhenOpen()
	{
		return m_colorWhenOpen;
	}

	public Color getInheritedColorWhenOpen() 
	{
		Color	ret;

		ret = getColorWhenOpen();
		if (ret == null) {
			ret = getSuperColorWhenOpen();
		}
		return ret;
	}

	public void setColorWhenOpen(Color color) 
	{
		m_colorWhenOpen = color;
	}

	public boolean canEditAttribute(int index)
	{
		if (index == 0) {
			// This is the id of the object
			return(false);
		}
		return(super.canEditAttribute(index));
	}
}

