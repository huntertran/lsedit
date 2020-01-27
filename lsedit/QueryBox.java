package lsedit;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

import javax.swing.event.ChangeListener;

class LiftEntityClassEdges implements ActionListener
{
	private LandscapeEditorCore	m_ls;

	public LiftEntityClassEdges(LandscapeEditorCore ls)
	{
		m_ls = ls;
	}

	public void actionPerformed(ActionEvent ev)
	{
		LandscapeEditorCore ls = m_ls;

		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (ls.processMetaKeyEvent(QueryBox.g_liftActiveRelations)) {
				return;
		}	}

		switch (JOptionPane.showConfirmDialog(null, "Raise active edges in active entities", "Lift edges to parent else delete", JOptionPane.YES_NO_CANCEL_OPTION)) {
		case JOptionPane.YES_OPTION:
		{
			Diagram		diagram = ls.getDiagram();

			ls.beginUndoRedo(QueryBox.g_liftActiveRelations);
			diagram.updateLiftEdges();
			ls.endUndoRedo();
			break;
		}
		case JOptionPane.NO_OPTION:
			break;
		}
}	}

class DeleteActiveEntities implements ActionListener
{
	private LandscapeEditorCore	m_ls;

	public DeleteActiveEntities(LandscapeEditorCore	ls)
	{
		m_ls = ls;
	}

	public void actionPerformed(ActionEvent ev)
	{
		LandscapeEditorCore ls = m_ls;

		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (ls.processMetaKeyEvent(QueryBox.g_deleteActiveEntities)) {
				return;
		}	}

		switch (JOptionPane.showConfirmDialog(null, "Delete active entities below draw root", "Delete entities", JOptionPane.YES_NO_CANCEL_OPTION)) {
		case JOptionPane.YES_OPTION:
		{
			Diagram		diagram = ls.getDiagram();

			ls.beginUndoRedo(QueryBox.g_deleteActiveEntities);
			diagram.updateDeleteActiveEntities();
			ls.endUndoRedo();
			break;
		}
		case JOptionPane.NO_OPTION:
			break;
		}
}	}

class DeleteActiveContainers implements ActionListener
{
	private LandscapeEditorCore	m_ls;

	public DeleteActiveContainers(LandscapeEditorCore	ls)
	{
		m_ls = ls;
	}

	public void actionPerformed(ActionEvent ev)
	{
		LandscapeEditorCore	ls = m_ls;

		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (ls.processMetaKeyEvent(QueryBox.g_deleteActiveContainers)) {
				return;
		}	}

		switch (JOptionPane.showConfirmDialog(null, "Delete active containers below draw root", "Delete containers", JOptionPane.YES_NO_CANCEL_OPTION)) {
		case JOptionPane.YES_OPTION:
		{
			Diagram		diagram = ls.getDiagram();

			ls.beginUndoRedo(QueryBox.g_deleteActiveContainers);
			diagram.updateDeleteActiveContainers();
			ls.endUndoRedo();
			break;
		}
		case JOptionPane.NO_OPTION:
			break;
		}
}	}

class DuplicateEdges implements ActionListener
{
	LandscapeEditorCore	m_ls;
	RelationClass		m_rc;

	public DuplicateEdges(LandscapeEditorCore ls, RelationClass rc)
	{
		m_ls = ls;
		m_rc = rc;
	}

	public void actionPerformed(ActionEvent ev)
	{	
		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (m_ls.processMetaKeyEvent(QueryBox.g_duplicateRelations)) {
				return;
		}	}
		m_ls.duplicateEdges(m_rc);
	}
}

class DeleteActiveEdges implements ActionListener
{
	private	LandscapeEditorCore	m_ls;

	public DeleteActiveEdges(LandscapeEditorCore	ls)
	{
		m_ls = ls;
	}

	public void actionPerformed(ActionEvent ev)
	{
		LandscapeEditorCore	ls = m_ls;

		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (ls.processMetaKeyEvent(QueryBox.g_deleteActiveRelations)) {
				return;
		}	}

		switch (JOptionPane.showConfirmDialog(null, "Delete active relations in active entities below draw root", "Delete edges", JOptionPane.YES_NO_CANCEL_OPTION)) {
		case JOptionPane.YES_OPTION:
		{
			Diagram		diagram = ls.getDiagram();

			ls.beginUndoRedo(QueryBox.g_deleteActiveRelations);
			diagram.updateDeleteActiveEdges();
			ls.endUndoRedo();
			break;
		}
		case JOptionPane.NO_OPTION:
			break;
		}
}	}

class ResetIOpoints implements ActionListener
{
	private	LandscapeEditorCore	m_ls;

	public ResetIOpoints(LandscapeEditorCore	ls)
	{
		m_ls = ls;
	}

	public void actionPerformed(ActionEvent ev)
	{
		LandscapeEditorCore	ls = m_ls;

		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (ls.processMetaKeyEvent(QueryBox.g_resetIOpoints)) {
				return;
		}	}
		Diagram diagram = ls.getDiagram();
		if (diagram != null) {
			ls.beginUndoRedo(QueryBox.g_resetIOpoints);
			diagram.resetIOpoints();
			ls.endUndoRedo();
			diagram.repaint();
}	}	}

class TraceLiftedChkBox extends JCheckBox implements ItemListener
{
	LandscapeEditorCore	m_ls;

	TraceLiftedChkBox(LandscapeEditorCore ls)
	{
		super("Trace lifted");

		m_ls = ls;
		setSelected(Options.isTraceLifted());
		addItemListener(this);
	}

	public void itemStateChanged(ItemEvent ev)
	{
		Option				option = Options.getDiagramOptions();
		LandscapeEditorCore ls     = m_ls;

		option.setTraceLifted(isSelected());
		ls.doFeedback("Lifted edges " + (option.isGroupQuery() ? "are" : "aren't") + " traced");
		ls.requestFocus();
	}
}	

class GrpChkBox extends JCheckBox implements ItemListener
{
	LandscapeEditorCore	m_ls;

	GrpChkBox(LandscapeEditorCore ls)
	{
		super("Group with queries");

		m_ls = ls;
		setSelected(Options.isGroupQuery());
		addItemListener(this);
	}

	public void itemStateChanged(ItemEvent ev)
	{
		Option				option = Options.getDiagramOptions();
		LandscapeEditorCore ls     = m_ls;

		option.setGroupQuery(isSelected());
		ls.doFeedback("Entities " + (option.isGroupQuery() ? "are" : "aren't") + " grouped with queries");
		ls.requestFocus();
	}
}	

class QueryPersistsChkBox extends JCheckBox implements ItemListener
{
	LandscapeEditorCore	m_ls;

	QueryPersistsChkBox(LandscapeEditorCore ls)
	{
		super("Queries persist");

		m_ls = ls;
		setSelected(Options.isQueryPersists());
		addItemListener(this);
	}

	public void itemStateChanged(ItemEvent ev)
	{
		Option option          = Options.getDiagramOptions();
		LandscapeEditorCore ls = m_ls;
		
		option.setQueryPersists(isSelected());
		ls.doFeedback("Queries " + (option.isQueryPersists() ? "are" : "aren't") + " persisted");
		ls.requestFocus();
	}
}	

public final class QueryBox extends ERBox /* extends TabBox extends JComponent */ implements ChangeListener, TaListener, MouseListener
{
	public final static String g_duplicateRelations			= "Duplicate Relations";
	public final static String g_liftActiveRelations		= "Lift Active Relations";
	public final static String g_deleteActiveEntities       = "Delete Active Entities";
	public final static String g_deleteActiveContainers     = "Delete Active Containers";
	public final static String g_deleteActiveRelations      = "Delete Active Relations";
	public final static String g_resetIOpoints				= "Reset IO points";

	public final static String m_helpStr = "This box shows the active entities and relations during update and queries";

	protected static Font	m_titleFont = null;
	protected static Font	m_textFont  = null;

	// Objects used in layout

	protected TraceLiftedChkBox		m_traceLiftedChk;
	protected GrpChkBox				m_grpChk;
	protected QueryPersistsChkBox	m_queryPersistsChk;

	// --------------
	// Public methods 
	// --------------

	public QueryBox(LandscapeEditorCore ls, JTabbedPane tabbedPane) 
	{
		super(ls, tabbedPane, "Query", m_helpStr);

		Font titleFont = m_titleFont;
		Font textFont  = m_textFont;

		if (titleFont == null) {
			m_titleFont = titleFont = Options.getTargetFont(Option.FONT_QUERY_TITLE);
		}
		if (textFont == null) {
			m_textFont = textFont = Options.getTargetFont(Option.FONT_QUERY_TEXT);
		}

		m_ulabel = new JLabel("Active Entities");
		m_ulabel.setForeground(Color.red);

		m_elabel1 = new JLabel("Pass cursor over entity for description.");
		m_elabel1.setForeground(Color.black);

		m_elabel2 = new JLabel("Right click for menu.");
		m_elabel2.setForeground(Color.black);

		m_rlabel = new JLabel("Active Relations");
		m_rlabel.setForeground(Color.red);

		m_rlabel2 = new JLabel("Active Reversed Relations");
		m_rlabel2.setForeground(Color.red);

		m_clabel.setForeground(Color.red);

		m_footer1 = new JLabel("Checkboxes select query relations.");
		m_footer1.setForeground(Color.black);

		m_footer2 = new JLabel("Click relation arrow for description.");
		m_footer2.setForeground(Color.black);

		m_traceLiftedChk = new TraceLiftedChkBox(m_ls);
		m_traceLiftedChk.setForeground(Color.black);
		
		m_grpChk = new GrpChkBox(m_ls);
		m_grpChk.setForeground(Color.black);
//		m_grpChk.setToolTip("Group satisfying items");

		m_queryPersistsChk = new QueryPersistsChkBox(m_ls);
		m_queryPersistsChk.setForeground(Color.black);
//		m_queryPersistsChk.setToolTip("Prior queries persist");
		
		setComponentsTitleFont(titleFont);
		setComponentsTextFont(textFont);
	}

	public static void setTitleFont(Font font)
	{
		m_titleFont = font;
	}
	
	public void setComponentsTextFont(Font textFont)
	{
		super.setComponentsTextFont(textFont);
		m_traceLiftedChk.setFont(textFont);
		m_grpChk.setFont(textFont);
		m_queryPersistsChk.setFont(textFont);
	}

	public Font getTextFont()
	{
		return m_textFont;
	}

	public static void setTextFont(Font font)
	{
		m_textFont = font;
	}
	
	public void setTraceLifted(boolean value)
	{
		if (m_traceLiftedChk.isSelected() != value) {
			m_traceLiftedChk.setSelected(value);
	}	}

	public void setGroupQuery(boolean value)
	{
		if (m_grpChk.isSelected() != value) {
			m_grpChk.setSelected(value);
	}	}

	public void setQueryPersists(boolean value)
	{
		if (m_queryPersistsChk.isSelected() != value) {
			m_queryPersistsChk.setSelected(value);
	}	}

	protected boolean getFlag(EntityClass ec)
	{
		return ec.isActive();
	}
	
	protected int setFlag(EntityClass ec, boolean value, boolean applyToSubclasses)
	{
		return ec.setActive(value, applyToSubclasses);
	}

	protected boolean getFlag(RelationClass rc, int direction)
	{
		return (rc.isActive(direction));
	}

	protected int setFlag(RelationClass rc, int direction, boolean value, boolean applyToSubclasses)
	{
		return rc.setActive(direction, value, applyToSubclasses);
	}
	
	protected String getFlagName()
	{
		return "Active";
	}

	protected void customFill()
	{
		add(m_traceLiftedChk);
		m_height += 10;
		add(m_grpChk);
		m_height += 10;
		add(m_queryPersistsChk);
	}
	
	protected void customRelationOptions(JPopupMenu popup, RelationClass rc)
	{
		LandscapeEditorCore ls = getLs();
		JMenuItem			mi;

		mi = new JMenuItem(g_duplicateRelations);
		mi.addActionListener(new DuplicateEdges(ls, rc));
		popup.add(mi);
	}
	
	protected void customOptions(JPopupMenu popupMenu)
	{
		LandscapeEditorCore	ls      = m_ls;
		JMenuItem			mi;

		mi = new JMenuItem(QueryBox.g_liftActiveRelations);
		mi.addActionListener(new LiftEntityClassEdges(ls));
		popupMenu.add(mi);

		mi = new JMenuItem(g_deleteActiveEntities);
		mi.addActionListener(new DeleteActiveEntities(ls));
		popupMenu.add(mi);

		mi = new JMenuItem(g_deleteActiveContainers);
		mi.addActionListener(new DeleteActiveContainers(ls));
		popupMenu.add(mi);

		mi = new JMenuItem(g_deleteActiveRelations);
		mi.addActionListener(new DeleteActiveEdges(ls));
		popupMenu.add(mi);

		mi = new JMenuItem(g_resetIOpoints);
		mi.addActionListener(new ResetIOpoints(ls));
		popupMenu.add(mi);
	}
}

