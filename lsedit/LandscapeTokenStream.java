package lsedit;

import java.util.Vector;
import java.io.Reader;
import java.io.IOException;


public class LandscapeTokenStream extends TA_StreamTokenizer {


	public final static int SCHEME_TUPLE		= 0;
	public final static int SCHEME_ATTRIBUTE	= 1;
	public final static int FACT_TUPLE			= 2;
	public final static int FACT_ATTRIBUTE		= 3;

	public final static int ERROR				= -1;
	public final static int NONE				= -2; 
	public final static int EOF					= 99;

	protected final static String SCHEME_ID    = "SCHEME";
	protected final static String FACT_ID	   = "FACT";
	protected final static String TUPLE_ID	   = "TUPLE";
	protected final static String ATTRIBUTE_ID = "ATTRIBUTE";

	protected EntityCache	m_entityCache;	// A secondary reference to the main entity cache

	protected boolean m_fatalError = false;

	protected String m_includeFile;

	// push backed tokens 

	protected String m_token1, m_token2; 
	protected int	 m_ttype3;
	
	// triple values
	
	public String	m_verb;
	public String	m_object;
	public String	m_subject; 
	public int		m_relations;
	
	// Start line no
	
	protected int	m_startLineno = -1;

	static Vector	m_tempAttributes = new Vector();

	protected void skipRecord() throws IOException 
	{
		int ttype;

		do {
			ttype = nextToken();
		} while (ttype != TT_EOF && ttype != '}');
	}

	protected void parseAttributes(LandscapeObject target) throws IOException 
	{
		Vector				tempAttributes = m_tempAttributes;
		int					ttype;
		String				attributeId;
		String				value;
		Attribute			newAttr;

		tempAttributes.clear();

		for (;;) {
			ttype = nextToken();

			switch (ttype) {
			case '}':
				target.addAttributes(tempAttributes);
				return;
			case TT_WORD:
				break;
			default:
				error("Expecting attribute id for " + target);
				target.addAttributes(tempAttributes);
				return;
			}
			attributeId = m_sval;

			ttype = nextToken();
			if (ttype != '=') {
				// Attribute declaration with no value 
				pushBack(ttype);
				value = null; 
			} else {
				value = nextAVI();
			}
			target.addAttribute(attributeId, value, tempAttributes);
		}
	}

	// --------------

	// Public methods

	// --------------

	public LandscapeTokenStream(Reader reader, String filename, EntityCache entityCache) 
	{
		super(reader, filename);
		m_entityCache = entityCache;
	}

	public int getStartLineno()
	{
		return m_startLineno;
	}
	
	public void errorNS(String msg) 
	{
/*
		private static int first_time = 1;

		if (first_time == 1) {
				first_time = 0;
				System.out.println("errorNS");
				java.lang.Thread.dumpStack();
				System.out.println("-----");
		}
*/
		MsgOut.println("*** Error (" + filename() + ":" + lineno() + "): " + msg);
	}

	public void error(String msg) 
	{
		MsgOut.println("*** Error (" + filename() + ":" + lineno() + "): " + msg + ". Found " + m_sval);
	}

	public void warning(String msg) 
	{
		MsgOut.println(">>> Warning (" + filename() + ":" + lineno() + "): " + msg);
	}

	public int nextSection() throws IOException 
	{
		// Called when a section id is expected
		// Returns section id if new section found, or 
		// EOF when at stream end.

		int ttype;

		String graph, type;

		if (m_fatalError) {
			return EOF;
		}

		if (m_token1 != null) {
			if (m_ttype3 == ':') {
				graph  = m_token1; 
				type   = m_token2;
				m_token1 = null;
				m_token2 = null;
			} else {
				error("Expecting section header ':'"); 
				return ERROR; 
			}
		} else { 
			ttype = nextToken();
			switch (ttype) {
			case TT_EOF:
				return EOF;
			case TT_WORD:
				graph = m_sval;

				if (nextToken() != TT_WORD) { 
					error("Expecting section type id");
					return ERROR;
				}
				type = m_sval; 
				if (nextToken() != ':') {
					error("Expecting ':'");
					return ERROR;
				}
				break;
			default:
				error("Expecting graph id");
				return ERROR;
		}	} 

		if (graph.equals(SCHEME_ID)) {
			if (type.equals(TUPLE_ID)) {
				return SCHEME_TUPLE;
			}
			if (type.equals(ATTRIBUTE_ID)) {
				return SCHEME_ATTRIBUTE;
			}
			if (type.equals("END")) {
				return EOF;
			}
			error("Bad section type");
			return ERROR; 
		}

		if (graph.equals(FACT_ID)) {
			if (type.equals(TUPLE_ID)) {
				return FACT_TUPLE;
			}
			if (type.equals(ATTRIBUTE_ID)) {
				return FACT_ATTRIBUTE;
			}
			error("Bad section type");
			return ERROR;
		}

		error("Bad section id");
		return ERROR;
	}

	public String getIncludeFile() 
	{ 
		return m_includeFile; 
	}

	public boolean nextSchemaTriple() throws IOException 
	{
		if (!m_fatalError) {

			m_startLineno = lineno();
			
			switch (nextToken()) {
			case TT_EOF:
				// End of section 
				return false;
			case TT_WORD: 
				break;
			default:
				error("Expecting word verb token");
				m_fatalError = true;
				return false;
			}

			m_relations = 0;
			m_verb      = m_sval;

			switch (nextToken()) {
			case TT_WORD:
				m_object = m_sval;
				break;
			case '(':
				m_relations |= 1;
				if (nextToken() == TT_WORD) {
					m_object = m_sval;
					if (nextToken() == ')') {
						break;
				}	}
			default:
				error("Expecting word object token"); 
				m_fatalError = true;
				return false;
			}

			switch (nextToken()) {
			case ':':
				m_token1 = m_verb;
				m_token2 = m_object; 
				m_ttype3 = ':'; 
				return false; 
			case TT_WORD:
				m_subject = m_sval;
				return true;
			case '(':
				m_relations |= 2;
				if (nextToken() == TT_WORD) {
					m_subject = m_sval;
					if (nextToken() == ')') {
						return true;
				}	}
			default:
				error("Expecting word subject token");
				m_fatalError = true;
			}
			return true;
		}
		return false;
	}

	public boolean nextFactTriple() throws IOException 
	{
		if (!m_fatalError) {

			m_startLineno = lineno();

			int ttype = nextToken();

			switch (ttype) {
			case TT_EOF:
				// End of section 
				return false;
			case TT_WORD: 
				m_verb = m_sval;

				if (nextToken() != TT_WORD) {
					error("Expecting word token"); 
					break;
				}

				m_object = m_sval;

				ttype = nextToken();

				if (ttype == ':') {
					m_token1 = m_verb;
					m_token2 = m_object; 
					m_ttype3 = ttype; 
					return false; 
				} 

				if (ttype != TT_WORD) {
					error("Error in tuple parse");
					break;
				}

				m_subject = m_sval;

				return true;
			default:		
				error("Expecting word token");
			}
			m_fatalError = true;
		}
		return false;
	}

	public void processSchemeAttributes(Ta ta) throws IOException 
	{

		int				ttype;
		LandscapeObject	target = null;
		String			token1;
		String			token2;
		String			token3;
		String			msg;

		// 
		//  * id "{" {attribute} "}" *
		// 

		while (!m_fatalError) {
		
			m_startLineno = lineno();

			ttype = nextToken();
			msg   = null;

			switch (ttype) {
			case TT_EOF:
				// End of section
				return; 
			case '(':
				// Relation or relation class 
				ttype = nextToken();
				if (ttype != TT_WORD) {
					msg = "Expected ( <relation class name>";
					break;
				} 
				token1 = m_sval;
				ttype  = nextToken();
				if (ttype != ')') {
					msg = "Expected ( " + token1 + "<)>";
					break;
				}
				ttype = nextToken();
				if (ttype != '{') {
					msg = "Expected (...) <{>";
					break;
				}

				if (Ta.m_strict_TA) {
					target = ta.getRelationClass(token1);
					if (target == null) {
						msg = "Strict TA: Undeclared relation class '" + token1 + "' has attributes";
					}
				} else {
					target = ta.addRelationClass(token1);
				}
				break;
			case TT_WORD:
				// Entity class or entity 
				token1 = m_sval;
				ttype  = nextToken();
				if (ttype != '{') {
					if (ttype != TT_WORD) {
						msg = "Expecting " + token1 + " <{>";
						break;
					}
					token2 = m_sval;
					ttype = nextToken();
					if (ttype != ':') {
						msg = "Expecting section header or id <{>";
						break;
					}
					m_token1 = token1; 
					m_token2 = token2;
					m_ttype3 = ttype;
					// End of section 
					return;
				}
				if (!Ta.m_strict_TA) {
					target = ta.addEntityClass(token1);
				} else {
					target = ta.getEntityClass(token1);
					if (target == null) {
						msg = "Strict TA: Undeclared entity class '" + token1 + "' has attributes";
				}	}
				break;
			default:
				msg = "Expecting object id";
			}
			if (msg != null) {
				errorNS(msg);
				skipRecord();
				continue;
			} 
			parseAttributes(target); 
	}	}

	public void processFactAttributes(Ta ta) throws IOException 
	{
		int				ttype;
		LandscapeObject	target = null;
		String			token1;
		String			token2;
		String			token3;
		String			msg;
		RelationClass	rc;
		EntityInstance	src, dst;

		// 
		//  * id "{" {attribute} "}" *
		// 

		MsgOut.vprint("\nFACT ATTRIBUTES : ");

		int n = 0;

		while (!m_fatalError) {

			n++; 

			if ((n % Ta.UPDATE_FREQ) == 0) {
				ta.showProgress("Attr Records: " + n);
			}

			m_startLineno = lineno();

			ttype = nextToken();
			msg   = null;

			switch (ttype) {
			case TT_EOF:
				// End of section
				return; 
			case '(':
			{
				// Relation or relation class 
				ttype = nextToken();
				if (ttype != TT_WORD) {
					msg = "Expected ( <class> src dst )";
					break;
				} 
				token1 = m_sval;
				ttype = nextToken();
				if (ttype != TT_WORD) {
					msg = "Expected (" + token1 + " <src> dst)" + " not " + ttype;
					break;
				}
				token2 = m_sval;
				ttype = nextToken();
				if (ttype != TT_WORD) {
					msg = "Expected (" + token1 + " " + token2 + " <dst>)" + " not " + ttype;
					break;
				}
				token3 = m_sval;
				ttype = nextToken();
				if (ttype != ')') {
					msg = "Expected (" + token1 + " " + token2 + " " + token3 + "<)>" + " not " + ttype;
					break;
				}
				ttype = nextToken();
				if (ttype != '{') {
					msg = "Expected (...) <{>" + " not " + ttype;
					break;
				}
				
				rc  = ta.getRelationClass(token1);
				if (rc == null && !Ta.m_strict_TA) {
					rc  = ta.addRelationClass(token1);
				}

				src = m_entityCache.get(token2);
				if (src == null && !Ta.m_strict_TA) {
					// Create it
					src = ta.newCachedEntity(ta.m_entityBaseClass, token2);
				}

				dst = m_entityCache.get(token3);
				if (dst == null && !Ta.m_strict_TA) {
					// Create it
					dst = ta.newCachedEntity(ta.m_entityBaseClass, token3);
				}

				if (rc == null || src == null || dst == null) {
					msg = "Strict TA: Relation " + "(" + token1 + " " + token2 + " " + token3 + ")";
					if (rc == null) {
						msg += " member of undeclared relation class '" + token1 + "'";
					}
					if (src == null) {
						msg += " has undeclared source entity '" + token2 + "'";
					} 
					if (dst == null) {
						msg += " has undeclared destination entity '" + token3 + "'";
					}
					break;
				}
				target = src.getRelationTo(rc, dst);
				if (target == null) {
					if (Ta.m_strict_TA) {
						msg = "Strict TA: Undeclared relation (" + token1 + " " + token2 + " " + token3 + ") has attributes";
						break;
					}
					ta.addEdge(rc, src, dst);
					target = src.getRelationTo(rc, dst);
				}
				break;
			}
			case TT_WORD:
				// Entity class or entity 
				token1 = m_sval;
				ttype  = nextToken();
				if (ttype != '{') {
					if (ttype != TT_WORD) {
						msg = "Expecting " + token1 + " <{>";
						break;
					}
					token2 = m_sval;
					ttype = nextToken();
					if (ttype != ':') {
						msg = "Expecting section header or id <{>";
						break;
					}
					m_token1 = token1; 
					m_token2 = token2;
					m_ttype3 = ttype;
					// End of section 
					return;
				}

				if (token1.equals(Ta.ROOT_ID)) {
					target = ta.getRootInstance();
				} else {
					target = m_entityCache.get(token1);
					if (target == null) {
						if (Ta.m_strict_TA) {
							msg = "Strict TA: Undeclared entity '" + token1 + "' has attributes";
							break;
						}
						// Create it
						target = ta.newCachedEntity(ta.m_entityBaseClass, token1);
					}
				}
				break;
			default:
				msg = "Expecting object id";
			}
			if (msg != null) {
				errorNS(msg);
				skipRecord();
				continue;
			} 
			parseAttributes(target); 
	}	} 
}

