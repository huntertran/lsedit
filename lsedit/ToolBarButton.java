package lsedit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JButton;

abstract public class ToolBarButton extends JButton implements MouseListener,MouseMotionListener
{
	abstract protected void paintIcon(Graphics gc);
	abstract protected String getDesc();
	abstract protected String getHelpString();

	protected final static double ARROW_L  = 5.0;
	protected final static double ARROW_TH = 0.40; 

	protected static final Font font  = FontCache.get("Helvetica", Font.PLAIN, 12);
	protected static final Font font1 = FontCache.get("Helvetica", Font.PLAIN, 8);

	public static final int MARGIN = 2;
	public static final int HEIGHT = 24;
	public static final int WIDTH  = 40;

	protected boolean depressed  = false;
	protected boolean mouseIn	 = false;
	protected boolean mouseDown  = false;

	private int		m_key      = 0;
	private int		m_modifier = 0;

	protected ToolBarEventHandler teh;

	protected void setKeystroke(int modifiers, int key)
	{
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
		m_key      = key;
		m_modifier = modifiers;
	}

	public static void drawEdge(Graphics g, int x1, int y1, int x2, int y2) 
	{
		g.drawLine(x1, y1, x2, y2);

		double dx = x1 - x2;
		double dy = y1 - y2;

		double theta = Math.atan2(dy, dx);

		double len = Math.min(ARROW_L, Math.sqrt(dx*dx + dy*dy));
		double ax = x2 + len*Math.cos(theta-ARROW_TH);
		double ay = y2 + len*Math.sin(theta-ARROW_TH);
		double bx = x2 + len*Math.cos(theta+ARROW_TH);
		double by = y2 + len*Math.sin(theta+ARROW_TH);

		int[] x = new int[3];
		int[] y = new int[3];

		x[0] = (int) Math.round(x2);
		y[0] = (int) Math.round(y2);
		x[1] = (int) Math.round(ax);
		y[1] = (int) Math.round(ay);
		x[2] = (int) Math.round(bx);
		y[2] = (int) Math.round(by);

		g.fillPolygon(x, y, 3);
	}

	public ToolBarButton(ToolBarEventHandler teh) {

		this.teh = teh;

		setSize(WIDTH, HEIGHT);

		if (teh != null) {
			addMouseListener(this);
			addMouseMotionListener(this);
			setAlignmentY(CENTER_ALIGNMENT);
			setToolTipText(getDesc());
	}	}

	public void paintComponent(Graphics gc) 
	{
		// Paint button base and character

		Dimension dim = getSize();

		gc.setColor(getBackground());
		gc.fillRect(0, 0, dim.width, dim.height);

		if (!isEnabled()) {
			gc.setColor(Color.black);
			paintIcon(gc);
		} else {

			gc.setColor(getBackground());

			if (depressed) {
				gc.draw3DRect(0, 0, dim.width-1, dim.height-1, true);
			} else if (mouseIn) {
				gc.draw3DRect(0, 0, dim.width-1, dim.height-1, !mouseDown);
			}

			gc.setColor(Color.black);

			paintIcon(gc);

			String	str = null;
			int		key = m_key;

			int dw = 0;

			if (key >= 'A' && key <= 'Z') {
				int modifier = m_modifier;

				if ((modifier & Event.ALT_MASK) != 0) {
					str = "Alt-";
				} else {
					str = "";
				}
				if ((modifier & Event.SHIFT_MASK) == 0) {
					key += 'a' - 'A';
				}
				str += ((char) key);
				dw = 0;
			} else if (key > 0 && key < 27) {
				str = "^" + ((char) (m_key+64));
				dw = 2;
			}

			if (str != null) {
				gc.setFont(font);
				FontMetrics fm = gc.getFontMetrics();

				int w = fm.stringWidth(str);
				int h = fm.getHeight();

				gc.drawString(str, dim.width - w + dw - MARGIN*2, dim.height - (dim.height - h) / 2 - MARGIN);
		}	}
	}

	public Dimension getPreferredSize()
	{
		return (new Dimension(WIDTH, HEIGHT));
	}

	public Dimension getMinimumSize()
	{
		return(getPreferredSize());
	}

	public Dimension getMaximumSize()
	{
		return(getPreferredSize());
	}

	// Generic 	MouseListener interface

	public void mouseClicked(MouseEvent e)
	{
	}

	public void mouseEntered(MouseEvent ev)
	{
		int x = ev.getX();
		int y = ev.getY();
	
		teh.showInfo(getDesc());
		mouseIn = true;
		repaint();
	}

	public void mouseExited(MouseEvent e)
	{
		mouseIn = false;
		repaint();
	}

	public void mousePressed(MouseEvent ev)
	{
		mouseDown = true;		
		repaint();
	}

	public void mouseReleased(MouseEvent ev)
	{
		if (mouseDown) {
			if (!ev.isMetaDown() || !teh.processMetaKeyEvent(getHelpString())) {
				teh.processKeyEvent(m_key, m_modifier, null);
			}
			mouseDown = false;
		}
		repaint();
	}

	// Generic MouseMotionListener interface

	public void mouseDragged(MouseEvent ev)
	{
		// Prevent event going to parent
	}

	public void mouseMoved(MouseEvent ev)
	{
		// Prevent event going to parent
	}

}





