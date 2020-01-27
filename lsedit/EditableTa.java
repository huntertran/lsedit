package lsedit;

import java.awt.Color;

import java.util.Enumeration;
import java.util.Vector;

/* This class extends TA with basic update operations
 * It knows about the diagram.  The intent is that
 * all updates are performed through it so that it
 * can eventually signal all changes to the TA
 * appropriately.
 */

public class EditableTa extends Ta 
{
	private		Vector			m_taListeners	          = new Vector();	// TaListeners
	private		int[]			m_taListenersOffsets;						// Start of listeners in this category
	private		int				m_numberTaListeners       = 0;
	private		int				m_numberActiveTaListeners = 0;

	private ClipboardListener	m_clipboardListener;

	private		Clipboard		m_clipboard  = null;

	private		Diagram			m_diagram;

	private		boolean			m_counts_bad = false;

	// ------------------
	// EditableTa methods
	// ------------------

	public EditableTa(TaFeedback taFeedback)
	{
		super(taFeedback);

		int[]	offsets;
		int		i;
		
		m_taListenersOffsets = offsets = new int[TaListener.CATEGORIES + 1];
		for (i = TaListener.CATEGORIES; i >= 0; --i) {
			offsets[i] = 0;
		}
	}

	private final static	String[] g_taSignals = {

/*  0 */	"EC_NEW_SIGNAL",
/*  1 */	"EC_DELETE_SIGNAL",
/*  2 */	"EC_UNDELETE_SIGNAL",
/*  3 */	"RC_NEW_SIGNAL",
/*  4 */	"RC_DELETE_SIGNAL",
/*  5 */	"RC_UNDELETE_SIGNAL",
/*  6 */	"RC_IOFACTOR_SIGNAL",
/*  7 */	"CONTAINS_CHANGING_SIGNAL",
/*  8 */	"CONTAINS_CHANGED_SIGNAL",
/*  9 */	"DRAWROOT_CUTTING_SIGNAL",
/* 10 */	"ENTITY_NEW_SIGNAL",
/* 11 */	"ENTITY_CUTTING_SIGNAL",
/* 12 */	"ENTITY_CUT_SIGNAL",
/* 13 */	"ENTITY_PASTED_SIGNAL",
/* 14 */	"CONTAINER_CUTTING_SIGNAL",
/* 15 */	"CONTAINER_CUT_SIGNAL",
/* 16 */	"CONTAINER_PASTED_SIGNAL",
/* 17 */	"ENTITY_RELOCATING_SIGNAL",
/* 18 */	"ENTITY_RELOCATED_SIGNAL",
/* 19 */	"RELATION_NEW_SIGNAL",
/* 20 */	"RELATION_SRC_CUT_SIGNAL",
/* 21 */	"RELATION_SRC_PASTED_SIGNAL",
/* 22 */	"RELATION_DST_CUT_SIGNAL",
/* 23 */	"RELATION_DST_PASTED_SIGNAL",
/* 24 */	"RELATION_CUT_SIGNAL",
/* 25 */	"RELATION_PASTED_SIGNAL",
/* 26 */	"POSITION_SIGNAL",
/* 27 */	"SIZE_SIGNAL",
/* 28 */	"BOUNDS_SIGNAL",
/* 29 */	"PARENTCLASS_SIGNAL",
/* 30 */	"STYLE_SIGNAL",
/* 31 */	"LABEL_SIGNAL",
/* 32 */	"DESCRIPTION_SIGNAL",
/* 33 */	"COLOR_SIGNAL",
/* 34 */	"LABEL_COLOR_SIGNAL",
/* 35 */	"OPEN_COLOR_SIGNAL",
/* 36 */	"FONT_DELTA_SIGNAL",
/* 37 */	"INHERITS_SIGNAL",
/* 38 */	"IO_FACTOR_SIGNAL",
/* 39 */	"ARROW_COLOR_SIGNAL",
/* 40 */	"NEW_VIEW_SIGNAL",
/* 41 */	"EC_IMAGE_SIGNAL",
/* 42 */	"EC_ANGLE_SIGNAL",
/* 43 */	"EC_ICON_SIGNAL"
		};

	public static String	taSignal(int signal)
	{
		if (signal < 0 || signal >= g_taSignals.length) {
			return ("Unknown TA signal");
		}
		return g_taSignals[signal];
	}

	public void addTaListener(TaListener listener, int category)
	{
		Vector		listeners = m_taListeners;
		int[]		offsets   = m_taListenersOffsets;
		int			i;

		if (category < 0 || category >= TaListener.CATEGORIES) {
			System.out.println("EditableTa.addTaListener invalid category=" + category);
			return;
		}
		listeners.insertElementAt(listener, offsets[category+1]);
		for (i = category; ++i <= TaListener.CATEGORIES; ) {
			// Start offsets for later categories
			++offsets[i];
		}
		m_numberActiveTaListeners = ++m_numberTaListeners;
	}

	public void disableTaListeners()
	{
		m_numberActiveTaListeners = -1;
	}

	public void enableTaListeners()
	{
		m_numberActiveTaListeners = m_numberTaListeners;
	}

	protected void signalDiagramChanging(Diagram diagram)
	{
		Vector		listeners = m_taListeners;
		int			i         = m_numberActiveTaListeners;

		for (; --i >= 0; ) {
			((TaListener) listeners.elementAt(i)).diagramChanging(diagram);
		}
	}
	
	protected void signalDiagramChanged(Diagram diagram, int signal)
	{
		Vector		listeners = m_taListeners;
		int			i         = m_numberActiveTaListeners;

		for (; --i >= 0; ) {
			((TaListener) listeners.elementAt(i)).diagramChanged(diagram, signal);
		}
	}

	/* Special handling for massive changes made to attributes of entities
	 * or of changes to edges
	 */

	protected void beginUpdates()
	{
		Vector		listeners = m_taListeners;
		int			i         = m_numberActiveTaListeners;

		for (; --i >= 0; ) {
			((TaListener) listeners.elementAt(i)).updateBegins();
		}
	}

	protected void endUpdates()
	{
		Vector		listeners = m_taListeners;
		int			i         = m_numberActiveTaListeners;

		for (; --i >= 0; ) {
			((TaListener) listeners.elementAt(i)).updateEnds();
		}
	}

	public void signalEntityClassChanged(EntityClass ec, int signal)
	{
		Vector		listeners = m_taListeners;
		int			last      = m_taListenersOffsets[TaListener.SCHEMALISTENER];
		int			i         = m_numberActiveTaListeners;

		for (; --i >= last; ) {
			((TaListener) listeners.elementAt(i)).entityClassChanged(ec, signal);
		}
	}
	
	public void signalRelationClassChanged(RelationClass rc, int signal)
	{
		Vector		listeners = m_taListeners;
		int			last      = m_taListenersOffsets[TaListener.SCHEMALISTENER];
		int			i         = m_numberActiveTaListeners;

		for (; --i >= last; ) {
			((TaListener) listeners.elementAt(i)).relationClassChanged(rc, signal);
		}
	}

	protected void signalEntityParentChanged(EntityInstance e, EntityInstance parent, int signal)
	{
		Vector		listeners = m_taListeners;
		int			last      = m_taListenersOffsets[TaListener.ENTITYLISTENER];
		int			i         = m_numberActiveTaListeners;

		for (; --i >= last; ) {
			((TaListener) listeners.elementAt(i)).entityParentChanged(e, parent, signal);
		}	
	}

	protected void signalRelationParentChanged(RelationInstance ri, int signal)
	{
		Vector		listeners = m_taListeners;
		int			last      = m_taListenersOffsets[TaListener.INSTANCELISTENER];
		int			i         = m_numberActiveTaListeners;

		for (; --i >= last; ) {
			((TaListener) listeners.elementAt(i)).relationParentChanged(ri, signal);
		}	
	}

	protected void signalEntityChanged(EntityInstance e, int signal)
	{
		Vector		listeners = m_taListeners;
		int			last      = m_taListenersOffsets[TaListener.ENTITYATTRIBUTELISTENER];
		int			i         = m_numberActiveTaListeners;

		for (; --i >= last; ) {
			((TaListener) listeners.elementAt(i)).entityInstanceChanged(e, signal);
		}	
	}

	protected void signalRelationChanged(RelationInstance ri, int signal)
	{
		Vector		listeners = m_taListeners;
		int			last      = m_taListenersOffsets[TaListener.ATTRIBUTELISTENER];
		int			i         = m_numberActiveTaListeners;

		for (; --i >= last; ) {
			((TaListener) listeners.elementAt(i)).relationInstanceChanged(ri, signal);
		}
	}

	protected void signalObjectChanged(LandscapeObject object, int signal)
	{
		if (object instanceof EntityInstance) {
			signalEntityChanged((EntityInstance) object, signal);
			return;
		}
		if (object instanceof RelationInstance) {
			signalRelationChanged((RelationInstance) object, signal);
			return;
		}
		if (object instanceof EntityClass) {
			signalEntityClassChanged((EntityClass) object, signal);
			return;
		}
		if (object instanceof RelationClass) {
			signalRelationClassChanged((RelationClass) object, signal);
	}	}

	public void setDiagram(Diagram diagram)
	{
		m_diagram = diagram;
	}

	public void setInitialLocation(EntityInstance e, EntityInstance container)
	{
		e.setInitialLocation(container);
		signalEntityChanged(e, TaListener.BOUNDS_SIGNAL);
	}

	public void recomputeCounts()
	{
		if (m_counts_bad) {
			Enumeration		en;
			EntityClass		ec;
			RelationClass	rc;

			for (en = enumEntityClasses(); en.hasMoreElements(); ) {
				ec = (EntityClass) en.nextElement();
				ec.noMembers();
			}

			for (en = enumRelationClasses(); en.hasMoreElements(); ) {
				rc = (RelationClass) en.nextElement();
				rc.noMembers();
			}

			if (m_rootInstance != null) {
				m_rootInstance.recomputeCounts();
			}
			m_counts_bad = false;
	}	}

	public boolean isCountsBad()
	{
		return m_counts_bad;
	}

	// ---------------
	// LandscapeObject
	// ---------------

	public void setParentClass(LandscapeObject object, LandscapeClassObject value)
	{
		object.setParentClass(value);
		signalObjectChanged(object, TaListener.PARENTCLASS_SIGNAL);
	}

	public void setStyle(LandscapeObject object, int value)
	{
		object.setStyle(value);
		signalObjectChanged(object, TaListener.STYLE_SIGNAL);
	}

	public void setObjectColor(LandscapeObject object, Color value) 
	{
		object.setObjectColor(value);
		signalObjectChanged(object, TaListener.COLOR_SIGNAL);
	}

	public void setAttributeName(LandscapeObject object, String oldName, String newName)
	{
		object.setAttributeName(oldName, newName);
		// Not currently signalled
	}

	public void addAttribute(LandscapeObject object, String id, String value) 
	{
		object.addAttribute(id, value);
		// Not currently signalled
	}

	// ----------------
	// RelationInstance
	// ----------------

	protected RelationInstance getNewRelation(RelationClass rc, EntityInstance from, EntityInstance to)
	{
		RelationInstance ri = addEdge(rc, from, to);

		signalRelationParentChanged(ri, TaListener.RELATION_NEW_SIGNAL);
		return ri;
	}

	private void cutRelationSrc(RelationInstance ri)
	{
		EntityInstance src = ri.getSrc();
		if (src.removeSrcRelation(ri)) {
			signalRelationParentChanged(ri, TaListener.RELATION_SRC_CUT_SIGNAL);
	}	}

	private void cutRelationDst(RelationInstance ri)
	{
		EntityInstance dst = ri.getDst();
		if (dst.removeDstRelation(ri)) {
			signalRelationParentChanged(ri, TaListener.RELATION_DST_CUT_SIGNAL);
	}	}

	private void pasteRelationSrc(RelationInstance ri, EntityInstance src)
	{
		ri.setSrc(src);
		src.addSrcRelation(ri);
		signalRelationParentChanged(ri, TaListener.RELATION_SRC_PASTED_SIGNAL);
	}

	private void pasteRelationDst(RelationInstance ri, EntityInstance dst)
	{
		ri.setDst(dst);
		dst.addDstRelation(ri);
		signalRelationParentChanged(ri, TaListener.RELATION_DST_PASTED_SIGNAL);
	}

	public void moveRelationSrc(RelationInstance ri, EntityInstance src)
	{
		cutRelationSrc(ri);
		pasteRelationSrc(ri, src);
	}

	public void moveRelationDst(RelationInstance ri, EntityInstance dst)
	{
		cutRelationDst(ri);
		pasteRelationDst(ri, dst);
	}

	public void cutRelation(RelationInstance ri)
	{
		ri.decrementClassMembers();
		cutRelationSrc(ri);
		cutRelationDst(ri);

		signalRelationParentChanged(ri, TaListener.RELATION_CUT_SIGNAL);
	}

	public void pasteRelation(RelationInstance ri)
	{
		ri.incrementClassMembers();
		pasteRelationSrc(ri, ri.getSrc());
		pasteRelationDst(ri, ri.getDst());
		signalRelationParentChanged(ri, TaListener.RELATION_PASTED_SIGNAL);
	}

	/* LandscapeObject3D updates */

	protected void setLabel(LandscapeObject3D object, String value)
	{
		object.setLabel(value);
		signalObjectChanged(object, TaListener.LABEL_SIGNAL);
	}

	protected void setReversedLabel(RelationClass object, String value)
	{
		object.setReversedLabel(value);
		signalObjectChanged(object, TaListener.LABEL_SIGNAL);
	}
	
	protected void setDescription(LandscapeObject3D object, String value)
	{
		object.setDescription(value);
		signalObjectChanged(object, TaListener.DESCRIPTION_SIGNAL);
	}

	public boolean setLabelColor(LandscapeObject3D object, Color value) 
	{
		boolean ret = object.setLabelColor(value);
		signalObjectChanged(object, TaListener.LABEL_COLOR_SIGNAL);
		return ret;
	}

	public void setColorWhenOpen(LandscapeObject3D object, Color value) 
	{
		object.setColorWhenOpen(value);
		signalObjectChanged(object, TaListener.OPEN_COLOR_SIGNAL);
	}

	// --------------
	// EntityInstance
	// --------------

	public EntityInstance getNewEntity(EntityClass ec, EntityInstance container)
	{
		int					n;
		String				ename;
		EntityInstance		e;
		RelationInstance	ri;

		for (n = 0; ; ++n) {
			ename = "Entity#" + n;
			if (!entityExists(ename)) {
				break;
		}	}

		if (ec == null) {
			ec  = m_defaultEntityClass;
			if (ec == null) {
				ec = m_entityBaseClass;
		}	}

		e  = newCachedEntity(ec, ename);
		ri = addEdge(getPrimaryContainsClass(), container, e);
		e.setContainedByRelation(ri);
		prepostorder();
		signalEntityParentChanged(e, container, TaListener.ENTITY_NEW_SIGNAL);

		return(e);
	}

	public void setFontDelta(EntityInstance e, int value) 
	{
		e.setFontDelta(value);
		signalEntityChanged(e, TaListener.FONT_DELTA_SIGNAL);
	}

	public void setXRelLocal(EntityInstance e, double value)
	{
		e.setXRelLocal(value);
		signalEntityChanged(e, TaListener.POSITION_SIGNAL);
	}

	public void setYRelLocal(EntityInstance e, double value)
	{
		e.setYRelLocal(value);
		signalEntityChanged(e, TaListener.POSITION_SIGNAL);
	}

	public void setWidthRelLocal(EntityInstance e, double value) 
	{
		e.setWidthRelLocal(value);
		signalEntityChanged(e, TaListener.SIZE_SIGNAL);
	}

	public void setHeightRelLocal(EntityInstance e, double value) 
	{
		e.setHeightRelLocal(value);
		signalEntityChanged(e, TaListener.SIZE_SIGNAL);
	}

	public void setSizeRelLocal(EntityInstance e, double widthRelLocal, double heightRelLocal)
	{
		e.setWidthRelLocal(widthRelLocal);
		e.setHeightRelLocal(heightRelLocal);
		signalEntityChanged(e, TaListener.SIZE_SIGNAL);
	}

	public void setLocationRelLocal(EntityInstance e, double xRelLocal, double yRelLocal)
	{
		e.setXRelLocal(xRelLocal);
		e.setYRelLocal(yRelLocal);
		signalEntityChanged(e, TaListener.POSITION_SIGNAL);
	}

	public void	setRelLocal(EntityInstance e, double newX, double newY, double newWidth, double newHeight)
	{
//		System.out.println("setRelLocal " + newX + "," + newY + " " + newWidth + "x" + newHeight);

		boolean	positionChanged = (e.xRelLocal()     != newX)     || (e.yRelLocal()      != newY);
		boolean areaChanged     = (e.widthRelLocal() != newWidth) || (e.heightRelLocal() != newHeight);
		e.setRelLocal(newX, newY, newWidth, newHeight);
		if (positionChanged && areaChanged) {
			signalEntityChanged(e, TaListener.BOUNDS_SIGNAL);
		} else {
			if (positionChanged) {
				signalEntityChanged(e, TaListener.POSITION_SIGNAL);
			}
			if (areaChanged) {
				signalEntityChanged(e, TaListener.SIZE_SIGNAL);
		}	}
	}

	public void removeEntitiesFromCache()
	{
		EntityInstance rootInstance = m_rootInstance;

		if (rootInstance != null) {
			rootInstance.removeTreeFromCache(m_entityCache);
		}
		int size = m_entityCache.size();

		if (size != 0) {
			System.out.println("Diagram.removeEntitiesFromCache failed count=" + size);
			m_entityCache.show();
	}	}

	public void addEntitiesToCache()
	{
		if (m_rootInstance != null) {
			m_rootInstance.addTreeToCache(m_entityCache);
	}	}

	public void markDeleted(EntityInstance me)
	{
		m_counts_bad = true;
		removeCache(me);
		me.markDeleted();
	}

	public void clearDeleted(EntityInstance me)
	{
		m_counts_bad = true;
		me.clearDeleted();
		putCache(me);
	}

	// After this operation everything in/under top knows about its
	// edges but nothing else does

	public void disconnectEdges(EntityInstance me, EntityInstance top)
	{
		Enumeration			en = me.srcRelationElements();
		RelationInstance	ri;
		EntityInstance		other;
		EntityInstance		e;

		markDeleted(me);

		if (en != null) {
			while (en.hasMoreElements()) {
				ri    = (RelationInstance) en.nextElement();
				other = ri.getDst();
				if (!top.hasDescendantOrSelf(other)) {
					other.removeDstRelation(ri); 
		}	}	}

		en = me.dstRelationElements();
		if (en != null) {
			while (en.hasMoreElements()) {
				ri    = (RelationInstance) en.nextElement();
				other = ri.getSrc();
				if (!top.hasDescendantOrSelf(other)) {
					other.removeSrcRelation(ri);
		}	}	}

		// We continue to know who our children are but by this point they
		// don't know who their parent is

		for (en = me.getChildren(); en.hasMoreElements(); ) {
			e = (EntityInstance) en.nextElement();
			disconnectEdges(e, top);
		} 
	}

	public void reconnectEdges(EntityInstance me)
	{
		Enumeration			en;
		RelationInstance	ri;
		EntityInstance		other;
		EntityInstance		e;

		for (en = me.getChildren(); en.hasMoreElements(); ) {
			e = (EntityInstance) en.nextElement();
			reconnectEdges(e);
		}

		clearDeleted(me);

		en = me.srcRelationElements();
		if (en != null) {
			while (en.hasMoreElements()) {
				ri    = (RelationInstance) en.nextElement();
				other = ri.getDst();
				other.addDstRelationIfAbsent(ri); 
		}	}

		en = me.dstRelationElements();
		if (en != null) {
			while (en.hasMoreElements()) {
				ri    = (RelationInstance) en.nextElement();
				other = ri.getSrc();
				other.addSrcRelationIfAbsent(ri);
	}	}	}

	// Remove m_dstRelList relations from entities on our m_srcRelList.
	// Remove m_srcRelList relations from entities on our m_dstRelList. 
	// After this operation we continue to know all about all edges to/from
	// us, but other entitites know nothing about all edges to/from us.
	// Can't apply this to the root since no where to add children

	private void disconnectEdgesJustMe(EntityInstance me, EntityInstance parent)
	{
		RelationClass		containsClass = getPrimaryContainsClass();
		Vector				dstRelList    = me.getDstRelList();
		Vector				srcRelList    = me.getSrcRelList();
		RelationInstance	ri;
		EntityInstance		other;
		int					i;

		// This entity can no longer belongs to any active group

		if (dstRelList != null) {
			for (i = dstRelList.size(); --i >= 0; ) {
				ri    = (RelationInstance) dstRelList.elementAt(i);
				other = ri.getSrc();
				if (other != me) {
					// Remove everything that points at me
					other.removeSrcRelation(ri);
		}	}	}
		
		if (srcRelList != null) {
			for (i = srcRelList.size(); --i >= 0; ) {
				ri    = (RelationInstance) srcRelList.elementAt(i);
				if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
					other = ri.getDst();
					
					// No longer a child of us -- make child of parent

					signalEntityParentChanged(other, me, TaListener.ENTITY_RELOCATING_SIGNAL);
					me.removeContainment(other);
					ri.setSrc(parent);
					parent.addSrcRelation(ri);
					parent.addContainment(other);
					prepostorder();
					signalEntityParentChanged(other, parent, TaListener.ENTITY_RELOCATED_SIGNAL);

					// Preserve old facts in me in a dummy relation
					ri = newRelation(containsClass, me, other);
					other.setContainedByRelation(ri);
					srcRelList.setElementAt(ri, i);
				} else {
					other = ri.getDst();
					if (other != me) {
						other.removeDstRelation(ri);
		}	}	}	}
	}

	// Add m_dstRelList relations from entities on our m_srcRelList.
	// Add m_srcRelList relations from entities on our m_dstRelList. 
	// Reverses changes made by disconnectEdgesJustMe
	
	private EntityInstance reconnectEdgesJustMe(EntityInstance me)
	{
		Vector				srcRelList    = me.getSrcRelList();
		Vector				dstRelList	  = me.getDstRelList();
		RelationInstance	ri;
		EntityInstance		other, parent;
		int					i;

		if (srcRelList != null) {
			for (i = srcRelList.size(); --i >= 0; ) {
				ri    = (RelationInstance) srcRelList.elementAt(i);
				other = ri.getDst();
				if (other != me) {
					if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
						ri     = other.getContainedByRelation();
						parent = ri.getSrc();
						signalEntityParentChanged(other, parent, TaListener.ENTITY_CUTTING_SIGNAL);
						parent.removeContainment(other);
						parent.removeSrcRelation(ri);
						ri.setSrc(me);
						srcRelList.setElementAt(ri, i);
						me.addContainment(other);
						prepostorder();
						signalEntityParentChanged(other, parent, TaListener.ENTITY_CUT_SIGNAL);
					} else {
						other.addDstRelation(ri);
		}	}	}	}

		parent = null;

		if (dstRelList != null) {
			for (i = dstRelList.size(); --i >= 0; ) {
				ri    = (RelationInstance) dstRelList.elementAt(i);
				other = ri.getSrc();
				if (other != me) {
					other.addSrcRelation(ri);
					if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
						parent = other;
		}	}	}	}

		return parent;
	}

	// Lift active relation in e into parent of e

	protected void liftRelations(EntityInstance e, EntityInstance parent)
	{
		Vector				srcRelList    = e.getSrcRelList();
		Vector				dstRelList    = e.getDstRelList();
		int					i;
		RelationInstance	ri;
		RelationClass		rc;

		if (parent == m_rootInstance) {
			parent = null;
		}

		if (dstRelList != null) {
			for (i = dstRelList.size(); --i >= 0; ) {
				ri = (RelationInstance) dstRelList.elementAt(i);
				rc = ri.getRelationClass();
				if (rc.isActive() && !ri.isMarked(RelationInstance.SPANNING_MARK)) {
					if (parent == null || ri.getSrc() == parent) {
//						System.out.println(e + " cutting dst relation " + ri);
						cutRelation(ri);
					} else {
//						System.out.println(e + " moving dst relation " + ri + " to " + parent);
						moveRelationDst(ri, parent);
					}
		}	}	}

		if (srcRelList != null) {
			for (i = srcRelList.size(); --i >= 0; ) {
				ri = (RelationInstance) srcRelList.elementAt(i);
				rc = ri.getRelationClass();
				if (rc.isActive() && !ri.isMarked(RelationInstance.SPANNING_MARK)) {
					if (parent == null || ri.getDst() == parent) {
//						System.out.println(e + " cutting src relation " + ri);
						cutRelation(ri);
					} else {
//						System.out.println(e + " moving srct relation " + ri + " to " + parent);
						moveRelationSrc(ri, parent);
					}
		}	}	}
	}

	// We are cutting this entity

	public boolean cutEntity(EntityInstance e)
	{
		if (e == m_rootInstance) {
			error("Can't cut the root node in the diagram");
			return false;
		}

		EntityInstance parent = e.getContainedBy();

		if (m_drawRoot != null && e.hasDescendantOrSelf(m_drawRoot)) {
			signalEntityParentChanged(e, parent, TaListener.DRAWROOT_CUTTING_SIGNAL);
		}

		signalEntityParentChanged(e, parent, TaListener.ENTITY_CUTTING_SIGNAL);


		// Hide all edges out of whats cut
		disconnectEdges(e, e);

		// Remove our image from the diagram
		parent.removeContainment(e);

		signalEntityParentChanged(e, parent, TaListener.ENTITY_CUT_SIGNAL);
		return true;
	}

	public void pasteEntity(EntityInstance parent, EntityInstance e)
	{
		RelationInstance ri;

		if (parent == null) {
			ri     = null;
			parent = e.getContainedBy();
		} else {
			ri = e.getContainedByRelation();
			ri.setSrc(parent);
		}
		reconnectEdges(e);
		prepostorder();
		// Add our image into the diagram
		if (parent != null) {
			parent.addContainment(e);
		}
		signalEntityParentChanged(e, parent, TaListener.ENTITY_PASTED_SIGNAL);
	}

	public boolean moveEntityContainment(EntityInstance parent, EntityInstance e)
	{
		RelationInstance	ri            = e.getContainedByRelation();
		EntityInstance		oldParent     = ri.getSrc();

		if (oldParent == parent) {
			return false;
		}
		signalEntityParentChanged(e, oldParent, TaListener.ENTITY_RELOCATING_SIGNAL);

		oldParent.removeContainment(e);
		oldParent.removeSrcRelation(ri);
		ri.setSrc(parent);
		parent.addSrcRelation(ri);
		parent.addContainment(e);
		prepostorder();
		signalEntityParentChanged(e, parent, TaListener.ENTITY_RELOCATED_SIGNAL);
		return true;
	}
		
	// Place the node e under cluster container
	// N.B. e is not in the TA hierarchy

	public EntityInstance clusterEntity(EntityInstance container, EntityInstance e /* In other TA */)
	{
		EntityInstance		ret;
		RelationInstance	ri;
		EntityClass			ec = e.getEntityClass();	// In other TA

		if (ec != null) {
			// Get corresponding entityClass in our TA
			ec = getEntityClass(ec.getId());		
		}

		if (ec == null) {
			ec = m_defaultEntityClass;
			if (ec == null) {
				ec = m_entityBaseClass;
		}	}

		String	ename = e.getId();
		if (entityExists(ename)) {
			int				n;
			for (n = 0; ; ++n) {
				ename = "Entity#" + n;
				if (!entityExists(ename)) {
					break;
		}	}	}
		ret = newCachedEntity(ec, ename);
		ri  = addEdge(getPrimaryContainsClass(), container, ret);
		ret.setContainedByRelation(ri);
		if (e.xRelLocal() >= 0 && e.yRelLocal() >= 0 && e.widthRelLocal() > 0 && e.heightRelLocal() > 0) {
			ret.setRelLocal(e);
		}
		signalEntityParentChanged(ret, container, TaListener.ENTITY_NEW_SIGNAL);
		return(ret);
	}
 
	// Used by ClusterInterface to move an entity from one TA to another
	// Container is the container the entity is to be contained in
	// match is the entity in my TA

	public EntityInstance importEntity(EntityInstance container /* My TA */, EntityInstance e /* Other TA */, EntityInstance match /* Corresponding thing in my TA */)
	{
		EntityClass	ec = e.getEntityClass();	// In other TA

		if (ec != null) {
			ec = getEntityClass(ec.getId());
		}
		if (ec != null) {
			match.setParentClass(ec);
		}
		moveEntityContainment(container, match);
		if (e.xRelLocal() >= 0 && e.yRelLocal() >= 0 && e.widthRelLocal() > 0 && e.heightRelLocal() > 0) {
			match.setRelLocal(e);
		}
		return(match);
	}

	// We are cutting this container but not its contents
	// May not cut the root of the forest

	public boolean deleteContainer(EntityInstance e)
	{
		EntityInstance parent;

		if (e == m_rootInstance) {
			error("Can't cut the root node container in the graph");
			return false;
		}

		parent = e.getContainedBy();

		signalEntityParentChanged(e, parent, TaListener.CONTAINER_CUTTING_SIGNAL);

		if (m_drawRoot != null && e.hasDescendantOrSelf(m_drawRoot)) {
			signalEntityParentChanged(e, parent, TaListener.DRAWROOT_CUTTING_SIGNAL);
		}

		disconnectEdgesJustMe(e, parent);
		markDeleted(e);

		signalEntityParentChanged(e, parent, TaListener.CONTAINER_CUT_SIGNAL);
		return true;
	}

	public void undeleteContainer(EntityInstance e)
	{
		EntityInstance parent;

		parent = reconnectEdgesJustMe(e);
		clearDeleted(e);
		signalEntityParentChanged(e, parent, TaListener.CONTAINER_PASTED_SIGNAL);
	}

	// --------------------
	// LandscapeClassObject
	// --------------------

	public void setInherits(LandscapeClassObject object, Vector value)
	{
		object.setInherits(value);
		signalObjectChanged(object, TaListener.INHERITS_SIGNAL);
	}

	/* EntityClass operations */

	public void removeEntityClass(EntityClass ec)
	{
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
						relationList.removeElementAt(i);
		}	}	}	}

		if (m_defaultEntityClass == ec) {
			setDefaultEntityClass(m_entityBaseClass);
		}
		m_entityClasses.remove(ec.getId());
		signalEntityClassChanged(ec, TaListener.EC_DELETE_SIGNAL);
	}

	// -----------
	// EntityClass
	// -----------

	public EntityClass newEntityClass(String id, EntityClass baseClass)
	{
		EntityClass ec = addEntityClass(id);

		ec.addParentClass(baseClass);
		signalEntityClassChanged(ec, TaListener.EC_NEW_SIGNAL);
		return ec;
	}

	public void unRemoveEntityClass(EntityClass ec, Vector eps, EntityClass oldDefaultEntityClass)
	{
		m_entityClasses.put(ec.getId(), ec);
		
		if (eps != null) {
			EntityClassPair	ep;
			int				i;

			for (i = eps.size(); i > 0; ) {
				ep = (EntityClassPair) eps.elementAt(--i);
				ep.m_rc.addRelationConstraint(ep);
		}	}

		if (m_defaultEntityClass != oldDefaultEntityClass) {
			setDefaultEntityClass(oldDefaultEntityClass);
		}
		signalEntityClassChanged(ec, TaListener.EC_UNDELETE_SIGNAL);
	}

	public void setImage(EntityClass ec, int image)
	{
		ec.setImage(image);
		signalEntityClassChanged(ec, TaListener.EC_IMAGE_SIGNAL);
	}

	public void setUnscaledIconFile(EntityClass ec, String file)
	{
		ec.setUnscaledIconFile(file);
		signalEntityClassChanged(ec, TaListener.EC_ICON_SIGNAL);
	}

	public void setAngle(EntityClass ec, double angle)
	{
		int	style = ec.getInheritedStyle();

		ec.setAngle(angle);

		if (style >= EntityClass.ENTITY_STYLE_TRIANGLE && style <= EntityClass.ENTITY_STYLE_20SIDED) {
			// Changing the angle is irrelevant for other styles at present
			signalEntityClassChanged(ec, TaListener.EC_ANGLE_SIGNAL);
		}
	}

	// -------------
	// RelationClass
	// -------------

	public RelationClass newRelationClass(String id, RelationClass baseClass)
	{
		RelationClass rc = addRelationClass(id);

		rc.addParentClass(baseClass);
		signalRelationClassChanged(rc, TaListener.RC_NEW_SIGNAL);

		return(rc);
	}

	public void removeRelationClass(RelationClass rc)
	{
		if (m_defaultRelationClass == rc) {
			setDefaultRelationClass(m_relationBaseClass);
		}
		m_relationClasses.remove(rc.getId());
		signalRelationClassChanged(rc, TaListener.RC_DELETE_SIGNAL);
	}

	public void unRemoveRelationClass(RelationClass rc, RelationClass defaultClass)
	{
		m_relationClasses.put(rc.getId(), rc);
		if (m_defaultRelationClass != defaultClass) {
			setDefaultRelationClass(defaultClass);
		}
		signalRelationClassChanged(rc, TaListener.RC_UNDELETE_SIGNAL);
	}

	public void setArrowColor(RelationClass object, Color value)
	{
		object.setArrowColor(value);
	}

	public void setIOfactor(RelationClass rc, short value)
	{
		Diagram	diagram = getDiagram();

		rc.setIOfactor(value);

		if (diagram != null) {
			changeIOfactor(rc);
		}

		signalRelationClassChanged(rc, TaListener.RC_IOFACTOR_SIGNAL);
	}

	/* Clipboard operations */

	public void setClipboardListener(ClipboardListener clipboardListener)
	{
		m_clipboardListener = clipboardListener;
	}

	public Clipboard getClipboard()
	{
		return m_clipboard;
	}

	protected void setClipboard(Clipboard value)
	{
		m_clipboard = value;
		if (m_clipboardListener != null) {
			m_clipboardListener.clipboardChanged();
	}	}

	public boolean cutClipboard(Clipboard clipboard)
	{
		boolean ok = !clipboard.contains(m_rootInstance);

		if (!ok) {
			error("Can't cut the root node in the diagram");
		} else {
			int					i, size;
			EntityInstance		e;

			size    = clipboard.size();
			for (i = 0; i < size; ++i) {
				// Cut children and then ancestors
				e = (EntityInstance) clipboard.elementAt(i);
				cutEntity(e);
			}
			prepostorder();		// Can do at end when cutting
			setClipboard(clipboard);
		}
		return(ok);
	}

	public void pasteClipboard(Clipboard clipboard, EntityInstance container)
	{
		ClipboardEnumerator	en;
		EntityInstance		e;

		// Paste all elements in the composite clipboard
		for (en = clipboard.clipboardElements(); en.hasMoreElements(); ) {
			e = (EntityInstance) en.nextElement();
			pasteEntity(container, e);	// Uncached
		}
		// Erase current clipboard
		setClipboard(null);
	}

	public void unPasteClipboard(Clipboard saved_clipboard)
	{
		ClipboardEnumerator	en;
		EntityInstance		e;
		RelationInstance	ri;
		EntityInstance		container;
		int					i;

		i       = 0;
		for (en = saved_clipboard.clipboardElements(); en.hasMoreElements(); ++i) {
			// N.B. order irrelevant when pasting and unpasting -- all under same parent
			e = (EntityInstance) en.nextElement();
			cutEntity(e);
			container = en.oldContainer();

			ri = e.getContainedByRelation();
			if (ri.getSrc() != container) {
				// Must change container for e back to what it was
				ri.setSrc(container);
		}	}
		setClipboard(saved_clipboard);
		prepostorder();		// Can do at end when cutting
	}

}

