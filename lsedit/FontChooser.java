package lsedit;

import java.lang.Integer;

// import java.util.Vector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/*
 * This applet displays a String with the user's selected 
 * fontname, style and size attributes.
*/

public class FontChooser extends JDialog implements ItemListener, ActionListener {
 
	class FontPanel extends JPanel {

		public FontPanel()
		{ 
		}

		public Dimension getPreferredSize()
		{
			return (new Dimension(m_fontPanelWidth, m_fontPanelHeight));
		}

		public Dimension getMinimumSize()
		{
			return(getPreferredSize());
		}

		public Dimension getMaximumSize()
		{
			return(getPreferredSize());
		}

		public void paintComponent (Graphics g) 
		{
			super.paintComponent(g);

			g.setColor(Color.darkGray);
			g.setFont(m_font);

			String change = "Set " + m_targetname + " font " + m_fontname + " ";
			int	   width;

			if (m_style < 4) {
				change += m_stylenames[m_style+1];
			} else {
				change += "style " + m_style;
			}
			change += " size " + m_size;
			width   = FontChooser.this.getWidth();

			Util.drawStringWrapped(g, change, m_fontPanelXMargin, m_fontPanelYMargin, width - 2 * m_fontPanelXMargin, m_fontPanelHeight - m_fontPanelYMargin, true, false, false);
	}	}
	  
	private final static int	  m_fontPanelWidth   = 150;
	private final static int	  m_fontPanelHeight  = 100;
	private final static int	  m_fontPanelXMargin =   5;
	private final static int	  m_fontPanelYMargin =  10;

	private final static String[] m_targetnames = new String[]
								{
						/*  0 */	"ALL FONTS",
						/*  1 */	"CLOSED LABEL",
						/*  2 */	"OPEN LABEL",
						/*  3 */	"OPEN CLASS LABEL",
						/*  4 */	"CLIENT/SUPPIER LABEL",
						/*  5 */	"CARDINALS",
						/*  6 */	"RESULTS TITLE",
						/*  7 */	"RESULTS TEXT",
						/*  8 */	"LEGEND TITLE",
						/*  9 */	"LEGEND TEXT",
						/* 10 */	"QUERY TITLE",
						/* 11 */	"QUERY TEXT",
						/* 12 */	"TOC",
						/* 13 */	"UNDO",
						/* 14 */	"HISTORY",
						/* 15 */	"MAP",
						/* 16 */	"TITLE",
						/* 17 */	"TEXTBOX",
						/* 18 */	"FEEDBACK",
						/* 19 */	"MENU",
						/* 20 */	"DIALOG",
						/* 21 */	"CLIPBOARD",
						/* 22 */	"ATTR BOX TEXT",
						/* 23 */	"RELATION LABEL",
						/* 24 */	"VIEWS",
						/* 25 */	"TABS"
								};

	private final static String[] m_targetDescription = new String[]
								{
									"***ALL***",
									"closed label",
									"small label",
									"open class label",
									"client/supplier",
									"cardinal",
									"results title",
									"results text",
									"legend title",
									"legend text",
									"query title",
									"query text",
									"TOC",
									"undo",
									"history",
									"map",
									"title",
									"text box",
									"feedback",
									"menu",
									"dialog",
									"clipboard",
									"attribute text",
									"relation label",
									"views",
									"tabs"
								};
		
	private final static String[] m_stylenames = new String[] 
								{
									"DEFAULT",
									"PLAIN",
									"BOLD",
									"ITALIC",
									"BOLD & ITALIC"
								};

	private final static String[] m_sizenames = new String[]
								{	
									"DEFAULT", "4", "5", "6", "7", "8", "9", "10", "12", "14", "16", "18", "20", "22", "24", "26", "28", "30"
								};

	private static GraphicsEnvironment m_gEnv = null;
	private static String m_envfonts[]        = null;

	private	int			m_target;
	private String		m_targetname;
	private Font		m_font;
	private String		m_fontname;
	private int			m_style;
    private	int			m_size;

	private boolean		m_name_default;
	private boolean		m_style_default;
	private boolean		m_size_default;

	private	FontPanel	m_fontPanel;
	private	JComboBox	m_targets, m_fonts, m_sizes, m_styles;
	
	public final static String[]	g_optionchoices = new String[]
								{
									"Reset",		// Fixed position
									"Default",		// Fixed position
									"Landscape",
									"Current",
								};
						
	protected JComboBox	m_optionchoices = new JComboBox(g_optionchoices);
	protected Option	m_current       = new Option("Fontchooser");

	static protected final int BUTTON_OK     = 0;
	static protected final int BUTTON_CANCEL = 1;
	static protected final int BUTTON_LOAD   = 2;
	static protected final int BUTTON_SAVE   = 3;
	static protected final int BUTTON_CLEAR  = 4;

	protected final static String[] m_button_titles =
							{
								"Ok",
								"Cancel",
								"Load",
								"Save",
								"Clear"
							};

	protected final static String[] m_button_tips =
							{
								"Set fonts as changed",
								"Change nothing",
								"Load fonts from specified option",
								"Set specified options to fonts",
								"Clear the font cache" 
							};

	protected JButton[] m_buttons;
						
	protected void newFont(Font font)
	{
		m_font      = font;
		m_fontname  = font.getFamily();
		m_style     = font.getStyle();
		m_size      = font.getSize();
	}

	protected void newFont()
	{
		newFont(FontCache.get(m_fontname, m_style, m_size));
	}

	protected Option getTarget()
	{
		switch (m_optionchoices.getSelectedIndex()) {
		case 1:
			return Options.getDefaultOptions();
		case 2:
			return Options.getLandscapeOptions();
		case 3:
			return Options.getDiagramOptions();
		}
		return(null);
	}
	
	protected void setTarget(int index)
	{
		Option	option;
		String	s;
		Font	font;
		int		i;

		option = getTarget();
		if (option == null) {
			font = Option.getDefaultFont(index);
		} else {
			font = option.getTargetFont(index);
		}
		if (font == null) {
			return;
		}
	
		m_target     = index;
		m_targetname = m_targetDescription[index];
		newFont(font);

		m_targets.setSelectedIndex(index);

		m_name_default = true;
		for (i = 1; i < m_envfonts.length; i++ ) {
			if (m_fontname.equalsIgnoreCase(m_envfonts[i])) {
				m_fonts.setSelectedIndex(i);
				m_name_default = false;
				break;
		}	}
        
		if (m_name_default) {
			m_fonts.setSelectedIndex(0);
		}

		if (m_style < 4) {
			m_style_default = true;
			m_styles.setSelectedIndex(0);
		} else {
			m_style_default = false;	
			m_styles.setSelectedIndex(m_style+1);
		}

		s = "" + m_size;

		m_size_default = true;
		for (i = 1; i < m_sizenames.length; i++ ) {
			if (s.equals(m_sizenames[i])) {
				m_sizes.setSelectedIndex(i);
				m_size_default = false;
				break;
		}	}

		if (m_size_default) {
			m_sizes.setSelectedIndex(0);
	}	}

	public FontChooser(JFrame frame)
	{
		super(frame, "Select font for target", true /* modal */);

		Container	contentPane;
		JLabel		label;
		Font		font, bold;
		JButton		button;
		String		tip;
		int			i;

		contentPane = getContentPane();
		font        = FontCache.getDialogFont();
		bold        = font.deriveFont(Font.BOLD);

        contentPane.setLayout( new BorderLayout() );

        JPanel topPanel          = new JPanel();
		JPanel targetPanel       = new JPanel();
        JPanel fontPanel         = new JPanel();
        JPanel sizePanel         = new JPanel();
        JPanel stylePanel        = new JPanel();
        JPanel sizeAndStylePanel = new JPanel();

        topPanel.setLayout( new BorderLayout() );

		targetPanel.setLayout( new GridLayout( 2, 1) );
        fontPanel.setLayout( new GridLayout( 2, 1 ) );
        sizePanel.setLayout( new GridLayout( 2, 1 ) );
        stylePanel.setLayout( new GridLayout( 2, 1 ) );
        
		sizeAndStylePanel.setLayout( new BorderLayout() );
        sizeAndStylePanel.add( BorderLayout.CENTER, stylePanel );
        sizeAndStylePanel.add( BorderLayout.EAST, sizePanel );

        topPanel.add( BorderLayout.WEST, targetPanel );
        topPanel.add( BorderLayout.CENTER, fontPanel );
        topPanel.add( BorderLayout.EAST, sizeAndStylePanel );

        contentPane.add( BorderLayout.NORTH, topPanel );

		{
			label = new JLabel();
			label.setText("Target");
			label.setFont(bold);
			label.setHorizontalAlignment(JLabel.CENTER);
			targetPanel.add(label);
		}

		{
			label = new JLabel();
			label.setText("Fonts");
			label.setFont(bold);
			label.setHorizontalAlignment(JLabel.CENTER);
			fontPanel.add(label);
		}

		{
			label = new JLabel();
			label.setText("Styles");
			label.setFont(bold);
			label.setHorizontalAlignment(JLabel.CENTER);
			stylePanel.add(label);
		}

		{
			label = new JLabel();
			label.setText("Sizes");
			label.setFont(bold);
			label.setHorizontalAlignment(JLabel.CENTER);
			sizePanel.add(label);
		}

		{
			m_targets = new JComboBox(m_targetnames);
			m_targetname = "target";

			m_targets.setFont(bold);
			m_targets.setSelectedIndex(0);
			m_targets.setMaximumRowCount( 9 );
			m_targets.addItemListener(this);
			targetPanel.add(m_targets);
		}

		{

			if (m_gEnv == null) {
				m_gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
			}
			if (m_envfonts == null) {
				String	augmented[];

				m_envfonts = m_gEnv.getAvailableFontFamilyNames();
				i = m_envfonts.length;
				augmented = new String[i+1];
				for (; i > 0; --i) {
					augmented[i] = m_envfonts[i-1];
				}
				augmented[0] = "DEFAULT";
				m_envfonts   = augmented;
			}

			m_fonts = new JComboBox(m_envfonts);
			m_fonts.setFont(bold);
			m_fonts.setMaximumRowCount( 9 );
			m_fonts.addItemListener(this);
     		fontPanel.add(m_fonts);
		}

		{
			m_styles = new JComboBox( m_stylenames );
			m_styles.setFont(bold);
			m_styles.setMaximumRowCount( 9 );
			m_styles.addItemListener(this);
			stylePanel.add(m_styles);
		}

		{
			m_sizes = new JComboBox(m_sizenames);
			m_sizes.setFont(bold);
			m_sizes.setMaximumRowCount( 9 );
			m_sizes.addItemListener(this);
			sizePanel.add(m_sizes);
		}

        {
			m_fontPanel = new FontPanel();
			m_fontPanel.setBackground(Color.white);
			contentPane.add( BorderLayout.CENTER, m_fontPanel);
		}

		// Create an Okay button in a Panel; add the Panel to the window

		// Use a FlowLayout to center the button and give it margins.

		m_current.setFontsTo(Options.getDiagramOptions());

		{
			JPanel bottomPanel = new JPanel();

			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));

			m_optionchoices.setFont(bold);
			m_optionchoices.setSelectedIndex(3);
			m_optionchoices.addActionListener(this);

			bottomPanel.add(m_optionchoices);

			m_buttons = new JButton[m_button_titles.length];
			for (i = 0; i < m_button_titles.length; ++i) {
				m_buttons[i] = button = new JButton(m_button_titles[i]);
				button.setFont(bold);
				tip = m_button_tips[i];
				if (tip != null) {
					button.setToolTipText(tip);
				}
				button.addActionListener(this);
				bottomPanel.add(button);
			}
			
			contentPane.add( BorderLayout.SOUTH, bottomPanel);
		}

		setLocation(20, 20);
		m_name_default  = false;
		m_style_default = false;
		m_size_default  = false;
		setTarget(0);
		
		// Resize the window to the preferred size of its components
		pack();
		setVisible(true);
    }

	public Font getFontChoice()
	{
		return (m_font);
	}
	
/*
 * ItemListener interface

 * Detects a state change in any of the Lists.  Resets the variable corresponding
 * to the selected item in a particular List.  Invokes changeFont with the currently
 * selected fontname, style and size attributes.
*/
	protected void loadValues()
	{
		setTarget(m_targets.getSelectedIndex());
	}
	
    public void itemStateChanged(ItemEvent e) 
	{
        if ( e.getStateChange() == ItemEvent.SELECTED ) {

			Object	list  = e.getSource();
			int		index;

			if ( list == m_targets) {
				loadValues();
			} else {
				if ( list == m_fonts ) {
					index = m_fonts.getSelectedIndex();
					if (index == 0) {
						m_name_default = true;
						m_fontname     = Option.getDefaultFontName(m_target);
					} else {
						m_name_default = false;
						m_fontname     = (String) m_fonts.getSelectedItem();
					}
				} else if ( list == m_styles ) {
					index = m_styles.getSelectedIndex();
					if (index == 0) {
						m_style_default = true;
						m_style         = Option.getDefaultFontStyle(m_target);
					} else {
						m_style_default = false;
						m_style         = --index;
					}
				} else {
					index = m_sizes.getSelectedIndex();
					if (index == 0) {
						m_size_default  = true;
						m_size          = Option.getDefaultFontSize(m_target);
					} else { 
						String  sizename = (String) m_sizes.getSelectedItem();
						Integer newSize  = new Integer(sizename);
						m_size           = newSize.intValue();
						m_size_default   = false;
				}	}
				newFont();
			}
			m_fontPanel.repaint();
		}
    }

/* ActionListener interface */

	public void actionPerformed(ActionEvent e)
	{
		Object	source = e.getSource();
		int		state  = -1;
		int		i;
		Option	target, current;

		if (source == m_optionchoices) {
			m_buttons[BUTTON_SAVE].setEnabled(m_optionchoices.getSelectedIndex() != 0);
			loadValues();
			return;
		}			

		for (i = 0; i < m_button_titles.length; ++i) {
			if (source == m_buttons[i])	{
				state = i;
				break;
		}	}
		
		current = m_current;
		switch (state) {
		case BUTTON_CANCEL:
			break;
		case BUTTON_LOAD:
			current.setFontsTo(getTarget());
			return;
		case BUTTON_SAVE:
			target = getTarget();
			if (target != null) {
				target.setFontsTo(current);
			}
			return;
		case BUTTON_OK:
			current.setTargetFont(m_target, m_font, m_name_default, m_style_default, m_size_default);
			target = Options.getDiagramOptions();
			target.setFontsTo(current);
			break;
		case BUTTON_CLEAR:
			int	cnt = FontCache.size();
			FontCache.clear();
			JOptionPane.showMessageDialog(this, cnt + " fonts have been removed from the cache");
		default:
			return;
		}
		setVisible(false);
	}
	
	public static void create(LandscapeEditorCore ls)
	{
		Option option     = Options.getDiagramOptions();
		Option oldOptions = new Option("Old fontchooser");

		oldOptions.setFontsTo(option);

		FontChooser fontChooser = new FontChooser(ls.getFrame());
		
		fontChooser.dispose();
		
		ls.fontsChanged(oldOptions, option);
	}
}



