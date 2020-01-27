package lsedit;

import java.util.Enumeration;
import java.util.Vector;

class StringCompareFn extends Object implements CompareFn 
{
	public int compare(Object o1, Object o2) 
	{
		String s1 = o1.toString();
		String s2 = o2.toString();

		return s1.compareTo(s2);
	}
}

class ClientSupplierCompareFn extends Object implements CompareFn
{
	public int compare(Object o1, Object o2) 
	{
		if (o1 == o2) {
			return 0;
		}
		EntityInstance e1 = (EntityInstance) o1;
		EntityInstance e2 = (EntityInstance) o2;

		double x1 = e1.getAvgX();
		double x2 = e2.getAvgX();

		if (x1 > x2) {
			return 1;
		} 
		if (x1 < x2) {
			return -1;
		}
		return 0;
	}
}

class HorizontalCompareFn implements CompareFn
{
	public int compare(Object o1, Object o2)
	{
		double	x1, x2;

		x1 = ((EntityInstance) o1).xRelLocal();
		x2 = ((EntityInstance) o2).xRelLocal();

		if (x1 < x2) {
			return(-1);
		}
		if (x1 > x2) {
			return(1);
		}
		return(0);
}	}

class VerticalCompareFn implements CompareFn
{
	public int compare(Object o1, Object o2)
	{
		double	y1, y2;

		y1 = ((EntityInstance) o1).yRelLocal();
		y2 = ((EntityInstance) o2).yRelLocal();

		if (y1 < y2) {
			return(-1);
		}
		if (y1 > y2) {
			return(1);
		}
		return(0);
}	}

class PreorderCompareFn extends Object implements CompareFn
{
	public int compare(Object o1, Object o2) 
	{
		if (o1 == o2) {
			return 0;
		}
		EntityInstance e1 = (EntityInstance) o1;
		EntityInstance e2 = (EntityInstance) o2;

		int x1 = e1.getPreorder();
		int x2 = e2.getPreorder();

		if (x1 > x2) {
			return 1;
		} 
		if (x1 < x2) {
			return -1;
		}
		return 0;
	}
}

class IdCompareFn extends Object implements CompareFn
{
	public int compare(Object o1, Object o2) 
	{
		if (o1 == o2) {
			return 0;
		}
		LandscapeClassObject e1 = (LandscapeClassObject) o1;
		LandscapeClassObject e2 = (LandscapeClassObject) o2;

		int x1 = e1.getNid();
		int x2 = e2.getNid();

		if (x1 > x2) {
			return 1;
		} 
		if (x1 < x2) {
			return -1;
		}
		return 0;
	}
}

class PositionCompareFn extends Object implements CompareFn
{
	public int compare(Object o1, Object o2) 
	{
		if (o1 == o2) {
			return 0;
		}

		HiArc e1 = (HiArc) o1;
		HiArc e2 = (HiArc) o2;

		int x1 = e1.to().m_position;
		int x2 = e2.to().m_position;

		if (x1 > x2) {
			return 1;
		} 
		if (x1 < x2) {
			return -1;
		}
		return 0;
	}
}

class DistanceCompareFn extends Object implements CompareFn
{
	public int compare(Object o1, Object o2) 
	{
		if (o1 == o2) {
			return 0;
		}

		Distance d1 = (Distance) o1;
		Distance d2 = (Distance) o2;

		double x1 = d1.m_length;
		double x2 = d2.m_length;

		if (x1 > x2) {
			return 1;
		} 
		if (x1 < x2) {
			return -1;
		}
		return 0;
	}
}

public class SortVector
{
	static final StringCompareFn			m_stringCompareFn         = new StringCompareFn();
	static final ClientSupplierCompareFn	m_clientSupplierCompareFn = new ClientSupplierCompareFn();
	static final HorizontalCompareFn		m_horizontalCompareFn     = new HorizontalCompareFn();
	static final VerticalCompareFn			m_verticalCompareFn       = new VerticalCompareFn();
	static final PreorderCompareFn			m_preorderCompareFn       = new PreorderCompareFn();
	static final IdCompareFn				m_idCompareFn		      = new IdCompareFn();
	static final PositionCompareFn			m_positionCompareFn	      = new PositionCompareFn();
	static final DistanceCompareFn			m_distanceCompareFn       = new DistanceCompareFn();

	private static final int partition( Vector v, int l, int r, CompareFn cf, boolean asc )
	{
		// Arbitrarily pick the left element as the pivot element:

		Object p = v.elementAt(l);
		Object o;
		Object q;

		l--; 
		r++;

		while(true) {
			// Figure out what's before and after the pivot:

			boolean f;

			do {
				o = v.elementAt(--r);

				if (asc) {
					f = (cf.compare(o, p) > 0);
				} else {	
					f = (cf.compare(o, p) < 0);
				}
			} while (f);

			do {
				q = v.elementAt(++l);

				if (asc) {
					f = (cf.compare(q, p) < 0);
				} else {	
					f = (cf.compare(q, p) > 0);
				}
			} while (f);

			// Swap elements if we can:

			if (r <= l) {
				return r;
			}
			if (cf.compare(q, o) != 0) {
				v.setElementAt(o, l);
				v.setElementAt(q, r);
	}	}	}

	private static final void qsort( Vector v, int l, int r, CompareFn cf, boolean asc)
	{
		// If we haven't reached a termination condition...

		if (l < r) {

			//	Partition the vector into left and right halves:
			int p = partition(v, l, r, cf, asc);

			//	Recursively sort each half:
			qsort(v, l, p, cf, asc);
			qsort(v, p+1, r, cf, asc);
	}	}

	private static final void sort( Vector v, int l, int r, CompareFn cf, boolean asc)
	{
		switch(r) {
		case 0:
			return;
		case 1:
			Object object0 = v.elementAt(0);
			Object object1 = v.elementAt(1);
			if (cf.compare(object0, object1) == 1) {
				v.setElementAt(object1, 0);
				v.setElementAt(object0, 1);
			}
			return;
		default:
			qsort(v, l, r, cf, asc);
			return;
	}	}

	public static void sortVector(Vector v, CompareFn cf, boolean ascending) 
	{
		sort(v, 0, v.size() - 1, cf, ascending);
	}

	public static void byString(Vector v, boolean ascending)
	{
		sort(v, 0, v.size() - 1, m_stringCompareFn, ascending);
	}

	public static void byString(Vector v) 
	{
		byString(v, true);
	}

	public static void byAvgX(Vector v, boolean ascending)
	{
		if (v != null) {
			Enumeration		en;
			EntityInstance	e;
			
			for (en = v.elements(); en.hasMoreElements(); ) {
				e = (EntityInstance) en.nextElement();
				e.computeAvgX();
			}
			sort(v, 0, v.size() - 1, m_clientSupplierCompareFn, ascending);
	}	}

	public static void byAvgX(Vector v) 
	{
		byAvgX(v, true);
	}

	public static void byHorizontalPosition(Vector v, boolean ascending)
	{
		sort(v, 0, v.size() - 1, m_horizontalCompareFn, ascending);
	}

	public static void byHorizontalPosition(Vector v) 
	{
		byHorizontalPosition(v, true);
	}

	public static void byVerticalPosition(Vector v, boolean ascending)
	{
		sort(v, 0, v.size() - 1, m_verticalCompareFn, ascending);
	}

	public static void byVerticalPosition(Vector v) 
	{
		byVerticalPosition(v, true);
	}

	// N.B. Don't sort by postorder (multiple identical values possible)

	public static void byPreorder(Vector v, boolean ascending)
	{
		sort(v, 0, v.size() - 1, m_preorderCompareFn, ascending);
	}

	public static void byId(Vector v) 
	{
		sort(v, 0, v.size() - 1, m_idCompareFn, true);
	}

	public static void byTreeNode(Vector v)
	{
		sort(v, 0, v.size() - 1, m_stringCompareFn, true);
	}

	public static void byPosition(Vector v)
	{
		sort(v, 0, v.size() - 1, m_positionCompareFn, true);
	}

	public static void byDistance(Vector v)
	{
		sort(v, 0, v.size()-1, m_distanceCompareFn, true);
	}
}

