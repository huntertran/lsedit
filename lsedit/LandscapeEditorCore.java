package lsedit;

import java.applet.AppletContext;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import java.util.Enumeration;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.undo.UndoableEdit;

class DefaultBrowserButton extends JButton implements ActionListener {

	protected JFileChooser		m_fileChooser;
	protected File				m_defaultFile;
	
	public DefaultBrowserButton(JFileChooser fileChooser, String os, File defaultFile)
	{
		super("default");
		setToolTipText(os + " default");
		m_fileChooser = fileChooser;
		m_defaultFile = defaultFile;
		addActionListener(this);
	}

	// ActionListener interface

	public void actionPerformed(ActionEvent ev)
	{
		if (ev.getSource() == this) {
			m_fileChooser.setSelectedFile(m_defaultFile);
}	}	}

class LoadLsSwingWorker extends SwingWorker
{
	LandscapeEditorCore m_ls;
	String				m_path;
	String				m_drawRoot;

	public LoadLsSwingWorker(LandscapeEditorCore ls, String path, String drawRoot)
	{
		m_ls       = ls;
		m_path     = path;
		m_drawRoot = drawRoot;
	}

	public Object construct()
	{
		System.out.println("Loading " + m_path + " in background");
		m_ls.loadLs1(m_path, m_drawRoot);
		return null;
}	}

public final class LandscapeEditorCore extends Do /* extends JPanel */ implements TaListener, UndoListener, TaFeedback, ToolBarEventHandler
{
	protected final static int GRID_MAX			 = 100;

	protected final static int GAP               = 4;		// Gap between GUI objects

	protected final static String TITLE = "Software Landscape Editor";

	public final static int FA_LOAD       = 0;
	public final static int FA_LOAD_DIR   = FA_LOAD+1;
	public final static int FA_SAVE		  = FA_LOAD_DIR+1;


	protected EditModeHandler m_editModeHandler;

	/* Parameters */

	public	  String m_aboutURL        = "http://www.swag.uwaterloo.ca";
	public	  String m_helpURL         = "http://www.swag.uwaterloo.ca/lsedit";
	public	  String m_startParam      = null;
	public    String m_startEntity     = null;
	public    String m_lsPath          = null;	/* Initial landscape to load			*/
	public	  String m_lsInit          = null;	/* File to use for initialising state	*/

	protected JFrame        m_frame        = null;
	protected URL			m_documentBase = null;
	protected AppletContext m_ac           = null; 

	protected static final String m_leftTextBoxHelp  = "Displays the 'description' for the current landscape.";
	protected static final String m_rightTextBoxHelp = "Displays the 'description' for the closed (not a container) entity currently under the mouse cursor.";
	protected static final String m_feedbackHelp     = "Displays feedback from the program. Examples include errors, warnings, and confirmations of action.";
	protected static final String m_nameBoxHelp      = "Displays the landscape entity, edge, or application button/box currently under the mouse cursor.";

	protected static double m_mainSplitRatio   = 0.80;
	protected static double m_secondSplitRatio = 0.1;
	protected static double m_thirdSplitRatio  = 0.5;
	
	protected static String	m_browser          = null;	/* HTML browser */

	protected static JMenuBar m_menuBar;
	protected static JMenu m_fileMenu;
	protected static JMenu m_windowsMenu;

	// Frame contained in

	protected static int m_openFrames = 1;	// Number of open frames

	// Content pane

	protected Container			m_contentPane;

	// GUI compontents

	protected JToolBar			m_toolBar	         = null;

	protected JSplitPane		m_infoSplitPane	     = null;	// Left and right diagnostic area
	protected JSplitPane		m_infoDiagramSplitPane= null;	// Diagram below
	protected JSplitPane		m_diagramTabSplitPane= null;	// Tab box on right
	protected JScrollPane		m_scrollLeftTextBox  = null;		

	protected JPanel			m_leftInfoPanel      = null;
	protected JPanel			m_rightInfoPanel     = null;

	protected JLabel			m_leftTextBoxTitle   = null;
	protected TextBox			m_leftTextBox        = null;
	protected JScrollPane		m_scrollRightTextBox = null;
	protected JLabel			m_rightTextBoxTitle  = null;
	protected TextBox			m_rightTextBox       = null;
	protected Feedback			m_feedback           = null;
	protected Feedback			m_nameBox            = null; 

	public    JScrollPane		m_scrollDiagram      = null;
	protected Diagram			m_diagram            = null;		// Active diagram 

	protected RightTabbedPane	m_rightTabbedPane    = null;		// The right panel of the view
	protected LegendBox			m_legendBox          = null;
	protected MapBox			m_mapBox             = null;
	protected QueryBox			m_queryBox           = null;
	protected ResultBox			m_resultBox          = null;
	protected TocBox			m_tocBox             = null;
	protected UndoBox			m_undoBox            = null;
	protected HistoryBox		m_historyBox         = null;
	protected ClipboardBox		m_clipboardBox       = null;
	protected AttributeBox		m_attributeBox       = null;
	protected ViewBox			m_viewBox			 = null;


	protected EntityInstance	m_currentNameEntity = null;

	/* Parameters */

	protected String			 m_lsSavePath, m_lsSaveSuffix, m_lsSaveCmd;	// Not used by viewer
	protected int				m_handicapped     = 0;
	protected int				m_forward         = -1;			// If >= 0 then path trace forward this number of steps
	protected EntityInstance	m_forwardEntity   = null;


	protected int mode = 0;

	protected boolean modeHandlingActive = false;

	protected RelationInstance	m_currentEdge       = null;
	protected EntityInstance	m_currentDescEntity = null;

	protected Find				m_findResults       = null;

	private int m_curCursor        = Cursor.DEFAULT_CURSOR;

	protected JApplet			m_applet		   = null;	

	protected static int PREV_HISTORY_BUTTON = 13;
	protected static int NEXT_HISTORY_BUTTON = 14;
	protected static int PREV_FIND_BUTTON    = 15;	// Position in ToolBarButton
	protected static int NEXT_FIND_BUTTON    = 16;

	protected ToolBarButton[] m_toolButton;
	
	// Hook for CMDB parsing

	protected SpecialPath	m_specialPath = null;
		
	public boolean isSpecialPath(String path)
	{
		if (m_specialPath == null) {
			return false;
		}
		return m_specialPath.isSpecialPath(path);
	}
	
	public String parseSpecialPath(Ta ta, String path)
	{
		return m_specialPath.parseSpecialPath(ta, m_resultBox, path);
	}

	public void specialPathOptions(Option option)
	{
		m_specialPath.specialPathOptions(option);
	}
	
	// ------------------
	// JComponent methods
	// ------------------

	public void add(JComponent c)
	{
		m_contentPane.add(c);
	}

	public void repaint()
	{
		m_contentPane.repaint();
	}

	public void validate()
	{
		m_contentPane.validate();
	}

	public void requestFocus()
	{
		m_contentPane.requestFocus();
	}

	public Graphics getGraphics()
	{
		return(m_contentPane.getGraphics());
	}

	public int getHeight()
	{
		return(m_contentPane.getHeight());
	}

	// ===========================
	// LandscapeEditorCore methods
	// ===========================

	public boolean isApplet() 
	{
		return (m_applet != null);
	}
	
	public String getStartEntity()
	{
		return m_startEntity;
	}
	
	public void setStartEntity(String entityName)
	{
		m_startParam     = entityName;
		m_startEntity    = entityName;
	}
	
	public String parameterDetails()
	{
		LandscapeLayouter	layout;
		String ret = "\n";
		
		if (m_aboutURL != null) {
			ret += "toolabout=\"" + m_aboutURL + "\"\n";
		}
		if (m_helpURL != null) {
			ret += "toolhelp=\"" + m_helpURL + "\"\n";
		}
		if (m_lsPath != null) {
			ret += "lsfile=\"" + m_lsPath + "\"\n";
		} 
		if (m_startParam != null) {
			ret += "startEntity=\"" + m_startParam + "\"\n";
		}
		if (m_lsInit != null) {
			ret += "init=\"" + m_lsInit + "\"\n";
		}
		layout = getLayouter();
		if (layout != null) {
			ret += "layout=\"" + layout.getName() + "\"\n";
		}

		return ret;
	}

	public boolean isAddToClipboard()
	{
		return m_clipboardBox.isAddToClipboard();
	}

	protected void createFileMenu()
	{
		JMenu	m;

		// Have to do this up front because the landscapeEditorFrame may
		// immediately start adding to the end of this menu even before
		// the application has been launched

		m_fileMenu = m = new JMenu("File");
		fileMenuItem(m, this, isApplet());
		m.addSeparator();
	}

	public LandscapeEditorCore(JFrame frame, SpecialPath specialPath)
	{
		m_frame       = frame;		// m_applet == null
		m_specialPath = specialPath;
		m_contentPane = frame.getContentPane();
		createFileMenu();
	}

	public LandscapeEditorCore(JApplet applet, SpecialPath specialPath)
	{
		m_applet       = applet;	// m_frame == null
		m_specialPath  = specialPath;
		m_documentBase = applet.getDocumentBase();
		m_contentPane  = applet.getContentPane();

		createFileMenu();
	}

	public static String getTitle() 
	{
		return TITLE;
	}

	public EditModeHandler getModeHandler()
	{
		return m_editModeHandler;
	}

	public URL getDocumentBase()
	{
		return m_documentBase;
	}

	protected void repaintTabs() 
	{
		if (m_rightTabbedPane != null) {
			m_rightTabbedPane.revalidate();
		}
	}

	protected static String canonicalPath(String lseditFile)
	{
		File	file;
		String	path;

		try {
			file = new File(lseditFile);
			path = file.getCanonicalPath();
		} catch (Exception e) {
			path = lseditFile;
		}
		return(path);
	}

	protected String defaultIniFile()
	{
		if (m_lsInit == null) {
			String	file;

			if (isApplet()) {
				file = "http:lsedit.ini";
			} else {
				String	os = Version.Detail("os.name");
		
				if (os != null && os.startsWith("Windows")) {
					file = "lsedit.ini";
				} else {
					file = ".lsedit";
			}	}
			m_lsInit = file;
		}
		return m_lsInit;
	}

	protected String defaultIniDir()
	{
		if (isApplet()) {
			return(".");
		}
		return Version.Detail("user.home");
	}

	protected String defaultIniPath()
	{
		return Util.formFileName(defaultIniDir(), defaultIniFile()) ;
	}

	protected void addLseditHistory(String lseditPath)
	{
		int i, cnt;
		String path;
		JMenuItem item;

		path = canonicalPath(lseditPath);
		cnt = m_fileMenu.getItemCount();
		for (i = 0; i < cnt; ++i)
		{
			item = m_fileMenu.getItem(i);
			if (item instanceof MyHistoryMenuItem)
			{
				if (((MyHistoryMenuItem)item).isPath(path))
				{
					return;
				}
			}
		}
		new MyHistoryMenuItem(m_fileMenu, path, this);
		//		System.out.println("LandscapeEditorCore.addLseditHistory");
	}

	private static void
	reportException(Throwable e)
	{
		for (;e != null; e = e.getCause()) {
			System.out.println(e.toString() + ": " + e.getMessage());
		}
	}
		
	protected void loadLseditHistory(JMenu m, String file)
	{
		String				userHome;
		FileReader			fileReader;
		BufferedReader		bufferedReader;
		String				line, attribute, value;
		int					i, mode;
		LandscapeLayouter	layouter;
		Option				defaultOptions, landscapeOptions, diagramOptions, options;

		defaultOptions   = Options.getDefaultOptions();
		landscapeOptions = Options.getLandscapeOptions();
		diagramOptions   = Options.getDiagramOptions();
		options          = null;
		mode             = -2;
		
		defaultOptions.reset();
		landscapeOptions.reset();
		diagramOptions.reset();
		
		for (i = 0; i < m_layouters.length; ++i) {
			layouter = m_layouters[i];
			layouter.reset();
		}

		try {

			if (isApplet()) {
				if (file == null) {
					file = defaultIniFile();
				}
			} else {
				if (file == null) {
					userHome = defaultIniDir();
					if (userHome == null || userHome == "") {
						System.out.println("Can't load state: Unknown home directory");
						return;
					}
					file = defaultIniPath();
			}	}
				
			if (file == null || file.length() == 0) {
				System.out.println("Can't load state: no filename"); 
				return;
			}
			
			if (file.startsWith("http:")) {
				URL url;
				
				if (isApplet()) {				
					url = new URL(m_documentBase, file);
				} else {
					url = new URL(file);
				}
				file = url.toString();
				
				URLConnection		conn   = url.openConnection();
				InputStream			is     = conn.getInputStream();
				InputStreamReader	reader = new InputStreamReader(is);

				bufferedReader = new BufferedReader(reader); 
			} else {
				fileReader     = new FileReader(file);
				bufferedReader = new BufferedReader(fileReader);
			}
			System.out.println("Load state from " + file);
		
			while ((line = bufferedReader.readLine()) != null) {
//				System.out.println(line);
				if (line.equals("GENERAL:")) {
					mode    = -1;
					continue;
				}

				if (line.equals("DEFAULTS:")) {
					options = defaultOptions;
					mode    = Options.DEFAULT_OPTION;
					continue;
				}
				if (line.equals("USER:")) {
					landscapeOptions.setTo(defaultOptions);
					options = landscapeOptions;
					mode    = Options.LANDSCAPE_OPTION; 
					continue;
				}
				if (line.equals("DIAGRAM:")) {
					options = diagramOptions;
					mode    = Options.DIAGRAM_OPTION;
					continue;
				}
				if (mode < -1) {
					continue;
				}

				i = line.indexOf('=');
				if (i < 0 || line.length() == i+1) {
					continue;
				}
				attribute = line.substring(0, i);
				attribute = attribute.trim();
				value     = line.substring(i + 1);
				value     = value.trim();
				if (attribute.equals("browser")) {
					m_browser = value;
					continue;
				}
				if (attribute.startsWith("execute:")) {
					ExecuteAction.loadIni(attribute, value);
					continue;
				}
				for (i = 0; i < m_layouters.length; ++i) {
					layouter = m_layouters[i];
					if (attribute.startsWith(layouter.getTag())) {
						layouter.loadLayoutOption(mode, attribute, value);
						break;
				}	}
				if (attribute.equals("menu")) {
					addLseditHistory(value);
					continue;
				}
				if (options != null) {
					options.loadOption(attribute, value);
			}	}
			bufferedReader.close();
		} catch (Exception e) {
			System.out.println("Exception reading \"" + file + "\"");
			reportException(e);
			return;
		}
	}
	
	public void saveLseditHistory()
	{
		if (isApplet()) {
			// Can't invoke System.getProperty("user.home") 
			// Don't want applets to be able to change the lsedit.ini files anyway
			return;
		}
		int					i, cnt;
		JMenuItem			item;

		String				userHome, path, filename;
		String				browser;
		String				file = "";
		String				s;
		LandscapeLayouter	layouter;
		Option				defaultOptions, landscapeOptions, diagramOptions;

		try {
			userHome       = defaultIniDir();

			if (userHome == null) {
				System.out.println("Can't save state: don't know location of $HOME");
			} else {
				file = defaultIniPath();
				System.out.println("Saving state in " + file);

				defaultOptions   = Options.getDefaultOptions();
				landscapeOptions = Options.getLandscapeOptions();
				diagramOptions   = Options.getDiagramOptions();
				
				FileOutputStream os = new FileOutputStream(file);
				PrintWriter      ps = new PrintWriter(os);

				ps.println("GENERAL:");
				ps.println("");
				
				cnt = m_fileMenu.getItemCount();
				for (i = 0; i < cnt; ++i) {
					item = m_fileMenu.getItem(i);
					if (item instanceof MyHistoryMenuItem) {
						path = ((MyHistoryMenuItem) item).getPath();
						ps.println("menu=" + path);
				}	}
				browser = m_browser;
				if (browser != null) {
					ps.println("browser=" + browser);
				}
				
				ExecuteAction.saveOptions(ps);

				ps.println("");
				ps.println("DEFAULTS:");
				ps.println("");

				defaultOptions.saveOptions(ps, false);

				for (i = 0; i < m_layouters.length; ++i) {
					layouter = m_layouters[i];
					layouter.saveLayoutOptions(Options.DEFAULT_OPTION, ps);
				}
							
				ps.println("");
				ps.println("USER:");
				ps.println("");
			
				landscapeOptions.saveOptions(ps, false);
	
				for (i = 0; i < m_layouters.length; ++i) {
					layouter = m_layouters[i];
					layouter.saveLayoutOptions(Options.LANDSCAPE_OPTION, ps);
				}
				
				ps.println("");
				ps.println("DIAGRAM:");
				ps.println("");
				
				diagramOptions.saveOptions(ps, false);
								
				if (ps.checkError()) {
					System.out.println("Print error occured when attempting to save history in " + file);
				}
				ps.close();
			}
		} catch (Exception e) {
			System.out.println("Exception  " + e.getMessage() + " caught when attempting to save history in " + file);
		}
	}
	
	public void removeHistoryMenu(MyHistoryMenuItem item)
	{
		m_fileMenu.remove(item);
	}

	public void removeHistory()
	{
		int			i, cnt;
		JMenuItem	item;

		cnt = m_fileMenu.getItemCount();
		for (i = cnt; i > 0; ) {
			item = m_fileMenu.getItem(--i);
			if (item instanceof MyHistoryMenuItem) {
				removeHistoryMenu((MyHistoryMenuItem) item);
	}	}	}

	// Add this diagram to the list of diagrams in the Windows menu if not already present there

	protected void rememberDiagram(Diagram newDg, String path)
	{
		int			i, cnt;
		JMenuItem	item;

		cnt  = m_windowsMenu.getItemCount();
		for (i = 0; i < cnt; ++i) {
			item = m_windowsMenu.getItem(i);
			if (item instanceof MyWindowsMenuItem) {
				if ( ((MyWindowsMenuItem) item).isDiagram(newDg) ) {
					return;
		}	}	}

		new MyWindowsMenuItem(m_windowsMenu, newDg, path, this);
	}

	protected void changeDiagramPath(String path)
	{
		int			i, cnt;
		JMenuItem	item;

		cnt  = m_windowsMenu.getItemCount();
		for (i = 0; i < cnt; ++i) {
			item = m_windowsMenu.getItem(i);
			if (item instanceof MyWindowsMenuItem) {
				if ( ((MyWindowsMenuItem) item).isDiagram(m_diagram) ) {
					((MyWindowsMenuItem) item).setPath(path);
					return;
	}	}	}	}

	protected void activeDiagram(Diagram diagram)
	{
		int			i, cnt;
		JMenuItem	item;

		cnt  = m_windowsMenu.getItemCount();
		for (i = 0; i < cnt; ++i) {
			item = m_windowsMenu.getItem(i);
			if (item instanceof MyWindowsMenuItem) {
				((MyWindowsMenuItem) item).activeDiagram(diagram);
	}	}	}

	protected Diagram getOpenDiagram(String path)
	{
		int			i, cnt;
		JMenuItem	item;

		cnt  = m_windowsMenu.getItemCount();
		for (i = 0; i < cnt; ++i) {
			item = m_windowsMenu.getItem(i);
			if (item instanceof MyWindowsMenuItem) {
				Diagram diagram = ((MyWindowsMenuItem) item).getDiagram(path);
				if (diagram != null) {
					return diagram;
		}	}	}
		return null;
	}

	private Diagram newDiagram()
	{
		Diagram diagram = new Diagram(this);

		diagram.addTaListener(m_legendBox,		TaListener.INSTANCELISTENER);
		diagram.addTaListener(m_mapBox,			TaListener.SCHEMALISTENER);
		diagram.addTaListener(m_queryBox,		TaListener.SCHEMALISTENER);
		diagram.addTaListener(m_resultBox,		TaListener.ENTITYLISTENER);
		diagram.addTaListener(m_tocBox,			TaListener.ENTITYLISTENER);
		diagram.addTaListener(m_historyBox,		TaListener.ENTITYLISTENER);
		diagram.addTaListener(m_undoBox,		TaListener.DIAGRAMLISTENER);
		diagram.addTaListener(m_viewBox,		TaListener.DIAGRAMLISTENER);
		diagram.addTaListener(this,             TaListener.ENTITYLISTENER);		// To support Find set

		return diagram;
	}

	protected void showDescription(RelationInstance ri, boolean showOpens)
	{
		if (ri != m_currentEdge) {
			EntityInstance	src, dst;
			String			info;

			m_currentEdge      = ri;
			m_currentNameEntity= null;
			src                = ri.drawSrc();
			dst                = ri.drawDst();
			if (src == null) {
				info = "??null??";
			} else {
				info = Util.quoted(src.getEntityLabel());
			}
			info += " " + ri.getClassLabel() + " ";
			if (dst == null) {
				info += "??null??";
			} else {
				info += Util.quoted(dst.getEntityLabel());
			}
			showInfo(info);
		} 
	}

	protected void showDescription(EntityInstance e, boolean showOpens)
	{
		if (e != m_currentNameEntity) {
			m_currentNameEntity = e;
			m_currentEdge       = null;
			String str;

			if (e == null) {
				str = "";
			} else {
				EntityInstance pe = e.getContainedBy();
				if (pe != null) {
					str = pe.getEntityLabel() + " . " + e.getEntityLabel();
				} else {
					str = e.getEntityLabel();
				}
			}
			showInfo(str);
		}
		if (e != null) {

			if (m_currentDescEntity != e && (!e.isOpen() || showOpens) &&	e.getEntityClass() != null)	{
				String label = e.getEntityLabel();
				String title = e.getTitle(); 
				String desc = e.getDescription();

				if (desc == null) {
					desc = "The " + label + " " + e.getClassLabel() + ".";
				}
				String topline = " (" + e.getClassLabel() + (e.hasChildren() ? " - " + e.numChildren() + " items)" : ")" );
				if (title != null) {
					topline = title + topline;
				} else {
					topline = label + topline;
				}
				m_rightTextBoxTitle.setText(topline);
				m_rightTextBox.set(desc);
				m_currentDescEntity = e;
		}	}
	}

	public JMenuBar genMenu() 
	{ 
		JMenuBar			menuBar;
		JMenu				m;
		String				initfile;

		m_menuBar = menuBar = new JMenuBar();

		// Build File Menu

		menuBar.add(m_fileMenu);

		m = new JMenu("Edit");
		editMenuItem(m, this);
		menuBar.add(m);

		m = new JMenu("Layout");
		layoutMenuItem(m, this, isApplet());
		menuBar.add(m);

		m = new JMenu("Explore");
		exploreMenuItem(m, this, isApplet());
		menuBar.add(m);

		m = new JMenu("Options");
		optionsMenuItem(m, this);
		menuBar.add(m);

		m_windowsMenu = m = new JMenu("Windows");
		menuBar.add(m);

		m = new JMenu("Help");
		helpMenuItem(m, this, "Editor");
		menuBar.add(m);

		setMenuFont(FontCache.getMenuFont());

		// Must do before loading a diagram
		loadLseditHistory(m, null);

//		dump_menubar(menuBar);

		return menuBar;
	}

	protected void setMenuFont(Font font)
	{
		FontCache.setMenuFont(font);
		FontCache.setMenuFont((MenuElement) m_menuBar, font);
	}
	
	protected void setTabsFont(Font font)
	{
		m_rightTabbedPane.setFont(font);
	}
	
	public void fontsChanged(Option oldOptions, Option newOptions)
	{
		ResultBox	resultBox        = null;
		LegendBox	legendBox        = null;
		QueryBox	queryBox         = null;
		boolean		diagram_changed  = false;
		Font		oldFont, newFont;
		int			i;
		
		for (i = 1; i <= Option.FONT_LAST; ++i) {
			oldFont = oldOptions.getTargetFont(i);
			newFont = newOptions.getTargetFont(i);
			
			if (oldFont != newFont) {
				switch (i) {
				case Option.FONT_CLOSED:
					EntityInstance.setClosedFont(newFont);
					IconCache.setElisionIconFont(newFont);
					diagram_changed = true;
					break;
				case Option.FONT_SMALL:
					EntityInstance.setSmallFont(newFont);
					diagram_changed = true;
					break;
				case Option.FONT_OPEN:
					EntityInstance.setOpenFont(newFont);
					diagram_changed = true;
					break;
				case Option.FONT_CLIENTS:
					ClientSupplierSet.setTextFont(newFont);
					diagram_changed = true;
					break;
				case Option.FONT_CARDINALS:
					Cardinal.setTextFont(newFont);
					diagram_changed = true;
					break;
				case Option.FONT_RESULTS_TITLE:
					resultBox = m_resultBox;
					resultBox.setTitleFont(newFont);
					break;
				case Option.FONT_RESULTS_TEXT:
					resultBox = m_resultBox;
					resultBox.setTextFont(newFont);
					break;
				case Option.FONT_LEGEND_TITLE:
					legendBox = m_legendBox;
					legendBox.setTitleFont(newFont);
					break;
				case Option.FONT_LEGEND_TEXT:
					legendBox = m_legendBox;
					legendBox.setTextFont(newFont);
					break;
				case Option.FONT_QUERY_TITLE:
					queryBox = m_queryBox;
					queryBox.setTitleFont(newFont);
					break;
				case Option.FONT_QUERY_TEXT:
					queryBox = m_queryBox;
					queryBox.setTextFont(newFont);
					break;
				case Option.FONT_TOC:
					m_tocBox.textFontChanged(newFont);
					break;
				case Option.FONT_UNDO:
					m_undoBox.textFontChanged(newFont);
					break;
				case Option.FONT_HISTORY:
					m_historyBox.textFontChanged(newFont);
					break;
				case Option.FONT_VIEWS:
					m_viewBox.textFontChanged(newFont);
					break;
				case Option.FONT_MAP:
					m_mapBox.textFontChanged(newFont);
					break;
				case Option.FONT_TEXTBOX_TITLE:
					m_leftTextBoxTitle.setFont(newFont);
					m_rightTextBoxTitle.setFont(newFont);
					break;
				case Option.FONT_TEXTBOX_TEXT:
					m_leftTextBox.setFont(newFont);
					m_rightTextBox.setFont(newFont);
					break;
				case Option.FONT_FEEDBACK:
					m_feedback.setFont(newFont);
					m_nameBox.setFont(newFont);
					repaintFeedback();
					break;
				case Option.FONT_MENU:
					setMenuFont(newFont);
					break;
				case Option.FONT_DIALOG:
					FontCache.setDialogFont(newFont);
					break;
				case Option.FONT_CLIPBOARD:
					m_clipboardBox.textFontChanged(newFont);
					break;
				case Option.FONT_ATTR_TEXT:
					m_attributeBox.textFontChanged(newFont);
					break;
				case Option.FONT_EDGE_LABEL:
					RelationLabel.setEdgeLabelFont(newFont);
					diagram_changed = true;
					break;
				case Option.FONT_TABS:
					setTabsFont(newFont);
					break;
		}	}	}

		if (resultBox != null) {
			resultBox.fontChanged();
		}

		if (legendBox != null) {
			legendBox.fontChanged();
		}
		
		if (queryBox != null) {
			queryBox.fontChanged();
		}

		if (diagram_changed) {
			refillDiagram();
		}
	}

/*
	Rough sketch of layout

	MENU BAR
    Buttons x x x                                     ****
	*********************** ******************* ***********
	*  Leftbox			  * * Right box		  * *		  *
	*********************** ******************* *		  *
											    *		  *
	******Feedback********* * Edge under mouse* * TABBED  *
	                                            * TABLE   *
	******************************************* *		  *
	*										  * *		  *
	*	    			DIAGRAM				  * *		  *
	*										  * *		  *
	******************************************* ***********
*/
/*
	protected void dumpSizes(String name, JComponent c)
	{
		if (c != null) {
			Dimension d;
			
			System.out.print("  " + name);
			if (c.isMinimumSizeSet()) {
				d = c.getMinimumSize();
				System.out.print(" min=" + d.width +"x" + d.height);
			}
			if (c.isPreferredSizeSet()) {
				d = c.getPreferredSize();
				System.out.print(" pre=" + d.width +"x" + d.height);
			}
			if (c.isMaximumSizeSet()) {
				d = c.getMaximumSize();
				System.out.print(" max=" + d.width +"x" + d.height);
			}
			d = c.getSize();
			System.out.println(" size=" + d.width +"x" + d.height);
	}	}

	protected void dumpGUI(String step)
	{
		System.out.println(step);
		dumpSizes("LeftInfoPanel ", m_leftInfoPanel);
		dumpSizes("RightInfoPanel", m_rightInfoPanel);
		dumpSizes("Diagram       ", m_diagram);
		dumpSizes("TabbedPane    ", m_rightTabbedPane);
		dumpSizes("InfoSplitPane ", m_infoSplitPane);
		dumpSizes("InfoDiagram   ", m_infoDiagramSplitPane);
		dumpSizes("DiagramTab    ", m_diagramTabSplitPane);
	}
*/	
	protected void genMainGUI() 
	{
		// The division between the left and right information above the diagram
		m_infoSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		// Split between information above the diagram and the diagram
		m_infoDiagramSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		// Split between things to left of tab boxes and tab boxes
		m_diagramTabSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		Option		options        = Options.getDiagramOptions();
		boolean		showToolbar    = options.isShowToolbar();
		Container	contentPane    = m_contentPane;
		int			width          = contentPane.getWidth();					// Width of panel
		int			height         = contentPane.getHeight();					// Height of panel
		int			toolbar_width  = width-2*GAP;								// Width of toolbar
		int			usable_height  = height - ToolBarButton.HEIGHT;				// Height of panel left after removal of Buttons
		int			divider1       = m_diagramTabSplitPane.getDividerSize();
		int			diagram_width  = (int) (width  * m_mainSplitRatio) - divider1;
		int			divider3       = m_infoSplitPane.getDividerSize();
		int			left_width     = (diagram_width - divider3) / 2;			// Width of each left information part above diagram
		int			right_width    = diagram_width - divider3 - left_width;		// Width of each right information part above diagram
		int			info_height    = (int) (usable_height * m_secondSplitRatio);// Height of information part above diagram
		int			box_height     = info_height - 40;							// Size of the central info box 
		int			divider2       = m_infoDiagramSplitPane.getDividerSize();
		int			diagram_height = usable_height - info_height - divider2;	// Height of diagram
		int			tab_width      = width - diagram_width;						// Width of tabs

//		System.out.println("LandscapeEditorCore width=" + width + " height=" + height);

		contentPane.setVisible(false);

		// Handle the tool bar

		m_toolBar = new JToolBar();
		m_toolBar.setRollover(true);

		m_toolButton     = new ToolBarButton[17];
		m_toolButton[0]  = new Find_Button(this);	
		m_toolButton[1]  = new Query_f_Button(this);
		m_toolButton[2]  = new Query_b_Button(this); 
		m_toolButton[3]  = new Query_C_Button(this);
		m_toolButton[4]  = new Query_Clear_Button(this);
		m_toolButton[5]  = new Elision_c_Button(this);
		m_toolButton[6]  = new Elision_I_Button(this); 
		m_toolButton[7]  = new Elision_u_Button(this);
		m_toolButton[8]  = new Elision_CU_Button(this); 
		m_toolButton[9]  = new Elision_s_Button(this);
		m_toolButton[10] = new Elision_CS_Button(this);
		m_toolButton[11] = new FontSmallerButton(this);
		m_toolButton[12] = new FontBiggerButton(this);
		m_toolButton[13] = new HistoryPrev_Button(this);
		m_toolButton[14] = new HistoryNext_Button(this);
		m_toolButton[15] = new FindPrev_Button(this);
		m_toolButton[16] = new FindNext_Button(this);

		m_toolButton[NEXT_FIND_BUTTON].setEnabled(false);
		m_toolButton[PREV_FIND_BUTTON].setEnabled(false);
		m_toolButton[NEXT_HISTORY_BUTTON].setEnabled(false);
		m_toolButton[PREV_HISTORY_BUTTON].setEnabled(false);

		for (int i = 0; i< m_toolButton.length ; ++i) {
			 m_toolBar.add(m_toolButton[i]);
		}
		m_toolBar.setFloatable(false);
		
		m_toolBar.setSize(toolbar_width, ToolBarButton.HEIGHT);
//		m_toolBar.setPreferredSize();

		contentPane.add(m_toolBar, BorderLayout.NORTH);
		m_toolBar.setVisible(showToolbar);
		
		// Handle the informational display above the diagram

		{
			Font	textBoxTitleFont = Option.getDefaultFont(Option.FONT_TEXTBOX_TITLE);
			

			m_leftInfoPanel = new JPanel(new BorderLayout());
//			m_leftInfoPanel.setMinimumSize(new Dimension(100,50));
//			m_leftInfoPanel.setSize(left_width, info_height);
			m_leftInfoPanel.setPreferredSize(new Dimension(left_width, info_height));
			m_leftInfoPanel.setVisible(true);

			m_leftTextBoxTitle = new JLabel("");
			m_leftTextBoxTitle.setSize(left_width, 20);
//			m_leftTextBoxTitle.setPreferredSize(d);
			m_leftTextBoxTitle.setBackground(Diagram.boxColor);
			m_leftTextBoxTitle.setForeground(TextBox.titleColor);
			m_leftTextBoxTitle.setFont(textBoxTitleFont);
			m_leftInfoPanel.add(m_leftTextBoxTitle, BorderLayout.NORTH);
			
			m_scrollLeftTextBox = new JScrollPane();
			m_scrollLeftTextBox.setSize(left_width, box_height);
//			m_scrollLeftTextBox.setPreferredSize(d);
			m_leftTextBox = new TextBox(m_scrollLeftTextBox, m_leftTextBoxHelp);
			m_leftTextBox.setSize(left_width, box_height);
//			m_leftTextBox.setPreferredSize(d);
			m_leftInfoPanel.add(m_scrollLeftTextBox,BorderLayout.CENTER);
			
			m_feedback = new Feedback(m_feedbackHelp);
			m_feedback.setSize(left_width, 20);
//			m_feedback.setPreferredSize(d);
			m_leftInfoPanel.add(m_feedback, BorderLayout.SOUTH);
			
			m_rightInfoPanel = new JPanel(new BorderLayout());
			m_rightInfoPanel.setSize(right_width, info_height);
			m_rightInfoPanel.setPreferredSize(new Dimension(right_width, info_height));
//			m_rightInfoPanel.setMinimumSize(new Dimension(100,50));
			m_rightInfoPanel.setVisible(true);

			m_rightTextBoxTitle = new JLabel("");
			m_rightTextBoxTitle.setSize(right_width,20);
//			m_rightTextBoxTitle.setPreferredSize(d);
			m_rightTextBoxTitle.setBackground(Diagram.boxColor);
			m_rightTextBoxTitle.setForeground(TextBox.titleColor);
			m_rightTextBoxTitle.setFont(textBoxTitleFont);
			m_rightInfoPanel.add(m_rightTextBoxTitle, BorderLayout.NORTH);
			
			m_scrollRightTextBox = new JScrollPane();
			m_scrollRightTextBox.setSize(right_width, box_height);
//			m_scrollRightTextBox.setPreferredSize(d);
			m_rightTextBox = new TextBox(m_scrollRightTextBox, m_rightTextBoxHelp);
			m_rightTextBox.setSize(right_width, box_height);
//			m_rightTextBox.setPreferredSize(d);
			m_rightInfoPanel.add(m_scrollRightTextBox, BorderLayout.CENTER);
			
			m_nameBox  = new Feedback(m_nameBoxHelp);
			m_nameBox.setSize(right_width, 20);
//			m_nameBox.setPreferredSize(d);
			m_rightInfoPanel.add(m_nameBox, BorderLayout.SOUTH);

			computeMinInfoHeight();
			
			// The division between the left and right information above the diagram
			m_infoSplitPane.setLeftComponent(m_leftInfoPanel);
			m_infoSplitPane.setRightComponent(m_rightInfoPanel);
//			m_infoSplitPane.setSize(diagram_width, info_height);
//			m_infoSplitPane.setPreferredSize(new Dimension(diagram_width, info_height));
			m_infoSplitPane.setOneTouchExpandable(true);
			m_infoSplitPane.setDividerLocation(left_width);	
			m_infoSplitPane.setResizeWeight(0.5);
		}

		// Handle the right tabs (Must be done before the diagram since these listen to diagram)

		{
			m_rightTabbedPane = new RightTabbedPane(this);
			m_rightTabbedPane.setMinimumSize(new Dimension(100,50));
//			m_rightTabbedPane.setPreferredSize(new Dimension(tab_width, usable_height));
			m_rightTabbedPane.setSize(tab_width, usable_height);
			m_legendBox    = new LegendBox(this,    m_rightTabbedPane);
			m_queryBox     = new QueryBox(this,     m_rightTabbedPane);
			m_resultBox    = new ResultBox(this,    m_rightTabbedPane);
			m_mapBox       = new MapBox(this,       m_rightTabbedPane);
			m_tocBox       = new TocBox(this,       m_rightTabbedPane);
			m_undoBox      = new UndoBox(this,      m_rightTabbedPane);
			m_historyBox   = new HistoryBox(this,   m_rightTabbedPane);
			m_clipboardBox = new ClipboardBox(this, m_rightTabbedPane);
			m_attributeBox = new AttributeBox(this, m_rightTabbedPane);
			m_viewBox      = new ViewBox(this,      m_rightTabbedPane);

//			m_legendBox.setSize(tab_width, usable_height);
//			m_queryBox.setSize(tab_width, usable_height);
//			m_resultBox.setSize(tab_width, usable_height);
//			m_mapBox.setSize(tab_width, usable_height);
//			m_tocBox.setSize(tab_width, usable_height);
//			m_undoBox.setSize(tab_width, usable_height);
//			m_historyBox.setSize(tab_width, usable_height);
//			m_clipboardBox.setSize(tab_width, usable_height);
//			m_attributeBox.setSize(tab_width, usable_height);
//			m_viewBox.setSize(d);
/*
			m_legendBox.setPreferredSize(d);
			m_mapBox.setPreferredSize(d);
			m_queryBox.setPreferredSize(d);
			m_resultBox.setPreferredSize(d);
			m_tocBox.setPreferredSize(d);
			m_rightTabbedPane.setPreferredSize(d);
			m_undoBox.setPreferredSize(d);
			m_historyBox.setPreferredSize(d);
			m_clipboardBox.setPreferredSize(d);
			m_attributeBox.setPreferredSize(d);
*/
			setTabsFont(FontCache.getMenuFont());
		}

		// Handle the scroll diagram

		{
			m_scrollDiagram = new JScrollPane(); 
			m_scrollDiagram.setSize(diagram_width, diagram_height);
			m_scrollDiagram.setPreferredSize(new Dimension(diagram_width, diagram_height));
			m_diagram = newDiagram();
			m_diagram.setSize(diagram_width, diagram_height);
//			m_diagram.setPreferredSize(new Dimension(diagram_width, diagram_height));
			
			m_scrollDiagram.setViewportView(m_diagram);
			m_diagram.initialZoom();
			
			// Split between information above the diagram and the diagram
			m_infoDiagramSplitPane.setTopComponent(m_infoSplitPane);
			m_infoDiagramSplitPane.setBottomComponent(m_scrollDiagram);
			m_infoDiagramSplitPane.setMinimumSize(new Dimension(100,50));
			m_infoDiagramSplitPane.setPreferredSize(new Dimension(diagram_width, usable_height));
			m_infoDiagramSplitPane.setSize(diagram_width, usable_height);
//			m_infoDiagramSplitPane.setPreferredSize(d);
			m_infoDiagramSplitPane.setOneTouchExpandable(true);
			m_infoDiagramSplitPane.setDividerLocation(info_height);
			m_infoDiagramSplitPane.setResizeWeight(0.0);
		}

		
		// Split between things to left of tab boxes and tab boxes
		m_diagramTabSplitPane.setLeftComponent(m_infoDiagramSplitPane);
		m_diagramTabSplitPane.setRightComponent(m_rightTabbedPane);
		m_diagramTabSplitPane.setSize(width, usable_height);
		m_diagramTabSplitPane.setPreferredSize(new Dimension(width, usable_height));
		m_diagramTabSplitPane.setOneTouchExpandable(true);
		m_diagramTabSplitPane.setDividerLocation(diagram_width);
		m_diagramTabSplitPane.setResizeWeight(1.0);

		contentPane.add(m_diagramTabSplitPane, BorderLayout.CENTER);

		contentPane.setVisible(true);
 	}

	protected void genModeHandlers() 
	{
		m_editModeHandler = new EditModeHandler(this);
	}

	// Generate top of screen GUI 

	protected void computeMinInfoHeight()
	{
		Dimension d;

		int	left  = 0;
		int	right = 0;

		if (m_leftTextBoxTitle != null && m_leftTextBoxTitle.isVisible()) {
			d = m_leftTextBoxTitle.getMinimumSize();
			left += d.height;
		}
		if (m_scrollLeftTextBox != null && m_scrollLeftTextBox.isVisible()) {
			d = m_scrollLeftTextBox.getMinimumSize();
			left += d.height;
		}
		if (m_feedback != null && m_feedback.isVisible()) {
			d = m_feedback.getMinimumSize();
			left += d.height;
		}
		d = new Dimension(100, left);
		m_leftInfoPanel.setMinimumSize(d);

		if (m_rightTextBoxTitle != null && m_rightTextBoxTitle.isVisible()) {
			d = m_rightTextBoxTitle.getMinimumSize();
			right += d.height;
		}
		if (m_scrollRightTextBox != null && m_scrollRightTextBox.isVisible()) {
			d = m_scrollRightTextBox.getMinimumSize();
			right += d.height;
		}
		if (m_nameBox != null && m_nameBox.isVisible()) {
			d = m_nameBox.getMinimumSize();
			right += d.height;
		}
		d = new Dimension(100, right);
		m_rightInfoPanel.setMinimumSize(d);
	}

	protected void setLeftBox() 
	{
		Diagram diagram = m_diagram;
		
		if (diagram != null) {
			EntityInstance root = diagram.getDrawRoot();

			if (root != null) {
				String label = root.getEntityLabel();
				String title = root.getTitle();
				String desc  = root.getDescription();
				String topline;

				if (desc == null) {
					if (root.getContainedBy() == null) {
						desc = "The " + label + " landscape.";
					} else {
						desc = "The " + label + " " + root.getClassLabel();
				}	}

				if (title != null) {
					topline = title + " (" + label + ")";
				} else {
					topline = label;
				}
				m_leftTextBoxTitle.setText(topline);
				m_leftTextBox.set(desc);
		}	}
	}

	protected void changingDiagram()
	{
		Diagram diagram = m_diagram;
		
		if (diagram != null) {
			diagram.signalDiagramChanging(diagram);
	}	}
	
	protected void changeDiagram(Diagram diagram) 
	{
		Diagram	oldDiagram = m_diagram;
		String	name, diagramName;
		int		i;

//		System.out.println("LandscapeEditorCore.changeDiagram " + m_scrollDiagram.getBounds());

		m_editModeHandler.cleanup();

		if (oldDiagram != null) {
			oldDiagram.removeEntitiesFromCache();
		}
		if (diagram != null) {
			diagram.addEntitiesToCache();
		}
		setDiagram(diagram);
		activeDiagram(diagram);
		clipboardChanged();

		m_scrollDiagram.setViewportView(diagram);
		if (diagram != null) {
			diagram.initialZoom();
			diagramName = diagram.getContextName();
			if (diagramName == null) {
				doFeedback("No diagram");
			} else {
				doFeedback("Set to: " + diagramName);
			}
			diagram.signalDiagramChanged(diagram, TaListener.DIAGRAM_CHANGED);
		}
		setLeftBox();
	}

	private boolean loadDiagram(Diagram diagram, String file)
	{
		EntityInstance	startEntity  = null;
		EntityInstance	parentEntity = null;
		
		changingDiagram();
		
		String			rc = diagram.loadDiagram(file);
		EntityInstance	e;

		if (rc != null) {
			error("Load failed (" + rc + ") for: " + file);
			System.out.println("Load failed (" + rc + ") for: " + file);
			return(false);
		} 

		changeDiagram(diagram);
		e = diagram.getRootInstance();
		if (m_startEntity != null) {
			startEntity = diagram.getCache(m_startEntity);
			if (startEntity == null) {
				System.out.println("Start Entity not found: '" + m_startEntity + "'");		// IJD
			} else {
				e = startEntity;
				if (m_forward >= 0) {
					parentEntity = startEntity.getContainedBy();
					if (parentEntity == null) {
						System.out.println("Parent entity of '" + m_startEntity + "' not found");
					} else {
						System.out.println("Doing " + m_forward + " step for " + e);
						m_forwardEntity = startEntity;
						e               = parentEntity;
			}	}	}
			m_startEntity = null;
		}
		if (e != null) {
			diagram.navigateTo(e, false);
			addHistoryEntity(e);
		} 
		return true;
	}
		
	public void loadLs1(String file, String drawRoot) 
	{
		Diagram newDg = newDiagram();

		if (file == null) {
			doFeedback("No diagram");
			newDg.noDiagram();
		} else if (file.length() < 1) {
			changingDiagram();
			doFeedback("New Diagram");
			newDg.emptyDiagram();
			changeDiagram(newDg);
		} else {
			doFeedback("Reading: " + file + " in background");
			if (!loadDiagram(newDg, file)) {
				return;
			}
			if (drawRoot != null) {
				EntityInstance e = m_diagram.getCache(drawRoot);

				if (e != null) {
					navigateTo(e, false);
		}	}	}
		rememberDiagram(newDg, file);
		this.requestFocus();
		repaint();
	}

	public void loadLs(String file, String drawRoot, boolean async)
	{
		if (async) {
			LoadLsSwingWorker worker = new LoadLsSwingWorker(this, file, drawRoot);
			worker.start();
		} else {
			loadLs1(file, drawRoot);
	}	}

	public void switchDiagram(Diagram newDg, String lsPath, String drawRoot, boolean async)
	{
		Diagram	old;
		

		old = m_diagram;
		if (old != newDg) {
			IconCache.clearElisionCache();
			if (newDg == null) {
				loadLs(lsPath, drawRoot, async);
				return;
			}
			changingDiagram();
			changeDiagram(newDg);
			newDg.revalidate();
		}
		if (drawRoot != null) {
			EntityInstance e = m_diagram.getCache(drawRoot);
			if (e != null) {
				navigateTo(e, false);
	}	}	}	
	
	protected void attach(String lsPath, String drawRoot) 
	{
		Diagram	attachDiagram;

		attachDiagram = getOpenDiagram(lsPath);
		switchDiagram(attachDiagram, lsPath, drawRoot, false);
	}

	public String getParameter(String name) 
	{
		String ret;
		
		if (isApplet()) {
			ret = m_applet.getParameter(name);
		} else {
			ret = System.getProperty(name);
		} 
		return ret;
	}	

	public void mapboxChanged(EntityInstance e)
	{
		m_mapBox.setDrawRoot(e);
	}

	public void zoomChanged()
	{
		m_diagram.zoomChanged();
	}
	
	public void refillDiagram()
	{
		m_diagram.revalidate();
	}
	
	public void repaintDiagram()
	{
		m_diagram.repaint();
	}
	
	public void refillToolbar()
	{
		m_toolBar.revalidate();
	}
	
	public void showToolbarChanged()
	{
		Option		options        = Options.getDiagramOptions();
		boolean		showToolbar    = options.isShowToolbar();

		if (showToolbar) {
			refillToolbar();
		}
		m_toolBar.setVisible(showToolbar);
	}
	
	public void iconPathChanged()
	{
		Diagram diagram = m_diagram;
		
		// Have to clear the cache so don't see old icon paths
		IconCache.clear();
		
		if (diagram != null) {
			diagram.iconPathChanged();
	}	}
	
	public void iconFixedChanged()
	{
		Diagram diagram = m_diagram;
		
		if (diagram != null) {
			diagram.iconPathChanged();
	}	}
	
	public String filePrompt(String banner, String default_file, int mode, FileFilter[] filters) 
	{
		File		file;
		FileFilter	filter;
		String		name;
		int			select, ret;

		if (isApplet()) {
			name = JOptionPane.showInputDialog("Which file do you wish to upload from the server"); 
		} else {
			JFileChooser fd = new JFileChooser(".");

			fd.setDialogTitle(banner);
			if (mode == FA_LOAD_DIR) {
				select = JFileChooser.DIRECTORIES_ONLY;
			} else {
				select = JFileChooser.FILES_ONLY;
			}
			fd.setFileSelectionMode(select);
			if (default_file != null) {
				fd.setSelectedFile(new File(default_file));
			}
			if (filters != null) {
				for (int i = 0; i < filters.length; ++i) {
					fd.addChoosableFileFilter(filters[i]);
			}	} 

			if (mode != FA_SAVE) {
				ret = fd.showOpenDialog(m_contentPane);
			} else {
				ret = fd.showSaveDialog(m_contentPane);
			}
			if (ret == JFileChooser.APPROVE_OPTION) {
				file = fd.getSelectedFile();
				name = file.getAbsolutePath();
			} else {
				name = null;
		}	}
		return name;
	}

	protected void getBrowser()
	{
		JFileChooser	fileChooser = new JFileChooser();

		String	os              = Version.Detail("os.name");
		String	defaultFilename = null;
		String	browser         = null;
		File	defaultfile, startfile, file;
		int		ret;

		if (os != null) {
			if (os.startsWith("Windows")) {
				defaultFilename = "C:\\Program Files\\Internet Explorer\\IEXPLORE.EXE";
		}	}

		if (defaultFilename != null) {
			defaultfile = new File(defaultFilename);
		} else {
			defaultfile = null;
		}

		fileChooser.setDialogTitle("Identify internet browser");
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setApproveButtonText("Select");

		if (m_browser == null) {
			startfile = defaultfile;
		} else {
			startfile = new File(m_browser);
		}

		if (startfile != null) {
			fileChooser.setSelectedFile(startfile);
		}

		if (defaultfile != null) {
			fileChooser.setAccessory(new DefaultBrowserButton(fileChooser, os, defaultfile));
		}
		ret = fileChooser.showOpenDialog(m_contentPane);

		if (ret == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
			browser = file.getAbsolutePath();
			if (browser.equals(m_browser)) {
				return;
			}
			m_browser = browser;
	}	}

	protected void showURL(String urlName, int target) 
	{
		MsgOut.vprintln("URL: " + urlName + " - target: " + target);

		try {
			URL newURL;
			
			if (!isApplet()) {
				try {
					BrowserLauncher.openURL(urlName);
				} catch (Exception e) {
					System.out.println("Unable to execute BrowserLauncher " + e.getMessage());

					String[]	argv    = new String[2];
					String		browser = m_browser;

					if (browser == null) {
						getBrowser();
						browser = m_browser;
						if (browser == null) {
							return;
					}	}

					argv[0] = browser;
					argv[1] = urlName;

					try {
						Runtime runtime = Runtime.getRuntime();
						runtime.exec(argv);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(m_frame, 	"Unable to execute " + argv[0] + " " + e1.getMessage(), "Browser error", JOptionPane.ERROR_MESSAGE | JOptionPane.OK_OPTION);
					}
				}
 
			} else {
				newURL = new URL(m_documentBase, urlName);
				if (m_ac == null) {
					m_ac = m_applet.getAppletContext();
				}
				// URLConnection urlCon = newURL.openConnection();
				// urlCon.connect();

				switch(target) {
				case LsLink.TARGET_TOP:
					m_ac.showDocument(newURL, "_top");
					break;
				case LsLink.TARGET_HELP:
					m_ac.showDocument(newURL, "_blank");
					break;
				case LsLink.TARGET_FRAME:
					m_ac.showDocument(newURL, "Map");
					break;
				case LsLink.TARGET_LIST:
					m_ac.showDocument(newURL, "List");
					break;
			}	}
		} catch (MalformedURLException ex) {
			MsgOut.println("Malformed URL: " + urlName + " " + ex.getMessage());
		}
	}

	static int[] g_showSourceKeys = 
	{
		Do.SHOW_SOURCECODE,
		Do.SHOW_SOURCECODE2,
		Do.SHOW_SOURCECODE3,
		Do.SHOW_SOURCECODE4,
		Do.SHOW_SOURCECODE5,
		Do.SHOW_SOURCECODE6,
		Do.SHOW_SOURCECODE7,
		Do.SHOW_SOURCECODE8
	};


	protected void showSource(Object object, int key)
	{
		int index;

		if (isApplet()) {
			error("Applets can't execute external commands");
			return;
		}
		if (object == null) {
			if (m_diagram != null) {
				object = m_diagram.targetEntityRelation(object);
			} else {
				error("No diagram");
		}	}
		if (object == null) {
			return;
		}
		if (!(object instanceof LandscapeObject)) {
			error(object + " not a LandscapeObject");
			return;
		} 

		for (index = 0; index < g_showSourceKeys.length; ++index) {
			if (key == g_showSourceKeys[index]) {
				break;
		}	}
		
		ExecuteAction.onObject(this, index, (LandscapeObject) object);
	}
	
	protected void about() 
	{
		if (m_aboutURL != null) {
			showURL(m_aboutURL, LsLink.TARGET_HELP);
		}
	}

	protected void help() 
	{
		if (m_helpURL != null) {
			showURL(m_helpURL, LsLink.TARGET_HELP);
		}
	}

	protected void setVisibility(JComponent bd, boolean state) 
	{
		if (bd != null) {
			bd.setVisible(state);
		}
	} 

	protected void readyMsg() 
	{
		doFeedback(getTitle() + " " + Version.MAJOR + "." + Version.MINOR + " (build " + Version.BUILD + ") Started.");
	}

	public void setLsPath(String file)
	{
		if (m_lsPath == null) {
			m_lsPath = file;
		} else {
			addLseditHistory(file);
	}	}
	
	protected void followURL(String url, int target) 
	{
//		System.out.println("LandscapeEditorCore.followURL: " + target);

		switch (target) {
		case LsLink.TARGET_APP:
			attach(url, null);		//  Treat the url as the name of a TA file to be displayed
			break;
		case LsLink.TARGET_NEW:
			if (m_frame != null) {
				// Running under a frame -- not an applet
				LandscapeEditorFrame af = LandscapeEditorFrame.create();
				af.setLsPath(url);	// Set the other applications path (not ours)
				af.launch(null);
				m_openFrames++;
				break;
			}
		default:
			showURL(url, target);
	}	}

	public void clearFeedback() 
	{
		m_feedback.set("");
	}

	protected void fitToLabel() 
	{
		Vector v = startGroupOp();
		
		if (v == null) {
			return;
		}

		Diagram			diagram = m_diagram;
		EntityInstance	e;
		EntityComponent	entityComponent;
		Component		parentComponent;
		Dimension		ld;
		Rectangle		curLayout;
		int				i, width, height, parentWidth, parentHeight;
		double			relWidth, relHeight;


		for (i = v.size(); --i >= 0; ) {
			e = (EntityInstance) v.elementAt(i);

			ld = e.getLabelDim(getGraphics(), EntityInstance.REG_FONT);

			width  = ld.width + EntityComponent.MARGIN*2;	// Margin both sides
			height = ld.height+ EntityComponent.MARGIN*2;

			if (e.hasChildren()) {
				width += EntityComponent.CONTENTS_FLAG_X_RESERVE;
				if (height < EntityComponent.CONTENTS_FLAG_Y_RESERVE) {
					height = EntityComponent.CONTENTS_FLAG_Y_RESERVE;
				}
			}

			entityComponent = e.getEntityComponent();
			parentComponent = entityComponent.getParent();
			parentWidth     = parentComponent.getWidth();
			parentHeight    = parentComponent.getHeight();

			if (parentWidth <= 0.0) {
				relWidth = 0.1;
			} else {
				relWidth    = ((double) width)/((double) parentWidth);
			}
			if (parentHeight <= 0.0) {
				relHeight = 0.1;
			} else {
				relHeight   = ((double) height)/((double) parentHeight);
			}
			diagram.updateSizeRelLocal(e, relWidth, relHeight);
		}
	}

	protected void avoidCollisions()
	{
		Vector v = startGroupOp();
		
		if (v == null) {
			return;
		}


		int	size = v.size();

		if (size == 0) {
			return;
		}

		Diagram		diagram = getDiagram();
		double[]	x       = new double[size];
		double[]	y       = new double[size];
		double[]	width   = new double[size];
		double[]	height  = new double[size];
		int[]		next    = new int[size];
		int[]		prior   = new int[size];

		int				i, j, k;
		EntityInstance	e, e1;
		double			xi, yi, widthi, heighti, xj, yj, widthj, shift, max;


		for (i = 0; i < size; ++i) {
			e = (EntityInstance) v.elementAt(i);
			x[i]      = e.xRelLocal();
			y[i]      = e.yRelLocal();
			width[i]  = e.widthRelLocal();
			height[i] = e.heightRelLocal();
			next[i]   = -1;
			prior[i]  = -1;
		}

		for (i = 0; i < size; ++i) {
			xi      = x[i];
			yi      = y[i];
			widthi  = width[i];
			heighti = height[i]; 
			for (j = i+1; j < size; ++j) {

				yj = y[j];
				if (yj >= yi + heighti) {
					continue;
				}
				if (yj +  height[j] <= yi) {
					continue;
				}

				xj = x[j];
				if (xi <= xj) {
					// i is to the left of j
					k = prior[j];
					if (k == -1 || (xi + widthi) > (x[k] + width[k])) {
						prior[j] = i;
					}
					k = next[i];
					if (k == -1 || xj < x[k]) {
						next[i] = j;
					}	
				} else {
					// i is to the right of j
					k = prior[i];
					if (k == -1 || (xj + width[j]) > (x[k] + width[k])) {
						prior[i] = j;
					}
					k = next[j];
					if (k == -1 || xi < x[k]) {
						next[j] = i;
		}	}	}	}	

/*
		for (i = 0; i < size; ++i) {
			e = (EntityInstance) v.elementAt(i);
			k = prior[i];
			if (k >= 0) {
				e1 = (EntityInstance) v.elementAt(k);
				System.out.println(e1 + " prior " + e);
			}
			k = next[i];
			if (k >= 0) {
				e1 = (EntityInstance) v.elementAt(k);
				System.out.println(e + " next " + e1);
		}	}
*/				
				
		for (i = 0; i < size; ++i) {
			for (j = i; (k = prior[j]) >= 0; j = k) {
				// Overlap on left
				shift = x[k] + width[k] - x[j];
				if (shift < 0) {
					break;
				}
				e = (EntityInstance) v.elementAt(k);
//				System.out.println(e.toString() + (x[k] - shift - 0.001) + "<-" + x[k]);
				x[k] -= (shift + 0.001);
				if (x[k] <= 0.0) {
					x[k] = 0.0;
					break;
		}	}	}

		for (i = 0; i < size; ++i) {
			for (j = i; (k = next[j]) >= 0; j = k) {
				// Overlap on right
				shift = x[j] + width[j] - x[k];
				if (shift < 0.0) {
					break;
				}
				shift += 0.001;
				max = 1.0 - x[k] - width[k];
				if (shift >= max) {
					shift = max;
				}
				if (shift <= 0.0) {
					break;
				}
				e = (EntityInstance) v.elementAt(k);
//				System.out.println(e.toString() + x[k] + "->" + x[k] + shift);

				x[k] += shift;
		}	}

		for (i = 0; i < size; ++i) {
			e = (EntityInstance) v.elementAt(i);
			if (x[i] != e.xRelLocal()) {
//				System.out.println(e + "{" + x[i] + "," + y[i] + "," + width[i] + "x" + height[i] + "}");
				diagram.updateRelLocal(e, x[i], y[i], width[i], height[i]);
		}	}
	}

	protected Vector startGroupOp() 
	{
		Vector grp = m_diagram.getGroupedEntities();

		if (grp == null) {
			error("Group not selected");
			return null;
		}
		return grp;
	}

	// Alignment

	protected void alignTop() 
	{
		Vector grp = startGroupOp();

		if (grp == null) {
			return;
		}

		// Align group based on alignment to key entity

		Diagram			diagram = m_diagram;
		EntityInstance	ke      = diagram.getKeyEntity();
		double			top     = ke.yRelLocal();
		int				size    = grp.size();
		EntityInstance	e;
		int				i;

		for (i = size; --i >= 0; ) {
			e = (EntityInstance) grp.elementAt(i);
			if ((top + e.heightRelLocal()) > 1.0) {
				top = 1.0 - e.heightRelLocal();
		}	}

		for (i = size; --i >= 0; ) {
			e = (EntityInstance) grp.elementAt(i);
			diagram.updateYRelLocal(e, top);
		}
	}

	protected void alignHorizontal() 
	{
		Vector grp = startGroupOp();

		if (grp == null) {
			return;
		}

		// Align group based on alignment to key entity

		Diagram			diagram = m_diagram;
		EntityInstance	ke      = diagram.getKeyEntity();
		double			center  = ke.yRelLocal() + (ke.heightRelLocal()/2);
		int				size    = grp.size();
		EntityInstance	e;
		int				i;

		for (i = size; --i >= 0; ) {
			e = (EntityInstance) grp.elementAt(i);
			if ((center + (e.heightRelLocal()/2.0)) > 1.0) {
				center = 1.0 - (e.heightRelLocal()/2.0);
		}	}

		for (i = size; --i >= 0; ) {
			e = (EntityInstance) grp.elementAt(i);
			if ((center - (e.heightRelLocal()/2.0)) < 0) {
				center = (e.heightRelLocal()/2.0);
		}	}

		for (i = size; --i >= 0; ) {
			e = (EntityInstance) grp.elementAt(i);
			diagram.updateYRelLocal(e, center - (e.heightRelLocal()/2.0));
		}
	}

	protected void alignBottom() 
	{
		Vector	grp     = startGroupOp();

		if (grp == null) {
			return;
		}

		// Align group based on alignment to key entity

		Diagram			diagram = m_diagram;
		EntityInstance	ke      = diagram.getKeyEntity();
		double			bottom  = ke.yRelLocal() + ke.heightRelLocal();
		int				size    = grp.size();
		EntityInstance	e;
		int				i;

		for (i = size; --i >= 0; ) {
			e = (EntityInstance) grp.elementAt(i);
			if ((bottom - e.heightRelLocal()) < 0) {
				bottom = e.heightRelLocal();
		}	}

		for (i = size; --i >= 0; ) {
			e = (EntityInstance) grp.elementAt(i);
			diagram.updateYRelLocal(e, bottom - e.heightRelLocal());
		}
	}

	protected void alignLeft() 
	{
		Vector grp = startGroupOp();

		if (grp == null) {
			return;
		}

		// Align group based on alignment to key entity

		Diagram			diagram = m_diagram;
		EntityInstance	ke      = diagram.getKeyEntity();
		double			left    = ke.xRelLocal();
		int				size    = grp.size();
		EntityInstance	e;
		int				i;

		for (i = size; --i >= 0; ) {
			e = (EntityInstance) grp.elementAt(i);
			if ((left + e.widthRelLocal()) > 1.0) {
				left = 1.0 - e.widthRelLocal();
		}	}

		for (i = size; --i >= 0; ) {
			e = (EntityInstance) grp.elementAt(i);
			diagram.updateXRelLocal(e, left);
		}
	}

	protected void alignVertical() 
	{
		Vector grp = startGroupOp();

		if (grp == null) {
			return;
		}

		// Align group based on alignment to key entity

		Diagram			diagram = m_diagram;
		EntityInstance	ke      = diagram.getKeyEntity();
		double			center  = ke.xRelLocal() + (ke.widthRelLocal()/2);
		int				size    = grp.size();
		EntityInstance	e;
		int				i;

		for (i = size; --i >= 0; ) {
			e = (EntityInstance) grp.elementAt(i);
			if ((center + (e.widthRelLocal()/2.0)) > 1.0) {
				center = 1.0 - (e.widthRelLocal()/2.0);
		}	}

		for (i = size; --i >= 0; ) {
			e = (EntityInstance) grp.elementAt(i);
			if ((center - (e.widthRelLocal()/2.0)) < 0) {
				center = (e.widthRelLocal()/2.0);
		}	}

		for (i = size; --i >= 0; ) {
			e = (EntityInstance) grp.elementAt(i);
			diagram.updateXRelLocal(e, center - (e.widthRelLocal()/2.0));
		}
	}

	protected void alignRight() 
	{
		Vector grp = startGroupOp();

		if (grp == null) {
			return;
		}

		// Align group based on alignment to key entity

		Diagram			diagram = m_diagram;
		EntityInstance	ke      = diagram.getKeyEntity();
		double			right   = ke.xRelLocal() + ke.widthRelLocal();
		int				size    = grp.size();
		EntityInstance	e;
		int				i;

		for (i = size; --i >= 0; ) {
			e = (EntityInstance) grp.elementAt(i);
			if ((right - e.widthRelLocal()) < 0) {
				right = e.widthRelLocal();
		}	}

		for (i = size; --i >= 0; ) {
			e = (EntityInstance) grp.elementAt(i);
			diagram.updateXRelLocal(e, right - e.widthRelLocal());
		}
	}

	protected void sameWidth() 
	{
		Vector grp = startGroupOp();

		if (grp == null)
			return;

		// Size group based on size of key entity

		Diagram			diagram = m_diagram;
		EntityInstance	ke      = diagram.getKeyEntity();
		double			width   = ke.widthRelLocal();
		int				i;

		for (i = grp.size(); --i >= 0; ) {
			EntityInstance e = (EntityInstance) grp.elementAt(i);

			if (e != ke) {
				diagram.updateWidthRelLocal(e, width);
			}
		}
	} 

	protected void sameHeight() 
	{
		Vector grp = startGroupOp();

		if (grp == null)
			return;

		// Size group based on size of key entity

		Diagram			diagram = m_diagram;
		EntityInstance	ke      = diagram.getKeyEntity();
		double			height  = ke.heightRelLocal();
		int				i;

		for (i = grp.size(); --i >= 0; ) {
			EntityInstance e = (EntityInstance) grp.elementAt(i);
			if (e != ke) {
				diagram.updateHeightRelLocal(e, height);
			}
		}
	} 

	protected void sameSize() 
	{
		Vector grp = startGroupOp();

		if (grp == null)
			return;

		// Size group based on size of key entity

		Diagram			diagram = m_diagram;
		EntityInstance	ke      = diagram.getKeyEntity();
		Rectangle		klyt    = ke.getDiagramBounds();
		double			width   = ke.widthRelLocal();
		double			height  = ke.heightRelLocal();
		int				i;

		for (i = grp.size(); --i >= 0; ) {
			EntityInstance e = (EntityInstance) grp.elementAt(i);

			if (e != ke) {
				diagram.updateSizeRelLocal(e, width, height);
			}
		}
	} 

	protected void equalHorizontalSpacing() 
	{
		Vector			grp   = startGroupOp();
		double			space;
		double			xRelLocal;
		EntityInstance	e;
		int				size, i;

		if (grp == null) {
			return;
		}

		// First and last form boundary

		size = grp.size();
		if (size < 3) {
			error("Minimum group of three required");
			return;
		}

		SortVector.byHorizontalPosition(grp);

		Diagram		diagram = m_diagram;

		e         = (EntityInstance) grp.elementAt(0);	// First element
		xRelLocal = e.xRelLocal() + e.widthRelLocal();
		i         = size - 1;
		e         = (EntityInstance) grp.elementAt(i);	// Last element
		space     = e.xRelLocal() - xRelLocal;

		for (; --i > 0; ) {
			e = (EntityInstance) grp.elementAt(i);
			space -= e.widthRelLocal();
		}
		
		// Space now represents the spare space between end of first entity and start of last
		space /= (size - 1);
		// Ignore last entity
		--size;

		for (i = 1; i < size; ++i) {
			e = (EntityInstance) grp.elementAt(i);
			xRelLocal += space;
			diagram.updateXRelLocal(e, xRelLocal);
			xRelLocal += e.widthRelLocal();
		}
	}

	protected void equalVerticalSpacing() 
	{
		Vector			grp   = startGroupOp();
		double			space;
		double			yRelLocal;
		EntityInstance	e;
		int				size, i;

		if (grp == null) {
			return;
		}

		// First and last form boundary

		size = grp.size();
		if (size < 3) {
			error("Minimum group of three required");
			return;
		}

		SortVector.byVerticalPosition(grp);

		Diagram		diagram = m_diagram;

		e         = (EntityInstance) grp.elementAt(0);
		yRelLocal = e.yRelLocal() + e.heightRelLocal();	// Bottom of top component
		i         = size - 1;
		e         = (EntityInstance) grp.elementAt(i);
		space     = e.yRelLocal() - yRelLocal;			// Distance from bottom of top component to top of bottom component

		for (; --i > 0; ) {
			e = (EntityInstance) grp.elementAt(i);
			space -= e.heightRelLocal();
		}

		// Space to share

		// Ignore last entity
		--size;
		space /= (double) size;

		for (i = 1; i < size; ++i) {
			e = (EntityInstance) grp.elementAt(i);
			yRelLocal += space;
			diagram.updateYRelLocal(e, yRelLocal);
			yRelLocal += e.heightRelLocal();
		}
	}

	protected void updateCreateContainedGroup() 
	{
		Vector grp = startGroupOp();

		if (grp == null) {
			return;
		}

		Diagram			diagram = m_diagram;
		EntityInstance	pe      = ((EntityInstance) grp.firstElement()).getContainedBy();
		EntityInstance	ne, e;
		EntityComponent	entityComponent;
		double			minX, minY, maxX, maxY, x, y;
		int				i;

		// Determine the initial relLocal bounding box of whole group

		minX = minY = Double.MAX_VALUE;
		maxX = maxY = Double.MIN_VALUE;

		for (i = grp.size(); --i >= 0; ) {
			e               = (EntityInstance) grp.elementAt(i);
			entityComponent = e.getEntityComponent();
			x               = e.xRelLocal();
			y               = e.yRelLocal();

			x               = entityComponent.getDiagramX();
			y               = entityComponent.getDiagramY();

			if (x < minX) {
				minX = x;
			}
			if (y < minY) {
				minY = y;
			} 
			x              += e.widthRelLocal();
			y              += e.heightRelLocal();
			if (x > maxX) {
				maxX = x;
			}
			if (y > maxY) {
				maxY = y;
		}	}

		beginUndoRedo("New Entity Container");

		ne = diagram.updateNewEntity(null, pe);

		diagram.updateRelLocal(ne, minX, minY, maxX-minX, maxY-minY);

		for (i = grp.size(); --i >= 0; ) {
			e = (EntityInstance) grp.elementAt(i);
			diagram.updateMoveEntityContainment(ne, e);
		}
		endUndoRedo();
	}
		
	protected void saveStatus(String stat) 
	{
		doFeedback("Save status: " + stat);
	}


	// Save the landscape in the URL

	protected String doSaveByURL(String lsSaveURL) 
	{
		URL		lsURL;

		try {
			lsURL = new URL(lsSaveURL);
		}
		catch(MalformedURLException e) {
		   return "Malformed URL on write";
		}

		URLConnection urlCon;

		try {
			urlCon = lsURL.openConnection();

			urlCon.setDoInput(false);
			urlCon.setDoOutput(true);
			urlCon.setAllowUserInteraction(false);
		}
		catch (IOException e) {
			return "Couldn't open URL connection " + e.getMessage();
		}

		OutputStream os;

		try {
			os = urlCon.getOutputStream();
		}
		catch (IOException e) {
			return "Couldn't open URL Output Stream " + e.getMessage();
		}

		return m_diagram.saveDiagram(os, false);
	}

	protected void saveByURL(String overridePath) 
	{
		MsgOut.dprintln("Save by URL");

		URL lsURL;

		String str = doSaveByURL(m_lsSavePath);

		if (str != null) {
			saveStatus(str);
			return;
		}

		if (m_lsSaveCmd == null) {
			saveStatus("Success");
			return;
		}

		try {
			lsURL = new URL(m_lsSaveCmd);
		}
		catch(MalformedURLException e) {
		   saveStatus("Malformed URL on write");
		   return;
		}

		InputStream is;
		try {
			is = lsURL.openStream();
		}
		catch (IOException e) {
			saveStatus("Couldn't openStream for store " + e.getMessage());
			return;
		}

		try {

			InputStreamReader	reader         = new InputStreamReader(is);
			BufferedReader		bufferedReader = new BufferedReader(reader);

         	str = bufferedReader.readLine();

			is.close();

			saveStatus(str);
		}
		catch(IOException e) {
			saveStatus("IOexception on store " + e.getMessage());
		}
	}

	protected String saveByFile(String newPath) 
	{
		String ret = m_diagram.saveByFile(newPath);
		if (ret == null && newPath != null) {
			changeDiagramPath(newPath);
		}
		return ret;
	}

	protected String saveWithCmd(String saveSuffix, String cmdTemplate)
	{
		File file   = (File) m_diagram.getContext();
		String path = file.getPath() + saveSuffix;
		File nfile  = new File(path);
		FileOutputStream os;	

		// Save away the monolithic TA file

		try {
			os = new FileOutputStream(nfile);
		}
		catch(IOException e) {
			return "IOException creating output stream " + e.getMessage();
		}
		
		String ret = m_diagram.saveDiagram(os, true);
		
		if (ret != null) {
			return ret;
		}
	
		// Run command after save

		String cmd = Util.expand(cmdTemplate, file.getName(), this);

		// System.out.println("Save command: " + cmd);

		String rc;

		try {
			Runtime rt = Runtime.getRuntime();

			Process p = rt.exec(cmd);

			InputStreamReader	reader         = new InputStreamReader(p.getErrorStream());
			BufferedReader		bufferedReader = new BufferedReader(reader);
          
			rc = bufferedReader.readLine();

			if (rc.equals("Done"))
				rc = "Save succeeded";

			p.destroy();
		}
		catch (Exception e) {
			rc = "Exec failed for: " + cmd;
		}

		return rc;
	}

	protected void doSaveLs(String overridePath) 
	{
		doFeedback("Saving landscape...");

		if (m_diagram.getContext() instanceof URL) {
			saveByURL(overridePath);
		}
		else {
			String rc;

			if (overridePath == null && m_lsSaveCmd != null) {
				MsgOut.dprintln("Save by app");
				rc = saveWithCmd(m_lsSaveSuffix, m_lsSaveCmd);
			} else {
				MsgOut.dprintln("Save by file");
				rc = saveByFile(overridePath); 
			}

			if (rc == null) {
				rc = "Success";
			}
			saveStatus(rc);
		}
	}

	protected void saveLs() 
	{
		Object context = m_diagram.getContext();
		String txt = null;

		if (context == null) {
			txt = filePrompt("Save Landscape", m_diagram.getAbsolutePath(), FA_SAVE, null);
			if (txt == null || txt.length() == 0) {
				return;
		}	}
		doSaveLs(txt);
	}

	protected static final String indAdd = "  ";

	protected void initialLoad() 
	{
		EntityInstance	startEntity  = null;
		int				forward      = m_forward;
		
		String lsPath	= m_lsPath;
		Diagram	diagram = m_diagram;

		setDiagram(diagram);

		if (lsPath == null) {
			MsgOut.dprintln("Empty landscape");
			if (m_leftTextBox != null) {
				m_leftTextBoxTitle.setText("Empty Landscape");
				m_leftTextBox.set("Select 'Open landscape' from 'File' menu to load a new landscape.");
			}
			readyMsg();
		} else if (diagram != null) {

			MsgOut.dprintln("Load: " + lsPath);
			if (!loadDiagram(diagram, lsPath)) {		// Initial load
				return;
			} 
			setLeftBox();
			readyMsg();
		}	

		if (m_rightTabbedPane != null && m_rightTabbedPane.isVisible()) {
			if (!m_legendBox.isActive()) {
				m_legendBox.activate();
		}	}

		requestFocus();
		rememberDiagram(m_diagram, lsPath);
		activeDiagram(m_diagram);
	}

	// Called to initialize component

	public void init_core(int diagramPercentWidth, int diagramPercentHeight) 
	{
		m_contentPane.setBackground(Color.lightGray);
//		m_contentPane.setBackground(Color.GREEN);

		// We must generate the mode handlers before the Diagram
		genModeHandlers();
		if (diagramPercentWidth > 0 && diagramPercentHeight > 0) {
			// Specifies as percentages of total width x total height size of diagram
			m_secondSplitRatio = (double) (1.0 - (diagramPercentHeight / 100.0));
			m_mainSplitRatio   = (double) (diagramPercentWidth / 100.0);
		}		
		genMainGUI();

		initialLoad();
		showInfo("");
	}

	// Show the results in res as red boxes

	protected void goTo(Vector res) 
	{
		Diagram	diagram = m_diagram;
		int	i;

		doFeedback("");

		diagram.clearFlags(true);

		EntityInstance e  = (EntityInstance) res.elementAt(0);
		EntityInstance pe = e.getContainedBy();

		SortVector.byString(res);
		m_resultBox.showResults("FIND RESULTS:", res, "-- Find step --");
		m_resultBox.activate();
		m_rightTextBoxTitle.setText("");
		m_rightTextBox.set("");

		navigateTo(pe, false);

		for (i = res.size(); --i >= 0; ) {
			e = (EntityInstance) res.elementAt(i);
			diagram.setRedBoxFlag(e);
		}
	}

	// Called by Cntl.F3

	protected void findNext() 
	{
		if (m_findResults == null) {
			error("No search has occured.");
		} else if (m_findResults.isEmpty()) {
			error("No entities found which match search pattern.");
		} else {
			JButton button = m_toolButton[PREV_FIND_BUTTON];
			boolean change = false;
			boolean	state  = !m_findResults.atBeginning();

			if (button.isEnabled() != state) {
				button.setEnabled(state);
				change = true;
			}

			Vector result = m_findResults.nextResult();

			state  = !m_findResults.atEnd();
			button = m_toolButton[NEXT_FIND_BUTTON];
			if (button.isEnabled() != state) {
				button.setEnabled(state);
				change = true;
			}
			if (change) {
				refillToolbar();
			}
			if (result == null) {
				error("No more results available.");
			} else {
				goTo(result);
	}	}	}

	// Called by Cntl F

	protected void find() 
	{
		Diagram		diagram       = m_diagram;
		FindBox		findBox       = new FindBox(getFrame(), this, m_diagram);
		FindRules	findRules     = findBox.getFindRules();
		findBox.dispose();


		if (findRules == null) {
			error("No meaningful find rules provided");
		} else {
			m_findResults = new Find(diagram.getRootInstance(), findRules);

			if (m_findResults.isEmpty()) {
				error("No entities found which match search pattern.");
			} else {
				if (diagram.clearHighlighting(true)) {
					diagram.revalidate();
				}
				m_findResults.reset();
				findNext();
				if (!m_findResults.atEnd()) {
					m_rightTextBox.set("Use F3 to step forward through layers found and F2 to step back");
	}	}	}	}

	// Called by Cntl.F2

	protected void findPrev() 
	{
		Vector result;

		if (m_findResults == null) {
			error("No search has occured.");
		} else if (m_findResults.isEmpty()) {
			error("No search results.");
		} else {
			if (!m_findResults.regress()) {
				error("At first result.");
			} else {
				findNext();
	}	}	}

	public RelationInstance updateNewRelation(EntityInstance from, EntityInstance to)
	{
		RelationInstance ri;

		beginUndoRedo("New Relation");
		ri = m_diagram.updateNewRelation(null, from, to);
		endUndoRedo();
		return ri;
	}

	protected boolean deleteContainer(EntityInstance e)
	{
		Diagram	diagram = m_diagram;
		boolean ret;
		
		diagram.clearEntityGroupFlag(e);	// No repaint

		beginUndoRedo("Delete Container " + e);

		ret = diagram.updateDeleteContainer(e);
		if (ret) {
			doFeedback("Container " + e.getEntityLabel() + " has been deleted.");
		} else {
			doFeedback("Container " + e.getEntityLabel() + " can't be deleted.");
		}
		endUndoRedo();

		return ret;
	}

	protected boolean removeDead(EntityInstance e)
	{
		Diagram	diagram = m_diagram;
		boolean ret;
		
		diagram.clearEntityGroupFlag(e);	// No repaint

		beginUndoRedo("Remove Dead " + e);

		ret = diagram.updateRemoveDead(e);
		if (ret) {
			doFeedback("Unreachable from " + e.getEntityLabel() + " deleted.");
		} else {
			doFeedback("Unreachable from " + e.getEntityLabel() + " can't be deleted.");
		}
		endUndoRedo();

		return ret;
	}

	protected boolean testForClose(boolean windowClosing) 
	{
		if (isApplet()) {
			return false;
		}
		if (m_diagram.getChangedFlag()) {
			Util.beep();

			int	option = (windowClosing ? JOptionPane.YES_NO_OPTION : JOptionPane.YES_NO_CANCEL_OPTION);
			int rc = JOptionPane.showConfirmDialog(m_frame, "Landscape has been changed.\nShould it be saved?", "Landscape Changed", option);

			switch(rc) {
			case JOptionPane.YES_OPTION:
				saveLs();
				// Drop through
			case JOptionPane.NO_OPTION:
				break;
			default:
				if (!windowClosing) {
					return false;
		}	}	}
		saveLseditHistory();
		return true;
	}

	protected void sleep(int ms) 
	{
		try {
			Thread.sleep(ms);
		} catch(Exception e) {
		}
	}

	protected void navigateTo(EntityInstance e, boolean mustbeContainer) 
	{
		m_diagram.navigateTo(e, mustbeContainer);
		setRightTextBox("", "");
	}

	public void navigateToDrawRootParent()
	{
		Diagram diagram = m_diagram;

		if (diagram != null) {
			EntityInstance e = diagram.getDrawRootParent();

			if (e == null) {
				error("At topmost landscape");
			} else {
				// Going up
				navigateTo(e, false);
	}	}	}

	public void followLink(EntityInstance e, boolean mustbeContainer) 
	{
		Attribute	attr = e.getLsAttribute(EntityInstance.NAVLINK_ID);

		m_editModeHandler.cleanup();
		m_diagram.clearFlags(true);

		if (attr == null) {
			navigateTo(e, mustbeContainer);
			return;
		}
		// Custom navigation rules
		attr.followLink(this, e, mustbeContainer);
	}

	protected void shiftDeltaFont(int delta) 
	{
		Diagram			diagram = m_diagram;
		Vector			grp     = diagram.getGroupedEntities();
		EntityInstance	ge;
		String			msg;

		if (grp != null && !grp.isEmpty()) {
			int	i;
			
			for (i = grp.size(); --i >= 0;) {
				ge = (EntityInstance) grp.elementAt(i);
				diagram.shiftDeltaFont(ge, delta);
			}
			msg = "Selected entities font changed";
		} else { 
			ge = m_diagram.getDrawRoot();
			if (ge == null) {
				msg = "No entities exist";
			} else {
				diagram.shiftDeltaFont(ge, delta);
				msg = "Draw root font changed";
		}	} 
		doFeedback(msg);
	}

	protected void redboxEntities() 
	{
		Diagram			diagram = m_diagram;
		Vector			grp     = diagram.getGroupedEntities();
		EntityInstance	ge;
		String			msg;

		if (grp != null && !grp.isEmpty()) {
			int	i;
			
			for (i = grp.size(); --i >= 0;) {
				ge = (EntityInstance) grp.elementAt(i);
				diagram.setRedBoxFlag(ge);
			}
			msg = "Selected redbox entities changed";
		} else { 
			msg = "No entities exist";
		}
		doFeedback(msg);
	}
	
	public void show_groupList() 
	{
		Diagram diagram = m_diagram;
		String	msg;

		if (diagram != null) {
			msg = diagram.show_groupList(m_resultBox);
			if (msg != null) {
				doFeedback(msg);
	}	}	}

	public void groupAll() 
	{
		Diagram			diagram = m_diagram;
		String	msg;

		if (diagram != null) {
			msg = diagram.groupAll();
			if (msg != null) {
				doFeedback(msg);
	}	}	}

	public JFrame getFrame()
	{
		return(m_frame);
	}

	public JPanel getContentPane()
	{
		return((JPanel) m_contentPane);
	}

	public Feedback getFeedbackBox()
	{
		return m_feedback;
	}

	public Feedback getNameBox()
	{
		return m_nameBox;
	}

	public Diagram getDiagram() 
	{
		return m_diagram;
	}

	protected void setDiagram(Diagram diagram)
	{
		Option	diagramOptions = diagram.getDiagramOptions();
		
		m_diagram = diagram;

		Options.setDiagramOptions(diagramOptions);
		if (m_handicapped > 0) {		
			Font	font = FontCache.get("Dialog", Font.BOLD, m_handicapped);
			diagramOptions.setTargetFont(Option.FONT_ALL, font, true, true, false);
		}
		diagram.setClipboardListener(m_clipboardBox);
		diagram.setUndoListener(this);
	}

	public TocBox getTocBox() 
	{
		return 	m_tocBox;
	}

	public UndoBox getUndoBox()
	{
		return m_undoBox;
	}

	public LegendBox getLegendBox() 
	{
		return 	m_legendBox;
	}

	public MapBox getMapBox()
	{
		return m_mapBox;
	}

	public QueryBox getQueryBox()
	{
		return m_queryBox;
	}

	public ResultBox getResultBox()
	{
		return m_resultBox;
	}

	public AttributeBox getAttributeBox()
	{
		return m_attributeBox;
	}
	
	public ClipboardBox getClipboardBox() 
	{
		return 	m_clipboardBox;
	}

	public HistoryBox getHistoryBox()
	{
		return m_historyBox;
	}
	
	public ViewBox getViewBox()
	{
		return m_viewBox;
	}
	
	public TextBox	getLeftTextBox()
	{
		return m_leftTextBox;
	}
	
	public TextBox	getRightTextBox()
	{
		return m_rightTextBox;
	}
	
	// Returns previous cursor so can stack/unstack

	public int getCursor()
	{
		return m_curCursor;
	}

	public int setCursor(int value) 
	{
		int	ret = m_curCursor;

		if (ret != value) {
			Cursor cursor = Cursor.getPredefinedCursor(value);

			m_curCursor = value;
			if (m_frame != null) {
				m_frame.setCursor(cursor);
			} else {
				m_applet.setCursor(cursor);
			}
/*
			System.out.println("LandscapeEditorCore.setCursor to " + m_frame.getCursor().getName() + "=" + cursor);
			java.lang.Thread.dumpStack();
			System.out.println("-----");
 */
		}
		return ret;
	}

	public void setRightTextBox(String title, String description)
	{
		m_rightTextBoxTitle.setText(title);
		m_rightTextBox.set(description);
	}

	public Enumeration enumEntityClasses() {

		return m_diagram.enumEntityClasses(); 
	}

	public Enumeration enumEntityClassesInOrder() {

		return m_diagram.enumEntityClassesInOrder(); 
	}

	public Enumeration enumRelationClassesInOrder() {

		return m_diagram.enumRelationClassesInOrder(); 
	}

	public void beginUndoRedo(String name)
	{
		if (m_diagram != null) {
			m_diagram.beginUndoRedo(name);
	}	}

	public void endUndoRedo()
	{
		if (m_diagram != null) {
			m_diagram.endUndoRedo();
	}	}

	public Vector getEdits()
	{
		if (m_diagram != null) {
			return m_diagram.getEdits();
		}
		return null;
	}

	public UndoableEdit getEditToBeRedone()
	{
		return m_diagram.getEditToBeRedone();
	}

	public int countEdits()
	{
		return m_diagram.countEdits();
	}

	public void massChange(UndoableEdit undoableEdit, boolean redo)
	{
		if (m_diagram != null) {
			m_diagram.massChange(undoableEdit, redo);
			doFeedback("Changes " + (redo ? "redone" : "undone"));
	}	}

	public void discardAllEdits()
	{
		if (m_diagram != null) {
			m_diagram.discardAllEdits();
	}	}
	
	public boolean clearUndoCache()
	{
		Vector v  = getEdits();
		boolean ret;

		if (v == null || v.size() == 0) {
			ret = true;
		} else {
			int ret1 = JOptionPane.showConfirmDialog(getFrame(), "Confirm deletion of undo history?", "Delete Undo History", JOptionPane.OK_CANCEL_OPTION);

			ret = (ret1 == JOptionPane.OK_OPTION);
			if (ret) {
				discardAllEdits();
				m_undoBox.fill();
		}	}
		return ret;
	}

	public boolean clearAllUndoCaches()
	{
		int			i, cnt;
		JMenuItem	item;
		Diagram		diagram;
		Vector		v;
		int			ret1;
		boolean		ret  = false;
		
		cnt  = m_windowsMenu.getItemCount();
		for (i = 0; i < cnt; ++i) {
			item = m_windowsMenu.getItem(i);
			if (!(item instanceof MyWindowsMenuItem)) {
				continue;
			}
			diagram = ((MyWindowsMenuItem) item).getDiagram();
			v       = diagram.getEdits();
			if (v == null || v.size() == 0) {
				continue;
			}
			if (!ret) {
				ret1 = JOptionPane.showConfirmDialog(getFrame(), "Confirm deletion of all undo histories to free memory?", "Delete All Undo Histories", JOptionPane.OK_CANCEL_OPTION);
				if (ret1 != JOptionPane.OK_OPTION) {
					break;
				}
				ret  = true;
			}
			diagram.discardAllEdits();
		}	
		if (ret) {
			m_undoBox.fill();
		}
		return ret;
	}
	
	public int getDiagramX()
	{
		return(m_scrollDiagram.getX());
	}

	public int getDiagramY()
	{
		return(m_scrollDiagram.getY());
	}
	
	public Point
	getDiagramViewPosition()
	{
		JViewport	viewport = m_scrollDiagram.getViewport();
		
		return viewport.getViewPosition();
	} 

	protected void edgeNavigateSrc(Object object) 
	{
		Diagram			 diagram = getDiagram();
		RelationInstance ri      = diagram.targetRelation(object);

		if (ri == null) {
			return;
		}
		followLink(ri.getSrc(), true);
	}

	protected void edgeNavigateDst(Object object) 
	{
		Diagram			 diagram = getDiagram();
		RelationInstance ri      = diagram.targetRelation(object);

		if (ri == null) {
			return;
		}
		followLink(ri.getDst(), true);
	}

	public void setHistoryButtons(HistoryBox historyBox)
	{
		JButton		button     = m_toolButton[PREV_HISTORY_BUTTON];
		boolean		change     = false;
		boolean		state      = historyBox.hasPrevious();

		if (button.isEnabled() != state) {
			button.setEnabled(state);
			change = true;
		}
		state  = historyBox.hasNext();
		button = m_toolButton[NEXT_HISTORY_BUTTON];
		if (button.isEnabled() != state) {
			button.setEnabled(state);
			change = true;
		}
		if (change) {
			refillToolbar();
	}	}

	public void addHistoryEntity(EntityInstance e)
	{
		HistoryBox historyBox = m_historyBox;

		if (historyBox != null) {
			historyBox.addEntity(e);
			setHistoryButtons(historyBox);
	}	}

	protected void closeEditor()
	{

		if (testForClose(false)) {
			int					i, cnt;
			JMenuItem			item;
			MyWindowsMenuItem	menuItem;

			cnt  = m_windowsMenu.getItemCount();
			for (i = cnt; i > 0; ) {
				item = m_windowsMenu.getItem(--i);
				if (item instanceof MyWindowsMenuItem) {
					menuItem = (MyWindowsMenuItem) item;
					if (menuItem.isDiagram(m_diagram)) {
						m_windowsMenu.remove(i);
						break;
			}	}	}

			cnt  = m_windowsMenu.getItemCount();
			for (i = cnt; i > 0; ) {
				item = m_windowsMenu.getItem(--i);
				if (item instanceof MyWindowsMenuItem) {
					menuItem = (MyWindowsMenuItem) item;
					menuItem.actionPerformed(null);
					return;
			}	}	
			System.exit(0);
		}
	}

	public void clipboardChanged()
	{
		if (m_clipboardBox != null) {
			m_clipboardBox.clipboardChanged();
	}	}

	public void repaintFeedback()
	{
		computeMinInfoHeight();
		m_infoDiagramSplitPane.revalidate();
	}
	
	public void changeShowDesc(boolean show)
	{
		setVisibility(m_leftTextBoxTitle, show);
		setVisibility(m_scrollLeftTextBox, show);
		setVisibility(m_rightTextBoxTitle, show);
		setVisibility(m_scrollRightTextBox, show);
		repaintFeedback();
	}
		
	public void changeShowFeedback(boolean show)
	{
		setVisibility(m_feedback, show);
		setVisibility(m_nameBox,  show); 
		repaintFeedback();
	}
	
	public void changeFixScrollbars(boolean state)
	{
		if (state) {
			m_scrollDiagram.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			m_scrollDiagram.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			doFeedback("Diagram scroll bars permanently enabled");
		} else {
			m_scrollDiagram.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			m_scrollDiagram.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			doFeedback("Diagram scroll bars will appear as needed");
	}	}

	public void changeLeftTabbox(boolean state)
	{
		JComponent	left, right;
			
		m_diagramTabSplitPane.setLeftComponent(null);
		m_diagramTabSplitPane.setRightComponent(null);

		if (state) {
			left    = m_rightTabbedPane;
			right   = m_infoDiagramSplitPane;
		} else {
			left    = m_infoDiagramSplitPane;
			right   = m_rightTabbedPane;
		}

//		left.setMinimumSize(new Dimension(100,50));
//		right.setMinimumSize(new Dimension(100,50));
		m_diagramTabSplitPane.setLeftComponent(left);
		m_diagramTabSplitPane.setRightComponent(right);
		m_diagramTabSplitPane.setDividerLocation(left.getWidth());

//		If either min width is zero slider won't move
//		System.out.println("Min " + m_diagramTabSplitPane.getMinimumDividerLocation() + " Max " + m_diagramTabSplitPane.getMaximumDividerLocation()); 
	}
	
	public void changeTabsScroll(boolean state)
	{
		m_rightTabbedPane.setTabsScroll(state);
	}
		
	public void changeTraceLifted(boolean state)
	{
		m_queryBox.setTraceLifted(state);
	}

	public void changeGroupQuery(boolean state)
	{
		m_queryBox.setGroupQuery(state);
	}
	
	public void changeQueryPersists(boolean state)
	{
		m_queryBox.setQueryPersists(state);
	}
	
	public void repaintTOC()
	{
		if (m_tocBox != null) {
			m_tocBox.repaint();
	}	}
	
	public void changeTOC()
	{
		if (m_tocBox != null) {
			m_tocBox.fill();
	}	}
	
	public void changeLegendQuery()
	{
		if (m_legendBox != null) {
			m_legendBox.fill();
		}
		if (m_queryBox != null) {
			m_queryBox.fill();
	}	}

	public RelationClass newRelationClass()
	{
		Diagram			diagram = getDiagram();
		RelationClass	rc      = null;
	
		if (diagram != null) {
			beginUndoRedo("New relation class");
			rc = diagram.newRelationClass();
			endUndoRedo();
		}
		return rc;
	}

	public void duplicateEdges(RelationClass oldRc)
	{
		Diagram	diagram = getDiagram();

		if (diagram != null) {
			RelationClass newRc;
			
			beginUndoRedo("Duplicate Edges " + oldRc);
			newRc = diagram.newRelationClass();
			if (newRc != null) {
				diagram.duplicateEdges(oldRc, newRc);
			}
			endUndoRedo();
	}	}

	public void moveGroup(int key)
	{
		beginUndoRedo("Move");
		m_editModeHandler.moveGroup(key);
		endUndoRedo();
	}

	protected void newEntity(Object object)
	{
		Diagram			diagram = getDiagram();
		EntityInstance	pe, e;

		if (diagram == null) {
			error("No diagram to add entity to");
			return;
		} 
		pe = diagram.targetEntity(object);
		if (pe == null) {
			error("No entity to add new entity to");
			return;
		}
		beginUndoRedo("New Entity");
		e = diagram.updateNewEntity(null, pe);
		diagram.setInitialLocation(e, pe);
		endUndoRedo();
	}
	
	// This is a common entry point that permits recovery if the layout algorithm runs out of memory, etc.

	public void doLayout1(LandscapeLayouter layouter, Vector selectedBoxes, EntityInstance parent, boolean force)
	{
		Runtime	r     = Runtime.getRuntime();
		int		state = 0;
		Diagram	dg    = getDiagram();
		boolean	undo  = dg.undoEnabled();
		String	name;
	

		dg.beginUndoRedo(layouter.getName() + " layout");
		setCursor(Cursor.WAIT_CURSOR);
		
		try {
			for (; state < 2; ++state) {
				name = layouter.getName();
				try {
					r.gc();
					
					System.out.println(name + ": started");
					if (!layouter.doLayout1(selectedBoxes, parent)) {
						LandscapeLayouter layouter1;
						layouter1 = layouter.getFallback();
						state = 0;
						if (layouter1 == null) {
							System.out.println(name + ": failed and no fallback strategy");
							state = 2;
						} 
						layouter = layouter1;
						continue;
					}
					state = 2;
					System.out.println(name + ": finished");
				} catch (Throwable e) {
					System.out.print(name + ": " + e.getMessage());
					if (!force) {
						state = 2;
					}
					switch (state) {
					case 0:
						if (clearAllUndoCaches()) {
							if (undo) {
								dg.setUndoEnabled(false);
							}
							System.out.println(" - cleared caches");
							state = 1;
							continue;
						}
					case 1:
						layouter = layouter.getFallback();
		
						if (layouter != null) {
							System.out.println(" - fallback to " + layouter.getName());
							state = 0;
							continue;
						}
						System.out.println("");
						state = 2;
			}	}	}
		} catch (Throwable e) {
			System.out.println(e.getMessage());
		} finally {
			if (undo) {
				dg.setUndoEnabled(true);
			}
			setCursor(Cursor.DEFAULT_CURSOR);		
			dg.endUndoRedo();
	}	}

	public void navigateToLandscape(EntityInstance e, boolean forward)
	{
		String			lseditPath;
		String			lseditDrawRoot;

		if (forward) {
			lseditPath = e.getNavNext();
			if (lseditPath == null) {
				return;
			}
			lseditDrawRoot = e.getNavNextDrawRoot();
		} else {
			lseditPath = e.getNavPrev();
			if (lseditPath == null) {
				return;
			}
			lseditDrawRoot = e.getNavPrevDrawRoot();
		}
		attach(lseditPath, lseditDrawRoot);
	}
	
	// --------------
	// Event handling 
	// --------------

	public void processKey(int key, int modifiers, Object object) 
	{
//		System.out.println("LandscapeEditorCore:processKey(" + key + ", " + modifiers + ", " + object +")");

		String str; 

		if ((modifiers & Event.CTRL_MASK) != 0) {
			switch(key) {
			case Do.GROUP_ALL:
				groupAll();
				break;
			case Do.QUIT_PROGRAM:
				if (testForClose(false)) {
					System.exit(0);
				}
				break;
			case Do.CLOSE_LANDSCAPE:
				closeEditor();
				break;
			case Do.NEW_LANDSCAPE:
				loadLs("", null, false);	// Open new landscape
				break;
			case Do.REFRESH:
				refillDiagram();
				break;
			case Do.FIND_QUERY:
				find();
				break;
			case Do.SWITCH_TOC:
				if (m_tocBox != null) {
					m_tocBox.switch_TOC();
				}
				break;
			case Do.SET_TO_VIEWPORT:
				if (m_diagram.set_to_viewport()) {
					doFeedback( "Diagram set to viewport");
				}
				break;
			case Do.OPEN_LANDSCAPE:
			{
				Object context = null;
				String txt;

				if (m_diagram != null) {
					context = m_diagram.getContext();
				}
				if (context != null) {
					txt = filePrompt( "Load Landscape", m_diagram.getAbsolutePath(), FA_LOAD, null);
				} else {
					txt = filePrompt("Load Landscape", "", FA_LOAD, null);
				}
				if (txt != null) {
					loadLs(txt, null, true);	// Open landscape
				}
				break;
			}
			case Do.OPEN_SETTINGS:
			{
				String file = filePrompt("Load settings", defaultIniPath(), FA_LOAD, null);
				if (file != null) {
					loadLseditHistory(m_fileMenu, file);
				}
				refillDiagram();
				break;
			}
			case Do.SELECT_BROWSER:
				getBrowser();
				break;
			case Do.SELECT_COMMAND:
				ExecuteAction.configure(this, -1);
				break;
			case Do.CLEAR_ELISIONS:
				if (m_diagram != null) {
					int cnt = m_diagram.clearElisions(object);
					doFeedback("Cleared " + cnt + " elisions");
					if (cnt != 0) {
						refillDiagram();
				}	}
				break;
			case Do.PRINT_LANDSCAPE:
			{
				Vista vista = new Vista(this); 
				break;
			}
			case Do.SAVE_AS:
			{
				Object context = m_diagram.getContext();
				String txt;

				if (context != null) {
					txt = filePrompt("Save Landscape As", m_diagram.getAbsolutePath(), FA_SAVE, null);
				} else {
					txt = filePrompt("Save Landscape As", "", FA_SAVE, null);
				}
				if (txt != null && txt.length() != 0) {
					doSaveLs(txt);
				}
				break;
			}
			case Do.SAVE:
				saveLs();
				break;
			case Do.PASTE:
				m_diagram.pasteGroup(object);
				break;
			case Do.CUT_GROUP:
				m_diagram.cutGroup(object);
				break;
			case Do.UNDO:
				if (m_diagram != null) {
					m_diagram.undo();
				}
				break;
			case Do.REDO:
				if (m_diagram != null) {
					m_diagram.redo();
				}
				break;
			case Do.NAV_NEXT:
				if (object != null) {
					EntityInstance	e      = (EntityInstance) object;
					navigateToLandscape(e, true);
				}
				return;
			case Do.NAV_PREV:
				if (object != null) {
					EntityInstance	e      = (EntityInstance) object;
					navigateToLandscape(e, false);
				}
				return;
			}
		} else if ((modifiers & Event.ALT_MASK) != 0) {
			switch (key) {
			case Do.NAVIGATE_TO:
				if(object != null) {
					Diagram diagram = m_diagram;
					if (diagram != null) {
						navigateTo((EntityInstance) object, true);
				}	}
				break;
			case Do.EDGE_OPEN_LOW:
			case Do.EDGE_OPEN_SRC:
			case Do.EDGE_OPEN_DST:
			case Do.EDGE_CLOSE_LOW:
			case Do.EDGE_CLOSE_SRC:
			case Do.EDGE_CLOSE_DST:
			{		
				if (m_diagram != null) {
					m_diagram.handleEdgeExpansion(key, object, getResultBox());
				}
				break;
			}
			case Do.EDGE_NAVIGATE_SRC:
				edgeNavigateSrc(object);
				break;
			case Do.EDGE_NAVIGATE_DST:
				edgeNavigateDst(object);
				break;
			case Do.TOC_PATH:
				if (m_tocBox != null) {
					m_tocBox.toc_path();
				}
				break;
			case Do.A_HORIZ_TOP:
				beginUndoRedo("Align top");
				alignTop();
				endUndoRedo();
				break;
			case Do.A_HORIZ_CENTER:
				beginUndoRedo("Align horizontal center");
				alignHorizontal();
				endUndoRedo();
				break;
			case Do.A_HORIZ_BOTTOM:
				beginUndoRedo("Align bottom");
				alignBottom();
				endUndoRedo();
				break;
			case Do.A_VERTICAL_LEFT:
				beginUndoRedo("Align vertical left");
				alignLeft();
				endUndoRedo();
				break;
			case Do.A_VERTICAL_RIGHT:
				beginUndoRedo("Align vertical right");
				alignRight();
				endUndoRedo();
				break;
			case Do.A_VERTICAL_CENTER:
				beginUndoRedo("Align vertical center");
				alignVertical();
				endUndoRedo();
				break;
			case Do.A_FIT_LABEL:
				beginUndoRedo("Fit label");
				fitToLabel();
				endUndoRedo();
				break;
			case Do.A_GROUP:
				updateCreateContainedGroup();
				break;
			case Do.SZ_WIDTH:
				beginUndoRedo("Same width");
				sameWidth();
				endUndoRedo();
				break;
			case Do.SZ_HEIGHT:
				beginUndoRedo("Same height");
				sameHeight();
				endUndoRedo();
				break;
			case Do.SZ_WIDTH_HEIGHT:
				beginUndoRedo("Same size");
				sameSize();
				endUndoRedo();
				break;
			case Do.SPC_HORIZ:
				beginUndoRedo("Space horizontally");
				equalHorizontalSpacing();
				endUndoRedo();
				break;
			case Do.SPC_VERTICAL:
				beginUndoRedo("Space vertically");
				equalVerticalSpacing();
				endUndoRedo();
				break;
			case Do.RELAYOUT_ALL:
			{
				String msg = m_diagram.relayoutAll();
				if (msg != null) {
					doFeedback(msg);
				}
				break;
			}
			case Do.CLOSE_ALL:
			{
				String msg = m_diagram.closeAll();
				if (msg != null) {
					doFeedback(msg);
				}
				refillDiagram();
				break;
			}
			case Do.OPEN_ALL:
			{
				String msg = m_diagram.openAll();
				if (msg != null) {
					doFeedback(msg);
				}
				refillDiagram();
				break;
			}
			case Do.SET_FONT:
			{
				FontChooser.create(this);
				break;
			}
			case Do.DECREASE_MAG:
			case Do.INCREASE_MAG:
			{
				String			msg;

				if (m_diagram != null) {
					beginUndoRedo("Magnify");
					msg = m_diagram.scaleEntity(key, !m_editModeHandler.mouseIsDown());
					endUndoRedo();
					if (msg != null) {
						doFeedback(msg);
					}
					refillDiagram();
				}
				break;
			}
			case Do.NEW_ECLASS:
			{
				Diagram		   diagram = getDiagram();
				if (diagram != null) {
					diagram.newEntityClass();
				}
				break;
			}
			case Do.NEW_RCLASS:
			{
				newRelationClass();
				break;
			}
			case Do.VALIDATE_ALL:
				validateAll();
				break;
			case Do.EDIT_ELISIONS:
			{
				EditElisions dialog = new EditElisions(getFrame(), this, object);
				dialog.dispose();
				break;
			}
			case Do.REDBOX_ENTITY: 
			{
				if (object instanceof EntityInstance) {
					m_diagram.setRedBoxFlag((EntityInstance) object);
				} else {
					redboxEntities();
				}
				break;
			}
			case Do.CHECK_REFCNTS:
				if (m_diagram != null) {
					m_diagram.checkRefcnts(getResultBox());
				}
				break;
			case Do.REMOVE_DEAD:
			{
				Diagram			diagram = getDiagram();
				if (diagram != null) {
					EntityInstance	e       = diagram.targetEntity(object);
					if (e != null) {
						removeDead(e);
				}	}
				break;
			}
			}
		} else if (modifiers == Event.SHIFT_MASK) {
			// Only used for special keys
			switch (key) {
			case Do.ASCEND:	// Special key SHIFT still set
			{
				navigateToDrawRootParent();
				break;
			}
			case Do.TOGGLE_RELATION_ALL:
			case Do.TOGGLE_RELATION_1:
			case Do.TOGGLE_RELATION_2:
			case Do.TOGGLE_RELATION_3:
			case Do.TOGGLE_RELATION_4:
			case Do.TOGGLE_RELATION_5:
			case Do.TOGGLE_RELATION_6:
			case Do.TOGGLE_RELATION_7:
			case Do.TOGGLE_RELATION_8:
			case Do.TOGGLE_RELATION_9:
				m_queryBox.activate();
				m_queryBox.toggleFlags(key - Do.TOGGLE_RELATION_ALL);
				refillDiagram();
				requestFocus();
				break;
			}
		} else if (modifiers == 0) { 
			switch (key) {
			case Do.ESCAPE:	/* Escape */
			{
				boolean cleared = m_diagram.clearFlags(true);
				doFeedback("Query/selection cleared");

				/* Can't just repaint() because edges hidden as a result of a query
				 * will then not be shown
				 */

				m_editModeHandler.cleanup();
				if (cleared) {
					refillDiagram();
				}
				break;
			}
			case Do.PREV_HISTORY:
			{
				if (m_historyBox != null) {
					m_historyBox.navigatePrevious();
				}
				break;
			}
			case Do.NEXT_HISTORY:
			{
				if (m_historyBox != null) {
					m_historyBox.navigateNext();
				}
				break;
			}
			case Do.TOGGLE_DESCENDANTS:
			{	
				if (m_diagram != null) {
					int cnt = m_diagram.toggleDescendants(object);
					doFeedback("Opened/closed " + cnt + " entities");
					if (cnt != 0) {
						refillDiagram();
				}	}
				break;
			}
			case Do.FORWARD_QUERY:
			case Do.FORWARD_CLOSURE:
			case Do.BACKWARD_QUERY:
			case Do.BACKWARD_CLOSURE:
			case Do.CONTENTS_QUERY:
			case Do.CONTENT_CLOSURE:
			case Do.ROOT_CAUSE:
			{
				if (m_diagram != null) {		
					m_diagram.queryEntity(key, object, getResultBox(), -1);
				}
				break;
			}
			case Do.SHOW_CONTENTS:
			case Do.DST_EDGES:
			case Do.ENTERING_EDGES:
			case Do.SRC_EDGES:
			case Do.EXITING_EDGES:
			case Do.INTERNAL_EDGES:
			{
				String			msg;

				if (m_diagram != null) {
					msg = m_diagram.handleElision(key, object);
					if (msg != null) {
						doFeedback(msg);
					}
					refillDiagram();
				}
				break;
			}
			case Do.DECREASE_WIDTH:
			case Do.INCREASE_WIDTH:
			case Do.DECREASE_HEIGHT:
			case Do.INCREASE_HEIGHT:
			case Do.DECREASE_SIZE:
			case Do.INCREASE_SIZE:
			{
				String			msg;

				if (m_diagram != null) {
					beginUndoRedo("Scale");
					msg = m_diagram.scaleEntity(key, !m_editModeHandler.mouseIsDown());
					endUndoRedo();
					if (msg != null) {
						doFeedback(msg);
					}
				}
				break;
			}
			case Do.MOVE_GROUP_UP:
			case Do.MOVE_GROUP_DOWN:
			case Do.MOVE_GROUP_LEFT:
			case Do.MOVE_GROUP_RIGHT:
				moveGroup(key);
				break;

			case Do.NAVIGATE_ROOT:
			{
				Diagram			diagram = getDiagram();

				if (diagram != null) {
					EntityInstance	e = diagram.getRootInstance();
					if (e != null) {
						EntityInstance drawRoot = diagram.getDrawRoot();

						if (e == drawRoot) {
							error("Already in: " + e.getEntityLabel());
						} else {
							// Going to root
							followLink(e, false);
				}	}	}
				break;
			}
			case Do.DESCEND:
			{
				Diagram			diagram = getDiagram();

				if (diagram != null) {
					EntityInstance	e = diagram.targetEntity(object);
					if (e != null) {
						EntityInstance drawRoot = diagram.getDrawRoot();

						if (e == drawRoot) {
							error("Already in: " + e.getEntityLabel());
						} else {
							// Going down
							followLink(e, e.isClientOrSupplier());
				}	}	}
				break;
			}
			case Do.DELETE:
				m_diagram.doDelete(object);
				break;
			case Do.INCREASE_LABEL_FONT:
				beginUndoRedo("Increase Font");
				shiftDeltaFont(1);
				endUndoRedo();
				break;
			case Do.DECREASE_LABEL_FONT: 
				beginUndoRedo("Decrease Font");
				shiftDeltaFont(-1);
				endUndoRedo();
				break;
			case Do.NEW_EDGE:
			{
				Diagram		   diagram = getDiagram();

				if (diagram != null && diagram.getRootInstance() != null) {
					m_editModeHandler.newEdge(object);
				}
				break;
			}
			case Do.NEW_ENTITY:
			{
				newEntity(object);
				break;
			}

			case Do.EDIT_ENTITY_CLASS:
			{
				Diagram	diagram = getDiagram();
				if (diagram != null) {
					LandscapeObject landscapeObject = diagram.targetEntityRelation(object);
					if (landscapeObject != null) {
						if (landscapeObject instanceof EntityInstance) {
							EntityInstance e = (EntityInstance) landscapeObject;
							// edit class attributes
							EditAttribute.Create(this, e.getEntityClass());
						} else {
							RelationInstance ri = (RelationInstance) landscapeObject;
							// edit class attributes
							EditAttribute.Create(this, ri.getRelationClass());
						}
				}	}
				break;
			}
			case Do.EDIT_ATTRIBUTES:
			{
				Diagram			diagram         = getDiagram();
				if (diagram != null) {
					LandscapeObject landscapeObject = diagram.targetEntityRelation(object);
					if (landscapeObject != null) {
						if (landscapeObject instanceof EntityInstance) {
							EntityInstance e = (EntityInstance) landscapeObject;
							// edit attributes
							EditAttribute.Create(this, e);
						} else {
							RelationInstance ri = (RelationInstance) landscapeObject;
							// edit attributes
							EditAttribute.Create(this, ri);
						}
				}	}
				break;
			}
			case Do.DELETE_CONTAINER:
			{
				Diagram			diagram = getDiagram();
				if (diagram != null) {
					EntityInstance	e       = diagram.targetEntity(object);
					if (e != null) {
						deleteContainer(e);
				}	}
				break;
			}
			case Do.ADD_GROUP_REDBOXES:
			case Do.GROUP_REDBOXES:
			{
				Diagram	diagram = getDiagram();
				if (diagram != null) {
					diagram.groupRedBoxes(key == GROUP_REDBOXES);
				}
				break;
			}
			case Do.TOGGLE_LEGEND_ALL:
			case Do.TOGGLE_LEGEND_1:
			case Do.TOGGLE_LEGEND_2:
			case Do.TOGGLE_LEGEND_3:
			case Do.TOGGLE_LEGEND_4:
			case Do.TOGGLE_LEGEND_5:
			case Do.TOGGLE_LEGEND_6:
			case Do.TOGGLE_LEGEND_7:
			case Do.TOGGLE_LEGEND_8:
			case Do.TOGGLE_LEGEND_9:
				m_legendBox.activate();
				m_legendBox.toggleFlags(key - Do.TOGGLE_LEGEND_ALL);
				refillDiagram();
				requestFocus();
				break;
			case Do.FIND_PREV:
				findPrev();
				break;
			case Do.FIND_NEXT:
				findNext();
				break;
			case Do.ABOUT_PROGRAM:
				JOptionPane.showMessageDialog(m_frame, 	getTitle() + " " + Version.Details(getDiagram()), "About " + getTitle(), JOptionPane.OK_OPTION);
				break;
			case Do.ABOUT_URL:
				about();
				break;
			case Do.HELP_URL:
				help();
				break;
			case Do.REDISTRIBUTE:
			{
				Diagram diagram = m_diagram;

				if (diagram != null) {
					diagram.redistribute();
				}
				break;
			}
			case Do.GROUP_UNCONNECTED:
			{
				Diagram diagram = m_diagram;

				if (diagram != null) {
					diagram.group_unconnected();
				}
				break;
			}
			case Do.CLUSTER_METRICS:
				if (m_diagram != null) {
					m_diagram.clusterMetrics();
				}
				break;
		
			case Do.SHOW_SOURCECODE:
			case Do.SHOW_SOURCECODE2:
			case Do.SHOW_SOURCECODE3:
			case Do.SHOW_SOURCECODE4:
			case Do.SHOW_SOURCECODE5:
			case Do.SHOW_SOURCECODE6:
			case Do.SHOW_SOURCECODE7:
			case Do.SHOW_SOURCECODE8:
				showSource(object, key);
				break;

			case Do.SHOW_OPTIONS:
				OptionsDialog.create(this);
				break;
			case Do.AVOID_COLLISIONS:
				beginUndoRedo("Avoid collisions");
				avoidCollisions();
				endUndoRedo();
				break;
			}
		}
		return;
	}

	public Font getFont()
	{
		if (m_frame != null) {
			return m_frame.getFont();
		} 
		return m_applet.getFont();
	}

	public void setHandicapped(int size)
	{
		m_handicapped = size;
	}
	
	public int getForward()
	{
		return m_forward;
	}
	
	public void setForward(int value)
	{
		m_forward = value;
	}
	
	public void setForward(String value)
	{
		if (value != null) {
			setForward(Util.parseInt(value, -1));
	}	}		 

	public EntityInstance getForwardEntity()
	{
		return m_forwardEntity;
	}
	
	public void clearForwardEntity()
	{
		m_forwardEntity = null;
	}
	
	public void showValidAttributes(LandscapeClassObject o)
	{
		ResultBox resultBox = getResultBox();

		resultBox.clear();
		m_diagram.showValidAttributes(o, resultBox);
	}

	public void validateEntityAttributes(EntityClass ec)
	{
		ResultBox resultBox = getResultBox();

		resultBox.clear();
		m_diagram.validateEntityAttributes(ec, resultBox, true);
	}

	public void validateRelationAttributes(RelationClass rc)
	{
		ResultBox resultBox = getResultBox();

		resultBox.clear();
		m_diagram.validateRelationAttributes(rc, resultBox, true);
	}

	public void validateRelations(RelationClass rc)
	{
		ResultBox resultBox = getResultBox();

		resultBox.clear();
		m_diagram.validateRelations(rc, resultBox, true);
	}

	public void validateAll()
	{
		ResultBox resultBox = getResultBox();

		resultBox.clear();
		m_diagram.validateAll(resultBox);
	}

	// Interface TaFeedback

	public void showProgress(String str)
	{	
		if (m_nameBox != null) {
			m_nameBox.set(str);
	}	}

	public void doFeedback(String str) 
	{
		if (m_feedback != null) {
			m_feedback.set(str);
	}	}

	public void showInfo(String str) 
	{
		if (m_nameBox != null) {
/*			System.out.println("LandscapeEditorCore.showInfo(" + str + ")");
			java.lang.Thread.dumpStack();
			System.out.println("-----");
 */
			m_nameBox.set(str);
	}	}

	public void error(String msg) 
	{
		doFeedback(msg);
		Util.beep();
	}
	
	public void showCycle1(ResultBox resultBox, RelationInstance ri)
	{
		EntityInstance parent = ri.getSrc();
		if (!parent.isMarked(EntityInstance.NOT_IN_FOREST_MARK)) {
			showCycle1(resultBox, parent.getContainedByRelation());
		}
		resultBox.addRelation("  " /* Indent */, ri);
	}
		
	public void showCycle(RelationInstance ri)
	{
		ResultBox	resultBox = getResultBox();

		if (resultBox != null) {
			resultBox.activate();
			resultBox.addResultTitle("Cycle using " + ri.getParentClass().getLabel());
			showCycle1(resultBox, ri);
	}	}

	public void noContainRelation(String taPath)
	{
		JOptionPane.showMessageDialog(getFrame(), "No containing relation class defined", 
		                                          "Unable to load '" + taPath + "'", 
												   JOptionPane.ERROR_MESSAGE | JOptionPane.OK_OPTION);
	}

	public void hasMultipleParents(RelationClass rc, EntityInstance e)
	{
		Diagram				diagram       = getDiagram();
		String				containsLabel = rc.getLabel();

		JFrame				frame         = getFrame();
		ResultBox			resultBox     = getResultBox();
		Vector				v;
		int					i, size;
		RelationInstance	ri;
		

		if (resultBox != null) {
			resultBox.activate();
			resultBox.addResultTitle("Parents of " + e);
			v    = e.getDstRelList();
			size = v.size();
			for (i = 0; i < size; ++i) {
				ri = (RelationInstance) v.elementAt(i);
				if (ri.getRelationClass() == rc) {
					resultBox.addRelation("  " /* Indent */, ri);
		}	}	}
	}

	public void invertUndo()
	{
		if (m_undoBox != null) {
			m_undoBox.invertUndo();
	}	}

	public void fixupFind()
	{
		if (m_findResults != null) {
			if (m_findResults.entityCut()) {
				JButton button = m_toolButton[PREV_FIND_BUTTON];
				boolean change = false;
				boolean	state  = !m_findResults.atBeginning();

				if (button.isEnabled() != state) {
					button.setEnabled(state);
					change = true;
				}

				button = m_toolButton[NEXT_FIND_BUTTON];
				state  = !m_findResults.atEnd();
				if (button.isEnabled() != state) {
					button.setEnabled(state);
					change = true;
				}
				if (change) {
					refillToolbar();
	}	}	}	}

	// TaListener interface

	public void diagramChanging(Diagram diagram)
	{
	}

	public void diagramChanged(Diagram diagram, int signal)
	{
		if (m_findResults != null && signal == DIAGRAM_CHANGED) {
			m_findResults.clear();
		}
	}

	public void updateBegins()
	{
	}

	public void updateEnds()
	{
	}

	public void entityClassChanged(EntityClass ec, int signal)
	{
	}

	public void relationClassChanged(RelationClass rc, int signal)
	{
	}

	public void entityParentChanged(EntityInstance e, EntityInstance parent, int signal)
	{
		switch (signal) {
		case TaListener.ENTITY_CUT_SIGNAL:
		case TaListener.CONTAINER_CUT_SIGNAL:
			fixupFind();
			break;
	}	}

	public void relationParentChanged(RelationInstance ri, int signal)
	{
	}

	public void entityInstanceChanged(EntityInstance e, int signal)
	{
	}

	public void relationInstanceChanged(RelationInstance ri, int signal)
	{
	}

	// UndoListener interface
	
	// abstract void setEnabledRedo(boolean value); -- in Do.java
	// abstract void setEnabledUndo(boolean value); -- in Do.java
 
 	public void setPreferredSizeUndo(Vector edits, UndoableEdit lastEdit)
	{
		if (m_undoBox != null) {
			m_undoBox.setNewPreferredSize(edits, lastEdit);
	}	}

	public void undoHistoryChanged()
	{
		if (m_undoBox != null && m_undoBox.isActive()) {
			m_undoBox.fill();
	}	}

	// ToolBarEventHandler

	// Mouse right clicked

	public boolean processMetaKeyEvent(String name)
	{
		if (name != null) {
			String	urlname = m_helpURL + "/action.html#" + Util.encodedURLname(name);
			showURL(urlname, LsLink.TARGET_HELP);
			return(true);
		}
		return(false);
	}

	public void processKeyEvent(int key, int modifiers, Object object) 
	{
//		System.out.println("Process Key seen");

		if (key <= KeyEvent.VK_Z) {
			if (key >= KeyEvent.VK_A) {
				if ((modifiers & Event.SHIFT_MASK) != 0) {
					// Keep character as upper case but remove shift
					modifiers &= ~Event.SHIFT_MASK;
				} else {
					// Convert to lower case
					key += 'a' - KeyEvent.VK_A;
			}	}
		} else if ((key >= KeyEvent.VK_F1 && key <= KeyEvent.VK_F12) && (modifiers & (Event.ALT_MASK|Event.CTRL_MASK)) == 0) {
			// Utter stupidity:  VK_F1 is 0x70 smack bang in the middle of the lower case keys!!!
			// Co can't pass a lower cass key into processKeyEvent
			key += Do.FUNCTION_KEY;
		}	
		processKey(key, modifiers, object);
	}

	public void infoShown(LandscapeObject object)
	{
		if (LandscapeObject.g_infoShown != object) {
			LandscapeObject.g_infoShown = object;
			m_attributeBox.show(object);
	}	}

	// abstract void showInfo(String msg); -- shared with TaFeedback

} 
