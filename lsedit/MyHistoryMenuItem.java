package lsedit;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

class MyHistoryMenuItem extends JMenuItem implements ActionListener
{
	protected LandscapeEditorCore	m_ls;
	protected String				m_path;


	public static String getLabel(String path)
	{
		int		from, to;

		if (path == null) {
			return("");
		}
		from  = path.lastIndexOf('/');
		to    = path.lastIndexOf('\\');
		if (to > from) {
			from = to;
		}
		to    = path.length();
		if (to > 3 && path.endsWith(".ta")) {
			to -= 3;
		}
		return(path.substring(from+1, to));
	}

	public void setPath(String path)
	{
		m_path = path;
		if (path == null || path.length() == 0) {
			path = "New Landscape";
		}
		setText(getLabel(path));
		setToolTipText(path);
	}

	public MyHistoryMenuItem(JMenu menu, String path, LandscapeEditorCore ls)
	{
		super();
		m_ls = ls;
		setPath(path);
		menu.add(this);
		addActionListener(this);
	}

	public boolean isPath(String path) 
	{
		return(m_path.equals(path));
	}

	public String getPath()
	{
		return m_path;
	}

	public void actionPerformed(ActionEvent ev)
	{
		int	modifiers;

//		System.out.println("HistoryMenuItem clicked");

		modifiers = ev.getModifiers();

		if ((modifiers & ActionEvent.META_MASK) != 0) {
			int	choice;

			switch (JOptionPane.showConfirmDialog(null, m_path, "Delete All history menu items", JOptionPane.YES_NO_CANCEL_OPTION)) {
			case JOptionPane.YES_OPTION:
				m_ls.removeHistory();
				break;
			case JOptionPane.NO_OPTION:
				m_ls.removeHistoryMenu(this);
				break;
			}
			return;
		} else {
			m_ls.loadLs(m_path, null, false);	// Load landscape from history
	}	}
}
