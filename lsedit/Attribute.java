package lsedit;

import java.util.BitSet;
import java.awt.Color;
import java.io.PrintWriter;

/* Attribute.java
 *
 * Each attribute declaration is represented as an instance of this
 * class on an EntityClass or RelationClass attribute list.
 *
 * Each attribute instance is represented as an instance of this class
 * on a EntityInstance or RelationInstance attribute list.
 *
 */

public class Attribute extends Object 
{
	public static final Integer NULL			= new Integer(0);
	public static final Integer INT				= new Integer(1);
	public static final Integer DOUBLE			= new Integer(2);
	public static final Integer STRING			= new Integer(3);
	public static final Integer INT_LIST		= new Integer(4);
	public static final Integer DOUBLE_LIST		= new Integer(5);
	public static final Integer STRING_LIST		= new Integer(6);

	public static final int NULL_TYPE			= 0;
	public static final int INT_TYPE			= 1;
	public static final int DOUBLE_TYPE			= 2;
	public static final int STRING_TYPE			= 3;
	public static final int INT_LIST_TYPE		= 4;
	public static final int DOUBLE_LIST_TYPE	= 5;
	public static final int STRING_LIST_TYPE	= 6;

	// Extra types

	public static final	int COLOR_TYPE			= 7;
	public static final int COLOR_OR_NULL_TYPE  = 8;
	public static final int POINT_TYPE          = 9;
	public static final int ELISION_TYPE        = 10;
	public static final int ATTR_TYPE			= 11;	// Generic AVI value
	public static final int TEXT_TYPE			= 12;	// Multiline input string
	public static final int ENTITY_STYLE_TYPE	= 13;	// Entity class style
	public static final int REL_STYLE_TYPE		= 14;	// Relational style
	public static final int ENTITY_CLASS_TYPE   = 15;	// The class of this entity
	public static final int RELATION_CLASS_TYPE = 16;	// The class of this relation
	public static final int ENTITY_IMAGE_TYPE	= 17;	// Entity class type

	public static final String indent = " ";			// one space indent 

//	protected static int	m_totalAttributes   = 0;

	/* Nothing may change either of the following values once set
	 * because attributes may be shared across entities
	 */

	public		String				m_id;
	private		String				m_avi;				// May be object or external string

/*
	protected void finalize() throws Throwable
	{
		--m_totalAttributes;
	}
*/

	public Attribute(String id, String avi) 
	{
//		StringCache.isCached(id);
		m_id       = id;
		m_avi      = avi;
//		++m_totalAttributes;
	}

/*
	public static int totalAttributes()
	{
		return m_totalAttributes;
	}
*/

	public boolean hasId(String id) 
	{
		return m_id.equals(id);
	}

	public String externalString()
	{
		return m_avi;
	}

	public static int countValues(String value)
	{
		if (value == null) {
			return 0;
		}
		if (value.charAt(0) != '(') {
			return 1;
		}
		return (AttributeValue.countValues(value));
	}	

	public int countValues()
	{
		return countValues(m_avi);
	}	

	// Parse methods. Convert attribute value into a particular form.

	public static String parseStringValue(String value) 
	{
		if (value != null && value.length() != 0) {
			switch (value.charAt(0)) {
			case '"':
			case '\'':
			case '(':
				return AttributeValue.parseString(value);
		}	}
		/* N.B. Will be quoted if it requires translation from external to internal 
		 *      eg. if it uses \ within itself
		 */
		return value;
	}

	public String parseString() 
	{
		return parseStringValue(m_avi);
	}

	public static int parseIntValue(String value)
	{
		if (value == null) {
			return 0;
		}
		// Anything that is a single int will never be quoted
		return Util.parseInt(value);
	}

	public int parseInt() 
	{
		return parseIntValue(m_avi);
	}

	public static int parseIntValue(String value, int defaultValue)
	{
		if (value == null) {
			return defaultValue;
		}
		// Anything that is a single int will never be quoted
		return Util.parseInt(value);
	}

	public int parseInt(int defaultValue)
	{
		return parseIntValue(m_avi, defaultValue);
	}

	public static double parseDoubleValue(String value)
	{
		if (value == null) {
			return 0.0;
		}
		// Anything that is a single double will never be quoted
		return Util.parseDouble(value);
	}

	public double parseDouble()
	{
		return parseDoubleValue(m_avi);
	}

	public static double parseDoubleValue(String value, double defaultValue)
	{
		if (value == null) {
			return defaultValue;
		}
		// Anything that is a single double will never be quoted
		return Util.parseDouble(value);
	}

	public double parseDouble(double defaultValue)
	{
		return parseDoubleValue(m_avi, defaultValue);
	}

	public static Color parseColorValue(String value, LandscapeObject forObject, String forMember) 
	{
		if (value == null) {
			MsgOut.println("parseColor: missing rgb values for " + forObject + " " + forMember);
			return ColorCache.get(0, 0, 0, 255);
		}	
		return AttributeValue.parseColor(value, forObject, forMember);
	}

	public Color parseColor(LandscapeObject forObject, String forMember) 
	{
		return parseColorValue(m_avi, forObject, forMember);
	}

	//
	// Parse an attribute record to set the appropriate edge point factors
	// supplied in an attribute record.

	public static void parseElisionsValue(String value, Ta ta, int type, BitSet bitset) 
	{
		if (value != null) {
			AttributeValue.parseElisions(value, ta, type, bitset);
		}
	}

	public void parseElisions(Ta ta, int type, BitSet bitset) 
	{
		parseElisionsValue(m_avi, ta, type, bitset);
	}

	public void followLink(LandscapeEditorCore ls, EntityInstance e, boolean mustbeContainer) 
	{
		String	avi    = m_avi;

		if (avi == null) {
		   // This seems to be the case when going up
		   /* [irbull] if avi is null, just use the navigate to? */
			ls.navigateTo(e, mustbeContainer);
			return;
		}

		AttributeValue.followLink(avi, ls, e, mustbeContainer);
	}

	// Output this attribute entry 

	public String writeAttribute(PrintWriter ps, String nodeId, LandscapeObject parentClass, boolean classType) 
	{
		String avi = m_avi;

		// Classes and Instances which have overwritten class default

		if (avi == null) {
			// Null value possible in class definitions 
			// Acts as a declarer of a attribute variable 
			// Output just the id 

			if (!classType) {
				return nodeId;
			}
		} else {
			// General case. Output ID and value.  
			// Value could be single item, list, or nested. 

			if (parentClass != null && parentClass.defaultValue(m_id, avi)) {
				return nodeId;
		}	}
		return LandscapeObject.writeAttribute(ps, nodeId, m_id, avi);
	}

	public String toString() 
	{
		String					ret;

		ret = m_id;
		if (m_avi != null) {
			ret += "=" + m_avi;
		}
		return(ret);
	}
}

