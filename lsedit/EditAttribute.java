package lsedit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class EditAttribute extends JDialog implements ActionListener { //Class definition

	// This is the class which maps to the data being presented/updated to lsedit variables

	protected class AttributeTableModel extends AbstractTableModel {
		
		LandscapeObject	m_object;
		JTable			m_table;

		public AttributeTableModel(LandscapeObject object)
		{
			m_object = object;
		}

		public void setJTable(JTable table)
		{
			m_table = table;
		}

		public int getRowCount()
		{
			return(m_object.getLsAttributeCount());
		}

		public int getColumnCount()
		{
			return(2);
		}

		public String getColumnName(int col)
		{
			if (col == 0) {
				return("Name");
			} else {
				return("Value");
			}
		}

		public Object getValueAt(int row, int col)
		{
			if (col == 0) {
				return(m_object.getLsAttributeNameAt(row));
			} 
			return(m_object.getLsAttributeValueAt(row));
		}

		public boolean isCellEditable(int row, int col)
		{
			if (col == 0) {
				return(m_object.canEditName(row));
			}
			return(m_object.canEditAttribute(row));
		}

		public void setValueAt(Object value, int row, int col)
		{
			LandscapeObject	object  = m_object;
			Diagram			diagram = object.getDiagram();

			if (col == 0) {
				if (diagram.updateAttributeNameAt(object, row, value)) {
					// Order of rows may change
					m_table.revalidate();
					m_table.repaint();
				}
			} else {
				diagram.setValueAt(object, value, row);
			}
		}
	}
	
	// This is the class which decides how the table is drawn and edited

	protected class MyJTable extends JTable {

		protected class ColorRenderer extends JLabel implements TableCellRenderer {
			Border unselectedBorder = null;
			Border selectedBorder   = null;

			public ColorRenderer() 
			{
				super("COLOR", JLabel.CENTER);
				// The text COLOR helps distinguish white from null
				// Null's don't seem to get painted
				setOpaque(true); //MUST do this for background to show up.
			}

			public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus, int row, int column) 
			{
				if (color == null) {
					setBackground(Color.WHITE);
					setForeground(Color.BLACK);
				} else {
					Color	color1 = (Color) color;
					setBackground(color1);
					setForeground(ColorCache.getInverse(color1.getRGB()));
				}
				if (isSelected) {
					if (selectedBorder == null) {
						selectedBorder = BorderFactory.createMatteBorder(2,5,2,5, table.getSelectionBackground());
					}
					setBorder(selectedBorder);
				} else {
					if (unselectedBorder == null) {
						unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5, table.getBackground());
					}
					setBorder(unselectedBorder);
				}
				return this;
			}
		}

		/*
		 * This interface returns a button which is placed where the old value used to be and when fired brings up the
		 * actual editor to change the old value.
		 */

		class ColorEditor extends DefaultCellEditor 
		{
			JFrame			m_frame;
			Color			m_currentColor;
			boolean			m_allowNull;
			JButton			m_button;

			public ColorEditor(JFrame frame, boolean allowNull) 
			{
				super(new JCheckBox()); //Unfortunately, the constructor expects a check box, combo box, or text field.

				m_frame     = frame;
				m_allowNull = allowNull;

				//First, set up the button that brings up the dialog.

				m_button =  new JButton("EDITING");
				m_button.setBackground(Color.white);
				m_button.setBorderPainted(false);
				m_button.setMargin(new Insets(0,0,0,0));

				editorComponent = m_button;
				setClickCountToStart(1); //This is usually 1 or 2.

				//Here's the code that brings up the dialog.
				m_button.addActionListener(new ActionListener() 
											 {
												public void actionPerformed(ActionEvent e) 
												{
													if (m_currentColor == null) {
														m_button.setBackground(Color.WHITE);
														m_button.setForeground(Color.BLACK);
													} else {
														m_button.setBackground(m_currentColor);
														m_button.setForeground(ColorCache.getInverse(m_currentColor.getRGB()));
													}
													ColorChooser colorChooser = new ColorChooser(m_frame, "Pick a color", m_currentColor, true /* include alpha */, m_allowNull);
													m_currentColor = colorChooser.getColor();
													fireEditingStopped(); 
													colorChooser.dispose();
												}
											 });

			}

/*
			protected void fireEditingStopped()
			{
				super.fireEditingStopped();
				System.out.println("fireEditingStopped");
			}
 */
			public Object getCellEditorValue() 
			{
				return m_currentColor;
			}

			public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) 
			{
				m_currentColor = (Color) value;
				return editorComponent;
			}
		}

		// Do something sensible to handle new lines in input string

		protected class TextRenderer extends JTextField implements TableCellRenderer {

			public TextRenderer() 
			{
				super();
			}

			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
			{
				setText((String) value);
				return this;
			}
		}

		// Bring up a large box for editting multiple lines of text

		class TextEditor extends DefaultCellEditor {

			JFrame			m_frame;
			String			m_currentText;
			JButton			m_button;				// This is the thing that the editor clicks to bring up dialog
			JTextArea		m_textArea = null;
			JDialog			m_dialog   = null;

			public TextEditor(JFrame frame, final String title) 
			{
				super(new JCheckBox()); //Unfortunately, the constructor expects a check box, combo box, or text field.

				//First, set up the button that brings up the dialog.

				m_frame  = frame;
				m_button = new JButton(); 
        
				m_button.setBackground(Color.white);
				m_button.setBorderPainted(false);
				m_button.setMargin(new Insets(0,0,0,0));

				editorComponent = m_button;
				setClickCountToStart(1); //This is usually 1 or 2.

				//Here's the code that brings up the dialog.
				m_button.addActionListener(new ActionListener() 
											 {
												public void actionPerformed(ActionEvent e) 
												{
													if (m_dialog == null) {
														Container		contentPane;
														JScrollPane		scrollPane;
														JButton			OK_button;
														Font			font;

														m_dialog   = new JDialog(m_frame, title, true);
														m_textArea = new JTextArea();
														m_dialog.setSize(438,369);

														contentPane = m_dialog.getContentPane();
														contentPane.setLayout(null);

														font = FontCache.get("Dialog",Font.PLAIN,12);
														m_dialog.setForeground(ColorCache.get(0,0,0));
														m_dialog.setBackground(ColorCache.get(192,192,192));
														m_dialog.setFont(font);

														scrollPane = new JScrollPane(m_textArea);
														scrollPane.setVisible(true);
														scrollPane.setBounds(5,5,423,300);
														contentPane.add(scrollPane);

														OK_button = new JButton("OK", null);
														// font = FontCache.get("Dialog",Font.PLAIN,8);
														OK_button.setFont(font);
														OK_button.setBounds(5, 310, 60, 30);
																											
														contentPane.add(OK_button);
														OK_button.addActionListener(new ActionListener() 
																					{
																						public void actionPerformed(ActionEvent e) {
																							m_currentText = m_textArea.getText();
																							m_dialog.setVisible(false);
																						}
																					});
													}
													m_textArea.setText(m_currentText);
											 		m_dialog.setVisible(true);
													fireEditingStopped();
												}
											 });

			}
			public Object getCellEditorValue() 
			{
				return m_currentText;
			}

			public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) 
			{
				m_currentText = (String) value;
				return editorComponent;
			}
		}

		// Ignore changes which do not produce double values

		class DoubleEditor extends DefaultCellEditor {

			Double			m_current;
			JTextField		m_textField;

			public DoubleEditor() 
			{
				super(new JCheckBox()); //Unfortunately, the constructor expects a check box, combo box, or text field.

				//First, set up the button that brings up the dialog.

				m_textField =  new JTextField(); 
        		editorComponent = m_textField;
				setClickCountToStart(1); //This is usually 1 or 2.

				//Must do this so that editing stops when appropriate.
				m_textField.addActionListener(new ActionListener() 
											 {
												public void actionPerformed(ActionEvent e) {
													fireEditingStopped();
												}
											 });

				m_textField.addActionListener(new ActionListener() 
											 {
												public void actionPerformed(ActionEvent e) 
												{
													Double	newValue;

													try {
														newValue  = new Double(m_textField.getText());
														m_current = newValue;
													} catch (Exception e1) {
														System.out.println("\"" + m_textField.getText() + "\" is not a double");
													}
												}
											 });

			}

			public Object getCellEditorValue() 
			{
				return m_current;
			}

			public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) 
			{
				m_current = (Double) value;
				m_textField.setText(m_current.toString());
				return editorComponent;
			}
		}

		class IntegerEditor extends DefaultCellEditor {

			Integer			m_current;
			JTextField		m_textField;

			public IntegerEditor() 
			{
				super(new JCheckBox()); //Unfortunately, the constructor expects a check box, combo box, or text field.

				//First, set up the button that brings up the dialog.

				m_textField =  new JTextField(); 
        		editorComponent = m_textField;
				setClickCountToStart(1); //This is usually 1 or 2.

				//Must do this so that editing stops when appropriate.
				m_textField.addActionListener(new ActionListener() 
											 {
												public void actionPerformed(ActionEvent e) {
													fireEditingStopped();
												}
											 });

				m_textField.addActionListener(new ActionListener() 
											 {
												public void actionPerformed(ActionEvent e) 
												{
													Integer	newValue;

													try {
														newValue  = new Integer(m_textField.getText());
														m_current = newValue;
													} catch (Exception e1) {
														System.out.println("\"" + m_textField.getText() + "\" is not an integer");
													}
												}
											 });

			}

			public Object getCellEditorValue() 
			{
				return m_current;
			}

			public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) 
			{
				m_current = (Integer) value;
				m_textField.setText(m_current.toString());
				return editorComponent;
			}
		}

		// Do something sensible to handle limited choice of styles

		protected class StyleRenderer extends JTextField implements TableCellRenderer {

			public StyleRenderer() 
			{
				super();
			}

			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
			{
				int		style;
				String	description;

				style = ((Integer) value).intValue();

				if (style < 0 || style >= EntityClass.styleName.length) {
					description = "";
				} else {
					description = EntityClass.styleName[style];
				}
				setText(description);
				return this;
			}
		}

		class StyleEditor extends DefaultCellEditor {

			Integer			m_current;
			JComboBox		m_comboBox;

			public StyleEditor() 
			{
				super(new JComboBox()); 

				//First, set up the combo box
				
				int	i;

				m_comboBox =  (JComboBox) editorComponent; 

				for (i = 0; i < EntityClass.styleName.length; ++i) {
					m_comboBox.addItem(EntityClass.styleName[i]);
				}
				setClickCountToStart(1); //This is usually 1 or 2.

				//Must do this so that editing stops when appropriate.
				m_comboBox.addActionListener(new ActionListener() 
											 {
												public void actionPerformed(ActionEvent e) {
													fireEditingStopped();
												}
											 });

				m_comboBox.addActionListener(new ActionListener() 
											 {
												public void actionPerformed(ActionEvent e) 
												{
													int		selected;

													selected = m_comboBox.getSelectedIndex();
													if (selected >= 0) {
														m_current = new Integer(selected);
													}
												}
											 });

			}

			public Object getCellEditorValue() 
			{
				return m_current;
			}

			public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) 
			{
				int	selected;

				m_current = (Integer) value;
				selected  = m_current.intValue();
				if (selected >= 0 && selected < m_comboBox.getItemCount()) {
					m_comboBox.setSelectedIndex(m_current.intValue());
				}
				return editorComponent;
			}
		}

		// Do something sensible to handle limited choice of styles

		protected class ImageRenderer extends JComponent implements TableCellRenderer {

			int		m_image;
			boolean m_isSelected;

			public ImageRenderer() 
			{
				super();
			}

			public void paintComponent(Graphics g) 
			{
				int		width	= getWidth();
				int		height	= getHeight();
				int		size	= width;
				Color	color   = Color.BLACK;

				if (height < size) {
					size = height;
				}

				if (m_isSelected) {
					color = Color.BLUE;
				}
				g.setColor(color);
				EntityComponent.paintImage(g, m_image, (width - size)/2, (height-size)/2, size, size, true /* force */);
			}

			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
			{
				m_image = ((Integer) value).intValue();
				return this;
			}
		}

		class ImageEditor extends DefaultCellEditor 
		{
			JFrame			m_frame;
			Integer			m_currentImage;
			JButton			m_button;

			public ImageEditor(JFrame frame) 
			{
				super(new JCheckBox()); //Unfortunately, the constructor expects a check box, combo box, or text field.

				m_frame = frame;

				//First, set up the button that brings up the dialog.

				m_button =  new JButton("");
				m_button.setBackground(Color.white);
				m_button.setBorderPainted(false);
				m_button.setMargin(new Insets(0,0,0,0));

				editorComponent = m_button;
				setClickCountToStart(1); //This is usually 1 or 2.

				//Here's the code that brings up the dialog.
				m_button.addActionListener(new ActionListener() 
											 {
												public void actionPerformed(ActionEvent e) 
												{
													ImageChooser imageChooser = new ImageChooser(m_frame, m_currentImage);
													m_currentImage = imageChooser.getImage();
													fireEditingStopped(); 
												}
											 });
			}

			public Object getCellEditorValue() 
			{
				return m_currentImage;
			}

			public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) 
			{
				m_currentImage = (Integer) value;
				return editorComponent;
			}
		}

		// Do something sensible to handle limited choice of styles

		protected class RelStyleRenderer extends JTextField implements TableCellRenderer {

			public RelStyleRenderer() 
			{
				super();
			}

			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
			{
				int		style;
				String	description;

				style = ((Integer) value).intValue();

				if (style < 0 || style >= Util.lineStyleName.length) {
					description = "";
				} else {
					description = Util.lineStyleName[style];
				}
				setText(description);
				return this;
			}
		}

		class RelStyleEditor extends DefaultCellEditor {

			Integer			m_current;
			JComboBox		m_comboBox;

			public RelStyleEditor() 
			{
				super(new JComboBox()); 

				//First, set up the combo box
				
				int	i;

				m_comboBox =  (JComboBox) editorComponent; 

				for (i = 0; i < Util.lineStyleName.length; ++i) {
					m_comboBox.addItem(Util.lineStyleName[i]);
				}
				setClickCountToStart(1); //This is usually 1 or 2.

				//Must do this so that editing stops when appropriate.
				m_comboBox.addActionListener(new ActionListener() 
											 {
												public void actionPerformed(ActionEvent e) {
													fireEditingStopped();
												}
											 });

				m_comboBox.addActionListener(new ActionListener() 
											 {
												public void actionPerformed(ActionEvent e) 
												{
													int		selected;

													selected = m_comboBox.getSelectedIndex();
													if (selected >= 0) {
														m_current = new Integer(selected);
													}
												}
											 });

			}

			public Object getCellEditorValue() 
			{
				return m_current;
			}

			public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) 
			{
				int	selected;

				m_current = (Integer) value;
				selected  = m_current.intValue();
				if (selected >= 0 && selected < m_comboBox.getItemCount()) {
					m_comboBox.setSelectedIndex(m_current.intValue());
				}
				return editorComponent;
			}
		}

		class ParentEntityClassEditor extends DefaultCellEditor {

			String			m_current;
			JComboBox		m_comboBox;

			public ParentEntityClassEditor() 
			{
				super(new JComboBox()); 

				//First, set up the combo box
				
				Enumeration	en;
				EntityClass	ec;

				m_comboBox =  (JComboBox) editorComponent; 

				for (en = m_ls.enumEntityClassesInOrder(); en.hasMoreElements(); ) {
					ec = (EntityClass) en.nextElement();
					m_comboBox.addItem(ec.getLabelId());
				}
				setClickCountToStart(1); //This is usually 1 or 2.

				//Must do this so that editing stops when appropriate.
				m_comboBox.addActionListener(new ActionListener() 
											 {
												public void actionPerformed(ActionEvent e) {
													fireEditingStopped();
												}
											 });

				m_comboBox.addActionListener(new ActionListener() 
											 {
												public void actionPerformed(ActionEvent e) 
												{
													int		selected;

													selected = m_comboBox.getSelectedIndex();
													if (selected >= 0) {
														m_current = (String) m_comboBox.getItemAt(selected);
													}
												}
											 });

			}

			public Object getCellEditorValue() 
			{
				return m_current;
			}

			public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) 
			{
				int			selected;
				Enumeration	en;
				EntityClass	ec;

				m_current = (String) value;

				selected = 0;
				for (en = m_ls.enumEntityClassesInOrder(); en.hasMoreElements(); ++selected) {
					ec = (EntityClass) en.nextElement();
					if (ec.getLabelId().equals(m_current)) {
						m_comboBox.setSelectedIndex(selected);
						break;
				}	}
				return editorComponent;
			}
		}	

		class ParentRelationClassEditor extends DefaultCellEditor {

			String			m_current;
			JComboBox		m_comboBox;

			public ParentRelationClassEditor() 
			{
				super(new JComboBox()); 

				//First, set up the combo box
				
				Enumeration		en;
				RelationClass	rc;

				m_comboBox =  (JComboBox) editorComponent; 

				for (en = m_ls.enumRelationClassesInOrder(); en.hasMoreElements(); ) {
					rc = (RelationClass) en.nextElement();
					m_comboBox.addItem(rc.getLabelId());
				}	
				setClickCountToStart(1); //This is usually 1 or 2.

				//Must do this so that editing stops when appropriate.
				m_comboBox.addActionListener(new ActionListener() 
											 {
												public void actionPerformed(ActionEvent e) {
													fireEditingStopped();
												}
											 });

				m_comboBox.addActionListener(new ActionListener() 
											 {
												public void actionPerformed(ActionEvent e) 
												{
													int		selected;

													selected = m_comboBox.getSelectedIndex();
													if (selected >= 0) {
														m_current = (String) m_comboBox.getItemAt(selected);
													}
												}
											 });

			}

			public Object getCellEditorValue() 
			{
				return m_current;
			}

			public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) 
			{
				int				selected;
				Enumeration		en;
				RelationClass	rc;

				m_current = (String) value;

				selected = 0;
				for (en = m_ls.enumRelationClassesInOrder(); en.hasMoreElements(); ) {
					rc = (RelationClass) en.nextElement();
					if (rc.getLabelId().equals(m_current)) {
						m_comboBox.setSelectedIndex(selected);
						break;
					}
					++selected;
				}	
				return editorComponent;
			}
		}	

		JFrame				m_frame;
		LandscapeEditorCore	m_ls;
		LandscapeObject		m_object;

		public MyJTable(JFrame frame, LandscapeEditorCore ls, AbstractTableModel tableModel, LandscapeObject object)
		{
			super(tableModel);

			m_frame  = frame;
			m_ls     = ls;
			m_object = object;
		}

		// Overload how cells are rendered

		public TableCellRenderer getCellRenderer(int row, int column)
		{
			Object				value;
			TableCellRenderer	ret;
			String				tip;

			ret = null;
			tip = null;
			if (convertColumnIndexToModel(column) == 1) {

				int type;

				value = dataModel.getValueAt(row, column);
				type  = m_object.getLsAttributeTypeAt(row);

				switch (type) {
				case Attribute.STRING_TYPE:
					if (row != 0) {
						tip = "Click to change string";
					} else {
						tip = "This value is fixed";
					}
					break;
				case Attribute.DOUBLE_TYPE:
					tip = "Click to change double";
					break;
				case Attribute.INT_TYPE:
					tip = "Click to change integer";
					break;
				case Attribute.COLOR_TYPE:
				case Attribute.COLOR_OR_NULL_TYPE:
					if (value != null) {
						ret = new ColorRenderer();
					}
					tip = "Click to change color";
					break;
				case Attribute.TEXT_TYPE:
					if (value != null) {
						ret = new TextRenderer();
					}
					tip = "Click to change text";
					break;
				case Attribute.ENTITY_STYLE_TYPE:
					if (value != null) {
						ret = new StyleRenderer();
					}
					tip = "Click to change entity style";
					break;
				case Attribute.ENTITY_IMAGE_TYPE:
					if (value != null) {
						ret = new ImageRenderer();
					}
					tip = "Click to change entity image";
					break;
				case Attribute.REL_STYLE_TYPE:
					if (value != null) {
						ret = new RelStyleRenderer();
					}
					tip = "Click to change relation style";
					break;
				case Attribute.ATTR_TYPE:
					tip = "Click to change attribute value";
					break;
				case Attribute.NULL_TYPE:
					tip = "First enter name to create new attribute";
					break;
				case Attribute.ENTITY_CLASS_TYPE:
					tip = "Click to change entity class";
					break;
				case Attribute.RELATION_CLASS_TYPE:
					tip = "Click to change relation class";
					break;
				}
				if (ret == null) {
					ret = super.getCellRenderer(row, column);
					((JLabel) ret).setForeground(Color.black);
				}
			} else {
				Color	foreground;

				ret = super.getCellRenderer(row, column);
				if (row < m_object.getPrimaryAttributeCount()) {
					foreground = Color.blue;
					tip        = "This attribute name is fixed";
				} else {
					foreground = Color.red;
					if (row == m_object.getLsAttributeCount()-1) {
						tip    = "Click to add new attribute name";
					} else {
						tip    = "Click to change/delete attribute name";
				}	}
				((JLabel) ret).setForeground(foreground);
			}
			if (ret != null) {
				((JComponent) ret).setToolTipText(tip);
			}
			return(ret);
		}

		// Overload how cells are editted

		public TableCellEditor getCellEditor(int row, int column)
		{
			int		type;

			if (convertColumnIndexToModel(column) == 1) {
				type = m_object.getLsAttributeTypeAt(row);
				switch (type) {
				case Attribute.INT_TYPE:
					return(new IntegerEditor());
				case Attribute.DOUBLE_TYPE:
					return(new DoubleEditor());
				case Attribute.COLOR_TYPE:
					return(new ColorEditor(m_frame, false));
				case Attribute.COLOR_OR_NULL_TYPE:
					return(new ColorEditor(m_frame, true));
				case Attribute.TEXT_TYPE:
					return(new TextEditor(m_frame, "Change text"));
				case Attribute.ATTR_TYPE:
					return(new TextEditor(m_frame, "Change attribute value"));
				case Attribute.ENTITY_STYLE_TYPE:
					return(new StyleEditor());
				case Attribute.ENTITY_IMAGE_TYPE:
					return(new ImageEditor(m_frame));
				case Attribute.REL_STYLE_TYPE:
					return(new RelStyleEditor());
				case Attribute.ENTITY_CLASS_TYPE:
					return(new ParentEntityClassEditor());
				case Attribute.RELATION_CLASS_TYPE:
					return(new ParentRelationClassEditor());
			}	}
			// STRING_TYPE
			// INT_LIST_TYPE
			// DOUBLE_LIST_TYPE
			// STRING_LIST_TYPE
			// POINTER_TYPE
			// ELISION_TYPE
			return(super.getCellEditor(row, column));
		}
	}

	private MyJTable	m_table;
	private JButton		m_ok     = null;
			
	protected EditAttribute(JFrame frame, LandscapeEditorCore ls, LandscapeObject e, String title) //Constructor
	{
		super(frame, title,true); //false if non-modal

		Container	contentPane;
		JScrollPane	scrollPane;
		Font		font, bold;

		font         = FontCache.getDialogFont();
		bold         = font.deriveFont(Font.BOLD);

//		setSize(438,369);

		contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		setForeground(ColorCache.get(0,0,0));
		setBackground(ColorCache.get(192,192,192));
		setFont(font);

		AttributeTableModel	tableModel;
		MyJTable			table;

		tableModel = new AttributeTableModel(e);
		table      = new MyJTable(frame, ls, tableModel, e);
		table.setFont(font);
		m_table    = table;

		JTableHeader tableHeader = table.getTableHeader();
		tableHeader.setFont(bold);

		FontMetrics fm = getFontMetrics(font);

		table.setRowHeight(fm.getHeight() + 4);

		tableModel.setJTable(table);
		table.setVisible(true);
		scrollPane = new JScrollPane(table);

		scrollPane.setVisible(true);
		contentPane.add(scrollPane, BorderLayout.CENTER);


		m_ok     = new JButton("Ok");
		m_ok.setFont(bold);
		m_ok.addActionListener(this);
		contentPane.add(m_ok, BorderLayout.SOUTH);

		pack();
		setVisible(true);

		table.removeEditor();
	}

	public static void Create(LandscapeEditorCore ls, EntityInstance e) 
	{
		ls.beginUndoRedo("Edit Entity " + e.getEntityLabel());
		{
			EditAttribute editAttribute = new EditAttribute(ls.getFrame(), ls, e, "Edit Entity Attributes");
		
			editAttribute.dispose();
		}
		ls.repaint(); 
		ls.endUndoRedo();
	}

	public static void Create(LandscapeEditorCore ls, RelationInstance e) 
	{
		ls.beginUndoRedo("Edit Relation " + e);
		{
			EditAttribute editAttribute = new EditAttribute(ls.getFrame(), ls, e, "Edit Relation Attributes");

			editAttribute.dispose();
		}
		ls.repaint(); 
		ls.endUndoRedo();
	}

	public static void Create(LandscapeEditorCore ls, EntityClass ec) 
	{
		EditAttribute editAttribute;

		ls.beginUndoRedo("Edit EntityClass " + ec.getLabel());
		editAttribute = new EditAttribute(ls.getFrame(), ls, ec, "Edit Entity Class Attributes");
		editAttribute.dispose();
		ls.endUndoRedo();
	}

	public static void Create(LandscapeEditorCore ls, RelationClass rc) 
	{
		EditAttribute editAttribute;

		ls.beginUndoRedo("Edit RelationClass " + rc);
		editAttribute = new EditAttribute(ls.getFrame(), ls, rc, "Edit Relation Class Attributes");
		editAttribute.dispose();
		ls.endUndoRedo();
	}

	// ActionListener interface

	public void actionPerformed(ActionEvent ev)
	{
		Object	source;

		// Pop down the window when the button is clicked.
		// System.out.println("event: " + ev);

		source = ev.getSource();

		if (source == m_ok) {
/*
			int				rows    = m_table.getRowCount();
			int				columns = m_table.getColumnCount();
			int				erow    = m_table.getEditingRow();
			int				ecolumn = m_table.getEditingColumn();
			TableCellEditor editor;
			int				column, row;
			Object			value;

			for (row = 0;  row < rows; ++row) {
				for (column = 0; column < columns; ++column) {
					System.out.print("row=" + row + " col=" + column);
					try {
						System.out.print(" model=");
						System.out.print(m_table.getModel().getValueAt (row, column).toString());
						System.out.print(" table=");
						System.out.print(m_table.getValueAt(row,column).toString());

						if (row == erow && column == ecolumn) {
							editor = m_table.getCellEditor(row, column); 
							if (editor == null) {
								System.out.print(" noeditor");
							} else {
								System.out.print(" class=" + editor.getClass());
								if (!editor.stopCellEditing()) {
									System.out.print(" unstoppable");
								} else {
									value = editor.getCellEditorValue();
									System.out.print(" '" + value + "'");
						}	}	}
					} catch(Exception e) {
						System.out.print(" exception=" + e.getMessage());
					}
					System.out.println("");
			}	}
*/
			this.setVisible(false);
			return;
		}
	}

} //End of class EditAttribute



