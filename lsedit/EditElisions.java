package lsedit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

class ElisionTableModel extends AbstractTableModel {
	
	Diagram					m_diagram;
	Vector					m_entityClasses;
	Vector					m_relationClasses;
	SelectedElisions[][]	m_array;
	int						m_rows;
	int						m_columns;

	public ElisionTableModel(Diagram diagram, Object object)
	{
		LandscapeEditorCore		ls    = diagram.getLs();
		Enumeration				en;
		EntityClass				ec;
		EntityInstance			e     = null;
		RelationClass			rc;
		Vector					entityClasses;
		int						entityClassesSize;
		Vector					relationClasses;
		int						relationClassesSize;
		int						rows, columns;
		SelectedElisions[][]	array;
		SelectedElisions[]		row;
		int						i, j;
		int						all_row, all_col, elisions;

		m_diagram         = diagram;

		m_entityClasses   = entityClasses = new Vector();

		i  = 0;
		if (object instanceof EntityInstance) {
			e  = (EntityInstance) object;
			ec = ((EntityInstance) object).getEntityClass();
			ec.setOrderedId(0);
			entityClasses.addElement(ec);
			rows = 1;
		} else if (object instanceof EntityClass) { 
			ec = (EntityClass) object;
			ec.setOrderedId(0);
			entityClasses.addElement(ec);
			rows = 1;
		} else {
			for (en = diagram.enumEntityClassesInOrder(); en.hasMoreElements(); ++i) {
				ec = (EntityClass) en.nextElement();
				ec.setOrderedId(i);
				entityClasses.addElement(ec);
			}
			rows = entityClasses.size() + 1;
		}

		if (object instanceof RelationClass) {
			relationClasses = new Vector(1);
			relationClasses.addElement(object);
			columns         = 2;
		} else {
			relationClasses = diagram.getRelationClasses();
			columns         = relationClasses.size() + 2;
		}
		m_relationClasses = relationClasses;
		m_rows            = rows;
		m_columns         = columns;
		
		--columns;	// We have an additional first column
			
		m_array = array   = new SelectedElisions[rows][];

		row = null;
		entityClassesSize   = entityClasses.size();
		relationClassesSize = relationClasses.size();
		for (i = 0; i < rows; ++i) {
			if (i < entityClassesSize) {
				all_col = 0;
			} else {
				all_col = SelectedElisions.ALL_COL;
			} 
			array[i] = row = new SelectedElisions[columns];
			for (j = 0; j < columns; ++j) {
				if (j < relationClassesSize) {
					all_row = 0;
					rc      = (RelationClass) m_relationClasses.elementAt(j);
				} else {
					all_row = SelectedElisions.ALL_ROW;
					rc      = null;
				}
				if (e != null && all_row == 0 && all_col == 0) {
					elisions = e.getElisions(rc);
				} else {
					elisions = all_row | all_col;
				}
				row[j] = new SelectedElisions(elisions);
			}
		}
	}
	
	public Vector getEntityClasses()
	{
		return m_entityClasses;
	}
	
	public int getEntityClassesCount()
	{
		return m_entityClasses.size();
	}
	
	public Vector getRelationClasses()
	{
		return m_relationClasses;
	}
	
	public int getRelationClassesCount()
	{
		return m_relationClasses.size();
	}

	public int getRowCount()
	{
		return m_rows;
	}
	
	public int getColumnCount()
	{
		return m_columns;
	}

	public Class getColumnClass(int column) 
	{
		return getValueAt(0, column).getClass();
	}

	public String getColumnName(int col)
	{
		if (col == 0) {
			return("");
		} else if (col > m_relationClasses.size()) {
			return "*ALL*";
		} else {
			Object object = m_relationClasses.elementAt(col-1);
			return ((RelationClass) object).getLabel();
		}
	}

	public boolean isCellEditable(int row, int col)
	{
		return (col != 0);
	}

	public Object getValueAt(int row, int col)
	{
		if (col == 0) {
			Vector		entityClasses = m_entityClasses;

			if (row < entityClasses.size()) {
				return entityClasses.elementAt(row);
			} 
			return "*ALL*";
		}	
		return m_array[row][col-1];
	}

	public void updateElisions(int row, int column, int elisions, int mode)
	{
		SelectedElisions selected = m_array[row][column];

		selected.updateElisions(elisions, mode);
//		System.out.println("Firing " + row + "x" + column + " " + elisions + " " + mode);
		fireTableCellUpdated(row, column);
	}

	public void setValueAt(Object value, int row, int col)
	{
		int elisions = ((SelectedElisions) value).getElisions();
		int	scope    = 0;

		if ((elisions & SelectedElisions.ALL_COL) != 0) {
			scope |= 1;
		}
		if ((elisions & SelectedElisions.ALL_ROW) != 0) {
			scope |= 2;
		}		
		if (scope != 0) {
			int	entitiesSize  = m_entityClasses.size();
			int	relationsSize = m_relationClasses.size();
			int	i, j, mode;
			
			mode      = elisions & SelectedElisions.MODE;
			elisions &= ~SelectedElisions.MODE;

			switch (scope) {
			case 1:
				--col;
				for (i = 0; i < entitiesSize; ++i) {
					updateElisions(i, col, elisions, mode);
				}
				break;
			case 2:
				for (j = 0; j < relationsSize; ++j) {
					updateElisions(row, j, elisions, mode);
				}
				break;
			case 3:
				for (i = 0; i < entitiesSize; ++i) {
					for (j = 0; j < relationsSize; ++j) {
						updateElisions(i, j, elisions, mode);
				}	}
				break;
	}	}	}

	public SelectedElisions[][] getArray()
	{
		return m_array;
	}
}

/*
 * This interface returns a button which is placed where the old value used to be and when fired brings up the
 * actual editor to change the old value.
 */

class ElisionEditor extends DefaultCellEditor implements ActionListener 
{
	JFrame				m_frame;
	JTable				m_table;
	AbstractTableModel	m_tableModel;
	SelectedElisions	m_selectedElisions;
	JButton				m_button;
	int					m_row;
	int					m_column;

	public ElisionEditor(JFrame frame, AbstractTableModel tableModel) 
	{
		super(new JCheckBox()); //Unfortunately, the constructor expects a check box, combo box, or text field.

		m_frame      = frame;
		m_tableModel = tableModel;

		//First, set up the button that brings up the dialog.

		m_button =  new JButton("EDITING");
		m_button.setBackground(Color.white);
		m_button.setBorderPainted(false);
		m_button.setMargin(new Insets(0,0,0,0));

		editorComponent = m_button;
		setClickCountToStart(1); //This is usually 1 or 2.

		//Here's the code that brings up the dialog.
		m_button.addActionListener(this);
	}

	public Object getCellEditorValue() 
	{
		return m_selectedElisions;
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) 
	{
		m_table            = table;
		m_selectedElisions = (SelectedElisions) value;
		m_row              = row;
		m_column           = column;
		return editorComponent;
	}

	// Action listener interface

	public void actionPerformed(ActionEvent e) 
	{
		SelectedElisions selectedElisions = m_selectedElisions;
		ElisionChooser	 elisionChooser   = new ElisionChooser(m_frame, selectedElisions.getElisions());
		int				 elisions         = elisionChooser.getElisions();

		if (elisions >= 0) {
			selectedElisions.setElisions(elisions);
			m_tableModel.setValueAt(selectedElisions, m_row, m_table.convertColumnIndexToModel(m_column)); 
		}	

		fireEditingStopped(); 
		elisionChooser.dispose();
	}
}


// This is the class which decides how the table is drawn and edited

class ElisionTable extends JTable {

	private ElisionEditor m_elisionEditor;

	public ElisionTable(JFrame frame, AbstractTableModel tableModel)
	{
		super(tableModel);
		m_elisionEditor = new ElisionEditor(frame, tableModel);
//		tableModel.addTableModelListener(this);
	}

	public TableCellRenderer getCellRenderer(int row, int column)
	{
		int col = convertColumnIndexToModel(column);

		if (col == 0) {
			return(super.getCellRenderer(row, column));
		}

		return (SelectedElisions) dataModel.getValueAt(row, col);
	}

	// Overload how cells are editted

	public TableCellEditor getCellEditor(int row, int column)
	{
		return m_elisionEditor;
	}
}

public class EditElisions extends JDialog implements ActionListener { 

	private	LandscapeEditorCore	m_ls;
	private ElisionTableModel	m_elisionTableModel;
	private ElisionTable		m_table;
	private JButton				m_ok      = null;
	private JButton				m_cancel  = null;
	private Object				m_object;
			
	protected EditElisions(JFrame frame, LandscapeEditorCore ls, Object object)
	{
		super(frame, "Edit elision rules" + ((object != null) ? " for " + object.toString() : ""),true); //false if non-modal

		Container	contentPane;
		JScrollPane	scrollPane;
		JPanel		buttons;
		Font		font, bold;
		int			height, height1;
		Dimension	d, d1;
		
		m_ls         = ls;
		m_object     = object;

		font         = FontCache.getDialogFont();
		bold         = font.deriveFont(Font.BOLD);

		setLocation(20, 20);

//		setSize(438,369);

		contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		setForeground(ColorCache.get(0,0,0));
		setBackground(ColorCache.get(192,192,192));
		setFont(font);

		ElisionTableModel	tableModel;
		ElisionTable		table;

		m_elisionTableModel = tableModel = new ElisionTableModel(ls.getDiagram(), object);
		m_table             = table      = new ElisionTable(ls.getFrame(), tableModel);
		table.setFont(font);

		JTableHeader tableHeader = table.getTableHeader();
		tableHeader.setFont(bold);

		FontMetrics fm = getFontMetrics(font);
		height  = fm.getHeight() + 4;
		height1 = ToolBarButton.HEIGHT * 2;
		if (height < height1) {
			height = height1;
		}
		table.setRowHeight(height);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		scrollPane = new JScrollPane(table);
		
		table.setVisible(true);
		scrollPane.setVisible(true);
		contentPane.add(scrollPane, BorderLayout.CENTER);


		buttons = new JPanel();
		buttons.setLayout(new FlowLayout());

		m_ok     = new JButton("Ok");
		m_ok.setFont(bold);
		m_ok.addActionListener(this);
		buttons.add(m_ok);

		m_cancel = new JButton("Cancel");
		m_cancel.setFont(bold);
		m_cancel.addActionListener(this);
		buttons.add(m_cancel);

		contentPane.add(BorderLayout.SOUTH, buttons);

		table.removeEditor();
		table.doLayout();
		
		height *= (tableModel.getRowCount() + 1);
		d1      = table.getPreferredSize();
		d       = new Dimension(d1.width, height);
		
		table.setPreferredSize(d);
		scrollPane.setPreferredSize(d);
		
		pack();
		setVisible(true);
	}

	private static void changeElisions(EntityInstance e, SelectedElisions[] selectedElisions, Vector rcs)
	{
		SelectedElisions	selectedElision;
		RelationClass		rc;
		int					elisions, col, i, mask, nid;
		
		for (col = rcs.size(); --col >= 0; ) {
			selectedElision = selectedElisions[col];
			rc              = (RelationClass) rcs.elementAt(col);
			nid             = rc.getNid();
			elisions        = selectedElision.getElisions();
			if ((elisions & SelectedElisions.CHANGE) == 0) {
				continue;
			}

//			System.out.println("Elisions for " + e + "/" + rc + " " + elisions);
			mask = 1;
			for (i = 0; i < EntityInstance.ELISIONS; ++i) {
				if ((elisions & mask) != 0) {
					e.setElision(i, nid);
				} else {
					e.clearElision(i, nid);
				}
				mask <<= 1;
		}	}
		e.elisionsChanged();
	}
	
	private static void descend(EntityInstance e, SelectedElisions[] row, EntityClass ec, Vector rcs)
	{
		Vector	srcRelList = e.getSrcRelList();
		
		if (e.getParentClass() == ec) {
			changeElisions(e, row, rcs);
		}
		
		if (srcRelList != null) {
			
			RelationInstance	ri;
			EntityInstance		child;
			int					i;

			for (i = srcRelList.size(); --i >= 0; ) {
				ri = (RelationInstance) srcRelList.elementAt(i);
				if (ri.isMarked(RelationInstance.SPANNING_MARK)) {
					child = ri.getDst();
					descend(child, row, ec, rcs);
	}	}	}	}

	private void doEditElisions()
	{
		ElisionTableModel		tableModel    = m_elisionTableModel;
		SelectedElisions[][]	array         = tableModel.getArray();
		Diagram					diagram       = m_ls.getDiagram();
		Vector					rcs           = tableModel.getRelationClasses();
		
		if (m_object instanceof EntityInstance) {
			changeElisions((EntityInstance) m_object, array[0], rcs);
		} else {
			Vector				entityClasses = tableModel.getEntityClasses();
			EntityInstance		root          = diagram.getRootInstance();

			int					row;

			for (row = entityClasses.size() /* Ignore the ALL column */; --row >= 0; ) {
				descend(root, array[row], (EntityClass) entityClasses.elementAt(row), rcs);
		}	}
		diagram.revalidate();
		return;
	}
			
	// ActionListener interface

	public void actionPerformed(ActionEvent ev)
	{
		Object	source = ev.getSource();

		if (source == m_ok || source == m_cancel) {
			if (source == m_ok) {
				doEditElisions();
			}	
			this.setVisible(false);
			return;
		}
	}
} 



