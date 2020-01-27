package lsedit;

import java.lang.Math;

/* This is the object which handles inflections at the edge of a box */

public class EdgePoint extends Object {

	public static final int TOP			= 0;
	public static final int BOTTOM		= 1;
	public static final int LEFT		= 2;
	public static final int RIGHT		= 3;
	public static final int SIDES       = 4;

	private short			m_wf;		// Width fraction of  entity e width
	private short			m_hf;		// Height fraction of entity e height 

	private short			m_adjusted_wf;
	private short			m_adjusted_hf;

	public EdgePoint() 
	{
//		System.out.println("EdgePoint::");
	}

	public double getWidthFactor()
	{
		return Util.shortToRelative(m_wf);
	}

	public double getHeightFactor()
	{
		return Util.shortToRelative(m_hf);
	}
	
	public double getAdjustedWidthFactor()
	{
		return Util.shortToRelative(m_adjusted_wf);
	}
	
	public double getAdjustedHeightFactor()
	{
		return Util.shortToRelative(m_adjusted_hf);
	}

	/* Given the edge point specified relatively in m_wf, m_hf, compute the relative adjusted
	 * point to be used where the given edgePoint intersects the current shape associated with
	 * e (ie e's entityClass).
	 */

	public void adjustEdgePoint(EntityClass ec)
	{
		double			wf, hf;
		int				style, direction;
		int				regular_dimension = 0;
		boolean			correct_x         = false;
		boolean			correct_y         = false; 
		double			x1                = 0;
		double			y1                = 0;
		double			x2                = 0;
		double			y2                = 0; 
		double			diff;    

		m_adjusted_wf = m_wf;	// Set adjusted to defaults
		m_adjusted_hf = m_hf;
		
		wf = Util.shortToRelative(m_wf);
		hf = Util.shortToRelative(m_hf);

		style     = ec.getInheritedStyle();
		direction = ec.getDirection();

		switch (style) {
			case EntityClass.ENTITY_STYLE_SOURCEOBJ:
			{

				/* Formula for circle (treating center of box as (0,0) having size (1, 1)
				 *
				 * (2*hf)^2 + (2*wf)^2 = 1
				 */

				if (hf == 0.0 || hf == 1.0) {
					// Coming from top or bottom
					double wf1 = wf - 0.5;
					double d   = Math.sqrt(1.0 - 4.0*wf1*wf1)/2.0;
					if (hf == 0.0) {
						d = -d;
					}
					m_adjusted_hf = Util.relativeToShort(0.5 + d);
				} else if (wf == 0.0 || wf == 1.0) {
					double hf1 = hf - 0.5;
					double d   = Math.sqrt(1.0 - 4.0*hf1*hf1)/2.0;
					if (wf == 0.0) {
						d = -d;
					}
					m_adjusted_wf = Util.relativeToShort(0.5 + d);
				}
				return;
			}
			case EntityClass.ENTITY_STYLE_PAPER:
			{
				if (hf == 0 || wf == 0) {
					return;
				}
				if (hf == 1) {
					if (wf < (3.0/8.0)) {
						// Entering arc
						// Todo
						return;
					}
					if (wf < (2.0/3.0)) {
						// Entering diagonal
						x1 = (3.0/8.0);
						y1 = (193.0/200.0);
						x2 = (2.0/3.0);
						y2 = (4.0/5.0);
					} else {
						// Entering base
						x1 = (2.0/3.0);
						y1 = (4.0/5.0);
						x2 = 1;
						y2 = y1;
					}
					correct_y = true;
				} else if (wf == 1) {
					if (hf < (4.0/5.0)) {
						return;
					}
					// Entering diagonal
					x1 = (3.0/8.0);
					y1 = (193.0/200.0);
					x2 = (2.0/3.0);
					y2 = (4.0/5.0);
					correct_x = true;
				}
				break;
			}
			case EntityClass.ENTITY_STYLE_TRIANGLE:
			{
				if (hf == 0 || hf == 1.0) {
					// Coming in from top or bottom
					switch (direction) {
					case 0:				/* /\ */
										/* -- */
						if (hf == 1.0) {
							return;
						}
						if (wf < 0.5) {
							x1 = 0;
						} else {
							x1 = 1;
						}
						y1 = 1;
						x2 = 0.5;
						y2 = 0;
						break;
					case 1:				/*  \ */
										/* |  */
										/*  / */
						x1 = 0;
						if (hf == 0) {
							y1 = 0;
						} else {
							y1 = 1;
						}
						x2 = 1;
						y2 = 0.5;
						break;
					case 2:				/* --- */
										/* \ / */
						if (hf == 0) {
							return;
						}
						if (wf < 0.5) {
							x1 = 0;
						} else {
							x1 = 1;
						}
						y1 = 0;
						x2 = 0.5;
						y2 = 1;
						break;
					case 3:				/*  /  */
										/*    |*/
										/*  \  */
						x1 = 1;
						if (hf == 0) {
							y1 = 0;
						} else {
							y1 = 1;
						}
						x2 = 0;
						y2 = 0.5;
						break;
					}
					correct_y = true;
				} else if (wf == 0 || wf == 1) {
					// Coming in from left or right
					switch (direction) {
					case 0:				/* /\ */
										/* -- */
						if (wf == 0) {
							x1 = 0;
						} else {
							x1 = 1;
						}
						y1 = 1;
						x2 = 0.5;
						y2 = 0;
						break;
					case 1:				/*  \ */
										/* |  */
										/*  / */
						if (wf == 0) {
							return;
						}
						x1 = 0;
						if (hf < 0.5) {
							y1 = 0;
						} else {
							y1 = 1;
						}
						x2 = 1;
						y2 = 0.5;
						break;
					case 2:				/* --- */
										/* \ / */
						if (wf == 0) {
							x1 = 0;
						} else {
							x1 = 1;
						}
						y1 = 0;
						x2 = 0.5;
						y2 = 1;
						break;
					case 3:				/*  /  */
										/*    |*/
										/*  \  */
						if (wf == 1) {
							return;
						}
						x1 = 1;
						if (hf < 0.5) {
							y1 = 0;
						} else {
							y1 = 1;
						}
						x2 = 0;
						y2 = 0.5;
						break;
					}
					correct_x = true;
				}
				break;
			}
			case EntityClass.ENTITY_STYLE_ROMBUS:
			{
				if (hf == 0 || hf == 1.0) {
					// Coming in from top or bottom
					switch (direction) {/*  ---- */
					case 0:				/* /  /  */
										/* --    */
						if (hf == 0) {
							if (wf > 0.2) {
								return;
							}
						} else {
							if (wf < 0.8) {
								return;
							}
						}
						// Line we must intersect
						x1 = 0;
						y1 = 1;
						x2 = 0.2;
						y2 = 0;
						if (hf != 0) {
							x1 += 0.8;
							x2 += 0.8;
						}
						break;
					case 1:				/* | \  */
										/* |  | */
										/*  \ | */
						x1 = 0;
						y1 = 0;
						x2 = 1;
						y2 = 0.2;
						if (hf != 0) {
							y1 += 0.8;
							y2 += 0.8;
						}
						break;
					case 2:				/* ---   */
										/* \   \ */
						if (hf == 0) {	/*   ----*/
							if (wf < 0.8) {
								return;
							}
						} else {
							if (wf > 0.2) {
								return;
							}
						}
						x1 = 0;
						y1 = 0;
						x2 = 0.2;
						y2 = 1;
						if (hf == 0) {
							x1 += 0.8;
							x2 += 0.8;
						}
						break;
					case 3:	
						x1 = 0;			/*    /| */
						y1 = 0.2;		/*   | | */
						x2 = 1.0;		/*   |/	 */
						y2 = 0;
						if (hf != 0) {	
							y1 += 0.8;
							y2 += 0.8;
						}
						break;
					}
					correct_y = true;
				} else if (wf == 0 || wf == 1) {
					// Coming in from left or right
					switch (direction) {/*  ---- */
					case 0:				/* /  /  */
										/* --    */
						x1 = 0.2;
						y1 = 0;
						x2 = 0;
						y2 = 1;
						if (wf != 0) {
							x1 += 0.8;
							x2 += 0.8;
						}
						break;
					case 1:				/* | \  */
										/* |  | */
										/*  \ | */
						
						if (wf == 0) {
							if (hf < 0.8) {
								return;
							}
						} else {
							if (hf > 0.2) {
								return;
							}
						}

						x1 = 0;
						y1 = 0;
						x2 = 1;
						y2 = 0.2;
						if (wf == 0) {
							y1 += 0.8;
							y2 += 0.8;
						}
						break;
					case 2:				/* ---   */
										/* \   \ */
										/*   ----*/
						x1 = 0;
						y1 = 0;
						x2 = 0.2;
						y2 = 1;

						if (wf != 0) {
							x1 += 0.8;
							x2 += 0.8;
						}
						break;
					case 3:	
										/*    /| */
										/*   | | */
										/*   |/	 */
						if (wf == 0) {
							if (hf > 0.2) {
								return;
							}
						} else {
							if (hf < 0.8) {
								return;
						}	}
						x1 = 0;
						y1 = 0.2;
						x2 = 1;
						y2 = 0;
						if (wf != 0) {
							y1 += 0.8;
							y2 += 0.8;
						}
						break;
					}
					correct_x = true;
				}
				break;
			}
			case EntityClass.ENTITY_STYLE_TRAPEZOID:
			{
				if (hf == 0 || hf == 1) {
					// Coming from top or bottom
					switch (direction) {
					case 0:					/*			*/
											/*  ---		*/
											/* /    \   */
											/* ------	*/
						if (hf == 1) {
							return;
						}
						if (wf < 0.2) {
							x1 = 0;
							x2 = 0.2;
						} else if (wf > 0.8) {
							x1 = 1;
							x2 = 0.8;
						} else {
							return;
						}
						y1 = 1;
						y2 = 0;
						break;
					case 1:					/* |\    */
											/* |  |  */
											/* |  |  */
											/* |/	 */
						x1 = 0;
						x2 = 1;
						if (hf == 0) {
							y1 = 0;
							y2 = 0.2;
						} else {
							y1 = 1;
							y2 = 0.8;
						}
						break;
					case 2:					/* ------ */
											/*  \   / */
						if (hf == 0) {		/*   ---  */
							return;
						}
						if (wf < 0.2) {
							x1 = 0;
							x2 = 0.2;
						} else if (wf > 0.8) {
							x1 = 1;
							x2 = 0.8;
						} else {
							return;
						}
						y1 = 0;
						y2 = 1;
						break;
					case 3:					/*   /| */
											/*  | | */
											/*   \| */
						x1 = 0;
						x2 = 1;
						if (hf == 0) {
							y1 = 0.2;
							y2 = 0;
						} else {
							y1 = 0.8;
							y2 = 1;
						}
						break;
					}
					correct_y = true;
				} else if (wf == 0 || wf == 1) {
					// Coming in from left or right
					switch (direction) {
					case 0:					/*			*/
											/*  ---		*/
											/* /    \   */
											/* ------	*/
						y1 = 0;
						y2 = 1;
						if (wf == 0) {
							x1 = 0.2;
							x2 = 0;
						} else {
							x1 = 0.8;
							x2 = 1;
						}
						break;
					case 1:					/* |\    */
											/* |  |  */
											/* |  |  */
											/* |/	 */
						if (wf == 0) {
							return;
						}
						if (hf < 0.2) {
							y1 = 0;
							y2 = 0.2;
						} else if (hf > 0.8) {
							y1 = 1;
							y2 = 0.8;
						} else {
							return;
						}
						x1 = 0;
						x2 = 1;
						break;
					case 2:					/* ------ */
											/*  \   / */
											/*   ---  */
						y1 = 0;
						y2 = 1;
						if (wf == 0) {
							x1 = 0;
							x2 = 0.2;
						} else {
							x1 = 1;
							x2 = 0.8;
						}
						break;
					case 3:					/*   /| */
											/*  | | */
											/*   \| */
						if (hf < 0.2) {
							y1 = 0.2;
							y2 = 0;
						} else if (hf > 0.8) {
							y1 = 0.8;
							y2 = 1;
						} else {
							return;
						}
						x1 = 0;
						x2 = 1;
						break;
					}
					correct_x = true;
				}
				break;
			}
			case EntityClass.ENTITY_STYLE_TRIANGLE2:
			{
				regular_dimension = 3;
				break;
			}
			case EntityClass.ENTITY_STYLE_RECTANGLE:
			{
				regular_dimension = 4;
				break;
			}

			case EntityClass.ENTITY_STYLE_PENTAGON:
			{
				regular_dimension = 5;
				break;
			}
			case EntityClass.ENTITY_STYLE_HEXAGON:
			{
				regular_dimension = 6;
				break;
			}
			case EntityClass.ENTITY_STYLE_OCTAGON:
			{
				regular_dimension = 8;
				break;
			}
			case EntityClass.ENTITY_STYLE_DECAHEDRON:
			{
				regular_dimension = 10;
				break;
			}
			case EntityClass.ENTITY_STYLE_12SIDED:
			{
				regular_dimension = 12;
				break;
			}
			case EntityClass.ENTITY_STYLE_14SIDED:
			{
				regular_dimension = 14;
				break;
			}
			case EntityClass.ENTITY_STYLE_16SIDED:
			{
				regular_dimension = 16;
				break;
			}
			case EntityClass.ENTITY_STYLE_18SIDED:
			{
				regular_dimension = 18;
				break;
			}
			case EntityClass.ENTITY_STYLE_20SIDED:
			{
				regular_dimension = 20;
				break;
			}
			default:
			{
				return;
			}
		}

		if (regular_dimension != 0) {
			double wf1     = wf - 0.5;
			double hf1     = hf - 0.5;
			double myangle = Util.degrees(wf1, hf1);					// An angle between 0 and 360
			double angle   = ec.getAngle() - 90.0;						// -90.0 to point up N.B. coordinate at (0,-.5)
			double shift   = 360.0/((double) regular_dimension);
			double last_angle;
			int	   i;
			
//			System.out.println(ec + " myangle=" + myangle + " startangle=" + angle + " regular=" + regular_dimension + " shift=" + shift);
			while (angle >= 360.0) {
				angle -= 360.0;
			}
			if (angle < 0) {
				angle += 360.0;
			}
			// angle is now in range [0,360)
			for (i = 0; i <= regular_dimension; ++i) {
				last_angle = angle;
				angle     -= shift;
				if (angle < 0) {
					angle += 360.0;
				}
//				System.out.println("angle=" + angle + " lastangle=" + last_angle);
				// angle, lastangle and myangle all in range 0-360
				diff = angle-myangle;
				if (diff < 0) {
					diff = -diff;
				}
				if (diff > 180.0) {
					diff = 360.0-diff;
				}
				if (diff > shift) {
					continue;
				}
				diff = last_angle - myangle;
				if (diff < 0) {
					diff = -diff;
				}
				if (diff > 180.0) {
					diff = 360.0 - diff;
				}
				if (diff > shift) {
					continue;
				}

				// Radius always 0.5 since within relative coordinates oval is a circle
	
				angle      = Math.toRadians(angle);
				last_angle = Math.toRadians(last_angle);

				x1        = Math.cos(angle)*0.5;
				y1        = Math.sin(angle)*0.5;
				x2        = Math.cos(last_angle)*0.5;
				y2        = Math.sin(last_angle)*0.5;


				/* Compute the point of intersection for a line from the origin with angle myangle to the
				 * line
				 *
				 * Formula for line between (x1,y1) and (x2,y2)
				 * 
				 * y = m(x-x1) + y1
				 *
				 * y = [(y2-y1)/(x2-x1)](x-x1) + y1  iff x2 != x1 (case A)
				 *
				 * x = x1                            iff x2 == x1 (case B)
				 *
				 * Formula for line at myangle through origin
				 *
				 * y = x*tan(myangle) if myangle != +/- 90        (case 1)
				 *
				 * x = 0              if myangle == +/- 90		  (case 2)
				 *
				 * A1) solving two equations for x gives:
				 *
				 *  x = (x1*y2 - y1*x2)/(y2-y1+(x1-x2)*tan(myangle)  [divisor 0 if parallel]
				 *  y solved by plugging x into formula in case A
				 *
				 * A2) x = 0 solve for y
				 *
				 * B1) x = x1  y = x1*tan(myangle)
				 *
				 * B2) x,y = 0 if x1 = 0 else parallel
				 */

				double x, y;

				if (myangle == 90.0 || myangle == 270.0) {
					if (x1 != x2) {
						// Case A2
						x = 0;
						y = y1 - x1*(y2-y1)/(x2-x1);
					} else {
						// Case B2
						if (x1 != 0) {
							return;
						}
						x = 0;
						y = 0;
					}
				} else {
					double tana = Math.tan(Math.toRadians(myangle));

					if (x1 != x2) {
						// Case A1
						diff = y2-y1+(x1-x2)*tana;
						if (diff == 0) {
							return;
						}
						x = (x1*y2 - y1*x2)/diff;
					} else {
						// Case B1
						x = x1;
					}
					y = x*tana;
				}
			
				m_adjusted_wf = Util.relativeToShort(x + 0.5);
				m_adjusted_hf = Util.relativeToShort(y + 0.5);
				return;
		}	}


		if (correct_y) {
			diff = x1 - x2;

			if (diff == 0) {
				return;
			} else {
				m_adjusted_hf = Util.relativeToShort(((x1-wf)*y2 - (x2-wf)*y1)/diff);
			}
//			System.out.println("Adjusted hf from " + m_wf + ", " + m_hf + " to " + m_adjusted_wf + ", " + m_adjusted_hf);
		} 
		if (correct_x) {
			diff = y1 - y2;
			if (diff == 0) {
				return;
			} 
			m_adjusted_wf = Util.relativeToShort(((y1-hf)*x2 - (y2-hf)*x1)/diff);
//			System.out.println("Adjusted wf from " + m_wf + ", " + m_hf + " to " + m_adjusted_wf + ", " + m_adjusted_hf);
		}
	}

	public void setFactors(EntityClass ec, short wf, short hf)
	{
		m_wf          = wf;			// Width factor
		m_hf          = hf;			// Height factor

		adjustEdgePoint(ec);
	}
	
	public void setFactors(EntityClass ec, double wf, double hf)
	{
		m_wf          = Util.relativeToShort(wf);			// Width factor
		m_hf          = Util.relativeToShort(hf);			// Height factor

		adjustEdgePoint(ec);
	}

	public void setFactors(EntityClass ec, RelationClass rc, int side)
	{
		short f  = rc.getIOfactor();
		short wf = Short.MIN_VALUE + 1;		// ie 0.0
		short hf = Short.MIN_VALUE + 1;

		switch(side) {
		case EdgePoint.TOP:
			wf = f;					// Put at entity x + width * f at top
			break;
		case EdgePoint.BOTTOM:
			wf = f;
			hf = Short.MAX_VALUE;	// Put at bottom
			break;
		case EdgePoint.LEFT:
			hf = f;					// Put at left
			break;
		default:
			wf = Short.MAX_VALUE;	// Put at right
			hf = f;
			break;
		}
		setFactors(ec, wf, hf);
	}


	public boolean isDefault(RelationClass rc)
	{
		short	f  = rc.getIOfactor();
		short	wf = m_wf;
		short	hf = m_hf;

		if (wf == Short.MIN_VALUE+1 || wf == Short.MAX_VALUE) {
			// Left or right edge
			if (hf == f) {
				return true;
			}
		} else if (hf == Short.MIN_VALUE+1 || hf == Short.MAX_VALUE) {
			// Top or bottom edge
			if (wf == f) {
				return true;
		}	}
		return false;
	}

	public String getString(RelationClass rc) 
	{
		return "(" + rc.getId() + " " + m_wf + " " + m_hf + ") ";
	}

	private String EdgeSide() 
	{
		short wf = m_wf;		// Width factor
		short hf = m_hf;		// Height factor

		if (hf == Short.MIN_VALUE+1) {
			if (wf == Short.MIN_VALUE+1) {
				return "TOP LEFT";
			}
			if (wf == Short.MAX_VALUE) {
				return "TOP RIGHT";
			}
			return "TOP";
		}
		if (hf == Short.MAX_VALUE) {
			if (wf == Short.MIN_VALUE+1) {
				return "BOTTOM LEFT";
			}
			if (wf == Short.MAX_VALUE) {
				return "BOTTOM RIGHT";
			}
			return "BOTTOM";
		}
		if (wf == Short.MIN_VALUE+1) {
			return "LEFT";
		}
		if (wf == Short.MAX_VALUE) {
			return "RIGHT";
		}
		return "EDGE POINT";
	}

	public String toString() 
	{
		String ret;

		ret = EdgeSide() + " (" + m_wf;
		if (m_wf != m_adjusted_wf) {
			ret += "->" + m_adjusted_wf;
		}
		ret += ", " + m_hf;
		if (m_hf != m_adjusted_hf) {
			ret += "->" + m_adjusted_hf;
		}
		ret += ")";
		return ret;
	}

}

