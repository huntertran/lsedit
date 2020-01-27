package lsedit;

import java.util.Enumeration;
import java.util.Vector;

public class FlipLayoutHorizontally extends LandscapeLayouter implements ToolBarEventHandler {

	public FlipLayoutHorizontally(LandscapeEditorCore ls, LandscapeLayouter fallback) 
	{
		super(ls, fallback);
	}

	public String getName()
	{
		return "FlipHorizontal";
	}

	public String getMenuLabel() 
	{
		return "Flip layout horizontally";
	}

	public boolean doLayout1(Vector masterBoxes, EntityInstance parent)
	{
		Diagram			diagram = m_ls.getDiagram();
		Enumeration		en;
		EntityInstance	ce;

		for (en = parent.getChildren(); en.hasMoreElements(); ) {
			ce = (EntityInstance) en.nextElement();
			diagram.updateXRelLocal(ce, 1.0 - ce.xRelLocal() - ce.widthRelLocal());
		}
		return true;
	}
	
	public String doLayout(Diagram diagram) 
	{
		m_ls.doLayout1(this, null, diagram.getDrawRoot(), false);
		return "Layout flipped horizontally";
	}

	public void processKeyEvent(int key, int modifiers, Object object) 
	{
		Diagram				dg;
		String				rmsg;

		dg = m_ls.getDiagram();
		if (dg != null) {
			rmsg = doLayout(dg);
			m_ls.doFeedback(rmsg);
	}	}
}

