package lsedit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.Scrollable;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.ExpandVetoException;

import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;

/* In a large diagram the tree can be huge .. therefore we compute
 * the contents of tree only when needed and discard ASAP
 */

/* This class makes TreePaths more efficient by leveraging the fact that the underlying data model is naturally hierarchical */

class MyTreePath extends TreePath
{
	protected static MyTreePath parentPath(EntityInstance e)
	{
		EntityInstance parent = e.getContainedBy();
		if (parent != null) {
			return new MyTreePath(parent);
		}
		return null;
	}

	public MyTreePath(EntityInstance e)
	{
		super(parentPath(e), e);
	}

	public MyTreePath(MyTreePath path, EntityInstance e)
	{
		super(path, e);
	}

    public boolean equals(Object o) 
	{
		if (o instanceof MyTreePath) {
			if (getLastPathComponent() == ((MyTreePath) o).getLastPathComponent()) {
				return true;
		}	}
		return false;
    }

	// Returns true if aTreePath is a descendant of this TreePath. 

	public boolean isDescendant(TreePath aTreePath) 
	{
		EntityInstance e  = (EntityInstance) getLastPathComponent();
		EntityInstance e1 = (EntityInstance) aTreePath.getLastPathComponent();

		return e.hasDescendant(e1);
	}

    public TreePath pathByAddingChild(Object child) 
	{
		return new MyTreePath(this, (EntityInstance) child);
    }
}

class MyTreeCellRenderer extends DefaultTreeCellRenderer /* extends JLabel */
{
	Font	m_textFont;

	// -------------------------------
	// DefaultTreeCellRenderer methods
	// -------------------------------

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		super.setFont(m_textFont);

		if (value instanceof EntityInstance) {
			EntityClass				ec;
			EntityInstance			e;
			int						cnt;
			Icon					icon;

			e    = (EntityInstance) value;
			if (!e.isRoot()) {
				ec   = e.getEntityClass();
				if (leaf) {
					icon = ec.getLeafIcon();
				} else {
					if (expanded) {
						icon = ec.getOpenIcon();
					} else {
						icon = ec.getClosedIcon();
				}	} 
				setIcon(icon);
			}
		} 
		return(this);
	}

	public void setFont(Font font)
	{
		m_textFont = font;
		super.setFont(font);
	}
	
	public MyTreeCellRenderer(Font textFont)
	{
		setFont(textFont);
}	}

public final class TocBox extends JTree implements Scrollable, ChangeListener, TaListener, TreeModel, TreeExpansionListener, MouseListener, MouseMotionListener
{
	public    static final String m_helpStr	 = "Right click for menu.";
	
	protected MyTreeCellRenderer  m_renderer;
	protected static Font m_textFont  = null;

	protected EntityInstance	m_childrensParent;	// Parent of the following sorted list of children (null if must rebuild children)
	protected Vector			m_children;			// Sorted list of children (use as needed)

	protected LandscapeEditorCore m_ls;

	protected JTabbedPane		  m_tabbedPane;
	protected JScrollPane		  m_scrollPane;

	/* If hidden and first time actually tabbed to this set visible */


	protected Vector			  m_treeModelListeners;
	protected EntityInstance	  m_hover;

	// --------------
	// Object methods
	// --------------

	public String toString()
	{
		return("TocBox");
	}

	// ------------------
	// JComponent methods
	// ------------------

	// -------------
	// JTree methods
	// -------------

	// Need subclass since want to have special rendering

	public String convertValueToText(Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		String ret;

		if (value instanceof EntityInstance) {
			EntityInstance  e     = (EntityInstance) value;
			int				cnt   = e.numChildren();

			ret = e.getEntityLabel();
			if (cnt > 1) {
				ret += " (" + cnt + ")";
			}
		} else {
			ret = value.toString();
		}
		return ret;
	}
			 
	// Always return false so that collapsing marked nodes does not highlight new collapsed node

	protected boolean removeDescendantSelectedPaths(TreePath path, boolean includePath) 
	{
		super.removeDescendantSelectedPaths(path, includePath);
		return(false);
	}

	// --------------
	// Public methods 
	// --------------

	protected void setRowHeight()
	{
		FontMetrics	fm     = getFontMetrics(m_textFont);
		int			height = fm.getHeight();

		if (height < OpenIcon.g_height) {
			height = OpenIcon.g_height;
		}

		setRowHeight(height);
	}

	public TocBox(LandscapeEditorCore ls, JTabbedPane tabbedPane) 
	{
		super();
		
		Insets	insets    = tabbedPane.getInsets();
		int		width     = tabbedPane.getWidth()  - insets.left - insets.right;
		int		height    = tabbedPane.getHeight() - insets.top  - insets.bottom;
		Font	textFont = m_textFont;
		
		if (textFont == null) {
			m_textFont = textFont = Options.getTargetFont(Option.FONT_TOC);
		}
		
		m_ls          = ls;
		m_tabbedPane  = tabbedPane;
		m_children    = new Vector();

		MyTreeCellRenderer	renderer;

//		setLargeModel(true);
		setBackground(Diagram.boxColor);
		setFont(textFont);
		setEditable(false);
		setShowsRootHandles(true);
		m_renderer = renderer = new MyTreeCellRenderer(textFont);
		renderer.setBackgroundNonSelectionColor(Diagram.boxColor);
//		renderer.setBackgroundSelectionColor(Color.BLUE); 
		setCellRenderer(renderer);
		setDragEnabled(true);
		setToolTipText(m_helpStr);
		setRowHeight();

		m_scrollPane = new JScrollPane();
		m_scrollPane.setBounds(0,0,width, height);
		setSize(width, height);
		m_scrollPane.setViewportView(this);

		tabbedPane.addTab("TOC", null, m_scrollPane, m_helpStr);
		addMouseListener(this);

		tabbedPane.addChangeListener(this);
	}
	
	/* The JTree does not compute its preferred height correctly which screws up scrolling */

	public void treeSizeChanged()
	{
		Dimension	d      = getPreferredSize();
		int			height = (getRowCount() + 1) * getRowHeight();

		if (d.height != height) {
			d.height = height;
//			System.out.print("treeSizeChanged " + getPreferredSize());
			setPreferredSize(d);
//			System.out.println(" -> " + getPreferredSize());

			treeDidChange();
	}	}

	public void textFontChanged(Font font)
	{
		m_textFont = font;
		m_renderer.setFont(font);

		setFont(font);
		setRowHeight();
		fill();
	}	

 	public void activate() 
	{
		m_tabbedPane.setSelectedComponent(m_scrollPane);
	}

	public boolean isActive() 
	{
		Component active;

		if (!isVisible()) {
			return(false);
		}
		active = m_tabbedPane.getSelectedComponent();
		return(m_scrollPane == active);
	}

	protected JPopupMenu buildPopup(EntityInstance e)
	{
		LandscapeEditorCore ls = m_ls;
		JPopupMenu			m;
		MyMenuItem			m1;
		Diagram				diagram = ls.getDiagram();

		
		m = new JPopupMenu("TOC options");

		if (e != null) {
			Do.navigateMenuItem(m, ls, e);
			Do.editAttributesMenuItem(m, ls, e);
			Do.editElisionsMenuItem(m, ls, e);
		}

		if (diagram != null) {
			Clipboard	clipboard = diagram.getClipboard();
			// Get paths of all selected nodes
			TreePath[] paths = getSelectionPaths();
			if (paths != null && paths.length > 0) {
				m1 = Do.cutMenuItem(m, ls);
				m1.setObject(this);
			} 
			if (clipboard != null && !clipboard.isEmpty()) {
				if (paths != null && paths.length == 1) {
					m1 = Do.pasteMenuItem(m, ls);
					m1.setObject(this);
		}	}	}
		Do.openCloseTOCMenuItem(m, ls);
		Do.alignTOCMenuItem(m, ls);
		FontCache.setMenuTreeFont(m); 

//		Do.dump_menu(m);
		return(m);
	}

	// Identify the nodes to cut

	public Vector getTocGroupedEntities()
	{
		Vector	ret = null;

		EntityInstance	e;
		int				i, cnt;
		TreePath[]		paths = getSelectionPaths();

		if (paths != null && (cnt = paths.length) > 0) {
			ret = new Vector(cnt);
			for (i = 0; i < cnt; ++i) {
				e = (EntityInstance) paths[i].getLastPathComponent();
				ret.add(e);
		}	}
		return(ret);
	}

	// Identify the single selected node where we are to paste

	public EntityInstance targetEntity()
	{
		EntityInstance	e;
		TreePath[]		paths = getSelectionPaths();

		if (paths != null && paths.length == 1) {
			e = (EntityInstance) paths[0].getLastPathComponent();
			return(e);
		}	
		return(null);
	}

	public void fill() 
	{
		Diagram			diagram = m_ls.getDiagram();
		EntityInstance	root;

//		System.out.println("TocBox.fill() " + isActive());

		m_childrensParent = null;

		setModel(null);

		if (diagram == null || !isActive()) {
			return;
		}
		root = diagram.getRootInstance();
		if (root == null) {
			return;
		}

		removeTreeExpansionListener(this);

		m_treeModelListeners = null;

		setModel(this);

		addTreeExpansionListener(this);

		treeSizeChanged();
	}
		
	public MyTreePath getTreePath(EntityInstance e)
	{
		return new MyTreePath(e);
	}

	private void insertTOC(EntityInstance e, EntityInstance parent)
	{
		if (isActive()) {

			if (parent != null) {

//				System.out.println("insertTOC " + e + " " + parent);
				// Make sure we don't remember false information
				m_childrensParent = null;

				MyTreePath path = getTreePath(parent);

				if (path != null) {


					int[]				indices = {	getIndexOfChild(parent, e) };
					Object[]			objects = { e };
					TreeModelEvent		event   = new TreeModelEvent(this, path, indices, objects);
					TreeModelListener	listener;
					int					i;

					for (i = m_treeModelListeners.size(); i > 0; ) {
						listener = (TreeModelListener) m_treeModelListeners.elementAt(--i);
						listener.treeNodesInserted(event);
//						listener.treeStructureChanged(event);
	}	}	}	}	}

	private void deleteTOC(EntityInstance e, EntityInstance parent)
	{
		if (isActive()) {

			if (parent != null) {

//				System.out.println("deleteTOC " + e + " " + parent);
				// Make sure we don't remember false information
				m_childrensParent = null;

				MyTreePath path = getTreePath(parent);

				if (path != null) {
					int[]				indices = {	getIndexOfChild(parent, e) };
					Object[]			objects = { e };
					TreeModelEvent		event   = new TreeModelEvent(this, path, indices, objects);
					TreeModelListener	listener;
					int					i;

					for (i = m_treeModelListeners.size(); i > 0; ) {
						listener = (TreeModelListener) m_treeModelListeners.elementAt(--i);
						listener.treeNodesRemoved(event);
//						listener.treeStructureChanged(event);
	}	}	}	}	}

	protected void closeAll()
	{
		int			i, cnt;

		removeTreeExpansionListener(this);

		cnt = getRowCount();
		for (i = cnt; i > 0; ) {
			collapseRow(--i);
		}
		addTreeExpansionListener(this);

		treeSizeChanged();
	}
		
	protected void expandTo(EntityInstance e)
	{
		int				cnt;
		TreePath		path;
		EntityInstance	e1;

		removeTreeExpansionListener(this);

		closeAll();
		clearSelection();
		if (e != null) {
			for (cnt = 0; cnt < getRowCount(); ++cnt) {
				path     = getPathForRow(cnt);
				e1       = (EntityInstance) path.getLastPathComponent();
				if (e1 != null) {
					if (e1 == e) {
						setSelectionRow(cnt);
						scrollRowToVisible(cnt);
						// Also open this
						expandRow(cnt);
						break;
					}
					if (e1.hasDescendant(e)) {
						expandRow(cnt);
		}	}	}	} 
		addTreeExpansionListener(this);
		treeSizeChanged();
	}	
		
	protected void expandAll()
	{
		int	cnt;

		removeTreeExpansionListener(this);
		for (cnt = 0; cnt < getRowCount(); ++cnt) {
			expandRow(cnt);
		}
		addTreeExpansionListener(this);
		treeSizeChanged();
	}

	// Alt-P

	public void toc_path()
	{
		Diagram diagram = m_ls.getDiagram();
		EntityInstance e;

		if (diagram != null) {
			activate();
			e = diagram.getDrawRoot();
			expandTo(e);
	}	}

	// Open up or close down the Table of contents (Cntl-T)

	public void switch_TOC()
	{
		Diagram diagram = m_ls.getDiagram();

		if (diagram != null) {
			int	cnt;

			activate();
			cnt = getRowCount();
			if (cnt > 1) {
				closeAll();
			} else {
				expandAll();
	}	}	}	

	protected void doRightPopup(MouseEvent ev, EntityInstance e)
	{
		int				x, y;
		JPopupMenu		popupMenu;
				
		x         = ev.getX();
		y         = ev.getY();
		popupMenu = buildPopup(e);
		add(popupMenu);
		popupMenu.show(this, x, y);
		remove(popupMenu);
	}

	protected Vector sortedChildren(EntityInstance e)
	{
		Vector children = m_children;

		if (e != m_childrensParent) {

			// This cache avoids sorting a level on every request to it

			children.clear();
			e.addChildren(children);
			SortVector.byString(children, true);
			m_childrensParent = e;
		}
		return children;
	}

	// TreeModel interface

	// Adds a listener for the TreeModelEvent posted after the tree changes. 

	public void addTreeModelListener(TreeModelListener l) 
	{
		if (m_treeModelListeners == null) {
			m_treeModelListeners = new Vector();
		}
		m_treeModelListeners.add(l);
	}

	// Returns the child of parent at index index in the parent's child array. 

	public Object getChild(Object parent, int index)
	{
		EntityInstance	e        = (EntityInstance) parent;

//		System.out.println("getChild " + parent + " " + index);
		if (!Options.getDiagramOptions().isSortTOC()) {
			return e.getChild(index);
		}

		Vector			children = sortedChildren(e);
		
		return children.elementAt(index);
	}

	// Returns the number of children of parent. 

	public int getChildCount(Object parent) 
	{
		return ((EntityInstance) parent).numChildren();
	}

    // Returns the index of child in parent. 

	public int getIndexOfChild(Object parent, Object child) 
	{
		if (parent != null) {

			EntityInstance	e        = (EntityInstance) parent;

			if (!Options.getDiagramOptions().isSortTOC()) {
				return e.getIndexOfChild(child);
			}

			Vector			children = sortedChildren(e);
			int				index;
		
			for (index = children.size(); index > 0; ) {
				e = (EntityInstance) children.elementAt(--index);
				if (e == child) {
					return index;
		}	}	}
		return -1;
	}

	// Returns the root of the tree. 

	public Object getRoot() 
	{
		Diagram			diagram = m_ls.getDiagram();
		EntityInstance	root;

		if (diagram == null) {
			return null;
		}
		return diagram.getRootInstance();
	}

	// Returns true if node is a leaf. 

	public boolean isLeaf(Object node) 
	{
		return (getChildCount(node) == 0);
	}

	// Removes a listener previously added with addTreeModelListener. 

	public void removeTreeModelListener(TreeModelListener l) 
	{
		if (m_treeModelListeners != null) {
			m_treeModelListeners.remove(l);
			if (m_treeModelListeners.isEmpty()) {
				m_treeModelListeners = null;
	}	}	}

	// Messaged when the user has altered the value for the item identified by path to newValue. 

	public void valueForPathChanged(TreePath path, Object newValue)
	{
	} 

	private void containerCut(EntityInstance e, EntityInstance parent)
	{
		if (isActive()) {
			if (parent != null) {

//				System.out.println("containerCut " + parent + " " + e);

				m_childrensParent = null;
				MyTreePath path = getTreePath(parent);

//				System.out.println("path = " + path);

				if (path != null) {
					TreeModelEvent		event = new TreeModelEvent(this, path);
					TreeModelListener	listener;
					int					i;

					for (i = m_treeModelListeners.size(); i > 0; ) {
						listener = (TreeModelListener) m_treeModelListeners.elementAt(--i);
//						System.out.println(i + ") told " + listener);
						listener.treeStructureChanged(event);
	}	}	}	}	}

	private void containerUncut(EntityInstance e, EntityInstance parent)
	{
		containerCut(e, parent);
	}

	public EntityInstance entityClicked(MouseEvent ev)
	{
		int				x, y;
		TreePath		selPath;
		EntityInstance	e;
		Diagram			diagram;
				
		x       = ev.getX();
		y       = ev.getY();
		selPath = getPathForLocation(x, y);
		if(selPath == null) {
			e = null;
		} else {
			e = (EntityInstance) selPath.getLastPathComponent();
		}
		return e;
	}
	
/*
	private void testUpdate(String type, int signal)
	{
		System.out.println("TocBox.taSignal " + type + " (" + Diagram.taSignal(signal) + ")");
	}
*/
	// ChangeListener interface 

	public void stateChanged(ChangeEvent e) 
	{
//		System.out.println("TocBox stateChanged " + isActive());
		fill();
	}

	// TaListener interface

	public void diagramChanging(Diagram diagram)
	{
	}

	public void diagramChanged(Diagram diagram, int signal)
	{
		fill();
	}

	public void updateBegins()
	{
	}

	public void updateEnds()
	{
	}

	public void entityClassChanged(EntityClass ec, int signal)
	{
//		testUpdate(ec + " entityClassChanged", signal);

		switch (signal) {
		case TaListener.STYLE_SIGNAL:
		case TaListener.EC_ANGLE_SIGNAL:
		case TaListener.EC_IMAGE_SIGNAL:
		case TaListener.EC_ICON_SIGNAL:
		case TaListener.COLOR_SIGNAL:
		case TaListener.OPEN_COLOR_SIGNAL:
			fill();
			break;
		}
	}

	public void relationClassChanged(RelationClass rc, int signal)
	{
//		testUpdate(rc + " relationClassChanged", signal);

		switch (signal) {
		case TaListener.CONTAINS_CHANGED_SIGNAL:
			fill();
			break;
		}
	}

	public void entityParentChanged(EntityInstance e, EntityInstance parent, int signal)
	{
//		testUpdate(parent + "." + e + " entityParentChanged", signal);

		switch (signal) {
		case TaListener.ENTITY_CUTTING_SIGNAL:
		case TaListener.ENTITY_RELOCATING_SIGNAL:
			deleteTOC(e, parent);
			break;
		case TaListener.ENTITY_NEW_SIGNAL:
		case TaListener.ENTITY_PASTED_SIGNAL:
		case TaListener.ENTITY_RELOCATED_SIGNAL:
			insertTOC(e, parent);
			break;
		case TaListener.CONTAINER_CUT_SIGNAL:
			containerCut(e, parent);
			break;

		case TaListener.CONTAINER_PASTED_SIGNAL:
			containerUncut(e, parent);
			break;
		}
	}

	public void relationParentChanged(RelationInstance ri, int signal)
	{
	}

	public void entityInstanceChanged(EntityInstance e, int signal)
	{
	}

	public void relationInstanceChanged(RelationInstance ri, int signal)
	{
	}

	// TreeExpansionListener implementation

	public void treeCollapsed(TreeExpansionEvent event) 
	{
		treeSizeChanged();
	}

	public void treeExpanded(TreeExpansionEvent event) 
	{
		treeSizeChanged();
	}
 
	// Generic 	MouseListener interface

	public void mouseClicked(MouseEvent ev)
	{
		if (ev.isMetaDown()  && !ev.isAltDown()) {
			EntityInstance	e = entityClicked(ev);

			doRightPopup(ev, e);
	}	}

	public void mouseEntered(MouseEvent ev)
	{
	}

	public void mouseExited(MouseEvent e)
	{
	}

	public void mousePressed(MouseEvent ev)
	{
		if (ev.isAltDown()) {
			EntityInstance e = entityClicked(ev);
			m_hover = e;
			e.startHover();
		}	
	}

	public void mouseReleased(MouseEvent ev)
	{
		if (m_hover != null) {
			m_hover.endHover();
	}	}

	// MouseMotionListener interface

	public void mouseDragged(MouseEvent ev)
	{
	}

	public void mouseMoved(MouseEvent ev)
	{
	}
}





