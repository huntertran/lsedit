package lsedit;

import java.util.Vector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


class SetNewGridDialog extends JDialog implements ChangeListener, ActionListener
{
	class GridImage extends JComponent
	{
		public GridImage()
		{
			super();
			setPreferredSize(new Dimension(400, 100));
		}

		public void paintComponent(Graphics g)
		{
			int	grid = m_gridSize;
			int	w    = getWidth();
			int h    = getHeight();
			int	i;

			g.drawRect(0,0, w-1, h-1);

			if (grid > 1) {
				Color color   = m_gridColor;
				Color inverse = ColorCache.getInverse(color.getRGB());

				g.setColor(inverse);
				g.fillRect(1,1,w-2,w-2);

				g.setColor(color);
				for (i = grid; i < h; i += grid) {
					g.drawLine(0,i,w-1,i);
				}
				for (i = grid; i < w; i += grid) {
					g.drawLine(i,0,i, h-1);
		}	}	}
	}

	JFrame		m_frame;
	JSlider		m_gridSlider;
	GridImage	m_gridImage;
	JLabel		m_feedback;
	JButton		m_color;
	int			m_gridSize;
	Color		m_gridColor;

	public SetNewGridDialog(JFrame frame, int gridSize, Color gridColor)
	{
		super(frame, "Set grid size", true);

		Container	contentPane = getContentPane();
		Font		font, bold;

		m_frame      = frame;
		m_gridSize   = gridSize;
		m_gridColor  = gridColor;
		font         = FontCache.getDialogFont();
		bold         = font.deriveFont(Font.BOLD);

		JLabel sliderLabel = new JLabel("Grid size when moving entities", JLabel.CENTER);
		sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		sliderLabel.setFont(bold);

		JLabel feedback  = new JLabel("Current grid size " + gridSize, JLabel.CENTER);
		feedback.setAlignmentX(Component.CENTER_ALIGNMENT);
		feedback.setFont(font);
		feedback.setForeground(Color.BLUE);
		m_feedback = feedback;

		m_gridImage = new GridImage();

		JSlider gridSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, m_gridSize);

		m_gridSlider = gridSlider;

		gridSlider.addChangeListener(this);

		gridSlider.setMajorTickSpacing(10);
		gridSlider.setMinorTickSpacing(2);
		gridSlider.setPaintTicks(true);
		gridSlider.setPaintLabels(true);
		gridSlider.setFont(font);
		gridSlider.setPreferredSize(new Dimension(400, 50));

		gridSlider.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));

		JPanel	panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(feedback, BorderLayout.NORTH);
		panel.add(m_gridImage,BorderLayout.CENTER);

		m_color = new JButton("Colour");
		m_color.addActionListener(this);
		panel.add(m_color, BorderLayout.SOUTH);

		contentPane.add(sliderLabel, BorderLayout.NORTH);
		contentPane.add(gridSlider,  BorderLayout.CENTER);
		contentPane.add(panel,       BorderLayout.SOUTH);

		if (frame != null) {
			setLocation(frame.getX()+200, frame.getY()+300);
		}
		pack();
		setVisible(true);
	}
	
	public int getGridSize()
	{
		return m_gridSize;
	}
	
	public Color getGridColor()
	{
		return m_gridColor;
	}

	// ActionListener interface

	public void actionPerformed(ActionEvent ev)
	{
		Object source = ev.getSource();

		if (source == m_color) {
			ColorChooser colorChooser = new ColorChooser(m_frame, "Pick a grid color", m_gridColor, true /* include alpha */, false /* Don't allow null */);
			m_gridColor = colorChooser.getColor();
			colorChooser.dispose();
			m_gridImage.repaint();
	}	}

	public void stateChanged(ChangeEvent ev)
	{
		Object source = ev.getSource();

		if (source == m_gridSlider) {
			int value = (int) m_gridSlider.getValue();

			if (value < 1) {
				value = 1;
			}
			m_gridSize = value;
			m_gridImage.repaint();
	}	}
}
	
public class OptionsDialog extends JDialog implements ActionListener { 

	protected LandscapeEditorCore	m_ls;
	protected Font					m_font;
	protected Font					m_bold;

	// JPanel1 stuff
	
	private JCheckBox	m_show_toolbar		 = new JCheckBox("");
	private JCheckBox	m_show_desc          = new JCheckBox("");
	private JCheckBox	m_show_feedback      = new JCheckBox("");
	private JCheckBox	m_left_tabbox        = new JCheckBox("");
	private JCheckBox	m_tabs_scroll        = new JCheckBox("");
	private JCheckBox	m_fix_scrollbars     = new JCheckBox("");
	private JCheckBox	m_sort_toc           = new JCheckBox("");
	private JCheckBox	m_top_clients        = new JCheckBox("");
	private JCheckBox	m_show_clients       = new JCheckBox("");
	private JCheckBox	m_show_suppliers     = new JCheckBox("");
	private JCheckBox	m_use_compaction     = new JCheckBox("");
	private JCheckBox	m_visible_edges      = new JCheckBox("");
	private JCheckBox	m_visible_entities   = new JCheckBox("");
	private JCheckBox	m_lift_edges         = new JCheckBox("");
	private JCheckBox	m_show_dst_cardinals = new JCheckBox("");
	private JCheckBox	m_show_src_cardinals = new JCheckBox("");
	private JCheckBox	m_focus_ancestor     = new JCheckBox("");
	private JCheckBox	m_hide_empty         = new JCheckBox("");
	private JCheckBox	m_member_counts      = new JCheckBox("");
	private JCheckBox	m_inheritance        = new JCheckBox("");
	private JCheckBox	m_legend_labels      = new JCheckBox("");

	// JPanel2 stuff
	
	private	JTextField	m_line_width         = new JTextField(8);
	private	JTextField	m_arrow_length       = new JTextField(8);
	private	JTextField	m_arrow_arc          = new JTextField(8);
	private	JTextField	m_pixels_3d          = new JTextField(8);
	private JTextField	m_shadow_size        = new JTextField(8);
	private JTextField	m_label_angle        = new JTextField(8);
	private JTextField	m_hover_scale        = new JTextField(8);
	private JTextField	m_zoom_x             = new JTextField(8);
	private	JTextField	m_zoom_y             = new JTextField(8);

	private	JCheckBox	m_fill_arrowhead           = new JCheckBox("");
	private	JCheckBox	m_center_arrowhead         = new JCheckBox("");
	private	JCheckBox	m_permanently_weight       = new JCheckBox("");
	private	JCheckBox	m_blackwhite_3d            = new JCheckBox("");
	private	JCheckBox	m_show_edge_labels         = new JCheckBox("");
	private JCheckBox	m_rotate_edge_labels       = new JCheckBox("");
	private	JCheckBox	m_show_edge_tooltip        = new JCheckBox("");
	private	JCheckBox	m_variable_arrow_color     = new JCheckBox("");
	private	JCheckBox	m_invert_edge_label_back   = new JCheckBox("");
	private	JCheckBox	m_label_invert_fore        = new JCheckBox("");
	private	JCheckBox	m_label_invert_back        = new JCheckBox("");
	private	JCheckBox	m_entity_labels_blackwhite = new JCheckBox("");	

	// JPanel 3 options

	public final static String[]	g_loadchoices = new String[]
	{
		"Load",				// Fixed position
		"Ignore",			// Fixed position
		"Prompt",
	};
	private JComboBox		m_loadchoices = new JComboBox(g_loadchoices);

	public final static String[]	g_edgechoices = new String[]
	{
		"Inflection",		// Fixed position
		"Top/Bottom",		// Fixed position
		"Direct",
		"Direct+Side"
	};
	
	private JComboBox				m_edgechoices = new JComboBox(g_edgechoices);

	public final static String[]	g_iconchoices = new String[]
								{
									"No icons",			// Fixed position
									"Plain icons",		// Fixed position
									"Labels centered",
									"Labels bottom",
									"Labels top",
									"Empty cache"
								};	
	
	public final static String[]	g_showchoices = new String[]
								{
									"Hide",
									"Show",
									"Image"
								};

	private JComboBox	m_iconchoices        = new JComboBox(g_iconchoices);
	private JComboBox	m_elision_icon       = new JComboBox();
	private JTextField	m_iconPath           = new JTextField(8);
	private JCheckBox	m_icon_fixed_shape   = new JCheckBox("");

	private JCheckBox	m_show_grid          = new JCheckBox("");
	private JCheckBox	m_snap_to_grid       = new JCheckBox("");
	private JButton		m_setGridSize		 = new JButton();
	private JTextField	m_chase_edges        = new JTextField(8);
	private JCheckBox	m_chase_hide         = new JCheckBox("");
	private JCheckBox	m_visible_spans      = new JCheckBox("");

	private JComboBox	m_show_span_type     = new JComboBox(g_showchoices);
	private JComboBox	m_show_class_type    = new JComboBox(g_showchoices);
	private JComboBox	m_show_navnext		 = new JComboBox(g_showchoices);
	private JComboBox	m_show_navprev		 = new JComboBox(g_showchoices);

	private JCheckBox	m_trace_lifted       = new JCheckBox("");
	private JCheckBox	m_group_query        = new JCheckBox("");
	private JCheckBox	m_query_persists     = new JCheckBox("");

	// General stuff	

	private Container			m_contentPane;
	private JComponent			m_centerThing;
	private JScrollPane			m_scrollPane1;
	private JScrollPane			m_scrollPane2;
	private JScrollPane			m_scrollPane3;
	private boolean				m_use_tabs = false;

	// Other stuff

	public final static String[]	g_optionchoices = new String[]
								{
									"Reset",		// Fixed position
									"Default",		// Fixed position
									"Landscape",
									"Current",
								};
						
	protected JComboBox	m_optionchoices = new JComboBox(g_optionchoices);
		
	protected Option	m_current       = new Option("Current");
	
	static protected final int BUTTON_OK      = 0;
	static protected final int BUTTON_CANCEL  = 1;
	static protected final int BUTTON_LOAD    = 2;
	static protected final int BUTTON_SAVE    = 3;
	static protected final int BUTTON_TAB     = 4;
	static protected final int BUTTON_HELP    = 5;

	protected final static String[] m_button_titles =
							{
								"Ok",
								"Cancel",
								"Load",
								"Save",
								"Tab",
								"Help"
							};

	protected final static String[] m_button_tips =
							{
								"Set values shown as current options",
								"Change nothing",
								"Load values show with specified options",
								"Set specified options to values shown",
								"Switch to/from tabbed view",
								"Bring up browser describing options"
							};

	protected JLabel	m_message;
	protected JButton[]	m_buttons = new JButton[m_button_titles.length];
	
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
	
	protected String getTargetName()
	{
		int choice = m_optionchoices.getSelectedIndex();
		
		if (choice >= 0 && choice < g_optionchoices.length) {
			return(g_optionchoices[choice]);
		}
		return "?";
	}
	
	protected void loadValues1()
	{
		Option			option     = m_current;
	
		m_show_toolbar.setSelected(option.isShowToolbar());
		m_show_desc.setSelected(option.isShowDesc());
		m_show_feedback.setSelected(option.isShowFeedback());
		m_left_tabbox.setSelected(option.isLeftTabbox());
		m_tabs_scroll.setSelected(option.isTabsScroll());
		m_fix_scrollbars.setSelected(option.isFixScrollBars());
		m_sort_toc.setSelected(option.isSortTOC());
		m_top_clients.setSelected(option.isTopClients());
		m_show_clients.setSelected(option.isShowClients());
		m_show_suppliers.setSelected(option.isShowSuppliers());
		m_use_compaction.setSelected(option.isUseCompaction());
		m_visible_edges.setSelected(option.isVisibleEdges());
		m_visible_entities.setSelected(option.isVisibleEntities());
		m_lift_edges.setSelected(option.isLiftEdges());
		m_show_dst_cardinals.setSelected(option.isShowDstCardinals());
		m_show_src_cardinals.setSelected(option.isShowSrcCardinals());
		m_focus_ancestor.setSelected(option.isFocusAncestor());
		m_hide_empty.setSelected(option.isHideEmpty());
		m_member_counts.setSelected(option.isMemberCounts());
		m_inheritance.setSelected(option.isShowInheritance());
		m_legend_labels.setSelected(option.isLegendLabelBlack());
	}
	
	protected void saveValues1()
	{
		Option			option     = m_current;
	
		option.setShowToolbar(m_show_toolbar.isSelected());
		option.setShowDesc(m_show_desc.isSelected());
		option.setShowFeedback(m_show_feedback.isSelected());
		option.setLeftTabbox(m_left_tabbox.isSelected());
		option.setTabsScroll(m_tabs_scroll.isSelected());
		option.setFixScrollBars(m_fix_scrollbars.isSelected());
		option.setSortTOC(m_sort_toc.isSelected());
		option.setTopClients(m_top_clients.isSelected());
		option.setShowClients(m_show_clients.isSelected());
		option.setShowSuppliers(m_show_suppliers.isSelected());
		option.setUseCompaction(m_use_compaction.isSelected());
		option.setVisibleEdges(m_visible_edges.isSelected());
		option.setVisibleEntities(m_visible_entities.isSelected());
		option.setLiftEdges(m_lift_edges.isSelected());
		option.setShowDstCardinals(m_show_dst_cardinals.isSelected());
		option.setShowSrcCardinals(m_show_src_cardinals.isSelected());
		option.setFocusAncestor(m_focus_ancestor.isSelected());
		option.setHideEmpty(m_hide_empty.isSelected());
		option.setMemberCounts(m_member_counts.isSelected());
		option.setShowInheritance(m_inheritance.isSelected());
		option.setLegendLabelBlack(m_legend_labels.isSelected());
	}
	
	protected void loadValues2()
	{
		Option			option     = m_current;
	
		m_line_width.setText("" + option.getLineWidth());
		m_arrow_length.setText("" + option.getArrowLength());
		m_arrow_arc.setText("" + option.getArrowArc());
		m_pixels_3d.setText("" + option.getPixels3D());
		m_shadow_size.setText("" + option.getShadowSize());
		m_label_angle.setText("" + option.getLabelAngle());
		m_hover_scale.setText("" + option.getHoverScale());
		m_zoom_x.setText("" + option.getZoomX());
		m_zoom_y.setText("" + option.getZoomY());

		m_fill_arrowhead.setSelected(option.isFillArrowhead());
		m_center_arrowhead.setSelected(option.isCenterArrowhead());
		m_permanently_weight.setSelected(option.isPermanentlyWeight());
		m_blackwhite_3d.setSelected(option.isBlackWhite3D());
		m_show_edge_labels.setSelected(option.isShowEdgeLabels());
		m_rotate_edge_labels.setSelected(option.isRotateEdgeLabels());
		m_show_edge_tooltip.setSelected(option.isShowEdgeTooltip());
		m_variable_arrow_color.setSelected(option.isVariableArrowColor());
		m_invert_edge_label_back.setSelected(option.isInvertEdgeLabelBackground());
		m_label_invert_fore.setSelected(option.isLabelInvertForeground());
		m_label_invert_back.setSelected(option.isLabelInvertBackground());
		m_entity_labels_blackwhite.setSelected(option.isLabelBlackWhite());	
	}
	
	protected String saveValues2()
	{
		Option			option     = m_current;
		String			ret        = null;
		String			ret1;
	
		ret1 = option.setLineWidth(m_line_width.getText());
		if (ret1 != null) {
			ret = ret1;
		}
		ret1 = option.setArrowLength(m_arrow_length.getText());
		if (ret1 != null) {
			ret = ret1;
		}
		ret1 = option.setArrowArc(m_arrow_arc.getText());
		if (ret1 != null) {
			ret = ret1;
		}
		ret1 = option.setPixels3D(m_pixels_3d.getText());
		if (ret1 != null) {
			ret = ret1;
		}
		ret1 = option.setShadowSize(m_shadow_size.getText());
		if (ret1 != null) {
			ret = ret1;
		}
		ret1 = option.setLabelAngle(m_label_angle.getText());
		if (ret1 != null) {
			ret = ret1;
		}
		ret1 = option.setHoverScale(m_hover_scale.getText());
		if (ret1 != null) {
			ret = ret1;
		}
		ret1 = option.setZoomX(m_zoom_x.getText());
		if (ret1 != null) {
			ret = ret1;
		}
		ret1 = option.setZoomY(m_zoom_y.getText());
		if (ret1 != null) {
			ret = ret1;
		}
			
		option.setFillArrowhead(m_fill_arrowhead.isSelected());
		option.setCenterArrowhead(m_center_arrowhead.isSelected());
		option.setPermanentlyWeight(m_permanently_weight.isSelected());
		option.setBlackWhite3D(m_blackwhite_3d.isSelected());
		option.setShowEdgeLabels(m_show_edge_labels.isSelected());
		option.setRotateEdgeLabels(m_rotate_edge_labels.isSelected());
		option.setShowEdgeTooltip(m_show_edge_tooltip.isSelected());
		option.setVariableArrowColor(m_variable_arrow_color.isSelected());
		option.setInvertEdgeLabelBackground(m_invert_edge_label_back.isSelected());
		option.setLabelInvertForeground(m_label_invert_fore.isSelected());
		option.setLabelInvertBackground(m_label_invert_back.isSelected());
		option.setLabelBlackWhite(m_entity_labels_blackwhite.isSelected());			
		
		return ret;
	}
	
	protected void loadValues3()
	{
		JComboBox		elision_icon = m_elision_icon;
		Option			option       = m_current;
		int				elision      = option.getElisionIcon();
		RelationClass	rc;
		int				i;
	
		m_loadchoices.setSelectedIndex(option.getLoadMode());
		
		m_show_grid.setSelected(option.isShowGrid());
		m_snap_to_grid.setSelected(option.isSnapToGrid());
			
		m_setGridSize.setText("" + option.getGridSize());
		m_setGridSize.setForeground(option.getGridColor());
		
		m_iconPath.setText(option.getIconPath());
		
		m_iconchoices.setSelectedIndex(option.getIconRule());
		
		// Can't assume all relation classes still exist so can't assume all Nid's present
		elision_icon.setSelectedIndex(0);
		if (elision >= 0) {
			for (i = elision_icon.getItemCount(); --i > 0; ) {
				rc = (RelationClass) elision_icon.getItemAt(i);
				if (rc.getNid() == elision) {
					elision_icon.setSelectedIndex(i);
					break;
		}	}	}

		m_icon_fixed_shape.setSelected(option.isIconFixedShape());
		
		m_edgechoices.setSelectedIndex(option.getEdgeMode());
		m_chase_edges.setText("" + option.getChaseEdges());
		m_chase_hide.setSelected(option.isChaseHide());
		m_visible_spans.setSelected(option.isVisibleSpans());

		m_show_span_type.setSelectedIndex(option.getShowSpanType());
		m_show_class_type.setSelectedIndex(option.getShowClassType());
		m_show_navnext.setSelectedIndex(option.getShowNavNext());
		m_show_navprev.setSelectedIndex(option.getShowNavPrev());

		m_trace_lifted.setSelected(option.isTraceLifted());
		m_group_query.setSelected(option.isGroupQuery());
		m_query_persists.setSelected(option.isQueryPersists());
	}
	
	protected String saveValues3()
	{
		Option			option       = m_current;
		JComboBox		elision_icon = m_elision_icon;
		RelationClass	rc;
		int				index;
		String			msg;
	
		option.setLoadMode(m_loadchoices.getSelectedIndex());
		
		option.setShowGrid(m_show_grid.isSelected());
		option.setSnapToGrid(m_snap_to_grid.isSelected());	

		option.setIconPath(m_iconPath.getText());	
		index = m_iconchoices.getSelectedIndex();
		if (index == Option.ICON_RULE_EMPTY) {
			IconCache.clear();
			index = Option.ICON_RULE_NONE;
		} 
		option.setIconRule(index);
		
		// Can't assume all relation classes still exist so can't assume all Nid's present
		index = elision_icon.getSelectedIndex();
		if (index <= 0) {
			index = -1;
		} else {
			rc = (RelationClass) elision_icon.getItemAt(index);
			index = rc.getNid();
		}
		option.setElisionIcon(index);	
		
		option.setIconFixedShape(m_icon_fixed_shape.isSelected());
	
		option.setChaseHide(m_chase_hide.isSelected());
		option.setVisibleSpans(m_visible_spans.isSelected());
		option.setShowSpanType(m_show_span_type.getSelectedIndex());
		option.setShowClassType(m_show_class_type.getSelectedIndex());
		option.setShowNavNext(m_show_navnext.getSelectedIndex());
		option.setShowNavPrev(m_show_navprev.getSelectedIndex());

		option.setTraceLifted(m_trace_lifted.isSelected());
		option.setGroupQuery(m_group_query.isSelected());
		option.setQueryPersists(m_query_persists.isSelected());
		
		option.setEdgeMode(m_edgechoices.getSelectedIndex());
		
		msg = option.setChaseEdges(m_chase_edges.getText());
		return(msg);
	}
	
	protected void addCenterThing()
	{
		if (m_use_tabs) {
			JTabbedPane tabbedPane = new JTabbedPane();
				
			tabbedPane.add("Main",          m_scrollPane1);
			tabbedPane.add("Visualisation", m_scrollPane2);	
			tabbedPane.add("Diagram",       m_scrollPane3);
			m_centerThing = tabbedPane;
		} else {
			JPanel	topPanel = new JPanel();
			topPanel.setLayout( new BorderLayout() );
 			topPanel.add(BorderLayout.WEST, m_scrollPane1);
			topPanel.add(BorderLayout.EAST, m_scrollPane3);
			topPanel.add(BorderLayout.CENTER, m_scrollPane2);
	        m_centerThing = topPanel;
		}
		m_contentPane.add( BorderLayout.CENTER, m_centerThing );
	}

	protected void addPanel(JPanel panel, String text)
	{
		JLabel label = new JLabel(text, JLabel.RIGHT);
		label.setFont(m_bold);
		panel.add(label);
	}
	
	protected void addUnits(JPanel panel, String text)
	{
		JLabel label = new JLabel(text, JLabel.LEFT);
		label.setFont(m_bold);
		panel.add(label);
	}
	
	protected void addPanel(JPanel panel, JComboBox combobox)
	{
		combobox.setFont(m_font);
		panel.add(combobox);
	}
	
	protected void addPanel(JPanel panel, JCheckBox checkbox)
	{
		checkbox.setFont(m_font);
		panel.add(checkbox);
	}
	
	protected void addPanel(JPanel panel, JTextField textfield)
	{
		textfield.setFont(m_font);
		panel.add(textfield);
	}
	
	protected OptionsDialog(LandscapeEditorCore ls) //Constructor
	{
		super(ls.getFrame(), "Modify LSEdit options", true); //false if non-modal

		Diagram		diagram = ls.getDiagram();
		Container	contentPane;
		Font		font, bold;

		m_ls         = ls;
		m_font       = font = FontCache.getDialogFont();
		m_bold       = bold = m_font.deriveFont(Font.BOLD);

		m_contentPane = contentPane = getContentPane();
        contentPane.setLayout( new BorderLayout() );

		setForeground(ColorCache.get(0,0,0));
		setBackground(ColorCache.get(192,192,192));

		setFont(font);

		// Top panel1 logic
		
		JPanel topPanel  = new JPanel();
		JPanel panel1    = new JPanel();
		JPanel panel2    = new JPanel();

		GridLayout gridLayout;
		
		topPanel.setLayout( new BorderLayout() );
		gridLayout = new GridLayout(22,1);
		gridLayout.setVgap(0);
		panel1.setLayout( gridLayout);

		gridLayout = new GridLayout(22,1);
		gridLayout.setVgap(0);
		panel2.setLayout( gridLayout);

		topPanel.add( BorderLayout.WEST, panel1);
		topPanel.add( BorderLayout.EAST, panel2);

		// ----------

		addPanel(panel1, "Show toolbar:");
		addPanel(panel2, m_show_toolbar);

		addPanel(panel1, "Show description box:");
		addPanel(panel2, m_show_desc);
		
		addPanel(panel1, "Show feedback box:");
		addPanel(panel2, m_show_feedback);
		
		addPanel(panel1, "Show Tab Box on left:");
		addPanel(panel2, m_left_tabbox);

		addPanel(panel1, "Scroll through tabs:");
		addPanel(panel2, m_tabs_scroll);

		addPanel(panel1, "Always show scrollbars:");
		addPanel(panel2, m_fix_scrollbars);
		
		addPanel(panel1, "Sort the Table of Contents:");
		addPanel(panel2, m_sort_toc);

		addPanel(panel1, "Show clients at top:");
		addPanel(panel2, m_top_clients);

		addPanel(panel1, "Show clients:");
		addPanel(panel2, m_show_clients);

		addPanel(panel1, "Show suppliers:");
		addPanel(panel2, m_show_suppliers);

		addPanel(panel1, "Use client/supplier compaction:");
		addPanel(panel2, m_use_compaction);

		addPanel(panel1, "Client/suppliers use visible edges:");
		addPanel(panel2, m_visible_edges);

		addPanel(panel1, "Client/suppliers use visible entities:");
		addPanel(panel2, m_visible_entities);

		addPanel(panel1, "Lift edges under closed entities:");
		addPanel(panel2, m_lift_edges);

		addPanel(panel1, "Show destination cardinal counts:");
		addPanel(panel2, m_show_dst_cardinals);

		addPanel(panel1, "Show source cardinal counts:");
		addPanel(panel2, m_show_src_cardinals);

		addPanel(panel1, "Nearest ancestor changing hierarchy:");
		addPanel(panel2, m_focus_ancestor);
	
		addPanel(panel1, "Hide empty classes in the legend:");
		addPanel(panel2, m_hide_empty);

		addPanel(panel1, "Show member counts in the legend:");
		addPanel(panel2, m_member_counts);

		addPanel(panel1, "Show inheritance in legend:");
		addPanel(panel2, m_inheritance);

		addPanel(panel1, "Show legend labels in black:");
		addPanel(panel2, m_legend_labels);

		m_scrollPane1 = new JScrollPane(topPanel);
		
		// JPanel2 setup
		
		topPanel    = new JPanel();
		panel1      = new JPanel();
		panel2      = new JPanel();

		JPanel panel3 = new JPanel();

		topPanel.setLayout( new BorderLayout() );
		gridLayout = new GridLayout(22,1);
		gridLayout.setVgap(0);
		panel1.setLayout( gridLayout);

		gridLayout = new GridLayout(22,1);
		gridLayout.setVgap(0);
		panel2.setLayout( gridLayout);

		gridLayout = new GridLayout(22,1);
		gridLayout.setVgap(0);
		panel3.setLayout( gridLayout);

		topPanel.add( BorderLayout.WEST,   panel1);
		topPanel.add( BorderLayout.EAST,   panel3);
		topPanel.add( BorderLayout.CENTER, panel2);

		// ----------

		addPanel(panel1, "Width of edge:");
		addPanel(panel2, m_line_width);
		addUnits(panel3, "Pixels");
		
		addPanel(panel1, "Length of arrow edge:");
		addPanel(panel2, m_arrow_length);
		addUnits(panel3, "Pixels");

		addPanel(panel1, "Width of arrow arc:");
		addPanel(panel2, m_arrow_arc);
		addUnits(panel3, "Radians");

		addPanel(panel1, "3D highlighting weight:");
		addPanel(panel2, m_pixels_3d);
		addUnits(panel3, "Pixels");

		addPanel(panel1, "Incidental shadow size:");
		addPanel(panel2, m_shadow_size);
		addUnits(panel3, "Pixels");

		addPanel(panel1, "Angle of label:");
		addPanel(panel2, m_label_angle);
		addUnits(panel3, "Degrees");

		addPanel(panel1, "Entity hover factor:");
		addPanel(panel2, m_hover_scale);
		addUnits(panel3, "Scale");

		addPanel(panel1, "Horizontal zoom factor:");
		addPanel(panel2, m_zoom_x);
		addUnits(panel3, "Scale");

		addPanel(panel1, "Vertical zoom factor:");
		addPanel(panel2, m_zoom_y);
		addUnits(panel3, "Scale");

		addPanel(panel1, "Fill arrow head:");
		addPanel(panel2, m_fill_arrowhead);

		addPanel(panel1, "Center arrow head:");
		addPanel(panel2, m_center_arrowhead);

		addPanel(panel1, "Permanently weight arrow head:");
		addPanel(panel2, m_permanently_weight);
		
		addPanel(panel1, "3D in Black&White:");
		addPanel(panel2, m_blackwhite_3d);

		addPanel(panel1, "Show fixed edge labels:");
		addPanel(panel2, m_show_edge_labels);

		addPanel(panel1, "Edge labels angle w.r.t edge:");
		addPanel(panel2, m_rotate_edge_labels);
		
		addPanel(panel1, "Show edge tooltip:");
		addPanel(panel2, m_show_edge_tooltip);

		addPanel(panel1, "Variable arrow color:");
		addPanel(panel2, m_variable_arrow_color);

		addPanel(panel1, "Invert edge label background:");
		addPanel(panel2, m_invert_edge_label_back);

		addPanel(panel1, "Label color inverts background");
		addPanel(panel2, m_label_invert_fore);
		
		addPanel(panel1, "Invert entity label background");
		addPanel(panel2, m_label_invert_back);

		addPanel(panel1, "Make entity label black/white");
		addPanel(panel2, m_entity_labels_blackwhite);
		
		m_scrollPane2 = new JScrollPane(topPanel);
		
		// JPanel3 setup
		
		topPanel    = new JPanel();
		panel1      = new JPanel();
		panel2      = new JPanel();

		topPanel.setLayout( new BorderLayout() );
		gridLayout = new GridLayout(22,1);
		gridLayout.setVgap(0);
		panel1.setLayout( gridLayout);

		gridLayout = new GridLayout(22,1);
		gridLayout.setVgap(0);
		panel2.setLayout( gridLayout);

		topPanel.add( BorderLayout.WEST,   panel1);
		topPanel.add( BorderLayout.EAST,   panel2);
		
		addPanel(panel1, "Load TA options:");
		addPanel(panel2, m_loadchoices);
		
		addPanel(panel1, "Edge mode:");
		addPanel(panel2, m_edgechoices);
		
		addPanel(panel1, "Icon search path:");
		addPanel(panel2, m_iconPath);
		
		addPanel(panel1, "Show icons:");
		addPanel(panel2, m_iconchoices);
		
		m_elision_icon.addItem("");
		if (diagram != null) {
			Vector	relationClasses = diagram.getRelationClasses();
			int		size            = relationClasses.size();
			int		i;
			
			for (i = 0; i < size; ++i) {
				m_elision_icon.addItem(relationClasses.elementAt(i));
		}	}
		
		addPanel(panel1, "Elision icon:");
		addPanel(panel2, m_elision_icon);
		
		addPanel(panel1, "Fixed Icon Shape:");
		addPanel(panel2, m_icon_fixed_shape);

		addPanel(panel1, "Set Diagram Grid:");
		panel2.add(m_setGridSize);
		m_setGridSize.addActionListener(this);
		
		addPanel(panel1, "Show grid:");
		addPanel(panel2, m_show_grid);
	
		addPanel(panel1, "Snap entities to grid:");
		addPanel(panel2, m_snap_to_grid);
		
		addPanel(panel1, "Chase max edges:");
		addPanel(panel2, m_chase_edges);
		addPanel(panel1, "Hide not chased:");
		addPanel(panel2, m_chase_hide);
		addPanel(panel1, "Visible spans");
		addPanel(panel2, m_visible_spans);

		addPanel(panel1, "Show span type");
		addPanel(panel2, m_show_span_type);
		addPanel(panel1, "Show class type");
		addPanel(panel2, m_show_class_type);
		addPanel(panel1, "Show navnext arrow");
		addPanel(panel2, m_show_navnext);
		addPanel(panel1, "Show navprev arrow");
		addPanel(panel2, m_show_navprev);
		
		addPanel(panel1, "Trace lifted:");
		addPanel(panel2, m_trace_lifted);
		addPanel(panel1, "Group the queried items:");
		addPanel(panel2, m_group_query);
		addPanel(panel1, "Queries persist:");
		addPanel(panel2, m_query_persists);
		
		m_scrollPane3 = new JScrollPane(topPanel);

		
		// ------------------
	
		Option options = Options.getDiagramOptions();
		
		m_current.setMainOptionsTo(options);
		m_current.setArrowOptionsTo(options);
		m_current.setDiagramOptionsTo(options);
		
		// ------------

		m_message = new JLabel(" ", JLabel.CENTER);
		m_message.setFont(font);
		m_message.setForeground(Color.RED);

		m_message.setSize(400,50);
		contentPane.add( BorderLayout.NORTH, m_message);

		addCenterThing();

		// --------------
		// Use a FlowLayout to center the button and give it margins.

		JPanel bottomPanel = new JPanel();

		bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));

		m_optionchoices.setFont(bold);
		m_optionchoices.setSelectedIndex(3);
		
		loadValues1();
		loadValues2();
		loadValues3();
		
		m_loadchoices.setEnabled(false);
	
		bottomPanel.add(m_optionchoices);

		JButton		button;
		int			i;
		String		tip;

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
		m_optionchoices.addActionListener(this);

		contentPane.add( BorderLayout.SOUTH, bottomPanel);

		// Resize the window to the preferred size of its components
		setLocation(20, 20);
		pack();
		setVisible(true);
	}

	// ActionListener interface

	public void actionPerformed(ActionEvent ev)
	{
		Object			source;
		String			msg, msg1;
		int				state, i;
		Option			target, current;
		boolean			firstPane, secondPane, thirdPane;

		// Pop down the window when the button is clicked.
		// System.out.println("event: " + ev);

		source = ev.getSource();
		
		if (source == m_optionchoices) {
			int index = m_optionchoices.getSelectedIndex();
			m_buttons[BUTTON_SAVE].setEnabled(index != 0);
			m_loadchoices.setEnabled(index != 3);
			return;
		}			

		current = m_current;

		if (source == m_setGridSize) {
			SetNewGridDialog	dialog    = new SetNewGridDialog(m_ls.getFrame(), current.getGridSize(), current.getGridColor());
			
			current.setGridSize(dialog.getGridSize());
			current.setGridColor(dialog.getGridColor());
			
			m_setGridSize.setText("" + current.getGridSize());
			m_setGridSize.setForeground(current.getGridColor());

			dialog.dispose();
			dialog = null;
			return;
		}

		state = -1;
		for (i = 0; i < m_button_titles.length; ++i) {
			if (source == m_buttons[i]) {
				state = i;
				break;
		}	}

		switch (state) {
		case BUTTON_CANCEL:
			break;
		case BUTTON_LOAD:
			target = getTarget();
			
			if (!m_use_tabs) {
				firstPane  = true;
				secondPane = true;
				thirdPane  = true;
				msg        = "All"; 
			} else {
				Object currentPane = ((JTabbedPane) m_centerThing).getSelectedComponent();
				firstPane  = false;
				secondPane = false;
				thirdPane  = false;
				if (currentPane == m_scrollPane1) {
					firstPane  = true;
					msg        = "Main"; 
				} else if (currentPane == m_scrollPane2) {
					secondPane = true;
					msg        = "Visualisation"; 
				} else {
					thirdPane  = true;
					msg        = "Diagram";
			}	} 

			if (firstPane) {
				current.setMainOptionsTo(target);
				loadValues1();
			}
			if (secondPane) {
				current.setArrowOptionsTo(target);
				loadValues2();
			}
			if (thirdPane) {
				current.setDiagramOptionsTo(target);
				loadValues3();
			}
			m_message.setText(msg + " options loaded from " + getTargetName() + " values");
			return;
		case BUTTON_SAVE:
		case BUTTON_OK:
			if (state == BUTTON_OK) {
				target = Options.getDiagramOptions();
			} else {
				target = getTarget();
			}
			if (target == null) {
				m_message.setText("Can't alter factory reset options");
				return;
			}
			if (!m_use_tabs) {
				firstPane  = true;
				secondPane = true;
				thirdPane  = true;
				msg        = "All"; 
			} else {
				Object currentPane = ((JTabbedPane) m_centerThing).getSelectedComponent();
				
				firstPane  = false;
				secondPane = false;
				thirdPane  = false;
				if (currentPane == m_scrollPane1) {
					firstPane  = true;
					msg        = "Main"; 
				} else if (currentPane == m_scrollPane2) {
					secondPane = true;
					msg        = "Visualisation"; 
				} else {
					thirdPane  = true;
					msg        = "Diagram";
			}	}
			
			if (thirdPane) {
				msg1 = saveValues3();
				if (msg1 != null) {
					m_message.setText(msg1);
					return;
				}
				target.setDiagramOptionsTo(current);
			}			
			if (secondPane) {
				msg1 = saveValues2();
				if (msg1 != null) {
					m_message.setText(msg1);
					return;
				}
				target.setArrowOptionsTo(current);
			}
			if (firstPane) {
				saveValues1();
				target.setMainOptionsTo(current);
			}
			if (state == BUTTON_OK) {
				break;
			}
			m_message.setText(msg + " values saved to " + getTargetName() + " options");
			return;
		case BUTTON_TAB:
			m_use_tabs = !m_use_tabs;
			m_contentPane.remove(m_centerThing);
			addCenterThing();
			m_buttons[BUTTON_TAB].setText(m_use_tabs ? "Detab" : "Tab");
			pack();
			return;
		case BUTTON_HELP:
			m_ls.showURL(m_ls.m_helpURL + "/options.html", LsLink.TARGET_HELP);
		default:
			return;
		}
		
		setVisible(false);
		return;
	}

	public static void create(LandscapeEditorCore ls) 
	{
		Option option     = Options.getDiagramOptions();
		Option oldOptions = new Option("OptionDialog old");
		
		oldOptions.setTo(option);

		OptionsDialog optionsDialog = new OptionsDialog(ls);
		optionsDialog.dispose();
		option.optionsChanged(ls, oldOptions);
	}
} 



