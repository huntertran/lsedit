package search;

import java.sql.*;
import com.microsoft.sqlserver.jdbc.*;

public class Search {
	
	static Connection			m_conn    = null;
	static String[]				m_uuids   = null;

	private static void
	scan_column(String catalog, String schema, String table_name, String column_name) throws Exception
	{
		Statement			stmt    = m_conn.createStatement();
		ResultSet			rs;
		String				sql;
		String				value;
		int					i, values, cnt;

		sql = "select \"" + column_name + "\" from " + catalog + "." + schema + "." + table_name + " where not \"" + column_name + "\" is null";

		try {
			rs = stmt.executeQuery(sql);
			
			values  = m_uuids.length;
			cnt     = 0;
			
			while (rs.next()) {
				value = rs.getString(1);
				for (i = 0; i < values; ++i) {
					if (value.equals(m_uuids[i])) {
						++cnt;
						break;
				}	}
/*
				value = value.toLowerCase();
				if (value.endsWith(".jpg") || value.endsWith("_32")) {
					System.out.println(catalog + "." + schema + table_name + "." + column_name + " (" + value + ")");
				}
*/
			}	
			if (cnt > 0) {
				System.out.println(schema + "." + table_name + "." + column_name + " (" + cnt + ")");
			}
			
			rs.close();
		} catch (Exception e) {
			System.out.println(sql);
			System.out.println(e.getMessage());
		}
		stmt.close();
	}			
	
	private static void
	scan_tables() throws Exception
	{
		Statement			stmt    = m_conn.createStatement();
		ResultSet			rs;

		rs = stmt.executeQuery(
			"select table_catalog, table_schema, table_name, column_name from information_schema.columns where table_catalog = 'mdb' and table_schema = 'dbo' and ordinal_position = 1 order by table_catalog,table_schema, table_name, ordinal_position"
		);
		
		for (;rs.next();) {
			scan_column(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4));	
 		}
		rs.close();
		stmt.close();
	}			
	
	private static void
	load_ca_owned_resource() throws Exception
	{
		Statement			stmt    = m_conn.createStatement();
		ResultSet			rs;
		int					i, size;

		rs = stmt.executeQuery(
			"select count(*) from dbo.nr_com"
		);
		
		rs.next();
		
		size = rs.getInt(1);
		
		m_uuids = new String[size];
		
		rs.close();
		
		rs = stmt.executeQuery(
			"select writer_id from dbo.nr_com"
		);
		
        for (i = 0; rs.next(); ++i) {
			m_uuids[i] = rs.getString(1);	
 		}
		rs.close();
		stmt.close();
	}			
	
	public static void main(String args[]) 
	{
		String msg = null;
		
		System.out.println("Connecting to mdb");
		try {
			// Establish the connection. 
			SQLServerDataSource ds = new SQLServerDataSource();
			// ds.setDatabaseName("IDB");
			// ds.setDatabaseName("betatest");
			ds.setDatabaseName("mdb");
			ds.setUser("sa");
			ds.setPassword("unicenter");
			ds.setServerName("localhost");
			ds.setPortNumber(1433); 

			m_conn = ds.getConnection();
		} catch (Exception e) {
			System.out.println( "Unable to connect to CMDB: " + e.getMessage());
			return;
		}
		
		try {
			load_ca_owned_resource();
			scan_tables();		
		} catch (Exception e) {
			StackTraceElement[] stack = e.getStackTrace();
			int					i;

			msg = "Unable to load CMDB: " + e.getMessage() + "\n\n";
			for (i = stack.length; i > 0; ) {
				msg += stack[--i].toString() + "\n";
			}
		}
		System.out.println("Loaded mdb");
		
		try {
			m_conn.close();
		} catch (Exception e) {
			System.out.println("Unable to close connection: " + e.getMessage());
		}
		if (msg != null) {
			System.out.println(msg);
		}
	}
}
