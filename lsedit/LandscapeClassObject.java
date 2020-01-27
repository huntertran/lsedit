package lsedit;

import java.awt.Color;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public abstract class LandscapeClassObject extends LandscapeObject3D /* extends LandscapeObject */ {

	protected static final String CLASSLABEL_ID = "class_label";
	protected static final String CLASSSTYLE_ID = "class_style";
	protected static final String CLASSDESC_ID	= "class_description";
	protected static final String ACTIVE_ID     = "class_active";
	protected static final String VISIBLE_ID    = "class_visible";

	public	  static final int    DIRECTION_NORMAL   = 0x01;
	public    static final int    DIRECTION_REVERSED = 0x02;
	public    static final int    DIRECTION_NONE     = 0x03;
		
	private Diagram	m_dg;								// Diagram which contains it
	private Ta		m_ta;								// TA for use when not part of a diagram

	protected int	m_shown        = DIRECTION_NORMAL;	// True if class active in legend (avoid the word visible - used by Swing)
	protected int	m_active       = DIRECTION_NORMAL;	// True if class active in queries
	protected int	m_orderedId;
	private Vector	m_inheritsFrom = new Vector();		// Set of class's this class inherits from
	private Vector	m_inheritedBy  = null;				// Set of classes that inherit from this class
	private int		m_nid          = -1;				// Used to output things in order entered
	private	int		m_style        = -1;
	private	int		m_members      = 0;					// Number of logical instantiations of this class

	public LandscapeClassObject(Ta ta)
	{
		m_ta = ta;
		m_dg = ta.getDiagram();
	}

	public boolean isShown()
	{
		return m_shown != 0;
	}
	
	public boolean isShown(int direction)
	{
		return (m_shown & direction) != 0;
	}
	
	public int getShown()
	{
		return(m_shown);
	}

	// Overloaded by subclasses
	// Returns -1 if any of the subclasses changed

	public int setShown(int value, boolean applyToSubclassesTo)
	{
		int ret = 0;

		if (value != m_shown) {
			m_shown = value;
			ret     = 1;
		}
		if (applyToSubclassesTo) {
			Vector inheritedBy = m_inheritedBy;
			int i, size;
			LandscapeClassObject child;

			if (inheritedBy != null) {
				size = inheritedBy.size();
				for (i = 0; i < size; ++i) {
					child = (LandscapeClassObject)inheritedBy.elementAt(i);
					if (this == child.getPrimaryInheritance())	{
						if (child.setShown(value, true) != 0) {
							ret = -1;
		}	}	}	}	}
		return (ret);
	}
	
	public int setShown(boolean value, boolean applyToSubclassesTo)
	{
		return(setShown((value ? DIRECTION_NORMAL : 0), applyToSubclassesTo));
	}
	
	public int setShown(int direction, boolean value, boolean applyToSubclassesTo) 
	{
		int	value1 = m_shown & ~direction;
		if (value) {
			value1 |= direction;
		}
		return setShown(value1, applyToSubclassesTo);
	}
	
	public boolean isActive()
	{
		return m_active != 0;
	}
	
	public boolean isActive(int direction)
	{
		return (m_active & direction) != 0;
	}
	
	public int getActive() 
	{
		return m_active;
	}

	public int setActive(int value, boolean applyToSubclassesTo) 
	{
		int ret = 0;

		if (m_active != value) {
			m_active = value; 
			ret      = 1;
		}
		if (applyToSubclassesTo) {
			Vector inheritedBy = m_inheritedBy;
			int i, size;
			LandscapeClassObject child;

			if (inheritedBy != null) {
				size = inheritedBy.size();
				for (i = 0; i < size; ++i) {
					child = (LandscapeClassObject)inheritedBy.elementAt(i);
					if (this == child.getPrimaryInheritance())	{
						if (child.setActive(value, true) != 0) {
							ret = -1;
		}	}	}	}	}
		return (ret);
	}
	
	public int setActive(boolean value, boolean applyToSubclassesTo)
	{
		return(setActive((value ? DIRECTION_NORMAL : 0), applyToSubclassesTo));
	}
	
	public int setActive(int direction, boolean value, boolean applyToSubclassesTo) 
	{
		int	value1 = m_active & ~direction;
		if (value) {
			value1 |= direction;
		}
		return setActive(value1, applyToSubclassesTo);
	}

	public int getOrderedId()
	{
		return(m_orderedId);
	}

	public void setOrderedId(int orderedId)
	{
		m_orderedId = orderedId;
	}

	void noMembers()
	{
		m_members = 0;
	}

	void incrementMembers() 
	{
		++m_members;
	};

	void decrementMembers()
	{
		--m_members;
	}

	public int countMembers()
	{
		return m_members;
	}

	public int getStyle()
	{
		return m_style;
	}

	public void setStyle(int value)
	{
		m_style = value;
	}

	public Enumeration enumInheritsFrom() 
	{
		return m_inheritsFrom.elements();
	}

	public Enumeration enumInheritedBy()
	{
		return m_inheritedBy.elements();
	}

	private void enumMyHierarchy(Vector to, boolean hideEmpty)
	{
		Vector					inheritedBy = m_inheritedBy;
		int						i, size, size1;
		LandscapeClassObject	child;
		
		to.add(this);
		size1 = to.size();
		if (inheritedBy != null) {
			size  = inheritedBy.size();
			for (i = 0; i < size; ++i) {
				child = (LandscapeClassObject) inheritedBy.elementAt(i);
				if (this == child.getPrimaryInheritance()) {
					child.enumMyHierarchy(to, hideEmpty);
		}	}	}
		if (hideEmpty && size1 == to.size() && countMembers() == 0) {
			to.removeElementAt(--size1);
	}	}
		
	public Enumeration enumHierarchy(boolean hideEmpty, int size)
	{
		Vector	to = new Vector(size);
		
		enumMyHierarchy(to, hideEmpty);
		return(to.elements());
	}
	
	public int	getInheritsFromCnt()
	{
		return m_inheritsFrom.size();
	}

	public Vector getInheritsFrom()
	{
		return m_inheritsFrom;
	}
	
	public Object getPrimaryInheritance()
	{
		if (m_inheritsFrom == null || m_inheritsFrom.size() == 0) {
			return(null);
		}
		return m_inheritsFrom.elementAt(0);
	}
	
	public int getInheritanceDepth()
	{
		int						depth;
		LandscapeClassObject	at;
		
		depth = 0;
		for (at = this; (at = (LandscapeClassObject) at.getPrimaryInheritance()) != null; ++depth);
		return(depth);
	}

	private void removeInheritedBy(LandscapeClassObject child)
	{
		m_inheritedBy.remove(child);
	}
	
	protected void addInheritedBy(LandscapeClassObject child)
	{
		if (m_inheritedBy == null) {
			m_inheritedBy = new Vector();
		}
		m_inheritedBy.addElement(child);
	}
	
	public void setInherits(Vector inheritsFrom)
	{
		Vector					old = m_inheritsFrom;
		LandscapeClassObject	classObject;
		int						i;
				
		if (old != inheritsFrom) {
			if (old != null) {
				for (i = 0; i < old.size(); ++i) {
					classObject = (LandscapeClassObject) old.elementAt(i);
					classObject.removeInheritedBy(this);
			}	}
						
			m_inheritsFrom = inheritsFrom;
			for (i = 0; i < inheritsFrom.size(); ++i) {
				classObject = (LandscapeClassObject) inheritsFrom.elementAt(i);
				classObject.addInheritedBy(this);
			}

			if (m_dg != null) {
				m_dg.getLs().repaint();
	}	}	}

	public boolean directlyInheritsFrom(Object thing)
	{
		return m_inheritsFrom.contains(thing);
	}

	public boolean subclassOf(LandscapeClassObject lco)
	{
		Enumeration				en;
		LandscapeClassObject	parent;

		for (en = enumInheritsFrom(); en.hasMoreElements(); ) {
			parent = (LandscapeClassObject) en.nextElement();
			if (parent == lco || parent.subclassOf(lco)) {
				return true;
		}	}
		return false;
	}

	// Get the set of classes that are this class or a subclass of it

	public Vector getClassAndSubclasses(Hashtable classes)
	{
		Vector				 v = new Vector();
		Vector				 inherits;
		LandscapeClassObject o;
		int					 i, size;
		boolean				 seen;
		Enumeration			 en;

		v.add(this);
		do {
			seen = false;
			for (en = classes.elements(); en.hasMoreElements();) {
				o = (LandscapeClassObject) en.nextElement();
				if (!v.contains(o)) {
					inherits = o.getInheritsFrom();
					size     = inherits.size();
					for (i = 0; i < size; ++i) {
						if (v.contains(inherits.elementAt(i))) {
							v.add(o);
							seen = true;
							break;
			}	}	}	}
		} while (seen);

		return v;
	}
	
	// This will return a vector in which nearer superclasses occur earlier than more distant ones

	public Vector getClassAndSuperclasses()
	{
		Vector				 v = new Vector();
		Vector				 inherits;
		LandscapeClassObject o, o1;
		int					 i, j, size;
		boolean				 seen;

		v.add(this);
		do {
			seen = false;
			for (i = 0; i < v.size(); ++i) {
				o        = (LandscapeClassObject) v.elementAt(i);
				inherits = o.getInheritsFrom();
				for (j = 0; j < inherits.size(); ++j) {
					o1 = (LandscapeClassObject) inherits.elementAt(j);
					if (!v.contains(o1)) {
						v.add(o1);
						seen = true;
			}	}	}
		} while (seen);

		return v;
	}
		
	public Vector getValidAttributes()
	{
		Vector	v1   = getClassAndSuperclasses();
		int		size = v1.size();
		Vector	v    = new Vector();
		LandscapeClassObject o;
		Attribute[]	attributes;
		Attribute attribute, attribute1;
		String	id;
		int		i, j, k,attributes_length;

		for (i = 0; i < size; ++i) {
			o = (LandscapeClassObject) v1.elementAt(i);
			attributes = o.m_attributes;
			if (attributes == null) {
				continue;
			}
			attributes_length = attributes.length;
			for (j = 0; j < attributes_length; ++j) {
				attribute = attributes[j];
				if (attribute == null) {
					continue;
				}
				id        = attribute.m_id;
				for (k = v.size(); ; ) {
					if (--k < 0) {
						v.add(attribute);
						break;
					}
					attribute1 = (Attribute) v.elementAt(k);
					if (id.equals(attribute1.m_id)) {
						break;
		}	}	}	}
		return v;
	}

	public String addParentClass(LandscapeClassObject lco) 
	{
		int	i;
		LandscapeClassObject	o;

		if (lco == null) {
			return ("Can't inherit from null class");
		}

		// I already inherit from lco or some subclass of lco
		// then don't add lco to the things I inherit from

		if (subclassOf(lco)) {
			return ("Already inherits from " + lco.getId());	   
		}

		// If what I am adding is itself a subclass of something
		// I inherit from then remove those somethings.

		for (i = m_inheritsFrom.size(); --i >= 0; ) {
			o = (LandscapeClassObject) m_inheritsFrom.elementAt(i);
			if (lco.subclassOf(o)) {
				m_inheritsFrom.remove(i);
				o.removeInheritedBy(this);
		}	}

		m_inheritsFrom.addElement(lco);
		lco.addInheritedBy(this);
		return null;
	}

	public boolean processFirstOrder(String id, String value) 
	{
		String s;

		if (id.equals(CLASSLABEL_ID)) {
			s = Attribute.parseStringValue(value);
			if (s != null) {
				setLabel(s);
			}
			return true;
		}
		if (id.equals(CLASSSTYLE_ID)) {
			if (value != null) {
				setStyle(Attribute.parseIntValue(value));
			}
			return true;
		}
		if (id.equals(CLASSDESC_ID)) {
			s = Attribute.parseStringValue(value);
			if (s != null) {
				setDescription(s);
			}
			return true;
		} 
		if (id.equals(ACTIVE_ID)) {
			if (value != null) {
				int	val;
				if (value.startsWith("f")) {
					val = 0;
				} else if (value.startsWith("t")) {
					val = DIRECTION_NORMAL;
				} else {
					val = Util.parseInt(value);
				}			
				setActive(val, false);
			}
			return true;
		}
		if (id.equals(VISIBLE_ID)) {
			if (value != null) {
				int	val;
				if (value.startsWith("f")) {
					val = 0;
				} else if (value.startsWith("t")) {
					val = DIRECTION_NORMAL;
				} else {
					val = Util.parseInt(value);
				}			
				setShown(val, false);
			}
			return true;
		}
		return super.processFirstOrder(id, value);
	}

	public void reportClassAttributes(ResultBox resultBox)
	{
		resultBox.addText(CLASSLABEL_ID);
		resultBox.addText(CLASSSTYLE_ID);
		resultBox.addText(CLASSDESC_ID);
		resultBox.addText(COLOR_ID);
		resultBox.addText(LABEL_COLOR_ID);
		resultBox.addText(OPEN_COLOR_ID);
	}

	// --------------

	// Public methods

	// --------------

	public Ta getTa()
	{
		return m_ta;
	}

	public Diagram getDiagram()
	{
		return m_dg;
	}

 	public int getNid() 
	{
		return m_nid;
	}

	public void setNid(int nid) 
	{
		this.m_nid = nid;
	}

	public String getLabelId()
	{
		return  "(" + m_nid + ") " + getLabel();
	}

	public LandscapeClassObject derivedFrom(int i)
	{
		return((i < m_inheritsFrom.size()) ? (LandscapeClassObject) m_inheritsFrom.elementAt(i) : null);
	}

	// Return the attribute with the associated id
	/* This logic is flawed -- it won't find the attribute in the
	 * nearest ancestor -- only the ancestor in the nearest leftmost ancestor
	 * IJD
	 */

	public Attribute getLsAttribute(String id) 
	{
		Attribute attr = (Attribute) super.getLsAttribute(id);

		if (attr != null) {
			return attr;
		}

		Enumeration			 en;
		LandscapeClassObject lco;

		for (en = enumInheritsFrom(); en.hasMoreElements(); ) {
			lco = (LandscapeClassObject) en.nextElement();
			attr = lco.getLsAttribute(id);
			if (attr != null) {
				return attr;
		}	}
		return null;
	}

	public String writeAttributes(PrintWriter ps, String nodeId)
	{
		nodeId = super.writeAttributes(ps, nodeId, null, true);
	
		String label = getLabel();

		if (!getId().equals(label)) {
			nodeId = writeAttribute(ps, nodeId, CLASSLABEL_ID, qt(label));
		}

		int style = getStyle();

		if (style >= 0) {
			nodeId = writeAttribute(ps, nodeId, CLASSSTYLE_ID, style);
		}

		String description = getDescription();

		if (description != null) {
			nodeId = writeAttribute(ps, nodeId, CLASSDESC_ID, qt(description));
		}

		if (m_active != DIRECTION_NORMAL) {
			nodeId = writeAttribute(ps, nodeId, ACTIVE_ID, m_active);
		}
		if (m_shown != DIRECTION_NORMAL) {
			nodeId = writeAttribute(ps, nodeId, VISIBLE_ID, m_shown);
		}
		return nodeId;
	}
}

