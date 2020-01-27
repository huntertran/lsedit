package lsedit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.util.Enumeration;
import java.util.Vector;

import java.net.URL;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;

import javax.swing.undo.UndoableEdit;

class Zoom extends MyUndoableEdit implements UndoableEdit
{
	Diagram	m_diagram;
	double	m_oldX, m_oldY;
	double	m_newX, m_newY;

	Zoom(Diagram diagram, double oldx, double oldy, double newx, double newy)
	{
		m_diagram   = diagram;
		m_oldX      = oldx;
		m_oldY      = oldy;
		m_newX      = newx;
		m_newY      = newy;
	}
	
	public String getPresentationName() 
	{
		return " Zoom diagram";
	}

	protected void changeTo(double x, double y)
	{
		m_diagram.setZoom(x, y);
	}

	public void undo()
	{
		changeTo(m_oldX, m_oldY);
	}

	public void redo()
	{
		changeTo(m_newX, m_newY);
	}
}	

class RelayoutSubtree extends MyUndoableEdit implements UndoableEdit
{
	// This is probably the cheapest storage cost
	// We have to cache and uncache the whole operation without
	// intermediate redraws since the -1's will be corrected by a redrawer
	// before we are finished undoing..

	Diagram				m_diagram;
	EntityInstance[]	m_entities;
	double[]			m_xRel;
	double[]			m_yRel;
	double[]			m_widthRel;
	double[]			m_heightRel;
	LandscapeLayouter	m_layouter;
	boolean				m_compute;

	protected void saveInfo(EntityInstance e, int basePreorder)
	{
		{
			int	index          = e.getPreorder() - basePreorder;
		
			m_entities[index]  = e;
			m_xRel[index]      = e.xRelLocal();
			m_yRel[index]      = e.yRelLocal();
			m_widthRel[index]  = e.widthRelLocal();
			m_heightRel[index] = e.heightRelLocal();
		}

		Enumeration		en;
		EntityInstance	e1;

		for (en = e.getChildren(); en.hasMoreElements(); ) {
			e1 = (EntityInstance) en.nextElement();
			saveInfo(e1, basePreorder);
	}	}

	RelayoutSubtree(Diagram diagram, EntityInstance container, boolean compute)
	{
		int	need = container.nodesInSubtree();

		m_diagram   = diagram;
		m_entities  = new EntityInstance[need];
		m_xRel      = new double[need];
		m_yRel      = new double[need];
		m_widthRel  = new double[need];
		m_heightRel = new double[need];
		m_layouter  = diagram.getLs().getLayouter();
		m_compute   = compute;

		saveInfo(container, container.getPreorder());
	}

	public String getPresentationName() 
	{
		return "Relayout subtree " + m_entities[0];
	}

	public void undo()
	{
		Diagram				diagram   = m_diagram;
		EntityInstance[]	entities  = m_entities;
		double[]			xRel      = m_xRel;
		double[]			yRel      = m_yRel;
		double[]			widthRel  = m_widthRel;
		double[]			heightRel = m_heightRel;
		int					length    = entities.length;
		int					i;
		EntityInstance		e;

		for (i = 1; i < length; ++i) {
			e = entities[i];
			// Not all entities exist if we do a lazy delete of an entity
			// This is because be don't preorder in such situations
			if (e != null) {
				e.setRelLocal(xRel[i], yRel[i], widthRel[i], heightRel[i]);
		}	}
		diagram.getLs().setLayouter(m_layouter);
	}

	public void redo()
	{
		Diagram				diagram   = m_diagram;
		EntityInstance[]	entities  = m_entities;
		double[]			widthRel  = m_widthRel;
		double[]			heightRel = m_heightRel;
		int					length    = entities.length;
		int					i;
		EntityInstance		e;

		for (i = 1; i < length; ++i) {
			e = entities[i];
			if (e != null) {
				// Not all entities exist if we do a lazy delete of an entity
				// This is because be don't preorder in such situations
				e.setRelLocal(-1, -1, widthRel[i], heightRel[i]);

				// TODO: handle m_compute
		}	}
		diagram.getLs().setLayouter(m_layouter);
	}
}	

class ExitFlag extends JComponent implements MouseListener
{
	protected final static int		EXIT_FLAG_DIM = 8;
	protected int					m_priorcursor = -1;
	protected LandscapeEditorCore	m_ls;

	public ExitFlag(LandscapeEditorCore	ls)
	{
		super();
		m_ls = ls;
		addMouseListener(this);
	}

	public void setLocation(int x, int y)
	{
		setBounds(x, y, EXIT_FLAG_DIM, EXIT_FLAG_DIM);
	}

	public void activate()
	{
		setVisible(true);
	}

	public void paintComponent(Graphics g)
	{
//		System.out.println("Diagram.ExitFlag.paintComponent()");

		super.paintComponent(g);

		/*  Draw a small mark as shown in top left of object

			x---
			|
			|   --------
			|  |        |
			   |        |
			   |        |
	  		   |  ----  |
			   |        |
			   |        |
			   |        |
			   |________|
		*/

		g.setColor(Diagram.boxColor.darker());
		g.drawRect(0, 0, EXIT_FLAG_DIM, EXIT_FLAG_DIM);
		// g.setColor(Color.black);
		g.drawLine(2, 4, 6, 4);
//		System.out.println("Diagram.paintComponent() done");
	}

	// MouseListener interface

	public void mouseClicked(MouseEvent ev)	
	{
		LandscapeEditorCore ls = m_ls;

		if (m_priorcursor != -1) {
			ls.setCursor(m_priorcursor); 
			m_priorcursor = -1;
		}
		ls.navigateToDrawRootParent();
	}

	public void mouseEntered(MouseEvent e)
	{
		m_priorcursor = m_ls.setCursor(Cursor.HAND_CURSOR);
//		System.out.println("m_priorcursor=" + m_priorcursor);
	}

	public void mouseExited(MouseEvent e)
	{
		if (m_priorcursor != -1) {
//			System.out.println("MouseExited " + m_priorcursor);
			m_ls.setCursor(m_priorcursor); 
			m_priorcursor = -1;
	}	}

	public void mousePressed(MouseEvent ev)
	{
	}

	public void mouseReleased(MouseEvent ev)
	{
	}
}

public final class Diagram extends TemporalTa /* extends UndoableTa extends Ta extends JPanel */ implements TaListener, MouseMotionListener  
{
	// Final values

	public final static int   BG              = 191;				// Was 0.75 -> 255 * 0.75 = 191.25
	public final static Color boxColor        = Color.lightGray;
	public final static Color lighterBoxColor = new Color(0xe0e0e0);

	protected final static int GAP            = 5;
	protected final static int MARGIN         = GAP*2;

	protected final static double SMALL_SCALE_UP		= 1.2; 
	protected final static double SMALL_SCALE_DOWN		= 0.8; 
	protected final static String SMALL_SCALE_STRING	= "20%";

	// Values

	protected LandscapeEditorCore m_ls;
	protected Option			  m_diagramOptions;

	protected int		m_view_x = 0;
	protected int		m_view_y = 0;

	/* By putting the edges in a separate container we simplify the management of painting
		(1) We ensure that edges are painted after entities. This is because the m_edges
			components is added first.
		(2) We ensure that adding and deleting edges is efficient.
			This is because we can add at end.
	 */

	protected ExitFlag			m_exitFlag;
	protected Container			m_cardinals = null;
	protected Container			m_edges       = null;
	protected Container			m_edge_labels = null;
	protected SupplierSet		m_supplierSet;
	protected ClientSet			m_clientSet;	

	protected int				m_numVisibleRelationClasses = -1;

	protected boolean			m_loaded        = false;

	protected boolean			m_visibleEdges = true;
	protected boolean			m_drawEntities = true;		/* True then draw entities normally -- false draw only highlight entities */
	protected boolean			m_drawEdges    = true;		/* True then draw edges normally -- false draw only highlight edges */
	protected boolean			m_viewActive   = false;		/* True if we are showing a partially hidden view */
	
	protected EntityInstance	m_keyEntity;				// The currently highlight entity if any else null
	private	  Vector			m_groupedEntities    = new Vector();
	private	  Vector			m_groupedRelations   = new Vector();
	private	  Vector			m_redBoxEntities     = new Vector();
	private	  Vector			m_highlightRelations = new Vector();
	
	protected EntityInstance	m_chaseEntity        = null;	// The entity to be chased if any

	protected boolean			m_modeHandlingActive = false;

	protected Clipboard			m_clipboard = null;

	protected Vector			m_oldChildren;

	protected ClusterMetrics	m_clusterMetrics = null;
	
	private	  int				m_flags = 0;

	private final static int	REFILL_FLAG			= 0x01;
	private final static int	REPAINT_FLAG		= 0x02;
	private final static int	REFILL_EDGES_FLAG   = 0x04;
	private final static int	RESHADE_FLAG		= 0x08;
	private final static int	PAINT_FLAGS			= REFILL_FLAG | REPAINT_FLAG | REFILL_EDGES_FLAG | RESHADE_FLAG;

	private	final static int	SHOWS_CLIENTS_FLAG	 = 0x10;
	private final static int	SHOWS_SUPPLIERS_FLAG = 0x20;
	private final static int	FILL_FLAGS           = SHOWS_CLIENTS_FLAG | SHOWS_SUPPLIERS_FLAG;

	// ------------------
	// JComponent methods
	// ------------------

/*
	public void revalidate()
	{
		System.out.println("Revalidate");
		super.revalidate();
	}

	public void invalidate()
	{
		System.out.println("Invalidate");
		super.invalidate();
	}
*/

	// -----------------
	// Protected methods
	// -----------------

	protected Diagram getDiagram()
	{
		// Hide the TA layer from having to know what the diagram is
		// While still being able to pass it into its own functions
		return(this);
	}
	
	protected boolean isSpecialPath(String path)
	{
		return m_ls.isSpecialPath(path);
	}
	
	protected String parseSpecialPath(Ta ta, String path)
	{
		return m_ls.parseSpecialPath(ta, path);
	}

	protected void setVisibilityFlags() 
	{
		Enumeration en;

		m_numVisibleRelationClasses = 0;

		for (en = enumRelationClasses(); en.hasMoreElements(); ) {
			RelationClass rc = (RelationClass) en.nextElement();
			if (rc.isShown()) {
				rc.setOrdinal(m_numVisibleRelationClasses++);
			}
		}
		m_visibleEdges = (m_numVisibleRelationClasses > 0);
	}
	
	public void invalidateVisibleRelationClasses()
	{
		m_numVisibleRelationClasses = -1;
	}

/* 
	What a cautionary tale this is for a software engineering course..

	1. Somewhere in Java AWT's history someone had the bright idea of changing
	   the AWT framework so that every component could have a mouse cursor
	   associated with it instead of just the frame managing what the current
	   mouse cursor object should be at any given time.  The idea was that as
	   you moved the mouse over different objects each object would have the
	   capability of explicitly indicating what the cursor associated with the
	   mouse should be, while it hovered over that object.

	2. This means that when ever a new visible object is added to a container,
	   AWT must handle the possibility that the cursor happens to be over this
	   new object, in which case this new object should if appropriate change
	   the appearance of that mouse cursor immediately.

	3. So whenever an object is added to the AWT containment hierarchy, there
	   is an invocation to that object to change the system cursor immediately.

	   [This by the way is a less than smart implementation.. The smart thing
		to tell the component is change the cursor if the mouse pointer is over
		it and it actually cared what the cursor  should be in that unusual case]

	4. The object handles this request by walking all the way up the containment
	   heirarchy until it hits a component which actually cares what the cursor
	   looks like (in lsedit's case the topmost frame object, since tha ability
	   to associate cursors directly with objects is not used).

	5. This cursor isn't changed but the frame object doesn't know that. That
	   is because any component can over ride the frame's opinion as to what
	   the cursor should be (not that any does in lsedit).

	6. So the frame object starts off by asking itself what the mouse pointer
	   is over. To do this it walks back down through the containment heirarchy
	   asking each level if it contains the current point the mouse pointer is
	   over, recursively.  This comes close to search all components, if the
	   mouse pointer happens to be over the diagram, since entities and edges
	   dominate the number of components.

	7. Now containment is not a trivial exercise for edges.  At a minimum we
	   need to decide if the given point is within 'n' pixels of any part of
	   the line, the arrow head, etc.

	8. Having identified the deepest thing the mouse pointer is over, we walk up
	   finding the nearest component above which wishes to control the cursor
	   image, arriving back at the frame.  The frame then dutifully sets the
	   mouse cursor to what it already was.


	Now this is repeated every time we add a component.  So it is clearly an
	O(N^2) operation for N components.  So when displaying a large number of
	edges (order the number in linux or whatever) the cost of JAVA painting
	the cursor image associated with the mouse pointer, becomes prohibitive
	as the actual graph image is constructed line by line.

	I worked this out by narrowing down the performance problems when displaying
	a large ta file to: White cursor mouse arrow left on screen until lsedit
	draws = 40 seconds, while time taken was a mere 4 seconds if I cleverly fired
	up lsedit in a manner which left the cursor off the lsedit screen, somewhere
	down at the bottom of my actual screen.

	Needless to say I at first found this behaviour quite astonishing.

	Work around to correct this <<bug>> was to add to the diagram.java class
	the following special rule about what the diagram contains when not
	loaded -- nothing.

	Ian Davis
*/

	public String parameterDetails()
	{
		return m_ls.parameterDetails();
	}
	
	public boolean contains(int x, int y)
	{
		return (m_loaded && super.contains(x, y));
	}

/*
	// For debugging
	
	
    protected void paintChildren(Graphics g) 
    {
    	super.paintChildren(g);
    }

	// For debugging
	    	
    public void repaint() 
    {
    	super.repaint();
    }
 */
	// --------------
	// Public methods
	// --------------

	// This constructor is only called to create copies of diagrams
	// They are never active so don't need to assign them listeners

	public Component add(Component c)
	{
		if (c instanceof RelationComponent) {
			RelationLabel label = ((RelationComponent) c).getRelationLabel();
			if (label != null) {
				m_edge_labels.add(label);
			}
			return(m_edges.add(c));
		}
		if (c instanceof Cardinal) {
			return(m_cardinals.add(c));
		}
		return(super.add(c));
	}

	public void remove(Component c)
	{
		if (c instanceof RelationComponent) {
			RelationLabel label = ((RelationComponent) c).getRelationLabel();
			if (label != null) {
				m_edge_labels.remove(label);
			}
			m_edges.remove(c);
			return;
		}
		if (c instanceof Cardinal) {
			m_cardinals.remove(c);
			return;
		}
		super.remove(c);
	}

	public Diagram(LandscapeEditorCore ls) 
	{
		super(ls);

		m_cardinals = new Container();
		m_cardinals.setLayout(null);
		m_edges = new Container();
		m_edges.setLayout(null);
		m_edge_labels = new Container();
		m_edge_labels.setLayout(null);

		m_ls = ls;
		setDiagram(this);

		setLayout(null);
		setLocation(0, 0);

		m_exitFlag = new ExitFlag(ls);
		m_exitFlag.setLocation(3, 3);

		m_supplierSet    = new SupplierSet(this);
		m_clientSet      = new ClientSet(this);	// Ignore suppliers when computing clients

		m_diagramOptions = new Option("Active");
		m_diagramOptions.setTo(Options.getLandscapeOptions());
		Options.setDiagramOptions(m_diagramOptions);

		addMouseMotionListener(this);
		// The default empty instance

		// No marks are needed when the diagram is first drawn

		addTaListener(this, TaListener.ATTRIBUTELISTENER);
	}

	public LandscapeEditorCore getLs() 
	{
		return m_ls;
	}

	public Option getDiagramOptions()
	{
		return m_diagramOptions;
	}
	
	public void updateLiftEdges()
	{
		updateLiftEdges(m_drawRoot);
	}

	public void updateDeleteActiveEntities()
	{
		updateDeleteActiveEntities(m_drawRoot);
	}

	public void updateDeleteActiveContainers()
	{
		updateDeleteActiveContainers(m_drawRoot);
	}

	/* Remove all active edges under at under the draw root belonging to active entities
	 * possibly because this relation class needs to be deleted
	 */

	public void updateDeleteActiveEdges()
	{
		updateDeleteActiveEdges(m_drawRoot);
	}

	// Programatically delete marked things

	public void doDelete(Object object) 
	{
		Vector				objects;
		Object				o;
		Enumeration			en;
		RelationInstance	ri;
		EntityInstance		e;
		int					mode = 0;
		String				msg;


		objects = targetEntityRelations(object);
		if (objects != null) {
			beginUndoRedo("Delete");
			for (en = objects.elements(); en.hasMoreElements(); ) {
				o = en.nextElement();
				if (o != null) {
					if (o instanceof RelationInstance) {
						ri = (RelationInstance) o;
						mode |= 1;
						updateDeleteEdge(ri);
						continue;
					}
					if (o instanceof EntityInstance) {
						e = (EntityInstance) o;
						if (e.getContainedBy() == null) {
							continue;
						}
						updateCutEntity(e);
						mode |= 2;
						continue;
					}
					System.out.println("Can't delete object of type " + o.getClass());
			}	}
			endUndoRedo();
			if (mode != 0) {
				if ((mode & 2) != 0) {
					clearGroupFlags();
				}
		}	}

		switch (mode) {
		case 0:	
			msg = "Nothing selected to delete";
			break;
		case 1:
			msg = "Edge(s) deleted";
			break;
		case 2:
			msg = "Entity(s) deleted";
			break;
		case 3:
			clearGroupFlags();
			msg = "Entity(s) and Edge(s) deleted";
			break;
		default:
			msg = "???";
		}
		m_ls.doFeedback(msg);
	}

	protected void duplicateEdges(EntityInstance e, RelationClass oldRc, RelationClass newRc)
	{
		Vector			srcRelList = e.getSrcRelList();
		EntityInstance	dst;

		if (srcRelList != null) {
			int				 i, size;
			RelationInstance ri;

			size = srcRelList.size();
			for (i = 0; i < size; ++i) {
				ri = (RelationInstance) srcRelList.elementAt(i);
				if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
					dst = ri.getDst();
					
					// Traverse downwards
					// Give new relationclass the same positioning as the old
					dst.savePositioning(newRc);
					duplicateEdges(dst, oldRc, newRc);
				}
				if (oldRc == ri.getRelationClass()) {
					updateNewRelation(newRc, e, ri.getDst());
	}	}	}	}

	public void duplicateEdges(RelationClass oldRc, RelationClass newRc)
	{
		if (m_rootInstance != null) {
			Enumeration		en;
			EntityInstance	e;

			for (en = m_rootInstance.getChildren(); en.hasMoreElements(); ) {
				e = (EntityInstance) en.nextElement();
				duplicateEdges(e, oldRc, newRc);
	}	}	}
	
	public void changeContainsClasses(RelationClass[] relationClasses)
	{
		signalRelationClassChanged(null, TaListener.CONTAINS_CHANGING_SIGNAL); 

		clearEstablishForest(relationClasses);	// Performed as part of undo/redo
		switchContainsClasses(relationClasses);

		signalRelationClassChanged(null, TaListener.CONTAINS_CHANGED_SIGNAL);
	}
		
	public void updateContainsClasses(RelationClass[] relationClasses)
	{
		String			msg;
		
		signalRelationClassChanged(null, TaListener.CONTAINS_CHANGING_SIGNAL); 
		if (relationClasses.length == 0) {
			msg = "Removing containment";
		} else {
			msg = "Switch to " + relationClasses[0].getLabel();
			if (relationClasses.length > 1) {
				msg += " ... ";
		}	}
		msg += " heirarchy";
		beginUndoRedo(msg);

		clearEstablishForest(relationClasses);	// Switch contains heirarchy as requested by user
		updateSwitchContainsClasses(relationClasses); 
		signalRelationClassChanged(null, TaListener.CONTAINS_CHANGED_SIGNAL);
		endUndoRedo();
	}
	
	public void recomputeContainsClasses()
	{
		if (m_changed_spanning_edges) {
			m_changed_spanning_edges = false;
			changeContainsClasses(m_containsClasses);
	}	}

	private void loadDiagramOption(String attribute, String value)
	{
		if (attribute.equals("diagram:drawroot")) {
			if (m_ls.m_startEntity == null) {
				m_ls.m_startEntity = value;
			}
			return;
		}
		if (attribute.equals("diagram:viewx")) {
			int	ival = Util.parseInt(value);
			if (ival > 0) {
				m_view_x = ival;
			}
			return;
		}
		if (attribute.equals("diagram:viewy")) {
			int ival = Util.parseInt(value);
			if (ival > 0) {
				m_view_y = ival;
			}
			return;
		}
	}
	
	// This logic attempts to assign a Rectangle to any component which lacks Rectangle information

	// Called from landscapeEditorCore.attach()
	// Called from landscapeEditorCore.loadLs()
	// Called from landscapeEditorCore.initialLoad()

	// Returns non-null string on error

	public String loadDiagram(String taPath) 
	{
		LandscapeEditorCore	ls = m_ls;
		URL					documentBase;
		String				ret;

		ls.doFeedback("Loading: " + taPath);

		documentBase = ls.getDocumentBase();

		ret = loadTA(taPath, documentBase);
		if (ret == null) {
			Option			landscapeOptions = Options.getLandscapeOptions();
			Option			diagramOptions   = Options.getDiagramOptions();
			EntityInstance	optionsInstance;

			ls.addLseditHistory(taPath);
			diagramOptions.setTo(landscapeOptions);
			optionsInstance = m_rootInstance;
			m_view_x = 0;
			m_view_y = 0;
			if (optionsInstance != null) {
				Attribute[]		attributes = optionsInstance.m_attributes;
					
				if (attributes != null  && attributes.length > 0) {
					
					switch (diagramOptions.getLoadMode()) {
					case Option.LOAD_NO:
						break;
					case Option.LOAD_PROMPT:
						if (JOptionPane.showConfirmDialog(m_ls.getFrame(),
																"Do you wish to load the options contained in " + taPath + "?\n" +
																"You are being asked this question because your landscape\n" +
																"load TA option is currently set to \"Prompt\"",
																"Load options contained in this TA file",
																JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
							break;
						}
					default:
						Attribute	attribute;
						String		id, value;
						int			i;
					
						for (i = 0; i < attributes.length; ++i) {
							attribute = attributes[i];
							if (attribute == null) {
								continue;
							}
							value = attribute.externalString();
							if (value == null) {
								continue;
							}
							value = value.trim();
							id = attribute.m_id;
							if (id.startsWith("diagram:")) {
								loadDiagramOption(id, value);
								continue;
							}
							diagramOptions.loadOption(id, value);
			}	}	}	}	
			
			if (m_loaded_special) {
				ls.specialPathOptions(diagramOptions);
			}

			ls.changeTraceLifted(diagramOptions.isTraceLifted());
			ls.changeGroupQuery(diagramOptions.isGroupQuery());
			ls.changeQueryPersists(diagramOptions.isQueryPersists());
		}
		return ret;
	}

	public void cutGroup(Object object) 
	{
		Vector			vector;
		Clipboard		old_clipboard, new_clipboard;
		String			msg;
		boolean			addPriorCuts = m_ls.isAddToClipboard();

		TocBox			tocBox = m_ls.getTocBox();
		
		if (object != null && object == tocBox) {
			vector = tocBox.getTocGroupedEntities();
		} else {
			vector = getGroupedEntities();
		}

		if (vector == null) {
			m_ls.error("Group not selected");
			return;
		}

		if (vector.contains(m_rootInstance)) {
			m_ls.error("Can't cut the root node in the diagram");
		}

		beginUndoRedo("Cut group");

		// Don't want a subsequent paste to try and paste X into X

		clearAllEntityGroupFlags();

		new_clipboard = new Clipboard(vector);

		msg = "Group copied to clipboard";
		
		old_clipboard = getClipboard();
		if (old_clipboard != null && old_clipboard.size() != 0) {
			if (addPriorCuts) {
				new_clipboard.setExtendsClipboard(old_clipboard);
				msg += " - old cuts preserved";
			} else {
				msg += " - old cuts discarded";
		}	}
		updateCutClipboard(old_clipboard, new_clipboard);

		endUndoRedo();
		m_ls.doFeedback(msg);
	}
	
	public void pasteGroup(Object object) 
	{
		Clipboard clipboard = getClipboard();

		if (clipboard == null) {
			m_ls.error("Clipboard empty");
			return;
		}

		TocBox			tocBox = m_ls.getTocBox();
		EntityInstance	pe;

		if (object != null && object == tocBox) {
			pe = tocBox.targetEntity();
		} else {
			pe = targetEntity(object);
		}
		if (pe != null) {
			String		label = pe.getEntityLabel();

			beginUndoRedo("Paste to " + label);
			updatePasteClipboard(clipboard, pe);
			endUndoRedo();
			m_ls.doFeedback("Pasted entities into " + label);
	}	}	

	public EntityInstance getDrawRootParent()
	{
		return (m_drawRoot.getContainedBy());
	}


	public void setDrawRoot(EntityInstance e)
	{
		EntityInstance old    = m_drawRoot;

		if (e != old) {

			if (old != null) {
				old.nandMark(EntityInstance.DRAWROOT_MARK);
				old.setToolTipText();
			}

			m_drawRoot = e;

			if (e != null) {
				e.orMark(EntityInstance.DRAWROOT_MARK);
				e.setToolTipText();
				e.setOpen();

				m_ls.addHistoryEntity(e);
			}
			m_ls.mapboxChanged(e);
	}	}

	public void toggleDstElision(EntityInstance e) 
	{
		e.toggleElision(EntityInstance.DST_ELISION, enumRelationClasses()); 
	}

	public void toggleSrcElision(EntityInstance e) 
	{
		e.toggleElision(EntityInstance.SRC_ELISION, enumRelationClasses()); 
	}

	public void toggleEnteringElision(EntityInstance e) 
	{
		e.toggleElision(EntityInstance.ENTERING_ELISION, enumRelationClasses());
	}

	public void toggleExitingElision(EntityInstance e) 
	{
		e.toggleElision(EntityInstance.EXITING_ELISION, enumRelationClasses());
	}

	public void toggleInternalElision(EntityInstance e) 
	{
		e.toggleElision(EntityInstance.INTERNAL_ELISION, enumRelationClasses());
	}

	public void setVisible(boolean value)
	{
		if (m_drawRoot != null) {
			m_drawRoot.setVisible(value);
	}	}
	
	public void setViewActive()
	{
		m_viewActive = true;
	}
	
	// ----------------
	// Grouping methods
	// ----------------

	public EntityInstance getKeyEntity() 
	{
		return m_keyEntity;
	}

	public int getGroupedEntitiesCount()
	{
		return m_groupedEntities.size();
	}

	public EntityInstance getGroupedEntitiesContainer()
	{
		EntityInstance fe = (EntityInstance) m_groupedEntities.firstElement();

		return fe.getContainedBy();
	}

	public Vector getGroupedEntities() 
	{
		if (m_rootInstance != null) {
			Vector groupedEntities    = m_groupedEntities;

			if (!groupedEntities.isEmpty()) {
				return (Vector) groupedEntities.clone();
		}	}
		return null;
	}

	public boolean clearEntityGroupFlag(EntityInstance e)
	{
		if (e.getGroupFlag()) {
			Vector groupedEntities    = m_groupedEntities;
			EntityInstance	keyEntity = m_keyEntity;

	 		e.nandMark(EntityInstance.GROUP_MARK|EntityInstance.GROUPKEY_MARK);
//			System.out.println("ClearEntityGroupFlag " + e);

			groupedEntities.removeElement(e);
			if (e == keyEntity) {
				if (groupedEntities.isEmpty()) {
					m_keyEntity = null;
				} else {
					m_keyEntity = keyEntity = (EntityInstance) groupedEntities.firstElement();
					keyEntity.orMark(EntityInstance.GROUPKEY_MARK);
					keyEntity.repaint();
			}	}
			return true;
		}
		return false;
	}

	public void clearEntityClassGroupFlags(EntityClass ec)
	{
		Vector			groupedEntities = m_groupedEntities;
		EntityInstance	e;
		EntityClass		ec1;
		int				i;

		for (i = groupedEntities.size(); --i >= 0; ) {
			// Remove anything now hidden from the selected group 
			e   = (EntityInstance) groupedEntities.elementAt(i);
			ec1 = e.getEntityClass();
			if (ec1 == ec) {
				clearEntityGroupFlag(e);
		}	}
	}

	public void clearAllEntityGroupFlags() 
	{
		Vector			groupedEntities = m_groupedEntities;
		EntityInstance	e;
		int				i;

		for (i = groupedEntities.size(); --i >= 0; ) {
			e = (EntityInstance) groupedEntities.elementAt(i);
//			System.out.println("ClearAllEntityGroupFlags " + e);

	 		e.nandMark(EntityInstance.GROUP_MARK|EntityInstance.GROUPKEY_MARK);
			e.repaint();
		}
		groupedEntities.removeAllElements();
		m_keyEntity = null;
	}

	protected void setEntityGroupFlag(EntityInstance e) 
	{
		if (!e.isMarked(EntityInstance.GROUP_MARK)) {
			if (e.isShown()) {
				e.orMark(EntityInstance.GROUP_MARK);
				m_groupedEntities.addElement(e);

				if (m_keyEntity == null) {
					m_keyEntity = e;
					e.orMark(EntityInstance.GROUPKEY_MARK);
				}
//				System.out.println("SetEntityGroupFlag " + e);

				e.repaint(); 
	}	}	}

	public void setKeyEntity(EntityInstance e) 
	{
		if (e != m_keyEntity) {
			if (e.isShown()) {
				if (!e.isMarked(EntityInstance.GROUP_MARK)) {
					m_groupedEntities.addElement(e);
				}
				if (m_keyEntity != null) {
					m_keyEntity.nandMark(EntityInstance.GROUPKEY_MARK);
					m_keyEntity.repaint();
				}
				e.orMark(EntityInstance.GROUP_MARK | EntityInstance.GROUPKEY_MARK);
				m_keyEntity = e;
//				System.out.println("SetKeyEntity " + e);

				e.repaint();
	}	}	}

	public void clearKeyEntity()
	{
		Vector			groupedEntities = m_groupedEntities;
		EntityInstance	keyEntity       = m_keyEntity;
		EntityInstance	other;
		int				i;

		if (keyEntity != null) {
			for (i = groupedEntities.size(); --i >= 0;) {
				other = (EntityInstance) groupedEntities.elementAt(i);
				if (other != keyEntity) {
					keyEntity.nandMark(EntityInstance.GROUPKEY_MARK);
					other.orMark(EntityInstance.GROUPKEY_MARK);
//					System.out.println("ClearKeyEntity " + keyEntity + " -> " + other);

					m_keyEntity = other;
					other.repaint();
					return;
			}	}
//			System.out.println("ClearKeyEntity " + keyEntity);
		}
		clearEntityGroupFlag(keyEntity);
	}

	public void groupRedBoxes(boolean clear) 
	{
		LandscapeEditorCore	ls       = m_ls;
		Vector				redboxes = getRedboxEntities();
		int					i        = ((redboxes == null) ? 0 : redboxes.size());

		if (i == 0) {
			ls.error("No query result (highlighted red boxes) active.");
		} else {
			EntityInstance	e;

			ls.doFeedback("Grouped " + i + " redboxes");
			if (clear) {
				clearGroupFlags();
			}
			while (0 <= --i) { 
				e = (EntityInstance) redboxes.elementAt(i);
				if (e.isMarked(EntityInstance.DIAGRAM_MARK)) {
					setEntityGroupFlag(e);
	}	}	}	}
	
	// -------------------------
	// Grouped relations methods
	// -------------------------

	public Vector getGroupedRelations()
	{
		if (m_rootInstance != null) {
			Vector	groupedRelations = m_groupedRelations;
			if (!groupedRelations.isEmpty()) {
				return (Vector) groupedRelations.clone();
		}	}
		return null;
	}
		
	public void setRelationGroupFlag(RelationInstance ri)
	{
		if (!ri.getGroupFlag()) {
//			System.out.println("setRelationGroupFlag " + ri);
			m_groupedRelations.addElement(ri);
			ri.orMark(RelationInstance.GROUP_FLAG_MARK);
			ri.orEmbellished(RelationComponent.DRAW_CENTRE_MARK);
	}	}

	public void clearRelationGroupFlag(RelationInstance ri) 
	{
		if (ri.getGroupFlag()) {
//			System.out.println("clearRelationGroupFlag " + ri);
			m_groupedRelations.removeElement(ri);
			ri.nandMark(RelationInstance.GROUP_FLAG_MARK);
			ri.nandEmbellished(RelationComponent.DRAW_CENTRE_MARK);
	}	}

	public void clearRelationClassGroupFlags(RelationClass rc)
	{
		Vector				groupedRelations = m_groupedRelations;
		RelationInstance	ri;
		RelationClass		rc1;
		int					i;

		for (i = groupedRelations.size(); --i >= 0; ) {
			// Remove anything now hidden from the selected group 
			ri  = (RelationInstance) groupedRelations.elementAt(i);
			rc1 = ri.getRelationClass();
			if (rc1 == rc) {
				clearRelationGroupFlag(ri);
		}	}
	}

	public void clearAllRelationGroupFlags() 
	{
		Vector				groupedRelations = m_groupedRelations;
		RelationInstance	ri;
		int					i;

		for (i = groupedRelations.size(); --i >= 0; ) {
			ri = (RelationInstance) groupedRelations.elementAt(i);
//			System.out.println("ClearAllRelationGroupFlags " + ri);

	 		ri.nandMark(RelationInstance.GROUP_FLAG_MARK);
			ri.nandEmbellished(RelationComponent.DRAW_CENTRE_MARK);
		}
		groupedRelations.removeAllElements();
	}

	// -----------------
	// Redbox operations
	// -----------------

	public Vector getRedboxEntities() 
	{

		if (m_rootInstance != null) {
			Vector redboxEntities    = m_redBoxEntities;

			if (!redboxEntities.isEmpty()) {
				return (Vector) redboxEntities.clone();
		}	}
		return null;
	}

	public void setTracedFlag(EntityInstance e)
	{
		if (!e.isMarked(EntityInstance.TRACED_MARK)) {
			m_redBoxEntities.addElement(e);
			e.orMark(EntityInstance.TRACED_MARK);
			e.nandMark(EntityInstance.HIDDEN_MARK);
	}	}
	
	public void setRedBoxFlag(EntityInstance e)
	{
		if (!e.isMarked(EntityInstance.REDBOX_MARK)) {
//			System.out.println("setRedboxFlag " + e);
			setTracedFlag(e);
			e.orMark(EntityInstance.REDBOX_MARK);
			e.repaint();
	}	}

	public boolean clearAllRedBoxFlags() 
	{
		Vector				redBoxEntities = m_redBoxEntities;
		EntityInstance		e;
		int					i, size;

		size = redBoxEntities.size();
		if (size > 0) {
			for (i = size; --i >= 0; ) {
				e = (EntityInstance) redBoxEntities.elementAt(i);
//				System.out.println("ClearAllRedBoxFlags " + e);

	 			e.nandMark(EntityInstance.REDBOX_MARK | EntityInstance.TRACED_MARK);
				e.repaint();
			}
			redBoxEntities.removeAllElements();
			return true;
		}
		return false;
	}

	// --------------------------
	// Relation highlight methods
	// --------------------------

	public void setHighlightFlag(RelationInstance ri)
	{
		if (!ri.isMarked(RelationInstance.HIGHLIGHT_FLAG_MARK)) {
//			System.out.println("setHighlightFlag " + ri);
			m_highlightRelations.addElement(ri);
			ri.orMark(RelationInstance.HIGHLIGHT_FLAG_MARK);
			ri.validate();
	}	}

	public void setGroupAndHighlightFlag(RelationInstance ri) 
	{
		setRelationGroupFlag(ri);
		setHighlightFlag(ri);
	}

	public void clearHighlightFlag(RelationInstance ri)
	{
		if (ri.isMarked(RelationInstance.HIGHLIGHT_FLAG_MARK)) {
//			System.out.println("clearHighlightFlag " + ri);
			m_highlightRelations.removeElement(ri);
			ri.nandMark(RelationInstance.HIGHLIGHT_FLAG_MARK);
			ri.validate();
	}	}

	public boolean clearAllHighlightFlags() 
	{
		Vector				highlightRelations = m_highlightRelations;
		RelationInstance	ri;
		int					i, size;

		size = highlightRelations.size();
		if (size > 0) {
			for (i = size; --i >= 0; ) {
				ri = (RelationInstance) highlightRelations.elementAt(i);
//				System.out.println("clearAllHighlightFlags " + ri);
				ri.nandMark(RelationInstance.HIGHLIGHT_FLAG_MARK);
				ri.validate();
			}
			highlightRelations.removeAllElements();
			return true;
		}
		return false;
	}

	public boolean getDrawEntities()
	{
		return(m_drawEntities);
	}
	
	public boolean getDrawEdges()
	{
		return(m_drawEdges);
	}

	// Only draw the highlight entities found by the query

	public boolean clearDrawEntities() 
	{
		if (m_drawEntities) {
			m_drawEntities = false;
			return true;
		}
		return false;
	}
	
	public boolean clearDrawEdges() 
	{
//		System.out.println("clearDrawEdges");
		if (m_drawEdges) {
			m_drawEdges = false;
//			java.lang.Thread.dumpStack();
//			System.out.println("-----");
			return true;
		}
		return false;
	}


	public void clearQueryFlags(boolean force) 
	{
		if (force || !Options.isQueryPersists()) {
			clearAllRedBoxFlags();
			clearAllHighlightFlags();
		}
	}

	public boolean clearHighlighting(boolean force) 
	{
		boolean ret = !getDrawEntities();

		if (ret) {
			m_drawEntities = true;
			m_drawEdges    = true;
		}
		clearQueryFlags(force);
		return ret;
	}

	public void clearGroupFlags() 
	{
		clearAllEntityGroupFlags();
		clearAllRelationGroupFlags();
	}

	public boolean clearFlags(boolean force) 
	{
		m_viewActive = false;
		clearGroupFlags();
		return clearHighlighting(force);
	}

	public int numVisibleRelationClasses() 
	{
		return m_numVisibleRelationClasses;
	}

	public String show_groupList(ResultBox resultBox) 
	{
		Vector grp      = getGroupedEntities();
		String msg      = null;

		if (grp != null) {
			SortVector.byString(grp);
			resultBox.showResults("GROUP:", grp, "-- End of group --");
		} else {
			msg = "No entities selected";
		}
		return(msg);
	}

	public String groupAll() 
	{
		EntityInstance	ke      = getKeyEntity();
		int				seen    = 0;
		EntityInstance	ge;
		Enumeration		en;
		EntityInstance	ce;

		if (ke == null) {
			ke = getDrawRoot();
		}
		ge  = ke;
		clearGroupFlags();

		if (!ge.isOpen()) {
			ge = ge.getContainedBy();
		}

		for (en = ge.getChildrenShown(); en.hasMoreElements(); ) {
			ce = (EntityInstance) en.nextElement();
			setEntityGroupFlag(ce);
			++seen;
		}

		if (seen == 0) {
			return "No entities selected";
		}
		return seen  + " entities selected";
	}

	public static int getHardGrid()
	{
		Option option = Options.getDiagramOptions();
		if (option.isSnapToGrid()) {
			int gridPixels = option.getGridSize();
			if (gridPixels > 1) {
				return gridPixels;
		}	}
		return 1;
	}

	// Mark all edges as needing to be repainted

	public void invalidateAllEdges(EntityInstance root)
	{
		Enumeration en;

		/* Draw all the visible objects */

		root.invalidateAllEdges(); 

		if (root.isOpen()) {
			for (en = root.getChildren(); en.hasMoreElements(); ) {
				invalidateAllEdges((EntityInstance) en.nextElement());
		}	}
	}

	/* If this isn't changed to return true the revalidate() becomes a
	   request to revalidate the JScrollPane containing us and that for
	   some reason doesn't attempt to validate() the thing it contains.
	 */

	public boolean isValidateRoot()
	{
		return(true);
	}

	public void zoomChanged()
	{
		Dimension	preferredSize;
		JComponent	container;
		Insets		insets;
		int			w, h;

		container = (JComponent) getParent();
		if (container == null) {
			w = 0;
			h = 0;
		} else {
			Option	diagramOptions = Options.getDiagramOptions();
			
			insets = container.getInsets();

			w = container.getWidth()-insets.right-insets.left;
			if (w <= 0) {
				w = 0;
			}
			h = container.getHeight()-insets.bottom-insets.top;
			if (h <= 0) {
				w = 0;
			}
			w = (int) (((double) w) * diagramOptions.getZoomX());
			h = (int) (((double) h) * diagramOptions.getZoomY());
		}
		if (getWidth() != w || getHeight() != h) {
			preferredSize = getPreferredSize();
			preferredSize.setSize(w, h);
			setPreferredSize(preferredSize);
			setSize(w, h);
			if (container != null) {
				container.revalidate();
			}
			revalidate();
	}	}
		
	public void setZoom(double xfactor, double yfactor)
	{
		Option	diagramOptions = Options.getDiagramOptions();
		
		diagramOptions.setZoomX(xfactor);
		diagramOptions.setZoomY(yfactor);
		zoomChanged();
	}
	
	public void initialZoom()
	{
		zoomChanged();
		centerDiagramOn(m_view_x, m_view_y);
	}

	public boolean updateZoom(double xfactor, double yfactor) 
	{
		Option	diagramOptions = Options.getDiagramOptions();
		
		double oldx = diagramOptions.getZoomX();
		double oldy = diagramOptions.getZoomY();
		double x, y;

		if (xfactor <= 1.0 && yfactor <= 1.0 && oldx == 1.0 && oldy == 1.0) {
			return(false);
		}
		x = oldx * xfactor;
		y = oldy * yfactor;

		if (x < 1.0) {
			x = 1.0;
		}
		if (y < 1.0) {
			y = 1.0;
		}

		if (x != oldx || y != oldy) {
			setZoom(x, y);
			if (undoEnabled()) {
				logEdit(new Zoom(this, oldx,  oldy, x, y));
		}	}
		return true;
	}

	public boolean set_to_viewport()
	{
		Option	diagramOptions = Options.getDiagramOptions();

		double oldx = diagramOptions.getZoomX();
		double oldy = diagramOptions.getZoomY();
		
		if (oldx > 1.0 || oldy > 1.0) {
			setZoom(1.0, 1.0);
			if (undoEnabled()) {
				logEdit(new Zoom(this, oldx, oldy, 1.0, 1.0));
			}
			return true;
		}
		return false;
	}

	public void validate()
	{
		int				mw, mh, yshift;
		ClientSet		clientSet;
		SupplierSet		supplierSet;
		Vector			clients, suppliers;
		Container		container;
		EntityInstance	e, drawRoot;
		boolean			liftEdges, visibleEntities, visibleEdges, normalEntities, normalEdges, hide;
		Cardinal		cardinal;
		int				temp, i, x, y, cnt;

		mw       = getWidth();
		mh       = getHeight();
		
		m_flags &= ~FILL_FLAGS;

		if (mw < 1 || mh < 1) {		// We've been called before m_diagram has been assigned a bounds
			return;
		}

//		System.out.println("Diagram.fill Started mw=" + mw + " mh=" + mh);

/*
		java.lang.Thread.dumpStack();
		System.out.println("-----");
*/

		removeAll();				// Remove everything currently in the diagram

		drawRoot     = m_drawRoot;

		if (drawRoot == null) {
			// Empty diagram
			return;
		}
		
		Option				option       = Options.getDiagramOptions();
		LandscapeEditorCore	ls           = m_ls;
		EntityInstance		rootInstance = m_rootInstance;

		ls.setCursor(Cursor.WAIT_CURSOR);

		recomputeContainsClasses();

		if (m_numVisibleRelationClasses < 0) {
			setVisibilityFlags();
		}

		liftEdges       = option.isLiftEdges();
		visibleEntities = option.isVisibleEntities();
		visibleEdges    = option.isVisibleEdges();
		normalEntities  = getDrawEntities();
		hide            = (!normalEntities && (option.isChaseHide() || m_viewActive));
		
		m_cardinals.removeAll();

		// These go on top
		if (option.isShowDstCardinals() || option.isShowSrcCardinals()) {
			m_cardinals.setBounds(0, 0, mw, mh);
			// The region to contain cardinals
			add(m_cardinals);
		}

		// This goes under cardinals so add next
		 
		// The region to contain edges
		m_edges.removeAll();
		m_edges.setBounds(0,0,mw, mh);
		add(m_edges);		// Add the set of all edges

		// These go under edges so add next

		// The region to contain edge labels
		m_edge_labels.removeAll();
		m_edge_labels.setBounds(0,0,mw, mh);
		add(m_edge_labels);

		mw       -= GAP*6;		// Maximum width  of e
		mh	     -= GAP*4;		// Maximum height of e
//		System.out.println("mw=" + mw + " mh=" + mh);
		yshift    = 0;
		clients   = null;
		suppliers = null;

		// Make entity take a full footprint in the display.

		if (drawRoot != rootInstance) {
			add(m_exitFlag);
			m_exitFlag.activate();
		} 

		rootInstance.clearLiftedEdges(0, liftEdges, hide);
		m_drawRoot.setOpen();

		drawRoot.orMark(EntityInstance.DRAWROOT_MARK);

		// Things under drawroot now marked as UNDER_DRAWROOT
		// DrawRoot now marked as the DRAW_ROOT

		clientSet   = m_clientSet;
		supplierSet = m_supplierSet;

		clientSet.removeAll();
		supplierSet.removeAll();

		// This marks everything that clients and suppliers can see by indicating the nodes lifted node

		/* This performs a first cut at finding clients and suppliers
		 * It essentially collects into a vector topmost clients and suppliers
		 */

		if (drawRoot == rootInstance) {
			// Don't bother
			clientSet     = null;
			supplierSet   = null;
		} else {
			boolean showClients     = option.isShowClients();
			boolean showSuppliers   = option.isShowSuppliers();
			boolean isTopClients    = option.isTopClients();
			boolean useCompaction   = option.isUseCompaction();
			int		ypos;

			if (showClients || showSuppliers) {

				if (!showClients) {
					clientSet = null;
				}
				if (!showSuppliers) {
					supplierSet = null;
				}

				// Have to start from root since clients/suppliers are any nodes off path from root to drawroot not under drawroot

				rootInstance.findClientsSuppliers(clientSet, supplierSet, drawRoot, null, visibleEntities, visibleEdges, liftEdges);

//				System.out.println("Diagram.fill clients = " + clientSet.setFound() + " suppliers = " + supplierSet.setFound());
				if (clientSet != null && clientSet.setFound() == 0) {
					clientSet = null;
				}
				if (supplierSet != null && supplierSet.setFound() == 0) {
					supplierSet = null;
				}

				if (useCompaction) {
					if (supplierSet != null) {
						// This compacts suppliers and may reduce clients
						supplierSet.compact(mw, clientSet, drawRoot);
					}
					if (clientSet != null) {
						// This compacts clients
						if (clientSet.compact(mw, supplierSet, drawRoot)) {
							// If increased suppliers
							supplierSet.compact(mw, clientSet, drawRoot);
				}	}	} 

				if (clientSet != null) {
					// we will either show or report not showing
					// Reduce height to allow clients
					temp = (ClientSupplierSet.CLIENT_SUPPLIER_HEIGHT + GAP*4);
					mh  -= temp;
					if (isTopClients) {
						ypos   = GAP*2;
						yshift = temp; 
					} else {
						ypos = getHeight() - ClientSupplierSet.CLIENT_SUPPLIER_HEIGHT - GAP*2;
					}

					// N.B. This will also size the top level entities inside clientSet

					clientSet.setBounds(GAP*3, ypos, mw, ClientSupplierSet.CLIENT_SUPPLIER_HEIGHT);
//					System.out.println("Diagram.validate() clientSet=" + clientSet.getBounds());

				}	

				if (supplierSet != null) {
					temp = (ClientSupplierSet.CLIENT_SUPPLIER_HEIGHT + GAP*4);
					mh  -= temp;
					if (!isTopClients) {
						ypos   = GAP*2;
						yshift = temp; 
					} else {
						ypos   = getHeight() - ClientSupplierSet.CLIENT_SUPPLIER_HEIGHT - GAP*2;
					}

					// N.B. This will also size the top level entities inside supplierSet

					supplierSet.setBounds(GAP*3, ypos, mw, ClientSupplierSet.CLIENT_SUPPLIER_HEIGHT);
//					System.out.println("Diagram.validate() supplierSet=" + supplierSet.getBounds());
				}
//				System.out.println("Diagram.validate() found clients and suppliers");
		}	}
	
		// Now know the actual size of the drawroot box so can set this size
		// Must do this before calling computeAllDiagramEdges because zero sized entities are presumed closed
		// This will assign unspecified width/height (those marked with value -1.0) values
		// This will also set the location of all entityComponents to (0,0) a key requirement for when
		// we later set the diagram location to the location of the parent entity, so that subsequent
		// shifts of location keep the diagram location correct.

		// N.B This step also adds drawRoot into the diagram

		drawRoot.setEntitySize(mw, mh, this);
//		System.out.println("Diagram.validate() resized Entities in Diagram");

		drawRoot.computeDrawEntity(null /* No closed entities yet */, EntityInstance.DIAGRAM_MARK, true /* visibleEntities */, liftEdges);
//		System.out.println("Diagram.validate() computed Draw Entities");

		if (clientSet != null) {
			clients = clientSet.getFullSet();
			if (clients != null) {
				for (i = clients.size(); --i >= 0; ) {
					((EntityInstance) clients.elementAt(i)).computeDrawEntity(null /* No closed entities yet */, EntityInstance.CLIENT_MARK, visibleEntities, liftEdges);
		}	}	}	

		if (supplierSet != null) {
			suppliers = supplierSet.getFullSet();
			if (suppliers != null) { 
				for (i = suppliers.size(); --i >= 0; ) {
					((EntityInstance) suppliers.elementAt(i)).computeDrawEntity(null /* No closed entities yet */, EntityInstance.SUPPLIER_MARK, visibleEntities, liftEdges);
		}	}	}

		drawRoot.liftAllDiagramEdges(drawRoot, liftEdges);
		
		e = ls.getForwardEntity();
		if (e != null) {
			// Can't do this until after we have liftedAllDiagramEdges
			// Have to do it in the right thread too which is why it is here
			queryEntity(Do.FORWARD_CLOSURE, e, ls.getResultBox(), ls.getForward());
			ls.clearForwardEntity();
			validate();
			return;
		}
		
//		System.out.println("Diagram.validate() lifted all edges");

		// Don't do until we have lifted all diagram edges

		x = GAP*3;
		y = GAP*2 + yshift;

		drawRoot.setEntityLocation(x, y);
		drawRoot.setDiagramLocation(x, y);

		drawRoot.computeShading();

//		System.out.println("Diagram.validate() setDiagramLocation");
//		System.out.println("Diagram.validate() added to Diagram");

		// Do after lifted edges since ordering based on where edges go to/come from

		if (supplierSet != null && supplierSet.getFullSetSize() != 0) {	
			supplierSet.order();
			add(supplierSet);		// Add the quasi supplier box to diagram
			m_flags |= SHOWS_SUPPLIERS_FLAG;
			supplierSet.setVisible(true);
//			System.out.println("Diagram.validate() added Suppliers");
		}

		if (clientSet != null && clientSet.getFullSetSize() != 0) {
			clientSet.order();
			add(clientSet);		// Add the quasi client box to the diagram
			m_flags |= SHOWS_CLIENTS_FLAG;
			clientSet.setVisible(true);
//			System.out.println("Diagram.validate() added Clients");
		}

		// This will set VALID_MARK in all drawn lifted edges
		// and put them in the diagram

		normalEdges     = getDrawEdges();
		if (!normalEntities || m_visibleEdges) {
			/* Draw all the visible objects if normal else draw all highlight edges*/
			drawRoot.drawAllEdges(this, normalEdges); 
		} 

		refillEdges();

		if (option.isShowDstCardinals()) {
		
			drawRoot.resizeDstCardinals(m_numRelationClasses);
			drawRoot.resetDstCardinals();
			drawRoot.calcDstEdgeCardinals();
			drawRoot.showDstCardinals();
//			System.out.println("Show dst cardinals " + m_cardinals.getComponentCount());
		}

		if (option.isShowSrcCardinals()) {
		
			drawRoot.resizeSrcCardinals(m_numRelationClasses);
			drawRoot.resetSrcCardinals();
			drawRoot.calcSrcEdgeCardinals();
			drawRoot.showSrcCardinals();
//			System.out.println("Show src cardinals " + m_cardinals.getComponentCount());
		}

		container = m_cardinals;
		cnt = container.getComponentCount();
		for (i = 0; i < cnt; ++i) {
			cardinal = (Cardinal) container.getComponent(i);
			cardinal.known();
		}

//		System.out.println("Diagram.validate() processed edges");

		ls.setCursor(Cursor.DEFAULT_CURSOR);

		repaint();
		m_loaded = true;

//		System.out.println("Diagram.validate() done");
	}

	public void navigateTo(EntityInstance e, boolean mustbeContainer) 
	{
		LandscapeEditorCore ls       = getLs();
		EntityInstance		child    = null;
		EntityInstance		e1       = e;

		clearFlags(true);
		if (mustbeContainer) {
			if (e.getFirstChild() == null) {
				EntityInstance parent = e.getContainedBy();
				if (parent != null) {
					child = e;
					e1    = parent;
		}	}	}

		// Make entity take a full footprint in the display.

		if (child == null) {
			EntityInstance	old = getDrawRoot();
			if (old != null) {
				EntityInstance	parent;

				for (; (parent = old.getContainedBy()) != null; old = parent) {
					if (parent == e1) {
						child = old;
						break;
		}	}	}	}
				
		if (child != null) {
			setRedBoxFlag(child);
			setEntityGroupFlag(child);
		}
		setDrawRoot(e1);
		ls.setLeftBox();
		if (e != e1) {
			centerDiagramOn(e);
		}
		
		revalidate();
		ls.doFeedback("Now showing: " + ((e == null) ? " nothing" : e.getEntityLabel()));
	}

	// Navigate to default entity on loading

	public void navigateToRoot()
	{
		navigateTo(m_rootInstance, false);
	}

	public boolean allowElision() 
	{
		return getDrawEntities();
	}

	public boolean isModeHandlingActive()
	{
		return(m_modeHandlingActive);
	}

	public void setModeHandlingActive(boolean value)
	{
		m_modeHandlingActive = value;
	}

	public RelationInstance targetRelation(Object object)
	{
		if (object != null) {
			if (object instanceof Vector) {
				Vector v = (Vector) object;
				switch (v.size()) {
				case 0:
					m_ls.doFeedback("Target relation set is empty");
					return(null);
				case 1:
					object = v.firstElement();
					break;
				default:
					m_ls.doFeedback("Target relation set has multiple objects in it");
					return(null);
				}
			}
			if (object instanceof RelationInstance) {
				return((RelationInstance) object);
			}
			return(null);
		}

		Vector v = getGroupedRelations();

		if (v == null) {
			m_ls.doFeedback("Select the edge you wish to work on");
		} else {
			if (v.size() == 1) {
				return((RelationInstance) v.firstElement());
			}
			m_ls.doFeedback("Can't operate on " + v.size() + " grouped edges");
		}
		return(null);
	}

	public EntityInstance targetEntity(Object object)
	{
		if (object != null) {
			if (object instanceof Vector) {
				Vector v = (Vector) object;
				switch (v.size()) {
				case 0:
					m_ls.doFeedback("Target entity set is empty");
					return(null);
				case 1:
					object = v.firstElement();
					break;
				default:
					m_ls.doFeedback("Target entity set has multiple objects in it");
					return(null);
			}	}
			if (object instanceof EntityInstance) {
				return((EntityInstance) object);
			}
			return(null);
		}

		EntityInstance ke;

		ke = getKeyEntity();
		if (ke != null) {
			return(ke);
		}
		return(getDrawRoot());
	}

	public Vector targetEntityRelations(Object object)
	{
		Vector v, v1;

		if (object != null) {
			if (object instanceof Vector) {
				v = (Vector) object;
				if (v.size() > 0) {
					return(v);
				}
				m_ls.doFeedback("Target set is empty");
				return(null);
			}
			v = new Vector();
			v.addElement(object);
			return(v);
		}

		v  = getGroupedRelations();

		v1 = getGroupedEntities();
		if (v == null) {
			v = v1;
		} else if (v1 != null) {
			v.addAll(v1);
		}
		if (v == null) {
			m_ls.doFeedback("Select the entity/relations you wish to work on");
		}
		return(v);
	}

	public LandscapeObject targetEntityRelation(Object object)
	{
		Vector			v               = targetEntityRelations(object);
		LandscapeObject	landscapeObject = null;


		if (v == null) {
			m_ls.doFeedback("No object specified");
		} else {
			if (v.size() != 1) {
				m_ls.doFeedback("Select a single entity or relation that you wish to work with");
			} else {
				landscapeObject = (LandscapeObject) v.elementAt(0);
			}
			v = null;
		}	
		return(landscapeObject);
	}

	public Vector targetRelations(Object object)
	{
		Vector v;

		if (object != null) {
			if (object instanceof Vector) {
				v = (Vector) object;
				if (v.size() > 0) {
					return(v);
				}
				m_ls.doFeedback("Target relation set is empty");
				return(null);
			}
			v = new Vector();
			v.addElement(object);
			return(v);
		}

		v = getGroupedRelations();

		if (v == null) {
			m_ls.doFeedback("Select the edges you wish to work on");
		}
		return(v);
	}

	public Vector targetEntities(Object object)
	{
		Vector v;

		if (object != null) {
			if (object instanceof Vector) {
				v = (Vector) object;
				if (v.size() > 0) {
					return(v);
				}
				m_ls.doFeedback("Target entity set is empty");
				return(null);
			}
			v = new Vector();
			v.addElement(object);
			return(v);
		}
		return(getGroupedEntities());
	}

	public Vector getClusterGroup()
	{
		Vector grp = getGroupedEntities();

		if (grp == null) {
			Enumeration		children;
			EntityInstance	child;

			for (children = m_drawRoot.getChildrenShown(); children.hasMoreElements(); ) {
				child = (EntityInstance) children.nextElement();
				if (grp == null) {
					grp = new Vector();
				}
				grp.addElement(child);
		}	}
		return grp;
	}

	public Vector getReclusterGroup()
	{
		Vector grp = getGroupedEntities();
		
		if (grp != null) {
			return grp;
		}
		grp = getGroupedRelations();
		if (grp != null) {
			return grp;
		}

		Enumeration children;

		for (children = m_drawRoot.getChildrenShown(); children.hasMoreElements(); ) {
			if (grp == null) {
				grp = new Vector();
			}
			grp.addElement(children.nextElement());
		}	
		return grp;
	}

	public Vector getTargetGroup(Object object)
	{
		Vector v;

		if (object != null) {
			v = targetEntities(object);
		} else {
			v = getClusterGroup();
		}
		return v;
	}

	public Clipboard getClipboard()
	{
		return m_clipboard;
	}

	protected void setClipboard(Clipboard value)
	{
		m_clipboard = value;
		m_ls.clipboardChanged();
	}

	protected void relayoutSubtree(EntityInstance container, boolean compute)
	{
		Enumeration		en;
		EntityInstance	e;

		for (en = container.getChildrenShown(); en.hasMoreElements(); ) {
			e = (EntityInstance) en.nextElement();
			e.setRelLocal(-1, -1, e.widthRelLocal(), e.heightRelLocal());
			relayoutSubtree(e, compute);
	}	}

	protected void doRelayoutAll(EntityInstance e, boolean compute)
	{
		if (undoEnabled()) {
			logEdit(new RelayoutSubtree(this, e, compute));
		}
		relayoutSubtree(e, compute);
	} 

	protected void doRelayoutAll(boolean layout)
	{
		doRelayoutAll(m_drawRoot, layout);
	} 

	public class RelayoutDialog extends JDialog implements ActionListener {

		LandscapeLayouter[]			m_layouters;

		protected JRadioButton[]	m_radioButtons;
		protected JButton[]			m_buttons;

		protected JButton			m_clearLayoutAll;
		protected JButton			m_relayoutAll;
		protected JButton			m_setDefault;
		protected JButton			m_close;
		protected String			m_msg = null;
		
		class LayoutTab extends JPanel implements ActionListener
		{
			public LayoutTab()
			{
				JPanel				grid1;
				LandscapeLayouter   layouter;
				JButton				button;
				int					i;
				Font				font, bold;
				int				    layouts;

				setLayout(new BorderLayout());

				layouts     = m_layouters.length;

				font   = FontCache.getDialogFont();
				bold   = font.deriveFont(Font.BOLD);
		
				grid1 = new JPanel();
				grid1.setLayout(new GridLayout(6, 1, 0, 10));

				for (i = 0; i < layouts; ++i) {
					layouter = m_layouters[i];
					if (!layouter.isLayouter() || !layouter.isConfigurable()) {
						continue;
					}
					button = new JButton("configure " + layouter.getName() + " layouter");
					button.setFont(bold);
					button.addActionListener(this);
					m_buttons[i] = button; 
					grid1.add(button);

				}
				add(grid1, BorderLayout.CENTER);
			}

			public void actionPerformed(ActionEvent ev)
			{
				Object			source;
				int				i, cnt;

				source = ev.getSource();
				cnt    = m_buttons.length;
				for (i = 0; i < cnt; ++i) {
					if (source == m_buttons[i]) {
						m_layouters[i].configure(m_ls);
						return;
			}	}	}
		}	

		class ClusterTab extends JPanel implements ActionListener
		{
			public ClusterTab()
			{

				JPanel				grid1;
				LandscapeLayouter   layouter;
				JButton				button;
				int					i;
				Font				font, bold;
				int				    layouts;

				setLayout(new BorderLayout());

				layouts     = m_layouters.length;

				font   = FontCache.getDialogFont();
				bold   = font.deriveFont(Font.BOLD);
			
		
				grid1   = new JPanel();
				grid1.setLayout(new GridLayout(6, 1, 0, 10));

				for (i = 0; i < layouts; ++i) {
					layouter = m_layouters[i];
					if (layouter.isLayouter() || !layouter.isConfigurable()) {
						continue;
					}
					button = new JButton("configure " + m_layouters[i].getName() );
					button.setFont(bold);
					button.addActionListener(this);
					m_buttons[i] = button; 
					grid1.add(button);

				}
				add(grid1, BorderLayout.CENTER);
			}

			public void actionPerformed(ActionEvent ev)
			{
				Object			source;
				int				i, cnt;

				source = ev.getSource();
				cnt    = m_buttons.length;
				for (i = 0; i < cnt; ++i) {
					if (source == m_buttons[i]) {
						m_layouters[i].configure(m_ls);
						return;
			}	}	}
		}	

		class RelayoutAllTab extends JPanel implements ActionListener
		{
			protected void changeSelected(int i)
			{
				JRadioButton		radioButton;
				LandscapeLayouter   layouter;
				String				name;

				radioButton = m_radioButtons[i];
				layouter    = m_layouters[i];
				radioButton.setSelected(true);
				name        = layouter.getName();

				m_clearLayoutAll.setText("Clear layout for subtree " + m_drawRoot + " using " + name);

				m_relayoutAll.setText("Relayout subtree " + m_drawRoot + " using " + name);
				m_setDefault.setText("Set default layouter to " + layouter.getName()); 
			}

			public RelayoutAllTab()
			{
				JPanel				grid;
				ButtonGroup			buttonGroup;
				JRadioButton		radioButton;
				int					i;
				Font				font, bold;
				LandscapeLayouter   layouter;
				int				    layouts;

				setLayout(new BorderLayout());

				m_clearLayoutAll = new JButton();
				m_clearLayoutAll.addActionListener(this);
				m_relayoutAll = new JButton();
				m_relayoutAll.addActionListener(this);
				m_setDefault  = new JButton();
				m_setDefault.addActionListener(this);

				layouts        = m_layouters.length;
				m_radioButtons = new JRadioButton[layouts];
				layouter       = m_ls.getLayouter();

				font   = FontCache.getDialogFont();
				bold   = font.deriveFont(Font.BOLD);
		
				grid   = new JPanel();
				grid.setLayout(new GridLayout(9, 1, 0, 10));

				buttonGroup    = new ButtonGroup();

				for (i = 0; i < layouts; ++i) {
					if (!m_layouters[i].isLayouter()) {
						continue;
					}
					m_radioButtons[i] = radioButton = new JRadioButton(m_layouters[i].getMenuLabel());
					radioButton.setFont(bold);
					radioButton.addActionListener(this);
					if (m_layouters[i] == layouter) {
						changeSelected(i);
					}
					buttonGroup.add(radioButton);
					grid.add(radioButton);
				}

				grid.add(m_clearLayoutAll);
				grid.add(m_relayoutAll);
				grid.add(m_setDefault);

				add(grid, BorderLayout.CENTER);
			}

			public void actionPerformed(ActionEvent ev)
			{
				Object			source;
				JRadioButton	radioButton;
				int				i, cnt;

				source = ev.getSource();
				cnt    = m_radioButtons.length;

				if (source == m_clearLayoutAll || source == m_relayoutAll || source == m_setDefault) {

					for (i = 0; i < cnt; ++i) {
						radioButton = m_radioButtons[i];
						if (radioButton != null && radioButton.isSelected()) {
							if (source == m_setDefault) {
								m_ls.defaultToLayouter(i);
							} else {
								doRelayoutAll(source == m_relayoutAll);
								m_msg = "Prior graph layout deleted";
							}
							break;
					}	}
					return;
				}

				for (i = 0; i < cnt; ++i) {
					radioButton = m_radioButtons[i];
					if (source == radioButton) {
						if (radioButton.isEnabled()) {
							changeSelected(i);
						}
						return;
			}	}	}
		}	

		public RelayoutDialog()
		{
			super(m_ls.getFrame(), "Reconfigure layouters/clusterers", true);

			Container			contentPane;
			JTabbedPane			tabbedPane;
			JPanel				panel;
			int					x, y;
			Font				font, bold;
			int				    layouts;

			m_layouters    = m_ls.getLayouters();
			layouts        = m_layouters.length;
			m_buttons      = new JButton[layouts];

			font   = FontCache.getDialogFont();
			bold   = font.deriveFont(Font.BOLD);

			x      = Diagram.this.getX() + 20;
			y      = Diagram.this.getY() + 20;
			setLocation(x, y);
			setForeground(ColorCache.get(0,0,0));
			setBackground(ColorCache.get(192,192,192));
			setFont(font);

			contentPane = getContentPane();
			tabbedPane  = new JTabbedPane();
			tabbedPane.addTab("Layouters", null, new LayoutTab(),  null);
			tabbedPane.addTab("Clusterers",null, new ClusterTab(), null);
			tabbedPane.addTab("Layout all",null, new RelayoutAllTab(), null);
			contentPane.add(BorderLayout.NORTH, tabbedPane);

			panel = new JPanel();
			panel.setLayout(new FlowLayout());

			m_close = new JButton("Close");
			m_close.setFont(bold);
			m_close.addActionListener(this);
			panel.add(m_close);

			contentPane.add(panel, BorderLayout.SOUTH);

			// Resize the window to the preferred size of its components

			this.pack();
			setVisible(true);
		}

		public String msg()
		{
			return m_msg;
		}

		// ActionListener interface

		public void actionPerformed(ActionEvent ev)
		{
			Object			source;

			source = ev.getSource();
			if (source == m_close) {
				setVisible(false);
				return;
			}
		}
	}

	public String relayoutAll()
	{
		RelayoutDialog relayoutDialog = new RelayoutDialog();
		String		   ret            = relayoutDialog.msg();

		relayoutDialog.dispose();
		return ret;
	}

	public String closeAll()
	{
		m_drawRoot.closeDescendants();
		revalidate();
		return "Entities closed";
	}

	public String openAll()
	{
		m_drawRoot.openDescendants();
		return "Entities opened";
	}

	protected void centerDiagramOn(int x, int y)
	{
		Option	diagramOptions = Options.getDiagramOptions();
		double	zoom_x         = diagramOptions.getZoomX();
		double	zoom_y         = diagramOptions.getZoomY();

		if (zoom_x > 1.0 || zoom_y > 1.0) {

			JScrollPane	scrollPane = m_ls.m_scrollDiagram;
			JViewport	viewport   = scrollPane.getViewport();
			Dimension	d          = getPreferredSize();
			int			width1     = d.width;
			int			height1    = d.height;
			int			width2     = (int) (((double) width1)  / zoom_x);
			int			height2    = (int) (((double) height1) / zoom_y);
			int			max;
			
			if (x < 0) {
				x = 0;
			} else {
				max = width1 - width2;
				if (x > max) {
					x = max;
			}	}
			
			if (y < 0) {
				y = 0;
			} else {
				max = height1 - height2;
				if (y > max){
					y = max;
			}	}

			scrollPane.validate();		// Call to fix a bug in Swing http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5066771

			// Sets the view coordinates that appear in the upper left hand corner of the viewport, 
			// does nothing if there's no view.

			// System.out.println("centerDiagramOn(" + x + ", " + y + ")");
		
			viewport.setViewPosition(new Point(x, y));
	}	}
	
	/* 
	 * width1 = preferredSize.width & height1 = preferredSize.height (total thing being viewed)
	 * width2 = width1/m_zoom_x     & height2 = height1/m_zoom_y     (logical size of the viewport)
	 * 
	 * The center point of e in the total thing being viewed (first formula) must be placed at the
	 * center of the viewport, or equivalently we must move the viewport on that its x offset + width/2
	 * corresponds to the center of e
	 * 
	 * e.xc = (e.x + e.w/2)*x1 = x + width2/2
	 * e.yc = (e.y + e.h/2)*y1 = y + height2/2
	 *
	 * Now solve for x and y.
	 */

	protected void centerDiagramOn(EntityInstance e)
	{
		Option	diagramOptions = Options.getDiagramOptions();
		double	zoom_x         = diagramOptions.getZoomX();
		double	zoom_y         = diagramOptions.getZoomY();

		if (zoom_x > 1.0 || zoom_y > 1.0) {

			Dimension	d          = getPreferredSize();
			int			width1     = d.width;
			int			height1    = d.height;
			int			width2     = (int) (((double) width1)  / zoom_x);
			int			height2    = (int) (((double) height1) / zoom_y);
			int			x_center   = (int) ((e.xRelLocal() + e.widthRelLocal() /2.0) * ((double) width1) );
			int			y_center   = (int) ((e.yRelLocal() + e.heightRelLocal()/2.0) * ((double) height1));
			int			x          = x_center - width2 / 2;
			int			y          = y_center - height2/ 2;
			
			centerDiagramOn(x, y);
	}	}


	protected void scale(EntityInstance e, double wf, double hf, boolean doContainer, boolean doPos)
	{
		// Scale ourselves 

//		System.out.println("Diagram.scale(" e + "," + wf + "," + hf + "," + doContainer + "," + doPos + ")");

		if (doContainer) {
			double x1, y1, w1, h1; 

			x1 = e.xRelLocal();		
			y1 = e.yRelLocal();
			w1 = e.widthRelLocal()  * wf;
			h1 = e.heightRelLocal() * hf;

			if (doPos) {
				x1 *= wf;
				y1 *= hf;
			}
			updateRelLocal(e, x1, y1, w1, h1);
		} else {
			Enumeration		en;
			EntityInstance	e1;

			for (en = e.getChildren(); en.hasMoreElements(); ) {
				e1 = (EntityInstance) en.nextElement();
				scale(e1, wf, hf, true, true);
		}	}
	}

	public void scale(EntityInstance e, double sf, boolean doContainer, boolean doPos) 
	{
		scale(e, sf, sf, doContainer, doPos);
	}

	public void scale(EntityInstance e, double sf, boolean doContainer) 
	{
		scale(e, sf, sf, doContainer, false);
	}

	public void scale(EntityInstance e, double sf) 
	{
		scale(e, sf, sf, true, false);
	}

	public void scaleX(EntityInstance e, double sf, boolean doContainer, boolean doPos) 
	{
		scale(e, sf, 1.0, doContainer, doPos);
	}

	public void scaleX(EntityInstance e, double sf, boolean doContainer) 
	{
		scale(e, sf, 1.0, doContainer, false);
	}

	public void scaleX(EntityInstance e, double sf) 
	{
		scale(e, sf, 1.0, true, false);
	}

	public void scaleY(EntityInstance e, double sf, boolean doContainer, boolean doPos) 
	{
		scale(e, 1.0, sf, doContainer, doPos);
	}

	public void scaleY(EntityInstance e, double sf, boolean doContainer) 
	{
		scale(e, 1.0, sf, doContainer, false); 
	}

	public void scaleY(EntityInstance e, double sf) 
	{
		scale(e, 1.0, sf, true, false);
	}

	public String scaleEntity(int scale, boolean incFlag) 
	{
		Vector			grp     = getClusterGroup();
		String			msg     = null;
		EntityInstance	e       = null;

		if (grp != null) {
			Enumeration en;

			switch (grp.size()) {
			case 0:
				break;
			case 1:
				e = (EntityInstance) grp.firstElement();
				if (e.isDrawRoot()) {
					break;
				}
			default:
				switch (scale) {
				case Do.DECREASE_MAG:
				case Do.INCREASE_MAG:
					if (e == null) {
						e = (EntityInstance) grp.firstElement();
					}
					break;
				default:
					for (en = grp.elements(); en.hasMoreElements(); ) {
						EntityInstance ge = (EntityInstance) en.nextElement();
						switch(scale) {
						case Do.DECREASE_WIDTH:
							scaleX(ge, SMALL_SCALE_DOWN, incFlag);
							break; 

						case Do.INCREASE_WIDTH:
							scaleX(ge, SMALL_SCALE_UP, incFlag);
							break; 

						case Do.DECREASE_HEIGHT:
							scaleY(ge, SMALL_SCALE_DOWN, incFlag);
							break; 

						case Do.INCREASE_HEIGHT:
							scaleY(ge, SMALL_SCALE_UP, incFlag);
							break; 

						case Do.DECREASE_SIZE:
							scale(ge, SMALL_SCALE_DOWN, incFlag);
							break;

						case Do.INCREASE_SIZE:
							scale(ge, SMALL_SCALE_UP, incFlag);
							break;
						}
					}
					return(msg);
			}	}
		}

		// Do it to root

		switch (scale) {
		case Do.INCREASE_MAG:
			if (e != null) {
				if (updateZoom(SMALL_SCALE_UP, SMALL_SCALE_UP)) {
					msg = "Magnified " + e.getEntityLabel();
				}
				centerDiagramOn(e);
				break;
			}
		case Do.INCREASE_SIZE:
			// Zoom in
			if (updateZoom(SMALL_SCALE_UP, SMALL_SCALE_UP)) {
				msg = "Zoomed in";
			}
			break;
		case Do.DECREASE_MAG:
			if (e != null) {
				if (updateZoom(SMALL_SCALE_DOWN, SMALL_SCALE_DOWN)) {
					msg = "Reduced " + e.getEntityLabel();
				}
				centerDiagramOn(e);
				break;
			}
		case Do.DECREASE_SIZE:
			// Zoom out
			if (updateZoom(SMALL_SCALE_DOWN, SMALL_SCALE_DOWN)) {
				msg = "Zoomed out";
			}
			break;

		case Do.INCREASE_WIDTH:
			if (updateZoom(SMALL_SCALE_UP, 1.0)) {
				msg = "Zoomed in X direction";
			}
			break;

		case Do.DECREASE_WIDTH:
			if (updateZoom(SMALL_SCALE_DOWN, 1.0)) {
				msg = "Zoomed out X direction";
			}
			break;

		case Do.INCREASE_HEIGHT:
			if (updateZoom(1.0, SMALL_SCALE_UP)) {
				msg = "Zoomed in Y direction";
			}
			break;

		case Do.DECREASE_HEIGHT:
			if (updateZoom(1.0, SMALL_SCALE_DOWN)) {
				msg = "Zoomed out Y direction";
			}
			break;
		}
		
		Option	diagramOptions = Options.getDiagramOptions();
		double	zoom_x         = diagramOptions.getZoomX();
		double	zoom_y         = diagramOptions.getZoomY();

		msg += " (" + SMALL_SCALE_STRING + ") to " + zoom_x + "x" + zoom_y;
		return(msg);
	}


	public String handleElision(int key, Object object) 
	{
		String			em     = "";
		Vector			grp    = targetEntities(object);
		boolean			open   = false;
		EntityInstance	ge, ke = null;
		Enumeration		en;

		if (grp == null  || grp.size() == 0) {
			return "Nothing selected";
		}

		switch(key)	{
		case Do.SHOW_CONTENTS:
			 // Toggle contain elision
			em = "Containment";

			if (grp.size() == 1) {
				ke = (EntityInstance) grp.elementAt(0);
			} else {
				ke = getKeyEntity();
			}
			if (ke == null) {
				return "Unable to identify key entity to open/close";
			}
			open = ke.isStateOpen();
			break;
		case Do.DST_EDGES:
			em = "Target ";
			break;

		case Do.SRC_EDGES:
			em = "Source "; 
			break;

		case Do.ENTERING_EDGES:
			em = "Entering";
			break; 

		case Do.EXITING_EDGES:
			em = "Exiting";
			break;

		case Do.INTERNAL_EDGES:
			em = "Internal edges";
			break;
		default:
			return(null);
		}

		for (en = grp.elements(); en.hasMoreElements(); ) {
			ge = (EntityInstance) en.nextElement();

			switch(key)	{
			case Do.SHOW_CONTENTS:
			{
				boolean geopen = ge.isStateOpen();

				if (ge == ke || open == geopen) {
					if (geopen) {
						ge.setStateClosed();
					} else {
						ge.setStateOpen();
				}	}
				break;
			}
			case Do.DST_EDGES:
				toggleDstElision(ge);
				break;

			case Do.SRC_EDGES:
				toggleSrcElision(ge);
				break;

			case Do.ENTERING_EDGES:
				toggleEnteringElision(ge);
				break;

			case Do.EXITING_EDGES:
				toggleExitingElision(ge);
				break;
			
			case Do.INTERNAL_EDGES:
				toggleInternalElision(ge);
				break;
			}
		}
		revalidate();
		return(em + " elision toggled for group");
	}

	// --------------------
	// Tracing computations
	// --------------------

	/* If there is any edge from me to/under under then perform openEdge operation */

	public boolean openEdgeExpansion(EntityInstance me, EntityInstance from, EntityInstance to, String indent, ResultBox resultBox)
	{
		Vector	srcRelList = me.getSrcRelList();
		boolean	ret        = false;

		if (srcRelList != null) {
			Enumeration			en;
			EntityInstance		e;
			RelationInstance	ri;
			EntityInstance		dst;
			boolean				ret1, opened;
			String				indent1 = indent;

			for (en = srcRelList.elements(); en.hasMoreElements(); ) {
				ri  = (RelationInstance) en.nextElement();
				dst = ri.getDst();
				if (me != from || dst != to) {
					if (to.hasDescendantOrSelf(dst)) {
						ret = true;
						resultBox.addRelation(indent, ri);
						if (indent1 == indent) {
							indent1 = indent + "+- ";
						}
						setHighlightFlag(ri);
						if (dst != to) {
							for (;;) {
								dst = dst.getContainedBy();
								dst.setStateOpen(); 
								if (dst == to) {
									break;
			}	}	}	}	}	}

			opened = false;
			for (en = me.getChildren(); en.hasMoreElements(); ) {
				e = (EntityInstance) en.nextElement();
				ret1 = openEdgeExpansion(e, from, to, indent1, resultBox);
				if (ret1 && !opened) {
					ret = opened = true;
					me.setStateOpen();
		}	}	}
		return(ret);
	}

	// Traverse the source side

	public boolean openSrcEdgeExpansion(EntityInstance me, EntityInstance from, EntityInstance to, String indent, ResultBox resultBox)
	{
		Vector	srcRelList = me.getSrcRelList();
		boolean	ret        = false;

		if (srcRelList != null) {
			Enumeration			en;
			EntityInstance		e;
			RelationInstance	ri;
			EntityInstance		dst;
			boolean				ret1, opened;
			String				indent1 = indent;

			for (en = srcRelList.elements(); en.hasMoreElements(); ) {
				ri  = (RelationInstance) en.nextElement();
				dst = ri.getDst();
				if (me != from || dst != to) {
					if (to.hasDescendantOrSelf(dst)) {
						ret = true;
						resultBox.addRelation(indent, ri);
						if (indent1 == indent) {
							indent1 = indent + "+- ";
						}
						setHighlightFlag(ri); 
			}	}	}

			opened = false;
			for (en = me.getChildren(); en.hasMoreElements(); ) {
				e = (EntityInstance) en.nextElement();
				ret1 = openSrcEdgeExpansion(e, from, to, indent1, resultBox);
				if (ret1 && !opened) {
					ret = opened = true;
					me.setStateOpen();
		}	}	}
		return(ret);
	}

	// Traverse the destination side

	public boolean openDstEdgeExpansion(EntityInstance me, EntityInstance from, EntityInstance to, String indent, ResultBox resultBox)
	{
		Vector				dstRelList = me.getDstRelList();
		boolean				ret = false;

		Enumeration			en;
		EntityInstance		e;
		boolean				ret1, opened;
		String				indent1 = indent;

		if (dstRelList != null) {
			RelationInstance	ri;
			EntityInstance		src;

			for (en = dstRelList.elements(); en.hasMoreElements(); ) {
				ri  = (RelationInstance) en.nextElement();
				src = ri.getSrc();
				if (src != from || me != to) {
					if (from.hasDescendantOrSelf(src)) {
						ret = true;
						resultBox.addRelation(indent, ri);
						if (indent1 == indent) {
							indent1 = indent + "+- ";
						}
						setHighlightFlag(ri);
		}	}	}	}

		opened = false;
		for (en = me.getChildren(); en.hasMoreElements(); ) {
			e = (EntityInstance) en.nextElement();
			ret1 = openDstEdgeExpansion(e, from, to, indent1, resultBox);
			if (ret1 && !opened) {
				ret = opened = true;
				me.setStateOpen();
		}	}
		return(ret);
	}

	protected boolean handleEdgeExpansion(int key, Object object, ResultBox resultBox) 
	{
		Vector				edges;
		Enumeration			en;
		RelationInstance	ri;
		EntityInstance		src, dst, drawRoot;
		String				msg, msg1;

		edges = targetRelations(object);
		if (edges == null) {
			return false;
		}

		msg = null;

		switch (key) {
		case Do.EDGE_OPEN_LOW:
			msg1 = "Opened sources and destinations of edges.";
			break;
		case Do.EDGE_OPEN_SRC:
			msg1 = "Opened sources of edges.";
			break;
		case Do.EDGE_OPEN_DST:
			msg1 = "Opened destination of edges.";
			break;
		case Do.EDGE_CLOSE_LOW:
			msg1 = "Closed source and destination of edges";
			break;
		case Do.EDGE_CLOSE_SRC:
			msg1 = "Closed source of edges";
			break;
		default:
			msg1 = "Closed source and destination of edges";
			break;
		}

		resultBox.setResultTitle("SELECTED RELATIONS:");

		drawRoot = getDrawRoot();

		for (en = edges.elements(); en.hasMoreElements(); ) {
			ri = (RelationInstance) en.nextElement();
			setGroupAndHighlightFlag(ri);

			src = ri.getDrawSrc();
			dst = ri.getDrawDst();
			switch (key) {
			case Do.EDGE_OPEN_LOW:
				if (openEdgeExpansion(src, src, dst, "", resultBox)) {
					msg = msg1;
				}
				continue;
			case Do.EDGE_OPEN_SRC:
				if (openSrcEdgeExpansion(src, src, dst, "", resultBox)) {
					msg = msg1;
				}
				continue;
			case Do.EDGE_OPEN_DST:
				if (openDstEdgeExpansion(dst, src, dst, "", resultBox)) {
					msg = msg1;
				}
				continue;
			case Do.EDGE_CLOSE_LOW:
				dst = ri.getDst();
				src = ri.getSrc();
				for (;;) {
					src = src.getContainedBy();
					if (src == null) {
						break;
					}
					if (!drawRoot.hasDescendant(src)) {
						break;
					}
					if (src == dst || src.hasDescendant(dst)) {
						break;
					}
					if (src.setStateClosed()) {
						msg = msg1;
				}	}
				for (;;) {
					dst = dst.getContainedBy();
					if (dst == null) {
						break;
					}
					if (!drawRoot.hasDescendant(dst)) {
						break;
					}
					if (src == dst || dst.hasDescendant(src)) {
						break;
					}
					if (dst.setStateClosed()) {
						msg = msg1;
				}	}
				break;
			case Do.EDGE_CLOSE_SRC:
				dst = ri.getDst();
				src = ri.getSrc();
				for (;;) {
					src = src.getContainedBy();
					if (src == null) {
						break;
					}
					if (!drawRoot.hasDescendant(src)) {
						break;
					}
					if (src == dst || src.hasDescendant(dst)) {
						break;
					}
					if (src.setStateClosed()) {
						msg = msg1;
				}	}
				break;
			case Do.EDGE_CLOSE_DST:
				dst = ri.getDst();
				src = ri.getSrc();
				for (;;) {
					dst = dst.getContainedBy();
					if (dst == null) {
						break;
					}
					if (!drawRoot.hasDescendant(dst)) {
						break;
					}
					if (src == dst || dst.hasDescendant(src)) {
						break;
					}
					if (dst.setStateClosed()) {
						msg = msg1;
				}	}
				break;
			}
			resultBox.addRelation(ri);
		}

//		resultBox.activate();
		resultBox.done("-- End --");

		if (clearDrawEntities() || clearDrawEdges()) {
			revalidate();
		}

		if (msg != null) {
			m_ls.doFeedback(msg);
			return true;
		}

		switch (key) {
		case Do.EDGE_OPEN_LOW:
		case Do.EDGE_OPEN_SRC:
		case Do.EDGE_OPEN_DST:
			msg = "No further expansion is possible";
			break;
		default:
			msg = "No further contraction is possible";
		}	
		m_ls.error(msg);
		return false;
	}

	// Set the highlight flag on a relation that goes from me to dst

	protected void setHighlightFlag(EntityInstance src, EntityInstance dst, RelationClass rc)
	{
		Vector			 srcLiftedList = src.getSrcLiftedList();
		RelationInstance ri;
		int				 i;

		if (srcLiftedList != null) {
			for (i = srcLiftedList.size(); --i >= 0; ) {
				ri = (RelationInstance) srcLiftedList.elementAt(i);
				if (ri.getDrawDst() == dst && ri.getRelationClass() == rc) {
					setHighlightFlag(ri);
					break;
	}	}	}	}

	// rc is null if doing closure

	public void addToTracedList(boolean forward, EntityInstance e, EntityInstance te /* top entity*/, RelationClass rc, boolean markFlag, boolean groupingFlag, Vector list, int steps, boolean traceLifted)
	{
		setTracedFlag(e);
		
		if (steps == 0) {
			return;
		}
		Vector			 relList = e.getDstRelList();
		int				 size    = 0;
		RelationInstance ri;
		EntityInstance	 dst;
		int				 direction, i;

		for (direction = LandscapeClassObject.DIRECTION_REVERSED; direction != 0; --direction) {
			i = 0;
			if (relList == null) {
				// Have no list
				size = 0;
			} else {
				size = relList.size();
				if (rc != null) {
					int direction2 = rc.getActive();
					
					switch (direction2) {
					case LandscapeClassObject.DIRECTION_NORMAL:
					case LandscapeClassObject.DIRECTION_REVERSED:
						if (forward ^ (direction2 != direction)) {
							break;
						} 
					case 0:
						i = size;	// Skip
			}	}	}

			for (; i < size; ++i) {
				ri  = (RelationInstance) relList.elementAt(i);
				{
					RelationClass	rc1;
					EntityInstance	drawSrc, drawDst, temp;

					rc1 = ri.getRelationClass();
					if (rc == null) {
						int direction1 = rc1.getActive();
						
						switch (direction1) {
						case LandscapeClassObject.DIRECTION_NORMAL:
						case LandscapeClassObject.DIRECTION_REVERSED:
							if (forward ^ (direction1 != direction)) {
								break;
							} 
						case 0:
							continue;
						}
					} else {
						if (rc1 != rc) {
							continue;
					}	}
					drawSrc = ri.getDrawSrc();
					drawDst = ri.getDrawDst();
					
					if (direction == LandscapeClassObject.DIRECTION_NORMAL) {
						dst     = ri.getDst();
					} else {
						temp    = drawSrc;
						drawSrc = drawDst;
						drawDst = temp;
						dst     = ri.getSrc();
					}
				
					if (dst.isMarked(EntityInstance.NOT_IN_FOREST_MARK)) {
						// Already seen this
						continue;
					}
										
					if (drawSrc != null && drawDst != null && (drawSrc.isMarked(EntityInstance.DIAGRAM_MARK) || drawDst.isMarked(EntityInstance.DIAGRAM_MARK))) {
						// At least one of drawSrc/drawDst is in diagram
						
						if (markFlag) {
							setRedBoxFlag(drawDst);

							if (groupingFlag && drawDst.isMarked(EntityInstance.DIAGRAM_MARK)) {
								setEntityGroupFlag(drawDst);
							}
							if (direction != LandscapeClassObject.DIRECTION_NORMAL) {
								temp    = drawSrc;
								drawSrc = drawDst;
								drawDst = temp;
							}
							setHighlightFlag(drawSrc, drawDst, rc1);
					}	}
					dst.orMark(EntityInstance.NOT_IN_FOREST_MARK);

					if (rc == null) {
						/* With closure */
						if (markFlag) {
							list.addElement(ri);
						} else {
							/* For root cause */
							list.addElement(dst);
						}
						addToTracedList(forward, dst, dst, rc, markFlag, groupingFlag, list, steps-1, traceLifted);
					} else {
						if (e == te) {
							list.addElement(dst);
						} else {
							list.addElement(ri);
			}	}	}	}
			relList = e.getSrcRelList();
		}

		// If this entity is a closed entity we have to consider lifted edges from this source

		if (!te.isOpen() && traceLifted) {
			for (i = 0; i < size; ++i) {
				ri  = (RelationInstance) relList.elementAt(i);
				if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
					dst = ri.getDst();
					// Would be nice to look at NOT_IN_FOREST_MARK but can't be certain see lifted edge before setting this
					// Can't reduce number of steps, since this will mean a search for one step will never consider lifted edges
					addToTracedList(forward, dst, te, rc, markFlag, groupingFlag, list, steps, traceLifted);
		}	}	}
	}

	protected void clearFoundList(Vector list)
	{
		Object				object;
		EntityInstance		e;
		RelationInstance	ri;
		int					i;
		
		for (i = list.size(); --i >= 0; ) {
			object = list.elementAt(i);
			if (object instanceof EntityInstance) {
				e = (EntityInstance) object;
				e.nandMark(EntityInstance.NOT_IN_FOREST_MARK);
			} else if (object instanceof RelationInstance) {
				ri = (RelationInstance) object;
				e  = ri.getSrc();
				e.nandMark(EntityInstance.NOT_IN_FOREST_MARK);
				e  = ri.getDst();
				e.nandMark(EntityInstance.NOT_IN_FOREST_MARK);
	}	}	}
	
	// rc is null if doing closure

	protected int addTracedRelations(boolean forward, EntityInstance e, RelationClass rc, ResultBox resultBox, boolean groupingFlag, int steps, boolean traceLifted)
	{
		Vector			list          = new Vector();
		int				size;

		addToTracedList(forward, e, e, rc, true /* MarkFlag */, groupingFlag, list, steps, traceLifted);

		size = list.size();
		if (size > 0) {
			clearFoundList(list);
			resultBox.addRelations(e, rc, list, forward, (rc == null));
		}
		return size;
	}

	protected int addTracedRelationClasses(boolean forward, EntityInstance e, ResultBox resultBox, boolean groupingFlag, int steps, boolean traceLifted) 
	{
		Enumeration en;
		int			num  = 0;

		for (en = enumRelationClasses(); en.hasMoreElements(); ) {
			RelationClass rc = (RelationClass) en.nextElement();
//			System.out.println("Diagram.addTracedRelationClasses class " + rc);
			if (rc.isActive()) {
				num += addTracedRelations(forward, e, rc, resultBox, groupingFlag, steps, traceLifted);
//				System.out.println("Diagram.addTracedRelationClasses num " + num);
		}	}
		return num;
	}

	protected int showContents(EntityInstance e, ResultBox resultBox) 
	{
		Enumeration en;
		Vector list = new Vector();
		int n = 0;

		for (en = e.getChildren(); en.hasMoreElements(); ) {
			list.add(en.nextElement());
			n++;
		}
		if (n > 0) {
			SortVector.byString(list);
		}
		resultBox.addContents(e, list);
		return n;
	}

	protected int showContentsWithClosure(EntityInstance e, ResultBox resultBox)
	{ 
		Enumeration en;
		int			n;

		n = showContents(e, resultBox);

		for (en = e.getChildren(); en.hasMoreElements(); ) {
			n += showContentsWithClosure((EntityInstance) en.nextElement(), resultBox);
		}
		return n;
	}

	protected int doQueryEntity(EntityInstance e, int query, ResultBox resultBox, boolean groupingFlag, int steps) 
	{
		Option	option      = Options.getDiagramOptions();
		boolean	traceLifted = option.isTraceLifted();

		switch(query) {
		case Do.FORWARD_QUERY:
			return addTracedRelationClasses(true /* forward */, e, resultBox, groupingFlag, steps, traceLifted);

		case Do.BACKWARD_QUERY:
			return addTracedRelationClasses(false /* backwards */, e, resultBox, groupingFlag, steps, traceLifted);

		case Do.FORWARD_CLOSURE:
			return addTracedRelations(true /* forward */, e, null, resultBox, groupingFlag, steps, traceLifted);

		case Do.BACKWARD_CLOSURE:
			return 	addTracedRelations(false /* backedges */, e, null, resultBox, groupingFlag, steps, traceLifted);

		case Do.CONTENTS_QUERY:
			return showContents(e, resultBox);

		case Do.CONTENT_CLOSURE:
			return showContentsWithClosure(e, resultBox);
			
		default:
			return 0;
		}
	}

	private boolean rootCause1(EntityInstance e, EntityInstance te, Vector list, boolean groupingFlag)
	{
		Vector			 relList = e.getDstRelList();
		int				 size    = 0;
		boolean			 ret     = false;
		boolean			 ret1;
		RelationInstance ri;
		EntityInstance	 dst;
		int				 direction, i;

		if (e.isMarked(EntityInstance.NOT_IN_FOREST_MARK)) {
			// Already seen this
			return e.isMarked(EntityInstance.IN_SET_MARK);
		}
		e.orMark(EntityInstance.NOT_IN_FOREST_MARK);
		list.addElement(e);

		for (direction = LandscapeClassObject.DIRECTION_REVERSED; direction != 0; --direction) {
			size = ((relList == null) ? 0: relList.size());
			for (i = 0; i < size; ++i) {
				ri  = (RelationInstance) relList.elementAt(i);
				{
					RelationClass	rc1        = ri.getRelationClass();
					int				direction1 = rc1.getActive();
					EntityInstance	drawSrc, drawDst, temp;

					switch (direction1) {
					case LandscapeClassObject.DIRECTION_NORMAL:
					case LandscapeClassObject.DIRECTION_REVERSED:
						if (direction1 == direction) {
							break;
						} 
					case 0:
						continue;
					}
					drawSrc = ri.getDrawSrc();
					drawDst = ri.getDrawDst();
					
					if (direction == LandscapeClassObject.DIRECTION_NORMAL) {
						dst     = ri.getDst();
					} else {
						temp    = drawSrc;
						drawSrc = drawDst;
						drawDst = temp;
						dst     = ri.getSrc();
					}

					if (dst.isMarked(EntityInstance.IN_SET_MARK)) {
						ret1 = true;
					} else {
						ret1 = false;
					}
					ret1 |= rootCause1(dst, dst, list, groupingFlag);

					if (ret1) {
						ret = true;
						e.orMark(EntityInstance.IN_SET_MARK);

						if (drawSrc != null && drawDst != null && (drawSrc.isMarked(EntityInstance.DIAGRAM_MARK) || drawDst.isMarked(EntityInstance.DIAGRAM_MARK))) {
							if (groupingFlag) {
								if (e.isMarked(EntityInstance.DIAGRAM_MARK)) {
									setEntityGroupFlag(e);
							}	}
							if (direction != LandscapeClassObject.DIRECTION_NORMAL) {
								temp    = drawSrc;
								drawSrc = drawDst;
								drawDst = temp;
							}
							setHighlightFlag(drawSrc, drawDst, rc1);
				}	}	}
			}
			relList = e.getSrcRelList();
		}
			
		// If this entity is a closed entity we have to consider lifted edges from this source

		if (!te.isOpen()) {
			for (i = 0; i < size; ++i) {
				ri  = (RelationInstance) relList.elementAt(i);
				if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
					dst = ri.getDst();
					ret |= rootCause1(dst, te, list, false);
		}	}	}
		
		return ret;
	}
	 
	private int rootCause(Vector grp, ResultBox resultBox, boolean groupingFlag) 
	{
		int				size  = grp.size();
		Vector			list  = null;			// The list of potential root causes
		int				last  = 0;
		Vector			list1 = new Vector();
		EntityInstance	e, e1;
		int				i, j;

		for (i = 0; i < size; ++i) {
			e          = (EntityInstance) grp.elementAt(i);
			resultBox.addResultEntity(e, 0 /* Don't show ancestors */);

			list1.addElement(e);		// An object can itself be the root cause
					
			addToTracedList(true /* forward */, e, e, null /* Use closure */, false /* Don't mark */, false /* Don't group */, list1, -1, true);
//			System.out.println("" + i + ") " + list1);
			clearFoundList(list1);
			if (i == 0) {
				// Remember this list
				list  = list1;
				last  = list.size() - 1;
				list1 = new Vector();
			} else {
				// Delete every entityInstance in list not in list1
				// Do this fast by just looking at the IN_SET_MARK

				for (j = list1.size(); 0 <= --j; ) {
					e1 = (EntityInstance) list1.elementAt(j);
					e1.orMark(EntityInstance.IN_SET_MARK);
				}
				for (j = last; 0 <= j; --j) {
					e1 = (EntityInstance) list.elementAt(j);
					if (!e1.isMarked(EntityInstance.IN_SET_MARK)) {
						if (j != last) {
							list.setElementAt(list.elementAt(last), j);
						}
						list.removeElementAt(last);
						--last;
				}	}
				for (j = list1.size(); 0 <= --j; ) {
					e1 = (EntityInstance) list1.elementAt(j);
					e1.nandMark(EntityInstance.IN_SET_MARK);
				}
				list1.removeAllElements();
			}
			if (last < 0) {
				return 0;
		}	}
				
		SortVector.byPreorder(list, true /* ascending */);
		resultBox.addResultTitle("Collectively depend on");
		for (i = 0; i <= last; ++i) {
			e = (EntityInstance) list.elementAt(i);
			// Any path that arrives here from a source should be marked back on up
			e.orMark(EntityInstance.IN_SET_MARK);
			setRedBoxFlag(e);
			
			if (groupingFlag) {
				if (e.isMarked(EntityInstance.DIAGRAM_MARK)) {
					setEntityGroupFlag(e);
			}	}
			resultBox.addResultEntity(e, 0 /* Don't show ancestors */);
		}
		list1.removeAllElements();
		for (i = size; i > 0; ) {
			e = (EntityInstance) grp.elementAt(--i);
			rootCause1(e, e, list1, groupingFlag);
		}
		for (i = list1.size(); --i >= 0; ) {
			e = (EntityInstance) list1.elementAt(i);
			e.nandMark(EntityInstance.NOT_IN_FOREST_MARK | EntityInstance.IN_SET_MARK);
		}		
		return last+1;
	}
	
	public void queryEntity(int query, Object object, ResultBox resultBox, int forSteps) 
	{
		boolean			groupingFlag = Options.isGroupQuery();
		boolean			contentQuery = false;
		Vector			grp          = targetEntities(object);
		int				steps        = 1;
		Enumeration		en;
		String			title, footer;
		EntityInstance	ge;

		clearQueryFlags(false);

		switch(query) {
		case Do.CONTENTS_QUERY:
			title        = "CONTENTS";
			contentQuery = true;
			break;
		case Do.FORWARD_QUERY:
		case Do.BACKWARD_QUERY:
		{
			int	mode;

			if (query == Do.FORWARD_QUERY) {
				title = "FORWARD QUERY";
			} else {
				title = "BACKWARD QUERY";
			}
			mode = 0;
			if (grp != null) {
				for (en = grp.elements(); en.hasMoreElements(); ) {
					ge = (EntityInstance) en.nextElement();
					if (ge.isMarked(EntityInstance.CLIENT_SUPPLIER)) {
						mode |= 2;
					} else {
						mode |= 1;
			}	}	}
			switch (mode) {
			case 2:
				title += " IN DIAGRAM";
				break;
			case 3:
				title += " IN DIAGRAM WHEN CLIENT/SUPPLIER";
				break;
			}
			break;
		}
		case Do.CONTENT_CLOSURE:
			contentQuery = true;
		case Do.FORWARD_CLOSURE:
		case Do.BACKWARD_CLOSURE:
		case Do.ROOT_CAUSE:
			switch (query) {
			case Do.FORWARD_CLOSURE:
				title = "FORWARD QUERY ";
				break;
			case Do.BACKWARD_CLOSURE:
				title = "BACK QUERY ";
				break;
			case Do.CONTENT_CLOSURE:
				title = "CONTENTS";
				break;
			default:
				title = "FORWARD ROOT CAUSE ";
			}
			steps = forSteps;
			if (steps < 0) {
				steps = m_diagramOptions.getChaseEdges();
			}
			break;
		default:
			title = null;
		}
		if (steps < 0) {
			title += "(closure)";
		} else {
			title += "(+" + steps + ")";
		}
		resultBox.setResultTitle(title);

		if (grp == null || grp.isEmpty()) {
			grp = new Vector();
			grp.addElement(getDrawRoot());
		}

		int num = 0;
		
		if (query == Do.ROOT_CAUSE) {
			num = rootCause(grp, resultBox, groupingFlag);
		} else {
			for (en = grp.elements(); en.hasMoreElements(); ) {
				ge = (EntityInstance) en.nextElement();
				num += doQueryEntity(ge, query, resultBox, groupingFlag, steps);
		}	}

		if (num == 0) {
			footer = "NO ENTITIES";
		} else {
			footer = "-- End of Query --";
		}

		resultBox.done(footer);
		resultBox.activate();

		if (!contentQuery) {
			// Only show the highlight edges
			clearDrawEntities();
			clearDrawEdges();
			revalidate();
	}	}

	public boolean newEntityClass()
	{
		String		id;
		String		message;
		EntityClass	ec;

		message = "Enter new entry class name";
		for (;;) {
			id = JOptionPane.showInputDialog(message); 
			if (id == null) {
				return false;
			}
			ec = getEntityClass(id);
			if (ec == null) {
				beginUndoRedo("New entity class");
				updateNewEntityClass(id, m_entityBaseClass);
				endUndoRedo();

				return true;
			}
			message = id + " in use. Enter new name";
		}
	}

	public RelationClass newRelationClass()
	{
		String			id;
		String			message;
		RelationClass	rc;

		message = "Enter new relation class name";
		for (;;) {
			id = JOptionPane.showInputDialog(message); 
			if (id == null) {
				return null;
			}
			id = id.trim();
			if (id == null) {
				return null;
			}
			rc = getRelationClass(id);
			if (rc == null) {
				rc = updateNewRelationClass(id, m_relationBaseClass);
				return rc;
			}
			message = id + " in use. Enter new name";
		}
	}

	public void alsoValidateSubclasses(Vector v, String title)
	{
		String					message;
		LandscapeClassObject	first, o;
		int						i, size;

		size = v.size();
		switch (size) {
		case 1:
			return;
		case 2:
			o = (LandscapeClassObject) v.elementAt(1);
			message = "Also validate subclass " + o.getLabel();
			break;
		default:
			o = (LandscapeClassObject) v.elementAt(1);
			message = "Also validate subclasses " + o.getLabel();
			for (i = 2; i < size; ) {
				o = (LandscapeClassObject) v.elementAt(i);
				++i;
				if (i == size) {
					message += " and ";
				} else {
					message += ", ";
				}
				message += o.getLabel();
		}	}

		first = (LandscapeClassObject) v.elementAt(0);

		switch (JOptionPane.showConfirmDialog(null, message, "Validating " + first.getLabel() + title, JOptionPane.YES_NO_CANCEL_OPTION)) {
		case JOptionPane.YES_OPTION:
			break;
		case JOptionPane.NO_OPTION:
			v.removeAllElements();
			v.add(first);
			break;
		default:
			v.removeAllElements();
		}
		return;
	}

	public void showValidAttributes(LandscapeClassObject o, ResultBox resultBox)
	{
		Vector		v;
		int			i, size;
		Attribute	attribute;

		resultBox.addResultTitle("Valid attributes of " + o.getLabel() );
		v = o.getValidAttributes();
		size = v.size();
		for (i = 0; i < size; ++i) {
			attribute = (Attribute) v.elementAt(i);
			resultBox.addResultAttribute(attribute);
		}

		resultBox.addResultTitle("Lsedit class attributes" );
		o.reportClassAttributes(resultBox);
		resultBox.addResultTitle("Lsedit first order attributes");
		if (o instanceof EntityClass) {
			EntityInstance.reportFirstOrderAttributes(resultBox);
		} else {
			RelationInstance.reportFirstOrderAttributes(resultBox);
		}
		resultBox.done("End of report");
		resultBox.activate();
	}

	public void validateEntityAttributes(EntityClass ec, ResultBox resultBox, boolean query)
	{
		Vector		v;
		Vector		a[];
		int			errors;
		String		message;

		v      = getClassAndSubclasses(ec);

		if (query) {
			alsoValidateSubclasses(v, " attributes");
			if (v.size() == 0) {
				return;
		}	}

		a      = new Vector[v.size()];
		resultBox.addResultTitle("Validating attributes of " + ec.getLabel() + ((v.size() <= 1) ? "" : " and subclasses") + ((m_drawRoot == m_rootInstance) ? "" : " under " + m_drawRoot.getEntityLabel()) );
		resultBox.activate();

		// Never validate the root instance 
		// This instance contains all sorts of attributes that carry flags

		if (m_drawRoot != m_rootInstance) {
			errors = m_drawRoot.validateEntityAttributes(v, a, resultBox);
		} else {
			Enumeration		en;
			EntityInstance	child;

			errors = 0;
			for (en = m_rootInstance.getChildren(); en.hasMoreElements(); ) {
				child = (EntityInstance) en.nextElement();
				errors += child.validateEntityAttributes(v, a, resultBox);
		}	}


		switch (errors) {
		case 0:
			message = "No errors";
			break;
		case 1:
			message = "1 erroneous entity encountered during validation";
			break;
		default:
			message = errors + " erroneous entities encountered during validation";
		}
		resultBox.done(message);
	}

	public void validateRelationAttributes(RelationClass rc, ResultBox resultBox, boolean query)
	{
		Vector	v;
		Vector	a[];
		int		errors;
		String	message;

		v      = getClassAndSubclasses(rc);
		if (query) {
			alsoValidateSubclasses(v, " attributes");
			if (v.size() == 0) {
				return;
		}	}

		a      = new Vector[v.size()];
		resultBox.addResultTitle("Validating attributes of " + rc.getLabel() + ((v.size() <= 1) ? "" : " and subclasses") + ((m_drawRoot == m_rootInstance) ? "" : " under " + m_drawRoot.getEntityLabel()) );
		resultBox.activate();

		// Never validate the root instance 
		// This instance contains all sorts of attributes that carry flags

		errors = m_drawRoot.validateRelationAttributes(v, a, resultBox);

		switch (errors) {
		case 0:
			message = "No errors";
			break;
		case 1:
			message = "1 erroneous relation encountered during validation";
			break;
		default:
			message = errors + " erroneous relations encountered during validation";
		}
		resultBox.done(message);
	}

	public void validateRelations(RelationClass rc, ResultBox resultBox, boolean query)
	{
		Vector	v;
		boolean	a[][][];
		int		errors;
		String	message;

		v      = rc.getClassAndSubclasses(m_relationClasses);
		if (query) {
			alsoValidateSubclasses(v, " constraints");
			if (v.size() == 0) {
				return;
		}	}

		a      = new boolean[v.size()][][];
		resultBox.addResultTitle("Validating " + rc.getLabel() + " relations" + ((v.size() <= 1) ? "" : " and subclasses") + ((m_drawRoot == m_rootInstance) ? "" : " under " + m_drawRoot.getEntityLabel()) );
		resultBox.activate();

		// Never validate the root instance 
		// This instance contains all sorts of attributes that carry flags

		m_drawRoot.clearValidatedMark();
		errors = m_drawRoot.validateRelations(v, a, resultBox, m_rootInstance);

		switch (errors) {
		case 0:
			message = "No errors";
			break;
		case 1:
			message = "1 erroneous relation encountered during validation";
			break;
		default:
			message = errors + " erroneous relations encountered during validation";
		}
		resultBox.done(message);
	}

	public void validateAll(ResultBox resultBox)
	{
		validateEntityAttributes(m_entityBaseClass, resultBox, false);
		validateRelationAttributes(m_relationBaseClass, resultBox, false);
		validateRelations(m_relationBaseClass, resultBox, false);
	}

	public void redistribute()
	{
		m_ls.setCursor(Cursor.WAIT_CURSOR);
		beginUndoRedo("Redistribute nodes");
		m_ls.doFeedback(Recluster.layout(this));
		endUndoRedo();
		m_ls.setCursor(Cursor.DEFAULT_CURSOR);
	}

	public void group_unconnected()
	{
		int				seen = 0;
		EntityInstance	e;
		int				i;
		Vector			v;
		String			message;
		boolean			cleared;
		
		
		v = getClusterGroup();
		if (v == null) {
		  Util.beep();
		  message = "No group selected";
		} else {
			cleared = clearFlags(false);

			for (i = 0; i < v.size(); ++i) {
				e = (EntityInstance) v.elementAt(i);
				if (e.hasNoEdges()) {
					setEntityGroupFlag(e);
					++seen;
			}	}

			if (seen == 0) {
				message = "No suitable entities detected";
			} else {
				message = seen + " entities selected";
			}
			if (cleared) {
				revalidate();
		}	}
		m_ls.doFeedback(message);
	}

	public void clusterMetrics()
	{
		if (m_clusterMetrics == null) {
			m_clusterMetrics = new ClusterMetrics(m_ls);
		}

		m_clusterMetrics.init(m_drawRoot);
		m_drawRoot.clusterMetrics(m_clusterMetrics, 1);
		m_clusterMetrics.showit();
	}

	public void checkRefcnts(ResultBox resultBox)
	{
		resultBox.activate();
		resultBox.setResultTitle("Checking refcnt's");

		if (m_rootInstance != null) {
			m_rootInstance.checkRefcnts(resultBox);
		}
		resultBox.done("End of report");
	}

	public int clearElisions(Object object)
	{
		Vector			grp        = getTargetGroup(object);
		Enumeration		en;
		EntityInstance	e;
		int				ret        = 0;

		if (grp != null) {
			for (en = grp.elements(); en.hasMoreElements(); ) {
				e = (EntityInstance) en.nextElement();
				ret += e.clearElisions();
		}	}
		return ret;
	}
	
	public int toggleDescendants(Object object)
	{
		Vector			grp        = getTargetGroup(object);
		Enumeration		en;
		EntityInstance	e;
		int				ret        = 0;

		if (grp != null) {

			for (en = grp.elements(); en.hasMoreElements(); ) {
				e = (EntityInstance) en.nextElement();
				if (e.isMarked(EntityInstance.CLOSED_MARK)) {
					ret += e.openDescendants();
				} else {
					ret += e.closeDescendants();
		}	}	}
		return ret;
	}

	public void resetIOpoints()
	{
		if (m_rootInstance != null) {

			int		ret;

			ret = JOptionPane.showConfirmDialog(m_ls.getFrame(), "Set IO points associated with active entities and relations to default?", "Cancel operation?", JOptionPane.OK_CANCEL_OPTION);
			if (ret == JOptionPane.OK_OPTION) {
				m_rootInstance.resetIOpoints();
	}	}	}
	
	// Called from the ResizeModeHandler
	// Input rectangle is size of desired entity e using diagram bounds
	// Presumes parent of this entityInstance is an entityInstance

	public void resizeEntity(EntityInstance e, Rectangle lyt)
	{
		EntityComponent	entityComponent = e.getEntityComponent();
		EntityComponent	parentComponent = (EntityComponent) entityComponent.getParent();
		int				parentWidth     = parentComponent.getWidth();
		int				parentHeight    = parentComponent.getHeight();
		double			xRelLocal, yRelLocal, widthRelLocal, heightRelLocal;

		if (parentWidth <= 0) {
			xRelLocal = widthRelLocal = 0.1;
		} else {
			widthRelLocal = (double) parentWidth;
			xRelLocal     = ((double) (lyt.x - parentComponent.getDiagramX())) / widthRelLocal;
			widthRelLocal = ((double) lyt.width) / widthRelLocal;
		}

		if (parentHeight <= 0) {
			yRelLocal = heightRelLocal = 0.1;
		} else {
			heightRelLocal = (double) parentHeight;
			yRelLocal      = ((double) (lyt.y - parentComponent.getDiagramY())) / heightRelLocal;
			heightRelLocal = ((double) lyt.height) / heightRelLocal;
		}
		beginUndoRedo("Resize " + e);
		updateRelLocal(e, xRelLocal, yRelLocal, widthRelLocal, heightRelLocal);
		endUndoRedo();
	}

	public void containsClassesChanging()
	{
		Vector			oldChildren        = null;

		if (getContainsClasses() != null && Options.getDiagramOptions().isFocusAncestor()) {
			Enumeration en;
			
			// This step doesn't depend on the contains hierarchy
			oldChildren = getGroupedEntities();
			if (oldChildren == null) {
				oldChildren = new Vector();
			}
			if (oldChildren.size() == 0) {
				for (en = m_drawRoot.getChildren(); en.hasMoreElements(); ) {
					oldChildren.add(en.nextElement());
				}
				if (oldChildren.size() == 0) {
					oldChildren = null;
		}	}	}
		m_oldChildren = oldChildren;
	}

	public void containsClassesChanged()
	{
		Vector	oldChildren = m_oldChildren;

		clearFlags(true);
		if (oldChildren != null) {
			EntityInstance	drawRoot = getDrawRoot();
			EntityInstance	newDrawRoot;

			for (newDrawRoot = drawRoot; newDrawRoot != null && !newDrawRoot.hasDescendantsOrSelf(oldChildren); newDrawRoot = newDrawRoot.getContainedBy());
			if (newDrawRoot != drawRoot && newDrawRoot != null) {
				navigateTo(newDrawRoot, false);
			}
			m_oldChildren = null;
		}
	}

	//  Event handling

	public void entityPressed(MouseEvent ev, EntityInstance e, int x, int y /* x, y are the diagram coordinate where the press occurred */)
	{
		LandscapeEditorCore		ls          = m_ls;
		LandscapeModeHandler	modeHandler = ls.getModeHandler();

		ls.showDescription(e, true);
		
		modeHandler.entityPressed(ev, e, x, y);
	}

	public void entityDragged(MouseEvent ev, EntityInstance e, int x, int y)
	{		
		LandscapeModeHandler	modeHandler = m_ls.getModeHandler();

		modeHandler.entityDragged(ev, e, x, y);
	}

	public void entityReleased(MouseEvent ev, EntityInstance e, int x, int y)
	{
		LandscapeEditorCore		ls          = m_ls;
		LandscapeModeHandler	modeHandler = ls.getModeHandler();

//		System.out.println("Diagram.entityReleased=" + e);

		modeHandler.entityReleased(ev, e, x, y);
		if (y > 0) {
			ls.m_toolButton[0].requestFocus();
			ls.requestFocus();
	}	}		

	public void relationPressed(MouseEvent ev, RelationInstance ri, int x, int y)
	{
		LandscapeEditorCore	 ls          = m_ls;
		LandscapeModeHandler modeHandler = ls.getModeHandler();

//		System.out.println("Diagram.relationPressed=" + ri);

		ls.showDescription(ri, true);
		modeHandler.relationPressed(ev, ri, x, y);
	}
	
	public void relationDragged(MouseEvent ev, RelationInstance ri, int x, int y)
	{		
		LandscapeModeHandler modeHandler = m_ls.getModeHandler();
		
		modeHandler.relationDragged(ev, ri, x, y);
	}
		
	public void relationReleased(MouseEvent ev, RelationInstance ri, int x, int y)
	{
		LandscapeEditorCore	 ls          = m_ls;
		LandscapeModeHandler modeHandler = ls.getModeHandler();

//		System.out.println("Diagram.relationReleased=" + ri);

		modeHandler.relationReleased(ev, ri, x, y);
		if (y > 0) {
			ls.m_toolButton[0].requestFocus();
			ls.requestFocus();
	}	}

	public void movedOverThing(MouseEvent ev, Object thing, int x, int y)
	{
		LandscapeModeHandler modeHandler = m_ls.getModeHandler();
		
		modeHandler.movedOverThing(ev, thing, x, y);
	}

	public void doUpdateInherits(LandscapeClassObject object, Vector value)
	{
		beginUndoRedo("Update inherits");
		updateInherits(object, value);
		endUndoRedo();
	}

	public void doUpdateRemoveEntityClass(EntityClass ec)
	{
		beginUndoRedo("Delete " + ec.getLabel() + " entity class");
		updateRemoveEntityClass(ec);
		clearGroupFlags();
		endUndoRedo();
	}

	public void doUpdateRemoveRelationClass(RelationClass rc)
	{
		beginUndoRedo("Delete " + rc.getLabel() + " relation class");
		updateRemoveRelationClass(rc);
		endUndoRedo();
	}

	private boolean m_updating = false;

	// TaListener interface

/*
	private void testUpdate(String type, int signal)
	{
		System.out.println("Diagram.taSignal " + type + " (" + taSignal(signal) + ")");

		if (!m_updating) {
			System.out.println("Diagram.taSignal " + type + " (" + taSignal(signal) + ")");
			System.out.println("Diagram missed updateBegins");
			java.lang.Thread.dumpStack();
	}	}
 */

	/* N.B. When we move things around necessitating revalidation of edges connected to
	 *      these things we can't just revalidate and paint these things. We have to
	 *      repaint all edges if we want them to remain on top of the things seen.
	 *		Using a mark is better in some ways than validating edges when things
	 *      move because if we move both end points we would otherwise validate twice
	 */

	private void refillEdges()
	{
		Container			edges = m_edges;
		RelationComponent	component;
		RelationInstance	ri;
		int					i;

//		System.out.println("RefillEdges");

		for (i = edges.getComponentCount(); --i >= 0; ) {
			component = (RelationComponent) edges.getComponent(i);
			ri        = component.getRelationInstance();
//			System.out.println("Refilling " + ri + " " + (!ri.isMarked(RelationInstance.FILLED_MARK)));
			if (!ri.isMarked(RelationInstance.FILLED_MARK)) {
				ri.orMark(RelationInstance.FILLED_MARK);
				component.fill();
			}
	}	}

	public void diagramChanging(Diagram diagram)
	{
	}

	// We can ignore this signal because we always do a navigateTo when the
	// diagram changes to force a refill

	public void diagramChanged(Diagram diagram, int signal)
	{
	}

	public void updateBegins()
	{
		if (m_updating) {
			System.out.println("Diagram nested updateBegins");
			java.lang.Thread.dumpStack();
		}
//		System.out.println("Update begins");
		m_updating = true;
	}

	public void updateEnds()
	{
		int	paintFlags = m_flags & PAINT_FLAGS;

//		System.out.println("Update ends " + paintFlags);

		if (!m_updating) {
			System.out.println("Diagram not-nested updateEnds");
			java.lang.Thread.dumpStack();
		}
		m_updating = false;

		if (paintFlags != 0) {
			m_flags &= ~PAINT_FLAGS;
			if ((paintFlags & REFILL_FLAG) != 0) {
				revalidate();
				return;
			}
			if ((paintFlags & REFILL_EDGES_FLAG) != 0) {
				refillEdges();
			}
			if ((paintFlags & RESHADE_FLAG) != 0) {
				m_drawRoot.computeShading();
			}
			if ((paintFlags & REPAINT_FLAG) != 0) {
				repaint();
			}
	}	}

	public void iconsChange(EntityClass ec)
	{
		EntityInstance root = getRootInstance();

		if (root != null) {
			root.entityIconChanges(ec);
		}
	}
	
	public void iconPathChanged()
	{
		Enumeration		en   = enumEntityClasses();
		EntityInstance	root = getRootInstance();
		EntityClass		ec;

		for (; en.hasMoreElements(); ) {
			ec = (EntityClass) en.nextElement();
			ec.clearIcons();
		}
		if (root != null) {
			root.entityIconChanges(null);
		}
	}
	
	public void entityClassChanged(EntityClass ec, int signal)
	{
//		testUpdate(ec + " entityClassChanged", signal);

		switch (signal) {
			case TaListener.EC_ANGLE_SIGNAL:
			case TaListener.STYLE_SIGNAL:
			{
				EntityInstance	root    = getRootInstance();

				ec.adjustEdgePoints();
				if (root != null) {
					root.shapeChanges(ec);
				}
				m_flags |= REPAINT_FLAG;
				break;
			}
			case TaListener.EC_IMAGE_SIGNAL:
			case TaListener.COLOR_SIGNAL:
			case TaListener.OPEN_COLOR_SIGNAL:
			case TaListener.LABEL_COLOR_SIGNAL:
			{
				EntityInstance	root    = getRootInstance();

				if (root != null) {
					root.entityAppearanceChanges(ec);
				}
				m_flags |= REPAINT_FLAG;
				break;
			}
			case TaListener.EC_ICON_SIGNAL:
			{	
				iconsChange(ec);
				m_flags |= REPAINT_FLAG;
				break;
			}
		}
	}

	public void relationClassChanged(RelationClass rc, int signal)
	{
//		testUpdate(rc + " relationClassChanged", signal);

		switch (signal) {
		case TaListener.STYLE_SIGNAL:
		case TaListener.COLOR_SIGNAL:
		{
			EntityInstance	root = getRootInstance();
			if (root != null) {
				root.edgeAppearanceChanges(rc);
			}
			m_flags |= REPAINT_FLAG;
			break;
		}
		case TaListener.RC_IOFACTOR_SIGNAL:
		{
			m_flags |= REFILL_FLAG;
			break;
		}
		case TaListener.CONTAINS_CHANGING_SIGNAL:
			containsClassesChanging();
			m_flags |= REFILL_FLAG;
			break;
		case TaListener.CONTAINS_CHANGED_SIGNAL:
			containsClassesChanged();
			m_flags |= REFILL_FLAG;
			break;
		}
	}

	public void entityParentChanged(EntityInstance e, EntityInstance parent, int signal)
	{
//		testUpdate(parent + "." + e + " entityParentChanged", signal);

		switch (signal) {
		case TaListener.DRAWROOT_CUTTING_SIGNAL:
			navigateTo(parent, true);
			break;
		case TaListener.CONTAINER_CUTTING_SIGNAL:
		{
			clearEntityGroupFlag(e);
			break;
		}
		case TaListener.ENTITY_CUTTING_SIGNAL:
		{
			Vector			groupedEntities = m_groupedEntities;
			EntityInstance	grouped;
			int				i;

			for (i = groupedEntities.size(); --i >= 0; ) {
				grouped = (EntityInstance) groupedEntities.elementAt(i);
				if (e.hasDescendantOrSelf(grouped)) {
					clearEntityGroupFlag(grouped);
			}	}
			break;
		}
		case TaListener.ENTITY_NEW_SIGNAL:
		case TaListener.ENTITY_RELOCATED_SIGNAL:
		case TaListener.ENTITY_CUT_SIGNAL:
		case TaListener.ENTITY_PASTED_SIGNAL:
		case TaListener.CONTAINER_PASTED_SIGNAL:
			m_flags |= REFILL_FLAG;
			break;
		}
	}

	public void relationParentChanged(RelationInstance ri, int signal)
	{
//		testUpdate(ri + " relationParentChanged", signal);

		m_flags |= REFILL_FLAG;
	}

	public void entityInstanceChanged(EntityInstance e, int signal)
	{
//		testUpdate(e + " entityInstanceChanged", signal);

		switch (signal) {
		case TaListener.POSITION_SIGNAL:
		case TaListener.BOUNDS_SIGNAL:
		case TaListener.SIZE_SIGNAL:
			e.setEntityBounds();
			m_flags |= (REFILL_EDGES_FLAG | RESHADE_FLAG | REPAINT_FLAG);
		default:
			e.repaint();
	}	}

	public void relationInstanceChanged(RelationInstance ri, int signal)
	{
		RelationComponent	component = ri.getRelationComponent();

//		testUpdate(ri + " relationInstanceChanged", signal);

		switch (signal) {
		case TaListener.STYLE_SIGNAL:
			if (component != null) {
				component.styleChanged(ri.getInheritedStyle());
			}
		default:
			if (component != null) {
				component.repaint();
	}	}	}

	// MouseMotionListener interface

	public void mouseDragged(MouseEvent ev)
	{
	}

	public void mouseMoved(MouseEvent ev)
	{
//		System.out.println("Diagram.mouseMoved");
		movedOverThing(ev, this, ev.getX(), ev.getY());
	}
}

