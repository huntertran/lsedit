package dms;

import java.util.Vector;
import java.awt.Color;

import lsedit.SpecialPath;

import lsedit.EntityCache;
import lsedit.Ta;
import lsedit.LandscapeObject;
import lsedit.EntityClass;
import lsedit.RelationClass;
import lsedit.EntityInstance;
import lsedit.RelationInstance;
import lsedit.ResultBox;
import lsedit.SpecialPath;

import org.xml.sax.SAXException;

public abstract class BasicReader {

	final static boolean	f_debug_lsedit = false;
	
	public	  static int		m_li;
	public	  static Vector		m_namespaces = new Vector();

	protected static Ta				m_diagram   = null;
	protected static ResultBox		m_resultBox = null;
	protected static int			m_cnt       = 0;
	protected static int			m_literal_cnt = 0;
	protected static String			m_rdf       = null;	// URL currently reading
	protected static String			m_xml_lang  = null;
	protected static int 			m_contains_order = -1;
	protected static RelationClass	m_contains = null;

	static final String[]		f_namespaces = {
		"http://www.w3.org/XML/1998/namespace",
   		"http://www.w3.org/1999/02/22-rdf-syntax-ns#",
 		"http://www.w3.org/2008/content#",
   		"http://www.w3.org/2003/12/exif/ns#",
 		"http://purl.org/dc/elements/1.1/",
   		"http://purl.org/dc/terms/",
        "http://purl.org/dc/dcmitype/",
   		"http://www.openannotation.org/ns/",
   		"http://dms.stanford.edu/ns/",
   		"http://xmlns.com/foaf/0.1/",
   		"http://www.openarchives.org/ore/terms/",
		"http://example.org/stuff/1.0/"
	};

	static final String[]		f_prefixes = {
		"xml",
   		"rdf",
		"cnt",
		"exif",
 		"dc",
   		"dcterms",
		"dcmitype",
		"oac",
   		"dms",
   		"foaf",
   		"ore",
		"ex"
	};

	static final String[]		f_nests = {
		"rdf:first",
		"rdf:rest",
		"ore:aggregates",
		"dcterms:isPartOf",
		"ore:isDescribedBy",
		"oac:hasBody",
		"oac:hasTarget",
		"dms:option",
		"dms:forCanvas",
		"literal",
		"ore:describes",
		"contain",
		"path"
	};

	static final boolean[]		f_nests_reverse = {
		true,	// rdf:first
		false,	// rdf:rest
		false,	// ore:aggregates
		true,	// dcterms:isPartOf
		false,	// ore:isDescribedBy
		false,	// oac:hasBody
		false,	// oac:hasTarget
		false,	// dms:option
		false,	// dms:forCanvas
		false,	// literal
		false,	// ore:describes
		false,	// contain
		false	// path
	};

	static final String[]		f_inherits = {
		"cnt:ContentAsText",		// 0
		"rdf:List",					// 1
		"oac:Annotation",			// 2
		"oac:SvgConstraint",		// 3
		"dcmitype:Text",			// 4
		"dcmitype:Image",			// 5
		"ore:Aggregation",			// 6
		"dms:AnnotationList",		// 7
		"dms:TextAnnotationList",	// 8		
		"dms:ImageAnnotationList",	// 9
		"dms:Sequence",				// 10
		"dms:Manifest",				// 11
		"dms:TextAnnotation",		// 12
		"dms:ImageAnnotation",		// 13
		"dms:ImageBody",			// 14
		"dms:InitialBody",			// 15
		"dms:RubricBody"			// 16
	};

	static final int[]			f_inherits_from = {
		-2,	// 0  cnt:ContentAsText
		-2,	// 1  rdf:List
		-2, // 2  oac:Annotation
		0,  // 3  oac:SvgConstraint       inherits from cnt:ContentAsText
		0,	// 4  dcmitype:Text           inherits from cnt:ContentAsText
		-2, // 5  dcmitype:Image
		1,	// 6  ore:Aggregation         inherits from rdf:List
		6,	// 7  dms:AnnotationList      inherits from ore:Aggregation
		7,	// 8  dms:TextAnnotationList  inherits from dms:AnnotationList
		7,  // 9  dms:ImageAnnotationList inherits from dms:AnnotationList
		6,	// 10 dms:Sequence            inherits from ore:Aggregation
		6,	// 11 dms:Manifest            inherits from ore:Aggregation
		2,	// 12 dms:TextAnnotation      inherits from oac:Annotation
		2,	// 13 dms:ImageAnnotation     inherits from oac:Annotation
		5,	// 14 dms:ImageBody           inherits from dcmitype:Image
		4,	// 15 dms:InitialBody         inherits from dcmitype:Image
		4	// 16 dms:RubricBody          inherits from dcmitype:Text
	};

	protected static boolean
	isXmlNamespace(String uri)
	{
		return (uri != null && uri.equals(f_namespaces[0]));
	}

	protected static boolean
	isRdfNamespace(String uri)
	{
		return (uri != null && uri.equals(f_namespaces[1]));
	}

	protected static boolean
	isRdfType(String namespace, String localName)
	{
		return (namespace != null && localName != null && isRdfNamespace(namespace) && localName.equals("type"));
	}

	protected static void
	setTitle(String text)
	{
		m_resultBox.addResultTitle(text);
		System.out.println(text);
	}
	
	protected static void
	report(String text)
	{
		m_resultBox.addText(text);
		System.out.println(text);
	}
	
	protected static void
	reportException(Throwable e)
	{
		String msg1;
			
		for (;e != null; e = e.getCause()) {
			report(e.getClass().toString());
			msg1 = e.getMessage();
			if (msg1 == null) {
				msg1 = e.toString();
			}
			report(msg1);
		}
	}

	protected static void
	done(String text)
	{
		m_resultBox.done(text);
		System.out.println(text);
	}
	
	private	static final String[] g_root_attribute_name = {
		"opencolor",
		"edgemode",
		"topclients",
		"wantclients",
		"wantcardinals",
		"wantoutcardinals",
		"arrow:iconrule",
		"option:showspantype",
		"option:showclasstype"
	};
	
	private static final String[] g_root_attribute_value = {
		"(255 255 255)",
		"3",
		"true",
		"true",
		"false",
		"false",
		"3",
		"true",
		"true"
	};
	
	protected static void
	addRootAttributes()
	{
		Ta				diagram = m_diagram;
		EntityInstance	root    = diagram.getRootInstance();		
		EntityClass		ec, superclass;
		int				i, j;
	
		if (root.getParentClass() == null) {
			root.setParentClass(diagram.getEntityBaseClass());
		}
		root.setLabelColor(Color.BLACK);
		root.addAttribute("label", m_rdf);
		for (i = 0; i < g_root_attribute_name.length; ++i) {
			root.addAttribute(g_root_attribute_name[i], g_root_attribute_value[i]);
		}
		m_contains = m_diagram.addRelationClass("contain");

		for (i = 0; i < f_inherits.length; ++i) {
			ec = getEntityClass(f_inherits[i], -1);
			j = f_inherits_from[i];
			if (0 <= j) {
				superclass = getEntityClass(f_inherits[j], -1);
				ec.addParentClass(superclass);
		}	}
	}


	protected static void
	start()
	{
		BasicReader.m_li = 0;
		m_xml_lang = null;
		m_contains_order = -1;
		addRootAttributes();
	}

	protected static String 
	makeLabel(String namespace, String localName)
	{
		if (localName == null) {
			return null;
		}
		if (namespace == null) {
			return localName;
		}
		String 	prefix = namespace;
		int		i;

		for (i = f_namespaces.length; ; ) {
			if (--i < 0) {
				prefix = namespace;
				break;
			}
			if (namespace.equals(f_namespaces[i])) {
				prefix = f_prefixes[i] + ':';
				break;
		}	}
		return prefix + localName;
	}

	protected static void
	addAttribute(LandscapeObject object, String id, String value)
	{
		if (object == null) {
			System.out.println("Object null");
		}
		object.addAttribute(id, LandscapeObject.qt(value));
	}
		
	protected static void
	addSpecial(LandscapeObject o, int line, int column)
	{
		if (m_xml_lang != null) {
			addAttribute(o, "lang", m_xml_lang);
		}
		if (m_rdf != null) {
			addAttribute(o, "rdf", m_rdf);
		}
		if (line >= 0) {
			addAttribute(o, "line", "" + line);
		}
		if (column >= 0) {
			addAttribute(o, "column", "" + column);
	}	}

	
	protected static RelationInstance
	addEdge(RelationClass rc, EntityInstance from, EntityInstance to) throws SAXException
	{
		RelationInstance r;

		if (rc == null) {
			throw new SAXException("addEdge relation class null");
		}
		if (from == null) {
			throw new SAXException("addEdge " + rc + " from null");
		}
		if (to == null) {
			throw new SAXException("addEdge " + rc + " to null");
		}
		r = m_diagram.addEdge(rc, from, to);
		addSpecial(r, -1, -1);
		if (f_debug_lsedit) {
			System.out.println("Edge " + r);
		}
		return r;
	}
	
	protected static RelationInstance
	makeEdge(String namespace, String localName, EntityInstance from, EntityInstance to) throws SAXException
	{
		Ta					diagram = m_diagram;
		RelationClass		rc;
		RelationInstance	r;
		String				type, type1, color;


		if (isRdfNamespace(namespace) && localName.equals("li")) {
			localName = "_" + (++m_li);
		}
		type = makeLabel(namespace, localName);

		if (type == null) {
			type = "no-name";
		}

		// type1 = from.getClassLabel() + "->" + type + "->" + to.getClassLabel();
		type1 = type;

		rc = diagram.getRelationClass(type1);

		if (rc == null) {
			rc = diagram.addRelationClass(type1);
			addAttribute(rc,"type", type);
			if (type.equals("rdf:rest")) {
				color = "(0 204 0)";
			} else if (type.equals("rdf:first")) {
				color = "(255 0 0)";
			} else if (type.equals("literal")) {
				color = "(51 51 255)";
			} else if (type.equals("ore:aggregates")) {
				color = "(255 204 0)";
			} else {
				color = null;
			}
			if (color != null) {
				rc.addAttribute("color", color);
			}
		}
		r = addEdge(rc, from, to);
		if (namespace != null) {
			addAttribute(r, "namespace", namespace);
		}
		return r;
	}

	protected static EntityClass
	getEntityClass(String name, int style)
	{
		EntityClass	ec;

		ec = m_diagram.getEntityClass(name);
		if (ec == null) {
			String 		color          = null;
			int			i;

			ec = m_diagram.addEntityClass(name);

			// ec.setImage(EntityClass.ENTITY_IMAGE_CLASS | EntityClass.ENTITY_IMAGE_SPAN);

			if (style >= 0) {
				ec.setStyle(style);
				switch (style) {
				case 5:	// literal
					color = "(255 204 255)";
					break;
				case 6:	// dummy node
				case 8:	// collection or list
					break;
				}
			} else if (name.equals("ore:ResourceMap")) {
				ec.setImage(EntityClass.ENTITY_IMAGE_NAV_NEXT);
				color = "(  0 255  51)";
			} else if (name.equals("ore:Aggregation")) {
				color = "( 51 255 255)";
			} else if (name.equals("dcterms:Agent")) {
				color = "(255 102 102)";
			} else if (name.equals("dms:Canvas")) {
				color = "(255 255 255)";
			} else if (name.equals("dms:TextAnnotationList")) {
				color = "(250 120 224)";
			} else if (name.equals("dms:ImageAnnotationList")) {
				color = "( 26 192 252)";
			} else if (name.equals("dms:Image")) {
				color = "( 66 186 208)";
			} else if (name.equals("dms:Sequence")) {
				color = "(149 137 158)";
			} else if (name.equals("dms:Manifest")) {
				color = "(227 164  18)";
			} else if (name.equals("dms:AnnotationList")) {
				color = "(255 255 204)";
			} else {
				color = "(255 255 51)";
			}
			if (color != null) {
				ec.addAttribute("color", color);
			}
			ec.addAttribute("labelcolor", "(0 0 0)");
		}
		return ec;
	}

	protected static void
    sawNamespace(String namespace)
	{
		String	value;
		int		i;
	
		for (i = m_namespaces.size(); --i >= 0; ) {
			if (namespace.equals((String) m_namespaces.elementAt(i))) {
				return;
		}	}
		m_namespaces.add(namespace);
	}

	protected static void
	setType(EntityInstance e, String namespace, String localName, String value)
	{
		EntityClass	oldEc   = e.getEntityClass();
		String		newType = makeLabel(namespace, localName);
		EntityClass	newEc;

		if (oldEc != m_diagram.m_entityBaseClass) {
			String 	oldType = oldEc.getId();
			boolean	ok      = false;
			int		i, j;

			for (i = f_inherits.length; 0 <= --i; ) {
				if (newType.equals(f_inherits[i])) {
					break;
			}	}
			for (j = f_inherits.length; 0 <= --j; ) {
				if (oldType.equals(f_inherits[j])) {
					break;
			}	}
			
			if (i > j) {
				while ((i = f_inherits_from[i]) > j);
				if (i == j) {
					ok = true;
				}
			} else if (j > i) {
				while ((j = f_inherits_from[j]) > i);
				if (i == j) {
					return;
			}	}
			if (!ok) {
				System.out.println("Types " + oldType + " & " + newType + " are incompatible");
				return;
		}	}
		newEc = getEntityClass(newType, -1);
		e.setParentClass(newEc);
		if (value != null) {
			addAttribute(e, "rdf:datatype", value);
		}
		if (f_debug_lsedit) {
			System.out.println("Entitytype " + e + " = " + newEc);
		}
		if (newType.equals("ore:ResourceMap")) {
			String	label = e.getEntityLabel();
			if (label.startsWith("http:") || label.startsWith("https:") || label.startsWith("//")) {
				if (!label.equals(m_rdf)) {
					e.putNavNext("dms:" + label);
	}	}	}	}


	protected static void
	setType(EntityInstance e, String url)
	{
		String	namespace;
		String	localName;
		int		i;

		for (i = f_namespaces.length; ; ) {
			if (--i < 0) {
				namespace = null;
				localName = url;
				break;
			}
			if (url.startsWith(f_namespaces[i])) {
				namespace = f_namespaces[i];
				localName = url.substring(namespace.length());
				break;
		}	}
		setType(e, namespace, localName, null);
	}

	protected static EntityInstance
	getEntity(String namespace, String localName, String id, int dummyNode, int line, int column)
	{
		String			name  = null;
		String			label = null;
		EntityInstance 	e     = null;
		EntityClass		ec;
		boolean			first = false;

		if (id != null) {
			name = id;
		} else if (localName != null) {
			if (namespace == null) {
				name = localName;
			} else if (!isRdfNamespace(namespace) || !localName.equals("Description")) {
				name = makeLabel(namespace, localName);
		}	}
		if (name == null) {
			name = "_Node" + (++m_cnt);
		}
		e = m_diagram.getCache(name);
		if (e == null) {
			first = true;
			switch (dummyNode) {
			case 0:
				e = m_diagram.newCachedEntity(m_diagram.m_entityBaseClass, name); 
				break;
			case 1:
				ec = getEntityClass("dummy", 6);
				e = m_diagram.newCachedEntity(ec, name); 
				break;
			case 2:
				ec = getEntityClass("Collection", 8);
				e = m_diagram.newCachedEntity(ec, name); 
				e.setLabel("Collection");
				break;
			}

			addSpecial(e, line, column);
		}

		if (localName != null) {
			if (namespace == null || !isRdfNamespace(namespace) || !localName.equals("Description")) {
				setType(e, namespace, localName, null);
		}	}

		if (namespace != null) {
			sawNamespace(namespace);
		}
		if (f_debug_lsedit) {
			System.out.println("Entity " + e);
		}
		return e;
	}

	protected void
	addLiteral(EntityInstance e, String namespace, String localName, String value, String datatype, String edgeType, int line, int column) throws SAXException
	{
		String			type    = makeLabel(namespace, localName);
		int				cnt     = e.getAttributesLength();
		String			suffix  = "[" + cnt + "]";
		String			label   = type + "[" + cnt + "]";

		if (datatype != null) {
			label += datatype;
		}
		e.putAttribute(label, LandscapeObject.qt(value));

		
/*
		EntityClass		ec      = getEntityClass(type, 5);
		EntityInstance	literal;

		literal = m_diagram.newCachedEntity(ec, "_Literal" + (++m_literal_cnt));

		literal.addAttribute("label", value);
		if (datatype != null) {
			literal.addAttribute("datatype", datatype);
		}	
		addSpecial(literal, line, column);
		makeEdge(null, edgeType, e, literal);
*/
	}

	protected void
	setContainer(EntityInstance container, EntityInstance e)
	{
		Vector				dstRelList = e.getDstRelList();
		RelationInstance	ri;
		RelationClass		rc;
		int					i;

		if (dstRelList != null) {
			for (i = dstRelList.size(); --i >= 0; ) {
				ri = (RelationInstance) dstRelList.elementAt(i);
				rc = ri.getRelationClass();
				if (rc == m_contains) {
					ri.removeEdge();
					break;
		}	}	}
		ri = m_diagram.addEdge(m_contains, container, e);
System.out.println("" + ri);
	}

	protected void
	endDocument()
	{
		Ta				diagram = m_diagram;
		RelationClass 	rest	= diagram.getRelationClass("rdf:rest");
		RelationClass	rc;
		int				i;

		for (i = 0; i < f_nests.length; ++i) {
			rc = diagram.getRelationClass(f_nests[i]);
			if (rc != null) {
				rc.addAttribute("class_iscontains", "" + (++m_contains_order)); 
				if (f_nests_reverse[i]) {
					rc.setShown(RelationClass.DIRECTION_REVERSED, false);
		}	}	}

/*
		if (rest == null) {
			return;
		}

		RelationClass		first       = diagram.getRelationClass("rdf:first");
		EntityCache			entityCache = diagram.getEntityCache();
		EntityClass			listec      = null;
		EntityInstance		parent, next, src;
		EntityClass			ec;
		RelationInstance	ri, span;
		Vector				srcRelList, dstRelList;
		
nodes:	for (parent = entityCache.getFirst(); parent != null; parent = entityCache.getNext()) {
			// For each entity which is not a list
System.out.println("Considering " + parent);
			ec = parent.getEntityClass();
			if (ec == listec) {
System.out.println("Is List");
				continue;
			}
			dstRelList = parent.getDstRelList();
			if (dstRelList != null) {
				for (i = dstRelList.size(); --i >= 0; ) {
					ri = (RelationInstance) dstRelList.elementAt(i);
					rc = ri.getRelationClass();
					if (rc == rest) {
						// This node has prior members in its list
System.out.println("Prior nodes in list");
						continue nodes;
			}	}	}

list:		for (next = parent;;) {
				srcRelList = next.getSrcRelList();
				if (srcRelList == null) {
System.out.println("No src list");
					break;
				}
				next = null;
				for (i = srcRelList.size(); ; ) {
					if (--i < 0) {
System.out.println("No list");
						break list;
					}
					ri = (RelationInstance) srcRelList.elementAt(i);
					rc = ri.getRelationClass();
					if (rc == rest) {
						next = ri.getDst();
						break;
				}	}
				// next is entity addressed by rest edge
				
				// setContainer(parent, next);

				if (first != null) {
					srcRelList = next.getSrcRelList();
					if (srcRelList != null) {
						for (i = srcRelList.size(); --i >= 0; ) {
							ri = (RelationInstance) srcRelList.elementAt(i);
							rc = ri.getRelationClass();
							if (rc == first) {
								setContainer(parent, ri.getDst());
		}	}	}	}	}	}
*/
		diagram.attachBaseClasses();
	}

	public abstract String parseSpecialPath(Ta diagram, ResultBox resultBox, String path);
}
