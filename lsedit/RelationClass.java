package lsedit;

import java.awt.Color;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public final class RelationClass extends LandscapeClassObject /* extends LandscapeObject3D extends LandscapeObject */ {

	protected static final String FACTOR_ID      = "class_iofactor";
	protected static final String HIERARCHY_ID   = "class_hierarchy";
	protected static final String ISCONTAINS_ID  = "class_iscontains";
	protected static final String CLASSRLABEL_ID = "class_rlabel";

	public static final int	ID_ATTR				= 0;
	public static final int	CLASSLABEL_ATTR		= 1;
	public static final int CLASSRLABEL_ATTR    = 2;
	public static final int	CLASSDESC_ATTR		= 3;
	public static final int	CLASSSTYLE_ATTR		= 4;
	public static final int	COLOR_ATTR			= 5;
	public static final int	LABEL_COLOR_ATTR	= 6;
	public static final int	FACTOR_ATTR			= 7;
	public static final int ARROWCOLOR_ATTR     = 8;
	public static final int ATTRS               = 9;

	public static final String[] attributeName =
	{
		"id",
		CLASSLABEL_ID,
		CLASSRLABEL_ID,
		CLASSDESC_ID,
		CLASSSTYLE_ID,
		COLOR_ID,
		LABEL_COLOR_ID,
		FACTOR_ID,
		ARROWCOLOR_ID
	};

	public static final int[] attributeType =
	{
		Attribute.STRING_TYPE,
		Attribute.STRING_TYPE,
		Attribute.STRING_TYPE,
		Attribute.TEXT_TYPE,
		Attribute.REL_STYLE_TYPE,
		Attribute.COLOR_OR_NULL_TYPE,
		Attribute.COLOR_TYPE,
		Attribute.DOUBLE_TYPE,
		Attribute.COLOR_OR_NULL_TYPE
	};

	protected static final String RELATION_BASE_CLASS_ID = "$RELATION";

	protected int		m_containsOffset = -1;				// Offset into the Ta.m_containsClasses array else -1

	protected short		m_iofactor   = Util.undefined();	// Where the relation should intersect entities as a percentage
	protected int		m_cIndex     = -1;					// What is the offset of EntityShape[] for entity shape using this relation as contains
	protected int		m_weight     = 1;
	protected Color		m_arrowColor = null;
	protected String	m_reversedLabel = null;

	// Permitted relations

	// This is the list of Entity class pairs that can participate in relations of this relation class
	// It can be presumed to be quite small.

	private Vector		m_relationList = new Vector();
		 
	protected int		m_ordinal;		// 0 is first relation

	// Overrides LandscapeClassObject methods

	public int setShown(int value, boolean applyToSubclassesTo) 
	{
		int shown = m_shown;
		int	ret   = 0;
		
		if (shown != value) {
			Diagram diagram  = getDiagram();
			
			if (value == LandscapeClassObject.DIRECTION_NONE) {
				// About to hide this relation class
				diagram.clearRelationClassGroupFlags(this);
				m_weight = 0;
				diagram.invalidateVisibleRelationClasses();
			} else if (shown == LandscapeClassObject.DIRECTION_NONE) {
				// Was hiding this relation class
				m_weight = ((m_containsOffset == 0) ? 0 : 1); 
				diagram.invalidateVisibleRelationClasses();
			}
			if (value == LandscapeClassObject.DIRECTION_REVERSED) {
				diagram.reverseRelations(this, true);
			} else if (shown == LandscapeClassObject.DIRECTION_REVERSED) {
				diagram.reverseRelations(this, false);
			}
			ret = 1;
		}
		if (super.setShown(value, applyToSubclassesTo) != 0) {
			ret = -1;
		}
		return(ret);
	}
	
	// --------------
	// Public methods
	// --------------

	public RelationClass(String id, int nid, Ta ta)
	{
		super(ta);
		setId(id);						// The string name of this relation class
		setNid(nid);					// The numeric id
		setLabel(id);					// The label of this relation
	}

	public String getReversedLabel()
	{
		return m_reversedLabel;
	}
	
	public String getRelationLabel()
	{
		String ret;

		if (getShown() != LandscapeClassObject.DIRECTION_REVERSED) {
			ret = getLabel();
		} else {
			ret = getReversedLabel();
			if (ret != null) {
				return ret;
			}
			ret = getLabel();
			if (ret != null) {
				ret += " (r)";
		}	}
		return ret;
	}

	public void setReversedLabel(String value)
	{
		m_reversedLabel = value;
	}

	public RelationClass getView()
	{
		RelationClass	rc = new RelationClass(getId(), getNid(), getTa());
		
		rc.setShown(getShown(), false);
		rc.setActive(getActive(), false);
		rc.setContainsClassOffset(getContainsClassOffset());
	
		return rc;
	}
	
	public boolean setView()
	{
		Ta				ta = getTa();
		RelationClass	rc = ta.getRelationClass(getId());
		
		if (rc != null) {
			rc.setShown(getShown(), false);
			rc.setActive(getActive(), false);
			if (rc.getContainsClassOffset() != getContainsClassOffset()) {
				rc.setContainsClassOffset(getContainsClassOffset());
				return true;
		}	}
		return false;
	}
	
	public int getCIndex()
	{
		return m_cIndex;
	}
	
	public int computeCIndex()
	{
		int cindex = m_cIndex;
		
		if (cindex < 0) {
			Diagram	diagram = getDiagram();
			
			cindex   = diagram.getMaxCIndex() + 1;
			m_cIndex = cindex;
		} 
		return cindex;
	}
	
	public void setCIndex(int value)
	{
		m_cIndex = value;
	}

	public int getWeight()
	{
		return m_weight;
	}

	public void setWeight(int value)
	{
		m_weight = value;
	}

	public String getStyleName(int style) 
	{
		return Util.getLineStyleName(style);
	}

	public void setContainsClassOffset(int containsOffset)
	{
		if (m_containsOffset != containsOffset) {
			m_containsOffset = containsOffset;
			m_weight         = (((containsOffset==0) || m_shown==0) ? 0 : 1);
	}	}

	public Color getArrowColor()
	{
		return m_arrowColor;
	}

	public void setArrowColor(Color arrowColor)
	{
		m_arrowColor = arrowColor;
	}

	// Is this the contains relation

	public int getContainsClassOffset() 
	{
		return m_containsOffset;
	}

	public boolean processFirstOrder(String id, String value) 
	{
		if (id.equals(CLASSRLABEL_ID)) {
			String s = Attribute.parseStringValue(value);
			if (s != null) {
				setReversedLabel(s);
			}
			return true;
		}

		if (id.equals(ARROWCOLOR_ID)) {
			setArrowColor(Attribute.parseColorValue(value, this, ARROWCOLOR_ID));
			return true;
		}
		if (id.equals(HIERARCHY_ID)) {
			if (value != null) {
				m_cIndex = Attribute.parseIntValue(value, m_cIndex);
			}
			return true;
		}
		if (id.equals(FACTOR_ID)) {
			if (value != null) {
				short iofactor = Util.parseRelativeValue(value);
				if (Util.defined(iofactor)) {
					m_iofactor = iofactor;
			}	}
			return true;
		}
		if (id.equals(ISCONTAINS_ID)) {
			if (value != null) {
				int	containsClassOffset;
				if (value.startsWith("f")) {
					containsClassOffset = -1;
				} else if (value.startsWith("t")) {
					containsClassOffset = 0;
				} else {
					containsClassOffset = Util.parseInt(value, -1);
				}
				setContainsClassOffset(containsClassOffset);
			}
			return true;
		}
		return super.processFirstOrder(id, value);
	}

	public void reportClassAttributes(ResultBox resultBox)
	{
		resultBox.addText(HIERARCHY_ID);
		resultBox.addText(CLASSRLABEL_ID);
		resultBox.addText(FACTOR_ID);
		resultBox.addText(ACTIVE_ID);
		resultBox.addText(VISIBLE_ID);
		resultBox.addText(ISCONTAINS_ID);

		super.reportClassAttributes(resultBox);
	}

	// If new, add an edge to the ERD (permitted relation)

	public Vector getRelationList()
	{
		return m_relationList;
	}

	public void addRelationConstraint(EntityClassPair ep)
	{
		m_relationList.addElement(ep);
	}

	public void addRelationConstraint(EntityClass ec1, EntityClass ec2) 
	{
		if (m_relationList == null) {
			m_relationList = new Vector();
		} else {
			Enumeration		en;
			EntityClassPair ep;

			for (en = m_relationList.elements(); en.hasMoreElements(); ) {
				ep = (EntityClassPair) en.nextElement();
				if (ep.equals(ec1, ec2)) {
					return;
				}
		}	}

		addRelationConstraint(new EntityClassPair(ec1, this, ec2));
	}

	public void removeRelation(EntityClassPair ep) 
	{
		Vector relationList;

		relationList = m_relationList;
		if (relationList != null) {
			relationList.remove(ep);
	}	}

	public boolean[][] getInheritedRelationArray()
	{
		Ta				ta = getTa();
		boolean[][]		array;
		boolean[]		row;
		Enumeration		en, en1, en2, en3;
		RelationClass	rc;
		int				i, j, size;
		Vector			srcs, dsts;
		EntityClassPair	ep;
		EntityClass		src1, dst1;
		int				from, to;

		i = 0;
		for (en = ta.enumEntityClassesInOrder(); en.hasMoreElements(); ++i) {
			src1 = (EntityClass) en.nextElement();
			src1.setOrderedId(i);
		}
		size = i;

		array = new boolean[size][];
		for (i = 0; i < size; ++i) {
			array[i] = row = new boolean[size];
			for (j = 0; j < size; ++j) {
				row[j] = false;
		}	}

		Vector	rcs     = getClassAndSuperclasses();
		for (en = rcs.elements(); en.hasMoreElements(); ) {
			rc = (RelationClass) en.nextElement();
			for (en1 = rc.m_relationList.elements(); en1.hasMoreElements(); ) {
				ep = (EntityClassPair) en1.nextElement();
				srcs = ta.getClassAndSubclasses(ep.m_entityClass1);
				dsts = ta.getClassAndSubclasses(ep.m_entityClass2);
				for (en2 = srcs.elements(); en2.hasMoreElements(); ) {
					src1 = (EntityClass) en2.nextElement();
					from = src1.getOrderedId();
					for (en3 = dsts.elements(); en3.hasMoreElements(); ) {
						dst1 = (EntityClass) en3.nextElement();
						to   = dst1.getOrderedId();
						array[from][to] = true;
		}	}	}	}
		return array;
	}

	public void getUsage(int[][] array, int direction)
	{
		Ta				ta               = getTa();
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
		int[]			maxs;


		for (i = 0; i < numEntityClasses; ++i) {
			row = array[i];
			for (j = 0; j < numEntityClasses; ++j) {
				row[j] = 0;
		}	}

		switch (direction) {
		case ClassUsage.RC_DIRECTION_MAX:
		case ClassUsage.RC_DIRECTION_MAX_FROM:
		case ClassUsage.RC_DIRECTION_MAX_TO:
		case ClassUsage.RC_DIRECTION_MAX_FROM_TO:

			maxs = new int[numEntityClasses];

			for (e = entityCache.getFirst(); e != null; e = entityCache.getNext()) {
				for (i = 0; i < numEntityClasses; ++i) {
					maxs[i] = 0;
				}
				ec  = e.getEntityClass();
				row = array[ec.getOrderedId()];
				if (direction != ClassUsage.RC_DIRECTION_MAX_TO) {
					v = e.getSrcRelList();
					if (v != null) {
						row = array[ec.getOrderedId()];
						for (i = v.size(); --i >= 0; ) {
							ri = (RelationInstance) v.elementAt(i);
							rc = ri.getRelationClass();
							if (rc == this) {
								e1  = ri.getDst();
								ec1 = e1.getEntityClass();
								++maxs[ec1.getOrderedId()];
				}	}	}	}

				if (direction == ClassUsage.RC_DIRECTION_MAX_FROM_TO) {
					for (i = 0; i < numEntityClasses; ++i) {
						if (maxs[i] > row[i]) {
							row[i] = maxs[i];
						}
						maxs[i] = 0;
				}	}

				if (direction != ClassUsage.RC_DIRECTION_MAX_FROM) {
					v = e.getDstRelList();
					if (v != null) {
						row = array[ec.getOrderedId()];
						for (i = v.size(); --i >= 0; ) {
							ri = (RelationInstance) v.elementAt(i);
							rc = ri.getRelationClass();
							if (rc == this) {
								e1  = ri.getSrc();
								ec1 = e1.getEntityClass();
								++maxs[ec1.getOrderedId()];
				}	}	}	}
				for (i = 0; i < numEntityClasses; ++i) {
					if (maxs[i] > row[i]) {
						row[i] = maxs[i];
			}	}	}
			break;

		default:

			for (e = entityCache.getFirst(); e != null; e = entityCache.getNext()) {
				v = e.getSrcRelList();
				if (v != null) {
					ec  = e.getEntityClass();
					row = array[ec.getOrderedId()];
					for (i = v.size(); --i >= 0; ) {
						ri = (RelationInstance) v.elementAt(i);
						rc = ri.getRelationClass();
						if (rc == this) {
							e1 = ri.getDst();
							switch (direction) {
							case ClassUsage.RC_DIRECTION_LOOP:
								if (e != e1) {
									continue;
								}
								break;
							case ClassUsage.RC_DIRECTION_SPAN:
								if (e.getContainedByRelation() != ri && e1.getContainedByRelation() != ri) {
									continue;
								}
								break;
							case ClassUsage.RC_DIRECTION_ALL_EXCEPT_SPAN:
								if (e.getContainedByRelation() == ri || e1.getContainedByRelation() == ri) {
									continue;
								}
								break;
							}
							ec1 = e1.getEntityClass();
							++row[ec1.getOrderedId()];
	}	}	}	}	}	}
				
	public void writeEntityClassPairs(PrintWriter ps)
	{
		Enumeration en;
		EntityClassPair ep;

		for (en = m_relationList.elements(); en.hasMoreElements(); ) {
			ep = (EntityClassPair) en.nextElement();
			ps.println(qt(getId()) + " " + qt(ep.m_entityClass1.getId()) + " " + qt(ep.m_entityClass2.getId()));
		}
	}

	public void writeAttributes(PrintWriter ps) 
	{
		String nodeId = "(" + getId() + ")";
		
		if (m_cIndex >= 0) {
			nodeId = writeAttribute(ps, nodeId, HIERARCHY_ID, m_cIndex);
		}
		if (m_reversedLabel != null) {
			nodeId = writeAttribute(ps, nodeId, CLASSRLABEL_ID, m_reversedLabel);
		}
		if (Util.defined(m_iofactor)) {
			nodeId = writeAttribute(ps, nodeId, FACTOR_ID, m_iofactor);
		}
		if (0 <= m_containsOffset) {
			nodeId = writeAttribute(ps, nodeId, ISCONTAINS_ID, m_containsOffset);
		}

		Color color = getArrowColor();
		if (color != null) { 
			nodeId = writeAttribute(ps, nodeId, ARROWCOLOR_ID, Util.taColor(color));
		}
		
		nodeId = super.writeAttributes(ps, nodeId);
		if (nodeId == null) {
			ps.println("}");
			ps.println();
	}	}

	// Accessor functions

	public int	getOrdinal()
	{
		return m_ordinal;
	}

	public void setOrdinal(int ord) 
	{
		m_ordinal = ord;
	}

	public short getIOfactor()
	{
		short iofactor = m_iofactor;
		
		if (!Util.defined(iofactor)) {
			iofactor = Util.relativeToShort(getRelativeIOfactor());
		}
		return iofactor;
	}

	public double getRelativeIOfactor() 
	{
		if (Util.defined(m_iofactor)) {
			return Util.shortToRelative(m_iofactor);
		}
		
		Diagram diagram = getDiagram();

		if (diagram.allowElision()) {
			double num = diagram.numVisibleRelationClasses();
			return (m_ordinal+1)/(num+1);
		}
		double num = diagram.numRelationClasses();
		return ((num > 2) ? (getNid()-1)/(num-1) : 0.5);
	}

	public void setIOfactor(short value)
	{
		m_iofactor = value;
	}
	
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
		case CLASSRLABEL_ATTR:
			value = getReversedLabel();
			break;
		case CLASSDESC_ATTR:
			value = getDescription();
			break;
		case CLASSSTYLE_ATTR:
			value = new Integer(getStyle());
			break;
		case COLOR_ATTR:
			value = getObjectColor();
			break;
		case LABEL_COLOR_ATTR:
			value = getLabelColor();
			break;
		case FACTOR_ATTR:
			value = new Double(Util.shortToRelative(m_iofactor));
			break;
		case ARROWCOLOR_ATTR:
			value = getArrowColor();
			break;
		default:
			value = super.getLsAttributeValueAt(index);
		}
		return(value);
	}

}

