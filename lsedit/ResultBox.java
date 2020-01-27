package lsedit;

import java.util.Enumeration;
import java.util.Vector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
//import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.Scrollable;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class ResultBoxConfigure extends JDialog implements ActionListener {

	protected ResultBox			m_resultBox;
	protected JSpinner			m_left_show;
	protected JSpinner			m_right_show;
	protected JSpinner			m_max_size;

	static protected final int BUTTON_OK      = 0;
	static protected final int BUTTON_CANCEL  = 1;
	static protected final int BUTTON_HELP    = 2;

	protected final static String[] m_button_titles =
							{
								"Ok",
								"Cancel",
								"Help"
							};

	protected JButton[]			m_buttons;


	public ResultBoxConfigure(ResultBox resultBox, String title)
	{
		super(resultBox.getLs().getFrame(), title, true);

		JTabbedPane			tabbedPane;
		Container			contentPane;
		JPanel				centerPanel, buttonPanel;
		GridBagLayout		gridBagLayout;
		GridBagConstraints	c;
		Font				font, bold;
		JLabel				label;
		String				string;
		JButton				button;
		int					i;

		m_resultBox = resultBox;
		font        = FontCache.getDialogFont();
		bold        = font.deriveFont(Font.BOLD);

		tabbedPane  = resultBox.getTabbedPane();
		contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		setForeground(ColorCache.get(0,0,0));
		setBackground(ColorCache.get(192,192,192));
		setFont(font);

		centerPanel    = new JPanel();
		gridBagLayout = new GridBagLayout();
		centerPanel.setLayout(gridBagLayout);

		c           = new GridBagConstraints();

		label       = new JLabel("Left show ancestors");
		m_left_show = new JSpinner(new SpinnerNumberModel( resultBox.getLeftShowAncestors(), 0,Integer.MAX_VALUE, 1));


		c.gridx     = 0;
		c.anchor    = GridBagConstraints.EAST;
		c.fill      = GridBagConstraints.NONE;
		c.gridwidth = 1;
		c.weightx   = 0.0;
		gridBagLayout.setConstraints(label, c);
		centerPanel.add(label);

		c.gridx     = 1;
		c.anchor    = GridBagConstraints.WEST;
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx   = 1.0;
		gridBagLayout.setConstraints(m_left_show, c);
		centerPanel.add(m_left_show);

		label       = new JLabel("Right show ancestors");
		m_right_show = new JSpinner(new SpinnerNumberModel( resultBox.getRightShowAncestors(), 0, Integer.MAX_VALUE, 1));


		c.gridx     = 0;
		c.anchor    = GridBagConstraints.EAST;
		c.fill      = GridBagConstraints.NONE;
		c.gridwidth = 1;
		c.weightx   = 0.0;
		gridBagLayout.setConstraints(label, c);
		centerPanel.add(label);

		c.gridx     = 1;
		c.anchor    = GridBagConstraints.WEST;
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx   = 1.0;
		gridBagLayout.setConstraints(m_right_show, c);
		centerPanel.add(m_right_show);
		
		label       = new JLabel("Maximum report size");
		m_max_size  = new JSpinner(new SpinnerNumberModel( resultBox.getMaxSize(), 0, Integer.MAX_VALUE, 1));


		c.gridx     = 0;
		c.anchor    = GridBagConstraints.EAST;
		c.fill      = GridBagConstraints.NONE;
		c.gridwidth = 1;
		c.weightx   = 0.0;
		gridBagLayout.setConstraints(label, c);
		centerPanel.add(label);

		c.gridx     = 1;
		c.anchor    = GridBagConstraints.WEST;
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx   = 1.0;
		gridBagLayout.setConstraints(m_max_size, c);
		centerPanel.add(m_max_size);


		// --------------
		// Use a FlowLayout to center the button and give it margins.

		buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		m_buttons = new JButton[m_button_titles.length];
		for (i = 0; i < m_button_titles.length; ++i) {
			string = m_button_titles[i];
			m_buttons[i] = button = new JButton(string);
			button.setFont(bold);
			button.addActionListener(this);
			buttonPanel.add(button);
		}

		contentPane.add(BorderLayout.CENTER, centerPanel);
		contentPane.add(BorderLayout.SOUTH,  buttonPanel);

		// Resize the window to the preferred size of its components
		pack();
		setLocation(tabbedPane.getX(), tabbedPane.getY());
		setVisible(true);
	}

	private int	getSpinnerValue(JSpinner spinner)
	{
		Number	   value  = (Number) spinner.getValue();

		return value.intValue();
	}

	// ActionListener interface

	public void actionPerformed(ActionEvent ev)
	{
		Object		source;
		int			state, i;

		source = ev.getSource();

		state = -1;
		for (i = 0; i < m_button_titles.length; ++i) {
			if (source == m_buttons[i]) {
				state = i;
				break;
		}	}

		switch (state) {
		case BUTTON_HELP:
			JOptionPane.showMessageDialog(m_resultBox.getLs().getFrame(), 	
			  "The permitted options are:\n" +
			  "1) Left show ancestors.\n" +
			  "   The number of ancestors to be shown as part of left component names.\n" +
			  "2) Right show ancestors.\n" +
			  "   The number of ancestors to be shown as part of right component names.\n" +
			  "3) Maximum lines.\n" +
			  "   The maximum number of lines in this report."
				  , "Help", JOptionPane.OK_OPTION);
			return;
		case BUTTON_OK:
			m_resultBox.setShowAncestors(getSpinnerValue(m_left_show), getSpinnerValue(m_right_show));
			m_resultBox.setMaxSize(getSpinnerValue(m_max_size));
		case BUTTON_CANCEL:
			break;
		default:
			return;
		}
		setVisible(false);
		return;
	}
}

public final class ResultBox extends TabBox /* extends JComponent */ implements ChangeListener, TaListener, Scrollable, MouseListener 
{
	protected static final Color	m_titleColor = Color.red.darker();

	protected static Font m_titleFont = null;
	protected static Font m_textFont  = null;
	protected static final String	m_indent     = "    ";

	// <entity name> [<entity class name>]

	class ResultLabel extends JLabel
	{
		public ResultLabel()
		{
			super();
			setHorizontalAlignment(LEFT);
			setHorizontalTextPosition(LEFT);
			setFont(m_textFont);
		}

		public String toString()
		{
			return(getText());
		}
	}
	
	class ResultTitleLabel extends ResultLabel
	{
	}

	// <entity> [ {<class>} ]

	class ResultEntity extends ResultLabel implements MouseListener
	{
		protected EntityInstance		m_entity;

		public ResultEntity(String indent, EntityInstance entity, int depth, boolean showClass)
		{
			super();

			String		text;
			//FontMetrics	fm = getFontMetrics(m_textFont);
			//int			width, height;
			
			m_entity  = entity;

			setToolTipText(entity.getDescription());
			text = entity.getEntityLabel();
			if (showClass) {
				text += " {" + entity.getClassLabel() + "}";
			}

			for (; --depth >= 0; ) {
				entity = entity.getContainedBy();
				if (entity == null) {
					break;
				}
				text = entity.getEntityLabel() + "." + text;
			}
			text  = indent + text;

//			width  = fm.stringWidth(text);
//			height = fm.getHeight();
//			setSize(width, height);

			setText(text);
			setForeground(Color.blue);
			addMouseListener(this);
		}

		public void paintComponent(Graphics g)
		{
			Color	color;

			if (m_entity.isMarked(EntityInstance.DELETED_MARK)) {
				color = Color.black;
			} else {
				color = Color.blue;
			}
			setForeground(color);
			super.paintComponent(g);
		}

		// MouseListener interface

		public void mouseClicked(MouseEvent e)
		{
		}

		public void mouseEntered(MouseEvent e)
		{
			if (!m_entity.isMarked(EntityInstance.DELETED_MARK)) {
				setForeground(Color.red);
				repaint();
		}	}

		public void mouseExited(MouseEvent e)
		{
			if (!m_entity.isMarked(EntityInstance.DELETED_MARK)) {
				setForeground(Color.blue);
				repaint();
		}	}

		public void mousePressed(MouseEvent ev)
		{
			if (ev.isAltDown()) {
				EntityInstance e = m_entity;

				if(!e.isMarked(EntityInstance.DELETED_MARK))	{
					e.startHover();
		}	}	}

		public void mouseReleased(MouseEvent ev)
		{
			EntityInstance e = m_entity;
			
			if (!e.isMarked(EntityInstance.DELETED_MARK)) {
				if (!e.endHover()) {
					setForeground(Color.blue);
					m_ls.followLink(e, true);
		}	}	}
	}

	class ResultAttribute extends ResultLabel
	{
		public ResultAttribute(String indent, Attribute attribute)
		{
			super();

			String		text;
			
			text     = indent + attribute.toString();
			setText(text);
//			setForeground(Color.black);
		}
	}

	class HorizontalResult extends JComponent
	{
		public HorizontalResult()
		{	
			super();
			setAlignmentX(LEFT_ALIGNMENT);
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		}

		public String toString()
		{
			int			i, cnt;
			Component	component;
			String		string;

			cnt      = getComponentCount();
			string   = "";
			for (i = 0; i < cnt; ++i) {
				component = getComponent(i);
				string += component.toString();
			}
			return string;
		}
	}

	class VerticalResult extends JComponent
	{
		public VerticalResult()
		{
			super();
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		}

		public String toString()
		{
			int			i, cnt;
			Component	component;
			String		string;

			cnt      = getComponentCount();
			string   = "";
			for (i = 0; i < cnt; ++i) {
				component = getComponent(i);
				string   += component.toString() + "\n";
			}
			return string;
		}
	}

	//  <Entity name> [<relation class>] <Entity name>

	class ResultRelation extends HorizontalResult
	{
		protected RelationInstance		m_relation;

		public ResultRelation(String indent, RelationInstance relation, boolean hideRc)
		{	
			super();

			ResultEntity	resultEntity;
			ResultLabel		rc;

			m_relation = relation;
			resultEntity = new ResultEntity(indent, relation.getSrc(), getLeftShowAncestors(), false);
			add(resultEntity);
			if (!hideRc) {
				rc = new ResultLabel();
				rc.setText(" " + relation.getClassLabel());
				rc.setHorizontalAlignment(JLabel.CENTER);
				add(rc);
			}
			resultEntity = new ResultEntity(" ", relation.getDst(), getRightShowAncestors(), false);
			add(resultEntity);
		}

		public boolean matches(RelationInstance relation)
		{
			return(m_relation.matches(relation));
		}
	}

	// <ResultEntity> <rc> [*|?] 
	// [*|?] <rc> <ResultEntity>

	class ResultSetHeader extends HorizontalResult
	{
		public ResultSetHeader(EntityInstance entity, RelationClass relationClass, boolean forward, boolean closure)
		{
			super();

			Enumeration			en;
			ResultEntity		resultEntity;
			ResultLabel			rc, label;
			Object				object;
			EntityInstance		e;
			RelationInstance	r;
			String				type;

			if (closure) {
				type = "*";
			} else {
				type = "?";
			}

			if (relationClass == null) {
				rc  = null;
			} else {
				rc           = new ResultLabel();
				rc.setText(" " + relationClass.getLabel());
				rc.setHorizontalAlignment(JLabel.CENTER);
			}
			label        = new ResultLabel();
			label.setHorizontalAlignment(JLabel.LEFT);

			if (forward) {
				resultEntity = new ResultEntity("", entity, getLeftShowAncestors(), false);
				add(resultEntity);
				if (rc != null) {
					add(rc);
				}
				label.setText(" " + type);
				add(label);
			} else {
				label.setText(type);
				add(label);
				if (rc != null) {
					add(rc);
				}
				resultEntity = new ResultEntity(" ", entity, getRightShowAncestors(), false);
				add(resultEntity);
			}
		}

		public ResultSetHeader(EntityInstance entity, Vector list)
		{
			super();

			ResultEntity	resultEntity;
			ResultLabel		label;

			resultEntity = new ResultEntity("", entity, getLeftShowAncestors(), false);
			label        = new ResultLabel();
			label.setText(" contains (" + list.size() + " items):");
			add(resultEntity);
			add(label);
		}
	}

	/*
		<entity> <relation class> ?
			<entity>
			...
			<entity>
	 */

	class ResultSet extends VerticalResult
	{
		public ResultSet(EntityInstance entity, RelationClass relationClass, Vector entities, boolean forward, boolean closure)
		{
			super();

			Enumeration			en;
			ResultEntity		resultEntity;
			ResultRelation		resultRelation;
			Object				object;
			EntityInstance		e;
			RelationInstance	r;
			boolean				hideRelation = (relationClass != null);

			add(new ResultSetHeader(entity, relationClass, forward, closure));
			for (en = entities.elements(); en.hasMoreElements(); ) {
				object = en.nextElement();
				if (object instanceof EntityInstance) {
					e = (EntityInstance) object;
					resultEntity = new ResultEntity(m_indent, e, getRightShowAncestors(), false);
					add(resultEntity);
				} else {
					r = (RelationInstance) object;
					resultRelation = new ResultRelation(m_indent, r, hideRelation);
					add(resultRelation); 
			}	}
		}
	}

	/*
		<entity> <relation class> ?
			<entity>
			...
			<entity>
	 */

	class ResultContents extends VerticalResult
	{
		public ResultContents(EntityInstance entity, Vector entities)
		{
			super();

			Enumeration			en;
			ResultEntity		resultEntity;
			Object				object;
			EntityInstance		e;

			add(new ResultSetHeader(entity, entities));
			for (en = entities.elements(); en.hasMoreElements(); ) {
				e = (EntityInstance) en.nextElement();
				resultEntity = new ResultEntity(m_indent, e, getRightShowAncestors(), true);
				add(resultEntity);
			}
		}
	}

	protected static final int	horizontal_margin    = 10;
	protected static final int	vertical_indent      = 10;

	public    static final String m_helpStr = "This box shows the results of queries, and groupings";

	protected BoxLayout				m_boxLayout;
	private	  boolean				m_refill = false;

	protected	int					m_left_show  = 0;
	protected	int					m_right_show = 0;
	protected	int					m_max_size   = 500;
	protected	int					m_lines      = 0;


	// --------------
	// Object methods
	// --------------

	public String toString()
	{
		int			i, cnt;
		Component	component;
		String		string;

		cnt      = getComponentCount();
		string   = "";
		for (i = 0; i < cnt; ++i) {
			component = getComponent(i);
			string += component.toString() + "\n---\n";
		}
		return string;
	}

	// ------------------
	// JComponent methods
	// ------------------

/*
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		int	width, height;

		width   = getWidth();
		height  = getHeight();
		
		System.out.println("ResultBox.paintComponent width=" + width + " height=" + height);
		// For debugging
		g.setColor(Color.green);
		g.drawLine(0, 0, width, height);
		g.drawLine(0, height, width, 0);
	}
*/

	// --------------
	// Public methods 
	// --------------

	public ResultBox(LandscapeEditorCore ls, JTabbedPane tabbedPane) 
	{
		super(ls, tabbedPane, "Results", m_helpStr);

		if (m_titleFont == null) {
			m_titleFont = Options.getTargetFont(Option.FONT_RESULTS_TITLE);
		}
		if (m_textFont  == null) {
			m_textFont  = Options.getTargetFont(Option.FONT_RESULTS_TEXT);
		}
		m_boxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(m_boxLayout);

		tabbedPane.addChangeListener(this);
		addMouseListener(this);
	}

	public static void setTitleFont(Font font)
	{
		m_titleFont = font;
	}

	public static void setTextFont(Font font)
	{
		m_textFont = font;
	}

	public JTabbedPane getTabbedPane()
	{
		return m_tabbedPane;
	}

	public int getLeftShowAncestors()
	{
		return m_left_show;
	}

	public int getRightShowAncestors()
	{
		return m_right_show;
	}

	public void setShowAncestors(int left, int right)
	{
		m_left_show  = left;
		m_right_show = right;
	}
	
	public int getMaxSize()
	{
		return m_max_size;
	}
	
	public void setMaxSize(int value)
	{
		m_max_size = value;
	}
	
	public boolean maxSize()
	{
		if (m_lines > m_max_size) {
			return true;
		}
		if (m_lines == m_max_size) {
			showAll();
			ResultBoxConfigure configure = new ResultBoxConfigure(this, "Maximum lines observed");
			configure.dispose();
			if (m_lines >= m_max_size) {
				m_lines = -1;
				done("Report truncated at " + m_max_size + " lines");
				m_lines = m_max_size + 1;
				return true; 
		}	}
		++m_lines;
		return false;
	}

	public void clear() 
	{
//		System.out.println("Cleared result box");
//		java.lang.Thread.dumpStack();

		m_lines = 0;
		removeAll();
		revalidate();
		repaint();
	}

	public void addResultTitle(String title)
	{
		if (title != null && !maxSize()) {
			ResultLabel	label = new ResultTitleLabel();
			label.setText(title);
			label.setHorizontalAlignment(JLabel.LEFT);
			label.setFont(m_titleFont);
			label.setForeground(m_titleColor);
			add(label);
			add(Box.createVerticalStrut(5));
	}	}

	public void setResultTitle(String title)
	{
		clear();
		addResultTitle(title);
	}

	public void addResultEntity(EntityInstance e, int showAncestors)
	{
		if (!maxSize()) {
			add(new ResultEntity("", e, showAncestors, false));
	}	}

	public void addResultAttribute(Attribute attribute)
	{
		if (!maxSize()) {
			add(new ResultAttribute("  ", attribute));
	}	}

	public void addRelation(RelationInstance r)
	{
		if (!maxSize()) {
			add(new ResultRelation("", r, false));
		}
	}

	public void addRelation(String indent, RelationInstance r)
	{
		if (!maxSize()) {
			add(new ResultRelation(indent, r, false));
	}	}

	public void addRelations(EntityInstance e, RelationClass rc, Vector list, boolean isForwards, boolean withClosure)
	{
		if (!maxSize()) {
			add(new ResultSet(e, rc, list, isForwards, withClosure));
	}	}

	protected void addContents(EntityInstance e, Vector list)
	{
		if (!maxSize()) {
			add(new ResultContents(e, list));
	}	}

	public void addText(String message)
	{
		if (message != null && !maxSize()) {
			ResultLabel	label = new ResultLabel();
			label.setText(message);
			label.setHorizontalAlignment(JLabel.LEFT);
			label.setForeground(Color.black);
			add(label);
	}	}

	private void sizeIt()
	{
		JScrollPane scrollPane = m_scrollPane;
		Insets		insets	   = scrollPane.getInsets();
		Dimension	d          = m_boxLayout.preferredLayoutSize(this);
		int			w1         = scrollPane.getWidth()  - insets.left - insets.right;
		int			h1         = scrollPane.getHeight() - insets.top  - insets.bottom;

		if (w1 > d.width) {
			d.width = w1;
		}
		if (h1 > d.height) {
			d.height = h1;
		}
		setPreferredSize(d);
		setBounds(0, 0, d.width, d.height);
	}

	public void showAll()
	{
		sizeIt();
		validate();

		m_scrollPane.revalidate();
	}
	
	public void done(String footer)
	{
		if (!maxSize()) {
			if (footer != null) {
				add(Box.createVerticalStrut(10));
				addText(footer);
			}
			showAll();
		}
	}

	/* Show a list of entity labels vertically in order contained in vector 
	 * Used by GROUP ALL and FIND
	 */

	public void showResults(String title, Vector v, String footer) 
	{
		setResultTitle(title);

		if (v.size() > 0) {
			Enumeration		en;
			EntityInstance	e;

			for (en = v.elements(); en.hasMoreElements(); ) {
				e = (EntityInstance) en.nextElement();
				addResultEntity(e, getLeftShowAncestors());
			}
		} else {
			footer = "No entities";
		}
		done(footer);
	}
	
	public void fontChanged()
	{
		Component	component;
		int			i;
		Font		titleFont = m_titleFont;
		Font		textFont  = m_textFont;
		Font		font;
		
		for (i = getComponentCount(); --i >= 0; ) {
			component = getComponent(i);
			if (component instanceof ResultTitleLabel) {
				font = titleFont;
			} else {
				font = textFont;
			}
			component.setFont(font);
	}	}

	// ChangeListener interface

	public void stateChanged(ChangeEvent e) 
	{
		if (isActive()) {
			sizeIt();
			repaint();
	}	}

	// TaListener interface
	
	public void diagramChanging(Diagram diagram)
	{
		clear();
	}

	public void diagramChanged(Diagram diagram, int signal)
	{
	}

	public void updateBegins()
	{
	}

	public void updateEnds()
	{
		if (m_refill) {
			m_refill = false;
			repaint();
	}	}

	public void entityClassChanged(EntityClass ec, int signal)
	{
	}

	public void relationClassChanged(RelationClass rc, int signal)
	{
	}

	public void entityParentChanged(EntityInstance e, EntityInstance parent, int signal)
	{
		m_refill = true;
	}

/*
	public void relationParentChanged(RelationInstance ri, int signal)
	{
	}

	public void entityInstanceChanged(EntityInstance e, int signal)
	{
	}

	public void relationInstanceChanged(RelationInstance ri, int signal)
	{
	}
 */
	public void entityCut(EntityInstance e)
	{
	}

	// MouseListener interface 

	public void mouseClicked(MouseEvent ev)
	{
		if (ev.isMetaDown()) {
			ResultBoxConfigure configure = new ResultBoxConfigure(this, "Configure resultBox");
			configure.dispose();
	}	}

	public void mouseEntered(MouseEvent ev)
	{
	}

	public void mouseExited(MouseEvent ev)
	{
	}

	public void mousePressed(MouseEvent ev)
	{
	}

	public void mouseReleased(MouseEvent ev)
	{
	}

	// Scrollable interface
	// We need to implement this to stop iterative size changes..
	// The resultbox knows what size it wants to be and sets it on every validate
	// If we don't disable it the viewport tries to change the size to fit the window every time it validates

	public Dimension getPreferredScrollableViewportSize()
	{
		return(getSize());
	}
	 
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		return(50);
	}
	 
	public boolean getScrollableTracksViewportHeight() 
	{
		return(false);
	}

	public boolean getScrollableTracksViewportWidth() 
	{
		return(false);
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		return(10);
	} 
}






