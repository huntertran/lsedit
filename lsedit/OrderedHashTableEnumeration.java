package lsedit;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class OrderedHashTableEnumeration
{
	public static Enumeration elements(Hashtable ht) 
	{
		Vector		vector = new Vector(ht.size());

		for (Enumeration en = ht.elements(); en.hasMoreElements(); ) {
			vector.add(en.nextElement());
		}

		SortVector.byId(vector);
		return(vector.elements());
}	}


