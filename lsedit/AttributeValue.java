package lsedit;

import java.util.BitSet;
import java.awt.Color;

/* We now use the strategy that the attribute value is preserved as a single string in its external form.
 * This is preferable to internally representing the value as structs because it uses less memory and is
 * more willing to accept whatever the user specifies as the value of an attribute.  This class is
 * responsible for performing the on the fly translation from the external attribute value representation
 * to its internal information content.
 */

/* This is made static for efficiency but it is relatively simple to make the object dynamic if this is
 * warranted.  This class is kept separate from Attribute to make that transition straight forward.
 */

public class AttributeValue extends Object {

	private static final int	CT_QUOTE    = 1;

	protected static char[]	m_input;

	/**
	 * The String token value.
	 */

	protected static int	m_start;
	protected static String	m_sval;

	/* Load the string into the parse buffer */

	protected static void load(String avi) 
	{
		int		length  = avi.length();
		int		i, pos;
		char	c;


//		System.out.println("AVI: " + avi);

		if (m_input == null || m_input.length < length+1) {
			m_input = new char[length+1];
		}
		for (i = pos = 0; i < length; ++i) {
			switch (c = avi.charAt(i)) {
			case '"':
			case '\'':
				m_input[pos++] = CT_QUOTE;
				continue;
			case '\\':
				if (++i < length) {
					switch (c = avi.charAt(i)) {
					case 'n':
						c = '\n';
						break;
					case 't':
						c = '\t';
						break;
					case 'f':
						c = '\f';
						break;
					case 'r':
						c = '\r';
						break;
					case 'e':
						c = 27;
						break;
					case 'd':
						c = 127;
				}	}
			default:
				m_input[pos++] = c;
				continue;
		}	}
		m_start        = 0;
		m_input[pos]   = 0;
	}

	/**
	 * Parses the next token from the input string 
	 * N.B. We know that the string conforms to the syntax of an AVI
	 */

	protected static final int nextToken()
	{
		int		pos = m_start;
		int		c;

		for (;;) {
			// This loop is repeated whitespace
			c = m_input[pos];
			switch (c) {
			case ' ':
				++pos;
				continue;
			case 0:
				m_start = pos;
				return 0;
			case '(':
			case ')':
				m_start = ++pos; 
//				System.out.println("Token :" + (char) c);
				return c;
			case CT_QUOTE:
				for (m_start = ++pos; (c = m_input[pos]) != CT_QUOTE && c != 0; ++pos);
				m_sval = StringCache.get(m_input, m_start, pos-m_start);
				m_start = ++pos;
//				System.out.println("Quoted: " + m_sval);
				return CT_QUOTE;
			default:
				m_start = pos;
				for (; ; ++pos) {
					switch (c = m_input[pos]) {
					case ' ':
					case ')':
					case '(':
					case 0:
						break;
					default:
						continue;
					}
					m_sval  = StringCache.get(m_input, m_start, pos-m_start);
					m_start = pos;
//					System.out.println("Plain: " + m_sval);
					return CT_QUOTE;
	}	}	}	}

	/* Count the number of values in the attribute value */

	public static int countValues(String avi)
	{
		int	values = 0;
		int	depth  = 0;

		load(avi);
		for (;;) {
			switch (nextToken()) {
			case CT_QUOTE:
				++values;
				continue;
			case '(':
				if (++depth > 1) {
					values = -1;
					break;
				}
				continue;
			case ')':
				if (--depth < 0) {
					values = -1;
					break;
				}
				continue;
			case 0:
				if (depth != 0) {
					values = -1;
				}
			}
			break;
		}	
		return values;
	}

	public static String parseString(String avi)
	{
		load(avi);
		if (nextToken() == CT_QUOTE) {
			return m_sval;
		}
		return null;
	}

	private static int parseColorValue(String value, LandscapeObject forObject, String forMember, int forComponent)
	{
		int		i;
		char	sign = value.charAt(0);

		if (value.indexOf('.') >= 0) {
			if (sign == '*') {
				value = value.substring(1);
			}
			double	d = Double.parseDouble(value);

			i = (int) (d * 255 + 0.5);

			if (sign == '*' && forObject != null) {
				Color	baseColor = forObject.getSuperColor(forMember);
				switch(forComponent) {
				case 1:
					i = baseColor.getRed();
					break;
				case 2:
					i = baseColor.getGreen();
					break;
				case 3:
					i = baseColor.getBlue();
					break;
				case 4:
					i = baseColor.getAlpha();
					break;
				}
				i = (int) (d * ((double) i));
			}
		} else {
			if (sign == '+') {
				value = value.substring(1);
			}
			i = Integer.parseInt(value);
			switch(sign) {
			case '+':
			case '-':
				if (forObject != null) {
					Color	baseColor = forObject.getSuperColor(forMember);
					if (baseColor != null) {
						switch(forComponent) {
						case 1:
							i += baseColor.getRed();
							break;
						case 2:
							i += baseColor.getGreen();
							break;
						case 3:
							i += baseColor.getBlue();
							break;
						case 4:
							i += baseColor.getAlpha();
							break;
				}	}	}
				if (i < 0) {
					i = 0;
				} else if (i > 255) {
					i = 255;
		}	}	}

		i %= 256;
		return(i);
	}

	private static String[] colorNames = {
		"black", 
		"blue", 
		"cyan", 
		"gray", 
        "green",
		"magenta",
		"orange",
		"pink",
		"red",
		"white",	
		"yellow"
	};
	
	private static Color[] colorNameValues = {
		Color.black, 
		Color.blue, 
		Color.cyan, 
		Color.gray, 
        Color.green,
		Color.magenta,
		Color.orange,
		Color.pink,
		Color.red,
		Color.white,	
		Color.yellow
	};


	public static Color parseColor(String avi, LandscapeObject forObject, String forMember) 
	{
		int		r      = 0;
		int		g      = 0;
		int		b      = 0;
		int		a      = 255;

		// Value is chain of three normalized reals or four including final alpha

		int					values = 0;
		int					depth  = 0;
		int					val;

		load(avi);
		for (;;) {
			switch (nextToken()) {
			case CT_QUOTE:
				if (depth == 0 && values == 0) {
					String	sval = m_sval;
					int		lth  = sval.length();
					String	name;
					int		i, lth1;
					
					for (i = colorNames.length; 0 <= --i; ) {
						name = colorNames[i];
						lth1 = name.length();
						if (lth1 <= lth) {
							int	extra = lth - lth1;
							if (name.equalsIgnoreCase(sval.substring(extra))) {
								Color color = colorNameValues[i];
								if (4 <= extra) {  
									if (sval.substring(0,4).equalsIgnoreCase("dark")) {
										color = color.darker();
									} else if (5 <= extra && sval.substring(0,5).equalsIgnoreCase("light")) {
										color = color.brighter();
								}	}
								r      = color.getRed();
								g      = color.getGreen();
								b      = color.getBlue();
								values = 4;
								break;
					}	}	}
					if (i >= 0) {
						continue;
				}	}
				val = parseColorValue(m_sval, forObject, forMember, ++values); 
				switch (values) {
				case 1:
					r = val;
					continue;
				case 2:
					g = val;
					continue;
				case 3:
					b = val;
					continue;
				case 4:
					a = val;
					continue;
				}
				MsgOut.println("parseColor: too many values");
				break;
			case ')':
			case 0:
				break;
			case '(':
				if (depth == 0) {
					depth = 1;
					continue;
				}
			default:
				MsgOut.println("parseColor: illegal list structure");
			}
			break;
		}
		switch (values) {
		case 0:
			MsgOut.println("parseColor: missing rgb values");
			break;
		case 1:
			MsgOut.println("parseColor: missing green and blue values");
			break;
		case 2:
			MsgOut.println("parseColor: missing blue value");
		}
		return ColorCache.get(r, g, b, a);		
	}

	// Parse an attribute record to set the appropriate edge point factors
	// supplied in an attribute record.

	public static void parsePoints(String avi, EntityInstance e, int side, EdgePoint[] ept) 
	{
		Ta					ta     = e.getTa();
		int					state  = 0;
 		int					ttype;
		RelationClass		rc     = null;
		int					index  = 0;
		short				wf     = Short.MIN_VALUE + 1;	// ie 0
		short				hf;
		EdgePoint			edgePoint;


		for (load(avi); (ttype = nextToken()) != 0; ) {
			switch (state) {
			case 0:
				if (ttype != '(') {
					MsgOut.println("parsePoints: not a list");
					break;
				}
				state = 1;
				continue;
			case 1:
				if (ttype != '(') {
					MsgOut.println("parsePoints: not a list of lists");
					break;
				}
				state = 2;
				continue;
			case 2:
				rc = ta.getRelationClass(m_sval);
				if (rc == null) {
					state = 5;
					continue;
				}
				index = rc.getNid() * EdgePoint.SIDES + side;
				state = 3;
				continue;
			case 3:
				wf = Util.parseRelativeValue(m_sval);
				state = 4;
				continue;
			case 4:
				hf = Util.parseRelativeValue(m_sval);

				edgePoint = ept[index];
				if (edgePoint == null) {
					ept[index] = edgePoint = new EdgePoint();
				} 
				edgePoint.setFactors(e.getEntityClass(), wf, hf);
				state = 5;
				continue;
			case 5:
				if (ttype == ')') {
					state = 6;
				}
				continue;
			case 6:
				switch (ttype) {
				case '(':
					state = 2;
					continue;
				case ')':
					break;
				default:
					MsgOut.println("parsePoints: illegal list structure");
				}
				break;
			case 7:
				if (ttype != 0) {
					MsgOut.println("parsePoints: expected EOF");
				}
				break;
			}
			break;
		}
	}

	public static void parseElisions(String avi, Ta ta, int type, BitSet bitset) 
	{
		String				sval;
		int					depth  = 0;
		RelationClass		rc;

		for (load(avi);;) {
			switch (nextToken()) {
			case CT_QUOTE:
				sval = m_sval;
				rc   = ta.getRelationClass(sval);
				if (rc == null) {
					MsgOut.println("parseElisions: '" + sval + "'is not a known relation class");
					continue;
				}
				bitset.set(rc.getNid() * EntityInstance.ELISIONS + type);
				continue;
			case ')':
			case 0:
				break;
			case '(':
				if (depth == 0) {
					depth = 1;
					continue;
				}
			default:
				MsgOut.println("parseElisions: illegal list structure");
			}
			break;
		}
	}

	public static void parseXEntityPositions(String avi, EntityPosition[] positions)
	{
		int					depth  = 0;
		int					i      = 0;
		EntityPosition		position;


		for (load(avi);;) {
			switch (nextToken()) {
			case CT_QUOTE:
				position = positions[i];
				if (position == null) {
					positions[i] = position = new EntityPosition();
				}
				position.m_xRelLocal  = Util.parseRelativeValue(m_sval);
				++i;
				continue;
			case ')':
			case 0:
				break;
			case '(':
				if (depth == 0) {
					depth = 1;
					continue;
				}
			default:
				MsgOut.println("parseEntityPositions: illegal X list structure");
			}
			break;
		}
	}

	public static void parseYEntityPositions(String avi, EntityPosition[] positions)
	{
		int					depth  = 0;
		int					i      = 0;
		EntityPosition		position;

		for (load(avi);;) {
			switch (nextToken()) {
			case CT_QUOTE:
				position = positions[i];
				if (position == null) {
					positions[i] = position = new EntityPosition();
				}
				position.m_yRelLocal = Util.parseRelativeValue(m_sval);
				++i;
				continue;
			case ')':
			case 0:
				break;
			case '(':
				if (depth == 0) {
					depth = 1;
					continue;
				}
			default:
				MsgOut.println("parseEntityPositions: illegal Y list structure");
			}
			break;
		}
	}

	public static void parseWidthEntityPositions(String avi, EntityPosition[] positions)
	{
		int					depth  = 0;
		int					i      = 0;
		EntityPosition		position;

		for (load(avi);;) {
			switch (nextToken()) {
			case CT_QUOTE:
				position = positions[i];
				if (position == null) {
					positions[i] = position = new EntityPosition();
				}
				position.m_widthRelLocal = Util.parseRelativeValue(m_sval);
				++i;
				continue;
			case ')':
			case 0:
				break;
			case '(':
				if (depth == 0) {
					depth = 1;
					continue;
				}
			default:
				MsgOut.println("parseEntityPositions: illegal width list structure");
			}
			break;
		}
	}

	public static void parseHeightEntityPositions(String avi, EntityPosition[] positions)
	{
		int					depth  = 0;
		int					i      = 0;
		EntityPosition		position;

		for (load(avi);;) {
			switch (nextToken()) {
			case CT_QUOTE:
				position = positions[i];
				if (position == null) {
					positions[i] = position = new EntityPosition();
				}
				position.m_heightRelLocal = Util.parseRelativeValue(m_sval);
				++i;
				continue;
			case ')':
			case 0:
				break;
			case '(':
				if (depth == 0) {
					depth = 1;
					continue;
				}
			default:
				MsgOut.println("parseEntityPositions: illegal list structure");
			}
			break;
		}
	}
	
	public static void followLink(String avi, LandscapeEditorCore ls, EntityInstance e, boolean mustbeContainer) 
	{
		String	file   = null;
		int		target = LsLink.TARGET_NEW;

		int					values = 0;
		int					depth  = 0;
		String				tgtStr;

		for (load(avi);;) {
			switch (nextToken()) {
			case CT_QUOTE:
				switch (++values) {
				case 1:
					file = Util.expand(m_sval, e.getId(), ls);
					continue;
				case 2:
					// The second argument here allows us to say how the target is to be followed
					tgtStr = Util.expand(m_sval, ls);
					target = LsLink.convertTarget(tgtStr);
				}
				continue;
			case ')':
			case 0:
				break;
			case '(':
				if (depth == 0) {
					depth = 1;
					continue;
				}
			default:
				MsgOut.println("followLink: illegal list structure");
				target = LsLink.TARGET_ERR;
				break;
			}
			break;
		} 

		switch (target) {
		case LsLink.TARGET_APP:
			// Eventually the link name should be resolved
			// we may want to allow a navigate on one 
			// entity to go to another
			ls.navigateTo(e, mustbeContainer);
		case LsLink.TARGET_ERR:
			break;
		default:
			ls.followURL(file, target);
		}
	}
}

