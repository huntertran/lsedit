package lsedit;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JComponent;

class Cardinal extends JComponent {

	protected final static int MARGIN = 5; 

	protected int	m_cnt;
	protected int	m_w;	// Width and height of text

	protected static	Font		m_font = null;
	public	  static	FontMetrics	m_fm   = null;
	public	  static	int			m_h;
	
	// --------------
	// Object methods
	// --------------

	public String toString()
	{
		return("Cardinal " + m_cnt);
	}

	// ------------------
	// JComponent methods
	// ------------------

	public void setBackground(Color color)
	{	
		super.setBackground(color);
		setForeground(ColorCache.getInverse(color.getRGB()));
	}

	public void paintComponent(Graphics g)
	{
		int width, height;

//		System.out.println("Cardinal.paint cardinal Component()");
		
		width  = getWidth();
		height = getHeight();

		g.setFont(m_font);
		g.setColor(getBackground());
		g.fillOval(0, 0, width, height);
		g.setColor(Color.black);
		g.drawOval(0, 0, width-1, height-1);
		g.setColor(getForeground());
		g.drawString(""+m_cnt, 2, m_h - 2);

//		System.out.println("Cardinal.paintComponent() done");
	}

	public void removeNotify()
	{
		super.removeNotify();
		setSize(0,0);
	}

	// ----------------
	// Cardinal methods
	// ----------------

	public Cardinal()
	{
		super();
		if (m_font == null) {
			m_font = Options.getTargetFont(Option.FONT_CARDINALS);
		}
		setFont(m_font);
	}

	public static void setTextFont(Font font)
	{
		if (m_font != font) {
			m_font = font;
			m_fm   = null;
	}	}

	public void reset()
	{
		m_cnt = 0;
	}

	public void sum(RelationInstance ri)
	{
		m_cnt += ri.getFrequency();

//		System.out.println(ri + " + " + ri.getFrequency() + " " + this);
	}

	public void known()
	{
		setToolTipText("" + m_cnt);
	}

	public int getCnt()
	{
		return(m_cnt);
	}
	
	// Center at top the cardinal

	public void setCenterTop(int x, int y, int width, int height, RelationClass rc)
	{
		double factor = rc.getRelativeIOfactor();

//		System.out.println("setCenterTop ");

		setFont(m_font);

		if (m_fm == null) {
			m_fm = getFontMetrics(m_font);
			m_h  = m_fm.getAscent();
		}
		x   += ((double) width) * factor;
		y   += height + MARGIN;
		m_w  = m_fm.stringWidth("" + m_cnt);
		this.setBounds(x - (m_w/2) - 2, y, m_w + 4, m_h + 4);
	}
}
