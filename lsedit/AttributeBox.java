package lsedit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class ShowLabel extends JLabel
{
	public ShowLabel(String value)
	{
		super(value);

		setHorizontalAlignment(LEFT);
		setHorizontalTextPosition(LEFT);
		setFont(AttributeBox.m_textFont);
}	}


class ShowAttributeColor extends ShowLabel {

	public ShowAttributeColor(Color color) 
	{
		super("COLOR");
		// The text COLOR helps distinguish white from null
		// Null's don't seem to get painted

		if (color == null) {
			setBackground(Color.WHITE);
			setForeground(Color.BLACK);
		} else {
			setBackground(color);
			setForeground(ColorCache.getInverse(color.getRGB()));
}	}	}

class ShowImage extends ShowLabel {

	int		m_image;

	public ShowImage(int image) 
	{
		super("  ");
		m_image = image;
	}

	public void paintComponent(Graphics g) 
	{
		int		width	= getWidth();
		int		height	= getHeight();
		int		size	= width;

		if (height < size) {
			size = height;
		}
		g.setColor(Color.BLACK);
		EntityComponent.paintImage(g, m_image, (width - size)/2, (height-size)/2, size, size, true /* force */);
}	}

class AttributeBoxPanel extends JPanel
{
	AttributeBox	m_attributeBox;

	// ------------------
	// JComponent methods
	// ------------------

/*
	public Component add(Component comp)
	{
		super.add(comp);

		System.out.println("AttributeBoxPanel add " + comp + " " + comp.getBounds());
	
		return(comp);
	}
*/

	public Dimension getPreferredSize()
	{
		return(getSize());
	}

/*
	public void paintComponent(Graphics g)
	{
		int	width, height;

		super.paintComponent(g);

		width   = getWidth();
		height  = getHeight();

		System.out.println("AttributeBoxPanel paintComponent " + width + "x" + height);

		
		// For debugging
		g.setColor(Color.green);
		g.drawLine(0, 0, width, height);
		g.drawLine(0, height, width, 0);
	}
 */
	// Paints rows in alternating backgrounds

	public void paintBorder(Graphics g)
	{
		super.paintBorder(g);

		int			width = getWidth();
		int			cnt   = getComponentCount();
		int			i, y, height;
		Component	component;
		boolean		flag;

		g.setColor(Diagram.lighterBoxColor);
		flag = false;
		for (i = 0; i < cnt; ++i) {
			component = getComponent(i);
			if (!(component instanceof JLabel)) {
				flag = false;
				continue;
			}
			if (!flag) {
				flag = true;
			} else {
				y         = component.getY();
				height    = component.getHeight();
				g.fillRect(0, y, width, height);
				flag = false;
	}	}	}

	public AttributeBoxPanel(AttributeBox attributeBox)
	{
		super();
		m_attributeBox = attributeBox;
		setLayout(null);
	}
}	

// Can't be a subclass of TabBox 

public final class AttributeBox extends JSplitPane implements ChangeListener
{
	protected static final  int GAP = 20;

	protected static final Color	m_titleColor = Color.red.darker();

	public    static       Font		m_textFont	 = null;
	protected static final String	m_indent     = "    ";

	protected static final String	m_helpStr    = "Hold shift down to freeze";

	protected static final int	horizontal_margin    = 10;
	protected static final int	vertical_indent      = 10;

	protected LandscapeEditorCore	m_ls;
	protected JTabbedPane			m_tabbedPane;
	protected JScrollPane			m_scrollPane;
	protected GridLayout			m_layout;
	protected JPanel				m_left;
	protected JPanel				m_right;

	protected LandscapeObject		m_object = null;

	protected int					m_leftWidth  = 0;
	protected int					m_rightWidth = 0;
	protected int					m_height     = 0;

	// ------------------
	// JComponent methods
	// ------------------

	public Dimension getPreferredSize()
	{
		return(getSize());
	}

/*
	public void paintComponent(Graphics g)
	{
		int	width, height;

		super.paintComponent(g);

		width   = getWidth();
		height  = getHeight();

		System.out.println("AttributeBox paintComponent " + width + "x" + height);

		
		// For debugging
		g.setColor(Color.green);
		g.drawLine(0, 0, width, height);
		g.drawLine(0, height, width, 0);
	}
*/

	// --------------
	// Public methods 
	// --------------

	public AttributeBox(LandscapeEditorCore ls, JTabbedPane tabbedPane) 
	{
		super(JSplitPane.HORIZONTAL_SPLIT);

		Insets	    insets  = tabbedPane.getInsets();
		int			divider = getDividerSize();
		int			width   = tabbedPane.getWidth()  - insets.left - insets.right;
		int			height  = tabbedPane.getHeight() - insets.top  - insets.bottom;
		int			left    = (width - divider) / 2;
		int			right   = width - divider - left;

		if (m_textFont == null) {
			m_textFont = Options.getTargetFont(Option.FONT_ATTR_TEXT);
		}

		m_ls         = ls;
		m_tabbedPane = tabbedPane;

		m_left  = new AttributeBoxPanel(this);
		m_right = new AttributeBoxPanel(this);

		m_left.setBackground(Diagram.boxColor);
		m_right.setBackground(Diagram.boxColor);
		m_left.setPreferredSize(new Dimension(left, height));
		m_left.setSize(left, height);
		m_right.setPreferredSize(new Dimension(right, left));
		m_right.setSize(right, height);
		
		setLeftComponent(m_left);
		setRightComponent(m_right);
	
		setSize(width, height);
		setDividerLocation(left);
		setOneTouchExpandable(true);

		m_scrollPane = new JScrollPane();
		m_scrollPane.setBounds(0, 0, width, height);
		setPreferredSize(new Dimension(width, height));

		setToolTipText(m_helpStr);
		m_scrollPane.setViewportView(this);

		tabbedPane.addTab("Attrs", null, m_scrollPane, m_helpStr);
		tabbedPane.addChangeListener(this);
	}

	public static void setTextFont(Font font)
	{
		m_textFont = font;
	}
	
	public void textFontChanged(Font font)
	{
		setTextFont(font);
		fill();
	}
	
	public void activate() 
	{
		m_tabbedPane.setSelectedComponent(m_scrollPane);
	}

	public boolean isActive() 
	{
		
		if (isVisible()) {
			Component active;

			active = m_tabbedPane.getSelectedComponent();
			return(m_scrollPane == active);
		}
		return(false);
	}
	
	public void	addBoth(JComponent left, JComponent right)
	{
		int			leftWidth, rightWidth, leftHeight, rightHeight, height;
		Dimension	dim;

		
		dim = left.getPreferredSize();
		leftWidth   = dim.width;
		leftHeight  = dim.height;

		dim = right.getPreferredSize();

		rightWidth  = dim.width;
		rightHeight = dim.height;

		if (leftWidth > m_leftWidth) {
			m_leftWidth = leftWidth;
		}
		if (rightWidth > m_rightWidth) {
			m_rightWidth = rightWidth;
		}
		if (leftHeight < rightHeight) {
			height = rightHeight;
		} else {
			height = leftHeight;
		}

//		System.out.println("Height=" + height + " left=" + leftWidth + " right=" + rightWidth + " height=" + height1);

		left.setBounds(0,  m_height, leftWidth,  height);
		right.setBounds(0, m_height, rightWidth, height);

		m_left.add(left);
		m_right.add(right);

		m_height += height;
	}

	public void fill(LandscapeObject object)
	{
		ShowLabel	label;
		int			attributes = 0;

		int			i, primary, type;
		String		name;
		Object		value;
		JComponent	left, right;
		Color		foreground;

		attributes = object.getLsAttributeCount();
		primary    = object.getPrimaryAttributeCount();

		for (i = 0; i < attributes; ++i) {
			type  = object.getLsAttributeTypeAt(i);
			if (type == Attribute.NULL_TYPE) {
				break;
			}

			name  = object.getLsAttributeNameAt(i);
			label = new ShowLabel(name);

			if (i < primary) {
				foreground = Color.blue;
			} else {
				foreground = Color.red;
			}
			label.setForeground(foreground);
			label.setFont(m_textFont);
			left = label;

			value = object.getLsAttributeValueAt(i);
			if (value == null) {
				label = new ShowLabel("null");
				label.setForeground(Color.red);
				right = label;
			} else {
				switch (type) {
				case Attribute.STRING_TYPE:
				case Attribute.TEXT_TYPE:
				case Attribute.DOUBLE_TYPE:
				case Attribute.INT_TYPE:
				case Attribute.ENTITY_CLASS_TYPE:
				case Attribute.RELATION_CLASS_TYPE:
				case Attribute.ATTR_TYPE:
					right = new ShowLabel(value.toString());
					break;
				case Attribute.COLOR_TYPE:
				case Attribute.COLOR_OR_NULL_TYPE:
					right = new ShowAttributeColor((Color) value);
					right.setOpaque(true); // MUST do this for background to show up.
					break;
				case Attribute.ENTITY_IMAGE_TYPE:
					right = new ShowImage(((Integer) value).intValue());
					break;
				case Attribute.ENTITY_STYLE_TYPE:
				{
					int		style = ((Integer) value).intValue();
					String	description;

					if (style < 0 || style >= EntityClass.styleName.length) {
						description = "" + style;
					} else {
						description = EntityClass.styleName[style];
					}
					right = new ShowLabel(description);
					break;
				}
				case Attribute.REL_STYLE_TYPE:
				{
					int		style;
					String	description;

					style = ((Integer) value).intValue();

					if (style < 0 || style >= Util.lineStyleName.length) {
						description = "" + style;
					} else {
						description = Util.lineStyleName[style];
					}
					right = new ShowLabel(description);
					break;
				}
				default:
					right = new ShowLabel("");
			}	}

			addBoth(left, right);
		}	

		m_height += 10;

		label = new ShowLabel("Attributes");
		label.setForeground(Color.GREEN);
		left  = label;
		right = new ShowLabel("" + attributes);

		addBoth(left, right);

		LandscapeObject parent;

		if (object instanceof LandscapeObject3D) {
			name = ((LandscapeObject3D) object).getId();
		} else {
			name = "Relation";
		}

		for (i = 0; (parent = object.derivedFrom(i)) != null; ++i) {
			m_height += GAP;
			left  = new ShowLabel("Superclass of");
			right = new ShowLabel(name);

			addBoth(left, right);

			fill(parent);
		}
	}

	public void fill()
	{
		JPanel			left   = m_left;
		JPanel			right  = m_right;

		left.removeAll();
		right.removeAll();

//		System.out.println("AttributeBox fill " + isActive() + " " + m_object);

		if (isActive()) {
			LandscapeObject object = m_object;
			int				i, leftWidth, rightWidth, width, height;
			Component		component;

			m_leftWidth  = 0;
			m_rightWidth = 0;
			m_height     = 0;
			if (object == null) {
				ShowLabel		leftlabel;
				ShowLabel		rightlabel;
				
				leftlabel  = new ShowLabel("No object");
				rightlabel = new ShowLabel("");
				m_height   = 10;
				addBoth(leftlabel, rightlabel);
			} else {
				fill(object);
			}
			leftWidth  = m_leftWidth;
			rightWidth = m_rightWidth;
			height     = m_height;

			for (i = left.getComponentCount(); --i >= 0; ) {
				component = left.getComponent(i);
				component.setSize(leftWidth, component.getHeight());
			}
			for (i = right.getComponentCount(); --i >= 0; ) {
				component = right.getComponent(i);
				component.setSize(rightWidth, component.getHeight());
			}

			width = leftWidth + rightWidth + 10;

			setBounds(0, 0, width, height);
			setPreferredSize(new Dimension(width, height));

			left.setBounds(0,  0, leftWidth,  height);
			left.setPreferredSize(new Dimension(leftWidth,  height));
			
			right.setBounds(0, 0, rightWidth, height);
			right.setPreferredSize(new Dimension(rightWidth, height));
			
			setDividerLocation(leftWidth + 5);

			m_scrollPane.revalidate();
			revalidate();
	}	}

	public void show(LandscapeObject object)
	{
		if (m_object != object) {
			m_object = object;
			fill();
	}	}

	// ChangeListener interface

	public void stateChanged(ChangeEvent e) 
	{
//		System.out.println("AttributeBox stateChanged " + isActive());
		fill();
	}
}






