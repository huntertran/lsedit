package lsedit;

import java.awt.Event;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

public class RightTabbedPane extends JTabbedPane implements ToolBarEventHandler, MouseListener {

	LandscapeEditorCore	m_ls;
	JPopupMenu			m_popup = null;

	public RightTabbedPane(LandscapeEditorCore ls)
	{
		m_ls = ls;
		addMouseListener(this);
	}

	public void setTabsScroll(boolean state)
	{
		setTabLayoutPolicy((state ? SCROLL_TAB_LAYOUT : WRAP_TAB_LAYOUT));
	}

/*
	public void validate()
	{
		System.out.println("RightTabbedPane.validate() " + getBounds());
		java.lang.Thread.dumpStack();
		System.out.println("-----");

		super.validate();
	}
 */


	// MouseListener interface

	public void mouseClicked(MouseEvent e)
	{
	}

	public void mouseEntered(MouseEvent e)
	{
	}

	public void mouseExited(MouseEvent e)
	{
	}

	public void mousePressed(MouseEvent ev)
	{
		if (ev.isMetaDown()) {
			JMenuItem			mi;
			int					x  = ev.getX();
			int					y  = ev.getY();

			if (m_popup == null) {
				JPopupMenu m = new JPopupMenu("Tabbed Pane menu");

				m_popup = m;
				new MyMenuItem(m, "Flip position", this, -1, 'l', "Reverse the left/right position of the tab pane");
				new MyMenuItem(m, "Flip tabs",     this, -1, 't', "Move where the tabs are with respect to the tab pane");
				new MyMenuItem(m, "Scroll tabs",   this, -1, 's', "Use/don't use scrolling tabs");
			}
			FontCache.setMenuTreeFont(m_popup); 
			m_popup.show(this, x, y);
	}	}

	public void mouseReleased(MouseEvent ev)
	{
	}

	// ToolBarEventHandler

	public boolean processMetaKeyEvent(String name)
	{
		return (m_ls.processMetaKeyEvent(name));
	}

	public void processKeyEvent(int key, int modifiers, Object object)
	{
		switch (key) {
			case 'l':
			{
				Option	option = Options.getDiagramOptions();
				boolean	value  = !option.isLeftTabbox();
				
				option.setLeftTabbox(value);
				m_ls.changeLeftTabbox(value);
				break;
			}
			case 't':
			{
				int placement = getTabPlacement();
				switch (placement) {
				case JTabbedPane.TOP:
					placement = JTabbedPane.LEFT;
					break;
				case JTabbedPane.BOTTOM:
					placement = JTabbedPane.RIGHT;
					break;
				case JTabbedPane.LEFT:
					placement = JTabbedPane.BOTTOM;
					break;
				case JTabbedPane.RIGHT:
					placement = JTabbedPane.TOP;
					break;
				default:
					return;
				}
				setTabPlacement(placement);
				break;
			}
			case 's':
			{
				Option	option = Options.getDiagramOptions();
				boolean	value  = !option.isTabsScroll(); 
				
				option.setTabsScroll(value);
				setTabsScroll(value);
				break;
		}	}
	 }

	 public void showInfo(String msg)
	 {
	 }		
}

