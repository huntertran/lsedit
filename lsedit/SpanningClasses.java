package lsedit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.event.TableModelListener;

	// This is the class which maps to the data being presented/updated to lsedit variables

class MyTableModel extends AbstractTableModel {
	
	Ta		m_ta;
	Vector	m_classes;
	int		m_cnt_spanning;

	private void restart1()
	{
		RelationClass[] containsClasses = m_ta.getContainsClasses();
		Vector			classes         = m_classes;
		Enumeration		en;
		RelationClass	rc;
		int				i;
		
		classes.clear();
		for (i = 0; i < containsClasses.length; ++i) {
			classes.add(containsClasses[i]);
		}
		m_cnt_spanning = i;
		for (en = m_ta.enumRelationClassesInOrder(); en.hasMoreElements(); ) {
			rc = (RelationClass) en.nextElement(); 
			if (!classes.contains(rc)) {
				classes.add(rc);
	}	}	}
	
	public void restart()
	{
		restart1();
		fireTableDataChanged();
	}
	
	public MyTableModel(Ta ta)
	{
		m_ta	  = ta;
		m_classes = new Vector();

		restart1();
	}
		
	public void clearSpanningClasses()
	{
		// Used to signal should return null
		m_cnt_spanning = 0;
	}
	
	public Vector getClasses()
	{
		return m_classes;
	}
	
	public int getCntSpanning()
	{
		return m_cnt_spanning;
	}

	public int getRowCount()
	{
		return (m_classes.size());
	}

	public int getColumnCount()
	{
		return(1);
	}

	public String getColumnName(int col)
	{
		return(null);
	}

	public Object getValueAt(int row, int col)
	{
		if (row < m_classes.size()) {
			RelationClass rc = (RelationClass) m_classes.elementAt(row);
			return(rc.getRelationLabel());
		}
		return "";
	}

	public boolean isCellEditable(int row, int col)
	{
		return(false);
	}

	public void setValueAt(Object value, int row, int col)
	{
	}
	
	public void forceNotEmpty()
	{
		if (m_cnt_spanning == 0) {
			Object rc = m_ta.getRelationBaseClass();
			m_classes.remove(rc);
			m_classes.insertElementAt(rc, 0);
			m_cnt_spanning = 1;
	}	}
	
	public void add(int[] rows)
	{
		Object	rc;
		int		i, row;
		boolean	ret = false;
		
		for (i = 0; i < rows.length; ++i) {
			row = rows[i];
			if (m_cnt_spanning <= row) {
				rc = m_classes.elementAt(row);
				m_classes.remove(row);
				m_classes.insertElementAt(rc, m_cnt_spanning);
				++m_cnt_spanning;
				ret = true;
		}	}
		if (ret) {
			fireTableDataChanged();
		}
	}
	
	public void remove(int[]	rows)
	{
		Object	rc;
		int		i, row, at;
		boolean	ret = false;
		
		at = m_classes.size()-1;
		for (i = rows.length; --i >= 0; ) {
			row = rows[i];
			if (row < m_cnt_spanning) {
				rc = m_classes.elementAt(row);
				m_classes.remove(row);
				m_classes.insertElementAt(rc, at);
				--at;
				--m_cnt_spanning;
				ret = true;
		}	}
		forceNotEmpty();
		if (ret) {
			fireTableDataChanged();
	}	}
	
	public void up(int[] rows)
	{
		Object	rc;
		int		i, row;
		boolean	ret = false;
		
		for (i = 0; i < rows.length; ++i) {
			row = rows[i];
			if (m_cnt_spanning == row) {
				++m_cnt_spanning;
				ret = true;
			} else if (row > 0) {
				rc = m_classes.elementAt(row);
				m_classes.remove(row);
				m_classes.insertElementAt(rc, row-1);
				ret = true;
		}	}
		if (ret) {
			fireTableDataChanged();
	}	}
	
	public void down(int[]	rows)
	{
		Object	rc;
		int		i, row;
		boolean	ret = false;
		
		for (i = rows.length; --i >= 0; ) {
			row = rows[i];
			if (row == m_cnt_spanning-1) {
				--m_cnt_spanning;
				ret = true;
			} else if (row < m_classes.size()-1) {
				rc = m_classes.elementAt(row);
				m_classes.remove(row);
				m_classes.insertElementAt(rc, row+1);
				ret = true;
		}	}
		forceNotEmpty();
		if (ret) {
			fireTableDataChanged();
	}	}
}

// This is the class which decides how the table is drawn and edited

class MyJTable extends JTable implements TableModelListener {

	public MyJTable(AbstractTableModel tableModel)
	{
		super(tableModel);
		//drag and drop 
		setTableHeader(null);
//		setFillsViewportHeight(false); 
		tableModel.addTableModelListener(this); 
	}

	// Overload how cells are rendered

	public TableCellRenderer getCellRenderer(int row, int column)
	{
		MyTableModel		model   = (MyTableModel) getModel();
		Vector				classes = model.getClasses();
		TableCellRenderer	ret;
		Color				foreground;

		ret = super.getCellRenderer(row, column);
		if (row < classes.size()) {
			if (row < model.getCntSpanning()) {
				foreground = Color.blue;
			} else {
				foreground = Color.black;
			}
			((JLabel) ret).setForeground(foreground);
		}
		return(ret);
	}
}
		
public class SpanningClasses extends JDialog implements ActionListener { //Class definition

	static protected final int BUTTON_ADD     = 0;
	static protected final int BUTTON_REMOVE  = 1;
	static protected final int BUTTON_UP      = 2;
	static protected final int BUTTON_DOWN    = 3;
	static protected final int BUTTON_OK      = 4;
	static protected final int BUTTON_CANCEL  = 5;
	static protected final int BUTTON_RESTART = 6;
	static protected final int BUTTON_HELP    = 7;

	protected final static String[] m_button_titles =
							{
								"Add",
								"Remove",
								"Up",
								"Down",
								"Ok",
								"Cancel",
								"Restart",
								"Help",
							};
					
	private LandscapeEditorCore	m_ls;	
	private Diagram				m_diagram;	
	private MyTableModel		m_model;
	private	MyJTable			m_table;
	private JButton[]			m_buttons;

	protected SpanningClasses(JFrame frame, Diagram diagram) 
	{
		super(frame, "Choose Spanning Classes",true); //false if non-modal

		Container	contentPane;
		JScrollPane	scrollPane;
		JPanel		panel;
		JButton		button;
		Font		font, bold;
		int			i;

		m_diagram    = diagram;
		m_ls         = diagram.getLs();
		font         = FontCache.getDialogFont();
		bold         = font.deriveFont(Font.BOLD);

//		setSize(438,369);

		contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		setForeground(ColorCache.get(0,0,0));
		setBackground(ColorCache.get(192,192,192));
		setFont(font);

		MyTableModel	tableModel;
		MyJTable		table;

		m_model = tableModel = new MyTableModel(diagram);
		m_table = table      = new MyJTable(tableModel);
		table.setFont(font);

		FontMetrics fm = getFontMetrics(font);

		table.setRowHeight(fm.getHeight() + 4);

		table.setVisible(true);
		scrollPane = new JScrollPane(table);

		scrollPane.setVisible(true);
		contentPane.add(scrollPane, BorderLayout.CENTER);

		// --------------
		// Use a FlowLayout to center the button and give it margins.

		JPanel		bottomPanel = new JPanel();
		GridLayout	gridLayout = new GridLayout(2,4);
		gridLayout.setVgap(0);
		bottomPanel.setLayout(gridLayout);

		m_buttons = new JButton[m_button_titles.length];
		for (i = 0; i < m_button_titles.length; ++i) {
			m_buttons[i] = button = new JButton(m_button_titles[i]);
			button.setFont(bold);
			button.addActionListener(this);
			bottomPanel.add(button);
		}
		contentPane.add(bottomPanel, BorderLayout.SOUTH);

		pack();
		setVisible(true);

		table.removeEditor();
	}
	
	public	RelationClass[] getSpanningClasses()
	{
		int				cnt      = m_model.getCntSpanning();
		RelationClass[]	ret      = null;
		
		if (cnt > 0) {
			Vector		classes  = m_model.getClasses();
			int			i;

			ret = new RelationClass[cnt];
			for (i = 0; i < cnt; ++i) {
				ret[i] = (RelationClass) classes.elementAt(i);
		}	}
		return ret;
	} 

	// ActionListener interface

	public void actionPerformed(ActionEvent ev)
	{
		Object	source = ev.getSource();
		int		state, i;
		
		state = -1;
		for (i = 0; i < m_button_titles.length; ++i) {
			if (source == m_buttons[i]) {
				state = i;
				break;
		}	}

		switch (state) {
		case BUTTON_ADD:
			m_model.add(m_table.getSelectedRows());
			return;
		case BUTTON_REMOVE:
			m_model.remove(m_table.getSelectedRows());
			return;
		case BUTTON_UP:
			m_model.up(m_table.getSelectedRows());
			return;		
		case BUTTON_DOWN:
			m_model.down(m_table.getSelectedRows());
			return;
		case BUTTON_CANCEL:
			m_model.clearSpanningClasses();
		case BUTTON_OK:
			break;
		case BUTTON_RESTART:
			m_model.restart();
			return;
		case BUTTON_HELP:
			JOptionPane.showMessageDialog(m_ls.getFrame(), 	
			  "Blue relation classes in the order shown will be used to form a new spanning\n" +
			  "tree. Earlier named classes will be preferred for this purpose over later\n" +
			  "named classes.  Use [add] to add to the end of the set of selected spanning\n" +
			  "classes, [remove] to remove from this set, and [up] and [down] to reorder items\n" +
			  "in the list of spanning classes.  Multiple items may be selected by each of these\n"  +
			  "operations."
			 	  , "Help", JOptionPane.OK_OPTION);
		default:
			return;
		}
		setVisible(false);
		return;
	}		
		
	public static RelationClass[] getSpanningClasses(LandscapeEditorCore ls) 
	{
		SpanningClasses spanningClasses = new SpanningClasses(ls.getFrame(), ls.getDiagram());
		RelationClass[]	classes         = spanningClasses.getSpanningClasses();
		
		spanningClasses.dispose();
		return classes;
	}
} 



