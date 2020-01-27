package lsedit;


/* This class caches attributes for reuse */

public class AttributeCache {

	static class Entry {
		public Attribute	m_attribute;
		public Entry		m_next;

		protected Entry(Attribute attribute, Entry next) 
		{
			m_attribute = attribute;
			m_next      = next;
		}
	};

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

    private static int m_count    = 0;
	private static int m_mask     = (1 << 16) - 1;

	private static int m_actual_attributes     = 0;
	private static int m_requested_attributes  = 0;

    /**
     * The table is rehashed when its size exceeds this threshold.  (The
     * value of this field is (int)(capacity * loadFactor).)
     *
     * @serial
     */

    private static int m_threshold = (int) (m_mask * m_loadFactor);
							 
     /**
     * Clears this hashtable so that it contains no keys. 
     */
    
	public static void activate()
	{
		if (m_table == null) {
			m_table = new Entry[m_mask+1];
			m_count = 0;
	}	}

	public static void deactivate() 
	{
		Entry	tab[] = m_table;
		Entry	entry, next;
		int		index;
	
		if (tab != null) {
			for (index = tab.length; --index >= 0; ) {
				for (entry = tab[index]; entry != null; entry = next) {
					entry.m_attribute = null;
					next              = entry.m_next;
					entry.m_next      = null;
				}
				tab[index] = null;
			}
			m_table = null;
		}
    }

	protected static int hashCode(String id, String value)
	{
		int hashcode;

		hashcode = id.hashCode();
		if (value != null) {
			hashcode ^= value.hashCode();
		}
		return (hashcode & m_mask);
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
		Entry		oldMap[]    = m_table;
		Entry		newMap[];
		Entry		entry, next;
		Attribute	attribute;
		int			i, index;

		m_mask      = (m_mask << 1) | 1;
		m_threshold = (int)(m_mask * m_loadFactor);

		m_table = newMap = new Entry[m_mask + 1];

		for (i = oldMap.length; --i >= 0; ) {
			for (entry = oldMap[i]; entry != null; entry = next) {
				attribute            = entry.m_attribute;
				next                 = entry.m_next;
				index                = hashCode(attribute.m_id, attribute.externalString());
				entry.m_next         = newMap[index];
				newMap[index]        = entry;
	}	}	}

    /**
     * Returns the Attribute having this property.
     *
     */
    
	public static Attribute get(String id, String value) 
	{
		Entry		table[] = m_table;
		int			index = hashCode(id, value);
		Entry		entry;
		Attribute	attribute;
		String		value1;
	
		++m_requested_attributes;

		if (table == null) {
			// Not cached
			attribute  = new Attribute(id, value);
		} else {
			for (entry = table[index]; entry != null; entry = entry.m_next) {
				attribute = entry.m_attribute;
				if (!id.equals(attribute.m_id)) {
					continue;
				}
				value1 = attribute.externalString();
				if (value == null) {
					if (value1 == null) {
						return(attribute);
					}
				} else {
					if (value.equals(value1)) {
						return(attribute);
			}	}	}

			// Attribute not found

			attribute            = new Attribute(id, value);
			entry                = new Entry(attribute, table[index]);
			table[index]         = entry;

			if (++m_count > m_threshold) {
				// Rehash the table if the threshold is exceeded
				rehash();
		}	}
		++m_actual_attributes;

		return attribute;
    }

	public static int requestedAttributes()
	{
		return m_requested_attributes;
	}

	public static int actualAttributes()
	{
		return m_actual_attributes;
	}
}
