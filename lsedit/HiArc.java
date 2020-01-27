package lsedit;

/**
 * Developed by: Ian Davis in Summer 1997 for Grant Weddell
 * A HiArc is an arc between any two HiGraph nodes
 * This class provides the methods needed to create, display
 * and destroy a HiArc.
 */

public class HiArc {
	private HiGraph m_from;				// Arc from this node
	private HiGraph m_to;				// Arc to this node
	private boolean m_inclusion;		// True if inclusion arc

	/* Used for simplex algorithm */

	private boolean m_reversed  = false;// Set if arc has been reversed to avoid cycles
	private int		m_cutvalue  = 0;	// The cut value when this arc in the spanning tree is cut
	private int		m_weight    = 0;	// The greater the weight the shorter the ideal length
	private int		m_minlength = 0;	// The minimum length of this arc
	
	int				m_fromX;
	int				m_fromY;
	int				m_toX;
	int				m_toY;

	/**
	 * Constructor.
	 */

	HiArc(HiGraph from_node, HiGraph to_node, boolean inclusion) {
		super();
		m_from      = from_node;
		m_to   	    = to_node;
		m_inclusion = inclusion;
	}

	HiArc(HiGraph from_node, HiGraph to_node) {
		this(from_node, to_node, false);
	}

	/* Provide a short string description of a HiArc node */

	public String toString() {
		String s = "";
		String operator;

		s = "(weight=" + m_weight + " minlength=" + m_minlength + ")";
		
		if (m_from == null || m_to == null) {
			System.out.println("Broken arc");
		}
		operator = (m_inclusion ? "=" : "-");
		if (m_reversed) {
			s += m_from.label() + "<" + operator + m_to.label();
		} else {
			s += m_from.label() + operator + ">" + m_to.label();
		}
		
		return(s);
	}

	public HiGraph from() {
		return(m_from);
	}

	void from(HiGraph value) {		// Used to change edges to introduce dummy nodes
		m_from = value;
	}

	public HiGraph to() {
		return(m_to);
	}

	public boolean onSide()
	{
		return(false);
	}

	public boolean inclusion() {
		return(m_inclusion);
	}

	void reverse(boolean flag) {
		m_reversed = flag;
	}

	void reverse() {
		HiGraph temp;

		temp       = m_from;
		m_from     = m_to;
		m_to       = temp;
		m_reversed = !m_reversed;
	}

	boolean reversed() {
		return(m_reversed);
	}

	void cutValue(int value) {
		m_cutvalue = value;
	}

	int cutValue() {
		return(m_cutvalue);
	}

	public void setWeight(int value) {
		m_weight = value;
	}

	int getWeight() {
		return(m_weight);
	}

	void setMinlength(int value) {
		m_minlength = value;
	}

	int getMinlength() {
		return(m_minlength);
	}

	/* Eliminate this arc. */

	void dispose() throws HiGraphException {
			
		if (m_from == null) {
			throw new HiGraphException("Removing a non-existant arc");
		}
		m_from = null;
		m_to   = null;
	}
}
