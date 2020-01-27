package lsedit;

import java.awt.Font;
import javax.swing.JComponent;
import javax.swing.MenuElement;
import javax.swing.JTabbedPane;

/* This class caches fonts
 * It avoids use of key which is expensive to construct
 * This won't cache Fonts returned by Swing classes
 *
 * N.B. The hash function used must conform with the Font.hashCode()
 *
 */

public class FontCache {


	static class Entry {
		public Font 	m_value;
		public Entry	m_next;

		protected Entry(Font font, Entry next) 
		{
			m_value = font;
			m_next  = next;
		}
	};

	// Here for convenience

	protected static Font m_menuFont   = null; 
	protected static Font m_dialogFont = null;

	private static final int	m_initialCapacity = 50;

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
    }

	// Must agree with Font.hashCode()
	
	protected static int hashCode(String name, int style, int size)
	{
		// N.B name -- not family

		return name.hashCode() ^ style ^ size;
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

				index         = (e.m_value.hashCode() & 0x7FFFFFFF) % newCapacity;
				e.m_next      = newMap[index];
				newMap[index] = e;
			}
		}
    }

    /**
	  * Add a new font to the cache
      */
    
	protected static void put(Font font, int index) 
	{
		Entry table[] = m_table;

		if (m_count >= m_threshold) {
			// Rehash the table if the threshold is exceeded
			rehash();

			table = m_table;
			index = (font.hashCode() & 0x7FFFFFFF) % table.length;
		} 
		// Creates the new entry.
		table[index] = new Entry(font, table[index]);;
		++m_count;
    }

    /**
     * Returns the Font having this property.
     *
     */
    
	public static Font get(String name, int style, int size) 
	{
		Entry	tab[] = m_table;
		int		hash  = hashCode(name, style, size);
		Entry	e;
		int		index;
		Font	font;
	
		if (tab == null) {
			// Construct at runtime since order statics initialized uncertain
			m_table     = new Entry[m_initialCapacity];
			index       = (hash & 0x7FFFFFFF) % m_table.length;
		} else {
			index       = (hash & 0x7FFFFFFF) % tab.length;
	
			for (e = tab[index] ; e != null ; e = e.m_next) {
				font = e.m_value;
				if (name.equals(font.getName()) && style == font.getStyle() && size == font.getSize()) {
					return font;
		}	}	}
		
		font = new Font(name, style, size);
		put(font, index);
		return font;
    }

	// Utility font routines

	public static Font getDialogFont()
	{
		Font dialogFont = m_dialogFont;
		
		if (dialogFont == null) {
			m_dialogFont = dialogFont = Options.getTargetFont(Option.FONT_DIALOG);
		}
		return dialogFont;
	}

	public static void setDialogFont(Font font)
	{
		m_dialogFont = font;
	}

	public static Font getMenuFont()
	{
		Font	menuFont = m_menuFont;
		
		if (menuFont == null) {
			m_menuFont = menuFont = Options.getTargetFont(Option.FONT_MENU);
		} 
		return menuFont;
	}

	public static void setMenuFont(Font font)
	{
		m_menuFont = font;
	}

	public static void setMenuFont(MenuElement element, Font font)
	{
		MenuElement[]	subelements = element.getSubElements();
		int				i;

		((JComponent) element).setFont(font);
		if (subelements != null) {
			for (i = 0; i < subelements.length; ++i) {
				setMenuFont(subelements[i], font);
		}	}
	}

	public static void setMenuTreeFont(MenuElement element)
	{
		setMenuFont(element, getMenuFont());
	}
}
