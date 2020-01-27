package dms;

import java.lang.Character;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import lsedit.EntityInstance;

/* http://www.saxproject.org/apidoc/org/xml/sax/helpers/DefaultHandler.html */

/* Tracks tags seen */

class DmsTag {
	public	String			m_namespace;
	public	String			m_localName;

	DmsTag(String namespace, String localName)
	{
		m_namespace = namespace;
		m_localName = localName;
	}
};

/* Entries in the stack */

class DmsEntry extends DmsTag {
	public	String			m_qName;
	public	EntityInstance	m_e;		// if null then and edge type
	public	int				m_virtual;	// 1 if doesn't map to a real XML node 2 if collection
	public	String			m_xml_lang;
	public	String			m_xml_base;
	public	int				m_line;
	public	int				m_column;

	DmsEntry(String namespace, String qName, String localName, String xml_lang, Locator locator) 
	{
		super(namespace, localName);

		m_qName     = qName;
		m_e         = null;
		m_virtual   = 0;
		m_xml_lang  = xml_lang;
		m_xml_base  = null;
		if (locator == null) {
			m_line   = -1;
			m_column = -1;
		} else {
			m_line   = locator.getLineNumber();
			m_column = locator.getColumnNumber();
	}	}
};

public class DmsHandler extends DefaultHandler {
	
	Locator		m_locator     = null;
	BasicReader	m_basicReader = null;
    Vector		m_stack       = new Vector();
	String		m_datatype    = null;
	String		m_xml_base;
	String		m_characters  = null;

	static final boolean	f_debug_parse  = false;

	static final String		f_rdf_special[] = {"about", "nodeID", "resource", "parseType", "datatype", "ID" };
	static final String		f_protocols[] = { "http:", "https:", "urn:" };

	private	String
	prefixURL(String url)
	{

		if (url == null) {
			return url;
		}
		String		protocol = null;
		String		entityId = url;
		String		base     = m_xml_base;
		int			at       = m_stack.size();
		DmsEntry	entry;
		int			i;

		for (i = f_protocols.length; ; ) {
			if (--i < 0) {
				protocol = null;
				break;
			}
			protocol = f_protocols[i];
			if (entityId.startsWith(protocol)) {
				if (i > 1) {
					return url;
				}
				entityId = entityId.substring(protocol.length());
				break;
		}	}

		while (!entityId.startsWith("//")) {
			while (base == null && 0 <= --at) {
				entry = (DmsEntry) m_stack.elementAt(at);
				base  = entry.m_xml_base;
			}
			if (base == null) {
				break;
			}
			switch (entityId.charAt(0)) {
			case '#':
				if (base.endsWith("/") || base.endsWith("#")) {
					entityId = base.substring(0, base.length()-1) + entityId;
				} else {
					entityId = base + entityId;
				}
				break;
			case '/':
				if (base.endsWith("/")) {
					entityId = base + entityId.substring(1);
				} else {
					entityId = base + entityId;
				}
				break;
			default:
				if (base.endsWith("/")) {
					entityId = base + entityId;
				} else {
					entityId = base + "/" + entityId;
			}	}
			base = null;
		}
		if (protocol != null) {
			entityId = protocol + entityId;
		}
		return entityId;
	}

	public
	DmsHandler(BasicReader reader)
	{
		m_basicReader = reader;
	}

	public void
	setDocumentLocator(Locator locator)
	{
		m_locator = locator;
	}

	public void
	startDocument()
	{
		m_basicReader.start();
		m_datatype = null;
		m_xml_base = m_basicReader.m_rdf;
		m_stack.clear();
		m_characters = "";
	}

	public void
    characters(char ch[], int start, int length)
	{
		m_characters += new String(ch, start, length);
	}

	public void
	addContent() throws SAXException
	{
		String	characters = m_characters;
		int 	lth        = characters.length();
		int		start, toend;

		m_characters = "";
		for (start = 0; start < lth; ++start) {
			if (!Character.isWhitespace(characters.charAt(start))) {
				break;
		}	}

		for (toend = lth; ; --toend) {
			if (start == toend) {
				return;
			}
			if (!Character.isWhitespace(characters.charAt(toend-1))) {
				break;
		}	}
		if (start != 0 || toend != lth) {
			characters = characters.substring(start, toend);
		}

		int	stack_size = m_stack.size();

		if (f_debug_parse) {
			int i;

			for (i = stack_size; i > 0; --i) {
				System.out.print("  ");
			}
			System.out.println("(" + (characters.length()) + ")" + characters);
		}

		DmsEntry		entry  = (DmsEntry) m_stack.elementAt(--stack_size);
		DmsEntry		entry1 = (DmsEntry) m_stack.elementAt(--stack_size);
		EntityInstance	e      = entry1.m_e;
		String			value;
		int				line;
		int				column;

		if (m_locator == null) {
			line   = -1;
			column = -1;
		} else {
			line   = m_locator.getLineNumber();
			column = m_locator.getColumnNumber();
		}


		if (e == null) {
			fail();
		}
		m_basicReader.addLiteral(e, entry.m_namespace, entry.m_localName, characters, m_datatype, "literal", line, column);
	}

	public void
    startElement(String namespace, String localName, String qName, Attributes attributes) throws SAXException
	{
		int				stack_size = m_stack.size();
		DmsEntry		entry      = new DmsEntry(namespace, qName, localName, m_basicReader.m_xml_lang, m_locator);
		int 			lth        = attributes.getLength();
		EntityInstance	parent   = null;
		int				i, j;
		int 			dummyNode  = 0;
		int				sawAttributes = 0;
		DmsEntry		entry1;
		DmsEntry		entry2;


		if (stack_size == 0) {
			entry1 = null;
		} else {
			addContent();

			entry1     = (DmsEntry) m_stack.elementAt(stack_size-1);
			if (entry1.m_virtual == 2) {
				// Create a contains edge
				entry1 = new DmsEntry(null,null,"contain", m_basicReader.m_xml_lang, m_locator);
				entry1.m_virtual = 1;
				m_stack.add(entry1);
				++stack_size;
		}	}

		if (f_debug_parse) {

			for (i = stack_size; i > 0; --i) {
				System.out.print("  ");
			}
			if (lth == 0) {
				System.out.println('<' + "[" + namespace + "]" + qName + '>');
			} else {
				System.out.println('<' + "[" + namespace + "]" + qName);
	
				for (j = 0; j < lth; ++j) {
					for (i = stack_size; i >= 0; --i) {
						System.out.print("  ");
					}
					System.out.println('[' + attributes.getURI(j) + ':' + attributes.getLocalName(j) + '=' + attributes.getValue(j) + ']');
				}
				for (i = stack_size; i > 0; --i) {
					System.out.print("  ");
				}
				System.out.println('>');
		}	}


		m_stack.add(entry);
		if (stack_size == 0) {
			if (!BasicReader.isRdfNamespace(namespace) || !localName.equals("RDF")) {
				throw new SAXException("Not RDF document");
			}
			return;
		}

		String			entityId    = null;
		String			aUri, aLocalName;
		String			aValue;
		EntityInstance  e;
	

		// Handle xml attributes

		for (j = 0; j < lth; ++j) {
			aUri       = attributes.getURI(j);
			if (BasicReader.isXmlNamespace(aUri)) {
				aLocalName = attributes.getLocalName(j);
				if (aLocalName.equals("lang")) {
					BasicReader.m_xml_lang = attributes.getValue(j);
					++sawAttributes;
					continue;
				}
				if (aLocalName.equals("base")) {
					aValue = attributes.getValue(j);
					if (aValue != null) {
						entry.m_xml_base = m_xml_base;
						m_xml_base = attributes.getValue(j);
					}
					++sawAttributes;
		}	}	}

		// See if it has about attribute
		// See if it has resource attribute
		// See if it has parse type attribute
		for (j = 0; j < lth; ++j) {
			aUri       = attributes.getURI(j);
			if (BasicReader.isRdfNamespace(aUri)) {
				aLocalName = attributes.getLocalName(j);
				for (i = f_rdf_special.length; --i >= 0; ) {
					if (aLocalName.equals(f_rdf_special[i])) {
						break;
				}	}
				switch (i) {
				case 5:	// ID
					if (entry1.m_e == null) {
						// This is a nodeId of a new node */
						entityId = "#" + attributes.getValue(j);
						break;
					}
					System.out.println("We don't handle reification");
					break;
				case 0:	// about
				{
					/* This is the nodeId of this new node */
					entityId    = attributes.getValue(j);
					break;
				}
				case 1: // nodeId
				{
					dummyNode = 1;
					if (entry1.m_e == null) {
						/* This is a nodeId of a new node */
						entityId = prefixURL(attributes.getValue(j));
						break;
					}
				}
				case 2:	// resource
				{
					/* The currently being described edge points at the named resource */
					EntityInstance e1;

					aValue = prefixURL(attributes.getValue(j));
			

					parent = entry1.m_e;
					if (parent == null) {
						throw new SAXException("Resource associated with a node - not an edge");
					}
					if (BasicReader.isRdfType(namespace, localName)) {
						m_basicReader.setType(parent, aValue);
						return;
					}
					e = m_basicReader.getEntity(aUri, "Description", aValue, dummyNode, entry.m_line, entry.m_column);
					m_basicReader.makeEdge(namespace, localName, parent, e);
					return;
				}
				case 3:	// parseType
				{
					aValue = attributes.getValue(j);
					if (aValue.equals("Resource")) {
						// This property behaves as a property with a dummy node below it */
						e      = entry1.m_e;
						entry  = new DmsEntry(null, null, null, BasicReader.m_xml_lang, m_locator);
						entry.m_e = m_basicReader.getEntity(null, null, null, 1, entry.m_line, entry.m_column);
						entry.m_virtual = 1;
						m_basicReader.makeEdge(namespace, localName, e, entry.m_e);
						m_stack.add(entry);
						return;
					}
					if (aValue.equals("Collection")) {
						e      = entry1.m_e;
						entry  = new DmsEntry(null, null, null, BasicReader.m_xml_lang, m_locator);
						entry.m_e = m_basicReader.getEntity(null, null, "Collection", 2, entry.m_line, entry.m_column);
						entry.m_virtual = 2;
						m_basicReader.makeEdge(namespace, localName, e, entry.m_e);
						m_stack.add(entry);
						return;
						
					}
					continue;
				}
				case 4: // datatype
				{
					m_datatype = attributes.getValue(j);
					break;
				}
				default:
					continue;
				}
				++sawAttributes;
			} 
		}

		entityId = prefixURL(entityId);

		String			edgeType = null;
		String			edgeURI  = null;

		if (stack_size == 1) {
			parent   = null;
		} else {
			parent  = entry1.m_e;

			if (parent != null) {
				// We've pushed an edge onto the stack
				if (sawAttributes < lth) {
					entry1 = new DmsEntry(null,null, null, BasicReader.m_xml_lang, m_locator);
					entry1.m_e = e = m_basicReader.getEntity(null, null, null, 1, entry.m_line, entry.m_column);
					entry1.m_virtual = 1;
					m_stack.add(entry1);
					m_basicReader.makeEdge(namespace, localName, parent, e);
					for (j = 0; j < lth; ++j) {
						aUri       = attributes.getURI(j);
						aLocalName = attributes.getLocalName(j);
						if (BasicReader.isRdfNamespace(aUri)) {
							for (i = f_rdf_special.length; --i >= 0; ) {
								if (aLocalName.equals(f_rdf_special[i])) {
									break;
							}	}
							if (0 <= i) {
								continue;
							}
						} else if (BasicReader.isXmlNamespace(aUri)) {
							if (aLocalName.equals("lang")) {
								continue;
							}
							if (aLocalName.equals("base")) {
								continue;
						}	}
						m_basicReader.addLiteral(e, aUri, aLocalName, attributes.getValue(j), null, "path", entry.m_line, entry.m_column);
				}	}	
				return;
			}
			// Last was an edge
			edgeType = entry1.m_localName;
			edgeURI  = entry1.m_namespace;
			entry2   = (DmsEntry) m_stack.elementAt(stack_size-2);
			parent   = entry2.m_e;

			if (BasicReader.isRdfType(edgeURI, edgeType)) {
				if (entityId != null) {
					if (parent != null) {
						m_basicReader.setType(parent, entityId);
						return;
		}	}	}	}

		entry.m_e = e = m_basicReader.getEntity(namespace, localName, entityId, dummyNode, entry.m_line, entry.m_column);

		for (j = 0; j < lth; ++j) {
			aUri       = attributes.getURI(j);
			aLocalName = attributes.getLocalName(j);
			if (BasicReader.isRdfNamespace(aUri)) {
				for (i = f_rdf_special.length; --i >= 0; ) {
					if (aLocalName.equals(f_rdf_special[i])) {
						break;
				}	}
				if (i >= 0) {
					continue;
				}
			} else if (BasicReader.isXmlNamespace(aUri)) {
				if (aLocalName.equals("lang")) {
					continue;
				}
				if (aLocalName.equals("base")) {
					continue;
			}	}
			aValue = attributes.getValue(j);
			m_basicReader.addLiteral(e, aUri, aLocalName, aValue, null, "path", entry.m_line, entry.m_column);
		}
		if (parent != null) {
			m_basicReader.makeEdge(edgeURI, edgeType, parent, e);
		}
	}
 
	public void
    endElement(String namespace, String localName, String qName) throws SAXException
    {
		int			stack_size = m_stack.size();
		String 		qName1;
		DmsEntry	entry;


		if (f_debug_parse) {
			int 	i;
	
			for (i = stack_size; --i > 0;) {
				System.out.print("  ");
			}
			System.out.println("</" + qName + ">");
		}

		if (stack_size == 0) {
			throw new SAXException("Stack underflow on " + qName);
		}

		addContent();

		for (;;) {
			--stack_size;
			entry = (DmsEntry) m_stack.remove(stack_size);
			if (entry.m_virtual == 0) {
				break;
		}	}
		qName1 = entry.m_qName;
		if (!qName.equals(qName1)) {
			throw new SAXException("End tag for " + qName + " but start tag for " + qName1);
		}

		m_basicReader.m_xml_lang = entry.m_xml_lang;
		if (entry.m_xml_base != null) {
			m_xml_base  = entry.m_xml_base;
		}
		m_datatype = null;
	}
 
	public void
	fail()
	{
		return;
	}

	public void
	endDocument()
	{
		int i;

		i = m_stack.size();
		if (i != 0) {
			System.out.println("XML Stack left " + i + " items");
			while (0 <= --i) {
				System.out.println(m_stack.elementAt(i));
		}	}
		m_basicReader.endDocument();
	}
};
