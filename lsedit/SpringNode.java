package lsedit;

class SpringNode {
	public EntityInstance	m_e;			// Entity represented
	double					m_x;			// Computed final position
	double					m_y;			// Computed final position
	double					m_xForce;
	double					m_yForce;
	int						m_forces;
	int						m_clients;		// Number of edges from this to clients
	int						m_suppliers;	// Number of edges from this to suppliers
};

