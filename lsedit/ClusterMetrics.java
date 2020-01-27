package lsedit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

class MetricsEntry
{
	public EntityInstance	m_drawRoot;
	public int				m_internal_edges;
	public int				m_external_edges;
	public int				m_boxes_seen;
	public int				m_leaf_nodes;
	public int				m_max_children;
	public int				m_min_children;
	public int				m_total_children;
	public int				m_max_depth;

	public MetricsEntry(EntityInstance drawRoot)
	{
		m_drawRoot       = drawRoot;
		m_internal_edges = 0;
		m_external_edges = 0;
		m_boxes_seen     = 0;
		m_leaf_nodes     = 0;
		m_max_children   = 0;
		m_min_children   = 0;
		m_total_children = 0;
		m_max_depth      = 0;
	}

	public boolean sameEntry(MetricsEntry last, int i)
	{
		boolean ret;

		switch (i) {
		case 0:
			ret = (last.m_drawRoot == m_drawRoot);
			break;
		case 1:
			ret = (last.m_internal_edges == m_internal_edges);
			break;
		case 2:
			ret = (last.m_external_edges == m_external_edges);
			break;
		case 3:
			ret = (last.m_min_children   == m_min_children);
			break;
		case 4:
			ret = (last.m_max_children   == m_max_children);
			break;
		case 5:
			ret = ((last.m_boxes_seen    == m_boxes_seen) && (last.m_total_children == m_total_children));
			break;
		case 6:
			ret = (last.m_leaf_nodes     == m_leaf_nodes);
			break;
		case 7:
			ret = ((last.m_leaf_nodes + last.m_boxes_seen) == (m_leaf_nodes + m_boxes_seen));
			break;
		case 8:
			ret = (last.m_max_depth      == m_max_depth);
			break;
		default:
			ret = true;
		}
		return(ret);
	}

	public boolean same(MetricsEntry last)
	{
		if (last != null) {
			int	i;

			for (i = 0; i < 9; ++i) {
				if (!sameEntry(last, i)) {
					return false;
		}	}	}
		return true;
	}

	public String text(int i)
	{
		String s;

		switch (i) {
		case 0:
			s = m_drawRoot.getFullEntityLabel() + " ";
			break;
		case 1:
			s = "" + m_internal_edges;
			break;
		case 2:
			s = "" + m_external_edges;
			break;
		case 3:
			s = "" + m_min_children;
			break;
		case 4:
			s = "" + m_max_children;
			break;
		case 5:
			if (m_boxes_seen == 0) {
				s = "N/A";
			} else {
				NumberFormat format = NumberFormat.getInstance();
				format.setMaximumFractionDigits(2);

				s = format.format((double) ((double) m_total_children) / ((double) m_boxes_seen));
			}
			break;
		case 6:
			s = "" + m_leaf_nodes;
			break;
		case 7:
			s = "" + (m_leaf_nodes + m_boxes_seen + 1);
			break;
		case 8:
			s = "" + m_max_depth;
			break;
		default:
			s = "????";
		}
		return(s);
	}
}

public class ClusterMetrics extends JDialog implements ActionListener {

	protected static final String[]	m_titles = new String[]
		{
			"Rooted at",
			"Internal edges",
			"External edges",
			"Minimum children",
			"Maximum children",
			"Average children",
			"Leaf nodes",
			"Total entities",
			"Maximum depth"
		};

	protected Font				m_font;
	protected Font				m_bold;

	protected JPanel			m_left;
	protected JPanel			m_center;
	protected JPanel			m_right;

	protected JButton			m_save;
	protected JButton			m_ok;

	protected MetricsEntry		m_last;
	protected MetricsEntry		m_current;
		
	public ClusterMetrics(LandscapeEditorCore ls) 	
	{
		super(ls.getFrame(), "Cluster Metrics", true);

		JFrame			frame;
		Container		contentPane;
		JPanel			panel;
		Font			font, bold;
		JLabel			label;
		int				i;

		m_font = font = FontCache.getDialogFont();
		m_bold = bold = font.deriveFont(Font.BOLD);
		
		frame = ls.getFrame();
		setLocation(frame.getX()+200, frame.getY()+300);
		setForeground(ColorCache.get(0,0,0));
		setBackground(ColorCache.get(192,192,192));
		setFont(font);

		contentPane = getContentPane();
		m_left      = new JPanel();
		m_left.setLayout(new GridLayout(m_titles.length,1));

		for (i = 0; i < m_titles.length; ++i) {
			label = new JLabel(m_titles[i] + ": ", JLabel.RIGHT);
			label.setFont(m_font);
			m_left.add(label);
		}

		m_center    = new JPanel();
		m_center.setLayout(new GridLayout(m_titles.length,1));
		m_right     = new JPanel();
		m_right.setLayout(new GridLayout(m_titles.length,1));

		contentPane.add(m_left,   BorderLayout.WEST);
		contentPane.add(m_center, BorderLayout.CENTER);
		contentPane.add(m_right,  BorderLayout.EAST);
		 
		panel = new JPanel();
		panel.setLayout(new FlowLayout());

		m_save = new JButton("Save");
		m_save.setFont(bold);
		panel.add(m_save);
		m_save.addActionListener(this);

		m_ok = new JButton("Ok");
		m_ok.setFont(bold);
		panel.add(m_ok);
		m_ok.addActionListener(this);

		contentPane.add(panel, BorderLayout.SOUTH);
		m_last    = null;
		m_current = null;
	}

	public void init(EntityInstance drawRoot) 	
	{
		m_current = new MetricsEntry(drawRoot);
	}

	public void seenRelation(RelationInstance ri)
	{
		if (!ri.isMarked(RelationInstance.SPANNING_MARK)) {
			EntityInstance	src = ri.getSrc();
			EntityInstance	dst = ri.getDst();

			if (src.getContainedBy() == dst.getContainedBy()) {
				++m_current.m_internal_edges;
			} else {
				++m_current.m_external_edges;
	}	}	}

	public void seenEntity(EntityInstance e, int depth)
	{
		int	children = e.numChildren();

		if (depth > m_current.m_max_depth) {
			m_current.m_max_depth = depth;
		}
		if (children == 0) {
			++m_current.m_leaf_nodes;
		} else {
			++m_current.m_boxes_seen;
			m_current.m_total_children += children;

			if (m_current.m_min_children == 0) {
				m_current.m_min_children = children;
			} else if (m_current.m_min_children > children) {
				m_current.m_min_children = children;
			}
			if (m_current.m_max_children < children) {
				m_current.m_max_children = children;
		}	}
	}	

	public void showit()
	{
		int			i;
		String		s;
		JLabel		label;
		Color		color;

		m_right.removeAll();
		for (i = 0; i < m_titles.length; ++i) {
			s = m_current.text(i);
			label = new JLabel(s);
			label.setFont(m_bold);
			m_right.add(label);
		}

		if (m_current.same(m_last)) {
			m_center.setVisible(false);
		} else {
			m_center.removeAll();
			for (i = 0; i < m_titles.length; ++i) {
				s = m_last.text(i);
				label = new JLabel(s);
				label.setFont(m_font);
				if (m_current.sameEntry(m_last, i)) {
					color = Color.BLACK;
				} else {
					color = Color.RED;
				}
				label.setForeground(color);
				m_center.add(label);
			}
			m_center.setVisible(true);
		}

		// Resize the window to the preferred size of its components

		this.pack();
		setVisible(true);
	}

	// ActionListener interface

	public void actionPerformed(ActionEvent ev)
	{
		Object	source;

		// Pop down the window when the button is clicked.
		// System.out.println("event: " + ev);

		source = ev.getSource();

		if (source == m_save) {
			m_last = m_current;
		} else if (source == m_ok) {
			this.setVisible(false);
		}
		return;
	}
}

