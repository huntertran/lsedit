package lsedit;

import java.io.File;

import java.awt.Font;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/* This class caches icons 
 * It avoids use of key which is expensive to construct
 *
 * N.B. The hash function used must conform with the Icon.getHash()
 *
 */


public class IconCache {

	static class Entry {
		public String		m_name;
		public Icon			m_value;
		public Entry		m_next;

		protected Entry(String name, Icon icon, Entry next) 
		{
			m_name  = name;
			m_value = icon;
			m_next  = next;
		}
	};

	private static final int   m_initialCapacity = 200;

    /**
     * The load factor for the hashtable.
     *
     * @serial
     */

    private static final float m_loadFactor = 0.75f;

    /**
     * The hash table.
     */

    private static Entry m_table[] = null;

    /**
     * The total number of entries in the hash table.
     */

    private static int m_count = 0;
    
	static Icon[] g_elisionCache = new Icon[256];


    /**
     * The table is rehashed when its size exceeds this threshold.  (The
     * value of this field is (int)(capacity * loadFactor).)
     *
     * @serial
     */

    private static int m_threshold = (int) (m_initialCapacity * m_loadFactor);
							 
    /**
     * Returns the number of keys in this hashtable.
     *
     * @return  the number of keys in this hashtable.
     */

    public static int size() 
	{
		return m_count;
    }

    /**
     * Tests if this hashtable maps no keys to values.
     *
     * @return  <code>true</code> if this hashtable maps no keys to values;
     *          <code>false</code> otherwise.
     */
    
	public static boolean isEmpty() 
	{
		return m_count == 0;
    }

	public static void clearElisionCache()
	{
		g_elisionCache = null;
	}
	
    /**
     * Clears this hashtable so that it contains no keys. 
     */
    
	public static void clear() 
	{
		Entry	tab[] = m_table;
		int		index;
	
		if (tab != null) {
			for (index = tab.length; --index >= 0; ) {
				tab[index] = null;
		}	}
		m_count = 0;
		clearElisionCache();
    }

    /**
     * Increases the capacity of and internally reorganizes this 
     * hashtable, in order to accommodate and access its entries more 
     * efficiently.  This method is called automatically when the 
     * number of keys in the hashtable exceeds this hashtable's capacity 
     * and load factor. 
     */
    
	protected static void rehash() 
	{
		Entry	old, e;
		int		i, index;

		Entry	oldMap[]    = m_table;
		int		oldCapacity = oldMap.length;

		int newCapacity     = oldCapacity * 2 + 1;
		Entry newMap[]      = new Entry[newCapacity];

		m_threshold = (int)(newCapacity * m_loadFactor);
		m_table     = newMap;

		for (i = oldCapacity ; i-- > 0 ;) {
			for (old = oldMap[i] ; old != null ; ) {
				e   = old;
				old = old.m_next;

				index         = (e.m_name.hashCode() & 0x7FFFFFFF) % newCapacity;
				e.m_next      = newMap[index];
				newMap[index] = e;
			}
		}
    }

    /**
	  * Add a new Icon to the cache
      */
    
	protected static void put(String name, Icon icon) 
	{
		Entry	table[] = m_table;
		int		index;

		if (m_count >= m_threshold) {
			// Rehash the table if the threshold is exceeded
			rehash();
			table = m_table;
		} 
		// Creates the new entry.

		index        = (name.hashCode() & 0x7FFFFFFF) % table.length;
		table[index] = new Entry(name, icon, table[index]);;
		++m_count;
    }

    /**
     * Returns the icon that corresponds to the given filename.
     *
     */
    
	public static Icon lookup(String name) 
	{
		Entry		tab[] = m_table;
		Entry		e;
		int			index;
	
		if (tab != null) {
			index  = (name.hashCode() & 0x7FFFFFFF) % tab.length;
	
			for (e = tab[index] ; e != null ; e = e.m_next) {
				if (name.compareTo(e.m_name) == 0) {
					return(e.m_value);
		}	}	}
		return null;
	}
		
	public static Icon get(String name, EntityClass entityClass) 
	{
		Entry		tab[]         = m_table;
		Icon		icon          = null;
		String		name1;
		int			index;
		File		file;
	
		if (tab == null) {
			// Construct at runtime since order statics initialized uncertain
			m_table = tab = new Entry[m_initialCapacity];
		}
		name  = name.replace('\\', '/');
		name1 = name;
		
		if (name.length() > 0) {
			file = new File(name);
			if (!file.isAbsolute()) {
				Option		diagramOptions = Options.getDiagramOptions();
				String		path           = diagramOptions.getIconPath();
				Diagram		diagram        = null;
				String		diagramDir     = null;
				String		path1;
				
				if (path != null && path.length() > 0) {
					for (;;) {
						if (path == null) {
							return null;
						}
						index = path.indexOf(';');
						if (index < 0) {
							path1 = path;
							path  = null;
						} else {
							path1 = path.substring(0, index);
							path  = path.substring(index + 1);
						}
						path1 = path1.trim();
						if (path1.length() == 0) {
							continue;
						}
						name1 = path1 + "/" + name;
						if (name1.startsWith("./")) {
							if (diagram == null) {
								Object	context;
								
								diagram = entityClass.getDiagram();
								context = diagram.getContext();
								if (context != null && context instanceof File) {
									diagramDir = ((File) context).getParent();
							}	}
							if (diagramDir != null) {
								name1 = diagramDir.replace('\\','/') + "/" + name1.substring(2);
						}	}
						file = new File(name1);
						if (file.exists()) {
							break;
			}	}	}	}
						
			if (file.canRead())	{
				icon = new ImageIcon(name1);
				put(name, icon);
		}	}
		file = null;
		return (icon);
	}
	
	public static Icon getElisionIcon(int elisions, String text)
	{
		if (g_elisionCache == null) {
			g_elisionCache = new Icon[64];
		}
		elisions &= SelectedElisions.ALL_ELIDED;

		Icon icon = g_elisionCache[elisions];
		
		if (icon == null) {
			g_elisionCache[elisions] = icon = new SelectedElisions(elisions |  SelectedElisions.CHANGE | SelectedElisions.BOX);
			((JLabel) icon).setText(text);
		}
		return icon;
	}
	
	public static void setElisionIconFont(Font font)
	{
		if (g_elisionCache != null) {
			JLabel	label;
			int		i;

			for (i = g_elisionCache.length; --i >= 0; ) {
				label = (JLabel) g_elisionCache[i];
				if (label != null) {
					label.setFont(font);
	}	}	}	}
	
}
