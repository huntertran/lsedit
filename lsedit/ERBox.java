package lsedit;

import java.util.Vector;
import java.util.Enumeration;

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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class DisplayClassHierarchy implements ActionListener
{
	private LandscapeEditorCore		m_ls;
	private LandscapeClassObject	m_landscapeClass;
	int m_x;
	int m_y;
	JPopupMenu m_popup = null;


	public DisplayClassHierarchy(LandscapeEditorCore ls, LandscapeClassObject landscapeClass, int x, int y)
	{
		m_ls = ls;
		m_landscapeClass = landscapeClass;
		m_x = x;
		m_y = y;
	}

	public void actionPerformed(ActionEvent ev)
	{
		LandscapeEditorCore ls = m_ls;

		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (ls.processMetaKeyEvent(ERBox.g_editInheritanceRules_text))	{
				return;
		}	}

		LandscapeClassObject o = m_landscapeClass;
		ClassInherits dialog = new ClassInherits(m_ls, o);

		dialog.setLocation(m_x, m_y);
		dialog.setVisible(true);

		Vector value = dialog.getResult();

		if (value != null) {
			Diagram diagram = ls.getDiagram();
			diagram.doUpdateInherits(o, value);
}	}	}

class EditClassAttributes implements ActionListener
{
	private LandscapeEditorCore m_ls;
	private EntityClass m_entity;

	public EditClassAttributes(LandscapeEditorCore ls, EntityClass entity)
	{
		m_ls = ls;
		m_entity = entity;
	}

	public void actionPerformed(ActionEvent ev)
	{
		LandscapeEditorCore ls = m_ls;

		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (ls.processMetaKeyEvent(ERBox.g_editClassAttributes_text)) {
				return;
		}	}

		EditAttribute.Create(ls, m_entity);
		ls.repaint();
}	}

class ShowValidAttributes implements ActionListener
{
	private LandscapeEditorCore m_ls;
	private LandscapeClassObject m_o;

	public ShowValidAttributes(LandscapeEditorCore ls, LandscapeClassObject o)
	{
		m_ls = ls;
		m_o = o;
	}

	public void actionPerformed(ActionEvent ev)
	{
		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (m_ls.processMetaKeyEvent(ERBox.g_showValidAttributes_text))	{
				return;
		}	}
		m_ls.showValidAttributes(m_o);
}	}

class CheckEntityAttributes implements ActionListener
{
	private LandscapeEditorCore m_ls;
	private EntityClass m_ec;

	public CheckEntityAttributes(LandscapeEditorCore ls, EntityClass ec) {
		m_ls = ls;
		m_ec = ec;
	}

	public void actionPerformed(ActionEvent ev)
	{
		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (m_ls.processMetaKeyEvent(ERBox.g_validateAttributes_text)) {
				return;
		}	}
		m_ls.validateEntityAttributes(m_ec);
}	}

class SetDefaultEntityClass implements ActionListener
{
	private LandscapeEditorCore m_ls;
	private EntityClass m_ec;

	public SetDefaultEntityClass(LandscapeEditorCore ls, EntityClass ec)
	{
		m_ls = ls;
		m_ec = ec;
	}

	public void actionPerformed(ActionEvent ev)
	{
		LandscapeEditorCore ls = m_ls;

		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (ls.processMetaKeyEvent(ERBox.g_createEntitiesOfThisClass_text))	{
				return;
		}	}
		ls.getDiagram().setDefaultEntityClass(m_ec);
}	}

class DeleteEntityClass implements ActionListener
{
	private LandscapeEditorCore m_ls;
	private EntityClass m_ec;

	public DeleteEntityClass(LandscapeEditorCore ls, EntityClass ec)
	{
		m_ls = ls;
		m_ec = ec;
	}

	public void actionPerformed(ActionEvent ev)
	{
		LandscapeEditorCore ls = m_ls;
		EntityClass ec = m_ec;
		Diagram diagram = ls.getDiagram();
		EntityInstance rootInstance;
		Enumeration en;
		EntityClass ec1;
		String message = null;
		int cnt;

		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (ls.processMetaKeyEvent("Delete Entity Class")) {
				return;
		}	}

		for (en = diagram.enumEntityClasses(); en.hasMoreElements(); ) {
			ec1 = (EntityClass)en.nextElement();
			if (ec != ec1 && ec1.directlyInheritsFrom(ec)) {
				if (message != null) {
					message += ", ";
				} else {
					message = "";
				}
				message += ec1.getLabel();
		}	}
		if (message != null) {
			message += " inherit from " + ec.getLabel();
		} else {
			rootInstance = diagram.getRootInstance();
			if (rootInstance != null && rootInstance.getEntityClass() == ec) {
				message = "Root instance has class " + ec.getLabel();
			} else {
				diagram.recomputeCounts();
				cnt = ec.countMembers();

				if (cnt != 0) {
					message = ec.getLabel() + " has " + cnt + " instantiations";
		}	}	}

		if (message != null) {
			JOptionPane.showConfirmDialog(null, message, "Can't delete entity class", JOptionPane.DEFAULT_OPTION);
			return;
		}

		switch (JOptionPane.showConfirmDialog(null, "Delete class " + ec.getLabel(), "Delete all " + ec.getLabel() + " entities", JOptionPane.YES_NO_CANCEL_OPTION)) {
		case JOptionPane.YES_OPTION:
			diagram.doUpdateRemoveEntityClass(ec);
			break;
		case JOptionPane.NO_OPTION:
			break;
		}
		ls.repaint();
}	}

class DisplayEditElisions implements ActionListener
{
	private LandscapeEditorCore m_ls;
	private LandscapeObject		m_object;

	public DisplayEditElisions(LandscapeEditorCore ls, LandscapeObject object)
	{
		m_ls     = ls;
		m_object = object;
	}

	public void actionPerformed(ActionEvent ev)
	{
		LandscapeEditorCore ls = m_ls;

		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (ls.processMetaKeyEvent(ERBox.g_editElisionRules_text)) {
				return;
		}	}

		EditElisions dialog = new EditElisions(ls.getFrame(), ls, m_object);

		dialog.dispose();
}	}

// Used by legend box

class ShownEntityChkBox extends JComponent implements ItemListener, MouseListener
{

	protected static final int GAP    = 5;
	protected static final int WIDTH  = 45;
	protected static final int HEIGHT = 20;

	protected ERBox			m_erBox;
	protected JCheckBox		m_checkbox;
	protected EntityClass	m_ec;
	protected EntityInstance m_e;
	protected int			m_index;
	protected JLabel		m_label;
	
	protected	JPopupMenu	m_popup = null;

	public ShownEntityChkBox(ERBox erBox, EntityClass ec, int index, int count, int height, Font font)
	{
		Option			option;
		FlowLayout		flowLayout;
		JCheckBox		checkbox;
		EntityComponent entityComponent;
		JLabel			label;
		Dimension		d;
		int				bh = height * 2;
		int				bw = (bh * 4) / 3;
		Color			color;
		String			string;

		option     = Options.getDiagramOptions();
		flowLayout = new FlowLayout(FlowLayout.LEFT);
		flowLayout.setHgap(GAP);
		setLayout(new FlowLayout(FlowLayout.LEFT));

		m_erBox = erBox;
		m_ec    = ec;
		m_index = index;

		m_checkbox = checkbox = new JCheckBox();
		//		checkbox.setBackground(Color.WHITE);
		checkbox.setBorderPaintedFlat(false);
		checkbox.setSelected(erBox.getFlag(ec));
		checkbox.setEnabled(true);
		checkbox.setVisible(true);
		add(checkbox);

		m_e = ec.newEntity("");
		m_e.orMark(EntityInstance.LEGEND_MARK);

		// This entity isn't counted as part of the set of members in its class
		ec.decrementMembers();

		// Explicitly create entity component as a plain component without listeners etc
		// Otherwise automagical creation of this component might assign it listeners,
		// tooltips etc that were not required
		entityComponent = m_e.neededPlainComponent();

		entityComponent.setSize(bw, bh);
		entityComponent.setLocation(0, 0);
		entityComponent.setPreferredSize(new Dimension(bw, bh));
		add(entityComponent);

		string = ec.getLabel();
		if (count >= 0)	{
			string += " [" + count + "]";
		}

		m_label = label = new JLabel(string);
		
		if (!option.isLegendLabelBlack()) {
			color = ec.getInheritedLabelColor();
		} else {
			color = Color.BLACK;
		}
		label.setForeground(color);
		add(label);

		setToolTipText(ec.getDescription());
		
		setFont(font);
		addItemListener(this);
		addMouseListener(this);
	}

	public void setFont(Font font)
	{
		m_label.setFont(font);
	}

	public void addItemListener(ItemListener listener)
	{
		m_checkbox.addItemListener(listener);
	}

	public boolean isSelected()
	{
		return (m_checkbox.isSelected());
	}

	public void doClick()
	{
		m_checkbox.doClick();
	}

	public int getIndex()
	{
		return (m_index);
	}

	public boolean isActive()
	{
		return m_erBox.getFlag(m_ec);
	}

	public void setActive(boolean value)
	{
		Option		option = Options.getDiagramOptions();
		ERBox		erBox  = m_erBox;
		EntityClass	ec     = m_ec;

		if (erBox.getFlag(ec) != value)	{
			LandscapeEditorCore ls = erBox.getLs();
			if (erBox.setFlag(ec, value, option.isShowInheritance()) == -1) {
				erBox.syncEntityClasses();
			}
			ls.refillDiagram();
		}
	}

	public void sync()
	{
		m_checkbox.setSelected(isActive());
	}

	public void itemStateChanged(ItemEvent ev)
	{
		//		System.out.println("ShownEntityChkBox.itemStateChanged " + ev.getStateChange() + " " + isSelected());
		setActive(ev.getStateChange() == ItemEvent.SELECTED);
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
		if (ev.isMetaDown()) {
			int			x     = ev.getX();
			int			y     = ev.getY();
			JPopupMenu	popup = m_popup;

			if (popup == null) {
				ERBox erBox = m_erBox;
				LandscapeEditorCore ls      = erBox.getLs();
				Diagram				diagram = ls.getDiagram();
				EntityClass			ec      = m_ec;
				JMenuItem mi;

				m_popup = popup = new JPopupMenu("Class menu");

				mi = new JMenuItem(erBox.g_editInheritanceRules_text);
				mi.addActionListener(new DisplayClassHierarchy(ls, ec, x, y));
				popup.add(mi);

				mi = new JMenuItem(erBox.g_editClassAttributes_text);
				mi.addActionListener(new EditClassAttributes(ls, ec));
				popup.add(mi);

				mi = new JMenuItem(erBox.g_editElisionRules_text);
				mi.addActionListener(new DisplayEditElisions(ls, ec));
				popup.add(mi);

				mi = new JMenuItem(ERBox.g_showUsage_text);
				mi.addActionListener(new ShowUsage(ls, ec));
				popup.add(mi);
			
				mi = new JMenuItem(erBox.g_showValidAttributes_text);
				mi.addActionListener(new ShowValidAttributes(ls, ec));
				popup.add(mi);

				mi = new JMenuItem(erBox.g_validateAttributes_text);
				mi.addActionListener(new CheckEntityAttributes(ls, ec));
				popup.add(mi);

				mi = new JMenuItem(erBox.g_createEntitiesOfThisClass_text);
				mi.addActionListener(new SetDefaultEntityClass(ls, ec));
				popup.add(mi);
				
				if (m_ec != diagram.m_entityBaseClass) {
					mi = new JMenuItem("Delete entity class " + ec.getLabel());
					mi.addActionListener(new DeleteEntityClass(ls, ec));
					popup.add(mi);
				}
				erBox.add(popup);
			}
			FontCache.setMenuTreeFont(m_popup);

			//			Do.dump_menu(m_popup);
			popup.show(this, x, y);
		}
	}

	public void mouseReleased(MouseEvent ev)
	{
	}

	public String toString()
	{
		return "EntityChkBox " + m_label.getText();
	}
}

class EditRelationClassAttributes implements ActionListener
{
	private LandscapeEditorCore m_ls;
	private RelationClass m_relation;

	public EditRelationClassAttributes(LandscapeEditorCore ls, RelationClass relation) {
		m_ls = ls;
		m_relation = relation;
	}

	public void actionPerformed(ActionEvent ev)
	{
		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (m_ls.processMetaKeyEvent(ERBox.g_editClassAttributes_text))	{
				return;
		}	}
		EditAttribute.Create(m_ls, m_relation);
		m_ls.repaint();
	}
}

class ShowUsage implements ActionListener
{
	private LandscapeEditorCore m_ls;
	private LandscapeClassObject m_classObject;

	public ShowUsage(LandscapeEditorCore ls, LandscapeClassObject classObject) {
		m_ls          = ls;
		m_classObject = classObject;
	}

	public void actionPerformed(ActionEvent ev)
	{
		LandscapeEditorCore ls = m_ls;

		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (ls.processMetaKeyEvent(ERBox.g_showUsage_text)) {
				return;
		}	}
		ClassUsage.create(ls.getDiagram(), m_classObject);
}	}

class ShowConstraintsMatrix implements ActionListener
{
	private LandscapeEditorCore m_ls;
	private RelationClass m_rc;

	public ShowConstraintsMatrix(LandscapeEditorCore ls, RelationClass rc) {
		m_ls = ls;
		m_rc = rc;
	}

	public void actionPerformed(ActionEvent ev)
	{
		LandscapeEditorCore ls = m_ls;

		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (ls.processMetaKeyEvent(ERBox.g_editRelationConstraints_text)) {
				return;
		}	}
		EditConstraints.create(ls.getDiagram(), m_rc);
}	}

class ShowConstraintsClosure implements ActionListener
{
	private LandscapeEditorCore m_ls;
	private RelationClass m_rc;

	public ShowConstraintsClosure(LandscapeEditorCore ls, RelationClass rc)
	{
		m_ls = ls;
		m_rc = rc;
	}

	public void actionPerformed(ActionEvent ev)
	{
		LandscapeEditorCore ls = m_ls;

		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (ls.processMetaKeyEvent(ERBox.g_closureOfConstraints_text)) {
				return;
		}	}
		ClosureConstraints.create(ls.getDiagram(), m_rc);
}	}

class CheckRelationAttributes implements ActionListener
{
	private LandscapeEditorCore m_ls;
	private RelationClass m_rc;

	public CheckRelationAttributes(LandscapeEditorCore ls, RelationClass rc)
	{
		m_ls = ls;
		m_rc = rc;
	}

	public void actionPerformed(ActionEvent ev)
	{
		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (m_ls.processMetaKeyEvent(ERBox.g_validateAttributes_text))	{
				return;
		}	}
		m_ls.validateRelationAttributes(m_rc);
}	}

class CheckRelations implements ActionListener
{
	private LandscapeEditorCore m_ls;
	private RelationClass m_rc;

	public CheckRelations(LandscapeEditorCore ls, RelationClass rc)
	{
		m_ls = ls;
		m_rc = rc;
	}

	public void actionPerformed(ActionEvent ev)
	{
		LandscapeEditorCore ls = m_ls;

		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (ls.processMetaKeyEvent(ERBox.g_validateRelations_text)) {
				return;
		}	}
		ls.validateRelationAttributes(m_rc);
		ls.validateRelations(m_rc);
}	}

class ShowElisionIcons implements ActionListener
{
	private LandscapeEditorCore m_ls;
	private RelationClass		m_rc;

	public ShowElisionIcons(LandscapeEditorCore ls, RelationClass rc)
	{
		m_ls = ls;
		m_rc = rc;
	}

	public void actionPerformed(ActionEvent ev)
	{
		LandscapeEditorCore ls = m_ls;
		
		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (ls.processMetaKeyEvent(ERBox.g_showElisionIcons_text)) {
				return;
		}	}
		
		Option	option = Options.getDiagramOptions();
		
		option.setElisionIcon(m_rc.getNid());
		ls.repaintDiagram();
}	}

class HideElisionIcons implements ActionListener
{
	private LandscapeEditorCore m_ls;

	public HideElisionIcons(LandscapeEditorCore ls)
	{
		m_ls = ls;
	}

	public void actionPerformed(ActionEvent ev)
	{
		LandscapeEditorCore ls = m_ls;
		
		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (ls.processMetaKeyEvent(ERBox.g_hideElisionIcons_text)) {
				return;
		}	}
		
		Option	option = Options.getDiagramOptions();
		
		option.setElisionIcon(-1);
		ls.repaintDiagram();
}	}

class DeleteRelationClass implements ActionListener
{
	private LandscapeEditorCore m_ls;
	private RelationClass m_rc;

	public DeleteRelationClass(LandscapeEditorCore ls, RelationClass rc)
	{
		m_ls = ls;
		m_rc = rc;
	}

	public void actionPerformed(ActionEvent ev)
	{
		LandscapeEditorCore ls = m_ls;

		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (ls.processMetaKeyEvent("Delete Relation Class")) {
				return;
		}	}

		RelationClass rc = m_rc;
		Diagram diagram = ls.getDiagram();
		Enumeration en;
		RelationClass rc1;
		int cnt;
		String message = null;


		for (en = diagram.enumRelationClasses(); en.hasMoreElements(); ) {
			rc1 = (RelationClass)en.nextElement();
			if (rc != rc1 && rc1.directlyInheritsFrom(rc))	{
				if (message != null) {
					message += ", ";
				} else {
					message = "";
				}
				message += rc1.getLabel();
		}	}
		if (message != null) {
			message += " inherit from " + rc.getLabel();
		} else {
			diagram.recomputeCounts();
			cnt = rc.countMembers();
			if (cnt != 0) {
				message = rc.getLabel() + " has " + cnt + " instantiations";
		}	}
		if (message != null) {
			JOptionPane.showConfirmDialog(null, message, "Can't delete relation class", JOptionPane.DEFAULT_OPTION);
			return;
		}

		switch (JOptionPane.showConfirmDialog(null, "Delete class " + rc.getLabel(), "Delete all " + rc.getLabel() + " edges", JOptionPane.YES_NO_CANCEL_OPTION)) {
		case JOptionPane.YES_OPTION:
			diagram.doUpdateRemoveRelationClass(rc);
			break;
		case JOptionPane.NO_OPTION:
			return;
		default:
			return;
}	}	}

class SetDefaultRelationClass implements ActionListener
{
	private LandscapeEditorCore m_ls;
	private RelationClass m_rc;

	public SetDefaultRelationClass(LandscapeEditorCore ls, RelationClass rc)
	{
		m_ls = ls;
		m_rc = rc;
	}

	public void actionPerformed(ActionEvent ev)
	{
		LandscapeEditorCore ls = m_ls;

		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (ls.processMetaKeyEvent(ERBox.g_createEdgesOfThisClass_text)) {
				return;
		}	}
		ls.getDiagram().setDefaultRelationClass(m_rc);
}	}

class SetContainsRelation implements ActionListener
{
	private LandscapeEditorCore m_ls;

	public SetContainsRelation(LandscapeEditorCore ls)
	{
		m_ls = ls;
	}

	public void actionPerformed(ActionEvent ev)
	{
		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (m_ls.processMetaKeyEvent(ERBox.g_formsHierarchy_text))	{
				return;
		}	}

		RelationClass[]	  spanningClasses = SpanningClasses.getSpanningClasses(m_ls);
		
		if (spanningClasses != null) {
			Diagram diagram = m_ls.getDiagram();
			diagram.updateContainsClasses(spanningClasses);
}	}	}

// Used by query and legend box

class ShownRelnChkBox extends JComponent implements ItemListener, MouseListener
{
	public static final int GAP    = 5;
	public static final int WIDTH  = 45;
	public static final int HEIGHT = 20;

	protected ERBox			m_erBox;
	protected JCheckBox		m_checkbox = null;
	protected RelationClass m_rc;
	protected int			m_direction;
	protected int			m_index;
	protected JPopupMenu	m_popup;

	public ShownRelnChkBox(ERBox erBox, RelationClass rc, int direction, int index, int count, Font font)
	{
		Option		option;
		FlowLayout	flowLayout;
		Color		color;
		JCheckBox	checkbox;
		Arrow		arrow;
		JLabel		label;
		String		labelText;

		m_erBox     = erBox;
		m_rc        = rc;
		m_direction = direction;
		m_index     = index;
	
		option      = Options.getDiagramOptions();
		flowLayout  = new FlowLayout(FlowLayout.LEFT);
//		flowLayout.setHgap(GAP);
		setLayout(flowLayout);

		if (0 <= m_index) {
			m_checkbox = checkbox = new JCheckBox();
//			checkbox.setBackground(Color.WHITE);
			checkbox.setBorderPaintedFlat(false);

			checkbox.setSelected(erBox.getFlag(rc, direction));
			checkbox.setEnabled(true);
			checkbox.setVisible(true);
			add(checkbox);
		}

		arrow = new Arrow(WIDTH, HEIGHT);
		color = rc.getInheritedObjectColor();
		arrow.setForeground(color);		// Defaults to parents foreground color
		if (option.isVariableArrowColor()) {
			color  = rc.getInheritedArrowColor();
			arrow.setHeadColor(color);
		}
		arrow.setStyle(rc.getInheritedStyle());
		add(arrow);

		if (direction == LandscapeClassObject.DIRECTION_REVERSED) {
			labelText = rc.getReversedLabel();
			if (labelText == null) {
				labelText = rc.getLabel() + "(r)";
			}
		} else {
			labelText   = rc.getLabel();
		}
		if (index >= 0) {
			labelText += " (" + index + ")";
		}
		if (count >= 0) {
			labelText += " [" + count + "]";
		}
		label = new JLabel(labelText);
		if (!option.isLegendLabelBlack()) {
			color = rc.getInheritedLabelColor();
		} else {
			color = Color.BLACK;
		}
		label.setFont(font);
		label.setForeground(color);
		add(label);

		setToolTipText(rc.getDescription());		

		addItemListener(this);
		addMouseListener(this);
	}

	public void addItemListener(ItemListener listener)
	{
		if (m_checkbox != null) {
			m_checkbox.addItemListener(listener);
	}	}

	public boolean isSelected()
	{
		if (m_checkbox == null) {
			return false;
		}
		return (m_checkbox.isSelected());
	}

	public void doClick()
	{
		if (m_checkbox != null) {
			m_checkbox.doClick();
	}	}

	public int getIndex()
	{
		return (m_index);
	}

	public boolean isActive()
	{
		return (m_erBox.getFlag(m_rc, m_direction));
	}

	public void setActive(boolean value)
	{
		Option			option = Options.getDiagramOptions();
		ERBox			erBox  = m_erBox;
		RelationClass	rc     = m_rc;

		if (erBox.getFlag(rc, m_direction) != value)	{
			LandscapeEditorCore ls = erBox.getLs();
			if (erBox.setFlag(m_rc, m_direction, value, option.isShowInheritance()) == -1) {
				erBox.syncRelationClasses();
			}
			ls.refillDiagram();
	}	}

	public void sync()
	{
		if (m_checkbox != null) {
			m_checkbox.setSelected(isActive());
	}	}

	public void itemStateChanged(ItemEvent ev)
	{
		//		System.out.println("ShownRelnChkBox.itemStateChanged " + ev.getStateChange() + " " + isSelected());
		setActive(ev.getStateChange() == ItemEvent.SELECTED);
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
		if (ev.isMetaDown()) {

			ERBox				erBox   = m_erBox;
			LandscapeEditorCore ls      = erBox.getLs();
			Diagram				diagram = ls.getDiagram();
			int					x       = ev.getX();
			int					y       = ev.getY();
			RelationClass		rc      = m_rc;
			JPopupMenu			popup;
			JMenuItem			mi;

			m_popup = popup = new JPopupMenu("Relation menu");

			mi = new JMenuItem(ERBox.g_editInheritanceRules_text);
			mi.addActionListener(new DisplayClassHierarchy(ls, rc, x, y));
			popup.add(mi);

			mi = new JMenuItem(ERBox.g_editClassAttributes_text);
			mi.addActionListener(new EditRelationClassAttributes(ls, rc));
			popup.add(mi);

			mi = new JMenuItem(ERBox.g_showUsage_text);
			mi.addActionListener(new ShowUsage(ls, rc));
			popup.add(mi);
			
			mi = new JMenuItem(ERBox.g_editRelationConstraints_text);
			mi.addActionListener(new ShowConstraintsMatrix(ls, rc));
			popup.add(mi);
			
			mi = new JMenuItem(ERBox.g_editElisionRules_text);
			mi.addActionListener(new DisplayEditElisions(ls, rc));
			popup.add(mi);

			mi = new JMenuItem(ERBox.g_showElisionIcons_text);
			mi.addActionListener(new ShowElisionIcons(ls, rc));
			popup.add(mi);

			mi = new JMenuItem(ERBox.g_closureOfConstraints_text);
			mi.addActionListener(new ShowConstraintsClosure(ls, rc));
			popup.add(mi);
						
			mi = new JMenuItem(ERBox.g_showValidAttributes_text);
			mi.addActionListener(new ShowValidAttributes(ls, rc));
			popup.add(mi);

			mi = new JMenuItem(ERBox.g_validateRelations_text);
			mi.addActionListener(new CheckRelations(ls, rc));
			popup.add(mi);
			
			mi = new JMenuItem(ERBox.g_validateAttributes_text);
			mi.addActionListener(new CheckRelationAttributes(ls, rc));
			popup.add(mi);

			if (rc.getContainsClassOffset() < 0)	{
				// Can't delete classes that are currently involved in forming the spanning tree
				if (rc != diagram.m_relationBaseClass) {
					mi = new JMenuItem("Delete relation class " + rc.getLabel());
					mi.addActionListener(new DeleteRelationClass(ls, rc));
					popup.add(mi);
				}

				mi = new JMenuItem(ERBox.g_createEdgesOfThisClass_text);
				mi.addActionListener(new SetDefaultRelationClass(ls, rc));
				popup.add(mi);
			}

			erBox.customRelationOptions(popup, rc);

			FontCache.setMenuTreeFont(popup);
			erBox.add(popup);
			//			Do.dump_menu(m_popup);
			popup.show(this, x, y);
	}	}

	public void mouseReleased(MouseEvent ev)
	{
	}
}

class SetEntitiesFlag implements ActionListener
{
	private ERBox	m_erBox;
	private boolean	m_value;

	public SetEntitiesFlag(ERBox erBox, boolean value)
	{
		m_erBox  = erBox;
		m_value  = value;
	}

	public void actionPerformed(ActionEvent ev)
	{
		ERBox				erBox = m_erBox;
		LandscapeEditorCore	ls    = erBox.getLs();
		
		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (ls.processMetaKeyEvent("Set Entities " + erBox.getFlagName())) {
				return;
		}	}

		Diagram				diagram = ls.getDiagram();
		Enumeration			en      = diagram.enumEntityClasses();
		boolean				value   = m_value;
		EntityClass			ec;
		boolean				ret     = false;

		for (; en.hasMoreElements(); ) {
			ec = (EntityClass) en.nextElement();
			if (erBox.setFlag(ec, value, false) != 0) {
				ret = true;
		}	}
		if (ret) {	
			diagram.revalidate();
			erBox.fill();
	}	}
}

class SetRelationsFlag implements ActionListener
{
	ERBox	m_erBox;
	int		m_direction;
	boolean	m_value;

	public SetRelationsFlag(ERBox erBox, int direction, boolean value)
	{
		m_erBox     = erBox;
		m_direction = direction;
		m_value     = value;
	}

	public void actionPerformed(ActionEvent ev)
	{
		ERBox				erBox = m_erBox;
		LandscapeEditorCore	ls    = erBox.getLs();

		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (ls.processMetaKeyEvent("Set Relations " + erBox.getFlagName())) {
				return;
		}	}

		Diagram				diagram   = ls.getDiagram();
		Enumeration			en        = diagram.enumRelationClasses();
		int					direction = m_direction;
		boolean				value     = m_value;
		RelationClass		rc;
		boolean				ret       = false;


		for (; en.hasMoreElements(); ) {
			rc = (RelationClass) en.nextElement();
			if (erBox.setFlag(rc, direction, value, false) != 0) {
				ret = true;
		}	}
		if (ret) {
			diagram.revalidate();
			erBox.fill();
	}	}
}

public class ERBox extends TabBox /* extends JComponent */ implements ChangeListener, TaListener, MouseListener 
{
	protected static final int MARGIN = 5;
	protected static final int GAP    = 5;
	protected static final int INDENT = 10;

	public static final String g_editInheritanceRules_text = "Edit inheritance rules";
	public static final String g_editClassAttributes_text  = "Edit Class Attributes";
	public static final String g_showValidAttributes_text  = "Show valid attributes";
	public static final String g_validateAttributes_text   = "Validate attributes";
	public static final String g_createEntitiesOfThisClass_text = "Create entities of this class";

	public static final String g_showUsage_text = "Show Usage";
	public static final String g_editRelationConstraints_text = "Edit relation constraints";
	public static final String g_closureOfConstraints_text = "Closure of constraints";
	public static final String g_validateRelations_text = "Validate relations";
	public static final String g_showElisionIcons_text = "Show Elision Icons";
	public static final String g_hideElisionIcons_text = "Hide Elision Icons";

	public static final String g_createEdgesOfThisClass_text = "Create relations of this class";
	public static final String g_formsHierarchy_text = "Forms Hierarchy";

	public static final String g_empty_classes         = "Empty Classes";
	public static final String g_member_counts         = "Member Counts";
	public static final String g_editElisionRules_text = "Edit Elision Rules";
	public static final String g_hierarchy_text        = "Inheritance Hierarchy";

	protected JLabel		m_ulabel;
	protected JLabel		m_elabel1 = null;
	protected JLabel		m_elabel2;
	protected JLabel		m_rlabel;	// TODO underline it
	protected JLabel		m_rlabel2;
	protected JLabel		m_footer1;
	protected JLabel		m_footer2;

	protected JLabel		m_clabel;

	protected boolean	m_refill    = false;
	protected boolean	m_badcnts   = false;

	protected int m_width;
	protected int m_height;
	
	// ------------------
	// JComponent methods
	// ------------------

/*
	public void setBounds(int x, int y, int width, int height)
	{
		super.setBounds(x,y,width,height);
		System.out.println("Legend size " + getBounds());
		return;
	}
*/
	public Dimension getPreferredSize()
	{
		return(getSize());
	}

/*
	public Dimension getMinimumSize()
	{
		return(getSize());
	}
*/
	
	public Dimension getMaximumSize()
	{
		return(getSize());
	}
	
	protected void add(JComponent component, int indent)
	{
		Dimension	d;
		int			indent1;
		int			width, height;

		d       = component.getPreferredSize();
		indent1 = MARGIN + indent;
		width   = indent1 + d.width;
		height  = d.height;
		if (width > m_width) {
//			System.out.println("Width=" + width + " for " + component);
			m_width = width;
		}
		component.setBounds(indent1, m_height, width, height);
		super.add(component);
		m_height += height;
	}

	protected void add(JComponent component)
	{
		add(component, 0);
	}

	// --------------
	// Public methods 
	// --------------

	public ERBox(LandscapeEditorCore ls, JTabbedPane tabbedPane, String title, String helpStr) 
	{
		super(ls, tabbedPane, title, helpStr);

		m_clabel = new JLabel("Contains Hierarchy");

		tabbedPane.addChangeListener(this);
		addMouseListener(this);
	}
	
	public Font getTextFont()
	{
		return null;
	}
	
	public void setComponentsTextFont(Font font)
	{
		if (m_elabel1 != null) {
			m_elabel1.setFont(font);
		}
		m_elabel2.setFont(font);
		m_footer1.setFont(font);
		m_footer2.setFont(font);
	}
	
	public void setComponentsTitleFont(Font font)
	{
		m_ulabel.setFont(font);
		m_rlabel.setFont(font);
		m_clabel.setFont(font);
	}	

	protected boolean helpEntityFlag()
	{
		return true;
	}
	
	protected boolean getFlag(EntityClass ec)
	{
		return false;
	}
	
	protected int setFlag(EntityClass ec, boolean value, boolean applyToSubclasses)
	{
		return 0;
	}

	protected boolean getFlag(RelationClass rc, int direction)
	{
		return false;
	}

	protected int setFlag(RelationClass rc, int direction, boolean value, boolean applyToSubclasses)
	{
		return 0;
	}
	
	protected String getFlagName()
	{
		return null;
	}
	
	protected boolean can_reverse()
	{
		return true;
	}
	
	protected void customFill()
	{
	}
	
	public void fill()
	{
/*		System.out.println("LegendBox.fill()");
		java.lang.Thread.dumpStack();
		System.out.println("-----");
*/
		removeAll();
		m_width  = 0;
		m_height = 0;

		if (isActive()) {
			Option				option         = Options.getDiagramOptions();
			LandscapeEditorCore	ls             = m_ls;
			Diagram				diagram        = ls.getDiagram();
			Font				textFont       = getTextFont();
			FontMetrics			fontMetrics    = getFontMetrics(textFont);
			int					h              = fontMetrics.getHeight();
			boolean				isHideEmpty    = option.isHideEmpty();
			boolean				isMemberCounts = option.isMemberCounts();
			boolean				isInheritance  = option.isShowInheritance();
			boolean				usesCounts     = isHideEmpty || isMemberCounts;
			int					n              = 0;
			int					cnt            = -1;
			RelationClass[]		containsClasses= diagram.getContainsClasses();
			Enumeration			en;
			EntityClass			ec;
			RelationClass		rc;
			int					dir;
			
			// Draw legend for entities

			m_height   += 10;
			add(m_ulabel);

			if (diagram != null) {
				if (usesCounts) {
					diagram.recomputeCounts();
				}

				n = 0;
				if (isInheritance) {
					for (en = diagram.enumEntityClassHierarchy(isHideEmpty); en.hasMoreElements(); ) {
						ec = (EntityClass)en.nextElement();
						m_height += 10;
						if (isMemberCounts)	{
							cnt = ec.countMembers();
						}
						add(new ShownEntityChkBox(this, ec, ++n, cnt, h, textFont), ec.getInheritanceDepth() * INDENT);
					}
				} else {
					for (en = diagram.enumEntityClassesInOrder(); en.hasMoreElements(); ) {
						ec = (EntityClass)en.nextElement();

						if (usesCounts)	{
							cnt = ec.countMembers();
							if (isHideEmpty && cnt == 0) {
								continue;
							}
							if (!isMemberCounts) {
								cnt = -1;
						}	}
						m_height += 10;
						add(new ShownEntityChkBox(this, ec, ++n, cnt, h, textFont));
				}	}
			
				m_height += 10;
				if (m_elabel1 != null) {
					add(m_elabel1);
				}
				add(m_elabel2);
				m_height += 20;
				add(m_rlabel);
				m_height += GAP;

				// Draw legend for relations

				n   = 0;
				cnt = -1;
				if (isInheritance) {
					for (en = diagram.enumRelationClassHierarchy(isHideEmpty); en.hasMoreElements(); ) {
						rc = (RelationClass)en.nextElement();
						if (isMemberCounts)	{
							cnt = rc.countMembers();
						}
						add(new ShownRelnChkBox(this, rc, LandscapeClassObject.DIRECTION_NORMAL, ++n, cnt, textFont), rc.getInheritanceDepth() * INDENT);
					}	
				} else {
					for (en = diagram.enumRelationClassesInOrder(); en.hasMoreElements(); )	{
						rc = (RelationClass)en.nextElement();
						if (usesCounts)	{
							cnt = rc.countMembers();
							if (isHideEmpty && cnt == 0) {
								continue;
							}
							if (!isMemberCounts) {
								cnt = -1;
						}	}
						add(new ShownRelnChkBox(this, rc, LandscapeClassObject.DIRECTION_NORMAL, ++n, cnt, textFont));
				}	}
				
				// Draw legend for reversed relations

				if (can_reverse()) {
					add(m_rlabel2);
					m_height += GAP;

					n   = 0;
					cnt = -1;
					if (isInheritance) {
						for (en = diagram.enumRelationClassHierarchy(isHideEmpty); en.hasMoreElements(); ) {
							rc = (RelationClass)en.nextElement();
							if (isMemberCounts)	{
								cnt = rc.countMembers();
							}
							add(new ShownRelnChkBox(this, rc, LandscapeClassObject.DIRECTION_REVERSED, ++n, cnt, textFont), rc.getInheritanceDepth() * INDENT);
						}	
					} else {
						for (en = diagram.enumRelationClassesInOrder(); en.hasMoreElements(); )	{
							rc = (RelationClass)en.nextElement();
							if (usesCounts)	{
								cnt = rc.countMembers();
								if (isHideEmpty && cnt == 0) {
									continue;
								}
								if (!isMemberCounts) {
									cnt = -1;
							}	}
							add(new ShownRelnChkBox(this, rc, LandscapeClassObject.DIRECTION_REVERSED, ++n, cnt, textFont));
				}	}	}
								
				// Now show contains classes 				

				if (containsClasses != null) {
					m_height += 10;
					add(m_clabel);
					for (n = 0; n < containsClasses.length; ++n) {
						m_height += GAP;
						rc        = containsClasses[n];
						if (rc.getShown() != LandscapeClassObject.DIRECTION_REVERSED) {
							dir = LandscapeClassObject.DIRECTION_NORMAL;
						} else {
							dir = LandscapeClassObject.DIRECTION_REVERSED;
						}
						add(new ShownRelnChkBox(this, rc, dir, -1, -1, textFont));
			}	}	}
			
			
			m_height += 20;
			add(m_footer1);
			add(m_footer2);

			// Max width is computed in the add() method
			m_height += 10;
			customFill();
		}
		setBounds(0, 0, m_width, m_height);
		
		// Need to revalidate to get labels to show with text
		revalidate();
	}
	
	public void syncEntityClasses()
	{
		int i, size;
		Object o;

		size = getComponentCount();
		for (i = 0; i < size; ++i)	{
			o = getComponent(i);
			if (o instanceof ShownEntityChkBox)	{
				((ShownEntityChkBox)o).sync();
	}	}	}

	public void syncRelationClasses()
	{
		int i, size;
		Object o;

		size = getComponentCount();
		for (i = 0; i < size; ++i) {
			o = getComponent(i);
			if (o instanceof ShownRelnChkBox) {
				((ShownRelnChkBox)o).sync();
	}	}	}

	public void toggleFlags(int key)
	{
		int cnt = getComponentCount();
		int i;
		Component c;
		boolean ns;

		ns = true;
		if (key == 0) {
			for (i = 0; i < cnt; ++i) {
				c = getComponent(i);
				if (c instanceof ShownRelnChkBox) {
					if (((ShownRelnChkBox)c).getIndex() == 1) {
						// Change everything the same way as the way the first one changed
						ns = !((ShownRelnChkBox)c).isActive();
						break;
			}	}	}

			for (i = 0; i < cnt; ++i) {
				c = getComponent(i);
				if (c instanceof ShownRelnChkBox) {
					if (ns != ((ShownRelnChkBox)c).isActive()) {
						((ShownRelnChkBox)c).doClick();
			}	}	}
		} else {
			for (i = 0; i < cnt; ++i) {
				c = getComponent(i);
				if (c instanceof ShownRelnChkBox) {
					if (((ShownRelnChkBox)c).getIndex() == key) {
						((ShownRelnChkBox)c).doClick();
						break;
		}	}	}	}
	}

	protected void customOptions(JPopupMenu popupMenu)
	{
	}
	
	protected void customRelationOptions(JPopupMenu popup, RelationClass rc)
	{
	}

	protected void doRightPopup(MouseEvent ev)
	{
		Option				option  = Options.getDiagramOptions();
		LandscapeEditorCore ls      = m_ls;
		Diagram				diagram = ls.getDiagram();
		int					x       = ev.getX();
		int					y       = ev.getY();
		Enumeration			en;
		EntityClass			ec;
		RelationClass		rc;
		JPopupMenu			popupMenu;
		JMenuItem			mi;
		String				string;

		popupMenu = new JPopupMenu("Options");

		string = getFlagName();

		for (en = diagram.enumEntityClasses(); en.hasMoreElements(); )	{
			ec = (EntityClass)en.nextElement();
			if (getFlag(ec)) {
				mi = new JMenuItem("No entities " + string);
				mi.addActionListener(new SetEntitiesFlag(this, false));
				popupMenu.add(mi);
				break;
		}	}

		for (en = diagram.enumEntityClasses(); en.hasMoreElements(); ) {
			ec = (EntityClass)en.nextElement();
			if (!getFlag(ec)) {
				mi = new JMenuItem("All entities " + string);
				mi.addActionListener(new SetEntitiesFlag(this, true));
				popupMenu.add(mi);
				break;
		}	}

		for (en = diagram.enumRelationClasses(); en.hasMoreElements(); ) {
			rc = (RelationClass)en.nextElement();
			if (getFlag(rc, LandscapeClassObject.DIRECTION_NORMAL)) {
				mi = new JMenuItem("No relations " + string);
				mi.addActionListener(new SetRelationsFlag(this, LandscapeClassObject.DIRECTION_NORMAL, false));
				popupMenu.add(mi);
				break;
		}	}

		for (en = diagram.enumRelationClasses(); en.hasMoreElements(); ) {
			rc = (RelationClass)en.nextElement();
			if (!getFlag(rc, LandscapeClassObject.DIRECTION_NORMAL)) {
				mi = new JMenuItem("All relations " + string);
				mi.addActionListener(new SetRelationsFlag(this, LandscapeClassObject.DIRECTION_NORMAL, true));
				popupMenu.add(mi);
				break;
		}	}
		
		if (can_reverse()) {
			for (en = diagram.enumRelationClasses(); en.hasMoreElements(); ) {
				rc = (RelationClass)en.nextElement();
				if (getFlag(rc, LandscapeClassObject.DIRECTION_REVERSED)) {
					mi = new JMenuItem("No reversed relations " + string);
					mi.addActionListener(new SetRelationsFlag(this, LandscapeClassObject.DIRECTION_REVERSED, false));
					popupMenu.add(mi);
					break;
			}	}

			for (en = diagram.enumRelationClasses(); en.hasMoreElements(); ) {
				rc = (RelationClass)en.nextElement();
				if (!getFlag(rc, LandscapeClassObject.DIRECTION_REVERSED)) {
					mi = new JMenuItem("All reversed relations " + string);
					mi.addActionListener(new SetRelationsFlag(this, LandscapeClassObject.DIRECTION_REVERSED, true));
					popupMenu.add(mi);
					break;
		}	}	}

		if (option.isHideEmpty()) {
			string = "Show ";
		} else {
			string = "Hide ";
		}

		mi = new JMenuItem(string + g_empty_classes);
		mi.addActionListener(new ToggleEmptyClasses());
		popupMenu.add(mi);

		if (option.isMemberCounts()) {
			string = "Hide ";
		} else {
			string = "Show ";
		}

		mi = new JMenuItem(string + g_member_counts);
		mi.addActionListener(new ToggleMemberCounts());
		popupMenu.add(mi);


		mi = new JMenuItem(g_editElisionRules_text);
		mi.addActionListener(new DisplayEditElisions(ls, null));
		popupMenu.add(mi);

		if (option.getElisionIcon() >= 0) {
			mi = new JMenuItem(g_hideElisionIcons_text);
			mi.addActionListener(new HideElisionIcons(ls));
			popupMenu.add(mi);
		}
		
		if (option.isShowInheritance()) {
			string = "Hide ";
		} else {
			string = "Show ";
		}

		mi = new JMenuItem(string + g_hierarchy_text);
		mi.addActionListener(new ToggleHierarchy());
		popupMenu.add(mi);
			
		customOptions(popupMenu);

		mi = new JMenuItem(ERBox.g_formsHierarchy_text);
		mi.addActionListener(new SetContainsRelation(ls));
		popupMenu.add(mi);

		FontCache.setMenuTreeFont(popupMenu);
		add(popupMenu);
		popupMenu.show(this, x, y);
		//		Do.dump_menu(popupMenu);
		remove(popupMenu);
	}
	
	class ToggleEmptyClasses implements ActionListener
	{
		public ToggleEmptyClasses()
		{
		}

		public void actionPerformed(ActionEvent ev)
		{
			Option	option = Options.getDiagramOptions();
			
			if ((ev.getModifiers() & ActionEvent.META_MASK) != 0)
			{
				if (m_ls.processMetaKeyEvent(g_empty_classes))
				{
					return;
				}
			}
			option.setHideEmpty(!option.isHideEmpty());
			fill();
		}
	}

	class ToggleMemberCounts implements ActionListener
	{
		public ToggleMemberCounts()
		{
		}

		public void actionPerformed(ActionEvent ev)
		{
			if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
				if (m_ls.processMetaKeyEvent(g_member_counts)) {
					return;
				}
			}
			Option option = Options.getDiagramOptions();
			
			option.setMemberCounts(!option.isMemberCounts());
			fill();
	}	}


	class ToggleHierarchy implements ActionListener
	{
		public ToggleHierarchy()
		{
		}

		public void actionPerformed(ActionEvent ev)
		{
			if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
				if (m_ls.processMetaKeyEvent(g_hierarchy_text)) {
					return;
				}
			}
			Option	option = Options.getDiagramOptions();
			
			option.setShowInheritance(!option.isShowInheritance());
			fill();
	}	}

	public void fontChanged()
	{
		fill();
	}
	
	// ChangeListener interface

	public void stateChanged(ChangeEvent e)
	{
		fill();
	}

	// TaListener interface
	
	public void diagramChanging(Diagram diagram)
	{
	}

	public void diagramChanged(Diagram diagram, int signal)
	{
		fill();
	}

	public void updateBegins()
	{
	}

	public void updateEnds()
	{
		if (m_badcnts) {
			m_badcnts = false;
			if (!m_refill) {
				Option	option = Options.getDiagramOptions();

				if (!option.isMemberCounts() && !option.isHideEmpty()) {
					return;
			}	}
		} else if (!m_refill) {
			return;
		}

		m_refill = false;
		fill();
	}		

	public void entityClassChanged(EntityClass ec, int signal)
	{
		m_refill = true;
	}

	public void relationClassChanged(RelationClass rc, int signal)
	{
		m_refill = true;
	}

	public void entityParentChanged(EntityInstance e, EntityInstance parent, int signal)
	{
		m_badcnts = true;
	}

	public void relationParentChanged(RelationInstance ri, int signal)
	{
		m_badcnts = true;
	}

/*
	public void entityInstanceChanged(EntityInstance e, int signal)
	{
	}

	public void relationInstanceChanged(RelationInstance ri, int signal)
	{
	}
*/

	// MouseListener interface
	
	public void mouseClicked(MouseEvent ev)
	{

		if (ev.isMetaDown())
		{
			doRightPopup(ev);
		}
	}

	public void mouseEntered(MouseEvent ev)
	{
	}

	public void mouseExited(MouseEvent e)
	{
	}

	public void mousePressed(MouseEvent ev)
	{
	}

	public void mouseReleased(MouseEvent ev)
	{
	}
}



