package lsedit;

import java.awt.Event;
import java.awt.event.KeyEvent;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.KeyStroke;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/* Function:
 * Clearly identify the available accelerator keys used in lsedit
 * Encapsulate the construction of menus so that menu's can be easily changed
 * Manage state of checkboxes contained with in menus
 */

class MyCheckBoxMenuItem extends JCheckBoxMenuItem implements ActionListener
{
	ToolBarEventHandler	m_handler;
	int					m_modifiers;
	int					m_key;

	MyCheckBoxMenuItem(JComponent menu, String label, boolean state, ToolBarEventHandler handler, int modifiers, int key, String tooltipHelp)
	{
		super(label);
		setSelected(state);
		m_handler   = handler;
		if (menu instanceof JMenu) {
			((JMenu) menu).add(this);
		} else {
			((JPopupMenu) menu).add(this);
		}
		if (key <= 'Z') {
			if (key >= 'A') {
				modifiers |= Event.SHIFT_MASK;
			}
		} else if (key <= 'z') {
			if (key >= 'a') {
				modifiers &= ~Event.SHIFT_MASK;
				key += 'A' - 'a';
			}
		} else if (key >= Do.FUNCTION_KEY) {
			key -= Do.FUNCTION_KEY;
		}
		m_modifiers = modifiers;
		m_key       = key;

		if (tooltipHelp != null) {
			setToolTipText(tooltipHelp);
		}
		setAccelerator(KeyStroke.getKeyStroke(key, modifiers));
		addActionListener(this);
	}

	public int getKey()
	{
		return(m_key);
	}

	public void actionPerformed(ActionEvent ev)
	{
//		System.out.println("CheckBoxMenu clicked");
		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (m_handler.processMetaKeyEvent(getText())) {
				return;
		}	}
		m_handler.processKeyEvent(m_key, m_modifiers, null);
	}
}

public class Do 
{
	/* Utter stupidity -- the set accelerator wants the Upper case value of a letter even if it is lower
	 * case and VK_F1 is defined to have the same value as a 'p'.  So if a lower case 'p' is pressed
	 * it is ambiguous if this is really a 'p' or a F1, unless one converts to upper case and sets SHIFT_MASK.
	 * We don't want to do this stupidity since it messes up all our case statements so do a cludge
	 */

	 protected static final int FUNCTION_KEY   = 1024;

	/* N.B. For a key to be active it **MUST** be in a permanent menu -- this ensures that it is treated as an
	 * accelerator key
	 */

	/* Control keys */

	protected static final int GROUP_ALL       = 'a';
	protected static final int SELECT_BROWSER  = 'b';
	protected static final int COPY            = 'c';	/* Reserved for future use */
	protected static final int SET_TO_VIEWPORT = 'd';
	protected static final int CLEAR_ELISIONS  = 'e';
	protected static final int FIND_QUERY      = 'f';
	protected static final int SELECT_COMMAND  = 'h';
	protected static final int NAV_NEXT        = 'j';
	protected static final int NAV_PREV        = 'k';
	protected static final int OPEN_SETTINGS   = 'l';
	protected static final int NEW_LANDSCAPE   = 'n';
	protected static final int OPEN_LANDSCAPE  = 'o';
	protected static final int PRINT_LANDSCAPE = 'p';
	protected static final int QUIT_PROGRAM    = 'q';
	protected static final int REFRESH         = 'r';
	protected static final int SAVE            = 's';	/* May be combined with ALT to save RAW */
	protected static final int SAVE_AS         = 'S';
	protected static final int SWITCH_TOC      = 't';
	protected static final int PASTE           = 'v';
	protected static final int CLOSE_LANDSCAPE = 'w';
	protected static final int CUT_GROUP       = 'x';
	protected static final int REDO			   = 'y';
	protected static final int UNDO            = 'z';

	/* Alt keys */

	protected static final int A_HORIZ_TOP       = KeyEvent.VK_UP;
	protected static final int A_VERTICAL_LEFT   = KeyEvent.VK_LEFT;
	protected static final int A_VERTICAL_RIGHT  = KeyEvent.VK_RIGHT;
	protected static final int A_HORIZ_BOTTOM    = KeyEvent.VK_DOWN;
	protected static final int EDIT_ELISIONS     = 'a';
	protected static final int REDBOX_ENTITY     = 'b';
	protected static final int EDGE_CLOSE_DST    = 'c';
	protected static final int EDGE_CLOSE_SRC    = 'C';
	protected static final int REMOVE_DEAD       = 'D';
	protected static final int NEW_ECLASS        = 'e';
	protected static final int NEW_RCLASS        = 'E';
	protected static final int A_FIT_LABEL       = 'f';
	protected static final int SET_FONT          = 'F';
	protected static final int A_GROUP           = 'g';
	protected static final int SPC_VERTICAL      = 'H';
	protected static final int SZ_HEIGHT         = 'h';
	protected static final int EDGE_CLOSE_LOW    = 'I';
	protected static final int EDGE_OPEN_LOW     = 'i';
	protected static final int RELAYOUT_ALL      = 'j';
	protected static final int NAVIGATE_TO       = 'J';
	protected static final int DECREASE_MAG      = 'm';
	protected static final int INCREASE_MAG      = 'M';
	protected static final int EDGE_NAVIGATE_DST = 'n';
	protected static final int EDGE_NAVIGATE_SRC = 'N';
	protected static final int EDGE_OPEN_SRC     = 'O';
	protected static final int EDGE_OPEN_DST     = 'o';
	protected static final int TOC_PATH          = 'p';
	protected static final int CLOSE_ALL		 = 'r';
	protected static final int OPEN_ALL          = 'R';
	protected static final int SZ_WIDTH          = 'w';
	protected static final int SPC_HORIZ         = 'W';
	protected static final int SZ_WIDTH_HEIGHT   = 'x';
	protected static final int A_HORIZ_CENTER    = 'X';
	protected static final int A_VERTICAL_CENTER = 'Y';
	protected static final int CHECK_REFCNTS     = 'z';
	protected static final int VALIDATE_ALL      = 'Z';

	/* Normal upper keys */

	protected static final int EDIT_ENTITY_CLASS = 'A';
	protected static final int BACKWARD_CLOSURE  = 'B';
	protected static final int NEW_EDGE          = 'E';
	protected static final int FORWARD_CLOSURE   = 'F';
	protected static final int ADD_GROUP_REDBOXES= 'G';
	protected static final int AVOID_COLLISIONS  = 'H';
	protected static final int INTERNAL_EDGES    = 'I';
	protected static final int CLUSTER_METRICS   = 'K';
	protected static final int SPRING_LAYOUT     = 'L';
	protected static final int SUGIYAMA_LAYOUT   = 'N';
	protected static final int ACDCCLUSTER_LAYOUT = 'P';
	protected static final int BUNCHCLUSTER_LAYOUT = 'Q';
	protected static final int ROOT_CAUSE        = 'R';
	protected static final int EXITING_EDGES     = 'S';
	protected static final int INCREASE_LABEL_FONT = 'T';
	protected static final int ENTERING_EDGES    = 'U';
	protected static final int CONTENT_CLOSURE   = 'V';
	protected static final int REDISTRIBUTE      = 'W';
	protected static final int INCREASE_WIDTH    = 'X';
	protected static final int INCREASE_HEIGHT   = 'Y';
	protected static final int INCREASE_SIZE     = 'Z';

	/* Special upper case keys */

	protected static final int TOGGLE_RELATION_ALL = '0';
	protected static final int TOGGLE_RELATION_1   = '1';
	protected static final int TOGGLE_RELATION_2   = '2';
	protected static final int TOGGLE_RELATION_3   = '3';
	protected static final int TOGGLE_RELATION_4   = '4';
	protected static final int TOGGLE_RELATION_5   = '5';
	protected static final int TOGGLE_RELATION_6   = '6';
	protected static final int TOGGLE_RELATION_7   = '7';
	protected static final int TOGGLE_RELATION_8   = '8';
	protected static final int TOGGLE_RELATION_9   = '9';

	protected static final int ASCEND              = KeyEvent.VK_ENTER;
	

	/* Normal lower keys */

	protected static final int EDIT_ATTRIBUTES   = 'a';
	protected static final int BACKWARD_QUERY    = 'b';
	protected static final int SHOW_CONTENTS     = 'c';
	protected static final int TOGGLE_DESCENDANTS  = 'd';
	protected static final int NEW_ENTITY        = 'e';
	protected static final int FORWARD_QUERY     = 'f';
	protected static final int GROUP_REDBOXES    = 'g';
	protected static final int SHOW_SOURCECODE   = 'h';
	protected static final int CLUSTER_INTERFACE = 'i';
	protected static final int CLUSTER_LAYOUT    = 'j';
	protected static final int DELETE_CONTAINER  = 'k';
	protected static final int SPRING_LAYOUT2    = 'l';
	protected static final int MATRIX_LAYOUT     = 'm';
	protected static final int SIMPLEX_LAYOUT    = 'n';
	protected static final int SHOW_OPTIONS      = 'o';
	protected static final int AACLUSTER_LAYOUT  = 'p';
	protected static final int EXPAND_LAYOUT     = 'r';
	protected static final int SRC_EDGES         = 's';
	protected static final int DECREASE_LABEL_FONT = 't';
	protected static final int DST_EDGES         = 'u';
	protected static final int CONTENTS_QUERY    = 'v';
	protected static final int GROUP_UNCONNECTED = 'w';
	protected static final int DECREASE_WIDTH    = 'x';
	protected static final int DECREASE_HEIGHT   = 'y';
	protected static final int DECREASE_SIZE     = 'z';

	protected static final int TOGGLE_LEGEND_ALL = '0';
	protected static final int TOGGLE_LEGEND_1   = '1';
	protected static final int TOGGLE_LEGEND_2   = '2';
	protected static final int TOGGLE_LEGEND_3   = '3';
	protected static final int TOGGLE_LEGEND_4   = '4';
	protected static final int TOGGLE_LEGEND_5   = '5';
	protected static final int TOGGLE_LEGEND_6   = '6';
	protected static final int TOGGLE_LEGEND_7   = '7';
	protected static final int TOGGLE_LEGEND_8   = '8';
	protected static final int TOGGLE_LEGEND_9   = '9';

	/* Special keys */

	protected static final int DESCEND           = KeyEvent.VK_ENTER;
	protected static final int DELETE            = KeyEvent.VK_DELETE;
	protected static final int ESCAPE            = KeyEvent.VK_ESCAPE;

	protected static final int MOVE_GROUP_UP     = KeyEvent.VK_UP;
	protected static final int MOVE_GROUP_DOWN   = KeyEvent.VK_DOWN;
	protected static final int MOVE_GROUP_LEFT   = KeyEvent.VK_LEFT;
	protected static final int MOVE_GROUP_RIGHT  = KeyEvent.VK_RIGHT;
	
	protected static final int NAVIGATE_ROOT     = KeyEvent.VK_HOME;
	protected static final int PREV_HISTORY      = KeyEvent.VK_PAGE_DOWN;
	protected static final int NEXT_HISTORY      = KeyEvent.VK_PAGE_UP;


	protected static final int ABOUT_PROGRAM     = KeyEvent.VK_F1  + FUNCTION_KEY;	
	protected static final int FIND_PREV         = KeyEvent.VK_F2  + FUNCTION_KEY;
	protected static final int FIND_NEXT         = KeyEvent.VK_F3  + FUNCTION_KEY;

	protected static final int SHOW_SOURCECODE2  = KeyEvent.VK_F4  + FUNCTION_KEY;
	protected static final int SHOW_SOURCECODE3  = KeyEvent.VK_F5  + FUNCTION_KEY;
	protected static final int SHOW_SOURCECODE4  = KeyEvent.VK_F6  + FUNCTION_KEY;
	protected static final int SHOW_SOURCECODE5  = KeyEvent.VK_F7  + FUNCTION_KEY;
	protected static final int SHOW_SOURCECODE6  = KeyEvent.VK_F8  + FUNCTION_KEY;
	protected static final int SHOW_SOURCECODE7  = KeyEvent.VK_F9  + FUNCTION_KEY;
	protected static final int SHOW_SOURCECODE8  = KeyEvent.VK_F10 + FUNCTION_KEY;

	protected static final int ABOUT_URL         = KeyEvent.VK_F11 + FUNCTION_KEY;		
	protected static final int HELP_URL          = KeyEvent.VK_F12 + FUNCTION_KEY;

	private MyMenuItem          m_undo           = null;
	private MyMenuItem			m_redo           = null;

	protected LandscapeLayouter	m_fliphorizontally = null; 
	protected LandscapeLayouter	m_flipvertically   = null; 
	protected LandscapeLayouter m_layexpand        = new ExpandLayout((LandscapeEditorCore)      this, null);

	public    LandscapeLayouter m_laymatrix        = new MatrixLayout((LandscapeEditorCore)      this, null);
	protected LandscapeLayouter	m_laysugiyama      = new SugiyamaLayout((LandscapeEditorCore)    this, m_laymatrix);
	protected LandscapeLayouter	m_laysimplex       = new SimplexLayout((LandscapeEditorCore)     this, m_laysugiyama);
	protected LandscapeLayouter m_layspring        = new SpringLayout((LandscapeEditorCore)      this, m_laymatrix);
	protected LandscapeLayouter m_layspring2       = new SpringLayout2((LandscapeEditorCore)     this, m_layspring);

	protected LandscapeLayouter	m_springcluster	   = new ClusterLayout((LandscapeEditorCore)     this, null);	
	protected LandscapeLayouter m_aacluster        = new AAClusterLayout((LandscapeEditorCore)   this, m_springcluster);
	protected LandscapeLayouter m_acdccluster      = new ACDCClusterLayout((LandscapeEditorCore) this, m_springcluster);
	protected LandscapeLayouter m_bunchcluster     = new BunchClusterLayout((LandscapeEditorCore)this, m_springcluster);
	protected LandscapeLayouter m_clusterinterface = new ClusterInterface((LandscapeEditorCore)  this, m_springcluster);

	protected LandscapeLayouter[] m_layouters;

	protected LandscapeLayouter	m_layouter         = m_layspring2;

	public Do()
	{
		m_layouters    = new LandscapeLayouter[11];

		m_layouters[0] = m_laysimplex;
		m_layouters[1] = m_laysugiyama;
		m_layouters[2] = m_laymatrix;
		m_layouters[3] = m_layspring;
		m_layouters[4] = m_layspring2;
		m_layouters[5] = m_layexpand;
		m_layouters[6] = m_springcluster;
		m_layouters[7] = m_aacluster;
		m_layouters[8] = m_acdccluster;
		m_layouters[9] = m_bunchcluster;
		m_layouters[10]= m_clusterinterface;
	}

	public LandscapeLayouter getLayouter()
	{
		return m_layouter;
	}

	public void setLayouter(LandscapeLayouter layouter)
	{
		m_layouter = layouter;
	}

	public LandscapeLayouter[] getLayouters()
	{
		return m_layouters;
	}

	public void defaultToLayouter(int index)
	{
//		System.out.println("Defaulting to " + m_layouters[index].getName() + " layout");
		if (index >= 0 && index < m_layouters.length) {
			setLayouter(m_layouters[index]);
	}	}

	public void defaultToLayouter(String name)
	{
		if (name != null) {
			LandscapeLayouter	layouter;
			String				name1;
			int					i;

			if (name.equalsIgnoreCase("lisp")) {
				name = "simplex";
				SimplexLayout.lispSemantics();
			}
			for (i = m_layouters.length; i > 0; ) {
				layouter = m_layouters[--i];
				name1    = layouter.getName();
				if (name1.equalsIgnoreCase(name)) {
					defaultToLayouter(i);
					return;
	}	}	}	}

	public void setEnabledRedo(boolean value)
	{
		m_redo.setEnabled(true /* value */);
	}

	public void setEnabledUndo(boolean value)
	{
		m_undo.setEnabled(true /* value */);
	}

	private static void setMyCheckBoxMenuItemState(AbstractButton m, boolean value)
	{
		if (m != null) {
			m.setSelected(value);
	}	}

	public static void addSeparator(JComponent m)
	{
		if (m instanceof JMenu) {
			((JMenu) m).addSeparator();
		} else if (m instanceof JPopupMenu) {
			((JPopupMenu) m).addSeparator();
	}	}

	public static MyMenuItem navigateMenuItem(JComponent m, ToolBarEventHandler handler, EntityInstance e)
	{
		MyMenuItem	ret = new MyMenuItem(m, "Go to entity",  handler, Event.ALT_MASK | Event.SHIFT_MASK, NAVIGATE_TO, "Navigate to entity " + e);
		ret.setObject(e);
		return(ret);
	}

	public static MyMenuItem cutMenuItem(JComponent m, ToolBarEventHandler handler)
	{
		MyMenuItem	ret = new MyMenuItem(m, "Cut group",		  handler, Event.CTRL_MASK, CUT_GROUP, "Removes selected entities from the landscapes and places them on the clipboard");
		return(ret);
	}

	public static void createClassMenuItem(JComponent m, ToolBarEventHandler handler)
	{
		new MyMenuItem(m, "Create Entity Class",		  handler, Event.ALT_MASK, NEW_ECLASS, "Create a new entity class");
		new MyMenuItem(m, "Create Relation Class",		  handler, Event.ALT_MASK|Event.SHIFT_MASK, NEW_RCLASS, "Create a new relation class");
		new MyMenuItem(m, "Validate all",				  handler, Event.ALT_MASK|Event.SHIFT_MASK, VALIDATE_ALL, "Check the diagram against its scheme");
	}

	public static void groupAllMenuItem(JComponent m, ToolBarEventHandler handler)
	{
		new MyMenuItem(m, "Select all children", handler,        Event.CTRL_MASK, GROUP_ALL, "Selects all entities currently displayed");
	}

	public static void scaleMenuItem(JComponent m, ToolBarEventHandler handler)
	{
		new MyMenuItem(m, "Grow", handler,                    Event.SHIFT_MASK, INCREASE_SIZE,    "Increases the size of selected entities by 20%");
		new MyMenuItem(m, "Shrink", handler,                                  0, DECREASE_SIZE,   "Decreases the size of selected entities by 20%");
		new MyMenuItem(m, "Increase width", handler,           Event.SHIFT_MASK, INCREASE_WIDTH,  "Increases the width of selected entities by 20%");
		new MyMenuItem(m, "Decrease width", handler,                         0, DECREASE_WIDTH,   "Decreases the width of selected entities by 20%");
		new MyMenuItem(m, "Increase height", handler,           Event.SHIFT_MASK, INCREASE_HEIGHT,"Increases height of selected entities by 20%");
		new MyMenuItem(m, "Decrease height", handler,                         0, DECREASE_HEIGHT, "Decreases height of selected entities by 20%");

		new MyMenuItem(m, "Magnify item", handler, Event.ALT_MASK|    Event.SHIFT_MASK, INCREASE_MAG, "Zooms in on the selected entity");
		new MyMenuItem(m, "Reduce  item", handler,                     Event.ALT_MASK,   DECREASE_MAG, "Zooms out from the selected entity");
	}

	public static void setToViewportMenuItem(JComponent m, ToolBarEventHandler handler)
	{
		new MyMenuItem(m, "Scale landscape to viewport", handler, Event.CTRL_MASK, SET_TO_VIEWPORT, "Zooms out until no scrollbars are necessary");
	}

	public static void groupRedBoxMenuItem(JComponent m, ToolBarEventHandler handler)
	{
		new MyMenuItem(m, "Group red boxes",		handler,       0,				 GROUP_REDBOXES,     "Set highlighted (dark red) entityes as group");
		new MyMenuItem(m, "Add red boxes to group", handler,       Event.SHIFT_MASK, ADD_GROUP_REDBOXES, "Add highlighted (dark red) entities to group");
		new MyMenuItem(m, "Group unconnected",		handler,		0,				 GROUP_UNCONNECTED,  "Group unconnected entities");
	
	}

	public static void navigateToDstMenuItem(JComponent m, ToolBarEventHandler handler)
	{
		new MyMenuItem(m, "Navigate to destination", handler, Event.ALT_MASK, EDGE_NAVIGATE_DST, "Go to the destination of the selected edge");	// Make the destination the new root
	}

	public static void navigateToSrcMenuItem(JComponent m, ToolBarEventHandler handler)
	{
		new MyMenuItem(m, "Navigate to source",      handler, Event.ALT_MASK, EDGE_NAVIGATE_SRC, "Go to source of the selected edge");	// Make the source the new root
	}

	public static void navigateEdgeMenu(JComponent m, ToolBarEventHandler handler)
	{
		new MyMenuItem(m, "Open to lowest level",    handler, Event.ALT_MASK, EDGE_OPEN_LOW, "Opens source and destination entities of selected edge");		// Open all edges to real src,dst
		new MyMenuItem(m, "Open edge destination",   handler, Event.ALT_MASK, EDGE_OPEN_DST, "Opens destination entity of selected edge");		// Open all edges to real dst
		new MyMenuItem(m, "Open edge source",        handler, Event.ALT_MASK, EDGE_OPEN_SRC, "Opens source entity of selected edge");		// Open all edges to real src
		new MyMenuItem(m, "Close to top level",      handler, Event.ALT_MASK, EDGE_CLOSE_LOW,"Closes source and destionation entities of selected edge");		// Close all edges to real src,dst
		new MyMenuItem(m, "Close edge destination",  handler, Event.ALT_MASK, EDGE_CLOSE_DST,"Closes destination entity of selected edge");		// Close all edges to real dst
		new MyMenuItem(m, "Close edge source",       handler, Event.ALT_MASK, EDGE_CLOSE_SRC,"Closes source entity of selected edge");		// Close all edges to real src
	}

	public static void showSourceMenu(JComponent m, ToolBarEventHandler handler, boolean isApplet)
	{
		if (!isApplet) {
			JMenu				m1;

			m1 = new JMenu("External command...");
			new MyMenuItem(m1, "Command 1",      handler, 0, SHOW_SOURCECODE,  "Performs the action associated with external command #1");
			new MyMenuItem(m1, "Command 2",      handler, 0, SHOW_SOURCECODE2, "Performs the action associated with external command #2");
			new MyMenuItem(m1, "Command 3",      handler, 0, SHOW_SOURCECODE3, "Performs the action associated with external command #3");
			new MyMenuItem(m1, "Command 4",      handler, 0, SHOW_SOURCECODE4, "Performs the action associated with external command #4");
			new MyMenuItem(m1, "Command 5",      handler, 0, SHOW_SOURCECODE5, "Performs the action associated with external command #5");
			new MyMenuItem(m1, "Command 6",      handler, 0, SHOW_SOURCECODE6, "Performs the action associated with external command #6");
			new MyMenuItem(m1, "Command 7",      handler, 0, SHOW_SOURCECODE7, "Performs the action associated with external command #7");
			new MyMenuItem(m1, "Command 8",      handler, 0, SHOW_SOURCECODE8, "Performs the action associated with external command #8");
	
			m.add(m1);
		}
	}

	public static final String g_prev_text = "Previous";
	public static final String g_next_text = "Next";

	public static void navigateEntityMenu(JComponent m, ToolBarEventHandler handler, boolean isApplet)
	{
		new MyMenuItem(m, "Descend",                 handler, 0,                DESCEND, "Goes to the selected entity");
		new MyMenuItem(m, "Ascend",                  handler, Event.SHIFT_MASK, ASCEND,  "Goes one level up");
		new MyMenuItem(m, g_prev_text,               handler, 0,   PREV_HISTORY, "Goes back in navigation history");
		new MyMenuItem(m, g_next_text,               handler, 0, NEXT_HISTORY, "Goes forward in navigation history");
		new MyMenuItem(m, "Root",                    handler, 0, NAVIGATE_ROOT, "Goes to root of graph");
		showSourceMenu(m, handler, isApplet);
	}

	public static MyMenuItem pasteMenuItem(JComponent m, ToolBarEventHandler handler)
	{
		MyMenuItem	ret = new MyMenuItem(m, "Paste group", handler, Event.CTRL_MASK, PASTE, "Inserts the contents of the clipboard to the landscape, emptying the clipboard");
		return(ret);
	}

	public static void openCloseTOCMenuItem(JComponent m, ToolBarEventHandler handler)
	{
		new MyMenuItem(m, "Open/close TOC", handler,          Event.CTRL_MASK,SWITCH_TOC, "Completely expands or collapses the Table of Contents");
	}

	public static void alignTOCMenuItem(JComponent m, ToolBarEventHandler handler)
	{
		new MyMenuItem(m, "Sync TOC with diagram",  handler,          Event.ALT_MASK, TOC_PATH, "Opens Table of Contents to the entity currently displayed"
);
	}

	public static void editAttributesMenuItem(JComponent m, ToolBarEventHandler handler, Object object)
	{	MyMenuItem m1;
	
		m1 = new MyMenuItem(m, "Edit attributes",       handler, 0,                EDIT_ATTRIBUTES,   "Modifies attributes of selected entity or edge");
		m1.setObject(object);
		m1 = new MyMenuItem(m, "Edit class attributes", handler, Event.SHIFT_MASK, EDIT_ENTITY_CLASS, "Modifies attributes of the class of selected entity or edge");
		m1.setObject(object);
	}

	public static void editElisionsMenuItem(JComponent m, ToolBarEventHandler handler, Object object)
	{	MyMenuItem m1;
			
		m1 = new MyMenuItem(m, ERBox.g_editElisionRules_text, handler, Event.ALT_MASK,   EDIT_ELISIONS,     "Edit the elisions associated with selected entity of edge");
		m1.setObject(object);
	}
	
	public static final String g_forward_query_text = "Forward query";

	public static final String g_backward_query_text = "Backward query";
	public static final String g_contents_query_text = "Contents query";

	public static void queryMenuItem(JComponent m, ToolBarEventHandler handler)
	{
		new MyMenuItem(m, g_forward_query_text,  handler, 0,                FORWARD_QUERY,   "Highlights entities to which edges from selected box go");
		new MyMenuItem(m, "Forward closure",     handler, Event.SHIFT_MASK, FORWARD_CLOSURE, "Highlights entities reachable from selected box");
		new MyMenuItem(m, g_backward_query_text, handler, 0,                BACKWARD_QUERY,  "Highlights entities with edges going to selected box");
		new MyMenuItem(m, "Backward closure",    handler, Event.SHIFT_MASK, BACKWARD_CLOSURE,"Highlights entities from which selected box can be reached");
		new MyMenuItem(m, g_contents_query_text, handler, 0,                CONTENTS_QUERY,  "Lists all children of selected entity");
		new MyMenuItem(m, "Content closure",     handler, Event.SHIFT_MASK, CONTENT_CLOSURE, "Lists all descendants of selected entity");
		new MyMenuItem(m, "Root cause analysis", handler, Event.SHIFT_MASK, ROOT_CAUSE,      "Lists all enties at/forward from *ALL* selected entities");
		new MyMenuItem(m, "Check refcnts",       handler, Event.ALT_MASK,   CHECK_REFCNTS,   "Check the attribute values for the attribute refcnt");
	}

	public static final String g_exiting_edges_text  = "Hide/show edges from inside";
	public static final String g_entering_edges_text = "Hide/show edges to inside";
	public static final String g_internal_edges_text = "Hide/show internal edges";
	public static final String g_show_contents_text  = "Hide/show contents";
	public static final String g_src_edges_text      = "Hide/show source edges";
	public static final String g_dst_edges_text      = "Hide/show destination edges";

	public static void hideMenuItem(JComponent m, ToolBarEventHandler handler)
	{
		new MyMenuItem(m, g_dst_edges_text,				 handler, 0,                DST_EDGES,     "Toggles display of edges going to selected entity");
		new MyMenuItem(m, g_entering_edges_text,         handler, Event.SHIFT_MASK, ENTERING_EDGES,"Toggles display of edges going into selected entity");
		new MyMenuItem(m, g_src_edges_text,				 handler, 0,                SRC_EDGES,     "Toggles display of edges going from selected entity");
		new MyMenuItem(m, g_exiting_edges_text,			 handler, Event.SHIFT_MASK, EXITING_EDGES, "Toggles display of edges going from inside of selected entity");
		new MyMenuItem(m, g_internal_edges_text,		 handler, Event.SHIFT_MASK, INTERNAL_EDGES,"Toggles display of edges going between children of selected entity");
		new MyMenuItem(m, g_show_contents_text,          handler, 0,                SHOW_CONTENTS, "Opens or closes selected entity");
		new MyMenuItem(m, "Show all edges",			     handler, Event.CTRL_MASK,  CLEAR_ELISIONS,"Clear all edge elisions for selected entity(s)");
		new MyMenuItem(m, "Toggle descendants",		     handler, 0,                TOGGLE_DESCENDANTS,"Recursively open/close up all descendants for selected entity(s)");
	}

	public static final String g_decrease_font_size_text = "Decrease font size";
	public static final String g_increase_font_size_text = "Increase font size";
	public static final String g_redbox_entity_text      = "Redbox entity";

	public static void fontMenuItem(JComponent m, ToolBarEventHandler handler)
	{
		new MyMenuItem(m, g_decrease_font_size_text, handler,                 0, DECREASE_LABEL_FONT, "Decreases font size of selected entity's labels");
		new MyMenuItem(m, g_increase_font_size_text, handler,  Event.SHIFT_MASK, INCREASE_LABEL_FONT, "Increases font size of selected entity's labels");
		new MyMenuItem(m, g_redbox_entity_text,      handler,    Event.ALT_MASK, REDBOX_ENTITY,       "Highlight this entity as a redbox");
	}

	public static void activeMenuItem(JComponent m, ToolBarEventHandler handler)
	{
		new MyMenuItem(m, "Toggle query  relations", handler,           Event.SHIFT_MASK, TOGGLE_RELATION_ALL, "Reverse relations considered in queries");
		new MyMenuItem(m, "Toggle query  relation 1",    handler,           Event.SHIFT_MASK, TOGGLE_RELATION_1, null);
		new MyMenuItem(m, "Toggle query  relation 2",    handler,           Event.SHIFT_MASK, TOGGLE_RELATION_2, null);
		new MyMenuItem(m, "Toggle query  relation 3",    handler,           Event.SHIFT_MASK, TOGGLE_RELATION_3, null);
		new MyMenuItem(m, "Toggle query  relation 4",    handler,           Event.SHIFT_MASK, TOGGLE_RELATION_4, null);
		new MyMenuItem(m, "Toggle query  relation 5",    handler,           Event.SHIFT_MASK, TOGGLE_RELATION_5, null);
		new MyMenuItem(m, "Toggle query  relation 6",    handler,           Event.SHIFT_MASK, TOGGLE_RELATION_6, null);
		new MyMenuItem(m, "Toggle query  relation 7",    handler,           Event.SHIFT_MASK, TOGGLE_RELATION_7, null);
		new MyMenuItem(m, "Toggle query  relation 8",    handler,           Event.SHIFT_MASK, TOGGLE_RELATION_8, null);
		new MyMenuItem(m, "Toggle query  relation 9",    handler,           Event.SHIFT_MASK, TOGGLE_RELATION_9, null);
	}

	public static void visibleMenuItem(JComponent m, ToolBarEventHandler handler)
	{
		new MyMenuItem(m, "Toggle visible relations", handler,                              0, TOGGLE_LEGEND_ALL, "Reverse the relations which are visible");
		new MyMenuItem(m, "Toggle visible relation 1",    handler,                          0, TOGGLE_LEGEND_1, null);
		new MyMenuItem(m, "Toggle visible relation 2",    handler,                          0, TOGGLE_LEGEND_2, null);
		new MyMenuItem(m, "Toggle visible relation 3",    handler,                          0, TOGGLE_LEGEND_3, null);
		new MyMenuItem(m, "Toggle visible relation 4",    handler,                          0, TOGGLE_LEGEND_4, null);
		new MyMenuItem(m, "Toggle visible relation 5",    handler,                          0, TOGGLE_LEGEND_5, null);
		new MyMenuItem(m, "Toggle visible relation 6",    handler,                          0, TOGGLE_LEGEND_6, null);
		new MyMenuItem(m, "Toggle visible relation 7",    handler,                          0, TOGGLE_LEGEND_7, null);
		new MyMenuItem(m, "Toggle visible relation 8",    handler,                          0, TOGGLE_LEGEND_8, null);
		new MyMenuItem(m, "Toggle visible relation 9",    handler,                          0, TOGGLE_LEGEND_9, null);
	}

	public static void newEntityMenuItem(JComponent m, ToolBarEventHandler handler)
	{
		new MyMenuItem(m, "New entity", handler,                                    0, NEW_ENTITY, "Creates a new entity");
	}

	public static void newEdgeMenuItem(JComponent m, ToolBarEventHandler handler)
	{
		new MyMenuItem(m, "New edge",             handler,            Event.SHIFT_MASK, NEW_EDGE, "Creates a new edge between entities");
	}

	public static void deleteMenuItem(JComponent m, ToolBarEventHandler handler)
	{
		new MyMenuItem(m, "Delete", handler,		                             0, DELETE, "Deletes selected entities and edges");
	}

	public static void deleteContainerMenuItem(JComponent m, ToolBarEventHandler handler)
	{
		new MyMenuItem(m, "Delete container", handler,                           0, DELETE_CONTAINER, "Delete the selected entity but not its children");
	}

	public static void removeDeadMenuItem(JComponent m, ToolBarEventHandler handler)
	{
		new MyMenuItem(m, "Remove unreachable", handler,                           Event.ALT_MASK, REMOVE_DEAD, "Remove all unreachable entities from selected nodes");
	}

	public static void fileMenuItem(JComponent m, ToolBarEventHandler handler, boolean isApplet)
	{
		new MyMenuItem(m, "New  landscape",       handler, Event.CTRL_MASK, NEW_LANDSCAPE,  "Create a new landscape");

		new MyMenuItem(m, "Open landscape",       handler, Event.CTRL_MASK, OPEN_LANDSCAPE, "Open an existing landscape");
		new MyMenuItem(m, "Open user settings",   handler, Event.CTRL_MASK, OPEN_SETTINGS,  "Open landscape settings");
		new MyMenuItem(m, "Refresh",              handler, Event.CTRL_MASK, REFRESH,        "Refresh current view");
		if (!isApplet) {
			new MyMenuItem(m, "Save landscape",   handler, Event.CTRL_MASK,                    SAVE,            "Saves active landscape");
			new MyMenuItem(m, "Save landscape as",handler, Event.CTRL_MASK | Event.SHIFT_MASK, SAVE_AS,         "Saves active landscape with a different file name");
			new MyMenuItem(m, "Print",            handler, Event.CTRL_MASK,                    PRINT_LANDSCAPE, "Prints active landscape" );
		}
		new MyMenuItem(m, "Close landscape",	  handler, Event.CTRL_MASK,                    CLOSE_LANDSCAPE, "Close landscape switching to another if any loaded else exit");
		new MyMenuItem(m, "Quit",    		      handler, Event.CTRL_MASK,                    QUIT_PROGRAM,    "Closes LSEdit after prompting you to save any modified landscapes");
	}

	public static final String g_clear_text = "Clear query/selection";

	public void editMenuItem(JComponent m, ToolBarEventHandler handler)
	{
		m_undo = new MyMenuItem(m, "Undo", handler, Event.CTRL_MASK, UNDO, "Undoes the last action");
		m_redo = new MyMenuItem(m, "Redo", handler, Event.CTRL_MASK, REDO, "Redes the last undone action");
//		setEnabledRedo(false);
//		setEnabledUndo(false);

		addSeparator(m);

		newEntityMenuItem(m, handler);
		newEdgeMenuItem(m, handler);
		createClassMenuItem(m, handler);

		addSeparator(m);

		editAttributesMenuItem(m, handler, null);
		editElisionsMenuItem(m, handler, null);

		addSeparator(m);

		cutMenuItem(m, handler);
		pasteMenuItem(m, handler);
		deleteMenuItem(m, handler);
		deleteContainerMenuItem(m, handler);
		removeDeadMenuItem(m, handler);

		addSeparator(m);

		groupAllMenuItem(m, handler);
		groupRedBoxMenuItem(m, handler);

		new MyMenuItem(m, g_clear_text, handler, 0, ESCAPE, "Clears currently active selections and queries");
	}

	public void layoutMenuItem(JComponent m, LandscapeEditorCore handler, boolean isApplet)
	{
		JMenu				m1;
		LandscapeLayouter	layout;

		m1 = new JMenu("Align...");
		new MyMenuItem(m1, "Align (top/horiz.)",      handler, Event.ALT_MASK, A_HORIZ_TOP, "Aligns selected entities horizontally on their top borders");
		new MyMenuItem(m1, "Align (center/horiz.)",   handler, Event.ALT_MASK|Event.SHIFT_MASK, A_HORIZ_CENTER, "Aligns selected entities horizontally on their centerlines");
		new MyMenuItem(m1, "Align (bottom/horiz.)",   handler, Event.ALT_MASK, A_HORIZ_BOTTOM, "Aligns selected entities horizontally on their bottom borders");
		new MyMenuItem(m1, "Align (left/vertical)",   handler, Event.ALT_MASK, A_VERTICAL_LEFT, "Aligns selected entities vertically on their left borders");
		new MyMenuItem(m1, "Align (center/vertical)", handler, Event.ALT_MASK|Event.SHIFT_MASK, A_VERTICAL_CENTER, "Aligns selected entities vertically on their centerlines");
		new MyMenuItem(m1, "Align (right/vertical)",  handler, Event.ALT_MASK, A_VERTICAL_RIGHT, "Aligns selected entities vertically on their right borders");

		m.add(m1);
		
		m1 = new JMenu("Distribute");
		layout = m_fliphorizontally;
		if (layout == null) {
			m_fliphorizontally = layout = new FlipLayoutHorizontally(handler, null);
		}
		new MyMenuItem(m1, layout.getMenuLabel(), layout, -1, 0, "Mirrors the locations of all entities horizontally");

		layout = m_flipvertically;
		if (layout == null) {
			m_flipvertically = layout = new FlipLayoutVertically(handler, null);
		}
		new MyMenuItem(m1, layout.getMenuLabel(), layout, -1, 0, "Mirrors the locations of all entities vertically");
		new MyMenuItem(m1, "Distribute horizontally",  handler, Event.ALT_MASK|Event.SHIFT_MASK, SPC_HORIZ, "Spaces selected entities out on the horizontal plane");
		new MyMenuItem(m1, "Distribute vertically",handler, Event.ALT_MASK|Event.SHIFT_MASK, SPC_VERTICAL, "Spaces selected entities out on the vertical plane");
		m.add(m1);

		m1 = new JMenu("Resize");
		new MyMenuItem(m1, "Same size",				 handler, Event.ALT_MASK, SZ_WIDTH_HEIGHT, "Makes all selected entities of the same size");
		new MyMenuItem(m1, "Same width",              handler, Event.ALT_MASK, SZ_WIDTH,       "Makes all selected entities of the same width");
		new MyMenuItem(m1, "Same height",             handler, Event.ALT_MASK, SZ_HEIGHT,      "Makes all selected entities of the same height");

		addSeparator(m1);

		scaleMenuItem(m1, handler);

		addSeparator(m1);

		fontMenuItem(m1, handler);
		new MyMenuItem(m1, "Fit to label", handler, Event.ALT_MASK, A_FIT_LABEL, "Changes the size of selected entity so that its label is visible");
		new MyMenuItem(m1, "Remove collisions", handler, Event.SHIFT_MASK, AVOID_COLLISIONS, "Attempt to distribute horizontally to avoid collisions");

		m.add(m1);

		m1 = new JMenu("Nudge");
		new MyMenuItem(m1, "Nudge group up",    handler,  0, MOVE_GROUP_UP,   "Moves selection up by one grid division");
		new MyMenuItem(m1, "Nudge group down",  handler,  0, MOVE_GROUP_DOWN, "Moves selection down by one grid division");
		new MyMenuItem(m1, "Nudge group left",  handler,  0, MOVE_GROUP_LEFT, "Moves selection left by one grid division");
		new MyMenuItem(m1, "Nudge group right", handler,  0, MOVE_GROUP_RIGHT,"Move selection right by one grid division");

		m.add(m1);

		m1 = new JMenu("Layout");

		layout = m_laysugiyama;
		new MyMenuItem(m1, layout.getMenuLabel(), layout, Event.SHIFT_MASK, SUGIYAMA_LAYOUT, "Applies Sugiyama graph graph layout algorithm to selected entities");

		layout = m_laysimplex;
		new MyMenuItem(m1, layout.getMenuLabel(), layout, 0, SIMPLEX_LAYOUT, "Applies Network Simplex graph layout algorithm to selected entities");

		layout = m_laymatrix;
		new MyMenuItem(m1, layout.getMenuLabel(), layout, 0, MATRIX_LAYOUT, "Applies matrix graph layout algorithm to selected entities");

		layout = m_layspring;
		new MyMenuItem(m1, layout.getMenuLabel(), layout, 0, SPRING_LAYOUT,  "Layout using old springs algorithm");

		layout = m_layspring2;
		new MyMenuItem(m1, layout.getMenuLabel(), layout, 0, SPRING_LAYOUT2, "Layout using springs");

		layout = m_layexpand;
		new MyMenuItem(m1, layout.getMenuLabel(), layout, 0, EXPAND_LAYOUT, "Moves selected entities so that they fill the frame");

		m.add(m1);

		m1 = new JMenu("Cluster");

		layout = m_springcluster;
		new MyMenuItem(m1, layout.getMenuLabel(), layout, 0, CLUSTER_LAYOUT, "Cluster using springs layout");

		layout = m_aacluster;
		new MyMenuItem(m1, layout.getMenuLabel(), layout, 0, AACLUSTER_LAYOUT, "Cluster using AA");

		layout = m_acdccluster;
		new MyMenuItem(m1, layout.getMenuLabel(), layout, Event.SHIFT_MASK, ACDCCLUSTER_LAYOUT, "Cluster using ACDC");

		layout = m_bunchcluster;
		new MyMenuItem(m1, layout.getMenuLabel(), layout, Event.SHIFT_MASK, BUNCHCLUSTER_LAYOUT, "Cluster using Bunch");

		if (!isApplet) {
			layout = m_clusterinterface;
			new MyMenuItem(m1, layout.getMenuLabel(), layout, 0, CLUSTER_INTERFACE, "Invoke external clustering tool");
		}

		new MyMenuItem(m1, "Redistribute", handler, Event.SHIFT_MASK, REDISTRIBUTE, "Recluster nodes");

		new MyMenuItem(m1, "Cluster metrics", handler, Event.SHIFT_MASK, CLUSTER_METRICS, "Report cluster metrics");
		m.add(m1);

		new MyMenuItem(m, "Reconfigure layouters", handler, Event.ALT_MASK, RELAYOUT_ALL, "Modifies settings of graph layout algorithms and optionally relayout");

		setToViewportMenuItem(m, handler);

		new MyMenuItem(m, "Put group in container",  handler, Event.ALT_MASK, A_GROUP, "Creates a new entity and puts all selected entities into it");
	}

	public static final String g_find_query_text = "Find entities";
	public static final String g_find_next_text  = "Find next";
	public static final String g_find_prev_text  = "Find prev";
	public static final String g_nav_next_text   = "Nav next";
	public static final String g_nav_prev_text   = "Nav prev";

	public static void exploreMenuItem(JComponent m, ToolBarEventHandler handler, boolean isApplet)
	{
		JMenu m1;

		m1 = new JMenu("Find");
		new MyMenuItem(m1, g_find_query_text, handler, Event.CTRL_MASK, FIND_QUERY, "Searches for an entity by name");
		new MyMenuItem(m1, g_find_prev_text,  handler, 0, FIND_PREV, "Goes to previous set of entries in the search results");
		new MyMenuItem(m1, g_find_next_text,  handler, 0, FIND_NEXT, "Goes to next set of entries in the search results");
		new MyMenuItem(m1, g_nav_prev_text,  handler, Event.CTRL_MASK, NAV_PREV, "Previous landscape");
		new MyMenuItem(m1, g_nav_next_text,  handler, Event.CTRL_MASK, NAV_NEXT, "Next landscape");
		m.add(m1);

		m1 = new JMenu("Navigate");
		navigateToDstMenuItem(m1, handler);
		navigateToSrcMenuItem(m1, handler);
		navigateEntityMenu(m1, handler, isApplet);
		m.add(m1);

		m1 = new JMenu("Edge");
		navigateEdgeMenu(m1, handler);
		m.add(m1);

		m1 = new JMenu("Query");
		queryMenuItem(m1, handler);
		m.add(m1);

		m1 = new JMenu("Elide");
		hideMenuItem(m1, handler);
		m.add(m1);

		m1 = new JMenu("Visible relations");
		visibleMenuItem(m1, handler);
		m.add(m1);
		m1 = new JMenu("Query relations");
		activeMenuItem(m1, handler);
		m.add(m1);

		new MyMenuItem(m, "Open all", handler, Event.ALT_MASK|Event.SHIFT_MASK, Do.OPEN_ALL, "Opens all entities");
		new MyMenuItem(m, "Close all", handler, Event.ALT_MASK, Do.CLOSE_ALL, "Closes all entities");

		openCloseTOCMenuItem(m, handler);
		alignTOCMenuItem(m, handler);
	}

	/* N.B. All menu check items must have different key code */

	public void optionsMenuItem(JComponent m, ToolBarEventHandler handler)
	{
		new MyMenuItem(m, "Select browser...",       handler, Event.CTRL_MASK, SELECT_BROWSER, "Specify the default web browser that LSEdit should use");
		new MyMenuItem(m, "Configure fonts...",      handler,  Event.ALT_MASK,   SET_FONT, "Adjust the fonts used in LSEdit");
		new MyMenuItem(m, "Specify command...",      handler, Event.CTRL_MASK, SELECT_COMMAND, "Specify the external command to execute on objects");
		new MyMenuItem(m, "Options...",				 handler, 0, SHOW_OPTIONS, "Set desired landscape options"); 
	}

	public static void helpMenuItem(JComponent m, ToolBarEventHandler handler, String type)
	{
		new MyMenuItem(m, "About Landscape" + type, handler, 0, ABOUT_PROGRAM, "Show information about LSedit");
		new MyMenuItem(m, "About SWAG",  handler, 0, ABOUT_URL, "Show the about URL");
		new MyMenuItem(m, "About LSEdit",  handler, 0, HELP_URL,  "Show the help URL");
	}

	public void entityPopupMenuItem(JPopupMenu m, ToolBarEventHandler handler, boolean isApplet)
	{
		JMenu m1;

		m1 = new JMenu("Navigate");
		navigateEntityMenu(m1, handler, isApplet);
		m.add(m1);
		m1 = new JMenu("Query");
		queryMenuItem(m1, handler);
		m.add(m1);

		m1 = new JMenu("Elide");
		hideMenuItem(m1, handler);
		m.add(m1);

		m1 = new JMenu("Draw");
		newEntityMenuItem(m1, handler);
		newEdgeMenuItem(m1, handler);
		deleteMenuItem(m1, handler);
		deleteContainerMenuItem(m1, handler);
		removeDeadMenuItem(m1, handler);

		m1.addSeparator();
		editAttributesMenuItem(m1, handler, null);
		editElisionsMenuItem(m1, handler, null);
		m1.addSeparator();
		fontMenuItem(m1, handler);
		m.add(m1);

		m1 = new JMenu("Move");
		groupAllMenuItem(m1, handler);
		m.addSeparator();
		scaleMenuItem(m1, handler);
		setToViewportMenuItem(m1, handler);
		m.add(m1);

//		dump_menu(m);
	}

	public static void edgePopupMenuItem(JPopupMenu m, ToolBarEventHandler handler, boolean isApplet)
	{
		navigateEdgeMenu(m, handler);
		navigateToDstMenuItem(m, handler);
		navigateToSrcMenuItem(m, handler);
		showSourceMenu(m, handler, isApplet);
		new MyMenuItem(m, "Delete edge", handler,         0, DELETE, "Delete the selected edge");
		editAttributesMenuItem(m, handler, null);

//		dump_menu(m);
	}

/*
	// Used to rapidly generate html documenting the menu

	public static void dump_menu(JMenuItem m, int level)
	{
		int			i, cnt;
		String		name;
		JMenuItem	item;

		for (i = 0; i < level; ++i) {
			System.out.print("  ");
		}

		if (m instanceof JMenu) {
			cnt  = ((JMenu) m).getItemCount();
		} else {
			cnt  = 0;
		}
		name = m.getText();

		if (cnt == 0) {
			System.out.print("<li><a href=\"#");
			System.out.print(Util.encodedURLname(name));
			System.out.println("\">" + name + "</a></li>");
		} else {
			System.out.println("<li>" + name + "</li>");
			for (i = 0; i < level; ++i) {
				System.out.print("  ");
			}
			System.out.println("<ul>");
			for (i = 0; i < cnt; ++i) {
				item = ((JMenu) m).getItem(i);
				if (item != null) {
					dump_menu(item, level+1);
			}	}
			for (i = 0; i < level; ++i) {
				System.out.print("  ");
			}
			System.out.println("</ul>");
	}	}

	public static void dump_menu(JMenuItem m, int level)
	{
		int			i, cnt;
		String		name, keys;
		JMenuItem	item;
		KeyStroke	keystroke;	

		if (m instanceof JMenu) {
			cnt  = ((JMenu) m).getItemCount();
		} else {
			cnt  = 0;
		}
		name = m.getText();

		if (cnt == 0) {
			System.out.print("<a name=\"");
			System.out.print(Util.encodedURLname(name));
			System.out.println("\"></a>");

			System.out.print("<h3>" + name);
			keystroke = m.getAccelerator();
			if (keystroke != null) {
				keys = keystroke.toString();
				keys = keys.replaceAll("pressed ","");
				System.out.print(" (" + keys + ")");
			}
			System.out.println("</h3>");
			System.out.println("<p>");
			System.out.println("<p>");
			System.out.println("");


		} else {
			for (i = 0; i < cnt; ++i) {
				item = ((JMenu) m).getItem(i);
				if (item != null) {
					dump_menu(item, level+1);
			}	}
	}	}


	public static void dump_menu(JPopupMenu m)
	{
		Component m1;
		int			i;

		for (i = 0; i < m.getComponentCount(); ++i) {
			m1 = m.getComponent(i);
			if (m1 instanceof JMenuItem) {
				dump_menu((JMenuItem) m1, 0);
	}	}	}

	public static void dump_menubar(JMenuBar m)
	{
		JMenu		m1;
		int			i;

		for (i = 0; i < m.getMenuCount(); ++i) {
			m1 = m.getMenu(i);
			dump_menu(m1, 0);
	}	}
*/
}
