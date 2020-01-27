package lsedit;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;

import java.io.PrintWriter;

public class Option { 

	// Fonts
	
	public static final String	DEFAULT_FONT_NAME             = "Helvetica";
	public static final int		DEFAULT_FONT_STYLE            = Font.PLAIN;
	public static final int		DEFAULT_FONT_SIZE             = 12;

	public static final String	DEFAULT_MENU_FONT_NAME        = "Dialog";
	public static final int		DEFAULT_MENU_FONT_STYLE       = Font.BOLD;
	public static final int		DEFAULT_MENU_FONT_SIZE        = 12;

	public static final String	DEFAULT_DIALOG_FONT_NAME      = "Dialog";
	public static final int		DEFAULT_DIALOG_FONT_STYLE     = Font.PLAIN;
	public static final int		DEFAULT_DIALOG_FONT_SIZE      = 12;

	public final static String	DEFAULT_OPEN_FONT_NAME        = DEFAULT_FONT_NAME;
	public final static int		DEFAULT_OPEN_FONT_STYLE       = Font.PLAIN;
	public final static int		DEFAULT_OPEN_FONT_SIZE        = 12;

	public final static String	DEFAULT_CLOSED_FONT_NAME      = DEFAULT_FONT_NAME;
	public final static int		DEFAULT_CLOSED_FONT_STYLE     = Font.PLAIN;
	public final static int		DEFAULT_CLOSED_FONT_SIZE      = 12;

	public final static String	DEFAULT_SMALL_FONT_NAME       = DEFAULT_FONT_NAME;
	public final static int		DEFAULT_SMALL_FONT_STYLE      = Font.PLAIN;
	public final static int		DEFAULT_SMALL_FONT_SIZE       = 10;
	
	public final static String DEFAULT_CARDINAL_FONT_NAME     = DEFAULT_FONT_NAME;
	public final static int    DEFAULT_CARDINAL_FONT_STYLE    = Font.PLAIN;
	public final static int    DEFAULT_CARDINAL_FONT_SIZE     = 9;
	
	public final static String DEFAULT_CLIENT_FONT_NAME       = DEFAULT_FONT_NAME;
	public final static int    DEFAULT_CLIENT_FONT_STYLE      = Font.PLAIN;
	public final static int    DEFAULT_CLIENT_FONT_SIZE       = 10;
	
	public final static String DEFAULT_RESULT_TITLE_FONT_NAME  = DEFAULT_FONT_NAME;
	public final static int    DEFAULT_RESULT_TITLE_FONT_STYLE = Font.BOLD;
	public final static int    DEFAULT_RESULT_TITLE_FONT_SIZE  = 14;

	public final static String DEFAULT_RESULT_TEXT_FONT_NAME   = DEFAULT_FONT_NAME;
	public final static int    DEFAULT_RESULT_TEXT_FONT_STYLE  = Font.PLAIN;
	public final static int    DEFAULT_RESULT_TEXT_FONT_SIZE   = 11;

	public final static String DEFAULT_LEGEND_TITLE_FONT_NAME  = DEFAULT_FONT_NAME;
	public final static int    DEFAULT_LEGEND_TITLE_FONT_STYLE = Font.BOLD;
	public final static int    DEFAULT_LEGEND_TITLE_FONT_SIZE  = 12;

	public final static String DEFAULT_LEGEND_TEXT_FONT_NAME   = DEFAULT_FONT_NAME;
	public final static int    DEFAULT_LEGEND_TEXT_FONT_STYLE  = Font.PLAIN;
	public final static int    DEFAULT_LEGEND_TEXT_FONT_SIZE   = 11;
	
	public final static String DEFAULT_QUERY_TITLE_FONT_NAME   = DEFAULT_FONT_NAME;
	public final static int    DEFAULT_QUERY_TITLE_FONT_STYLE  = Font.BOLD;
	public final static int    DEFAULT_QUERY_TITLE_FONT_SIZE   = 12;

	public final static String DEFAULT_QUERY_TEXT_FONT_NAME    = DEFAULT_FONT_NAME;
	public final static int    DEFAULT_QUERY_TEXT_FONT_STYLE   = Font.PLAIN;
	public final static int    DEFAULT_QUERY_TEXT_FONT_SIZE    = 11;
	
	public final static String DEFAULT_TOC_FONT_NAME           = DEFAULT_FONT_NAME;
	public final static int    DEFAULT_TOC_FONT_STYLE          = Font.PLAIN;
	public final static int    DEFAULT_TOC_FONT_SIZE           = 11;

	public final static String DEFAULT_UNDO_FONT_NAME          = DEFAULT_FONT_NAME;
	public final static int    DEFAULT_UNDO_FONT_STYLE         = Font.PLAIN;
	public final static int    DEFAULT_UNDO_FONT_SIZE          = 11;

	public final static String DEFAULT_HISTORY_FONT_NAME       = DEFAULT_FONT_NAME;
	public final static int    DEFAULT_HISTORY_FONT_STYLE      = Font.PLAIN;
	public final static int    DEFAULT_HISTORY_FONT_SIZE       = 11;
	
	public final static String DEFAULT_MAP_FONT_NAME           = DEFAULT_FONT_NAME;
	public final static int    DEFAULT_MAP_FONT_STYLE          = Font.PLAIN;
	public final static int    DEFAULT_MAP_FONT_SIZE           = 10;

	public final static String DEFAULT_CLIPBOARD_FONT_NAME     = DEFAULT_FONT_NAME;
	public final static int    DEFAULT_CLIPBOARD_FONT_STYLE    = Font.PLAIN;
	public final static int    DEFAULT_CLIPBOARD_FONT_SIZE     = 11;

	public final static String DEFAULT_TEXTBOX_TITLE_FONT_NAME = DEFAULT_FONT_NAME;
	public final static int    DEFAULT_TEXTBOX_TITLE_FONT_STYLE= Font.BOLD;
	public final static int    DEFAULT_TEXTBOX_TITLE_FONT_SIZE = 14;

	public final static String DEFAULT_TEXTBOX_TEXT_FONT_NAME  = DEFAULT_FONT_NAME;
	public final static int    DEFAULT_TEXTBOX_TEXT_FONT_STYLE = Font.PLAIN;
	public final static int    DEFAULT_TEXTBOX_TEXT_FONT_SIZE  = 11;

	public final static String DEFAULT_FEEDBACK_FONT_NAME      = DEFAULT_FONT_NAME;
	public final static int    DEFAULT_FEEDBACK_FONT_STYLE     = Font.PLAIN;
	public final static int    DEFAULT_FEEDBACK_FONT_SIZE      = 11;

	public final static String DEFAULT_ATTRIBUTE_TEXT_FONT_NAME  = DEFAULT_FONT_NAME;
	public final static int    DEFAULT_ATTRIBUTE_TEXT_FONT_STYLE = Font.PLAIN;
	public final static int    DEFAULT_ATTRIBUTE_TEXT_FONT_SIZE  = 11;

	public final static String DEFAULT_EDGE_FONT_NAME			= DEFAULT_FONT_NAME;
	public final static int    DEFAULT_EDGE_FONT_STYLE			= Font.PLAIN;
	public final static int    DEFAULT_EDGE_FONT_SIZE			= 11;
	
	public final static String DEFAULT_VIEWS_FONT_NAME         = DEFAULT_FONT_NAME;
	public final static int    DEFAULT_VIEWS_FONT_STYLE        = Font.PLAIN;
	public final static int    DEFAULT_VIEWS_FONT_SIZE         = 11;
	
	public final static String DEFAULT_TABS_FONT_NAME          = DEFAULT_FONT_NAME;
	public final static int    DEFAULT_TABS_FONT_STYLE         = Font.PLAIN;
	public final static int    DEFAULT_TABS_FONT_SIZE          = 11;



	public  final static int	  FONT_ALL           = 0;
	public  final static int	  FONT_CLOSED        = 1;
	public  final static int	  FONT_SMALL         = 2;
	public  final static int	  FONT_OPEN          = 3;
	public  final static int	  FONT_CLIENTS       = 4;
	public  final static int	  FONT_CARDINALS     = 5;
	public  final static int	  FONT_RESULTS_TITLE = 6;
	public  final static int	  FONT_RESULTS_TEXT  = 7;
	public  final static int	  FONT_LEGEND_TITLE  = 8;
	public  final static int      FONT_LEGEND_TEXT   = 9;
	public  final static int	  FONT_QUERY_TITLE   = 10;
	public  final static int	  FONT_QUERY_TEXT    = 11;
	public  final static int	  FONT_TOC           = 12;
	public  final static int	  FONT_UNDO          = 13;
	public  final static int      FONT_HISTORY       = 14;
	public  final static int      FONT_MAP           = 15;
	public  final static int	  FONT_TEXTBOX_TITLE = 16;
	public  final static int	  FONT_TEXTBOX_TEXT  = 17;
	public  final static int	  FONT_FEEDBACK      = 18;
	public  final static int      FONT_MENU          = 19;
	public  final static int	  FONT_DIALOG        = 20;
	public	final static int	  FONT_CLIPBOARD     = 21;
	public  final static int	  FONT_ATTR_TEXT     = 22;
	public  final static int	  FONT_EDGE_LABEL    = 23;
	public	final static int	  FONT_VIEWS         = 24;
	public  final static int      FONT_TABS          = 25;
	public  final static int	  FONT_LAST          = 25;
	
	public final static String[] m_default_fontnames = {
		DEFAULT_FONT_NAME,				/* FONT_ALL */
		DEFAULT_CLOSED_FONT_NAME,		/* FONT_CLOSED */
		DEFAULT_SMALL_FONT_NAME,		/* FONT_SMALL	*/
		DEFAULT_OPEN_FONT_NAME,			/* FONT_OPEN */
		DEFAULT_CLIENT_FONT_NAME,		/* FONT_CLIENTS	*/
		DEFAULT_CARDINAL_FONT_NAME,
		DEFAULT_RESULT_TITLE_FONT_NAME,
		DEFAULT_RESULT_TEXT_FONT_NAME,
		DEFAULT_LEGEND_TITLE_FONT_NAME,
		DEFAULT_LEGEND_TEXT_FONT_NAME,
		DEFAULT_QUERY_TITLE_FONT_NAME,
		DEFAULT_QUERY_TEXT_FONT_NAME,
		DEFAULT_TOC_FONT_NAME,
		DEFAULT_UNDO_FONT_NAME,
		DEFAULT_HISTORY_FONT_NAME,
		DEFAULT_MAP_FONT_NAME,
		DEFAULT_TEXTBOX_TITLE_FONT_NAME,
		DEFAULT_TEXTBOX_TEXT_FONT_NAME,
		DEFAULT_FEEDBACK_FONT_NAME,
		DEFAULT_MENU_FONT_NAME,
		DEFAULT_DIALOG_FONT_NAME,
		DEFAULT_CLIPBOARD_FONT_NAME,
		DEFAULT_ATTRIBUTE_TEXT_FONT_NAME,
		DEFAULT_EDGE_FONT_NAME,
		DEFAULT_VIEWS_FONT_NAME,
		DEFAULT_TABS_FONT_NAME
	};
	
	public final static int[] m_default_fontstyles =
	{
		DEFAULT_FONT_STYLE,
		DEFAULT_CLOSED_FONT_STYLE,
		DEFAULT_SMALL_FONT_STYLE,
		DEFAULT_OPEN_FONT_STYLE,
		DEFAULT_CLIENT_FONT_STYLE,
		DEFAULT_CARDINAL_FONT_STYLE,
		DEFAULT_RESULT_TITLE_FONT_STYLE,
		DEFAULT_RESULT_TEXT_FONT_STYLE,
		DEFAULT_LEGEND_TITLE_FONT_STYLE,
		DEFAULT_LEGEND_TEXT_FONT_STYLE,
		DEFAULT_QUERY_TITLE_FONT_STYLE,
		DEFAULT_QUERY_TEXT_FONT_STYLE,
		DEFAULT_TOC_FONT_STYLE,
		DEFAULT_UNDO_FONT_STYLE,
		DEFAULT_HISTORY_FONT_STYLE,
		DEFAULT_MAP_FONT_STYLE,
		DEFAULT_TEXTBOX_TITLE_FONT_STYLE,
		DEFAULT_TEXTBOX_TEXT_FONT_STYLE,
		DEFAULT_FEEDBACK_FONT_STYLE,
		DEFAULT_MENU_FONT_STYLE,
		DEFAULT_DIALOG_FONT_STYLE,
		DEFAULT_CLIPBOARD_FONT_STYLE,
		DEFAULT_ATTRIBUTE_TEXT_FONT_STYLE,
		DEFAULT_EDGE_FONT_STYLE,
		DEFAULT_VIEWS_FONT_STYLE,
		DEFAULT_TABS_FONT_STYLE
	};
	
	public final static int[] m_default_fontsizes =
	{
		DEFAULT_FONT_SIZE,
		DEFAULT_CLOSED_FONT_SIZE,
		DEFAULT_SMALL_FONT_SIZE,
		DEFAULT_OPEN_FONT_SIZE,
		DEFAULT_CLIENT_FONT_SIZE,
		DEFAULT_CARDINAL_FONT_SIZE,
		DEFAULT_RESULT_TITLE_FONT_SIZE,
		DEFAULT_RESULT_TEXT_FONT_SIZE,
		DEFAULT_LEGEND_TITLE_FONT_SIZE,
		DEFAULT_LEGEND_TEXT_FONT_SIZE,
		DEFAULT_QUERY_TITLE_FONT_SIZE,
		DEFAULT_QUERY_TEXT_FONT_SIZE,
		DEFAULT_TOC_FONT_SIZE,
		DEFAULT_UNDO_FONT_SIZE,
		DEFAULT_HISTORY_FONT_SIZE,
		DEFAULT_MAP_FONT_SIZE,
		DEFAULT_TEXTBOX_TITLE_FONT_SIZE,
		DEFAULT_TEXTBOX_TEXT_FONT_SIZE,
		DEFAULT_FEEDBACK_FONT_SIZE,
		DEFAULT_MENU_FONT_SIZE,
		DEFAULT_DIALOG_FONT_SIZE,
		DEFAULT_CLIPBOARD_FONT_SIZE,
		DEFAULT_ATTRIBUTE_TEXT_FONT_SIZE,
		DEFAULT_EDGE_FONT_SIZE,
		DEFAULT_VIEWS_FONT_SIZE,
		DEFAULT_TABS_FONT_SIZE
	};
	private String			m_option_name;
	
	private	Font[]			m_fonts       = new Font[FONT_LAST + 1];
	
	public void reset()
	{
		resetFonts();
		resetMainOptions();
		resetArrowOptions();
		resetDiagramOptions();
	}
	
	public Option(String option_name)
	{
		m_option_name = option_name;
		reset();
	}
	
	public static String getDefaultFontName(int target)
	{
		if (target >= 0 && target < m_default_fontnames.length) {
			return m_default_fontnames[target];
		}
		System.out.println("Option.getDefaultFontName() Illegal target " + target);
		return DEFAULT_FONT_NAME;
	}

	public static int getDefaultFontStyle(int target)
	{
		if (target >= 0 && target < m_default_fontstyles.length) {
			return m_default_fontstyles[target];
		}
		System.out.println("Option.getDefaultFontStyle() Illegal target " + target);
		return DEFAULT_FONT_STYLE;
	}

	public static int getDefaultFontSize(int target)
	{
		if (target >= 0 && target < m_default_fontsizes.length) {
			return m_default_fontsizes[target];
		}
		System.out.println("Option.getDefaultFontSize() Illegal target " + target);
		return DEFAULT_FONT_SIZE;
	}
	
	public static Font getDefaultFont(int target)
	{
		return FontCache.get(getDefaultFontName(target), getDefaultFontStyle(target), getDefaultFontSize(target));
	}
	
	public Font getTargetFont(int target)
	{
		return m_fonts[target];
	}
	
	public void setTargetFont(int target, Font font)
	{
		m_fonts[target] = font;
	}
	
	// Set the target font to font specified but override if default required
	// Used by FontChooser
	
	public void setTargetFont(int target, Font font, boolean name_default, boolean style_default, boolean size_default)
	{
		if (target == FONT_ALL) {

			int	i;

			for (i = 1; i <= Option.FONT_LAST; ++i) {
				if (name_default || style_default || size_default) {
					String  name;
					int		style;
					int		size;
					Font	font1;

					if (name_default) {
						name = getDefaultFontName(i);
					} else {
						name = font.getName();
					}
					if (style_default) {
						style = getDefaultFontStyle(i);
					} else {
						style = font.getStyle();
					}
					if (size_default) {
						size  = getDefaultFontSize(i);
					} else {
						size  = font.getSize();
					}
					font1 = FontCache.get(name, style, size);
					setTargetFont(i, font1);

				} else {
					setTargetFont(i, font);
			}	}
		} else {
			setTargetFont(target, font);
	}	}
	
	public void resetFonts()
	{
		Font[]	fonts = m_fonts;
		int		target;
		
		for (target = 0; target <= FONT_LAST; ++target) {
			setTargetFont(target, getDefaultFont(target));
	}	}
	
	public void setFontsTo(Option other)
	{
		if (other == null) {
			resetFonts();
		} else {
			Font[]	fonts = other.m_fonts;
			int		target;
			
			for (target = 1; target <= FONT_LAST; ++target) {
				setTargetFont(target, fonts[target]);
	}	}	}
	
	private void loadFont(String attribute, String value)
	{
		int		target = 0;
		String	name;
		int		style  = 0;
		int		size   = 0;
		int		i;
		Font	font;
		int		lth;
		char	c;

		lth = value.length();
		if (lth > 1 && value.charAt(0) == '"' && value.charAt(lth-1) == '"') {
			value = value.substring(1, lth-1);
		}
		try	{
			for (i = 5; ; ++i)	{
				if (i >= attribute.length()) {
					return;
				}
				c = attribute.charAt(i);
				if (c == ']') {
					break;
				}
				if (c < '0' || c > '9') {
					return;
				}
				target = target * 10 + (c - '0');
			}
			if (target <= 0 || FONT_LAST < target) {
				return;
			}

			for (i = 0; ; ++i) {
				if (i >= value.length()) {
					return;
				}
				c = value.charAt(i);
				if (c == ',') {
					break;
			}	}
			if (i == 0) {
				return;
			}
			name  = value.substring(0, i);
			for (;;) {
				if (++i >= value.length()) {
					return;
				}
				c = value.charAt(i);
				if (c == ',') {
					break;
				}
				if (c < '0' || c > '9') {
					return;
				}
				style = style * 10 + (c - '0');
			}
			for (;;) {
				if (++i >= value.length()) {
					break;
				}
				c = value.charAt(i);
				if (c == '\r' || c == '\n' || c == ' ')	{
					break;
				}
				if (c < '0' || c > '9')	{
					return;
				}
				size = size * 10 + (c - '0');
			}
			font = FontCache.get(name, style, size);
			setTargetFont(target, font);
//			System.out.println("font[" + target + "]=" + name + "," + style + "," + size);
		} catch (Exception e) {
			System.out.println("Option.loadFont=" + target + " " + e.getMessage());
	}	}

	private void saveFonts(PrintWriter ps)
	{
		int		target;	// The font chooser code for this font
		Font	font;
		String	name;
		int		style;
		int		size;

		for (target = 1; target <= FONT_LAST; ++target) {
			font          = getTargetFont(target);
			name          = font.getName();
			style         = font.getStyle();
			size          = font.getSize();
			ps.println("font[" + target + "]=\"" + name + "," + style + "," + size + "\"");
	}	}
	
//  =================================== Diagram options logic ========================================

	// Since edge states are emitted to the output TA they may not be renumbered

	public static final int			INFLECTION_EDGE_STATE = 0;			// INFLECTION POINT			
	public static final int			TB_EDGE_STATE	      = 1;			// TOP BOTTOM EDGE
	public static final int			DIRECT_EDGE_STATE     = 2; 
	public static final int			SIDE_EDGE_STATE       = 3;			// DIRECT EDGE WITH ANC/DESC on SIDE

	public	static final int		ICON_RULE_NONE     = 0;
	public	static final int		ICON_RULE_PLAIN	   = 1;
	public	static final int		ICON_RULE_CENTERED = 2;
	public	static final int		ICON_RULE_BOTTOM   = 3;
	public	static final int		ICON_RULE_TOP      = 4;
	public  static final int		ICON_RULE_EMPTY    = 5;
	
	public final static int			LOAD_RULE          = 0;
	public final static int			GRIDSIZE           = 1;
	public final static int			GRIDCOLOR          = 2;
	public final static int			ELISIONICON        = 3;
	public final static int			SHOW_CLASS_TYPE    = 4;
	public final static int			SHOW_SPAN_TYPE     = 5;
	public final static int			SHOW_NAVNEXT       = 6;
	public final static int			SHOW_NAVPREV       = 7;
	
	protected final String[] m_numeric_option_tags = 
	{
		"option:load",
		"option:gridsize",
		"option:gridcolor",
		"option:elisionicon",
		"option:showclasstype",
		"option:showspantype",
		"option:shownavnext",
		"option:shownavprev"
	};
	
	public final static int		LOAD_YES            = 0;
	public final static int		LOAD_NO             = 1;
	public final static int		LOAD_PROMPT         = 2;
	

	public final static int		LOAD_STATE_DEFAULT  = LOAD_YES;
	public final static int		EDGE_STATE_DEFAULT  = SIDE_EDGE_STATE;
	public final static boolean ICON_FIXED_DEFAULT  = true;
	public final static int     ELISION_ICON_DEFAULT= -1;
	public final static String	ICONPATH_DEFAULT    = ".;icons;../icons";
	public final static int		GRIDPIXELS_DEFAULT  = 1;
	public final static Color	GRIDCOLOR_DEFAULT   = Color.WHITE;
	public final static int		CHASE_EDGES_DEFAULT = -1;
	public final static boolean CHASE_HIDE_DEFAULT  = false;
	public final static boolean TRACE_LIFTED_DEFAULT = true;
	public final static boolean GROUP_QUERY_DEFAULT = false;
	public final static boolean QUERY_PERSISTS_DEFAULT = false;
	public final static boolean	VISIBLE_SPANS_DEFAULT  = true;

	public final static int		SHOW_SPAN_TYPE_DEFAULT = 1;
	public final static int		SHOW_CLASS_TYPE_DEFAULT= 1;
	public final static int		SHOW_NAVNEXT_DEFAULT   = 1;
	public final static int		SHOW_NAVPREV_DEFAULT   = 1;

	protected int				m_load_state        = LOAD_STATE_DEFAULT;
	protected int				m_edge_state        = EDGE_STATE_DEFAULT;
	private	int					m_icon_rule         = ICON_RULE_NONE;
	private int					m_elision_icon      = ELISION_ICON_DEFAULT;
	protected boolean			m_visible_spans_state  = VISIBLE_SPANS_DEFAULT;  

	protected int				m_show_class_type_state = SHOW_CLASS_TYPE_DEFAULT;  
	protected int				m_show_span_type_state  = SHOW_SPAN_TYPE_DEFAULT;
	protected int				m_show_navnext_state    = SHOW_NAVNEXT_DEFAULT;
	protected int				m_show_navprev_state    = SHOW_NAVPREV_DEFAULT;

	private String				m_iconPath          = ICONPATH_DEFAULT;
	private boolean				m_icon_fixed_shape  = ICON_FIXED_DEFAULT;
	protected boolean			m_show_grid_state    = SHOW_GRID_DEFAULT;
	protected boolean			m_snap_to_grid_state = SNAP_TO_GRID_DEFAULT;
	protected int				m_gridPixels        = GRIDPIXELS_DEFAULT;
	protected Color				m_gridColor         = GRIDCOLOR_DEFAULT;
	protected int				m_chase_edges       = CHASE_EDGES_DEFAULT;	// Number of consecutive edges to chase in path analysus
	protected boolean			m_chase_hide_state  = CHASE_HIDE_DEFAULT;	// Hide those entities not involved in path chasing
	protected boolean			m_trace_lifted_state = TRACE_LIFTED_DEFAULT;
	protected boolean			m_group_query_state = GROUP_QUERY_DEFAULT;
	protected boolean			m_query_persists_state = QUERY_PERSISTS_DEFAULT;


	public int getLoadMode()
	{
		return m_load_state;
	}
	
	public void setLoadMode(int mode) 
	{
		if (mode >= 0 && mode < 4) {
			m_load_state = mode;
	}	}
	
	public void setLoadMode(String string)
	{
		try {
			m_load_state = Integer.parseInt(string);
		} catch (Throwable e) {
			System.out.println("Option.setLoadMode(\"" + string + "\") " + e.getMessage());
	}	}

	public int	getEdgeMode()
	{
		return m_edge_state;
	}

	public void setEdgeMode(int mode) 
	{
		if (mode >= 0 && mode <= SIDE_EDGE_STATE) {
			m_edge_state = mode;
	}	}
	
	public String setEdgeMode(String string)
	{
		try {
			int ival = Integer.parseInt(string);
			if (ival < 0 || ival > SIDE_EDGE_STATE) {
				return "EdgeMode must be a value between 0 and 3";
			}
			m_edge_state = ival;
		} catch (Throwable e) {
			return "EdgeMode is not an integer";
		}
		return null;
	}
	
	public int getIconRule()
	{
		return m_icon_rule;
	}
	
	public void setIconRule(int value)
	{
		if (value >= 0) {
			m_icon_rule = value;
	}	}	
	
	public String setIconRule(String string)
	{
		try {
			int ival = Integer.parseInt(string);
			if (ival < 0 || ival > 4) {
				return "IconRule must be a value between 0 and 4";
			}
			m_icon_rule = ival;
		} catch (Throwable e) {
			return "IconRule is not an integer";
		}
		return null;
	}

	public int getShowSpanType()
	{
		return m_show_span_type_state;
	}

	public void setShowSpanType(int value)
	{
		m_show_span_type_state = value;
	}	
		
	public String setShowSpanType(String string)
	{
		try {
			int ival = Integer.parseInt(string);
			if (ival < 0 || ival > 2) {
				return "ShowSpanType must be a value between 0 and 2";
			}
			m_show_span_type_state = ival;
		} catch (Throwable e) {
			return "ShowSpanType is not an integer";
		}
		return null;
	}

	public int getShowClassType()
	{
		return m_show_class_type_state;
	}

	public void setShowClassType(int value)
	{
		m_show_class_type_state = value;
	}	

	public String setShowClassType(String string)
	{
		try {
			int ival = Integer.parseInt(string);
			if (ival < 0 || ival > 2) {
				return "ShowClassType must be a value between 0 and 2";
			}
			m_show_span_type_state = ival;
		} catch (Throwable e) {
			return "ShowClassType is not an integer";
		}
		return null;
	}

	public int getShowNavNext()
	{
		return m_show_navnext_state;
	}

	public void setShowNavNext(int value)
	{
		m_show_navnext_state = value;
	}	

	public String setShowNavNext(String string)
	{
		try {
			int ival = Integer.parseInt(string);
			if (ival < 0 || ival > 2) {
				return "ShowNavNext must be a value between 0 and 2";
			}
			m_show_navnext_state = ival;
		} catch (Throwable e) {
			return "ShowNavNext is not an integer";
		}
		return null;
	}

	public int getShowNavPrev()
	{
		return m_show_navprev_state;
	}

	public void setShowNavPrev(int value)
	{
		m_show_navprev_state = value;
	}	
		
	public String setShowNavPrev(String string)
	{
		try {
			int ival = Integer.parseInt(string);
			if (ival < 0 || ival > 2) {
				return "ShowNavPrev must be a value between 0 and 2";
			}
			m_show_navprev_state = ival;
		} catch (Throwable e) {
			return "ShowNavPrev is not an integer";
		}
		return null;
	}

	public String getIconPath()
	{
		return m_iconPath;
	}
	
	public String setIconPath(String value)
	{
		m_iconPath = value.replace('\\','/');
		return null;
	}
	
	public boolean isIconFixedShape()
	{
		return m_icon_fixed_shape;
	}
	
	public void setIconFixedShape(boolean value)
	{
		m_icon_fixed_shape = value;
	}

	public int getElisionIcon()
	{
		return m_elision_icon;
	}

	public void setElisionIcon(int value)
	{
		if (m_elision_icon != value) {
			IconCache.clearElisionCache();
			m_elision_icon = value;
	}	}

	public void setElisionIcon(String string)
	{
		try {
			m_elision_icon = Integer.parseInt(string);
		} catch (Throwable e) {
			System.out.println("Option.elisionIcon(\"" + string + "\") " + e.getMessage());
	}	}
	
	public int getGridSize()
	{
		return m_gridPixels;
	}
	
	public void setGridSize(int value)
	{
		m_gridPixels = value;
	}
	
	public void setGridSize(String string)
	{
		try {
			m_gridPixels = Integer.parseInt(string);
		} catch (Throwable e) {
			System.out.println("Option.setGridSize(\"" + string + "\") " + e.getMessage());
	}	}
	
	public Color getGridColor()
	{
		return m_gridColor;
	}
	
	public void setGridColor(Color value)
	{
		m_gridColor = value;
	}
	
	public boolean isShowGrid()
	{
		return m_show_grid_state;
	}

	public boolean isSnapToGrid()
	{
		return m_snap_to_grid_state;
	}

	public void setSnapToGrid(boolean value)
	{
		m_snap_to_grid_state = value;
	}

	public void setShowGrid(boolean value)
	{
		m_show_grid_state = value;
	}
	
	public int getChaseEdges()
	{
		return m_chase_edges;
	}
		
	public void setChaseEdges(int value)
	{
		m_chase_edges = value;
	}
	
	public String setChaseEdges(String string)
	{
		try {
			int ival = Integer.parseInt(string);
			m_chase_edges = ival;
		} catch (Throwable e) {
			return "Chase edges is not an integer";
		}
		return null;
	}

	public boolean isChaseHide()
	{
		return m_chase_hide_state;
	}
	
	public void setChaseHide(boolean value)
	{
		m_chase_hide_state = value;
	}

	public boolean isTraceLifted()
	{
		return m_trace_lifted_state;
	}

	public void setTraceLifted(boolean value)
	{
		m_trace_lifted_state = value;
	}

	public boolean isGroupQuery()
	{
		return m_group_query_state;
	}

	public void setGroupQuery(boolean value)
	{
		m_group_query_state = value;
	}

	public boolean isQueryPersists()
	{
		return m_query_persists_state;
	}

	public void setQueryPersists(boolean value)
	{
		m_query_persists_state = value;
	}
	
	public boolean isVisibleSpans()
	{
		return m_visible_spans_state;
	}
	
	public void setVisibleSpans(boolean value)
	{
		m_visible_spans_state = value;
	}

	public void resetDiagramOptions()
	{
		m_load_state           = LOAD_YES;
		m_edge_state	       = EDGE_STATE_DEFAULT;
		m_icon_rule            = ICON_RULE_NONE;
		m_iconPath             = ICONPATH_DEFAULT;
		m_icon_fixed_shape     = ICON_FIXED_DEFAULT;
		m_elision_icon         = ELISION_ICON_DEFAULT;
		m_show_grid_state      = SHOW_GRID_DEFAULT;
		m_snap_to_grid_state   = SNAP_TO_GRID_DEFAULT;
		m_gridPixels           = GRIDPIXELS_DEFAULT;
		m_gridColor            = GRIDCOLOR_DEFAULT;
		m_chase_edges          = CHASE_EDGES_DEFAULT;
		m_chase_hide_state     = CHASE_HIDE_DEFAULT;
		m_trace_lifted_state   = TRACE_LIFTED_DEFAULT;
		m_group_query_state    = GROUP_QUERY_DEFAULT;
		m_query_persists_state = QUERY_PERSISTS_DEFAULT;
		m_visible_spans_state  = VISIBLE_SPANS_DEFAULT;
		m_show_span_type_state = SHOW_SPAN_TYPE_DEFAULT;
		m_show_class_type_state= SHOW_CLASS_TYPE_DEFAULT;
		m_show_navnext_state   = SHOW_NAVNEXT_DEFAULT;
		m_show_navprev_state   = SHOW_NAVPREV_DEFAULT;
	}
	
	public void setDiagramOptionsTo(Option other)
	{
		if (other == null) {
			resetDiagramOptions();
		} else {
			m_load_state           = other.m_load_state;
			m_edge_state           = other.m_edge_state;
			m_icon_rule            = other.m_icon_rule;
			m_iconPath             = other.m_iconPath;
			m_icon_fixed_shape     = other.m_icon_fixed_shape;
			m_elision_icon         = other.m_elision_icon;
			m_show_grid_state      = other.m_show_grid_state;
			m_snap_to_grid_state   = other.m_snap_to_grid_state;
			m_gridPixels           = other.m_gridPixels;
			m_gridColor            = other.m_gridColor;
			m_chase_edges          = other.m_chase_edges;
			m_chase_hide_state     = other.m_chase_hide_state;
			m_trace_lifted_state   = other.m_trace_lifted_state;
			m_group_query_state    = other.m_group_query_state;
			m_query_persists_state = other.m_query_persists_state;
			m_visible_spans_state  = other.m_visible_spans_state;
			m_show_span_type_state = other.m_show_span_type_state;
			m_show_class_type_state= other.m_show_class_type_state;
			m_show_navnext_state   = other.m_show_navnext_state;
			m_show_navprev_state   = other.m_show_navprev_state;
	}	}
		
//  =================================== Arrow options logic ==========================================

	// ArrowDimension options

	public static final int			LINE_WIDTH   = 0;
	public static final int			ARROW_LENGTH = 1;
	public static final int			ARROW_ARC    = 2;
	public static final int			PIXELS_3D    = 3;
	public static final int			SHADOW_SIZE  = 4;
	public static final int			LABEL_ANGLE  = 5;
	public static final int			HOVER_SCALE  = 6;
	public static final int			ZOOM_X       = 7;
	public static final int			ZOOM_Y       = 8;
	public static final int			ICON_PATH    = 9;
	public static final int			EDGE_MODE    = 10;
	public static final int			ICON_RULE    = 11;
	public static final int			CHASE_EDGES  = 12;

	private	int		m_line_width;
	private	double	m_arrow_length;
	private	double	m_arrow_arc;
	private	int		m_pixels_3d;
	private int		m_shadow_size;
	private double	m_label_angle;
	private double	m_hover_scale;
	private double	m_zoom_x;
	private	double	m_zoom_y;

	public final static String[] m_arrow_numeric_tags = 
	{
		"arrow:linewidth",
		"arrow:linelength",
		"arrow:arc",
		"arrow:3dpixels",
		"arrow:shadow",
		"arrow:labelarc",
		"arrow:hover",
		"arrow:zoomx",
		"arrow:zoomy",
		"arrow:iconpath",
		"arrow:edgemode",
		"arrow:iconrule",
		"arrow:chase"
	};	
	
	public static final int		ICON_FIXED_SHAPE   = 0;
	public static final int		FILL_ARROWHEAD     = 1;
	public static final int		CENTER_ARROWHEAD   = 2;
	public static final int		WEIGHTED_ARROWHEAD = 3;
	public static final int		BLACKWHITE_3D      = 4;
	public static final int		SHOW_EDGE_LABELS   = 5;
	public static final int     ROTATE_EDGE_LABELS = 6;
	public static final int		SHOW_EDGE_TOOLTIP  = 7;
	public static final int		VARIABLE_ARROW_COLOR = 8;
	public static final int		INVERT_EDGE_LABEL_BACKGROUND = 9;
	public static final int		LABEL_INVERT_FORE  = 10;
	public static final int		LABEL_INVERT_BACK  = 11;
	public static final int		LABEL_BLACK_WHITE  = 12;

	private final static String[] m_arrow_boolean_tags = 
	{
		"arrow:iconfixed",
		"arrow:fill",
		"arrow:center",
		"arrow:weighted",
		"arrow:blackwhite",
		"arrow:edgelabel",
		"arrow:rotate",
		"arrow:edgetip",
		"arrow:color",
		"arrow:invert",
		"arrow:labelfg",
		"arrow:labelbg",
		"arrow:labelbw"
	};	
	
	private	boolean	m_fill_arrowhead;
	private	boolean m_center_arrowhead;
	private	boolean m_permanently_weight;
	private	boolean m_blackwhite_3d;
	private	boolean m_show_edge_labels;
	private boolean m_rotate_edge_labels;
	private	boolean	m_show_edge_tooltip;
	private	boolean m_variable_arrow_color;
	private	boolean m_invert_edge_label_back;
	private	boolean m_label_invert_fore;
	private	boolean m_label_invert_back;
	private	boolean m_entity_labels_blackwhite;
	
	public void resetArrowOptions()
	{
		m_line_width   = 1;
		m_arrow_length = 10.0;
		m_arrow_arc    = 0.4;
		m_pixels_3d    = 3;
		m_shadow_size  = 5;
		m_label_angle  = 0.0;
		m_hover_scale  = 1.0;
		m_zoom_x       = 1.0;
		m_zoom_y       = 1.0;
	
		m_fill_arrowhead           = true;
		m_center_arrowhead         = false;
		m_permanently_weight       = true;
		m_blackwhite_3d            = true;
		m_show_edge_labels         = false;
		m_rotate_edge_labels       = false;
		m_show_edge_tooltip        = false;
		m_variable_arrow_color     = false;
		m_invert_edge_label_back   = false;
		m_label_invert_fore        = false;
		m_label_invert_back        = false;
		m_entity_labels_blackwhite = false;
	}
	
	public void setArrowOptionsTo(Option other)
	{
		if (other == null) {
			resetArrowOptions();
		} else {
			m_line_width               = other.m_line_width;
			m_arrow_length             = other.m_arrow_length;
			m_arrow_arc                = other.m_arrow_arc;
			m_pixels_3d                = other.m_pixels_3d;
			m_shadow_size              = other.m_shadow_size;
			m_label_angle              = other.m_label_angle;
			m_hover_scale              = other.m_hover_scale;
			m_zoom_x                   = other.m_zoom_x;
			m_zoom_y                   = other.m_zoom_y;

			m_fill_arrowhead           = other.m_fill_arrowhead;
			m_center_arrowhead         = other.m_center_arrowhead;
			m_permanently_weight       = other.m_permanently_weight;
			m_blackwhite_3d            = other.m_blackwhite_3d;
			m_show_edge_labels         = other.m_show_edge_labels;
			m_rotate_edge_labels       = other.m_rotate_edge_labels;
			m_show_edge_tooltip        = other.m_show_edge_tooltip;
			m_variable_arrow_color     = other.m_variable_arrow_color;
			m_invert_edge_label_back   = other.m_invert_edge_label_back;
			m_label_invert_fore        = other.m_label_invert_fore;
			m_label_invert_back        = other.m_label_invert_back;
			m_entity_labels_blackwhite = other.m_entity_labels_blackwhite;
	}	}
	
	public int getLineWidth()
	{
		return m_line_width;
	}
	
	public String setLineWidth(String string)
	{
		try {
			int ival = Integer.parseInt(string);
			
			if (ival < 0) {
				return "LineWidth may not be negative";
			}
			if (ival > 100) {
				return "LineWidth may not exceed 99";
			}
			m_line_width = ival;
		} catch (Throwable e) {
			return "Line width not an integer";
		}
		return null;
	}

	public double getArrowLength()
	{
		return m_arrow_length;
	}
	
	public String setArrowLength(String string)
	{
		try {
			double	dval = Double.parseDouble(string);

			if (Double.isNaN(dval)) {
				return "ArrowLength not allowed to be NaN";
			}
			if (dval < 0) {
				return "ArrowLength may not be negative";
			}
			m_arrow_length = dval;
		} catch (Throwable e) {
			return "ArrowLength is not a double";
		}
		return null;
	}
	
	public double getArrowArc()
	{
		return m_arrow_arc;
	}
		
	public String setArrowArc(String string)
	{
		try {
			double dval = Double.parseDouble(string);
			if (Double.isNaN(dval)) {
				return "ArrowArc not allowed to be NaN";
			}
			if (dval < 0) {
				return "ArrowArc may not be negative";
			}
			if (dval > 1.5) {
				return "The maximum allowed arc angle is 1.5 (very near pi/2)";
			}		
			m_arrow_arc = dval;
		} catch (Throwable e) {
			return "ArrowArc is not a double";
		}
		return null;
	}
	
	public int getPixels3D()
	{
		return m_pixels_3d;
	}	
	
	public String setPixels3D(String string)
	{
		try {
			int ival = Integer.parseInt(string);
			if (ival < 0) {
				return "Pixels may not be negative";
			}
			if (ival > 100) {
				return "Pixels may not exceed 99";
			}
			m_pixels_3d = ival;
		} catch (Throwable e) {
			return "Pixels not an integer";
		}
		return null;
	}
	
	public int getShadowSize()
	{
		return m_shadow_size;
	}		
	
	public String setShadowSize(String string)
	{
		try {
			int	ival = Integer.parseInt(string);
			
			if (ival < 0) {
				return "ShadowSize may not be negative";
			}
			if (ival > 100) {
				return "ShadowSize may not exceed 99";
			}
			m_shadow_size = ival;
		} catch (Throwable e) {
			return "ShadowSize not an integer";
		}
		return null;
	}
	
	public double getLabelAngle()
	{
		return m_label_angle;
	}
	
	public String setLabelAngle(String string)
	{
		try {
			double dval = Double.parseDouble(string);
			
			m_label_angle = Math.toRadians(dval);
		} catch (Throwable e) {
			return "LabelAngle is not a double";
		}
		return null;
	}
	
	public double getHoverScale()
	{
		return m_hover_scale;
	}	
	
	public String setHoverScale(String string)
	{
		try {
			double dval = Double.parseDouble(string);
			if (dval < 0.0) {
				return "HoverScale may not be negative";
			}
			if (dval > 10.0) {
				return "HoverScale may not exceed 10.0";
			} 
			m_hover_scale = dval;
		} catch (Throwable e) {
			return "HoverScale is not a double";
		}
		return null;
	}

	public double getZoomX()
	{
		return m_zoom_x;
	}	
	
	public void setZoomX(double val)
	{
		m_zoom_x = val;
	}	
	
	public String setZoomX(String string)
	{
		try {
			double dval = Double.parseDouble(string);
			if (dval < 1.0) {
				return "ZoomX may not be less than 1.0";
			}
			m_zoom_x = dval;
		} catch (Throwable e) {
			return "ZoomX is not a double";
		}
		return null;
	}

	public double getZoomY()
	{
		return m_zoom_y;
	}	

	public void setZoomY(double val)
	{
		m_zoom_y = val;
	}	
		
	public String setZoomY(String string)
	{
		try {
			double dval = Double.parseDouble(string);
			if (dval < 1.0) {
				return "ZoomY may not be less than 1.0";
			}
			m_zoom_y = dval;
		} catch (Throwable e) {
			return "ZoomY is not a double";
		}
		return null;
	}
		
	private String getArrowNumericParameter(int parameter)
	{
		switch(parameter) {
		case LINE_WIDTH:
			return "" + getLineWidth();
		case ARROW_LENGTH:
			return "" + getArrowLength();
		case ARROW_ARC:
			return "" + getArrowArc();
		case PIXELS_3D:
			return "" + getPixels3D();
		case SHADOW_SIZE:
			return "" + getShadowSize();
		case LABEL_ANGLE:
			return "" + getLabelAngle();
		case HOVER_SCALE:
			return "" + getHoverScale();
		case ZOOM_X:
			return "" + getZoomX();
		case ZOOM_Y:
			return "" + getZoomY();
		case EDGE_MODE:
			return "" + getEdgeMode();
		case ICON_PATH:
			return getIconPath();
		case ICON_RULE:
			return "" + getIconRule();
		case CHASE_EDGES:
			return "" + getChaseEdges();
		}
		return null;
	}
	
	private String setArrowNumericParameter(int parameter, String value)
	{
		String ret;
		
		switch(parameter) {
		case LINE_WIDTH:
			ret = setLineWidth(value);
			break;
		case ARROW_LENGTH:
			ret = setArrowLength(value);
			break;
		case ARROW_ARC:
			ret = setArrowArc(value);
			break;
		case PIXELS_3D:
			ret = setPixels3D(value);
			break;
		case SHADOW_SIZE:
			ret = setShadowSize(value);
			break;
		case LABEL_ANGLE:
			ret = setLabelAngle(value);
			break;
		case HOVER_SCALE:
			ret = setHoverScale(value);
			break;
		case ZOOM_X:
			ret = setZoomX(value);
			break;
		case ZOOM_Y:
			ret = setZoomY(value);
			break;
		case EDGE_MODE:
			ret = setEdgeMode(value);
			break;
		case ICON_PATH:
			ret = setIconPath(value);
			break;
		case ICON_RULE:
			ret = setIconRule(value);
			break;
		case CHASE_EDGES:
			ret = setChaseEdges(value);
			break;
		default:
			ret = "setArrowNumericParameter has no parameter " + parameter;
		}
		return ret;
	}
	
	public boolean isFillArrowhead()
	{
		return m_fill_arrowhead;
	}
	
	public void setFillArrowhead(boolean value)
	{
		m_fill_arrowhead = value;
	}
	
	public boolean isCenterArrowhead()
	{
		return m_center_arrowhead;
	}
	
	public void	setCenterArrowhead(boolean value)
	{
		m_center_arrowhead = value;
	}	
	
	public boolean isPermanentlyWeight()
	{
		return m_permanently_weight;
	}
	
	public void setPermanentlyWeight(boolean value)
	{
		m_permanently_weight = value;
	}
	
	public boolean isBlackWhite3D()
	{
		return m_blackwhite_3d;
	}
	
	public void setBlackWhite3D(boolean value)
	{
		m_blackwhite_3d = value;
	}
	
	public boolean isShowEdgeLabels()
	{
		return m_show_edge_labels;
	}
	
	public void setShowEdgeLabels(boolean value)
	{
		m_show_edge_labels = value;
	}
	
	public boolean isRotateEdgeLabels()
	{
		return m_rotate_edge_labels;
	}
	
	public void setRotateEdgeLabels(boolean value)
	{
		m_rotate_edge_labels = value;
	}
	
	public boolean isShowEdgeTooltip()
	{
		return m_show_edge_tooltip;
	}
	
	public void setShowEdgeTooltip(boolean value)
	{
		m_show_edge_tooltip = value;
	}
	
	public boolean isVariableArrowColor()
	{
		return m_variable_arrow_color;
	}
	
	public void setVariableArrowColor(boolean value)
	{
		m_variable_arrow_color = value;
	}
	
	public boolean isInvertEdgeLabelBackground()
	{
		return m_invert_edge_label_back;
	}
	
	public void setInvertEdgeLabelBackground(boolean value)
	{
		m_invert_edge_label_back = value;
	}
	
	public boolean isLabelInvertForeground()
	{
		return m_label_invert_fore;
	}
	
	public void setLabelInvertForeground(boolean value)
	{
		m_label_invert_fore = value;
	}
	
	public boolean isLabelBlackWhite()
	{
		return m_entity_labels_blackwhite;
	}

	public void setLabelBlackWhite(boolean value)
	{
		m_entity_labels_blackwhite = value;
	}
	
	public boolean isLabelInvertBackground()
	{
		return m_label_invert_back;
	}
	
	public void setLabelInvertBackground(boolean value)
	{
		m_label_invert_back = value;
	}
	
	private boolean getArrowBooleanParameter(int parameter)
	{
		boolean bool = false;
	
		switch (parameter) {
		case ICON_FIXED_SHAPE:
			bool = isIconFixedShape();
			break;
		case FILL_ARROWHEAD:
			bool = isFillArrowhead();
			break; 
		case CENTER_ARROWHEAD:
			bool = isCenterArrowhead();
			break; 
		case WEIGHTED_ARROWHEAD:
			bool = isPermanentlyWeight();
			break; 
		case BLACKWHITE_3D:
			bool = isBlackWhite3D();
			break; 
		case SHOW_EDGE_LABELS:
			bool = isShowEdgeLabels();
			break; 
		case ROTATE_EDGE_LABELS:
			bool = isRotateEdgeLabels();
			break; 
		case SHOW_EDGE_TOOLTIP:
			bool = isShowEdgeTooltip();
			break; 
		case VARIABLE_ARROW_COLOR:
			bool = isVariableArrowColor();
			break; 
		case INVERT_EDGE_LABEL_BACKGROUND:
			bool = isInvertEdgeLabelBackground();
			break; 
		case LABEL_INVERT_FORE:
			bool = isLabelInvertForeground();
			break; 
		case LABEL_INVERT_BACK:
			bool = isLabelInvertBackground();
			break; 
		case LABEL_BLACK_WHITE:
			bool = isLabelBlackWhite();
			break; 
		}
		return bool;
	}
	
	private void setArrowBooleanParameter(int parameter, boolean bool)
	{
		switch (parameter) {
		case ICON_FIXED_SHAPE:
			setIconFixedShape(bool);
			break;
		case FILL_ARROWHEAD:
			setFillArrowhead(bool);
			break; 
		case CENTER_ARROWHEAD:
			setCenterArrowhead(bool);
			break; 
		case WEIGHTED_ARROWHEAD:
			setPermanentlyWeight(bool);
			break; 
		case BLACKWHITE_3D:
			setBlackWhite3D(bool);
			break; 
		case SHOW_EDGE_LABELS:
			setShowEdgeLabels(bool);
			break; 
		case ROTATE_EDGE_LABELS:
			setRotateEdgeLabels(bool);
			break; 
		case SHOW_EDGE_TOOLTIP:
			setShowEdgeTooltip(bool);
			break; 
		case VARIABLE_ARROW_COLOR:
			setVariableArrowColor(bool);
			break; 
		case INVERT_EDGE_LABEL_BACKGROUND:
			setInvertEdgeLabelBackground(bool);
			break; 
		case LEGEND_LABELS_BLACK:
			setLegendLabelBlack(bool);
			break; 
		case LABEL_INVERT_FORE:
			setLabelInvertForeground(bool);
			break; 
		case LABEL_INVERT_BACK:
			setLabelInvertBackground(bool);
			break; 
		case LABEL_BLACK_WHITE:
			setLabelBlackWhite(bool);
			break; 
	}	}
	
	private void setArrowBooleanParameter(int parameter, String value)
	{
		boolean bool = ((value.charAt(0) == 't') ? true : false);
		
		setArrowBooleanParameter(parameter, bool);
	}
	
	public void loadArrowOption(String attribute, String value)
	{
		String[]	arrow_numeric_tags, arrow_boolean_tags;	
		int			i;

		arrow_numeric_tags = m_arrow_numeric_tags;
		for (i = 0; i < arrow_numeric_tags.length; ++i) {
			if (attribute.equals(arrow_numeric_tags[i])) {
				setArrowNumericParameter(i, value);
				return;
		}	}

		arrow_boolean_tags = m_arrow_boolean_tags;
		for (i = 0; i < arrow_boolean_tags.length; ++i) {
			if (attribute.equals(arrow_boolean_tags[i])) {
				setArrowBooleanParameter(i, value);
				return;
	}	}	}
	
	private void saveArrowOptions(PrintWriter ps, boolean toTa)
	{
		String[]	arrow_numeric_tags = m_arrow_numeric_tags;
		String[]	arrow_boolean_tags = m_arrow_boolean_tags;	
		int			i;
		
		for (i = 0; i < arrow_numeric_tags.length; ++i) {
			if (i != ICON_PATH || !toTa) {
				ps.println(arrow_numeric_tags[i] + "=" + getArrowNumericParameter(i));
			} else {
				ps.println(arrow_numeric_tags[i] + "=\""  + m_iconPath + "\"");
		}	}

		for (i = 0; i < arrow_boolean_tags.length; ++i) {
			ps.println(arrow_boolean_tags[i] + "=" + (getArrowBooleanParameter(i) ? "true" : "false"));
	}	}	
	
//  ============================ Main options =======================================================

	protected final String[] m_boolean_option_tags = 
	{
		"option:showdesc",
		"option:showfeedback",
		"option:lefttabbox",
		"option:tabsscroll",
		"option:fixedscrollbars",
		"option:sorttoc",
		"option:topclients",
		"option:showclients",
		"option:showsuppliers",
		"option:usecompaction",
		"option:visibleedges",
		"option:visibleentities",
		"option:liftedges",
		"option:dstcardinals",
		"option:srccardinals",
		"option:groupquery",
		"option:querypersists",
		"option:focusancestor",
		"option:hideempty",
		"option:membercounts",
		"option:inheritance",
		"option:legendblack",
		"option:showgrid",
		"option:snaptogrid",
		"option:chasehide",
		"option:toolbar",
		"option:visiblespans",
		"option:tracelifted"
	};

	private static final int		SHOW_DESC          = 0;
	private static final int		SHOW_FEEDBACK      = 1;
	private static final int		LEFT_TABBOX        = 2;
	private static final int		TABS_SCROLL        = 3;
	private static final int		FIXED_SCROLLBARS   = 4;
	private static final int		SORT_TOC           = 5;

	private static final int		TOP_CLIENTS        = 6;
	private static final int		SHOW_CLIENTS       = 7;
	private static final int		SHOW_SUPPLIERS     = 8;
	private static final int		USE_COMPACTION     = 9;
	private static final int		VISIBLE_EDGES      = 10;
	private static final int		VISIBLE_ENTITIES   = 11;

	private static final int		LIFT_EDGES         = 12;
	private static final int        SHOW_DST_CARDINALS = 13;
	private static final int		SHOW_SRC_CARDINALS = 14;
	
	private static final int		GROUP_QUERY        = 15;
	private static final int		QUERY_PERSISTS     = 16;
	private static final int		FOCUS_ANCESTOR     = 17;
	private static final int		HIDE_EMPTY         = 18;
	private static final int		MEMBER_COUNTS      = 19;
	private static final int        SHOW_INHERITANCE   = 20;
	private static final int		LEGEND_LABELS_BLACK= 21;
	private static final int		SHOW_GRID          = 22;
	private static final int		SNAP_TO_GRID       = 23;
	private static final int		CHASE_HIDE         = 24;
	private static final int		SHOW_TOOLBAR       = 25;
	private static final int        VISIBLE_SPANS      = 26;
	private static final int		TRACE_LIFTED       = 27;
	
	public    static final boolean SHOW_DESC_DEFAULT      = true;
	public 	  static final boolean SHOW_FEEDBACK_DEFAULT  = true;
	public	  static final boolean TOP_CLIENTS_DEFAULT	 = true;
	public    static final boolean SHOW_CLIENTS_DEFAULT  = true;
	public    static final boolean SHOW_SUPPLIERS_DEFAULT= true;
	public	  static final boolean USE_COMPACTION_DEFAULT= true;
	public    static final boolean SHOW_DST_CARDINALS_DEFAULT= false;
	public    static final boolean SHOW_SRC_CARDINALS_DEFAULT= false;
	public	  static final boolean FIX_SCROLLBARS_DEFAULT   = false;
	public	  static final boolean LEFT_TABBOX_DEFAULT      = false;
	public	  static final boolean TABS_SCROLL_DEFAULT      = false;
	public    static final boolean LIFT_EDGES_DEFAULT       = true;
	public    static final boolean VISIBLE_EDGES_DEFAULT    = false;
	public	  static final boolean VISIBLE_ENTITIES_DEFAULT = false;
	public    static final boolean FOCUS_ANCESTOR_DEFAULT   = false;
	public    static final boolean SORT_TOC_DEFAULT         = false;
	public	  static final boolean HIDE_EMPTY_DEFAULT       = false;
	public	  static final boolean MEMBER_COUNTS_DEFAULT    = false;
	public	  static final boolean INHERITANCE_DEFAULT      = false;
	public	  static final boolean LEGEND_BLACK_DEFAULT     = false;
	public	  static final boolean SHOW_GRID_DEFAULT        = false;
	public	  static final boolean SNAP_TO_GRID_DEFAULT     = true;
	public    static final boolean SHOW_TOOLBAR_DEFAULT     = true;

	protected boolean m_show_toolbar_state   = SHOW_TOOLBAR_DEFAULT;
	protected boolean m_show_desc_state      = SHOW_DESC_DEFAULT;
	protected boolean m_show_feedback_state  = SHOW_FEEDBACK_DEFAULT;
	protected boolean m_top_clients_state    = TOP_CLIENTS_DEFAULT;
	protected boolean m_show_clients_state   = SHOW_CLIENTS_DEFAULT;
	protected boolean m_show_suppliers_state = SHOW_SUPPLIERS_DEFAULT;
	protected boolean m_use_compaction_state = USE_COMPACTION_DEFAULT;
	protected boolean m_show_dst_cardinals_state = SHOW_DST_CARDINALS_DEFAULT;
	protected boolean m_show_src_cardinals_state = SHOW_SRC_CARDINALS_DEFAULT;
	protected boolean m_fix_scrollbars_state = FIX_SCROLLBARS_DEFAULT;
	protected boolean m_left_tabbox_state    = LEFT_TABBOX_DEFAULT;
	protected boolean m_tabs_scroll_state    = TABS_SCROLL_DEFAULT;
	protected boolean m_lift_edges_state     = LIFT_EDGES_DEFAULT;
	protected boolean m_visible_edges_state  = VISIBLE_EDGES_DEFAULT;
	protected boolean m_visible_entities_state  = VISIBLE_ENTITIES_DEFAULT;

	protected boolean m_focus_ancestor_state = FOCUS_ANCESTOR_DEFAULT;
	protected boolean m_sort_toc_state       = SORT_TOC_DEFAULT;

	protected boolean m_hide_empty_state     = HIDE_EMPTY_DEFAULT;
	protected boolean m_member_counts_state  = MEMBER_COUNTS_DEFAULT;
	protected boolean m_inheritance_state    = INHERITANCE_DEFAULT;
	protected boolean m_legend_labels_black  = LEGEND_BLACK_DEFAULT;

	public void resetMainOptions()
	{
		m_top_clients_state    = TOP_CLIENTS_DEFAULT;
		m_show_desc_state      = SHOW_DESC_DEFAULT;
		m_show_feedback_state  = SHOW_FEEDBACK_DEFAULT;
		m_show_clients_state   = SHOW_CLIENTS_DEFAULT;
		m_show_suppliers_state = SHOW_SUPPLIERS_DEFAULT;
		m_show_dst_cardinals_state = SHOW_DST_CARDINALS_DEFAULT;
		m_show_src_cardinals_state = SHOW_SRC_CARDINALS_DEFAULT;
		m_use_compaction_state = USE_COMPACTION_DEFAULT;
		m_fix_scrollbars_state = FIX_SCROLLBARS_DEFAULT;
		m_left_tabbox_state    = LEFT_TABBOX_DEFAULT;
		m_tabs_scroll_state    = TABS_SCROLL_DEFAULT;
		m_lift_edges_state     = LIFT_EDGES_DEFAULT;
		m_visible_edges_state  = VISIBLE_EDGES_DEFAULT;
		m_visible_entities_state  = VISIBLE_ENTITIES_DEFAULT;

		m_focus_ancestor_state = FOCUS_ANCESTOR_DEFAULT;
		m_sort_toc_state       = SORT_TOC_DEFAULT;

		m_hide_empty_state     = HIDE_EMPTY_DEFAULT;
		m_member_counts_state  = MEMBER_COUNTS_DEFAULT;
		m_inheritance_state    = INHERITANCE_DEFAULT;
		m_legend_labels_black  = LEGEND_BLACK_DEFAULT;
		m_show_toolbar_state   = SHOW_TOOLBAR_DEFAULT;
	}
	
	public void setMainOptionsTo(Option other)
	{
		if (other == null) {
			resetMainOptions();
		} else {
			m_top_clients_state    = other.m_top_clients_state;
			m_show_desc_state      = other.m_show_desc_state;
			m_show_feedback_state  = other.m_show_feedback_state;
			m_show_clients_state   = other.m_show_clients_state;
			m_show_suppliers_state = other.m_show_suppliers_state;
			m_show_dst_cardinals_state = other.m_show_dst_cardinals_state;
			m_show_src_cardinals_state = other.m_show_src_cardinals_state;
			m_use_compaction_state = other.m_use_compaction_state;
			m_fix_scrollbars_state = other.m_fix_scrollbars_state;
			m_left_tabbox_state    = other.m_left_tabbox_state;
			m_tabs_scroll_state    = other.m_tabs_scroll_state;
			m_lift_edges_state     = other.m_lift_edges_state;
			m_visible_edges_state  = other.m_visible_edges_state;
			m_visible_entities_state  = other.m_visible_entities_state;

			m_focus_ancestor_state = other.m_focus_ancestor_state;
			m_sort_toc_state       = other.m_sort_toc_state;

			m_hide_empty_state     = other.m_hide_empty_state;
			m_member_counts_state  = other.m_member_counts_state;
			m_inheritance_state    = other.m_inheritance_state;
			m_legend_labels_black  = other.m_legend_labels_black;
			m_show_toolbar_state   = other.m_show_toolbar_state;
	}	}

	public boolean isShowToolbar()
	{
		return m_show_toolbar_state;
	}

	public void setShowToolbar(boolean value)
	{
		m_show_toolbar_state = value;
	}
	
	public boolean isTopClients()
	{
		return m_top_clients_state;
	}

	public void setTopClients(boolean value)
	{
		m_top_clients_state = value;
	}

	public boolean isShowDesc()
	{
		return m_show_desc_state;
	}

	public void setShowDesc(boolean value)
	{
		m_show_desc_state = value;
	}

	public boolean isShowFeedback()
	{
		return m_show_feedback_state;
	}

	public void setShowFeedback(boolean value)
	{
		m_show_feedback_state = value;
	}

	public boolean isShowClients()
	{
		return m_show_clients_state;
	}

	public void setShowClients(boolean value)
	{
		m_show_clients_state = value;
	}

	public boolean isShowSuppliers()
	{
		return m_show_suppliers_state;
	}

	public void setShowSuppliers(boolean value)
	{
		m_show_suppliers_state = value;
	}

	public boolean isShowDstCardinals()
	{
		return m_show_dst_cardinals_state;
	}

	public void setShowDstCardinals(boolean value)
	{
		m_show_dst_cardinals_state = value;
	}

	public boolean isShowSrcCardinals()
	{
		return m_show_src_cardinals_state;
	}

	public void setShowSrcCardinals(boolean value)
	{
		m_show_src_cardinals_state = value;
	}

	public boolean isUseCompaction()
	{
		return m_use_compaction_state;
	}

	public void setUseCompaction(boolean value)
	{
		m_use_compaction_state = value;
	}

	public boolean isFixScrollBars()
	{
		return m_fix_scrollbars_state;
	}

	public void setFixScrollBars(boolean value)
	{
		m_fix_scrollbars_state = value;
	}

	public boolean isLeftTabbox()
	{
		return m_left_tabbox_state;
	}

	public void setLeftTabbox(boolean value)
	{
		m_left_tabbox_state = value;
	}

	public boolean isTabsScroll()
	{
		return m_tabs_scroll_state;
	}

	public void setTabsScroll(boolean value)
	{
		m_tabs_scroll_state = value;
	}

	public boolean isLiftEdges()
	{
		return m_lift_edges_state;
	}

	public void setLiftEdges(boolean value)
	{
		m_lift_edges_state = value;
	}

	public boolean isVisibleEdges()
	{
		return m_visible_edges_state;
	}

	public void setVisibleEdges(boolean value)
	{
		m_visible_edges_state = value;
	}

	public boolean isVisibleEntities()
	{
		return m_visible_entities_state;
	}

	public void setVisibleEntities(boolean value)
	{
		m_visible_entities_state = value;
	}

	public boolean isHideEmpty()
	{
		return m_hide_empty_state;
	}

	public void setHideEmpty(boolean value)
	{
		m_hide_empty_state = value;
	}

	public boolean isMemberCounts()
	{
		return m_member_counts_state;
	}

	public void setMemberCounts(boolean value)
	{
		m_member_counts_state = value;
	}

	public boolean isShowInheritance()
	{
		return m_inheritance_state;
	}

	public void setShowInheritance(boolean value)
	{
		m_inheritance_state = value;
	}

	public boolean isLegendLabelBlack()
	{
		return m_legend_labels_black;
	}
	
	public void setLegendLabelBlack(boolean value)
	{
		m_legend_labels_black = value;
	}

	public boolean isFocusAncestor()
	{
		return m_focus_ancestor_state;
	}

	public void setFocusAncestor(boolean value)
	{
		m_focus_ancestor_state = value;
	}

	public boolean isSortTOC()
	{
		return m_sort_toc_state;
	}

	public void setSortTOC(boolean value)
	{
		m_sort_toc_state = value;
	}
	
	public String getMainNumericParameter(int parameter)
	{
		int val;

		switch(parameter) {
		case LOAD_RULE:
			val = getLoadMode();
			break;
		case GRIDSIZE:
			val = getGridSize();
			break;
		case GRIDCOLOR:
			return Util.taColor(getGridColor());
		case ELISIONICON:
			val = getElisionIcon();
			break;
		case SHOW_CLASS_TYPE:
			val = getShowClassType();
			break;
		case SHOW_SPAN_TYPE:
			val = getShowSpanType();
			break;
		case SHOW_NAVNEXT:
			val = getShowNavNext();
			break;
		case SHOW_NAVPREV:
			val = getShowNavPrev();
			break;
		default:
			return null;
		}
		return "" + val;
	}
	
	public void setMainNumericParameter(int parameter, String value)
	{
		switch(parameter) {
		case LOAD_RULE:
			setLoadMode(value);
			break;
		case GRIDSIZE:
			setGridSize(value);
			break;
		case GRIDCOLOR:
			setGridColor(Util.colorTa(value));
			break;
		case ELISIONICON:
			setElisionIcon(value);
		case SHOW_CLASS_TYPE:
			setShowClassType(value);
			break;
		case SHOW_SPAN_TYPE:
			setShowSpanType(value);
			break;
		case SHOW_NAVNEXT:
			setShowNavNext(value);
			break;
		case SHOW_NAVPREV:
			setShowNavPrev(value);
			break;
	}	}
		
	private boolean getMainBooleanParameter(int i)
	{
		switch (i) {
		case SHOW_DESC:				return m_show_desc_state;
		case SHOW_FEEDBACK:			return m_show_feedback_state;
		case TOP_CLIENTS:			return m_top_clients_state;
		case SHOW_CLIENTS:			return m_show_clients_state;
		case SHOW_SUPPLIERS:		return m_show_suppliers_state;
		case USE_COMPACTION:		return m_use_compaction_state;		
		case SHOW_DST_CARDINALS:	return m_show_dst_cardinals_state;
		case SHOW_SRC_CARDINALS:	return m_show_src_cardinals_state;
		case FIXED_SCROLLBARS:		return m_fix_scrollbars_state;		
		case LEFT_TABBOX:			return m_left_tabbox_state;
		case TABS_SCROLL:			return m_tabs_scroll_state;
		case LIFT_EDGES:			return m_lift_edges_state;
		case VISIBLE_EDGES:			return m_visible_edges_state;
		case VISIBLE_ENTITIES:		return m_visible_entities_state;
		case TRACE_LIFTED:			return m_trace_lifted_state;
		case GROUP_QUERY:			return m_group_query_state;
		case QUERY_PERSISTS:		return m_query_persists_state;
		case VISIBLE_SPANS:         return m_visible_spans_state;
		case FOCUS_ANCESTOR:		return m_focus_ancestor_state;
		case SORT_TOC:				return m_sort_toc_state;
		case HIDE_EMPTY:			return m_hide_empty_state;
		case MEMBER_COUNTS:			return m_member_counts_state;
		case SHOW_INHERITANCE:		return m_inheritance_state;
		case LEGEND_LABELS_BLACK:	return m_legend_labels_black;
		case SHOW_GRID:				return m_show_grid_state;
		case SNAP_TO_GRID:			return m_snap_to_grid_state;
		case CHASE_HIDE:            return m_chase_hide_state;
		case SHOW_TOOLBAR:			return m_show_toolbar_state;
		}
		return false;
	}

	public void setMainBooleanParameter(int parameter, boolean bool)
	{
		switch (parameter) {
		case SHOW_DESC:
			setShowDesc(bool);
			break;
		case SHOW_FEEDBACK:
			setShowFeedback(bool);
			break;
		case TOP_CLIENTS:
			m_top_clients_state = bool;
			break;
		case SHOW_CLIENTS:
			m_show_clients_state = bool;
			break;
		case SHOW_SUPPLIERS:
			m_show_suppliers_state = bool;
			break;
		case USE_COMPACTION:
			m_use_compaction_state = bool;
			break;		
		case SHOW_DST_CARDINALS:
			m_show_dst_cardinals_state = bool;
			break;
		case SHOW_SRC_CARDINALS:
			m_show_src_cardinals_state = bool;
			break;
		case FIXED_SCROLLBARS:
			m_fix_scrollbars_state = bool;
			break;		
		case LEFT_TABBOX:
			m_left_tabbox_state = bool;
			break;
		case TABS_SCROLL:
			m_tabs_scroll_state = bool;
			break;
		case LIFT_EDGES:
			m_lift_edges_state = bool;
			break;
		case VISIBLE_EDGES:
			m_visible_edges_state = bool;
			break;
		case VISIBLE_ENTITIES:
			m_visible_entities_state = bool;
			break;
		case TRACE_LIFTED:
			m_trace_lifted_state = bool;
			break;
		case GROUP_QUERY:
			m_group_query_state = bool;
			break;
		case QUERY_PERSISTS:
			m_query_persists_state = bool;
			break;
		case VISIBLE_SPANS:
			m_visible_spans_state = bool;
			break;
		case FOCUS_ANCESTOR:
			m_focus_ancestor_state = bool;
			break;
		case SORT_TOC:
			m_sort_toc_state = bool;
			break;
		case HIDE_EMPTY:
			m_hide_empty_state = bool;
			break;
		case MEMBER_COUNTS:
			m_member_counts_state = bool;
			break;
		case SHOW_INHERITANCE:
			m_inheritance_state = bool;
			break;
		case LEGEND_LABELS_BLACK:	
			m_legend_labels_black = bool;
			break;
		case SHOW_GRID:
			m_show_grid_state = bool;
			break;
		case SNAP_TO_GRID:
			m_snap_to_grid_state = bool;
			break;
		case CHASE_HIDE:
			m_chase_hide_state = bool;
			break;
		case SHOW_TOOLBAR:
			m_show_toolbar_state = bool;
			break;
	}	}

	public void setMainBooleanParameter(int parameter, String value)
	{
		boolean bool = ((value.charAt(0) == 't') ? true : false);
		
		setMainBooleanParameter(parameter, bool);
	}
	
	public void optionsChanged(LandscapeEditorCore ls, Option other)
	{
		boolean	change1 = false;
		boolean change2 = false;
		boolean change3 = false;

		if (m_show_toolbar_state != other.m_show_toolbar_state) {
			ls.showToolbarChanged();
		}
		
		change1 =	(m_line_width != other.m_line_width) ||
					(m_arrow_length != other.m_arrow_length) ||
					(m_arrow_arc != other.m_arrow_arc);
					
		change2 =	(m_icon_rule != other.m_icon_rule);
		
		if (m_elision_icon != other.m_elision_icon) {
			IconCache.clearElisionCache();
		}
		
		if (m_icon_fixed_shape != other.m_icon_fixed_shape) {
			change2 = true;
			ls.iconFixedChanged();
		}
		        
		if (!m_iconPath.equals(other.m_iconPath)) {
			change1 = true;
			ls.iconPathChanged();
		}
		    
		change3 = 	(m_variable_arrow_color != other.m_variable_arrow_color);

		if (m_left_tabbox_state != other.m_left_tabbox_state) {
			ls.changeLeftTabbox(m_left_tabbox_state);
		}

		if (m_fix_scrollbars_state != other.m_fix_scrollbars_state) {
			ls.changeFixScrollbars(m_fix_scrollbars_state);
		}

		if (m_show_desc_state != other.m_show_desc_state) {
			ls.changeShowDesc(m_show_desc_state);
		}
		
		if (m_show_feedback_state != other.m_show_feedback_state) {
			ls.changeShowFeedback(m_show_feedback_state);
		}
		
		if (m_tabs_scroll_state != other.m_tabs_scroll_state) {
			ls.changeTabsScroll(m_tabs_scroll_state);
		}

		if (change1 || change2 || change3 ||
		    (m_hide_empty_state    != other.m_hide_empty_state) || 
			(m_member_counts_state != other.m_member_counts_state) ||
			(m_inheritance_state   != other.m_inheritance_state) ||
			(m_legend_labels_black != other.m_legend_labels_black)
		   ) {
			ls.changeLegendQuery();
		}
		
		if (m_trace_lifted_state != other.m_trace_lifted_state) {
			ls.changeTraceLifted(m_trace_lifted_state);
		}
	
		if (m_group_query_state != other.m_group_query_state) {
			ls.changeGroupQuery(m_group_query_state);
		}

		if (m_query_persists_state != other.m_query_persists_state) {
			ls.changeQueryPersists(m_query_persists_state);
		}

		if (m_sort_toc_state != other.m_sort_toc_state) {
			ls.changeTOC();
		} else if (change2) {
			ls.repaintTOC();
		}
		
		if ((m_zoom_x != other.m_zoom_x) ||
			(m_zoom_y != other.m_zoom_y)
		   ) {
			ls.zoomChanged();
			return;
		}

		if ( change1 ||
			(m_edge_state != other.m_edge_state) ||
			(m_pixels_3d != other.m_pixels_3d) ||
			(m_shadow_size != other.m_shadow_size) ||
			(m_center_arrowhead != other.m_center_arrowhead) || 
			(m_show_edge_labels != other.m_show_edge_labels) ||
			(m_show_edge_tooltip != other.m_show_edge_tooltip) ||
			(m_show_edge_labels && ((m_label_angle != other.m_label_angle) || (m_rotate_edge_labels != other.m_rotate_edge_labels))) ||
			(m_zoom_x != other.m_zoom_x) ||
			(m_zoom_y != other.m_zoom_y)
		   ) {
			ls.refillDiagram();
			return;
		}
						
		if (	
			(m_show_clients_state != other.m_show_clients_state) ||
			(m_show_suppliers_state != other.m_show_suppliers_state) ||
			(m_show_dst_cardinals_state != other.m_show_dst_cardinals_state) ||
			(m_show_src_cardinals_state != other.m_show_src_cardinals_state) ||
			(m_lift_edges_state != other.m_lift_edges_state)
		   ) {
			ls.refillDiagram();
			return;
		}
		
		if (m_show_clients_state || m_show_suppliers_state) {
			if ((m_top_clients_state != other.m_top_clients_state) ||
				(m_use_compaction_state != other.m_use_compaction_state) ||
				(m_visible_edges_state != other.m_visible_edges_state) ||
				(m_visible_entities_state != other.m_visible_entities_state)
			   ) {
				ls.refillDiagram();
				return;
		}	}
		
		if (m_chase_edges != other.m_chase_edges) {
			ls.refillDiagram();
			return;
		}

		if ( change2 || change3 ||
			(m_fill_arrowhead != other.m_fill_arrowhead) ||
			(m_permanently_weight != other.m_permanently_weight) ||
			(m_blackwhite_3d != other.m_blackwhite_3d) ||
			(m_invert_edge_label_back != other.m_invert_edge_label_back) ||
			(m_label_invert_fore != other.m_label_invert_fore) ||
			(m_label_invert_back != other.m_label_invert_back) ||
			(m_entity_labels_blackwhite != other.m_entity_labels_blackwhite) ||
			(m_elision_icon != other.m_elision_icon) ||
			(m_show_grid_state != other.m_show_grid_state) ||
			(m_show_class_type_state != other.m_show_class_type_state) ||
			(m_show_span_type_state != other.m_show_span_type_state) ||
			(m_show_navnext_state != other.m_show_navnext_state) ||
			(m_show_navprev_state != other.m_show_navprev_state) ||
			(m_show_grid_state && ((m_gridPixels != other.m_gridPixels) ||
				                   (m_gridColor  != other.m_gridColor)
				                 ))
		   ) {
			ls.repaintDiagram();
		}
	}

	public void loadMainOption(String attribute, String value)
	{
		String[]	numeric_option_tags = m_numeric_option_tags;
		String[]	boolean_option_tags = m_boolean_option_tags;
		int			i;

		for (i = 0; i < numeric_option_tags.length; ++i) {
			if (attribute.equals(numeric_option_tags[i])) {
				setMainNumericParameter(i, value);
				return;
		}	}

		for (i = 0; i < boolean_option_tags.length; ++i) {
			if (attribute.equals(boolean_option_tags[i])) {
				setMainBooleanParameter(i, value);
				return;
	}	}	}
	
	private void saveMainOptions(PrintWriter ps, boolean toTA)
	{
		String[]	numeric_option_tags = m_numeric_option_tags;
		String[]	boolean_option_tags = m_boolean_option_tags;
		int			i;

		for (i = (toTA ? 1 : 0); i < numeric_option_tags.length; ++i) {
			ps.println(numeric_option_tags[i] + "=" + getMainNumericParameter(i));
		}

		for (i = 0; i < boolean_option_tags.length; ++i) {
			ps.println(boolean_option_tags[i] + "=" + (getMainBooleanParameter(i) ? "true" : "false"));
	}	}
	
	public void loadOption(String attribute, String value)
	{
		if (attribute.startsWith("font[")) {
			loadFont(attribute, value);
			return;
		}
		if (attribute.startsWith("option:")) {
			loadMainOption(attribute, value);
			return;
		}
		if (attribute.startsWith("arrow:")) {
			loadArrowOption(attribute, value);
			return;
	}	}
	
	public void saveOptions(PrintWriter ps, boolean toTa)
	{
		saveFonts(ps);
		saveMainOptions(ps, toTa);
		saveArrowOptions(ps, toTa);
	}
	
	public void setTo(Option other)
	{
		setFontsTo(other);
		setMainOptionsTo(other);
		setArrowOptionsTo(other);
		setDiagramOptionsTo(other);
	}
	
	public String toString()
	{
		return m_option_name + " option " + hashCode();
	}
} 

