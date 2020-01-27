package lsedit;

import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;

/* Perform basic setup and operations for a simple tab box */

public class TabBox extends JComponent
{
	protected LandscapeEditorCore   m_ls;
	protected JTabbedPane			m_tabbedPane;
	protected JScrollPane			m_scrollPane;
	protected String				m_tabLabel;

	// --------------
	// Public methods 
	// --------------

	public TabBox(LandscapeEditorCore ls, JTabbedPane tabbedPane, String tabLabel, String helpString) 
	{
		Insets	insets = tabbedPane.getInsets();
		int		width  = tabbedPane.getWidth()  - insets.left - insets.right;
		int		height = tabbedPane.getHeight() - insets.top  - insets.bottom;
		
		setLayout(null);
		setLocation(0,0);

		m_ls          = ls;
		m_tabbedPane  = tabbedPane;
		m_tabLabel    = tabLabel;

		setBackground(Diagram.boxColor);
//		setToolTipText(helpString);

		m_scrollPane = new JScrollPane();
		m_scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
		m_scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
		m_scrollPane.setBounds(0, 0, width, height);
		setSize(width, height);

		m_scrollPane.setViewportView(this);
		tabbedPane.addTab(tabLabel, null, m_scrollPane, helpString);
	}

	public LandscapeEditorCore getLs()
	{
		return m_ls;
	}

	public void activate() 
	{
		m_tabbedPane.setSelectedComponent(m_scrollPane);
	}

	public boolean isActive() 
	{
		return(m_scrollPane == m_tabbedPane.getSelectedComponent());
	}

	// TaListener defaults

	public void diagramChanging(Diagram diagram)
	{
	}
	
	public void diagramChanged(Diagram diagram, int signal)
	{
		System.out.println(m_tabLabel + " TabBox ignored diagram change");
	}

	public void updateBegins()
	{
	}

	public void updateEnds()
	{
	}

	public void entityClassChanged(EntityClass ec, int signal)
	{
		System.out.println(m_tabLabel + " TabBox ignored entityClassChanged");
	}

	public void relationClassChanged(RelationClass rc, int signal)
	{
		System.out.println(m_tabLabel + " TabBox ignored relationClassChanged");
	}

	public void entityParentChanged(EntityInstance e, EntityInstance parent, int signal)
	{
		System.out.println(m_tabLabel + " TabBox ignored entityParentChanged");

	}

	public void relationParentChanged(RelationInstance ri, int signal)
	{
		System.out.println(m_tabLabel + " TabBox ignored relationParentChanged");
	}

	public void entityInstanceChanged(EntityInstance e, int signal)
	{
		System.out.println(m_tabLabel + " TabBox ignored entityInstanceChanged");
	}

	public void relationInstanceChanged(RelationInstance ri, int signal)
	{
		System.out.println(m_tabLabel + " TabBox ignored relationInstanceChanged");
	}
}



