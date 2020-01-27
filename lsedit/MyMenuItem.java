package lsedit;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

class MyMenuItem extends JMenuItem implements ActionListener
{
	ToolBarEventHandler	m_handler;
	int					m_key;
	int					m_modifiers;
	Object				m_object;

	public MyMenuItem(JComponent menu, String label, ToolBarEventHandler handler, int modifiers, int key, String tooltipHelp)
	{
		super(label);
		m_handler   = handler;
		m_object    = null;
		if (menu instanceof JMenu) {
			((JMenu) menu).add(this);
		} else {
			((JPopupMenu) menu).add(this);
		}

		/* Utter stupidity -- the set accelerator wants the Upper case value of a letter even if it is lower
		 * case and VK_F1 is defined to have the same value as a 'p'.  So if a lower case 'p' is passed in
		 * it is ambiguous if this is really a 'p' or a F1.
		 */

		if (tooltipHelp != null) {
			setToolTipText(tooltipHelp);
		}
		addActionListener(this);

		if (modifiers == -1) {
			modifiers = 0;
		} else {
			if (key <= 'Z') {
				if (key >= 'A') {
					modifiers |= Event.SHIFT_MASK;
				}
			} else if (key <= 'z') {
				if (key >= 'a') {
					modifiers &= ~Event.SHIFT_MASK;
					key += 'A' - 'a';
				}
			} else if (key >= Do.FUNCTION_KEY) {
				key -= Do.FUNCTION_KEY;
			}
			setAccelerator(KeyStroke.getKeyStroke(key, modifiers));
		}
		m_modifiers = modifiers;
		m_key       = key;
	}

	public void setObject(Object object)
	{
		m_object = object;
	}

	public void actionPerformed(ActionEvent ev)
	{
//		System.out.println("MenuItem clicked");
		if ((ev.getModifiers() & ActionEvent.META_MASK) != 0) {
			if (m_handler.processMetaKeyEvent(getText())) {
				return;
			}
		}

		m_handler.processKeyEvent(m_key, m_modifiers, m_object);
}	}
