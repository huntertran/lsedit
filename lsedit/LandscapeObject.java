package lsedit;

import java.awt.Color;
import java.awt.Graphics;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JComponent;

// Don't put any things in this class that can be avoided -- thousands of relations

public abstract class LandscapeObject {

	public  final static String COLOR_ID       =	 "color";
	public  final static String LABEL_COLOR_ID =	 "labelcolor";
	public  final static String OPEN_COLOR_ID  =	 "opencolor";
	public  final static String STYLE_ID       =     "style";
	public  final static String	ARROWCOLOR_ID  =     "arrowcolor";


	private final static int    MAX_RGB         = 255; 

	public static LandscapeObject	g_infoShown = null;	// Last identified thing mouse moved over

	/* All things that are not LandscapeClassObjects must have a parentClass
	 * Otherwise we can't getDiagram() for object in diagram
	 */

	// The class this entity belongs to (if entity or relation)
	// Probably null if a LandscapeClassObject

	private LandscapeClassObject	m_parentClass;				

	/* Logical color's need to be kept separate from the JComponent colors
	 * otherwise when we paint a component we set its actual color to its
	 * logical color loosing the fact that it may not have had a logical color
	 * to begin with (ie. null->red if the class was red)
	 */

	private Color					m_objectColor  = null;		// 

	// Attribute database

	public Attribute[]				m_attributes   = null;

	protected static final String specialChars = "\n\t\f\r\u001b\u007f\\\" /,:(){}^<>=";	// u001b=ESC u007f=DEL

	public static String qt(String str) 
	{
		if (str == null || str.length() == 0) {
			return "\"\"";
		}
		for (int i = 0; i < specialChars.length(); i++) {
			if (str.indexOf(specialChars.charAt(i)) >= 0) {
				if (i < 8) {
					String	ret = "\"";
					char	c;

					c = 0;
					for (i = 0; i < str.length(); ++i) {
						c    = str.charAt(i);
						switch (c) {
						case '\n':
						case '\t':
						case '\f':
						case '\r':
						case '\u001b':
						case '\u007f':
						case '\\':
						case '"':
							ret += '\\';
							switch (c) {
							case '\n':
								c = 'n';
								break;
							case '\t':
								c = 't';
								break;
							case '\f':
								c = 'f';
								break;
							case '\r':
								c = 'r';
								break;
							case '\u001b':
								c = 'e';
								break;
							case '\u007f':
								c = 'd';
								break;
							}
							break;
						}
						ret += c;
					}
					return ret + '"';
				}
				return "\"" + str + "\"";
			}

		}
		return str;
	}

	// --------------
	// Public methods 
	// --------------

	public LandscapeObject() 
	{
	}
	
	public LandscapeClassObject getParentClass()
	{
		return m_parentClass;
	}

	public LandscapeClassObject derivedFrom(int i)
	{
		return(i == 0 ? m_parentClass : null);
	}

	public void decrementClassMembers()
	{
		LandscapeClassObject pc;

		pc = m_parentClass;
		if (pc != null) {
			pc.decrementMembers();
	}	}

	public void incrementClassMembers()
	{
		LandscapeClassObject pc;

		pc = m_parentClass;
		if (pc != null) {
			pc.incrementMembers();
	}	}

	public void setParentClass(LandscapeClassObject parentClass)
	{
		decrementClassMembers();
		m_parentClass = parentClass;
		incrementClassMembers();
	}	

	// Entities and Relations obtain the diagram from their class..
	// This avoids a reference to the diagram on every edge/entity at small cost

	public Ta getTa()
	{
		return(m_parentClass.getTa());
	}

	public Diagram getDiagram()
	{
		return(m_parentClass.getDiagram());
	}

	public String getId()
	{
		return null;
	}
	
	public int getStyle()
	{
		return -1;
	}

	public void setStyle(int value)
	{
		System.out.println("Can't set style on " + this);
	}

	/* This is broken re multiple inheritence rules IJD */

	public int getInheritedStyle() 
	{
		int	ret, i;

		ret = getStyle();
		if (ret < 0) {
			LandscapeClassObject	superclass;

			for (i = 0; (superclass = derivedFrom(i)) != null; ++i) {
				ret = superclass.getInheritedStyle();
				if (ret >= 0) {
					return ret;
			}	}
			return 0;
		}
		return ret;
	}

	public String getStyleName(int style)
	{
		return "";
	}


	// Relations and RelationClasses can't be opened

	public Color getSuperColorWhenOpen()
	{
		return null;
	}
	
	public Color getColorWhenOpen()
	{
		return null;
	}

	public Color getInheritedColorWhenOpen() 
	{
		return null;
	}

	public void setColorWhenOpen(Color color) 
	{
		System.out.println("Can't setColorWhenOpen(" + color + ") on " + this);
	}

	public Color getSuperObjectColor() 
	{
		Color					ret = null;
		LandscapeClassObject	superclass;
		int						i;

		for (i = 0; (superclass = derivedFrom(i)) != null; ++i) {
			ret = superclass.getInheritedObjectColor();
			if (ret != null) {
				break;
		}	}
		return ret;
	}

	public Color getObjectColor()
	{
		return m_objectColor;
	}

	public void setObjectColor(Color color) 
	{
		m_objectColor = color;
	}

	public Color getInheritedObjectColor() 
	{
		Color	ret;

		ret = getObjectColor();
		if (ret == null) {
			ret = getSuperObjectColor();
		}
		return ret;
	}

	// Relations don't have labels

	public Color getSuperLabelColor()
	{
		Color					ret = null;
		LandscapeClassObject	superclass;

		
		for (int i = 0; (superclass = derivedFrom(i)) != null; ++i) {
			ret = superclass.getInheritedLabelColor();
			if (ret != null) {
				break;
		}	}
		return ret;
	}


	public Color getLabelColor()
	{
		Attribute attribute = getLsAttribute(LABEL_COLOR_ID);
		if (attribute != null) {
			return attribute.parseColor(this, LABEL_COLOR_ID);
		}
		return null;
	}

	public Color getInheritedLabelColor() 
	{
		Color	ret;

		ret = getLabelColor();
		if (ret == null) {
			ret = getSuperLabelColor();
		}
		return ret;
	}

	public boolean setLabelColor(Color color) 
	{
		// Relations do not treat label color as a first order value
		return false;
	}

	public Color getSuperArrowColor() 
	{
		Color					ret = null;
		LandscapeClassObject	superclass;

		for (int i = 0; (superclass = derivedFrom(i)) != null; ++i) {
			ret = superclass.getInheritedArrowColor();
			if (ret != null) {
				break;
		}	}
		return ret;
	}

	public Color getArrowColor()
	{
		Attribute attribute = getLsAttribute(ARROWCOLOR_ID);
		if (attribute != null) {
			return attribute.parseColor(this, ARROWCOLOR_ID);
		}
		return null;
	}

	public Color getInheritedArrowColor() 
	{
		Color	ret = getArrowColor();

		if (ret == null) {
			ret = getSuperArrowColor();
		}
		return ret;
	}

	public Color getSuperColor(String id)
	{
		if (id.equals(COLOR_ID)) {
			return getSuperObjectColor();
		}

		if (id.equals(LABEL_COLOR_ID)) {
			return getSuperLabelColor();
		}

		if (id.equals(OPEN_COLOR_ID)) {
			return getSuperColorWhenOpen();
		}
		if (id.equals(ARROWCOLOR_ID)) {
			return getSuperArrowColor();
		}
		return null;
	}

	// Return the attribute with the associated id
	// Avoid name collision potential with Swing
	// Allow attributes to be null

	public int getAttributesLength()
	{
		Attribute[] attributes = m_attributes;

		if (attributes == null) {
			return 0;
		}
		return attributes.length;
	}

	public int getLsAttributesSize()
	{
		int			i, lth, ret;
		Attribute[]	attributes;
		Attribute	attribute;

		ret = 0;
		attributes = m_attributes;
		if (attributes != null) {
			lth = attributes.length;
			for (i = 0; i < lth; ++i) {
				attribute = attributes[i];
				if (attribute == null) {
					continue;
				}
				++ret;
		}	}
		return ret;
	}

	public Attribute getLsAttribute(String id) 
	{
		int			i, lth;
		Attribute[]	attributes;
		Attribute	attribute;

		attributes = m_attributes;
		if (attributes != null) {
			lth = attributes.length;
			for (i = 0; i < lth; ++i) {
				attribute = attributes[i];
				if (attribute == null) {
					continue;
				}
				if (id.equals(attribute.m_id)) {
					return attribute;
		}	}	}
		return null;
	}

	public Attribute getLsAttributeAt(int index)
	{
		int			i, lth;
		Attribute[]	attributes;
		Attribute	attribute;

		attributes = m_attributes;
		if (attributes != null) {
			lth = attributes.length;
			for (i = 0; i < lth; ++i) {
				attribute = attributes[i];
				if (attribute == null) {
					continue;
				}
				if (index == 0) {
					return attribute;
				}
				--index;
		}	}
		return null;
	}

	public int countAttributes()
	{
		Attribute[]	attributes = m_attributes;
		int			ret        = 0;

		if (attributes != null) {
			int length = attributes.length;
			int	i;

			for (i = 0; i < length; ++i) {
				if (attributes[i] != null) {
					++ret;
		}	}	}
		return(ret);
	}

	public void maxAttributes(int max)
	{
		Attribute[] oldAttributes = m_attributes;
		Attribute[]	attributes    = new Attribute[max];
		int			i, length;

		if (oldAttributes != null) {
			length = oldAttributes.length;
			
			for (i = 0; i < length; ++i) {
				attributes[i] = oldAttributes[i];
			}
			oldAttributes = null;
		}
		m_attributes  = attributes;
	}

	/* Never change either the id or value of an attribute */

	public void putAttribute(String id, String value) 
	{
		Attribute[]	attributes;
		Attribute	attribute;
		int			i, free, length;
		String		oldValue;

		attributes = m_attributes;
		if (attributes == null) {
			m_attributes = attributes = new Attribute[1];
		}
		length = free = attributes.length;

		for (i = 0; i < length; ++i) {
			attribute = attributes[i];
			if (attribute == null) {
				if (free > i) {
					free = i;
				}
				continue;
			}
			if (id.equals(attribute.m_id)) {
				oldValue = attribute.externalString();
				if (value == null) {
					if (oldValue == null) {
						return;
					}
				} else {
					if (value.equals(oldValue)) {
						return;
				}	}
				free = i;
				break;
		}	}
		if (free == length) {
			maxAttributes(free+1);
			attributes = m_attributes;
		}

		// We must always create new Attributes when values changed
		// since attributes may be cached causing multiple logical
		// attributes to be represented by the same physical attribute

		attributes[free] = AttributeCache.get(id, value);
	}

	/* N.B. Overloaded in subclasses */

	public boolean processFirstOrder(String id, String value) 
	{
		if (id.equals(COLOR_ID)) {
			setObjectColor(Attribute.parseColorValue(value, this, COLOR_ID));
			return true;
		}

		if (id.equals(LABEL_COLOR_ID)) {
			return setLabelColor(Attribute.parseColorValue(value, this, LABEL_COLOR_ID));
		}

		if (id.equals(OPEN_COLOR_ID)) {
			setColorWhenOpen(Attribute.parseColorValue(value, this, OPEN_COLOR_ID));
			return true;
		}
		return false;
	}

	public void addAttribute(String id, String value) {

		if (processFirstOrder(id, value)) { 
			return;
		}
		putAttribute(id, value);
	}

	public void addAttribute(String id, int ivalue)
	{
		String	value = StringCache.get(String.valueOf(ivalue));

		addAttribute(id, value);
	}

	public void addAttribute(String id, boolean bvalue)
	{
		String	value = (bvalue ? "true" : "false");

		addAttribute(id, value);
	}

	public void addAttribute(String id, String value, Vector v) {

		if (processFirstOrder(id, value)) { 
			return;
		}
		v.add(AttributeCache.get(id, value));
	}

	/* This logic exists to avoid constantly extending an array by one */

	public void addAttributes(Vector newAttributes)
	{
		int			size = newAttributes.size();
	
		if (size != 0) {
			Attribute	oldAttribute, newAttribute;
			int			left, i, j, max, length, free;
			Attribute[]	attributes;
			String		id;

			max = countAttributes() + size;
			maxAttributes(max);
			attributes = m_attributes;
			length     = attributes.length;
			for (i = 0; i < size; ++i) {
				free         = length;
				newAttribute = (Attribute) newAttributes.elementAt(i);
				if (newAttribute == null) {
					continue;
				}
				id = newAttribute.m_id;
				for (j = 0; j < length; ++j) {
					oldAttribute = attributes[j];
					if (oldAttribute == null) {
						if (free > j) {
							free = j;
						}
						continue;
					}
					if (id.equals(oldAttribute.m_id)) {
						free = j;
						break;
				}	}
		
				// We must always create new Attributes when values changed
				// since attributes may be cached causing multiple logical
				// attributes to be represented by the same physical attribute

				attributes[free] = newAttribute;
		}	}
	}

	public String writeColorAttributes(PrintWriter ps, String nodeId, LandscapeObject parentClass) 
	{
		Color		color, color1;

		// Output the body of the attribute record 
		// The child method outputs the header and tail 

		color = getObjectColor();
		if (color != null) {
			if (parentClass != null) {
				color1 = parentClass.getInheritedObjectColor();
			} else {
				color1 = null;
			}
			if (!color.equals(color1)) {
				nodeId = writeAttribute(ps, nodeId, COLOR_ID, Util.taColor(color));
		}	}

		color = getLabelColor();
		if (color != null) { 
			if (parentClass != null) {
				color1 = parentClass.getInheritedLabelColor();
			} else {
				color1 = null;
			}
			if (!color.equals(color1)) {
				nodeId = writeAttribute(ps, nodeId, LABEL_COLOR_ID, Util.taColor(color));
		}	}

		color = getColorWhenOpen();
		if (color != null) { 
			if (parentClass != null) {
				color1 = parentClass.getInheritedColorWhenOpen();
			} else {
				color1 = null;
			}
			if (!color.equals(color1)) {
				nodeId = writeAttribute(ps, nodeId, OPEN_COLOR_ID, Util.taColor(color));
		}	}
		return nodeId;
	}
	
	public static String writeAttribute(PrintWriter ps, String nodeId, String label, String value)
	{
		if (nodeId != null) {
			ps.println(nodeId + "{");
			nodeId = null;
		}
		if (value == null) {
			ps.println(Attribute.indent + label);
		} else {
			ps.println(Attribute.indent + label + " = " + value);
		}
		return nodeId;
	}
	
	public static String writeAttribute(PrintWriter ps, String nodeId, String label, short value)
	{
		return writeAttribute(ps, nodeId, label, "" + value);
	}
	
	public static String writeAttribute(PrintWriter ps, String nodeId, String label, int value)
	{
		return writeAttribute(ps, nodeId, label, "" + value);
	}
	
	public static String writeAttribute(PrintWriter ps, String nodeId, String label, double value)
	{
		return writeAttribute(ps, nodeId, label, "" + value);
	}
			
	public String writeAttributes(PrintWriter ps, String nodeId, LandscapeObject parentClass, boolean classType) 
	{
		Attribute[]	attributes;
		Attribute	attribute;
		int			i, length;

		// Output the body of the attribute record 
		// The child method outputs the header and tail 

		nodeId = writeColorAttributes(ps, nodeId, parentClass);

		attributes = m_attributes;
		if (attributes != null) {
			length = attributes.length;
			for (i = 0; i < length; ++i) {
				attribute = attributes[i];
				if (attribute == null) {
					continue;
				}
				nodeId = attribute.writeAttribute(ps, nodeId, parentClass, classType);
		}	}
		return nodeId;
	}

	// The routines that follow hide the complexity of getting/setting attribute values
	// from EditAttributes

	public int getPrimaryAttributeCount()
	{
		return(0);
	}

	public int getLsAttributeCount()
	{
		return(getPrimaryAttributeCount() + getLsAttributesSize() + 1);
	}

	public boolean canEditName(int index)
	{
		int	primary = getPrimaryAttributeCount();

		if (index < primary) {
			return(false);
		}
		return(true);
	}

	// Use overloading to set special rules

	public boolean canEditAttribute(int index)
	{
		int	primary;

		primary =  getPrimaryAttributeCount();
		if (index < primary) {
			return(true);
		}
		index -= primary;
		if (index < getLsAttributesSize()) {
			return(true);
		}
		return(false);
	}

	public String getLsAttributeNameAt(int index)
	{
		Attribute	attr;

		index -= getPrimaryAttributeCount();
		if (index == getLsAttributesSize()) {
			// Dummy row to allow insertion
			return("");
		}
		attr   = getLsAttributeAt(index);
		if (attr == null) {
			return(null);
		}
		return(attr.m_id);
	}

	public Object getLsAttributeValueAt(int index)
	{
		Attribute	attr;
		String		ret;

		index -= getPrimaryAttributeCount();
		if (index == getLsAttributesSize()) {
			// Dummy row to allow insertion
			return("");
		}
		attr   = getLsAttributeAt(index);
		if (attr == null) {
			return(null);
		}
		ret = attr.parseString();
		if (ret == null) {
			ret = "";
		}
		return ret;
	}

	public boolean unknownAttributeName(String name)
	{
		int			i, primary;
		String		name1;

		for (i = 0; ; ++i) {
			name1 = getLsAttributeNameAt(i);
			if (name1 == null) {
				break;
			}
			if (name.equals(name1)) {
				System.out.println("Can't rename generic attribute to '" + name + "': attribute already exists");
				return(false);
		}	} 
		return(true);
	}

	// Can be used to delete an attribute by setting newName null

	public void setAttributeName(String oldName, String newName)
	{
		Attribute[]	attributes;
		String		value = null;
		Attribute	attribute;
		int			i, length;

		attributes = m_attributes;
		if (attributes != null && oldName != null) {
			length = attributes.length;
			for (i = 0; i < length; ++i) {
				attribute = attributes[i];
				if (attribute == null) {
					continue;
				}
				if (oldName.equals(attribute.m_id)) {
					// Delete this attribute
					value = attribute.externalString();
					attributes[i] = null;
					break;
		}	}	}
		
		if (newName != null) {
			addAttribute(newName, value);
		}
	}

	// Need to know the type in cases where value might be null
	// For example with some colors

	public int getLsAttributeTypeAt(int index)
	{
		if (index < getLsAttributeCount() - 1) {
			return(Attribute.ATTR_TYPE);
		}
		// This attribute does not yet exist
		return(Attribute.NULL_TYPE);
	}

	public int getLsAttributeOffset(String id)
	{
		String	name;
		int		i;

		for (i = 0; ; ++i) {
			name = getLsAttributeNameAt(i);
			if (name == null) {
				return(-1);
			}
			if (name.equals(id)) {
				return(i);
	}	}	}

	public boolean defaultValue(String id, Object object)
	{
		if (object != null) {
			int		i;
			Object	object1;

			i = getLsAttributeOffset(id);
			if (i >= 0) {
				object1 = getLsAttributeValueAt(i);
				if (object.equals(object1)) {
					return(true);
		}	}	}
		return(false);
	}
}

