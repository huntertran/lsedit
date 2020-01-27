package lsedit;

import java.awt.Color;

/* This class caches colors 
 * It avoids use of key which is expensive to construct
 *
 * N.B. The hash function used must conform with the Color.getHash()
 *
 */


public class ColorCache {


	static class Entry {
		public Color	m_value;
		public Entry	m_next;

		protected Entry(Color color, Entry next) 
		{
			m_value = color;
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

				index         = (e.m_value.getRGB() & 0x7FFFFFFF) % newCapacity;
				e.m_next      = newMap[index];
				newMap[index] = e;
			}
		}
    }

    /**
	  * Add a new color to the cache
      */
    
	protected static void put(Color color, int index) 
	{
		Entry table[] = m_table;

		if (m_count >= m_threshold) {
			// Rehash the table if the threshold is exceeded
			rehash();

			table = m_table;
			index = (color.getRGB() & 0x7FFFFFFF) % table.length;
		} 
		// Creates the new entry.
		table[index] = new Entry(color, table[index]);;
		++m_count;
    }

    /**
     * Returns the color having this rgb.
     *
     */
    
	public static Color get(int rgba) 
	{
		Entry	tab[] = m_table;
		Entry	e;
		int		index;
		Color	color;
	
		if (tab == null) {
			// Construct at runtime since order statics initialized uncertain
			m_table     = new Entry[m_initialCapacity];
			index       = (rgba & 0x7FFFFFFF) % m_table.length;
		} else {
			index       = (rgba & 0x7FFFFFFF) % tab.length;
	
			for (e = tab[index] ; e != null ; e = e.m_next) {
				color = e.m_value;
				if (color.getRGB() == rgba) {
					return color;
		}	}	}
		
		color = new Color(rgba, true);
		put(color, index);
		return color;
    }

	public static Color	get(int r, int g, int b, int a)
	{
		int			rgba;

		rgba =  ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8)  |
				 (b & 0xFF);

		return(get(rgba));
	}

	public static Color	get(int r, int g, int b)
	{
		int			rgb;

		rgb =    0xFF000000        |
		        ((r & 0xFF) << 16) |
                ((g & 0xFF) <<  8) |
				 (b & 0xFF);

		return(get(rgb));
	}

	public static Color	get(float r, float g, float b)
	{
		return(get((int) (r*255+0.5), (int) (g*255+0.5), (int) (b*255+0.5)));
	}

	public static Color getInverse(int rgb)
	{
		return(get(rgb ^ 0xFFFFFF));
	}

	public static Color getBW(Color c)
	{
		int	darkness = c.getRed() + c.getGreen() + c.getBlue();
		int alpha    = c.getAlpha();
		int	mode     = 0;

		if (darkness > 382) {
			mode = 255;
		}
		return get(mode, mode, mode, alpha);
	}
}
