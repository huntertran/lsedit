package lsedit;

import java.lang.Runtime;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

public class LandscapeEditorFrame extends JFrame implements WindowListener
{
	/* These items are static so that if we create a second landscape it inherits the values originally specified */
	
	protected static final int		MAX_WIDTH  = 1280;
	protected static final int		MAX_HEIGHT = 1024;
	
	protected static String			m_title				   = LandscapeEditorCore.getTitle();
	protected static boolean		m_landscapeGeo         = false;			// Set by -L option
	protected static int			m_frameWidth           = 0;
	protected static int			m_frameHeight          = 0;
	protected static int			m_diagramPercentWidth  = 0;
	protected static int			m_diagramPercentHeight = 0;

	protected LandscapeEditorCore	m_app                  = new LandscapeEditorCore(this, getSpecialPath());

	// This just gives back something we can then set parameters on

	public LandscapeEditorFrame() 
	{
		super();
	}

	public SpecialPath getSpecialPath()
	{
		return null;
	}

	public void setDebugOn()
	{
		// Turn on debugging output 
		MsgOut.setDebugFlag(true);
		MsgOut.dprintln("Start app: ");
	}

	public void setVerboseOn()
	{
		// Turn on verbose parsing output 
		MsgOut.setVerboseFlag(true); 
	}

	public void setLandscapeGeo()
	{
		m_landscapeGeo = true;
	}

	public boolean setFrameDimension(int width, int height) 
	{
		if (width > 0 && height > 0) {
			m_frameWidth  = width;
			m_frameHeight = height;
			return true;
		}
		return false;
	}
	// Returns the dimensions for input of form <x>x<y>

	public boolean setForward(String value)
	{
		int	size;

		try {
			size = Util.parseInt(value);
		} catch (Exception e) {
			return false;
		}
		m_app.setForward(size);
		return true;
	}

	
	public boolean setFrameDimension(String geo) 
	{
		int ind = geo.indexOf('x');

		if (ind > 0) {
			int	w, h;

			try {
				w = Util.parseInt(geo.substring(0, ind));
				h = Util.parseInt(geo.substring(ind+1));
				return(setFrameDimension(w, h));
			} catch (Exception e) {
			}
		}
		return false;
	}

	public boolean setDiagramPercent(int width, int height) 
	{
		if (width > 0 && width <= 100 && height > 0 && height <= 100) {
			m_diagramPercentWidth  = width;
			m_diagramPercentHeight = height;
			return true;
		}
		return false;
	}

	// Returns the dimensions for input of form <x>x<y>

	public boolean setDiagramPercent(String geo) 
	{
		int ind = geo.indexOf('x');

		if (ind > 0) {
			int	w, h;

			try {
				w = Util.parseInt(geo.substring(0, ind));
				h = Util.parseInt(geo.substring(ind+1));
				return(setDiagramPercent(w, h));
			} catch (Exception e) {
			}
		}
		return false;
	}

	public boolean setHandicapped(String fontsize) 
	{
		int	size;

		try {
			size = Util.parseInt(fontsize);
		} catch (Exception e) {
			return false;
		}
		if (size > 0) {
			m_app.setHandicapped(size);
		}
		return true;
	}

	public void setLsPath(String file)
	{
		m_app.setLsPath(file);
	}

	public static void usage() 
	{
		System.out.println("\nLandscape Editor " + Version.MAJOR + "." + Version.MINOR + "." + Version.BUILD + "\n");
		System.out.println("<executor> LandscapeEditorFrame [options] <HTTP or file path>\n");
		System.out.println("-h	               This message");
		System.out.println("-?	               This message");

		System.out.println("-b fontsize        Use larger fonts than normal");
		System.out.println("-d                 Debugging output");
		System.out.println("-v	               Verbose parsing output");
		System.out.println("-s				   Enforce strict semantics when reading TA");
		System.out.println("");

		System.out.println("-g<width>x<height> Geometry of editor");
		System.out.println("-G<width>x<height> Geometry of diagram");
		System.out.println("");

		System.out.println("-l \"matrix\"      Use the matrix layout algorithm in any initial layout");
  		System.out.println("-l \"simplex\"     Use the network simplex layout algorithm in any initial layout");
		System.out.println("-l \"sugiyama\"    Use the sugiyama layout algorithm in any initial layout");
		System.out.println("-l \"spring\"      Use the spring layout algorithm in any initial layout");
		System.out.println("-l \"old spring\"  Use the old spring layout algorithm in any initial layout");
		System.out.println("-l \"lisp\"        Use the lisp layout algorithm in any initial layout");
		System.out.println("-N <entity>        Open the landscape with the named entity as the draw root");
		System.out.println("-f <steps>         Forward path from <entity> n steps");
		System.out.println("-i <inifile>       Use this init file");	

		System.out.println("-L	               Start with landscape geometry");
		System.out.println("-V	               Start in viewer mode");
		System.out.println("");

		System.out.println("-P <save path>     Optional save path");
		System.out.println("-S <path suffix>   Optional save suffix");
		System.out.println("-T <frame title>   Optional frame title");
		System.out.println("-X <application>   Optional application to exec on save");
		System.out.println("");
	}

	// A program can pass arguments this way without invoking main if it wishes
	// However in general it would probably prefer to just make the necessary calls

	public boolean setOptions(String args[]) 
	{
		int		n;
		char	c;
		boolean	ok;
		int		lth;
		String	option, value;

		ok = true;
		
		// Decide what we are running

		for (n= 0; n < args.length && args[n].charAt(0) == '-'; ++n) {
			c = args[n].charAt(1);

			switch(c) {
			case 'h':
			case '?':
				return(false);
		}	}

		// Process command line switches


		for (n = 0; n < args.length; ++n) {
			option = args[n];
			lth    = option.length();
			if (lth < 2 || option.charAt(0) != '-') {
				break;
			}
			c = option.charAt(1);
			switch(c) {
			case 'd':
				setDebugOn();
				continue;
			case 'h':
			case '?':
				ok = false;
				continue;
			case 'v':
				setVerboseOn();
				continue;
			case 'L':
				setLandscapeGeo();
				continue;
			case 's':
				Ta.m_strict_TA = true;
				continue;
			}
			if (lth > 2) {
				value = option.substring(2);
			} else {
				if (++n >= args.length) {
					System.out.println("Option " + option + " missing argument");
					ok = false;
					break;
				}
				value = args[n];
			}
			switch(c) {
			case 'b':	// big (blind is descriminatory)
				if (!setHandicapped(value)) {
					System.out.println("Invalid use of -b");
					ok = false;
				}
				continue;
			case 'f':	// Forward path trace this number of steps on startup (use with -N)
				if (!setForward(value)) {
					System.out.println("Illegal -f option [ignored]");
					ok = false;
				}
				continue;
			case 'g':
				if (!setFrameDimension(value)) {
					System.out.println("Illegal -g option [ignored]");
					ok = false;
				}
				continue;
			case 'G':
				if (!setDiagramPercent(value)) {
					System.out.println("Illegal -G option [must be percentages]");
					ok = false;
				}
				continue;
			case 'i':
				m_app.m_lsInit = value;
				continue;
			case 'l':
				m_app.defaultToLayouter(value);
				continue;
			case 'N':
				m_app.setStartEntity(value);
				continue;
			case 'P':
				m_app.m_lsSavePath = value;
				continue;
			case 'S':
				m_app.m_lsSaveSuffix = value;
				continue;			
			case 'T':
				m_title = value;
				continue;
			case 'X':
				m_app.m_lsSaveCmd = value;
				continue;
			default:
				ok = false;
		}	}

		// Process command line args
		while (n < args.length) {
			setLsPath(args[n++]);
		}
		return(ok);
	}
	
	public void launch(String args[])
	{
		LandscapeEditorCore	app;
		JMenuBar			mb;

		if (args != null && !setOptions(args)) {
			usage();
			System.exit(0);
		}

		if (m_frameWidth <= 0 || m_frameHeight <= 0) {
			Toolkit tk = Toolkit.getDefaultToolkit();

			Dimension dim = tk.getScreenSize();

			double f = 0.75;

			if (m_landscapeGeo) {
				m_frameWidth  = Math.min(MAX_WIDTH, dim.width);
				m_frameHeight = Math.min(MAX_HEIGHT, dim.height);
			} else {
				m_frameWidth  = (int) (dim.width/f);
				m_frameHeight = dim.height-50;
			}

			m_frameWidth  = Math.min(m_frameWidth,  dim.width);
			m_frameHeight = Math.min(m_frameHeight, dim.height);
		}
		setBounds(0, 0, m_frameWidth, m_frameHeight); 

		app = m_app;
		
		setTitle(m_title);
		setVisible(true);
		mb = app.genMenu();
		setJMenuBar(mb);
						
		app.init_core(m_diagramPercentWidth, m_diagramPercentHeight);

		addWindowListener(this);				// Want to be able to detect x pressed in Top RH corner
		setBackground(Color.lightGray);
		setVisible(true);
	}

	public static LandscapeEditorFrame create() 
	{
		return new LandscapeEditorFrame();
	}
	
	public static void main(String args[]) 
	{
		LandscapeEditorFrame af = create();

		af.launch(args);

		MsgOut.dprintln("exit main");
	}

	// Window Listener implementation follows

	public void windowOpened(WindowEvent evt) 
	{
	}

	public void windowActivated(WindowEvent evt) 
	{
	}

	public void windowClosed(WindowEvent evt) 
	{
	}

	public void windowClosing(WindowEvent evt) 
	{
//		System.out.println("LandscapeEditorFrame.windowClosing");

		if (m_app != null) {
			m_app.testForClose(true);
		}
		removeWindowListener(this);
		Runtime.getRuntime().exit(0);
		dispose();

	}

	public void windowDeactivated(WindowEvent evt) 
	{
	}

	public void windowDeiconified(WindowEvent evt) 
	{
	}

	public void windowIconified(WindowEvent evt) 
	{
	}
}
