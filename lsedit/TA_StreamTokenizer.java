package lsedit;

import java.io.IOException;
import java.io.Reader;
import java.io.LineNumberReader;

/**
 * A class to turn an TA input stream into a stream of tokens.
 * @version 1.0, 22/07/98
 * @author	Gary Farmaner
 */

/*
class TA_IncludeStack extends Object {

	public TA_IncludeStack	m_next;
	public Reader			m_reader;
	public String			m_filename;
}
*/

public class TA_StreamTokenizer extends Object {

	private static final byte	CT_DELIMIT	= 2;
	private static final byte	CT_QUOTE    = 3;
	public  static final int	TT_EOF      = -1;				// The End-of-file token.
	public	static final int	TT_WORD     = -2;				// The word token.	This value is in sval.

	private Reader		m_reader;
	private String		m_filename;
	private int			m_peekc = ' ';	// Will appear to be first character but ignored so ok
	private boolean		m_pushedBack;	// True if last token pushed back to be reconsumed on next call
	private boolean		m_escaped;

	private static char	m_ctype[];

	/* TODO: Thinks about a more memory efficient -- extensible method perhaps by using StringBuffer 
	         Also try to make this static by changing how include is implemented
	 */

	private int			m_buf_size = 20 * 1024;
	private char[]		m_buf      = new char[m_buf_size];		// The area that captures a token or attribute value

	/**
	 * The type of the last token returned when m_pushBack is true.	 It's value will either
	 * be one of the following TT_* constants, or a single character.  For example, if '+' is
	 * encountered and is not a valid word character, ttype will be '+'
	 */

	private int m_ttype = ' ';


	/**
	 * The Stream value.
	 */

	protected String m_sval;


	public String	m_comments = null;

//	private TA_IncludeStack m_stack = null;

	private void dumpBuf(int lth)
	{
		int i;
					
		System.out.print("(" + lth + ") ");
		for (i = 0; i < lth && i < 30; ++i) {
			System.out.print(m_buf[i]);
		}
		if (lth > 30) {
			System.out.print(" ... ");
			i = lth - 30;
			if (i < 30) {
				i = 30;
			}
			for (; i < lth; ++i) {
				System.out.print(m_buf[i]);
			}
	}	}

	private int enlargeBuffer()
	{
		int		buf_size     = m_buf_size;
		int		new_buf_size = buf_size * 2;
		char[]	new_buf      = new char[new_buf_size];
	
/*	
		{
			System.out.print("Overflow :");
			dumpBuf(buf_size);
			System.out.println("");
			java.lang.Thread.dumpStack();
		}
*/
		System.arraycopy(m_buf, 0, new_buf, 0, buf_size); 
		m_buf_size = new_buf_size;
		m_buf      = new_buf;
		
		return(new_buf_size);
	}							

	protected void close()
	{
		try {
			m_reader.close();
		} catch (Exception e) {
			System.out.println("TA_StreamTokenizer::close() " + e.getMessage());
		}
		m_reader = null;
//		m_stack  = null;
	}

	public TA_StreamTokenizer(Reader reader, String filename) 
	{
		if (m_ctype == null) {
			m_ctype = new char[256];
			m_ctype[' ']  = CT_DELIMIT;
			m_ctype['\f'] = CT_DELIMIT;
			m_ctype['\t'] = CT_DELIMIT;
			m_ctype['\r'] = CT_DELIMIT;
			m_ctype['\n'] = CT_DELIMIT;
			m_ctype['=']  = CT_DELIMIT;
//			m_ctype[':']  = CT_DELIMIT;
			m_ctype['{']  = CT_DELIMIT;
			m_ctype['}']  = CT_DELIMIT;
			m_ctype['(']  = CT_DELIMIT;
			m_ctype[')']  = CT_DELIMIT;
//			m_ctype['"']  = CT_QUOTE;
//			m_ctype['\''] = CT_QUOTE;
		}
		m_reader      = reader;
		m_filename    = filename;
	}

	/* We presume that the reader is sensibly buffered externally */

	private final int charToken() throws IOException 
	{
		int	c;

		c         = m_peekc;
		m_peekc   = m_reader.read();
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
				m_escaped = true;
				break;
			case '\'':
				c = '\'';
				m_escaped = true;
				break;
			default:
				c = m_peekc;
			}
			charToken();	// Consume and discard m_peekc
			break;
		case '\'':
		case '"':
			m_escaped = false;
		}
//		System.err.print("" + ((char) c));
		return c;
	}

	/** Does an unget of the last token */

	protected final void pushBack(int ttype) 
	{
		m_ttype      = ttype;
		m_pushedBack = true;
	} 

	public String filename()
	{
		return m_filename;
	}

	/** Return the current line number. */

	public int lineno() 
	{
		if (m_reader instanceof LineNumberReader) {
			return ((LineNumberReader) m_reader).getLineNumber();
		}
		return 0;
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

		int		buf_size = m_buf_size;
		char[]	buf      = m_buf;
		int		pos      = 0;
		int		c;

		for (m_sval = null; ; ) {
			// This loop is repeated while initial whitespace
			c = charToken();

			switch(c) {
			case TT_EOF:
				m_sval = "EOF";
				return TT_EOF;
			case '\f':
			case '\r':
			case '\n':
			case '\t':
			case ' ':
				continue;
			case '"':
				for (;;) {
					c = charToken();
					switch (c) {
					case TT_EOF:
						System.out.println("Unterminated \" in input");
					case '"':
						if (!m_escaped) {
							break;
						}
					default:
						if (pos >= buf_size) {
							buf_size = enlargeBuffer();
							buf      = m_buf;
						}	
						buf[pos++] = (char) c;
						continue;
					}
					break;
				} 
				m_sval = StringCache.get(buf, pos);
				return TT_WORD;
			case '\'':
				for (;;) {
					c = charToken();
					switch (c) {
					case TT_EOF:
						System.out.println("Unterminated ' in input");
						break;
					case '\'':
						if (!m_escaped) {
							break;
						}
					default:	
						if (pos >= buf_size) {
							buf_size = enlargeBuffer();
							buf      = m_buf;
						}	
						buf[pos++] = (char) c;
						continue;
					}
					break;
				} 
				m_sval = StringCache.get(buf, pos);
/*
				if (pos < 30) {
					System.out.println("Quote: " + m_sval);
				} else {
					System.out.println("Quote: " + m_sval.substring(0,30) + " ...");
				}
 */
				return TT_WORD;
			case '/':
				switch (m_peekc) {
					case '/':
					{
						String comment;

						if (m_comments == null) {
							comment = null;
						} else {
							comment = "/";
						}
						// Toss the remainder of the line
						for (;;) {
							c = charToken();
							if (comment != null && c != TT_EOF) {
								comment += (char) c;
							}
							switch (c) {
							case '\r':
								if (m_peekc == '\n') {
									continue;
								}
							case '\n':
							case TT_EOF:
								break;
							default:
								continue;
							}
							break;
						}
						if (comment != null) {
							m_comments += comment;
						}
						continue;
					}
					case '*':
					{
						String comment;

						// Toss the remainder of the comment
						if (m_comments == null) {
							comment = null;
						} else {
							comment = "/*";
						}
						for (;;) {
							c = charToken();
							if (comment != null && c != TT_EOF) {
								comment += (char) c;
							}
							switch (c) {
							case '*':
								if (m_peekc != '/') {
									continue;
								}
								c = charToken();
								if (comment != null) {
									comment += "/\n";
								}
							case TT_EOF:
								break;
							default:
								continue;
							}
							break;
						}
						if (comment != null) {
							m_comments += comment;
						}
						continue;
				}	}
			default:
				if (c < m_ctype.length && m_ctype[c] == CT_DELIMIT) {
					m_sval = String.valueOf((char) c);
					return c;
				}

				// Read characters until delimitter

				for (;;) {
					if (pos >= buf_size) {
						buf_size = enlargeBuffer();
						buf      = m_buf;
					}	
					buf[pos++] = (char) c;
					if (m_peekc == TT_EOF) {
						break;
					}
					if (c == '\\') {
						if (pos >= buf_size) {
							buf_size = enlargeBuffer();
							buf      = m_buf;
						}	
						buf[pos++] = (char) charToken();
					} 
					if (c < m_ctype.length && m_ctype[m_peekc] != 0) {
						break;
					}
					c = charToken();
				}
				m_sval = StringCache.get(buf, pos);
				if (pos == 1 && buf[0] == ':') {
					return ':';
				}
				return TT_WORD;
			}
		}
	}

	private final int charAVI() throws IOException 
	{
		int	c;

		c         = m_peekc;
		m_peekc   = m_reader.read();
//		System.err.println("char :" + ((char) c));
		return c;
	}

	public final String nextAVI() throws IOException 
	{
		int		depth		 = 0;
		int		buf_size     = m_buf_size;
		char[]	buf          = m_buf;
		int		pos			 = 0;
		int		tokens		 = 0;
		int		startbracket = -1;
		int		i, c, start;
		boolean	escaped, simple;
//		String	ret;

		for (; ; ) {
			// This loop is repeated while initial whitespace
			c = charAVI();

			switch(c) {
			case TT_EOF:
				return null;
			case '\f':
			case '\r':
			case '\n':
			case '\t':
			case ' ':
				continue;
			case '"':
				++tokens;
				start        = pos;
				buf[pos++]   = '"';
				escaped      = false;
				simple       = true;
				for (;;) {
					c = charAVI();
					switch (c) {
					case TT_EOF:
						System.out.println("Unterminated \" in input");
					case '\\':
						escaped      = !escaped;
						if (pos >= buf_size) {
							buf_size = enlargeBuffer();
							buf      = m_buf;
						}	
						buf[pos++] = '\\';
						simple     = false;
						continue;
					case '"':
						if (!escaped) {
							break;
						}
					case '\'':
						simple = false;
					default:
						if (c < m_ctype.length && m_ctype[c] == CT_DELIMIT) {
							simple = false;
						}
						if (pos >= buf_size) {
							buf_size = enlargeBuffer();
							buf      = m_buf;
						}							
						buf[pos++] = (char) c;
						escaped    = false;
						continue;
					}
					break;
				} 
				if (!simple) {
					if (pos >= buf_size) {
						buf_size = enlargeBuffer();
						buf      = m_buf;
					}	
					buf[pos++] = '"';
				} else {
					// Remove the double quotes so that integers etc parse easily
					for (--pos; ++start <= pos; buf[start-1] = buf[start]);
				}
				break;
			case '\'':
				++tokens;
				start        = pos;
				buf[pos++]   = '\'';
				escaped      = false;
				simple       = true;
				for (;;) {
					c = charAVI();
					switch (c) {
					case TT_EOF:
						System.out.println("Unterminated ' in input");
						break;
					case '\\':
						escaped      = !escaped;
						if (pos >= buf_size) {
							buf_size = enlargeBuffer();
							buf      = m_buf;
						}	
						buf[pos++] = '\\';
						simple     = false;
						continue;
					case '\'':
						if (!escaped) {
							break;
						}
					case '"':
						simple = false;
					default:	
						if (c < m_ctype.length && m_ctype[c] == CT_DELIMIT) {
							simple = false;
						}
						if (pos >= buf_size) {
							buf_size = enlargeBuffer();
							buf      = m_buf;
						}							
						buf[pos++] = (char) c;
						escaped    = false;
						continue;
					}
					break;
				} 
				if (!simple) {
					if (pos >= buf_size) {
						buf_size = enlargeBuffer();
						buf      = m_buf;
					}							
					buf[pos++] = '\'';
				} else {
					// Remove the single quotes so that integers etc parse easily
					for (--pos; ++start <= pos; buf[start-1] = buf[start]);
				}
				break;
			case '(':
				startbracket = pos;
				if (pos >= buf_size) {
					buf_size = enlargeBuffer();
					buf      = m_buf;
				}							
				buf[pos++] = '(';
				if (depth != 0) {
					tokens = 2;	// Force later test to fail -- don't want to treat ((1)) as 1
				}
				++depth;
				continue;
			case ')':
				if (depth <= 0) {
					// Ignore the ')' -- treat as parse error
					System.out.println("Bad nesting of () at " + lineno());
					return null;
				} 
				switch (buf[pos-1]) {
				case '(':
					// Omit () altogether
					System.out.println("Empty nesting of () at " + lineno());
					--pos;
					break;
				case ' ':
					// Omit ' ' before ')'
					--pos;
				default:
					if (tokens != 1) {
						if (pos >= buf_size) {
							buf_size = enlargeBuffer();
							buf      = m_buf;
						}							
						buf[pos++] = ')';
					} else {
						// Drop the brackets
						--pos;
						for (i = startbracket; i < pos; ++i) {
							buf[i] = buf[i+1];
				}	}	}
				--depth;
				break;
			case '/':
				switch (m_peekc) {
					case '/':
					{
						String comment;

						if (m_comments == null) {
							comment = null;
						} else {
							comment = "/";
						}
						// Toss the remainder of the line
						for (;;) {
							c = charAVI();
							if (comment != null && c != TT_EOF) {
								comment += (char) c;
							}
							switch (c) {
							case '\r':
								if (m_peekc == '\n') {
									continue;
								}
							case '\n':
							case TT_EOF:
								break;
							default:
								continue;
							}
							break;
						}
						if (comment != null) {
							m_comments += comment;
						}
						continue;
					}
					case '*':
					{
						String comment;

						// Toss the remainder of the comment
						if (m_comments == null) {
							comment = null;
						} else {
							comment = "/*";
						}
						for (;;) {
							c = charAVI();
							if (comment != null && c != TT_EOF) {
								comment += (char) c;
							}
							switch (c) {
							case '*':
								if (m_peekc != '/') {
									continue;
								}
								c = charAVI();
								if (comment != null) {
									comment += "/\n";
								}
							case TT_EOF:
								break;
							default:
								continue;
							}
							break;
						}
						if (comment != null) {
							m_comments += comment;
						}
						continue;
				}	}
			default:
				if (c < m_ctype.length && m_ctype[c] == CT_DELIMIT) {
					pushBack(c);
					return null;
				}

				// Read characters until delimitter

				++tokens;
				start    = pos;
				simple   = true;
				for (;;) {
					if (c == '\\') {
						simple = false;
					}
					if (pos >= buf_size) {
						buf_size = enlargeBuffer();
						buf      = m_buf;
					}
					buf[pos++] = (char) c;
					if (m_peekc == TT_EOF) {
						break;
					}
					if (c < m_ctype.length && m_ctype[m_peekc] != 0) {
						break;
					}
					c = charAVI();
				}
				if (!simple) {
					// If the string contains \ then quote it so that parseString kept simple
					if (pos >= buf_size) {
						buf_size = enlargeBuffer();
						buf      = m_buf;
					}							
					for (i = pos; i >= start; --i) {
						buf[i+1] = buf[i];
					}
					buf[start] = '"';
					if (pos >= buf_size) {
						buf_size = enlargeBuffer();
						buf      = m_buf;
					}							
					buf[pos++] = '"';
				}
				break;
			}
			if (depth > 0) {
				if (pos >= buf_size) {
					buf_size = enlargeBuffer();
					buf      = m_buf;
				}							
				buf[pos++] = ' ';
				continue;
			}
			if (tokens == 0) {
				return null;
			}

/*
			System.out.println("token :");
			dumpBuf(pos);
			System.out.println("");
			String	ret = StringCache.get(buf, pos);
			return ret;
*/
			return StringCache.get(buf, pos);
		}
	}
}

