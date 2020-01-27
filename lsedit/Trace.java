package lsedit;


public class Trace extends Object {

	// Trace methods

	private static final boolean m_supported = true;
	private static       boolean m_enabled   = false;

	private static   int m_depth = 0;

	public static void in(String source, String routine)
	{
		if (m_supported) {
			if (m_enabled) {
				int i;

				for (i = 0; i < m_depth; ++i) {
					System.out.print(" ");
				}
				System.out.println(">" + source + " " + routine);
				++m_depth;
	}	}	}

	public static void out(String source, String routine)
	{
		if (m_supported) {
			if (m_enabled) {
				int i;

				--m_depth;
				for (i = 0; i < m_depth; ++i) {
					System.out.print(" ");
				}
				System.out.println("<" + source + " " + routine);
	}	}	}

	public static void setEnabled(boolean value)
	{
		if (m_supported) {
			m_enabled = value;
	}	}
}