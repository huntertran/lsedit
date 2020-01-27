package acdc;
import java.io.IOException;
import java.io.BufferedReader;

/**
 * A class to turn an TA input stream into a stream of tokens.
 * @version 1.0, 22/07/98
 * @author	Gary Farmaner
 */

public class TA_StreamTokenizer extends Object {

	private static final byte CT_DELIMIT	= 2;

	private BufferedReader m_is;

	private char		m_buf[];
	private int			m_peekc = ' ';	// Will appear to be first character but ignored so ok
	private boolean		m_pushedBack;	// True if last token pushed back to be reconsumed on next call
	private boolean		m_escaped;

	/** The line number of the last token read */

	private int			m_lineno = 1;
	private byte		m_ctype[] = new byte[256];

	private static final int IBUF_SIZE = 8 * 1024;

	private char[]	m_ibuf = new char[IBUF_SIZE];
	private int		m_cnt  = 0;
	private int		m_pos  = 0;

	/**
	 * The type of the last token returned.	 It's value will either
	 * be one of the following TT_* constants, or a single
	 * character.  For example, if '+' is encountered and is
	 * not a valid word character, ttype will be '+'
	 */

	private int m_ttype;

	public static final int TT_EOF    = -1;				// The End-of-file token.
	public static final int TT_WORD   = -2;				// The word token.	This value is in sval.

	/**
	 * The Stream value.
	 */

	protected String m_sval;

	/**
	 * The number value.
	 */

	public TA_StreamTokenizer () 
	{
		m_buf         = new char[20*1024];

		m_ctype[' ']  = CT_DELIMIT;
		m_ctype['\t'] = CT_DELIMIT;
		m_ctype['\r'] = CT_DELIMIT;
		m_ctype['\n'] = CT_DELIMIT;
		m_ctype['=']  = CT_DELIMIT;
//		m_ctype[':']  = CT_DELIMIT;
		m_ctype['{']  = CT_DELIMIT;
		m_ctype['}']  = CT_DELIMIT;
		m_ctype['(']  = CT_DELIMIT;
		m_ctype[')']  = CT_DELIMIT;
	}

	public void setInputStream(BufferedReader is)
	{
		m_is = is;
	}

	private final int cread() throws IOException 
	{
		int	c;

		c         = m_peekc;
		m_escaped = false;
		if (m_pos == m_cnt) {
			if (c == TT_EOF || (m_cnt = m_is.read(m_ibuf, 0, IBUF_SIZE)) < 1) {
				m_pos   = m_cnt;
				m_peekc = TT_EOF;
//				System.err.print("\n" + ((char) c));
				return c;
			}
			m_pos = 0;
		}
		m_peekc = (int) m_ibuf[m_pos++];
		switch (c) {
		case '\\':
			switch (m_peekc) {
			case 'n':
				c = '\n';
				break;
			case 't':
				c = '\t';
				break;
			case 'f':
				c = '\f';
				break;
			case 'r':
				c = '\r';
				break;
			case 'e':
				c = 27;
				break;
			case 'd':
				c = 127;
			case '\\':
				m_peekc = ' ';
				break;
			case '"':
				c = '"';
				break;
			case '\'':
				c = '\'';
				break;
			default:
				return c;
			}
			cread();	// Consume and discard next character
			m_escaped = true;
			break;
		case '\r':
			if (m_peekc == '\n') {
				break;
			}
		case '\n':
			++m_lineno;
		}
//		System.err.print("" + ((char) c));
		return c;
	}

	/** Does an unget of the last token */

	protected final void pushBack() 
	{
		m_pushedBack = true;
	} 

	/** Return the current line number. */
	public int lineno() 
	{
		return m_lineno;
	}

	/**
	 * Parses a token from the input stream.  The return value is
	 * the same as the value of ttype.	Typical clients of this
	 * class first set up the syntax tables and then sit in a loop
	 * calling nextToken to parse successive tokens until TT_EOF
	 * is returned.
	 */

	public final int nextToken() throws IOException 
	{
		if (m_pushedBack) {
			m_pushedBack = false;
			return m_ttype;
		}

		int c;
		int pos = 0;

		for (m_sval = null; ; ) {
			// This loop is repeated while initial whitespace
			c = cread();

			switch(c) {
			case TT_EOF:
				m_sval = "EOF";
				return m_ttype = TT_EOF;
			case '\r':
			case '\n':
			case '\t':
			case ' ':
				continue;
			case '"':
				for (;;) {
					c = cread();
					switch (c) {
					case TT_EOF:
						System.out.println("Unterminated \" in input");
					case '"':
						if (!m_escaped) {
							break;
						}
					default:	
						m_buf[pos++] = (char) c;
						continue;
					}
					break;
				} 
				m_sval = new String(m_buf, 0, pos);
				return m_ttype = TT_WORD;
			case '\'':
				for (;;) {
					c = cread();
					switch (c) {
					case TT_EOF:
						System.out.println("Unterminated ' in input");
					case '\'':
						if (!m_escaped) {
							break;
						}
					default:	
						m_buf[pos++] = (char) c;
						continue;
					}
					break;
				} 
				m_sval = new String(m_buf, 0, pos);
				return m_ttype = TT_WORD;
			case '/':
				switch (m_peekc) {
				case '/':	
					// Toss the remainder of the line
					for (;;) {
						c = cread();
						switch (c) {
						case '\r':
							if (m_peekc == '\n') {
								continue;
							}
						case TT_EOF:
						case '\n':
							break;
						default:
							continue;
						}
						break;
					}
					continue;
				case '*':
					// Toss the remainder of the comment
					for (;;) {
						c = cread();
						switch (c) {
						case '*':
							if (m_peekc != '/') {
								continue;
							}
							c = cread();
						case TT_EOF:
							break;
						default:
							continue;
						}
						break;
					}
					continue;
				}
			default:
				if (m_ctype[c] == CT_DELIMIT) {
					m_sval = String.valueOf((char) c);
					return m_ttype = c;
				}

				// Read characters until delimitter

				for (;;) {
					m_buf[pos++] = (char) c;
					if (m_peekc == TT_EOF) {
						break;
					}
					if (c == '\\') {
						m_buf[pos++] = (char) cread();
					} 
					if (m_ctype[m_peekc] == CT_DELIMIT) {
						break;
					}
					c = cread();
				}
				m_sval = new String(m_buf, 0, pos);
				if (pos == 1 && m_buf[0] == ':') {
					return m_ttype = ':';
				}
				return m_ttype = TT_WORD;
			}
		}
	}
}

