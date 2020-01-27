package lsedit;

import java.util.Enumeration;
import java.util.Vector;

import java.io.PrintWriter;

import java.awt.event.ActionEvent;

// Base class for layout classes accessed by Landscape Editor
//
// For a layouter to be available in the Landscape Editor, an instance entry
// must appear in the layouterList array in class LayouterManager.java

abstract public class LandscapeLayouter implements ToolBarEventHandler
{
	protected LandscapeEditorCore	m_ls;
	protected LandscapeLayouter		m_fallback;

	protected EntityInstance parentOfSet(Vector	set)
	{
		EntityInstance	e, parent, parent1;
		Enumeration		en;

		parent = null;
		for (en = set.elements(); en.hasMoreElements(); ) {
			e     = (EntityInstance) en.nextElement();
			parent1 = e.getContainedBy();
			if (parent == null) {
				parent = parent1;
			} else if (parent != parent1) {
				parent = null;
				break;
		}	}
		return(parent);
	}

	// --------------
	// Public methods
	// --------------

	public LandscapeLayouter(LandscapeEditorCore ls, LandscapeLayouter fallback) 
	{
		m_ls       = ls;
		m_fallback = fallback;
	}

	public LandscapeEditorCore getLs()
	{
		return m_ls;
	}
	
	public LandscapeLayouter getFallback()
	{
		return m_fallback;
	}

	public String undoLabel()
	{
		Diagram	diagram = m_ls.getDiagram();

		if (diagram == null) {
			return "No diagram";
		} else if (diagram.undoEnabled()) {
			return "Disable undo";
		}
		return "Enable undo";
	}

	public boolean ok()
	{
		return false;
	}

	public String allInDiagram(Vector selectedBoxes)
	{
		Diagram			diagram  = m_ls.getDiagram();
		int				i;
		EntityInstance	e;

		if (diagram == null) {
			Util.beep();
			return "Can't layout: No diagram";
		}

		for (i = selectedBoxes.size(); --i >= 0; ) {
			e = (EntityInstance) selectedBoxes.elementAt(i);
			if (!e.isMarked(EntityInstance.DIAGRAM_MARK)) {
				Util.beep();
				return "Attempting to layout things not in diagram";
		}	}
		return null;
	}

	// -----------------------
	// Abstract public methods
	// -----------------------

	// Called by system to obtain menu entry label for this layout routine

	abstract String getName();

	abstract String getMenuLabel();

	// This does the actual work of layouting
	// This needs to be subclassed if layout is used to automate layout for missing coordinates

	abstract boolean doLayout1(Vector selectedBoxes, EntityInstance parent);

	// Called by the system when a layout is requested
	// Needs to be supplied by layout classes which derive from this
	// base class.
	//
	// Returns a status/error message string.


	abstract public String doLayout(Diagram dg);

	public String getTag()
	{
		return "xxx:";
	}

	public void reset()
	{
	}
	
	public void loadLayoutOption(int mode, String attribute, String value)
	{
	}

	public void saveLayoutOptions(int mode, PrintWriter ps)
	{
	}

	public boolean configure(LandscapeEditorCore ls)
	{
		return false;
	}

	public boolean isConfigurable()
	{
		return false;
	}

	public boolean isLayouter()
	{
		return true;
	}

	// ToolBarEventHandler

	public boolean processMetaKeyEvent(String name)
	{
		return(m_ls.processMetaKeyEvent(name));
	}

	public void actionPerformed(ActionEvent ev)
	{
	}

	public void showInfo(String msg)
	{
	}
}

