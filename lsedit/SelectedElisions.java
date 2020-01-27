package lsedit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

class SelectedElisions extends JLabel implements TableCellRenderer, Icon {

	public static final int DST			 = 0x01;	// Type DST_ELISION      = 0
	public static final int SRC			 = 0x02;	// Type SRC_ELISION      = 1
	public static final int ENTERS		 = 0x04;	// Type ENTERING_ELISION = 2
	public static final int EXITS		 = 0x08;	// Type EXITING_ELISION  = 3
	public static final int INTERNAL	 = 0x10;	// Type INTERNAL_ELISION = 4
	public static final int CONTAINS	 = 0x20;	// Type CLOSED_ELISION   = 5
	public static final int CHANGE   	 = 0x40;	// Type OK if false      = 6
	public static final int BOX          = 0x80;	// Type BOX_ELISION      = 6
	public static final int ALL_ROW      = 0x100;
	public static final int ALL_COL      = 0x200;

	public static final int ELIDED     = INTERNAL | DST | ENTERS | SRC |EXITS;
	public static final int ALL_ELIDED = ELIDED   | CONTAINS;
	public static final int SUMMARY    = ALL_ROW  | ALL_COL;

	/* Used for summary types */

	public static final int AND_ELISIONS  = 0x1000;
	public static final int OR_ELISIONS   = 0x2000;
	public static final int NAND_ELISIONS = 0x4000;

	public static final int MODE          = AND_ELISIONS | OR_ELISIONS | NAND_ELISIONS;


	private static final int GAP       = 4;

	private	int				m_elisions = 0;

	public SelectedElisions(int elisions)
	{
		Font		font = Options.getTargetFont(Option.FONT_CLOSED);
		Font		bold = font.deriveFont(Font.BOLD);
		
		setFont(font);
		setIcon(this);
		
		m_elisions = elisions;
		
		setSize(ToolBarButton.WIDTH*2, ToolBarButton.HEIGHT*2);
	}
	
	public int getElisions()
	{
		return m_elisions;
	}

	public void setElisions(int elisions)
	{

		m_elisions = (elisions & ~(BOX|SUMMARY)) | (m_elisions & (BOX|SUMMARY));

		setToolTipText(toString());
	}

	public void updateElisions(int elisions, int mode) 
	{
		int elisions1 = elisions;

		if (mode == SelectedElisions.AND_ELISIONS) {
			elisions1 &= m_elisions;
		} else if (mode == SelectedElisions.OR_ELISIONS) {
			elisions1 |= m_elisions;
		} else if (mode == SelectedElisions.NAND_ELISIONS) {
			elisions1  = ~elisions1;
			elisions1 &= m_elisions;
		}
		setElisions(elisions1);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		return this;
	} 
 
	// Icon interface (used to paint image as icon)

	public int getIconWidth()
	{
		return(getWidth());
	}

	public int getIconHeight()
	{
		return(getHeight());
	}

	public void paintIcon(Component c, Graphics g, int x, int y)
	{
		int		w        = getWidth();
		int		h        = getHeight();
		int		x1       = x + GAP;      // x+gap -> x + width1
		int		y1       = y + h/3;
		int		width1   = w - 2 * GAP;
		int		height1  = (h * 2)/3 - GAP;
		int		elisions = m_elisions;
		int		x2, y2, x3, y3;
		String	text;

		if ((elisions & CHANGE) == 0) {
			if ((elisions & SUMMARY) == 0) {
				g.setColor(Color.RED);
				g.drawString("OK", x, y + h/2);
			}
		} else {
			if ((elisions & CONTAINS) == 0) {
				// Closed box
				g.setColor(Color.cyan);
				g.fillRect(x1, y1, width1, height1);
			}

			g.setColor(Color.black);
			g.drawRect(x1, y1, width1, height1);

			if ((elisions & INTERNAL) == 0) {
				x2 = x1 + (width1/3);
				y2 = y1 + (height1/2);
				x3 = x1 + (width1 * 2)/3;
				ToolBarButton.drawEdge(g, x2, y2, x3, y2); 
			}

			if ((elisions & SRC) == 0) {
				x2 = x1 + (width1/5);
				y3 = y + GAP;
				ToolBarButton.drawEdge(g, x2, y1, x2, y3);
			}

			if ((elisions & EXITS) == 0) {
				x2 = (x1 + (2*width1)/5);
				y2 = y1 + (height1/3);
				y3 = y + GAP;
				ToolBarButton.drawEdge(g, x2, y2, x2, y3);
			}

			if ((elisions & ENTERS) == 0) {
				x2 = x1 + (3*width1)/5;
				y2 = y1 + (height1/3);
				y3 = y + GAP;
				ToolBarButton.drawEdge(g, x2, y3, x2, y2);
			}

			if ((elisions & DST) == 0) {
				x2 = x1 + (4 * width1)/5;
				y3 = y + GAP;
				ToolBarButton.drawEdge(g, x2, y3, x2, y1);
		}	}
		
		if ((elisions & BOX) != 0) {
			Util.drawOutlineBox(g, x+1, y+1, w-2, h-2);
		}

		text = getText();

		if (text != null) {
			g.setFont(getFont());
			FontMetrics fm         = g.getFontMetrics();
			int			heightText = fm.getHeight();
			Util.drawStringClipped(g, text,	x1 , y1 + height1 - heightText, width1, height1, false, false, true);
		}
	}
	
	public String toString()
	{
		String	tip      = "";
		int		elisions = m_elisions;
		String	text     = getText();

		if ((elisions & CHANGE) == 0) {
			tip = "No change";
		} else {
			if ((elisions & ELIDED) == 0) {
				tip = " all";
			} else if ((elisions & ELIDED) == ELIDED) {
				tip = " no";
			} else {
				if ((elisions & SRC) == 0) {
					tip = ", +source";
				} 
				if ((elisions & EXITS) == 0) {
					tip += ", +exits";
				}
				if ((elisions & ENTERS) == 0) {
					tip += ", +enters";
				}
				if ((elisions & DST) == 0) {
					tip += ", +destination";
				}
				if ((elisions & INTERNAL) == 0) {
					tip += ", +internal";
			}	}
			if (text != null) {
				tip += " " + text;
			}
			tip += " relations permitted";
		
			if ((elisions & CONTAINS) == 0) {
				tip = "Closed" + tip;
			} else {
				tip = "Open" + tip;
		}	}
		return tip;
	}		
}
