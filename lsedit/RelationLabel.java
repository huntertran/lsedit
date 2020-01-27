package lsedit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.awt.geom.AffineTransform;

import javax.swing.JComponent;

// A low weight component

class RelationLabel extends JComponent
{
	protected final static int	LABEL_SPACE = 10;	// Extra space to allow a label to handle left end bias

	protected static Font			m_labelFont	  = null;
	protected static FontMetrics	m_fm          = null;

	/* Statics to save memory */
	protected static int[]			m_xs     = {0,0,0,0};
	protected static int[]			m_ys     = {0,0,0,0};

	protected String				m_text;			// String to show
	protected AffineTransform		m_transform;	// Angle to draw at (horizontal if null)

	protected int					m_x, m_y;		// Where to draw	

	public RelationLabel(String text, Color foreground) 
	{
		Font labelFont = m_labelFont;
		
		if (labelFont == null) {
			m_labelFont = labelFont = Options.getTargetFont(Option.FONT_EDGE_LABEL);
		}
		m_text = text;

		if (foreground != null) {
			setForeground(foreground);
		}
		setFont(labelFont);
	/*
		setBackground(Color.BLUE);
		setOpaque(true);
	*/
//		System.out.println("RelationLabel.RelationLabel(" + text + ")");
	}

	public static Font getEdgeLabelFont()
	{
		return m_labelFont;
	}

	public static void setEdgeLabelFont(Font font)
	{
		if (font != m_labelFont) {
			m_labelFont = font;
			m_fm        = null;
	}	}

	public void place(int srcX, int srcY, int dstX, int dstY)
	{
        String		text = m_text;
        Font		font = m_labelFont;
        FontMetrics fm   = m_fm;

        int lwidth, lheight;

        if ((text == null) || (font == null) || (text == "")) {
			lwidth = lheight = 0;
        } else {
			
			if (fm == null) {
				m_fm = fm = getFontMetrics(font);
			}

            lwidth  = fm.stringWidth(text) + LABEL_SPACE;
            lheight = fm.getHeight();
        }

		if (lwidth == 0 || lheight == 0) {
			return;
		}

//		System.out.println("RelationLabel.place dim=" + lwidth + "x" + lheight);

		int		  rwidth  = dstX - srcX;
		int		  rheight = dstY - srcY;
		Option	  option  = Options.getDiagramOptions();
		double    angle   = option.getLabelAngle();	// In radians

		if (option.isRotateEdgeLabels()) {
			if (rwidth == 0) {
				if (rheight >= 0) {
					angle += Math.PI/2.0;
				} else {
					angle -= Math.PI/2.0;
				}
			} else {
				double tan = ((double) rheight)/((double) rwidth);					
				angle     += Math.atan(tan);

				while (angle < 0) {
					angle += Math.PI * 2;
				}

				if (angle > (Math.PI/2.0) && angle < (3.0*Math.PI/2.0)) {
					// Upside down rotate it 180
					angle += Math.PI;
				}
		}	}
		
/*
		int factor = ((srcX < dstX) ? 1 : 2);
		if (srcX == dstX && srcY < dstY) {
			factor = 1;
		}
*/
		int	  xc     = srcX + (dstX - srcX) / 3;		// Centre of label wrt diagram (either 1/3 or 2/3 of way along edge)
		int	  yc     = srcY + (dstY - srcY) / 3;

		int	lx = xc-(lwidth /2);							// Left bound wrt diagram
		int ly = yc-(lheight/2);

		m_x = LABEL_SPACE/2;								// Wrt lx
		m_y = fm.getAscent();								// Need the baseline (not the top of the character);

		if (angle == 0.0) {
			m_transform = null;
		} else {
			AffineTransform transform = m_transform;

			if (transform == null) {
				m_transform = transform = new AffineTransform();
			}
/*
			A := [1,    0,  -xc]   makes the selected centre point of the label the 0,0 point
				 [0,    1,  -yc]
				 [0,    0,  1  ]   

			B := [cosa,-sina,0 ]   rotates the new centre around 0,0 point
				 [sina,cosa, 0 ]
				 [0,   0,    1 ]

			C := [1,    0,  xc ]   puts centre back where it was
				 [0,    1,  yc ]
				 [0,    0,  1  ]

			Transform = (C*B*A)[x] =


				 [cos(alpha), -sin(alpha), xc-cos(alpha)*xc+sin(alpha)*yc]  rotate about selected centre point of label
				 [sin(alpha), cos(alpha),  yc-sin(alpha)*xc-cos(alpha)*yc]
				 [0,          0,           1                             ]

 */
			double	  sina   = Math.sin(angle);
			double	  cosa   = Math.cos(angle);

			int		x1, y1, x2, y2, x, y;
			int		w, h;
			double	wd, hd;

			x1 = Integer.MAX_VALUE;
			x2 = Integer.MIN_VALUE;
			y1 = Integer.MAX_VALUE;
			y2 = Integer.MIN_VALUE;

			for (w = 0; w <= lwidth; w += lwidth) {
				for (h = 0; h <= lheight; h += lheight) {
					wd = (double) w;
					hd = (double) h;
					x  = (int) (wd * cosa - hd * sina);
					y  = (int) (wd * sina + hd * cosa);
					if (x < x1) {
						x1 = x;
					}
					if (y < y1) {
						y1 = y;
					}
					if (x > x2) {
						x2 = x;
					}
					if (y > y2) {
						y2 = y;
			}	}	}

			// Now have the increase size used by the label because drawn at an angle

			int newWidth  = (x2 - x1);
			int newHeight = (y2 - y1);
			int extra     = (newWidth - lwidth)  / 2;
			lx           -= extra;
			m_x          += extra;
			extra         = (newHeight - lheight) / 2;
			ly           -= extra;
			m_y          += extra;
			lwidth        = newWidth;
			lheight       = newHeight;

			xc           -= lx;		// Wrt my bounds
			yc           -= ly;
 			
			double xcd    = (double) xc;
			double ycd    = (double) yc;
			double m02    = xcd*(1.0-cosa)+sina*ycd;
			double m12    = ycd*(1.0-cosa)-sina*xcd;

			transform.setTransform(cosa, sina, -sina, cosa, m02, m12);
		}

		if (lx != getX() || ly != getY() || lwidth != getWidth() || lheight != getHeight()) {
//			System.out.println("RelationLabel.old = " + getBounds());
			setBounds(lx, ly, lwidth, lheight);
		}
//		System.out.println("RelationLabel.position = " + m_x + "x" + m_y + " bound=" + getBounds());
	}

	public void paintComponent(Graphics g)
	{
		Option			option    = Options.getDiagramOptions();
		AffineTransform save      = null;
		AffineTransform transform = m_transform;

/*
		int width      = getWidth();
		int height     = getHeight();
		g.drawLine(0,0, width, height);
		g.drawLine(0,height, width, 0);
 */

		if (option.isInvertEdgeLabelBackground()) {
			Color color = g.getColor();
			g.setColor(ColorCache.getInverse(color.getRGB()));
			if (transform == null) {
				g.fillRect(0, 0, getWidth(), getHeight());
			} else {
				FontMetrics fm = m_fm;

				if (fm == null) {
					m_fm = fm = getFontMetrics(m_labelFont);
				}

				double  left   = (double) (m_x - LABEL_SPACE/2);
				double	right  = left + (double) (fm.stringWidth(m_text) + LABEL_SPACE);
				double	top    = (double) (m_y - fm.getAscent());
				double	bottom = (double) (m_y + fm.getDescent());
				double	m00    = transform.getScaleX();
				double	m01    = transform.getShearX();
				double  m02    = transform.getTranslateX();
				double	m10    = transform.getShearY();
				double  m11    = transform.getScaleY();
				double	m12    = transform.getTranslateY();

				// (0,top) -> (width, top) -> (width, bottom) -> (0, bottom)
				m_xs[0] = (int) (left  * m00 + top    * m01 + m02);
				m_ys[0] = (int) (left  * m10 + top    * m11 + m12);
				m_xs[1] = (int) (right * m00 + top    * m01 + m02);
				m_ys[1] = (int) (right * m10 + top    * m11 + m12);
				m_xs[2] = (int) (right * m00 + bottom * m01 + m02);
				m_ys[2] = (int) (right * m10 + bottom * m11 + m12);
				m_xs[3] = (int) (left  * m00 + bottom * m01 + m02);
				m_ys[3] = (int) (left  * m10 + bottom * m11 + m12);

				g.fillPolygon(m_xs, m_ys, 4);
			} 
			g.setColor(color);
		}
 
		if (transform != null && (g instanceof Graphics2D)) {
			Graphics2D g2d = (Graphics2D) g;
			// Get the current transform
			save = g2d.getTransform();


			// Perform transformation
			g2d.transform(transform);

/*
			g.setColor(Color.GREEN);
			g.drawLine(0,0, width, height);
			g.drawLine(0,height, width, 0);
 */
		}

		// Render
		g.drawString(m_text, m_x, m_y); 

		if (save != null) {
			// Restore original transform
			((Graphics2D) g).setTransform(save);
		}
	}

	public String toString()
	{
		return "RelationLabel=" + m_text;
	}
}
