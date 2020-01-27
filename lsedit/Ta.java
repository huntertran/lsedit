package lsedit;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.util.zip.ZipInputStream;
import java.util.zip.GZIPInputStream;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

/* This layer of software is merely concerned with representing the TA graph structures 
   Any access to update methods at this level will perform no signalling of changes
   These methods should only be used during the actual loading of an initial TA graph
 */

class NoFeedback implements TaFeedback 
{
	public void showProgress(String message) {}
	public void doFeedback(String message) {}
	public void showInfo(String message) {}
	public void error(String message) {}
	public void showCycle(RelationInstance ri) {}
	public void noContainRelation(String taPath) {}
	public void hasMultipleParents(RelationClass rc, EntityInstance e) {}
}

public class Ta extends JPanel implements TaFeedback
{
	public final static String INSTANCE_ID				= "$INSTANCE";	
	public final static String INHERIT_RELN				= "$INHERIT";
	public final static String CONTAIN_ID				= "contain"; 
	public final static String ROOT_ID                  = "$ROOT";

	public final static int	   UPDATE_FREQ              = 1000; 

	public static boolean		m_strict_TA			    = false;		// Set by -s option

	// Values

	protected TaFeedback			m_taFeedback;				// Listener to recieve progress feedback etc.
	protected static TaFeedback		m_noFeedback = null;		// Default TaFeedbackListener

	private   Diagram				m_diagram;					// The actual diagram instance else null
	protected EntityCache			m_entityCache;				// Cache of entities in the graph
	protected RelationClass			m_defaultContainsClass;		// Default contains class

	protected EntityInstance		m_rootInstance;				// The root instance of the diagram
	protected EntityInstance		m_drawRoot;					// The current entity being drawn

	protected RelationClass[]		m_containsClasses      = null;	// The classes that are used to form the spanning tree
	protected boolean				m_undoEnabled          = false;

	protected Hashtable				m_entityClasses	       = new Hashtable(50);	// Entity classes

	// The set of relationClasses
	protected Hashtable				m_relationClasses      = new Hashtable(50);	// Relation classes
	private   Vector				m_numToRel		       = new Vector(50);

	public    RelationClass			m_relationBaseClass; 
	public    EntityClass			m_entityBaseClass;

	protected RelationClass			m_defaultRelationClass = null;	// Class to create when building edges
	protected int					m_numRelationClasses   = 0;

	protected EntityClass			m_defaultEntityClass   = null;	// Class to create when building entities
	private   int					m_numEntityClasses     = 0; 

	private   Object				m_context;
	private	  String				m_zipEntry;

	protected String				m_resString            = null;

	protected int					m_progressCount;
	protected boolean				m_changedFlag          = false;
	
	protected int					m_numberRelations;

	private	  String				m_comments             = null;
	
	private	  Vector				m_views                = null;
	
	protected boolean				m_changed_spanning_edges = false;

	protected boolean				m_loaded_special       = false;

	public void setNoFeedback()
	{
		if (m_noFeedback == null) {
			m_noFeedback = new NoFeedback();
		}
		m_taFeedback = m_noFeedback;
	}
		
	public Ta(TaFeedback taFeedback) 
	{
		m_taFeedback   = taFeedback;
		if (taFeedback == null) {
			setNoFeedback();
		}
		m_entityCache  = new EntityCache();
		m_diagram      = getDiagram();
		m_rootInstance = null;

		// Must create this before create $ROOT
		// Need a navlink attribute on rootInstance

		m_entityBaseClass = new EntityClass(EntityClass.ENTITY_BASE_CLASS_ID, m_numEntityClasses++, this);							// $ENTITY
		m_entityBaseClass.setStyle(EntityClass.ENTITY_STYLE_3DBOX);
		m_entityBaseClass.setObjectColor(Color.blue);		// Fill color
		m_entityBaseClass.setLabelColor(Color.cyan);		// Label color

		m_entityClasses.put(EntityClass.ENTITY_BASE_CLASS_ID, m_entityBaseClass);

		m_relationBaseClass =	 new RelationClass(RelationClass.RELATION_BASE_CLASS_ID, m_numRelationClasses++, this);	// $RELATION
		m_relationBaseClass.setIOfactor((short) 0);		// ie. 0.5
		m_relationBaseClass.setStyle(Util.LINE_STYLE_NORMAL);
		m_relationBaseClass.setObjectColor(Color.black);

		m_relationClasses.put(RelationClass.RELATION_BASE_CLASS_ID, m_relationBaseClass);													// Add to hash table
		m_numToRel.addElement(m_relationBaseClass);

		RelationClass rc = new RelationClass(CONTAIN_ID, m_numRelationClasses++, this);								// contain
		rc.setCIndex(0);
		m_defaultContainsClass = rc;
		m_relationClasses.put(CONTAIN_ID, rc);																							// Add to hash table
		m_numToRel.addElement(rc);
	}

	// Overloaded by Diagram to return the actual diagram pointer

	protected Diagram getDiagram()
	{
		return(null);
	}
	
	public EntityClass getEntityBaseClass()
	{
		return m_entityBaseClass;
	}

	public RelationClass getRelationBaseClass()
	{
		return m_relationBaseClass;
	}
	
	public RelationClass[] getContainsClasses()
	{
		return m_containsClasses;
	}

	public RelationClass getPrimaryContainsClass()
	{
		if (m_containsClasses == null) {
			return null;
		}
		return m_containsClasses[0];
	}
	
	public RelationClass getDefaultContainsClass()
	{
		return m_defaultContainsClass;
	}
	
	public boolean getChangedFlag()
	{
		return m_changedFlag;
	}

	public boolean undoEnabled()
	{
		return m_undoEnabled;
	}

	public Vector getClassAndSubclasses(EntityClass ec)
	{
		return (ec.getClassAndSubclasses(m_entityClasses));
	}

	public EntityClass getEntityClass(String id)
	{
		return (EntityClass) m_entityClasses.get(id); 
	}

	public EntityInstance getRootInstance()
	{
		return m_rootInstance;
	}

	public EntityInstance getDrawRoot() 
	{
		return m_drawRoot; 
	}

	public Vector getClassAndSubclasses(RelationClass rc)
	{
		return rc.getClassAndSubclasses(m_relationClasses);
	}

	public RelationClass getRelationClass(String id)
	{
		return (RelationClass) m_relationClasses.get(id);
	}

	public int getMaxCIndex()
	{
		Enumeration		en;
		RelationClass	rc;
		int				cindex, cindex1;

		cindex = -1;
		for (en = enumRelationClasses(); en.hasMoreElements(); ) {
			rc = (RelationClass) en.nextElement();
			cindex1 = rc.getCIndex();
			if (cindex1 > cindex) {
				cindex = cindex1;
		}	}
		return cindex;
	}

	public boolean entityExists(String name) 
	{
		return (getCache(name) != null);
	}

	public EntityCache	getEntityCache()
	{
		return m_entityCache;
	}

	public EntityInstance getCache(String id)
	{
		return m_entityCache.get(id);
	} 

	public void removeCache(EntityInstance e)
	{
		m_entityCache.remove(e);
	}

	public void putCache(EntityInstance e)
	{
		m_entityCache.put(e);
	}

	public Enumeration enumRelationClasses() 
	{
		return m_relationClasses.elements(); 
	}

	public Enumeration enumRelationClassesInOrder() 
	{
		return OrderedHashTableEnumeration.elements(m_relationClasses); 
	}

	public int numRelationClasses() 
	{
		return m_numRelationClasses;
	}

	public Enumeration enumEntityClasses() 
	{
		return m_entityClasses.elements(); 
	}

	public Enumeration enumEntityClassesInOrder() 
	{
		return OrderedHashTableEnumeration.elements(m_entityClasses); 
	}

	public Enumeration enumEntityClassHierarchy(boolean hideEmpty)
	{
		return m_entityBaseClass.enumHierarchy(hideEmpty, m_entityClasses.size());
	}

	public Enumeration enumRelationClassHierarchy(boolean hideEmpty)
	{
		return m_relationBaseClass.enumHierarchy(hideEmpty, m_relationClasses.size());
	}

	public int numEntityClasses() 
	{
		return m_entityClasses.size();
	}

	public Object getContext() 
	{
		return m_context;
	}

	public void setContext(Object context) 
	{
		m_context = context;
	}

	public String getContextName() 
	{
		String ret;

		if (m_context instanceof File) {
			ret = Util.nameFromPath(((File) m_context).getPath());
		} else if (m_rootInstance == null) { 
			ret = null;
		} else {
			ret = m_rootInstance.getEntityLabel();
		}
		if (m_zipEntry != null) {
			ret += "[" + m_zipEntry + "]";
		}
		return ret;
	}

	public String getDir(File file) 
	{
		if (file.isAbsolute()) {
			return file.getParent();
		}
		return (new File(file.getAbsolutePath())).getParent();
	}

	public String getDir() 
	{
		return getDir((File) m_context);
	}

	public String getAbsolutePath() 
	{
		if (m_context != null) {
			if (m_context instanceof File) {
				return ((File) m_context).getAbsolutePath();
			}
//			return ((URL) m_context).toExternalForm();
		}
		return null;
	}

	public Vector getRelationClasses()
	{
		return m_numToRel;
	}

	public RelationClass numToRelationClass(int n) 
	{
		return (RelationClass) m_numToRel.elementAt(n);
	}

	public int getNumberEntitiesLoaded()
	{
		return m_entityCache.size();
	}

	public int getNumberRelationsLoaded()
	{
		return m_numberRelations;
	}

	// --------------
	// Update methods
	// --------------

	private void createRootInstance()
	{
		m_rootInstance = new EntityInstance(null, ROOT_ID); 	// $ROOT
		m_rootInstance.setRelLocal(0.0, 0.0, 1.0, 1.0);
		// Want to be able to find diagram so give root node a base class up front
		m_rootInstance.setParentClass(m_entityBaseClass);
	}

	private void setupUniversalScheme() 
	{
		m_relationBaseClass.addRelationConstraint(m_entityBaseClass, m_entityBaseClass);
	}

	// After this method has been called however many times
	// need to fill again

	public EntityClass addEntityClass(String id) 
	{
		EntityClass ec = getEntityClass(id); 

		if (ec == null) {
			ec = new EntityClass(id, m_numEntityClasses++, this);
			m_entityClasses.put(id, ec);
			if (m_defaultEntityClass == null) {
				setDefaultEntityClass(ec);
			}
		}
		return ec;
	}

	public void setDefaultRelationClass(RelationClass rc)
	{
		m_defaultRelationClass = rc;
	}

	public RelationClass addRelationClass(String id) 
	{
		RelationClass rc = getRelationClass(id);		// Lookup in the hash table

		if (rc == null) {
			rc = new RelationClass(id, m_numRelationClasses, this);
			m_relationClasses.put(id, rc);
			m_numToRel.addElement(rc);
			m_numRelationClasses++;
			if (m_defaultRelationClass == null)
				setDefaultRelationClass(rc);
		}
		return rc;
	}

	public EntityInstance newCachedEntity(EntityClass ec, String id)
	{
		if (ec == null) {
			System.out.println("newCachedEntity EntityClass null");
		}
		EntityInstance e = ec.newEntity(id); 

		putCache(e);
		return e;
	}

	public RelationInstance newRelation(RelationClass rc, EntityInstance src, EntityInstance dst)
	{
		if (rc == null) {
			rc = m_defaultRelationClass;
			if (rc == null) {
				rc = m_relationBaseClass;
		}	}
		++m_numberRelations;
		return new RelationInstance(rc, src, dst);
	}

	public RelationInstance addEdge(RelationClass rc, EntityInstance src, EntityInstance dst)
	{
		RelationInstance ri = newRelation(rc, src, dst);
		src.addSrcRelation(ri);
		dst.addDstRelation(ri);

		return ri;
	}

	public void changeIOfactor(RelationClass rc)
	{
		Enumeration en;

		for (en = m_entityClasses.elements(); en.hasMoreElements(); ) {
			EntityClass ec = (EntityClass) en.nextElement(); 
			ec.changeIOfactor(rc);
		}
	}

	public void setDefaultEntityClass(EntityClass ec)
	{
		m_defaultEntityClass   = ec;
	}

	public void noDiagram() 
	{
		RelationClass[]	containsClasses;
		setupUniversalScheme();

		setDefaultEntityClass(m_entityBaseClass);
		setDefaultRelationClass(m_relationBaseClass);
		containsClasses = new RelationClass[1];
		containsClasses[0] = m_defaultContainsClass;
		setContainsClasses(containsClasses);
		m_rootInstance = null;
	} 

	public void emptyDiagram() 
	{
		RelationClass[]	containsClasses = new RelationClass[1];
		setupUniversalScheme();

		setDefaultEntityClass(m_entityBaseClass);
		setDefaultRelationClass(m_relationBaseClass);
		m_rootInstance     = newCachedEntity(m_entityBaseClass, ROOT_ID);
		m_drawRoot         = m_rootInstance;
		containsClasses[0] = m_defaultContainsClass;
		switchContainsClasses(containsClasses);
	} 

	private void compactEntities()
	{
		EntityCache		entityCache = m_entityCache;
		EntityInstance	e;

		for (e = entityCache.getFirst(); e != null; e = entityCache.getNext()) {
			e.compact();
	}	}

	public void switchContainsClasses(RelationClass[] containsClasses)
	{
		RelationClass	containsClass;
		int				i;
		
		setContainsClasses(containsClasses);
		
		for (i = 0; i < containsClasses.length; ++i) {
			containsClass = containsClasses[i];
		
			// This will assign it an index for positioning if not having one
			containsClass.computeCIndex();
		}

		prepostorder();
	}

	// ------------------
	// TA Reading methods
	// ------------------

	private void 
	processSchemeTuples(LandscapeTokenStream ts) throws IOException 
	{
		RelationClass	rc1, rc2;
		String			verb, object;
		EntityClass		ec1, ec2;
		String			msg;

		while (ts.nextSchemaTriple()) {

			verb    = ts.m_verb;
			object  = ts.m_object;

			if (verb.equals(INHERIT_RELN)) {							// $INHERIT
				switch (ts.m_relations) {
				case 0:	/* Neither term is a relation */
					if (object.equals(EntityClass.ENTITY_BASE_CLASS_ID)) {	// $ENTITY
						ts.errorNS(verb + " " + object + " " + ts.m_subject + ": Improper use of $ENTITY with $INHERIT");
						break;
					}
					ec1 = addEntityClass(object); 
					ec2 = addEntityClass(ts.m_subject);
					msg = ec1.addParentClass(ec2);
					if (msg != null) {
						ts.errorNS(msg);
					}
					break;
				case 1:	/* First  is relation		*/
				case 2:	/* Second is relation		*/
					ts.errorNS("Mismatched entity/relation with $INHERIT -- presuming both relations");
				case 3:	/* Both term is a relation	*/
					if (object.equals(RelationClass.RELATION_BASE_CLASS_ID)) {	// $ENTITY
						ts.errorNS(verb + " " + object + " " + ts.m_subject + ": Improper use of $RELATION with $INHERIT");
						break;
					}
					rc1 = addRelationClass(object);
					rc2 = addRelationClass(ts.m_subject);
					msg = rc1.addParentClass(rc2);
					if (msg != null) {
						ts.errorNS(msg);
					}
					break;
				}

			} else { 
				if (ts.m_relations != 0) {
					ts.errorNS("Cannot create relationships between relationships");
				} else {
					ec1 = addEntityClass(object); 
					ec2 = addEntityClass(ts.m_subject);
					rc1 = addRelationClass(verb);
					rc1.addRelationConstraint(ec1, ec2);	// If not already present
			}	}
		}
	}

	private void processFactTuples(LandscapeTokenStream ts) throws IOException 
	{
		String			verb, object, subject;
		EntityInstance	e, e1, e2;
		int				ne = 0;
		int				nr = 0;
		EntityClass		ec, ec1;
		RelationClass	rc;

		MsgOut.vprint("\nFACT TUPLE : ");

		while (ts.nextFactTriple()) {

			verb    = ts.m_verb;
			object  = ts.m_object;
			subject = ts.m_subject;

			if (verb.equals(INSTANCE_ID)) {

				// 
				// $INSTANCE instanceId classId
				//

				ne++;
		
				if ((ne % UPDATE_FREQ) == 0) {
					MsgOut.vprint(".");
					m_taFeedback.showProgress("Entities: " + ne);
				}		

				if (object.equals(ROOT_ID)) {
					e = getRootInstance();
				} else {
					e = getCache(object);
				}

				if (!m_strict_TA) {
					ec = addEntityClass(subject);
				} else {
					ec = getEntityClass(subject);
				}
				if (ec == null) {
					ts.errorNS("Strict TA: Entity '" + object + "' instance of undeclared entity class '" + subject + "'");
				} else if (e == null) {

					// New $INSTANCE
					e = newCachedEntity(ec, object);
				} else {
					ec1 = e.getEntityClass();
					if (ec != ec1) {
						if (ec1 != null /* Special case for $ROOT */  && ec1 != m_entityBaseClass) {
							ts.warning("Redeclaration of " + e + " from instance of " + ec1 + " to instance of " + ec);
						}
						e.setParentClass(ec);
				}	}
			} else {

				// Relation tuple definition
				//
				// relationClass entityInstance entityInstance
				//

				nr++;

				if ((nr % UPDATE_FREQ) == 0) {

					MsgOut.vprint(".");
					m_taFeedback.showProgress("Relations: " + nr);
				}		

				e1 = getCache(object);
				e2 = getCache(subject);

				if (!m_strict_TA) {
					rc  = addRelationClass(verb);
					if (e1 == null) {
						e1 = newCachedEntity(m_entityBaseClass, object);
					}

					if (e2 == null) {
						e2 = newCachedEntity(m_entityBaseClass, subject);
					}
				} else {
					rc  = getRelationClass(verb);
				}

				if (rc == null || e1 == null || e2 == null) {
					String msg = "Strict TA: Relation " + "(" + verb + " " + object + " " + subject + ")";
					if (rc == null) {
						msg += " member of undeclared relation class '" + verb + "'";
					}
					if (e1 == null) {
						msg += " has undeclared source entity '" + object + "'";
					} 
					if (e2 == null) {
						msg += " has undeclared destination entity '" + subject + "'";
					}
					ts.errorNS(msg);
				} else {
					addEdge(rc, e1, e2);
				}
			}
		}
		m_numberRelations = nr;
	}

	public void attachBaseClasses()
	{
		Enumeration			en;
		RelationClass		rc1;
		EntityClass			ec1;
		EntityInstance		root;
		
		root = getRootInstance();
		if (root.getParentClass() == null) {
			root.setParentClass(m_entityBaseClass);
		}
		for (en = m_entityClasses.elements(); en.hasMoreElements(); ) {
			ec1 = (EntityClass) en.nextElement(); 
			if (ec1 != m_entityBaseClass  && ec1.getInheritsFromCnt() == 0) {
				ec1.addParentClass(m_entityBaseClass);
			}
			if (ec1.getStyle() == EntityClass.ENTITY_STYLE_NONE) {
				ec1.setStyle(EntityClass.ENTITY_STYLE_2DBOX);
		}	}
		
		for (en = m_relationClasses.elements(); en.hasMoreElements(); ) {
			rc1  = (RelationClass) en.nextElement();
			if (rc1 != m_relationBaseClass && rc1.getInheritsFromCnt() == 0) {
				rc1.addParentClass(m_relationBaseClass);
			}
			if (rc1.getStyle() == EntityClass.ENTITY_STYLE_NONE) {
				rc1.setStyle(Util.LINE_STYLE_NORMAL);
	}	}	}
		
	private void parseStream(Reader reader, String src, URL context) 
	{
		LineNumberReader	linenoReader = null;
		boolean				schemeSetup  = false;

		MsgOut.vprintln("Parse TA file: " + src);

//		System.out.println("" + Thread.currentThread());

		try {
			linenoReader = new LineNumberReader(reader);

			LandscapeTokenStream ts = new LandscapeTokenStream(linenoReader, src, m_entityCache);

			linenoReader.setLineNumber(1);

			ts.m_comments = "";

			for (;;) {
				int sec = ts.nextSection();

				if (sec == LandscapeTokenStream.EOF) {
					break;
				}
				try { 
					switch(sec) {

					case LandscapeTokenStream.SCHEME_TUPLE:

						schemeSetup = true;
						processSchemeTuples(ts);
						break;

					case LandscapeTokenStream.SCHEME_ATTRIBUTE:

						if (!schemeSetup) {
							schemeSetup = true;
							setupUniversalScheme();
						}
						ts.processSchemeAttributes(this);
						break;

					case LandscapeTokenStream.FACT_TUPLE:

						if (ts.m_comments.length() > 0) {
							m_comments = ts.m_comments;
						} else {
							m_comments = null;
						}
						ts.m_comments = null;

						if (!schemeSetup) {
							schemeSetup = true;
							setupUniversalScheme();
						}

						processFactTuples(ts);
						break;

					case LandscapeTokenStream.FACT_ATTRIBUTE:

						compactEntities();
						ts.processFactAttributes(this);
						break;
					}

				} catch (Exception e) {
					m_resString = e.getMessage();
					if (m_resString == null) {
						m_resString = e.toString();
					}
					System.out.println("IO error reading landscape: " + m_resString); 
					System.out.println("Start Line number:" + ts.getStartLineno());
					if (linenoReader != null) {
						System.out.println("Last line read: " + linenoReader.getLineNumber());
					}
					e.printStackTrace();
					break; 
				}
			}
			linenoReader.close(); 
			linenoReader = null;

		} catch (Exception e) {
			m_resString = e.getMessage();
			System.out.println("Parse error: " + m_resString);
			if (linenoReader != null) {
				System.out.println("Line number: " + linenoReader.getLineNumber());
//				e.printStackTrace();
		}	}

		attachBaseClasses();

	} // end parseStream() 

	private InputStream decompress(InputStream is, String source, String subfile)
	{
		InputStream ret      = is;
		int			lth      = source.length();

		if (lth > 4) {
			String ends = source.substring(lth - 4);
			if (ends.equalsIgnoreCase(".zip")) {
				ZipInputStream	zipInputStream;
				ZipEntry		zipEntry;

				try {
					zipInputStream = new ZipInputStream(is);
					for (;;) {
						zipEntry = zipInputStream.getNextEntry();
						if (subfile == null || subfile.equalsIgnoreCase(zipEntry.getName())) {
							break;
						}
						zipInputStream.closeEntry();
					}
				} catch (Exception e) {
					System.out.println("Attempt to open " + source + ((subfile == null) ? "" : "#" + subfile) + " as zip file failed");
					m_resString    = e.getMessage();
					zipInputStream = null;
				}
				return zipInputStream;
			}
			if (ends.equalsIgnoreCase(".jar")) {
				JarInputStream	jarInputStream;
				ZipEntry		zipEntry;

				try {
					jarInputStream = new JarInputStream(is);
					for (;;) {
						zipEntry = jarInputStream.getNextEntry();
						if (subfile == null || subfile.equalsIgnoreCase(zipEntry.getName())) {
							break;
						}
						jarInputStream.closeEntry();
					}
				} catch (Exception e) {
					System.out.println("Attempt to open " + source + ((subfile == null) ? "" : "#" + subfile) + " as jar file failed");
					m_resString    = e.getMessage();
					jarInputStream = null;
				}
				return jarInputStream;
			}

			if (lth > 5) {
				ends = source.substring(lth - 5);
				if (ends.equalsIgnoreCase(".gzip")) {
					GZIPInputStream	gzipInputStream;

					try {
						gzipInputStream = new GZIPInputStream(is);
					} catch (Exception e) {
						System.out.println("Attempt to open " + source + " as gzip file failed");
						m_resString     = e.getMessage();
						gzipInputStream = null;
					}
					return gzipInputStream;
		}	}	}
		return is;
	}

	private boolean parseURL(String taPath, URL context, boolean topLevel) 
	{
		int		lth   = taPath.length();
		char	lc	  = taPath.charAt(lth-1);
		String	entry = null;

		if (lth > 2 &&  lc == ']') {
			int	i = taPath.lastIndexOf('['); 

			if (i > 0 && i < lth - 2) {
				entry  = taPath.substring(i+1, lth-1);
				taPath = taPath.substring(0, i);
				if (m_zipEntry == null) {
					m_zipEntry = entry;
				}
				lth    = i;
				lc     = taPath.charAt(lth-1);
		}	}

		if (lc == File.separatorChar) {
			taPath = taPath.substring(0, lth-1);
		}

		URL					lsURL;
		InputStreamReader	reader;
		
		try {
			if (context == null) {
				lsURL = new URL(taPath);
			} else {
				lsURL = new URL(context, taPath);
			}

			MsgOut.dprintln("Opening URL: " + taPath);
			InputStream is = lsURL.openStream();

			is = decompress(is, taPath, entry);
			if (is == null) {
				return false;
			}
			MsgOut.dprintln("opened");

			reader = new InputStreamReader(is);

			parseStream(reader, taPath, lsURL);
		}
		catch(Exception e) {
			m_resString = e.getMessage();
			return false;
		}

		if (topLevel) {
			setContext(lsURL);
		}
		return true;
	}

	// Called from diagram.loadDiagram to parse a file or an include within a file
	// Also called from ClusterInterface to load an import

	public boolean parseFile(String taPath, Object context, boolean topLevel) 
	{
		String	entry = null;
		int		lth   = taPath.length();
		File	file  = null;
		InputStreamReader	reader;

		if (lth > 0) {
			char	lc	  = taPath.charAt(lth-1);

			if (lth > 2 &&  lc == ']') {
				int	i = taPath.lastIndexOf('['); 

				if (i > 0 && i < lth - 2) {
					entry  = taPath.substring(i+1, lth-1);
					if (m_zipEntry == null) {
						m_zipEntry = entry;
					}
					taPath = taPath.substring(0, i);
					lth    = i;
					lc     = taPath.charAt(lth-1);
			}	}
			if (lc == File.separatorChar) {
				taPath = taPath.substring(0, lth-1);
		}	}	
		
		try {
			InputStream	is   = null;

			if (lth == 0) {
				if (context instanceof InputStream) {
					is = (InputStream) context;
				} 
			} else {
				if (context instanceof File) {
					String dir = getDir((File) context);
					file = new File(dir, taPath);
				} else {
					file = new File(taPath);
				}
				if (file != null) {
//					System.out.println(taPath);
					is = new FileInputStream(file);
					is = decompress(is, taPath, entry);
			}	}
			if (is == null) {
				m_resString = "No input stream specified";
				return false;
			}

			reader = new InputStreamReader(is);

			parseStream(reader, taPath, null);
			is.close();

		} catch (Exception e) {
			m_resString = e.getMessage();
			return false;
		}
		if (topLevel && file != null) {
			setContext(file);
		}
		return true;
	}

	public void prepostorder()
	{
		m_rootInstance.prepostorder(1);
	}

	// This does just enough to allow navigation by contains edge
	// Call this if root == null

	public void setContainsClasses(RelationClass[] containsClasses)
	{
		Enumeration		en;
		RelationClass	rc;
		int				i;

		for (en = enumRelationClasses(); en.hasMoreElements(); ) {
			rc = (RelationClass) en.nextElement();
			rc.setContainsClassOffset(-1);
		}
		
		if (containsClasses != null) {
			for (i = containsClasses.length; --i >= 0; ) {
				rc = containsClasses[i];
				// System.out.println("" + i + ") " + rc);

				rc.setContainsClassOffset(i);
				//rc.setActive(false, false);
				if (m_defaultRelationClass == rc) {
					setDefaultRelationClass(m_relationBaseClass);
		}	}	}
		m_containsClasses = containsClasses;
	}

	public void reverseRelations(RelationClass rc, boolean reverse)
	{
		EntityCache			entityCache = m_entityCache;
		EntityInstance		e;
		RelationInstance	ri;
		Vector				v;
		int					i, j;
		
//		System.out.println("ReverseRelations " + rc + " " + reverse);
		
		for (e = entityCache.getFirst(); e != null; e = entityCache.getNext()) {
			v = e.getSrcRelList();
			for (i = 2;; ) {
				if (v != null) {
					for (j = v.size(); j > 0; ) {
						ri = (RelationInstance) v.elementAt(--j);
						if (ri.getParentClass() == rc) {
							if (ri.isMarked(RelationInstance.REVERSED_MARK) != reverse) {
								ri.reverseRelation();
								if (reverse) {
									ri.orMark(RelationInstance.REVERSED_MARK);
								} else {
									ri.nandMark(RelationInstance.REVERSED_MARK);
								}
								if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
									m_changed_spanning_edges = true;
				}	}	}	}	}
				if (--i == 0) {
					break;
				}
				v = e.getDstRelList();
	}	}	}
	
	// Done after loading the TA to reverse relations when appropriate
	
	public void reverseRelations()
	{
		Enumeration		en;
		RelationClass	rc;
		
		for (en = enumRelationClasses(); en.hasMoreElements(); ) {
			rc = (RelationClass) en.nextElement();
			if (rc.getShown() == LandscapeClassObject.DIRECTION_REVERSED) {
				reverseRelations(rc, true);
		}	}  
		m_changed_spanning_edges = false;
	}
	
	public Vector getForest()
	{
		Enumeration			en = m_rootInstance.srcRelationElements();
		Vector				forest;
		RelationInstance	ri;

		forest = new Vector();
		if (en != null) {
			while (en.hasMoreElements()) {
				ri = (RelationInstance) en.nextElement();
				if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
					forest.addElement(ri.getDst());
		}	}	}
		return(forest);
	}

	protected void establishForest(RelationClass[] containsClasses, boolean switching)
	{
		EntityInstance	rootInstance     = m_rootInstance;
		EntityCache		entityCache      = m_entityCache;
		int				size;
		
		removeCache(rootInstance);		// Dont want to see this while searching for forests (not part of graph)

		size = entityCache.size();
		
		if (size > 0) {
			EntityInstance[] array     = new EntityInstance[size];
			TaFeedback		taFeedback = m_taFeedback;
			EntityInstance	e;
			RelationClass	containsClass;
			RelationInstance ri;
			int				i, j;

			i = 0;
			for (e = entityCache.getFirst(); e != null; e = entityCache.getNext()) {
				e.notInForest(switching);
				array[i++] = e;
			}

			rootInstance.removeAllEdges();
			
			for (i = 0; i < containsClasses.length; ++i) {
				containsClass = containsClasses[i];
				for (j = 0; j < size; ) {
					e = array[j];
					e.tryToAddToForest(containsClass, taFeedback);
					if (!e.isMarked(EntityInstance.NOT_IN_FOREST_MARK)) {
						// Succeeded
						array[j] = array[--size];
					} else {
						++j;
			}	}	}			
							
			// Add the root instance back in
			putCache(m_rootInstance);

			containsClass = containsClasses[0];
			for (i = 0; i < size; ++i) {
				e  = array[i];
				ri = addEdge(containsClass, rootInstance, e);	// Not cached
				// Don't clear these marks till found all spans
				e.nandMark(EntityInstance.NOT_IN_FOREST_MARK);
				e.setContainedByRelation(ri);
			}
			array = null;
		}
	}
	
	protected void clearResultBox()
	{
		Diagram			diagram          = getDiagram();
		
		if (diagram != null) {
			LandscapeEditorCore	ls        = diagram.getLs();
			ResultBox			resultBox = ls.getResultBox();
			
			resultBox.clear();
	}	}
			
	protected void clearEstablishForest(RelationClass[] containsClasses)
	{
		clearResultBox();
		establishForest(containsClasses, true);
	}

	// Overridden if can handle CMDB:
	
	protected boolean isSpecialPath(String path)
	{
		return false;
	}
	
	protected String parseSpecialPath(Ta ta, String path)
	{
		return null;
	}

	// If taPath is "" and context is Process then read from processes stdout

	public RelationClass[] computedContainsClasses() 
	{
		Enumeration		en;
		RelationClass[]	containsClasses;
		RelationClass	rc;
		int				max_contain, containsClassOffset;
		
		max_contain     = -1;
		for (en = m_relationClasses.elements(); en.hasMoreElements(); ) {
			rc = (RelationClass) en.nextElement();
			containsClassOffset = rc.getContainsClassOffset();
			if (containsClassOffset > max_contain) {
				max_contain = containsClassOffset;
		}	}
		if (max_contain < 0) {
			rc = getRelationClass(CONTAIN_ID);
			if (rc == null) {
				return null;
			}
			containsClasses    = new RelationClass[1];
			containsClasses[0] = rc;
		} else {
			containsClasses = new RelationClass[max_contain+1];
			
			for (en = m_relationClasses.elements(); en.hasMoreElements(); ) {
				rc = (RelationClass) en.nextElement();
				containsClassOffset = rc.getContainsClassOffset();
				if (0 <= containsClassOffset) {
					if (containsClasses[containsClassOffset] == null) {
						containsClasses[containsClassOffset] = rc;
					} else {
						// Fix the lie
						rc.setContainsClassOffset(-1);
		}	}	}	}
		return containsClasses;
	}
	
	// If taPath is "" and context is Process then read from processes stdout

	public String loadTA(String taPath, Object context) 
	{
		boolean		ok;

		m_progressCount  = 0;
		m_loaded_special = false;

		m_containsClasses = null;
		m_resString       = null;
		m_zipEntry        = null;
		
		clearResultBox();
		
		if (taPath == null) {
			noDiagram();
		} else {
			int		lth   = taPath.length();

			if (lth == 0 && context == null) {
				emptyDiagram();
			} else {
				m_numberRelations = 0;
				createRootInstance();
				AttributeCache.activate();
				if (isSpecialPath(taPath)) {
					m_resString       = parseSpecialPath(this, taPath);
					ok = (m_resString == null);
					if (ok) {
						m_loaded_special = true;
					}
				} else if (context instanceof URL || Util.isHTTP(taPath)) {
					MsgOut.dprintln("Parse a URL");
					ok = parseURL(taPath, (URL) context, true);
				} else {
					MsgOut.dprintln("Parse a file");
					ok = parseFile(taPath, context, true);
				}	
				AttributeCache.deactivate();
				if (!ok) {
					if (m_resString == null) {
						m_resString = "Unknown error parsing " + context;
					}
					m_rootInstance = null;
					return m_resString;
				}
				putCache(m_rootInstance);
		}	}
	
		if (m_defaultEntityClass == null) {
			setDefaultEntityClass(m_entityBaseClass);
		}
		if (m_defaultRelationClass == null) {
			setDefaultRelationClass(m_relationBaseClass);
		}
		
		StringCache.clear();

		reverseRelations();
		
		RelationClass[]	containsClasses = computedContainsClasses();
		
		if (containsClasses == null) {
			m_taFeedback.noContainRelation(taPath);
			return "No contains relation class defined";
		}
					
		establishForest(containsClasses, false);	// Initial load
		switchContainsClasses(containsClasses);
		
		if (m_rootInstance != null) {
			m_rootInstance.setDefaultOpenStatus();
		}			
		if (m_context == null) {
			return m_resString;
		}
		return null;
	}

	// -------------------
	// TA writing methoods
	// -------------------

	public void writeSchemeTuples(PrintWriter ps, Enumeration entityClasses, Enumeration relationClasses, Enumeration relationClassPairs)
	{
		Enumeration		en, en1;
		EntityClass		ec, parent;
		RelationClass	rc, rparent;
		String			id;
		boolean			seen;

		ps.println("SCHEME TUPLE :");
		ps.println(""); 

		while (entityClasses.hasMoreElements()) {
			ec = (EntityClass) entityClasses.nextElement();
			if (ec != m_entityBaseClass) {
				seen = false;
				id   = ec.getId();
				for (en1 = ec.enumInheritsFrom(); en1.hasMoreElements(); ) {
					parent = (EntityClass) en1.nextElement();
					ps.println(INHERIT_RELN + " " + id + " " + parent.getId());
					seen = true;
				}
				if (!seen && !id.equals(EntityClass.ENTITY_BASE_CLASS_ID)) {
					ps.println(INHERIT_RELN + " " + id + " " + EntityClass.ENTITY_BASE_CLASS_ID);
				}
			}
		}

		while (relationClasses.hasMoreElements()) {
			rc = (RelationClass) relationClasses.nextElement();
			if (rc != m_relationBaseClass) {
				seen = false;
				id   = rc.getId();
				for (en1 = rc.enumInheritsFrom(); en1.hasMoreElements(); ) {
					rparent = (RelationClass) en1.nextElement();
					ps.println(INHERIT_RELN + " (" + id + ") (" + rparent.getId() + ")");
					seen = true;
				}
				if (!seen && !id.equals(RelationClass.RELATION_BASE_CLASS_ID)) {
					ps.println(INHERIT_RELN + " (" + id + ") (" + RelationClass.RELATION_BASE_CLASS_ID + ")");
				}
			}
		}

		ps.println();
		while (relationClassPairs.hasMoreElements()) {	// Output in same order as input
			rc = (RelationClass) relationClassPairs.nextElement();
			rc.writeEntityClassPairs(ps); 
		}
	}
	
	public void writeSchemeTuples(PrintWriter ps)
	{
		writeSchemeTuples(ps, enumEntityClassesInOrder(), enumRelationClassesInOrder(), enumRelationClassesInOrder());
	}

	public void writeSchemeAttributes(PrintWriter ps, Enumeration entityClasses, Enumeration relationClasses)
	{
		Enumeration en;

		ps.println();
		ps.println();
		ps.println("SCHEME ATTRIBUTE :");
		ps.println();
		// Write attributes for entity classes 

		for (en = entityClasses; en.hasMoreElements(); ) {
			EntityClass ec = (EntityClass) en.nextElement(); 
			ec.writeAttributes(ps);
		}

		// Write attributes for relation classes 

		for (en = relationClasses; en.hasMoreElements(); ) {
			RelationClass rc = (RelationClass) en.nextElement();
			rc.writeAttributes(ps);
		}
	}
	
	public void writeSchemeAttributes(PrintWriter ps)
	{
		writeSchemeAttributes(ps, m_entityClasses.elements(), m_relationClasses.elements());
	}

	private void writeFactAttributes(PrintWriter ps)
	{
		EntityInstance		rootInstance = m_rootInstance;
		Vector				srcRelList   = rootInstance.getSrcRelList();

		ps.println();
		ps.println();
		ps.println("FACT ATTRIBUTE :");
		ps.println();
				 
		rootInstance.writeOptionsAttributes(ps); 

		// Write out attribute record for us, and then our children
		
		if (srcRelList != null) {
			RelationInstance	ri;
			EntityInstance		child;
			int					i, size;

			// Recurse for contained children

			size = srcRelList.size();
			for (i = 0; i < size; ++i) {
				ri = (RelationInstance) srcRelList.elementAt(i);
				if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
					child = ri.getDst();
					child.writeAttributes(ps, ri.getRelationClass());
	}	}	}	}

	private void writeFactTuples(PrintWriter ps) 
	{
		Enumeration		 en; 
		EntityInstance	 e;
		
		ps.println();
		ps.println();
		ps.println("FACT TUPLE :"); 
		ps.println();

		for (en = m_rootInstance.getChildren(); en.hasMoreElements(); ) {
			EntityInstance child = (EntityInstance) en.nextElement();
			child.writeInstances(ps);
		}

		// Avoid writing m_rootInstance relations

		for (en = m_rootInstance.getChildren(); en.hasMoreElements(); ) {
			EntityInstance child = (EntityInstance) en.nextElement();
			child.writeRelations(ps);
		}
	}

	// Save the landscape in the output stream

	public String saveDiagram(OutputStream os, boolean markEnd)
	{
		String ret = null;
		
		if (m_rootInstance != null) {
		
			PrintWriter	ps = new PrintWriter(os);
	
			ps.println("// Landscape TA file written by LSEdit " + Version.Number());
			if (m_comments != null) {
				ps.print(m_comments);
			}
			ps.println();

			writeSchemeTuples(ps);
			writeSchemeAttributes(ps);
			writeFactTuples(ps);
			writeFactAttributes(ps);

			if (markEnd) {
				ps.println("END");
			}
			if (ps.checkError()) {
				ret = "Error saving TA";
			} else { 
				m_changedFlag = false;
			}
			ps.close();
		}
		return ret;
	}		

	protected String saveByFile(String newPath) 
	{
		OutputStream os = null;

		try {
			if (newPath != null && newPath.length() == 0) {
				os = System.out;
			} else {
				File file  = (File) getContext();

				if (newPath == null) {
					String name = file.getPath();
					file.renameTo(new File(name + ".old"));
				} else {
					// New filename
					file = new File(newPath);
					setContext(file);
				}
				os = new FileOutputStream(file);
			}
		} catch(IOException e) {
			return "IOException on file open";
		}
		return saveDiagram(os, false);
	}
	
	public void addView(View view)
	{
		Vector	v        = m_views;
/*
		String	label    = view.getText();
		String	label1;

		View	view1;
		int		i, size;
 */
		
		if (v == null) {
			m_views = v = new Vector();
		}
/*
		size = v.size();
		for (i = 0; i < size; ++i) {
			view1  = (View) v.elementAt(i);
			label1 = view1.getText();
			if (label1.compareToIgnoreCase(label) > 0) {
				break;
		}	}
		v.add(i, view);
 */
		v.add(view);
	}

	public void addRootView(String attributeId, String value)
	{
		View		view;
			
		view = new View();
		view.setDiagram(getDiagram());
		view.load(attributeId, value);
		addView(view);
	}		
	
	private void repaintViewBox()
	{
		Diagram	diagram = getDiagram();
		
		if (diagram != null) {
			LandscapeEditorCore	ls      = diagram.getLs();
			ViewBox				viewBox = ls.getViewBox();
				
			viewBox.fill();
	}	}
			
	public void removeView(View view)
	{
		if (m_views != null) {
			m_views.removeElement(view);
			repaintViewBox();
	}	}
	
	public void removeAllViews()
	{
		if (m_views != null) {
			m_views.clear();
			repaintViewBox();
	}	}
	
	public void moveView(View view, int direction, int offset)
	{
		int	index    = m_views.indexOf(view);
		int	newindex = index;
		
		switch (direction) {
		case 0:
			newindex = offset-1;
			break;
		case 1:
			newindex = index + offset;
			break;
		case -1:
			newindex = index - offset;
			break;
		}
		if (newindex < 0) {
			newindex = 0;
		} else if (newindex >= m_views.size()) {
			newindex = m_views.size() - 1;
		}
		if (newindex != index) {
			m_views.remove(index);
			if (newindex > index) {
				--newindex;
			}
			m_views.insertElementAt(view, newindex);
			repaintViewBox();
	}	}
	
	public Vector getViews()
	{
		return m_views;
	}
	
	public void LoadSchemaForView(View view, String attributeId, String input)
	{
		TaFeedback	save_taFeedback         = m_taFeedback;				
		Hashtable	save_entityClasses      = m_entityClasses;
		Hashtable	save_relationClasses    = m_relationClasses;
		Vector		save_numToRel           = m_numToRel;
		int			save_numRelationClasses = m_numRelationClasses;		
		int			save_numEntityClasses   = m_numEntityClasses;	
		String		save_comments           = m_comments;
		StringReader reader	                = new StringReader(input);	
		
		setNoFeedback();
		m_entityClasses   = new Hashtable(50);	// Entity classes
		m_relationClasses = new Hashtable(50);	// Relation classes

		parseStream(reader, attributeId, null);
		view.getSchemeSnapshot(this);
		
		m_taFeedback         = save_taFeedback;
		m_entityClasses      = save_entityClasses;
		m_relationClasses    = save_relationClasses;
		m_numToRel           = save_numToRel;
		m_numRelationClasses = save_numRelationClasses;
		m_numEntityClasses   = save_numEntityClasses;
		m_comments           = save_comments;
	} 

	// Wraps the TaFeedback interface

	public void showProgress(String message)
	{
		m_taFeedback.showProgress(message);
	}

	public void doFeedback(String message)
	{
		m_taFeedback.doFeedback(message);
	}

	public void showInfo(String message)
	{
		m_taFeedback.showInfo(message);
	}

	public void error(String message)
	{
		m_taFeedback.error(message);
	}

	public void showCycle(RelationInstance ri)
	{
		m_taFeedback.showCycle(ri);
	}

	public void noContainRelation(String taPath)
	{
		m_taFeedback.noContainRelation(taPath);
	}

	public void hasMultipleParents(RelationClass rc, EntityInstance e)
	{
		m_taFeedback.hasMultipleParents(rc, e);
	}
}

