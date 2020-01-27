package lsedit;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;

class ReclusterConfigure extends JDialog implements ActionListener {

	static protected final int BUTTON_OK      = 0;
	static protected final int BUTTON_CANCEL  = 1;
	static protected final int BUTTON_HELP    = 2;

	protected final static String[] m_parameters =
	{
		"Scope of redistribution",
		"Type of edge to consider",
		"Bias towards moving"
	};

	protected final static String[] g_button_titles =
							{
								"Ok",
								"Cancel",
								"Help"
							};

	protected final static String[] g_edge_modes =
							{
								"All",
								"Incoming",
								"Outgoing"
							};

	protected final static String[] g_shift =
							{
								"Don't move if equal",
								"Move if equal",
								"Move if possible"
							};


	protected LandscapeEditorCore	m_ls;
	protected EntityInstance		m_e;
	protected JComboBox				m_scope;
	protected JComboBox				m_edge_mode;
	protected JComboBox				m_shift;
	protected JButton[]				m_buttons;
	protected boolean				m_ret;
	
	public ReclusterConfigure(LandscapeEditorCore ls, EntityInstance e)
	{
		super(ls.getFrame(), "ReCluster Configuration", true);

		Container			contentPane;
		Font				font, bold;
		JLabel				label;
		int					i;
		String				string;
		JButton				button;

		m_ls		 = ls;
		m_e          = e;
		m_ret        = false;
		font         = FontCache.getDialogFont();
		bold         = font.deriveFont(Font.BOLD);

		setForeground(ColorCache.get(0,0,0));
		setBackground(ColorCache.get(192,192,192));
		setFont(font);

		JPanel labelPanel = new JPanel();
		JPanel valuePanel = new JPanel();
		GridLayout gridLayout;

		gridLayout = new GridLayout(m_parameters.length, 1, 0, 10);
		labelPanel.setLayout(gridLayout);
		gridLayout = new GridLayout(m_parameters.length, 1, 0, 10);
		valuePanel.setLayout(gridLayout);

		for (i = 0; i < m_parameters.length; ++i) {
			label = new JLabel(m_parameters[i], JLabel.RIGHT);
			label.setFont(bold);
			labelPanel.add(label);
		}

		EntityInstance parent = e;

		m_scope = new JComboBox();
		while ((parent = parent.getContainedBy()) != null) {
			m_scope.addItem(parent.getFullEntityLabel());
		}
		m_scope.setFont(bold);
		valuePanel.add(m_scope);

		m_edge_mode = new JComboBox(g_edge_modes);
		valuePanel.add(m_edge_mode);

		m_shift = new JComboBox(g_shift);
		valuePanel.add(m_shift);

		JPanel topPanel   = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.add(BorderLayout.WEST, labelPanel);
		topPanel.add(BorderLayout.EAST, valuePanel);

		contentPane = getContentPane();
		contentPane.add( BorderLayout.NORTH, topPanel);

		// --------------
		// Use a FlowLayout to center the button and give it margins.

		JPanel bottomPanel = new JPanel();

		bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));

		m_buttons = new JButton[g_button_titles.length];
		for (i = 0; i < g_button_titles.length; ++i) {
			string = g_button_titles[i];
			m_buttons[i] = button = new JButton(string);
			button.setFont(bold);
			button.addActionListener(this);
			bottomPanel.add(button);
		}

		contentPane.add( BorderLayout.SOUTH, bottomPanel);

		// Resize the window to the preferred size of its components
		pack();
		setVisible(true);
	}

	public boolean ok()
	{
		return m_ret;
	}

	public EntityInstance scope()
	{
		int				index  = m_scope.getSelectedIndex();
		EntityInstance	parent = m_e;

		if (index >= 0) {
			while ((parent = parent.getContainedBy()) != null) {
				if (index == 0) {
					return parent;
				}
				--index;
		}	}
		return null;
	}

	public int	edgemode()
	{
		return m_edge_mode.getSelectedIndex();
	}

	public int shift()
	{
		return m_shift.getSelectedIndex();
	}

	// ActionListener interface

	public void actionPerformed(ActionEvent ev)
	{
		Object			source;
		int				state, i;

		source = ev.getSource();

		state = -1;
		for (i = 0; i < g_button_titles.length; ++i) {
			if (source == m_buttons[i]) {
				state = i;
				break;
		}	}

		switch (state) {
		case BUTTON_HELP:
			JOptionPane.showMessageDialog(m_ls.getFrame(), 
			  "Redistribution selectively moves nodes under the selected nodes\n" +
			  "and/or those nodes associated with selected edges to other target\n" +
			  "parents, in a manner that maximizes internal edges.\n" +
			  "This operation is constrained by:\n" +
			  "1) The target parent must be within the specified scope.\n" +
			  "2) Considering only edges of the specified type.\n" +
			  "3) If the total number of internal edges remains unchanged by\n" +
			  "   moving nodes, nodes are move only if the bias is towards\n" +
			  "   moving.  However nodes are always moved if possible if the\n" +
			  "   option 'always move' is selected." 
				  , "Help", JOptionPane.DEFAULT_OPTION);
			return;
		case BUTTON_OK:
			m_ret = true;
		case BUTTON_CANCEL:
			break;
		default:
			return;
		}

		setVisible(false);
		return;
	}
}

class HashEntry {
	public EntityInstance	m_parent;
	public int				m_cnt;

	public HashEntry(EntityInstance parent)
	{
		m_parent = parent;
		m_cnt    = 1;
	}

	public void increment()
	{
		++m_cnt;
	}
}

public class Recluster {

	public static String layout(Diagram diagram)
	{
		LandscapeEditorCore ls;
		EntityInstance	e, e1, scope, lastparent;
		Enumeration		en;
		int				i;
		Vector			v;
		int				shift;
		boolean			incoming, outgoing;
		
		
		v = diagram.getReclusterGroup();
		if (v == null) {
		  Util.beep();
		  return "No group selected";
		}
		ls = diagram.getLs();

		ReclusterConfigure configure = new ReclusterConfigure(ls, (EntityInstance) v.elementAt(0));
	
		if (!configure.ok()) {
			return "Action cancelled";
		}
		scope = configure.scope();
		if (scope == null) {
			return "Scope not specified";
		}
		switch (configure.edgemode()) {
		case 1:
			incoming = true;
			outgoing = false;
			break;
		case 2:
			incoming = false;
			outgoing = true;
			break;
		default:
			incoming = true;
			outgoing = true;
		}

		for (i = 0; i < v.size(); ++i) {
			e = (EntityInstance) v.elementAt(i);
			for (en = e.getChildrenShown(); en.hasMoreElements(); ) {
				e1 = (EntityInstance) en.nextElement();
				v.addElement(e1);
		}	} 

		shift = configure.shift();

		EntityInstance		parent, old;
		Hashtable			hashtable;
		RelationInstance	ri;
		HashEntry			entry, best;
		boolean				ret;
		String				id;
		int					iteration, size, moves;
		ResultBox			resultBox = ls.getResultBox();

		resultBox.clear();
		resultBox.activate();
		resultBox.setResultTitle("Moved nodes");

		hashtable = new Hashtable();

		moves      = 0;
		lastparent = null;
		size  = v.size();
		for (iteration = 0; iteration < 10; ++iteration) {
			ret = false;

			for (i = 0; i < size; ++i) {
				e      = (EntityInstance) v.elementAt(i);
				old    = e.getContainedBy();
				hashtable.clear();

				if (outgoing) {
					en = e.srcRelationElements();
					if (en != null) {
						while (en.hasMoreElements()) {
							ri = (RelationInstance) en.nextElement();
							if (ri.isRelationShown()) {
								e1       = ri.getDst();
								if (e1 != e && scope.hasDescendant(e1)) {
									parent = e1.getContainedBy();

									if (shift == 2 && parent == old) {
										continue;
									}
									id     = parent.getId();
									entry  = (HashEntry) hashtable.get(id);
									if (entry == null) {
										entry = new HashEntry(parent);
										hashtable.put(id, entry);
									} else {
										entry.increment();
				}	}	}	}	}	}

				if (incoming) {
					en = e.dstRelationElements();
					if (en != null) {
						while (en.hasMoreElements()) {
							ri = (RelationInstance) en.nextElement();
							if (ri.isRelationShown()) {
								e1       = ri.getSrc();
								if (e1 != e && scope.hasDescendant(e1)) {
									parent = e1.getContainedBy();

									if (shift == 2 && parent == old) {
										continue;
									}
									id     = parent.getId();
									entry  = (HashEntry) hashtable.get(id);
									if (entry == null) {
										entry = new HashEntry(parent);
										hashtable.put(id, entry);
									} else {
										entry.increment();
				}	}	}	}	}	}

				if (hashtable.isEmpty()) {
					continue;
				}
				best   = null;
				for (en = hashtable.elements(); en.hasMoreElements(); ) {
					entry = (HashEntry) en.nextElement();
					if (best != null) {
						if (entry.m_cnt < best.m_cnt) {
							continue;
						}
						if (entry.m_cnt == best.m_cnt) {
							if (shift == 0) {
								if (entry.m_parent != old) {
									continue;
								}
							} else {
								if (entry.m_parent == old) {
									continue;
							}	}
					}	}
					best = entry;
				}
				if (best.m_parent == old) {
					continue;
				}
				diagram.updateMoveEntityContainment(best.m_parent, e);
				if (old != lastparent) {
					resultBox.addResultEntity(old, resultBox.getLeftShowAncestors());
					lastparent = old;
				}
				resultBox.addRelation("  ", e.getContainedByRelation());
				++moves;
				ret = true;
			}
			if (ret == false) {
				break;
		}	}
		resultBox.done("-- End of redistribution --");
		hashtable.clear();
		return("" + moves + " nodes moved within " + scope.getFullEntityLabel() + " in " + iteration + " iterations");
	}
}