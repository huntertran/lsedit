package lsedit;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

import javax.swing.event.ChangeListener;

public final class LegendBox extends ERBox /* extends TabBox extends JComponent */ implements ChangeListener, TaListener, MouseListener 
{
	public final static String m_helpStr	 = "This box shows the types of entities and relations that are present in the current landscape.";

	protected static Font	m_titleFont = null;
	protected static Font	m_textFont  = null;

	// --------------
	// Public methods 
	// --------------

	public LegendBox(LandscapeEditorCore ls, JTabbedPane tabbedPane) 
	{
		super(ls, tabbedPane, "Legend", m_helpStr);

		Font titleFont = m_titleFont;
		Font textFont  = m_textFont;

		if (titleFont == null) {
			m_titleFont = titleFont = Options.getTargetFont(Option.FONT_LEGEND_TITLE);
		}
		if (textFont == null) {
			m_textFont = textFont = Options.getTargetFont(Option.FONT_LEGEND_TEXT);
		}

		m_ulabel = new JLabel("Entity Classes");
		m_ulabel.setForeground(Color.red);

		m_elabel1 = new JLabel("Pass cursor over entity for description.");
		m_elabel1.setForeground(Color.black);

		m_elabel2 = new JLabel("Right click for menu.");
		m_elabel2.setForeground(Color.black);

		m_rlabel = new JLabel("Relation Classes");
		m_rlabel.setForeground(Color.red);

		m_rlabel2 = new JLabel("Reversed Relation Classes");
		m_rlabel2.setForeground(Color.red);

		m_clabel.setForeground(Color.red);

		m_footer1 = new JLabel("Checkboxes select visible entities/relations.");
		m_footer1.setForeground(Color.black);

		m_footer2 = new JLabel("Right click for menu.");
		m_footer2.setForeground(Color.black);

		setComponentsTitleFont(titleFont);
		setComponentsTextFont(textFont);
		tabbedPane.addChangeListener(this);
		addMouseListener(this);
	}
	
	public Font getTextFont()
	{
		return m_textFont;
	}

	public static void setTextFont(Font font)
	{
		m_textFont = font;
	}

	public static void setTitleFont(Font font)
	{
		m_titleFont = font;
	}
		
	// These are overloaded methods to change the shown flag
	
	protected boolean getFlag(EntityClass ec)
	{
		return ec.isShown();
	}

	protected int setFlag(EntityClass ec, boolean value, boolean applyToSubclasses)
	{
		return ec.setShown(value, applyToSubclasses);
	}
	
	protected boolean getFlag(RelationClass rc, int direction)
	{
		return (rc.isShown(direction));
	}

	protected int setFlag(RelationClass rc, int direction, boolean value, boolean applyToSubclasses)
	{
		return rc.setShown(direction, value, applyToSubclasses);
	}
		
	protected String getFlagName()
	{
		return "Shown";
	}

/*
	protected boolean can_reverse()
	{
		return false;
	}
*/
	
	protected void customOptions(JPopupMenu popupMenu)
	{
		Do.createClassMenuItem(popupMenu, m_ls);
	}
}



