package lsedit;

import java.util.Vector;

// The clipboard is implemented as a vector of entities that have been clipped plus 
// an optional earlier clipboard that this extends
//
// A clipboard may never contain zero items within it.  If it did enumeration and
// isEmpty would both break.

public class Clipboard extends Vector
{
	private	Vector		m_old_containers = new Vector();
	private Clipboard	m_extends        = null;

	public Clipboard(Vector v)
	{
		EntityInstance	e, e1;
		int				i, j;

		// Descendants occurs before their anscestors
		// Make sure we cut descendants before ancestors so that we can still find them
		SortVector.byPreorder(v, false);

		for (i = 0; i < v.size(); ) {
			e1 = (EntityInstance) v.elementAt(i);
			for (j = v.size(); ; ) {
				--j;
				if (j == i) {
					++i;
					break;
				}
				e = (EntityInstance) v.elementAt(j);
				if (e.hasDescendantOrSelf(e1)) {
					v.remove(i);
					break;
		}	}	}
					
		for (i = 0; i < v.size(); ++i) {
			add(v.elementAt(i));
		}
	}

	public int clipboardSize()
	{
		Clipboard	clipboard;
		int			ret;

		ret = 0;
		for (clipboard = this; clipboard != null; clipboard = clipboard.getExtendsClipboard()) {
			ret += clipboard.size();
		}
		return(ret);
	}

	public boolean add(Object o)
	{
		m_old_containers.add( ((EntityInstance) o).getContainedBy() );
		return super.add(o);
	}

	public Clipboard getExtendsClipboard()
	{
		return m_extends;
	}

	public void setExtendsClipboard(Clipboard clipboard)
	{
		m_extends              = clipboard;
	}
			
	public ClipboardEnumerator clipboardElements()
	{
		return new ClipboardEnumerator(this);
	}

	public EntityInstance oldContainer(int i)
	{
		return (EntityInstance) m_old_containers.elementAt(i);
	}

	public String toString()
	{
		switch (size()) {
		case 0:
			return "clipboard";
		case 1:
			return elementAt(0).toString();
		}
		return elementAt(0).toString() + "...";
	}
}

