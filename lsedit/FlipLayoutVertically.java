package lsedit;

import java.util.Enumeration;
import java.util.Vector;

public class FlipLayoutVertically extends LandscapeLayouter  implements ToolBarEventHandler 
{
	public FlipLayoutVertically(LandscapeEditorCore ls, LandscapeLayouter fallback) 
	{
		super(ls, fallback);
	}

	public String getName()
	{
		return "FlipVertical";
	}

	public String getMenuLabel() 
	{
		return "Flip layout vertically";
	}

	public boolean doLayout1(Vector selected, EntityInstance parent) 
	{
		Diagram			diagram = m_ls.getDiagram();
		Enumeration		en;
		EntityInstance	ce;

		for (en = parent.getChildren(); en.hasMoreElements();) {
			ce = (EntityInstance) en.nextElement();
			diagram.updateYRelLocal(ce, 1.0 - ce.yRelLocal() - ce.heightRelLocal());
		}
		return true;
	}
	
	public String doLayout(Diagram diagram) 
	{
		m_ls.doLayout1(this, null, diagram.getDrawRoot(), false); 
		return "Layout flipped vertically";
	}

	public void processKeyEvent(int key, int modifiers, Object object) 
	{
		Diagram	dg;
		String	rmsg;

		dg = m_ls.getDiagram();
		if (dg != null) {
			dg.beginUndoRedo("Flip Vertically");
			rmsg = doLayout(dg);
			dg.endUndoRedo();
			m_ls.doFeedback(rmsg);
	}	}

}

