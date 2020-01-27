package lsedit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.event.TableModelListener;

// This is the class which maps to the data being presented/updated to lsedit variables

class UsageLabel extends JLabel implements TableCellRenderer {

	public UsageLabel(String label)
	{
		super(label);
	}

	public Component
	getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
	{
		return this;
	}
}

class UsageTableModel extends AbstractTableModel {
	
	int						m_rows;
	int						m_columns;
	UsageLabel[][]			m_labels;

	public UsageTableModel(int rows, int columns, UsageLabel[][] labels)
	{
		m_rows        = rows;
		m_columns     = columns;
		m_labels      = labels;
	}
		
	public int getRowCount()
	{
		return m_rows;
	}

	public void setRowCount(int rows)
	{
		m_rows = rows;
	}

	public int getColumnCount()
	{
		return m_columns;
	}

	public String getColumnName(int col)
	{
		return "";
	}

	public Object getValueAt(int row, int col)
	{
		return m_labels[row][col];
	}

	public boolean isCellEditable(int row, int col)
	{
		return(false);
	}

	public void setValueAt(Object value, int row, int col)
	{
	}
}

class UsageColumnModel extends DefaultTableColumnModel {

	TableColumn[]	m_columns;
	int				m_total_width;

	UsageColumnModel(FontMetrics fm, UsageLabel[] labels, LandscapeClassObject[] cs)
	{
		super();

		int			i, min, width;
		TableColumn	column;

		m_total_width = 0;
		m_columns = new TableColumn[labels.length];

		min = fm.stringWidth("999999");
		for (i = labels.length; --i > 0; ) {
			width = fm.stringWidth(labels[i].getText());
			if (width < min) {
				width = min;
			}
			width += 4;
			m_columns[i] = column = new TableColumn(i, width);
			column.setPreferredWidth(width);
			m_total_width += width;
		}

		width = 0;
		for (i = cs.length; --i >= 0; ) {
			min = fm.stringWidth(cs[i].getLabel());
			if (width < min) {
				width = min;
		}	}
		width += 4;
		m_columns[0] = column = new TableColumn(0, width);
		column.setPreferredWidth(width);
		m_total_width += width;
	}

	public TableColumn getColumn(int columnIndex) 
	{
		return m_columns[columnIndex];
	}
 
	public int getColumnCount() 
	{
		return m_columns.length;
	}

	public int getTotalColumnWidth() 
	{
		return m_total_width;
	}
};

// This is the class which decides how the table is drawn and edited

class UsageTable extends JTable implements TableModelListener {

	public UsageTable(AbstractTableModel tableModel)
	{
		super(tableModel);
		setTableHeader(null);
	}

	// Overload how cells are rendered

	public TableCellRenderer getCellRenderer(int row, int column)
	{
		UsageTableModel		model   = (UsageTableModel) getModel();
		UsageLabel			ret;

		ret = (UsageLabel) model.getValueAt(row, column);
		return(ret);
	}
}
		
public class ClassUsage extends JDialog implements ActionListener, ItemListener { 

	public static final String[] g_ec_directions =
		{ "All", "From", "To", "Loop", "Child", "Parent", "Span", "All Except Span","From Except Span", "To Except Span"};

	public static final String[] g_ec_arrows =
		{ "<-all->", "-from->", "<-to-", "-loop-", "=child=>", "<=parent=","<=span=>", "<-nospan->", "-nospan->", "<-nospan-" };


	public static final int	EC_DIRECTION_ALL  = 0;
	public static final int	EC_DIRECTION_FROM = 1;
	public static final int	EC_DIRECTION_TO   = 2;
	public static final int	EC_DIRECTION_LOOP = 3;
	public static final int EC_DIRECTION_CHILD = 4;
	public static final int EC_DIRECTION_PARENT = 5;
	public static final int EC_DIRECTION_SPAN = 6;
	public static final int EC_DIRECTION_ALL_EXCEPT_SPAN = 7;
	public static final int EC_DIRECTION_FROM_EXCEPT_SPAN = 8;
	public static final int EC_DIRECTION_TO_EXCEPT_SPAN = 9;

	public static final String[] g_rc_directions =
		{ "All", "Loop", "Span", "All Except Span", "Max", "Max From", "Max To", "Max To/From"};

	public static final String[] g_rc_arrows =
		{ "<-all->", "-loop-", "<=span=>", "<-nospan->", "<-max->", "-max from->", "<-max to-", "<-max to/from->" };

	public static final int RC_DIRECTION_ALL = 0;
	public static final int RC_DIRECTION_LOOP = 1;
	public static final int RC_DIRECTION_SPAN = 2;
	public static final int RC_DIRECTION_ALL_EXCEPT_SPAN = 3;
	public static final int RC_DIRECTION_MAX = 4;
	public static final int RC_DIRECTION_MAX_FROM = 5;
	public static final int RC_DIRECTION_MAX_TO = 6;
	public static final int RC_DIRECTION_MAX_FROM_TO = 7;

	private Font			m_font;
	private Font			m_bold;
	private	int				m_numEntityClasses;
	private	int				m_numRelationClasses;
	private LandscapeClassObject m_classObject;

	private EntityClass		m_ecs[];
	private RelationClass	m_rcs[];
	private LandscapeClassObject m_cs[];
	private	UsageLabel[][]	m_labels;
	private	int[][]			m_array     = null;
	private JButton			m_ok        = null;
	private JComboBox		m_comboBox  = null;
	private JComboBox		m_directionBox = null;
	private int				m_direction;

	private UsageTableModel	m_model;
	private UsageColumnModel m_column_model;
	private	UsageTable		m_table = null;
	private	Dimension		m_preferredSize;

	protected void
	fill(LandscapeClassObject classObject)
	{
		boolean			isEntityClass, isMax;
		int 			i, j, cnt, rows, sum, total, total1;
		UsageLabel[]	row;
		UsageLabel 		label;
		String			text, arrow;
		String[]		terms;
		


		isEntityClass = (classObject instanceof EntityClass);
		terms         = (isEntityClass ? g_ec_directions : g_rc_directions);
		if (classObject != m_classObject) {
			if ((m_classObject == null) || (isEntityClass ^ (m_classObject instanceof EntityClass))) {
				m_directionBox.removeAllItems();
				for (i = 0; i < terms.length; ++i) {
					m_directionBox.addItem(terms[i]);
				}
				m_directionBox.setSelectedIndex(0);
		}	}

		m_classObject = classObject;
		m_direction = m_directionBox.getSelectedIndex();
		if (m_direction < 0) {
			m_direction = 0;
		}


		isMax = false;
		if (isEntityClass) {
			setTitle("Usage for Entity Class " + classObject.getLabel() + " " + terms[m_direction]);
			((EntityClass) classObject).getUsage(m_array, m_direction);
			rows = m_rcs.length;
			for (i = 0;  i < rows; ++i) {
				label = m_labels[i+1][0];
				label.setForeground(Color.RED);
				label.setText(m_rcs[i].getLabel());
			}
		} else {
			setTitle("Usage for Relation Class " + classObject.getLabel() + " " + terms[m_direction]);
			switch (m_direction) {
			case RC_DIRECTION_MAX:
			case RC_DIRECTION_MAX_FROM:
			case RC_DIRECTION_MAX_TO:
			case RC_DIRECTION_MAX_FROM_TO:
				isMax = true;
			}
			((RelationClass) classObject).getUsage(m_array, m_direction);
			rows = m_ecs.length;
			for (i = 0; i < rows; ++i) {
				label = m_labels[i+1][0];
				label.setForeground(Color.BLACK);
				label.setText(m_ecs[i].getLabel());
			}
		}

		total = 0;
		for (i = 0; i < rows; ++i) {
			row = m_labels[i+1];
			sum = 0;
			for (j = 0; j < m_ecs.length; ++j) {
				label = row[j+1];
				cnt   = m_array[i][j];
				if (cnt == 0) {
					text = "-";
				} else {
					if (isMax) {
						if (sum < cnt) {
							sum = cnt;
						}
					} else {
						sum += cnt;
					}
					text = "" + cnt;
				}
				label.setText(text);
				if (isEntityClass) {
					if (i == j) {
						label.setOpaque(false);
					}
					arrow = g_ec_arrows[m_direction];
					text = classObject.getLabel() + arrow + m_rcs[i].getLabel() + arrow + m_ecs[j].getLabel();
				} else {
					if (i == j) {
						label.setOpaque(true);
					}
					arrow = g_rc_arrows[m_direction];
					text = m_ecs[i].getLabel() + arrow + classObject.getLabel() + arrow + m_ecs[j].getLabel();
				}
				label.setToolTipText(text);
			}
			label = row[j+1];
			label.setText("" + sum);
			if (isMax) {
				if (total < sum) {
					total = sum;
				}
			} else {
				total += sum;
			}
		}

		total1 = 0;
		row = m_labels[rows+1];
		for (j = 0; j < m_ecs.length; ++j) {
			sum = 0;
			for (i = 0; i < rows; ++i) {
				cnt = m_array[i][j];
				if (isMax) {
					if (sum < cnt) {
						sum = cnt;
					}
				} else {
					sum += cnt;
			}	}
			label = row[j+1];
			label.setText("" + sum);
			if (isMax) {
				if (total1 < sum) {
					total1 = sum;
				}
			} else {
				total1 += sum;
		}	}

		if (isMax) {
			label = m_labels[0][m_ecs.length+1];
			label.setText("*MAX*");
			label = m_labels[rows+1][0];
			label.setText("*MAX*");
		} else {
			label = m_labels[0][m_ecs.length+1];
			label.setText("*TOTAL*");
			label = m_labels[rows+1][0];
			label.setText("*TOTAL*");
		}

		label = m_labels[rows+1][m_ecs.length+1];
		if (total == total1) {
			label.setText("" + total);
		} else {
			label.setText("" + total1 + "/" + total);
		}

		if (m_table == null) {

			FontMetrics fm;
			int			rowHeight;
			int			preferredWidth;
			int			preferredHeight;

			fm = getFontMetrics(m_bold);

			m_table = new UsageTable(m_model);
			m_table.setFont(m_font);
			rowHeight = fm.getHeight() + 4;
			m_table.setRowHeight(rowHeight);
			rowHeight += 1;
			m_column_model = new UsageColumnModel(fm, m_labels[0], m_cs);
			m_table.setColumnModel(m_column_model);
			preferredWidth  = m_column_model.getTotalColumnWidth();
			preferredHeight = (rows+2)*rowHeight;
			m_preferredSize = new Dimension(preferredWidth, preferredHeight);

			m_table.setPreferredSize(m_preferredSize);
		}

		m_model.setRowCount(rows+2);
		m_model.fireTableDataChanged();
		m_table.doLayout();
	}

	protected ClassUsage(JFrame frame, Diagram diagram, LandscapeClassObject classObject)
	{
		super(frame, "Usage for " + classObject.getLabel(), true); //false if non-modal

		Container		contentPane;
		JScrollPane		scrollPane;
		JPanel			panel;
		UsageLabel[]	row;
		UsageLabel		label;
		Color			diagonal = new Color(222,222,197);

		Enumeration	en;
		EntityClass	ec;
		RelationClass rc;
		int			i, j, size, selected;

		m_font  = FontCache.getDialogFont();
		m_bold  = m_font.deriveFont(Font.BOLD);

		m_numEntityClasses   = diagram.numEntityClasses();
		m_numRelationClasses = diagram.numRelationClasses();

		m_ecs                = new EntityClass[m_numEntityClasses];
		m_rcs                = new RelationClass[m_numRelationClasses];
		m_cs                 = new LandscapeClassObject[m_numEntityClasses + m_numRelationClasses];

		i        = 0;
		selected = 0;
		for (en = diagram.enumEntityClassesInOrder(); en.hasMoreElements(); ++i) {
			ec = (EntityClass) en.nextElement();
			if (ec == classObject) {
				selected = i;
			}
			ec.setOrderedId(i);
			m_cs[i]  = ec;
			m_ecs[i] = ec;
		}

		j = 0;
		for (en = diagram.enumRelationClassesInOrder(); en.hasMoreElements(); ++i) {
			rc = (RelationClass) en.nextElement();
			if (rc == classObject) {
				selected = i;
			}
			rc.setOrderedId(j);
			m_cs[i]  = rc;
			m_rcs[j] = rc;
			++j;
		}

		size = m_numEntityClasses;
		if (size < m_numRelationClasses) {
			size = m_numRelationClasses;
		}

		m_array  = new int[size+1][];
		m_labels = new UsageLabel[size+2][];
		for (i = 0; i <= size+1; ++i) {
			if (i <= size) {
				m_array[i] = new int[m_numEntityClasses+1];
			}
			m_labels[i] = row = new UsageLabel[m_numEntityClasses+2];
			for (j = 0; j <= m_numEntityClasses+1; ++j) {
				if (i == 0) {
					if (j == 0) {
						label = new UsageLabel("");
					} else {
						if (j <= m_numEntityClasses) {
							label = new UsageLabel(m_ecs[j-1].getLabel());
						} else {
							label = new UsageLabel("*All*");
						}
						label.setFont(m_bold);
						label.setForeground(Color.BLACK);
					
					}
				}  else {
					label = new UsageLabel("");
					if (j > 0) {
						if (i == j) {
							label.setBackground(diagonal);
						}
						label.setForeground(Color.BLUE);
						label.setFont(m_font);
				}	}
				label.setHorizontalAlignment(JLabel.CENTER);
				row[j] = label;
		}	}

		contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		setForeground(ColorCache.get(0,0,0));
		setBackground(ColorCache.get(192,192,192));
		setFont(m_font);

		m_model = new UsageTableModel(0, m_numEntityClasses + 2, m_labels);

		m_directionBox = new JComboBox();
		m_directionBox.addItemListener(this);

		m_classObject = null;
		fill(classObject);

		scrollPane = new JScrollPane(m_table);
		scrollPane.setPreferredSize(m_preferredSize);

		scrollPane.setVisible(true);
		contentPane.add(scrollPane, BorderLayout.CENTER);

		panel = new JPanel();
		panel.setLayout(new FlowLayout());

		m_comboBox = new JComboBox(m_cs);
		m_comboBox.setSelectedIndex(selected);
		m_comboBox.addItemListener(this);
		panel.add(m_comboBox);


		panel.add(m_directionBox);

		m_ok = new JButton("Close");
		m_ok.setFont(m_bold);
		m_ok.addActionListener(this);
		panel.add(m_ok);

		contentPane.add(panel, BorderLayout.SOUTH);

		pack();
		setVisible(true);
	}

	public static void create(Diagram diagram, LandscapeClassObject classObject) 
	{
		LandscapeEditorCore ls = diagram.getLs();

		ClassUsage classUsage = new ClassUsage(ls.getFrame(), diagram, classObject);
		classUsage.dispose();
	}

	// ItemListener interface

	public void itemStateChanged(ItemEvent ev)
	{
		Object					source;
		LandscapeClassObject	classObject;
		int						direction;

		source = ev.getSource();
		if (source == m_comboBox) {
			classObject = (LandscapeClassObject) m_comboBox.getSelectedItem();
			if (classObject != m_classObject) {
				fill(classObject);
			}
			return;
		}
		if (source == m_directionBox) {
			direction = m_directionBox.getSelectedIndex();
			if (direction != m_direction) {
				fill(m_classObject);
			}
			return;
		}
	}

	// ActionListener interface

	public void actionPerformed(ActionEvent ev)
	{
		Object	source;

		// Pop down the window when the button is clicked.
		// System.out.println("event: " + ev);

		source = ev.getSource();

		if (source != m_ok) {
			return;
		}
		this.setVisible(false);
	}
}



