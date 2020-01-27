package lsedit;

import java.awt.Font;

import javax.swing.JMenuBar;
import javax.swing.JMenu;

public class Options { 

	public  final static int DEFAULT_OPTION   = 0;
	public	final static int LANDSCAPE_OPTION = 1;
	public	final static int DIAGRAM_OPTION   = 2;
	
	private	static	Option			m_defaultOption   = new Option("Default");		// The default value for all options
	private	static	Option			m_landscapeOption = new Option("Landscape");	// The landscape value for all options
	private	static  Option			m_diagramOption   = new Option("Diagram");		// The diagram value for all options
		
	public static Option getDefaultOptions()
	{
		return m_defaultOption;
	}
	
	public static Option getLandscapeOptions()
	{
		return m_landscapeOption;
	}
	
	public static Option getDiagramOptions()
	{
		return m_diagramOption;
	}
	
	public static void setDiagramOptions(Option diagramOption)
	{
		m_diagramOption = diagramOption;
	}
	
	// ============================== Font options ===============================================
	
	public static void resetFonts()
	{
		m_defaultOption.resetFonts();
		m_landscapeOption.resetFonts();
		m_diagramOption.resetFonts();
	}
	
	public static void useDefaultFonts()
	{
		m_landscapeOption.setFontsTo(m_defaultOption);
		m_diagramOption.setFontsTo(m_defaultOption);
	}
	
	public static void setDefaultFonts()
	{
		m_defaultOption.setFontsTo(m_diagramOption);
	}
	
	public static void saveLandscapeFonts()
	{
		m_landscapeOption.setFontsTo(m_diagramOption);
	}
	
	public static void forgetDiagramFonts()
	{
		m_diagramOption.setFontsTo(m_landscapeOption);
	}		
	
	public static Font getTargetFont(int target)
	{
		return m_diagramOption.getTargetFont(target);
	}

	// ============================== Arrow options ===============================================

	public static double getHoverScale()
	{
		return m_diagramOption.getHoverScale();
	}
	
	public static boolean isTraceLifted()
	{
		return m_diagramOption.isTraceLifted();
	}
	
	public static boolean isQueryPersists()
	{
		return m_diagramOption.isQueryPersists();
	}
	
	public static boolean isGroupQuery()
	{
		return m_diagramOption.isGroupQuery();
	}
	
	public static int getLineWidth()
	{
		return m_diagramOption.getLineWidth();
	}
	
	public static double getArrowLength()
	{
		return m_diagramOption.getArrowLength();
	}
} 



