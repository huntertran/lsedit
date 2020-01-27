package lsedit;

import java.awt.Color;

import java.util.Vector;
import java.util.Enumeration;
import java.io.File;
import java.io.PrintWriter;

import javax.swing.Icon;

public final class EntityClass extends LandscapeClassObject /* extends LandscapeObject3D extends LandscapeObject */ 
{
	public static final String ENTITY_BASE_CLASS_ID		= "$ENTITY";

	protected static final String CLASSANGLE_ID = "class_angle";
	protected static final String CLASSIMAGE_ID = "class_image";
	protected static final String CLASSICON_ID  = "class_icon";

	public static final int	ID_ATTR				= 0;
	public static final int	CLASSLABEL_ATTR		= 1;
	public static final int	CLASSDESC_ATTR		= 2;
	public static final int	CLASSSTYLE_ATTR		= 3;
	public static final int	CLASSANGLE_ATTR		= 4;
	public static final int	CLASSIMAGE_ATTR		= 5;
	public static final int	COLOR_ATTR			= 6;
	public static final int	LABEL_COLOR_ATTR	= 7;
	public static final int	OPEN_COLOR_ATTR		= 8;
	public static final int CLASSICON_ATTR      = 9; 

	public static final int ATTRS               = 10;

	public static final String[] attributeName =
	{
		"id",
		CLASSLABEL_ID,
		CLASSDESC_ID,
		CLASSSTYLE_ID,
		CLASSANGLE_ID,
		CLASSIMAGE_ID,
		COLOR_ID,
		LABEL_COLOR_ID,
		OPEN_COLOR_ID,
		CLASSICON_ID
	};

	public static final int[] attributeType =
	{
		Attribute.STRING_TYPE,
		Attribute.STRING_TYPE,
		Attribute.TEXT_TYPE,
		Attribute.ENTITY_STYLE_TYPE,
		Attribute.DOUBLE_TYPE,
		Attribute.ENTITY_IMAGE_TYPE,
		Attribute.COLOR_OR_NULL_TYPE,
		Attribute.COLOR_OR_NULL_TYPE,
		Attribute.COLOR_OR_NULL_TYPE,
		Attribute.STRING_TYPE
	};


	public static final int ENTITY_STYLE_NONE           = -1;
	public static final int ENTITY_STYLE_3DBOX			= 0;
	public static final int ENTITY_STYLE_2DBOX			= 1;
	public static final int ENTITY_STYLE_FILE			= 2;
	public static final int ENTITY_STYLE_DISK			= 3;
	public static final int ENTITY_STYLE_FOLDER			= 4;
	public static final int ENTITY_STYLE_SOURCEOBJ		= 5;
	public static final int ENTITY_STYLE_CLASS			= 6;
	public static final int ENTITY_STYLE_GROUP			= 7;
	public static final int ENTITY_STYLE_LABELLED_GROUP = 8;
	public static final int ENTITY_STYLE_TRIANGLE       = 9;
	public static final int ENTITY_STYLE_ROMBUS		    = 10;
	public static final int ENTITY_STYLE_TRAPEZOID      = 11;
	public static final int ENTITY_STYLE_TRIANGLE2      = 12;

	public static final int ENTITY_STYLE_RECTANGLE      = 13;
	public static final int ENTITY_STYLE_PENTAGON       = 14;
	public static final int ENTITY_STYLE_HEXAGON        = 15;
	public static final int ENTITY_STYLE_OCTAGON        = 16;
	public static final int ENTITY_STYLE_DECAHEDRON     = 17;
	public static final int ENTITY_STYLE_12SIDED        = 18;
	public static final int ENTITY_STYLE_14SIDED        = 19;
	public static final int ENTITY_STYLE_16SIDED        = 20;
	public static final int ENTITY_STYLE_18SIDED        = 21;
	public static final int ENTITY_STYLE_20SIDED        = 22;
	public static final int ENTITY_STYLE_PAPER          = 23;
	public static final int ENTITY_STYLE_SUNK_BOX       = 24;

	public static final String[] styleName =
		{ 
				"3D Box", 
				"2D Box", 
				"File", 
				"Disk", 
				"Folder", 
				"Source Object", 
				"Class", 
				"Group", 
				"Labelled Group",
				"Triangle  (directed)",
				"Rombus    (directed)",
				"Trapezoid (directed)",
				"3-sided  Triangle",
				"4-sided  Diamond",
				"5-sided  Pentagon",
				"6-sided  Hexagon",
				"8-sided  Octagon",
				"10-sided Decahedron",
				"12 sided Polygon",
				"14 sided Polygon",
				"16 sided Polygon",
				"18 sided Polygon",
				"20 sided Dodecahedron",
				"Paper",
				"Sunken Box"
		};


	public static final int ENTITY_IMAGE_NONE           =   0x00;
	public static final int ENTITY_IMAGE_ACTOR          =   0x01;
	public static final int ENTITY_IMAGE_OVAL           =   0x02;
	public static final int ENTITY_IMAGE_FRAME          =   0x04;
	public static final int ENTITY_IMAGE_ROUNDED_FRAME  =   0x08;
	public static final int ENTITY_IMAGE_X              =   0x10;
	public static final int ENTITY_IMAGE_CLASS          =   0x20;
	public static final int ENTITY_IMAGE_SPAN           =   0x40;
	public static final int ENTITY_IMAGE_NAV_NEXT       =	0x80;
	public static final int ENTITY_IMAGE_NAV_PREV	    =   0x100;
	

	public static final int ENTITY_IMAGE_LAST           =   ENTITY_IMAGE_NAV_PREV;

	public static final String[] imageName =
		{ 
				"Actor",
				"Oval",
				"Frame",
				"Rounded Frame",
				"X",
				"Class",
				"Span",
				"Nav Next",
				"Nav Prev"				
		};

	protected int		m_image = 0;
	protected double	m_angle = 0;

	protected boolean	m_unscaledIconBad    = false;
	protected Icon		m_unscaledIcon       = null;
	protected String	m_unscaledIconFile   = null;
	
	private EdgePoint[] m_edgePoints;

	OpenIcon	m_openIcon   = null;
	ClosedIcon	m_closedIcon = null;
	LeafIcon	m_leafIcon   = null;
	
	// Overrides LandscapeClassObject

	public int setShown(int value, boolean applyToSubclassesTo)
	{
		int	ret = 0;
		
		if (m_shown != value) {
			if (value == 0) {
				Diagram	diagram = getDiagram();
				if (diagram != null) {
					diagram.clearEntityClassGroupFlags(this);
			}	}
			ret     = 1;
		}
		if (super.setShown(value, applyToSubclassesTo) != 0) {
			ret = -1;
		}
//		System.out.println("setShown(" + value + ") returned " + ret + ": " + this);
		return(ret);
	}

	// ----------------
	// Exported methods
	// ----------------

	// Constructor 

	public EntityClass(String id, int nid, Ta ta) 
	{
		super(ta);
		setId(id);
		setNid(nid); 
		setLabel(id); 
	}
	
	public EntityClass getView()
	{
		EntityClass	ec = new EntityClass(getId(), getNid(), getTa());
		
		ec.setShown(getShown(), false);
		ec.setActive(getActive(), false);
		return ec;
	}
	
	public void setView()
	{
		Ta			ta = getTa();
		EntityClass ec = ta.getEntityClass(getId());
		
		if (ec != null) {
			ec.setShown(getShown(), false);
			ec.setActive(getActive(), false);
	}	}
	
	public static String getEntityStyleName(int style) 
	{
		if (style < 0 || style >= EntityClass.styleName.length) {
			return "";
		}
		return styleName[style];
	}

	public String getStyleName(int style)
	{
		return EntityClass.getEntityStyleName(style);
	}

	public int	getImage()
	{
		return m_image;
	}

	public void setImage(int image)
	{
		m_image = image;
	}

	public double	getAngle()
	{
		return m_angle;
	}

	public int getDirection() 
	{
		int val = (int) m_angle;

		return( ((val + 45) / 90) % 4);
	}

	public void setAngle(double angle)
	{
		m_angle = angle;
	}
	
	public void clearUnscaledIcon()
	{
		m_unscaledIcon       = null;
		m_unscaledIconBad    = false;
	}
	
	public void clearIcons()
	{
		if (m_openIcon != null) {
			m_openIcon.clear();
		}
		if (m_closedIcon != null) {
			m_closedIcon.clear();
		}
		if (m_leafIcon != null) {
			m_leafIcon.clear();
		}
		clearUnscaledIcon();
	}

	public Icon getUnscaledIcon()
	{
		Icon unscaledIcon = m_unscaledIcon;

		if (unscaledIcon == null) {
			if (!m_unscaledIconBad && m_unscaledIconFile != null) {
				unscaledIcon = IconCache.lookup(m_unscaledIconFile);
				if (unscaledIcon == null) {
					unscaledIcon = IconCache.get(m_unscaledIconFile, this);
				}
				if (unscaledIcon == null) {
					m_unscaledIconBad = true;
				} else {
					m_unscaledIcon = unscaledIcon;
		}	}	}
		return (unscaledIcon);
	}

	public String getUnscaledIconFile()
	{
		return m_unscaledIconFile;
	}

	public void setUnscaledIconFile(String iconFile)
	{
		clearUnscaledIcon();
		m_unscaledIconFile   = iconFile;
	}
	
	public boolean processFirstOrder(String id, String value) 
	{
		if (id.equals(CLASSIMAGE_ID)) {
			if (value != null) {
				setImage(Attribute.parseIntValue(value));
			}
			return true;
		}
		if (id.equals(CLASSANGLE_ID)) {
			if (value != null) {
				setAngle(Attribute.parseDoubleValue(value));
			}
			return true;
		}
		if (id.equals(CLASSICON_ID)) {
			if (value != null) {
				setUnscaledIconFile(value);
			}
			return true;
		}
		return super.processFirstOrder(id, value);
	}

	public void writeAttributes(PrintWriter ps)
	{
		String	nodeId   = getId();
		double	angle    = getAngle();
		int		image    = getImage();
		String	iconFile = getUnscaledIconFile();
		
		nodeId = super.writeAttributes(ps, nodeId);

		if (angle != 0) {
			nodeId = writeAttribute(ps, nodeId, CLASSANGLE_ID, angle);
		}
		if (image != 0) {
			nodeId = writeAttribute(ps, nodeId, CLASSIMAGE_ID, image);
		}
		if (iconFile != null) {
			nodeId = writeAttribute(ps, nodeId, CLASSICON_ID, qt(iconFile));
		}
		if (nodeId == null) {
			ps.println("}");
			ps.println(""); 
	}	}

	public void reportClassAttributes(ResultBox resultBox)
	{
		super.reportClassAttributes(resultBox);
		resultBox.addText(CLASSIMAGE_ID);
		resultBox.addText(CLASSANGLE_ID);
		resultBox.addText(CLASSICON_ID);
	}

	// A raw constructor of uncached entities

	public EntityInstance newEntity(String id) 
	{
		EntityInstance e = new EntityInstance(this, id);
		return e;
	}

	protected EdgePoint[] needEdgePoints()
	{
		EdgePoint[]	edgePoints = m_edgePoints;
		int			needed     = getTa().numRelationClasses() * EdgePoint.SIDES;

		if (edgePoints == null) {
			m_edgePoints  = edgePoints = new EdgePoint[needed];
		} else {
			int	have = m_edgePoints.length;

			if (have < needed) {
				EdgePoint[] old = edgePoints;

				m_edgePoints = edgePoints = new EdgePoint[needed];
				for (int i = 0; i < have; ++i) {
					edgePoints[i] = old[i];
				}
				old = null;
		}	}
		return edgePoints;
	}

	protected void adjustEdgePoints() 
	{
		EdgePoint[]	edgePoints = m_edgePoints;
		EdgePoint	edgePoint;

		if (edgePoints != null) {
			int i      = 0;
			int	length = edgePoints.length;

			for (; i < length; ++i) {
				edgePoint = edgePoints[i];
				if (edgePoint != null) {
					edgePoint.adjustEdgePoint(this);
	}	}	}	}

	public EdgePoint getPoint(int index)
	{
		return(m_edgePoints[index]);
	}

	public EdgePoint getPoint(RelationClass rc, int side) 
	{
		int			index      = rc.getNid() * EdgePoint.SIDES + side;
		EdgePoint[] edgePoints = needEdgePoints();
		EdgePoint	edgePoint  = edgePoints[index];

		// Cache calculated points

		if (edgePoint == null) {

			edgePoints[index] = edgePoint = new EdgePoint();

			edgePoint.setFactors(this, rc, side);
		}
		return edgePoint;
	}

	public int	getEdgePointIndex(int startindex, int endindex, EdgePoint edgePoint)
	{
		EdgePoint[]	edgePoints = m_edgePoints;
		int			index;

		if (edgePoints != null) {
			for (index = startindex; index < endindex; ++index) {
				if (edgePoints[index] == edgePoint) {
					return(index);
		}	}	}
		return(-1);
	}

	public void changeIOfactor(RelationClass rc)
	{
		EdgePoint[]	edgePoints = m_edgePoints;

		if (edgePoints != null) {
			int			index      = rc.getNid() * EdgePoint.SIDES;
			int			endindex   = index + EdgePoint.SIDES;
			EdgePoint	edgePoint;
			int			side;

			if (endindex > edgePoints.length) {
				endindex = edgePoints.length;
			}
			for (; index < endindex; ++index) {
				edgePoint = edgePoints[index];
				if (edgePoint != null) {
					side = index % EdgePoint.SIDES;
					edgePoint.setFactors(this, rc, side);
		}	}	}
	}

	public Icon getOpenIcon()
	{
		if (m_openIcon == null) {
			m_openIcon = new OpenIcon(this);
		}
		return m_openIcon;
	}

	public Icon getClosedIcon()
	{
		if (m_closedIcon == null) {
			m_closedIcon = new ClosedIcon(this);
		}
		return m_closedIcon;
	}

	public Icon getLeafIcon()
	{
		if (m_leafIcon == null) {
			m_leafIcon = new LeafIcon(this);
		}
		return m_leafIcon;
	}

	public void getUsage(int[][] array, int direction)
	{
		Ta				ta               = getTa();
		int				numRelationClasses = ta.numRelationClasses();
		int				numEntityClasses = ta.numEntityClasses();
		EntityCache		entityCache      = ta.getEntityCache();
		int[]			row;
		Enumeration		en;
		EntityClass		ec, ec1;
		EntityInstance	e, e1;
		RelationClass	rc;
		RelationInstance ri;
		int				i, j, size;
		Vector			v;
		boolean			omitSpan;


		for (i = 0; i < numRelationClasses; ++i) {
			row = array[i];
			for (j = 0; j < numEntityClasses; ++j) {
				row[j] = 0;
		}	}

		switch (direction) {
		case ClassUsage.EC_DIRECTION_ALL_EXCEPT_SPAN:
			direction = ClassUsage.EC_DIRECTION_ALL;
			omitSpan  = true;
			break;
		case ClassUsage.EC_DIRECTION_FROM_EXCEPT_SPAN:
			direction = ClassUsage.EC_DIRECTION_FROM;
			omitSpan  = true;
			break;
		case ClassUsage.EC_DIRECTION_TO_EXCEPT_SPAN:
			direction = ClassUsage.EC_DIRECTION_TO;
			omitSpan  = true;
			break;
		default:
			omitSpan = false;
		}
		

		for (e = entityCache.getFirst(); e != null; e = entityCache.getNext()) {
			ec  = e.getEntityClass();
			if (ec != this) {
				continue;
			}


			switch (direction) {
			case ClassUsage.EC_DIRECTION_ALL:
			case ClassUsage.EC_DIRECTION_FROM:
			case ClassUsage.EC_DIRECTION_LOOP:
			case ClassUsage.EC_DIRECTION_CHILD:
			case ClassUsage.EC_DIRECTION_SPAN:
				v = e.getSrcRelList();
				if (v != null) {
					for (i = v.size(); --i >= 0; ) {
						ri  = (RelationInstance) v.elementAt(i);
						e1  = ri.getDst();
	
						if (e1 == e) {
							switch (direction) {
							case ClassUsage.EC_DIRECTION_ALL:
							case ClassUsage.EC_DIRECTION_LOOP:
								break;
							default:
								continue;
							}
						} else {
							if (direction == ClassUsage.EC_DIRECTION_LOOP) {
								continue;
							}
							if (ri == e1.getContainedByRelation()) {
								// e1 is child of e
								if (omitSpan) {
									continue;
								}
							} else {
								switch (direction) {
								case ClassUsage.EC_DIRECTION_CHILD:
								case ClassUsage.EC_DIRECTION_SPAN:
									continue;
						}	}	}
	
						rc  = ri.getRelationClass();
						row = array[rc.getOrderedId()];
						ec1 = e1.getEntityClass();
						++row[ec1.getOrderedId()];
				}	}
				if (direction != ClassUsage.EC_DIRECTION_ALL && direction != ClassUsage.EC_DIRECTION_SPAN) {
					break;
				}
			case ClassUsage.EC_DIRECTION_PARENT:
			case ClassUsage.EC_DIRECTION_TO:
				v = e.getDstRelList();
				if (v != null) {
					for (i = v.size(); --i >= 0; ) {
						ri  = (RelationInstance) v.elementAt(i);
						e1  = ri.getSrc();
						if (e1 == e) {
							// Don't count loops twice
							continue;
						}
						if (ri == e.getContainedByRelation()) {
							//e1 is parent of e
							if (omitSpan) {
								continue;
							}
						} else {
							switch (direction) {
							case ClassUsage.EC_DIRECTION_PARENT:
							case ClassUsage.EC_DIRECTION_SPAN:
								continue;
						}	}
		
						rc  = ri.getRelationClass();
						row = array[rc.getOrderedId()];
						ec1 = e1.getEntityClass();
						++row[ec1.getOrderedId()];
				}	}
				break;
	}	}	}
				
	// The routines that follow hide the complexity of getting/setting attribute values
	// from EditAttributes

	public int getPrimaryAttributeCount()
	{
		return(ATTRS);
	}

	public String getLsAttributeNameAt(int index)
	{
		String	name;

		if (index < ATTRS) {
			name = attributeName[index];
		} else {
			name  = super.getLsAttributeNameAt(index);
		}
		return(name);
	}

	// Need to know the type in cases where value might be null
	// For example with some colors

	public int getLsAttributeTypeAt(int index)
	{
		int		ret;
	
		if (index < ATTRS) {
			ret = attributeType[index];
		} else {
			ret = super.getLsAttributeTypeAt(index);
		}
		return(ret);
	}

	public Object getLsAttributeValueAt(int index)
	{
		Object	value;

		switch (index) {
		case ID_ATTR:
			value = getId();
			break;
		case CLASSLABEL_ATTR:
			value = getLabel();
			break;
		case CLASSDESC_ATTR:
			value = getDescription();
			break;
		case CLASSSTYLE_ATTR:
			value = new Integer(getStyle());
			break;
		case CLASSANGLE_ATTR:
			value = new Double(getAngle());
			break;
		case CLASSIMAGE_ATTR:
			value = new Integer(getImage());
			break;
		case COLOR_ATTR:
			value = getObjectColor();
			break;
		case LABEL_COLOR_ATTR:
			value = getLabelColor();
			break;
		case OPEN_COLOR_ATTR:
			value = getColorWhenOpen();
			break;
		case CLASSICON_ATTR:
			value = getUnscaledIconFile();
			break;
		default:
			value = super.getLsAttributeValueAt(index);
		}
		return(value);
	}
}



