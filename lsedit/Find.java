package lsedit;

import java.util.Enumeration;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Find extends Object 
{
	protected Vector m_results = new Vector();
	protected int	 m_ind     = 0;

	protected void find(EntityInstance e, FindRules rules) 
	{
		Enumeration		en;
		EntityInstance	ce;
		Matcher			m;
		Vector			entityClasses;
		Object			ec, ec1;
		int				i;

		for (en = e.getChildren(); en.hasMoreElements(); ) {
			ce = (EntityInstance) en.nextElement();

			if (rules.m_pattern != null) {
				m = rules.m_pattern.matcher(ce.getEntityLabel());
				if (!m.matches()) {
					continue;
			}	}
			entityClasses = rules.m_entityClasses;
			if (entityClasses != null) {
				ec = ce.getEntityClass();
				for (i = entityClasses.size(); --i >= 0; ) {
					ec1 = entityClasses.elementAt(i);
					if (ec == ec1) {
						break;
				}	}
				if (i < 0) {
					continue;
			}	}
			if (rules.m_in_edges_min > 0 || rules.m_in_edges_max >= 0) {
				if (!ce.hasActiveInEdges(rules.m_in_edges, rules.m_in_edges_min, rules.m_in_edges_max)) {
					continue;
			}	}
			if (rules.m_out_edges_min > 0 || rules.m_out_edges_max >= 0) {
				if (!ce.hasActiveOutEdges(rules.m_out_edges, rules.m_out_edges_min, rules.m_out_edges_max)) {
					continue;
			}	}


//			System.out.println("Found " + ce);
			m_results.addElement(ce);
		}	
		for (en = e.getChildren(); en.hasMoreElements(); ) {
			ce = (EntityInstance) en.nextElement();
			find(ce, rules);
		}
	}

	/*
	 * Public methods
	 */

	public Find(EntityInstance root, FindRules rules) 
	{
		find(root, rules);
	}

	public void clear()
	{
		m_results.clear();
	}

	public boolean isEmpty() 
	{
		return m_results.isEmpty();
	}

	public boolean atEnd()
	{
		return (m_ind >= m_results.size());
	}

	public boolean atBeginning()
	{
		return (m_ind <= 0);
	}
	
	public void reset() 
	{
		m_ind = 0;
	}

	public Vector nextResult() 
	{
		if (m_ind < 0 || m_ind >= m_results.size()) {
			return null;
		}

		Vector v = new Vector();
		EntityInstance e  = (EntityInstance) m_results.elementAt(m_ind);
		EntityInstance pe = e.getContainedBy();

		for (; m_ind < m_results.size(); ++m_ind) {
			e = (EntityInstance) m_results.elementAt(m_ind);
			if (e.getContainedBy() != pe) {
				break;
			}
			v.addElement(e);
		} 
		return v;
	}

	public boolean regress() 
	{
		if (m_ind >  0) {
			int orgInd = m_ind;

			// Move back before the start of the last group returned

			EntityInstance e  = (EntityInstance) m_results.elementAt(--m_ind);
			EntityInstance pe = e.getContainedBy();

			for (; --m_ind >= 0; ) {
				e = (EntityInstance) m_results.elementAt(m_ind);
				if (e.getContainedBy() != pe) {
					pe = e.getContainedBy();
					// Move back to the start of the group we want to return
					for (; --m_ind >= 0; ) {
						e = (EntityInstance) m_results.elementAt(m_ind);
						if (e.getContainedBy() != pe) {
							break;
					}	}
					++m_ind;
					return true;
			}	}
			m_ind = orgInd;
		} 
		return false;
	}

	public boolean entityCut()
	{
		int				i, size, newsize;
		EntityInstance	e;

		size = newsize = m_results.size();
		for (i = 0; i < newsize; ) {
			e = (EntityInstance) m_results.elementAt(i);
			if (e.isMarked(EntityInstance.DELETED_MARK)) {
				m_results.remove(i);
				--newsize;
				continue;
			}
			++i;
		}
		if (m_ind > newsize) {
			m_ind = newsize;
		}
		return (size != newsize);
	}
}

