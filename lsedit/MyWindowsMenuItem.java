package lsedit;

import java.awt.Color;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

class MyWindowsMenuItem extends MyHistoryMenuItem implements ActionListener
{
	protected Diagram	m_diagram;

	public MyWindowsMenuItem(JMenu menu, Diagram diagram, String path, LandscapeEditorCore ls)
	{
		super(menu, path, ls);
		m_diagram = diagram;
	}

	public void activeDiagram(Diagram diagram)
	{
		if (diagram == m_diagram) {
			setForeground(Color.BLUE);
		} else {
			setForeground(Color.BLACK);
	}	}

	public boolean isDiagram(Diagram diagram)
	{
		return diagram == m_diagram;
	}

	public Diagram getDiagram()
	{
		return m_diagram;
	}
	
	public Diagram getDiagram(String path)
	{
		if (m_path.equals(path)) {
			return m_diagram;
		}
		return null;
	}

	public void actionPerformed(ActionEvent ev)
	{
		m_ls.switchDiagram(m_diagram, m_path, null, false);
	}
}
