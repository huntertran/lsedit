package lsedit;

import java.util.Vector;
import java.util.Enumeration;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.PrintWriter;
import java.io.CharArrayWriter;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JFrame;

class RenameView implements ActionListener
{
	private View m_view;

	public RenameView(View view)
	{
		m_view = view;
	}

	public void actionPerformed(ActionEvent ev)
	{
		View				view    = m_view;
		Diagram				diagram = view.getDiagram();
		LandscapeEditorCore	ls      = diagram.getLs();
		
		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (ls.processMetaKeyEvent("RenameView")) {
				return;
		}	}
		
		view.rename();
	}
}

class DeleteView implements ActionListener
{
	private View m_view;

	public DeleteView(View view)
	{
		m_view = view;
	}

	public void actionPerformed(ActionEvent ev)
	{
		View				view    = m_view;
		Diagram				diagram = view.getDiagram();
		LandscapeEditorCore	ls      = diagram.getLs();
		
		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (ls.processMetaKeyEvent("DeleteView")) {
				return;
		}	}
		
		diagram.removeView(view);
	}
}

class MoveView implements ActionListener
{
	private View m_view;

	public MoveView(View view)
	{
		m_view = view;
	}

	public void actionPerformed(ActionEvent ev)
	{
		View				view    = m_view;
		Diagram				diagram = view.getDiagram();
		LandscapeEditorCore	ls      = diagram.getLs();
		
		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (ls.processMetaKeyEvent("MoveView")) {
				return;
		}	}
		
		view.move();
		ViewBox				viewBox = ls.getViewBox();
		viewBox.fill();
	}
}

class ViewName extends JDialog implements ActionListener {

	static protected final int BUTTON_OK         = 0;
	static protected final int BUTTON_FOREGROUND = 1;
	static protected final int BUTTON_BACKGROUND = 2;

	protected final static String[] m_button_titles =
							{
								"Ok",
								"Foreground",
								"Background"
							};

	protected final static String[] m_button_tips =
							{
								null,
								"Change foreground color",
								"Change background color"
							};

	protected JFrame		m_frame;
	protected View			m_view;
	protected JTextField	m_name;
	protected JTextField	m_description;
	protected JCheckBox     m_showSelected;
	protected JButton[]		m_buttons;

	private void showColor()
	{
		View	view = m_view;
		JButton foreground, background;
		Color	color;
		
		foreground = m_buttons[BUTTON_FOREGROUND];
		background = m_buttons[BUTTON_BACKGROUND];
		color      = view.getForeground();
		foreground.setForeground(color);
		background.setForeground(color);
		color      = view.getBackground();
		foreground.setBackground(color);
		background.setBackground(color);
		foreground.repaint();
		background.repaint();
	}
	
	public ViewName(JFrame frame, View view)
	{
		super(frame, "Enter View Details", true);

		Container	contentPane;
		Font		font, bold;
		JLabel		label;
		JTextField	textField;
		JButton		button;
		int			i;
		String		tip;
		JCheckBox	showSelected;
		
		m_frame     = frame;
		m_view      = view;
		font        = FontCache.getDialogFont();
		bold        = font.deriveFont(Font.BOLD);

		contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		setForeground(ColorCache.get(0,0,0));
		setBackground(ColorCache.get(192,192,192));
		setFont(font);
		
		JPanel centerPanel1 = new JPanel();
		GridLayout gridLayout;

		gridLayout = new GridLayout(6,1);
		gridLayout.setVgap(0);
		centerPanel1.setLayout( gridLayout);
		
		label = new JLabel("View Name:");
		label.setFont(bold);
		centerPanel1.add(label);
				
		m_name = textField = new JTextField(view.getText(), 32);
		textField.setFont(font);
		centerPanel1.add(textField);
		
		label = new JLabel("View Description:");
		label.setFont(bold);
		centerPanel1.add(label);
		
		m_description = textField = new JTextField(view.getToolTipText(), 32);
		textField.setFont(font);
		centerPanel1.add(textField);

		m_showSelected = showSelected = new JCheckBox("Show Only Selected",false);
		centerPanel1.add(showSelected);
		
		contentPane.add(BorderLayout.CENTER, centerPanel1);
		
		JPanel bottomPanel = new JPanel();

		bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));

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
		
		showColor();
		
		contentPane.add( BorderLayout.SOUTH, bottomPanel);

		// Resize the window to the preferred size of its components
		pack();
		setLocation(20, 20);
		setVisible(true);
	}
	
	// ActionListener interface

	public void actionPerformed(ActionEvent ev)
	{
		View	view   = m_view;
		Object	source = ev.getSource();
		int		i, state;
		
		state = -1;
		for (i = 0; i < m_button_titles.length; ++i) {
			if (source == m_buttons[i]) {
				state = i;
				break;
		}	}

		switch (state) {
		case BUTTON_FOREGROUND:
		case BUTTON_BACKGROUND:
			JButton		 foreground, background;
			ColorChooser colorChooser = new ColorChooser(m_frame, "Pick a color", view.getForeground(), false /* don't include alpha */, false /* No null */);
			Color        color        = colorChooser.getColor();
			if (state == BUTTON_FOREGROUND) {
				view.setForeground(color);
			} else {
				view.setBackground(color);
			}
			colorChooser.dispose();
			showColor();
			return;
		}
		
		view.setText(m_name.getText());
		view.setDescription(m_description.getText());
		view.setShowEntities(m_showSelected.isSelected());
		setVisible(false);
		return;
	}	
}

class ViewMove extends JDialog implements ActionListener {

	protected JFrame		m_frame;
	protected View			m_view;
	protected JTextField	m_move;
	protected int			m_direction = 0;
	protected int			m_offset    = -1;
	
	public ViewMove(JFrame frame, View view)
	{
		super(frame, "View Move", true);

		Container	contentPane;
		Font		font, bold;
		JLabel		label;
		JTextField	textField;
		
		m_frame     = frame;
		m_view      = view;
		font        = FontCache.getDialogFont();
		bold        = font.deriveFont(Font.BOLD);

		contentPane = getContentPane();
		contentPane.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));

		setForeground(ColorCache.get(0,0,0));
		setBackground(ColorCache.get(192,192,192));
		setFont(font);
		
		label = new JLabel("Move [+/-]#: ");
		label.setFont(bold);
		contentPane.add(label);

		m_move = textField = new JTextField("", 10);
		textField.setFont(font);
		contentPane.add(textField);
		textField.addActionListener(this);
		
		// Resize the window to the preferred size of its components
		pack();
		setLocation(20, 20);
		setVisible(true);
	}
	
	public int getDirection()
	{
		return m_direction;
	}
	
	public int getOffset()
	{
		return m_offset;
	}
	
	// ActionListener interface

	public void actionPerformed(ActionEvent ev)
	{
		String	text      = m_move.getText();
		
		text = text.trim();
		if (text.length() > 0) {
			switch (text.charAt(0)) {
			case '+':
				m_direction = 1;
				text        = text.substring(1);
				break;
			case '-':
				m_direction = -1;
				text        = text.substring(1);
				break;
			}
			m_offset = Util.parseInt(text, -1);
		}
			
		setVisible(false);
		return;
	}	
}


class View extends JButton implements ActionListener, MouseListener
{
	protected Diagram	m_diagram;
	
	protected String	m_description;
	protected String	m_startEntity;
	protected Vector	m_entityClasses;	// Entity classes
	protected Vector	m_relationClasses;	// Relation classes
	protected Vector	m_showEntities;		// EntityInstance Ids
	protected Vector	m_redBoxEntities;	// EntityInstance Ids to Redbox
	protected Option	m_options;
	protected boolean	m_backgroundSet = false;

	// --------------
	// Object methods
	// --------------

	public String toString()
	{
		return ("View " + getText() + " " + getBounds());
	}
	
	public View()
	{
		addActionListener(this);
		addMouseListener(this);
	}
	
	public void setBackground(Color color)
	{
		super.setBackground(color);
		m_backgroundSet = true;
	}
	
	public Diagram getDiagram()
	{
		return m_diagram;
	}
	
	public void rename()
	{
		Diagram				diagram  = m_diagram;
		LandscapeEditorCore	ls       = diagram.getLs();
		ViewName			viewName = new ViewName(ls.getFrame(), this);	
		ViewBox				viewBox  = ls.getViewBox();
			
		viewName.dispose();
		viewBox.fill();		
	}
	
	public void move()
	{
		Diagram				diagram   = m_diagram;
		LandscapeEditorCore	ls        = diagram.getLs();
		ViewMove			viewMove  = new ViewMove(ls.getFrame(), this);
		int					direction = viewMove.getDirection();
		int					offset    = viewMove.getOffset();	
			
		viewMove.dispose();
		if (offset > 0) {
			diagram.moveView(this, direction, offset);
	}	}
	
	public void
	setDiagram(Diagram value)
	{
		m_diagram = value;
	}
	
	public void
	setStartEntity(String value)
	{
		m_startEntity = value;
	}
	
	public void
	setDescription(String value)
	{
		if (value != null) {
			setToolTipText(value);
		}
	}

	public void
	setShowEntities(boolean value)
	{
		Diagram			diagram = m_diagram;
		Vector			v;
		int				i, size;
		EntityInstance	e;
		String			id;
		
		m_showEntities   = null;
		m_redBoxEntities = null;
		if (value) {
			v = diagram.getGroupedEntities();
			
			if (v != null) {
				Vector showEntities;
				
				size           = v.size();
				m_showEntities = showEntities = new Vector(size);
				for (i = 0; i < size; ++i) {
					e  = (EntityInstance) v.elementAt(i);
					id = e.getId();
					showEntities.addElement(id);
		}	}	}	
		
		v = diagram.getRedboxEntities();
		if (v != null) {
			Vector redBoxEntities;
					
			size             = v.size();
			m_redBoxEntities = redBoxEntities = new Vector(size);
			for (i = 0; i < size; ++i) {
				e  = (EntityInstance) v.elementAt(i);
				id = e.getId();
				if (e.isMarked(EntityInstance.REDBOX_MARK)) {
					m_redBoxEntities.addElement(id);
			}	}
			if (redBoxEntities.size() == 0) {
				m_redBoxEntities = null;
		}	}
	}
		
	public void
	getSchemeSnapshot(Ta ta)
	{
		Enumeration		en;
		EntityClass		ec;
		RelationClass	rc;
		Vector			v;
		
		v = m_entityClasses;
		if (v == null) {
			m_entityClasses = v = new Vector();
		} else {
			v.clear();
		}
		
		for (en = ta.enumEntityClasses(); en.hasMoreElements(); ) {
			ec = (EntityClass) en.nextElement();
			v.add(ec.getView());
		}
		
		v = m_relationClasses;
		if (v == null) {
			m_relationClasses = v = new Vector();
		} else {
			v.clear();
		}
		
		for (en = ta.enumRelationClasses(); en.hasMoreElements(); ) {
			rc = (RelationClass) en.nextElement();
			v.add(rc.getView());
	}	}
	
	public void
	getSnapshot(Ta ta)
	{
		Option			options  = Options.getDiagramOptions();

		m_options     = new Option("View");
		m_options.setTo(options);
		m_diagram     = ta.getDiagram();
		m_startEntity = ta.getDrawRoot().getId();
		getSchemeSnapshot(ta);
	}
	
	public void
	setSnapshot()
	{
		Diagram			diagram = m_diagram;
		Option			options, oldOptions, newOptions;
		LandscapeEditorCore ls  = diagram.getLs();
		Enumeration		en;
		EntityClass		ec;
		RelationClass	rc;
		EntityInstance	e, e1;
		RelationInstance ri;
		Vector			v;
		int				i, size;
		boolean			ret = false;
		
		if (m_options != null) {
			options    = Options.getDiagramOptions();
			oldOptions = new Option("OptionDialog old");
			newOptions = m_options;

			oldOptions.setTo(options);
			oldOptions.setFontsTo(options);
			options.setTo(newOptions);
			options.setFontsTo(newOptions);
			options.optionsChanged(ls, oldOptions);
			ls.fontsChanged(oldOptions, options);
		}
			
		v = m_entityClasses;
		for (i = 0; i < v.size(); ++i) {
			ec = (EntityClass) v.elementAt(i);
			ec.setView();
		}
		v = m_relationClasses;
		for (i = 0; i < v.size(); ++i) {
			rc   = (RelationClass) v.elementAt(i);
			ret |= rc.setView();
		}
		if (ret) {
			RelationClass[]	spanningClasses = diagram.computedContainsClasses();
		
			if (spanningClasses != null) {
				// Contains classes have changed
				diagram.updateContainsClasses(spanningClasses);
		}	}
		
		e = null;
		if (m_startEntity != null) {
			e = diagram.getCache(m_startEntity);
		}
		if (e == null) {
			e = diagram.getDrawRoot();
		}
		
		diagram.invalidateVisibleRelationClasses();
		diagram.navigateTo(e, false);
		
		v = m_showEntities;
		if (v != null) {
			diagram.clearHighlighting(false);
			diagram.clearAllRedBoxFlags();

			size = v.size();
			for (i = 0; i < size; ++i) {
				e1 = diagram.getCache((String) v.elementAt(i));
				if (e1 != null) {
					diagram.setTracedFlag(e1);
			}	}
			diagram.setViewActive();
			diagram.clearDrawEntities();
		}
		v = m_redBoxEntities;
		if (v != null) {
			size = v.size();
			for (i = 0; i < size; ++i) {
				e1 = diagram.getCache((String) v.elementAt(i));
				if (e1 != null) {
					diagram.setRedBoxFlag(e1);
		}	}	}
		
		diagram.signalDiagramChanged(diagram, TaListener.VIEW_CHANGED);
	}
	
	private static String escape1(String str) 
	{
		StringBuilder buffer = new StringBuilder(str.length() + 120);

		char	c;
		int		i;

		for (i = 0; i < str.length(); ++i) {
			c = str.charAt(i);
			switch (c) {
			case '\t':
			case '\f':
			case '\u001b':
			case '\u007f':
			case '\\':
			case '"':
				buffer.append('\\');
				switch (c) {
				case '\t':
					c = 't';
					break;
				case '\f':
					c = 'f';
					break;
				case '\u001b':
					c = 'e';
					break;
				case '\u007f':
					c = 'd';
					break;
				}
				break;
			}
			buffer.append(c);
		}
		return buffer.toString();
	}
	
	protected static final String escapeChars  = "\t\f\u001b\u007f\\\"";				// u001b=ESC u007f=DEL
	
	// Allow \n in output.. otherwise lines get so long can't be editted using vi.
	
	private static String escape(String str) 
	{
		if (str != null && str.length() > 0) {
			for (int i = 0; i < escapeChars.length(); i++) {
				if (str.indexOf(escapeChars.charAt(i)) >= 0) {
					return escape1(str);
		}	}	}
		return str;
	}

	private static String unescape(String str)
	{
		if (0 <= str.indexOf('\\')) {
			StringBuilder buffer = new StringBuilder(str.length());
			
			char	c;
			int		i;

			for (i = 0; i < str.length(); ++i) {
				c    = str.charAt(i);
				if (c == '\\') {
					c = str.charAt(++i);
					if (c != '\\' && c != '"') {
						--i;
						c = '\\';
				}	}
				buffer.append(c);
			}
			return buffer.toString();
		}
		return str;
	}
		
	public void write(PrintWriter ps)
	{
		Diagram			diagram   = m_diagram;
		CharArrayWriter	charArray = null;
		PrintWriter		ps1       = null;
		String			viewname  = getText();
		Vector			v;
		Color			color;
		
		ps.println("");
		
		if (viewname != null) {
			ps.println(":NAME:");
			ps.println(escape(viewname));
		}
		if (m_description != null) {
			ps.println(":DESCRIPTION:");
			ps.println(escape(m_description));
		}
		if (m_startEntity != null) {
			ps.println(":STARTENTITY:");
			ps.println(escape(m_startEntity));
		}
		
		color      = getForeground();
		if (color != null) {
			ps.println(":FOREGROUND:");
			ps.println(Util.taColor(color));
		}
		if (m_backgroundSet) {
			color      = getBackground();
			if (color != null) {
				ps.println(":BACKGROUND:");
				ps.println(Util.taColor(color));
		}	}
	
		if (m_entityClasses != null) {
			charArray = new CharArrayWriter(20000);
			ps1       = new PrintWriter(charArray);
			
			diagram.writeSchemeTuples(ps1, m_entityClasses.elements(), m_relationClasses.elements(), m_relationClasses.elements());
			diagram.writeSchemeAttributes(ps1, m_entityClasses.elements(), m_relationClasses.elements());

			ps1.flush();
			ps.println(escape(charArray.toString()));
			ps.println("SCHEME END :");
		}
		v = m_showEntities;
		if (v != null) {
			String	id;
			int		i;
			
			ps.println(":SHOW:");
			for (i = 0; i < v.size(); ++i) {
				id = (String) v.elementAt(i);
				ps.println(escape(id));
		}	}
		v = m_redBoxEntities;
		if (v != null) {
			String	id;
			int		i;
			
			ps.println(":REDBOX:");
			for (i = 0; i < v.size(); ++i) {
				id = (String) v.elementAt(i);
				ps.println(escape(id));
		}	}
		if (m_options != null) {
			if (charArray == null) {
				charArray = new CharArrayWriter(10000);
				ps1       = new PrintWriter(charArray);
			} else {
				charArray.reset();
			}
			ps.println(":OPTIONS:");
			m_options.saveOptions(ps1, false /* Not to TA */);
			ps1.flush();
			ps.println(escape(charArray.toString()));
		}

		
		if (ps1 != null) {
			ps1.close();
		}
	}
	
	public void load(String attributeId, String image)
	{
		Diagram			diagram;
		int				i, last, state, newstate, start, newstart, eq;
		String			line, attribute, value;
		EntityInstance	e;
		
		m_description     = null;
		m_startEntity     = null;
		m_options         = null;
		m_showEntities    = null;
		
		diagram = m_diagram;
		
		if (m_entityClasses != null) {
			m_entityClasses.clear();
		}
		if (m_relationClasses != null) {
			m_relationClasses.clear();
		}
		image = unescape(image);
		last  = image.length() - 1;
		state = newstate = -1;
		
		start = newstart = 0;
		for (i = 0; ; ++i) {
			if (i >= last) {
				newstate = -2;
			} else {
				if (image.charAt(i) != '\n') {
					continue;
				}
				switch (image.charAt(i+1)) {
				case ':':
					if (image.substring(i, i+8).equals("\n:NAME:\n")) {
						newstart = i+8;
						newstate = 0;
						break;
					}
					if (image.substring(i, i+15).equals("\n:DESCRIPTION:\n")) {
						newstart = i+15;
						newstate = 1;
						break;
					}

					if (image.substring(i, i+15).equals("\n:STARTENTITY:\n")) {
						newstart   = i+15;
						newstate   = 2;
						break;
					}
					if (image.substring(i, i+14).equals("\n:FOREGROUND:\n")) {
						newstart   = i+14;
						newstate   = 3;
						break;
					}
					if (image.substring(i, i+14).equals("\n:BACKGROUND:\n")) {
						newstart   = i+14;
						newstate   = 4;
						break;
					}
					if (image.substring(i, i+8).equals("\n:SHOW:\n")) {
						newstart   = i+8;
						newstate   = 6;
						break;
					}
					if (image.substring(i, i+10).equals("\n:REDBOX:\n")) {
						newstart   = i+10;
						newstate   = 7;
						break;
					}
					if (image.substring(i, i+11).equals("\n:OPTIONS:\n")) {
						newstart   = i+11;
						newstate   = 8;
						m_options  = new Option("VIEW");
						break;
					}
					break;
				case 'S':
					if (image.substring(i, i+16).equals("\nSCHEME TUPLE :\n")) {
						newstart    = i;
						newstate    = 5;
						break;
			}	}	}
			
			if (state >= 0) {
				if (state < 6) {
					if (newstate != -1) {
						// At the end of something (take the whole multi-line chunk)
						value = image.substring(start, i);
						switch (state) {
						case 0:
							setText(value);
							break;
						case 1:
							m_description = value;
							break;
						case 2:
							m_startEntity = value;
							break;
						case 3:
							setForeground(Util.colorTa(value));
							break;
						case 4:
							setBackground(Util.colorTa(value));
							break;
						case 5:
							diagram.LoadSchemaForView(this, attributeId, value); 
							{
								Enumeration		en;
								RelationClass	rc;
				
								if (m_relationClasses != null) {
									for (en = m_relationClasses.elements(); en.hasMoreElements(); ) {
										rc = (RelationClass) en.nextElement();
							}	}	}
							break;
					}	}
				} else if (i > start) {
					// Take each line
					line      = image.substring(start, i);
					start     = i+1;
					switch (state) {
					case 6:
						if (m_showEntities == null) {
							m_showEntities = new Vector(20);
						}
						m_showEntities.addElement(line);
						break;
					case 7:
						if (m_redBoxEntities == null) {
							m_redBoxEntities = new Vector(20);
						}
						m_redBoxEntities.addElement(line);
						break;						
					case 8:
						eq           = line.indexOf('=');
						if (eq >= 0 && eq+1 < line.length()) {
							attribute = line.substring(0, eq);
							attribute = attribute.trim();
							value     = line.substring(eq + 1);
							value     = value.trim();
							m_options.loadOption(attribute, value);
						}
						break;
			}	}	}
			switch (newstate) {
			case -2:
				return;
			case -1:
				// In the middle of reading lines
				break;
			default:
				state    = newstate;
				start    = newstart;
				newstate = -1;
	}	}	}
	
	protected void doRightPopup(MouseEvent ev)
	{
		Diagram				diagram = m_diagram;
		LandscapeEditorCore ls      = diagram.getLs();
		int					x       = ev.getX();
		int					y       = ev.getY();
		JPopupMenu			popupMenu;
		JMenuItem			mi;

		popupMenu = new JPopupMenu("Options");

		mi = new JMenuItem("Rename");
		mi.addActionListener(new RenameView(this));
		popupMenu.add(mi);

		mi = new JMenuItem("Delete");
		mi.addActionListener(new DeleteView(this));
		popupMenu.add(mi);
		
		mi = new JMenuItem("Move");
		mi.addActionListener(new MoveView(this));
		popupMenu.add(mi);
		
		FontCache.setMenuTreeFont(popupMenu);
		add(popupMenu);
		popupMenu.show(this, x, y);
		//		Do.dump_menu(popupMenu);
		remove(popupMenu);
	}
	
	// ActionListener interface
	
	public void actionPerformed(ActionEvent ev)
	{
		setSnapshot();
	}
	
	// MouseListener interface

	public void mouseClicked(MouseEvent e)
	{
	}

	public void mouseEntered(MouseEvent e)
	{
	}

	public void mouseExited(MouseEvent e)
	{
	}

	public void mousePressed(MouseEvent ev)
	{
		if(ev.isMetaDown()) {
			doRightPopup(ev);
	}	}

	public void mouseReleased(MouseEvent ev)
	{
	}
}

