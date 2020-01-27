package lsedit;

import java.util.regex.Pattern;
import java.util.Vector;

/* A container for all the various rules used in finding entities */

public class FindRules extends Object 
{
	public	Pattern			m_pattern;
	public	Vector			m_entityClasses;

	public	RelationClass	m_in_edges;
	public	int				m_in_edges_min;
	public	int				m_in_edges_max;
	public	RelationClass	m_out_edges;
	public	int				m_out_edges_min;
	public	int				m_out_edges_max;
	public	int				m_children_min;
	public	int				m_children_max;

	public FindRules(Pattern pattern, Vector entityClasses, RelationClass in_edges, int in_edges_min, int in_edges_max, RelationClass out_edges, int out_edges_min, int out_edges_max, int children_min, int children_max)
	{
		m_pattern       = pattern;
		m_entityClasses = entityClasses;
		m_in_edges      = in_edges;
		m_in_edges_min  = in_edges_min;
		m_in_edges_max  = in_edges_max;
		m_out_edges     = out_edges;
		m_out_edges_min = out_edges_min;
		m_out_edges_max = out_edges_max;
		m_children_min  = children_min;
		m_children_max  = children_max;
	}
}

