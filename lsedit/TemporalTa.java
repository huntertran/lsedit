package lsedit;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import java.util.Enumeration;
import java.util.Vector;

import java.io.PrintWriter;

import javax.swing.undo.UndoableEdit;

class SetParentClass extends MyUndoableEdit implements UndoableEdit
{
	LandscapeObject		 m_object;
	LandscapeClassObject m_old;
	LandscapeClassObject m_new;

	SetParentClass(LandscapeObject object, LandscapeClassObject old, LandscapeClassObject value)
	{
		m_object = object;
		m_old    = old;
		m_new    = value;
	}
	
	public void dumpEdit(PrintWriter ps)
	{
		ps.println(m_object.getId() + " { action=SetParentClass Bclass=" + m_old.getId() + " Aclass=" + m_new.getId() + "}" );
	}	

	public String getPresentationName() 
	{
		return m_object + " ParentClass " + m_new;
	}

	protected void changeTo(LandscapeClassObject value)
	{
		LandscapeObject object = m_object;

		getDiagram(object).setParentClass(object, value);
	}

	public void undo()
	{
		changeTo(m_old);
	}

	public void redo()
	{
		changeTo(m_new);
	}
}	

class SetStyle extends MyUndoableEdit implements UndoableEdit
{
	LandscapeObject	m_object;
	int             m_old;
	int             m_new;

	SetStyle(LandscapeObject object, int old, int value)
	{
		m_object = object;
		m_old    = old;
		m_new    = value;
	}

	public String getPresentationName() 
	{
		return m_object + " Style " + m_object.getStyleName(m_new);
	}

	protected void changeTo(int value)
	{
		LandscapeObject object = m_object;

		getDiagram(object).setStyle(object, value);
	}

	public void undo()
	{
		changeTo(m_old);
	}

	public void redo()
	{
		changeTo(m_new);
}	}

class SetObjectColor extends MyPaintableUndoableEdit implements UndoableEdit
{
	LandscapeObject	m_object;
	Color           m_old;
	Color           m_new;

	SetObjectColor(LandscapeObject object, Color old, Color value)
	{
		m_object = object;
		m_old    = old;
		m_new    = value;
	}

	public String getPresentationName() 
	{
		return m_object + " ObjectColor ";
	}

	protected void changeTo(Color value)
	{
		LandscapeObject object = m_object;

		getDiagram(object).setObjectColor(object, value);
	}

	public void undo()
	{
		changeTo(m_old);
	}

	public void redo()
	{
		changeTo(m_new);
	}

	public void paintComponent(Graphics g, int x, int y)
	{
		paintComponentColor(g, x, y, m_new);
	}

	public int getPreferredWidth()
	{
		return(getPreferredWidthColor(m_object));
	}
}

class SetAttributeName extends MyUndoableEdit implements UndoableEdit
{
	LandscapeObject	m_object;
	String			m_oldName;
	String			m_newName;

	SetAttributeName(LandscapeObject object, String oldName, String newName)
	{
		m_object    = object;
		m_oldName   = oldName;
		m_newName   = newName;
	}

	public String getPresentationName() 
	{
		LandscapeObject object = m_object;

		if (m_newName == null) {
			return object + " delete Attribute " + m_oldName;
		}
		if (m_oldName == null) {
			return object + " create Attribute " + m_newName;
		}
		return object + " rename Attribute " + m_oldName + " to " + m_newName;
	}

	protected void changeTo(String old, String value)
	{
		LandscapeObject object = m_object;

		getDiagram(object).setAttributeName(object, old, value);
	}

	public void undo()
	{
		changeTo(m_newName, m_oldName);
	}

	public void redo()
	{
		changeTo(m_oldName, m_newName);
}	}

class SetAttributeValue extends MyUndoableEdit implements UndoableEdit
{
	LandscapeObject		m_object;
	String				m_id;
	String				m_oldValue;
	String				m_newValue;

	SetAttributeValue(LandscapeObject object, String id, String oldValue, String newValue)
	{
		m_object    = object;
		m_id        = id;
		m_oldValue  = oldValue;
		m_newValue  = newValue;
	}

	public String getPresentationName() 
	{
		LandscapeObject object = m_object;

		if (m_newValue == null) {
			return object + " remove Attribute " + m_id + " value";
		}
		if (m_oldValue == null) {
			return object + " add Attribute " + m_id + " value";
		}
		return object + " replace Attribute " +  m_id + " value";
	}

	protected void changeTo(String value)
	{
		LandscapeObject object = m_object;

		getDiagram(object).addAttribute(object, m_id, value);
	}

	public void undo()
	{
		changeTo(m_oldValue);
	}

	public void redo()
	{
		changeTo(m_newValue);
}	}

class NewRelation extends MyUndoableEdit implements UndoableEdit
{
	RelationInstance m_ri;

	NewRelation(RelationInstance ri)
	{
		m_ri = ri;
	}

	public String getPresentationName() 
	{
		return "Create relation " + m_ri;
	}

	public void undo()
	{
		RelationInstance ri = m_ri;

		getDiagram(ri).cutRelation(ri);
	}

	public void redo()
	{
		RelationInstance ri = m_ri;

		getDiagram(ri).pasteRelation(ri);
	}
}	

class DeleteEdge extends MyUndoableEdit implements UndoableEdit
{
	RelationInstance	m_ri;

	DeleteEdge(RelationInstance ri)
	{
		m_ri = ri;
	}

	public String getPresentationName() 
	{
		return "Delete " + m_ri.toString();
	}

	public void undo()
	{
		RelationInstance ri = m_ri;

		getDiagram(ri).pasteRelation(ri);
	}

	public void redo()
	{
		RelationInstance ri = m_ri;

		getDiagram(ri).cutRelation(ri);
	}
}	

class SetLabel extends MyUndoableEdit implements UndoableEdit
{
	LandscapeObject3D	m_object;
	String				m_old;
	String				m_new;

	SetLabel(LandscapeObject3D object, String old,String value)
	{
		m_object = object;
		m_old    = old;
		m_new    = value;
	}

	public String getPresentationName() 
	{
		return m_object + " Label " + m_new;
	}

	protected void changeTo(String value)
	{
		LandscapeObject3D object = m_object;

		getDiagram(object).setLabel(object, value);
	}

	public void undo()
	{
		changeTo(m_old);
	}

	public void redo()
	{
		changeTo(m_new);
	}
}	

class SetReversedLabel extends MyUndoableEdit implements UndoableEdit
{
	RelationClass	m_object;
	String			m_old;
	String			m_new;

	SetReversedLabel(RelationClass object, String old,String value)
	{
		m_object = object;
		m_old    = old;
		m_new    = value;
	}

	public String getPresentationName() 
	{
		return m_object + " Label " + m_new;
	}

	protected void changeTo(String value)
	{
		RelationClass object = m_object;

		getDiagram(object).setReversedLabel(object, value);
	}

	public void undo()
	{
		changeTo(m_old);
	}

	public void redo()
	{
		changeTo(m_new);
	}
}	

class SetDescription extends MyUndoableEdit implements UndoableEdit
{
	LandscapeObject3D	m_object;
	String				m_old;
	String				m_new;

	SetDescription(LandscapeObject3D object, String old, String value)
	{
		m_object = object;
		m_old    = old;
		m_new    = value;
	}

	public String getPresentationName() 
	{
		return m_object + " Description";
	}

	protected void changeTo(String value)
	{
		LandscapeObject3D object = m_object;

		getDiagram(object).setDescription(object, value);
	}

	public void undo()
	{
		changeTo(m_old);
	}

	public void redo()
	{
		changeTo(m_new);
	}
}	

class SetLabelColor extends MyPaintableUndoableEdit implements UndoableEdit
{
	LandscapeObject3D	m_object;
	Color               m_old;
	Color               m_new;

	SetLabelColor(LandscapeObject3D object, Color old, Color value)
	{
		m_object = object;
		m_old    = old;
		m_new    = value;
	}

	public String getPresentationName() 
	{
		return m_object + " LabelColor ";
	}

	protected void changeTo(Color value)
	{
		LandscapeObject3D object = m_object;

		getDiagram(object).setLabelColor(object, value);
	}

	public void undo()
	{
		changeTo(m_old);
	}

	public void redo()
	{
		changeTo(m_new);
	}

	public void paintComponent(Graphics g, int x, int y)
	{
		paintComponentColor(g, x, y, m_new);
	}

	public int getPreferredWidth()
	{
		return(getPreferredWidthColor(m_object));
	}
}

class SetColorWhenOpen extends MyPaintableUndoableEdit implements UndoableEdit
{
	LandscapeObject3D	m_object;
	Color               m_old;
	Color               m_new;

	SetColorWhenOpen(LandscapeObject3D object, Color old, Color value)
	{
		m_object = object;
		m_old    = old;
		m_new    = value;
	}

	public String getPresentationName() 
	{
		return m_object + " ColorWhenOpen ";
	}

	protected void changeTo(Color value)
	{
		LandscapeObject3D object = m_object;

		getDiagram(object).setColorWhenOpen(object, value);
	}

	public void undo()
	{
		changeTo(m_old);
	}

	public void redo()
	{
		changeTo(m_new);
	}

	public void paintComponent(Graphics g, int x, int y)
	{
		paintComponentColor(g, x, y, m_new);
	}

	public int getPreferredWidth()
	{
		return(getPreferredWidthColor(m_object));
	}
}

class NewEntity extends MyUndoableEdit implements UndoableEdit
{
	EntityInstance		 m_container;
	EntityInstance		 m_e;

	NewEntity(EntityInstance container, EntityInstance e)
	{
		m_container = container;
		m_e         = e;
	}

	public void dumpEdit(PrintWriter ps)
	{
		ps.println(m_e.getId() + " { action=NewEntity Acontainer=" + m_container.getId() + "}" );
	}	

	public String getPresentationName() 
	{
		return "Create entity " + m_e;
	}

	public void undo()
	{
		EntityInstance e = m_e;

		getDiagram(e).cutEntity(e);
	}

	public void redo()
	{
		EntityInstance e = m_e;

		getDiagram(e).pasteEntity(m_container, e);
	}
}

class SetFontDelta extends MyUndoableEdit implements UndoableEdit
{
	EntityInstance	m_e;
	int             m_old;
	int             m_new;

	SetFontDelta(EntityInstance e, int old, int value)
	{
		m_e   = e;
		m_old = old;
		m_new = value;
	}

	public String getPresentationName() 
	{
		return m_e.toString() + " FontDelta " + m_new;
	}

	protected void changeTo(int value)
	{
		EntityInstance e = m_e;

		getDiagram(e).setFontDelta(e, value);
	}

	public void undo()
	{
		changeTo(m_old);
	}

	public void redo()
	{
		changeTo(m_new);
}	}

class SetXRelLocal extends MyUndoableEdit implements UndoableEdit
{
	EntityInstance	m_e;
	double          m_old;
	double          m_new;

	SetXRelLocal(EntityInstance e, double old, double value)
	{
		m_e   = e;
		m_old = old;
		m_new = value;
	}	

	public String getPresentationName() 
	{
		return m_e.toString() + " xRelLocal " + Util.formatFraction(m_new);
	}

	protected void changeTo(double value)
	{
		EntityInstance e = m_e;

		getDiagram(e).setXRelLocal(e, value);
	}

	public void undo()
	{
		changeTo(m_old);
	}

	public void redo()
	{
		changeTo(m_new);
}	}

class SetYRelLocal extends MyUndoableEdit implements UndoableEdit
{
	EntityInstance	m_e;
	double          m_old;
	double          m_new;

	SetYRelLocal(EntityInstance e, double old, double value)
	{
		m_e   = e;
		m_old = old;
		m_new = value;
	}

	public String getPresentationName() 
	{
		return m_e.toString() + " yRelLocal " + Util.formatFraction(m_new);
	}

	protected void changeTo(double value)
	{
		EntityInstance e = m_e;

		getDiagram(e).setYRelLocal(e, value);
	}

	public void undo()
	{
		changeTo(m_old);
	}

	public void redo()
	{
		changeTo(m_new);
}	}

class SetWidthRelLocal extends MyUndoableEdit implements UndoableEdit
{
	EntityInstance	m_e;
	double          m_old;
	double          m_new;

	SetWidthRelLocal(EntityInstance e, double old, double value)
	{
		m_e   = e;
		m_old = old;
		m_new = value;
	}

	public String getPresentationName() 
	{
		return m_e.toString() + " widthRelLocal " + Util.formatFraction(m_new);
	}

	protected void changeTo(double value)
	{
		EntityInstance e = m_e;

		getDiagram(e).setWidthRelLocal(e, value);
	}

	public void undo()
	{
		changeTo(m_old);
	}

	public void redo()
	{
		changeTo(m_new);
}	}
	
class SetHeightRelLocal extends MyUndoableEdit implements UndoableEdit
{
	EntityInstance	m_e;
	double          m_old;
	double          m_new;

	SetHeightRelLocal(EntityInstance e, double old, double value)
	{
		m_e   = e;
		m_old = old;
		m_new = value;
	}

	public String getPresentationName() 
	{
		return m_e.toString() + " heightRelLocal " + Util.formatFraction(m_new);
	}

	protected void changeTo(double value)
	{
		EntityInstance e = m_e;

		getDiagram(e).setHeightRelLocal(e, value);
	}

	public void undo()
	{
		changeTo(m_old);
	}

	public void redo()
	{
		changeTo(m_new);
}	}

class SetSizeRelLocal extends MyUndoableEdit implements UndoableEdit
{
	EntityInstance	m_e;
	double	        m_oldWidthRelLocal, m_oldHeightRelLocal, m_newWidthRelLocal, m_newHeightRelLocal;

	SetSizeRelLocal(EntityInstance e, double oldWidthRelLocal, double oldHeightRelLocal, double newWidthRelLocal, double newHeightRelLocal)
	{
		m_e                 = e;
		m_oldWidthRelLocal  = oldWidthRelLocal;
		m_oldHeightRelLocal = oldHeightRelLocal;
		m_newWidthRelLocal  = newWidthRelLocal;
		m_newHeightRelLocal = newHeightRelLocal;
	}

	public String getPresentationName() 
	{
		return m_e.toString() + " RelSize " + Util.formatFraction(m_newWidthRelLocal) + "x" + Util.formatFraction(m_newHeightRelLocal);
	}

	protected void changeTo(double widthRelLocal, double heightRelLocal)
	{
		EntityInstance e = m_e;

		getDiagram(e).setSizeRelLocal(e, widthRelLocal, heightRelLocal);
	}

	public void undo()
	{
		changeTo(m_oldWidthRelLocal, m_oldHeightRelLocal);
	}

	public void redo()
	{
		changeTo(m_newWidthRelLocal, m_newHeightRelLocal);
}	}

class SetLocationRelLocal extends MyUndoableEdit implements UndoableEdit
{
	EntityInstance	m_e;
	double	        m_oldXRelLocal, m_oldYRelLocal, m_newXRelLocal, m_newYRelLocal;

	SetLocationRelLocal(EntityInstance e, double oldXRelLocal, double oldYRelLocal, double newXRelLocal, double newYRelLocal)
	{
		m_e            = e;
		m_oldXRelLocal = oldXRelLocal;
		m_oldYRelLocal = oldYRelLocal;
		m_newXRelLocal = newXRelLocal;
		m_newYRelLocal = newYRelLocal;
	}

	public String getPresentationName() 
	{
		return m_e.toString() + " RelLocation " + Util.formatFraction(m_newXRelLocal) + "x" + Util.formatFraction(m_newYRelLocal);
	}

	protected void changeTo(double xRelLocal, double yRelLocal)
	{
		EntityInstance e = m_e;

		getDiagram(e).setLocationRelLocal(e, xRelLocal, yRelLocal);
	}

	public void undo()
	{
		changeTo(m_oldXRelLocal, m_oldYRelLocal);
	}

	public void redo()
	{
		changeTo(m_newXRelLocal, m_newYRelLocal);
}	}

class SetRelLocal extends MyUndoableEdit implements UndoableEdit
{
	EntityInstance	m_e;
	double          m_oldX, m_oldY, m_oldWidth, m_oldHeight;
	double          m_newX, m_newY, m_newWidth, m_newHeight;

	SetRelLocal(EntityInstance e, double oldX, double oldY, double oldWidth, double oldHeight, double newX, double newY, double newWidth, double newHeight)
	{
		m_e         = e;
		m_oldX      = oldX;
		m_oldY      = oldY;
		m_oldWidth  = oldWidth;
		m_oldHeight = oldHeight;
		m_newX      = newX;
		m_newY      = newY;
		m_newWidth  = newWidth;
		m_newHeight = newHeight;
	}

	public String getPresentationName() 
	{
		return m_e.toString() + " RelLocal {" + Util.formatFraction(m_newX) + "x" + Util.formatFraction(m_newY) + "," + Util.formatFraction(m_newWidth) + "x" + Util.formatFraction(m_newHeight) + "}";
	}

	protected void changeTo(double x, double y, double width, double height)
	{
		EntityInstance e = m_e;

		getDiagram(e).setRelLocal(e, x, y, width, height);
	}


	public void undo()
	{
		changeTo(m_oldX, m_oldY, m_oldWidth, m_oldHeight);
	}

	public void redo()
	{
		changeTo(m_newX, m_newY, m_newWidth, m_newHeight);
}	}

class LiftEntityEdges extends MyUndoableEdit implements UndoableEdit
{
	EntityInstance	m_e;
	Vector			m_srcRelList;	// Null if empty 
	Vector			m_dstRelList;	// Null if empty

	LiftEntityEdges(EntityInstance e)
	{
		Vector				relList, v1;
		int					i;
		RelationInstance	ri;
		RelationClass		rc;

		// Remember all the relations that will be lifted */

		m_e     = e;
		v1      = null;
		relList = e.getSrcRelList();

		if (relList != null) {
			for (i = relList.size(); --i >= 0; ) {
				ri = (RelationInstance) relList.elementAt(i);
				rc = ri.getRelationClass();
				if (rc.isActive() && !ri.isMarked(RelationInstance.SPANNING_MARK)) {
					if (v1 == null) {
						v1 = new Vector();
					}
					v1.add(ri);
		}	}	}
		m_srcRelList = v1;

		v1      = null;
		relList = e.getDstRelList();

		if (relList != null) {
			for (i = relList.size(); --i >= 0; ) {
				ri = (RelationInstance) relList.elementAt(i);
				rc = ri.getRelationClass();
				if (rc.isActive() && !ri.isMarked(RelationInstance.SPANNING_MARK)) {
					if (v1 == null) {
						v1 = new Vector();
					}
					v1.add(ri);
		}	}	}
		m_dstRelList = v1;
	}

	public String getPresentationName() 
	{
		return m_e + " Lifted";
	}

	public void undo()
	{
		EntityInstance		e             = m_e;
		Diagram				diagram       = getDiagram(e);
		Vector				srcRelList    = m_srcRelList;
		Vector				dstRelList    = m_dstRelList;

		int					i;
		RelationInstance	ri;

		if (srcRelList != null) {
			for (i = srcRelList.size(); --i >= 0; ) {
				ri = (RelationInstance) srcRelList.elementAt(i);
				if (ri.getSrc() == e) {
					diagram.pasteRelation(ri);
				} else {
					diagram.moveRelationSrc(ri, e);
		}	}	}
			
		if (dstRelList != null) {
			for (i = dstRelList.size(); --i >= 0; ) {
				ri = (RelationInstance) dstRelList.elementAt(i);
				if (ri.getDst() == e) {
					diagram.pasteRelation(ri);
				} else {
					diagram.moveRelationDst(ri, e);
		}	}	}
	}

	// Can't simply invoke liftRelations because the active flags
	// may have changed

	public void redo()
	{
		EntityInstance		e             = m_e;
		Diagram				diagram       = getDiagram(e);
		Vector				srcRelList    = m_srcRelList;
		Vector				dstRelList    = m_dstRelList;
		EntityInstance		parent        = e.getContainedBy();

		int					i;
		RelationInstance	ri;
		RelationClass		rc;

		if (parent == diagram.getRootInstance()) {
			parent = null;
		}

		if (dstRelList != null) {
			for (i = dstRelList.size(); --i >= 0; ) {
				ri = (RelationInstance) dstRelList.elementAt(i);
				if (parent == null || ri.getSrc() == parent) {
					diagram.cutRelation(ri);
				} else {
					diagram.moveRelationDst(ri, parent);
		}	}	}

		if (srcRelList != null) {
			for (i = srcRelList.size(); --i >= 0; ) {
				ri = (RelationInstance) srcRelList.elementAt(i);
				if (parent == null || ri.getDst() == parent) {
					diagram.cutRelation(ri);
				} else {
					diagram.moveRelationSrc(ri, parent);
		}	}	}
	}
}

class CutEntity extends MyUndoableEdit implements UndoableEdit
{
	EntityInstance m_e;

	CutEntity(EntityInstance e)
	{
		m_e = e;
	}
	
	public void dumpEdit(PrintWriter ps)
	{
		ps.println(m_e.getId() + " { action=CutEntity }" );
	}	

	public String getPresentationName() 
	{
		return m_e + " Deleted";
	}

	public void undo()
	{
		EntityInstance e = m_e;

		getDiagram(e).pasteEntity(null, e);
	}

	public void redo()
	{
		EntityInstance e = m_e;

		getDiagram(e).cutEntity(e);
}	}

class MoveEntityContainment extends MyUndoableEdit implements UndoableEdit
{
	EntityInstance	m_e;
	EntityInstance	m_containedByOld;
	EntityInstance	m_containedByNew;

	MoveEntityContainment(EntityInstance e, EntityInstance oldContainer, EntityInstance newContainer)
	{
		m_e              = e;
		m_containedByOld = oldContainer;
		m_containedByNew = newContainer;
	}

	public void dumpEdit(PrintWriter ps)
	{
		ps.println(m_e.getId() + " { action=MoveEntityContainment Bcontainer=" + m_containedByOld.getId() + " Acontainer=" + m_containedByNew.getId() + "}" );
	}	
	
	public String getPresentationName() 
	{
		return m_e + " moved from " + m_containedByOld + " to " + m_containedByNew;
	}

	protected void changeTo(EntityInstance value)
	{
		EntityInstance e = m_e;

		getDiagram(e).moveEntityContainment(value, e);
	}

	public void undo()
	{
		changeTo(m_containedByOld);
	}

	public void redo()
	{
		changeTo(m_containedByNew);
}	}

class UpdateDeleteContainer extends MyUndoableEdit implements UndoableEdit
{
	EntityInstance		m_e;

	UpdateDeleteContainer(EntityInstance e) 
	{
		m_e = e;
	}
	
	public void dumpEdit(PrintWriter ps)
	{
		ps.println(m_e.getId() + " { action=UpdateDeleteContainer }" );
	}	

	public String getPresentationName() 
	{
		return "Deleting container " + m_e;
	}

	public void undo()
	{
		EntityInstance e = m_e;

		getDiagram(e).undeleteContainer(e);
	}

	public void redo()
	{
		EntityInstance e = m_e;

		getDiagram(e).deleteContainer(e);
	}
}

class ClusterEntity extends MyUndoableEdit implements UndoableEdit
{
	EntityInstance		 m_container;
	EntityInstance		 m_e;

	ClusterEntity(EntityInstance container, EntityInstance e)
	{
		m_container = container;
		m_e         = e;
	}
	
	public void dumpEdit(PrintWriter ps)
	{
		ps.println(m_e.getId() + " { action=ClusterEntity Aclass=" + m_container.getId() + " }" );
	}	

	public String getPresentationName() 
	{
		return "Create cluster " + m_e;
	}

	public void undo()
	{
		EntityInstance e = m_e;

		getDiagram(e).cutEntity(e);
	}

	public void redo()
	{
		EntityInstance e = m_e;

		getDiagram(e).pasteEntity(m_container, e);
	}
}	

class SetInherits extends MyUndoableEdit implements UndoableEdit
{
	LandscapeClassObject m_object;
	Vector               m_old;
	Vector               m_new;

	SetInherits(LandscapeClassObject object, Vector old, Vector value)
	{
		m_object = object;
		m_old    = old;
		m_new    = value;
	}

	public String getPresentationName() 
	{
		return m_object.getLabel() + " inheritance";
	}

	protected void changeTo(Vector value)
	{
		LandscapeClassObject object = m_object;

		getDiagram(object).setInherits(object, value);
	}

	public void undo()
	{
		changeTo(m_old);
	}

	public void redo()
	{
		changeTo(m_new);
}	}

class NewEntityClass extends MyUndoableEdit implements UndoableEdit
{
	EntityClass	m_ec;
	String		m_id;
	EntityClass	m_baseClass;

	NewEntityClass(EntityClass ec, String id, EntityClass baseClass)
	{
		m_ec        = ec;
		m_id        = id;
		m_baseClass = baseClass;
	}

	public String getPresentationName() 
	{
		return m_ec + " created";
	}

	public void undo()
	{
		EntityClass ec = m_ec;

		getDiagram(ec).removeEntityClass(ec);
	}

	public void redo()
	{
		m_ec = getDiagram(m_ec).newEntityClass(m_id, m_baseClass);
}	}

class RemoveEntityClass extends MyUndoableEdit implements UndoableEdit
{
	EntityClass	m_ec, m_oldDefaultEntityClass;
	Vector		m_eps;

					 
	RemoveEntityClass(EntityClass ec, Vector eps, EntityClass defaultEntityClass)
	{
		m_ec                    = ec;
		m_eps                   = eps;
		m_oldDefaultEntityClass = defaultEntityClass;
	}

	public String getPresentationName() 
	{
		return " Remove entity class " + m_ec.getLabel();
	}

	public void undo()
	{
		EntityClass ec = m_ec;

		getDiagram(ec).unRemoveEntityClass(ec, m_eps, m_oldDefaultEntityClass);
	}

	public void redo()
	{
		EntityClass ec = m_ec;

		getDiagram(ec).removeEntityClass(ec);
	}
}	

class SetImage extends MyUndoableEdit implements UndoableEdit
{
	EntityClass	m_ec;
	int         m_old;
	int         m_new;

	SetImage(EntityClass ec, int old, int value)
	{
		m_ec  = ec;
		m_old = old;
		m_new = value;
	}

	public String getPresentationName() 
	{
		return m_ec + " Change Image";
	}

	protected void changeTo(int value)
	{
		EntityClass ec = m_ec;

		getDiagram(ec).setImage(ec, value);
	}

	public void undo()
	{
		changeTo(m_old);
	}

	public void redo()
	{
		changeTo(m_new);
}	}

class SetAngle extends MyUndoableEdit implements UndoableEdit
{
	EntityClass	m_ec;
	double      m_old;
	double      m_new;

	SetAngle(EntityClass ec, double old, double value)
	{
		m_ec  = ec;
		m_old = old;
		m_new = value;
	}	

	public String getPresentationName() 
	{
		return m_ec + " Angle " + m_new;
	}

	protected void changeTo(double value)
	{
		EntityClass ec = m_ec;

		getDiagram(ec).setAngle(ec, value);
	}

	public void undo()
	{
		changeTo(m_old);
	}

	public void redo()
	{
		changeTo(m_new);
}	}

class SetUnscaledIconFile extends MyUndoableEdit implements UndoableEdit
{
	EntityClass m_ec;
	String		m_old;
	String		m_new;

	SetUnscaledIconFile(EntityClass ec, String old, String value)
	{
		m_ec = ec;
		m_old = old;
		m_new = value;
	}

	public String getPresentationName()
	{
		return m_ec + " Icon " + m_new;
	}

	protected void changeTo(String value)
	{
		EntityClass ec = m_ec;

		getDiagram(ec).setUnscaledIconFile(ec, value);
	}

	public void undo()
	{
		changeTo(m_old);
	}

	public void redo()
	{
		changeTo(m_new);
	}
}

class NewRelationClass extends MyUndoableEdit implements UndoableEdit
{
	RelationClass	m_rc;
	String			m_id;
	RelationClass	m_baseClass;

	NewRelationClass(RelationClass rc, String id, RelationClass baseClass)
	{
		m_rc        = rc;
		m_id        = id;
		m_baseClass = baseClass;
	}

	public String getPresentationName() 
	{
		return m_rc + " created";
	}

	public void undo()
	{
		getDiagram(m_rc).removeRelationClass(m_rc);
	}

	public void redo()
	{
		m_rc = getDiagram(m_rc).newRelationClass(m_id, m_baseClass);
}	}

class RemoveRelationClass extends MyUndoableEdit implements UndoableEdit
{
	RelationClass	m_rc, m_default;

	RemoveRelationClass(RelationClass rc, RelationClass defaultRelationClass)
	{
		m_rc      = rc;
		m_default = defaultRelationClass;
	}	

	public String getPresentationName() 
	{
		return " Remove relation class " + m_rc.getLabel();
	}

	public void undo()
	{
		RelationClass rc = m_rc;

		getDiagram(rc).unRemoveRelationClass(rc, m_default);
	}

	public void redo()
	{
		RelationClass rc = m_rc;

		getDiagram(rc).removeRelationClass(rc);
	}
}	

class SetArrowColor extends MyUndoableEdit implements UndoableEdit
{
	RelationClass	m_rc;
	Color			m_old;
	Color			m_new;

	SetArrowColor(RelationClass rc, Color old, Color value)
	{
		m_rc  = rc;
		m_old = old;
		m_new = value;
	}

	public String getPresentationName() 
	{
		return m_rc + " Change arrow color";
	}

	protected void changeTo(Color value)
	{
		RelationClass rc = m_rc;

		getDiagram(rc).setArrowColor(rc, value);
	}

	public void undo()
	{
		changeTo(m_old);
	}

	public void redo()
	{
		changeTo(m_new);
}	}

class SetIOfactor extends MyUndoableEdit implements UndoableEdit
{
	RelationClass	m_rc;
	short			m_old;
	short			m_new;

	SetIOfactor(RelationClass rc, short old, short value)
	{
		m_rc  = rc;
		m_old = old;
		m_new = value;
	}

	public String getPresentationName() 
	{
		return m_rc + " Change IO factor";
	}

	protected void changeTo(short value)
	{
		RelationClass rc = m_rc;

		getDiagram(rc).setIOfactor(rc, value);
	}

	public void undo()
	{
		changeTo(m_old);
	}

	public void redo()
	{
		changeTo(m_new);
}	}

class SwitchContainsClasses extends MyUndoableEdit implements UndoableEdit
{
	RelationClass[]		m_oldContainsClasses;
	RelationClass[]		m_newContainsClasses;

	SwitchContainsClasses(RelationClass[] newContainsClasses, RelationClass[] oldContainsClasses) 
	{
		m_oldContainsClasses = oldContainsClasses;
		m_newContainsClasses = newContainsClasses;
	}

	public String getPresentationName() 
	{
		String	ret;
		int		i;
		
		ret  = "Switch hierarchy ";
		if (m_oldContainsClasses != null) {
			ret += "from ";
			if (m_oldContainsClasses.length == 0) {
				ret += "no hierarchy";
			} else {
				ret +=	m_oldContainsClasses[0].getLabel();
				if (m_oldContainsClasses.length > 1) {
					ret += " ... ";
		}	}	}
		if (m_newContainsClasses != null) {
			ret += "to ";
			if (m_newContainsClasses.length == 0) {
				ret += "no hierarchy";
			} else {
				ret +=	m_newContainsClasses[0].getLabel();
				if (m_newContainsClasses.length > 1) {
					ret += " ... ";
		}	}	}
		return ret;
	}

	protected void changeContainsClasses(RelationClass[] containsClasses)
	{
		Diagram	diagram = containsClasses[0].getDiagram();
		
		diagram.changeContainsClasses(containsClasses);
	}

	public void undo()
	{
		changeContainsClasses(m_oldContainsClasses);
	}

	public void redo()
	{
		changeContainsClasses(m_newContainsClasses);
	}
}
	
class CutClipboard extends MyUndoableEdit implements UndoableEdit
{
	TemporalTa	m_diagram;
	Clipboard	m_old_clipboard;
	Clipboard	m_new_clipboard;

	CutClipboard(TemporalTa diagram, Clipboard old_clipboard, Clipboard new_clipboard)
	{
		m_diagram       = diagram;
		m_old_clipboard = old_clipboard;
		m_new_clipboard = new_clipboard;
	}
	
	public void dumpEdit(PrintWriter ps)
	{
		Clipboard		clipboard = m_new_clipboard;
		int				i;
		EntityInstance	e;

		for (i = clipboard.size(); i > 0; ) {
			// Paste ancestors then children
			e = (EntityInstance) clipboard.elementAt(--i);
			ps.println(e.getId() + " { action=CutClipboard }" );
		}
	}	

	public String getPresentationName() 
	{
		if (m_new_clipboard.getExtendsClipboard() == null) {
			return "Cut " + m_new_clipboard;
		}
		return "Cut additional";
	}

	public void undo()
	{
		TemporalTa		diagram   = m_diagram;
		Clipboard		clipboard = m_new_clipboard;
		int				i;
		EntityInstance	e;

		for (i = clipboard.size(); i > 0; ) {
			// Paste ancestors then children
			e = (EntityInstance) clipboard.elementAt(--i);
			diagram.pasteEntity(null, e);
		}
		diagram.setClipboard(m_old_clipboard);
	}

	public void redo()
	{
		m_diagram.cutClipboard(m_new_clipboard);
	}
}	

class PasteClipboard extends MyUndoableEdit implements UndoableEdit
{
	TemporalTa			m_diagram;
	Clipboard			m_saved_clipboard;
	EntityInstance		m_container;

	PasteClipboard(TemporalTa diagram, Clipboard clipboard, EntityInstance container)
	{
		m_diagram         = diagram;
		m_saved_clipboard = clipboard;
		m_container       = container;
	}

	public void dumpEdit(PrintWriter ps)
	{
		Clipboard		clipboard = m_saved_clipboard;
		String			container = LandscapeObject.qt(m_container.getId());
		int				i;
		EntityInstance	e;

		for (i = clipboard.size(); i > 0; ) {
			// Paste ancestors then children
			e = (EntityInstance) clipboard.elementAt(--i);
			ps.println(e.getId() + " { action=PasteClipboard Acontainer=" +  container + " }" );
		}
	}	
	
	public String getPresentationName() 
	{
		return "Paste";
	}

	public void undo()
	{
		m_diagram.unPasteClipboard(m_saved_clipboard);
	}

	public void redo()
	{
		m_diagram.pasteClipboard(m_saved_clipboard, m_container);
	}
}	

/* This class extends UndoableTa with the ability to actually update the TA
 * It knows about the diagram
 */

public class TemporalTa extends UndoableTa 
{
	public TemporalTa(TaFeedback taFeedback)
	{
		super(taFeedback);
	}

	// ----------------------------
	// Updates for LandscapeObjects 
	// ----------------------------

	/* Update entityInstance or relationInstance class */

	public void updateParentClass(LandscapeObject object, LandscapeClassObject value)
	{
		LandscapeClassObject old = object.getParentClass();

		if (value != old) {
			setParentClass(object, value);
			if (undoEnabled()) {
				logEdit(new SetParentClass(object, old, value));
	}	}	}

	public void updateStyle(LandscapeObject object, int value)
	{
		int old = object.getStyle();

		if (value != old) {
			setStyle(object, value);
			if (undoEnabled()) {
				logEdit(new SetStyle(object, old, value));
	}	}	}

	public void updateObjectColor(LandscapeObject object, Color value)
	{
		Color old = object.getObjectColor();

		if (value == null) {
			if (old == null) {
				return;
			}
		} else if (value.equals(old)) {
			return;
		}
		setObjectColor(object, value);
		if (undoEnabled()) {
			logEdit(new SetObjectColor(object, old, value));
	}	}

	public boolean updateAttributeNameAt(LandscapeObject object, int index, Object value)
	{
		Attribute	attribute;
		String		oldname;
		String		name;
		int			primary;

		primary = object.getPrimaryAttributeCount();

		if (index < primary) {
			// Can't change names of first order attributes
			return(false);
		}
		index -= primary;

		name = StringCache.get((String) value);
		if (index == object.getLsAttributesSize()) {
			if (name == null || name.equals("")) {
				// Dummy row hasn't changed
				return(false);
			}
			if (!object.unknownAttributeName(name)) {
				return(false);
			} 
			setAttributeName(object, null, name);
			if (undoEnabled()) {
				logEdit(new SetAttributeName(object, null, name));
			}
			return(true);
		}

		attribute = object.getLsAttributeAt(index);
		if (attribute == null) {
			return(false);
		}
		oldname = attribute.m_id;

		if (name.equals("")) {
			// Remove this attribute
			setAttributeName(object, oldname, null);
			if (undoEnabled()) {
				logEdit(new SetAttributeName(object, oldname, null));
			}
			return(true);
		}
		if (name.equals(oldname)) {
			return(false);
		}
		if (!object.unknownAttributeName(name)) {
			return(false);
		} 
		setAttributeName(object, oldname, name);
		if (undoEnabled()) {
			logEdit(new SetAttributeName(object, oldname, name));
		}
		return(true);
	}

	public void updateAttributeValueAt(LandscapeObject object, int index, Object value)
	{
		Attribute	attr;
		String		id;
		String		oldAvi;
		String		newAvi;

		index -= object.getPrimaryAttributeCount();
		attr   = object.getLsAttributeAt(index);
		if (attr == null) {
			return;
		}
		oldAvi = attr.externalString();
		newAvi = (String) value;
		if (newAvi != null) {
			newAvi = StringCache.get(newAvi.trim());
			if (newAvi.length() == 0) {
				newAvi = null;
		}	}

		if (newAvi == null) {
			if (oldAvi == null) {
				return;
			}
		} else {
			if (newAvi.equals(oldAvi)) {
				return;
			}
			ParseAttributeValue	parseAttributeValue = new ParseAttributeValue(newAvi); 

			newAvi = parseAttributeValue.result();
			if (newAvi == null) {
				System.out.println("Can't parse '" + newAvi + "' as attribute value");
				return;
			}
			if (newAvi.equals(oldAvi)) {
				return;
		}	}
		id = attr.m_id;

		addAttribute(object, id, newAvi);
		if (undoEnabled()) {
			logEdit(new SetAttributeValue(object, id, oldAvi, newAvi));
		}	
	}

	// -----------------------------
	// Updates for relationInstances
	// -----------------------------

	// Create a new edge

	public RelationInstance updateNewRelation(RelationClass rc, EntityInstance from, EntityInstance to)
	{
		RelationInstance ri = getNewRelation(rc, from, to);
		if (undoEnabled()) {
			logEdit(new NewRelation(ri));
		}
		return ri;
	}

	public void updateDeleteEdge(RelationInstance ri)
	{
		cutRelation(ri);
		if (undoEnabled()) {
			logEdit(new DeleteEdge(ri));
	}	}

	/* Updates for LandscapeObject3D things */

	public void updateLabel(LandscapeObject3D object, String value)
	{
		String old = object.getLabel();

		if (value == null) {
			if (old == null) {
				return;
			}
		} else if (value.equals(old)) {
			return;
		}
		setLabel(object, value);
		if (undoEnabled()) {
			logEdit(new SetLabel(object, old, value));
	}	}
	
	public void updateReversedLabel(RelationClass rc, String value)
	{
		String old = rc.getReversedLabel();

		if (value == null) {
			if (old == null) {
				return;
			}
		} else if (value.equals(old)) {
			return;
		}
		setReversedLabel(rc, value);
		if (undoEnabled()) {
			logEdit(new SetReversedLabel(rc, old, value));
	}	}

	public void updateDescription(LandscapeObject3D object, String value)
	{
		String	old = object.getDescription();

		if (value == null) {
			if (old == null) {
				return;
			}
		} else if (value.equals(old)) {
			return;
		}
		setDescription(object, value);
		if (undoEnabled()) {
			logEdit(new SetDescription(object, old, value));
	}	}

	public void updateLabelColor(LandscapeObject3D object, Color value)
	{
		Color old = object.getLabelColor();

		if (value == null) {
			if (old == null) {
				return;
			}
		} else if (value.equals(old)) {
			return;
		}
		setLabelColor(object, value);
		if (undoEnabled()) {
			logEdit(new SetLabelColor(object, old, value));
	}	}

	public void updateColorWhenOpen(LandscapeObject3D object, Color value)
	{
		Color old = object.getColorWhenOpen();

		if (value == null) {
			if (old == null) {
				return;
			}
		} else if (value.equals(old)) {
			return;
		}
		setColorWhenOpen(object, value);
		if (undoEnabled()) {
			logEdit(new SetColorWhenOpen(object, old, value));
	}	}

	// ---------------------------
	// Updates for entityInstances 
	// ---------------------------

	public EntityInstance updateNewEntity(EntityClass ec, EntityInstance container)
	{
		EntityInstance	e = getNewEntity(ec, container);

		if (undoEnabled()) {
			logEdit(new NewEntity(container, e));
		}
		return(e);
	}

	public void updateFontDelta(EntityInstance e, int value)
	{
		int	old = e.getFontDelta();

		if (old != value) {
			setFontDelta(e, value);
			if (undoEnabled()) {
				logEdit(new SetFontDelta(e, old, value));
	}	}	}

	public void shiftDeltaFont(EntityInstance e, int delta) 
	{
		updateFontDelta(e, e.getFontDelta() + delta);
	}

	public void updateXRelLocal(EntityInstance e, double value)
	{
		double old = e.xRelLocal();

		if (value < 0) {
			value = 0;
		} 
		if (value != old) {
			setXRelLocal(e, value);
			if (undoEnabled()) {
				logEdit(new SetXRelLocal(e, old, value));
	}	}	}


	public void updateYRelLocal(EntityInstance e, double value)
	{
		double old = e.yRelLocal();

		if (value < 0) {
			value = 0;
		}
		if (value != old) {
			setYRelLocal(e, value);
			if (undoEnabled()) {
				logEdit(new SetYRelLocal(e, old, value));
	}	}	}

	public void updateWidthRelLocal(EntityInstance e, double value)
	{
		double old = e.widthRelLocal();

		if (value > 1.0) {
			value = 1.0;
		}
		if ((e.xRelLocal() + value) > 1.0) {
			updateRelLocal(e, 1.0 - value, e.yRelLocal(), value, e.heightRelLocal()); 
			return;
		}

		if (value != old) {
			setWidthRelLocal(e, value);
			if (undoEnabled()) {
				logEdit(new SetWidthRelLocal(e, old, value));
			}
	}	}

	public void updateHeightRelLocal(EntityInstance e, double value)
	{
		double old = e.heightRelLocal();

		if (value > 1.0) {
			value = 1.0;
		}
		if (e.yRelLocal() + value > 1.0) {
			updateRelLocal(e, e.xRelLocal(), 1.0 - value, e.widthRelLocal(), value);
			return;
		}
		if (value != old) {
			setHeightRelLocal(e, value);
			if (undoEnabled()) {
				logEdit(new SetHeightRelLocal(e, old, value));
			}
	}	}

	public void updateSizeRelLocal(EntityInstance e, double widthRelLocal, double heightRelLocal)
	{
		double	oldWidthRelLocal  = e.widthRelLocal();
		double	oldHeightRelLocal = e.heightRelLocal();
		boolean shifted           = false;

		if (widthRelLocal > 1.0) {
			widthRelLocal = 1.0;
		}
		if (heightRelLocal > 1.0) {
			heightRelLocal = 1.0;
		}

		if (oldWidthRelLocal == widthRelLocal) {
			updateHeightRelLocal(e, heightRelLocal);
			return;
		}
		if (oldHeightRelLocal == heightRelLocal) {
			updateWidthRelLocal(e, widthRelLocal);
			return;
		}

		double xRelLocal      = e.xRelLocal();
		double yRelLocal      = e.yRelLocal();

		if ((xRelLocal + widthRelLocal) > 1.0) {
			xRelLocal = 1.0 - widthRelLocal;
			shifted   = true;
		}
		
		if ((yRelLocal + heightRelLocal) > 1.0) {
			yRelLocal = 1.0 - heightRelLocal;
			shifted   = true;
		}

		if (shifted) {
			updateRelLocal(e, xRelLocal, yRelLocal, widthRelLocal, heightRelLocal);
			return;
		}

		setSizeRelLocal(e, widthRelLocal, heightRelLocal);

		if (undoEnabled()) {
			logEdit(new SetSizeRelLocal(e, oldWidthRelLocal, oldHeightRelLocal, widthRelLocal, heightRelLocal));
	}	}

	public void updateLocationRelLocal(EntityInstance e, double xRelLocal, double yRelLocal)
	{
		double	oldXRelLocal  = e.xRelLocal();
		double	oldYRelLocal  = e.yRelLocal();

		if (xRelLocal < 0.0) {
			xRelLocal = 0.0;
		}
		if (yRelLocal < 0.0) {
			yRelLocal = 0.0;
		}

		if (xRelLocal == oldXRelLocal) {
			updateYRelLocal(e, yRelLocal);
			return;
		}
		if (yRelLocal == oldYRelLocal) {
			updateXRelLocal(e, xRelLocal);
			return;
		}

		setLocationRelLocal(e, xRelLocal, yRelLocal);

		if (undoEnabled()) {
			logEdit(new SetLocationRelLocal(e, oldXRelLocal, oldYRelLocal, xRelLocal, yRelLocal));
	}	}

	public void updateRelLocal(EntityInstance e, double xRelLocal, double yRelLocal, double widthRelLocal, double heightRelLocal)
	{
		double oldXRelLocal      = e.xRelLocal();
		double oldYRelLocal      = e.yRelLocal();
		double oldWidthRelLocal  = e.widthRelLocal();
		double oldHeightRelLocal = e.heightRelLocal();

		if (xRelLocal < 0.0) {
			xRelLocal = 0.0;
		} 
		if (yRelLocal < 0.0) {
			yRelLocal = 0.0;
		}
		if (widthRelLocal > 1.0) {
			widthRelLocal = 1.0;
		}
		if (heightRelLocal > 1.0) {
			heightRelLocal = 1.0;
		}
		if ((xRelLocal + widthRelLocal) > 1.0) {
			xRelLocal = 1.0 - widthRelLocal;
		}

		if ((yRelLocal + heightRelLocal) > 1.0) {
			yRelLocal = 1.0 - heightRelLocal;
		}

		if (oldXRelLocal == xRelLocal && oldYRelLocal == yRelLocal) {
			updateSizeRelLocal(e, widthRelLocal, heightRelLocal);
			return;
		}
		if (oldWidthRelLocal == widthRelLocal && oldHeightRelLocal == heightRelLocal) {
			updateLocationRelLocal(e, xRelLocal, yRelLocal);
			return;
		}
		 
		setRelLocal(e, xRelLocal, yRelLocal, widthRelLocal, heightRelLocal);
		if (undoEnabled()) {
			logEdit(new SetRelLocal(e, oldXRelLocal, oldYRelLocal, oldWidthRelLocal, oldHeightRelLocal, xRelLocal, yRelLocal, widthRelLocal, heightRelLocal));
	}	}

	public void updateLiftEntityEdges(EntityInstance e)
	{
		EntityInstance	parent = e.getContainedBy();

		if (parent == null || parent == m_rootInstance) {
			return;
		}

		if (undoEnabled()) {
			logEdit(new LiftEntityEdges(e));
		}
		liftRelations(e, parent);
	}

	public void updateLiftEdges(EntityInstance rootedAt)
	{
		Enumeration			en;
		EntityInstance		child;

		// Do first so edges lifted only once
		// Ie. don't see edges later lifted into me

		if (rootedAt.getEntityClass().isActive()) {
			updateLiftEntityEdges(rootedAt);
		}	

		for (en = rootedAt.getChildren(); en.hasMoreElements(); ) {
			child = (EntityInstance) en.nextElement();
			updateLiftEdges(child);
		}
	}

	public boolean updateCutEntity(EntityInstance e)
	{
		boolean ret = cutEntity(e);
		if (ret && undoEnabled()) {
			logEdit(new CutEntity(e));
		}
		return ret;
	}

	public void updateDeleteActiveEntities(EntityInstance rootedAt)
	{
		Vector				srcRelList;
		int					i;
		RelationInstance	ri;

		if (!rootedAt.isMarked(EntityInstance.DRAWROOT_MARK) && rootedAt.getEntityClass().isActive()) {
			updateCutEntity(rootedAt);
			return;
		}
		
		srcRelList = rootedAt.getSrcRelList();	

		if (srcRelList != null) {
			for (i = srcRelList.size(); --i >= 0; ) {
				ri = (RelationInstance) srcRelList.elementAt(i);
				if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
					updateDeleteActiveEntities(ri.getDst());
		}	}	}
	}
	
	public void updateDeleteActiveEdges(EntityInstance rootedAt)
	{
		Vector				dstRelList = rootedAt.getDstRelList();
		Enumeration			en;
		EntityInstance		child;

		if (dstRelList != null) {
			EntityClass		ec = rootedAt.getEntityClass();
			if (ec.isActive()) {
				RelationInstance	ri;
				int					i;

				for (i = dstRelList.size(); i > 0; ) {
					ri = (RelationInstance) dstRelList.elementAt(--i);
					if (ri.getRelationClass().isActive()) {
						updateDeleteEdge(ri);
		}	}	}	}

		for (en = rootedAt.getChildren(); en.hasMoreElements(); ) {
			child = (EntityInstance) en.nextElement();
			updateDeleteActiveEdges(child);
	}	}

	public void updateMoveEntityContainment(EntityInstance newContainer, EntityInstance me) 
	{
		EntityInstance oldContainer = me.getContainedBy();

		if (moveEntityContainment(newContainer, me)) {
			if (undoEnabled()) {
				logEdit(new MoveEntityContainment(me, oldContainer, newContainer));
	}	}	}
	
	public void updateMovePlaceEntityContainment(EntityInstance newContainer, EntityInstance me)
	{
		double	xrel1      = me.xRelLocal();
		double	yrel1      = me.yRelLocal();
		double	widthrel1  = me.widthRelLocal();
		double	heightrel1 = me.heightRelLocal();  
		double	xrel2, yrel2, widthrel2, heightrel2;
			
		me.setInitialLocation(newContainer);	
		xrel2      = me.xRelLocal();
		yrel2      = me.yRelLocal();
		widthrel2  = me.widthRelLocal();
		heightrel2 = me.heightRelLocal(); 
		// Put it back the way it was so update sees correct old value
		// and undo will correctly restore these old values
		me.setRelLocal(xrel1, yrel1, widthrel1, heightrel1);
		updateMoveEntityContainment(newContainer, me);
		updateRelLocal(me, xrel2, yrel2, widthrel2, heightrel2);
	}

	public boolean updateDeleteContainer(EntityInstance e)
	{
		boolean ret = deleteContainer(e);

		if (ret) {
			if (undoEnabled()) {
				logEdit(new UpdateDeleteContainer(e));
		}	}
		return(ret);
	}

	public void updateDeleteActiveContainers(EntityInstance rootedAt)
	{
		Vector				srcRelList;
		int					i;
		RelationInstance	ri;
		
		srcRelList = rootedAt.getSrcRelList();	

		if (srcRelList != null) {
			for (i = srcRelList.size(); --i >= 0; ) {
				ri = (RelationInstance) srcRelList.elementAt(i);
				if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
					updateDeleteActiveContainers(ri.getDst());
		}	}	}

		if (!rootedAt.isMarked(EntityInstance.DRAWROOT_MARK) && rootedAt.getEntityClass().isActive()) {
			updateDeleteContainer(rootedAt);
		}
	}

	// Transfer an entity from one TA to another using caching

	public EntityInstance updateClusterEntity(EntityInstance container, EntityInstance e)
	{
		EntityInstance ret = clusterEntity(container, e);
		
		if (undoEnabled()) {
			logEdit(new ClusterEntity(container, ret));
   		} 
		return(ret);
	}

	private void visitRemoveDead(EntityInstance from)
	{
		Vector				relations;
		RelationInstance	ri;
		EntityInstance		to;
		int					i, size;

		if (!from.isMarked(EntityInstance.IN_GRAPH_MARK)) {
			return;
		}
		from.nandMark(EntityInstance.IN_GRAPH_MARK);
		relations = from.getSrcRelList();
		size      = (relations != null ? relations.size() : 0);
		for (i = 0; i < size; ++i) {
			ri = (RelationInstance) relations.elementAt(i);
			to = ri.getDst();
			visitRemoveDead(to);
	}	}

	public boolean updateRemoveDead(EntityInstance from)
	{
		Diagram			diagram     = getDiagram();
		LandscapeEditorCore ls      = diagram.getLs();
		ResultBox       resultBox   = ls.getResultBox();
		EntityCache 	entityCache = diagram.getEntityCache();
		EntityInstance 	e;
		EntityClass		ec;
		boolean			ret = true;

		resultBox.clear();
        resultBox.setResultTitle("Removing unreachable from " + from);

		for (e = entityCache.getFirst(); e != null; e = entityCache.getNext()) {
			e.orMark(EntityInstance.IN_GRAPH_MARK);
		}
		visitRemoveDead(from);
		for (e = entityCache.getFirst(); e != null; e = entityCache.getNext()) {
			if (e.isMarked(EntityInstance.IN_GRAPH_MARK)) {
				e.nandMark(EntityInstance.IN_GRAPH_MARK);
				if (e.getContainedBy() == null) {
					continue;
				}
				ec = e.getEntityClass();
				if (!ec.isActive()) {
					continue;
				}
                ret &= updateCutEntity(e);
				resultBox.addResultEntity(e, 0);
		}	}
		resultBox.addText("End of Report");
		return ret;
	}

	class ImportEntity extends MyUndoableEdit implements UndoableEdit
	{
		EntityInstance		 m_container;
		EntityInstance		 m_e;
		EntityInstance		 m_match;
		EntityInstance		 m_ret;


		EntityClass			 m_old_match_parentClass;
		EntityInstance		 m_old_match_containedBy;
		double				 m_old_match_x, m_old_match_y, m_old_match_width, m_old_match_height;

		ImportEntity(EntityInstance container, EntityInstance e, EntityInstance match)
		{
			m_container = container;
			m_e         = e;
			m_match     = match;

			m_old_match_parentClass = (EntityClass) match.getParentClass();
			m_old_match_containedBy = match.getContainedBy();
			m_old_match_x           = match.xRelLocal();
			m_old_match_y           = match.yRelLocal();
			m_old_match_width       = match.widthRelLocal();
			m_old_match_height      = match.heightRelLocal();

			m_ret                   = importEntity(container, e, match);
		}

		public void dumpEdit(PrintWriter ps)
		{
			ps.println(m_e.getId() + " { action=ImportEntity Acontainer=" +  m_container.getId() + " Amatch=" + m_match.getId() + " }" );
		}
		
		public EntityInstance getEntity()
		{
			return m_ret;
		}

		public String getPresentationName() 
		{
			return "Import entity " + m_e;
		}

		public void undo()
		{
			EntityInstance match = m_match;

			match.setRelLocal(m_old_match_x, m_old_match_y, m_old_match_width, m_old_match_height);
			moveEntityContainment(m_old_match_containedBy, match);
			match.setParentClass(m_old_match_parentClass);
		}

		public void redo()
		{
			m_ret = importEntity(m_container, m_e, m_match);
		}
	}	

	// Transfer an entity from one TA to another using caching

	public EntityInstance updateImportEntity(EntityInstance container, EntityInstance e, EntityInstance match)
	{
		EntityInstance ret;
		
		if (!undoEnabled()) {
			ret = importEntity(container, e, match);
		} else {
			ImportEntity ie;
			
			ie  = new ImportEntity(container, e, match);
			logEdit(ie);
			ret = ie.getEntity();     
		} 
		return(ret);
	}

	/* Updates for LandscapeClassObject */

	public void updateInherits(LandscapeClassObject object, Vector value)
	{
		Vector old   = object.getInheritsFrom();

		if (old.size() != value.size() || !value.containsAll(old)) {
			setInherits(object, value);
			if (undoEnabled()) {
				logEdit(new SetInherits(object, old, value));
	}	}	}

	// -----------
	// EntityClass
	// -----------


	private Vector cacheEntityClassPairs(EntityClass ec)
	{
		Vector			eps = null;
		Enumeration		en;
		RelationClass	rc;
		Vector			relationList;
		EntityClassPair	ep;
		int				i;
		
		for (en = m_relationClasses.elements(); en.hasMoreElements(); ) {
			rc           = (RelationClass) en.nextElement(); 
			relationList = rc.getRelationList();
			if (relationList != null) {
				for (i = relationList.size(); i > 0; ) {
					ep = (EntityClassPair) relationList.elementAt(--i);
					if (ep.m_entityClass1 == ec || ep.m_entityClass2 == ec) {
						if (eps == null) {
							eps = new Vector();
						}
						eps.addElement(ep);
		}	}	}	}
		return(eps);
	}

	public void updateNewEntityClass(String id, EntityClass baseClass)
	{
		EntityClass ec = newEntityClass(id, baseClass);
		if (undoEnabled()) {
			logEdit(new NewEntityClass(ec, id, baseClass));
	}	}

	/* N.B.updateDeleteActiveEntities() first */

	public void updateRemoveEntityClass(EntityClass ec)
	{
		if (undoEnabled()) {
			logEdit(new RemoveEntityClass(ec, cacheEntityClassPairs(ec), m_defaultEntityClass));
		}
		removeEntityClass(ec);
	}

	public void updateImage(EntityClass ec, int value)
	{
		int	old = ec.getImage();
		if (old != value) {
			setImage(ec, value);
			if (undoEnabled()) {
				logEdit(new SetImage(ec, old, value));
	}	}	}

	public void updateAngle(EntityClass ec, double value)
	{
		double	old = ec.getAngle();

		if (value != old) {
			setAngle(ec, value);
			if (undoEnabled()) {
				logEdit(new SetAngle(ec, old, value));
	}	}	}

	public void updateIconFile(EntityClass ec, String value)
	{
		String old = ec.getUnscaledIconFile();

		if (old != value) {
			setUnscaledIconFile(ec, value);
			if (undoEnabled()) {
				logEdit(new SetUnscaledIconFile(ec, old, value));
			}
		}
	}

	// -------------
	// RelationClass
	// -------------

	public RelationClass updateNewRelationClass(String id, RelationClass baseClass)
	{
		RelationClass rc = newRelationClass(id, baseClass);
		if (undoEnabled()) {
			logEdit(new NewRelationClass(rc, id, baseClass));
		}
		return rc;
	}

	// N.B. updateDeleteActiveEdges() first

	public void updateRemoveRelationClass(RelationClass rc)
	{
		if (undoEnabled()) {
			logEdit(new RemoveRelationClass(rc, m_defaultRelationClass));
		}
		removeRelationClass(rc);
	}

	public void updateArrowColor(RelationClass object, Color value)
	{
		Color	old = object.getArrowColor();
		if (old != value) {
			setArrowColor(object, value);
			if (undoEnabled()) {
				logEdit(new SetArrowColor(object, old, value));
	}	}	}

	public void updateIOfactor(RelationClass object, short value)
	{
		short	old = object.getIOfactor();
		if (old != value) {
			setIOfactor(object, value);
			if (undoEnabled()) {
				logEdit(new SetIOfactor(object, old, value));
	}	}	}

	public void updateSwitchContainsClasses(RelationClass[] newContainsClasses)
	{
		if (undoEnabled()) {
			logEdit(new SwitchContainsClasses(newContainsClasses, m_containsClasses));
		}
		switchContainsClasses(newContainsClasses);
	}

	/* Generic update facility */

	public void setValueAt(LandscapeObject object, Object value, int row)
	{
		if (object instanceof EntityInstance) {
			EntityInstance e = (EntityInstance) object;

			switch (row) {
			case EntityInstance.ID_ATTR:
				// Can't change id
				// e.setId((String) value);
				return;
			case EntityInstance.CLASS_ATTR:
			{
				if (value != null) {
					LandscapeClassObject parentClass = e.getParentClass();
					String newId = (String) value;
					if (parentClass == null || !parentClass.getLabelId().equals(newId)) {
						Enumeration	en;
						EntityClass	ec;

						for (en = enumEntityClasses(); en.hasMoreElements(); ) {
							ec = (EntityClass) en.nextElement();
							if (ec.getLabelId().equals(newId)) {
								updateParentClass(e, ec);
								break;
				}	}	}	}
				return;
			}
			case EntityInstance.LABEL_ATTR:
				updateLabel(e, (String) value);
				return;
			case EntityInstance.DESC_ATTR:
				updateDescription(e, (String) value);
				return;
			case EntityInstance.COLOR_ATTR:
				updateObjectColor(e, (Color) value);
				return;
			case EntityInstance.LABEL_COLOR_ATTR:
				updateLabelColor(e, (Color) value);
				return;
			case EntityInstance.OPEN_COLOR_ATTR:
				updateColorWhenOpen(e, (Color) value);
				return;
			case EntityInstance.XRELPOSITION_ATTR:
				updateXRelLocal(e, ((Double) value).doubleValue());
				return;
			case EntityInstance.YRELPOSITION_ATTR:
				updateYRelLocal(e, ((Double) value).doubleValue());
				return;
			case EntityInstance.WIDTHREL_ATTR:
				updateWidthRelLocal(e, ((Double) value).doubleValue());
				return;
			case EntityInstance.HEIGHTREL_ATTR:
				updateHeightRelLocal(e, ((Double) value).doubleValue());
				return;
			case EntityInstance.FONTDELTA_ATTR:
				updateFontDelta(e, ((Integer) value).intValue());
				return;
			}
		} else if (object instanceof RelationInstance) {
			RelationInstance ri = (RelationInstance) object;

			switch (row) {
			case RelationInstance.CLASS_ATTR:
				if (value != null) {
					LandscapeClassObject parentClass = ri.getParentClass();
					String				 newId       = (String) value;

					if (parentClass == null || !parentClass.getLabelId().equals(newId)) {
						Enumeration		en;
						RelationClass	ec;

						for (en = enumRelationClasses(); en.hasMoreElements(); ) {
							ec = (RelationClass) en.nextElement();
							if (ec.getLabelId().equals(newId)) {
								updateParentClass(ri, ec);
								break;
				}	}	}	}
				return;
			case RelationInstance.COLOR_ATTR:
				updateObjectColor(ri, (Color) value);
				return;
			case RelationInstance.STYLE_ATTR:
				updateStyle(ri, ((Integer) value).intValue());
				return;
			}
		} else if (object instanceof EntityClass) {
			EntityClass ec = (EntityClass) object;

			switch (row) {
			case EntityClass.ID_ATTR:
				// Can't change id
				// ec.setId((String) value);
				return;
			case EntityClass.CLASSLABEL_ATTR:
				updateLabel(ec, (String) value);
				return;
			case EntityClass.CLASSDESC_ATTR:
				updateDescription(ec, (String) value);
				return;
			case EntityClass.CLASSSTYLE_ATTR:
				updateStyle(ec, ((Integer) value).intValue());
				return;
			case EntityClass.CLASSANGLE_ATTR:
				updateAngle(ec, ((Double) value).doubleValue());
				return;
			case EntityClass.CLASSICON_ATTR:
				updateIconFile(ec, (String) value);
				return;
			case EntityClass.CLASSIMAGE_ATTR:
				updateImage(ec, ((Integer) value).intValue());
				return;
			case EntityClass.COLOR_ATTR:
				updateObjectColor(ec, (Color) value);
				return;
			case EntityClass.LABEL_COLOR_ATTR:
				updateLabelColor(ec, (Color) value);
				return;
			case EntityClass.OPEN_COLOR_ATTR:
				updateColorWhenOpen(ec, (Color) value);
				return;
			}
		} else if (object instanceof RelationClass) {
			RelationClass rc = (RelationClass) object;

			switch (row) {
			case RelationClass.ID_ATTR:
				// Can't change id
				// rc.setId((String) value);
				return;
			case RelationClass.CLASSLABEL_ATTR:
				updateLabel(rc, (String) value);
				return;
			case RelationClass.CLASSRLABEL_ATTR:
				updateReversedLabel(rc, (String) value);
				return;
			case RelationClass.CLASSDESC_ATTR:
				updateDescription(rc, (String) value);
				return;
			case RelationClass.CLASSSTYLE_ATTR:
				updateStyle(rc, ((Integer) value).intValue());
				return;
			case RelationClass.COLOR_ATTR:
				updateObjectColor(rc, (Color) value);
				return;
			case RelationClass.LABEL_COLOR_ATTR:
				updateLabelColor(rc, (Color) value);
				return;
			case RelationClass.FACTOR_ATTR:
				updateIOfactor(rc, Util.relativeToShort(((Double) value).doubleValue()));
				return;
			case RelationClass.ARROWCOLOR_ATTR:
				updateArrowColor(rc, (Color) value);
				return;
		}	}

		updateAttributeValueAt(object, row, value);
	}

	/* Clipboard updates */

	public boolean updateCutClipboard(Clipboard old_clipboard, Clipboard new_clipboard)
	{
		boolean ok = cutClipboard(new_clipboard);
		if (ok && undoEnabled()) {
			logEdit(new CutClipboard(this, old_clipboard, new_clipboard));
		}
		return(ok);
	}

	public void updatePasteClipboard(Clipboard clipboard, EntityInstance pe) 
	{
		pasteClipboard(clipboard, pe);
		if (undoEnabled()) {
			logEdit(new PasteClipboard(this, clipboard, pe));
		}
	}
}

