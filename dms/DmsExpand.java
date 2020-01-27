package dms;

import lsedit.EntityInstance;

public class DmsExpand {
	String			m_namespace;
	String			m_localName;
	String			m_url;
	EntityInstance	m_e;
	String			m_edgeUri;
	String			m_edgeType;

	DmsExpand(String namespace, String localName, String url, EntityInstance e, String edgeUri, String edgeType)
	{
		m_namespace = namespace;
		m_localName = localName;
		m_url		= url;
		m_e			= e;
		m_edgeUri   = edgeUri;
		m_edgeType  = edgeType;
	}
}
