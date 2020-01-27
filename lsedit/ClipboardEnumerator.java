package lsedit;

import java.util.Enumeration;

// A clipboard may never contain zero items within it.  If it did enumeration and
// isEmpty would both break.

public class ClipboardEnumerator implements Enumeration
{
	Clipboard		m_clipboard;
	EntityInstance	m_oldContainer;
	int				m_at;

	ClipboardEnumerator(Clipboard clipboard)
	{
		m_clipboard = clipboard;
		m_at        = 0;
	}

	public EntityInstance oldContainer()
	{
		return m_oldContainer;
	}

	// Enumeration interface

	public boolean hasMoreElements()
	{
		return (m_clipboard != null);
	}

	public Object nextElement() 
	{
		Object	o      = m_clipboard.elementAt(m_at);
		m_oldContainer = m_clipboard.oldContainer(m_at);
	
		if (++m_at == m_clipboard.size()) {
			m_clipboard = m_clipboard.getExtendsClipboard();
			m_at        = 0;
		}
		return o;
	}
}
