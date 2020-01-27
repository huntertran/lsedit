package lsedit;

import java.awt.FontMetrics;

public class StringLinizer extends Object {

	protected FontMetrics fm;
	protected int		  width;

	protected String	  input;
	protected char[]	  inputchars;
	protected int		  start;
	protected int		  lth;

	private static char[]  a0 = {'<','a',' ','h','r','e','f','=',0};

	// Exported routines

	
	public StringLinizer(String str, FontMetrics fm, int width) {

		this.fm	   = fm;
		this.width = width;
		this.input = str;
		start	   = 0;
		lth		   = str.length();
		inputchars = input.toCharArray();
	}


	public boolean hasMoreLines()
	{
		return (start < lth);
	}

	// This mind boggling logic is needed because "sometimes" lines of
	// text are written to text boxes which support HTML.  We must factor
	// out the widths of markup language not actually presented.

	public int charsWidth(int start, int toend)
	{
		int		i, c, width;
		boolean inText;

		inText	   = true;
		width	   = 0;

		for (i = start; i < toend; ++i) {
			c = inputchars[i];
			switch (c) {
			case '<':
				inText = false;
				continue;
			case '>':
				if (inText) {
					width = 0;
					continue;
				}
				inText = false;
				continue;
			}
			if (inText) {
				width += fm.charWidth(c);
		}	}
		return(width);
	}

	public String nextLine() 
	{
		String	rest;
		int		at, best, state, subwidth, ind;

		for (best = start; best < lth; ++best) {
			if (inputchars[best] == '\\') {
				if (inputchars[best+1] == 'n') {
					break;
				}
			} else if (inputchars[best] == '\n') {
				break;
		}	}

		// Best is now at end of line or at '\n' or at '\\n'

		if (charsWidth(start, best) <= width) {
			// Simple case -- rest of string fits in one line
			at = best;
		} else {

			// Normal case break at some sort of word boundary

			--best;
			state = 0;
			for (at = best; at > start;) {
				switch (inputchars[--at]) {
				case '\r':
				case '<':
					state = 1;	// This can start a new line
					continue;
				case '\n':
				case '\t':
				case ' ':
				case '=':
				case '"':
				case '>':
				case '-':
					state = 1;			// This can terminate a line or start a new line
					break;
				default:
					if (state != 0) {	// If next character may start a new line
						state = 0;		// this character may terminate a line
						break;
					}
					continue;
				}
				subwidth = charsWidth(start, at+1);

				if (subwidth <= width) {
					++at;
					break;
				}
				best = at;
			}
			if (at <= start) {
				// Bad case -- can't fit one word onto a line (fit as much as possible)

				for (at = best; --at > start;) {
					subwidth = charsWidth(start, at);
					if (subwidth <= width) {
						break;
			}	}	}

			if (at <= start) {

				// Terrible case can't fit a single character to a line
				// Discard the rest of the string and return an empty string

				start = lth;
				return("");		
		}	}

		rest  = input.substring(start, at);		// Returns string from offset start to offset at-1

		start = at;

		// Thow away newline character if at one

		if (start < lth) {
			switch (inputchars[start]) {
			case '\\':
				if (start < lth-1 && inputchars[start+1] != 'n') {
					start += 2;
				}
				break;
			case '\n':
				++start;
		}	}

		while ((ind = rest.indexOf('\t')) >= 0) {
			String a, b;

			if (ind != 0) {
				a = rest.substring(0, ind);
			} else {
				a = "";
			}
			if (ind < rest.length()-1) {
				b = rest.substring(ind+1);
			} else {
				b = "";
			}
			rest = a + "    " + b;
		}

		return(rest);
	} 
}

