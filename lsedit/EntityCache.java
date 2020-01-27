package lsedit;

/* This class caches entities in the current diagram. It provides the
 * ability to search for current entities by name, to scan all current
 * entities, to count current entities, etc.
 *
 * Entities in a clipboard but not in the diagram do not occur in this
 * cache.  Whenever a diagram is unloaded its cache is emptied and a
 * check is made to ensure that the diagram entities match the cached
 * entities.
 */

public class EntityCache {

	static class Entry {
		public EntityInstance 	m_value;
		public Entry			m_next;

		protected Entry(EntityInstance e, Entry next) 
		{
			m_value = e;
			m_next  = next;
		}
	};

    private Entry m_table[]    = null;
	private int	 m_last_index  = 0;

	private int	 m_next        = 0;
	private Entry m_current    = null;

    /**
     * The total number of entries in the hash table.
     */

    private int m_count = 0;

    /**
     * Returns the number of keys in this hashtable.
     *
     * @return  the number of keys in this hashtable.
     */

    public int size() 
	{
		return m_count;
    }

    /**
     * Clears this hashtable so that it contains no keys. 
     */
    
	public void clear() 
	{
		m_table      = null;
		m_count      = 0;
		m_last_index = 0;
		m_next       = 0;
		m_current    = null;
    }

	public EntityCache()
	{
		clear();
	}

    /**
	  * Add a new Entity to the cache
      */
    
	public void put(EntityInstance e) 
	{
		Entry	table[] = m_table;
		int		index;

		if (table == null) {
			table   = new Entry[0x40000];	// 262,144
			m_table = table;
		} 
		m_last_index = index = e.getId().hashCode() & 0x3FFFF;
		{
			Entry			entry;
			EntityInstance	e1;

			for (entry = table[index] ; entry != null ; entry = entry.m_next) {
				e1 = entry.m_value;
				if (e1.getId().equals(e.getId())) {
					if (e == e1) {
						return;
		}	}	}	}

		// Creates the new entry.
		table[index] = new Entry(e, table[index]);
		++m_count;
    }

	public void remove(EntityInstance e) 
	{
		Entry	table[] = m_table;
		int		index;

		if (table != null) {
			index = e.getId().hashCode() & 0x3FFFF;

			Entry			entry, prev;
			EntityInstance	e1;

			for (entry = table[index], prev = null ; entry != null ; prev = entry, entry = entry.m_next) {
				if (e == entry.m_value) {
					if (prev != null) {
						prev.m_next = entry.m_next;
					} else {
						table[index] = entry.m_next;
					}
					entry.m_value = null;
					--m_count;
					return;
		}	}	}
		System.out.println("EntityCache: can't remove " + e);
    }

    /**
     * Returns the Font having this property.
     *
     */
    
	public EntityInstance get(String id) 
	{
		Entry			table[] = m_table;
		Entry			entry;
		EntityInstance	e;
	
		if (table != null) {
			for (entry = table[id.hashCode() & 0x3FFFF] ; entry!= null ; entry = entry.m_next) {
				e = entry.m_value;
				if (id.equals(e.getId())) {
					return e;
		}	}	}
		return null;
    }

	public EntityInstance someEntity()
	{
		if (m_count != 0) {
			Entry	table[] = m_table;
			Entry	entry   = table[m_last_index];

			if (entry != null) {
				return entry.m_value;
			}

			int		index, length;

			length = table.length;
			for (index = 0; index < length; ++index) {
				entry = table[index];
				if (entry != null) {
					return entry.m_value;
		}	}	}
		return null;
	}

	protected EntityInstance getFirst(int start)
	{
		int		lth = (m_table == null ? 0 : m_table.length);
		Entry	current;
		int		next;

		for (next = start; next < lth; ++next) {
			current = m_table[next];
			if (current != null) {
				m_current = current.m_next;
				m_next    = ++next;
				return current.m_value;
		}	}
		return null;
	}

	public EntityInstance getNext()
	{
		EntityInstance e;

		if (m_current != null) {
			e         = m_current.m_value;
			m_current = m_current.m_next;
			return e;
		}
		return getFirst(m_next);
	}

	public EntityInstance getFirst()
	{
		return getFirst(0);
	}

	/* For debugging [Can't use toString -- static class] */

	public void show()
	{
		Entry	table[] = m_table;
		Entry	entry;
		int		index, length;

		if (table == null) {
			System.out.println("Null Entity Cache");
		}
		if (m_count == 0) {
			System.out.println("Empty Entity Cache");
		}
		length = table.length;
		for (index = 0; index < length; ++index) {
			entry = table[index];
			if (entry != null) {
				System.out.print(index + ": ");
				for (; entry != null; entry = entry.m_next) {
					System.out.print(" " + entry.m_value);
				}
				System.out.println("");
	}	}	}
}
