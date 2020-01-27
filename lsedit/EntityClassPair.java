package lsedit;

/* This class holds a relation class schema constraint */

public class EntityClassPair extends Object {

	public EntityClass		m_entityClass1;
	public RelationClass	m_rc;
	public EntityClass		m_entityClass2;

	public EntityClassPair(EntityClass ec1, RelationClass rc, EntityClass ec2) 
	{
		m_entityClass1 = ec1;
		m_rc           = rc;
		m_entityClass2 = ec2;
	}

	public boolean equals(EntityClass ec1, EntityClass ec2) 
	{
		return (ec1 == m_entityClass1 && ec2 == m_entityClass2);
	}
}

